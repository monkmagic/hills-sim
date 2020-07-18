package com.hills.sim.logs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * LogRow is the row of a log file, to be recorded in CSV format.
 * @author Mark L
 *
 */
public class LogRow {

	protected List<String> value;
	
	
	/**
	 * For headers. Constructor that takes in an Array of Strings. 
	 * @param value array of String
	 */
	public LogRow(String[] value) {
		
//		A List object, created out an Array, does not support append/in-place
//		modification of the List
		if(value == null) this.value = new ArrayList<>();
		else this.value = new ArrayList<>(Arrays.asList(value));
	}	
	
	/**
	 * Get the value of the log row, which is a list of String
	 * @return value List of String
	 */
	public List<String> getValue() {
		return(this.value);
	}
	
	
	/**
	 * Performs in-place prepend of the argument col, a single String.
	 * @param col String value
	 */
	public void prepend(String col) {
		if(col == null) return;
		if(col.equalsIgnoreCase("")) return;
		this.value.add(0, col);
	}

	
	/**
	 * Performs in-place append of the argument col, a single String
	 * @param col String value
	 */
	public void append(String col) {
		if(col == null) return;
		if(col.equalsIgnoreCase("")) return;		
		this.value.add(col);
	}
	
	


	@Override
	public String toString() {
		return "LogRow [value=" + value + "]";
	}
}
