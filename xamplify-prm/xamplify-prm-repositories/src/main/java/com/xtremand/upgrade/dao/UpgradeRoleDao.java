package com.xtremand.upgrade.dao;

import java.util.Map;

import com.xtremand.common.bom.Pagination;
import com.xtremand.upgrade.dto.UpgradeRoleGetDTO;

public interface UpgradeRoleDao {
	
	public boolean isRequestExists(Integer companyId);
	
	Map<String, Object> findAll(Pagination pagination);
	
	public boolean isRequestApproved(Integer requestId);
	
	UpgradeRoleGetDTO findCompanyIdAndCreatedBy(Integer requestId);
	
	void approveRequest(Integer requestId);
	
	void rejectRequest(Integer requestId);
	

}
