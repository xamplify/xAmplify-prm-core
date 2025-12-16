package com.xtremand.lead.dao;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import com.xtremand.activity.dto.ContactOpportunityRequestDTO;
import com.xtremand.campaign.bom.ModuleAccess;
import com.xtremand.common.bom.Pagination;
import com.xtremand.integration.bom.Integration.IntegrationType;
import com.xtremand.lead.bom.Lead;
import com.xtremand.lead.bom.LeadCustomField;
import com.xtremand.lead.bom.LeadField;
import com.xtremand.lead.dto.LeadCountsResponseDTO;
import com.xtremand.lead.dto.LeadDto;
import com.xtremand.util.dto.UserDetailsUtilDTO;

public interface LeadDAO {

	Map<String, Object> getLeadsForPartner(Pagination pagination);

	Map<String, Object> getLeadsForVendor(Pagination pagination);

	List<String> getStageNamesForVendor(Integer loggedInUserId);

	List<String> getStageNamesForPartner(Integer loggedInUserId);

	List<String> getStageNamesForPartner(Integer partnerCompanyId, Integer vendorCompanyId);

	List<Object[]> getVendorList(Integer companyId);

	List<String> getStgaeNamesFOrPartnerInCampaign(Integer loggedInUserId);

	Lead getCampaignLead(Integer campaignId, Integer userId);

	LeadCountsResponseDTO getCountsForVendor(Integer companyId);

	LeadCountsResponseDTO getCountsForPartner(Integer companyId);

	LeadCountsResponseDTO getCountsForPartnerInVanity(Integer companyId, Integer vendorCompanyId);

	List<Lead> getLeadsWithSfLeadIdForVendor(Integer companyId);

	BigInteger getUnReadChatCount(Integer leadId, Integer loggedInUserId);

	LeadCountsResponseDTO findLeadsCountByFilter(String sqlQuery, Integer companyId, List<Integer> partnerCompanyIds);

	List<String> getStageNamesForCampaign(Integer campaignId, Integer companyId, boolean vanityUrlFilter);

	List<String> getStageNamesForParentCampaign(Integer campaignId, Integer companyId, boolean vanityUrlFilter);

	List<Lead> getLeadsWithMicrosoftDynamicsLeadIdForVendor(Integer companyId);

	List<Lead> getLeadsForVendorByType(Integer companyId, IntegrationType type);

	List<Object[]> getVendorListForLoginAsPartner(Integer companyId, Integer vendorCompanyId);

	List<Lead> getLeadsByType(Integer companyId, IntegrationType type);

	/**** XNFR-505 ****/
	Map<String, Object> getLeadsForLeadAttachment(Pagination pagination);

	List<LeadField> getDefaultLeadFilds();

	List<LeadCustomField> getLeadCustomFields(Integer companyId);

	/** XNFR-649 **/
	Map<String, Object> queryLeadsForVendor(Pagination pagination);

	Map<String, Object> queryLeadsForPartner(Pagination pagination);

	/** XNFR-553 **/
	Map<String, Object> findLeadsAndCountByContactId(ContactOpportunityRequestDTO contactOpportunityRequestDTO);

	/** XNFR-848 **/
	Map<String, Object> fetchLeadsForCompanyJourney(Pagination pagination);

	/** XNFR-892 **/
	UserDetailsUtilDTO fetchFullNameAndEmailIdByUserId(Integer userId);

	LeadDto fetchMergeTagsDataForPartnerMailNotification(Integer leadId);

	List<String> findPartnerAssosiatedTeamMembers(Integer partnerCompanyId, Integer companyId);

	boolean checkPipelineStageMappedToCustomFiled(Integer companyId);

	String getActiveCRMTypeByCompanyId(Integer companyId);

	String findLatestLeadCommentByLead(Lead lead);

	List<LeadDto> fetchLeadsForContactReport(String dynamicQueryCondition, Integer createdByCompanyId,
			Integer createdForCompanyId, Integer limit);

	List<LeadDto> fetchLeadsForContact(Integer contactId, Integer userListId, Integer createdByCompanyId,
			Integer createdForCompanyId);

	List<LeadDto> fetchLeadsForPartnerReport(Integer createdByCompanyId, Integer createdForCompanyId,
			Integer userListId, Integer queryLimit);

	void getUserUserDetailsForLead(LeadDto leadDto, Integer partnerId, Integer vendorId);

	ModuleAccess getCompanyAccess(Integer companyId);
	
	List<Lead> findLeadsByCreatedForCompanyId(Integer createdForCompanyId);

}
