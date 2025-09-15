package com.xtremand.highlevel.analytics.dto;

import java.math.BigInteger;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@RequiredArgsConstructor
@Data
public class HighLevelAnalyticsActivePartnersDto {

	private String vendorCompanyName;
	
	private String partnerCompanyName;
	
	private Integer userId;
	
	private String emailId;
	
	private String firstName;
	
	private String lastName;
	
	private String firstCampaignName;
	
	private Date firstCampaignlaunchedTime;
	
	private String recentCampaignName;
	
	private Date recentCampaignLaunchedTime;
	
	private BigInteger redistributedCampaignId;
	
	private Integer customerId;
	
	private Integer companyId;
	
	private Date dateReg;
	
	private Date dateLastLogin;
	
	
}
