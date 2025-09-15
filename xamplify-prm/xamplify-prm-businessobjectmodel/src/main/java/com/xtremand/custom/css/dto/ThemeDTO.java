package com.xtremand.custom.css.dto;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.xtremand.custom.css.bom.Theme.ThemeStatus;

import lombok.Data;

@Data
public class ThemeDTO {

	private Integer id;

	private String name;
	
	private Integer companyId;

	private String description;
	
	private String themeImagePath;
	
	private Integer parentId;
	
	private boolean defaultTheme;
	
	private Set<ThemePropertiesDTO> themesProperties=new HashSet<>();

	private Integer createdBy;

	private Integer updatedBy;

	private Date createdDate;

	private Date updatedDate;
	
	private ThemeStatus parentThemeName;
	
	private String backgroundImagePath;
	
	@JsonIgnore
	private MultipartFile file;

}
