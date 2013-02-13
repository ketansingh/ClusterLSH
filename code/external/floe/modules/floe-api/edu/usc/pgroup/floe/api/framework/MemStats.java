package edu.usc.pgroup.floe.api.framework;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "MemStats")
public class MemStats {
	private long totalMem;
	private long freeMem;
	private long usedMem;
	
	public long getTotalMem()
	{
		return totalMem;
	}
	
	public void setTotalMem(long aTotalMem)
	{
		totalMem = aTotalMem;
	}
	
	public long getFreeMem()
	{
		return freeMem;
	}
	
	public void setFreeMem(long aFreeMem)
	{
		freeMem = aFreeMem;
	}
	
	
	public long getUsedMem()
	{
		return usedMem;
	}
	
	public void setUsedMem(long aUsedMem)
	{
		usedMem = aUsedMem;
	}
}
