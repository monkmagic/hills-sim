package com.hills.sim.stage;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.OptionalDouble;
import java.util.stream.IntStream;

import com.hills.sim.Constants.CandleDirectionE;
import com.hills.sim.worker.CompactorCandle;

public class Candle implements Comparable<Candle> {

	private final CompactorCandle base_candle;
	private final int candle_id;
	private final int total_candles;
	private final int unit_pip;
	private final int unit_distance;
	
	private final LocalDateTime time;
	private final float spread;
	private final float[] bid;
	private final float[] ask;

	private final float body;
	private final float length;
	private final CandleDirectionE direction;
	
	
	/**
	 * Make Candle
	 * @param base_candle CompactorCandle
	 * @param candle_id whole number, id of the candle
	 * @param total_candles whole number, total number of candles
	 * @param pip whole number, pips in number of decimal places
	 * @param distance whole number, distance in number of decimal places
	 */
	public Candle(CompactorCandle base_candle, 
					int candle_id, 
					int total_candles,
					int pip, 
					int distance) {
		
		this.base_candle = base_candle;
		
		this.candle_id = candle_id;
		this.total_candles = total_candles;
		this.unit_pip = pip;
		this.unit_distance = distance;
		this.time = this.base_candle.getTime();
		
		this.spread = this.setSpread();
		this.bid = this.setBid();
		this.ask = this.setAsk();

		this.body = this.setBody();
		this.length = this.setLength();
		this.direction = this.setDirection();
	}
	

	/**
	 * Setters
	 */
	
	
	/**
	 * Get the spread for the candle 
	 * Calculate the spread between OHLC for Ask and Bid
	 * and choose the greatest value	  
	 * @return
	 */
	private float setSpread() {
		float result;
		
		float[] ask = this.base_candle.getAsk();
		float[] bid = this.base_candle.getBid();
		
		OptionalDouble od = IntStream.range(0, 4)
							.mapToDouble(i -> Math.abs(ask[i] - bid[i]))
							.max();
		
		result = (float) od.getAsDouble();
		result = this.round(result);
		
		return(result);
	}
	
	
	/**
	 * Set the body of the candle, based on the bid price
	 * @return body Float, positive number or 0.
	 */
	private float setBody() {
		float result = Math.abs(this.getBidC() - this.getBidO());
		result = this.round(result);
		return(result);
	}
	
	
	/**
	 * Set the length of the candle, based on the bid price
	 * @return length Float, postive number or 0.
	 */
	private float setLength() {
		float result = this.getBidH() - this.getBidL();
		result = this.round(result);
		return(result);
	}


	/**
	 * Calculate, then set the direction of the candle
	 * @return candle_direction CandleDirectionE
	 */
	private CandleDirectionE setDirection() {
		CandleDirectionE result;
		float body = this.getBidC() - this.getBidO();
		
		if(body > 0) result = CandleDirectionE.BULL;
		else if(body < 0) result = CandleDirectionE.BEAR;
		else result = CandleDirectionE.DOJI;
		
		return(result);
	}
	
	
	/**
	 * Return the Bid candle, perform rounding based on Bid CompactorCandle
	 * @return bid_candle array of float for bid prices
	 */
	private float[] setBid() {
		float[] bid = new float[] {
							   this.round(this.base_candle.getBidO()),
							   this.round(this.base_candle.getBidH()),
							   this.round(this.base_candle.getBidL()),
							   this.round(this.base_candle.getBidC()),
							 };
		
		return(bid);
	}
	
	
	/**
	 * Return the Ask candle, based on the sum of the largest spread (between
	 * Bid and Ask CompactorCandle) and Bid CompactorCandle.
	 * Perform rounding.
	 * @return ask_candle array of float for ask prices
	 */
	private float[] setAsk() {
		float[] ask = new float[] {
					   this.round(this.base_candle.getBidO() + this.spread),
					   this.round(this.base_candle.getBidH() + this.spread),
					   this.round(this.base_candle.getBidL() + this.spread),
					   this.round(this.base_candle.getBidC() + this.spread),
					 };
		
		return(ask);
	}
	

	
	/**
	 * Calculator functions
	 */
	
	
	/**
	 * Convert from decimal places to pip value
	 * @param value float, amount to be converted
	 * @return result integer, value converted to pips
	 */
	public int toPip(float value) {
		double result = (double) value * Math.pow(10d, this.unit_pip);
		return((int) result);
	}
	
	
	/**
	 * Convert from decimal places to distance, in whole number
	 * @param value Decimal number, to be converted 
	 * @return points Whole number, value converted to distance
	 */
	public int toPoints(float value) {
		double result = (double) value * Math.pow(10d, this.unit_distance);
		result = this.round((float) result, 0);
		
		return((int) result);
	}

	
	/**
	 * Round float number to the nearest unit distance
	 * @param number Value to be rounded off
	 * @return rounded_value float
	 */
	public float round(float number) {
	      double shift = Math.pow(10, this.unit_distance);
	      double result = Math.round(number * shift)/ shift;		
	      return((float) result);
	} 
	
	
	/**
	 * Round float number to the nearest decimal place
	 * @param number Value to be rounded off
	 * @param decimal_place Number of decimal place to round to
	 * @return rounded_value float
	 */
	public float round(float number, int decimal_place) {
	      double shift = Math.pow(10, decimal_place);
	      double result = Math.round(number * shift)/ shift;		
	      return((float) result);
	}
	
	/**
	 * Getters
	 */
	
	
	/**
	 * Get Candle
	 * @return candle Candle
	 */
	public Candle getCandle() {
		return(this);
	}
	
	
	/**
	 * Get Ask Open price
	 * @return ask_open price
	 */
	public float getAskO() {
		return (this.ask[0]);
	}
	
	
	/**
	 * Get Ask High price
	 * @return ask_high price
	 */
	public float getAskH() {
		return (this.ask[1]);
	}
	
	
	/**
	 * Get Ask Low price
	 * @return ask_low price
	 */
	public float getAskL() {
		return (this.ask[2]);
	}
	
	
	/**
	 * Get Ask Close price
	 * @return ask_close price
	 */
	public float getAskC() {
		return (this.ask[3]);
	}
	
	
	/**
	 * Get Bid Open price
	 * @return bid_open price
	 */
	public float getBidO() {
		return (this.bid[0]);
	}
	
	
	/**
	 * Get Bid High price
	 * @return bid_high price
	 */
	public float getBidH() {
		return (this.bid[1]);
	}
	
	
	/**
	 * Get Bid Low price
	 * @return bid_low price
	 */
	public float getBidL() {
		return (this.bid[2]);
	}
	
	
	/**
	 * Get Bid Close price
	 * @return bid_close price
	 */
	public float getBidC() {
		return (this.bid[3]);
	}
	
	/**
	 * Get time of the candle
	 * @return time LocalDateTime
	 */
	public LocalDateTime getTime() {
		return time;
	}
	
	
	/**
	 * Get direction of the candle
	 * @return direction CandleDirectionE
	 */	
	public CandleDirectionE getDirection() {
		return direction;
	}
	
	
	/**
	 * Get volume
	 * @return Volume, whole number
	 */
	public int getVolume() {
		return(this.base_candle.getVolume());
	}
	

	/**
	 * Get maximum spread between the ask and bid candle
	 * @return spread, float rounded off by unit distance
	 */
	public float getSpread() {
		return spread;
	}


	/**
	 * Get body of the candle, calculated by close price minus open price
	 * @return body, float rounded off by unit distance
	 */
	public float getBody() {
		return body;
	}


	/**
	 * Get length of the candle, calculated by high price minus low price
	 * @return length, float rounded off by unit distance
	 */
	public float getLength() {
		return length;
	}
	
	
	/**
	 * Get Ask candle OHLC
	 * @return ask Ask candles OHLC
	 */
	public float[] getAsk() {
		return(this.ask);
	}	
	
	
	/**
	 * Get Bid candle OHLC
	 * @return bid Bid candles OHLC
	 */
	public float[] getBid() {
		return(this.bid);
	}	
	
	
	/**
	 * Get the Candle ID of the candle
	 * @return candle_id whole number
	 */
	public int getId() {
		return(this.candle_id);
	}
	
	
	/**
	 * Get the total number of Candles
	 * @return total_candles whole number
	 */
	public int getTotalCandles() {
		return(this.total_candles);
	}
	
	
	/**
	 * Get number of decimal places a unit of pip is defined as
	 * @return unit_pip Integer, number of decimal places
	 */
	public int getUnitPip() {
		return unit_pip;
	}


	/**
	 * Get number of decimal places a point is defined as
	 * @return unit_distance Integer, number of decimal places
	 */
	public int getUnitDistance() {
		return unit_distance;
	}


	/**
	 * Return true when the candle is a bull candle
	 * @return is_bull boolean
	 */
	public boolean isBull() {
		return(this.direction == CandleDirectionE.BULL);
	}
	
	
	/**
	 * Return true when the candle is a bear candle
	 * @return is_bear boolean
	 */
	public boolean isBear() {
		return(this.direction == CandleDirectionE.BEAR);
	}
	
	
	/**
	 * Return true when the candle is a doji candle
	 * @return is_doji boolean
	 */
	public boolean isDoji() {
		return(this.direction == CandleDirectionE.DOJI);
	}
	
	
	
	/**
	 * Return true when the candle is lit
	 * @return is_lit boolean
	 */
	public boolean isLit() {
		return(this.base_candle.isLit());
	}
	
	
	/**
	 * Return true when the candle is the last candle
	 * @return is_last_candle boolean
	 */
	public boolean isLastCandle() {
		return(this.candle_id == this.total_candles);
	}
	
	
	@Override
	public String toString() {
			return "Candle "
					+ "\n[ id=" + candle_id
					+ "\n, total_candles=" + total_candles
					+ "\n, time=" + time 
					+ "\n, unit_pip=" + unit_pip 
					+ "\n, unit_distance=" + unit_distance
					+ "\n, direction=" + direction 
					+ "\n, spread=" + spread 
					+ "\n, body=" + body 
					+ "\n, length=" + length
					+ "\n, volume=" + this.getVolume()
					+ "\n, bid=" + Arrays.toString(bid) 
					+ "\n, ask=" + Arrays.toString(ask) 
					+ "\n]\n";
	}

	
	/**
	 * For Testing; Include the base candle for reference
	 * @return test_string
	 */
	public String toTestString() {
		return "Candle "
				+ "\n[ id=" + candle_id	
				+ "\n, total_candles=" + total_candles
				+ "\n, time=" + time 
				+ "\n, unit_pip=" + unit_pip 
				+ "\n, unit_distance=" + unit_distance
				+ "\n, direction=" + direction 
				+ "\n, spread=" + spread 
				+ "\n, body=" + body 
				+ "\n, length=" + length 
				+ "\n, volume=" + this.getVolume()
				+ "\n, bid=" + Arrays.toString(bid) 
				+ "\n, ask=" + Arrays.toString(ask) 
				+ "\n, base_candle=" + base_candle 
				+ "]\n";
	}
	
	
	/**
	 * Default method of comparing candle is via time (LocalDateTime)
	 */
	@Override
	public int compareTo(Candle o) {
		LocalDateTime o_time = o.getTime();
		return (this.time.compareTo(o_time));
	}




}
