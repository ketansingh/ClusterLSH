package DataModels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ClusterRepresentatives {
	
	String clusterId;
	
	List<Tweet> tweetListInMemory = new ArrayList<Tweet>();

	ArrayList<Long> centroid_featureVector = new ArrayList<Long>();
	
	public ClusterRepresentatives(String ccluster) {
		// TODO Auto-generated constructor stub
		clusterId = ccluster;
	}

	public void updateRepresentatives(Tweet nt) {
		// TODO Auto-generated method stub
		
	}

	public double getClosestDistance(Tweet nt) {
		
		if(tweetListInMemory.size() < 10)
		{
			Tweet t = new Tweet();
			t.setTweet(nt.getTweet());
			t.setFeatureVector(nt.getFeatureVector());
			
			tweetListInMemory.add(t);
			
			ArrayList<Long> fv = nt.getFeatureVector();
			for(Long feature : fv)
			{
				if(!centroid_featureVector.contains(feature))
					centroid_featureVector.add(feature);
			}
			
			return -1;
		}
		else
		{
			ArrayList<Long> fv = nt.getFeatureVector();
			Collections.sort(fv);
			Collections.sort(centroid_featureVector);
			
			//long largestIndexFV = fv.get(fv.size()-1);
			//long largestIndexCentroid = centroid_featureVector.get(centroid_featureVector.size()-1);
			
			int fvArrayIndex = 0;
			int centroidArrayIndex = 0;
			double distance = 0;
			while(fvArrayIndex < fv.size() && centroidArrayIndex < centroid_featureVector.size())
			{
				long featureIndexInFV = fv.get(fvArrayIndex);
				long featureIndexInCentroid = centroid_featureVector.get(centroidArrayIndex);
				
				
				if(featureIndexInFV<featureIndexInCentroid)
				{
					fvArrayIndex++;
					distance += 1;
				}
				else if (featureIndexInCentroid > featureIndexInFV)
				{
					centroidArrayIndex++;
					distance += 1;
				}
				else
				{
					fvArrayIndex++;
					centroidArrayIndex++;
				}
			}
			
			return Math.sqrt(distance);
		}
		
	}

}
