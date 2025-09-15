package com.xtremand.user.list.dto;

import java.io.Serializable;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class ContactsCSVDTO implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = 8178731490384860118L;

	private String firstName;

	private String lastName;

	private String company;

	private String jobTitle;

	private String emailId;

	private String address;

	private String city;

	private String state;

	private String zipCode;

	private String country;

	private String mobileNumber;

	private String partnerCompanyName;

	private Integer contactsCount;

	@Getter(value = AccessLevel.NONE)
	private String contactsCountInString;

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getContactsCountInString() {
		if (contactsCount != null && contactsCount > 0) {
			return String.valueOf(contactsCount);
		} else {
			return "";
		}

	}

}
