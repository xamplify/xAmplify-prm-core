package com.xtremand.highlevel.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@RequiredArgsConstructor
@Data
public class HighLevelAnalyticsOnboardPartnersDto {

	private String companyName;
	
	private String email;

	private String firstname;
	
	private String lastname;
}
