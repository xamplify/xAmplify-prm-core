package com.custom.link.dto;

import java.io.Serializable;

import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.xtremand.util.dto.CreatedTimeConverter;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonPropertyOrder({ "id", "title", "link", "icon" })
public class CustomLinkResponseDTO extends CreatedTimeConverter implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = 2169946076551983383L;

	private Integer id;

	private String title;

	private String link;

	private String icon;

	private boolean openLinkInNewTab;

	private String type;

	private String createdBy;

	private String description;

	private String bannerImagePath;

	/**** XNFR-532 ***/
	private boolean displayTitle;

	private String buttonText;
	/**** XNFR-532 ***/
	
	@Transient
	private String cdnBannerImagePath;

}
