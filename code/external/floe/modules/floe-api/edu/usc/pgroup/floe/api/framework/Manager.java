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

import java.util.HashMap;
import java.util.List;

/***
 * This manages the Containers and their resources within a region.
 * 
 * @author Sreedhar Natarajan (sreedhan@usc.edu)
 * @author Yogesh Simmhan (simmhan@usc.edu)
 * @version v0.1, 2012-01-03
 *
 */
public interface Manager
{
	/**
	 * Create a new container on a host/VM.
	 * @return The identifier for the Container that was created
	 */
	//public String createContainer();
	HashMap<String,ContainerInfo> allocateResources(List<String> pellets, List<ResourceInfo> resources);
	
	/**
	 * Destroy an existing container and free all its allocated resources.
	 */
	//public void destroyContainer(String containerId);
	void releaseResources(List<ContainerInfo> containers);
	
	/**
	 * List all the Containers that are controlled by this coordinator
	 */
	public List<ContainerInfo> listContainers();
	
}
