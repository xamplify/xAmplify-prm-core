package com.xtremand.analytics.dao;

import java.util.List;
import java.util.Map;

import com.xtremand.common.bom.Pagination;
import com.xtremand.formbeans.UserDTO;

public interface VendorAnalyticsDAO {

	public Map<String, Object> findAllVendors(Integer partnerId, Pagination pagination);
	
	public  List<Object[]> getDistributedCampaignsCount(List<Integer> companyIdsList, Integer partnerCompanyId);
	
	Map<String, Object> listVendors(Pagination pagination);

	public List<UserDTO> listTop10Users(String columnName);
	
	public Integer getVendorsCountByPartnerCompanyId(Integer partnerCompanyId);
	
	public UserDTO getUserDetailsByCompanyIdAndUserAlias(Integer companyId,String userAlias);
	
	public Map<String, Object> getVendors(Pagination pagination);
}
