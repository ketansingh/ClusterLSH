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

import java.io.IOException;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import edu.usc.pgroup.floe.api.communication.ConnectionInfo;
import edu.usc.pgroup.floe.api.communication.Message;
import edu.usc.pgroup.floe.api.communication.SourceChannel;

/***
 * TCPSourcePullChannel constantly pulls data from the server
 *  
 * @author Sreedhar Natarajan (sreedhan@usc.edu)
 * @author Yogesh Simmhan (simmhan@usc.edu)
 * @version v0.1, 2012-01-11
 *
 */

public class TCPSourcePullChannel<T> extends Thread implements SourceChannel<T> 
{
	BlockingQueue<Message<T>> blockingQueue;
	Socket socket;
	ServerSocketChannel objServer;
	ConnectionInfo configInfo;
	boolean running = true;
	public TCPSourcePullChannel(ConnectionInfo inpConnection,BlockingQueue<Message<T>> inQueue)
	{		
		configInfo = inpConnection;
		this.blockingQueue = inQueue;
	}
	public void setChannelQueue(BlockingQueue<Message<T>> inpQueue)
	{
		this.blockingQueue = inpQueue;
	}
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
	public void run()
	{
		try
		{
			// Open A Server Socket Channel to Listen to receive the incoming messages
			SocketChannel socketChannel = objServer.accept();
			socketChannel.close();				
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			
		}
	}
	public void shutdown()
	{
		this.running = false;
	}
	public void openConnection()
	{
		// The blocking queue instantiation should be removed if
		// the Queue is going to share between multiple channels
		try
		{
			this.socket = new Socket(this.configInfo.getDestAddress(),this.configInfo.getOutPortNo());
			this.start();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		this.blockingQueue = new LinkedBlockingQueue<Message<T>>();
	}
	public void closeConnection()
	{
		try
		{	
			this.shutdown();								
			this.objServer.close();
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}		
	public ConnectionInfo getConnectionInfo() {
		// TODO Auto-generated method stub
		return this.configInfo;
	}
	
	public void setConnectionInfo(ConnectionInfo inpConfig) {
		// TODO Auto-generated method stub
		this.configInfo = inpConfig;
	}

	
}


