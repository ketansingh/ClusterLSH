package edu.usc.pgroup.floe.api.framework;

public class MemoryReadWriteStats {
	double memFillTime;
	
	public void setMemFillTime(double elapsedSeconds) {
		// TODO Auto-generated method stub
		memFillTime = elapsedSeconds;				
	}

	public double getMemFillTime()
	{
		return memFillTime;
	}
}
