package appl;



import java.io.IOException;
import java.util.logging.Level;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;

import edu.usc.pgroup.floe.impl.*;
import edu.usc.pgroup.floe.logging.FloeLoggerSettings;

public class ContainerStartUp 
{
	public static void main(String[] args) throws SecurityException, IOException
	{			
			FloeLoggerSettings.init("t.log", Level.OFF);
			String managerLocation = args[0];
			ContainerImpl<byte[]> container = ContainerImpl.getContainer();
			container.SetMonitorLocation(args[0]);
			Client c = Client.create();										
			//System.out.println("http://localhost:45001/Manager/addContainerInfo/Container="+container.getContainerInfo().getContainerId());
			WebResource r = c.resource("http://"+managerLocation + ":45001/Manager/addContainerInfo/Container="+container.getContainerInfo().getContainerId());																
			c.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);
			ClientResponse response ;
			response = r.get(ClientResponse.class);					
	}	
}
