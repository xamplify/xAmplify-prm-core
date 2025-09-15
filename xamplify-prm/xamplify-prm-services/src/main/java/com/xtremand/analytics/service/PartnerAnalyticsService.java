package com.xtremand.analytics.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.xtremand.common.bom.Pagination;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.partner.bom.PartnerContactUsageDTO;
import com.xtremand.util.dto.Pageable;
import com.xtremand.util.dto.PartnerJourneyRequestDTO;

public interface PartnerAnalyticsService {

	public Integer getCompanyPartnersCount(Integer companyId);

	public Map<String, Object> listCountrywisePartnersCount(Integer userId, Integer companyId);

	XtremandResponse sendPartnerReminder(UserDTO userDto, Integer vendorId);

	public XtremandResponse findLeadsToDealsConversionPercentageAsText(Integer companyId, Integer userId,
			boolean applyFilter);

	public XtremandResponse findOpportunityAmountAsText(Integer companyId, Integer userId, boolean applyFilter);

	public XtremandResponse findChannelCampaigns(Pagination pagination);

	public XtremandResponse findRedistributedCampaigns(Pagination pagination);

	public Map<String, Object> countrywisePartnersCount(Integer userId, boolean applyFilter);

	public Map<String, Object> findRedistributedCampaignsCount(Integer userId, boolean applyFilter);

	public Map<String, Object> findActivePartnersCount(Integer userId, boolean applyFilter);

	public Map<String, Object> findThroughPartnerCampaignsCount(Integer userId, boolean applyFilter);

	public Map<String, Object> findInActivePartnersCount(Integer userId, boolean applyFilter);

	public Map<String, Object> findApprovePartnersCount(Integer userId, boolean applyFilter);

	public XtremandResponse findAllPartnerCompanies(Pagination pagination);

	public XtremandResponse findJourney(Integer partnershipId);

	/**** XNFR-316 ***/
	public XtremandResponse getActivePartnerCompanies(Pagination pagination);

	public XtremandResponse getPartnerJourneyCompanyInfo(Integer partnerCompanyId, Integer loggedInUserId);

	public XtremandResponse getPartnerJourneyTeamInfo(Pagination pagination);

	public XtremandResponse getPartnerJourneyCounts(PartnerJourneyRequestDTO partnerJourneyRequestDTO);

	public XtremandResponse getPartnerJourneyTrackDetailsByInteraction(Pagination pagination);

	public XtremandResponse getPartnerJourneyTrackAssetDetailsByType(Pagination pagination);

	public XtremandResponse getPartnerJourneyTracksByUser(Pagination pagination);

	public XtremandResponse getPartnerJourneyTrackDetailsByUser(Pagination pagination);

	public XtremandResponse getPartnerJourneyTrackAssetDetails(Pagination pagination);

	public XtremandResponse getPartnerJourneyPlaybookAssetDetails(Pagination pagination);

	public XtremandResponse getPartnerJourneyShareLeadDetails(Pagination pagination);

	public XtremandResponse getPartnerJourneyRedistributedCampaignDetails(Pagination pagination);

	public XtremandResponse getPartnerJourneyLeadDetails(Pagination pagination);

	public XtremandResponse getPartnerJourneyDealDetails(Pagination pagination);

	public XtremandResponse getPartnerJourneyContactDetails(Pagination pagination);

	public XtremandResponse getPartnerLeadToDealCounts(PartnerJourneyRequestDTO partnerJourneyRequestDTO);

	public XtremandResponse getPartnerJourneyTrackCountsByInteraction(
			PartnerJourneyRequestDTO partnerJourneyRequestDTO);

	public XtremandResponse getPartnerJourneyTrackCountsByType(PartnerJourneyRequestDTO partnerJourneyRequestDTO);

	public XtremandResponse getPartnerJourneyTeamEmails(PartnerJourneyRequestDTO partnerJourneyRequestDTO);

	public XtremandResponse getPartnerJourneyPlaybooksByUser(Pagination pagination);

	public XtremandResponse getPartnerJourneyMdfDetails(Pagination pagination);

	public XtremandResponse getPartnerJourneyCompanyInfoForFilter(Pagination pagination);

	public XtremandResponse getAllPartnersRegionNamesForFilter(Pagination pagination);

	public XtremandResponse getPartnerCampaignToLeadCounts(PartnerJourneyRequestDTO partnerJourneyRequestDTO);

	/**** END: XNFR-316 ***/

	public Map<String, Object> findPendingSignupAndCompanyProfileIncompletePartnersCount(Integer userId,
			boolean applyFilter);

	public Map<String, Object> getPendingSignupAndCompanyProfileIncompletePartnersCount(Pagination pagination);

	XtremandResponse sendSingupIncompleteCompanyprofilEmail(Pagination pagination);

	public HttpServletResponse downloadPartnerAnalyticsTeamMembers(Pagination pagination, HttpServletResponse response);

	public HttpServletResponse downloadInteractedAndNonInteractedTracksReport(
			PartnerJourneyRequestDTO partnerJourneyRequestDTO, HttpServletResponse response);

	public HttpServletResponse downloadTypeWiseTrackContentReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response);

	public HttpServletResponse downloadUserWiseTrackCountReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response);

	public HttpServletResponse downloadUserWisePlayBookCountReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response);

	public HttpServletResponse downloadTrackAssetsDetailedReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response);

	public HttpServletResponse downloadTrackAssetsDetailsReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response);

	public HttpServletResponse downloadPlayBookAssetsDetailsReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response);

	public HttpServletResponse downloadShareLeadsDetailsReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response);


	public HttpServletResponse downloadLeadsDetailsReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response);

	public HttpServletResponse downloadDealsDetailsReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response);

	public HttpServletResponse downloadMDFDetailsReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response);

	XtremandResponse findAllLeadsAndDealsCount(PartnerJourneyRequestDTO partnerJourneyRequestDTO);

	XtremandResponse findPartnerCompanyNamesAndLeadsAndDealsCount(PartnerJourneyRequestDTO partnerJourneyRequestDTO);

	public XtremandResponse findTotalPartnersCount(Integer userId, boolean applyFilter);


	public HttpServletResponse downloadTeamMembersReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response);

	public XtremandResponse getPartnerJourneyAssetsDetails(PartnerJourneyRequestDTO partnerJourneyRequestDTO);

	public HttpServletResponse downloadAssetDetailsReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response);

	/*** XNFR-835 ***/
	public HttpServletResponse downloadActivePartnersReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response);

	public HttpServletResponse downloadInActivePartnersReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response);

	public HttpServletResponse downloadCompanyProfileIncompletePartnersReport(
			PartnerJourneyRequestDTO partnerJourneyRequestDTO, HttpServletResponse response);

	/*** XNFR-835 ***/

	/*** XNFR-944 ***/
	public XtremandResponse findAllPartnerDetails(PartnerJourneyRequestDTO partnerJourneyRequestDTO);

	/*** XNFR-944 ***/
	public XtremandResponse getAllPartnersDetailsList(Pagination pagination);

	/*** XNFR-944 ***/

	public XtremandResponse listAllPartnersForContactUploadManagementSettings(Pageable pageable,
			Integer loggedInUserId);

	public XtremandResponse saveOrUpdateContactUploadSManagementSettings(Integer loggedInUserId,
			List<PartnerContactUsageDTO> partnerContactUsageDTOs);

	public XtremandResponse fetchTotalNumberOfContactsAddedForCompany(Integer loggedInUserId);

	public XtremandResponse loadContactsUploadedCountByAllPartners(Integer loggedInUserId);

	public XtremandResponse loadContactUploadSubscriptionLimitForCompany(Integer loggedInUserId);

	public XtremandResponse getNumberOfContactSubscriptionUsedByCompany(Integer loggedInUserId);

	public void allpartnersDownloadCsv(Integer userId, String regionFilter, String sortColumn,
			List<String> selectedRegionIdList, List<String> selectedStatusIdList, boolean partnerTeamMemberGroupFilter,
			Pageable pageable, HttpServletResponse response);

	public XtremandResponse getAllAssetNamesForFilter(Pagination pagination);

	public XtremandResponse getAllEmailIdsForFilter(Pagination pagination);

	public XtremandResponse getAssetJourneyAssetsDetails(Pagination pagination);

	public HttpServletResponse downloadAssetJourneyAssetDetailsReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response);

	public XtremandResponse getPlaybookJourneyInteractionDetails(Pagination pagination);

	public HttpServletResponse downloadPlaybookJourneyInteractionDetailsReport(
			PartnerJourneyRequestDTO partnerJourneyRequestDTO, HttpServletResponse response);

	public XtremandResponse getAllPlaybookNamesForFilter(Pagination pagination);

	public XtremandResponse findTotalDeactivatePartnersCount(Integer userId, boolean applyFilter);

	public XtremandResponse getDeactivePartnerCompanies(Pagination pagination);

	public HttpServletResponse downloadDeactivePartnersReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response);

	public XtremandResponse getAllPartnerCompaniesDeatails(Pagination pagination);

	public HttpServletResponse downloadAllPartnersDetailsReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response);

}
