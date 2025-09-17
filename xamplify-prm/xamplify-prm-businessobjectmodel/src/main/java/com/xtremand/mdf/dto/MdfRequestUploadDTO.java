package com.xtremand.mdf.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class MdfRequestUploadDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3518255433650908068L;
	
	
	private Integer requestId;
	
	private Integer loggedInUserId;
	
	private String description;
	

}
