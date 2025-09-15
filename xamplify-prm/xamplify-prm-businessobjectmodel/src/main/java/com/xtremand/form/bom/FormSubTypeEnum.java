package com.xtremand.form.bom;

public enum FormSubTypeEnum {
	
	REGULAR("REGULAR"), QUIZ("QUIZ"), SURVEY("SURVEY");
	
	protected String type;

	private FormSubTypeEnum(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
}
