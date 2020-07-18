package com.hills.sim.stage;

import com.hills.sim.settings.SESettings;
import com.hills.sim.settings.SymbolSettings;
import com.hills.sim.Constants;
import com.hills.sim.exceptions.AccountExc;
import com.hills.sim.exceptions.CalculatorExc;

public class Calculator {

	private final SESettings settings;
	
	/**
	 * Members, relating to the Symbol Settings
	 */
	private final float leverage;
	private final float distance;
	private final float contract_size_min;
	private final float contract_size_int;
	
	
	/**
	 * Make Calculator
	 * @param settings SimEngine settings
	 * @throws CalculatorExc
	 */
	public Calculator(SESettings settings) throws CalculatorExc {
		
		this.settings = settings;
		
//		Initialize all fields
		try {
			SymbolSettings sym_settings =
								this.settings.getSymSettings();
			
			this.leverage = sym_settings.getMarginRate();
			
			int distance_dec = sym_settings.getDistance();
			this.distance = (float) Math.pow(10, -distance_dec);
			
			this.contract_size_min = sym_settings.getContractSizeMin();
			
			this.contract_size_int = sym_settings.getContractSizeInt();
			
		} catch (Exception e) {
			String error_msg = "%s: Error making Account\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new CalculatorExc();
		}
		
//		Perform initial checks
		this.performInitialChecks();		
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
	private void performInitialChecks() throws CalculatorExc {
		
//		Check for valid leverage
		if(this.isLeverageValid() == false) {
			String error_msg = "%s: Leverage is invalid\n";
			String error_msg2 = "Leverage: %.2f, Leverage limit: %.2f\n";
			
			System.err.printf(error_msg, this.getClass().getName());
			System.err.printf(error_msg2, this.leverage, 
										  Constants.ACC_LEVERAGE_LIMIT);
			
			throw new CalculatorExc();			
		}		

//		Check for valid distance
		if(this.isDistanceValid() == false) {
			String error_msg = "%s: Distance is invalid\n";
			String error_msg2 = "Distance: %.6f\n";
			
			System.err.printf(error_msg, this.getClass().getName());
			System.err.printf(error_msg2, this.distance);
			
			throw new CalculatorExc();			
		}			
		
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
	 * Calculation method
	 * Converts a decimal number into points, while retaining the
	 * sign
	 * @param price Decimal number
	 * @return
	 */
	public int toPoints(float price) {
		int result = (int) (price / this.distance);
		return(result);
	}
	

	/**
	 * Calculation method
	 * Converts a number in points into decimal numbers, while
	 * retaining the sign
	 * @param points Whole numbers
	 * @return
	 */
	public float toDecimal(int points) {
		float result = points * this.distance;
		return(result);
	}
	
	
	/**
	 * Calculation method
	 * Calculate the required margin when total distance,
	 * price, and contract size are given.
	 * @param target Decimal number, price to fill in
	 * @param price Decimal number, ask/bid price
	 * @param contract_size Decimal number, size of contract
	 * @return
	 * @throws AccountCalculateException
	 */
	public float calcRequiredMargin(float target,
									 float price,
									 float contract_size)
								throws CalculatorExc {
		float result = 0;
		
		float price_ppt = this.calcPricePerPoint(price);
		float total_distance = this.toPoints(target);
		
//		Check that arguments > 0
		if(target <= 0 || price_ppt <= 0 || contract_size <= 0) {
			String error_msg = 
					"%s: Value of argument(s) <= 0\n";
			String error_msg2 =
			"Total distance: %.2f, Price per point: %.2f, Contract size: %.2f\n";
			System.err.printf(error_msg, this.getClass().getName());
			System.err.printf(error_msg2, 
									target, price_ppt, contract_size);
			throw new CalculatorExc();				
		}
		
		result = total_distance * price_ppt * contract_size;
		
		return(result);
	}
	
	
	/**
	 * Calculation method
	 * Calculate the contract size when margin, total distance,
	 * and current price are given.
	 * @param margin Decimal number, Margin allocated for trade
	 * @param total_distance Whole number, points that the margin is 
	 * spread across
	 * @param price Decimal number, current bid/ask price
	 * @return
	 * @throws AccountCalculateException
	 */
	public float calcContractSize(float margin, 
								   int total_distance, 
								   float price) 
								throws CalculatorExc {
		float result = 0;
		float n_intervals = 0;
		
		float price_ppt = this.calcPricePerPoint(price);
		
//		Check that arguments > 0
		if(margin <= 0 || total_distance <= 0 || price_ppt <= 0) {
			String error_msg = 
					"%s: Value of argument(s) <= 0\n";
			String error_msg2 =
			"Margin: %.2f, Total distance: %.2f, Price per point: %.2f\n";
			System.err.printf(error_msg, this.getClass().getName());
			System.err.printf(error_msg2, 
									margin, total_distance, price_ppt);
			throw new CalculatorExc();			
		}
		
		result = margin / (total_distance * price_ppt);
		
//		Round up to the minimum contract size if the amount is below
//		the minimum
		if(result < this.contract_size_min)
			result = this.contract_size_min;
		
//		Round the contract size by the nearest contract size interval
		n_intervals = result / this.contract_size_int;
		n_intervals = this.round(n_intervals, 0);
		result = n_intervals * this.contract_size_int;
		
		
		return(result);
	}
	
	
	/**
	 * Standard utility function to round numbers
	 * @param number Number to be rounded
	 * @param decimal_places Whole number, number of decimal places
	 * @return
	 */
	public float round(float number, int decimal_places) {
	      double shift = Math.pow(10, decimal_places);
	      double result = Math.round(number * shift)/ shift;		
	      return((float) result);
	}
	
	
	/**
	 * Calculation method
	 * Given a price, calculate the leveraged price per point 
	 * (distance unit).
	 * @param price Decimal number, ask/bid price
	 * @return
	 * @throws AccountCalculateException
	 */
	public float calcPricePerPoint(float price) 
								throws CalculatorExc {
		float result;
		
//		Check that arguments > 0
		if(price == 0) {
			String error_msg = "%s: Argument is 0, divisor is 0.\n";
			String error_msg2 = "Price: %.5f\n";
			System.err.printf(error_msg, this.getClass().getName());
			System.err.printf(error_msg2, price);
			throw new CalculatorExc();	
		}
		
		result = (this.distance / price) 
				* Constants.ACC_STD_LOT 
				* this.leverage;
		
		
		return(result);
	}

	
	
//	Getters
	/**
	 * Return the leverage, in decimal points
	 * @return
	 */
	public float getLeverage() {
		return leverage;
	}

	
	/**
	 * Return the distance, in decimal points
	 * @return
	 */
	public float getDistance() {
		return distance;
	}

	
	/**
	 * Get the minimum contract size allowed, in decimal points
	 * @return
	 */
	public float getContractSizeMin() {
		return contract_size_min;
	}

	
	/**
	 * Get the minimum interval for the contract, in decimal points
	 * @return
	 */
	public float getContractSizeInt() {
		return contract_size_int;
	}
	
	
	
	
	
}
