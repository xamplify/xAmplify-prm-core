package com.xtremand.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xtremand.company.dto.EmailNotificationSettingsDTO;
import com.xtremand.company.service.CompanyProfileService;
import com.xtremand.formbeans.XtremandResponse;

@RestController
@RequestMapping(value = "/company/")
public class CompanyProfileController {

	@Autowired
	private CompanyProfileService companyService;

	@GetMapping("emailNotificationSettings/{userId}")
	public ResponseEntity<XtremandResponse> getById(@PathVariable Integer userId) {
		return new ResponseEntity<>(companyService.getEmailNotificationSettings(userId), HttpStatus.OK);
	}

	@PutMapping("emailNotificationSettings/{userId}")
	public ResponseEntity<XtremandResponse> getById(@PathVariable Integer userId,
			@RequestBody EmailNotificationSettingsDTO emailNotificationSettingsDTO) {
		return new ResponseEntity<>(
				companyService.updateEmailNotificationSettings(userId, emailNotificationSettingsDTO), HttpStatus.OK);
	}

	@GetMapping("/users/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> findAllUsers(@PathVariable Integer loggedInUserId) {
		return new ResponseEntity<>(companyService.findAllUsers(loggedInUserId), HttpStatus.OK);
	}

}
