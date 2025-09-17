package com.xtremand.formbeans;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class RoleDTO implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int totalRoles;
	
	private List<String> roles;
	
	private boolean isOnlyPartner;
	
	private int superiorId;
	
	private boolean isPartnerTeamMember;
	
	private String role;
	
	private String superiorRole;
	
	

}
