package edu.usc.pgroup.floe.api.state;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FState {
	private final static Logger logger = Logger.getLogger(FState.class.getName());
	private Object object;

	public synchronized StateObject getCopy() {
		return new StateObjectImpl(deepCopy(this.object));
	}

	public synchronized void updateState(FStateUpdator updator) {
		Object copy = deepCopy(object);
		Object update;
		try {
			update = updator.updateStateObject(copy);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error updating state using updator " + updator, e);
			return;
		}
		object = deepCopy(update);
	}

	private static Object deepCopy(Object object) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(object);

			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bais);
			return ois.readObject();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Exception while deep copying..", e);
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			logger.log(Level.SEVERE, "Exception while deep copying..", e);
			throw new RuntimeException(e);
		}
	}
}
