package com.xtremand.campaign.dto;

import java.util.Date;

import com.xtremand.util.dto.DateInString;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class WorkFlowDTO {
	
	private Integer companyId;
	
	private String companyName;
	
	private Integer userId;
	
	private String emailId;
	
	private String firstName;
	
	private String lastName;
	
	private Integer campaignId;
	
	private String campaignName;
	
	private boolean redistributed;
	
	private Date launchTime;
	
	private String timezone;

	private boolean scheduled;
	
	private Integer workflowId;
	
	private Integer replyInDays;
	
	private String subject;
	
	private Date replyTime;
	
	private Integer sentEmailsCount;
	
	private String workflowType;
	
	@Getter(value = AccessLevel.NONE)
	private String launchTimeInUTCString;
	
	@Getter(value = AccessLevel.NONE)
	private String replyTimeInUTCString;


	public String getReplyTimeInUTCString() {
		if (replyTime != null) {
			return DateInString.getUtcString(replyTime);
		} else {
			return "";
		}
	}


	

}
