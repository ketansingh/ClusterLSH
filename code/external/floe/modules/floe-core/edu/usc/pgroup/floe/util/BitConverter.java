package edu.usc.pgroup.floe.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class BitConverter {
	
	public static <T> byte[] getBytes(T obj)	
	{
		byte[] retBytes = null;
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		try 
		{
		    ObjectOutputStream outStream = new ObjectOutputStream(byteStream); 
		    outStream.writeObject(obj);
		    outStream.flush(); 
		    outStream.close(); 
		    byteStream.close();
		    retBytes = byteStream.toByteArray ();
		}
		catch (IOException ex) 
		{
		    //TODO: Handle the exception
			ex.printStackTrace();
		}
		return retBytes;
	}
	
	public static <T> T getObject(byte[] bytes)
	{
		T obj = null;
		try 
		{
			ByteArrayInputStream byteInput = new ByteArrayInputStream (bytes);
			ObjectInputStream ois = new ObjectInputStream (byteInput);
			obj = (T)ois.readObject();
		}
		catch (Exception ex) 
		{
	  
		}
		return obj;	
	}
	
	
		
}
