package com.xtremand.admin.dto;

import java.io.Serializable;
import java.util.List;

import com.xtremand.user.bom.User.UserStatus;

import lombok.Data;

@Data
public class AdminUserDTO implements Serializable {/**
	 * 
	 */
	private static final long serialVersionUID = 40038067820623681L;
	
	private Integer userId;
	
	private String companyName;
	
	private String emailId;
	
	private String firstName;
	
	private String lastName;
	
	private UserStatus status;
	
	private String roleIdsString;
	
	private String roleNameString;
	
	private List<Integer> roleIds;
	
	private List<String> roleNames;
	

}
