package com.xtremand.superadmin.dto;

import java.io.Serializable;
import java.math.BigInteger;

import lombok.Data;

@Data
public class PartnerCompanyMetricsDTO implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -108455561604296607L;

	private BigInteger totalLeads;

	private BigInteger totalDeals;

	private BigInteger totalDealContacts;

	private BigInteger totalTeamMembers;

	private BigInteger totalRedistributedCampaigns;

	private BigInteger totalCampaignLeads;

	private BigInteger totalCampaignDeals;

}
