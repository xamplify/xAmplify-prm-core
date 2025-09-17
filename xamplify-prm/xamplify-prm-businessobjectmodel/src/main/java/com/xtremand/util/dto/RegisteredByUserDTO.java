package com.xtremand.util.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class RegisteredByUserDTO implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = 1L;

	private Integer userId;

	private String emailId;

	private String registeredBy;

}
