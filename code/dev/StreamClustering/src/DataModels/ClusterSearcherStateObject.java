package DataModels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ClusterSearcherStateObject {
	
	//hash map to store cluster id the Cluster representatives.
	Map<String,ClusterRepresentatives> clusterRepresentativesMap;
	
	public ClusterSearcherStateObject() {
		// TODO Auto-generated constructor stub
		clusterRepresentativesMap = new HashMap<String, ClusterRepresentatives>();
	}
	
	public void updateClusterRepresentatives(String clusterId, Tweet nt)
	{
		ClusterRepresentatives  cluster  = clusterRepresentativesMap.get(clusterId);
		if(cluster == null)
		{
			cluster = new ClusterRepresentatives(clusterId);
		}
		cluster.updateRepresentatives(nt);
		clusterRepresentativesMap.put(clusterId,cluster);
	}
	
	public DistanceResult getClosestCluster(Tweet nt)
	{
		ArrayList<String> potentialClusters = nt.getPotentialClusterList();
		double min_closest = Double.MAX_VALUE;
		String closest_cluster = null;
		
		DistanceResult r = new DistanceResult();
		r.closestClusterId = "None";
		r.setClosestClusterDistance(-1);
		
		for(String ccluster : potentialClusters)
		{
			ClusterRepresentatives cr = clusterRepresentativesMap.get(ccluster);
			
			if(cr == null)
			{
				cr = new ClusterRepresentatives(ccluster);
				clusterRepresentativesMap.put(ccluster, cr);
			}
			
			double curr_distance = cr.getClosestDistance(nt);
			if(curr_distance == -1)
			{
				continue;
			}
			
			if(curr_distance < min_closest)
			{
				curr_distance = min_closest;
				closest_cluster = ccluster;
			}
		}
		
		r.setClosestClusterDistance(min_closest);
		r.setClosestClusterId(closest_cluster);
		return r;
	}
	
}
