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
 * A Flake is the computation unit that can process a message/task. 
 * The task that it performs depends on the type of Pellet.
 * A Flake is part of a Container, and the resources available to a Flake are determined by the Container.
 * 
 * @author Sreedhar Natarajan (sreedhan@usc.edu)
 * @author Yogesh Simmhan (simmhan@usc.edu)
 * @version v0.1, 2012-01-03
 *
 */

import java.util.List;
import java.util.Map;

import edu.usc.pgroup.floe.api.communication.ConnectionInfo;
import edu.usc.pgroup.floe.api.communication.Message;
import edu.usc.pgroup.floe.api.communication.SinkChannel;
import edu.usc.pgroup.floe.api.communication.SourceChannel;
import edu.usc.pgroup.floe.api.framework.FloeGraph.Edge;

public interface Flake 
{
	/**
	 * Get the FlakeInfo information
	 */
	public FlakeInfo getFlakeInfo();
	/**
	 * Initialize a Flake of the given type. This allocates resources. No communication with other Flakes has been established.  
	 */
	public String create(String inpFlakeID,String pelletType, ResourceInfo resources,List<Edge> inpChannel,List<Edge> outChannel,List<Port> inpPorts,List<Port> outPorts);
	
	public void wire(List<ConnectionInfo> inpConnection);
	/**
	 * This sets up the input and output communication channels between source and sink Flakes.
	 * Upon completion, the Flake is ready to accept and process messages. 
	 */
	public void startFlake(List<SourceChannel> sources, List<SinkChannel> sinks);

	/**
	 * This stops the current Flake after completing the current message. 
	 * The Flake cannot continue after this point. 
	 * All resources allocated to the Flake can be recovered. 
	 */
	public void stopFlake();

	/**
	 * This pauses the Flake after completing the current message.
	 * The Flake can be resumed later. 
	 * Implementations may allow state information of the Flake to be saved for resuming within a different container.  
	 */
	public void pauseFlake();

	/**
	 * This resumes a paused the Flake and allows it to accept and process messages.
	 * Implementations may the Flake to be resumed within a different container, 
	 * using different source and sinks, or different resource allocations than before.  
	 */
	
	public void modifyResource(ResourceInfo inpResource);
	public void modifyPelletCount(int count);
	public void resumeFlake();
	
	public String getPelletType();
	
	public String getFlakeId();
	
	public ResourceInfo getResources();
	
	public List<SourceChannel> getSources();

	public List<SinkChannel> getSinks();
	}
