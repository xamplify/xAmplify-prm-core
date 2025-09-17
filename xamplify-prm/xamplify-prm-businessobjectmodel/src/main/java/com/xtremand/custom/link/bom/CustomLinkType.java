package com.xtremand.custom.link.bom;

public enum CustomLinkType {

	NEWS("NEWS"), ANNOUNCEMENTS("ANNOUNCEMENTS"), DASHBOARD_BANNERS("DASHBOARD_BANNERS");

	protected String linkType;

	private CustomLinkType(String linkType) {
		this.linkType = linkType;
	}

	public String getLinkType() {
		return linkType;
	}

}
