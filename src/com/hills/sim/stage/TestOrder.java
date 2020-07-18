package com.hills.sim.stage;

import com.hills.sim.Constants.OrderFillE;
import com.hills.sim.Constants.OrderStatusE;
import com.hills.sim.exceptions.OrderExc;


/**
 * TestOrder is the Order class, to assist in testing margin calls
 * and other calculations where margin calculations are enabled.
 * The TestOrder class can be called from its native package (i.e. test)
 * @author Mark L
 *
 */
public class TestOrder extends Order {

	public TestOrder(Account account, Calculator calculator) 
										throws OrderExc {
		super(account, calculator);
	}

	
	/**
	 * Do nothing for every candle viewed. 
	 * @param candle Candle to listen to
	 */
	@Override
	public void viewCandle(Candle candle) 
									throws OrderExc {
		return;
	}
	
	
	/**
	 * Set Pnl
	 * @param pnl 
	 */
	public void setPnl(float pnl) {
		this.pnl = pnl;
	}	
	
	
	/**
	 * Set Required Margin
	 * @param req_margin
	 */
	public void setRequiredMargin(float req_margin) {
		this.req_margin = req_margin;
	}	
	
	
//	For the status
	/**
	 * Set an open status
	 */
	public void setOpen() {
		this.status = OrderStatusE.OPEN;
	}
	
	
	/**
	 * Set a closed status
	 */
	public void setClose() {
		this.status = OrderStatusE.CLOSE;
	}
	
	
	/**
	 * Set unfilled status
	 */
	public void setUnfilled() {
		this.fill_status = OrderFillE.UNFILLED;
	} 
	
	
	/**
	 * Set filled status
	 */
	public void setFilled() {
		this.fill_status = OrderFillE.FILLED;
	} 	
	
	
	/**
	 * Close the order
	 */
	public void close() {
		this.setClose();
	}
	
}
