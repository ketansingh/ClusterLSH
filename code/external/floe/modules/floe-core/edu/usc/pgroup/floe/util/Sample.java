package edu.usc.pgroup.floe.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import edu.usc.pgroup.floe.api.framework.FlakeInfo;
import edu.usc.pgroup.floe.api.framework.FloeGraph.EdgeList;
import edu.usc.pgroup.floe.api.framework.FloeGraph.Node;
import edu.usc.pgroup.floe.api.framework.FloeGraph.NodeList;
import edu.usc.pgroup.floe.api.framework.Port;
import edu.usc.pgroup.floe.api.framework.ResourceInfo;
import edu.usc.pgroup.floe.api.framework.rest.RestContainerResourceInfo;
import edu.usc.pgroup.floe.impl.ContainerImpl;


public class Sample 
{
	public static void tempList(List<String> inpList)
	{
		inpList.add("sas");
	}
	public static void main(String[] args)
	{
		try
		{
			ArrayList<String> inpList = new ArrayList<String>();
			tempList(inpList);
			System.out.println(inpList.size());
			Class clazz = (Class) Class.forName("edu.usc.pgroup.floe.impl.communication.MessageImpl");  
			Class[] itfs = clazz.getInterfaces();
			System.out.println(itfs[0].getName());
			JAXBContext ctx = JAXBContext.newInstance(NodeList.class);
			JAXBContext ctx1 = JAXBContext.newInstance(EdgeList.class);
			Unmarshaller um = ctx.createUnmarshaller();
			Unmarshaller um1 = ctx1.createUnmarshaller();
			NodeList tempNodeList = (NodeList) um.unmarshal(new StreamSource(new File("C://Users//Sreedhan/Desktop//NodeList.xml")));
			EdgeList tempEdgeList = (EdgeList) um1.unmarshal(new StreamSource(new File("C://Users//Sreedhan/Desktop//EdgeList.xml")));
			Marshaller m = ctx.createMarshaller();
			Marshaller m1 = ctx1.createMarshaller();
			m.marshal(tempNodeList, System.out);
			System.out.println("");
			m1.marshal(tempEdgeList, System.out);
			//ContainerImpl<byte[]> tempContainer = ContainerImpl.getContainer();
	        String tempStr = "\\l\\i\\s\\List_of_mathematics_articles_(M-O)_7f76";
	        tempStr = tempStr.trim().replaceAll("\\\\", "/").substring(1);
	        System.out.println("\n" + tempStr);
	        FlakeInfo tempInfo = new FlakeInfo();
	        ctx1 = JAXBContext.newInstance(FlakeInfo.class);
	        m1 = ctx1.createMarshaller();
			um1 = ctx1.createUnmarshaller();
			m1.marshal(tempInfo, System.out);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}			
	}
}
