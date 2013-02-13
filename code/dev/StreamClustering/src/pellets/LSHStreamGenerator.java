package pellets;

import java.util.HashMap;
import java.util.Map;

import DataModels.ClosestClustersInformation;
import DataModels.DistanceResult;
import DataModels.StreamGeneratorStateDataModel;
import DataModels.Tweet;

import edu.usc.pgroup.floe.api.exception.LandmarkException;
import edu.usc.pgroup.floe.api.framework.pelletmodels.ReducerPellet;
import edu.usc.pgroup.floe.api.framework.pelletmodels.SingleInStreamTupleOutPellet;
import edu.usc.pgroup.floe.api.framework.pelletmodels.StreamInStreamOutPellet;
import edu.usc.pgroup.floe.api.state.StateObject;
import edu.usc.pgroup.floe.api.stream.FEmitter;
import edu.usc.pgroup.floe.api.stream.FIterator;
import edu.usc.pgroup.floe.api.stream.FMapEmitter;
import edu.usc.pgroup.floe.api.stream.FTupleEmitter;

public class LSHStreamGenerator implements SingleInStreamTupleOutPellet{

	/** maximum number of examples in a cluster. */
    private long maximumClusterSize;
   
    /** minimum number of examples in a cluster. */    
    private long minimumClusterSize;
    
    
    @Override
	public void invoke(Object in, FTupleEmitter out, StateObject stateObject) {
		Map<String, ClosestClustersInformation> tweetClusters = new HashMap<String, ClosestClustersInformation>();
    	
		
		int numOfCombiners = 4;
		
		Tweet t = (Tweet) in;
		
		StreamGeneratorStateDataModel model = (StreamGeneratorStateDataModel) stateObject.get();
		
		if(model == null)
		{
			model = new StreamGeneratorStateDataModel();		
		}
		
		DistanceResult result = model.getClosestOrUpdateCounter(t);
		
		if(result != null)//counter updated		
		{
			//System.out.println("TSG: recieved all clusters");
			t.setAsProcessed();
			
			t.setClosestCluster(result);
			
			Long id = new Long(result.getClosestClusterId());
			
			//NOTE: MAP CLUSTER ID TO NODE ID USING MOD (SAME AS BEFORE)
			int destination = (int) (id % numOfCombiners);			
			
			Map<String,Object> processedTweet = new HashMap<String, Object>();
			
			processedTweet.put("C"+destination, t);

			processedTweet.put("O", t);
			
			System.out.println( result.getClosestClusterId() + t.getTweet());
			
			out.emit(processedTweet);		
		}
		else
		{
			//System.out.println("updated counter:" + (System.currentTimeMillis() - t.getGenTime()) );
		}
		
		stateObject.set(model);
		
	}

}
