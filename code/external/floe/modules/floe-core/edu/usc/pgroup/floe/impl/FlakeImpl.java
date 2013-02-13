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
 * A Flake is the computation unit that can process a message/task. 
 * The task that it performs depends on the type of Pellet.
 * A Flake is part of a Container, and the resources available to a Flake are determined by the Container.
 * 
 * @author Sreedhar Natarajan (sreedhan@usc.edu)
 * @author Yogesh Simmhan (simmhan@usc.edu)
 * @version v0.1, 2012-01-11
 *
 */

import java.io.File;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.usc.pgroup.floe.api.communication.ConnectionInfo;
import edu.usc.pgroup.floe.api.communication.Message;
import edu.usc.pgroup.floe.api.communication.SinkChannel;
import edu.usc.pgroup.floe.api.communication.SourceChannel;
import edu.usc.pgroup.floe.api.framework.Flake;
import edu.usc.pgroup.floe.api.framework.FlakeInfo;
import edu.usc.pgroup.floe.api.framework.FloeGraph.Edge;
import edu.usc.pgroup.floe.api.framework.PelletTask;
import edu.usc.pgroup.floe.api.framework.PelletTask.PelletTaskStatus;
import edu.usc.pgroup.floe.api.framework.Port;
import edu.usc.pgroup.floe.api.framework.ResourceInfo;
import edu.usc.pgroup.floe.api.framework.pelletmodels.MapperPellet;
import edu.usc.pgroup.floe.api.framework.pelletmodels.Pellet;
import edu.usc.pgroup.floe.api.framework.pelletmodels.ReducerPellet;
import edu.usc.pgroup.floe.api.framework.pelletmodels.SingleInStreamTupleOutPellet;
import edu.usc.pgroup.floe.api.framework.pelletmodels.StreamInStreamOutPellet;
import edu.usc.pgroup.floe.api.framework.pelletmodels.StreamTupleInStreamTupleOutPellet;
import edu.usc.pgroup.floe.api.framework.pelletmodels.TupleInTupleOutPellet;
import edu.usc.pgroup.floe.api.state.FState;
import edu.usc.pgroup.floe.api.state.FStateUpdator;
import edu.usc.pgroup.floe.impl.communication.TCPSinkPushChannel;
import edu.usc.pgroup.floe.impl.communication.TCPSourcePushChannel;
import edu.usc.pgroup.floe.impl.events.MessageEvent;
import edu.usc.pgroup.floe.impl.events.MessageEvent.MessageEventType;
import edu.usc.pgroup.floe.impl.events.MessageEventListener;
import edu.usc.pgroup.floe.impl.pelletHandlers.MapperPelletHandler;
import edu.usc.pgroup.floe.impl.pelletHandlers.PelletHandler;
import edu.usc.pgroup.floe.impl.pelletHandlers.ReducerPelletHandler;
import edu.usc.pgroup.floe.impl.pelletHandlers.SingleInSingleOutPelletHandler;
import edu.usc.pgroup.floe.impl.pelletHandlers.SingleInStreamTupleOutPelletHandler;
import edu.usc.pgroup.floe.impl.pelletHandlers.StreamInStreamOutPelletHandler;
import edu.usc.pgroup.floe.impl.pelletHandlers.StreamTupleInStreamTupleOutPelletHandler;
import edu.usc.pgroup.floe.impl.pelletHandlers.TupleInTupleOutPelletHandler;
import edu.usc.pgroup.floe.impl.queues.SinkQueue;
import edu.usc.pgroup.floe.impl.queues.SourceQueue;

public class FlakeImpl<T> implements Flake, Runnable, MessageEventListener {
	private static Logger logger = Logger.getLogger(FlakeImpl.class.getName());

	public static enum FlakeStatus {
		CREATED, INITIALIZED, PAUSE_INITIALIZED, PAUSED, RUNNING, STOP_INITIALIZED, CORE_CHANGE_INITIALIZED, STOPPED;
	}

	// Each Flake could have a Single Input Buffer and Single Output Buffer
	// Each Flake to have two ports up and running. One for incoming requests
	// and one for outgoing requests.
	private ForkJoinPool forkJoinPool;
	FlakeInfo flakeInfo;
	// List of Pellet Tasks & The corresponding status
	private List<Port> inputPorts;
	private List<Port> outputPorts;
	HashMap<String, T> mappablePelletMap; // Hash Map for the Mappable Pellet
	ArrayList<String> portNameMap; // ArrayList to keep track of different
									// portName;
	Thread flakeThread;
	boolean startFlag;
	boolean resourceAltered;
	boolean pelletNumberAltered;
	int pelletCount;
	private ResourceInfo alterResourceRequest;
	private final Lock pelletCountLock;
	private final Condition availablePelletSlot;
	private int runningPelletCount;
	private PelletHandler pelletHandler;
	private SourceQueue sourceQueue;
	private SinkQueue sinkQueue;
	private Class pelletClass;
	private String pelletType;
	private FlakeStatus flakeStatus;
	private final List<PelletTask> runningTasks = new ArrayList<PelletTask>();
	private final FState state;

	// class laoder which loads pellet jars
	private URLClassLoader classLoader;

	HashMap<String, Long> msgTagToTimeStampHash;
	double averagePelletProcessingLatency;

	public FlakeImpl() {
		this.flakeStatus = FlakeStatus.CREATED;
		alterResourceRequest = new ResourceInfo();
		pelletCountLock = new ReentrantLock();
		availablePelletSlot = pelletCountLock.newCondition();
		this.runningPelletCount = 0;
		resourceAltered = false;
		pelletNumberAltered = false;
		this.alterResourceRequest = new ResourceInfo(0);
		this.msgTagToTimeStampHash = new HashMap<String, Long>();
		state = new FState();
		this.averagePelletProcessingLatency = -1;

	}

	@Override
	public FlakeInfo getFlakeInfo() {
		// update buffer lengths..
		this.flakeInfo.setInBufferSize(sourceQueue.getSize());
		this.flakeInfo.setOutBufferSize(sinkQueue.getSize());
		// this.flakeInfo.setPelletCount(this.pelletCount);

		return this.flakeInfo;
	}

	public String getHostAddress() {
		// Problem with getting actual IP address visible to outside world
		// Simple InetAddress.getHostAddress would give the IP in WIndows but
		// not in Ubuntu
		// It gives 127.0.0.1 in Ubuntu.
		// A Hackish Attempt works in all cases
		Socket socket;
		try {
			socket = new Socket("google.com", 80);
			InetAddress addr = socket.getLocalAddress();
			String hostAddr = addr.getHostAddress();
			socket.close();
			return hostAddr.trim();
		} catch (Exception e) {
			e.printStackTrace();
			return "Error";
		}
	}

	public void createInputConnections(String inpFlakeID, List<Edge> inpChannel, List<SourceChannel> tempSourceChannel,
			List<SinkChannel> tempSinkChannel) {
		// Start the Source TCP Channel
		// Start only those channels in the inpList of type push

		// Identify whether there is a Map Based Communication or Single Message
		// Based Communication.
		// Port Based or Single Input Based on the Input Port Tags in the Node
		// Config File

		try {
			System.out.println("Source Channel Establishment for " + inpFlakeID);
			System.out.println("\tChannels at Port ");
			System.out.print("\t");
			for (Edge tempEdge : inpChannel) {
				if (tempEdge.getedgePort() != null) {
					// Port Based Communication
					if (tempEdge.getchannelBehaviourType().contains("Push")) {
						// If port found use the Mappable Input Port else use
						// the normal Single Input Buffer
						ServerSocket inServer = new ServerSocket(0);
						SourceChannel inpTCPChannel = new TCPSourcePushChannel(sourceQueue, tempEdge.getedgePort().getport());
						sourceQueue.addSourceChannel(inpTCPChannel);
						tempSourceChannel.add(inpTCPChannel);
						inpTCPChannel.openConnection();
						System.out.print("\t" + inpTCPChannel.getConnectionInfo().getInPortNo());
						// Swap the Edge Information and Return back the
						// Connection Information
						// So the Other Side can establish a connection
						ConnectionInfo tempConnectionInfo = inpTCPChannel.getConnectionInfo();
						Edge tempUpdateEdge = tempEdge;
						String tempSource = tempUpdateEdge.getsourceNodeId();
						tempUpdateEdge.setsourceNodeId(tempUpdateEdge.getsinkNodeId());
						tempUpdateEdge.setsinkNodeId(tempSource);
						tempConnectionInfo.setEdge(tempUpdateEdge);
						// Update Port Name in the Connection List. It ensures
						// that in the receiving end the message goes to the
						// right
						// Channel
						tempConnectionInfo.setPortName(tempEdge.getedgePort().getport());
						System.out.println("\n\tThe Connection Established for Flake " + inpFlakeID + " & For Port "
								+ tempEdge.getedgePort().getport());
						this.flakeInfo.addSourceConnection(tempConnectionInfo);
						inServer.close();
					}
				} else {
					// Single Buffer Based Communication
					if (tempEdge.getchannelBehaviourType().contains("Push")) {
						ServerSocket inServer = new ServerSocket(0);
						SourceChannel inpTCPChannel = new TCPSourcePushChannel(this.sourceQueue);
						this.sourceQueue.addSourceChannel(inpTCPChannel);
						tempSourceChannel.add(inpTCPChannel);
						inpTCPChannel.openConnection();
						System.out.print("\t" + inpTCPChannel.getConnectionInfo().getInPortNo());
						// Swap the Edge Information and Return back the
						// Connection Information
						// So the Other Side can establish a connection
						ConnectionInfo tempConnectionInfo = inpTCPChannel.getConnectionInfo();
						Edge tempUpdateEdge = tempEdge;
						String tempSource = tempUpdateEdge.getsourceNodeId();
						tempUpdateEdge.setsourceNodeId(tempUpdateEdge.getsinkNodeId());
						tempUpdateEdge.setsinkNodeId(tempSource);
						tempConnectionInfo.setEdge(tempUpdateEdge);
						this.flakeInfo.addSourceConnection(tempConnectionInfo);
						inServer.close();
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// The Connections are established based on whether Pull Channel can be
	// established
	// in the first phase or not
	public void createOutputConnections(List<Edge> outChannel, List<SinkChannel> tempSinkChannel) {
		// Start the TCP Sink Channel
		// Start only those channels in the outputList of type Pull
		try {
			System.out.print("\n");
			for (Edge tempEdge : outChannel) {
				if (tempEdge.getedgePort() == null) {
					// if(tempEdge.getchannelBehaviourType().contains("Pull"))
					// {
					// ServerSocket inServer = new ServerSocket(0);
					// SinkChannel<T> outTCPChannel = new
					// TCPSinkPullChannel<T>(this.inpBuffer);
					// tempSinkChannel.add(outTCPChannel);
					// //System.out.println("A Push Channel Established at " +
					// inServer.getLocalPort());
					// outTCPChannel.openConnection();
					// this.flakeInfo.addSourceConnection(outTCPChannel.getConnectionInfo());
					// inServer.close();
					// }
				} else {
					// if(tempEdge.getchannelBehaviourType().contains("Pull"))
					// {
					// BlockingQueue<Message<T>> tempQueue = null;
					// if(this.mappableInputBuffer.containsKey(tempEdge.getedgePort().getport()))
					// {
					// tempQueue =
					// this.mappableInputBuffer.get(tempEdge.getedgePort().getport());
					// }
					// else
					// {
					// tempQueue = this.inpBuffer;
					// }
					// ServerSocket inServer = new ServerSocket(0);
					// SinkChannel<T> outTCPChannel = new
					// TCPSinkPullChannel<T>(tempQueue);
					// ConnectionInfo tempConnectionInfo =
					// outTCPChannel.getConnectionInfo();
					// tempConnectionInfo.setPortName(tempEdge.getedgePort().getport());
					// tempSinkChannel.add(outTCPChannel);
					// //System.out.println("A Push Channel Established at " +
					// inServer.getLocalPort());
					// outTCPChannel.openConnection();
					// this.flakeInfo.addSourceConnection(tempConnectionInfo);
					// inServer.close();
					// }
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void setInputPorts(List<Port> inpPorts) {
		this.inputPorts = inpPorts;

	}

	public void setOutputPorts(List<Port> outPorts) {
		this.outputPorts = outPorts;

	}

	@Override
	public String create(String inpFlakeID, String pelletType, ResourceInfo resources, List<Edge> inpChannel, List<Edge> outChannel,
			List<Port> inpPorts, List<Port> outPorts) {
		try {
			String flakeID = "Flake@" + getHostAddress() + "@" + inpFlakeID;

			// Flake Specific Info
			flakeInfo = new FlakeInfo(flakeID, pelletType, resources);
			synchronized (this.flakeStatus) {
				this.flakeInfo.setstatus("Starting");
				this.flakeStatus = FlakeStatus.INITIALIZED;
			}

			List<SourceChannel> tempSourceChannel = new ArrayList<SourceChannel>();
			List<SinkChannel> tempSinkChannel = new ArrayList<SinkChannel>();

			List<Port> tempInpPorts = new ArrayList<Port>();			
			for(Edge e : inpChannel)
			{
				Port tInpPort = e.getedgePort();
				if(tInpPort != null)
					tempInpPorts.add(tInpPort);
			}
			if(tempInpPorts.size()>0)
				inpPorts = tempInpPorts;
			
			
			
			List<Port> tempOutPorts = new ArrayList<Port>();
			
			for(Edge e : outChannel)
			{
				Port tOutPort = e.getedgePort();
				if(tOutPort != null)
					tempOutPorts.add(tOutPort);
			}
			if(tempOutPorts.size()>0)
				outPorts = tempOutPorts;
			
			// Set Up Ports if the InputPorts list is not empty
			// Becomes a Port Based Message Exchange when Map Entry exists
			this.setInputPorts(inpPorts);
			this.inputPorts = inpPorts;

			// Set Up Output Ports If the OutputPorts list is not empty
			// Create Input Source Connections
			this.setOutputPorts(outPorts);
			this.outputPorts = outPorts;

			this.pelletType = pelletType;
			this.pelletClass = loadClass(pelletType);
			this.pelletHandler = createPelletHandler(this.pelletClass);
			this.sourceQueue = this.pelletHandler.createSourceQueue();
			this.sinkQueue = this.pelletHandler.createSinkQueue();

			sinkQueue.addMessageEventListener(this);
			sourceQueue.addMessageEventListener(this);

			this.createInputConnections(inpFlakeID, inpChannel, tempSourceChannel, tempSinkChannel);

			// Create Output Sink Connections
			this.createOutputConnections(outChannel, tempSinkChannel);

			this.flakeInfo.setInputSources(tempSourceChannel);
			this.flakeInfo.setOutputSinks(tempSinkChannel);

			// Create Flake Task. To Make Sure the Thread is not Running
			this.startFlag = false;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return this.flakeInfo.getflakeId();
	}

	private PelletHandler createPelletHandler(Class pelletClass) {
		Class pelletInterface = null;
		for (Class pi : pelletClass.getInterfaces()) {
			if (pi.getName().endsWith("Pellet")) {
				pelletInterface = pi;
				break;
			}
		}
		if (pelletInterface == Pellet.class) {
			return new SingleInSingleOutPelletHandler();
		} else if (pelletInterface == StreamInStreamOutPellet.class) {
			return new StreamInStreamOutPelletHandler();
		} else if (pelletInterface == TupleInTupleOutPellet.class) {
			return new TupleInTupleOutPelletHandler(getInputPortNames(), getOutputPortNames());
		} else if (pelletInterface == MapperPellet.class) {
			return new MapperPelletHandler();
		} else if (pelletInterface == ReducerPellet.class) {
			return new ReducerPelletHandler();
		} else if (pelletInterface == SingleInStreamTupleOutPellet.class) {
			return new SingleInStreamTupleOutPelletHandler(getOutputPortNames());
		} else if (pelletInterface == StreamTupleInStreamTupleOutPellet.class) {
			return new StreamTupleInStreamTupleOutPelletHandler(getInputPortNames(), getOutputPortNames());
		} else {
			throw new UnsupportedOperationException("Unsupported pellet type : " + pelletClass.getName() + " which implements interface  "
					+ pelletInterface.getName());
		}
	}

	@Override
	public void wire(List<ConnectionInfo> inpConnectionList) {
		// Wiring To be established now for the Assosciated Channels
		System.out.println("Sink Channel Establishment for " + this.flakeInfo.getflakeId());
		System.out.println("\tChannels at Port ");
		List<SinkChannel> tempSinkList = this.flakeInfo.getSinks();
		List<SourceChannel> tempSourceList = this.flakeInfo.getSources();

		for (ConnectionInfo tempConnectionInfo : inpConnectionList) {
			tempConnectionInfo.setDestAddress(tempConnectionInfo.getSourceAddress());
			tempConnectionInfo.setOutPort(tempConnectionInfo.getInPortNo());
			/*
			 * if(tempConnectionInfo.getEdge().getchannelBehaviourType().contains
			 * ("Pull")) { // For Wiring Ensure for a Mappable the Buffers are
			 * identified properly // based on the Port Name
			 * if(tempConnectionInfo.getPortName()!=null) {
			 * BlockingQueue<Message<T>> tempQueue = null;
			 * if(this.mappableOutputBuffer
			 * .containsKey(tempConnectionInfo.getPortName())) { tempQueue =
			 * this.mappableOutputBuffer.get(tempConnectionInfo.getPortName());
			 * } // The Buffer does not get assigned if the port name is not
			 * maintained. if(tempQueue!=null) { SourceChannel<T>
			 * tempSourceChannel = new
			 * TCPSourcePullChannel<T>(tempConnectionInfo,tempQueue);
			 * tempSourceChannel.openConnection();
			 * this.flakeInfo.addSinkConnection
			 * (tempSourceChannel.getConnectionInfo());
			 * tempSourceList.add(tempSourceChannel); }
			 * 
			 * } else { SourceChannel<T> tempSourceChannel = new
			 * TCPSourcePullChannel<T>(tempConnectionInfo,outBuffer);
			 * tempSourceChannel.openConnection();
			 * this.flakeInfo.addSinkConnection
			 * (tempSourceChannel.getConnectionInfo());
			 * tempSourceList.add(tempSourceChannel); } }
			 */
			if (tempConnectionInfo.getEdge().getchannelBehaviourType().contains("Push")) {
				BlockingQueue<Message<T>> tempQueue = null;
				if (tempConnectionInfo.getPortName() != null) {
					SinkChannel tempSinkChannel = new TCPSinkPushChannel(tempConnectionInfo);
					sinkQueue.addSinkChannel(tempConnectionInfo.getPortName(), tempSinkChannel);
					tempSinkChannel.openConnection();
					this.flakeInfo.addSinkConnection(tempSinkChannel.getConnectionInfo());
					tempSinkList.add(tempSinkChannel);
					System.out.println("\t\t" + tempSinkChannel.getConnectionInfo().getDestAddress() + "\t"
							+ tempSinkChannel.getConnectionInfo().getOutPortNo() + " Mappable " + tempConnectionInfo.getPortName());
				} else {
					SinkChannel tempSinkChannel = new TCPSinkPushChannel(tempConnectionInfo);
					sinkQueue.addSinkChannel(tempConnectionInfo.getPortName(), tempSinkChannel);
					tempSinkChannel.openConnection();
					this.flakeInfo.addSinkConnection(tempSinkChannel.getConnectionInfo());
					tempSinkList.add(tempSinkChannel);
					System.out.println("\t\t" + tempSinkChannel.getConnectionInfo().getDestAddress() + "\t"
							+ tempSinkChannel.getConnectionInfo().getOutPortNo());
				}
			}
		}
		this.flakeInfo.setInputSources(tempSourceList);
		// set sink list to routers
		// sinkRouter = new SinkRouter();
		this.flakeInfo.setOutputSinks(tempSinkList);
	}

	@Override
	public void startFlake(List<SourceChannel> sources, List<SinkChannel> sinks) {
		if (this.startFlag == false) {
			// Initializing the Thread
			this.flakeThread = new Thread(this);
			// processMessage has to be executed in a seperate thread
			this.startFlag = true;
			this.flakeStatus = FlakeStatus.RUNNING;
			this.flakeInfo.setstatus("Processing");
			this.flakeThread.start();
		}
	}

	public List<String> getOutputPortNames() {
		List<String> outputPts = new ArrayList<String>();
		if (this.outputPorts == null) {
			return outputPts;
		}
		for (Port port : this.outputPorts) {
			outputPts.add(port.getport());
		}
		return outputPts;
	}

	public List<String> getInputPortNames() {
		List<String> inputPts = new ArrayList<String>();
		if (inputPts == null) {
			return inputPts;
		}
		for (Port port : this.inputPorts) {
			inputPts.add(port.getport());
		}
		return inputPts;
	}

	private void clearRunningTasks() {
		logger.info("Clearing running tasks..");
		for (PelletTask task : runningTasks) {
			task.join();
		}
		runningTasks.clear();
	}

	@Override
	public void run() {
		ResourceInfo tempResourceInfo = new ResourceInfo(1);
		this.flakeInfo.setresources(tempResourceInfo);
		logger.info("Setting number of cores to " + this.flakeInfo.getResourceInfo().getNumberOfCores());
		this.forkJoinPool = new ForkJoinPool(this.flakeInfo.getResourceInfo().getNumberOfCores());
		this.flakeInfo.setPelletCount(1);
		this.pelletCount = 1;
		exitThread: while (flakeStatus != FlakeStatus.STOP_INITIALIZED && flakeStatus != FlakeStatus.STOPPED) {
			// block and receive message
			pelletCountLock.lock();
			// sometimes waiting for multiple times is needed
			// as the notifications can be because of alterPelletCount
			// in which case the pelletCount can go up or down depending on the
			// caller.
			synchronized (availablePelletSlot) {
				// flakeStatus is paused or pause initliazed.. keep on waiting
				while (runningPelletCount >= pelletCount || flakeStatus == FlakeStatus.PAUSE_INITIALIZED || flakeStatus == FlakeStatus.PAUSED
						|| flakeStatus == FlakeStatus.STOP_INITIALIZED || flakeStatus == FlakeStatus.CORE_CHANGE_INITIALIZED) {
					// update status if pause complete.. and continue to wait
					if (flakeStatus == FlakeStatus.PAUSE_INITIALIZED && runningPelletCount == 0) {
						clearRunningTasks();
						flakeStatus = FlakeStatus.PAUSED;
					}
					// update status if stop complete.. and exit
					if (flakeStatus == FlakeStatus.STOP_INITIALIZED && runningPelletCount == 0) {
						clearRunningTasks();
						flakeStatus = FlakeStatus.STOPPED;
						break exitThread;
					}
					// core change initialized and running pellets stopped
					// running...
					if (flakeStatus == FlakeStatus.CORE_CHANGE_INITIALIZED && runningPelletCount == 0) {
						logger.info("Core change complete.. Resetting fork join pool..");
						clearRunningTasks();
						resetForkJoinPool();
						flakeStatus = FlakeStatus.RUNNING;
						this.flakeInfo.setstatus("Processing");
						// re-do the checks before going on to wait..
						logger.info("Back after core change completion..");
						continue;
					}
					try {
						logger.info("Running pellet count " + runningPelletCount + " pelletCount: " + pelletCount);
						availablePelletSlot.await();
					} catch (InterruptedException e) {
						logger.log(Level.INFO, "Interrupted exception while waiting for finding available free slot.. Exiting", e);
						break exitThread;
					}
				}
			}
			if (flakeThread.isInterrupted()) {
				logger.log(Level.INFO, "Interrupted exception while waiting for finding available free slot.. Exiting");
				break;
			}
			PelletTask freeSlotTask = null;
			for (PelletTask runningTask : runningTasks) {
				if (runningTask.getPelletStatus() == PelletTaskStatus.COMPLETED) {
					freeSlotTask = runningTask;
				}
			}
			// Make sure the completed task finished completely.
			if (freeSlotTask != null) {
				freeSlotTask.join();
			}
			PelletTask newTask = new PelletTask(pelletHandler, sourceQueue, sinkQueue, state.getCopy(), this, this.pelletClass);
			runningPelletCount++;
			runningTasks.add(newTask);
			this.forkJoinPool.submit(newTask);
			pelletCountLock.unlock();
		}
	}

	public void notifyPelletCompletion(Object pelletInstance) {
		pelletCountLock.lock();
		runningPelletCount--;
		if (pelletInstance instanceof FStateUpdator) {
			FStateUpdator updator = (FStateUpdator) pelletInstance;
			state.updateState(updator);
		}
		availablePelletSlot.signal();
		pelletCountLock.unlock();

	}

	private void resetForkJoinPool() {
		try {
			logger.info("Resetting fork join pool...");
			// make sure the forkJoinPoll shuts down
			logger.info("Shutting down fork join pool..");
			forkJoinPool.shutdown();
			if (forkJoinPool.isTerminated() == false) {
				while (forkJoinPool.awaitTermination(500, TimeUnit.MICROSECONDS) == false)
					;
			}
			logger.info("Old forkJoin pool terminated..");
			logger.info("Creating new forkJoin pool with number of cores " + alterResourceRequest.getNumberOfCores());
			forkJoinPool = new ForkJoinPool(this.alterResourceRequest.getNumberOfCores());
			this.flakeInfo.setresources(alterResourceRequest);
			alterResourceRequest = null;
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, "Exception while resetting forkJoinPool", e);
		}
	}

	@Override
	public void modifyResource(ResourceInfo inpResource) {
		logger.info("Input resouce getting changed by " + inpResource.getNumberOfCores());
		if (inpResource.getNumberOfCores() == 0) {
			// Nothing to do if no change.
			return;
		}
		synchronized (flakeStatus) {
			if (flakeStatus == FlakeStatus.PAUSE_INITIALIZED || flakeStatus == FlakeStatus.STOP_INITIALIZED
					|| flakeStatus == FlakeStatus.CORE_CHANGE_INITIALIZED) {
				throw new UnsupportedOperationException();
			}
		}
		logger.info("Setting alter request");
		synchronized (alterResourceRequest) {
			logger.info("Alter Request " + this.alterResourceRequest + " INp Resource " + inpResource);
			this.alterResourceRequest = new ResourceInfo(inpResource.getNumberOfCores() + flakeInfo.getResourceInfo().getNumberOfCores());
		}
		synchronized (flakeStatus) {
			if (flakeStatus == FlakeStatus.PAUSED || flakeStatus == FlakeStatus.CREATED || flakeStatus == FlakeStatus.INITIALIZED
					|| flakeStatus == FlakeStatus.STOPPED) {
				resetForkJoinPool();
				return;
			}
			logger.info("Initializing core change");
			flakeStatus = FlakeStatus.CORE_CHANGE_INITIALIZED;
			flakeInfo.setstatus("Resource being modified");
		}
		this.stopRunningPellets();
	}

	@Override
	public synchronized void modifyPelletCount(int inpCount) {
		pelletCountLock.lock();
		pelletCount = inpCount;
		availablePelletSlot.signal();
		pelletCountLock.unlock();
	}

	@Override
	public void stopFlake() {
		synchronized (flakeStatus) {
			if (flakeStatus == FlakeStatus.STOPPED) {
				// nothing to do
				return;
			}
			if (flakeStatus == FlakeStatus.PAUSE_INITIALIZED) {
				flakeStatus = FlakeStatus.STOP_INITIALIZED;
				this.flakeInfo.setstatus("StopInitialized");
				return;
			}
			if (flakeStatus == FlakeStatus.PAUSED) {
				flakeThread.interrupt();
			}
			if (flakeStatus == FlakeStatus.CREATED || flakeStatus == FlakeStatus.INITIALIZED) {
				// nothing to do..
				this.flakeInfo.setstatus("Stop");
				flakeStatus = FlakeStatus.STOPPED;
				return;
			}
			flakeStatus = FlakeStatus.STOP_INITIALIZED;
			this.flakeInfo.setstatus("StopInitialized");
		}
		this.stopConnections();
		stopRunningPellets();
	}

	public void stopConnections() {
		Iterator<SourceChannel> sourceIter = this.flakeInfo.getSources().iterator();
		Iterator<SinkChannel> sinkIter = this.flakeInfo.getSinks().iterator();
		while (sourceIter.hasNext()) {
			SourceChannel tempSource = sourceIter.next();
			tempSource.closeConnection();
		}
		while (sinkIter.hasNext()) {
			SinkChannel tempSink = sinkIter.next();
			tempSink.closeConnection();
		}
		// **** To decide whether to clear the Buffer or Not *****
	}

	private void stopRunningPellets() {
		logger.info("Stopping runnig pellets by setting landmarks");
		synchronized (runningTasks) {
			for (PelletTask pelletTask : runningTasks) {
				pelletTask.sendPauseLandmark();
			}
		}
	}

	@Override
	public void pauseFlake() {
		System.out.println("Pause flake for " + flakeInfo.getflakeId() + " called");
		synchronized (flakeStatus) {
			if (flakeStatus == FlakeStatus.STOPPED) {
				throw new UnsupportedOperationException("Cannot pause a stopped flake");
			}
			if (flakeStatus == FlakeStatus.CORE_CHANGE_INITIALIZED) {
				throw new UnsupportedOperationException("Cannot pause a while resource change initialized");
			}
			if (flakeStatus == FlakeStatus.PAUSE_INITIALIZED || flakeStatus == FlakeStatus.PAUSED) {
				// nothing to do..
				return;
			}
			if (flakeStatus == FlakeStatus.CREATED || flakeStatus == FlakeStatus.INITIALIZED) {
				// just updated the status
				flakeStatus = FlakeStatus.PAUSED;
				this.flakeInfo.setstatus("Paused");
				return;
			}

			flakeStatus = FlakeStatus.PAUSE_INITIALIZED;
			this.flakeInfo.setstatus("PauseInitilaized");
		}
		stopRunningPellets();
	}

	@Override
	public void resumeFlake() {
		if (flakeStatus != FlakeStatus.PAUSED) {
			throw new UnsupportedOperationException("Can resume only if paused...");
		}
		// If the current thread is not alive make it running and
		// set the Appropriate Status.
		if (!this.flakeThread.isAlive()) {
			throw new UnsupportedOperationException("Cannot resume a stopped thread.. Use start");
		}
		// reload class
		pelletClass = loadClass(this.pelletType);
		synchronized (flakeStatus) {
			flakeStatus = FlakeStatus.RUNNING;
			flakeInfo.setstatus("Processing");
		}
		pelletCountLock.lock();
		availablePelletSlot.signal();
		pelletCountLock.unlock();
	}

	@Override
	public String getPelletType() {
		return this.flakeInfo.getpelletType();
	}

	@Override
	public String getFlakeId() {
		return this.flakeInfo.getflakeId();
	}

	@Override
	public ResourceInfo getResources() {
		return this.flakeInfo.getResourceInfo();
	}

	@Override
	public List<SourceChannel> getSources() {
		return this.flakeInfo.getSources();
	}

	@Override
	public List<SinkChannel> getSinks() {
		return this.flakeInfo.getSinks();
	}

	private void updateAverageProcessingLatency(String tag, Long inpTimeStamp, Long outTimeStamp) {
		Long latency = (long) -1;
		if (inpTimeStamp != null) {
			latency = outTimeStamp - inpTimeStamp;
		}

		if (averagePelletProcessingLatency == -1) {
			averagePelletProcessingLatency = latency;
		} else {
			// double oldL = averagePelletProcessingLatency;
			averagePelletProcessingLatency = (averagePelletProcessingLatency + latency) / 2.0;
			// System.out.println(this.flakeInfo.getflakeId() + "old: " + oldL +
			// " new:" + averagePelletProcessingLatency);
		}

	}

	private Class loadClass(String className) {
		try {
			System.out.println("Class name : " + className);
			String[] splits = className.split("\\.");
			System.out.println("Splits is " + Arrays.toString(splits));
			String lastName = splits[splits.length - 1];
			File jarFile = new File("pelletJars\\" + lastName + ".jar");
			URL jarFileURL = new URL("file:" + jarFile.getAbsolutePath());
			System.out.println("file: " + jarFileURL);
			classLoader = new URLClassLoader(new URL[] { jarFileURL });
			Class<?> clazz = clazz = classLoader.loadClass(className);
			classLoader.close();
			return clazz;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception at class loading", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public synchronized void handleMessageEvent(MessageEvent e) {

		if (e.getMessageEventType() == MessageEventType.Incoming) {
			flakeInfo.handleIncomingMessage();
		} else {
			flakeInfo.handleOutgoingMessage();
		}
	}
}
