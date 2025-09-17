package com.xtremand.lead.dto;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import com.xtremand.lead.bom.LeadApprovalStatusEnum;
import com.xtremand.salesforce.bom.SfCustomFieldsData;
import com.xtremand.salesforce.dto.SfCustomFieldsDataDTO;
import com.xtremand.util.bom.DateUtils;

import lombok.Data;

@Data
public class LeadDto {
	private Integer id;
	private String firstName;
	private String lastName;
	private String company;
	private String email;	
	private String phone;
	private String website;
	private String street;
	private String city;
	private String state;
	private String country;
	private String postalCode;
	private Integer campaignId;
	private String campaignName;	
	private Integer parentCampaignId;
	private String parentCampaignName;
	private Integer associatedUserId;
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
	private boolean canRegisterDeal = false;
	private String createdTime;
	private String createdDateString;
	private Integer associatedDealId;
	private BigInteger unReadChatCount;
	//XNFR-316
	private String fullName;
	private Date createdDate;
	private String createdDateInUTC;
	private DateUtils dateUtils = new DateUtils();
	private String createdByMobileNumber;
	
	
	/******XNFR-426***/
	private boolean leadApprovalOrRejection;
	private LeadApprovalStatusEnum leadApprovalStatusType;
	private String approvalStatusComment;
	private String approvalStatusUpdatedTime;
	private String leadComment;
	private boolean isSelfLead;
	
	/*** XNFR-505 ***/
	private boolean enableRegisterDealButton = false;
	private boolean isAssociatedCampaignDeleted = false;
	
	// XNFR-521

		private Integer createdByPipelineId;
		private Integer createdByPipelineStageId;
		private Integer createdForPipelineId;
		private Integer createdForPipelineStageId;
		
	private String halopsaTicketTypeId;
	private boolean dealByPartner;
	private boolean dealByVendor;
	private boolean dealBySelfLead;
	
	/**XNFR-693**/
	private boolean partnerEditLead;
	private boolean partnerDeleteLead;
	
	private String createdForPipeline;
	private String createdByPipeline;
	private String createdForPipelineStage;
	private String createdByPipelineStage;
	
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
		if (createdDate != null) {			
			setCreatedDateInUTC(dateUtils.getUTCString(createdDate));
		}
	}
	private String industry;
	private String title;
	//XNFR-613
	private String region;
	//XNFR-615
	private List<SfCustomFieldsDataDTO> sfCustomFieldsDataDto;
	private String referenceId;
	private String crmId;
	private String sfcfDealRegStatusForVersa;
	private List<SfCustomFieldsData> sfCustomFieldsData;
	private String sfcfDealRegStatusNameForVersa;
	//XNFR-766
	private Integer formSubmitId;
	private boolean activeCRM;
	private String partnerCompanyLogoPath;
	private String partnerStatus;
	private String contactName;
	private String contactEmailId;
	private String customFields;
	private String accountSubType;
	private String partnerType;
}
