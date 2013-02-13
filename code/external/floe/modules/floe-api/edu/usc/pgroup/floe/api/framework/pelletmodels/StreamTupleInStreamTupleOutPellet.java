package edu.usc.pgroup.floe.api.framework.pelletmodels;

import edu.usc.pgroup.floe.api.state.StateObject;
import edu.usc.pgroup.floe.api.stream.FTupleEmitter;
import edu.usc.pgroup.floe.api.stream.FTupleIterator;

public interface StreamTupleInStreamTupleOutPellet {
	public void invoke(FTupleIterator iterator, FTupleEmitter emitter, StateObject stateObject);
}
