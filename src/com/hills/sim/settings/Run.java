package com.hills.sim.settings;

import com.hills.sim.logs.Loggable;

public interface Run extends Loggable {
	
	/**
	 * Get Run ID
	 * @return run_id
	 */
	public int getId();

	
	/**
	 * Get Total number of Runs
	 * @return total_runs
	 */
	public int getTotalRuns();
	
	
	/**
	 * For printing 
	 */
	public String toString();
	
	

}
