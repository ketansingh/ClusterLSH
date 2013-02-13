package edu.usc.pgroup.floe.api.stream;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import edu.usc.pgroup.floe.api.exception.LandmarkException;

public interface FTupleIterator {
	public Map<String, Object> next() throws LandmarkException;

	public Map<String, Object> next(int timeout, TimeUnit timeunit) throws LandmarkException;
}
