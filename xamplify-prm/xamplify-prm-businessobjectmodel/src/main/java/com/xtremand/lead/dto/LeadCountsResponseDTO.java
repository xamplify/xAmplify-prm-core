package com.xtremand.lead.dto;

import lombok.Data;

@Data 
public class LeadCountsResponseDTO {

	private Integer id;
	private Integer companyId;
	private Integer createdForCompanyId;
	private Integer totalLeads = 0;
	private Integer wonLeads = 0;
	private Integer lostLeads = 0;
	private Integer convertedLeads = 0;
	
}
