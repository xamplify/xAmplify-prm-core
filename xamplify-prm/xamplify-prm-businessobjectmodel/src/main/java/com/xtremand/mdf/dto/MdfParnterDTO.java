package com.xtremand.mdf.dto;

import java.io.Serializable;
import java.util.Date;

import org.springframework.util.StringUtils;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Data
@EqualsAndHashCode(callSuper=false)
public class MdfParnterDTO extends MdfAmountTilesDTO implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Integer partnershipId;
	
	private String firstName;
	
	private String lastName;
	
	private String emailId;
	
	private String contactCompany;
	
	private Date partnershipCreatedOn;
	
	private Integer mdfDetailsId;
	
	@Getter(AccessLevel.NONE)
	private String displayName;
	
	@Getter(AccessLevel.NONE)
	private String fullName;

	private String partnerStatus;

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getDisplayName() {
		String customizedFullName = firstName+" "+lastName;
		if(StringUtils.hasText(customizedFullName)) {
			return customizedFullName.trim();
		}else {
			return emailId;
		}
	}

	public String getFullName() {
		String customizedFullName = firstName+" "+lastName;
		if(StringUtils.hasText(customizedFullName)) {
			return customizedFullName.trim();
		}else if(StringUtils.hasText(firstName) && !StringUtils.hasText(lastName)) {
			customizedFullName = firstName;
		}else if(!StringUtils.hasText(firstName) && StringUtils.hasText(lastName)) {
			customizedFullName = lastName;
		}
		return customizedFullName;
	}

	
}
