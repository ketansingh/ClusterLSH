package edu.usc.pgroup.floe.api.exception;

public class LandmarkException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3010474966775213152L;

	public LandmarkException(final String msg) {
		super(msg);
	}

	public LandmarkException(String msg, Throwable th) {
		super(msg, th);
	}
}
