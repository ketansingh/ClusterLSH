package ru.fuzzysearch;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;

import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;

public class CreateIndex {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Metric mymetric = new LevensteinMetric();
		BKTreeIndexer BKT = new BKTreeIndexer(mymetric);
		ArrayList<String> dict = new ArrayList<String>();
		
		try {
			
			BufferedReader reader = new BufferedReader(new FileReader("../wordlist/corncob_lowercase.txt"));
			String text;
			int i = 0;
			while ((text = reader.readLine()) != null) {
				String word = text.trim();
				dict.add(word.toLowerCase());
				
			}			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String [] param = new String[dict.size()];
		dict.toArray(param);
		BKTreeIndex I = (BKTreeIndex) BKT.createIndex(param);
		BKTreeSearcher Searcher = new BKTreeSearcher(I, mymetric, 4);
		Set<SearchResult> K = Searcher.search("zoos",true);
		for(SearchResult R: K){
			System.out.println(param[R.index.intValue()]+":"+R.distance);
		}
		
		byte[] data = Serialize(I);
		
		FileOutputStream fos;
		try {
			fos = new FileOutputStream("out.obj");
			fos.write(data);
			fos.flush();
			fos.close();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		File fis = new File("out.obj");
		
		try {
			BKTreeIndex RI = Deserialize(getBytesFromFile(fis));
			
			BKTreeSearcher RSearcher = new BKTreeSearcher(RI, mymetric, 4);
			Set<Integer> RK = RSearcher.search("Acokanthera");
			for(Integer RR: RK){
				System.out.println(param[RR]);
			}
			
			//p.print();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}
	
	public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
    
        // Get the size of the file
        long length = file.length();
    
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }
    
        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];
    
        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }
    
        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }
    
        // Close the input stream and return bytes
        is.close();
        return bytes;
    }
	
	public static BKTreeIndex Deserialize (byte[] bytes)
	{
		BKTreeIndex I = null;
		
		try 
		{
			
			
			ByteArrayInputStream byteInput = new ByteArrayInputStream (bytes);
			ObjectInputStream ois = new ObjectInputStream (byteInput);
			I = (BKTreeIndex)ois.readObject();
		}
		catch (Exception ex) 
		{
	  
		}
		return I;
		
	}
	
	
	public static byte[] Serialize(BKTreeIndex I) 
	{
		byte[] retBytes = null;
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		try 
		{
		    ObjectOutputStream outStream = new ObjectOutputStream(byteStream); 
		    outStream.writeObject(I);
		    outStream.flush(); 
		    outStream.close(); 
		    byteStream.close();
		    retBytes = byteStream.toByteArray ();
		}
		catch (IOException ex) 
		{
		    //TODO: Handle the exception
		}
		return retBytes;
	}

}


