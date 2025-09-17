package com.xtremand.deal.dto;

import lombok.Data;

@Data
public class DealCountsResponseDTO {
	
	private Integer id;
	private Integer companyId;
	private Integer createdForCompanyId;
	private Integer totalDeals = 0;
	private Integer wonDeals = 0;
	private Integer lostDeals = 0;
	private String companyName;
	
}
