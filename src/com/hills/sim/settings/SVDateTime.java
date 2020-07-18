package com.hills.sim.settings;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.hills.sim.exceptions.SettingExc;
import com.hills.sim.settings.SVBuildable;

public class SVDateTime extends SVBuildable 
						implements Comparable<SVDateTime> {
	
	private LocalDateTime value;
	
	/**
	 * Make SVDateTime
	 * @param value String value of the LocalDateTime
	 * @throws SettingExc
	 */
	public SVDateTime(String value) throws SettingExc {
		try {
			value = value.replace(' ', 'T');
			this.value = LocalDateTime.parse(value);
			
		} catch (Exception e) {
			e.printStackTrace();
			System.err.printf("String arg: %s\n", value);
			System.err.printf("%s: Error parsing String into LocalDateTime\n", 
								this.getClass().getName());
			throw new SettingExc();
		}
	}
	
	public LocalDateTime getValue() {
		return(this.value);
	}
	
	public String toString() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String result = this.value.format(formatter);
		
		return(result);
	}

	/**
	 * Compare to a target SVDateTime
	 * @param target SVDateTime compared to
	 */
	@Override
	public int compareTo(SVDateTime target) {
		return(this.value.compareTo(target.getValue()));
	}
}
