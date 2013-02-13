package edu.usc.pgroup.floe.impl.pelletHandlers;

import edu.usc.pgroup.floe.api.state.StateObject;
import edu.usc.pgroup.floe.impl.pelletRunners.PelletRunner;
import edu.usc.pgroup.floe.impl.queues.SinkQueue;
import edu.usc.pgroup.floe.impl.queues.SourceQueue;

public abstract class PelletHandler {

	public abstract SourceQueue createSourceQueue();

	public abstract SinkQueue createSinkQueue();

	public abstract PelletRunner createPelletRunner(SourceQueue sourceQueue, SinkQueue sinkQueue, Class pelletClass, StateObject stateObject);

	public abstract Class getPelletModel();

}
