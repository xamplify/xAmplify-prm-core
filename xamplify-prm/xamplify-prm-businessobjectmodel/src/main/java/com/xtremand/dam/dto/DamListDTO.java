package com.xtremand.dam.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.dam.bom.DamStatusEnum;
import com.xtremand.formbeans.VideoFileDTO;
import com.xtremand.util.dto.DisplayUserDetailsDTO;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;

@Data
@EqualsAndHashCode(callSuper = false)
public class DamListDTO extends DisplayUserDetailsDTO {
	/**
	* 
	*/
	private static final long serialVersionUID = -1738732794321485770L;

	private Integer id;

	private String assetName;

	private String assetType;

	private String assetPath;

	private String thumbnailPath;

	private String alias;

	private String pageSize;

	private String pageOrientation;

	private boolean beeTemplate;

	private String createdDateInUTCString;

	private Date createdTime;

	private boolean published;

	private boolean history;

	private boolean expand;

	private Date updatedTime;

	private String status;

	private boolean showPreviewIcon;

	private Integer parentId;

	private boolean finished;

	private String description;

	private boolean opened;

	@JsonIgnore
	private Integer templateVersion;

	private boolean edit;

	private boolean delete;

	private Integer createdBy;

	private String tagNamesString;

	private List<String> tagNames;

	private Integer childParentId;

	private CompanyProfile companyProfile;

	private DamStatusEnum damStatusEnum;

	private String htmlBody;

	private Date imageGeneratedOn;

	private boolean imageGeneratedSuccessfully;

	private String jsonBody;

	private Date publishedTime;

	private Integer version;

	private Integer updatedBy;

	private Set<Integer> tagIds;

	private String viewBy;

	private Integer totalViews;

	private Integer videoId;

	private String videoLength;

	private boolean isProcessed;

	private VideoFileDTO videoFileDTO;

	@Setter(value = AccessLevel.NONE)
	private String versionInString;

	private String companyName;

	private String categoryName;

	private Integer categoryId;

	/******* XNFR-255 *******/
	private boolean whiteLabeledAssetReceivedFromVendor;

	private boolean whiteLabeledAssetSharedWithPartners;

	private String whiteLabeledAssetSharedByVendorCompanyName;

	private boolean publishingOrWhiteLabelingInProgress;

	private boolean publishingToPartnerList;
	/******* XNFR-255 *******/

	private Integer learningTrackContentMappingId;
	
	/** XNFR-781 start **/
	private boolean addedToQuickLinks;

	private String approvalStatus;

	private Integer approvalStatusUpdatedBy;

	private Date approvalStatusUpdatedTimeInString;
	
	private boolean draft;

	private boolean createdByAnyApprovalManagerOrApprover;
	/** XNFR-781 end **/
	
	private boolean partnerSignatureRequired;

	private boolean vendorSignatureRequired;

	private String partnerSignatureToolTipMessage;

	private boolean disablePartnerSignatureOption;

	private String vendorSignatureToolTipMessage;

	private boolean disableVendorSignatuerOption;

	private boolean vendorSignatureCompleted;
	
	private Integer approvalReferenceId;
	
	private boolean hasAnyReApprovalVersion;
	
	private String parentAssetName;
	
	private boolean imageFileType;
	
    private boolean textFileType;
	
	private boolean contentPreviewType;
	
	private String assetProxyPath;

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public void setVersionInString(String versionInString) {
		if (templateVersion != null) {
			this.versionInString = this.templateVersion + ".0";
		} else {
			this.versionInString = versionInString != null ? "" : "-";
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
	
	private Date displayTime;
	
	private String proxyUrlForOliver;
	
	private boolean disableAccessForOliver;

	private String slug;
	
	private String openAIFileId;
	
    private String cdnAssetPath;
	
	private String cdnThumbnailPath;
	
	private boolean publishedAsset;

}
