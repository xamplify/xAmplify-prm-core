package com.xtremand.videoencoding.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FFMPEGStatus {

	private  static final  Logger logger = LoggerFactory.getLogger(FFMPEGStatus.class); 
	private String status;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		logger.debug(status);
		this.status = status;
	}
}
