package appl;



import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;

import edu.usc.pgroup.floe.impl.*;
import edu.usc.pgroup.floe.impl.HealthManager.HealthManagerImpl;

public class MonitorStartUp 
{
	public static void main(String[] args)
	{	
		
			String managerLocation = "128.125.124.144";
			HealthManagerImpl container = HealthManagerImpl.getInstance();
													
			//System.out.println("http://localhost:45001/Manager/addContainerInfo/Container="+container.getContainerInfo().getContainerId());
			//WebResource r = c.resource("http://"+managerLocation + ":45001/Manager/addContainerInfo/Container="+container.getContainerInfo().getContainerId());																
			//c.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);
			//ClientResponse response ;
			//response = r.get(ClientResponse.class);
	}	
}
