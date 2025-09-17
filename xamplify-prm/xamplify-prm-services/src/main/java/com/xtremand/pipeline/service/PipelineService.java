package com.xtremand.pipeline.service;

import java.util.Map;

import com.xtremand.common.bom.Pagination;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.integration.bom.Integration.IntegrationType;
import com.xtremand.lead.bom.PipelineType;
import com.xtremand.lead.dto.PipelineDto;
import com.xtremand.lead.dto.PipelineRequestDTO;

public interface PipelineService {

	public XtremandResponse getPipeLines(Integer loggedInUserId, Integer companyId, PipelineType type);

	public XtremandResponse savePipeline(PipelineDto pipelineDto);

	public XtremandResponse updatePipeline(PipelineDto pipelineDto);

	public XtremandResponse deletePipeline(PipelineDto pipelineDto);

	public XtremandResponse getPipeLine(Integer loggedInUserId, Integer pipelineId);

	public Map<String, Object> getPipeLinesForVendor(Pagination pagination);

	public XtremandResponse getPipelinesByIntegrationType(Integer loggedInUserId, Integer companyId, PipelineType type,
			IntegrationType integrationType, Long typeId);

	public XtremandResponse getPipelinesByIntegrationType(Integer loggedInUserId, PipelineType type,
			IntegrationType integrationType, Long typeId);

	public XtremandResponse getPipelinesForCompanyByIntegrationType(Integer loggedInUserId, Integer companyId,
			PipelineType type, IntegrationType integrationType);


	public XtremandResponse findLeadPipeLines(PipelineRequestDTO pipelineRequestDTO);

	public XtremandResponse findPipelineStages(Integer pipelineId, Integer loggedInUserId);

	public XtremandResponse findLeadPipelinesForPartner(PipelineRequestDTO pipelineRequestDTO);

	/*** Added By Sravan For Fixing Performance Issues ******/
	public XtremandResponse findLeadAndDealPipeLinesToCreateCampaign(Integer loggedInUserId);
	
	public XtremandResponse findPipelinesForCRMSettings(PipelineRequestDTO pipelineRequestDTO);

	
}
