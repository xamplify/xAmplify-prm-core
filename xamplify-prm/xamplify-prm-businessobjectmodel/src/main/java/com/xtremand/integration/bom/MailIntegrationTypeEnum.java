package com.xtremand.integration.bom;

public enum MailIntegrationTypeEnum {
GMAIL("GMAIL"),OUTLOOK("OUTLOOK");
	
	protected String type;
	private MailIntegrationTypeEnum(String type) {
		this.type = type;
	}
	
	public String getType() {
		return type;
	}
}
