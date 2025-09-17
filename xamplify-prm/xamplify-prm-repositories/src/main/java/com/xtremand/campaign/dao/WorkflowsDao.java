package com.xtremand.campaign.dao;

import java.util.Map;

import com.xtremand.common.bom.Pagination;

public interface WorkflowsDao {
	
	Map<String, Object> findEmailNotOpenedWorkflowDetails(Pagination pagination);


}
