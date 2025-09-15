package com.xtremand.dashboard.layout.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.custom.html.block.bom.CustomHtmlBlock;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.dashboard.layout.dao.DashboardLayoutDao;
import com.xtremand.dashboard.layout.dto.DashboardLayoutDTO;
import com.xtremand.dashboard.layout.dto.DashboardLayoutRequestDTO;
import com.xtremand.dashboard.layout.service.DashboardLayoutService;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.team.dao.TeamDao;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.util.XamplifyUtils;

@Service
@Transactional
public class DashboardLayoutServiceImpl implements DashboardLayoutService {

	@Autowired
	private DashboardLayoutDao dashboardLayoutDao;

	@Autowired
	private GenericDAO genericDao;

	@Autowired
	private TeamDao teamDao;

	@Autowired
	private UserDAO userDao;

	@Override
	public List<DashboardLayoutDTO> findAll(Integer loggedInUserId, String companyProfileName) {
		if (XamplifyUtils.isValidInteger(loggedInUserId)) {
			if (XamplifyUtils.isValidString(companyProfileName)) {
				Integer companyId = userDao.getCompanyIdByProfileName(companyProfileName);
				Integer primaryAdminId = teamDao.findPrimaryAdminIdByCompanyId(companyId);
				boolean isLayoutUpdated = dashboardLayoutDao.findDefaultDashboardSettings(companyId);
				Integer userId = isLayoutUpdated ? primaryAdminId : loggedInUserId;
				List<DashboardLayoutDTO> dashboardLayouts = dashboardLayoutDao.findCustomDashboardLayout(userId,
						companyId);
				List<DashboardLayoutDTO> htmlBlocks = dashboardLayoutDao.findCustomHtmlBlocks(companyId);
				return mergeAndSortLayoutsByDisplayIndex(dashboardLayouts, htmlBlocks);
			} else {
				Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
				List<DashboardLayoutDTO> dashboardLayouts = dashboardLayoutDao.findCustomDashboardLayout(loggedInUserId, null);
				List<DashboardLayoutDTO> htmlBlocks = dashboardLayoutDao.findCustomHtmlBlocks(companyId);
				return mergeAndSortLayoutsByDisplayIndex(dashboardLayouts, htmlBlocks);
			}
		}
		return new ArrayList<>();
	}

	private List<DashboardLayoutDTO> mergeAndSortLayoutsByDisplayIndex(List<DashboardLayoutDTO> dashboardLayouts,
			List<DashboardLayoutDTO> htmlBlocks) {
		List<DashboardLayoutDTO> combinedList = new ArrayList<>();
		combinedList.addAll(dashboardLayouts);
		combinedList.addAll(htmlBlocks);
		return combinedList.stream().sorted((o1, o2) -> {
			Integer displayIndex1 = o1.getDisplayIndex();
			Integer displayIndex2 = o2.getDisplayIndex();
			if (displayIndex1 == null && displayIndex2 == null) {
				if (dashboardLayouts.contains(o1) && !dashboardLayouts.contains(o2)) {
					return -1;
				} else if (!dashboardLayouts.contains(o1) && dashboardLayouts.contains(o2)) {
					return 1;
				}
				return 0;
			}
			if (displayIndex1 != null && displayIndex2 != null) {
				return Integer.compare(displayIndex1, displayIndex2);
			}
			if (displayIndex1 == null && displayIndex2 != null) {
				return dashboardLayouts.contains(o1) ? -1 : 1;
			}
			if (displayIndex1 != null && displayIndex2 == null) {
				return dashboardLayouts.contains(o1) ? -1 : 1;
			}
			return 0;
		}).collect(Collectors.toList());
	}

	@Override
	public XtremandResponse update(DashboardLayoutRequestDTO dashboardLayoutRequestDTO) {
		XtremandResponse response = new XtremandResponse();
		Integer loggedInUserId = dashboardLayoutRequestDTO.getUserId();
		String companyProfileName = dashboardLayoutRequestDTO.getCompanyProfileName();
		if (XamplifyUtils.isValidInteger(loggedInUserId)) {
			if (XamplifyUtils.isValidString(companyProfileName)) {
				Integer companyId = userDao.getCompanyIdByProfileName(companyProfileName);
				dashboardLayoutDao.updateCustomDashboardLayout(dashboardLayoutRequestDTO, companyId);
				XamplifyUtils.addSuccessStatusWithMessage(response, "Dashboard layout updated successfully");
			} else {
				dashboardLayoutDao.updateCustomDashboardLayout(dashboardLayoutRequestDTO, null);
				XamplifyUtils.addSuccessStatusWithMessage(response, "Dashboard layout updated successfully");
			}
			if (XamplifyUtils.isNotEmptySet(dashboardLayoutRequestDTO.getIds())) {
				for (Integer id : dashboardLayoutRequestDTO.getIds()) {
					CustomHtmlBlock customHtmlBlock = genericDao.get(CustomHtmlBlock.class, id);
					customHtmlBlock.setSelected(false);
				}
			}
		}
		return response;
	}

	@Override
	public XtremandResponse findDefaultDashboardSettings(String companyProfileName) {
		XtremandResponse response = new XtremandResponse();
		if (XamplifyUtils.isValidString(companyProfileName)) {
			Integer companyId = userDao.getCompanyIdByProfileName(companyProfileName);
			boolean isLayoutupdated = dashboardLayoutDao.findDefaultDashboardSettings(companyId);
			response.setData(isLayoutupdated);
			XamplifyUtils.addSuccessStatus(response);
		}
		return response;
	}

	@Override
	public XtremandResponse updateDefaultDashboardSettings(String companyProfileName, boolean isLayoutUpdated) {
		XtremandResponse response =  new XtremandResponse();
		response.setStatusCode(401);
		if (XamplifyUtils.isValidString(companyProfileName)) {
			Integer companyId = userDao.getCompanyIdByProfileName(companyProfileName);
			dashboardLayoutDao.updateDefaultDashboardSettings(companyId, isLayoutUpdated);
			response.setStatusCode(200);
			response.setMessage("Default dashboard settings have been saved successfully");
		}
		return response;
	}

}
