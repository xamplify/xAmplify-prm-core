package com.xtremand.form.submit.bom;

public enum FormSubmitEnum {

	FORM("FORM"), MDF_REQUEST_FORM("MDF_REQUEST_FORM"), LMS_FORM("LMS_FORM");

	protected String type;

	private FormSubmitEnum(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
}
