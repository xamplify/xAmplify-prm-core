package com.xtremand.guide.service;

import com.xtremand.common.bom.Pagination;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;

public interface UserGuideService {
	
	XtremandResponse getTagIdByName(String tagName);
	
	XtremandResponse getUserGudesByModuleName(Pagination pagination);
	
	XtremandResponse getGuideLinkByTitle(Pagination pagination);
	
	XtremandResponse getUserGuideBySlug(Pagination pagination);
	
	XtremandResponse getSearchResults(Pagination pagination);
	
	XtremandResponse getUserRolesForDashBoard(VanityUrlDetailsDTO vanityUrlDetailsDTO);

	XtremandResponse getModuleNameByModuleId(Integer moduleId);
}
