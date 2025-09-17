package com.xtremand.company.dto;

import lombok.Data;

@Data
public class CompanyProfileDTO {

	Integer id;

	String companyName;

	String companyProfileName;

	String emailId;

	String tagLine;

	String facebookLink;

	String googlePlusLink;

	String linkedInLink;

	String twitterLink;

	String companyLogoPath;

	String backgroundLogoPath;

	String aboutUs;

	String street;

	Integer videoId;

	boolean showVendorCompanyLogo;

	private String favIconLogoPath;

	private String loginScreenDirection = "Center";

	String phone;
	String website;
	String city;
	String state;
	String country;
	String zip;
	
	private boolean emailDnsConfigured;

	private boolean spfConfigured;

	private String privacyPolicy;

	private boolean notifyPartners;

	private String eventUrl;

	private String instagramLink;

	private boolean assetPublishedEmailNotification;
	
	private boolean trackPublishedEmailNotification;
	
	private boolean playbookPublishedEmailNotification;
	
	private boolean isDomainConnected;
	
	private String godaddyDomainName;
	
	private String companyNameStatus;
		
	private Integer addedAdminCompanyId;
	
	private boolean syncContactsCompanyList;
	
	private String supportEmailId;
	
	private String companyLogoSourceLink;

}
