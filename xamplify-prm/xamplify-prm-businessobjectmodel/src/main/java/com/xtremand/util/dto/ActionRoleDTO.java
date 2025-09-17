package com.xtremand.util.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class ActionRoleDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private boolean anyAdmin;

	private boolean superVisorTeamMember;

	private boolean teamMember;


}
