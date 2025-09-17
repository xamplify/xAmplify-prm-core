package com.xtremand.dashboard.analytics.views.dto;

import java.math.BigInteger;

import lombok.Data;

@Data
public class PartnerContactsAnalyticsCountDTO {

	private BigInteger totalContacts;

	private BigInteger availableContacts;

	private BigInteger usedContacts;

}
