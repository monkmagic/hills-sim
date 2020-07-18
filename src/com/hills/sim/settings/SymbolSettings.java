package com.hills.sim.settings;

import java.util.List;

import com.hills.sim.exceptions.BigSettingFrameExc;
import com.hills.sim.exceptions.BigSettingExc;

public class SymbolSettings extends BigSetting {
	
	
	/**
	 * From Html Settings file
	 */
	private final String source;
	private final String name;
	private final String timeframe;
	
	


	private float margin_rate;
	private int pip;
	private int distance;
	private float contract_size_min;
	private float contract_size_int;
	
	/**
	 * A symbol name can be broken to: Base_Quote. 
	 * i.e. SPX500_USD where SPX500 is the base and USD is the quote. 
	 */
	private final String base_name;
	private final String quote_name;
	
	
	/**
	 * Make SymbolSettings
	 * @param prefix Prefix for the ID attribute of Html TR tag
	 * @param rframe Raw frame that contains the definitions
	 * @param html_settings Settings that are parsed from Html 
	 * @throws BigSettingExc
	 */
	public SymbolSettings 
	(String prefix, String[][] rframe, List<Setting> html_settings) 
									throws BigSettingExc {
	
		super(prefix, rframe, html_settings);
		
//		Set the fields from the Html settings file
//		Extract the base and quote names
		try {
			this.source = this.bsf.getStringValue("SYM_SOURCE");
			this.name = this.bsf.getStringValue("SYM_NAME");
			this.timeframe = this.bsf.getStringValue("SYM_TIMEFRAME");
			
			this.base_name = this.setBaseName(this.name);
			this.quote_name = this.setQuoteName(this.name);
			
		} catch (BigSettingFrameExc e) {
			String error_msg = "%s: Error getting values for SymbolSettings\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new BigSettingExc();
		}
	}
	
	
	/**
	 * Set the base name of the symbol
	 * @param name Base name
	 * @return
	 */
	private String setBaseName(String name) {
		String result = name.split("_")[0];
		return(result);
	}
	
	/**
	 * Set the quote name of the symbol
	 * @param name Quote name
	 * @return
	 */
	private String setQuoteName(String name) {
		String result = name.split("_")[1];
		return(result);		
	}
	
	/**
	 * Getters
	 * @return
	 */
	
	
	public String getSource() {
		return source;
	}

	
	public String getName() {
		return name;
	}

	
	public String getTimeframe() {
		return timeframe;
	}

	
	public float getMarginRate() {
		return margin_rate;
	}

	
	public int getPip() {
		return pip;
	}

	
	public int getDistance() {
		return distance;
	}

	
	public float getContractSizeMin() {
		return contract_size_min;
	}

	
	public float getContractSizeInt() {
		return contract_size_int;
	}

	
	
	public String getBaseName() {
		return base_name;
	}


	public String getQuoteName() {
		return quote_name;
	}


	@Override
	public String toString() {
		return "SymbolSettings "
				+ "\n[ source=" + source 
				+ "\n, name=" + name 
				+ "\n, timeframe=" + timeframe 
				+ "\n, margin_rate=" + margin_rate 
				+ "\n, pip=" + pip 
				+ "\n, distance=" + distance 
				+ "\n, contract_size_min=" + contract_size_min
				+ "\n, contract_size_int=" + contract_size_int 
				+ "\n, base_name=" + base_name 
				+ "\n, quote_name=" + quote_name + "]\n";
	}


	
//	Setters
	/**
	 * @param margin_rate the margin_rate to set
	 */
	public void setMarginRate(float margin_rate) {
		this.margin_rate = margin_rate;
	}


	/**
	 * @param pip the pip to set
	 */
	public void setPip(int pip) {
		this.pip = pip;
	}


	/**
	 * @param distance the distance to set
	 */
	public void setDistance(int distance) {
		this.distance = distance;
	}


	/**
	 * @param contract_size_min the contract_size_min to set
	 */
	public void setContractSizeMin(float contract_size_min) {
		this.contract_size_min = contract_size_min;
	}


	/**
	 * @param contract_size_int the contract_size_int to set
	 */
	public void setContractSizeInt(float contract_size_int) {
		this.contract_size_int = contract_size_int;
	}
	
	
}
