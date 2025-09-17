package com.xtremand.sms.exception;

public class SmsException extends RuntimeException {

	private static final long serialVersionUID = 4257488507771758213L;
	
	public SmsException(){
		super();
	}
	
	public SmsException(String message){
		super(message);
	}
}
