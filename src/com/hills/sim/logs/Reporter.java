package com.hills.sim.logs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hills.sim.Constants;
import com.hills.sim.settings.Run;
import com.hills.sim.worker.RunHandler;


public class Reporter implements RunHandler, Loggable {

	private Run run;
	
	/**
	 * Members for the logging to CSV file
	 */
	private int rep_id;
	private float total_net_profit;
	private float total_gross_profit;
	private float total_gross_loss;
	private float profit_factor;
	private int total_trades;
	private float perc_profitable;
	private int win_trades;
	private int lose_trades;
	
	private float avg_net_profit;
	private float avg_win;
	private float avg_loss;
	private float avg_win_loss_ratio;
	private float largest_win;
	private float largest_loss;
	private int max_win_streak;
	private int max_lose_streak;
	
	private float avg_bars_all;
	private float avg_bars_win;
	private float avg_bars_loss;
	private float max_drawdown;
	
	
	/**
	 * Members for calculating the average numbers of bars
	 */
	private int total_bars_all;
	private int total_bars_win;
	private int total_bars_loss;
	
	
	/**
	 * Members to calculate the win/lose streaks
	 */
	private int curr_win_streak;
	private int curr_lose_streak;
	
	
	public Reporter() {
		this.rep_id = 0;
		
		this.total_net_profit = 0;
		this.total_gross_profit = 0;
		this.total_gross_loss = 0;
		this.profit_factor = 0;
		this.total_trades = 0;
		this.perc_profitable = 0;
		this.win_trades = 0;
		this.lose_trades = 0;
		
		this.avg_net_profit = 0;
		this.avg_win = 0;
		this.avg_loss = 0;
		this.avg_win_loss_ratio = 0;
		this.largest_win = 0;
		this.largest_loss = 0;
		this.max_win_streak = 0;
		this.max_lose_streak = 0;
		
		this.avg_bars_all = 0;
		this.avg_bars_win = 0;
		this.avg_bars_loss = 0;
		this.max_drawdown = 0;
		
		this.total_bars_all = 0;
		this.total_bars_win = 0;
		this.total_bars_loss = 0;
		
		this.curr_win_streak = 0;
		this.curr_lose_streak = 0;
	}
	
	@Override
	public void setRun(Run run) {
		this.run = run;
		this.rep_id++;
	}


	/**
	 * Reset the Reporter for the next run
	 */
	@Override
	public void reset() {
		this.total_net_profit = 0;	 
		this.total_gross_profit = 0;  
		this.total_gross_loss = 0;	 
		this.profit_factor = 0;		 
		this.total_trades = 0;		 
		this.perc_profitable = 0;	 
		this.win_trades = 0;		 
		this.lose_trades = 0;		 
		
		this.avg_net_profit = 0;	 
		this.avg_win = 0;			 
		this.avg_loss = 0;			 
		this.avg_win_loss_ratio = 0; 
		this.largest_win = 0;		 
		this.largest_loss = 0;		 
		this.max_win_streak = 0;	 
		this.max_lose_streak = 0;	 
		
		this.avg_bars_all = 0;	 
		this.avg_bars_win = 0;	 
		this.avg_bars_loss = 0;	 
		this.max_drawdown = 0;	 
		
		this.total_bars_all = 0;   
		this.total_bars_win = 0;   
		this.total_bars_loss = 0;  
		
		this.curr_win_streak = 0;  
		this.curr_lose_streak = 0;  
	}
	
	
	/**
	 * Update the logs within the Reporter
	 * @param account LogRow of Account
	 * @param orders List<LogRow> of Orders
	 */
	public void updateLogs(LogRow account, List<LogRow> orders) {
		
		this.updateMaxDD(account);
		this.updateTradeData(orders);
		
	}
	
	
	/**
	 * Calculate the totals and averages for the Run
	 */
	private void calculateReport() {
		
		this.total_net_profit = this.total_gross_profit + this.total_gross_loss;
		
//		Absolute value is needed as gross loss is a negative number
		this.profit_factor = this.total_gross_loss != 0
							? this.total_gross_profit / this.total_gross_loss 
							: 0;
		this.profit_factor = Math.abs(this.profit_factor);
		
//		Typecast to Float is needed as the division is between 2 integers
		this.perc_profitable = this.total_trades != 0 
							? (float) 100 * this.win_trades / this.total_trades
							: 0;
		
		this.avg_net_profit = this.total_trades != 0
							? this.total_net_profit / this.total_trades
							: 0;
		
		this.avg_win = this.win_trades != 0
					 ? this.total_gross_profit / this.win_trades
					 : 0;
		
		this.avg_loss = this.lose_trades != 0
					  ? this.total_gross_loss / this.lose_trades
					  : 0;
		
		this.avg_win_loss_ratio = this.avg_loss != 0
								? this.avg_win / this.avg_loss
								: 0;
		this.avg_win_loss_ratio = Math.abs(this.avg_win_loss_ratio);
		
		this.avg_bars_all = this.total_trades != 0
						  ? (float) this.total_bars_all / this.total_trades
						  : 0;
		
		this.avg_bars_win = this.win_trades != 0
						  ? (float) this.total_bars_win / this.win_trades
						  : 0;
		
		this.avg_bars_loss = this.lose_trades != 0
						   ? (float) this.total_bars_loss / this.lose_trades
						   : 0;
	}
	
	
//	TODO: Function A & C
	/**
	 * Update the Trade-related data
	 * @param lrs_orders
	 */
	private void updateTradeData(List<LogRow> lrs_orders) {
		if(lrs_orders == null) return;
		if(lrs_orders.size() == 0) return;
		
		List<Map<String, String>> orders = this.getRowMaps
										(Constants.HEADER_ORDER, lrs_orders);
		
//		Increment the total number of trades
//		Only include non-null entries
		this.total_trades += orders.stream()
									.filter(x -> x != null).count();
		
		
//		Increment the win/lose trades
//		Extract the candle counts for all trades
//		Add the pnl to the total gross profit / loss
//		Save the largest win/loss
		for(Map<String, String> order : orders) {
			if(order == null) continue;
			
			float pnl = Float.parseFloat(order.get("ORD_PNL"));
			int candle_count = Integer.parseInt(order.get("ORD_CANDLE_COUNT"));
			
			this.total_bars_all += candle_count;
			
//			Win trade
			if(pnl > 0) {
				this.win_trades++;
				this.total_bars_win += candle_count;
				this.total_gross_profit += pnl;
				this.largest_win = pnl > this.largest_win
									? pnl : this.largest_win;
			}
			
//			Lose trade
			if(pnl < 0) {
				this.lose_trades++;
				this.total_bars_loss += candle_count;
				this.total_gross_loss += pnl;
				this.largest_loss = pnl < this.largest_loss
									? pnl : this.largest_loss;
			}
		}
		
		
//		Update the current win streak
//		On every increase in the win streak, update the max win streak
		for(Map<String, String> order : orders) {
			if(order == null) continue;
			
			float pnl = Float.parseFloat(order.get("ORD_PNL"));
			
//			Win trade
			if(pnl > 0) {
				this.curr_win_streak++;
				this.max_win_streak = 
						this.curr_win_streak > this.max_win_streak
						? this.curr_win_streak : this.max_win_streak;
			}
			
//			Lose trade, reset the current win streak
			if(pnl < 0) this.curr_win_streak = 0;
		}
		
		
//		Update the current lose streak
//		On every increase in the lose streak, update the max lose streak
		for(Map<String, String> order: orders) {
			if(order == null) continue;
			
			float pnl = Float.parseFloat(order.get("ORD_PNL"));
			
//			Lose trade
			if(pnl < 0) {
				this.curr_lose_streak++;
				this.max_lose_streak = 
						this.curr_lose_streak > this.max_lose_streak
						? this.curr_lose_streak : this.max_lose_streak;
			}
			
//			Win trade, reset the current lose streak
			if(pnl > 0) this.curr_lose_streak = 0;
		}
	}
	
	
	
//	TODO: Function B
	/**
	 * Update the maximum draw-down for the run
	 * @param lr_account LogRow of the Account
	 */
	private void updateMaxDD(LogRow lr_account) {
		if(lr_account == null) return;
		
		Map<String, String> account = 
						this.getRowMap(Constants.HEADER_ACCOUNT, lr_account);
		
		float curr_dd = Float.parseFloat(account.get("ACC_DRAWDOWN"));
		
		this.max_drawdown = curr_dd > this.max_drawdown
								? curr_dd : this.max_drawdown;
	}
	
	
	/**
	 * Helper function to map the LogRow into a HashMap for SQL insert statements
	 * @param header_fields Default headers of the type of log file
	 * @param log_row LogRow
	 * @return Map of <String, String>
	 */
	private Map<String, String> getRowMap(String[] header_fields, LogRow log_row) {
		if(log_row == null) return(null);
		Map<String, String> result = new HashMap<>();
		List<String> values = log_row.getValue();
		
		for(int i = 0; i < header_fields.length; i++) 
			result.put(header_fields[i], values.get(i));
		
		return(result);
	}
	
	
	
	/**
	 * Helper function to map the LogRows into a List of HashMap for SQL insert statements
	 * @param header_fields Default headers of the type of log file
	 * @param log_rows List of LogRows
	 * @return List of Map<String, String>
	 */
	private List<Map<String, String>> getRowMaps
								(String[] header_fields, List<LogRow> log_rows) {
		List<Map<String, String>> result = new ArrayList<>();
		for(LogRow log_row: log_rows)
			result.add(this.getRowMap(header_fields, log_row));
		
		return(result);
	}
	
	
	/**
	 * to LogRow
	 */
	@Override
	public LogRow toLogRow() {
		this.calculateReport();
		
		String rep_id = String.format("%d", this.rep_id);
		String total_net_profit = String.format("%.2f", this.total_net_profit);
		String total_gross_profit = String.format("%.2f", this.total_gross_profit);
		String total_gross_loss = String.format("%.2f", this.total_gross_loss);
		String profit_factor = String.format("%.2f", this.profit_factor);
		String total_trades = String.format("%d", this.total_trades);
		String perc_profitable = String.format("%.2f", this.perc_profitable);
		String win_trades = String.format("%d", this.win_trades);
		String lose_trades = String.format("%d", this.lose_trades);
		String avg_net_profit = String.format("%.2f", this.avg_net_profit);
		String avg_win = String.format("%.2f", this.avg_win);
		String avg_loss = String.format("%.2f", this.avg_loss);
		String avg_win_loss_ratio = String.format("%.2f", this.avg_win_loss_ratio);
		String largest_win = String.format("%.2f", this.largest_win);
		String largest_loss = String.format("%.2f", this.largest_loss);
		String max_win_streak = String.format("%d", this.max_win_streak);
		String max_lose_streak = String.format("%d", this.max_lose_streak);
		String avg_bars_all = String.format("%.2f", this.avg_bars_all);
		String avg_bars_win = String.format("%.2f", this.avg_bars_win);
		String avg_bars_loss = String.format("%.2f", this.avg_bars_loss);
		String max_drawdown = String.format("%.2f", this.max_drawdown);
		String run_id = String.format("%d", this.run.getId());
		
		String[] values = new String[] {
								rep_id,
								total_net_profit,
								total_gross_profit,
								total_gross_loss,
								profit_factor,
								total_trades,
								perc_profitable,
								win_trades,
								lose_trades,
								avg_net_profit,
								avg_win,
								avg_loss,
								avg_win_loss_ratio,
								largest_win,
								largest_loss,
								max_win_streak,
								max_lose_streak,
								avg_bars_all,
								avg_bars_win,
								avg_bars_loss,
								max_drawdown,
								run_id
							};
		
		return(new LogRow(values));
	}
	
}
