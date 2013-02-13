package edu.usc.pgroup.floe.impl.pelletHandlers;

import edu.usc.pgroup.floe.api.framework.pelletmodels.ReducerPellet;
import edu.usc.pgroup.floe.api.state.StateObject;
import edu.usc.pgroup.floe.api.stream.FIterator;
import edu.usc.pgroup.floe.api.stream.FMapEmitter;
import edu.usc.pgroup.floe.impl.pelletRunners.PelletRunner;
import edu.usc.pgroup.floe.impl.pelletRunners.ReducerPelletRunner;
import edu.usc.pgroup.floe.impl.queues.MapperSinkQueue;
import edu.usc.pgroup.floe.impl.queues.ReducerSourceQueue;
import edu.usc.pgroup.floe.impl.queues.SinkQueue;
import edu.usc.pgroup.floe.impl.queues.SourceQueue;

public class ReducerPelletHandler extends PelletHandler {
	FIterator iterator;
	FMapEmitter emitter;

	@Override
	public SourceQueue createSourceQueue() {
		return new ReducerSourceQueue();
	}

	@Override
	public SinkQueue createSinkQueue() {
		return new MapperSinkQueue();
	}

	@Override
	public PelletRunner createPelletRunner(SourceQueue sourceRouter, SinkQueue sinkRouter, Class pelletClass, StateObject stateObject) {
		MapperSinkQueue mapperSinkQueue = (MapperSinkQueue) sinkRouter;
		ReducerSourceQueue reducerSourceRouter = (ReducerSourceQueue) sourceRouter;
		return new ReducerPelletRunner(sourceRouter, sinkRouter, pelletClass, stateObject);
	}

	@Override
	public Class getPelletModel() {
		return ReducerPellet.class;
	}

}
