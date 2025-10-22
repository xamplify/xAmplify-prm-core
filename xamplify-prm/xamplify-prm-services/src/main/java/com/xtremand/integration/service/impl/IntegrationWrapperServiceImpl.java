package com.xtremand.integration.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.Criteria;
import com.xtremand.common.bom.Criteria.OPERATION_NAME;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.customfields.service.CustomFieldsService;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.deal.bom.Deal;
import com.xtremand.form.bom.FormTypeEnum;
import com.xtremand.form.dao.FormDao;
import com.xtremand.form.dto.FormChoiceDTO;
import com.xtremand.form.dto.FormDTO;
import com.xtremand.form.dto.FormLabelDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.integration.bom.CustomCity;
import com.xtremand.integration.bom.CustomCountry;
import com.xtremand.integration.bom.CustomRegion;
import com.xtremand.integration.bom.CustomState;
import com.xtremand.integration.bom.Integration;
import com.xtremand.integration.bom.Integration.IntegrationType;
import com.xtremand.integration.dao.IntegrationDao;
import com.xtremand.integration.dto.CustomChoicesDTO;
import com.xtremand.integration.dto.CustomFormRequestDto;
import com.xtremand.integration.dto.IntegrationDTO;
import com.xtremand.integration.dto.IntegrationSettingsDTO;
import com.xtremand.integration.service.IntegrationWrapperService;
import com.xtremand.lead.bom.Lead;
import com.xtremand.lead.bom.OpportunityType;
import com.xtremand.lead.bom.Pipeline;
import com.xtremand.lead.bom.PipelineStage;
import com.xtremand.lead.bom.PipelineType;
import com.xtremand.lead.dao.LeadDAO;
import com.xtremand.lead.dto.PipelineDto;
import com.xtremand.lead.dto.PipelineRequestDTO;
import com.xtremand.mail.service.AsyncComponent;
import com.xtremand.pipeline.dao.PipelineDAO;
import com.xtremand.pipeline.service.PipelineService;
import com.xtremand.salesforce.dto.OpportunityFormFieldsDTO;
import com.xtremand.salesforce.dto.PicklistValues;
import com.xtremand.user.bom.User;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.user.service.UserService;
import com.xtremand.userlist.dao.UserListDAO;
import com.xtremand.userlist.service.UserListService;
import com.xtremand.util.EmailValidatorUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.HibernateSQLQueryResultUtilDao;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.dao.XamplifyUtilDao;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.QueryParameterDTO;
import com.xtremand.util.service.UtilService;

@Service("oAuth")
@Transactional
public class IntegrationWrapperServiceImpl implements IntegrationWrapperService {

	private static final String NUMBER = "number";

	private static final String PERCENT = "percent";

	private static final String SELECT = "select";

	private static final String NOT_AVAILABLE = "Not Available";

	@Autowired
	IntegrationServiceHelper integrationServiceHelper;

	@Autowired
	IntegrationDao integrationDao;

	@Autowired
	UserService userService;

	@Autowired
	UserListService userListService;

	@Autowired
	private GenericDAO genericDAO;

	@Autowired
	EmailValidatorUtil emailValidator;

	@Autowired
	@Lazy
	private AsyncComponent asyncComponent;

	@Autowired
	private UtilService utilService;

	@Autowired
	PipelineDAO pipelineDao;

	@Autowired
	private UserListDAO userListDAO;

	@Autowired
	private UserDAO userDao;

	@Autowired
	PipelineService pipelineService;

	@Autowired
	private FormDao formDao;

	@Autowired
	private UtilDao utilDao;

	@Autowired
	private XamplifyUtilDao xamplifyUtilDao;

	@Autowired
	private HibernateSQLQueryResultUtilDao hibernateSQLQueryResultUtilDao;

	@Autowired
	private CustomFieldsService customFieldsService;

	@Autowired
	private LeadDAO leadDao;

	@Value("${salesforce.refreshtoken.expired}")
	private String REFRESH_TOKEN_EXPIRED;

	@Value("${marketo.old.list.name}")
	private String MARKETO_OLD_LIST_NAME;

	@Value("#{'${crms.without.lead.pipelines}'.split(',')}")
	private List<String> crmsWithoutLeadPipelines;

	@Value("#{'${crms.without.deal.pipelines}'.split(',')}")
	private List<String> crmsWithoutDealPipelines;

	/** XNFR-461 **/
	@Value("#{'${connectwise.base.cf.names}'.split(',')}")
	private List<String> connectwiseBaseCustomFields;

	@Value("#{'${salesforce.base.cf.names}'.split(',')}")
	private List<String> salesforceBaseCustomFields;

	@Value("#{'${halopsa.base.cf.names}'.split(',')}")
	private List<String> halopsaBaseCustomFields;

	@Value("#{'${crms.without.custom.form}'.split(',')}")
	private List<String> crmsWithoutCustomForm;

	@Value("#{'${multi.crm.text.priority.fields}'.split(',')}")
	private List<String> priorityFieldsForText;

	@Value("#{'${multi.crm.textarea.priority.fields}'.split(',')}")
	private List<String> priorityFieldsForTextArea;

	// XNFR-615
	@Value("#{'${salesforce.lead.base.cf.names}'.split(',')}")
	private List<String> salesforceBaseCustomFieldsForLead;

	private static final String UNAUTHORIZED = "UnAuthorized";
	private static final String SUCCESS = "Success";
	private static final String FAILED = "Failed";
	private static final String INVALID_INPUT = "Invalid Input";

	@Override
	public XtremandResponse getActiveCRMDetails(Integer loggedInUserId, Integer createdForCompanyId)
			throws IOException, ParseException {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (loggedInUserId != null && loggedInUserId > 0 && createdForCompanyId != null && createdForCompanyId > 0) {
			CompanyProfile loggedInCompany = userService.getCompanyProfileByUser(loggedInUserId);
			IntegrationDTO integrationDTO = getActiveCRMDetails(loggedInCompany, createdForCompanyId);
			if (integrationDTO != null) {
				if (loggedInCompany != null && (loggedInCompany.getId().equals(createdForCompanyId)
						|| utilService.hasPartnerShip(createdForCompanyId, loggedInCompany.getId()))) {
					Integration activeCRMIntegration = integrationDao.getActiveCRMIntegration(createdForCompanyId);
					if (activeCRMIntegration != null) {
						integrationDTO.setType(activeCRMIntegration.getType());
						integrationDTO.setActiveCRM(true);
					} else {
						integrationDTO.setActiveCRM(false);
					}
					setPipelineVisibility(integrationDTO, loggedInCompany, createdForCompanyId);
					response.setData(integrationDTO);
					responseMessage = SUCCESS;
					responseStatusCode = 200;
				} else {
					responseMessage = UNAUTHORIZED;
					responseStatusCode = 401;
				}
			}
		} else {
			responseMessage = INVALID_INPUT;
			responseStatusCode = 500;
		}
		response.setMessage(responseMessage);
		response.setStatusCode(responseStatusCode);
		return response;
	}

	private IntegrationDTO getActiveCRMDetails(CompanyProfile loggedInCompany, Integer createdForCompanyId) {
		IntegrationDTO integrationDTO = null;
		if (loggedInCompany != null && (loggedInCompany.getId().equals(createdForCompanyId)
				|| utilService.hasPartnerShip(createdForCompanyId, loggedInCompany.getId()))) {
			integrationDTO = new IntegrationDTO();
			integrationDTO.setActiveCRM(false);
			if (createdForCompanyId != null && createdForCompanyId > 0) {
				integrationDTO.setCreatedForActiveCRMType(IntegrationType.XAMPLIFY);
				integrationDTO.setHasCustomForm(true);
			}
		}
		return integrationDTO;
	}

	private void setCreatedForActiveCRMDetails(CompanyProfile loggedInCompany, Integer createdForCompanyId,
			IntegrationDTO integrationDTO, Integration createdForActiveCRMIntegration) {
		if (createdForActiveCRMIntegration != null) {
			integrationDTO.setType(createdForActiveCRMIntegration.getType());
			integrationDTO.setActiveCRM(true);
			integrationDTO.setCreatedForActiveCRMType(createdForActiveCRMIntegration.getType());
			integrationDTO.setCreatedForActiveCRM(true);
			if (loggedInCompany.getId().equals(createdForCompanyId)) {
				integrationDTO.setCreatedByActiveCRMType(createdForActiveCRMIntegration.getType());
				integrationDTO.setCreatedByActiveCRM(true);
			}
			integrationDTO.setDealByPartnerEnabled(createdForActiveCRMIntegration.isDealByPartnerEnabled());
			integrationDTO.setDealByVendorEnabled(createdForActiveCRMIntegration.isDealByVendorEnabled());
			integrationDTO.setDealBySelfLeadEnabled(createdForActiveCRMIntegration.isDealBySelfLeadEnabled());
			setPipelineDetails(createdForActiveCRMIntegration, integrationDTO);
			setLeadAndDealFormSettings(createdForActiveCRMIntegration, integrationDTO);
			/** XNFR-693 **/
			integrationDTO.setPartnerEditLead(createdForActiveCRMIntegration.isPartnerEditLead());
			integrationDTO.setPartnerDeleteLead(createdForActiveCRMIntegration.isPartnerDeleteLead());
		}
	}

	private void setCreatedByActiveCRMDetails(CompanyProfile loggedInCompany, Integer createdForCompanyId,
			IntegrationDTO integrationDTO, Integration createdForActiveCRMIntegration) {
		if (!loggedInCompany.getId().equals(createdForCompanyId)) {
			Integration createdByActiveCRMIntegration = integrationDao.getActiveCRMIntegration(loggedInCompany.getId());
			if (createdByActiveCRMIntegration != null) {
				integrationDTO.setCreatedByActiveCRMType(createdByActiveCRMIntegration.getType());
				integrationDTO.setCreatedByActiveCRM(true);
				integrationDTO.setActiveCRM(true);
				if (createdForActiveCRMIntegration == null) {
					integrationDTO.setType(createdByActiveCRMIntegration.getType());
				}

			}
		}
	}


	@SuppressWarnings("unchecked")
	private List<PipelineDto> getCreatedForCompanyPipelines(Integer loggedInUserId, CompanyProfile loggedInCompany,
			Integer vendorCompanyId, IntegrationDTO integrationDTO, PipelineType type, Long halopsaTicketTypeId) {
		IntegrationType integrationType = IntegrationType.XAMPLIFY;
		if (integrationDTO != null && integrationDTO.getCreatedForActiveCRMType() != null) {
			integrationType = integrationDTO.getCreatedForActiveCRMType();
		}

		List<PipelineDto> createdForCompanyPipelines = new ArrayList<PipelineDto>();
		XtremandResponse vendorPipelineResponse = pipelineService.getPipelinesByIntegrationType(loggedInUserId,
				vendorCompanyId, type, integrationType, halopsaTicketTypeId);
		if (vendorPipelineResponse != null && vendorPipelineResponse.getData() != null) {
			createdForCompanyPipelines = (List<PipelineDto>) vendorPipelineResponse.getData();
		}

		return createdForCompanyPipelines;
	}

	@SuppressWarnings("unchecked")
	private List<PipelineDto> getCreatedByCompanyPipelines(Integer loggedInUserId, IntegrationDTO integrationDTO,
			PipelineType type, Long halopsaTicketTypeId) {
		List<PipelineDto> createdByCompanyPipelines = new ArrayList<PipelineDto>();
		IntegrationType integrationType = IntegrationType.XAMPLIFY;
		if (integrationDTO != null && integrationDTO.getCreatedByActiveCRMType() != null) {
			integrationType = integrationDTO.getCreatedByActiveCRMType();
		}

		XtremandResponse partnerPipelineResponse = pipelineService.getPipelinesByIntegrationType(loggedInUserId, type,
				integrationType, halopsaTicketTypeId);
		if (partnerPipelineResponse != null && partnerPipelineResponse.getData() != null) {
			createdByCompanyPipelines = (List<PipelineDto>) partnerPipelineResponse.getData();
		}

		return createdByCompanyPipelines;
	}

	private FormDTO buildCustomDealForm(FormDTO activeCRM1CustomFormDTO, FormDTO activeCRM2CustomFormDTO) {
		FormDTO customFormDTO = new FormDTO();
		List<FormLabelDTO> activeCRM1CFLabelsToShow = new ArrayList<FormLabelDTO>();
		List<FormLabelDTO> activeCRM2CFLabelsToShow = new ArrayList<FormLabelDTO>();

		List<FormLabelDTO> activeCRM2LabelDTOs = new ArrayList<FormLabelDTO>();
		List<String> activeCRM2LabelDTONames = new ArrayList<String>();
		if (activeCRM2CustomFormDTO != null && activeCRM2CustomFormDTO.getFormLabelDTOs() != null) {
			activeCRM2LabelDTOs = activeCRM2CustomFormDTO.getFormLabelDTOs();
			activeCRM2LabelDTONames = activeCRM2LabelDTOs.stream().map(FormLabelDTO::getDisplayName)
					.collect(Collectors.toList());
		}

		boolean hasPartnerCustomForm = false;
		if (activeCRM1CustomFormDTO != null && activeCRM1CustomFormDTO.getFormLabelDTOs() != null
				&& !activeCRM1CustomFormDTO.getFormLabelDTOs().isEmpty()) {
			hasPartnerCustomForm = true;
			for (FormLabelDTO formLabelDTO : activeCRM1CustomFormDTO.getFormLabelDTOs()) {
				if (formLabelDTO != null) {
					List<String> baseCustomFields = getBaseCustomFields(activeCRM1CustomFormDTO.getFormType());
					if (baseCustomFields != null) {
						if (!baseCustomFields.contains(formLabelDTO.getLabelId())) {
							if (showLabel(formLabelDTO, activeCRM2CustomFormDTO.getFormLabelDTOs(),
									activeCRM2LabelDTONames)) {
								activeCRM1CFLabelsToShow.add(formLabelDTO);
							}
						} else {
							activeCRM1CFLabelsToShow.add(formLabelDTO);
						}
					} else {
						activeCRM1CFLabelsToShow.add(formLabelDTO);
					}
				}
			}
			customFormDTO.getConnectWiseProducts().addAll(activeCRM1CustomFormDTO.getConnectWiseProducts());
		}

		if (activeCRM2CustomFormDTO != null && activeCRM2CustomFormDTO.getFormLabelDTOs() != null) {
			for (FormLabelDTO formLabelDTO : activeCRM2CustomFormDTO.getFormLabelDTOs()) {
				List<String> baseCustomFields = getBaseCustomFields(activeCRM2CustomFormDTO.getFormType());
				if (baseCustomFields != null) {
					if (!baseCustomFields.contains(formLabelDTO.getLabelId())) {
						if (activeCRM2LabelDTONames.contains(formLabelDTO.getDisplayName())) {
							activeCRM2CFLabelsToShow.add(formLabelDTO);
						}
					} else {
						if (!hasPartnerCustomForm) {
							activeCRM2CFLabelsToShow.add(formLabelDTO);
						}
					}
				} else {
					activeCRM2CFLabelsToShow.add(formLabelDTO);
				}
			}
			customFormDTO.getConnectWiseProducts().addAll(activeCRM2CustomFormDTO.getConnectWiseProducts());
		}

		customFormDTO.getFormLabelDTOs().addAll(activeCRM1CFLabelsToShow);
		customFormDTO.getFormLabelDTOs().addAll(activeCRM2CFLabelsToShow);
		return customFormDTO;
	}

	private List<String> getBaseCustomFields(FormTypeEnum formType) {
		List<String> baseCustomFields = null;
		switch (formType) {

		default:
			baseCustomFields = null;

		}

		return baseCustomFields;
	}

	private boolean showLabel(FormLabelDTO formLabelDTO, List<FormLabelDTO> formLabelDTOsToCompare,
			List<String> formLabelDTONames) {
		boolean showLabel = false;
		FormLabelDTO matchedFormLabelDTO = utilService.getMatchedObject(formLabelDTO, formLabelDTOsToCompare);
		if (matchedFormLabelDTO != null) {
			if (matchedFormLabelDTO.getLabelType().equals(SELECT)
					|| matchedFormLabelDTO.getLabelType().equals("radio")) {
				if (formLabelDTO.getLabelType().equals(SELECT) || formLabelDTO.getLabelType().equals("radio")) {
					List<FormChoiceDTO> ch1 = matchedFormLabelDTO.getDropDownChoices();
					List<FormChoiceDTO> ch2 = formLabelDTO.getDropDownChoices();
					List<String> ch1Names = ch1.stream().map(FormChoiceDTO::getName).collect(Collectors.toList());
					List<String> ch2Names = ch2.stream().map(FormChoiceDTO::getName).collect(Collectors.toList());
					if (!ch1Names.toString().contentEquals(ch2Names.toString())) {
						showLabel = true;
					} else {
						showLabel = true;
						removeMatchedFormLabelDTO(formLabelDTO, matchedFormLabelDTO, formLabelDTONames);
					}
				} else if (formLabelDTO.getLabelType().equals(PERCENT)
						|| formLabelDTO.getLabelType().equals(NUMBER)) {
					if (!isNumberDropdown(matchedFormLabelDTO)) {
						showLabel = true;
					}
				} else if (!formLabelDTO.getLabelType().equals("text")
						&& !formLabelDTO.getLabelType().equals("textarea")) {
					showLabel = true;
				}
			} else if (matchedFormLabelDTO.getLabelType().equals("multiselect")) {
				if (formLabelDTO.getLabelType().equals("multiselect")) {
					List<FormChoiceDTO> ch1 = matchedFormLabelDTO.getDropDownChoices();
					List<FormChoiceDTO> ch2 = formLabelDTO.getDropDownChoices();
					List<String> ch1Names = ch1.stream().map(FormChoiceDTO::getName).collect(Collectors.toList());
					List<String> ch2Names = ch2.stream().map(FormChoiceDTO::getName).collect(Collectors.toList());
					if (!ch1Names.toString().contentEquals(ch2Names.toString())) {
						showLabel = true;
					} else {
						showLabel = true;
						removeMatchedFormLabelDTO(formLabelDTO, matchedFormLabelDTO, formLabelDTONames);
					}
				} else if (!formLabelDTO.getLabelType().equals("text")
						&& !formLabelDTO.getLabelType().equals("textarea")) {
					showLabel = true;
				}
			} else if (matchedFormLabelDTO.getLabelType().equals("checkbox")) {
				if (formLabelDTO.getLabelType().equals("checkbox")) {
					showLabel = true;
					removeMatchedFormLabelDTO(formLabelDTO, matchedFormLabelDTO, formLabelDTONames);
				} else {
					showLabel = true;
				}
			} else if (matchedFormLabelDTO.getLabelType().equals(PERCENT)) {
				if (formLabelDTO.getLabelType().equals(PERCENT)) {
					showLabel = true;
					removeMatchedFormLabelDTO(formLabelDTO, matchedFormLabelDTO, formLabelDTONames);
				} else if (formLabelDTO.getLabelType().equals(SELECT)) {
					showLabel = true;
					if (isNumberDropdown(formLabelDTO)) {
						removeMatchedFormLabelDTO(formLabelDTO, matchedFormLabelDTO, formLabelDTONames);
					}
				} else if (!formLabelDTO.getLabelType().equals("text")
						&& !formLabelDTO.getLabelType().equals("textarea")) {
					showLabel = true;
				}
			} else if (matchedFormLabelDTO.getLabelType().equals("text")) {
				if (priorityFieldsForText.contains(formLabelDTO.getLabelType())) {
					showLabel = true;
					removeMatchedFormLabelDTO(formLabelDTO, matchedFormLabelDTO, formLabelDTONames);
				} else {
					showLabel = true;
				}
			} else if (matchedFormLabelDTO.getLabelType().equals("textarea")) {
				if (priorityFieldsForTextArea.contains(formLabelDTO.getLabelType())) {
					showLabel = true;
					removeMatchedFormLabelDTO(formLabelDTO, matchedFormLabelDTO, formLabelDTONames);
				} else if (!formLabelDTO.getLabelType().equals("text")) {
					showLabel = true;
				}
			} else if (matchedFormLabelDTO.getLabelType().equals("date")) {
				if (formLabelDTO.getLabelType().equals("date")) {
					showLabel = true;
					removeMatchedFormLabelDTO(formLabelDTO, matchedFormLabelDTO, formLabelDTONames);
				} else if (!formLabelDTO.getLabelType().equals("text")
						&& !formLabelDTO.getLabelType().equals("textarea")) {
					showLabel = true;
				}
			} else if (matchedFormLabelDTO.getLabelType().equals("datetime")) {
				if (formLabelDTO.getLabelType().equals("datetime")) {
					showLabel = true;
					removeMatchedFormLabelDTO(formLabelDTO, matchedFormLabelDTO, formLabelDTONames);
				} else if (!formLabelDTO.getLabelType().equals("text")
						&& !formLabelDTO.getLabelType().equals("textarea")) {
					showLabel = true;
				}
			} else if (matchedFormLabelDTO.getLabelType().equals("time")) {
				if (formLabelDTO.getLabelType().equals("time")) {
					showLabel = true;
					removeMatchedFormLabelDTO(formLabelDTO, matchedFormLabelDTO, formLabelDTONames);
				} else if (!formLabelDTO.getLabelType().equals("text")
						&& !formLabelDTO.getLabelType().equals("textarea")) {
					showLabel = true;
				}
			} else if (matchedFormLabelDTO.getLabelType().equals("email")) {
				if (formLabelDTO.getLabelType().equals("email")) {
					showLabel = true;
					removeMatchedFormLabelDTO(formLabelDTO, matchedFormLabelDTO, formLabelDTONames);
				} else if (!formLabelDTO.getLabelType().equals("text")
						&& !formLabelDTO.getLabelType().equals("textarea")) {
					showLabel = true;
				}
			} else if (matchedFormLabelDTO.getLabelType().equals("currency")) {
				if (formLabelDTO.getLabelType().equals("currency")) {
					showLabel = true;
					removeMatchedFormLabelDTO(formLabelDTO, matchedFormLabelDTO, formLabelDTONames);
				} else if (!formLabelDTO.getLabelType().equals("text")
						&& !formLabelDTO.getLabelType().equals("textarea")) {
					showLabel = true;
				}
			} else if (matchedFormLabelDTO.getLabelType().equals("url")) {
				if (formLabelDTO.getLabelType().equals("url")) {
					showLabel = true;
					removeMatchedFormLabelDTO(formLabelDTO, matchedFormLabelDTO, formLabelDTONames);
				} else if (!formLabelDTO.getLabelType().equals("text")
						&& !formLabelDTO.getLabelType().equals("textarea")) {
					showLabel = true;
				}
			} else if (matchedFormLabelDTO.getLabelType().equals("phone")) {
				if (formLabelDTO.getLabelType().equals("phone")) {
					showLabel = true;
					removeMatchedFormLabelDTO(formLabelDTO, matchedFormLabelDTO, formLabelDTONames);
				} else if (!formLabelDTO.getLabelType().equals("text")
						&& !formLabelDTO.getLabelType().equals("textarea")) {
					showLabel = true;
				}
			} else if (matchedFormLabelDTO.getLabelType().equals(NUMBER)) {
				if (formLabelDTO.getLabelType().equals(NUMBER)) {
					showLabel = true;
					removeMatchedFormLabelDTO(formLabelDTO, matchedFormLabelDTO, formLabelDTONames);
				} else if (formLabelDTO.getLabelType().equals(SELECT)) {
					showLabel = true;
					if (isNumberDropdown(formLabelDTO)) {
						removeMatchedFormLabelDTO(formLabelDTO, matchedFormLabelDTO, formLabelDTONames);
					}
				} else if (!formLabelDTO.getLabelType().equals("text")
						&& !formLabelDTO.getLabelType().equals("textarea")) {
					showLabel = true;
				}
			} else if (matchedFormLabelDTO.getLabelType().equals("geolocation")) {
				if (formLabelDTO.getLabelType().equals("geolocation")) {
					showLabel = true;
					removeMatchedFormLabelDTO(formLabelDTO, matchedFormLabelDTO, formLabelDTONames);
				} else if (!formLabelDTO.getLabelType().equals("text")
						&& !formLabelDTO.getLabelType().equals("textarea")) {
					showLabel = true;
				}
			}
		} else {
			showLabel = true;
		}
		return showLabel;
	}

	private boolean isNumberDropdown(FormLabelDTO matchedFormLabelDTO) {
		boolean isNumberDropdown = true;
		List<FormChoiceDTO> choices = matchedFormLabelDTO.getDropDownChoices();
		if (choices != null && !choices.isEmpty()) {
			for (FormChoiceDTO choice : choices) {
				if (choice != null && StringUtils.isNotBlank(choice.getName())) {
					if (!utilService.isNumeric(choice.getName())) {
						isNumberDropdown = false;
						break;
					}
				}
			}
		} else {
			isNumberDropdown = false;
		}
		return isNumberDropdown;
	}

	private void removeMatchedFormLabelDTO(FormLabelDTO formLabelDTO, FormLabelDTO matchedFormLabelDTO,
			List<String> formLabelDTONames) {
		if (formLabelDTONames != null) {
			formLabelDTONames.remove(matchedFormLabelDTO.getDisplayName());
			if (matchedFormLabelDTO.isRequired()) {
				formLabelDTO.setRequired(true);
			}
		}
	}


	@Override
	public XtremandResponse getActiveCRMDetails(Integer loggedInUserId) throws IOException, ParseException {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (loggedInUserId != null && loggedInUserId > 0) {
			CompanyProfile loggedInCompany = userService.getCompanyProfileByUser(loggedInUserId);
			if (loggedInCompany != null) {
				Integration activeCRMIntegration = integrationDao.getActiveCRMIntegration(loggedInCompany.getId());
				IntegrationDTO integrationDTO = new IntegrationDTO();
				if (activeCRMIntegration != null) {
					integrationDTO.setType(activeCRMIntegration.getType());
					integrationDTO.setActiveCRM(true);
					integrationDTO.setShowLeadPipeline(activeCRMIntegration.isShowLeadPipeline());
					integrationDTO.setShowLeadPipelineStage(activeCRMIntegration.isShowLeadPipelineStage());
				} else {
					integrationDTO.setActiveCRM(false);
				}
				response.setData(integrationDTO);
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
	public XtremandResponse getIntegrationUserDetails(String type, Integer userId) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (userId != null && userId > 0) {
			User loggedInUser = userService.loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, userId)),
					new FindLevel[] { FindLevel.COMPANY_PROFILE });
			CompanyProfile loggedInCompany = loggedInUser.getCompanyProfile();
			if (loggedInCompany != null) {
				Integration integration = integrationDao.getUserIntegrationDetails(loggedInCompany.getId(),
						IntegrationType.valueOf(type.toUpperCase()));
				if (integration != null) {
					IntegrationDTO integrationDTO = assignValuesToIntegrationDto(loggedInUser, integration);
					response.setData(integrationDTO);
					responseMessage = SUCCESS;
					responseStatusCode = 200;
				}
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

	private IntegrationDTO assignValuesToIntegrationDto(User loggedInUser, Integration integration) {
		IntegrationDTO integrationDTO = new IntegrationDTO();
		integrationDTO.setExternalEmail(
				(StringUtils.isNotBlank(integration.getExternalEmail())) ? integration.getExternalEmail()
						: NOT_AVAILABLE);
		integrationDTO.setExternalDisplayName(
				(StringUtils.isNotBlank(integration.getExternalDisplayName())) ? integration.getExternalDisplayName()
						: NOT_AVAILABLE);
		integrationDTO.setExternalUserName(
				(StringUtils.isNotBlank(integration.getExternalUserName())) ? integration.getExternalUserName()
						: NOT_AVAILABLE);
		integrationDTO.setExternalOrganizationName((StringUtils.isNotBlank(integration.getExternalOrganizationName()))
				? integration.getExternalOrganizationName()
				: NOT_AVAILABLE);
		integrationDTO.setExternalThumbnail(
				(StringUtils.isNotBlank(integration.getExternalThumbnail())) ? integration.getExternalThumbnail() : "");
		integrationDTO.setActiveCRM(integration.isActive());
		/* XNFR-615 */
		integrationDTO.setDealByPartnerEnabled(integration.isDealByPartnerEnabled());
		integrationDTO.setDealByVendorEnabled(integration.isDealByVendorEnabled());
		integrationDTO.setDealBySelfLeadEnabled(integration.isDealBySelfLeadEnabled());
		setLeadAndDealFormSettings(integration, integrationDTO);

		setPipelineDetails(integration, integrationDTO);
		setEnableLink(loggedInUser, integration, integrationDTO);
		/** XNFR-693 **/
		integrationDTO.setPartnerEditLead(integration.isPartnerEditLead());
		integrationDTO.setPartnerDeleteLead(integration.isPartnerDeleteLead());
		return integrationDTO;
	}

	private void setLeadAndDealFormSettings(Integration integration, IntegrationDTO integrationDTO) {
		integrationDTO.setLeadFormColumnLayout(integration.getLeadFormColumnLayout());
		integrationDTO.setDealFormColumnLayout(integration.getDealFormColumnLayout());
		integrationDTO.setLeadDescription(integration.getLeadDescription());
		integrationDTO.setDealDescription(integration.getDealDescription());
		// XNFR-681
		integrationDTO.setLeadTitle(integration.getLeadTitle());
		integrationDTO.setDealTitle(integration.getDealTitle());
	}

	private void setPipelineDetails(Integration integration, IntegrationDTO integrationDTO) {
		integrationDTO.setShowLeadPipeline(integration.isShowLeadPipeline());
		integrationDTO.setShowLeadPipelineStage(integration.isShowLeadPipelineStage());
		integrationDTO.setShowDealPipeline(integration.isShowDealPipeline());
		integrationDTO.setShowDealPipelineStage(integration.isShowDealPipelineStage());
		if (integration.getLeadPipelineId() != null) {
			integrationDTO.setLeadPipelineId(integration.getLeadPipelineId().getId());
		} else {
			integrationDTO.setLeadPipelineId(0);
		}
		if (integration.getLeadPipelineStageId() != null) {
			integrationDTO.setLeadPipelineStageId(integration.getLeadPipelineStageId().getId());
		} else {
			integrationDTO.setLeadPipelineStageId(0);
		}
		if (integration.getDealPipelineId() != null) {
			integrationDTO.setDealPipelineId(integration.getDealPipelineId().getId());
		} else {
			integrationDTO.setDealPipelineId(0);
		}
		if (integration.getDealPipelineStageId() != null) {
			integrationDTO.setDealPipelineStageId(integration.getDealPipelineStageId().getId());
		} else {
			integrationDTO.setDealPipelineStageId(0);
		}
	}

	private void setEnableLink(User loggedInUser, Integration integration, IntegrationDTO integrationDTO) {
		Integer integrationsCount = integrationDao.getTotalIntegrationsCount(loggedInUser.getCompanyProfile().getId());
		if (!integration.isActive()) {
			integrationDTO.setEnableUnlink(true);
		} else {
			if (integrationsCount == 1) {
				integrationDTO.setEnableUnlink(true);
			}
		}
	}

	@Override
	public XtremandResponse unlinkCRM(String type, Integer userId) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (userId != null && userId > 0) {
			User loggedInUser = userService.loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, userId)),
					new FindLevel[] { FindLevel.COMPANY_PROFILE });
			CompanyProfile loggedInCompany = loggedInUser.getCompanyProfile();
			if (loggedInCompany != null) {
				Integration integration = integrationDao.getUserIntegrationDetails(loggedInCompany.getId(),
						IntegrationType.valueOf(type.toUpperCase()));
				Integer integrationsCount = integrationDao.getTotalIntegrationsCount(loggedInCompany.getId());

				if (integration != null && (!integration.isActive() || integrationsCount.equals(1))) {
					List<Pipeline> pipelines = pipelineDao.getPipelinesByIntegrationType(loggedInCompany.getId(),
							IntegrationType.valueOf(type.toUpperCase()));
					if (pipelines != null && !pipelines.isEmpty()) {
						for (Pipeline pipeline : pipelines) {
							if (pipeline != null) {
								pipeline.setIntegrationType(IntegrationType.XAMPLIFY);
								pipeline.setName(pipeline.getName() + "_" + System.currentTimeMillis());
								pipeline.setSalesforcePipeline(false);
								pipeline.initialiseCommonFields(false, userId);
							}
						}
					}
					genericDAO.remove(integration);
					responseMessage = SUCCESS;
					responseStatusCode = 200;
				} else {
					responseMessage = UNAUTHORIZED;
					responseStatusCode = 401;
				}
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
	public XtremandResponse getActiveCRMPipelines(Integer createdForCompanyId, Integer loggedInUserId,
			Integer campaignId, PipelineType type, Long halopsaTicketTypeId) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (loggedInUserId != null && loggedInUserId > 0 && createdForCompanyId != null && createdForCompanyId > 0) {
			CompanyProfile loggedInCompany = userService.getCompanyProfileByUser(loggedInUserId);
			IntegrationDTO integrationDTO = getActiveCRMDetails(loggedInCompany, createdForCompanyId);
			if (integrationDTO != null) {
				setPipelines(loggedInUserId, loggedInCompany, createdForCompanyId, campaignId, integrationDTO, type,
						halopsaTicketTypeId);
				setPipelineVisibility(integrationDTO, loggedInCompany, createdForCompanyId);
				response.setData(integrationDTO);
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

	private void setPipelineVisibility(IntegrationDTO integrationDTO, CompanyProfile loggedInCompany,
			Integer createdForCompanyId) {
		if (!loggedInCompany.getId().equals(createdForCompanyId)) {
			integrationDTO.setShowCreatedByPipelineAndStageOnTop(true);
			integrationDTO.setShowCreatedByLeadPipelineAndStageOnTop(true);
		}
	}

	private void setPipelines(Integer loggedInUserId, CompanyProfile loggedInCompany, Integer vendorCompanyId,
			Integer campaignId, IntegrationDTO integrationDTO, PipelineType type, Long halopsaTicketTypeId) {
		List<PipelineDto> createdForCompanyPipelines = new ArrayList<PipelineDto>();
		List<PipelineDto> createdByCompanyPipelines = new ArrayList<PipelineDto>();
		if (campaignId != null && campaignId > 0) {
			// && integrationDTO.getCreatedForActiveCRMType() != IntegrationType.SALESFORCE
			if (integrationDTO.isCreatedForActiveCRM() || integrationDTO.isCreatedByActiveCRM()) {
			} else {
				if (!integrationDTO.isCreatedForActiveCRM()) {
					createdForCompanyPipelines = getCreatedForCompanyPipelines(loggedInUserId, loggedInCompany,
							vendorCompanyId, integrationDTO, type, halopsaTicketTypeId);
				}
				if (!integrationDTO.isCreatedByActiveCRM() && !loggedInCompany.getId().equals(vendorCompanyId)) {
					createdByCompanyPipelines = getCreatedByCompanyPipelines(loggedInUserId, integrationDTO, type,
							halopsaTicketTypeId);
				}
			}
		} else {
			createdForCompanyPipelines = getCreatedForCompanyPipelines(loggedInUserId, loggedInCompany, vendorCompanyId,
					integrationDTO, type, halopsaTicketTypeId);
			if (!loggedInCompany.getId().equals(vendorCompanyId)) {
				createdByCompanyPipelines = getCreatedByCompanyPipelines(loggedInUserId, integrationDTO, type,
						halopsaTicketTypeId);
			}
		}
		integrationDTO.setCreatedByCompanyPipelines(createdByCompanyPipelines);
		integrationDTO.setCreatedForCompanyPipelines(createdForCompanyPipelines);
	}

	@Override
	public XtremandResponse getDealPipelinesForView(Integer dealId, Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (loggedInUserId != null && loggedInUserId > 0 && dealId != null && dealId > 0) {
			CompanyProfile loggedInCompany = userService.getCompanyProfileByUser(loggedInUserId);
			Deal deal = genericDAO.get(Deal.class, dealId);
			if (deal != null) {
				IntegrationDTO integrationDTO = getActiveCRMDetails(deal.getCreatedByCompany(),
						deal.getCreatedForCompany().getId());
				if (integrationDTO != null) {
					setPipelines(deal, loggedInUserId, loggedInCompany, integrationDTO);
					setPipelineVisibility(deal, integrationDTO, loggedInCompany);
					response.setData(integrationDTO);
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
		} else {
			responseMessage = INVALID_INPUT;
			responseStatusCode = 500;
		}
		response.setMessage(responseMessage);
		response.setStatusCode(responseStatusCode);
		return response;
	}

	private void setPipelineVisibility(Deal deal, IntegrationDTO integrationDTO, CompanyProfile loggedInCompany) {
		if (!deal.getCreatedByCompany().getId().equals(deal.getCreatedForCompany().getId())) {
			if (!deal.getCreatedForCompany().getId().equals(loggedInCompany.getId())) {
				integrationDTO.setShowCreatedByPipelineAndStageOnTop(true);
				integrationDTO.setShowCreatedByLeadPipelineAndStageOnTop(true);
			}
		}
	}

	private void setPipelines(Deal deal, Integer loggedInUserId, CompanyProfile loggedInCompany,
			IntegrationDTO integrationDTO) {
		List<PipelineDto> createdForCompanyPipelines = new ArrayList<PipelineDto>();
		List<PipelineDto> createdByCompanyPipelines = new ArrayList<PipelineDto>();

		createdForCompanyPipelines = getCreatedForCompanyPipelines(loggedInUserId, loggedInCompany.getId(),
				deal.getCreatedByCompany(), deal.getCreatedForCompany().getId(), integrationDTO);
		if (!deal.getCreatedByCompany().getId().equals(deal.getCreatedForCompany().getId())) {
			createdByCompanyPipelines = getCreatedByCompanyPipelines(loggedInUserId, loggedInCompany.getId(),
					deal.getCreatedByCompany().getId(), integrationDTO);

		}

		integrationDTO.setCreatedForCompanyPipelines(createdForCompanyPipelines);
		integrationDTO.setCreatedByCompanyPipelines(createdByCompanyPipelines);

	}

	@SuppressWarnings("unchecked")
	private List<PipelineDto> getCreatedByCompanyPipelines(Integer loggedInUserId, Integer loggedInCompanyId,
			Integer createdByCompanyId, IntegrationDTO integrationDTO) {
		List<PipelineDto> createdByCompanyPipelines = new ArrayList<PipelineDto>();
		IntegrationType integrationType = IntegrationType.XAMPLIFY;
		if (integrationDTO != null && integrationDTO.getCreatedByActiveCRMType() != null) {
			integrationType = integrationDTO.getCreatedByActiveCRMType();
		}

		XtremandResponse createdByPipelineResponse = pipelineService.getPipelinesForCompanyByIntegrationType(
				loggedInUserId, createdByCompanyId, PipelineType.DEAL, integrationType);
		if (createdByPipelineResponse != null && createdByPipelineResponse.getData() != null) {
			createdByCompanyPipelines = (List<PipelineDto>) createdByPipelineResponse.getData();
		}

		return createdByCompanyPipelines;
	}

	@SuppressWarnings("unchecked")
	private List<PipelineDto> getCreatedForCompanyPipelines(Integer loggedInUserId, Integer loggedInCompanyId,
			CompanyProfile createdByCompanyId, Integer createdForCompanyId, IntegrationDTO integrationDTO) {
		List<PipelineDto> createdForCompanyPipelines = new ArrayList<PipelineDto>();
		IntegrationType integrationType = IntegrationType.XAMPLIFY;
		if (integrationDTO != null && integrationDTO.getCreatedForActiveCRMType() != null) {
			integrationType = integrationDTO.getCreatedForActiveCRMType();
		}

		XtremandResponse createdForPipelineResponse = pipelineService.getPipelinesForCompanyByIntegrationType(
				loggedInUserId, createdForCompanyId, PipelineType.DEAL, integrationType);
		if (createdForPipelineResponse != null && createdForPipelineResponse.getData() != null) {
			createdForCompanyPipelines = (List<PipelineDto>) createdForPipelineResponse.getData();
		}
		return createdForCompanyPipelines;
	}

	@Override
	public XtremandResponse getLeadPipelinesForView(Integer leadId, Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (loggedInUserId != null && loggedInUserId > 0 && leadId != null && leadId > 0) {
			CompanyProfile loggedInCompany = userService.getCompanyProfileByUser(loggedInUserId);
			Lead lead = genericDAO.get(Lead.class, leadId);
			if (lead != null) {
				IntegrationDTO integrationDTO = getActiveCRMDetails(lead.getCreatedByCompany(),
						lead.getCreatedForCompany().getId());
				if (integrationDTO != null) {
					setPipelines(lead, loggedInUserId, loggedInCompany, integrationDTO);
					setPipelineVisibility(lead, integrationDTO, loggedInCompany);
					response.setData(integrationDTO);
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
		} else {
			responseMessage = INVALID_INPUT;
			responseStatusCode = 500;
		}
		response.setMessage(responseMessage);
		response.setStatusCode(responseStatusCode);
		return response;
	}

	private void setPipelineVisibility(Lead lead, IntegrationDTO integrationDTO, CompanyProfile loggedInCompany) {
		if (!lead.getCreatedByCompany().getId().equals(lead.getCreatedForCompany().getId())) {
			if (!lead.getCreatedForCompany().getId().equals(loggedInCompany.getId())) {
				integrationDTO.setShowCreatedByPipelineAndStageOnTop(true);
				integrationDTO.setShowCreatedByLeadPipelineAndStageOnTop(true);
			}
		}
	}

	private void setPipelines(Lead lead, Integer loggedInUserId, CompanyProfile loggedInCompany,
			IntegrationDTO integrationDTO) {
		List<PipelineDto> createdForCompanyPipelines = new ArrayList<PipelineDto>();
		List<PipelineDto> createdByCompanyPipelines = new ArrayList<PipelineDto>();
		createdForCompanyPipelines = getCreatedForCompanyPipelinesForLead(loggedInUserId, loggedInCompany.getId(),
				lead.getCreatedByCompany(), lead.getCreatedForCompany().getId(), integrationDTO);
		if (!lead.getCreatedByCompany().getId().equals(lead.getCreatedForCompany().getId())) {
			createdByCompanyPipelines = getCreatedByCompanyPipelinesForLead(loggedInUserId, loggedInCompany.getId(),
					lead.getCreatedByCompany().getId(), integrationDTO);
		}

		integrationDTO.setCreatedForCompanyPipelines(createdForCompanyPipelines);
		integrationDTO.setCreatedByCompanyPipelines(createdByCompanyPipelines);
	}

	@SuppressWarnings("unchecked")
	private List<PipelineDto> getCreatedByCompanyPipelinesForLead(Integer loggedInUserId, Integer loggedInCompanyId,
			Integer createdByCompanyId, IntegrationDTO integrationDTO) {
		List<PipelineDto> createdByCompanyPipelines = new ArrayList<PipelineDto>();
		IntegrationType integrationType = IntegrationType.XAMPLIFY;
		if (integrationDTO != null && integrationDTO.getCreatedByActiveCRMType() != null) {
			integrationType = integrationDTO.getCreatedByActiveCRMType();
		}

		XtremandResponse createdByPipelineResponse = null;
		createdByPipelineResponse = pipelineService.getPipelinesForCompanyByIntegrationType(loggedInUserId,
				createdByCompanyId, PipelineType.LEAD, integrationType);
		if (createdByPipelineResponse != null && createdByPipelineResponse.getData() != null) {
			createdByCompanyPipelines = (List<PipelineDto>) createdByPipelineResponse.getData();
		}

		return createdByCompanyPipelines;
	}

	@SuppressWarnings("unchecked")
	private List<PipelineDto> getCreatedForCompanyPipelinesForLead(Integer loggedInUserId, Integer loggedInCompanyId,
			CompanyProfile createdByCompanyId, Integer createdForCompanyId, IntegrationDTO integrationDTO) {
		List<PipelineDto> createdForCompanyPipelines = new ArrayList<PipelineDto>();
		IntegrationType integrationType = IntegrationType.XAMPLIFY;
		if (integrationDTO != null && integrationDTO.getCreatedForActiveCRMType() != null) {
			integrationType = integrationDTO.getCreatedForActiveCRMType();
		}
		XtremandResponse createdForPipelineResponse = null;

		createdForPipelineResponse = pipelineService.getPipelinesForCompanyByIntegrationType(loggedInUserId,
				createdForCompanyId, PipelineType.LEAD, integrationType);

		if (createdForPipelineResponse != null && createdForPipelineResponse.getData() != null) {
			createdForCompanyPipelines = (List<PipelineDto>) createdForPipelineResponse.getData();
		}
		return createdForCompanyPipelines;
	}

	@Override
	public XtremandResponse updateCRMSettings(Integer userId, String integrationType, IntegrationDTO integrationDto) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (userId != null && userId > 0) {
			User loggedInUser = userService.loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, userId)),
					new FindLevel[] { FindLevel.COMPANY_PROFILE });
			CompanyProfile loggedInCompany = loggedInUser.getCompanyProfile();
			if (loggedInCompany != null) {
				Integration integration = integrationDao.getUserIntegrationDetails(loggedInCompany.getId(),
						IntegrationType.valueOf(integrationType.toUpperCase()));
				if (integration != null) {
					if (integrationDto.getLeadDescription() != null
							&& !integrationDto.getLeadDescription().equals("")) {
						integration.setLeadDescription(integrationDto.getLeadDescription());
					} else {
						integration.setLeadDescription(null);
					}
					if (integrationDto.getDealDescription() != null
							&& !integrationDto.getDealDescription().equals("")) {
						integration.setDealDescription(integrationDto.getDealDescription());
					} else {
						integration.setDealDescription(null);
					}
					integration.setShowLeadPipeline(integrationDto.isShowLeadPipeline());
					integration.setShowLeadPipelineStage(integrationDto.isShowLeadPipelineStage());
					integration.setShowDealPipeline(integrationDto.isShowDealPipeline());
					integration.setShowDealPipelineStage(integrationDto.isShowDealPipelineStage());
					integration.setDealByPartnerEnabled(integrationDto.isDealByPartnerEnabled());
					integration.setDealByVendorEnabled(integrationDto.isDealByVendorEnabled());
					integration.setDealBySelfLeadEnabled(integrationDto.isDealBySelfLeadEnabled());
					integration.setLeadFormColumnLayout(integrationDto.getLeadFormColumnLayout());
					integration.setDealFormColumnLayout(integrationDto.getDealFormColumnLayout());

					if (integrationDto.getLeadPipelineId() > 0) {
						Pipeline leadPipeline = genericDAO.get(Pipeline.class, integrationDto.getLeadPipelineId());
						integration.setLeadPipelineId(leadPipeline);
					} else {
						integration.setLeadPipelineId(null);
					}
					if (integrationDto.getLeadPipelineStageId() > 0) {
						PipelineStage leadPipelineStage = genericDAO.get(PipelineStage.class,
								integrationDto.getLeadPipelineStageId());
						integration.setLeadPipelineStageId(leadPipelineStage);
					} else {
						integration.setLeadPipelineStageId(null);
					}
					if (integrationDto.getDealPipelineId() > 0) {
						Pipeline dealPipeline = genericDAO.get(Pipeline.class, integrationDto.getDealPipelineId());
						integration.setDealPipelineId(dealPipeline);
					} else {
						integration.setDealPipelineId(null);
					}
					if (integrationDto.getDealPipelineStageId() > 0) {
						PipelineStage dealPipelineStage = genericDAO.get(PipelineStage.class,
								integrationDto.getDealPipelineStageId());
						integration.setDealPipelineStageId(dealPipelineStage);
					} else {
						integration.setDealPipelineStageId(null);
					}
					responseMessage = SUCCESS;
					responseStatusCode = 200;
				}
			}
		}
		response.setStatusCode(responseStatusCode);
		response.setMessage(responseMessage);
		return response;
	}

	@Override
	public Map<String, Object> getVendorRegisterDealMap(Integer partnerCompanyId, Integer vendorCompanyId) {
		Map<String, Object> vendorRegisterDealMap = new HashMap<>();
		if (partnerCompanyId != null && partnerCompanyId > 0) {
			List<IntegrationSettingsDTO> vendorRegisterDealList = integrationDao
					.getVendorRegisterDealList(partnerCompanyId, vendorCompanyId);
			if (vendorRegisterDealList != null && !vendorRegisterDealList.isEmpty()) {
				for (IntegrationSettingsDTO vendorRegisterDeal : vendorRegisterDealList) {
					vendorRegisterDealMap.put(vendorRegisterDeal.getVendorCompanyId(),
							vendorRegisterDeal.isDealByPartner());
				}
			}
		}
		return vendorRegisterDealMap;
	}

	@Override
	public XtremandResponse getVendorRegisterDealValue(Integer loggedInUserId, String vendorCompanyProfile) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		boolean showRegisterDeal = true;
		Integer vendorCompanyId = 0;
		Map<String, Object> vendorRegisterDealMap = null;
		if (loggedInUserId != null && loggedInUserId > 0) {
			Integer partnerCompanyId = userDao.getCompanyIdByUserId(loggedInUserId);
			if (vendorCompanyProfile != null && !vendorCompanyProfile.isEmpty() && !vendorCompanyProfile.equals(" ")) {
				vendorCompanyId = userDao.getCompanyIdByProfileName(vendorCompanyProfile);
			}
			vendorRegisterDealMap = getVendorRegisterDealMap(partnerCompanyId, vendorCompanyId);
			if (vendorCompanyId != null && vendorCompanyId > 0
					&& vendorRegisterDealMap.get(vendorCompanyId + "") != null) {
				showRegisterDeal = Boolean.parseBoolean(vendorRegisterDealMap.get(vendorCompanyId + "") + "");
			} else if (vendorCompanyId == 0 && vendorRegisterDealMap.size() == 1) {
				showRegisterDeal = Boolean
						.parseBoolean(vendorRegisterDealMap.get(vendorRegisterDealMap.keySet().toArray()[0]) + "");
			}
			responseStatusCode = 200;
			responseMessage = SUCCESS;
		}
		response.setData(showRegisterDeal);
		response.setMap(vendorRegisterDealMap);
		response.setMessage(responseMessage);
		response.setStatusCode(responseStatusCode);
		return response;
	}

	private void setOrgAdminOrMarketingRegisterDealValue(Map<String, Object> vendorRegisterDealMap, Integer companyId) {
		IntegrationSettingsDTO integrationSettingsDto = integrationDao.isSelfDealByVendor(companyId);
		if (integrationSettingsDto != null) {
			vendorRegisterDealMap.put(companyId.toString(), integrationSettingsDto.isDealBySelfLead());
		}
	}

	@Override
	public XtremandResponse updateCRMSettingsNew(Integer userId, String integrationType,
			IntegrationDTO integrationDto) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;

		if (XamplifyUtils.isValidInteger(userId) && XamplifyUtils.isValidString(integrationType)
				&& integrationDto != null) {
			Integer companyId = userService.getCompanyIdByUserId(userId);

			if (XamplifyUtils.isValidInteger(companyId)) {
				if (integrationDto.getLeadDescription() == null || integrationDto.getLeadDescription().equals("")) {
					integrationDto.setLeadDescription(null);
				} else {
					integrationDto.setLeadDescription(
							"'" + XamplifyUtils.replaceSingleQuote(integrationDto.getLeadDescription()) + "'");
				}
				if (integrationDto.getDealDescription() == null || integrationDto.getDealDescription().equals("")) {
					integrationDto.setDealDescription(null);
				} else {
					integrationDto.setDealDescription(
							"'" + XamplifyUtils.replaceSingleQuote(integrationDto.getDealDescription()) + "'");
				}
				if (integrationDto.getLeadPipelineId() == 0) {
					integrationDto.setLeadPipelineId(null);
				}
				if (integrationDto.getLeadPipelineStageId() == 0) {
					integrationDto.setLeadPipelineStageId(null);
				}
				if (integrationDto.getDealPipelineId() == 0) {
					integrationDto.setDealPipelineId(null);
				}
				if (integrationDto.getDealPipelineStageId() == 0) {
					integrationDto.setDealPipelineStageId(null);
				}
				// XNFR-681
				if (integrationDto.getLeadTitle() == null || integrationDto.getLeadTitle().equals("")) {
					integrationDto.setLeadTitle(null);
				} else {
					integrationDto
							.setLeadTitle("'" + XamplifyUtils.replaceSingleQuote(integrationDto.getLeadTitle()) + "'");
				}

				if (integrationDto.getDealTitle() == null || integrationDto.getDealTitle().equals("")) {
					integrationDto.setDealTitle(null);
				} else {
					integrationDto
							.setDealTitle("'" + XamplifyUtils.replaceSingleQuote(integrationDto.getDealTitle()) + "'");
				}

				HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
				String sqlQuery = "update xt_integration set lead_description = " + integrationDto.getLeadDescription()
						+ ", deal_description = " + integrationDto.getDealDescription()
						+ ", show_lead_pipeline = :showLeadPipeline,"
						+ " show_lead_pipeline_stage = :showLeadPipelineStage, show_deal_pipeline = :showDealPipeline, show_deal_pipeline_stage = :showDealPipelineStage, lead_pipeline_id = "
						+ integrationDto.getLeadPipelineId() + "," + " deal_title = " + integrationDto.getDealTitle()
						+ ", lead_title = " + integrationDto.getLeadTitle() + "," + " lead_pipeline_stage_id = "
						+ integrationDto.getLeadPipelineStageId() + "" + ", deal_pipeline_id = "
						+ integrationDto.getDealPipelineId() + ", deal_pipeline_stage_id = "
						+ integrationDto.getDealPipelineStageId() + ", deal_by_partner = :dealByPartner, "
						+ " deal_by_vendor = :dealByVendor, deal_by_self_lead = :dealBySelfLead, lead_form_column_layout = '"
						+ integrationDto.getLeadFormColumnLayout() + "', deal_form_column_layout = '"
						+ integrationDto.getDealFormColumnLayout() + "', "
						+ " can_partner_edit_lead = :partnerEditLead, can_partner_delete_lead = :partnerDeleteLead"
						+ " where id = (select id from xt_integration where company_id = :companyId and cast(type as text) = :integrationType)";

				hibernateSQLQueryResultRequestDTO.setQueryString(sqlQuery);
				hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
						.add(new QueryParameterDTO("showLeadPipeline", integrationDto.isShowLeadPipeline()));
				hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
						.add(new QueryParameterDTO("showLeadPipelineStage", integrationDto.isShowLeadPipelineStage()));
				hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
						.add(new QueryParameterDTO("showDealPipeline", integrationDto.isShowDealPipeline()));
				hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
						.add(new QueryParameterDTO("showDealPipelineStage", integrationDto.isShowDealPipelineStage()));
				hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
						.add(new QueryParameterDTO("dealByPartner", integrationDto.isDealByPartnerEnabled()));
				hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
						.add(new QueryParameterDTO("dealByVendor", integrationDto.isDealByVendorEnabled()));
				hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
						.add(new QueryParameterDTO("dealBySelfLead", integrationDto.isDealBySelfLeadEnabled()));
				hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
						.add(new QueryParameterDTO("companyId", companyId));
				hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
						.add(new QueryParameterDTO("integrationType", integrationType));
				hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
						.add(new QueryParameterDTO("partnerEditLead", integrationDto.isPartnerEditLead()));
				hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
						.add(new QueryParameterDTO("partnerDeleteLead", integrationDto.isPartnerDeleteLead()));
				hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);

				responseStatusCode = 200;
				responseMessage = SUCCESS;

				if (integrationDto.isShowLeadPipelineStage() && integrationType.equals("salesforce")
						&& leadDao.checkPipelineStageMappedToCustomFiled(companyId)) {
					HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO2 = new HibernateSQLQueryResultRequestDTO();
					String sqlQuery2 = "UPDATE xt_form_label SET form_default_field_type = NULL WHERE form_default_field_type = 'PIPELINE_STAGE' AND form_id IN (SELECT id FROM xt_form WHERE form_type = 'SALES_FORCE_LEAD_CUSTOM_FORM' AND company_id = :companyId)";
					hibernateSQLQueryResultRequestDTO2.setQueryString(sqlQuery2);
					hibernateSQLQueryResultRequestDTO2.getQueryParameterDTOs()
							.add(new QueryParameterDTO("companyId", companyId));
					hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO2);
				}
			}
		}
		response.setStatusCode(responseStatusCode);
		response.setMessage(responseMessage);
		return response;
	}

	@Override
	public XtremandResponse createCustomDependentDropdowns() {
		XtremandResponse response = new XtremandResponse();
		String responseString = formDao.getDefaultDependentChoicesJson();

		createCustomDepedentDropwnsByJson(responseString);

		String responseMessage = SUCCESS;
		Integer responseStatusCode = 200;
		response.setStatusCode(responseStatusCode);
		response.setMessage(responseMessage);
		return response;
	}

	private void createCustomDepedentDropwnsByJson(String responseString) {
		JSONObject json = new JSONObject(responseString);
		if (json.has("fields")) {
			JSONArray jArr = json.getJSONArray("fields");
			ObjectMapper mapper = new ObjectMapper();
			OpportunityFormFieldsDTO parentLabel = null;
			try {
				List<OpportunityFormFieldsDTO> allFields = Arrays
						.asList(mapper.readValue(jArr.toString(), OpportunityFormFieldsDTO[].class));
				for (OpportunityFormFieldsDTO dependentField : allFields) {
					if (dependentField.getName().equals("Region_State__c")) {
						List<CustomRegion> regionChoices = new ArrayList<>();
						for (PicklistValues pickListValue : dependentField.getPicklistValues()) {
							CustomRegion regionChoice = new CustomRegion();
							regionChoice.setChoiceName(pickListValue.getLabel());
							regionChoice.setChoiceId(pickListValue.getLabel());
							regionChoices.add(regionChoice);
						}
						xamplifyUtilDao.saveAll(regionChoices, "Region Fields");
					} else if (dependentField.getName().equals("Country_P__c")) {
						if (StringUtils.isNotBlank(dependentField.getControllerName())) {
							parentLabel = getParentLabel(allFields, dependentField.getControllerName());
						}
						List<CustomCountry> countryChoices = new ArrayList<>();
						for (PicklistValues pickListValue : dependentField.getPicklistValues()) {
							CustomCountry countryChoice = new CustomCountry();
							String parentChoice = getParentChoice(parentLabel, pickListValue);
							countryChoice.setChoiceName(pickListValue.getLabel());
							countryChoice.setChoiceId(pickListValue.getLabel());
							if (parentChoice != null) {
								Integer parentChoiceId = formDao.getIdByParentChoice(parentChoice, parentLabel);
								CustomRegion regionChoice = genericDAO.get(CustomRegion.class, parentChoiceId);
								countryChoice.setRegionId(regionChoice);
							}
							countryChoices.add(countryChoice);
						}
						xamplifyUtilDao.saveAll(countryChoices, "Country Choices");
					} else if (dependentField.getName().equals("State_P__c")) {
						if (StringUtils.isNotBlank(dependentField.getControllerName())) {
							parentLabel = getParentLabel(allFields, dependentField.getControllerName());
						}
						List<CustomState> stateChoices = new ArrayList<>();
						for (PicklistValues pickListValue : dependentField.getPicklistValues()) {
							CustomState stateChoice = new CustomState();
							String parentChoice = getParentChoice(parentLabel, pickListValue);
							if (parentChoice != null) {
								Integer parentChoiceId = formDao.getIdByParentChoice(parentChoice, parentLabel);
								CustomCountry countryChoice = genericDAO.get(CustomCountry.class, parentChoiceId);
								stateChoice.setCountryId(countryChoice);
							}
							stateChoice.setChoiceName(pickListValue.getLabel());
							stateChoice.setChoiceId(pickListValue.getLabel());
							stateChoices.add(stateChoice);
						}
						xamplifyUtilDao.saveAll(stateChoices, "State Choices");
					} else if (dependentField.getName().equals("CityVersa__c")) {
						if (StringUtils.isNotBlank(dependentField.getControllerName())) {
							parentLabel = getParentLabel(allFields, dependentField.getControllerName());
						}
						List<CustomCity> cityChoices = new ArrayList<>();
						for (PicklistValues pickListValue : dependentField.getPicklistValues()) {
							CustomCity cityChoice = new CustomCity();
							String parentChoice = getParentChoice(parentLabel, pickListValue);
							if (parentChoice != null) {
								Integer parentChoiceId = formDao.getIdByParentChoice(parentChoice, parentLabel);
								CustomState stateChoice = genericDAO.get(CustomState.class, parentChoiceId);
								cityChoice.setStateId(stateChoice);
								;
							}
							cityChoice.setChoiceName(pickListValue.getLabel());
							cityChoice.setChoiceId(pickListValue.getLabel());
							cityChoices.add(cityChoice);
						}
						xamplifyUtilDao.saveAll(cityChoices, "City Choices");
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private String getParentChoice(OpportunityFormFieldsDTO parentLabel, PicklistValues pickListValue) {
		String parentChoice = null;
		byte[] bytes = Base64.getDecoder().decode(pickListValue.getValidFor());
		for (int k = 0; k < validForByteSize(bytes); k++) {
			if (validForTestBit(bytes, k)) {
				// if bit k is set, this entry is valid for the controlling entry at index k
				PicklistValues parentChoices = parentLabel.getPicklistValues().get(k);
				if (parentChoices != null) {
					parentChoice = parentChoices.getLabel();
				}
			}
		}
		return parentChoice;
	}

	public int validForByteSize(byte[] bytes) {
		return bytes.length * 8;
	}

	public boolean validForTestBit(byte[] bytes, int n) {
		return (bytes[n >> 3] & (0x80 >> n % 8)) != 0;
	}

	private OpportunityFormFieldsDTO getParentLabel(List<OpportunityFormFieldsDTO> formFields, String parentName) {
		OpportunityFormFieldsDTO parent = null;
		Optional<OpportunityFormFieldsDTO> parentFieldObj = formFields.stream()
				.filter(formField -> formField.getName().equals(parentName)).findFirst();
		if (parentFieldObj.isPresent()) {
			parent = parentFieldObj.get();
		}
		return parent;
	}

	@Override
	public XtremandResponse getCustomChoices(String parentLabelId, String parentChoice) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		List<FormChoiceDTO> dropDownChoiceDtos = new ArrayList<>();
		if (!StringUtils.isBlank(parentLabelId) && !StringUtils.isBlank(parentChoice)) {
			List<CustomChoicesDTO> customChoices = formDao.getCustomChoicesDataByParentChoice(parentLabelId,
					parentChoice);
			for (CustomChoicesDTO choice : customChoices) {
				FormChoiceDTO formChoiceDto = new FormChoiceDTO();
				formChoiceDto.setLabelId(choice.getChoiceName());
				formChoiceDto.setHiddenLabelId(choice.getChoiceName());
				formChoiceDto.setName(choice.getChoiceName());
				formChoiceDto.setItemName(choice.getChoiceName());
				dropDownChoiceDtos.add(formChoiceDto);
			}
			responseMessage = SUCCESS;
			responseStatusCode = 200;
		} else {
			responseMessage = INVALID_INPUT;
			responseStatusCode = 500;
		}
		response.setStatusCode(responseStatusCode);
		response.setMessage(responseMessage);
		response.setData(dropDownChoiceDtos);
		return response;
	}

	public XtremandResponse getActiveCrmType(Integer userId) {
		XtremandResponse response = new XtremandResponse();
		Integer companyId = userService.getCompanyIdByUserId(userId);
		if (XamplifyUtils.isValidInteger(companyId)) {
			PipelineRequestDTO pipelineRequestDTO = new PipelineRequestDTO();
			pipelineRequestDTO.setVendorCompanyId(companyId);
			String activeCRMType = pipelineDao.getActiveCRM(pipelineRequestDTO);
			response.setData(activeCRMType);
			response.setStatusCode(200);
		} else {
			response.setStatusCode(404);
			response.setMessage("No Data Found");
		}
		return response;
	}

	/*** XNFR-701 ***/
	public boolean isSalesforceCrmActiveByCompanyId(Integer companyId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String sqlQuery = "SELECT CASE WHEN EXISTS (SELECT 1 FROM xt_integration WHERE company_id = :companyId AND active = TRUE AND type = 'salesforce') THEN TRUE ELSE FALSE END AS result";
		hibernateSQLQueryResultRequestDTO.setQueryString(sqlQuery);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("companyId", companyId));
		return (boolean) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);

	}

	@Override
	public XtremandResponse getCustomFormLablesValues(CustomFormRequestDto customFormRequestDto) {
		XtremandResponse response = new XtremandResponse();
		Integer companyId = customFormRequestDto.getCompanyId();
		if (XamplifyUtils.isValidInteger(companyId)) {
			List<FormLabelDTO> customFormLabelsDtos = new ArrayList<>();
			customFormRequestDto.setCompanyId(companyId);
			response.setData(customFormLabelsDtos);
			response.setStatusCode(200);
		} else {
			response.setMessage(INVALID_INPUT);
			response.setStatusCode(500);
		}
		return response;
	}

	public String getActiveCRMTypeByUserId(Integer companyId) {
		String defaultActiveCRM = "xamplify";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String sqlQuery = "select cast(type as text) from xt_integration where company_id = :companyId and active";
		hibernateSQLQueryResultRequestDTO.setQueryString(sqlQuery);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("companyId", companyId));
		String activeCRM = (String) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
		activeCRM = XamplifyUtils.isValidString(activeCRM) ? activeCRM : defaultActiveCRM;
		return activeCRM;
	}

	@Override
	public XtremandResponse getFormLablesChoices(Integer formLabelId) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		List<FormChoiceDTO> formLabelChoiceDtos = new ArrayList<>();
		if (XamplifyUtils.isValidInteger(formLabelId)) {
			List<FormChoiceDTO> labelChoices = formDao.getFormLabelChoices(formLabelId);
			for (FormChoiceDTO choice : labelChoices) {
				formLabelChoiceDtos.add(choice);
			}
			responseMessage = SUCCESS;
			responseStatusCode = 200;
		} else {
			responseMessage = INVALID_INPUT;
			responseStatusCode = 500;
		}
		response.setStatusCode(responseStatusCode);
		response.setMessage(responseMessage);
		response.setData(formLabelChoiceDtos);
		return response;
	}

	@Override
	public XtremandResponse getActiveIntegrationTypeByCompanyName(String companyProfileName) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (!XamplifyUtils.isValidString(companyProfileName)) {
			response.setMessage(responseMessage);
			response.setStatusCode(responseStatusCode);
		}
		Integer vendorCompanyId = userDao.getCompanyIdByProfileName(companyProfileName);
		if (XamplifyUtils.isValidInteger(vendorCompanyId)) {
			String integrationType = integrationDao.getActiveIntegrationTypeByCompanyId(vendorCompanyId);
			response.setData(integrationType);
			response.setMessage(SUCCESS);
			response.setStatusCode(200);
		} else {
			response.setMessage(responseMessage);
			response.setStatusCode(responseStatusCode);
		}
		return response;
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public XtremandResponse getActiveCRMCustomForm(Integer vendorCompanyId, Integer opportunityId, Integer loggedInUserId,
			OpportunityType opportunityType) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (vendorCompanyId != null && vendorCompanyId > 0 && loggedInUserId != null && loggedInUserId > 0) {
			User loggedInUser = userService.loadUser(
					Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, loggedInUserId)),
					new FindLevel[] { FindLevel.COMPANY_PROFILE });
			if (loggedInUser != null) {
				CompanyProfile loggedInCompany = loggedInUser.getCompanyProfile();
				if (loggedInCompany != null && loggedInCompany.getId() != null) {
					FormDTO customFormDTO = new FormDTO();

					IntegrationDTO integrationDTO = getActiveCRMDetails(opportunityId, loggedInCompany, vendorCompanyId,
							opportunityType);
					if (integrationDTO != null) {
						Long hs = (long) 0;
							try {
								customFormDTO = getActiveCRMCustomForm(vendorCompanyId, opportunityId, loggedInUser, hs, opportunityType);
							} catch (IOException | ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							if (opportunityType == OpportunityType.DEAL) {
								customFormDTO.setDescription(integrationDTO.getDealDescription());
							}
							if (opportunityType == OpportunityType.LEAD) {
								customFormDTO.setDescription(integrationDTO.getLeadDescription());
							}
					}
					response.setData(customFormDTO);
					response.setMessage(SUCCESS);
					response.setStatusCode(200);
				} else {
					responseMessage = UNAUTHORIZED;
					responseStatusCode = 401;
				}
			} 
		}
		return response;
	}
	
	
	private IntegrationDTO getActiveCRMDetails(Integer opportunityId, CompanyProfile loggedInCompany,
			Integer vendorCompanyId, OpportunityType opportunityType) {
		CompanyProfile createdByCompany = loggedInCompany;
		Integer createdForCompanyId = vendorCompanyId;

		if (opportunityId != null && opportunityId > 0) {
			if (opportunityType == OpportunityType.DEAL) {
				Deal deal = genericDAO.get(Deal.class, opportunityId);
				if (deal != null) {
					createdByCompany = deal.getCreatedByCompany();
					createdForCompanyId = deal.getCreatedForCompany().getId();
				}
			} else if (opportunityType == OpportunityType.LEAD) {
				Lead lead = genericDAO.get(Lead.class, opportunityId);
				if (lead != null) {
					createdByCompany = lead.getCreatedByCompany();
					createdForCompanyId = lead.getCreatedForCompany().getId();
				}
			}
		}

		return getActiveCRMDetails(createdByCompany, createdForCompanyId);
	}
	
	private FormDTO getActiveCRMCustomForm(Integer companyId, Integer opportunityId, User loggedInUser,
			Long TypeId, OpportunityType opportunityType) throws IOException, ParseException {
		FormDTO formDTO = new FormDTO();
			formDTO = customFieldsService.getCustomForm(companyId,opportunityId, loggedInUser, TypeId,
					opportunityType);
		return formDTO;
	}


}
