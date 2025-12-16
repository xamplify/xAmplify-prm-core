package com.xtremand.integration.dao;

import java.util.List;

import com.xtremand.integration.bom.Integration;
import com.xtremand.integration.bom.Integration.IntegrationType;
import com.xtremand.integration.dto.IntegrationSettingsDTO;
import com.xtremand.lead.bom.Pipeline;
import com.xtremand.lead.bom.PipelineStage;

public interface IntegrationDao {

	public Integration getUserIntegrationDetails(Integer companyId, IntegrationType type);

	public Integration getActiveCRMIntegration(Integer companyId);

	public Integer getTotalIntegrationsCount(Integer companyId);

	public void updateIntegrationTypeOnPipelines(Integer companyId, IntegrationType type);

	public List<Integration> getAllIntegrationsByType(IntegrationType type);


	public void setCRMPipelinesForExistingCampaigns(IntegrationType activeIntegrationType, Pipeline leadPipeline,
			Pipeline dealPipeline, Integer companyId, PipelineStage leadStage, PipelineStage dealStage);

	public void setDealPipelinesForExistingCampaigns(Pipeline pipeline, Integer companyId);

	public void setLeadPipelinesForExistingCampaigns(Pipeline pipeline, Integer companyId);

	public void setOtherDealPipelineForExistingCampaigns(List<Integer> removedIds, Pipeline pipeline, Integer companyId,
			PipelineStage dealStage);

	public void setNewPipelinesForExistingCampaigns(List<Integer> existingLeadPipeineIds,
			List<Integer> existingDealPipeineIds, Pipeline leadPipeline, Pipeline dealPipeline, Integer companyId,
			PipelineStage leadStage, PipelineStage dealStage);

	public void setCRMLeadPipelineForExistingCampaignsAndLeads(IntegrationType activeIntegrationType,
			Pipeline leadPipeline, Integer companyId, PipelineStage leadStage, Long halopsaTicketTypeId);

	public void setCRMDealPipelineForExistingCampaignsAndDeals(IntegrationType activeIntegrationType,
			Pipeline dealPipeline, Integer companyId, PipelineStage dealStage, Long ticketTypeId);

	public void setNewLeadPipelineForExistingCampaignsAndLeads(List<Integer> existingLeadPipeineIds,
			Pipeline leadPipeline, Integer companyId, PipelineStage leadStage, Long ticketTypeId);

	public void setNewDealPipelineForExistingCampaignsAndDeals(List<Integer> existingDealPipeineIds,
			Pipeline dealPipeline, Integer companyId, PipelineStage dealStage, Long ticketTypeId);

	/******** XNFR-344 *****/
	public List<Integration> getAllActiveIntegrationsByType(IntegrationType type);

	public boolean isMultipleCRMsActivatedByCompanyId(Integer companyId);

	public List<IntegrationSettingsDTO> getVendorRegisterDealList(Integer partnerCompanyId, Integer vendorCompanyId);

	public String getActiveIntegrationTypeByCompanyId(Integer companyId);
	
	public IntegrationSettingsDTO isSelfDealByVendor(Integer companyId);

	public boolean hasActiveCRMIntegration(Integer companyId);
	
	String fetchActiveIntegrationPAT(Integer companyId);

}
