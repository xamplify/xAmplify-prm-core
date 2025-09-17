package com.xtremand.form.bom;

public enum FormFieldTypeEnum {
	DEFAULT("DEFAULT"), CUSTOM("CUSTOM");

	protected String type;

	private FormFieldTypeEnum(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

}
