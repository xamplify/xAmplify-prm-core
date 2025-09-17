package com.xtremand.deal.service.impl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.activity.dto.ContactOpportunityRequestDTO;
import com.xtremand.campaign.bom.DownloadDataInfo;
import com.xtremand.campaign.bom.DownloadDataInfo.DownloadItem;
import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.Criteria;
import com.xtremand.common.bom.Criteria.OPERATION_NAME;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.common.bom.Pagination;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.deal.bom.Deal;
import com.xtremand.deal.bom.DealStage;
import com.xtremand.deal.dao.DealDAO;
import com.xtremand.deal.dto.DealCountsResponseDTO;
import com.xtremand.deal.dto.DealDto;
import com.xtremand.deal.dto.VendorSelfDealRequestDTO;
import com.xtremand.deal.service.DealService;
import com.xtremand.flexi.fields.dao.FlexiFieldDao;
import com.xtremand.flexi.fields.dto.FlexiFieldRequestDTO;
import com.xtremand.form.bom.Form;
import com.xtremand.form.bom.FormLabel;
import com.xtremand.form.bom.FormTypeEnum;
import com.xtremand.form.dao.FormDao;
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
import com.xtremand.lead.dto.PipelineDto;
import com.xtremand.lead.dto.PipelineStageDto;
import com.xtremand.mail.service.AsyncService;
import com.xtremand.mail.service.MailService;
import com.xtremand.partnership.dao.PartnershipDAO;
import com.xtremand.pipeline.dao.PipelineDAO;
import com.xtremand.salesforce.bom.SfCustomFieldsData;
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

}
