package com.xtremand.linkedin.model;

import lombok.Data;

@Data
public class Share {
	private String author;
	private String lifecycleState;
	private SpecificContent specificContent;
	private MemberNetworkVisibility visibility;
	
	public Share(String author, String lifecycleState, SpecificContent specificContent,
			MemberNetworkVisibility visibility, boolean page) {
		super();
		if (page) {
			this.author = "urn:li:organization:"+author;
		} else {
			this.author = "urn:li:person:"+author;
		}
		
		this.lifecycleState = lifecycleState;
		this.specificContent = specificContent;
		this.visibility = visibility;
	}
}