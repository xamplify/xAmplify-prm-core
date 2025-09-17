package com.xtremand.campaign.service;

import com.xtremand.common.bom.Pagination;
import com.xtremand.formbeans.XtremandResponse;

public interface WorkflowsService {
	
	XtremandResponse findWorkflowDetails(Pagination pagination);


}
