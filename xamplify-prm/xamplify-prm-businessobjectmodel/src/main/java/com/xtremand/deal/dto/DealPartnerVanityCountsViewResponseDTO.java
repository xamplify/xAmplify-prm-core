package com.xtremand.deal.dto;

import java.math.BigInteger;

import lombok.Data;

@Data
public class DealPartnerVanityCountsViewResponseDTO {
	
	private BigInteger id;
	Integer companyId;
	Integer createdForCompanyId;
	private BigInteger totalDeals = BigInteger.ZERO;
	private BigInteger wonDeals = BigInteger.ZERO;
	private BigInteger lostDeals = BigInteger.ZERO;
	
}
