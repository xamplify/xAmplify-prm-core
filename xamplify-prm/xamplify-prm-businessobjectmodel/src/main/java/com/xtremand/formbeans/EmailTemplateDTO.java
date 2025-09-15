package com.xtremand.formbeans;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.xtremand.mail.bom.EmailTemplateSource;
import com.xtremand.mail.bom.EmailTemplateType;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class EmailTemplateDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8960449497279754679L;

	private Integer id;

	private String name;

	private String body;

	private Integer userId;

	private Integer companyId;

	private boolean userDefined;

	private String jsonBody;

	private boolean regularTemplate;

	private boolean videoTemplate;

	private boolean beeRegularTemplate;

	private boolean beeVideoTemplate;

	private boolean defaultTemplate;

	private String subject;

	private String description;

	private String createdDate;

	private String updatedDate;

	private String createdBy;

	private Integer langId;

	private EmailTemplateType type;

	private EmailTemplateSource source;

	private boolean regularCoBrandingTemplate;

	private boolean videoCoBrandingTemplate;

	private String companyName;

	private boolean campaignDefault;

	private String emailTemplateType;

	private String vendorName;

	private String vendorOrganizationName;

	private boolean draft;

	private boolean beeEventTemplate;

	private boolean beeEventCoBrandingTemplate;

	private boolean marketoTemplate;

	private String spamScore;

	private Integer categoryId;

	private String category;

	private boolean editPartnerTemplate;

	private String vendorCompanyLogoPath;

	private String partnerCompanyLogoPath;

	private Integer vendorCompanyId;

	private boolean surveyTemplate;

	private boolean surveyCoBrandingTemplate;

	private boolean whiteLabeledEmailTemplateReceivedFromVendor;

	private boolean whiteLabeledEmailTemplateSharedWithPartners;

	private String whiteLabeledEmailTemplateSharedByVendorCompanyName;

	private String companyLogoPath;

	/******** XNFR-330 ********/
	private boolean autoResponseEmailTemplate;

	private Integer selectedAutoResponseCustomEmailTemplateId;

	private Integer autoResponseId;

	private String autoResponseType;
	
	/******** XNFR-330 ********/
	
	@Getter
	@Setter
	private String cdnSubject;

	/******** XNFR-993 ********/
	private boolean isLearningTrackExist;
	
	private Integer learningTrackId;
	
	private Integer workflowId;


}