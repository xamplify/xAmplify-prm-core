package com.xtremand.dam.dto;

import java.io.Serializable;
import java.util.Date;

import com.xtremand.util.dto.UserDetailsUtilDTO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class DamPartnerDetailsDTO extends UserDetailsUtilDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6373480611379169154L;
	
	private Integer id;
	
	private Integer partnershipId;
	
	private Integer partnerId;
	
	private String contactCompany;
	
	private Date partnershipCreatedOn;

	private String assetName;

	private Integer damPartnerGroupId;

}
