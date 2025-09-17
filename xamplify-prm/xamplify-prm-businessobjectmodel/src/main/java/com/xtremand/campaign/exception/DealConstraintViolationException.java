package com.xtremand.campaign.exception;

public class DealConstraintViolationException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DealConstraintViolationException() {
		super();
	}

	public DealConstraintViolationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public DealConstraintViolationException(String message, Throwable cause) {
		super(message, cause);
	}

	public DealConstraintViolationException(String message) {
		super(message);
	}

	public DealConstraintViolationException(Throwable cause) {
		super(cause);
	}

}
