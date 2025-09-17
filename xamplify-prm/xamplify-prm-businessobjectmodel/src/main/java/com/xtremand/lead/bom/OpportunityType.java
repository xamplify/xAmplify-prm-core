package com.xtremand.lead.bom;

public enum OpportunityType {
	LEAD("LEAD"), DEAL("DEAL");
	protected String type;
	private OpportunityType(String type) {
		this.type = type;
	}
	public String getType() {
		return type;
	}
}
