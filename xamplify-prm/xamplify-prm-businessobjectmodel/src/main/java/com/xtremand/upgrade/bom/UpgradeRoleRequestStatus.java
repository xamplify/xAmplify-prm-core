package com.xtremand.upgrade.bom;

public enum UpgradeRoleRequestStatus {
	
	REQUESTED("REQUESTED"), APPROVED("APPROVED"), REJECTED("REJECTED");
	protected String status;

	private UpgradeRoleRequestStatus(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

}
