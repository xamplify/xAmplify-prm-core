package com.xtremand.analytics.service;

import javax.servlet.http.HttpServletResponse;

import com.xtremand.common.bom.Pagination;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.util.dto.TeamMemberAnalyticsRequestDTO;


public interface TeamMemberAnalyticsService {
	
	public XtremandResponse getTeamMemberJourneyCounts(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO);

	public XtremandResponse getTeamMemberTrackCountsByInteraction(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO);

	public XtremandResponse getTeamMemberTrackDetailsByInteraction(Pagination pagination);

	public XtremandResponse getTeamMemberTrackCountsByType(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO);

	public XtremandResponse getTeamMemberTrackAssetDetailsByType(Pagination pagination);

	public XtremandResponse getTeamMemberTracksCount(Pagination pagination);

	public XtremandResponse getTeamMemberWisePlaybooksCount(Pagination pagination);

	public XtremandResponse getTeamMemberTrackDetails(Pagination pagination);

	public XtremandResponse getTeamMemberTrackAssetDetails(Pagination pagination);

	public XtremandResponse getTeamMemberPlaybookAssetDetails(Pagination pagination);

	public XtremandResponse getTeamMemberShareLeadDetails(Pagination pagination);


	public XtremandResponse getTeamMemberRedistributedCampaignDetails(Pagination pagination);

	public XtremandResponse getTeamMemberLeadDetails(Pagination pagination);

	public XtremandResponse getTeamMemberDealDetails(Pagination pagination);

	public XtremandResponse getVendorInfoForFilter(Pagination pagination);

	public XtremandResponse getTeamMemberInfoForFilter(Pagination pagination);

	public XtremandResponse getTeamMemberMdfDetails(Pagination pagination);

	public XtremandResponse getTeamMemberJourneyCountsForVendor(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO);

	public XtremandResponse getVendorTeamMemberTrackCountsByInteraction(
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO);

	public XtremandResponse getVendorTeamMemberTrackDetailsByInteraction(Pagination pagination);

	public XtremandResponse getVendorTeamMemberTrackCountsByType(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO);

	public XtremandResponse getVendorTeamMemberTrackAssetDetailsByType(Pagination pagination);

	public XtremandResponse getVendorTeamMemberTracksCount(Pagination pagination);

	public XtremandResponse getVendorTeamMemberWisePlaybooksCount(Pagination pagination);

	public XtremandResponse getVendorTeamMemberTrackAssetDetails(Pagination pagination);

	public XtremandResponse getVendorTeamMemberPlaybookAssetDetails(Pagination pagination);

	public XtremandResponse getVendorTeamMemberShareLeadDetails(Pagination pagination);


	public XtremandResponse getVendorTeamMemberLaunchedCampaignDetails(Pagination pagination);

	public XtremandResponse getVendorTeamMemberContactsDetails(Pagination pagination);

	public XtremandResponse getVendorTeamMemberallPartnersDetails(Pagination pagination);

	public XtremandResponse findLeadsAndDealsCountForTeamMember(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO,
			String filterType);

	public XtremandResponse findAllLeadsAndDealsCountForTeamMember(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO,
			String filterType);

	public XtremandResponse getContactsDetailsForTeamMember(Pagination pagination);

	public XtremandResponse getVendorTeamMemberTrackDetails(Pagination pagination);

	public XtremandResponse getVendorTeamMemberLeadDetails(Pagination pagination);

	public XtremandResponse getVendorTeamMemberDealDetails(Pagination pagination);

	public XtremandResponse getVendorTeamMemberMdfDetails(Pagination pagination);

	public XtremandResponse getVendorTeamMemberAssetsCount(Pagination pagination);

	public XtremandResponse getVendorTeamMemberAssetsDetails(Pagination pagination);

	public XtremandResponse getCompanyDetailsForTeamMember(Pagination pagination);

	public HttpServletResponse downloadTrackInteractionAndNonInteractionReport(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO,
			HttpServletResponse response);

	public HttpServletResponse downloadTrackAsserDetailsReport(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO,
			HttpServletResponse response);

	public HttpServletResponse downloadUserWiseTrackCountReport(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO,
			HttpServletResponse response);

	public HttpServletResponse downloadUserWisePlayBookCountReport(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO,
			HttpServletResponse response);

	public HttpServletResponse downloadTrackAssetsDetailedReport(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO,
			HttpServletResponse response);

	public HttpServletResponse downloadTrackAssetsDetailsReport(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO,
			HttpServletResponse response);

	public HttpServletResponse downloadPlayBookAssetsDetailsReport(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO,
			HttpServletResponse response);

	public HttpServletResponse downloadShareLeadsDetailsReport(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO,
			HttpServletResponse response);

	public HttpServletResponse downloadLeadsDetailsReport(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO,
			HttpServletResponse response);

	public HttpServletResponse downloadDealDetailsReport(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO,
			HttpServletResponse response);

	public HttpServletResponse downloadMDFDetailsReport(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO,
			HttpServletResponse response);

	public HttpServletResponse downloadContactDetailsReport(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO,
			HttpServletResponse response);

	public HttpServletResponse downloadCompanyDetailsReport(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO,
			HttpServletResponse response);

	public HttpServletResponse downloadAssetCountReport(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO,
			HttpServletResponse response);

	public HttpServletResponse downloadAssetDetailsReport(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO,
			HttpServletResponse response);

	public HttpServletResponse downloadAllPartnersDetailsReport(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO,
			HttpServletResponse response);

	public XtremandResponse getTeamMemberAssetsDetails(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO);

	public HttpServletResponse downloadTeamMemberAssetDetailsReport(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO,
			HttpServletResponse response);
	
}
