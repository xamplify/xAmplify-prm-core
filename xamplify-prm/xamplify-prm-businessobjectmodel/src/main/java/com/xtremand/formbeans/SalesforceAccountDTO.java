package com.xtremand.formbeans;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class SalesforceAccountDTO {
	
	private String id;
	private String name;
	private String subType;
	private String territory;
	private String companyDomain;
	private String owner;
	private String website;	
	private String region;
	private String country;

}
