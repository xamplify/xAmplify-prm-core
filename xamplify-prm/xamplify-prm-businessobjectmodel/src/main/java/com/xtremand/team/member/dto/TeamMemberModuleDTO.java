package com.xtremand.team.member.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class TeamMemberModuleDTO implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7155691627095567394L;

	private String moduleName;
	
	private String moduleIcon;
	
	private String description;
	
	private boolean enabled;
	
	private Integer roleId;

	public TeamMemberModuleDTO(String moduleName, String moduleIcon, String description,Integer roleId,boolean enabled) {
		super();
		this.moduleName = moduleName;
		this.moduleIcon = moduleIcon;
		this.description = description;
		this.roleId = roleId;
		this.enabled = enabled;
	}

	public TeamMemberModuleDTO() {
	}

}
