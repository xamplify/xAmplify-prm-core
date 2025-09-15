package com.xtremand.deal.dto;

import java.math.BigInteger;

import lombok.Data;

@Data
public class DealVendorCountsViewResponseDTO {

	private BigInteger id;
	private Integer companyId;
	private BigInteger totalDeals = BigInteger.ZERO;
	private BigInteger wonDeals = BigInteger.ZERO;
	private BigInteger lostDeals = BigInteger.ZERO;
}
