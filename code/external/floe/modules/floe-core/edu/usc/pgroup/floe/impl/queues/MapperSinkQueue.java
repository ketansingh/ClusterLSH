package edu.usc.pgroup.floe.impl.queues;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import edu.usc.pgroup.floe.api.communication.Message;
import edu.usc.pgroup.floe.api.communication.SinkChannel;
import edu.usc.pgroup.floe.impl.communication.MessageImpl;
import edu.usc.pgroup.floe.impl.communication.TCPSinkPushChannel;
import edu.usc.pgroup.floe.util.BitConverter;

public class MapperSinkQueue extends SinkQueue {
	private static final Logger logger = Logger.getLogger(TupleSourceQueue.class.getName());
	private final List<TCPSinkPushChannel> outputSinks;

	public MapperSinkQueue() {
		outputSinks = new ArrayList<TCPSinkPushChannel>();
	}

	@Override
	public synchronized void addSinkChannel(String key, SinkChannel channel) {
		outputSinks.add((TCPSinkPushChannel) channel);
	}

	public synchronized void writeMessage(String key, Message message) {
		
		raiseMessageSentEvent(message);
		
		int index = getEdgeIndex(key);
		System.out.println("Mapper sink queue sending key: " + key + " to edge index " + index);
		TCPSinkPushChannel channel = outputSinks.get(index);

		MessageImpl msg = (MessageImpl) message;
		msg.setKey(key);
		System.out.println("Sending the following message from mapper " + BitConverter.getObject((byte[]) message.getPayload()));
		channel.putMessage(msg);
	}

	public synchronized void writeBroadcastMessage(Message message) {
		raiseMessageSentEvent(message);
		for (TCPSinkPushChannel channel : outputSinks) {
			channel.putMessage(message);
		}
	}

	int getEdgeIndex(String key) {
		int sum = 0;
		for (int i = 0; i < key.length(); i++) {
			sum += key.charAt(i);
		}
		return sum % outputSinks.size();
	}

	@Override
	public int getSize() {
		return 0;
	}
}
