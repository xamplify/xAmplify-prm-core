package com.xtremand.linkedin.model;

public class TextShare extends Share {

	public TextShare(String author, String lifecycleState, SpecificContent specificContent, MemberNetworkVisibility visibility, boolean page) {
		super(author, lifecycleState, specificContent, visibility, page);
		this.setLifecycleState("PUBLISHED");
	}

}
