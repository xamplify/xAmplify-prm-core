package com.xtremand.dam.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.xtremand.landing.page.analytics.dto.GeoLocationAnalyticsDTO;
import com.xtremand.white.labeled.dto.DamVideoDTO;

import lombok.Data;

@Data
public class DamUploadPostDTO implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = 9218316822680009088L;

	private Integer id;

	private Integer loggedInUserId;

	private String assetName;

	private String description;

	private String thumbnailPath;

	private Set<Integer> tagIds;

	private String downloadLink;

	private String oauthToken;

	private String fileName;

	private boolean cloudContent;

	private String source;

	private String assetPath;

	private Integer categoryId;

	/***** XNFR-255 ****/
	private boolean shareAsWhiteLabeledAsset;

	private Set<Integer> partnerGroupIds;

	private boolean partnerGroupSelected;

	private Set<Integer> partnerIds;

	private List<Integer> updatedPartnerIds;

	private DamVideoDTO damVideoDTO;

	private String companyName;

	private Integer damId;

	private boolean assetSharedEmailNotification;

	private Integer videoId;

	private boolean newlyAddedPartnerFromPartnerList;

	private List<Integer> publishedPartnerUserIds = new ArrayList<>();

	/***** XNFR-255 ****/

	/**** XNFR-342 *****/
	private boolean publishingToPartnersInsidePartnerList;

	/*** XNFR-434 ***/
	private boolean replaceAsset;

	private boolean replaceVideoAsset;

	private String videoUri;

	/*** XNFR-434 ***/

	private boolean addedToQuickLinks;

	/*** XNFR-833 ***/

	private String completeAssetFileName;

	private String copiedAssetFilePath;

	private String sharedAssetPath;

	private boolean partnerSignatureRequired;

	private boolean vendorSignatureRequired;

	private String selectedSignatureImagePath;

	private boolean vendorSignatureCompleted;

	private GeoLocationAnalyticsDTO geoLocationDetails;

	private Map<Integer, LinkedList<Integer>> damPartnerIds;

	private boolean draft;

	private String approvalStatusInString;

	private boolean sendForReApproval;

	private Integer approvalReferenceId;

	private List<String> tags;

	private boolean beeTemplate;

	private String htmlBody;

	private String jsonBody;

	private boolean saveAs;

	private boolean vendorSignatureRequiredAfterPartnerSignature;

	private boolean sendForApproval;

	private Integer approvalStatusUpdatedby;

	private boolean updateApprovalStatus;

	/**** XNFR-955 *****/
	private String slug;

	private Integer companyId;

	private String companyProfileName;

}
