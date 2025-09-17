package com.xtremand.mdf.dto;

import java.io.Serializable;

import org.springframework.util.StringUtils;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class MdfUserMappedDTO implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1924302431085222784L;

	private String fullName;
	
	private String emailId;
	
	@Getter(value = AccessLevel.NONE)
	private String displayName;
	
	private String profilePicturePath;
	
	
	public String getDisplayName() {
		if(StringUtils.hasText(fullName)) {
			return fullName.trim();
		}else {
			return emailId;
		}
	
	}
	
	

}
