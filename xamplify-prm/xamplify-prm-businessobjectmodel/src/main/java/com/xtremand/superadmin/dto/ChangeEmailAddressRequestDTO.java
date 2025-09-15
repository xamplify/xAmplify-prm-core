package com.xtremand.superadmin.dto;

import java.io.Serializable;

import org.springframework.util.StringUtils;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class ChangeEmailAddressRequestDTO implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = -7942356295339042037L;

	@Getter(value = AccessLevel.NONE)
	private String existingEmailAddress;

	@Getter(value = AccessLevel.NONE)
	private String updatedEmailAddress;

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getExistingEmailAddress() {
		if (StringUtils.hasText(existingEmailAddress)) {
			return existingEmailAddress.trim().toLowerCase();
		} else {
			return existingEmailAddress;
		}

	}

	public String getUpdatedEmailAddress() {
		if (StringUtils.hasText(updatedEmailAddress)) {
			return updatedEmailAddress.trim().toLowerCase();
		} else {
			return updatedEmailAddress;
		}
	}

}
