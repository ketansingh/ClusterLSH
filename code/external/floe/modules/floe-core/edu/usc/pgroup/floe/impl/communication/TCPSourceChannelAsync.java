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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import edu.usc.pgroup.floe.api.communication.ConnectionInfo;
import edu.usc.pgroup.floe.api.communication.Message;
import edu.usc.pgroup.floe.api.communication.SourceChannel;

/***
 * This manages incoming messages
 *  
 * @author Sreedhar Natarajan (sreedhan@usc.edu)
 * @author Yogesh Simmhan (simmhan@usc.edu)
 * @version v0.1, 2012-01-11
 *
 */

public class TCPSourceChannelAsync<T> implements SourceChannel<T>
{
	BlockingQueue<Message<T>> blockingQueue ;
	ConnectionInfo configInfo;
	public Message<T> getMessage(int timeoutMillis)
	{
		try
		{			
			return blockingQueue.poll(timeoutMillis,TimeUnit.MILLISECONDS);
		}
		catch(InterruptedException ex)
		{
			ex.printStackTrace();
		}
		
		return null;
		
	}
	public void openConnection()
	{
		
	}
	public void closeConnection()
	{
		
	}
	@Override
	public ConnectionInfo getConnectionInfo() {
		// TODO Auto-generated method stub
		return this.configInfo;
	}
	@Override
	public void setConnectionInfo(ConnectionInfo inpConfig) {
		// TODO Auto-generated method stub
		this.configInfo = inpConfig;
	}

}
