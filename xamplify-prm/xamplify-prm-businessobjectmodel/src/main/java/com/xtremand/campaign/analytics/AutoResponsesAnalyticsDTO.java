package com.xtremand.campaign.analytics;

import java.io.Serializable;

import lombok.Data;

@Data
public class AutoResponsesAnalyticsDTO implements Serializable {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4269976493101038404L;
	

	
	private String autoResponseType;
	
	private String subject;
	
	private String replyInDays;
	
	private String replyTimeString;
	
	private String sentTimeUtcString;
	
	private Integer actionId;
	
	private String openedTimeUtcString;
	
	private Integer urlClickCount;
	
	private String clickedUrls;

	
	

}
