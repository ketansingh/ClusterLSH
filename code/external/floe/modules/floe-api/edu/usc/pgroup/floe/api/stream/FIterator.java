package edu.usc.pgroup.floe.api.stream;

import java.util.concurrent.TimeUnit;

import edu.usc.pgroup.floe.api.exception.LandmarkException;

public interface FIterator<Input> {
	public Input next() throws LandmarkException;

	public Input next(int timeout, TimeUnit timeUnit) throws LandmarkException;
}
