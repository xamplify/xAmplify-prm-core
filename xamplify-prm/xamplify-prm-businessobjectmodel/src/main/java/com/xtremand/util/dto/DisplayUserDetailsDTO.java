package com.xtremand.util.dto;

import java.io.Serializable;

import org.springframework.util.StringUtils;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class DisplayUserDetailsDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3527866841818396155L;

	private String fullName;

	private String emailId;

	@Getter(value = AccessLevel.NONE)
	private String displayName;
	
	
	public String getDisplayName() {
		if(StringUtils.hasText(fullName)) {
			return fullName.trim();
		}else {
			return emailId;
		}
	
	}

}
