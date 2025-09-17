package com.xtremand.linkedin.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class EmailAddressHandle {
	@JsonProperty("handle~")
	private Handle handle;
}
