package edu.usc.pgroup.floe.api.exception;

public class LandmarkPauseException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3010474966775213152L;

	public LandmarkPauseException(final String msg) {
		super(msg);
	}

	public LandmarkPauseException(String msg, Throwable th) {
		super(msg, th);
	}
}
