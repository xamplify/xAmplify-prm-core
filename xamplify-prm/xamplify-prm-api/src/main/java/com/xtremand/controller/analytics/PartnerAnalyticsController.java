package com.xtremand.controller.analytics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.xtremand.analytics.service.PartnerAnalyticsService;
import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.common.bom.Pagination;
import com.xtremand.common.bom.StatusCode;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.mail.service.StatusCodeConstants;
import com.xtremand.partner.bom.PartnerContactUsageDTO;
import com.xtremand.partner.bom.PartnerDataAccessException;
import com.xtremand.userlist.exception.UserListException;
import com.xtremand.util.BadRequestException;
import com.xtremand.util.HttpHeaderUtil;
import com.xtremand.util.ResponseUtil;
import com.xtremand.util.dto.Pageable;
import com.xtremand.util.dto.PartnerJourneyRequestDTO;

@RestController
@RequestMapping("/partner")
public class PartnerAnalyticsController {

	private static final Logger logger = LoggerFactory.getLogger(PartnerAnalyticsController.class);

	@Autowired
	private PartnerAnalyticsService partnerAnalyticsService;

	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/send-in-active-reminder-email/{vendorId}")
	public ResponseEntity sendInactiveReminderEmail(@RequestBody UserDTO userDto, @PathVariable Integer vendorId) {
		try {
			XtremandResponse response = partnerAnalyticsService.sendPartnerReminder(userDto, vendorId);
			return ResponseUtil.getResponse(HttpStatus.OK, response.getStatusCode(), response);
		} catch (PartnerDataAccessException e) {
			logger.error("Error In send-in-active-reminder-email", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", e.getMessage()));
		}

	}

	@PostMapping("/findChannelCampaigns")
	@ResponseBody
	public ResponseEntity<XtremandResponse> findChannelCampaigns(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(partnerAnalyticsService.findChannelCampaigns(pagination));
	}

	@PostMapping("/findRedistributedCampaigns")
	@ResponseBody
	public ResponseEntity<XtremandResponse> findRedistributedCampaigns(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(partnerAnalyticsService.findRedistributedCampaigns(pagination));
	}


	@GetMapping("/getLeadsAndDealsCount")
	public ResponseEntity<XtremandResponse> getLeadsAndDealsCount(PartnerJourneyRequestDTO partnerJourneyRequestDTO) {
		return ResponseEntity
				.ok(partnerAnalyticsService.findPartnerCompanyNamesAndLeadsAndDealsCount(partnerJourneyRequestDTO));
	}

	@GetMapping("/getAllLeadsAndDealsCount")
	public ResponseEntity<XtremandResponse> getAllLeadsAndDealsCount(
			PartnerJourneyRequestDTO partnerJourneyRequestDTO) {
		return ResponseEntity.ok(partnerAnalyticsService.findAllLeadsAndDealsCount(partnerJourneyRequestDTO));
	}

	@GetMapping("/findLeadsToDealsConversionPercentage/{companyId}/{userId}/{applyFilter}")
	public ResponseEntity<XtremandResponse> findLeadsToDealsConversionPercentage(@PathVariable Integer companyId,
			@PathVariable Integer userId, @PathVariable boolean applyFilter) {
		return ResponseEntity
				.ok(partnerAnalyticsService.findLeadsToDealsConversionPercentageAsText(companyId, userId, applyFilter));
	}

	@GetMapping("/findLeadsOpportunityAmount/{companyId}/{userId}/{applyFilter}")
	public ResponseEntity<XtremandResponse> findLeadsOpportunityAmount(@PathVariable Integer companyId,
			@PathVariable Integer userId, @PathVariable boolean applyFilter) {
		return ResponseEntity.ok(partnerAnalyticsService.findOpportunityAmountAsText(companyId, userId, applyFilter));
	}

	@RequestMapping(value = "/countrywisePartnersCount", method = RequestMethod.GET)
	public ResponseEntity<?> countrywisePartnersCount(@RequestParam Integer userId, @RequestParam boolean applyFilter) {
		Map<String, Object> resultMap = partnerAnalyticsService.countrywisePartnersCount(userId, applyFilter);
		return ResponseEntity.status(HttpStatus.OK).body(resultMap);
	}

	@RequestMapping(value = "/findActivePartnersCount", method = RequestMethod.GET)
	public ResponseEntity<?> findActivePartnersCount(@RequestParam Integer userId, @RequestParam boolean applyFilter) {
		Map<String, Object> resultMap = partnerAnalyticsService.findActivePartnersCount(userId, applyFilter);
		return ResponseEntity.status(HttpStatus.OK).body(resultMap);
	}

	@RequestMapping(value = "/findRedistributedCampaignsCount", method = RequestMethod.GET)
	public ResponseEntity<?> partnersLaunchedCampaignsCount(@RequestParam Integer userId,
			@RequestParam boolean applyFilter) {
		Map<String, Object> resultMap = partnerAnalyticsService.findRedistributedCampaignsCount(userId, applyFilter);
		return ResponseEntity.status(HttpStatus.OK).body(resultMap);
	}

	@RequestMapping(value = "/findThroughPartnerCampaignsCount", method = RequestMethod.GET)
	public ResponseEntity<?> findThroughPartnerCampaignsCount(@RequestParam Integer userId,
			@RequestParam boolean applyFilter) {
		Map<String, Object> resultMap = partnerAnalyticsService.findThroughPartnerCampaignsCount(userId, applyFilter);
		return ResponseEntity.status(HttpStatus.OK).body(resultMap);
	}

	@RequestMapping(value = "/findInActivePartnersCount", method = RequestMethod.GET)
	public ResponseEntity<?> findInActivePartnersCount(@RequestParam Integer userId,
			@RequestParam boolean applyFilter) {
		Map<String, Object> resultMap = partnerAnalyticsService.findInActivePartnersCount(userId, applyFilter);
		return ResponseEntity.status(HttpStatus.OK).body(resultMap);
	}

	@RequestMapping(value = "/findApprovePartnersCount", method = RequestMethod.GET)
	public ResponseEntity<?> findApprovePartnersCount(@RequestParam Integer userId, @RequestParam boolean applyFilter) {
		Map<String, Object> resultMap = partnerAnalyticsService.findApprovePartnersCount(userId, applyFilter);
		return ResponseEntity.status(HttpStatus.OK).body(resultMap);
	}

	/**** XNFR-220 ***/
	@PostMapping(value = "allPartners")
	public ResponseEntity<XtremandResponse> findAllPartners(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(partnerAnalyticsService.findAllPartnerCompanies(pagination));
	}

	/**** XNFR-220 ***/
	@GetMapping(value = "findJourney/{partnershipId}")
	public ResponseEntity<XtremandResponse> findJourney(@PathVariable Integer partnershipId) {
		return ResponseEntity.ok(partnerAnalyticsService.findJourney(partnershipId));
	}

	/**** Start : XNFR-316 ***/
	@PostMapping(value = "/active-partners")
	public ResponseEntity<XtremandResponse> getActivePartnerCompanies(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(partnerAnalyticsService.getActivePartnerCompanies(pagination));
	}

	/**** Start : XNFR-1006 ***/
	@PostMapping(value = "/deactivated-partners")
	public ResponseEntity<XtremandResponse> getDeactivePartnerCompanies(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(partnerAnalyticsService.getDeactivePartnerCompanies(pagination));
	}

	/**** Start : XNFR-1016 ***/
	@PostMapping(value = "/all-partners/detail/list")
	public ResponseEntity<XtremandResponse> getAllPartnerCompaniesDeatails(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(partnerAnalyticsService.getAllPartnerCompaniesDeatails(pagination));
	}

	@GetMapping(value = "journey/company/info/{partnerCompanyId}/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> getPartnerJourneyCompanyInfo(@PathVariable Integer partnerCompanyId,
			@PathVariable Integer loggedInUserId) {
		return ResponseEntity
				.ok(partnerAnalyticsService.getPartnerJourneyCompanyInfo(partnerCompanyId, loggedInUserId));
	}

	@PostMapping(value = "journey/team/info")
	public ResponseEntity<XtremandResponse> getPartnerJourneyTeamInfo(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(partnerAnalyticsService.getPartnerJourneyTeamInfo(pagination));
	}

	@PostMapping(value = "journey/team/emails")
	public ResponseEntity<XtremandResponse> getPartnerJourneyTeamEmails(
			@RequestBody PartnerJourneyRequestDTO partnerJourneyRequestDTO) {
		return ResponseEntity.ok(partnerAnalyticsService.getPartnerJourneyTeamEmails(partnerJourneyRequestDTO));
	}

	@PostMapping(value = "journey/counts")
	public ResponseEntity<XtremandResponse> getPartnerJourneyCounts(
			@RequestBody PartnerJourneyRequestDTO partnerJourneyRequestDTO) {
		return ResponseEntity.ok(partnerAnalyticsService.getPartnerJourneyCounts(partnerJourneyRequestDTO));
	}

	@PostMapping(value = "journey/track/interaction/counts")
	public ResponseEntity<XtremandResponse> getPartnerJourneyTrackCountsByInteraction(
			@RequestBody PartnerJourneyRequestDTO partnerJourneyRequestDTO) {
		return ResponseEntity
				.ok(partnerAnalyticsService.getPartnerJourneyTrackCountsByInteraction(partnerJourneyRequestDTO));
	}

	@PostMapping(value = "journey/track/interaction")
	public ResponseEntity<XtremandResponse> getPartnerJourneyTrackDetailsByInteraction(
			@RequestBody Pagination pagination) {
		return ResponseEntity.ok(partnerAnalyticsService.getPartnerJourneyTrackDetailsByInteraction(pagination));
	}

	@PostMapping(value = "journey/track/typewise/counts")
	public ResponseEntity<XtremandResponse> getPartnerJourneyTrackCountsByType(
			@RequestBody PartnerJourneyRequestDTO partnerJourneyRequestDTO) {
		return ResponseEntity.ok(partnerAnalyticsService.getPartnerJourneyTrackCountsByType(partnerJourneyRequestDTO));
	}

	@PostMapping(value = "journey/track/content/typewise")
	public ResponseEntity<XtremandResponse> getPartnerJourneyTrackAssetDetailsByType(
			@RequestBody Pagination pagination) {
		return ResponseEntity.ok(partnerAnalyticsService.getPartnerJourneyTrackAssetDetailsByType(pagination));
	}

	@PostMapping(value = "journey/track/userwise/count")
	public ResponseEntity<XtremandResponse> getPartnerJourneyTracksByUser(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(partnerAnalyticsService.getPartnerJourneyTracksByUser(pagination));
	}

	@PostMapping(value = "journey/track/userwise/details")
	public ResponseEntity<XtremandResponse> getPartnerJourneyTrackDetailsByUser(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(partnerAnalyticsService.getPartnerJourneyTrackDetailsByUser(pagination));
	}

	@PostMapping(value = "journey/track/asset/details")
	public ResponseEntity<XtremandResponse> getPartnerJourneyTrackAssetDetails(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(partnerAnalyticsService.getPartnerJourneyTrackAssetDetails(pagination));
	}

	@PostMapping(value = "journey/playbook/asset/details")
	public ResponseEntity<XtremandResponse> getPartnerJourneyPlaybookAssetDetails(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(partnerAnalyticsService.getPartnerJourneyPlaybookAssetDetails(pagination));
	}

	@PostMapping(value = "journey/share/lead/details")
	public ResponseEntity<XtremandResponse> getPartnerJourneyShareLeadDetails(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(partnerAnalyticsService.getPartnerJourneyShareLeadDetails(pagination));
	}

	@PostMapping(value = "/journey/redistributed/campaign/details")
	public ResponseEntity<XtremandResponse> getPartnerJourneyRedistributedCampaignDetails(
			@RequestBody Pagination pagination) {
		return ResponseEntity.ok(partnerAnalyticsService.getPartnerJourneyRedistributedCampaignDetails(pagination));
	}

	@PostMapping(value = "journey/lead/details")
	public ResponseEntity<XtremandResponse> getPartnerJourneyLeadDetails(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(partnerAnalyticsService.getPartnerJourneyLeadDetails(pagination));
	}

	@PostMapping(value = "journey/deal/details")
	public ResponseEntity<XtremandResponse> getPartnerJourneyDealDetails(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(partnerAnalyticsService.getPartnerJourneyDealDetails(pagination));
	}

	@PostMapping(value = "journey/contact/details")
	public ResponseEntity<XtremandResponse> getPartnerJourneyContactDetails(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(partnerAnalyticsService.getPartnerJourneyContactDetails(pagination));
	}

	@PostMapping(value = "journey/mdf/details")
	public ResponseEntity<XtremandResponse> getPartnerJourneyMdfDetails(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(partnerAnalyticsService.getPartnerJourneyMdfDetails(pagination));
	}

	@PostMapping(value = "journey/company/details/filter")
	public ResponseEntity<XtremandResponse> getPartnerJourneyCompanyInfoForFilter(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(partnerAnalyticsService.getPartnerJourneyCompanyInfoForFilter(pagination));
	}

	@PostMapping(value = "region/names/filter")
	public ResponseEntity<XtremandResponse> getAllPartnersRegionNamesForFilter(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(partnerAnalyticsService.getAllPartnersRegionNamesForFilter(pagination));
	}

	@GetMapping(value = "journey/lead-to-deal/counts")
	public ResponseEntity<XtremandResponse> getPartnerLeadToDealCounts(
			PartnerJourneyRequestDTO partnerJourneyRequestDTO) {
		return ResponseEntity.ok(partnerAnalyticsService.getPartnerLeadToDealCounts(partnerJourneyRequestDTO));
	}

	@GetMapping(value = "journey/campaigns-to-lead/counts")
	public ResponseEntity<XtremandResponse> getPartnerCampaignToLeadCounts(
			PartnerJourneyRequestDTO partnerJourneyRequestDTO) {
		return ResponseEntity.ok(partnerAnalyticsService.getPartnerCampaignToLeadCounts(partnerJourneyRequestDTO));
	}

	@PostMapping(value = "journey/playbook/userwise/count")
	public ResponseEntity<XtremandResponse> getPartnerJourneyPlaybooksByUser(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(partnerAnalyticsService.getPartnerJourneyPlaybooksByUser(pagination));
	}

	/**** End : XNFR-316 ***/

	@GetMapping("/findPendingSignupAndCompanyProfilePartnersCount")
	public ResponseEntity<Map<String, Object>> findPendingSignupAndCompanyProfileIncompletePartnersCount(
			@RequestParam Integer userId, @RequestParam boolean applyFilter) {
		return ResponseEntity.ok(
				partnerAnalyticsService.findPendingSignupAndCompanyProfileIncompletePartnersCount(userId, applyFilter));
	}

	@PostMapping("/companyProfileIncomplete-partners")
	public ResponseEntity<Map<String, Object>> getPendingSignupAndCompanyProfileIncompletePartnersCount(
			@RequestBody Pagination pagination) {
		return ResponseEntity
				.ok(partnerAnalyticsService.getPendingSignupAndCompanyProfileIncompletePartnersCount(pagination));
	}

	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/sendsingup-incompletecompanyprofile-mail")
	public ResponseEntity sendsingupincompletecompanyprofilemail(@RequestBody Pagination pagination) {
		try {
			XtremandResponse response = partnerAnalyticsService.sendSingupIncompleteCompanyprofilEmail(pagination);
			return ResponseEntity.status(HttpStatus.OK).body(response);
		} catch (PartnerDataAccessException e) {
			logger.error("sendsingup-incompletecompanyprofile-mail", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", e.getMessage()));
		}

	}

	@RequestMapping(value = "/download", method = RequestMethod.POST)
	public ResponseEntity<?> downloadTeamMemberList(@RequestBody Pagination pagination, HttpServletResponse response)
			throws IOException {
		try {

			partnerAnalyticsService.downloadPartnerAnalyticsTeamMembers(pagination, response);
			StatusCode statusCode = StatusCode.getstatuscode(StatusCodeConstants.DOWNLOAD_USERLIST_SUCCESS);
			return ResponseEntity.status(HttpStatus.OK).headers(HttpHeaderUtil.getHeader(statusCode)).body(null);
		} catch (UserListException e) {
			logger.error("error Occurred " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", e.getMessage()));
		}
	}

	@GetMapping("/journey/download/track-interaction-report")
	public void downloadInteractedAndNonInteractedTracksReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response) {
		partnerAnalyticsService.downloadInteractedAndNonInteractedTracksReport(partnerJourneyRequestDTO, response);
	}

	@GetMapping("/journey/download/typewise-track-content-report")
	public void downloadTypeWiseTrackContentReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response) {
		partnerAnalyticsService.downloadTypeWiseTrackContentReport(partnerJourneyRequestDTO, response);
	}

	@GetMapping("/journey/download/track-counts-report")
	public void downloadUserWiseTrackCountReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response) {
		partnerAnalyticsService.downloadUserWiseTrackCountReport(partnerJourneyRequestDTO, response);
	}

	@GetMapping("/journey/download/playbook-counts-report")
	public void downloadUserWisePlayBookCountReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response) {
		partnerAnalyticsService.downloadUserWisePlayBookCountReport(partnerJourneyRequestDTO, response);
	}

	@GetMapping("/journey/download/track-assets-detailed-report")
	public void downloadTrackAssetsDetailedReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response) {
		partnerAnalyticsService.downloadTrackAssetsDetailedReport(partnerJourneyRequestDTO, response);
	}

	@GetMapping("/journey/download/track-asset-details-report")
	public void downloadTrackAssetsDetailsReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response) {
		partnerAnalyticsService.downloadTrackAssetsDetailsReport(partnerJourneyRequestDTO, response);
	}

	@GetMapping("/journey/download/playbook-asset-details-report")
	public void downloadPlayBookAssetsDetailsReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response) {
		partnerAnalyticsService.downloadPlayBookAssetsDetailsReport(partnerJourneyRequestDTO, response);
	}

	@GetMapping("/journey/download/share-leads-details-report")
	public void downloadShareLeadsDetailsReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response) {
		partnerAnalyticsService.downloadShareLeadsDetailsReport(partnerJourneyRequestDTO, response);
	}

	@GetMapping("/journey/download/lead-details-report")
	public void downloadLeadsDetailsReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response) {
		partnerAnalyticsService.downloadLeadsDetailsReport(partnerJourneyRequestDTO, response);
	}

	@GetMapping("/journey/download/deal-details-report")
	public void downloadDealsDetailsReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response) {
		partnerAnalyticsService.downloadDealsDetailsReport(partnerJourneyRequestDTO, response);
	}

	@GetMapping("/journey/download/mdf-details-report")
	public void downloadMDFDetailsReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response) {
		partnerAnalyticsService.downloadMDFDetailsReport(partnerJourneyRequestDTO, response);
	}

	@GetMapping("/findTotalPartnersCount")
	public ResponseEntity<XtremandResponse> findTotalPartnersCount(@RequestParam Integer userId,
			@RequestParam boolean applyFilter) {
		return ResponseEntity.ok(partnerAnalyticsService.findTotalPartnersCount(userId, applyFilter));
	}

	// XNFR-1006
	@GetMapping("/findTotalDeactivatePartnersCount")
	public ResponseEntity<XtremandResponse> findTotalDeactivatePartnersCount(@RequestParam Integer userId,
			@RequestParam boolean applyFilter) {
		return ResponseEntity.ok(partnerAnalyticsService.findTotalDeactivatePartnersCount(userId, applyFilter));
	}

	@GetMapping("/journey/download/team-members-report")
	public void downloadTeamMembersReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response) {
		partnerAnalyticsService.downloadTeamMembersReport(partnerJourneyRequestDTO, response);
	}

	@GetMapping("/journey/assets/details")
	public ResponseEntity<XtremandResponse> getPartnerJourneyAssetsDetails(
			PartnerJourneyRequestDTO partnerJourneyRequestDTO) {
		return ResponseEntity.ok(partnerAnalyticsService.getPartnerJourneyAssetsDetails(partnerJourneyRequestDTO));
	}

	@GetMapping("/journey/download/asset-details-report")
	public void downloadAssetDetailsReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response) {
		partnerAnalyticsService.downloadAssetDetailsReport(partnerJourneyRequestDTO, response);
	}

	/*** XNFR-835 ***/
	@GetMapping("journey/download/active-partners-report")
	public void downloadActivePartnersReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response) {
		partnerAnalyticsService.downloadActivePartnersReport(partnerJourneyRequestDTO, response);
	}

	/*** XNFR-1006 ***/
	@GetMapping("journey/download/deactivated-partners-report")
	public void downloadDeactivePartnersReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response) {
		partnerAnalyticsService.downloadDeactivePartnersReport(partnerJourneyRequestDTO, response);
	}

	/*** XNFR-1016 ***/
	@GetMapping("journey/download/all-partners-report")
	public void downloadAllPartnersDetailsReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response) {
		partnerAnalyticsService.downloadAllPartnersDetailsReport(partnerJourneyRequestDTO, response);
	}

	@GetMapping("journey/download/inactive-partners-report")
	public void downloadInActivePartnersReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response) {
		partnerAnalyticsService.downloadInActivePartnersReport(partnerJourneyRequestDTO, response);
	}

	@GetMapping("journey/download/company-profile-incomplete-partners-report")
	public void downloadCompanyProfileIncompletePartnersReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response) {
		partnerAnalyticsService.downloadCompanyProfileIncompletePartnersReport(partnerJourneyRequestDTO, response);
	}

	/*** XNFR-835 ***/
	/**** XNFR-944 ***/
	@PostMapping(value = "/allPartners/details/regionwise/count")
	public ResponseEntity<XtremandResponse> findAllPartnerDetails(
			@RequestBody PartnerJourneyRequestDTO partnerJourneyRequestDTO) {
		return ResponseEntity.ok(partnerAnalyticsService.findAllPartnerDetails(partnerJourneyRequestDTO));
	}

	/**** XNFR-944 ***/
	@PostMapping(value = "/allPartners/details/list")
	public ResponseEntity<XtremandResponse> getAllPartnersDetailsList(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(partnerAnalyticsService.getAllPartnersDetailsList(pagination));
	}

	/**** XNFR-944 ***/
	@GetMapping(value = "/allPartners/downloadCsv")
	public ResponseEntity<Void> allpartnersDownloadCsv(@RequestParam("userId") Integer userId,
			@RequestParam("regionFilter") String regionFilter,
			@RequestParam(value = "sortColumn", required = false) String sortColumn,
			@RequestParam(value = "selectedRegionIds", required = false) String selectedRegionIds,
			@RequestParam(value = "selectedStatusIds", required = false) String selectedStatusIds,
			@RequestParam(value = "partnerTeamMemberGroupFilter", required = false) boolean partnerTeamMemberGroupFilter,
			@Valid Pageable pageable, HttpServletResponse response) {

		List<String> selectedRegionIdList = new ArrayList<>();
		if (StringUtils.hasText(selectedRegionIds)) {
			selectedRegionIdList = Arrays.asList(selectedRegionIds.split(","));
		}

		List<String> selectedStatusIdList = new ArrayList<>();
		if (StringUtils.hasText(selectedStatusIds)) {
			selectedStatusIdList = Arrays.asList(selectedStatusIds.split(","));
		}
		partnerAnalyticsService.allpartnersDownloadCsv(userId, regionFilter, sortColumn, selectedRegionIdList,
				selectedStatusIdList, partnerTeamMemberGroupFilter, pageable, response);
		StatusCode statusCode = StatusCode.getstatuscode(StatusCodeConstants.DOWNLOAD_USERLIST_SUCCESS);
		return ResponseEntity.ok().headers(HttpHeaderUtil.getHeader(statusCode)).body(null);

	}

	/** XNFR-952 **/
	@GetMapping("/listAllPartnersForContactUploadManagementSettings/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> listAllPartnersForContactUploadManagementSettings(
			@PathVariable Integer loggedInUserId, @Valid Pageable pageable) {
		try {
			return ResponseEntity.ok(partnerAnalyticsService.listAllPartnersForContactUploadManagementSettings(pageable,
					loggedInUserId));
		} catch (BadRequestException e) {
			throw new BadRequestException(e.getMessage());
		} catch (Exception e) {
			throw new XamplifyDataAccessException(e.getMessage());
		}
	}

	/** XNFR-952 **/
	@PostMapping("saveOrUpdateContactUploadSettings/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> saveOrUpdateContactUploadSettings(@PathVariable Integer loggedInUserId,
			@RequestBody List<PartnerContactUsageDTO> partnerContactUsageDTOs) {
		try {
			return ResponseEntity.ok(partnerAnalyticsService
					.saveOrUpdateContactUploadSManagementSettings(loggedInUserId, partnerContactUsageDTOs));
		} catch (BadRequestException e) {
			throw new BadRequestException(e.getMessage());
		} catch (Exception e) {
			throw new XamplifyDataAccessException(e.getMessage());
		}
	}

	/** XNFR-952 **/
	@GetMapping("/fetchTotalNumberOfContactsAddedForCompany/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> getContacts(@PathVariable Integer loggedInUserId) {
		try {
			return ResponseEntity.ok(partnerAnalyticsService.fetchTotalNumberOfContactsAddedForCompany(loggedInUserId));
		} catch (BadRequestException e) {
			throw new BadRequestException(e.getMessage());
		} catch (Exception e) {
			throw new XamplifyDataAccessException(e.getMessage());
		}
	}

	@GetMapping("/loadContactsUploadedCountByAllPartners/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> loadContactsUploadedCountByAllPartners(
			@PathVariable Integer loggedInUserId) {
		try {
			return ResponseEntity.ok(partnerAnalyticsService.loadContactsUploadedCountByAllPartners(loggedInUserId));
		} catch (BadRequestException e) {
			throw new BadRequestException(e.getMessage());
		} catch (Exception e) {
			throw new XamplifyDataAccessException(e.getMessage());
		}
	}

	@GetMapping("/loadContactUploadSubscriptionLimitForCompany/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> loadContactUploadSubscriptionLimitForCompany(
			@PathVariable Integer loggedInUserId) {
		try {
			return ResponseEntity
					.ok(partnerAnalyticsService.loadContactUploadSubscriptionLimitForCompany(loggedInUserId));
		} catch (BadRequestException e) {
			throw new BadRequestException(e.getMessage());
		} catch (Exception e) {
			throw new XamplifyDataAccessException(e.getMessage());
		}
	}

	@GetMapping("getTotalContactSubscriptionLimitUsedByCompany/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> getTotalContactSubscriptionLimitUsedByCompany(
			@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(partnerAnalyticsService.getNumberOfContactSubscriptionUsedByCompany(loggedInUserId));
	}

	@PostMapping(value = "journey/asset/names/filter")
	public ResponseEntity<XtremandResponse> getAllAssetNamesForFilter(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(partnerAnalyticsService.getAllAssetNamesForFilter(pagination));
	}

	@PostMapping(value = "journey/email/ids/filter")
	public ResponseEntity<XtremandResponse> getAllEmailIdsForFilter(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(partnerAnalyticsService.getAllEmailIdsForFilter(pagination));
	}

	// XNFR - 989
	@PostMapping("asset/journey/asset/details/list")
	public ResponseEntity<XtremandResponse> getAssetJourneyAssetsDetails(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(partnerAnalyticsService.getAssetJourneyAssetsDetails(pagination));
	}

	@GetMapping("/asset/journey/asset/details/download/asset-details-report")
	public void downloadAssetJourneyAssetDetailsReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response) {
		partnerAnalyticsService.downloadAssetJourneyAssetDetailsReport(partnerJourneyRequestDTO, response);
	}

	// XNFR - 1005
	@PostMapping("playbook/journey/interaction/details/list")
	public ResponseEntity<XtremandResponse> getPlaybookJourneyInteractionDetails(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(partnerAnalyticsService.getPlaybookJourneyInteractionDetails(pagination));
	}

	@GetMapping("/playbook/journey/interaction/details/download")
	public void downloadPlaybookJourneyInteractionDetailsReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response) {
		partnerAnalyticsService.downloadPlaybookJourneyInteractionDetailsReport(partnerJourneyRequestDTO, response);
	}

	@PostMapping(value = "playbook/names/filter")
	public ResponseEntity<XtremandResponse> getAllPlaybookNamesForFilter(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(partnerAnalyticsService.getAllPlaybookNamesForFilter(pagination));
	}
	
	@RequestMapping(value = "/inactive-partner-analytics", method = RequestMethod.POST)
	public ResponseEntity<?> inActivePartnerAnalytics(@RequestBody Pagination pagination) {
		Map<String, Object> resultMap = partnerAnalyticsService.getInActivePartnersAnalytics(pagination);
		return ResponseEntity.status(HttpStatus.OK).body(resultMap);
	}
	
}
