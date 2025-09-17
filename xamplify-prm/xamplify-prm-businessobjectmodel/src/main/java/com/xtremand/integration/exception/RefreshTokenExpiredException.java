package com.xtremand.integration.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xtremand.marekto.exception.MarketoIntegrationException;

public class RefreshTokenExpiredException extends RuntimeException{
	private  static final  Logger logger = LoggerFactory.getLogger(MarketoIntegrationException.class);
	
	public RefreshTokenExpiredException(){
		super("Expired Refresh Token");
	}
	
	public RefreshTokenExpiredException(String message) {
		super(message);
		logger.error(message);
	}
	private static final long serialVersionUID = 1L;
	
	
}
