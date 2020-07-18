package com.hills.sim.stage;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.hills.sim.exceptions.CandlesHistoryExc;
import com.hills.sim.exceptions.TimeSeriesExc;
import com.hills.sim.logs.LogRow;
import com.hills.sim.logs.Loggable;
import com.hills.sim.worker.RunHandler;

public class CandlesHistory implements 
							CandleViewer, RunHandler, Loggable {

	private final int capacity;
	private final TimeSeries<Candle> candles;
	private Candle current_candle;
	
	/**
	 * Number of candles viewed
	 */
	private int n_viewed_candles;
	
	/**
	 * Make History
	 * @param capacity Maximum limit for the number of elements in
	 * History
	 * @throws CandlesHistoryExc
	 */
	public CandlesHistory(int capacity) throws CandlesHistoryExc {
		this.capacity = capacity;
		this.n_viewed_candles = 0;
		
//		When the capacity is less than 0
		if(capacity <= 0) {
			String error_msg = "%s: Error making TimeSeries in History\n";
			System.err.printf("Capacity is less than 0: %d\n", capacity);
			System.err.printf(error_msg, this.getClass().getName());
			throw new CandlesHistoryExc();			
		}
		
//		Make TimeSeries
		try {
			candles = new TimeSeries<>(this.capacity);
			
		} catch (TimeSeriesExc e) {
			String error_msg = "%s: Error making TimeSeries in History\n";
			e.printStackTrace();
			System.err.printf("Capacity: %s\n", capacity);
			System.err.printf(error_msg, this.getClass().getName());
			throw new CandlesHistoryExc();
		}
		
	}

	
	/**
	 * Increment the number of candles viewed
	 * Add the Candle into the respective TimeSeries
	 * @param candle Candle to add
	 */
	@Override
	public void viewCandle(Candle candle) throws CandlesHistoryExc {
		
		this.n_viewed_candles++;
		this.current_candle = candle;
		
//		Add the candle into the TimeSeries
		try {
			this.candles.add(candle);
			
		} catch (TimeSeriesExc e) {
			String error_msg = "%s: Error adding candle to History\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new CandlesHistoryExc();
		}
	}
	
	

	
	/**
	 * Get the LogRow for Candle class
	 * @return row LogRow
	 */
	@Override
	public LogRow toLogRow() {

		String can_id = Integer.toString(this.current_candle.getId());
		String can_time = this.current_candle.getTime()
									.toString().replaceAll("T", " ");
		String can_volume = Integer.toString(this.current_candle.getVolume());
		String can_spread = String.format("%.5f",this.current_candle.getSpread());
		String can_bido = String.format("%.5f",this.current_candle.getBidO());
		String can_bidh = String.format("%.5f",this.current_candle.getBidH());
		String can_bidl = String.format("%.5f",this.current_candle.getBidL());
		String can_bidc = String.format("%.5f",this.current_candle.getBidC());
		String can_asko = String.format("%.5f",this.current_candle.getAskO());
		String can_askh = String.format("%.5f",this.current_candle.getAskH());
		String can_askl = String.format("%.5f",this.current_candle.getAskL());
		String can_askc = String.format("%.5f",this.current_candle.getAskC());
		String can_direction = this.current_candle.getDirection().toString();
		String can_body = String.format("%.5f", this.current_candle.getBody());
		String can_length = String.format("%.5f", this.current_candle.getLength());
		
		String can_unit_distance = Integer.toString
								(this.current_candle.getUnitDistance());
		String can_unit_pip = Integer.toString
								(this.current_candle.getUnitPip());
		
		String[] content = new String[] {
								can_id,
								can_time,
								can_volume,
								can_spread,
								can_bido,
								can_bidh,
								can_bidl,
								can_bidc,
								can_asko,
								can_askh,
								can_askl,
								can_askc,
								can_direction,
								can_body,
								can_length,
								can_unit_distance,
								can_unit_pip
							};
		
		return(new LogRow(content));
	}



	/**
	 * To reset the number of candles viewed and the TimeSeries,
	 *  for the next run.
	 */
	@Override
	public void reset() {
		this.n_viewed_candles = 0;
		this.current_candle = null;
		this.candles.clear();
	}
	
	
	/**
	 * Getters
	 */
	
	/**
	 * Get the capacity (no. of elements) of the History
	 * @return Integer
	 */
	public int getCapacity() {
		return capacity;
	}

	
	/**
	 * Get the number of candles viewed for the current run
	 * @return Integer
	 */
	public int getNumViewedCandles() {
		return(this.n_viewed_candles);
	}
	
	
	/**
	 * Get all items in the CandlesHistory, by selecting the type of 
	 * Candle item using the map function.
	 * @param get_func Get item function
	 * @return result List of result
	 */
	public <T> List<T> getAll(Function<Candle, T> get_func) {
		List<T> result = this.candles.toList()
								.parallelStream()
								.map(get_func)
								.collect(Collectors.toList());
		return(result);
	}
	
	
	/**
	 * Checks that the index is a positive number and within the 
	 * boundaries of the CandlesHistory
	 * @param index positive number, non-zero
	 * @return result valid or invalid index
	 */
	private boolean isValidIndex(int index) {
		boolean result = false;
		
//		Index must be positive and within the capacity
		if(index < 0 || index >= this.capacity) return(result);
		
//		When the number of viewed candles have not exceed the capacity,
//		the effective range of the index is 0 and 
//		n_viewed_candles - 1 (inclusive)
		if(index >= this.n_viewed_candles 
				&& this.n_viewed_candles <= this.capacity)
			return(result);
		
		result = true;
		
		return(result);
	}
	
	
	/**
	 * Get the item (select using Function) from the nth candle (index).
	 * @param get_func the member to extract from CandleHistory
	 * @param index nth item
	 * @return result Candle item
	 * @throws CHistoryGetException Error getting item from CandleHistory
	 */
	public <T> T getNth(Function<Candle, T> get_func, int index) 
												throws CandlesHistoryExc {
		
		T result;
		
//		Check the index is valid
		if(this.isValidIndex(index) == false) {
			String error_msg = "%s: Index is out-of-bounds.\n";
			System.err.printf(error_msg, this.getClass().getName());
			throw new CandlesHistoryExc();
		}
		
//		Extract the item from the nth candle
		try {
			Candle candle = this.candles.get(index);
			result = get_func.apply(candle);
			
		} catch (TimeSeriesExc e) {
			String error_msg = "%s: Error getting item from nth candle.\n";
			System.err.printf(error_msg, this.getClass().getName());
			throw new CandlesHistoryExc();
		}
		
		return(result);
	}
	
	
	/**
	 * Map a function to a type of Candle item, for all candles.
	 * Return the mapped list.
	 * @param get_func Get item function
	 * @param map_func Mapping function
	 * @return result List of result
	 */
	public <T, R> List<R> map(Function<Candle, T> get_func,
								Function<T, R> map_func) {
		
		List<R> result = this.candles.toList()
										.parallelStream()
										.map(get_func)
										.map(map_func)
										.collect(Collectors.toList());
		
		return(result);
	}
	
	
	/**
	 * Filter the CandlesHistory based on the filter condition (Predicate)
	 * and select the type of Candle item to be returned, 
	 * using a map function (Function) 
	 * @param filter_cond Filter condition
	 * @param get_func Get item function
	 * @return result List of result
	 */
	public <T> List<T> filter(Function<Candle, T> get_func,
								Predicate<Candle> filter_cond) {
		
		List<T> result = this.candles.toList()
									.parallelStream()
									.filter(filter_cond)
									.map(get_func)
									.collect(Collectors.toList());
		
		return(result);
	}
	
	
	/**
	 * Reduce the Integer type of a Candle item, for all Candles in the 
	 * CandlesHistory 
	 * @param get_func Get item function
	 * @return reduction Total of the reduced value
	 */
	public int reduceInt(Function<Candle, Integer> get_func) {
		
		int result = this.candles.toList()
								.parallelStream()
								.map(get_func)
								.reduce(0, Integer::sum);
					
		return(result);
	}
	
	
	/**
	 * Reduce the Float type of a Candle item, for all Candles in the 
	 * CandlesHistory 
	 * @param get_func Get item function
	 * @return reduction Total of the reduced value
	 */
	public float reduceFloat(Function<Candle, Float> get_func) {
		
		float result = this.candles.toList()
								.parallelStream()
								.map(get_func)
								.reduce(0F, Float::sum);
					
		return(result);
	}
	
	
	/**
	 * Sort the CandlesHistory based on the CandleComparator compare function.
	 * Next, select the type of Candle item using a map function (Function) 
	 * on the filtered list.
	 * @param get_func Get item function
	 * @param cmp_func Compare function
	 * @return result List of result
	 */
	public <T> List<T> sort(Function<Candle, T> get_func,
								Comparator<Candle> cmp_func) {
		
		List<T> result = this.candles.toList()
									.parallelStream()
									.sorted(cmp_func)
									.map(get_func)
									.collect(Collectors.toList());
		
		return(result);
	}
	
	
	/**
	 * Count the number of items, which matches the count condition 
	 * (Predicate).
	 * @param count_cond Count condition
	 * @return count Number of items that meet the count condition
	 */
	public int countBy(Predicate<Candle> count_cond) {
		int result = (int) this.candles.toList()
									 .parallelStream()
									 .filter(count_cond)
									 .count();
		return(result);
	}
	
	
	
	/**
	 * Get the max value of the type of Candle item, using
	 * the CandleComparator compare function.
	 * @param get_func Get item function
	 * @param cmp_func Compare function
	 * @return result Candle item
	 */
	public <T> T maxBy(Function<Candle, T> get_func,
							Comparator<Candle> cmp_func) {
		
		Optional<Candle> result = this.candles.toList()
									.parallelStream()
									.max(cmp_func);
		
		if(result.isPresent() == false) return(null);
		
		T f_result = get_func.apply(result.get());
										
		return(f_result);
	}
	
	
	
	/**
	 * Get the min value of the type of Candle item, using
	 * the CandleComparator compare function.
	 * @param get_func Get item function
	 * @param cmp_func Compare function
	 * @return result Candle item
	 */
	public <T> T minBy(Function<Candle, T> get_func,
							Comparator<Candle> cmp_func) {
		
		Optional<Candle> result = this.candles.toList()
									.parallelStream()
									.min(cmp_func);

		if(result.isPresent() == false) return(null);
		
		T f_result = get_func.apply(result.get());
										
		return(f_result);
	}


	@Override
	public String toString() {
		return "CandlesHistory "
				+ "\n[ capacity=" + capacity 
				+ "\n, candles=" + candles 
				+ "\n, n_viewed_candles=" + n_viewed_candles + "]\n";
	}
	
	
	
}
