package com.xtremand.highlevel.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@RequiredArgsConstructor
@Data
public class HighLevelAnalyticsPartnerAndTeamMemberDto {

	private String partnerCompanyName;
	
	private String emailId;

	private String firstName;
	
	private String lastName;
	
	private String description;
	
	private Integer partnerId;
	
	private Integer teamMemberId;
}
