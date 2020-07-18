package com.hills.sim.settings;

import com.hills.sim.exceptions.SettingExc;
import com.hills.sim.settings.SVUnbuildable;

public class SVString extends SVUnbuildable {
	private String value;
	
	/**
	 * Make SVString
	 * @param value String value
	 * @throws SettingExc
	 */
	public SVString(String value) throws SettingExc {
		
		try {
			this.value = value;
			
		} catch (Exception e) {
			e.printStackTrace();
			System.err.printf("String arg: %s\n", value);
			System.err.printf("%s: Error saving String\n", 
					this.getClass().getName());
			throw new SettingExc();
		}
	}
	
	public String getValue() {
		return(this.value);
	}

	public String toString() {
		return(this.value);
	}
}
