package com.xtremand.linkedin.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
public class ShareContent {
	private ShareCommentary shareCommentary;
	private String shareMediaCategory;
	
	@JsonInclude(Include.NON_NULL)
	private List<Media> media;
	
	public ShareContent(ShareCommentary shareCommentary, String shareMediaCategory) {
		super();
		this.shareCommentary = shareCommentary;
		this.shareMediaCategory = shareMediaCategory;
	}

	public ShareContent(ShareCommentary shareCommentary, String shareMediaCategory, List<Media> media) {
		super();
		this.shareCommentary = shareCommentary;
		this.shareMediaCategory = shareMediaCategory;
		this.media = media;
	}
}
