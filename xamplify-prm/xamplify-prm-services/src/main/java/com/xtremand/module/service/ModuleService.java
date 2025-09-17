package com.xtremand.module.service;

import com.xtremand.formbeans.UserDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.partnership.dto.LoginAsPartnerDTO;
import com.xtremand.util.dto.LeftSideNavigationBarItem;
import com.xtremand.util.dto.ModuleCustomDTO;
import com.xtremand.util.dto.ModuleCustomRequestDTO;
import com.xtremand.util.dto.WelcomePageItem;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;

public interface ModuleService {

	XtremandResponse getAvailableModules(Integer userId, String companyProfileName);


	public LeftSideNavigationBarItem findLeftMenuItems(VanityUrlDetailsDTO vanityUrlDetailsDTO);

	WelcomePageItem getWelcomePageItems(VanityUrlDetailsDTO dto);

	XtremandResponse findModuleCustomNamesByCompanyId(Integer companyId);

	XtremandResponse findPartnerModuleByCompanyId(Integer companyId);

	XtremandResponse updateModuleName(ModuleCustomDTO moduleCustomNameDTO);

	/********* XNFR-224 ********/
	XtremandResponse findLoginAsPartnerSettingsOptions(String vendorCompanyProfileName, Integer loggedInUserId);

	XtremandResponse updateLoginAsPartnerSettingsOptions(LoginAsPartnerDTO loginAsPartnerDTO);

	/********* XNFR-276 ********/
	XtremandResponse getCustomizedLeftMenuItems(VanityUrlDetailsDTO vanityUrlDetailsDTO);

	XtremandResponse updateLeftMenuItems(ModuleCustomRequestDTO moduleCustomRequestDTO);
	
	public XtremandResponse handleCustomNamesForExistingCompanies(Integer moduleId);

	public XtremandResponse handleCustomNamesForExistingPartnerships(Integer moduleId);
	
	/**XNFR-891**/
	public XtremandResponse fetchModulesToAddPartnerModulesAccess(Integer loggedInUserId, String companyProfileName);
	
	public XtremandResponse fetchModulesToEditPartnerModulesAccess(Integer loggedInUserId, Integer partnershipId, String companyProfileName);
	
	public XtremandResponse updatePartnerModulesAccess(UserDTO userDTO);
	
	public XtremandResponse getModulesAccessGivenByVendorForPartners(String vendorCompanyProfileName,Integer partnerCompanyId, Integer loggedInUserId);//XNFR-914

	public XtremandResponse loadTotalContactSubscriptionUsedByCompanyAndPartners(Integer companyId);
	
}
