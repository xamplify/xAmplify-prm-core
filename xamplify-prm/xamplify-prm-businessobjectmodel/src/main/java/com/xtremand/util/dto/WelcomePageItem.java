package com.xtremand.util.dto;

import lombok.Data;

@Data
public class WelcomePageItem {

	private boolean showPartners;

	private String partnersImgePosition;

	private boolean showContent;

	private String contentImagePosition;

	private boolean showContacts;

	private String contactsImagePosition;

	private boolean showTemplates;

	private String templatesImagePosition;

	private boolean showCampaigns;

	private String campaignsImagePosition;

	private boolean showTeamMembers;

	private String teamMembersImagePosition;

	private boolean showSocialAccounts;

	private String socialAccountsImagePosition;

	private boolean showAnalytics;

	private String analyticsImagePosition;
	

	public static String getPosition(Integer i) {
		return i % 2 == 0 ? "left" : "right";
	}

}
