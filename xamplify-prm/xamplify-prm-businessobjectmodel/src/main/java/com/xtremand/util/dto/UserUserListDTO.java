package com.xtremand.util.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.util.StringUtils;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class UserUserListDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7533690499796454147L;

	private Integer userId;

	private Integer userListId;

	private Integer userUserListId;
	
	private boolean companyContactList;

	private String country;

	private String city;

	private String address;

	private Integer contactCompanyId;

	private String contactCompany;

	private String jobTitle;

	private String firstName;

	private String lastName;

	private String mobileNumber;

	private String state;

	private String zip;

	private String vertical;

	private String region;

	private String partnerType;

	private String category;

	private String legalBasisString;
	
	private String emailId;
	
	private String companyDomain;
	
	private String website;
	
	private String countryCode;

	private Integer contactStatusId;

	// XNFR-427
	private Integer companyId;
	private String description;

	@Getter(value = AccessLevel.NONE)
	private List<Integer> legalBasis;
	
	private String customFields;

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public List<Integer> getLegalBasis() {
		if (StringUtils.hasText(legalBasisString)) {
			List<String> legalBasisArray = Arrays.asList(legalBasisString.split(","));
			List<Integer> legalBasisIds = new ArrayList<>(legalBasisArray.size());
			for (String role : legalBasisArray) {
				legalBasisIds.add(Integer.valueOf(role));
			}
			return legalBasisIds;
		} else {
			return new ArrayList<>();
		}

	}

}
