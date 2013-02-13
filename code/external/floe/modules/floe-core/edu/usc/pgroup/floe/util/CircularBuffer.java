package edu.usc.pgroup.floe.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CircularBuffer<T> 
{
	List<T> bufferList;
	List<Integer> participantList;
	int versionList[];
	Lock bufferLock; 		// Lock to add/remove entries from Buffer
	Lock participantLock; 	// Lock to add participants on the Buffer
	Lock spaceLock;			// Lock to wait on empty space on Buffer
	Lock fullLock;			// Lock to wait for new Message on Buffer
	int head;
	int tail;
	int size;
	public CircularBuffer(int size)
	{
		this.bufferList = new ArrayList<T>(size);
		this.participantList = new ArrayList<Integer>();
		this.bufferLock = new ReentrantLock();
		this.fullLock = new ReentrantLock();
		this.participantLock = new ReentrantLock();
		this.spaceLock = new ReentrantLock();
		this.head = 0;
		this.tail = 1;
		this.size = size;
		this.versionList = new int[size];
	}
	public synchronized int getIndex()
	{
		this.participantList.add(0);
		return this.participantList.size()-1;
	}
	public void insertMessage(T inpMessage)
	{
		if(this.bufferList.size()<this.size)
		{
			// Update the Version List for Each Element
			this.versionList[this.bufferList.size()]++;
			this.bufferList.add(inpMessage);			 
		}
		else
		{
			System.out.println("Tail Value " + this.tail);
			this.versionList[this.tail]++;
			this.bufferList.set(this.tail, inpMessage);
		}
		this.tail++;
		if(this.tail >= this.size)
		{
			this.tail = 0;
		}
		
		if(Math.abs(tail-head) > this.size/2)	
		{
			updateWindow();
		}			
	}
	public void add(T inpMessage)
	{ 
		this.bufferLock.lock();
		// Check if there is Space to add a new Message
		int absValue = Math.abs(this.head-this.tail); 
		if(absValue == this.size-1 || absValue == 0)
		{
			// If there is no space release the buffer lock and wait for space to be empty
			try
			{
				System.out.println("Waiting to Find a Free Space");
				synchronized(this.spaceLock)
				{
					this.spaceLock.wait();
					this.insertMessage(inpMessage);
					this.bufferLock.unlock();
				}
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
		else
		{			
			this.insertMessage(inpMessage);
			this.bufferLock.unlock();
		}
	}
	public void updateWindow()
	{
		int noofDeleted = 0;
		int smallestIndex = 10000;
		int smallestVersion = 10000;
		System.out.println("Reached a Threshhold to Advance Head");
		this.participantLock.lock();
		for(int i=0;i<this.participantList.size();i++)
		{
			int currVersion = this.versionList[i];
			if((currVersion <= smallestVersion)&&currVersion>0)
			{
				if(this.participantList.get(i) < smallestIndex)
				{
					smallestIndex = this.participantList.get(i);
					smallestVersion = currVersion;
				}
			}
		}
		if(smallestIndex!= 10000)
			this.head = smallestIndex;
		System.out.println("The Head Index Value is " + this.head + " and Tail is " + this.tail);
		
		if(noofDeleted > 0)
		{
			this.spaceLock.notifyAll();			
		}
		this.participantLock.unlock();
	}
	public T get(int participant)
	{
		int returnIndex = participantList.get(participant);
		// Establish Conditions for Circular buffer Get
		// The head should be greater than 
		int tempIndex = Math.abs(returnIndex+1 - this.head);
		if(tempIndex>0 && tempIndex <this.size && tempIndex < this.bufferList.size())
		{		
			if(returnIndex+1 >= this.size)
			{
				this.participantList.set(participant, 0);	
			}
			else
				this.participantList.set(participant, returnIndex+1);
		}
		else
		{
			try
			{
				synchronized(this.fullLock)
				{
					this.fullLock.wait();
					if(returnIndex+1 >= this.size)
					{
						this.participantList.set(participant, 0);	
					}
					else
						this.participantList.set(participant, returnIndex+1);
				}
				
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
			
		}		
		System.out.println("Return Index Value " + returnIndex);
		return bufferList.get(returnIndex);
	}
}
