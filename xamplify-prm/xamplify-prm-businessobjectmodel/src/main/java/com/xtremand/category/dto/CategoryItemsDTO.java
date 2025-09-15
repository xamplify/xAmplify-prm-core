package com.xtremand.category.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class CategoryItemsDTO implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4648525448425804651L;

	
	private String moduleName;
	
	private String moduleIcon;
	
	private boolean previewAccess;
	
	private Integer moduleItemsCount;

}
