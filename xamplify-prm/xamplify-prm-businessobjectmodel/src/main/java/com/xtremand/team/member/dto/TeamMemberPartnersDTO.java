package com.xtremand.team.member.dto;

import java.util.Date;

import lombok.Data;

@Data
public class TeamMemberPartnersDTO {
	
	private Integer id;

	private String emailId;

	private String firstName;

	private String lastName;

	private String companyName;

	private Integer partnerId;

	private Integer teamMemberId;

	private Date createdTime;

	private String partnerStatus;

}
