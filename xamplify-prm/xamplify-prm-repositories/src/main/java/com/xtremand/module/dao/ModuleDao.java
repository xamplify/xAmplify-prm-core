package com.xtremand.module.dao;

import java.util.List;

import com.xtremand.common.bom.Module;
import com.xtremand.common.bom.ModuleCustom;
import com.xtremand.formbeans.RoleDTO;
import com.xtremand.util.dto.ModuleCustomDTO;

public interface ModuleDao {

	List<RoleDTO> getRoleDetailsByUserId(Integer userId);

	List<Module> findModuleNames();

	List<ModuleCustom> findModuleCustomNamesByCompanyId(Integer companyId);

	ModuleCustomDTO findPartnerModuleByCompanyId(Integer companyId);

	/*** XNFR-276 *****/
	List<ModuleCustomDTO> getLeftSideItemsByCompanyId(Integer companyId);
	
	List<ModuleCustom> findModuleCustomInOrder(Integer loggedInUserCompanyId, Integer partnershipId);
	
	public boolean checkModuleCustom(Integer partnershipId, Integer companyId, Integer moduleId);

	/*** XNFR-127 *****/
	String findPartnersModuleCustomNameByUserId(Integer userId);
	
	Integer getModuleIdByModuleName(String name);
	
	public String  getModuleNameById(Integer moduleId);	
	
	/**XNFR-891**/
	void updateModulesAccess(List<Integer> restrictedModuleIds, Integer partnershipId, boolean isPartnerAccessModule);
	
	List<Integer> fetchRestrictedModuleIds(Integer companyId, Integer partnershipId);
	
	List<ModuleCustomDTO> fetchModuleCustomDTOs(List<Integer> moduleIds, Integer partnershipId);
	
	boolean fetchModuleAccessForPartner(Integer vendorCompanyId, Integer partnerCompanyId, Integer moduleId);

	void updateMarketingModulesAccess(List<Integer> moduleIds, Integer partnershipId, boolean isMarketingModule);

	List<ModuleCustom> findMarketingModulesCustomInOrder(Integer loggedInUserCompanyId, Integer partnershipId);

}
