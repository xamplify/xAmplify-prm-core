package com.xtremand.form.bom;

public enum FormTypeEnum {

	XAMPLIFY_FORM("XAMPLIFY_FORM"), MDF_REQUEST_FORM("MDF_REQUEST_FORM"), LMS_FORM("LMS_FORM"),
	XAMPLIFY_DEFAULT_FORM("XAMPLIFY_DEFAULT_FORM"), MASTER_PARTNER_FORM("MASTER_PARTNER_FORM"),
	XAMPLIFY_LEAD_CUSTOM_FORM("XAMPLIFY_LEAD_CUSTOM_FORM"), XAMPLIFY_DEAL_CUSTOM_FORM("XAMPLIFY_DEAL_CUSTOM_FORM");

	protected String type;

	private FormTypeEnum(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

}
