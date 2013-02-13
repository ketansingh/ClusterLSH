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

package edu.usc.pgroup.floe.impl.communication;

/***
* This manages the Containers and their resources within a region.
 * 
 * @author Sreedhar Natarajan (sreedhan@usc.edu)
 * @author Yogesh Simmhan (simmhan@usc.edu)
 * @version v0.1, 2012-01-17
 *
 */

import java.io.IOException;
import java.net.URI;

import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.net.httpserver.HttpServer;

import edu.usc.pgroup.floe.api.communication.ConnectionInfo;
import edu.usc.pgroup.floe.api.communication.Message;
import edu.usc.pgroup.floe.api.communication.SourceChannel;

public class RestSourceChannel<T> extends Thread implements SourceChannel<T>
{
	ResourceConfig rc;
	HttpServer httpServer;
	public RestSourceChannel(String resourceName,URI inpURI)
	{
		try
		{
			this.rc = new PackagesResourceConfig(resourceName);
			httpServer = HttpServerFactory.create(inpURI,this.rc);
		}
		catch(IOException e)
		{
			System.out.println("Rest Source Channel IO Exception");
			e.printStackTrace();
		}
	}
	public Message<T> getMessage(int timeoutMillis)
	{
		return new MessageImpl<T>();
	}		
	public void run()
    {
    	
    	try
    	{    		    		
    	    //HttpServer httpServer = HttpServerFactory.create(BASE_URI, rc);
    	  	httpServer.start();
    	    System.out.println("The Resource has been Started at " + this.httpServer.getAddress());
    	}
	    catch(Exception e)
	    {
	    	e.printStackTrace();
	    }
    }
	public void openConnection()
	{
		if(this.isAlive())
		{
		
		}
		else
		{			
			this.start();
		}
		
	}	
	public void closeConnection()
	{
		this.stop();
		this.httpServer.stop(0);
	}
	@Override
	public ConnectionInfo getConnectionInfo() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void setConnectionInfo(ConnectionInfo inpConfig) {
		// TODO Auto-generated method stub
		
	}
}
