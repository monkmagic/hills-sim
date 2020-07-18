package com.hills.sim.worker;

import java.time.LocalDateTime;
import java.util.Arrays;

public class CompactorCandle {
	private final boolean lit;
	private final LocalDateTime time;
	private final int ccandle_id;
	private final int content_id;
	private final int vol;
	private final float[] ask;
	private final float[] bid;
	private final float[] mid;
	

	/**
	 * Make CompactorCandle (Lit: alive with data)
	 * @param time LocalDateTime of the candle
	 * @param id_cid_vol int array for Compactor ID, ContentID and Volume
	 * @param ask float array for Ask OHLC
	 * @param bid float array for Bid OHLC
	 * @param mid float array for Mid OHLC
	 */
	public CompactorCandle(LocalDateTime time, 
						   int[] id_cid_vol,
						   float[] ask, 
						   float[] bid, 
						   float[] mid) {
		this.lit = true;
		this.time = time;
		
//		The order is: id, content_id, vol
		this.ccandle_id = id_cid_vol[0];
		this.content_id = id_cid_vol[1];
		this.vol = id_cid_vol[2];
		
//		The order is: O, H, L, C
		this.ask = new float[] {ask[0], ask[1], ask[2], ask[3]};
		this.bid = new float[] {bid[0], bid[1], bid[2], bid[3]};
		this.mid = new float[] {mid[0], mid[1], mid[2], mid[3]};
	}
	
	
	/**
	 * Make CompactorCandle (Unlit: Dead, with no data)
	 */
	public CompactorCandle() {
		this.lit = false;
		this.time = LocalDateTime.now();
		
		this.ccandle_id = -1;
		this.content_id = -1;
		this.vol = -1;
		
		this.ask = new float[] {-1f, -1f, -1f, -1f};
		this.bid = new float[] {-1f, -1f, -1f, -1f};
		this.mid = new float[] {-1f, -1f, -1f, -1f};		
	}
	
	
//	Getters


	public boolean isLit() {
		return lit;
	}
	
	public LocalDateTime getTime() {
		return (this.time);
	}
	
	public int getPrimaryKey() {
		return (this.ccandle_id);
	}
	
	public int getContentID() {
		return (this.content_id);
	}
	
	
	public int getVolume() {
		return (this.vol);
	}
	
	public float getAskO() {
		return (this.ask[0]);
	}
	
	public float getAskH() {
		return (this.ask[1]);
	}
	
	public float getAskL() {
		return (this.ask[2]);
	}
	
	public float getAskC() {
		return (this.ask[3]);
	}
	
	public float getBidO() {
		return (this.bid[0]);
	}
	
	public float getBidH() {
		return (this.bid[1]);
	}
	
	public float getBidL() {
		return (this.bid[2]);
	}
	
	public float getBidC() {
		return (this.bid[3]);
	}
	
	public float getMidO() {
		return (this.mid[0]);
	}
	
	public float getMidH() {
		return (this.mid[1]);
	}
	
	public float getMidL() {
		return (this.mid[2]);
	}
	
	public float getMidC() {
		return (this.mid[3]);
	}


	@Override
	public String toString() {
		return "CompactorCandle [lit=" + lit + ", time=" + time + 
				", id=" + ccandle_id + ", content_id=" + content_id + 
				", vol=" + vol + ", ask=" + Arrays.toString(ask) + 
				", bid=" + Arrays.toString(bid) + ", mid="
				+ Arrays.toString(mid) + "]\n";
	}
	
	
	/**
	 * New functions
	 */
	
	public float[] getAsk() {
		return ask;
	}


	public float[] getBid() {
		return bid;
	}	
}
