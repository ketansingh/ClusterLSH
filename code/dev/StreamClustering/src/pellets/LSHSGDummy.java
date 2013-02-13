package pellets;

import java.util.HashMap;
import java.util.Map;

import DataModels.ClosestClustersInformation;
import DataModels.Tweet;

import edu.usc.pgroup.floe.api.exception.LandmarkException;
import edu.usc.pgroup.floe.api.framework.SingleInputPellet;
import edu.usc.pgroup.floe.api.framework.pelletmodels.ReducerPellet;
import edu.usc.pgroup.floe.api.framework.pelletmodels.SingleInStreamTupleOutPellet;
import edu.usc.pgroup.floe.api.framework.pelletmodels.StreamInStreamOutPellet;
import edu.usc.pgroup.floe.api.state.StateObject;
import edu.usc.pgroup.floe.api.stream.FEmitter;
import edu.usc.pgroup.floe.api.stream.FIterator;
import edu.usc.pgroup.floe.api.stream.FMapEmitter;
import edu.usc.pgroup.floe.api.stream.FTupleEmitter;

public class LSHSGDummy implements StreamInStreamOutPellet{

	/** maximum number of examples in a cluster. */
    private long maximumClusterSize;
   
    /** minimum number of examples in a cluster. */    
    private long minimumClusterSize;
    
	@Override
	public void invoke(FIterator in, FEmitter out, StateObject stateObject) {
		// TODO Auto-generated method stub
		
	}

}
