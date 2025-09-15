package com.xtremand.form.submit.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.xtremand.form.bom.FormTypeEnum;
import com.xtremand.landing.page.analytics.dto.GeoLocationAnalyticsDTO;

import lombok.Data;

@Data
public class FormSubmitDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Integer id;
	
	private String alias;
	
	private List<FormSubmitFieldsDTO> fields = new ArrayList<>();
	
	private FormTypeEnum formType;	
	
	private Integer userId;
	
	private Integer learningTrackId;
	
	private GeoLocationAnalyticsDTO geoLocationAnalyticsDTO;
	
	private boolean vendorJourney;
	
	private boolean masterLandingPage;
	
	private Integer partnerMasterLandingPageId;
	
	private String masterLandingPageName;
	
	private String partnerCompanyName;
	
	private String partnerMailId;

	private Integer vendorLandingPageId;
	
	private String vendorLandingPageName;
	
	private String vendorCompanyName;
	
	private String vendorMailId;
	
	private String vendorFormAlias;
	
	private String searchKey;
	
	private boolean vanityUrlFilter;
	
	
	private boolean partnerJourneyPage;
	
	private boolean vendorMarketplacePage;
	
	private Integer vendorMarketplacePageId;
	
	private String vendorMarketplacePageName;
	
	private Integer vendorCompanyId;
	
	private Integer partnerCompanyId;


}
