package com.xtremand.upgrade.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class UpgradeRoleGetDTO implements Serializable {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Integer companyId;
	
	private Integer userId;

}
