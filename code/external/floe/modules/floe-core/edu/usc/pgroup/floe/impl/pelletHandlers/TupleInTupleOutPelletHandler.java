package edu.usc.pgroup.floe.impl.pelletHandlers;

import java.util.List;

import edu.usc.pgroup.floe.api.framework.pelletmodels.TupleInTupleOutPellet;
import edu.usc.pgroup.floe.api.state.StateObject;
import edu.usc.pgroup.floe.api.stream.FTupleEmitter;
import edu.usc.pgroup.floe.api.stream.FTupleIterator;
import edu.usc.pgroup.floe.impl.pelletRunners.PelletRunner;
import edu.usc.pgroup.floe.impl.pelletRunners.TupleInTupleOutPelletRunner;
import edu.usc.pgroup.floe.impl.queues.SinkQueue;
import edu.usc.pgroup.floe.impl.queues.SourceQueue;
import edu.usc.pgroup.floe.impl.queues.TupleSinkQueue;
import edu.usc.pgroup.floe.impl.queues.TupleSourceQueue;

public class TupleInTupleOutPelletHandler extends PelletHandler {
	private final List<String> inputPortTupleKeys;
	private final List<String> outputPortTupleKeys;
	private FTupleIterator iterator;
	private FTupleEmitter emitter;

	public TupleInTupleOutPelletHandler(List<String> inputPortTupleKeys, List<String> outputPortTupleKeys) {
		this.inputPortTupleKeys = inputPortTupleKeys;
		this.outputPortTupleKeys = outputPortTupleKeys;
	}

	@Override
	public SourceQueue createSourceQueue() {
		return new TupleSourceQueue(this.inputPortTupleKeys);
	}

	@Override
	public SinkQueue createSinkQueue() {
		return new TupleSinkQueue(this.outputPortTupleKeys);
	}

	@Override
	public PelletRunner createPelletRunner(SourceQueue sourceRouter, SinkQueue sinkRouter, Class pelletClass, StateObject stateObject) {
		TupleSinkQueue tupleSinkRouter = (TupleSinkQueue) sinkRouter;
		TupleSourceQueue tupleSourceRouter = (TupleSourceQueue) sourceRouter;
		return new TupleInTupleOutPelletRunner(tupleSourceRouter, tupleSinkRouter, pelletClass, stateObject, inputPortTupleKeys);
	}

	@Override
	public Class getPelletModel() {
		return TupleInTupleOutPellet.class;
	}

}
