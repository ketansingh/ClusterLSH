package edu.usc.pgroup.floe.impl.events;


public class MessageEvent {
	public static enum MessageEventType
	{
		Incoming,
		OutGoing
	}
	
	private int messageSize;
	private MessageEventType messageType;
	
	public MessageEvent(MessageEventType msgType, int size)
	{
		messageType = msgType;
		messageSize = size;		
	}
	
	
	public int getMessageSize()
	{
		return messageSize;
	}
	
	public MessageEventType getMessageEventType()
	{
		return messageType;
	}
}
