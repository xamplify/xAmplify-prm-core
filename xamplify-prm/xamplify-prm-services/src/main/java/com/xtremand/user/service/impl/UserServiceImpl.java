package com.xtremand.user.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.ParseException;
import org.hibernate.HibernateException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.xtremand.account.dto.ResendEmailDTO;
import com.xtremand.analytics.dao.PartnerAnalyticsDAO;
import com.xtremand.approve.dao.ApproveDAO;
import com.xtremand.approve.dto.ContentReApprovalDTO;
import com.xtremand.approve.service.ApproveService;
import com.xtremand.campaign.bom.DashboardTypeEnum;
import com.xtremand.campaign.bom.ModuleAccess;
import com.xtremand.campaign.bom.ModuleAccessDTO;
import com.xtremand.category.bom.Category;
import com.xtremand.category.dao.CategoryDao;
import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.CompanyProfile.CompanyNameStatus;
import com.xtremand.common.bom.Criteria;
import com.xtremand.common.bom.Criteria.OPERATION_NAME;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.common.bom.Module;
import com.xtremand.common.bom.ModuleCustom;
import com.xtremand.common.bom.Pagination;
import com.xtremand.common.bom.PartnerTeamMemberViewType;
import com.xtremand.company.bom.Company;
import com.xtremand.company.dao.CompanyProfileDao;
import com.xtremand.company.dto.ApprovalSettingsDTO;
import com.xtremand.dam.bom.ApprovalStatusHistory;
import com.xtremand.dam.bom.DamTag;
import com.xtremand.dam.dao.DamDao;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.deal.service.DealService;
import com.xtremand.domain.bom.DomainModuleNameType;
import com.xtremand.domain.dao.DomainDao;
import com.xtremand.drip.email.bom.DripEmailHistory;
import com.xtremand.exclude.bom.ExcludedDomain;
import com.xtremand.exclude.bom.ExcludedUser;
import com.xtremand.exclude.bom.ExcludedUserDTO;
import com.xtremand.flexi.fields.bom.FlexiField;
import com.xtremand.flexi.fields.bom.UserListFlexiField;
import com.xtremand.flexi.fields.dto.FlexiFieldRequestDTO;
import com.xtremand.form.bom.FormTypeEnum;
import com.xtremand.form.dao.FormDao;
import com.xtremand.form.dto.FormDTO;
import com.xtremand.form.service.FormService;
import com.xtremand.formbeans.AccountDTO;
import com.xtremand.formbeans.EmailDTO;
import com.xtremand.formbeans.EmailLogReport;
import com.xtremand.formbeans.RoleDTO;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.gdpr.setting.service.GdprSettingService;
import com.xtremand.integration.bom.ExternalContactDTO;
import com.xtremand.lms.dao.LMSDAO;
import com.xtremand.mail.bom.EmailTemplate;
import com.xtremand.mail.service.AsyncComponent;
import com.xtremand.mail.service.EmailConstants;
import com.xtremand.mail.service.MailService;
import com.xtremand.mail.service.StatusCodeConstants;
import com.xtremand.module.dao.ModuleDao;
import com.xtremand.partnership.bom.Partnership;
import com.xtremand.partnership.bom.PartnershipDTO;
import com.xtremand.partnership.dao.PartnershipDAO;
import com.xtremand.partnership.service.PartnershipService;
import com.xtremand.partnership.service.impl.PartnershipServiceHelper;
import com.xtremand.signup.dto.SignUpRequestDTO;
import com.xtremand.social.formbeans.TeamMemberDTO;
import com.xtremand.social.formbeans.UserPassword;
import com.xtremand.team.dao.TeamDao;
import com.xtremand.team.member.dto.RoleDisplayDTO;
import com.xtremand.team.member.dto.TeamMemberGroupDTO;
import com.xtremand.team.member.dto.TeamMemberModuleDTO;
import com.xtremand.team.member.group.bom.TeamMemberGroup;
import com.xtremand.team.member.group.bom.TeamMemberGroupRoleMapping;
import com.xtremand.team.member.group.dao.TeamMemberGroupDao;
import com.xtremand.team.member.group.service.TeamMemberGroupService;
import com.xtremand.team.service.TeamService;
import com.xtremand.unsubscribe.service.UnsubscribeService;
import com.xtremand.user.bom.AllAccountsView;
import com.xtremand.user.bom.AllUsersView;
import com.xtremand.user.bom.LegalBasis;
import com.xtremand.user.bom.ModulesDisplayType;
import com.xtremand.user.bom.PartnerCompany;
import com.xtremand.user.bom.Role;
import com.xtremand.user.bom.TeamMember;
import com.xtremand.user.bom.TeamMemberStatus;
import com.xtremand.user.bom.User;
import com.xtremand.user.bom.User.UserDefaultPage;
import com.xtremand.user.bom.User.UserStatus;
import com.xtremand.user.bom.UserCustomer;
import com.xtremand.user.bom.UserCustomerId;
import com.xtremand.user.bom.UserList;
import com.xtremand.user.bom.UserSource;
import com.xtremand.user.bom.UserUserList;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.user.exception.UserDataAccessException;
import com.xtremand.user.service.UserService;
import com.xtremand.user.validation.UserListValidator;
import com.xtremand.userlist.dao.hibernate.HibernateUserListDAO;
import com.xtremand.userlist.service.UserListService;
import com.xtremand.util.CustomExceptionMessage;
import com.xtremand.util.CustomValidatonException;
import com.xtremand.util.DateUtils;
import com.xtremand.util.DripEmailConstants;
import com.xtremand.util.EmailValidatorUtil;
import com.xtremand.util.GenerateRandomPassword;
import com.xtremand.util.ResponseUtil;
import com.xtremand.util.XamplifyUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.bom.ModuleType;
import com.xtremand.util.dao.HibernateSQLQueryResultUtilDao;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.dao.XamplifyUtilDao;
import com.xtremand.util.dto.CompanyDTO;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.ModulesEmailNotification;
import com.xtremand.util.dto.QueryParameterDTO;
import com.xtremand.util.dto.ViewTypePatchRequestDTO;
import com.xtremand.util.service.CsvUtilService;
import com.xtremand.util.service.EmailValidatorService;
import com.xtremand.util.service.ThymeLeafService;
import com.xtremand.util.service.UtilService;
import com.xtremand.vanity.url.dao.VanityURLDao;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;
import com.xtremand.video.bom.VideoDefaultSettings;
import com.xtremand.video.bom.VideoFile;
import com.xtremand.video.bom.VideoLead;
import com.xtremand.video.bom.VideoTag;
import com.xtremand.video.dao.VideoDao;
import com.xtremand.video.exception.VideoDataAccessException;
import com.xtremand.video.service.VideoService;

import au.com.bytecode.opencsv.CSV;
import au.com.bytecode.opencsv.CSVReader;

@Service("userService")
@Transactional
public class UserServiceImpl implements UserService {

	private static final String EMAIL_ID = "emailId";

	private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

	private static final Pattern PASSWORD_PATTERN = Pattern
			.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9])\\S{6,20}$");

	private static final CSV csv = CSV.separator(',').quote('\'').skipLines(1).charset("UTF-8").create();

	private static final String VIDEO_TAGS_TO_SAVE_MAP_KEY = "videoTagsToSave";

	private static final String VIDEO_IDS_TO_DELETE_MAP_KEY = "videoIdsToDelete";

	private static final String WHITE_LABELED_RE_APPROVAL_DAM_IDS = "whiteLabeledReApprovalDamIds";

	@Value("${spring.profiles.active}")
	private String profiles;

	@Value("${access.denied}")
	protected String ACCESS_DENIED;

	@Value("${user.existing.message}")
	protected String USER_EXISTING;

	@Value("${default-company-folder-suffix}")
	private String defaultFolderSuffix;

	@Value("${fa.folder.open}")
	private String faIcon;

	@Value("${account.already.exist}")
	private String accountAlreadyExists;

	@Value("${signup.success}")
	private String signUpSucessMessage;

	@Value("${teammember.signup.success}")
	private String teamMemberSignUpSuccessMessage;

	@Value("${server_path}")
	String server_path;

	@Value("${images_path}")
	String images_path;

	@Value("${media_base_path}")
	String mediaBasePath;

	@Value("${separator}")
	String sep;

	@Value("${specialCharacters}")
	String regex;

	@Value("${invalidVendorEmailId}")
	String invalidVendorEmailId;

	@Value("${web_url}")
	String webUrl;

	@Value("${default.player.color}")
	private String playerDefaultColor;

	@Value("${default.controller.color}")
	private String controllerDefaultColor;

	@Value("${brandfetch.endpoint}")
	private String brandFetchEndpoint;

	@Value("${brandfetch.api.token}")
	private String brandFetchApiToken;

	@Value("#{'${company.profile.name.suffixes}'.split(',')}")
	private List<String> companyProfileNameSuffixesForAutoFilling;

	@Autowired
	private DealService dealService;

	@Autowired
	private UserDAO userDAO;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private GenericDAO genericDAO;

	@Autowired
	private MailService mailService;

	@Autowired
	EmailValidatorService emailValidatorService;

	@Autowired
	HibernateUserListDAO userListDAO;

	@Autowired
	VideoService videoService;

	@Autowired
	PartnerAnalyticsDAO partnerAnalyticsDAO;

	@Autowired
	GdprSettingService gdprSettingService;

	@Autowired
	PartnershipDAO partnershipDAO;

	@Autowired
	UserListService userListService;

	@Autowired
	PartnershipService partnershipService;

	@Autowired
	PartnershipServiceHelper partnershipServiceHelper;

	@Autowired
	private UserListValidator userListValidator;

	@Autowired
	private CategoryDao categoryDao;

	@Autowired
	@Lazy
	private AsyncComponent asyncComponent;

	@Autowired
	private DamDao damDao;

	@Autowired
	private LMSDAO lmsDao;

	@Autowired
	private XamplifyUtil xamplifyUtil;

	@Autowired
	EmailValidatorUtil emailValidator;

	@Autowired
	private UtilService utilService;

	@Autowired
	private CsvUtilService csvUtilService;

	@Autowired
	private UnsubscribeService unsubscribeService;

	@Autowired
	private TeamDao teamDao;

	@Autowired
	private UtilDao utilDao;

	@Autowired
	private TeamMemberGroupDao teamMemberGroupDao;

	@Autowired
	private TeamService teamService;

	@Autowired
	private TeamMemberGroupService teamMemberGroupService;

	@Value("${devImagesHost}")
	private String devImagesHost;

	@Autowired
	private VideoDao videoDao;

	@Autowired
	private VanityURLDao vanityURLDao;

	@Autowired
	private ApproveService approveService;

	@Value("${drip.email.notifications.enabled}")
	private String dripEmailNotificationsEnabled;

	@Autowired
	private DomainDao domainDao;

	@Autowired
	private HibernateSQLQueryResultUtilDao hibernateSQLQueryResultUtilDao;

	@Autowired
	private FormDao formDao;

	@Autowired
	private FormService formService;
	/**** XNFR-428 ***/

	/**** XNFR-656 ***/
	@Autowired
	private XamplifyUtilDao xamplifyUtilDao;

	@Autowired
	private ModuleDao moduleDao;

	@Autowired
	private ApproveDAO approveDao;

	@Value("${dev.host}")
	String devHost;

	@Value("${prod.host}")
	String productionHost;

	@Value("${images.folder}")
	String vod;

	@Value("${company.logo.url}")
	private String companyLogoDefaultUrl;

	@Value("${existing.external.leads.csv}")
	String existingExternalLeadsPath;

	@Value("${external.colt.csv}")
	String externalColtCsvPath;

	@Value("${external.teamMemberEmails.csv}")
	String externalTeamMemberEmailsCsvPath;

	@Value("${mail.from.email}")
	private String fromEmail;

	@Value("${mail.from.name}")
	private String senderName;

	@Autowired
	private ThymeLeafService thmymeLeafService;

	@Autowired
	private CompanyProfileDao companyProfileDao;

	@Override
	public User findByPrimaryKey(Serializable pk, FindLevel[] levels) throws UserDataAccessException {
		return userDAO.findByPrimaryKey(pk, levels);
	}

	@Override
	public Collection<User> find(List<Criteria> criterias, FindLevel[] levels) throws UserDataAccessException {
		return userDAO.find(criterias, levels);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void saveUser(User user) throws UserDataAccessException {
		try {
			if (checkUserEmail(user.getEmailId()) && checkPasswordStrength(user.getPassword())) {
				user.setUserName(user.getEmailId());
				user.getRoles().add(Role.USER_ROLE);
				if (user.getPassword() != null && !user.getPassword().trim().equals(""))
					user.setPassword(passwordEncoder.encode(user.getPassword()));
				user.initialiseCommonFields(true, 0);

				genericDAO.save(user);

				if (user.getAlias() == null) {
					GenerateRandomPassword randomPassword = new GenerateRandomPassword();
					user.setAlias(randomPassword.getPassword());
				}
				user.setUserStatus(UserStatus.UNAPPROVED);

				mailService.sendMail(user, EmailConstants.NEW_USER_SIGNUP);

			}
		} catch (UserDataAccessException e) {
			throw new UserDataAccessException(e.getMessage());
		}
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse registerUser(UserDTO user) throws UserDataAccessException {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		try {
			if (checkUserEmail(user.getEmailId()) && checkPasswordStrength(user.getPassword())) {
				Criteria criteria = new Criteria(EMAIL_ID, OPERATION_NAME.eq, user.getEmailId());
				List<Criteria> criterias = Arrays.asList(criteria);
				Collection<User> users = userDAO.find(criterias,
						new FindLevel[] { FindLevel.ROLES, FindLevel.COMPANY_PROFILE });
				if (users != null && !users.isEmpty()) {
					User exUser = users.iterator().next();
					boolean isOnlyPartnerRole = partnershipServiceHelper.isOnlyPartnerRole(exUser);
					boolean isTeamMember = teamDao.isTeamMember(exUser.getUserId());
					if (isOnlyPartnerRole || isTeamMember) {
						if (!(exUser.isEmailValidationInd())) {
							updateUserEmailValidation(exUser);
						}
						if (user.getFirstName() != null) {
							exUser.setFirstName(user.getFirstName());
						}
						if (user.getLastName() != null) {
							exUser.setLastName(user.getLastName());
						}
						if (user.getPassword() != null && !user.getPassword().trim().equals("")) {
							exUser.setPassword(passwordEncoder.encode(user.getPassword()));
						}
						if (exUser.getAlias() == null) {
							GenerateRandomPassword generateAlias = new GenerateRandomPassword();
							exUser.setAlias(generateAlias.getPassword());
						}
						exUser.setSource(UserSource.SIGNUP);
						exUser.setRegisteredTime(new Date());
						exUser.setUserDefaultPage(UserDefaultPage.WELCOME);
						exUser.setUserName(user.getEmailId().trim().toLowerCase());
						exUser.initialiseCommonFields(true, 0);
						if (isTeamMember) {
							exUser.setUserStatus(UserStatus.APPROVED);
							teamDao.apporveTeamMember(exUser.getUserId());
							userListService.shareLeadsListToNewlyAddedTeamMembers(exUser.getUserId(),
									exUser.getCompanyProfile().getId());
							response.setMessage(teamMemberSignUpSuccessMessage);
						} else {
							exUser.setUserStatus(UserStatus.UNAPPROVED);
							response.setMessage(signUpSucessMessage);
						}
						genericDAO.saveOrUpdate(exUser);
						exUser.setCompanyProfileName(user.getCompanyProfileName());
						mailService.sendActivationMail(exUser, EmailConstants.NEW_USER_SIGNUP);
						response.setStatusCode(200);
					} else {
						response.setStatusCode(400);
						response.setMessage(ACCESS_DENIED);
					}
				} else {
					response.setStatusCode(400);
					response.setMessage(ACCESS_DENIED);
				}
			} else {
				response.setStatusCode(400);
				response.setMessage(USER_EXISTING);
			}
		} catch (UserDataAccessException e) {
			throw new UserDataAccessException(e.getMessage());
		} catch (Exception e) {
			throw new UserDataAccessException(e.getMessage());
		}
		return response;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = CustomValidatonException.class)
	public void approveUser(String alias) {
		User user = utilDao.getUser(alias);
		if (user != null && UserStatus.UNAPPROVED.equals(user.getUserStatus())) {
			user.setUserStatus(UserStatus.APPROVED);
			user.setActivatedTime(new Date());
			teamService.changeTeamMemberStatus(user.getUserId());
			sendWelcomeEmail(user);
		} else if (user != null && UserStatus.APPROVED.equals(user.getUserStatus())) {
			throw new CustomValidatonException(CustomExceptionMessage.ACCOUNT_ALREADY_ACTIVATED);
		} else if (user != null && UserStatus.DECLINE.equals(user.getUserStatus())) {
			throw new CustomValidatonException(CustomExceptionMessage.INVALID_ACTIVATION_LINK);
		} else if (user == null) {
			throw new CustomValidatonException(CustomExceptionMessage.USER_DOESNOT_EXIST);
		}
	}

	@Override
	public void sendWelcomeEmail(User user) {
		if (partnershipServiceHelper.isOnlyPartnerRole(user)) {
			sendPRMPartnerWelcomeEmail(user);
		}
	}

	public void sendPartnerWelcomeEmail(User user) {
		try {
			if ("true".equals(dripEmailNotificationsEnabled)) {
				EmailTemplate emailTemplate = genericDAO.get(EmailTemplate.class, EmailConstants.PARTNER_WELCOME_ET);
				if (emailTemplate != null) {
					Set<Partnership> partnerships = user.getPartnershipsAsPartner();
					String vendorCompanyName = partnerships.iterator().next().getVendorCompany().getCompanyName();
					DripEmailHistory dripObj = mailService.sendPartnerDripEmail(user, emailTemplate,
							"PARTNER_FIRST_NAME", vendorCompanyName);
					dripObj.setActionId(DripEmailConstants.PARTNER_WELCOME_AT);
					dripObj.setUserId(user);
					dripObj.setEmailTemplateId(emailTemplate);
					dripObj.setSentTime(new Date());
					genericDAO.saveOrUpdate(dripObj);

				}
			}

		} catch (Exception e) {
			logger.error("Error in sendPartnerWelcomeEmail() : ", e);
		}
	}

	@Override
	public User loadUser(List<Criteria> criterias, FindLevel[] levels) {
		User user = null;
		Collection<User> users = userDAO.find(criterias, levels);
		if (users != null && !users.isEmpty()) {
			user = users.iterator().next();
		}
		return user;
	}

	public User loadUserByUserName(String userName, FindLevel[] levels) {
		User user = null;

		Criteria criteria = new Criteria();
		criteria.setProperty("userName");
		criteria.setValue1(userName);
		criteria.setOperationName(OPERATION_NAME.eq);

		List<Criteria> criterias = new ArrayList<Criteria>();
		criterias.add(criteria);

		Collection<User> users = userDAO.find(criterias, levels);

		if (!users.isEmpty())
			user = users.iterator().next();
		return user;
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void saveSubAdmin(User user, String subAdminRole) throws UserDataAccessException {
		saveUser(user);
		if (subAdminRole == null || subAdminRole.trim().equals("")) {
			// user.setStatus(UserStatus.UNAPPROVED.getValue());
			mailService.sendMail(user, EmailConstants.NEW_USER_SIGNUP);
		}
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = CustomValidatonException.class)
	public void forgotPassword(String modifiedEmailId, String companyProfileName) {
		User user = userDAO.getUserByEmail(modifiedEmailId);
		if (user != null && user.getUserStatus() == UserStatus.UNAPPROVED && user.getPassword() == null) {
			throw new CustomValidatonException(CustomExceptionMessage.ACCOUNT_NOT_CREATED);
		} else if (user != null && user.getUserStatus() == UserStatus.UNAPPROVED && user.getPassword() != null) {
			throw new CustomValidatonException(CustomExceptionMessage.ACCOUNT_NOT_ACTIVATED);
		} else if (user != null && user.getUserStatus() == UserStatus.APPROVED) {
			GenerateRandomPassword password = new GenerateRandomPassword();
			user.setPassword(passwordEncoder.encode(password.getPassword()));
			user.setCompanyProfileName(companyProfileName);
			mailService.sendForgotPasswordMail(user, EmailConstants.FORGOT, password.getPassword());
		} else if (user == null)
			throw new CustomValidatonException(CustomExceptionMessage.EMAIL_NOT_FOUND);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void uploadUsers(MultipartFile file, UserList userList, Integer customerId) throws UserDataAccessException {
		CSVReader reader = null;
		List<String[]> data = null;
		try {
			reader = csv.reader(file.getInputStream());
			data = reader.readAll();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// need to check this i = 0 fix
		for (int i = 0; i < data.size(); i++) {
			// String email = data.get(i)[0];
			createUserWithEmail(data.get(i), userList, customerId);
		}
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void uploadUsers(String[][] data, UserList userList, Integer userId) {
		for (int i = 1; i < data.length; i++) {
			String email = data[i][0];
			createUserWithEmail(email, userList, userId);
		}

	}

	private void createUserWithEmail(String email, UserList userList, Integer customerId) {

		if (StringUtils.isNotBlank(email)) {
			User exUuser = userDAO.getUserByEmail(email);
			if (exUuser == null) {
				User user = new User();
				user.setEmailId(email.toLowerCase());
				user.setUserStatus(UserStatus.UNAPPROVED);
				user.getUserLists().add(userList);
				userList.getUsers().add(user);
				user.getRoles().add(Role.USER_ROLE);
				user.initialiseCommonFields(true, customerId);
				genericDAO.save(user);
				// addCustomer(user, userDAO.getUser(customerId), false);
			} else {
				// addCustomer(exUuser, userDAO.getUser(customerId), false);
				exUuser.getUserLists().add(userList);
				userList.getUsers().add(exUuser);
			}
		}
	}

	private void createUserWithEmail(String[] data, UserList userList, Integer customerId)
			throws UserDataAccessException {

		if (StringUtils.isNotBlank(data[0])) {

			User exUuser = null;

			Criteria criteria = new Criteria(EMAIL_ID, OPERATION_NAME.eq, data[0]);
			List<Criteria> criterias = Arrays.asList(criteria);
			Collection<User> users = userDAO.find(criterias, new FindLevel[] { FindLevel.SHALLOW });

			if (!users.isEmpty()) {
				exUuser = users.iterator().next();
			}

			if (exUuser == null) {
				User user = new User();
				user.setEmailId(data[0].toLowerCase());
				if (data.length > 1)
					user.setFirstName(data[1] != null ? data[1] : null);
				if (data.length > 2)
					user.setLastName(data[2] != null ? data[2] : null);

				user.setUserStatus(UserStatus.UNAPPROVED);
				user.getUserLists().add(userList);
				userList.getUsers().add(user);
				user.getRoles().add(Role.USER_ROLE);
				user.initialiseCommonFields(true, customerId);
				genericDAO.saveOrUpdate(user);
				// addCustomer(user, genericDAO.get(User.class, customerId),
				// false);
			} else {
				// addCustomer(exUuser, genericDAO.get(User.class, customerId),
				// false);
				exUuser.getUserLists().add(userList);
				userList.getUsers().add(exUuser);
			}
		}
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void addCustomer(User user, User customer, boolean isOrgAdmin) throws UserDataAccessException {
		if (user.getUserId() != null)
			user = userDAO.findByPrimaryKey(user.getUserId(), new FindLevel[] { FindLevel.CUSTOMERS });
		if (isOrgAdmin && user.getOrgAdminOfSubAdmin() != null) {
			throw new UserDataAccessException("User is sub admin to some other customer.");
		}
		UserCustomer uc = null;
		/*
		 * if(isOrgAdmin){ UserOptions userOptions = new UserOptions(user.getUserId());
		 * genericDao.saveOrUpdate(userOptions); }
		 */
		for (UserCustomer userCustomer : user.getUserCustomers()) {
			if (userCustomer.getUserCustomerId().equals(new UserCustomerId(user, customer))) {
				uc = userCustomer;
				break;
			}
		}
		if (uc == null) {
			uc = new UserCustomer(user, customer, isOrgAdmin);
		}
		uc.setOrgAdmin(isOrgAdmin);
		genericDAO.saveOrUpdate(uc);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void userWithRefreshToken(User user) throws UserDataAccessException {
		genericDAO.saveOrUpdate(user);
	}

	public User loadUser(int id) throws UserDataAccessException {
		return genericDAO.get(User.class, id);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public List<User> uploadContacts(List<ExternalContactDTO> contacts, UserList userList, Integer customerId)
			throws UserDataAccessException {
		List<User> allUsers = new ArrayList<User>();
		for (ExternalContactDTO contact : contacts) {
			if (contact != null && contact.getEmail() != null && !contact.getEmail().trim().equals("")) {
				UserDTO userDTO = new UserDTO();
				userDTO.setEmailId(contact.getEmail());
				userDTO.setFirstName(contact.getFirstName());
				userDTO.setLastName(contact.getLastName());
				userDTO.setAddress(contact.getAddress());
				userDTO.setCity(contact.getCity());
				userDTO.setCountry(contact.getCountry());
				userDTO.setState(contact.getState());
				userDTO.setZipCode(contact.getPostalCode());
				userDTO.setJobTitle(contact.getTitle());
				userDTO.setMobileNumber(contact.getMobilePhone());
				userDTO.setContactCompany(contact.getCompany());
				userDTO.setLegalBasis(contact.getLegalBasis());
				userDTO.setCreateContactCompanyIfDoesNotExist(true);
				userDTO.setContactCompanySource(contact.getSource());

				User insertedUser = createUserWithEmail(userDTO, userList, customerId);
				allUsers.add(insertedUser);
			}
		}
		return allUsers;
	}

	@Override
	public User createUserWithEmail(UserDTO userDTO, UserList userList, Integer customerId)
			throws UserDataAccessException {

		User returnUser = null;
		User customer = loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, customerId)),
				new FindLevel[] { FindLevel.COMPANY_PROFILE });

		if (StringUtils.isNotBlank(userDTO.getEmailId())) {
			User exUuser = null;
			Criteria criteria = new Criteria(EMAIL_ID, OPERATION_NAME.eq, userDTO.getEmailId().toLowerCase());
			List<Criteria> criterias = Arrays.asList(criteria);
			Collection<User> users = userDAO.find(criterias, new FindLevel[] { FindLevel.ROLES });
			if (users != null && !users.isEmpty()) {
				exUuser = users.iterator().next();
			}
			try {
				boolean isGdprOn = false;
				if (userList.getModuleName().equalsIgnoreCase("SHARE LEADS")) {
					isGdprOn = gdprSettingService.isGdprEnabled(customer.getCompanyProfile().getId());
				} else {
					isGdprOn = gdprSettingService.isGdprEnabled(userList.getCompany().getId());
				}

				if (exUuser == null) {
					User user = new User();
					user.setEmailId(userDTO.getEmailId().toLowerCase());
					user.setUserName(user.getEmailId());
					user.setUserDefaultPage(UserDefaultPage.WELCOME);
					user.setUserStatus(UserStatus.UNAPPROVED);
					user.setModulesDisplayType(ModulesDisplayType.LIST);
					UserUserList userUserList = new UserUserList();
					userUserList.setUser(user);
					userUserList.setUserList(userList);
					userUserList.setFirstName(userDTO.getFirstName());
					userUserList.setLastName(userDTO.getLastName());
					userUserList.setContactCompany(userDTO.getContactCompany());
					userUserList.setJobTitle(userDTO.getJobTitle());
					userUserList.setMobileNumber(userDTO.getMobileNumber());
					userUserList.setDescription(userDTO.getDescription());
					userUserList.setAddress(userDTO.getAddress());
					userUserList.setCity(userDTO.getCity());
					userUserList.setCountry(userDTO.getCountry());
					userUserList.setState(userDTO.getState());
					userUserList.setZipCode(userDTO.getZipCode());
					userUserList.setVertical(userDTO.getVertical());
					userUserList.setRegion(userDTO.getRegion());
					userUserList.setPartnerType(userDTO.getPartnerType());
					userUserList.setCategory(userDTO.getCategory());
					setLegalBasis(userUserList, userDTO.getLegalBasis(), isGdprOn);

					/***** XNFR-671 Flexi-Fields *****/
					setFlexiFieldsToUserUserList(userDTO, userList, userUserList);
					user.getRoles().add(Role.USER_ROLE);
					if (Boolean.TRUE.equals(userList.isPartnerUserList())) {
						user.getRoles().add(Role.COMPANY_PARTNER);
						makePartnerListEmailsValid(user);
					}
					user.initialiseCommonFields(true, customerId);
					genericDAO.save(userList);
					user.getUserUserLists().add(userUserList);
					userList.getUserUserLists().add(userUserList);
					genericDAO.save(user);
					if (user.getAlias() == null) {
						GenerateRandomPassword randomPassword = new GenerateRandomPassword();
						user.setAlias(randomPassword.getPassword());
					}
					if (Boolean.TRUE.equals(userList.isPartnerUserList())) {
						addPartnerCompany(user, customer.getCompanyProfile(), null);
					}
					/*** Added On 12/12/2024 By Sravan ****/
					returnUser = user;
				} else {
					if (exUuser.getAlias() == null) {
						GenerateRandomPassword randomPassword = new GenerateRandomPassword();
						exUuser.setAlias(randomPassword.getPassword());
					}
					/* XNFR-211 */
					boolean hasUserUserList = false;
					if (userList.getId() != null && exUuser.getUserId() != null) {
						hasUserUserList = userListDAO.hasUserUserList(exUuser.getUserId(), userList.getId());
					}
					if (!hasUserUserList) {
						UserUserList userUserList = new UserUserList();
						userUserList.setUser(exUuser);
						userUserList.setUserList(userList);
						userUserList.setFirstName(userDTO.getFirstName());
						userUserList.setLastName(userDTO.getLastName());
						userUserList.setContactCompany(userDTO.getContactCompany());
						userUserList.setJobTitle(userDTO.getJobTitle());
						userUserList.setMobileNumber(userDTO.getMobileNumber());
						userUserList.setDescription(userDTO.getDescription());
						userUserList.setAddress(userDTO.getAddress());
						userUserList.setCity(userDTO.getCity());
						userUserList.setCountry(userDTO.getCountry());
						userUserList.setState(userDTO.getState());
						userUserList.setZipCode(userDTO.getZipCode());
						userUserList.setVertical(userDTO.getVertical());
						userUserList.setRegion(userDTO.getRegion());
						userUserList.setPartnerType(userDTO.getPartnerType());
						userUserList.setCategory(userDTO.getCategory());
						setLegalBasis(userUserList, userDTO.getLegalBasis(), isGdprOn);

						/***** XNFR-671 Flexi-Fields *****/
						setFlexiFieldsToUserUserList(userDTO, userList, userUserList);

						userList.getUserUserLists().add(userUserList);
						genericDAO.save(userList);
					}

					if (Boolean.TRUE.equals(userList.isPartnerUserList())) {
						/*****
						 * Add "PARTNER_ROLE&" To Their Team Members/Org Admins/Vendors For That
						 * Organization
						 ********************/
						if (exUuser.getCompanyProfile() != null) {
							List<User> listCompanyTeamMembers = getAllUsersByCompanyId(
									exUuser.getCompanyProfile().getId());
							for (User user : listCompanyTeamMembers) {
								addPartnerAssociatedRoles(user, true);
							}
						} else {
							/********
							 * This Condition is Almost Comes For Vendor/OrgAdmin/New User Who has not
							 * logged into into platform,but signedup
							 *********/
							addPartnerAssociatedRoles(exUuser, false);
						}
						addPartnerCompany(exUuser, customer.getCompanyProfile(), null);
						if (!exUuser.isEmailValidationInd())
							makePartnerListEmailsValid(exUuser);
					}

					returnUser = exUuser;
				}
			} catch (Exception e) {
			}
		}
		return returnUser;
	}

	private void setFlexiFieldsToUserUserList(UserDTO userDTO, UserList userList, UserUserList userUserList) {
		if (userList.getModuleName().equalsIgnoreCase("CONTACTS")) {
			Set<UserListFlexiField> userListFlexiFields = new HashSet<>();
			setUserListFlexiFields(userUserList, userDTO.getFlexiFields(), userListFlexiFields);
			userUserList.setUserListFlexiFields(userListFlexiFields);
		}
	}

	@Override
	public void setUserListFlexiFields(UserUserList userUserList, List<FlexiFieldRequestDTO> flexiFields,
			Set<UserListFlexiField> userListFlexiFields) {
		if (XamplifyUtils.isNotEmptyList(flexiFields)) {
			for (FlexiFieldRequestDTO flexiFieldRequestDTO : flexiFields) {
				createUserListFlexiField(userUserList, userListFlexiFields, flexiFieldRequestDTO);
			}
		}
	}

	@Override
	public void createUserListFlexiField(UserUserList userUserList, Set<UserListFlexiField> userListFlexiFields,
			FlexiFieldRequestDTO flexiFieldRequestDTO) {
		if (XamplifyUtils.isValidString(flexiFieldRequestDTO.getFieldValue())) {
			FlexiField flexiField = new FlexiField();
			flexiField.setId(flexiFieldRequestDTO.getId());

			UserListFlexiField userListFlexiField = new UserListFlexiField();
			userListFlexiField.setUserUserList(userUserList);
			userListFlexiField.setFlexiField(flexiField);
			userListFlexiField.setFlexiFieldValue(flexiFieldRequestDTO.getFieldValue());
			userListFlexiField.setCreatedTime(new Date());
			userListFlexiFields.add(userListFlexiField);
		}
	}

	private void setLegalBasis(UserUserList userUserList, List<Integer> legalBasisIds, boolean isGdprOn) {
		if (userUserList != null) {
			List<LegalBasis> legalBasisList = new ArrayList<>();
			if (isGdprOn && legalBasisIds != null && !legalBasisIds.isEmpty()) {
				for (Integer legalBasisId : legalBasisIds) {
					LegalBasis legalBasis = genericDAO.get(LegalBasis.class, legalBasisId);
					if (legalBasis != null) {
						legalBasisList.add(legalBasis);
					}
				}
			} else {
				List<LegalBasis> legalBasisDefaultList = gdprSettingService.getSelectByDefaultLegalBasis();
				for (LegalBasis legalBasis : legalBasisDefaultList) {
					if (legalBasis != null) {
						legalBasisList.add(legalBasis);
					}
				}
			}
			userUserList.setLegalBasis(legalBasisList);
		}
	}

	private void addPartnerAssociatedRoles(User exUuser, boolean hasCompany) {
		exUuser.getRoles().add(Role.COMPANY_PARTNER);
	}

	private void makePartnerListEmailsValid(User user) {
		user.setEmailValid(true);
		user.setEmailValidationInd(true);
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void addPartnerCompany(User partner, CompanyProfile company, String brandingLogoUri) {
		PartnerCompany partnerCompany = new PartnerCompany(partner, company, null);
		genericDAO.saveOrUpdate(partnerCompany);
	}

	private boolean checkUserEmail(String emailId) throws UserDataAccessException {
		Criteria criteria = new Criteria(EMAIL_ID, OPERATION_NAME.eq, emailId);
		List<Criteria> criterias = Arrays.asList(criteria);
		Collection<User> users = userDAO.find(criterias, new FindLevel[] { FindLevel.SHALLOW });
		if ((users.isEmpty() && users.size() == 0)
				|| (!(users.isEmpty()) && users.iterator().next().getUserStatus() == UserStatus.UNAPPROVED
						&& users.iterator().next().getPassword() == null))
			return true;
		else
			throw new UserDataAccessException("User is already existing with this email");
	}

	private boolean checkPasswordStrength(String password) throws UserDataAccessException {
		if (password == null || password.length() > 5)
			return true;
		else
			throw new UserDataAccessException("Password strength is low");
	}

	public boolean checkPassword(User user, String password) throws Exception {
		return passwordEncoder.matches(password, user.getPassword());
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void updatePassword(UserPassword userPassword) {
		try {
			User user = genericDAO.get(User.class, userPassword.getUserId());
			user.setPassword(passwordEncoder.encode(userPassword.getNewPassword()));
			user.setUpdatedTime(new Date());
		} catch (UserDataAccessException e) {
			logger.error("Unable to update password", e);
			throw new UserDataAccessException(e.getMessage());
		}
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void updateUser(User updatedUser) {
		try {
			User user = loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, updatedUser.getUserId())),
					new FindLevel[] { FindLevel.ROLES });
			user.setFirstName(updatedUser.getFirstName());
			user.setLastName(updatedUser.getLastName());
			user.setMiddleName(updatedUser.getMiddleName());
			user.setMobileNumber(updatedUser.getMobileNumber());
			user.setInterests(updatedUser.getInterests());
			user.setOccupation(updatedUser.getOccupation());
			user.setDescription(updatedUser.getDescription());
			user.setWebsiteUrl(updatedUser.getWebsiteUrl());
			user.initialiseCommonFields(false, user.getUserId());
			user.setUpdatedTime(new Date());
			user.setPreferredLanguage(updatedUser.getPreferredLanguage());
		} catch (UserDataAccessException e) {
			logger.error("Unable to Update User Details", e);
			throw new UserDataAccessException(e.getMessage());
		}

	}

	@Override
	public UserDTO getUserDTO(String userName, boolean isSuperAdmin) {
		UserDTO userDTO = new UserDTO();
		try {
			User user = loadUser(
					Arrays.asList(
							new Criteria(EMAIL_ID, OPERATION_NAME.eq, XamplifyUtils.replaceSpacesWithPlus(userName))),
					new FindLevel[] { FindLevel.ROLES, FindLevel.COMPANY_PROFILE });

			if (user != null) {
				user.setDateLastNav(new Date());
			}

			if (!isSuperAdmin) {
				user.setDateLastLogin(new Date());
			}
			userDTO.setId(user.getUserId());
			userDTO.setEmailId(user.getEmailId());
			userDTO.setFirstName(user.getFirstName());
			userDTO.setLastName(user.getLastName());
			userDTO.setMiddleName(user.getMiddleName());
			userDTO.setMobileNumber(user.getMobileNumber());
			userDTO.setInterests(user.getInterests());
			userDTO.setOccupation(user.getOccupation());
			userDTO.setDescription(user.getDescription());
			userDTO.setWebsiteUrl(user.getWebsiteUrl());
			String profileImage = user.getProfileImage();
			if (profileImage != null) {
				if (profileImage.startsWith("http:")) {
					String updatedProfileImage = profileImage.replace("http:", "https:");
					userDTO.setProfileImagePath(updatedProfileImage);
				} else if (profileImage.startsWith("images/")) {
					setProfileImagePath(userDTO, profileImage);
				} else {
					userDTO.setProfileImagePath(profileImage);
				}
			} else {
				setProfileImagePath(userDTO, profileImage);
			}
			userDTO.setAlias(user.getAlias());
			userDTO.setUserDefaultPage(
					user.getUserDefaultPage() == null ? "welcome" : user.getUserDefaultPage().name());

			List<Integer> roleIds = user.getRoles().stream().map(Role::getRoleId).collect(Collectors.toList());
			updateOrSetRolesForOtherUsers(userDTO, user, roleIds);
			if (user.getCompanyProfile() != null
					&& user.getCompanyProfile().getCompanyNameStatus() == CompanyNameStatus.ACTIVE) {
				if (!partnershipDAO.isNonVanityAccessEnabled(user.getCompanyProfile().getId())) {
					userDTO.setUserStatusCode(400);
				}
				userDTO.setHasCompany(true);
				userDTO.setCompanyName(user.getCompanyProfile().getCompanyName());
				userDTO.setCompanyLogo(server_path + user.getCompanyProfile().getCompanyLogoPath());
				userDTO.setWebsiteUrl(user.getCompanyProfile().getWebsite());
				userDTO.setModuleAccessDto(getAccessByCompanyId(user.getCompanyProfile().getId()));
				userDTO.setCompanyFavIconPath(user.getCompanyProfile().getFavIconLogoPath());
			}
			userDTO.setHasPassword(
					(user.getPassword() == null && user.getUserStatus().name().equals(UserStatus.APPROVED.name()))
							? false
							: true);

			setUserSourceType(userDTO, user);
			setDefaultDisplayType(userDTO, user);
			userDTO.setPreferredLanguage(user.getPreferredLanguage());
			AllAccountsView allAccountsView = new AllAccountsView();
			allAccountsView.setUserId(user.getUserId());
			userDTO.setSecondAdmin(userDAO.isSecondAdmin(allAccountsView));
			setTeamMemberPartnerFilter(userDTO, user.getUserId());

		} catch (Exception e) {
			logger.error("Unable to Find User Details", e);
			throw new UserDataAccessException(e.getMessage());
		}
		return userDTO;
	}

	private void updateOrSetRolesForOtherUsers(UserDTO userDTO, User user, List<Integer> roleIds) {
		boolean isTeamMember = teamDao.isTeamMember(user.getUserId());
		boolean isOnlyUser = roleIds.size() == 1 && roleIds.indexOf(Role.USER_ROLE.getRoleId()) > -1;
		if (isTeamMember && isOnlyUser) {
			List<Integer> teamMemberGroupRoleIds = teamMemberGroupDao
					.findTeamMemberGroupRoleIdsByTeamMemberUserId(user.getUserId());
			if (teamMemberGroupRoleIds != null && !teamMemberGroupRoleIds.isEmpty()) {
				List<Integer> roleIdsForEmail = new ArrayList<>();
				for (Integer roleId : teamMemberGroupRoleIds) {
					addMissedRoles(user, roleIds, roleId, roleIdsForEmail);
				}
			}
			userDTO.setRoles(user.getRoles());
		} else {
			userDTO.setRoles(user.getRoles());
		}
	}

	private void addMissedRoles(User user, List<Integer> roleIds, Integer roleId, List<Integer> roleIdsForEmail) {
		if (roleId != null && roleIds.indexOf(roleId) < 0) {
			addBasicModules(user, roleId);
			addAdvancedModules(user, roleId);
			roleIdsForEmail.add(roleId);
		}
	}

	private void addBasicModules(User user, Integer roleId) {
		if (roleId.equals(Role.VIDEO_UPLOAD_ROLE.getRoleId())) {
			user.getRoles().add(Role.VIDEO_UPLOAD_ROLE);
		}
		if (roleId.equals(Role.STATS_ROLE.getRoleId())) {
			user.getRoles().add(Role.STATS_ROLE);
		}
		if (roleId.equals(Role.ALL_ROLES.getRoleId())) {
			user.getRoles().add(Role.ALL_ROLES);
		}
		if (roleId.equals(Role.PARTNERS.getRoleId())) {
			user.getRoles().add(Role.PARTNERS);
		}
	}

	private void addAdvancedModules(User user, Integer roleId) {

		if (roleId.equals(Role.OPPORTUNITY.getRoleId())) {
			user.getRoles().add(Role.OPPORTUNITY);
		}
		if (roleId.equals(Role.MDF.getRoleId())) {
			user.getRoles().add(Role.MDF);
		}
		if (roleId.equals(Role.DAM.getRoleId())) {
			user.getRoles().add(Role.DAM);
		}
		if (roleId.equals(Role.LEARNING_TRACK.getRoleId())) {
			user.getRoles().add(Role.LEARNING_TRACK);
		}
		if (roleId.equals(Role.PLAY_BOOK.getRoleId())) {
			user.getRoles().add(Role.PLAY_BOOK);
		}
		if (roleId.equals(Role.SHARE_LEADS.getRoleId())) {
			user.getRoles().add(Role.SHARE_LEADS);
		}
	}

	private void setProfileImagePath(UserDTO userDTO, String profileImage) {
		if ("dev".equals(profiles)) {
			userDTO.setProfileImagePath(devImagesHost + profileImage);
		} else {
			userDTO.setProfileImagePath(server_path + profileImage);
		}
	}

	private void setUserSourceType(UserDTO userDTO, User user) {
		boolean isTeamMember = teamDao.isTeamMember(user.getUserId());
		if (isTeamMember) {
			String source = userDAO.getSuperiorSourceType(user.getUserId());
			userDTO.setSource(source);
		} else {
			if (user.getSource() != null) {
				userDTO.setSource(user.getSource().name());
			} else {
				userDTO.setSource("-");
			}
		}
	}

	private void setDefaultDisplayType(UserDTO userDTO, User user) {
		ModulesDisplayType defaultDisplayType = user.getModulesDisplayType();
		if (defaultDisplayType != null) {
			userDTO.setModulesDisplayType(defaultDisplayType);
		} else {
			userDTO.setModulesDisplayType(ModulesDisplayType.LIST);
		}
	}

	@Override
	public boolean comparePassword(String password, Integer userId) {
		try {
			User user = genericDAO.get(User.class, userId);
			return passwordEncoder.matches(password, user.getPassword());
		} catch (Exception e) {
			logger.error("Unable to Compare Password", e);
			throw new UserDataAccessException(e.getMessage());
		}
	}

	@Override
	public String uploadProfilePicture(UserDTO userProfile) {
		try {
			logger.info("Uploading Profile Picture " + userProfile.getFile().getOriginalFilename() + " For User Id"
					+ userProfile.getId());
			final String path = new SimpleDateFormat("ddMMyyyy").format(new Date());
			Integer userId = userProfile.getId();
			String imagePath = userId + sep + path + sep + "profilePicture" + sep
					+ userProfile.getFile().getOriginalFilename();
			String imagesRealPath = images_path + imagePath;
			File imagesDir = new File(imagesRealPath);

			if (!imagesDir.exists()) {
				imagesDir.mkdirs();
				logger.info("New Image Directory Created" + imagesRealPath);
			}
			userProfile.getFile().transferTo(imagesDir);
			User user = genericDAO.get(User.class, userId);
			user.setProfileImage("images" + sep + imagePath);
			if ("dev".equals(profiles)) {
				return devImagesHost + sep + user.getProfileImage();
			} else {
				return server_path + sep + user.getProfileImage();
			}
		} catch (UserDataAccessException | IllegalStateException | IOException e) {
			logger.error("Unable to upload profile picutre for" + userProfile.getId() + "", e);
			throw new UserDataAccessException(e.getCause().toString());
		} catch (Exception ex) {
			logger.error("Unable to upload profile picutre for" + userProfile.getId() + "", ex);
			throw new UserDataAccessException(ex.getCause().toString());
		}
	}

	@Override
	public List<Integer> getSubAdminUserIds(Integer userId) {
		try {
			return userDAO.getSubAdminUserIds(userId);
		} catch (UserDataAccessException e) {
			logger.error("Unable to fetch data for getting SubAdmin User Ids", e);
			throw new UserDataAccessException(e.getMessage());
		}
	}

	@Override
	public Integer getOrgAdminUserId(Integer userId) {
		try {
			return userDAO.getOrgAdminUserId(userId);
		} catch (UserDataAccessException e) {
			logger.error("Unable to fetch data for getting Org Admin For" + userId, e);
			throw new UserDataAccessException(e.getMessage());
		}
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public List<User> uploadUsers(List<User> users, UserList userList, Integer customerId)
			throws UserDataAccessException {
		List<User> allUsers = new ArrayList<User>();
		if (users != null && users.size() > 0) {
			for (User user : users) {
				UserDTO userDTO = new UserDTO();
				userDTO.setEmailId(user.getEmailId());
				userDTO.setFirstName(user.getFirstName());
				userDTO.setLastName(user.getLastName());
				userDTO.setContactCompany(user.getContactCompany());
				userDTO.setJobTitle(user.getJobTitle());
				userDTO.setMobileNumber(user.getMobileNumber());
				userDTO.setDescription(user.getDescription());
				userDTO.setAddress(user.getAddress());
				userDTO.setCity(user.getCity());
				userDTO.setCountry(user.getCountry());
				userDTO.setState(user.getState());
				userDTO.setZipCode(user.getZipCode());
				userDTO.setVertical(user.getVertical());
				userDTO.setRegion(user.getRegion());
				userDTO.setPartnerType(user.getPartnerType());
				userDTO.setCategory(user.getCategory());
				userDTO.setLegalBasis(user.getLegalBasis());
				User insertedUser = createUserWithEmail(userDTO, userList, customerId);
				allUsers.add(insertedUser);
			}
		}
		return allUsers;
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public List<User> uploadUsers(Set<UserDTO> users, UserList userList, User customer) throws UserDataAccessException {
		List<User> allUsers = new ArrayList<User>();
		if (users != null && users.size() > 0) {
			for (UserDTO userDTO : users) {
				User insertedUser = createUserWithEmail(userDTO, userList, customer.getUserId());
				allUsers.add(insertedUser);
			}
		}
		return allUsers;
	}

	@Override
	public void loadUnsubscribedUsers(List<Criteria> criterias, FindLevel[] levels) {
		userDAO.find(criterias, levels);

	}

	@Override
	public String generateUserAlias(Integer userId, long time) {
		String string = String.valueOf(time + userId);
		return string.substring(string.length() - 8, string.length());
	}

	@Override
	public void embedVideoSaveUser(UserDTO userDTO) throws UserDataAccessException {
		User exuser = loadUser(Arrays.asList(new Criteria(EMAIL_ID, OPERATION_NAME.eq, userDTO.getEmailId())),
				new FindLevel[] { FindLevel.SHALLOW });
		try {
			if (exuser == null) {
				exuser = new User();
				exuser.setFirstName(userDTO.getFirstName());
				exuser.setLastName(userDTO.getLastName());
				exuser.setEmailId(userDTO.getEmailId());
				updateUserEmailValidation(exuser);
				genericDAO.save(exuser);
			} else if (exuser != null) {
				exuser.setFirstName(userDTO.getFirstName());
				exuser.setLastName(userDTO.getLastName());
				if (!(exuser.isEmailValidationInd())) {
					updateUserEmailValidation(exuser);
				}
				genericDAO.saveOrUpdate(exuser);
			}
		} catch (Exception e) {
			logger.error(e.getMessage() + "\n");
		}
	}

	@Override
	public String getUserDefaultPage(Integer userId) {
		return userDAO.getUserDefaultPage(userId);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void setUserDefaultPage(Integer userId, String defaultPage) {
		try {
			User user = genericDAO.get(User.class, userId);
			if (UserDefaultPage.DASHBOARD.toString().equalsIgnoreCase(defaultPage))
				user.setUserDefaultPage(UserDefaultPage.DASHBOARD);
			else
				user.setUserDefaultPage(UserDefaultPage.WELCOME);
			user.setDefaultPageUpdated(true); // XNFR-560
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public boolean isGridView(Integer userId) {
		return userDAO.isGridView(userId);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void setGridView(Integer userId, boolean isGridView) {
		try {
			User user = genericDAO.get(User.class, userId);
			user.setGridView(isGridView);
			user.setUpdatedTime(new Date());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public List<String> listAllOrgAdminEmailIds() {
		try {
			return userDAO.listAllAdminEmailIds();
		} catch (HibernateException | UserDataAccessException e) {
			logger.error("Error In  listAllOrgAdminEmailIds()", e);
			throw new UserDataAccessException(e.getMessage());
		} catch (Exception ex) {
			logger.error("Error In  listAllOrgAdminEmailIds()", ex);
			throw new UserDataAccessException(ex.getMessage());
		}

	}

	@Override
	public CompanyProfile getCompanyProfileByUser(Integer userId) {
		User user = userDAO.findByPrimaryKey(userId, new FindLevel[] { FindLevel.COMPANY_PROFILE });
		return user.getCompanyProfile();
	}

	@Override
	public XtremandResponse getCompanyProfileByUserId(Integer userId) {
		try {
			CompanyProfile companyProfie = getCompanyProfileByUser(userId);
			XtremandResponse response = new XtremandResponse();
			response.setStatusCode(1000);
			if (companyProfie != null) {
				response.setData(companyProfie);
			}
			return response;
		} catch (HibernateException | UserDataAccessException e) {
			logger.error("Error In  getCompanyProfileByUserId()", e);
			throw new UserDataAccessException(e.getMessage());
		} catch (Exception ex) {
			logger.error("Error In  getCompanyProfileByUserId()", ex);
			throw new UserDataAccessException(ex.getMessage());
		}

	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse save(CompanyProfile companyProfile, Integer userId) {
		try {
			XtremandResponse response = new XtremandResponse();

			boolean isTeamMember = teamDao.isTeamMember(userId);
			TeamMember teamMember = teamDao.getTeamMemberByUserId(userId);

			if (isTeamMember && companyProfile != null && teamMember != null
					&& XamplifyUtils.isValidInteger(companyProfile.getId())
					&& teamMember.getTeamMemberStatus().name().equals(TeamMemberStatus.APPROVE.name())) {
				userId = teamDao.findPrimaryAdminIdByCompanyId(companyProfile.getId());
				logger.debug("Team member is trying to fill the company profile instead of partner at : {}",
						new Date());
			} else if (isTeamMember && companyProfile != null && teamMember != null
					&& XamplifyUtils.isValidInteger(companyProfile.getId())
					&& teamMember.getTeamMemberStatus().name().equals(TeamMemberStatus.APPROVE.name())) {
				Integer partnerCompanyExistingPrimaryAdminId = teamDao
						.findPrimaryAdminIdByCompanyId(companyProfile.getId());
				teamDao.updatePrimaryAdminId(partnerCompanyExistingPrimaryAdminId, userId);
			}

			companyProfile.setEmailDnsConfigured(false);
			String companyProfileName = companyProfile.getCompanyProfileName().trim();
			if (org.springframework.util.StringUtils.hasText(companyProfileName) && companyProfileName.length() > 15) {
				companyProfileName = companyProfileName.substring(0, 14);
				companyProfile.setCompanyProfileName(companyProfileName);
			}
			/*** XBI-2064 ***/
			String companyName = companyProfile.getCompanyName().trim();
			companyProfile.setCompanyName(companyName);
			/** XNFR-982 **/
			/*** XBI-2064 **/
			companyProfile.setCompanyNameStatus(CompanyNameStatus.ACTIVE);
			if (companyProfile.getId() != null && companyProfile.getId() != 0) {
				genericDAO.update(companyProfile);
			} else {
				genericDAO.save(companyProfile);
			}
			User user = userDAO.findByPrimaryKey(userId,
					new FindLevel[] { FindLevel.ROLES, FindLevel.COMPANY_PROFILE });
			user.setCompanyProfile(companyProfile);
			/****** XNFR-83 ************/

			addDefaultVideoPlayerSettings(companyProfile);
			setPartnerData(companyProfile, user);
			/*** XNFR-428 ***/
			createDefaultFolder(companyProfile, userId);
			ModuleAccess moduleAccess = new ModuleAccess();
			boolean isPRM = user.getRoles().stream()
					.anyMatch(role -> (role.getRoleId()).equals(Role.PRM_ROLE.getRoleId()));
			if (isPRM) {
				moduleAccess.setEnableLeads(true);
				moduleAccess.setForm(true);
				moduleAccess.setLoginAsTeamMember(true);
				moduleAccess.setMdf(true);
				moduleAccess.setDam(true);
				moduleAccess.setShareLeads(true);
				moduleAccess.setLms(true);
				moduleAccess.setPlaybooks(true);
				moduleAccess.setExcludeUsersOrDomains(true);
				moduleAccess.setCustomSkinSettings(true);
				moduleAccess.setLoginAsPartner(true);
				moduleAccess.setShareWhiteLabeledContent(true);
				moduleAccess.setApprovalHub(true);
				moduleAccess.setAllowVendorToChangePartnerPrimaryAdmin(true);
				moduleAccess.setMailsEnabled(true);

				ModuleAccessDTO moduleAccessDTO = new ModuleAccessDTO();
				moduleAccessDTO.setLeads(true);
				moduleAccessDTO.setMdf(true);
				moduleAccessDTO.setDam(true);
				moduleAccessDTO.setLms(true);
				moduleAccessDTO.setPlaybooks(true);

				createDefaultPartnerList(user);
				dealService.createDefaultDealPipeline(companyProfile, user.getUserId());
				dealService.createDefaultLeadPipeline(companyProfile, user.getUserId());
				teamMemberGroupService.addDefaultGroups(Role.PRM_ROLE.getRoleId(), companyProfile.getId(),
						moduleAccessDTO);
				utilService.addDefaultDashboardBanners(userId, companyProfile, Role.PRM_ROLE.getRoleId(), false);
				utilService.insertDefaultCustomSkins(companyProfile, userId);
				utilService.insertDefaultThemes(companyProfile, userId);

			}
			moduleAccess.setDashboardType(DashboardTypeEnum.DASHBOARD);
			moduleAccess.setCompanyProfile(companyProfile);
			moduleAccess.setMaxAdmins(5);
			genericDAO.save(moduleAccess);

			unsubscribeService.addDefaultReasonsAndHeaderAndTextByCompanyId(companyProfile.getId());
			addPartnerManagerDefaultGroup(userId, companyProfile.getId());
			/*** XNFR-84 ****/
			utilService.addCustomModuleNamesByCompanyId(companyProfile.getId(), userId);
			/*** XNFR-454 ***/
			List<String> domains = new ArrayList<>();
			String domain = XamplifyUtils.getEmailDomain(user.getEmailId().trim().toLowerCase());
			domains.add(domain);
			List<String> domainNames = domainDao.findAllDomainNames(companyProfile.getId(),
					DomainModuleNameType.TEAM_MEMBER);
			if (domainNames.indexOf(domain) < 0) {
				utilService.saveDomain(user.getUserId(), companyProfile, domains, DomainModuleNameType.TEAM_MEMBER,
						false);
			}
			response.setStatusCode(StatusCodeConstants.COMPANY_PROFILE_UPDATED);
			response.setMessage("Company Profile Info Added Successfully");
			return response;
		} catch (HibernateException | UserDataAccessException e) {
			logger.error("Error In  save()", e);
			throw new UserDataAccessException(e.getMessage());
		} catch (Exception ex) {
			logger.error("Error In  save()", ex);
			throw new UserDataAccessException(ex.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	public Integer addPartnerManagerDefaultGroup(Integer userId, Integer companyId) {
		Integer teamMemberGroupId = null;
		VanityUrlDetailsDTO vanityUrlDetailsDTO = new VanityUrlDetailsDTO();
		List<TeamMemberGroup> teamMemberGroups = new ArrayList<>();
		vanityUrlDetailsDTO.setUserId(userId);
		XtremandResponse teamMemberGroupResponse = teamMemberGroupService.findDefaultModules(vanityUrlDetailsDTO);
		teamMemberGroups.addAll(teamMemberGroupDao.findGroupIdsAndNamesByCompanyId(companyId));
		Map<String, Object> map = (Map<String, Object>) teamMemberGroupResponse.getData();
		List<TeamMemberModuleDTO> modules = (List<TeamMemberModuleDTO>) map.get("modules");
		if (modules != null && !modules.isEmpty() && teamMemberGroups.isEmpty()) {
			Set<Integer> moduleIds = modules.stream().map(TeamMemberModuleDTO::getRoleId).collect(Collectors.toSet());
			TeamMemberGroup teamMemberGroup = new TeamMemberGroup();
			teamMemberGroup.setName("Partner Account Manager");
			teamMemberGroup.setCreatedUserId(1);
			teamMemberGroup.setUpdatedUserId(1);
			teamMemberGroup.setCompanyId(companyId);
			teamMemberGroup.setCreatedTime(new Date());
			teamMemberGroup.setUpdatedTime(new Date());
			teamMemberGroup.setDefaultGroup(true);
			GenerateRandomPassword password = new GenerateRandomPassword();
			teamMemberGroup.setAlias(password.getPassword());
			Set<TeamMemberGroupRoleMapping> teamMemberGroupRoleMappings = new HashSet<>();
			for (Integer moduleId : moduleIds) {
				boolean partners = moduleId != null && moduleId.equals(Role.PARTNERS.getRoleId());
				boolean stats = moduleId != null && moduleId.equals(Role.STATS_ROLE.getRoleId());
				if (!partners && !stats) {
					TeamMemberGroupRoleMapping teamMemberGroupRoleMapping = new TeamMemberGroupRoleMapping();
					teamMemberGroupRoleMapping.setTeamMemberGroup(teamMemberGroup);
					teamMemberGroupRoleMapping.setRoleId(moduleId);
					teamMemberGroupRoleMapping.setCreatedTime(new Date());
					teamMemberGroupRoleMappings.add(teamMemberGroupRoleMapping);
				}
			}
			teamMemberGroup.setTeamMemberGroupRoleMappings(teamMemberGroupRoleMappings);
			genericDAO.save(teamMemberGroup);
			teamMemberGroupId = teamMemberGroup.getId();
			logger.debug("Partner Account Manager has been added successfully");

		}
		return teamMemberGroupId;
	}

	private Integer createDefaultFolder(CompanyProfile companyProfile, Integer userId) {
		Integer count = categoryDao.isDefaultFolderExistsByCompanyId(companyProfile.getId());
		Integer categoryId = null;
		boolean isDefaultFolderExists = (count != null) && (count > 0) ? true : false;
		if (!isDefaultFolderExists) {
			Category defaultCategory = new Category();
			String defaultCategoryName = companyProfile.getCompanyProfileName() + defaultFolderSuffix;
			defaultCategory.setName(defaultCategoryName);
			defaultCategory.setDescription(defaultCategoryName);
			defaultCategory.setCompanyId(companyProfile.getId());
			defaultCategory.setIcon(faIcon);
			defaultCategory.setDefaultCategory(true);
			defaultCategory.setCreatedTime(new Date());
			defaultCategory.setCreatedUserId(userId);
			categoryDao.save(defaultCategory);
			String debugMessage = "Default Folder is created for " + companyProfile.getId() + " with id "
					+ defaultCategory.getId();
			logger.debug(debugMessage);
			categoryId = defaultCategory.getId();
		} else {
			String debugMessage = "Default Folder Already Exists For " + companyProfile.getId();
			logger.debug(debugMessage);
			categoryId = categoryDao.getDefaultCategoryIdByCompanyId(companyProfile.getId());
		}
		return categoryId;
	}

	/*** XNFR-2 ************/
	private void createDefaultPartnerList(User user) {
		List<Integer> roleIds = user.getRoles().stream().map(Role::getRoleId).collect(Collectors.toList());
		boolean isPrmRole = roleIds.contains(Role.PRM_ROLE.getRoleId());
		if (isPrmRole) {
			userListService.CreateOrGetDefaultPartnerList(user);
		}
	}

	@Override
	public boolean isOnlyUser(User loggedInUser) {
		boolean isOnlyUser = false;
		if (loggedInUser != null) {
			List<Integer> roleIdsList = loggedInUser.getRoles().stream().map((role) -> (role.getRoleId()))
					.collect(Collectors.toList());
			if (roleIdsList != null && roleIdsList.size() == 1) {
				isOnlyUser = roleIdsList.contains(3);
			}
		}
		return isOnlyUser;
	}

	/**
	 * @param companyProfile
	 * @param user
	 * @throws VideoDataAccessException
	 */
	private void setPartnerData(CompanyProfile companyProfile, User user) throws VideoDataAccessException {
		if (user.getRoles().stream().anyMatch((role) -> (role.getRoleId()) == Role.COMPANY_PARTNER.getRoleId())) {
			VideoDefaultSettings videoDefaultSettings = new VideoDefaultSettings();
			videoDefaultSettings.setCompanyProfile(companyProfile);
			videoService.updateVideoDefaultSettings(companyProfile, videoDefaultSettings);
		}
	}

	/**
	 * @param companyProfile
	 * @param user
	 * @throws VideoDataAccessException
	 */
	private void addDefaultVideoPlayerSettings(CompanyProfile companyProfile) throws VideoDataAccessException {
		videoService.updateVideoDefaultSettings(companyProfile, null);
	}

	@Override
	public XtremandResponse listAllCompanyNames() {
		try {
			XtremandResponse response = new XtremandResponse();
			response.setStatusCode(StatusCodeConstants.COMPANY_NAMES_FOUND);
			response.setData(userDAO.listAllCompanyNames());
			return response;
		} catch (HibernateException | UserDataAccessException e) {
			logger.error("Error In listAllCompanies()", e);
			throw new UserDataAccessException(e.getMessage());
		} catch (Exception ex) {
			logger.error("Error In listAllCompanies()", ex);
			throw new UserDataAccessException(ex.getMessage());
		}
	}

	@Override
	public List<Integer> getCompanyUserIds(Integer userId) {
		return userDAO.getCompanyUserIds(userId);
	}

	@Override
	public List<Integer> getCompanyUsers(Integer companyId) {
		return userDAO.getCompanyUsers(companyId);
	}

	@Override
	public XtremandResponse listAllCompanyProfileNames() {
		try {
			XtremandResponse response = new XtremandResponse();
			response.setStatusCode(StatusCodeConstants.COMPANY_NAME_PROFILES_FOUND);
			response.setData(userDAO.listAllCompanyProfileNames());
			return response;
		} catch (HibernateException | UserDataAccessException e) {
			logger.error("Error In listAllCompanyProfileNames()", e);
			throw new UserDataAccessException(e.getMessage());
		} catch (Exception ex) {
			logger.error("Error In listAllCompanyProfileNames()", ex);
			throw new UserDataAccessException(ex.getMessage());
		}
	}

	@Override
	public XtremandResponse update(CompanyProfile companyProfile, Integer userId) {
		try {
			User user = userDAO.findByPrimaryKey(userId, new FindLevel[] { FindLevel.SHALLOW });
			user.setUpdatedTime(new Date());
			XtremandResponse response = new XtremandResponse();
			/*** XBI-2064 ***/
			String companyName = companyProfile.getCompanyName().trim();
			companyProfile.setCompanyName(companyName);
			/*** XBI-2064 **/
			genericDAO.update(companyProfile);
			response.setMessage("Company Profile Info Updated Successfully");
			response.setStatusCode(StatusCodeConstants.COMPANY_PROFILE_UPDATED);
			return response;
		} catch (HibernateException | UserDataAccessException e) {
			logger.error("Error In update(" + companyProfile.toString() + ")", "userid:" + userId, e);
			throw new UserDataAccessException(e.getMessage());
		} catch (Exception ex) {
			logger.error("Error In update(" + companyProfile.toString() + ")", "userid:" + userId, ex);
			throw new UserDataAccessException(ex.getMessage());
		}
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public Map<String, Object> logSaveCallToActionUser(EmailLogReport emailLogReport, Integer videoId)
			throws Exception {
		User user = new User();
		try {
			BeanUtils.copyProperties(user, emailLogReport);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		Map<String, Object> resultMap = new HashMap<String, Object>();
		User exUser = null;
		List<User> usersList = new ArrayList<User>();
		usersList.add(user);
		Criteria criteria = new Criteria(EMAIL_ID, OPERATION_NAME.eq, user.getEmailId());
		List<Criteria> criterias = Arrays.asList(criteria);
		exUser = loadUser(criterias, new FindLevel[] { FindLevel.SHALLOW });
		if (exUser != null) {
			resultMap = userListValidator.validateEmailIds(usersList);
			if (resultMap.get("statusCode").equals(200)) {
				if (!(exUser.isEmailValidationInd())) {
					updateUserEmailValidation(exUser);
				}
			} else {
				return showValidationMessage(resultMap);
			}
		}
		if (exUser == null) {
			resultMap = userListValidator.validateEmailIds(usersList);
			if (resultMap.get("statusCode").equals(200)) {
				exUser = new User();
				exUser.setEmailId(user.getEmailId());
				exUser.getRoles().add(Role.USER_ROLE);
				updateUserEmailValidation(exUser);
				exUser.initialiseCommonFields(true, 0);
				exUser.setUpdatedTime(new Date());
				exUser.setUserStatus(UserStatus.UNAPPROVED);
				genericDAO.save(exUser);
				GenerateRandomPassword randomPassword = new GenerateRandomPassword();
				exUser.setAlias(randomPassword.getPassword());
			} else {
				return showValidationMessage(resultMap);
			}
		}
		logVideoLeads(exUser, videoId, user.getFirstName(), user.getLastName(), emailLogReport.getSessionId());
		resultMap.put("statusCode", 200);
		resultMap.put("id", exUser.getUserId());
		return resultMap;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> showValidationMessage(Map<String, Object> resultMap) {
		String invalidEmailId = ((List<String>) resultMap.get("emailAddresses")).get(0);
		resultMap.put("errorMessage", "The entered email id is invalid : " + invalidEmailId);
		return resultMap;

	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	private void logVideoLeads(User user, Integer videoId, String firstName, String lastName, String sessionId) {
		VideoFile videoFile = genericDAO.get(VideoFile.class, videoId);
		VideoLead videoLead = new VideoLead();
		videoLead.setVideoFile(videoFile);
		videoLead.setUser(user);
		videoLead.setDate(new Date());
		videoLead.setFirstName(firstName);
		videoLead.setLastName(lastName);
		videoLead.setSessionId(sessionId);
		genericDAO.save(videoLead);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xtremand.user.service.UserService#getFirstOrgAdminId(java.lang.
	 * Integer)
	 */
	@Override
	public Integer getFirstOrgAdminId(Integer userId) {
		return userDAO.getFirstAdminId(userId);
	}

	@Override
	public Integer getOrgAdminCountByUserId(Integer userId) {
		try {
			Integer count = userDAO.getOrgAdminsCountByUserId(userId);
			if (count != null) {
				return count;
			} else {
				return 0;
			}

		} catch (UserDataAccessException | HibernateException e) {
			logger.error("Error In getOrgAdminCountByUserId(" + userId + ")", e);
			throw new UserDataAccessException(e.getMessage());

		} catch (Exception ex) {
			logger.error("Error In getOrgAdminCountByUserId(" + userId + ")", ex);
			throw new UserDataAccessException(ex.getMessage());

		}
	}

	@Override
	public List<String> listAllPartnerEmailIds() {
		try {
			return userDAO.listAllPartnerEmailIds();
		} catch (HibernateException | UserDataAccessException e) {
			logger.error("Error In  listAllPartnerEmailIds()", e);
			throw new UserDataAccessException(e.getMessage());
		} catch (Exception ex) {
			logger.error("Error In  listAllPartnerEmailIds()", ex);
			throw new UserDataAccessException(ex.getMessage());
		}

	}

	@Override
	public List<String> getNonExistingUsers(List<String> emailIds) {
		List<String> nonExistingUsers = new ArrayList<String>();
		if (emailIds != null && !emailIds.isEmpty()) {
			emailIds.removeAll(Collections.singleton(null));
			for (String emailId : emailIds) {
				if (emailId != null && !"".equals(emailId.trim())) {
					User exUser = loadUser(
							Arrays.asList(new Criteria(EMAIL_ID, OPERATION_NAME.eq, emailId.toLowerCase())),
							new FindLevel[] { FindLevel.SHALLOW });
					if (exUser == null) {
						nonExistingUsers.add(emailId.toLowerCase());
					} else if (exUser != null && exUser.isEmailValidationInd() == false) {
						nonExistingUsers.add(emailId.toLowerCase());
					}
				}
			}
		}
		return nonExistingUsers;
	}

	@Override
	public String getCompanyLogoPath(MultipartFile imageFile, Integer userId) {
		return returnImagePath(imageFile, userId, "company-profile-logo");
	}

	@Override
	public String getCompanyBackGroundImagePath(MultipartFile imageFile, Integer userId) {
		return returnImagePath(imageFile, userId, "company-background-logo");
	}

	private String returnImagePath(MultipartFile imageFile, Integer userId, String type) {
		try {
			final String path = new SimpleDateFormat("ddMMyyyy").format(new Date());
			String companyLogoPath = null;
			if (imageFile != null && imageFile.getOriginalFilename() != null
					&& !imageFile.getOriginalFilename().trim().isEmpty()) {
				String imagePath = "";
				imagePath = images_path + userId + sep + path + sep + type + sep;
				File imageDir = new File(imagePath);
				if (!imageDir.exists()) {
					imageDir.mkdirs();
				}
				String imageFilePath = imagePath + sep + imageFile.getOriginalFilename().replaceAll(regex, "");
				File newImageFile = new File(imageFilePath);

				if (!newImageFile.exists()) {
					FileOutputStream fileOutputStream = new FileOutputStream(newImageFile);
					fileOutputStream.write(imageFile.getBytes());
					fileOutputStream.flush();
					fileOutputStream.close();
				}

				/*
				 * BufferedImage bufferedImage = XtremandUtils.toBufferedImg(imageFile);
				 * ImageIO.write(bufferedImage, "png", newImageFile);
				 */
				companyLogoPath = "images" + sep + userId + sep + path + sep + type + sep
						+ imageFile.getOriginalFilename().replaceAll(regex, "");

			}
			return companyLogoPath;
		} catch (HibernateException | UserDataAccessException e) {
			logger.error("Error In  returnImagePath()", e);
			throw new UserDataAccessException(e.getMessage());
		} catch (Exception ex) {
			logger.error("Error In  returnImagePath()", ex);
			throw new UserDataAccessException(ex.getMessage());
		}

	}

	@Override
	public List<UserDTO> listTeamMembers(Integer userId) {
		try {
			List<UserDTO> userDTOs = new ArrayList<>();
			List<Object[]> teamMembers = userDAO.listAllTeamMembers(userId);
			setData(userDTOs, teamMembers);
			return userDTOs;
		} catch (HibernateException | UserDataAccessException e) {
			logger.error("Error In listAllTeamMembers(" + userId + ")", e);
			throw new UserDataAccessException(e.getMessage());
		} catch (Exception ex) {
			logger.error("Error In listAllTeamMembers(" + userId + ")", ex);
			throw new UserDataAccessException(ex.getMessage());
		}

	}

	@Override
	public List<UserDTO> findAllAdminsAndSupervisors(Integer userId) {
		try {
			List<UserDTO> userDTOs = new ArrayList<>();
			List<Object[]> teamMembers = userDAO.findAllAdminsAndSuperVisors(userId);
			setData(userDTOs, teamMembers);
			return userDTOs;
		} catch (HibernateException | UserDataAccessException e) {
			logger.error("Error In findAllAdminsAndSupervisors(" + userId + ")", e);
			throw new UserDataAccessException(e.getMessage());
		} catch (Exception ex) {
			logger.error("Error In findAllAdminsAndSupervisors(" + userId + ")", ex);
			throw new UserDataAccessException(ex.getMessage());
		}

	}

	/**
	 * @param userDTOs
	 * @param teamMembers
	 */
	private void setData(List<UserDTO> userDTOs, List<Object[]> teamMembers) {
		try {
			for (Object[] result : teamMembers) {
				UserDTO userDTO = new UserDTO();
				userDTO.setEmailId(String.valueOf(result[0]));
				userDTO.setFirstName(String.valueOf(result[1] == null ? "" : result[1]));
				userDTO.setLastName(String.valueOf(result[2] == null ? "" : result[2]));
				userDTO.setId((Integer) result[3]);
				userDTOs.add(userDTO);
			}
		} catch (HibernateException | UserDataAccessException e) {
			logger.error("Error In setData()", e);
			throw new UserDataAccessException(e.getMessage());
		} catch (Exception ex) {
			logger.error("Error In setData()", ex);
			throw new UserDataAccessException(ex.getMessage());
		}
	}

	@Override
	public void resendActivationEmail(@RequestParam String emailId) {
		User exUser = loadUser(Arrays.asList(new Criteria(EMAIL_ID, OPERATION_NAME.eq, emailId)),
				new FindLevel[] { FindLevel.SHALLOW });
		mailService.sendMail(exUser, EmailConstants.NEW_USER_SIGNUP);
	}

	@Override
	public UserDTO getSignUpDetails(String alias) {
		UserDTO userDTO = new UserDTO();
		if (alias.indexOf("-") > 0) {
			String[] arrOfStr = alias.split("-", 2);
			Object[] result = userDAO.getSignUpDetails(arrOfStr[1], Integer.parseInt(arrOfStr[0]));
			if (result != null) {
				userDTO.setEmailId((String) result[0]);
				userDTO.setFirstName((String) result[1]);
				userDTO.setLastName((String) result[2]);
			}
		} else {
			User exUser = utilDao.getUser(alias);
			if (exUser != null) {
				userDTO.setEmailId(exUser.getEmailId());
				userDTO.setFirstName(exUser.getFirstName());
				userDTO.setLastName(exUser.getLastName());
			}
		}
		return userDTO;
	}

	@Override
	public Integer getCompanyIdByUserId(Integer userId) {
		Integer companyId = userDAO.getCompanyIdByUserId(userId);
		return companyId;
	}

	@Override
	public UserDTO getUser(Integer customerId) {
		UserDTO userDTO = new UserDTO();
		User user = userDAO.getUser(customerId);
		userDTO.setId(user.getUserId());
		userDTO.setEmailId(user.getEmailId());
		userDTO.setFirstName(user.getFirstName());
		userDTO.setLastName(user.getLastName());
		userDTO.setMobileNumber(user.getMobileNumber());
		userDTO.setCity(user.getCity());
		userDTO.setCountry(user.getCountry());
		userDTO.setZipCode(user.getZipCode());
		userDTO.setInterests(user.getInterests());
		userDTO.setAddress(user.getAddress());
		userDTO.setState(user.getState());
		userDTO.setOccupation(user.getOccupation());
		userDTO.setDescription(user.getDescription());
		userDTO.setWebsiteUrl(user.getWebsiteUrl());
		userDTO.setProfileImagePath(user.getProfileImage());
		if (user.getCompanyProfile() != null) {
			userDTO.setHasCompany(true);
			userDTO.setCompanyName(user.getCompanyProfile().getCompanyName());
			userDTO.setCompanyLogo(server_path + user.getCompanyProfile().getCompanyLogoPath());
			userDTO.setWebsiteUrl(user.getCompanyProfile().getWebsite());
		}
		return userDTO;
	}

	public List<String> listAllCompanyProfileImages(Integer userId) {
		List<Integer> userIds = userDAO.listAllUserIdsByLoggedInUserId(userId);
		List<String> paths = iterateFolderAndGetCompanyProfileImages(userIds);
		return paths;
	}

	private List<String> iterateFolderAndGetCompanyProfileImages(List<Integer> userIds) {
		List<String> paths = new ArrayList<>();
		for (Integer organizationUserId : userIds) {
			File currentDir = new File(mediaBasePath + sep + "images" + sep + organizationUserId); // current
			displayDirectoryContents(currentDir, paths);
		}
		return paths;
	}

	private void displayDirectoryContents(File dir, List<String> paths) {
		try {
			File[] files = dir.listFiles();
			if (files != null) {
				for (File file : files) {
					if (file.isDirectory()) {
						if (file.getCanonicalPath().endsWith("company-profile-logo")) {
							File[] companyProfiles = file.listFiles();
							if (companyProfiles != null) {
								for (File companyProfile : companyProfiles) {
									String filePath = companyProfile.getPath();
									String updatedFilePath = "";
									if ("dev".equals(profiles)) {
										updatedFilePath = filePath.replaceAll("\\\\", sep).replaceAll(mediaBasePath,
												"");
									} else {
										updatedFilePath = filePath.replaceAll(mediaBasePath, "");
									}
									paths.add(server_path + updatedFilePath);
								}
							}
						}
						displayDirectoryContents(file, paths);
					}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public XtremandResponse getUserOppertunityModule(Integer userId) {
		XtremandResponse response = new XtremandResponse();
		Boolean enable_leads = userDAO.getUserOppertunityModule(userId);
		response.setData(enable_leads);
		return response;
	}

	@Override
	public ModuleAccessDTO getAccessByCompanyId(Integer companyId) {
		ModuleAccessDTO dto = new ModuleAccessDTO();
		ModuleAccess moduleAccess = userDAO.getAccessByCompanyId(companyId);
		if (moduleAccess != null) {
			org.springframework.beans.BeanUtils.copyProperties(moduleAccess, dto);
			dto.setLeads(moduleAccess.isEnableLeads());
			dto.setFormBuilder(moduleAccess.isForm());
			dto.setCompanyId(companyId);

		}
		return dto;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse updateModulesAccess(ModuleAccessDTO moduleAccessDto) {
		XtremandResponse response = new XtremandResponse();
		try {
			if (moduleAccessDto != null && moduleAccessDto.getCompanyId() != null) {
				ModuleAccess moduleAccess = userDAO.getAccessByCompanyId(moduleAccessDto.getCompanyId());
				if (moduleAccess != null) {
					org.springframework.beans.BeanUtils.copyProperties(moduleAccessDto, moduleAccess);
					moduleAccess.setEnableLeads(moduleAccessDto.isLeads());
					moduleAccess.setForm(moduleAccessDto.isFormBuilder());
					userDAO.updateAccess(moduleAccess);
					userDAO.updateSource(moduleAccessDto);
					/******** Change The Role ************/
					updateRole(moduleAccessDto);
					response.setStatusCode(200);
				}
			}
		} catch (Exception e) {
			logger.error("Error while updating Module Access : " + e.getMessage());
		}
		return response;
	}

	private void updateRole(ModuleAccessDTO moduleAccessDto) {
		Integer userId = moduleAccessDto.getUserId();
		Integer roleId = moduleAccessDto.getRoleId();
		List<Integer> roleIds = userDAO.getRoleIdsByUserId(userId);
		if (roleIds.indexOf(roleId) < 0) {
			boolean isPrmRole = roleIds.indexOf(Role.PRM_ROLE.getRoleId()) > -1;
			if (isPrmRole) {
				userDAO.deleteByRoleId(Role.PRM_ROLE.getRoleId(), userId);
			}
			/********* Update the role **************/
			userDAO.insertRole(roleId, userId);
			/**** Update roles for team members who is having all role **********/
		}
	}

	@Override
	public XtremandResponse getSMSServiceModule(Integer userId) {
		XtremandResponse response = new XtremandResponse();
		Boolean sms_service = userDAO.getSMSServiceModule(userId);
		response.setData(sms_service);
		return response;
	}

	private void updateUserEmailValidation(User exUser) throws ParseException, IOException {
		JSONObject jsonObject = emailValidatorService.validate(exUser.getEmailId(), null, "", 1, 1);
		if (jsonObject.has("error") && jsonObject.getString("error")
				.equalsIgnoreCase("Invalid API Key or your account ran out of credits")) {
			exUser.setEmailValid(false);
			exUser.setEmailValidationInd(false);
		} else if (jsonObject.has("error")) {
			exUser.setEmailValid(false);
			exUser.setEmailValidationInd(true);
			exUser.setEmailCategory("invalid");
		} else {
			String status = jsonObject.getString("status");
			exUser.setEmailValid(
					(status.equalsIgnoreCase("valid") || status.equalsIgnoreCase("catch-all")) ? true : false);
			exUser.setEmailValidationInd(true);
			exUser.setEmailCategory(status);
		}
	}

	@Override
	public List<UserDTO> listPartnerAndHisTeamMembers(Integer userId) {
		try {
			List<UserDTO> userDTOs = new ArrayList<>();
			List<Object[]> teamMembers = userDAO.listPartnerAndHisTeamMembers(userId);
			setData(userDTOs, teamMembers);
			return userDTOs;
		} catch (HibernateException | UserDataAccessException e) {
			logger.error("Error In listPartnerAndHisTeamMembers(" + userId + ")", e);
			throw new UserDataAccessException(e.getMessage());
		} catch (Exception ex) {
			logger.error("Error In listPartnerAndHisTeamMembers(" + userId + ")", ex);
			throw new UserDataAccessException(ex.getMessage());
		}

	}

	@Override
	public RoleDTO getSuperiorRole(Integer teamMemberId) {
		try {
			RoleDTO roleDto = new RoleDTO();
			Integer id = teamDao.getOrgAdminIdByTeamMemberId(teamMemberId);
			if (id != null) {
				User user = userDAO.findByPrimaryKey(id, new FindLevel[] { FindLevel.ROLES });
				List<String> roleNames = user.getRoles().stream().map(Role::getRoleName).collect(Collectors.toList());
				roleDto.setRoles(roleNames);
				roleDto.setTotalRoles(roleNames.size());
				boolean isOnlyPartner = Role.isOnlyPartnerCompanyByRoleNames(roleNames);
				roleDto.setOnlyPartner(isOnlyPartner);
				roleDto.setSuperiorId(user.getUserId());
			}
			return roleDto;
		} catch (HibernateException | UserDataAccessException e) {
			logger.error("Error In getSuperiorRole(" + teamMemberId + ")", e);
			throw new UserDataAccessException(e.getMessage());
		} catch (Exception ex) {
			logger.error("Error In getSuperiorRole(" + teamMemberId + ")", ex);
			throw new UserDataAccessException(ex.getMessage());
		}

	}

	@Override
	public List<UserDTO> getCompanyIdsByEmailIds(List<String> emailIds) {
		if (emailIds != null && !emailIds.isEmpty()) {
			List<String> result = emailIds.stream().map(String::toLowerCase).collect(Collectors.toList());
			List<Object[]> list = userDAO.getCompanyIdsByEmailIds(result);
			List<UserDTO> userDtos = new ArrayList<>();
			for (Object[] user : list) {
				Integer companyId = (Integer) user[0];
				String emailId = (String) user[1];
				UserDTO userDto = new UserDTO();
				userDto.setEmailId(emailId);
				userDto.setCompanyId(companyId);
				userDtos.add(userDto);
			}
			return userDtos;
		} else {
			return new ArrayList<>();
		}
	}

	@Override
	public List<User> getAllUsersByCompanyId(Integer companyId) {
		return userDAO.getAllUsersByCompanyId(companyId);
	}

	private void setResponse(XtremandResponse response, Integer code, String message) {
		response.setStatusCode(code);
		response.setMessage(message);
	}

	@Override
	public XtremandResponse getRolesByUserId(Integer userId) {
		XtremandResponse response = new XtremandResponse();
		List<Integer> roles = userDAO.getRoleIdsByUserId(userId);
		if (roles.isEmpty()) {
			setResponse(response, 404, "No Data found");
		} else {
			RoleDTO roleDto = new RoleDTO();
			Integer companyPartner = Role.COMPANY_PARTNER.getRoleId();
			Integer prm = Role.PRM_ROLE.getRoleId();
			utilDao.addPartnerRoleToSecondAdmins(userId, roles);
			if (roles.indexOf(prm) > -1 && roles.indexOf(companyPartner) < 0) {
				roleDto.setRole("Prm");
			} else if (roles.indexOf(prm) > -1 && roles.indexOf(companyPartner) > -1) {
				roleDto.setRole("Prm & Partner");
			} else if (roles.indexOf(companyPartner) > -1 && roles.indexOf(prm) < 0) {
				roleDto.setRole("Partner");
			} else {
				setTeamMemberRole(userId, roleDto, "User", prm);
			}
			response.setData(roleDto);
			setResponse(response, 200, null);
		}
		return response;
	}

	private void setTeamMemberRole(Integer userId, RoleDTO roleDto, String role, Integer prm) {
		boolean isTeamMember = teamDao.isTeamMember(userId);
		if (isTeamMember) {
			Integer superiorId = teamDao.getOrgAdminIdByTeamMemberId(userId);
			List<Integer> roles = userDAO.getRoleIdsByUserId(superiorId);
			roleDto.setSuperiorRole(getSuperiorRole(roles));

			if (!roles.isEmpty()) {
				if (roles.indexOf(prm) < 0) {
					roleDto.setPartnerTeamMember(true);
				}
				roleDto.setRole("Team Member");
			}
		} else {
			roleDto.setRole(role);
		}
	}

	private String getSuperiorRole(List<Integer> roles) {
		String roleString = "";
		Integer companyPartner = Role.COMPANY_PARTNER.getRoleId();
		Integer prm = Role.PRM_ROLE.getRoleId();
		if (roles.indexOf(prm) > -1 && roles.indexOf(companyPartner) < 0) {
			roleString = "Prm";
		} else if (roles.indexOf(prm) > -1 && roles.indexOf(companyPartner) > -1) {
			roleString = "Prm & Partner";
		}
		/******** Only Partner ********/
		else if (roles.indexOf(companyPartner) > -1 && roles.indexOf(prm) < 0) {
			roleString = "Partner";
		}
		return roleString;
	}

	@Override
	public XtremandResponse createAccount(AccountDTO accountDTO) {
		XtremandResponse response = new XtremandResponse();
		if (org.springframework.util.StringUtils.hasText(accountDTO.getCompanyLogoPath())) {
			response.setMessage("Fail");
			String emailId = accountDTO.getUserEmailId();
			if (emailId != null && !emailId.trim().isEmpty()) {
				emailId = emailId.toLowerCase();
				Criteria criteria = new Criteria(EMAIL_ID, OPERATION_NAME.eq, emailId);
				List<Criteria> criterias = Arrays.asList(criteria);
				Collection<User> users = userDAO.find(criterias,
						new FindLevel[] { FindLevel.ROLES, FindLevel.COMPANY_PROFILE });
				try {
					User user = null;
					if (!users.isEmpty()) {
						User existingUser = users.iterator().next();
						user = existingUser;
						if (existingUser != null && (Role.hasAnyAdminRole(user.getRoles()))
								&& existingUser.getCompanyProfile() != null) {
							throw new UserDataAccessException("Existing Admin.");
						} else if (existingUser != null && teamDao.isTeamMember(existingUser.getUserId())) {
							throw new UserDataAccessException("Team member can not be upgraded to Vendor.");
						}
					}
					response.setStatusCode(200);
					response.setMessage("Success");
				} catch (UserDataAccessException | ParseException e) {
					throw new UserDataAccessException(e.getMessage());
				}
			}
		} else {
			response.setStatusCode(404);
		}

		return response;
	}

	@Override
	public XtremandResponse saveAccountPassword(UserDTO userDto) {
		XtremandResponse response = new XtremandResponse();
		if (userDto.getId() != null && userDto.getId() > 0
				&& org.springframework.util.StringUtils.hasText(userDto.getPassword())) {
			User user = genericDAO.get(User.class, userDto.getId());
			if (user != null) {
				if (!org.springframework.util.StringUtils.hasText(user.getPassword())) {
					user.setPassword(passwordEncoder.encode(userDto.getPassword()));
					user.setUpdatedTime(new Date());
					user.setUpdatedBy(userDto.getId());
					user.setFirstName(userDto.getFirstName());
					user.setLastName(userDto.getLastName());
					boolean isTeamMember = teamDao.isTeamMember(user.getUserId());
					if (isTeamMember) {
						teamDao.apporveTeamMember(user.getUserId());
					}
					user.setUserStatus(UserStatus.APPROVED);
					response.setStatusCode(200);
					response.setMessage("Success");
					String message = "Password Updated Successfully For {" + userDto.getId() + ","
							+ userDto.getFirstName() + "," + userDto.getLastName() + "}";
					logger.debug(message);
				} else {
					String errorMessage = accountAlreadyExists + "{" + userDto.getId() + "," + userDto.getFirstName()
							+ "," + userDto.getLastName() + "}";
					logger.error(errorMessage);
					ResponseUtil.setResponse(409, accountAlreadyExists, response);
				}
			} else {
				String warningMessage = "User does not exists for {" + userDto.getId() + "," + userDto.getFirstName()
						+ "," + userDto.getLastName() + "}";
				logger.error(warningMessage);
				ResponseUtil.setResponse(404, "User does not exists", response);
			}
		} else {
			String warningMessage = "userId/password is not received from the request{" + userDto.getId() + ","
					+ userDto.getFirstName() + "," + userDto.getLastName() + "}";
			logger.error(warningMessage);
			ResponseUtil.setResponse(400, "userId/password is not received from the request", response);
		}
		return response;
	}

	@Override
	public XtremandResponse sendUserWelcomeEmailThroughAdmin(EmailDTO emailDTO) {
		XtremandResponse response = new XtremandResponse();
		asyncComponent.sendUserWelcomeEmailThroughAdmin(emailDTO);
		response.setStatusCode(200);
		response.setMessage("Success");
		return response;
	}

	@Override
	public XtremandResponse updateAccount(AccountDTO accountDTO) {
		XtremandResponse response = new XtremandResponse();
		response.setMessage("Fail");
		Integer userId = accountDTO.getId();
		if (userId != null && userId > 1) {
			try {
				User user = userDAO.findByPrimaryKey(userId, new FindLevel[] { FindLevel.SHALLOW });
				user.setUpdatedTime(new Date());
				user.setFirstName(accountDTO.getFirstName());
				user.setLastName(accountDTO.getLastName());
				CompanyProfile companyProfile = user.getCompanyProfile();
				String existingCompanyProfileName = companyProfile.getCompanyProfileName();
				Integer companyId = companyProfile.getId();
				BeanUtils.copyProperties(companyProfile, accountDTO);
				companyProfile.setId(companyId);
				companyProfile.setCompanyProfileName(existingCompanyProfileName);
				response.setStatusCode(200);
				response.setMessage("Success");
			} catch (UserDataAccessException | IllegalAccessException | InvocationTargetException e) {
				throw new UserDataAccessException(e.getMessage());
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getUserAndCompanyProfileByEmailId(String emailId) {
		XtremandResponse response = new XtremandResponse();
		User user = loadUser(Arrays.asList(new Criteria(EMAIL_ID, OPERATION_NAME.eq, emailId)),
				new FindLevel[] { FindLevel.ROLES, FindLevel.COMPANY_PROFILE });
		if (user != null) {
			Map<String, Object> map = new HashMap<>();
			UserDTO userDTO = new UserDTO();
			userDTO.setId(user.getUserId());
			userDTO.setEmailId(user.getEmailId());
			userDTO.setFirstName(user.getFirstName());
			userDTO.setLastName(user.getLastName());
			userDTO.setRoles(user.getRoles());
			boolean isTeamMember = teamDao.isTeamMember(user.getUserId());
			if (isTeamMember) {
				userDTO.setTeamMember(isTeamMember);
			}
			if (user.getCompanyProfile() != null) {
				map.put("companyProfile", user.getCompanyProfile());
				userDTO.setCompanyLogo(server_path + user.getCompanyProfile().getCompanyLogoPath());
			}
			map.put("user", userDTO);
			response.setData(map);
			response.setStatusCode(200);
		} else {
			response.setStatusCode(404);
			response.setMessage("This user does not exists");
		}
		return response;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse accountApproval(UserDTO userDto, boolean approve) {
		XtremandResponse response = new XtremandResponse();
		response.setMessage("Fail");
		Integer userId = userDto.getId();
		if (userId != null && userId > 0) {
			User user = genericDAO.get(User.class, userId);
			if (user != null) {
				CompanyProfile companyProfile = user.getCompanyProfile();
				if (approve) {
					if (companyProfile != null) {
						Integer companyId = companyProfile.getId();
						userDAO.approveOrUnApproveAllUsersByCompanyId(companyId);
					} else {
						user.setUserStatus(UserStatus.APPROVED);
					}

				} else {
					if (companyProfile != null) {
						Integer companyId = companyProfile.getId();
						utilService.revokeAccessTokensByCompanyId(companyId);
						userDAO.declineAllUsersByCompanyId(companyId);
					} else {
						utilService.revokeAccessTokenByUserId(userId);
						user.setUserStatus(UserStatus.DECLINE);
					}

				}
			}
			response.setStatusCode(200);
			response.setMessage("Success");
		}
		return response;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse saveUserAndCompanyProfile(MultipartFile file, AccountDTO accountDto) {
		XtremandResponse response = new XtremandResponse();
		User user = loadUser(Arrays.asList(new Criteria(EMAIL_ID, OPERATION_NAME.eq, accountDto.getEmailId())),
				new FindLevel[] { FindLevel.ROLES, FindLevel.COMPANY_PROFILE });
		if (user != null) {

		} else {
			CompanyProfile companyProfile = new CompanyProfile();
			org.springframework.beans.BeanUtils.copyProperties(accountDto, companyProfile);
			companyProfile.setEmailDnsConfigured(false);
			Integer companyId = genericDAO.save(companyProfile);
			String companyLogoPath = returnImagePath(file, companyId, "company-profile-logo");
			companyProfile.setCompanyLogoPath(companyLogoPath);
		}

		return response;
	}

	@Override
	public XtremandResponse getUserByAlias(String alias, String companyProfileName) {
		XtremandResponse response = new XtremandResponse();
		User user = utilDao.getUser(alias);
		if (user != null) {
			if (!org.springframework.util.StringUtils.hasText(user.getPassword())) {
				UserDTO userDto = new UserDTO();
				userDto.setId(user.getUserId());
				userDto.setFirstName(user.getFirstName());
				userDto.setLastName(user.getLastName());
				userDto.setEmailId(user.getEmailId());
				response.setStatusCode(200);
				response.setData(userDto);
			} else {
				response.setStatusCode(409);
				String message = accountAlreadyExists;
				if (!StringUtils.isBlank(companyProfileName)) {
					String vanityURLDomain = xamplifyUtil.frameVanityURL(webUrl, companyProfileName);
					message = "Your account is already created.<br><br>Please <a href=" + vanityURLDomain
							+ "login>login</a> to access the platform.";
				}
				response.setMessage(message);
			}
		} else {
			response.setStatusCode(404);
			response.setMessage("Oops!This is invalid link.");
		}
		return response;
	}

	@Override
	public List<Integer> listUnsubscribedCompanyIdsByUserId(Integer userId) {
		return userDAO.listUnsubscribedCompanyIdsByUserId(userId);
	}

	@Override
	public XtremandResponse getRoleDtosByUserId(Integer userId) {
		XtremandResponse response = new XtremandResponse();
		List<String> roleDtos = userDAO.listRolesByUserId(userId);
		if (roleDtos.isEmpty()) {
			response.setStatusCode(404);
			response.setMessage("Roles not found");
		} else {
			response.setData(roleDtos);
			response.setStatusCode(200);
		}
		return response;
	}

	@Override
	public XtremandResponse registerPrmAccount(SignUpRequestDTO signUpRequestDTO) {
		XtremandResponse response = new XtremandResponse();
		Map<String, String> errors = new HashMap<>();
		if (signUpRequestDTO == null) {
			errors.put("emailId", "Email is required");
			errors.put("firstName", "First name is required");
			errors.put("password", "Password is required");
			errors.put("confirmPassword", "Confirm password is required");
		} else {
			if (StringUtils.isBlank(signUpRequestDTO.getEmailId())) {
				errors.put("emailId", "Email is required");
			} else if (!emailValidator.validate(signUpRequestDTO.getEmailId().trim().toLowerCase())) {
				errors.put("emailId", "The entered email is not formatted properly");
			}
			if (StringUtils.isBlank(signUpRequestDTO.getFirstName())) {
				errors.put("firstName", "First name is required");
			}
			if (StringUtils.isBlank(signUpRequestDTO.getPassword())) {
				errors.put("password", "Password is required");
			}
			if (StringUtils.isBlank(signUpRequestDTO.getConfirmPassword())) {
				errors.put("confirmPassword", "Confirm password is required");
			}
		}
		if (signUpRequestDTO != null && StringUtils.isNotBlank(signUpRequestDTO.getPassword())
				&& StringUtils.isNotBlank(signUpRequestDTO.getConfirmPassword())
				&& !signUpRequestDTO.getPassword().equals(signUpRequestDTO.getConfirmPassword())) {
			errors.put("confirmPassword", "Passwords do not match");
		}
		if (!errors.isEmpty()) {
			response.setStatusCode(400);
			response.setMessage("Validation failed");
			response.setErrors(errors);
			response.setAccess(false);
			return response;
		}
		if (!PASSWORD_PATTERN.matcher(signUpRequestDTO.getPassword()).matches()) {
			response.setStatusCode(400);
			response.setMessage(
					"Password must be 6–20 characters long, with at least one letter, one number, and one special "
							+ "character. Spaces are not allowed");
			return response;
		}
		if (userDAO.prmAccountExists()) {
			response.setStatusCode(400);
			response.setMessage("Account creation blocked: PRM account is already set up.");
			return response;
		}
		User existing = userDAO.getUserByEmail(signUpRequestDTO.getEmailId().toLowerCase());
		if (existing != null) {
			response.setStatusCode(400);
			response.setMessage("User is already existing with this email");
			return response;
		}
		User user = new User();
		user.setEmailId(signUpRequestDTO.getEmailId().toLowerCase());
		user.setUserName(signUpRequestDTO.getEmailId().toLowerCase());
		user.setFirstName(signUpRequestDTO.getFirstName());
		user.setLastName(signUpRequestDTO.getLastName());
		user.setPassword(passwordEncoder.encode(signUpRequestDTO.getPassword()));
		user.setUserStatus(UserStatus.APPROVED);
		user.setSource(UserSource.SIGNUP);
		user.setRegisteredTime(new Date());
		user.setAlias(new GenerateRandomPassword().getPassword());
		Set<Role> roles = new HashSet<>();
		roles.add(Role.PRM_ROLE);
		roles.add(Role.USER_ROLE);
		user.setRoles(roles);
		user.initialiseCommonFields(true, 0);
		genericDAO.save(user);
		response.setStatusCode(200);
		response.setMessage("PRM Account created");
		return response;
	}

	@Override
	public XtremandResponse getModulesDisplayDefaultView(Integer userId) {
		XtremandResponse response = new XtremandResponse();
		ModulesDisplayType modulesDisplayType = userDAO.getModulesDisplayDefaultView(userId);
		if (modulesDisplayType != null) {
			response.setStatusCode(200);
			response.setData(modulesDisplayType);
		} else {
			response.setStatusCode(404);
		}
		return response;
	}

	@Override
	public XtremandResponse updateDefaultDisplayView(Integer userId, String type) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(200);
		userDAO.updateDefaultDisplayView(userId, type);
		response.setMessage("Default view changed successfully");
		return response;
	}

	/* -- XNFR-415 -- */
	@Override
	public XtremandResponse updateDefaultDashboardForPartner(Integer companyId, String type) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(200);
		userDAO.updateDefaultDashboardForPartner(companyId, type);
		response.setMessage("Default dashboard for partner has changed successfully");
		return response;
	}

	/* -- XNFR-415 -- */
	@Override
	public XtremandResponse getDefaultDashboardForPartner(Integer companyId) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(200);
		response.setData(userDAO.getDefaultDashboardForPartner(companyId));
		return response;
	}

	@Override
	public int getRolesCountByUserId(Integer userId) {
		List<String> roles = userDAO.listRolesByUserId(userId);
		if (roles.isEmpty()) {
			return 0;
		} else {
			return roles.size();
		}
	}

	@Override
	public XtremandResponse getCompanyFavIconPath(Integer userId, MultipartFile file) {
		XtremandResponse xRes = new XtremandResponse();
		xRes.setStatusCode(400);
		try {
			xRes.setData(returnImagePath(file, userId, "favicon-logo"));
			xRes.setStatusCode(200);
		} catch (Exception e) {
			xRes.setStatusCode(400);
			String errorMessage = "Error in saving company Fav Icon file :: " + e.getMessage();
			logger.error(errorMessage);
		}
		return xRes;
	}

	@Override
	public XtremandResponse getCompanyBgImagePath(Integer userId, MultipartFile file) {
		XtremandResponse xRes = new XtremandResponse();
		xRes.setStatusCode(400);
		try {
			xRes.setData(returnImagePath(file, userId, "company-background-logo"));
			xRes.setStatusCode(200);
		} catch (Exception e) {
			String errorMessage = "Error in saving company Bg Image file :: " + e.getMessage();
			logger.error(errorMessage);
		}
		return xRes;
	}

	@Override
	public String getEmailIdByUserId(Integer userId) {
		return userDAO.getEmailIdByUserId(userId);
	}

	public XtremandResponse validateExcludedUser(UserDTO userDTO, XtremandResponse response) {
		if (!emailValidator.validate(userDTO.getEmailId().trim().toLowerCase())) {
			response.setMessage("The entered email is not formatted properly");
			response.setStatusCode(401);
		}
		return response;
	}

	public XtremandResponse isExcludedUserExists(Integer userId, User customer, XtremandResponse response) {
		boolean isExcludedUserExists = userDAO.isExcludedUserExists(customer.getCompanyProfile().getId(), userId);
		if (isExcludedUserExists) {
			response.setMessage("The entered email is already added for the exclusion");
			response.setStatusCode(402);
		}
		return response;
	}

	public User createUserWithUserRole(UserDTO userDTO, Integer userId) {
		User user = new User();
		user.setFirstName(userDTO.getFirstName());
		user.setLastName(userDTO.getLastName());
		user.setEmailId(userDTO.getEmailId().trim().toLowerCase());
		user.setUserStatus(UserStatus.UNAPPROVED);
		user.getRoles().add(Role.USER_ROLE);
		user.initialiseCommonFields(true, userId);
		genericDAO.save(user);
		return user;
	}

	public void saveExcludedUser(Integer excludedUserId, User customer) {
		ExcludedUser excludedUser = new ExcludedUser();
		excludedUser.setCompanyId(customer.getCompanyProfile().getId());
		excludedUser.setUserId(excludedUserId);
		excludedUser.setCreatedUser(customer);
		excludedUser.setCreatedTime(new Date());
		genericDAO.save(excludedUser);
	}

	public void saveExcludedDomain(String excludedDomainName, User customer) {
		ExcludedDomain excludedDomain = new ExcludedDomain();
		excludedDomain.setCompanyId(customer.getCompanyProfile().getId());
		excludedDomain.setDomainName(excludedDomainName);
		excludedDomain.setCreatedUser(customer);
		excludedDomain.setCreatedTime(new Date());
		genericDAO.save(excludedDomain);
	}

	@SuppressWarnings("unchecked")
	@Override
	public XtremandResponse listExcludedUsers(Integer userId, Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		Integer companyId = userDAO.getCompanyIdByUserId(userId);
		Map<String, Object> map = userDAO.listExcludedUsers(companyId, pagination);
		List<ExcludedUserDTO> list = (List<ExcludedUserDTO>) map.get("data");
		for (ExcludedUserDTO user : list) {
			String utcString = DateUtils.getUtcString(user.getTime());
			user.setUtcTimeString(utcString);
		}
		map.put("data", list);
		response.setStatusCode(200);
		response.setData(map);
		return response;
	}

	@Override
	public XtremandResponse deleteExcludedUser(Integer userId, Integer excludedUserId) {
		XtremandResponse response = new XtremandResponse();
		Integer companyId = userDAO.getCompanyIdByUserId(userId);
		userDAO.deleteExcludedUser(excludedUserId, companyId);
		response.setStatusCode(200);
		response.setMessage("success");
		return response;
	}

	@Override
	public XtremandResponse findAdminsAndTeamMembers(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(200);
		findPublishedPartnerIds(pagination);
		response.setData(userDAO.findAdminsAndTeamMembersByCompanyId(pagination));
		return response;
	}

	private void findPublishedPartnerIds(Pagination pagination) {
		List<Integer> findPublishedParnterIds = new ArrayList<>();
		if ("lms".equals(pagination.getType())) {
			if (pagination.getPartnershipId() != null && pagination.getPartnershipId() > 0
					&& pagination.getLearningTrackId() != null && pagination.getLearningTrackId() > 0) {
				findPublishedParnterIds = lmsDao.getAllVisibilityUsersIds(pagination.getPartnershipId(),
						pagination.getLearningTrackId());
			}
		} else if (pagination.getCampaignId() != null) {
			findPublishedParnterIds = damDao.findPublishedPartnerIdsByDamIdAndPartnershipId(pagination.getCampaignId(),
					pagination.getPartnershipId());
		}

		if (findPublishedParnterIds != null) {
			pagination.setFiltertedEmailTempalteIds(findPublishedParnterIds);
		}
	}

	public XtremandResponse updateSpfConfiguration(Integer companyId) {
		XtremandResponse response = new XtremandResponse();
		if (companyId != null && companyId > 0) {
			userDAO.updateSpfConfiguration(companyId);
		}
		response.setStatusCode(200);
		return response;
	}

	@Override
	public XtremandResponse isSpfConfigured(Integer companyId) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(200);
		if (companyId != null && companyId > 0) {
			response.setData(userDAO.isSpfConfigured(companyId));
		} else {
			response.setData(false);
		}
		return response;

	}

	@Override
	public XtremandResponse isDnsConfigured(Integer companyId) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(200);
		if (companyId != null && companyId > 0) {
			response.setData(userDAO.isDnsConfigured(companyId));
		} else {
			response.setData(false);
		}
		return response;

	}

	@Override
	public XtremandResponse updateDnsConfiguration(Integer companyId, boolean isDnsConfigured) {
		XtremandResponse response = new XtremandResponse();
		if (companyId != null && companyId > 0) {
			response.setStatusCode(200);
			userDAO.updateDnsConfiguration(companyId, isDnsConfigured);
		}
		return response;
	}

	public boolean isValidDomain(String str) {
		String regExPattern = "^((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}$";
		Pattern p = Pattern.compile(regExPattern);
		if (str == null) {
			return false;
		}
		Matcher m = p.matcher(str);
		return m.matches();
	}

	@Override
	public XtremandResponse deleteExcludedDomain(Integer userId, String domain) {
		XtremandResponse response = new XtremandResponse();
		Integer companyId = userDAO.getCompanyIdByUserId(userId);
		userDAO.deleteExcludedDomain(domain.trim().toLowerCase(), companyId);
		response.setStatusCode(200);
		response.setMessage("Domain name has been deleted successfully");
		return response;
	}

	@SuppressWarnings("unchecked")
	@Override
	public XtremandResponse listExcludedDomains(Integer userId, Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		Integer companyId = userDAO.getCompanyIdByUserId(userId);
		Map<String, Object> map = userDAO.listExcludedDomains(companyId, pagination);
		List<ExcludedUserDTO> list = (List<ExcludedUserDTO>) map.get("data");
		for (ExcludedUserDTO excludedUserDTO : list) {
			String utcString = DateUtils.getUtcString(excludedUserDTO.getTime());
			excludedUserDTO.setUtcTimeString(utcString);
		}
		map.put("data", list);
		response.setStatusCode(200);
		response.setData(map);
		return response;
	}

	@Override
	public XtremandResponse findPartnerCompaniesAndModulesAccess(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setData(userDAO.findPartnerCompaniesAndModulesAccess(pagination));
		return response;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse updatePartnerModules(ModuleAccessDTO moduleAccessDTO) {
		XtremandResponse response = new XtremandResponse();
		Integer companyId = moduleAccessDTO.getCompanyId();
		Integer assignedSecondAdminsCount = teamDao.getSecondAdminsCountByCompanyId(companyId) + 1;
		Integer selectedAdminsCount = moduleAccessDTO.getMaxAdmins();
		if (selectedAdminsCount < assignedSecondAdminsCount) {
			response.setStatusCode(403);
			response.setMessage("Maximum Admins (" + selectedAdminsCount + ") cannot be updated as this company has "
					+ assignedSecondAdminsCount + " admins already");
		} else {
			response.setStatusCode(200);
			ModulesEmailNotification modulesEmailNotification = new ModulesEmailNotification();
			userDAO.updatePartnerModules(moduleAccessDTO);
			if (!moduleAccessDTO.isNonVanityAccessEnabled()) {
				utilService.revokeAccessTokensByCompanyId(moduleAccessDTO.getCompanyId());
			}
			CompanyProfile companyProfile = new CompanyProfile();
			String companyName = userDAO.getCompanyNameByCompanyId(companyId).getCompanyName();
			companyProfile.setCompanyName(companyName);
			companyProfile.setId(companyId);
			thmymeLeafService.sendModulesUpdateEmailNotification(companyProfile, modulesEmailNotification);
		}
		return response;
	}

	@SuppressWarnings("unchecked")
	@Override
	public XtremandResponse findAllUsers(String filter) {
		XtremandResponse response = new XtremandResponse();
		boolean isPartnerTeamMemberFilter = "ptm".equalsIgnoreCase(filter);
		boolean isAnyAdminTeamMember = "atm".equalsIgnoreCase(filter);
		boolean isPartnerFilter = "p".equalsIgnoreCase(filter);
		boolean isAnyAdminFilter = "a".equalsIgnoreCase(filter);
		if (isPartnerTeamMemberFilter || isAnyAdminTeamMember || isPartnerFilter || isAnyAdminFilter) {
			Pagination pagination = new Pagination();
			pagination.setExcludeLimit(true);
			Map<String, Object> map = userDAO.findAllUsers(pagination);
			Map<String, Object> updatedMap = new HashMap<>();
			List<AllUsersView> allUsers = (List<AllUsersView>) map.get("list");
			List<AllUsersView> partners = new ArrayList<>();
			List<AllUsersView> partnerTeamMembers = new ArrayList<>();
			List<AllUsersView> adminTeamMembers = new ArrayList<>();
			List<AllUsersView> admins = new ArrayList<>();
			boolean anyTeamMember = isPartnerTeamMemberFilter || isAnyAdminTeamMember;
			boolean anyAdmin = isAnyAdminFilter || isPartnerFilter;
			for (AllUsersView user : allUsers) {
				List<Integer> roleIds = user.getRoleIds();
				if (user.isTeamMember() && (anyTeamMember)) {
					findTeamMembersData(isPartnerTeamMemberFilter, partnerTeamMembers, adminTeamMembers, user,
							isAnyAdminTeamMember);
				} else if (!user.isTeamMember() && anyAdmin) {
					findAdmins(isPartnerFilter, isAnyAdminFilter, partners, admins, user, roleIds);
				}
			}
			filterByRole(isPartnerTeamMemberFilter, isPartnerFilter, isAnyAdminFilter, updatedMap, partners,
					partnerTeamMembers, adminTeamMembers, admins);
			response.setData(updatedMap);
			response.setMap(updatedMap);
			response.setStatusCode(200);
		} else {
			response.setData(404);
			response.setMessage("Invalid Filter");
		}
		return response;
	}

	private void filterByRole(boolean isPartnerTeamMemberFilter, boolean isPartnerFilter, boolean isAnyAdminFilter,
			Map<String, Object> updatedMap, List<AllUsersView> partners, List<AllUsersView> partnerTeamMembers,
			List<AllUsersView> adminTeamMembers, List<AllUsersView> admins) {
		if (isPartnerFilter) {
			getPartnersMap(updatedMap, partners);
		} else if (isPartnerTeamMemberFilter) {
			getPartnerTeamMembersMap(updatedMap, partnerTeamMembers);
		} else if (isAnyAdminFilter) {
			getAdminsMap(updatedMap, admins);
		} else {
			getAdminTeamMembersMap(updatedMap, adminTeamMembers);
		}
	}

	private void getAdminTeamMembersMap(Map<String, Object> updatedMap, List<AllUsersView> adminTeamMembers) {
		updatedMap.put("adminTeamMembers", adminTeamMembers);
		updatedMap.put("totalAdminTeamMembers", adminTeamMembers.size());
	}

	private void getAdminsMap(Map<String, Object> updatedMap, List<AllUsersView> admins) {
		updatedMap.put("admins", admins);
		updatedMap.put("totalAdmins", admins.size());
	}

	private void getPartnerTeamMembersMap(Map<String, Object> updatedMap, List<AllUsersView> partnerTeamMembers) {
		updatedMap.put("partnerTeamMembers", partnerTeamMembers);
		updatedMap.put("totalPartnerTeamMembers", partnerTeamMembers.size());
	}

	private void getPartnersMap(Map<String, Object> updatedMap, List<AllUsersView> partners) {
		updatedMap.put("partners", partners);
		updatedMap.put("totalPartners", partners.size());
	}

	private void findAdmins(boolean isPartnerFilter, boolean isAnyAdminFilter, List<AllUsersView> partners,
			List<AllUsersView> admins, AllUsersView user, List<Integer> roleIds) {
		/******* Only Partners ***********/
		boolean isOnlyPartner = Role.isOnlyPartnerCompanyByRoleIds(roleIds);
		boolean isUserOrSuperAdmin = !roleIds.isEmpty() && roleIds.size() == 1
				&& (roleIds.indexOf(Role.USER_ROLE.getRoleId()) > -1);
		boolean isAnyAdmin = roleIds.indexOf(Role.PRM_ROLE.getRoleId()) > -1;
		if (isOnlyPartner && isPartnerFilter) {
			updateList(partners, user, "Partner");
		} else if (isAnyAdminFilter && !isUserOrSuperAdmin && !isOnlyPartner && isAnyAdmin) {
			RoleDisplayDTO roleDisplayDTO = utilService.setRoleDTO(roleIds);
			updateList(admins, user, roleDisplayDTO.getRole());
		}

	}

	private void findTeamMembersData(boolean isPartnerTeamMemberFilter, List<AllUsersView> partnerTeamMembers,
			List<AllUsersView> otherTeamMembers, AllUsersView user, boolean isOtherTeamMemberFilter) {
		RoleDTO roleDTO = teamDao.getSuperiorIdAndRolesByTeamMemberId(user.getUserId());
		List<String> superiorRoles = roleDTO.getRoles();
		if (superiorRoles != null && !superiorRoles.isEmpty()) {
			boolean isOnlyPartnerTeamMember = Role.isOnlyPartnerCompanyByRoleNames(superiorRoles);
			if (isOnlyPartnerTeamMember && isPartnerTeamMemberFilter) {
				String roleName = "Partner Team Member";
				updateList(partnerTeamMembers, user, roleName);
			} else if (!isOnlyPartnerTeamMember && isOtherTeamMemberFilter) {
				String roleName = "Team Member";
				updateList(otherTeamMembers, user, roleName);
			}

		}
	}

	private void updateList(List<AllUsersView> filteredUsers, AllUsersView user, String roleName) {
		AllUsersView updatedUser = new AllUsersView();
		org.springframework.beans.BeanUtils.copyProperties(user, updatedUser);
		updatedUser.setRoleName(roleName);
		filteredUsers.add(updatedUser);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void downloadAllUsers(String filter, HttpServletResponse httpServletResponse) {
		XtremandResponse response = findAllUsers(filter);
		if (response.getStatusCode() == 200) {
			boolean isPartnerTeamMemberFilter = "ptm".equalsIgnoreCase(filter);
			boolean isAnyAdminTeamMember = "atm".equalsIgnoreCase(filter);
			boolean isPartnerFilter = "p".equalsIgnoreCase(filter);
			boolean isAnyAdminFilter = "a".equalsIgnoreCase(filter);
			Map<String, Object> map = response.getMap();
			List<AllUsersView> users = new ArrayList<>();
			if (isPartnerTeamMemberFilter) {
				users.addAll((List<AllUsersView>) map.get("partnerTeamMembers"));
			} else if (isAnyAdminTeamMember) {
				users.addAll((List<AllUsersView>) map.get("adminTeamMembers"));
			} else if (isPartnerFilter) {
				users.addAll((List<AllUsersView>) map.get("partners"));
			} else if (isAnyAdminFilter) {
				users.addAll((List<AllUsersView>) map.get("admins"));
			}
			csvUtilService.downloadUsers(httpServletResponse, "", users);
		}

	}

	@Override
	public XtremandResponse findRegisteredOrRecentLoggedInUsers(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setData(userDAO.findRegisteredOrRecentLoggedInUsers(pagination));
		return response;
	}

	@Override
	public void removeNullEmailUsers(Collection<User> users) {
		List<User> usersList = (List<User>) users;
		ListIterator<User> listIterator = usersList.listIterator();
		while (listIterator.hasNext()) {
			User inputUser = listIterator.next();
			if (inputUser.getEmailId() == null) {
				listIterator.remove();
			}
		}
	}

	@Override
	public void removeNullEmailUserDTOs(Collection<UserDTO> users) {
		Iterator<UserDTO> iterator = users.iterator();
		while (iterator.hasNext()) {
			UserDTO inputUser = iterator.next();
			if (inputUser.getEmailId() == null) {
				iterator.remove();
			}
		}
	}

	@Override
	public XtremandResponse saveExcludedUsers(Set<UserDTO> userDTOs, Integer userId) {
		XtremandResponse response = new XtremandResponse();
		User customer = loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, userId)),
				new FindLevel[] { FindLevel.COMPANY_PROFILE });
		return validateAndSaveExcludedUsers(userDTOs, customer, response);
	}

	public XtremandResponse validateAndSaveExcludedUsers(Set<UserDTO> userDTOs, User customer,
			XtremandResponse response) {
		removeNullEmailUserDTOs(userDTOs);
		if (userDTOs != null) {
			response = validateExcludedUsers(userDTOs, response);
			if (response.getStatusCode() == 401) {
				return response;
			}
			if (userDTOs.size() == 1) {
				Iterator<UserDTO> iterator = userDTOs.iterator();
				UserDTO userDTO = iterator.next();
				User excludedUser = loadUser(
						Arrays.asList(new Criteria(EMAIL_ID, OPERATION_NAME.eq, userDTO.getEmailId().toLowerCase())),
						new FindLevel[] { FindLevel.SHALLOW });
				if (excludedUser != null) {
					response = isExcludedUserExists(excludedUser.getUserId(), customer, response);
					if (response.getStatusCode() == 402) {
						return response;
					}
				}
			}
			return saveExcludedUsers(userDTOs, customer, response);
		} else {
			response.setMessage("No users found");
			response.setStatusCode(403);
			return response;
		}
	}

	public XtremandResponse saveExcludedUsers(Set<UserDTO> userDTOs, User customer, XtremandResponse response) {
		for (UserDTO userDTO : userDTOs) {
			User excludedUser = loadUser(
					Arrays.asList(new Criteria(EMAIL_ID, OPERATION_NAME.eq, userDTO.getEmailId().toLowerCase())),
					new FindLevel[] { FindLevel.SHALLOW });
			if (excludedUser == null) {
				excludedUser = createUserWithUserRole(userDTO, customer.getUserId());
			} else {
				if (userDAO.isExcludedUserExists(customer.getCompanyProfile().getId(), excludedUser.getUserId())) {
					continue;
				}
			}
			saveExcludedUser(excludedUser.getUserId(), customer);
		}
		response.setStatusCode(200);
		response.setMessage("success");
		return response;
	}

	public XtremandResponse validateExcludedUsersExist(Set<UserDTO> excludedUsers, Integer userId) {
		XtremandResponse response = new XtremandResponse();
		Set<UserDTO> existingUsers = partnershipServiceHelper.getExistingUserList(excludedUsers);
		User customer = loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, userId)),
				new FindLevel[] { FindLevel.COMPANY_PROFILE });

		if (existingUsers != null && existingUsers.size() > 0) {
			List<String> existingMailIds = existingUsers.stream().map(user -> user.getEmailId())
					.collect(Collectors.toList());
			List<String> alreadyExcludedMailIds = userListDAO
					.getExistingExcludedUsers(customer.getCompanyProfile().getId(), existingMailIds);
			if (alreadyExcludedMailIds != null && alreadyExcludedMailIds.size() > 0) {
				response.setStatusCode(400);
				response.setMessage(
						"The following user Mail Id('s) already Excluded " + String.join(", ", alreadyExcludedMailIds));

				return response;
			}
		}
		response.setStatusCode(200);
		return response;
	}

	public XtremandResponse validateExcludedUsers(Set<UserDTO> userDTOs, XtremandResponse response) {
		List<String> invalidEmailIds = new ArrayList<>();
		for (UserDTO userDTO : userDTOs) {
			if (!emailValidator.validate(userDTO.getEmailId().trim().toLowerCase())) {
				invalidEmailIds.add(userDTO.getEmailId());
			}
		}
		if (!invalidEmailIds.isEmpty()) {
			response.setStatusCode(401);
			response.setMessage("Some of the input email address(es)'s aren't formatted properly :" + invalidEmailIds);
		}
		return response;
	}

	@Override
	public XtremandResponse saveExcludedDomains(Set<String> domainNames, Integer userId) {
		XtremandResponse response = new XtremandResponse();
		User customer = loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, userId)),
				new FindLevel[] { FindLevel.COMPANY_PROFILE });
		return validateAndSaveExcludedDomains(domainNames, customer, response);
	}

	public XtremandResponse validateAndSaveExcludedDomains(Set<String> domainNames, User customer,
			XtremandResponse response) {
		if (!domainNames.isEmpty()) {
			domainNames.removeIf(item -> item == null || "".equals(item));
			response = validateExcludedDomains(domainNames, response);
			if (response.getStatusCode() == 402) {
				return response;
			}
			if (domainNames.size() == 1) {
				Iterator<String> iterator = domainNames.iterator();
				String domainName = iterator.next();
				boolean isDomainExists = userDAO.isExcludedDomainExists(customer.getCompanyProfile().getId(),
						domainName.trim().toLowerCase());
				if (isDomainExists) {
					response.setStatusCode(401);
					response.setMessage("This domain name has already been added");
					return response;
				}
			}
			return saveExcludedDomains(domainNames, customer, response);
		} else {
			response.setMessage("No domain names are found");
			response.setStatusCode(403);
			return response;
		}
	}

	@Override
	public XtremandResponse validateExcludedDomainsExist(Set<String> excludedDomains, Integer userId) {
		List<String> existingDomainNames = new ArrayList<>();
		User customer = loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, userId)),
				new FindLevel[] { FindLevel.COMPANY_PROFILE });
		for (String domainName : excludedDomains) {
			boolean isDomainExists = userDAO.isExcludedDomainExists(customer.getCompanyProfile().getId(),
					domainName.trim().toLowerCase());
			if (isDomainExists) {
				existingDomainNames.add(domainName);
			}
		}

		XtremandResponse response = new XtremandResponse();
		if (!existingDomainNames.isEmpty()) {
			response.setStatusCode(400);
			response.setMessage(
					"The following user Domain('s) already Excluded " + String.join(", ", existingDomainNames));
		} else {
			response.setStatusCode(200);
		}
		return response;
	}

	public XtremandResponse validateExcludedDomains(Set<String> domainNames, XtremandResponse response) {
		List<String> inValidDomainNames = new ArrayList<>();
		for (String domainName : domainNames) {
			if (!isValidDomain(domainName.trim().toLowerCase())) {
				inValidDomainNames.add(domainName);
			}
		}
		if (!inValidDomainNames.isEmpty()) {
			response.setStatusCode(402);
			response.setMessage("Following domain name's aren't formatted properly :" + inValidDomainNames);
		}
		return response;
	}

	public XtremandResponse saveExcludedDomains(Set<String> domainNames, User customer, XtremandResponse response) {
		for (String domainName : domainNames) {
			domainName = domainName.trim().toLowerCase();
			boolean isDomainExists = userDAO.isExcludedDomainExists(customer.getCompanyProfile().getId(), domainName);
			if (isDomainExists) {
				continue;
			} else {
				saveExcludedDomain(domainName, customer);
			}
		}
		response.setStatusCode(200);
		response.setMessage("Domain name(s) has been added successfully");
		return response;
	}

	@Override
	public XtremandResponse findNotifyPartnersOption(Integer companyId) {
		XtremandResponse response = new XtremandResponse();
		Map<String, Object> map = new HashMap<String, Object>();
		response.setStatusCode(200);
		if (companyId != null && companyId > 0) {
			response.setData(userDAO.findNotifyPartnersOption(companyId));
			map.put("teamMemberGroups", teamMemberGroupService.findAllGroupIdsAndNamesByCompanyId(companyId, true));
			response.setMap(map);
		} else {
			response.setData(false);
			map.put("teamMemberGroups", new ArrayList<TeamMemberGroup>());
		}
		return response;

	}

	@Override
	public XtremandResponse updateNotifyPartnersOption(Integer companyId, boolean status) {
		XtremandResponse response = new XtremandResponse();
		if (companyId != null && companyId > 0) {
			userDAO.updateNotifyPartnersOption(companyId, status);
		}
		response.setStatusCode(200);
		return response;
	}

	@Override
	public Integer getUserIdByEmail(String emailId) {
		return userDAO.getUserIdByEmail(emailId);
	}

	@Override
	public boolean isNonProcessedUser(String emailId) {
		boolean result = false;
		User exUser = loadUser(Arrays.asList(new Criteria(EMAIL_ID, OPERATION_NAME.eq, emailId.toLowerCase())),
				new FindLevel[] { FindLevel.SHALLOW });
		if ((exUser == null) || (exUser != null && exUser.isEmailValidationInd() == false)) {
			result = true;
		}
		return result;
	}

	/********* XNFR-83 *****************/
	@Override
	public List<String> findVendorCompanyImages(Integer userId, String domainName) {
		List<String> companyImages = new ArrayList<String>();
		companyImages.addAll(listAllCompanyProfileImages(userId));
		return companyImages;
	}

	@Override
	public XtremandResponse getFirstNameLastNameAndEmailIdByUserId(Integer userId) {
		XtremandResponse response = new XtremandResponse();
		User user = userDAO.getFirstNameLastNameAndEmailIdByUserId(userId);
		response.setData(user);
		response.setStatusCode(200);
		return response;
	}

	@Override
	public User saveOrUpdateUser(UserDTO userDTO, Integer loggedInUserId) {
		User user = loadUser(Arrays.asList(new Criteria(EMAIL_ID, OPERATION_NAME.eq, userDTO.getEmailId().trim())),
				new FindLevel[] { FindLevel.COMPANY_PROFILE });
		if (user == null) {
			user = new User();
			user.setEmailId(userDTO.getEmailId().trim().toLowerCase());
			user.setUserName(userDTO.getEmailId().trim());
			user.setUserDefaultPage(UserDefaultPage.WELCOME);
			user.setUserStatus(UserStatus.UNAPPROVED);
			user.setModulesDisplayType(ModulesDisplayType.LIST);
			user.setRoles(new HashSet<>(Arrays.asList(Role.USER_ROLE)));
			GenerateRandomPassword randomPassword = new GenerateRandomPassword();
			user.setAlias(randomPassword.getPassword());
			user.initialiseCommonFields(true, loggedInUserId);
			genericDAO.save(user);
		} else {
			if (user.getAlias() == null) {
				GenerateRandomPassword randomPassword = new GenerateRandomPassword();
				user.setAlias(randomPassword.getPassword());
				user.initialiseCommonFields(true, loggedInUserId);
				genericDAO.save(user);
			}

		}
		return user;
	}

	@Override
	public XtremandResponse resendActivationEmail(ResendEmailDTO resendEmailDTO) {
		XtremandResponse response = new XtremandResponse();
		String emailId = resendEmailDTO.getEmailId();
		String domainName = resendEmailDTO.getVendorCompanyProfileName();
		User user = loadUser(Arrays.asList(new Criteria(EMAIL_ID, OPERATION_NAME.eq, emailId)),
				new FindLevel[] { FindLevel.SHALLOW });
		if (user != null) {
			boolean isOnlyPartner = utilDao.isOnlyPartnerCompany(user.getUserId());
			if (isOnlyPartner) {
				boolean isUnApprovedUser = user.getUserStatus().equals(UserStatus.UNAPPROVED);
				boolean hasPassword = org.springframework.util.StringUtils.hasText(user.getPassword());
				if (isUnApprovedUser && hasPassword) {
					user.setCompanyProfileName(domainName);
					mailService.sendActivationMail(user, EmailConstants.NEW_USER_SIGNUP);
					response.setStatusCode(200);
					response.setMessage("Activation Email Sent Successfully.");
				} else {
					setAccountAlreadyActivatedErrorMessage(response);
				}

			} else {
				setInvalidRequestErrorMessage(response);
			}
		} else {
			setAccountNotFoundErrorMessage(response);
		}

		return response;
	}

	private void setAccountAlreadyActivatedErrorMessage(XtremandResponse response) {
		response.setStatusCode(400);
		response.setMessage("Account Already Activated.");
	}

	private void setInvalidRequestErrorMessage(XtremandResponse response) {
		response.setStatusCode(400);
		response.setMessage("Invalid Request");
	}

	private void setAccountNotFoundErrorMessage(XtremandResponse response) {
		response.setStatusCode(400);
		response.setMessage(
				"We couldn't find your account. Please check that you've entered the correct email address and try again.");
	}

	@Override
	public XtremandResponse updateSpfConfiguration(Integer companyId, boolean isSpfConfigured) {
		XtremandResponse response = new XtremandResponse();
		if (companyId != null && companyId > 0) {
			userDAO.updateSpfConfiguration(companyId, isSpfConfigured);
		}
		response.setStatusCode(200);
		return response;
	}

	@Override
	public XtremandResponse isSpfConfiguredOrDomainConnected(Integer companyId) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(200);
		boolean result = false;
		if (companyId != null && companyId > 0) {
			CompanyDTO companyDTO = userDAO.isSpfConfiguredOrDomainConnected(companyId);
			if (companyDTO != null) {
				boolean isSpfConfigured = companyDTO.isSpfConfigured();
				boolean domainConnected = companyDTO.isDomainConnected();
				result = isSpfConfigured || domainConnected;
			}
		}
		response.setData(result);
		return response;

	}

	/*** Start XNFR-233 **/

	/* -- XNFR-415 -- */
	@Override
	public XtremandResponse getDefaultDashboardPageForPartner(VanityUrlDetailsDTO vanityUrlDetails) {
		XtremandResponse response = new XtremandResponse();
		UserDefaultPage defaultDashboardTypeForPartner = UserDefaultPage.WELCOME;
		User user = genericDAO.get(User.class, vanityUrlDetails.getUserId());
		if (user.getUserDefaultPage() != null) {
			defaultDashboardTypeForPartner = user.getUserDefaultPage();
		}
		if (vanityUrlDetails != null) {
			CompanyProfile cp = vanityURLDao
					.getCompanyProfileByCompanyProfileName(vanityUrlDetails.getVendorCompanyProfileName());
			if (cp != null) {
				Integer companyId = cp.getId();
				Integer loggedInUserId = vanityUrlDetails.getUserId();
				if (loggedInUserId != null && loggedInUserId > 0) {
					boolean isTeamMember = teamDao.isTeamMember(loggedInUserId);
					boolean isOnlyPartnerCompany = utilDao.isOnlyPartnerCompany(loggedInUserId);
					boolean isPartnerCompany = utilDao.isPartnerCompany(loggedInUserId);
					if (isTeamMember || isPartnerCompany || isOnlyPartnerCompany) {
						if (vanityUrlDetails.isVanityUrlFilter()) {
							VanityUrlDetailsDTO vanityUrlDetailsDTO = utilService.getVanityUrlFilteredData(
									loggedInUserId, vanityUrlDetails.isVanityUrlFilter(),
									vanityUrlDetails.getVendorCompanyProfileName());
							if (!vanityUrlDetailsDTO.isVendorLoggedInThroughOwnVanityUrl()) {
								if (!user.isDefaultPageUpdated()) {
									String dashboardType = (String) userDAO.getDefaultDashboardForPartner(companyId);
									if ("ASSIGNED_DASHBOARD".equals(dashboardType)) {
										defaultDashboardTypeForPartner = UserDefaultPage.DASHBOARD;
									} else {
										defaultDashboardTypeForPartner = UserDefaultPage.WELCOME;
									}
								}
							}
						}
					}
				}
			}
		}
		response.setStatusCode(200);
		response.setData(defaultDashboardTypeForPartner);
		return response;
	}

	/******* XNFR-426 *******/
	@Override
	public XtremandResponse updateLeadApprovalOrRejectionStatus(Integer companyId, Boolean leadApprovalStatus) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(200);
		userDAO.updateLeadApprovalOrRejectionStatus(companyId, leadApprovalStatus);
		return response;
	}

	@Override
	public XtremandResponse getLeadApprovalStatus(Integer companyId) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(200);
		response.setData(userDAO.getLeadApprovalStatus(companyId));
		return response;

	}

	public void updateCompanyContactListDetails(User user, UserDTO userDTO, User customer, Boolean isGdprOn,
			Company company, UserUserList userUserList) {
		Boolean isUserExistsInCompanyContactList = userListDAO.isUserExistsInCompanyContactList(user.getUserId(),
				customer.getCompanyProfile().getId());
		if (!isUserExistsInCompanyContactList) {
			Integer comapnyUserListId = userListDAO.getCompanyContactListId(company.getId());
			UserList companyUserList = genericDAO.get(UserList.class, comapnyUserListId);
			UserUserList companyUserUserList = userListService.getUserUserList(user, companyUserList, userDTO, customer,
					isGdprOn);
			genericDAO.save(companyUserUserList);
			companyUserList.getUserUserLists().add(companyUserUserList);
		}
		userListDAO.updateUserUserListByCompanyDetails(userUserList.getCompany().getId(),
				userUserList.getContactCompany(), customer.getCompanyProfile().getId(), user.getUserId());

	}

	public void sendPRMPartnerWelcomeEmail(User user) {
		try {
			if ("true".equals(dripEmailNotificationsEnabled)) {
				EmailTemplate emailTemplate = genericDAO.get(EmailTemplate.class,
						EmailConstants.PRM_PARTNER_WELCOME_ET);
				if (emailTemplate != null) {
					Set<Partnership> partnerships = user.getPartnershipsAsPartner();
					String vendorCompanyName = partnerships.iterator().next().getVendorCompany().getCompanyName();
					DripEmailHistory dripObj = mailService.sendPartnerDripEmail(user, emailTemplate,
							"PARTNER_FIRST_NAME", vendorCompanyName);
					dripObj.setActionId(DripEmailConstants.PARTNER_WELCOME_AT);
					dripObj.setUserId(user);
					dripObj.setEmailTemplateId(emailTemplate);
					dripObj.setSentTime(new Date());
					genericDAO.saveOrUpdate(dripObj);

				}
			}

		} catch (Exception e) {
			logger.error(" sendPRMPartnerWelcomeEmail() : ", e);
		}
	}

	@Override
	public XtremandResponse updateCompanyProfileName(Integer companyId, String companyProfileName) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(200);
		if (org.springframework.util.StringUtils.hasText(companyProfileName)) {
			String companyProfileNameByCompanyId = userDAO.getCompanyProfileNameById(companyId);
			String updatedCompanyProfileName = companyProfileName.trim().toLowerCase();
			if (org.springframework.util.StringUtils.hasText(companyProfileNameByCompanyId)) {
				String updatedCompanyProfileNameByCompanyId = companyProfileNameByCompanyId.trim().toLowerCase();
				if (updatedCompanyProfileNameByCompanyId.equals(updatedCompanyProfileName)) {
					response.setStatusCode(400);
					response.setMessage("No change found in the company profile name.");
				} else {
					boolean isCompanyProfileNameExists = validateCompanyProfileName(updatedCompanyProfileName);
					if (isCompanyProfileNameExists) {
						response.setStatusCode(400);
						response.setMessage("Already exists");
					} else {
						updateCompanyProfileName(companyId, response, updatedCompanyProfileName);
					}
				}

			}
		}
		return response;
	}

	private void updateCompanyProfileName(Integer companyId, XtremandResponse response,
			String updatedCompanyProfileName) {
		HibernateSQLQueryResultRequestDTO updateCompanyProfileNameRequestDTO = new HibernateSQLQueryResultRequestDTO();
		updateCompanyProfileNameRequestDTO.setQueryString(
				"update xt_company_profile set company_profile_name = :companyProfileName where company_id = :companyId");
		updateCompanyProfileNameRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("companyProfileName", updatedCompanyProfileName));
		updateCompanyProfileNameRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("companyId", companyId));
		hibernateSQLQueryResultUtilDao.update(updateCompanyProfileNameRequestDTO);
		response.setMessage("Company Profile Name Updated Successfully.");
	}

	private boolean validateCompanyProfileName(String updatedCompanyProfileName) {
		HibernateSQLQueryResultRequestDTO companyProfileNameExistsRequestDTO = new HibernateSQLQueryResultRequestDTO();
		companyProfileNameExistsRequestDTO.setQueryString(
				"select case when count(*)>0 then true else false end from xt_company_profile where company_profile_name = LOWER(TRIM(:companyProfileName))");
		companyProfileNameExistsRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("companyProfileName", updatedCompanyProfileName));
		return hibernateSQLQueryResultUtilDao.returnBoolean(companyProfileNameExistsRequestDTO);
	}

	@Override
	public XtremandResponse findAllCompanyNames() {
		XtremandResponse response = new XtremandResponse();
		XamplifyUtils.addSuccessStatus(response);
		response.setData(userDAO.findAllCompanyNames());
		return response;
	}

	@Override
	public XtremandResponse findAllPartnerCompanyNames(String vendorCompanyProfileName) {
		XtremandResponse response = new XtremandResponse();
		XamplifyUtils.addSuccessStatus(response);
		Integer companyId = userDAO.getCompanyIdByProfileName(vendorCompanyProfileName);
		response.setData(userDAO.findAllPartnerCompanyNamesByVendorCompanyId(companyId));
		return response;
	}

	@SuppressWarnings("unused")
	private void addCompanyAndPartnerIds(List<Partnership> partnerships,
			Map<Integer, Set<Integer>> companyAndPartnerIds, Set<Integer> partnerIds) {
		for (Partnership partnership : partnerships) {
			if (companyAndPartnerIds.containsKey(partnership.getPartnerCompany().getId())) {
				partnerIds = companyAndPartnerIds.get(partnership.getPartnerCompany().getId());
				partnerIds.add(partnership.getRepresentingPartner().getUserId());
				companyAndPartnerIds.put(partnership.getPartnerCompany().getId(), partnerIds);
			} else {
				partnerIds.add(partnership.getRepresentingPartner().getUserId());
				companyAndPartnerIds.put(partnership.getPartnerCompany().getId(), partnerIds);
			}
		}
	}

	@SuppressWarnings("unused")
	private FormDTO createDefaultForm(User user, CompanyProfile companyProfile, Integer categoryId,
			boolean isVendorJourney) {
		FormDTO formInputDto = new FormDTO();
		FormDTO newFormDTO = new FormDTO();
		Integer defaultFormId = formDao.getDefaulatVendorJourneyForm(isVendorJourney);
		formInputDto.setUserId(user.getUserId());
		formInputDto.setId(defaultFormId);
		XtremandResponse response = formService.getById(formInputDto);
		if (response.getStatusCode() == 200) {
			newFormDTO = (FormDTO) formService.getById(formInputDto).getData();
			if (categoryId == null || categoryId == 0) {
				categoryId = categoryDao.getDefaultCategoryIdByCompanyId(companyProfile.getId());
			}
			String formName = companyProfile.getCompanyName() + " - " + user.getFirstName()
					+ " - Vendor Journey Default Regular Form";
			boolean formNameExists = formDao.checkFormNameExists(formName);
			if (formNameExists) {
				formName = formName + "_" + companyProfile.getId() + "_" + System.currentTimeMillis();
			}
			newFormDTO.setAlias(null);
			newFormDTO.setId(null);
			newFormDTO.setThumbnailImage(null);
			newFormDTO.setName(formName);
			newFormDTO.setCreatedBy(user.getUserId());
			newFormDTO.setSaveAs(true);
			newFormDTO.setCategoryId(categoryId);
			newFormDTO.setFormType(FormTypeEnum.MASTER_PARTNER_FORM);
			FormDTO savedFormDto = (FormDTO) formService.save(newFormDTO, null).getData();
			newFormDTO.setId(savedFormDto.getId());
			newFormDTO.setAlias(savedFormDto.getAlias());
		}
		return newFormDTO;

	}

	/** XNFR-560 **/
	public void updateUserDefaultPage(String emailId) {
		User loggedInUser = loadUser(Arrays.asList(new Criteria(EMAIL_ID, OPERATION_NAME.eq, emailId)),
				new FindLevel[] { FindLevel.COMPANY_PROFILE });
		if (loggedInUser != null && loggedInUser.getUserId() > 0) {
			Integer loggedInUserId = loggedInUser.getUserId();
			boolean isTeamMember = teamDao.isTeamMember(loggedInUserId);
			boolean isOnlyPartnerCompany = utilDao.isOnlyPartnerCompany(loggedInUserId);
			boolean isPartnerCompany = utilDao.isPartnerCompany(loggedInUserId);
			if (isTeamMember || isPartnerCompany || isOnlyPartnerCompany) {
				loggedInUser.setDefaultPageUpdated(false);
			}
		}
	}

	/*--- XNFR-558 ---*/
	@Override
	public XtremandResponse updateDisplayViewType(Integer userId, String type, String companyProfileName) {
		XtremandResponse response = new XtremandResponse();
		userDAO.updateDefaultDisplayView(userId, type);
		if (org.springframework.util.StringUtils.hasText(companyProfileName)) {
			User loggedInUser = userDAO.findByPrimaryKey(userId, new FindLevel[] { FindLevel.SHALLOW });
			VanityUrlDetailsDTO vanityUrlDetailsDTO = new VanityUrlDetailsDTO();
			vanityUrlDetailsDTO.setUserId(userId);
			vanityUrlDetailsDTO.setVendorCompanyProfileName(companyProfileName);
			vanityUrlDetailsDTO.setVanityUrlFilter(true);
			utilService.isVanityUrlFilterApplicable(vanityUrlDetailsDTO);
			boolean isTeamMember = teamDao.isTeamMember(userId);
			if (isTeamMember) {
				RoleDisplayDTO roleDisplayDto = utilService.getRoleDetailsByUserId(userId);
				Integer primaryAdminId = teamDao
						.findPrimaryAdminIdByCompanyId(vanityUrlDetailsDTO.getVendorCompanyId());
				if (roleDisplayDto.isAnyAdminOrSuperVisor()) {
					userDAO.updateAdminOrPartnerOrTeamMemberViewType(primaryAdminId, null, null, type, false);
				} else {
					TeamMember teamMember = teamDao.getTeamMemberByUserId(userId);
					if (teamMember != null) {
						userDAO.updateAdminOrPartnerOrTeamMemberViewType(primaryAdminId, null, teamMember.getId(), type,
								true);
					}
				}
			} else {
				if (vanityUrlDetailsDTO.isVendorLoggedInThroughOwnVanityUrl()) {
					userDAO.updateAdminOrPartnerOrTeamMemberViewType(userId, null, null, type, false);
				} else if (vanityUrlDetailsDTO.isPartnerLoggedInThroughVanityUrl()) {
					CompanyProfile companyProfile = vanityURLDao
							.getCompanyProfileByCompanyProfileName(companyProfileName);
					Partnership partnership = partnershipDAO.getPartnershipByRepresentingPartner(loggedInUser,
							companyProfile);
					if (partnership != null) {
						Integer primaryAdminId = teamDao
								.findPrimaryAdminIdByCompanyId(vanityUrlDetailsDTO.getVendorCompanyId());
						userDAO.updateAdminOrPartnerOrTeamMemberViewType(primaryAdminId, partnership.getId(), null,
								type, true);
					}
				}
			}

			response.setStatusCode(200);
			response.setMessage("Default view changed successfully");
		}
		return response;
	}

	/*--- XNFR-558 ---*/
	@Override
	public XtremandResponse getDisplayViewType(Integer userId, String companyProfileName) {
		XtremandResponse response = new XtremandResponse();
		if (org.springframework.util.StringUtils.hasText(companyProfileName)) {
			User loggedInUser = userDAO.findByPrimaryKey(userId, new FindLevel[] { FindLevel.ROLES });
			VanityUrlDetailsDTO vanityUrlDetailsDTO = new VanityUrlDetailsDTO();
			vanityUrlDetailsDTO.setUserId(userId);
			vanityUrlDetailsDTO.setVendorCompanyProfileName(companyProfileName);
			vanityUrlDetailsDTO.setVanityUrlFilter(true);
			utilService.isVanityUrlFilterApplicable(vanityUrlDetailsDTO);
			boolean isTeamMember = teamDao.isTeamMember(userId);
			String modulesDisplayType = null;
			if (isTeamMember) {
				TeamMember teamMember = teamDao.getTeamMemberByUserId(userId);
				if (teamMember != null) {
					Integer primaryAdminId = teamDao
							.findPrimaryAdminIdByCompanyId(vanityUrlDetailsDTO.getVendorCompanyId());
					modulesDisplayType = userDAO.getAdminOrPartnerOrTeamMemberViewType(primaryAdminId, null,
							teamMember.getId());
				}
			} else {
				if (vanityUrlDetailsDTO.isVendorLoggedInThroughOwnVanityUrl()) {
					modulesDisplayType = userDAO.getAdminOrPartnerOrTeamMemberViewType(userId, null, null);
				} else if (vanityUrlDetailsDTO.isPartnerLoggedInThroughVanityUrl()) {
					CompanyProfile companyProfile = vanityURLDao
							.getCompanyProfileByCompanyProfileName(companyProfileName);
					Partnership partnership = partnershipDAO.getPartnershipByRepresentingPartner(loggedInUser,
							companyProfile);
					if (partnership != null) {
						Integer primaryAdminId = teamDao
								.findPrimaryAdminIdByCompanyId(vanityUrlDetailsDTO.getVendorCompanyId());
						modulesDisplayType = userDAO.getAdminOrPartnerOrTeamMemberViewType(primaryAdminId,
								partnership.getId(), null);
					}

				}
			}
			if (modulesDisplayType != null) {
				response.setData(modulesDisplayType);
			} else {
				response.setData(ModulesDisplayType.LIST);
			}
		}

		response.setStatusCode(200);
		return response;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse setViewTypeForExistingUsers() {
		logger.debug("View Type Execution Started :- " + new Date());
		XtremandResponse response = new XtremandResponse();
		List<PartnerTeamMemberViewType> partnerTeamMemberViewTypes = new ArrayList<>();
		setPrimaryAdminsViewType(partnerTeamMemberViewTypes);

		logger.debug("Total Records:- " + partnerTeamMemberViewTypes.size());
		userDAO.saveAll(partnerTeamMemberViewTypes);

		response.setStatusCode(200);
		XamplifyUtils.addSuccessStatusWithMessage(response, "View Types Updated Successfully");
		logger.debug("View Type Execution Ended :- " + new Date());
		return response;
	}

	private void initializeCommonFields(PartnerTeamMemberViewType partnerTeamMemberViewType) {
		partnerTeamMemberViewType.setViewUpdated(false);
		partnerTeamMemberViewType.setCreatedTime(new Date());
		partnerTeamMemberViewType.setUpdatedTime(new Date());
	}

	private ViewTypePatchRequestDTO isViewTypeUpdatedUser(UserDTO userDto, List<ViewTypePatchRequestDTO> existingData) {
		ViewTypePatchRequestDTO viewTypeData = existingData.stream()
				.filter(viewType -> userDto.getId().equals(viewType.getUserId())).findAny().orElse(null);
		return viewTypeData;
	}

	private ViewTypePatchRequestDTO isViewTypeUpdatedUser(PartnershipDTO partnershipDto,
			List<ViewTypePatchRequestDTO> existingData) {
		ViewTypePatchRequestDTO viewTypeData = existingData.stream()
				.filter(viewType -> partnershipDto.getId().equals(viewType.getPartnershipId())).findAny().orElse(null);
		return viewTypeData;
	}

	private ViewTypePatchRequestDTO isViewTypeUpdatedUser(TeamMemberDTO teamMemberDto,
			List<ViewTypePatchRequestDTO> existingData) {
		ViewTypePatchRequestDTO viewTypeData = existingData.stream()
				.filter(viewType -> teamMemberDto.getId().equals(viewType.getTeamMemberId())).findAny().orElse(null);
		return viewTypeData;
	}

	private void setPrimaryAdminsViewType(List<PartnerTeamMemberViewType> partnerTeamMemberViewTypes) {
		List<UserDTO> userDtos = userDAO.findAllPrimaryAdminsAndViewTypes();
		List<ViewTypePatchRequestDTO> existingData = userDAO.findUpdatedAdminsViewTypeData(null, false, false);
		for (UserDTO userDto : userDtos) {
			ViewTypePatchRequestDTO existingUser = isViewTypeUpdatedUser(userDto, existingData);
			PartnerTeamMemberViewType partnerTeamMemberViewType = new PartnerTeamMemberViewType();
			User representingVendor = new User();
			representingVendor.setUserId(userDto.getId());
			partnerTeamMemberViewType.setAdmin(representingVendor);
			if (existingUser != null) {
				partnerTeamMemberViewType.setVendorViewType(existingUser.getModulesDisplayType());
				partnerTeamMemberViewType.setViewUpdated(existingUser.isViewUpdated());
				partnerTeamMemberViewType.setCreatedTime(existingUser.getCreatedTime());
				partnerTeamMemberViewType.setUpdatedTime(existingUser.getUpdatedTime());
			} else {
				partnerTeamMemberViewType.setVendorViewType(
						ModulesDisplayType.findByName(userDto.getViewType()) == null ? ModulesDisplayType.LIST
								: ModulesDisplayType.findByName(userDto.getViewType()));
				initializeCommonFields(partnerTeamMemberViewType);
			}

			partnerTeamMemberViewTypes.add(partnerTeamMemberViewType);
			logger.debug("View Type Added to the saving list of adminId :- " + userDto.getId());
			if (XamplifyUtils.isValidInteger(userDto.getCompanyId())) {
				setPartnersViewTypeByUserId(userDto, partnerTeamMemberViewTypes, existingUser);

				setTeamMembersViewTypeByUserId(userDto, null, null, true, partnerTeamMemberViewTypes, existingUser);
			}
		}
	}

	private void setPartnersViewTypeByUserId(UserDTO userDto,
			List<PartnerTeamMemberViewType> partnerTeamMemberViewTypes, ViewTypePatchRequestDTO admin) {
		List<PartnershipDTO> partnershipDtos = userDAO.getAllPartnershipsByCompanyId(userDto.getCompanyId());
		List<ViewTypePatchRequestDTO> existingData = null;
		if (admin != null) {
			existingData = userDAO.findUpdatedAdminsViewTypeData(userDto.getId(), true, false);
		}
		if (XamplifyUtils.isNotEmptyList(partnershipDtos)) {
			for (PartnershipDTO partnershipDto : partnershipDtos) {
				ViewTypePatchRequestDTO existingUser = null;
				if (admin != null) {
					existingUser = isViewTypeUpdatedUser(partnershipDto, existingData);
				}
				PartnerTeamMemberViewType partnerTeamMemberViewType = new PartnerTeamMemberViewType();
				User representingVendor = new User();
				representingVendor.setUserId(userDto.getId());
				Partnership pship = new Partnership();
				pship.setId(partnershipDto.getId());
				partnerTeamMemberViewType.setAdmin(representingVendor);
				partnerTeamMemberViewType.setPartnership(pship);
				if (existingUser != null) {
					partnerTeamMemberViewType.setVendorViewType(existingUser.getModulesDisplayType());
					partnerTeamMemberViewType.setViewUpdated(existingUser.isViewUpdated());
					partnerTeamMemberViewType.setCreatedTime(existingUser.getCreatedTime());
					partnerTeamMemberViewType.setUpdatedTime(existingUser.getUpdatedTime());
				} else {
					partnerTeamMemberViewType
							.setVendorViewType(ModulesDisplayType.findByName(partnershipDto.getViewType()) == null
									? ModulesDisplayType.LIST
									: ModulesDisplayType.findByName(partnershipDto.getViewType()));
					initializeCommonFields(partnerTeamMemberViewType);
				}
				partnerTeamMemberViewTypes.add(partnerTeamMemberViewType);
				logger.debug("View Type Added to the saving list of adminId :- " + userDto.getId()
						+ " and partnershipId :- " + partnershipDto.getId());
				setTeamMembersViewTypeByUserId(userDto, partnershipDto.getRepresentingPartnerId(),
						partnershipDto.getId(), false, partnerTeamMemberViewTypes, admin);
			}
		}
	}

	private void setTeamMembersViewTypeByUserId(UserDTO userDto, Integer partnerUserId, Integer partnershipId,
			boolean isVendor, List<PartnerTeamMemberViewType> partnerTeamMemberViewTypes,
			ViewTypePatchRequestDTO admin) {
		List<TeamMemberDTO> teamMemberDtos = userDAO
				.getAllTeamMembersByUserId(isVendor ? userDto.getId() : partnerUserId);
		List<ViewTypePatchRequestDTO> existingData = null;
		if (admin != null) {
			existingData = userDAO.findUpdatedAdminsViewTypeData(userDto.getId(), isVendor ? false : true, true);
		}
		if (XamplifyUtils.isNotEmptyList(teamMemberDtos)) {
			for (TeamMemberDTO teamMemberDto : teamMemberDtos) {
				ViewTypePatchRequestDTO existingUser = null;
				if (admin != null) {
					existingUser = isViewTypeUpdatedUser(teamMemberDto, existingData);
				}
				PartnerTeamMemberViewType partnerTeamMemberViewType = new PartnerTeamMemberViewType();
				TeamMember tMember = new TeamMember();
				tMember.setId(teamMemberDto.getId());
				User user = new User();
				user.setUserId(userDto.getId());
				partnerTeamMemberViewType.setAdmin(user);
				if (!isVendor) {
					Partnership pship = new Partnership();
					pship.setId(partnershipId);
					partnerTeamMemberViewType.setPartnership(pship);
				}
				partnerTeamMemberViewType.setTeamMember(tMember);
				if (existingUser != null) {
					partnerTeamMemberViewType.setVendorViewType(existingUser.getModulesDisplayType());
					partnerTeamMemberViewType.setViewUpdated(existingUser.isViewUpdated());
					partnerTeamMemberViewType.setCreatedTime(existingUser.getCreatedTime());
					partnerTeamMemberViewType.setUpdatedTime(existingUser.getUpdatedTime());
				} else {
					partnerTeamMemberViewType.setVendorViewType(
							ModulesDisplayType.findByName(teamMemberDto.getViewType()) == null ? ModulesDisplayType.LIST
									: ModulesDisplayType.findByName(teamMemberDto.getViewType()));
					initializeCommonFields(partnerTeamMemberViewType);
				}
				partnerTeamMemberViewTypes.add(partnerTeamMemberViewType);
				logger.debug("View Type Added to the saving list of adminId :- " + userDto.getId()
						+ " and partnershipId :- " + partnershipId + " and teamMemberId :- " + teamMemberDto.getId());
			}
		}
	}

	@Override
	public XtremandResponse isPaymentOverDue(Integer loggedInUserId, String companyProfileName) {
		XtremandResponse response = new XtremandResponse();
		if (org.springframework.util.StringUtils.hasText(companyProfileName)) {
			VanityUrlDetailsDTO vanityUrlDetailsDTO = new VanityUrlDetailsDTO();
			vanityUrlDetailsDTO.setUserId(loggedInUserId);
			vanityUrlDetailsDTO.setVendorCompanyProfileName(companyProfileName);
			vanityUrlDetailsDTO.setVanityUrlFilter(true);
			utilService.isVanityUrlFilterApplicable(vanityUrlDetailsDTO);
			if (vanityUrlDetailsDTO.isVendorLoggedInThroughOwnVanityUrl()) {
				HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
				hibernateSQLQueryResultRequestDTO.setQueryString(
						"select xma.is_payment_over_due  from xt_module_access xma,xt_user_profile xup where xup.company_id  = xma.company_id and xup.user_id  = :userId");
				hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
						.add(new QueryParameterDTO("userId", loggedInUserId));
				boolean isPaymentOverDue = hibernateSQLQueryResultUtilDao
						.returnBoolean(hibernateSQLQueryResultRequestDTO);
				response.setData(isPaymentOverDue);
			} else {
				response.setData(false);
			}
		} else {
			response.setData(false);
		}
		XamplifyUtils.addSuccessStatus(response);
		return response;
	}

	@Override
	public Map<String, Object> loadUserDefaultPage(Integer loggedInUserId, String companyProfileName) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		String dashboardType = "welcome";
		boolean isDefualtPageUpdated = false;
		boolean isCurrentPageDefaultPage = true;
		if (loggedInUserId != null && loggedInUserId > 0) {
			User user = loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, loggedInUserId)),
					new FindLevel[] { FindLevel.SHALLOW });
			if (user != null && user.getUserId() != null) {
				if (UserDefaultPage.WELCOME.equals(user.getUserDefaultPage())) {
					dashboardType = "welcome";
				} else {
					dashboardType = "dashboard";
				}
				isDefualtPageUpdated = user.isDefaultPageUpdated();
				if (StringUtils.isNotBlank(companyProfileName)) {
					boolean isTeamMember = teamDao.isTeamMember(loggedInUserId);
					boolean isPartnerCompany = utilDao.isPartnerCompany(loggedInUserId);
					VanityUrlDetailsDTO vanityUrlDetailsDTO = new VanityUrlDetailsDTO();
					vanityUrlDetailsDTO.setUserId(loggedInUserId);
					vanityUrlDetailsDTO.setVendorCompanyProfileName(companyProfileName);
					vanityUrlDetailsDTO.setVanityUrlFilter(true);
					utilService.isVanityUrlFilterApplicable(vanityUrlDetailsDTO);
					if (vanityUrlDetailsDTO.isPartnerLoggedInThroughVanityUrl() && !isDefualtPageUpdated
							&& (isTeamMember || isPartnerCompany)) {
						isCurrentPageDefaultPage = false;
					}
				}
			}
		}
		resultMap.put("dashboardType", dashboardType);
		resultMap.put("isCurrentPageDefaultPage", isCurrentPageDefaultPage);
		return resultMap;
	}

	@Override
	public XtremandResponse importPartnersFromExternalCSV(Integer companyId) {
		XtremandResponse response = new XtremandResponse();
		File file = new File(existingExternalLeadsPath);
		List<String[]> data = null;
		Map<String, Set<UserDTO>> companiesAndUsersMap = new HashMap<>();
		Set<String> processedEmailds = new HashSet<>();
		try {
			CSVReader reader = new CSVReader(new FileReader(file));
			data = reader.readAll();
			if (data != null) {
				for (int i = 1; i < data.size(); i++) {
					String companyName = data.get(i)[6];
					String firstName = data.get(i)[7];
					String emailId = data.get(i)[8];
					if (StringUtils.isNotBlank(companyName) && StringUtils.isNotBlank(emailId)) {
						if (!companiesAndUsersMap.containsKey(companyName)) {
							processedEmailds.add(emailId);
							Set<UserDTO> users = new LinkedHashSet<>();
							UserDTO user = new UserDTO();
							user.setEmailId(emailId);
							user.setFirstName(firstName);
							user.setContactCompany(companyName);
							users.add(user);
							companiesAndUsersMap.put(companyName, users);
						} else {
							Set<UserDTO> users = companiesAndUsersMap.get(companyName);
							if (users != null && !processedEmailds.contains(emailId)) {
								processedEmailds.add(emailId);
								UserDTO user = new UserDTO();
								user.setEmailId(emailId);
								user.setFirstName(firstName);
								user.setContactCompany(companyName);
								users.add(user);
							}
						}
					}
				}

				if (!companiesAndUsersMap.isEmpty()) {
					createAccountsByCompanyMap(companiesAndUsersMap, companyId);
					logger.debug("Companies and partners are created sucessfully");
					response.setStatusCode(200);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			response.setStatusCode(500);
		}
		return response;
	}

	@SuppressWarnings("unchecked")
	private void createAccountsByCompanyMap(Map<String, Set<UserDTO>> companiesAndUsersMap, Integer vendorcompanyId) {
		int size = companiesAndUsersMap.size();
		int i = 1;
		CompanyProfile vendorCompany = genericDAO.get(CompanyProfile.class, vendorcompanyId);
		Integer vendorAdminId = teamDao.findPrimaryAdminIdByCompanyId(vendorCompany.getId());
		User representingVendor = loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, vendorAdminId)),
				new FindLevel[] { FindLevel.COMPANY_PROFILE });
		Integer defaultPartnerListId = userListDAO.getDefaultPartnerListIdByCompanyId(vendorCompany.getId());
		UserList defaultPartnerList = userListDAO.findByPrimaryKey(defaultPartnerListId,
				new FindLevel[] { FindLevel.COMPANY_PROFILE });
		UserList inActiveMasterPartnerList = null;
		inActiveMasterPartnerList = userListDAO.findInActivePartnerListByCompanyId(vendorCompany.getId());
		String viewType = userDAO.getAdminOrPartnerOrTeamMemberViewType(vendorAdminId, null, null);
		boolean isGdprOn = gdprSettingService.isGdprEnabled(vendorCompany.getId());
		List<LegalBasis> legalBasisList = gdprSettingService.getSelectByDefaultLegalBasis();
		for (Map.Entry<String, Set<UserDTO>> entry : companiesAndUsersMap.entrySet()) {
			logger.debug("No of companies :" + i + " / " + size);
			Set<UserDTO> users = entry.getValue();
			int counter = 0;
			int count = 1;
			Partnership partnership = null;
			if (vendorCompany != null) {
				for (UserDTO user : users) {
					logger.debug("No of users to process " + count + " / " + users.size());
					if (counter == 0) {
						partnership = xamplifyUtilDao.createPartnership(user, vendorCompany, representingVendor,
								vendorAdminId, defaultPartnerList, inActiveMasterPartnerList, viewType, isGdprOn,
								legalBasisList);
						CompanyProfile partnerCompany = partnership.getPartnerCompany();
						User representingPartner = partnership.getRepresentingPartner();
						List<Module> modules = moduleDao.findModuleNames();
						if (partnerCompany != null && representingPartner != null) {
							List<ModuleCustom> moduleCustomsForVendor = utilService
									.getModuleCustomsForVendor(partnership, partnerCompany.getId(), modules);
							if (moduleCustomsForVendor != null && !moduleCustomsForVendor.isEmpty()) {
								xamplifyUtilDao.saveAll(moduleCustomsForVendor, "ModuleCustomsForPartner");
							}
							VanityUrlDetailsDTO vanityUrlDetailsDTO = new VanityUrlDetailsDTO();
							vanityUrlDetailsDTO.setUserId(representingPartner.getUserId());
							XtremandResponse teamMemberGroupResponse = teamMemberGroupService
									.findDefaultModules(vanityUrlDetailsDTO);
							Map<String, Object> map = (Map<String, Object>) teamMemberGroupResponse.getData();
							List<TeamMemberModuleDTO> teamMembersModules = (List<TeamMemberModuleDTO>) map
									.get("modules");
							xamplifyUtilDao.createPartnerGroup(teamMembersModules, partnerCompany.getId());
						}
					} else {
						if (partnership != null) {
							addTeamMembersForPartnerCompany(partnership, user);
						}
					}
					count++;
					counter++;
				}
			}
			i++;
		}
	}

	private void addTeamMembersForPartnerCompany(Partnership partnership, UserDTO user) {
		CompanyProfile partnerCompany = partnership.getPartnerCompany();
		User representingPartner = partnership.getRepresentingPartner();
		Integer companyId = partnerCompany.getId();
		if (partnerCompany != null) {
			TeamMemberGroup partnerTeamMemberGroup = teamMemberGroupDao.findGroupIdsByPartnerCompanyId(companyId);
			Integer teamMemberGroupId = partnerTeamMemberGroup.getId();
			if (teamMemberGroupId != null) {
				List<User> newUsers = new ArrayList<>();
				List<com.xtremand.team.member.dto.TeamMemberDTO> teamMemberDTOs = new ArrayList<>();
				com.xtremand.team.member.dto.TeamMemberDTO teamMember = new com.xtremand.team.member.dto.TeamMemberDTO();
				teamMember.setFirstName(user.getFirstName());
				teamMember.setEmailId(user.getEmailId());
				teamMember.setTeamMemberGroupId(teamMemberGroupId);
				teamMemberDTOs.add(teamMember);
				for (com.xtremand.team.member.dto.TeamMemberDTO teamMemberDTO : teamMemberDTOs) {
					teamMemberGroupId = teamMemberDTO.getTeamMemberGroupId();
					List<Integer> roleIds = teamMemberGroupDao.findRoleIdsByTeamMemberGroupId(teamMemberGroupId);
					UserList teamMemberUserList = new UserList();
					teamService.setNewUsersAndTeamMembersData(representingPartner, newUsers, teamMemberDTO, roleIds,
							teamMemberGroupId, teamMemberUserList);
					if (newUsers != null && !newUsers.isEmpty()) {
						xamplifyUtilDao.saveAll(newUsers, "Team Members");
					}
				}
			}
		}
	}

	@Override
	public XtremandResponse findAllVendorCompanyNames() {
		XtremandResponse response = new XtremandResponse();
		XamplifyUtils.addSuccessStatus(response);
		response.setData(userDAO.findAllVendorCompanyIdAndNames());
		return response;
	}

	@Override
	public XtremandResponse updateExistingLeadsData(Integer companyId) {
		XtremandResponse response = new XtremandResponse();
		File file = new File(existingExternalLeadsPath);
		List<String[]> data = null;
		try {
			CSVReader reader = new CSVReader(new FileReader(file));
			data = reader.readAll();
			if (data != null) {
				int size = data.size();
				for (int i = 1; i < data.size(); i++) {
					logger.debug("No of Users :" + i + " / " + size);
					String amount = data.get(i)[3];
					if (XamplifyUtils.isValidString(amount)) {
						amount = amount.replace("$", "").replace(",", "").trim();
					}
					String closedDate = data.get(i)[4];
					if (XamplifyUtils.isValidString(closedDate)) {
						closedDate = convertClosedDateFormat(closedDate);
					}
				}
				reader.close();
				response.setStatusCode(200);
			}
		} catch (IOException e) {
			e.printStackTrace();
			response.setStatusCode(500);
		}
		return response;
	}

	private String convertClosedDateFormat(String closedDate) {
		SimpleDateFormat inputFormat = new SimpleDateFormat("MM/dd/yyyy");
		SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date date = inputFormat.parse(closedDate);
			closedDate = outputFormat.format(date);
		} catch (ParseException | java.text.ParseException e) {
			e.printStackTrace();
		}
		return closedDate;
	}

	@Override
	public XtremandResponse validateCompany(CompanyProfile companyProfile, User user) {
		XtremandResponse validationResponse = new XtremandResponse();
		CompanyProfile loggedInUserCompany = user.getCompanyProfile();
		Integer loggedInUserCompanyId = (loggedInUserCompany != null && loggedInUserCompany.getId() > 0)
				? loggedInUserCompany.getId()
				: null;
		boolean companyNameExists = companyProfileDao.companyNameExists(loggedInUserCompanyId,
				companyProfile.getCompanyName());
		if (companyNameExists) {
			validationResponse.setStatusCode(400);
			validationResponse.setMessage("Company name has already been added");
		} else {
			validationResponse.setStatusCode(200);
		}
		return validationResponse;
	}

	/** XNFR-781 start **/
	@Override
	public XtremandResponse getApprovalConfigurationSettingsByUserId(Integer userId) {
		XtremandResponse response = new XtremandResponse();
		if (XamplifyUtils.isValidInteger(userId)) {
			Integer companyId = userDAO.getCompanyIdByUserId(userId);
			ApprovalSettingsDTO approvalSettingsDTO = userDAO.getApprovalConfigurationSettingsByCompanyId(companyId);
			if (approvalSettingsDTO != null) {
				XamplifyUtils.addSuccessStatus(response);
				response.setData(approvalSettingsDTO);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse updateApprovalConfigurationSettings(ApprovalSettingsDTO approvalSettingsDTO) {
		XtremandResponse response = new XtremandResponse();
		if (XamplifyUtils.isValidInteger(approvalSettingsDTO.getLoggedInUserId())) {
			Integer companyId = userDAO.getCompanyIdByUserId(approvalSettingsDTO.getLoggedInUserId());
			if (XamplifyUtils.isValidInteger(companyId)) {
				approvalSettingsDTO.setCompanyId(companyId);
				Integer updatedCountForCompany = approveDao
						.updateApprovalConfigurationSettingsForCompany(approvalSettingsDTO);
				if (XamplifyUtils.isValidInteger(updatedCountForCompany)) {
					XamplifyUtils.addSuccessStatus(response);
				}
			}
		}
		return response;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void approvePendingAssetsAndTimeLineStatusHistory(ApprovalSettingsDTO approvalSettingsDTO,
			Map<String, Object> approvalsMap) {

		if (XamplifyUtils.isValidInteger(approvalSettingsDTO.getCompanyId())
				&& XamplifyUtils.isValidInteger(approvalSettingsDTO.getLoggedInUserId())) {
			Integer loggedInUserId = approvalSettingsDTO.getLoggedInUserId();
			List<Integer> pendingParentVersionDamIds = approveDao
					.getPendingStateParentVersionDamIdsByCompanyId(approvalSettingsDTO.getCompanyId());
			List<Integer> pendingReApprovalVersionDamIds = approveDao
					.getPendingStateReApprovalVersionDamIdsByCompanyId(approvalSettingsDTO.getCompanyId());
			pendingParentVersionDamIds.removeAll(pendingReApprovalVersionDamIds);
			if (XamplifyUtils.isNotEmptyList(pendingParentVersionDamIds)) {
				approveDao.approvePendingAssetsByCompanyIdAndDamIds(approvalSettingsDTO.getCompanyId(),
						approvalSettingsDTO.getLoggedInUserId(), pendingParentVersionDamIds);
				userDAO.approveInTimeLineStatusHistoryByCompanyIdAndModuleType(approvalSettingsDTO.getCompanyId(),
						ModuleType.DAM.name());
			}
			/** XNFR-885 **/
			if (XamplifyUtils.isNotEmptyList(pendingReApprovalVersionDamIds)) {

				List<ContentReApprovalDTO> contentReApprovalDTOs = damDao
						.getAssetDetailsForReApproval(pendingReApprovalVersionDamIds);

				List<Integer> whiteLabeledReApprovalDamIds = contentReApprovalDTOs.stream()
						.filter(ContentReApprovalDTO::isWhiteLabeledAssetSharedWithPartners)
						.map(ContentReApprovalDTO::getApprovalReferenceId).collect(Collectors.toList());
				approvalsMap.put(WHITE_LABELED_RE_APPROVAL_DAM_IDS, whiteLabeledReApprovalDamIds);

				List<ContentReApprovalDTO> pdfTypeAssetContentDetails = contentReApprovalDTOs.stream()
						.filter(dto -> "pdf".equalsIgnoreCase(dto.getAssetType())).collect(Collectors.toList());

				contentReApprovalDTOs.forEach(dto -> dto.setLoggedInUserId(loggedInUserId));
				List<ContentReApprovalDTO> videoTypeAssetContentDetails = contentReApprovalDTOs.stream()
						.filter(dto -> dto.getVideoId() != null).collect(Collectors.toList());
				List<ContentReApprovalDTO> nonVideoTypeAssetContentDetails = contentReApprovalDTOs.stream()
						.filter(dto -> dto.getVideoId() == null).collect(Collectors.toList());
				List<DamTag> allDamTagsToSave = new ArrayList<>();
				List<ApprovalStatusHistory> approvalHistoryList = new ArrayList<>();
				List<Integer> damIdsToDelete = approveService.processNonVideoAssetReApprovalAndGetIds(loggedInUserId,
						allDamTagsToSave, nonVideoTypeAssetContentDetails, approvalHistoryList, "");
				approvalsMap.put("damIdsToDelete", damIdsToDelete);
				approvalsMap.put("allDamTagsToSave", allDamTagsToSave);

				Map<String, Object> videoMap = approveService.handleReApprovalVersionForVideoTypeAsset(loggedInUserId,
						videoTypeAssetContentDetails, approvalHistoryList, "");
				List<VideoTag> videoTagsToSave = (List<VideoTag>) videoMap.get(VIDEO_TAGS_TO_SAVE_MAP_KEY);
				List<Integer> videoIdsToDelete = (List<Integer>) videoMap.get(VIDEO_IDS_TO_DELETE_MAP_KEY);
				approvalsMap.put(VIDEO_TAGS_TO_SAVE_MAP_KEY, videoTagsToSave);
				approvalsMap.put(VIDEO_IDS_TO_DELETE_MAP_KEY, videoIdsToDelete);

				approvalsMap.put("approvalHistoryList", approvalHistoryList);

				performApprovalInsertionOperations(approvalsMap);

				approveService.handleSharedAssetPathForPdfTypeAssets(pdfTypeAssetContentDetails);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void performApprovalInsertionOperations(Map<String, Object> approvalsMap) {

		List<ApprovalStatusHistory> approvalHistoryList = (List<ApprovalStatusHistory>) approvalsMap
				.get("approvalHistoryList");
		List<Integer> videoIdsToDelete = (List<Integer>) approvalsMap.get(VIDEO_IDS_TO_DELETE_MAP_KEY);
		List<Integer> damIdsToDelete = (List<Integer>) approvalsMap.get("damIdsToDelete");
		List<DamTag> allDamTagsToSave = (List<DamTag>) approvalsMap.get("allDamTagsToSave");
		List<VideoTag> videoTagsToSave = (List<VideoTag>) approvalsMap.get(VIDEO_IDS_TO_DELETE_MAP_KEY);

		if (XamplifyUtils.isNotEmptyList(approvalHistoryList)) {
			xamplifyUtilDao.saveAll(approvalHistoryList, "Approval Status History");
		}

		if (XamplifyUtils.isNotEmptyList(videoIdsToDelete)) {
			videoDao.deleteVideoRecordsByIds(videoIdsToDelete);
		}

		if (XamplifyUtils.isNotEmptyList(damIdsToDelete)) {
			damDao.deleteByDamIds(damIdsToDelete);
		}

		if (XamplifyUtils.isNotEmptyList(allDamTagsToSave)) {
			xamplifyUtilDao.saveAll(allDamTagsToSave, "Dam Tag for Re-Approval");
		}

		if (XamplifyUtils.isNotEmptyList(videoTagsToSave)) {
			xamplifyUtilDao.saveAll(videoTagsToSave, "VideoTag for Re-Approval");
		}
	}

	@Override
	public void approvePendingTracksOrPlaybooksByModuleTypeAndTimeLineStatusHistory(
			ApprovalSettingsDTO approvalSettingsDTO, ModuleType moduleType) {
		if (XamplifyUtils.isValidInteger(approvalSettingsDTO.getCompanyId())
				&& XamplifyUtils.isValidInteger(approvalSettingsDTO.getLoggedInUserId())
				&& XamplifyUtils.isValidString(moduleType.name())) {
			userDAO.approvePendingTracksOrPlaybooksByModuleType(approvalSettingsDTO.getCompanyId(),
					approvalSettingsDTO.getLoggedInUserId(), moduleType.name());
			userDAO.approveInTimeLineStatusHistoryByCompanyIdAndModuleType(approvalSettingsDTO.getCompanyId(),
					moduleType.name());
		}
	}

	/** XNFR-781 end **/

	@Override
	public XtremandResponse getNonExistingUsersfromCSV(Integer companyId) {
		XtremandResponse response = new XtremandResponse();
		File file = new File(externalColtCsvPath);
		List<String[]> data = null;
		Set<String> nonRegisteredUsers = new HashSet<>();
		try {
			CSVReader reader = new CSVReader(new FileReader(file));
			data = reader.readAll();
			if (data != null) {
				int size = data.size();
				for (int i = 1; i < data.size(); i++) {
					logger.debug("No of Users :" + i + " / " + size);
				}
				reader.close();
				for (String emailId : nonRegisteredUsers) {
					logger.debug(emailId);
				}
				logger.debug("getNonRegisteredUsersfromCSV() executed sucessfully");
				response.setStatusCode(200);
			}
		} catch (IOException e) {
			e.printStackTrace();
			response.setStatusCode(500);

		}
		return response;
	}

	@Override
	public XtremandResponse saveTeamMemberFilter(Integer userId, Integer filterType) {
		XtremandResponse response = new XtremandResponse();
		boolean filterOption = false;
		try {
			if (filterType != null && XamplifyUtils.isValidInteger(userId)) {
				filterOption = filterType == 1;
				userDAO.updateTeammemberFilterOption(userId, filterOption);
				XamplifyUtils.addSuccessStatusWithMessage(response, "Updated successfully");
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatusCode(500);
		}
		return response;
	}

	@Override
	public XtremandResponse createTeamMembersByCompanyId(Integer companyId) {
		XtremandResponse response = new XtremandResponse();
		File file = new File(externalTeamMemberEmailsCsvPath);
		List<TeamMemberGroupDTO> defaultTeamMemerGroups = teamMemberGroupDao.findDefaultGroupsByCompanyId(companyId);
		Integer teamMemberGroupId = defaultTeamMemerGroups.get(0).getId();
		Integer primaryAdminId = teamDao.findPrimaryAdminIdByCompanyId(companyId);
		User adminUser = userDAO.findByPrimaryKey(primaryAdminId,
				new FindLevel[] { FindLevel.ROLES, FindLevel.COMPANY_PROFILE });
		List<Integer> roleIds = teamMemberGroupDao.findRoleIdsByTeamMemberGroupId(teamMemberGroupId);
		Map<String, String> addNewUsers = new HashMap<>();
		List<String[]> data = null;
		CSVReader reader = null;
		try {
			reader = new CSVReader(new FileReader(file));
			data = reader.readAll();
			if (data != null) {
				int size = data.size();
				for (int i = 1; i < data.size(); i++) {
					String noOfEmails = "No of Emails :" + i + " / " + size;
					logger.debug(noOfEmails);
					String emailId = data.get(i)[0];
					if (XamplifyUtils.isValidString(emailId)) {
						boolean isUserExists = checkIfUserExistsByEmailId(emailId);
						if (!isUserExists) {
							createTeamMemberByEmailId(emailId, teamMemberGroupId, adminUser, roleIds, addNewUsers);
						}
					}
				}
				reader.close();
				response.setStatusCode(200);
				addNewUsers.forEach((key, value) -> logger.debug(" Email Id " + key + " " + " User Id " + value));
			}
		} catch (IOException e) {
			e.printStackTrace();
			response.setStatusCode(500);
		} finally {
			logger.debug("createTeamMembersByCompanyId() executed sucessfully");
		}
		return response;
	}

	private boolean checkIfUserExistsByEmailId(String emailId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String sqlQuery = "SELECT CASE WHEN EXISTS (SELECT 1 FROM xt_user_profile WHERE email_id = :emailId and company_id is not null) THEN TRUE ELSE FALSE END AS result";
		hibernateSQLQueryResultRequestDTO.setQueryString(sqlQuery);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("emailId", emailId));
		return (boolean) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
	}

	public void createTeamMemberByEmailId(String emailId, Integer teamMemberGroupId, User adminUser,
			List<Integer> roleIds, Map<String, String> addNewUsers) {
		if (XamplifyUtils.isValidInteger(teamMemberGroupId)) {
			List<User> newUsers = new ArrayList<>();
			com.xtremand.team.member.dto.TeamMemberDTO teamMember = new com.xtremand.team.member.dto.TeamMemberDTO();
			teamMember.setEmailId(emailId);
			teamMember.setTeamMemberGroupId(teamMemberGroupId);
			UserList teamMemberUserList = new UserList();
			teamService.setNewUsersAndTeamMembersData(adminUser, newUsers, teamMember, roleIds, teamMemberGroupId,
					teamMemberUserList);
			if (!newUsers.isEmpty()) {
				for (User newUser : newUsers) {
					genericDAO.save(newUser);
					String userId = newUser.getUserId().toString();
					addNewUsers.put(emailId, userId);
				}
			}
		}
	}

	@Override
	public void setTeamMemberPartnerFilter(UserDTO userDTO, Integer userId) {
		if (XamplifyUtils.isValidInteger(userId) && teamDao.isTeamMember(userId)) {
			userDTO.setPartnerFilter(teamDao.getTeamMemberOption(userId));
		} else {
			userDTO.setPartnerFilter(false);
		}
	}

}