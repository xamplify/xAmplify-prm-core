package com.xtremand.linkedin.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Media {
	private String status;
	private String originalUrl;
	private Description description;
	private Title title;
	private String media;
	private Thumbnails thumbnails[];
}
