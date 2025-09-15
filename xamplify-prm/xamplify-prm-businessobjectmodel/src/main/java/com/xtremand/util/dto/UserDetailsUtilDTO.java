package com.xtremand.util.dto;

import java.io.Serializable;

import org.springframework.util.StringUtils;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class UserDetailsUtilDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9189093474861261395L;

	private String emailId;

	private String firstName;

	private String lastName;

	@Getter(AccessLevel.NONE)
	private String displayName;

	private String fullName;

	private String companyName;
	
	private Integer id;

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getDisplayName() {
		if (StringUtils.hasText(fullName)) {
			return fullName.trim();
		} else {
			return emailId;
		}
	}

}
