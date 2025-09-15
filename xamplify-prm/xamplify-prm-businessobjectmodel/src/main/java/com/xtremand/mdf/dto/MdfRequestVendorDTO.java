package com.xtremand.mdf.dto;

import java.math.BigInteger;
import java.util.Date;

import lombok.Data;

@Data
public class MdfRequestVendorDTO {
	
	private Integer partnershipId;
	
	private Integer companyId;
	
	private String companyName;
	
	private String companyProfileName;
	
	private String companyLogoPath;
	
	private BigInteger requestsCount;
	
	private Date createdOn;

}
