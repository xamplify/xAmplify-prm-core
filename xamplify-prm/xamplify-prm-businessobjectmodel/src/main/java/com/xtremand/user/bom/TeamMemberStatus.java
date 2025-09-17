package com.xtremand.user.bom;

public enum TeamMemberStatus {

	APPROVE("APPROVE"),
	DECLINE("DECLINE"),
	UNAPPROVED("UNAPPROVED");
	
	protected String status;

	private TeamMemberStatus(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

	
}
