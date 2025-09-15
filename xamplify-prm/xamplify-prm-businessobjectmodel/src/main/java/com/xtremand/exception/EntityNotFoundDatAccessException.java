package com.xtremand.exception;

public class EntityNotFoundDatAccessException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7705262540522802894L;

	public EntityNotFoundDatAccessException() {
		super();
	}

	public EntityNotFoundDatAccessException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public EntityNotFoundDatAccessException(String message, Throwable cause) {
		super(message, cause);
	}

	public EntityNotFoundDatAccessException(String message) {
		super(message);
	}

	public EntityNotFoundDatAccessException(Throwable cause) {
		super(cause);
	}

}
