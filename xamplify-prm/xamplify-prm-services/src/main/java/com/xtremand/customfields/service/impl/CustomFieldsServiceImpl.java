package com.xtremand.customfields.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.custom.field.dto.CustomFieldsDTO;
import com.xtremand.custom.field.dto.FieldLabelType;
import com.xtremand.custom.field.dto.LeadFieldLabel;
import com.xtremand.custom.field.dto.ObjectType;
import com.xtremand.customfields.dao.CustomFieldsDao;
import com.xtremand.customfields.service.CustomFieldsService;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.deal.bom.Deal;
import com.xtremand.form.bom.Form;
import com.xtremand.form.bom.FormDefaultFieldTypeEnum;
import com.xtremand.form.bom.FormFieldTypeEnum;
import com.xtremand.form.bom.FormLabel;
import com.xtremand.form.bom.FormLabelChoice;
import com.xtremand.form.bom.FormTypeEnum;
import com.xtremand.form.dao.FormDao;
import com.xtremand.form.dto.FormChoiceDTO;
import com.xtremand.form.dto.FormDTO;
import com.xtremand.form.dto.FormLabelDTO;
import com.xtremand.form.service.FormService;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.lead.bom.Lead;
import com.xtremand.lead.bom.OpportunityType;
import com.xtremand.salesforce.bom.SfCustomFieldsData;
import com.xtremand.salesforce.dto.OpportunityFormFieldsDTO;
import com.xtremand.salesforce.dto.PicklistValues;
import com.xtremand.user.bom.User;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.user.service.UserService;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.service.UtilService;

@Service
@Transactional
public class CustomFieldsServiceImpl implements CustomFieldsService {

	private static final String UNAUTHORIZED = "UnAuthorized";
	private static final String SUCCESS = "Success";
	private static final String INVALID_INPUT = "Invalid Input";
	private static final String DUPLICATE_NAME = "Duplicate Name";

	@Value("#{'${lead.default.cf.names}'.split(',')}")
	private List<String> leadDefaultFields;

	@Value("#{'${deal.default.cf.names}'.split(',')}")
	private List<String> dealDefaultFields;

	@Value("#{'${lead.default.unselect.cf.names}'.split(',')}")
	private List<String> leadDefaultUnSelectFields;

	@Autowired
	private UserDAO userDao;

	@Autowired
	private FormDao formDao;

	@Autowired
	private FormService formService;

	@Autowired
	private UtilService utilService;

	@Autowired
	private UserService userService;

	@Autowired
	private CustomFieldsDao customFieldsDao;
	
	@Autowired
	private GenericDAO genericDAO;

	@Override
	public XtremandResponse syncCustomForm(CustomFieldsDTO customFieldsDTO) {
		XtremandResponse response = new XtremandResponse();
		if (validateSyncFormInput(customFieldsDTO)) {
			Integer loggedInCompanyId = userDao.getCompanyIdByUserId(customFieldsDTO.getLoggedInUserId());
			if (loggedInCompanyId != null) {
				List<OpportunityFormFieldsDTO> newLabels = customFieldsDTO.getSelectedFields();
				FormTypeEnum formType = XamplifyUtils.getFormTypeByObjectType(customFieldsDTO.getObjectType());
				Integer formId = formDao.getSfCustomFormIdByCompanyIdAndFormType(loggedInCompanyId, formType);
				if (formId == null) {
					Set<OpportunityFormFieldsDTO> allFieldsToSave = frameFieldsToSave(newLabels,
							customFieldsDTO.getObjectType());
					saveForm(formType, new ArrayList<>(allFieldsToSave), customFieldsDTO.getLoggedInUserId());
				} else {
					updateCustomFields(customFieldsDTO, newLabels, formId, response);
				}
				responseSuccess(response);
			} else {
				responseUnAuthorized(response);
			}
		} else {
			responseInValidInput(response);
		}
		return response;
	}

	private Set<OpportunityFormFieldsDTO> frameFieldsToSave(List<OpportunityFormFieldsDTO> newLabels,
			ObjectType objectType) {
		List<OpportunityFormFieldsDTO> defaultCustomFields = objectType.equals(ObjectType.LEAD)
				? getDefaultCustomFieldsDto()
				: getDealDefaultCustomFieldsDto();
		Map<String, OpportunityFormFieldsDTO> allFieldsToSaveMap = new LinkedHashMap<>();
		for (OpportunityFormFieldsDTO field : newLabels) {
			allFieldsToSaveMap.put(field.getLabel(), field);
		}
		for (OpportunityFormFieldsDTO field : defaultCustomFields) {
			allFieldsToSaveMap.putIfAbsent(field.getLabel(), field);
		}
		Set<OpportunityFormFieldsDTO> allFieldsToSave = new LinkedHashSet<>(allFieldsToSaveMap.values());
		Set<OpportunityFormFieldsDTO> newLabelsSet = new HashSet<>(newLabels);
		for (OpportunityFormFieldsDTO field : allFieldsToSave) {
			boolean isSelected = newLabelsSet.contains(field);
			field.setActive(isSelected);
			field.setSelected(isSelected);
		}
		return allFieldsToSave;
	}

	private void updateCustomFields(CustomFieldsDTO customFieldsDTO, List<OpportunityFormFieldsDTO> newLabels,
			Integer formId, XtremandResponse response) {
		Form form = formDao.getById(formId);
		List<FormLabel> existingLabels = form.getFormLabels();
		List<String> existingLabelIds = existingLabels.stream().map(FormLabel::getLabelId).collect(Collectors.toList());
		List<String> newLabelIds = newLabels.stream().map(OpportunityFormFieldsDTO::getName)
				.collect(Collectors.toList());
		existingLabelIds.removeAll(newLabelIds);
		for (String labelId : existingLabelIds) {
			List<FormLabel> formLabel = existingLabels.stream().filter(field -> (field.getLabelId().equals(labelId)))
					.collect(Collectors.toList());
			Integer formLabelId = formLabel.get(0).getId();
			customFieldsDao.updateFormFields(formLabelId);
		}
		updateForm(form, newLabels, customFieldsDTO.getLoggedInUserId(), response);
	}

	private void saveForm(FormTypeEnum formType, List<OpportunityFormFieldsDTO> newLabels, Integer loggedInUserId) {
		List<FormLabel> existingLabels = new ArrayList<>();
		FormDTO formDto = frameFormFields(formType, newLabels, loggedInUserId, existingLabels);
		formService.save(formDto, null);
	}

	private void updateForm(Form form, List<OpportunityFormFieldsDTO> newLabels, Integer loggedInUserId,
			XtremandResponse response) {
		List<FormLabel> existingLabels = form.getFormLabels();
		FormDTO formDto = frameFormFields(form.getFormTypeEnum(), newLabels, loggedInUserId, existingLabels);
		formDto.setId(form.getId());
		formService.updateCustomFormDetails(formDto, form, response);
	}

	private FormDTO frameFormFields(FormTypeEnum formTypeEnum, List<OpportunityFormFieldsDTO> newLabels,
			Integer loggedInUserId, List<FormLabel> existingLabels) {
		FormDTO formDto = setFormDetails(formTypeEnum, loggedInUserId);
		List<FormLabelDTO> formLabelDTOs = new ArrayList<>();
		for (OpportunityFormFieldsDTO field : newLabels) {
			FormLabelDTO labelDto = new FormLabelDTO();
			setIdForLabelDto(existingLabels, field, labelDto);
			setRegularLabelDto(field, labelDto);
			setFormFieldType(field, labelDto);
			setLabelTypeForLabelDto(field, labelDto);
			formLabelDTOs.add(labelDto);
		}
		formDto.setFormLabelDTOs(formLabelDTOs);
		formDto.setFormLabelDTORows(utilService.constructFormRows(formLabelDTOs));
		return formDto;
	}

	private FormDTO setFormDetails(FormTypeEnum formTypeEnum, Integer loggedInUserId) {
		FormDTO formDto = new FormDTO();
		String formName = XamplifyUtils.getFormNameByFormType(formTypeEnum);
		formDto.setName(formName);
		formDto.setDescription(formName);
		formDto.setFormType(formTypeEnum);
		formDto.setCreatedBy(loggedInUserId);
		return formDto;
	}

	private void setIdForLabelDto(List<FormLabel> existingLabels, OpportunityFormFieldsDTO field,
			FormLabelDTO labelDto) {
		if (field.getId() != null && field.getId() > 0) {
			List<FormLabel> formLabel = existingLabels.stream().filter(f -> f.getId().equals(field.getId()))
					.collect(Collectors.toList());
			if (!formLabel.isEmpty()) {
				Integer formLabelId = formLabel.get(0).getId();
				labelDto.setId(formLabelId);
			}
		}
	}

	private void setLabelTypeForLabelDto(OpportunityFormFieldsDTO field, FormLabelDTO labelDto) {
		setFieldTypeForCustomField(field);
		if (field.getType().equals(FieldLabelType.SELECT.getType())) {
			List<FormChoiceDTO> formChoiceDTOs = new ArrayList<>();
			List<PicklistValues> picklistValues = field.getOptions();
			setPickListValuesForSelectType(field, formChoiceDTOs, picklistValues);
			labelDto.setDropDownChoices(formChoiceDTOs);
			labelDto.setLabelType(FieldLabelType.SELECT.getType());
			labelDto.setOriginalCRMType(FieldLabelType.SELECT.getType());
		} else if (field.getType().equals(FieldLabelType.TEXTAREA.getType())) {
			labelDto.setLabelType(FieldLabelType.TEXTAREA.getType());
		} else if (field.getType().equals(FieldLabelType.EMAIL.getType())) {
			labelDto.setLabelType(FieldLabelType.EMAIL.getType());
		} else if (field.getType().equals(FieldLabelType.PHONE.getType())) {
			labelDto.setLabelType(FieldLabelType.PHONE.getType());
		} else if (field.getType().equals(FieldLabelType.URL.getType())) {
			labelDto.setLabelType(FieldLabelType.URL.getType());
		} else if (field.getType().equals(FieldLabelType.CHECKBOX.getType())) {
			setChoicesForCheckBoxLabelType(labelDto);
			labelDto.setLabelType(FieldLabelType.CHECKBOX.getType());
		} else if (field.getType().equals(FieldLabelType.NUMBER.getType())) {
			labelDto.setLabelType(FieldLabelType.NUMBER.getType());
		} else if (field.getType().equals(FieldLabelType.CURRENCY.getType())) {
			labelDto.setLabelType(FieldLabelType.CURRENCY.getType());
		} else if (field.getType().equals(FieldLabelType.DATE.getType())) {
			labelDto.setLabelType(FieldLabelType.DATE.getType());
		} else {
			labelDto.setLabelType(FieldLabelType.TEXT.getType());
		}
	}

	private void setChoicesForCheckBoxLabelType(FormLabelDTO labelDto) {
		List<FormChoiceDTO> formChoiceDTOs = new ArrayList<>();
		FormChoiceDTO fChoice = new FormChoiceDTO();
		fChoice.setLabelId("");
		fChoice.setHiddenLabelId("");
		fChoice.setName("");
		formChoiceDTOs.add(fChoice);
		labelDto.setCheckBoxChoices(formChoiceDTOs);
	}

	private void setPickListValuesForSelectType(OpportunityFormFieldsDTO field, List<FormChoiceDTO> formChoiceDTOs,
			List<PicklistValues> picklistValues) {
		if (picklistValues != null && !picklistValues.isEmpty()) {
			for (PicklistValues pickListValue : picklistValues) {
				FormChoiceDTO fChoice = new FormChoiceDTO();
				if (field.isDefaultField()) {
					fChoice.setLabelId(XamplifyUtils.replaceSpacesWithUnderScore(pickListValue.getLabel()));
				} else {
					fChoice.setLabelId(pickListValue.getLabel());
				}
				fChoice.setHiddenLabelId(XamplifyUtils.replaceSpacesWithUnderScore(pickListValue.getLabel()));
				fChoice.setName(pickListValue.getLabel());
				formChoiceDTOs.add(fChoice);
			}
		}
	}

	private void setFormFieldType(OpportunityFormFieldsDTO field, FormLabelDTO labelDto) {
		if (field.getFormFieldType() != null) {
			labelDto.setFormFieldType(field.getFormFieldType());
		} else if (isDefaultField(field) || isUnSelectedField(field.getLabel()) || isDealDefaultField(field)) {
			labelDto.setFormFieldType(FormFieldTypeEnum.DEFAULT);
		} else {
			labelDto.setFormFieldType(FormFieldTypeEnum.CUSTOM);
		}
	}

	private boolean isDealDefaultField(OpportunityFormFieldsDTO field) {
		return dealDefaultFields.contains(field.getLabel());
	}

	private boolean isDefaultField(OpportunityFormFieldsDTO field) {
		return leadDefaultFields.contains(field.getLabel());
	}

	private void setRegularLabelDto(OpportunityFormFieldsDTO field, FormLabelDTO labelDto) {
		if (!StringUtils.isBlank(field.getName())) {
			labelDto.setLabelId(field.getName());
			labelDto.setHiddenLabelId(field.getName());
			labelDto.setActive(field.isSelected());
			labelDto.setDisplayName(field.getDisplayName());
		} else {
			labelDto.setLabelId(XamplifyUtils.replaceSpacesWithUnderScore(field.getLabel()));
			labelDto.setHiddenLabelId(XamplifyUtils.replaceSpacesWithUnderScore(field.getLabel()));
			labelDto.setActive(true);
			labelDto.setDisplayName(field.getLabel());
		}
		labelDto.setLabelName(field.getLabel());
		labelDto.setPlaceHolder(field.getPlaceHolder());
		labelDto.setRequired(field.isRequired());
		labelDto.setLabelLength("500");
	}

	private boolean validateSyncFormInput(CustomFieldsDTO customFieldsDTO) {
		return customFieldsDTO != null && customFieldsDTO.getLoggedInUserId() != null
				&& customFieldsDTO.getLoggedInUserId() > 0 && customFieldsDTO.getObjectType() != null
				&& customFieldsDTO.getSelectedFields() != null && !customFieldsDTO.getSelectedFields().isEmpty();
	}

	@Override
	public XtremandResponse saveCustomField(CustomFieldsDTO customFieldsDTO) {
		XtremandResponse response = new XtremandResponse();
		if (validateSyncFormInput(customFieldsDTO)) {
			Integer loggedInCompanyId = userDao.getCompanyIdByUserId(customFieldsDTO.getLoggedInUserId());
			if (loggedInCompanyId != null) {
				List<OpportunityFormFieldsDTO> newLabels = customFieldsDTO.getSelectedFields();
				FormTypeEnum formType = XamplifyUtils.getFormTypeByObjectType(customFieldsDTO.getObjectType());
				Integer formId = formDao.getSfCustomFormIdByCompanyIdAndFormType(loggedInCompanyId, formType);
				if (formId == null) {
					checkDuplicateCustomFieldsAndSaveForm(customFieldsDTO, response, newLabels, formType);
				} else {
					checkDuplicateCustomFieldsAndSaveCustomField(response, newLabels, formId);
				}
			} else {
				responseUnAuthorized(response);
			}
		} else {
			responseInValidInput(response);
		}
		return response;
	}

	private void checkDuplicateCustomFieldsAndSaveCustomField(XtremandResponse response,
			List<OpportunityFormFieldsDTO> newLabels, Integer formId) {
		boolean isDuplicateNameExist;
		List<FormLabel> formLabels = customFieldsDao.getFormLabelsByFormId(formId);
		isDuplicateNameExist = formLabels.stream()
				.anyMatch(field -> field.getLabelName().equalsIgnoreCase(newLabels.get(0).getLabel().trim()));
		if (!isDuplicateNameExist) {
			createCustomField(response, newLabels, formId);
			responseSuccess(response);
		} else {
			responseDuplicateName(response);
		}
	}

	private void checkDuplicateCustomFieldsAndSaveForm(CustomFieldsDTO customFieldsDTO, XtremandResponse response,
			List<OpportunityFormFieldsDTO> newLabels, FormTypeEnum formType) {
		boolean isDuplicateNameExist;
		List<OpportunityFormFieldsDTO> defaultCustomFields = customFieldsDTO.getObjectType().equals(ObjectType.LEAD)
				? getDefaultCustomFieldsDto()
				: getDealDefaultCustomFieldsDto();
		isDuplicateNameExist = defaultCustomFields.stream()
				.anyMatch(field -> field.getLabel().equalsIgnoreCase(newLabels.get(0).getLabel().trim()));
		if (!isDuplicateNameExist) {
			newLabels.addAll(defaultCustomFields);
			saveForm(formType, newLabels, customFieldsDTO.getLoggedInUserId());
			responseSuccess(response);
		} else {
			responseDuplicateName(response);
		}
	}

	private void responseInValidInput(XtremandResponse response) {
		response.setMessage(INVALID_INPUT);
		response.setStatusCode(500);
	}

	private void responseUnAuthorized(XtremandResponse response) {
		response.setMessage(UNAUTHORIZED);
		response.setStatusCode(500);
	}

	private void responseSuccess(XtremandResponse response) {
		response.setMessage(SUCCESS);
		response.setStatusCode(200);
	}

	private void responseDuplicateName(XtremandResponse response) {
		response.setMessage(DUPLICATE_NAME);
		response.setStatusCode(500);
	}

	private void createCustomField(XtremandResponse response, List<OpportunityFormFieldsDTO> newLabels,
			Integer formId) {
		Form form = new Form();
		form.setId(formId);
		FormLabel formLabel = new FormLabel();
		List<FormLabelChoice> formLabelChoices = new ArrayList<>();
		FormLabelDTO formLabelDTO = setFormLabelDto(newLabels);
		setPickListValuesForFormLabelDTO(newLabels, formLabelDTO);
		if (formLabelDTO.getLabelType().equals(FieldLabelType.CHECKBOX.getType())) {
			setChoicesForCheckBoxLabelType(formLabelDTO);
			formLabelDTO.setLabelType(FieldLabelType.CHECKBOX.getType());
		}
		formService.saveFormLabel(form, 1, formLabelDTO, formLabel, formLabelChoices, response, 1);
	}

	private void setPickListValuesForFormLabelDTO(List<OpportunityFormFieldsDTO> newLabels, FormLabelDTO formLabelDTO) {
		if (formLabelDTO.getLabelType().equals(FieldLabelType.SELECT.getType())) {
			List<FormChoiceDTO> formChoiceDTOs = new ArrayList<>();
			List<PicklistValues> picklistValues = newLabels.get(0).getOptions();
			if (picklistValues != null && !picklistValues.isEmpty()) {
				for (PicklistValues pickListValue : picklistValues) {
					FormChoiceDTO fChoice = new FormChoiceDTO();
					if (newLabels.get(0).isDefaultField()) {
						fChoice.setLabelId(XamplifyUtils.replaceSpacesWithUnderScore(pickListValue.getLabel()));
					} else {
						fChoice.setLabelId(pickListValue.getLabel());
					}
					fChoice.setHiddenLabelId(XamplifyUtils.replaceSpacesWithUnderScore(pickListValue.getLabel()));
					fChoice.setName(pickListValue.getLabel());
					formChoiceDTOs.add(fChoice);
				}
			}
			formLabelDTO.setDropDownChoices(formChoiceDTOs);
			formLabelDTO.setLabelType(FieldLabelType.SELECT.getType());
			formLabelDTO.setOriginalCRMType(FieldLabelType.SELECT.getType());
		}
	}

	private FormLabelDTO setFormLabelDto(List<OpportunityFormFieldsDTO> newLabels) {
		FormLabelDTO formLabelDTO = new FormLabelDTO();
		formLabelDTO.setLabelName(newLabels.get(0).getLabel());
		formLabelDTO.setLabelId(XamplifyUtils.replaceSpacesWithUnderScore(newLabels.get(0).getLabel()));
		formLabelDTO.setHiddenLabelId(XamplifyUtils.replaceSpacesWithUnderScore(newLabels.get(0).getLabel()));
		formLabelDTO.setLabelType(newLabels.get(0).getType());
		formLabelDTO.setFormFieldType(FormFieldTypeEnum.CUSTOM);
		formLabelDTO.setDisplayName(newLabels.get(0).getLabel());
		formLabelDTO.setActive(true);
		return formLabelDTO;
	}

	@Override
	public XtremandResponse getCustomFields(Integer loggedInUserId, OpportunityType opportunityType) {
		XtremandResponse response = new XtremandResponse();
		List<OpportunityFormFieldsDTO> customFieldDtos = new ArrayList<>();
		FormTypeEnum formType = XamplifyUtils.getFormTypeByObjectType(
				(opportunityType != null && opportunityType.equals(OpportunityType.LEAD)) ? ObjectType.LEAD
						: ObjectType.DEAL);
		if (loggedInUserId != null) {
			Integer companyId = userService.getCompanyIdByUserId(loggedInUserId);
			if (companyId != null) {
				Integer formId = formDao.getSfCustomFormIdByCompanyIdAndFormType(companyId, formType);
				if (XamplifyUtils.isValidInteger(formId)) {
					customFieldDtos = getCustomFieldsDto(formId, opportunityType);
				} else {
					if (opportunityType != null && opportunityType.equals(OpportunityType.DEAL)) {
						customFieldDtos = getDealDefaultCustomFieldsDto();
					} else {
						customFieldDtos = getDefaultCustomFieldsDto();
					}

				}
			}
		}
		response.setData(customFieldDtos);
		response.setStatusCode(200);
		return response;
	}

	private List<OpportunityFormFieldsDTO> getCustomFieldsDto(Integer formId, OpportunityType opportunityType) {
		List<OpportunityFormFieldsDTO> opportunityFormFieldsDTOs = new ArrayList<>();
		List<FormLabel> formLabels = customFieldsDao.getFormLabelsByFormId(formId);
		if (formLabels != null && !formLabels.isEmpty()) {
			for (FormLabel label : formLabels) {
				OpportunityFormFieldsDTO formField = new OpportunityFormFieldsDTO();
				if (label != null) {
					setFormFields(label, formField, opportunityType);
				}
				opportunityFormFieldsDTOs.add(formField);
			}
		}
		return opportunityFormFieldsDTOs;
	}

	private void setFormFields(FormLabel label, OpportunityFormFieldsDTO formField, OpportunityType opportunityType) {
		setDisplayName(label, formField);
		setRegularFieldNames(label, formField);
		setFieldType(label, formField);
		setDefaultChoiceLabel(label, formField);
		setFormDefaultType(label, formField);
		setFormFieldsForLeadSelectedDefaultFields(label, formField, opportunityType);
		setOriginalTypeForFormFields(label, formField);
		iterateFormLabelChoicesAndSetOptions(label, formField);
	}

	private void setRegularFieldNames(FormLabel label, OpportunityFormFieldsDTO formField) {
		Integer labelId = label.getId();
		if (labelId != null) {
			formField.setId(labelId);
		}
		formField.setLabel(label.getLabelName());
		formField.setName(label.getLabelId());
		formField.setActive(label.isActive());
		formField.setSelected(label.isActive());
		formField.setRequired(label.isRequired());
		formField.setNonInteractive(label.isNonInteractive());
		formField.setPrivate(label.isPrivate());
		formField.setPlaceHolder(label.getPlaceHolder());
		formField.setOrder(label.getOrder());
		formField.setFormFieldType(label.getFormFieldType());
	}

	private void setFormDefaultType(FormLabel label, OpportunityFormFieldsDTO formField) {
		if (label.getFormDefaultFieldType() != null
				&& label.getFormDefaultFieldType().equals(FormDefaultFieldTypeEnum.CREATED_BY_NAME)) {
			formField.setFormDefaultFieldType(label.getFormDefaultFieldType());
		}
	}

	private boolean isUnSelectedField(String labelName) {
		return leadDefaultUnSelectFields.contains(labelName);
	}

	private void setDefaultChoiceLabel(FormLabel label, OpportunityFormFieldsDTO formField) {
		if (label.isNonInteractive() && label.getDefaultChoice() != null
				&& !StringUtils.isBlank(label.getDefaultChoice().getLabelChoiceName())) {
			formField.setDefaultChoiceLabel(label.getDefaultChoice().getLabelChoiceName());
		}
	}

	private void iterateFormLabelChoicesAndSetOptions(FormLabel label, OpportunityFormFieldsDTO formField) {
		List<PicklistValues> picklistValues = new ArrayList<>();
		List<FormLabelChoice> formLabelChoices = label.getFormLabelChoices();
		for (FormLabelChoice formLabelChoice : formLabelChoices) {
			PicklistValues picklistValue = new PicklistValues();
			picklistValue.setLabel(formLabelChoice.getLabelChoiceName());
			picklistValues.add(picklistValue);
		}
		formField.setOptions(picklistValues);
	}

	private void setOriginalTypeForFormFields(FormLabel label, OpportunityFormFieldsDTO formField) {
		if (label.getOriginalCRMType() != null) {
			if (label.getLabelType().getLabelType().equals(FieldLabelType.TEXT.getType())
					&& label.getOriginalCRMType().getLabelType().equals(FieldLabelType.TEXT.getType())) {
				formField.setOriginalCRMType(FieldLabelType.TEXT.getType());
			} else {
				formField.setOriginalCRMType(FieldLabelType.SELECT.getType());
			}
		} else if (label.getLabelType().getLabelType().equals(FieldLabelType.TEXT.getType())) {
			formField.setOriginalCRMType(FieldLabelType.TEXT.getType());
		}
	}

	private void setFormFieldsForLeadSelectedDefaultFields(FormLabel label, OpportunityFormFieldsDTO formField,
			OpportunityType opportunityType) {
		if ((leadDefaultFields.contains(label.getLabelName()) && opportunityType.equals(OpportunityType.LEAD))
				|| (dealDefaultFields.contains(label.getLabelName()) && opportunityType.equals(OpportunityType.DEAL))) {
			formField.setRequired(true);
			formField.setCanEditRequired(false);
			formField.setCanUnselect(false);
			formField.setSelected(true);
		}
	}

	private void setFieldType(FormLabel label, OpportunityFormFieldsDTO formField) {
		String labelType = label.getLabelType().getLabelType();

		switch (labelType) {
		case "text":
			formField.setType("Text");
			break;
		case "textarea":
			formField.setType("Text Area");
			break;
		case "select":
			formField.setType("Drop Down");
			break;
		case "email":
			formField.setType("Email");
			break;
		case "phone":
			formField.setType("Phone");
			break;
		case "url":
			formField.setType("URL");
			break;
		case "checkbox":
			formField.setType("Check Box");
			break;
		case "number":
			formField.setType("Number");
			break;
		case "currency":
			formField.setType("Currency");
			break;
		case "date":
			formField.setType("Date");
			break;
		default:
			formField.setType("Text");
			break;
		}
	}

	private void setFieldTypeForCustomField(OpportunityFormFieldsDTO field) {
		String labelType = field.getType();

		switch (labelType) {
		case "Text":
			field.setType(FieldLabelType.TEXT.getType());
			break;
		case "Text Area":
			field.setType(FieldLabelType.TEXTAREA.getType());
			break;
		case "Drop Down":
			field.setType(FieldLabelType.SELECT.getType());
			break;
		case "Email":
			field.setType(FieldLabelType.EMAIL.getType());
			break;
		case "Phone":
			field.setType(FieldLabelType.PHONE.getType());
			break;
		case "URL":
			field.setType(FieldLabelType.URL.getType());
			break;
		case "Check Box":
			field.setType(FieldLabelType.CHECKBOX.getType());
			break;
		case "Number":
			field.setType(FieldLabelType.NUMBER.getType());
			break;
		case "Currency":
			field.setType(FieldLabelType.CURRENCY.getType());
			break;
		case "Date":
			field.setType(FieldLabelType.DATE.getType());
			break;
		case "text":
			field.setType(FieldLabelType.TEXT.getType());
			break;
		case "textarea":
			field.setType(FieldLabelType.TEXTAREA.getType());
			break;
		case "select":
			field.setType(FieldLabelType.SELECT.getType());
			break;
		case "checkbox":
			field.setType(FieldLabelType.CHECKBOX.getType());
			break;
		case "number":
			field.setType(FieldLabelType.NUMBER.getType());
			break;
		case "currency":
			field.setType(FieldLabelType.CURRENCY.getType());
			break;
		case "date":
			field.setType(FieldLabelType.DATE.getType());
			break;
		default:
			field.setType(FieldLabelType.TEXT.getType());
			break;
		}
	}

	private void setDisplayName(FormLabel label, OpportunityFormFieldsDTO formField) {
		if (label.getDisplayName() != null) {
			formField.setDisplayName(label.getDisplayName());
		} else {
			formField.setDisplayName(label.getLabelName());
		}
	}

	private List<OpportunityFormFieldsDTO> getDefaultCustomFieldsDto() {
		return customFieldsDao.getDefaultLeadFilds();
	}

	private List<OpportunityFormFieldsDTO> getDealDefaultCustomFieldsDto() {
		return customFieldsDao.getDealDefaultCustomFieldsDto();
	}

	@Override
	public XtremandResponse deleteCustomField(Integer customFieldId, Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		if (XamplifyUtils.isValidInteger(customFieldId) && XamplifyUtils.isValidInteger(loggedInUserId)) {
			formDao.deleteSfCustomLabelById(customFieldId);
			responseSuccess(response);
		} else {
			responseInValidInput(response);
		}
		return response;
	}

	@Override
	public FormDTO getCustomForm(Integer companyId, Integer opportunityId, User loggedInUser, Long halopsaTicketTypeId,
			OpportunityType opportunityType) {
		FormDTO formDTO = new FormDTO();
		if (companyId != null && companyId > 0) {
			XtremandResponse response = frameCustomFormForUI(companyId, opportunityId, opportunityType);
			if (response.getStatusCode() == 200) {
				formDTO = (FormDTO) response.getData();
			}
		}
		return formDTO;
	}

	private XtremandResponse frameCustomFormForUI(Integer companyId, Integer opportunityId,
			OpportunityType opportunityType) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (companyId != null) {
			FormTypeEnum formType = XamplifyUtils.getFormTypeByObjectType(
					(opportunityType != null && opportunityType.equals(OpportunityType.LEAD)) ? ObjectType.LEAD
							: ObjectType.DEAL);
			Integer formId = formDao.getSfCustomFormIdByCompanyIdAndFormType(companyId, formType);
			Object opportunity = getOpportunityObject(opportunityId, opportunityType);
			if (formId != null) {
				List<FormLabel> sfLabelsList = customFieldsDao.getSelectedFormLabelsByFormId(formId);
				FormDTO formDTO = new FormDTO();
				List<FormLabelDTO> formLabelDtos = formDTO.getFormLabelDTOs();
				frameFormLabelDTO(sfLabelsList, formLabelDtos);
				if (opportunity != null) {
					setFormLabelValues(formLabelDtos, opportunity, opportunityType);
				}
				setFormDtoValues(response, formType, formDTO, formLabelDtos);
			} else {
				FormDTO formDTO = new FormDTO();
				List<OpportunityFormFieldsDTO> defaultFieldsDtos = (opportunityType != null
						&& opportunityType.equals(OpportunityType.LEAD)) ? getDefaultCustomFieldsDto()
								: getDealDefaultCustomFieldsDto();
				List<FormLabelDTO> formLabelDtos = frameFormLabelDTOs(defaultFieldsDtos);
				if (opportunity != null) {
					setFormLabelValues(formLabelDtos, opportunity, opportunityType);
				}
				setFormDtoValues(response, formType, formDTO, formLabelDtos);
			}
		}
		return response;
	}
	
	private Object getOpportunityObject(Integer opportunityId, OpportunityType opportunityType) {
		Object opportunity = null;
		if (opportunityId != null && opportunityId > 0) {
			if (opportunityType.equals(OpportunityType.DEAL)) {
				Deal deal = genericDAO.get(Deal.class, opportunityId);
				if (deal != null) {
					opportunity = deal;
				}
			} else {
				if (opportunityType.equals(OpportunityType.LEAD)) {
					Lead lead = genericDAO.get(Lead.class, opportunityId);
					if (lead != null) {
						opportunity = lead;
					}
				}
			}
		}
		return opportunity;
	}
	
	private void setFormLabelValues(List<FormLabelDTO> formLabelDtos, Object opportunity, OpportunityType opportunityType) {
		if (opportunity != null) {
			if (opportunityType == OpportunityType.DEAL) {
				setFormLabelValues(formLabelDtos, (Deal) opportunity);
			} else if (opportunityType == OpportunityType.LEAD) {
				setFormLabelValues(formLabelDtos, (Lead) opportunity);
			}
		}
	}
	
	private void setFormLabelValues(List<FormLabelDTO> formLabelDtos, Deal deal) {
		List<SfCustomFieldsData> sfCfData = deal.getSfCustomFieldsData();
		int index = 0;
		for (FormLabelDTO formLabelDTO : formLabelDtos) {
			frameFormLabelDtoForDefaultLeadvalues(deal, sfCfData, formLabelDTO);
			index = formLabelDtos.indexOf(formLabelDTO);
			formLabelDtos.set(index, formLabelDTO);
		}
		
	}

	
	private void setFormLabelValues(List<FormLabelDTO> formLabelDTOs, Lead lead) {
		List<SfCustomFieldsData> sfCfData = lead.getSfCustomFieldsData();
		int index = 0;
		for (FormLabelDTO formLabelDTO : formLabelDTOs) {
			frameFormLabelDtoForDefaultLeadvalues(lead, sfCfData, formLabelDTO);
			index = formLabelDTOs.indexOf(formLabelDTO);
			formLabelDTOs.set(index, formLabelDTO);
		}

	}
	
	private void frameFormLabelDtoForDefaultLeadvalues(Deal deal, List<SfCustomFieldsData> sfCfData,
			FormLabelDTO formLabelDTO) {
		String value = null;
		value = setFieldValues(deal, formLabelDTO, value);
		if (value != null) {
			formLabelDTO.setValue(value);
		} else {
			setSfCfDataValues(sfCfData, formLabelDTO);
		}
		
	}
	
	private void setSfCfDataValues(List<SfCustomFieldsData> customFields, FormLabelDTO formLabelDTO) {
		for (SfCustomFieldsData customField : customFields) {
			String customFieldValue = customField.getValue();
			if (!StringUtils.isBlank(customFieldValue)
					&& customField.getFormLabel().getLabelId().equals(formLabelDTO.getLabelId())) {
				formLabelDTO.setValue(customFieldValue);
			}
		}
	}

	

	private String setFieldValues(Deal deal, FormLabelDTO formLabelDTO, String value) {
		String labelId = formLabelDTO.getLabelId();
		LeadFieldLabel fieldLabel = LeadFieldLabel.fromString(labelId);
		String fieldValue = null;
		if (fieldLabel != null) {
			switch (fieldLabel) {
			case DEAL_NAME:
				fieldValue = deal.getTitle();
				break;
			case AMOUNT:
				fieldValue = deal.getAmount().toString();
				break;
			case CLOSE_DATE:
				fieldValue = deal.getCloseDate().toString();
				break;
			case NAME:
				fieldValue = deal.getTitle();
				break;
			}
		}
		if (!StringUtils.isBlank(fieldValue)) {
			value = fieldValue;
		}
		return value;
	}
	
	private void frameFormLabelDtoForDefaultLeadvalues(Lead lead, List<SfCustomFieldsData> sfCfData,
			FormLabelDTO formLabelDTO) {
		String value = null;
		value = setFieldValues(lead, formLabelDTO, value);
		if (value != null) {
			formLabelDTO.setValue(value);
		} else {
			setSfCfDataValues(sfCfData, formLabelDTO);
		}
	}
	
	private String setFieldValues(Lead lead, FormLabelDTO formLabelDTO, String value) {
		String labelId = formLabelDTO.getLabelId();
		LeadFieldLabel fieldLabel = LeadFieldLabel.fromString(labelId);
		String fieldValue = null;
		if (fieldLabel != null) {
			switch (fieldLabel) {
			case LAST_NAME:
				fieldValue = lead.getLastName();
				break;
			case FIRST_NAME:
				fieldValue = lead.getFirstName();
				break;
			case TITLE:
				fieldValue = lead.getTitle();
				break;
			case COMPANY:
				fieldValue = lead.getCompany();
				break;
			case STREET:
				fieldValue = lead.getStreet();
				break;
			case CITY:
				fieldValue = lead.getCity();
				break;
			case STATE:
				fieldValue = lead.getState();
				break;
			case POSTAL_CODE:
				fieldValue = lead.getPostalCode();
				break;
			case COUNTRY:
				fieldValue = lead.getCountry();
				break;
			case EMAIL:
				fieldValue = lead.getEmail();
				break;
			case WEBSITE:
				fieldValue = lead.getWebsite();
				break;
			case PHONE_NUMBER:
				fieldValue = lead.getPhone();
				if (!XamplifyUtils.isValidString(fieldValue)) {
					fieldValue = null;
				}
				break;
			case INDUSTRY:
				fieldValue = lead.getIndustry();
				if (!XamplifyUtils.isValidString(fieldValue)) {
					fieldValue = null;
				}
				break;
			case REGION:
				fieldValue = lead.getRegion();
				if (!XamplifyUtils.isValidString(fieldValue)) {
					fieldValue = null;
				}
				break;
			}
		}
		if (!StringUtils.isBlank(fieldValue)) {
			value = fieldValue;
		}
		return value;
	}

	private List<FormLabelDTO> frameFormLabelDTO(List<FormLabel> formLabels, List<FormLabelDTO> formLabelDtos) {
		for (FormLabel formLabel : formLabels) {
			FormLabelDTO formLabelDTO = new FormLabelDTO();
			formLabelDTO.setId(formLabel.getId());
			formLabelDTO.setLabelId(formLabel.getLabelId());
			formLabelDTO.setLabelName(formLabel.getLabelName());
			formLabelDTO.setHiddenLabelId(formLabel.getHiddenLabelId());
			formLabelDTO.setOrder(formLabel.getOrder());
			formLabelDTO.setPlaceHolder(formLabel.getPlaceHolder());
			formLabelDTO.setRequired(formLabel.isRequired());
			formLabelDTO.setDisplayName(formLabel.getDisplayName());
			String labelType = formLabel.getLabelType().getLabelType();
			formLabelDTO.setLabelType(labelType);
			formLabelDTO.setLabelLength(formLabel.getLabelLength());
			List<FormLabelChoice> formLabelChoices = formLabel.getFormLabelChoices();
			List<FormChoiceDTO> dropDownChoiceDtos = new ArrayList<>();
			List<FormChoiceDTO> checkBoxChoiceDtos = new ArrayList<>();
			for (FormLabelChoice formLabelChoice : formLabelChoices) {
				FormChoiceDTO formChoiceDto = addChoiceData(formLabelChoice);
				if (labelType.equals(FieldLabelType.SELECT.getType())) {
					dropDownChoiceDtos.add(formChoiceDto);
				} else if (labelType.equals(FieldLabelType.CHECKBOX.getType())) {
					checkBoxChoiceDtos.add(formChoiceDto);
				}
			}
			formLabelDTO.setDropDownChoices(dropDownChoiceDtos);
			formLabelDTO.setCheckBoxChoices(checkBoxChoiceDtos);
			formLabelDtos.add(formLabelDTO);
		}
		return formLabelDtos;
	}

	private FormChoiceDTO addChoiceData(FormLabelChoice choice) {
		FormChoiceDTO formChoiceDto = new FormChoiceDTO();
		formChoiceDto.setId(choice.getId());
		formChoiceDto.setLabelId(choice.getLabelChoiceId());
		formChoiceDto.setHiddenLabelId(choice.getLabelChoiceHiddenId());
		formChoiceDto.setName(choice.getLabelChoiceName());
		formChoiceDto.setItemName(choice.getLabelChoiceName());
		return formChoiceDto;
	}

	private void setFormDtoValues(XtremandResponse response, FormTypeEnum formType, FormDTO formDTO,
			List<FormLabelDTO> formLabelDtos) {
		formDTO.setName("Xamplify Custom Form Fields");
		formDTO.setFormLabelDTOs(formLabelDtos);
		formDTO.setFormLabelDTORows(utilService.constructFormRows(formLabelDtos));
		formDTO.setFormType(formType);
		response.setStatusCode(200);
		response.setData(formDTO);
	}

	private List<FormLabelDTO> frameFormLabelDTOs(List<OpportunityFormFieldsDTO> defaultFieldsDtos) {
		List<FormLabelDTO> formLabelDtos = new ArrayList<>();
		for (OpportunityFormFieldsDTO defaultCustomField : defaultFieldsDtos) {
			FormLabelDTO formLabelDTO = new FormLabelDTO();
			formLabelDTO.setId(defaultCustomField.getId());
			formLabelDTO.setLabelId(defaultCustomField.getName());
			formLabelDTO.setLabelName(defaultCustomField.getLabel());
			formLabelDTO.setPlaceHolder(defaultCustomField.getPlaceHolder());
			formLabelDTO.setRequired(defaultCustomField.isRequired());
			formLabelDTO.setDisplayName(defaultCustomField.getDisplayName());
			setFieldTypeForCustomField(defaultCustomField);
			formLabelDTO.setLabelType(defaultCustomField.getType());
			formLabelDtos.add(formLabelDTO);
		}
		return formLabelDtos;
	}

	@Override
	public XtremandResponse getLeadCountForCustomField(Integer customFieldId) {
		XtremandResponse response = new XtremandResponse();
		if (XamplifyUtils.isValidInteger(customFieldId)) {
			response.setData(customFieldsDao.getLeadCountForCustomField(customFieldId));
		}
		return response;
	}

}
