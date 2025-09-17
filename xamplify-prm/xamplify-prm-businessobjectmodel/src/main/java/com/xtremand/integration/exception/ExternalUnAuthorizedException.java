package com.xtremand.integration.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ExternalUnAuthorizedException  extends RuntimeException {
	
	private  static final  Logger logger = LoggerFactory.getLogger(ExternalUnAuthorizedException.class);
	
	public ExternalUnAuthorizedException(){
		super("401 UnAuthorized from External API");
	}
	
	public ExternalUnAuthorizedException(String message) {
		super(message);
		logger.error(message);
	}
	private static final long serialVersionUID = 1L;
}
