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

import java.util.List;

import edu.usc.pgroup.floe.api.framework.FloeGraph.Edge;

/***
 * An Container maps to a single host (or) VM. 
 * It is responsible for local resource allocation to Flakes that may be running within this VM.
 * A container can contain zero or more Flakes running within it.
 * A container is a single java process.
 *   
 * @author Sreedhar Natarajan (sreedhan@usc.edu)
 * @author Yogesh Simmhan (simmhan@usc.edu)
 * @version v0.1, 2012-01-03
 *
 */
public interface Container 
{
	/**
	 * 	Create a Flake of specific type and resource allocation
	 * @return Identifier for this Flake
	 */
	public FlakeInfo createFlake(String inpFlakeID,String pelletType, ResourceInfo resources,List<Edge> inpChannel,List<Edge> outChannel,List<Port> inpPorts,List<Port> outPorts);
	
	/**
	 * Start the Flake with the specified channels
	 * @param flakeID
	 */
	public void startFlake(String flakeID);
	
	/**
	 * Stop the Flake, Ignore the Buffer Entries
	 * @param flakeID
	 */
	public void stopFlake(String flakeID);
	
	/**
	 * Pause the specified Flake. Channels remain active 
	 * @param flakeID
	 */
	public void pauseFlake(String flakeID);
	
	/**
	 * Resume the Flake
	 * @param flakeID
	 */
	public void resumeFlake(String flakeID);
	
	/**
	 * Display the list of flakes that are running within this container
	 */	
	public List<FlakeInfo> listFlakes();
	
}
