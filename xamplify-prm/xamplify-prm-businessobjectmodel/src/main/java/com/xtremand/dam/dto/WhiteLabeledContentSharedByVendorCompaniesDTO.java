package com.xtremand.dam.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class WhiteLabeledContentSharedByVendorCompaniesDTO implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = 8224895978357873396L;

	private Integer sharedByVendorCompanyId;

	private String sharedByVendorCompanyName;
	
}
