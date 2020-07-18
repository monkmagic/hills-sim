package com.hills.sim.stage;

import com.hills.sim.Constants.OrderFillE;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.hills.sim.Constants;
import com.hills.sim.Constants.OrderDirectionE;
import com.hills.sim.Constants.OrderStatusE;
import com.hills.sim.Constants.OrderTypeE;
import com.hills.sim.exceptions.CalculatorExc;
import com.hills.sim.exceptions.OrderExc;
import com.hills.sim.logs.LogRow;
import com.hills.sim.logs.Loggable;

public class Order implements CandleViewer, Loggable {

	private Account account;
	private Calculator calculator;
	
	/**
	 * Identification 
	 * Allows values to be set externally
	 */
	protected int order_id;
	protected List<LogRow> log_rows;
	
	
	/**
	 * Nature/Status 
	 */
	protected OrderDirectionE direction;
	protected OrderTypeE type;
	protected OrderStatusE status;
	protected OrderFillE fill_status;
	
	
	/**
	 * Logging 
	 */
	protected Candle current_candle;
	protected int candle_count;
	protected LocalDateTime time_open;
	protected LocalDateTime time_filled;
	protected LocalDateTime time_closed;
	
	
	/**
	 * UI members: Subject to changes from EA and other sources
	 */
	protected float entry_price;
	protected float stop_loss;
	protected float take_profit;
	protected float contract_size;
	protected float commission;
	protected int slippage;	

	
	/**
	 * Fixed and final members for calculations
	 */
	protected float filled_price;
	protected float closed_price;
	protected float pnl;
	protected float req_margin;

	
	public Order(Account account, Calculator calculator) 
											throws OrderExc {
		try {
			this.account = account;
			this.calculator = calculator;
			
		} catch (Exception e) {
			String error_msg = "%s: Error making Order\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new OrderExc();
		}
		
//		Initialize the members
		this.order_id = 0;
		this.log_rows = new ArrayList<>();

		this.direction = OrderDirectionE.UNSET;
		this.type = OrderTypeE.UNSET;
		this.status = OrderStatusE.OPEN;
		this.fill_status = OrderFillE.UNFILLED;
		
		this.current_candle = null;
		this.candle_count = 0;
		this.time_open = null;
		this.time_filled = null;
		this.time_closed = null;
		
		this.entry_price = 0;
		this.stop_loss = 0;
		this.take_profit = 0;
		this.contract_size = 0;
		this.commission = 0;
		this.slippage = 0;	
		
		this.filled_price = 0;
		this.closed_price = 0;
		this.pnl = 0;
		this.req_margin = 0;
	}
	
	
	
	/**
	 * Listen to the candle
	 * @param candle Candle to listen to
	 */
	@Override
	public void viewCandle(Candle candle) 
									throws OrderExc {
		
		if(this.isClose()) return;
		
//		Save the necessary information
		this.current_candle = candle;
		this.candle_count++;
		
		if(this.isMarketOrder()) this.viewMarketType(candle);
		
		if(this.isStopOrder() || this.isLimitOrder())
			this.viewStopLimitTypes(candle);
		
	}
	
	
	/**
	 * Get the LogHeader for Order class
	 * @return header LogRow
	 */
	@Override
	public LogRow toLogHeader() {
		return(new LogRow(Constants.HEADER_ORDER));
	}

	
	/**
	 * Get the LogRow for Order class
	 * @return row LogRow
	 */
	@Override
	public LogRow toLogRow() {
		
		String ord_id = Integer.toString(this.order_id);
		String can_id = Integer.toString(this.current_candle.getId());
		String ord_type = this.type.toString();
		String ord_direction = this.direction.toString();
		String ord_status = this.status.toString();
		String ord_fill_status = this.fill_status.toString();
		String ord_candle_count = Integer.toString(this.candle_count);
		
		String ord_entry_price = String.format("%.5f", this.entry_price);
		String ord_stop_loss = String.format("%.5f", this.stop_loss);
		String ord_take_profit = String.format("%.5f", this.take_profit);
		String ord_filled_price = String.format("%.5f",this.filled_price);
		String ord_closed_price = String.format("%.5f",this.closed_price);
		
		String ord_time_open = this.time_open
									.toString().replaceAll("T", " ");
		String ord_time_filled = this.time_filled == null ?
									"" : this.time_filled
												.toString().replaceAll("T", " ");
		String ord_time_closed = this.time_closed == null ?
									"" : this.time_closed
												.toString().replaceAll("T", " ");
				
		String ord_pnl = String.format("%.2f",this.pnl);
		String ord_contract_size = String.format("%.2f",this.contract_size);
		String ord_required_margin = String.format("%.2f",this.req_margin);
		String ord_slippage = Integer.toString(this.slippage);
		String ord_commission =String.format("%.2f",this.commission);
		
		String[] content = new String[] {
								ord_id,
								can_id,
								ord_type,
								ord_direction,
								ord_status,
								ord_fill_status,
								ord_candle_count,
								
								ord_entry_price,
								ord_stop_loss,
								ord_take_profit,
								ord_filled_price,
								ord_closed_price,
								
								ord_time_open,
								ord_time_filled,
								ord_time_closed,
								
								ord_pnl,
								ord_contract_size,
								ord_required_margin,
								ord_slippage,
								ord_commission
							};
		
		return(new LogRow(content));
	}
	
	
	/**
	 * Return the LogRows for the Order
	 * @return log_rows List of LogRow
	 */
	@Override
	public List<LogRow> toLogRows() {
		return(this.log_rows);
	}

	
	public void popLastLogRow() {
		if(this.log_rows == null) return;
		if(this.log_rows.size() == 0) return;
		
		this.log_rows.remove(this.log_rows.size() - 1);
	}
	
	
	
	/**
	 * Save the current order status as a LogRow
	 */
	public void saveLogRow() {
		this.log_rows.add(this.toLogRow());
	}
	
	
	/**
	 * Helper function to listenCandle, for Stop and Limit orders
	 * The difference between the listenCandle for Market orders and other 
	 * orders is that Stop/Limit orders have to fill the Entry price 
	 * @param candle Current candle
	 * @throws OrderViewException
	 */
	private void viewStopLimitTypes(Candle candle)
								throws OrderExc {
		
		if(this.isUnfilled()) {
			
//			Fill the Entry price
			if(this.isOpen() && this.isEntryPriceSet())
				try {
					this.fillEntryPrice();
					
				} catch (OrderExc e) {
					String error_msg = 
							"%s: Error running method fill entry price\n";
					System.err.printf(error_msg, this.getClass().getName());
					e.printStackTrace();
					throw new OrderExc();
				}
		}
		
//		When the order is filled, check whether the stop loss/take profit
//		are hit in the same candle.
//		Procedure is the same as a filled order for the Market Type
		if(this.isFilled()) this.viewMarketType(candle);
	}
	
	
	/**
	 * Helper function to listenCandle, for Market orders
	 * The difference between the listenCandle for Market orders and other 
	 * orders is that Stop/Limit orders have to fill the Entry price
	 * @param candle Current candle
	 * @throws OrderViewException
	 */
	private void viewMarketType(Candle candle) 
							throws OrderExc {
		
		if(this.isFilled()) {
			
//			Close stop loss when the order is open-filled
//			If the stop loss cannot be closed, the order remains open.
//			Therefore, no 'return' from this if-statement is allowed
			if(this.isOpen() && this.isStopLossSet())  
				try {
					this.closeStopLoss();
					
				} catch (OrderExc e) {
					String error_msg = 
							"%s: Error running method close stop loss\n";
					System.err.printf(error_msg, this.getClass().getName());
					e.printStackTrace();
					throw new OrderExc();
				}
			
			
//			Close take profit when the order is open-filled
//			If the take profit cannot be closed, the order remains open.
//			Therefore, no 'return' from this if-statement is allowed
			if(this.isOpen() && this.isTakeProfitSet()) 
				try {
					this.closeTakeProfit();
					
				} catch (OrderExc e) {
					String error_msg = 
							"%s: Error running method close take profit\n";
					System.err.printf(error_msg, this.getClass().getName());
					e.printStackTrace();
					throw new OrderExc();
				}				
			
			
//			When stop loss and take profit are still open,
//			set the unrealized pnl
//			The program flow exits naturally
			if(this.isOpen()) 
				try {
					this.setUnrealizedPnl();
					
				} catch (OrderExc e) {
					String error_msg = 
							"%s: Error setting unrealized profit\n";
					System.err.printf(error_msg, this.getClass().getName());
					e.printStackTrace();
					throw new OrderExc();
				}
			
		}
	}
	
	
	/**
	 * Helper function to determine whether the take profit can be
	 * closed at the current price levels
	 * @return
	 */
	private boolean canCloseTakeProfit() {
		boolean result = this.isBuyOrder()
						? this.take_profit <= this.getBidHigh()
						: this.take_profit >= this.getAskLow();
		return(result);
	}
	
	
	/**
	 * Close at the take profit price for an open-filled order
	 * @throws OrderCloseException
	 */
	private void closeTakeProfit() throws OrderExc {
		boolean isvalid = true;
		float closed_price;

		
//		Check conditions are valid
		isvalid = isvalid && this.isOpen();
		isvalid = isvalid && this.isFilled();
		isvalid = isvalid && this.isTakeProfitSet();
		isvalid = isvalid && (this.current_candle != null);
		
		if(isvalid == false) {
			String error_msg = 
			"%s: Error invalid close conditions for take profit\n";
			System.err.printf(error_msg, this.getClass().getName());
			throw new OrderExc();			
		}
		
		
//		Price level must be valid before being closed
//		Otherwise, do nothing
		if(this.canCloseTakeProfit()) {
			closed_price = this.calcClosedPrice(this.take_profit);
			this.close(closed_price);
		}
	}
	


	
	/**
	 * Helper function to determine whether the stop loss can be
	 * closed at the current price levels
	 * @return
	 */
	private boolean canCloseStopLoss() {
		boolean result = this.isBuyOrder()
						? this.stop_loss >= this.getBidLow()
						: this.stop_loss <= this.getAskHigh();
		return(result);
	}

	
	/**
	 * Close at the stop loss price for an open-filled order
	 * @throws OrderCloseException
	 */
	private void closeStopLoss() throws OrderExc {
		boolean isvalid = true;
		float closed_price;

		
//		Check conditions are valid
		isvalid = isvalid && this.isOpen();
		isvalid = isvalid && this.isFilled();
		isvalid = isvalid && this.isStopLossSet();
		isvalid = isvalid && (this.current_candle != null);
		
		if(isvalid == false) {
			String error_msg = 
			"%s: Error invalid close conditions for stop loss\n";
			System.err.printf(error_msg, this.getClass().getName());
			throw new OrderExc();			
		}
		
		
//		Price level must be valid before being closed
//		Otherwise, do nothing
		if(this.canCloseStopLoss()) {
			closed_price = this.calcClosedPrice(this.stop_loss);
			this.close(closed_price);
		}
	}
	
	
	/**
	 * Helper function to determine whether the entry price can be
	 * filled at the current price levels
	 * @return
	 */
	private boolean canFillEntryPrice() {
		boolean result = false;
		
		if(this.isMarketOrder() || this.isStopOrder())
			result = this.isBuyOrder()
				    ? this.entry_price <= this.getAskHigh()
				    : this.entry_price >= this.getBidLow();
		
		if(this.isLimitOrder())
			result = this.isBuyOrder()
					? this.entry_price >= this.getAskLow()
					: this.entry_price <= this.getBidHigh();
					    
		return(result);
	}
	
	
	/**
	 * Fill the entry price for Stop and Limit orders
	 * @throws OrderFillException
	 */
	private void fillEntryPrice() throws OrderExc {
		boolean isvalid = true;
		float filled_price;

		
//		Check conditions are valid
		isvalid = isvalid && this.isUnfilled();
		isvalid = isvalid && (this.isMarketOrder() == false);
		isvalid = isvalid && (this.current_candle != null);
		
		if(isvalid == false) {
			String error_msg = 
			"%s: Error invalid fill conditions for entry price\n";
			System.err.printf(error_msg, this.getClass().getName());
			throw new OrderExc();			
		}
		
		
//		Price level must be valid before being filled
//		Otherwise, do nothing
		if(this.canFillEntryPrice()) {
			filled_price = this.calcFilledPrice(this.entry_price);
			this.fill(filled_price);
		}
		
	}
	
	
	
	/**
	 * Helper function to determine if the stop loss price is valid.
	 * @param sl Decimal number, Stop loss price without slippage
	 * @return
	 */
	private boolean isValidStopLoss(float sl) {
		boolean result = false;
		
		if(this.isFilled()) 
			result = this.isBuyOrder()
				   ? sl <= this.getBidClose()
				   : sl >= this.getAskClose();
		
//		Valid for Stop/Limit orders only
		if(this.isUnfilled()) {
			if(this.isEntryPriceSet() == false) return(false);

			result = this.isBuyOrder()
				   ? sl <= this.entry_price
				   : sl >= this.entry_price;
		}
		
		return(result);		
		
	}
	
	
	/**
	 * Set the Stop loss for all order types
	 * Stop loss can be set many times, regardless if the order 
	 * is filled or unfilled.
	 * @param sl Decimal number, price without slippage
	 * @throws OrderSetException
	 */
	public void setStopLoss(float sl) throws OrderExc {

		boolean isvalid = true;
		
//		Take profit argument must be valid
		isvalid = isvalid && (sl > 0);
		isvalid = isvalid && (this.isValidStopLoss(sl));
 
		
		if(isvalid)
			this.stop_loss = sl;
		
		else {
			String error_msg = 
			"%s: Error setting invalid stop loss\n";
			System.err.printf(error_msg, this.getClass().getName());
			throw new OrderExc();			
		}
		
	}

	
	
	/**
	 * Helper function to determine if the take profit price is valid.
	 * @param tp Decimal number, Take profit price without slippage
	 * @return
	 */
	private boolean isValidTakeProfit(float tp) {
		boolean result = false;
		
		if(this.isFilled()) 
			result = this.isBuyOrder()
				   ? tp >= this.getBidClose()
				   : tp <= this.getAskClose();
		
//		Valid for Stop/Limit orders only
		if(this.isUnfilled()) {
			if(this.isEntryPriceSet() == false) return(false);

			result = this.isBuyOrder()
				   ? tp >= this.entry_price
				   : tp <= this.entry_price;
		}
		
		return(result);
	}
	
	
	/**
	 * Set the Take profit for all order types
	 * Take profit can be set many times, regardless if the order 
	 * is filled or unfilled.
	 * @param tp Decimal number, price without slippage
	 * @throws OrderSetException
	 */
	public void setTakeProfit(float tp) throws OrderExc{

		boolean isvalid = true;
		
//		Take profit argument must be valid
		isvalid = isvalid && (tp > 0);
		isvalid = isvalid && (this.isValidTakeProfit(tp));
 
		
		if(isvalid)
			this.take_profit = tp;
		
		else {
			String error_msg = 
			"%s: Error setting invalid take profit\n";
			System.err.printf(error_msg, this.getClass().getName());
			throw new OrderExc();			
		}		
	}
	
	
	/**
	 * Helper function to determine if the entry price is valid
	 * for the different types of orders.
	 * Entry price can be set:
	 * Market order - Set once, on the order creation
	 * Stop/Limit order - Set many times, as long as the order is unfilled.
	 * @param entry Decimal number, Entry price without slippage
	 * @return
	 */
	private boolean isValidEntryPrice(float entry) {
		boolean result = false;
		
		
		if(this.isMarketOrder()) {
			if(this.isEntryPriceSet()) return(false);

			result = this.isBuyOrder()
				   ? entry == this.getAskClose()
				   : entry == this.getBidClose();
		}
		
		
		if(this.isStopOrder()) 
			result = this.isBuyOrder()
				   ? entry >= this.getAskClose()
				   : entry <= this.getBidClose();
		
		
		if(this.isLimitOrder()) 
			result = this.isBuyOrder()
				   ? entry <= this.getAskClose()
				   : entry >= this.getBidClose();
					
		return(result);
	}
	
	
	/**
	 * Set the Entry price for all order types
	 * @param entry Decimal number, Entry price without slippage
	 * @throws OrderSetException
	 */
	public void setEntryPrice(float entry) 
										throws OrderExc {
		boolean isvalid = true;
		
//		Entry price argument must be valid
//		Entry price must be unset
//		FOK status must be unfilled
		isvalid = isvalid && (entry > 0);
		isvalid = isvalid && (this.isUnfilled());
		isvalid = isvalid && (this.isValidEntryPrice(entry));
 
		
		if(isvalid)
			this.entry_price = entry;
		
		else {
			String error_msg = 
			"%s: Error setting invalid entry price\n";
			System.err.printf(error_msg, this.getClass().getName());
			throw new OrderExc();			
		}
	}
	

	
	/**
	 * Helper function to include Slippage into the filled price.
	 * If the order is a buy order, add the slippage to the filled price.
	 * If the order is a sell order, subtract the slippage from the filled price.
	 * @param entry_price Decimal number, filled price without slippage
	 * @return
	 */
	public float calcFilledPrice(float entry_price) {
		float slippage_dec = this.calculator.toDecimal(this.slippage);
		float result = (this.isBuyOrder())
						? entry_price + slippage_dec
						: entry_price - slippage_dec;
		
		return(result);
	}
	
	
	/**
	 * Helper function to include Slippage into the closed price.
	 * If the order is a buy order, subtract the slippage from the filled price.
	 * If the order is a sell order, add the slippage to the filled price.
	 * @param filled_price Decimal number, filled price without slippage
	 * @return
	 */
	public float calcClosedPrice(float exit_price) {
		float slippage_dec = this.calculator.toDecimal(this.slippage);
		float result = (this.isBuyOrder())
						? exit_price - slippage_dec
						: exit_price + slippage_dec;
		
		return(result);
	}
	
	
	/**
	 * Fill the order by allowing Account to approve/reject the order.
	 * The approval criteria is that the required margin is less than the
	 * free margin held in Account.
	 * If the order is rejected, it is closed and killed.
	 * Otherwise, the filled price and required margin are saved. 
	 * The order is set to open-filled
	 * @param filled_price Decimal number, must include slippage
	 * @throws OrderFillException
	 */
	public void fill(float filled_price) throws OrderExc {
		boolean approved = false;
		float req_margin = 0;
		
//		Do nothing if the order is closed or killed already
		if(this.isClose()) return;
		
//		Account must approve the order
		try {
			approved = this.approveOrder(filled_price);
			
		} catch (OrderExc e) {
			String error_msg = "%s: Error approving new order.\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new OrderExc();
		}			
		
		
//		Kill the order if the order is rejected
//		When the order is rejected, change the status to "Close-Unfilled"
		if(approved == false) { 
			this.kill();
			return;
		}
		
		
//		Calculate the margin
//		Set the members, especially filled price, required margin
//		and the status to Open-Filled
		try {
			req_margin = this.calcRequiredMargin(filled_price);
			
			this.setFilledPrice(filled_price);
			this.setRequiredMargin(req_margin);
			
			this.setFilled();
			this.setTimeFilled();
	
		} catch (OrderExc e) {
			String error_msg = 
			"%s: Error setting members for filled Order.\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new OrderExc();
		}		
		
	}
	
	/**
	 * Kill an order (of types Stop or Limit) by
	 * 1. Setting order status to close
	 * 2. Setting FOK status to killed
	 */
	public void kill() {
		
		if(this.isOpen() && this.isUnfilled()) 
			try {
				this.setClose();
				this.setTimeClosed();
				
				this.popLastLogRow();
				this.saveLogRow();
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error killing open-unfilled order\n";
				e.printStackTrace();
				System.err.printf(error_msg, this.getClass().getName());
			}
	}
	
	/**
	 * Close the current order at the current Ask/Bid close price
	 * @throws OrderCloseException
	 */
	public void close() throws OrderExc {
		float closed_price;
		
		
		if(this.current_candle == null) {
			String error_msg = "%s: Current candle is null";
			System.err.printf(error_msg, this.getClass().getName());
			throw new OrderExc();
		}
		
//		Calculate the closed price
//		1. Buy Order, perform Sell close: 
//		Closed price = (Bid close - slippage)
//		2. Sell Order, perform Buy close: 
//		Closed price = (Ask close + slippage)
		
		closed_price = (this.isBuyOrder())
						? this.getBidClose()
						: this.getAskClose();
		
		closed_price = this.calcClosedPrice(closed_price);
		
		this.close(closed_price);

	}
	
	
	/**
	 * Helper function to close the order at the stated closed price
	 * 1. Set the close price
	 * 2. Set the realized Pnl
	 * 3. Set the status to Close
	 * 
	 * @param closed_price Decimal number, include slippage
	 * @throws OrderCloseException
	 */
	private void close(float closed_price) throws OrderExc {
		boolean isvalid = true;
		
//		Perform nothing if the order is closed already
//		If the order is unapproved/open and unfilled,
//		kill the order
		if(this.isClose()) return;
		
		if(this.isOpen() && this.isUnfilled()) {
			this.kill();
			return;
		}
		
//		Check that the order is open-filled
		isvalid = isvalid && (this.isOpen());
		isvalid = isvalid && (this.isFilled());
		isvalid = isvalid && (this.isDirectionUnset() == false);
		isvalid = isvalid && (closed_price > 0);

		if(isvalid == false) {
			String error_msg = "%s: Members are wrongly set\n";
			System.err.printf(error_msg, this.getClass().getName());
			throw new OrderExc();	
		}
		
//		Set the Pnl
//		Set the closed price
//		Update Account with the realized Pnl and the margin information
//		Set the order status to close and record the close time
//		Replace the last Log Row, with the closed Log Row
		try {
			this.setClosedPrice(closed_price);
			this.setRealizedPnl();
			
			this.account.recordRealizedPnl(this.pnl);
			
			this.setClose();
			this.setTimeClosed();
			
			this.popLastLogRow();
			this.saveLogRow();
			
		} catch (OrderExc e) {
			String error_msg = 
			"%s: Error setting closed price, realized Pnl, and status\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());			
			throw new OrderExc();			
		}
		
	}
	

	/**
	 * Calculate the required margin, for Account to approve the new Order
	 * @param fill Proposed filled price
	 * @return 
	 * @throws OrderResultException
	 */
	public float calcRequiredMargin(float fill) 
									throws OrderExc {
		boolean isvalid = true;
		float result = 0;
		
//		Check that the settings are valid
		isvalid = isvalid && (fill > 0);
		isvalid = isvalid && (this.isContractSizeSet());
		isvalid = isvalid && (this.isDirectionUnset() == false);

		if(isvalid == false) {
			String error_msg = "%s: Members are wrongly set\n";
			String error_msg2 = 
			"Fill price: %.5f, size: %.2f, direction: %s\n";
			System.err.printf(error_msg, this.getClass().getName());
			System.err.printf(error_msg2,  
					fill, this.contract_size, this.direction);
			throw new OrderExc();			
		}
		
		
//		Calculate the required margin
		try {
			result = this.calculator.calcRequiredMargin
						(fill, fill, this.contract_size);
			result = this.calculator.round(result, 2);
			
		} catch (CalculatorExc e) {
			String error_msg = "%s: Error calculating margin\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new OrderExc();
		}
		
		return(result);
	}
	
	/**
	 * Setters
	 */

	
	/**
	 * Set the Order ID only if it is unset.
	 * @param order_id Order ID to be set
	 * @throws OrderSetException
	 */
	public void setOrderID(int order_id) 
									throws OrderExc {

		if(this.isOrderIDSet() == false && order_id > 0)
			this.order_id = order_id;
		
		else {
			String error_msg = "%s: Order ID has been set before\n";
			System.err.printf(error_msg, this.getClass().getName());
			throw new OrderExc();
		}
	}

	
	/**
	 * Set the open time once, when the open time has not been set
	 * before and the current candle is null
	 * @throws OrderSetException
	 */
	public void setTimeOpen() throws OrderExc {
		
		if(this.isTimeOpenSet() == false && 
								this.current_candle != null)
			this.time_open = this.current_candle.getTime();
		
		else {
			String error_msg = "%s: Error setting open time\n";
			System.err.printf(error_msg, this.getClass().getName());
			throw new OrderExc();
		}
	}
	
	
	/**
	 * Set the filled time once, when the filled time has not been set
	 * before and the current candle is null
	 * @throws OrderSetException
	 */
	public void setTimeFilled() throws OrderExc {
		
		if(this.isTimeFilledSet() == false && 
								this.current_candle != null)
			this.time_filled = this.current_candle.getTime();
		
		else {
			String error_msg = "%s: Error setting filled time\n";
			System.err.printf(error_msg, this.getClass().getName());
			throw new OrderExc();
		}
	}
	
	
	/**
	 * Set the closed time once, when the closed time has not been set
	 * before and the current candle is null
	 * @throws OrderSetException
	 */
	public void setTimeClosed() throws OrderExc {
		
		if(this.isTimeClosedSet() == false && 
								this.current_candle != null)
			this.time_closed = this.current_candle.getTime();
		
		else {
			String error_msg = "%s: Error setting closed time\n";
			System.err.printf(error_msg, this.getClass().getName());
			throw new OrderExc();
		}
	}
	
	
	/**
	 * Set the Order direction only if it is unset.
	 * @param direction Direction of the order
	 * @throws OrderSetException
	 */
	public void setDirection(OrderDirectionE direction) 
									throws OrderExc {
		
		if(this.isDirectionUnset()
			&& (direction != OrderDirectionE.UNSET))
			this.direction = direction;
		
		else {
			String error_msg = "%s: Order direction has been set before\n";
			System.err.printf(error_msg, this.getClass().getName());
			throw new OrderExc();
		}
	}

	
	/**
	 * Set the Order type only if it is unset
	 * @param type Order type (Market, Stop or Limit)
	 * @throws OrderSetException
	 */
	public void setType(OrderTypeE type) 
									throws OrderExc {
		
		if(this.isTypeUnset() 
				&& (type != OrderTypeE.UNSET))
			this.type = type;
		
		else {
			String error_msg = "%s: Order type has been set before\n";
			System.err.printf(error_msg, this.getClass().getName());
			throw new OrderExc();
		}
	}

	
	/**
	 * Set the Order status as Close .
	 * An open order can only be set to 'close'.
	 * A close order cannot be set anymore.
	 * @param status Status of the Order
	 * @throws OrderSetException
	 */
	public void setStatus(OrderStatusE status) 
									throws OrderExc {
		
		if(this.isOpen() 
				&& (status == OrderStatusE.CLOSE)) 
			this.status = status;
 	
		else {
			String error_msg = 
				"%s: Order status 'close' cannot be changed " +
				"and 'open' cannot be changed to 'unapproved'\n";
			System.err.printf(error_msg, this.getClass().getName());
			throw new OrderExc();				
		}
	}

	
	/**
	 * Fill status can be set once, from unfilled to filled only
	 * @param fill_status Fill Status
	 * @throws OrderSetException
	 */
	public void setFillStatus(OrderFillE fill_status) 
									throws OrderExc {
		
		if(this.isUnfilled() 
				&& (fill_status != OrderFillE.UNFILLED))
			this.fill_status = fill_status;
 		
		else {
			String error_msg = 
			"%s: FOK status 'unfilled' can change to 'filled' only\n";
			System.err.printf(error_msg, this.getClass().getName());
			throw new OrderExc();				
		}
	}

	
	
	/**
	 * Set the contract size once, for orders that are unfilled and
	 * where the contract size is valid
	 * @param contract_size Decimal number, greater than 0
	 * @throws OrderSetException
	 */
	public void setContractSize(float contract_size) 
									throws OrderExc {
		boolean isvalid;
		isvalid = true && (contract_size > 0);
		isvalid = isvalid && (this.isContractSizeSet() == false);
		isvalid = isvalid && (this.isUnfilled());
		
		if(isvalid == true)
			this.contract_size = contract_size;
		
		else {
			String error_msg = 
				"%s: Unable to set contract size\n";
			System.err.printf(error_msg, this.getClass().getName());
			throw new OrderExc();				
		}
			
	}

	
	/**
	 * Set the commission once, for orders that are unfilled and 
	 * commission must be valid
	 * @param commission Decimal number, greater than 0
	 * @throws OrderSetException
	 */
	public void setCommission(float commission) 
									throws OrderExc {
		boolean isvalid = true;
		
		isvalid = isvalid && (commission >= 0);
		isvalid = isvalid && (this.isCommissionSet() == false);
		isvalid = isvalid && (this.isUnfilled());
 		
		if(isvalid == true)
			this.commission = commission;
		
		else {
			String error_msg = 
				"%s: Unable to set commission\n";
			System.err.printf(error_msg, this.getClass().getName());
			throw new OrderExc();				
		}
	}

	
	/**
	 * Set the slippage once, for orders that are unfilled and
	 * slippage must be valid
	 * @param slippage Whole number, in points
	 * @throws OrderSetException
	 */
	public void setSlippage(int slippage) 
									throws OrderExc {
		boolean isvalid = true;
		
		isvalid = isvalid && (this.slippage >= 0);
		isvalid = isvalid && (this.isSlippageSet() == false);
		isvalid = isvalid && (this.isUnfilled());
		
		if(isvalid == true)
			this.slippage = slippage;
		
		else {
			String error_msg = 
				"%s: Unable to set slippage\n";
			System.err.printf(error_msg, this.getClass().getName());
			throw new OrderExc();				
		}
	}

	
	/**
	 * Set the current candle for the order
	 * @param current_candle Candle, non-NULL
	 * @throws OrderSetException
	 */
	public void setCurrentCandle(Candle current_candle) 
									throws OrderExc {
		if(current_candle != null)
			this.current_candle = current_candle;
		
		else {
			String error_msg = 
					"%s: Unable to set current candle\n";
				System.err.printf(error_msg, this.getClass().getName());
				throw new OrderExc();						
		}
	}

	
	/**
	 * Set the filled price once, when the fill status is unfilled and
	 * the filled price is valid.
	 * @param filled_price Decimal number, with slippage
	 * @throws OrderSetException
	 */
	public void setFilledPrice(float filled_price) 
									throws OrderExc {
		
		boolean isvalid;
		
		isvalid = true && (filled_price > 0);
		isvalid = isvalid && (this.isFilledPriceSet() == false);
		isvalid = isvalid && (this.isUnfilled());
		
		if(isvalid == true)
			this.filled_price = filled_price;
		
		else {
			String error_msg = 
				"%s: Unable to set filled price\n";
			System.err.printf(error_msg, this.getClass().getName());
			throw new OrderExc();				
		}
	}

	
	/**
	 * Set the filled price once, when the FOK status is filled and
	 * the filled price is valid.
	 * @param closed_price Decimal number, Closed price with slippage
	 * @throws OrderSetException
	 */
	public void setClosedPrice(float closed_price) 
									throws OrderExc {
		
		boolean isvalid;
		
		isvalid = true && (closed_price > 0);
		isvalid = isvalid && (this.isClosedPriceSet() == false);
		isvalid = isvalid && (this.isFilled());


		if(isvalid == true)
			this.closed_price = closed_price;
		
		else {
			String error_msg = 
				"%s: Unable to set closed price\n";
			System.err.printf(error_msg, this.getClass().getName());
			throw new OrderExc();				
		}
	}		
	

	/**
	 * Set the Realized Pnl into the Pnl member of the order.
	 * This method should be called for open-filled orders only.
	 * Closed price must be set, prior to method call.
	 * Closed price must include the slippage, prior to method call
	 * 
	 * @throws OrderSetException
	 */
	private void setRealizedPnl() throws OrderExc {
		
		if(this.isClosedPriceSet() == false) {
			String error_msg = 
					"%s: Closed price for the order is invalid\n";
			System.err.printf(error_msg, this.getClass().getName());
			throw new OrderExc();			
		}
		
		this.setPnl(this.closed_price);
	}
	
	
	/**
	 * Set the unrealized Pnl into the Pnl member of the order.
	 * This method should be called for open-filled orders only.
	 * It uses the difference between the filled price and close 
	 * price. 
	 * Close price will be the bid/ask close of the current candle.
	 * Slippage is included for the close price.
	 * 
	 * @throws OrderSetException
	 */
	private void setUnrealizedPnl() throws OrderExc {
		float closed_price;
		
		if(this.current_candle == null) {
			String error_msg = "%s: Current candle is null\n";
			System.err.printf(error_msg, this.getClass().getName());
			throw new OrderExc();			
		}
		
		
//		Calculate the current bid/ask close
//		For buy order, it is a sell close: use Bid Close
//		For sell order, it is a buy close: use Ask Close
		closed_price = this.isBuyOrder()
						? this.getBidClose()
						: this.getAskClose();
		
				
//		Include the slippage
		closed_price = this.calcClosedPrice(closed_price);
		
		
		this.setPnl(closed_price);
	}
	
	
	/**
	 * Set the Pnl for the order, by using the close number
	 * against the filled price. The closed price can be the
	 * ask/bid close of the current candle or Stop loss/Take profit
	 * close number. 
	 * @param closed_price Decimal number, bid/ask price includes slippage
	 * @throws OrderSetException
	 */
	private void setPnl(float closed_price) throws OrderExc {
		boolean isvalid = true;
		float total_distance;
		float price_ppt = 0;
		
//		Do nothing if the order is not open-filled
		if((this.isOpen() && this.isFilled()) == false)
			return;

		
//		Check if the necessary settings are available
		isvalid = isvalid && (this.isDirectionUnset() == false);
		isvalid = isvalid && (this.isFilledPriceSet());
		isvalid = isvalid && (this.isContractSizeSet());
		isvalid = isvalid && (closed_price > 0);
		
		
		if(isvalid == false) {
			String error_msg = "%s: Members are wrongly set\n";
			String error_msg2 = 
			"Direction: %s, filled price: %.5f, size: %.2f, close price: %.5f\n";
			System.err.printf(error_msg, this.getClass().getName());
			System.err.printf(error_msg2, this.direction, 
							this.filled_price, this.contract_size, 
							closed_price);
			throw new OrderExc();
		}
		
		
//		Calculate the total distance
//		Buy: Close - Filled price
//		Sell: Filled price - Close
		total_distance = this.isBuyOrder() 
					   ? closed_price - this.filled_price
					   : this.filled_price - closed_price;
		
		
//		Calculate the Pnl, where the calculation is the same as "required margin"
//		Pnl = price_per_point x total_distance x contract_size
//		Subtract the commission from the Pnl figure
		try {
			price_ppt = this.calculator
							.calcPricePerPoint(this.filled_price);

			total_distance = (float) this.calculator
										  .toPoints(total_distance);
			
			this.pnl = price_ppt * total_distance * this.contract_size;
			this.pnl -= this.commission;
			this.pnl = this.calculator.round(this.pnl, 2);
			
		} catch (CalculatorExc e) {
			String error_msg = "%s: Error using calculator for calculating Pnl\n";
			String error_msg2 = 
			"Price p/pt: %.2f, total distance: %.2f, size: %.2f\n";
			System.err.printf(error_msg, this.getClass().getName());
			System.err.printf(error_msg2, price_ppt, total_distance, 
								this.contract_size);
			throw new OrderExc();
		}
	}
	
	
	/**
	 * Set the required margin once, when the FOK status is unfilled
	 * and the required margin is valid
	 * @param req_margin Decimal number
	 * @throws OrderSetException
	 */
	public void setRequiredMargin(float req_margin) 
									throws OrderExc {

		boolean isvalid;
		
		isvalid = true && (req_margin > 0);
		isvalid = isvalid && (this.isReqMarginSet() == false);
		isvalid = isvalid && (this.isUnfilled());
		
		if(isvalid == true)
			this.req_margin = req_margin;
		
		else {
			String error_msg = 
				"%s: Unable to set required margin\n";
			System.err.printf(error_msg, this.getClass().getName());
			throw new OrderExc();				
		}		
	}

	
	/**
	 * Called by Orders class, to approve the order by 
	 * 1. Preparing the Order object
	 * 2. Sending the required margin to Account.
	 * @param fill Proposed filled price
	 * @throws OrderApproveException
	 */
	public boolean approveOrder(float fill) throws OrderExc {
		boolean isvalid = true;
		boolean result = false;
		float req_margin = 0;
		
//		Verify that the needed parameters are set, before hand
		isvalid = isvalid && (this.isContractSizeSet());
		isvalid = isvalid && (fill > 0);

		if(!isvalid) {
			String error_msg = 
					"%s: Wrong arguments to approve Order\n";
			String error_msg2 = 
			"size: %.2f, fill price: %.5f\n";
			System.err.printf(error_msg, this.getClass().getName());
			System.err.printf(error_msg2, this.contract_size, fill);
			throw new OrderExc();
		}
		
//		Calculate required margin
		try {
			req_margin = this.calcRequiredMargin(fill);
			
		} catch (OrderExc e) {
			String error_msg = 
			"%s: Error calculating margin for order approval\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new OrderExc();
		}
		
		
//		Account approves/reject order
		result = this.account.acceptNewOrder(req_margin);
		
		return(result);
	}	
	
	/**
	 * Special setters for Type, Status, and FOK status
	 */

//	For the types
	public void setTypeMarket() throws OrderExc {
		this.setType(OrderTypeE.MARKET);
	}
	
	public void setTypeStop() throws OrderExc {
		this.setType(OrderTypeE.STOP);
	}
	
	public void setTypeLimit() throws OrderExc {
		this.setType(OrderTypeE.LIMIT);
	}
	
	
//	For the status
	public void setClose() throws OrderExc {
		this.setStatus(OrderStatusE.CLOSE);
	}
	
	
//	For the FOK status
	public void setUnfilled() throws OrderExc {
		this.setFillStatus(OrderFillE.UNFILLED);
	} 
	
	public void setFilled() throws OrderExc {
		this.setFillStatus(OrderFillE.FILLED);
	} 	
	
	
	
	/**
	 * Boolean operators for non-Enum members, Direction, Type, Status
	 * , and FOK status
	 */
	
//	For the non-Enum members
	
	public boolean isOrderIDSet() {
		return(this.order_id > 0);
	}
	
	
	public boolean isTimeOpenSet() {
		return(this.time_open != null);
	}
	
	
	public boolean isTimeFilledSet() {
		return(this.time_filled != null);
	}
	
	
	public boolean isTimeClosedSet() {
		return(this.time_closed != null);
	}
	
	
	public boolean isEntryPriceSet() {
		return(this.entry_price > 0);
	}
	
	public boolean isStopLossSet() {
		return(this.stop_loss > 0);
	}
	
	public boolean isTakeProfitSet() {
		return(this.take_profit > 0);
	}
	
	public boolean isContractSizeSet() {
		return(this.contract_size > 0);
	}
	
	public boolean isSlippageSet() {
		return(this.slippage > 0);
	}
	
	public boolean isCommissionSet() {
		return(this.commission > 0);
	}
	
	public boolean isFilledPriceSet() {
		return(this.filled_price > 0);
	}
	
	public boolean isClosedPriceSet() {
		return(this.closed_price > 0);
	}
	
	public boolean isReqMarginSet() {
		return(this.req_margin > 0);
	}
	
//	For the directions
	public boolean isDirectionUnset() {
		return(this.direction == OrderDirectionE.UNSET);
	}
	
	public boolean isBuyOrder() {
		return(this.direction == OrderDirectionE.BUY);
	}
	
	public boolean isSellOrder() {
		return(this.direction == OrderDirectionE.SELL);
	}
	
	
//	For the order types
	public boolean isTypeUnset() {
		return(this.type == OrderTypeE.UNSET);
	}
	
	public boolean isMarketOrder() {
		return(this.type == OrderTypeE.MARKET);		
	}
	
	public boolean isStopOrder() {
		return(this.type == OrderTypeE.STOP);		
	}
	
	public boolean isLimitOrder() {
		return(this.type == OrderTypeE.LIMIT);		
	}
	
	
//	For the status
	public boolean isOpen() {
		return(this.status == OrderStatusE.OPEN);
	}
	
	public boolean isClose() {
		return(this.status == OrderStatusE.CLOSE);
	}
	
	
//	For the FOK status
	public boolean isFilled() {
		return(this.fill_status == OrderFillE.FILLED);
	}
	
	public boolean isUnfilled() {
		return(this.fill_status == OrderFillE.UNFILLED);
	}
	
	
	
	/**
	 * Special getters, to obtain the price information from 
	 * the current candle in the order.
	 */

	
	/**
	 * Returns the Bid high of the current candle
	 * @return
	 */
	public float getBidHigh() {
		float result = 0;
		
		if(this.current_candle == null) return(result);
		
		result = this.current_candle.getBid()[1];
		
		return(result);
	}
	
	
	/**
	 * Returns the Bid low of the current candle
	 * @return
	 */
	public float getBidLow() {
		float result = 0;
		
		if(this.current_candle == null) return(result);
		
		result = this.current_candle.getBid()[2];
		
		return(result);
	}
	
	
	/**
	 * Returns the Bid close of the current candle
	 * @return
	 */
	public float getBidClose() {
		float result = 0;
		
		if(this.current_candle == null) return(result);
		
		result = this.current_candle.getBid()[3];
		
		return(result);
	}
	
	
	/**
	 * Returns the Ask high of the current candle
	 * @return
	 */
	public float getAskHigh() {
		float result = 0;
		
		if(this.current_candle == null) return(result);
		
		result = this.current_candle.getAsk()[1];
		
		return(result);
	}
	
	
	/**
	 * Returns the Ask low of the current candle
	 * @return
	 */
	public float getAskLow() {
		float result = 0;
		
		if(this.current_candle == null) return(result);
		
		result = this.current_candle.getAsk()[2];
		
		return(result);
	}
	
	
	/**
	 * Returns the Ask close of the current candle
	 * @return
	 */
	public float getAskClose() {
		float result = 0;
		
		if(this.current_candle == null) return(result);
		
		result = this.current_candle.getBid()[3];
		result += this.current_candle.getSpread();
		
		return(result);
	}
	
	
	/**
	 * Getters
	 */
	

	public float getPnl() {
		return pnl;
	}

	public float getRequiredMargin() {
		return req_margin;
	}

	public int getOrderID() {
		return order_id;
	}


	public int getCandleCount() {
		return candle_count;
	}

	public Candle getCurrentCandle() {
		return current_candle;
	}

	public float getEntryPrice() {
		return entry_price;
	}

	public float getStopLoss() {
		return stop_loss;
	}

	public float getTakeProfit() {
		return take_profit;
	}

	public float getContractSize() {
		return contract_size;
	}

	public float getCommission() {
		return commission;
	}

	public int getSlippage() {
		return slippage;
	}

	public float getFilledPrice() {
		return filled_price;
	}

	public float getClosedPrice() {
		return closed_price;
	}



	/**
	 * String-equivalent of the Order
	 */
	@Override
	public String toString() {
		return "\nOrder [order_id=" + order_id 
				+ "\n, direction=" + direction 
				+ "\n, type=" + type 
				+ "\n, status=" + status 
				+ "\n, fill_status=" + fill_status
				+ "\n, candle_count=" + candle_count 
				+ "\n, time_open=" + time_open
				+ "\n, time_filled=" + time_filled 
				+ "\n, time_closed=" + time_closed 
				+ "\n, entry_price=" + entry_price
				+ "\n, stop_loss=" + stop_loss 
				+ "\n, take_profit=" + take_profit 
				+ "\n, contract_size=" + contract_size
				+ "\n, commission=" + commission 
				+ "\n, slippage=" + slippage 
				+ "\n, filled_price=" + filled_price
				+ "\n, closed_price=" + closed_price 
				+ "\n, pnl=" + pnl 
				+ "\n, req_margin=" + req_margin + "]\n";
	}

	
	
}
