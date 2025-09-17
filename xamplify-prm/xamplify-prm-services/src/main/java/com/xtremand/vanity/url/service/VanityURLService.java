package com.xtremand.vanity.url.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import com.xtremand.activity.bom.ActivityAttachment;
import com.xtremand.activity.bom.EmailActivity;
import com.xtremand.activity.bom.EmailRecipient;
import com.xtremand.activity.bom.EmailRecipientEnum;
import com.xtremand.activity.dto.ActivityAWSDTO;
import com.xtremand.activity.dto.EmailActivityStatusEnum;
import com.xtremand.activity.validator.EmailActivityValidator;
import com.xtremand.aws.AmazonWebService;
import com.xtremand.aws.CopiedFileDetails;
import com.xtremand.campaign.bom.ModuleAccess;
import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.CompanyProfile.LoginStyleType;
import com.xtremand.common.bom.Criteria;
import com.xtremand.common.bom.Criteria.OPERATION_NAME;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.common.bom.Pagination;
import com.xtremand.company.dao.CompanyProfileDao;
import com.xtremand.dam.exception.DamDataAccessException;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.dashboard.button.dao.DashboardButtonDao;
import com.xtremand.dashboard.buttons.bom.DashboardButton;
import com.xtremand.dashboard.buttons.dto.DashboardAlternateUrlDTO;
import com.xtremand.dashboard.buttons.dto.DashboardButtonsDTO;
import com.xtremand.form.emailtemplate.dto.SendTestEmailDTO;
import com.xtremand.formbeans.EmailTemplateDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.mail.service.MailService;
import com.xtremand.mail.service.MailService.EmailBuilder;
import com.xtremand.partnership.bom.Partnership;
import com.xtremand.partnership.bom.Partnership.PartnershipStatus;
import com.xtremand.partnership.bom.PartnershipDTO;
import com.xtremand.partnership.dao.PartnershipDAO;
import com.xtremand.team.dao.TeamDao;
import com.xtremand.user.bom.Role;
import com.xtremand.user.bom.TeamMember;
import com.xtremand.user.bom.User;
import com.xtremand.user.bom.User.UserStatus;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.user.exception.UserDataAccessException;
import com.xtremand.user.service.UserService;
import com.xtremand.userlist.dao.UserListDAO;
import com.xtremand.util.FileUtil;
import com.xtremand.util.XamplifyUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.HibernateSQLQueryResultUtilDao;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.dto.CompanyDetailsDTO;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.QueryParameterDTO;
import com.xtremand.util.dto.XamplifyConstants;
import com.xtremand.util.dto.XamplifyUtilValidator;
import com.xtremand.util.service.UtilService;
import com.xtremand.vanity.email.templates.bom.CustomDefaultEmailTemplate;
import com.xtremand.vanity.email.templates.bom.DefaultEmailTemplate;
import com.xtremand.vanity.email.templates.bom.DefaultEmailTemplateDTO;
import com.xtremand.vanity.email.templates.bom.DefaultEmailTemplateType;
import com.xtremand.vanity.url.dao.VanityURLDao;
import com.xtremand.vanity.url.dto.VanityURLDTO;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;

@Service
@Transactional
public class VanityURLService {

	private static final String CUSTOM_TEMPLATES = "customTemplates";

	private static final String TOTAL_RECORDS = "totalRecords";

	private static final String DEFAULT_TEMPLATES = "defaultTemplates";

	private static final String VANITY_COMPANY_LOGO = "<Vanity_Company_Logo>";

	private static final Logger logger = LoggerFactory.getLogger(VanityURLService.class);

	private static final String DB_BUTTONS = "dbButtons";

	private static final String VANITY_COMPANY_LOGO_HREF = "<Vanity_Company_Logo_Href>";

	private static final String VENDOR_COMPANY_NAME = "{{VENDOR_COMPANY_NAME}}";

	@Value("${email}")
	String fromEmail;

	/***** XNFR-233 *****/
	@Value("${processingGifPath}")
	private String processingGifPath;
	/***** XNFR-233 *****/

	@Value("${company.logo.url}")
	private String defaultCompanyLogoUrl;

	@Value("${server_path}")
	String server_path;

	@Autowired
	private VanityURLDao vanityURLDao;

	@Autowired
	private UserService userService;

	@Autowired
	private PartnershipDAO partnerShipDao;

	@Autowired
	private TeamDao teamDao;

	@Autowired
	private UtilService utilService;

	@Autowired
	private GenericDAO genericDao;

	@Autowired
	private UserDAO userDao;

	@Autowired
	private CompanyProfileDao companyProfileDao;

	@Value("${web_url}")
	String webUrl;

	@Value("${super.admin.email}")
	private String superAdminEmail;

	@Value("${replace.company.logo}")
	private String replaceCompanyLogo;

	@Value("${co.branding.logo}")
	String coBrandingLogo;

	@Value("${xamplify.logo}")
	String xAmplifyLogo;

	@Autowired
	private XamplifyUtil xamplifyUtil;

	@Autowired
	private UtilDao utilDao;

	@Autowired
	private XamplifyUtilValidator xamplifyUtilValidator;

	@Autowired
	private MailService mailService;

	/**** XNFR-233 ***/

	@Autowired
	private UserListDAO userListDao;

	@Autowired
	private HibernateSQLQueryResultUtilDao hibernateSQLQueryResultUtilDao;

	@Autowired
	private DashboardButtonDao dashboardButtonDao;

	@Autowired
	private UserDAO userDAO;

	@Value("${server_path}")
	String serverPath;

	@Value("${email.activity.attachments}")
	private String attachmentPath;

	@Autowired
	private AmazonWebService amazonWebService;

	@Autowired
	private FileUtil fileUtil;

	@Autowired
	private EmailActivityValidator emailValidator;

	public VanityURLDTO getCompanyDetails(String companyProfileNameOrDomain) {
		VanityURLDTO vanityURLDto = new VanityURLDTO();
		CompanyProfile cp = getCompanyProfileDetails(companyProfileNameOrDomain);
		if (cp != null) {
			vanityURLDto.setCompanyName(cp.getCompanyName());
			vanityURLDto.setCompanyProfileName(utilDao.getPrmCompanyProfileName());
			vanityURLDto.setCompanyLogoImagePath(cp.getCompanyLogoPath());
			vanityURLDto.setCompanyBgImagePath(cp.getBackgroundLogoPath());
			vanityURLDto.setShowVendorCompanyLogo(cp.isShowVendorCompanyLogo());
			vanityURLDto.setCompanyFavIconPath(cp.getFavIconLogoPath());
			vanityURLDto.setLoginScreenDirection(cp.getLoginScreenDirection());
			String companyWebsiteUrl = cp.getWebsite();
			boolean isLinkStartsWithHttpProtocol = companyWebsiteUrl.startsWith("https://")
					|| companyWebsiteUrl.startsWith("http://");
			String updatedCompanyWebsiteUrl = isLinkStartsWithHttpProtocol ? companyWebsiteUrl
					: "https://" + companyWebsiteUrl;
			vanityURLDto.setCompanyUrl(updatedCompanyWebsiteUrl);
			/***** XNFR-233 ******/
			vanityURLDto.setBackgroundLogoStyle2(cp.getBackgroundLogoStyle2());
			if (cp.getLoginType() != null) {
				vanityURLDto.setLoginType(cp.getLoginType().name());
			} else {
				vanityURLDto.setLoginType(LoginStyleType.STYLE_TWO.name());
			}
			/***** XNFR-233 ******/
			/***** STRAT XNFR-416 ****/
			vanityURLDto.setBackgroundColorStyle1(cp.getBackgroundColorStyle1());
			vanityURLDto.setBackgroundColorStyle2(cp.getBackgroundColorStyle2());
			vanityURLDto.setStyleOneBgColor(cp.isStyleOneBgColor());
			vanityURLDto.setStyleTwoBgColor(cp.isStyleTwoBgColor());
			/***** END XNFR-416 *****/
			/***** XBI-2016 ****/
			vanityURLDto.setLoginFormDirectionStyleOne(cp.getLoginFormDirectionStyleOne());
			/*** XBI-2016 ***/
			vanityURLDto.setCompanyId(cp.getId());
			vanityURLDto.setEnableVanityURL(true);
			// XNFR-603
			vanityURLDto.setSupportEmailId(cp.getSupportEmailId());
			vanityURLDto.setVanityURLink(xamplifyUtil.frameVanityURL(webUrl, utilDao.getPrmCompanyProfileName()));
		}
		return vanityURLDto;
	}

	private CompanyProfile getCompanyProfileDetails(String companyProfileName) {
		CompanyProfile cp = null;
		cp = vanityURLDao.getCompanyProfileByCompanyProfileName(utilDao.getPrmCompanyProfileName());
		return cp;
	}

	public XtremandResponse checkUserBelongsToCompany(String emailId, String companyProfileName) {
		XtremandResponse xRes = new XtremandResponse();
		boolean isSuperAdminLoggedIn = superAdminEmail.equalsIgnoreCase(emailId);
		if ("undefined".equals(emailId) || isSuperAdminLoggedIn) {
			xRes.setStatusCode(200);
			xRes.setMessage("success");
		} else {
			checkCompanyUsers(emailId, companyProfileName, xRes);
		}
		userService.updateUserDefaultPage(emailId); // XNFR-560
		return xRes;
	}

	private void checkCompanyUsers(String emailId, String companyProfileName, XtremandResponse xRes) {
		CompanyProfile vendorCompanyProfile = vanityURLDao.getCompanyProfileByCompanyProfileName(companyProfileName);
		User loggedInUser = userService.loadUser(Arrays.asList(new Criteria("emailId", OPERATION_NAME.eq, emailId)),
				new FindLevel[] { FindLevel.COMPANY_PROFILE });
		xRes.setMessage("fail");
		if ((vendorCompanyProfile != null && loggedInUser != null)
				&& (vanityURLDao.isUserBelongsToCompany(emailId, vendorCompanyProfile.getId())
						|| checkUserIsPartner(vendorCompanyProfile, loggedInUser)
						|| checkUserIsPartnerTeamMember(loggedInUser.getUserId(), vendorCompanyProfile.getId()))) {
			xRes.setStatusCode(200);
			xRes.setMessage("success");
		}
	}

//	private boolean checkUserIsActive(CompanyProfile vendorCompanyProfile, User loggedInUser) {
//		boolean status = false;
//		if (loggedInUser.getCompanyProfile() != null) {
//			Partnership partnership = partnerShipDao.getPartnershipByPartnerCompany(loggedInUser.getCompanyProfile(),
//					vendorCompanyProfile);
//			if (partnership != null && !partnership.getStatus().equals(PartnershipStatus.DEACTIVATED)) {
//				status = true;
//			}
//		} else {
//			PartnershipDTO partnershipDto = partnerShipDao.getPartnerShipByParnterIdAndVendorCompanyId(
//					loggedInUser.getUserId(), vendorCompanyProfile.getId());
//			if (partnershipDto != null && partnershipDto.getId() != null && !partnershipDto.getStatus().equals("deactivated")) {
//				status = true;
//			}
//		}
//		return status;
//	}

	private boolean checkUserIsPartner(CompanyProfile vendorCompanyProfile, User loggedInUser) {
		boolean status = false;
		if (loggedInUser.getCompanyProfile() != null) {
			Partnership partnership = partnerShipDao.getPartnershipByPartnerCompany(loggedInUser.getCompanyProfile(),
					vendorCompanyProfile);
			if (partnership != null && !partnership.getStatus().equals(PartnershipStatus.DEACTIVATED)) {
				status = true;
			}
		} else {
			PartnershipDTO partnershipDto = partnerShipDao.getPartnerShipByParnterIdAndVendorCompanyId(
					loggedInUser.getUserId(), vendorCompanyProfile.getId());
			if (partnershipDto != null && partnershipDto.getId() != null
					&& !partnershipDto.getStatus().equals("deactivated")) {
				status = true;
			}
		}
		return status;
	}

	private boolean checkUserIsPartnerTeamMember(Integer teamMemberId, Integer vendorCompanyId) {
		TeamMember tm = teamDao.getByTeamMemberId(teamMemberId);
		if (tm != null && tm.getOrgAdmin().getUserId() != null) {
			PartnershipDTO partnershipDto = partnerShipDao
					.getPartnerShipByParnterIdAndVendorCompanyId(tm.getOrgAdmin().getUserId(), vendorCompanyId);
			if (partnershipDto != null && partnershipDto.getId() != null && partnershipDto.getPartnerCompanyId() != null
					&& !partnershipDto.getStatus().equals("deactivated")) {
				return true;
			}
		}
		return false;
	}

	public XtremandResponse getVanityCompanyProfileName(String companyName) {
		XtremandResponse xRes = new XtremandResponse();
		CompanyProfile cp = userDao.getCompanyProfileByCompanyName(companyName);
		xRes.setStatusCode(100);
		if (cp != null) {
			ModuleAccess moduleAccess = userDao.getAccessByCompanyId(cp.getId());
			if (moduleAccess != null) {
				xRes.setData(xamplifyUtil.frameVanityURL(webUrl, utilDao.getPrmCompanyProfileName()));
				xRes.setStatusCode(200);
			}
		}
		return xRes;
	}

	public XtremandResponse getVanityURLRolesForUser(String emailId, VanityUrlDetailsDTO dashboardAnalyticsDto) {
		User user = userService.loadUser(Arrays.asList(new Criteria("emailId", OPERATION_NAME.eq, emailId)),
				new FindLevel[] { FindLevel.ROLES, FindLevel.COMPANY_PROFILE });
		XtremandResponse xRes = new XtremandResponse();

		boolean isWelcomePageEnabled = false;
		if (dashboardAnalyticsDto != null && dashboardAnalyticsDto.isVanityUrlFilter()) {
			dashboardAnalyticsDto.setUserId(user.getUserId());
			utilService.isVanityUrlFilterApplicable(dashboardAnalyticsDto);
			if (dashboardAnalyticsDto.isPartnerLoggedInThroughVanityUrl()) {
				Set<Role> roles = user.getRoles().stream()
						.filter(role -> role.equals(Role.USER_ROLE) || role.equals(Role.COMPANY_PARTNER)
								|| role.equals(Role.ALL_ROLES) || role.equals(Role.DAM) || role.equals(Role.MDF)
								|| role.equals(Role.OPPORTUNITY) || role.equals(Role.SHARE_LEADS)
								|| role.equals(Role.LEARNING_TRACK) || role.equals(Role.PLAY_BOOK))
						.collect(Collectors.toCollection(LinkedHashSet::new));

				xRes.setData(roles);
			} else {
				Set<Role> roles = user.getRoles().stream().filter(role -> !role.equals(Role.COMPANY_PARTNER))
						.collect(Collectors.toCollection(LinkedHashSet::new));
				xRes.setData(roles);
			}
		}

		Map<String, Object> map = new HashMap<>();
		map.put("isWelcomePageEnabled", isWelcomePageEnabled);
		xRes.setMap(map);
		return xRes;
	}

	public XtremandResponse saveDashboardButton(com.xtremand.dashboard.buttons.dto.DashboardButtonsDTO dbButtonsDto) {
		XtremandResponse xRes = new XtremandResponse();
		try {
			String companyProfileName = dbButtonsDto.getCompanyProfileName();
			if (org.springframework.util.StringUtils.hasText(companyProfileName)) {
				String buttonTitle = XamplifyUtils.convertToLowerCase(dbButtonsDto.getButtonTitle());
				CompanyProfile cp = vanityURLDao
						.getCompanyProfileByCompanyProfileName(dbButtonsDto.getCompanyProfileName());
				Integer companyId = cp.getId();
				List<String> existingTitles = vanityURLDao.findExistingDashboardButtonsByCompanyId(companyId);
				boolean isDuplicateTitle = existingTitles.indexOf(buttonTitle) > -1;
				if (isDuplicateTitle) {
					xRes.setStatusCode(100);
					xRes.setMessage("Already Exists");
				} else {
					User user = userService.loadUser(
							Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, dbButtonsDto.getVendorId())),
							new FindLevel[] { FindLevel.ROLES });
					DashboardButton dashboardButton = new DashboardButton();
					frameDashboardRecord(dbButtonsDto, dashboardButton);
					dashboardButton.setUser(user);
					Integer maxOrderId = vanityURLDao.getMaxOrderIdByCompanyId(companyId);
					if (maxOrderId != null) {
						dashboardButton.setOrder(maxOrderId + 1);
					} else {
						dashboardButton.setOrder(1);
					}
					dashboardButton.setCompanyProfile(cp);
					Set<Integer> partnerIds = dbButtonsDto.getPartnerIds();
					Set<Integer> partnerGroupIds = dbButtonsDto.getPartnerGroupIds();
					boolean isDashboardButtonPublished = XamplifyUtils
							.isPartnerGroupOrPartnerCompanySelected(partnerGroupIds, partnerIds);
					dashboardButton.setPublishingInProgress(isDashboardButtonPublished);
					vanityURLDao.saveDashboardButton(dashboardButton);
					dbButtonsDto.setId(dashboardButton.getId());
					xRes.setStatusCode(200);
				}
			}
		} catch (DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}

		return xRes;
	}

	public XtremandResponse updateDashboardButton(DashboardButtonsDTO dashboardButtonDto) {
		XtremandResponse xRes = new XtremandResponse();
		try {
			Integer id = dashboardButtonDto.getId();
			boolean isValidId = XamplifyUtils.isValidInteger(id);
			if (isValidId) {
				DashboardButton dashboardButton = vanityURLDao.getDashboardButtonById(id);
				if (dashboardButton != null) {
					String editedButtonTitle = XamplifyUtils.convertToLowerCase(dashboardButtonDto.getButtonTitle());
					String existingTitle = XamplifyUtils.convertToLowerCase(dashboardButton.getButtonTitle());
					Integer vendorCompanyId = dashboardButton.getCompanyProfile().getId();
					List<String> existingTitles = vanityURLDao.findExistingDashboardButtonsByCompanyId(vendorCompanyId);
					boolean isDuplicateTitle = existingTitles.indexOf(editedButtonTitle) > -1
							&& !existingTitle.equals(editedButtonTitle);
					if (isDuplicateTitle) {
						xRes.setStatusCode(100);
						xRes.setMessage("Already Exists");
					} else {
						frameDashboardRecord(dashboardButtonDto, dashboardButton);
						/***** XNFR-571 *******/
						setPublishedData(dashboardButtonDto, id, dashboardButton);
						/****** XNFR-571 *****/
						vanityURLDao.updateDashboardButton(dashboardButton);
						xRes.setStatusCode(200);
					}
				}
			}
		} catch (DataIntegrityViolationException e) {
			xRes.setStatusCode(100);
			String errorMessage = "Duplicate title name : " + e.getMessage();
			logger.error(errorMessage);
		} catch (Exception e) {
			String errorMessage = "Error while updating Dashboard button : " + e.getMessage();
			logger.error(errorMessage);
		}
		return xRes;
	}

	private void setPublishedData(DashboardButtonsDTO dashboardButtonDto, Integer id, DashboardButton dashboardButton) {
		isPartnerIdsMatched(dashboardButtonDto, id);

		isPartnerGroupIdsMatched(dashboardButtonDto, id);

		boolean isPartnerCompanyGroupIdsAndPartnerIdsMatched = dashboardButtonDto.isPartnerGroupIdsMatched()
				&& dashboardButtonDto.isPartnerIdsMatched();

		dashboardButton.setPublishingInProgress(!isPartnerCompanyGroupIdsAndPartnerIdsMatched);
		dashboardButtonDto.setPublishingFromEditSection(true);

		/***** XBI-2831 ***/
		List<Integer> alreadyPublishedPartnerUserIds = userListDao.findPublishedPartnerIdsById(id);
		dashboardButtonDto.setPublishedPartnerUserIds(alreadyPublishedPartnerUserIds);
	}

	private void isPartnerGroupIdsMatched(DashboardButtonsDTO dashboardButtonDto, Integer id) {
		List<Integer> publishedPartnerGroupIds = vanityURLDao.findPublishedPartnerGroupIdsByDashboardButtonId(id);
		boolean isPublishedAllUserswithInList = userListDao.isPublishedAllUserswithInList(id, publishedPartnerGroupIds);
		if (isPublishedAllUserswithInList) {
			Collections.sort(publishedPartnerGroupIds);
			dashboardButtonDto
					.setPublishedPartnerGroupIds(XamplifyUtils.convertListToSetElements(publishedPartnerGroupIds));
			List<Integer> selectedPartnerGroupIds = XamplifyUtils
					.convertSetToList(dashboardButtonDto.getPartnerGroupIds());
			Collections.sort(selectedPartnerGroupIds);

			boolean isUniquePartnerGroupIds = XamplifyUtils.isUniqueArrayLists(publishedPartnerGroupIds,
					selectedPartnerGroupIds);
			boolean isEmptyPublishedPartnerGroupIds = !XamplifyUtils.isNotEmptyList(publishedPartnerGroupIds);
			boolean isEmptySelectedPartnerGroupIds = !XamplifyUtils.isNotEmptyList(selectedPartnerGroupIds);
			boolean isNoGroupSelected = isEmptyPublishedPartnerGroupIds && isEmptySelectedPartnerGroupIds;
			boolean isPartnerGroupIdsMatched = isUniquePartnerGroupIds || isNoGroupSelected;
			dashboardButtonDto.setPartnerGroupIdsMatched(isPartnerGroupIdsMatched);
		}
	}

	private void isPartnerIdsMatched(DashboardButtonsDTO dashboardButtonDto, Integer id) {
		List<Integer> publishedPartnerIds = vanityURLDao.findPublishedPartnerIdsByDashboardButtonId(id);
		Collections.sort(publishedPartnerIds);
		dashboardButtonDto.setPublishedPartnerIds(XamplifyUtils.convertListToSetElements(publishedPartnerIds));
		List<Integer> selectedPartnerIds = XamplifyUtils.convertSetToList(dashboardButtonDto.getPartnerIds());
		Collections.sort(selectedPartnerIds);
		boolean isUniquePartnerIds = XamplifyUtils.isUniqueArrayLists(publishedPartnerIds, selectedPartnerIds);
		boolean isEmptyPublishedPartnerIds = !XamplifyUtils.isNotEmptyList(publishedPartnerIds);
		boolean isEmptySelectedPartnerIds = !XamplifyUtils.isNotEmptyList(selectedPartnerIds);
		boolean isNoPartnerIdSelected = isEmptyPublishedPartnerIds && isEmptySelectedPartnerIds;
		boolean isPartnerIdMatched = isUniquePartnerIds || isNoPartnerIdSelected;
		dashboardButtonDto.setPartnerIdsMatched(isPartnerIdMatched);
	}

	private void frameDashboardRecord(DashboardButtonsDTO dbButtonsDto, DashboardButton dashboardButton) {
		dashboardButton.setButtonTitle(dbButtonsDto.getButtonTitle());
		dashboardButton.setButtonSubTitle(dbButtonsDto.getButtonSubTitle());
		dashboardButton.setButtonDescription(dbButtonsDto.getButtonDescription());
		dashboardButton.setButtonLink(dbButtonsDto.getButtonLink());
		if (!XamplifyUtils.isValidInteger(dashboardButton.getId())) {
			dashboardButton.setTimestamp(new Date());
		}
		dashboardButton.setUpdatedTime(new Date());
		dashboardButton.setButtonIcon(dbButtonsDto.getButtonIcon());
		dashboardButton.setOpenInNewTab(dbButtonsDto.isOpenInNewTab());
		dashboardButton.setAlternateUrl(dbButtonsDto.getAlternateUrl());
	}

	@SuppressWarnings("unchecked")
	public XtremandResponse getDashboardButtonsForPagination(Pagination pagination, String searchKey) {
		XtremandResponse xRes = new XtremandResponse();
		try {
			String companyProfileName = utilDao.getPrmCompanyProfileName();
			if (pagination != null && !StringUtils.isBlank(companyProfileName)) {
				CompanyProfile cp = vanityURLDao.getCompanyProfileByCompanyProfileName(companyProfileName);
				if (cp != null) {
					pagination.setVendorCompanyId(cp.getId());
					Map<String, Object> dashboardButtonMap = vanityURLDao.getVendorDashboardButtons(pagination,
							searchKey);
					if (dashboardButtonMap.containsKey(DB_BUTTONS)) {
						List<DashboardButton> dbButtonsList = (List<DashboardButton>) dashboardButtonMap
								.get(DB_BUTTONS);
						List<DashboardButtonsDTO> dbButtonsDtoList = getDashboardButtonsList(dbButtonsList, false,
								false);
						dashboardButtonMap.put(DB_BUTTONS, dbButtonsDtoList);
					}
					xRes.setData(dashboardButtonMap);
					xRes.setStatusCode(200);
				}
			}
		} catch (Exception e) {
			String errorMessage = "Error while getting Dashboard buttons for pagination: " + e.getMessage();
			logger.error(errorMessage);
		}
		return xRes;
	}

	public XtremandResponse getDashboardButtonsForCarousel(String companyProfileName, Integer userId) {
		XtremandResponse xRes = new XtremandResponse();
		try {
			if (!StringUtils.isBlank(companyProfileName)) {
				CompanyProfile cp = vanityURLDao.getCompanyProfileByCompanyProfileName(companyProfileName);
				Integer loggedInUserCompanyId = userDao.getCompanyIdByUserId(userId);
				if (cp != null) {
					if (loggedInUserCompanyId.equals(cp.getId())) {
						List<DashboardButton> dbButtonsList = vanityURLDao.getVendorDashboardButtons(cp);
						List<DashboardButtonsDTO> dbButtonsDtoList = getDashboardButtonsList(dbButtonsList, true, true);
						Map<String, Object> dashboardButtonMap = new HashMap<>();
						dashboardButtonMap.put(DB_BUTTONS, dbButtonsDtoList);
						dashboardButtonMap.put(XamplifyConstants.TOTAL_RECORDS, dbButtonsList.size());
						xRes.setData(dashboardButtonMap);
						xRes.setStatusCode(200);
					} else {
						Map<String, Object> dashboardButtonMap = new HashMap<>();
						List<DashboardButtonsDTO> dashboardButtonsDtos = vanityURLDao
								.findDashboardButtonsForPartnerView(cp.getId(), userId);
						dashboardButtonMap.put(DB_BUTTONS, dashboardButtonsDtos);
						dashboardButtonMap.put(XamplifyConstants.TOTAL_RECORDS, dashboardButtonsDtos.size());
						xRes.setData(dashboardButtonMap);
						xRes.setStatusCode(200);
					}
				}
			}
		} catch (Exception e) {
			String errorMessage = "Error while getting Dashboard buttons for carousel: " + e.getMessage();
			logger.error(errorMessage);
		}
		return xRes;
	}

	private List<DashboardButtonsDTO> getDashboardButtonsList(List<DashboardButton> dbButtonsList,
			boolean isCarouselView, boolean replaceToAlternateUrl) {
		List<DashboardButtonsDTO> dbButtonsDtoList = new ArrayList<>();
		for (DashboardButton dashboardButton : dbButtonsList) {
			DashboardButtonsDTO dashboardButtonDto = new DashboardButtonsDTO();
			Integer id = dashboardButton.getId();
			dashboardButtonDto.setId(id);
			dashboardButtonDto.setButtonTitle(dashboardButton.getButtonTitle());
			dashboardButtonDto.setButtonSubTitle(dashboardButton.getButtonSubTitle());
			dashboardButtonDto.setButtonDescription(dashboardButton.getButtonDescription());
			dashboardButtonDto.setButtonIcon(dashboardButton.getButtonIcon());
			dashboardButtonDto.setOpenInNewTab(dashboardButton.isOpenInNewTab());
			if (dashboardButton.isOpenInNewTab()) {
				dashboardButtonDto.setOpenInNewTabTarget("_blank");
			}
			String buttonUrl = replaceToAlternateUrl && StringUtils.isNotEmpty(dashboardButton.getAlternateUrl())
					? dashboardButton.getAlternateUrl()
					: dashboardButton.getButtonLink();
			dashboardButtonDto.setButtonLink(buttonUrl);
			dashboardButtonDto.setAlternateUrl(dashboardButton.getAlternateUrl());
			List<DashboardAlternateUrlDTO> alternateUrls = dashboardButtonDao
					.findAlternateUrls(dashboardButton.getButtonLink());

			if (XamplifyUtils.isNotEmptyList(alternateUrls)) {
				dashboardButtonDto.setAlternateUrls(alternateUrls);
			}

			/***** XNFR-571 ****/
			setPublishedProperites(isCarouselView, dashboardButton, dashboardButtonDto, id);
			/***** XNFR-571 ****/

			dbButtonsDtoList.add(dashboardButtonDto);
		}
		return dbButtonsDtoList;
	}

	/***** XNFR-571 ****/
	private void setPublishedProperites(boolean isCarouselView, DashboardButton dashboardButton,
			DashboardButtonsDTO dashboardButtonDto, Integer id) {
		if (!isCarouselView) {
			dashboardButtonDto.setPublishingInProgress(dashboardButton.isPublishingInProgress());
			boolean isDashboardButtonPublished = vanityURLDao.isDashboardButtonPublished(id);
			dashboardButtonDto.setPublished(isDashboardButtonPublished);
			List<Integer> publishedPartnerGroupIds = vanityURLDao.findPublishedPartnerGroupIdsByDashboardButtonId(id);
			boolean isPublishedToPartnerGroups = XamplifyUtils.isNotEmptyList(publishedPartnerGroupIds);
			if (isPublishedToPartnerGroups) {
				dashboardButtonDto.setPartnerGroupSelected(isPublishedToPartnerGroups);
				dashboardButtonDto.setPartnerGroupIds(XamplifyUtils.convertListToSetElements(publishedPartnerGroupIds));
			}
			dashboardButtonDto.setPartnerIds(XamplifyUtils
					.convertListToSetElements(vanityURLDao.findPublishedPartnerIdsByDashboardButtonId(id)));
			dashboardButtonDto.setPartnershipIds(XamplifyUtils
					.convertListToSetElements(vanityURLDao.findPublishedPartnershipIdsByDashboardButtonId(id)));
		}
	}

	public XtremandResponse deleteDashboardButton(Integer id) {
		XtremandResponse xRes = new XtremandResponse();
		try {
			vanityURLDao.deleteDashboardButtonById(id);
			xRes.setStatusCode(200);
		} catch (Exception e) {
			String debugMessage = "Error while deleting Dashboard button : " + e.getMessage();
			logger.error(debugMessage);
		}
		return xRes;
	}

//	***************** Vanity Email Templates Implementation ***************************
	@SuppressWarnings("unchecked")
	public XtremandResponse getEmailTemplatesForPagination(Pagination pagination) {
		XtremandResponse xRes = new XtremandResponse();
		VanityUrlDetailsDTO vanityUrlDetailsDTO = new VanityUrlDetailsDTO();
		try {
			vanityUrlDetailsDTO.setUserId(pagination.getUserId());
			String companyProfileName = utilDao.getPrmCompanyProfileName();
			vanityUrlDetailsDTO.setVendorCompanyProfileName(companyProfileName);
			vanityUrlDetailsDTO.setVanityUrlFilter(true);
			utilService.isVanityUrlFilterApplicable(vanityUrlDetailsDTO);
			if (!StringUtils.isBlank(companyProfileName)) {
				CompanyProfile cp = vanityURLDao.getCompanyProfileByCompanyProfileName(companyProfileName);
				Integer companyId = cp.getId();
				Map<String, Object> emailTemplatesMap = new HashMap<>();
				if (pagination.getFilterKey().equals("DEFAULT")) {
					Map<String, Object> resultMap = findDefaultVanityEmailTemplates(pagination, companyId,
							vanityUrlDetailsDTO);
					List<DefaultEmailTemplateDTO> defaultVanityEmailTemplatesList = (List<DefaultEmailTemplateDTO>) resultMap
							.get(DEFAULT_TEMPLATES);
					defaultVanityEmailTemplatesList.forEach(item -> {
						if (XamplifyUtils.isValidString(item.getImagePath())) {
							String updatedImagePath = xamplifyUtil
									.replaceS3WithCloudfrontViceVersa(item.getImagePath());
							item.setCdnImagePath(updatedImagePath);
						}
					});
					emailTemplatesMap.put("vanityEmailTemplates", defaultVanityEmailTemplatesList);
					emailTemplatesMap.put(TOTAL_RECORDS, resultMap.get(TOTAL_RECORDS));
				} else if (pagination.getFilterKey().equals("CUSTOM")) {
					Map<String, Object> resultMap = findAllCustomVanityEmailTemplates(pagination, cp, companyId,
							vanityUrlDetailsDTO);
					List<DefaultEmailTemplateDTO> vanityEmailTemplatesList = (List<DefaultEmailTemplateDTO>) resultMap
							.get(CUSTOM_TEMPLATES);
					vanityEmailTemplatesList.forEach(item -> {
						if (XamplifyUtils.isValidString(item.getImagePath())) {
							String updatedImagePath = xamplifyUtil
									.replaceS3WithCloudfrontViceVersa(item.getImagePath());
							item.setCdnImagePath(updatedImagePath);
						}
					});
					emailTemplatesMap.put("vanityEmailTemplates", vanityEmailTemplatesList);
					emailTemplatesMap.put(TOTAL_RECORDS, resultMap.get(TOTAL_RECORDS));

				}

				xRes.setData(emailTemplatesMap);
				xRes.setStatusCode(200);
			}
		} catch (Exception e) {
			String errorMessage = "Error while getting vanity email templates " + e.getMessage();
			logger.error(errorMessage);
		}
		return xRes;
	}

	private List<DefaultEmailTemplateDTO> isAccessModuleDefault(
			List<DefaultEmailTemplateDTO> defaultVanityEmailTemplatesList, Integer companyId,
			List<DefaultEmailTemplate> vanityDefaultTemplates) {
		for (DefaultEmailTemplate vanityDefaultEmailTemplate : vanityDefaultTemplates) {
			DefaultEmailTemplateType type = vanityDefaultEmailTemplate.getType();
			boolean hasAccess = false;

			switch (type) {
			case ASSET_PUBLISH:
				hasAccess = utilDao.hasDamAccessByCompanyId(companyId);
				break;
			case PARTNER_SIGNATURE_ENABLED:
				hasAccess = utilDao.hasDamAccessByCompanyId(companyId);
				break;
			case TRACK_PUBLISH:
				hasAccess = utilDao.hasLmsAccessByCompanyId(companyId);
				break;
			case PLAYBOOK_PUBLISH:
				hasAccess = utilDao.hasPlaybookAccessByCompanyId(companyId);
				break;
			case SHARE_LEAD:
				hasAccess = utilDao.hasShareLeadsAccessByCompanyId(companyId);
				break;
			case PARTNER_PDF_SIGNATURE_COMPLETED:
				hasAccess = utilDao.hasDamAccessByCompanyId(companyId);
				break;
			case ADD_LEAD:
			case ADD_DEAL:
			case LEAD_UPDATE:
			case DEAL_UPDATE:
			case ADD_SELF_LEAD:
			case ADD_SELF_DEAL:
			case UPDATE_SELF_LEAD:
			case UPDATE_SELF_DEAL:
			case PRM_ADD_LEAD:
			case PRM_UPDATED:
			case LEAD_APPROVE:
			case LEAD_REJECT:
			case PRM_LEAD_APPROVE:
			case PRM_LEAD_REJECT:
			case PARTNER_ADD_LEAD:
			case PRM_PARTNER_ADD_LEAD:
			case PARTNER_UPDATE_LEAD:
			case PRM_PARTNER_UPDATE_LEAD:
			case PARTNER_ADD_DEAL:
			case PARTNER_UPDATE_DEAL:

				hasAccess = utilDao.hasEnableLeadsAccessByCompanyId(companyId);
				break;
			case UNLOCK_MDF_FUNDING:
				hasAccess = utilDao.hasUnlockMdfFundingAccessByCompanyId(companyId);
				break;
			default:
				addDefaultVanityEmailTemplates(companyId, defaultVanityEmailTemplatesList, vanityDefaultEmailTemplate);
				continue;
			}

			addDefaultTemplateByModuleAccess(companyId, defaultVanityEmailTemplatesList, vanityDefaultEmailTemplate,
					hasAccess);
		}
		return defaultVanityEmailTemplatesList;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> findDefaultVanityEmailTemplates(Pagination pagination, Integer companyId,
			VanityUrlDetailsDTO vanityUrlDetailsDTO) {

		Map<String, Object> resultMap;
		String category = pagination.getCategory();
		boolean hasCategory = category != null && !category.trim().isEmpty();

		if (pagination.isSelectedType() && hasCategory) {
			resultMap = vanityURLDao.getVanityDefaultEmailTemplates(pagination, vanityUrlDetailsDTO);
		} else if (!pagination.isSelectedType() && hasCategory) {
			resultMap = vanityURLDao.getVanityDefaultEmailTemplatesForPartner(pagination, vanityUrlDetailsDTO);
		} else {
			resultMap = new HashMap<>();
		}

		List<DefaultEmailTemplateDTO> defaultVanityEmailTemplatesList = new ArrayList<>();
		List<DefaultEmailTemplate> totalTemplatesRecords = (List<DefaultEmailTemplate>) resultMap.get("totalTemplates");

		List<DefaultEmailTemplateDTO> totalFilteredList = isAccessModuleDefault(new ArrayList<>(), companyId,
				totalTemplatesRecords);
		int totalTempRecords = totalFilteredList.size();

		int startIndex = (pagination.getPageIndex() - 1) * pagination.getMaxResults();
		int endIndex = Math.min(startIndex + pagination.getMaxResults(), totalTempRecords);

		if (startIndex < totalTempRecords) {
			defaultVanityEmailTemplatesList = totalFilteredList.subList(startIndex, endIndex);
		}

		resultMap.remove(DEFAULT_TEMPLATES);
		resultMap.remove(TOTAL_RECORDS);
		resultMap.put(DEFAULT_TEMPLATES, defaultVanityEmailTemplatesList);
		resultMap.put(TOTAL_RECORDS, totalTempRecords);

		return resultMap;
	}

	private List<DefaultEmailTemplateDTO> isAccessModuleCustom(List<DefaultEmailTemplateDTO> vanityEmailTemplatesList,
			Integer companyId, List<CustomDefaultEmailTemplate> vanityEmailTemplates) {

		for (CustomDefaultEmailTemplate vanityEmailTemplate : vanityEmailTemplates) {
			DefaultEmailTemplateType templateType = vanityEmailTemplate.getDefaultEmailTemplate().getType();
			boolean hasAccess = false;

			switch (templateType) {
			case ASSET_PUBLISH:
				hasAccess = utilDao.hasDamAccessByCompanyId(companyId);
				break;
			case PARTNER_SIGNATURE_ENABLED:
				hasAccess = utilDao.hasDamAccessByCompanyId(companyId);
				break;
			case TRACK_PUBLISH:
				hasAccess = utilDao.hasLmsAccessByCompanyId(companyId);
				break;
			case PLAYBOOK_PUBLISH:
				hasAccess = utilDao.hasPlaybookAccessByCompanyId(companyId);
				break;
			case SHARE_LEAD:
				hasAccess = utilDao.hasShareLeadsAccessByCompanyId(companyId);
				break;
			case PARTNER_PDF_SIGNATURE_COMPLETED:
				hasAccess = utilDao.hasDamAccessByCompanyId(companyId);
				break;
			case ADD_LEAD:
			case ADD_DEAL:
			case LEAD_UPDATE:
			case DEAL_UPDATE:
			case ADD_SELF_LEAD:
			case ADD_SELF_DEAL:
			case UPDATE_SELF_LEAD:
			case UPDATE_SELF_DEAL:
			case PRM_ADD_LEAD:
			case PRM_UPDATED:
			case LEAD_APPROVE:
			case LEAD_REJECT:
			case PRM_LEAD_APPROVE:
			case PRM_LEAD_REJECT:
			case PARTNER_ADD_LEAD:
			case PRM_PARTNER_ADD_LEAD:
			case PARTNER_UPDATE_LEAD:
			case PRM_PARTNER_UPDATE_LEAD:
			case PARTNER_ADD_DEAL:
			case PARTNER_UPDATE_DEAL:
				hasAccess = utilDao.hasEnableLeadsAccessByCompanyId(companyId);
				break;
			case UNLOCK_MDF_FUNDING:
				hasAccess = utilDao.hasUnlockMdfFundingAccessByCompanyId(companyId);
				break;
			default:
				addCustomDefaultEmailTempaltes(companyId, vanityEmailTemplatesList, vanityEmailTemplate);
				continue;
			}

			addCustomTemplateByModuleAccess(companyId, vanityEmailTemplatesList, vanityEmailTemplate, hasAccess);
		}
		return vanityEmailTemplatesList;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> findAllCustomVanityEmailTemplates(Pagination pagination, CompanyProfile cp,
			Integer companyId, VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		Map<String, Object> resultMap;
		if (pagination.isSelectedType()) {
			resultMap = vanityURLDao.getVanityEmailTemplates(cp, pagination, vanityUrlDetailsDTO);
		} else {
			resultMap = vanityURLDao.getVanityEmailTemplatesForPartner(cp, pagination, vanityUrlDetailsDTO);
		}
		List<CustomDefaultEmailTemplate> vanityEmailTemplates = (List<CustomDefaultEmailTemplate>) resultMap
				.get(CUSTOM_TEMPLATES);
		List<DefaultEmailTemplateDTO> vanityEmailTemplatesList = new ArrayList<>();
		vanityEmailTemplatesList = isAccessModuleCustom(vanityEmailTemplatesList, companyId, vanityEmailTemplates);
		Integer totalRecords = vanityEmailTemplatesList.size();
		resultMap.remove(CUSTOM_TEMPLATES);
		if (pagination.getPageIndex() > 1) {
			int startIndex = (pagination.getPageIndex() - 1) * pagination.getMaxResults();
			int endIndex = Math.min(startIndex + pagination.getMaxResults(), totalRecords);

			vanityEmailTemplatesList = vanityEmailTemplatesList.subList(startIndex, endIndex);
		} else if (vanityEmailTemplates.size() != vanityEmailTemplatesList.size()
				&& pagination.getMaxResults() < totalRecords
				&& vanityEmailTemplatesList.size() > pagination.getMaxResults()) {
			vanityEmailTemplatesList = vanityEmailTemplatesList.subList(0, pagination.getMaxResults());
		}

		resultMap.remove(TOTAL_RECORDS);
		resultMap.put(TOTAL_RECORDS, totalRecords);

		resultMap.put(CUSTOM_TEMPLATES, vanityEmailTemplatesList);
		return resultMap;

	}

	private void addCustomTemplateByModuleAccess(Integer companyId,
			List<DefaultEmailTemplateDTO> vanityEmailTemplatesList, CustomDefaultEmailTemplate vanityEmailTemplate,
			boolean moduleAccess) {
		if (moduleAccess) {
			addCustomDefaultEmailTempaltes(companyId, vanityEmailTemplatesList, vanityEmailTemplate);
		}

	}

	private void addCustomDefaultEmailTempaltes(Integer companyId,
			List<DefaultEmailTemplateDTO> vanityEmailTemplatesList, CustomDefaultEmailTemplate vanityEmailTemplate) {
		DefaultEmailTemplateDTO vanityEmailTemplateDto = new DefaultEmailTemplateDTO();
		BeanUtils.copyProperties(vanityEmailTemplate, vanityEmailTemplateDto);
		vanityEmailTemplateDto.setDefaultEmailTemplateId(vanityEmailTemplate.getDefaultEmailTemplate().getId());
		vanityEmailTemplateDto.setCompanyId(companyId);
		vanityEmailTemplateDto.setName(vanityEmailTemplate.getDefaultEmailTemplate().getName());
		vanityEmailTemplateDto.setSubject(vanityEmailTemplate.getSubject());
		vanityEmailTemplateDto.setTypeInString(vanityEmailTemplate.getDefaultEmailTemplate().getType().name());
		vanityEmailTemplatesList.add(vanityEmailTemplateDto);
	}

	private void addDefaultTemplateByModuleAccess(Integer companyId,
			List<DefaultEmailTemplateDTO> defaultVanityEmailTemplatesList,
			DefaultEmailTemplate vanityDefaultEmailTemplate, boolean moduleAccess) {
		if (moduleAccess) {
			addDefaultVanityEmailTemplates(companyId, defaultVanityEmailTemplatesList, vanityDefaultEmailTemplate);
		}
	}

	private void addDefaultVanityEmailTemplates(Integer companyId,
			List<DefaultEmailTemplateDTO> defaultVanityEmailTemplatesList,
			DefaultEmailTemplate vanityDefaultEmailTemplate) {

		if (companyId == null || defaultVanityEmailTemplatesList == null || vanityDefaultEmailTemplate == null) {
			throw new IllegalArgumentException("Parameters cannot be null");
		}

		DefaultEmailTemplateDTO vanityEmailTemplateDto = new DefaultEmailTemplateDTO();

		try {
			BeanUtils.copyProperties(vanityDefaultEmailTemplate, vanityEmailTemplateDto);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		vanityEmailTemplateDto.setDefaultEmailTemplateId(vanityDefaultEmailTemplate.getId());
		vanityEmailTemplateDto.setCompanyId(companyId);

		String typeString = (vanityDefaultEmailTemplate.getType() != null) ? vanityDefaultEmailTemplate.getType().name()
				: "";
		vanityEmailTemplateDto.setTypeInString(typeString);

		defaultVanityEmailTemplatesList.add(vanityEmailTemplateDto);
	}

	public XtremandResponse saveOrUpdateEmailTemplate(DefaultEmailTemplateDTO vanityEmailTemplateDto) {
		XtremandResponse xRes = new XtremandResponse();
		try {

			User user = userService.loadUser(
					Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, vanityEmailTemplateDto.getUserId())),
					new FindLevel[] { FindLevel.ROLES, FindLevel.COMPANY_PROFILE });
			CompanyProfile cp = user.getCompanyProfile();

			if (cp != null) {
				CustomDefaultEmailTemplate vanityEmailTemplate = vanityURLDao
						.getVanityETByDefVanityETIdAndCompanyId(vanityEmailTemplateDto.getDefaultEmailTemplateId(), cp);
				if (vanityEmailTemplate == null) {
					vanityEmailTemplate = new CustomDefaultEmailTemplate();
					vanityEmailTemplate.setCreatedTimestamp(new Date());
					vanityEmailTemplate.setSubject(vanityEmailTemplateDto.getSubject());
					vanityEmailTemplate.setJsonBody(vanityEmailTemplateDto.getJsonBody());
					vanityEmailTemplate.setHtmlBody(vanityEmailTemplateDto.getHtmlBody());
					vanityEmailTemplate.setSpamScore(vanityEmailTemplateDto.getSpamScore());
					vanityEmailTemplate.setCreatedUser(user);
					vanityEmailTemplate.setImagePath(vanityEmailTemplateDto.getImagePath());
					vanityEmailTemplate.setCompanyProfile(cp);
					vanityEmailTemplate.setDefaultEmailTemplate(vanityURLDao
							.getVanityDefaultEmailTemplateById(vanityEmailTemplateDto.getDefaultEmailTemplateId()));
				} else {
					vanityEmailTemplate.setSubject(vanityEmailTemplateDto.getSubject());
					vanityEmailTemplate.setUpdatedTimestamp(new Date());
					vanityEmailTemplate.setJsonBody(vanityEmailTemplateDto.getJsonBody());
					vanityEmailTemplate.setHtmlBody(vanityEmailTemplateDto.getHtmlBody());
					vanityEmailTemplate.setSpamScore(vanityEmailTemplateDto.getSpamScore());
					vanityEmailTemplate.setUpdatedUser(user);
				}
				genericDao.saveOrUpdate(vanityEmailTemplate);
				xRes.setStatusCode(200);
				Map<String, Object> map = new HashMap<>();
				String thumbnailHtmlBody = vanityEmailTemplate.getHtmlBody();
				String logoPath = userDao.getCompanyLogoPath(cp.getId());
				logoPath = serverPath + XamplifyUtils.escapeDollarSequece(logoPath);
				thumbnailHtmlBody = thumbnailHtmlBody.replaceAll(VANITY_COMPANY_LOGO, logoPath)
						.replaceAll(replaceCompanyLogo, logoPath).replaceAll(VANITY_COMPANY_LOGO_HREF, logoPath);
				map.put("customEmailTemplateId", vanityEmailTemplate.getId());
				map.put("customEmailTemplateHtmlBody", thumbnailHtmlBody);
				map.put("companyId", cp.getId());
				xRes.setMap(map);

			} else {
				xRes.setStatusCode(404);
				xRes.setMessage("Unable to save the template at this time.Please try after sometime");
			}

		} catch (Exception e) {
			String errorMessage = "Error while save/update Vanity Email Template : " + e.getMessage();
			logger.error(errorMessage);
			xRes.setStatusCode(100);
		}
		return xRes;
	}

	public XtremandResponse deleteEmailTemplate(Integer defaultEmailTemplateId) {
		XtremandResponse xRes = new XtremandResponse();
		try {
			vanityURLDao.deleteVanityEmailTemplateById(defaultEmailTemplateId);
			xRes.setStatusCode(200);
		} catch (Exception e) {
			String errorMessage = "Error while deleting Vanity Email Template : " + e.getMessage();
			logger.error(errorMessage);
		}
		return xRes;
	}

	public XtremandResponse findCompanyProfileName(Integer companyId) {
		XtremandResponse xRes = new XtremandResponse();
		String companyProfileName = userDao.getCompanyProfileNameById(companyId);
		xRes.setStatusCode(100);
		if (org.springframework.util.StringUtils.hasText(companyProfileName)) {
			ModuleAccess moduleAccess = userDao.getAccessByCompanyId(companyId);
			if (moduleAccess != null) {
				xRes.setData(xamplifyUtil.frameVanityURL(webUrl, companyProfileName));
				xRes.setStatusCode(200);
			}
		}
		return xRes;
	}

	/************ XNFR- 233 ***************/

	/** XNFR-618 **/
	public XtremandResponse getVanityUrlDetailsbyCompanyProfileName(String companyProfileName) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = "FAILED";
		Integer responseStatusCode = 400;
		if (StringUtils.isNotBlank(companyProfileName)) {
			VanityURLDTO vanityUrlDto = getCompanyDetails(companyProfileName);
			if (vanityUrlDto != null) {
				response.setData(vanityUrlDto);
				responseStatusCode = 200;
				responseMessage = "Success";
			}
		}
		response.setStatusCode(responseStatusCode);
		response.setMessage(responseMessage);
		return response;
	}

	public XtremandResponse getCompanyProfileNameByCustomDomain(String domain) {
		XtremandResponse response = new XtremandResponse();
		String companyProfileName = utilDao.getPrmCompanyProfileName();
		if (org.springframework.util.StringUtils.hasText(companyProfileName)) {
			response.setData(companyProfileName);
			XamplifyUtils.addSuccessStatus(response);
		} else {
			XamplifyUtils.addErorMessageWithStatusCode(response, "Invalid Custom Domain", 404);
		}

		return response;
	}

	public XtremandResponse getEmailTemplatesForDuplicates(Integer userId) {
		try {
			Integer loggedInUserCompanyId = userDao.getCompanyIdByUserId(userId);
			XtremandResponse response = new XtremandResponse();
			response.setStatusCode(200);
			response.setData(vanityURLDao.listAllTemplateDuplicates(loggedInUserCompanyId));
			return response;
		} catch (HibernateException | UserDataAccessException e) {
			logger.error("Error In listAllCompanyProfileNames()", e);
			throw new UserDataAccessException(e.getMessage());
		} catch (Exception ex) {
			logger.error("Error In listAllCompanyProfileNames()", ex);
			throw new UserDataAccessException(ex.getMessage());

		}
	}

	public XtremandResponse getHtmlBody(Integer id, Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		try {
			String companyProfieName = userDao.getCompanyProfileNameByUserId(loggedInUserId);
			boolean hasVanityAccess = utilDao.hasVanityAccessByUserId(loggedInUserId);

			if (XamplifyUtils.isValidString(companyProfieName)) {
				if (hasVanityAccess) {
					String customQuery = "SELECT xcdt.html_body AS \"body\", xcdt.subject AS \"subject\", xcp.company_logo  "
							+ "as " + " \"companyLogoPath\", xcp.website as \"vendorOrganizationName\" "
							+ "FROM xt_custom_default_templates xcdt "
							+ "JOIN xt_user_profile xup ON xcdt.company_id = xup.company_id "
							+ "JOIN xt_company_profile xcp ON xup.company_id = xcp.company_id "
							+ "WHERE xcdt.default_email_template_id = :id " + "AND xup.user_id = :loggedInUserId";

					HibernateSQLQueryResultRequestDTO requestDTO = new HibernateSQLQueryResultRequestDTO();
					requestDTO.setQueryString(customQuery);
					requestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("id", id));
					requestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("loggedInUserId", loggedInUserId));
					EmailTemplateDTO customTemplate = (EmailTemplateDTO) hibernateSQLQueryResultUtilDao
							.getDto(requestDTO, EmailTemplateDTO.class);
					if (customTemplate != null) {
						customTemplate.setBody(customTemplate.getBody().replaceAll(VANITY_COMPANY_LOGO,
								serverPath + customTemplate.getCompanyLogoPath()));
						customTemplate.setBody(customTemplate.getBody().replaceAll(replaceCompanyLogo,
								serverPath + customTemplate.getCompanyLogoPath()));
						response.setStatusCode(200);
						response.setMessage("Successfully fetched custom email template.");
						response.setData(customTemplate);
					} else {
						fetchDefaultTemplate(id, response, loggedInUserId);
					}
				} else {
					fetchDefaultTemplate(id, response, loggedInUserId);
				}
			} else {
				fetchDefaultTemplate(id, response, loggedInUserId);
			}

		} catch (Exception e) {
			response.setStatusCode(500);
			response.setMessage("Failed to fetch email template: " + e.getMessage());
			e.printStackTrace();
			response.setData(null);
		}
		return response;
	}

	private void fetchDefaultTemplate(Integer id, XtremandResponse response, Integer loggedInUserId) {
		boolean hasVanityAccess = utilDao.hasVanityAccessByUserId(loggedInUserId);
		if (hasVanityAccess) {
			String defaultQuery = "SELECT det.html_body AS \"body\", det.subject AS \"subject\", cp.company_logo as \"companyLogoPath\", "
					+ "cp.website as \"vendorOrganizationName\" " + "FROM xt_default_email_templates det "
					+ "JOIN xt_company_profile cp ON cp.company_id = (SELECT up.company_id FROM xt_user_profile up WHERE up.user_id = :loggedInUserId) "
					+ "WHERE det.id = :id";

			HibernateSQLQueryResultRequestDTO defaultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			defaultRequestDTO.setQueryString(defaultQuery);
			defaultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("id", id));
			defaultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("loggedInUserId", loggedInUserId));

			EmailTemplateDTO defaultTemplate = (EmailTemplateDTO) hibernateSQLQueryResultUtilDao
					.getDto(defaultRequestDTO, EmailTemplateDTO.class);

			if (defaultTemplate != null) {
				defaultTemplate.setBody(defaultTemplate.getBody().replaceAll(VANITY_COMPANY_LOGO,
						serverPath + defaultTemplate.getCompanyLogoPath()));
				response.setStatusCode(200);
				response.setMessage("Successfully fetched default email template.");
				response.setData(defaultTemplate);
			} else {
				response.setStatusCode(404);
				response.setMessage("No default email template found for the provided ID.");
				response.setData(null);

			}
		} else {
			EmailTemplateDTO fallbackTemplate = null;
			HibernateSQLQueryResultRequestDTO fallbackRequestDTO = new HibernateSQLQueryResultRequestDTO();
			String templateQuery = null;
			if (id == 34) {
				templateQuery = "SELECT  et.id AS  \"id\",  et.subject AS \"subject\",et.html_body AS \"body\" "
						+ "FROM xt_default_email_templates et WHERE et.id = :id";
				fallbackRequestDTO.setQueryString(templateQuery);
				fallbackRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("id", id));

			} else {
				templateQuery = "SELECT et.id AS \"id\", et.subject AS \"subject\", et.body AS \"body\" "
						+ "FROM xt_email_templates et " + "WHERE et.id = :id";
				fallbackRequestDTO.setQueryString(templateQuery);
				fallbackRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("id", id));
			}

			fallbackTemplate = (EmailTemplateDTO) hibernateSQLQueryResultUtilDao.getDto(fallbackRequestDTO,
					EmailTemplateDTO.class);
			fallbackTemplate.setBody(fallbackTemplate.getBody().replaceAll("VENDOR_COMPANY_LOGO", xAmplifyLogo));
			fallbackTemplate.setBody(fallbackTemplate.getBody().replaceAll("<Org_Admin_Company_logo>", xAmplifyLogo));
			fallbackTemplate.setBody(fallbackTemplate.getBody().replaceAll(VANITY_COMPANY_LOGO, xAmplifyLogo));

			response.setStatusCode(200);
			response.setMessage("Fallback to template 638.");
			response.setData(fallbackTemplate);
		}
	}

	public XtremandResponse getTemplateId(String emailId, Integer loggedInUserId, String isInactivePartnersDiv) {
		XtremandResponse response = new XtremandResponse();
		User user = userDao.getUserByEmail(emailId);
		response.setStatusCode(200);

		boolean hasVanityAccess = utilDao.hasVanityAccessByUserId(loggedInUserId);
		boolean isPrm = utilDao.isPrmCompany(loggedInUserId);

		if (XamplifyUtils.isValidString(isInactivePartnersDiv)
				&& isInactivePartnersDiv.equalsIgnoreCase("isInactivePartnersDiv")) {
			if (hasVanityAccess) {
				response.setData(26);
				response.setMessage("Template ID 26 returned for unapproved user with vanity access: " + emailId);
			} else {
				response.setData(55);
				response.setMessage("Template ID 55 returned for unapproved user without vanity access: " + emailId);
			}
		} else if (XamplifyUtils.isValidString(isInactivePartnersDiv)
				&& isInactivePartnersDiv.equalsIgnoreCase("isIncompleteCompanyProfileDiv")) {
			if (hasVanityAccess) {
				response.setData(27);
				response.setMessage("Template ID 27 returned for unapproved user with vanity access: " + emailId);
			} else {
				response.setData(1682);
				response.setMessage("Template ID 1682 returned for unapproved user without vanity access: " + emailId);
			}
		} else if (XamplifyUtils.isValidString(isInactivePartnersDiv)
				&& isInactivePartnersDiv.equalsIgnoreCase("teamMemberFilter")) {
			if (hasVanityAccess) {
				response.setData(34);
				response.setMessage("Template ID 34 returned for unapproved user with vanity access: " + emailId);
				response.setStatusCode(200);
			} else {
				response.setData(34);
				response.setMessage("Template ID 34 returned for unapproved user with vanity access: " + emailId);
				response.setStatusCode(200);

			}
		} else {
			if (isPrm) {
				if (XamplifyUtils.isValidString(isInactivePartnersDiv)
						&& isInactivePartnersDiv.equalsIgnoreCase("isSingUpPendingDiv")
						&& XamplifyUtils.isValidString(user.getPassword())
						&& UserStatus.UNAPPROVED.equals(user.getUserStatus())) {
					response.setData(2);
					response.setMessage("Template ID 2 returned for PRM user with password: " + emailId);
				} else {
					response.setData(14);
					response.setMessage("Template ID 14 returned for PRM user without password: " + emailId);
				}
			} else {
				if (XamplifyUtils.isValidString(isInactivePartnersDiv)
						&& isInactivePartnersDiv.equalsIgnoreCase("isSingUpPendingDiv")
						&& XamplifyUtils.isValidString(user.getPassword())
						&& UserStatus.UNAPPROVED.equals(user.getUserStatus())) {
					response.setData(2);
					response.setMessage("Template ID 2 returned for non-PRM user with password: " + emailId);
				} else {
					response.setData(4);
					response.setMessage("Template ID 4 returned for non-PRM user without password: " + emailId);
				}
			}
			if (!hasVanityAccess) {
				if (isPrm) {
					if (XamplifyUtils.isValidString(user.getPassword())) {
						response.setData(405);
						response.setMessage(
								"Template ID 405 returned for PRM user without vanity access but with password: "
										+ emailId);
					} else {
						response.setData(1178);
						response.setMessage(
								"Template ID 1178 returned for PRM user without vanity access and without password: "
										+ emailId);
					}
				} else {
					if (XamplifyUtils.isValidString(user.getPassword())) {
						response.setData(405);
						response.setMessage(
								"Template ID 405 returned for user without vanity access but with password: "
										+ emailId);
					} else {
						response.setData(638);
						response.setMessage(
								"Template ID 638 returned for user without vanity access and without password: "
										+ emailId);
					}
				}
			}

		}

		return response;
	}

	public XtremandResponse getMDFFundingTemplateHtmlBody(Integer loggedInUserId, String alias, String recipientName,
			String campaignName) {
		Integer templateId = vanityURLDao
				.getDefaultTemplateIdByType(DefaultEmailTemplateType.UNLOCK_MDF_FUNDING.name());
		XtremandResponse response = getHtmlBody(templateId, loggedInUserId);
		if (XamplifyUtils.isSuccessfulResponse(response)) {
			EmailTemplateDTO emailTemplateDTO = (EmailTemplateDTO) response.getData();

			String htmlBody = emailTemplateDTO.getBody();
			String companyLogoPath = serverPath + emailTemplateDTO.getCompanyLogoPath();
			htmlBody = htmlBody.replace(defaultCompanyLogoUrl, companyLogoPath);
			CompanyDetailsDTO companyDetailsDTO = companyProfileDao.findCompanyDetailsByLoggedInUserId(loggedInUserId);
			String vanityUrl = "";
			if (companyDetailsDTO != null) {
				htmlBody = replaceVendorCompanyName(htmlBody, companyDetailsDTO);
				htmlBody = replaceVendorCompanyUrl(htmlBody, companyDetailsDTO);
				vanityUrl = xamplifyUtil.frameVanityURL(webUrl, companyDetailsDTO.getCompanyProfileName());
			} else {
				vanityUrl = webUrl;
			}

			htmlBody = replaceCampaignAnaltyicsUrlMergeTag(alias, htmlBody, vanityUrl);

			htmlBody = replaceSearchLinkMergeTag(htmlBody, vanityUrl);

			if (XamplifyUtils.isValidString(recipientName)) {
				htmlBody = htmlBody.replace("{{recipientName}}", recipientName);
			}
			if (XamplifyUtils.isValidString(campaignName)) {
				htmlBody = htmlBody.replace("{{campaignName}}", campaignName);
			}
			htmlBody = addMdfAliasMergeTag(alias, htmlBody);

			emailTemplateDTO.setBody(htmlBody);
			response.setData(emailTemplateDTO);
		}
		return response;
	}

	private String replaceCampaignAnaltyicsUrlMergeTag(String alias, String htmlBody, String vanityUrl) {
		String hrefLink = XamplifyUtils.isValidString(alias) ? vanityUrl + "funding-request/" + alias + "/analytics"
				: "javascript:void(0)";
		String campaignLink = "<a href=\"" + hrefLink + "\" target=\"blank\">Please click here to see analytics</a>";
		htmlBody = htmlBody.replace("{{campaignAnalyticsLink}}", campaignLink);
		return htmlBody;
	}

	private String replaceSearchLinkMergeTag(String htmlBody, String vanityUrl) {
		String searchLink = vanityUrl + "search";
		String searchAnchorLink = "<a href=\"" + searchLink
				+ "\" target=\"blank\">Please click here and enter the key to access analytics</a>";
		htmlBody = htmlBody.replace("{{mdfKeySearchLink}}", searchAnchorLink);
		return htmlBody;
	}

	private String addMdfAliasMergeTag(String alias, String htmlBody) {
		String mdfAlias = XamplifyUtils.isValidString(alias) ? alias : "XXXXXX";
		String mdfAliasMergeTag = "<table role=\"presentation\">\n" + "        <tr>\n"
				+ "            <td align=\"center\">\n"
				+ "                <table role=\"presentation\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" width=\"500\" style=\"background: #d3d3d3 !important; padding: 30px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">\n"
				+ "                    <tr>\n" + "                        <td align=\"center\">\n"
				+ "                            <h2 style=\"color: #333; margin-bottom: 10px;font-family: Arial, sans-serif\">Campaign MDF Key</h2>\n"
				+ "                            <p style=\"color: #666; font-size: 16px; margin-bottom: 20px;font-family: Arial, sans-serif\">Use the following Campaign MDF Key to find analytics.</p>\n"
				+ "                            <div style=\"font-size: 32px; font-weight: bold; color: #2c3e50; letter-spacing: 0px; background: #f0f0f0; padding: 20px 40px; display: inline-block; border-radius: 8px;font-family: Arial, sans-serif;background-color: #f4f4f4;margin-bottom: 20px;\">\n"
				+ mdfAlias + "\n                            </div>\n" + "                        </td>\n"
				+ "                    </tr>\n" + "                </table>\n" + "            </td>\n"
				+ "        </tr>\n" + "    </table>";

		htmlBody = htmlBody.replace("{{mdfKey}}", mdfAliasMergeTag);
		return htmlBody;
	}

	private String replaceVendorCompanyUrl(String htmlBody, CompanyDetailsDTO companyDetailsDTO) {
		String companyUrl = companyDetailsDTO.getCompanyUrl();
		if (!org.springframework.util.StringUtils.hasText(companyUrl)) {
			companyUrl = "javascript:void(0)";
		}
		htmlBody = htmlBody.replace(VANITY_COMPANY_LOGO_HREF, companyUrl);
		return htmlBody;
	}

	private String replaceVendorCompanyName(String htmlBody, CompanyDetailsDTO companyDetailsDTO) {
		String vendorCompanyName = companyDetailsDTO.getCompanyName();
		if (!org.springframework.util.StringUtils.hasText(vendorCompanyName)) {
			vendorCompanyName = "";
		}
		htmlBody = htmlBody.replace(VENDOR_COMPANY_NAME, vendorCompanyName);
		return htmlBody;
	}

	/** XNFR-618 **/
	public XtremandResponse getSupportEmailIdByCompanyProfileName(String companyProfileName) {
		XtremandResponse response = new XtremandResponse();
		if (!XamplifyUtils.isValidString(companyProfileName)) {
			XamplifyUtils.addErorMessageWithStatusCode(response, XamplifyConstants.INVALID_INPUT, 400);
			return response;
		}
		Integer companyId = userDao.getCompanyIdByProfileName(companyProfileName);
		if (XamplifyUtils.isValidInteger(companyId)) {
			String supportEmailId = userDao.getSupportEmailIdByCompanyId(companyId);
			if (XamplifyUtils.isValidString(supportEmailId)) {
				response.setData(supportEmailId);
				XamplifyUtils.addSuccessStatus(response);
			}
		}
		return response;
	}

	public XtremandResponse sendPartnerSignatureReminder() {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(200);
		return response;
	}

	public XtremandResponse getPartnerRemainderTemplate(Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();

		try {
			DefaultEmailTemplateType templateType = DefaultEmailTemplateType.PARTNER_SIGNATURE_PENDING;
			DefaultEmailTemplate defaultTemplate = vanityURLDao.getVanityDefaultEmailTemplateByType(templateType);
			Integer templateId = defaultTemplate.getId();

			boolean hasVanityAccess = utilDao.hasVanityAccessByUserId(loggedInUserId);

			EmailTemplateDTO emailTemplate = hasVanityAccess
					? vanityURLDao.getCustomEmailTemplate(templateId, loggedInUserId)
					: null;

			if (emailTemplate == null) {
				emailTemplate = vanityURLDao.getDefaultEmailTemplateById(templateId, loggedInUserId);
			}

			if (emailTemplate == null) {
				response.setStatusCode(404);
				response.setMessage("No email template found for the provided ID.");
				return response;
			}

			String logoPath = hasVanityAccess ? serverPath + emailTemplate.getCompanyLogoPath() : xAmplifyLogo;

			String updatedBody = emailTemplate.getBody();

			updatedBody = updatedBody.replaceAll(VANITY_COMPANY_LOGO, logoPath);
			updatedBody = updatedBody.replaceAll(replaceCompanyLogo, logoPath);

			if (!hasVanityAccess) {
				updatedBody = updatedBody.replaceAll(VANITY_COMPANY_LOGO_HREF, xamplifyUtil.getLoginUrl());
			}

			emailTemplate.setBody(updatedBody);

			response.setStatusCode(200);
			response.setMessage("Successfully fetched email template.");
			response.setData(emailTemplate);

		} catch (Exception e) {
			e.printStackTrace();
			response.setStatusCode(500);
			response.setMessage("Failed to fetch email template: " + e.getMessage());
			response.setData(null);
		}

		return response;
	}

	public XtremandResponse getWelcomeTemplateForPartnerDomainWhitelisting(Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();

		try {
			DefaultEmailTemplateType templateType = DefaultEmailTemplateType.WELCOME_EMAIL_REMAINDER;
			DefaultEmailTemplate defaultTemplate = vanityURLDao.getVanityDefaultEmailTemplateByType(templateType);
			Integer templateId = defaultTemplate.getId();

			boolean hasVanityAccess = utilDao.hasVanityAccessByUserId(loggedInUserId);

			EmailTemplateDTO emailTemplate = hasVanityAccess
					? vanityURLDao.getCustomEmailTemplate(templateId, loggedInUserId)
					: null;

			if (emailTemplate == null) {
				emailTemplate = vanityURLDao.getDefaultEmailTemplateById(templateId, loggedInUserId);
			}

			if (emailTemplate == null) {
				response.setStatusCode(404);
				response.setMessage("No email template found for the provided ID.");
				return response;
			}

			String logoPath = hasVanityAccess ? serverPath + emailTemplate.getCompanyLogoPath() : xAmplifyLogo;

			String updatedBody = emailTemplate.getBody();

			updatedBody = updatedBody.replaceAll(VANITY_COMPANY_LOGO, logoPath);
			updatedBody = updatedBody.replaceAll(replaceCompanyLogo, logoPath);

			if (!hasVanityAccess) {
				updatedBody = updatedBody.replaceAll(VANITY_COMPANY_LOGO_HREF, xamplifyUtil.getLoginUrl());
			}

			emailTemplate.setBody(updatedBody);

			response.setStatusCode(200);
			response.setMessage("Successfully fetched email template.");
			response.setData(emailTemplate);

		} catch (Exception e) {
			e.printStackTrace();
			response.setStatusCode(500);
			response.setMessage("Failed to fetch email template: " + e.getMessage());
			response.setData(null);
		}

		return response;
	}

	private EmailTemplateDTO findTemplate(String companyProfileName, boolean hasVanityAccess, Integer loggedInUserId) {
		EmailTemplateDTO template = null;
		if (XamplifyUtils.isValidString(companyProfileName) || hasVanityAccess) {
			template = teamDao.fetchCustomTemplate(46, loggedInUserId);
			if (template == null) {
				template = teamDao.fetchDefaultTemplate(46, loggedInUserId);
			}
		} else {
			template = teamDao.fetchDefaultTemplate(46, loggedInUserId);
		}
		prepareTemplateBody(template);
		return template;
	}

	private void prepareTemplateBody(EmailTemplateDTO template) {
		String logoPath = serverPath + template.getCompanyLogoPath();
		template.setBody(template.getBody().replace("<Vanity_Company_Logo>", logoPath));
		template.setBody(template.getBody().replace(replaceCompanyLogo, logoPath));
	}

	private String customerFullNameMergeTag(User customer) {
		String customerNameEmpty = "";
		if (customer.getFirstName() != null && !customer.getFirstName().trim().isEmpty()) {
			customerNameEmpty += customer.getFirstName().trim();
		}
		if (customer.getMiddleName() != null && !customer.getMiddleName().trim().isEmpty()) {
			if (!customerNameEmpty.isEmpty()) {
				customerNameEmpty += " ";
			}
			customerNameEmpty += customer.getMiddleName().trim();
		}
		if (customer.getLastName() != null && !customer.getLastName().trim().isEmpty()) {
			if (!customerNameEmpty.isEmpty()) {
				customerNameEmpty += " ";
			}
			customerNameEmpty += customer.getLastName().trim();
		}
		String senderName = customerNameEmpty.isEmpty() ? "There" : customerNameEmpty;
		return senderName;
	}

	private String getFromEmail(boolean isDnsConfigured, User vendor, String fromEmail) {
		return isDnsConfigured ? vendor.getEmailId() : fromEmail;
	}

	public XtremandResponse sendWelcomeMailForPartnerDomainWhitelisting(SendTestEmailDTO sendTestEmailDTO) {
		XtremandResponse response = new XtremandResponse();
		Integer loggedInUserId = sendTestEmailDTO.getLoggedInUserId();
		User vendor = userService.loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, loggedInUserId)),
				new FindLevel[] { FindLevel.COMPANY_PROFILE });
		CompanyProfile vendorCompanyProfile = vendor.getCompanyProfile();

		boolean isDnsConfigured = userDao.isDnsConfigured(vendorCompanyProfile.getId());

		String frmEmail = getFromEmail(isDnsConfigured, vendor, fromEmail);
		String companyProfileName = sendTestEmailDTO.getCompanyProfileName();
		boolean hasVanityAccess = utilDao.hasVanityAccessByUserId(loggedInUserId);

		EmailTemplateDTO template = findTemplate(companyProfileName, hasVanityAccess, loggedInUserId);
		prepareTemplateBody(template);
		String templateBody = template.getBody();

		String firstName = XamplifyUtils.isValidString(vendor.getFirstName()) ? vendor.getFirstName() : "There";
		String lastName = XamplifyUtils.isValidString(vendor.getLastName()) ? vendor.getLastName() : "There";
		String senderFullName = mailService.getFirstNameMiddleNameLastName(vendor.getFirstName(),
				vendor.getMiddleName(), vendor.getLastName());

		String vanityURLDomain = hasVanityAccess
				? xamplifyUtil.frameVanityURL(webUrl, vendorCompanyProfile.getCompanyProfileName())
				: webUrl;

		String companyLogoHref = hasVanityAccess ? utilService.getWebsite(vendorCompanyProfile.getWebsite())
				: xamplifyUtil.getLoginUrl();

		String companyLogoImage = hasVanityAccess ? null : xAmplifyLogo;
		List<String> deactivatedPartners = partnerShipDao
				.findDeactivatedPartnersByCompanyId(vendorCompanyProfile.getId());

		for (String recipientEmail : sendTestEmailDTO.getToEmailIds()) {
			if (deactivatedPartners.contains(recipientEmail))
				continue;
			User user = userDao.getFirstNameLastNameMidlleNameByEmailId(recipientEmail);
			String customerFullName = (user != null) ? customerFullNameMergeTag(user) : "There";

			String personalizedBody = templateBody.replace("{{customerFullName}}", customerFullName)
					.replace("{{senderFullName}}", senderFullName)
					.replace(VENDOR_COMPANY_NAME, vendorCompanyProfile.getCompanyName())
					.replace("{{firstName}}", firstName).replace("{{lastName}}", lastName)
					.replace("{{emailId}}", vendor.getEmailId())
					.replace("{{sendorCompanyName}}", vendorCompanyProfile.getCompanyName())
					.replace("login_url", vanityURLDomain).replace(VANITY_COMPANY_LOGO_HREF, companyLogoHref);

			if (XamplifyUtils.isValidString(companyLogoImage)) {
				personalizedBody = personalizedBody.replace(replaceCompanyLogo, companyLogoImage);
			}

			mailService.sendMail(new EmailBuilder().from(frmEmail).senderName(vendor.getCompanyName())
					.to(recipientEmail).subject(template.getSubject()).body(personalizedBody).build());
		}

		response.setStatusCode(200);
		response.setMessage("Email sent successfully.");
		return response;
	}

	public XtremandResponse sendWelcomeMailForPartnerDomainWhitelisting(SendTestEmailDTO sendTestEmailDTO,
			List<MultipartFile> attachments, BindingResult result) {
		XtremandResponse response = new XtremandResponse();
		emailValidator.validateAttachmentFile(attachments, result);
		if (result.hasErrors()) {
			xamplifyUtilValidator.addErrorResponse(result, response);
		} else {
			setDetailsAndSendWelcomeMail(sendTestEmailDTO, attachments, response);
		}
		return response;
	}

	private void setDetailsAndSendWelcomeMail(SendTestEmailDTO sendTestEmailDTO, List<MultipartFile> attachments,
			XtremandResponse response) {
		Integer loggedInUserId = sendTestEmailDTO.getLoggedInUserId();
		User vendor = userService.loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, loggedInUserId)),
				new FindLevel[] { FindLevel.COMPANY_PROFILE });
		CompanyProfile vendorCompanyProfile = vendor.getCompanyProfile();

		boolean isDnsConfigured = userDao.isDnsConfigured(vendorCompanyProfile.getId());

		String frmEmail = getFromEmail(isDnsConfigured, vendor, fromEmail);
		String companyProfileName = sendTestEmailDTO.getCompanyProfileName();
		boolean hasVanityAccess = utilDao.hasVanityAccessByUserId(loggedInUserId);

		EmailTemplateDTO template = findTemplate(companyProfileName, hasVanityAccess, loggedInUserId);
		prepareTemplateBody(template);
		String templateBody = template.getBody();

		String firstName = XamplifyUtils.isValidString(vendor.getFirstName()) ? vendor.getFirstName() : "There";
		String lastName = XamplifyUtils.isValidString(vendor.getLastName()) ? vendor.getLastName() : "There";
		String senderFullName = mailService.getFirstNameMiddleNameLastName(vendor.getFirstName(),
				vendor.getMiddleName(), vendor.getLastName());

		String vanityURLDomain = hasVanityAccess
				? xamplifyUtil.frameVanityURL(webUrl, vendorCompanyProfile.getCompanyProfileName())
				: webUrl;

		String companyLogoHref = hasVanityAccess ? utilService.getWebsite(vendorCompanyProfile.getWebsite())
				: xamplifyUtil.getLoginUrl();

		String companyLogoImage = hasVanityAccess ? null : xAmplifyLogo;
		List<String> deactivatedPartners = partnerShipDao
				.findDeactivatedPartnersByCompanyId(vendorCompanyProfile.getId());
		Integer deactivatedPartnersCount = 0;
		EmailActivity email = new EmailActivity();
		email.setBody(templateBody);
		email.setSubject(sendTestEmailDTO.getSubject());
		User sender = new User();
		sender.setUserId(loggedInUserId);
		email.setSender(sender);
		email.setCreatedTime(new Date());
		email.setStatus(EmailActivityStatusEnum.DELIVERED);
		CompanyProfile companyProfile = new CompanyProfile();
		Integer companyId = userDAO.getCompanyIdByUserId(loggedInUserId);
		companyProfile.setId(companyId);
		sendTestEmailDTO.setCompanyId(companyId);
		email.setCompanyProfile(companyProfile);
		Integer taskId = genericDao.save(email);
		sendTestEmailDTO.setTrackId(taskId);
		saveEmailIds(sendTestEmailDTO.getToEmailIds(), EmailRecipientEnum.TO, email);
		saveEmailIds(sendTestEmailDTO.getCcEmailIds(), EmailRecipientEnum.CC, email);
		saveEmailIds(sendTestEmailDTO.getBccEmailIds(), EmailRecipientEnum.BCC, email);
		uploadFiles(sendTestEmailDTO, attachments, response);
		for (String recipientEmail : sendTestEmailDTO.getToEmailIds()) {
			if (deactivatedPartners.contains(recipientEmail)) {
				deactivatedPartnersCount++;
			} else {
				User user = userDao.getFirstNameLastNameMidlleNameByEmailId(recipientEmail);

				String customerFullName = (user != null) ? customerFullNameMergeTag(user) : "There";

				String personalizedBody = templateBody.replace("{{customerFullName}}", customerFullName)
						.replace("{{senderFullName}}", senderFullName)
						.replace(VENDOR_COMPANY_NAME, vendorCompanyProfile.getCompanyName())
						.replace("{{firstName}}", firstName).replace("{{lastName}}", lastName)
						.replace("{{emailId}}", vendor.getEmailId())
						.replace("{{sendorCompanyName}}", vendorCompanyProfile.getCompanyName())
						.replace("login_url", vanityURLDomain).replace(VANITY_COMPANY_LOGO_HREF, companyLogoHref);
				if (companyLogoImage != null) {
					personalizedBody = personalizedBody.replace(replaceCompanyLogo, companyLogoImage);
				}
				mailService.sendMail(new EmailBuilder().from(frmEmail).senderName(vendorCompanyProfile.getCompanyName())
						.to(recipientEmail).subject(sendTestEmailDTO.getSubject()).body(personalizedBody)
						.ccEmailIds(sendTestEmailDTO.getCcEmailIds()).bccEmailIds(sendTestEmailDTO.getBccEmailIds())
						.attachments(attachments).build());

			}
		}
		if (deactivatedPartnersCount == sendTestEmailDTO.getToEmailIds().size()) {
			response.setStatusCode(402);
			response.setMessage("Email(s) cannot be sent to deactivated partner(s)");
		} else {
			response.setStatusCode(200);
			response.setMessage("Email sent successfully.");
		}
	}

	private XtremandResponse uploadFiles(SendTestEmailDTO sendTestEmailDTO, List<MultipartFile> attachments,
			XtremandResponse response) {
		try {
			List<ActivityAWSDTO> emailActivityAWSDTOs = new ArrayList<>();
			if (XamplifyUtils.isNotEmptyList(attachments)) {
				for (MultipartFile uploadedFile : attachments) {
					emailActivityAWSDTOs.add(upload(uploadedFile, sendTestEmailDTO));
				}
			}
			response.setData(emailActivityAWSDTOs);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return response;
	}

	private ActivityAWSDTO upload(MultipartFile uploadedFile, SendTestEmailDTO sendTestEmailDTO) throws IOException {
		ActivityAWSDTO emailActivityAWSDTO = new ActivityAWSDTO();
		copyFilesAndUpload(uploadedFile, sendTestEmailDTO, emailActivityAWSDTO);
		List<Integer> attachmentIds = saveEmailActivityAttachment(sendTestEmailDTO, emailActivityAWSDTO, uploadedFile);
		emailActivityAWSDTO.setAttachmentIds(attachmentIds);
		return emailActivityAWSDTO;
	}

	private List<Integer> saveEmailActivityAttachment(SendTestEmailDTO sendTestEmailDTO,
			ActivityAWSDTO emailActivityAWSDTO, MultipartFile uploadedFile) {
		List<Integer> activityAttachmentIds = new ArrayList<>();
		ActivityAttachment activityAttachment = new ActivityAttachment();
		activityAttachment.setFileName(emailActivityAWSDTO.getFileName());
		EmailActivity emailActivity = new EmailActivity();
		emailActivity.setId(sendTestEmailDTO.getTrackId());
		activityAttachment.setEmailActivity(emailActivity);
		activityAttachment.setFileType(uploadedFile.getContentType());
		activityAttachment.setSize(uploadedFile.getSize());
		activityAttachment.setTemporaryFilePath(emailActivityAWSDTO.getTemporaryFilePath());
		activityAttachmentIds.add(genericDao.save(activityAttachment));
		return activityAttachmentIds;
	}

	private void copyFilesAndUpload(MultipartFile uploadedFile, SendTestEmailDTO sendTestEmailDTO,
			ActivityAWSDTO emailActivityAWSDTO) throws IOException {
		CopiedFileDetails copiedAwsFileDetails = new CopiedFileDetails();
		copiedAwsFileDetails.setIsFromEmailActivity(true);
		String filePathSuffix = attachmentPath + sendTestEmailDTO.getCompanyId();
		getCopiedAwsFileDetails(uploadedFile, sendTestEmailDTO, emailActivityAWSDTO, copiedAwsFileDetails,
				filePathSuffix);
	}

	private void getCopiedAwsFileDetails(MultipartFile uploadedFile, SendTestEmailDTO sendTestEmailDTO,
			ActivityAWSDTO emailActivityAWSDTO, CopiedFileDetails copiedAwsFileDetails, String filePathSuffix)
			throws IOException {
		String fileName = uploadedFile.getOriginalFilename();
		amazonWebService.copyFileToXamplifyServer(uploadedFile, sendTestEmailDTO.getLoggedInUserId(), filePathSuffix,
				fileName, copiedAwsFileDetails, false);
		String completeFileName = copiedAwsFileDetails.getCompleteName();
		completeFileName = completeFileName.substring(0, completeFileName.lastIndexOf('.')) + "."
				+ (completeFileName.substring(completeFileName.lastIndexOf('.') + 1)).toLowerCase();
		emailActivityAWSDTO.setCompleteFileName(completeFileName);
		emailActivityAWSDTO.setCompanyId(sendTestEmailDTO.getCompanyId());
		emailActivityAWSDTO.setEmailActivityId(sendTestEmailDTO.getId());
		emailActivityAWSDTO.setUserId(sendTestEmailDTO.getLoggedInUserId());
		String fileType = fileUtil.getFileExtension(uploadedFile);
		emailActivityAWSDTO.setFileType(fileType);
		emailActivityAWSDTO.setFileName(fileName);
		emailActivityAWSDTO.setFilePath(copiedAwsFileDetails.getCopiedImageFilePath());
		emailActivityAWSDTO.setUpdatedFileName(copiedAwsFileDetails.getUpdatedFileName());
		emailActivityAWSDTO.setTemporaryFilePath(copiedAwsFileDetails.getCopiedImageFilePath());
	}

	private void saveEmailIds(List<String> emailIds, EmailRecipientEnum recipientType, EmailActivity emailActivity) {
		if (XamplifyUtils.isNotEmptyList(emailIds)) {
			for (String emailId : emailIds) {
				saveEmailId(emailId, recipientType, emailActivity);
			}
		}
	}

	private void saveEmailId(String emailId, EmailRecipientEnum emailRecipientEnum, EmailActivity emailActivity) {
		EmailRecipient emailRecipient = new EmailRecipient();
		emailRecipient.setEmailId(emailId);
		emailRecipient.setEmailActivity(emailActivity);
		emailRecipient.setEmailRecipientEnum(emailRecipientEnum);
		genericDao.save(emailRecipient);
	}

	public XtremandResponse getEmailTemplateByType(Integer loggedInUserId, String defaultEmailTemplateTypeInString) {
		XtremandResponse response = new XtremandResponse();

		try {
			DefaultEmailTemplateType templateType = DefaultEmailTemplateType.valueOf(defaultEmailTemplateTypeInString);
			DefaultEmailTemplate defaultTemplate = vanityURLDao.getVanityDefaultEmailTemplateByType(templateType);
			Integer templateId = defaultTemplate.getId();

			boolean hasVanityAccess = utilDao.hasVanityAccessByUserId(loggedInUserId);

			CompanyProfile companyProfile = userService.getCompanyProfileByUser(loggedInUserId);
			CustomDefaultEmailTemplate emailTemplate = hasVanityAccess
					? vanityURLDao.getVanityETByDefVanityETIdAndCompanyId(templateId, companyProfile)
					: null;
			List<DefaultEmailTemplateDTO> templates = new ArrayList<>();

			if (emailTemplate != null) {
				templates = isAccessModuleCustom(templates, companyProfile.getId(), Arrays.asList(emailTemplate));
			} else if (defaultTemplate != null) {
				templates = isAccessModuleDefault(templates, companyProfile.getId(), Arrays.asList(defaultTemplate));
			}
			response.setStatusCode(200);
			response.setMessage("Successfully fetched email template.");
			response.setData(templates.get(0));

		} catch (Exception e) {
			e.printStackTrace();
			response.setStatusCode(500);
			response.setMessage("Failed to fetch email template: " + e.getMessage());
			response.setData(null);
		}

		return response;
	}

}
