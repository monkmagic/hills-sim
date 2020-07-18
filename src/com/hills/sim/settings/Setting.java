package com.hills.sim.settings;

import com.hills.sim.exceptions.SettingExc;

public class Setting {
	private final String key;
	private final String html_class;
	
	/**
	 * Make the Setting object
	 * @param key ID attribute of the Html TR tag
	 * @param html_class Class attribute of the Html TR tag
	 * @throws SettingExc
	 */
	public Setting(String key, String html_class) 
						throws SettingExc {
		
		try {
			this.key = key;
			this.html_class = html_class;
			
		} catch (Exception e) {
			e.printStackTrace();
			System.err.printf("%s: Error saving key and html_class\n", 
					this.getClass().getName());
			throw new SettingExc();
		}
		
	}
	
	public String getKey() {
		return(this.key);
	}
	
	public String getHtmlClass() {
		return(this.html_class);
	}

}
