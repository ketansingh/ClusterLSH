package edu.usc.pgroup.floe.impl.pelletRunners;

import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.usc.pgroup.floe.api.framework.pelletmodels.SingleInStreamTupleOutPellet;
import edu.usc.pgroup.floe.api.state.StateObject;
import edu.usc.pgroup.floe.api.stream.FIterator;
import edu.usc.pgroup.floe.api.stream.FTupleEmitter;
import edu.usc.pgroup.floe.impl.queues.SinkQueue;
import edu.usc.pgroup.floe.impl.queues.SourceQueue;
import edu.usc.pgroup.floe.impl.queues.StreamSourceQueue;
import edu.usc.pgroup.floe.impl.queues.TupleSinkQueue;
import edu.usc.pgroup.floe.impl.stream.FIteratorImpl;
import edu.usc.pgroup.floe.impl.stream.FTupleEmitterImpl;

public class SingleInStreamTupleOutPelletRunner extends PelletRunner {
	private static final Logger logger = Logger.getLogger(SingleInStreamTupleOutPelletRunner.class.getName());
	private final List<String> outputTupleKeys;
	private final FIterator iterator;
	private final FTupleEmitter emitter;
	private Object pelletInstance;

	public SingleInStreamTupleOutPelletRunner(SourceQueue sourceQueue, SinkQueue sinkQueue, Class pelletClass, StateObject stateObject,
			List<String> output) {
		super(sourceQueue, sinkQueue, pelletClass, stateObject);
		this.outputTupleKeys = output;
		iterator = new FIteratorImpl((StreamSourceQueue) sourceQueue);
		emitter = new FTupleEmitterImpl((TupleSinkQueue) sinkQueue);
	}

	@Override
	public void sendPauseLandmark() {
		((FIteratorImpl) iterator).setPauseLandmark();
	}

	@Override
	public void runPellet() {
		StreamSourceQueue streamSourceQueue = (StreamSourceQueue) sourceQueue;
		TupleSinkQueue tupleSinkQueue = (TupleSinkQueue) sinkQueue;
		try {
			pelletInstance = pelletClass.newInstance();
			Class partypes[] = new Class[] { Object.class, FTupleEmitter.class, StateObject.class };
			Method invokeMethod = pelletClass.getMethod("invoke", partypes);
			Object in = iterator.next();
			Object[] argList = { in, emitter, stateObject };
			invokeMethod.invoke(pelletInstance, argList);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Pellet invocation error in class" + pelletClass.getCanonicalName(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public Class getPelletModel() {
		return SingleInStreamTupleOutPellet.class;
	}

	@Override
	public Object getPelletInstance() {
		return pelletInstance;
	}

}
