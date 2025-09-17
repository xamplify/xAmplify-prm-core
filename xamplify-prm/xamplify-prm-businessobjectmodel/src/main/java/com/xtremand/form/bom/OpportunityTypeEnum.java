package com.xtremand.form.bom;

public enum OpportunityTypeEnum {
	LEAD("LEAD"), DEAL("DEAL");
	protected String type;
	private OpportunityTypeEnum(String type) {
		this.type = type;
	}
	public String getType() {
		return type;
	}
}
