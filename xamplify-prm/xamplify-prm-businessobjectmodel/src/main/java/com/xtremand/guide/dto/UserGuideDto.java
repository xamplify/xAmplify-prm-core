package com.xtremand.guide.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserGuideDto {

	private Integer id;

	private String title;

	private String description;

	private String link;

	private String slug;

	private Integer moduleId;

	private Integer subModuleId;
	
	private Integer mergeTagId;

	private Integer createdBy;

	private Integer updatedBy;

	private Date createdDate;

	private Date updatedDate;
	
	private String customName;
	
	private String subModuleName;
	
}

