package com.xtremand.mdf.dto;

import java.util.Date;

import lombok.Data;

@Data
public class MdfFormSubmittedDetailsDTO {
	
	private Integer id;
	
	private String emailId;
	
	private String companyName;
	
	private String partnerCompanyName;
	
	private String requestStatus;
	
	private Date createdTime;
	
	private String companyLogo;
	
	private Integer createdBy;
	
	private Integer requestId;
	
	private Integer partnershipId;
	
	private String fullName;

	private String partnerStatus;

}
