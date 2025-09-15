package com.xtremand.dam.dto;

import java.io.Serializable;

import com.xtremand.landing.page.analytics.dto.GeoLocationAnalyticsDTO;

import lombok.Data;

@Data
public class DamAnalyticsPostDTO   implements Serializable {/**
	 * 
	 */
	private static final long serialVersionUID = 991684168676351912L;
	
	
	private Integer damPartnerId;
	
	private Integer loggedInUserId;
	
	private Integer actionType;
	
	private GeoLocationAnalyticsDTO geoLocationDetails;
	
	
	

}
