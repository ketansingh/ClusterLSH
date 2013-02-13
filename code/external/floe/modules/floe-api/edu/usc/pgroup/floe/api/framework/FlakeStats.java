package edu.usc.pgroup.floe.api.framework;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "FlakeBufferStats")
public class FlakeStats {
	
	
	private int inputBufferLen;
	private int outputBufferLen;
	private String flakeId;
	private int numberOfAllocatedCores;
	private int numberOfPelletInstances;
	
	public String getFlakeId()
	{
		return flakeId;
	}
	
	public void setFlakeId(String aFlakeId)
	{
		flakeId = aFlakeId;
	}
	
	public int getInputBufferLen()
	{
		return inputBufferLen;
	}
	
	public void setInputBufferLen(int aInputBufferLen)
	{
		inputBufferLen = aInputBufferLen;
	}
	
	public int getOutputBufferLen()
	{
		return outputBufferLen;
	}
	
	public void setOutputtBufferLen(int aOutputBufferLen)
	{
		outputBufferLen = aOutputBufferLen;
	}

	public void setNumberOfAllocatedCores(int aNumberOfCores) {
		numberOfAllocatedCores = aNumberOfCores;
	}

	public void setNumberOfPelletInstances(int pelletCount) {
		numberOfPelletInstances = pelletCount;
	}
	
	public int getNumberOfAllocatedCores() {
		return numberOfAllocatedCores;
	}

	public int getNumberOfPelletInstances() {
		return numberOfPelletInstances;
	}
}
