package com.xtremand.dashboard.button.service;

import java.util.List;
import java.util.Set;

import org.springframework.validation.BindingResult;

import com.xtremand.dashboard.buttons.dto.PublishedDashboardButtonDetailsDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.util.dto.Pageable;

public interface DashboardButtonService {

	XtremandResponse findPublishedPartnerGroupPartnerIdMappingIds(Integer userListId, Integer dashboardButtonId);

	List<PublishedDashboardButtonDetailsDTO> findPublishedDashboardButtonsAndUpdateStatus(
			Set<Integer> partnerListIds, Integer loggedInUserId);

	void updateStatus(Set<Integer> dashboardButtonIds, boolean isPublishingInProgress);

	XtremandResponse isPublished(Integer dashboardButtonId);

	XtremandResponse findAllPublishedAndUnPublished(Pageable pageable, BindingResult result, Integer loggedInUserId,
			Integer userListId, Integer partnerUserId);

	XtremandResponse publishDashboardButtonToPartnerCompany(Integer userListId, Integer partnerUserId,
			Integer dashboardButtonId, Integer loggedInUserId);
	
	 XtremandResponse updateDashboardButtonStatus(Set<Integer> dashboardButtonIds);

	 XtremandResponse findAlternateUrls(String referenceUrl);
}
