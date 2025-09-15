package com.xtremand.dam.dto;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class DamPartnerCompanyUsersAnalyticsDTO implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = 2573351492531159223L;


	private Integer damId;

	private Integer damPartnerId;

	private Integer userId;

	@JsonIgnore
	private Integer partnerId;

	private Integer partnershipId;

	private String emailId;

	private String firstName;

	private String lastName;

	private String fullName;

	private String companyName;

	private Integer viewCount;

	private Integer downloadCount;
	
	private String assetName;

	private String assetType;
	
	private Date publishedOn;

	private String createdBy;
}
