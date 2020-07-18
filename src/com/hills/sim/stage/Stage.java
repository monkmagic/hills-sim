package com.hills.sim.stage;

import java.util.List;

import com.hills.sim.Constants;
import com.hills.sim.exceptions.AccountExc;
import com.hills.sim.exceptions.CandlesHistoryExc;
import com.hills.sim.exceptions.CalculatorExc;
import com.hills.sim.exceptions.OrderBookExc;
import com.hills.sim.exceptions.StageExc;
import com.hills.sim.logs.LogRow;
import com.hills.sim.logs.LogRowsBag;
import com.hills.sim.logs.Loggable;
import com.hills.sim.logs.Reporter;
import com.hills.sim.settings.Run;
import com.hills.sim.settings.SESettings;
import com.hills.sim.worker.RunHandler;


/**
 * Stage class is the UI to SimEngine platform
 * @author Mark L
 *
 */
public class Stage implements 
							RunHandler, CandleViewer, Loggable {

	private final SESettings settings;
	
	private final Account account;
	private final Calculator calculator;
	private final CandlesHistory history;
	private final OrderBook orders;
	
	private final Reporter reporter;
	private boolean is_last_candle;
	
	
	/**
	 * Make Stage
	 * @param settings SimEngine settings
	 * @throws StageExc
	 */
	public Stage(SESettings settings) throws StageExc {
		
		this.settings = settings;
		this.is_last_candle = false;
		
//		Make Orders object. Have to reference Account to Orders
//		later
		try {
			this.orders = new OrderBook(settings);
			
		} catch (OrderBookExc e) {
			String error_msg = "%s: Error making Orders\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new StageExc();
		}
		
//		Make Account object and set Orders to refer to Account
		try {
			this.account = new Account(this.settings, this.orders);
			this.orders.setAccount(this.account);
			
		} catch (AccountExc e) {
			String error_msg = "%s: Error making Account\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new StageExc();
		}
		
//		Make Calculator object
		try {
			this.calculator = new Calculator(this.settings);
			
		} catch (CalculatorExc e) {
			String error_msg = "%s: Error making Calculator\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new StageExc();
		}
		
//		Make the History object, with the capacity defined in EA package
		try {
			String strategy_name = this.settings.getGenSettings().getStrategyName();
			String class_path = Constants.getPackageName(strategy_name) + ".EAConstants";
			Class<?> ea_constants = Class.forName(class_path);
			int capacity = (int) ea_constants.getField("HISTORY_LIMIT").get(int.class);
			
			this.history = new CandlesHistory(capacity);
			
		} catch (Exception e) {
			String error_msg = "%s: Error making History\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new StageExc();
		}
		
		this.reporter = new Reporter();
		
	}

	
	/**
	 * Listen to the candles
	 * @param candle Candle to listen to
	 */
	@Override
	public void viewCandle(Candle candle) throws StageExc {
		
		this.is_last_candle = candle.isLastCandle()
								? true : false;
		
		try {
			this.account.viewCandle(candle);
			
		} catch (AccountExc e) {
			String error_msg = "%s: Error viewing candles for Account\n";
			System.err.printf(error_msg, this.getClass().getName());
			e.printStackTrace();
			throw new StageExc();
		}
		
		
		try {
			this.history.viewCandle(candle);
			
		} catch (CandlesHistoryExc e) {
			String error_msg = "%s: Error viewing candles for History\n";
			System.err.printf(error_msg, this.getClass().getName());
			e.printStackTrace();
			throw new StageExc();
		}
		
		
		try {
			this.orders.viewCandle(candle);
			
		} catch (OrderBookExc e) {
			String error_msg = "%s: Error viewing candles for Orders\n";
			System.err.printf(error_msg, this.getClass().getName());
			e.printStackTrace();
			throw new StageExc();
		}
		
	}
	
	
	
	/**
	 * Get the Log Rows for all Log files
	 */
	@Override
	public LogRowsBag toLogRowsBag() {
		LogRowsBag result = null;
		
		LogRow candle = this.history.toLogRow();
		LogRow account = this.account.toLogRow();
		List<LogRow> orders = this.orders.toLogRows();
		
		this.reporter.updateLogs(account, orders);
		
		if(this.is_last_candle == false)
			result = new LogRowsBag(candle, account, orders);
		else {
			LogRow report = this.reporter.toLogRow();
			result = new LogRowsBag(candle, account, orders, report);
		}
		
		return(result);
	}
	
	
	@Override
	public void setRun(Run run) {
		this.orders.setRun(run);
		this.account.setRun(run);
		this.reporter.setRun(run);
	}


	/**
	 * Reset the Stage for the next run
	 */
	@Override
	public void reset() {
		this.is_last_candle = false;
		
		this.history.reset();
		this.orders.reset();
		this.account.reset();
		this.reporter.reset();
	}
	

	/**
	 * Getters
	 * @return
	 */
	public Calculator getCalculator() {
		return calculator;
	}
	
	/**
	 * @return Account in String format
	 */
	public Account getAccount() {
		return this.account;
	}
	
	
	public CandlesHistory getHistory() {
		return this.history;
	}
	
	
//	Functions related to opening an Order
	
	/**
	 * Open a Sell LimitOrder
	 * @param entry_price Decimal number
	 * @param contract_size Decimal number
	 * @param slippage Whole number, in points
	 * @param commission Decimal number
	 * @return Order or Null
	 */
	public Order openSellLimitOrder(float entry_price,
									float contract_size, 
									int slippage, 
									float commission) {
		
		try {
			Order order = this.orders
								.openSellLimitOrder(entry_price,
													contract_size, 
													slippage, 
													commission);
			return(order);
			
		} catch (Exception e) {
			return(null);
		}
	}
	
	
	/**
	 * Open a Buy LimitOrder
	 * @param entry_price Decimal number
	 * @param contract_size Decimal number
	 * @param slippage Whole number, in points
	 * @param commission Decimal number
	 * @return Order or Null
	 */
	public Order openBuyLimitOrder(float entry_price,
									float contract_size, 
									int slippage, 
									float commission) {
		
		try {
			Order order = this.orders
							  .openBuyLimitOrder(entry_price,
												contract_size, 
												slippage, 
												commission);
			return(order);
			
		} catch (Exception e) {
			return(null);
		}
	}
	
	
	/**
	 * Open a Sell StopOrder
	 * @param entry_price Decimal number
	 * @param contract_size Decimal number
	 * @param slippage Whole number, in points
	 * @param commission Decimal number
	 * @return Order or Null
	 */
	public Order openSellStopOrder(float entry_price,
									float contract_size, 
									int slippage, 
									float commission) {
		
		try {
			Order order = this.orders
							  .openSellStopOrder(entry_price,
												contract_size, 
												slippage, 
												commission);
			return(order);
			
		} catch (Exception e) {
			return(null);
		}
	}
	
	
	/**
	 * Open a Buy StopOrder
	 * @param entry_price Decimal number
	 * @param contract_size Decimal number
	 * @param slippage Whole number, in points
	 * @param commission Decimal number
	 * @return Order or Null
	 */
	public Order openBuyStopOrder(float entry_price,
									float contract_size, 
									int slippage, 
									float commission) {
		
		try {
			Order order = this.orders
					  		   .openBuyStopOrder(entry_price,
					  				   			contract_size, 
					  				   			slippage, 
					  				   			commission);
			return(order);
			
		} catch (Exception e) {
			return(null);
		}
	}
	
	
	/**
	 * Open a Sell MarketOrder
	 * @param contract_size Decimal number
	 * @param slippage Whole number, in points
	 * @param commission Decimal number
	 * @return Order or Null
	 */
	public Order openSellMarketOrder(float contract_size, 
									int slippage, 
									float commission) {
		
		try {
			Order order = this.orders
								.openSellMarketOrder(contract_size, 
													slippage, 
													commission);
			return(order);
			
		} catch (Exception e) {
			return(null);
		}
	}
	
	
	/**
	 * Open a Buy MarketOrder
	 * @param contract_size Decimal number
	 * @param slippage Whole number, in points
	 * @param commission Decimal number
	 * @return Order or Null
	 */
	public Order openBuyMarketOrder(float contract_size, 
									int slippage, 
									float commission) {
		
		try {
			Order order = this.orders
								.openBuyMarketOrder(contract_size, 
													slippage, 
													commission);
			return(order);
			
		} catch (Exception e) {
			return(null);
		}
	}
	
	
	/**
	 * Open a Test Order. Use the Test Order for testing only.
	 * A test order helps to test the margin calculations
	 * of Account/Order (i.e. Margin call).
	 * @return TestOrder or Null
	 */
	public Order openTestOrder() {
		try {
			Order order = this.orders.openTestOrder();
			return(order);
			
		} catch (Exception e) {
			return(null);
		}
	}
	
	
//	Functions related to History of past candles
	
	/**
	 * Get the capacity (no. of elements) of the History
	 * @return integer
	 */
	public int getHistoryCapacity() {
		return this.history.getCapacity();
	}

	
	/**
	 * Get the number of all candles viewed (up to method call) 
	 * for the current run
	 * @return Integer
	 */
	public int getNumViewedCandles() {
		return(this.history.getNumViewedCandles());
	}


	
}
