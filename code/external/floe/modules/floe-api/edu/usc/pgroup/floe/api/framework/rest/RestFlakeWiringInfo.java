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
 * This is the Coordinator Implementation  
 * 
 * @author Sreedhar Natarajan (sreedhan@usc.edu)
 * @author Yogesh Simmhan (simmhan@usc.edu)
 * @version v0.1, 2012-02-08
 *
 */

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import edu.usc.pgroup.floe.api.communication.ConnectionInfo;

@XmlRootElement(name = "RestFlakeWiringInfo")
@XmlType(propOrder = { "flakeID","connectionList"})
public class RestFlakeWiringInfo
{
	String flakeID;
	List<ConnectionInfo> connectionList;
	public List<ConnectionInfo> getConnectionList()
	{
		return this.connectionList;
	}
	public void setConnectionList(List<ConnectionInfo> inpConnectionList)
	{
		this.connectionList = inpConnectionList;
	}
	public String getFlakeID()
	{
		return this.flakeID;
	}
	public void setFlakeID(String inpFlakeID)
	{
		this.flakeID = inpFlakeID;
	}
}
