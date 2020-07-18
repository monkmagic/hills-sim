package com.hills.sim.ea;

import com.hills.sim.ea.prototype.EAConstants.XoverDirectionE;
import com.hills.sim.ea.prototype.EAIndicators;
import com.hills.sim.ea.prototype.EALogBook;
import com.hills.sim.ea.prototype.EARun;
import com.hills.sim.exceptions.CandlesHistoryExc;
import com.hills.sim.exceptions.IndicatorExc;
import com.hills.sim.exceptions.LogBookExc;
import com.hills.sim.exceptions.OrderExc;
import com.hills.sim.exceptions.StrategyExc;
import com.hills.sim.logs.LogRow;
import com.hills.sim.settings.Run;
import com.hills.sim.settings.SESettings;
import com.hills.sim.stage.Candle;
import com.hills.sim.stage.CandleComparator;
import com.hills.sim.stage.CandlesHistory;
import com.hills.sim.stage.Order;
import com.hills.sim.stage.Stage;

/**
 * TestStrategy is the Strategy class where tests are performed
 * @author Mark L
 *
 */
public class Prototype extends Strategy {

	private EAIndicators ea_indis;
	private EALogBook ea_logbook;
	private EARun ea_run;
	
	private CandlesHistory window;
	private Order order;
	private int candle_count;
	private XoverDirectionE xover_dir;
			
	public Prototype(Stage stage, SESettings settings) throws StrategyExc {
		super(stage, settings);
		
		this.ea_indis = new EAIndicators();
		
		try {
			this.ea_logbook = new EALogBook(settings);
			
		} catch (LogBookExc e) {
			String error_msg = "%s: Error creating log book for EA\n";
			System.err.printf(error_msg, this.getClass().getName());
			e.printStackTrace();
			throw new StrategyExc();
		}
		
		this.order = null;
		this.candle_count = 0;
	}
	
	
	/**
	 * Set the run
	 * @param run
	 */
	@Override
	public void setRun(Run run) {
		this.run = run;
		this.ea_run = (EARun) this.run;
		int window_size = this.ea_run.getWindow();
		
		try {
			this.ea_indis.setRun(run);
			
		} catch (IndicatorExc e) {
			String error_msg = "%s: Error set EAIndicator for EA\n";
			System.err.printf(error_msg, this.getClass().getName());
			e.printStackTrace();
		}
		
		try {
			this.window = new CandlesHistory(window_size);
			
		} catch (CandlesHistoryExc e) {
			String error_msg = "%s: Error creating CandlesHistory for EA Run\n";
			System.err.printf(error_msg, this.getClass().getName());
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Set the run as null and reset the Strategy members
	 */
	@Override
	public void reset() {
		this.run = null;
		this.order = null;
		this.window = null;
		
		this.candle_count = 0;
		this.ea_indis.reset();
	}
	
	
	/**
	 * Connect the EAIndicators and EALogBook to the viewCandle
	 * function
	 */
	@Override
	public void viewCandle(Candle candle) throws Exception {
		LogRow row;
		if(this.candle_count == 0) {
			row = this.ea_indis.toLogHeader();
			this.ea_logbook.writeIndisLog(row);
		}
		
		this.ea_indis.viewCandle(candle);
		this.window.viewCandle(candle);
		
		row = this.ea_indis.toLogRow();
//		this.ea_logbook.writeIndisLog(row);
		
		this.candle_count++;
		this.xover_dir = this.getCrossoverDirection();
		
		this.execute(candle);
	}
	
	
	/**
	 * Execute the Strategy
	 * @param candle
	 */
	private void execute(Candle candle) {
		
//		Skip if the crossover direction is doji
		if(this.isDojiXover()) return;
		
//		Close the order when the xover direction opposes the order direction
		if(order != null) 
			if((this.order.isBuyOrder() && this.isBearXover()) ||
			   (this.order.isSellOrder() && this.isBullXover())) {
				try {
					this.order.close();
					
				} catch (OrderExc e) {
					String error_msg = "%s: Error closing order\n";
					System.err.printf(error_msg, this.getClass().getName());
					e.printStackTrace();
				}
				
				this.order = null;
			}
		
//		Open the order
//		When applicable, open the buy order
		if(order == null) {
			if(this.isBullXover()) 
				if(this.isMaxInWindow(candle)) {
					float cur_bidl = candle.getBidL();
					this.order = this.stage.openBuyMarketOrder(0.1F, 0, 0);
					
					try {
						this.order.setStopLoss(cur_bidl);
					} catch (OrderExc e) {
						String error_msg = "%s: Error setting stop loss.\n";
						System.err.printf(error_msg, this.getClass().getName());
						e.printStackTrace();
					}
				}
			
			else if(this.isBearXover())
				if(this.isMinInWindow(candle)) {
					float cur_askh = candle.getAskH();
					this.order = this.stage.openSellMarketOrder(0.1F, 0, 0);
					
					try {
						this.order.setStopLoss(cur_askh);
					} catch (OrderExc e) {
						String error_msg = "%s: Error setting stop loss.\n";
						System.err.printf(error_msg, this.getClass().getName());
						e.printStackTrace();
					}
				}
		}
		
	}

	
	/**
	 * Identify if the current candle contains the maximum BidC within the Window
	 * @param candle
	 * @return result boolean
	 */
	private boolean isMaxInWindow(Candle candle) {
		boolean result = false;
		int decimal_pl = this.settings.getSymSettings().getDistance();
		
		float max_bidc = this.window.maxBy(Candle::getBidC, CandleComparator.BidC);
		max_bidc = this.calculator.round(max_bidc, decimal_pl);
		
		float curr_bidc = candle.getBidC();
		curr_bidc = this.calculator.round(curr_bidc, decimal_pl);
		
		result = curr_bidc == max_bidc ? true : false;
		
		return(result);
	}
	
	
	/**
	 * Identify if the current candle contains the minimum BidC within the Window
	 * @param candle
	 * @return result boolean
	 */
	private boolean isMinInWindow(Candle candle) {
		boolean result = false;
		int decimal_pl = this.settings.getSymSettings().getDistance();
		
		float min_bidc = this.window.minBy(Candle::getBidC, CandleComparator.BidC);
		min_bidc = this.calculator.round(min_bidc, decimal_pl);
		
		float curr_bidc = candle.getBidC();
		curr_bidc = this.calculator.round(curr_bidc, decimal_pl);
		
		result = curr_bidc == min_bidc ? true : false;
		
		return(result);
	}
	
	
	/**
	 * Get the Crossover Direction for the Fast and Slow SMA
	 * @return direction Signal to enter a buy/sell/doji trades
	 */
	private XoverDirectionE getCrossoverDirection() {
		XoverDirectionE result;
		
		int decimal_pl = this.settings.getSymSettings().getDistance();
		float fast = this.ea_indis.getFastSma().getBidC();
		float slow = this.ea_indis.getSlowSma().getBidC();
		
		fast = this.calculator.round(fast, decimal_pl);
		slow = this.calculator.round(slow, decimal_pl);
		
		if(fast == slow)
			result = XoverDirectionE.DOJI;
		else if(fast > slow)
			result = XoverDirectionE.BULL;
		else
			result = XoverDirectionE.BEAR;
		
		return(result);
	}
	
	
	/**
	 * Bull crossover direction
	 * @return
	 */
	private boolean isBullXover() {
		return(this.xover_dir == XoverDirectionE.BULL);
	}
	
	
	/**
	 * Bear crossover direction
	 * @return
	 */
	private boolean isBearXover() {
		return(this.xover_dir == XoverDirectionE.BEAR);
	}
	
	
	/**
	 * Doji crossover direction
	 * @return
	 */
	private boolean isDojiXover() {
		return(this.xover_dir == XoverDirectionE.DOJI);
	}
	
//	Filler code

	/**
	 * @return the ea_run
	 */
	public EARun getEa_run() {
		return ea_run;
	}


	/**
	 * @return the order
	 */
	public Order getOrder() {
		return order;
	}


	/**
	 * @return the candle_count
	 */
	public int getCandle_count() {
		return candle_count;
	}


	/**
	 * @return the ea_indis
	 */
	public EAIndicators getEa_indis() {
		return ea_indis;
	}


	/**
	 * @return the ea_logbook
	 */
	public EALogBook getEa_logbook() {
		return ea_logbook;
	}

}
