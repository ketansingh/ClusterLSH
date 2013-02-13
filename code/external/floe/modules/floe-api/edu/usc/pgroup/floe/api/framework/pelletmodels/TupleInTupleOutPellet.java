package edu.usc.pgroup.floe.api.framework.pelletmodels;

import java.util.Map;

import edu.usc.pgroup.floe.api.state.StateObject;

public interface TupleInTupleOutPellet {
	public Map<String, Object> invoke(Map<String, Object> in, StateObject stateObject);
}
