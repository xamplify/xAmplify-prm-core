package com.xtremand.mdf.bom;

public enum MdfWorkFlowStepType {

	NEW_REQUEST("NEW_REQUEST"), IN_PROGRESS("IN_PROGRESS"), PRE_APPROVED("PRE_APPROVED"),
	REIMBURSEMENT_ISSUED("REIMBURSEMENT_ISSUED"), REQUEST_DECLINED("REQUEST_DECLINED"),
	REIMBURSEMENT_DECLINED("REIMBURSEMENT_DECLINED"), REQUEST_EXPIRED("REQUEST_EXPIRED");

	protected String status;

	private MdfWorkFlowStepType(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}
}
