package com.xtremand.util.dto;

import java.io.Serializable;

import org.springframework.util.StringUtils;

import com.xtremand.common.bom.Criteria;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class Pageable implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = -1521755976413914241L;

	private String page;

	private String size;

	@Getter(value = AccessLevel.NONE)
	private String sort;

	@Getter(value = AccessLevel.NONE)
	private String search;

	@Getter(value = AccessLevel.NONE)
	private Integer pageNumber;

	@Getter(value = AccessLevel.NONE)
	private Integer limit;

	private boolean filterPartners;

	private String filterBy;
	
	/**XNFR-735**/
	private String vendorCompanyProfileName;

	private String fromDateFilterString;
	
	private String toDateFilterString;
	
	private String timeZone;
	
	/***** XNFR-758 *****/
	private Integer loginAsUserId;
	
	/***** XNFR-820 *****/
	private String filterKey;
	
	/***** XNFR-837 *****/
	private Criteria[] criterias;
	
	/**XNFR-867**/
	private String campaignType;

	private String partnerSignatureType;
	private String sortcolumn;
	public String getSortcolumn() {
		return sortcolumn;
	}

	public void setSortcolumn(String sortcolumn) {
		this.sortcolumn = sortcolumn;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getSort() {
		if (sort != null && StringUtils.hasText(sort)) {
			return sort.trim();
		} else {
			return sort;
		}

	}

	public String getSearch() {
		if (search != null && StringUtils.hasText(search)) {
			return search.trim();
		} else {
			return search;
		}
	}

	public Integer getPageNumber() {
		pageNumber = XamplifyUtility.convertStringToInteger(page);
		return pageNumber;
	}

	public Integer getLimit() {
		limit = XamplifyUtility.convertStringToInteger(size);
		return limit;
	}
	
	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}
	
	public Criteria[] getCriterias() {
		return criterias;
	}

	public void setCriterias(Criteria[] criterias) {
		this.criterias = criterias;
	}

	public String getPartnerSignatureType() {
		if (partnerSignatureType != null && StringUtils.hasText(partnerSignatureType)) {
			return partnerSignatureType.trim();
		} else {
			return search;
		}
	}
	
}
