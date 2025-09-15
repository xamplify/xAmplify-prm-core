package com.xtremand.user.list.dto;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.xtremand.user.bom.User;

import lombok.Data;

@Data
public class CopyGroupUsersDTO implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = -3403134887158902772L;

	private Set<Integer> userIds;

	private Set<Integer> userGroupIds;

	private Integer loggedInUserId;
	
	private Integer userGroupId;
	
	private String moduleName;
	
	private Set<User> users = new HashSet<User>();

	private boolean move;
	
	public boolean isMove() {
	    return move;
	}
}
