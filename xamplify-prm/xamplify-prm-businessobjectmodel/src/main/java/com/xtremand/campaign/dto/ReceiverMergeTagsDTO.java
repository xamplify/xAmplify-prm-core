package com.xtremand.campaign.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class ReceiverMergeTagsDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2260782305243675301L;

	private String firstName;

	private String lastName;

	private String fullName;

	private String emailId;

	private String companyName;

	private String mobileNumber;

	private String address;

	private String zip;

	private String city;

	private String state;

	private String country;

	private String alias;
	
	private boolean validEmail;

}
