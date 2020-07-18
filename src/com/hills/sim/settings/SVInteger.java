package com.hills.sim.settings;

import com.hills.sim.exceptions.SettingExc;
import com.hills.sim.settings.SVBuildable;

public class SVInteger extends SVBuildable 
						implements Comparable<SVInteger> {
	private int value;
	
	/**
	 * Make SVInteger
	 * @param value String value of integer number
	 * @throws SettingExc
	 */
	public SVInteger(String value) throws SettingExc {
		try {
			this.value = Integer.parseInt(value);
			
		} catch (Exception e) {
			e.printStackTrace();
			System.err.printf("String arg: %s\n", value);
			System.err.printf("%s: Error parsing String into int\n", 
									this.getClass().getName());
			throw new SettingExc();
		}
	}
	
	public int getValue() {
		return(this.value);
	}

	public String toString() {
		Integer i = new Integer(this.value);
		return(i.toString());
	}
	
	/**
	 * Compare to target SVInteger
	 * @param target SVInteger compared to
	 */
	@Override
	public int compareTo(SVInteger target) {
		Integer src = new Integer(this.value);
		Integer trg = new Integer(target.getValue());
		
		return(src.compareTo(trg));
	}
}
