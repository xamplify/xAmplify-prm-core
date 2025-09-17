package com.xtremand.util.dto;

import java.util.Date;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class AllPartnersDetailsDTO {


	private String partnerCompany;
	
	private String fullName;

	private Date onboardTime;
	
	private String userStatus;
	
	private String partnerEmailId;
	
	private String emailId;
	
	private String partnerName;


	@Getter(value = AccessLevel.NONE)
	private String onboardTimeInUTCString;


	public String getOnboardTimeInUTCString() {
		if (onboardTime != null) {
			return DateInString.getUtcString(onboardTime);
		} else {
			return "";
		}
	}
}
