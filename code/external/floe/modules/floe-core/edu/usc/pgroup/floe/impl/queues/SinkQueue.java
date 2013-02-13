package edu.usc.pgroup.floe.impl.queues;

import java.util.ArrayList;
import java.util.List;

import edu.usc.pgroup.floe.api.communication.Message;
import edu.usc.pgroup.floe.api.communication.SinkChannel;
import edu.usc.pgroup.floe.impl.events.MessageEvent;
import edu.usc.pgroup.floe.impl.events.MessageEventListener;
import edu.usc.pgroup.floe.impl.events.MessageEvent.MessageEventType;

public abstract class SinkQueue {
	public abstract void addSinkChannel(String key, SinkChannel channel);
	public abstract int getSize();
	
	protected List<MessageEventListener> messageEventListeners = new ArrayList<MessageEventListener>();
	
	public void addMessageEventListener(MessageEventListener listener)
	{
		messageEventListeners.add(listener);
	}
	
	protected void raiseMessageSentEvent(Message message)
	{
		MessageEvent event = new MessageEvent(MessageEventType.OutGoing,-1);
		for(MessageEventListener listener : messageEventListeners)
		{
			listener.handleMessageEvent(event);
		}
	}
}
