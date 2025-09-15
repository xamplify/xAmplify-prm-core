package com.xtremand.unsubscribe.service;

import com.xtremand.common.bom.Pagination;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.unsubscribe.dto.UnsubscribePageDetailsDTO;
import com.xtremand.unsubscribe.dto.UnsubscribeReasonDTO;

public interface UnsubscribeService {
	
	XtremandResponse save(UnsubscribeReasonDTO unsubscribeReasonDTO);
	
	XtremandResponse update(UnsubscribeReasonDTO unsubscribeReasonDTO);
	
	XtremandResponse findAll(Pagination pagination);
	
	XtremandResponse findById(Integer id);
	
	XtremandResponse delete(Integer id);
	
	XtremandResponse addDefaultReasons();

	XtremandResponse findAll(Integer companyId);

	XtremandResponse findUnsubscribePageDetailsByCompanyId(Integer userId);

	XtremandResponse updateHeaderAndFooterText(UnsubscribePageDetailsDTO unsubscribePageDetailsDTO);

	XtremandResponse findUnsubscribePageContent(Integer userId);

	XtremandResponse findUnsubscribePageContentByCompanyId(Integer companyId);
	
	void addDefaultReasonsAndHeaderAndTextByCompanyId(Integer companyId);


}
