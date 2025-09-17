package com.xtremand.dam.dto;

import java.math.BigInteger;

import org.springframework.util.StringUtils;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class DamAnalyticsTilesDTO {

	private String emailId;

	@Setter(AccessLevel.NONE)
	private String firstName;

	@Setter(AccessLevel.NONE)
	private String lastName;
	
	@Getter(AccessLevel.NONE)
	private String fullName;

	private String contactCompany;

	private BigInteger viewCount;

	private BigInteger downloadCount;
	
	private Integer allCount;

	private Integer signedCount;
	
	private Integer notSignedCount;
	
	

	public String getFullName() {
		String customFullName = firstName+ " "+lastName;
		if(StringUtils.hasText(customFullName)) {
			return customFullName;
		}else {
			return "";
		}
		
	}

	public void setFirstName(String firstName) {
		if(StringUtils.hasText(firstName)) {
			this.firstName = firstName;
		}else {
			this.firstName = "";
		}
	}

	public void setLastName(String lastName) {
		if(StringUtils.hasText(lastName)) {
			this.lastName = lastName;
		}else {
			this.lastName = "";
		}
		
	}


}
