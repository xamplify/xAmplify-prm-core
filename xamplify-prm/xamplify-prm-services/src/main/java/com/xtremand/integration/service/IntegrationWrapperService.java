package com.xtremand.integration.service;

import java.io.IOException;
import java.util.Map;

import org.json.simple.parser.ParseException;

import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.integration.dto.CustomFormRequestDto;
import com.xtremand.integration.dto.IntegrationDTO;
import com.xtremand.lead.bom.OpportunityType;
import com.xtremand.lead.bom.PipelineType;

public interface IntegrationWrapperService {

	public XtremandResponse getActiveCRMDetails(Integer loggedInUserId, Integer createdForCompanyId)
			throws IOException, ParseException;


	public XtremandResponse getActiveCRMDetails(Integer loggedInUserId) throws IOException, ParseException;


	public XtremandResponse getIntegrationUserDetails(String type, Integer userId);

	public XtremandResponse unlinkCRM(String type, Integer userId);



	public XtremandResponse getActiveCRMPipelines(Integer companyId, Integer loggedInUserId, Integer campaignId,
			PipelineType type, Long typeId);

	public XtremandResponse getDealPipelinesForView(Integer dealId, Integer loggedInUserId);

	public XtremandResponse getLeadPipelinesForView(Integer leadId, Integer loggedInUserId);

	public XtremandResponse updateCRMSettings(Integer userId, String integrationType, IntegrationDTO integrationDto);

	public Map<String, Object> getVendorRegisterDealMap(Integer partnerCompanyId, Integer vendorCompanyId);

	public XtremandResponse getVendorRegisterDealValue(Integer partnerUserId, String vendorCompanyProfile);

	public XtremandResponse updateCRMSettingsNew(Integer userId, String integrationType, IntegrationDTO integrationDto);

	public XtremandResponse createCustomDependentDropdowns();

	public XtremandResponse getCustomChoices(String parentLabelId, String parentChoice);

	XtremandResponse getActiveCrmType(Integer userId);

	public boolean isSalesforceCrmActiveByCompanyId(Integer vendorCompanyId);

	XtremandResponse getCustomFormLablesValues(CustomFormRequestDto customFormRequestDto);

	XtremandResponse getFormLablesChoices(Integer formLabelId);

	/*** XNFR-887 *****/
	XtremandResponse getActiveIntegrationTypeByCompanyName(String companyProfileName);
	/*** XNFR-887 *****/


	public XtremandResponse getActiveCRMCustomForm(Integer companyId, Integer opportunityId, Integer loggedInUserId,
			OpportunityType opportunityType);

}
