package com.xtremand.util.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class CompanyDetailsDTO implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = 7395025522980450595L;

	private String companyName;

	private String companyProfileName;

	private String companyType;

	private Integer companyId;

	@JsonIgnore
	private String emailId;

	@JsonIgnore
	private Integer userId;

	private String companyStatus;

	private String companyUrl;

	private String companyAboutUs;

	private String vanityUrl;

}
