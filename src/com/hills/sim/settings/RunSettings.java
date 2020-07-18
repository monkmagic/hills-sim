package com.hills.sim.settings;

import java.util.List;

import com.hills.sim.logs.Loggable;


public interface RunSettings extends Loggable {
	
	/**
	 * All EASettings must return List<Run>
	 * @return list_runs
	 */
	public List<Run> getRuns();

}
