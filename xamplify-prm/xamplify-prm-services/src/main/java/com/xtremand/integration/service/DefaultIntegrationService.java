package com.xtremand.integration.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.json.simple.parser.ParseException;

import com.xtremand.deal.bom.Deal;
import com.xtremand.form.dto.FormDTO;
import com.xtremand.formbeans.ExternalEmailTemplateDTO;
import com.xtremand.integration.bom.ExternalContactDTO;
import com.xtremand.integration.bom.ExternalContactListDTO;
import com.xtremand.integration.bom.ExternalOpportunityDTO;
import com.xtremand.integration.bom.Integration;
import com.xtremand.lead.bom.Lead;
import com.xtremand.lead.bom.OpportunityType;
import com.xtremand.lead.bom.Pipeline;
import com.xtremand.salesforce.dto.OpportunityFormFieldsDTO;
import com.xtremand.user.bom.User;

public interface DefaultIntegrationService {

	public boolean authorizeUser(User user);

	public String getRedirectionUrl(Integer userId);

	public List<ExternalContactDTO> getContacts(Long listId, User user) throws IOException, ParseException;

	public List<ExternalContactListDTO> getContactLists(User user) throws IOException, ParseException;

	public List<ExternalEmailTemplateDTO> getTemplates(User user, Boolean fillContent)
			throws IOException, ParseException;

	public ExternalEmailTemplateDTO getTemplateById(User user, Long id) throws IOException, ParseException;

	public void changeDealStatus(User user, Map<String, Object> leadCustomObjectDetails,
			ExternalOpportunityDTO opportunity) throws IOException, ParseException;

	public void pushLead(User user, Lead lead);

	public void pushDeal(User user, Deal deal);

	public List<OpportunityFormFieldsDTO> getExternalCustomFields(User user, OpportunityType opportunityType)
			throws IOException, ParseException;

	public void syncCustomForm(User loggedInUser, List<String> fields) throws IOException, ParseException;

	public void syncPipeline(User loggedInUser, Pipeline pipeline) throws IOException, ParseException;

	public FormDTO getCustomForm(Integration activeCRMIntegration, Integer dealId, User loggedInUser,
			Long halopsaTicketTypeId, OpportunityType opportunityType) throws IOException, ParseException;

	public void synchronizeLeads(User loggedInUser) throws IOException, ParseException;

	public void syncPipelines(User loggedInUser) throws IOException, ParseException;

	public void syncCustomFormV2(User loggedInUser, List<OpportunityFormFieldsDTO> fields,
			OpportunityType opportunityType) throws ParseException, IOException;
}
