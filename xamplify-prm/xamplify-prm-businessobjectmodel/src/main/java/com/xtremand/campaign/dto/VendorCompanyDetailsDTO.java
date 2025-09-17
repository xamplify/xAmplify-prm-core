package com.xtremand.campaign.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class VendorCompanyDetailsDTO implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = -2036016105654690439L;

	private String companyLogo;

	private Integer companyId;

	private String companyWebsite;

}
