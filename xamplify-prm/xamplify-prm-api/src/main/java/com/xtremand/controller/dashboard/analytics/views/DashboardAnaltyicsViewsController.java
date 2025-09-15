package com.xtremand.controller.dashboard.analytics.views;

import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xtremand.dashboard.analytics.views.dto.PartnerAnalyticsCountDTO;
import com.xtremand.dashboard.analytics.views.service.DashboardAnalyticsViewsService;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.log.bom.VideoStats;
import com.xtremand.module.service.ModuleService;
import com.xtremand.util.dto.Pageable;
import com.xtremand.util.dto.WelcomePageItem;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;

@RestController
@RequestMapping("/dashboard/views/")
public class DashboardAnaltyicsViewsController {

	@Autowired
	private DashboardAnalyticsViewsService dashboardAnalyticsViewsService;

	@Autowired
	private ModuleService moduleService;

	@PostMapping("/modulesAnalytics")
	public ResponseEntity<XtremandResponse> listDashboardModulesAnalytics(@RequestBody VanityUrlDetailsDTO dto) {
		return ResponseEntity.ok(dashboardAnalyticsViewsService.getDashboardModuleViewByCompanyId(dto));
	}

	@GetMapping("/getVendorCompanyDetails/{userId}")
	public ResponseEntity<XtremandResponse> getVendorCompanyDetails(@PathVariable Integer userId) {
		return ResponseEntity.ok(dashboardAnalyticsViewsService.listVendorCompanyDetailsByUserId(userId));
	}

	@PostMapping("/emailStats")
	public ResponseEntity<XtremandResponse> getEmailStats(@RequestBody VanityUrlDetailsDTO dto) {
		return ResponseEntity.ok(dashboardAnalyticsViewsService.getEmailStats(dto));
	}

	@PostMapping("/regionalStatistics")
	public ResponseEntity<XtremandResponse> getRegionalStatistics(@RequestBody VanityUrlDetailsDTO dto) {
		return ResponseEntity.ok(dashboardAnalyticsViewsService.getRegionalStatistics(dto));
	}

	@GetMapping("/opportunities/vendor/analytics/{userId}")
	public ResponseEntity<XtremandResponse> getOpportunitiesVendorAnalyticsView(@PathVariable Integer userId) {
		return ResponseEntity.ok(dashboardAnalyticsViewsService.getOpportunitiesVendorAnalytics(userId));
	}

	@PostMapping("/opportunities/partner/analytics")
	public ResponseEntity<XtremandResponse> getOpportunitiesPartnerAnalyticsView(@RequestBody VanityUrlDetailsDTO dto) {
		return ResponseEntity.ok(dashboardAnalyticsViewsService.getOpportunitiesPartnerAnalytics(dto));
	}

	/***********
	 * dashboard page email open logs data for a logged in user
	 ********************/
	@PostMapping(value = "/email-logs-by-user-and-action")
	public ResponseEntity<?> listEmailOpenLogs(@RequestBody VanityUrlDetailsDTO dto, @RequestParam Integer actionId,
			@RequestParam Integer pageSize, @RequestParam Integer pageNumber) {
		return ResponseEntity.status(HttpStatus.OK)
				.body(dashboardAnalyticsViewsService.listEmailOpenLogs(dto, actionId, pageSize, pageNumber));
	}

	/***********
	 * dashboard page email GifClicked & UrlClicked logs for a logged in user
	 ********************/
	@PostMapping(value = "/email-click-logs-by-user")
	public ResponseEntity<?> listEmailGifClickedUrlClickedLogsByUser(@RequestBody VanityUrlDetailsDTO dto,
			@RequestParam Integer pageSize, @RequestParam Integer pageNumber) {
		return ResponseEntity.status(HttpStatus.OK)
				.body(dashboardAnalyticsViewsService.listEmailGifClickedUrlClickedLogs(dto, pageSize, pageNumber));
	}

	@PostMapping(value = "/watched-users")
	public ResponseEntity<?> listWatchedUsers(@RequestBody VanityUrlDetailsDTO dto, Integer pageSize,
			Integer pageNumber) {
		return ResponseEntity.status(HttpStatus.OK)
				.body(dashboardAnalyticsViewsService.listWatchedUsersByUser(dto, pageSize, pageNumber));
	}

	/*************************
	 * dashboard page world-map detail report
	 *****************************/
	@PostMapping(value = "/world-map-detail-report")
	public ResponseEntity<?> getDashboardWorldMapDetailReport(@RequestBody VanityUrlDetailsDTO dto,
			@RequestParam String countryCode, @RequestParam Integer pageSize, @RequestParam Integer pageNumber) {
		return new ResponseEntity<>(
				dashboardAnalyticsViewsService.getDashboardWorldMapDetailReport(dto, pageSize, pageNumber, countryCode),
				HttpStatus.OK);

	}

	/*********** dashboard videostats data **********************/
	@PostMapping(value = "/videostats-data")
	public ResponseEntity<?> getDashboardVideoStatsData(@RequestBody VanityUrlDetailsDTO dto,
			@RequestParam Integer daysInterval) {
		VideoStats videoStats = dashboardAnalyticsViewsService.getDashboardVideoStatsData(dto, daysInterval);
		return ResponseEntity.status(HttpStatus.OK).body(videoStats);

	}


	/***** Welcome page items ***********/
	@PostMapping(value = "/getWelcomePageItems")
	public ResponseEntity<WelcomePageItem> getWelcomePageItems(@RequestBody VanityUrlDetailsDTO dto) {
		return new ResponseEntity<>(moduleService.getWelcomePageItems(dto), HttpStatus.OK);
	}

	@GetMapping(value = "/getActiveInActiveTotalPartnerCounts/{userId}/{applyFilter}")
	public ResponseEntity<PartnerAnalyticsCountDTO> getActiveInActiveTotalPartnerCounts(@PathVariable Integer userId,
			@PathVariable boolean applyFilter) {
		return new ResponseEntity<>(
				dashboardAnalyticsViewsService.getActiveInActiveTotalPartnerCounts(userId, applyFilter), HttpStatus.OK);
	}



	@GetMapping(value = "/getDealBubbleChartData/{userId}/{applyFilter}")
	public ResponseEntity<Map<String, Object>> getDealBubbleChartData(@PathVariable Integer userId,
			@PathVariable boolean applyFilter) {
		return new ResponseEntity<>(
				dashboardAnalyticsViewsService.findDataForDealOrLeadBubbleChart(userId, "d", applyFilter),
				HttpStatus.OK);
	}

	@GetMapping(value = "/getLeadBubbleChartData/{userId}/{applyFilter}")
	public ResponseEntity<Map<String, Object>> getLeadBubbleChartData(@PathVariable Integer userId,
			@PathVariable boolean applyFilter) {
		return new ResponseEntity<>(
				dashboardAnalyticsViewsService.findDataForDealOrLeadBubbleChart(userId, "l", applyFilter),
				HttpStatus.OK);
	}

	/*** Funnel Chart Analytics ***/
	@PostMapping(value = "/getFunnelChartsAnalyticsData")
	public ResponseEntity<XtremandResponse> getFunnelChartsAnalyticsData(
			@RequestBody VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		return new ResponseEntity<>(dashboardAnalyticsViewsService.getFunnelChartsAnalytics(vanityUrlDetailsDTO),
				HttpStatus.OK);
	}

	/*** Pie Chart Analytics ***/
	@PostMapping(value = "/getPieChartsLeadsAnalyticsData")
	public ResponseEntity<XtremandResponse> getPieChartsLeadsAnalyticsData(
			@RequestBody VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		return new ResponseEntity<>(dashboardAnalyticsViewsService.getPieChartsLeadsAnalytics(vanityUrlDetailsDTO),
				HttpStatus.OK);
	}

	@PostMapping(value = "/getPieChartsDealsAnalyticsData")
	public ResponseEntity<XtremandResponse> getPIeChartsDealsAnalyticsData(
			@RequestBody VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		return new ResponseEntity<>(dashboardAnalyticsViewsService.getPieChartsDealsAnalytics(vanityUrlDetailsDTO),
				HttpStatus.OK);
	}

	@PostMapping(value = "/getPieChartStatisticsLeadAnalyticsData")
	public ResponseEntity<XtremandResponse> getPieChartStatisticsLeadAnalyticsData(
			@RequestBody VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		return new ResponseEntity<>(
				dashboardAnalyticsViewsService.getPieChartsStatisticsLeadAnalytics(vanityUrlDetailsDTO), HttpStatus.OK);
	}

	@PostMapping(value = "/getPieChartDealStatisticsData")
	public ResponseEntity<XtremandResponse> getPieChartDealStatisticsData(
			@RequestBody VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		return new ResponseEntity<>(
				dashboardAnalyticsViewsService.getPieChartsDealStatisticsAnalytics(vanityUrlDetailsDTO), HttpStatus.OK);
	}

	@PostMapping(value = "/getPieChartDealStatisticsWithStageNames")
	public ResponseEntity<XtremandResponse> getPieChartDealStatisticsWithStageNames(
			@RequestBody VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		return new ResponseEntity<>(
				dashboardAnalyticsViewsService.getPieChartsDealStatisticsWithStageNames(vanityUrlDetailsDTO),
				HttpStatus.OK);
	}

	@PostMapping(value = "/getPieChartLeadsStatisticsWithStageNames")
	public ResponseEntity<XtremandResponse> getPieChartLeadsStatisticsWithStageNames(
			@RequestBody VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		return new ResponseEntity<>(
				dashboardAnalyticsViewsService.getPieChartsLeadsStatisticsWithStageNames(vanityUrlDetailsDTO),
				HttpStatus.OK);
	}

	@GetMapping("findAllQuickLinks/domainName/{domainName}/userId/{userId}")
	public ResponseEntity<XtremandResponse> findAllQuickLinks(@PathVariable String domainName,
			@PathVariable Integer userId, @Valid Pageable pageable, BindingResult result) {
		return new ResponseEntity<>(
				dashboardAnalyticsViewsService.findAllQuickLinks(pageable, domainName, userId, result), HttpStatus.OK);
	}

	@GetMapping("findUniversalSearch/{userId}")
	public ResponseEntity<XtremandResponse> universalSearch(
			@RequestParam(required = false, defaultValue = "") String domainName, @PathVariable Integer userId,
			@Valid Pageable pageable, BindingResult result) {
		return new ResponseEntity<>(
				dashboardAnalyticsViewsService.universalSearch(pageable, domainName, userId, result), HttpStatus.OK);
	}

}
