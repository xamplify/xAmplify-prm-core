package com.xtremand.partner.journey.bom;

public enum TriggerComponentType {
	SUBJECT("SUBJECT"), 
	ACTION("ACTION"), 
	TIME_PHRASE("TIME_PHRASE"), 
	FILTER_PROPERTY("FILTER_PROPERTY"), 
	FILTER_CONDITION("FILTER_CONDITION");
	protected String type;

	private TriggerComponentType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
}
