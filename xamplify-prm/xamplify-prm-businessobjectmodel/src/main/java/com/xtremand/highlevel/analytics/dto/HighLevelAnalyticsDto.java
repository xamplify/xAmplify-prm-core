package com.xtremand.highlevel.analytics.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@RequiredArgsConstructor
@Data
public class HighLevelAnalyticsDto {

	private String emailId;

	private String firstName;
	
	private String lastName;
	
	private Date dateReg;
	
	private Date dateLastLogin;
	
}
