package com.hills.sim.ea.teststrategy;

import com.hills.sim.logs.LogRow;
import com.hills.sim.settings.Run;

public class EARun implements Run {
	
	private final int id;
	private final int total_runs;
	private final int test_id;
	
	private final String[] log_header;
	
	public EARun(int id, int total_runs, int test_id) {
		this.id = id;
		this.total_runs = total_runs;
		this.test_id = test_id;
		this.log_header = EAConstants.HEADER_RUN;
	}
	
	public int getTestId() {
		return test_id;
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
		return "EARun [id=" + id + ", total_runs=" + total_runs + 
				", test_id=" + test_id + "]\n";
	}

	
	@Override
	public LogRow toLogHeader() {
		return(new LogRow(this.log_header));
	}
	
	
	@Override
	public LogRow toLogRow() {
		String[] value = new String[] {Integer.toString(id), 
										 Integer.toString(total_runs), 
										 Integer.toString(test_id)};
		
		return(new LogRow(value));
	}

}
