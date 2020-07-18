package com.hills.sim;

import java.lang.reflect.Constructor;
import java.util.List;

import com.hills.sim.exceptions.SESettingsExc;
import com.hills.sim.exceptions.SimEngineExc;
import com.hills.sim.exceptions.WorkerExc;
import com.hills.sim.settings.RunSettings;
import com.hills.sim.settings.SESettings;
import com.hills.sim.settings.Setting;
import com.hills.sim.worker.Worker;

public class SimEngine {
	
	private final String filename;
	
	private final RunSettings ea_settings;
	private final SESettings se_settings;
	private final Worker worker;
	
	
	public SimEngine(String filename) throws SimEngineExc {
		
		this.filename = filename;
		
//		Make SESettings object
		try {
			this.se_settings = new SESettings(this.filename);
			
		} catch (SESettingsExc e) {
			String error_msg = "%s: Error making SESettings\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new SimEngineExc();
		}
		
//		Make EASettings object
		try {
			List<Setting> html_settings = 
							this.se_settings.getHtmlSettings();
			
			String strategy_name = this.se_settings.getGenSettings()
												.getStrategyName();
			
			String class_path = Constants.getPackageName(strategy_name) + 
									".EASettings";
			
			Class<?> c = Class.forName(class_path);
			Constructor<?> cons = c.getConstructor(List.class);
			this.ea_settings = (RunSettings) cons.newInstance(html_settings);
			
		} catch (Exception e) {
			String error_msg = "%s: Error making EASettings\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new SimEngineExc();
		}

		
//		Make Worker object
		try {
			this.worker = new Worker(se_settings, ea_settings);
			
		} catch (WorkerExc e) {
			String error_msg = "%s: Error making Worker\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new SimEngineExc();
		}
	}
	
	
	public void execute() throws SimEngineExc {

		System.out.println("");
		System.out.println("Status");
		System.out.println("===============");
		System.out.println("\n");
		
		System.out.println("1. Parsed HTML settings.");
		System.out.println("2. Generated Run settings.");
		System.out.println("3. Made Worker.");
		System.out.println("\n");

		System.out.println("Execute Runs");
		System.out.println("===============");
		
		long startTime = System.currentTimeMillis();
		
		try {
			this.worker.execute();
			
		} catch (WorkerExc e) {
			String error_msg = "%s: Error executing runs\n";
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new SimEngineExc();
		}
		
		System.out.println("\n");
		System.out.println("Completed");
		System.out.println("===============");
		System.out.println("");
		
		long difference = System.currentTimeMillis() - startTime;
		long millis = difference % 1000;
		long second = millis <= 500 
				? (difference / 1000) % 60 : 1 + ((difference / 1000) % 60);
		long minute = (difference / (1000 * 60)) % 60;
		long hour = (difference / (1000 * 60 * 60)) % 24;

		String time = String.format("%02d:%02d:%02d", hour, minute, second);
		System.out.printf("Total execution time (hh:mm:ss) : %s\n\n", time);
	}
	
	
	public static void main(String[] args) {
		
		SimEngine se = null;
		String settings = "";
		
//		Find the start date, end date and time frame
		for(int i = 0; i < args.length; i++) {
			
//			Save the start date
			if(args[i].equalsIgnoreCase(Constants.CMD_ARG_SETTINGS)) {
				i++;
                if(i >= args.length)
                    usage();
                settings = args[i];
			}
		}
		
//		Exit if incorrect parameters are given
		if(args.length == 0) {
			usage();
			System.exit(1);
		}
		
//		Make the simulation engine
//		Run the simulation
		try {
			se = new SimEngine(settings);
			se.execute();
			
		} catch (SimEngineExc e) {
			System.err.printf("SimEngine: Failed simulation.\n");
			e.printStackTrace();
			System.exit(1);
		}
		
	}

	
	/**
	 * Prints "Howto" use the program
	 */
	public static void usage() {
		System.out.println("\n[Version: xxx] "
						   + "SimEngine will run trade simulation(s)");
		System.out.println("\nArgument: --settings <filename>");
		System.out.println("\nExample of running a trial: "
						   + "\n\tjava -cp .;libs/* -jar <jarfile> --settings settings.html");
	}
}
