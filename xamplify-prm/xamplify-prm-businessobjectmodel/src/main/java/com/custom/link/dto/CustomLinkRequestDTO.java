package com.custom.link.dto;

import java.io.Serializable;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.xtremand.custom.link.bom.CustomLinkType;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class CustomLinkRequestDTO implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = -9171607468414481328L;

	private Integer id;

	@Getter(value = AccessLevel.NONE)
	private String title;

	@Getter(value = AccessLevel.NONE)
	private String link;

	@Getter(value = AccessLevel.NONE)
	private String icon;

	@Getter(value = AccessLevel.NONE)
	private String description;

	private String type;

	private Integer loggedInUserId;

	private Integer companyId;

	private boolean openLinkInNewTab;

	@JsonIgnore
	private CustomLinkType customLinkType;

	/**** XNFR-532 ***/
	private boolean displayTitle;

	@Getter(value = AccessLevel.NONE)
	private String buttonText;
	/**** XNFR-532 ***/

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getTitle() {
		if (StringUtils.hasText(title)) {
			return title.trim();
		} else {
			return title;
		}

	}

	public String getLink() {
		if (StringUtils.hasText(link)) {
			return link.trim();
		} else {
			return link;
		}
	}

	public String getIcon() {
		if (StringUtils.hasText(icon)) {
			return icon.trim();
		} else {
			return icon;
		}
	}

	public String getDescription() {
		if (StringUtils.hasText(description)) {
			return description.trim();
		} else {
			return description;
		}
	}

	public String getButtonText() {
		if (StringUtils.hasText(buttonText)) {
			return buttonText.trim();
		} else {
			return "Learn More";
		}
	}

}
