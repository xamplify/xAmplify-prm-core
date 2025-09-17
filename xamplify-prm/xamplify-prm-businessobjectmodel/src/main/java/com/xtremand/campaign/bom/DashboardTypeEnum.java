package com.xtremand.campaign.bom;

public enum DashboardTypeEnum {

	DASHBOARD("DASHBOARD"), ADVANCED_DASHBOARD("ADVANCED_DASHBOARD"), DETAILED_DASHBOARD("DETAILED_DASHBOARD");

	protected String dashboardType;

	DashboardTypeEnum(String dashboardType) {
		this.dashboardType = dashboardType;
	}

	public String getDashboardType() {
		return dashboardType;
	}

}
