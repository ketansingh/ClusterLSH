package edu.usc.pgroup.floe.impl.communication;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import edu.usc.pgroup.floe.api.communication.ConnectionInfo;
import edu.usc.pgroup.floe.api.communication.Message;
import edu.usc.pgroup.floe.api.communication.SinkChannel;
import edu.usc.pgroup.floe.util.CircularBuffer;

public class TCPSinkCircularPush<T> extends Thread implements SinkChannel<T>
{

	Socket socket;
	OutputStream oos;
	ConnectionInfo configInfo;
	CircularBuffer<Message<T>> circularQueue;
	Thread currThread;
	boolean running = true;
	public TCPSinkCircularPush(ConnectionInfo inpConnection,CircularBuffer<Message<T>> inpQueue)
	{
		this.configInfo = inpConnection;
		this.circularQueue = inpQueue;
	}
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
	public void putMessage(Message<T> message)
	{
		try
		{	
			byte[] msgByteBuffer = (byte[])message.getPayload();
			if(msgByteBuffer!=null)
			{
				this.oos = socket.getOutputStream();							
				byte[] sizeByteBuffer = intToByteArray(msgByteBuffer.length);
				byte[] finalBuffer = new byte[msgByteBuffer.length + sizeByteBuffer.length];
				System.arraycopy(sizeByteBuffer, 0, finalBuffer, 0, sizeByteBuffer.length);
				System.arraycopy(msgByteBuffer, 0, finalBuffer, sizeByteBuffer.length, msgByteBuffer.length);
				System.out.println("Size of Buffer " + msgByteBuffer.length + " " + sizeByteBuffer.length + " Final Buffer" + finalBuffer.length);
				this.oos.write(finalBuffer);
				oos.flush();		
			    return;	
			}
			
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}
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
	public void run() 
	{	
		int currentThreadID = circularQueue.getIndex();
		try
		{
			while(running)
			{	
				Message<T> tempMessage = circularQueue.get(currentThreadID);
				if(tempMessage!=null)
				{
					this.putMessage(tempMessage);
				}
			}							
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	

}
