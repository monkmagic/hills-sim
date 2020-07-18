package com.hills.sim.ea.prototype;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.hills.sim.ea.prototype.EAConstants;
import com.hills.sim.ea.prototype.EARun;
import com.hills.sim.exceptions.BigSettingExc;
import com.hills.sim.exceptions.SettingExc;
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
	private final Range<SVInteger> fast;
	private final Range<SVInteger> slow;
	private final Range<SVInteger> window;
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
			this.fast = (Range<SVInteger>) 
							 this.bsf.getSetting("EA_FAST_SMA");
			this.slow = (Range<SVInteger>) 
					 this.bsf.getSetting("EA_SLOW_SMA");
			this.window = (Range<SVInteger>) 
					 this.bsf.getSetting("EA_WINDOW");
			
		} catch (Exception e) {
			String error_msg = "%s: Error getting values for EASettings\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new BigSettingExc();
		}
		
//		Make List<EARun>
		try {
			this.runs = this.makeRuns();
			
		} catch (SettingExc e) {
			String error_msg = "%s: Error making runs for EASettings\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new BigSettingExc();
		}
	}
	
	/**
	 * EA-invariant; method is required for all EASettings
	 */
	public List<EARun> makeRuns() throws SettingExc {
		List<EARun> result = new ArrayList<>();
		
		int run_id;
		int fast_start, fast_end, fast_step;
		int slow_start, slow_end, slow_step;
		int win_start, win_end, win_step;
		
		try {
			fast_start = this.fast.getStart().getValue();
			fast_end   = this.fast.getEnd().getValue();
			fast_step  = this.fast.getStep().getValue();
			
			slow_start = this.slow.getStart().getValue();
			slow_end   = this.slow.getEnd().getValue();
			slow_step  = this.slow.getStep().getValue();
			
			win_start = this.window.getStart().getValue();
			win_end = this.window.getEnd().getValue();
			win_step = this.window.getStep().getValue();
			
		} catch (Exception e) {
			String error_msg = "%s: Error saving fast/slow period values.\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new SettingExc();
		}
		
//		Calculate the number of total runs
		this.total_runs = 0;
		for(int i = fast_start; i <= fast_end; i += fast_step) 
			for(int j = slow_start; j <= slow_end; j += slow_step)
				for(int k = win_start; k <= win_end; k += win_step)
					this.total_runs++;
		
		run_id = 0;
		for(int i = fast_start; i <= fast_end; i += fast_step) 
			for(int j = slow_start; j <= slow_end; j += slow_step) 
				for(int k = win_start; k <= win_end; k += win_step) {
					run_id++;
					result.add(new EARun(run_id, this.total_runs, i, j, k));
				}
			
		
		return(result);		
		
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
	public List<LogRow> toLog() {
		LogRow header = this.runs.get(0).toLogHeader();
		List<LogRow> result = this.runs.stream()
										.map(Run::toLogRow)
										.collect(Collectors.toList());
		result.add(0, header);
		
		return(result);
	}
	
	
	
}
