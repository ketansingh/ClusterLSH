package edu.usc.pgroup.floe.impl.pelletHandlers;

import java.util.List;

import edu.usc.pgroup.floe.api.framework.pelletmodels.StreamTupleInStreamTupleOutPellet;
import edu.usc.pgroup.floe.api.state.StateObject;
import edu.usc.pgroup.floe.impl.pelletRunners.PelletRunner;
import edu.usc.pgroup.floe.impl.pelletRunners.StreamTupleInStreamTupleOutPelletRunner;
import edu.usc.pgroup.floe.impl.queues.SinkQueue;
import edu.usc.pgroup.floe.impl.queues.SourceQueue;
import edu.usc.pgroup.floe.impl.queues.TupleSinkQueue;
import edu.usc.pgroup.floe.impl.queues.TupleSourceQueue;

public class StreamTupleInStreamTupleOutPelletHandler extends PelletHandler {
	private final List<String> inTupleKeys;
	private final List<String> outTupleKeys;

	public StreamTupleInStreamTupleOutPelletHandler(List<String> inTupleKeys, List<String> outTupleKeys) {
		this.inTupleKeys = inTupleKeys;
		this.outTupleKeys = outTupleKeys;
	}

	@Override
	public SourceQueue createSourceQueue() {
		return new TupleSourceQueue(this.inTupleKeys);
	}

	@Override
	public SinkQueue createSinkQueue() {
		return new TupleSinkQueue(this.outTupleKeys);
	}

	@Override
	public PelletRunner createPelletRunner(SourceQueue sourceQueue, SinkQueue sinkQueue, Class pelletClass, StateObject stateObject) {
		return new StreamTupleInStreamTupleOutPelletRunner(sourceQueue, sinkQueue, pelletClass, stateObject, inTupleKeys, outTupleKeys);
	}

	@Override
	public Class getPelletModel() {
		return StreamTupleInStreamTupleOutPellet.class;
	}

}
