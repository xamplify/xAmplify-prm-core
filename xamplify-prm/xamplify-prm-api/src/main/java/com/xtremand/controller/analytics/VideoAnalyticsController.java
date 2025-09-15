package com.xtremand.controller.analytics;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.xtremand.common.bom.Pagination;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.log.bom.VideoViewsMinutesWatched;
import com.xtremand.log.service.XamplifyLogService;
import com.xtremand.video.exception.VideoDataAccessException;
import com.xtremand.video.service.VideoService;

@Controller
public class VideoAnalyticsController {

	private  static final  Logger logger = LoggerFactory.getLogger(VideoAnalyticsController.class);
	
	@Autowired
	VideoService videoService;
	@Autowired
	XamplifyLogService xamplifyLogService;
	
	/*************************video report logs*************************************************************************************/
	
	@RequestMapping(value = "/videos/watchedfully-minuteswatched-views/{alias}", method = RequestMethod.GET)
	public ResponseEntity<?> getWatchedFullyMinutesWatchedVideoViews(@PathVariable String alias){
		try{
			Map<String, Object> resultMap = videoService.getWatchedFullyMinutesWatchedVideoViews(alias);
			return ResponseEntity.status(HttpStatus.OK).body(Collections.singletonMap("video_views_count_data", resultMap));
		}catch (VideoDataAccessException e) {
			logger.error("Error In getWatchedFullyMinutesWatchedVideoViews()",e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("message", e.getMessage()));
		}
	}
	
	@RequestMapping(value = "videos/{videoId}/{userId}/watched-fully-report", method = RequestMethod.POST)
	public ResponseEntity<?> getVideoWatchedFullyDetailReport(@PathVariable Integer videoId, @PathVariable Integer userId, @RequestBody Pagination pagination){
		logger.debug("entered into getVideoWatchedFullyDetailReport() with videoId:  " + videoId );
		try{
			Map<String, Object> resultMap = xamplifyLogService.getVideoWatchedFullyDetailReport(videoId, pagination, userId);
			return ResponseEntity.status(HttpStatus.OK).body(resultMap) ;
		}catch (Exception e) {
			logger.error("error occured in getVideoWatchedFullyDetailReport(): "+e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("message", e.getMessage()));
		}
	}
	
	@RequestMapping(value = "videos/{videoId}/{userId}/total-minutes-watched-by-top-10-users-detailreport", method = RequestMethod.GET)
	public ResponseEntity<?> listTotalMinutesWatchedByTop10UsersDetailReport(@PathVariable Integer videoId, @PathVariable Integer userId){
		logger.debug("entered into listTotalMinutesWatchedByTop10UsersDetailReport() with videoId:  " + videoId );
		try{
			XtremandResponse response = xamplifyLogService.listTotalMinutesWatchedByTop10UsersDetailReport(videoId, userId);
			return ResponseEntity.status(HttpStatus.OK).body(response) ;
		}catch (Exception e) {
			logger.error("error occured in listTotalMinutesWatchedByTop10UsersDetailReport(): "+e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("message", e.getMessage()));
		}
	}
	
	@RequestMapping(value = "videos/{videoId}/N/A-users-views-minuteswatched", method = RequestMethod.GET)
	public ResponseEntity<?> getNAUsersVideoViewsMinutesWatched(@PathVariable Integer videoId){
		logger.debug("entered into getNAUsersVideoViewsMinutesWatched() with videoId:  " + videoId );
		try{
			VideoViewsMinutesWatched videoViewsMinutesWatched = xamplifyLogService.getNAUsersVideoViewsMinutesWatched(videoId);
			return ResponseEntity.status(HttpStatus.OK).body(videoViewsMinutesWatched) ;
		}catch (Exception e) {
			logger.error("error occured in getNAUsersVideoViewsMinutesWatched(): "+e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("message", e.getMessage()));
		}
	}
	
	@RequestMapping(value = "videos/{videoId}/skipped-duration", method = RequestMethod.GET)
	public ResponseEntity<?> getVideoSkippedDurationData(@PathVariable Integer videoId){
		logger.debug("entered into getVideoSkippedDurationData() with videoId:  " + videoId );
		try{
			Map<String, Object> resultMap = xamplifyLogService.getVideoSkippedDurationData(videoId);
			return ResponseEntity.status(HttpStatus.OK).body(resultMap) ;
		}catch (Exception e) {
			logger.error("error occured in getVideoSkippedDurationData(): "+e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("message", e.getMessage()));
		}
	}
	
	@RequestMapping(value = "videos/{videoId}/{userId}/video-duration-played-users", method = RequestMethod.POST)
	public ResponseEntity<?> listVideoDurationPlayedUsers(@PathVariable Integer userId, @PathVariable Integer videoId, @RequestBody Pagination pagination){
		logger.debug("entered into listVideoDurationPlayedUsers() with videoId:  " + videoId );
		try{
			Map<String, Object> resultMap = xamplifyLogService.listVideoDurationPlayedUsers(userId, videoId, pagination);
			return ResponseEntity.status(HttpStatus.OK).body(resultMap) ;
		}catch (Exception e) {
			logger.error("error occured in listVideoDurationPlayedUsers(): "+e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("message", e.getMessage()));
		}
	}
	
	@RequestMapping(value = "videos/{videoId}/{userId}/video-duration-skipped-users", method = RequestMethod.POST)
	public ResponseEntity<?> listVideoDurationSkippedUsers(@PathVariable Integer userId, @PathVariable Integer videoId, @RequestBody Pagination pagination){
		logger.debug("entered into listVideoDurationSkippedUsers() with videoId:  " + videoId );
		try{
			Map<String, Object> resultMap = xamplifyLogService.listVideoDurationSkippedUsers(userId, videoId, pagination);
			return ResponseEntity.status(HttpStatus.OK).body(resultMap) ;
		}catch (Exception e) {
			logger.error("error occured in listVideoDurationSkippedUsers(): "+e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("message", e.getMessage()));
		}
	}
	
	@RequestMapping(value = "videos/{videoId}/countrywise-users-count", method = RequestMethod.GET)
	public ResponseEntity<?> getVideoViewsCountByCountry(@PathVariable Integer videoId){
		logger.debug("entered into getVideoViewsCountByCountry() with videoId:  " + videoId );
		try{
			Map<String, Object> resultMap = xamplifyLogService.getVideoViewsCountByCountry(videoId);
			return ResponseEntity.status(HttpStatus.OK).body(resultMap) ;
		}catch (Exception e) {
			logger.error("error occured in getVideoViewsCountByCountry(): "+e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("message", e.getMessage()));
		}
	}
	
	@RequestMapping(value = "videos/{videoId}/{userId}/countrywise-users-report", method = RequestMethod.POST)
	public ResponseEntity<?> listCountryWiseVideoViewsDetailReport(@PathVariable Integer userId, @PathVariable Integer videoId, String countryCode, @RequestBody Pagination pagination){
		logger.debug("entered into listCountryWiseVideoViewsDetailReport() with videoId:  " + videoId );
		try{
			Map<String,Object> resultMap = xamplifyLogService.listCountryWiseVideoViewsDetailReport(userId, videoId, countryCode, pagination);
			return ResponseEntity.status(HttpStatus.OK).body(resultMap) ;
		}catch (Exception e) {
			logger.error("error occured in listCountryWiseVideoViewsDetailReport(): "+e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("message", e.getMessage()));
		}
	}
	
	@RequestMapping(value = "/videos/timePeriod/{timePeriod}", method = RequestMethod.GET)
	public ResponseEntity<?> getTimePeriodValues(@PathVariable String timePeriod){
		try{
			List<String> list = xamplifyLogService.getTimePeriodValues(timePeriod);
			return ResponseEntity.status(HttpStatus.OK).body(list);
		}catch (Exception e) {
			logger.error("Error In getTimePeriodValues()",e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("message", e.getMessage()));
		}
	}
	
	@RequestMapping(value = "/videos/{timePeriod}/views-minuteswatched", method = RequestMethod.GET)
	public ResponseEntity<?> listVideoViewsMinutesWatchedByTimePeriod(@PathVariable String timePeriod, @RequestParam Integer videoId, @RequestParam String timePeriodValue){
		try{
			List<VideoViewsMinutesWatched> resultList = xamplifyLogService.listVideoViewsMinutesWatchedByTimePeriod(timePeriod, videoId, timePeriodValue);
			return ResponseEntity.status(HttpStatus.OK).body(resultList);
		}catch (VideoDataAccessException e) {
			logger.error("Error In listVideoViewsMinutesWatchedByTimePeriod()",e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("message", e.getMessage()));
		}
	}
	
	@RequestMapping(value = "/videos/{loggedInUser}/{timePeriod}/views-minuteswatched-detail-report", method = RequestMethod.POST)
	public ResponseEntity<?> listVideoViewsMinutesWatchedDetailReport(@PathVariable Integer loggedInUser, @PathVariable String timePeriod,@RequestParam Integer userId, @RequestParam Integer videoId, @RequestParam String timePeriodValue, @RequestBody Pagination pagination){
		try{
			Map<String, Object> resultMap = xamplifyLogService.listVideoViewsMinutesWatchedDetailReport(loggedInUser, timePeriod, userId, videoId, timePeriodValue, pagination);
			return ResponseEntity.status(HttpStatus.OK).body(resultMap);
		}catch (Exception e) {
			logger.error("Error In listVideoViewsMinutesWatchedDetailReport()",e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("message", e.getMessage()));
		}
	}
	
	@RequestMapping(value = "/videos/{timePeriod}/views", method = RequestMethod.GET)
	public ResponseEntity<?> listVideoViewsByTimePeriod(@PathVariable String timePeriod, @RequestParam Integer videoId){
		try{
			Map<String, Object> resultMap = xamplifyLogService.listVideoViewsByTimePeriod(timePeriod, videoId);
			return ResponseEntity.status(HttpStatus.OK).body(resultMap);
		}catch (VideoDataAccessException e) {
			logger.error("Error In listVideoViewsByTimePeriod()",e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("message", e.getMessage()));
		}
	}
	
	@RequestMapping(value = "videos/views/{timePeriod}/detail-report", method = RequestMethod.GET)
	public ResponseEntity<?> listVideoViewsDetialReport1(@PathVariable String timePeriod, @RequestParam Integer videoId, @RequestParam String timePeriodValue){
		logger.debug("entered into listVideoViewsDetialReport1() with videoId:  " + videoId );
		try{
			VideoViewsMinutesWatched videoViewsMinutesWatched = xamplifyLogService.listVideoViewsDetailReport1(timePeriod, videoId, timePeriodValue);
			return ResponseEntity.status(HttpStatus.OK).body(videoViewsMinutesWatched) ;
		}catch (Exception e) {
			logger.error("error occured in listVideoViewsDetialReport1(): "+e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("message", e.getMessage()));
		}
	}
	
	@RequestMapping(value = "videos/views/{userId}/{timePeriod}/detail-report", method = RequestMethod.POST)
	public ResponseEntity<?> listVideoViewsDetailReport2(@PathVariable Integer userId, @PathVariable String timePeriod, @RequestParam Integer videoId, @RequestParam String timePeriodValue, @RequestBody Pagination pagination){
		logger.debug("entered into listVideoViewsDetailReport2() with videoId:  " + videoId );
		try{
			Map<String, Object> resultMap = xamplifyLogService.listVideoViewsDetailReport2(userId, timePeriod, videoId, timePeriodValue, pagination);
			return ResponseEntity.status(HttpStatus.OK).body(resultMap) ;
		}catch (Exception e) {
			logger.error("error occured in listVideoViewsDetailReport2(): "+e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("message", e.getMessage()));
		}
	}
	
	@RequestMapping(value = "videos/{id}/{userId}/leads-info", method = RequestMethod.POST)
	public ResponseEntity<?> listLeadsDetails(@PathVariable Integer id, @PathVariable Integer userId, @RequestBody Pagination pagination){
		logger.debug("entered into listLeadsDetails() with videoId:  " + id );
		try{
			Map<String, Object> resultMap = xamplifyLogService.listLeadsDetails(userId, id, pagination);
			return ResponseEntity.status(HttpStatus.OK).body(resultMap) ;
		}catch (Exception e) {
			logger.error("error occured in listLeadsDetails(): "+e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("message", e.getMessage()));
		}
	}
	
	@RequestMapping(value = "videos/{userId}/has-video-access", method = RequestMethod.GET)
	public ResponseEntity<?>  hasVideoAccess( @PathVariable Integer userId){
		XtremandResponse response = videoService.hasVideoAccess(userId);
		return ResponseEntity.status(HttpStatus.OK).body(response) ;
	}
	
}
