package com.xtremand.approve.dto;

import java.io.Serializable;
import java.util.Set;

import lombok.Data;

@Data
public class ContentReApprovalDTO implements Serializable{
		
	private static final long serialVersionUID = -8833043961959962355L;
	
	private Integer id;
    
	private String assetName;
    
	private String assetPath;
    
	private String thumbnailPath;
    
	private String description;
    
	private String assetType;
    
	private String jsonBody;
    
	private String htmlBody;
    
	private Boolean published;
   
	private String assetStatus;
    
	private Integer videoId;
    
	private String approvalStatus;
	
	private boolean addedToQuickLinks;
	
	private Integer loggedInUserId;
	
	private String alias;
	
	private boolean beeTemplate;
	
	private Integer parentId;
	
	private Integer childParentId;
	
	private String createdTime;
	
	private Integer createdBy;
	
	private boolean partnerSignatureRequired;
	
	private boolean vendorSignatureRequired;
	
	private boolean vendorSignatureRequiredAfterPartnerSignature;
	
	private Integer approvalReferenceId;
	
	private Set<Integer> tagIds;

	private Integer categoryId;
	
	private boolean whiteLabeledAssetSharedWithPartners;
	
	private String pageSize;
	
	private String pageOrientation;
	
	private boolean imageGeneratedSuccessfully;
	
	private Integer companyId;
	
}
