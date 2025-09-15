package com.xtremand.linkedin.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class LinkedInElement {
	
	@JsonProperty("roleAssignee")
	private String roleAssignee;
	
	@JsonProperty("state")
	private String state;
	
	@JsonProperty("role")
	private String role;
	
	@JsonProperty("organization")
	private String organization;
	
	@JsonProperty("localizedName")
	private String name;
	
	@JsonProperty("id")
	private String organizationId;
	
	@JsonProperty("original")
	private String logoV2Original;

}
