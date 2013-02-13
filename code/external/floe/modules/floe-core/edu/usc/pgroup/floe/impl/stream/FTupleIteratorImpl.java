package edu.usc.pgroup.floe.impl.stream;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import edu.usc.pgroup.floe.api.communication.Message;
import edu.usc.pgroup.floe.api.exception.LandmarkException;
import edu.usc.pgroup.floe.api.exception.LandmarkPauseException;
import edu.usc.pgroup.floe.api.stream.FTupleIterator;
import edu.usc.pgroup.floe.impl.queues.TupleSourceQueue;
import edu.usc.pgroup.floe.util.BitConverter;

public class FTupleIteratorImpl implements FTupleIterator {
	private final TupleSourceQueue sourceRouter;
	private boolean pauseLandmark = false;
	private final List<String> tupleKeys;

	public FTupleIteratorImpl(TupleSourceQueue sourceRouter, List<String> tupleKeys) {
		this.sourceRouter = sourceRouter;
		this.tupleKeys = tupleKeys;
	}

	@Override
	public Map<String, Object> next() throws LandmarkException {
		Map<String, Object> ret = null;
		while (ret == null) {
			ret = next(5000, TimeUnit.MICROSECONDS);
		}
		return ret;
	}

	@Override
	public Map<String, Object> next(int timeout, TimeUnit timeunit) throws LandmarkException {
		Map<String, Object> tuple = new HashMap<String, Object>();
		Map<String, Message> tupleMessages = new HashMap<String, Message>();
		Map<String, Object> returnTuple = null;
		try {
			for (String key : tupleKeys) {
				Message m = null;
				if (pauseLandmark == true)
					throw new LandmarkPauseException("Pause exception");
				m = sourceRouter.readMessage(key, timeout, timeunit);
				if (m == null) {
					return null;
				}
				if (m != null && m.getLandMark() == true)
					throw new LandmarkException("Landmark exception");
				if (m != null) {
					tuple.put(key, BitConverter.getObject((byte[]) m.getPayload()));
					tupleMessages.put(key, m);
				}
			}
			returnTuple = tuple;
			return returnTuple;
		} finally {
			if (returnTuple == null) {
				for (String key : tuple.keySet()) {
					sourceRouter.putMessageBack(key, tupleMessages.get(key));
				}
			}
		}
	}

	public void setPauseLandmark() {
		pauseLandmark = true;
	}

}
