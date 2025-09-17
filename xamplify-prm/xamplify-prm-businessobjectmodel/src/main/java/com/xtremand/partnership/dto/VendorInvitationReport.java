package com.xtremand.partnership.dto;

import java.io.Serializable;
import java.util.Date;

import org.springframework.util.StringUtils;

import com.xtremand.util.dto.CreatedTimeConverter;
import com.xtremand.util.dto.DateInString;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Data
@EqualsAndHashCode(callSuper = false)
public class VendorInvitationReport extends CreatedTimeConverter implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Integer inviterId;

	private String inviterEmailId;

	private String inviterCompanyName;

	private String inviterCompanyLogo;

	private Integer inviteeId;

	private String inviteeEmailId;

	@Getter(value = AccessLevel.NONE)
	private String inviteeCompanyName;

	private String inviteeCompanyLogo;

	@Getter(value = AccessLevel.NONE)
	private Integer inviteeCompanyId;

	private String inviteeRole;
	
	private String inviteStatus;
	
	private Date updatedTime;

	@Getter(value = AccessLevel.NONE)
	private String updatedTimeInString;
	
	public String getUpdatedTimeInString() {
		if(updatedTime!=null) {
			return DateInString.getUtcString(updatedTime);
		}else {
			return "";
		}
	}


	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getInviteeCompanyName() {
		if (StringUtils.hasText(inviteeCompanyName)) {
			return inviteeCompanyName;
		} else {
			return "";
		}
	}

	public Integer getInviteeCompanyId() {
		if (inviteeCompanyId != null) {
			return inviteeCompanyId;
		} else {
			return 0;
		}

	}

}
