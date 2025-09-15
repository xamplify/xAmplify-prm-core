package com.xtremand.linkedin.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ServiceRelationship {
	private String relationshipType;
	private String identifier;
}
