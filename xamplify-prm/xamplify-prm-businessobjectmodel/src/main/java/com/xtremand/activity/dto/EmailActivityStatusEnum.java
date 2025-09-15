package com.xtremand.activity.dto;

public enum EmailActivityStatusEnum {

	DELIVERED("DELIVERED"), OPENED("OPENED");
	protected String status;
	
	EmailActivityStatusEnum(String status) {
		this.status = status;
	}
	
	public String getStatus() {
		return status;
	}
}
