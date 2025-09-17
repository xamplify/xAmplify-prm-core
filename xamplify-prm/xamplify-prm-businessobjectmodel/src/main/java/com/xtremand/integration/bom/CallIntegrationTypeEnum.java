package com.xtremand.integration.bom;

public enum CallIntegrationTypeEnum {

	AIRCALL("AIRCALL");
	
	protected String type;
	
	private CallIntegrationTypeEnum(String type) {
		this.type = type;
	}
	
	public String getType() {
		return type;
	}
}
