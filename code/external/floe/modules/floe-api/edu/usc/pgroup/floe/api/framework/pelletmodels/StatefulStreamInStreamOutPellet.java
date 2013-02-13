package edu.usc.pgroup.floe.api.framework.pelletmodels;

import edu.usc.pgroup.floe.api.state.FState;
import edu.usc.pgroup.floe.api.stream.FEmitter;
import edu.usc.pgroup.floe.api.stream.FIterator;

public interface StatefulStreamInStreamOutPellet<Input, Output> {
	public void invoke(FIterator<Input> in, FEmitter<Output> out, FState s);
}
