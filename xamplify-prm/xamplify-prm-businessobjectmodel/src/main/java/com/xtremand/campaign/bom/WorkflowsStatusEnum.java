package com.xtremand.campaign.bom;

public enum WorkflowsStatusEnum {
	
	ACTIVE ("ACTIVE"),
	INACTIVE ("INACTIVE"),
	COMPLETED ("COMPLETED");
	
	protected String displayType;

	private WorkflowsStatusEnum(String displayType) {
		this.displayType = displayType;
	}

	
	public String getDisplayType() {
		return displayType;
	}

}
