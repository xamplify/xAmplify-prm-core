package com.xtremand.team.member.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class TeamMemberVanityUrlPostDTO implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8089293633553281711L;

	private boolean vanityUrlFilter;
	
	private String vanityUrlDomainName;
	
	private Integer userId;
	
	private String emailId;
	
	
	

}
