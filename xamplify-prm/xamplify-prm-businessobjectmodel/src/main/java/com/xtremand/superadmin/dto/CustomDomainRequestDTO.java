package com.xtremand.superadmin.dto;

import java.io.Serializable;

import org.springframework.util.StringUtils;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class CustomDomainRequestDTO implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = 6792017920641933054L;

	private Integer companyId;

	@Getter(value = AccessLevel.NONE)
	private String customDomain;

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getCustomDomain() {
		if (StringUtils.hasText(customDomain)) {
			return customDomain.trim().toLowerCase();
		} else {
			return customDomain;
		}
	}

}
