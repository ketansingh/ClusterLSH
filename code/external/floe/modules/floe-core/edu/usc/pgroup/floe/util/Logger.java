package edu.usc.pgroup.floe.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;


import edu.usc.pgroup.floe.api.framework.HealthManager.ReactiveActions;

public class Logger {
	private static Logger instance;
	
	private BufferedWriter lout;
	private FileWriter writer;
	public static Logger getInstance()
	{
		if(instance == null)
		{
			instance = new Logger();
		}
		return instance;
	}
	
	private Logger(){
		try {
			writer = new FileWriter("healthLogs.csv", true);
			lout = new BufferedWriter(writer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void LogInfo(String cid,String fid, ReactiveActions suggestedAction, boolean accepted)
	{
		String acceptedstr = accepted?"accepted":"rejected";
		Date timeStamp = new Date();
		try {
			lout.write("scale_request,"+
					cid+","+
					timeStamp+","+
					fid+","+
					suggestedAction+","+
					acceptedstr+ ","+
					"\n");
			lout.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void LogInfo(String cid, String fid, Date timeStamp, double latency, double cpu, int inpBufferLength, int outBufferLength, int allocatedCores, int allocatedPellets)
	{
		try {
			lout.write("container_status,"+cid+","+
					fid+","+
					timeStamp+","+
					latency+","+
					cpu+","+
					inpBufferLength+","+
					outBufferLength+","+
					allocatedCores+","+
					allocatedPellets+					
					"\n");
			lout.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void LogInfo(String data)
	{
		try {
			lout.write(data+"\n");
			lout.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
