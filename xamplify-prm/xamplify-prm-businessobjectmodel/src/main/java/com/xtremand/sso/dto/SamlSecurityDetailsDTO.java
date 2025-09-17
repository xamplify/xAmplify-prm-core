package com.xtremand.sso.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class SamlSecurityDetailsDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// ************** IdP User Object Details *************

	private String idpEmailId;
	private String idpFirstName;
	private String idpLastName;
	private String idpCompanyName;
	private Integer idpCompanyId;

	// ************** SP User Object Details *************

	private String vendorEmailId;
	private Integer vendorUserId;
	private Integer vendorComapanyId;
	private Integer vendorSamlSecurityId;
	private String vendorComapanyProfileName;

	private Integer userId;
	private Integer companyId;

}
