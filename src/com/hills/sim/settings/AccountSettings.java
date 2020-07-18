package com.hills.sim.settings;

import java.util.List;

import com.hills.sim.exceptions.BigSettingFrameExc;
import com.hills.sim.exceptions.BigSettingExc;

public class AccountSettings extends BigSetting {

	private final float balance;
	private final String currency;
	private final boolean margin_calc_enabled;
	private final int stop_out_level;
	
	
	/**
	 * Make AccountSettings
	 * @param prefix Prefix for the ID attribute of Html TR tag
	 * @param rframe Raw frame that contains the definitions
	 * @param html_settings Settings that are parsed from Html 
	 * @throws BigSettingExc
	 */
	public AccountSettings 
		(String prefix, String[][] rframe, List<Setting> html_settings) 
										throws BigSettingExc {
		
		super(prefix, rframe, html_settings);
		
		try {
			this.balance = this.bsf.getFloatValue("ACC_BALANCE");
			this.currency = this.bsf.getStringValue("ACC_CURRENCY");
			this.margin_calc_enabled = this.bsf.getBooleanValue
												("ACC_MARGIN_CALC");
			this.stop_out_level = this.bsf.getIntegerValue
												("ACC_STOP_OUT_LEVEL");
			
		} catch (BigSettingFrameExc e) {
			String error_msg = "%s: Error getting values for AccountSettings\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new BigSettingExc();
		}
	}
	
	/**
	 * Getters
	 * @return
	 */
	
	public float getBalance() {
		return balance;
	}

	public String getCurrency() {
		return currency;
	}

	public boolean isMarginCalcEnabled() {
		return margin_calc_enabled;
	}

	public int getStopOutLevel() {
		return stop_out_level;
	}

	@Override
	public String toString() {
		return "AccountSettings "
				+ "\n[ balance=" + balance 
				+ "\n, currency=" + currency 
				+ "\n, margin_calc_enabled=" + margin_calc_enabled 
				+ "\n, stop_out_level=" + stop_out_level + "]\n";
	}
	
	
}
