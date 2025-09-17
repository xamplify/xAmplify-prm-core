package com.xtremand.campaign.exception;

public class XamplifyDataAccessException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4103075932331572376L;

	public XamplifyDataAccessException() {
		super();
	}

	public XamplifyDataAccessException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public XamplifyDataAccessException(String message, Throwable cause) {
		super(message, cause);
	}

	public XamplifyDataAccessException(String message) {
		super(message);
	}

	public XamplifyDataAccessException(Throwable cause) {
		super(cause);
	}

}
