package com.xtremand.integration.dao;

import com.xtremand.integration.bom.CallIntegration;
import com.xtremand.integration.bom.CallIntegrationTypeEnum;
import com.xtremand.integration.dto.CallIntegrationDTO;

public interface CallIntegrationDAO {
	
	CallIntegration getUserIntegrationDetails(Integer companyId, CallIntegrationTypeEnum type);
	
	CallIntegrationDTO getAuthenticationResources(Integer companyId, CallIntegrationTypeEnum type);
	
	Integer getTotalIntegrationsCount(Integer companyId);
	
	CallIntegrationDTO getUserOrganizationDetails(Integer userId, Integer companyId, CallIntegrationTypeEnum type);
	
	Integer removeCall(Integer id);
	
	CallIntegrationDTO getActiveCallDetails(Integer companyId);

}
