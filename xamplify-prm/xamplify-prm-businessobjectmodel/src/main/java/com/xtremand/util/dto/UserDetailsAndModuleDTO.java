package com.xtremand.util.dto;

import java.io.Serializable;
import java.util.Date;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class UserDetailsAndModuleDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6703305113785381372L;

	private Integer id;

	private Date createdOn;

	private Date updatedOn;

	private String companyName;

	private String companyLogo;

	private String createdBy;

	private String emailId;

	private String status;

	@Getter(value = AccessLevel.NONE)
	private String createdDateInUTCString;

	@Getter(value = AccessLevel.NONE)
	private String updatedDateInUTCString;

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getCreatedDateInUTCString() {
		if (createdOn != null) {
			return DateInString.getUtcString(createdOn);
		} else {
			return "";
		}
	}

	public String getUpdatedDateInUTCString() {
		if (updatedOn != null) {
			return DateInString.getUtcString(updatedOn);
		} else {
			return "";
		}
	}

}
