package edu.usc.pgroup.floe.api.framework;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "NetworkStats")
public class NetworkStats {
	private int totalInComing;
	private int totalOutGoing;
	
	public int getTotalIncoming()
	{
		return totalInComing;
	}
	
	public void setTotalIncoming(int aTotalInComing)
	{
		totalInComing = aTotalInComing;
	}
	
	public int getTotalOutgoing()
	{
		return totalOutGoing;
	}
	
	public void setTotalOutgoing(int aTotalOutgoing)
	{
		totalOutGoing = aTotalOutgoing;
	}
}
