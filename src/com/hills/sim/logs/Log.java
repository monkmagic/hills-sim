package com.hills.sim.logs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import com.hills.sim.Constants.LogTypeE;
import com.hills.sim.exceptions.LogExc;
import com.hills.sim.settings.Run;
import com.hills.sim.settings.RunSettings;
import com.hills.sim.worker.RunHandler;

public class Log implements RunHandler {
	
	/**
	 * Members relating to settings of the Log
	 */
	private final LogTypeE log_type;
	private final String output_dir;
	private final boolean write_once;
	private boolean wrote_once;

	/**
	 * Members relating to working of the logs
	 */
	private CSVPrinter output_file;
	
	public Log(String output_dir, LogTypeE log_type) throws LogExc {
		this.log_type = log_type;
		this.output_dir = output_dir;
		this.write_once = (this.isTypeRuns() || this.isTypeCandles())
						 ? true : false;
		this.wrote_once = false;
		
		
//		Open the output file and write the headers, if they are available
		try {
			this.output_file = this.setOutputFile();
			
			String[] headers = this.log_type.getHeaders();
			if(headers != null) this.write(new LogRow(headers));
			
		} catch (IOException e) {
			String error_msg = "%s: Error creating new log file\n";
			System.err.printf(error_msg, this.getClass().getName());
			System.err.printf("Log type: %s\n", this.log_type.getLogName());
			e.printStackTrace();
			throw new LogExc();
		}
	}
	
	
	/**
	 * RunHandler setRun:
	 */
	@Override
	public void setRun(Run run) {
//		Do nothing if the runs or candles log have been written
		if(this.isTypeRuns() || this.isTypeCandles()) 
			if(this.isWroteOnce()) return;

	}


	/**
	 * RunHandler reset:
	 */
	@Override
	public void reset() {
//		Do nothing if the runs or candles log have been written
		if(this.isTypeRuns() || this.isTypeCandles()) 
			if(this.isWroteOnce()) return;
		
//		If the runs or candles log have just been written once,
//		set the wrote once flag to true
		if(this.isTypeRuns() || this.isTypeCandles()) 
			if(this.isWroteOnce() == false) this.setWroteOnce();
	}
	

	/**
	 * Close the log file
	 */
	public void close() {
		
//		Close the output file
		try {
			this.output_file.flush();
			this.output_file.close();

		} catch (IOException e) {
			String error_msg = "%s: Error closing log file\n";
			System.err.printf(error_msg, this.getClass().getName());
			e.printStackTrace();
		}
	}
	
	/**
	 * For a given run, create the CSVPrinter to log the record
	 * @return csv_printer the csv printer
	 * @throws IOException
	 */
	private CSVPrinter setOutputFile() throws IOException {
		CSVPrinter result = null;
		
		String filename = this.log_type.getLogName();
		
//		Create new file and open a new CSVPrinter
		try {
			File output_fname = new File
								(this.output_dir + File.separator + filename);
			FileWriter output_file = new FileWriter(output_fname);
			result = new CSVPrinter(output_file, CSVFormat.DEFAULT);
			
		} catch(IOException e) {
			String error_msg = "%s: Error creating new CSVPrinter.\n";
			System.err.printf(error_msg, this.getClass().getName());
			System.err.printf("Absolute path: %s\n", filename);
			throw new IOException();
		}
		
		return(result);
	}
	
	
	/**
	 * Write 1 LogRow into the log file.
	 * @param row LogRow
	 * @throws IOException
	 */
	public void write(LogRow row) throws IOException {
		if(row == null) return;
		
		List<String> value = row.getValue();
		if(value.size() == 0) return;
		
		try {
			this.output_file.printRecord(value);
			this.output_file.flush();
			
		} catch (IOException e) {
			String error_msg = "%s: Error writing record.\n";
			System.err.printf(error_msg, this.getClass().getName());
			System.err.printf("Values: %s\n", value);
			throw new IOException();
		}
	}
	
	
	/**
	 * Write many LogRows into the log file
	 * @param rows list of LogRow
	 * @throws IOException
	 */
	public void write(List<LogRow> rows) throws IOException {
		if(rows == null) return;
		
		List<List<String>> values = rows.stream()
										.map(LogRow::getValue)
										.collect(Collectors.toList());
		
		if(values.size() == 0) return;
		
		try {
			this.output_file.printRecords(values);
			this.output_file.flush();
			
		} catch (IOException e) {
			String error_msg = "%s: Error writing records.\n";
			System.err.printf(error_msg, this.getClass().getName());
			System.err.printf("Values: %s\n", values);
			throw new IOException();
		}
	}
	
	
	
	/**
	 * Write the RunsLog in 1 statement
	 * @param run_settings Run Settings
	 * @throws IOException
	 */
	public void writeRunsLog(RunSettings run_settings) throws IOException {
		String filename = this.log_type.getLogName();
		
		//Create new file and open a new CSVPrinter
		try {
			File output_fname = new File
								(this.output_dir + File.separator + filename);
			FileWriter output_file = new FileWriter(output_fname);
			this.output_file = new CSVPrinter(output_file, CSVFormat.DEFAULT);
			
		} catch(IOException e) {
			String error_msg = "%s: Error creating new CSVPrinter.\n";
			System.err.printf(error_msg, this.getClass().getName());
			System.err.printf("Absolute path: %s\n", filename);
			throw new IOException();
		}
		
		this.write(run_settings.toLog());
		this.wrote_once = true;
		
		this.output_file.flush();
		
	}
	
	/**
	 * Checks Status
	 */
	
	public boolean isTypeAccount() {
		return(this.log_type == LogTypeE.ACCOUNT);
	}
	
	public boolean isTypeCandles() {
		return(this.log_type == LogTypeE.CANDLES);
	}
	
	public boolean isTypeIndicators() {
		return(this.log_type == LogTypeE.INDICATORS);
	}
	
	public boolean isTypeOrders() {
		return(this.log_type == LogTypeE.ORDERS);
	}
	
	public boolean isTypeRuns() {
		return(this.log_type == LogTypeE.RUNS);
	}
	
	public boolean isTypeStrategy() {
		return(this.log_type == LogTypeE.STRATEGY);
	}
	
	
	/**
	 * Can the log file be written once?
	 * @return write_once Boolean
	 */
	public boolean isWriteOnce() {
		return(this.write_once);
	}
	
	/**
	 * Has a file that can be written once, been written?
	 * @return wrote_once Boolean
	 */
	public boolean isWroteOnce() {
		return(this.wrote_once);
	}
	
	/**
	 * Set a file, that can be written once, to wrote once
	 */
	public void setWroteOnce() {
		if(this.isWriteOnce() == false) return;
		if(this.isWroteOnce() == true) return;
		this.wrote_once = true;
	}
	
}
