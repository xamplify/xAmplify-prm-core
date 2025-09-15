package com.xtremand.controller;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xtremand.campaign.bom.ModuleAccess;
import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.module.service.ModuleService;
import com.xtremand.partnership.dto.LoginAsPartnerDTO;
import com.xtremand.team.member.dto.RoleDisplayDTO;
import com.xtremand.util.dto.LeftSideNavigationBarItem;
import com.xtremand.util.dto.ModuleCustomDTO;
import com.xtremand.util.dto.ModuleCustomRequestDTO;
import com.xtremand.util.service.UtilService;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;

@Controller
@RequestMapping(value = "/module/")
public class ModuleController {

	@Autowired
	private ModuleService moduleService;

	@Autowired
	private UtilService utilService;

	@GetMapping(value = "getAvailableModules/{userId}")
	public ResponseEntity<XtremandResponse> getAvailableModules(@PathVariable Integer userId,
			@RequestParam String companyProfileName) {
		return new ResponseEntity<>(moduleService.getAvailableModules(userId, companyProfileName), HttpStatus.OK);
	}

	@GetMapping(value = "getRoleDetails/{userId}")
	public ResponseEntity<RoleDisplayDTO> getRoleDetails(@PathVariable Integer userId) {
		return new ResponseEntity<>(utilService.getRoleDetailsByUserId(userId), HttpStatus.OK);
	}

	@PostMapping(value = "findLeftMenuItems")
	public ResponseEntity<LeftSideNavigationBarItem> findLeftMenuItems(
			@RequestBody VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		return new ResponseEntity<>(moduleService.findLeftMenuItems(vanityUrlDetailsDTO), HttpStatus.OK);
	}

	@PostMapping("getDashboardType")
	public ResponseEntity<Set<String>> getDashboardType(@RequestBody VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		return new ResponseEntity<>(utilService.getDashboardType(vanityUrlDetailsDTO), HttpStatus.OK);
	}

	@GetMapping(value = "getModuleDetails/{userId}")
	public ResponseEntity<ModuleAccess> getModuleDetails(@PathVariable Integer userId) {
		return new ResponseEntity<>(utilService.getModuleDetailsByUserId(userId), HttpStatus.OK);
	}

	@GetMapping(value = "findModuleCustomNamesByCompanyId/{companyId}")
	public ResponseEntity<XtremandResponse> findModuleCustomNamesByCompanyId(@PathVariable Integer companyId) {
		return new ResponseEntity<>(moduleService.findModuleCustomNamesByCompanyId(companyId), HttpStatus.OK);
	}

	@GetMapping(value = "findPartnerModuleByCompanyId/{companyId}")
	public ResponseEntity<XtremandResponse> findPartnerModuleByCompanyId(@PathVariable Integer companyId) {
		return new ResponseEntity<>(moduleService.findPartnerModuleByCompanyId(companyId), HttpStatus.OK);
	}

	@PostMapping(value = "updateModuleName")
	public ResponseEntity<XtremandResponse> updateModuleName(@RequestBody ModuleCustomDTO moduleCustomNameDTO) {
		return new ResponseEntity<>(moduleService.updateModuleName(moduleCustomNameDTO), HttpStatus.OK);
	}

	/********* XNFR-276 ********/

	@PostMapping(value = "getCustomizedLeftMenuItems")
	public ResponseEntity<XtremandResponse> getCustomizedLeftMenuItems(
			@RequestBody VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		return new ResponseEntity<>(moduleService.getCustomizedLeftMenuItems(vanityUrlDetailsDTO), HttpStatus.OK);
	}

	@PostMapping(value = "updateLeftMenuItems")
	public ResponseEntity<XtremandResponse> updateLeftMenuItems(
			@RequestBody ModuleCustomRequestDTO moduleCustomRequestDTO) {
		return new ResponseEntity<>(moduleService.updateLeftMenuItems(moduleCustomRequestDTO), HttpStatus.OK);
	}

	/****
        * http://localhost:8080/xamplify-prm-api/module/handleCustomNamesForExistingCompanies/moduleId/{moduleId}
	 ******/
	@GetMapping(value = "handleCustomNamesForExistingCompanies/moduleId/{moduleId}")
	@ResponseBody
	public XtremandResponse handleCustomNamesForExistingCompanies(@PathVariable Integer moduleId) {
		return moduleService.handleCustomNamesForExistingCompanies(moduleId);
	}

	/****
        * http://localhost:8080/xamplify-prm-api/module/handleCustomNamesForExistingPartnerships/moduleId/{moduleId}
	 ******/
	@GetMapping(value = "handleCustomNamesForExistingPartnerships/moduleId/{moduleId}")
	@ResponseBody
	public XtremandResponse handleCustomNamesForExistingPartnerships(@PathVariable Integer moduleId) {
		return moduleService.handleCustomNamesForExistingPartnerships(moduleId);
	}

	/********* XNFR-224 ********/
	@GetMapping(value = "findLoginAsPartnerSettingsOptions/{vendorCompanyProfileName}/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> findLoginAsPartnerSettingsOptions(
			@PathVariable String vendorCompanyProfileName, @PathVariable Integer loggedInUserId) {
		return new ResponseEntity<>(
				moduleService.findLoginAsPartnerSettingsOptions(vendorCompanyProfileName, loggedInUserId),
				HttpStatus.OK);
	}

	/********* XNFR-224 ********/
	@PostMapping(value = "updateLoginAsPartnerSettingsOptions")
	public ResponseEntity<XtremandResponse> updateLoginAsPartnerSettingsOptions(
			@RequestBody LoginAsPartnerDTO loginAsPartnerDTO) {
		return new ResponseEntity<>(moduleService.updateLoginAsPartnerSettingsOptions(loginAsPartnerDTO),
				HttpStatus.OK);
	}

	/** XNFR-891 **/
	@GetMapping(value = "fetchModuleForPartnerModuleAccess/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> fetchModuleForPartnerModuleAccess(@PathVariable Integer loggedInUserId,
			@RequestParam(defaultValue = "") String companyProfileName) {
		return ResponseEntity
				.ok(moduleService.fetchModulesToAddPartnerModulesAccess(loggedInUserId, companyProfileName));
	}

	@GetMapping(value = "fetchModulesForEditPartnerModule/{loggedInUserId}/{partnershipId}")
	public ResponseEntity<XtremandResponse> fetchModulesForEditPartnerModule(@PathVariable Integer loggedInUserId,
			@PathVariable Integer partnershipId, @RequestParam(defaultValue = "") String companyProfileName) {
		return ResponseEntity.ok(moduleService.fetchModulesToEditPartnerModulesAccess(loggedInUserId, partnershipId,
				companyProfileName));
	}

	@PutMapping(value = "updatePartnerModulesAccess")
	public ResponseEntity<XtremandResponse> updatePartnerModulesAccess(@RequestBody UserDTO userDTO,
			@RequestParam(defaultValue = "") String companyProfileName) {
		XtremandResponse response = new XtremandResponse();
		try {
			response = moduleService.updatePartnerModulesAccess(userDTO);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			throw new XamplifyDataAccessException(e);
		} finally {
		}
	}

	/*** XNFR-914 ***/
	@GetMapping("module-access/{companyProfileName}/{partnerCompanyId}/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> getModulesAccessGivenByVendorForPartners(
			@PathVariable String companyProfileName, @PathVariable Integer partnerCompanyId,
			@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(moduleService.getModulesAccessGivenByVendorForPartners(companyProfileName,
				partnerCompanyId, loggedInUserId));
	}

	/*** XNFR-914 ***/

	/** XNFR-952 **/
	@GetMapping("loadTotalContactSubscriptionUsedByCompanyAndPartners/{companyId}")
	public ResponseEntity<XtremandResponse> loadTotalContactSubscriptionUsedByCompanyAndPartners(
			@PathVariable Integer companyId) {
		return ResponseEntity.ok(moduleService.loadTotalContactSubscriptionUsedByCompanyAndPartners(companyId));
	}

}
