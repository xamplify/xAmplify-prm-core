package com.xtremand.linkedin.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ProfilePicture {
	private String displayImage;
	
	@JsonProperty("displayImage~")
	private DisplayImageObject displayImageObject;
}
