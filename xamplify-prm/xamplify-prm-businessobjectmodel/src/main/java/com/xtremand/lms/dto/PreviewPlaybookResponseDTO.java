package com.xtremand.lms.dto;

import java.util.List;

import lombok.Data;

@Data
public class PreviewPlaybookResponseDTO {
	private Integer id;
	private String title;
	private String description;
	private String slug;
	private String featuredImage;
	private boolean published = false;
	private String createdTime;
	private String publishedOn;
	private String createdByName;
	private String categoryName;
	private boolean followAssetSequence = false;
	private String type;
	private Integer createdBy;
	private List<PlaybookContentCategoryListDTO> contents;
	private String expireDate; //XBI-4333
}
