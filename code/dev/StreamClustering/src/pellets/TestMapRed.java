package pellets;



import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.mahout.clustering.minhash.HashFactory;
import org.apache.mahout.clustering.minhash.HashFunction;
//import org.apache.mahout.clustering.minhash.MinHashMapper;
import org.apache.mahout.clustering.minhash.HashFactory.HashType;
import org.apache.mahout.common.commandline.MinhashOptionCreator;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;


import org.unigram.likelike.lsh.function.CalcHashValue;
import org.unigram.likelike.lsh.function.IHashFunction;
import org.unigram.likelike.lsh.function.MinWiseFunction;

import com.sun.jmx.snmp.tasks.ThreadService;
import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;



import edu.usc.pgroup.floe.api.exception.LandmarkException;
import edu.usc.pgroup.floe.api.stream.FIterator;
import edu.usc.pgroup.floe.api.stream.FMapEmitter;

import utils.*;
import ru.fuzzysearch.*;
import sun.net.www.protocol.https.Handler;

//import pellets.LSHBucketizer;
//import pellets.LSHStreamGenerator;
import utils.TreeMapSerializer;

class BucketRunner implements Runnable
{
	private String strLine;
	int cnt;
	public BucketRunner(String sl,int cnt)
	{
		strLine = sl;
		this.cnt = cnt;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		  String newStrLine = Utils.cleanTweet(strLine);
		  
		  String stemmed = TestMapRed.stemTerm(newStrLine);
		  
		  //system.out.println (stemmed);
		  ArrayList<Long> featureVector = TestMapRed.getFeatureVector(stemmed);
		
		  TestMapRed.calltest(featureVector,strLine);
		System.out.println(cnt);
	}
	
}

public class TestMapRed {
	/** Hash function object. */
    private static IHashFunction function;
    
    /** Set of hash seeds. */
    private static long[] seedsAry; 

    /** Symbol: hash seed. */
    public static final String MINWISE_HASH_SEEDS
        = "likelike.minwise.hash.seedS";
    
    /** Default: hash seed. */
    public static final String DEFAULT_MINWISE_HASH_SEEDS    
        = "1";
    
    private static CalcHashValue calcHash  = new CalcHashValue();
    
    private static TreeMap<String, Integer> D = new TreeMap<String, Integer>();

    
    //variable from mahout
    private static HashFunction[] hashFunction;
    private static int numHashFunctions;
    private static int keyGroups;
    private static int minVectorSize;
    private static boolean debugOutput;
    private static int[] minHashValues;
    private static byte[] bytesToHash;
    
    
    private static HashMap<String, ArrayList<String> > H = new HashMap<String, ArrayList<String> >();


    
    
    //variable from mahout
    
    
    
    
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		setup();
		try{
			  FileInputStream fstream = new FileInputStream("data/tentweets.txt");
			  DataInputStream in = new DataInputStream(fstream);
			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  String strLine;
			  int cnt = 10000;
			  int maxThreads = 24;
			  Thread[] ts = new Thread[24];
			  int tcnt = 0;
			  
			  ArrayList<String> tweets = new ArrayList<String>();
			  
			  while ((strLine = br.readLine()) != null)   {
				  
				  Thread t = new Thread(new BucketRunner(strLine, cnt--));
				  ts[tcnt++] = t;
				  t.start();
				  
				  if(tcnt == 24)
				  {
					  for(int i = 0;  i < 24; i++)
					  {
						  ts[i].join();
					  }
					  tcnt = 0;
				  }
				  
			  }
			  in.close();
			  
			  
			  for(Entry<String, ArrayList<String>> entry: H.entrySet()){
				  System.out.println(entry.getKey());
				  System.out.println("------------------------------------");
				  for(String t: entry.getValue()){
					  System.out.println(t);
				  }
				  System.out.println("*********************************");
			  }
		}catch(Exception e){
			
		}
		
	}
	
	static String stemTerm (String str) {
		String[] tokens = str.split(" ");
		PorterStemmer stemmer = new PorterStemmer();
		int i=0;
		String ret_string = "";
		
		while(i<tokens.length){
			if(i==0)
				ret_string = stemmer.stem(tokens[i].toLowerCase());
			else{
				ret_string = ret_string +" "+ stemmer.stem(tokens[i].toLowerCase());
			}
			i=i+1;
		}
	    
	    return ret_string;
	}
	
	private static File fis;
	private static BKTreeIndex RI;
	
	static{
		fis = new File("../../external/StringMatching/out.obj");
		try {
			RI = Utils.<BKTreeIndex>Deserialize(getBytesFromFile(fis));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static ArrayList<Long> getFeatureVector (String str) {
		String[] tokens = str.split(" ");

		int i=0;
		String ret_string = "";
		ArrayList<Long> results = new ArrayList<Long>();
		while(i<tokens.length){
				try {
						Metric mymetric = new LevensteinMetric();
						
						String curr_tok = tokens[i];
						int dist = 4;
						if(curr_tok.length()<5){
							dist = 2;
						}
						BKTreeSearcher RSearcher = new BKTreeSearcher(RI, mymetric, dist);
						////system.out.println("Searched: " + curr_tok.toLowerCase() + "\n");
						Set<SearchResult> RK = RSearcher.search(curr_tok.toLowerCase(),true);
						////system.out.println(RK.size());
						int curr_min = 1000;
						String min_str = "";
						Long minIdx = -1L;
						for(SearchResult RR: RK){
							if(curr_min > RR.distance){
								curr_min = RR.distance;
								min_str = RI.getDictionary()[RR.index.intValue()];
								minIdx = RR.index;
							}
							////system.out.println(RI.getDictionary()[RR.index] + ":" + RR.distance);
						}
						if(minIdx != -1)
						{
							results.add(minIdx);
						}
						
						////system.out.println(min_str + " " + curr_min);
						if(ret_string==""){
							ret_string = ret_string + min_str;
						}
						else{
							ret_string = ret_string + " "+ min_str;
						}
					
					//p.print();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				i=i+1;
			}
			
		return results;
	    //return ret_string;
	}
	
	
	
	private static void createTreeMap() {
		// TODO Auto-generated method stub
		try {
			
			File fis = new File("../../external/wordlist/w1_treemap.obj");
			
			TreeMapSerializer p = new TreeMapSerializer();
			try {
				p.Deserialize(getBytesFromFile(fis));
				//p.print();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			D = p.getTreeMap();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	 private static byte[] getBytesFromFile(File file) throws IOException {
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
	
	public final static void setup() {
    	createTreeMap();
        
    	//hash from mahout!
    	numHashFunctions = 10;
        minHashValues = new int[numHashFunctions];
        bytesToHash = new byte[4];
        keyGroups = 1;
        minVectorSize = 5;
        String htype = "linear";

        HashType hashType;
        try {
          hashType = HashType.valueOf(htype);
        } catch (IllegalArgumentException iae) {
          hashType = HashType.LINEAR;
        }
        hashFunction = HashFactory.createHashFunctions(hashType, numHashFunctions);
    	
    	
        
    	
    	//hash from mahout!
    	
    	
    	
        /* create a object implements IHashFunction */
        /*
        function = new MinWiseFunction();
        
        // extract set of hash seeds 
        String seedsStr = DEFAULT_MINWISE_HASH_SEEDS;
        String[] seedsStrAry = seedsStr.split(":");
        seedsAry = new long[seedsStrAry.length];
        for (int i =0; i< seedsStrAry.length; i++) {
            seedsAry[i] =Long.parseLong(seedsStrAry[i]);
        }
        */
    }
	
	public static void calltest(ArrayList<Long> featureSet, String orginal) {
			
			//String[] tokens = s.split("\t");
		
		
			if(featureSet == null || featureSet.size() == 0) 
			{
				System.out.println("w2");
				return;
			}
			
			
			//from mahout
			for (int i1 = 0; i1 < numHashFunctions; i1++) {
			      minHashValues[i1] = Integer.MAX_VALUE;
			}

			for (int i1 = 0; i1 < numHashFunctions; i1++) {
				for (Long ele : featureSet) {
					int value = (int) ele.intValue();
			        bytesToHash[0] = (byte) (value >> 24);
			        bytesToHash[1] = (byte) (value >> 16);
			        bytesToHash[2] = (byte) (value >> 8);
			        bytesToHash[3] = (byte) value;
			        int hashIndex = hashFunction[i1].hash(bytesToHash);
			        //if our new hash value is less than the old one, replace the old one
			        if (minHashValues[i1] > hashIndex) {
			          minHashValues[i1] = hashIndex;
			        }
				}
			}
			    // output the cluster information
			    for (int i = 0; i < numHashFunctions; i++) {
			      StringBuilder clusterIdBuilder = new StringBuilder();
			      
			      for (int j = 0; j < keyGroups; j++) {
			        clusterIdBuilder.append(minHashValues[(i + j) % numHashFunctions]).append('-');
			      }
			      //remove the last dash
			      clusterIdBuilder.deleteCharAt(clusterIdBuilder.length() - 1);
			      ////system.out.println(clusterIdBuilder.toString());
			      
			      synchronized (H) {
				      if(H.containsKey(clusterIdBuilder.toString())){
				    	  H.get(clusterIdBuilder.toString()).add(orginal);
				      }
				      else{
				    	  ArrayList<String> curr_list = new ArrayList<String>();
				    	  curr_list.add(orginal);
				    	  H.put(clusterIdBuilder.toString(), curr_list);
				      }
			      }
			      
			    }
			
			
			//from mahout
						
			
			/*
			
			
			for (int i=0; i<seedsAry.length; i++) {
				

				
				
				
				//system.out.println("hello");
			    Long clusterId = returnReducerId(featureSet, seedsAry[i]);
			    //system.out.println(clusterId);
			    //context.write(new SeedClusterId(seedsAry[i], clusterId.get()), new RelatedUsersWritable(id)); 
			}
			*/
			
		}
	private static Long returnReducerId(Set<Long> featureVector, long seed) {
		// TODO Auto-generated method stub
		TreeMap<Long, Long> hashedFeatureVector = createHashedVector(featureVector, seed);
	  
		long clusterId = 0;
		for (int i = 0; i < featureVector.size(); i++) {
			if (hashedFeatureVector.size() <= 0) {
				return new Long(clusterId);
			}
			Long minimum = hashedFeatureVector.firstKey();
			clusterId += (minimum + (i * 13));
			hashedFeatureVector.remove(minimum);
		}
		return new Long(clusterId);
	
	}
	
	private static TreeMap<Long, Long> createHashedVector(Set<Long> featureVector, long seed) {
		// TODO Auto-generated method stub
		TreeMap<Long, Long> hashedFeatureVector = new TreeMap<Long, Long>(); // key: hashed feature-id, value: dummy
	
	    for (Long key : featureVector) {
	        hashedFeatureVector.put(calcHash.run(key, seed), key);
	    }
	    return hashedFeatureVector;
	}
	
	private static Set<Long> extractFeatures(StringTokenizer str) {
		// TODO Auto-generated method stub
		Set<Long> retSet = new HashSet<Long>();
		
		int i=0;
		
		while(str.hasMoreTokens()){
			try{
				String curr_t = str.nextToken();
				if(D.containsKey(curr_t)){
					Long val = new Long(D.get(curr_t));
					////system.out.println(val);

					retSet.add(val);
				}
				i=i+1;
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		return retSet;
	}


}
