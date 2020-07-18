package com.hills.sim.unused;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.hills.sim.Constants;
import com.hills.sim.logs.LogRow;
import com.hills.sim.settings.SESettings;


/**
 * LiteCompactor is the Logs and Reports Compactor in Sqlite
 * @author Mark L
 *
 */
public class LiteCompactor {
	
	public static final String ACCOUNTSLOG = "accountsLog";
	public static final String CANDLESLOG = "candlesLog";
	public static final String ORDERSLOG = "ordersLog";

	public static final String SCHEMA_ACCOUNTSLOG =
		  "CREATE TABLE accountsLog ("
		+ " ACC_ID INT NOT NULL PRIMARY KEY," 
		+ " CAN_ID INT NOT NULL,"
		+ " ACC_BALANCE DECIMAL(15,2) NOT NULL," 
		+ " ACC_EQUITY DECIMAL(15,2) NOT NULL," 
		+ " ACC_FREE_MARGIN DECIMAL(15,2) NOT NULL," 
		+ " ACC_USED_MARGIN DECIMAL(15,2) NOT NULL," 
		+ " ACC_MARGIN_LEVEL DECIMAL(15,2) NOT NULL," 
		+ " ACC_CURRENCY VARCHAR(10) NOT NULL," 
		+ " ACC_M_CALC_ENABLED VARCHAR(10) NOT NULL," 
		+ " ACC_STOP_OUT_LEVEL DECIMAL(15,2) NOT NULL," 
		+ " ACC_LEVERAGE DECIMAL(5,2) NOT NULL," 
		+ " ACC_DISTANCE DECIMAL(10,5) NOT NULL," 
		+ " RUN_ID INT NOT NULL,"
		+ " FOREIGN KEY(CAN_ID) REFERENCES candlesLog(CAN_ID));";


	public static final String SCHEMA_CANDLESLOG =
		  "CREATE TABLE candlesLog ("
		+ " CAN_ID INT NOT NULL PRIMARY KEY,"  
		+ " CAN_TIME DATETIME NOT NULL UNIQUE,"  
		+ " CAN_VOLUME DECIMAL(10,0) NOT NULL,"  
		+ " CAN_SPREAD DECIMAL(15,5) NOT NULL,"  
		+ " CAN_BIDO DECIMAL(15,5) NOT NULL," 
		+ " CAN_BIDH DECIMAL(15,5) NOT NULL,"  
		+ " CAN_BIDL DECIMAL(15,5) NOT NULL,"  
		+ " CAN_BIDC DECIMAL(15,5) NOT NULL,"  
		+ " CAN_ASKO DECIMAL(15,5) NOT NULL,"  
		+ " CAN_ASKH DECIMAL(15,5) NOT NULL,"  
		+ " CAN_ASKL DECIMAL(15,5) NOT NULL,"  
		+ " CAN_ASKC DECIMAL(15,5) NOT NULL,"  
		+ " CAN_DIRECTION VARCHAR(10) NOT NULL,"  
		+ " CAN_BODY DECIMAL(15,5) NOT NULL,"  
		+ " CAN_LENGTH DECIMAL(15,5) NOT NULL,"  
		+ " CAN_UNIT_DISTANCE DECIMAL(3,0) NOT NULL,"  
		+ " CAN_UNIT_PIP DECIMAL(3,0) NOT NULL);";


	public static final String SCHEMA_ORDERSLOG =
		  "CREATE TABLE ordersLog ("	
		+ " ORD_ID INT NOT NULL PRIMARY KEY,"  
		+ " ORD_ORDER_ID INT NOT NULL,"  
		+ " CAN_ID INT NOT NULL,"
		+ " ORD_TYPE VARCHAR(10) NOT NULL,"  
		+ " ORD_DIRECTION VARCHAR(10) NOT NULL,"  
		+ " ORD_STATUS VARCHAR(10) NOT NULL,"  
		+ " ORD_FILL_STATUS VARCHAR(10) NOT NULL,"  
		+ " ORD_CANDLE_COUNT INT NOT NULL,"  
		+ " ORD_ENTRY_PRICE DECIMAL(15,5) NOT NULL,"  
		+ " ORD_STOP_LOSS DECIMAL(15,5) NOT NULL,"  
		+ " ORD_TAKE_PROFIT DECIMAL(15,5) NOT NULL,"  
		+ " ORD_FILLED_PRICE DECIMAL(15,5) NOT NULL,"  
		+ " ORD_CLOSED_PRICE DECIMAL(15,5) NOT NULL,"  
		+ " ORD_TIME_OPEN DATETIME DEFAULT NULL,"  
		+ " ORD_TIME_FILLED DATETIME DEFAULT NULL,"  
		+ " ORD_TIME_CLOSED DATETIME DEFAULT NULL,"  
		+ " ORD_PNL DECIMAL(15,2) NOT NULL,"  
		+ " ORD_CONTRACT_SIZE DECIMAL(15,2) NOT NULL,"  
		+ " ORD_REQUIRED_MARGIN DECIMAL(15,2) NOT NULL,"  
		+ " ORD_SLIPPAGE INT NOT NULL,"  
		+ " ORD_COMMISSION DECIMAL(5,2) NOT NULL,"  
		+ " RUN_ID INT NOT NULL,"
		+ " FOREIGN KEY(CAN_ID) REFERENCES candlesLog(CAN_ID));";
	private final SESettings se_settings;
	private final Connection connection;
	
	private final String output_dir;
	private final String db_name;
	private final String db_fname;
	
	/**
	 * Make a LiteCompactor
	 * @param db_fname String DB filename
	 * @throws LiteCompactorExc
	 */
	public LiteCompactor(SESettings se_settings) throws LiteCompactorExc {
		this.se_settings = se_settings;
		
        try {
        	this.output_dir = 
        			this.se_settings.getGenSettings().getOutputDirectory();
        	this.db_name = this.se_settings.getGenSettings().getOutputDB();
        	this.db_fname = output_dir + File.separator + db_name + ".db";
        	
			this.connection = DriverManager.getConnection("jdbc:sqlite:" + db_fname);
		} 
        catch (SQLException e) {
			String error_msg = "%s: Error connecting to Lite DB (Sqlite)\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new LiteCompactorExc();
		} 
        
//      Make the main tables accountsLog, candlesLog, ordersLog
        try {
        	this.createTables();
        }
        catch (LiteCompactorExc e) {
			String error_msg = "%s: Error making accountsLog, candlesLog & ordersLog\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new LiteCompactorExc();
		}
	}
	
	
	/**
	 * Execute a given Sql query
	 * @param query String sql query
	 * @throws LCompactorQueryException
	 */
	public void executeQuery(String query) throws LiteCompactorExc {
		
		try(Statement stmt = this.connection.createStatement()) {
			stmt.execute(query);
			
		} catch (SQLException e) {
			String error_msg = "%s: Error executing Sql query into Lite DB\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			System.err.printf("Query: %s\n", query);
			throw new LiteCompactorExc();
		}
	}
	
	
	/**
	 * Create the AccountsLog, OrdersLog, and CandlesLog tables
	 * @throws LCompactorQueryException
	 */
	public void createTables() throws LiteCompactorExc {
		List<String> tablenames = this.getAllTables();
		
//		Make tables if all tables do not exist
//		Check that each table does not exist, before making
//		candlesLog must be made first as it is referenced with a foreign key
//		in the other logs
		try {
			if(tablenames.size() == 0) {
				this.executeQuery(LiteCompactor.SCHEMA_CANDLESLOG);
				this.executeQuery(LiteCompactor.SCHEMA_ACCOUNTSLOG);
				this.executeQuery(LiteCompactor.SCHEMA_ORDERSLOG);
			}
			else {
				if(tablenames.indexOf(LiteCompactor.CANDLESLOG) == -1)
					this.executeQuery(LiteCompactor.SCHEMA_CANDLESLOG);
				
				if(tablenames.indexOf(LiteCompactor.ACCOUNTSLOG) == -1)
					this.executeQuery(LiteCompactor.SCHEMA_ACCOUNTSLOG);
				
				if(tablenames.indexOf(LiteCompactor.ORDERSLOG) == -1)
					this.executeQuery(LiteCompactor.SCHEMA_ORDERSLOG);
			}
			
		} catch (LiteCompactorExc e) {
			String error_msg = "%s: Error creating tables in Lite DB\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new LiteCompactorExc();
		}
	}
	
	
	/**
	 * Get all the table names in the database
	 * @return table_names List of String
	 * @throws LCompactorQueryException
	 */
	private List<String> getAllTables() throws LiteCompactorExc {
		List<String> result = new ArrayList<>();
		String query = "SELECT name FROM sqlite_master WHERE type='table';";
		
		try(Statement stmt = this.connection.createStatement()) {
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) result.add(rs.getString("name"));
		}		
		
		catch(SQLException e) {
			String error_msg = 
					"%s: Error getting all tablenames from Compactor\n";
			e.printStackTrace();
			System.err.println(e.getMessage());
			System.err.printf(error_msg, this.getClass().getName());
			throw new LiteCompactorExc();
		}
		
		return(result);
	}
	
	
	/**
	 * Helper function to map the LogRow into a HashMap for SQL insert statements
	 * @param header_fields Default headers of the type of log file
	 * @param log_row LogRow
	 * @return Map of <String, String>
	 */
	private Map<String, String> getRowMap(String[] header_fields, LogRow log_row) {
		Map<String, String> result = new HashMap<>();
		List<String> values = log_row.getValue();
		
		for(int i = 0; i < header_fields.length; i++) 
			result.put(header_fields[i], values.get(i));
		
		return(result);
	}
	
	
	/**
	 * Helper function to map the LogRows into a List of HashMap for SQL insert statements
	 * @param header_fields Default headers of the type of log file
	 * @param log_rows List of LogRows
	 * @return List of Map<String, String>
	 */
	private List<Map<String, String>> getRowMaps
								(String[] header_fields, List<LogRow> log_rows) {
		List<Map<String, String>> result = new ArrayList<>();
		for(LogRow log_row: log_rows)
			result.add(this.getRowMap(header_fields, log_row));
		
		return(result);
	}
	
	
	/**
	 * Insert 1 Candle record into Log DB
	 * @param candle_row LogRow
	 */
	public void insertCandleRecord(LogRow candle_row) {
		if(candle_row == null)	return;
		Map<String, String> row_map = this.getRowMap(Constants.HEADER_CANDLE, candle_row);
		String headers = Arrays.asList(Constants.HEADER_CANDLE)
							.stream()
							.collect(Collectors.joining(", "));
		String query = 
				String.format("INSERT INTO %s (%s) VALUES (", 
						LiteCompactor.CANDLESLOG, headers)
			  + String.format("%s, ", row_map.get("CAN_ID"))
			  + String.format("'%s', ", row_map.get("CAN_TIME"))
			  + String.format("%s, ", row_map.get("CAN_VOLUME"))
			  + String.format("%s, ", row_map.get("CAN_SPREAD"))
			  + String.format("%s, ", row_map.get("CAN_BIDO"))
			  + String.format("%s, ", row_map.get("CAN_BIDH"))
			  + String.format("%s, ", row_map.get("CAN_BIDL"))
			  + String.format("%s, ", row_map.get("CAN_BIDC"))
			  + String.format("%s, ", row_map.get("CAN_ASKO"))
			  + String.format("%s, ", row_map.get("CAN_ASKH"))
			  + String.format("%s, ", row_map.get("CAN_ASKL"))
			  + String.format("%s, ", row_map.get("CAN_ASKC"))
			  + String.format("'%s', ", row_map.get("CAN_DIRECTION"))
			  + String.format("%s, ", row_map.get("CAN_BODY"))
			  + String.format("%s, ", row_map.get("CAN_LENGTH"))
			  + String.format("%s, ", row_map.get("CAN_UNIT_DISTANCE"))
			  + String.format("%s", row_map.get("CAN_UNIT_PIP"))
			  + ");";
		
//		Insert into DB
		try {
			this.executeQuery(query);
			
		} catch (LiteCompactorExc e) {
			String error_msg = "%s: Error inserting candle record.\n";
			System.err.printf(error_msg, this.getClass().getName());
			System.err.printf("Query: %s", query);
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Insert 1 Account record into Log DB
	 * @param acc_row LogRow
	 */
	public void insertAccountRecord(LogRow acc_row) {
		if(acc_row == null)	return;
		
		Map<String, String> row_map = this.getRowMap(Constants.HEADER_ACCOUNT, acc_row);
		String headers = Arrays.asList(Constants.HEADER_ACCOUNT)
							.stream()
							.collect(Collectors.joining(", "));
		String query = 
				String.format("INSERT INTO %s (%s) VALUES (", 
						LiteCompactor.ACCOUNTSLOG, headers)
			  + String.format("%s, ", row_map.get("ACC_ID"))
			  + String.format("%s, ", row_map.get("CAN_ID"))
			  + String.format("%s, ", row_map.get("ACC_BALANCE"))
			  + String.format("%s, ", row_map.get("ACC_EQUITY"))
			  + String.format("%s, ", row_map.get("ACC_FREE_MARGIN"))
			  + String.format("%s, ", row_map.get("ACC_USED_MARGIN"))
			  + String.format("%s, ", row_map.get("ACC_MARGIN_LEVEL"))
			  + String.format("'%s', ", row_map.get("ACC_CURRENCY"))
			  + String.format("'%s', ", row_map.get("ACC_M_CALC_ENABLED"))
			  + String.format("%s, ", row_map.get("ACC_STOP_OUT_LEVEL"))
			  + String.format("%s, ", row_map.get("ACC_LEVERAGE"))
			  + String.format("%s, ", row_map.get("ACC_DISTANCE"))
			  + String.format("%s", row_map.get("RUN_ID"))
			  + ");";

		
//		Insert into DB
		try {
			this.executeQuery(query);
			
		} catch (LiteCompactorExc e) {
			String error_msg = "%s: Error inserting account record.\n";
			System.err.printf(error_msg, this.getClass().getName());
			System.err.printf("Query: %s", query);
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Insert 1 Account record into Log DB
	 * @param acc_row LogRow
	 */
	public void insertOrderRecords(List<LogRow> order_rows) {
		if(order_rows == null) return;
		
		List<Map<String, String>> row_maps = 
						this.getRowMaps(Constants.HEADER_ORDER, order_rows);
		String headers = Arrays.asList(Constants.HEADER_ORDER)
							.stream()
							.collect(Collectors.joining(", "));
		
		for(Map<String, String> row_map : row_maps) {
			String query = 
					String.format("INSERT INTO %s (%s) VALUES (", 
							LiteCompactor.ORDERSLOG, headers)
				  + String.format("%s, ", row_map.get("ORD_ID"))
				  + String.format("%s, ", row_map.get("ORD_ORDER_ID"))
				  + String.format("%s, ", row_map.get("CAN_ID"))
				  + String.format("'%s', ", row_map.get("ORD_TYPE"))
				  + String.format("'%s', ", row_map.get("ORD_DIRECTION"))
				  + String.format("'%s', ", row_map.get("ORD_STATUS"))
				  + String.format("'%s', ", row_map.get("ORD_FILL_STATUS"))
				  + String.format("%s, ", row_map.get("ORD_CANDLE_COUNT"))
				  + String.format("%s, ", row_map.get("ORD_ENTRY_PRICE"))
				  + String.format("%s, ", row_map.get("ORD_STOP_LOSS"))
				  + String.format("%s, ", row_map.get("ORD_TAKE_PROFIT"))
				  + String.format("%s, ", row_map.get("ORD_FILLED_PRICE"))
				  + String.format("%s, ", row_map.get("ORD_CLOSED_PRICE"))
				  + String.format("'%s', ", row_map.get("ORD_TIME_OPEN"))
				  + String.format("'%s', ", row_map.get("ORD_TIME_FILLED"))
				  + String.format("'%s', ", row_map.get("ORD_TIME_CLOSED"))
				  + String.format("%s, ", row_map.get("ORD_PNL"))
				  + String.format("%s, ", row_map.get("ORD_CONTRACT_SIZE"))
				  + String.format("%s, ", row_map.get("ORD_REQUIRED_MARGIN"))
				  + String.format("%s, ", row_map.get("ORD_SLIPPAGE"))
				  + String.format("%s, ", row_map.get("ORD_COMMISSION"))
				  + String.format("%s", row_map.get("RUN_ID"))
				  + ");";
			
//			Insert into DB
			try {
				this.executeQuery(query);
				
			} catch (LiteCompactorExc e) {
				String error_msg = "%s: Error inserting order record.\n";
				System.err.printf(error_msg, this.getClass().getName());
				System.err.printf("Query: %s", query);
				e.printStackTrace();
			}
		}
	}
	
}
