package com.xtremand.highlevel.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@RequiredArgsConstructor
@Data
public class HighLevelAnalyticsInactivePartnersDto {

	private Integer userId;
	
	private String companyName;
	
	private String emailId;
	
	private String firstName;
	
	private String lastName;
	
}
