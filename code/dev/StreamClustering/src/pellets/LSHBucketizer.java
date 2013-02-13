package pellets;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;


import org.apache.mahout.clustering.minhash.HashFactory;
import org.apache.mahout.clustering.minhash.HashFactory.HashType;
import org.apache.mahout.clustering.minhash.HashFunction;

import DataModels.Tweet;

import utils.TreeMapSerializer;
import utils.Utils;

import edu.usc.pgroup.floe.api.communication.Message;
import edu.usc.pgroup.floe.api.exception.LandmarkException;
import edu.usc.pgroup.floe.api.framework.pelletmodels.MapperPellet;
import edu.usc.pgroup.floe.api.framework.pelletmodels.SingleInStreamTupleOutPellet;
import edu.usc.pgroup.floe.api.framework.pelletmodels.StreamTupleInStreamTupleOutPellet;
import edu.usc.pgroup.floe.api.state.StateObject;
import edu.usc.pgroup.floe.api.stream.FIterator;
import edu.usc.pgroup.floe.api.stream.FMapEmitter;
import edu.usc.pgroup.floe.api.stream.FTupleEmitter;
import edu.usc.pgroup.floe.api.stream.FTupleIterator;

public class LSHBucketizer implements SingleInStreamTupleOutPellet{

	int numHashFunctions;
	int[] minHashValues;
	byte[] bytesToHash;
	HashFunction[] hashFunction;
	int keyGroups;
	int minVectorSize;

    public LSHBucketizer() {
    	
    	numHashFunctions = Utils.NumHashFunctions;
        minHashValues = new int[numHashFunctions];
        bytesToHash = new byte[4];
        keyGroups = 1;
        minVectorSize = 5;
        String htype = "linear";
        
        HashType hashType;
        try {
          hashType = HashType.valueOf(htype);
        } catch (IllegalArgumentException iae) {
          hashType = HashType.MURMUR;
        }
        hashFunction = HashFactory.createHashFunctions(hashType, numHashFunctions);
	}
    
    //setup funtion
    public final void setup() {
    	
    }
	
    

	public ArrayList<String> bucketize(ArrayList<Long> featureSet, String orginal) {
		
		if (featureSet == null || featureSet.size() == 0) {
			System.out.println("w2");
			return null;
		}

		ArrayList<String> cluster_list = new ArrayList<String>();


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
				// if our new hash value is less than the old one, replace the
				// old one
				if (minHashValues[i1] > hashIndex) {
					minHashValues[i1] = hashIndex;
				}
			}
		}
		
		// output the cluster information
		for (int i = 0; i < numHashFunctions; i++) {
			StringBuilder clusterIdBuilder = new StringBuilder();

			for (int j = 0; j < keyGroups; j++) {
				clusterIdBuilder.append(
						minHashValues[(i + j) % numHashFunctions]).append('-');
			}
			// remove the last dash
			clusterIdBuilder.deleteCharAt(clusterIdBuilder.length() - 1);

			cluster_list.add(clusterIdBuilder.toString());

		}
		    
		return cluster_list;
	}
	
	@Override
	public void invoke(Object in, FTupleEmitter out, StateObject stateObject) {
		
		try {
			
			byte[] bd = (byte[]) in;
			Tweet d = Utils.<Tweet>Deserialize(bd);
			
			ArrayList<Long> featureSet = Utils.getFeatureVector(d.getStemmedCleanTweet());
			ArrayList<String> clusters = bucketize(featureSet, d.getTweet());
			
			d.setFeatureVector(featureSet);
			
			int NumOfCombiners = 4;
			
			Map<String,ArrayList<String>> destinationToClusterMap = new HashMap<String,ArrayList<String>>();
			for(String cluster : clusters)
			{
				int destination = Integer.parseInt(cluster)%NumOfCombiners;
				ArrayList<String> cList = destinationToClusterMap.get("C"+destination);
				if(cList == null)
				{
					cList = new ArrayList<String>();
				}
				cList.add(cluster);
				destinationToClusterMap.put("C"+destination, cList);
			}
			
			
			Map<String, Object> outTuple = new HashMap<String, Object>();
			for(Entry<String,ArrayList<String>> e: destinationToClusterMap.entrySet())
			{
				Tweet o = new Tweet();
				o.setTweet(d.getTweet());
				o.setStemmedCleanTweet(d.getStemmedCleanTweet());
				
				
				String destination = e.getKey();
				o.setCurrentClusterList(e.getValue());
				o.setClusterSearchNodeId(destination);
				
				o.setFeatureVector(d.getFeatureVector());
				o.setGenTime(d.getGenTime());
				//System.out.println("BUCKET3 DEST: " + destination);
										
				outTuple.put(destination, o);
				//System.out.println("Bucketizer:"+o.getTweet());
			}
			
			out.emit(outTuple);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
}
