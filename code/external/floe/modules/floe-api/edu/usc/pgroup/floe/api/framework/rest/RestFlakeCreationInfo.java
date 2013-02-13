/*
 * Copyright 2011, University of Southern California. All Rights Reserved.
 * 
 * This software is experimental in nature and is provided on an AS-IS basis only. 
 * The University SPECIFICALLY DISCLAIMS ALL WARRANTIES, EXPRESS AND IMPLIED, INCLUDING WITHOUT 
 * LIMITATION ANY WARRANTY AS TO MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.
 * 
 * This software may be reproduced and used for non-commercial purposes only, 
 * so long as this copyright notice is reproduced with each such copy made.
 */

package edu.usc.pgroup.floe.api.framework.rest;

/***
 * 
 * @author Sreedhar Natarajan (sreedhan@usc.edu)
 * @author Yogesh Simmhan (simmhan@usc.edu)
 * @version v0.1, 2012-06-03
 *
 */
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import edu.usc.pgroup.floe.api.framework.FloeGraph.Edge;
import edu.usc.pgroup.floe.api.framework.Port;
import edu.usc.pgroup.floe.api.framework.ResourceInfo;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "RestFlakeCreationInfo")
@XmlType(propOrder = { "flakeID","pelletType","resource","inChannel","outChannel","inputPorts","outputPorts"})
public class RestFlakeCreationInfo 
{
	@XmlElement(name = "flakeID")
	String flakeID;
	@XmlElement(name = "pelletType")
	String pelletType;
	@XmlElement(name = "resource")
	ResourceInfo resource;
	@XmlElement(name = "inChannel")
	List<Edge> inChannel;
	@XmlElement(name = "outChannel")
	List<Edge> outChannel;
	@XmlElement(name = "inputPorts")
	List<Port> inputPorts;
	@XmlElement(name = "outputPorts")
	List<Port> outputPorts;
	public RestFlakeCreationInfo()
	{
		this.inChannel = new ArrayList<Edge>();
		this.outChannel = new ArrayList<Edge>();
	}
	public String getFlakeID()
	{
		return this.flakeID;
	}
	public void setFlakeID(String inpID)
	{
		this.flakeID = inpID;
	}
	public String getPellet()
	{
		return this.pelletType;
	}
	public ResourceInfo getResource()
	{
		return this.resource;
	}
	public List<Edge> getInChannel()
	{
		return this.inChannel;
	}
	public List<Edge> getOutChannel()
	{
		return this.outChannel;
	}
	public void setPellet(String pelletType)
	{
		this.pelletType = pelletType;
	}
	public void setResource(ResourceInfo inpResource)
	{
		this.resource = inpResource;
	}
	public void setInChannel(List<Edge> inChannel)
	{
		this.inChannel = inChannel;
	}
	public void setOutChannel(List<Edge> outChannel)
	{
		this.outChannel = outChannel;
	}
	public List<Port> getInputPorts()
	{
		return this.inputPorts;
	}
	public List<Port> getOutputPorts()
	{
		return this.outputPorts;
	}
	public void setInputPorts(List<Port> inpPorts)
	{
		this.inputPorts = inpPorts;
	}
	public void setOutputPorts(List<Port> outPorts)
	{
		this.outputPorts = outPorts;
	}
}
