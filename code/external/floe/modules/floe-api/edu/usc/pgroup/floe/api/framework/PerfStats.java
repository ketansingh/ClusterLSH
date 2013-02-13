package edu.usc.pgroup.floe.api.framework;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "PerfStats")
public class PerfStats {
	private CPUStats cpuStats;
	private MemStats memStats;
	private FlakeInfo[] flakeInfo;
	private NetworkStats networkStats;
	private String containerID;
	Date timeStamp;
	
	
	public Date getTimeStamp()
	{
		return timeStamp;
	}
	
	public void setTimeStamp(Date aTimeStamp)
	{
		timeStamp = aTimeStamp;
	}
	
	
	public String getContainerID()
	{
		return containerID;
	}
	
	public void setContainerID(String aCid)
	{
		containerID = aCid;
	}
	
	public  CPUStats getCPUStats() {
		return cpuStats;
	}
	
	public  void setCPUStats(CPUStats cs) {
		cpuStats = cs;
	}
	
	public  MemStats getMemStats() {
		return memStats;	
	}
	
	public  void setMemStats(MemStats ms) {
		memStats = ms;	
	}
	
	public  FlakeInfo[] getFlakeBufferStats() {
		return flakeInfo;
	}
	
	public  void setFlakeBufferStats(FlakeInfo[] fs) {
		flakeInfo = fs;
	}
	
	public  NetworkStats getNetworkStats() {
		return networkStats;
	}
	
	public  void setNetworkStats(NetworkStats ns) {
		networkStats = ns;
	}
}
