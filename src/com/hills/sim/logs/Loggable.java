package com.hills.sim.logs;

import java.util.List;

public interface Loggable {
	
	/**
	 * For logging of the header, for classes with varying headers
	 * The classes with varying headers: Run, Indicators & Strategy
	 * @return log_headers Log the headers
	 */
	default public LogRow toLogHeader() {
		throw new UnsupportedOperationException();
	}
	
	
	/**
	 * For logging of the individual record 
	 * @return log_row Log the individual record
	 */
	default public LogRow toLogRow() {
		throw new UnsupportedOperationException();
	}
	
	
	/**
	 * For logging, where multiple rows define an object
	 * @return log_rows list of log rows
	 */
	default public List<LogRow> toLogRows() {
		throw new UnsupportedOperationException();
	}
	
	
	/**
	 * For logging, where different types of log rows are bundled
	 * @return log_rows_bag a bag of different types of log rows
	 */
	default public LogRowsBag toLogRowsBag() {
		throw new UnsupportedOperationException();
	}
	
	
	/**
	 * For Run-settings only
	 * Get the log-equivalent of the Run-settings.
	 * @return log_records includes log headers and rows
	 */
	default public List<LogRow> toLog() {
		throw new UnsupportedOperationException();
	}
	
	
	
}
