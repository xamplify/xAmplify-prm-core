package com.xtremand.linkedin.model;

import lombok.Data;

@Data
public class Identifier{
	private String identifier;
	private String file;
	private Integer index;
	private String mediaType;
	private String identifierType;
	private Long identifierExpiresInSeconds;
}
