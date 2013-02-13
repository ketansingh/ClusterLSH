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

import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.core.UriBuilder;

import edu.usc.pgroup.floe.api.communication.SinkChannel;
import edu.usc.pgroup.floe.api.communication.SourceChannel;
import edu.usc.pgroup.floe.api.framework.Container;
import edu.usc.pgroup.floe.api.framework.ContainerInfo;
import edu.usc.pgroup.floe.api.framework.Flake;
import edu.usc.pgroup.floe.api.framework.FlakeInfo;
import edu.usc.pgroup.floe.api.framework.FloeGraph.Edge;
import edu.usc.pgroup.floe.api.framework.Port;
import edu.usc.pgroup.floe.api.framework.ResourceInfo;
import edu.usc.pgroup.floe.api.framework.rest.RestFlakeWiringInfo;
import edu.usc.pgroup.floe.impl.HealthManager.LocalResourceMonitor;
import edu.usc.pgroup.floe.impl.HealthManager.PeriodicHealthCheck;
import edu.usc.pgroup.floe.impl.communication.RestSourceChannel;
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
public class ContainerImpl<T> implements Container
{
	// Should think of replaceing the List<Flake> to HashMap<String,Flake>
	static ContainerImpl<?> container;
	ContainerInfo containerInfo;
	List<Flake> flakeList;
	public static URI BASE_URI = getBaseURI();
	public static RestSourceChannel restChannel;			
	ContainerImpl()
	{
		// Make a Web Server Start and Listen for incoming connections
		// Assign the ContainerID as the ip address of the system
		try
		{
			this.flakeList = new ArrayList<Flake>();
			String hostIp = getHostAddress();
			String containerID = "Container@" + hostIp;
			
			ResourceInfo available = new ResourceInfo();
			ResourceInfo allocated = new ResourceInfo();
			available.setNumberOfCores(30*Runtime.getRuntime().availableProcessors());
			allocated.setNumberOfCores(0);
			this.containerInfo = new ContainerInfo(containerID,available,allocated);
			if(restChannel == null)
				restChannel = new RestSourceChannel("edu.usc.pgroup.floe.impl",getBaseURI());			
			restChannel.openConnection();	
			
//			LocalResourceMonitor.setMonitorFlags(LocalResourceMonitor.ALL);
//			LocalResourceMonitor.setFlakeList(flakeList);
//			LocalResourceMonitor.setStatInterval(1000 * 5);
//			LocalResourceMonitor.StartMonitor(containerID);
			
			/*
			PeriodicHealthCheck.setStatInterval(15 * 1000 * 60);
			PeriodicHealthCheck.setHostIp(hostIp);
			PeriodicHealthCheck.StartMonitor(containerID);*/
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}		 		
	}
	
	public static synchronized void SetMonitorLocation(String monitorLocation)
	{
		LocalResourceMonitor.setGlobalMonitorLocation(monitorLocation);
		PeriodicHealthCheck.setGlobalMonitorLocation(monitorLocation);
	}
	
	public static synchronized ContainerImpl getContainer()
	{
		if(container!=null)
		{
			return container;		
		}
		else
		{								
			container = new ContainerImpl();				
			return container;
		}
			// Send an Acknowledgment to the Manager			
	}
	private static URI getBaseURI() 
	{
        return UriBuilder.fromUri("http://localhost/").port(45002).build();
    }
	public String getHostAddress()
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
	@Override
	public FlakeInfo createFlake(String inpFlakeID,String pelletType, ResourceInfo resources,List<Edge> inpChannel,List<Edge> outChannel,List<Port> inpPorts,List<Port> outPorts)
	{		
		synchronized(container)
		{
			FlakeImpl<T> newFlake = new FlakeImpl<T>();
			if(this.containerInfo.incrementResources(resources))
			{				
				newFlake.create(inpFlakeID,pelletType, resources,inpChannel,outChannel,inpPorts,outPorts);
				this.flakeList.add(newFlake);				
				return newFlake.flakeInfo;	
			}
			return null;				
		}
		
	}
	public void wireFlake(RestFlakeWiringInfo flakeWiringInfo)
	{
		synchronized(container)
		{
			Iterator<Flake> flakeIter = flakeList.iterator();
			while(flakeIter.hasNext())
			{
				Flake tempFlake = flakeIter.next();
				if(tempFlake.getFlakeId().matches(flakeWiringInfo.getFlakeID()))
				{
					tempFlake.wire(flakeWiringInfo.getConnectionList());
				}
			}	
		}		
	}
	public synchronized boolean modifyFlakeCore(String flakeID,ResourceInfo inpResource)
	{
		try
		{					
			Iterator<Flake> flakeIter = this.flakeList.iterator();
			Flake tempFlake = null;
			while(flakeIter.hasNext())
			{
				tempFlake = flakeIter.next();
				if(tempFlake.getFlakeId().matches(flakeID))
				{
					break;
				}
				else
					tempFlake = null;
			}
			if(tempFlake!=null)
			{
				int aggregateCount = tempFlake.getResources().getNumCores() + inpResource.getNumberOfCores();
				int totalCoresAllocated = 0;
				int coreAvailable = this.containerInfo.getactiveResources().getNumberOfCores() + this.containerInfo.getavailableResources().getNumberOfCores();
				flakeIter = this.flakeList.iterator();
				Flake tempFlakeNew = null;
				while(flakeIter.hasNext())
				{
					tempFlakeNew = flakeIter.next();
					totalCoresAllocated += tempFlakeNew.getResources().getNumberOfCores();
				}	
				if(aggregateCount > 0 &&aggregateCount < coreAvailable)
				{
					tempFlake.modifyResource(inpResource);
				}
				else
				{
					System.out.println("Request Violates the Resource Allocation Specifications");
					return false;
				}
			}				
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			return false;
		}				
		return true;
	}	
	public synchronized boolean modifyPelletCount(String flakeID,int inpCount)
	{
		try
		{
			int tempCount =0;
			Iterator<Flake> flakeIter = this.flakeList.iterator();
			Flake tempFlake = null;
			while(flakeIter.hasNext())
			{
				tempFlake = flakeIter.next();
				if(tempFlake.getFlakeId().matches(flakeID))
				{
					break;
				}
				else
					tempFlake = null;
				tempCount ++;
			}
			if(tempFlake!=null)
			{				
				int aggregateCount = tempFlake.getFlakeInfo().getPelletCount() + inpCount;
				if((aggregateCount >0)&& (aggregateCount < tempFlake.getResources().getNumberOfCores()*4))
				{
					tempFlake.modifyPelletCount(inpCount);
					this.flakeList.set(tempCount, tempFlake);
				}
				else
				{
					System.out.println("Pellet Count Not Modified. Violates Resource Allocation Conditions.");
					return false;
				}
			}								
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			return false;
		}
		return true;
	}	
	@Override
	public synchronized void startFlake(String flakeID)
	{
		
		Iterator<Flake> flakeIter = this.flakeList.iterator();
		Flake tempFlake = null;
		while(flakeIter.hasNext())
		{
			tempFlake = flakeIter.next();
			if(tempFlake.getFlakeId().matches(flakeID))
			{
				break;
			}
			else
				tempFlake = null;
		}
		if(tempFlake!=null)
		{
			List<SourceChannel> inList = new ArrayList<SourceChannel>();
			List<SinkChannel> outList = new ArrayList<SinkChannel>();			
			tempFlake.startFlake(inList, outList);
		}									
	}
	@Override
	public synchronized void stopFlake(String flakeID)
	{		
		Iterator<Flake> flakeIter = this.flakeList.iterator();
		Flake tempFlake = null;
		while(flakeIter.hasNext())
		{
			tempFlake = flakeIter.next();
			if(tempFlake.getFlakeId().matches(flakeID))
			{
				break;
			}
			else
				tempFlake = null;
		}
		if(tempFlake!=null)
		{				
			tempFlake.stopFlake();
			this.containerInfo.decerementResouces(tempFlake.getResources());
			return;
		}			
						
	}	
	@Override
	public synchronized void pauseFlake(String flakeID)
	{
		Iterator<Flake> flakeIter = this.flakeList.iterator();
		Flake tempFlake = null;
		while(flakeIter.hasNext())
		{
			tempFlake = flakeIter.next();
			if(tempFlake.getFlakeId().matches(flakeID))
			{
				break;
			}
			else
				tempFlake = null;
		}
		if(tempFlake!=null)
		{				
			tempFlake.pauseFlake();
			return;
		}									
	}
	@Override
	public synchronized void resumeFlake(String flakeID)
	{		
		Iterator<Flake> flakeIter = this.flakeList.iterator();
		Flake tempFlake = null;
		while(flakeIter.hasNext())
		{
			tempFlake = flakeIter.next();
			if(tempFlake.getFlakeId().matches(flakeID))
			{
				break;
			}
			else
				tempFlake = null;
		}
		if(tempFlake!=null)
		{				
			tempFlake.resumeFlake();
			return;
		}										
	}
	
	@Override
	public List<FlakeInfo> listFlakes()
	{
		List<FlakeInfo> flakeInfoList = new ArrayList<FlakeInfo>();
		Iterator<Flake> flakeIter = this.flakeList.iterator();
		while(flakeIter.hasNext())
		{
			Flake tempFlake = flakeIter.next();
			if(tempFlake.getFlakeInfo()!=null)
				flakeInfoList.add(tempFlake.getFlakeInfo());
		}
		return flakeInfoList;
	}
	public ContainerInfo getContainerInfo()
	{
		return this.containerInfo;
	}

	
}
