package com.xtremand.dam.dto;

import java.io.Serializable;
import java.util.Date;

import com.xtremand.landing.page.analytics.dto.GeoLocationAnalyticsDTO;

import lombok.Data;

@Data
public class SharedAssetDetailsViewDTO implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = 2458814302308128403L;

	private Integer id;

	private String assetName;

	private String description;

	private String alias;

	private Date publishedTime;

	private String publishedTimeInUTCString;

	private String thumbnailPath;

	private boolean beeTemplate;

	private Integer damId;

	private Integer videoId;

	// XNFR-833
	private boolean partnerSignatureRequired;

	private Integer loggedInUserId;

	private String sharedAssetPath;

	private boolean partnerSignatureCompleted;

	private String selectedSignaturePath;

	private GeoLocationAnalyticsDTO geoLocationDetails;

	private boolean vendorSignatureCompleted;

	private Integer damPartnerId;

	private String categoryName;

	private String displayName;

	private String vendorCompanyName;

	private String loggedInUserProfileImage;

	private String assetType;

	private String assetPath;

	private String threadId;

	private String vectorStoreId;

	private boolean imageFileType;

	private boolean textFileType;

	private boolean contentPreviewType;

	private String jsonBody;

}
