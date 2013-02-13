package edu.usc.pgroup.floe.api.framework.pelletmodels;

import java.util.Map;

import edu.usc.pgroup.floe.api.state.FState;
import edu.usc.pgroup.floe.api.stream.FEmitter;
import edu.usc.pgroup.floe.api.stream.FIterator;

public interface StatefulStreamTupleInStreamTupleOutPellet {
	public void invoke(FIterator<Map<String, Object>> in, FEmitter<Map<String, Object>> out, FState s);
}
