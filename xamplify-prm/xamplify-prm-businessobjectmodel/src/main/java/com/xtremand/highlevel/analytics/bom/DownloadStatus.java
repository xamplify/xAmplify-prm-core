package com.xtremand.highlevel.analytics.bom;

public enum DownloadStatus {

	REQUESTED("REQUESTED"), PROCESSING("PROCESSING"), COMPLETED("COMPLETED"),  FAILED("FAILED");
	protected String status;

	public String getStatus() {
		return status;
	}

	private DownloadStatus(String status) {
		this.status = status;
	}
	
	
}
