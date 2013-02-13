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

import edu.usc.pgroup.floe.api.communication.ConnectionInfo;

/***
 * This manages the Floe graphs and Flakes for different executions.
 * 
 * @author Sreedhar Natarajan (sreedhan@usc.edu)
 * @author Yogesh Simmhan (simmhan@usc.edu)
 * @version v0.1, 2012-01-03
 *
 */
public interface Coordinator 
{
	/**
	 * Create a new Floe graph, InpConnection Specifies the inpConnections for each of the 
	 * first layer nodes. OutConnection specifies who receives the output
	 */
	public StartFloeInfo createFloe(FloeGraph graph,ConnectionInfo inpConnection,ConnectionInfo outConnection);
	
	/**
	 * Start a created Floe graph with optional initial inputs
	 */
	public void startFloe(String floeId);
	
	/**
	 * Stop a Floe graph
	 */
	public void stopFloe(String floeId);
	
	/**
	 * Pause a started Floe graph.
	 */
	public void pauseFloe(String floeId);

	/**
	 * Resumes a paused Floe graph.  
	 */
	public void resumeFloe(String floeId);

	/**
	 * Updates an existing Floe graph.  
	 */
	public void updateFloe(String floeId, FloeGraph graph);

}
