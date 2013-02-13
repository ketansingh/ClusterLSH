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
 * 	RestResourcePellet is used to act as a wrapper for List of Pellets and Resources which
 * 	needs to be created.	
 * 
 * @author Sreedhar Natarajan (sreedhan@usc.edu)
 * @author Yogesh Simmhan (simmhan@usc.edu)
 * @version v0.1, 2012-01-31
 *
 */

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import edu.usc.pgroup.floe.api.framework.ResourceInfo;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "RestResourcePellet")
@XmlType(propOrder = { "pelletList","resourceList"})	
public class RestResourcePellet 
{	
	@XmlElement(name = "pelletList")
	List<ResourceInfo> resourceList;
	@XmlElement(name = "resourceList")
	List<String> pelletList;
	public RestResourcePellet()
	{
		this.resourceList = new ArrayList<ResourceInfo>();
		this.pelletList = new ArrayList<String>();
	}
	public List<String> getPelletList()
	{
		return this.pelletList;
	}
	public List<ResourceInfo> getResourceList()
	{
		return this.resourceList;
	}
	public void setPelletList(List<String> pelletList)
	{
		this.pelletList = pelletList;
	}
	public void setResourceList(List<ResourceInfo> resourceList)
	{
		this.resourceList = resourceList;
	}
}
