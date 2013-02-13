package edu.usc.pgroup.floe.api.framework.pelletmodels;

import edu.usc.pgroup.floe.api.state.StateObject;

public interface Pellet<Input, Output> {
	public Output invoke(Input in, StateObject stateObject);
}
