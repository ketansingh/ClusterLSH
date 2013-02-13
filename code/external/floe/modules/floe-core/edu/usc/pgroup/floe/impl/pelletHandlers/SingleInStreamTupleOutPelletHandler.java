package edu.usc.pgroup.floe.impl.pelletHandlers;

import java.util.List;

import edu.usc.pgroup.floe.api.framework.pelletmodels.SingleInStreamTupleOutPellet;
import edu.usc.pgroup.floe.api.state.StateObject;
import edu.usc.pgroup.floe.impl.pelletRunners.PelletRunner;
import edu.usc.pgroup.floe.impl.pelletRunners.SingleInStreamTupleOutPelletRunner;
import edu.usc.pgroup.floe.impl.queues.SinkQueue;
import edu.usc.pgroup.floe.impl.queues.SourceQueue;
import edu.usc.pgroup.floe.impl.queues.StreamSourceQueue;
import edu.usc.pgroup.floe.impl.queues.TupleSinkQueue;

public class SingleInStreamTupleOutPelletHandler extends PelletHandler {
	private final List<String> outTupleKeys;

	public SingleInStreamTupleOutPelletHandler(List<String> outTupleKeys) {
		this.outTupleKeys = outTupleKeys;
	}

	@Override
	public SourceQueue createSourceQueue() {
		return new StreamSourceQueue();
	}

	@Override
	public SinkQueue createSinkQueue() {
		return new TupleSinkQueue(this.outTupleKeys);
	}

	@Override
	public PelletRunner createPelletRunner(SourceQueue sourceQueue, SinkQueue sinkQueue, Class pelletClass, StateObject stateObject) {
		return new SingleInStreamTupleOutPelletRunner(sourceQueue, sinkQueue, pelletClass, stateObject, outTupleKeys);
	}

	@Override
	public Class getPelletModel() {
		return SingleInStreamTupleOutPellet.class;
	}

}
