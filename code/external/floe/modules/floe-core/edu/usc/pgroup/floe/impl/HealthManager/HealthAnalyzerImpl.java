package edu.usc.pgroup.floe.impl.HealthManager;


import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import edu.usc.pgroup.floe.api.framework.FlakeInfo;
import edu.usc.pgroup.floe.api.framework.HealthAnalyzer;
import edu.usc.pgroup.floe.api.framework.HealthManager;
import edu.usc.pgroup.floe.api.framework.HealthManager.ReactiveActions;
import edu.usc.pgroup.floe.api.framework.HealthManagerEvent;
import edu.usc.pgroup.floe.api.framework.PerfStats;
import edu.usc.pgroup.floe.impl.HealthManager.FlakeState.EFlakeState;
import edu.usc.pgroup.floe.impl.HealthManager.FlakeState.EThroughputTrendPriv;

class FlakeState
{
	private static final int HighBuffThreshold = 100;
	private static final int LowBuffThreshold = 10;
	
	private static final double HighCPUThreshold = 0.95;
	private static final double LowCPUThreshold = 0.50;
	
	public enum EThroughputTrendPriv
	{
		Stable,
		Increasing,
		Decreasing,
		Undeterministic
	}
	
	public enum EThroughputState
	{
		StableWaitingForIncrementPellet,
		IncreasingAfterIncrementPellet,
		StableAfterIncrementPelletWaitingForDecrease,		
		Decreasing,
		StableAfterDecreasing,
		StableAfterDecreasePellet,
		DecreasingAfterDecreasePellet,
		Undeterministic
	}
	
	public enum EFlakeState
	{
		HighBuffHighCPU,
		HighBuffLowCPU,
		HighBuffNormalCPU,
		LowBuffHighCPU,
		LowBuffLowCPU,
		LowBuffNormalCPU,
		LongRunningConstantBuffer,
		Normal
	}
	public EFlakeState CurrentState;
	public int StateLifeTime;
	
	public int SameBufferSizeCount = 0;
	
	public FlakeInfo previousFlakeStats;

	public static EFlakeState ConvertToState(PerfStats perfStats, int i) {
		double totalCPU = perfStats.getCPUStats().getTotalUsage();
		int totalCores = perfStats.getCPUStats().getNoOfCores();
		FlakeInfo flStats = perfStats.getFlakeBufferStats()[i];
		
		int numAllocatedCores = flStats.getResourceInfo().getNumberOfCores();

		//Assuming one flake per container..
		//And also that we cannot find flake to core mapping.. :(
		double effectiveCPU = totalCPU*totalCores/numAllocatedCores;
		
		int inputBufferLength = flStats.getInBufferSize();
		
		EFlakeState state = EFlakeState.Normal;		
		
		if(effectiveCPU > HighCPUThreshold)
		{
			if(inputBufferLength > HighBuffThreshold)
			{
				state = EFlakeState.HighBuffHighCPU;
			}
			else if(inputBufferLength < LowBuffThreshold)
			{
				state = EFlakeState.LowBuffHighCPU;
			}
			else
			{
				state = EFlakeState.Normal; 
			}
		}
		else if(effectiveCPU < LowCPUThreshold)
		{
			if(inputBufferLength > HighBuffThreshold)
			{
				state = EFlakeState.HighBuffLowCPU;
			}
			else if(inputBufferLength < LowBuffThreshold)
			{
				state = EFlakeState.LowBuffLowCPU;
			}
			else
			{
				state = EFlakeState.Normal; 
			}
		}
		else {
			if(inputBufferLength > HighBuffThreshold)
			{
				state = EFlakeState.HighBuffNormalCPU;
			}
			else if(inputBufferLength < LowBuffThreshold)
			{
				state = EFlakeState.LowBuffNormalCPU;
			}
			else
			{
				state = EFlakeState.Normal; 
			}
		}
		
//		System.out.println("Flake:"+flStats.getFlakeId() + " cpu:" + effectiveCPU + " inbuf:" + inputBufferLength + " state:" + state);
		
		return state;
	}

	private List<Double> throughputHistory = new ArrayList<Double>();
	private EThroughputState previousState = EThroughputState.Undeterministic;
	
	public EThroughputTrendPriv getThroughputState(FlakeInfo stats)
	{
		if(throughputHistory.size() < 5)
			return EThroughputTrendPriv.Undeterministic;
		
		EThroughputTrendPriv trend = getCurrentTrend();
		
		throughputHistory.clear();
		
		return trend;
		
	}

	private EThroughputTrendPriv getCurrentTrend() {
		double sumx = 0, sumy = 0;
		int i = 0;
		for(Double throughput : throughputHistory)
		{
			sumx += i;
			sumy += throughput;
			i++;
		}
		
		double xbar = sumx/i;
		double ybar = sumy/i;
		
		double xxbar = 0.0, yybar = 0.0, xybar = 0.0;
		
		i = 0;
		for(Double throughput : throughputHistory){
            xxbar += (i - xbar) * (i - xbar);
            yybar += (throughputHistory.get(i) - ybar) * (throughputHistory.get(i) - ybar);
            xybar += (i - xbar) * (throughputHistory.get(i) - ybar);
        }
		
        double beta1 = xybar / xxbar;
        
        if(beta1 < -0.1)
        {
        	return EThroughputTrendPriv.Decreasing;
        }
        else if(beta1 > 0.1)
        {
        	return EThroughputTrendPriv.Increasing;
        }
        else
        {
        	return EThroughputTrendPriv.Stable;
        }
	}
}

class ContainerState
{
	private ConcurrentHashMap<String, FlakeState> flakeMap;
	private Date latestTimeStamp;
	
	public ContainerState() {
		latestTimeStamp = null;
		flakeMap = new ConcurrentHashMap<String, FlakeState>();
	}

	public void updateStateAndNotify(List<PerfStats> list, HealthAnalyzer healthAnalyzer) {
		for(int i = list.size() - 1; i >= 0 ; i--)
		{
			
			if(latestTimeStamp != null && latestTimeStamp.after(list.get(i).getTimeStamp()))
			{
				//System.out.println(i + "th item is older than what i already saw.. ignoring it");
				break;
			}
			
			//System.out.println("updating state for: " + i + "th item..");
			updateStateAndNotify(list.get(i),healthAnalyzer);
		}
		latestTimeStamp = list.get(list.size()-1).getTimeStamp();
	}

	private void updateStateAndNotify(PerfStats perfStats,HealthAnalyzer healthAnalyzer) {
		// TODO Auto-generated method stub
		FlakeInfo[] flakeStats = perfStats.getFlakeBufferStats();
		if(flakeStats == null) return;	//No flakes allocated yet.. 
		
		for(int i = 0; i < flakeStats.length; i++)
		{
			FlakeState flState = new FlakeState();
			FlakeInfo stats = flakeStats[i];
			
			FlakeState temp = flakeMap.putIfAbsent(stats.getflakeId(), flState);
			if(temp == null) 
				temp = flState;
			else
				flState = temp;
			
			
			EFlakeState efstate = FlakeState.ConvertToState(perfStats,i);
			
			
			
			if(flState.previousFlakeStats == null)
			{
				flState.previousFlakeStats = stats;
				flState.StateLifeTime = 0;
				flState.CurrentState = efstate;
			}			
			else
			{
				if(flState.previousFlakeStats.getInBufferSize() == stats.getInBufferSize() && flState.previousFlakeStats.getInBufferSize() != 0)
					flState.SameBufferSizeCount ++;
				else
					flState.SameBufferSizeCount = 0;
					
				if(flState.SameBufferSizeCount >= 3)
					efstate = EFlakeState.LongRunningConstantBuffer;
				
				if(efstate == flState.CurrentState)
				{
					flState.StateLifeTime++;
				}
				else
				{
					flState.StateLifeTime = 0;
				}
				
				flState.previousFlakeStats = stats;
				flState.CurrentState = efstate;					
			}
			
			//System.out.println( "StateLifeTime:" + flState.StateLifeTime + " SameBurrferSizeCount" + flState.SameBufferSizeCount);
			if(flState.StateLifeTime >= 3 || flState.SameBufferSizeCount >= 3)
			{
				
				HealthManagerEvent healthEve = new HealthManagerEvent(
						healthAnalyzer);
				healthEve.setContainerID(perfStats.getContainerID());
				healthEve.setFlakeID(stats.getflakeId());

				ReactiveActions suggestedAction = ReactiveActions.None;

				if (flState.CurrentState == EFlakeState.HighBuffHighCPU) { //TODO: Add a condition to check if we are going beyond the available cores.. 
					suggestedAction = ReactiveActions.IncreaseCoreAllocation;
				} else if ((flState.CurrentState == EFlakeState.HighBuffLowCPU || flState.CurrentState == EFlakeState.HighBuffNormalCPU)) {
					suggestedAction = ReactiveActions.IncreasePelletCount;
				} else if (flState.CurrentState == EFlakeState.LowBuffLowCPU) {
					if (stats.getPelletCount() > stats
							.getResourceInfo().getNumberOfCores()){
							if(stats.getPelletCount() > 1)
								suggestedAction = ReactiveActions.DecreasePelletCount;
						}
					else
					{
						if(stats.getResourceInfo().getNumberOfCores() > 1)
							suggestedAction = ReactiveActions.DecreaseCoreAllocation;
					}
				}
				else if(flState.CurrentState == EFlakeState.LongRunningConstantBuffer && stats.getPelletCount() < 4 * stats
						.getResourceInfo().getNumberOfCores())
				{
					suggestedAction = ReactiveActions.IncreasePelletCount;
				}
				
				healthEve.setSuggestedAction(suggestedAction);
		
				if(suggestedAction != ReactiveActions.None)
				{
					healthAnalyzer.fireHealthManagerEvent(healthEve);
					flState.StateLifeTime = 0;
				}
			}
		}
		
	}
	
	
	public void updateStateAndNotify2(List<PerfStats> list, HealthAnalyzer healthAnalyzer) {
		for(int i = list.size() - 1; i >= 0 ; i--)
		{
			
			if(latestTimeStamp != null && latestTimeStamp.after(list.get(i).getTimeStamp()))
			{
				//System.out.println(i + "th item is older than what i already saw.. ignoring it");
				break;
			}
			
			//System.out.println("updating state for: " + i + "th item..");
			updateStateAndNotify2(list.get(i),healthAnalyzer);
		}
		latestTimeStamp = list.get(list.size()-1).getTimeStamp();
	}
	
	private void updateStateAndNotify2(PerfStats perfStats,HealthAnalyzer healthAnalyzer) {
		FlakeInfo[] flakeStats = perfStats.getFlakeBufferStats();
		if(flakeStats == null) return;	//No flakes allocated yet.. 
		
		for(int i = 0; i < flakeStats.length; i++)
		{
			FlakeState flState = new FlakeState();
			FlakeInfo stats = flakeStats[i];
			
			FlakeState temp = flakeMap.putIfAbsent(stats.getflakeId(), flState);
			if(temp == null) 
				temp = flState;
			else
				flState = temp;
			
			
			EThroughputTrendPriv currentTrend = temp.getThroughputState(stats);
			switch(currentTrend){
			case Undeterministic:
				stats.setThroughputTrend(Integer.MIN_VALUE);
				break;
			case Decreasing:
				stats.setThroughputTrend(-1);
				break;
			case Increasing:
				stats.setThroughputTrend(-1);
				break;
			case Stable:
				stats.setThroughputTrend(0);
			}
		}
	}
}

public class HealthAnalyzerImpl extends HealthAnalyzer implements Runnable {

	
	private ConcurrentHashMap<String, List<PerfStats>> perfInfoAllRef;
	
	private ConcurrentHashMap<String, ContainerState> containerStateMap;
	
	//List<PerfStats> is the recent history of stats for the given container..  
	public HealthAnalyzerImpl(ConcurrentHashMap<String, List<PerfStats>> aPerfInfoAll) {
		perfInfoAllRef = aPerfInfoAll;		
		containerStateMap = new ConcurrentHashMap<String, ContainerState>();
	}
	
	
	@Override	
	public void run()
	{
		
		
		while(true)
		{
			
			Enumeration<String> perfInfoEnum = perfInfoAllRef.keys();
			//System.out.println("Analyzing data");
			
			while(perfInfoEnum.hasMoreElements())
			{
				String containerID = perfInfoEnum.nextElement();
				
				ContainerState state = new ContainerState();
				
				
				ContainerState temp = containerStateMap.putIfAbsent(containerID, state);
				if(temp == null) temp = state;
				
				List<PerfStats> containerStats = perfInfoAllRef.get(containerID);
				//System.out.println("Analyzing Container:" + containerID);
				
				
				//temp.updateStateAndNotify(containerStats, this);
				temp.updateStateAndNotify2(containerStats, this);
			}
						
			try {
				Thread.sleep(1000 * 5);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
