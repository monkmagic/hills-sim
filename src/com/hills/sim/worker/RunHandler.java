package com.hills.sim.worker;

import com.hills.sim.settings.Run;

/**
 * To reset the object for every run
 * @author Mark L
 *
 */
public interface RunHandler {

	/**
	 * To update with Run values
	 */
	default public void setRun(Run run) throws Exception {
		throw new UnsupportedOperationException();
	}
	
	
	/**
	 * To reset, preparing for the next run
	 */
	default public void reset() throws Exception {
		throw new UnsupportedOperationException();		
	}
}
