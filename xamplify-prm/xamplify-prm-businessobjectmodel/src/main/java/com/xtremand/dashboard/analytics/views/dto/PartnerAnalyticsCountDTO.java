package com.xtremand.dashboard.analytics.views.dto;

import java.math.BigInteger;

import lombok.Data;

@Data
public class PartnerAnalyticsCountDTO {

	private BigInteger totalPartners;
	
	private BigInteger activePartners;
	
	private BigInteger inActivePartners;
}
