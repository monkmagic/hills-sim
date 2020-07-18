package com.hills.sim.ea.prototype;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hills.sim.exceptions.IndicatorExc;
import com.hills.sim.indis.Indicator;
import com.hills.sim.indis.TASma;
import com.hills.sim.logs.LogRow;
import com.hills.sim.settings.Run;
import com.hills.sim.stage.Candle;
import com.hills.sim.worker.RunHandler;

public class EAIndicators extends Indicator 
							implements RunHandler {
	
	private Run run;
	private EARun ea_run;
	private TASma fast_sma;
	private TASma slow_sma;
	private long ta_id;
	
	public EAIndicators() {
		this.run = null;
		this.ea_run = null;
		this.fast_sma = null;
		this.slow_sma = null;
		this.ta_id = 1;
	}
	
	/**
	 * Set the Run and typecast to EARun
	 */
	@Override
	public void setRun(Run run) throws IndicatorExc {
		this.run = run;
		this.ea_run = (EARun) run;
		
		int fast_period = this.ea_run.getFastPeriod();
		int slow_period = this.ea_run.getSlowPeriod();
		
		try {
			this.fast_sma = new TASma(fast_period);
			this.slow_sma = new TASma(slow_period);
			
		} catch (IndicatorExc e) {
			String error_msg = "%s: Error making TA Sma (invalid period)\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new IndicatorExc();
		}
	}
	
	
	@Override
	public void viewCandle(Candle candle) throws IndicatorExc {
		if(this.fast_sma == null || this.slow_sma == null) {
			String error_msg = "%s: Fast/Slow Sma are not initialized\n";
			System.err.printf(error_msg, this.getClass().getName());
			throw new IndicatorExc();
		}
		
		this.fast_sma.viewCandle(candle);
		this.slow_sma.viewCandle(candle);
			
	}
	
	
	@Override
	public void reset() {
		this.fast_sma = null;
		this.slow_sma = null;

		this.run = null;
		this.ea_run = null;
	}

	
	/**
	 * Get the LogHeader
	 */
	@Override
	public LogRow toLogHeader() {
		return(new LogRow(EAConstants.HEADER_INDIS));
	}
	
	
	/**
	 * Get the LogRow
	 */
	@Override
	public LogRow toLogRow() {
		Map<String, String> f_row = this.toLogMap
					(this.fast_sma.toLogHeader(), this.fast_sma.toLogRow());
		
		Map<String, String> s_row = this.toLogMap
					(this.slow_sma.toLogHeader(), this.slow_sma.toLogRow());
		
		String ta_id = String.format("%d", this.ta_id);
		String ta_fsma_period = f_row.get("TA_SMA_PERIOD");
		String ta_ssma_period = s_row.get("TA_SMA_PERIOD");
		String ta_fsma_bido = f_row.get("TA_SMA_BIDO");
		String ta_fsma_bidh = f_row.get("TA_SMA_BIDH");
		String ta_fsma_bidl = f_row.get("TA_SMA_BIDL");
		String ta_fsma_bidc = f_row.get("TA_SMA_BIDC");
		String ta_ssma_bido = s_row.get("TA_SMA_BIDO");
		String ta_ssma_bidh = s_row.get("TA_SMA_BIDH");
		String ta_ssma_bidl = s_row.get("TA_SMA_BIDL");
		String ta_ssma_bidc = s_row.get("TA_SMA_BIDC");
		String run_id = String.format("%d", this.run.getId());
		
		String[] values = new String[] {
				ta_id,
				ta_fsma_period,
				ta_ssma_period,
				ta_fsma_bido,
				ta_fsma_bidh,
				ta_fsma_bidl,
				ta_fsma_bidc,
				ta_ssma_bido,
				ta_ssma_bidh,
				ta_ssma_bidl,
				ta_ssma_bidc,
				run_id
		};
		
		this.ta_id++;
		
		return(new LogRow(values));
	}
	
	/**
	 * Takes a header and content and change them into a LogMap
	 * @param header LogRow
	 * @param content LogRow
	 * @return Map of header and values
	 */
	private Map<String, String> toLogMap(LogRow header, LogRow content)	{
		Map<String, String> result = new HashMap<>();
		
		List<String> columns = header.getValue();
		List<String> values = content.getValue();
		
		for(int i = 0; i < columns.size(); i++) 
			result.put(columns.get(i), values.get(i));
		
		return(result);
	}
	
	/**
	 * Getters
	 */
	/**
	 * Get the Fast Sma
	 * @return the fast_sma
	 */
	public TASma getFastSma() {
		return fast_sma;
	}


	/**
	 * Get the Slow Sma
	 * @return the slow_sma
	 */
	public TASma getSlowSma() {
		return slow_sma;
	}
}
