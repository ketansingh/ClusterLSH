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

package edu.usc.pgroup.floe.impl;

/***
 * This is the Coordinator Implementation  
 * 
 * @author Sreedhar Natarajan (sreedhan@usc.edu)
 * @author Yogesh Simmhan (simmhan@usc.edu)
 * @version v0.1, 2012-01-27
 *
 */

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.JAXBElement;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;

import edu.usc.pgroup.floe.api.communication.ConnectionInfo;
import edu.usc.pgroup.floe.api.framework.ContainerInfo;
import edu.usc.pgroup.floe.api.framework.Coordinator;
import edu.usc.pgroup.floe.api.framework.FlakeInfo;
import edu.usc.pgroup.floe.api.framework.FloeGraph;
import edu.usc.pgroup.floe.api.framework.FloeGraph.Edge;
import edu.usc.pgroup.floe.api.framework.FloeGraph.Node;
import edu.usc.pgroup.floe.api.framework.FloeInfo;
import edu.usc.pgroup.floe.api.framework.ResourceInfo;
import edu.usc.pgroup.floe.api.framework.StartFloeInfo;
import edu.usc.pgroup.floe.api.framework.rest.RestContainerResourceInfo;
import edu.usc.pgroup.floe.api.framework.rest.RestFlakeCreationInfo;
import edu.usc.pgroup.floe.api.framework.rest.RestFlakeWiringInfo;
import edu.usc.pgroup.floe.api.framework.rest.RestResourcePellet;
import edu.usc.pgroup.floe.impl.communication.RestSourceChannel;
import edu.usc.pgroup.floe.impl.communication.TCPSourcePushChannel;
import edu.usc.pgroup.floe.impl.queues.SourceQueue;
import edu.usc.pgroup.floe.impl.queues.StreamSourceQueue;

public class CoordinatorImpl implements Coordinator {
	static int floeNo = 0;
	List<FloeInfo> floeList;
	static CoordinatorImpl coordinator;
	SourceQueue sourceRouter;
	public static RestSourceChannel restChannel;
	TCPSourcePushChannel inpChannel;

	private static URI getBaseURI() {
		return UriBuilder.fromUri("http://localhost/").port(45000).build();
	}

	CoordinatorImpl() {
		if (restChannel == null) {
			restChannel = new RestSourceChannel("edu.usc.pgroup.floe.impl", getBaseURI());
			this.floeList = new ArrayList<FloeInfo>();
		}
		restChannel.openConnection();
	}

	public static synchronized CoordinatorImpl getCoordinator() {
		if (coordinator != null) {
			return coordinator;

		} else {
			coordinator = new CoordinatorImpl();
			return coordinator;
		}
		// Send an Acknowledgment to the Manager
	}

	public void dagEdgeCount(List<Edge> edgeList, TreeMap<String, Integer> inNodeMap, TreeMap<String, List<Edge>> inEdgeMap,
			TreeMap<String, List<Edge>> outEdgeMap) {
		// Keep track of Edge Count for DAG Ordering
		Iterator<Edge> edgeIter = edgeList.iterator();
		while (edgeIter.hasNext()) {
			Edge tempEdge = edgeIter.next();
			// Update Source Node with Node Count 0, if not present
			if (!inNodeMap.containsKey(tempEdge.getsourceNodeId()))
				inNodeMap.put(tempEdge.getsourceNodeId(), 0);
			// Update Sink Node with Node Count 1, if node not present ;
			// increment if present.
			if (!inNodeMap.containsKey(tempEdge.getsinkNodeId()))
				inNodeMap.put(tempEdge.getsinkNodeId(), 1);
			else {
				int tempValue = inNodeMap.remove(tempEdge.getsinkNodeId());
				inNodeMap.put(tempEdge.getsinkNodeId(), tempValue + 1);
			}
			// Update the Incoming and Outcoming Edges for each node
			if (outEdgeMap.containsKey(tempEdge.getsourceNodeId())) {
				List<Edge> tempEdgeList = outEdgeMap.remove(tempEdge.getsourceNodeId());
				tempEdgeList.add(tempEdge);
				outEdgeMap.put(tempEdge.getsourceNodeId(), tempEdgeList);
			} else {
				List<Edge> tempEdgeList = new ArrayList<Edge>();
				tempEdgeList.add(tempEdge);
				outEdgeMap.put(tempEdge.getsourceNodeId(), tempEdgeList);
			}
			if (inEdgeMap.containsKey(tempEdge.getsinkNodeId())) {
				List<Edge> tempEdgeList = inEdgeMap.remove(tempEdge.getsinkNodeId());
				tempEdgeList.add(tempEdge);
				inEdgeMap.put(tempEdge.getsinkNodeId(), tempEdgeList);
			} else {
				List<Edge> tempEdgeList = new ArrayList<Edge>();
				tempEdgeList.add(tempEdge);
				inEdgeMap.put(tempEdge.getsinkNodeId(), tempEdgeList);
			}
		}
	}

	public void dagNodeCount(List<Node> nodeList, List<Edge> edgeList, List<Node> activeNodeList, TreeMap<String, Node> nodeMap,
			TreeMap<String, Integer> inNodeMap) {
		Iterator<Node> nodeIter = nodeList.iterator();
		while (nodeIter.hasNext()) {
			Node tempNode = nodeIter.next();
			nodeMap.put(tempNode.getnodeId(), tempNode);
		}
		Set<Entry<String, Integer>> tempEntrySet = inNodeMap.entrySet();
		Iterator<Entry<String, Integer>> entryIter = tempEntrySet.iterator();
		while (entryIter.hasNext()) {
			Entry<String, Integer> tempEntry = entryIter.next();
			if (tempEntry.getValue() == 0) {
				// Insert the Node with zero count to the activeNodeList
				Iterator<Edge> tempEdgeIter = edgeList.iterator();
				while (tempEdgeIter.hasNext()) {
					Edge tempEdge = tempEdgeIter.next();
					if (tempEdge.getsourceNodeId().matches(tempEntry.getKey())) {
						activeNodeList.add(nodeMap.get(tempEdge.getsourceNodeId()));
						break;
					}
				}
			}
		}
	}

	public void dagSetSourceConnections(TreeMap<String, Node> nodeMap, List<Node> activeNodeList, TreeMap<String, List<Edge>> inEdgeMap,
			String[] inpNodes) {
		Iterator<Node> nodeIter = activeNodeList.iterator();
		int tempCount = 0;
		while (nodeIter.hasNext()) {
			Node tempNode = nodeIter.next();
			nodeMap.put(tempNode.getnodeId(), tempNode);
			List<Edge> tempEdgeList = null;
			Edge tempEdge = new Edge();
			if (inEdgeMap.containsKey(tempNode.getnodeId())) {
				tempEdgeList = inEdgeMap.remove(tempNode.getnodeId());
				if (tempEdgeList == null)
					tempEdgeList = new ArrayList<Edge>();
			} else {
				tempEdgeList = new ArrayList<Edge>();
			}
			tempEdge.setsourceNodeId(tempNode.getnodeId());
			tempEdge.setchannelBehaviourType("Push");
			tempEdge.setsinkNodeId("X");
			tempEdgeList.add(tempEdge);
			inEdgeMap.put(tempNode.getnodeId(), tempEdgeList);
			inpNodes[tempCount] = tempNode.getnodeId();
			tempCount++;
		}
	}

	public void printDagOrdering(TreeMap<String, Node> nodeMap, List<Node> activeNodeList, TreeMap<String, Integer> inNodeMap,
			TreeMap<String, List<Edge>> inEdgeMap, TreeMap<String, List<Edge>> edgeNodeMap, List<Node> processNodeList) {
		// ProcessNodeList will have the DAG ordered sequence
		System.out.println("\n---- DAG Ordering of Nodes  ---- ");
		System.out.print("Start -> ");
		ArrayList<String> visited = new ArrayList<String>();
		while (activeNodeList.size() > 0) {
			// Take a Node from Active List and Add to Process List
			Node currActiveNode = activeNodeList.remove(0);
			processNodeList.add(currActiveNode);
			System.out.print(currActiveNode.getnodeId() + " -> ");
			// Find all the Adjacent Nodes and add it to the Active list
			// if there are no incoming connections to any of the Nodes
			List<Edge> tempEdgeList = edgeNodeMap.get(currActiveNode.getnodeId());
			Iterator<Edge> tempListIter = tempEdgeList.iterator();
			while (tempListIter.hasNext()) {
				Edge tempEdge = tempListIter.next();
				if (inNodeMap.containsKey(tempEdge.getsinkNodeId())) {
					int tempValue = inNodeMap.remove(tempEdge.getsinkNodeId());
					//if (tempValue - 1 == 0) {
					if(tempValue > 0)
					{
						if(!visited.contains(tempEdge.getsinkNodeId()))
						{
							activeNodeList.add(nodeMap.get(tempEdge.getsinkNodeId()));
							visited.add(tempEdge.getsinkNodeId());
						}
					}
					//} else {
					//	inNodeMap.put(tempEdge.getsinkNodeId(), tempValue - 1);
					//}
				}

			}
		}
		System.out.print("End\n");
	}

	public void createFlakes(List<Node> processNodeList, TreeMap<String, String> nodeFlakeMap, TreeMap<String, List<ConnectionInfo>> wiringMap,
			TreeMap<String, List<Edge>> inEdgeMap, TreeMap<String, List<Edge>> outEdgeMap, TreeMap<String, ContainerInfo> tempContainerHash,
			Client client, WebResource webRes, ClientResponse response) {
		System.out.println("\n --- Creating Flakes --- ");
		Iterator<Node> nodeIter = processNodeList.iterator();
		while (nodeIter.hasNext()) {
			Node tempNode = nodeIter.next();
			ContainerInfo tempContainerInfo;
			if (tempContainerHash.containsKey(tempNode.getnodeId())) {
				tempContainerInfo = tempContainerHash.get(tempNode.getnodeId());
				String containerIP = tempContainerInfo.getContainerId().split("@")[1];
				RestFlakeCreationInfo newRestFlake = new RestFlakeCreationInfo();
				newRestFlake.setFlakeID(tempNode.getnodeId());
				newRestFlake.setPellet(tempNode.getpelletType());
				newRestFlake.setResource(tempNode.getresources());
				newRestFlake.setInputPorts(tempNode.getInputPorts());
				newRestFlake.setOutputPorts(tempNode.getOutputPorts());
				if (inEdgeMap.containsKey(tempNode.getnodeId())) {
					newRestFlake.setInChannel(inEdgeMap.get(tempNode.getnodeId()));
				}
				if (outEdgeMap.containsKey(tempNode.getnodeId())) {
					newRestFlake.setOutChannel(outEdgeMap.get(tempNode.getnodeId()));
				}
				webRes = client.resource("http://" + containerIP + ":45002/Container/createFlake");
				client.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);
				response = webRes.post(ClientResponse.class, newRestFlake);
				GenericType<JAXBElement<FlakeInfo>> flakeInfoType = new GenericType<JAXBElement<FlakeInfo>>() {
				};
				FlakeInfo tempFlakeInfo = response.getEntity(flakeInfoType).getValue();
				String tempFlakeID = tempFlakeInfo.getflakeId();
				tempFlakeID = tempFlakeID.split("@")[2]; // Flake ID follows a
															// Convention
															// Flake@IPAddress@NodeID
				nodeFlakeMap.put(tempFlakeID, tempFlakeInfo.getflakeId());
				if (!wiringMap.containsKey(tempFlakeID)) {
					List<ConnectionInfo> tempConnectionList = tempFlakeInfo.getsourceConnections();
					wiringMap.put(tempFlakeID, tempConnectionList);
				}
			}
		}
	}

	public void resourceAllocation(List<String> nodeStrList, List<ResourceInfo> resourceList, TreeMap<String, ContainerInfo> containerHash,
			Client client, WebResource webRes, ClientResponse response) {
		System.out.println("\n --- Allocating Resources --- ");
		// Issue Commands to the Manager to Create/ Retrieve Containers for the
		// Flakes
		RestResourcePellet tempResourcePellet = new RestResourcePellet();
		tempResourcePellet.setPelletList(nodeStrList);
		tempResourcePellet.setResourceList(resourceList);
		webRes = client.resource("http://localhost:45001/Manager/allocateResources");
		client.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);
		response = webRes.post(ClientResponse.class, tempResourcePellet);
		GenericType<JAXBElement<RestContainerResourceInfo>> containerResourceType = new GenericType<JAXBElement<RestContainerResourceInfo>>() {
		};
		RestContainerResourceInfo tempContainerResourceInfo = response.getEntity(containerResourceType).getValue();
		// Convert the List Back to HashMap From JAXBElement.
		// Direct Mapping of Hash Objects to JAXBElement is not trivial

		Iterator<String> nodeStrIter = tempContainerResourceInfo.getnodeList().iterator();
		Iterator<ContainerInfo> containerIter = tempContainerResourceInfo.getcontainerList().iterator();
		while (nodeStrIter.hasNext()) {
			String tempStr = nodeStrIter.next();
			ContainerInfo tempContainerInfo = containerIter.next();
			containerHash.put(tempStr, tempContainerInfo);
		}
	}

	public void wireFlakes(List<Node> processNodeList, TreeMap<String, List<ConnectionInfo>> wiringMap,
			TreeMap<String, List<ConnectionInfo>> tempWiringMap) {
		// The Wiring has to be completed by Connecting the Sink for each of the
		// Flakes
		// Gather all the necessary information about channels to be created
		// first
		// Issue the wire command to container
		System.out.println("\n --- Wiring Flakes --- ");
		Iterator<Node> nodeIter = processNodeList.iterator();
		while (nodeIter.hasNext()) {
			Node tempNode = nodeIter.next();
			if (wiringMap.containsKey(tempNode.getnodeId())) {
				List<ConnectionInfo> connectionList = wiringMap.get(tempNode.getnodeId());
				for (ConnectionInfo tempConnection : connectionList) {
					// Check if there are any entries for the sinkNode value of
					// Edge in the TempWiringMao
					// Sink corresponds to the flakes where corresponding
					// channel has to be established
					// Ignore those Connection Which were created for The
					// Incoming Nodes

					if (!(tempConnection.getEdge().getsourceNodeId().matches("X") || tempConnection.getEdge().getsinkNodeId().matches("X"))) {
						if (tempWiringMap.containsKey(tempConnection.getEdge().getsinkNodeId())) {
							List<ConnectionInfo> tempConnectionList = tempWiringMap.remove(tempConnection.getEdge().getsinkNodeId());
							tempConnectionList.add(tempConnection);
							tempWiringMap.put(tempConnection.getEdge().getsinkNodeId(), tempConnectionList);
						} else {
							List<ConnectionInfo> tempConnectionList = new ArrayList<ConnectionInfo>();
							tempConnectionList.add(tempConnection);
							tempWiringMap.put(tempConnection.getEdge().getsinkNodeId(), tempConnectionList);
						}
					}
				}
			}
		}
	}

	public void setSinkConnections(List<Node> processNodeList, TreeMap<String, List<ConnectionInfo>> tempWiringMap, ConnectionInfo outConnectionInfo) {
		// For the last node in the network add a SinkChannel to push the data
		// out to the
		// specified connection
		// If outConnection is mentioned as null create a new SourceChannel for
		// the Coordinator
		// Assign the Connection Info for the last Node.
		if (outConnectionInfo == null) {
			sourceRouter = new StreamSourceQueue();
			inpChannel = new TCPSourcePushChannel(sourceRouter);
			sourceRouter.addSourceChannel(inpChannel);
			inpChannel.openConnection();
			ConnectionInfo retConnectionInfo = inpChannel.getConnectionInfo();
			Node tempNode = processNodeList.get(processNodeList.size() - 1);
			Edge tempEdge = new Edge();
			tempEdge.setsourceNodeId(tempNode.getnodeId());
			tempEdge.setsinkNodeId("X");
			tempEdge.setchannelBehaviourType("Push");
			retConnectionInfo.setEdge(tempEdge);
			List<ConnectionInfo> connectionList;
			System.out.println("Connecting The Node " + tempNode.getnodeId() + " to the COntainer " + " Process Node List " + processNodeList);
			if (tempWiringMap.containsKey(tempNode.getnodeId()))
				connectionList = tempWiringMap.remove(tempNode.getnodeId());
			else
				connectionList = new ArrayList<ConnectionInfo>();
			connectionList.add(retConnectionInfo);
			tempWiringMap.put(tempNode.getnodeId(), connectionList);
		}
	}

	@Override
	public StartFloeInfo createFloe(FloeGraph graph, ConnectionInfo inpConnectionInfo, ConnectionInfo outConnectionInfo) {
		// For Now Assumption is Manager is Running in the Same Machine as
		// Coordinator. Can be Changed Later

		FloeInfo currFloe = new FloeInfo();
		// Should be performing DAG ordering before actually Allocating any
		// resources.
		List<Node> processNodeList = new ArrayList<Node>();
		List<Node> activeNodeList = new ArrayList<Node>();
		// To Keep track of how many connections a particular node has and the
		// type of edge
		TreeMap<String, List<Edge>> edgeNodeMap = new TreeMap<String, List<Edge>>();
		// For Quick referencing of the Node
		TreeMap<String, Node> nodeMap = new TreeMap<String, Node>();
		// To Keep track of Information about incoming Edges
		TreeMap<String, List<Edge>> inEdgeMap = new TreeMap<String, List<Edge>>();
		// To Keep track of Information about outgoing Edges
		TreeMap<String, List<Edge>> outEdgeMap = new TreeMap<String, List<Edge>>();
		// Find the number of incomming connections for each of the nodes
		TreeMap<String, Integer> inNodeMap = new TreeMap<String, Integer>();
		// Keep Track of the ConnectionInfo for the Flakes for Wiring
		// Information
		TreeMap<String, List<ConnectionInfo>> wiringMap = new TreeMap<String, List<ConnectionInfo>>();
		// Keep Track of the NodeID and FlakeID Information
		TreeMap<String, String> nodeFlakeMap = new TreeMap<String, String>();

		List<Edge> edgeList = graph.getEdgeList();
		List<Node> nodeList = graph.getNodeList();

		String[] inpNodes;
		Iterator<Node> nodeIter = nodeList.iterator();
		this.dagEdgeCount(edgeList, inNodeMap, inEdgeMap, outEdgeMap);

		this.dagNodeCount(nodeList, edgeList, activeNodeList, nodeMap, inNodeMap);

		Collections.sort(nodeList);
		inpNodes = new String[activeNodeList.size()];

		// For each of the Nodes with 0 Input Connections Set up a SourceChannel
		int tempCount = 0;
		this.dagSetSourceConnections(nodeMap, activeNodeList, inEdgeMap, inpNodes);

		// Assumption That Graph does not contain two edges between same source
		// and sink
		Iterator<Edge> edgeIter = edgeList.iterator();
		while (edgeIter.hasNext()) {
			Edge tempEdge = edgeIter.next();

			if (edgeNodeMap.containsKey(tempEdge.getsourceNodeId())) {
				// Add Edge Information for Quick Referencing
				List<Edge> tempEdgeList = edgeNodeMap.remove(tempEdge.getsourceNodeId());
				tempEdgeList.add(tempEdge);
				edgeNodeMap.put(tempEdge.getsourceNodeId(), tempEdgeList);
			} else {
				List<Edge> tempEdgeList = new ArrayList<Edge>();
				tempEdgeList.add(tempEdge);
				edgeNodeMap.put(tempEdge.getsourceNodeId(), tempEdgeList);
			}
			if (!edgeNodeMap.containsKey(tempEdge.getsinkNodeId())) {
				edgeNodeMap.put(tempEdge.getsinkNodeId(), new ArrayList<Edge>());
			}
		}

		// Print DAG Ordering
		this.printDagOrdering(nodeMap, activeNodeList, inNodeMap, inEdgeMap, edgeNodeMap, processNodeList);

		// Coordinator Should talk to Manager to Find out the containers in
		// which the Flakes could be instantiated.
		nodeList = processNodeList;
		nodeIter = nodeList.iterator();
		List<String> nodeStrList = new ArrayList<String>();
		List<ResourceInfo> resourceList = new ArrayList<ResourceInfo>();
		while (nodeIter.hasNext()) {
			Node tempNode = nodeIter.next();
			nodeStrList.add(tempNode.getnodeId());
			resourceList.add(tempNode.getresources());
		}

		// Resource Allocation
		Client client = Client.create();
		WebResource webRes = null;
		ClientResponse response = null;
		TreeMap<String, ContainerInfo> containerHash = new TreeMap<String, ContainerInfo>();
		this.resourceAllocation(nodeStrList, resourceList, containerHash, client, webRes, response);

		// Issue Commands to Container to Create the necessary Flakes with the
		// specified Channel Types
		this.createFlakes(processNodeList, nodeFlakeMap, wiringMap, inEdgeMap, outEdgeMap, containerHash, client, webRes, response);

		// Wire Flakes by Sending Signals to the appropriate Flakes in
		// respective containers
		TreeMap<String, List<ConnectionInfo>> tempWiringMap = new TreeMap<String, List<ConnectionInfo>>();
		
		this.wireFlakes(processNodeList, wiringMap, tempWiringMap);

		// Sink Connections for Container If the Container is the node to
		// receive the data
		this.setSinkConnections(processNodeList, tempWiringMap, outConnectionInfo);

		nodeIter = processNodeList.iterator();
		while (nodeIter.hasNext()) {
			Node tempNode = nodeIter.next();
			if (tempWiringMap.containsKey(tempNode.getnodeId())) {
				String tempFlakeID = nodeFlakeMap.get(tempNode.getnodeId());

				// Check to Ensure that The Node has been allocated as Flake
				if (tempWiringMap.containsKey(tempNode.getnodeId()) && tempFlakeID != null) {
					webRes = client.resource("http://" + tempFlakeID.split("@")[1] + ":45002/Container/wireFlake");
					client.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);
					RestFlakeWiringInfo tempFlakeWiringInfo = new RestFlakeWiringInfo();
					tempFlakeWiringInfo.setConnectionList(tempWiringMap.get(tempNode.getnodeId()));
					tempFlakeWiringInfo.setFlakeID(tempFlakeID);
					response = webRes.put(ClientResponse.class, tempFlakeWiringInfo);
				}
				// GenericType<JAXBElement<FlakeInfo>> flakeInfoType = new
				// GenericType<JAXBElement<FlakeInfo>>() {};
				// FlakeInfo tempFlakeInfo =
				// response.getEntity(flakeInfoType).getValue();
			}
		}
		// Set Floe Specific Information
		floeNo++;
		currFloe.setFloeID("Floe@" + floeNo);
		currFloe.setDAGOrdering(processNodeList);
		currFloe.setEdgeNodeMap(edgeNodeMap);
		currFloe.setNodeFlakemap(nodeFlakeMap);
		currFloe.setWiringMap(wiringMap);

		System.out.println("\n --- Start Point for The Floe Connections ---\n");
		tempCount = 0;
		StartFloeInfo tempFloeInfo = new StartFloeInfo();
		tempFloeInfo.setFloeID(currFloe.getFloeID());
		List<ConnectionInfo> startConnectionList = new ArrayList<ConnectionInfo>();
		while (tempCount < inpNodes.length) {
			List<ConnectionInfo> tempConnectionList = wiringMap.get(inpNodes[tempCount]);
			ConnectionInfo tempConnectionInfo = new ConnectionInfo();
			tempConnectionInfo.setInPort(tempConnectionList.get(0).getInPortNo());
			tempConnectionInfo.setPortName(inpNodes[tempCount]); // Contains the
																	// Node ID
			tempConnectionInfo.setSourceAddress(tempConnectionList.get(0).getSourceAddress());
			startConnectionList.add(tempConnectionInfo);
			System.out.println(" --- Incoming Connections for Node  --- " + inpNodes[tempCount] + " Size of List " + tempConnectionList.size());
			System.out.println("\t Node ID : " + inpNodes[tempCount] + " IP Address " + tempConnectionList.get(0).getSourceAddress() + " Port No"
					+ tempConnectionList.get(0).getInPortNo());
			tempCount++;
		}
		this.floeList.add(currFloe);
		tempFloeInfo.setConnections(startConnectionList);
		return tempFloeInfo;
	}

	@Override
	public void startFloe(String floeId) {
		// Start the Floe with mentioned Floe ID
		FloeInfo currFloeInfo = null;
		for (FloeInfo tempFloeInfo : this.floeList) {
			if (tempFloeInfo.getFloeID().matches(floeId)) {
				currFloeInfo = tempFloeInfo;
				break;
			}
		}
		if (currFloeInfo != null) {
			List<Node> dagOrdering = currFloeInfo.getDAGOrdering();
			TreeMap<String, String> nodeFlakeMap = currFloeInfo.getNodeFlakeMap();
			Iterator<Node> nodeIter = dagOrdering.iterator();
			WebResource webRes;
			Client client = Client.create();
			ClientResponse response;
			while (nodeIter.hasNext()) {
				Node tempNode = nodeIter.next();
				if (nodeFlakeMap.containsKey(tempNode.getnodeId())) {
					String tempFlakeID = nodeFlakeMap.get(tempNode.getnodeId());
					webRes = client.resource("http://" + tempFlakeID.split("@")[1] + ":45002/Container/startFlake/FlakeID=" + tempFlakeID);
					client.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);
					response = webRes.put(ClientResponse.class);
				}
			}
		}
	}

	@Override
	public void stopFloe(String floeId) {
		FloeInfo currFloeInfo = null;
		for (FloeInfo tempFloeInfo : this.floeList) {
			if (tempFloeInfo.getFloeID().matches(floeId)) {
				currFloeInfo = tempFloeInfo;
				break;
			}
		}
		if (currFloeInfo != null) {
			List<Node> dagOrdering = currFloeInfo.getDAGOrdering();
			TreeMap<String, String> nodeFlakeMap = currFloeInfo.getNodeFlakeMap();
			Iterator<Node> nodeIter = dagOrdering.iterator();
			WebResource webRes;
			Client client = Client.create();
			ClientResponse response;
			while (nodeIter.hasNext()) {
				Node tempNode = nodeIter.next();
				if (nodeFlakeMap.containsKey(tempNode.getnodeId())) {
					String tempFlakeID = nodeFlakeMap.get(tempNode.getnodeId());
					webRes = client.resource("http://" + tempFlakeID.split("@")[1] + ":45002/Container/stopFlake/FlakeID=" + tempFlakeID);
					client.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);
					response = webRes.put(ClientResponse.class);
				}
			}
		}
	}

	@Override
	public void pauseFloe(String floeId) {

	}

	@Override
	public void resumeFloe(String floeId) {

	}

	@Override
	public void updateFloe(String floeId, FloeGraph graph) {

	}
}
