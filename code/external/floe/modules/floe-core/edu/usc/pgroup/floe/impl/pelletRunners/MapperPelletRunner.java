package edu.usc.pgroup.floe.impl.pelletRunners;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.usc.pgroup.floe.api.framework.pelletmodels.MapperPellet;
import edu.usc.pgroup.floe.api.state.StateObject;
import edu.usc.pgroup.floe.api.stream.FIterator;
import edu.usc.pgroup.floe.api.stream.FMapEmitter;
import edu.usc.pgroup.floe.impl.queues.MapperSinkQueue;
import edu.usc.pgroup.floe.impl.queues.SinkQueue;
import edu.usc.pgroup.floe.impl.queues.SourceQueue;
import edu.usc.pgroup.floe.impl.queues.StreamSourceQueue;
import edu.usc.pgroup.floe.impl.stream.FIteratorImpl;
import edu.usc.pgroup.floe.impl.stream.FMapEmitterImpl;

public class MapperPelletRunner extends PelletRunner {
	private final static Logger logger = Logger.getLogger(MapperPelletRunner.class.getName());
	private final FIterator iterator;
	private final FMapEmitter emitter;
	private Object pelletInstance;

	public MapperPelletRunner(SourceQueue sourceQueue, SinkQueue sinkQueue, Class pellet, StateObject stateObject) {
		super(sourceQueue, sinkQueue, pellet, stateObject);
		this.iterator = new FIteratorImpl((StreamSourceQueue) sourceQueue);
		this.emitter = new FMapEmitterImpl((MapperSinkQueue) sinkQueue);
	}

	@Override
	public void runPellet() {
		try {
			pelletInstance = pelletClass.newInstance();
			Class partypes[] = new Class[] { FIterator.class, FMapEmitter.class };
			Method invokeMethod = pelletClass.getMethod("invoke", partypes);
			Object[] argList = { iterator, emitter };
			invokeMethod.invoke(pelletInstance, argList);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error in running mapper pellet", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public Class getPelletModel() {
		return MapperPellet.class;
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
