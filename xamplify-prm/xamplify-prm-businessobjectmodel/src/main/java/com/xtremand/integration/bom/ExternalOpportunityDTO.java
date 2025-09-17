package com.xtremand.integration.bom;

import java.util.Map;

public class ExternalOpportunityDTO {

	private String title;
	private String dealType;
	private String dealStatus;
	private Double opportunityAmount;
	private String estimatedClosedDate;
	private String role;
	private boolean close;
	private boolean won;
	private Map<String, Object> questionAnswerMap;
	
	public final String leadSource = "XAMPLIFY";
		
	public String getLeadsource() {
		return leadSource;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDealType() {
		return dealType;
	}
	public void setDealType(String dealType) {
		this.dealType = dealType;
	}
	public Double getOpportunityAmount() {
		return opportunityAmount;
	}
	public void setOpportunityAmount(Double opportunityAmount) {
		this.opportunityAmount = opportunityAmount;
	}
	public String getEstimatedClosedDate() {
		return estimatedClosedDate;
	}
	public void setEstimatedClosedDate(String estimatedClosedDate) {
		this.estimatedClosedDate = estimatedClosedDate;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public Map<String, Object> getQuestionAnswerMap() {
		return questionAnswerMap;
	}
	public void setQuestionAnswerMap(Map<String, Object> questionAnswerMap) {
		this.questionAnswerMap = questionAnswerMap;
	}
	public boolean isClose() {
		return close;
	}
	public void setClose(boolean close) {
		this.close = close;
	}
	public boolean isWon() {
		return won;
	}
	public void setWon(boolean won) {
		this.won = won;
	}
	public String getDealStatus() {
		return dealStatus;
	}
	public void setDealStatus(String dealStatus) {
		this.dealStatus = dealStatus;
	}
	
	
	
}
