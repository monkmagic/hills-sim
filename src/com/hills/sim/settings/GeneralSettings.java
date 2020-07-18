package com.hills.sim.settings;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import com.hills.sim.exceptions.BigSettingFrameExc;
import com.hills.sim.exceptions.BigSettingExc;

public class GeneralSettings extends BigSetting {

	private final String output_directory;
	private final String strategy_name;
	private final LocalDateTime[] period_range;
	
	/**
	 * Make GeneralSettings
	 * @param prefix Prefix for the ID attribute of Html TR tag
	 * @param rframe Raw frame that contains the definitions
	 * @param html_settings Settings that are parsed from Html 
	 * @throws BigSettingExc
	 */
	public GeneralSettings 
	(String prefix, String[][] rframe, List<Setting> html_settings) 
									throws BigSettingExc {
	
		super(prefix, rframe, html_settings);
		
		try {
			this.output_directory = this.bsf.getStringValue
														("GEN_OUTPUT_DIRECTORY");
			this.strategy_name = this.bsf.getStringValue("GEN_STRATEGY_NAME");
			this.period_range = this.bsf.getDateTimeBounds("GEN_PERIOD_RANGE");
			
		} catch (BigSettingFrameExc e) {
			String error_msg = "%s: Error getting values for GeneralSettings\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new BigSettingExc();
		}
	}
	
	/**
	 * Getters
	 * @return
	 */


	public String getOutputDirectory() {
		return output_directory;
	}
	
	public String getStrategyName() {
		return strategy_name;
	}
	
	public LocalDateTime[] getPeriodRange() {
		return period_range;
	}
	
	@Override
	public String toString() {
		return "GeneralSettings "
				+ "\n[ strategy_name=" + strategy_name 
				+ "\n, output_directory=" + output_directory 
				+ "\n, period_range=" + Arrays.toString(period_range) + "]\n";
	}
	
	
}
