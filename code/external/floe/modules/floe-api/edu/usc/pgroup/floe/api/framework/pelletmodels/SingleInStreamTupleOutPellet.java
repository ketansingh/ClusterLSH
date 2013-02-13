package edu.usc.pgroup.floe.api.framework.pelletmodels;

import edu.usc.pgroup.floe.api.state.StateObject;
import edu.usc.pgroup.floe.api.stream.FTupleEmitter;

public interface SingleInStreamTupleOutPellet {
	public void invoke(Object in, FTupleEmitter out, StateObject stateObject);
}
