package com.hills.sim.indis;

import com.hills.sim.exceptions.CandlesHistoryExc;
import com.hills.sim.exceptions.IndicatorExc;
import com.hills.sim.logs.LogRow;
import com.hills.sim.stage.Candle;
import com.hills.sim.stage.CandlesHistory;

import com.hills.sim.Constants;

public class TASma extends Indicator {
	
	public static final String[] HEADER_LOG =
			new String[] {
					"TA_SMA_PERIOD",
					"TA_SMA_BIDO",
					"TA_SMA_BIDH",
					"TA_SMA_BIDL",
					"TA_SMA_BIDC",
					"TA_SMA_ASKO",
					"TA_SMA_ASKH",
					"TA_SMA_ASKL",
					"TA_SMA_ASKC",
			};
	
	private int period;
	private CandlesHistory window;
	private float[] bid;
	private float[] ask;
	
	public TASma(int period) throws IndicatorExc {
		super();
		
//		Initialize the period
		try {
			this.period = period;
			this.isValidPeriod();
			
		} catch (IndicatorExc e) {
			String error_msg = "%s: Error making TA Sma (invalid period)\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new IndicatorExc();
		}
		
//		Initialize the window
		try {
			this.window = new CandlesHistory(this.period);
			
		} catch (CandlesHistoryExc e) {
			String error_msg = "%s: Error making TA Sma (invalid window)\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new IndicatorExc();
		}
		
//		Initialize the averages OHLC order
		this.bid = new float[] {0, 0, 0, 0};
		this.ask = new float[] {0, 0, 0, 0};
	}

	
	/**
	 * Determines if the period is valid. Period must be greater than or equal
	 * to 0. Period must be less than or equals to the total numbers of elements
	 * allowed
	 * @throws IndicatorExc
	 */
	private void isValidPeriod() throws IndicatorExc {
		
		if(this.period <= 0) {
			String error_msg = "%s: Period is less than or equals to 0\n";
			System.err.printf(error_msg, this.getClass().getName());
			throw new IndicatorExc();
			
		} else if(this.period > Constants.TA_MAX_ELEM) {
			String error_msg = "%s: Period is greater than TA maximum elements allowed\n";
			System.err.printf("Look back buffer: %d\n", Constants.TA_MAX_ELEM);
			System.err.printf(error_msg, this.getClass().getName());
			throw new IndicatorExc();
		}
	}
	
	
	/**
	 * View Candle by updating the window and moving averages
	 */
	@Override
	public void viewCandle(Candle candle) throws IndicatorExc {
		
		try {
			this.window.viewCandle(candle);
			
		} catch (CandlesHistoryExc e) {
			String error_msg = "%s: Window cannot view candle\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new IndicatorExc();
		}
		
		this.updateAverages();
	}
	
	
	/**
	 * Update the averages after the window has viewed each candle
	 * @throws IndicatorExc
	 */
	private void updateAverages() throws IndicatorExc {
		
		int divisor = this.window.getNumViewedCandles() >= this.period
				? this.period : this.window.getNumViewedCandles();
		
		if(divisor == 0) {
			String error_msg = "%s: Divisor for sma is 0\n";
			System.err.printf(error_msg, this.getClass().getName());
			throw new IndicatorExc();
		}
		
		this.bid[0] = this.window.reduceFloat(Candle::getBidO) / divisor;
		this.bid[1] = this.window.reduceFloat(Candle::getBidH) / divisor;
		this.bid[2] = this.window.reduceFloat(Candle::getBidL) / divisor;
		this.bid[3] = this.window.reduceFloat(Candle::getBidC) / divisor;
		
		this.ask[0] = this.window.reduceFloat(Candle::getAskO) / divisor;
		this.ask[1] = this.window.reduceFloat(Candle::getAskH) / divisor;
		this.ask[2] = this.window.reduceFloat(Candle::getAskL) / divisor;
		this.ask[3] = this.window.reduceFloat(Candle::getAskC) / divisor;
	}
	
	
	/**
	 * Save to log row
	 */
	@Override
	public LogRow toLogRow() {
		LogRow result = null;
		
		String period = String.format("%d", this.period);
		String bido = String.format("%.5f", this.bid[0]);
		String bidh = String.format("%.5f", this.bid[1]);
		String bidl = String.format("%.5f", this.bid[2]);
		String bidc = String.format("%.5f", this.bid[3]);
		String asko = String.format("%.5f", this.ask[0]);
		String askh = String.format("%.5f", this.ask[1]);
		String askl = String.format("%.5f", this.ask[2]);
		String askc = String.format("%.5f", this.ask[3]);
		
		String[] row = new String[] {
				period,
				bido,
				bidh,
				bidl,
				bidc,
				asko,
				askh,
				askl,
				askc
			};
		
		result = new LogRow(row);
		return(result);
	}
	
	
	/**
	 * Save to log header
	 */
	@Override
	public LogRow toLogHeader() {
		LogRow result = new LogRow(TASma.HEADER_LOG);
		return(result);
	}
	
	
	/**
	 * Getters
	 */
	/**
	 * @return the period
	 */
	public int getPeriod() {
		return period;
	}
	
	/**
	 * Get Bid Open
	 * @return BidO float
	 */
	public float getBidO() {
		return this.bid[0];
	}
	
	/**
	 * Get Bid High
	 * @return BidH float
	 */
	public float getBidH() {
		return this.bid[1];
	}
	
	/**
	 * Get Bid Close
	 * @return BidC float
	 */
	public float getBidC() {
		return this.bid[2];
	}
	
	/**
	 * Get Bid Low
	 * @return BidL float
	 */
	public float getBidL() {
		return this.bid[3];
	}
	
	/**
	 * Get Ask Open
	 * @return AskO float
	 */
	public float getAskO() {
		return this.ask[0];
	}
	
	/**
	 * Get Ask High
	 * @return AskH float
	 */
	public float getAskH() {
		return this.ask[1];
	}
	
	/**
	 * Get Ask Close
	 * @return AskC float
	 */
	public float getAskC() {
		return this.ask[2];
	}
	
	/**
	 * Get Ask Low
	 * @return AskL float
	 */
	public float getAskL() {
		return this.ask[3];
	}
	

}
