package com.xtremand.dam.bom;

public enum ApprovalStatusType {

	CREATED("CREATED"), APPROVED("APPROVED"), REJECTED("REJECTED"),COMMENTED("COMMENTED"),UPDATED("UPDATED"),DRAFT("DRAFT");

	protected String status;

	private ApprovalStatusType(String status) {
		this.status = status;
	}

	public String getApprovalStatusType() {
		return status;
	}

}
