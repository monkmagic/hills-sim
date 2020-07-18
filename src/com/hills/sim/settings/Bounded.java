package com.hills.sim.settings;

import com.hills.sim.exceptions.SettingExc;
import com.hills.sim.settings.SVBuildable;
import com.hills.sim.settings.Setting;

public class Bounded<T extends SVBuildable> extends Setting {
	
	private final T lower, upper;
	
	/**
	 * Make Bounded Setting
	 * @param lower Generic type T, lower bound
	 * @param upper Generic type T, upper bound
	 * @param key ID attribute of the Html TR tag
	 * @param html_class Class attribute of the Html TR tag
	 * @throws SettingExc
	 */
	public Bounded(T lower, T upper, String key, String html_class) 
									throws SettingExc {
		super(key, html_class);
		
		try {
			this.lower = lower;
			this.upper = upper;
			
		} catch (Exception e) {
			e.printStackTrace();
			System.err.printf("%s: Error making Bounded setting object\n", 
					this.getClass().getName());
			throw new SettingExc();			
		}
	}
	
	public T getLower() {
		return(this.lower);
	}
	
	public T getUpper() {
		return(this.upper);
	}
	
	public String toString() {
		String format = "Key: %s; Html Class: %s; Lower: %s; Upper: %s\n";
		String result = String.format(format, 
										this.getKey(),
										this.getHtmlClass(),
										this.lower,
										this.upper);
		return(result);
	}
}
