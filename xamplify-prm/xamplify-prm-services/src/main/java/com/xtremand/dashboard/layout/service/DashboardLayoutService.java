package com.xtremand.dashboard.layout.service;

import java.util.List;

import com.xtremand.dashboard.layout.dto.DashboardLayoutDTO;
import com.xtremand.dashboard.layout.dto.DashboardLayoutRequestDTO;
import com.xtremand.formbeans.XtremandResponse;

public interface DashboardLayoutService {

	List<DashboardLayoutDTO> findAll(Integer loggedInUserId, String companyProfileName);

	XtremandResponse update(DashboardLayoutRequestDTO dashboardLayoutRequestDTO);

	XtremandResponse findDefaultDashboardSettings(String companyProfileName);

	XtremandResponse updateDefaultDashboardSettings(String companyProfileName, boolean isLayoutUpdated);

}
