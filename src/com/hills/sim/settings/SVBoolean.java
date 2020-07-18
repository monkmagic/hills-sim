package com.hills.sim.settings;

import com.hills.sim.exceptions.SettingExc;
import com.hills.sim.settings.SVUnbuildable;

public class SVBoolean extends SVUnbuildable {
	private boolean value;
	
	/**
	 * Make SVBoolean
	 * @param value String value of Boolean
	 * @throws SettingExc
	 */
	public SVBoolean(String value) throws SettingExc {
		try {
			if(!(value.equalsIgnoreCase("true") 
				 || value.equalsIgnoreCase("false")))
				throw new Exception();
				
			this.value = Boolean.parseBoolean(value);
			
		} catch (Exception e) {
			e.printStackTrace();
			System.err.printf("String arg: %s\n", value);
			System.err.printf("%s: Error parsing String into boolean\n", 
					this.getClass().getName());
			throw new SettingExc();
		}
	}
	
	public boolean getValue() {
		return(this.value);
	}
	
	public String toString() {
		Boolean bool = new Boolean(value);
		return(bool.toString());
	}

}
