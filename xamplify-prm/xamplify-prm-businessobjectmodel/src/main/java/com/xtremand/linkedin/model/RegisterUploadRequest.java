package com.xtremand.linkedin.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
@Data
@AllArgsConstructor
public class RegisterUploadRequest {
	List<String> recipes;
	private String owner;
	ServiceRelationship[] serviceRelationships;
}
