package com.xtremand.util.dto;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

import lombok.Data;

@Data
public class CompanyDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -981267382192222904L;

	private Integer id;

	private String companyName;
	
	private String itemName;

	private String companyProfileName;

	private String companyLogo;

	private BigInteger count;

	private boolean spfConfigured;

	private boolean domainConnected;

	private String name;

	private Integer viewCount;

	private String partnerStatus;
	
	private String trackName;
	
	private String createdBy;
	
	private Date publishedOn;
	
	private String emailId;
	
	private String partnerName;
	
	private Integer progress;
	
	private String score;
	
	private String contactCompany;

	private String formName;
	
	private Date submittedOn;
}
