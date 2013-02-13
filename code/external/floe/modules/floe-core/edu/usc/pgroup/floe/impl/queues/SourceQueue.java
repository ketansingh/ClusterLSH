package edu.usc.pgroup.floe.impl.queues;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import edu.usc.pgroup.floe.api.communication.Message;
import edu.usc.pgroup.floe.api.communication.SourceChannel;
import edu.usc.pgroup.floe.impl.events.MessageEvent;
import edu.usc.pgroup.floe.impl.events.MessageEventListener;
import edu.usc.pgroup.floe.impl.events.MessageEvent.MessageEventType;

public abstract class SourceQueue {
	private final List<SourceChannel> sourceChannels = new LinkedList<SourceChannel>();
	private final HashMap<SourceChannel, List<Message>> landmarkMap = new HashMap<SourceChannel, List<Message>>();
	
	protected List<MessageEventListener> messageEventListeners = new ArrayList<MessageEventListener>();
	
	protected abstract void addLandmarkToAllQueues(Message landmarkMessage);

	public void addSourceChannel(SourceChannel sourceChannel) {
		sourceChannels.add(sourceChannel);
	}

	protected void handleLandmarksWaitForAll(SourceChannel sourceChannel, Message message) {
		if (sourceChannels.contains(sourceChannel) == false) {
			throw new IllegalArgumentException("Message from a not added source channel");
		}
		int landmarkMapSize;
		synchronized (landmarkMap) {
			List<Message> existingList = landmarkMap.get(sourceChannel);
			if (existingList == null) {
				existingList = new LinkedList<Message>();
			}
			existingList.add(message);
			landmarkMap.put(sourceChannel, existingList);
			landmarkMapSize = landmarkMap.size();
		}
		// Landmarks from all input channels
		if (landmarkMapSize == sourceChannels.size()) {
			addLandmarkToAllQueues(message);
			synchronized (landmarkMap) {
				// remove one message from all the source channel
				List<SourceChannel> keysToRemove = new LinkedList<SourceChannel>();
				for (SourceChannel channel : landmarkMap.keySet()) {
					LinkedList<Message> list = (LinkedList) landmarkMap.get(channel);
					list.removeFirst();
					if (list.size() == 0) {
						keysToRemove.add(channel);
					}
				}
				for (SourceChannel channel : keysToRemove) {
					landmarkMap.remove(channel);
				}
			}
		}
	}

	public abstract void queueMessage(SourceChannel sourceChannel, Message message);

	public abstract int getSize();
	
	public void addMessageEventListener(MessageEventListener listener)
	{
		messageEventListeners.add(listener);
	}
	
	protected void raiseMessageRecievedEvent(Message message)
	{
		MessageEvent event = new MessageEvent(MessageEventType.Incoming,-1);
		for(MessageEventListener listener : messageEventListeners)
		{
			listener.handleMessageEvent(event);
		}
	}
}
