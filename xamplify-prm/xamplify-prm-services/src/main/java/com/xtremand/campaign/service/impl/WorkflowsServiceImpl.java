package com.xtremand.campaign.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.campaign.dao.WorkflowsDao;
import com.xtremand.campaign.service.WorkflowsService;
import com.xtremand.common.bom.Pagination;
import com.xtremand.formbeans.XtremandResponse;

@Service
@Transactional
public class WorkflowsServiceImpl implements WorkflowsService {
	
	@Autowired
	private WorkflowsDao workflowsDao;
	
	@Override
	public XtremandResponse findWorkflowDetails(Pagination pagination) {
		XtremandResponse response = new XtremandResponse(); 
		response.setStatusCode(200);
		response.setData(workflowsDao.findEmailNotOpenedWorkflowDetails(pagination));
		return response;
	}
	

}
