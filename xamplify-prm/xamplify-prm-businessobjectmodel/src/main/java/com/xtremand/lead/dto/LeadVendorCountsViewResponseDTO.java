package com.xtremand.lead.dto;

import java.math.BigInteger;

import lombok.Data;

@Data
public class LeadVendorCountsViewResponseDTO {

	private BigInteger id;
	private Integer companyId;
	private BigInteger totalLeads = BigInteger.ZERO;
	private BigInteger wonLeads = BigInteger.ZERO;
	private BigInteger lostLeads = BigInteger.ZERO;
	private BigInteger convertedLeads = BigInteger.ZERO;
	
}
