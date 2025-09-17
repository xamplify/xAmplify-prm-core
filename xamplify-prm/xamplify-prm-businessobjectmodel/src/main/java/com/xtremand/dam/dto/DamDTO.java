package com.xtremand.dam.dto;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.dam.bom.DamTag;

import lombok.Data;

@Data
public class DamDTO {

	private Integer id;

	private String assetName;

	private String description;

	private String status;

	private CompanyProfile companyProfile;

	private String assetPath;

	private String thumbnailPath;

	private String assetType;

	private boolean beeTemplate;

	private Integer parentId;

	private Integer childParentId;

	private Date publishedTime;

	private Date createdTime;

	private Integer createdBy;

	private Date updatedTime;

	private Integer updatedBy;

	private String pageSize;

	private String pageOrientation;

	private Integer version;

	private Date imageGeneratedOn;

	private boolean imageGeneratedSuccessfully;

	private Set<DamTag> tags = new HashSet<>();

	private Integer videoFileId;

	/****** XNFR-255 ***********/
	private boolean sharedWithPartnersAsAWhiteLabeledAsset;

	private boolean shareWhiteLabelContentAccess;
	
	private String companyName;
	/****** XNFR-255 ***********/
	
	private Integer companyId;
	
	private String folder;
	
	private String fullName;
	
	private Date publishedOn;
}
