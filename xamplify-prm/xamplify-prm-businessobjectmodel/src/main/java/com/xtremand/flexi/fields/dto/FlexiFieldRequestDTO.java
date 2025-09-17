package com.xtremand.flexi.fields.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class FlexiFieldRequestDTO implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = 5435050001560008540L;

	private Integer id;

	private String fieldName;

	private Integer loggedInUserId;

	private Integer companyId;
	
	private String fieldValue;

}
