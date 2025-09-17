package com.xtremand.form.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.xtremand.form.bom.FormSubTypeEnum;
import com.xtremand.form.bom.FormTypeEnum;
import com.xtremand.integration.dto.ConnectWiseProductDTO;
import com.xtremand.landing.page.analytics.bom.GeoLocationAnalyticsEnum;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FormDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2047320886630427023L;

	private Integer id;

	private String name;

	private String description;

	private String alias;

	private String backgroundColor;

	private String labelColor;

	private String buttonValue;

	private String buttonValueColor;

	private String buttonColor;

	private String formSubmitMessage;

	private String backgroundImage;

	private String companyLogo;

	private boolean showCompanyLogo;

	private String footer;

	private boolean showFooter;

	private String titleColor;

	private String borderColor;

	private String pageBackgroundColor;

	private boolean showBackgroundImage;

	private boolean showCaptcha;

	private boolean quizForm;

	private Integer createdBy;

	private Integer updatedBy;

	private String createdName;

	private String createdDateString;

	private String updatedDateString;

	private String companyName;

	private int count;

	private boolean dataShare;

	private boolean campaignForm;

	private boolean landingPageForm;

	private boolean partnerLandingPageForm;

	private GeoLocationAnalyticsEnum analyticsType;

	private FormTypeEnum formType;

	private Integer landingPageId;

	private Integer campaignId;

	private Integer userId;

	private Integer partnerCompanyId;

	private Integer categoryId;

	private String categoryName;

	private String embedUrl;

	private String ailasUrl;

	private String userName;

	private boolean createdByAdmin;

	private boolean openLinkInNewTab;

	private String formSubmissionUrl;

	private boolean saveAs;

	private String thumbnailImage;

	private boolean saveAsDefaultForm;

	private FormSubTypeEnum formSubType;

	private Integer formSubmitId;

	private String emailId;

	private boolean disableEmail;

	private Set<Integer> selectedTeamMemberIds;

	private Set<Integer> selectedGroupIds;

	private boolean showTitleHeader = Boolean.TRUE;

	private String descriptionColor;

	private boolean selected = false;

	private boolean associatedWithTrack = false;

	/******* XNFR-255 *******/
	private boolean whiteLabeledFormReceivedFromVendor;

	private boolean whiteLabeledFormSharedWithPartners;

	private String whiteLabeledFormSharedByVendorCompanyName;
	/******* XNFR-255 *******/

	private boolean vanityUrlFilter = false;

	private String vendorCompanyProfileName;

	private String customSkinTextColor;

	private String customSkinBackgroundColor;

	private String customSkinDivBackgroundColor;

	private String customSkinButtonBorderColor;

	// XNFR-403
	private List<ConnectWiseProductDTO> connectWiseProducts = new ArrayList<>();

	private List<String> countryNames;

	/******* XNFR-424 *******/
	boolean hasEmailField = false;

	private List<FormLabelDTORow> formLabelDTORows = new ArrayList<>();

	private List<FormLabelDTO> formLabelDTOs = new ArrayList<>();

	int columnOrder = 1;

	/******* XNFR-424 ENDS *******/

	private boolean showConnectWiseProducts;

	private boolean creatingMdfForm;
	
	//XNFR-611
	
	private String dealFormHeader;

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	//XNFR-712
	private boolean vendorJourney;
	
	private boolean partnerJourneyPage;

	private String cdnThumbnailImage;

	private String companyProfileName;

}
