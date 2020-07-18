package com.hills.sim.stage;

import java.util.Comparator;

/**
 * Aggregate class to hold all the Comparators for Candle
 * @author Mark L
 *
 */
public class CandleComparator {
	
	/**
	 * Comparator for Direction
	 */
	public static final Comparator<Candle> Direction
		= (x, y) -> Integer.compare(x.getDirection().ordinal(),
									y.getDirection().ordinal());
	
		
	/**
	 * Comparator for Time
	 */
	public static final Comparator<Candle> Time
	= Comparator.comparing(Candle::getTime);
	
	
	/**
	 * Comparator for Body
	 */
	public static final Comparator<Candle> Body
	= Comparator.comparing(Candle::getBody);
	
	
	/**
	 * Comparator for Length
	 */
	public static final Comparator<Candle> Length
	= Comparator.comparing(Candle::getLength);
	
	
	/**
	 * Comparator for Volume
	 */
	public static final Comparator<Candle> Volume
	= Comparator.comparing(Candle::getVolume);
	
	
	/**
	 * Comparator for Spread
	 */
	public static final Comparator<Candle> Spread
	= Comparator.comparing(Candle::getSpread);
	
	
	/**
	 * Comparator for AskO
	 */
	public static final Comparator<Candle> AskO
	= Comparator.comparing(Candle::getAskO);
	
	
	/**
	 * Comparator for AskH
	 */
	public static final Comparator<Candle> AskH
	= Comparator.comparing(Candle::getAskH);
	
	
	/**
	 * Comparator for AskL
	 */
	public static final Comparator<Candle> AskL
	= Comparator.comparing(Candle::getAskL);
	
	
	/**
	 * Comparator for AskC
	 */
	public static final Comparator<Candle> AskC
	= Comparator.comparing(Candle::getAskC);
	
	
	/**
	 * Comparator for BidO
	 */
	public static final Comparator<Candle> BidO
	= Comparator.comparing(Candle::getBidO);

	
	/**
	 * Comparator for BidH
	 */
	public static final Comparator<Candle> BidH
	= Comparator.comparing(Candle::getBidH);
	
	
	/**
	 * Comparator for BidL
	 */
	public static final Comparator<Candle> BidL
	= Comparator.comparing(Candle::getBidL);
	
	
	/**
	 * Comparator for BidC
	 */
	public static final Comparator<Candle> BidC
	= Comparator.comparing(Candle::getBidC);
	

}
