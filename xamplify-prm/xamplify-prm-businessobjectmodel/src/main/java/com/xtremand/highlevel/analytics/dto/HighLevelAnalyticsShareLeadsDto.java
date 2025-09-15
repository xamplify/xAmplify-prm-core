package com.xtremand.highlevel.analytics.dto;

import java.math.BigInteger;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@RequiredArgsConstructor
@Data
public class HighLevelAnalyticsShareLeadsDto {

	private Integer companyId;
	
	private String partnerCompanyName;
	
	private String vendorCompanyName;
	
	private Integer leadsList;
	
	private BigInteger shareLeadsList;
	
	private String userListName;
	
	private Integer userListId;
	
	private String leadEmailId;
	
	private String leadName;
	
	private Integer id;
	
	private Integer numberOfShareLeads;

}
