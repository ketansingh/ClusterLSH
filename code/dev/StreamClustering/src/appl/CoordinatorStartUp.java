package appl;


import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import edu.usc.pgroup.floe.api.framework.FloeGraph;
import edu.usc.pgroup.floe.api.framework.ResourceInfo;
import edu.usc.pgroup.floe.api.framework.FloeGraph.*;
import edu.usc.pgroup.floe.impl.CoordinatorImpl;
public class CoordinatorStartUp 
{
	
	public static void main(String [] args)
	{
		try
		{												
			CoordinatorImpl tempCoordinator = CoordinatorImpl.getCoordinator();													
		}
	    catch(Exception e)
	    {
	    	e.printStackTrace();
	    }
//	    assertEquals(180, bugs.getBugInstance().size());		
	}	
}
