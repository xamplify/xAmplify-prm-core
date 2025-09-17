package com.xtremand.formbeans;

import java.io.Serializable;

import com.xtremand.campaign.bom.ModuleAccessDTO;

import lombok.Data;

@Data
public class AccountDTO implements Serializable {
	private static final long serialVersionUID = 1955272911302242935L;
	
	private Integer id;
	private String firstName;
	private String lastName;
	private String userEmailId;
	
	private String companyName;
	private String companyProfileName;
	private String emailId;
	private String tagLine;
	
	private String facebookLink;
	private String googlePlusLink;
	private String linkedInLink;
	private String twitterLink;

	private String companyLogoPath;
	private String backgroundLogoPath;

	private String phone;
	private String website;
	private String aboutUs;
	
	private String street;
	private String city;
	private String state;
	private String country;
	private String zip;
	
	private ModuleAccessDTO moduleAccessDto;
	
	private Integer roleId;
	
	
	
}
