package com.xtremand.landing.page.analytics.dto;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.landing.page.analytics.bom.GeoLocationAnalyticsEnum;

import lombok.Data;

@Data
@JsonInclude(value=Include.NON_EMPTY)
public class GeoLocationAnalyticsDTO implements Serializable {/**
	 * 
	 */
	private static final long serialVersionUID = -4119014886074958007L;
	
	
	private Integer id;
	
	private String landingPageAlias;
	
	private String deviceType;
	
	private String os;
	
	private String city;
	
	private String state;
	
	private String zip;
	
	private String country;
	
	private String isp;
	
	private String ipAddress;
	
	private String latitude;
	
	private String longitude;
	
	private String countryCode;
	
	private String timezone;
	
	private Date openedTime;
	
	private String openedTimeInString;
	
	private Integer campaignId;
	
	private Integer userId;
	
	private Integer formId;
	
	private Integer landingPageId;
	
	private String url;
	
	private GeoLocationAnalyticsEnum analyticsType;
	
	private String partnerLandingPageAlias;
	
	private Integer partnerCompanyId;
	
	private String partnerEmailId;
	
	private boolean openedInBrowser;
	
	private Integer formSubmitId;
	
	private UserDTO user;
	
	private boolean vendorJourney;
	
	private boolean fromMasterLandingPage;
	
	private boolean partnerJourneyPage;
	private boolean fromVendoeMarketplacePage;


}
