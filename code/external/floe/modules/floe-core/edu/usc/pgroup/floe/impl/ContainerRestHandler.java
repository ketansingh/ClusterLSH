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
package edu.usc.pgroup.floe.impl;

/***
 * 
 * @author Sreedhar Natarajan (sreedhan@usc.edu)
 * @author Yogesh Simmhan (simmhan@usc.edu)
 * @version v0.1, 2012-25-03
 *
 */

import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import edu.usc.pgroup.floe.api.framework.ContainerInfo;
import edu.usc.pgroup.floe.api.framework.FlakeInfo;
import edu.usc.pgroup.floe.api.framework.ResourceInfo;
import edu.usc.pgroup.floe.api.framework.rest.RestFlakeCreationInfo;
import edu.usc.pgroup.floe.api.framework.rest.RestFlakeWiringInfo;
import edu.usc.pgroup.floe.impl.HealthManager.PeriodicHealthCheck;
@Resource
@Provider
@Path("/Container")
public class ContainerRestHandler
{
	@GET
	@Produces("text/plain")
    public String welcomeContainer() 
	{
         // Return some cliched textual content    	
        return "Container is Up and Running";
    }
	@GET
	@Path("/getContainerInfo")	
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public ContainerInfo getContainerInfo( ) 
	{
		ContainerImpl<?> refContainer = ContainerImpl.getContainer();		
		return refContainer.getContainerInfo();
    }
	@POST	
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Path("/createFlake")	
    public FlakeInfo createFlake(RestFlakeCreationInfo flakeCreationInfo) 
	{
		ContainerImpl<?> refContainer = ContainerImpl.getContainer();
		FlakeInfo newFlake = refContainer.createFlake(flakeCreationInfo.getFlakeID(),flakeCreationInfo.getPellet(), flakeCreationInfo.getResource(),flakeCreationInfo.getInChannel(),flakeCreationInfo.getOutChannel(),flakeCreationInfo.getInputPorts(),flakeCreationInfo.getOutputPorts());
		if(newFlake != null)
		{
			return newFlake;
		}			
		else {		
			return new FlakeInfo();
		}
    }
	@PUT
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Path("/wireFlake")
	@Produces("text/plain")
	 public void wireFlake(RestFlakeWiringInfo flakeWiringInfo ) 
	{
		ContainerImpl refContainer = ContainerImpl.getContainer();
		refContainer.wireFlake(flakeWiringInfo);
	}
	
	@GET
	@Path("/listFlakes")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })	
	public List<FlakeInfo> listFlakes()
	{
		try
		{
			ContainerImpl refContainer = ContainerImpl.getContainer();
			return  refContainer.listFlakes();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	@PUT
	@Path("/increaseCore/FlakeID={flakeID}/Size={coreSize}")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces("text/plain")	
	public String increaseCore(@PathParam("flakeID")String flakeID,@PathParam("coreSize")String coreSize)
	{
		try
		{
			System.out.println("Request for Increase in Core Size " + flakeID);
			
			ContainerImpl refContainer = ContainerImpl.getContainer();
			ResourceInfo tempResourceInfo = new ResourceInfo();
			tempResourceInfo.setNumberOfCores(Integer.parseInt(coreSize));
			boolean accpeted = refContainer.modifyFlakeCore(flakeID, tempResourceInfo);
			
			return String.valueOf(accpeted);	
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return "false";
	}	
	@PUT
	@Path("/decreaseCore/FlakeID={flakeID}/Size={coreSize}")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces("text/plain")	
	public String decreaseCore(@PathParam("flakeID")String flakeID,@PathParam("coreSize")String coreSize)
	{
		try
		{
			System.out.println("Request for Decrease in Core Size" + flakeID);
			ContainerImpl refContainer = ContainerImpl.getContainer();
			ResourceInfo tempResourceInfo = new ResourceInfo();
			tempResourceInfo.setNumberOfCores(Integer.parseInt(coreSize)*-1);
			boolean accpeted = refContainer.modifyFlakeCore(flakeID, tempResourceInfo);
			return String.valueOf(accpeted);	
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return "false";
	}
	@PUT
	@Path("/increasePelletCount/FlakeID={flakeID}/Size={pelletCount}")
	@Produces("text/plain")	
	public String increasePelletCount(@PathParam("flakeID")String flakeID,@PathParam("pelletCount")String pelletCount)
	{
		boolean requestedAccepted = false;
		try
		{
			System.out.println("Request for Increase in Pellet Count " + flakeID);
			ContainerImpl refContainer = ContainerImpl.getContainer();
			requestedAccepted = refContainer.modifyPelletCount(flakeID, Integer.parseInt(pelletCount));
			return String.valueOf(requestedAccepted);	
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return "false";
	}
	@PUT
	@Path("/decreasePelletCount/FlakeID={flakeID}/Size={pelletCount}")
	@Produces("text/plain")	
	public String decreasePelletCount(@PathParam("flakeID")String flakeID,@PathParam("pelletCount")String pelletCount)
	{
		boolean requestedAccepted = false;
		try
		{
			System.out.println("Request for decrease in Pellet Count " + flakeID);
			ContainerImpl refContainer = ContainerImpl.getContainer();
			requestedAccepted = refContainer.modifyPelletCount(flakeID, Integer.parseInt(pelletCount)*-1);
			return String.valueOf(requestedAccepted);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return "false";
	}
	
	@PUT
	@Path("/stopFlake/FlakeID={flakeID}")
	@Produces("text/plain")
	public String stopFlake(@PathParam("flakeID") String flakeID)
	{
		System.out.println("Trying to Stop Flake " + flakeID);
		ContainerImpl refContainer = ContainerImpl.getContainer();
		refContainer.stopFlake(flakeID);
		return flakeID + " Stopped";
	}
	
	@PUT
	@Path("/startFlake/FlakeID={flakeID}")
	@Produces("text/plain")
	public String startFlake(@PathParam("flakeID") String flakeID)
	{
		ContainerImpl refContainer = ContainerImpl.getContainer();
		refContainer.startFlake(flakeID);
		
		return flakeID + " Started";
	}
	
	@PUT
	@Path("/pauseFlake/FlakeID={flakeID}")
	@Produces("text/plain")
	public String pauseFlake(@PathParam("flakeID") String flakeID)
	{
		ContainerImpl refContainer = ContainerImpl.getContainer();
		refContainer.pauseFlake(flakeID);
		return flakeID + " Paused";
	}
	
	@PUT
	@Path("/resumeFlake/FlakeID={flakeID}")
	@Produces("text/plain")
	public String resumeFlake(@PathParam("flakeID") String flakeID)
	{
		ContainerImpl refContainer = ContainerImpl.getContainer();
		refContainer.resumeFlake(flakeID);
		return flakeID + " Resumed";
	}
	@PUT
	@Path("/stopContainer")
	@Produces("text/plain")
	public String stopContainer()
	{
		//ContainerImpl refContainer = ContainerImpl.getContainer();
		// Should write a method to remove all the Flakes and destroy the container
		// itself
		return "Container Stopped";
	}
	
	
	@POST	
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("/addToContainersTopology")//?ContainerID={containerID}&IP={ip}	
    public void AddToContainersTopology(@QueryParam("containerID") String containerID, @QueryParam("ip") String ip)
    {
		PeriodicHealthCheck.AddToContainersTopology(containerID,ip);
    }
}
