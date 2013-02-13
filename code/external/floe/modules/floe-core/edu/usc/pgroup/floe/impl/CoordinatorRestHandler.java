package edu.usc.pgroup.floe.impl;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import edu.usc.pgroup.floe.api.framework.FloeGraph;
import edu.usc.pgroup.floe.api.framework.StartFloeInfo;
import edu.usc.pgroup.floe.api.framework.rest.RestFloeInfo;

@Provider
@Path("/Coordinator")
public class CoordinatorRestHandler
{	
	@GET
	@Produces("text/plain")
    public String welcomeMessage() 
	{
         // Return some cliched textual content    	
        return "Container is Up and Running";
    }	
	@POST	
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Path("/createFloe")	
    public StartFloeInfo createFloe(RestFloeInfo floeInfo ) 
	{
		FloeGraph currGraph = new FloeGraph(floeInfo.getNodeList().getNodeList(),floeInfo.getEdgeList().getEdgeList());
		CoordinatorImpl tempCoordinator = CoordinatorImpl.getCoordinator();
		StartFloeInfo tempFloeOutInfo =  tempCoordinator.createFloe(currGraph,null,null);
		System.out.println("\n --- Floe Graph Created Successfully ---");
		System.out.println("\n --- Starting Flakes Now--- ");
		tempCoordinator.startFloe(tempFloeOutInfo.getFloeID());
		
		if(tempFloeOutInfo != null)
		{
			return tempFloeOutInfo;			
		}			
		else
			return new StartFloeInfo();
    }
	@POST	
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Path("/startFloe")	
    public StartFloeInfo startFloe(RestFloeInfo floeInfo ) 
	{
		FloeGraph currGraph = new FloeGraph(floeInfo.getNodeList().getNodeList(),floeInfo.getEdgeList().getEdgeList());
		CoordinatorImpl tempCoordinator = CoordinatorImpl.getCoordinator();
		StartFloeInfo tempFloeOutInfo =  tempCoordinator.createFloe(currGraph,null,null);
		System.out.println("\n --- Floe Graph Created Successfully ---");
		System.out.println("\n --- Starting Flakes Now--- ");
		tempCoordinator.startFloe(floeInfo.getFloeID());
		if(tempFloeOutInfo != null)
		{
			return tempFloeOutInfo;			
		}			
		else
			return new StartFloeInfo();
    }
}
