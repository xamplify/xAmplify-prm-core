package com.xtremand.lead.bom;

/******XNFR-426***/
public enum LeadApprovalStatusEnum {
	APPROVED("APPROVED"),REJECTED("REJECTED");
	protected String type;
	
	private LeadApprovalStatusEnum(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
}
