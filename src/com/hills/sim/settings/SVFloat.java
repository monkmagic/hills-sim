package com.hills.sim.settings;

import com.hills.sim.exceptions.SettingExc;
import com.hills.sim.settings.SVBuildable;

public class SVFloat extends SVBuildable 
						implements Comparable<SVFloat> {
	private float value;
	
	/**
	 * Make SVFloat
	 * @param value String value of float number
	 * @throws SettingExc
	 */
	public SVFloat(String value) throws SettingExc {
		try {
			this.value = Float.parseFloat(value+"F");
			
		} catch (Exception e) {
			e.printStackTrace();
			System.err.printf("String arg: %s\n", value);
			System.err.printf("%s: Error parsing String into float\n", 
					this.getClass().getName());
			throw new SettingExc();
		}
	}
	
	public float getValue() {
		return(this.value);
	}

	public String toString() {
		Float f = new Float(this.value);
		return(f.toString());
	}
	
	/**
	 * Compare to target SVFloat
	 * @param target SVFloat compared to
	 */
	@Override
	public int compareTo(SVFloat target) {
		Float src = new Float(this.value);
		Float trg = new Float(target.getValue());
		
		return(src.compareTo(trg));
	}
}
