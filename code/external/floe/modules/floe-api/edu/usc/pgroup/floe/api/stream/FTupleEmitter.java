package edu.usc.pgroup.floe.api.stream;

import java.util.Map;

public interface FTupleEmitter {
	public void emit(Map<String, Object> messages);

	public void emitLandmark(String tupleKey);

	public void emitMessageBroadcast(Object output);

	public void emitLandmarkBroadcast();
}
