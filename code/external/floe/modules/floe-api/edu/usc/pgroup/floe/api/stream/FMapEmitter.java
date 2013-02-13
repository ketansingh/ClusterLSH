package edu.usc.pgroup.floe.api.stream;

public interface FMapEmitter<Output> {
	public void emit(String key, Output object);

	public void emitLandmark(String key);

	public void emitMessageBroadcast(Output output);

	public void emitLandmarkBroadcast();
}
