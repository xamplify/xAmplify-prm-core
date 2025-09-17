package com.xtremand.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmailNotificationException extends RuntimeException {

	private static final Logger logger = LoggerFactory.getLogger(EmailNotificationException.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 8362765585843217478L;

	public EmailNotificationException() {
		super();
	}

	public EmailNotificationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public EmailNotificationException(String message, Throwable cause) {
		super(message, cause);
		logger.error(message, cause);
	}

	public EmailNotificationException(String message) {
		super(message);
	}

	public EmailNotificationException(Throwable cause) {
		super(cause);
	}

}
