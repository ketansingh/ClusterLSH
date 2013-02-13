package DataModels;

import java.io.Serializable;
import java.util.ArrayList;

import org.apache.mahout.vectorizer.encoders.FeatureVectorEncoder;

import edu.usc.pgroup.floe.api.framework.PelletInfo;

public class Tweet extends PelletInfo implements Serializable{
	private String tweet;
	private String stemmedCleanTweet;
	private ArrayList<String> currClusterList;
	private String currSearchClusterNodeId;
	private DistanceResult closestCluster;
	private boolean isProcessed = false;
	private ArrayList<Long> featureVector;
	private long genTime;
	
	
	public Tweet(){
	
	}
	
	public String getTweet()
	{
		return tweet;
	}
	
	public void setTweet(String t)
	{
		tweet = t;
	}
	
	
	public String getStemmedCleanTweet()
	{
		return stemmedCleanTweet;
	}
	
	public void setStemmedCleanTweet(String t)
	{
		stemmedCleanTweet = t;
	}
	
	public void setCurrentClusterList(ArrayList<String> list) {
		// TODO Auto-generated method stub
		currClusterList = list;
	}
	
	public ArrayList<String> getPotentialClusterList() {
		// TODO Auto-generated method stub
		return currClusterList;
	}

	public void setClusterSearchNodeId(String clusterSearchNodeId) {
		currSearchClusterNodeId = clusterSearchNodeId;
	}
	
	public String getClusterSearchNodeId()
	{
		return currSearchClusterNodeId;
	}

	public void setClosestCluster(DistanceResult r) {
		closestCluster = r;		
	}
	
	public DistanceResult getClosestCluster()
	{
		return closestCluster;
	}

	public boolean isProcessedTweet() {
		return isProcessed ;
	}
	
	public void setAsProcessed()
	{
		isProcessed = true;
	}

	public void setFeatureVector(ArrayList<Long> featureSet) {
		featureVector = featureSet;		
	}
	
	public ArrayList<Long> getFeatureVector()
	{
		return featureVector;
	}

	public void setGenTime(long currentTimeMillis) {
		// TODO Auto-generated method stub
		this.genTime = currentTimeMillis;
		
	}

	public long getGenTime() {
		// TODO Auto-generated method stub
		return genTime;
	}
}
