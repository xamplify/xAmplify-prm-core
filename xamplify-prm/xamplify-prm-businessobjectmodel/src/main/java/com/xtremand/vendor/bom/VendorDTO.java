package com.xtremand.vendor.bom;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class VendorDTO {

	private Integer id;
	private Integer companyId;
	private String companyName;
	private String companyNameC;
	private String companyLogo;
	private Integer campaignsCount;

	private String vendorFirstName;
	private String vendorLastName;
	private String vendorEmailId;
	private String vendorProfileImage;
	private String vendorPhone;

	private String partnerFirstName;
	private String partnerLastName;
	private String partnerEmailId;
	private String partnerProfileImage;
	private String partnerPhone;

	private Integer vendorCompanyId;
	private String vendorAdminFirstName;
	private String vendorAdminLastName;
	private String vendorAdminEmailId;
	private String vendorAdminProfileImage;
	private String vendorAdminPhone;

	private String partnerAdminFirstName;
	private String partnerAdminLastName;
	private String partnerAdminEmailId;
	private String partnerAdminProfileImage;
	private String partnerAdminPhone;

	private Integer tracksCount;
	private Integer playbooksCount;
	private Integer vendorCompanyIdA;
	private Integer vendorCompanyIdC;
	private Integer vendorCompanyIdP;
	private Integer assetsCount;
	private Integer pagesCount;

	private Long publishedFeedCount;
	private String vendorDisplayName;
	private String partnerDisplayName;
	private String vendorAdminDisplayName;
	private String partnerAdminDisplayName;
	private List<MyVendorCountsDTO> counts;
	private Integer distinctCompanyId;

}
