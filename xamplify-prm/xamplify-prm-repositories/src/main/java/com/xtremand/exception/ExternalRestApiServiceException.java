package com.xtremand.exception;

public class ExternalRestApiServiceException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7223766855639862147L;

	public ExternalRestApiServiceException() {
		super();
	}

	public ExternalRestApiServiceException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ExternalRestApiServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExternalRestApiServiceException(String message) {
		super(message);
	}

	public ExternalRestApiServiceException(Throwable cause) {
		super(cause);
	}

}
