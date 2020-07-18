package com.hills.sim.stage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.hills.sim.settings.Run;
import com.hills.sim.settings.SESettings;
import com.hills.sim.Constants.OrderDirectionE;
import com.hills.sim.exceptions.CalculatorExc;
import com.hills.sim.exceptions.OrderBookExc;
import com.hills.sim.exceptions.OrderExc;
import com.hills.sim.logs.LogRow;
import com.hills.sim.logs.Loggable;
import com.hills.sim.worker.RunHandler;


public class OrderBook implements 
								CandleViewer, RunHandler, Loggable {

	private final Calculator calculator;
	private final List<Order> open_orders;
	private final List<Order> closed_orders;
	private Account account;
	
	private Run run;
	private Candle current_candle;
	private int order_id;
	private long order_log_id;
	
	/**
	 * Make Orders
	 * @param settings SimEngine settings
	 * @throws OrderBookExc
	 */
	public OrderBook(SESettings settings) throws OrderBookExc {
		this.run = null;
		this.current_candle = null;
		this.order_id = 1;
		this.order_log_id = 1;
		
//		Make Calculator object
		try {
			this.calculator = new Calculator(settings);
			
		} catch (CalculatorExc e) {
			String error_msg = "%s: Error making Calculator\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new OrderBookExc();
		}
		
		
//		Make new lists
		try {
			this.open_orders = new ArrayList<Order>();
			this.closed_orders = new ArrayList<Order>();
			
		} catch (Exception e) {
			String error_msg = "%s: Error making lists for orders\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new OrderBookExc();
		}
		
	}

	
	/**
	 * Open a Sell MarketOrder
	 * @param contract_size Decimal number
	 * @param slippage Whole number, in points
	 * @param commission Decimal number
	 * @return
	 */
	public Order openSellMarketOrder(float contract_size, 
									int slippage, 
									float commission) {
		
		try {
			Order order = this.openMarketOrder(OrderDirectionE.SELL, 
										contract_size, 
										slippage, 
										commission);
			open_orders.add(order);
			return(order);
			
		} catch (OrderExc e) {
			return(null);
		}
	}
	
	
	/**
	 * Open a Buy MarketOrder
	 * @param contract_size Decimal number
	 * @param slippage Whole number, in points
	 * @param commission Decimal number
	 * @return
	 */
	public Order openBuyMarketOrder(float contract_size, 
									int slippage, 
									float commission) {
		
		try {
			Order order = this.openMarketOrder(OrderDirectionE.BUY, 
										contract_size, 
										slippage, 
										commission);
			open_orders.add(order);
			return(order);
			
		} catch (OrderExc e) {
			return(null);
		}
	}
	
	
	
	
	/**
	 * Helper method to create a new MarketOrder
	 * @param direction Direction of the order
	 * @param contract_size Decimal number
	 * @param slippage Whole number, in points
	 * @param commission Decimal number, in currency
	 * @return
	 * @throws OrderExc
	 */
	private Order openMarketOrder(OrderDirectionE direction,
								float contract_size,
								int slippage,
								float commission) 
							throws OrderExc {
		
		float entry_price = 0;
		float filled_price;
		
		
//		No orders are created on the last candle of the run
		if(this.current_candle.isLastCandle()) {
			return(null);
		}
		
		
//		Make the MarketOrder object
		Order result = new Order(this.account, this.calculator);
		try {
			
//			Set the function arguments into the object
			result.setTypeMarket();
			result.setDirection(direction);
			result.setContractSize(contract_size);
			result.setSlippage(slippage);
			result.setCommission(commission);
			
//			Set up for submitting the order to Account, for approval
			result.setCurrentCandle(this.current_candle);
			result.setTimeOpen();
			
//			Determine the correct entry price for a MarketOrder
//			Buy MarketOrder: Ask close = Bid close + spread
//			Sell MarketOrder: Bid close			
			
			entry_price = result.isBuyOrder()
					    ? result.getAskClose()
					    : result.getBidClose();
			
			result.setEntryPrice(entry_price);
			
		} catch (OrderExc e) {
			String error_msg = 
			"%s: Error setting members for new MarketOrder.\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new OrderExc();
		}
		

//		Calculate the filled price, which is close including spread
//		Include the slippage: Add slippage for buy orders to enter at a 
//		higher price.
//		Minus slippage for sell orders to enter at a lower price.
		filled_price = result.calcFilledPrice(entry_price);
		
		
//		Send the MarketOrder to Account, for approval.
//		If the order is approved, it is filled. Otherwise, it is killed.
		try {
			result.fill(filled_price);
			
		} catch (OrderExc e) {
			String error_msg = "%s: Error approving new MarketOrder.\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new OrderExc();
		}

		
//		Set up the identification in the MarketOrder
		try {
			result.setOrderID(this.order_id++);
			
		} catch (OrderExc e) {
			String error_msg = 
					"%s: Error setting identification for new MarketOrder.\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new OrderExc();
		}
		
//		Record the current status as the first LogRow
		result.saveLogRow();
			
		return(result);
	}
	
	
	/**
	 * Open a Sell StopOrder
	 * @param entry_price Decimal number
	 * @param contract_size Decimal number
	 * @param slippage Whole number, in points
	 * @param commission Decimal number
	 * @return
	 */
	public Order openSellStopOrder(float entry_price,
									float contract_size, 
									int slippage, 
									float commission) {
		
		try {
			Order order = this.openStopOrder(OrderDirectionE.SELL, 
										entry_price,
										contract_size, 
										slippage, 
										commission);
			open_orders.add(order);
			return(order);
			
		} catch (OrderExc e) {
			return(null);
		}
	}
	
	
	/**
	 * Open a Buy StopOrder
	 * @param entry_price Decimal number
	 * @param contract_size Decimal number
	 * @param slippage Whole number, in points
	 * @param commission Decimal number
	 * @return
	 */
	public Order openBuyStopOrder(float entry_price,
									float contract_size, 
									int slippage, 
									float commission) {
		
		try {
			Order order = this.openStopOrder(OrderDirectionE.BUY, 
										entry_price,
										contract_size, 
										slippage, 
										commission);
			open_orders.add(order);
			return(order);
			
		} catch (OrderExc e) {
			return(null);
		}
	}
	
	
	/**
	 * Helper function to create a new Stop Order
	 * @param direction Direction of the order (Buy or Sell)
	 * @param entry_price Decimal number
	 * @param contract_size Decimal number
	 * @param slippage Whole number, in points
	 * @param commission Decimal number
	 * @return
	 * @throws OrderExc
	 */
	private Order openStopOrder(OrderDirectionE direction,
								float entry_price,
								float contract_size,
								int slippage,
								float commission) 
										throws OrderExc {
		
		
//		No orders are created on the last candle of the run
		if(this.current_candle.isLastCandle()) {
			return(null);
		}
		
//		Make the Order object
		Order result = new Order(this.account, this.calculator);
		try {
			
//			Set the function arguments into the object
			result.setTypeStop();
			result.setDirection(direction);
			result.setCurrentCandle(this.current_candle);
			result.setTimeOpen();

			result.setEntryPrice(entry_price);
			result.setContractSize(contract_size);
			result.setSlippage(slippage);
			result.setCommission(commission);
			
		} catch (OrderExc e) {
			String error_msg = 
			"%s: Error setting members for new StopOrder.\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new OrderExc();
		}		
		
		
		
//		Set up the identification in the StopOrder
		try {
			result.setOrderID(this.order_id++);
			
		} catch (OrderExc e) {
			String error_msg = 
					"%s: Error setting identification for new StopOrder.\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new OrderExc();
		}
			
		
//		Record the current status as the first LogRow
		result.saveLogRow();
		
		return(result);
	}
	
	
	
	/**
	 * Open a Sell LimitOrder
	 * @param entry_price Decimal number
	 * @param contract_size Decimal number
	 * @param slippage Whole number, in points
	 * @param commission Decimal number
	 * @return
	 */
	public Order openSellLimitOrder(float entry_price,
									float contract_size, 
									int slippage, 
									float commission) {
		
		try {
			Order order = this.openLimitOrder(OrderDirectionE.SELL, 
										entry_price,
										contract_size, 
										slippage, 
										commission);
			open_orders.add(order);
			return(order);
			
		} catch (OrderExc e) {
			return(null);
		}
	}
	
	
	/**
	 * Open a Buy LimitOrder
	 * @param entry_price Decimal number
	 * @param contract_size Decimal number
	 * @param slippage Whole number, in points
	 * @param commission Decimal number
	 * @return
	 */
	public Order openBuyLimitOrder(float entry_price,
									float contract_size, 
									int slippage, 
									float commission) {
		
		try {
			Order order = this.openLimitOrder(OrderDirectionE.BUY, 
										entry_price,
										contract_size, 
										slippage, 
										commission);
			open_orders.add(order);
			return(order);
			
		} catch (OrderExc e) {
			return(null);
		}
	}
	
	/**
	 * Helper function to create a new Limit Order
	 * @param direction Order Direction (Buy or Sell)
	 * @param entry_price Decimal number
	 * @param contract_size Decimal number
	 * @param slippage Whole number, in points
	 * @param commission Decimal number
	 * @return
	 * @throws OrderExc
	 */
	private Order openLimitOrder(OrderDirectionE direction,
								float entry_price,
								float contract_size,
								int slippage,
								float commission) 
										throws OrderExc {
		
		
//		No orders are created on the last candle of the run
		if(this.current_candle.isLastCandle()) {
			return(null);
		}
		
		
//		Make the MarketOrder object
		Order result = new Order(this.account, this.calculator);
		try {
			
//			Set the function arguments into the object
			result.setTypeLimit();
			result.setDirection(direction);
			result.setCurrentCandle(this.current_candle);
			result.setTimeOpen();
			
			result.setEntryPrice(entry_price);
			result.setContractSize(contract_size);
			result.setSlippage(slippage);
			result.setCommission(commission);
			
		} catch (OrderExc e) {
			String error_msg = 
			"%s: Error setting members for new LimitOrder.\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new OrderExc();
		}		
		
		
		
//		Set up the identification in the StopOrder
		try {
			result.setOrderID(this.order_id++);
			
		} catch (OrderExc e) {
			String error_msg = 
					"%s: Error setting identification for new LimitOrder.\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new OrderExc();
		}
		
//		Record the current status as the first LogRow
		result.saveLogRow();
		
		return(result);
	}
	
	
	/**
	 * Open a Test Order. A test order helps to test the margin calculations
	 * of Account/Order (i.e. Margin call).
	 * @return TestOrder
	 * @throws OrderExc
	 */
	public Order openTestOrder() throws OrderExc {
		try {
			Order order = new TestOrder(this.account, this.calculator);
			open_orders.add(order);
			return(order);
			
		} catch (OrderExc e) {
			return(null);
		}
	}
	
	

	/**
	 * Get the LogRows for Order class, with Order Row ID and Run ID
	 * @return rows LogRow
	 */
	@Override
	public List<LogRow> toLogRows() {
		List<LogRow> result = null;
		if(this.closed_orders.size() == 0) return(null);
		result = this.closed_orders.stream()
									.map(Order::toLogRows)
									.flatMap(List::stream)
									.collect(Collectors.toList());
		
		for(LogRow row : result) {
			row.prepend(Long.toString(this.order_log_id));
			row.append(Integer.toString(this.run.getId()));
			this.order_log_id++;
		}
		
		this.closed_orders.clear();
		
		return(result);
	}


	@Override
	public void setRun(Run run) {
		this.run = run;
		this.order_id = 1;
		this.open_orders.clear();
		this.closed_orders.clear();
	}
	
	
	/**
	 * Close all open orders for the next run
	 * All closed orders will be removed for logging
	 */
	@Override
	public void reset() {
	}
	
	
	/**
	 * Close all open orders. Used in Account MarginCall and at the end of the last
	 * Candle view.
	 * Replace the last LogRow with the correct LogRow, that shows the orders
	 * have been closed.
	 */
	public void closeAllOpenOrders() throws OrderExc {
		for(Order order: this.open_orders)
			if(order.isOpen()) {
				order.close();
				order.popLastLogRow();
				order.saveLogRow();
			}
	}
	
	
	/**
	 * Counts the total Open-Filled orders
	 * @return
	 */
	public int totalOpenFilledOrders() {
		int result = 0;
		
		for(Order order: this.open_orders) 
			if(order.isOpen() && order.isFilled()) 
				result++;
		
		return(result);
	}
	
	
	/**
	 * Also known as totalUsedMargin for all Open-Filled orders.
	 * Add all the required margin for Open-Filled orders	  
	 * @return
	 */
	public float totalRequiredMargin() {
		float result = 0;
		
		for(Order order: this.open_orders) 
			if(order.isOpen() && order.isFilled()) 
				result += order.getRequiredMargin();
		
		
		return(result);
	}
	
	
	/**
	 * Add all the unrealized PnL for Open-Filled orders
	 * @return
	 */
	public float totalUnrealizedPnL() {
		float result = 0;
		
		for(Order order: this.open_orders) 
			if(order.isOpen() && order.isFilled()) 
				result += order.getPnl();
		
		
		return(result);
	}
	
	
	/**
	 * Add all the unrealized PnL for Open-Filled orders
	 * @return
	 */
	public float totalRealizedPnL() {
		float result = 0;
		
		for(Order order: this.open_orders) 
			if(order.isClose() && order.isFilled()) 
				result += order.getPnl();
		
		
		return(result);
	}
	
	
	/**
	 * Listen to the Candle.
	 * Save the current candle and update all orders with the current
	 * candle.
	 * Margin information is updated after each order is traversed.
	 * If the margin level has dropped below the stop out level, after an order,
	 * perform a margin call.
	 * Flush all closed orders for logging
	 * @param candle Candle to listen to
	 */
	@Override
	public void viewCandle(Candle candle) 
							throws OrderBookExc {
		
//		Save the current candle
		this.current_candle = candle;
		
//		Listen only when there are orders
		if(this.open_orders.size() == 0) return;
	
//		Update all orders
//		update the margin info after listening to the candle 
//		Perform margin call, if necessary
		for(Order order: this.open_orders) {
			try {
				order.viewCandle(candle);
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error hearing candle for orders.\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
				throw new OrderBookExc();
			}
			
			this.account.updateMarginInfo();
		
//		When margin calculations are enabled, always see if margin call
//		should be called after the margin information is updated after
//		each order is traversed.
			try {
				this.account.performMarginCall();
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error performing margin call\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
				throw new OrderBookExc();
			}
		}
		
//		Close all open orders when the last candle is encountered
		if(this.current_candle.isLastCandle())
			try {
				this.closeAllOpenOrders();
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error closing all open orders\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
				throw new OrderBookExc();
			}
		
		this.flushClosedOrders();
	}
	
	
	/**
	 * Flush out all closed orders from the list of open orders
	 * Save all the closed orders for logging
	 */
	private void flushClosedOrders() {
		List<Order> closed_orders = this.open_orders.stream()
												.filter(Order::isClose)
												.collect(Collectors.toList());
		
		for(Order order: closed_orders) this.open_orders.remove((Object) order);
		
		this.closed_orders.addAll(closed_orders);
		
	}
	
	
	
	
	/**
	 * @return the calculator
	 */
	public Calculator getCalculator() {
		return calculator;
	}
	
	
	//	Setters
	/**
	 * Allow the Account to be saved as a reference under Orders.
	 * This method call must be made after Orders is made, in order
	 * to reference Account.
	 * @param account Account object
	 */
	public void setAccount(Account account) {
		this.account = account;
	}
	
}
