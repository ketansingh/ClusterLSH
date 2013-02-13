package edu.usc.pgroup.floe.impl.pelletRunners;

import edu.usc.pgroup.floe.api.state.StateObject;
import edu.usc.pgroup.floe.impl.queues.SinkQueue;
import edu.usc.pgroup.floe.impl.queues.SourceQueue;

public abstract class PelletRunner {
	protected final Class pelletClass;
	protected final SourceQueue sourceQueue;
	protected final SinkQueue sinkQueue;
	protected final StateObject stateObject;

	public PelletRunner(SourceQueue sourceQueue, SinkQueue sinkQueue, Class pelletClass, StateObject stateObject) {
		this.pelletClass = pelletClass;
		this.sourceQueue = sourceQueue;
		this.sinkQueue = sinkQueue;
		this.stateObject = stateObject;
	}

	public abstract void sendPauseLandmark();

	public abstract void runPellet();

	public abstract Class getPelletModel();

	public abstract Object getPelletInstance();
}
