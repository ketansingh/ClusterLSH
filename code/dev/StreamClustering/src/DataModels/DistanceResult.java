package DataModels;

import java.io.Serializable;

public class DistanceResult implements Serializable{
	String closestClusterId;
	double closestClusterDistance;
	
	public String getClosestClusterId()
	{
		return closestClusterId;
	}
	
	public double getClosestClusterDistance()
	{
		return closestClusterDistance;
	}
	
	public void setClosestClusterId(String id)
	{
		closestClusterId = id;
	}
	
	public void setClosestClusterDistance(double d)
	{
		closestClusterDistance = d;
	}
}

