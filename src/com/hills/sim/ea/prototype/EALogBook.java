package com.hills.sim.ea.prototype;

import java.io.File;
import java.io.IOException;

import com.hills.sim.Constants.LogTypeE;
import com.hills.sim.exceptions.LogBookExc;
import com.hills.sim.exceptions.LogExc;
import com.hills.sim.logs.Log;
import com.hills.sim.logs.LogRow;
import com.hills.sim.settings.SESettings;

public class EALogBook {
	
	private final SESettings se_settings;
	private final String output_dir;
	
	private final Log indis_log;
	
	
	public EALogBook(SESettings se_settings) throws LogBookExc {
		
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
		
//		Make the log
		try {
			this.indis_log = new Log(this.output_dir, LogTypeE.INDICATORS);
			
		} catch (LogExc e) {
			String error_msg = "%s: Error creating new log file(s)\n";
			System.err.printf(error_msg, this.getClass().getName());
			e.printStackTrace();
			throw new LogBookExc();
		}

	}
	
	
	/**
	 * Make the directory if it don't exists
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
	 * Write the row to the Indicators Log
	 * @param row LogRow
	 * @throws LogBookExc
	 */
	public void writeIndisLog(LogRow row) throws LogBookExc {
		if(row == null) return;
		if(row.getValue() == null) return;
		if(row.getValue().size() == 0) return;
		
		try {
			this.indis_log.write(row);
			
		} catch (IOException e) {
			String error_msg = "%s: Error writing to log\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new LogBookExc();
		}
	}
	
}
