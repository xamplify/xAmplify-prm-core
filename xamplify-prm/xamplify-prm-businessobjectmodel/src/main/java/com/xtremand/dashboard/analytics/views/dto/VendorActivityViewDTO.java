package com.xtremand.dashboard.analytics.views.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class VendorActivityViewDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6746000017020086349L;
	
	
	private String name;
	
	private Integer count;
	
	private String imagePath;
	
	

}
