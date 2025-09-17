package com.xtremand.linkedin.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class UploadMechanism {
	@JsonProperty("com.linkedin.digitalmedia.uploading.MediaUploadHttpRequest")
	MediaUploadHttpRequest mediaUploadHttpRequest;
}
