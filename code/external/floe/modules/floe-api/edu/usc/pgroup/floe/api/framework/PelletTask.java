/*
 * Copyright 2011, University of Southern California. All Rights Reserved.
 * 
 * This software is experimental in nature and is provided on an AS-IS basis only. 
 * The University SPECIFICALLY DISCLAIMS ALL WARRANTIES, EXPRESS AND IMPLIED, INCLUDING WITHOUT 
 * LIMITATION ANY WARRANTY AS TO MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.
 * 
 * This software may be reproduced and used for non-commercial purposes only, 
 * so long as this copyright notice is reproduced with each such copy made.
 */
package edu.usc.pgroup.floe.api.framework;

/***
 * A Pellet Task which intializes the Pellet Class and calls the invoke method of the Implemented
 * Cutom Pellet
 * 
 * @author Sreedhar Natarajan (sreedhan@usc.edu)
 * @author Yogesh Simmhan (simmhan@usc.edu)
 * @version v0.1, 2012-04-18
 *
 */

import java.util.concurrent.RecursiveTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.usc.pgroup.floe.api.state.StateObject;
import edu.usc.pgroup.floe.impl.FlakeImpl;
import edu.usc.pgroup.floe.impl.pelletHandlers.PelletHandler;
import edu.usc.pgroup.floe.impl.pelletRunners.PelletRunner;
import edu.usc.pgroup.floe.impl.queues.SinkQueue;
import edu.usc.pgroup.floe.impl.queues.SourceQueue;

public class PelletTask extends RecursiveTask<Object> {
	public static enum PelletTaskStatus {
		COMPLETED, RUNNING, NEW
	}

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(PelletTask.class.getName());
	private final StateObject stateObject;
	private final PelletHandler pelletHandler;
	private final SourceQueue sourceQueue;
	private final SinkQueue sinkQueue;
	private final String tag;
	private PelletTaskStatus pelletStatus;
	private final FlakeImpl flakeImpl;
	private final Class pelletClass;
	private final PelletRunner pelletRunner;

	public PelletTask(PelletHandler pelletHandler, SourceQueue sourceQueue, SinkQueue sinkQueue, StateObject stateObject, FlakeImpl flakeImpl,
			Class pelletClass) {
		this.pelletHandler = pelletHandler;
		this.stateObject = stateObject;
		this.pelletClass = pelletClass;
		this.tag = null;
		this.sourceQueue = sourceQueue;
		this.sinkQueue = sinkQueue;
		this.pelletStatus = PelletTaskStatus.NEW;
		this.flakeImpl = flakeImpl;
		this.pelletRunner = pelletHandler.createPelletRunner(sourceQueue, sinkQueue, pelletClass, stateObject);
		logger.info("Consturcotr of pellet task is getting invoked");
	}

	@Override
	protected Object compute() {
		logger.info("Running compute inside pellet task");
		pelletStatus = PelletTaskStatus.RUNNING;
		logger.info("Pellet runner class " + pelletHandler.getClass().getName());

		try {
			pelletRunner.runPellet();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception while running pellet", e);
		}
		// notify pellet completion
		pelletStatus = PelletTaskStatus.COMPLETED;
		this.flakeImpl.notifyPelletCompletion(pelletRunner.getPelletInstance());
		return null;
	}

	public PelletTaskStatus getPelletStatus() {
		return this.pelletStatus;
	}

	public void sendPauseLandmark() {
		pelletRunner.sendPauseLandmark();
	}

	public String getTag() {
		return this.tag;
	}
}
