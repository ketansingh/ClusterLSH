package edu.usc.pgroup.floe.impl.pelletRunners;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import edu.usc.pgroup.floe.api.framework.pelletmodels.StreamInStreamOutPellet;
import edu.usc.pgroup.floe.api.state.StateObject;
import edu.usc.pgroup.floe.api.stream.FEmitter;
import edu.usc.pgroup.floe.api.stream.FIterator;
import edu.usc.pgroup.floe.impl.queues.SinkQueue;
import edu.usc.pgroup.floe.impl.queues.SourceQueue;
import edu.usc.pgroup.floe.impl.queues.StreamSinkQueue;
import edu.usc.pgroup.floe.impl.queues.StreamSourceQueue;
import edu.usc.pgroup.floe.impl.stream.FEmitterImpl;
import edu.usc.pgroup.floe.impl.stream.FIteratorImpl;

public class StreamInStreamOutPelletRunner extends PelletRunner {
	private final FIterator iterator;
	private final FEmitter emitter;
	private Object pelletInstance;

	public StreamInStreamOutPelletRunner(SourceQueue sourceQueue, SinkQueue sinkQueue, Class pellet, StateObject stateObject) {
		super(sourceQueue, sinkQueue, pellet, stateObject);
		this.iterator = new FIteratorImpl((StreamSourceQueue) sourceQueue);
		this.emitter = new FEmitterImpl((StreamSinkQueue) sinkQueue);
	}

	@Override
	public void runPellet() {
		try {
			pelletInstance = pelletClass.newInstance();
			Class partypes[] = new Class[] { FIterator.class, FEmitter.class, StateObject.class };
			Method invokeMethod = pelletClass.getMethod("invoke", partypes);
			Object[] argList = { iterator, emitter, stateObject };
			invokeMethod.invoke(pelletInstance, argList);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Object getPelletInstance() {
		return pelletInstance;
	}

	@Override
	public Class getPelletModel() {
		return StreamInStreamOutPellet.class;
	}

	@Override
	public void sendPauseLandmark() {
		((FIteratorImpl) iterator).setPauseLandmark();
	}

}
