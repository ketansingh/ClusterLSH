package edu.usc.pgroup.floe.api.framework.rest;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import edu.usc.pgroup.floe.api.framework.FlakeInfo;
import edu.usc.pgroup.floe.api.framework.PerfStats;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "RestPerfInfo")
@XmlType(propOrder = { "perfStatsList"})
public class RestPerfInfo {
	
	@XmlElement(name = "perfStatsList")
	List<PerfStats> perfStatsList;
	
	public RestPerfInfo()
	{
		
	}
	
	public List<PerfStats> getPerfStatsList()
	{
		return this.perfStatsList;
	}
	
	public void setPerfStatsList(List<PerfStats> inpFlakeList)
	{
		this.perfStatsList = inpFlakeList;
	}
}
