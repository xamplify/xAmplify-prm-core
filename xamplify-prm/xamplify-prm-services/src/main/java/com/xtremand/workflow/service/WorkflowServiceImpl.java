package com.xtremand.workflow.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import com.xtremand.campaign.bom.WorkflowsStatusEnum;
import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.Pagination;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.lms.bom.LearningTrack;
import com.xtremand.mail.bom.EmailTemplate;
import com.xtremand.partner.journey.bom.TriggerComponent;
import com.xtremand.partner.journey.bom.TriggerComponentType;
import com.xtremand.partner.journey.bom.Workflow;
import com.xtremand.partner.journey.bom.WorkflowPartner;
import com.xtremand.partner.journey.bom.WorkflowUserlist;
import com.xtremand.partner.journey.dto.TriggerComponentDTO;
import com.xtremand.partner.journey.dto.WorkflowRequestDTO;
import com.xtremand.partner.journey.validator.PartnerJourneyWorkFlowValidator;
import com.xtremand.user.bom.User;
import com.xtremand.user.bom.UserList;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.user.service.UserService;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.dto.Pageable;
import com.xtremand.util.dto.XamplifyUtilValidator;
import com.xtremand.util.service.UtilService;
import com.xtremand.workflow.dao.WorkflowDAO;

@Service("WorkflowService")
@Transactional
public class WorkflowServiceImpl implements WorkflowService {

	@Autowired
	private UserService userService;

	@Autowired
	private GenericDAO genericDAO;

	@Autowired
	private WorkflowDAO workflowDAO;

	@Autowired
	private UserDAO userDao;

	@Autowired
	private UtilService utilService;

	@Autowired
	UtilDao utilDAO;

	@Autowired
	private PartnerJourneyWorkFlowValidator partnerJourneyWorkFlowValidator;

	@Autowired
	private XamplifyUtilValidator xamplifyUtilValidator;

	private static final String UNAUTHORIZED = "UnAuthorized";
	private static final String SUCCESS = "Success";
	private static final String FAILED = "Failed";
	private static final String INVALID_INPUT = "Invalid Input";

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse save(WorkflowRequestDTO workflowRequestDTO, BindingResult result) {
		XtremandResponse response = new XtremandResponse();
		partnerJourneyWorkFlowValidator.validate(workflowRequestDTO, result);
		if (result.hasErrors()) {
			xamplifyUtilValidator.addErrorResponse(result, response);
		} else {
			saveWorkFlowData(workflowRequestDTO);
			XamplifyUtils.addSuccessStatusWithMessage(response, "Workflow created successfully");
		}

		return response;
	}

	private void saveWorkFlowData(WorkflowRequestDTO workflowRequestDTO) {
		Workflow workflow = new Workflow();
		workflow.setCreatedBy(workflowRequestDTO.getLoggedInUserId());
		workflow.setCreatedTime(new Date());
		workflow.setPartnerGroupSelected(workflowRequestDTO.isPartnerGroupSelected());
		setWorkflowDTOProperties(workflowRequestDTO, workflow);
		if (workflowRequestDTO.getPlaybookId() != null) {
			LearningTrack playbook = new LearningTrack();
			playbook.setId(workflowRequestDTO.getPlaybookId());
			workflow.setLearningTrack(playbook);
		}
		addPartnerUserLists(workflowRequestDTO, workflow);
		addPartners(workflowRequestDTO, workflow);
		genericDAO.save(workflow);
	}

	private void setWorkflowDTOProperties(WorkflowRequestDTO workflowRequestDTO, Workflow workflow) {
		Integer loggedInUserId = workflowRequestDTO.getLoggedInUserId();
		workflow.setTitle(workflowRequestDTO.getTitle());
		CompanyProfile loggedInCompany = userService.getCompanyProfileByUser(loggedInUserId);
		workflow.setCompany(loggedInCompany);
		TriggerComponent triggerSubject = new TriggerComponent();
		triggerSubject.setId(workflowRequestDTO.getSubjectId());
		workflow.setTriggerSubject(triggerSubject);
		TriggerComponent triggerAction = new TriggerComponent();
		triggerAction.setId(workflowRequestDTO.getActionId());
		workflow.setTriggerAction(triggerAction);
		Integer timePhraseId = workflowRequestDTO.getTimePhraseId();
		TriggerComponent timePhrase = new TriggerComponent();
		timePhrase.setId(timePhraseId);
		workflow.setTriggerTimePhrase(timePhrase);
		if (timePhraseId != null) {
			workflow.setCustomTriggerDays(workflowRequestDTO.getCustomDays());
		} else {
			workflow.setCustomTriggerDays(0);
		}
		workflow.setFilterQueryJson(workflowRequestDTO.getQueryBuilderInputString());
		workflow.setNotificationSubject(workflowRequestDTO.getNotificationSubject());
		workflow.setNotificationMessage(workflowRequestDTO.getNotificationMessage());
		workflow.setPreHeader(workflowRequestDTO.getPreHeader());
		User fromEmailUser = new User();
		fromEmailUser.setUserId(workflowRequestDTO.getFromEmailUserId());
		workflow.setFromEmailUser(fromEmailUser);
		workflow.setStatus(WorkflowsStatusEnum.ACTIVE);
		if (workflowRequestDTO.isCustomTemplateSelected()) {
			EmailTemplate emailTemplate = new EmailTemplate();
			emailTemplate.setId(workflowRequestDTO.getTemplateId());
			workflow.setTemplate(emailTemplate);
			workflow.setNotificationMessage("");
		} else {
			workflow.setTemplate(null);
		}
		workflow.setUpdatedBy(loggedInUserId);
		workflow.setUpdatedTime(new Date());
	}

	private void addPartnerUserLists(WorkflowRequestDTO workflowRequestDTO, Workflow workflow) {
		Set<WorkflowUserlist> workflowUserlists = new HashSet<WorkflowUserlist>();
		if (!XamplifyUtils.isNotEmptySet(workflowRequestDTO.getSelectedPartnerListIds())) {
			return;
		}
		for (Integer partnerListId : workflowRequestDTO.getSelectedPartnerListIds()) {
			WorkflowUserlist workflowUserlist = new WorkflowUserlist();
			workflowUserlist.setWorkflow(workflow);
			UserList userList = new UserList();
			userList.setId(partnerListId);
			workflowUserlist.setUserList(userList);
			workflowUserlist.setCreatedBy(workflow.getCreatedBy());
			workflowUserlist.setCreatedTime(new Date());
			workflowUserlist.setUpdatedBy(workflow.getCreatedBy());
			workflowUserlist.setUpdatedTime(new Date());
			workflowUserlist.setStatus(WorkflowsStatusEnum.ACTIVE);
			workflowUserlists.add(workflowUserlist);
		}
		workflow.setUserlists(workflowUserlists);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse update(WorkflowRequestDTO workflowRequestDTO, BindingResult result) {
		XtremandResponse response = new XtremandResponse();
		Integer id = workflowRequestDTO.getId();
		try {
			partnerJourneyWorkFlowValidator.validateUpdateWorkflowProperties(workflowRequestDTO, result);
		} catch (AccessDeniedException a) {
			throw new AccessDeniedException(a.getMessage());
		}
		if (result.hasErrors()) {
			xamplifyUtilValidator.addErrorResponse(result, response);
		} else {
			updateWorkFlowData(workflowRequestDTO, id);
			XamplifyUtils.addSuccessStatusWithMessage(response, "Workflow updated successfully");
		}
		return response;
	}

	private void updateWorkFlowData(WorkflowRequestDTO workflowRequestDTO, Integer id) {
		Workflow workflow = genericDAO.get(Workflow.class, id);
		workflow.setPartnerGroupSelected(workflowRequestDTO.isPartnerGroupSelected());

		setWorkflowDTOProperties(workflowRequestDTO, workflow);
		updateWorkflowPartnerListDetails(workflowRequestDTO, id, workflow);
		updateWorkflowPartnerDetails(workflowRequestDTO, id, workflow);
	}

	private void updateWorkflowPartnerListDetails(WorkflowRequestDTO workflowRequestDTO, Integer id,
			Workflow workflow) {
		List<Integer> selectedPartnerListIds = XamplifyUtils
				.convertSetToList(workflowRequestDTO.getSelectedPartnerListIds());
		List<Integer> existingSavedPartnerListIds = workflowDAO.findSelectedPartnerGroupIdsByWorkflowId(id, true);
		Collections.sort(selectedPartnerListIds);
		Collections.sort(existingSavedPartnerListIds);
		if (!selectedPartnerListIds.equals(existingSavedPartnerListIds)) {
			deletePartnerLists(selectedPartnerListIds, existingSavedPartnerListIds);
			insertPartnerLists(id, selectedPartnerListIds, workflow);
		}
	}

	private void insertPartnerLists(Integer id, List<Integer> selectedPartnerListIds, Workflow workflow) {
		List<Integer> existingSavedPartnerListIds = workflowDAO.findSelectedPartnerGroupIdsByWorkflowId(id, true);
		if (!selectedPartnerListIds.equals(existingSavedPartnerListIds)) {
			selectedPartnerListIds.removeAll(existingSavedPartnerListIds);
		}
		if (XamplifyUtils.isNotEmptyList(selectedPartnerListIds)) {
			workflowDAO.saveWorkflowUserLists(selectedPartnerListIds, workflow);
		}

	}

	private void deletePartnerLists(List<Integer> selectedPartnerListIds, List<Integer> existingSavedPartnerListIds) {
		existingSavedPartnerListIds.removeAll(selectedPartnerListIds);
		workflowDAO.deleteWorkflowUserLists(existingSavedPartnerListIds);
	}

	@Override
	public XtremandResponse getTriggerComponentData(TriggerComponentType type, Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (loggedInUserId != null && loggedInUserId > 0 && type != null) {
			CompanyProfile loggedInCompany = userService.getCompanyProfileByUser(loggedInUserId);
			if (loggedInCompany != null) {
				List<TriggerComponentDTO> triggerComponentDTOList = workflowDAO.getTriggerComponentData(type);
				if (triggerComponentDTOList != null) {
					response.setData(triggerComponentDTOList);
				}
				responseMessage = SUCCESS;
				responseStatusCode = 200;
			} else {
				responseMessage = UNAUTHORIZED;
				responseStatusCode = 401;
			}
		} else {
			responseMessage = INVALID_INPUT;
			responseStatusCode = 500;
		}
		response.setMessage(responseMessage);
		response.setStatusCode(responseStatusCode);
		return response;
	}

	@Override
	public XtremandResponse getById(Integer workflowId, Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		partnerJourneyWorkFlowValidator.validateId(workflowId, loggedInUserId, false);
		response.setData(workflowDAO.findById(workflowId));
		response.setStatusCode(200);
		return response;
	}

	@Override
	public XtremandResponse findPartnerQueryBuilderData(Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(200);
		Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
		List<TriggerComponentDTO> triggerComponentDtos = workflowDAO
				.getTriggerComponentData(TriggerComponentType.FILTER_PROPERTY);
		JSONObject jsonObject = new JSONObject();
		JSONObject filedsJsonObject = new JSONObject();
		setQueryBuilderJsonInput(companyId, triggerComponentDtos, jsonObject);
		filedsJsonObject.put("fields", jsonObject);
		response.setData(filedsJsonObject.toMap());
		return response;
	}

	private JSONObject getJsonObject(String value, List<String> options) {
		JSONObject fieldOptions = new JSONObject();
		fieldOptions.put("name", value);
		fieldOptions.put("type", "category");
		JSONArray optionsArray = new JSONArray();
		for (String option : options) {
			JSONObject jobTitleJsonOptions = new JSONObject();
			jobTitleJsonOptions.put("name", option);
			jobTitleJsonOptions.put("value", option);
			optionsArray.put(jobTitleJsonOptions);
		}
		fieldOptions.put("options", optionsArray);
		return fieldOptions;
	}

	public XtremandResponse findDefaultTriggerOptions(Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(200);
		Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
		List<TriggerComponentDTO> triggerComponentDtos = workflowDAO
				.findDefaultTriggerComponentsByTimePhraseAndActionAndSubjectAndFilterProperty();
		boolean isValidDtos = triggerComponentDtos != null && !triggerComponentDtos.isEmpty();
		List<TriggerComponentDTO> filterPropertyComponents = new ArrayList<TriggerComponentDTO>();
		List<TriggerComponentDTO> subjects = new ArrayList<TriggerComponentDTO>();
		List<TriggerComponentDTO> actions = new ArrayList<TriggerComponentDTO>();
		List<TriggerComponentDTO> timePhrases = new ArrayList<TriggerComponentDTO>();
		JSONObject jsonObject = new JSONObject();
		JSONObject filedsJsonObject = new JSONObject();
		if (isValidDtos) {
			filterPropertyComponents.addAll(
					triggerComponentDtos.stream().filter(x -> x.getType().equals(TriggerComponentType.FILTER_PROPERTY))
							.collect(Collectors.toList()));
			subjects.addAll(triggerComponentDtos.stream().filter(x -> x.getType().equals(TriggerComponentType.SUBJECT))
					.collect(Collectors.toList()));
			List<TriggerComponentDTO> actionList = triggerComponentDtos.stream()
					.filter(x -> x.getType().equals(TriggerComponentType.ACTION)).collect(Collectors.toList());
			List<TriggerComponentDTO> triggerComponentActions = getActionTypeByAccess(actionList, companyId);
			actions.addAll(triggerComponentActions);
			timePhrases.addAll(triggerComponentDtos.stream()
					.filter(x -> x.getType().equals(TriggerComponentType.TIME_PHRASE)).collect(Collectors.toList()));

		}
		if (filterPropertyComponents != null && !filterPropertyComponents.isEmpty()) {
			setQueryBuilderJsonInput(companyId, filterPropertyComponents, jsonObject);

		}
		filedsJsonObject.put("fields", jsonObject);

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("queryBuilderJson", filedsJsonObject.toMap());
		map.put("triggerConditions", triggerComponentDtos);
		map.put("subjects", subjects);
		map.put("actions", actions);
		map.put("timePhrases", timePhrases);
		response.setData(map);

		return response;
	}

	private List<TriggerComponentDTO> getActionTypeByAccess(List<TriggerComponentDTO> TriggerComponentDTOs,
			Integer companyId) {
		List<TriggerComponentDTO> triggerComponentActions = new ArrayList<>();
		for (TriggerComponentDTO triggerComponentDto : TriggerComponentDTOs) {
			if (!utilDAO.isPrmByVendorCompanyId(companyId)
					&& triggerComponentDto.getKey().equalsIgnoreCase("redistributed_campaign")) {
				triggerComponentActions.add(triggerComponentDto);
			} else if (utilDAO.hasEnableLeadsAccessByCompanyId(companyId)
					&& (triggerComponentDto.getKey().equalsIgnoreCase("created_lead")
							|| triggerComponentDto.getKey().equalsIgnoreCase("created_deal")
							|| triggerComponentDto.getKey().equalsIgnoreCase("converted_lead")
							|| triggerComponentDto.getKey().equalsIgnoreCase("closed_deal"))) {
				triggerComponentActions.add(triggerComponentDto);
			} else if (utilDAO.hasMdfAccessByCompanyId(companyId)
					&& triggerComponentDto.getKey().equalsIgnoreCase("requested_mdf")) {
				triggerComponentActions.add(triggerComponentDto);
			} else if (!utilDAO.isPrmByVendorCompanyId(companyId)
					&& triggerComponentDto.getKey().equalsIgnoreCase("added_contact")) {
				triggerComponentActions.add(triggerComponentDto);
			} else if (utilDAO.hasLmsAccessByCompanyId(companyId)
					&& (triggerComponentDto.getKey().equalsIgnoreCase("viewed_track")
							|| triggerComponentDto.getKey().equalsIgnoreCase("completed_track"))) {
				triggerComponentActions.add(triggerComponentDto);
			} else if (utilDAO.hasPlaybookAccessByCompanyId(companyId)
					&& (triggerComponentDto.getKey().equalsIgnoreCase("viewed_playbook")
							|| triggerComponentDto.getKey().equalsIgnoreCase("completed_playbook"))) {
				triggerComponentActions.add(triggerComponentDto);
			} else if (utilDAO.hasShareLeadsAccessByCompanyId(companyId)
					&& triggerComponentDto.getKey().equalsIgnoreCase("redistributed_sharelead")) {
				triggerComponentActions.add(triggerComponentDto);
			} else if (triggerComponentDto.getKey().equalsIgnoreCase("activated")
					|| triggerComponentDto.getKey().equalsIgnoreCase("signed_up")
					|| triggerComponentDto.getKey().equalsIgnoreCase("created_company_profile")
					|| triggerComponentDto.getKey().equalsIgnoreCase("added_team_member")) {
				triggerComponentActions.add(triggerComponentDto);
			}
		}
		return triggerComponentActions;
	}

	private void setQueryBuilderJsonInput(Integer companyId, List<TriggerComponentDTO> filterPropertyComponents,
			JSONObject jsonObject) {
		for (TriggerComponentDTO triggerComponentDTO : filterPropertyComponents) {
			String key = triggerComponentDTO.getKey();
			String value = triggerComponentDTO.getValue();

			switch (key) {

			/******** Job Title *******/
			case "job_title":
				List<String> jobTitles = workflowDAO.findValuesByColumnName("job_title", companyId);
				if (jobTitles != null && !jobTitles.isEmpty()) {
					jsonObject.put(key, getJsonObject(value, jobTitles));
				}
				break;

			/******** Address *******/
			case "address":
				List<String> addresses = workflowDAO.findValuesByColumnName("address", companyId);
				if (addresses != null && !addresses.isEmpty()) {
					jsonObject.put(key, getJsonObject(value, addresses));
				}

				break;
			/******** City/Town *******/
			case "city":
				List<String> cities = workflowDAO.findValuesByColumnName("city", companyId);
				if (cities != null && !cities.isEmpty()) {
					jsonObject.put(key, getJsonObject(value, cities));
				}

				break;

			/******** State *******/
			case "state":
				List<String> states = workflowDAO.findValuesByColumnName("state", companyId);
				if (states != null && !states.isEmpty()) {
					jsonObject.put(key, getJsonObject(value, states));
				}
				break;
			/******** Zip Code *******/
			case "zip":
				List<String> zipCodes = workflowDAO.findValuesByColumnName("zip", companyId);
				if (zipCodes != null && !zipCodes.isEmpty()) {
					jsonObject.put(key, getJsonObject(value, zipCodes));
				}

				break;

			/******** Country *******/
			case "country":
				List<String> countries = workflowDAO.findValuesByColumnName("country", companyId);
				if (countries != null && !countries.isEmpty()) {
					jsonObject.put(key, getJsonObject(value, countries));
				}
				break;
			default:
				break;

			}

		}
	}

	@Override
	public XtremandResponse findTriggerTitles(Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		List<String> triggerTitles = workflowDAO.findAllTriggerTitlesByUserId(loggedInUserId);
		response.setData(triggerTitles);
		XamplifyUtils.addSuccessStatus(response);
		return response;
	}

	@Override
	public XtremandResponse findAll(Pageable pageable, BindingResult result, Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		partnerJourneyWorkFlowValidator.validatePagableParameters(pageable, result);
		if (result.hasErrors()) {
			xamplifyUtilValidator.addErrorResponse(result, response);
		} else {
			Pagination pagination = utilService.setPageableParameters(pageable, loggedInUserId);
			response.setData(workflowDAO.findAll(pagination, pageable.getSearch()));
			XamplifyUtils.addSuccessStatus(response);
		}
		return response;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse deleteWorkflow(Integer id, Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		partnerJourneyWorkFlowValidator.validateId(id, loggedInUserId, true);
		workflowDAO.delete(id);
		XamplifyUtils.addSuccessStatusWithMessage(response, "Workflow deleted successfully");
		return response;
	}

	// XNFR-921

	private void addPartners(WorkflowRequestDTO workflowRequestDTO, Workflow workflow) {
		Set<WorkflowPartner> workflowPartners = new HashSet<>();
		if (!XamplifyUtils.isNotEmptySet(workflowRequestDTO.getSelectedPartnerIds())) {
			return;
		}
		for (Integer partnerId : workflowRequestDTO.getSelectedPartnerIds()) {
			WorkflowPartner workflowPartner = new WorkflowPartner();
			workflowPartner.setWorkflow(workflow);
			User partner = new User();
			partner.setUserId(partnerId);
			workflowPartner.setPartner(partner);
			workflowPartner.setCreatedBy(workflow.getCreatedBy());
			workflowPartner.setCreatedTime(new Date());
			workflowPartner.setUpdatedBy(workflow.getCreatedBy());
			workflowPartner.setUpdatedTime(new Date());
			workflowPartner.setStatus(WorkflowsStatusEnum.ACTIVE);
			workflowPartners.add(workflowPartner);
		}
		workflow.setPartners(workflowPartners);
	}

	private void updateWorkflowPartnerDetails(WorkflowRequestDTO workflowRequestDTO, Integer id, Workflow workflow) {
		List<Integer> selectedPartnerIds = XamplifyUtils.convertSetToList(workflowRequestDTO.getSelectedPartnerIds());
		List<Integer> existingSavedPartnerIds = workflowDAO.findSelectedPartnerGroupIdsByWorkflowId(id, false);
		Collections.sort(selectedPartnerIds);
		Collections.sort(existingSavedPartnerIds);
		if (!selectedPartnerIds.equals(existingSavedPartnerIds)) {
			deletePartners(selectedPartnerIds, existingSavedPartnerIds);
			insertPartners(id, selectedPartnerIds, workflow);
		}
	}

	private void deletePartners(List<Integer> selectedPartnerIds, List<Integer> existingSavedPartnerIds) {
		existingSavedPartnerIds.removeAll(selectedPartnerIds);
		workflowDAO.deleteWorkflowPartners(existingSavedPartnerIds);
	}

	private void insertPartners(Integer id, List<Integer> selectedPartnerIds, Workflow workflow) {
		List<Integer> existingSavedPartnerIds = workflowDAO.findSelectedPartnerGroupIdsByWorkflowId(id, false);
		if (!selectedPartnerIds.equals(existingSavedPartnerIds)) {
			selectedPartnerIds.removeAll(existingSavedPartnerIds);
		}
		if (XamplifyUtils.isNotEmptyList(selectedPartnerIds)) {
			workflowDAO.saveWorkflowPartners(selectedPartnerIds, workflow);
		}
	}

	public void savePlaybookWorkFlows(List<WorkflowRequestDTO> workflowRequestDTOs, Integer playbookId,
			List<Integer> deletedWorkflowIds, String playbookName) {
		if (XamplifyUtils.isNotEmptyList(deletedWorkflowIds)) {
			workflowDAO.deleteWorkflowByWorkFlowIds(deletedWorkflowIds);
		}
		if (XamplifyUtils.isNotEmptyList(workflowRequestDTOs)) {
			for (WorkflowRequestDTO workflowRequestDTO : workflowRequestDTOs) {
				workflowRequestDTO.setPlaybookId(playbookId);
				if (workflowRequestDTO.getId() != null && !workflowRequestDTO.getId().equals(0)) {
					workflowRequestDTO.setTitle(playbookName + "_playbook_" + System.nanoTime());
					updateWorkFlowData(workflowRequestDTO, workflowRequestDTO.getId());
				} else {
					workflowRequestDTO.setTitle(playbookName + "_playbook_" + System.nanoTime());
					saveWorkFlowData(workflowRequestDTO);
				}
			}

		}

	}

	@Override
	public XtremandResponse getWorkflowsByPlaybookId(Integer playbookId) {
		XtremandResponse response = new XtremandResponse();
		response.setData(workflowDAO.getWorkflowsByPlaybookId(playbookId));
		XamplifyUtils.addSuccessStatus(response);
		return response;
	}
}
