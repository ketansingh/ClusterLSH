package appl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import edu.usc.pgroup.floe.api.framework.ResourceInfo;


import edu.usc.pgroup.floe.impl.*;
import edu.usc.pgroup.floe.util.EucalyptusInstance;

public class ManagerStartUp 
{
	public static void main(String[] args)
	{
		List<EucalyptusInstance> tempList = new ArrayList<EucalyptusInstance>();
		EucalyptusInstance tempInstance = new EucalyptusInstance();
		tempInstance.setInstanceType("m1.xlarge");
		tempInstance.setInstanceType(new ResourceInfo(8));				
		tempList.add(tempInstance);
		tempInstance.setInstanceType("c1.medium");
		tempInstance.setInstanceType(new ResourceInfo(2));
		InputStream credentialsAsStream;
		try 
		{
			 			    
			credentialsAsStream = new FileInputStream("config/Eucalyptus.properties");		
			ManagerImpl newManager = ManagerImpl.getManager(credentialsAsStream,tempList);									
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	 
			
		
	}
}
