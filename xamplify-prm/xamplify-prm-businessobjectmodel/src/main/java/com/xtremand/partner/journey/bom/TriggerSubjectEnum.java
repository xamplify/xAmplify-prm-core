package com.xtremand.partner.journey.bom;

public enum TriggerSubjectEnum {
	partner_has("partner_has"), partner_has_not("partner_has_not");

	protected String type;
	TriggerSubjectEnum(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
}
