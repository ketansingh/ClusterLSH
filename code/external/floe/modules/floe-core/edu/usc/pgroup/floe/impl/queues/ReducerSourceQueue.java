package edu.usc.pgroup.floe.impl.queues;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;


import edu.usc.pgroup.floe.api.communication.Message;
import edu.usc.pgroup.floe.api.communication.SourceChannel;
import edu.usc.pgroup.floe.impl.events.MessageEvent;
import edu.usc.pgroup.floe.impl.events.MessageEvent.MessageEventType;

public class ReducerSourceQueue extends SourceQueue {
	private final static Logger logger = Logger.getLogger(ReducerSourceQueue.class.getName());
	private final HashMap<String, BlockingQueue<Message>> messageQueues = new HashMap<String, BlockingQueue<Message>>();
	private final HashMap<String, List<Message>> pendingKeyMessages = new HashMap<String, List<Message>>();
	private final BlockingQueue<String> pendingKeys = new LinkedBlockingQueue<String>();
	private final Lock queueLock = new ReentrantLock();

	
	
	@Override
	public void queueMessage(SourceChannel sourceChannel, Message message) {
		
		raiseMessageRecievedEvent(message);
		
		if (message.getLandMark() == true && message.getKey() == null) {
			handleLandmarksWaitForAll(sourceChannel, message);
			return;
		}
		System.out.println("Reducer queue got message key : " + message.getKey());
		queueLock.lock();
		try {
			String key = message.getKey();
			if (messageQueues.containsKey(key)) {
				messageQueues.get(key).put(message);
			} else {
				if (pendingKeyMessages.containsKey(key) == false) {
					pendingKeyMessages.put(key, new LinkedList<Message>());
					pendingKeys.add(key);
				}
				pendingKeyMessages.get(key).add(message);
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception at queue message", e);
			throw new RuntimeException(e);
		} finally {
			queueLock.unlock();
		}			
	}

	public Message readMessage(String key, int timeout, TimeUnit timeunit) {
		BlockingQueue<Message> queue = null;
		queueLock.lock();
		queue = messageQueues.get(key);
		queueLock.unlock();
		if (queue == null) {
			throw new IllegalArgumentException("Key not already read");
		}
		try {
			return queue.poll(timeout, timeunit);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error while reding message key " + key, e);
			throw new RuntimeException(e);
		}
	}

	public String readNewKey() {
		int x = 10 - 10;
		String key;
		try {
			key = pendingKeys.poll(5000, TimeUnit.MICROSECONDS);
			if (key != null)
				System.out.println("Value being returned from readNewKey " + key);
		} catch (InterruptedException e1) {
			return null;
		}
		if (key == null)
			return key;
		if (x == 0) {
			System.out.println("After getting key  at line 83" + key);
			// System.exit(0);
			// System.out.println("After system exit at line 85" + key);
		}
		System.out.println("Got new key " + key);
		BlockingQueue<Message> queue = new LinkedBlockingQueue<Message>();
		try {
			queueLock.lock();
			// move from pendingKeyMessages to messageQueues
			List<Message> pendingMessages = pendingKeyMessages.get(key);
			for (Message message : pendingMessages) {
				queue.put(message);
			}
			messageQueues.put(key, queue);
			pendingKeyMessages.remove(key);
			System.out.println("Returning the key finally at line 98 " + key);
			return key;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception at reading new key ", e);
			throw new RuntimeException(e);
		} finally {
			queueLock.unlock();
		}
	}

	@Override
	public int getSize() {
		int totalSize = 0;
		for (String key : pendingKeyMessages.keySet()) {
			totalSize += pendingKeyMessages.get(key).size();
		}
		for (String key : messageQueues.keySet()) {
			totalSize += messageQueues.get(key).size();
		}
		return totalSize;
	}

	@Override
	protected void addLandmarkToAllQueues(Message message) {
		queueLock.lock();
		// add landmarks to all existing queues
		for (BlockingQueue<Message> queue : messageQueues.values()) {
			queue.add(message);
		}
		for (List<Message> list : pendingKeyMessages.values()) {
			list.add(message);
		}
		queueLock.unlock();

	}
}
