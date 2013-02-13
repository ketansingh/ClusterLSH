package edu.usc.pgroup.floe.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import edu.usc.pgroup.floe.api.framework.ContainerInfo;
import edu.usc.pgroup.floe.api.framework.rest.RestContainerResourceInfo;
import edu.usc.pgroup.floe.api.framework.rest.RestResourcePellet;
@Resource
@Provider
@Path("/Manager")
public class ManagerRestHandler
{
	@GET
	@Produces("text/plain")
    public String welcomeMessage() 
	{
         // Return some cliched textual content    	
        return "Manager is Up and Running";
    }
	@Path("/allocateResources")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public RestContainerResourceInfo allocateResources(RestResourcePellet resPellet) 
	{						
		System.out.println("Allocate Resources Invoked");
		ManagerImpl manager = ManagerImpl.getManager();
		
		HashMap<String,ContainerInfo> containerHash = manager.allocateResources(resPellet.getPelletList(),resPellet.getResourceList());
		System.out.println("Manager Finished Allocation ");
		RestContainerResourceInfo tempContainerObj = new RestContainerResourceInfo();
		Set<Entry<String,ContainerInfo>> tempEntrySet = containerHash.entrySet();
		List<String> tempNodeList = new ArrayList<String>();
		List<ContainerInfo> tempContainerList = new ArrayList<ContainerInfo>();
		for(Entry<String,ContainerInfo> tempEntry:tempEntrySet)
		{
			tempNodeList.add(tempEntry.getKey());
			tempContainerList.add(tempEntry.getValue());
		}
		tempContainerObj.setnodeList(tempNodeList);
		tempContainerObj.setcontainerList(tempContainerList);		
		return tempContainerObj;
        
    }
	@Path("/deallocateResources")
	@PUT
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces("text/plain")
    public String deallocateResources() 
	{		
		// Return some cliched textual content    	
        return "Resource DeAllocated";
    }
	@Path("/addContainerInfo/Container={containerID}")
	@GET	
	@Produces("text/plain")
    public String addContainerInfo(@PathParam("containerID") String containerID) 
	{		
		// Return some cliched textual content		
		ManagerImpl refManager = ManagerImpl.getManager();	
		refManager.addContainerInfo(containerID);
        return "Container Info Added - " + containerID;
    }
	@Path("/listContainers")
	@GET			
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<ContainerInfo> listContainers()
	{
		try
		{
			ManagerImpl refManager = ManagerImpl.getManager();
			List<ContainerInfo> containerList = refManager.listContainers();			
			return containerList;			
		}
		catch(Exception e)	
		{
			
		}
		return null;
	}
	
}
