package com.hills.sim.ea;

import com.hills.sim.settings.Run;
import com.hills.sim.settings.SESettings;
import com.hills.sim.stage.Calculator;
import com.hills.sim.stage.Candle;
import com.hills.sim.stage.CandleViewer;
import com.hills.sim.stage.Stage;
import com.hills.sim.worker.RunHandler;

public class Strategy implements CandleViewer, RunHandler {
	
	protected final Stage stage;
	protected final SESettings settings;
	protected final Calculator calculator;
	protected Run run;
	
	
	public Strategy(Stage stage, SESettings settings) {
		this.stage = stage;
		this.settings = settings;
		this.calculator = this.stage.getCalculator(); 
	}
	
	
	@Override
	public void viewCandle(Candle candle) 
								throws Exception {
		
	}

	
	/**
	 * Set the run as null and reset the runpile
	 */
	@Override
	public void reset() {
		this.run = null;
	}
	
	/**
	 * Set the run
	 * @param run
	 */
	public void setRun(Run run) {
		this.run = run;
	}
	
	
//	Filler code
	public Run getRun() {
		return run;
	}


	/**
	 * @return the stage
	 */
	public Stage getStage() {
		return stage;
	}


	/**
	 * @return the calculator
	 */
	public Calculator getCalculator() {
		return calculator;
	}

	
	
}
