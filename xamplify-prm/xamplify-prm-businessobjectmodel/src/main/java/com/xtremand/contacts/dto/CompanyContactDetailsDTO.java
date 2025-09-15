package com.xtremand.contacts.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.util.StringUtils;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Data
@EqualsAndHashCode(callSuper = false)
public class CompanyContactDetailsDTO implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = -1921634672406915299L;

	private Integer userId;
	
	private String address;

	private String city;

	private String contactCompany;

	private Integer contactCompanyId;

	private String country;

	@Getter(value = AccessLevel.NONE)
	private String emailId;

	private String firstName;

	private String jobTitle;

	private String lastName;

	private List<Integer> legalBasis = new ArrayList<>();

	private String mobileNumber;

	private String state;

	private String zipCode;
	
	public String getEmailId() {
		if (StringUtils.hasText(emailId)) {
			return emailId.trim().toLowerCase();
		} else {
			return emailId;
		}

	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
