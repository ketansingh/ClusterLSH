package edu.usc.pgroup.floe.api.framework.pelletmodels;

import edu.usc.pgroup.floe.api.state.FState;

public interface StatefulPellet<Input, Output> {
	public Output invoke(Input in, FState s);
}
