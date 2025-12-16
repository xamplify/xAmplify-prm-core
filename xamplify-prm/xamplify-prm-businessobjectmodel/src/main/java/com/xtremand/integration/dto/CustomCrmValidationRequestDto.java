package com.xtremand.integration.dto;

import lombok.Data;

@Data
public class CustomCrmValidationRequestDto {
	
	private String pat;
    private Integer companyId;
    private Integer userId;

}
