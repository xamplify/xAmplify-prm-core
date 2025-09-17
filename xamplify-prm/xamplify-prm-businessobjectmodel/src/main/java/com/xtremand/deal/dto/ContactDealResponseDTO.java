package com.xtremand.deal.dto;

import lombok.Data;

@Data
public class ContactDealResponseDTO {

	private Integer id;
	
	private String title;
	
	private String stageName;
	
	private String createdForCompanyName;
	
	private Double amount;
	
	private String closeDate;
	
	private String campaignName;

}
