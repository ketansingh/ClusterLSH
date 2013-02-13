package edu.usc.pgroup.floe.impl.pelletHandlers;

import edu.usc.pgroup.floe.api.framework.pelletmodels.Pellet;
import edu.usc.pgroup.floe.api.state.StateObject;
import edu.usc.pgroup.floe.api.stream.FEmitter;
import edu.usc.pgroup.floe.api.stream.FIterator;
import edu.usc.pgroup.floe.impl.pelletRunners.PelletRunner;
import edu.usc.pgroup.floe.impl.pelletRunners.SingleInSingleOutPelletRunner;
import edu.usc.pgroup.floe.impl.queues.SinkQueue;
import edu.usc.pgroup.floe.impl.queues.SourceQueue;
import edu.usc.pgroup.floe.impl.queues.StreamSinkQueue;
import edu.usc.pgroup.floe.impl.queues.StreamSourceQueue;

public class SingleInSingleOutPelletHandler extends PelletHandler {
	private FEmitter emitter;
	private FIterator iterator;

	@Override
	public Class getPelletModel() {
		return Pellet.class;
	}

	@Override
	public SourceQueue createSourceQueue() {
		return new StreamSourceQueue();
	}

	@Override
	public SinkQueue createSinkQueue() {
		return new StreamSinkQueue();
	}

	@Override
	public PelletRunner createPelletRunner(SourceQueue sourceRouter, SinkQueue sinkRouter, Class pelletClass, StateObject stateObject) {
		StreamSinkQueue streamSinkRouter = (StreamSinkQueue) sinkRouter;
		StreamSourceQueue streamSourceRouter = (StreamSourceQueue) sourceRouter;
		return new SingleInSingleOutPelletRunner(sourceRouter, sinkRouter, pelletClass, stateObject);
	}
}
