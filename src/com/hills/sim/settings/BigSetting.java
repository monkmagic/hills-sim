package com.hills.sim.settings;

import java.util.List;

import com.hills.sim.exceptions.BigSettingFrameExc;
import com.hills.sim.exceptions.BigSettingExc;
import com.hills.sim.settings.BigSettingFrame;


public class BigSetting {
	
	protected final BigSettingFrame bsf;
	
	
	/**
	 * Make BigSettings
	 * @param prefix Prefix for the ID attribute of Html TR tag
	 * @param rframe Raw frame that contains the definitions
	 * @param html_settings Settings that are parsed from Html 
	 * @throws BigSettingExc
	 */
	public BigSetting
			(String prefix, String[][] frame, List<Setting> html_settings) 
			throws BigSettingExc {
		
		try {
			this.bsf = new BigSettingFrame(prefix, frame, html_settings);
			
		} catch (BigSettingFrameExc e) {
			String error_msg = "%s: Error constructing BigSetting\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new BigSettingExc();
		}
	}

	public String getPrefix() {
		return(this.bsf.getPrefix());
	}
	
	public String[][] getRawFrame() {
		return(this.bsf.getRawFrame());
	}
}
