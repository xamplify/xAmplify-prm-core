package com.xtremand.util.bom;

public enum TaskStatus {

	INITIATED("INITIATED"), IN_PROGRESS("IN_PROGRESS"), COMPLETED("COMPLETED"),NOT_APPLICABLE("NOT_APPLICABLE");

	protected String status;

	private TaskStatus(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

}
