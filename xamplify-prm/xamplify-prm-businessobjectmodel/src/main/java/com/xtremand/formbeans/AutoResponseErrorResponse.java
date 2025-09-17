package com.xtremand.formbeans;

import java.util.ArrayList;
import java.util.List;

public class AutoResponseErrorResponse {
	
	private int emailErrorCount;
	
	private int websiteErrorCount;
	
	private int totalErrorCount;
	
	private List<String> emailErrorDivs = new ArrayList<>();
	
	private List<String> websiteErrorDivs = new ArrayList<>();

	public int getEmailErrorCount() {
		return emailErrorCount;
	}

	public void setEmailErrorCount(int emailErrorCount) {
		this.emailErrorCount = emailErrorCount;
	}

	public int getWebsiteErrorCount() {
		return websiteErrorCount;
	}

	public void setWebsiteErrorCount(int websiteErrorCount) {
		this.websiteErrorCount = websiteErrorCount;
	}

	public int getTotalErrorCount() {
		return totalErrorCount;
	}

	public void setTotalErrorCount(int totalErrorCount) {
		this.totalErrorCount = totalErrorCount;
	}

	public List<String> getEmailErrorDivs() {
		return emailErrorDivs;
	}

	public void setEmailErrorDivs(List<String> emailErrorDivs) {
		this.emailErrorDivs = emailErrorDivs;
	}

	public List<String> getWebsiteErrorDivs() {
		return websiteErrorDivs;
	}

	public void setWebsiteErrorDivs(List<String> websiteErrorDivs) {
		this.websiteErrorDivs = websiteErrorDivs;
	}



}
