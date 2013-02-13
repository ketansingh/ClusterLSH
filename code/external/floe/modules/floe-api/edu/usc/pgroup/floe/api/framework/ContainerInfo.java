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
 * This described the resources for a Flake within a container.  
 * 
 * @author Sreedhar Natarajan (sreedhan@usc.edu)
 * @author Yogesh Simmhan (simmhan@usc.edu)
 * @version v0.1, 2012-01-03
 *
 */
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "ContainerInfo")
@XmlType(propOrder = { "containerId", "resourceDescription", "activeResources","availableResources","containerStatus"})
public class ContainerInfo 
{
	@XmlElement(name="containerId")
	String containerId;
	@XmlElement(name="resourceDescription")
	String resourceDescription;
	@XmlElement(name="activeResources")
	ResourceInfo activeResources;
	@XmlElement(name="availableResources")
	ResourceInfo availableResources;
	@XmlElement(name="containerStatus")
	String containerStatus;	
	ContainerInfo()
	{
		
	}
	public ContainerInfo(String containerId,ResourceInfo availableResource,ResourceInfo activeResource)
	{		
		assert containerId != null : "Container ID must be specified";
			this.containerId = containerId;		
		// One core is allocated for the Container and the rest of the Cores
		// for the flakes.
		this.availableResources = availableResource;
		this.activeResources = activeResource;		
	}
	public String getContainerId()
	{
		return this.containerId;
	}
	public void setContainerId(String containerID)
	{
		this.containerId = containerID;
	}
	public String getresourceDescription()
	{
		return this.resourceDescription;
	}
	public void setresourceDescription(String resourceDesc)
	{
		this.resourceDescription = resourceDesc;
	}
	public ResourceInfo getactiveResources()
	{
		return this.activeResources;
	}
	public void setactiveResources(ResourceInfo activeRes)
	{
		this.activeResources = activeRes;
	}
	public ResourceInfo getavailableResources()
	{
		return this.availableResources;
	}	
	public void setavailableResources(ResourceInfo availRes)
	{
		this.availableResources = availRes;
	}	
	
	public boolean incrementResources(ResourceInfo resources)
	{
		// Increment the assigned Resources &
		// Decrement the available Resources
		if(resources.numCores <= this.availableResources.numCores)
		{
			this.activeResources.numCores = this.activeResources.numCores + resources.numCores;
			this.availableResources.numCores = this.availableResources.numCores - resources.numCores;
			return true;
		}
		return false;
			
	}
	public boolean decerementResouces(ResourceInfo resources)
	{
		// Increment the available Resources &
		// Decrement the Assigned Resources
		if(resources.numCores <= this.activeResources.numCores)
		{
			this.activeResources.numCores = this.activeResources.numCores - resources.numCores;
			this.availableResources.numCores = this.availableResources.numCores + resources.numCores;
			return true;
		}
		return false;
	}
	public boolean incrementAvailableResource(ResourceInfo resources)
	{
		if(resources.numCores<=this.activeResources.getNumberOfCores())
		{
			this.availableResources.numCores = this.availableResources.numCores + resources.numCores;
			return true;
		}
		return false;
	}
	public boolean decrementAvailableResource(ResourceInfo resources)
	{
		if(resources.numCores<=this.availableResources.getNumberOfCores())
		{
			this.availableResources.numCores = this.availableResources.numCores - resources.numCores;
			return true;
		}
		return false;		
	}
	public String getContainerStatus()
	{
		return this.containerStatus;
	}
	public void setContainerStatus(String inpStatus)
	{
		this.containerStatus = inpStatus;
	}
}

