package edu.usc.pgroup.floe.impl.queues;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import edu.usc.pgroup.floe.api.communication.SinkChannel;
import edu.usc.pgroup.floe.impl.communication.MessageImpl;
import edu.usc.pgroup.floe.impl.communication.TCPSinkPushChannel;

public class StreamSinkQueue extends SinkQueue {
	private static final Logger logger = Logger
			.getLogger(TupleSourceQueue.class.getName());
	private final List<TCPSinkPushChannel> outputSinks;
	private int turn = 0;

	public StreamSinkQueue() {
		outputSinks = new ArrayList<TCPSinkPushChannel>();
	}

	@Override
	public synchronized void addSinkChannel(String key, SinkChannel channel) {
		outputSinks.add((TCPSinkPushChannel)channel);
	}

	public synchronized void writeBroadcastMessage(Object message) {
		raiseMessageSentEvent((MessageImpl)message);
		for (TCPSinkPushChannel channel : outputSinks) {
			channel.putMessage((MessageImpl)message);
		}
	}
	public synchronized void writeMessage(Object message) {
		raiseMessageSentEvent((MessageImpl)message);
		if (outputSinks.size() == 0) {
			return;
		}
		turn++;
		int index = turn % outputSinks.size();
		TCPSinkPushChannel channel = outputSinks.get(index);

		MessageImpl msg = (MessageImpl) message;
		channel.putMessage(msg);
	}

	@Override
	public int getSize() {
		// TODO Auto-generated method stub
		return 0;
	}

}
