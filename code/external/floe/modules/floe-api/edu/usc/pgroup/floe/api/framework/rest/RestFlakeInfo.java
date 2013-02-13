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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import edu.usc.pgroup.floe.api.framework.FlakeInfo;

/***
 * 	RestFlakeInfo is used to Act as a wrapper for sending data through Rest Channels.
 *  Contains a List of FlakeInfo Which needs to be transmitted between Container, Manager and Coordinator.	
 * 
 * @author Sreedhar Natarajan (sreedhan@usc.edu)
 * @author Yogesh Simmhan (simmhan@usc.edu)
 * @version v0.1, 2012-01-31
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "RestFlakeInfo")
@XmlType(propOrder = { "flakeList"})

public class RestFlakeInfo 
{
	@XmlElement(name = "flakeList")
	List<FlakeInfo> flakeList;
	public RestFlakeInfo()
	{
		this.flakeList = new ArrayList<FlakeInfo>();
	}
	public List<FlakeInfo> getFlakeList()
	{
		return this.flakeList;
	}
	public void setFlakeList(List<FlakeInfo> inpFlakeList)
	{
		this.flakeList = inpFlakeList;
	}

}
