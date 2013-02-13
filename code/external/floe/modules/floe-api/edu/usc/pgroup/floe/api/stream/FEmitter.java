package edu.usc.pgroup.floe.api.stream;

public interface FEmitter<Output> {
	public void emit(Output object);

	public void emitLandmark();

	public void emitMessageBroadcast(Output output);

	public void emitLandmarkBroadcast();
}
