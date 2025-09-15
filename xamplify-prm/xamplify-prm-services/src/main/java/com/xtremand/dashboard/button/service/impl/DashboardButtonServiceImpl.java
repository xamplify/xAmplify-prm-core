package com.xtremand.dashboard.button.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import com.xtremand.common.bom.Pagination;
import com.xtremand.dashboard.button.dao.DashboardButtonDao;
import com.xtremand.dashboard.button.service.DashboardButtonService;
import com.xtremand.dashboard.buttons.dto.DashboardAlternateUrlDTO;
import com.xtremand.dashboard.buttons.dto.PublishedDashboardButtonDetailsDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.partnership.dao.PartnershipDAO;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.userlist.dao.UserListDAO;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dto.Pageable;
import com.xtremand.util.service.UtilService;

@Service
@Transactional
public class DashboardButtonServiceImpl implements DashboardButtonService {

	@Autowired
	private UtilService utilService;

	@Autowired
	private DashboardButtonDao dashboardButtonDao;

	@Autowired
	private UserDAO userDao;

	@Autowired
	private UserListDAO userListDao;

	@Autowired
	private PartnershipDAO partnershipDao;

	@Override
	public XtremandResponse findPublishedPartnerGroupPartnerIdMappingIds(Integer userListId,
			Integer dashboardButtonId) {
		XtremandResponse response = new XtremandResponse();
		List<Integer> userUserListIds = dashboardButtonDao.findPublishedPartnerGroupPartnerIds(userListId,
				dashboardButtonId);
		response.setData(userUserListIds);
		Set<Integer> publishedPartnershipIds = new HashSet<>();
		publishedPartnershipIds.addAll(dashboardButtonDao.findPublishedPartnershipIds(dashboardButtonId));
		Map<String, Object> map = new HashMap<>();
		map.put("partnershipIds", publishedPartnershipIds);
		response.setMap(map);
		XamplifyUtils.addSuccessStatus(response);
		return response;
	}

	@Override
	public List<PublishedDashboardButtonDetailsDTO> findPublishedDashboardButtonsAndUpdateStatus(
			Set<Integer> partnerListIds, Integer loggedInUserId) {
		List<PublishedDashboardButtonDetailsDTO> publishedDashboardButtonDtos = new ArrayList<>();
		if (XamplifyUtils.isNotEmptySet(partnerListIds)) {
			Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
			publishedDashboardButtonDtos.addAll(dashboardButtonDao
					.findPublishedDashboardButtonIdAndTitlesByPartnerListIdAndCompanyId(partnerListIds, companyId));
			if (XamplifyUtils.isNotEmptyList(publishedDashboardButtonDtos)) {
				Set<Integer> dashboardButtonIds = publishedDashboardButtonDtos.stream()
						.map(PublishedDashboardButtonDetailsDTO::getId).collect(Collectors.toSet());
				dashboardButtonDao.updateStatus(dashboardButtonIds, true);
			}
		}
		return publishedDashboardButtonDtos;

	}

	@Override
	public void updateStatus(Set<Integer> dashboardButtonIds, boolean isPublishingInProgress) {
		dashboardButtonDao.updateStatus(dashboardButtonIds, isPublishingInProgress);
	}

	@Override
	public XtremandResponse isPublished(Integer dashboardButtonId) {
		XtremandResponse response = new XtremandResponse();
		boolean isPublishingInProgress = dashboardButtonDao.isPublishingInProgress(dashboardButtonId);
		XamplifyUtils.addSuccessStatus(response);
		response.setData(isPublishingInProgress);
		return response;
	}

	@Override
	public XtremandResponse findAllPublishedAndUnPublished(Pageable pageable, BindingResult result,
			Integer loggedInUserId, Integer userListId, Integer partnerUserId) {
		Integer userUserListId = null;
		XtremandResponse response = new XtremandResponse();
		Pagination pagination = utilService.setPageableParameters(pageable, loggedInUserId);
		pagination.setUserListId(userListId);
		pagination.setPartnerId(partnerUserId);
		if(XamplifyUtils.isValidInteger(pagination.getPartnerId())) {
			 userUserListId = userListDao.getUserUserListIdByUserListIdAndUserId(pagination.getUserListId(),pagination.getPartnerId());
			 pagination.setUserUserListId(userUserListId);
		}
		response.setData(dashboardButtonDao.findAllPublishedAndUnPublished(pagination, pageable.getSearch()));
		XamplifyUtils.addSuccessStatus(response);
		return response;
	}


	@Override
	public XtremandResponse publishDashboardButtonToPartnerCompany(Integer userListId, Integer partnerUserId,
			Integer dashboardButtonId, Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		Integer vendorCompanyId = userListDao.getCompanyIdByUserListId(userListId);
		Integer partnershipId = partnershipDao.getPartnershipIdByPartnerCompanyUserId(partnerUserId, vendorCompanyId);
		Integer userUserListId = userListDao.getUserUserListIdByUserListIdAndUserId(userListId, partnerUserId);
		boolean isDashboardButtonPublished = dashboardButtonDao.isDashboardButtonPublished(dashboardButtonId,
				partnershipId, userUserListId, partnerUserId);
		if (isDashboardButtonPublished) {
			response.setStatusCode(400);
			response.setMessage("Dashboard button has already been published.");
		} else {
			dashboardButtonDao.publishDashboardButtonToPartnerCompanyByPartnerId(dashboardButtonId, partnershipId,
					userUserListId, loggedInUserId);
			XamplifyUtils.addSuccessStatusWithMessage(response, "Dashboard button published successfully.");
		}
		return response;
	}
	
	/**** XNFR-599 ****/
	@Override
	public XtremandResponse updateDashboardButtonStatus(Set<Integer> dashboardButtonIds) {
		if (XamplifyUtils.isNotEmptySet(dashboardButtonIds)) {
			dashboardButtonDao.updateStatus(dashboardButtonIds, false);
		}
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(200);
		response.setAccess(true);
		response.setMessage("Dashboard Button(s) shared Sucessfully");
		return response;
	}

	@Override
	public XtremandResponse findAlternateUrls(String referenceUrl) {
		XtremandResponse response = new XtremandResponse();
		List<DashboardAlternateUrlDTO> alternateUrls= dashboardButtonDao.findAlternateUrls(referenceUrl);
		XamplifyUtils.addSuccessStatus(response);
		response.setData(alternateUrls);
		return response;
	}


}
