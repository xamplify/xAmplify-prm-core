package com.xtremand.dam.dto;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class DamAnalyticsDTO implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = 8482674643087079687L;

	private Integer damId;

	private Integer damPartnerId;

	private Integer userId;

	private Integer partnerId;

	private Integer partnershipId;

	private String emailId;

	private String firstName;

	private String lastName;

	private String fullName;

	private String contactCompany;

	private Integer viewCount;

	private Integer downloadCount;
	
	private Integer companyId;
	
	private String companyLogo;
	
	private String companyName;
	
	private String damPartnerAlias;
	
	private Integer partnerDamId;

	private boolean partnerSignatureCompleted;
	
	private Integer partnerUserId;
	
	private String sharedAssetPath;
	
	private boolean vendorSignatureCompleted;
	
	private String assetProxyPath;

	private String partnerStatus;
	
	private String assetName;
	
	private String assetType;
	
	private String createdBy;
	
	private String categoryName;
	
	private Date publishedOn;
	
	private String partnerName;

}
