package com.xtremand.util.dto;

import lombok.Data;

@Data
public class TeamMemberAnalyticsDTO {

	private String allPartnersCount;
	private String shareLeadCount;
	private String leadCount;
	private String dealCount;
	private String contactCount;
	private String mdfAmount;
	private String assetCount;
	private String trackCount;
	private String playbookCount;
	private String launchedCampaignCount;
	private String companyCount;
	private String trackAssetCount;

	private boolean showRedistributedCampaignCount = false;
	private boolean showTeamMemberCount = false;
	private boolean showContactCount = false;
	private boolean showShareLeadCount = false;
	private boolean showLeadCount = false;
	private boolean showDealCount = false;
	private boolean showMDFAmount = false;
	private boolean showAssetCount = false;
	private boolean showTrackCount = false;
	private boolean showPlaybookCount = false;
	private boolean showAllPartnersCount = false;
	private boolean showlaunchedCampaignCount = false;
	private boolean showCompanyCount = false;
	private boolean showTrackAssetCount = false;
}
