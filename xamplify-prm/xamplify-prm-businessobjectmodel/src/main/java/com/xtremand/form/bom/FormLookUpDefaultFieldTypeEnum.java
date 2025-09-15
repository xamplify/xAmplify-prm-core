package com.xtremand.form.bom;

public enum FormLookUpDefaultFieldTypeEnum {
	 AUTO_SELECT_USING_COMPANY_NAME("AUTO_SELECT_USING_COMPANY_NAME"), AUTO_SELECT_ACCOUNT_ID("AUTO_SELECT_ACCOUNT_ID");

	protected String type;

	private FormLookUpDefaultFieldTypeEnum(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
}
