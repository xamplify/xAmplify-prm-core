package com.xtremand.upgrade.dto;

import java.io.Serializable;
import java.util.Date;

import com.xtremand.util.dto.DateInString;
import com.xtremand.util.dto.UserDetailsUtilDTO;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Data
@EqualsAndHashCode(callSuper = false)
public class UpgradeRoleDTO extends UserDetailsUtilDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 335177798291318109L;

	private Integer id;

	private Integer companyId;

	private String companyName;

	private String companyLogo;

	private String status;

	private Date createdTime;

	private Date updatedTime;

	@Getter(value = AccessLevel.NONE)
	private String createdTimeInString;

	@Getter(value = AccessLevel.NONE)
	private String updatedTimeInString;

	public String getCreatedTimeInString() {
		if (createdTime != null) {
			return DateInString.getUtcString(createdTime);
		} else {
			return "";
		}
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getUpdatedTimeInString() {
		if (updatedTime != null) {
			return DateInString.getUtcString(updatedTime);
		} else {
			return "";
		}
	}

}
