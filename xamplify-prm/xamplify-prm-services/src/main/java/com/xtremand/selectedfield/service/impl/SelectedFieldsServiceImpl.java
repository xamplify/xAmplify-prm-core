package com.xtremand.selectedfield.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import com.xtremand.customfields.service.CustomFieldsService;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.form.bom.Form;
import com.xtremand.form.bom.FormTypeEnum;
import com.xtremand.form.bom.OpportunityTypeEnum;
import com.xtremand.form.bom.SelectedFields;
import com.xtremand.form.dao.FormDao;
import com.xtremand.form.dto.SelectedFieldsDTO;
import com.xtremand.form.dto.SelectedFieldsResponseDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.integration.bom.Integration;
import com.xtremand.integration.dao.IntegrationDao;
import com.xtremand.integration.service.IntegrationWrapperService;
import com.xtremand.lead.bom.OpportunityType;
import com.xtremand.salesforce.dto.OpportunityFormFieldsDTO;
import com.xtremand.selectedfield.service.SelectedFieldsService;
import com.xtremand.selectedfields.dao.SelectedFieldsDAO;
import com.xtremand.team.dao.TeamDao;
import com.xtremand.user.bom.User;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.dto.XamplifyUtilValidator;

@Service
@Transactional
public class SelectedFieldsServiceImpl implements SelectedFieldsService {

	private static final String SUCCESS = "Success";
	private static final String FAILED = "Failed";
	private static final String CUSTOM = "custom";

	public static final String XAMPLIFY = "xamplify";

	@Autowired
	private XamplifyUtilValidator xamplifyUtilValidator;

	@Autowired
	private IntegrationDao integrationDao;

	@Autowired
	private UserDAO userDao;

	@Autowired
	private FormDao formDao;

	@Autowired
	private SelectedFieldsDAO selectedFieldsDao;

	@Autowired
	private GenericDAO genericDao;

	@Autowired
	private UtilDao utilDao;

	@Autowired
	private IntegrationWrapperService integrationWrapperService;

	@Autowired
	private CustomFieldsService customFieldsService;

	@Autowired
	private TeamDao teamDao;

	/*** XNFR-840 **/
	@Override
	public XtremandResponse saveOrUpdateSelectedFields(SelectedFieldsResponseDTO selectedFieldsResponseDto,
			BindingResult result) {
		XtremandResponse response = createDefaultResponse();

		if (result.hasErrors()) {
			xamplifyUtilValidator.addErrorResponse(result, response);
			return response;
		}

		if (isValidRequest(selectedFieldsResponseDto)) {
			processSelectedFields(selectedFieldsResponseDto);
			response.setMessage(SUCCESS);
			response.setStatusCode(200);
		}

		return response;
	}

	private XtremandResponse createDefaultResponse() {
		XtremandResponse response = new XtremandResponse();
		response.setMessage(FAILED);
		response.setStatusCode(400);
		return response;
	}

	private boolean isValidRequest(SelectedFieldsResponseDTO dto) {
		return dto != null && !dto.getPropertiesList().isEmpty()
				&& XamplifyUtils.isValidString(dto.getCompanyProfileName())
				&& (dto.isMyPreferances() || dto.isDefaultField());
	}

	private void processSelectedFields(SelectedFieldsResponseDTO dto) {
		Integer loggedInUserId = dto.getLoggedInUserId();
		Integer companyId = userDao.getCompanyIdByProfileName(dto.getCompanyProfileName());
		Integration integration = integrationDao.getActiveCRMIntegration(companyId);
		if (!dto.isIntegation()) {
			integration = null;
		}
		String customName = dto.isIntegation() ? "" : CUSTOM;
		String activeCRM = resolveActiveCRM(customName, companyId);
		FormTypeEnum formType = getFormTypeByActiveCRM(activeCRM, dto.getOpportunityType());
		Integer formId = formDao.getSfCustomFormIdByCompanyIdAndFormType(companyId, formType);
		Form form = formDao.getById(formId);
		if (dto.isDefaultField()) {
			loggedInUserId = utilDao.findAdminIdByCompanyId(companyId);
		}
		User user = userDao.getUser(loggedInUserId);
		deleteUnselectedFields(loggedInUserId, formId, dto.isDefaultField(), dto.getOpportunityType());

		prepareSelectedFields(dto.getPropertiesList(), form, user, integration, dto.isMyPreferances(),
				dto.isDefaultField(), dto.getOpportunityType());
	}

	private List<SelectedFields> prepareSelectedFields(List<SelectedFieldsDTO> selectedFieldsDtos, Form form, User user,
			Integration integration, boolean isMyPreferences, boolean isDefault, String opportunityType) {
		List<SelectedFields> records = new ArrayList<>();
		int index = 1;
		for (SelectedFieldsDTO dto : selectedFieldsDtos) {

			SelectedFields field = selectedFieldsDao.getSelectedFieldByLableId(dto.getLabelId(), user.getUserId(),
					form.getId(), opportunityType);
			if (field == null) {
				field = new SelectedFields();
			}
			if (XamplifyUtils.isValidInteger(field.getId())) {
				field.setId(field.getId());
				field.setUpdatedBy(user);
				field.setCreatedBy(user);
				field.setUpdatedTime(new Date());
				if (isDefault) {
					field.setDefaultColumn(isDefault);
				}
			} else {
				field.setCreatedBy(user);
				field.setCreatedTime(new Date());
				field.setDefaultColumn(isDefault);
			}
			field.setLabelName(dto.getLabelName());
			field.setDisplayName(dto.getDisplayName());
			field.setLabelId(dto.getLabelId());
			field.setSelectedColumn(dto.isSelectedColumn());
			field.setForm(form);
			field.setOpportunityType(OpportunityTypeEnum.valueOf(opportunityType));
			field.setIntegration(integration);
			field.setMyPreferencesEnabled(isMyPreferences);
			field.setColumnOrder(index++);
			genericDao.save(field);
			records.add(field);
		}

		return records;
	}

	private void deleteUnselectedFields(Integer userId, Integer formId, boolean isDefaultField,
			String opportunityType) {
		List<SelectedFieldsDTO> selectedFields = selectedFieldsDao
				.getAllSelectedFieldsByLoggedInuserIdAndOpportunity(userId, formId, opportunityType);
		List<Integer> unselectedIds = new ArrayList<>();
		if (!isDefaultField) {
			unselectedIds.addAll(selectedFields.stream().filter(obj -> !obj.isDefaultColumn())
					.map(SelectedFieldsDTO::getId).collect(Collectors.toList()));
		} else {
			unselectedIds.addAll(selectedFields.stream().filter(SelectedFieldsDTO::isDefaultColumn)
					.map(SelectedFieldsDTO::getId).collect(Collectors.toList()));
		}
		if (XamplifyUtils.isNotEmptyList(unselectedIds)) {
			selectedFieldsDao.deleteUnSelectedFieldsByIds(unselectedIds);
		}
	}

	@Override
	public XtremandResponse isMyPreferances(Integer userId, String companyProfileName, String opportunityType) {
		XtremandResponse response = new XtremandResponse();
		response.setMessage(FAILED);
		response.setStatusCode(400);
		if (!XamplifyUtils.isValidInteger(userId)) {
			return response;
		}
		Integer companyId = userDao.getCompanyIdByProfileName(companyProfileName);
		String activeCRM = integrationDao.getActiveIntegrationTypeByCompanyId(companyId);
		if (!XamplifyUtils.isValidString(activeCRM)) {
			activeCRM = XAMPLIFY;
		}
		FormTypeEnum formType = getFormTypeByActiveCRM(activeCRM, opportunityType);
		Integer formId = formDao.getSfCustomFormIdByCompanyIdAndFormType(companyId, formType);
		boolean isMyPreferance = false; // XBI-4921
		if (XamplifyUtils.isValidInteger(formId)) {
			isMyPreferance = selectedFieldsDao.isMyPreferances(userId, formId, opportunityType);
		}
		response.setData(isMyPreferance);
		response.setMessage(SUCCESS);
		response.setStatusCode(200);
		Map<String, Object> map = new HashMap<>();
		map.put("formId", formId);
		response.setMap(map);
		return response;
	}

	/**** XNFR-840 ***/
	@Override
	public XtremandResponse getExportExcelColumns(String companyProfileName, String userType, Integer userId,
			String customFormName, String opportunityType, boolean myprofile) {
		XtremandResponse response = new XtremandResponse();
		response.setMessage(FAILED);
		response.setStatusCode(400);
		Integer companyId = getCompanyId(companyProfileName);
		if (companyId == null) {
			return response;
		}
		List<SelectedFieldsDTO> shownAllFields = new ArrayList<>();
		List<SelectedFieldsDTO> formLableFields = new ArrayList<>();
		List<SelectedFieldsDTO> selectedFields = new ArrayList<>();
		String activeCRM = resolveActiveCRM(customFormName, companyId);
		FormTypeEnum formType = getFormTypeByActiveCRM(activeCRM, opportunityType);
		Integer formId = formDao.getSfCustomFormIdByCompanyIdAndFormType(companyId, formType);
		List<SelectedFieldsDTO> commonFields = addDefaultFormFields(userType, activeCRM, opportunityType, companyId);
		if (XamplifyUtils.isValidInteger(formId) || activeCRM.equals(XAMPLIFY)) {
			Integer vendorUserId = utilDao.findAdminIdByCompanyId(companyId);
			response = fetchFormFields(activeCRM, vendorUserId, opportunityType);
			if (response.getStatusCode() != 200) {
				return response;
			}
			@SuppressWarnings("unchecked")
			List<OpportunityFormFieldsDTO> formFields = (List<OpportunityFormFieldsDTO>) response.getData();
			formLableFields = formFields.stream().filter(OpportunityFormFieldsDTO::isSelected)
					.map(this::mapToSelectedFieldsDTO).collect(Collectors.toList());
			if (userType.equals("p")) {
				formLableFields.removeIf(
						dto -> dto.getFormDefaultFieldType() != null && (dto.getFormDefaultFieldType().equals("CRM_ID")
								|| dto.getFormDefaultFieldType().equals("DEAL_ID")
								|| dto.getFormDefaultFieldType().equals("LEAD_ID")));
				formLableFields.removeIf(SelectedFieldsDTO::isPrivateField);
			}
			boolean isExportFields = false;// XBI-4921
			if (XamplifyUtils.isValidInteger(formId)) {
				isExportFields = selectedFieldsDao.isFieldsByFormId(formId, opportunityType);
			}
			if (isExportFields) {
				selectedFields = selectedFieldsDao.getSelectedFieldsByFormIdAndOpportunityType(formId, opportunityType);
			}
			List<SelectedFieldsDTO> finalList = getShowAllFieldsLsit(formLableFields, selectedFields, commonFields,
					userId, vendorUserId, myprofile, userType);
			shownAllFields.addAll(finalList);
		}
		List<String> defaultFields = selectedFields.stream().filter(SelectedFieldsDTO::isDefaultColumn)
				.map(SelectedFieldsDTO::getLabelId).collect(Collectors.toList());
		List<String> userSelectedFields = selectedFields.stream()
				.filter(f -> f.isMyPreferencesEnabled() && !f.isDefaultColumn() && userId.equals(f.getLoggedInUserId()))
				.map(SelectedFieldsDTO::getLabelId).collect(Collectors.toList());
		boolean isChecked = !XamplifyUtils.isNotEmptyList(defaultFields);
		boolean isUserChecked = !XamplifyUtils.isNotEmptyList(userSelectedFields);
		if ((isUserChecked || myprofile) && isChecked) {
			markAllSelected(shownAllFields);
			sortByPriority(shownAllFields);
		} else {
			markSelectedFieldsForUser(shownAllFields, userId, customFormName, myprofile, companyId);
		}
		response.setData(shownAllFields);
		response.setMessage(SUCCESS);
		XamplifyUtils.addSuccessStatus(response);
		return response;
	}

	private List<SelectedFieldsDTO> getShowAllFieldsLsit(List<SelectedFieldsDTO> formLabelFields,
			List<SelectedFieldsDTO> selectedFields, List<SelectedFieldsDTO> commonFields, Integer userId,
			Integer vendorUserId, boolean isMyProfile, String userType) {
		List<SelectedFieldsDTO> finalList = new ArrayList<>();
		boolean isTeamMember = teamDao.isTeamMember(userId);
		Integer targetUserId = (isMyProfile && isTeamMember) ? vendorUserId : userId;
		Set<String> selectedIds = getSelectedIds(selectedFields, targetUserId, vendorUserId);
		Set<String> defaultIds = getDefaultIds(selectedFields, vendorUserId);
		Map<String, String> labelIdToDisplayName = formLabelFields.stream()
				.collect(Collectors.toMap(SelectedFieldsDTO::getLabelId, SelectedFieldsDTO::getDisplayName));
		Set<String> lookupLabelIds = Stream.concat(formLabelFields.stream(), commonFields.stream())
				.map(SelectedFieldsDTO::getLabelId).collect(Collectors.toSet());
		List<SelectedFieldsDTO> matchedSelected = selectedFields.stream()
				.filter(dto -> lookupLabelIds.contains(dto.getLabelId()))
				.filter(dto -> (selectedIds.contains(dto.getLabelId()) || defaultIds.contains(dto.getLabelId()))
						&& (targetUserId.equals(dto.getLoggedInUserId())
								|| vendorUserId.equals(dto.getLoggedInUserId())))
				.map(dto -> updateDto(dto, labelIdToDisplayName, userType, isTeamMember, isMyProfile, selectedIds,
						targetUserId))
				.collect(Collectors.toList());
		List<SelectedFieldsDTO> sortedDefaults = matchedSelected
				.stream().filter(SelectedFieldsDTO::isDefaultColumn).sorted(Comparator
						.comparing(SelectedFieldsDTO::getColumnOrder, Comparator.nullsLast(Integer::compareTo)))
				.collect(Collectors.toList());
		List<SelectedFieldsDTO> sortedUserFields = matchedSelected.stream()
				.filter(dto -> !defaultIds.contains(dto.getLabelId())).sorted(Comparator
						.comparing(SelectedFieldsDTO::getColumnOrder, Comparator.nullsLast(Integer::compareTo)))
				.collect(Collectors.toList());
		finalList.addAll(sortedDefaults);
		finalList.addAll(sortedUserFields);
		List<SelectedFieldsDTO> unmatchedForm = formLabelFields.stream()
				.filter(dto -> !selectedIds.contains(dto.getLabelId())).collect(Collectors.toList());
		finalList.addAll(unmatchedForm);
		List<SelectedFieldsDTO> unmatchedCommon = commonFields.stream()
				.filter(dto -> !selectedIds.contains(dto.getLabelId())).collect(Collectors.toList());
		finalList.addAll(unmatchedCommon);
		return finalList;
	}

	private Set<String> getSelectedIds(List<SelectedFieldsDTO> selectedFields, Integer targetUserId,
			Integer vendorUserId) {
		Set<String> selectedIdsByUser = selectedFields.stream().filter(f -> targetUserId.equals(f.getLoggedInUserId()))
				.map(SelectedFieldsDTO::getLabelId).collect(Collectors.toSet());
		boolean selectedIdsByUserExist = XamplifyUtils.isNotEmptySet(selectedIdsByUser);
		if (!selectedIdsByUserExist) {
			selectedIdsByUser = selectedFields.stream().filter(f -> vendorUserId.equals(f.getLoggedInUserId()))
					.map(SelectedFieldsDTO::getLabelId).collect(Collectors.toSet());
		}
		return selectedIdsByUser;
	}

	private Set<String> getDefaultIds(List<SelectedFieldsDTO> selectedFields, Integer vendorUserId) {
		return selectedFields.stream().filter(SelectedFieldsDTO::isDefaultColumn)
				.filter(f -> vendorUserId.equals(f.getLoggedInUserId())).map(SelectedFieldsDTO::getLabelId)
				.collect(Collectors.toSet());
	}

	private SelectedFieldsDTO updateDto(SelectedFieldsDTO dto, Map<String, String> labelIdToDisplayName,
			String userType, boolean isTeamMember, boolean isMyProfile, Set<String> selectedIds, Integer targetUserId) {

		String displayName = labelIdToDisplayName.get(dto.getLabelId());
		if (displayName != null) {
			dto.setDisplayName(displayName);
		}

		boolean shouldSetId = "p".equals(userType) || (isTeamMember && "v".equals(userType) && !isMyProfile)
				&& selectedIds.contains(dto.getLabelId()) && targetUserId.equals(dto.getLoggedInUserId());

		if (!shouldSetId) {
			dto.setId(null);
		}

		return dto;
	}

	private void sortByPriority(List<SelectedFieldsDTO> fields) {
		List<String> priorityLabels = new ArrayList<>();
		priorityLabels.addAll(Arrays.asList("LastName", "Last_Name", "Company", "Email", "Name", "dealname", "name",
				"title", "symptom", "Account_Name", "Deal_Name", "CloseDate", "closedate", "expectedCloseDate",
				"expected_close_date", "FOppTargetDate", "Closing_Date", "Close_Date", "Amount", "amount", "value",
				"FOppValue"));
		fields.sort(Comparator.comparingInt(field -> {
			int index = priorityLabels.indexOf(field.getLabelId());
			return index == -1 ? Integer.MAX_VALUE : index;
		}));
	}

	private SelectedFieldsDTO mapToSelectedFieldsDTO(OpportunityFormFieldsDTO f) {
		SelectedFieldsDTO dto = new SelectedFieldsDTO();
		dto.setId(null);
		dto.setDisplayName(f.getDisplayName());
		dto.setLabelName(f.getLabel());
		dto.setLabelId(f.getName());
		dto.setRequired(f.isRequired());
		dto.setDefaultColumn(f.isDefaultField());
		dto.setPrivateField(f.isPrivate());
		dto.setFormDefaultFieldType(f.getFormDefaultFieldType() != null ? f.getFormDefaultFieldType().name() : null);
		return dto;
	}

	private XtremandResponse fetchFormFields(String activeCRM, Integer vendorUserId, String opportunityType) {
		return customFieldsService.getCustomFields(vendorUserId, OpportunityType.valueOf(opportunityType));

	}

	private Integer getCompanyId(String companyProfileName) {
		if (XamplifyUtils.isValidString(companyProfileName)) {
			return userDao.getCompanyIdByProfileName(companyProfileName);
		}
		return null;
	}

	private String resolveActiveCRM(String customFormName, Integer companyId) {
		String activeCRM = integrationDao.getActiveIntegrationTypeByCompanyId(companyId);
		if (customFormName.equalsIgnoreCase(CUSTOM) || !XamplifyUtils.isValidString(activeCRM)) {
			return XAMPLIFY;
		}
		return activeCRM;
	}

	private void markAllSelected(List<SelectedFieldsDTO> fields) {
		fields.forEach(field -> field.setSelectedColumn(true));
	}

	private void markSelectedFieldsForUser(List<SelectedFieldsDTO> fields, Integer userId, String customFormName,
			boolean myprofile, Integer companyId) {
		boolean isMyprofile = customFormName.equals(CUSTOM) || myprofile;
		Integer vendorUserId = utilDao.findAdminIdByCompanyId(companyId);
		fields.forEach(field -> {
			boolean isOwnerOrDefault = isMyprofile
					? vendorUserId.equals(field.getLoggedInUserId()) && field.isDefaultColumn()
					: userId.equals(field.getLoggedInUserId()) || field.isDefaultColumn();
			field.setSelectedColumn(isOwnerOrDefault);
		});
	}

	private List<SelectedFieldsDTO> addDefaultFormFields(String userType, String activeCRM, String opportunityType,
			Integer companyId) {
		List<SelectedFieldsDTO> fieldHeaders = new ArrayList<>();
		boolean isPrmCompany = utilDao.isPrmByVendorCompanyId(companyId);
		if ("v".equals(userType)) {
			handleVendorFields(fieldHeaders, activeCRM, opportunityType, isPrmCompany);
		} else {
			handleNonVendorFields(fieldHeaders, isPrmCompany, opportunityType);
		}
		fieldHeaders.add(createField("Added By - Company",
				opportunityType.equals("DEAL") ? "getCreatedByCompanyName" : "added_by_company_c_xamp"));
		fieldHeaders.add(createField("Added By - Name",
				opportunityType.equals("DEAL") ? "getCreatedByName" : "added_by_name_c_xamp"));
		fieldHeaders.add(createField("Added By - Email ID",
				opportunityType.equals("DEAL") ? "getCreatedByEmail" : "added_by_email_id_c_xamp"));
		fieldHeaders.add(createField("Added On (PST)",
				opportunityType.equals("DEAL") ? "getCreatedDateString" : "added_on_date_string_c_xamp"));
		fieldHeaders.add(createField("Status",
				opportunityType.equals("DEAL") ? "getCurrentStageName" : "current_stage_name_c_xamp"));

		return fieldHeaders;
	}

	private void handleVendorFields(List<SelectedFieldsDTO> fieldHeaders, String activeCRM, String opportunityType,
			boolean isPrmCompany) {
		if (XamplifyUtils.isValidString(activeCRM) && !activeCRM.equals(XAMPLIFY)) {
			fieldHeaders.add(createField("CRM Id", opportunityType.equals("DEAL") ? "getCrmId" : "crm_id_c_xamp"));
		}
		if ("LEAD".equals(opportunityType)) {
			fieldHeaders.add(createField("Lead Id", "lead_id_c_xamp"));
			fieldHeaders.add(createField("Account Owner", "Account_Owner"));
			fieldHeaders.add(createField("Partner Type", "Partner_Type"));
			fieldHeaders.add(createField("Account SubType", "Account_Sub_Type"));
		} else {
			fieldHeaders.add(createField("Deal Id", "getReferenceId"));
			fieldHeaders.add(createField("Partner Type", "getPartnerType"));
			fieldHeaders.add(createField("Account SubType", "getAccountSubType"));
		}
	}

	private void handleNonVendorFields(List<SelectedFieldsDTO> fieldHeaders, boolean isPrmCompany,
			String opportunityType) {
		fieldHeaders.add(createField("Added For - Company",
				opportunityType.equals("DEAL") ? "getCreatedForCompanyName" : "added_for_company_c_xamp"));
	}

	private SelectedFieldsDTO createField(String label, String id) {
		SelectedFieldsDTO field = new SelectedFieldsDTO();
		field.setLabelName(label);
		field.setLabelId(id);
		field.setDisplayName(label);
		return field;
	}

	private FormTypeEnum getFormTypeByActiveCRM(String activeCRM, String opportunityType) {
		if (opportunityType.equals("LEAD")) {
			return FormTypeEnum.XAMPLIFY_LEAD_CUSTOM_FORM;
		} else {
			return getActiveCRMFormTypeByActiveIntegration(activeCRM);
		}
	}

	public FormTypeEnum getActiveCRMFormTypeByActiveIntegration(String activeCRMIntegration) {
		FormTypeEnum formType = null;
		if (activeCRMIntegration != null) {
			switch (activeCRMIntegration) {

			case XAMPLIFY:
				formType = FormTypeEnum.XAMPLIFY_DEAL_CUSTOM_FORM;
				break;
			default:
				formType = FormTypeEnum.XAMPLIFY_DEAL_CUSTOM_FORM;
				break;
			}
		}
		return formType;
	}

	/**** XNFR-840 ***/

}
