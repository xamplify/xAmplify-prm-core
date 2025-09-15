package com.xtremand.custom.css.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ThemePropertiesDTO {

	private Integer id;
	
	private Integer themeId;

	private String backgroundColor;
	
	private String tableHeaderColor;
	
	private String tableBodyColor;

	private String buttonColor;

	private String buttonValueColor;
	
	private String buttonPrimaryBorderColor;
	
	private String buttonSecondaryColor;
	
	private String buttonSecondaryBorderColor;
	
	private String buttonSecondaryTextColor;

	private Integer createdBy;

	private Integer updatedBy;

	private Date createdDate;

	private Date updatedDate;

	private String textColor;

	private String buttonBorderColor;

	private String iconColor;
	
	private String iconBorderColor;
	
	private String iconHoverColor;

	private String textContent;

	private String moduleTypeString;

	private boolean showFooter;

	private String divBgColor;
	
	private String gradiantColorOne;
	
	private String gradiantColorTwo;
	


}
