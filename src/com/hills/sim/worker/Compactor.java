package com.hills.sim.worker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hills.sim.Constants;
import com.hills.sim.exceptions.CompactorExc;
import com.hills.sim.settings.GeneralSettings;
import com.hills.sim.settings.SymbolSettings;
import com.hills.sim.worker.CompactorCandle;

public class Compactor {
	
	private final Connection connection;
	
	private final String table_name;
	private final Map<String, String> table_fields;
	private final LocalDateTime[] period_range;
	
	private final DateTimeFormatter dt_format = 
			DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private float margin_rate;
	private int pip;
	private int distance;
	private float contract_size_min;
	private float contract_size_int;
	
	/**
	 * Make Compactor
	 * @throws CompactorExc
	 */
	public Compactor(SymbolSettings sym_settings, GeneralSettings gen_settings) 
															throws CompactorExc {
		
//		Make the connection
    	try {
//			Specify the location of the driver
			Class.forName(Constants.DB_DRIVER);
			this.connection = DriverManager.getConnection
					(Constants.DB_URL, Constants.DB_USR, Constants.DB_PWD);
			
		} catch (Exception e) {
			String error_msg = "%s: Error connecting to DB\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new CompactorExc();
		}
    	
    	
//    	Set the private final members
    	try {
    		String src = sym_settings.getSource();
    		String sym = sym_settings.getName();
    		String tf = sym_settings.getTimeframe();
    		
    		LocalDateTime[] period_range = gen_settings.getPeriodRange();
    		
    		this.table_name = this.setTableName(src, sym, tf);
    		this.table_fields = this.setTableFields();
    		this.period_range = this.setPeriodRange(period_range);
    		
    	} catch(CompactorExc e) {
			String error_msg = 
					"%s: Error initializing table name, fields, and period range\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new CompactorExc();
    	}
    	
    	
//    	Set the SymbolSettings required data
		try {
			String sym = sym_settings.getName();
			this.setSymbolData(sym);
			
		} catch (CompactorExc e) {
			String error_msg = 
					"%s: Error initializing data required by SymbolSettings\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new CompactorExc();
		}
	}
	

	
	/**
	 * Set the Symbol data, within the Compactor.
	 * This symbol data is referenced by SymbolSettings
	 * @param symbol_name Name of the Symbol
	 * @throws CompactorExc
	 */
	private void setSymbolData(String symbol_name) throws CompactorExc {
		String query = String.format
				("SELECT * FROM Symbol WHERE SymName = '%s';", symbol_name);
		
		try(Statement stmt = this.connection.createStatement()) {
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				this.margin_rate = rs.getFloat("SymMarginRate");
				this.pip = rs.getInt("SymPip");
				this.distance = rs.getInt("SymDistance");
				this.contract_size_min = rs.getFloat("SymContractSizeMin");
				this.contract_size_int = rs.getFloat("SymContractSizeInt");
			}
			
		} catch (SQLException e) {
			String error_msg = "%s: Error saving Symbol data\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new CompactorExc();
		}
	}
	
	
    /**
     * Return a List<CompactorCandle>, given a query
     * @param query String value of SQL query
     * @return
     * @throws CompactorResultException
     */
     public List<CompactorCandle> getCandles(String query) 
    								throws CompactorExc {
    	List<CompactorCandle> result = new ArrayList<>();
    	Map<String, String> lookup = this.table_fields;
    	
//		Parse each record into a candle
		try(Statement stmt = this.connection.createStatement()) {
			ResultSet rs = stmt.executeQuery(query);
			
			while(rs.next()) {
				LocalDateTime time; 
				int[] id_cid_vol;
				float[] ask, bid, mid;

//				Extract data from ResultSet
				time = this.toLocalDateTime
							(rs.getTimestamp(lookup.get("Time")));
				
				int id = rs.getInt(lookup.get("ID"));
				int cid = rs.getInt(lookup.get("ContentID"));
				int vol = rs.getInt(lookup.get("Vol"));
				
				float asko = rs.getFloat(lookup.get("AskO"));
				float askh = rs.getFloat(lookup.get("AskH"));
				float askl = rs.getFloat(lookup.get("AskL"));
				float askc = rs.getFloat(lookup.get("AskC"));
				
				float bido = rs.getFloat(lookup.get("BidO"));
				float bidh = rs.getFloat(lookup.get("BidH"));
				float bidl = rs.getFloat(lookup.get("BidL"));
				float bidc = rs.getFloat(lookup.get("BidC"));
				
				float mido = rs.getFloat(lookup.get("MidO"));
				float midh = rs.getFloat(lookup.get("MidH"));
				float midl = rs.getFloat(lookup.get("MidL"));
				float midc = rs.getFloat(lookup.get("MidC"));

//				Prepare into arrays
				id_cid_vol = new int[] {id, cid, vol};
				ask = new float[] {asko, askh, askl, askc};
				bid = new float[] {bido, bidh, bidl, bidc};
				mid = new float[] {mido, midh, midl, midc};
				
				CompactorCandle candle = 
					new CompactorCandle(time, id_cid_vol, ask, bid, mid);
															 
				result.add(candle);
			}
		}		
		catch(SQLException e) {
			String error_msg = "%s: Error saving CompactorCandles\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new CompactorExc();
		}
    	return(result);
    }
    
	/**
	 * Make a List<String> of SQL queries, that get the candles by day
	 * @return
	 */
 	public List<String> getQueries() {
		List<LocalDateTime> periods = this.getPeriods();
		List<String> result = new ArrayList<>();
		String start, end; 
		
//		For the case where the period range ends within 1 day
		if(periods.size() == 2) {
			LocalDate start_date = periods.get(0).toLocalDate();
			LocalDate end_date = periods.get(1).toLocalDate();
			
			if(start_date.compareTo(end_date) == 0) {
				start = periods.get(0).format(this.dt_format);
				end = periods.get(1).format(this.dt_format);
				result.add(this.makeQuery(start, end));
				return(result);
			}
		}
		
		for(int i = 0; i < periods.size(); i++) {
			LocalDateTime curr = periods.get(i);
			
//			All candles: Start - 00:00:00/User-defined, End - 23:59:59
//			Last candle: Start - 00:00:00, End - User-defined
			if (i == (periods.size() - 1)) {
				LocalDate s_date = curr.toLocalDate();
				LocalTime s_time = LocalTime.parse("00:00:00");
				LocalDateTime s = LocalDateTime.of(s_date, s_time);	
				
				start = s.format(this.dt_format);
				end = curr.format(this.dt_format);
				
			} else {
				LocalDate e_date = curr.toLocalDate();
				LocalTime e_time = LocalTime.parse("23:59:59");
				LocalDateTime e = LocalDateTime.of(e_date, e_time);	
				
				start = curr.format(this.dt_format);
				end = e.format(this.dt_format);
			}
			
			result.add(this.makeQuery(start, end));
		}
		
		return(result);
	}    
    
    /**
     * Set the table name, for future searches
     * @param src Source of the candles (i.e CFD provides Oanda, IG)
     * @param sym Symbol name
     * @param tf Time frame
     * @throws CompactorResultException
     */
    public String setTableName(String src, String sym, String tf) 
    			throws CompactorExc {
    	
    	boolean exist;
    	String result = null;
    	
//    	Check that table name exists. Throw exception if
//    	table names cannot be retrieved or when the table name don't exist
    	try {
    		exist = this.checkTableName(src, sym, tf);
    		
			if(exist == false) {
	    		System.err.printf("%s: Table is missing\n", this.getClass().getName());
	    		throw new CompactorExc();
			}
			
		} catch (CompactorExc e) {
    		System.err.printf("%s: Table is missing\n", this.getClass().getName());
			e.printStackTrace();
			throw new CompactorExc();
		}
    	
    	if(exist == true) result = this.makeTableName(src, sym, tf);
    	
    	return(result);
    }
    
    
    /**
     * Set the table fields, for easy reference
     * @throws CompactorExc
     */
    public Map<String, String> setTableFields() throws CompactorExc {
    	Map<String, String> result = null;
    	
//    	Check that table name is set and table fields are unset
    	if(this.table_name == null) {
    		System.err.printf("%s: Table name member is unset\n", 
					this.getClass().getName());
    		throw new CompactorExc();
    		
    	} else if(this.table_fields != null) {
    		System.err.printf("%s: Table fields member has been set\n", 
					this.getClass().getName());
    		throw new CompactorExc();
    	}
    	
    	result = new HashMap<>();
    	for(String field: Constants.DB_TABLE_FIELDS)	
    		if(field.equals("ContentID")) result.put(field, field);
    		else result.put(field, this.table_name + "__" + field);
    	
    	return(result);
    	
    }
    
    
    /**
     * Set the period range member, for generating candles during runs
	 * @param settings Bounded LocalDateTime containing the start and end
     * @throws CompactorResultException
     */
	public LocalDateTime[] setPeriodRange(LocalDateTime[] settings) 
									throws CompactorExc {
		boolean valid = false;
		LocalDateTime[] result = null;
		
//		Ensures the period member has not been set
		if(this.period_range != null) {
    		System.err.printf("%s: Period range member has been set\n", 
					this.getClass().getName());
    		throw new CompactorExc();			
		}
		
//		Check the period bounds gotten from settings
		try {
			valid = this.isValidPeriodBounds(settings);
			
		} catch (CompactorExc e) {
			String error_msg = "%s: Invalid period bounds\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new CompactorExc();
		}
		
//		Set the period range member
		if(valid == true) result = settings;
		
		return(result);
	}
	
	
    /**
     * True: Tablename exists; False: Tablename doesn't exist
     * @param src Source of the candles (i.e CFD provides Oanda, IG)
     * @param sym Symbol name
     * @param tf Time frame
     * @return
     * @throws CompactorQueryException
     */
    private boolean checkTableName(String src, String sym, String tf) 
    				throws CompactorExc {
    	boolean result = false;
    	
    	String tablename = this.makeTableName(src, sym, tf);
    	
    	try {
			List<String> db_tablenames = this.getTableNames();
			result = db_tablenames.contains(tablename);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new CompactorExc();
		}
    	
    	return(result);
    }
    
    
	/**
	 * Creates the table name, for a search
	 * @return tablename
	 */
	private String makeTableName(String src, String sym, String tf) {
		
		String result = String.format
							(Constants.DB_TABLE_FMT, src, tf, sym);
		
		result = result.toUpperCase();
		return(result);
	}
	
	
	/**
	 * Get the List<String> of table names in the Compactor, to compare
	 * @return
	 * @throws CompactorQueryException
	 */
	private List<String> getTableNames() throws CompactorExc {
		List<String> result = new ArrayList<>();
		
		String query = "SELECT table_name FROM information_schema.tables " + 
					   "WHERE table_schema='public' " + 
					   "AND table_type='BASE TABLE';";
		
		try(Statement stmt = this.connection.createStatement()) {
			ResultSet rs = stmt.executeQuery(query);
			
			while(rs.next()) {
				String tablename = rs.getString("table_name")
									 .toUpperCase();
				result.add(tablename);
			}
			
		}		
		catch(SQLException e) {
			String error_msg = 
					"%s: Error getting all tablenames from Compactor\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new CompactorExc();
		}
		return(result);
	}

	
	/**
	 * Check that period bounds from the settings is within the DB period
	 * bounds
	 * @param settings Bounded LocalDateTime containing the start and end
	 * @return
	 * @throws CompactorResultException
	 */
	private boolean isValidPeriodBounds(LocalDateTime[] settings) 
						throws CompactorExc {
		boolean result = true;
		LocalDateTime[] db_bounds;
		
//		Get DB period bounds
		try {
			db_bounds = this.getPeriodBounds();
			
		} catch (CompactorExc e) {
			String error_msg = 
					"%s: Error getting time bounds for comparison\n";
				e.printStackTrace();
				System.err.printf(error_msg, this.getClass().getName());
			throw new CompactorExc();
		}
		
//		Lower bound must be after DB lower bound
//		and before DB upper bound.
//		Upper bound must be after DB lower bound
//		and before DB upper bound.
		
		if(settings[0].isBefore(db_bounds[0])) {
			System.err.println
					("Settings lower bound is before DB lower bound");
			throw new CompactorExc();
		}
		else if(settings[0].isAfter(db_bounds[1])) {
			System.err.println
					("Settings lower bound is after DB upper bound");			
			throw new CompactorExc();
		}
		if(settings[1].isBefore(db_bounds[0])) {
			System.err.println
					("Settings upper bound is before DB lower bound");
			throw new CompactorExc();
		}
		else if(settings[1].isAfter(db_bounds[1])) {
			System.err.println
					("Settings upper bound is after DB upper bound");			
			throw new CompactorExc();
		}
		
		return(result);
	}
	
	
	/**
	 * Get period bounds of the table, from the DB
	 * @return
	 * @throws CompactorQueryException
	 */
	private LocalDateTime[] getPeriodBounds() 
				throws CompactorExc {
		LocalDateTime[] results = new LocalDateTime[2];
		String time_field = this.table_fields.get("Time");
		
		String query_lower = 
				String.format("SELECT %s FROM %s ORDER BY %s ASC LIMIT 1;",
							  time_field,
							  this.table_name,
							  time_field);
		
		String query_upper = 
				String.format("SELECT %s FROM %s ORDER BY %s DESC LIMIT 1;",
							  time_field,
							  this.table_name,
							  time_field);
		
		try {
			results[0] = this.getOneTimeRecord(query_lower);
			results[1] = this.getOneTimeRecord(query_upper);
			
		} catch (CompactorExc e) {
			String error_msg = 
				"%s: Error getting lower/upper time bounds from Compactor\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new CompactorExc();			
		}

		return(results);
	}
	
	
	/**
	 * Helper function to get 1 LocalDateTime from the database
     * @param query String value of SQL query
	 * @return
	 * @throws CompactorQueryException
	 */
	private LocalDateTime getOneTimeRecord(String query) 
				throws CompactorExc {
		LocalDateTime result = null;
			
		try(Statement stmt = this.connection.createStatement()) {
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				
//				Column index begins from 1, not 0.
				result = this.toLocalDateTime(rs.getTimestamp(1));

			}
		}		
		catch(SQLException e) {
			String error_msg = 
					"%s: Error getting 1 LocalDateTime from Compactor\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new CompactorExc();
		}
			
		return(result);
	}
	
	
	/**
	 * Make a LocalDateTime out of a SQL Timestamp object
	 * @param timest Timestamp object
	 * @return
	 */
	private LocalDateTime toLocalDateTime(Timestamp timest) {
		String s = timest.toString().replace(' ', 'T');
		s = s.substring(0, s.lastIndexOf('.'));
		return(LocalDateTime.parse(s));
	}
	

	/**
	 * Get the List<LocalDateTime> from the period range member
	 * @return
	 */
	private List<LocalDateTime> getPeriods() {
		List<LocalDateTime> result = new ArrayList<>();
		
		LocalDateTime start = LocalDateTime.parse
									(this.period_range[0].toString());
		LocalDateTime end = LocalDateTime.parse
									(this.period_range[1].toString());
		
//		Make an index variable of LocalDateTime
		LocalDate i_date = start.toLocalDate();
		LocalTime i_time = LocalTime.parse("00:00:00");
		LocalDateTime i = LocalDateTime.of(i_date, i_time);
		
		LocalDate end_date = end.toLocalDate();
		
//		Add the start and end separately. Add the body via a while loop
		result.add(start);
		while(i.isBefore(end)) {
			i = i.plusDays(1);
			if(i.isBefore(end) && 
			   (i.toLocalDate().compareTo(end_date) < 0)) 
					result.add(i);
		}
		result.add(end);

		return(result);
	}
	
	
	/**
	 * Generate the SQL query, to retrieve the candles
	 * @param start String value of LocalDateTime, without 'T'
	 * @param end String value of LocalDateTime, without 'T'
	 * @return
	 */
	private String makeQuery(String start, String end) {
		String time_field = this.table_fields.get("Time");
		String vol_field = this.table_fields.get("Vol");
		
//		"SELECT * FROM <TABLE> 
		String q_select = String.format("SELECT * FROM %s ",
										this.table_name);
		
//		WHERE <TIME> >= <START> AND <TIME> <= <END> 
		String q_cond_period = String.format(
								   "WHERE %s >= '%s' AND %s <= '%s' ", 
									time_field, start,
									time_field, end);
		
//		AND <VOL> > 0 
		String q_cond_vol = String.format("AND %s > 0 ", vol_field);

//		ORDER BY <TIME> ASC;",
		String q_order = String.format(" ORDER BY %s ASC;", time_field);
		
		String result = q_select + q_cond_period + q_cond_vol + q_order;
		
		return(result);
	}
	
	
	/**
	 * Get the total number of Compactor Candles, given a start and end time period
	 * @return total Total number of compactor candles
	 * @throws CompactorResultException
	 */
	public int getTotalCompactorCandles() throws CompactorExc {
		int result = 0;
		
		if(this.table_name == null ||
				this.table_fields == null ||
				this.period_range == null) {
			String error_msg = 
					"%s: Table name, fields, and/or period ranges are not set.\n";
			System.err.printf(error_msg, this.getClass().getName());
			throw new CompactorExc();
		}
		
		String time_field = this.table_fields.get("Time");
		String vol_field = this.table_fields.get("Vol");
		String start_time = this.period_range[0].format(this.dt_format);
		String end_time = this.period_range[1].format(this.dt_format);
		
		String query = 
				String.format("SELECT COUNT(*) FROM %s "
							+ "WHERE %s >= '%s' AND %s <= '%s' AND %s > 0;",
								this.table_name,
								time_field,
								start_time,
								time_field,
								end_time,
								vol_field);
		
		try(Statement stmt = this.connection.createStatement()) {
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) 
				
//				Column index begins from 1, not 0.
				result = rs.getInt(1);
		}
		
		catch(SQLException e) {
			String error_msg = 
					"%s: Error getting integer from Compactor\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new CompactorExc();
		}
		
		return(result);
	}


	
	/**
	 * Close the open DB connections
	 * @throws CompactorExc
	 */
	public void close() throws CompactorExc {
		
		try {
			this.connection.close();
			
		} catch (SQLException e) {
			String error_msg = "%s: Error closing db connection.\n";
			e.printStackTrace();
			System.out.printf(error_msg, this.getClass().getName());
			throw new CompactorExc();
		}
	}
	
//	Getters
	/**
	 * @return the margin_rate
	 */
	public float getMarginRate() {
		return margin_rate;
	}


	/**
	 * @return the pip
	 */
	public int getPip() {
		return pip;
	}


	/**
	 * @return the distance
	 */
	public int getDistance() {
		return distance;
	}


	/**
	 * @return the contract_size_min
	 */
	public float getContractSizeMin() {
		return contract_size_min;
	}


	/**
	 * @return the contract_size_int
	 */
	public float getContractSizeInt() {
		return contract_size_int;
	}
	
	
	

}
