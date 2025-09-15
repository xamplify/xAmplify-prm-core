package com.xtremand.workflow.service;

import java.util.List;

import org.springframework.validation.BindingResult;

import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.partner.journey.bom.TriggerComponentType;
import com.xtremand.partner.journey.dto.WorkflowRequestDTO;
import com.xtremand.util.dto.Pageable;


public interface WorkflowService {

	public XtremandResponse save(WorkflowRequestDTO workflowrequestDto, BindingResult result);

	public XtremandResponse getTriggerComponentData(TriggerComponentType type, Integer loggedInUserId);

	public XtremandResponse getById(Integer workflowId, Integer loggedInUserId);


	public XtremandResponse findPartnerQueryBuilderData(Integer loggedInUserId);

	public XtremandResponse findDefaultTriggerOptions(Integer loggedInUserId);

	public XtremandResponse findTriggerTitles(Integer loggedInUserId);

	public XtremandResponse findAll(Pageable pageable, BindingResult result, Integer loggedInUserId);

	public XtremandResponse deleteWorkflow(Integer id, Integer loggedInUserId);

	public XtremandResponse update(WorkflowRequestDTO workflowrequestDto, BindingResult result);
	//XNFR-921
	public void savePlaybookWorkFlows(List<WorkflowRequestDTO> workflowrequestDtos, Integer playbookId, List<Integer>deletedWorkflowIds, String playbookName);
	
	public XtremandResponse  getWorkflowsByPlaybookId(Integer playbookId);
}
