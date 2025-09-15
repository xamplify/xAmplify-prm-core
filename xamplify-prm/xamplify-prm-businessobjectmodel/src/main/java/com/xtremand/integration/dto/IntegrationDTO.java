package com.xtremand.integration.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.xtremand.integration.bom.FormColumnLayoutTypeEnum;
import com.xtremand.integration.bom.Integration.IntegrationType;
import com.xtremand.lead.dto.PipelineDto;

import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class IntegrationDTO {
	
	private Integer userId;
	private String instanceUrl;
	private String webApiInstanceUrl;
	private String apiKey;
	private IntegrationType type;
	private Boolean activeCRM = false;
	private String externalEmail;
	private String externalUserId;
	private String externalDisplayName;
	private String externalUserName;
	private String externalOrganizationId;
	private String externalThumbnail;
	private boolean enableUnlink = false;
	private Integer defaultDealPipelineId;
	/* XNFR-215 */
	private String externalOrganizationName;
	/* XNFR-403 */
	private String clientId;
	private String publicKey;
	private String privateKey;
	private String clientSecret;
	
	/* XNFR-461 */
	private boolean createdForActiveCRM = false;
	private boolean createdByActiveCRM = false;
	private IntegrationType createdForActiveCRMType;
	private IntegrationType createdByActiveCRMType;
	private boolean hasCustomForm = false;
	private List<PipelineDto> createdForCompanyPipelines = new ArrayList<>();
	private List<PipelineDto> createdByCompanyPipelines = new ArrayList<>();
	private boolean showCreatedByPipelineAndStage = false;
	private boolean showCreatedByPipelineAndStageOnTop = false;
	private boolean showHaloPSAOpportunityTypesDropdown = false;
	private boolean showCreatedByLeadPipelineAndStage = false;
	private boolean showCreatedByLeadPipelineAndStageOnTop = false;
	
	/* XNFR-615 */
	private String leadDescription;
	private String dealDescription;
	private boolean showLeadPipeline = true;
	private boolean showLeadPipelineStage = true;
	private boolean showDealPipeline = true;
	private boolean showDealPipelineStage = true;
	private Integer leadPipelineId;
	private Integer leadPipelineStageId;
	private Integer dealPipelineId;
	private Integer dealPipelineStageId;
	private boolean isDealByPartnerEnabled = true;
	private boolean isDealByVendorEnabled;
	private boolean isDealBySelfLeadEnabled = true;
	private FormColumnLayoutTypeEnum leadFormColumnLayout = FormColumnLayoutTypeEnum.TWO_COLUMN_LAYOUT;
	private FormColumnLayoutTypeEnum dealFormColumnLayout = FormColumnLayoutTypeEnum.TWO_COLUMN_LAYOUT;
	private boolean isCreatedForSelfCompany;
	// XNFR-681
	private String leadTitle;
	private String dealTitle;
	
	/**XNFR-693**/
	private boolean partnerEditLead = true;
	private boolean partnerDeleteLead = true;
}
