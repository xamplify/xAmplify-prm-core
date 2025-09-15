package com.xtremand.company.dto;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.xtremand.company.bom.CompanySource;
import com.xtremand.flexi.fields.dto.FlexiFieldResponseDTO;
import com.xtremand.util.dto.DateInString;

import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class CompanyDTO implements Serializable{
	
	private static final long serialVersionUID = 129013717480861426L;

	private Integer id;
	private Long externalId;
	private String name;
	private String address;
	private String city;
	private String state;
	private String country;
	private String zip;
	private String phone;
	private String fax;
	private String email;
	private String website;
	private String linkedinURL;
	private String facebookURL;
	private String twitterURL;
	private CompanySource source = CompanySource.XAMPLIFY;
	private Integer companyId;
	private Integer userId;
	private BigInteger contactCount;
	private BigInteger contactCounts;
	private BigInteger companyCount;
	private String companyListName;
	private Date createdTime;
	private String createdTimeInUTC;
	private Integer createdByUserId;
	private Integer companyUserListId;
	private boolean publicCompanyList;
	private boolean canUpdate;
	private boolean canDelete;
	private boolean canViewContacts;
	private String websiteProfilePath;
	private List<FlexiFieldResponseDTO> customFields;
	private String countryCode;
	
	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
		if (createdTime != null) {
			setCreatedTimeInUTC(DateInString.getUtcString(createdTime));
		}
	}
	 
}
