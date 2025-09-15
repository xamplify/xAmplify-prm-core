package com.xtremand.dashboard.layout.dao;

import java.util.List;

import com.xtremand.dashboard.layout.dto.DashboardLayoutDTO;
import com.xtremand.dashboard.layout.dto.DashboardLayoutRequestDTO;

public interface DashboardLayoutDao {

	void updateCustomDashboardLayout(DashboardLayoutRequestDTO dashboardLayoutRequestDTO, Integer companyId);

	List<DashboardLayoutDTO> findCustomDashboardLayout(Integer loggedInUserId, Integer companyId);

	void updateDefaultDashboardSettings(Integer primaryAdminId, boolean isLayoutUpdated);

	boolean findDefaultDashboardSettings(Integer companyId);

	public List<DashboardLayoutDTO> findCustomHtmlBlocks(Integer companyId);

}
