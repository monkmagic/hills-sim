package com.hills.sim.logs;

import java.io.File;
import java.io.IOException;

import com.hills.sim.Constants.LogTypeE;
import com.hills.sim.exceptions.LogBookExc;
import com.hills.sim.exceptions.LogExc;
import com.hills.sim.settings.Run;
import com.hills.sim.settings.RunSettings;
import com.hills.sim.settings.SESettings;
import com.hills.sim.worker.RunHandler;

public class LogBook implements RunHandler {
	private boolean wrote_candleslog;
	
	private final SESettings se_settings;
	private final String output_dir;
	
	private final Log runs_log;
	private final Log candles_log;
	private final Log accounts_log;
	private final Log orders_log;
	private final Log reports_log;
	
	
	public LogBook(SESettings se_settings) throws LogBookExc {
		this.wrote_candleslog = false;
		
		this.se_settings = se_settings;
		this.output_dir =  
				this.se_settings.getGenSettings().getOutputDirectory();
		
//		Make the output directory
		try {
			this.makeOutputDir();
			
		} catch (LogBookExc e) {
			String error_msg = "%s: Error making output directory.\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new LogBookExc();
		}
		
//		Make the logs
		try {
			this.runs_log = new Log(this.output_dir, LogTypeE.RUNS);
			this.candles_log = new Log(this.output_dir, LogTypeE.CANDLES);
			this.accounts_log = new Log(this.output_dir, LogTypeE.ACCOUNT);
			this.orders_log = new Log(this.output_dir, LogTypeE.ORDERS);
			this.reports_log = new Log(this.output_dir, LogTypeE.REPORTS);
			
		} catch (LogExc e) {
			String error_msg = "%s: Error creating new log file(s)\n";
			System.err.printf(error_msg, this.getClass().getName());
			e.printStackTrace();
			throw new LogBookExc();
		}

	}
	
	/**
	 * Make the directory if it exists
	 */
	private void makeOutputDir() throws LogBookExc {
		File dir = new File(this.output_dir);
		if(dir.exists() && dir.isDirectory()) return;
		
		if(dir.exists() && dir.isDirectory() == false) {
			String error_msg = "%s: file exists in place of output directory.\n";
			System.err.printf(error_msg, this.getClass().getName());
			throw new LogBookExc();
		}
		
		if(dir.exists() == false) dir.mkdirs();
	}
	
	
	/**
	 * Close all the open logs in the LogBook
	 */
	public void close() {
		this.runs_log.close();
		this.candles_log.close();
		this.accounts_log.close();
		this.orders_log.close();
		this.reports_log.close();
	}
	
	
	@Override
	public void setRun(Run run) {
		this.candles_log.setRun(run);
		this.accounts_log.setRun(run);
		this.orders_log.setRun(run);
	}


	@Override
	public void reset() {
		this.candles_log.reset();
		this.accounts_log.reset();
		this.orders_log.reset();
		
		if(this.wrote_candleslog == false) 
			this.wrote_candleslog = true;
	}
	
	
	/**
	 * Write the LogRowsBag into the respective Logs
	 * @param logrowsbag LogRowsBag
	 * @throws IOException
	 */
	public void write(LogRowsBag logrowsbag) throws IOException {
		try {
			if(this.candles_log.isWriteOnce() && !this.candles_log.isWroteOnce()) {
				this.candles_log.write(logrowsbag.getCandle());
			}
			
			this.accounts_log.write(logrowsbag.getAccount());
			this.orders_log.write(logrowsbag.getOrders());
			this.reports_log.write(logrowsbag.getReport());
			
		} catch (IOException e) {
			String error_msg = "%s: Error writing LogRowBag.\n";
			System.err.printf(error_msg, this.getClass().getName());
			throw new IOException();
		}
	}
	
	
	/**
	 * Write the Runs Log
	 * @param run_settings Run Settings
	 */
	public void writeRunsLog(RunSettings run_settings) {
		try {
			this.runs_log.writeRunsLog(run_settings);
			
		} catch (IOException e) {
			String error_msg = "%s: Error writing Runs log.\n";
			System.err.printf(error_msg, this.getClass().getName());
		}
	}
	
}
