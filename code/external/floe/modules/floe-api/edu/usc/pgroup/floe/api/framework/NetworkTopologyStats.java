package edu.usc.pgroup.floe.api.framework;


import java.util.HashMap;
import java.util.Map;

public class NetworkTopologyStats {

	class NetworkMetric
	{
		public double connectTime;
		public double latency;
		public double throughput;
	}
	
	Map<String,NetworkMetric> connectTimes = new HashMap<String,NetworkMetric>();
	
	public void addNetworkMetrics(String containerID, double connectTime, double latency, double throughput) {
		NetworkMetric metric = new NetworkMetric();
		metric.connectTime = connectTime;
		metric.latency = latency;
		metric.throughput = throughput;
		connectTimes.put(containerID, metric);
	}

}
