package edu.usc.pgroup.floe.api.framework.pelletmodels;

import edu.usc.pgroup.floe.api.stream.FIterator;
import edu.usc.pgroup.floe.api.stream.FMapEmitter;

public interface MapperPellet<Input> {
	public void invoke(FIterator<Input> iterator, FMapEmitter emitter);
}
