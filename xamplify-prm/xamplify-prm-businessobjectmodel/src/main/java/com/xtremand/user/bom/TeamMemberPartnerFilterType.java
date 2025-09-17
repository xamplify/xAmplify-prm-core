package com.xtremand.user.bom;

public enum TeamMemberPartnerFilterType {
	
	ALL ("ALL"),
	MY_PARTNERS ("MY_PARTNERS");
	
	protected String displayType;

	private TeamMemberPartnerFilterType(String displayType) {
		this.displayType = displayType;
	}

	
	public String getDisplayType() {
		return displayType;
	}
	

}
