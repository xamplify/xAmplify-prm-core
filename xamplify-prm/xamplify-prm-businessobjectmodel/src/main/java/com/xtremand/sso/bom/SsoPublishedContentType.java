package com.xtremand.sso.bom;

public enum SsoPublishedContentType {
	
	CAMPAIGN("Campaign"), PLAYBOOK("Playbook"), TRACK("Track"), ASSET("Asset");
	
	protected String type;
	
	private SsoPublishedContentType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
}
