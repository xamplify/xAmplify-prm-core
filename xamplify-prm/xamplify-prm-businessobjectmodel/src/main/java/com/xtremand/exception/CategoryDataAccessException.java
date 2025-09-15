package com.xtremand.exception;

public class CategoryDataAccessException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5287329425840180979L;

	public CategoryDataAccessException() {
		super();
	}

	public CategoryDataAccessException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public CategoryDataAccessException(String message, Throwable cause) {
		super(message, cause);
	}

	public CategoryDataAccessException(String message) {
		super(message);
	}

	public CategoryDataAccessException(Throwable cause) {
		super(cause);
	}



}
