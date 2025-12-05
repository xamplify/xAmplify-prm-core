package com.xtremand.deal.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.xtremand.activity.dto.ContactOpportunityRequestDTO;
import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.Pagination;
import com.xtremand.deal.bom.Deal;
import com.xtremand.deal.dto.DealCountsResponseDTO;
import com.xtremand.deal.dto.DealDto;
import com.xtremand.deal.dto.VendorSelfDealRequestDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.lead.bom.Lead;
import com.xtremand.lead.bom.Pipeline;
import com.xtremand.util.dto.Pageable;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;

public interface DealService {

	public XtremandResponse saveDeal(DealDto dealDto);

	public XtremandResponse updateDeal(DealDto dealDto);

	public Map<String, Object> getDealsForPartner(Pagination pagination);

	public Map<String, Object> getDealsForVendor(Pagination pagination);

	public XtremandResponse getDeal(Integer loggedInUserId, Integer dealId);

	public XtremandResponse deleteDeal(DealDto dealDto);

	public XtremandResponse changeDealStatus(DealDto dealDto);

	public XtremandResponse getDealCounts(VanityUrlDetailsDTO vanityUrlDetails);

	public Deal createDealFromSfOpportunity(JSONObject opportunityInSf, Lead lead, Integer loggedInUserId,
			Integer loggedInCompanyId);

	public Deal updateDealFromSfOpportunity(Deal deal, JSONObject opportunityInSf, Lead lead, Integer loggedInUserId,
			Integer loggedInCompanyId);

	public Pipeline createDefaultDealPipeline(CompanyProfile company, Integer userId);

	public Pipeline createDefaultLeadPipeline(CompanyProfile company, Integer userId);


	DealCountsResponseDTO getVendorDealsCount(Integer userId, boolean applyFilter);

	public XtremandResponse getChatByProperty(Integer propertyId, Integer loggedInUserId);

	public XtremandResponse getChat(Integer dealId, Integer loggedInUserId);

	public void downloadDeals(HttpServletResponse httpServletResponse, String userType, String type, Integer userId,
			String filename, boolean vanityUrlFilter, String vendorCompanyProfileName, String searchKey,
			String fromDate, String toDate, boolean partnerTeamMemberGroupFilter, String timeZone, String stageName,
			Integer createdForCompanyId);

	public List<String> getStageNamesForVendor(Integer loggedInUserId);

	public List<String> getStageNamesForPartner(Integer loggedInUserId, Integer vendorCompanyId);

	public List<String> getStageNamesForPartner(Integer loggedInUserId);

	public List<String> getStageNamesForVendorInCampaign(Integer loggedInUserId);

	public List<String> getStageNamesForPartnerCompanyId(Integer companyId);

	public XtremandResponse downloadDeals(Integer userId);

	public List<DealDto> getDealsForCSV(Pagination pagination);

	public LinkedHashMap<String, String> getFieldHeaderMapping(String userType, Integer userId);

	public Deal createDealFromHalopsa(org.json.simple.JSONObject dealInHalopsa, Lead lead, Integer loggedInUserId);

	public XtremandResponse findRegisteredByCompanies(Integer loggedInUserId);

	public XtremandResponse findRegisteredByUsers(Integer loggedInUserId);

	public XtremandResponse findRegisteredByUsersForPartnerView(Integer loggedInUserId);

	public XtremandResponse findRegisteredByUsersByPartnerCompanyId(Integer partnerCompanyId, Integer campaignId);

	public Deal createDealFromZoho(org.json.simple.JSONObject dealInZoho, Lead lead, Integer loggedInUser);

	public XtremandResponse findDealAndLeadInfoAndComments(Integer dealId);

	Map<String, Object> queryDealsForPartner(Pagination pagination);

	Map<String, Object> queryDealsForVendor(Pagination pagination);

	public XtremandResponse findVendorDetailsWithSelfDealsCount(VendorSelfDealRequestDTO vendorSelfDealRequestDTO);

	/** XNFR-553 **/
	XtremandResponse findDealsAndCountByContactId(ContactOpportunityRequestDTO contactOpportunityRequestDTO);

	public XtremandResponse fetchContactsForDealAttachment(Integer loggedInUserId, Pageable pageable);

	public XtremandResponse fetchTotalDealAmountForCompanyJourney(
			ContactOpportunityRequestDTO contactOpportunityRequestDTO);

	XtremandResponse saveDealCustomFormFromMcp(Integer userId);

	XtremandResponse saveDealPipelinesFromMcp(Integer userId);
	
	void saveAndPushDealToxAmplify(DealDto dealDto);
	
	void updateAndPushDealToxAmplify(DealDto dealDto);
	
	void updateDealStatusToxAmplify(DealDto dealDto);

}
