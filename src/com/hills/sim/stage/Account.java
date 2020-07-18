package com.hills.sim.stage;

import com.hills.sim.settings.AccountSettings;
import com.hills.sim.settings.Run;
import com.hills.sim.settings.SESettings;
import com.hills.sim.settings.SymbolSettings;
import com.hills.sim.Constants;
import com.hills.sim.exceptions.AccountExc;
import com.hills.sim.exceptions.OrderExc;
import com.hills.sim.logs.LogRow;
import com.hills.sim.logs.Loggable;
import com.hills.sim.worker.RunHandler;

public class Account implements CandleViewer, RunHandler, Loggable {
	
	private final SESettings settings;
	private final OrderBook orders;
	private final Calculator calculator;
	
	/**
	 * For logging purposes
	 * Prev_acc_balance is for ensuring that if there are no changes to the
	 * acc_balance, the row will not be logged.
	 */
	private Run run;
	private Candle candle;
	private int account_id;
	private float prev_acc_balance;
	
	/**
	 * Members, relating to the Accounts and Symbol Settings
	 */
	private final String acc_currency;
	private final boolean m_calc_enabled;
	private final float stop_out_level;

	private final float leverage;
	private final float distance;
	
	
	/**
	 * Members, for the calculation of margin level
	 * . Account balance is updated only when orders are closed
	 * . Free margin is used to accept new orders
	 * . Margin level determines if a stop out is called on all open-filled 
	 * 	  orders, when the user-provided stop-out level is reached
	 */
	private float acc_balance;
	private float equity;
	private float used_margin;
	private float free_margin;
	private float margin_level;
	
	private float min_balance;
	private float max_balance;
	private float drawdown;
	
	
	/**
	 * Make Account
	 * @param settings SimEngine settings
	 * @param orders List of Orders
	 * @throws AccountExc
	 */
	public Account(SESettings settings, OrderBook orders) 
								throws AccountExc {
		
		this.settings = settings;
		this.orders = orders;
		this.calculator = this.orders.getCalculator();
		
//		Initialize all fields
		try {
			AccountSettings acc_settings = 
								this.settings.getAccSettings();
			SymbolSettings sym_settings =
								this.settings.getSymSettings();
			
//			Initialize the settings
			this.acc_currency = acc_settings.getCurrency();
			this.m_calc_enabled = acc_settings.isMarginCalcEnabled();
			this.stop_out_level = 0.01f * acc_settings.getStopOutLevel();
			this.leverage = sym_settings.getMarginRate();
			
			int distance_dec = sym_settings.getDistance();
			this.distance = (float) Math.pow(10, -distance_dec);

//			Initialize the variables
			this.acc_balance = acc_settings.getBalance();
			this.equity = 0;
			this.used_margin = 0;
			this.free_margin = 0;
			this.margin_level = 0;
			this.min_balance = this.acc_balance;
			this.max_balance = this.acc_balance;
			this.drawdown = 0;
			
//			Initialize the log-related variables
			this.account_id = 1;
			this.prev_acc_balance = Float.valueOf(this.acc_balance);
			
		} catch (Exception e) {
			String error_msg = "%s: Error making Account\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new AccountExc();
		}
		
//		Perform initial checks
		this.performInitialChecks();
	}

	
	/**
	 * Perform the Margin call when margin calculations are enabled
	 * @throws OrderCloseException
	 */
	public void performMarginCall() throws OrderExc {
		if(this.m_calc_enabled == false) return;
		
		if(this.margin_level <= this.stop_out_level) {
			this.orders.closeAllOpenOrders();
			this.updateMarginInfo();
		}
	}
	

	/**
	 * View Candle for Account
	 * @param candle Current candle
	 * @throws Exception
	 */
	@Override
	public void viewCandle(Candle candle) throws AccountExc {
		this.candle = candle;
		this.prev_acc_balance = this.acc_balance;
	}
	
	/**
	 * Set the Run; Save the Run for logging
	 */
	@Override
	public void setRun(Run run) {
		this.run = run;
	}


	/**
	 * Reset account balance, free margin, and margin level for
	 * the next run.
	 */
	@Override
	public void reset() {
		this.run = null;
		this.candle = null;
		
		this.acc_balance = this.settings.getAccSettings()
										.getBalance();
		this.equity = 0;
		this.used_margin = 0;
		this.free_margin = 0;
		this.margin_level = 0;
		this.max_balance = this.acc_balance;
		this.drawdown = 0;
		
		this.prev_acc_balance = this.acc_balance;
	}
	
	
	/**
	 * Get the row for each Candle, for the Account class
	 * @return row LogRow
	 */
	@Override
	public LogRow toLogRow() {
		LogRow result = null;
		
		if(this.candle.getId() == 1) {
			result = this.prepareLogRow();
			return(result);			
		}
		
		if(Math.abs(this.prev_acc_balance - this.acc_balance) >= 0.01) {
			result = this.prepareLogRow();
			return(result);
		}
		
		return(result);
	}
	
	
	/**
	 * Prepare the LogRow, for logging
	 * @return log_row LogRow
	 */
	private LogRow prepareLogRow() {
		String acc_id = Integer.toString(this.account_id);
		String can_id = String.format("%d", this.candle.getId());
		
		String acc_balance = String.format("%.2f", this.acc_balance);
		String acc_equity = String.format("%.2f", this.equity);
		String acc_free_margin = String.format("%.2f", this.free_margin);
		String acc_used_margin = String.format("%.2f", this.used_margin);
		String acc_margin_level = String.format("%.2f", this.margin_level);
		String acc_min_balance = String.format("%.2f", this.min_balance);
		String acc_max_balance = String.format("%.2f", this.max_balance);
		String acc_drawdown = String.format("%.2f", this.drawdown);
		
		String acc_currency = this.acc_currency;
		String acc_m_calc_enabled = Boolean.toString(this.m_calc_enabled)
											.toUpperCase();
		String acc_stop_out_level = String.format("%.2f", this.stop_out_level);
		String acc_leverage = String.format("%.2f", this.leverage);
		String acc_distance = String.format("%.5f", this.distance);
		
		String run_id = Integer.toString(this.run.getId());
		String[] content = new String[] {
								acc_id,
								can_id,
								acc_balance,
								acc_equity,
								acc_free_margin,
								acc_used_margin,
								acc_margin_level,
								acc_min_balance,
								acc_max_balance,
								acc_drawdown,
								acc_currency,
								acc_m_calc_enabled,
								acc_stop_out_level,
								acc_leverage,
								acc_distance
						};
		
		LogRow result = new LogRow(content);
		result.append(run_id);
		
		this.account_id++;
		
		return (result);
	}

	
	/**
	 * Accept new order when the required margin is less than or equals to
	 * the free margin.
	 * Update the margin information before each approval, as new orders
	 * may have been placed by the EA during the same candle, as the
	 * current order.
	 * If margin calculations are disabled, all new orders are accepted
	 * @param required_margin Proposed required margin
	 * @return
	 */
	public boolean acceptNewOrder(float required_margin) {
		boolean result = false;
		
		this.updateMarginInfo();
		
//		Accept all orders when margin calculations are disabled
		if(this.m_calc_enabled == false) return(true);
		
		if(this.free_margin >= required_margin) result = true;
		
		return(result);
	}
	
	
	/**
	 * Update the:
	 * Equity (account plus unrealized Pnl)
	 * Used Margin (show how much is held as margin)
	 * Free Margin (used to decide if new orders can be made)
	 * Margin Level (used to decide whether to stop out of all 
	 * open-filled orders)
	 */
	public void updateMarginInfo() {
		int n_open_filled_orders;
		float unrealized_pnl;
		
//		Calculate used margin & equity
		n_open_filled_orders = this.orders.totalOpenFilledOrders();
		unrealized_pnl = this.orders.totalUnrealizedPnL();
		this.used_margin = this.orders.totalRequiredMargin();
		this.equity = this.acc_balance + unrealized_pnl;

//		Free margin is used to accept new orders
//		0 will be the floor for free margin
		this.free_margin = this.equity - this.used_margin;
		this.free_margin = this.free_margin < 0 ? 0 : this.free_margin;
		
		
//		Margin level determines if a stop out is called on all 
//		open-filled orders
		if(this.used_margin == 0 && n_open_filled_orders == 0)
			this.margin_level = Constants.ACC_STOP_OUT_LIMIT;
		else
			this.margin_level = this.equity / this.used_margin;
		
	}

	
	/**
	 * Record the realized PnL
	 * @param pnl Realized Pnl
	 */
	public void recordRealizedPnl(float pnl) {
		
//		Assign the prev acc balance and acc balance
		this.prev_acc_balance = this.acc_balance;
		this.acc_balance = this.calculator.round(this.acc_balance + pnl, 2);
		
//		Assign the minimum and maximum balances and drawdown
		this.min_balance = this.acc_balance < this.min_balance 
							? this.acc_balance : this.min_balance;
		this.max_balance = this.acc_balance > this.max_balance
							? this.acc_balance : this.max_balance;
		
		float dd_diff = this.calculator.round
				((this.acc_balance - this.max_balance)/this.max_balance, 4);
		this.drawdown = Math.abs(Math.min(0, dd_diff)) * 100;
	}
	
	
	/**
	 * Check the initial values are defined properly.
	 * 1. Account balance is valid
	 * 2. Account currency is within the Symbol
	 * 3. Stop out level is within the Stop Out Limit
	 * 4. Leverage is within the Leverage Limit
	 * 
	 * @throws AccountExc
	 */
	private void performInitialChecks() throws AccountExc {
		
//		Check for valid account
		if(this.isAccountValid() == false) {
			String error_msg = "%s: Account balance is invalid\n";
			String error_msg2 = "Negative acc enabled: %b, Acc balance: %f\n";
			
			System.err.printf(error_msg, this.getClass().getName());
			System.err.printf(error_msg2, this.m_calc_enabled, 
					this.acc_balance);
			
			throw new AccountExc();
		}
		
//		Check for valid currency
		if(this.isCurrencyValid() == false) {
			SymbolSettings sym_settings =
					this.settings.getSymSettings();
			
			String symbol_base = sym_settings.getBaseName();
			String symbol_quote = sym_settings.getQuoteName();
			
			String error_msg = "%s: Account currency is invalid\n";
			String error_msg2 = "Account currency: %s, Symbol name: %s_%s\n";
			
			System.err.printf(error_msg, this.getClass().getName());
			System.err.printf(error_msg2, this.acc_currency, 
										  symbol_base,
										  symbol_quote);
			
			throw new AccountExc();			
		}
		
//		Check for valid stop out level
		if(this.isStopOutLevelValid() == false) {
			String error_msg = "%s: Stop out level is invalid\n";
			String error_msg2 = "Stop out level: %.2f, Stop out limit: %.2f\n";
			
			System.err.printf(error_msg, this.getClass().getName());
			System.err.printf(error_msg2, this.stop_out_level, 
										  Constants.ACC_STOP_OUT_LIMIT);
			
			throw new AccountExc();			
		}
		
//		Check for valid leverage
		if(this.isLeverageValid() == false) {
			String error_msg = "%s: Leverage is invalid\n";
			String error_msg2 = "Leverage: %.2f, Leverage limit: %.2f\n";
			
			System.err.printf(error_msg, this.getClass().getName());
			System.err.printf(error_msg2, this.leverage, 
										  Constants.ACC_LEVERAGE_LIMIT);
			
			throw new AccountExc();			
		}		

		
//		Check for valid distance
		if(this.isDistanceValid() == false) {
			String error_msg = "%s: Distance is invalid\n";
			String error_msg2 = "Distance: %.6f\n";
			
			System.err.printf(error_msg, this.getClass().getName());
			System.err.printf(error_msg2, this.distance);
			
			throw new AccountExc();			
		}		
	}

	
	/**
	 * Check that the account balance is equals to or greater than 0.
	 * @return
	 */
	private boolean isAccountValid() {
		boolean result = false;
		
		result = (this.acc_balance >= 0) ? true : false;
		
		return(result);
	}
	
	
	/**
	 * Check that the account currency is either the symbol base or 
	 * symbol quote.
	 * @return
	 */
	private boolean isCurrencyValid() {
		boolean result = false;
		
		SymbolSettings sym_settings =
				this.settings.getSymSettings();
		
		String symbol_base = sym_settings.getBaseName();
		String symbol_quote = sym_settings.getQuoteName();
		
		if(this.acc_currency.equalsIgnoreCase(symbol_base)
			|| this.acc_currency.equalsIgnoreCase(symbol_quote))
			result = true;
		
		return(result);
	}


	/**
	 * Check that the stop out level is a positive number, that is less than
	 * or equals to the stop out limit
	 * @return
	 */
	private boolean isStopOutLevelValid() {
		boolean result = false;
		
//		Reject negative numbers
		if(this.stop_out_level <= 0) return(false);
		
//		Leverage must be within a leverage limit
		if(this.stop_out_level < Constants.ACC_STOP_OUT_LIMIT)
			result = true;
		else
			result = false;
		
		return(result);
	}	
	
	
	/**
	 * Check that the leverage is a positive number, that is less than
	 * or equals to the leverage limit
	 * @return
	 */
	private boolean isLeverageValid() {
		boolean result = false;
		
//		Reject negative numbers
		if(this.leverage <= 0) return(false);
		
//		Leverage must be within a leverage limit
		if(this.leverage < Constants.ACC_LEVERAGE_LIMIT)
			result = true;
		else
			result = false;
		
		return(result);
	}
	
	
	/**
	 * Check that the distance is a positive number, that is greater than
	 * 0.
	 * @return 
	 */
	private boolean isDistanceValid() {
		boolean result = false;
		SymbolSettings sym_settings =
				this.settings.getSymSettings();
		int distance = sym_settings.getDistance();
		
//		Reject negative numbers
		if(distance <= 0) return(false);
		else result = true;
		
		return(result);
	}


	
	/**
	 * Getters
	 */


	/**
	 * @return Account Balance
	 */
	public float getAccBalance() {
		return acc_balance;
	}


	/**
	 * @return Account Currency
	 */
	public String getAccCurrency() {
		return acc_currency;
	}


	/**
	 * @return Whether Margin Calculations are enabled
	 */
	public boolean isMarginCalcEnabled() {
		return m_calc_enabled;
	}


	/**
	 * @return Stop Out Level, Decimal places
	 */
	public float getStopOutLevel() {
		return stop_out_level;
	}


	/**
	 * @return Leverage, Decimal places
	 */
	public float getLeverage() {
		return leverage;
	}


	/**
	 * @return Distance, Decimal places
	 */
	public float getDistance() {
		return distance;
	}


	/**
	 * @return Free Margin
	 */
	public float getFreeMargin() {
		return free_margin;
	}


	/**
	 * @return Margin Level
	 */
	public float getMarginLevel() {
		return margin_level;
	}
	
	


	/**
	 * @return the equity
	 */
	public float getEquity() {
		return equity;
	}


	/**
	 * @return the used_margin
	 */
	public float getUsedMargin() {
		return used_margin;
	}


	/**
	 * To String
	 */
	@Override
	public String toString() {
		return "Account [acc_currency=" + acc_currency 
				+ "\n, m_calc_enabled=" + m_calc_enabled 
				+ "\n, stop_out_level=" + stop_out_level 
				+ "\n, leverage=" + leverage 
				+ "\n, distance=" + distance 
				+ "\n\n, acc_balance=" + acc_balance
				+ "\n, equity=" + equity 
				+ "\n, used_margin=" + used_margin 
				+ "\n, free_margin=" + free_margin
				+ "\n, margin_level=" + margin_level + "]\n";
	}

}
