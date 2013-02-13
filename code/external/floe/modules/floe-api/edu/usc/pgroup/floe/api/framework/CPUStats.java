package edu.usc.pgroup.floe.api.framework;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "CpuStats")
public class CPUStats {
	private int noOfCores;
	private int noOfCPUs;
	private int speed;
	private double[] perCoreCombinedUsage;
	private double totalUsage;
	
	public int getNoOfCores()
	{
		return noOfCores;
	}
	public void setNoOfCores(int aNoOfCores)
	{
		noOfCores = aNoOfCores;
	}
	
	public int getNoOfCPUs()
	{
		return noOfCPUs;
	}
	public void setNoOfCPUs(int aNoOfCPUs)
	{
		noOfCPUs = aNoOfCPUs;
	}
	
	public int getSpeed()
	{
		return speed;
	}
	public void setSpeed(int aSpeed)
	{
		speed = aSpeed;
	}
	
	public double[] getPerCoreCombinedUsage()
	{
		return perCoreCombinedUsage;
	}
	public void setPerCoreCombinedUsage(double[] aPerCoreCombinedUsage)
	{
		perCoreCombinedUsage = aPerCoreCombinedUsage;
	}
	
	public double getTotalUsage()
	{
		return totalUsage;
	}
	public void setTotalUsage(double aTotalUsage)
	{
		totalUsage = aTotalUsage;
	}
}
