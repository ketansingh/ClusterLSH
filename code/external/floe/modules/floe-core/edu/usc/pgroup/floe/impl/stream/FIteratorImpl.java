package edu.usc.pgroup.floe.impl.stream;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.usc.pgroup.floe.api.exception.LandmarkException;
import edu.usc.pgroup.floe.api.exception.LandmarkPauseException;
import edu.usc.pgroup.floe.api.stream.FIterator;
import edu.usc.pgroup.floe.impl.communication.MessageImpl;
import edu.usc.pgroup.floe.impl.queues.StreamSourceQueue;
import edu.usc.pgroup.floe.util.BitConverter;

public class FIteratorImpl implements FIterator {
	private static final Logger logger = Logger.getLogger(FIteratorImpl.class.getName());
	private final StreamSourceQueue sourceRouter;
	private boolean pauseLandmark = false;

	public FIteratorImpl(StreamSourceQueue sourceRouter) {
		this.sourceRouter = sourceRouter;
	}

	@Override
	public Object next(int timeout, TimeUnit timeunit) throws LandmarkException {
		MessageImpl message = null;
		if (pauseLandmark == true) {
			throw new LandmarkPauseException("Request to pause");
		}
		message = (MessageImpl) sourceRouter.getMessage(timeout, timeunit);
		if (message == null) {
			return null;
		}
		if (message.getLandMark() == true) {
			throw new LandmarkException("Landmark at input");
		}
		byte[] bytes = null;
		try {
			bytes = (byte[]) message.getPayload();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Typecast exception", e);
		}
		Object object = BitConverter.getObject(bytes);
		logger.log(Level.INFO, "message " + object + " and not landmark");
		return object;
	}

	@Override
	public Object next() throws LandmarkException {
		Object ret = null;
		while (ret == null) {
			ret = next(5000, TimeUnit.MICROSECONDS);
		}
		return ret;
	}

	public void setPauseLandmark() {
		pauseLandmark = true;
	}
}
