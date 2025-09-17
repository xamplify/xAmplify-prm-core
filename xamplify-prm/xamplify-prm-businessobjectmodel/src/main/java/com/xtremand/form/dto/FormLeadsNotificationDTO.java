package com.xtremand.form.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.xtremand.form.submit.bom.FormSubmitEnum;

import lombok.Data;

@Data
public class FormLeadsNotificationDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7943222998609916785L;

	
	private String emailId;
	
	private String firstName;
	
	private Integer formId;
	
	private String formName;
	
	private Integer landingPageId;
	
	private String landingPageName;
	
	private String campaignName;
	
	private boolean formLead;
	
	private boolean landingPageFormLead;
	
	private boolean partnerLandingPageFormLead;
	
	private boolean campaignLandingPageFormLead;
	
	private boolean eventCampaignFormLead;
	
	
	private FormSubmitEnum formSubitEnum;
	
	private String formAnalyticsUrl;
	
	private String partnerCompanyName;
	
	private boolean vanityUrlFilter;

	
	private List<FormDataForLeadNotification> submittedDetails = new ArrayList<>();
	
	
	
	
}
