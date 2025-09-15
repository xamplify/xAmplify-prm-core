package com.xtremand.dashboard.analytics.views.dto;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class InstantNavigationLinksDTO implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = -6386999830262373004L;

	private Integer id;

	private Integer damPartnerId;

	private String title;

	private Date date;

	private String type;

	private String slug;
	
	private boolean published;
	
	private boolean beeTemplate;
	
	private String assetType;
	
	private Integer videoId;
	

}
