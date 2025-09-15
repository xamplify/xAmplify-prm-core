package com.xtremand.linkedin.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class MemberNetworkVisibility {
	@JsonProperty("com.linkedin.ugc.MemberNetworkVisibility")
	private String visibility = "PUBLIC";
}
