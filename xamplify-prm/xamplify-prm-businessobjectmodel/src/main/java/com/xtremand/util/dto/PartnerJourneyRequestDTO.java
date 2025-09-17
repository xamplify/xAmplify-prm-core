package com.xtremand.util.dto;

import java.util.Date;
import java.util.List;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class PartnerJourneyRequestDTO {

	private Integer loggedInUserId;

	private Integer partnerCompanyId;

	private Integer teamMemberUserId;

	private Integer vendorCompanyId;

	private String trackTypeFilter;

	private List<Integer> selectedPartnerCompanyIds;

	private boolean detailedAnalytics;

	private boolean partnerTeamMemberGroupFilter;

	private String assetType;

	private String searchKey;

	private String campaignTypeFilter;

	private String lmsType;

	private Date fromDateFilter;

	private Date toDateFilter;

	private String fromDateFilterInString;

	private String toDateFilterInString;
	
	private String filterFromDateString;
	
	private String filterToDateString;
	
	private Date filterFromDate;
	
	private Date filterToDate;

	private String timeZone;

	private String filterType;

	private String page;

	private String size;
	
	private String sortcolumn;
	
	private String sortingOrder;
	
	private String moduleName;
	
	private String partnershipStatus;
	
	private List<String> assetNames;
	private List<Integer> assetIds;
	private List<Integer> companyIds;
	private List<String> emailIds;	
	private List<String> playbookNames;	
	
	
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
