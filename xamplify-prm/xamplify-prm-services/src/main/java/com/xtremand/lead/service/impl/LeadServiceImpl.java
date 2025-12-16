package com.xtremand.lead.service.impl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import com.xtremand.activity.dto.ContactOpportunityRequestDTO;
import com.xtremand.analytics.dao.TeamMemberAnalyticsDAO;
import com.xtremand.campaign.bom.DownloadDataInfo;
import com.xtremand.campaign.bom.DownloadDataInfo.DownloadItem;
import com.xtremand.campaign.bom.ModuleAccess;
import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.Criteria;
import com.xtremand.common.bom.Criteria.OPERATION_NAME;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.common.bom.Pagination;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.deal.bom.Deal;
import com.xtremand.deal.service.DealService;
import com.xtremand.form.bom.Form;
import com.xtremand.form.bom.FormDefaultFieldTypeEnum;
import com.xtremand.form.bom.FormLabel;
import com.xtremand.form.bom.FormLabelChoice;
import com.xtremand.form.bom.FormTypeEnum;
import com.xtremand.form.dao.FormDao;
import com.xtremand.form.dto.FormChoiceDTO;
import com.xtremand.form.dto.FormDTO;
import com.xtremand.form.dto.FormLabelDTO;
import com.xtremand.form.dto.FormLabelDTORow;
import com.xtremand.form.emailtemplate.dto.SendTestEmailDTO;
import com.xtremand.form.service.FormService;
import com.xtremand.form.submit.bom.FormSubmit;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.integration.bom.Integration;
import com.xtremand.integration.bom.Integration.IntegrationType;
import com.xtremand.integration.dao.IntegrationDao;
import com.xtremand.integration.dto.IntegrationSettingsDTO;
import com.xtremand.integration.service.IntegrationWrapperService;
import com.xtremand.lead.bom.Lead;
import com.xtremand.lead.bom.LeadApprovalStatusEnum;
import com.xtremand.lead.bom.LeadCustomField;
import com.xtremand.lead.bom.LeadField;
import com.xtremand.lead.bom.LeadStage;
import com.xtremand.lead.bom.Pipeline;
import com.xtremand.lead.bom.PipelineStage;
import com.xtremand.lead.bom.PipelineType;
import com.xtremand.lead.dao.LeadDAO;
import com.xtremand.lead.dto.LeadCountsResponseDTO;
import com.xtremand.lead.dto.LeadCustomFieldDto;
import com.xtremand.lead.dto.LeadDto;
import com.xtremand.lead.dto.LeadSyncDetailsDto;
import com.xtremand.lead.dto.PipelineDto;
import com.xtremand.lead.dto.PipelineStageDto;
import com.xtremand.lead.service.LeadService;
import com.xtremand.mail.service.AsyncComponent;
import com.xtremand.mail.service.MailService;
import com.xtremand.mail.service.MailService.EmailBuilder;
import com.xtremand.partnership.bom.Partnership;
import com.xtremand.partnership.dao.PartnershipDAO;
import com.xtremand.pipeline.dao.PipelineDAO;
import com.xtremand.salesforce.bom.SfCustomFieldsData;
import com.xtremand.salesforce.dto.SfCustomFieldsDataDTO;
import com.xtremand.sf.cf.data.dao.SfCustomFormDataDAO;
import com.xtremand.team.dao.TeamDao;
import com.xtremand.team.member.dto.RoleDisplayDTO;
import com.xtremand.team.member.dto.TeamMemberDTO;
import com.xtremand.user.bom.ModulesDisplayType;
import com.xtremand.user.bom.Role;
import com.xtremand.user.bom.User;
import com.xtremand.user.bom.User.UserStatus;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.user.service.UserService;
import com.xtremand.util.DateUtils;
import com.xtremand.util.GenerateRandomPassword;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.HibernateSQLQueryResultUtilDao;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.QueryParameterDTO;
import com.xtremand.util.dto.SearchableDropDownDTO;
import com.xtremand.util.dto.TeamMemberFilterDTO;
import com.xtremand.util.dto.XamplifyConstants;
import com.xtremand.util.service.CsvUtilService;
import com.xtremand.util.service.UtilService;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;
import com.xtremand.vendor.bom.VendorDTO;

@Service("LeadService")
@Transactional
public class LeadServiceImpl implements LeadService {
	private static final String BEARER = "Bearer ";

	private static final String LABEL_NAME = "labelName";

	private static final String ACCOUNT_SUB_TYPE = "account_sub_type";

	private static final String PARTNER_TYPE = "partner_type";

	private static final String USER_ID = "userId";

	private static final String TOTAL_RECORDS = "totalRecords";

	private static final String UNAUTHORIZED = "UnAuthorized";
	private static final String SUCCESS = "Success";
	private static final String FAILED = "Failed";
	private static final String INVALID_INPUT = "Invalid Input";
	private static final String PARTNER = "Partner";

	private static final String LEAD_AND_USER_PROFILE = " from xt_lead xl,xt_user_profile xup";

	private static final String USER_DETAILS_QUERY_PREFIX = "select distinct xup.user_id as \"id\",case when LENGTH(TRIM(concat(TRIM(xup.firstname), ' ', TRIM(xup.lastname))))>0  \r\n"
			+ "then TRIM(concat(TRIM(xup.email_id),' - ( ',TRIM(xup.firstname), ' ', TRIM(xup.lastname),' )')) else xup.email_id end as \"name\" ";

	private static final String USER_DETAILS_QUERY_STRING = USER_DETAILS_QUERY_PREFIX + LEAD_AND_USER_PROFILE
			+ " where xup.user_id  = xl.created_by \r\n";

	private static final String CREATED_BY_COMPANY_ID = XamplifyConstants.CREATED_BY_COMPANY_ID;
	private static final Logger LOGGER = LoggerFactory.getLogger(LeadServiceImpl.class);

	@Value("${server_path}")
	String serverPath;
	
	@Value("${xAmplify.base.url}")
	private String baseUrl;

	@Value("${spring.profiles.active}")
	private String profiles;

	@Autowired
	private UserService userService;

	@Autowired
	private UserDAO userDao;

	@Autowired
	private GenericDAO genericDAO;

	@Autowired
	private LeadDAO leadDAO;

	@Autowired
	private IntegrationDao integrationDao;

	@Autowired
	private IntegrationWrapperService integrationService;

	@Autowired
	private PartnershipDAO partnershipDAO;

	@Autowired
	private UtilService utilService;

	@Autowired
	private MailService mailService;

	@Autowired
	private CsvUtilService csvUtilService;

	@Autowired
	private TeamDao teamDao;

	@Autowired
	private UtilDao utilDao;

	@Autowired
	PipelineDAO pipelineDAO;

	/**** XNFR-426 *****/
	@Autowired
	@Lazy
	private AsyncComponent asyncComponent;

	@Autowired
	private HibernateSQLQueryResultUtilDao hibernateSQLQueryResultUtilDao;

	/**************/

	@Autowired
	private FormDao formDao;

	@Autowired
	private SfCustomFormDataDAO sfCustomFormDataDAO;

	@Autowired
	TeamMemberAnalyticsDAO teamMemberAnalyticsDao;

	@Value("${replace.company.logo}")
	private String replaceCompanyLogo;

	@Value("${email}")
	String fromEmail;
	
	@Autowired
	private FormService formService;
	
	@Autowired
	private DealService dealService;

	@Override
	public XtremandResponse saveLead(LeadDto leadDto) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		Integer responseLeadId = 0;
		if (validateSaveLeadRequest(leadDto)) {
			Integer loggedInUserId = leadDto.getUserId();
			CompanyProfile loggedInCompany = userService.getCompanyProfileByUser(loggedInUserId);
			if (loggedInCompany != null && isAuthorizedUser(loggedInUserId)) {
				Lead lead = createLead(leadDto, loggedInUserId, loggedInCompany);
				if (lead != null && lead.getId() != null) {
					lead.setLeadComment(leadDto.getLeadComment());
					fillIntegrationDetailsInLead(lead);
					pushLeadToActiveCRM(lead, leadDto);
					mailService.sendLeadAddedEmail(lead);
					responseMessage = SUCCESS;
					responseStatusCode = 200;
					responseLeadId = lead.getId();

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
		response.setData(responseLeadId);
		return response;
	}

	private void pushLeadToActiveCRM(Lead lead, LeadDto leadDto) {
		lead.setPushToCRMUserId(lead.getForCompanyUserId());
		lead.setPushToCRMStage(lead.getCreatedForPipelineStage());
		pushLeadToActiveCRM(lead, leadDto, lead.getCreatedForCompany().getId());
	}

	private void pushLeadToActiveCRM(Lead lead, LeadDto leadDTO, Integer companyId) {
	if (lead.getCreatedByCompany() != null) {
			saveOrUpdateSfCustomFieldsData(lead, leadDTO.getSfCustomFieldsDataDto(), IntegrationType.XAMPLIFY);
		}
	}

	public void saveOrUpdateSfCustomFieldsData(Lead lead, List<SfCustomFieldsDataDTO> sfCfData,
			IntegrationType activeCRMIntegrationType) {
		if (lead != null && sfCfData != null && !sfCfData.isEmpty()) {
			List<SfCustomFieldsData> sfCustomFieldsDataList = new ArrayList<>();
			Form form = getActiveCRMCustomForm(lead.getCreatedForCompany().getId());
			if (form != null) {
				for (SfCustomFieldsDataDTO sfCfDataDto : sfCfData) {
					FormLabel formLabel = formDao.getFormLabelByFormIdAndLabelId(form, sfCfDataDto.getSfCfLabelId());
					if (formLabel != null) {
						SfCustomFieldsData sfCustomFieldsData = sfCustomFormDataDAO
								.getSfCustomFieldDataByLeadIdAndLabelId(lead, formLabel);
						if (sfCustomFieldsData != null) {
							sfCustomFieldsData.setFormLabel(formLabel);
							if (!Objects.equals(sfCustomFieldsData.getValue(), sfCfDataDto.getValue())) {
								lead.setUpdated(true);
							}
							sfCustomFieldsData.setValue(sfCfDataDto.getValue());
							if (XamplifyUtils.isValidString(sfCfDataDto.getSelectedChoiceValue())) {
								sfCustomFieldsData.setSelectedChoiceValue(sfCfDataDto.getSelectedChoiceValue());
							} else {
								sfCustomFieldsData.setSelectedChoiceValue(null);
							}
							sfCustomFormDataDAO.updateSfCfData(sfCustomFieldsData);
						} else {
							lead.setUpdated(true);
							sfCustomFieldsData = new SfCustomFieldsData();
							sfCustomFieldsData.setLead(lead);
							sfCustomFieldsData.setFormLabel(formLabel);
							sfCustomFieldsData.setValue(sfCfDataDto.getValue());
							if (XamplifyUtils.isValidString(sfCfDataDto.getSelectedChoiceValue())) {
								sfCustomFieldsData.setSelectedChoiceValue(sfCfDataDto.getSelectedChoiceValue());
							} else {
								sfCustomFieldsData.setSelectedChoiceValue(null);
							}
							sfCustomFormDataDAO.saveSfCfData(sfCustomFieldsData);
						}
						sfCustomFieldsDataList.add(sfCustomFieldsData);
					}
				}
				lead.setSfCustomFieldsData(sfCustomFieldsDataList);
			}
		}
	}

	public void saveOrUpdateSfCustomFieldsData(Lead lead, List<SfCustomFieldsDataDTO> sfCfData, Form pushLeadCustomForm,
			Form comparingCompanyCustomForm) {
		if (lead != null && sfCfData != null && !sfCfData.isEmpty()) {
			List<SfCustomFieldsData> sfCustomFieldsDataList = new ArrayList<>();
			if (pushLeadCustomForm != null) {
				List<FormLabel> formLabels = pushLeadCustomForm.getFormLabels();
				for (SfCustomFieldsDataDTO sfCfDataDto : sfCfData) {
					FormLabel formLabel = formDao.getFormLabelByFormIdAndLabelId(pushLeadCustomForm,
							sfCfDataDto.getSfCfLabelId());
					if (formLabel != null) {
						saveOrUpdateSfCustomFieldsData(lead, sfCfDataDto, formLabel, sfCustomFieldsDataList, null);
					} else {
						if (comparingCompanyCustomForm != null) {
							FormLabel formLabelInOtherForm = formDao.getFormLabelByFormIdAndLabelId(
									comparingCompanyCustomForm, sfCfDataDto.getSfCfLabelId());
							if (formLabelInOtherForm != null) {
								FormLabel matchingFormLabel = utilService.getMatchedObject(formLabelInOtherForm,
										formLabels);
								if (matchingFormLabel != null) {
									saveOrUpdateSfCustomFieldsData(lead, sfCfDataDto, matchingFormLabel,
											sfCustomFieldsDataList, formLabelInOtherForm);
								}
							}
						}
					}
				}
				lead.getSfCustomFieldsData().addAll(sfCustomFieldsDataList);
			}
		}
	}

	private void saveOrUpdateSfCustomFieldsData(Lead lead, SfCustomFieldsDataDTO sfCfDataDto, FormLabel formLabel,
			List<SfCustomFieldsData> sfCustomFieldsDataList, FormLabel formLabelInOtherForm) {
		String value = sfCfDataDto.getValue();
		boolean saveOrUpdate = true;
		if (formLabelInOtherForm != null) {
			Map<String, Object> customFieldsDataMap = utilService.getSfCustomFieldsDataValue(sfCfDataDto.getValue(),
					formLabel, formLabelInOtherForm);
			value = (String) customFieldsDataMap.get("value");
			saveOrUpdate = (boolean) customFieldsDataMap.get("saveOrUpdate");
		}

		if (saveOrUpdate) {
			SfCustomFieldsData sfCustomFieldsData = sfCustomFormDataDAO.getSfCustomFieldDataByLeadIdAndLabelId(lead,
					formLabel);
			if (sfCustomFieldsData != null) {
				sfCustomFieldsData.setFormLabel(formLabel);
				if (!Objects.equals(sfCustomFieldsData.getValue(), sfCfDataDto.getValue())) {
					lead.setUpdated(true);
				}
				sfCustomFieldsData.setValue(value);
				sfCustomFormDataDAO.updateSfCfData(sfCustomFieldsData);
			} else {
				lead.setUpdated(true);
				sfCustomFieldsData = new SfCustomFieldsData();
				sfCustomFieldsData.setLead(lead);
				sfCustomFieldsData.setFormLabel(formLabel);
				sfCustomFieldsData.setValue(value);
				sfCustomFormDataDAO.saveSfCfData(sfCustomFieldsData);
			}
			sfCustomFieldsDataList.add(sfCustomFieldsData);
		}
	}

	private Form getActiveCRMCustomForm(Integer companyId) {
		Form form = null;
		if (companyId != null) {
			Integer formId = null;
			formId = formDao.getSfCustomFormIdByCompanyIdAndFormType(companyId, FormTypeEnum.CRM_LEAD_CUSTOM_FORM);
			if (formId != null && formId > 0) {
				form = formDao.getById(formId);
			}
		}
		return form;
	}

	private void fillIntegrationDetailsInLead(Lead lead) {
		List<User> forUserIds = partnershipDAO.getOwners(lead.getCreatedForCompany().getId());
		if (forUserIds != null && !forUserIds.isEmpty()) {
			lead.setForCompanyUserId(forUserIds.get(0).getUserId());
		}

		fillCreatedByDetails(lead);
	}

	private Lead createLead(LeadDto leadDto, Integer loggedInUserId, CompanyProfile loggedInCompany) {
		Lead lead = new Lead();
		setAssociatedUser(lead, leadDto, loggedInUserId);

		if (leadDto.getCreatedForCompanyId() != null && leadDto.getCreatedForCompanyId() > 0) {
			CompanyProfile forCompany = genericDAO.get(CompanyProfile.class, leadDto.getCreatedForCompanyId());
			if (forCompany != null) {
				lead.setCreatedForCompany(forCompany);
			}
		}

		PipelineStage pipelineStage = genericDAO.get(PipelineStage.class, leadDto.getPipelineStageId());
		if (pipelineStage != null) {
			lead.setCurrentStage(pipelineStage);
			lead.setPipeline(pipelineStage.getPipeline());
		}

		lead.setCity(leadDto.getCity());
		lead.setCompany(leadDto.getCompany());
		if (!"Select Country".equals(leadDto.getCountry())) {
			lead.setCountry(leadDto.getCountry());
		}
		lead.setCreatedBy(loggedInUserId);
		lead.setCreatedByCompany(loggedInCompany);
		lead.setEmail(leadDto.getEmail());
		lead.setFirstName(leadDto.getFirstName());
		lead.setLastName(leadDto.getLastName());
		lead.setPhone(leadDto.getPhone());
		lead.setPostalCode(leadDto.getPostalCode());
		if (!"Select State".equals(leadDto.getState())) {
			lead.setState(leadDto.getState());
		}
		lead.setStreet(leadDto.getStreet());
		lead.setWebsite(leadDto.getWebsite());
		lead.setTitle(leadDto.getTitle());
		if (!"Select Industry".equals(leadDto.getIndustry())) {
			lead.setIndustry(leadDto.getIndustry());
		}
		// XNFR-613
		if (!"Select Region".equals(leadDto.getRegion())) {
			lead.setRegion(leadDto.getRegion());
		}
		lead.initialiseCommonFields(true, loggedInUserId);
		if (leadDto.getHalopsaTicketTypeId() != null && Long.parseLong(leadDto.getHalopsaTicketTypeId()) > 0L) {
			lead.setHalopsaTicketTypeId(Long.parseLong(leadDto.getHalopsaTicketTypeId()));
		} else {
			lead.setHalopsaTicketTypeId(null);
		}

		/**** XNFR-483 ****/
		if (lead.getCreatedForCompany().getId() != null && lead.getCreatedForCompany().getId() > 0) {
			Boolean isVendorEnabledLeadApprovalRejectionFeature = (Boolean) userDao
					.getLeadApprovalStatus(lead.getCreatedForCompany().getId());
			if (!isVendorEnabledLeadApprovalRejectionFeature) {
				lead.setLeadApprovalStatusType(LeadApprovalStatusEnum.APPROVED);
				lead.setApprovalStatusUpdatedTime(new Date());
			} else if (lead.getCreatedByCompany().getId() != null && lead.getCreatedByCompany().getId() > 0
					&& lead.getCreatedForCompany().getId() == lead.getCreatedByCompany().getId()) {
				lead.setLeadApprovalStatusType(LeadApprovalStatusEnum.APPROVED);
				lead.setApprovalStatusUpdatedTime(new Date());
			}
		}
		/** XNFR-521 **/
		Pipeline createdForPipeline = genericDAO.get(Pipeline.class, leadDto.getCreatedForPipelineId());
		PipelineStage createdForpipelineStage = genericDAO.get(PipelineStage.class,
				leadDto.getCreatedForPipelineStageId());
		if (leadDto.getCreatedForPipelineId() != null && leadDto.getCreatedForPipelineStageId() != null) {
			lead.setCreatedForPipeline(createdForPipeline);
			lead.setCreatedForPipelineStage(createdForpipelineStage);
		}
		Pipeline createdByPipeline = genericDAO.get(Pipeline.class, leadDto.getCreatedByPipelineId());
		PipelineStage createdBypipelineStage = genericDAO.get(PipelineStage.class,
				leadDto.getCreatedByPipelineStageId());
		if (leadDto.getCreatedByPipelineId() != null && leadDto.getCreatedByPipelineStageId() != null) {
			lead.setCreatedByPipeline(createdByPipeline);
			lead.setCreatedByPipelineStage(createdBypipelineStage);
		}
		if (XamplifyUtils.isValidInteger(leadDto.getFormSubmitId())) {
			FormSubmit formSubmit = new FormSubmit();
			formSubmit.setId(leadDto.getFormSubmitId());
			lead.setFormSubmit(formSubmit);
		}
		genericDAO.save(lead);

		if (pipelineStage != null) {
			LeadStage leadStage = new LeadStage();
			leadStage.setLead(lead);
			leadStage.setPipelineStage(pipelineStage);
			leadStage.setCreatedBy(loggedInUserId);
			leadStage.initialiseCommonFields(true, loggedInUserId);
			genericDAO.save(leadStage);
		}
		return lead;
	}

	private void setAssociatedUser(Lead lead, LeadDto leadDto, Integer loggedInUserId) {
		if (leadDto.getAssociatedUserId() != null && leadDto.getAssociatedUserId() > 0) {
			User associatedUser = userService.loadUser(
					Arrays.asList(new Criteria(USER_ID, OPERATION_NAME.eq, leadDto.getAssociatedUserId())),
					new FindLevel[] { FindLevel.COMPANY_PROFILE });
			lead.setAssociatedUser(associatedUser);
		} else {
			User exUser = loadUser(
					Arrays.asList(new Criteria("emailId", OPERATION_NAME.eq, leadDto.getEmail().toLowerCase())),
					new FindLevel[] { FindLevel.SHALLOW });
			if (exUser != null) {
				lead.setAssociatedUser(exUser);
			} else {
				User user = new User();
				user.setEmailId(leadDto.getEmail().toLowerCase());
				user.setUserStatus(UserStatus.UNAPPROVED);
				user.setModulesDisplayType(ModulesDisplayType.LIST);
				user.getRoles().add(Role.USER_ROLE);
				user.initialiseCommonFields(true, loggedInUserId);
				user.setEmailValid(true);
				user.setEmailValidationInd(true);
				user.setEmailCategory("catch-all");
				genericDAO.save(user);
				if (user.getAlias() == null) {
					GenerateRandomPassword randomPassword = new GenerateRandomPassword();
					user.setAlias(randomPassword.getPassword());
				}
				lead.setAssociatedUser(user);
			}
		}
	}

	public User loadUser(List<Criteria> criterias, FindLevel[] levels) {
		User user = null;
		Collection<User> users = userDao.find(criterias, levels);
		if (users != null && !users.isEmpty()) {
			user = users.iterator().next();
		}
		return user;
	}

	private boolean validateSaveLeadRequest(LeadDto leadDto) {
		boolean valid = false;
		boolean isValidLeadDto = leadDto != null;
		boolean isValidLastName = isValidLeadDto && XamplifyUtils.isValidString(leadDto.getLastName());
		boolean isValidEmail = isValidLeadDto && XamplifyUtils.isValidString(leadDto.getEmail());
		boolean isValidCompany = isValidLeadDto && XamplifyUtils.isValidString(leadDto.getCompany());
		boolean isValidUserId = isValidLeadDto && XamplifyUtils.isValidInteger(leadDto.getUserId());
		boolean isValidCreatedForCompanyId = isValidLeadDto && leadDto.getCampaignId() == null
				&& leadDto.getCreatedForCompanyId() != null && leadDto.getCreatedForCompanyId() > 0;
		boolean isValidCampaignId = isValidLeadDto && leadDto.getCampaignId() != null && leadDto.getCampaignId() > 0;
		boolean isValidCreatedForCompanyIdOrValidCampaignId = isValidCreatedForCompanyId || isValidCampaignId;
		boolean isValidCreatedForPipeLineId = isValidLeadDto
				&& XamplifyUtils.isValidInteger(leadDto.getCreatedForPipelineId());
		boolean isValidCreatedForPipeLineStageId = isValidLeadDto
				&& XamplifyUtils.isValidInteger(leadDto.getCreatedForPipelineStageId());
		if (isValidLeadDto && isValidLastName && isValidEmail && isValidCompany && isValidUserId
				&& isValidCreatedForCompanyIdOrValidCampaignId && isValidCreatedForPipeLineId
				&& isValidCreatedForPipeLineStageId) {
			valid = true;
		}
		return valid;
	}

	private boolean validateEditLeadRequest(LeadDto leadDto) {
		boolean valid = false;
		// leadDto.getUserId() is the loggedIn User ID
		if (leadDto != null && leadDto.getId() != null && leadDto.getId() > 0
				&& !StringUtils.isBlank(leadDto.getLastName()) && !StringUtils.isBlank(leadDto.getEmail())
				&& !StringUtils.isBlank(leadDto.getCompany()) && leadDto.getUserId() != null && leadDto.getUserId() > 0
				&& leadDto.getPipelineStageId() != null && leadDto.getPipelineStageId() > 0) {
			valid = true;
		}
		return valid;
	}

	private boolean isAuthorizedUser(Integer userId) {
		boolean isTeamMember = teamDao.isTeamMember(userId);
		if (isTeamMember) {
			Integer superiorId = teamDao.getOrgAdminIdByTeamMemberId(userId);
			List<Integer> roles = userDao.getRoleIdsByUserId(superiorId);
			return roles.contains(Role.COMPANY_PARTNER.getRoleId());
		} else {
			List<Integer> roles = userDao.getRoleIdsByUserId(userId);
			return roles.contains(Role.COMPANY_PARTNER.getRoleId());
		}
	}

	private void fillCreatedByDetails(Lead lead) {

		User createdUser = userService.loadUser(
				Arrays.asList(new Criteria(USER_ID, OPERATION_NAME.eq, lead.getCreatedBy())),
				new FindLevel[] { FindLevel.COMPANY_PROFILE });
		if (createdUser != null) {
			String name = "";
			if (createdUser.getFirstName() != null) {
				name = createdUser.getFirstName() + " ";
			}
			if (createdUser.getLastName() != null) {
				name = name + createdUser.getLastName();
			}

			lead.setCreatedByName(name);
			lead.setCreatedByEmail(createdUser.getEmailId());
			if (createdUser.getCompanyProfile() != null) {
				lead.setCreatedByCompanyName(createdUser.getCompanyProfile().getCompanyName());
			}
		}
	}

	private void fillCreatedByDetailsInDTO(LeadDto leadDto, Lead lead) {
		User createdUser = userService.loadUser(
				Arrays.asList(new Criteria(USER_ID, OPERATION_NAME.eq, lead.getCreatedBy())),
				new FindLevel[] { FindLevel.COMPANY_PROFILE });
		if (createdUser != null) {
			String name = "";
			if (createdUser.getFirstName() != null) {
				name = createdUser.getFirstName() + " ";
			}
			if (createdUser.getLastName() != null) {
				name = name + createdUser.getLastName();
			}

			leadDto.setCreatedByName(name);
			leadDto.setCreatedByEmail(createdUser.getEmailId());
		}
	}

	@Override
	public XtremandResponse updateLead(LeadDto leadDto) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (validateEditLeadRequest(leadDto)) {
			Integer loggedInUserId = leadDto.getUserId();
			CompanyProfile loggedInCompany = userService.getCompanyProfileByUser(loggedInUserId);
			Lead lead = genericDAO.get(Lead.class, leadDto.getId());
			if (lead != null) {
				if (loggedInCompany != null && canUpdateLead(lead, loggedInUserId) && isAuthorizedUser(loggedInUserId)
						&& validateLead(lead)) {
					lead = updateLead(lead, leadDto, loggedInUserId, loggedInCompany);
					if (lead != null && lead.getId() != null) {
						lead.setLeadComment(leadDto.getLeadComment());
						fillIntegrationDetailsInLead(lead);
						pushLeadToActiveCRM(lead, leadDto);
						if (lead.isUpdated()) {
							mailService.sendLeadUpdatedEmail(lead);
						}
						responseMessage = SUCCESS;
						responseStatusCode = 200;
					}
				} else {
					responseMessage = UNAUTHORIZED;
					responseStatusCode = 401;
				}
			} else {
				responseMessage = "Lead not Found";
				responseStatusCode = 404;
			}
		} else {
			responseMessage = INVALID_INPUT;
			responseStatusCode = 500;
		}
		response.setMessage(responseMessage);
		response.setStatusCode(responseStatusCode);
		return response;
	}

	private Lead updateLead(Lead lead, LeadDto leadDto, Integer loggedInUserId, CompanyProfile loggedInCompany) {
		if (!Objects.equals(lead.getCity(), leadDto.getCity())) {
			lead.setUpdated(true);
			lead.setCity(leadDto.getCity());
		}
		if (!Objects.equals(lead.getCompany(), leadDto.getCompany())) {
			lead.setUpdated(true);
			lead.setCompany(leadDto.getCompany());
		}

		if (!"Select Country".equals(leadDto.getCountry())) {
			lead.setUpdated(true);
			lead.setCountry(leadDto.getCountry());
		} else {
			lead.setCountry(null);
		}

		if (!Objects.equals(lead.getEmail(), leadDto.getEmail())) {
			lead.setUpdated(true);
			lead.setEmail(leadDto.getEmail());
		}
		if (!Objects.equals(lead.getFirstName(), leadDto.getFirstName())) {
			lead.setUpdated(true);
			lead.setFirstName(leadDto.getFirstName());
		}
		if (!Objects.equals(lead.getLastName(), leadDto.getLastName())) {
			lead.setUpdated(true);
			lead.setLastName(leadDto.getLastName());
		}
		if (!Objects.equals(lead.getPhone(), leadDto.getPhone())) {
			lead.setUpdated(true);
			lead.setPhone(leadDto.getPhone());
		}
		if (!Objects.equals(lead.getPostalCode(), leadDto.getPostalCode())) {
			lead.setUpdated(true);
			lead.setPostalCode(leadDto.getPostalCode());
		}
		if (!"Select State".equals(leadDto.getState())) {
			lead.setUpdated(true);
			lead.setState(leadDto.getState());
		} else {
			lead.setState(null);
		}
		if (!Objects.equals(lead.getStreet(), leadDto.getStreet())) {
			lead.setUpdated(true);
			lead.setStreet(leadDto.getStreet());
		}
		if (!Objects.equals(lead.getWebsite(), leadDto.getWebsite())) {
			lead.setUpdated(true);
			lead.setWebsite(leadDto.getWebsite());
		}
		lead.initialiseCommonFields(false, loggedInUserId);
		if (leadDto.getHalopsaTicketTypeId() != null && Long.parseLong(leadDto.getHalopsaTicketTypeId()) > 0) {
			lead.setHalopsaTicketTypeId(Long.parseLong(leadDto.getHalopsaTicketTypeId()));
		} else {
			lead.setHalopsaTicketTypeId(null);
		}

		/** XNFR-521 **/
		if (leadDto.getCreatedForPipelineId() != null
				&& (leadDto.getCreatedForPipelineStageId() != null && leadDto.getCreatedForPipelineStageId() > 0)) {
			Pipeline createdForPipeline = genericDAO.get(Pipeline.class, leadDto.getCreatedForPipelineId());
			PipelineStage createdForpipelineStage = genericDAO.get(PipelineStage.class,
					leadDto.getCreatedForPipelineStageId());
			lead.setCreatedForPipeline(createdForPipeline);
			lead.setCreatedForPipelineStage(createdForpipelineStage);
		}

		if (leadDto.getCreatedByPipelineId() != null
				&& (leadDto.getCreatedByPipelineStageId() != null && leadDto.getCreatedByPipelineStageId() > 0)) {
			Pipeline createdByPipeline = genericDAO.get(Pipeline.class, leadDto.getCreatedByPipelineId());
			PipelineStage createdBypipelineStage = genericDAO.get(PipelineStage.class,
					leadDto.getCreatedByPipelineStageId());
			lead.setCreatedByPipeline(createdByPipeline);
			lead.setCreatedByPipelineStage(createdBypipelineStage);
		}

		if (!leadDto.getPipelineStageId().equals(lead.getCurrentStage().getId())) {
			lead.setUpdated(true);
			PipelineStage pipelineStage = genericDAO.get(PipelineStage.class, leadDto.getPipelineStageId());
			if (pipelineStage != null) {
				lead.setPipeline(pipelineStage.getPipeline());
				lead.setCurrentStage(pipelineStage);
				LeadStage leadStage = new LeadStage();
				leadStage.setLead(lead);
				leadStage.setPipelineStage(pipelineStage);
				leadStage.setCreatedBy(loggedInUserId);
				leadStage.initialiseCommonFields(true, loggedInUserId);
				genericDAO.save(leadStage);
			}
		}

		if (!"Select Industry".equals(leadDto.getIndustry())) {
			lead.setUpdated(true);
			lead.setIndustry(leadDto.getIndustry());
		}

		if (!Objects.equals(lead.getTitle(), leadDto.getTitle())) {
			lead.setUpdated(true);
			lead.setTitle(leadDto.getTitle());
		}

		if (!"Select Region".equals(leadDto.getRegion())) {
			lead.setUpdated(true);
			lead.setRegion(leadDto.getRegion());
		} else {
			lead.setRegion(null);
		}

		return lead;
	}

	private Boolean canUpdateLead(Lead lead, Integer loggedInUserId) {
		Boolean canUpdate = false;
		if (lead != null && loggedInUserId != null) {
			List<Integer> superiorIds = partnershipDAO.getSuperiorIds(lead.getCreatedByCompany().getId());
			if (superiorIds.contains(loggedInUserId) || lead.getCreatedBy().intValue() == loggedInUserId.intValue()) {
				canUpdate = true;
			}
		}
		return canUpdate;
	}

	private Boolean canRegisterDeal(Lead lead, Integer loggedInUserId) {
		Boolean canRegisterDeal = false;
		if (lead != null && loggedInUserId != null) {
			CompanyProfile loggedIncompany = userService.getCompanyProfileByUser(loggedInUserId);
			if (lead.getCreatedByCompany().getId() == loggedIncompany.getId() && lead.getAssociatedDeal() == null) {
				canRegisterDeal = true;
			}
		}
		return canRegisterDeal;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getLeadsForPartner(Pagination pagination) {
		Map<String, Object> resultMap = new HashMap<>();
		if (pagination != null && pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer companyId = userService.getCompanyIdByUserId(pagination.getUserId());
			if (companyId != null) {
				pagination.setCompanyId(companyId);
				setVendorCompanyId(pagination);
				utilService.setDateFilters(pagination);
				if (Boolean.TRUE.equals(pagination.getIsCompanyJourney())) {
					Map<String, Object> map = leadDAO.fetchLeadsForCompanyJourney(pagination);
					resultMap.put(TOTAL_RECORDS, map.get(TOTAL_RECORDS));
					resultMap.put("data", map.get("list"));
				} else {
					resultMap = leadDAO.getLeadsForPartner(pagination);
					if (resultMap != null && !resultMap.isEmpty() && resultMap.get("data") != null) {
						List<Lead> leads = (List<Lead>) resultMap.get("data");
						resultMap.put("data", getLeadDtoList(leads, pagination.getUserId()));
					}
				}
			}
		}
		return resultMap;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getLeadsForPartnerForCSV(Pagination pagination) {
		Map<String, Object> resultMap = new HashMap<>();
		if (pagination != null && pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer companyId = userService.getCompanyIdByUserId(pagination.getUserId());
			if (companyId != null) {
				pagination.setCompanyId(companyId);
				setVendorCompanyId(pagination);
				utilService.setDateFilters(pagination);
				resultMap = leadDAO.getLeadsForPartner(pagination);
				if (resultMap != null && !resultMap.isEmpty() && resultMap.get("data") != null) {
					List<Lead> leads = (List<Lead>) resultMap.get("data");
					resultMap.put("data", getLeadDtoListCSV(leads, pagination.getUserId()));
				}
			}
		}
		return resultMap;
	}

	private void setVendorCompanyId(Pagination pagination) {
		VanityUrlDetailsDTO vanityUrlDetailsDTO = utilService.getVanityUrlFilteredData(pagination.getUserId(),
				pagination.isVanityUrlFilter(), utilDao.getPrmCompanyProfileName());
		if (vanityUrlDetailsDTO.isPartnerLoggedInThroughVanityUrl()
				|| (XamplifyUtils.isValidInteger(pagination.getContactId())
						&& !pagination.isShowLeadsForAttachingLead())) {
			pagination.setVendorCompanyId(vanityUrlDetailsDTO.getVendorCompanyId());
		} else {
			Integer loginAsUserId = pagination.getLoginAsUserId();
			boolean isLoginAsPartner = XamplifyUtils.isLoginAsPartner(loginAsUserId);
			if (isLoginAsPartner) {
				pagination.setVendorCompanyId(userDao.getCompanyIdByUserId(loginAsUserId));
			}
		}
	}

	private List<LeadDto> getLeadDtoList(List<Lead> leads, Integer loggedInUserId) {
		List<LeadDto> leadDtoList = new ArrayList<>();
		if (leads != null && !leads.isEmpty()) {
			for (Lead lead : leads) {
				LeadDto leadDto = getLeadDto(lead, loggedInUserId);
				if (leadDto != null) {
					leadDtoList.add(leadDto);
				}
			}
		}
		return leadDtoList;
	}

	private List<LeadDto> getLeadDtoListCSV(List<Lead> leads, Integer loggedInUserId) {
		List<LeadDto> leadDtoList = new ArrayList<>();
		if (leads != null && !leads.isEmpty()) {
			for (Lead lead : leads) {
				LeadDto leadDto = getLeadDtoCSV(lead, loggedInUserId);
				if (leadDto != null) {
					leadDtoList.add(leadDto);
				}
			}
		}
		return leadDtoList;
	}

	private LeadDto getLeadDto(Lead lead, Integer loggedInUserId) {
		LeadDto leadDto = null;
		if (lead != null) {
			leadDto = new LeadDto();
			leadDto.setCity(lead.getCity());
			leadDto.setCompany(lead.getCompany());
			leadDto.setCountry(lead.getCountry());
			leadDto.setEmail(lead.getEmail());
			leadDto.setFirstName(lead.getFirstName());
			leadDto.setId(lead.getId());
			leadDto.setLastName(lead.getLastName());
			leadDto.setPhone(lead.getPhone());
			leadDto.setPostalCode(lead.getPostalCode());
			leadDto.setState(lead.getState());
			leadDto.setStreet(lead.getStreet());
			leadDto.setWebsite(lead.getWebsite());
			leadDto.setPipelineId(lead.getPipeline().getId());
			PipelineDto pipeline = getPipelineDto(lead.getPipeline(), loggedInUserId);
			leadDto.setPipeline(pipeline);
			leadDto.setPipelineStageId(lead.getCurrentStage().getId());
			leadDto.setCurrentStageId(lead.getCurrentStage().getId());
			leadDto.setCurrentStageName(lead.getCurrentStage().getStageName());
			leadDto.setCurrentStagePrivate(lead.getCurrentStage().isPrivate());
			Boolean canUpdate = canUpdateLead(lead, loggedInUserId);
			leadDto.setCanUpdate(canUpdate);
			leadDto.setCanDelete(canUpdate);
			leadDto.setCanRegisterDeal(canRegisterDeal(lead, loggedInUserId));
			leadDto.setCreatedTime(DateUtils.getUtcString(lead.getCreatedTime()));
			leadDto.setCreatedDateString(DateUtils.convertDateToString(lead.getCreatedTime()));
			leadDto.setCreatedForCompanyName(lead.getCreatedForCompany().getCompanyName());
			leadDto.setCreatedByCompanyName(lead.getCreatedByCompany().getCompanyName());
			leadDto.setCreatedByCompanyId(lead.getCreatedByCompany().getId());
			leadDto.setCreatedForCompanyId(lead.getCreatedForCompany().getId());
			/***** XNFR-426 ******/
			leadDto.setLeadApprovalStatusType(lead.getLeadApprovalStatusType());
			leadDto.setLeadApprovalOrRejection(lead.getCreatedForCompany().isLeadApprovalOrRejection());
			leadDto.setApprovalStatusComment(lead.getApprovalStatusComment());
			leadDto.setApprovalStatusUpdatedTime(DateUtils.getUtcString(lead.getApprovalStatusUpdatedTime()));
			if (lead.getAssociatedDeal() != null) {
				leadDto.setAssociatedDealId(lead.getAssociatedDeal().getId());
			}
			if (lead.getCreatedForCompany().getId() == lead.getCreatedByCompany().getId()) {
				leadDto.setSelfLead(true);
			}
			/*** XNFR-505 ***/
			if (lead.getCreatedForCompany().getId() != null && lead.getCreatedByCompany().getId() != null) {
				Integer partnerCompanyId = lead.getCreatedByCompany().getId();
				Integer vendorCompanyId = lead.getCreatedForCompany().getId();
				Partnership partnership = partnershipDAO.checkPartnership(vendorCompanyId, partnerCompanyId);
				String partnerStatus = partnershipDAO
						.findPartnerShipStatusByPartnerCompanyIdAndVendorCompanyId(partnerCompanyId, vendorCompanyId);
				leadDto.setPartnerStatus(partnerStatus);
				if (partnership != null || leadDto.isSelfLead()) {
					leadDto.setEnableRegisterDealButton(true);
				}
			}

			/** XNFR-521 **/
			if (lead.getCreatedForPipeline() != null && lead.getCreatedForPipelineStage() != null) {
				leadDto.setCreatedForPipelineId(lead.getCreatedForPipeline().getId());
				leadDto.setCreatedForPipelineStageId(lead.getCreatedForPipelineStage().getId());
			}

			if (lead.getCreatedByPipeline() != null && lead.getCreatedByPipelineStage() != null) {
				leadDto.setCreatedByPipelineId(lead.getCreatedByPipeline().getId());
				leadDto.setCreatedByPipelineStageId(lead.getCreatedByPipelineStage().getId());
			}

			if (lead.getHalopsaTicketTypeId() != null && lead.getHalopsaTicketTypeId() > 0) {
				leadDto.setHalopsaTicketTypeId(lead.getHalopsaTicketTypeId() + "");
			}

			leadDto.setTitle(lead.getTitle());
			leadDto.setIndustry(lead.getIndustry());
			leadDto.setRegion(lead.getRegion());
			leadDto.setReferenceId(lead.getReferenceId());
			/**** Added On 28/07/2024 ****/
			setShowRegisterDealPropertyByCreatedForCompanyId(leadDto);
			setPartnerDetails(leadDto, lead);
			fillCreatedByDetailsInDTO(leadDto, lead);
			fillUnReadChatCounts(leadDto, lead, loggedInUserId);
			leadDto.setActiveCRM(integrationDao.hasActiveCRMIntegration(lead.getCreatedForCompany().getId()));
		}
		return leadDto;
	}

	private void setPartnerDetails(LeadDto leadDto, Lead lead) {
		if (lead.getCreatedByCompany() != null && lead.getCreatedForCompany() != null) {
			Integer partnerId = lead.getCreatedByCompany().getId();
			Integer vendorId = lead.getCreatedForCompany().getId();
			leadDAO.getUserUserDetailsForLead(leadDto, partnerId, vendorId);
		}
	}

	private LeadDto getLeadDtoCSV(Lead lead, Integer loggedInUserId) {
		LeadDto leadDto = null;
		if (lead != null) {
			leadDto = new LeadDto();
			leadDto.setCity(lead.getCity());
			leadDto.setCompany(lead.getCompany());
			leadDto.setCountry(lead.getCountry());
			leadDto.setEmail(lead.getEmail());
			leadDto.setFirstName(lead.getFirstName());
			leadDto.setId(lead.getId());
			leadDto.setLastName(lead.getLastName());
			leadDto.setPhone(lead.getPhone());
			leadDto.setPostalCode(lead.getPostalCode());
			leadDto.setState(lead.getState());
			leadDto.setStreet(lead.getStreet());
			leadDto.setWebsite(lead.getWebsite());
			leadDto.setPipelineId(lead.getPipeline().getId());
			PipelineDto pipeline = getPipelineDto(lead.getPipeline(), loggedInUserId);
			leadDto.setPipeline(pipeline);
			leadDto.setPipelineStageId(lead.getCurrentStage().getId());
			leadDto.setCurrentStageId(lead.getCurrentStage().getId());
			leadDto.setCurrentStageName(lead.getCurrentStage().getStageName());
			leadDto.setCurrentStagePrivate(lead.getCurrentStage().isPrivate());
			Boolean canUpdate = canUpdateLead(lead, loggedInUserId);
			leadDto.setCanUpdate(canUpdate);
			leadDto.setCanDelete(canUpdate);
			leadDto.setCanRegisterDeal(canRegisterDeal(lead, loggedInUserId));
			leadDto.setCreatedTime(DateUtils.getUtcString(lead.getCreatedTime()));
			leadDto.setCreatedDateString(DateUtils.convertDateToString(lead.getCreatedTime()));
			leadDto.setCreatedForCompanyName(lead.getCreatedForCompany().getCompanyName());
			leadDto.setCreatedByCompanyName(lead.getCreatedByCompany().getCompanyName());
			leadDto.setCreatedByCompanyId(lead.getCreatedByCompany().getId());
			leadDto.setCreatedForCompanyId(lead.getCreatedForCompany().getId());
			/***** XNFR-426 ******/
			leadDto.setLeadApprovalStatusType(lead.getLeadApprovalStatusType());
			leadDto.setLeadApprovalOrRejection(lead.getCreatedForCompany().isLeadApprovalOrRejection());
			leadDto.setApprovalStatusComment(lead.getApprovalStatusComment());
			leadDto.setApprovalStatusUpdatedTime(DateUtils.getUtcString(lead.getApprovalStatusUpdatedTime()));
			if (lead.getAssociatedDeal() != null) {
				leadDto.setAssociatedDealId(lead.getAssociatedDeal().getId());
			}
			if (lead.getCreatedForCompany().getId() == lead.getCreatedByCompany().getId()) {
				leadDto.setSelfLead(true);
			}
			/*** XNFR-505 ***/
			if (lead.getCreatedForCompany().getId() != null && lead.getCreatedByCompany().getId() != null) {
				Partnership partnership = partnershipDAO.checkPartnership(lead.getCreatedForCompany().getId(),
						lead.getCreatedByCompany().getId());
				if (partnership != null || leadDto.isSelfLead()) {
					leadDto.setEnableRegisterDealButton(true);
				}
			}

			/** XNFR-521 **/
			if (lead.getCreatedForPipeline() != null && lead.getCreatedForPipelineStage() != null) {
				leadDto.setCreatedForPipelineId(lead.getCreatedForPipeline().getId());
				leadDto.setCreatedForPipelineStageId(lead.getCreatedForPipelineStage().getId());
			}

			if (lead.getCreatedByPipeline() != null && lead.getCreatedByPipelineStage() != null) {
				leadDto.setCreatedByPipelineId(lead.getCreatedByPipeline().getId());
				leadDto.setCreatedByPipelineStageId(lead.getCreatedByPipelineStage().getId());
			}

			if (lead.getHalopsaTicketTypeId() != null && lead.getHalopsaTicketTypeId() > 0) {
				leadDto.setHalopsaTicketTypeId(lead.getHalopsaTicketTypeId() + "");
			}

			leadDto.setTitle(lead.getTitle());
			leadDto.setIndustry(lead.getIndustry());
			leadDto.setRegion(lead.getRegion());
			leadDto.setReferenceId(lead.getReferenceId());

			/**** Added On 28/07/2024 ****/
			setShowRegisterDealPropertyByCreatedForCompanyId(leadDto);
			if (lead.getSfCustomFieldsData() != null) {
				leadDto.setSfCustomFieldsData(lead.getSfCustomFieldsData());
			}
			setPartnerDetails(leadDto, lead);
			fillCreatedByDetailsInDTO(leadDto, lead);
			fillUnReadChatCounts(leadDto, lead, loggedInUserId);
		}
		return leadDto;
	}


	private void setShowRegisterDealPropertyByCreatedForCompanyId(LeadDto leadDto) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(
				"select distinct i.deal_by_partner as \"dealByPartner\", i.deal_by_vendor as \"dealByVendor\",i.deal_by_self_lead as \"dealBySelfLead\",i.can_partner_edit_lead as \"partnerEditLead\", "
						+ "i.can_partner_delete_lead as \"partnerDeleteLead\" from xt_integration i,xt_lead l where l.created_for_company_id = i.company_id and i.company_id = :vendorCompanyId and i.active");
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("vendorCompanyId", leadDto.getCreatedForCompanyId()));
		IntegrationSettingsDTO integrationSettingsDTO = (IntegrationSettingsDTO) hibernateSQLQueryResultUtilDao
				.getDto(hibernateSQLQueryResultRequestDTO, IntegrationSettingsDTO.class);
		setCRMToggleValuesToLead(leadDto, integrationSettingsDTO);
	}

	private void setCRMToggleValuesToLead(LeadDto leadDto, IntegrationSettingsDTO integrationSettingsDTO) {
		if (integrationSettingsDTO != null) {
			leadDto.setDealByPartner(integrationSettingsDTO.isDealByPartner());
			leadDto.setDealByVendor(integrationSettingsDTO.isDealByVendor());
			leadDto.setDealBySelfLead(integrationSettingsDTO.isDealBySelfLead());
			if (!leadDto.getCreatedForCompanyId().equals(leadDto.getCreatedByCompanyId())) {
				leadDto.setPartnerEditLead(integrationSettingsDTO.isPartnerEditLead());
				leadDto.setPartnerDeleteLead(integrationSettingsDTO.isPartnerDeleteLead());
			} else {
				leadDto.setPartnerEditLead(true);
				leadDto.setPartnerDeleteLead(true);
			}
		} else {
			leadDto.setDealByPartner(true);
			leadDto.setDealByVendor(false);
			leadDto.setDealBySelfLead(true);
			leadDto.setPartnerEditLead(true);
			leadDto.setPartnerDeleteLead(true);
		}
	}

	private void fillUnReadChatCounts(LeadDto leadDto, Lead lead, Integer loggedInUserId) {
		BigInteger unReadCount = leadDAO.getUnReadChatCount(lead.getId(), loggedInUserId);
		leadDto.setUnReadChatCount(unReadCount);
	}

	private PipelineDto getPipelineDto(Pipeline pipeline, Integer loggedInUserId) {
		PipelineDto pipelineDto = null;
		if (pipeline != null) {
			pipelineDto = new PipelineDto();
			pipelineDto.setId(pipeline.getId());
			pipelineDto.setName(pipeline.getName());
			pipelineDto.setPrivate(pipeline.isPrivate());
			List<PipelineStageDto> stagesDtoList = new ArrayList<>();
			List<PipelineStage> stages = pipeline.getStages();
			for (PipelineStage stage : stages) {
				if (canViewStage(pipeline, loggedInUserId, stage)) {
					PipelineStageDto stageDto = new PipelineStageDto();
					stageDto.setId(stage.getId());
					stageDto.setStageName(stage.getStageName());
					stageDto.setDisplayIndex(stage.getDisplayIndex());
					stagesDtoList.add(stageDto);
				}
			}
			pipelineDto.setStages(stagesDtoList);
		}
		return pipelineDto;
	}

	private boolean canViewStage(Pipeline pipeline, Integer loggedInUserId, PipelineStage stage) {
		boolean canView = false;
		Integer loggedInCompanyId = userService.getCompanyIdByUserId(loggedInUserId);
		if (stage != null) {
			if (pipeline.getCompany().getId().equals(loggedInCompanyId)) {
				canView = true;
			} else {
				if (!stage.isPrivate()) {
					canView = true;
				}
			}
		}
		return canView;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getLeadsForVendor(Pagination pagination) {
		Map<String, Object> resultMap = new HashMap<>();
		if (pagination != null && pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer companyId = userService.getCompanyIdByUserId(pagination.getUserId());
			if (companyId != null) {
				pagination.setCompanyId(companyId);
				utilService.setDateFilters(pagination);
				resultMap = leadDAO.getLeadsForVendor(pagination);
				if (resultMap != null && !resultMap.isEmpty() && resultMap.get("data") != null) {
					List<Lead> leads = (List<Lead>) resultMap.get("data");
					resultMap.put("data", getLeadDtoList(leads, pagination.getUserId()));
				}
			}
		}
		return resultMap;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getLeadsForVendorForCsv(Pagination pagination) {
		Map<String, Object> resultMap = new HashMap<>();
		if (pagination != null && pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer companyId = userService.getCompanyIdByUserId(pagination.getUserId());
			if (companyId != null) {
				pagination.setCompanyId(companyId);
				utilService.setDateFilters(pagination);
				resultMap = leadDAO.getLeadsForVendor(pagination);
				if (resultMap != null && !resultMap.isEmpty() && resultMap.get("data") != null) {
					List<Lead> leads = (List<Lead>) resultMap.get("data");
					resultMap.put("data", getLeadDtoListCSV(leads, pagination.getUserId()));
				}
			}
		}
		return resultMap;
	}

	@Override
	public XtremandResponse getLead(Integer loggedInUserId, Integer leadId) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (loggedInUserId != null && loggedInUserId > 0 && leadId != null && leadId > 0) {
			Lead lead = genericDAO.get(Lead.class, leadId);
			if (canViewLead(lead, loggedInUserId)) {
				response.setData(getLeadDto(lead, loggedInUserId));
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

	private boolean canViewLead(Lead lead, Integer loggedInUserId) {
		boolean canViewLead = false;
		Integer loggedInCompanyId = userService.getCompanyIdByUserId(loggedInUserId);
		if (loggedInCompanyId != null) {
			if (loggedInCompanyId.intValue() == lead.getCreatedByCompany().getId().intValue()
					|| loggedInCompanyId.intValue() == lead.getCreatedForCompany().getId().intValue()) {
				canViewLead = true;
			}
		}
		return canViewLead;
	}

	@Override
	public XtremandResponse deleteLead(LeadDto leadDto) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (leadDto != null && leadDto.getId() != null && leadDto.getId() > 0 && leadDto.getUserId() != null
				&& leadDto.getUserId() > 0) {
			Integer loggedInUserId = leadDto.getUserId();
			Lead lead = genericDAO.get(Lead.class, leadDto.getId());
			if (canUpdateLead(lead, loggedInUserId)) {
				Deal deal = lead.getAssociatedDeal();
				if (deal != null) {
					deal.setAssociatedLead(null);
				}
				genericDAO.remove(lead);
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
	public XtremandResponse getVendorList(Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (loggedInUserId != null && loggedInUserId > 0) {
			CompanyProfile company = userService.getCompanyProfileByUser(loggedInUserId);
			if (company != null) {
				Integer companyId = company.getId();
				List<VendorDTO> vendorDTOList = new ArrayList<>();
				List<Object[]> vendorList = leadDAO.getVendorList(companyId);
				for (Object[] vendor : vendorList) {
					VendorDTO vendorDTO = new VendorDTO();
					vendorDTO.setCompanyId((Integer) vendor[0]);
					vendorDTO.setCompanyName((String) vendor[1]);
					vendorDTOList.add(vendorDTO);
				}
				response.setData(vendorDTOList);
				responseMessage = SUCCESS;
				responseStatusCode = 200;
			}
		} else {
			responseMessage = INVALID_INPUT;
			responseStatusCode = 500;
		}
		response.setMessage(responseMessage);
		response.setStatusCode(responseStatusCode);
		return response;
	}

	private boolean hasOpportunitiesAccess(Integer userId) {
		boolean hasOpportunitiesAccess = utilDao.hasEnableLeadsAccessByUserId(userId);
		if (hasOpportunitiesAccess && teamDao.isTeamMember(userId)) {
			RoleDisplayDTO roleDisplayDto = utilService.getRoleDetailsByUserId(userId);
			List<Integer> roleIds = roleDisplayDto.getRoleIds();
			hasOpportunitiesAccess = roleIds.indexOf(Role.OPPORTUNITY.getRoleId()) > -1;
		}
		return hasOpportunitiesAccess;
	}

	@Override
	public XtremandResponse getCreatedForCompanyId(Integer campaignId, Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (loggedInUserId != null && loggedInUserId > 0 && campaignId != null && campaignId > 0) {
			CompanyProfile company = userService.getCompanyProfileByUser(loggedInUserId);
			if (company != null) {
				responseMessage = UNAUTHORIZED;
				responseStatusCode = 401;
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
	public XtremandResponse getLeadCounts(VanityUrlDetailsDTO vanityUrlDetails) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (vanityUrlDetails != null) {
			Integer loggedInUserId = vanityUrlDetails.getUserId();
			if (loggedInUserId != null && loggedInUserId > 0) {
				CompanyProfile company = userService.getCompanyProfileByUser(loggedInUserId);
				if (company != null) {
					VanityUrlDetailsDTO vanityUrlDetailsDTO = utilService.getVanityUrlFilteredData(loggedInUserId,
							vanityUrlDetails.isVanityUrlFilter(), vanityUrlDetails.getVendorCompanyProfileName());
					Integer vendorCompanyId = null;
					if (vanityUrlDetailsDTO.isPartnerLoggedInThroughVanityUrl()) {
						vendorCompanyId = vanityUrlDetailsDTO.getVendorCompanyId();
					} else {
						Integer loginAsUserId = vanityUrlDetails.getLoginAsUserId();
						boolean isLoginAsUser = XamplifyUtils.isLoginAsPartner(loginAsUserId);
						if (isLoginAsUser) {
							vendorCompanyId = userDao.getCompanyIdByUserId(loginAsUserId);
						}
					}
					Map<String, Object> resultMap = new HashMap<>();
					resultMap.put("vendorCounts", new LeadCountsResponseDTO());
					resultMap.put("partnerCounts", new LeadCountsResponseDTO());

					Object leadPartnerCounts = null;
					LeadCountsResponseDTO leadCountsResponseDTO = vendorLeadsCount(loggedInUserId,
							vanityUrlDetails.isApplyFilter());
					if (vendorCompanyId != null && vendorCompanyId > 0) {
						leadPartnerCounts = leadDAO.getCountsForPartnerInVanity(company.getId(), vendorCompanyId);
					} else {
						leadPartnerCounts = leadDAO.getCountsForPartner(company.getId());
					}
					if (leadCountsResponseDTO != null) {
						resultMap.put("vendorCounts", leadCountsResponseDTO);
					}
					if (leadPartnerCounts != null) {
						resultMap.put("partnerCounts", leadPartnerCounts);
					}
					response.setData(resultMap);
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

	@Override
	public XtremandResponse getCompanyIdByCompanyProfileName(String companyProfileName, Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(200);
		response.setMessage(SUCCESS);
		Integer vendorCompanyId = userDao.getCompanyIdByProfileName(companyProfileName);
		response.setData(vendorCompanyId);
		return response;
	}

	@Override
	public XtremandResponse getViewType(VanityUrlDetailsDTO vanityUrlDetails) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		String viewType = "VendorView";
		if (vanityUrlDetails != null) {
			Integer loggedInUserId = vanityUrlDetails.getUserId();
			if (loggedInUserId != null && loggedInUserId > 0) {
				CompanyProfile company = userService.getCompanyProfileByUser(loggedInUserId);
				if (company != null) {
					if (vanityUrlDetails.isVanityUrlFilter()) {
						VanityUrlDetailsDTO vanityUrlDetailsDTO = utilService.getVanityUrlFilteredData(loggedInUserId,
								vanityUrlDetails.isVanityUrlFilter(), vanityUrlDetails.getVendorCompanyProfileName());
						if (vanityUrlDetailsDTO.isPartnerLoggedInThroughVanityUrl()) {
							viewType = "PartnerView";
						} else if (vanityUrlDetailsDTO.isVendorLoggedInThroughOwnVanityUrl()) {
							viewType = "VendorView";
						}
					} else {
						String companyType = utilService.getCompanyType(company.getId());
						if (companyType != null && companyType.equals(PARTNER)) {
							viewType = "PartnerView";
						} else {
							viewType = "VendorView";
						}
					}
					responseMessage = SUCCESS;
					responseStatusCode = 200;
				} else {
					responseMessage = INVALID_INPUT;
					responseStatusCode = 500;
				}
			} else {
				responseMessage = INVALID_INPUT;
				responseStatusCode = 500;
			}
		} else {
			responseMessage = INVALID_INPUT;
			responseStatusCode = 500;
		}
		response.setData(viewType);
		response.setMessage(responseMessage);
		response.setStatusCode(responseStatusCode);
		return response;
	}

	@Override
	public LeadCountsResponseDTO vendorLeadsCount(Integer userId, boolean applyFilter) {
		try {
			Integer companyId = userDao.getCompanyIdByUserId(userId);
			TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(userId, applyFilter, false);
			boolean applyTeamMemberFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
			if (teamMemberFilterDTO.isEmptyFilter()) {
				return new LeadCountsResponseDTO();
			} else {
				if (applyTeamMemberFilter) {
					LeadCountsResponseDTO leadCountsResponseDTO = getCountsForVendorWithTeamMemberFilter(companyId,
							teamMemberFilterDTO.getPartnershipIdsOrPartnerCompanyIds());
					return setLeadVendorCountsView(leadCountsResponseDTO);
				} else {
					LeadCountsResponseDTO leadCountsResponseDTO = leadDAO.getCountsForVendor(companyId);
					return setLeadVendorCountsView(leadCountsResponseDTO);
				}
			}
		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex);
		}

	}

	private LeadCountsResponseDTO getCountsForVendorWithTeamMemberFilter(Integer companyId,
			List<Integer> partnershipIdsOrPartnerCompanyIds) {
		String teamMemberFilterCondition = "l.created_by_company_id  in (:partnerCompanyIds)";
		String sqlQuery = " WITH total_leads AS (SELECT l.created_for_company_id,count(*) AS total_leads FROM xt_lead l where "
				+ teamMemberFilterCondition
				+ " GROUP BY 1),won_leads AS (SELECT l.created_for_company_id,count(*) AS won_leads FROM xt_lead l,xt_pipeline_stage ps  WHERE l.pipeline_stage_id = ps.id AND ps.is_won = true "
				+ " and " + teamMemberFilterCondition
				+ " GROUP BY l.created_for_company_id), lost_leads AS (SELECT l.created_for_company_id,count(*) AS lost_leads FROM xt_lead l,"
				+ "  xt_pipeline_stage ps   WHERE l.pipeline_stage_id = ps.id AND ps.is_lost = true and  "
				+ teamMemberFilterCondition + " GROUP BY l.created_for_company_id),"
				+ " converted_leads AS (SELECT l.created_for_company_id,count(*) AS converted_leads FROM xt_lead l, xt_deal d WHERE d.associated_lead_id IS NOT NULL AND d.associated_lead_id = l.id "
				+ " and " + teamMemberFilterCondition
				+ " GROUP BY l.created_for_company_id) select total_leads.created_for_company_id AS \"companyId\", "
				+ " cast(COALESCE(total_leads.total_leads, 0) as int) AS \"totalLeads\", cast(COALESCE(won_leads.won_leads,0) as int) AS \"wonLeads\",cast(COALESCE(lost_leads.lost_leads,0) as int) AS \"lostLeads\", "
				+ " cast(COALESCE(converted_leads.converted_leads,0) as int) AS \"convertedLeads\"  FROM total_leads LEFT JOIN won_leads ON won_leads.created_for_company_id = total_leads.created_for_company_id "
				+ " LEFT JOIN lost_leads ON lost_leads.created_for_company_id = total_leads.created_for_company_id  LEFT JOIN converted_leads ON  "
				+ "converted_leads.created_for_company_id = total_leads.created_for_company_id "
				+ " where total_leads.created_for_company_id= :companyId";

		return leadDAO.findLeadsCountByFilter(sqlQuery, companyId, partnershipIdsOrPartnerCompanyIds);
	}

	private LeadCountsResponseDTO setLeadVendorCountsView(LeadCountsResponseDTO leadCountsResponseDTO) {
		if (leadCountsResponseDTO != null) {
			return leadCountsResponseDTO;
		} else {
			return new LeadCountsResponseDTO();
		}
	}

	@Override
	public XtremandResponse getChat(Integer leadId, Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (leadId != null && leadId > 0 && loggedInUserId != null && loggedInUserId > 0) {
			response.setData(null);
			responseMessage = SUCCESS;
			responseStatusCode = 200;
		} else {
			responseMessage = INVALID_INPUT;
			responseStatusCode = 500;
		}

		response.setMessage(responseMessage);
		response.setStatusCode(responseStatusCode);
		return response;
	}

	@SuppressWarnings("unchecked")
	public List<LeadDto> getLeadsForCSV(Pagination pagination) {
		if ("all".equals(pagination.getType())) {
			pagination.setFilterKey(null);
		}
		pagination.setExcludeLimit(true);

		Map<String, Object> leadsMap = null;
		if ("v".equals(pagination.getUserType())) {
			pagination.setPartnerTeamMemberGroupFilter(pagination.isPartnerTeamMemberGroupFilter());
			leadsMap = getLeadsForVendorForCsv(pagination);
		} else if ("p".equals(pagination.getUserType())) {
			leadsMap = getLeadsForPartnerForCSV(pagination);
		}
		return (List<LeadDto>) leadsMap.get("data");
	}

	@Override
	public void downloadLeads(HttpServletResponse httpServletResponse, String userType, String type, Integer userId,
			String filename, boolean vanityUrlFilter, String vendorCompanyProfileName, String searchKey,
			String fromDate, String toDate, boolean partnerTeamMemberGroupFilter, String timeZone, String stageName) {
		if (!StringUtils.isBlank(type) && userId != null && userId > 0) {
			Pagination pagination = new Pagination();
			pagination.setUserType(userType);
			pagination.setFilterKey(type);
			pagination.setUserId(userId);
			pagination.setVanityUrlFilter(vanityUrlFilter);
			pagination.setVendorCompanyProfileName(vendorCompanyProfileName);
			pagination.setSearchKey(searchKey);
			pagination.setFromDateFilterString(fromDate);
			pagination.setToDateFilterString(toDate);
			pagination.setPartnerTeamMemberGroupFilter(partnerTeamMemberGroupFilter);
			pagination.setTimeZone(timeZone);
			pagination.setStageFilter(stageName);

			List<LeadDto> leads = getLeadsForCSV(pagination);
			LinkedHashMap<String, String> fieldHeaderMapping = getFieldHeaderMapping(userType, userId, vanityUrlFilter,
					vendorCompanyProfileName);
			if (leads != null && !leads.isEmpty()) {
				csvUtilService.downLoadToCSV(httpServletResponse, filename, fieldHeaderMapping, leads);
			}
		}
	}

	@Override
	public List<String> getStageNamesForVendor(Integer loggedInUserId) {
		List<String> stageNames = new ArrayList<>();

		if (loggedInUserId != null && loggedInUserId > 0) {
			Integer companyId = userService.getCompanyIdByUserId(loggedInUserId);
			if (companyId != null && companyId > 0) {
				stageNames = leadDAO.getStageNamesForVendor(companyId);
			}
		}
		return stageNames;

	}

	@Override
	public List<String> getStageNamesForPartner(Integer loggedInUserId) {
		List<String> stageNames = new ArrayList<>();
		if (loggedInUserId != null && loggedInUserId > 0) {
			Integer companyId = userService.getCompanyIdByUserId(loggedInUserId);
			if (companyId != null && companyId > 0) {
				stageNames = leadDAO.getStageNamesForPartner(companyId);
			}
		}
		return stageNames;
	}

	@Override
	public List<String> getStageNamesForPartnerInCampaign(Integer loggedInUserId) {
		List<String> stageNames = new ArrayList<String>();
		if (loggedInUserId != null && loggedInUserId > 0) {
			Integer companyId = userService.getCompanyIdByUserId(loggedInUserId);
			if (companyId != null && companyId > 0) {
				stageNames = leadDAO.getStgaeNamesFOrPartnerInCampaign(companyId);
			}
		}
		return stageNames;
	}

	@Override
	public List<String> getStageNamesForPartner(VanityUrlDetailsDTO vanityUrlDetails) {
		List<String> stageNames = new ArrayList<String>();
		if (vanityUrlDetails != null) {
			Integer loggedInUserId = vanityUrlDetails.getUserId();
			if (loggedInUserId != null && loggedInUserId > 0) {
				CompanyProfile company = userService.getCompanyProfileByUser(loggedInUserId);
				if (company != null) {
					VanityUrlDetailsDTO vanityUrlDetailsDTO = utilService.getVanityUrlFilteredData(loggedInUserId,
							vanityUrlDetails.isVanityUrlFilter(), vanityUrlDetails.getVendorCompanyProfileName());
					Integer vendorCompanyId = null;
					if (vanityUrlDetailsDTO.isPartnerLoggedInThroughVanityUrl()) {
						vendorCompanyId = vanityUrlDetailsDTO.getVendorCompanyId();
					}
					stageNames = leadDAO.getStageNamesForPartner(company.getId(), vendorCompanyId);
				}
			}
		}
		return stageNames;
	}

	@Override
	public XtremandResponse getVendorListForLoginAsUserId(Integer loggedInUserId, Integer loginAsUserId) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (loggedInUserId != null && loggedInUserId > 0 && loginAsUserId != null && loginAsUserId > 0) {
			CompanyProfile company = userService.getCompanyProfileByUser(loggedInUserId);
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(loginAsUserId);
			if (company != null && vendorCompanyId != null) {
				Integer companyId = company.getId();
				List<VendorDTO> vendorDTOList = new ArrayList<>();
				if (hasOpportunitiesAccess(loggedInUserId)) {
					VendorDTO vendorDTO = new VendorDTO();
					vendorDTO.setCompanyId(companyId);
					vendorDTO.setCompanyName("Self - " + company.getCompanyName());
					vendorDTOList.add(vendorDTO);
				}
				List<Object[]> vendorList = leadDAO.getVendorListForLoginAsPartner(companyId, vendorCompanyId);
				for (Object[] vendor : vendorList) {
					VendorDTO vendorDTO = new VendorDTO();
					vendorDTO.setCompanyId((Integer) vendor[0]);
					vendorDTO.setCompanyName((String) vendor[1]);
					vendorDTOList.add(vendorDTO);
				}
				response.setData(vendorDTOList);
				responseMessage = SUCCESS;
				responseStatusCode = 200;
			}
		} else {
			responseMessage = INVALID_INPUT;
			responseStatusCode = 500;
		}
		response.setMessage(responseMessage);
		response.setStatusCode(responseStatusCode);
		return response;
	}

	/****** XNFR-426 ***/
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse updateLeadApprovalStatus(LeadDto leadDto) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;

		Lead lead = genericDAO.get(Lead.class, leadDto.getId());
		Integer loggedInUserId = leadDto.getUserId();
		CompanyProfile loggedInCompany = userService.getCompanyProfileByUser(loggedInUserId);
		Integer partnerCompanyId = lead.getCreatedByCompany().getId();
		Integer vendorCompanyId = lead.getCreatedForCompany().getId();
		String partnerStatus = partnershipDAO
				.findPartnerShipStatusByPartnerCompanyIdAndVendorCompanyId(partnerCompanyId, vendorCompanyId);

		if (loggedInCompany.getId() == lead.getCreatedForCompany().getId()
				&& !"DEACTIVATED".equalsIgnoreCase(partnerStatus)) {
			if (lead != null && leadDto.getLeadApprovalStatusType() != null) {
				lead.setLeadApprovalStatusType(leadDto.getLeadApprovalStatusType());
				lead.setApprovalStatusComment(leadDto.getApprovalStatusComment());
				lead.setApprovalStatusUpdatedTime(new Date());
				lead.setUpdatedBy(leadDto.getUserId());
				genericDAO.update(lead);
				mailService.sendLeadApprovedOrRejectedEmail(lead, leadDto.getUserId());
				responseMessage = SUCCESS;
				responseStatusCode = 200;
			}
		}

		response.setMessage(responseMessage);
		response.setStatusCode(responseStatusCode);
		return response;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse downloadLeads(Integer userId) {
		XtremandResponse response = new XtremandResponse();
		DownloadDataInfo downloadDataInfo = utilDao.getDownloadDataInfo(userId, DownloadItem.LEADS_DATA);
		if (downloadDataInfo == null || !downloadDataInfo.isDownloadInProgress()) {
			downloadDataInfo = utilDao.updateDownloadDataInfo(userId, downloadDataInfo, DownloadItem.LEADS_DATA);
			response.setMessage(
					"We are processing your lead(s) reports. We will send it over an email when the report is ready");
			response.setStatusCode(200);
		} else {
			response.setMessage("Please wait until the previous request is processed...!");
			response.setStatusCode(401);
		}
		response.setData(downloadDataInfo.getId());
		return response;
	}

	private boolean validateLead(Lead lead) {
		boolean valid = false;
		if (lead != null && lead.getId() > 0) {
			if (lead.getLeadApprovalStatusType() != LeadApprovalStatusEnum.REJECTED) {
				valid = true;
			}
		}
		return valid;
	}

	/*** XNFR-505 ***/
	@Override
	public Map<String, Object> getLeadsForLeadAttachment(Pagination pagination) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		if (pagination != null && pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer companyId = userService.getCompanyIdByUserId(pagination.getUserId());
			if (companyId != null) {
				pagination.setCompanyId(companyId);
				setVendorCompanyId(pagination);
				/*** XNFR-505,XNFR-483 ***/
				if (pagination.getVendorCompanyId() != null && pagination.getVendorCompanyId() > 0) {
					Boolean isVendorEnabledLeadApprovalRejectionFeature = (Boolean) userDao
							.getLeadApprovalStatus(pagination.getVendorCompanyId());
					if (Boolean.TRUE.equals(!isVendorEnabledLeadApprovalRejectionFeature)
							|| pagination.getCompanyId().equals(pagination.getVendorCompanyId())) {
						pagination.setLeadApprovalFeatureEnabledForVendorCompany(true);
					}
				}
				setInvalidVendorsList(pagination, companyId);
				utilService.setDateFilters(pagination);
				resultMap = leadDAO.getLeadsForLeadAttachment(pagination);
			}
		}
		return resultMap;
	}

	private void setInvalidVendorsList(Pagination pagination, Integer companyId) {
		Map<String, Object> vendorsMap = integrationService.getVendorRegisterDealMap(companyId, 0);
		List<Integer> vendorsList = vendorsMap.keySet().stream()
				.filter(entry -> (vendorsMap.get(entry) != null) && !Boolean.parseBoolean(vendorsMap.get(entry) + ""))
				.map(Integer::parseInt).collect(Collectors.toList());
		pagination.setInvalidVendorIds(vendorsList);
	}

	@SuppressWarnings("unchecked")
	@Override
	public XtremandResponse findRegisteredByCompanies(Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(
				"select distinct  xcp.company_id as \"id\",xcp.company_name as \"name\" from xt_lead xl,xt_company_profile xcp where xcp.company_id  = xl.created_by_company_id \r\n"
						+ "and xl.created_for_company_id  = :createdForCompanyId order by xcp.company_name  asc");
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("createdForCompanyId", companyId));
		hibernateSQLQueryResultRequestDTO.setClassInstance(SearchableDropDownDTO.class);
		List<SearchableDropDownDTO> companyDetails = (List<SearchableDropDownDTO>) hibernateSQLQueryResultUtilDao
				.returnDTOList(hibernateSQLQueryResultRequestDTO);
		XamplifyUtils.addSuccessStatus(response);
		response.setData(companyDetails);
		return response;
	}

	@SuppressWarnings("unchecked")
	@Override
	public XtremandResponse findRegisteredByCompaniesForPartnerView(Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(
				"select distinct  xcp.company_id as \"id\",xcp.company_name as \"name\" from xt_lead xl,xt_company_profile xcp where xcp.company_id  = xl.created_by_company_id \r\n"
						+ "and xl.created_by_company_id  = :createdByCompanyId order by xcp.company_name  asc");
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(CREATED_BY_COMPANY_ID, companyId));
		hibernateSQLQueryResultRequestDTO.setClassInstance(SearchableDropDownDTO.class);
		List<SearchableDropDownDTO> companyDetails = (List<SearchableDropDownDTO>) hibernateSQLQueryResultUtilDao
				.returnDTOList(hibernateSQLQueryResultRequestDTO);
		XamplifyUtils.addSuccessStatus(response);
		response.setData(companyDetails);
		return response;
	}

	@SuppressWarnings("unchecked")
	@Override
	public XtremandResponse findRegisteredByUsers(Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(USER_DETAILS_QUERY_STRING
				+ "and xl.created_for_company_id  = :createdForCompanyId order by \"name\" asc");
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("createdForCompanyId", companyId));
		hibernateSQLQueryResultRequestDTO.setClassInstance(SearchableDropDownDTO.class);
		List<SearchableDropDownDTO> companyDetails = (List<SearchableDropDownDTO>) hibernateSQLQueryResultUtilDao
				.returnDTOList(hibernateSQLQueryResultRequestDTO);
		XamplifyUtils.addSuccessStatus(response);
		response.setData(companyDetails);
		return response;
	}

	@SuppressWarnings("unchecked")
	@Override
	public XtremandResponse findRegisteredByUsersForPartnerView(Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(USER_DETAILS_QUERY_STRING
				+ "and xl.created_by_company_id  = :createdByCompanyId order by \"name\" asc");
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(CREATED_BY_COMPANY_ID, companyId));
		hibernateSQLQueryResultRequestDTO.setClassInstance(SearchableDropDownDTO.class);
		List<SearchableDropDownDTO> companyDetails = (List<SearchableDropDownDTO>) hibernateSQLQueryResultUtilDao
				.returnDTOList(hibernateSQLQueryResultRequestDTO);
		XamplifyUtils.addSuccessStatus(response);
		response.setData(companyDetails);
		return response;
	}

	@SuppressWarnings("unchecked")
	@Override
	public XtremandResponse findRegisteredByUsersByPartnerCompanyId(Integer partnerCompanyId, Integer campaignId) {
		XtremandResponse response = new XtremandResponse();
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(
				"select distinct xup.user_id as \"id\",case when LENGTH(TRIM(concat(TRIM(xup.firstname), ' ', TRIM(xup.lastname))))>0  \r\n"
						+ "then TRIM(concat(TRIM(xup.email_id),' - ( ',TRIM(xup.firstname), ' ', TRIM(xup.lastname),' )')) else xup.email_id end as \"name\" from xt_lead xl,xt_user_profile xup,xt_campaign xc"
						+ " where xup.user_id  = xl.created_by  and xc.campaign_id  = xl.campaign_id \r\n"
						+ "and xl.created_by_company_id = :createdByCompanyId and xc.parent_campaign_id = :campaignId order by \"name\" asc");
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(CREATED_BY_COMPANY_ID, partnerCompanyId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("campaignId", campaignId));
		hibernateSQLQueryResultRequestDTO.setClassInstance(SearchableDropDownDTO.class);
		List<SearchableDropDownDTO> companyDetails = (List<SearchableDropDownDTO>) hibernateSQLQueryResultUtilDao
				.returnDTOList(hibernateSQLQueryResultRequestDTO);
		XamplifyUtils.addSuccessStatus(response);
		response.setData(companyDetails);
		return response;
	}

	@Override
	public XtremandResponse getCustomLeadFields(Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		List<LeadCustomFieldDto> leadCustomFieldDtos = new ArrayList<LeadCustomFieldDto>();
		if (loggedInUserId != null) {
			Integer companyId = userService.getCompanyIdByUserId(loggedInUserId);
			if (companyId != null) {
				leadCustomFieldDtos = getLeadCustomFieldsDto(companyId);
			}
		}
		response.setData(leadCustomFieldDtos);
		response.setStatusCode(200);
		return response;
	}

	private List<LeadCustomFieldDto> getLeadCustomFieldsDto(Integer companyId) {
		List<LeadCustomFieldDto> leadCustomFieldDtos = new ArrayList<LeadCustomFieldDto>();
		if (companyId != null) {
			List<LeadField> defaultLeadFields = leadDAO.getDefaultLeadFilds();
			List<LeadCustomField> existingfields = leadDAO.getLeadCustomFields(companyId);
			if (existingfields.isEmpty()) {
				for (LeadField leadfield : defaultLeadFields) {
					LeadCustomFieldDto leadCustomFieldDto = new LeadCustomFieldDto();
					leadCustomFieldDto.setLabelName(leadfield.getLabelName());
					leadCustomFieldDto.setLabelId(leadfield.getLabelId());
					leadCustomFieldDto.setPlaceholder("");
					leadCustomFieldDto.setDisplayName(leadfield.getLabelName());
					leadCustomFieldDto.setId(leadfield.getId());
					if (leadfield.getLabelId().equals("region")) {
						leadCustomFieldDtos.add(10, leadCustomFieldDto);
					} else {
						leadCustomFieldDtos.add(leadCustomFieldDto);
					}
				}
			} else {
				for (LeadCustomField leadfield : existingfields) {
					LeadCustomFieldDto leadCustomFieldDto = new LeadCustomFieldDto();
					leadCustomFieldDto.setLabelName(leadfield.getLabelName());
					leadCustomFieldDto.setLabelId(leadfield.getLabelId());
					leadCustomFieldDto.setPlaceholder(leadfield.getPlaceholder());
					leadCustomFieldDto.setDisplayName(leadfield.getDisplayName());
					leadCustomFieldDto.setId(leadfield.getLeadFieldId().getId());
					leadCustomFieldDtos.add(leadCustomFieldDto);
				}
				if (leadCustomFieldDtos != null) {
					leadCustomFieldDtos = updateCustomFieldsWithDeafaultFields(defaultLeadFields, existingfields,
							leadCustomFieldDtos);
				}
			}
		}
		return leadCustomFieldDtos;
	}

	private List<LeadCustomFieldDto> updateCustomFieldsWithDeafaultFields(List<LeadField> defaultLeadFields,
			List<LeadCustomField> existingfields, List<LeadCustomFieldDto> leadCustomFieldDtos) {
		Map<String, LeadCustomField> existingFieldsMap = Collections.emptyMap();
		if (existingfields != null) {
			existingFieldsMap = existingfields.stream().collect(Collectors.toMap(LeadCustomField::getLabelId,
					Function.identity(), (existing, replacement) -> existing));
		}
		for (LeadField leadfield : defaultLeadFields) {
			if (!existingFieldsMap.containsKey(leadfield.getLabelId())) {
				LeadCustomFieldDto leadCustomFieldDto = new LeadCustomFieldDto();
				leadCustomFieldDto.setLabelName(leadfield.getLabelName());
				leadCustomFieldDto.setLabelId(leadfield.getLabelId());
				leadCustomFieldDto.setPlaceholder("");
				leadCustomFieldDto.setDisplayName(leadfield.getLabelName());
				leadCustomFieldDto.setId(leadfield.getId());
				leadCustomFieldDtos.add(leadCustomFieldDto);
			}
		}
		return leadCustomFieldDtos;
	}

	@Override
	public XtremandResponse saveOrupdateCustomLeadFields(Integer loggedInUserId,
			List<LeadCustomFieldDto> leadFieldsDto) {
		XtremandResponse response = new XtremandResponse();
		int displayIndex = 1;
		List<LeadCustomFieldDto> leadCustomFieldDtos = new ArrayList<LeadCustomFieldDto>();
		if (loggedInUserId != null) {
			Integer companyId = userService.getCompanyIdByUserId(loggedInUserId);
			if (companyId != null) {
				List<LeadCustomField> existingfields = leadDAO.getLeadCustomFields(companyId);
				if (existingfields.isEmpty()) {
					for (LeadCustomFieldDto leadField : leadFieldsDto) {
						LeadCustomField leadCustomField = new LeadCustomField();
						LeadField leadFieldObj = genericDAO.get(LeadField.class, leadField.getId());
						leadCustomField.setCompanyId(companyId);
						leadCustomField.setCreatedBy(loggedInUserId);
						leadCustomField.setUpdatedBy(loggedInUserId);
						leadCustomField.setLabelId(leadField.getLabelId());
						leadCustomField.setLabelName(leadField.getLabelName());
						leadCustomField.setDisplayName(leadField.getDisplayName());
						leadCustomField.setPlaceholder(leadField.getPlaceholder());
						leadCustomField.setCreatedTime(new Date());
						leadCustomField.setUpdatedTime(new Date());
						leadCustomField.setLeadFieldId(leadFieldObj);
						leadCustomField.setDisplayIndex(displayIndex);
						genericDAO.save(leadCustomField);
						displayIndex++;
					}
				} else {
					for (LeadCustomFieldDto leadDTo : leadFieldsDto) {
						List<LeadCustomField> leadCustomField = existingfields.stream()
								.filter(f -> f.getLabelName().equals(leadDTo.getLabelName()))
								.collect(Collectors.toList());
						if (!leadCustomField.isEmpty()) {
							LeadCustomField customField = leadCustomField.get(0);
							customField.setLabelName(leadDTo.getLabelName());
							if (leadDTo.getDisplayName() != null && leadDTo.getDisplayName() != "") {
								customField.setDisplayName(leadDTo.getDisplayName());
							} else {
								customField.setDisplayName(leadDTo.getLabelName());
							}
							if (leadDTo.getPlaceholder() != null) {
								customField.setPlaceholder(leadDTo.getPlaceholder());
							}
							customField.setDisplayIndex(displayIndex);
							genericDAO.update(customField);
							displayIndex++;
						}
					}

					if (existingfields != null) {
						saveCustomFieldsWithDefaultFields(leadFieldsDto, existingfields, companyId, loggedInUserId);
					}

				}
			}
		}
		response.setData(leadCustomFieldDtos);
		response.setStatusCode(200);
		return response;
	}

	private void saveCustomFieldsWithDefaultFields(List<LeadCustomFieldDto> leadFieldsDto,
			List<LeadCustomField> existingfields, Integer companyId, Integer loggedInUserId) {
		Map<String, LeadCustomField> exisitngFieldsMap = null;
		int displayIndex = existingfields.size() + 1;
		if (existingfields != null) {
			exisitngFieldsMap = existingfields.stream()
					.collect(Collectors.toMap(LeadCustomField::getLabelId, Function.identity()));
		}  

		for (LeadCustomFieldDto leadField : leadFieldsDto) {
			if (!exisitngFieldsMap.containsKey(leadField.getLabelId())) {
				LeadCustomField leadCustomField = new LeadCustomField();
				LeadField leadFieldObj = genericDAO.get(LeadField.class, leadField.getId());
				leadCustomField.setCompanyId(companyId);
				leadCustomField.setCreatedBy(loggedInUserId);
				leadCustomField.setUpdatedBy(loggedInUserId);
				leadCustomField.setLabelId(leadField.getLabelId());
				leadCustomField.setLabelName(leadField.getLabelName());
				leadCustomField.setDisplayName(leadField.getDisplayName());
				leadCustomField.setPlaceholder(leadField.getPlaceholder());
				leadCustomField.setCreatedTime(new Date());
				leadCustomField.setUpdatedTime(new Date());
				leadCustomField.setLeadFieldId(leadFieldObj);
				leadCustomField.setDisplayIndex(displayIndex);
				genericDAO.save(leadCustomField);
			}
		}
	}

	@Override
	public XtremandResponse getCustomLeadFieldsByVendorCompanyId(Integer vendorCompanyId) {
		XtremandResponse response = new XtremandResponse();
		List<LeadCustomFieldDto> leadCustomFieldDtos = new ArrayList<LeadCustomFieldDto>();
		if (vendorCompanyId != null) {
			leadCustomFieldDtos = getLeadCustomFieldsDto(vendorCompanyId);
		}
		response.setData(leadCustomFieldDtos);
		response.setStatusCode(200);
		return response;
	}

	// XNFR-602
	@Override
	public LinkedHashMap<String, String> getFieldHeaderMapping(String userType, Integer userId, boolean vanityUrlFilter,
			String vendorCompanyProfileName) {
		LinkedHashMap<String, String> fieldHeaderMapping = new LinkedHashMap<>();
		ArrayList<String> headers = new ArrayList<>(Arrays.asList("first_name", "last_name", "email", "company",
				"phone", "website", "address", "city", "postalCode", "state", "country", "region", "title", "industry",
				PARTNER_TYPE, ACCOUNT_SUB_TYPE));
		if (vanityUrlFilter) {
			Integer companyId = userDao.getCompanyIdByProfileName(vendorCompanyProfileName);
			fieldHeaderMapping = setFieldHeaderMappingByCompanyId(fieldHeaderMapping, headers, companyId, userType);
		} else {
			if ("v".equals(userType)) {
				Integer companyId = userService.getCompanyIdByUserId(userId);
				fieldHeaderMapping = setFieldHeaderMappingByCompanyId(fieldHeaderMapping, headers, companyId, userType);
			} else {
				fieldHeaderMapping.put("First Name", "getFirstName");
				fieldHeaderMapping.put("Last Name", "getLastName");
				fieldHeaderMapping.put("Email", "getEmail");
				fieldHeaderMapping.put("Company", "getCompany");
				fieldHeaderMapping.put("Phone", "getPhone");
				fieldHeaderMapping.put("Website", "getWebsite");
				fieldHeaderMapping.put("Address", "getStreet");
				fieldHeaderMapping.put("City", "getCity");
				fieldHeaderMapping.put("Postal Code", "getPostalCode");
				fieldHeaderMapping.put("State", "getState");
				fieldHeaderMapping.put("Country", "getCountry");
				fieldHeaderMapping.put("Region", "getRegion");
				fieldHeaderMapping.put("Job Title", "getTitle");
				fieldHeaderMapping.put("Industry", "getIndustry");
			}
		}
		/************ XBI-1000 ***********/
		if ("p".equals(userType) && !utilDao.isPartnershipEstablishedOnlyWithPrm(userId)) {
			fieldHeaderMapping.put("Campaign Name", "getCampaignName");
			fieldHeaderMapping.put("Added For - Company", "getCreatedForCompanyName");
		} else if ("v".equals(userType) && !utilDao.isPrmCompany(userId)) {
			fieldHeaderMapping.put("Campaign Name", "getParentCampaignName");
		}

		fieldHeaderMapping.put("Added By - Company", "getCreatedByCompanyName");
		fieldHeaderMapping.put("Added By - Name", "getCreatedByName");
		fieldHeaderMapping.put("Added By - Email ID", "getCreatedByEmail");
		fieldHeaderMapping.put("Added On (PST)", "getCreatedDateString");
		fieldHeaderMapping.put("Status", "getCurrentStageName");
		return fieldHeaderMapping;
	}

	private LinkedHashMap<String, String> setFieldHeaderMappingByCompanyId(
			LinkedHashMap<String, String> fieldHeaderMapping, ArrayList<String> headers, Integer companyId,
			String userType) {
		for (String header : headers) {
			if (header.equalsIgnoreCase("first_name")) {
				fieldHeaderMapping.put("First Name", "getFirstName");
			}

			if (header.equalsIgnoreCase("last_name")) {
				fieldHeaderMapping.put("Last Name", "getLastName");
			}

			if (header.equalsIgnoreCase("email")) {
				fieldHeaderMapping.put("Email", "getEmail");
			}

			if (header.equalsIgnoreCase("company")) {
				fieldHeaderMapping.put("Company", "getCompany");
			}

			if (header.equalsIgnoreCase("phone")) {
				fieldHeaderMapping.put("Phone", "getPhone");
			}

			if (header.equalsIgnoreCase("website")) {
				fieldHeaderMapping.put("Website", "getWebsite");
			}

			if (header.equalsIgnoreCase("address")) {
				fieldHeaderMapping.put("Address", "getStreet");
			}

			if (header.equalsIgnoreCase("city")) {
				fieldHeaderMapping.put("City", "getCity");
			}

			if (header.equalsIgnoreCase("region")) {
				fieldHeaderMapping.put("Region", "getRegion");
			}

			if (header.equalsIgnoreCase("country")) {
				fieldHeaderMapping.put("Country", "getCountry");
			}

			if (header.equalsIgnoreCase("state")) {
				fieldHeaderMapping.put("State", "getState");
			}

			if (header.equalsIgnoreCase("postalCode")) {
				fieldHeaderMapping.put("Postal Code", "getPostalCode");
			}

			if (header.equalsIgnoreCase("title")) {
				fieldHeaderMapping.put("Job Title", "getTitle");
			}

			if (header.equalsIgnoreCase("industry")) {
				fieldHeaderMapping.put("Industry", "getIndustry");
			}
			if (header.equalsIgnoreCase(ACCOUNT_SUB_TYPE)) {
				fieldHeaderMapping.put(ACCOUNT_SUB_TYPE, "getAccountSubType");
			}
			if (header.equalsIgnoreCase(PARTNER_TYPE)) {
				fieldHeaderMapping.put(PARTNER_TYPE, "getPartnerType");
			}
		}
		if ("v".equals(userType)) {
			fieldHeaderMapping.put("CRM Id", "getCrmId");
			fieldHeaderMapping.put("Lead Id", "getReferenceId");
		}
		return fieldHeaderMapping;
	}

	public void updateSfCustomFieldsData(Lead lead, LeadSyncDetailsDto leadDetails, Integer activeCRMCompanyId,
			IntegrationType activeCRMIntegrationType, Integration otherActiveCRMIntegration) {
		if (activeCRMCompanyId != null && activeCRMCompanyId > 0 && activeCRMIntegrationType != null) {
			List<FormLabel> activeCRMCFLabels = utilService.getLeadCustomFormLabelsByIntegrationType(activeCRMCompanyId,
					activeCRMIntegrationType);

			List<FormLabel> otherActiveCRMCFLabels = new ArrayList<>();
			if (otherActiveCRMIntegration != null) {
				otherActiveCRMCFLabels = utilService.getLeadCustomFormLabelsByIntegrationType(
						otherActiveCRMIntegration.getCompany().getId(), otherActiveCRMIntegration.getType());
			}

			Map<String, String> customFieldValues = buildCustomFieldValues(leadDetails);

			for (FormLabel formLabel : activeCRMCFLabels) {
				if (formLabel != null) {
					saveOrUpdateSfCustomFieldsData(lead, customFieldValues, formLabel, null);
					FormLabel matchingFormLabel = utilService.getMatchedObject(formLabel, otherActiveCRMCFLabels);
					if (matchingFormLabel != null) {
						saveOrUpdateSfCustomFieldsData(lead, customFieldValues, matchingFormLabel, formLabel);
					}
				}
			}
		}
	}

	private void saveOrUpdateSfCustomFieldsData(Lead lead, JSONObject leadJson, FormLabel formLabel,
			FormLabel formLabelInOtherForm) {
		SfCustomFieldsData sfCustomFieldsData = sfCustomFormDataDAO.getSfCustomFieldDataByLeadIdAndLabelId(lead,
				formLabel);
		String value = null;
		boolean saveOrUpdate = true;
		if (formLabelInOtherForm != null) {
			value = getString(leadJson, formLabelInOtherForm.getLabelId());
			Map<String, Object> customFieldsDataMap = utilService.getSfCustomFieldsDataValue(value, formLabel,
					formLabelInOtherForm);
			value = (String) customFieldsDataMap.get("value");
			saveOrUpdate = (boolean) customFieldsDataMap.get("saveOrUpdate");
		} else {
			value = getString(leadJson, formLabel.getLabelId());
		}

		if (saveOrUpdate) {
			if (sfCustomFieldsData != null) {
				sfCustomFieldsData.setFormLabel(formLabel);
				sfCustomFieldsData.setValue(value);
				sfCustomFormDataDAO.updateSfCfData(sfCustomFieldsData);
			} else {
				sfCustomFieldsData = new SfCustomFieldsData();
				sfCustomFieldsData.setLead(lead);
				sfCustomFieldsData.setFormLabel(formLabel);
				sfCustomFieldsData.setValue(value);
				sfCustomFormDataDAO.saveSfCfData(sfCustomFieldsData);
			}
		}

	}

	private String getString(JSONObject object, String key) {
		return (object.isNull(key)) ? null : object.get(key).toString();
	}

	@Override
	public XtremandResponse findLeadAndLeadInfoAndComments(Integer leadId) {
		XtremandResponse response = new XtremandResponse();
		Lead lead = genericDAO.get(Lead.class, leadId);
		if (lead != null) {
			LeadDto leadDto = new LeadDto();
			leadDto.setId(lead.getId());
			leadDto.setCompany(lead.getCompany());
			leadDto.setCountry(lead.getCountry());
			leadDto.setEmail(lead.getEmail());
			leadDto.setFirstName(lead.getFirstName());
			leadDto.setLastName(lead.getLastName());
			leadDto.setPhone(lead.getPhone());
			leadDto.setTitle(lead.getTitle());
			leadDto.setCreatedTime(DateUtils.getUtcString(lead.getCreatedTime()));
			setPartnerDetails(leadDto, lead);
			fillCreatedByDetailsInDTO(leadDto, lead);
			XamplifyUtils.addSuccessStatus(response);
			response.setData(leadDto);
		} else {
			response.setStatusCode(404);
		}
		return response;
	}

	/*** XNFR-649 ***/
	@Override
	public Map<String, Object> queryLeadsForPartner(Pagination pagination) {
		Map<String, Object> resultMap = new HashMap<>();
		if (pagination != null && pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer companyId = userService.getCompanyIdByUserId(pagination.getUserId());
			if (companyId != null) {
				pagination.setCompanyId(companyId);
				setVendorCompanyId(pagination);
				utilService.setDateFilters(pagination);
				resultMap = leadDAO.queryLeadsForPartner(pagination);
			}
		}
		return resultMap;
	}

	@Override
	public Map<String, Object> queryLeadsForVendor(Pagination pagination) {
		Map<String, Object> resultMap = new HashMap<>();
		if (pagination != null && pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer companyId = userService.getCompanyIdByUserId(pagination.getUserId());
			if (companyId != null) {
				pagination.setCompanyId(companyId);
				utilService.setDateFilters(pagination);
				resultMap = leadDAO.queryLeadsForVendor(pagination);
			}
		}
		return resultMap;
	}

	/** XNFR-553 **/
	@Override
	public XtremandResponse findLeadsAndCountByContactId(ContactOpportunityRequestDTO contactOpportunityRequestDTO) {
		XtremandResponse response = new XtremandResponse();
		Map<String, Object> map = null;
		if (XamplifyUtils.isValidInteger(contactOpportunityRequestDTO.getLoggedInUserId())
				&& XamplifyUtils.isValidInteger(contactOpportunityRequestDTO.getContactId())) {
			Integer companyId = userDao.getCompanyIdByUserId(contactOpportunityRequestDTO.getLoggedInUserId());
			contactOpportunityRequestDTO.setLoggedInUserCompanyId(companyId);
			if (contactOpportunityRequestDTO.isVanityUrlFilter()) {
				Integer vendorCompanyId = userDao
						.getCompanyIdByProfileName(contactOpportunityRequestDTO.getVendorCompanyName());
				contactOpportunityRequestDTO.setVendorCompanyId(vendorCompanyId);
			}
			map = leadDAO.findLeadsAndCountByContactId(contactOpportunityRequestDTO);
			response.setData(map);
			XamplifyUtils.addSuccessStatus(response);
		} else {
			XamplifyUtils.addErorMessageWithStatusCode(response, "Failed to fetch the leads", 401);
		}
		return response;
	}

	@Override
	public XtremandResponse checkIfHasAcessForAddLeadOrDeal(VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		XtremandResponse response = new XtremandResponse();
		boolean hasAcess = false;
		Integer loggedInUserId = vanityUrlDetailsDTO.getUserId();
		boolean hasOpportunityAccess = checkIfHasOpportunityAccessForAdmin(loggedInUserId, vanityUrlDetailsDTO);
		hasAcess = checkIfHasAccessForAdd(vanityUrlDetailsDTO, hasAcess, loggedInUserId, hasOpportunityAccess);
		response.setData(hasAcess);
		response.setStatusCode(200);
		return response;
	}

	private boolean checkIfHasOpportunityAccessForAdmin(Integer loggedInUserId,
			VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		boolean hasAcess = false;
		if (XamplifyUtils.isValidString(vanityUrlDetailsDTO.getVendorCompanyProfileName())) {
			hasAcess = checkOpportunityAccessForVanityUrl(loggedInUserId, vanityUrlDetailsDTO, hasAcess);
		} else {
			boolean isTeamMember = teamDao.isTeamMember(loggedInUserId);
			boolean hasOpportunityAccess = utilDao.hasEnableLeadsAccessByUserId(loggedInUserId);
			boolean hasOpportunityAccessForPartner = utilDao.enableLeadsForPartner(loggedInUserId);
			boolean isSuperVisorOrAdmin = utilDao.isSuperVisorOrAdmin(loggedInUserId);
			if (isSuperVisorOrAdmin || !isTeamMember) {
				hasAcess = hasOpportunityAccess || hasOpportunityAccessForPartner;
			} else {
				hasAcess = checkIfHasOpportunityAccessForTeamMember(loggedInUserId);
			}
		}
		return hasAcess;
	}

	private boolean checkOpportunityAccessForVanityUrl(Integer loggedInUserId, VanityUrlDetailsDTO vanityUrlDetailsDTO,
			boolean hasAcess) {
		Integer companyId = userDao.getCompanyIdByProfileName(vanityUrlDetailsDTO.getVendorCompanyProfileName());
		if (XamplifyUtils.isValidInteger(companyId)) {
			boolean hasOpportunityAccess = utilDao.hasEnableLeadsAccessByCompanyId(companyId);
			if (hasOpportunityAccess) {
				boolean isTeamMember = teamDao.isTeamMember(loggedInUserId);
				boolean isSuperVisorOrAdmin = utilDao.isSuperVisorOrAdmin(loggedInUserId);
				if (isSuperVisorOrAdmin || !isTeamMember) {
					return hasOpportunityAccess;
				} else {
					hasAcess = checkIfHasOpportunityAccessForTeamMember(loggedInUserId);
				}
			}
		}
		return hasAcess;
	}

	private boolean checkIfHasOpportunityAccessForTeamMember(Integer loggedInUserId) {
		boolean hasAccess = false;
		List<Integer> roleIds = utilDao.findRoleIdsByUserId(loggedInUserId);
		if (roleIds != null && !roleIds.isEmpty()) {
			hasAccess = roleIds.indexOf(Role.OPPORTUNITY.getRoleId()) > -1;
		}
		return hasAccess;
	}

	private boolean checkIfHasAccessForAdd(VanityUrlDetailsDTO vanityUrlDetailsDTO, boolean hasAcess,
			Integer loggedInUserId, boolean hasOpportunityAccess) {
		if (hasOpportunityAccess) {
			boolean isVanityUrlFilterApplied = false;
			if (XamplifyUtils.isValidString(vanityUrlDetailsDTO.getVendorCompanyProfileName())) {
				isVanityUrlFilterApplied = true;
				vanityUrlDetailsDTO.setVanityUrlFilter(isVanityUrlFilterApplied);
				utilService.isVanityUrlFilterApplicable(vanityUrlDetailsDTO);
				if (vanityUrlDetailsDTO.isPartnerLoggedInThroughVanityUrl()) {
					hasAcess = true;
				} else if (vanityUrlDetailsDTO.isVendorLoggedInThroughOwnVanityUrl()) {
					hasAcess = checkIfHasAddAccessByRoles(loggedInUserId, isVanityUrlFilterApplied);
				}
			} else {
				hasAcess = checkIfHasAddAccessByRoles(loggedInUserId, isVanityUrlFilterApplied);
			}
		}
		return hasAcess;
	}

	private boolean checkIfHasAddAccessByRoles(Integer loggedInUserId, boolean isVanityUrlFilterApplied) {
		boolean hasAccesByRoles = false;
		boolean isPrmCompany = utilDao.isPrmCompany(loggedInUserId);
		boolean isPartnerCompany = utilDao.isPartnerCompany(loggedInUserId);
		boolean isHasPartnerRole = utilDao.isPartnerRole(loggedInUserId);
		boolean isTeamMember = teamDao.isTeamMember(loggedInUserId);
		if (!(isPrmCompany)) {
			hasAccesByRoles = true;
		} else if (!isVanityUrlFilterApplied && (isPartnerCompany || isHasPartnerRole)) {
			return true;
		} else if (isTeamMember) {
			hasAccesByRoles = checkIfHasOpportunityAccessForTeamMember(loggedInUserId);
		}
		return hasAccesByRoles;
	}

	@Override
	public XtremandResponse checkIfHasOpporunityAcess(VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		XtremandResponse response = new XtremandResponse();
		boolean hasOpportunityAccess = false;
		Integer loggedInUserId = vanityUrlDetailsDTO.getUserId();
		hasOpportunityAccess = checkIfHasOpportunityAccessForAdmin(loggedInUserId, vanityUrlDetailsDTO);
		response.setData(hasOpportunityAccess);
		response.setStatusCode(200);
		return response;
	}

	@Override
	public XtremandResponse findSendReminderTemplateForLead(Integer loggedInUserId, String companyProfileName,
			String emailId) {
		XtremandResponse response = new XtremandResponse();
		Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
		Integer partnerCompanyId = userDao.getCompanyIdByEmailId(emailId);

		List<String> partnerAssociatedTeamMembers = leadDAO.findPartnerAssosiatedTeamMembers(partnerCompanyId,
				companyId);
		List<String> filterTeamMembers = findFilterTeamMembers(companyId, partnerAssociatedTeamMembers);
		SendTestEmailDTO dto = new SendTestEmailDTO();
		dto.setToEmailIds(partnerAssociatedTeamMembers);
		dto.setCcEmailIds(filterTeamMembers);
		dto.setSubject("Test Subject");
		dto.setBody("Test Body");
		response.setStatusCode(200);
		response.setData(dto);
		response.setMessage("Successfully fetched lead status reminder template.");
		return response;
	}

	private List<String> findFilterTeamMembers(Integer companyId, List<String> partnerAssociatedTeamMembers) {
		if (XamplifyUtils.isNotEmptyList(partnerAssociatedTeamMembers)) {
			return (new ArrayList<>());
		}
		Pagination pagination = new Pagination();
		pagination.setPartnerCompanyId(companyId);
		List<TeamMemberDTO> teamMembersList = teamMemberAnalyticsDao.getTeamMemberInfoForFilter(pagination);
		return teamMembersList.stream().map(TeamMemberDTO::getEmailId).collect(Collectors.toList());
	}

	@Override
	public XtremandResponse sendReminderNotificationForLead(SendTestEmailDTO sendTestEmailDTO) {
		XtremandResponse response = new XtremandResponse();
		Integer loggedInUserId = sendTestEmailDTO.getLoggedInUserId();
		Lead lead = genericDAO.get(Lead.class, sendTestEmailDTO.getId());
		UserDTO senderUser = userDao.getFullNameAndEmailIdAndCompanyNameByUserId(loggedInUserId);
		String templateBody = replaceLeadsMergeTags("Lead Reminder Notification Body", lead, senderUser);

		for (String recipientEmail : sendTestEmailDTO.getToEmailIds()) {
			UserDTO teamMember = userDao.getDisplayNameByEmailId(recipientEmail);
			String teamMemberFullName = XamplifyUtils.isValidString(teamMember.getFullName()) ? teamMember.getFullName()
					: "There";
			String finalBody = templateBody.replace("{{teammemberFullName}}", teamMemberFullName);

			mailService.sendMail(new EmailBuilder().from(fromEmail).senderName(senderUser.getCompanyName())
					.to(recipientEmail).subject("Test Subject").body(finalBody).build());
		}

		response.setStatusCode(200);
		response.setMessage("Email sent successfully.");
		return response;
	}

	private String replaceLeadsMergeTags(String templateBody, Lead lead, UserDTO senderUser) {
		String body = templateBody;

		String partnerModuleCustomName = utilService
				.findPartnerModuleCustomNameByCompanyId(lead.getCreatedForCompany().getId());
		body = body.replace("{{partnerModuleCustomName}}",
				XamplifyUtils.isValidString(partnerModuleCustomName) ? partnerModuleCustomName : PARTNER);

		UserDTO partnerUser = userDao.getFullNameAndEmailIdAndCompanyNameByUserId(lead.getCreatedBy());
		String partnerName = XamplifyUtils.isValidString(partnerUser.getFullName()) ? partnerUser.getFullName() : "---";
		body = body.replace("{{partnerName}}", partnerName);
		body = body.replace("{{partnerCompany}}", partnerUser.getCompanyName());

		String leadName = XamplifyUtils.isValidString(mailService.getFullName(lead.getFirstName(), lead.getLastName()))
				? mailService.getFullName(lead.getFirstName(), lead.getLastName())
				: "---";
		body = body.replace("{{leadName}}", leadName);

		String leadCompany = XamplifyUtils.isValidString(lead.getCompany()) ? lead.getCompany() : "---";
		body = body.replace("{{leadCompany}}", leadCompany);

		body = replaceLeadStatusMergeTag(lead, body);

		String leadComment = leadDAO.findLatestLeadCommentByLead(lead);
		Pattern pattern = Pattern.compile("<b>Comment</b>:\\s*(.*)");
		Matcher matcher = pattern.matcher(leadComment);
		leadComment = matcher.find() ? matcher.group(1).trim() : leadComment;
		body = body.replace("{{leadComment}}", XamplifyUtils.isValidString(leadComment) ? leadComment : "---");

		String senderName = XamplifyUtils.isValidString(senderUser.getFullName()) ? senderUser.getFullName() : "There";
		body = body.replace("{{senderFullName}}", senderName);

		body = body.replace("{{senderCompanyName}}", senderUser.getCompanyName());

		return body;
	}

	private String replaceLeadStatusMergeTag(Lead lead, String body) {
		String leadStage = (lead.getCurrentStage() != null) ? lead.getCurrentStage().getStageName() : "---";
		String activeCRM = leadDAO.getActiveCRMTypeByCompanyId(lead.getCreatedForCompany().getId());
		boolean replaced = false;

		if ("salesforce".equalsIgnoreCase(activeCRM) && XamplifyUtils.isNotEmptyList(lead.getSfCustomFieldsData())) {
			for (SfCustomFieldsData sfcfData : lead.getSfCustomFieldsData()) {
				if (sfcfData.getFormLabel() != null && sfcfData.getFormLabel()
						.getFormDefaultFieldType() == FormDefaultFieldTypeEnum.PIPELINE_STAGE) {
					String value = sfcfData.getValue();
					body = body.replace("{{leadStage}}", (XamplifyUtils.isValidString(value)) ? value : leadStage);
					replaced = true;
					break;
				}
			}
		}

		if (!replaced) {
			body = body.replace("{{leadStage}}", leadStage);
		}
		return body;
	}

	@Override
	public ModuleAccess findCompanyAccess(Integer companyId, String companyProfileName) {
		return leadDAO.getCompanyAccess(companyId);
	}
	
	private Map<String, Object> fetchMcpLeadCustomForm(String accessToken) throws XamplifyDataAccessException {
		if (!XamplifyUtils.isValidString(accessToken)) {
			throw new IllegalArgumentException("accessToken is required");
		}

		String url = buildMcpCustomFormUrl();
		if (url.trim().isEmpty()) {
			throw new IllegalStateException("MCP custom form URL is empty");
		}

		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.AUTHORIZATION, BEARER + accessToken.trim());
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

		HttpEntity<Void> entity = new HttpEntity<>(headers);
		RestTemplate restTemplate = new RestTemplate();

		try {
			ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.GET, entity,
					new ParameterizedTypeReference<Map<String, Object>>() {
					});

			if (response.getStatusCode().is2xxSuccessful()) {
				return response.getBody();
			} else if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
				throw new XamplifyDataAccessException("Received 401 Unauthorized from MCP for URL: " + url);
			} else {
				throw new XamplifyDataAccessException("Unexpected response from MCP: " + response.getStatusCode() + " for URL: " + url);
			}
		} catch (HttpClientErrorException ex) {
			String body = ex.getResponseBodyAsString();
			throw new XamplifyDataAccessException("401 Unauthorized calling MCP. Response body: " + body, ex);
		} catch (RestClientResponseException ex) {
			String body = ex.getResponseBodyAsString();
			throw new XamplifyDataAccessException("Error calling MCP: HTTP " + ex.getRawStatusCode() + ", body: " + body, ex);
		} catch (Exception ex) {
			throw new XamplifyDataAccessException("Error calling MCP", ex);
		}
	}


	private IntegrationType resolveIntegrationType(Object integrationType) {
		if (integrationType instanceof String && !((String) integrationType).isEmpty()) {
			return IntegrationType.CUSTOM_CRM;
		}
		return IntegrationType.XAMPLIFY;
	}
	
	private FormTypeEnum resolveLeadFormType() {
		return FormTypeEnum.CRM_LEAD_CUSTOM_FORM;
	}
	
	private FormDTO buildLeadCustomFormDto(Integer companyId, FormTypeEnum formType, Object fields,
			Map<String, FormLabel> existingLabelsById, List<LeadField> defaultLeadFields,
			Map<String, LeadField> defaultLeadFieldsById) {
		CompanyProfile companyProfile = genericDAO.get(CompanyProfile.class, companyId);
		if (companyProfile == null) {
			return null;
		}

		FormDTO formDto = new FormDTO();
		String formNamePrefix = companyProfile.getCompanyName();
		if (!XamplifyUtils.isValidString(formNamePrefix)) {
			formNamePrefix = "Lead";
		}
		formDto.setName(formNamePrefix + " Lead Custom Form");
		formDto.setDescription(formDto.getName());
		formDto.setCompanyName(companyProfile.getCompanyName());
		formDto.setCompanyProfileName(companyProfile.getCompanyProfileName());
		formDto.setFormType(formType);
		Integer adminId = utilDao.findAdminIdByCompanyId(companyId);
		formDto.setCreatedBy(adminId);
		formDto.setUpdatedBy(adminId);

		List<FormLabelDTO> formLabelDTOs = buildLeadCustomFormLabels(fields, existingLabelsById, defaultLeadFields,
				defaultLeadFieldsById);
		if (formLabelDTOs.isEmpty()) {
			return null;
		}
		formDto.setFormLabelDTOs(formLabelDTOs);
		formDto.setFormLabelDTORows(utilService.constructFormRows(formLabelDTOs));
		return formDto;
	}
	
	@Override
	public XtremandResponse saveLeadCustomFormFromMcp(Integer userId) {
		XtremandResponse response = new XtremandResponse();
		if (!XamplifyUtils.isValidInteger(userId)) {
			return response;
		}
		Integer companyId = userDao.getCompanyIdByUserId(userId);
		if (!XamplifyUtils.isValidInteger(companyId)) {
			return response;
		}
		String pat = integrationDao.fetchActiveIntegrationPAT(companyId);
		if (!XamplifyUtils.isValidString(pat)) {
			return response;
		}
		Map<String, Object> formResponse;
		try {
			formResponse = fetchMcpLeadCustomForm(pat);
			if (formResponse == null || formResponse.isEmpty()) {
				return response;
			}
			IntegrationType integrationType = resolveIntegrationType(formResponse.get("integrationType"));
			FormTypeEnum formType = resolveLeadFormType();
			if (formType == null) {
				return response;
			}
			
			Integer formId = formDao.getSfCustomFormIdByCompanyIdAndFormType(companyId, formType);
			Form existingForm = XamplifyUtils.isValidInteger(formId) ? formDao.getById(formId) : null;
			Map<String, FormLabel> existingLabelsById = existingForm != null ? existingForm.getFormLabels().stream()
					.filter(Objects::nonNull)
					.filter(label -> label.getLabelId() != null && !label.getLabelId().isEmpty())
					.collect(Collectors.toMap(label -> label.getLabelId().trim(), Function.identity(), (a, b) -> a))
					: Collections.emptyMap();

			Object fields = formResponse.get("fields");
			List<LeadField> defaultLeadFields = Collections.emptyList();
			Map<String, LeadField> defaultLeadFieldsById = Collections.emptyMap();
			if (!(fields instanceof List<?>) || ((List<?>) fields).isEmpty()) {
				defaultLeadFields = leadDAO.getDefaultLeadFilds();
				defaultLeadFieldsById = defaultLeadFields.stream().filter(Objects::nonNull)
						.filter(field -> XamplifyUtils.isValidString(field.getLabelId()))
						.collect(Collectors.toMap(field -> field.getLabelId().trim(), Function.identity(), (a, b) -> a,
								LinkedHashMap::new));
			}

			FormDTO formDto = buildLeadCustomFormDto(companyId, formType, fields, existingLabelsById, defaultLeadFields,
					defaultLeadFieldsById);
			if (formDto == null) {
				return response;
			}

//			Integer formId = formDao.getSfCustomFormIdByCompanyIdAndFormType(companyId, formType);
			if (XamplifyUtils.isValidInteger(formId)) {
				Set<String> incomingLabelIds = new HashSet<>();
				if (formDto.getFormLabelDTOs() != null) {
					for (FormLabelDTO labelDTO : formDto.getFormLabelDTOs()) {
						if (labelDTO == null || labelDTO.getLabelId() == null || labelDTO.getLabelId().isEmpty()) {
							continue;
						}
						String labelId = labelDTO.getLabelId().trim();
						incomingLabelIds.add(labelId);
						FormLabel existingLabel = existingLabelsById.get(labelId);
						if (existingLabel != null) {
							labelDTO.setId(existingLabel.getId());
						}
					}
				}

				for (FormLabel existingLabel : existingLabelsById.values()) {
					if (existingLabel != null && existingLabel.getLabelId() != null
							&& !existingLabel.getLabelId().isEmpty()
							&& !incomingLabelIds.contains(existingLabel.getLabelId().trim())) {
						sfCustomFormDataDAO.deleteCustomFormLabelByFieldId(existingLabel.getId());
						formDao.deleteSfCustomLabelById(existingLabel.getId());
					}
				}
				formDto.setId(formId);
				formService.update(formDto, null);
			} else {
				formService.save(formDto, null);
			}
			response.setStatusCode(200);
			response.setMessage("Custom form sync completed successfully.");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}
	
	private String toString(Object value) {
		return value != null ? String.valueOf(value) : null;
	}
	
	private List<FormLabelDTO> buildLeadCustomFormLabels(Object fieldsObject) {
		if (!(fieldsObject instanceof List<?>)) {
			return Collections.emptyList();
		}
		List<FormLabelDTO> labels = new ArrayList<>();
		int order = 1;
		for (Object fieldObject : (List<?>) fieldsObject) {
			if (!(fieldObject instanceof Map<?, ?>)) {
				continue;
			}
			Map<?, ?> fieldMap = (Map<?, ?>) fieldObject;
			String labelId = toString(fieldMap.get("labelId"));
			if (!XamplifyUtils.isValidString(labelId)) {
				labelId = toString(fieldMap.get(LABEL_NAME));
			}
			if (!XamplifyUtils.isValidString(labelId)) {
				continue;
			}

			FormLabelDTO dto = new FormLabelDTO();
			dto.setLabelId(labelId);
			dto.setHiddenLabelId(toString(fieldMap.get("hiddenFieldName")));
			dto.setLabelName(toString(fieldMap.get(LABEL_NAME)));
			dto.setDisplayName(toString(fieldMap.get(LABEL_NAME)));
			dto.setLabelType(toString(fieldMap.get("labelType")));
			dto.setRequired(Boolean.TRUE.equals(fieldMap.get("required")));
			dto.setPlaceHolder(toString(fieldMap.get("placeholder")));
			dto.setDescription(toString(fieldMap.get("description")));
			dto.setOrder((Integer) fieldMap.get("order"));
			dto.setColumnOrder(order++);
			dto.setFormDefaultFieldType(null);
			dto.setActive(true);

			List<FormChoiceDTO> choiceDTOs = buildChoiceDtos(fieldMap.get("options"),
					fieldMap.get("defaultChoiceValue"));
			applyChoicesByType(dto, choiceDTOs);
			labels.add(dto);
		}
		return labels;
	}
	
	private void applyChoicesByType(FormLabelDTO dto, List<FormChoiceDTO> choiceDTOs) {
		if (choiceDTOs.isEmpty()) {
			return;
		}
		String labelType = dto.getLabelType() != null && !dto.getLabelType().isEmpty() ? dto.getLabelType().toLowerCase(Locale.ROOT)
				: "";
		switch (labelType) {
		case "checkbox":
			dto.setCheckBoxChoices(choiceDTOs);
			break;
		case "radio":
			dto.setRadioButtonChoices(choiceDTOs);
			break;
		default:
			dto.setDropDownChoices(choiceDTOs);
			break;
		}
	}
	
	private List<FormChoiceDTO> buildChoiceDtos(Object optionsObject, Object defaultChoiceObject) {
		if (!(optionsObject instanceof List<?>)) {
			return Collections.emptyList();
		}
		String defaultChoiceValue = toString(defaultChoiceObject);
		List<FormChoiceDTO> choices = new ArrayList<>();
		for (Object optionObject : (List<?>) optionsObject) {
			if (!(optionObject instanceof Map<?, ?>)) {
				continue;
			}
			Map<?, ?> optionMap = (Map<?, ?>) optionObject;
			FormChoiceDTO choiceDTO = new FormChoiceDTO();
			choiceDTO.setLabelId(toString(optionMap.get("value")));
			choiceDTO.setHiddenLabelId(toString(optionMap.get("hiddenValue")));
			choiceDTO.setName(toString(optionMap.get("label")));
			boolean isDefault = Boolean.TRUE.equals(optionMap.get("default"))
					|| (defaultChoiceValue != null && !defaultChoiceValue.isEmpty()
							&& defaultChoiceValue.equals(choiceDTO.getLabelId()));
			choiceDTO.setDefaultColumn(isDefault);
			choices.add(choiceDTO);
		}
		return choices;
	}
	
	private String buildMcpCustomFormUrl() {
		if (!baseUrl.endsWith("/")) {
			baseUrl = baseUrl + "/";
		}
		return baseUrl + "lead/custom-form";
//		return "http://localhost:8080/lead/custom-form";
	}
	
	private Map<String, Object> createPrmLead(String patToken, LeadDto leadDto) throws XamplifyDataAccessException {

	    if (!XamplifyUtils.isValidString(patToken)) {
	        throw new IllegalArgumentException("patToken is required");
	    }
	    if (leadDto == null) {
	        throw new IllegalArgumentException("leadDto is required");
	    }

		if (!baseUrl.endsWith("/")) {
			baseUrl = baseUrl + "/";
		}
	    String url = baseUrl + "lead/create";
//		String url = "http://localhost:8080/lead/create";

	    HttpHeaders headers = new HttpHeaders();
	    headers.set(HttpHeaders.AUTHORIZATION, BEARER + patToken.trim());
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

	    HttpEntity<LeadDto> requestEntity = new HttpEntity<>(leadDto, headers);
	    RestTemplate restTemplate = new RestTemplate();

	    try {
	        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
	                url,
	                HttpMethod.POST,
	                requestEntity,
	                new ParameterizedTypeReference<Map<String, Object>>() {}
	        );

	        HttpStatus status = response.getStatusCode();

	        if (status.is2xxSuccessful()) {
	            return response.getBody();
	        } else if (status == HttpStatus.UNAUTHORIZED) {
	            throw new XamplifyDataAccessException("Received 401 Unauthorized calling createPRMLead at URL: " + url);
	        } else if (status == HttpStatus.FORBIDDEN) {
	            throw new XamplifyDataAccessException("Received 403 Forbidden calling createPRMLead at URL: " + url);
	        } else {
	            throw new XamplifyDataAccessException("Unexpected status " + status.value()
	                    + " calling createPRMLead at URL: " + url);
	        }

	    } catch (HttpClientErrorException ex) {
	        String body = ex.getResponseBodyAsString();
	        throw new XamplifyDataAccessException("Client error calling createPRMLead. HTTP "
	                + ex.getStatusCode().value() + ", body: " + body, ex);

	    } catch (RestClientResponseException ex) {
	        String body = ex.getResponseBodyAsString();
	        throw new XamplifyDataAccessException("Error calling createPRMLead. HTTP "
	                + ex.getRawStatusCode() + ", body: " + body, ex);

	    } catch (Exception ex) {
	        throw new XamplifyDataAccessException("Unexpected error calling createPRMLead", ex);
	    }
	}

	@Override
	public void saveAndPushLeadToxAmplify(LeadDto leadDto) {
		if (leadDto != null && leadDto.getId() != null && leadDto.getId() > 0) {
			try {
				String pat = integrationDao.fetchActiveIntegrationPAT(leadDto.getCreatedForCompanyId());
				if (XamplifyUtils.isValidString(pat)) {
					UserDTO userDTO = userDao.getSendorCompanyDetailsByUserId(leadDto.getUserId());
					leadDto.setCreatedByCompanyName(userDTO.getCompanyName());
					leadDto.setPartnerCompanyId(userDTO.getCompanyId());
					leadDto.setCreatedByEmail(userDTO.getEmailId());
					leadDto.setCreatedByName(userDTO.getFullName());
					leadDto.setLeadId(leadDto.getId());
					leadDto.setExternalPipelineId(leadDto.getCreatedForPipelineId() + "");
					leadDto.setExternalPipelineStageId(leadDto.getCreatedForPipelineStageId() + "");
					leadDto.setPipelineName(leadDto.getCreatedForPipeline());
					leadDto.setPipelineStageName(leadDto.getCreatedForPipelineStage());
					createPrmLead(pat, leadDto);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
//	@SuppressWarnings({ "rawtypes", "unchecked" })
//	private Map<String, Object> fetchMcpLeadPipelines() {
//		String url = buildMcpLeadPipelinesUrl();
//		if (url.isEmpty()) {
//			return Collections.emptyMap();
//		}
//		HttpHeaders headers = new HttpHeaders();
//		headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + xAmplifyPat.trim());
//		RestTemplate restTemplate = new RestTemplate();
//		try {
//			ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers),
//					Map.class);
//			if (response != null && response.getStatusCode() == HttpStatus.OK) {
//				return response.getBody();
//			}
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//		return Collections.emptyMap();
//	}
	
	private Map<String, Object> fetchMcpLeadPipelines(String xAmplifyPat) throws XamplifyDataAccessException {

		String url = buildMcpLeadPipelinesUrl();
		if (url.trim().isEmpty()) {
			throw new IllegalStateException("MCP custom form URL is empty");
		}

		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.AUTHORIZATION, BEARER + xAmplifyPat.trim());
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

		HttpEntity<Void> entity = new HttpEntity<>(headers);
		RestTemplate restTemplate = new RestTemplate();

		try {
			ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.GET, entity,
					new ParameterizedTypeReference<Map<String, Object>>() {
					});

			if (response.getStatusCode().is2xxSuccessful()) {
				return response.getBody();
			} else if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
				throw new XamplifyDataAccessException("Received 401 Unauthorized from MCP for URL: " + url);
			} else {
				throw new XamplifyDataAccessException("Unexpected response from MCP: " + response.getStatusCode() + " for URL: " + url);
			}
		} catch (HttpClientErrorException ex) {
			String body = ex.getResponseBodyAsString();
			throw new XamplifyDataAccessException("401 Unauthorized calling MCP. Response body: " + body, ex);
		} catch (RestClientResponseException ex) {
			String body = ex.getResponseBodyAsString();
			throw new XamplifyDataAccessException("Error calling MCP: HTTP " + ex.getRawStatusCode() + ", body: " + body, ex);
		} catch (Exception ex) {
			throw new XamplifyDataAccessException("Error calling MCP", ex);
		}
	}
	
	@Override
	@Transactional
	public XtremandResponse saveLeadPipelinesFromMcp(Integer userId) {
		XtremandResponse response = new XtremandResponse();
		if (!XamplifyUtils.isValidInteger(userId)) {
			return response;
		}
		Integer companyId = userDao.getCompanyIdByUserId(userId);
		if (!XamplifyUtils.isValidInteger(companyId)) {
			return response;
		}
		String pat = integrationDao.fetchActiveIntegrationPAT(companyId);
		if (!XamplifyUtils.isValidString(pat)) {
			return response;
		}

		try {
			Map<String, Object> pipelineResponse = fetchMcpLeadPipelines(pat);
			if (pipelineResponse == null || pipelineResponse.isEmpty()) {
				response.setStatusCode(404);
				response.setMessage("Pipeline(s) not found.");
				return response;
			}

			Object pipelinesObject = pipelineResponse.get("pipelines");
			if (!(pipelinesObject instanceof List<?>)) {
				response.setStatusCode(404);
				response.setMessage("Pipeline(s) not found.");
				return response;
			}

			CompanyProfile companyProfile = genericDAO.get(CompanyProfile.class, companyId);
			if (companyProfile == null) {
				response.setStatusCode(404);
				response.setMessage("Company not found.");
				return response;
			}

			IntegrationType integrationType = resolveIntegrationType(pipelineResponse.get("integrationType"));
			if (integrationType == null || IntegrationType.XAMPLIFY.equals(integrationType)) {
				integrationType = IntegrationType.CUSTOM_CRM;
			}

			Integer adminId = utilDao.findAdminIdByCompanyId(companyId);
			List<Pipeline> existingPipelines = pipelineDAO.getPipelinesByIntegrationType(companyProfile.getId(),
					PipelineType.LEAD, integrationType, null);
			Map<String, Pipeline> pipelinesByExternalId = existingPipelines.stream()
					.filter(pipeline -> pipeline != null && XamplifyUtils.isValidString(pipeline.getExternalPipelineId()))
					.collect(Collectors.toMap(pipeline -> pipeline.getExternalPipelineId().trim(), Function.identity(),
							(a, b) -> a));
			Map<String, Pipeline> pipelinesByName = existingPipelines.stream()
					.filter(pipeline -> pipeline != null && XamplifyUtils.isValidString(pipeline.getName()))
					.collect(Collectors.toMap(pipeline -> pipeline.getName().trim(),
							Function.identity(), (a, b) -> a));

			Set<Integer> syncedPipelineIds = new HashSet<>();
			List<Pipeline> syncedPipelines = new ArrayList<>();
			for (Object pipelineObject : (List<?>) pipelinesObject) {
				Pipeline pipeline = upsertLeadPipeline(companyProfile, adminId, integrationType, pipelineObject,
						pipelinesByExternalId, pipelinesByName);
				if (pipeline != null && pipeline.getId() != null) {
					syncedPipelineIds.add(pipeline.getId());
					syncedPipelines.add(pipeline);
				}
			}

			Pipeline defaultPipeline = determineDefaultPipeline(syncedPipelines, adminId);
			List<Pipeline> pipelinesToRemove = existingPipelines.stream().filter(pipeline -> pipeline != null
					&& pipeline.getId() != null && !syncedPipelineIds.contains(pipeline.getId()))
					.collect(Collectors.toList());

			if (!pipelinesToRemove.isEmpty() && defaultPipeline != null) {
				pipelineDAO.reassignLeadPipelines(pipelinesToRemove, defaultPipeline);
				pipelineDAO.deletePipelineStages(pipelinesToRemove);
				pipelineDAO.deletePipelines(pipelinesToRemove);
			}

			response.setStatusCode(200);
			response.setMessage("Pipeline(s) sync completed successfully.");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}
	
	private Pipeline determineDefaultPipeline(List<Pipeline> pipelines, Integer adminId) {
		List<Pipeline> availablePipelines = pipelines.stream().filter(Objects::nonNull).collect(Collectors.toList());
		if (availablePipelines.isEmpty()) {
			return null;
		}

		Pipeline chosenDefault = null;
		for (Pipeline pipeline : availablePipelines) {
			if (pipeline.isDefault()) {
				if (chosenDefault == null) {
					chosenDefault = pipeline;
				} else {
					pipeline.setDefault(false);
					pipeline.initialiseCommonFields(false, adminId);
					genericDAO.update(pipeline);
				}
			}
		}

		if (chosenDefault == null) {
			chosenDefault = availablePipelines.get(0);
			chosenDefault.setDefault(true);
			chosenDefault.initialiseCommonFields(false, adminId);
			genericDAO.update(chosenDefault);
		}
		return chosenDefault;
	}
	
	private String buildMcpLeadPipelinesUrl() {
		if (!baseUrl.endsWith("/")) {
			baseUrl = baseUrl + "/";
		}
		return baseUrl + "lead/pipelines";
	}
	
	private Pipeline upsertLeadPipeline(CompanyProfile companyProfile, Integer adminId, IntegrationType integrationType,
			Object pipelineObject, Map<String, Pipeline> pipelinesByExternalId, Map<String, Pipeline> pipelinesByName) {
		if (!(pipelineObject instanceof Map<?, ?>)) {
			return null;
		}
		Map<?, ?> pipelineMap = (Map<?, ?>) pipelineObject;
		String externalPipelineId = toString(pipelineMap.get("pipelineId"));
		String pipelineName = toString(pipelineMap.get("pipelineName"));
		Pipeline pipeline = null;
		if (XamplifyUtils.isValidString(pipelineName)) {
			pipeline = pipelinesByName.get(pipelineName.trim());
		}
		if (pipeline == null && XamplifyUtils.isValidString(externalPipelineId)) {
			pipeline = pipelinesByExternalId.get(externalPipelineId.trim());
		}

		boolean isNew = pipeline == null;
		if (isNew) {
			pipeline = new Pipeline();
			pipeline.setCompany(companyProfile);
			pipeline.setType(PipelineType.LEAD);
			pipeline.setCreatedBy(adminId);
			pipeline.setPrivate(false);
		}

		boolean pipelineChanged = false;
		if (XamplifyUtils.isValidString(pipelineName) && !pipelineName.equals(pipeline.getName())) {
			pipeline.setName(pipelineName);
			pipelineChanged = true;
		}

		Boolean defaultPipeline = Boolean.TRUE.equals(pipelineMap.get("defaultPipeline"));
		if (pipeline.isDefault() != Boolean.TRUE.equals(defaultPipeline)) {
			pipeline.setDefault(defaultPipeline);
			pipelineChanged = true;
		}

		if (integrationType != null && !integrationType.equals(pipeline.getIntegrationType())) {
			pipeline.setIntegrationType(integrationType);
			pipelineChanged = true;
		}

		if (externalPipelineId != null && !externalPipelineId.equals(pipeline.getExternalPipelineId())) {
			pipeline.setExternalPipelineId(externalPipelineId);
			pipelineChanged = true;
		}

		if (isNew || pipelineChanged) {
			pipeline.initialiseCommonFields(isNew, adminId);
			if (isNew) {
				genericDAO.save(pipeline);
			} else {
				genericDAO.update(pipeline);
			}
		}

		upsertLeadPipelineStages(pipeline, pipelineMap.get("stages"), adminId);
		return pipeline;
	}
	
	private void upsertLeadPipelineStages(Pipeline pipeline, Object stagesObject, Integer adminId) {
		if (!(stagesObject instanceof List<?>)) {
			return;
		}
		int displayIndex = 1;
		List<PipelineStage> stages = pipeline.getStages();
		Integer maxId = 0;
		if (stages != null && !stages.isEmpty()) {
			maxId = stages.stream().max(Comparator.comparing(PipelineStage::getDisplayIndex)).get()
					.getDisplayIndex();
		}
		for (Object stageObject : (List<?>) stagesObject) {
			if (!(stageObject instanceof Map<?, ?>)) {
				continue;
			}
			Map<?, ?> stageMap = (Map<?, ?>) stageObject;
			String externalStageId = toString(stageMap.get("pipelineStageId"));
			PipelineStage stage = null;
			if (externalStageId != null && !externalStageId.isEmpty()) {
				stage = pipelineDAO.getPipelineStageByExternalPipelineStageId(pipeline.getCompany().getId(),
						pipeline.getId(), externalStageId, null);
			}
			
			String stageName = toString(stageMap.get("stageName"));
			if (stage == null && stageName != null && !stageName.isEmpty()) {
				stage = pipelineDAO.getPipelineStageByExternalPipelineStageId(pipeline.getCompany().getId(),
						pipeline.getId(), null, stageName);
			}

			boolean isNew = stage == null;
			if (isNew) {
				stage = new PipelineStage();
				stage.setPipeline(pipeline);
				stage.setCreatedBy(adminId);
			}

			boolean stageChanged = false;
			if (stageName != null && !stageName.isEmpty() && !stageName.equals(stage.getStageName())) {
				stage.setStageName(stageName);
				stageChanged = true;
			}
			if (XamplifyUtils.isValidString(externalStageId) && !externalStageId.equals(stage.getExternalPipelineStageId())) {
				stage.setExternalPipelineStageId(externalStageId);
				stageChanged = true;
			}

			Boolean defaultStage = Boolean.TRUE.equals(stageMap.get("defaultStage"));
			if (stage.isDefaultStage() != Boolean.TRUE.equals(defaultStage)) {
				stage.setDefaultStage(defaultStage);
				stageChanged = true;
			}

			int desiredDisplayIndex = displayIndex++;
			if (isNew || !Objects.equals(stage.getDisplayIndex(), desiredDisplayIndex)) {
				stage.setDisplayIndex(++maxId);
				stageChanged = true;
			}
			if (stage.isWon()) {
				stage.setWon(false);
				stageChanged = true;
			}
			if (stage.isLost()) {
				stage.setLost(false);
				stageChanged = true;
			}

			if (isNew || stageChanged) {
				stage.initialiseCommonFields(isNew, adminId);
				if (isNew) {
					genericDAO.save(stage);
				} else {
					genericDAO.update(stage);
				}
			}
		}
	}
	
	private void mergeDefaultLeadField(FormLabelDTO dto, LeadField defaultLeadField, int order) {
		if (!XamplifyUtils.isValidString(dto.getLabelName())) {
			dto.setLabelName(defaultLeadField.getLabelName());
		}
		if (!XamplifyUtils.isValidString(dto.getDisplayName())) {
			dto.setDisplayName(defaultLeadField.getLabelName());
		}
		if (!XamplifyUtils.isValidString(dto.getLabelType())) {
			dto.setLabelType(defaultLeadField.getLabelType());
		}
		if (dto.getOrder() == null) {
			dto.setOrder(order);
		}
		if (dto.getColumnOrder() == 0) {
			dto.setColumnOrder(order);
		}
	}
	
	private FormLabelDTO convertLeadFieldToDto(LeadField leadField, FormLabel existingLabel, int order) {
		FormLabelDTO dto = new FormLabelDTO();
		dto.setLabelId(leadField.getLabelId());
		dto.setHiddenLabelId(leadField.getLabelId());
		dto.setLabelName(leadField.getLabelName());
		dto.setDisplayName(leadField.getLabelName());
		String labelType = leadField.getLabelType();
		if ("Text Area".equals(labelType)) {
			labelType = "TextArea";
		}
		dto.setLabelType(labelType.toLowerCase());
		dto.setOrder(order);
		dto.setColumnOrder(order);
		dto.setRequired(false);
		dto.setActive(true);
		if (existingLabel != null) {
			dto.setId(existingLabel.getId());
			if (existingLabel.getFormDefaultFieldType() != null) {
				dto.setFormDefaultFieldType(existingLabel.getFormDefaultFieldType());
			}
			if (existingLabel.getFormLookUpDefaultFieldType() != null) {
				dto.setFormLookUpDefaultFieldType(existingLabel.getFormLookUpDefaultFieldType());
			}
			dto.setFormFieldType(existingLabel.getFormFieldType());
			dto.setPrivate(existingLabel.isPrivate());
			dto.setNonInteractive(existingLabel.isNonInteractive());
			dto.setEmailNotificationEnabledOnUpdate(existingLabel.isEmailNotificationEnabledOnUpdate());
			if (existingLabel.getDefaultChoice() != null) {
				dto.setDefaultChoiceId(existingLabel.getDefaultChoice().getId());
				dto.setDefaultChoiceLabel(existingLabel.getDefaultChoice().getLabelChoiceName());
			}
			applyChoicesByType(dto, convertFormLabelChoices(existingLabel.getFormLabelChoices()));
		}
		return dto;
	}
	
	private List<FormChoiceDTO> convertFormLabelChoices(List<FormLabelChoice> formLabelChoices) {
		List<FormChoiceDTO> choiceDTOs = new ArrayList<>();
		if (formLabelChoices == null) {
			return choiceDTOs;
		}
		for (FormLabelChoice choice : formLabelChoices) {
			if (choice == null) {
				continue;
			}
			FormChoiceDTO choiceDTO = new FormChoiceDTO();
			choiceDTO.setId(choice.getId());
			choiceDTO.setLabelId(choice.getLabelChoiceId());
			choiceDTO.setHiddenLabelId(choice.getLabelChoiceHiddenId());
			choiceDTO.setName(choice.getLabelChoiceName());
			choiceDTO.setDefaultColumn(choice.isDefaultColumn());
			choiceDTOs.add(choiceDTO);
		}
		return choiceDTOs;
	}
	
	private FormLabelDTO convertFormLabelToDto(FormLabel formLabel) {
		FormLabelDTO dto = new FormLabelDTO();
		dto.setId(formLabel.getId());
		dto.setLabelId(formLabel.getLabelId());
		dto.setHiddenLabelId(formLabel.getHiddenLabelId());
		dto.setLabelName(formLabel.getLabelName());
		dto.setDisplayName(formLabel.getDisplayName());
		dto.setLabelType(formLabel.getLabelType() != null ? formLabel.getLabelType().getLabelType() : null);
		dto.setPlaceHolder(formLabel.getPlaceHolder());
		dto.setOrder(formLabel.getOrder());
		dto.setRequired(formLabel.isRequired());
		dto.setDefaultColumn(formLabel.isDefaultColumn());
		dto.setDescription(formLabel.getDescription());
		dto.setFormDefaultFieldType(formLabel.getFormDefaultFieldType());
		dto.setColumnOrder(formLabel.getColumnOrder() != null ? formLabel.getColumnOrder() : 0);
		dto.setNonInteractive(formLabel.isNonInteractive());
		dto.setDefaultChoiceId(formLabel.getDefaultChoice() != null ? formLabel.getDefaultChoice().getId() : null);
		dto.setDefaultChoiceLabel(
				formLabel.getDefaultChoice() != null ? formLabel.getDefaultChoice().getLabelChoiceName() : null);
		dto.setPrivate(formLabel.isPrivate());
		dto.setFormLookUpDefaultFieldType(formLabel.getFormLookUpDefaultFieldType());
		dto.setFormFieldType(formLabel.getFormFieldType());
		dto.setActive(formLabel.isActive());
		dto.setEmailNotificationEnabledOnUpdate(formLabel.isEmailNotificationEnabledOnUpdate());
		applyChoicesByType(dto, convertFormLabelChoices(formLabel.getFormLabelChoices()));
		return dto;
	}
	
	private List<FormLabelDTO> buildLabelsFromDefaults(List<LeadField> defaultLeadFields,
			Map<String, FormLabel> existingLabels) {
		if (defaultLeadFields == null || defaultLeadFields.isEmpty()) {
			if (existingLabels == null || existingLabels.isEmpty()) {
				return Collections.emptyList();
			}
			return existingLabels.values().stream().filter(Objects::nonNull)
					.sorted(Comparator.comparing(FormLabel::getOrder, Comparator.nullsLast(Integer::compareTo)))
					.map(this::convertFormLabelToDto).collect(Collectors.toList());
		}
		Map<String, FormLabel> labelsById = existingLabels != null ? existingLabels : Collections.emptyMap();
		List<FormLabelDTO> formLabelDTOs = new ArrayList<>();
		int order = 1;
		for (LeadField leadField : defaultLeadFields) {
			if (leadField == null || !XamplifyUtils.isValidString(leadField.getLabelId())) {
				continue;
			}
			String labelId = leadField.getLabelId().trim();
			FormLabel existingLabel = labelsById.get(labelId);
			FormLabelDTO dto = convertLeadFieldToDto(leadField, existingLabel, order++);
			formLabelDTOs.add(dto);
		}
		return formLabelDTOs;
	}
	
	private List<FormLabelDTO> buildLeadCustomFormLabels(Object fieldsObject, Map<String, FormLabel> existingLabels,
			List<LeadField> defaultLeadFields, Map<String, LeadField> defaultLeadFieldsById) {
		Map<String, FormLabel> labelsById = existingLabels != null ? existingLabels : Collections.emptyMap();
		if (!(fieldsObject instanceof List<?>)) {
			return buildLabelsFromDefaults(defaultLeadFields, labelsById);
		}
		List<?> fieldList = (List<?>) fieldsObject;
		if (fieldList.isEmpty()) {
			return buildLabelsFromDefaults(defaultLeadFields, labelsById);
		}
		List<FormLabelDTO> labels = new ArrayList<>();
		int order = 1;
		for (Object fieldObject : fieldList) {
			if (!(fieldObject instanceof Map<?, ?>)) {
				continue;
			}
			Map<?, ?> fieldMap = (Map<?, ?>) fieldObject;
			String labelId = toString(fieldMap.get("labelId"));
			if (!XamplifyUtils.isValidString(labelId)) {
				labelId = toString(fieldMap.get(LABEL_NAME));
			}
			if (!XamplifyUtils.isValidString(labelId)) {
				continue;
			}

			FormLabelDTO dto = new FormLabelDTO();
			dto.setLabelId(labelId);
			dto.setHiddenLabelId(toString(fieldMap.get("hiddenFieldName")));
			dto.setLabelName(toString(fieldMap.get(LABEL_NAME)));
			dto.setDisplayName(toString(fieldMap.get(LABEL_NAME)));
			dto.setLabelType(toString(fieldMap.get("labelType")));
			Object requiredValue = fieldMap.get("required");
			dto.setRequired(requiredValue instanceof Boolean ? (Boolean) requiredValue : false);
			dto.setPlaceHolder(toString(fieldMap.get("placeholder")));
			dto.setDescription(toString(fieldMap.get("description")));
			dto.setOrder((Integer) fieldMap.get("order"));
			dto.setColumnOrder(order++);
			dto.setFormDefaultFieldType(null);
			dto.setActive(true);

			List<FormChoiceDTO> choiceDTOs = buildChoiceDtos(fieldMap.get("options"),
					fieldMap.get("defaultChoiceValue"));
			FormLabel existingLabel = labelsById.get(labelId);
			if (existingLabel != null) {
				dto.setId(existingLabel.getId());
				if (!XamplifyUtils.isValidString(dto.getLabelName())) {
					dto.setLabelName(existingLabel.getLabelName());
				}
				if (!XamplifyUtils.isValidString(dto.getDisplayName())) {
					dto.setDisplayName(existingLabel.getDisplayName());
				}
				if (!XamplifyUtils.isValidString(dto.getPlaceHolder())) {
					dto.setPlaceHolder(existingLabel.getPlaceHolder());
				}
				if (!XamplifyUtils.isValidString(dto.getDescription())) {
					dto.setDescription(existingLabel.getDescription());
				}
				if (dto.getOrder() == null) {
					dto.setOrder(existingLabel.getOrder());
				}
				if (dto.getColumnOrder() == 0 && existingLabel.getColumnOrder() != null) {
					dto.setColumnOrder(existingLabel.getColumnOrder());
				}
				if (dto.getLabelType() == null && existingLabel.getLabelType() != null) {
					dto.setLabelType(existingLabel.getLabelType().getLabelType());
				}
				if (requiredValue == null) {
					dto.setRequired(existingLabel.isRequired());
				}
				if (dto.getFormDefaultFieldType() == null) {
					dto.setFormDefaultFieldType(existingLabel.getFormDefaultFieldType());
				}
				dto.setPrivate(existingLabel.isPrivate());
				dto.setFormLookUpDefaultFieldType(existingLabel.getFormLookUpDefaultFieldType());
				dto.setFormFieldType(existingLabel.getFormFieldType());
				dto.setNonInteractive(existingLabel.isNonInteractive());
				dto.setEmailNotificationEnabledOnUpdate(existingLabel.isEmailNotificationEnabledOnUpdate());
				if (dto.getDefaultChoiceId() == null && existingLabel.getDefaultChoice() != null) {
					dto.setDefaultChoiceId(existingLabel.getDefaultChoice().getId());
					dto.setDefaultChoiceLabel(existingLabel.getDefaultChoice().getLabelChoiceName());
				}
				if (choiceDTOs.isEmpty()) {
					choiceDTOs = convertFormLabelChoices(existingLabel.getFormLabelChoices());
				}
			} else {
				LeadField defaultLeadField = defaultLeadFieldsById.get(labelId);
				if (defaultLeadField != null) {
					mergeDefaultLeadField(dto, defaultLeadField, order);
				}
			}
			applyChoicesByType(dto, choiceDTOs);
			labels.add(dto);
		}
		return labels;
	}
	
	@Override
	public void updateAndPushLeadToxAmplify(LeadDto leadDto) {
		if (leadDto != null && leadDto.getId() != null && leadDto.getId() > 0) {
			try {
				String pat = integrationDao.fetchActiveIntegrationPAT(leadDto.getCreatedForCompanyId());
				if (XamplifyUtils.isValidString(pat)) {
					populateLeadDtoForXamplifySync(leadDto);
					updatePrmLead(pat, leadDto);
				}
			} catch (Exception e) {
				LOGGER.error("Error while update and push lead to xAmplify", e);
//				e.printStackTrace();
			}
		}
	}
	
	private void populateLeadDtoForXamplifySync(LeadDto leadDto) {
		UserDTO userDTO = userDao.getSendorCompanyDetailsByUserId(leadDto.getUserId());
		if (userDTO != null) {
			leadDto.setCreatedByCompanyName(userDTO.getCompanyName());
			leadDto.setPartnerCompanyId(userDTO.getCompanyId());
			leadDto.setCreatedByEmail(userDTO.getEmailId());
			leadDto.setCreatedByName(userDTO.getFullName());
		}
		leadDto.setLeadId(leadDto.getId());
		PipelineDto pipelineDto = pipelineDAO.fetchPipelineDetailsByPipelineId(leadDto.getCreatedForPipelineId());
		PipelineStageDto pipelineStageDto = pipelineDAO.fetchPipelineStageDetailsByPipelineStageId(leadDto.getCreatedForPipelineStageId());
		leadDto.setExternalPipelineId(pipelineDto.getExternalPipelineId());
		leadDto.setExternalPipelineStageId(pipelineStageDto.getExternalPipelineStageId());
		leadDto.setPipelineName(pipelineDto.getName());
		leadDto.setPipelineStageName(pipelineStageDto.getStageName());
	}
	
	private Map<String, Object> updatePrmLead(String patToken, LeadDto leadDto) throws XamplifyDataAccessException {

		if (!XamplifyUtils.isValidString(patToken)) {
			throw new IllegalArgumentException("patToken is required");
		}
		if (leadDto == null) {
			throw new IllegalArgumentException("leadDto is required");
		}

		if (!baseUrl.endsWith("/")) {
			baseUrl = baseUrl + "/";
		}
		String url = baseUrl + "lead/update";

		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.AUTHORIZATION, BEARER + patToken.trim());
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

		HttpEntity<LeadDto> requestEntity = new HttpEntity<>(leadDto, headers);
		RestTemplate restTemplate = new RestTemplate();

		try {
			ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity,
					new ParameterizedTypeReference<Map<String, Object>>() {
					});

			HttpStatus status = response.getStatusCode();

			if (status.is2xxSuccessful()) {
				return response.getBody();
			} else if (status == HttpStatus.UNAUTHORIZED) {
				throw new XamplifyDataAccessException("Received 401 Unauthorized calling updatePRMLead at URL: " + url);
			} else if (status == HttpStatus.FORBIDDEN) {
				throw new XamplifyDataAccessException("Received 403 Forbidden calling updatePRMLead at URL: " + url);
			} else {
				throw new XamplifyDataAccessException(
						"Unexpected status " + status.value() + " calling updatePRMLead at URL: " + url);
			}

		} catch (HttpClientErrorException ex) {
			String body = ex.getResponseBodyAsString();
			throw new XamplifyDataAccessException(
					"Client error calling updatePRMLead. HTTP " + ex.getStatusCode().value() + ", body: " + body, ex);

		} catch (RestClientResponseException ex) {
			String body = ex.getResponseBodyAsString();
			throw new XamplifyDataAccessException(
					"Error calling updatePRMLead. HTTP " + ex.getRawStatusCode() + ", body: " + body, ex);

		} catch (Exception ex) {
			throw new XamplifyDataAccessException("Unexpected error calling updatePRMLead", ex);
		}
	}
	
	@Override
	public XtremandResponse syncLeads(Integer userId, Integer companyId) {
		XtremandResponse response = new XtremandResponse();
		if (!XamplifyUtils.isValidInteger(companyId) || !XamplifyUtils.isValidInteger(userId)) {
			response.setStatusCode(400);
			response.setMessage(INVALID_INPUT);
			return response;
		}

		CompanyProfile companyProfile = userService.getCompanyProfileByUser(userId);
		if (companyProfile == null || companyProfile.getId() == null
				|| !companyProfile.getId().equals(companyId)) {
			response.setStatusCode(401);
			response.setMessage(UNAUTHORIZED);
			return response;
		}

		List<Lead> leads = leadDAO.findLeadsByCreatedForCompanyId(companyId);
		if (leads == null || leads.isEmpty()) {
			response.setStatusCode(200);
			response.setMessage(SUCCESS);
			response.setData(Collections.emptyMap());
			return response;
		}

		Integration activeCRMIntegration = integrationDao.getActiveCRMIntegration(companyId);
		IntegrationType activeIntegrationType = activeCRMIntegration != null ? activeCRMIntegration.getType() : null;
		Integration otherActiveCRMIntegration = null;

		int synced = 0;
		int failed = 0;
		for (Lead lead : leads) {
			try {
				String openSourceLeadId = lead.getId().toString();
				if (!XamplifyUtils.isValidString(openSourceLeadId)) {
					failed++;
					continue;
				}

				LeadSyncDetailsDto leadDetails = fetchOpenSourceLead(openSourceLeadId, activeCRMIntegration.getAccessToken());
				applyLeadUpdates(lead, leadDetails);
				updateLeadPipeline(lead, leadDetails, companyId, activeIntegrationType);
				updateSfCustomFieldsData(lead, leadDetails, companyId, activeIntegrationType,
						otherActiveCRMIntegration);
				genericDAO.update(lead);
				synced++;
			} catch (Exception ex) {
				failed++;
				LOGGER.error("Error syncing lead {} for company {}", lead.getId(), companyId, ex);
			}
		}

		Map<String, Integer> syncStatus = new HashMap<>();
		syncStatus.put("synced", synced);
		syncStatus.put("failed", failed);
		response.setStatusCode(200);
		response.setMessage(SUCCESS);
		response.setData(syncStatus);
		return response;
	}
	
	private LeadSyncDetailsDto fetchOpenSourceLead(String openSourceLeadId, String xAmplifyPat) {
		if (!XamplifyUtils.isValidString(openSourceLeadId)) {
			throw new IllegalArgumentException("openSourceLeadId is required");
		}

		String url = baseUrl + "lead/getById/" + openSourceLeadId.trim();
//		String url = "http://localhost:8080/lead/getById/" + openSourceLeadId.trim();
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		if (StringUtils.isNotBlank(xAmplifyPat)) {
			headers.set(HttpHeaders.AUTHORIZATION, BEARER + xAmplifyPat.trim());
		}

		HttpEntity<Void> entity = new HttpEntity<>(headers);
		RestTemplate restTemplate = new RestTemplate();

		ResponseEntity<Map<String, LeadSyncDetailsDto>> response = restTemplate.exchange(url, HttpMethod.GET, entity,
				new ParameterizedTypeReference<Map<String, LeadSyncDetailsDto>>() {
				});
		HttpStatus status = response.getStatusCode();

		if (status.is2xxSuccessful() && response.getBody() != null) {
			if (response.getBody().containsKey("details")) {
				return response.getBody().get("details");
			}
		}

		String message = "Failed to fetch lead details from open source for id: " + openSourceLeadId;
		throw new XamplifyDataAccessException(message);
	}
	
	private void applyLeadUpdates(Lead lead, LeadSyncDetailsDto leadDetails) {
		if (leadDetails == null || lead == null) {
			return;
		}

		setIfPresent(leadDetails.getFirstName(), lead::setFirstName);
		setIfPresent(leadDetails.getLastName(), lead::setLastName);
		setIfPresent(leadDetails.getEmail(), lead::setEmail);
		setIfPresent(leadDetails.getPhone(), lead::setPhone);
		setIfPresent(leadDetails.getCompany(), lead::setCompany);
		setIfPresent(leadDetails.getWebsite(), lead::setWebsite);
		setIfPresent(leadDetails.getTitle(), lead::setTitle);
		setIfPresent(leadDetails.getIndustry(), lead::setIndustry);
		setIfPresent(leadDetails.getStreet(), lead::setStreet);
		setIfPresent(leadDetails.getCity(), lead::setCity);
		setIfPresent(leadDetails.getState(), lead::setState);
		setIfPresent(leadDetails.getRegion(), lead::setRegion);
		setIfPresent(leadDetails.getCountry(), lead::setCountry);
		setIfPresent(leadDetails.getPostalCode(), lead::setPostalCode);
		if (StringUtils.isNotBlank(leadDetails.getReferenceId())) {
			lead.setReferenceId(leadDetails.getReferenceId());
		}
	}

	private void setIfPresent(String value, Consumer<String> consumer) {
		if (value != null) {
			consumer.accept(value);
		}
	}

	private void updateLeadPipeline(Lead lead, LeadSyncDetailsDto leadDetails, Integer companyId,
			IntegrationType activeIntegrationType) {
		if (lead == null || leadDetails == null) {
			return;
		}

		Pipeline pipeline = null;
		if (XamplifyUtils.isValidString(leadDetails.getExternalPipelineId()) && activeIntegrationType != null) {
			pipeline = pipelineDAO.getLeadPipelineByExternalPipelineId(companyId, leadDetails.getExternalPipelineId(),
					activeIntegrationType);
		}

		if (pipeline == null && XamplifyUtils.isValidString(leadDetails.getPipelineName())) {
			pipeline = pipelineDAO.getPipeLineByName(companyId, leadDetails.getPipelineName(), PipelineType.LEAD);
		}

		if (pipeline != null) {
			lead.setPipeline(pipeline);
			lead.setCreatedForPipeline(pipeline);
			PipelineStage stage = pipelineDAO.getPipelineStageByExternalPipelineStageId(companyId, pipeline.getId(),
					leadDetails.getExternalPipelineStageId(), leadDetails.getPipelineStageName());
			if (stage == null) {
				stage = pipelineDAO.getDefaultStage(pipeline.getId());
			}
			if (stage == null) {
				stage = pipelineDAO.findFallbackStage(pipeline.getId());
			}
			if (stage != null) {
				lead.setCurrentStage(stage);
				lead.setCreatedForPipelineStage(stage);
			}
		}
	}
	
	private Map<String, String> buildCustomFieldValues(LeadSyncDetailsDto leadDetails) {
		Map<String, String> customFieldValues = new HashMap<>();
		if (leadDetails != null && leadDetails.getSfCustomFieldsData() != null) {
			for (SfCustomFieldsDataDTO dataDTO : leadDetails.getSfCustomFieldsData()) {
				if (dataDTO != null && StringUtils.isNotBlank(dataDTO.getSfCfLabelId())) {
					String value = dataDTO.getValue();
					if (value == null && StringUtils.isNotBlank(dataDTO.getSelectedChoiceValue())) {
						value = dataDTO.getSelectedChoiceValue();
					}
					if (value == null && StringUtils.isNotBlank(dataDTO.getDateTimeIsoValue())) {
						value = dataDTO.getDateTimeIsoValue();
					}
					customFieldValues.put(dataDTO.getSfCfLabelId(), value);
				}
			}
		}
		return customFieldValues;
	}
	
	private void saveOrUpdateSfCustomFieldsData(Lead lead, Map<String, String> customFieldValues, FormLabel formLabel,
			FormLabel formLabelInOtherForm) {
		SfCustomFieldsData sfCustomFieldsData = sfCustomFormDataDAO.getSfCustomFieldDataByLeadIdAndLabelId(lead,
				formLabel);
		String value = null;
		boolean saveOrUpdate = true;
		if (formLabelInOtherForm != null) {
			value = customFieldValues.get(formLabelInOtherForm.getLabelId());
			Map<String, Object> customFieldsDataMap = utilService.getSfCustomFieldsDataValue(value, formLabel,
					formLabelInOtherForm);
			value = (String) customFieldsDataMap.get("value");
			saveOrUpdate = (boolean) customFieldsDataMap.get("saveOrUpdate");
		} else {
			value = customFieldValues.get(formLabel.getLabelId());
		}

		if (saveOrUpdate) {
			if (sfCustomFieldsData != null) {
				sfCustomFieldsData.setFormLabel(formLabel);
				sfCustomFieldsData.setValue(value);
				sfCustomFormDataDAO.updateSfCfData(sfCustomFieldsData);
			} else {
				sfCustomFieldsData = new SfCustomFieldsData();
				sfCustomFieldsData.setLead(lead);
				sfCustomFieldsData.setFormLabel(formLabel);
				sfCustomFieldsData.setValue(value);
				sfCustomFormDataDAO.saveSfCfData(sfCustomFieldsData);
			}
		}

	}

	@Override
	public void updateSfCustomFieldsData(Lead lead, JSONObject leadJson, Integer activeCRMCompanyId,
			IntegrationType activeCRMIntegrationType, Integration otherActiveCRMIntegration) {
		if (activeCRMCompanyId != null && activeCRMCompanyId > 0 && activeCRMIntegrationType != null) {
			List<FormLabel> activeCRMCFLabels = utilService.getLeadCustomFormLabelsByIntegrationType(activeCRMCompanyId,
					activeCRMIntegrationType);

			List<FormLabel> otherActiveCRMCFLabels = new ArrayList<>();
			if (otherActiveCRMIntegration != null) {
				otherActiveCRMCFLabels = utilService.getLeadCustomFormLabelsByIntegrationType(
						otherActiveCRMIntegration.getCompany().getId(), otherActiveCRMIntegration.getType());
			}

			for (FormLabel formLabel : activeCRMCFLabels) {
				if (formLabel != null) {
					saveOrUpdateSfCustomFieldsData(lead, leadJson, formLabel, null);
					FormLabel matchingFormLabel = utilService.getMatchedObject(formLabel, otherActiveCRMCFLabels);
					if (matchingFormLabel != null) {
						saveOrUpdateSfCustomFieldsData(lead, leadJson, matchingFormLabel, formLabel);
					}
				}
			}
		}
	}

	@Override
	public XtremandResponse syncDetailsWithXamplify(Integer userId) {
		XtremandResponse response = new XtremandResponse();

		try {
			if (!XamplifyUtils.isValidInteger(userId)) {
				response.setStatusCode(400);
				response.setMessage(INVALID_INPUT);
				return response;
			}

			Integer companyId = userDao.getCompanyIdByUserId(userId);
			if (!XamplifyUtils.isValidInteger(companyId)) {
				response.setStatusCode(400);
				response.setMessage("Invalid company for given user.");
				return response;
			}

			response = saveLeadPipelinesFromMcp(userId);
			if (!isSuccess(response)) {
				return response;
			}

			response = syncLeads(userId);
			if (!isSuccess(response)) {
				return response;
			}

			response = dealService.saveDealPipelinesFromMcp(userId);
			if (!isSuccess(response)) {
				return response;
			}

			response = dealService.syncDeals(userId, companyId);
			return response != null ? response : buildServerError("Sync failed.");

		} catch (Exception ex) {
			return buildServerError("Unexpected error while syncing details with Xamplify");
		}
	}

	private boolean isSuccess(XtremandResponse response) {
		return response != null && response.getStatusCode() == 200;
	}

	private XtremandResponse buildServerError(String message) {
		XtremandResponse error = new XtremandResponse();
		error.setStatusCode(500);
		error.setMessage(message);
		return error;
	}

	@Override
	public XtremandResponse syncLeads(Integer userId) {
		XtremandResponse response = new XtremandResponse();
		if (!XamplifyUtils.isValidInteger(userId)) {
			response.setStatusCode(400);
			response.setMessage(INVALID_INPUT);
			return response;
		}
		Integer companyId = userDao.getCompanyIdByUserId(userId);
		return syncLeads(userId, companyId);
	}

}
