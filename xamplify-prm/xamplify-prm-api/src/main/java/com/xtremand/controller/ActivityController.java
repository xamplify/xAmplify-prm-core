package com.xtremand.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xtremand.activity.service.ActivityService;
import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.util.dto.Pageable;

@RestController
@RequestMapping(value = "/activity")
public class ActivityController {
	
	@Autowired
	private ActivityService activityService;

	@GetMapping("/fetchRecentActivities/{userId}/{loggedInUserId}/{isCompanyJourney}")
	public ResponseEntity<XtremandResponse> fetchRecentActivities(@PathVariable Integer userId, @PathVariable Integer loggedInUserId, 
			@PathVariable Boolean isCompanyJourney, @Valid Pageable pageable, BindingResult result) {
		XtremandResponse response = new XtremandResponse();
		try {
			response = activityService.fetchRecentActivities(pageable, userId, loggedInUserId, isCompanyJourney, result);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			throw new XamplifyDataAccessException(e);
		}
	}
	
	@GetMapping("/fetchLogoFromExternalSource/{userId}")
	public ResponseEntity<XtremandResponse> fetchLogoFromExternalSource(@PathVariable Integer userId) {
		XtremandResponse response = new XtremandResponse();
		try {
			response = activityService.fetchLogoFromExternalSource(userId);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			throw new XamplifyDataAccessException(e);
		}
	}
}
