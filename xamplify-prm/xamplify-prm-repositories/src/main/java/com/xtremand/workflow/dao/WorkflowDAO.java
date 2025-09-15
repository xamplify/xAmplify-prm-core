package com.xtremand.workflow.dao;

import java.util.List;
import java.util.Map;

import com.xtremand.common.bom.Pagination;
import com.xtremand.partner.journey.bom.TriggerComponentType;
import com.xtremand.partner.journey.bom.Workflow;
import com.xtremand.partner.journey.dto.TriggerComponentDTO;
import com.xtremand.partner.journey.dto.WorkflowResponseDTO;
import com.xtremand.user.bom.User;
import com.xtremand.util.dto.UserUserListDTO;

public interface WorkflowDAO {

	List<TriggerComponentDTO> getTriggerComponentData(TriggerComponentType type);

	List<UserUserListDTO> findPartnerDetailsForWorkFlow(Integer loggedInUserId);

	List<String> findValuesByColumnName(String columnName, Integer companyId);

	List<TriggerComponentDTO> findDefaultTriggerComponentsByTimePhraseAndActionAndSubjectAndFilterProperty();

	List<Workflow> getAllActiveWorkflows();

	List<User> listNotRedistributedCampaignUsers(Workflow workflow);

	List<User> listRedistributedCampaignUsers(Workflow workflow);

	List<User> listNotSignedUpUsers(Workflow workflow);

	List<User> listSignedUpUsers(Workflow workflow);

	List<User> listNotActivatedUsers(Workflow workflow);

	List<User> listActivatedUsers(Workflow workflow);

	List<User> listCreatedCompanyProfileUsers(Workflow workflow);

	List<User> listNotCreatedCompanyProfileUsers(Workflow workflow);

	List<User> listAddedTeamMemberUsers(Workflow workflow);

	List<User> listNotAddedTeamMemberUsers(Workflow workflow);

	List<User> listCreatedDealUsers(Workflow workflow);

	List<User> listNotCreatedDealUsers(Workflow workflow);

	List<User> listCreatedLeadUsers(Workflow workflow);

	List<User> listNotCreatedLeadUsers(Workflow workflow);

	List<User> listConvertedLeadUsers(Workflow workflow);

	List<User> listNotConvertedLeadUsers(Workflow workflow);

	List<User> listClosedDealUsers(Workflow workflow);

	List<User> listNotClosedDealUsers(Workflow workflow);

	List<User> listCompletedPlaybookUsers(Workflow workflow);

	List<User> listNotCompletedPlaybookUsers(Workflow workflow);

	List<User> listCompletedTrackUsers(Workflow workflow);

	List<User> listNotCompletedTrackUsers(Workflow workflow);

	List<User> listAddedContactUsers(Workflow workflow);

	List<User> listNotAddedContactUsers(Workflow workflow);
	
	List<User> listRedistributedShareLeadUsers(Workflow workflow);
	
	List<User> listNotRedistributedShareLeadUsers(Workflow workflow);

	List<User> listRequestedMdfUsers(Workflow workflow);

	List<User> listNotRequestedMdfUsers(Workflow workflow);

	List<User> listViewedPagesUsers(Workflow workflow);

	List<User> listNotViewedPagesUsers(Workflow workflow);

	List<User> listViewedPlaybookUsers(Workflow workflow);

	List<User> listNotViewedPlaybookUsers(Workflow workflow);

	List<User> listViewedTrackUsers(Workflow workflow);

	List<User> listNotViewedTrackUsers(Workflow workflow);

	List<Integer> findIdsByType(TriggerComponentType triggerComponentType);

	List<String> findAllTriggerTitlesByUserId(Integer loggedInUserId);

	List<String> findAllTriggerTitlesByUserIdAndExcludeTitleById(Integer loggedInUserId, Integer workflowId);

	Integer findCustomDaysActionId();

	Map<String, Object> findAll(Pagination pagination, String searchKey);

	List<Integer> findIdsByCompanyId(Integer companyId);

	void delete(Integer id);

	WorkflowResponseDTO findById(Integer id);

	List<Integer> findSelectedPartnerGroupIdsByWorkflowId(Integer workflowId, boolean isPartnerGroupSelected);

	void deleteWorkflowUserLists(List<Integer> userListIds);

	void saveWorkflowUserLists(List<Integer> userListIds, Workflow workflow);
	
	//XNFR-921
	void deleteWorkflowPartners(List<Integer> partnerIds);
	
	void saveWorkflowPartners(List<Integer> partnerIds, Workflow workflow);
	
	void deleteWorkflowUserListsByWorkFlowId(Integer workFlowId);
	
	void deleteWorkflowPartnersByWorkFlowId(Integer workFlowId);

	List<WorkflowResponseDTO> getWorkflowsByPlaybookId(Integer playbookId);
	
	public void deleteWorkflowByWorkFlowIds(List<Integer> workFlowIds);
	
	public List<Workflow> getAllActivePlabookWorkflows();
	
	//XNFR-993
	public List<Integer> getUserIdsByWorkflowId(Integer workflowId);
	
	public List<User> listNotViewedPlaybookUsersV2(Workflow workflow);
	
	public List<User> listViewedPlaybookUsersV2(Workflow workflow);
	
	public List<User> listNotCompletedPlaybookUsersV2(Workflow workflow); 
	
	public List<User> listCompletedPlaybookUsersV2(Workflow workflow);

}
