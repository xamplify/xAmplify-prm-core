package com.xtremand.custom.field.dto;

public enum FieldLabelType {
	TEXT("text"), TEXTAREA("textarea"), EMAIL("email"), RADIO("radio"), CHECKBOX("checkbox"), SELECT("select"),
	DATE("date"), NUMBER("number"), PERCENT("percent"), CURRENCY("currency"), DATETIME("datetime"), TIME("time"),
	MULTISELECT("multiselect"), PHONE("phone"), COUNTRY("country"), URL("url");

	protected String type;

	private FieldLabelType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
}
