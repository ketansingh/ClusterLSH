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
import java.io.OutputStream;
import java.net.Socket;

import edu.usc.pgroup.floe.api.communication.ConnectionInfo;
import edu.usc.pgroup.floe.api.communication.Message;
import edu.usc.pgroup.floe.api.communication.SinkChannel;
import edu.usc.pgroup.floe.util.BitConverter;

/***
 * This manages outgoing messages
 *  
 * @author Sreedhar Natarajan (sreedhan@usc.edu)
 * @author Yogesh Simmhan (simmhan@usc.edu)
 * @version v0.1, 2012-01-11
 *
 */

public class TCPSinkPushChannel<T> extends Thread implements SinkChannel<T>
{
	Socket socket;
	OutputStream oos;
	ConnectionInfo configInfo;
	Thread currThread;
	boolean running = true;
	public TCPSinkPushChannel()
	{
		
	}
	public TCPSinkPushChannel(ConnectionInfo inpConnection)
	{
		this.configInfo = inpConnection;
	}
	@Override
	public void openConnection()
	{
		try
		{			
			this.socket = new Socket(this.configInfo.getDestAddress(),this.configInfo.getOutPortNo());
			this.start();
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}
	@Override
	public ConnectionInfo getConnectionInfo()
	{
		return this.configInfo;
	}
	public static final int byteArrayToInt(byte [] b) {
        return (b[0] << 24)
                + ((b[1] & 0xFF) << 16)
                + ((b[2] & 0xFF) << 8)
                + (b[3] & 0xFF);
	}
	public static final byte[] intToByteArray(int value) {
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
	}
	@Override
	public void putMessage(Message<T> message)
	{
		try
		{	
			String key = message.getKey();
			byte[] msgByteBuffer = BitConverter.getBytes(message);
			if(msgByteBuffer!=null)
			{
				this.oos = socket.getOutputStream();							
				byte[] sizeByteBuffer = intToByteArray(msgByteBuffer.length);
				byte[] finalBuffer = new byte[msgByteBuffer.length + sizeByteBuffer.length];
				System.arraycopy(sizeByteBuffer, 0, finalBuffer, 0, sizeByteBuffer.length);
				System.arraycopy(msgByteBuffer, 0, finalBuffer, sizeByteBuffer.length, msgByteBuffer.length);
				//System.out.println("Size of Buffer " + msgByteBuffer.length + " " + sizeByteBuffer.length + " Final Buffer" + finalBuffer.length);
				this.oos.write(finalBuffer);
				sizeByteBuffer = null;
				finalBuffer = null;
				oos.flush();		
			    return;	
			}
			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	@Override
	public void closeConnection()
	{
		try
		{
			this.running = false;
			// Thread has stopped running. Close the connection
			if(this.oos!=null)
				this.oos.close();
			if(this.socket!=null)
				this.socket.close();	
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}	
	@Override
	public void run() 
	{	
		/*try
		{
			System.out.println("Message to be Sent to  " + this.configInfo.getInPortNo() + " " +  this.configInfo.getOutPortNo());
			while(running)
			{	
				Message<T> tempMessage = this.blockingQueue.poll(5000,TimeUnit.MILLISECONDS);				
				if(tempMessage!=null)
				{
					this.putMessage(tempMessage);
				}
				tempMessage = null;
			}							
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}*/
	}
	
}
