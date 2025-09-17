package com.xtremand.custom.html.block.dto;

import java.io.Serializable;
import java.util.Set;

import com.xtremand.util.dto.CreatedTimeConverter;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class CustomHtmlBlockDTO extends CreatedTimeConverter implements Serializable {

	private static final long serialVersionUID = 5435050001560008540L;

	private Integer id;

	private String title;

	private String htmlBody;

	private String leftHtmlBody;

	private String rightHtmlBody;

	private String createdBy;

	private boolean isSelected;

	private String layoutSize;

	private Integer loggedInUserId;

	private boolean isTitleVisible;

	private Set<CustomHtmlBlockDTO> customHtmlBlockDtos;

}
