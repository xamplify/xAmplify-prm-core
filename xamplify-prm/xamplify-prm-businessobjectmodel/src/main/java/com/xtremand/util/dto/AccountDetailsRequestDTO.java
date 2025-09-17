package com.xtremand.util.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class AccountDetailsRequestDTO implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = 3096555727432073612L;

	private String emailId;

	private Integer companyIdOrUserId;

	private String filterType;

}
