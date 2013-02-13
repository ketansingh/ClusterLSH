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

package edu.usc.pgroup.floe.api.framework;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/***
 * This described the resources for a Flake within a container.  
 * 
 * @author Sreedhar Natarajan (sreedhan@usc.edu)
 * @author Yogesh Simmhan (simmhan@usc.edu)
 * @version v0.1, 2012-01-03
 *
 */
//
@XmlRootElement(name = "Port")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType( propOrder = {
 "numCores"
})
public class ResourceInfo {

 
 protected Integer numCores;
 //protected String tagName;	// TO define the affinity of Resource Requirement { Windows,Unix }

 /**
  * Gets the value of the numCores property.
  * 
  * @return
  *     possible object is
  *     {@link BigInteger }
  *     
  */
 public int getNumCores() {
     return numCores;
 }

 /**
  * Sets the value of the numCores property.
  * 
  * @param value
  *     allowed object is
  *     {@link BigInteger }
  *     
  */
 	public void setNumCores(int value) {
 		this.numCores = value;
 	}
 	public ResourceInfo()
 	{		
	}	
 	public ResourceInfo(int processor)
	{
		this.numCores = processor;
	}	
	public int getNumberOfCores()
	{
		return this.numCores;
	}
	public void setNumberOfCores(int numCores)
	{
		this.numCores = numCores;
	}
	/*public void setTagName(String inpTag)
	{
		this.tagName = inpTag;
	}
	public String getTagName()
	{
		return this.tagName;
	}*/

}
