package com.xtremand.integration.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomFieldsLimitReachedException extends RuntimeException{

private  static final  Logger logger = LoggerFactory.getLogger(CustomFieldsLimitReachedException.class);
	
	public CustomFieldsLimitReachedException(){
		super("Could not add custom field. Custom fields limit reached.");
	}
	
	public CustomFieldsLimitReachedException(String message) {
		super(message);
		logger.error(message);
	}
	private static final long serialVersionUID = 1L;
}
