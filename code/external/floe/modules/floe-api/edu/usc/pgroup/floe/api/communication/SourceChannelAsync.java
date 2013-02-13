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
 * This is an asynchronous, non-blocking channel source that pushes 
 * messages back to the receiver upon arrival. 
 * 
 * @author Sreedhar Natarajan (sreedhan@usc.edu)
 * @author Yogesh Simmhan (simmhan@usc.edu)
 * @version v0.1, 2012-01-03
 *
 */
public interface SourceChannelAsync<T> extends SourceChannel<T> {

	/** 
	 * Register interest in receiving message as callback
	 * @param receiver Object implementing callback method
	 */
	public void registerCallback(Receiver<T> receiver);
	
	/** 
	 * Register interest in receiving message as callback
	 * @param receiver Object implementing callback method
	 */
	public void deregisterCallback(Receiver<T> receiver);
	
	/**
	 * Interface with callback method for received message  
	 */
	public interface Receiver<T> {
		public void notify(Message<T> message);	
	}
}
