package edu.usc.pgroup.floe.api.framework;

public class DiskIOStats {

	double readThroughput;
	double writeThroughput;
	
	public void setThroughput(double readThroughput, double writeThroughput) {
		this.readThroughput = readThroughput;
		this.writeThroughput = writeThroughput;		
	}

}
