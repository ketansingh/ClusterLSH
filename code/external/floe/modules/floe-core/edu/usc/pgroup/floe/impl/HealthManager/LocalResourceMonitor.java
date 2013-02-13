package edu.usc.pgroup.floe.impl.HealthManager;

import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.Console;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.cmd.SigarCommandBase;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;

import edu.usc.pgroup.floe.api.framework.CPUStats;
import edu.usc.pgroup.floe.api.framework.MemStats;
import edu.usc.pgroup.floe.api.framework.PerfStats;
import edu.usc.pgroup.floe.api.framework.NetworkStats;

import edu.usc.pgroup.floe.api.framework.Flake;
import edu.usc.pgroup.floe.api.framework.FlakeInfo;

public class LocalResourceMonitor {

	public static final int TotalCPU = 1;
	public static final int TotalMemory = 1 << 1;
	public static final int FlakesBuffer = 1 << 2;
	public static final int NetworkStats = 1 << 3;
	public static final int ALL = TotalCPU | TotalMemory | FlakesBuffer | NetworkStats; 
			
	private static Thread monitorThread;
	private static MonitorTask monitorTask;
	private static String globalMonitorLocation;
	private static MonitorTask getMonitorTaskInstance()
	{
		if(monitorTask == null)
			monitorTask = new MonitorTask();
		
		return monitorTask;
	}
	
	public static void setStatInterval(long aStatInterval) {
		getMonitorTaskInstance().setStatInterval(aStatInterval);
	}

	public static void setMonitorFlags(int flags) {
		getMonitorTaskInstance().setMonitorFlags(flags);
	}
	
	public static void setFlakeList(List<Flake> flst)
	{
		getMonitorTaskInstance().setFlakeList(flst);
	}
	
	public static void setGlobalMonitorLocation(String gmlocation)
	{
		getMonitorTaskInstance().setGlobalMonitorLocation(gmlocation);
	}
	
	public static void StartMonitor(String containerID) {
		
		System.out.println("test");
		//return;
		
		getMonitorTaskInstance().setContainerID(containerID);

		if (monitorThread == null || monitorThread.isAlive() == false) {
			monitorThread = new Thread(monitorTask);
			monitorThread.start();
		}
		
	}
}

class MonitorTask implements Runnable {

	private Object locker = new Object();
	
	//default parameter values..
	private long statInterval = 1000 * 5;
	private int monitorFlags = LocalResourceMonitor.ALL;
	
	private Sigar sigar;
	private List<Flake> flakeList = null;
	private String containerID;
	private String globalMonitorLocation;
	public void setContainerID(String aContainerID) {
		synchronized (locker) {
			containerID = aContainerID;
		}
	}
	
	public void setGlobalMonitorLocation(String gmlocation) {
		// TODO Auto-generated method stub
		synchronized (locker) {
			globalMonitorLocation = gmlocation;
		}
	}

	public void setStatInterval(long aStatInterval) {
		synchronized (locker) {
			statInterval = aStatInterval;
		}
	}

	public void setFlakeList(List<Flake> flst)
	{
		synchronized (locker) {
			flakeList = flst;
		}
	}
	
	public void setMonitorFlags(int flags) {
		synchronized (locker) {
			monitorFlags = flags;
		}
	}

	public MonitorTask() {
		sigar = new Sigar();
	}

	public void run() {
		long l_statIntercal;
		String l_globalMonitorLocation;
		
		synchronized (locker) {
			l_statIntercal = statInterval;
			l_globalMonitorLocation = globalMonitorLocation;
		}	
		while (true) {
			try {

				
				int l_flags;

				
				NetworkStats ns = null;
				FlakeInfo[] fbs = null;
				MemStats ms = null;
				CPUStats cs = null;
				
				synchronized (locker) {
					l_statIntercal = statInterval;
					l_flags = monitorFlags;
					
				}

				if ((l_flags & LocalResourceMonitor.TotalCPU) != 0) {
					cs = getCPUStats();
				}

				if ((l_flags & LocalResourceMonitor.TotalMemory) != 0) {
					ms = getMemStats();
				}

				if ((l_flags & LocalResourceMonitor.FlakesBuffer) != 0) {
					fbs = FlakesStats();
				}

				if ((l_flags & LocalResourceMonitor.NetworkStats) != 0) {
					ns = getNetworkStats();
				}

				PerfStats pfs = new PerfStats();
				pfs.setContainerID(containerID);
				pfs.setCPUStats(cs);
				pfs.setFlakeBufferStats(fbs);
				pfs.setMemStats(ms);
				pfs.setNetworkStats(ns);
				pfs.setTimeStamp(new Date());
				
				Client client = Client.create();
				String url = "http://"+l_globalMonitorLocation+":45003/GlobalResourceMonitor/reportStatus";
				System.out.println(url);
				WebResource webRes = client.resource(url);
				//System.out.println(url);
				client.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);
				ClientResponse response = webRes.post(ClientResponse.class, pfs);
				//System.out.println(response);
				
				
				/*XMLEncoder encoder =
				           new XMLEncoder(
				              System.out
				                );
								
				encoder.writeObject(pfs);
				encoder.flush();*/
				
				
			} catch (Exception iex) {
				//System.out.println("Exception Occured******************" + iex.getMessage() + iex.getCause());
				iex.printStackTrace();
			}
			finally
			{
				
				try {
					Thread.sleep(l_statIntercal);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private NetworkStats getNetworkStats() throws SigarException {
		NetworkStats nstats = new NetworkStats();
		
		org.hyperic.sigar.NetStat netStat = this.sigar.getNetStat();
		
		nstats.setTotalIncoming(netStat.getAllInboundTotal());
		nstats.setTotalOutgoing(netStat.getAllOutboundTotal());
		return nstats;
	}

	private FlakeInfo[] FlakesStats() {
		
		// TODO Auto-generated method stub
		if(flakeList == null) return null;
		
		FlakeInfo[] flstats = new FlakeInfo[flakeList.size()];
		
		Iterator<Flake> flakeIter = this.flakeList.iterator();
		int i = 0;
		
		while(flakeIter.hasNext())
		{
			Flake tempFlake = flakeIter.next();
			flstats[i] = tempFlake.getFlakeInfo();
			i++;
			
//			/*if(tempFlake.getFlakeInfo()!=null)
//			{
//				FlakeStats fbs = new FlakeStats();
//				FlakeInfo finfo = tempFlake.getFlakeInfo();
//				
//				fbs.setFlakeId(finfo.getflakeId());
//				fbs.setInputBufferLen(finfo.getInBufferSize());
//				fbs.setOutputtBufferLen(finfo.getOutBufferSize());
//				fbs.setNumberOfAllocatedCores(finfo.getResourceInfo().getNumberOfCores());
//				fbs.setNumberOfPelletInstances(finfo.getPelletCount());
//				
//				flstats[i] = fbs;
//				i++;
//			}*/
		}

		return flstats;
	}

	private MemStats getMemStats() throws SigarException {
		MemStats memStats = new MemStats();
		org.hyperic.sigar.Mem memInfo = this.sigar.getMem();
		
		memStats.setTotalMem(memInfo.getTotal());
		memStats.setFreeMem(memInfo.getFree());
		memStats.setUsedMem(memInfo.getUsed());
		
		return memStats;
	}

	private CPUStats getCPUStats() throws SigarException{

		CPUStats cpuStats = new CPUStats();
		
		org.hyperic.sigar.CpuInfo[] infos = this.sigar.getCpuInfoList();

		CpuPerc[] cpus = this.sigar.getCpuPercList();

		org.hyperic.sigar.CpuInfo info = infos[0];

		long cacheSize = info.getCacheSize();
		String vendor = info.getVendor();
		String model = info.getModel();
		int speed =  info.getMhz();
		int totalCores = info.getTotalCores();
		int totalSockets = info.getTotalSockets();		
		
		cpuStats.setNoOfCores(totalCores);
		cpuStats.setNoOfCPUs(totalSockets);
		cpuStats.setSpeed(speed);
		
		if (cacheSize == Sigar.FIELD_NOTIMPL) cacheSize = -1;
		
		double[] coreUsage = new double[cpus.length];
		 
		for (int i=0; i<cpus.length; i++) 
		{
			coreUsage[i] = cpus[i].getCombined();
		}
		
		cpuStats.setPerCoreCombinedUsage(coreUsage);
		cpuStats.setTotalUsage(this.sigar.getCpuPerc().getCombined());
		
		
		return cpuStats;
	}
}




