package com.xtremand.guide.dto;

import lombok.Data;

@Data
public class UserGuideDashboardDto {
    
	
	private boolean isPrmCompany;
	
	private boolean isPartnerLoggedInThroughVanityUrl;
	
	private boolean isVendorLoggedInThroughOwnVanityUrl;
}
