package com.xtremand.formbeans;

import java.io.Serializable;

import lombok.Data;

@Data
public class TeamMemberRoleDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private Integer roleId;
	
	private String role;

}
