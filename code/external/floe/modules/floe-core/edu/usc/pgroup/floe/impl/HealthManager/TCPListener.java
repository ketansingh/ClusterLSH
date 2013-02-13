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

package edu.usc.pgroup.floe.impl.HealthManager;




import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import edu.usc.pgroup.floe.api.communication.ConnectionInfo;
import edu.usc.pgroup.floe.api.communication.Message;
import edu.usc.pgroup.floe.api.communication.SinkChannel;
import edu.usc.pgroup.floe.impl.communication.MessageImpl;


/***
 * This manages outgoing messages
 *  
 * @author Alok Kumbhare (kumbhare@usc.edu)
 * @version v0.1, 2012-06-06
 *
 */

public class TCPListener extends Thread
{
	ServerSocket objServer;
	ExecutorService execSvc;

	boolean running = true;
	public TCPListener()
	{
		
	}
	
	public void run()
	{				
		try
		{
			this.objServer = new ServerSocket(9999);		
			while(running)
			{			
				Socket newSocket = this.objServer.accept();						         
				SourceChannelConnect newConnection = new SourceChannelConnect(newSocket);
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
	
	public void startListening()
	{
		try
		{
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
		    
		 public SourceChannelConnect() {}

		 public SourceChannelConnect(Socket clientSocket) 
		 {			   			   
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
						 //Read and return the same message back.. TODO
						 /*byte[] recObj = readBytes(tempSize);
						 Message<T> inpMsg = new MessageImpl<T>();
						 inpMsg.putPayload((T)recObj);
						 blockingQueue.put(inpMsg);*/
						 
					 }
				 }			
			 } 
			 catch(Exception e) 
			 {
				 System.out.println("Interrupted Exception, Network Source Channel Queue");
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
