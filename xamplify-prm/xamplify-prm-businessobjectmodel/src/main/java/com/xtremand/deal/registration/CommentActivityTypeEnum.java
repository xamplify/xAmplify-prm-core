package com.xtremand.deal.registration;

/****XNFR-426*****/
public enum CommentActivityTypeEnum {

	LEAD_APPROVED("LEAD_APPROVED"),LEAD_REJECTED("LEAD_REJECTED"),STAGE_UPDATED("STAGE_UPDATED"),
	LEAD_CREATED("LEAD_CREATED"),DEAL_CREATED("DEAL_CREATED"),COMMENT_UPDATED("COMMENT_UPDATED"),
	LEAD_STAGE_UPDATED("LEAD_STAGE_UPDATED"),DEAL_STAGE_UPDATED("DEAL_STAGE_UPDATED");
	protected String type;
	
	CommentActivityTypeEnum(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
	
}
