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


/***
 * This manages incoming messages
 * 
 * @author Sreedhar Natarajan (sreedhan@usc.edu)
 * @author Yogesh Simmhan (simmhan@usc.edu)
 * @version v0.1, 2012-01-03
 *
 */

public interface SourceChannel<T> {
	// Every Socket Based Source Channel Implementation will have a constructor 
	// which takes the port no as the parameter	
	public void openConnection();
	public abstract Message<T> getMessage(int timeoutMillis);
	public ConnectionInfo getConnectionInfo();
	public void setConnectionInfo(ConnectionInfo inpConfig);
	public void closeConnection();
}
