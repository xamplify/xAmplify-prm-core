package com.xtremand.util.dto;

import java.io.Serializable;

import org.springframework.util.StringUtils;

public class PartnerOrContactInputDTO implements Serializable {
	
	
	private static final long serialVersionUID = 7630331361498129304L;

	private String emailId;
	
	private String firstName;
	
	private String lastName;
	
	private String companyName;
	
	
	
	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public String getFirstName() {
		if(StringUtils.hasText(firstName)) {
			return firstName;
		}else {
			return "";
		}
		
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		if(StringUtils.hasText(lastName)) {
			return lastName;
		}else {
			return "";
		}
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getCompanyName() {
		if(StringUtils.hasText(companyName)) {
			return companyName;
		}else {
			return "";
		}
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	@Override
	public String toString() {
		return "PartnerOrContactInputDTO [emailId=" + emailId + ", firstName=" + firstName + ", lastName=" + lastName
				+ ", companyName=" + companyName + "]";
	}

	/**
	 * 
	 */
	

}
