package com.xtremand.partner.journey.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;

import com.xtremand.common.bom.Pagination.SORTINGORDER;
import com.xtremand.partner.journey.bom.TriggerComponentType;
import com.xtremand.partner.journey.dto.WorkflowRequestDTO;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.userlist.dao.UserListDAO;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dto.Pageable;
import com.xtremand.util.dto.XamplifyUtilValidator;
import com.xtremand.util.dto.XamplifyUtility;
import com.xtremand.validator.PageableValidator;
import com.xtremand.workflow.dao.WorkflowDAO;

@Component
public class PartnerJourneyWorkFlowValidator implements Validator {

	/**** Title ***/
	@Value("${partnerJourney.workflow.title}")
	private String titleParameter;

	@Value("${partnerJourney.workflow.title.missing}")
	private String titleNameMissingParameterErrorMessage;

	@Value("${partnerJourney.workflow.title.empty}")
	private String emptyTitleErrorMessage;

	@Value("${partnerJourney.workflow.title.invalid}")
	private String invalidTitleNameErrorMessage;

	@Value("${partnerJourney.workflow.title.duplicate}")
	private String duplicateTitleErrorMessage;

	/**** Subject Id ***/
	@Value("${partnerJourney.workflow.subjectId}")
	private String subjectIdParameter;

	@Value("${partnerJourney.workflow.subjectId.missing}")
	private String subjectIdMissingParameterErrorMessage;

	@Value("${partnerJourney.workflow.subjectId.empty}")
	private String emptySubjectIdErrorMessage;

	@Value("${partnerJourney.workflow.subjectId.invalid}")
	private String invalidSubjectIdErrorMessage;

	/**** Action Id ***/
	@Value("${partnerJourney.workflow.actionId}")
	private String actionIdParameter;

	@Value("${partnerJourney.workflow.actionId.missing}")
	private String actionIdMissingParameterErrorMessage;

	@Value("${partnerJourney.workflow.actionId.empty}")
	private String emptyActionIdErrorMessage;

	@Value("${partnerJourney.workflow.actionId.invalid}")
	private String invalidActionIdErrorMessage;

	/*** TimePhrase Id ***/
	@Value("${partnerJourney.workflow.timePhraseId}")
	private String timePhraseIdParameter;

	@Value("${partnerJourney.workflow.timePhraseId.missing}")
	private String timePhraseIdMissingParameterErrorMessage;

	@Value("${partnerJourney.workflow.timePhraseId.empty}")
	private String emptyTimePhraseIdErrorMessage;

	@Value("${partnerJourney.workflow.timePhraseId.invalid}")
	private String invalidTimePhraseIdErrorMessage;

	/*** Custom Days *****/
	@Value("${partnerJourney.workflow.customDays}")
	private String customDaysParameter;

	@Value("${partnerJourney.workflow.customDays.missing}")
	private String customDaysMissingParameterErrorMessage;

	@Value("${partnerJourney.workflow.customDays.empty}")
	private String emptyCustomDaysErrorMessage;

	/*** Selected Partner List Ids ***/
	@Value("${partnerJourney.workflow.selectedPartnerListIds}")
	private String selectedPartnerListIdsParameter;

	@Value("${partnerJourney.workflow.selectedPartnerListIds.missing}")
	private String selectedPartnerListIdsMissingParameterErrorMessage;

	@Value("${partnerJourney.workflow.selectedPartnerListIds.empty}")
	private String emptySelectedPartnerListIdsErrorMessage;

	@Value("${partnerJourney.workflow.selectedPartnerListIds.invalid}")
	private String invalidSelectedPartnerListIdsErrorMessage;

	/*** Notification Subject ******/
	@Value("${partnerJourney.workflow.notificationSubject}")
	private String notificationSubjectParameter;

	@Value("${partnerJourney.workflow.notificationSubject.missing}")
	private String notificationSubjectMissingParameterErrorMessage;

	@Value("${partnerJourney.workflow.notificationSubject.empty}")
	private String emptyNotificationSubjectErrorMessage;

	@Value("${partnerJourney.workflow.notificationSubject.invalid}")
	private String invalidNotificationSubjectErrorMessage;

	/**** PreHeader ***/
	@Value("${partnerJourney.workflow.preHeader}")
	private String preHeaderParameter;

	@Value("${partnerJourney.workflow.preHeader.missing}")
	private String preHeaderMissingParameterErrorMessage;

	@Value("${partnerJourney.workflow.preHeader.empty}")
	private String emptyPreHeaderErrorMessage;

	/**** Notification Message ***/
	@Value("${partnerJourney.workflow.notificationMessage}")
	private String notificationMessageParameter;

	@Value("${partnerJourney.workflow.notificationMessage.missing}")
	private String notificationMessageMissingParameterErrorMessage;

	@Value("${partnerJourney.workflow.notificationMessage.empty}")
	private String emptyNotificationMessageErrorMessage;

	@Value("${partnerJourney.workflow.notificationMessage.invalid}")
	private String invalidNotificationMessageSubjectErrorMessage;

	/*** Template Id *****/
	@Value("${partnerJourney.workflow.templateId}")
	private String templateIdParameter;

	@Value("${partnerJourney.workflow.templateId.missing}")
	private String templateIdMissingParameterErrorMessage;

	@Value("${partnerJourney.workflow.templateId.empty}")
	private String emptyTemplateIdErrorMessage;

	@Value("${partnerJourney.workflow.templateId.invalid}")
	private String invalidTemplateIdErrorMessage;

	/****** LoggedInUserId *****/
	@Value("${partnerJourney.workflow.loggedInUserId}")
	private String loggedInUserIdParameter;

	@Value("${partnerJourney.workflow.loggedInUserId.missing}")
	private String loggedInUserIdMissingParameterErrorMessage;

	@Value("${partnerJourney.workflow.loggedInUserId.empty}")
	private String emptyLoggedInUserIdErrorMessage;

	@Value("${partnerJourney.workflow.loggedInUserId.invalid}")
	private String invalidLoggedInUserIdErrorMessage;

	/****** FormEmailUserId *****/
	@Value("${partnerJourney.workflow.fromEmailUserId}")
	private String fromEmailUserIdParameter;

	@Value("${partnerJourney.workflow.fromEmailUserId.missing}")
	private String fromEmailUserIdMissingParameterErrorMessage;

	@Value("${partnerJourney.workflow.fromEmailUserId.empty}")
	private String emptyFromEmailUserIdErrorMessage;

	@Value("${partnerJourney.workflow.fromEmailUserId.invalid}")
	private String invalidFromEmailUserIdErrorMessage;

	/*** Selected Partner List Ids ***/
	@Value("${partnerJourney.workflow.selectedPartnerIds}")
	private String selectedPartnerIdsParameter;

	@Value("${partnerJourney.workflow.selectedPartnerIds.missing}")
	private String selectedPartnerIdsMissingParameterErrorMessage;

	@Value("${partnerJourney.workflow.selectedPartnerIds.empty}")
	private String emptySelectedPartnerIdsErrorMessage;

	@Value("${partnerJourney.workflow.selectedPartnerIds.invalid}")
	private String invalidSelectedPartnerIdsErrorMessage;

	/**** Sort Options ******/

	@Value("${pageable.sort.parameter}")
	private String sortParameter;

	@Value("${pageable.sort.invalid}")
	private String invalidSort;

	@Value("${partnerJourney.workflow.sort.columns}")
	private String sortColumnsString;

	@Value("${partnerJourney.workflow.invalid.sort.column}")
	private String invalidSortColumn;

	@Value("${text.pattern}")
	private String textPattern;

	@Autowired
	private WorkflowDAO workflowDao;

	@Autowired
	private UserListDAO userListDao;

	@Autowired
	private UserDAO userDao;

	@Autowired
	private PageableValidator pagebleValidator;

	@Autowired
	private XamplifyUtilValidator xamplifyUtilValidator;

	@Override
	public boolean supports(Class<?> clazz) {
		return WorkflowRequestDTO.class.isAssignableFrom(clazz);

	}

	@Override
	public void validate(Object target, Errors errors) {
		WorkflowRequestDTO workflowRequestDTO = (WorkflowRequestDTO) target;
		validateAllProperties(errors, workflowRequestDTO);

	}

	private void validateAllProperties(Errors errors, WorkflowRequestDTO workflowRequestDTO) {
		/**** Title ****/
		validateTitle(errors, workflowRequestDTO);

		/*** Subject Id ****/
		validateSubjectId(errors, workflowRequestDTO);

		/**** Action Id ****/
		validateActionId(errors, workflowRequestDTO);

		/**** TimePhrase Id ******/
		validateTimePhraseId(errors, workflowRequestDTO);

		/******* LoggedInUserId ******/
		validateLoggedInUserId(errors, workflowRequestDTO);

		/*** SelectedPartnerListIds *****/
		if (workflowRequestDTO.getLearningTrackId() == null || workflowRequestDTO.getLearningTrackId() == 0) {
			if (workflowRequestDTO.isPartnerGroupSelected()) {
				validateSelectedPartnerListIds(errors, workflowRequestDTO);
			} else {
				validateSelectedPartnerIds(errors, workflowRequestDTO);
			}
		}
		/*** Notification Subject *****/
		validateNotificationSubject(errors, workflowRequestDTO);

		/*** PreHeader ****/
		validatePreHeader(errors, workflowRequestDTO);

		/**** From Email User Id ******/
		validateFromEmailUserId(errors, workflowRequestDTO);

		/******* Template Id & Notification Message *****/
		boolean isTemplateSelected = workflowRequestDTO.isCustomTemplateSelected();
		if (isTemplateSelected) {
			validateTemplateId(errors, workflowRequestDTO);
		} else {
			validateNotificationMessage(errors, workflowRequestDTO);
		}

		/*** Custom Days *****/
		validateCustomDays(errors, workflowRequestDTO);
	}

	private void validateFromEmailUserId(Errors errors, WorkflowRequestDTO workflowRequestDTO) {
		Integer fromEmailUserId = workflowRequestDTO.getFromEmailUserId();
		Integer loggedInUserId = workflowRequestDTO.getLoggedInUserId();
		if (fromEmailUserId == null) {
			addFieldError(errors, fromEmailUserIdParameter, fromEmailUserIdMissingParameterErrorMessage);
		} else if (fromEmailUserId <= 0) {
			addFieldError(errors, fromEmailUserIdParameter, emptyFromEmailUserIdErrorMessage);
		} else if (fromEmailUserId > 0 && loggedInUserId != null && loggedInUserId > 0) {
			List<Integer> userIds = userDao.listAllUserIdsByCompanyId(workflowRequestDTO.getCompanyId());
			if (userIds != null && !userIds.isEmpty() && userIds.indexOf(fromEmailUserId) < 0) {
				addFieldError(errors, fromEmailUserIdParameter, invalidFromEmailUserIdErrorMessage);
			}
		}
	}

	private void validatePreHeader(Errors errors, WorkflowRequestDTO workflowRequestDTO) {
		String preHeader = workflowRequestDTO.getPreHeader();
		if (preHeader == null) {
			addFieldError(errors, preHeaderParameter, preHeaderMissingParameterErrorMessage);
		} else if (!StringUtils.hasText(preHeader)) {
			addFieldError(errors, preHeaderParameter, emptyPreHeaderErrorMessage);
		}
	}

	private void validateCustomDays(Errors errors, WorkflowRequestDTO workflowRequestDTO) {
		Integer timePhraseIdForCustomDays = workflowDao.findCustomDaysActionId();
		Integer selectedTimePhraseId = workflowRequestDTO.getTimePhraseId();
		if (timePhraseIdForCustomDays.equals(selectedTimePhraseId)) {
			Integer customDays = workflowRequestDTO.getCustomDays();
			if (customDays == null) {
				addFieldError(errors, customDaysParameter, customDaysMissingParameterErrorMessage);
			} else if (customDays <= 0) {
				addFieldError(errors, customDaysParameter, emptyCustomDaysErrorMessage);
			}
		}
	}

	private void validateTemplateId(Errors errors, WorkflowRequestDTO workflowRequestDTO) {
		Integer templateId = workflowRequestDTO.getTemplateId();
		if (templateId == null) {
			addFieldError(errors, templateIdParameter, templateIdMissingParameterErrorMessage);
		} else if (templateId <= 0) {
			addFieldError(errors, templateIdParameter, emptyTemplateIdErrorMessage);
		} else if (templateId > 0) {
		}
	}

	private void validateNotificationMessage(Errors errors, WorkflowRequestDTO workflowRequestDTO) {
		String notificationMessage = workflowRequestDTO.getNotificationMessage();
		if (notificationMessage == null) {
			addFieldError(errors, notificationMessageParameter, notificationMessageMissingParameterErrorMessage);
		} else if (!StringUtils.hasText(notificationMessage)) {
			addFieldError(errors, notificationMessageParameter, emptyNotificationMessageErrorMessage);
		}
	}

	private void validateNotificationSubject(Errors errors, WorkflowRequestDTO workflowRequestDTO) {
		String notificationSubject = workflowRequestDTO.getNotificationSubject();
		if (notificationSubject == null) {
			addFieldError(errors, notificationSubjectParameter, notificationSubjectMissingParameterErrorMessage);
		} else if (!StringUtils.hasText(notificationSubject)) {
			addFieldError(errors, notificationSubjectParameter, emptyNotificationSubjectErrorMessage);
		}
	}

	private void validateSelectedPartnerListIds(Errors errors, WorkflowRequestDTO workflowRequestDTO) {
		Set<Integer> selectedPartnerListIds = workflowRequestDTO.getSelectedPartnerListIds();
		if (selectedPartnerListIds == null) {
			addFieldError(errors, selectedPartnerListIdsParameter, selectedPartnerListIdsMissingParameterErrorMessage);
		} else if (selectedPartnerListIds.isEmpty()) {
			addFieldError(errors, selectedPartnerListIdsParameter, emptySelectedPartnerListIdsErrorMessage);
		} else if (!selectedPartnerListIds.isEmpty()) {
			Integer loggedInUserId = workflowRequestDTO.getLoggedInUserId();
			if (loggedInUserId != null && loggedInUserId > 0) {
				Integer companyId = workflowRequestDTO.getCompanyId();
				List<Integer> partnerListIds = userListDao.findPartnerListIdsByCompanyId(companyId);
				List<Integer> selectedParnterListIdsForComparision = new ArrayList<>();
				selectedParnterListIdsForComparision.addAll(selectedPartnerListIds);
				selectedParnterListIdsForComparision.removeAll(partnerListIds);
				if (!selectedParnterListIdsForComparision.isEmpty()) {
					addFieldError(errors, selectedPartnerListIdsParameter,
							invalidSelectedPartnerListIdsErrorMessage + selectedParnterListIdsForComparision);
				}
			}

		}

	}

	private void validateLoggedInUserId(Errors errors, WorkflowRequestDTO workflowRequestDTO) {
		Integer loggedInUserId = workflowRequestDTO.getLoggedInUserId();
		if (loggedInUserId == null) {
			addFieldError(errors, loggedInUserIdParameter, loggedInUserIdMissingParameterErrorMessage);
		} else if (loggedInUserId <= 0) {
			addFieldError(errors, loggedInUserIdParameter, emptyLoggedInUserIdErrorMessage);
		} else if (loggedInUserId > 0) {
			Integer companyId = userDao.getCompanyIdByUserId(workflowRequestDTO.getLoggedInUserId());
			workflowRequestDTO.setCompanyId(companyId);

		}
	}

	private void validateTimePhraseId(Errors errors, WorkflowRequestDTO workflowRequestDTO) {
		Integer timePhraseId = workflowRequestDTO.getTimePhraseId();
		if (timePhraseId == null) {
			addFieldError(errors, timePhraseIdParameter, timePhraseIdMissingParameterErrorMessage);
		} else if (timePhraseId <= 0) {
			addFieldError(errors, timePhraseIdParameter, emptyTimePhraseIdErrorMessage);
		} else if (timePhraseId > 0) {
			List<Integer> timePhraseIds = workflowDao.findIdsByType(TriggerComponentType.TIME_PHRASE);
			if (timePhraseIds != null && !timePhraseIds.isEmpty()) {
				if (timePhraseIds.indexOf(timePhraseId) < 0) {
					addFieldError(errors, timePhraseIdParameter, invalidTimePhraseIdErrorMessage);
				}
			} else {
				addFieldError(errors, timePhraseIdParameter, invalidTimePhraseIdErrorMessage);
			}
		}

	}

	private void validateActionId(Errors errors, WorkflowRequestDTO workflowRequestDTO) {
		Integer actionId = workflowRequestDTO.getActionId();
		if (actionId == null) {
			addFieldError(errors, actionIdParameter, actionIdMissingParameterErrorMessage);
		} else if (actionId <= 0) {
			addFieldError(errors, actionIdParameter, emptyActionIdErrorMessage);
		} else if (actionId > 0) {
			List<Integer> actionIds = workflowDao.findIdsByType(TriggerComponentType.ACTION);
			if (actionIds != null && !actionIds.isEmpty()) {
				if (actionIds.indexOf(actionId) < 0) {
					addFieldError(errors, actionIdParameter, invalidActionIdErrorMessage);
				}
			} else {
				addFieldError(errors, actionIdParameter, invalidActionIdErrorMessage);
			}
		}
	}

	private void validateTitle(Errors errors, WorkflowRequestDTO workflowRequestDTO) {
		String title = workflowRequestDTO.getTitle();
		if (title == null) {
			addFieldError(errors, titleParameter, titleNameMissingParameterErrorMessage);
		} else if (!StringUtils.hasText(title)) {
			addFieldError(errors, titleParameter, emptyTitleErrorMessage);
		} else if (StringUtils.hasText(title)) {
			validatePattern(errors, title, titleParameter, invalidTitleNameErrorMessage);
			checkDuplicateTitle(errors, workflowRequestDTO, title);
		}
	}

	private void validatePattern(Errors errors, String propertyName, String parameter, String errorMessage) {
		boolean isValidPattern = xamplifyUtilValidator.isValidText(propertyName);
		if (!isValidPattern) {
			addFieldError(errors, parameter, errorMessage);
		}
	}

	private void checkDuplicateTitle(Errors errors, WorkflowRequestDTO workflowRequestDTO, String title) {
		boolean isEdit = workflowRequestDTO.getId() != null && workflowRequestDTO.getId() > 0;
		List<String> existingTitles = new ArrayList<>();
		if (isEdit) {
			existingTitles.addAll(workflowDao.findAllTriggerTitlesByUserIdAndExcludeTitleById(
					workflowRequestDTO.getLoggedInUserId(), workflowRequestDTO.getId()));
		} else {
			existingTitles.addAll(workflowDao.findAllTriggerTitlesByUserId(workflowRequestDTO.getLoggedInUserId()));
		}

		String lowerCaseTitle = title.toLowerCase();
		if (!existingTitles.isEmpty()) {
			boolean isDuplicateTitle = existingTitles.indexOf(lowerCaseTitle) > -1;
			if (isDuplicateTitle) {
				String errorMessage = title + " " + duplicateTitleErrorMessage;
				addFieldError(errors, titleParameter, errorMessage);
			}
		}
	}

	private void validateSubjectId(Errors errors, WorkflowRequestDTO workflowRequestDTO) {
		Integer subjectId = workflowRequestDTO.getSubjectId();
		if (subjectId == null) {
			addFieldError(errors, subjectIdParameter, subjectIdMissingParameterErrorMessage);
		} else if (subjectId <= 0) {
			addFieldError(errors, subjectIdParameter, emptySubjectIdErrorMessage);
		} else if (subjectId > 0) {
			List<Integer> subjectIds = workflowDao.findIdsByType(TriggerComponentType.SUBJECT);
			if (subjectIds != null && !subjectIds.isEmpty()) {
				if (subjectIds.indexOf(subjectId) < 0) {
					addFieldError(errors, subjectIdParameter, invalidSubjectIdErrorMessage);
				}
			} else {
				addFieldError(errors, subjectIdParameter, invalidSubjectIdErrorMessage);
			}
		}
	}

	private void addFieldError(Errors errors, String field, String errorMessage) {
		errors.rejectValue(field, "", errorMessage);
	}

	public void validatePagableParameters(Object target, Errors errors) {
		pagebleValidator.validate(target, errors);
		FieldError sortFieldError = errors.getFieldError(sortParameter);
		if (sortFieldError == null) {
			String sort = ((Pageable) target).getSort();
			if (sort != null) {
				List<String> sortColumnAndOrder = XamplifyUtils.convertStringToArrayList(sort);
				String sortColumn = sortColumnAndOrder.get(0);
				String sortOrder = sortColumnAndOrder.get(1);
				boolean ascendingOrder = SORTINGORDER.ASC.name().equalsIgnoreCase(sortOrder);
				boolean descendingOrder = SORTINGORDER.DESC.name().equalsIgnoreCase(sortOrder);
				if (ascendingOrder || descendingOrder) {
					List<String> sortColumns = XamplifyUtils.convertStringToArrayList(sortColumnsString);
					if (sortColumns.indexOf(sortColumn) < 0) {
						XamplifyUtility.setRejectedValue(errors, sortParameter, invalidSortColumn);
					}
				} else {
					XamplifyUtility.setRejectedValue(errors, sortParameter, invalidSort);
				}
			}

		}
	}

	public void validateId(Integer id, Integer loggedInUserId, boolean isDelete) {
		if (id != null && id > 0) {
			Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
			List<Integer> ids = workflowDao.findIdsByCompanyId(companyId);
			if (ids.indexOf(id) < 0) {
				throwInvalidIdException(id, isDelete);
			}
		} else {
			throwInvalidIdException(id, isDelete);
		}
	}

	public void throwInvalidIdException(Integer id, boolean isDelete) {
		String message = isDelete ? "Unable to delete the record for " + id : "Unable to find details for " + id;
		throw new AccessDeniedException(message);
	}

	public void validateUpdateWorkflowProperties(WorkflowRequestDTO workflowRequestDTO, Errors errors) {
		validateId(workflowRequestDTO.getId(), workflowRequestDTO.getLoggedInUserId(), false);
		validateAllProperties(errors, workflowRequestDTO);

	}

	// XNFR-921
	private void validateSelectedPartnerIds(Errors errors, WorkflowRequestDTO workflowRequestDTO) {
		Set<Integer> selectedPartnerIds = workflowRequestDTO.getSelectedPartnerIds();
		if (selectedPartnerIds == null) {
			addFieldError(errors, selectedPartnerIdsParameter, selectedPartnerIdsMissingParameterErrorMessage);
		} else if (selectedPartnerIds.isEmpty()) {
			addFieldError(errors, selectedPartnerIdsParameter, emptySelectedPartnerIdsErrorMessage);
		}
	}

}
