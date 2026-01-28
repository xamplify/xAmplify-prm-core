package com.xtremand.activity.service;

import org.springframework.validation.BindingResult;

import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.util.dto.Pageable;

public interface ActivityService {

	XtremandResponse fetchRecentActivities(Pageable pageable, Integer contactId, Integer loggedInUserId, Boolean isCompanyJourney, BindingResult result);
	
	XtremandResponse fetchLogoFromExternalSource(Integer userId);
	
	String getLogoFromExternalSourceUsingDomain(String domain);
	
}
