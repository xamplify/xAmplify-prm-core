package com.xtremand.dam.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class DamPublishGetDTO implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8018203548274800241L;

	private Integer id;
	
	private String assetName;
	
	private String description;
	
	private String htmlBody;
	
	private String jsonBody;
	
	private String vendorCompanyLogo;
	
	private String partnerCompanyLogo;
	

}
