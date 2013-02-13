/*
 * Copyright 2011, University of Southern California. All Rights Reserved.
 * 
 * This software is experimental in nature and is provided on an AS-IS basis only. 
 * The University SPECIFICALLY DISCLAIMS ALL WARRANTIES, EXPRESS AND IMPLIED, INCLUDING WITHOUT 
 * LIMITATION ANY WARRANTY AS TO MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.
 * 
 * This software may be reproduced and used for non-commercial purposes only, 
 * so long as this copyright notice is reproduced with each such copy made.
 */

package edu.usc.pgroup.floe.api.framework;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import edu.usc.pgroup.floe.api.communication.ConnectionInfo;
import edu.usc.pgroup.floe.api.communication.SinkChannel;
import edu.usc.pgroup.floe.api.communication.SourceChannel;
import edu.usc.pgroup.floe.impl.HealthManager.PeriodicThrouputCalculator;

/***
 * This described the resources for a Flake within a container.
 * 
 * @author Sreedhar Natarajan (sreedhan@usc.edu)
 * @author Yogesh Simmhan (simmhan@usc.edu)
 * @version v0.1, 2012-01-03
 * 
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "FlakeInfo")
@XmlType(propOrder = { "flakeId", "pelletType", "pelletCount", "inBufferSize", "outBufferSize", "resources", "status", "sourceConnections",
		"sinkConnections", "averagePelletProcessingLatency", "outputThroughput", "throughputTrend" })
public class FlakeInfo {
	@XmlElement(name = "flakeId")
	String flakeId;
	@XmlElement(name = "pelletType")
	String pelletType;
	@XmlElement(name = "Resource")
	ResourceInfo resources;
	@XmlElement(name = "status")
	String status;
	@XmlElement(name = "pelletCount")
	int pelletCount;
	@XmlElement(name = "inBufferSize")
	int inBufferSize;
	@XmlElement(name = "outBufferSize")
	int outBufferSize;
	@XmlElement(name = "averageProcessingLatency")
	double averagePelletProcessingLatency;
	@XmlElement(name = "sourceConnections")
	List<ConnectionInfo> sourceConnections;
	@XmlElement(name = "sinkConnections")
	List<ConnectionInfo> sinkConnections;
	@XmlTransient
	List<SourceChannel> sources;
	@XmlTransient
	List<SinkChannel> sinks;

	@XmlTransient
	long incomingMessageCount;
	@XmlTransient
	long outgoingMessageCount;

	@XmlTransient
	double transientOutputThrouput;

	@XmlElement(name = "outputThroughput")
	// It is a weighted average with more weightage given to the current
	// throughput value.
	double outputThroughput;

	@XmlElement(name = "throughputTrend")
	private int throughputTrend;

	@XmlTransient
	long lastMessageTimeStamp = -1;

	@XmlTransient
	long currentMessageTimeStamp = -1;

	@XmlTransient
	long transientTimeStamp = -1;

	@XmlTransient
	PeriodicThrouputCalculator ptc;

	public FlakeInfo() {
		ptc = new PeriodicThrouputCalculator(this);
		// ptc.start();
	}

	public FlakeInfo(String flakeId, String pelletType, ResourceInfo resources) {
		this.flakeId = flakeId;
		this.pelletType = pelletType;
		this.resources = resources;
		this.sourceConnections = new ArrayList<ConnectionInfo>();
		this.sinkConnections = new ArrayList<ConnectionInfo>();
		this.outputThroughput = -1;
		this.transientOutputThrouput = -1;
	}

	public int getThroughputTrend() {
		return throughputTrend;
	}

	public void setThroughputTrend(int t) {
		throughputTrend = t;
	}

	public double getOutputThroughput() {
		return this.outputThroughput;
	}

	public void setOutputThroughput(double througput) {
		this.outputThroughput = througput;
	}

	public String getflakeId() {
		return this.flakeId;
	}

	public void setflakeId(String flakeID) {
		this.flakeId = flakeID;
	}

	public String getpelletType() {
		return this.pelletType;
	}

	public void setpelletType(String pelletType) {
		this.pelletType = pelletType;
	}

	public ResourceInfo getResourceInfo() {
		return this.resources;
	}

	public String getstatus() {
		return this.status;
	}

	public void setPelletCount(int inpCount) {
		this.pelletCount = inpCount;
	}

	public int getPelletCount() {
		return this.pelletCount;
	}

	public List<SourceChannel> getSources() {
		return this.sources;
	}

	public List<SinkChannel> getSinks() {
		return this.sinks;
	}

	public void setresources(ResourceInfo resource) {
		this.resources = resource;
	}

	public void setstatus(String inpStatus) {
		this.status = inpStatus;
	}

	public void setInputSources(List<SourceChannel> inpSource) {
		this.sources = inpSource;
	}

	public void setOutputSinks(List<SinkChannel> outSink) {
		this.sinks = outSink;
	}

	public int getInBufferSize() {
		return this.inBufferSize;
	}

	public void setInBufferSize(int bufSize) {
		this.inBufferSize = bufSize;
	}

	public int getOutBufferSize() {
		return this.outBufferSize;
	}

	public void setOutBufferSize(int bufSize) {
		this.outBufferSize = bufSize;
	}

	public List<ConnectionInfo> getsourceConnections() {
		return this.sourceConnections;
	}

	public List<ConnectionInfo> getsinkConnections() {
		return this.sinkConnections;
	}

	public void setsourceConnections(List<ConnectionInfo> inpConnectionList) {
		this.sourceConnections = inpConnectionList;
	}

	public void setsinkConnections(List<ConnectionInfo> inpConnectionList) {
		this.sinkConnections = inpConnectionList;
	}

	public void addSourceConnection(ConnectionInfo inpConnection) {
		if (inpConnection != null)
			this.sourceConnections.add(inpConnection);
	}

	public void addSinkConnection(ConnectionInfo inpConnection) {
		if (inpConnection != null)
			this.sinkConnections.add(inpConnection);
	}

	public void setAveragePelletProcessingLatency(double averagePelletProcessingLatency) {
		this.averagePelletProcessingLatency = averagePelletProcessingLatency;
	}

	public double getAveragePelletProcessingLatency() {
		return averagePelletProcessingLatency;
	}

	public synchronized void handleIncomingMessage() {
		lastMessageTimeStamp = System.currentTimeMillis();
	}

	public synchronized void handleOutgoingMessage() {
		// caluclate weighted average..
		currentMessageTimeStamp = System.currentTimeMillis();

		if (lastMessageTimeStamp == -1) {
			lastMessageTimeStamp = currentMessageTimeStamp;
			return;
		} else {
			if (currentMessageTimeStamp - lastMessageTimeStamp > 0) {
				double currentThroughput = 1 / ((double) (currentMessageTimeStamp - lastMessageTimeStamp) / 1000);
				if (outputThroughput == -1) {
					outputThroughput = currentThroughput;
				} else {
					outputThroughput = (outputThroughput + currentThroughput) / 2;
				}
				transientOutputThrouput = outputThroughput;
			}
		}

	}

	public synchronized void AdjustThroughput(long halfLifeInMilliSeconds) {

		currentMessageTimeStamp = System.currentTimeMillis();

		if (outputThroughput != -1 && lastMessageTimeStamp != -1 && transientOutputThrouput != -1) {
			outputThroughput = (transientOutputThrouput) * (currentMessageTimeStamp - lastMessageTimeStamp) / (2 * halfLifeInMilliSeconds / 1000);
		}
	}

}
