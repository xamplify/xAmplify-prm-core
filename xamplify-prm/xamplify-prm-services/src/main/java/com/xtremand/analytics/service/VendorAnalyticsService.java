package com.xtremand.analytics.service;

import java.util.Map;

import com.xtremand.common.bom.Pagination;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;

public interface VendorAnalyticsService {

	public Map<String, Object> listVendors(Pagination pagination);
	
	public Map<String, Object> listTop10Users();
	
	public Integer getVendorsCountByPartnerCompanyId(Integer partnerCompanyId);
	
	public UserDTO getUserDetailsByCompanyIdAndUserAlias(Integer companyId,String userAlias);
	
	public Map<String, Object> findAllVendors(Pagination pagination,Integer partnerId);

	public Map<String, Object> getVendors(Pagination pagination);

	public XtremandResponse getVendorCount(VanityUrlDetailsDTO vanityURLDTO);
}
