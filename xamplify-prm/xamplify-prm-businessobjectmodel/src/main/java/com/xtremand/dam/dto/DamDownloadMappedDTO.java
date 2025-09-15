package com.xtremand.dam.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class DamDownloadMappedDTO implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

	private String pageSize;
	
	private String pageOrientation;

}
