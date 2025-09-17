package com.xtremand.team.member.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class PartnerPrimaryAdminUpdateDto implements Serializable {

	/**
	* 
	*/
	private static final long serialVersionUID = 7157145494879808236L;
	private Integer vendorCompanyUserId;
	private Integer partnerCompanyTeamMemberUserId;

}
