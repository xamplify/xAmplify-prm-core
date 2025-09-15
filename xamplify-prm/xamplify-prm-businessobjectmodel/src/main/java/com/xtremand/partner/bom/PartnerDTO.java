package com.xtremand.partner.bom;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class PartnerDTO {
	private Integer partnerId;
	private String firstName;
	private String lastName;
	private String fullName;
	private String jobTitle;
	private String emailId;
	private Integer count;
	private double countColor;
	private String countryCode;
	private String companyName;
	private String partnerCompanyName;
	private Integer campaignId;
	private String campaignName;
	private String mobileNumber;
	private String city;
	private String state;
	private String companyLogo;
	private String country;
	private String time;
	private Timestamp createdDate;
	private String deviceType;
	private boolean dataSharingEnabled;
	private String redistributedTime;
	private boolean dataShare;
	private String contactCompany;
	private String invitationStatus;
	private Integer companyId;
	private String password;
	private boolean vanityUrlDomain;
	private String region;
	private String assetName;
	private String playbook;
	private Integer assetId;
	private String asset;
	private String partnershipStatus;
	private String companyStatus;
	private Integer partnershipId;
	private String deactivatedOn;
	private String loginStatus;
	private String userStatus;
	private Integer userCompanyId;
	private String status;
	
	//XNFR-1026
	private Timestamp sentOn;
	
	private boolean activationMail;

}
