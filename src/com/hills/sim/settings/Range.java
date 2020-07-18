package com.hills.sim.settings;

import com.hills.sim.exceptions.SettingExc;
import com.hills.sim.settings.SVBuildable;
import com.hills.sim.settings.SVInteger;
import com.hills.sim.settings.Setting;

public class Range<T extends SVBuildable> extends Setting {
	
	private final T start, end;
	private final SVInteger step;
	
	/**
	 * Make Range Setting
	 * @param start Generic type T, start
	 * @param end Generic type T, end
	 * @param step SVInteger step
	 * @param key ID attribute of the Html TR tag
	 * @param html_class Class attribute of the Html TR tag
	 * @throws SettingExc
	 */
	public Range(T start, T end, SVInteger step, 
					String key, String html_class) 
							throws SettingExc {
		
		super(key, html_class);
		
		try {
			this.start = start;
			this.end = end;
			this.step = step;
			
		} catch (Exception e) {
			e.printStackTrace();
			System.err.printf("%s: Error making Range setting object\n", 
					this.getClass().getName());
			throw new SettingExc();
		}
	}
	
	public T getStart() {
		return(this.start);
	}
	
	public T getEnd() {
		return(this.end);
	}
	
	public SVInteger getStep() {
		return(this.step);
	}
	
	public String toString() {
		String format = "Key: %s; Html Class: %s; Start: %s; End: %s; Step: %s\n";
		String result = String.format(format, 
										this.getKey(),
										this.getHtmlClass(),
										this.start,
										this.end,
										this.step);
		return(result);
	}	
}
