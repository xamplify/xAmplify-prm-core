package com.xtremand.dam.bom;

public enum DamAnalyticsActionEnum {
	
	VIEW("VIEW"),
	DOWNLOAD("DOWNLOAD");
	
	protected String actionType;

	private DamAnalyticsActionEnum(String actionType) {
		this.actionType = actionType;
	}

	public String getActionType() {
		return actionType;
	}


}
