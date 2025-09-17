package com.xtremand.dao.exception;

public class ObjectNotFoundException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8545343935517530501L;

	public ObjectNotFoundException() {
		super();
	}

	public ObjectNotFoundException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ObjectNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public ObjectNotFoundException(String message) {
		super(message);
	}

	public ObjectNotFoundException(Throwable cause) {
		super(cause);
	}
	
}
