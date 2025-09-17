package com.xtremand.dam.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class DamPatchDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2452731081794931851L;

	private Integer damId;

	private String assetName;

	private Integer createdBy;

	private Integer companyId;

	private Integer categoryId;

	private String categoryName;

}
