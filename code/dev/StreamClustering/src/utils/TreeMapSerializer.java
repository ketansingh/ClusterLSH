package utils;

import java.io.*;
import java.util.*;

public class TreeMapSerializer {

	private TreeMap<String, Integer> treeMap = new TreeMap<String, Integer>();
	private String stopWords = "a,able,about,across,after,all,almost,also,am,among,an,and,any,are,as,at,be,because,been,but,by,can,cannot,could,dear,did,do,does,either,else,ever,every,for,from,get,got,had,has,have,he,her,hers,him,his,how,however,i,if,in,into,is,it,its,just,least,let,like,likely,may,me,might,most,must,my,neither,no,nor,not,of,off,often,on,only,or,other,our,own,rather,said,say,says,she,should,since,so,some,than,that,the,their,them,then,there,these,they,this,tis,to,too,twas,us,wants,was,we,were,what,when,where,which,while,who,whom,why,will,with,would,yet,you,your";
	
	public TreeMapSerializer()
	{
		
	}
	
	public void Initialize()
	{
		
		StringTokenizer tokens = new StringTokenizer(stopWords,",");
		String s;
		Vector<String> v_stopWords = new Vector<String>();
		while(tokens.hasMoreTokens())
		{
			s=tokens.nextToken();
			v_stopWords.add(s);
		}
		
		try {
			
			BufferedReader reader = new BufferedReader(new FileReader("wordlist.txt"));
			String text;
			int i = 0;
			while ((text = reader.readLine()) != null) {
				String word = text.substring(0,text.indexOf(' '));
				if(!v_stopWords.contains(word.toLowerCase()))
				{
					treeMap.put(word, i);
					i++;
				}
			}			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void Deserialize (byte[] bytes)
	{
		
		try 
		{
			ByteArrayInputStream byteInput = new ByteArrayInputStream (bytes);
			ObjectInputStream ois = new ObjectInputStream (byteInput);
			treeMap = (TreeMap<String, Integer>)ois.readObject();
		}
		catch (Exception ex) 
		{
	  
		}
		
	}
	
	
	public byte[] Serialize() 
	{
		byte[] retBytes = null;
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		try 
		{
		    ObjectOutputStream outStream = new ObjectOutputStream(byteStream); 
		    outStream.writeObject(treeMap);
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

	public TreeMap<String,Integer> getTreeMap()
	{
		return treeMap;
	}
	public void print()
	{
		// Get a set of the entries 
		Set set = treeMap.entrySet(); 
		// Get an iterator 
		Iterator i = set.iterator(); 
		// Display elements 
		while(i.hasNext()) { 
			Map.Entry me = (Map.Entry)i.next(); 
			System.out.print(me.getKey() + ": "); 
			System.out.println(me.getValue()); 
		} 
		System.out.println(); 
	}
		
	
	  

}