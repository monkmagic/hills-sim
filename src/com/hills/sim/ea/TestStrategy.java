package com.hills.sim.ea;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.hills.sim.ea.teststrategy.EARun;
import com.hills.sim.exceptions.CandlesHistoryExc;
import com.hills.sim.exceptions.OrderExc;
import com.hills.sim.settings.Run;
import com.hills.sim.settings.SESettings;
import com.hills.sim.stage.Candle;
import com.hills.sim.stage.CandleComparator;
import com.hills.sim.stage.CandlesHistory;
import com.hills.sim.stage.Order;
import com.hills.sim.stage.Stage;
import com.hills.sim.stage.TestOrder;

/**
 * TestStrategy is the Strategy class where tests are performed
 * @author Mark L
 *
 */
public class TestStrategy extends Strategy {

	private EARun ea_run;
	private Order order;
	private Order order2;
	private int candle_count;
	private int capacity;
	
	private CandlesHistory history;
	private Map<Integer, Runnable> test_cases;
			
	public TestStrategy(Stage stage, SESettings settings) {
		super(stage, settings);
		
		this.order = null;
		this.order2 = null;
		this.candle_count = 0;
		this.capacity = 30;
		
		
		this.test_cases = this.setTestCases();

		try {
			this.history = new CandlesHistory(this.capacity);
			
		} catch (CandlesHistoryExc e) {
			this.history = null;
			
			String error_msg = "%s: Error making Test CandlesHistory\n";
			System.err.printf(error_msg, this.getClass().getName());
			e.printStackTrace();
		}
	}
	
	
	
	private Map<Integer, Runnable> setTestCases() {
		Map<Integer, Runnable> result = new HashMap<>();
		
//		Don't run test case 50 as the TestOrder does not support logging
//		Running test case 50 will lead to null pointer exception
//		test50_MarginCall_OpenBuyStopOrder
		result.put(1, this::test1_Open_BuyMarketOrder);
		result.put(2, this::test2_Close_BuyMarketOrder);
		result.put(3, this::test3_Open_SellMarketOrder);
		result.put(4, this::test4_Close_SellMarketOrder);
		result.put(5, this::test5_Slippage);
		result.put(6, this::test6_Commission);
		result.put(7, this::test7_SellSL_BuyMarketOrder);
		result.put(8, this::test8_BuySL_SellMarketOrder);
		result.put(9, this::test9_SellTP_BuyMarketOrder);
		result.put(10, this::test10_BuyTP_SellMarketOrder);
		
		result.put(11, this::test11_OpenClose_BuyStopOrder);
		result.put(12, this::test12_OpenClose_SellStopOrder);
		result.put(13, this::test13_SellSL_BuyStopOrder);
		result.put(14, this::test14_BuySL_SellStopOrder);
		result.put(15, this::test15_SellTP_BuyStopOrder);
		result.put(16, this::test16_BuyTP_SellStopOrder);
		result.put(17, this::test17_Kill_BuyStopOrder);
		result.put(18, this::test18_Kill_SellStopOrder);
		result.put(19, this::test19_ChangeEP_BuyStopOrder);
		result.put(20, this::test20_ChangeEP_SellStopOrder);
		
		result.put(21, this::test21_ChangeSL_BuyStopOrder);
		result.put(22, this::test22_ChangeSL_SellStopOrder);
		result.put(23, this::test23_ChangeSL_UnfilledBuyStopOrder);
		result.put(24, this::test24_ChangeSL_UnfilledSellStopOrder);
		result.put(25, this::test25_ChangeTP_BuyStopOrder);
		result.put(26, this::test26_ChangeTP_SellStopOrder);
		result.put(27, this::test27_ChangeTP_UnfilledBuyStopOrder);
		result.put(28, this::test28_ChangeTP_UnfilledSellStopOrder);
		result.put(29, this::test29_OpenClose_BuyLimitOrder);
		result.put(30, this::test30_OpenClose_SellLimitOrder);
		
		result.put(31, this::test31_SellSL_BuyLimitOrder);
		result.put(32, this::test32_BuySL_SellLimitOrder);
		result.put(33, this::test33_SellTP_BuyLimitOrder);
		result.put(34, this::test34_BuyTP_SellLimitOrder);
		result.put(35, this::test35_Kill_BuyLimitOrder);
		result.put(36, this::test36_Kill_SellLimitOrder);
		result.put(37, this::test37_ChangeEP_BuyLimitOrder);
		result.put(38, this::test38_ChangeEP_SellLimitOrder);
		result.put(39, this::test39_ChangeSL_BuyLimitOrder);
		result.put(40, this::test40_ChangeSL_SellLimitOrder);
		
		result.put(41, this::test41_ChangeSL_UnfilledBuyLimitOrder);
		result.put(42, this::test42_ChangeSL_UnfilledSellLimitOrder);
		result.put(43, this::test43_ChangeTP_BuyLimitOrder);
		result.put(44, this::test44_ChangeTP_SellLimitOrder);
		result.put(45, this::test45_ChangeTP_UnfilledBuyLimitOrder);
		result.put(46, this::test46_ChangeTP_UnfilledSellLimitOrder);
		result.put(47, this::test47_Margin_MarginLvl_Equity);
		result.put(48, this::test48_Open2SellLimitOrders);
		result.put(49, this::test49_Open2BuyStopOrders);
		
		result.put(51, this::test51_printCandles);
		result.put(52, this::test52_countCandle);
		result.put(53, this::test53_filterCandles);
		result.put(54, this::test54_getAllCandles);
		result.put(55, this::test55_getNthCandle);
		result.put(56, this::test56_mapCandles);
		result.put(57, this::test57_maxMinCandles);
		result.put(58, this::test58_reduceCandles);
		result.put(59, this::test59_sortCandles);
		result.put(60, this::test60_PrintLogRow);
		
		result.put(61, this::test61_PrintEARunSettings);
		
		return(result);
	}
	
	
	/**
	 * Set the run
	 * @param run
	 */
	@Override
	public void setRun(Run run) {
		this.run = run;
		this.ea_run = (EARun) this.run;
	}
	
	
	/**
	 * Set the run as null and reset the Strategy members
	 */
	@Override
	public void reset() {
		this.run = null;
		
		this.order = null;
		this.order2 = null;
		this.candle_count = 0;
		
		this.history.reset();
	}
	
	@Override
	public void viewCandle(Candle candle) 
								throws Exception {
		
//		Increment candle count,
//		and view Candle for TestStrategy's CandlesHistory
		this.candle_count++;

		if(this.history != null)
			try {
				this.history.viewCandle(candle);
				
			} catch (CandlesHistoryExc e) {
				String error_msg = 
						"%s: Error viewing candle for Test CandlesHistory\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		
		
//		Test function
		int test_id = this.ea_run.getTestId();
		if(test_id == 50) return;
		this.test_cases.get(test_id).run();
		
	}
	
	
	/**
	 * Test 61: Test printing of EARun settings
	 */
	public void test61_PrintEARunSettings() {
		if(this.candle_count == 24) {
			String print_fmt = "Setting value: %d\n";
			System.out.printf(print_fmt, this.ea_run.getTestId());
		}
	}
	
	
	/**
	 * Test 60: Test printing of LogRow
	 */
	public void test60_PrintLogRow() {
		if(this.candle_count == 1) {
			this.order = this.stage.openBuyLimitOrder(1.45800F, 0.1F, 0, 0);;
		}
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.order.toLogRow());
		System.out.println(this.stage.getAccount().toLogRow());
		System.out.println(this.history.toLogRow());
	}
	
	
	/**
	 * Test 59: Test CandlesHistory; 
	 * Sort the candles based on the candle direction.
	 */
	public void test59_sortCandles() {
		if(this.candle_count == 24) {
			List<Candle> result = this.history.sort
					(Candle::getCandle, CandleComparator.Direction);
			
			System.out.println(result);
		}
	}
	
	
	
	/**
	 * Test 58: Test CandlesHistory; 
	 * Test the reduceInt and reduceFloat functions
	 * Test for volume and Ask-High
	 */
	public void test58_reduceCandles() {
		if(this.candle_count == 24) {
			int reduced_vol = this.history.reduceInt(Candle::getVolume);
			float reduced_askh = this.history.reduceFloat(Candle::getAskH);
			
			System.out.printf("Reduced volume: %d\n", reduced_vol);
			System.out.printf("Reduced Ask-High price: %.5f\n", reduced_askh);
		}
	}
	
	
	
	/**
	 * Test 57: Test CandlesHistory; Test the maxBy and minBy functions
	 * Test for the AskL (max) and AskC (min)
	 */
	public void test57_maxMinCandles() {
		if(this.candle_count == 24) {
			float max_askl = this.history.maxBy
									(Candle::getAskL, CandleComparator.AskL);
			
			float min_askc = this.history.minBy
									(Candle::getAskC, CandleComparator.AskC);
			
			System.out.printf("Max Ask-Low price: %.5f\n", max_askl);
			System.out.printf("Min Ask-Close price: %.5f\n", min_askc);
		}
	}
	
	
	/**
	 * Test 56: Test CandlesHistory; Test map function
	 * Map function (-1.00) to the Bid C of all Candles 
	 */
	public void test56_mapCandles() {
		if(this.candle_count == 24) {
			List<Float> result = this.history.map
									(Candle::getBidC, c -> c--);
			
			System.out.println(result);
		}
	}
	
	
	/**
	 * Test 55: Test CandlesHistory; Test getNth function;
	 * Get length of Candle 10 (the candle when candle count is 10)
	 * Note: Capacity is set at 30 elements.
	 * Test for invalid indices
	 * a. (i < 0 || i >= capacity)
	 * b. (i >= candle_count && candle_count <= capacity)
	 * c. (i <= candle_count && candle_count <= capacity)
	 */
	public void test55_getNthCandle() {
		if(this.candle_count == 24) {
			int index = 0;
			float result1 = -1F;
			float result2 = -1F;
			float result3 = -1F;
			float result4 = -1F;

//			Case a1: Index < 0
			try {
				index = -1;
				result1 = this.history.getNth(Candle::getLength, index);
				
			} catch (CandlesHistoryExc e) {
				System.out.printf(
					"1. Capacity: %d, Index: %d, Candle length: %.5f, Index < 0\n"
					, this.capacity, index, result1);
			}
			
			
//			Case a2: Index >= Capacity
			try {
				index = this.capacity;
				result2 = this.history.getNth(Candle::getLength, index);
				
			} catch (CandlesHistoryExc e) {
				System.out.printf(
				"2. Capacity: %d, Index: %d, Candle length: %.5f, Index == Capacity\n"
				, this.capacity, index, result2);
			}
			
			
//			Case b, Index >= candle_count, candle_count <= capacity
			try {
				index = 25;
				result3 = this.history.getNth(Candle::getLength, index);
				
			} catch (CandlesHistoryExc e) {
				System.out.printf(
				"3. Capacity: %d, Index: %d, Candle length: %.5f, "
				+ "Index >= candle_count and candle_count <= Capacity\n"
				, this.capacity, index, result3);
			}
			
//			Case c, Index <= candle_count, candle_count <= capacity
			try {
				index = 14;
				result4 = this.history.getNth(Candle::getLength, index);
				System.out.printf(
				"4. Capacity: %d, Index: %d, Candle length: %.5f, "
				+ "Index <= candle_count and candle_count <= Capacity\n"
				, this.capacity, index, result4);				
			} catch (CandlesHistoryExc e) {
			}
		}
		
	}
	
	
	/**
	 * Test 54: Test CandlesHistory; Test getAll function; 
	 * Get bodies of all candles
	 */
	public void test54_getAllCandles() {
		if(this.candle_count == 24) {
			List<Float> result = this.history.getAll(Candle::getBody);
			System.out.println(result);
		}
	}
	
	
	
	/**
	 * Test 53: Test CandlesHistory; Test filter function
	 * Filter Candles where the candle volume > 100
	 */
	public void test53_filterCandles() {
		if(this.candle_count == 24) {
			Predicate<Candle> filter_cond = x -> x.getVolume() > 100;
			List<Candle> result = this.history.filter
											(Candle::getCandle, filter_cond);
			System.out.println(result);
		}
	}
	
	
	/**
	 * Test 52: Test CandlesHistory; Test countBy function
	 * Count the different types of Candles
	 */
	public void test52_countCandle() {
		if(this.candle_count == 24) {
			int bull_candles = this.history.countBy(Candle::isBull);
			int bear_candles = this.history.countBy(Candle::isBear);
			int doji_candles = this.history.countBy(Candle::isDoji);
			
			System.out.printf("Bull: %d, Bear: %d, Doji: %d\n", 
								bull_candles,
								bear_candles,
								doji_candles);
		}
	}
	
	
	/**
	 * Test 51: Print Candle and vouch
	 */
	public void test51_printCandles() {
		if(this.candle_count == 24) {
			System.out.println(this.history);
		}
		
	}
	
	
	/**
	 * Test 50: Margin calculations enabled; Test Margin Call
	 * 
	 */
	public void test50_MarginCall_OpenBuyStopOrder() {
		if(this.candle_count == 1) {
			this.order = this.stage.openBuyStopOrder(1.46200F, 5F, 0, 0);
			this.order2 = this.stage.openTestOrder();
		}
		
		if(this.candle_count == 23) {
			TestOrder test_order = (TestOrder) this.order2;
			test_order.setFilled();
			test_order.setPnl(-10000F);
		}
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
		System.out.println(this.order2);
	}
	
	
	
	/**
	 * Test 49: Margin calculations enabled; Test ApproveOrder
	 * Fill Order 1 first as the order is pushed first in OrderBook
	 */
	public void test49_Open2BuyStopOrders() {
		if(this.candle_count == 1) {
			this.order = this.stage.openBuyStopOrder(1.46200F, 5F, 0, 0);
			this.order2 = this.stage.openBuyStopOrder(1.46200F, 5F, 0, 0);
		}
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
		System.out.println(this.order2);
	}
	
	
	
	/**
	 * Test 48: Margin calculations enabled; Test ApproveOrder
	 * Decline Order 2 when the required margin > free margin
	 */
	public void test48_Open2SellLimitOrders() {
		if(this.candle_count == 1) {
			this.order = this.stage.openSellStopOrder(1.45700F, 5F, 0, 0);
		}
		
		if(this.candle_count == 15) {
			this.order2 = this.stage.openSellStopOrder(1.45700F, 5F, 0, 0);
		}
		
		if(this.candle_count == 23)
			try {
				this.order.close();
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error closing order\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
		if(this.candle_count >= 15) System.out.println(this.order2);
	}
	
	
	/**
	 * Test 47: Test Margin, Margin level, and Equity
	 */
	public void test47_Margin_MarginLvl_Equity() {
		if(this.candle_count == 1) {
			this.order = this.stage.openSellStopOrder(1.45700F, 5F, 0, 0);
		}
		
		if(this.candle_count == 23)
			try {
				this.order.close();
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error closing order\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
	}
	
	
	
	/**
	 * Test 46: Change Take Profit with an unfilled Sell Limit Order
	 */
	public void test46_ChangeTP_UnfilledSellLimitOrder() {
		if(this.candle_count == 1) {
			this.order = this.stage.openSellLimitOrder(1.46300F, 0.1F, 0, 0);
		
			try {
				this.order.setTakeProfit(1.45800F);
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error setting buy take profit\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		}
		
		if(this.candle_count == 15) {
			try {
				this.order.setTakeProfit(1.45700F);
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error setting sell take profit\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		}
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
	}
	
	
	
	/**
	 * Test 45: Change Sell Take Profit with an unfilled Buy Limit Order
	 */
	public void test45_ChangeTP_UnfilledBuyLimitOrder() {
		if(this.candle_count == 1) {
			this.order = this.stage.openBuyLimitOrder(1.45600F, 0.1F, 0, 0);
		
			try {
				this.order.setTakeProfit(1.46100F);
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error setting sell take profit\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		}
		
		if(this.candle_count == 15) {
			try {
				this.order.setTakeProfit(1.46200F);
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error setting sell take profit\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		}
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
	}
	
	
	
	/**
	 * Test 44: Change Take Profit with a Sell Limit Order
	 */
	public void test44_ChangeTP_SellLimitOrder() {
		if(this.candle_count == 1) {
			this.order = this.stage.openSellLimitOrder(1.46000F, 0.1F, 0, 0);
		
			try {
				this.order.setTakeProfit(1.45800F);
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error setting buy take profit\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		}
		
		if(this.candle_count == 3) {
			try {
				this.order.setTakeProfit(1.45700F);
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error setting sell take profit\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		}
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
	}
	
	
	/**
	 * Test 43: Change Sell Take Profit with a filled Buy Limit Order
	 */
	public void test43_ChangeTP_BuyLimitOrder() {
		if(this.candle_count == 1) {
			this.order = this.stage.openBuyLimitOrder(1.45900F, 0.1F, 0, 0);
		
			try {
				this.order.setTakeProfit(1.46100F);
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error setting sell take profit\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		}
		
		if(this.candle_count == 15) {
			try {
				this.order.setTakeProfit(1.46200F);
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error setting sell take profit\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		}
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
	}
	
	
	
	/**
	 * Test 42: Test Kill with a Sell Limit Order
	 */
	public void test42_ChangeSL_UnfilledSellLimitOrder() {
		if(this.candle_count == 1) {
			this.order = this.stage.openSellLimitOrder(1.46300F, 0.1F, 0, 0);
			
			try {
				this.order.setStopLoss(1.46400F);
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error setting sell stop loss\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		}
		
		if(this.candle_count == 5) {
			try {
				this.order.setStopLoss(1.46500F);
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error setting sell stop loss\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		}
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
	}
	
	
	
	/**
	 * Test 41: Test Change Sell Stop Loss with a Buy Limit Order
	 */
	public void test41_ChangeSL_UnfilledBuyLimitOrder() {
		if(this.candle_count == 1) {
			this.order = this.stage.openBuyLimitOrder(1.45600F, 0.1F, 0, 0);
		
			try {
				this.order.setStopLoss(1.45500F);
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error setting sell stop loss\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		}
		
		if(this.candle_count == 5) {
			try {
				this.order.setStopLoss(1.45600F);
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error setting sell stop loss\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		}
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
	}
	
	
	
	/**
	 * Test 40: Test Change Stop Loss with a Sell Limit Order
	 */
	public void test40_ChangeSL_SellLimitOrder() {
		if(this.candle_count == 1) {
			this.order = this.stage.openSellLimitOrder(1.46000F, 0.1F, 0, 0);
		
			try {
				this.order.setStopLoss(1.46200F);
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error setting buy stop loss\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		}
		
		if(this.candle_count == 5) {
			try {
				this.order.setStopLoss(1.46400F);
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error setting sell stop loss\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		}
		
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
	}
	
	
	
	/**
	 * Test 39: Test Change Sell Stop Loss with a Buy Limit Order
	 */
	public void test39_ChangeSL_BuyLimitOrder() {
		if(this.candle_count == 1) {
			this.order = this.stage.openBuyLimitOrder(1.45900F, 0.1F, 0, 0);
		
			try {
				this.order.setStopLoss(1.45600F);
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error setting sell stop loss\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		}
		
		if(this.candle_count == 5) {
			try {
				this.order.setStopLoss(1.45700F);
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error setting sell stop loss\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		}
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
	}
	
	
	/**
	 * Test 38: Test Change entry price of an unfilled Sell Limit Order
	 */
	public void test38_ChangeEP_SellLimitOrder() {
		if(this.candle_count == 1) {
			this.order = this.stage.openSellLimitOrder(1.46100F, 0.1F, 0, 0);
		}
		
		if(this.candle_count == 4) {
			try {
				this.order.setEntryPrice(1.45900F);
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error setting new entry price\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		}
		
		if(this.candle_count == 22)
			try {
				this.order.close();
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error closing order\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
	}
	
	
	/**
	 * Test 37: Test Change entry price of an unfilled Buy Limit Order
	 */
	public void test37_ChangeEP_BuyLimitOrder() {
		if(this.candle_count == 1) {
			this.order = this.stage.openBuyLimitOrder(1.45600F, 0.1F, 0, 0);
		}
		
		if(this.candle_count == 3) {
			try {
				this.order.setEntryPrice(1.45900F);
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error setting new entry price\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		}
		
		if(this.candle_count == 22)
			try {
				this.order.close();
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error closing order\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
	}
	
	
	
	/**
	 * Test 36: Test Kill with a Sell Limit Order
	 */
	public void test36_Kill_SellLimitOrder() {
		if(this.candle_count == 1) {
			this.order = this.stage.openSellLimitOrder(1.46300F, 0.1F, 0, 0);
		}
		
		if(this.candle_count == 22)
			try {
				this.order.close();
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error closing order\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
	}
	
	
	
	/**
	 * Test 35: Test Kill an unfilled Buy Limit Order
	 */
	public void test35_Kill_BuyLimitOrder() {
		if(this.candle_count == 1) {
			this.order = this.stage.openBuyLimitOrder(1.45600F, 0.1F, 0, 0);
		}
		
		if(this.candle_count == 22)
			try {
				this.order.close();
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error closing order\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
	}
	
	
	
	/**
	 * Test 34: Test Buy Take Profit with a Sell Limit Order
	 */
	public void test34_BuyTP_SellLimitOrder() {
		if(this.candle_count == 1) {
			this.order = this.stage.openSellLimitOrder(1.46000F, 0.1F, 0, 0);
		
			try {
				this.order.setTakeProfit(1.45800F);
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error setting buy take profit\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		}
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
	}
	
	
	
	/**
	 * Test 33: Test Sell Take Profit with a Buy Limit Order
	 */
	public void test33_SellTP_BuyLimitOrder() {
		if(this.candle_count == 1) {
			this.order = this.stage.openBuyLimitOrder(1.45900F, 0.1F, 0, 0);
		
			try {
				this.order.setTakeProfit(1.46200F);
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error setting sell take profit\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		}
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
	}
	
	
	/**
	 * Test 32: Test Buy Stop Loss with a Sell Limit Order
	 */
	public void test32_BuySL_SellLimitOrder() {
		if(this.candle_count == 1) {
			this.order = this.stage.openSellLimitOrder(1.46000F, 0.1F, 0, 0);
		
			try {
				this.order.setStopLoss(1.46200F);
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error setting buy stop loss\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		}
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
	}
	
	
	
	/**
	 * Test 31: Test Sell Stop Loss with a Buy Limit Order
	 */
	public void test31_SellSL_BuyLimitOrder() {
		if(this.candle_count == 1) {
			this.order = this.stage.openBuyLimitOrder(1.45900F, 0.1F, 0, 0);
		
			try {
				this.order.setStopLoss(1.45700F);
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error setting sell stop loss\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		}
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
	}
	
	
	/**
	 * Test 30: Test Open/Close with a Sell Limit Order
	 */
	public void test30_OpenClose_SellLimitOrder() {
		if(this.candle_count == 1) {
			this.order = this.stage.openSellLimitOrder(1.46000F, 0.1F, 0, 0);
		}
		
		if(this.candle_count == 22)
			try {
				this.order.close();
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error closing order\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
	}
	
	
	
	/**
	 * Test 29: Test Open/Close with a Buy Limit Order
	 */
	public void test29_OpenClose_BuyLimitOrder() {
		if(this.candle_count == 1) {
			this.order = this.stage.openBuyLimitOrder(1.45800F, 0.1F, 0, 0);
		}
		
		if(this.candle_count == 22)
			try {
				this.order.close();
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error closing order\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
	}
	
	
	
	/**
	 * Test 28: Change TP with an unfilled Sell Stop Order
	 */
	public void test28_ChangeTP_UnfilledSellStopOrder() {
		if(this.candle_count == 1) {
			this.order = this.stage.openSellStopOrder(1.45500F, 0.1F, 0, 0);
			try {
				this.order.setTakeProfit(1.45275F);
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error setting Buy TP\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		}
		
		
		if(this.candle_count == 3)
			try {
				this.order.setTakeProfit(1.45300F);
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error setting Buy TP\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
	}
	
	
	/**
	 * Test 27: Change take profit of an unfilled Buy Stop Order
	 */
	public void test27_ChangeTP_UnfilledBuyStopOrder() {
		if(this.candle_count == 1) {
			this.order = this.stage.openBuyStopOrder(1.46300F, 0.1F, 0, 0);
			try {
				this.order.setTakeProfit(1.46500F);
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error setting Sell Take profit\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		}
		
		if(this.candle_count == 15)
			try {
				this.order.setTakeProfit(1.46700F);
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error setting Sell Take profit\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
	}
	
	
	
	/**
	 * Test 26: Buy TP with a filled Sell Stop Order
	 */
	public void test26_ChangeTP_SellStopOrder() {
		if(this.candle_count == 1) {
			this.order = this.stage.openSellStopOrder(1.45880F, 0.1F, 0, 0);
			try {
				this.order.setTakeProfit(1.45500F);
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error setting Buy TP\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		}
		
		
		if(this.candle_count == 3)
			try {
				this.order.setTakeProfit(1.45775F);
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error setting Buy TP\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
	}
	
	
	
	/**
	 * Test 25: Change take profit of a filled Buy Stop Order
	 */
	public void test25_ChangeTP_BuyStopOrder() {
		if(this.candle_count == 1) {
			this.order = this.stage.openBuyStopOrder(1.46100F, 0.1F, 0, 0);
			try {
				this.order.setTakeProfit(1.46300F);
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error setting Sell Take profit\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		}
		
		if(this.candle_count == 15)
			try {
				this.order.setTakeProfit(1.46200F);
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error setting Sell Take profit\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
	}
	
	
	
	/**
	 * Test 24: Change Stop Loss of an unfilled Sell Stop Order
	 */
	public void test24_ChangeSL_UnfilledSellStopOrder() {
		if(this.candle_count == 1) {
			this.order = this.stage.openSellStopOrder(1.45600F, 0.1F, 0, 0);
			try {
				this.order.setStopLoss(1.46200F);
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error setting Buy Stop Loss\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		}
		
		if(this.candle_count == 15)
			try {
				this.order.setStopLoss(1.46400F);
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error setting Buy Stop Loss\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
	}
	
	
	
	/**
	 * Test 23: Change Stop Loss of an unfilled Buy Stop Order
	 */
	public void test23_ChangeSL_UnfilledBuyStopOrder() {
		if(this.candle_count == 1) {
			this.order = this.stage.openBuyStopOrder(1.46200F, 0.1F, 0, 0);
			try {
				this.order.setStopLoss(1.45000F);
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error setting Sell Stop Loss\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		}
		
		if(this.candle_count == 15)
			try {
				this.order.setStopLoss(1.45800F);
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error setting Sell Stop Loss\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
	}
	
	
	/**
	 * Test 22: Change Stop Loss of a filled Sell Stop Order
	 */
	public void test22_ChangeSL_SellStopOrder() {
		if(this.candle_count == 1) {
			this.order = this.stage.openSellStopOrder(1.45700F, 0.1F, 0, 0);
			try {
				this.order.setStopLoss(1.46200F);
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error setting Buy Stop Loss\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		}
		
		if(this.candle_count == 15)
			try {
				this.order.setStopLoss(1.46400F);
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error setting Buy Stop Loss\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
	}
	
	
	
	/**
	 * Test 21: Change Stop Loss of a filled Buy Stop Order
	 */
	public void test21_ChangeSL_BuyStopOrder() {
		if(this.candle_count == 1) {
			this.order = this.stage.openBuyStopOrder(1.46100F, 0.1F, 0, 0);
			try {
				this.order.setStopLoss(1.45000F);
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error setting Sell Stop Loss\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		}
		
		if(this.candle_count == 15)
			try {
				this.order.setStopLoss(1.45800F);
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error setting Sell Stop Loss\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
	}
	
	
	/**
	 * Test 20: Change EP with a Sell Stop Order
	 */
	public void test20_ChangeEP_SellStopOrder() {
		if(this.candle_count == 1) {
			this.order = this.stage.openSellStopOrder(1.45600F, 0.1F, 0, 0);
		}
		
		if(this.candle_count == 15)
			try {
				this.order.setEntryPrice(1.45500F);
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error changing entry price\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
	}
	

	/**
	 * Test 19: Change EP with a Buy Stop Order
	 */
	public void test19_ChangeEP_BuyStopOrder() {
		if(this.candle_count == 1) {
			this.order = this.stage.openBuyStopOrder(1.46200F, 0.1F, 0, 0);
		}
		
		if(this.candle_count == 15)
			try {
				this.order.setEntryPrice(1.46400F);
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error changing entry price\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
	}
	
	
	
	/**
	 * Test 18: Kill with a Sell Stop Order
	 */
	public void test18_Kill_SellStopOrder() {
		if(this.candle_count == 1) {
			this.order = this.stage.openSellStopOrder(1.45600F, 0.1F, 0, 0);
		}
		
		if(this.candle_count == 21)
			try {
				this.order.close();
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error closing order\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
	}
	
	
	/**
	 * Test 17: Kill with a Buy Stop Order
	 */
	public void test17_Kill_BuyStopOrder() {
		if(this.candle_count == 1) {
			this.order = this.stage.openBuyStopOrder(1.46200F, 0.1F, 0, 0);
		}
		
		if(this.candle_count == 16)
			try {
				this.order.close();
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error closing order\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
	}
	
	
	/**
	 * Test 16: Buy TP with a Sell Stop Order
	 */
	public void test16_BuyTP_SellStopOrder() {
		if(this.candle_count == 1) {
			this.order = this.stage.openSellStopOrder(1.45880F, 0.1F, 0, 0);
			try {
				this.order.setTakeProfit(1.45775F);
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error setting Buy TP\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		}
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
	}
	
	
	/**
	 * Test 15: Sell TP with a Buy Stop Order
	 */
	public void test15_SellTP_BuyStopOrder() {
		if(this.candle_count == 1) {
			this.order = this.stage.openBuyStopOrder(1.46100F, 0.1F, 0, 0);
			try {
				this.order.setTakeProfit(1.46200F);
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error setting Sell TP\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		}
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
	}
	
	
	/**
	 * Test 14: Buy Stop Loss with a Sell Stop Order
	 */
	public void test14_BuySL_SellStopOrder() {
		if(this.candle_count == 1) {
			this.order = this.stage.openSellStopOrder(1.45700F, 0.1F, 0, 0);
			try {
				this.order.setStopLoss(1.46100F);
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error setting Sell Stop Loss\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		}
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
	}
	
	
	/**
	 * Test 13: Sell Stop Loss with a Buy Stop Order
	 */
	public void test13_SellSL_BuyStopOrder() {
		if(this.candle_count == 1) {
			this.order = this.stage.openBuyStopOrder(1.46100F, 0.1F, 0, 0);
			try {
				this.order.setStopLoss(1.45745f);
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error setting Sell Stop Loss\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		}
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
	}
	
	
	/**
	 * Test 12: Test Open/Close with a Sell Stop Order
	 */
	public void test12_OpenClose_SellStopOrder() {
		if(this.candle_count == 1) {
			this.order = this.stage.openSellStopOrder(1.45700F, 0.1F, 0, 0);
		}
		
		if(this.candle_count == 23)
			try {
				this.order.close();
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error closing order\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
	}
	
	
	/**
	 * Test 11: Test Open/Close with a Buy Stop Order
	 */
	public void test11_OpenClose_BuyStopOrder() {
		if(this.candle_count == 1) {
			this.order = this.stage.openBuyStopOrder(1.46200F, 0.1F, 0, 0);
		}
		
		if(this.candle_count == 23)
			try {
				this.order.close();
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error closing order\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
	}
	
	
	/**
	 * Test 10: Test Buy Take Profit with a Sell Market Order
	 */
	public void test10_BuyTP_SellMarketOrder() {
		if(this.candle_count == 2) {
			this.order = this.stage.openSellMarketOrder(0.1F, 0, 0);
			try {
				this.order.setTakeProfit(1.45820f);
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error setting Buy Take Profit\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		}
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
	}
	
	
	/**
	 * Test 9: Test Sell Take Profit with a Buy Market Order
	 */
	public void test9_SellTP_BuyMarketOrder() {
		if(this.candle_count == 1) {
			this.order = this.stage.openBuyMarketOrder(0.1F, 0, 0);
			try {
				this.order.setTakeProfit(1.46230f);
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error setting Sell Take Profit\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		}
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
	}
	
	
	/**
	 * Test 8: Test Buy Stop Loss with a Sell Market Order
	 */
	public void test8_BuySL_SellMarketOrder() {
		if(this.candle_count == 1) {
			this.order = this.stage.openSellMarketOrder(0.1F, 0, 0);
			try {
				this.order.setStopLoss(1.46200f);
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error setting Buy Stop Loss\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		}
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
	}
	
	
	/**
	 * Test 7: Test Sell Stop Loss with a Buy Market Order
	 */
	public void test7_SellSL_BuyMarketOrder() {
		if(this.candle_count == 1) {
			this.order = this.stage.openBuyMarketOrder(0.1F, 0, 0);
			try {
				this.order.setStopLoss(1.45743f);
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error setting Sell Stop Loss\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		}
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
	}
	
	
	/**
	 * Test 6: Test Commission using a Buy Market Order, close at Candle 15
	 */
	public void test6_Commission() {
		if(this.candle_count == 1) 
			this.order = this.stage.openBuyMarketOrder(0.1F, 0, 10);
		
		if(this.candle_count == 15)
			try {
				this.order.close();
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error closing order\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
	}
	
	
	
	/**
	 * Test 5: Test Slippage using a Buy Market Order, close at Candle 15
	 */
	public void test5_Slippage() {
		if(this.candle_count == 1) 
			this.order = this.stage.openBuyMarketOrder(0.1F, 10, 0);
		
		if(this.candle_count == 15)
			try {
				this.order.close();
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error closing order\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
	}
	
	
	/**
	 * Test 4: Test Close Sell Market Order
	 */
	public void test4_Close_SellMarketOrder() {
		if(this.candle_count == 1) 
			this.order = this.stage.openSellMarketOrder(0.1F, 0, 0);
		
		if(this.candle_count == 15)
			try {
				this.order.close();
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error closing order\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
	}
	
	
	
	/**
	 * Test 3: Test Open Sell Market Order
	 */
	public void test3_Open_SellMarketOrder() {
		if(this.candle_count == 1) 
			this.order = this.stage.openSellMarketOrder(0.1F, 0, 0);
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
	}
	
	
	/**
	 * Test 2: Test Close Buy Market Order
	 */
	public void test2_Close_BuyMarketOrder() {
		if(this.candle_count == 1) 
			this.order = this.stage.openBuyMarketOrder(0.1F, 0, 0);
		
		if(this.candle_count == 15)
			try {
				this.order.close();
				
			} catch (OrderExc e) {
				String error_msg = "%s: Error closing order\n";
				System.err.printf(error_msg, this.getClass().getName());
				e.printStackTrace();
			}
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
	}
	
	
	/**
	 * Test 1: Test Open Buy Market Order
	 */
	public void test1_Open_BuyMarketOrder() {
		if(this.candle_count == 1) 
			this.order = this.stage.openBuyMarketOrder(0.1F, 0, 0);
		
		System.out.printf("Candle %d:\n", this.candle_count);
		System.out.println(this.stage.getAccount());
		System.out.println(this.order);
	}
	
	

}
