package edu.usc.pgroup.floe.impl.HealthManager;

import edu.usc.pgroup.floe.api.framework.FlakeInfo;

public class PeriodicThrouputCalculator extends Thread{

	long period = 1000 * 10;
	long halfLife = 1000 * 50;
	FlakeInfo finfo;
	public PeriodicThrouputCalculator(FlakeInfo info)
	{		
		finfo = info;
		this.start();
	}
	
	@Override
	public void run() {
		
		while(true)
		{
			
			finfo.AdjustThroughput(halfLife);
			
			
			try {
				Thread.currentThread().sleep(period);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
			
	}

}
