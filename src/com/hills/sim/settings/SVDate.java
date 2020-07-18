package com.hills.sim.settings;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.hills.sim.exceptions.SettingExc;
import com.hills.sim.settings.SVBuildable;

public class SVDate extends SVBuildable 
					implements Comparable<SVDate> {
	
	private LocalDate value;
	
	/**
	 * Make SVDate
	 * @param value String value of LocalDate
	 * @throws SettingExc
	 */
	public SVDate(String value) throws SettingExc {
		try {
			this.value = LocalDate.parse(value);
			
		} catch (Exception e) {
			e.printStackTrace();
			System.err.printf("String arg: %s\n", value);
			System.err.printf("%s: Error parsing String into LocalDate\n", 
								this.getClass().getName());
			throw new SettingExc();
		}
	}
	
	public LocalDate getValue() {
		return(this.value);
	}
	
	public String toString() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String result = this.value.format(formatter);
		
		return(result);
	}
	
	/**
	 * Compare to a target SVDate
	 * @param target SVDate compared to
	 */
	@Override
	public int compareTo(SVDate target) {
		return(this.value.compareTo(target.getValue()));
	}

}
