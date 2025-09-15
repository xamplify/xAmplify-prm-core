package com.xtremand.mail.bom;

public enum EmailTemplateType {
	NONE("NONE"), BASIC("BASIC"), RICH("RICH"), UPLOADED("UPLOADED"), PARTNER("PARTNER"),
	REGULAR_CO_BRANDING("REGULAR_CO_BRANDING"), VIDEO_CO_BRANDING("VIDEO_CO_BRANDING"),
	EVENT_CO_BRANDING("EVENT_CO_BRANDING"), EMAIL("EMAIL"), VIDEO("VIDEO"), EVENT("EVENT"), MARKETO("MARKETO"),
	HUBSPOT("HUBSPOT"), SURVEY("SURVEY"), SURVEY_CO_BRANDING("SURVEY_CO_BRANDING"),;

	protected String type;

	private EmailTemplateType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
}
