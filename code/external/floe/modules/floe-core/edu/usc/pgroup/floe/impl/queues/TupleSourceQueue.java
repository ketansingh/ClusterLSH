package edu.usc.pgroup.floe.impl.queues;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.usc.pgroup.floe.api.communication.Message;
import edu.usc.pgroup.floe.api.communication.SourceChannel;

public class TupleSourceQueue extends SourceQueue {
	private static final Logger logger = Logger.getLogger(TupleSourceQueue.class.getName());
	private final Map<String, LinkedBlockingDeque<Message>> queues;
	private final List<String> tupleKeys;

	public TupleSourceQueue(List<String> tupleKeys) {
		this.tupleKeys = tupleKeys;
		queues = new HashMap<String, LinkedBlockingDeque<Message>>();
		for (String key : this.tupleKeys) {
			queues.put(key, new LinkedBlockingDeque<Message>());
		}
	}

	@Override
	public void queueMessage(SourceChannel sourceChannel, Message message) {
		
		raiseMessageRecievedEvent(message);
		
		if (message.getLandMark() == true && message.getKey() == null) {
			handleLandmarksWaitForAll(sourceChannel, message);
			return;
		}
		try {
			String key = message.getKey();
			BlockingQueue<Message> queue;
			queue = queues.get(key);
			if (queue == null) {
				throw new IllegalArgumentException("Unexpected key " + key);
			}
			queue.put(message);
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, "Exception", e);
			throw new RuntimeException(e);
		}
	}

	public Message readMessage(String key, int timeout, TimeUnit timeunit) {
		if (tupleKeys.contains(key) == false) {
			throw new IllegalArgumentException("Unknown key " + key);
		}
		BlockingQueue<Message> queue = queues.get(key);
		try {
			return queue.poll(timeout, timeunit);
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, "Exception ", e);
			throw new RuntimeException(e);
		}
	}

	public void putMessageBack(String key, Message message) {
		LinkedBlockingDeque<Message> queue = queues.get(key);
		try {
			queue.putFirst(message);
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, "Exception ", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public int getSize() {
		int totalSize = 0;
		for (String key : tupleKeys) {
			BlockingQueue<Message> queue;
			queue = queues.get(key);
			totalSize += queue.size();
		}
		return totalSize;
	}

	@Override
	protected void addLandmarkToAllQueues(Message landmarkMessage) {
		throw new UnsupportedOperationException();
	}
}
