package com.hills.sim;

public class Constants {
	
	/**
	 * File name for the Settings html file
	 */
	public static final String CMD_ARG_SETTINGS = "--settings";
	
	
	public static final String EA_PATH = "com.hills.sim.ea.";

	public static final String getPackageName(String strategy) {
		if(strategy.equalsIgnoreCase("")) return("");
		return(Constants.EA_PATH + strategy.toLowerCase());
	}
	
	/**
	 * Enumerations for the Factory method, to create the Setting types/values
	 * @author Mark L
	 *
	 */
	public enum SettingE {
		PRIMITIVE, 
		BOUNDED, 
		RANGE
	};

	
	/**
	 * Enumerations: Boolean, Date, DateTime, Float, Integer, String
	 * @author Mark L
	 *
	 */
	public enum SettingValueE {
		BOOLEAN, 
		DATE, 
		DATETIME, 
		FLOAT, 
		INTEGER, 
		STRING, 
		TIME
	};
	
	
	/**
	 * BigSettingFrame prefixes
	 */
	public static final String BIGSETTINGF_PREFIX_GEN = "GEN";
	public static final String BIGSETTINGF_PREFIX_ACC = "ACC";
	public static final String BIGSETTINGF_PREFIX_SYM = "SYM";
	public static final String BIGSETTINGF_PREFIX_EA = "EA";	
	
	/**
	 * BigSettingFrame definitions for the following BigSetting objects:
	 * General, Account, Symbol
	 * Each BigSettingFrame definition contains a 'Framelet' where it is
	 * defined as "Key": "SettingType-SettingValue".
	 */
	public static final String[][] BIGSETTINGF_GEN = 
			new String[][] {
				{"GEN_STRATEGY_NAME", "PRIMITIVE-STRING"},
				{"GEN_OUTPUT_DIRECTORY", "PRIMITIVE-STRING"},
				{"GEN_PERIOD_RANGE", "BOUNDED-DATETIME"},
			};
			
	public static final String[][] BIGSETTINGF_ACC = 
			new String[][] {
				{"ACC_BALANCE", "PRIMITIVE-FLOAT"},
				{"ACC_CURRENCY", "PRIMITIVE-STRING"},
				{"ACC_MARGIN_CALC", "PRIMITIVE-BOOLEAN"},
				{"ACC_STOP_OUT_LEVEL", "PRIMITIVE-INTEGER"},
			};
			
	public static final String[][] BIGSETTINGF_SYM = 
			new String[][] {
				{"SYM_SOURCE", "PRIMITIVE-STRING"},
				{"SYM_NAME", "PRIMITIVE-STRING"},
				{"SYM_TIMEFRAME", "PRIMITIVE-STRING"}
			};	
			
			
			
	
	/**
	 * Enumeration for the different log types. 
	 * Includes the header prefixes and the log name
	 */
	public enum LogTypeE {
		CANDLES("CAN", "CandlesLog.csv", HEADER_CANDLE),
		ACCOUNT("ACC", "AccountsLog.csv", HEADER_ACCOUNT),
		ORDERS("ORD", "OrdersLog.csv", HEADER_ORDER),
		REPORTS("REP", "ReportsLog.csv", HEADER_REPORT),
		INDICATORS("IND", "IndicatorsLog.csv"),
		STRATEGY("STR", "StrategyLog.csv"),
		RUNS("RUN", "RunsLog.csv");
		
		private final String prefix;
		private final String log_name;
		private final String[] headers;
		
		/**
		 * Get the prefix of the log type
		 * @return the prefix
		 */

		public String getPrefix() {
			return prefix;
		}

		/**
		 * Get the log name of the log type (i.e. log_file.csv)
		 * @return the log_name
		 */
		public String getLogName() {
			return log_name;
		}

		/**
		 * Get the headers for the different type of log file
		 * @return the headers
		 */
		public String[] getHeaders() {
			return headers;
		}
		

		LogTypeE(String prefix, String log_name) {
			this.prefix = prefix;
			this.log_name = log_name;
			this.headers = null;
		}
		
		LogTypeE(String prefix, String log_name, String[] headers) {
			this.prefix = prefix;
			this.log_name = log_name;
			this.headers = headers;
		}
		
	};
	
	
	public static final String[] HEADER_ACCOUNT = 
			new String[] {
					"ACC_ID",
					"CAN_ID",
					"ACC_BALANCE",
					"ACC_EQUITY",
					"ACC_FREE_MARGIN",
					"ACC_USED_MARGIN",
					"ACC_MARGIN_LEVEL",
					"ACC_MIN_BALANCE",
					"ACC_MAX_BALANCE",
					"ACC_DRAWDOWN",
					"ACC_CURRENCY",
					"ACC_M_CALC_ENABLED",
					"ACC_STOP_OUT_LEVEL",
					"ACC_LEVERAGE",
					"ACC_DISTANCE",
					"RUN_ID"
			};
	
	
	public static final String[] HEADER_CANDLE = 
			new String[] {
					"CAN_ID",
					"CAN_TIME",
					"CAN_VOLUME",
					"CAN_SPREAD",
					"CAN_BIDO",
					"CAN_BIDH",
					"CAN_BIDL",
					"CAN_BIDC",
					"CAN_ASKO",
					"CAN_ASKH",
					"CAN_ASKL",
					"CAN_ASKC",
					"CAN_DIRECTION",
					"CAN_BODY",
					"CAN_LENGTH",
					"CAN_UNIT_DISTANCE",
					"CAN_UNIT_PIP"
			};
	
	
	public static final String[] HEADER_ORDER = 
			new String[] {
					"ORD_ID",
					"ORD_ORDER_ID",
					"CAN_ID",
					"ORD_TYPE",
					"ORD_DIRECTION",
					"ORD_STATUS",
					"ORD_FILL_STATUS",
					"ORD_CANDLE_COUNT",
					"ORD_ENTRY_PRICE",
					"ORD_STOP_LOSS",
					"ORD_TAKE_PROFIT",
					"ORD_FILLED_PRICE",
					"ORD_CLOSED_PRICE",
					"ORD_TIME_OPEN",
					"ORD_TIME_FILLED",
					"ORD_TIME_CLOSED",
					"ORD_PNL",
					"ORD_CONTRACT_SIZE",
					"ORD_REQUIRED_MARGIN",
					"ORD_SLIPPAGE",
					"ORD_COMMISSION",
					"RUN_ID"
			};
	
	
	public static final String[] HEADER_REPORT =
			new String[] {
					"REP_ID",
					"REP_TOTAL_NET_PROFIT",
					"REP_GROSS_PROFIT",
					"REP_GROSS_LOSS",
					"REP_PROFIT_FACTOR",
					"REP_TOTAL_TRADES",
					"REP_PERC_PROFITABLE",
					"REP_WIN_TRADES",
					"REP_LOSE_TRADES",
					"REP_AVG_NET_PROFIT",
					"REP_AVG_WIN",
					"REP_AVG_LOSS",
					"REP_AVG_WIN_LOSS_RATIO",
					"REP_LARGEST_WIN",
					"REP_LARGEST_LOSS",
					"REP_MAX_WIN_STREAK",
					"REP_MAX_LOSE_STREAK",
					"REP_AVG_BARS_ALL",
					"REP_AVG_BARS_WIN",
					"REP_AVG_BARS_LOSS",
					"REP_MAX_DRAWDOWN",
					"RUN_ID"
			};

	
	/**
	 * Used in Account; Leverage limit is 1:1.
	 */
	public static int ACC_LEVERAGE_LIMIT = 1;
	
	
	/**
	 * Used in Account; Stop out level limit is 100%
	 */
	public static float ACC_STOP_OUT_LIMIT = 1.0f;
	
	
	/**
	 * Standard number of units in a lot
	 */
	public static int ACC_STD_LOT = 100_000;
	
	
	/**
	 * Direction of the candle
	 * Bull, Bear or Doji
	 * Order of the Doji(0), Bear(1), and Bull(2)
	 * @author Mark L
	 *
	 */
	public enum CandleDirectionE {
		DOJI,
		BEAR,
		BULL;
	};
	
	
	/**
	 * Direction of the Order
	 * Buy, Sell (default: unset)
	 * @author Mark L
	 *
	 */
	public enum OrderDirectionE {
		UNSET,
		BUY,
		SELL,
	};
	
	
	/**
	 * Types of Order
	 * Market, Stop, Limit (default: unset)
	 * @author Mark L
	 *
	 */
	public enum OrderTypeE {
		UNSET,
		MARKET,
		STOP,
		LIMIT,
	};
	
	
	/**
	 * Status of the Order
	 * Open, Close
	 * @author Mark L
	 *
	 */
	public enum OrderStatusE {
		OPEN,
		CLOSE,
	};
	
	
	/**
	 * Fill or Kill status of the Order
	 * Filled, Unfilled (default: Unfilled)
	 */
	public enum OrderFillE {
		UNFILLED,
		FILLED,
	};	
	
	
	public static final String[] SYM_SOURCES = {"OA"};
	
	public static final String DB_DRIVER = "org.postgresql.Driver";
	public static final String DB_PWD = "OracleDB123";	
	public static final String DB_USR = "HillsUser";
	public static final String DB_URL = 
					"jdbc:postgresql://localhost/HillsCompactor";
	
	public static final String[] DB_TABLE_FIELDS =
			   new String[] {"ID", "ContentID", "Time", "Vol",
			   	 			 "AskO", "AskH", "AskL", "AskC",
			   	 			 "BidO", "BidH", "BidL", "BidC",
			   	 			 "MidO", "MidH", "MidL", "MidC"};
	
	public static final String DB_TABLE_FMT = "%s_%s__%s";
	
	public static final int Q_LIMIT_CCANDLES = 1000;
	public static final int Q_LIMIT_LOGROWSBAG = 1000;
	

	public static final int TA_MAX_ELEM = 1000;
}
