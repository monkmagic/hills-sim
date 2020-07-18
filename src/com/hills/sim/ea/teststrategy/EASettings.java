package com.hills.sim.ea.teststrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.hills.sim.ea.teststrategy.EAConstants;
import com.hills.sim.ea.teststrategy.EARun;
import com.hills.sim.exceptions.BigSettingExc;
import com.hills.sim.logs.LogRow;
import com.hills.sim.Constants;
import com.hills.sim.settings.BigSetting;
import com.hills.sim.settings.Range;
import com.hills.sim.settings.Run;
import com.hills.sim.settings.RunSettings;
import com.hills.sim.settings.SVInteger;
import com.hills.sim.settings.Setting;

public class EASettings extends BigSetting
						implements RunSettings {
	
	/**
	 * EA-specific settings; differs from 1 EA to another EA
	 */
	private final Range<SVInteger> test_id;
	private final List<EARun> runs;
	private int total_runs;
	
	@SuppressWarnings("unchecked")
	public EASettings (List<Setting> html_settings) 
								throws BigSettingExc {
		
		super(Constants.BIGSETTINGF_PREFIX_EA, 
			  EAConstants.RAW_FRAME, 
			  html_settings);
		
//		Expand Range settings
		try {
			this.test_id = (Range<SVInteger>) 
							 this.bsf.getSetting("EA_TEST_ID");
			
		} catch (Exception e) {
			String error_msg = "%s: Error getting values for EASettings\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new BigSettingExc();
		}
		
//		Make List<EARun>
		try {
			this.runs = this.makeRuns();
			
		} catch (EASettingsExc e) {
			String error_msg = "%s: Error making runs for EASettings\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new BigSettingExc();
		}
	}
	
	/**
	 * EA-invariant; method is required for all EASettings
	 */
	public List<EARun> makeRuns() throws EASettingsExc {
		List<EARun> result = new ArrayList<>();
		
		int test_start, test_end, test_step;
		int run_id;
		try {
			test_start = this.test_id.getStart().getValue();
			test_end   = this.test_id.getEnd().getValue();
			test_step  = this.test_id.getStep().getValue();
			
			
		} catch (Exception e) {
			String error_msg = "%s: Error saving test id values\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new EASettingsExc();
		}
		
//		Calculate the number of total runs
		this.total_runs = 0;
		for(int i = test_start; i <= test_end; i += test_step) 
			this.total_runs++;
		
		run_id = 0;
		for(int i = test_start; i <= test_end; i += test_step) {
			run_id++;
			result.add(new EARun(run_id, this.total_runs, i));
		}
		
		return(result);		
		
	}
	
	

	
	public Range<SVInteger> getTestId() {
		return test_id;
	}


	/**
	 * EA-specific;
	 */
	@Override
	public List<Run> getRuns() {
		List<Run> result = this.runs.stream()
									.map(x -> (Run) x)
									.collect(Collectors.toList());
		return(result);
	}
	
	
	@Override
	public String toString() {
		return "EASettings [test_id=" + test_id + "]\n";
	}

	
	@Override
	public List<LogRow> toLog() {
		LogRow header = this.runs.get(0).toLogHeader();
		List<LogRow> result = this.runs.stream()
										.map(Run::toLogRow)
										.collect(Collectors.toList());
		result.add(0, header);
		
		return(result);
	}
	
	
	
}
