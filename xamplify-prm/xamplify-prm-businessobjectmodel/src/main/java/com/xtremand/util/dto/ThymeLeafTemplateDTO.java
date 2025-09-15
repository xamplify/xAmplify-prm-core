package com.xtremand.util.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.xtremand.user.bom.User.UserStatus;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class ThymeLeafTemplateDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9002650876968206448L;

	private String firstName;

	private String lastName;

	private String emailId;

	private String companyLogo;

	private String displayName;
	
	private String vendorCompanyName;
	
	private String vendorCompanyProfileName;

	private String alias;

	@Getter(value = AccessLevel.NONE)
	private String status;

	@JsonIgnore
	private UserStatus userStatus;

	public String getStatus() {
		if (userStatus != null) {
			return userStatus.getStatus();
		} else {
			return status;
		}

	}

}
