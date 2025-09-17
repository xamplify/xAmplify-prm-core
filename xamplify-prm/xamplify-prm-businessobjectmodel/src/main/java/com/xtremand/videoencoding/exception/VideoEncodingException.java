package com.xtremand.videoencoding.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VideoEncodingException extends Exception{
	private  static final  Logger logger = LoggerFactory.getLogger(VideoEncodingException.class);

	private static final long serialVersionUID = 1L;
	public VideoEncodingException(){
		super();
	}
	public VideoEncodingException(String message) {
		super(message);
		logger.error(message);
	}
}
