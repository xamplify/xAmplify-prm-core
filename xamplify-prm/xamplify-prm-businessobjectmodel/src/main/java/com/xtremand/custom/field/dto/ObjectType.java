package com.xtremand.custom.field.dto;

public enum ObjectType {
	LEAD("LEAD"), DEAL("DEAL"), CONTACT("CONTACT");
	protected String type;
	private ObjectType(String type) {
		this.type = type;
	}
	public String getType() {
		return type;
	}
}
