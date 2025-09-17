package com.xtremand.util.dto;

import java.util.Date;
import java.util.List;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class TeamMemberAnalyticsRequestDTO {

    private Integer loggedInUserId;
	
	private Integer partnerCompanyId;
	
	private Integer teamMemberUserId;
	
	private Integer vendorCompanyId;
	
	private String trackTypeFilter;
	
	private List<Integer> selectedVendorCompanyIds;
	
	private List<Integer> selectedTeamMemberIds;
	
	private boolean vanityUrlFilter;
	
	private String vendorCompanyProfileName;
	
	private Date fromDateFilter;

	private Date toDateFilter;

	private String fromDateFilterInString;

	private String toDateFilterInString;

	private String timeZone;
	
	private String assetType;

	private String searchKey;

	private String campaignTypeFilter;

	private String lmsType;
	
	private boolean vendorVersion;
	
	private String page;

	private String size;

	@Getter(value = AccessLevel.NONE)
	private Integer pageNumber;

	@Getter(value = AccessLevel.NONE)
	private Integer limit;

	public Integer getPageNumber() {
		pageNumber = XamplifyUtility.convertStringToInteger(page);
		return pageNumber;
	}

	public Integer getLimit() {
		limit = XamplifyUtility.convertStringToInteger(size);
		return limit;
	}

}
