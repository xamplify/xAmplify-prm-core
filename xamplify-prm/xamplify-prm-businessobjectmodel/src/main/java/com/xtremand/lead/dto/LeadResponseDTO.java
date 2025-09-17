package com.xtremand.lead.dto;
import java.math.BigInteger;

import lombok.Data;


@Data
public class LeadResponseDTO {
	/*** XNFR-649 ***/
	private Integer id;
	private String firstName;
	private String lastName;
	private String company;
	private String email;	
	private String phone;
	private Integer campaignId;
	private String campaignName;	
	private Integer parentCampaignId;
	private String parentCampaignName;
	private Integer createdByCompanyId;
	private Integer createdForCompanyId;
	private String createdForCompanyName;
	private String createdByName;
	private String createdByEmail;
	private String createdByCompanyName;
	private String currentStageName;
	private boolean canUpdate = false;
	private boolean canRegisterDeal = false;
	private String createdTime;
	private Integer associatedDealId;
	private BigInteger unReadChatCount;
	private boolean selfLead;
	private boolean showRegisterDeal;
	private String referenceId;
	private String leadApprovalStatusType;
	private boolean leadApprovalOrRejection;
	private boolean enableRegisterDealButton = false;
	private boolean isAssociatedCampaignDeleted = false;
	private boolean dealBySelfLead = false;
	private boolean dealByPartner = false;
	private boolean dealByVendor = false;
	private boolean partnerDeleteLead = false;
	private boolean partnerEditLead = false;
	
}
