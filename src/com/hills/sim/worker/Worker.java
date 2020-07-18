package com.hills.sim.worker;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import com.hills.sim.Constants;
import com.hills.sim.ea.Strategy;
import com.hills.sim.exceptions.CompactorExc;
import com.hills.sim.exceptions.LogBookExc;
import com.hills.sim.exceptions.StageExc;
import com.hills.sim.exceptions.WorkerExc;
import com.hills.sim.logs.LogBook;
import com.hills.sim.logs.LogRowsBag;
import com.hills.sim.settings.Run;
import com.hills.sim.settings.RunSettings;
import com.hills.sim.settings.SESettings;
import com.hills.sim.settings.SymbolSettings;
import com.hills.sim.stage.Candle;
import com.hills.sim.stage.CandleViewer;
import com.hills.sim.stage.Stage;

public class Worker implements RunHandler, CandleViewer {
	
	private final SESettings se_settings;
	private final RunSettings ea_settings;
	
	private final LinkedBlockingQueue<CompactorCandle> queue;
	private final LinkedBlockingQueue<LogRowsBag> log_queue;
	
	private final Compactor compactor;
	
	private final Stage stage;
	private final Strategy strategy;
	private final LogBook log_book;
	
	private int candle_id;
	private final int total_candles;
	
	/**
	 * Make Worker
	 * @param se_settings SimEngine settings
	 * @param ea_settings EA setting
	 * @throws WorkerExc
	 */
	public Worker(SESettings se_settings, RunSettings ea_settings)  
			throws WorkerExc {
		
//		Set the candle id to 1
		this.candle_id = 1;
		
//		Make linked blocking queue
		this.queue = new LinkedBlockingQueue<>
										(Constants.Q_LIMIT_CCANDLES);	
		this.log_queue = new LinkedBlockingQueue<>
										(Constants.Q_LIMIT_LOGROWSBAG);	
		
//		Save the SimEngine settings and EA settings
		this.se_settings = se_settings;
		this.ea_settings = ea_settings;
		
//		Get the Compactor
		this.compactor = this.se_settings.getCompactor();		
			
//		Set the total number of candles
		try {
			this.total_candles = this.compactor.getTotalCompactorCandles();
			
		} catch (CompactorExc e) {
			String error_msg = "%s: Error getting total number of candles\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new WorkerExc();
		}
		
		
//		Make Stage
		try {
			this.stage = new Stage(this.se_settings);
			
		} catch (StageExc e) {
			String error_msg = "%s: Error making Stage\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new WorkerExc();
		}
		
		
//		Make Strategy using Reflection API
		try {
			String strategy_name = this.se_settings.getGenSettings()
												.getStrategyName();
			
			String class_path = Constants.EA_PATH + strategy_name;
			
			Class<?> c = Class.forName(class_path);
			Constructor<?> cons = c.getConstructor(Stage.class, SESettings.class);
			this.strategy = (Strategy) cons.newInstance(this.stage, this.se_settings);
			
		} catch (Exception e) {
			String error_msg = "%s: Error making Strategy\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new WorkerExc();
		}
		
		
//		Make LogBook 
		try {
			this.log_book = new LogBook(this.se_settings);
			
		} catch (LogBookExc e) {
			String error_msg = "%s: Error making LogBook\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new WorkerExc();
		} 
		
	}

	
	/**
	 * Test code: Iterate each run and perform the run
	 */
	public void execute() throws WorkerExc {
		Thread writeRunsLog = new Thread(this::writeRunsLog, "WriteRunsLog");
		writeRunsLog.start();

		try {
			List<Run> runs = this.ea_settings.getRuns();
			for(int i = 0; i < runs.size(); i++) {
				Run run = runs.get(i);
				System.out.println("");
				System.out.printf("Run %d of %d: %s", i+1, runs.size(), run);

//			Update with the new run,
//			Performs run and reset the run
				this.setRun(run); this.run(); this.reset();
			}
			
		} catch (Exception e) {
			String error_msg = "%s: Error executing run\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new WorkerExc();
		}
		
//		The join command will ensure the main thread wait for
//		thread to end (i.e. join back).
		try {
			writeRunsLog.join();
			
		} catch (InterruptedException e) {
			String error_msg = "%s: writeRunsLog thread was interrupted\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new WorkerExc();
		}
		
//		Close the log book and the compactor
		this.log_book.close();
		try {
			this.compactor.close();
			
		} catch (CompactorExc e) {
			String error_msg = "%s: Error closing Compactor\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new WorkerExc();
		}
	}
	
	
	/**
	 * Perform 1 run
	 */
	public void run() {
//		Put thread must be initialized first
		Thread put_candles = new Thread(this::putCompactorCandles, 
										"PutCompactorCandles");
		Thread take_candles = new Thread(this::takeCompactorCandles, 
										"TakeCompactorCandles");
		Thread log_logrowsbag = new Thread(this::recordLogRowsBag, 
										"RecordLogRowsBag");
		
		put_candles.start();
		take_candles.start();
		log_logrowsbag.start();
		
//		The join command will ensure the main thread wait for
//		thread to end (i.e. join back).
		try {
			put_candles.join();
			take_candles.join();
			log_logrowsbag.join();
			
		} catch (InterruptedException e) {
			String error_msg = "%s: Put, take or log threads were interrupted\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
		}
	}

	
	/**
	 * Close the previous run by resetting the objects
	 */
	@Override
	public void reset() {
		this.stage.reset();
		this.strategy.reset();
		this.log_book.reset();
	}
	
	
	/**
	 * Save the current run settings
	 * @param r
	 */
	@Override
	public void setRun(Run r) {
		this.candle_id = 1;
		this.stage.setRun(r);
		this.strategy.setRun(r);
		this.log_book.setRun(r);
	}
	
	
	/**
	 * Method to write Runs Log in a new Thread
	 */
	private void writeRunsLog() {
		this.log_book.writeRunsLog(this.ea_settings);
	}
	
	
	/**
	 * Method to run in separate thread; Put CompactorCandle into queue
	 */
	private void putCompactorCandles() {
		String query = "";
		CompactorCandle candle = null;

//		Generate all the queries. For every query, get the candles
		try {
			List<String> queries = this.compactor.getQueries();
			
			for(int i = 0; i < queries.size(); i++) {
				query = queries.get(i);
				List<CompactorCandle> candles =
						this.compactor.getCandles(query);
				
//				Blocking code to 'put'
				for(int j = 0; j < candles.size(); j++) {
					candle = candles.get(j);
					this.queue.put(candle);
				}
			}
			
		} catch (CompactorExc e) {
			String error_msg = "%s: Error getting candles\n";
			e.printStackTrace();
			System.err.printf("SQL query: %s\n", query);
			System.err.printf(error_msg, this.getClass().getName());
			Thread.currentThread().interrupt();
			
		} catch (InterruptedException e) {
			String error_msg = "%s: Error inserting candle\n";
			e.printStackTrace();
			System.err.printf("Candle: %s\n", candle);
			System.err.printf(error_msg, this.getClass().getName());
			Thread.currentThread().interrupt();			
		}
		
//		Put unlit candle, to signal the end of task
		try {
			this.queue.put(new CompactorCandle());
			
		} catch (InterruptedException e) {
			String error_msg = "%s: Error inserting unlit candle\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			Thread.currentThread().interrupt();		
		}
	}
	
	
	
	/**
	 * Method to run in separate thread; 
	 * Take CompactorCandle from queue and process it
	 */	
	private void takeCompactorCandles() {
		CompactorCandle comp_candle = null;
		SymbolSettings sym_settings = this.se_settings.getSymSettings();
		
//		Make the Candle object from the CompactorCandle
//		When an unlit candle is received, place an empty logrowsbag into
//		the log queue
		int unit_pip = sym_settings.getPip();
		int unit_distance = sym_settings.getDistance();
		
		try {

			while(true) {


				comp_candle = this.queue.take();
				
				Candle candle = new Candle
								(comp_candle, 
								 this.candle_id, 
								 this.total_candles,
								 unit_pip, 
								 unit_distance);
				this.candle_id++;
				this.viewCandle(candle);
				if(!comp_candle.isLit()) {
					this.log_queue.put(new LogRowsBag());
					break;
				}
				

			} 
						
		} catch (InterruptedException e) {
			String error_msg = "%s: Error taking compactor candle\n";
			e.printStackTrace();
			System.err.printf("Candle: %s\n", comp_candle);
			System.err.printf(error_msg, this.getClass().getName());
			Thread.currentThread().interrupt();
			
		} catch (WorkerExc e) {
			String error_msg = "%s: Error viewing candle\n";
			e.printStackTrace();
			System.err.printf("Candle: %s\n", comp_candle);
			System.err.printf(error_msg, this.getClass().getName());
			Thread.currentThread().interrupt();			
		}
		
	}
	
	
	
	/**
	 * Method to run in separate thread; 
	 * Take LogRowsBag from log queue and process them
	 * Break the while loop when an empty LogRowsBag is received
	 */	
	private void recordLogRowsBag() {
		LogRowsBag logrowsbag = null;
		
		try {
			System.out.printf
			("Total number of candles: %d, processed candle #", 
					this.total_candles);
			
			for(int proc_candle = 0; true; proc_candle++) {
				logrowsbag = this.log_queue.take();
				System.out.printf("%d", proc_candle);

				if(!logrowsbag.isFull()) break;
				this.log_book.write(logrowsbag);
				
				int num_length = Integer.toString(proc_candle).length();
				for(int i = 0; i < num_length; i++) System.out.printf("\b");
			} 
			
			System.out.println("");	
			
		} catch (InterruptedException e) {
			String error_msg = "%s: Error taking LogRowsBag from queue\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			Thread.currentThread().interrupt();
			
		} catch (IOException e) {
			String error_msg = "%s: Error logging LogRowsBag\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			Thread.currentThread().interrupt();	
		}
		

	}
	
	
	@Override
	public void viewCandle(Candle candle) throws WorkerExc {

//		Exit when the candle is unlit
		if(!candle.isLit()) return;
		
//		Stage listens to candle
		try {
			this.stage.viewCandle(candle);
			
		} catch (StageExc e) {
			String error_msg = "%s: Error viewing candle\n";
			System.err.printf(error_msg, this.getClass().getName());
			e.printStackTrace();
			throw new WorkerExc();
		}
		
//		Strategy listens to candle
		try {
			this.strategy.viewCandle(candle);
			
		} catch (Exception e) {
			String error_msg = "%s: Error viewing candle\n";
			System.err.printf(error_msg, this.getClass().getName());
			e.printStackTrace();
			throw new WorkerExc();
		}
	
		
//		Put LogRowsBag onto blocking queue
		try {
			LogRowsBag logrowsbag = this.stage.toLogRowsBag();
			this.log_queue.put(logrowsbag);
			
		} catch (InterruptedException e) {
			String error_msg = "%s: Error putting LogRowsBag onto queue\n";
			System.err.printf(error_msg, this.getClass().getName());
			e.printStackTrace();
			Thread.currentThread().interrupt();	
			throw new WorkerExc();
		}
		
	}
	
}
