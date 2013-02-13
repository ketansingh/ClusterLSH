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
* This manages the Containers and their resources within a region.
 * 
 * @author Sreedhar Natarajan (sreedhan@usc.edu)
 * @author Yogesh Simmhan (simmhan@usc.edu)
 * @version v0.1, 2012-01-11
 *
 */

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.core.UriBuilder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import edu.usc.pgroup.floe.api.framework.ContainerInfo;
import edu.usc.pgroup.floe.api.framework.Manager;
import edu.usc.pgroup.floe.api.framework.ResourceInfo;
import edu.usc.pgroup.floe.impl.communication.RestSourceChannel;
import edu.usc.pgroup.floe.util.EucalyptusAccountInfo;
import edu.usc.pgroup.floe.util.EucalyptusInstance;

public class ManagerImpl extends Thread implements Manager
{
	static List<ContainerInfo> containerList;
	
	// The Rest Protocol is used by default to perform exchange of data
	// between Manager and Container
	// Can it be generalized to allow any sort of Communiaction Channel.???
	static ManagerImpl manager;
	public static URI BASE_URI = getBaseURI();
	public static EucalyptusAccountInfo newAccountInfo;
	public List<EucalyptusInstance> availableInstanceTypes;
	TreeMap<String,EucalyptusInstance> requestedContainers;	// HashMap of IP Addresses of requested containers
	Lock containerUpdate;
	
	ManagerImpl(InputStream eucaProperties,List<EucalyptusInstance> instanceType) 
	{ 
		RestSourceChannel restChannel = new RestSourceChannel("edu.usc.pgroup.floe.impl",BASE_URI);
		restChannel.openConnection();
		containerList = new ArrayList<ContainerInfo>();
		newAccountInfo = new EucalyptusAccountInfo(eucaProperties);		
		this.availableInstanceTypes = instanceType;
		requestedContainers = new TreeMap<String,EucalyptusInstance>();
	}
	ManagerImpl()
	{
		
	}
	public void setEucalyptus(InputStream eucaProperties,List<EucalyptusInstance> instanceType)
	{
		newAccountInfo = new EucalyptusAccountInfo(eucaProperties);		
		this.availableInstanceTypes = instanceType;
	}
	public static String generateGMTTimeStamp()	
	{
		try
		{
			SimpleDateFormat gmtDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			gmtDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");	
			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
			SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
			timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
			Date date = new Date();
			return dateFormat.format(date)+ "T" + URLEncoder.encode(timeFormat.format(date),"UTF-8");
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return null;
	}
	// Only required During Intial Creation When Cloud Level Details are necessary
	public synchronized static ManagerImpl getManager(InputStream eucaProperties,List<EucalyptusInstance> instanceType)
	{
		if(manager==null)
			manager = new ManagerImpl(eucaProperties,instanceType);
		return manager;
	}
	
	public synchronized static ManagerImpl getManager()
	{
		if(manager==null)
			manager = new ManagerImpl();
		return manager;
	}	
	private static URI getBaseURI() 
	{
        return UriBuilder.fromUri("http://localhost/").port(45001).build();
    }
    public void addContainer(ContainerInfo newCont)
    {
    	containerList.add(newCont);
    }
    public void updateContainerInfo()
    {
    	Iterator<ContainerInfo> containerIter = containerList.iterator();
    	int listCount = 0;
    	while(containerIter.hasNext())
    	{    		
    		Client tempClient = Client.create();
    		ContainerInfo tempContainer = containerIter.next();
    		String containerIP = tempContainer.getContainerId().split("@")[1];
    		WebResource r = tempClient.resource("http://"+ containerIP + ":45002/Container/getContainerInfo");    			
    		tempClient.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);
    		ClientResponse response = r.get(ClientResponse.class);		 
    		ContainerInfo tempContainerRes = response.getEntity(ContainerInfo.class);
    		containerList.set(listCount, tempContainerRes);
    		listCount++;
    	}    	
    }
	public String addContainerInfo(String containerID)
	{	
		// Function Should be deleted in the Final Version.
		// Just for the Initial Version. Since we assume we know all the Containers in Place.		
		Client c = Client.create();			
		String containerIP = containerID.split("@")[1];
		WebResource r = c.resource("http://"+ containerIP + ":45002/Container/getContainerInfo");		
		if(requestedContainers.containsKey(containerIP))
		{
			EucalyptusInstance tempInstance = requestedContainers.get(containerIP);
			tempInstance.setInstanceState("running");
			requestedContainers.put(containerIP,tempInstance);
		}		
		c.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);
		ClientResponse response = r.get(ClientResponse.class);		 
		ContainerInfo cont = response.getEntity(ContainerInfo.class);
		containerList.add(cont);			    
		return "Container Info";
	}
	String createContainer()
	{
		//Send an SSH command to create the Container at the remote system
		String containerID = new String();
		return containerID;
	}	
	void destroyContainer(String containerId)
	{
		Iterator<ContainerInfo> removeIter = containerList.iterator();
		while(removeIter.hasNext())
		{
			ContainerInfo remContainer = removeIter.next();
			if(remContainer.getContainerId().matches(containerId))
			{
				// Take Action on the List of releasable Containers by issuing the
				// Necessary commands.	
				removeIter.remove();
			}
		}
	}	
	public synchronized HashMap<String,ContainerInfo> allocateResources(List<String> nodes,List<ResourceInfo> resources)
	{
		// Should decide whether the particular resource could be included
		// in a Current Container or should a new container be created	
		
		Iterator<ResourceInfo> resIter = resources.iterator();
		Iterator<String> nodeIter = nodes.iterator();
		Iterator<ContainerInfo> containerIter;
		HashMap<String,ContainerInfo> retContainerHash = new HashMap<String,ContainerInfo>();		
		boolean newResourceAllocate = false;
		int resourceCount = 0;
		int instanceRequested = 0;
		// List of index positions of nodes which needs to be allocated a resource
		List<Integer> nodesToCreate = new ArrayList<Integer>();
		int currIndex = 0;
		while(resIter.hasNext())
		{
			ResourceInfo currRes = resIter.next();
			String nodeID = nodeIter.next(); 
			containerIter = containerList.iterator();		
			boolean resAllocated = false; 
			int containerCount = 0;
			while(containerIter.hasNext())
			{				
				ContainerInfo currCont = containerIter.next();
				if(currCont.getavailableResources().getNumberOfCores() >= currRes.getNumberOfCores())
				{					
					// Add the ContainerInfo to return List and break
					retContainerHash.put(nodeID,currCont);
					currCont.incrementResources(currRes);
					//currCont.decrementAvailableResource(currRes);					
					containerList.set(containerCount, currCont);
					resAllocated = true;
					break;
				}
				containerCount++;
			}
			if(!resAllocated)
			{
				// Find out the total number of resources to be allocated
				newResourceAllocate = true;
				resourceCount += currRes.getNumberOfCores();
				nodesToCreate.add(currIndex);
			}
			currIndex++;
		}		
		if(newResourceAllocate)
		{
			// Create containers based on the ReourceInfo and return the container Info
			// Currently using greedy method to assign vm's
			while(resourceCount >0)
			{
				int bestCount = 0;
				String bestInstanceType = "";
				Iterator<EucalyptusInstance> instanceIter = availableInstanceTypes.iterator();
				while(instanceIter.hasNext())
				{
					EucalyptusInstance tempInstance = instanceIter.next();
					bestCount = tempInstance.getResourceInfo().getNumberOfCores();
					bestInstanceType = tempInstance.getInstanceType();
					if(resourceCount>=bestCount)
					{
						// Keep track of Instances requested
						instanceRequested++;
						break;
					}
				}
				createEqulyptusInstance(bestCount,bestInstanceType);
				resourceCount = resourceCount - bestCount;		
			}
		}
		// Wait for the newly Created Cotainers to Respond Back
		// Assign the Container ID's
		// Return
		describeInstances();
				
		// For Each of The unique Containers Update the Resource Available.		
		return retContainerHash;
	}
	public static void createEqulyptusInstance(int instanceCount, String instanceType)
	{
		TreeMap<String,String> queryParams = new TreeMap<String,String>();
		queryParams.put("ImageId","emi-D37D148D");
		queryParams.put("InstanceType",instanceType);
		queryParams.put("Timestamp",generateGMTTimeStamp());
		queryParams.put("MaxCount",Integer.toString(instanceCount));
		queryParams.put("MinCount",Integer.toString(instanceCount));
		queryParams.put("Action","RunInstances");
		String queryString = newAccountInfo.generateQueryString(queryParams);		
		System.out.println(queryString);
		Client c = Client.create();	
		WebResource r = c.resource(queryString);
		c.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);
		ClientResponse response = r.get(ClientResponse.class);		       
	}
	public void deleteEucalyptusInstance(String instanceID)
	{
		TreeMap<String,String> queryParams = new TreeMap<String,String>();
		queryParams.put("Timestamp",generateGMTTimeStamp());
		queryParams.put("InstanceId.1",instanceID);
		queryParams.put("Action","TerminateInstances");
		String queryString = newAccountInfo.generateQueryString(queryParams);
		System.out.println(queryString);
		Client c = Client.create();	
		WebResource r = c.resource(queryString);
		c.getProperties().get(ClientConfig.PROPERTY_FOLLOW_REDIRECTS);
		ClientResponse response = r.get(ClientResponse.class);
	}
	public void describeInstances()
	{
		TreeMap<String,String> queryParams = new TreeMap<String,String>();
		queryParams.put("Timestamp",generateGMTTimeStamp());
		queryParams.put("Action","DescribeInstances");
		String queryString = newAccountInfo.generateQueryString(queryParams);
		Client c = Client.create();	
		WebResource r = c.resource(queryString);
		c.getProperties().get(ClientConfig.PROPERTY_FOLLOW_REDIRECTS);
		ClientResponse response = r.get(ClientResponse.class);
		String queryStrin = response.getEntity(String.class);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();		
		try
		{
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new ByteArrayInputStream(queryStrin.getBytes("UTF-8")));
		    NodeList list = doc.getElementsByTagName("dnsName");		    
		    List<String> ipList = new ArrayList<String>();
		    List<EucalyptusInstance> typeList = new ArrayList<EucalyptusInstance>();
		    for (int i=0; i<list.getLength(); i++) 
		    {		   
		    	Element element = (Element)list.item(i);
		    	ipList.add(element.getTextContent().trim());		    }
		    NodeList list1 = doc.getElementsByTagName("instanceType");
		    for (int i=0; i<list.getLength(); i++) 
		    {		  
		    	Element element = (Element)list1.item(i);
		    	EucalyptusInstance tempInstance = new EucalyptusInstance();
		    	tempInstance.setInstanceState(element.getTextContent().trim());
		    	typeList.add(tempInstance);
		    }		
		    Iterator<String> ipIter = ipList.iterator();
		    Iterator<EucalyptusInstance> typeIter = typeList.iterator();
		    while(ipIter.hasNext())
		    {
		    	this.requestedContainers.put(ipIter.next(), typeIter.next());	
		    }		    
		}		
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
	}
	public void releaseResources(List<ContainerInfo> releaseContainers)
	{
		Iterator<ContainerInfo> releaseIter = releaseContainers.iterator();
		while(releaseIter.hasNext())
		{
			this.destroyContainer(releaseIter.next().getContainerId());
		}
	}	
	public List<ContainerInfo> listContainers()
	{
		return containerList;
	}
}
