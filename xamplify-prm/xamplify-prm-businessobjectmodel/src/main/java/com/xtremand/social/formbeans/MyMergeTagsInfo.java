package com.xtremand.social.formbeans;

import java.io.Serializable;

import lombok.Data;

@Data
public class MyMergeTagsInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String myFirstName;

	private String myMiddleName;

	private String myLastName;

	private String myFullName;

	private String myEmailId;

	private String myContactNumber;

	private String senderCompany = "";

	private String myCompanyUrl = "";

	private String myCompanyAddress = "";

	private String myCompanyContactNumber = "";

	private String aboutUs = "";

	private String senderJobTitle = "";

	private String privacyPolicy = "";

	private String unsubscribeLink = "";

	private String eventUrl = "";
	
	/******** XNFR-281 ***********/
	private String companyInstagramUrl = "";

	private String companyTwitterUrl = "";
	
	private String companyGoogleUrl = "";
	
	private String companyFacebookUrl = "";
	
	private String companyLinkedinUrl = "";
		/******** XNFR-281 ***********/

}
