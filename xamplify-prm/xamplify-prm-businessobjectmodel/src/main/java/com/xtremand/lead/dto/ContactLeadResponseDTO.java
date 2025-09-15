package com.xtremand.lead.dto;

import lombok.Data;

@Data
public class ContactLeadResponseDTO {
	
	private Integer id;

	private Integer companyId;
	
	private Integer loggedInUserId;
	
	private Integer contactId;
	
	private String firstName;
	
	private String lastName;
	
	private String campaignName;
	
	private String stageName;
	
	private String mobileNumber;
	
	private String emailId;
	
	private String fullName;
}
