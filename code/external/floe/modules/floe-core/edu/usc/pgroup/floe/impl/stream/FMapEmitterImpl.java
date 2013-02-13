package edu.usc.pgroup.floe.impl.stream;

import edu.usc.pgroup.floe.api.communication.Message;
import edu.usc.pgroup.floe.api.stream.FMapEmitter;
import edu.usc.pgroup.floe.impl.communication.MessageImpl;
import edu.usc.pgroup.floe.impl.queues.MapperSinkQueue;
import edu.usc.pgroup.floe.util.BitConverter;

public class FMapEmitterImpl implements FMapEmitter {
	private final MapperSinkQueue sinkRouter;

	public FMapEmitterImpl(MapperSinkQueue sinkRouter) {
		this.sinkRouter = sinkRouter;
	}

	@Override
	public void emit(String key, Object object) {
		Message message = new MessageImpl();
		message.putPayload(BitConverter.getBytes(object));
		message.setKey(key);
		System.out.println("Emitter has passed in object " + object + " passing out: " + BitConverter.getObject((byte[]) message.getPayload()));
		sinkRouter.writeMessage(key, message);
	}

	@Override
	public void emitLandmark(String key) {
		Message message = new MessageImpl();
		message.putPayload(null);
		message.setKey(key);
		message.setLandMark(true);
		sinkRouter.writeMessage(key, message);
	}

	@Override
	public void emitLandmarkBroadcast() {
		Message message = new MessageImpl();
		message.putPayload(null);
		message.setLandMark(true);
		sinkRouter.writeBroadcastMessage(message);
	}

	@Override
	public void emitMessageBroadcast(Object output) {
		throw new UnsupportedOperationException("emitMessagebroadcast not supported");
	}

}
