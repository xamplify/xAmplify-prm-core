package com.xtremand.analytics.dao;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import com.xtremand.common.bom.Pagination;
import com.xtremand.partner.bom.PartnerDTO;
import com.xtremand.partner.bom.ReminderEmailLog;
import com.xtremand.team.member.dto.TeamMemberDTO;
import com.xtremand.util.dto.PaginatedDTO;
import com.xtremand.util.dto.PartnerCompanyDTO;
import com.xtremand.util.dto.PartnerJourneyAnalyticsDTO;
import com.xtremand.util.dto.PartnerJourneyRequestDTO;
import com.xtremand.util.dto.PartnerJourneyTrackDetailsDTO;

public interface PartnerAnalyticsDAO {

	public Integer getCompanyPartnersCount(Integer companyId);

	public Integer getPartnersLaunchedCampaignsCount(Integer companyId);

	public List<Object[]> listPartnersLaunchedCampaignsByCampaignType(Integer companyId, boolean applyFilter,
			Integer userId);

	public Map<String, Object> listNoOfCampaignsLaunchedByPartner(Pagination pagination);

	public List<Object[]> listCountrywisePartnersCount(Integer userId, Integer companyId);

	public Map<String, Object> listPartnerCampaigns(Integer companyId, Pagination pagination);

	public Map<String, Object> listPartnerCampaignInteraction(Integer campaignId, Pagination pagination);

	Integer getThroughPartnerCampaignsCount(Integer companyId, boolean applyFilter, Integer userId);

	Integer getInactivePartnersCount(Integer companyId, boolean applyFilter, Integer userId);

	Integer getActivePartnersCount(Integer companyId, boolean applyFilter, Integer userId);

	public Map<String, Object> listInActiveCampaignPartners(Pagination pagination);

	public Integer partnersRedistributedCampaignsCount(Integer companyId, Integer userId, boolean applyFilter);

	public Integer partnersRedistributedCampaignsCount(List<Integer> userIds);

	Integer getThroughPartnersCampaignRedistributedCount(Integer campaignId);

	Map<String, Object> listRedistributedCampaigns(Integer campaignId, Pagination pagination);

	public ReminderEmailLog getReminderEmailLog(Integer vendorId, Integer partnerId);


	String findLeadsToDealsConversionPercentageAsText(Integer companyId, Integer userId, boolean applyFilter);

	Double findOpportunityAmount(Integer companyId, Integer userId, boolean applyFilter);

	public Map<String, Object> findChannelCampaigns(Pagination pagination);

	public Map<String, Object> findRedistributedCampaigns(Pagination pagination);

	/**** XNFR-316 *********/
	public PartnerCompanyDTO getPartnerJourneyCompanyInfo(Integer vendorCompanyId, Integer partnerCompanyId);

	public Map<String, Object> getPartnerJourneyTeamInfo(Pagination pagination);

	public String getPartnerJourneyRedistributedCampaignCount(PartnerJourneyRequestDTO partnerJourneyRequestDTO);

	public String getPartnerJourneyTeamMemberCount(PartnerJourneyRequestDTO partnerJourneyRequestDTO);

	public String getPartnerJourneyShareLeadCount(PartnerJourneyRequestDTO partnerJourneyRequestDTO);

	public String getPartnerJourneyLeadCount(PartnerJourneyRequestDTO partnerJourneyRequestDTO);

	public String getPartnerJourneyDealCount(PartnerJourneyRequestDTO partnerJourneyRequestDTO);

	public String getPartnerJourneyContactCount(PartnerJourneyRequestDTO partnerJourneyRequestDTO);

	public String getPartnerJourneyMdfAmount(PartnerJourneyRequestDTO partnerJourneyRequestDTO);

	public String getPartnerJourneyAssetCount(PartnerJourneyRequestDTO partnerJourneyRequestDTO);

	public PartnerJourneyAnalyticsDTO getPartnerJourneyTrackAndPlaybookCount(
			PartnerJourneyRequestDTO partnerJourneyRequestDTO);

	public Map<String, Object> getPartnerJourneyTrackDetailsByInteraction(Pagination pagination);

	public Map<String, Object> getPartnerJourneyTrackAssetDetailsByType(Pagination pagination);

	public Map<String, Object> getPartnerJourneyTracksByUser(Pagination pagination);

	public Map<String, Object> getPartnerJourneyTrackDetailsByUser(Pagination pagination);

	public Map<String, Object> getPartnerJourneyTrackAssetDetails(Pagination pagination);

	public Map<String, Object> getPartnerJourneyPlaybookAssetDetails(Pagination pagination);

	public Map<String, Object> getPartnerJourneyShareLeadDetails(Pagination pagination);

	public Map<String, Object> getPartnerJourneyRedistributedCampaignDetails(Pagination pagination);

	public Map<String, Object> getPartnerJourneyLeadDetails(Pagination pagination);

	public Map<String, Object> getPartnerJourneyDealDetails(Pagination pagination);

	public Map<String, Object> getPartnerJourneyContactDetails(Pagination pagination);

	public List<Object[]> getPartnerLeadToDealCounts(PartnerJourneyRequestDTO partnerJourneyRequestDTO);

	public PartnerJourneyTrackDetailsDTO getPartnerJourneyTrackCountsByInteraction(
			PartnerJourneyRequestDTO partnerJourneyRequestDTO);

	public PartnerJourneyTrackDetailsDTO getPartnerJourneyTrackCountsByType(
			PartnerJourneyRequestDTO partnerJourneyRequestDTO);

	public List<TeamMemberDTO> getPartnerJourneyTeamEmails(PartnerJourneyRequestDTO partnerJourneyRequestDTO);

	public Map<String, Object> getPartnerJourneyPlaybooksByUser(Pagination pagination);

	public Map<String, Object> getPartnerJourneyMdfDetails(Pagination pagination);

	public List<PartnerDTO> getPartnerJourneyCompanyInfoForFilter(Pagination pagination);

	public List<PartnerDTO> getAllPartnersRegionNamesForFilter(Pagination pagination);

	public List<Object[]> listPartnersLaunchedCampaignsByCampaignTypeBarGraph(
			PartnerJourneyRequestDTO partnerJourneyRequestDTO);

	public List<Object[]> getPartnerCampaignToLeadCounts(PartnerJourneyRequestDTO partnerJourneyRequestDTO);

	public BigInteger getPendingSignupAndCompanyProfileIncompletePartnersCount(Integer companyId, boolean applyFilter,
			Integer userId, String countType);

	public Map<String, Object> getPendingSignupAndCompanyProfileIncompletePartners(Integer companyId,
			Pagination pagination);

	public Integer getDefaultPartnerListidByUserid(Integer logginedUserId);

	List<Object[]> findAllPartnerCompanyNamesAndLeadsAndDealsCount(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			Integer companyId);

	List<Object[]> findPartnerCompanyNamesAndLeadsAndDealsCount(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			Integer companyId);

	public Map<String, Object> getTotalPartnersCount(Integer companyId, boolean applyFilter, Integer userId);

	public String getPartnerJourneyTrackAssetCount(PartnerJourneyRequestDTO partnerJourneyRequestDTO);

	public Map<String, Object> getPartnerJourneyAssetDetails(Pagination pagination);

	public List<PartnerJourneyTrackDetailsDTO> getAllPartnerRegionSDetailsCount(
			PartnerJourneyRequestDTO partnerJourneyRequestDTO);

	public Map<String, Object> getAllPartnersDetailsList(Pagination pagination);

	public PaginatedDTO listAllPartnersForContactUploadManagementSettings(Pagination pagination, String search);

	public void updatePartnerContactUploadLimit(Integer partnerCompanyId, Integer vendorCompanyId,
			Integer contactLimit);

	public Integer fetchNumberOfContactsAddedByCompanyId(Integer companyId);

	public Integer getContactsUploadedCountByAllPartnersForCompanyById(Integer companyId);

	public Integer getContactUploadSubscriptionLimitByCompanyId(Integer companyId);

	public List<PartnerDTO> getAllAssetNamesForFilter(Pagination pagination);

	public List<PartnerDTO> getAllEmailIdsForFilter(Pagination pagination);

	public Map<String, Object> getAssetJourneyAssetsDetails(Pagination pagination);

	public Map<String, Object> getPlaybookJourneyInteractionDetails(Pagination pagination);

	public List<PartnerDTO> getAllPlaybookNamesForFilter(Pagination pagination);

	public Map<String, Object> findTotalDeactivatePartnersCount(Integer companyId, boolean applyFilter, Integer userId);

	public Map<String, Object> getPartnerJourneyAssetInteractionDetails(Pagination pagination);

	public Map<String, Object> getTotalPartnerInteractionDetails(Pagination pagination);

	public Map<String, Object> getPartnerAssetDetailsInteraction(Pagination pagination);

	List<PartnerJourneyTrackDetailsDTO> getPlaybookInteractionDetailsForGroupOfPartners(Integer userListId,
			Integer vendorCompanyId);

	List<PartnerJourneyTrackDetailsDTO> getTrackDetailsForGroupOfPartners(Integer userListId, Integer vendorCompanyId);

	List<PartnerJourneyTrackDetailsDTO> getAssetDetailsForGroupOfPartners(Integer userListId, Integer vendorCompanyId);

}
