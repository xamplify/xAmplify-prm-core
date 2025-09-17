package com.xtremand.integration.bom;

public enum CalendarIntegrationTypeEnum {
	
	CALENDLY("CALENDLY");
	
	protected String type;
	
	private CalendarIntegrationTypeEnum(String type) {
		this.type = type;
	}
	
	public String getType() {
		return type;
	}

}
