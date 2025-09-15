package com.xtremand.util.dto;

import java.math.BigInteger;
import java.util.Date;

import com.xtremand.util.bom.DateUtils;

import lombok.Data;

@Data
public class PartnerJourneyTrackDetailsDTO {
	private Integer id;
	private String title;
	private String createdByEmail;
	private String status;
	private Integer progress;
	private String assetName;
	private String assetType;
	private Date publishedOn;
	private String publishedOnInUTC;
	private BigInteger openedCount;
	private BigInteger viewedCount;
	private BigInteger downloadedCount;
	private BigInteger assetCount;
	private BigInteger quizCount;
	private BigInteger notOpenedCount;
	private BigInteger submittedCount;
	private String score;	
	private String emailId;
	private Date assetCreatedTime;
	private String assetCreatedTimeInUTC;
	private DateUtils dateUtils = new DateUtils();
	private String fullName;
	private String firstName;
	private String lastName;
	private String mobileNumber;
	private String companyName;
	private Integer companyId;
	private String partnerEmailId;
	private String vendorCompany;
	private String vendorTeamMemberEmailId;
	private String createdByName;
	private String folderName;
	private String region;
	private Integer partnersCount;
	private Date onboardedOn;
	private Date dateLastLogin;
	private Integer partnerId;
	private Integer damId;
	private String password;
	private boolean vanityUrlDomain;
	private String playbookName;
	private BigInteger completedCount;
	private Integer partnerCompanyId;
	private Integer assetId;
	private Date viewedTime;
	private String viewedOnInUTC;
	private Date downloadedTime;
	private String downloadedOnInUTC;
	private String partnerName;
	private String unsubscribed;
	//private Integer teamMemberUserId;
	
	
	private String description;
	private boolean published;
	private String createdTime;
	private String updatedTime;
	private String approvalStatus;
	private String expireDate;
	private boolean plublished;
	private boolean addedToQuickLinks;
	private String createdByEmailId;
	private String createdByMobileNumber;
	private boolean whiteLabledPlaybookSharedWithParnters;
	private String publishedStatus;
	private String openAIFileId;
	
	
	private String partnerCompanyName;
	private String partnerFullName;
	public void setAssetCreatedTime(Date assetCreatedTime) {
		this.assetCreatedTime = assetCreatedTime;
		if (assetCreatedTime != null) {
			setAssetCreatedTimeInUTC(dateUtils.getUTCString(assetCreatedTime));
		}
	}
	public void setViewedTime(Date viewedTime) {
		this.viewedTime = viewedTime;
		if (viewedTime != null) {
			setViewedOnInUTC(dateUtils.getUTCString(viewedTime));
		}
	}
	public void setDownloadedTime(Date downloadedTime) {
		this.downloadedTime = downloadedTime;
		if (downloadedTime != null) {
			setDownloadedOnInUTC(dateUtils.getUTCString(downloadedTime));
		}
	}

}
