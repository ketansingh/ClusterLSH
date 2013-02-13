package pellets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import DataModels.ClusterSearcherStateObject;
import DataModels.DistanceResult;
import DataModels.Tweet;
import edu.usc.pgroup.floe.api.framework.pelletmodels.SingleInStreamTupleOutPellet;
import edu.usc.pgroup.floe.api.state.StateObject;
import edu.usc.pgroup.floe.api.stream.FTupleEmitter;

public class LSHClusterSearch implements SingleInStreamTupleOutPellet {

	@Override
	public void invoke(Object in, FTupleEmitter out, StateObject stateObject) {

		Tweet t = (Tweet) in;

		ClusterSearcherStateObject so = (ClusterSearcherStateObject) stateObject
				.get();

		if (so == null) {
			so = new ClusterSearcherStateObject();
		}

		if (!t.isProcessedTweet()) {

			ArrayList<String> clusters = t.getPotentialClusterList();
			String currClusterSearchNodeId = t.getClusterSearchNodeId();

			System.out.print("Initial Search on node: "
					+ currClusterSearchNodeId + ":");
			for (String cid : clusters) {
				System.out.print(cid + ",");
			}
			System.out.println("");

			try {
				DistanceResult r = so.getClosestCluster(t);

				t.setClosestCluster(r);

				Map<String, Object> outM = new HashMap<String, Object>();

				// for now assuming only one aggregating node
				outM.put("O1", t);

				out.emit(outM);

			} catch (Exception e) {
				e.printStackTrace();
			}

		} else {
			System.out.println("processed tweet recieved at :"
					+ t.getClusterSearchNodeId());
			DistanceResult closestCluster = t.getClosestCluster();
			so.updateClusterRepresentatives(
					closestCluster.getClosestClusterId(), t);
		}
		
		stateObject.set(so);
	}

}
