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
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import edu.usc.pgroup.floe.api.communication.ConnectionInfo;
import edu.usc.pgroup.floe.api.communication.Message;
import edu.usc.pgroup.floe.api.communication.SinkChannel;

/***
 * TCPSinkPull enables client to pull data from Server on Request.
 *  
 * @author Sreedhar Natarajan (sreedhan@usc.edu)
 * @author Yogesh Simmhan (simmhan@usc.edu)
 * @version v0.1, 2012-01-11
 *
 */

public class TCPSinkPullChannel<T> extends Thread implements SinkChannel<T> 
{
	ConnectionInfo configInfo;
	ServerSocketChannel objServer;
	boolean running = true;
	ObjectOutputStream oos;
	BlockingQueue<Message<T>> blockingQueue;
	public TCPSinkPullChannel(BlockingQueue<Message<T>> inpQueue)
	{
		this.configInfo = new ConnectionInfo();
		this.blockingQueue = inpQueue;
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
	public void openConnection()
	{
		try
		{
			ServerSocket inServer = new ServerSocket(0);	
			this.configInfo.setSourceAddress(getHostAddress());
			this.configInfo.setSourceAddress(getHostAddress());
			this.configInfo.setInPort(inServer.getLocalPort());
			inServer.close();
			this.start();
		}
		catch(Exception ex)
		{			
			ex.printStackTrace();
		}		
	}
	public ConnectionInfo getConnectionInfo()
	{
		return this.configInfo;
	}
	public void run()
	{
		try
		{
			while(running)
			{
				SocketChannel socketChannel = objServer.accept();
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
	}
	public void putMessage(Message<T> message)
	{
		try
		{								
		    return;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	public void closeConnection()
	{
		try
		{
			this.running = false;
			while(this.isAlive())
				
			this.oos.close();				
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}
	class SourceChannelConnect extends Thread
	{		 
		 Socket client = null;
		 InputStream inStrm = null;
		 OutputStream outStrm = null;
		 boolean running = true;
		    
		 public SourceChannelConnect() {}
		 public final byte[] intToByteArray(int value) 
		 {
			 return new byte[] {
					 (byte)(value >>> 24),
		             (byte)(value >>> 16),
		             (byte)(value >>> 8),
		             (byte)value};
		 }
		 public SourceChannelConnect(Socket clientSocket) 
		 {			   			   
			 client = clientSocket;
			 try 
		     {
				 inStrm = client.getInputStream();
				 outStrm = client.getOutputStream();
				 this.start();
		     } 
		     catch(Exception e1) 
		     {
		         try 
		         {		        	
		            client.close();
		         }
		         catch(Exception e) 
		         {
		           System.out.println(e.getMessage());
		         }
		         return;
		     }		     	
		 }	
		 // Reads the mentioned amount of bytes from the Socket Connection
		 public byte[] readBytes(int size)
		 {
			 try
			 {
				 byte[] retByte = new byte[size];
				 int read = 0, offset = 0,toRead = size;
				 int i = 1;
				 while(toRead > 0 && (read = inStrm.read(retByte, offset, toRead)) > 0) 
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
				 byte[] headObj = new byte[4];
				 byte[] sizeByteBuffer = null;
				 byte[] finalBuffer = null;
				 // If 4 bytes are received then the Client is looking for information
				 // Check if there are any Buffered Message. Send one Messaage to the Client
				 while(true)
				 {
					 headObj = readBytes(4);
					 Message<T> currMsg = blockingQueue.poll(500,TimeUnit.MILLISECONDS);
					 if(currMsg!=null)
					 {
						 byte[] msgByteBuffer = (byte[])currMsg.getPayload();
						 if(msgByteBuffer!=null)
						 {						
							 sizeByteBuffer = intToByteArray(msgByteBuffer.length);
							 finalBuffer = new byte[msgByteBuffer.length + sizeByteBuffer.length];
							 System.arraycopy(sizeByteBuffer, 0, finalBuffer, 0, sizeByteBuffer.length);
							 System.arraycopy(msgByteBuffer, 0, finalBuffer, sizeByteBuffer.length, msgByteBuffer.length);

						 }						 
						 msgByteBuffer = null;
					 }
					 else
					 {
						 // Send 4 bytes of Data mentioning that there is not content 
						 // ready to be delivered
						 finalBuffer = intToByteArray(0);
					 }
					 outStrm.write(finalBuffer);
					 outStrm.flush();					 					
				 }			
			 } 
			 catch(Exception e) 
			 {
				 System.out.println("Interrupted Exception, Network Source Channel Queue");
			 }       		 
		 }
		 public void closeConnection()
		 {
			 try
			 {				
				 this.inStrm.close();
				 this.client.close();
			 }
			 catch(IOException ex)
			 {
				 ex.printStackTrace();
			 }
		 }
	}	

	
}
