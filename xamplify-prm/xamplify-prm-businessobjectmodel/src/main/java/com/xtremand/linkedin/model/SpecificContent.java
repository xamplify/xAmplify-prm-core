package com.xtremand.linkedin.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class SpecificContent {
	@JsonProperty("com.linkedin.ugc.ShareContent")
	private ShareContent shareContent;
}
