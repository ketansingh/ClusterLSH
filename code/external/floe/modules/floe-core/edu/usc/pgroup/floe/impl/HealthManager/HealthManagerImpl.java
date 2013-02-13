package edu.usc.pgroup.floe.impl.HealthManager;

import java.beans.XMLEncoder;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.JAXBElement;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;
//import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;

import edu.usc.pgroup.floe.api.framework.ContainerInfo;
import edu.usc.pgroup.floe.api.framework.Flake;
import edu.usc.pgroup.floe.api.framework.FlakeInfo;
import edu.usc.pgroup.floe.api.framework.HealthAnalyzer;
import edu.usc.pgroup.floe.api.framework.HealthManager;
import edu.usc.pgroup.floe.api.framework.HealthManagerEvent;
import edu.usc.pgroup.floe.api.framework.HealthManagerEventListener;
import edu.usc.pgroup.floe.api.framework.PerfStats;
import edu.usc.pgroup.floe.api.framework.ResourceInfo;
import edu.usc.pgroup.floe.api.framework.StartFloeInfo;
import edu.usc.pgroup.floe.impl.communication.RestSourceChannel;
import edu.usc.pgroup.floe.util.Logger;

public class HealthManagerImpl implements HealthManager, HealthManagerEventListener {

	private static final int RECENT_HISTORY_SIZE = 6;
	private static HealthManagerImpl instance;
	private static RestSourceChannel restChannel;
	
	private Thread analyzerThread;

	private Map<String, String> containersMap;
	
	//private Hashtable perfInfoAll = new Hashtable();
	
	private ConcurrentHashMap<String, List<PerfStats>> perfInfoAll = new ConcurrentHashMap<String, List<PerfStats>>();
	
	private HealthAnalyzer analyzer;
	
	public void ReportStatus(PerfStats status) {
		AddToHistory(status);
	}
	
	public PerfStats getLatestContainerPerfInfo(String containerID) {
		LinkedList<PerfStats> stats = (LinkedList<PerfStats>) perfInfoAll.get(containerID);
		
		if(stats!=null)
		{
			//System.out.println(stats.size());
			return stats.getFirst();
		}
		else{
			return null;
		}
	}

	public List<PerfStats> getLatestGlobalPerfInfo() {
		
		Enumeration e = perfInfoAll.keys();
		List<PerfStats> perfInfoAll = new ArrayList<PerfStats>();
		while (e.hasMoreElements()) {
			//System.out.println("*");
			String containerID = (String) e.nextElement();			
			
			PerfStats latestStats = getLatestContainerPerfInfo(containerID);
			if(latestStats != null)
				perfInfoAll.add(latestStats);
			
			//System.out.println("##");
		}
		
		return perfInfoAll;
	}
	
	//private functions
	public static synchronized HealthManagerImpl getInstance()
	{
		if(instance!=null)
		{
			return instance;		
		}
		else
		{								
			instance = new HealthManagerImpl();
			return instance;
		}
			// Send an Acknowledgment to the Manager			
	}
	
	private String getHostAddress()
	{
		// Problem with getting actual IP address visible to outside world
		// Simple InetAddress.getHostAddress would give the IP in WIndows but not in Ubuntu
		// It gives 127.0.0.1 in Ubuntu. 
		// A Hackish Attempt works in all cases
		Socket socket;
		try 
		{
			socket = new Socket("google.com", 80);
			InetAddress addr = socket.getLocalAddress();
	        String hostAddr = addr.getHostAddress();
	        System.out.println("Addr: " + hostAddr);
	        socket.close();
	        return hostAddr.trim();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			return "Error";			
		}        
	}	
	
	
	private HealthManagerImpl()
	{
		try
		{
			
			String monitorID = "GlobalResourceMonitor@" + getHostAddress();			
		
			//System.out.println("HERE1");
			
			if(restChannel == null)
				restChannel = new RestSourceChannel("edu.usc.pgroup.floe.impl",getBaseURI());			
			restChannel.openConnection();	
						
			
			containersMap = new HashMap<String, String>();
			
			
			analyzer = new HealthAnalyzerImpl(perfInfoAll);			
			analyzer.addHealthManagerEventListener(this);
			analyzerThread = new Thread((HealthAnalyzerImpl)analyzer);
			analyzerThread.start();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}		 		
	}
	
	private static URI getBaseURI() 
	{
        return UriBuilder.fromUri("http://localhost/").port(45003).build();
    }
	
	private void AddToHistory(PerfStats status) {
		
		String containerId = status.getContainerID();
		if(!perfInfoAll.containsKey(containerId))
		{
			perfInfoAll.put(containerId, new LinkedList<PerfStats>());
		}
		AddToContainerRecentHistory(containerId,status);
	}
	
	private void AddToContainerRecentHistory(String containerId,
			PerfStats status) {

		LinkedList<PerfStats> stats = (LinkedList<PerfStats>) perfInfoAll.get(containerId);
		while(stats.size() >= RECENT_HISTORY_SIZE){
			stats.removeFirst();
		}			
		
		
		double totalCPU = status.getCPUStats().getTotalUsage();
		int totalCores = status.getCPUStats().getNoOfCores();
		FlakeInfo[] flakeStats = status.getFlakeBufferStats();
		Date timeStamp = status.getTimeStamp();
		
		if(flakeStats != null)
		{
			for(int i = 0; i < flakeStats.length; i++){
				
				FlakeInfo flStats = status.getFlakeBufferStats()[i];
				
				String fid = flStats.getflakeId();
				
				int numAllocatedCores = flStats.getResourceInfo().getNumberOfCores();
				double effectiveCPU = totalCPU*totalCores/numAllocatedCores;
			
				int inputBufferLength = flStats.getInBufferSize();
				int outputBufferLength = flStats.getOutBufferSize();
				double latency = flStats.getAveragePelletProcessingLatency();
				int allocatedPellets = flStats.getPelletCount();
				int allocatedCores = flStats.getResourceInfo().getNumCores();
				Logger.getInstance().LogInfo(containerId, fid, timeStamp, latency, totalCPU, inputBufferLength, outputBufferLength, allocatedCores, allocatedPellets);			
			}
		}
		

		//Assuming one flake per container..
		//And also that we cannot find flake to core mapping.. :(
		
		
		

		
		stats.addLast(status);
	}

	
	
	@Override
	//override from HealManagerEventListener.. 
	public void CriticalEventOccurred(HealthManagerEvent evt) {
		//Firing NOW..
		
		//System.out.println("firing event recieved.. firing now..");
		String containerID = evt.getContainerID();
		String flakeID = evt.getFlakeID();
		ReactiveActions suggestedAction = evt.getSuggestedAction();
		
		String containerIP = getContainerIP(containerID);
		
		String baseURI = "http://"+containerIP+":45002/Container/";
		String command = "";
		switch(suggestedAction)
		{
		case IncreasePelletCount:
			command = "increasePelletCount/FlakeID="+flakeID+"/Size=1";
			break;
		case DecreasePelletCount:
			command = "decreasePelletCount/FlakeID="+flakeID+"/Size=1";
			break;
		case IncreaseCoreAllocation:
			command = "increaseCore/FlakeID="+flakeID+"/Size=1";
			break;
		case DecreaseCoreAllocation:
			command = "decreaseCore/FlakeID="+flakeID+"/Size=1";
			break;
		case ReallocateFlake:
			break;
		}
		
		String uri = baseURI + command;
		Client c = Client.create();
		WebResource r = c.resource(uri);
		
		c.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);

		ClientResponse response;
		response = r.put(ClientResponse.class);
		
		
		//GenericType<String> retString = new GenericType<String>() {};			
		String retStatus = response.getEntity(String.class);
		
		
		Logger.getInstance().LogInfo(containerID, flakeID, suggestedAction, Boolean.parseBoolean(retStatus)); 
	}

	private String getContainerIP(String containerID) {
		int idx = containerID.indexOf('@');		
		return containerID.substring(idx + 1);
	}

	public Map<String, String> getContainers() {
		return containersMap;
	}

	public void registerContainer(String containerID, String ip) {
		containersMap.put(containerID, ip);
		//TODO: Fork a thread to notify all other containers.. 
	}
}

