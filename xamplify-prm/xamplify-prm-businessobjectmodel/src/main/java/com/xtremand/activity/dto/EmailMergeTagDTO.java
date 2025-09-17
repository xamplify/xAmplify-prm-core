package com.xtremand.activity.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class EmailMergeTagDTO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1399524949575054056L;
	
	private String contactFirstName;
	private String contactLastName;
	private String contactEmailId;
	private String contactCompanyName;
	private String contactMobileNumber;
	private String contactAddress;
	private String contactZipCode;
	private String contactCity;
	private String contactState;
	private String contactCountry;
	private String senderFirstName;
	private String senderLastName;
	private String senderMiddleName;
	private String senderEmailId;
	private String senderMobileNumber;
	private String senderCompanyName;
	private String senderCompanyUrl;
	private String senderCompanyGoogleUrl;
	private String senderCompanyFacebookUrl;
	private String senderCompanyInstagramUrl;
	private String senderCompanyLinkedInUrl;
	private String senderCompanyTwitterUrl;
	private String senderCompanyAddress;
	private String senderCompanyContactNumber;
	private String senderAboutUs;
	private String senderJobTitle;
	private String senderEventUrl;
	private String senderPrivacyPolicy;
	private String contactAboutUs;
	private String senderCompanyLogoPath;
}
