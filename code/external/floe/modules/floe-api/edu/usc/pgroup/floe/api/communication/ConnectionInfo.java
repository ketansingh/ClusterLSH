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

package edu.usc.pgroup.floe.api.communication;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import edu.usc.pgroup.floe.api.framework.FloeGraph.Edge;

/***
 * This manages outgoing messages
 *  
 * @author Sreedhar Natarajan (sreedhan@usc.edu)
 * @author Yogesh Simmhan (simmhan@usc.edu)
 * @version v0.1, 2012-02-07
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "ConnectionInfo")
@XmlType(propOrder = { "sourceAddress", "destAddress", "inPortNo","outPortNo","edgeInfo","portName"})
public class ConnectionInfo 
{
	// Refers to The Place Where Connection has been established
	String sourceAddress;
	// Refers to the Place where Connection will be listening/sending information.
	String destAddress;
	// Refers to the Port where Connection is listening or pulling
	int inPortNo;
	// Refers to the Port for Outgoing Information
	int outPortNo;
	Edge edgeInfo;
	String portName;	// Name of the Port incase mappable data is used
	public String getDestAddress()
	{
		return this.destAddress;
	}
	public int getInPortNo()
	{
		return this.inPortNo;
	}
	public int getOutPortNo()
	{
		return this.outPortNo;
	}
	public String getSourceAddress()
	{
		return this.sourceAddress;
	}
	public void setSourceAddress(String inpSource)
	{
		this.sourceAddress = inpSource;
	}
	public void setDestAddress(String destAddr)
	{
		this.destAddress = destAddr;
	}
	public void setInPort(int inpPort)
	{
		this.inPortNo = inpPort;
	}
	public void setOutPort(int outPort)
	{
		this.outPortNo = outPort;
	}	
	
	public void setEdge(Edge inpEdge)
	{
		this.edgeInfo = inpEdge;
	}
	
	public Edge getEdge()
	{
		return this.edgeInfo;
	}
	public String getPortName()
	{
		return this.portName;
	}
	public void setPortName(String inpPort)
	{
		this.portName = inpPort;
	}
}
