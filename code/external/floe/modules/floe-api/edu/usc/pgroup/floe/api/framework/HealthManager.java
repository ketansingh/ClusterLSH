package edu.usc.pgroup.floe.api.framework;

import java.util.List;

import edu.usc.pgroup.floe.api.framework.FloeGraph.Edge;

public interface HealthManager {
	
	public enum ReactiveActions
	{
		IncreasePelletCount,
		IncreaseCoreAllocation,
		DecreasePelletCount,
		DecreaseCoreAllocation,
		ReallocateFlake,		
		None
	}
	
	public void ReportStatus(PerfStats status);
	
	public PerfStats getLatestContainerPerfInfo(String containerID);
	
	public List<PerfStats> getLatestGlobalPerfInfo();
}
