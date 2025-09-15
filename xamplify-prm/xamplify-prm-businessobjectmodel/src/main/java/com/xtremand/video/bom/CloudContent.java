package com.xtremand.video.bom;

public class CloudContent {
	private String downloadLink;
	private String fileName;
	private String oauthToken;
	public CloudContent(){
		
	}
	public CloudContent(String downloadLink, String fileName, String oauthToken) {
		super();
		this.downloadLink = downloadLink;
		this.fileName = fileName;
		this.oauthToken = oauthToken;
	}
	public String getDownloadLink() {
		return downloadLink;
	}
	public void setDownloadLink(String downloadLink) {
		this.downloadLink = downloadLink;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getOauthToken() {
		return oauthToken;
	}
	public void setOauthToken(String oauthToken) {
		this.oauthToken = oauthToken;
	}
}
