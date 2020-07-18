package com.hills.sim.logs;

import java.util.List;

public class LogRowsBag {
	private final boolean full;
	
	private final LogRow candle;
	private final LogRow account;
	private final List<LogRow> orders;
	private final LogRow report;
	
	public LogRowsBag(LogRow candle, LogRow account, List<LogRow> orders) {
		this.full = true;
		this.candle = candle;
		this.account = account;
		this.orders = orders;
		this.report = null;
	}
	
	
	public LogRowsBag
			(LogRow candle, LogRow account, List<LogRow> orders, LogRow report) {
		this.full = true;
		this.candle = candle;
		this.account = account;
		this.orders = orders;
		this.report = report;
	}
	
	public LogRowsBag() {
		this.full = false;
		this.candle = null;
		this.account = null;
		this.orders = null;
		this.report = null;
	}

	/**
	 * @return the candle
	 */
	public LogRow getCandle() {
		return candle;
	}

	/**
	 * @return the account
	 */
	public LogRow getAccount() {
		return account;
	}

	/**
	 * @return the orders
	 */
	public List<LogRow> getOrders() {
		return orders;
	}
	
	
	/**
	 * @return the report
	 */
	public LogRow getReport() {
		return report;
	}


	public boolean isFull() {
		return(this.full);
	}
}
