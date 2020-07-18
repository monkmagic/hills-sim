package com.hills.sim.stage;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.hills.sim.exceptions.TimeSeriesExc;

public class TimeSeries<E> implements Iterable<E> {
	
	private int update;
	private final int capacity;
	private final ArrayDeque<E> dequeue;
	private List<E> reference_list;
	
	/**
	 * Construct the TimeSeries. Capacity is the maximum limit.
	 * @param capacity Integer, maximum limit
	 * @throws TimeSeriesExc
	 */
	public TimeSeries(int capacity) 
						throws TimeSeriesExc {
		this.update = 0;
		this.capacity = capacity;
		
//		When the capacity is less than 0
		if(capacity <= 0) {
			System.err.printf("Capacity is less than 0: %d\n", capacity);
			throw new TimeSeriesExc();			
		}
		
		try {
			this.dequeue = new ArrayDeque<>(this.capacity);
			
		} catch (Exception e) {
			String error_msg = "%s: Error making ArrayDeque in TimeSeries\n";
			e.printStackTrace();
			System.err.printf("Capacity: %s\n", capacity);
			System.err.printf(error_msg, this.getClass().getName());
			throw new TimeSeriesExc();
		}
		
	}
	
	
	/**
	 * Get a single element from the TimeSeries
	 * @param index Index within the TimeSeries
	 * @return
	 * @throws TSeriesGetException
	 */
	public E get(int index) throws TimeSeriesExc {
		E result = null;
		
//		Check that 'get' method is within bounds
		if(index < 0 || index >= this.capacity) {
			String error_msg = 
				"%s: Error as index is out of bounds of maximum capacity\n";
			System.err.printf("Max. capacity: %d, index: %d\n", 
											this.capacity, index);
			System.err.printf(error_msg, this.getClass().getName());
			throw new TimeSeriesExc();			
		}
		
//		Check that 'get' method is within bounds, of used capacity
		else if(index >= this.update && this.update <= this.capacity) {
			String error_msg = 
				"%s: Error as index is out of bounds of current capacity\n";
			System.err.printf("Current capacity: %d, index: %d\n", 
											this.update, index);
			System.err.printf(error_msg, this.getClass().getName());
			throw new TimeSeriesExc();		
		}
		
		else 
			result = this.reference_list.get(index);
		
		
		return(result);
	}
	
	
	/**
	 * Insert a new record into the TimeSeries
	 * @param element New record
	 * @throws TSeriesAddException
	 */
	public void add(E element) throws TimeSeriesExc {
		
		try {
//			Remove the last element only when the capacity is met
			if(this.update >= this.capacity) 
				this.dequeue.removeLast();
			this.dequeue.addFirst(element);
			
		} catch (Exception e) {
			String error_msg = "%s: Error adding element to TimeSeries\n";
			e.printStackTrace();
			System.err.printf("Element: %s\n", element);
			System.err.printf(error_msg, this.getClass().getName());
			throw new TimeSeriesExc();
		}
		
//		Update the counter
//		Create the reference list
		this.update++;
		this.reference_list = this.dequeue.stream()
										.collect(Collectors.toList());
	}

	
	/**
	 * Clear the TimeSeries and reset the update counter to 0.
	 */
	public void clear() {
		this.update = 0;
		this.dequeue.clear();
	}
	
	
	/**
	 * Returns the size of the TimeSeries<E>
	 * @return
	 */
	public int size() {
		return(this.dequeue.size());
	}
	
	
	/**
	 * Returns an Iterator<E> for the TimeSeries
	 */
	@Override
	public Iterator<E> iterator() {
		return(this.dequeue.iterator());
	}
	

	/**
	 * Returns a List<E> from the TimeSeries
	 * @return
	 */
	public List<E> toList() {
		return(this.reference_list);
	}
	
	
	@Override
	public String toString() {
		return "TimeSeries "
				+ "\n[ update=" + update 
				+ "\n, capacity=" + capacity 
				+ "\n, dequeue=\n" + dequeue + "]\n";
	}
	

}
