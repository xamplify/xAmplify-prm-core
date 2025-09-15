package com.xtremand.dashboard.analytics.views.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class UniversalSearchDTO implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = -6386999830262373004L;

	private Integer id;
	
	private String name;
	
	private String slug;
	
	private String tag;
	
	private String stage;
	
	private String createdBy;
	
	private String from;
	
	private String type;
	
	private Integer damPartnerId;
	
	private boolean beeTemplate;
	
	private Integer videoId;
	
	private String folder;
	
	private boolean expand;
	
	private String referenceId;
	
	private String campaignName;
	
	private String companyName;
	
	private String addedFor;
	
	private String activeIntegrationId;
	
	private String navigate;
	
	private Integer createdByCompanyId;
	

}
