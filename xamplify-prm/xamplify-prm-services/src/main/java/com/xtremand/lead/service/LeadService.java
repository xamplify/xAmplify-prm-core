package com.xtremand.lead.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.xtremand.activity.dto.ContactOpportunityRequestDTO;
import com.xtremand.campaign.bom.ModuleAccess;
import com.xtremand.common.bom.Pagination;
import com.xtremand.form.emailtemplate.dto.SendTestEmailDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.integration.bom.Integration;
import com.xtremand.integration.bom.Integration.IntegrationType;
import com.xtremand.lead.bom.Lead;
import com.xtremand.lead.dto.LeadCountsResponseDTO;
import com.xtremand.lead.dto.LeadCustomFieldDto;
import com.xtremand.lead.dto.LeadDto;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;

public interface LeadService {

	public XtremandResponse saveLead(LeadDto leadDto);

	public XtremandResponse updateLead(LeadDto leadDto);

	public Map<String, Object> getLeadsForPartner(Pagination pagination);

	public Map<String, Object> getLeadsForVendor(Pagination pagination);

	public XtremandResponse getLead(Integer loggedInUserId, Integer leadId);

	public XtremandResponse deleteLead(LeadDto leadDto);

	public XtremandResponse getVendorList(Integer loggedInUserId);

	public XtremandResponse getCreatedForCompanyId(Integer campaignId, Integer loggedInUserId);

	public XtremandResponse getLeadCounts(VanityUrlDetailsDTO vanityUrlDetails);

	public XtremandResponse getCompanyIdByCompanyProfileName(String companyProfileName, Integer loggedInUserId);

	public XtremandResponse getViewType(VanityUrlDetailsDTO vanityUrlDetails);

	LeadCountsResponseDTO vendorLeadsCount(Integer userId, boolean applyFilter);

	public XtremandResponse getChat(Integer leadId, Integer userId);


	public void downloadLeads(HttpServletResponse httpServletResponse, String userType, String type, Integer userId,
			String filename, boolean vanityUrlFilter, String vendorCompanyProfileName, String searchKey,
			String fromDate, String toDate, boolean partnerTeamMemberGroupFilter, String timeZone, String stageName);


	public List<String> getStageNamesForVendor(Integer loggedInUserId);

	public List<String> getStageNamesForPartner(Integer loggedInUserId);

	public List<String> getStageNamesForPartnerInCampaign(Integer loggedInUserId);

	public List<String> getStageNamesForPartner(VanityUrlDetailsDTO vanityUrlDetails);

	public XtremandResponse getVendorListForLoginAsUserId(Integer loggedInUserId, Integer loginAsUserId);

	public XtremandResponse downloadLeads(Integer userId);

	public List<LeadDto> getLeadsForCSV(Pagination pagination);

	public LinkedHashMap<String, String> getFieldHeaderMapping(String userType, Integer userId, boolean vanityUrlFilter, String vendorCompanyProfileName);

	/**** XNFR-426 ****/
	public XtremandResponse updateLeadApprovalStatus(LeadDto leadDto);


	/*** XNFR-505 ***/
	public Map<String, Object> getLeadsForLeadAttachment(Pagination pagination);

	public XtremandResponse findRegisteredByCompanies(Integer loggedInUserId);

	public XtremandResponse findRegisteredByUsers(Integer loggedInUserId);

	public XtremandResponse findRegisteredByCompaniesForPartnerView(Integer loggedInUserId);

	public XtremandResponse findRegisteredByUsersForPartnerView(Integer loggedInUserId);


	public XtremandResponse findRegisteredByUsersByPartnerCompanyId(Integer partnerCompanyId, Integer campaignId);

	public XtremandResponse getCustomLeadFields(Integer companyId);
	
	/*** XNFR-592 ***/
	public XtremandResponse saveOrupdateCustomLeadFields(Integer loggedInUserId, List<LeadCustomFieldDto> leadFieldsDto);

	public XtremandResponse getCustomLeadFieldsByVendorCompanyId(Integer vendorCompanyId);
	
	/*** XNFR-615 ***/
	public void updateSfCustomFieldsData(Lead lead, JSONObject leadJson, Integer activeCRMCompanyId,
			IntegrationType activeCRMIntegrationType, Integration otherActiveCRMIntegration);

	public XtremandResponse findLeadAndLeadInfoAndComments(Integer leadId);

	/** XNFR-649 **/
	Map<String, Object> queryLeadsForVendor(Pagination pagination);

	Map<String, Object> queryLeadsForPartner(Pagination pagination);

	public XtremandResponse checkIfHasAcessForAddLeadOrDeal(VanityUrlDetailsDTO vanityUrlDetailsDTO);

	public XtremandResponse checkIfHasOpporunityAcess(VanityUrlDetailsDTO vanityUrlDetailsDTO);
	
	/**XNFR-553**/
	XtremandResponse findLeadsAndCountByContactId(ContactOpportunityRequestDTO contactOpportunityRequestDTO);

	public XtremandResponse findSendReminderTemplateForLead(Integer loggedInUserId, String companyProfileName,
			String emailId);

	public XtremandResponse sendReminderNotificationForLead(SendTestEmailDTO sendTestEmailDTO);

	public ModuleAccess findCompanyAccess(Integer companyId, String companyProfileName);

}
