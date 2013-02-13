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

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/***
 * This describes a data flow graph
 * 
 * @author Sreedhar Natarajan (sreedhan@usc.edu)
 * @author Yogesh Simmhan (simmhan@usc.edu)
 * @version v0.1, 2012-01-03
 * 
 */
public class FloeGraph {

	/**
	 * List of nodes in the DAG
	 */
	List<Node> nodes;

	/**
	 * List of edges in the DAG
	 */
	List<Edge> edges;

	/***
	 * This describes a node in the Floe graph by its pellet type and resource
	 * needs
	 * 
	 */
	public FloeGraph() {

	}

	public FloeGraph(List<Node> nodeList, List<Edge> edgeList) {
		this.nodes = nodeList;
		this.edges = edgeList;
	}

	public List<Node> getNodeList() {
		return this.nodes;
	}

	public List<Edge> getEdgeList() {
		return this.edges;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "Nodes")
	@XmlType(propOrder = { "nodeList" })
	public static class NodeList {
		@XmlElement(name = "Node")
		List<Node> nodeList;

		public List<Node> getNodeList() {
			return this.nodeList;
		}

		public void setNodeList(List<Node> inpList) {
			this.nodeList = inpList;
		}
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "Node")
	@XmlType(propOrder = { "nodeId", "pelletType", "Resource", "InputPorts", "OutputPorts", "type", "nodeCount" })
	public static class Node implements Comparable {
		String nodeId;
		String pelletType;
		@XmlElement(name = "Resource")
		ResourceInfo Resource;
		@XmlElementWrapper(name = "InputPorts")
		@XmlElement(name = "Port")
		List<Port> InputPorts;
		@XmlElementWrapper(name = "OutputPorts")
		@XmlElement(name = "Port")
		List<Port> OutputPorts;
		@XmlElement(name = "type")
		String type;
		@XmlElement(name = "nodeCount")
		Integer nodeCount;

		public Node() {

		}

		public Node(String nodeID, String pelletType, ResourceInfo resources) {
			this.nodeId = nodeID;
			this.pelletType = pelletType;
			this.Resource = resources;
		}

		public String getnodeId() {
			return this.nodeId;
		}

		public String getpelletType() {
			return this.pelletType;
		}

		public ResourceInfo getresources() {
			return this.Resource;
		}

		public void setnodeId(String nodeID) {
			this.nodeId = nodeID;
		}

		public void setpelletType(String pelletType) {
			this.pelletType = pelletType;
		}

		public void setresources(ResourceInfo resources) {
			this.Resource = resources;
		}

		@Override
		public int compareTo(Object node2) {
			Integer nodeID1 = Integer.parseInt(this.nodeId);
			Integer nodeID2 = Integer.parseInt(((Node) node2).getnodeId());
			if (nodeID1 > nodeID2)
				return 1;
			else if (nodeID1 < nodeID2)
				return -1;
			else
				return 0;
		}

		public void setInputPorts(List<Port> inpList) {
			this.InputPorts = inpList;
		}

		public List<Port> getInputPorts() {
			return this.InputPorts;
		}

		public void setOutputPorts(List<Port> inpList) {
			this.OutputPorts = inpList;
		}

		public List<Port> getOutputPorts() {
			return this.OutputPorts;
		}

		public String getType() {
			return this.type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public Integer getNodeCount() {
			return nodeCount;
		}

		public void setNodecount(Integer nodeCount) {
			this.nodeCount = nodeCount;
		}

	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "Edges")
	@XmlType(propOrder = { "edgeList" })
	public static class EdgeList {
		@XmlElement(name = "Edge")
		List<Edge> edgeList;

		public List<Edge> getEdgeList() {
			return this.edgeList;
		}

		public void setEdgeList(List<Edge> inpList) {
			this.edgeList = inpList;
		}
	}

	/***
	 * This describes an edge in the Floe graph by its source and sink nodes,
	 * and channel type
	 * 
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "Edge")
	@XmlType(propOrder = { "sourceNodeId", "sinkNodeId", "channelBehaviourType", "channelTransportType", "edgePort" })
	public static class Edge {
		String sourceNodeId;
		String sinkNodeId;
		String channelBehaviourType; // push, pull, sync, async, buffered,
										// unbuffered
		String channelTransportType; // queue, socket
		@XmlElement(name = "EdgePort")
		Port edgePort;

		public Edge() {

		}

		public Edge(String sourceNode, String sinkNode, String channelBehaviour, String channelType) {
			this.sourceNodeId = sourceNode;
			this.sinkNodeId = sinkNode;
			this.channelBehaviourType = channelBehaviour;
			this.channelTransportType = channelType;
		}

		public String getsourceNodeId() {
			return this.sourceNodeId;
		}

		public String getsinkNodeId() {
			return this.sinkNodeId;
		}

		public String getchannelBehaviourType() {
			return this.channelBehaviourType;
		}

		public String getchannelTransportType() {
			return this.channelTransportType;
		}

		public void setsourceNodeId(String sourceNode) {
			this.sourceNodeId = sourceNode;
		}

		public void setsinkNodeId(String sinkNode) {
			this.sinkNodeId = sinkNode;
		}

		public void setchannelBehaviourType(String channelBehaviour) {
			this.channelBehaviourType = channelBehaviour;
		}

		public void setchannelTransportType(String channelType) {
			this.channelTransportType = channelType;
		}

		public void setedgePort(Port inpPort) {
			this.edgePort = inpPort;
		}

		public Port getedgePort() {
			return this.edgePort;
		}
	}
}
