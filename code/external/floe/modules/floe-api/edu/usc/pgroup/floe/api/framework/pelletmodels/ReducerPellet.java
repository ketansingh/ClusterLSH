package edu.usc.pgroup.floe.api.framework.pelletmodels;

import edu.usc.pgroup.floe.api.stream.FIterator;
import edu.usc.pgroup.floe.api.stream.FMapEmitter;

public interface ReducerPellet<Input, Output> {
	public void invoke(String key, FIterator<Input> in, FMapEmitter<Output> out);
}
