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
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.usc.pgroup.floe.api.communication.ConnectionInfo;
import edu.usc.pgroup.floe.api.communication.Message;
import edu.usc.pgroup.floe.api.communication.SourceChannel;
import edu.usc.pgroup.floe.impl.queues.SourceQueue;
import edu.usc.pgroup.floe.util.BitConverter;

/***
 * This manages incoming messages
 *  
 * @author Sreedhar Natarajan (sreedhan@usc.edu)
 * @author Yogesh Simmhan (simmhan@usc.edu)
 * @version v0.1, 2012-01-11
 *
 */

public class TCPSourcePushChannel<T> extends Thread implements SourceChannel<T>
{
	private static Logger logger = Logger.getLogger(TCPSourcePushChannel.class.getName());
	private SourceQueue sourceQueue;
	private  String overrideKey;
	ServerSocket objServer;
	ExecutorService execSvc;
	ConnectionInfo configInfo;
	boolean running = true;
	public TCPSourcePushChannel()
	{
		
	}
	public TCPSourcePushChannel(SourceQueue sourceQueue)
	{
		this.sourceQueue = sourceQueue;
		configInfo = new ConnectionInfo();
		this.overrideKey = null;
	}
	public TCPSourcePushChannel(SourceQueue sourceQueue, String key)
	{
		this.sourceQueue = sourceQueue;
		configInfo = new ConnectionInfo();
		this.overrideKey = key;
	}
	
	@Override
	public Message<T> getMessage(int timeoutMillis)
	{
		throw new UnsupportedOperationException("Get Message not supported in Push mechanish");
	}
	@Override
	public void run()
	{				
		try
		{
			this.objServer = new ServerSocket(this.configInfo.getInPortNo());		
			while(running)
			{			
				Socket newSocket = this.objServer.accept();						         
				SourceChannelConnect newConnection = new SourceChannelConnect(this, newSocket);
				this.execSvc.execute(newConnection);
			}
		}
		catch(SocketException e)
		{			
			System.out.println("Terminated the ObjServer");
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
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
	public void openConnection()
	{
		try
		{
			ServerSocket inServer = new ServerSocket(0);	
			this.configInfo.setSourceAddress(getHostAddress());		
			this.configInfo.setSourceAddress(getHostAddress());
			this.configInfo.setInPort(inServer.getLocalPort());
			inServer.close();
			// The blocking queue instantiation should be removed if
			// the Queue is going to share between multiple channels
			this.execSvc = Executors.newFixedThreadPool(5);	
			this.start();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}					
	}
	public void shutdown()
	{
		try
		{
			this.objServer.close();		
			this.execSvc.shutdown();
			System.out.println("Safe Termination of Executor Service");
		}
		catch(Exception ex)
		{
			System.out.println("Safely Terminated the Exceutor Service on Exception");
		}
	}
	@Override
	public ConnectionInfo getConnectionInfo()
	{
		return this.configInfo;
	}
	@Override
	public void setConnectionInfo(ConnectionInfo inpConnection)
	{
		this.configInfo = inpConnection;
	}
	
	@Override
	public void closeConnection()
	{
		try
		{							
			//this.execSvc.shutdown();									
			this.shutdown();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	class SourceChannelConnect implements Runnable
	{		 
		 Socket client = null;
		 InputStream ois = null;
		 boolean running = true;
		 TCPSourcePushChannel channel;
		    
		 public SourceChannelConnect() {}

		 public SourceChannelConnect(TCPSourcePushChannel channel, Socket clientSocket) 
		 {			  
			 this.channel = channel;
			 client = clientSocket;			
		 }	
		 // Reads the mentioned amount of bytes from the Socket Connection
		 public byte[] readBytes(int size)
		 {
			 try
			 {
				 byte[] retByte = new byte[size];
				 int read = 0, offset = 0,toRead = size;
				 int i = 1;
				 while(toRead > 0 && (read = ois.read(retByte, offset, toRead)) > 0) 
				 {
				     toRead -= read;
				     offset += read;
				     i++;
				 }
				 return retByte;
			 }
			 catch(Exception e)
			 {
				 
			 }
			 return null;
		 }
		 @Override
		@SuppressWarnings("unchecked")
		 public void run() 
		 {		
			 try 
			 {		
				 ois = client.getInputStream();
				 byte[] headObj = new byte[4];
				 //Fetch the size of the input data from the first 4 bytes
				 while(true)
				 {
					 headObj = readBytes(4);
					 int tempSize = 0;
					 ByteBuffer byteBuffer = ByteBuffer.wrap(headObj);
					 IntBuffer intBuffer = byteBuffer.asIntBuffer(); 
					 tempSize = intBuffer.get();
					 intBuffer.clear();
					 if(tempSize>0)
					 {
						 byte[] recObj = readBytes(tempSize);
						 Message inpMsg = (MessageImpl) BitConverter.getObject(recObj);
						 if (overrideKey != null) {
							 inpMsg.setKey(overrideKey);
						 }
						 sourceQueue.queueMessage(this.channel, inpMsg);
					 }
				 }			
			 } 
			 catch(Exception e) 
			 {
				 logger.log(Level.SEVERE, "Interrupted Exception, Network Source Channel Queue", e);
				 try 
		         {
		            client.close();
		         }
		         catch(Exception ex) 
		         {
		           System.out.println(ex.getMessage());
		         }
			 }       		 
		 }
		 public void shutdown()
		 {
			 
		 }
		 public void closeConnection()
		 {
			 try
			 {				
				 this.ois.close();
				 this.client.close();
			 }
			 catch(IOException ex)
			 {
				 ex.printStackTrace();
			 }
		 }
	}	
}


