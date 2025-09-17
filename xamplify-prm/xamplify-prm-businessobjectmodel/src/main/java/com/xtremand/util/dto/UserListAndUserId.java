package com.xtremand.util.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class UserListAndUserId implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7765911870228141337L;

	private Integer userListId;
	
	private Integer userId;

	private Integer companyId;

}
