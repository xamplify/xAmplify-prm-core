package com.xtremand.vendor.journey.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class VendorLogoDTO implements Serializable {

	private static final long serialVersionUID = 8648320812235657379L;

	private Integer companyId;

	private String companyName;

	private String companyLogo;

	private boolean selected;

	private Integer landingPageId;

	private String firstName;

	private String lastName;

	private String emailId;

	private Integer partnerId;

	private Integer vendorJourneyId;

	private List<Integer> categoryIds;

	private Integer partnershipId;

	private Integer partnerJourneyPageId;

	private Integer partnerCompanyId;

	private Integer vendorCompanyId;

	private String fullAddress;

	private String phone;

	private String website;

	private String alias;

	private boolean imageReplaceRequired;

}
