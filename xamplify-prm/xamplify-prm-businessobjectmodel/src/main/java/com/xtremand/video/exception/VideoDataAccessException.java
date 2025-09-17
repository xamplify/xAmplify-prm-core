package com.xtremand.video.exception;

public class VideoDataAccessException extends RuntimeException {

	private static final long serialVersionUID = 4257488507771758213L;
	
	public VideoDataAccessException(){
		super();
	}
	
	public VideoDataAccessException(String message){
		super(message);
	}
}
