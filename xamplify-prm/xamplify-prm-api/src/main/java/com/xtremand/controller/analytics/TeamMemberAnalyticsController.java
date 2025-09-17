package com.xtremand.controller.analytics;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xtremand.analytics.service.TeamMemberAnalyticsService;
import com.xtremand.common.bom.Pagination;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.util.dto.TeamMemberAnalyticsRequestDTO;

@RestController
@RequestMapping("/teamMemberAnalytics")
public class TeamMemberAnalyticsController {

	private static final Logger logger = LoggerFactory.getLogger(PartnerAnalyticsController.class);

	@Autowired
	private TeamMemberAnalyticsService teamMemberAnalyticsService;

	@PostMapping(value = "/counts")
	public ResponseEntity<XtremandResponse> getTeamMemberJourneyCounts(
			@RequestBody TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO) {
		return ResponseEntity.ok(teamMemberAnalyticsService.getTeamMemberJourneyCounts(teamMemberJourneyRequestDTO));
	}

	@PostMapping(value = "v/counts")
	public ResponseEntity<XtremandResponse> getTeamMemberJourneyCountsForVendor(
			@RequestBody TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO) {
		return ResponseEntity
				.ok(teamMemberAnalyticsService.getTeamMemberJourneyCountsForVendor(teamMemberJourneyRequestDTO));
	}

	@PostMapping(value = "/track/interaction/counts")
	public ResponseEntity<XtremandResponse> getTeamMemberTrackCountsByInteraction(
			@RequestBody TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO) {
		return ResponseEntity
				.ok(teamMemberAnalyticsService.getTeamMemberTrackCountsByInteraction(teamMemberJourneyRequestDTO));
	}

	@PostMapping(value = "v/track/interaction/counts")
	public ResponseEntity<XtremandResponse> getVendorTeamMemberTrackCountsByInteraction(
			@RequestBody TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO) {
		return ResponseEntity.ok(
				teamMemberAnalyticsService.getVendorTeamMemberTrackCountsByInteraction(teamMemberJourneyRequestDTO));
	}

	@PostMapping(value = "/track/interaction")
	public ResponseEntity<XtremandResponse> getTeamMemberTrackDetailsByInteraction(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(teamMemberAnalyticsService.getTeamMemberTrackDetailsByInteraction(pagination));
	}

	@PostMapping(value = "v/track/interaction")
	public ResponseEntity<XtremandResponse> getVendorTeamMemberTrackDetailsByInteraction(
			@RequestBody Pagination pagination) {
		return ResponseEntity.ok(teamMemberAnalyticsService.getVendorTeamMemberTrackDetailsByInteraction(pagination));
	}

	@PostMapping(value = "track/typewise/counts")
	public ResponseEntity<XtremandResponse> getTeamMemberTrackCountsByType(
			@RequestBody TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO) {
		return ResponseEntity
				.ok(teamMemberAnalyticsService.getTeamMemberTrackCountsByType(teamMemberJourneyRequestDTO));
	}

	@PostMapping(value = "track/content/typewise")
	public ResponseEntity<XtremandResponse> getTeamMemberTrackAssetDetailsByType(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(teamMemberAnalyticsService.getTeamMemberTrackAssetDetailsByType(pagination));
	}

	@PostMapping(value = "v/track/typewise/counts")
	public ResponseEntity<XtremandResponse> getVendorTeamMemberTrackCountsByType(
			@RequestBody TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO) {
		return ResponseEntity
				.ok(teamMemberAnalyticsService.getVendorTeamMemberTrackCountsByType(teamMemberJourneyRequestDTO));
	}

	@PostMapping(value = "v/track/content/typewise")
	public ResponseEntity<XtremandResponse> getVendorTeamMemberTrackAssetDetailsByType(
			@RequestBody Pagination pagination) {
		return ResponseEntity.ok(teamMemberAnalyticsService.getVendorTeamMemberTrackAssetDetailsByType(pagination));
	}

	@PostMapping(value = "track/userwise/count")
	public ResponseEntity<XtremandResponse> getTeamMemberWiseTracksCount(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(teamMemberAnalyticsService.getTeamMemberTracksCount(pagination));
	}

	@PostMapping(value = "playbook/userwise/count")
	public ResponseEntity<XtremandResponse> getTeamMemberWisePlaybooksCount(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(teamMemberAnalyticsService.getTeamMemberWisePlaybooksCount(pagination));
	}

	@PostMapping(value = "v/track/userwise/count")
	public ResponseEntity<XtremandResponse> getVendorTeamMemberTracksCount(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(teamMemberAnalyticsService.getVendorTeamMemberTracksCount(pagination));
	}

	@PostMapping(value = "v/playbook/userwise/count")
	public ResponseEntity<XtremandResponse> getVendorTeamMemberWisePlaybooksCount(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(teamMemberAnalyticsService.getVendorTeamMemberWisePlaybooksCount(pagination));
	}

	@PostMapping(value = "track/userwise/details")
	public ResponseEntity<XtremandResponse> getTeamMemberTrackDetails(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(teamMemberAnalyticsService.getTeamMemberTrackDetails(pagination));
	}

	@PostMapping(value = "v/track/userwise/details")
	public ResponseEntity<XtremandResponse> getVendorTeamMemberTrackDetails(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(teamMemberAnalyticsService.getVendorTeamMemberTrackDetails(pagination));
	}

	@PostMapping(value = "track/asset/details")
	public ResponseEntity<XtremandResponse> getTeamMemberTrackAssetDetails(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(teamMemberAnalyticsService.getTeamMemberTrackAssetDetails(pagination));
	}

	@PostMapping(value = "playbook/asset/details")
	public ResponseEntity<XtremandResponse> getTeamMemberPlaybookAssetDetails(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(teamMemberAnalyticsService.getTeamMemberPlaybookAssetDetails(pagination));
	}

	@PostMapping(value = "v/track/asset/details")
	public ResponseEntity<XtremandResponse> getVendorTeamMemberTrackAssetDetails(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(teamMemberAnalyticsService.getVendorTeamMemberTrackAssetDetails(pagination));
	}

	@PostMapping(value = "v/playbook/asset/details")
	public ResponseEntity<XtremandResponse> getVendorTeamMemberPlaybookAssetDetails(
			@RequestBody Pagination pagination) {
		return ResponseEntity.ok(teamMemberAnalyticsService.getVendorTeamMemberPlaybookAssetDetails(pagination));
	}

	@PostMapping(value = "share/lead/details")
	public ResponseEntity<XtremandResponse> getTeamMemberShareLeadDetails(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(teamMemberAnalyticsService.getTeamMemberShareLeadDetails(pagination));
	}

	@PostMapping(value = "v/share/lead/details")
	public ResponseEntity<XtremandResponse> getVendorTeamMemberShareLeadDetails(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(teamMemberAnalyticsService.getVendorTeamMemberShareLeadDetails(pagination));
	}

	@PostMapping(value = "redistributed/campaign/details")
	public ResponseEntity<XtremandResponse> getTeamMemberRedistributedCampaignDetails(
			@RequestBody Pagination pagination) {
		return ResponseEntity.ok(teamMemberAnalyticsService.getTeamMemberRedistributedCampaignDetails(pagination));
	}

	@PostMapping(value = "v/launched/campaign/details")
	public ResponseEntity<XtremandResponse> getVendorTeamMemberLaunchedCampaignDetails(
			@RequestBody Pagination pagination) {
		return ResponseEntity.ok(teamMemberAnalyticsService.getVendorTeamMemberLaunchedCampaignDetails(pagination));
	}

	@PostMapping(value = "lead/details")
	public ResponseEntity<XtremandResponse> getTeamMemberLeadDetails(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(teamMemberAnalyticsService.getTeamMemberLeadDetails(pagination));
	}

	@PostMapping(value = "deal/details")
	public ResponseEntity<XtremandResponse> getTeamMemberDealDetails(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(teamMemberAnalyticsService.getTeamMemberDealDetails(pagination));
	}

	@PostMapping(value = "v/lead/details")
	public ResponseEntity<XtremandResponse> getVendorTeamMemberLeadDetails(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(teamMemberAnalyticsService.getVendorTeamMemberLeadDetails(pagination));
	}

	@PostMapping(value = "v/deal/details")
	public ResponseEntity<XtremandResponse> getVendorTeamMemberDealDetails(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(teamMemberAnalyticsService.getVendorTeamMemberDealDetails(pagination));
	}

	@PostMapping(value = "vendor/details/filter")
	public ResponseEntity<XtremandResponse> getVendorInfoForFilter(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(teamMemberAnalyticsService.getVendorInfoForFilter(pagination));
	}

	@PostMapping(value = "teamMember/details/filter")
	public ResponseEntity<XtremandResponse> getTeamMemberInfoForFilter(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(teamMemberAnalyticsService.getTeamMemberInfoForFilter(pagination));
	}

	@PostMapping(value = "mdf/details")
	public ResponseEntity<XtremandResponse> getTeamMemberMdfDetails(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(teamMemberAnalyticsService.getTeamMemberMdfDetails(pagination));
	}

	@PostMapping(value = "v/mdf/details")
	public ResponseEntity<XtremandResponse> getVendorTeamMemberMdfDetails(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(teamMemberAnalyticsService.getVendorTeamMemberMdfDetails(pagination));
	}

	@PostMapping("/getLeadsAndDealsCount/{filterType}")
	public ResponseEntity<XtremandResponse> getLeadsAndDealsCount(
			@RequestBody TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO, @PathVariable String filterType) {
		return ResponseEntity.ok(teamMemberAnalyticsService
				.findLeadsAndDealsCountForTeamMember(teamMemberJourneyRequestDTO, filterType));
	}

	@PostMapping("/getAllLeadsAndDealsCount/{filterType}")
	public ResponseEntity<XtremandResponse> getAllLeadsAndDealsCount(
			@RequestBody TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO, @PathVariable String filterType) {
		return ResponseEntity.ok(teamMemberAnalyticsService
				.findAllLeadsAndDealsCountForTeamMember(teamMemberJourneyRequestDTO, filterType));
	}

	@PostMapping(value = "v/contacts/details")
	public ResponseEntity<XtremandResponse> getVendorTeamMemberContactsDetails(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(teamMemberAnalyticsService.getVendorTeamMemberContactsDetails(pagination));
	}

	@PostMapping(value = "contacts/details")
	public ResponseEntity<XtremandResponse> getContactsDetailsForTeamMember(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(teamMemberAnalyticsService.getContactsDetailsForTeamMember(pagination));
	}

	@PostMapping(value = "all/onboard/partner/details")
	public ResponseEntity<XtremandResponse> getVendorTeamMemberallPartnersDetails(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(teamMemberAnalyticsService.getVendorTeamMemberallPartnersDetails(pagination));
	}

	@PostMapping(value = "v/assets/count")
	public ResponseEntity<XtremandResponse> getVendorTeamMemberAssetsCount(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(teamMemberAnalyticsService.getVendorTeamMemberAssetsCount(pagination));
	}

	@PostMapping(value = "v/assets/details")
	public ResponseEntity<XtremandResponse> getVendorTeamMemberAssetsDetails(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(teamMemberAnalyticsService.getVendorTeamMemberAssetsDetails(pagination));
	}

	@PostMapping(value = "company/details")
	public ResponseEntity<XtremandResponse> getCompanyDetailsForTeamMember(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(teamMemberAnalyticsService.getCompanyDetailsForTeamMember(pagination));
	}

	@GetMapping("/download/track-interaction-report")
	public void downloadTrackInteractionAndNonInteractionReport(
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO, HttpServletResponse response) {
		teamMemberAnalyticsService.downloadTrackInteractionAndNonInteractionReport(teamMemberJourneyRequestDTO,
				response);
	}

	@GetMapping("/download/typewise-asset-details-report")
	public void downloadTrackAsserDetailsReport(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO,
			HttpServletResponse response) {
		teamMemberAnalyticsService.downloadTrackAsserDetailsReport(teamMemberJourneyRequestDTO, response);
	}

	@GetMapping("/download/track-counts-report")
	public void downloadUserWiseTrackCountReport(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO,
			HttpServletResponse response) {
		teamMemberAnalyticsService.downloadUserWiseTrackCountReport(teamMemberJourneyRequestDTO, response);
	}

	@GetMapping("/download/playbook-counts-report")
	public void downloadUserWisePlayBookCountReport(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO,
			HttpServletResponse response) {
		teamMemberAnalyticsService.downloadUserWisePlayBookCountReport(teamMemberJourneyRequestDTO, response);
	}

	@GetMapping("/download/track-assets-detailed-report")
	public void downloadTrackAssetsDetailedReport(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO,
			HttpServletResponse response) {
		teamMemberAnalyticsService.downloadTrackAssetsDetailedReport(teamMemberJourneyRequestDTO, response);
	}

	@GetMapping("/download/track-asset-details-report")
	public void downloadTrackAssetsDetailsReport(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO,
			HttpServletResponse response) {
		teamMemberAnalyticsService.downloadTrackAssetsDetailsReport(teamMemberJourneyRequestDTO, response);
	}

	@GetMapping("/download/playbook-asset-details-report")
	public void downloadPlayBookAssetsDetailsReport(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO,
			HttpServletResponse response) {
		teamMemberAnalyticsService.downloadPlayBookAssetsDetailsReport(teamMemberJourneyRequestDTO, response);
	}

	@GetMapping("/download/share-leads-details-report")
	public void downloadShareLeadsDetailsReport(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO,
			HttpServletResponse response) {
		teamMemberAnalyticsService.downloadShareLeadsDetailsReport(teamMemberJourneyRequestDTO, response);
	}

	@GetMapping("/download/leads-details-report")
	public void downloadLeadsDetailsReport(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO,
			HttpServletResponse response) {
		teamMemberAnalyticsService.downloadLeadsDetailsReport(teamMemberJourneyRequestDTO, response);
	}

	@GetMapping("/download/deal-details-report")
	public void downloadDealDetailsReport(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO,
			HttpServletResponse response) {
		teamMemberAnalyticsService.downloadDealDetailsReport(teamMemberJourneyRequestDTO, response);
	}

	@GetMapping("/download/mdf-details-report")
	public void downloadMDFDetailsReport(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO,
			HttpServletResponse response) {
		teamMemberAnalyticsService.downloadMDFDetailsReport(teamMemberJourneyRequestDTO, response);
	}

	@GetMapping("/download/contact-details-report")
	public void downloadContactDetailsReport(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO,
			HttpServletResponse response) {
		teamMemberAnalyticsService.downloadContactDetailsReport(teamMemberJourneyRequestDTO, response);
	}

	@GetMapping("/download/company-details-report")
	public void downloadCompanyDetailsReport(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO,
			HttpServletResponse response) {
		teamMemberAnalyticsService.downloadCompanyDetailsReport(teamMemberJourneyRequestDTO, response);
	}

	@GetMapping("/download/asset-analytics-report")
	public void downloadAssetCountReport(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO,
			HttpServletResponse response) {
		teamMemberAnalyticsService.downloadAssetCountReport(teamMemberJourneyRequestDTO, response);
	}

	@GetMapping("/download/asset-details-report")
	public void downloadAssetDetailsReport(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO,
			HttpServletResponse response) {
		teamMemberAnalyticsService.downloadAssetDetailsReport(teamMemberJourneyRequestDTO, response);
	}

	@GetMapping("/download/all-partners-details-report")
	public void downloadAllPartnersDetailsReport(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO,
			HttpServletResponse response) {
		teamMemberAnalyticsService.downloadAllPartnersDetailsReport(teamMemberJourneyRequestDTO, response);
	}

	@GetMapping("/assets/details")
	public ResponseEntity<XtremandResponse> getTeamMemberAssetsDetails(
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO) {
		return ResponseEntity.ok(teamMemberAnalyticsService.getTeamMemberAssetsDetails(teamMemberJourneyRequestDTO));
	}

	@GetMapping("/download/team-asset-details-report")
	public void downloadTeamMemberAssetDetailsReport(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO,
			HttpServletResponse response) {
		teamMemberAnalyticsService.downloadTeamMemberAssetDetailsReport(teamMemberJourneyRequestDTO, response);
	}

}
