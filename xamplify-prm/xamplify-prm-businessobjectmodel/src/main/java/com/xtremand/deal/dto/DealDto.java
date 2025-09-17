package com.xtremand.deal.dto;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.lead.dto.LeadDto;
import com.xtremand.lead.dto.PipelineDto;
import com.xtremand.salesforce.bom.SfCustomFieldsData;
import com.xtremand.salesforce.dto.SfCustomFieldsDataDTO;
import com.xtremand.util.bom.DateUtils;

import lombok.Data;

@Data
@JsonInclude(value = Include.NON_NULL)
public class DealDto {

	private Integer id;
	private String title;
	private String dealType;
	private Double amount;
	private String closeDateString;
	private Date closeDate;
	private String closeDateUTC;
	private LeadDto associatedLead;
	private Integer campaignId;
	private String campaignName;
	private Integer parentCampaignId;
	private String parentCampaignName;
	private Integer associatedLeadId;
	private Integer createdByCompanyId;
	private Integer createdForCompanyId;
	private String createdForCompanyName;
	private Integer forCompanyUserId;
	private Integer pipelineId;
	private Integer pipelineStageId;
	private PipelineDto pipeline;
	private Integer userId;
	private String createdByName;
	private String createdByEmail;
	private String createdByCompanyName;
	private boolean pushToMarketo;
	private boolean pushToHubspot;
	private boolean pushToSalesforce;
	private String currentStageName;
	private Integer currentStageId;
	private boolean isCurrentStagePrivate;
	private boolean canUpdate = false;
	private boolean canDelete = false;
	private String description;
	private String createdTime;
	private String createdDateString;
	private String campaignType;
	private String stage;
	private String probability;
	private String nextStep;
	private String leadSource;
	private UserDTO associatedContact;
	private List<SfCustomFieldsDataDTO> sfCustomFieldsDataDto;
	private BigInteger unReadChatCount;
	private BigInteger unReadPropertyChatCount;
	private boolean isOnNonInteractiveStage = false;

	// XNFR-426
	private String dealComment;

	// XNFR-316
	private Date createdDate;
	private String createdDateInUTC;
	private DateUtils dateUtils = new DateUtils();
	private String createdByMobileNumber;

	// XNFR-403
	private String forecastItemsJson;

	// XNFR-461

	private Integer createdByPipelineId;
	private Integer createdByPipelineStageId;
	private Integer createdForPipelineId;
	private Integer createdForPipelineStageId;

	private String haloPSATickettypeId;

	// XNFR-575
	private String referenceId;
	private boolean showDealActions;
	private boolean showEditDealStage;

	private String crmId;
	private List<SfCustomFieldsData> sfCustomFieldsData;
	private boolean activeCRM;
	private Integer associatedContactId;

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
		if (createdDate != null) {
			setCreatedDateInUTC(dateUtils.getUTCString(createdDate));
		}
	}

	private String partnerCompanyLogoPath;
	private String leadName;
	private String leadCompany;
	private String createdForcompanyProfileName;
	private String website;
	private String partnerStatus;

	private String createdForPipeline;
	private String createdByPipeline;
	private String createdForPipelineStage;
	private String createdByPipelineStage;
	private String customFields;
	private String contactName;
	private String contactEmailId;
	private String accountSubType;
	private String partnerType;
}
