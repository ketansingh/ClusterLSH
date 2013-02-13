package edu.usc.pgroup.floe.impl.HealthManager;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.MultivaluedMap;

import org.biojava3.core.sequence.StartCodonSequence;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class ContainerNotifier implements Runnable{

	class ContainerEntry
	{
		public String id;
		public String ip;
	}
	
	private BlockingQueue<ContainerEntry> newContainers;
	private Thread notifierThread;
	
	private static ContainerNotifier instance;
	public static ContainerNotifier getInstance() {
		if(instance!=null)
		{
			return instance;		
		}
		else
		{								
			instance = new ContainerNotifier();
			
			return instance;
		}
	}
	
	private ContainerNotifier()
	{
		newContainers = new LinkedBlockingQueue<ContainerEntry>();
		notifierThread = new Thread(this);
		notifierThread.start();
	}

	public void AddToNotifierList(String containerID, String ip) {
		ContainerEntry entry = new ContainerEntry();
		entry.id = containerID;
		entry.ip = ip;
		
		newContainers.add(entry);
	}

	@Override
	public void run() {
		while(true)
		{
			ContainerEntry entry = null;
			try {
				entry = newContainers.poll(10,TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(entry == null) continue;
			
			HealthManagerImpl refMonitor = HealthManagerImpl.getInstance();
			HashMap containers;
			HashMap containersCopy;
			
			synchronized(refMonitor)
			{		
				containers = (HashMap)refMonitor.getContainers();
				containersCopy = (HashMap) containers.clone(); 	//todo see if this is correct or we need deep copy..
			}
			
			for(Object keyo : containersCopy.keySet())
			{
				String key = (String) keyo;
				String ip = (String) containersCopy.get(key);
				
				//call notify for these containers.. 
				Client rclient = Client.create();
				String rurl = "http://"+ip+":45002/Container/addToContainersTopology";
				WebResource rwebRes = rclient.resource(rurl);
				
				//TODO: Check for self.. ?
				
				MultivaluedMap queryParams = new MultivaluedMapImpl();
				queryParams.add("containerID", key);
				queryParams.add("ip", ip);
				   
				rwebRes.queryParams(queryParams).post();
			}
		}
	}

	 
}
