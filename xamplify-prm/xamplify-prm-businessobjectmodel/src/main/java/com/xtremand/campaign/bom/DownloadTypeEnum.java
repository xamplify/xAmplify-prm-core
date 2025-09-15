package com.xtremand.campaign.bom;

public enum DownloadTypeEnum {

	HTML("HTML"), PDF("PDF"), IMAGE("IMAGE");

	protected String downloadType;

	private DownloadTypeEnum(String downloadType) {
		this.downloadType = downloadType;
	}

	public String getDownloadType() {
		return downloadType;
	}

}
