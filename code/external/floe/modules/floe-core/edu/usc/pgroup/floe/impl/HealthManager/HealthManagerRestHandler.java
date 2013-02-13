package edu.usc.pgroup.floe.impl.HealthManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import com.sun.jersey.core.util.MultivaluedMapImpl;

import edu.usc.pgroup.floe.api.framework.ContainerInfo;
import edu.usc.pgroup.floe.api.framework.FlakeInfo;
import edu.usc.pgroup.floe.api.framework.PerfStats;
import edu.usc.pgroup.floe.api.framework.rest.RestFlakeInfo;
import edu.usc.pgroup.floe.api.framework.rest.RestFlakeWiringInfo;
import edu.usc.pgroup.floe.api.framework.rest.RestPerfInfo;

@Resource
@Provider
@Path("/GlobalResourceMonitor")
public class HealthManagerRestHandler {
	@GET
	@Produces("text/plain")
    public String welcomeContainer() 
	{
         // Return some cliched textual content    	
        return "Global Resource Monitor is Up and Running";
    }
	
	@POST
	@Path("/reportStatus")	
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	//@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public void ReportStatus(JAXBElement<PerfStats> perfStatsEle) 
	{
		HealthManagerImpl refContainer = HealthManagerImpl.getInstance();		
		refContainer.ReportStatus(perfStatsEle.getValue());
    }
	
	public void ReportNetworkLatency(JAXBElement<PerfStats> perfStatsEle)
	{
		
	}

	//TODO: Return map of registered nodes and their IPs..
	//public JAXBElement<Map<String,String>> RegisterContainer(@QueryParam("ContainerId") final String containerID,@QueryParam("IP") final String ip)
	@POST
	@Path("/registerContainer")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_PLAIN)
	//public JAXBElement<HashMap> RegisterContainer(@QueryParam("ContainerId") final String containerID,@QueryParam("IP") final String ip)
	public String RegisterContainer(@QueryParam("ContainerId") final String containerID,@QueryParam("IP") final String ip)
	{		
		
		//Lock this		
		try
		{
			HealthManagerImpl refMonitor = HealthManagerImpl.getInstance();
			HashMap previousContainers;
			
			synchronized(refMonitor)
			{
				refMonitor.registerContainer(containerID, ip);			
				previousContainers = (HashMap)refMonitor.getContainers();
			}
			
			//add to the notify list..  
			ContainerNotifier notifier = ContainerNotifier.getInstance();
			notifier.AddToNotifierList(containerID,ip);
			
			
			return previousContainers.toString();
			//return "rest";
						
		}
		catch(Exception e)
		{
			
		}
		return null;
	}
	
	@GET
	@Path("/getLatestContainerPerfInfo/ContainerId={containerID}")	
	//@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	//@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public JAXBElement<PerfStats> GetLatestContainerPerfInfo(@PathParam("containerID")String containerID) 
	{
		try
		{
			HealthManagerImpl refMonitor = HealthManagerImpl.getInstance();
			PerfStats stats = refMonitor.getLatestContainerPerfInfo(containerID);
						
			return new JAXBElement<PerfStats>(new QName("PerfStats"), PerfStats.class, stats);
		}
		catch(Exception e)
		{
			
		}
		return null;
    }
	
	@GET
	@Path("/getLatestPerfInfo/")	
	//@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	//@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public JAXBElement<RestPerfInfo> GetLatestPerfInfo() 
	{
		//System.out.println("GetLatestPerfInfo requested..");
		try
		{
			HealthManagerImpl refMonitor = HealthManagerImpl.getInstance();
			List<PerfStats> stats = refMonitor.getLatestGlobalPerfInfo();
			RestPerfInfo perfInfo = new RestPerfInfo();
			perfInfo.setPerfStatsList(stats);
			
			//System.out.println("Returning perfInfo..");	
			return new JAXBElement<RestPerfInfo>(new QName("RestPerfInfo"), RestPerfInfo.class, perfInfo);
		}
		catch(Exception e)
		{
			
		}
		//System.out.println("Returning null..");	
		return new JAXBElement<RestPerfInfo>(new QName("RestPerfInfo"), RestPerfInfo.class, null);
    }	
}
