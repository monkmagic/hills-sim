package com.hills.sim.settings;

import com.hills.sim.exceptions.SettingExc;
import com.hills.sim.settings.Setting;
import com.hills.sim.settings.SettingValue;

public class Primitive<T extends SettingValue> extends Setting {
	
	private final T value;
	
	/**
	 * Make Primitive Setting
	 * @param value Generic type T, basic setting type
	 * @param key ID attribute of the Html TR tag
	 * @param html_class Class attribute of the Html TR tag
	 * @throws SettingExc
	 */
	public Primitive(T value, String key, String html_class)
									throws SettingExc {
		
		super(key, html_class);
		
		try {
			this.value = value;
			
		} catch (Exception e) {
			e.printStackTrace();
			System.err.printf("%s: Error making Primitive setting object\n", 
					this.getClass().getName());
			throw new SettingExc();
		}
	}
	
	public T getValue() {
		return(this.value);
	}
	
	public String toString() {
		String format = "Key: %s; Html Class: %s; Value: %s\n";
		String result = String.format(format, 
										this.getKey(),
										this.getHtmlClass(),
										this.value);
		return(result);
	}	
}
