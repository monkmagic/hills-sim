package com.hills.sim.ea.prototype;

import com.hills.sim.logs.LogRow;
import com.hills.sim.settings.Run;

public class EARun implements Run {
	
	private final int id;
	private final int total_runs;
	private final int fast_period;
	private final int slow_period;
	private final int window;
	
	private final String[] log_header;
	
	public EARun(int id, int total_runs, int fast, int slow, int window) {
		this.id = id;
		this.total_runs = total_runs;
		this.fast_period = fast;
		this.slow_period = slow;
		this.window = window;
		this.log_header = EAConstants.HEADER_RUN;
	}
	
	public int getFastPeriod() {
		return fast_period;
	}
	
	public int getSlowPeriod() {
		return slow_period;
	}

	public int getWindow() {
		return window;
	}
	
	@Override
	public int getId() {
		return id;
	}

	@Override
	public int getTotalRuns() {
		return total_runs;
	}

	
	@Override
	public String toString() {
		return "EARun [id=" + id 
				+ ", total_runs=" + total_runs 
				+ ", fast_period=" + fast_period 
				+ ", slow_period=" + slow_period 
				+ ", window=" + window + "]\n";
	}

	@Override
	public LogRow toLogHeader() {
		return(new LogRow(this.log_header));
	}
	
	
	@Override
	public LogRow toLogRow() {
		
		String[] value = new String[] {
				Integer.toString(id), 
				Integer.toString(total_runs), 
				Integer.toString(fast_period),
				Integer.toString(slow_period),
				Integer.toString(window)
		};
		
		return(new LogRow(value));
	}

}
