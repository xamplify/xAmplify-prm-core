package com.xtremand.activity.dto;

import java.util.List;

import lombok.Data;

@Data
public class ContactOpportunityRequestDTO {

	private Integer loggedInUserId;
	
	private Integer contactId;
	
	private boolean vanityUrlFilter;
	
	private String vendorCompanyName;
	
	private Integer loggedInUserCompanyId;
	
	private Integer vendorCompanyId;
	
	private List<List<Integer>> userIds;
	
	private Boolean isCompanyJourney;
	
	private Integer userListId;
	
	private String type;
	
	private boolean partnerMarketingCompany;
}
