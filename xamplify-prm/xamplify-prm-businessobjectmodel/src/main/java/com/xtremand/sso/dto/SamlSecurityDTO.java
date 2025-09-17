package com.xtremand.sso.dto;

import java.io.Serializable;

import com.xtremand.sso.bom.IdentityProviderName;

import lombok.Data;

@Data
public class SamlSecurityDTO implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = 1L;

	private Integer id;
	private Integer companyId;
	private String timestamp;
	private String metadata;
	private String acsURL;
	private String metadataFileName;

	// ***************** IdP User Object Details *********************

	private String emailId;
	private String firstName;
	private String lastName;
	private String companyName;
	private Integer idpCompanyId;
	
	/** XNFR-579 **/
	private Integer createdByUserId;
	private String createdTime;
	private Integer updatedByUserId;
	private String updatedTime;
	private String acsId;
	private IdentityProviderName identityProviderName;
	private Integer loggedInUserId;


}
