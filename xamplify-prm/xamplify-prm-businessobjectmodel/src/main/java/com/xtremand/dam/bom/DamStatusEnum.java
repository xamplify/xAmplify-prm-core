package com.xtremand.dam.bom;

public enum DamStatusEnum {
	
	PROCESSING("PROCESSING"),
	COMPLETED("COMPLETED"),
	FAILED("FAILED");
	
	protected String actionType;

	private DamStatusEnum(String actionType) {
		this.actionType = actionType;
	}

	public String getActionType() {
		return actionType;
	}

}
