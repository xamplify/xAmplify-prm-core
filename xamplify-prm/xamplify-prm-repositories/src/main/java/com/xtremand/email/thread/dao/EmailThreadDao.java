package com.xtremand.email.thread.dao;

import java.util.List;

import com.xtremand.integration.bom.MailIntegration;
import com.xtremand.integration.bom.MailIntegrationTypeEnum;
import com.xtremand.integration.dto.CallIntegrationDTO;

public interface EmailThreadDao {

	List<CallIntegrationDTO> getAccessToken(Integer userId);

	MailIntegration getUserIntegrationDetails(Integer userId, MailIntegrationTypeEnum gmail);

	Integer getTotalIntegrationsCount(Integer loggedInUser);

	void deactivateAllMailIntegrations(Integer userId);

	CallIntegrationDTO getAuthenticationResources(Integer userId, MailIntegrationTypeEnum gmail);
}
