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

/***
 * This keeps track of the Information about the Floe Graph, Node,Edges and the Wiring Map Information
 * 
 * @author Sreedhar Natarajan (sreedhan@usc.edu)
 * @author Yogesh Simmhan (simmhan@usc.edu)
 * @version v0.1, 2012-01-03
 *
 */
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import edu.usc.pgroup.floe.api.communication.ConnectionInfo;
import edu.usc.pgroup.floe.api.framework.FloeGraph.*;

public class FloeInfo 
{
	String floeID;
	// Edge Associations
	TreeMap<String,List<Edge>> edgeNodeMap;
	// Keep Track of the NodeID and FlakeID Information
	TreeMap<String,String> nodeFlakeMap;
	// DAG Ordering of the Floe
	List<Node> processNodeList;
	// ConnectionInfo for the Flakes for Wiring Information
	TreeMap<String,List<ConnectionInfo>> wiringMap;
	public FloeInfo()
	{		
		this.edgeNodeMap = new TreeMap<String,List<Edge>>();		
		this.processNodeList = new ArrayList<Node>();
		nodeFlakeMap = new TreeMap<String,String>();
		wiringMap = new TreeMap<String,List<ConnectionInfo>>();
	}
	public String getFloeID()
	{
		return this.floeID;
	}
	public void setFloeID(String inpFloe)
	{
		this.floeID = inpFloe;
	}
	public TreeMap<String,List<Edge>> getEdgeNodeMap()
	{
		return this.edgeNodeMap;
	}
	public TreeMap<String,String> getNodeFlakeMap()
	{
		return this.nodeFlakeMap;
	}
	public List<Node> getDAGOrdering()
	{
		return this.processNodeList;
	}
	public TreeMap<String,List<ConnectionInfo>> getWiringMap()
	{
		return this.wiringMap;
	}
	public void setEdgeNodeMap(TreeMap<String,List<Edge>> edgeMap)
	{
		this.edgeNodeMap = edgeMap;
	}
	public void setNodeFlakemap(TreeMap<String,String> flakeMap)
	{
		this.nodeFlakeMap = flakeMap;
	}
	public void setDAGOrdering(List<Node> dagOrdering)
	{
		this.processNodeList = dagOrdering;
	}
	public void setWiringMap(TreeMap<String,List<ConnectionInfo>> wireMap)
	{
		this.wiringMap = wireMap;
	}
}
