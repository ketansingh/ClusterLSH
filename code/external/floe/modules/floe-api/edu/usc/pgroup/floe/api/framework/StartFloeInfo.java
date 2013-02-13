package edu.usc.pgroup.floe.api.framework;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import edu.usc.pgroup.floe.api.communication.ConnectionInfo;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "FloeInfo")
@XmlType(propOrder = { "floeID", "connectionList"})
public class StartFloeInfo 
{
	@XmlElement(name = "floeID")
	String floeID;
	@XmlElement(name = "connectionList")
	List<ConnectionInfo> connectionList;
	public String getFloeID()
	{
		return this.floeID;
	}
	public void setFloeID(String inpId)
	{
		this.floeID = inpId;
	}
	public void setConnections(List<ConnectionInfo> tempInfo)
	{
		this.connectionList = tempInfo;
	}
	public List<ConnectionInfo> getConnection()
	{
		return this.connectionList;
	}
}
