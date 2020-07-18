package com.hills.sim.settings;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import com.hills.sim.exceptions.SettingExc;
import com.hills.sim.settings.SVBuildable;

public class SVTime extends SVBuildable 
						implements Comparable<SVTime> {
	
	private LocalTime value;
	
	/**
	 * Make SVTime
	 * @param value String value of LocalTime
	 * @throws SettingExc
	 */
	public SVTime(String value) throws SettingExc {
		try {
			this.value = LocalTime.parse(value);
			
		} catch (Exception e) {
			e.printStackTrace();
			System.err.printf("String arg: %s\n", value);
			System.err.printf("%s: Error parsing String into LocalTime\n", 
						this.getClass().getName());
			throw new SettingExc();
		}
	}
	
	public LocalTime getValue() {
		return(this.value);
	}

	public String toString() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("kk:mm");
		String result = this.value.format(formatter);
		
		return(result);		
	}
	
	/**
	 * Compare to SVTime
	 * @param target SVTime compared to
	 */
	@Override
	public int compareTo(SVTime target) {
		return(this.value.compareTo(target.getValue()));
	}
}
