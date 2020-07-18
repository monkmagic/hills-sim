package com.hills.sim.ea.teststrategy;

public class EAConstants {
	
	
	public static final String[][] RAW_FRAME = 
			new String[][] {{"EA_TEST_ID","RANGE-INTEGER"}};
	
	
	/**
	 * Do not remove constant definition
	 * Defines the maximum capacity of the TimeSeries within
	 * the History class within Stage
	 */
	public static final int HISTORY_LIMIT = 100;
	
	
	/**
	 * Contains the definition of the header, for the Run Log
	 */
	public static final String[] HEADER_RUN = 
			new String[] {
					"RUN_ID",
					"RUN_TOTAL",
					"RUN_TEST_ID",
			};
}
