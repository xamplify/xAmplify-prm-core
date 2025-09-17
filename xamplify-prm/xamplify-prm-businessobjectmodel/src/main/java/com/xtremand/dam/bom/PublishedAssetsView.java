package com.xtremand.dam.bom;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.Transient;

import org.springframework.util.StringUtils;

import com.xtremand.util.dto.DateInString;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class PublishedAssetsView implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = 1509167593520145957L;

	private Integer id;

	private Integer damId;

	private Integer partnerCompanyId;

	private Integer partnerId;

	private Integer vendorCompanyId;

	private String assetName;

	private String alias;

	private String assetType;

	private String thumbnailPath;

	private boolean beeTemplate;

	private Date publishedTime;

	private String status;

	@Getter(value = AccessLevel.NONE)
	private String displayName;

	private String emailId;

	private String vendorCompanyName;

	private Integer videoId;

	@Transient
	@Getter(value = AccessLevel.NONE)
	private String publishedTimeInUTCString;

	private String tagNamesString;

	private List<String> tagNames;

	private String categoryName;
	
	private boolean vendorSignatureRequired;
	
	private boolean partnerSignatureRequired;
	
	
	private String assetPath;
	

	private String partnerStatus;

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getPublishedTimeInUTCString() {
		if (publishedTime != null) {
			return DateInString.getUtcString(publishedTime);
		} else {
			return "";
		}
	}

	public void setTagNamesString(String tagNamesString) {
		this.tagNamesString = tagNamesString;
		if (StringUtils.hasText(tagNamesString)) {
			this.tagNames = new ArrayList<>(Arrays.asList(tagNamesString.trim().split(",")));
		} else {
			this.tagNames = new ArrayList<>();
		}

	}

	public String getDisplayName() {
		if (StringUtils.hasText(displayName)) {
			return displayName;
		} else {
			return emailId;
		}

	}
}
