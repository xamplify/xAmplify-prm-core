package com.xtremand.partnership.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class LoginAsPartnerDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 159649708029423958L;

	private boolean loginAsPartnerOptionEnabledForVendor;

	private boolean loginAsPartnerEmailNotificationEnabled;
	
	private String vendorCompanyProfileName;
	
	private Integer vendorCompanyId;
	
	private Integer partnerCompanyId;
	
	private Integer loggedInUserId;

}
