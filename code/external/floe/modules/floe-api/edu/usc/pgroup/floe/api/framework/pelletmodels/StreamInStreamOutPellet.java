package edu.usc.pgroup.floe.api.framework.pelletmodels;

import edu.usc.pgroup.floe.api.state.StateObject;
import edu.usc.pgroup.floe.api.stream.FEmitter;
import edu.usc.pgroup.floe.api.stream.FIterator;

public interface StreamInStreamOutPellet<Input, Output> {
	public void invoke(FIterator<Input> in, FEmitter<Output> out, StateObject stateObject);
}
