package edu.usc.pgroup.floe.impl.pelletHandlers;

import edu.usc.pgroup.floe.api.framework.pelletmodels.MapperPellet;
import edu.usc.pgroup.floe.api.state.StateObject;
import edu.usc.pgroup.floe.api.stream.FIterator;
import edu.usc.pgroup.floe.api.stream.FMapEmitter;
import edu.usc.pgroup.floe.impl.pelletRunners.MapperPelletRunner;
import edu.usc.pgroup.floe.impl.pelletRunners.PelletRunner;
import edu.usc.pgroup.floe.impl.queues.MapperSinkQueue;
import edu.usc.pgroup.floe.impl.queues.SinkQueue;
import edu.usc.pgroup.floe.impl.queues.SourceQueue;
import edu.usc.pgroup.floe.impl.queues.StreamSourceQueue;

public class MapperPelletHandler extends PelletHandler {
	private FMapEmitter emitter;
	private FIterator iterator;

	@Override
	public SourceQueue createSourceQueue() {
		return new StreamSourceQueue();
	}

	@Override
	public SinkQueue createSinkQueue() {
		return new MapperSinkQueue();
	}

	@Override
	public PelletRunner createPelletRunner(SourceQueue sourceRouter, SinkQueue sinkRouter, Class pelletClass, StateObject stateObject) {
		MapperSinkQueue mapperSinkRouter = (MapperSinkQueue) sinkRouter;
		StreamSourceQueue streamSourceIterator = (StreamSourceQueue) sourceRouter;

		return new MapperPelletRunner(sourceRouter, sinkRouter, pelletClass, stateObject);
	}

	@Override
	public Class getPelletModel() {
		return MapperPellet.class;
	}

}
