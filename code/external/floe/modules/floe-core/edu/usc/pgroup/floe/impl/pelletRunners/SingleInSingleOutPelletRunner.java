package edu.usc.pgroup.floe.impl.pelletRunners;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.usc.pgroup.floe.api.framework.pelletmodels.Pellet;
import edu.usc.pgroup.floe.api.state.StateObject;
import edu.usc.pgroup.floe.api.stream.FEmitter;
import edu.usc.pgroup.floe.api.stream.FIterator;
import edu.usc.pgroup.floe.impl.queues.SinkQueue;
import edu.usc.pgroup.floe.impl.queues.SourceQueue;
import edu.usc.pgroup.floe.impl.queues.StreamSinkQueue;
import edu.usc.pgroup.floe.impl.queues.StreamSourceQueue;
import edu.usc.pgroup.floe.impl.stream.FEmitterImpl;
import edu.usc.pgroup.floe.impl.stream.FIteratorImpl;

public class SingleInSingleOutPelletRunner extends PelletRunner {
	private static Logger logger = Logger.getLogger(SingleInSingleOutPelletRunner.class.getName());
	private final FIterator iterator;
	private final FEmitter emitter;
	private Object pelletInstance;

	public SingleInSingleOutPelletRunner(SourceQueue sourceQueue, SinkQueue sinkQueue, Class pellet, StateObject stateObject) {
		super(sourceQueue, sinkQueue, pellet, stateObject);
		this.iterator = new FIteratorImpl((StreamSourceQueue) sourceQueue);
		this.emitter = new FEmitterImpl((StreamSinkQueue) sinkQueue);
	}

	@Override
	public void runPellet() {
		try {
			pelletInstance = pelletClass.newInstance();
			Object input = iterator.next();
			Class partypes[] = new Class[] { Object.class, StateObject.class };
			Method invokeMethod = pelletClass.getMethod("invoke", partypes);
			Object[] argList = { input, stateObject };
			Object output = invokeMethod.invoke(pelletInstance, argList);
			emitter.emit(output);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Method not found in class " + pelletClass.getCanonicalName(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public Class getPelletModel() {
		return Pellet.class;
	}

	@Override
	public void sendPauseLandmark() {
		((FIteratorImpl) iterator).setPauseLandmark();
	}

	@Override
	public Object getPelletInstance() {
		return pelletInstance;
	}

}
