package com.xtremand.user.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class TypedSignatureRequestDTO implements Serializable {

	/**
	* 
	*/
	private static final long serialVersionUID = 1L;
	private String text;
	private String font;
	private Integer loggedInUserId;

}
