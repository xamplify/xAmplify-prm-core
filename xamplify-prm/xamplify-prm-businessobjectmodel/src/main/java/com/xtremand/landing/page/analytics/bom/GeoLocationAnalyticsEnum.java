package com.xtremand.landing.page.analytics.bom;

public enum GeoLocationAnalyticsEnum {

	FORM("FORM"), LANDING_PAGE("LANDING_PAGE"), LANDING_PAGE_FORM("LANDING_PAGE_FORM"), 
	CAMPAIGN_FORM("CAMPAIGN_FORM"), CAMPAIGN_LANDING_PAGE("CAMPAIGN_LANDING_PAGE"), 
	CAMPAIGN_LANDING_PAGE_FORM("CAMPAIGN_LANDING_PAGE_FORM"), OTHER_LINKS("OTHER_LINKS"),
	PARTNER_LANDING_PAGE("PARTNER_LANDING_PAGE"), PARTNER_LANDING_PAGE_FORM("PARTNER_LANDING_PAGE_FORM"),
	DAM("DAM"),FORM_SUBMIT("FORM_SUBMIT");

	protected String type;

	private GeoLocationAnalyticsEnum(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

}
