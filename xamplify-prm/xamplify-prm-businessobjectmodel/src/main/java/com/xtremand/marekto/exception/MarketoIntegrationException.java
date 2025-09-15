package com.xtremand.marekto.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MarketoIntegrationException extends Exception{
	private  static final  Logger logger = LoggerFactory.getLogger(MarketoIntegrationException.class);

	private static final long serialVersionUID = 1L;

	public MarketoIntegrationException(){
		super();
	}
	public MarketoIntegrationException(String message) {
		super(message);
		logger.error(message);
	}
	public MarketoIntegrationException(Exception ex){
		super(ex);
	}
}
