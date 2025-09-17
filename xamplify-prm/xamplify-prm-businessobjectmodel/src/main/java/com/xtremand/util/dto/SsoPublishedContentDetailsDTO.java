package com.xtremand.util.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class SsoPublishedContentDetailsDTO {
	
	private String firstName;

	private List<String> titles = new ArrayList<>();

	private String partnerEmailAddress;

	private String bodyPrefixText;

	private boolean partnerOnboardedThroughSSO;
	
	private String publishedContentType;
	
	private Integer count;
}
