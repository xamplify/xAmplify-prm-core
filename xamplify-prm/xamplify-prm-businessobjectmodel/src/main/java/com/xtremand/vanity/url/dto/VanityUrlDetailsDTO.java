package com.xtremand.vanity.url.dto;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class VanityUrlDetailsDTO {

	private Integer userId;

	private Integer loggedInUserCompanyId;

	private Integer vendorCompanyId;

	private boolean vanityUrlFilter;

	private String vendorCompanyProfileName;

	private boolean partnerLoggedInThroughVanityUrl;

	private boolean vendorLoggedInThroughOwnVanityUrl;

	private String loggedInUserRole;

	private boolean applyFilter;

	private Map<String, Object> map = new HashMap<>();

	private Integer loginAsUserId;
	
	private Integer defaultEmailTemplateId;

}
