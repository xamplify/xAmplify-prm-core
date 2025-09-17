package com.xtremand.custom.css.dto;


import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomSkinDTO implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    
	private Integer id;
	
	private String backgroundColor;

	private String tableHeaderColor;
	
	private String tableBodyColor;
	
	private String buttonColor;
	
	private String buttonValueColor;
	
	private Integer createdBy;
	
	private Integer updatedBy;
	
	private Date createdDate;
	
	private Date updatedDate;
	
	private Integer  companyId;
	
	private String textColor;
	
	private String buttonBorderColor;
	
	private String fontFamily;
	
	private String iconColor;
	
	private String iconBorderColor;
	
	private String iconHoverColor;
	
	private String textContent;
	
	private String moduleTypeString;
	
	private boolean defaultSkin;
	
	private boolean showFooter;
	
	private String divBgColor;
	
	private String headerTextColor;
	
	private boolean darkTheme;
}
