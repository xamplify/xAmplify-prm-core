package com.xtremand.user.list.dto;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import lombok.Data;

@Data
public class CopiedUserListUsersDTO implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = -3109735237162270944L;

	private Integer userListId;

	private Set<Integer> partnerIds = new HashSet<>();
	
	private Integer loggedInUserId;
	
	private Integer loggedInUserCompanyId;

}
