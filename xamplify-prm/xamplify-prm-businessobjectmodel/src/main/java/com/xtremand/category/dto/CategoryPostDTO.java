package com.xtremand.category.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class CategoryPostDTO implements Serializable {/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private Integer categoryId;
	
	private Integer loggedInUserId;
	
	private String vendorCompanyProfileName;
	
	private boolean vanityUrlFilter;

}
