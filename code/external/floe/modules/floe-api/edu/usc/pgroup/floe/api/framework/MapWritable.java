package edu.usc.pgroup.floe.api.framework;

import java.util.Map;

import edu.usc.pgroup.floe.api.communication.Message;

/***
 * An Interface to Write Map Values on to the Flake's Output Buffer
 * 
 * @author Sreedhar Natarajan (sreedhan@usc.edu)
 * @author Yogesh Simmhan (simmhan@usc.edu)
 * @version v0.1, 2012-01-11
 *
 */

public interface MapWritable<T> extends Writable
{
	public void write(Map<String,Message<T>> inpMsg);
}
