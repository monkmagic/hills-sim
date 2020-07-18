package com.hills.sim.ea.prototype;

public class EAConstants {
	
	/**
	 * Accessed via Reflection API
	 * Do not remove constant definition
	 * Defines the maximum capacity of the TimeSeries within
	 * the History class within Stage
	 */
	public static final int HISTORY_LIMIT = 100;
	
	public static final String[][] RAW_FRAME = 
			new String[][] {
				{"EA_FAST_SMA","RANGE-INTEGER"},
				{"EA_SLOW_SMA","RANGE-INTEGER"},
				{"EA_WINDOW", "RANGE-INTEGER"}
			};
			
	/**
	 * Contains the definition of the header, for the Run Log
	 */
	public static final String[] HEADER_RUN = 
			new String[] {
					"RUN_ID",
					"RUN_TOTAL",
					"RUN_FAST_PERIOD",
					"RUN_SLOW_PERIOD",
					"RUN_WINDOW"
			};

	/**
	 * Contains the definition of the header, for the Indicators Log
	 */
	public static final String[] HEADER_INDIS =
			new String[] {
					"TA_ID",
					"TA_FSMA_PERIOD",
					"TA_SSMA_PERIOD",
					"TA_FSMA_BIDO",
					"TA_FSMA_BIDH",
					"TA_FSMA_BIDL",
					"TA_FSMA_BIDC",
					"TA_SSMA_BIDO",
					"TA_SSMA_BIDH",
					"TA_SSMA_BIDL",
					"TA_SSMA_BIDC",
					"RUN_ID"
			};
	
	public enum XoverDirectionE {
			DOJI,
			BULL,
			BEAR
	}
}
