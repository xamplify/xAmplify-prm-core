package com.xtremand.integration.bom;

public enum FormColumnLayoutTypeEnum {

	SINGLE_COLUMN_LAYOUT("SINGLE_COLUMN_LAYOUT"),TWO_COLUMN_LAYOUT("TWO_COLUMN_LAYOUT");
	
	protected String type;
	
	private FormColumnLayoutTypeEnum(String type) {
		this.type = type;
	}
	
	public String getType() {
		return type;
	}
}
