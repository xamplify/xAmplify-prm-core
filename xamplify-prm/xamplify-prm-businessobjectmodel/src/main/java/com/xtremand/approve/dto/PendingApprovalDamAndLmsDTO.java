package com.xtremand.approve.dto;

import java.util.Date;

import com.xtremand.util.bom.DateUtils;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
public class PendingApprovalDamAndLmsDTO {

	private Integer id;

	private DateUtils dateUtils = new DateUtils();

	private String name;

	private String type;

	private String createdBy;

	private String folder;

	private String assetType;

	private String status;

	private Integer createdById;

	private boolean beeTemplate;

	private Integer videoId;

	private String slug;

	private String alias;

	private Integer createdByCompanyId;

	private Date createdTime;

	private boolean createdByAnyApprovalManagerOrApprover;

	private boolean published;

	private String createdDateInUTC;

	private String viewBy;

	private String videoAlias;

	private boolean canEdit;

	private boolean canDelete;

	private boolean canPublish;

	private boolean canUnPublish;

	private String templateVersion;
	
	private String moduleType;
	
	private boolean hasVisibility;
	
	private boolean hasDamContent = true;
	
	private Integer approvalReferenceId;
	
	private boolean hasAnyReApprovalVersion;
	
	private String parentAssetName;
	
	private boolean sendForReApproval;
	
	private boolean sendForApproval;
	
	private boolean approvalReminder;
	
	private Integer companyId;
	
	private String assetPath;
	
	private boolean imageFileType;
	
    private boolean textFileType;
	
	private boolean contentPreviewType;
	
	private String assetProxyPath;
	
	private boolean partnerSignatureRequired;

	private boolean vendorSignatureRequired;
	
	
	@Setter(value = AccessLevel.NONE)
	private String versionInString;

	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
		if (createdTime != null) {
			setCreatedDateInUTC(dateUtils.getUTCString(createdTime));
		}
	}

	public void setVersionInString(String versionInString) {
		if (templateVersion != null) {
			this.versionInString = this.templateVersion + ".0";
		} else {
			this.versionInString = versionInString != null ? "" : "-";
		}

	}
	
	private String expireDate;
}
