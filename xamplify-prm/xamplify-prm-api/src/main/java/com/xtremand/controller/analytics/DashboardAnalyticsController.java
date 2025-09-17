package com.xtremand.controller.analytics;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xtremand.common.bom.Pagination;
import com.xtremand.formbeans.HeatMapData;
import com.xtremand.log.bom.VideoStats;
import com.xtremand.log.service.XamplifyLogService;

@RestController
public class DashboardAnalyticsController {

	private static final Logger logger = LoggerFactory.getLogger(DashboardAnalyticsController.class);

	@Autowired
	XamplifyLogService xamplifyLogService;

	/***********
	 * dashboard page videos, video views, contacts, email templates, campaigns,
	 * social connections count by logged in user
	 ********************/
	@RequestMapping(value = "dashboard/analytics_count", method = RequestMethod.GET)
	public ResponseEntity<?> getDashboardPageAnalyticsCount(@RequestParam Integer userId) {
		try {
			return new ResponseEntity<>(xamplifyLogService.getDashboardPageAnalyticsCount(userId), HttpStatus.OK);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", e.getMessage()));
		}
	}

	/***********
	 * dashboard page country wise Users Count in world map by logged in user
	 ********************/
	@RequestMapping(value = "dashboard/countrywise_users_count", method = RequestMethod.GET)
	public ResponseEntity<?> countrywiseUsersCount(@RequestParam Integer userId) {
		try {
			return new ResponseEntity<>(xamplifyLogService.countrywiseUsersCount(userId), HttpStatus.OK);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", e.getMessage()));
		}
	}

	/*************************
	 * dashboard page world-map detail report
	 *****************************/
	@RequestMapping(value = "dashboard/world-map-detail-report", method = RequestMethod.POST)
	public ResponseEntity<?> getDashboardWorldMapDetailReport(@RequestBody Pagination pagination,
			@RequestParam Integer userId, @RequestParam String countryCode) {
		try {
			return new ResponseEntity<>(
					xamplifyLogService.getDashboardWorldMapDetailReport(userId, countryCode, pagination),
					HttpStatus.OK);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", e.getMessage()));
		}
	}

	/*********** dashboard heatmap data **********************/
	@RequestMapping(value = "dashboard/heatmap-data", method = RequestMethod.GET)
	public ResponseEntity<?> getDashboardHeatMapData(@RequestParam Integer userId, @RequestParam String limit) {
		logger.debug("entered into getDashboardHeatMapData() with userid:  " + userId);
		try {
			List<HeatMapData> heatMapList = xamplifyLogService.getDashboardHeatMapData(userId, limit);
			return ResponseEntity.status(HttpStatus.OK).body(Collections.singletonMap("heatMapData", heatMapList));
		} catch (Exception e) {
			logger.error("error occured in getDashboardHeatMapData(): " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", e.getMessage()));
		}
	}

	/*********** dashboard bar chart data **********************/
	@RequestMapping(value = "dashboard/barChart-data", method = RequestMethod.POST)
	public ResponseEntity<?> getDashboardBarChartData(@RequestParam Integer userId,
			@RequestBody List<Integer> campaignIdsList) {
		logger.debug("entered into getDashboardBarChartData() with userid:  " + userId);
		try {
			Map<String, Object> resultMap = xamplifyLogService.getDashboardBarChartData(userId, campaignIdsList);
			return ResponseEntity.status(HttpStatus.OK).body(resultMap);
		} catch (Exception e) {
			logger.error("error occured in getDashboardBarChartData(): " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", e.getMessage()));
		}
	}

	/*********** dashboard videostats data **********************/
	@RequestMapping(value = "dashboard/videostats-data", method = RequestMethod.GET)
	public ResponseEntity<?> getDashboardVideoStatsData(@RequestParam Integer userId,
			@RequestParam Integer daysInterval) {
		logger.debug("entered into getDashboardVideoStatsData() with userid:  " + userId);
		try {
			VideoStats videoStats = xamplifyLogService.getDashboardVideoStatsData(userId, daysInterval);
			return ResponseEntity.status(HttpStatus.OK).body(videoStats);
		} catch (Exception e) {
			logger.error("error occured in getDashboardVideoStatsData(): " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", e.getMessage()));
		}
	}

	@RequestMapping(value = "dashboard/watched-users-count/{userId}", method = RequestMethod.GET)
	public ResponseEntity<?> getWatchedUsersCountByUser(@PathVariable Integer userId) {
		try {
			Integer count = xamplifyLogService.getWatchedUsersCountByUser(userId);
			return ResponseEntity.status(HttpStatus.OK).body(Collections.singletonMap("watched-users-count", count));
		} catch (Exception e) {
			logger.error("Error In listWatchedUsersByUser() method ", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", e.getMessage()));
		}
	}

	@RequestMapping(value = "dashboard/watched-users/{userId}", method = RequestMethod.GET)
	public ResponseEntity<?> listWatchedUsersByUser(@PathVariable Integer userId, Integer pageSize,
			Integer pageNumber) {
		try {
			return ResponseEntity.status(HttpStatus.OK)
					.body(xamplifyLogService.listWatchedUsersByUser(userId, pageSize, pageNumber));
		} catch (Exception e) {
			logger.error("Error In listWatchedUsersByUser() method ", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", e.getMessage()));
		}
	}
}
