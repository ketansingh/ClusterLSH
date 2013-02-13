package edu.usc.pgroup.floe.impl.HealthManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.AccessControlException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.JAXBElement;

import org.apfloat.Apfloat;
import org.apfloat.ApfloatContext;
import org.apfloat.ApfloatRuntimeException;
import org.apfloat.samples.Operation;
import org.apfloat.samples.Pi;
import org.apfloat.samples.PiParallel;
import org.apfloat.spi.FilenameGenerator;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.jmx.SigarMem;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;


import edu.usc.pgroup.floe.api.framework.CPUStats;
import edu.usc.pgroup.floe.api.framework.DiskIOStats;
import edu.usc.pgroup.floe.api.framework.Flake;
import edu.usc.pgroup.floe.api.framework.FlakeInfo;
import edu.usc.pgroup.floe.api.framework.MemStats;
import edu.usc.pgroup.floe.api.framework.MemoryReadWriteStats;
import edu.usc.pgroup.floe.api.framework.NetworkStats;
import edu.usc.pgroup.floe.api.framework.NetworkTopologyStats;
import edu.usc.pgroup.floe.api.framework.PICPUBenchmarkStats;
import edu.usc.pgroup.floe.api.framework.PerfStats;
import edu.usc.pgroup.floe.util.NullOutputStream;

public class PeriodicHealthCheck {

	public static final int TotalCPU = 1;
	public static final int TotalMemory = 1 << 1;
	public static final int FlakesBuffer = 1 << 2;
	public static final int NetworkStats = 1 << 3;
	public static final int ALL = TotalCPU | TotalMemory | FlakesBuffer | NetworkStats; 
			
	private static Thread monitorThread;
	private static PeriodicHealthCheckTask monitorTask;
	private static String globalMonitorLocation;
	
	private static PeriodicHealthCheckTask getPeriodicHealthCheckTaskInstance()
	{
		if(monitorTask == null)
			monitorTask = new PeriodicHealthCheckTask();
		
		return monitorTask;
	}
	
	public static void AddToContainersTopology(String containerID, String ip) {
		// TODO Auto-generated method stub
		getPeriodicHealthCheckTaskInstance().AddToContainersTopology(containerID,ip);
	}
	
	public static void setStatInterval(long aStatInterval) {
		getPeriodicHealthCheckTaskInstance().setStatInterval(aStatInterval);
	}

	public static void setMonitorFlags(int flags) {
		getPeriodicHealthCheckTaskInstance().setMonitorFlags(flags);
	}
	
	public static void setFlakeList(List<Flake> flst)
	{
		getPeriodicHealthCheckTaskInstance().setFlakeList(flst);
	}
	
	public static void setGlobalMonitorLocation(String gmlocation)
	{
		getPeriodicHealthCheckTaskInstance().setGlobalMonitorLocation(gmlocation);
	}
	
	public static void StartMonitor(String containerID) {
		
		System.out.println("test");
		//return;
		
		getPeriodicHealthCheckTaskInstance().setContainerID(containerID);

		if (monitorThread == null || monitorThread.isAlive() == false) {
			monitorThread = new Thread(monitorTask);
			monitorThread.start();
		}
		
	}

	public static void setHostIp(String hostIp) {
		getPeriodicHealthCheckTaskInstance().setHostIp(hostIp);
	}

	
}

class PeriodicHealthCheckTask implements Runnable {

	private Object locker = new Object();
	
	//default parameter values..
	private long statInterval = 1000 * 5;
	private int monitorFlags = LocalResourceMonitor.ALL;
	
	private Sigar sigar;
	private List<Flake> flakeList = null;
	private String containerID;
	private String globalMonitorLocation;
	private String hostIp;
	
	private Map<String,String> containerTopology = new HashMap<String, String>();
	
	TCPListener listener;
	
	public PeriodicHealthCheckTask() {
		sigar = new Sigar();
		listener = new TCPListener();
		listener.startListening();
		
		init();
	}
		
	public void setHostIp(String aHostIp) {
		synchronized (locker) {
			hostIp = aHostIp;
		}
	}

	public void setContainerID(String aContainerID) {
		synchronized (locker) {
			containerID = aContainerID;
		}
	}
	
	public void AddToContainersTopology(String aContainerID, String ip) {
		// TODO Auto-generated method stub
		synchronized (locker) {
			containerTopology.put(aContainerID, ip);
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



	public void run() {
		long l_statIntercal;
		String l_globalMonitorLocation;
		
		synchronized (locker) {
			l_statIntercal = statInterval;
			l_globalMonitorLocation = globalMonitorLocation;
		}
		
		
		//Register container 
		Client rclient = Client.create();
		String rurl = "http://"+l_globalMonitorLocation+":45003/GlobalResourceMonitor/registerContainer";
		WebResource rwebRes = rclient.resource(rurl);
		

		MultivaluedMap queryParams = new MultivaluedMapImpl();
		queryParams.add("ContainerId", containerID);
		queryParams.add("IP", hostIp);
		   
		   String rresponse = rwebRes.queryParams(queryParams).post(String.class);
		   
		// use properties to restore the map
		   Properties props = new Properties();
		   try {
			props.load(new StringReader(rresponse.substring(1, rresponse.length() - 1).replace(", ", "\n")));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}       
		   Map<String, String> map2 = new HashMap<String, String>();
		   for (Map.Entry<Object, Object> e : props.entrySet()) {
		       map2.put((String)e.getKey(), (String)e.getValue());
		   }  
		   
		
		
		
		System.out.println(map2);
		   /*
		rclient.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);
		ClientResponse rresponse = rwebRes.post(ClientResponse.class, "test");*/

		
		while (true) {
			long firstTime = System.currentTimeMillis();
			try {
				
				/*
				int l_flags;

				
				
				NetworkTopologyStats nts = null;
				PICPUBenchmarkStats cpubenckmark = null;
				MemoryReadWriteStats memorybenckmark = null;
				DiskIOStats diskIOStats = null;
				
				synchronized (locker) {
					l_statIntercal = statInterval;
					l_flags = monitorFlags;
					
				}

				nts = getNetworkTopologyStats();
				cpubenckmark = getPICPUBenchmarkStats();
				memorybenckmark = getMemoryReadWriteStats();
				diskIOStats = getDiskIOStats();
				
				
				
				Client client = Client.create();
				String url = "http://"+l_globalMonitorLocation+":45003/GlobalResourceMonitor/reportStatus";
				WebResource webRes = client.resource(url);
				//System.out.println(url);

				client.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);
				ClientResponse response = webRes.post(ClientResponse.class, nts);
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
				long lastTime = System.currentTimeMillis();
				long executionTime = lastTime - firstTime;
				try {
					Thread.sleep(l_statIntercal - executionTime);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private DiskIOStats getDiskIOStats() {
		double elapsedSeconds = -1;
		double readThroughput = -1;
		double writeThroughput = -1;
		DiskIOStats diskstats = new DiskIOStats();
		try {
			
			File f = new File("frameworkdata/temp.data");
			long fileSize = f.length();
			byte buffer[] = new byte[4 * 1024];
			
			{
				long start = System.nanoTime();
			
				FileInputStream fstream = new FileInputStream("frameworkdata/temp.data");
				
				while(fstream.read(buffer) != -1);
				fstream.close();
				
				long end = System.nanoTime();
				long elapsedTime = end - start;
				elapsedSeconds = (double)elapsedTime / 1000000000.0;
				readThroughput = fileSize/elapsedSeconds;
			}
			diskstats.setThroughput(readThroughput,writeThroughput);
			System.out.println("read throughput: "
			        + readThroughput + "MBps");
			{
				Random r = new Random();
				r.nextBytes(buffer);
				long start = System.nanoTime();
				FileOutputStream fostream = new FileOutputStream("frameworkdata/temp2.data");
				for(long i = 0;i < fileSize; i+=buffer.length)
				{
					fostream.write(buffer);
				}
				fostream.close();
				long end = System.nanoTime();
				long elapsedTime = end - start;
				elapsedSeconds = (double)elapsedTime / 1000000000.0;
				writeThroughput = fileSize/elapsedSeconds;
			}	
			
		    
			
			
			System.out.println("write throughput: "
			        + writeThroughput + " MBps");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	private NetworkTopologyStats getNetworkTopologyStats() {
		
		NetworkTopologyStats stats = new NetworkTopologyStats();
				
		for(String containerID : containerTopology.keySet())
		{
			String ip = containerTopology.get(containerID);
			double connectTime = getConnectTime(ip);
			double latency = getLatency(ip);
			double throughput = getThroughput(ip);			
			stats.addNetworkMetrics(containerID, connectTime, latency, throughput);
		}
		return stats;
	}
	
	
	private double getThroughput(String ip) {
		double elapsedSeconds = -1;
		double throughput = -1;
		try {
			//do not get the connect time... just the latency for the data
			Socket clientSocket = new Socket(ip, 9999);
						
			ByteBuffer b = ByteBuffer.allocate(4);			
			b.putInt(0);
			byte[] len = b.array();
			
			byte[] result = new byte[4 * 1024 * 1024 + 4];
			Random r = new Random();
			r.nextBytes(result);
			System.arraycopy(len,0, result, 0, len.length);
			
			{
				long start = System.nanoTime();
				OutputStream outS = clientSocket.getOutputStream();
				outS.write(result);
				outS.flush();
				//TODO: wait for ack.. ?
				outS.close();
				long end = System.nanoTime();
				
				long elapsedTime = end - start;
			    
				elapsedSeconds = (double)elapsedTime / 1000000000.0;
			}
			
			{
				long start = System.nanoTime();
				InputStream inS = clientSocket.getInputStream();
				int total = 0;
				while(total < 4*1024*1024)
				{
					total += inS.read(result);
				}
				long end = System.nanoTime();
				
				long elapsedTime = end - start;
			    
				elapsedSeconds = (elapsedSeconds + (double)elapsedTime / 1000000000.0)/2;
				throughput = 4*1024*1024/elapsedSeconds;
				System.out.println("throughput: "
				        + throughput + " seconds");
			}
			
			clientSocket.close();
			clientSocket = null;	//mark for gc					
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return throughput;
	}

	private double getLatency(String ip) {
		double elapsedSeconds = -1;
		try {
			//do not get the connect time... just the latency for the data
			Socket clientSocket = new Socket(ip, 9999);
						
			ByteBuffer b = ByteBuffer.allocate(4);			
			b.putInt(0);
			byte[] result = b.array();
			
			long start = System.nanoTime();
			OutputStream outS = clientSocket.getOutputStream();
			outS.write(result);
			long end = System.nanoTime();
			
			long elapsedTime = end - start;
		    
			elapsedSeconds = (double)elapsedTime / 1000000000.0;
			System.out.println("latency is approximately: "
			        + elapsedSeconds + " seconds");
			clientSocket.close();
			clientSocket = null;	//mark for gc					
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return elapsedSeconds;
	}

	private double getConnectTime(String ip) {
		double elapsedSeconds = -1;
		try {
			
			long start = System.nanoTime();
			Socket clientSocket = new Socket(ip, 9999);
			long end = System.nanoTime();
			
			long elapsedTime = end - start;
		    
			elapsedSeconds = (double)elapsedTime / 1000000000.0;
			System.out.println("Connect took approximately: "
			        + elapsedSeconds + " seconds");
			clientSocket.close();
			clientSocket = null;	//mark for gc					
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return elapsedSeconds;
	}
	
	
	 private void init()
     {
         // Recreate the executor service in case the old thread group was destroyed by reloading the applet
         ApfloatContext ctx = ApfloatContext.getContext();
         ctx.setExecutorService(ApfloatContext.getDefaultExecutorService());

         try
         {
             // The applet may not be able to write files to the current directory, but probably can write to the temp directory
             FilenameGenerator filenameGenerator = new FilenameGenerator(System.getProperty("java.io.tmpdir"), null, null);
             ctx.setFilenameGenerator(filenameGenerator);
         }
         catch (AccessControlException ace)
         {
             // Ignore - reading the system property may not be allowed in unsigned applets
         }
     }
	private PICPUBenchmarkStats getPICPUBenchmarkStats()
	{
		PICPUBenchmarkStats stats = new PICPUBenchmarkStats();
		
		long precision = 50000;
		int radix = 10;
		
		Pi.setOut(new PrintWriter(new NullOutputStream()));
        Pi.setErr(new PrintWriter(new NullOutputStream()));

        double elapsedSeconds = -1;
        try {
        	Operation<Apfloat> operation = new PiParallel.ParallelChudnovskyPiCalculator(precision, radix);
        
        	long start = System.nanoTime();
        	Pi.run(precision, radix, operation);
			long end = System.nanoTime();
		    
			long elapsedTime = end - start;
		    
			elapsedSeconds = (double)elapsedTime / 1000000000.0;
		        
		    System.out.println("The process took approximately: "
		        + elapsedSeconds + " seconds");
		        
		} catch (ApfloatRuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        stats.setPICPUBenchmarkTime(elapsedSeconds);
		return stats;
	}
	
	private MemoryReadWriteStats getMemoryReadWriteStats()
	{
		MemoryReadWriteStats stats = new MemoryReadWriteStats();
		double elapsedSeconds = -1;
    	Sigar sigar = new Sigar();
    	try {
			Mem mem = sigar.getMem();
			
			long totalMem = mem.getTotal();
			
			int toFillMem = (int) (totalMem * 0.1);
			
			long start = System.nanoTime();
			byte[] arr = new byte[toFillMem];
			byte filler = 1;
			Arrays.fill(arr, filler);			
	    	long end = System.nanoTime();
	    	
	    	arr = null;
	    	long elapsedTime = end - start;
	    	elapsedSeconds = (double)elapsedTime / 1000000000.0;
	        
		    System.out.println("The mem fill took: "
		        + elapsedSeconds + " seconds");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    	stats.setMemFillTime(elapsedSeconds);
    	
		return stats;
	}
}




