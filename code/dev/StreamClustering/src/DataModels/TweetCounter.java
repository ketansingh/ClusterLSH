package DataModels;

public class TweetCounter {

	double minDist = Double.MAX_VALUE;
	int counter = 0;
	DistanceResult min = null;
	final int MAX_CNT = 10;
	
	public DistanceResult getClosestOrUpdateCounter(Tweet t) {
		if(counter < MAX_CNT)
		{
			DistanceResult r = t.getClosestCluster();
			if(r.getClosestClusterDistance() < minDist)
			{
				minDist = r.getClosestClusterDistance();
				min = r;				
			}
			
			counter += t.getPotentialClusterList().size();
		}
		else
		{
			return min;
		}
		
		return null;
	}

}
