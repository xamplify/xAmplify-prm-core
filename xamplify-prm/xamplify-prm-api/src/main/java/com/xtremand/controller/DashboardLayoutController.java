package com.xtremand.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xtremand.dashboard.layout.dto.DashboardLayoutDTO;
import com.xtremand.dashboard.layout.dto.DashboardLayoutRequestDTO;
import com.xtremand.dashboard.layout.service.DashboardLayoutService;
import com.xtremand.formbeans.XtremandResponse;

@RestController
@RequestMapping(value = "/dashboard/layout")
public class DashboardLayoutController {

	@Autowired
	private DashboardLayoutService dashboardLayoutService;

	@GetMapping
	public List<DashboardLayoutDTO> findAll(@RequestParam("loggedInUserId") Integer loggedInUserId,
			@RequestParam("companyProfileName") String companyProfileName) {
		return dashboardLayoutService.findAll(loggedInUserId, companyProfileName);
	}

	@PutMapping
	public ResponseEntity<XtremandResponse> update(@RequestBody DashboardLayoutRequestDTO dashboardLayoutRequestDTO) {
		return ResponseEntity.ok(dashboardLayoutService.update(dashboardLayoutRequestDTO));
	}

	@GetMapping("default-dashboard-settings")
	public ResponseEntity<XtremandResponse> findDefaultDashboardSettings(
			@RequestParam("companyProfileName") String companyProfileName) {
		return ResponseEntity.ok(dashboardLayoutService.findDefaultDashboardSettings(companyProfileName));
	}

	@PutMapping("default-dashboard-settings")
	public ResponseEntity<XtremandResponse> updateDefaultDashboardSettings(
			@RequestParam("companyProfileName") String companyProfileName,
			@RequestParam("isLayoutUpdated") boolean isLayoutUpdated) {
		return ResponseEntity
				.ok(dashboardLayoutService.updateDefaultDashboardSettings(companyProfileName, isLayoutUpdated));
	}

}
