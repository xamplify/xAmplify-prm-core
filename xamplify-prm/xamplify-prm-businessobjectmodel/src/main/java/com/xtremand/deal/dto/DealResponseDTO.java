package com.xtremand.deal.dto;

import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(value=Include.NON_NULL)
public class DealResponseDTO {
	/** XNFR-650 **/
	private Integer id;
	private String title;
	private String referenceId;
	private String createdForCompanyName;
	private String createdByCompanyName;
	private String campaignName;
	private Integer campaignId;
	private Integer parentCampaignId;
	private String parentCampaignName;
	private String createdByName;
	private String createdByEmail;
	private String createdTime;
	private String currentStageName;
	private boolean canUpdate = false;
	private BigInteger unReadChatCount;
	private BigInteger unReadPropertyChatCount;
	private boolean isOnNonInteractiveStage = false;
	private Integer pipelineStageId;
	private boolean showDealActions;
	private boolean showEditDealStage;
	private Integer pipelineId;

}
