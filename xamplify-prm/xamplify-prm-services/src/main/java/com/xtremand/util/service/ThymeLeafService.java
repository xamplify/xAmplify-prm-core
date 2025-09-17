package com.xtremand.util.service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.HibernateException;
import org.jcodec.common.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;

import com.xtremand.activity.dao.EmailActivityDAO;
import com.xtremand.activity.dto.EmailActivityRequestDTO;
import com.xtremand.activity.dto.EmailMergeTagDTO;
import com.xtremand.approve.dao.ApproveDAO;
import com.xtremand.approve.dto.ApprovalPrivilegesEmailNotificationDTO;
import com.xtremand.approve.dto.PendingApprovalDamAndLmsDTO;
import com.xtremand.approve.dto.TeamMemberApprovalPrivilegesDTO;
import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.company.dao.CompanyProfileDao;
import com.xtremand.company.dto.EmailNotificationSettingsDTO;
import com.xtremand.dam.dto.ApprovalStatusHistoryDTO;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.dashboard.buttons.dto.DashboardButtonsEmailNotificationRequestDTO;
import com.xtremand.deal.dto.DealDto;
import com.xtremand.exception.EmailNotificationException;
import com.xtremand.form.bom.FormDefaultFieldTypeEnum;
import com.xtremand.formbeans.LeadDTO;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.formbeans.VendorInvitationDTO;
import com.xtremand.highlevel.analytics.dto.DownloadRequestUserDetailsDTO;
import com.xtremand.lead.bom.Lead;
import com.xtremand.lead.dao.LeadDAO;
import com.xtremand.lead.dto.LeadDto;
import com.xtremand.lms.bom.LearningTrack;
import com.xtremand.lms.bom.LearningTrackType;
import com.xtremand.mail.service.MailService;
import com.xtremand.mail.service.MailService.EmailBuilder;
import com.xtremand.module.dao.ModuleDao;
import com.xtremand.salesforce.dto.SfCustomFieldsDataDTO;
import com.xtremand.signup.dto.SignUpRequestDTO;
import com.xtremand.team.dao.TeamDao;
import com.xtremand.team.member.dto.PartnerPrimaryAdminUpdateDto;
import com.xtremand.team.member.dto.TeamMemberListDTO;
import com.xtremand.user.bom.User;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.user.service.UserService;
import com.xtremand.userlist.dao.UserListDAO;
import com.xtremand.util.BadRequestException;
import com.xtremand.util.XamplifyUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.dto.ActiveAndInActivePartnerEmailNotificationDTO;
import com.xtremand.util.dto.ContentSharedEmailNotificationDTO;
import com.xtremand.util.dto.ModuleCustomDTO;
import com.xtremand.util.dto.ModulesEmailNotification;
import com.xtremand.util.dto.UserDetailsUtilDTO;
import com.xtremand.vanity.email.templates.bom.CustomDefaultEmailTemplate;
import com.xtremand.vanity.email.templates.bom.DefaultEmailTemplate;
import com.xtremand.vanity.email.templates.bom.DefaultEmailTemplateType;
import com.xtremand.vanity.url.dao.VanityURLDao;

@Service
@Transactional
public class ThymeLeafService {

	private static final String LEAD_COMPANY = "{{leadCompany}}";

	private static final String LEAD_NAME = "{{leadName}}";

	private static final String VANITY_COMPANY_LOGO_HREF = "<Vanity_Company_Logo_Href>";

	private static final String MODULE_TYPE = "moduleType";

	private static final String THERE = "there";

	private static final String PARTNER_EMAIL_ID = "partnerEmailId";

	private static final String PARTNER_EMAIL = "partnerEmail";

	private static final String PARTNER_COMPANY_NAME = "partnerCompanyName";

	private static final String X_AMPLIFY_LOGO = "xAmplifyLogo";

	private static final String COMPANY_NAME = "companyName";

	private static final String VENDOR_COMPANY_NAME = "vendorCompanyName";

	private static final String EMAIL_TRACKING_INFORMATION_EMAIL_ACTIVITY_ID = "xamplify-prm-api//email/trackingInformation?emailActivityId=";

	private static final String UPDATE_TEXT_FOR_SUBJECT = "Update: ";

	private static final String APPROVAL_REMINDER_MESSAGE = "reminderMessage";

	@Autowired
	private SpringTemplateEngine templateEngine;

	@Value("${fromEmail}")
	private String fromEmail;

	@Autowired
	private MailService mailService;

	@Value("${spring.profiles.active}")
	private String profiles;

	@Value("${qa.dev.emailIds}")
	private String senderEmailIds;

	@Value("${web_url}")
	private String webUrl;

	@Value("${replace.there}")
	private String replaceThere;

	@Value("${server_path}")
	String serverPath;

	@Value("${xamplify.logo}")
	private String xamplifyLogo;

	@Value("${mail.sender}")
	String fromName;

	@Value("${system.notification.message}")
	private String systemNotificationPrefixMessage;

	/** XNFR-553 **/
	@Value("${server_url}")
	private String serverUrl;

	@Value("${no.salesforce.found.for.sso.onboarding.email.subject}")
	private String noSalesforceFoundForSsoOnboardingEmailSubject;

	@Value("${no.salesforce.account.found.for.lead.email.subject}")
	private String noSalesforceAccountFoundForLeadEmailSubject;

	@Autowired
	private XamplifyUtil xamplifyUtil;

	@Autowired
	private UserService userService;

	@Autowired
	private TeamDao teamDao;

	@Autowired
	private UserDAO userDao;

	@Autowired
	private UtilService utilService;

	@Autowired
	private ModuleDao moduleDao;

	@Autowired
	private UserListDAO userListDao;

	@Autowired
	private CompanyProfileDao companyDao;

	@Autowired
	private ApproveDAO approveDao;

	@Autowired
	private GenericDAO genericDao;

	@Autowired
	private LeadDAO leadDAO;

	private static final String PRODUCTION = "production";

	private static final String ACTIVE_AND_INACTIVE_PARTNER_NOTIFICATION_DTO = "activeAndInActivePartnerEmailNotificationDTO";

	private static final String WELCOME_DISPLAY_NAME = "welcomeDisplayName";

	private static final String XAMPLIFY_TEXT = "xAmplify";

	private static final String EMAIL_ID = "emailId";

	private static final String TARGET_URL = "targetURL";

	private static final String LOGIN = "login";

	private static final String UNABLE_TO_SEND_DASHBOARD_BUTTON = "Unable To Send Dashboard Button Published Email";

	/** XNFR-553 **/
	private static final String EMAIL_TRACKING_IMAGE_WITH_DIV = "<div id=\"bottomDiv\"><img src=\"<emailOpenImgURL>\" alt=\"\" class='backup_picture' style='display:none'></div>";

	private static final String ENCODED_EMAIL_OPEN_IMAGE_URL = "&lt;emailOpenImgURL&gt;";

	String emailOpenImgUrl = "<emailOpenImgURL>";

	/** XNFR-867 **/
	@Autowired
	private EmailActivityDAO emailActivityDAO;

	private static final String TO_NAME = "toName";

	/** XNFR-892 **/
	@Autowired
	private UtilDao utilDao;

	@Autowired
	private VanityURLDao vanityURLDao;

	@Value("${replace.company.logo}")
	private String replaceCompanyLogo;

	public void sendModulesUpdateEmailNotification(CompanyProfile companyProfile,
			ModulesEmailNotification modulesEmailNotification) {
		if (modulesEmailNotification.sendNotification()) {
			Context context = new Context();
			Map<String, Object> model = new HashMap<>();
			model.put("modulesEmailNotification", modulesEmailNotification);
			context.setVariables(model);
			String htmlName = "module-update-notification";
			String subject = "Modules Updated For " + companyProfile.getCompanyName() + "(" + companyProfile.getId()
					+ ")" + "(" + getSubjectSuffix(profiles) + ")";
			String html = templateEngine.process(htmlName, context);
			sendEmailNotification(subject, html);
		}

	}

	public void sendReferAVendorInvitationEmailNotification(VendorInvitationDTO vendorInvitationDTO, User sender) {
		Context context = new Context();
		Map<String, Object> model = new HashMap<>();
		String invitedBy = sender.getEmailId() + "( " + sender.getCompanyProfile().getCompanyName() + " )";
		model.put("invitedBy", invitedBy);
		model.put("inviteeEmailIds", vendorInvitationDTO.getEmailIds());
		context.setVariables(model);
		String htmlName = "refer-a-vendor-notification";
		String subject = "Refer A Vendor Notification ";
		String html = templateEngine.process(htmlName, context);
		sendEmailNotification(subject, html);

	}

	private void sendEmailNotification(String subject, String html) {
		List<String> emailIds = XamplifyUtils.convertStringToArrayList(senderEmailIds);
		for (String emailId : emailIds) {
			mailService.sendMail(new EmailBuilder().from(fromEmail).senderName(XAMPLIFY_TEXT).to(emailId)
					.subject(subject).body(html).build());
		}
	}

	public void sendActiveAndInActivePartnerEmailNotification(
			ActiveAndInActivePartnerEmailNotificationDTO activeAndInActivePartnerEmailNotificationDTO) {
		boolean atleastOneActivePartner = activeAndInActivePartnerEmailNotificationDTO.getActivePartners() != null
				&& !activeAndInActivePartnerEmailNotificationDTO.getActivePartners().isEmpty();
		boolean atleastOnInActivePartner = activeAndInActivePartnerEmailNotificationDTO.getInActivePartners() != null
				&& !activeAndInActivePartnerEmailNotificationDTO.getInActivePartners().isEmpty();
		if (atleastOneActivePartner || atleastOnInActivePartner) {
			Context context = new Context();
			Map<String, Object> model = new HashMap<>();
			model.put(ACTIVE_AND_INACTIVE_PARTNER_NOTIFICATION_DTO, activeAndInActivePartnerEmailNotificationDTO);
			context.setVariables(model);
			String htmlName = "inactive-and-active-partner-notification";
			String subject = "Active / Inactive Partner Master List Is Updated For "
					+ activeAndInActivePartnerEmailNotificationDTO.getSubject() + "" + "(" + getSubjectSuffix(profiles)
					+ ")";
			String html = templateEngine.process(htmlName, context);
			sendEmailNotification(subject, html);
		}

	}

	public void sendActivePartnerEmailNotification(
			ActiveAndInActivePartnerEmailNotificationDTO activeAndInActivePartnerEmailNotificationDTO) {
		boolean atleastOneActivePartner = activeAndInActivePartnerEmailNotificationDTO.getActivePartners() != null
				&& !activeAndInActivePartnerEmailNotificationDTO.getActivePartners().isEmpty();
		if (atleastOneActivePartner) {
			Context context = new Context();
			Map<String, Object> model = new HashMap<>();
			model.put(ACTIVE_AND_INACTIVE_PARTNER_NOTIFICATION_DTO, activeAndInActivePartnerEmailNotificationDTO);
			context.setVariables(model);
			String htmlName = "active-partner-notification";
			String subject = "Active Partner Master List Is Updated For "
					+ activeAndInActivePartnerEmailNotificationDTO.getSubject() + "" + "(" + getSubjectSuffix(profiles)
					+ ")";
			String html = templateEngine.process(htmlName, context);
			sendEmailNotification(subject, html);
		}

	}

	public void sendInActivePartnerEmailNotification(
			ActiveAndInActivePartnerEmailNotificationDTO activeAndInActivePartnerEmailNotificationDTO) {
		boolean atleastOnInActivePartner = activeAndInActivePartnerEmailNotificationDTO.getInActivePartners() != null
				&& !activeAndInActivePartnerEmailNotificationDTO.getInActivePartners().isEmpty();
		if (atleastOnInActivePartner) {
			Context context = new Context();
			Map<String, Object> model = new HashMap<>();
			model.put(ACTIVE_AND_INACTIVE_PARTNER_NOTIFICATION_DTO, activeAndInActivePartnerEmailNotificationDTO);
			context.setVariables(model);
			String htmlName = "inactive-partner-notification";
			String subject = "Inactive Partner Master List Is Updated For "
					+ activeAndInActivePartnerEmailNotificationDTO.getSubject() + "" + "(" + getSubjectSuffix(profiles)
					+ ")";
			String html = templateEngine.process(htmlName, context);
			sendEmailNotification(subject, html);
		}

	}

	public String getSubjectSuffix(String profiles) {
		String messageSuffix = "";
		switch (profiles) {
		case "dev":
			messageSuffix = "DEV" + "-" + findMachineName();
			break;

		case "qa":
			messageSuffix = "QA";
			break;

		case PRODUCTION:
			messageSuffix = "PRODUCTION";
			break;

		default:
			messageSuffix = "";
			break;

		}
		return messageSuffix;
	}

	public String findMachineName() {
		InetAddress ip;
		String hostname;
		try {
			ip = InetAddress.getLocalHost();
			hostname = ip.getHostName();
		} catch (UnknownHostException e) {
			hostname = "*******";
		}
		return hostname;

	}

	private void processTemplateAndSendEmailNotification(String receiverEmailId, Context context, String htmlName,
			String subject, Map<String, Object> model) {
		context.setVariables(model);
		String html = templateEngine.process(htmlName, context);
		mailService.sendMail(new EmailBuilder().from(fromEmail).senderName(XAMPLIFY_TEXT).to(receiverEmailId)
				.subject(subject).body(html).build());
	}

	private void addWelcomeDisplayName(String displayName, Map<String, Object> model) {
		model.put(WELCOME_DISPLAY_NAME, StringUtils.hasText(displayName) ? displayName : replaceThere);
	}

	/*** XNFR -128 ****/
	public void sendHighLevelAnalyticsLinkEmailNotification(DownloadRequestUserDetailsDTO userDetailsUtilDTO,
			String path) {
		/*** XNFR-930 **/
		String cloudFrontUrl = xamplifyUtil.replaceS3WithCloudfrontViceVersa(path);
		/*** XNFR-930 **/
		Context context = new Context();
		Map<String, Object> model = new HashMap<>();
		String welcomeDisplayName = userDetailsUtilDTO != null && StringUtils.hasText(userDetailsUtilDTO.getFullName())
				? userDetailsUtilDTO.getFullName()
				: replaceThere;
		if (userDetailsUtilDTO != null) {
			String emailId = userDetailsUtilDTO.getEmailId();
			model.put(WELCOME_DISPLAY_NAME, welcomeDisplayName);
			model.put(EMAIL_ID, emailId);
			model.put("filePath", cloudFrontUrl);
			model.put("requestedOn", new Date());
			model.put("headerImage", webUrl + "assets/images/high-level-page.png");
			context.setVariables(model);
			String htmlName = "high-level-analytics-report";
			String subject = utilService.addPerfixToSubject("High Level Analytics Excel");
			String html = templateEngine.process(htmlName, context);
			String userEmailId = userDetailsUtilDTO.getEmailId();
			mailService.sendMail(new EmailBuilder().from(fromEmail).senderName(XAMPLIFY_TEXT).to(userEmailId)
					.subject(subject).body(html).build());
		}

	}

	/******** XNFR-224 ********/
	public void sendLoginAsPartnerEmailNotification(UserDTO partnerCompanyUserDTO, UserDTO vendorCompanyUserDTO,
			String domainName, String vendorCompanyName) {
		Context context = new Context();
		Map<String, Object> model = new HashMap<>();
		addWelcomeDisplayName(partnerCompanyUserDTO.getFullName(), model);
		model.put(VENDOR_COMPANY_NAME, vendorCompanyName);
		String hostUrl = xamplifyUtil.getVaniryOrXamplifyDomainUrl(domainName);
		model.put("hostUrl", hostUrl);
		model.put(TARGET_URL, hostUrl + LOGIN);
		model.put(X_AMPLIFY_LOGO, xamplifyLogo);
		String vendorCompanyUserFullName = vendorCompanyUserDTO.getFullName();
		String loggedInVendorCompanyUser = StringUtils.hasText(vendorCompanyUserFullName)
				? vendorCompanyUserFullName + "(" + vendorCompanyUserDTO.getEmailId() + "),"
				: vendorCompanyUserDTO.getEmailId();
		model.put("loggedInVendorCompanyUser", loggedInVendorCompanyUser);
		String subject = utilService.addPerfixToSubject("Your vendor has logged in as you");
		processTemplateAndSendEmailNotification(partnerCompanyUserDTO.getEmailId(), context, "login-as-notification",
				subject, model);

	}

	public String getRequestDemoSubjectSuffix(String profiles) {
		String messageSuffix = "";
		switch (profiles) {
		case PRODUCTION:
			messageSuffix = "";
			break;

		default:
			messageSuffix = "";
			break;

		}
		return messageSuffix;
	}

	public void sendAssetSharedWithNewPartnersEmailNotificationToVendorCompany(
			ContentSharedEmailNotificationDTO contentSharedEmailNotificationDTO) {
		Context context = new Context();
		Map<String, Object> model = new HashMap<>();
		String welcomeDisplayName = contentSharedEmailNotificationDTO != null
				&& StringUtils.hasText(contentSharedEmailNotificationDTO.getReceiverName())
						? contentSharedEmailNotificationDTO.getReceiverName()
						: replaceThere;
		model.put(WELCOME_DISPLAY_NAME, welcomeDisplayName);
		if (contentSharedEmailNotificationDTO != null) {
			boolean isPublishedToPartnerList = contentSharedEmailNotificationDTO.isPublishedToPartnerList();
			if (isPublishedToPartnerList) {
				model.put("partnerListName", contentSharedEmailNotificationDTO.getPartnerListName());
			} else {
				model.put(PARTNER_COMPANY_NAME, contentSharedEmailNotificationDTO.getPartnerCompanyName());
				model.put(PARTNER_EMAIL, contentSharedEmailNotificationDTO.getPartnerEmailId());
			}
			model.put("isPublishedToPartnerList", isPublishedToPartnerList);
			model.put("assetNames", contentSharedEmailNotificationDTO.getNames());
			String subject = utilService.addPerfixToSubject("Asset(s) Shared Successfully");
			processTemplateAndSendEmailNotification(contentSharedEmailNotificationDTO.getReceiverEmailId(), context,
					"asset-shared-vendor-notification", subject, model);
		}

	}

	public void sendVendorNotificationForSharedTrackOrPlayBook(
			ContentSharedEmailNotificationDTO contentSharedEmailNotificationDTO) {
		if (contentSharedEmailNotificationDTO == null) {
			return;
		}
		Context context = new Context();
		Map<String, Object> model = new HashMap<>();

		String welcomeDisplayName = getWelcomeDisplayName(contentSharedEmailNotificationDTO, replaceThere);
		model.put(WELCOME_DISPLAY_NAME, welcomeDisplayName);
		/*** XNFR-688 ***/
		if (contentSharedEmailNotificationDTO.isVendorEmailNotificationRequired()) {
			model.put("partnerListName", contentSharedEmailNotificationDTO.getPartnerListName());
			model.put("trackOrPlayBookNames", contentSharedEmailNotificationDTO.getNames());
			model.put("moduleName", contentSharedEmailNotificationDTO.getModuleName());

			String subject = utilService
					.addPerfixToSubject(contentSharedEmailNotificationDTO.getModuleName() + "(s) Shared Successfully");
			processTemplateAndSendEmailNotification(contentSharedEmailNotificationDTO.getReceiverEmailId(), context,
					"track-or-playbook-shared-vendor-notification", subject, model);
		} else {
			String debugMessage = contentSharedEmailNotificationDTO.getModuleName()
					+ " Published Email Notification Skipped For Vendor Company.";
			Logger.debug(debugMessage);
		}
	}

	private String getWelcomeDisplayName(ContentSharedEmailNotificationDTO contentSharedEmailNotificationDTO,
			String replaceThere) {
		return contentSharedEmailNotificationDTO != null
				&& StringUtils.hasText(contentSharedEmailNotificationDTO.getReceiverName())
						? contentSharedEmailNotificationDTO.getReceiverName()
						: replaceThere;
	}

	/**** XNFR-454 *****/
	public void sendTeamMemberSignedUpEmailNotificationsToAdmins(SignUpRequestDTO signUpRequestDto) {
		Integer companyId = signUpRequestDto.getCompanyId();
		String teamMemberEmailId = signUpRequestDto.getEmailId().trim().toLowerCase();
		List<TeamMemberListDTO> dtos = teamDao.findPrimaryAdminAndExtraAdmins(companyId);
		for (TeamMemberListDTO dto : dtos) {
			Context context = new Context();
			Map<String, Object> model = new HashMap<>();
			addWelcomeDisplayName(dto.getFullName(), model);
			model.put(X_AMPLIFY_LOGO, xamplifyLogo);
			model.put(TARGET_URL, webUrl + LOGIN);
			model.put("teamMemberEmailId", teamMemberEmailId);
			processTemplateAndSendEmailNotification(dto.getEmailId(), context, "team-member-signedup-notification",
					systemNotificationPrefixMessage + "Team member signed up successfully", model);
		}
	}

	public void sendPartnerSignedUpEmailNotificationsToAdmins(SignUpRequestDTO signUpRequestDto) {
		Integer companyId = signUpRequestDto.getCompanyId();
		String partnerEmailId = signUpRequestDto.getEmailId().trim().toLowerCase();
		String companyName = signUpRequestDto.getCompanyName().trim();
		List<TeamMemberListDTO> dtos = teamDao.findPrimaryAdminAndExtraAdmins(companyId);
		String partnerModuleCustomName = "Partner";
		ModuleCustomDTO moduleCustomDTO = moduleDao.findPartnerModuleByCompanyId(companyId);
		if (moduleCustomDTO != null) {
			partnerModuleCustomName = moduleCustomDTO.getCustomName();
		}
		String companyWebsiteUrl = userDao.getCompanyWebSiteUrlByCompanyId(companyId);
		if (companyWebsiteUrl == null) {
			companyWebsiteUrl = "";
		}
		String updatedCompanyWebsiteUrl = xamplifyUtil.addHttpOrHttpsProtocolAsPrefix(companyWebsiteUrl);
		String subjectLine = systemNotificationPrefixMessage + partnerModuleCustomName + " has been added successfully";
		sendEmailNotificationsToVendorAdmins(partnerEmailId, companyName, dtos, subjectLine, companyId, false);
		sendEmailNotificationToPartner(signUpRequestDto, companyId, partnerEmailId, companyName,
				updatedCompanyWebsiteUrl);

	}

	private void sendEmailNotificationToPartner(SignUpRequestDTO signUpRequestDto, Integer companyId,
			String partnerEmailId, String companyName, String updatedCompanyWebsiteUrl) {
		boolean isUserExcluded = false;
		boolean isUnsubscribed = false;
		Integer partnerId = userDao.getUserIdByEmail(partnerEmailId);
		if (XamplifyUtils.isValidInteger(partnerId)) {
			User user = new User();
			user.setEmailId(partnerEmailId);
			user.setUserId(partnerId);
			isUserExcluded = utilService.isUserExcluded(user, companyId);
			isUnsubscribed = userDao.isUserUnsubscribedForCompany(partnerId, companyId);
		}
		if (!isUserExcluded && !isUnsubscribed) {
			String vendorCompanyName = userDao.getCompanyNameByCompanyId(companyId).getCompanyName();
			Context context = new Context();
			Map<String, Object> model = new HashMap<>();
			addWelcomeDisplayName(signUpRequestDto.getFirstName(), model);
			boolean isSignedUpUsingVendorVanityUrl = signUpRequestDto.isAccessedFromVanityDomain();
			if (isSignedUpUsingVendorVanityUrl) {
				String vendorCompanyLogoPath = serverPath
						+ XamplifyUtils.escapeDollarSequece(userDao.getCompanyLogoPath(companyId));
				model.put(X_AMPLIFY_LOGO, vendorCompanyLogoPath);
				model.put(TARGET_URL, updatedCompanyWebsiteUrl);
				/***** XBI-2587 ****/
				String vanityLogin = xamplifyUtil.frameVanityURL(webUrl, signUpRequestDto.getCompanyProfileName());
				model.put("loginUrl", vanityLogin + LOGIN);
			} else {
				model.put(X_AMPLIFY_LOGO, xamplifyLogo);
				model.put(TARGET_URL, webUrl + LOGIN);
				model.put("loginUrl", webUrl + LOGIN);
			}
			model.put(PARTNER_EMAIL_ID, partnerEmailId);
			model.put(COMPANY_NAME, companyName);
			boolean isPasswordUpdated = signUpRequestDto.isPasswordUpdated();
			model.put("isPasswordUpdated", isPasswordUpdated);
			if (isPasswordUpdated) {
				model.put("password", signUpRequestDto.getPassword());
			}
			model.put(VENDOR_COMPANY_NAME, vendorCompanyName);
			processTemplateAndSendEmailNotification(partnerEmailId, context, "signed-up-as-partner-notification",
					systemNotificationPrefixMessage + "Partnership established successfully", model);
		}
	}

	private void sendEmailNotificationsToVendorAdmins(String partnerEmailId, String companyName,
			List<TeamMemberListDTO> dtos, String subjectLine, Integer companyId,
			boolean isNoSalesforceAccountFoundForSSOUserEmail) {
		boolean isPartnerOnBoardVendorEmailNotificationEnabled = companyDao
				.isPartnerOnBoardVendorEmailNotificationEnabledByCompanyId(companyId);
		if (isNoSalesforceAccountFoundForSSOUserEmail || isPartnerOnBoardVendorEmailNotificationEnabled) {
			sendEmailNotificationToVendorAdmins(partnerEmailId, companyName, dtos, subjectLine,
					isNoSalesforceAccountFoundForSSOUserEmail);
		} else {
			String debugMessage = "Vendor company users will not receive email notifications about partner sign up. at "
					+ new Date();
			Logger.debug(debugMessage);
		}

	}

	private void sendEmailNotificationToVendorAdmins(String partnerEmailId, String companyName,
			List<TeamMemberListDTO> dtos, String subjectLine, boolean isPartnerSignUpThroughSSO) {
		for (TeamMemberListDTO dto : dtos) {
			Context context = new Context();
			Map<String, Object> model = new HashMap<>();
			addWelcomeDisplayName(dto.getFullName(), model);
			model.put(X_AMPLIFY_LOGO, xamplifyLogo);
			model.put(TARGET_URL, webUrl + LOGIN);
			model.put(PARTNER_EMAIL_ID, partnerEmailId);
			model.put(COMPANY_NAME, companyName);
			String templateName = isPartnerSignUpThroughSSO ? "partner-onboarded-no-salesforce-acc-through-sso"
					: "partner-signedup-notification";
			processTemplateAndSendEmailNotification(dto.getEmailId(), context, templateName, subjectLine, model);
		}
	}

	/***** XNFR-523 *****/
	public void sendUpdatedTrackOrPlayBookEmailNotificationToProgressedPartners(User sender, User user,
			LearningTrack learningTrack) {
		if (learningTrack.isTrackUpdatedEmailNotification()) {
			Context context = new Context();
			Map<String, Object> model = new HashMap<>();
			addWelcomeDisplayName(XamplifyUtils.getCustomerFullName(user), model);
			CompanyProfile senderCompanyProfile = sender.getCompanyProfile();
			String companyWebsite = xamplifyUtil.addHttpOrHttpsProtocolAsPrefix(senderCompanyProfile.getWebsite());
			String companyLogoPath = serverPath
					+ XamplifyUtils.escapeDollarSequece(learningTrack.getCompany().getCompanyLogoPath());
			model.put("senderCompanyLogo", companyLogoPath);
			model.put(TARGET_URL, companyWebsite);
			boolean isTrack = LearningTrackType.TRACK.name().equals(learningTrack.getType().name());
			String moduleName = isTrack ? "Track" : "Play Book";
			model.put("learningTrackType", moduleName);
			model.put("learningTrackTitle", learningTrack.getTitle());
			model.put("senderCompanyName", senderCompanyProfile.getCompanyName());
			processTemplateAndSendEmailNotification(user.getEmailId(), context,
					"published-track-or-playbook-updated-notification", moduleName + " has been updated", model);
		}

	}

	/*********** XNFR-571 ************/
	public void sendDashboardButtonPublishedEmailNotificationsToPartnersAndVendors(
			DashboardButtonsEmailNotificationRequestDTO dashboardButtonsEmailNotificationRequestDTO) {
		try {
			Integer userId = dashboardButtonsEmailNotificationRequestDTO.getLoggedInUserId();
			if (dashboardButtonsEmailNotificationRequestDTO.isDashboardButtonPublished()) {
				User loggedInUser = userService.findByPrimaryKey(userId, new FindLevel[] { FindLevel.SHALLOW });
				EmailNotificationSettingsDTO emailNotificationSettingsDTO = companyDao.getEmailNotificationSettings(
						dashboardButtonsEmailNotificationRequestDTO.getLoggedInUserCompanyId());
				/*** Send Email Notifications To Partners *****/
				sendDashboardSharedEmailNotificationToPartners(dashboardButtonsEmailNotificationRequestDTO,
						loggedInUser, emailNotificationSettingsDTO.isDashboardButtonsEmailNotification());
				/*** Send Email Notifications To Vendors *****/
				if (dashboardButtonsEmailNotificationRequestDTO.isAtLeastOnePartnerOrGroupSelected()) {
					setEmailModelPropertiesAndSendEmailNotificationsToVendorCompany(
							dashboardButtonsEmailNotificationRequestDTO, loggedInUser,
							emailNotificationSettingsDTO.isDashboardButtonPublishVendorEmailNotification());
				}
			}
		} catch (EmailNotificationException e) {
			throw new EmailNotificationException(UNABLE_TO_SEND_DASHBOARD_BUTTON, e);
		} catch (HibernateException hex) {
			throw new EmailNotificationException(UNABLE_TO_SEND_DASHBOARD_BUTTON, hex);
		} catch (DataIntegrityViolationException dex) {
			throw new EmailNotificationException(UNABLE_TO_SEND_DASHBOARD_BUTTON, dex);
		} catch (XamplifyDataAccessException cex) {
			throw new EmailNotificationException(UNABLE_TO_SEND_DASHBOARD_BUTTON, cex);
		} catch (BadRequestException bex) {
			throw new EmailNotificationException(UNABLE_TO_SEND_DASHBOARD_BUTTON, bex);
		} catch (Exception ex) {
			throw new EmailNotificationException(UNABLE_TO_SEND_DASHBOARD_BUTTON, ex);
		}

	}

	private void setEmailModelPropertiesAndSendEmailNotificationsToVendorCompany(
			DashboardButtonsEmailNotificationRequestDTO dashboardButtonsEmailNotificationRequestDTO, User loggedInUser,
			boolean dashboardButtonPublishedVendorEmailNotification) {
		/** XNFR-688 ***/
		boolean isAtleastOnePartnerOrGroupSelected = dashboardButtonsEmailNotificationRequestDTO
				.isAtLeastOnePartnerOrGroupSelected();
		if (isAtleastOnePartnerOrGroupSelected && dashboardButtonPublishedVendorEmailNotification) {
			String title = dashboardButtonsEmailNotificationRequestDTO.getDashboardTitle();
			String publishedByEmailAddress = loggedInUser.getEmailId();
			String fullName = XamplifyUtils.getCustomerFullName(loggedInUser);
			String publishedBy = StringUtils.hasText(fullName) ? fullName : loggedInUser.getEmailId();
			populateModelAndSendEmail(fullName, publishedBy, publishedByEmailAddress, title, loggedInUser.getEmailId());
			sendEmailsToVendorCompanyAdmins(dashboardButtonsEmailNotificationRequestDTO.getLoggedInUserCompanyId(),
					loggedInUser.getUserId(), fullName, publishedBy, publishedByEmailAddress, title);
		} else {
			String debugMessage = "Email notifications for published dashboard buttons will not be sent to the vendor company.";
			Logger.debug(debugMessage);
		}

	}

	private void sendDashboardSharedEmailNotificationToPartners(
			DashboardButtonsEmailNotificationRequestDTO dashboardButtonsEmailNotificationRequestDTO, User loggedInUser,
			boolean isDashboardButtonPublishedEmailNotificationOptionEnabled) {
		Integer loggedInUserCompanyId = dashboardButtonsEmailNotificationRequestDTO.getLoggedInUserCompanyId();
		List<Integer> deactivatedPartners = utilDao.findDeactivedPartnersByCompanyId(loggedInUserCompanyId);
		List<Integer> partnerIdsByPartnerGroupIds = userListDao
				.findUserIdsByUserListIds(dashboardButtonsEmailNotificationRequestDTO.getPartnerGroupIds());
		List<Integer> partnerCompanyPartnerIds = dashboardButtonsEmailNotificationRequestDTO.getPartnerIds();
		partnerIdsByPartnerGroupIds.addAll(partnerCompanyPartnerIds);
		List<Integer> distinctPartnerIds = XamplifyUtils.removeDuplicatesAndNulls(partnerIdsByPartnerGroupIds);
		/********* XBI-2831 **********/
		List<Integer> publishedPartnerIds = dashboardButtonsEmailNotificationRequestDTO.getPublishedPartnerUserIds();
		if (!XamplifyUtils.isNotEmptyList(publishedPartnerIds)) {
			publishedPartnerIds = new ArrayList<>();
		}
		distinctPartnerIds.removeAll(publishedPartnerIds);
		distinctPartnerIds.removeIf(deactivatedPartners::contains);
		dashboardButtonsEmailNotificationRequestDTO
				.setAtLeastOnePartnerOrGroupSelected(XamplifyUtils.isNotEmptyList(distinctPartnerIds));
		Set<String> dashboardButtonTitles = new HashSet<>();
		boolean isPublishingToNewlyAddedPartners = false;
		iterateDashboardButtonPublishedPartnersAndSendEmails(
				dashboardButtonsEmailNotificationRequestDTO.getLoggedInUserCompanyId(), loggedInUser,
				dashboardButtonsEmailNotificationRequestDTO.getDashboardTitle(), distinctPartnerIds,
				dashboardButtonTitles, isPublishingToNewlyAddedPartners,
				isDashboardButtonPublishedEmailNotificationOptionEnabled);

	}

	public void iterateDashboardButtonPublishedPartnersAndSendEmails(Integer companyId, User loggedInUser, String title,
			List<Integer> distinctPartnerIds, Set<String> dashboardButtonTitles,
			boolean isPublishingToNewlyAddedPartners,
			boolean isDashboardButtonPublishedEmailNotificationOptionEnabled) {
		try {
			String templateName = isPublishingToNewlyAddedPartners
					? "dashboard-button-shared-with-newly-added-partners-notification"
					: "dashboard-button-shared-with-partners-notification";
			if (isDashboardButtonPublishedEmailNotificationOptionEnabled) {
				List<User> partnerUsers = userDao.findUserIdAndEmailIdAndFullNameByUserIds(distinctPartnerIds);
				if (XamplifyUtils.isNotEmptyList(partnerUsers)) {
					for (User partnerUser : partnerUsers) {
						checkUnsubscribedAndExcludedUsersAndSendEmails(companyId, loggedInUser, title,
								dashboardButtonTitles, isPublishingToNewlyAddedPartners, templateName, partnerUser);
					}
				}

			}
		} catch (EmailNotificationException e) {
			throw new EmailNotificationException(UNABLE_TO_SEND_DASHBOARD_BUTTON, e);
		} catch (HibernateException hex) {
			throw new EmailNotificationException(UNABLE_TO_SEND_DASHBOARD_BUTTON, hex);
		} catch (DataIntegrityViolationException dex) {
			throw new EmailNotificationException(UNABLE_TO_SEND_DASHBOARD_BUTTON, dex);
		} catch (XamplifyDataAccessException cex) {
			throw new EmailNotificationException(UNABLE_TO_SEND_DASHBOARD_BUTTON, cex);
		} catch (BadRequestException bex) {
			throw new EmailNotificationException(UNABLE_TO_SEND_DASHBOARD_BUTTON, bex);
		} catch (Exception ex) {
			throw new EmailNotificationException(UNABLE_TO_SEND_DASHBOARD_BUTTON, ex);
		}
	}

	private void checkUnsubscribedAndExcludedUsersAndSendEmails(Integer companyId, User loggedInUser, String title,
			Set<String> dashboardButtonTitles, boolean isPublishingToNewlyAddedPartners, String templateName,
			User partnerUser) {
		Integer partnerId = partnerUser.getUserId();
		boolean isUserExcluded = utilService.isUserExcluded(partnerUser, companyId);
		boolean isUnsubscribed = userDao.isUserUnsubscribedForCompany(partnerId, companyId);
		if (!isUserExcluded && !isUnsubscribed) {
			Context context = setDashboardButtonsThymeLeafProperties(loggedInUser, title, dashboardButtonTitles,
					isPublishingToNewlyAddedPartners, partnerUser);
			String html = templateEngine.process(templateName, context);
			mailService.sendMail(new EmailBuilder().from(loggedInUser.getEmailId())
					.senderName(XamplifyUtils.getCustomerFullName(loggedInUser)).to(partnerUser.getEmailId())
					.subject("New Dashboard Buttons are available in xAmplify").body(html).build());
		}
	}

	private Context setDashboardButtonsThymeLeafProperties(User loggedInUser, String title,
			Set<String> dashboardButtonTitles, boolean isPublishingToNewlyAddedPartners, User partnerUser) {
		Context context = new Context();
		Map<String, Object> model = new HashMap<>();
		addWelcomeDisplayName(partnerUser.getFullName(), model);
		CompanyProfile loggedInUserCompany = loggedInUser.getCompanyProfile();
		String companyWebsite = xamplifyUtil.addHttpOrHttpsProtocolAsPrefix(loggedInUserCompany.getWebsite());
		String companyLogoPath = serverPath
				+ XamplifyUtils.escapeDollarSequece(loggedInUserCompany.getCompanyLogoPath());
		model.put("senderCompanyLogo", companyLogoPath);
		model.put(TARGET_URL, companyWebsite);
		if (isPublishingToNewlyAddedPartners) {
			model.put("dashboardButtonTitles", dashboardButtonTitles);
		} else {
			model.put("dashboardTitle", title);
		}
		model.put("sharedByCompanyName", loggedInUser.getCompanyProfile().getCompanyName());
		model.put("sharedOn", new Date());
		context.setVariables(model);
		return context;
	}

	/*********** XNFR-571 ************/
	private void sendEmailsToVendorCompanyAdmins(Integer companyId, Integer userId, String fullName, String publishedBy,
			String publishedByEmailAddress, String title) {
		List<TeamMemberListDTO> dtos = teamDao.findPrimaryAdminAndExtraAdmins(companyId);
		for (TeamMemberListDTO dto : dtos) {
			Integer teamMemberUserId = dto.getTeamMemberUserId();
			if (!teamMemberUserId.equals(userId)) {
				String receiverEmailId = dto.getEmailId().trim().toLowerCase();
				populateModelAndSendEmail(fullName, publishedBy, publishedByEmailAddress, title, receiverEmailId);
			}
		}
	}

	/*********** XNFR-571 ************/
	private void populateModelAndSendEmail(String fullName, String publishedBy, String publishedByEmailAddress,
			String title, String receiverEmailId) {
		Context context = new Context();
		Map<String, Object> model = new HashMap<>();
		addWelcomeDisplayName(fullName, model);
		model.put(X_AMPLIFY_LOGO, xamplifyLogo);
		model.put(TARGET_URL, webUrl + LOGIN);
		model.put("dashboardTitle", title);
		model.put("publishedBy", publishedBy);
		model.put("publishedByEmailAddress", publishedByEmailAddress);
		model.put("publishedOn", new Date());
		processTemplateAndSendEmailNotification(receiverEmailId, context,
				"dashboard-buttons-shared-vendor-notification",
				systemNotificationPrefixMessage + "Dashboard button has been successfully shared", model);
	}

	/** XNFR-553 **/
	public void sendMailToUser(EmailActivityRequestDTO emailActivityRequestDTO, List<MultipartFile> attachments,
			Map<Integer, Integer> emailActivityIdsMap) {
		if (emailActivityRequestDTO != null) {
			String senderName = emailActivityRequestDTO.getFirstName()
					+ (XamplifyUtils.isValidString(emailActivityRequestDTO.getLastName())
							? emailActivityRequestDTO.getLastName()
							: "");
			iterateAndSendMail(emailActivityRequestDTO, attachments, emailActivityIdsMap, senderName);
		}
	}

	private void iterateAndSendMail(EmailActivityRequestDTO emailActivityRequestDTO, List<MultipartFile> attachments,
			Map<Integer, Integer> emailActivityIdsMap, String senderName) {
		String subject = emailActivityRequestDTO.getSubject();
		for (Integer userId : emailActivityRequestDTO.getUserIds()) {
			String body = emailActivityRequestDTO.getBody() + EMAIL_TRACKING_IMAGE_WITH_DIV;
			String emailTrackingUrl = "";
			if (emailActivityIdsMap != null && !emailActivityIdsMap.isEmpty()) {
				emailTrackingUrl = serverUrl + EMAIL_TRACKING_INFORMATION_EMAIL_ACTIVITY_ID
						+ emailActivityIdsMap.get(userId);
			}
			EmailMergeTagDTO emailMergeTagDTO = emailActivityDAO
					.fetchEmailMergeTagsData(emailActivityRequestDTO.getLoggedInUserId(), userId);
			body = formatEmailBody(emailMergeTagDTO, body, emailTrackingUrl);
			mailService.sendMail(new EmailBuilder().from(emailActivityRequestDTO.getFromEmailId())
					.senderName(senderName).to(emailMergeTagDTO.getContactEmailId()).subject(subject).body(body)
					.ccEmailIds(emailActivityRequestDTO.getCcEmailIds())
					.bccEmailIds(emailActivityRequestDTO.getBccEmailIds()).attachments(attachments).build());
		}
	}

	private String formatEmailBody(EmailMergeTagDTO emailMergeTagDTO, String body, String emailTrackingUrl) {
		String contactFirstName = emailMergeTagDTO.getContactFirstName();
		String contactLastName = emailMergeTagDTO.getContactLastName();
		body = safeReplace(body, "{{firstName}}", contactFirstName);
		body = safeReplace(body, "{{lastName}}", contactLastName);
		String contactFullName = ((XamplifyUtils.isValidString(contactFirstName)) ? contactFirstName + " " : "")
				+ ((XamplifyUtils.isValidString(contactFirstName)) ? contactLastName : "");
		body = safeReplace(body, "{{fullName}}", contactFullName);
		body = safeReplace(body, "{{emailId}}", emailMergeTagDTO.getContactEmailId());
		body = safeReplace(body, "{{" + COMPANY_NAME + "}}", emailMergeTagDTO.getContactCompanyName());
		body = safeReplace(body, "{{mobileNumber}}",
				emailMergeTagDTO.getContactMobileNumber() != null ? emailMergeTagDTO.getContactMobileNumber() : "");
		body = safeReplace(body, "{{address}}", emailMergeTagDTO.getContactAddress());
		body = safeReplace(body, "{{zipcode}}",
				emailMergeTagDTO.getContactZipCode() != null ? emailMergeTagDTO.getContactZipCode() : "");
		body = safeReplace(body, "{{city}}", emailMergeTagDTO.getContactCity());
		body = safeReplace(body, "{{state}}", emailMergeTagDTO.getContactState());
		body = safeReplace(body, "{{country}}", emailMergeTagDTO.getContactCountry());
		String senderFirstName = emailMergeTagDTO.getSenderFirstName();
		String senderMiddelName = emailMergeTagDTO.getSenderMiddleName();
		String senderLastName = emailMergeTagDTO.getSenderLastName();
		body = safeReplace(body, "{{senderFirstName}}", senderFirstName);
		body = safeReplace(body, "{{senderMiddleName}}", senderMiddelName);
		body = safeReplace(body, "{{senderLastName}}", senderLastName);
		String senderFullName = ((senderFirstName != null) ? senderFirstName + " " : "")
				+ ((senderMiddelName != null) ? senderMiddelName + " " : "")
				+ ((senderLastName != null) ? senderLastName : "");
		body = safeReplace(body, "{{senderFullName}}", senderFullName);
		body = safeReplace(body, "{{senderJobTitle}}", emailMergeTagDTO.getSenderJobTitle());
		body = safeReplace(body, "{{senderEmailId}}", emailMergeTagDTO.getSenderEmailId());
		body = safeReplace(body, "{{senderContactNumber}}", emailMergeTagDTO.getSenderMobileNumber());
		body = safeReplace(body, "{{senderCompany}}", emailMergeTagDTO.getSenderCompanyName());
		body = safeReplace(body, "{{senderCompanyUrl}}", emailMergeTagDTO.getSenderCompanyUrl());
		body = safeReplace(body, "{{senderCompanyGoogleUrl}}", emailMergeTagDTO.getSenderCompanyGoogleUrl());
		body = safeReplace(body, "{{senderCompanyFacebookUrl}}", emailMergeTagDTO.getSenderCompanyFacebookUrl());
		body = safeReplace(body, "{{senderCompanyLinkedinUrl}}", emailMergeTagDTO.getSenderCompanyLinkedInUrl());
		body = safeReplace(body, "{{senderCompanyInstagramUrl}}", emailMergeTagDTO.getSenderCompanyInstagramUrl());
		body = safeReplace(body, "{{senderCompanyTwitterUrl}}", emailMergeTagDTO.getSenderCompanyTwitterUrl());
		body = safeReplace(body, "{{senderCompanyAddress}}", emailMergeTagDTO.getSenderCompanyAddress());
		body = safeReplace(body, "{{senderCompanyContactNumber}}", emailMergeTagDTO.getSenderCompanyContactNumber());
		body = safeReplace(body, "{{senderAboutUs}}", emailMergeTagDTO.getSenderAboutUs());
		body = safeReplace(body, "{{senderEventUrl}}", emailMergeTagDTO.getSenderEventUrl());
		body = safeReplace(body, "{{partnerAboutUs}}", emailMergeTagDTO.getContactAboutUs());
		body = safeReplace(body, "{{senderPrivacyPolicy}}", emailMergeTagDTO.getSenderPrivacyPolicy());
		body = body.replace("{{unsubscribeLink}}", "");
		body = body.replace(emailOpenImgUrl, emailTrackingUrl);
		body = body.replace(ENCODED_EMAIL_OPEN_IMAGE_URL, emailTrackingUrl);
		body = "<div class='row' style='overflow-y: auto'>" + body + "</div>";
		body = safeReplace(body, "VENDOR_COMPANY_WEBSITE_URL", emailMergeTagDTO.getSenderCompanyUrl());
		return body;
	}

	private String safeReplace(String body, String placeholder, String value) {
		return body.replace(placeholder, value != null ? value : "");
	}

	public void sendNoSalesforceAccountFoundForLeadEmailToVendorAdmins(Lead lead) {
		boolean isSupportEmailInAdminList = false;
		Integer companyId = lead.getCreatedForCompany().getId();
		String partnerEmailId = lead.getCreatedByEmail().trim().toLowerCase();
		String partnerCompanyName = lead.getCreatedByCompany().getCompanyName().trim();
		List<TeamMemberListDTO> teamMemberListDTOs = teamDao.findPrimaryAdminAndExtraAdmins(companyId);
		String subjectLine = systemNotificationPrefixMessage + noSalesforceAccountFoundForLeadEmailSubject;
		sendLeadEmailNotificationsToVendorAdmins(partnerEmailId, teamMemberListDTOs, subjectLine, companyId, lead);
		String supportEmailId = userDao.getSupportEmailIdByCompanyId(companyId);
		if (XamplifyUtils.isValidString(supportEmailId)) {
			isSupportEmailInAdminList = teamMemberListDTOs.stream()
					.anyMatch(teamMember -> teamMember.getEmailId().equalsIgnoreCase(supportEmailId));
			if (!isSupportEmailInAdminList) {
				sendLeadEmailToSupportEmailId(partnerEmailId, partnerCompanyName, subjectLine, supportEmailId, lead);
			}
		} else {
			Logger.debug("Support Email not available for Vendor company. unable to send email for partner :"
					+ partnerEmailId + ". at: " + new Date());
		}
	}

	private void sendLeadEmailToSupportEmailId(String partnerEmailId, String partnerCompanyName, String subjectLine,
			String supportEmailId, Lead lead) {
		Context context = new Context();
		Map<String, Object> model = new HashMap<>();
		addWelcomeDisplayName(THERE, model);
		model.put(X_AMPLIFY_LOGO, xamplifyLogo);
		model.put(TARGET_URL, webUrl + LOGIN);
		model.put("lealFirstName", lead.getFirstName() != null ? lead.getFirstName() : "" + " " + lead.getLastName());
		model.put(PARTNER_COMPANY_NAME, partnerCompanyName);
		model.put(PARTNER_EMAIL, partnerEmailId);
		model.put("leadCRMID", lead.getSfLeadId());
		model.put("leadId", lead.getReferenceId());
		model.put("leadEmail", lead.getEmail());
		processTemplateAndSendEmailNotification(supportEmailId, context, "lead-created-without-sf-info", subjectLine,
				model);
	}

	private void sendLeadEmailNotificationsToVendorAdmins(String partnerEmailId, List<TeamMemberListDTO> dtos,
			String subjectLine, Integer companyId, Lead lead) {
		boolean isPartnerOnBoardVendorEmailNotificationEnabled = companyDao
				.isPartnerOnBoardVendorEmailNotificationEnabledByCompanyId(companyId);
		if (isPartnerOnBoardVendorEmailNotificationEnabled) {
			sendLeadEmailNotificationToVendorAdmins(partnerEmailId, dtos, subjectLine, lead);
		} else {
			String debugMessage = "Vendor company users will not receive email notifications about partner sign up. at "
					+ new Date();
			Logger.debug(debugMessage);
		}

	}

	private void sendLeadEmailNotificationToVendorAdmins(String partnerEmailId, List<TeamMemberListDTO> dtos,
			String subjectLine, Lead lead) {
		for (TeamMemberListDTO dto : dtos) {
			Context context = new Context();
			Map<String, Object> model = new HashMap<>();
			addWelcomeDisplayName(dto.getFullName(), model);
			model.put(X_AMPLIFY_LOGO, xamplifyLogo);
			model.put(TARGET_URL, webUrl + LOGIN);
			model.put("lealFirstName",
					lead.getFirstName() != null ? lead.getFirstName() : "" + " " + lead.getLastName());
			model.put(PARTNER_COMPANY_NAME, lead.getCreatedByCompany().getCompanyName().trim());
			model.put(PARTNER_EMAIL, partnerEmailId);
			model.put("leadCRMID", lead.getSfLeadId());
			model.put("leadId", lead.getReferenceId());
			model.put("leadEmail", lead.getEmail());
			String templateName = "lead-created-without-sf-info";
			processTemplateAndSendEmailNotification(dto.getEmailId(), context, templateName, subjectLine, model);
		}
	}



	/** XNFR-781 **/
	public void sendContentApprovalStatusEmailNotification(ApprovalStatusHistoryDTO approvalStatusHistoryDTO) {
		if (approvalStatusHistoryDTO != null) {
			Context context = new Context();
			String toEmailId = approvalStatusHistoryDTO.getEmailId();
			Map<String, Object> model = new HashMap<>();
			model.put(TO_NAME, approvalStatusHistoryDTO.getCreatedByName());
			String currentSatus = approvalStatusHistoryDTO.getStatusInString();
			String formattedStatus = currentSatus.charAt(0) + currentSatus.substring(1).toLowerCase();
			model.put("status", formattedStatus);
			model.put("name", approvalStatusHistoryDTO.getName());
			model.put("updatedByName", approvalStatusHistoryDTO.getStatusUpdatedByName());
			model.put(MODULE_TYPE, approvalStatusHistoryDTO.getModuleType());
			model.put("comment", approvalStatusHistoryDTO.getComment());
			context.setVariables(model);
			String subject = UPDATE_TEXT_FOR_SUBJECT + approvalStatusHistoryDTO.getModuleType() + " has been "
					+ formattedStatus;
			String htmlName = "approve-or-reject-dam-content";
			String html = templateEngine.process(htmlName, context);
			mailService.sendMail(new EmailBuilder().from(fromEmail).senderName(XAMPLIFY_TEXT).to(toEmailId)
					.subject(subject).body(html).build());
		}
	}

	/** XNFR-821 **/
	public void iterateAndSendTeamMemberPrivilegesUpdatedForApprovalProccessEmailNotification(
			List<ApprovalPrivilegesEmailNotificationDTO> approvalPrivilegesEmailNotificationDTOs,
			UserDTO senderDetailsUserDTO, Integer companyId) {
		if (XamplifyUtils.isValidInteger(companyId) && XamplifyUtils.isValidString(senderDetailsUserDTO.getEmailId())) {
			for (ApprovalPrivilegesEmailNotificationDTO approvalPrivilegesEmailNotificationDTO : approvalPrivilegesEmailNotificationDTOs) {
				processTeamMemberPrivilegesNotification(senderDetailsUserDTO, companyId,
						approvalPrivilegesEmailNotificationDTO);
			}
		}
	}

	private void processTeamMemberPrivilegesNotification(UserDTO senderDetailsUserDTO, Integer companyId,
			ApprovalPrivilegesEmailNotificationDTO approvalPrivilegesEmailNotificationDTO) {
		if (approvalPrivilegesEmailNotificationDTO.isAssetApproverFieldUpdated()
				|| approvalPrivilegesEmailNotificationDTO.isTrackApproverFieldUpdated()
				|| approvalPrivilegesEmailNotificationDTO.isPlaybookApproverFieldUpdated()) {
			UserDTO teamMemberDetails = userDao
					.getEmailIdAndDisplayName(approvalPrivilegesEmailNotificationDTO.getId());
			String toEmailId = teamMemberDetails.getEmailId();
			if (XamplifyUtils.isValidString(toEmailId)) {
				try {
					Context context = new Context();
					Map<String, Object> model = buildApprovalNotificationModel(companyId,
							approvalPrivilegesEmailNotificationDTO, senderDetailsUserDTO, teamMemberDetails);
					context.setVariables(model);
					String htmlName = "approval-privileges-updated-notification";
					String html = templateEngine.process(htmlName, context);

					mailService.sendMail(new EmailBuilder().from(senderDetailsUserDTO.getEmailId())
							.senderName(senderDetailsUserDTO.getFullNameOrEmailId()).to(toEmailId)
							.subject((String) model.get("subject")).body(html).build());
				} catch (Exception e) {
					Logger.debug("Failed to send Approval Privileges Notification for Team Member. Email: " + toEmailId
							+ ". Time stamp: " + new Date());
				}
			}
		}
	}

	private Map<String, Object> buildApprovalNotificationModel(Integer companyId,
			ApprovalPrivilegesEmailNotificationDTO approvalPrivilegesEmailNotificationDTO, UserDTO senderDetailsUserDTO,
			UserDTO teamMemberDetails) {
		Map<String, Object> model = new HashMap<>();
		String assignedModuleNames = processAssignedModuleNamesAsString(companyId,
				approvalPrivilegesEmailNotificationDTO);
		String subject = UPDATE_TEXT_FOR_SUBJECT;
		model.put(TO_NAME, teamMemberDetails.getFullNameOrEmailId());
		if (XamplifyUtils.isValidString(assignedModuleNames)) {
			model.put("assignedModuleNames", assignedModuleNames);
			model.put("greetingPhrase", "Pleased");
			model.put("statusPhrase", "granted");
			model.put("slashSeperatedModuleNames", assignedModuleNames.replace(',', '/'));
			model.put("helperText",
					", both those pending approval and those created in the future, will be automatically approved.");
			subject += "Approval Authority Assigned";
		} else {
			model.put("assignedModuleNames", "None");
			model.put("greetingPhrase", "regret");
			model.put("statusPhrase", "revoked");
			model.put("slashSeperatedModuleNames", "Assets/Tracks/Playbooks");
			model.put("helperText", " in the future will require approval.");
			subject += "Approval Authority Revoked";
		}
		model.put("senderFullName", senderDetailsUserDTO.getFullNameOrEmailId());
		model.put("subject", subject);
		return model;
	}

	private String processAssignedModuleNamesAsString(Integer companyId,
			ApprovalPrivilegesEmailNotificationDTO approvalPrivilegesEmailNotificationDTO) {
		String assignedModuleNames = "";
		TeamMemberApprovalPrivilegesDTO teamMemberApprovalSettings = approveDao
				.getTeamMemberApprovalPrivilegeSettingsByTeamMemberId(approvalPrivilegesEmailNotificationDTO.getId(),
						companyId);
		if (teamMemberApprovalSettings != null) {
			List<String> modules = new ArrayList<>();
			if (teamMemberApprovalSettings.isAssetApprover()) {
				modules.add("Assets");
			}
			if (teamMemberApprovalSettings.isTrackApprover()) {
				modules.add("Tracks");
			}
			if (teamMemberApprovalSettings.isPlaybookApprover()) {
				modules.add("Playbooks");
			}
			assignedModuleNames = modules.isEmpty() ? "" : String.join(", ", modules);
		}
		return assignedModuleNames;
	}

	/** XNFR-822 **/
	public void iterateAndSendApprovalReminderNotificationForPendingContent(List<Integer> allApproversIds,
			PendingApprovalDamAndLmsDTO pendingApprovalDamAndLmsDTO, UserDTO senderDetailsUserDTO) {
		if (XamplifyUtils.isValidString(senderDetailsUserDTO.getEmailId())
				&& XamplifyUtils.isNotEmptyList(allApproversIds) && pendingApprovalDamAndLmsDTO != null) {
			String webUrlForLogin = webUrl;
			boolean hasVanityAccess = utilDao.hasVanityAccessByUserId(pendingApprovalDamAndLmsDTO.getCreatedById());
			if (hasVanityAccess) {
				String companyProfileName = userDao
						.getCompanyProfileNameByUserId(pendingApprovalDamAndLmsDTO.getCreatedById());
				webUrlForLogin = xamplifyUtil.frameVanityURL(webUrl, companyProfileName);
			}
			for (Integer approverId : allApproversIds) {
				UserDTO approverUserDetails = userDao.getEmailIdAndDisplayName(approverId);
				String toEmailId = approverUserDetails.getEmailId();
				if (XamplifyUtils.isValidString(toEmailId)) {
					try {
						Context context = new Context();
						Map<String, Object> model = new HashMap<>();
						String subject = returnSubejctBasedOnApprovalOrReApproval(pendingApprovalDamAndLmsDTO);
						prepareModelObjectForApprovalReminder(pendingApprovalDamAndLmsDTO, senderDetailsUserDTO,
								approverUserDetails, model);
						model.put(TARGET_URL, webUrlForLogin + LOGIN);
						context.setVariables(model);
						String htmlName = "approval-reminder-notification";
						String html = templateEngine.process(htmlName, context);
						mailService.sendMail(new EmailBuilder().from(senderDetailsUserDTO.getEmailId())
								.senderName(senderDetailsUserDTO.getFullNameOrEmailId()).to(toEmailId).subject(subject)
								.body(html).build());
					} catch (Exception e) {
						String debugMessage = "Failed to send Content Approval Reminder notification for Approver. Email: "
								+ toEmailId + ". Time stamp: " + new Date();
						Logger.debug(debugMessage);
					}
				}
			}
		}
	}

	private String returnSubejctBasedOnApprovalOrReApproval(PendingApprovalDamAndLmsDTO pendingApprovalDamAndLmsDTO) {
		String subject;
		if (pendingApprovalDamAndLmsDTO.isSendForApproval()) {
			subject = "Request for Asset Approval";
		} else if (pendingApprovalDamAndLmsDTO.isSendForReApproval()) {
			subject = " Request for Asset Re-Approval";
		} else {
			subject = "Reminder: Content Approval Reminder Notification";
		}
		return subject;
	}

	private void prepareModelObjectForApprovalReminder(PendingApprovalDamAndLmsDTO pendingApprovalDamAndLmsDTO,
			UserDTO senderDetailsUserDTO, UserDTO approverUserDetails, Map<String, Object> model) {
		model.put(TO_NAME, approverUserDetails.getFullNameOrEmailId());
		model.put("createdBy", senderDetailsUserDTO.getFullNameOrEmailId());
		String currentApprovalStatus = pendingApprovalDamAndLmsDTO.getStatus();
		currentApprovalStatus = currentApprovalStatus.equalsIgnoreCase("created") ? "Pending Approval"
				: currentApprovalStatus;
		model.put("status", currentApprovalStatus);
		model.put("name", pendingApprovalDamAndLmsDTO.getName());
		model.put("createdTime", pendingApprovalDamAndLmsDTO.getCreatedTime());
		String formattedModuleType = pendingApprovalDamAndLmsDTO.getModuleType().charAt(0)
				+ pendingApprovalDamAndLmsDTO.getModuleType().substring(1).toLowerCase();
		if (formattedModuleType.equalsIgnoreCase("dam")) {
			model.put("typeKey", "Asset Type:");
			model.put("type", pendingApprovalDamAndLmsDTO.getAssetType());
			model.put("moduleTypeInBody", "n Asset");
			model.put(MODULE_TYPE, "Asset");
		} else {
			model.put("moduleTypeInBody", " " + formattedModuleType);
			model.put(MODULE_TYPE, formattedModuleType);
		}
		if (pendingApprovalDamAndLmsDTO.isApprovalReminder()) {
			model.put(APPROVAL_REMINDER_MESSAGE, "I just wanted to remind you that I have created");
		} else if (pendingApprovalDamAndLmsDTO.isSendForApproval()) {
			model.put(APPROVAL_REMINDER_MESSAGE, "I have added");
		} else if (pendingApprovalDamAndLmsDTO.isSendForReApproval()) {
			model.put(APPROVAL_REMINDER_MESSAGE, "I have replaced");
		}
	}

	public void sendPartnerPrimaryAdminUpdateEmail(PartnerPrimaryAdminUpdateDto partnerPrimaryAdminUpdateDto) {
		String htmlName = "partner-company-primary-admin-update";
		Context context = new Context();
		Map<String, Object> model = new HashMap<>();
		Integer partnerUserId = partnerPrimaryAdminUpdateDto.getPartnerCompanyTeamMemberUserId();
		String primaryAdminEmailAddress = userDao.getEmailIdByUserId(partnerUserId);
		String partnerCompanyName = userDao.getCompanyNameByUserId(partnerUserId);
		Integer vendorCompanyUserId = partnerPrimaryAdminUpdateDto.getVendorCompanyUserId();
		String updatedByEmailAddress = userDao.getEmailIdByUserId(vendorCompanyUserId);
		String vendorCompanyName = userDao.getCompanyNameByUserId(vendorCompanyUserId);
		model.put(PARTNER_COMPANY_NAME, partnerCompanyName);
		model.put("primaryAdminEmailAddress", primaryAdminEmailAddress);
		model.put("updatedBy", updatedByEmailAddress);
		model.put(VENDOR_COMPANY_NAME, vendorCompanyName);
		context.setVariables(model);
		String html = templateEngine.process(htmlName, context);
		String profileName = getSubjectSuffix(profiles);
		String subjectPrefix = "Partner Company Primary Admin Successfully Updated";
		String subject;
		if (profiles.equals(PRODUCTION)) {
			subject = subjectPrefix + " (" + profileName + ")";
		} else {
			subject = profileName + ":" + subjectPrefix;
		}

	}

	/** XNFR-867 **/
	public void sendTestMailToUser(EmailActivityRequestDTO emailActivityRequestDTO, List<MultipartFile> attachments,
			EmailMergeTagDTO emailMergeTagDTO) {
		if (emailActivityRequestDTO != null) {
			String senderName = emailActivityRequestDTO.getFirstName()
					+ (XamplifyUtils.isValidString(emailActivityRequestDTO.getLastName())
							? emailActivityRequestDTO.getLastName()
							: "");
			String subject = emailActivityRequestDTO.getSubject();
			String body = null;
			if (XamplifyUtils.isValidInteger(emailActivityRequestDTO.getTemplateId())) {
				String template = emailActivityDAO.getTemplateById(emailActivityRequestDTO.getTemplateId());
				body = template + EMAIL_TRACKING_IMAGE_WITH_DIV;
			} else {
				body = emailActivityRequestDTO.getBody() + EMAIL_TRACKING_IMAGE_WITH_DIV;
			}
			String emailTrackingUrl = serverUrl + EMAIL_TRACKING_INFORMATION_EMAIL_ACTIVITY_ID
					+ emailActivityRequestDTO.getEmailActivityId();
			body = formatEmailBody(emailMergeTagDTO, body, emailTrackingUrl);
			mailService.sendMail(new EmailBuilder().from(emailActivityRequestDTO.getFromEmailId())
					.senderName(senderName).to(emailActivityRequestDTO.getToEmailId()).subject(subject).body(body)
					.attachments(attachments).build());
		}
	}

	/** XNFR-892 **/
	public void sendLeadAddedOrUpdatedEmailToPartner(UserDetailsUtilDTO userDetails, LeadDto mergeTagsDto,
			boolean isUpdateLead) {
		String subject = null;
		String htmlBody = null;
		Integer createdForCompanyAdminId = utilDao.findAdminIdByCompanyId(mergeTagsDto.getCreatedForCompanyId());
		boolean isPrm = utilDao.isPrmCompany(createdForCompanyAdminId);
		DefaultEmailTemplateType leadType = fetchLeadEmailTemplateType(isUpdateLead, isPrm);
		boolean hasVanityAccess = utilDao.hasVanityAccessByUserId(createdForCompanyAdminId);
		CompanyProfile cp = new CompanyProfile();
		cp.setId(mergeTagsDto.getCreatedForCompanyId());
		if (hasVanityAccess) {
			DefaultEmailTemplate vanityDefaultEmailTemplate = vanityURLDao
					.getVanityDefaultEmailTemplateByType(leadType);
			if (vanityDefaultEmailTemplate != null) {
				CustomDefaultEmailTemplate vanityEmailTemplate = vanityURLDao
						.getVanityETByDefVanityETIdAndCompanyId(vanityDefaultEmailTemplate.getId(), cp);
				if (vanityEmailTemplate != null) {
					subject = vanityEmailTemplate.getSubject();
					htmlBody = genericDao.get(CustomDefaultEmailTemplate.class, vanityEmailTemplate.getId())
							.getHtmlBody();
				} else {
					subject = vanityDefaultEmailTemplate.getSubject();
					htmlBody = vanityDefaultEmailTemplate.getHtmlBody();
				}
			}
		} else {
			DefaultEmailTemplate defaultEmailTemplate = vanityURLDao.getVanityDefaultEmailTemplateByType(leadType);
			if (defaultEmailTemplate != null) {
				subject = defaultEmailTemplate.getSubject();
				htmlBody = defaultEmailTemplate.getHtmlBody();
			}
		}

		if (subject != null && htmlBody != null) {
			htmlBody = replaceLeadMailMergeTags(mergeTagsDto, htmlBody);
			sendLeadOrDealAddedOrUpdatedEmailToPartner(userDetails, htmlBody, subject, mergeTagsDto, null);
		}

	}

	private DefaultEmailTemplateType fetchLeadEmailTemplateType(boolean isUpdateLead, boolean isPrm) {
		DefaultEmailTemplateType leadType;
		if (isUpdateLead) {
			leadType = isPrm ? DefaultEmailTemplateType.PRM_PARTNER_UPDATE_LEAD
					: DefaultEmailTemplateType.PARTNER_UPDATE_LEAD;
		} else {
			leadType = isPrm ? DefaultEmailTemplateType.PRM_PARTNER_ADD_LEAD
					: DefaultEmailTemplateType.PARTNER_ADD_LEAD;
		}
		return leadType;
	}

	private void sendLeadOrDealAddedOrUpdatedEmailToPartner(UserDetailsUtilDTO userDetails, String htmlBody,
			String subject, LeadDto leadDto, DealDto dealDto) {
		String body;
		String senderName = "";
		if (leadDto != null && XamplifyUtils.isValidString(leadDto.getCreatedByCompanyName())) {
			senderName = leadDto.getCreatedForCompanyName();
		} else if (dealDto != null && XamplifyUtils.isValidString(dealDto.getCreatedByCompanyName())) {
			senderName = dealDto.getCreatedByCompanyName();
		}
		body = htmlBody.replace("{{customerFullName}}", userDetails.getFullName());
		mailService.sendMail(new EmailBuilder().from(fromEmail).senderName(senderName).to(userDetails.getEmailId())
				.subject(subject).body(body).build());
	}

	private String replaceLeadMailMergeTags(LeadDto leadDto, String htmlBody) {
		if (leadDto != null) {
			String activeCRM = leadDAO.getActiveCRMTypeByCompanyId(leadDto.getCreatedForCompanyId());
			String stageName = (leadDto.getCurrentStageName() != null) ? leadDto.getCurrentStageName() : "---";
			if (leadDto.getSfCustomFieldsDataDto() != null && activeCRM.equals("salesforce")) {
				boolean replaced = false;
				for (SfCustomFieldsDataDTO sfcfData : leadDto.getSfCustomFieldsDataDto()) {
					if (sfcfData.getFormLabel() != null && sfcfData.getFormLabel()
							.getFormDefaultFieldType() == FormDefaultFieldTypeEnum.PIPELINE_STAGE) {
						String value = sfcfData.getValue();
						htmlBody = htmlBody.replace("{{leadStage}}",
								(value != null && !value.isEmpty()) ? value : stageName);
						replaced = true;
						break;
					}
				}
				if (!replaced) {
					htmlBody = htmlBody.replace("{{leadStage}}", stageName);
				}
			} else {
				htmlBody = htmlBody.replace("{{leadStage}}", stageName);
			}
		} else {
			htmlBody = htmlBody.replace("{{leadStage}}", "---");
		}
//		htmlBody = htmlBody.replace("{{leadStage}}", leadDto.getCurrentStageName());
		htmlBody = XamplifyUtils.isValidString(leadDto.getLeadComment())
				? htmlBody.replace("{{leadComment}}", leadDto.getLeadComment())
				: htmlBody.replace("{{leadComment}}", "---");
		htmlBody = XamplifyUtils.isValidString(leadDto.getCampaignName())
				? htmlBody.replace("{{leadAssociatedCampaign}}", leadDto.getCampaignName())
				: htmlBody.replace("{{leadAssociatedCampaign}}", "---");
		htmlBody = htmlBody.replace("{{createdByName}}", leadDto.getCreatedByName());
		htmlBody = htmlBody.replace(LEAD_NAME, leadDto.getFullName());
		htmlBody = XamplifyUtils.isValidString(leadDto.getCompany())
				? htmlBody.replace(LEAD_COMPANY, leadDto.getCompany())
				: htmlBody.replace(LEAD_COMPANY, "---");
		htmlBody = htmlBody.replace("{{createdForCompanyName}}", leadDto.getCreatedForCompanyName());
		htmlBody = htmlBody.replace("{{companyName}}", leadDto.getCreatedByCompanyName());
		htmlBody = XamplifyUtils.isValidString(leadDto.getWebsite())
				? htmlBody.replace(VANITY_COMPANY_LOGO_HREF, leadDto.getWebsite())
				: htmlBody.replace(VANITY_COMPANY_LOGO_HREF, "");
		htmlBody = htmlBody.replace(replaceCompanyLogo,
				serverPath + XamplifyUtils.escapeDollarSequece(leadDto.getPartnerCompanyLogoPath()));
		return htmlBody;
	}

	public void sendDealAddedOrUpdatedEmailToPartner(UserDetailsUtilDTO userDetails, DealDto mergeTagsDto,
			boolean isUpdateDeal) {

		String subject = null;
		String htmlBody = null;
		Integer createdForCompanyAdminId = utilDao.findAdminIdByCompanyId(mergeTagsDto.getCreatedForCompanyId());

		DefaultEmailTemplateType dealType = isUpdateDeal ? DefaultEmailTemplateType.PARTNER_UPDATE_DEAL
				: DefaultEmailTemplateType.PARTNER_ADD_DEAL;

		boolean hasVanityAccess = utilDao.hasVanityAccessByUserId(createdForCompanyAdminId);
		CompanyProfile cp = new CompanyProfile();
		cp.setId(mergeTagsDto.getCreatedForCompanyId());
		if (hasVanityAccess) {
			DefaultEmailTemplate vanityDefaultEmailTemplate = vanityURLDao
					.getVanityDefaultEmailTemplateByType(dealType);
			if (vanityDefaultEmailTemplate != null) {
				CustomDefaultEmailTemplate vanityEmailTemplate = vanityURLDao
						.getVanityETByDefVanityETIdAndCompanyId(vanityDefaultEmailTemplate.getId(), cp);
				if (vanityEmailTemplate != null) {
					subject = vanityEmailTemplate.getSubject();
					htmlBody = genericDao.get(CustomDefaultEmailTemplate.class, vanityEmailTemplate.getId())
							.getHtmlBody();
				} else {
					subject = vanityDefaultEmailTemplate.getSubject();
					htmlBody = vanityDefaultEmailTemplate.getHtmlBody();
				}
			}
		} else {
			DefaultEmailTemplate defaultEmailTemplate = vanityURLDao.getVanityDefaultEmailTemplateByType(dealType);
			if (defaultEmailTemplate != null) {
				subject = defaultEmailTemplate.getSubject();
				htmlBody = defaultEmailTemplate.getHtmlBody();
			}
		}

		if (subject != null && htmlBody != null) {
			htmlBody = replaceDealMailMergeTags(mergeTagsDto, htmlBody);
			sendLeadOrDealAddedOrUpdatedEmailToPartner(userDetails, htmlBody, subject, null, mergeTagsDto);
		}
	}

	private String replaceDealMailMergeTags(DealDto dealDto, String htmlBody) {
		String dealAmount = dealDto.getAmount() != null ? String.valueOf(dealDto.getAmount()) : "0.0";
		htmlBody = htmlBody.replace("{{createdByName}}", dealDto.getCreatedByName());
		htmlBody = htmlBody.replace("{{createdForCompanyName}}", dealDto.getCreatedForCompanyName());
		htmlBody = XamplifyUtils.isValidString(dealDto.getLeadName())
				? htmlBody.replace(LEAD_NAME, dealDto.getLeadName())
				: htmlBody.replace(LEAD_NAME, "---");
		htmlBody = XamplifyUtils.isValidString(dealDto.getLeadCompany())
				? htmlBody.replace(LEAD_COMPANY, dealDto.getLeadCompany())
				: htmlBody.replace(LEAD_COMPANY, "---");
		htmlBody = htmlBody.replace("{{dealName}}",
				XamplifyUtils.isValidString(dealDto.getTitle()) ? dealDto.getTitle() : "---");
		htmlBody = htmlBody.replace("{{dealAmount}}", dealAmount);
		htmlBody = htmlBody.replace("{{dealStage}}", dealDto.getCurrentStageName());
		htmlBody = XamplifyUtils.isValidString(dealDto.getDealComment())
				? htmlBody.replace("{{dealComment}}", dealDto.getDealComment())
				: htmlBody.replace("{{dealComment}}", "---");
		htmlBody = htmlBody.replace("{{companyName}}", dealDto.getCreatedByCompanyName());
		htmlBody = XamplifyUtils.isValidString(dealDto.getWebsite())
				? htmlBody.replace(VANITY_COMPANY_LOGO_HREF, dealDto.getWebsite())
				: htmlBody.replace(VANITY_COMPANY_LOGO_HREF, "");
		htmlBody = htmlBody.replace(replaceCompanyLogo,
				serverPath + XamplifyUtils.escapeDollarSequece(dealDto.getPartnerCompanyLogoPath()));
		boolean hasVanityAccess = utilDao.hasVanityAccessByCompanyId(dealDto.getCreatedForCompanyId());
		String vanityURLDomain = xamplifyUtil.frameVanityURL(webUrl, dealDto.getCreatedForcompanyProfileName());
		htmlBody = htmlBody.replace("<pageLink>", (hasVanityAccess ? vanityURLDomain : webUrl) + LOGIN);
		return htmlBody;
	}

	/** XNFR-911 **/
	public void sendLeadFieldUpdatedNotification(Map<User, List<LeadDTO>> map) {
		Set<User> users = map.keySet();
		for (User user : users) {
			List<LeadDTO> leadDTOs = map.get(user);
			iterateAndSendFieldUpdateEmails(user, leadDTOs);
		}
	}

	private void iterateAndSendFieldUpdateEmails(User user, List<LeadDTO> leadDTOs) {
		String subject;
		String htmlString;
		Integer companyId = user.getCompanyProfile().getId();
		List<Integer> deactivatedPartners = utilDao.findDeactivedPartnersByCompanyId(companyId);
		boolean hasVanityAccess = utilDao.hasVanityAccessByCompanyId(companyId);
		String senderName = utilService.getFullName(user.getFirstName(), user.getMiddleName(), user.getLastName());
		DefaultEmailTemplate defaultEmailTemplate = vanityURLDao
				.getVanityDefaultEmailTemplateByType(DefaultEmailTemplateType.LEAD_DETAILS_UPDATE);
		if (hasVanityAccess) {
			CustomDefaultEmailTemplate vanityEmailTemplate = vanityURLDao
					.getVanityETByDefVanityETIdAndCompanyId(defaultEmailTemplate.getId(), user.getCompanyProfile());
			if (vanityEmailTemplate != null) {
				subject = vanityEmailTemplate.getSubject();
				htmlString = genericDao.get(CustomDefaultEmailTemplate.class, vanityEmailTemplate.getId())
						.getHtmlBody();
			} else {
				subject = defaultEmailTemplate.getSubject();
				htmlString = defaultEmailTemplate.getHtmlBody();
			}
		} else {
			subject = defaultEmailTemplate.getSubject();
			htmlString = defaultEmailTemplate.getHtmlBody();
		}
		for (LeadDTO leadDTO : leadDTOs) {
			Integer userId = leadDTO.getCreatedBy();
			if (XamplifyUtils.isNotEmptyList(deactivatedPartners) && deactivatedPartners.contains(userId))
				continue;
			String htmlBody = htmlString;
			UserDetailsUtilDTO userDetails = leadDAO.fetchFullNameAndEmailIdByUserId(userId);
			htmlBody = htmlBody.replace("{{customerFullName}}", userDetails.getFullName());
			String leadName = (XamplifyUtils.isValidString(leadDTO.getFirstName()) ? leadDTO.getFirstName() + " " : "")
					+ leadDTO.getLastName();
			htmlBody = htmlBody.replace(LEAD_NAME, leadName);
			htmlBody = htmlBody.replace("{{leadFieldsDetails}}", getLeadFieldsTable(leadDTO));
			htmlBody = htmlBody.replace("{{senderCompanyName}}", user.getCompanyProfile().getCompanyName());
			htmlBody = XamplifyUtils.isValidString(user.getCompanyProfile().getWebsite())
					? htmlBody.replace(VANITY_COMPANY_LOGO_HREF, user.getCompanyProfile().getWebsite())
					: htmlBody.replace(VANITY_COMPANY_LOGO_HREF, "");
			htmlBody = htmlBody.replace(replaceCompanyLogo,
					serverPath + XamplifyUtils.escapeDollarSequece(user.getCompanyProfile().getCompanyLogoPath()));
			String vanityURLDomain = xamplifyUtil.frameVanityURL(webUrl,
					user.getCompanyProfile().getCompanyProfileName());
			htmlBody = htmlBody.replace("<pageLink>", (hasVanityAccess ? vanityURLDomain : webUrl) + LOGIN);
			mailService.sendMail(new EmailBuilder().from(user.getEmailId()).senderName(senderName)
					.to(userDetails.getEmailId()).subject(subject).body(htmlBody).build());
		}
	}

	private String getLeadFieldsTable(LeadDTO leadDTO) {
		StringBuilder tableBuilder = new StringBuilder();
		tableBuilder.append("<table style='border:1px solid #dedede;padding:1em 3em;align:center;width:600px;;'>");
		String tableHtml = "<tr>"
				+ "<td style='font-size: 13px; line-height: 24px; padding: 15px 40px; border: 1px solid #dedede;'><b>{{labelName}}</b></td>"
				+ "<td  style='font-size: 13px; line-height: 24px; padding: 15px 40px; border: 1px solid #dedede;'>{{labelValue}}</td>"
				+ "</tr>";

		for (SfCustomFieldsDataDTO sfCustomFieldsDataDTO : leadDTO.getSfCfDataDtos()) {
			String rowHtml = tableHtml;

			if (sfCustomFieldsDataDTO.getSfCfLabelId() != null) {
				rowHtml = rowHtml.replace("{{labelName}}", sfCustomFieldsDataDTO.getSfCfLabelId());
			} else {
				rowHtml = rowHtml.replace("{{labelName}}", "");
			}

			if (sfCustomFieldsDataDTO.getValue() != null) {
				rowHtml = rowHtml.replace("{{labelValue}}", sfCustomFieldsDataDTO.getValue());
			} else {
				rowHtml = rowHtml.replace("{{labelValue}}", "");
			}
			tableBuilder.append(rowHtml);
		}

		tableBuilder.append("</table>");

		return tableBuilder.toString();
	}

}
