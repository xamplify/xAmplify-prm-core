package com.xtremand.lms.bom;

public enum PartnerActivityType {
	OPENED("OPENED"), VIEWED("VIEWED"), DOWNLOADED("DOWNLOADED"), FINISHED("FINISHED"), SUBMITTED("SUBMITTED");
	protected String type;

	private PartnerActivityType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
}
