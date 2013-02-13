package edu.usc.pgroup.floe.api.framework;

import java.util.EventObject;

import edu.usc.pgroup.floe.api.framework.HealthManager.ReactiveActions;

public class HealthManagerEvent extends EventObject{

	private ReactiveActions suggestedAction;
	private String containerID;
	private String flakeID;
	
	
	public void setSuggestedAction(ReactiveActions aSuggestedAction){
		suggestedAction = aSuggestedAction;
	}
	
	public void setContainerID(String aContainerID)
	{
		containerID = aContainerID;
	}
	
	public void setFlakeID(String aFlakeID)
	{
		flakeID = aFlakeID;
	}
	
	public ReactiveActions getSuggestedAction()
	{
		return suggestedAction;
	}
	
	public String getContainerID()
	{
		return containerID;
	}
	
	public String getFlakeID()
	{
		return flakeID;
	}
	
	public HealthManagerEvent(Object source)
	{
		super(source);
	}
}
