package edu.usc.pgroup.floe.impl.queues;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import edu.usc.pgroup.floe.api.communication.Message;
import edu.usc.pgroup.floe.api.communication.SinkChannel;
import edu.usc.pgroup.floe.impl.communication.MessageImpl;
import edu.usc.pgroup.floe.impl.communication.TCPSinkPushChannel;

public class TupleSinkQueue extends SinkQueue {
	private static final Logger logger = Logger
			.getLogger(TupleSourceQueue.class.getName());
	private final Map<String, TCPSinkPushChannel> outputSinks;
	private final List<String> tupleKeys;

	public TupleSinkQueue(List<String> tupleKeys) {
		this.tupleKeys = tupleKeys;
		outputSinks = new HashMap<String, TCPSinkPushChannel>();
	}

	@Override
	public void addSinkChannel(String key, SinkChannel channel) {
		if (tupleKeys.contains(key) == false) {
			throw new IllegalArgumentException("Invalid tuple key: " + key
					+ " valid keys: " + tupleKeys.toString());
		}
		synchronized (outputSinks) {
			outputSinks.put(key, (TCPSinkPushChannel) channel);
		}
	}

	public void writeMessage(Map<String, Message> messages) {
		for (String key : messages.keySet()) {
			if (tupleKeys.contains(key) == false) {
				throw new IllegalArgumentException("Tuple key doesn't exist "
						+ key);
			}
			MessageImpl message = (MessageImpl) messages.get(key);
			TCPSinkPushChannel channel = null;
			synchronized (outputSinks) {
				channel = outputSinks.get(key);
			}
			if (channel == null) {
				throw new IllegalArgumentException("Unexpected key " + key);
			}
			
			raiseMessageSentEvent(message);
			channel.putMessage(message);
		}
	}

	public void writeMessageBroadcast(Message message) {
		raiseMessageSentEvent(message);
		synchronized (outputSinks) {
			for (TCPSinkPushChannel channel : outputSinks.values()) {
				channel.putMessage(message);
			}
		}
	}

	@Override
	public int getSize() {
		return 0;
	}
}
