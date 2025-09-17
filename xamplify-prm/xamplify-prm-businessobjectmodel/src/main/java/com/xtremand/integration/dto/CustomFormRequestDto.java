package com.xtremand.integration.dto;

import lombok.Data;

@Data
public class CustomFormRequestDto {
	
	private Integer loggedInUserId;
	
	private String opportunityType;
	
	private Integer opportunityId;
	
	private Integer companyId;
	
}
