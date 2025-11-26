package com.xtremand.pipeline.dao;

import java.util.List;
import java.util.Map;

import com.xtremand.common.bom.Pagination;
import com.xtremand.integration.bom.Integration.IntegrationType;
import com.xtremand.lead.bom.Pipeline;
import com.xtremand.lead.bom.PipelineStage;
import com.xtremand.lead.bom.PipelineType;
import com.xtremand.lead.dto.PipelineRequestDTO;
import com.xtremand.lead.dto.PipelineResponseDTO;
import com.xtremand.lead.dto.PipelineStageResponseDTO;

public interface PipelineDAO {

	public List<Pipeline> getPipeLines(Integer companyId, PipelineType type, Boolean isPrivate);

	/*** Added By Sravan ***/
	public List<PipelineResponseDTO> findPipeLinesByCompanyIdAndPipeLineType(Integer companyId, PipelineType type,
			boolean isPrivate);

	public Pipeline getPipeLineByName(Integer companyId, String name, PipelineType type);

	public void clearDisplayIndex(Integer pipelineId);

	public Map<String, Object> getPipeLinesForVendor(Pagination pagination);

	public Pipeline getSalesforcePipeline(Integer companyId, PipelineType type);

	public Pipeline getPipeLineByCompanyIdAndName(Integer companyId, String name);

	public Pipeline getDefaultPipeLine(Integer companyId, PipelineType type);

	public List<Pipeline> getPipelinesByIntegrationType(Integer companyId, PipelineType type,
			IntegrationType integrationType, Boolean isPrivate);

	/*** Added By Sravan ***/
	public List<PipelineResponseDTO> findPipelinesByCompanyIdAndPipeLineTypeAndIntegrationType(Integer companyId,
			PipelineType type, String integrationType);

	public List<Pipeline> getPipelinesByIntegrationType(Integer companyId, IntegrationType integrationType);

	public Pipeline getDealPipelineByExternalPipelineId(Integer companyId, String externalPipelineId,
			IntegrationType integrationType);

	public PipelineStage getPipelineStageByExternalPipelineStageId(Integer companyId, Integer pipelineId,
			String externalPipelineStageId);

	public PipelineStage getDefaultStage(Integer pipelineId);

	public Integer getPublicPipelinesCount(Integer companyId, IntegrationType integrationType, PipelineType type);

	public boolean hasPartnerCreatedDealsOnPipeline(Integer pipelineId, Integer vendorCompanyId);

	public boolean hasPartnerCreatedDealsOnPipelineStage(Integer pipelineStageId, Integer vendorCompanyId);

	public boolean hasPartnerCreatedLeadsOnPipeline(Integer pipelineId, Integer vendorCompanyId);

	public boolean hasPartnerCreatedLeadsOnPipelineStage(Integer pipelineStageId, Integer vendorCompanyId);

	public List<Pipeline> getPipelineByExternalPipelineIdForHaloPSA(Integer companyId, PipelineType type,
			IntegrationType integrationType, Boolean isPrivate, List<String> externalPipelineIds);

	public String getActiveCRM(PipelineRequestDTO pipelineRequestDTO);

	public List<PipelineResponseDTO> findLeadPipeLinesByActiveCRM(PipelineRequestDTO pipelineRequestDTO,
			String activeCRM, String externalPipelineId);

	public List<PipelineStageResponseDTO> findPipelineStagesByPipelineId(Integer pipelineId, boolean isPrivateStage);

	public PipelineResponseDTO findLeadPipelinesByCampaignId(Integer campaignId);
	
	public String getTicketTypeIdFromCampaignByCampaignId(Integer campaignId);
	
	public List<PipelineResponseDTO> findPipelineForCRMSettings(PipelineRequestDTO pipelineRequestDTO, Integer companyId);

	public Pipeline getLeadPipelineByExternalPipelineId(Integer id, String externalPipelineId,
			IntegrationType integrationType);

}
