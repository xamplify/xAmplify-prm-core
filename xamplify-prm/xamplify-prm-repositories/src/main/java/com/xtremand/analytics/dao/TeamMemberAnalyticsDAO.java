package com.xtremand.analytics.dao;

import java.util.List;
import java.util.Map;

import com.xtremand.common.bom.Pagination;
import com.xtremand.team.member.dto.TeamMemberDTO;
import com.xtremand.util.dto.PartnerJourneyTrackDetailsDTO;
import com.xtremand.util.dto.TeamMemberAnalyticsDTO;
import com.xtremand.util.dto.TeamMemberAnalyticsRequestDTO;
import com.xtremand.vendor.bom.VendorDTO;

public interface TeamMemberAnalyticsDAO {

	public String getTeamMembersRedistributedCampaignCount(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO);

	public String getTeamMembersShareLeadCount(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO);

	public String getTeamMembersLeadCount(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO);

	public String getTeamMembersDealCount(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO);

	public String getTeamMembersAssetCount(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO);

	public TeamMemberAnalyticsDTO getTeamMembersTrackAndPlaybookCount(
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO);

	public String getTeamMembersContactCount(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO);

	public PartnerJourneyTrackDetailsDTO getTeamMemberTrackCountsByInteraction(
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO);

	public Map<String, Object> getTeamMemberTrackDetailsByInteraction(Pagination pagination);

	public PartnerJourneyTrackDetailsDTO getTeamMemberTrackCountsByType(
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO);

	public Map<String, Object> getTeamMemberTrackAssetDetailsByType(Pagination pagination);

	public Map<String, Object> getTeamMemberTracksCount(Pagination pagination);

	public Map<String, Object> getTeamMemberWisePlaybooksCount(Pagination pagination);

	public Map<String, Object> getTeamMemberTrackDetails(Pagination pagination);

	public Map<String, Object> getTeamMemberTrackAssetDetails(Pagination pagination);

	public Map<String, Object> getTeamMemberPlaybookAssetDetails(Pagination pagination);

	public Map<String, Object> getTeamMemberShareLeadDetails(Pagination pagination);

	public Map<String, Object> getTeamMemberRedistributedCampaignDetails(Pagination pagination);

	public Map<String, Object> getTeamMemberLeadDetails(Pagination pagination);

	public Map<String, Object> getTeamMemberDealDetails(Pagination pagination);

	public List<TeamMemberDTO> getTeamMemberInfoForFilter(Pagination pagination);

	public List<VendorDTO> getVendorInfoForFilter(Pagination pagination);

	public Map<String, Object> getTeamMemberMdfDetails(Pagination pagination);

	public String getAllPartnersCountForVendorTeamMember(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO);

	public String getlaunchedCampaignCountForVendorTeamMember(
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO);

	public String getShareLeadCountForVendorTeamMember(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO);

	public String getTeamMembersLeadCountForVendorTeamMember(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO);

	public String getTeamMembersDealCountForVendorTeamMember(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO);

	public String getTeamMembersAssetCountForVendorTeamMember(
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO);

	public TeamMemberAnalyticsDTO getTeamMembersTrackAndPlaybookCountForVendorTeamMember(
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO);

	public String getTeamMembersContactCountForVendorTeamMember(
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO);

	public PartnerJourneyTrackDetailsDTO getTeamMemberTrackCountsByInteractionForVendorTeamMember(
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO);

	public Map<String, Object> getTeamMemberTrackDetailsByInteractionForVendor(Pagination pagination);

	public PartnerJourneyTrackDetailsDTO getTeamMemberTrackCountsByTypeForVendor(
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO);

	public Map<String, Object> getTeamMemberTrackAssetDetailsByTypeForVendorTeamMember(Pagination pagination);

	public Map<String, Object> getTeamMemberTracksCountForVendorTeamMember(Pagination pagination);

	public Map<String, Object> getTeamMemberWisePlaybooksCountForVendorTeamMember(Pagination pagination);

	public Map<String, Object> getTeamMemberTrackAssetDetailsForVendorTeamMember(Pagination pagination);

	public Map<String, Object> getTeamMemberPlaybookAssetDetailsForVendorTeamMember(Pagination pagination);

	public Map<String, Object> getTeamMemberShareLeadDetailsForVendorTeamMember(Pagination pagination);

	public Map<String, Object> getVendorTeamMemberLaunchedCampaignDetails(Pagination pagination);

	public Map<String, Object> getVendorTeamMemberContactDetails(Pagination pagination);

	public Map<String, Object> getallPartnersDetailsForVendor(Pagination pagination);

	public List<Object[]> findLeadsAndDealsCountForTeamMember(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO,
			String filterType);

	public List<Object[]> findAllLeadsAndDealsCountForTeamMember(
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO, String filterType);

	public Map<String, Object> getContactsDetailsForTeamMember(Pagination pagination);

	public Map<String, Object> getVendorTeamMemberTrackDetails(Pagination pagination);

	public Map<String, Object> getVendorTeamMemberLeadDetails(Pagination pagination);

	public Map<String, Object> getVendorTeamMemberDealDetails(Pagination pagination);

	public Map<String, Object> getVendorTeamMemberMdfDetails(Pagination pagination);

	public Map<String, Object> getVendorTeamMemberAssetsCount(Pagination pagination);

	public Map<String, Object> getVendorTeamMemberAssetsDetails(Pagination pagination);

	public String getTeamMembersCompanyCount(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO);

	public Map<String, Object> getCompanyDetailsForTeamMember(Pagination pagination);

	public String getVendorTeamMembersCompanyCount(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO);

	public String getTeamMembersTrackAssetCount(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO);

	public String getVendorTeamMembersTrackAssetCount(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO);

	public Map<String, Object> getTeamMemberAssetsDetails(Pagination pagination);

}
