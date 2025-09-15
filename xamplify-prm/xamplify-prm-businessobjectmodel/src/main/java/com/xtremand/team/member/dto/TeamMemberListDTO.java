package com.xtremand.team.member.dto;

import java.io.Serializable;
import java.util.Date;

import org.springframework.util.StringUtils;

import com.xtremand.user.bom.TeamMemberStatus;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class TeamMemberListDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6625362297655010159L;

	private Integer teamMemberUserId;

	private String firstName;

	private String lastName;

	private String emailId;
	
	private String companyName;
	
	private Integer companyId;
	
	private Date logInTime;

	private Integer teamMemberId;

	private Integer partnersCount;

	private String status;

	private Boolean loginAs;

	private Integer teamMemberGroupId;

	private String teamMemberGroupName;

	private boolean resendInvitation;

	private boolean secondAdmin;

	private boolean enableOption;

	private Integer teamMemberGroupPartnersCount;

	private boolean primaryAdmin;
	
	private String mobileNumber;

	@Getter(value = AccessLevel.NONE)
	private boolean enabled;

	@Getter(value = AccessLevel.NONE)
	private String loginAsToolTipMessage;
	
	private String fullName;

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public boolean isEnabled() {
		if (StringUtils.hasText(this.status)) {
			this.enabled = TeamMemberStatus.APPROVE.name().equalsIgnoreCase(this.status);
		} else {
			this.enabled = false;
		}
		return this.enabled;
	}

	public String getLoginAsToolTipMessage() {
		return this.enabled ? "" : "Can't login for inactive team member";
	}

}
