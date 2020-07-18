package com.hills.sim.settings;

import java.util.List;

import com.hills.sim.settings.HtmlParser;
import com.hills.sim.worker.Compactor;
import com.hills.sim.Constants;
import com.hills.sim.exceptions.BigSettingExc;
import com.hills.sim.exceptions.CompactorExc;
import com.hills.sim.exceptions.HtmlParserExc;
import com.hills.sim.exceptions.SESettingsExc;


public class SESettings {
	
	private final HtmlParser parser;
	private final Compactor compactor;
	
	private final List<Setting> html_settings;
	private final AccountSettings acc_settings;
	private final GeneralSettings gen_settings;
	private final SymbolSettings sym_settings;
	
	
	public SESettings(String filename) throws SESettingsExc {
		
//		Make HtmlParser and HtmlSettings
		try {
			this.parser = new HtmlParser(filename);
			this.html_settings = this.parser.makeSettingsFromHtml();
			
		} catch (HtmlParserExc e) {
			e.printStackTrace();
			System.err.printf("%s: Error parsing html file\n", 
					this.getClass().getName());
			throw new SESettingsExc();
		}
		
		
//		Make BigSettings classes
		try {
			this.acc_settings = new AccountSettings
									(Constants.BIGSETTINGF_PREFIX_ACC,
									 Constants.BIGSETTINGF_ACC,
									 this.html_settings);
			
			this.gen_settings = new GeneralSettings
									(Constants.BIGSETTINGF_PREFIX_GEN,
									 Constants.BIGSETTINGF_GEN,
									 this.html_settings);
			
			this.sym_settings = new SymbolSettings
									(Constants.BIGSETTINGF_PREFIX_SYM,
									 Constants.BIGSETTINGF_SYM,
									 this.html_settings);
									
		} catch (BigSettingExc e) {
			String error_msg = 
						"%s: Error creating BigSetting(s) in SESettings\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new SESettingsExc();
		}
		
		
//		Make Compactor
		try {
			this.compactor = new Compactor(this.sym_settings, this.gen_settings);
			
		} catch (CompactorExc e) {
			String error_msg = "%s: Error making Compactor\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new SESettingsExc();
		}
		
		
//		Set the SymbolSettings required data
		try {
			float margin_rate = this.compactor.getMarginRate();
			int pip = this.compactor.getPip();
			int distance = this.compactor.getDistance();
			float contract_size_min = this.compactor.getContractSizeMin();
			float contract_size_int = this.compactor.getContractSizeInt();
			
			this.sym_settings.setMarginRate(margin_rate);
			this.sym_settings.setPip(pip);
			this.sym_settings.setDistance(distance);
			this.sym_settings.setContractSizeMin(contract_size_min);
			this.sym_settings.setContractSizeInt(contract_size_int);
			
		} catch (Exception e) {
			String error_msg = 
					"%s: Error saving SymbolSettings related data from Compactor\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new SESettingsExc();
		}
		
	}
	
	/**
	 * Getters
	 * @return
	 */
	
	public AccountSettings getAccSettings() {
		return acc_settings;
	}

	public GeneralSettings getGenSettings() {
		return gen_settings;
	}

	public SymbolSettings getSymSettings() {
		return sym_settings;
	}
	
	public List<Setting> getHtmlSettings() {
		return html_settings;
	}
	
	public Compactor getCompactor() {
		return compactor;
	}

	@Override
	public String toString() {
		return "SESettings "
				+ "\n[ parser=" + parser 
				+ "\n, html_settings=" + html_settings 
				+ "\n, acc_settings=" + acc_settings
				+ "\n, gen_settings=" + gen_settings  
				+ "\n, sym_settings=" + sym_settings + "]\n";
	}


}
