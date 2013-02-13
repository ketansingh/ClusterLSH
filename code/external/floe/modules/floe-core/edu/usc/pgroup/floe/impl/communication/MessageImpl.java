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
package edu.usc.pgroup.floe.impl.communication;

import java.io.Serializable;

import edu.usc.pgroup.floe.api.communication.Message;
/***
 
 * @author Sreedhar Natarajan (sreedhan@usc.edu)
 * @author Yogesh Simmhan (simmhan@usc.edu)
 * @version v0.1, 2012-01-11
 *
 */


public class MessageImpl<T> implements Message<T>,Serializable
{	
	private static final long serialVersionUID = 1L;
	T payLoad;
	String tag;
	Long timeStamp;
	private String key;
	private boolean isLandmark = false;
	
	@Override
	public T getPayload()
	{
		return this.payLoad;
	}
	@Override
	public void putPayload(T payload)
	{
		this.payLoad = null;
		this.payLoad = payload;
	}
	
	@Override
	public String getKey() {
		return key;
	}
	
	@Override
	public void setKey(String key) {
		this.key = key;
	}
	
	@Override
	public String getTag() {
		return this.tag;
	}
	
	@Override
	public Long getTimeStampMilliSecs() {
		return this.timeStamp;
	}
	
	@Override
	public void setTag(String t) {
		this.tag = t;		
	}
	
	@Override
	public void setTimeStampMilliSecs(Long ts) {
		this.timeStamp = ts;
	}
	
	@Override
	public void setLandMark(boolean landmark) {
		this.isLandmark = landmark;
	}
	@Override
	public boolean getLandMark()  {
		return this.isLandmark;
	}
	
	
}
