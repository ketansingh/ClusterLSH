package edu.usc.pgroup.floe.util;
import edu.usc.pgroup.floe.api.framework.ResourceInfo;

public class EucalyptusInstance 
{
	String instanceType;
	ResourceInfo instanceResource;
	String instanceState;
	public EucalyptusInstance()
	{
		
	}
	public EucalyptusInstance(String insType,ResourceInfo inpRes)	
	{
		this.instanceType = insType;
		this.instanceResource = inpRes;
	}
	public String getInstanceType()
	{
		return this.instanceType;
	}
	public ResourceInfo getResourceInfo()
	{
		return this.instanceResource;
	}
	public void setInstanceType(String instanceType)
	{
		this.instanceType = instanceType;
	}
	public void setInstanceType(ResourceInfo instanceResource)
	{
		this.instanceResource = instanceResource;
	}
	public String getInstanceState()
	{
		return this.instanceState;
	}
	public void setInstanceState(String instanceState)
	{
		this.instanceState = instanceState;
	}
}
