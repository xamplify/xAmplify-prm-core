package com.xtremand.deal.service.impl;

import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
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
import com.xtremand.campaign.bom.DownloadDataInfo;
import com.xtremand.campaign.bom.DownloadDataInfo.DownloadItem;
import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.Criteria;
import com.xtremand.common.bom.Criteria.OPERATION_NAME;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.common.bom.Pagination;
import com.xtremand.custom.field.dto.FieldLabelType;
import com.xtremand.customfields.dao.CustomFieldsDao;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.deal.bom.Deal;
import com.xtremand.deal.bom.DealStage;
import com.xtremand.deal.dao.DealDAO;
import com.xtremand.deal.dto.DealCountsResponseDTO;
import com.xtremand.deal.dto.DealDto;
import com.xtremand.deal.dto.DealStatusUpdateRequest;
import com.xtremand.deal.dto.DealSyncDetailsDto;
import com.xtremand.deal.dto.VendorSelfDealRequestDTO;
import com.xtremand.deal.service.DealService;
import com.xtremand.flexi.fields.dao.FlexiFieldDao;
import com.xtremand.flexi.fields.dto.FlexiFieldRequestDTO;
import com.xtremand.form.bom.Form;
import com.xtremand.form.bom.FormLabel;
import com.xtremand.form.bom.FormTypeEnum;
import com.xtremand.form.dao.FormDao;
import com.xtremand.form.dto.FormChoiceDTO;
import com.xtremand.form.dto.FormDTO;
import com.xtremand.form.dto.FormLabelDTO;
import com.xtremand.form.service.FormService;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.formbeans.UserListDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.gdpr.setting.service.GdprSettingService;
import com.xtremand.integration.bom.Integration;
import com.xtremand.integration.bom.Integration.IntegrationType;
import com.xtremand.integration.dao.IntegrationDao;
import com.xtremand.integration.dto.IntegrationSettingsDTO;
import com.xtremand.lead.bom.Lead;
import com.xtremand.lead.bom.LeadApprovalStatusEnum;
import com.xtremand.lead.bom.LeadStage;
import com.xtremand.lead.bom.Pipeline;
import com.xtremand.lead.bom.PipelineStage;
import com.xtremand.lead.bom.PipelineType;
import com.xtremand.lead.dto.LeadDto;
import com.xtremand.lead.dto.LeadSyncDetailsDto;
import com.xtremand.lead.dto.PipelineDto;
import com.xtremand.lead.dto.PipelineStageDto;
import com.xtremand.lead.service.LeadService;
import com.xtremand.mail.service.AsyncService;
import com.xtremand.mail.service.MailService;
import com.xtremand.partnership.dao.PartnershipDAO;
import com.xtremand.pipeline.dao.PipelineDAO;
import com.xtremand.salesforce.bom.SfCustomFieldsData;
import com.xtremand.salesforce.dto.OpportunityFormFieldsDTO;
import com.xtremand.salesforce.dto.PicklistValues;
import com.xtremand.salesforce.dto.SfCustomFieldsDataDTO;
import com.xtremand.sf.cf.data.dao.SfCustomFormDataDAO;
import com.xtremand.team.dao.TeamDao;
import com.xtremand.user.bom.Role;
import com.xtremand.user.bom.User;
import com.xtremand.user.bom.UserList;
import com.xtremand.user.bom.UserListUsersView;
import com.xtremand.user.bom.UserUserList;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.user.service.UserService;
import com.xtremand.userlist.dao.UserListDAO;
import com.xtremand.util.DateUtils;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.HibernateSQLQueryResultUtilDao;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.NumberFormatterString;
import com.xtremand.util.dto.Pageable;
import com.xtremand.util.dto.QueryParameterDTO;
import com.xtremand.util.dto.SearchableDropDownDTO;
import com.xtremand.util.dto.TeamMemberFilterDTO;
import com.xtremand.util.dto.XamplifyConstants;
import com.xtremand.util.service.CsvUtilService;
import com.xtremand.util.service.UtilService;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;

@Service("DealService")
@Transactional
public class DealServiceImpl implements DealService {
	private static final String BEARER = "Bearer ";

	private static final String TOTAL_RECORDS = "totalRecords";

	private static final String UNAUTHORIZED = "UnAuthorized";
	private static final String SUCCESS = "Success";
	private static final String FAILED = "Failed";
	private static final String INVALID_INPUT = "Invalid Input";

	private static final String CREATED_BY_COMPANY_ID = XamplifyConstants.CREATED_BY_COMPANY_ID;

	private static final String DEAL_AND_USER_PROFILE = " from xt_deal xl,xt_user_profile xup";

	private static final String USER_DETAILS_QUERY_PREFIX = "select distinct xup.user_id as \"id\",case when LENGTH(TRIM(concat(TRIM(xup.firstname), ' ', TRIM(xup.lastname))))>0  \r\n"
			+ "then TRIM(concat(TRIM(xup.email_id),' - ( ',TRIM(xup.firstname), ' ', TRIM(xup.lastname),' )')) else xup.email_id end as \"name\" ";

	private static final String USER_DETAILS_QUERY_STRING = USER_DETAILS_QUERY_PREFIX + DEAL_AND_USER_PROFILE
			+ " where xup.user_id  = xl.created_by \r\n";
	private static final String LABEL_NAME = "labelName";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DealServiceImpl.class);

	@Autowired
	private UserService userService;

	@Autowired
	private UserDAO userDao;

	@Autowired
	private GenericDAO genericDAO;

	@Autowired
	private IntegrationDao integrationDao;

	@Autowired
	private PartnershipDAO partnershipDAO;

	@Autowired
	private UtilService utilService;

	@Autowired
	private DealDAO dealDAO;

	@Autowired
	private MailService mailService;

	@Autowired
	private FormDao formDao;

	@Autowired
	private UserListDAO userListDAO;

	@Autowired
	GdprSettingService gdprSettingService;

	@Autowired
	private PipelineDAO pipelineDAO;

	@Autowired
	private TeamDao teamDao;

	@Autowired
	private CsvUtilService csvUtilService;

	@Autowired
	@Lazy
	private AsyncService asyncService;

	@Autowired
	private UtilDao utilDao;

	@Autowired
	private HibernateSQLQueryResultUtilDao hibernateSQLQueryResultUtilDao;

	@Autowired
	private FlexiFieldDao flexiFieldDao;

	@Autowired
	private SfCustomFormDataDAO sfCustomFormDataDAO;

	@Autowired
    private FormService formService;

	@Value("${server_path}")
	String server_path;

	@Value("${deal.contact.list.name}")
	private String DEAL_CONTACT_LIST_NAME;

	@Value("${salesforce.pipeline.name}")
	private String SALESFORCE_PIPELINE_NAME;

	@Value("${default.lead.pipeline.name}")
	private String DEFAULT_LEAD_PIPELINE_NAME;

	@Value("${default.deal.pipeline.name}")
	private String DEFAULT_DEAL_PIPELINE_NAME;

	@Value("${LeadAttachmentFailure}")
	private String LEAD_ATTACHMENT_FAILURE;
	
	@Value("${xAmplify.base.url}")
    private String baseUrl;
    
    @Autowired
    private CustomFieldsDao customFieldsDao;

	@Override
	public XtremandResponse saveDeal(DealDto dealDto) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		Integer responseDealId = 0;
		if (validateSaveDealRequest(dealDto)) {
			Integer loggedInUserId = dealDto.getUserId();
			CompanyProfile loggedInCompany = userService.getCompanyProfileByUser(loggedInUserId);
			if (loggedInCompany != null && isAuthorizedUser(loggedInUserId)) {
				if (validateLeadAttachment(dealDto.getAssociatedLeadId())) {
					Deal deal = createDeal(dealDto, loggedInUserId, loggedInCompany);
					if (deal != null) {
						fillIntegrationDetailsInDeal(deal);
						pushDealToActiveCRM(deal, dealDto);
						mailService.sendDealOpenedMail(deal);
						responseMessage = SUCCESS;
						responseStatusCode = 200;
						responseDealId = deal.getId();
					}
				} else {
					responseMessage = LEAD_ATTACHMENT_FAILURE;
					responseStatusCode = 500;
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
		response.setData(responseDealId);
		return response;
	}

	private void pushDealToActiveCRM(Deal deal, DealDto dealDto) {
	
			deal.setPushToCRMUserId(deal.getForCompanyUserId());
			deal.setPushToCRMStage(deal.getCreatedForPipelineStage());
			pushDealToActiveCRM(deal, dealDto, deal.getCreatedForCompany().getId());
	}

	private boolean pushToMultipleCRMs(Deal deal) {
		boolean pushToMultipleCRMs = false;
		return pushToMultipleCRMs;
	}

	private void prepareCreatedForCustomFieldsData(Deal deal, DealDto dealDto) {
		if (!deal.getCreatedByCompany().getId().equals(deal.getCreatedForCompany().getId())) {
			deal.setPushToCRMUserId(deal.getForCompanyUserId());
			deal.setPushToCRMStage(deal.getCreatedForPipelineStage());
			prepareCustomFieldsData(deal, dealDto, deal.getCreatedForCompany().getId(),
					deal.getCreatedByCompany().getId());
		}
	}

	private void prepareCreatedByCustomFieldsData(Deal deal, DealDto dealDto) {
		deal.setPushToCRMUserId(deal.getCreatedBy());
		deal.setPushToCRMStage(deal.getCreatedByPipelineStage());
		prepareCustomFieldsData(deal, dealDto, deal.getCreatedByCompany().getId(), deal.getCreatedForCompany().getId());
	}

	private void pushDealToCreatedForActiveCRM(Deal deal, DealDto dealDto) {
		if (!deal.getCreatedByCompany().getId().equals(deal.getCreatedForCompany().getId())) {
			deal.setPushToCRMUserId(deal.getForCompanyUserId());
			deal.setPushToCRMStage(deal.getCreatedForPipelineStage());
		}
	}

	private void pushDealToCreatedByActiveCRM(Deal deal, DealDto dealDto) {
		deal.setPushToCRMUserId(deal.getCreatedBy());
		deal.setPushToCRMStage(deal.getCreatedByPipelineStage());
	}

	private void prepareCustomFieldsData(Deal deal, DealDto dealDto, Integer pushDealCompanyId,
			Integer comparingCompanyId) {
	}


	private void pushDealToActiveCRM(Deal deal, DealDto dealDto, Integer companyId) {
		Integration activeCRMIntegration = integrationDao.getActiveCRMIntegration(companyId);
		if (activeCRMIntegration == null && deal.getCreatedByCompany() != null) {
			saveOrUpdateSfCustomFieldsData(deal, dealDto.getSfCustomFieldsDataDto(), IntegrationType.XAMPLIFY);
		} else {
			saveOrUpdateSfCustomFieldsData(deal, dealDto.getSfCustomFieldsDataDto(), IntegrationType.CUSTOM_CRM);
		}
	}
	
	private void fillIntegrationDetailsInDeal(Deal deal) {
		List<User> forUserIds = partnershipDAO.getOwners(deal.getCreatedForCompany().getId());
		if (forUserIds != null && !forUserIds.isEmpty()) {
			deal.setForCompanyUserId(forUserIds.get(0).getUserId());
		}
		setRole(deal);
		fillCreatedByDetails(deal);
	}

	private void setRole(Deal deal) {
		deal.setOldRole(deal.getRole());
		String role = "";
		if (deal.getTitle().length() > 10) {
			role = deal.getTitle().substring(0, 10);
		} else {
			role = deal.getTitle();
		}

		if (deal.getAssociatedLead() != null) {
			role = role + "-" + deal.getAssociatedLead().getLastName();
		}
		deal.setRole(role);
	}

	private void fillCreatedByDetails(Deal deal) {
		User createdUser = userService.loadUser(
				Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, deal.getCreatedBy())),
				new FindLevel[] { FindLevel.COMPANY_PROFILE });
		if (createdUser != null) {
			String name = "";
			if (createdUser.getFirstName() != null) {
				name = createdUser.getFirstName() + " ";
			}
			if (createdUser.getLastName() != null) {
				name = name + createdUser.getLastName();
			}

			deal.setCreatedByName(name);
			deal.setCreatedByEmail(createdUser.getEmailId());
			if (createdUser.getCompanyProfile() != null) {
				deal.setCreatedByCompanyName(createdUser.getCompanyProfile().getCompanyName());
			}
		}
	}

	private Deal createDeal(DealDto dealDto, Integer loggedInUserId, CompanyProfile loggedInCompany) {
		Deal deal = new Deal();

		if (dealDto.getCreatedForCompanyId() != null && dealDto.getCreatedForCompanyId() > 0) {
			CompanyProfile forCompany = genericDAO.get(CompanyProfile.class, dealDto.getCreatedForCompanyId());
			if (forCompany != null) {
				deal.setCreatedForCompany(forCompany);
			}
		}
		if (dealDto.getAssociatedLeadId() != null) {
			Lead associatedLead = genericDAO.get(Lead.class, dealDto.getAssociatedLeadId());
			if (associatedLead != null) {
				deal.setAssociatedUser(associatedLead.getAssociatedUser());
				setLeadStageAsWon(associatedLead, loggedInUserId);
				deal.setAssociatedLead(associatedLead);
			}
		}

		if (XamplifyUtils.isValidInteger(dealDto.getAssociatedContactId())) {
			UserUserList associatedContact = genericDAO.get(UserUserList.class, dealDto.getAssociatedContactId());
			if (associatedContact != null) {
				deal.setAssociatedUser(associatedContact.getUser());
			}
		}

		PipelineStage pipelineStage = genericDAO.get(PipelineStage.class, dealDto.getPipelineStageId());
		if (pipelineStage != null) {
			deal.setCurrentStage(pipelineStage);
			deal.setPipeline(pipelineStage.getPipeline());
		}
		if (dealDto.getAmount() != null) {
			deal.setAmount(dealDto.getAmount());
		} else {
			deal.setAmount(0.0);
		}

		if (StringUtils.isNotBlank(dealDto.getCloseDateString())) {
			deal.setCloseDate(DateUtils.convertStringToDate(dealDto.getCloseDateString()));
		} else {
			Date closeDate = new Date();
			Calendar c = Calendar.getInstance();
			c.setTime(closeDate);
			c.add(Calendar.YEAR, 1);
			closeDate = c.getTime();
			deal.setCloseDate(closeDate);
		}
		deal.setCreatedBy(loggedInUserId);
		deal.setCreatedByCompany(loggedInCompany);
		deal.setTitle(dealDto.getTitle());
		deal.setDealType(dealDto.getDealType());
		deal.setDealComment(dealDto.getDealComment());
		deal.initialiseCommonFields(true, loggedInUserId);
		if (dealDto.getHaloPSATickettypeId() != null && Long.parseLong(dealDto.getHaloPSATickettypeId()) > 0) {
			deal.setHaloPSATickettypeId(Long.parseLong(dealDto.getHaloPSATickettypeId()));
		} else {
			deal.setHaloPSATickettypeId(null);
		}

		/** XNFR-461 **/
		Pipeline createdForPipeline = genericDAO.get(Pipeline.class, dealDto.getCreatedForPipelineId());
		PipelineStage createdForpipelineStage = genericDAO.get(PipelineStage.class,
				dealDto.getCreatedForPipelineStageId());
		if (dealDto.getCreatedForPipelineId() != null && dealDto.getCreatedForPipelineStageId() != null) {
			deal.setCreatedForPipeline(createdForPipeline);
			deal.setCreatedForPipelineStage(createdForpipelineStage);
		}
		Pipeline createdByPipeline = genericDAO.get(Pipeline.class, dealDto.getCreatedByPipelineId());
		PipelineStage createdBypipelineStage = genericDAO.get(PipelineStage.class,
				dealDto.getCreatedByPipelineStageId());
		if (dealDto.getCreatedByPipelineId() != null && dealDto.getCreatedByPipelineStageId() != null) {
			deal.setCreatedByPipeline(createdByPipeline);
			deal.setCreatedByPipelineStage(createdBypipelineStage);
		}
		genericDAO.save(deal);

		if (pipelineStage != null) {
			DealStage dealStage = new DealStage();
			dealStage.setDeal(deal);
			dealStage.setPipelineStage(pipelineStage);
			dealStage.setCreatedBy(loggedInUserId);
			dealStage.initialiseCommonFields(true, loggedInUserId);
			genericDAO.save(dealStage);
		}

		genericDAO.flushCurrentSession();

		return deal;
	}

	private void setLeadStageAsWon(Lead associatedLead, Integer loggedInUserId) {
		List<PipelineStage> leadStages = associatedLead.getPipeline().getStages();
		if (leadStages != null && !leadStages.isEmpty()) {
			for (PipelineStage stage : leadStages) {
				if (stage.isWon() && stage.getId() != associatedLead.getCurrentStage().getId()) {
					associatedLead.setCurrentStage(stage);
					LeadStage leadStage = new LeadStage();
					leadStage.setLead(associatedLead);
					leadStage.setPipelineStage(stage);
					leadStage.setCreatedBy(loggedInUserId);
					leadStage.initialiseCommonFields(true, loggedInUserId);
					genericDAO.save(leadStage);
					break;
				}
			}
		}
	}

	private boolean validateSaveDealRequest(DealDto dealDto) {
		boolean valid = false;
		// dealDto.getUserId() is the loggedIn User ID
		// && !StringUtils.isBlank(dealDto.getCloseDateString())
		if (dealDto != null && !StringUtils.isBlank(dealDto.getTitle()) && dealDto.getUserId() != null
				&& dealDto.getUserId() > 0 && dealDto.getCreatedForCompanyId() != null
				&& dealDto.getCreatedForCompanyId() > 0 && dealDto.getPipelineStageId() != null
				&& dealDto.getPipelineStageId() > 0) {
			valid = true;
		}
		return valid;
	}

	private boolean isAuthorizedUser(Integer userId) {
		boolean isTeamMember = teamDao.isTeamMember(userId);
		if (isTeamMember) {
			Integer superiorId = teamDao.getOrgAdminIdByTeamMemberId(userId);
			List<Integer> roles = userDao.getRoleIdsByUserId(superiorId);
			if (roles.contains(Role.COMPANY_PARTNER.getRoleId()) || roles.contains(Role.PRM_ROLE.getRoleId())) {
				return true;
			}
		} else {
			List<Integer> roles = userDao.getRoleIdsByUserId(userId);
			if (roles.contains(Role.COMPANY_PARTNER.getRoleId()) || roles.contains(Role.PRM_ROLE.getRoleId())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public XtremandResponse updateDeal(DealDto dealDto) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (validateEditLeadRequest(dealDto)) {
			Integer loggedInUserId = dealDto.getUserId();
			CompanyProfile loggedInCompany = userService.getCompanyProfileByUser(loggedInUserId);
			Deal deal = genericDAO.get(Deal.class, dealDto.getId());
			if (deal != null) {
				if (validateLeadAttachment(dealDto.getAssociatedLeadId())) {
					if (loggedInCompany != null && canUpdateDeal(deal, loggedInUserId)
							&& isAuthorizedUser(loggedInUserId)) {
						deal = updateDeal(deal, dealDto, loggedInUserId);
						if (deal != null && deal.getId() != null) {
							deal.setDealComment(dealDto.getDealComment());
							fillIntegrationDetailsInDeal(deal);
							pushDealToActiveCRM(deal, dealDto);

							if (deal.isUpdated()) {
								mailService.sendDealUpdatedMail(deal);
							}
							if (!Objects.equals(deal.getTitle(), dealDto.getTitle())) {
								deal.setTitle(dealDto.getTitle());
								deal.setUpdated(true);
							}
							responseMessage = SUCCESS;
							responseStatusCode = 200;
						}
					} else {
						responseMessage = UNAUTHORIZED;
						responseStatusCode = 401;
					}
				} else {
					responseMessage = LEAD_ATTACHMENT_FAILURE;
					responseStatusCode = 500;
				}
			} else {
				responseMessage = "Deal not Found";
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

	private Deal updateDeal(Deal deal, DealDto dealDto, Integer loggedInUserId) {
		if (dealDto.getAmount() == null) {
			dealDto.setAmount(0.0);
		}

		if (deal.getAmount() == null) {
			deal.setAmount(0.0);
		}

		if (!deal.getAmount().equals(dealDto.getAmount())) {
			deal.setAmount(dealDto.getAmount());
			deal.setUpdated(true);
		}

		String oldCloseDateStr = DateUtils.convertToOnlyDateStringYMDFormat(deal.getCloseDate());
		if (!Objects.equals(oldCloseDateStr, dealDto.getCloseDateString())) {
			deal.setCloseDate(DateUtils.convertStringToDate(dealDto.getCloseDateString()));
			deal.setUpdated(true);
		}
		if (!Objects.equals(deal.getTitle(), dealDto.getTitle())) {
			deal.setTitle(dealDto.getTitle());
			deal.setUpdated(true);
		}
		if (!Objects.equals(deal.getDealType(), dealDto.getDealType())) {
			deal.setDealType(dealDto.getDealType());
			deal.setUpdated(true);
		}
		if (dealDto.getHaloPSATickettypeId() != null && Long.parseLong(dealDto.getHaloPSATickettypeId()) > 0) {
			deal.setHaloPSATickettypeId(Long.parseLong(dealDto.getHaloPSATickettypeId()));
		} else {
			deal.setHaloPSATickettypeId(null);
		}

		/****** XNFR-462 ******/
		if (dealDto.getAssociatedLeadId() != null) {
			Lead associatedLead = genericDAO.get(Lead.class, dealDto.getAssociatedLeadId());
			if (associatedLead != null) {
				deal.setAssociatedUser(associatedLead.getAssociatedUser());
				setLeadStageAsWon(associatedLead, loggedInUserId);
				if (deal.getAssociatedLead() == null) {
					deal.setUpdated(true);
				}
				deal.setAssociatedLead(associatedLead);
			}
		}

		deal.setDealComment(dealDto.getDealComment());
		deal.initialiseCommonFields(false, loggedInUserId);
		deal.setOldStage(deal.getCurrentStage());

		/** XNFR-461 **/
		deal.setCreatedForOldStage(deal.getCreatedForPipelineStage());
		if (dealDto.getCreatedForPipelineId() != null
				&& (dealDto.getCreatedForPipelineStageId() != null && dealDto.getCreatedForPipelineStageId() > 0)) {
			Pipeline createdForPipeline = genericDAO.get(Pipeline.class, dealDto.getCreatedForPipelineId());
			PipelineStage createdForpipelineStage = genericDAO.get(PipelineStage.class,
					dealDto.getCreatedForPipelineStageId());
			deal.setCreatedForPipeline(createdForPipeline);
			deal.setCreatedForPipelineStage(createdForpipelineStage);
		}
		deal.setCreatedByOldStage(deal.getCreatedByPipelineStage());

		if (dealDto.getCreatedByPipelineId() != null
				&& (dealDto.getCreatedByPipelineStageId() != null && dealDto.getCreatedByPipelineStageId() > 0)) {
			Pipeline createdByPipeline = genericDAO.get(Pipeline.class, dealDto.getCreatedByPipelineId());
			PipelineStage createdBypipelineStage = genericDAO.get(PipelineStage.class,
					dealDto.getCreatedByPipelineStageId());
			deal.setCreatedByPipeline(createdByPipeline);
			deal.setCreatedByPipelineStage(createdBypipelineStage);
		}
		if (!dealDto.getPipelineStageId().equals(deal.getCurrentStage().getId())) {
			deal.setUpdated(true);
			PipelineStage pipelineStage = genericDAO.get(PipelineStage.class, dealDto.getPipelineStageId());
			if (pipelineStage != null) {
				deal.setPipeline(pipelineStage.getPipeline());
				deal.setCurrentStage(pipelineStage);
				DealStage dealStage = new DealStage();
				dealStage.setDeal(deal);
				dealStage.setPipelineStage(pipelineStage);
				dealStage.setCreatedBy(loggedInUserId);
				dealStage.initialiseCommonFields(true, loggedInUserId);
				genericDAO.save(dealStage);
			}
		}
		return deal;
	}

	private boolean validateEditLeadRequest(DealDto dealDto) {
		boolean valid = false;
		// leadDto.getUserId() is the loggedIn User ID
		if (dealDto != null && dealDto.getId() != null && dealDto.getId() > 0 && dealDto.getUserId() != null
				&& dealDto.getUserId() > 0 && !StringUtils.isBlank(dealDto.getTitle())
				&& dealDto.getPipelineStageId() != null && dealDto.getPipelineStageId() > 0
				&& dealDto.getCloseDate() != null) {
			valid = true;
		}
		return valid;
	}

	private Boolean canUpdateDeal(Deal deal, Integer loggedInUserId) {
		Boolean canUpdate = false;
		if (deal != null && loggedInUserId != null) {
			List<Integer> superiorIds = partnershipDAO.getSuperiorIds(deal.getCreatedByCompany().getId());
			// List<Integer> adminIds =
			// userService.getOrgAdminIds(socialStatus.getUserId());
			if (superiorIds.contains(loggedInUserId) || deal.getCreatedBy().intValue() == loggedInUserId.intValue()) {
				canUpdate = true;
			}
		}
		return canUpdate;
	}

	private Boolean canUpdateDealStatus(Deal deal, Integer loggedInUserId) {
		Boolean canUpdate = false;
		if (deal != null && loggedInUserId != null) {
			CompanyProfile loggedInCompany = userService.getCompanyProfileByUser(loggedInUserId);
			if (deal.getCreatedForCompany().getId() == loggedInCompany.getId()
					|| deal.getCreatedByCompany().getId() == loggedInCompany.getId()) {
				canUpdate = true;
			}
		}
		return canUpdate;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getDealsForPartner(Pagination pagination) {
		Map<String, Object> resultMap = new HashMap<>();
		if (pagination != null && pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer companyId = userService.getCompanyIdByUserId(pagination.getUserId());
			if (companyId != null) {
				pagination.setCompanyId(companyId);
				setVendorCompanId(pagination);
				utilService.setDateFilters(pagination);
				if (Boolean.TRUE.equals(pagination.getIsCompanyJourney())) {
					Map<String, Object> map = dealDAO.fetchDealsForCompanyJourney(pagination);
					resultMap.put(TOTAL_RECORDS, map.get(TOTAL_RECORDS));
					resultMap.put("data", map.get("list"));
				} else {
					resultMap = dealDAO.getDealsForPartner(pagination);
					if (resultMap != null && !resultMap.isEmpty() && resultMap.get("data") != null) {
						List<Deal> deals = (List<Deal>) resultMap.get("data");
						resultMap.put("data", getDealDtoList(deals, pagination.getUserId()));
					}
				}
			}
		}
		return resultMap;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getDealsForPartnerForCSV(Pagination pagination) {
		Map<String, Object> resultMap = new HashMap<>();
		if (pagination != null && XamplifyUtils.isValidInteger(pagination.getUserId())) {
			Integer userId = pagination.getUserId();
			Integer companyId = userService.getCompanyIdByUserId(userId);
			if (companyId != null) {
				pagination.setCompanyId(companyId);
				setVendorCompanId(pagination);
				utilService.setDateFilters(pagination);
				resultMap = dealDAO.getDealsForPartner(pagination);
				if (resultMap != null && !resultMap.isEmpty() && resultMap.get("data") != null) {
					List<Deal> deals = (List<Deal>) resultMap.get("data");
					resultMap.put("data", getDealDtoListCSV(deals, userId));
				}
			}
		}
		return resultMap;
	}

	private void setVendorCompanId(Pagination pagination) {
		VanityUrlDetailsDTO vanityUrlDetailsDTO = utilService.getVanityUrlFilteredData(pagination.getUserId(),
				pagination.isVanityUrlFilter(), utilDao.getPrmCompanyProfileName());
		if (vanityUrlDetailsDTO.isPartnerLoggedInThroughVanityUrl()) {
			pagination.setVendorCompanyId(vanityUrlDetailsDTO.getVendorCompanyId());
		} else {
			Integer loginAsUserId = pagination.getLoginAsUserId();
			boolean isLoginAsPartner = XamplifyUtils.isLoginAsPartner(loginAsUserId);
			if (isLoginAsPartner) {
				pagination.setVendorCompanyId(userDao.getCompanyIdByUserId(loginAsUserId));
			}
		}
	}

	private List<DealDto> getDealDtoList(List<Deal> deals, Integer loggedInUserId) {
		List<DealDto> dealDtoList = new ArrayList<>();
		if (deals != null && !deals.isEmpty()) {
			for (Deal deal : deals) {
				DealDto dealDto = getDealDto(deal, loggedInUserId);
				dealDtoList.add(dealDto);
			}
		}
		return dealDtoList;
	}

	private List<DealDto> getDealDtoListCSV(List<Deal> deals, Integer loggedInUserId) {
		List<DealDto> dealDtoList = new ArrayList<>();
		if (deals != null && !deals.isEmpty()) {
			for (Deal deal : deals) {
				DealDto dealDto = getDealDtoCSV(deal, loggedInUserId);
				dealDtoList.add(dealDto);
			}
		}
		return dealDtoList;
	}

	private DealDto getDealDto(Deal deal, Integer loggedInUserId) {
		DealDto dealDto = null;
		if (deal != null) {
			dealDto = new DealDto();
			dealDto.setId(deal.getId());
			dealDto.setAmount(deal.getAmount());
			dealDto.setCloseDateString(DateUtils.convertDateToString(deal.getCloseDate()));
			dealDto.setCloseDate(deal.getCloseDate());
			dealDto.setCloseDateUTC(DateUtils.getUtcString(deal.getCloseDate()));
			dealDto.setTitle(deal.getTitle());
			dealDto.setDealType(deal.getDealType());
			if (deal.getAssociatedLead() != null) {
				dealDto.setAssociatedLeadId(deal.getAssociatedLead().getId());
			}
			dealDto.setCreatedForCompanyName(deal.getCreatedForCompany().getCompanyName());
			dealDto.setCreatedByCompanyName(deal.getCreatedByCompany().getCompanyName());
			PipelineDto pipeline = getPipelineDto(deal.getPipeline(), loggedInUserId);
			dealDto.setPipeline(pipeline);
			dealDto.setPipelineId(deal.getPipeline().getId());
			dealDto.setPipelineStageId(deal.getCurrentStage().getId());
			dealDto.setCurrentStageId(deal.getCurrentStage().getId());
			dealDto.setCurrentStagePrivate(deal.getCurrentStage().isPrivate());
			dealDto.setCurrentStageName(deal.getCurrentStage().getStageName());
			dealDto.setOnNonInteractiveStage(deal.getCurrentStage().isNonInteractive());
			Boolean canUpdate = canUpdateDeal(deal, loggedInUserId);
			dealDto.setCanUpdate(canUpdate);
			dealDto.setCanDelete(canUpdate);
			dealDto.setCreatedTime(DateUtils.getUtcString(deal.getCreatedTime()));
			dealDto.setCreatedDateString(DateUtils.convertDateToString(deal.getCreatedTime()));
			dealDto.setCreatedForCompanyName(deal.getCreatedForCompany().getCompanyName());
			dealDto.setCreatedByCompanyName(deal.getCreatedByCompany().getCompanyName());
			dealDto.setCreatedByCompanyId(deal.getCreatedByCompany().getId());
			dealDto.setCreatedForCompanyId(deal.getCreatedForCompany().getId());
			if (deal.getHaloPSATickettypeId() != null && deal.getHaloPSATickettypeId() > 0) {
				dealDto.setHaloPSATickettypeId(deal.getHaloPSATickettypeId() + "");
			} else {
				dealDto.setHaloPSATickettypeId(null);
			}

			if (deal.getCreatedForCompany().getId() != null && deal.getCreatedByCompany().getId() != null) {
				Integer partnerCompanyId = deal.getCreatedByCompany().getId();
				Integer vendorCompanyId = deal.getCreatedForCompany().getId();
				String partnerStatus = partnershipDAO
						.findPartnerShipStatusByPartnerCompanyIdAndVendorCompanyId(partnerCompanyId, vendorCompanyId);
				dealDto.setPartnerStatus(partnerStatus);
			}
			/** XNFR-461 **/
			setPipeLineDetails(deal, dealDto);
			fillPropertiesInDTO(dealDto, deal, loggedInUserId);

			addAssoicatedLead(deal, dealDto);
			setPartnerDetails(dealDto, deal);
			fillCreatedByDetailsInDTO(dealDto, deal);
			fillCampaignDetails(dealDto, deal);
			fillUnReadChatCounts(dealDto, deal, loggedInUserId);
			String forecastJSON = "[]";
			if (StringUtils.isNotBlank(deal.getConnectwiseForecastItemsJson())) {
				forecastJSON = deal.getConnectwiseForecastItemsJson();
			}
			dealDto.setForecastItemsJson(forecastJSON);
			/** XNFR-575 **/
			dealDto.setReferenceId(deal.getReferenceId());
			dealDto.setCrmId(getActiveCRMID(deal));
			dealDto.setActiveCRM(integrationDao.hasActiveCRMIntegration(deal.getCreatedForCompany().getId()));
			setValuesToEnableDealActions(dealDto);
			if (deal.getAssociatedContact() != null) {
				dealDto.setAssociatedContactId(deal.getAssociatedContact().getId());
			}
		}

		return dealDto;
	}

	private void setPartnerDetails(DealDto dealDto, Deal deal) {
		if (deal.getCreatedByCompany() != null && deal.getCreatedForCompany() != null) {
			Integer partnerId = deal.getCreatedByCompany().getId();
			Integer vendorId = deal.getCreatedForCompany().getId();
			dealDAO.getUserUserDetailsForDeal(dealDto, partnerId, vendorId);
		}
	}

	private DealDto getDealDtoCSV(Deal deal, Integer loggedInUserId) {
		DealDto dealDto = null;
		if (deal != null) {
			dealDto = new DealDto();
			dealDto.setId(deal.getId());
			dealDto.setAmount(deal.getAmount());
			dealDto.setCloseDateString(DateUtils.convertDateToString(deal.getCloseDate()));
			dealDto.setCloseDate(deal.getCloseDate());
			dealDto.setCloseDateUTC(DateUtils.getUtcString(deal.getCloseDate()));
			dealDto.setTitle(deal.getTitle());
			dealDto.setDealType(deal.getDealType());
			if (deal.getAssociatedLead() != null) {
				dealDto.setAssociatedLeadId(deal.getAssociatedLead().getId());
			}
			dealDto.setCreatedForCompanyName(deal.getCreatedForCompany().getCompanyName());
			dealDto.setCreatedByCompanyName(deal.getCreatedByCompany().getCompanyName());
			PipelineDto pipeline = getPipelineDto(deal.getPipeline(), loggedInUserId);
			dealDto.setPipeline(pipeline);
			dealDto.setPipelineId(deal.getPipeline().getId());
			dealDto.setPipelineStageId(deal.getCurrentStage().getId());
			dealDto.setCurrentStageId(deal.getCurrentStage().getId());
			dealDto.setCurrentStagePrivate(deal.getCurrentStage().isPrivate());
			dealDto.setCurrentStageName(deal.getCurrentStage().getStageName());
			dealDto.setOnNonInteractiveStage(deal.getCurrentStage().isNonInteractive());
			Boolean canUpdate = canUpdateDeal(deal, loggedInUserId);
			dealDto.setCanUpdate(canUpdate);
			dealDto.setCanDelete(canUpdate);
			dealDto.setCreatedTime(DateUtils.getUtcString(deal.getCreatedTime()));
			dealDto.setCreatedDateString(DateUtils.convertDateToString(deal.getCreatedTime()));
			dealDto.setCreatedForCompanyName(deal.getCreatedForCompany().getCompanyName());
			dealDto.setCreatedByCompanyName(deal.getCreatedByCompany().getCompanyName());
			dealDto.setCreatedByCompanyId(deal.getCreatedByCompany().getId());
			dealDto.setCreatedForCompanyId(deal.getCreatedForCompany().getId());
			if (deal.getHaloPSATickettypeId() != null && deal.getHaloPSATickettypeId() > 0) {
				dealDto.setHaloPSATickettypeId(deal.getHaloPSATickettypeId() + "");
			} else {
				dealDto.setHaloPSATickettypeId(null);
			}
			/** XNFR-461 **/
			setPipeLineDetails(deal, dealDto);
			fillPropertiesInDTO(dealDto, deal, loggedInUserId);

			addAssoicatedLead(deal, dealDto);
			setPartnerDetails(dealDto, deal);
			fillCreatedByDetailsInDTO(dealDto, deal);
			fillCampaignDetails(dealDto, deal);
			fillUnReadChatCounts(dealDto, deal, loggedInUserId);
			String forecastJSON = "[]";
			if (StringUtils.isNotBlank(deal.getConnectwiseForecastItemsJson())) {
				forecastJSON = deal.getConnectwiseForecastItemsJson();
			}
			dealDto.setForecastItemsJson(forecastJSON);
			/** XNFR-575 **/
			dealDto.setReferenceId(deal.getReferenceId());
			dealDto.setCrmId(getActiveCRMID(deal));
			if (deal.getSfCustomFieldsData() != null) {
				dealDto.setSfCustomFieldsData(deal.getSfCustomFieldsData());
			}
			setValuesToEnableDealActions(dealDto);
		}

		return dealDto;
	}

	private String getActiveCRMID(Deal deal) {
		String activeCRMIntegration = integrationDao
				.getActiveIntegrationTypeByCompanyId(deal.getCreatedForCompany().getId());
		return getActiveCRMIdByActiveIntegration(activeCRMIntegration, deal);
	}

	private String getActiveCRMIdByActiveIntegration(String activeCRMIntegration, Deal deal) {
		String activeCRMId = "";
		if (activeCRMIntegration != null) {
			switch (activeCRMIntegration) {
			case "salesforce":
				activeCRMId = deal.getSfDealId() != null ? deal.getSfDealId() : "";
				break;
			case "hubspot":
				activeCRMId = deal.getHubspotDealId() != null ? deal.getHubspotDealId().toString() : "";
				break;
			case "connectwise":
				activeCRMId = deal.getConnectwiseDealId() != null ? deal.getConnectwiseDealId() : "";
				break;
			case "pipedrive":
				activeCRMId = deal.getPipedriveDealId() != null ? deal.getPipedriveDealId() : "";
				break;
			case "halopsa":
				activeCRMId = deal.getHaloPSADealId() != null ? deal.getHaloPSADealId().toString() : "";
				break;
			case "zoho":
				activeCRMId = deal.getZohoDealId() != null ? deal.getZohoDealId() : "";
				break;
			case "microsoft":
				activeCRMId = deal.getMicrosoftDynamicsDealId() != null ? deal.getMicrosoftDynamicsDealId() : "";
				break;
			default:
				activeCRMId = "";
			}
		}
		return activeCRMId;
	}

	private void setValuesToEnableDealActions(DealDto dealDto) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(
				"select distinct i.deal_by_partner as \"dealByPartner\", i.show_deal_pipeline_stage as \"showDealPipelineStage\",i.deal_by_self_lead as \"dealBySelfLead\" from xt_integration i,xt_deal d where d.created_for_company_id = i.company_id and i.company_id = :vendorCompanyId and i.active");
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("vendorCompanyId", dealDto.getCreatedForCompanyId()));
		IntegrationSettingsDTO integrationSettingsDto = (IntegrationSettingsDTO) hibernateSQLQueryResultUtilDao
				.getDto(hibernateSQLQueryResultRequestDTO, IntegrationSettingsDTO.class);

		if (integrationSettingsDto != null) {
			if (!dealDto.getCreatedForCompanyId().equals(dealDto.getCreatedByCompanyId())) {
				dealDto.setShowDealActions(integrationSettingsDto.isDealByPartner());
			} else {
				dealDto.setShowDealActions(true);
			}
			dealDto.setShowEditDealStage(integrationSettingsDto.isShowDealPipelineStage());
		} else {
			dealDto.setShowDealActions(true);
			dealDto.setShowEditDealStage(true);
		}
	}

	private void setPipeLineDetails(Deal deal, DealDto dealDto) {
		if (deal.getCreatedForPipeline() != null && deal.getCreatedForPipelineStage() != null) {
			dealDto.setCreatedForPipelineId(deal.getCreatedForPipeline().getId());
			dealDto.setCreatedForPipelineStageId(deal.getCreatedForPipelineStage().getId());
		}

		if (deal.getCreatedByPipeline() != null && deal.getCreatedByPipelineStage() != null) {
			dealDto.setCreatedByPipelineId(deal.getCreatedByPipeline().getId());
			dealDto.setCreatedByPipelineStageId(deal.getCreatedByPipelineStage().getId());
		}
	}

	private void addAssoicatedLead(Deal deal, DealDto dealDto) {
		UserList dealContactList = userListDAO.getDealContactList(deal.getCreatedByCompany().getId());
		if (deal.getAssociatedUser() != null && dealContactList != null) {
			UserUserList contact = userListDAO.getContactFromDealContactList(deal.getAssociatedUser().getUserId(),
					dealContactList.getId());
			frameAssociatedContact(dealDto, contact);
		}
	}

	private void frameAssociatedContact(DealDto dealDto, UserUserList contact) {
		if (contact != null) {
			UserDTO contactDto = new UserDTO();
			contactDto.setEmailId(contact.getUser().getEmailId());
			contactDto.setFirstName(contact.getFirstName());
			contactDto.setLastName(contact.getLastName());
			contactDto.setMobileNumber(contact.getMobileNumber());
			contactDto.setCompanyName(contact.getContactCompany());
			contactDto.setContactCompany(contact.getContactCompany());
			dealDto.setAssociatedContact(contactDto);
		}
	}

	private void fillPropertiesInDTO(DealDto dealDto, Deal deal, Integer loggedInUserId) {
	}

	private void fillUnReadChatCounts(DealDto dealDto, Deal deal, Integer loggedInUserId) {
		BigInteger unReadCount = dealDAO.getUnReadChatCount(deal.getId(), loggedInUserId);
		dealDto.setUnReadChatCount(unReadCount);
	}

	private void fillCampaignDetails(DealDto dealDto, Deal deal) {
	}

	private void fillCreatedByDetailsInDTO(DealDto dealDto, Deal deal) {
		User createdUser = userService.loadUser(
				Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, deal.getCreatedBy())),
				new FindLevel[] { FindLevel.COMPANY_PROFILE });
		if (createdUser != null) {
			String name = "";
			if (createdUser.getFirstName() != null) {
				name = createdUser.getFirstName() + " ";
			}
			if (createdUser.getLastName() != null) {
				name = name + createdUser.getLastName();
			}

			dealDto.setCreatedByName(name);
			dealDto.setCreatedByEmail(createdUser.getEmailId());
		}
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
	public Map<String, Object> getDealsForVendor(Pagination pagination) {
		Map<String, Object> resultMap = new HashMap<>();
		if (pagination != null && pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer companyId = userService.getCompanyIdByUserId(pagination.getUserId());
			if (companyId != null) {
				pagination.setCompanyId(companyId);
				utilService.setDateFilters(pagination);
				resultMap = dealDAO.getDealsForVendor(pagination);
				if (resultMap != null && !resultMap.isEmpty() && resultMap.get("data") != null) {
					List<Deal> deals = (List<Deal>) resultMap.get("data");
					resultMap.put("data", getDealDtoList(deals, pagination.getUserId()));
				}
			}
		}
		return resultMap;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getDealsForVendorForCSV(Pagination pagination) {
		Map<String, Object> resultMap = new HashMap<>();
		if (pagination != null && XamplifyUtils.isValidInteger(pagination.getUserId())) {
			Integer userId = pagination.getUserId();
			Integer companyId = userService.getCompanyIdByUserId(userId);
			if (companyId != null) {
				pagination.setCompanyId(companyId);
				utilService.setDateFilters(pagination);
				resultMap = dealDAO.getDealsForVendor(pagination);
				if (resultMap != null && !resultMap.isEmpty() && resultMap.get("data") != null) {
					List<Deal> deals = (List<Deal>) resultMap.get("data");
					resultMap.put("data", getDealDtoListCSV(deals, userId));
				}
			}
		}
		return resultMap;
	}

	@Override
	public XtremandResponse getDeal(Integer loggedInUserId, Integer dealId) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (loggedInUserId != null && loggedInUserId > 0 && dealId != null && dealId > 0) {
			Deal deal = genericDAO.get(Deal.class, dealId);
			if (canViewDeal(deal, loggedInUserId)) {
				response.setData(getDealDto(deal, loggedInUserId));
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

	private boolean canViewDeal(Deal deal, Integer loggedInUserId) {
		boolean canViewLead = false;
		Integer loggedInCompanyId = userService.getCompanyIdByUserId(loggedInUserId);
		if (loggedInCompanyId != null) {
			if (loggedInCompanyId.intValue() == deal.getCreatedByCompany().getId().intValue()
					|| loggedInCompanyId.intValue() == deal.getCreatedForCompany().getId().intValue()) {
				canViewLead = true;
			}
		}
		return canViewLead;
	}

	@Override
	public XtremandResponse deleteDeal(DealDto dealDto) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (dealDto != null && dealDto.getId() != null && dealDto.getId() > 0 && dealDto.getUserId() != null
				&& dealDto.getUserId() > 0) {
			Integer loggedInUserId = dealDto.getUserId();
			Deal deal = genericDAO.get(Deal.class, dealDto.getId());
			if (canUpdateDeal(deal, loggedInUserId)) {
//				genericDAO.remove(deal);
				dealDAO.deleteDealById(deal.getId());
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
	public XtremandResponse changeDealStatus(DealDto dealDto) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (dealDto != null && dealDto.getId() != null && dealDto.getId() > 0 && dealDto.getUserId() != null
				&& dealDto.getUserId() > 0 && dealDto.getPipelineStageId() != null
				&& dealDto.getPipelineStageId() > 0) {
			Integer loggedInUserId = dealDto.getUserId();
			Deal deal = genericDAO.get(Deal.class, dealDto.getId());
			if (canUpdateDealStatus(deal, loggedInUserId)) {
				deal.setDealComment(dealDto.getDealComment());
				deal.setOldStage(deal.getCurrentStage());
				if (!dealDto.getPipelineStageId().equals(deal.getCurrentStage().getId())) {
					PipelineStage pipelineStage = genericDAO.get(PipelineStage.class, dealDto.getPipelineStageId());
					if (pipelineStage != null) {
						deal.setPipeline(pipelineStage.getPipeline());
						deal.setCurrentStage(pipelineStage);
						deal.setCreatedForPipeline(pipelineStage.getPipeline());
						deal.setCreatedForPipelineStage(pipelineStage);
						DealStage dealStage = new DealStage();
						dealStage.setDeal(deal);
						dealStage.setPipelineStage(pipelineStage);
						dealStage.setCreatedBy(loggedInUserId);
						dealStage.initialiseCommonFields(true, loggedInUserId);
						genericDAO.save(dealStage);
					}
					if (deal != null && deal.getId() != null) {
						fillIntegrationDetailsInDeal(deal);
						pushDealToActiveCRM(deal, dealDto);
						Integer partnerCompanyId = deal.getCreatedByCompany().getId();
						Integer vendorCompanyId = deal.getCreatedForCompany().getId();
						String partnerStatus = partnershipDAO.findPartnerShipStatusByPartnerCompanyIdAndVendorCompanyId(
								partnerCompanyId, vendorCompanyId);
						if (!"DEACTIVATED".equalsIgnoreCase(partnerStatus)) {
							mailService.sendDealStatusChangeMail(deal, dealDto.getUserId());
						}
					}
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

	public void saveOrUpdateSfDealRegistration(Deal deal, DealDto dealDto) {
	}

	public void saveOrUpdateSfCustomFieldsData(Deal deal, List<SfCustomFieldsDataDTO> sfCfData, Form pushDealCustomForm,
			Form comparingCompanyCustomForm) {
		if (deal != null && sfCfData != null && !sfCfData.isEmpty()) {
			List<SfCustomFieldsData> sfCustomFieldsDataList = new ArrayList<>();
			if (pushDealCustomForm != null) {
				List<FormLabel> formLabels = pushDealCustomForm.getFormLabels();
				for (SfCustomFieldsDataDTO sfCfDataDto : sfCfData) {
					FormLabel formLabel = formDao.getFormLabelByFormIdAndLabelId(pushDealCustomForm,
							sfCfDataDto.getSfCfLabelId());
					if (formLabel != null) {
						saveOrUpdateSfCustomFieldsData(deal, sfCfDataDto, formLabel, sfCustomFieldsDataList, null);
					} else {
						if (comparingCompanyCustomForm != null) {
							FormLabel formLabelInOtherForm = formDao.getFormLabelByFormIdAndLabelId(
									comparingCompanyCustomForm, sfCfDataDto.getSfCfLabelId());
							if (formLabelInOtherForm != null) {
								FormLabel matchingFormLabel = utilService.getMatchedObject(formLabelInOtherForm,
										formLabels);
								if (matchingFormLabel != null) {
									saveOrUpdateSfCustomFieldsData(deal, sfCfDataDto, matchingFormLabel,
											sfCustomFieldsDataList, formLabelInOtherForm);
								}
							}
						}
					}
				}
				deal.getSfCustomFieldsData().addAll(sfCustomFieldsDataList);
			}
		}
	}

	private void saveOrUpdateSfCustomFieldsData(Deal deal, SfCustomFieldsDataDTO sfCfDataDto, FormLabel formLabel,
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
			SfCustomFieldsData sfCustomFieldsData = sfCustomFormDataDAO.getSfCustomFieldDataByDealIdAndLabelId(deal,
					formLabel);
			if (sfCustomFieldsData != null) {
				sfCustomFieldsData.setFormLabel(formLabel);
				if (!Objects.equals(sfCustomFieldsData.getValue(), sfCfDataDto.getValue())) {
					deal.setUpdated(true);
				}
				sfCustomFieldsData.setValue(value);
				sfCustomFormDataDAO.updateSfCfData(sfCustomFieldsData);
			} else {
				deal.setUpdated(true);
				sfCustomFieldsData = new SfCustomFieldsData();
				sfCustomFieldsData.setDeal(deal);
				sfCustomFieldsData.setFormLabel(formLabel);
				sfCustomFieldsData.setValue(value);
				sfCustomFormDataDAO.saveSfCfData(sfCustomFieldsData);
			}
			sfCustomFieldsDataList.add(sfCustomFieldsData);
		}
	}

	public void saveOrUpdateSfCustomFieldsData(Deal deal, List<SfCustomFieldsDataDTO> sfCfData,
			IntegrationType activeCRMIntegrationType) {
		if (deal != null && sfCfData != null && !sfCfData.isEmpty()) {
			List<SfCustomFieldsData> sfCustomFieldsDataList = new ArrayList<>();
			Form form = getActiveCRMCustomForm(deal.getCreatedForCompany().getId(), activeCRMIntegrationType);
			if (form != null) {
				for (SfCustomFieldsDataDTO sfCfDataDto : sfCfData) {
					FormLabel formLabel = formDao.getFormLabelByFormIdAndLabelId(form, sfCfDataDto.getSfCfLabelId());
					if (formLabel != null) {
						SfCustomFieldsData sfCustomFieldsData = sfCustomFormDataDAO
								.getSfCustomFieldDataByDealIdAndLabelId(deal, formLabel);
						if (sfCustomFieldsData != null) {
							sfCustomFieldsData.setFormLabel(formLabel);
							if (!Objects.equals(sfCustomFieldsData.getValue(), sfCfDataDto.getValue())) {
								deal.setUpdated(true);
							}
							sfCustomFieldsData.setValue(sfCfDataDto.getValue());
							if (XamplifyUtils.isValidString(sfCfDataDto.getSelectedChoiceValue())) {
								sfCustomFieldsData.setSelectedChoiceValue(sfCfDataDto.getSelectedChoiceValue());
							} else {
								sfCustomFieldsData.setSelectedChoiceValue(null);
							}
							sfCustomFormDataDAO.updateSfCfData(sfCustomFieldsData);
						} else {
							deal.setUpdated(true);
							sfCustomFieldsData = new SfCustomFieldsData();
							sfCustomFieldsData.setDeal(deal);
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
				deal.setSfCustomFieldsData(sfCustomFieldsDataList);
			}
		}
	}

	private Form getActiveCRMCustomForm(Integer companyId, IntegrationType activeCRMIntegrationType) {
		Form form = null;
		if (companyId != null) {
			Integer formId = null;
			if (activeCRMIntegrationType == IntegrationType.XAMPLIFY) {
				formId = formDao.getSfCustomFormIdByCompanyIdAndFormType(companyId,
						FormTypeEnum.XAMPLIFY_DEAL_CUSTOM_FORM);
			} else if (activeCRMIntegrationType == IntegrationType.CUSTOM_CRM) {
				formId = formDao.getSfCustomFormIdByCompanyIdAndFormType(companyId,
						FormTypeEnum.CRM_DEAL_CUSTOM_FORM);
			}
			if (formId != null && formId > 0) {
				form = formDao.getById(formId);
			}
		}
		return form;
	}

	@Override
	public XtremandResponse getDealCounts(VanityUrlDetailsDTO vanityUrlDetails) {
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
						boolean isLoginAsPartner = XamplifyUtils.isLoginAsPartner(loginAsUserId);
						if (isLoginAsPartner) {
							vendorCompanyId = userDao.getCompanyIdByUserId(loginAsUserId);
						}
					}
					Map<String, Object> resultMap = new HashMap<>();
					resultMap.put("vendorCounts", new DealCountsResponseDTO());
					resultMap.put("partnerCounts", new DealCountsResponseDTO());

					DealCountsResponseDTO dealCountsResponseDTO = getVendorDealsCount(loggedInUserId,
							vanityUrlDetails.isApplyFilter());
					Object dealPartnerCounts = null;
					if (vendorCompanyId != null && vendorCompanyId > 0) {
						dealPartnerCounts = dealDAO.getCountsForPartnerInVanity(company.getId(), vendorCompanyId);
					} else {
						dealPartnerCounts = dealDAO.getCountsForPartner(company.getId());
					}
					if (dealCountsResponseDTO != null) {
						resultMap.put("vendorCounts", dealCountsResponseDTO);
					}
					if (dealPartnerCounts != null) {
						resultMap.put("partnerCounts", dealPartnerCounts);
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
	public Pipeline createDefaultLeadPipeline(CompanyProfile company, Integer userId) {
		Pipeline pipeline = new Pipeline();
		pipeline.setName(DEFAULT_LEAD_PIPELINE_NAME);
		pipeline.setType(PipelineType.LEAD);
		pipeline.setCompany(company);
		pipeline.setCreatedBy(userId);
		pipeline.setPrivate(false);
		pipeline.setSalesforcePipeline(false);
		pipeline.setDefault(true);
		pipeline.initialiseCommonFields(true, userId);
		genericDAO.save(pipeline);

		PipelineStage stage = new PipelineStage();
		stage.setStageName("Opened");
		stage.setDefaultStage(true);
		stage.setWon(false);
		stage.setLost(false);
		stage.setPipeline(pipeline);
		stage.setCreatedBy(userId);
		stage.setDisplayIndex(1);
		stage.initialiseCommonFields(true, userId);
		genericDAO.save(stage);

		PipelineStage stage2 = new PipelineStage();
		stage2.setStageName("Contacted");
		stage2.setDefaultStage(false);
		stage2.setWon(false);
		stage2.setLost(false);
		stage2.setPipeline(pipeline);
		stage2.setCreatedBy(userId);
		stage2.setDisplayIndex(2);
		stage2.initialiseCommonFields(true, userId);
		genericDAO.save(stage2);

		PipelineStage stage5 = new PipelineStage();
		stage5.setStageName("Hold");
		stage5.setDefaultStage(false);
		stage5.setWon(false);
		stage5.setLost(false);
		stage5.setPipeline(pipeline);
		stage5.setCreatedBy(userId);
		stage5.setDisplayIndex(3);
		stage5.initialiseCommonFields(true, userId);
		genericDAO.save(stage5);

		PipelineStage stage3 = new PipelineStage();
		stage3.setStageName("Converted");
		stage3.setDefaultStage(false);
		stage3.setWon(true);
		stage3.setLost(false);
		stage3.setPipeline(pipeline);
		stage3.setCreatedBy(userId);
		stage3.setDisplayIndex(4);
		stage3.initialiseCommonFields(true, userId);
		genericDAO.save(stage3);

		PipelineStage stage4 = new PipelineStage();
		stage4.setStageName("Closed - Lost");
		stage4.setDefaultStage(false);
		stage4.setWon(false);
		stage4.setLost(true);
		stage4.setPipeline(pipeline);
		stage4.setCreatedBy(userId);
		stage4.setDisplayIndex(5);
		stage4.initialiseCommonFields(true, userId);
		genericDAO.save(stage4);

		List<PipelineStage> stagesList = new ArrayList<>();
		stagesList.add(stage);
		stagesList.add(stage2);
		stagesList.add(stage5);
		stagesList.add(stage3);
		stagesList.add(stage4);
		pipeline.setStages(stagesList);
		return pipeline;
	}

	@Override
	public Pipeline createDefaultDealPipeline(CompanyProfile company, Integer userId) {
		Pipeline pipeline = new Pipeline();
		pipeline.setName(DEFAULT_DEAL_PIPELINE_NAME);
		pipeline.setType(PipelineType.DEAL);
		pipeline.setCompany(company);
		pipeline.setCreatedBy(userId);
		pipeline.setPrivate(false);
		pipeline.setSalesforcePipeline(false);
		pipeline.setDefault(true);
		pipeline.initialiseCommonFields(true, userId);
		genericDAO.save(pipeline);

		PipelineStage stage = new PipelineStage();
		stage.setStageName("Opened");
		stage.setDefaultStage(true);
		stage.setWon(false);
		stage.setLost(false);
		stage.setPipeline(pipeline);
		stage.setCreatedBy(userId);
		stage.setDisplayIndex(1);
		stage.initialiseCommonFields(true, userId);
		genericDAO.save(stage);

		PipelineStage stage2 = new PipelineStage();
		stage2.setStageName("Hold");
		stage2.setDefaultStage(false);
		stage2.setWon(false);
		stage2.setLost(false);
		stage2.setPipeline(pipeline);
		stage2.setCreatedBy(userId);
		stage2.setDisplayIndex(2);
		stage2.initialiseCommonFields(true, userId);
		genericDAO.save(stage2);

		PipelineStage stage3 = new PipelineStage();
		stage3.setStageName("Approved");
		stage3.setDefaultStage(false);
		stage3.setWon(true);
		stage3.setLost(false);
		stage3.setPipeline(pipeline);
		stage3.setCreatedBy(userId);
		stage3.setDisplayIndex(3);
		stage3.initialiseCommonFields(true, userId);
		genericDAO.save(stage3);

		PipelineStage stage4 = new PipelineStage();
		stage4.setStageName("Rejected");
		stage4.setDefaultStage(false);
		stage4.setWon(false);
		stage4.setLost(true);
		stage4.setPipeline(pipeline);
		stage4.setCreatedBy(userId);
		stage4.setDisplayIndex(4);
		stage4.initialiseCommonFields(true, userId);
		genericDAO.save(stage4);

		List<PipelineStage> stagesList = new ArrayList<>();
		stagesList.add(stage);
		stagesList.add(stage2);
		stagesList.add(stage3);
		stagesList.add(stage4);
		pipeline.setStages(stagesList);
		return pipeline;
	}

	@Override
	public Deal createDealFromSfOpportunity(JSONObject opportunityInSf, Lead lead, Integer loggedInUserId,
			Integer loggedInCompanyId) {
		Deal deal = new Deal();
		deal.setSfDealId(getString(opportunityInSf, "Id"));
		deal.setCreatedForCompany(lead.getCreatedForCompany());
		deal.setCreatedByCompany(lead.getCreatedByCompany());
		deal.setCreatedBy(lead.getCreatedBy());

		deal.setAssociatedUser(lead.getAssociatedUser());
		deal.setAssociatedLead(lead);

		deal.setTitle(getString(opportunityInSf, "Name"));
		deal.setDealType(getString(opportunityInSf, "Type"));
		deal.setAmount(getDouble(opportunityInSf, "Amount"));
		deal.setCloseDate(DateUtils.convertStringToDate(getString(opportunityInSf, "CloseDate")));

		Integration otherActiveCRMIntegration = utilService.getOtherActiveCRMIntegration(deal, loggedInCompanyId);
		Pipeline pipeline = pipelineDAO.getSalesforcePipeline(deal.getCreatedForCompany().getId(), PipelineType.DEAL);
		if (pipeline != null) {
			List<PipelineStage> stages = pipeline.getStages();
			Optional<PipelineStage> stage = stages.stream()
					.filter(e -> e.getStageName().equals(getString(opportunityInSf, "StageName"))).findFirst();
			deal.setCurrentStage(stage.get());
			deal.setPipeline(pipeline);
			PipelineStage pipelineStage = null;
			;
			if (stage.isPresent()) {
				pipelineStage = stage.get();
			}
			utilService.setPipelines(deal, loggedInCompanyId, pipeline, pipelineStage);
			utilService.setOtherActiveCRMPipeline(deal, otherActiveCRMIntegration);
		}

		deal.initialiseCommonFields(true, lead.getCreatedBy());
		genericDAO.save(deal);

		if (deal.getCurrentStage() != null) {
			DealStage dealStage = new DealStage();
			dealStage.setDeal(deal);
			dealStage.setPipelineStage(deal.getCurrentStage());
			dealStage.setCreatedBy(loggedInUserId);
			dealStage.initialiseCommonFields(true, loggedInUserId);
			genericDAO.save(dealStage);
		}
		return deal;
	}

	private String getString(JSONObject object, String key) {
		return (object.isNull(key)) ? null : object.get(key).toString();
	}

	private Double getDouble(JSONObject object, String key) {
		return (object.isNull(key)) ? null : object.getDouble(key);
	}

	@Override
	public Deal updateDealFromSfOpportunity(Deal deal, JSONObject opportunityInSf, Lead lead, Integer loggedInUserId,
			Integer loggedInCompanyId) {
		if (deal != null && loggedInUserId != null && opportunityInSf != null
				&& getString(opportunityInSf, "Id") != null) {
			deal.setSfDealId(getString(opportunityInSf, "Id"));
			deal.setTitle(getString(opportunityInSf, "Name"));
			deal.setDealType(getString(opportunityInSf, "Type"));
			deal.setAmount(getDouble(opportunityInSf, "Amount"));
			deal.setCloseDate(DateUtils.convertStringToDate(getString(opportunityInSf, "CloseDate")));
			deal.initialiseCommonFields(false, deal.getCreatedBy());

			String existingStageName = deal.getCurrentStage().getStageName();
			if (!existingStageName.equals(getString(opportunityInSf, "StageName"))) {
				Pipeline pipeline = pipelineDAO.getSalesforcePipeline(deal.getCreatedForCompany().getId(),
						PipelineType.DEAL);
				if (pipeline != null) {
					List<PipelineStage> stages = pipeline.getStages();

					PipelineStage newStage = null;
					if (getString(opportunityInSf, "StageName") != null) {
						Optional<PipelineStage> stage = stages.stream()
								.filter(e -> e.getStageName().equals(getString(opportunityInSf, "StageName")))
								.findFirst();
						if (stage.isPresent()) {
							newStage = stage.get();
						}
					}
					if (newStage == null) {
						Optional<PipelineStage> defaultStage = stages.stream().filter(e -> e.isDefaultStage())
								.findFirst();
						if (defaultStage != null) {
							newStage = defaultStage.get();
						}
					}

					utilService.setPipelines(deal, loggedInCompanyId, pipeline, newStage);

					DealStage dealStage = new DealStage();
					dealStage.setDeal(deal);
					dealStage.setPipelineStage(deal.getCurrentStage());
					dealStage.setCreatedBy(loggedInUserId);
					dealStage.initialiseCommonFields(true, loggedInUserId);
					genericDAO.save(dealStage);
				}
			}

		}
		return deal;
	}

	@Override
	public DealCountsResponseDTO getVendorDealsCount(Integer userId, boolean applyFilter) {
		Integer companyId = userDao.getCompanyIdByUserId(userId);
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(userId, applyFilter, false);
		boolean applyTeamMemberFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
		if (teamMemberFilterDTO.isEmptyFilter()) {
			return new DealCountsResponseDTO();
		} else {
			if (applyTeamMemberFilter) {
				String sqlQuery = "WITH total_deals AS (SELECT l.created_for_company_id,count(*) AS total_deals FROM xt_deal l where l.created_by_company_id in (:partnerCompanyIds)\r\n"
						+ " GROUP BY l.created_for_company_id), won_deals AS (SELECT l.created_for_company_id,count(*) AS won_deals FROM xt_deal l,xt_pipeline_stage ps\r\n"
						+ "WHERE l.pipeline_stage_id = ps.id AND ps.is_won = true and l.created_by_company_id in (:partnerCompanyIds) GROUP BY l.created_for_company_id), \r\n"
						+ "lost_deals AS (SELECT l.created_for_company_id,count(*) AS lost_deals FROM xt_deal l,xt_pipeline_stage ps WHERE l.pipeline_stage_id = ps.id AND ps.is_lost = true\r\n"
						+ " and l.created_by_company_id in (:partnerCompanyIds) GROUP BY l.created_for_company_id)\r\n"
						+ "SELECT total_deals.created_for_company_id AS \"companyId\",cast(COALESCE(total_deals.total_deals, 0) as int) AS \"totalDeals\","
						+ "cast(COALESCE(won_deals.won_deals, 0) as int) AS \"wonDeals\",cast( COALESCE(lost_deals.lost_deals, 0)as int) AS \"lostDeals\" "
						+ "FROM total_deals LEFT JOIN won_deals ON won_deals.created_for_company_id = total_deals.created_for_company_id\r\n"
						+ "LEFT JOIN lost_deals ON lost_deals.created_for_company_id = total_deals.created_for_company_id\r\n"
						+ "where total_deals.created_for_company_id = :companyId";
				DealCountsResponseDTO dealCountsResponseDTO = dealDAO.findDealsCountByFilter(sqlQuery, companyId,
						teamMemberFilterDTO.getPartnershipIdsOrPartnerCompanyIds());
				return setDealVendorsCountView(dealCountsResponseDTO);
			} else {
				DealCountsResponseDTO dealCountsResponseDTO = dealDAO.getCountsForVendor(companyId, applyFilter);
				return setDealVendorsCountView(dealCountsResponseDTO);

			}
		}
	}

	private DealCountsResponseDTO setDealVendorsCountView(DealCountsResponseDTO dealCountsResponseDTO) {
		if (dealCountsResponseDTO != null) {
			return dealCountsResponseDTO;
		} else {
			return new DealCountsResponseDTO();
		}
	}

	@Override
	public XtremandResponse getChatByProperty(Integer propertyId, Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (propertyId != null && propertyId > 0 && loggedInUserId != null && loggedInUserId > 0) {
			responseMessage = UNAUTHORIZED;
			responseStatusCode = 401;
		} else {
			responseMessage = INVALID_INPUT;
			responseStatusCode = 500;
		}

		response.setMessage(responseMessage);
		response.setStatusCode(responseStatusCode);
		return response;
	}

	@Override
	public XtremandResponse getChat(Integer dealId, Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (dealId != null && dealId > 0 && loggedInUserId != null && loggedInUserId > 0) {
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

	@Override
	public void downloadDeals(HttpServletResponse httpServletResponse, String userType, String type, Integer userId,
			String filename, boolean vanityUrlFilter, String vendorCompanyProfileName, String searchKey,
			String fromDate, String toDate, boolean partnerTeamMemberGroupFilter, String timeZone, String stageName,
			Integer createdForCompanyId) {
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
			pagination.setVendorCompanyId(createdForCompanyId);

			List<DealDto> deals = getDealsForCSV(pagination);
			LinkedHashMap<String, String> fieldHeaderMapping = getFieldHeaderMapping(userType, userId);
			if (deals != null && !deals.isEmpty()) {
				csvUtilService.downLoadToCSV(httpServletResponse, filename, fieldHeaderMapping, deals);
			}
		}
	}

	@Override
	public LinkedHashMap<String, String> getFieldHeaderMapping(String userType, Integer userId) {
		LinkedHashMap<String, String> fieldHeaderMapping = new LinkedHashMap<>();
		fieldHeaderMapping.put("Deal Title", "getTitle");
		/************ XBI-1000 ***********/
		if ("p".equals(userType) && !utilDao.isPartnershipEstablishedOnlyWithPrm(userId)) {
			fieldHeaderMapping.put("Campaign Name", "getCampaignName");
			fieldHeaderMapping.put("Added For - Company", "getCreatedForCompanyName");
		} else if ("v".equals(userType) && !utilDao.isPrmCompany(userId)) {
			fieldHeaderMapping.put("Campaign Name", "getParentCampaignName");
		}
		if ("v".equals(userType)) {
			fieldHeaderMapping.put("CRM Id", "getCrmId");
			fieldHeaderMapping.put("Deal Id", "getReferenceId");
		}
		fieldHeaderMapping.put("Added By - Company", "getCreatedByCompanyName");
		fieldHeaderMapping.put("Added By - Name", "getCreatedByName");
		fieldHeaderMapping.put("Added By - Email ID", "getCreatedByEmail");
		fieldHeaderMapping.put("Added On (PST)", "getCreatedDateString");
		fieldHeaderMapping.put("Status", "getCurrentStageName");
		if ("v".equals(userType)) {
			fieldHeaderMapping.put("AccountSubType", "getAccountSubType");
			fieldHeaderMapping.put("PartnerType", "getPartnerType");
		}
		return fieldHeaderMapping;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<DealDto> getDealsForCSV(Pagination pagination) {
		if ("all".equals(pagination.getType())) {
			pagination.setFilterKey(null);
		}
		pagination.setExcludeLimit(true);

		Map<String, Object> dealsMap = null;
		if ("v".equals(pagination.getUserType())) {
			pagination.setPartnerTeamMemberGroupFilter(pagination.isPartnerTeamMemberGroupFilter());
			dealsMap = getDealsForVendorForCSV(pagination);
		} else if ("p".equals(pagination.getUserType())) {
			dealsMap = getDealsForPartnerForCSV(pagination);
		}
		return (List<DealDto>) dealsMap.get("data");
	}

	@Override
	public List<String> getStageNamesForVendor(Integer loggedInUserId) {
		List<String> stageList = new ArrayList<String>();
		if (loggedInUserId != null && loggedInUserId > 0) {
			Integer companyId = userService.getCompanyIdByUserId(loggedInUserId);
			if (companyId != null && companyId > 0) {
				String activeCRM = getActiveCRMString(companyId);
				stageList = dealDAO.getStageNamesForVendor(companyId, activeCRM);
			}
		}
		return stageList;
	}

	private String getActiveCRMString(Integer companyId) {
		String activeCRM = null;
		Integration activeCRMIntegration = integrationDao.getActiveCRMIntegration(companyId);
		if (activeCRMIntegration != null) {
			IntegrationType type = activeCRMIntegration.getType();
			if (type != null) {
				activeCRM = type.name().toLowerCase();
			}
		}
		return activeCRM;
	}

	@Override
	public List<String> getStageNamesForPartner(Integer loggedInUserId, Integer vendorCompanyId) {
		List<String> stageList = new ArrayList<String>();
		if (loggedInUserId != null && loggedInUserId > 0 && vendorCompanyId != null && vendorCompanyId > 0) {
			String activeCRM = getActiveCRMString(vendorCompanyId);
			stageList = dealDAO.getStageNamesForVendor(vendorCompanyId, activeCRM);
		}
		return stageList;
	}

	@Override
	public List<String> getStageNamesForPartner(Integer loggedInUserId) {
		List<String> stageList = new ArrayList<String>();
		if (loggedInUserId != null && loggedInUserId > 0) {
			Integer companyId = userService.getCompanyIdByUserId(loggedInUserId);
			if (companyId != null && companyId > 0) {
				stageList = dealDAO.getStageNamesForPartner(companyId);
			}
		}
		return stageList;
	}

	@Override
	public List<String> getStageNamesForVendorInCampaign(Integer loggedInUserId) {
		List<String> stageList = new ArrayList<String>();
		if (loggedInUserId != null && loggedInUserId > 0) {
			Integer companyId = userService.getCompanyIdByUserId(loggedInUserId);
			if (companyId != null && companyId > 0) {
				stageList = dealDAO.getStageNamesForVendorInCampaign(companyId);
			}
		}
		return stageList;
	}

	@Override
	public List<String> getStageNamesForPartnerCompanyId(Integer companyId) {
		List<String> stageList = new ArrayList<String>();
		if (companyId != null && companyId > 0) {
			stageList = dealDAO.getStageNamesForPartnerCompanyId(companyId);
		}
		return stageList;
	}

	private String getString(org.json.simple.JSONObject object, String key) {
		return (object.get(key) == null) ? null : (String) object.get(key);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse downloadDeals(Integer userId) {
		XtremandResponse response = new XtremandResponse();
		DownloadDataInfo downloadDataInfo = utilDao.getDownloadDataInfo(userId, DownloadItem.DEALS_DATA);
		if (downloadDataInfo == null || !downloadDataInfo.isDownloadInProgress()) {
			downloadDataInfo = utilDao.updateDownloadDataInfo(userId, downloadDataInfo, DownloadItem.DEALS_DATA);
			response.setMessage(
					"We are processing your deal(s) reports. We will send it over an email when the report is ready!");
			response.setStatusCode(200);
		} else {
			response.setMessage("Please wait until the previous request is processed...!");
			response.setStatusCode(401);
		}
		response.setData(downloadDataInfo.getId());
		return response;
	}

	/**** XNFR-483 ****/
	private boolean validateLeadAttachment(Integer leadId) {
		boolean valid = false;
		if (leadId != null && leadId > 0) {
			Lead lead = genericDAO.get(Lead.class, leadId);
			if (lead != null && lead.getLeadApprovalStatusType() != LeadApprovalStatusEnum.REJECTED) {
				valid = true;
			}
		} else {
			valid = true;
		}
		return valid;
	}

	@Override
	public Deal createDealFromHalopsa(org.json.simple.JSONObject dealInHalopsa, Lead lead, Integer loggedInUserId) {
		Deal deal = new Deal();
		deal.setHaloPSADealId((Long) dealInHalopsa.get("id"));
		deal.setCreatedForCompany(lead.getCreatedForCompany());
		deal.setCreatedByCompany(lead.getCreatedByCompany());
		deal.setCreatedBy(lead.getCreatedBy());

		deal.setAssociatedUser(lead.getAssociatedUser());
		deal.setAssociatedLead(lead);

		deal.setTitle(getString(dealInHalopsa, "summary"));
		if (dealInHalopsa.get("oppvalue") != null && !String.valueOf(dealInHalopsa.get("oppvalue")).equals("")) {
			deal.setAmount(Double.valueOf(String.valueOf(dealInHalopsa.get("oppvalue"))));
		} else {
			deal.setAmount(0.0);
		}
		String closeDateString = null;
		if (dealInHalopsa.get("targetdate") != null && !String.valueOf(dealInHalopsa.get("targetdate")).equals("")) {
			closeDateString = (String) dealInHalopsa.get("targetdate");
		}
		deal.setCloseDate(new Date());
		if (StringUtils.isNotBlank(closeDateString)) {
			Date date = DateUtils.convertStringToDate(closeDateString);
			if (date != null && (date.compareTo(new Date()) > 0)) {
				deal.setCloseDate(date);
			}
		}
		deal.setHaloPSATickettypeId((Long) dealInHalopsa.get("tickettype_id"));
		deal.initialiseCommonFields(true, lead.getCreatedBy());
		genericDAO.save(deal);

		if (deal.getCurrentStage() != null) {
			DealStage dealStage = new DealStage();
			dealStage.setDeal(deal);
			dealStage.setPipelineStage(deal.getCurrentStage());
			dealStage.setCreatedBy(loggedInUserId);
			dealStage.initialiseCommonFields(true, loggedInUserId);
			genericDAO.save(dealStage);
		}
		return deal;
	}

	@SuppressWarnings("unchecked")
	@Override
	public XtremandResponse findRegisteredByCompanies(Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(
				"select distinct  xcp.company_id as \"id\",xcp.company_name as \"name\" from xt_deal xl,xt_company_profile xcp where xcp.company_id  = xl.created_by_company_id \r\n"
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
						+ "then TRIM(concat(TRIM(xup.email_id),' - ( ',TRIM(xup.firstname), ' ', TRIM(xup.lastname),' )')) else xup.email_id end as \"name\" from xt_deal xl,xt_user_profile xup,xt_campaign xc"
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
	public Deal createDealFromZoho(org.json.simple.JSONObject dealInZoho, Lead lead, Integer loggedInUserId) {
		Deal deal = new Deal();
		deal.setZohoDealId((String) dealInZoho.get("id"));
		deal.setCreatedForCompany(lead.getCreatedForCompany());
		deal.setCreatedByCompany(lead.getCreatedByCompany());
		deal.setCreatedBy(lead.getCreatedBy());

		deal.setAssociatedUser(lead.getAssociatedUser());
		setLeadStageAsWon(lead, loggedInUserId);
		deal.setAssociatedLead(lead);

		deal.setTitle(getString(dealInZoho, "Deal_Name"));
		if (dealInZoho.get("Amount") != null && !String.valueOf(dealInZoho.get("Amount")).equals("")) {
			deal.setAmount(Double.valueOf(String.valueOf(dealInZoho.get("Amount"))));
		} else {
			deal.setAmount(0.0);
		}
		String closeDateString = null;
		if (dealInZoho.get("Closing_Date") != null && !String.valueOf(dealInZoho.get("Closing_Date")).equals("")) {
			closeDateString = (String) dealInZoho.get("Closing_Date");
		}
		deal.setCloseDate(new Date());
		if (StringUtils.isNotBlank(closeDateString)) {
			Date date = DateUtils.convertStringToDate(closeDateString);
			if (date != null) {
				deal.setCloseDate(date);
			}
		}
		setPipelineDetailsForZohoDeal(lead.getCreatedByCompany().getId(), deal, dealInZoho);
		deal.initialiseCommonFields(true, lead.getCreatedBy());
		genericDAO.save(deal);

		if (deal.getCurrentStage() != null) {
			DealStage dealStage = new DealStage();
			dealStage.setDeal(deal);
			dealStage.setPipelineStage(deal.getCurrentStage());
			dealStage.setCreatedBy(loggedInUserId);
			dealStage.initialiseCommonFields(true, loggedInUserId);
			genericDAO.save(dealStage);
		}

		return deal;
	}

	private void setPipelineDetailsForZohoDeal(Integer loggedInCompanyId, Deal deal,
			org.json.simple.JSONObject dealInZoho) {
		String pipelineName = dealInZoho.get("Pipeline") + " (Zoho)";
		Pipeline pipeline = pipelineDAO.getPipeLineByName(loggedInCompanyId, pipelineName, PipelineType.DEAL);
		if (pipeline != null) {
			String stage = (String) dealInZoho.get("Stage");
			if (stage != null && !stage.isEmpty()) {
				for (PipelineStage pipelineStage : pipeline.getStages()) {
					if (pipelineStage.getStageName().equals(stage)) {
						utilService.setPipelines(deal, pipeline.getCompany().getId(), pipeline, pipelineStage);
						break;
					}
				}
			}
		}
		if (deal.getCurrentStage() == null) {
			deal.setCurrentStage(pipeline.getStages().get(0));
		}
	}

	@Override
	public XtremandResponse findDealAndLeadInfoAndComments(Integer dealId) {
		XtremandResponse response = new XtremandResponse();
		Deal deal = genericDAO.get(Deal.class, dealId);
		if (deal != null) {
			DealDto dealDto = new DealDto();
			dealDto.setId(deal.getId());
			dealDto.setTitle(deal.getTitle());
			setCreatedByNameAndCreatedByEmail(deal, dealDto);
			dealDto.setCreatedTime(DateUtils.getUtcString(deal.getCreatedTime()));
			dealDto.setReferenceId(deal.getReferenceId());
			setPartnerDetails(dealDto, deal);
			addAssoicatedLead(deal, dealDto);
			response.setData(dealDto);
			XamplifyUtils.addSuccessStatus(response);
		} else {
			response.setStatusCode(404);
		}
		return response;
	}

	private void setCreatedByNameAndCreatedByEmail(Deal deal, DealDto dealDto) {
		Integer dealCreatedBy = deal.getCreatedBy();
		if (XamplifyUtils.isValidInteger(dealCreatedBy)) {
			User createdUser = userService.loadUser(
					Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, dealCreatedBy)),
					new FindLevel[] { FindLevel.COMPANY_PROFILE });
			if (createdUser != null) {
				String name = "";
				if (createdUser.getFirstName() != null) {
					name = createdUser.getFirstName() + " ";
				}
				if (createdUser.getLastName() != null) {
					name = name + createdUser.getLastName();
				}
				dealDto.setCreatedByName(name);
				dealDto.setCreatedByEmail(createdUser.getEmailId());
			}
		}
	}

	/** XNFR-650 **/
	@Override
	public Map<String, Object> queryDealsForVendor(Pagination pagination) {
		Map<String, Object> resultMap = new HashMap<>();
		if (pagination != null && pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer companyId = userService.getCompanyIdByUserId(pagination.getUserId());
			if (companyId != null) {
				pagination.setCompanyId(companyId);
				utilService.setDateFilters(pagination);
				resultMap = dealDAO.queryDealsForVendor(pagination);
			}
		}
		return resultMap;
	}

	/** XNFR-650 **/
	@Override
	public Map<String, Object> queryDealsForPartner(Pagination pagination) {
		Map<String, Object> resultMap = new HashMap<>();
		if (pagination != null && pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer companyId = userService.getCompanyIdByUserId(pagination.getUserId());
			if (companyId != null) {
				pagination.setCompanyId(companyId);
				setVendorCompanId(pagination);
				utilService.setDateFilters(pagination);
				resultMap = dealDAO.queryDealsForPartner(pagination);
			}
		}
		return resultMap;
	}

	@SuppressWarnings("unchecked")
	public XtremandResponse findVendorDetailsWithSelfDealsCount(VendorSelfDealRequestDTO vendorSelfDealRequestDTO) {
		XtremandResponse response = new XtremandResponse();
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String sqlQuery = "select dv.company_id as \"companyId\", dv.company_name as \"companyName\", cast(dv.total_deals as int) as \"totalDeals\" "
				+ " from deal_campaign_partner_view dv, xt_user_profile up"
				+ " where dv.company_id = up.company_id and dv.campaign_id = :campaignId and up.user_id = :userId";
		hibernateSQLQueryResultRequestDTO.setQueryString(sqlQuery);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("campaignId", vendorSelfDealRequestDTO.getCampaignId()));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("userId", vendorSelfDealRequestDTO.getLoggedInUserId()));
		hibernateSQLQueryResultRequestDTO.setClassInstance(DealCountsResponseDTO.class);
		List<DealCountsResponseDTO> vendorDetailsWithSelfDealsCount = (List<DealCountsResponseDTO>) hibernateSQLQueryResultUtilDao
				.returnDTOList(hibernateSQLQueryResultRequestDTO);
		XamplifyUtils.addSuccessStatus(response);
		response.setData(vendorDetailsWithSelfDealsCount);
		return response;
	}

	/** XNFR-553 **/
	@Override
	public XtremandResponse findDealsAndCountByContactId(ContactOpportunityRequestDTO contactOpportunityRequestDTO) {
		XtremandResponse response = new XtremandResponse();
		if (XamplifyUtils.isValidInteger(contactOpportunityRequestDTO.getLoggedInUserId())
				&& XamplifyUtils.isValidInteger(contactOpportunityRequestDTO.getContactId())) {
			Map<String, Object> map;
			Integer companyId = userDao.getCompanyIdByUserId(contactOpportunityRequestDTO.getLoggedInUserId());
			contactOpportunityRequestDTO.setLoggedInUserCompanyId(companyId);
			if (contactOpportunityRequestDTO.isVanityUrlFilter()) {
				Integer vendorCompanyId = userDao
						.getCompanyIdByProfileName(contactOpportunityRequestDTO.getVendorCompanyName());
				contactOpportunityRequestDTO.setVendorCompanyId(vendorCompanyId);
			}
			map = dealDAO.findDealsAndCountByContactId(contactOpportunityRequestDTO);
			response.setData(map);
			XamplifyUtils.addSuccessStatus(response);
		} else {
			XamplifyUtils.addErorMessageWithStatusCode(response, "Failed to fetch the deals", 401);
		}
		return response;
	}

	@SuppressWarnings("unchecked")
	@Override
	public XtremandResponse fetchContactsForDealAttachment(Integer loggedInUserId, Pageable pageable) {
		XtremandResponse response = new XtremandResponse();
		if (XamplifyUtils.isValidInteger(loggedInUserId)) {
			UserListDTO userListDTO = new UserListDTO();
			Map<String, Object> resultMap = new HashMap<>();
			Map<String, Object> map = new HashMap<>();
			Pagination pagination = utilService.setPageableParameters(pageable, loggedInUserId);
			userListDTO.setPartnerUserList(false);
			List<Integer> userListIds = utilDao.getDefaultMasterContactListIdByUserId(loggedInUserId);
			if (XamplifyUtils.isNotEmptyList(userListIds)) {
				pagination.setUserListId(userListIds.get(0));
				map = userListDAO.fetchContactsFromUserList(pagination);
				List<UserListUsersView> userDTOs = (List<UserListUsersView>) map.get("list");
				Integer totalRecords = (Integer) map.get(TOTAL_RECORDS);
				iterateAndSetContactsProperties(userDTOs, userListIds.get(0));
				resultMap.put("list", userDTOs);
				resultMap.put(TOTAL_RECORDS, totalRecords);
				response.setData(resultMap);
				XamplifyUtils.addSuccessStatus(response);
			} else {
				XamplifyUtils.addErorMessageWithStatusCode(response, "Failed to fetch the contacts", 401);
			}
		} else {
			XamplifyUtils.addErorMessageWithStatusCode(response, "Failed to fetch the contacts", 401);
		}
		return response;
	}

	private void iterateAndSetContactsProperties(List<UserListUsersView> userDTOs, Integer userListId) {
		if (XamplifyUtils.isNotEmptyList(userDTOs)) {
			for (UserListUsersView userDTO : userDTOs) {
				if (userDTO.getUserListId() != null) {
					userDTO.setLegalBasis(
							userListDAO.listLegalBasisByContactListIdAndUserId(userListId, userDTO.getUserId()));
				}
				List<FlexiFieldRequestDTO> flexiFields = flexiFieldDao
						.findFlexiFieldsBySelectedUserIdAndUserListId(userDTO.getUserListId(), userDTO.getUserId());
				userDTO.setFlexiFields(flexiFields);
			}
		}
	}

	@Override
	public XtremandResponse fetchTotalDealAmountForCompanyJourney(
			ContactOpportunityRequestDTO contactOpportunityRequestDTO) {
		XtremandResponse response = new XtremandResponse();
		if (XamplifyUtils.isValidInteger(contactOpportunityRequestDTO.getLoggedInUserId())
				&& XamplifyUtils.isValidInteger(contactOpportunityRequestDTO.getContactId())) {
			Integer companyId = userDao.getCompanyIdByUserId(contactOpportunityRequestDTO.getLoggedInUserId());
			if (contactOpportunityRequestDTO.isVanityUrlFilter()) {
				Integer vendorCompanyId = userDao
						.getCompanyIdByProfileName(contactOpportunityRequestDTO.getVendorCompanyName());
				contactOpportunityRequestDTO.setVendorCompanyId(vendorCompanyId);
			}
			contactOpportunityRequestDTO.setLoggedInUserCompanyId(companyId);
			Double totalAmount = dealDAO.fetchTotalDealAmount(contactOpportunityRequestDTO);
			String dealAmount = "$ " + NumberFormatterString.formatValueInTrillionsOrBillions(totalAmount);
			response.setData(dealAmount);
			XamplifyUtils.addSuccessStatus(response);
		}
		return response;
	}
	
	@Override
	public XtremandResponse saveDealCustomFormFromMcp(Integer userId) {
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
			Map<String, Object> formResponse = fetchMcpDealCustomForm(pat);
			if (formResponse == null || formResponse.isEmpty()) {
				return response;
			}

			FormTypeEnum formType = resolveDealFormType();
			if (formType == null) {
				return response;
			}

//			FormDTO formDto = buildDealCustomFormDto(companyId, formType, formResponse.get("fields"));
//			if (formDto == null) {
//				return response;
//			}
			
			Object fields = formResponse.get("fields");
            List<OpportunityFormFieldsDTO> defaultDealFields = Collections.emptyList();
            if (!(fields instanceof List<?>) || ((List<?>) fields).isEmpty()) {
                    defaultDealFields = customFieldsDao.getDealDefaultCustomFieldsDto();
            }

            FormDTO formDto = buildDealCustomFormDto(companyId, formType, fields, defaultDealFields);
            if (formDto == null) {
                    return response;
            }

			Integer formId = formDao.getSfCustomFormIdByCompanyIdAndFormType(companyId, formType);
			if (XamplifyUtils.isValidInteger(formId)) {
				Form existingForm = formDao.getById(formId);
				Map<String, FormLabel> existingLabelsById = existingForm != null ? existingForm.getFormLabels().stream()
						.filter(Objects::nonNull)
						.filter(label -> label.getLabelId() != null && !label.getLabelId().isEmpty())
						.collect(Collectors.toMap(label -> label.getLabelId().trim(), Function.identity(), (a, b) -> a))
						: Collections.emptyMap();
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

	@Override
	public XtremandResponse saveDealPipelinesFromMcp(Integer userId) {
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

		Map<String, Object> pipelineResponse = fetchMcpDealPipelines(pat);
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
				PipelineType.DEAL, integrationType, null);
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
			Pipeline pipeline = upsertDealPipeline(companyProfile, adminId, integrationType, pipelineObject,
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
			pipelineDAO.reassignDealPipelines(pipelinesToRemove, defaultPipeline);
			pipelineDAO.deletePipelineStages(pipelinesToRemove);
			pipelineDAO.deletePipelines(pipelinesToRemove);
		}

		response.setStatusCode(200);
		response.setMessage("Pipeline(s) sync completed successfully.");
		return response;
	}

	private Map<String, Object> fetchMcpDealCustomForm(String accessToken) throws XamplifyDataAccessException {
		if (!XamplifyUtils.isValidString(accessToken)) {
			throw new IllegalArgumentException("accessToken is required");
		}

		String url = buildMcpDealCustomFormUrl();
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
				throw new XamplifyDataAccessException(
						"Unexpected response from MCP: " + response.getStatusCode() + " for URL: " + url);
			}
		} catch (HttpClientErrorException ex) {
			String body = ex.getResponseBodyAsString();
			throw new XamplifyDataAccessException("401 Unauthorized calling MCP. Response body: " + body, ex);
		} catch (RestClientResponseException ex) {
			String body = ex.getResponseBodyAsString();
			throw new XamplifyDataAccessException(
					"Error calling MCP: HTTP " + ex.getRawStatusCode() + ", body: " + body, ex);
		} catch (Exception ex) {
			throw new XamplifyDataAccessException("Error calling MCP", ex);
		}
	}

	private Map<String, Object> fetchMcpDealPipelines(String xAmplifyPat) throws XamplifyDataAccessException {
		String url = buildMcpDealPipelinesUrl();
		if (!XamplifyUtils.isValidString(url)) {
			throw new IllegalStateException("MCP pipelines URL is empty");
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
				throw new XamplifyDataAccessException(
						"Unexpected response from MCP: " + response.getStatusCode() + " for URL: " + url);
			}
		} catch (HttpClientErrorException ex) {
			String body = ex.getResponseBodyAsString();
			throw new XamplifyDataAccessException("401 Unauthorized calling MCP. Response body: " + body, ex);
		} catch (RestClientResponseException ex) {
			String body = ex.getResponseBodyAsString();
			throw new XamplifyDataAccessException(
					"Error calling MCP: HTTP " + ex.getRawStatusCode() + ", body: " + body, ex);
		} catch (Exception ex) {
			throw new XamplifyDataAccessException("Error calling MCP", ex);
		}
	}

	private IntegrationType resolveIntegrationType(Object integrationType) {
		if (integrationType instanceof String) {
			if (!((String) integrationType).isEmpty()) {
				return IntegrationType.CUSTOM_CRM;
			}
		}
		return IntegrationType.XAMPLIFY;
	}

	private FormTypeEnum resolveDealFormType() {
		return FormTypeEnum.CRM_DEAL_CUSTOM_FORM;
	}

	private FormDTO buildDealCustomFormDto(Integer companyId, FormTypeEnum formType, Object fields,
			List<OpportunityFormFieldsDTO> defaultDealFields) {
		CompanyProfile companyProfile = genericDAO.get(CompanyProfile.class, companyId);
		if (companyProfile == null) {
			return null;
		}

		FormDTO formDto = new FormDTO();
		String formNamePrefix = companyProfile.getCompanyName();
		if (!XamplifyUtils.isValidString(formNamePrefix)) {
			formNamePrefix = "Deal";
		}
		formDto.setName(formNamePrefix + " Deal Custom Form");
		formDto.setDescription(formDto.getName());
		formDto.setCompanyName(companyProfile.getCompanyName());
		formDto.setCompanyProfileName(companyProfile.getCompanyProfileName());
		formDto.setFormType(formType);
		Integer adminId = utilDao.findAdminIdByCompanyId(companyId);
		formDto.setCreatedBy(adminId);
		formDto.setUpdatedBy(adminId);

		List<FormLabelDTO> formLabelDTOs = buildDealCustomFormLabels(fields, defaultDealFields);
        if (formLabelDTOs.isEmpty()) {
                return null;
        }
		formDto.setFormLabelDTOs(formLabelDTOs);
		formDto.setFormLabelDTORows(utilService.constructFormRows(formLabelDTOs));

		return formDto;
	}

	private String toString(Object value) {
		return value != null ? String.valueOf(value) : null;
	}

	private List<FormLabelDTO> buildDealCustomFormLabels(Object fieldsObject,
			List<OpportunityFormFieldsDTO> defaultDealFields) {
		if (!(fieldsObject instanceof List<?>)) {
			return buildDealLabelsFromDefaults(defaultDealFields);
		}
		List<?> fieldList = (List<?>) fieldsObject;
		if (fieldList.isEmpty()) {
			return buildDealLabelsFromDefaults(defaultDealFields);
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
	
	private List<FormLabelDTO> buildDealLabelsFromDefaults(List<OpportunityFormFieldsDTO> defaultDealFields) {
		if (defaultDealFields == null || defaultDealFields.isEmpty()) {
			return Collections.emptyList();
		}
		List<FormLabelDTO> labels = new ArrayList<>();
		int order = 1;
		for (OpportunityFormFieldsDTO field : defaultDealFields) {
			if (field == null || !XamplifyUtils.isValidString(field.getLabel())) {
				continue;
			}
			FormLabelDTO dto = new FormLabelDTO();
			String labelId = XamplifyUtils.isValidString(field.getName()) ? field.getName().trim()
					: XamplifyUtils.replaceSpacesWithUnderScore(field.getLabel());
			dto.setLabelId(labelId);
			dto.setHiddenLabelId(labelId);
			dto.setLabelName(field.getLabel());
			dto.setDisplayName(
					XamplifyUtils.isValidString(field.getDisplayName()) ? field.getDisplayName() : field.getLabel());
			dto.setLabelType(normalizeLabelType(field.getType()));
			dto.setRequired(field.isRequired());
			dto.setPlaceHolder(field.getPlaceHolder());
			dto.setOrder(order);
			dto.setColumnOrder(order++);
			dto.setActive(true);
			dto.setFormDefaultFieldType(field.getFormDefaultFieldType());
			dto.setFormFieldType(field.getFormFieldType());
			dto.setNonInteractive(field.isNonInteractive());
			dto.setPrivate(field.isPrivate());
			dto.setEmailNotificationEnabledOnUpdate(field.isEmailNotificationEnabledOnUpdate());

			List<FormChoiceDTO> choiceDTOs = buildChoiceDtosFromPicklist(field.getOptions());
			applyChoicesByType(dto, choiceDTOs);
			labels.add(dto);
		}
		return labels;
	}
	
	private String normalizeLabelType(String labelType) {
		if (!XamplifyUtils.isValidString(labelType)) {
			return FieldLabelType.TEXT.getType();
		}
		switch (labelType.trim().toLowerCase(Locale.ROOT)) {
		case "text":
			return FieldLabelType.TEXT.getType();
		case "text area":
		case "textarea":
			return FieldLabelType.TEXTAREA.getType();
		case "drop down":
		case "select":
			return FieldLabelType.SELECT.getType();
		case "email":
			return FieldLabelType.EMAIL.getType();
		case "phone":
			return FieldLabelType.PHONE.getType();
		case "url":
			return FieldLabelType.URL.getType();
		case "check box":
		case "checkbox":
			return FieldLabelType.CHECKBOX.getType();
		case "number":
			return FieldLabelType.NUMBER.getType();
		case "currency":
			return FieldLabelType.CURRENCY.getType();
		case "date":
			return FieldLabelType.DATE.getType();
		default:
			return labelType.trim();
		}
	}

	private List<FormChoiceDTO> buildChoiceDtosFromPicklist(List<PicklistValues> options) {
		if (options == null || options.isEmpty()) {
			return Collections.emptyList();
		}
		List<FormChoiceDTO> choices = new ArrayList<>();
		for (PicklistValues option : options) {
			if (option == null || !XamplifyUtils.isValidString(option.getLabel())) {
				continue;
			}
			FormChoiceDTO choiceDTO = new FormChoiceDTO();
			choiceDTO.setLabelId(option.getLabel());
			choiceDTO.setHiddenLabelId(XamplifyUtils.replaceSpacesWithUnderScore(option.getLabel()));
			choiceDTO.setName(option.getLabel());
			choices.add(choiceDTO);
		}
		return choices;
	}

	private void applyChoicesByType(FormLabelDTO dto, List<FormChoiceDTO> choiceDTOs) {
		if (choiceDTOs.isEmpty()) {
			return;
		}
		String labelType = dto.getLabelType() != null && !dto.getLabelType().isEmpty()
				? dto.getLabelType().toLowerCase(Locale.ROOT)
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
			boolean isDefault = Boolean.TRUE.equals(optionMap.get("default")) || (defaultChoiceValue != null
					&& !defaultChoiceValue.isEmpty() && defaultChoiceValue.equals(choiceDTO.getLabelId()));
			choiceDTO.setDefaultColumn(isDefault);
			choices.add(choiceDTO);
		}
		return choices;
	}

	private String buildMcpDealCustomFormUrl() {
		if (!baseUrl.endsWith("/")) {
			baseUrl = baseUrl + "/";
		}
		return baseUrl + "deal/custom-form";
//		return "http://localhost:8080/deal/custom-form";
	}

	private String buildMcpDealPipelinesUrl() {
		if (!baseUrl.endsWith("/")) {
			baseUrl = baseUrl + "/";
		}
		return baseUrl + "deal/pipelines";
//		return "http://localhost:8080/deal/pipelines";
	}

	private Pipeline upsertDealPipeline(CompanyProfile companyProfile, Integer adminId, IntegrationType integrationType,
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
			pipeline.setType(PipelineType.DEAL);
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

		upsertDealPipelineStages(pipeline, pipelineMap.get("stages"), adminId);
		return pipeline;
	}

	private void upsertDealPipelineStages(Pipeline pipeline, Object stagesObject, Integer adminId) {
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
			if (XamplifyUtils.isValidString(externalStageId)
					&& !externalStageId.equals(stage.getExternalPipelineStageId())) {
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
	
	@Override
	public void saveAndPushDealToxAmplify(DealDto dealDto) {
		if (dealDto != null && dealDto.getId() != null && dealDto.getId() > 0) {
			try {
				String pat = integrationDao.fetchActiveIntegrationPAT(dealDto.getCreatedForCompanyId());
				if (XamplifyUtils.isValidString(pat)) {
					UserDTO userDTO = userDao.getSendorCompanyDetailsByUserId(dealDto.getUserId());
					dealDto.setDealId(dealDto.getId());
					if (userDTO != null) {
						dealDto.setCreatedByCompanyName(userDTO.getCompanyName());
						dealDto.setPartnerCompanyId(userDTO.getCompanyId());
						dealDto.setCreatedByEmail(userDTO.getEmailId());
						dealDto.setCreatedByName(userDTO.getFullName());
					}

					populatePipelineDetailsForXamplifySync(dealDto);
					populateLeadAndContactDetailsForXamplifySync(dealDto);
					createPrmDeal(pat, dealDto);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	
	private Map<String, Object> createPrmDeal(String patToken, DealDto dealDto) throws XamplifyDataAccessException {

		if (!XamplifyUtils.isValidString(patToken)) {
			throw new IllegalArgumentException("patToken is required");
		}
		if (dealDto == null) {
			throw new IllegalArgumentException("dealDto is required");
		}

		if (!baseUrl.endsWith("/")) {
			baseUrl = baseUrl + "/";
		}
		String url = baseUrl + "deal/create";
		LOGGER.info("Deal create URL: " + url);
//		String url = "http://localhost:8080/deal/create";

		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.AUTHORIZATION, BEARER + patToken.trim());
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

		HttpEntity<DealDto> requestEntity = new HttpEntity<>(dealDto, headers);
		RestTemplate restTemplate = new RestTemplate();

		try {
			ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity,
					new ParameterizedTypeReference<Map<String, Object>>() {
					});

			HttpStatus status = response.getStatusCode();
			if (status.is2xxSuccessful()) {
				LOGGER.info("Response body: " + response.getBody());
				return response.getBody();
			} else if (status == HttpStatus.UNAUTHORIZED) {
				throw new XamplifyDataAccessException("Received 401 Unauthorized calling createPRMDeal at URL: " + url);
			} else if (status == HttpStatus.FORBIDDEN) {
				throw new XamplifyDataAccessException("Received 403 Forbidden calling createPRMDeal at URL: " + url);
			} else {
				throw new XamplifyDataAccessException(
						"Unexpected status " + status.value() + " calling createPRMDeal at URL: " + url);
			}

		} catch (HttpClientErrorException ex) {
			String body = ex.getResponseBodyAsString();
			throw new XamplifyDataAccessException(
					"Client error calling createPRMDeal. HTTP " + ex.getStatusCode().value() + ", body: " + body, ex);

		} catch (RestClientResponseException ex) {
			String body = ex.getResponseBodyAsString();
			throw new XamplifyDataAccessException(
					"Error calling createPRMDeal. HTTP " + ex.getRawStatusCode() + ", body: " + body, ex);

		} catch (Exception ex) {
			throw new XamplifyDataAccessException("Unexpected error calling createPRMDeal", ex);
		}
	}
	
	private void populatePipelineDetailsForXamplifySync(DealDto dealDto) {
		PipelineStage pipelineStage = null;
		if (XamplifyUtils.isValidInteger(dealDto.getPipelineStageId())) {
			pipelineStage = genericDAO.get(PipelineStage.class, dealDto.getPipelineStageId());
		}

		if (pipelineStage != null) {
			dealDto.setExternalPipelineStageId(pipelineStage.getExternalPipelineStageId());
			dealDto.setPipelineStageName(pipelineStage.getStageName());

			Pipeline pipeline = pipelineStage.getPipeline();
			if (pipeline != null) {
				dealDto.setExternalPipelineId(pipeline.getExternalPipelineId());
				dealDto.setPipelineName(pipeline.getName());
			}
		}
	}

	private void populateLeadAndContactDetailsForXamplifySync(DealDto dealDto) {
		Lead associatedLead = null;
		if (XamplifyUtils.isValidInteger(dealDto.getAssociatedLeadId())) {
			associatedLead = genericDAO.get(Lead.class, dealDto.getAssociatedLeadId());
			if (associatedLead != null) {
				LeadDto associatedLeadDto = buildLeadDtoForXamplifySync(associatedLead);
				dealDto.setLeadDto(associatedLeadDto);
				dealDto.setAssociatedLead(associatedLeadDto);
				dealDto.setLeadName(buildFullName(associatedLead.getFirstName(), associatedLead.getLastName()));
				dealDto.setLeadCompany(associatedLead.getCompany());
			}
		}

		UserDTO contactDto = null;
		if (XamplifyUtils.isValidInteger(dealDto.getAssociatedContactId())) {
			UserUserList associatedContact = genericDAO.get(UserUserList.class, dealDto.getAssociatedContactId());
			contactDto = buildContactDto(associatedContact != null ? associatedContact.getUser() : null,
					associatedContact);
		} else if (associatedLead != null && associatedLead.getAssociatedUser() != null) {
			contactDto = buildContactDto(associatedLead.getAssociatedUser(), null);
		}

		if (contactDto != null) {
			dealDto.setContactDto(contactDto);
			dealDto.setAssociatedContact(contactDto);
			dealDto.setContactName(buildFullName(contactDto.getFirstName(), contactDto.getLastName()));
			dealDto.setContactEmailId(contactDto.getEmailId());
		}
	}
	
	private LeadDto buildLeadDtoForXamplifySync(Lead associatedLead) {
		LeadDto leadDto = new LeadDto();
		leadDto.setId(associatedLead.getId());
		leadDto.setLeadId(associatedLead.getId());
		leadDto.setFirstName(associatedLead.getFirstName());
		leadDto.setLastName(associatedLead.getLastName());
		leadDto.setCompany(associatedLead.getCompany());
		leadDto.setEmail(associatedLead.getEmail());
		leadDto.setPhone(associatedLead.getPhone());
		leadDto.setWebsite(associatedLead.getWebsite());
		leadDto.setStreet(associatedLead.getStreet());
		leadDto.setCity(associatedLead.getCity());
		leadDto.setState(associatedLead.getState());
		leadDto.setCountry(associatedLead.getCountry());
		leadDto.setPostalCode(associatedLead.getPostalCode());
		leadDto.setTitle(associatedLead.getTitle());
		leadDto.setIndustry(associatedLead.getIndustry());
		leadDto.setRegion(associatedLead.getRegion());
		leadDto.setReferenceId(associatedLead.getReferenceId());

		if (associatedLead.getCreatedByCompany() != null) {
			leadDto.setCreatedByCompanyId(associatedLead.getCreatedByCompany().getId());
			leadDto.setCreatedByCompanyName(associatedLead.getCreatedByCompany().getCompanyName());
		}

		if (associatedLead.getCreatedForCompany() != null) {
			leadDto.setCreatedForCompanyId(associatedLead.getCreatedForCompany().getId());
			leadDto.setCreatedForCompanyName(associatedLead.getCreatedForCompany().getCompanyName());
		}

		if (associatedLead.getPipeline() != null) {
			leadDto.setPipelineId(associatedLead.getPipeline().getId());
			leadDto.setExternalPipelineId(associatedLead.getPipeline().getExternalPipelineId());
			leadDto.setPipelineName(associatedLead.getPipeline().getName());
		}

		if (associatedLead.getCurrentStage() != null) {
			leadDto.setPipelineStageId(associatedLead.getCurrentStage().getId());
			leadDto.setCurrentStageId(associatedLead.getCurrentStage().getId());
			leadDto.setCurrentStageName(associatedLead.getCurrentStage().getStageName());
			leadDto.setExternalPipelineStageId(associatedLead.getCurrentStage().getExternalPipelineStageId());
			leadDto.setPipelineStageName(associatedLead.getCurrentStage().getStageName());
		}

		if (associatedLead.getCreatedForPipeline() != null) {
			leadDto.setCreatedForPipelineId(associatedLead.getCreatedForPipeline().getId());
			leadDto.setCreatedForPipeline(associatedLead.getCreatedForPipeline().getName());
			leadDto.setExternalPipelineId(associatedLead.getCreatedForPipeline().getExternalPipelineId());
		}

		if (associatedLead.getCreatedForPipelineStage() != null) {
			leadDto.setCreatedForPipelineStageId(associatedLead.getCreatedForPipelineStage().getId());
			leadDto.setCreatedForPipelineStage(associatedLead.getCreatedForPipelineStage().getStageName());
			leadDto.setExternalPipelineStageId(
					associatedLead.getCreatedForPipelineStage().getExternalPipelineStageId());
		}

		if (associatedLead.getCreatedByPipeline() != null) {
			leadDto.setCreatedByPipelineId(associatedLead.getCreatedByPipeline().getId());
			leadDto.setCreatedByPipeline(associatedLead.getCreatedByPipeline().getName());
		}

		if (associatedLead.getCreatedByPipelineStage() != null) {
			leadDto.setCreatedByPipelineStageId(associatedLead.getCreatedByPipelineStage().getId());
			leadDto.setCreatedByPipelineStage(associatedLead.getCreatedByPipelineStage().getStageName());
		}

		return leadDto;
	}

	private UserDTO buildContactDto(User contactUser, UserUserList associatedContact) {
		if (contactUser == null && associatedContact == null) {
			return null;
		}

		UserDTO contactDto = new UserDTO();
		if (contactUser != null) {
			contactDto.setId(contactUser.getUserId());
			contactDto.setFirstName(contactUser.getFirstName());
			contactDto.setLastName(contactUser.getLastName());
			contactDto.setEmailId(contactUser.getEmailId());
			contactDto.setMobileNumber(contactUser.getMobileNumber());
			contactDto.setPartnerType(contactUser.getPartnerType());
		}

		if (associatedContact != null) {
			if (contactDto.getFirstName() == null) {
				contactDto.setFirstName(associatedContact.getFirstName());
			}
			if (contactDto.getLastName() == null) {
				contactDto.setLastName(associatedContact.getLastName());
			}
			if (contactDto.getMobileNumber() == null) {
				contactDto.setMobileNumber(associatedContact.getMobileNumber());
			}
			contactDto.setJobTitle(associatedContact.getJobTitle());
			contactDto.setCompanyName(associatedContact.getContactCompany());
			contactDto.setContactCompany(associatedContact.getContactCompany());
			contactDto.setRegion(associatedContact.getRegion());
			contactDto.setVertical(associatedContact.getVertical());
		}
		return contactDto;
	}
	
	private String buildFullName(String firstName, String lastName) {
		if (StringUtils.isBlank(firstName) && StringUtils.isBlank(lastName)) {
			return null;
		}
		if (StringUtils.isBlank(firstName)) {
			return lastName;
		}
		if (StringUtils.isBlank(lastName)) {
			return firstName;
		}
		return (firstName + " " + lastName).trim();
	}
	
	@Override
	public void updateAndPushDealToxAmplify(DealDto dealDto) {
		if (dealDto != null && dealDto.getId() != null && dealDto.getId() > 0) {
			try {
				String pat = integrationDao.fetchActiveIntegrationPAT(dealDto.getCreatedForCompanyId());
				if (XamplifyUtils.isValidString(pat)) {
					populateDealDtoForXamplifySync(dealDto);
					updatePrmDeal(pat, dealDto);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	private void populateDealDtoForXamplifySync(DealDto dealDto) {
		UserDTO userDTO = userDao.getSendorCompanyDetailsByUserId(dealDto.getUserId());
		dealDto.setDealId(dealDto.getId());
		if (userDTO != null) {
			dealDto.setCreatedByCompanyName(userDTO.getCompanyName());
			dealDto.setPartnerCompanyId(userDTO.getCompanyId());
			dealDto.setCreatedByEmail(userDTO.getEmailId());
			dealDto.setCreatedByName(userDTO.getFullName());
		}

		populatePipelineDetailsForXamplifySync(dealDto);
		populateLeadAndContactDetailsForXamplifySync(dealDto);
	}
	
	private Map<String, Object> updatePrmDeal(String patToken, DealDto dealDto) throws XamplifyDataAccessException {

		if (!XamplifyUtils.isValidString(patToken)) {
			throw new IllegalArgumentException("patToken is required");
		}
		if (dealDto == null) {
			throw new IllegalArgumentException("dealDto is required");
		}

		if (!baseUrl.endsWith("/")) {
			baseUrl = baseUrl + "/";
		}
		String url = baseUrl + "deal/update";
		LOGGER.info("Deal update URL: " + url);
//		String url = "http://localhost:8080/deal/update";

		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.AUTHORIZATION, BEARER + patToken.trim());
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

		HttpEntity<DealDto> requestEntity = new HttpEntity<>(dealDto, headers);
		RestTemplate restTemplate = new RestTemplate();

		try {
			ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity,
					new ParameterizedTypeReference<Map<String, Object>>() {
					});

			HttpStatus status = response.getStatusCode();

			if (status.is2xxSuccessful()) {
				LOGGER.info("Response body: " + response.getBody());
				return response.getBody();
			} else if (status == HttpStatus.UNAUTHORIZED) {
				throw new XamplifyDataAccessException("Received 401 Unauthorized calling updatePRMDeal at URL: " + url);
			} else if (status == HttpStatus.FORBIDDEN) {
				throw new XamplifyDataAccessException("Received 403 Forbidden calling updatePRMDeal at URL: " + url);
			} else {
				throw new XamplifyDataAccessException(
						"Unexpected status " + status.value() + " calling updatePRMDeal at URL: " + url);
			}

		} catch (HttpClientErrorException ex) {
			String body = ex.getResponseBodyAsString();
			throw new XamplifyDataAccessException(
					"Client error calling updatePRMDeal. HTTP " + ex.getStatusCode().value() + ", body: " + body, ex);

		} catch (RestClientResponseException ex) {
			String body = ex.getResponseBodyAsString();
			throw new XamplifyDataAccessException(
					"Error calling updatePRMDeal. HTTP " + ex.getRawStatusCode() + ", body: " + body, ex);

		} catch (Exception ex) {
			throw new XamplifyDataAccessException("Unexpected error calling updatePRMDeal", ex);
		}
	}
	
	@Override
	public void updateDealStatusToxAmplify(DealDto dealDto) {
		if (dealDto == null || !XamplifyUtils.isValidInteger(dealDto.getId())
				|| !XamplifyUtils.isValidInteger(dealDto.getPipelineStageId())
				|| !XamplifyUtils.isValidInteger(dealDto.getUserId())) {
			return;
		}

		Deal deal = genericDAO.get(Deal.class, dealDto.getId());
		PipelineStage pipelineStage = genericDAO.get(PipelineStage.class, dealDto.getPipelineStageId());
		if (deal == null || pipelineStage == null) {
			return;
		}
		
		String pat = integrationDao.fetchActiveIntegrationPAT(deal.getCreatedForCompany().getId());

		if (XamplifyUtils.isValidString(pat)) {
			DealStatusUpdateRequest statusUpdateRequest = new DealStatusUpdateRequest();
			statusUpdateRequest.setDealId(deal.getId());
			statusUpdateRequest.setPipelineStageName(pipelineStage.getStageName());

			Integer updaterCompanyId = userDao.getCompanyIdByUserId(dealDto.getUserId());
			User updater = genericDAO.get(User.class, dealDto.getUserId());
			if (updater != null && XamplifyUtils.isValidInteger(updaterCompanyId)) {
				String updaterFullName = buildFullName(updater.getFirstName(), updater.getLastName());
				if (deal.getCreatedForCompany() != null
						&& Objects.equals(updaterCompanyId, deal.getCreatedForCompany().getId())) {
					statusUpdateRequest.setVendorUsername(updaterFullName);
					statusUpdateRequest.setVendorEmailId(updater.getEmailId());
				} else if (deal.getCreatedByCompany() != null
						&& Objects.equals(updaterCompanyId, deal.getCreatedByCompany().getId())) {
					statusUpdateRequest.setPartnerCompanyId(updaterCompanyId);
					statusUpdateRequest.setPartnerUsername(updaterFullName);
					statusUpdateRequest.setPartnerEmailId(updater.getEmailId());
				}
			}

			pushDealStatusUpdateToXamplify(statusUpdateRequest, pat);
		}
	}

	private void pushDealStatusUpdateToXamplify(DealStatusUpdateRequest statusUpdateRequest, String xAmplifyPat)
			throws XamplifyDataAccessException {
		if (statusUpdateRequest == null || !XamplifyUtils.isValidInteger(statusUpdateRequest.getDealId())
				|| !XamplifyUtils.isValidString(statusUpdateRequest.getPipelineStageName())) {
			return;
		}

		if (!baseUrl.endsWith("/")) {
			baseUrl = baseUrl + "/";
		}
		String url = baseUrl + "deal/status/update";
		LOGGER.info("Deal status update URL: " + url);
//		String url = "http://localhost:8080/deal/status/update";

		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.AUTHORIZATION, BEARER + xAmplifyPat.trim());
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<DealStatusUpdateRequest> requestEntity = new HttpEntity<>(statusUpdateRequest, headers);

		try {
			ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Void.class);
			HttpStatus status = response.getStatusCode();
			LOGGER.info("Response status code: " + response.getStatusCode());
			if (!status.is2xxSuccessful()) {
				throw new XamplifyDataAccessException(
						"Unexpected status " + status.value() + " calling updateDealStatus at URL: " + url);
			}

		} catch (HttpClientErrorException ex) {
			String body = ex.getResponseBodyAsString();
			throw new XamplifyDataAccessException(
					"Client error calling updateDealStatus. HTTP " + ex.getStatusCode().value() + ", body: " + body,
					ex);

		} catch (RestClientResponseException ex) {
			String body = ex.getResponseBodyAsString();
			throw new XamplifyDataAccessException(
					"Error calling updateDealStatus. HTTP " + ex.getRawStatusCode() + ", body: " + body, ex);
		}
	}
	
	@Override
	public XtremandResponse syncDeals(Integer userId, Integer companyId) {
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

		List<Deal> deals = dealDAO.findDealsByCreatedForCompanyId(companyId);
		if (deals == null || deals.isEmpty()) {
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
		for (Deal deal : deals) {
			try {
				String openSourceDealId = deal.getId().toString();
				if (!XamplifyUtils.isValidString(openSourceDealId)) {
					failed++;
					continue;
				}

				DealSyncDetailsDto dealDetails = fetchOpenSourceDeal(openSourceDealId, activeCRMIntegration.getAccessToken());
				applyDealUpdates(deal, dealDetails);
				updateDealPipeline(deal, dealDetails, companyId, activeIntegrationType);
				updateSfCustomFieldsData(deal, dealDetails, companyId, activeIntegrationType,
						otherActiveCRMIntegration);
				genericDAO.update(deal);
				synced++;
			} catch (Exception ex) {
				failed++;
				LOGGER.error("Error syncing deal {} for company {}", deal.getId(), companyId, ex);
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
	
	private DealSyncDetailsDto fetchOpenSourceDeal(String openSourceDealId, String xAmplifyPat) {
		if (!XamplifyUtils.isValidString(openSourceDealId)) {
			throw new IllegalArgumentException("openSourceDealId is required");
		}

		String url = baseUrl + "deal/getById/" + openSourceDealId.trim();
		LOGGER.info("Deal sync by Id URL: " + url);
//		String url = "http://localhost:8080/deal/getById/" + openSourceDealId.trim();
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		if (StringUtils.isNotBlank(xAmplifyPat)) {
			headers.set(HttpHeaders.AUTHORIZATION, BEARER + xAmplifyPat.trim());
		}

		HttpEntity<Void> entity = new HttpEntity<>(headers);
		RestTemplate restTemplate = new RestTemplate();

		ResponseEntity<Map<String, DealSyncDetailsDto>> response = restTemplate.exchange(url, HttpMethod.GET, entity,
				new ParameterizedTypeReference<Map<String, DealSyncDetailsDto>>() {
				});
		HttpStatus status = response.getStatusCode();

		if (status.is2xxSuccessful() && response.getBody() != null) {
//			return response.getBody();
			if (response.getBody().containsKey("details")) {
				return (DealSyncDetailsDto) response.getBody().get("details");
			}
		}

		String message = "Failed to fetch deal details from open source for id: " + openSourceDealId;
		throw new XamplifyDataAccessException(message);
	}

	private void applyDealUpdates(Deal deal, DealSyncDetailsDto dealDetails) {
		if (deal == null || dealDetails == null) {
			return;
		}

		if (dealDetails.getTitle() != null) {
			deal.setTitle(dealDetails.getTitle());
		}
		if (dealDetails.getAmount() != null) {
			deal.setAmount(dealDetails.getAmount());
		}
		Date syncedCloseDate = parseCloseDate(dealDetails.getCloseDate());
		if (syncedCloseDate != null) {
			deal.setCloseDate(syncedCloseDate);
		}
//		if (dealDetails.getOpenSourceDealId() != null) {
//			deal.setReferenceId(String.valueOf(dealDetails.getOpenSourceDealId()));
//		} else 
		if (StringUtils.isNotBlank(dealDetails.getReferenceId())) {
			deal.setReferenceId(dealDetails.getReferenceId());
		}
	}

	private Date parseCloseDate(String closeDate) {
		if (StringUtils.isBlank(closeDate)) {
			return null;
		}

		String trimmedCloseDate = closeDate.trim();
		
		if (trimmedCloseDate.matches("^-?\\d+$")) { // only digits, maybe leading -
			try {
				long epochValue = Long.parseLong(trimmedCloseDate);

				// Heuristic: 10 digits -> seconds, 13 digits -> millis
				if (trimmedCloseDate.length() <= 10) {
					return Date.from(Instant.ofEpochSecond(epochValue));
				} else {
					return Date.from(Instant.ofEpochMilli(epochValue));
				}
			} catch (NumberFormatException ignored) {
				// fall through to other parsers
			}
		}
		
		try {
			OffsetDateTime offsetDateTime = OffsetDateTime.parse(trimmedCloseDate, DateTimeFormatter.ISO_DATE_TIME);
			return Date.from(offsetDateTime.toInstant());
		} catch (DateTimeParseException ignored) {
		}

		try {
			LocalDateTime localDateTime = LocalDateTime.parse(trimmedCloseDate, DateTimeFormatter.ISO_DATE_TIME);
			return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
		} catch (DateTimeParseException ignored) {
		}

		try {
			LocalDate localDate = LocalDate.parse(trimmedCloseDate, DateTimeFormatter.ISO_DATE);
			return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
		} catch (DateTimeParseException ignored) {
		}

		return null;
	}

	private void updateDealPipeline(Deal deal, DealSyncDetailsDto dealDetails, Integer companyId,
			IntegrationType activeIntegrationType) {
		if (deal == null || dealDetails == null) {
			return;
		}

		Pipeline pipeline = null;
		if (XamplifyUtils.isValidString(dealDetails.getExternalPipelineId()) && activeIntegrationType != null) {
			pipeline = pipelineDAO.getDealPipelineByExternalPipelineId(companyId, dealDetails.getExternalPipelineId(),
					activeIntegrationType);
		}

		if (pipeline == null && XamplifyUtils.isValidString(dealDetails.getPipelineName())) {
			pipeline = pipelineDAO.getPipeLineByName(companyId, dealDetails.getPipelineName(), PipelineType.DEAL);
		}

		if (pipeline != null) {
			deal.setPipeline(pipeline);
			deal.setCreatedForPipeline(pipeline);
			PipelineStage stage = pipelineDAO.getPipelineStageByExternalPipelineStageId(companyId, pipeline.getId(),
					dealDetails.getExternalPipelineStageId(), dealDetails.getPipelineStageName());
			if (stage == null) {
				stage = pipelineDAO.getDefaultStage(pipeline.getId());
			}
			if (stage == null) {
				stage = pipelineDAO.findFallbackStage(pipeline.getId());
			}
			if (stage != null) {
				deal.setCurrentStage(stage);
				deal.setCreatedForPipelineStage(stage);
			}
		}
	}

	private void updateSfCustomFieldsData(Deal deal, DealSyncDetailsDto dealDetails, Integer activeCRMCompanyId,
			IntegrationType activeCRMIntegrationType, Integration otherActiveCRMIntegration) {
		if (activeCRMCompanyId != null && activeCRMCompanyId > 0 && activeCRMIntegrationType != null) {
			List<FormLabel> activeCRMCFLabels = utilService.getDealCustomFormLabelsByIntegrationType(activeCRMCompanyId,
					activeCRMIntegrationType);

			List<FormLabel> otherActiveCRMCFLabels = new ArrayList<>();
			if (otherActiveCRMIntegration != null) {
				otherActiveCRMCFLabels = utilService.getDealCustomFormLabelsByIntegrationType(
						otherActiveCRMIntegration.getCompany().getId(), otherActiveCRMIntegration.getType());
			}

			Map<String, String> customFieldValues = buildCustomFieldValues(dealDetails);

			for (FormLabel formLabel : activeCRMCFLabels) {
				if (formLabel != null) {
					saveOrUpdateSfCustomFieldsData(deal, customFieldValues, formLabel, null);
					FormLabel matchingFormLabel = utilService.getMatchedObject(formLabel, otherActiveCRMCFLabels);
					if (matchingFormLabel != null) {
						saveOrUpdateSfCustomFieldsData(deal, customFieldValues, matchingFormLabel, formLabel);
					}
				}
			}
		}
	}

	private Map<String, String> buildCustomFieldValues(DealSyncDetailsDto dealDetails) {
		Map<String, String> customFieldValues = new HashMap<>();
		if (dealDetails != null && dealDetails.getSfCustomFieldsData() != null) {
			for (SfCustomFieldsDataDTO dataDTO : dealDetails.getSfCustomFieldsData()) {
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

	private void saveOrUpdateSfCustomFieldsData(Deal deal, Map<String, String> customFieldValues, FormLabel formLabel,
			FormLabel formLabelInOtherForm) {
		SfCustomFieldsData sfCustomFieldsData = sfCustomFormDataDAO.getSfCustomFieldDataByDealIdAndLabelId(deal,
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
				sfCustomFieldsData.setDeal(deal);
				sfCustomFieldsData.setFormLabel(formLabel);
				sfCustomFieldsData.setValue(value);
				sfCustomFormDataDAO.saveSfCfData(sfCustomFieldsData);
			}
		}

	}

	@Override
	public XtremandResponse syncDeals(Integer userId) {
		XtremandResponse response = new XtremandResponse();
		if (!XamplifyUtils.isValidInteger(userId)) {
			response.setStatusCode(400);
			response.setMessage(INVALID_INPUT);
			return response;
		}
		Integer companyId = userDao.getCompanyIdByUserId(userId);
		return syncDeals(userId, companyId);
	}

}
