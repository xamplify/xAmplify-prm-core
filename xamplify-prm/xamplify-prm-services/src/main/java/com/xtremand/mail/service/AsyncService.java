package com.xtremand.mail.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;

import com.itextpdf.html2pdf.HtmlConverter;
import com.xtremand.activity.dto.ActivityAWSDTO;
import com.xtremand.activity.service.EmailActivityService;
import com.xtremand.approve.dto.ApprovalPrivilegesEmailNotificationDTO;
import com.xtremand.approve.dto.PendingApprovalDamAndLmsDTO;
import com.xtremand.aws.AmazonWebModel;
import com.xtremand.aws.AmazonWebService;
import com.xtremand.aws.CopiedFileDetails;
import com.xtremand.aws.FilePathAndThumbnailPath;
import com.xtremand.campaign.bom.DownloadDataInfo.DownloadItem;
import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.CompanyProfile.CompanyNameStatus;
import com.xtremand.common.bom.Criteria;
import com.xtremand.common.bom.Criteria.OPERATION_NAME;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.common.bom.Pagination;
import com.xtremand.company.dao.CompanyProfileDao;
import com.xtremand.dam.bom.Dam;
import com.xtremand.dam.bom.DamPartner;
import com.xtremand.dam.bom.DamPartnerGroupMapping;
import com.xtremand.dam.bom.DamPartnerGroupUserMapping;
import com.xtremand.dam.dao.DamDao;
import com.xtremand.dam.dto.ApprovalStatusHistoryDTO;
import com.xtremand.dam.dto.DamAwsDTO;
import com.xtremand.dam.dto.DamBasicInfo;
import com.xtremand.dam.dto.DamPartnerDetailsDTO;
import com.xtremand.dam.dto.DamPostDTO;
import com.xtremand.dam.dto.DamPreviewDTO;
import com.xtremand.dam.dto.DamUploadPostDTO;
import com.xtremand.dam.exception.DamDataAccessException;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.dashboard.button.dao.DashboardButtonDao;
import com.xtremand.dashboard.buttons.bom.DashboardButton;
import com.xtremand.dashboard.buttons.bom.DashboardButtonsPartnerCompanyMapping;
import com.xtremand.dashboard.buttons.bom.DashboardButtonsPartnerGroupMapping;
import com.xtremand.dashboard.buttons.dto.DashboardButtonsDTO;
import com.xtremand.dashboard.buttons.dto.DashboardButtonsEmailNotificationRequestDTO;
import com.xtremand.dashboard.buttons.dto.DashboardButtonsPartnersDTO;
import com.xtremand.dashboard.buttons.dto.DashboardButtonsToPartnersDTO;
import com.xtremand.deal.dao.DealDAO;
import com.xtremand.deal.dto.DealDto;
import com.xtremand.domain.dao.DomainDao;
import com.xtremand.domain.dto.DomainMediaResourceDTO;
import com.xtremand.exception.EmailNotificationException;
import com.xtremand.flexi.fields.dao.FlexiFieldDao;
import com.xtremand.flexi.fields.dto.FlexiFieldRequestDTO;
import com.xtremand.form.bom.Form;
import com.xtremand.form.dao.FormDao;
import com.xtremand.form.dto.FormDataForLeadNotification;
import com.xtremand.form.dto.FormLeadsNotificationDTO;
import com.xtremand.form.exception.FormLeadNotifictionException;
import com.xtremand.form.submit.bom.FormSubmit;
import com.xtremand.form.submit.bom.FormSubmitEnum;
import com.xtremand.form.submit.dto.FormSubmitDTO;
import com.xtremand.formbeans.AddPartnerResponseDTO;
import com.xtremand.formbeans.EmailDTO;
import com.xtremand.formbeans.EmailTemplateDTO;
import com.xtremand.formbeans.LeadDTO;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.formbeans.UserListDTO;
import com.xtremand.formbeans.UserListPaginationWrapper;
import com.xtremand.formbeans.VendorInvitationDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.gdpr.setting.service.GdprSettingService;
import com.xtremand.high.level.analytics.dao.HighLevelAnalyticsDao;
import com.xtremand.high.level.analytics.service.HighLevelAnalyticsService;
import com.xtremand.highlevel.analytics.bom.DownloadRequest;
import com.xtremand.highlevel.analytics.bom.DownloadStatus;
import com.xtremand.highlevel.analytics.dto.DownloadRequestUserDetailsDTO;
import com.xtremand.lead.dao.LeadDAO;
import com.xtremand.lead.dto.LeadDto;
import com.xtremand.lms.bom.LearningTrack;
import com.xtremand.lms.bom.LearningTrackContent;
import com.xtremand.lms.bom.LearningTrackType;
import com.xtremand.lms.bom.LearningTrackVisibility;
import com.xtremand.lms.bom.LearningTrackVisibilityGroup;
import com.xtremand.lms.dao.LMSDAO;
import com.xtremand.lms.dto.LearningTrackDto;
import com.xtremand.mail.bom.EmailTemplate;
import com.xtremand.mail.service.MailService.EmailBuilder;
import com.xtremand.mdf.bom.MdfAmountType;
import com.xtremand.mdf.bom.MdfDetails;
import com.xtremand.mdf.bom.MdfRequest;
import com.xtremand.mdf.dao.MdfDao;
import com.xtremand.mdf.dto.MdfRequestViewDTO;
import com.xtremand.module.dao.ModuleDao;
import com.xtremand.partner.journey.bom.WorkflowEmailSentLog;
import com.xtremand.partner.journey.dto.WorkflowRequestDTO;
import com.xtremand.partnership.bom.Partnership;
import com.xtremand.partnership.dao.PartnershipDAO;
import com.xtremand.partnership.dto.LoginAsPartnerDTO;
import com.xtremand.partnership.service.impl.PartnershipServiceHelper;
import com.xtremand.signup.dto.SignUpRequestDTO;
import com.xtremand.team.dao.TeamDao;
import com.xtremand.team.member.dto.TeamMemberDTO;
import com.xtremand.team.member.dto.TeamMemberGroupDTO;
import com.xtremand.team.member.group.bom.TeamMemberGroup;
import com.xtremand.team.member.group.dao.TeamMemberGroupDao;
import com.xtremand.user.bom.ModulesDisplayType;
import com.xtremand.user.bom.Role;
import com.xtremand.user.bom.ShareLeadsDTO;
import com.xtremand.user.bom.TeamMember;
import com.xtremand.user.bom.TeamMemberEmailsHistory;
import com.xtremand.user.bom.User;
import com.xtremand.user.bom.User.UserDefaultPage;
import com.xtremand.user.bom.User.UserStatus;
import com.xtremand.user.bom.UserList;
import com.xtremand.user.bom.UserUserList;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.user.service.UserService;
import com.xtremand.userlist.dao.UserListDAO;
import com.xtremand.userlist.service.UserListService;
import com.xtremand.util.FileUtil;
import com.xtremand.util.GenerateRandomPassword;
import com.xtremand.util.XamplifyUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.dao.XamplifyUtilDao;
import com.xtremand.util.dto.ActiveAndInActivePartnerEmailNotificationDTO;
import com.xtremand.util.dto.ContentSharedEmailNotificationDTO;
import com.xtremand.util.dto.LoginAsEmailNotificationDTO;
import com.xtremand.util.dto.ModuleCustomDTO;
import com.xtremand.util.dto.PartnerOrContactInputDTO;
import com.xtremand.util.dto.ShareContentRequestDTO;
import com.xtremand.util.dto.UserDetailsUtilDTO;
import com.xtremand.util.service.DownloadDataUtilService;
import com.xtremand.util.service.EmailValidatorService;
import com.xtremand.util.service.ThymeLeafService;
import com.xtremand.util.service.UtilService;
import com.xtremand.vanity.email.templates.bom.CustomDefaultEmailTemplate;
import com.xtremand.vanity.email.templates.bom.DefaultEmailTemplate;
import com.xtremand.vanity.email.templates.bom.DefaultEmailTemplateType;
import com.xtremand.vanity.email.templates.bom.VendorEmailSentLog;
import com.xtremand.vanity.url.dao.VanityURLDao;
import com.xtremand.vanity.url.dto.VanityURLDTO;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;
import com.xtremand.vanity.url.service.VanityURLService;
import com.xtremand.video.bom.VideoFile;
import com.xtremand.videoencoding.service.FFMPEGStatus;
import com.xtremand.videoencoding.service.VideoBitRateConverter;
import com.xtremand.white.labeled.dto.DamVideoDTO;
import com.xtremand.workflow.service.WorkflowService;

@Service("asyncService")
@Transactional
public class AsyncService {

	private static final String USER_ID = "userId";

	private static final Logger logger = LoggerFactory.getLogger(AsyncService.class);

	private static final String ANCHOR_START_TAG = "<a style=\"color:#fff;background-color:#0943e5;text-decoration:none;padding:10px 40px\"";

	private static final String LOGIN = "login";

	private static final String PARTNER_COMPANY_NAME = "partnerCompanyName";

	private static final String WELCOME_DISPLAY_NAME = "welcomeDisplayName";

	private static final String REQUEST_TITLE = "requestTitle";

	private static final String PARTNER_MODULE_CUSTOM_NAME = "partnerModuleCustomName";

	private static final String ORGADMIN_COMPANY_PARAMETER = "<Org_Admin_Company>";

	@Value("${server_path}")
	String serverPath;

	@Value("${partner.register.message}")
	String partnerRegisteredMessage;

	@Value("${partner.signin.message}")
	String partnerSignInMessage;

	@Value("${partner.register.button}")
	String partnerRegisterButton;

	@Value("${partner.login.button}")
	String partnerLoginButton;

	@Value("${login.url}")
	String partnerLoginUrl;

	@Value("${signup.url}")
	String partnerSignUpUrl;

	@Value("${partner.dynamic.code}")
	String partnerSignInCode;

	@Value("${partner.button.code}")
	String partnerButtonCode;

	@Value("${partner.login.code}")
	String partnerLoginCode;

	@Value("${tm.message.code}")
	String teamMemberMessageCode;

	@Value("${tm.message.button}")
	String teamMemberMessageButton;

	@Value("${tm.signup.message}")
	String teamMemberSignUpMessage;

	@Value("${tm.signin.message}")
	String teamMemberSignInMessage;

	@Value("${tm.signup.button.message}")
	String teamMemberSignUpButtonMessage;

	@Value("${tm.signin.button.message}")
	String teamMemberSignInButtonMessage;

	@Value("${access.button}")
	String accessButton;

	@Value("${email}")
	String fromEmail;

	@Value("${mail.sender}")
	String fromName;

	@Value("${unsubscirbeDiv}")
	String unsubscirbeDiv;

	@Value("${unsubscribeUrl}")
	String unsubscribeUrl;

	@Value("${web_url}")
	String webUrl;

	@Value("${appUrl}")
	String appUrl;

	@Value("${support.email.id}")
	String supportEmailId;

	@Value("${page.shared.subject}")
	String pageSharedSubject;

	@Value("${amazon.images.folder}")
	String amazonImageFolder;

	@Value("${amazon.base.url}")
	String amazonBaseUrl;

	@Value("${amazon.bucket.name}")
	String amazonBucketName;

	@Value("${amazon.env.folder}")
	String amazonEnvFolder;

	@Value("${amazon.email.templates.folder}")
	String amazonEmailTemplatesFolder;

	@Value("${amazon.landing.pages.folder}")
	String amazonLandingPagesFolder;

	@Value("${amazon.campaign.email.templates.folder}")
	String amazonCampaignEmailTemplatesFolder;

	@Value("${amazon.dam.bee.thumbnail.folder}")
	String amazonDamBeeThumbnailFolder;

	@Value("${amazon.previews.folder}")
	String amazonPreviewFolder;

	@Value("${spring.profiles.active}")
	private String profiles;

	@Value("${replace.there}")
	private String replaceThere;

	@Value("${partner.email.template}")
	String partnerEmailTemplate;

	@Value("${form.lead.notification.subject}")
	String formLeadNotificationSubject;

	@Value("${replace.company.logo}")
	private String replaceCompanyLogo;

	@Value("${new.mdf.request.vendor.notification}")
	private String mdfRequestVendorNotification;

	@Value("${new.mdf.request.partner.notification}")
	private String mdfRequestPartnerNotification;

	@Value("${mdfAmountUpdatedByYourVendor}")
	private String mdfAmountUpdatedByYourVendor;

	@Value("${mdfAmountUpdatedForYourPartner}")
	private String mdfAmountUpdatedForYourPartner;

	@Value("${mdfRequestDocumentUploadedPartnerNotification}")
	private String mdfRequestDocumentUploadedPartnerNotification;

	@Value("${mdfRequestDocumentUploadedVendorNotification}")
	private String mdfRequestDocumentUploadedVendorNotification;

	@Value("${mdfRequestStatusChangedPartnerNotification}")
	private String mdfRequestStatusChangedPartnerNotification;

	@Value("${amazon.dam.folder}")
	private String awsDamFolder;

	@Value("${partnerTemplateSchedulerTempPath}")
	private String partnerTemplateSchedulerTempPath;

	@Value("${partnerTemplateSchedulerAwsPath}")
	private String partnerTemplateSchedulerAwsPath;

	@Value("${lms.publish.partner.subject}")
	String lmsPublishPartnerSubject;

	@Value("${lms.publish.vendor.subject}")
	String lmsPublishVendorSubject;

	@Value("${sf.refresh.token.expired}")
	String sfExpiredVendorSubject;

	@Value("${playbook.publish.partner.subject}")
	String playbookPublishPartnerSubject;

	@Value("${playbook.publish.vendor.subject}")
	String playbookPublishVendorSubject;

	@Value("${lms.bulk.publish.partner.subject}")
	String lmsBulkPublishPartnerSubject;

	@Value("${playbook.bulk.publish.partner.subject}")
	String playbookBulkPublishPartnerSubject;

	@Value("${microsoft.config.issue}")
	String microsoftConfigErrorMsg;

	@Value("${amazon.high.level.analytics.folder}")
	private String highLevelAnalyticsFolder;

	@Value("${executor.service}")
	private String executorService;

	@Value("${crm.integration.invalid.pre}")
	String crmIntegrationInvalidSubjectPre;

	@Value("${crm.integration.invalid.post}")
	String crmIntegrationInvalidSubjectPost;

	@Value("${media_base_path}")
	String mediaBasePath;

	@Value("${campaigns_data_folder}")
	String campaignsDataFolder;

	@Value("${leads_data_folder}")
	String leadsDataFolder;

	@Value("${deals_data_folder}")
	String dealsDataFolder;

	@Value("${master_partner_list_folder}")
	String masterPartnerList;

	@Value("${partner_list_folder}")
	String partnerList;

	@Value("${contact_list_folder}")
	String contactList;

	@Value("${share_leads_list_folder}")
	String shareLeadsList;

	@Value("${shared_leads_list_folder}")
	String sharedLeadsList;

	@Value("${specialCharacters}")
	String regex;

	@Value("${server_path}")
	String server_path;

	@Value("${upload.company.domain.images.path}")
	private String uploadCompanyDomainImagesPath;

	@Value("${chrome_binari_path}")
	private String chromeBinariPath;

	@Value("${processingGifPath}")
	private String processingGifPath;

	@Value("${chatGpt.fileDelte.endpoint}")
	private String fileUploadEndPoint;

	@Value("${chatGpt.thread.delete.endpoint}")
	private String threadEndPoint;

	@Value("${co.branding.logo}")
	String coBrandingLogo;

	@Value("${xamplify.logo}")
	String xAmplifyLogo;

	@Autowired
	private UserDAO userDAO;

	@Autowired
	private UserListDAO userListDAO;

	@Autowired
	GenericDAO genericDAO;

	@Autowired
	private MailService mailService;

	@Autowired
	EmailValidatorService emailValidatorService;

	@Autowired
	private FormDao formDao;

	@Autowired
	private SpringTemplateEngine templateEngine;

	@Autowired
	private UtilService utilService;

	@Autowired
	private AmazonWebService amazonWebService;

	@Autowired
	private VideoBitRateConverter videoBitRateConverter;

	@Autowired
	private VanityURLService vanityURLService;

	@Autowired
	private VanityURLDao vanityURLDao;

	@Autowired
	private GdprSettingService gdprSettingService;

	@Autowired
	private MdfDao mdfDao;

	@Autowired
	private PartnershipDAO partnershipDao;

	@Autowired
	private XamplifyUtil xamplifyUtil;

	@Autowired
	private DamDao damDao;

	@Autowired
	private LMSDAO lmsDao;

	@Autowired
	private TeamDao teamDao;

	@Autowired
	private TeamMemberGroupDao teamMemberGroupDao;

	@Autowired
	UserListService userListService;

	@Autowired
	private ThymeLeafService thymeleafService;

	@Autowired
	private UtilDao utilDao;

	@Autowired
	private HighLevelAnalyticsService highlevalAnalyticsService;

	@Autowired
	private HighLevelAnalyticsDao highlevelAnalyticsDao;

	@Autowired
	private UserService userService;

	@Value("${saveas.draft.notification.subject}")
	String saveAsDraftNotificationSubject;

	@Autowired
	private FileUtil fileUtil;

	@Autowired
	@Lazy
	private AsyncComponent asyncComponent;

	@Autowired
	private XamplifyUtilDao xamplifyUtilDao;

	@Autowired
	private DashboardButtonDao dashboardButtonDao;

	@Value("${zerobounce.no.credits.message}")
	String zerobounceNoCreditsMessage;

	/* XNFR-215 */
	@Value("#{'${crms.without.lead.pipelines}'.split(',')}")
	private List<String> crmsWithoutLeadPipelines;

	@Value("#{'${crms.without.deal.pipelines}'.split(',')}")
	private List<String> crmsWithoutDealPipelines;
	/* XNFR-215 */

	/*** XNFR-326 ****/
	@Autowired
	private CompanyProfileDao companyProfileDao;

	@Autowired
	private DownloadDataUtilService downloadDataUtilService;

	/**** XNFR-233 ***/

	@Autowired
	private FlexiFieldDao flexiFieldDao;

	/** XNFR-735 **/
	@Autowired
	private EmailActivityService emailActivityService;

	@Autowired
	private DomainDao domainDao;

	@Value("${upload_content_path}")
	String uploadContentPath;

	@Value("${upload.attachment.path}")
	private String attachmentPath;

	@Value("${separator}")
	private String sep;

	@Autowired
	PartnershipServiceHelper partnershipServiceHelper;

	/** XNFr-892 **/
	@Autowired
	private LeadDAO leadDAO;

	@Autowired
	private DealDAO dealDAO;

	@Autowired
	private ModuleDao moduleDao;

	@Value("${content.pdf.path}")
	private String contentPdfPath;

	@Autowired
	private WorkflowService workflowService;

	public void sendPartnerMail(User user, int templateId, User customer, Integer userListId) {
		EmailTemplate template = genericDAO.get(EmailTemplate.class, templateId);
		String subject = template.getSubject();

		if (StringUtils.hasText(user.getPassword()) && UserStatus.APPROVED.equals(user.getUserStatus())) {
			subject = subject.replace("Join", "Login");
		}

		subject = subject.replaceAll(ORGADMIN_COMPANY_PARAMETER,
				customer.getCompanyProfile().getCompanyName() != null
						? XamplifyUtils.escapeDollarSequece(customer.getCompanyProfile().getCompanyName())
						: "");

		String body = template.getBody();
		body = body.replaceAll("<Org_Admin_First_Name>",
				customer.getFirstName() != null ? XamplifyUtils.escapeDollarSequece(customer.getFirstName()) : "");
		body = body.replaceAll("<Org_Admin_Last_Name>",
				customer.getLastName() != null ? XamplifyUtils.escapeDollarSequece(customer.getLastName()) : "");
		body = body.replaceAll("<Org_Admin_Title>",
				customer.getJobTitle() != null ? XamplifyUtils.escapeDollarSequece(customer.getJobTitle()) : "");
		body = body.replaceAll(ORGADMIN_COMPANY_PARAMETER,
				customer.getCompanyProfile().getCompanyName() != null
						? XamplifyUtils.escapeDollarSequece(customer.getCompanyProfile().getCompanyName())
						: "");
		body = body.replaceAll("<Org_Admin_Company_logo>",
				serverPath + XamplifyUtils.escapeDollarSequece(customer.getCompanyProfile().getCompanyLogoPath()));
		body = XamplifyUtils.replaceCustomerFullName(user, body);
		body = XamplifyUtils.replacePartnerFullName(user, body);
		String firstName = customer.getFirstName() != null ? XamplifyUtils.escapeDollarSequece(customer.getFirstName())
				: customer.getEmailId();
		body = sendPartnerSignUpOrSignInEmail(body, user, customer, userListId);
		body = body.replaceAll("<Vanity_Company_Logo_Href>", customer.getCompanyProfile().getWebsite());
		if (user.isNotifyPartners()) {
			String templateBody = body;
			templateBody = sendPartnerSignUpOrSignInEmail(templateBody, user, customer, userListId);
			templateBody = XamplifyUtils.replaceCustomerFullName(user, templateBody);
			templateBody = XamplifyUtils.replacePartnerFullName(user, templateBody);
			mailService.sendMail(new EmailBuilder().from(customer.getEmailId()).senderName(firstName)
					.to(user.getEmailId()).subject(subject).body(templateBody).build());
		} else {
			String message = "Notify Partners is OFF for " + user.getEmailId();
			logger.debug(message);
		}
	}

	public void sendPartnerMail(List<User> partnersList, int templateId, User customer, Integer userListId) {
		String subject = null;
		String body = null;
		CompanyProfile companyProfile = customer.getCompanyProfile();
		String verifyEmailLink = "";
		DefaultEmailTemplateType defaultEmailTemplateType = null;
		if (StringUtils.hasText(customer.getCompanyProfileName())) {
			DefaultEmailTemplate vanityDefaultEmailTemplate = null;
			CustomDefaultEmailTemplate vanityEmailTemplate = null;
			boolean isPrm = utilDao.isPrmCompany(customer.getUserId());
			boolean isTemplateExist = false;
			if (isPrm) {
				isTemplateExist = vanityURLDao.getPrmCustomDefaultCount(companyProfile.getId());
				if (isTemplateExist) {
					defaultEmailTemplateType = DefaultEmailTemplateType.JOIN_VENDOR_COMPANY;
					vanityDefaultEmailTemplate = vanityURLDao
							.getVanityDefaultEmailTemplateByType(DefaultEmailTemplateType.JOIN_VENDOR_COMPANY);
					vanityEmailTemplate = vanityURLDao
							.getVanityETByDefVanityETIdAndCompanyId(vanityDefaultEmailTemplate.getId(), companyProfile);
				} else {
					defaultEmailTemplateType = DefaultEmailTemplateType.JOIN_PRM_COMPANY;
					vanityDefaultEmailTemplate = vanityURLDao
							.getVanityDefaultEmailTemplateByType(DefaultEmailTemplateType.JOIN_PRM_COMPANY);
					vanityEmailTemplate = vanityURLDao
							.getVanityETByDefVanityETIdAndCompanyId(vanityDefaultEmailTemplate.getId(), companyProfile);
				}
				String vanityURLDomain = xamplifyUtil.frameVanityURL(webUrl, customer.getCompanyProfileName());
				for (User partner : partnersList) {
					if (StringUtils.hasText(partner.getPassword())
							&& UserStatus.UNAPPROVED.equals(partner.getUserStatus())) {
						defaultEmailTemplateType = DefaultEmailTemplateType.ACCOUNT_ACTIVATION;
						vanityDefaultEmailTemplate = vanityURLDao
								.getVanityDefaultEmailTemplateByType(DefaultEmailTemplateType.ACCOUNT_ACTIVATION);
						vanityEmailTemplate = vanityURLDao.getVanityETByDefVanityETIdAndCompanyId(
								vanityDefaultEmailTemplate.getId(), companyProfile);
						verifyEmailLink = vanityURLDomain + "register/verifyemail/user?alias=" + partner.getAlias();
					} else {
						defaultEmailTemplateType = DefaultEmailTemplateType.JOIN_PRM_COMPANY;
						vanityDefaultEmailTemplate = vanityURLDao
								.getVanityDefaultEmailTemplateByType(DefaultEmailTemplateType.JOIN_PRM_COMPANY);
						vanityEmailTemplate = vanityURLDao.getVanityETByDefVanityETIdAndCompanyId(
								vanityDefaultEmailTemplate.getId(), companyProfile);
					}
				}
			} else {
				String vanityURLDomain = xamplifyUtil.frameVanityURL(webUrl, customer.getCompanyProfileName());
				for (User partner : partnersList) {
					if (StringUtils.hasText(partner.getPassword())
							&& UserStatus.UNAPPROVED.equals(partner.getUserStatus())) {
						defaultEmailTemplateType = DefaultEmailTemplateType.ACCOUNT_ACTIVATION;
						vanityDefaultEmailTemplate = vanityURLDao
								.getVanityDefaultEmailTemplateByType(DefaultEmailTemplateType.ACCOUNT_ACTIVATION);
						vanityEmailTemplate = vanityURLDao.getVanityETByDefVanityETIdAndCompanyId(
								vanityDefaultEmailTemplate.getId(), companyProfile);
						verifyEmailLink = vanityURLDomain + "register/verifyemail/user?alias=" + partner.getAlias();
					} else {
						defaultEmailTemplateType = DefaultEmailTemplateType.JOIN_VENDOR_COMPANY;
						vanityDefaultEmailTemplate = vanityURLDao
								.getVanityDefaultEmailTemplateByType(DefaultEmailTemplateType.JOIN_VENDOR_COMPANY);
						vanityEmailTemplate = vanityURLDao.getVanityETByDefVanityETIdAndCompanyId(
								vanityDefaultEmailTemplate.getId(), companyProfile);
					}
				}
			}
			if (vanityEmailTemplate != null) {
				subject = vanityEmailTemplate.getSubject();
				body = genericDAO.get(CustomDefaultEmailTemplate.class, vanityEmailTemplate.getId()).getHtmlBody();
			} else {
				subject = vanityDefaultEmailTemplate.getSubject();
				body = genericDAO.get(DefaultEmailTemplate.class, vanityDefaultEmailTemplate.getId()).getHtmlBody();
			}
			body = XamplifyUtils.replaceReceiverMergeTagsInfo(customer, body);
		}

		else {
			String vanityURLDomain = null;
			if (StringUtils.hasText(customer.getCompanyProfileName())) {
				vanityURLDomain = xamplifyUtil.frameVanityURL(webUrl, customer.getCompanyProfileName());
			} else {
				vanityURLDomain = webUrl;

			}

			boolean hasVanityAccess = utilDao.hasVanityAccessByUserId(customer.getUserId());
			if (hasVanityAccess) {
				boolean isPrm = utilDao.isPrmCompany(customer.getUserId());
				DefaultEmailTemplate vanityDefaultEmailTemplate = null;
				CustomDefaultEmailTemplate vanityEmailTemplate = null;
				for (User partner : partnersList) {
					if (StringUtils.hasText(partner.getPassword())
							&& UserStatus.UNAPPROVED.equals(partner.getUserStatus())) {
						defaultEmailTemplateType = DefaultEmailTemplateType.ACCOUNT_ACTIVATION;
						vanityDefaultEmailTemplate = vanityURLDao
								.getVanityDefaultEmailTemplateByType(DefaultEmailTemplateType.ACCOUNT_ACTIVATION);
						vanityEmailTemplate = vanityURLDao.getVanityETByDefVanityETIdAndCompanyId(
								vanityDefaultEmailTemplate.getId(), companyProfile);
						verifyEmailLink = vanityURLDomain + "register/verifyemail/user?alias=" + partner.getAlias();
					} else if (isPrm) {
						defaultEmailTemplateType = DefaultEmailTemplateType.JOIN_PRM_COMPANY;
						vanityDefaultEmailTemplate = vanityURLDao
								.getVanityDefaultEmailTemplateByType(DefaultEmailTemplateType.JOIN_PRM_COMPANY);
						vanityEmailTemplate = vanityURLDao.getVanityETByDefVanityETIdAndCompanyId(
								vanityDefaultEmailTemplate.getId(), companyProfile);
					} else {
						defaultEmailTemplateType = DefaultEmailTemplateType.JOIN_VENDOR_COMPANY;
						vanityDefaultEmailTemplate = vanityURLDao
								.getVanityDefaultEmailTemplateByType(DefaultEmailTemplateType.JOIN_VENDOR_COMPANY);
						vanityEmailTemplate = vanityURLDao.getVanityETByDefVanityETIdAndCompanyId(
								vanityDefaultEmailTemplate.getId(), companyProfile);
					}
					if (vanityEmailTemplate != null) {
						subject = vanityEmailTemplate.getSubject();
						body = genericDAO.get(CustomDefaultEmailTemplate.class, vanityEmailTemplate.getId())
								.getHtmlBody();
					} else {
						subject = vanityDefaultEmailTemplate.getSubject();
						body = genericDAO.get(DefaultEmailTemplate.class, vanityDefaultEmailTemplate.getId())
								.getHtmlBody();
					}

				}

			}

			else {
				boolean isPrm = utilDao.isPrmCompany(customer.getUserId());
				EmailTemplate template;
				if (isPrm) {
					template = genericDAO.get(EmailTemplate.class, EmailConstants.PRM_PARTNER_EMAIL);
				} else {
					template = genericDAO.get(EmailTemplate.class, templateId);
				}

				subject = template.getSubject();
				body = template.getBody();

				subject = subject.replaceAll(ORGADMIN_COMPANY_PARAMETER,
						companyProfile.getCompanyName() != null
								? XamplifyUtils.escapeDollarSequece(companyProfile.getCompanyName())
								: "");
				body = body.replaceAll("<Org_Admin_Company_logo>", xAmplifyLogo);
				body = body.replaceAll("<Vanity_Company_Logo_Href>", xamplifyUtil.getLoginUrl());
				for (User partner : partnersList) {

					if (XamplifyUtils.isValidString(partner.getPassword())
							&& UserStatus.UNAPPROVED.equals(partner.getUserStatus())) {
						EmailTemplate newUserTemplate = genericDAO.get(EmailTemplate.class,
								EmailConstants.NEW_USER_SIGNUP);
						if (newUserTemplate != null) {
							subject = newUserTemplate.getSubject();
							body = newUserTemplate.getBody();
							verifyEmailLink = vanityURLDomain + "register/verifyemail/user?alias=" + partner.getAlias();
							body = body.replaceAll("<Vanity_Company_Logo>", xAmplifyLogo);
							body = body.replaceAll("<Vanity_Company_Logo_Href>", xamplifyUtil.getLoginUrl());
						}
					}
				}
			}
		}

		body = body.replaceAll("<Org_Admin_First_Name>",
				customer.getFirstName() != null ? XamplifyUtils.escapeDollarSequece(customer.getFirstName()) : "");
		body = body.replaceAll("<Org_Admin_Middle_Name>",
				customer.getMiddleName() != null ? XamplifyUtils.escapeDollarSequece(customer.getMiddleName()) : "");
		body = body.replaceAll("<Org_Admin_Last_Name>",
				customer.getLastName() != null ? XamplifyUtils.escapeDollarSequece(customer.getLastName()) : "");
		body = body.replaceAll("<Org_Admin_Title>",
				customer.getJobTitle() != null ? XamplifyUtils.escapeDollarSequece(customer.getJobTitle()) : "");
		body = body.replaceAll(ORGADMIN_COMPANY_PARAMETER,
				companyProfile.getCompanyName() != null
						? XamplifyUtils.escapeDollarSequece(companyProfile.getCompanyName())
						: "");
		body = body.replaceAll("<Org_Admin_Company_logo>",
				serverPath + XamplifyUtils.escapeDollarSequece(companyProfile.getCompanyLogoPath()));
		body = body.replaceAll("<Vanity_Company_Logo>",
				server_path + XamplifyUtils.escapeDollarSequece(customer.getCompanyProfile().getCompanyLogoPath()));

		body = body.replaceAll("<VerifyEmailLink>", verifyEmailLink);
		body = body.replaceAll(replaceCompanyLogo,
				serverPath + XamplifyUtils.escapeDollarSequece(companyProfile.getCompanyLogoPath()));
		String firstName = (customer.getFirstName() != null && customer.getLastName() != null)
				? (XamplifyUtils.escapeDollarSequece(customer.getFirstName()) + " "
						+ XamplifyUtils.escapeDollarSequece(customer.getLastName()))
				: (customer.getFirstName() != null ? customer.getFirstName() : customer.getEmailId());

		for (User partner : partnersList) {
			if (partner.isNotifyPartners()) {
				String templateBody = body;
				if (StringUtils.hasText(partner.getPassword()) && UserStatus.APPROVED.equals(partner.getUserStatus())) {
					subject = subject.replace("Join", "Login");
					if (StringUtils.hasText(customer.getCompanyProfile().getCompanyProfileName())) {
						CompanyProfile cp = vanityURLDao.getCompanyProfileByCompanyProfileName(
								customer.getCompanyProfile().getCompanyProfileName());
						DefaultEmailTemplate vanityDefaultEmailTemplate = null;
						CustomDefaultEmailTemplate vanityEmailTemplate = null;
						if (cp != null && cp.getCompanyNameStatus() == CompanyNameStatus.INACTIVE) {
							defaultEmailTemplateType = DefaultEmailTemplateType.PARTNER_REMAINDER;
							vanityDefaultEmailTemplate = vanityURLDao
									.getVanityDefaultEmailTemplateByType(DefaultEmailTemplateType.PARTNER_REMAINDER);
						} else {
							defaultEmailTemplateType = DefaultEmailTemplateType.COMPANY_PROFILE_INCOMPLETE;
							vanityDefaultEmailTemplate = vanityURLDao.getVanityDefaultEmailTemplateByType(
									DefaultEmailTemplateType.COMPANY_PROFILE_INCOMPLETE);
						}

						if (cp != null) {
							vanityEmailTemplate = vanityURLDao
									.getVanityETByDefVanityETIdAndCompanyId(vanityDefaultEmailTemplate.getId(), cp);
						}

						if (vanityEmailTemplate != null) {
							subject = vanityEmailTemplate.getSubject();
						} else {
							subject = vanityDefaultEmailTemplate.getSubject();
						}
					}
				}
				templateBody = sendPartnerSignUpOrSignInEmail(templateBody, partner, customer, userListId);
				templateBody = XamplifyUtils.replaceCustomerFullName(partner, templateBody);
				templateBody = XamplifyUtils.replacePartnerFullName(partner, templateBody);
				templateBody = XamplifyUtils.replaceTemplateBody(partner, companyProfile, templateBody, customer);
				mailService.sendMail(new EmailBuilder().from(customer.getEmailId()).senderName(firstName)
						.to(partner.getEmailId()).subject(subject).body(templateBody).build());

				saveVendorEmailSentLog(customer, companyProfile, defaultEmailTemplateType, partner);

			} else {
				String message = "Notify Partners is OFF for " + partner.getEmailId();
				logger.debug(message);
			}

		}
	}

	// XNFR-1026
	private void saveVendorEmailSentLog(User customer, CompanyProfile companyProfile,
			DefaultEmailTemplateType defaultEmailTemplateType, User partner) {
		VendorEmailSentLog vendorEmailSentLog = new VendorEmailSentLog();
		vendorEmailSentLog.setVendorId(customer.getUserId());
		vendorEmailSentLog.setPartnerId(partner.getUserId());
		vendorEmailSentLog.setSentOn(new Date());
		vendorEmailSentLog.setPartnerCompanyId(userDAO.getCompanyIdByEmailId(partner.getEmailId()));
		vendorEmailSentLog.setVendorCompanyId(companyProfile.getId());
		vendorEmailSentLog.setType(defaultEmailTemplateType);
		genericDAO.save(vendorEmailSentLog);
	}

	/**
	 * @param body
	 * @param updatedPartner
	 * @return
	 */
	@SuppressWarnings("unused")
	private String sendPartnerSignUpOrSignInEmail(String body, User updatedPartner, User customer, Integer userListId) {
		/*****
		 * User Exists & If user has password/status is Approved.Then send
		 * login.Otherwise sign up
		 ******/
		String updateBody;
		String templatebody;
		String website = customer.getCompanyProfile().getWebsite();
		website = utilService.getWebsite(website);
		if (StringUtils.hasText(updatedPartner.getPassword())
				&& UserStatus.APPROVED.equals(updatedPartner.getUserStatus())) {
			boolean hasVanityAccess = utilDao.hasVanityAccessByUserId(customer.getUserId());

			if (hasVanityAccess && XamplifyUtils.isValidString(customer.getCompanyProfile().getCompanyProfileName())) {
				String subject;
				CompanyProfile cp = vanityURLDao
						.getCompanyProfileByCompanyProfileName(customer.getCompanyProfile().getCompanyProfileName());
				DefaultEmailTemplate vanityDefaultEmailTemplate;
				CustomDefaultEmailTemplate vanityEmailTemplate;
				if (cp != null && cp.getCompanyNameStatus() == CompanyNameStatus.INACTIVE) {
					vanityDefaultEmailTemplate = vanityURLDao
							.getVanityDefaultEmailTemplateByType(DefaultEmailTemplateType.PARTNER_REMAINDER);
				} else {
					vanityDefaultEmailTemplate = vanityURLDao
							.getVanityDefaultEmailTemplateByType(DefaultEmailTemplateType.COMPANY_PROFILE_INCOMPLETE);
				}

				vanityEmailTemplate = vanityURLDao
						.getVanityETByDefVanityETIdAndCompanyId(vanityDefaultEmailTemplate.getId(), cp);

				if (vanityEmailTemplate != null) {
					subject = vanityEmailTemplate.getSubject();
					templatebody = genericDAO.get(CustomDefaultEmailTemplate.class, vanityEmailTemplate.getId())
							.getHtmlBody();
				} else {
					subject = vanityDefaultEmailTemplate.getSubject();
					templatebody = genericDAO.get(DefaultEmailTemplate.class, vanityDefaultEmailTemplate.getId())
							.getHtmlBody();
				}

				body = XamplifyUtils.replaceReceiverMergeTagsInfo(customer, body);
				String vanityURLDomain = xamplifyUtil.frameVanityURL(webUrl, customer.getCompanyProfileName());
				body = body.replaceAll("<VerifyEmailLink>",
						vanityURLDomain + "register/verifyemail/user?alias=" + customer.getAlias());
				templatebody = templatebody.replaceAll("\\{\\{<Org_Admin_Company_logo>\\}\\}", serverPath
						+ XamplifyUtils.escapeDollarSequece(customer.getCompanyProfile().getCompanyLogoPath()));
				templatebody = templatebody.replaceAll("\\{\\{<Org_Admin_First_Name>\\}\\}",
						customer.getFirstName() != null ? XamplifyUtils.escapeDollarSequece(customer.getFirstName())
								: "");
				templatebody = templatebody.replaceAll("\\{\\{<Org_Admin_Last_Name>\\}\\}",
						customer.getLastName() != null ? XamplifyUtils.escapeDollarSequece(customer.getLastName())
								: "");
				templatebody = templatebody.replaceAll("\\{\\{senderFullName\\}\\}", customer.getFirstName()
						+ (XamplifyUtils.isValidString(customer.getLastName()) ? customer.getLastName() : ""));
				templatebody = templatebody.replaceAll("<Vanity_Company_Logo_Href>",
						XamplifyUtils.escapeDollarSequece(customer.getCompanyProfile().getWebsite()));
				templatebody = templatebody.replaceAll("<Vanity_Company_Logo>", server_path
						+ XamplifyUtils.escapeDollarSequece(customer.getCompanyProfile().getCompanyLogoPath()));
				templatebody = templatebody.replaceAll("<VerifyEmailLink>",
						webUrl + "register/verifyemail/user?alias=" + customer.getAlias());
				templatebody = templatebody.replaceAll("login_url", xamplifyUtil.frameVanityURL(partnerLoginUrl,
						customer.getCompanyProfile().getCompanyProfileName()));
				templatebody = templatebody.replaceAll(replaceCompanyLogo, server_path
						+ XamplifyUtils.escapeDollarSequece(customer.getCompanyProfile().getCompanyLogoPath()));
				templatebody = templatebody.replaceAll("\\{\\{VENDOR_COMPANY_NAME\\}\\}",
						XamplifyUtils.escapeDollarSequece(customer.getCompanyProfile().getCompanyName()));
				templatebody = templatebody.replaceAll("\\{\\{VENDOR_FULL_NAME\\}\\}",
						(customer.getFirstName() != null ? XamplifyUtils.escapeDollarSequece(customer.getFirstName())
								: "")
								+ " "
								+ (customer.getLastName() != null
										? XamplifyUtils.escapeDollarSequece(customer.getLastName())
										: ""));
				String firstName = updatedPartner.getFirstName() != null
						? XamplifyUtils.escapeDollarSequece(updatedPartner.getFirstName())
						: "";
				String lastName = updatedPartner.getLastName() != null
						? XamplifyUtils.escapeDollarSequece(updatedPartner.getLastName())
						: "";

				String fullName = firstName + (firstName.isEmpty() ? "" : " ") + lastName;
				if (fullName.trim().isEmpty()) {
					fullName = replaceThere;
				}

				templatebody = templatebody.replaceAll("\\{\\{_CUSTOMER_FULL_NAME\\}\\}", fullName);
				body = body.replaceAll("login_url", webUrl + xamplifyUtil.getLoginUrl());
				templatebody = templatebody.replaceAll(ORGADMIN_COMPANY_PARAMETER,
						customer.getCompanyProfile().getCompanyName() != null
								? XamplifyUtils.escapeDollarSequece(customer.getCompanyProfile().getCompanyName())
								: "");
			}

			else {
				EmailTemplate template = genericDAO.get(EmailTemplate.class,
						EmailConstants.PARTNER_EMAIL_FOR_SIGNED_USERS);
				templatebody = template.getBody();
				String vanityURLDomain = xamplifyUtil.frameVanityURL(webUrl, customer.getCompanyProfileName());
				body = body.replaceAll("<VerifyEmailLink>",
						vanityURLDomain + "register/verifyemail/user?alias=" + customer.getAlias());
				templatebody = templatebody.replaceAll("<Org_Admin_First_Name>",
						customer.getFirstName() != null ? XamplifyUtils.escapeDollarSequece(customer.getFirstName())
								: "");
				templatebody = templatebody.replaceAll("<Org_Admin_Last_Name>",
						customer.getLastName() != null ? XamplifyUtils.escapeDollarSequece(customer.getLastName())
								: "");
				templatebody = templatebody.replaceAll("<login_url>", partnerLoginUrl);

				String customerFullName = (updatedPartner.getFirstName() != null
						? XamplifyUtils.escapeDollarSequece(updatedPartner.getFirstName())
						: "")
						+ (updatedPartner.getLastName() != null
								? " " + XamplifyUtils.escapeDollarSequece(updatedPartner.getLastName())
								: "");
				customerFullName = customerFullName.isEmpty() ? replaceThere : customerFullName;
				templatebody = templatebody.replaceAll("_CUSTOMER_FULL_NAME", customerFullName);
				templatebody = templatebody.replaceAll("<VerifyEmailLink>",
						webUrl + "register/verifyemail/user?alias=" + customer.getAlias());
				templatebody = templatebody.replaceAll(replaceCompanyLogo, server_path
						+ XamplifyUtils.escapeDollarSequece(customer.getCompanyProfile().getCompanyLogoPath()));
				templatebody = templatebody.replaceAll("login_url", xamplifyUtil.frameVanityURL(partnerLoginUrl,
						customer.getCompanyProfile().getCompanyProfileName()));
				templatebody = templatebody.replaceAll("<Org_Admin_Company_logo>", xAmplifyLogo);
				templatebody = templatebody.replaceAll("<Vanity_Company_Logo_Href>", xamplifyUtil.getLoginUrl());

				templatebody = templatebody.replaceAll(ORGADMIN_COMPANY_PARAMETER,
						customer.getCompanyProfile().getCompanyName() != null
								? XamplifyUtils.escapeDollarSequece(customer.getCompanyProfile().getCompanyName())
								: "");
			}
			updateBody = setSignInText(templatebody, customer.getCompanyProfileName(), website);
		} else {
			updateBody = setSignUpText(body, updatedPartner.getAlias(), userListId, customer.getCompanyProfileName(),
					website);
		}
		return updateBody;
	}

	/**
	 * @param body
	 * @return
	 */
	private String setSignInText(String body, String companyProfileName, String website) {
		body = body.replaceAll(partnerSignInCode, partnerSignInMessage);
		body = body.replaceAll(partnerButtonCode, partnerLoginButton);
		if (StringUtils.hasText(companyProfileName)) {
			body = body.replaceAll("<Vanity_Company_Logo_Href>", website);
			body = body.replaceAll(partnerLoginCode, xamplifyUtil.frameVanityURL(partnerLoginUrl, companyProfileName));
		} else {
			body = body.replaceAll("<Vanity_Company_Logo_Href>", xamplifyUtil.getLoginUrl());
			body = body.replaceAll(partnerLoginCode, partnerLoginUrl);
		}
		return body;
	}

	/**
	 * @param body
	 * @return
	 */
	public String setSignUpText(String body, String alias, Integer userListId, String companyProfileName,
			String website) {
		body = body.replaceAll(partnerSignInCode, partnerRegisteredMessage);
		body = body.replaceAll(partnerButtonCode, partnerRegisterButton);
		if (StringUtils.hasText(companyProfileName)) {
			body = body.replaceAll("<Vanity_Company_Logo_Href>", website);
			body = body.replaceAll(partnerLoginCode,
					xamplifyUtil.frameVanityURL(partnerSignUpUrl, companyProfileName) + "/" + userListId + "-" + alias);
		} else {
			body = body.replaceAll("<Vanity_Company_Logo_Href>", xamplifyUtil.getLoginUrl());
			body = body.replaceAll(partnerLoginCode, partnerSignUpUrl + "/" + userListId + "-" + alias);
		}
		return body;
	}

	public void processVideo(String finalPath, int currentBitRate, VideoFile video, User user, Dam dam,
			DamUploadPostDTO damUploadPostDTO) {
		boolean isReplaceAsset = damUploadPostDTO.isReplaceAsset() && !damUploadPostDTO.isSendForReApproval();
		long startTime = System.currentTimeMillis();
		videoBitRateConverter.process(finalPath, currentBitRate, new FFMPEGStatus(), video, user, dam);
		/** XNFR-885 **/
		if (isReplaceAsset) {
			String newVideoUri = video.getUri();
			String previousVideoUri = damUploadPostDTO.getVideoUri();
			lmsDao.updateTrackDescriptionWithReplacedVideoUri(newVideoUri, previousVideoUri);
			String debugMessage = "Tracks Description " + previousVideoUri + " Updated With " + newVideoUri;
			logger.debug(debugMessage);
		}
		Set<Integer> partnerIds = damUploadPostDTO.getPartnerIds();
		Set<Integer> partnerGroupIds = damUploadPostDTO.getPartnerGroupIds();
		boolean isAssetCanBePublished = XamplifyUtils.isPartnerGroupOrPartnerCompanySelected(partnerGroupIds,
				partnerIds);
		if (isAssetCanBePublished) {
			DamVideoDTO damVideoDTO = damDao.findDamAndVideoDetailsByVideoId(video.getId());
			damUploadPostDTO.setDamVideoDTO(damVideoDTO);
			damDao.publishVideoAsset(dam, damUploadPostDTO);
			if (!isReplaceAsset) {
				asyncComponent.sendAssetSharedNotificationEmailsToPartners(damUploadPostDTO.getUpdatedPartnerIds(),
						dam.getCompanyProfile().getCompanyName(), damUploadPostDTO.getAssetName(),
						damUploadPostDTO.getLoggedInUserId(), damUploadPostDTO.getDamPartnerIds());
			}
		}

		long endTime = System.currentTimeMillis();
		long timeTaken = endTime - startTime;
		String debugMessage = "Video processing is completed for " + video.getTitle() + " in " + timeTaken;
		logger.debug(debugMessage);
	}

	public void sendTeamMemberEmailsAsync(List<User> teamMembers, User orgAdmin, String body,
			boolean resendingInvitation) {
		if (StringUtils.hasText(orgAdmin.getCompanyProfileName())) {
			CompanyProfile cp = orgAdmin.getCompanyProfile();
			VanityURLDTO vanityURLDto = vanityURLService.getCompanyDetails(utilDao.getPrmCompanyProfileName());
			String companyLogoPath = serverPath
					+ XamplifyUtils.escapeDollarSequece(vanityURLDto.getCompanyLogoImagePath());
			body = body.replace("<Org_Admin_Company_logo>", companyLogoPath);
			body = body.replace("<Vanity_Company_Logo_Href>", cp.getWebsite());
			body = body.replace(replaceCompanyLogo, companyLogoPath);
		} else {
			String companyLogoPath = serverPath
					+ XamplifyUtils.replaceNullWithEmptyString(orgAdmin.getCompanyProfile().getCompanyLogoPath());
			body = body.replace("<Org_Admin_Company_logo>", companyLogoPath);
			body = body.replace(replaceCompanyLogo, companyLogoPath);
		}
		for (User teamMember : teamMembers) {
			body = XamplifyUtils.replaceReceiverMergeTagsInfo(teamMember, body);
			String updatedBody = sendTeamMemberSignUpOrSignInEmail(body, teamMember, orgAdmin.getCompanyProfileName());
			String senderFullName = XamplifyUtils.getFullName(orgAdmin);
			updatedBody = updatedBody.replace("<<senderFullName>>", senderFullName);
			String emailId = teamMember.getEmailId();
			mailService.sendMail(new EmailBuilder().from(orgAdmin.getEmailId()).senderName(senderFullName).to(emailId)
					.subject(orgAdmin.getSubjectLine()).teamMemberEmail(true).userId(orgAdmin.getUserId())
					.invitationSentFromTeamMemberSection(resendingInvitation)
					.teamMemberId(teamMember.getTeamMemerPkId())
					.body(XamplifyUtils.replaceCustomerFullName(teamMember, updatedBody)).build());
		}
	}

	private String sendTeamMemberSignUpOrSignInEmail(String body, User teamMember, String companyProfileName) {
		String updateBody;
		if (StringUtils.hasText(teamMember.getPassword()) || teamMember.getUserStatus().equals(UserStatus.APPROVED)) {
			updateBody = setTeamMemberSignInText(body, companyProfileName);
		} else {
			updateBody = setTeamMemberSignUpText(body, teamMember, companyProfileName);
		}
		return updateBody;
	}

	private String setTeamMemberSignUpText(String body, User teamMember, String companyProfileName) {
		body = body.replaceAll(teamMemberMessageCode, teamMemberSignUpMessage);
		body = body.replaceAll(teamMemberMessageButton, teamMemberSignUpButtonMessage);
		if (StringUtils.hasText(companyProfileName)) {
			String vanityURLDomain = xamplifyUtil.frameVanityURL(webUrl, companyProfileName);
			body = body.replaceAll(accessButton,
					xamplifyUtil.frameVanityURL(partnerSignUpUrl, companyProfileName) + "/" + teamMember.getAlias());
			body = body.replaceAll("<Vanity_Company_Logo_Href>", vanityURLDomain);
		} else {
			body = body.replaceAll(accessButton, partnerSignUpUrl + "/" + teamMember.getAlias());
			body = body.replaceAll("<Vanity_Company_Logo_Href>", xamplifyUtil.getLoginUrl());
		}
		return body;
	}

	private String setTeamMemberSignInText(String body, String companyProfileName) {
		body = body.replaceAll(teamMemberMessageCode, teamMemberSignInMessage);
		body = body.replaceAll(teamMemberMessageButton, teamMemberSignInButtonMessage);
		if (StringUtils.hasText(companyProfileName)) {
			String vanityURLDomain = xamplifyUtil.frameVanityURL(webUrl, companyProfileName);
			body = body.replaceAll(accessButton, xamplifyUtil.frameVanityURL(partnerLoginUrl, companyProfileName));
			body = body.replaceAll("<Vanity_Company_Logo_Href>", vanityURLDomain);
		} else {
			body = body.replaceAll(accessButton, partnerLoginUrl);
			body = body.replaceAll("<Vanity_Company_Logo_Href>", xamplifyUtil.getLoginUrl());
		}

		return body;
	}

	public void sendVendorInvitation(VendorInvitationDTO vendorInvitationDTO, User sender,
			List<User> sendPartnerMailsList) {
		String fromEmail = sender.getEmailId();
		String senderName = (sender.getFirstName() != null) ? sender.getFirstName()
				: (sender.getLastName() != null ? sender.getLastName() : sender.getCompanyProfile().getCompanyName());
		String subject = vendorInvitationDTO.getSubject();

		Context context = new Context();
		Map<String, Object> model = new HashMap<>();
		model.put("vendorInvitationDTO", vendorInvitationDTO);
		model.put("sender", sender);
		model.put("senderCompanyLogo", serverPath + sender.getCompanyProfile().getCompanyLogoPath());
		context.setVariables(model);
		String html = templateEngine.process("vendor-invitation", context);
		for (User vendor : sendPartnerMailsList) {
			String email = vendor.getEmailId();
			mailService.sendMail(new EmailBuilder().from(fromEmail).senderName(senderName).to(email).subject(subject)
					.body(html).build());
		}
	}

	public void sendUserWelcomeEmailThroughAdmin(EmailDTO emailDTO) {
		List<String> emaiIds = emailDTO.getEmailIds();
		String body = emailDTO.getMessage();
		String accessUrl = null;
		accessUrl = emailDTO.getVanityURL();
		String accessUrlTag = "<a href=" + accessUrl;
		body = body.replace(accessUrlTag, ANCHOR_START_TAG + " href=" + accessUrl);
		String emailId = emaiIds.get(0);
		String subject = utilService.addPerfixToSubject(emailDTO.getSubject());
		mailService.sendMail(new EmailBuilder().from(fromEmail).senderName(fromName).to(emailId).subject(subject)
				.body(body).build());
	}

	public void sendFormLeadsNotification(FormSubmit formSubmit, FormSubmitDTO formSubmitDTO,
			List<FormDataForLeadNotification> formDataForLeadNotifications) {
		FormLeadsNotificationDTO formLeadsNotificationDTO = new FormLeadsNotificationDTO();
		Form form = formSubmit.getForm();
		formLeadsNotificationDTO.setSubmittedDetails(formDataForLeadNotifications);
		formLeadsNotificationDTO.setFormName(form.getFormName());
		formLeadsNotificationDTO.setFormAnalyticsUrl(webUrl + LOGIN);
		formLeadsNotificationDTO.setFormId(form.getId());
		formLeadsNotificationDTO.setVanityUrlFilter(formSubmitDTO.isVanityUrlFilter());
		FormSubmitEnum formSubmitEnum = formSubmit.getFormSubmitEnum();
		formLeadsNotificationDTO.setFormSubitEnum(formSubmitEnum);
		Integer userId = form.getCreatedUserId();
		Set<Integer> uniqueAdminAndTeamMemberIds = getFormAdminsAndTeamMembers(form, userId);
		Set<String> emailSentToEmailIds = new HashSet<>();
		findUserAndSendEmailNotification(formSubmit, formSubmitDTO, formLeadsNotificationDTO, formSubmitEnum, userId,
				false, emailSentToEmailIds);
		if (!emailSentToEmailIds.isEmpty()) {
			List<Integer> emailSentToUserIds = userDAO.getUserIdsByEmailIds(emailSentToEmailIds);
			uniqueAdminAndTeamMemberIds.removeAll(emailSentToUserIds);
		}
		sendEmailsToFormAdminsAndTeamMembers(formSubmit, formSubmitDTO, formLeadsNotificationDTO, formSubmitEnum,
				userId, uniqueAdminAndTeamMemberIds, emailSentToEmailIds);
	}

	/******* XNFR-108 *********/
	private void sendEmailsToFormAdminsAndTeamMembers(FormSubmit formSubmit, FormSubmitDTO formSubmitDTO,
			FormLeadsNotificationDTO formLeadsNotificationDTO, FormSubmitEnum formSubmitEnum, Integer userId,
			Set<Integer> uniqueAdminAndTeamMemberIds, Set<String> emailSentTo) {
		if (uniqueAdminAndTeamMemberIds != null && uniqueAdminAndTeamMemberIds.size() > 0) {
			for (Integer id : uniqueAdminAndTeamMemberIds) {
				findUserAndSendEmailNotification(formSubmit, formSubmitDTO, formLeadsNotificationDTO, formSubmitEnum,
						id, true, emailSentTo);
			}
		}
	}

	private Set<Integer> getFormAdminsAndTeamMembers(Form form, Integer userId) {
		Integer formCreatorCompanyId = userDAO.getCompanyIdByUserId(userId);

		/************
		 * Getting admin emailIds to send emails by vendor companyId
		 ************/
		List<UserDTO> formAdminDtos = userDAO.listAdminsByCompanyId(formCreatorCompanyId);
		List<Integer> formAdminIds = formAdminDtos.stream().map(UserDTO::getId).collect(Collectors.toList());

		/************
		 * Getting form team members
		 ************/
		List<Integer> selecetdTeamMemberIds = formDao.getSelectedTeamMemberIdsByFormId(form.getId());

		/************
		 * Combining admin and team member IDs to eliminate duplicates
		 ************/
		Set<Integer> uniqueAdminAndTeamMemberIds = new HashSet<>();
		uniqueAdminAndTeamMemberIds.addAll(formAdminIds);
		uniqueAdminAndTeamMemberIds.addAll(selecetdTeamMemberIds);
		return uniqueAdminAndTeamMemberIds;
	}

	/******* XNFR-108 ENDS *********/

	private void findUserAndSendEmailNotification(FormSubmit formSubmit, FormSubmitDTO formSubmitDTO,
			FormLeadsNotificationDTO formLeadsNotificationDTO, FormSubmitEnum formSubmitEnum, Integer userId,
			Boolean isFormAdminOrTeamMember, Set<String> emailSentTo) {
		User user = userDAO.getFirstNameAndEmailIdByUserId(userId);
		if (user != null && StringUtils.hasText(user.getEmailId())) {
			String emailId = user.getEmailId();
			formLeadsNotificationDTO.setEmailId(emailId);
			formLeadsNotificationDTO.setFirstName(user.getFirstName());
			if (FormSubmitEnum.FORM.equals(formLeadsNotificationDTO.getFormSubitEnum())) {
				formLeadsNotificationDTO.setFormLead(true);
				emailSentTo.add(formLeadsNotificationDTO.getEmailId());
				setEmailContentAndSendEmail(formLeadsNotificationDTO);
			}
		} else {
			throw new FormLeadNotifictionException("User does not exists for" + userId);
		}
	}

	private void setEmailContentAndSendEmail(FormLeadsNotificationDTO formLeadsNotificationDTO) {
		User user = userService.loadUser(
				Arrays.asList(new Criteria("emailId", OPERATION_NAME.eq,
						formLeadsNotificationDTO.getEmailId().trim().toLowerCase())),
				new FindLevel[] { FindLevel.COMPANY_PROFILE });
		CompanyProfile companyProfile = user.getCompanyProfile();
		boolean hasVanityAccess = utilDao.hasVanityAccessByUserId(user.getUserId());
		if (hasVanityAccess) {
			formCompletedNotificationForVanity(user, companyProfile, formLeadsNotificationDTO);
		} else {
			Context context = new Context();
			Map<String, Object> model = new HashMap<>();
			model.put("formLeadsNotification", formLeadsNotificationDTO);
			context.setVariables(model);
			String html = templateEngine.process("form-leads-notification", context);
			String message = "Sending Lead Notification " + formLeadsNotificationDTO.toString();
			logger.debug(message);
			String subject = utilService.addPerfixToSubject(formLeadNotificationSubject);
			mailService.sendMail(new EmailBuilder().from(fromEmail).senderName(fromName)
					.to(formLeadsNotificationDTO.getEmailId()).subject(subject).body(html).build());
		}
	}

	private void formCompletedNotificationForVanity(User user, CompanyProfile companyProfile,
			FormLeadsNotificationDTO formLeadsNotificationDTO) {

		String body = null;
		String subject = null;
		DefaultEmailTemplate vanityDefaultEmailTemplate = vanityURLDao
				.getVanityDefaultEmailTemplateByType(DefaultEmailTemplateType.FORM_COMPLETED);

		if (vanityDefaultEmailTemplate != null) {
			CustomDefaultEmailTemplate vanityEmailTemplate = vanityURLDao
					.getVanityETByDefVanityETIdAndCompanyId(vanityDefaultEmailTemplate.getId(), companyProfile);
			if (vanityEmailTemplate != null) {
				subject = vanityEmailTemplate.getSubject();
				body = genericDAO.get(CustomDefaultEmailTemplate.class, vanityEmailTemplate.getId()).getHtmlBody();
			} else {
				subject = vanityDefaultEmailTemplate.getSubject();
				body = genericDAO.get(DefaultEmailTemplate.class, vanityDefaultEmailTemplate.getId()).getHtmlBody();
			}
			body = XamplifyUtils.replaceReceiverMergeTagsInfo(user, body);
			if (StringUtils.hasText(companyProfile.getCompanyProfileName())) {
				body = body.replaceAll("<Vanity_Company_Logo>",
						server_path + XamplifyUtils.escapeDollarSequece(user.getCompanyProfile().getCompanyLogoPath()));
				body = body.replaceAll(replaceCompanyLogo,
						server_path + XamplifyUtils.escapeDollarSequece(user.getCompanyProfile().getCompanyLogoPath()));
				body = body.replaceAll(XamplifyUtils.replaceMergeTagPrefixer("{{senderCompanyName}}"),
						user.getCompanyProfile().getCompanyName());
				body = body.replaceAll(XamplifyUtils.replaceMergeTagPrefixer("{{customerFullName}}"),
						XamplifyUtils.getCustomerFullName(user));
				body = body.replaceAll(XamplifyUtils.replaceMergeTagPrefixer("{{formTable}}"),
						getFormTable(formLeadsNotificationDTO));

				String formName = formLeadsNotificationDTO.getFormName();
				String replacementValue = (formName != null && !formName.isEmpty()) ? formName : "default value";
				String replacementWithBold = "<b>" + replacementValue + "</b>";
				body = body.replaceAll(XamplifyUtils.replaceMergeTagPrefixer("{{formName}}"), replacementWithBold);
				body = body.replaceAll("<Vanity_Company_Logo_Href>", companyProfile.getWebsite());
			}

			else {
				body = replaceCompanyLogo(body);
			}
			body = body.replace("<pageLink>", xamplifyUtil.getLoginUrl());
			mailService.sendMail(
					new EmailBuilder().from(fromEmail).senderName(fromName).to(formLeadsNotificationDTO.getEmailId())
							.subject(utilService.addPerfixToSubject(subject)).body(body).build());
		}

	}

	private String getFormTable(FormLeadsNotificationDTO formLeadsNotificationDTO) {

		StringBuilder tableBuilder = new StringBuilder();

		tableBuilder.append("<table style='border:1px solid #dedede;padding:1em 3em;align:center;width:600px;;'>");

		String tableHtml = "<tr>"
				+ "<td style='font-size: 13px; line-height: 24px; padding: 15px 40px; border: 1px solid #dedede;'><b>{{labelName}}</b></td>"
				+ "<td  style='font-size: 13px; line-height: 24px; padding: 15px 40px; border: 1px solid #dedede;'>{{labelValue}}</td>"
				+ "</tr>";

		for (FormDataForLeadNotification submittedData : formLeadsNotificationDTO.getSubmittedDetails()) {
			String rowHtml = tableHtml;

			if (submittedData.getLabelName() != null) {
				rowHtml = rowHtml.replace("{{labelName}}", submittedData.getLabelName());
			} else {
				rowHtml = rowHtml.replace("{{labelName}}", "");
			}

			if (submittedData.getLabelValue() != null) {
				rowHtml = rowHtml.replace("{{labelValue}}", submittedData.getLabelValue());
			} else {
				rowHtml = rowHtml.replace("{{labelValue}}", "");
			}
			tableBuilder.append(rowHtml);
		}

		tableBuilder.append("</table>");

		return tableBuilder.toString();
	}

	/**
	 * @param builder
	 */
	public void saveTeamMemberEmailsHistory(EmailBuilder builder) {
		if (builder.getTeamMemberId() != null && builder.getTeamMemberId() > 0) {
			TeamMemberEmailsHistory teamMemberEmailsHistory = new TeamMemberEmailsHistory();
			teamMemberEmailsHistory.setSentFromTeamMemberSection(builder.isInvitationSentFromTeamMemberSection());
			teamMemberEmailsHistory.setTeamMemberId(builder.getTeamMemberId());
			teamMemberEmailsHistory.setSentBy(builder.getUserId());
			teamMemberEmailsHistory.setSentTime(new Date());
			genericDAO.save(teamMemberEmailsHistory);
		}

	}

	public void sendNewMdfRequestNotification(MdfRequest mdfRequest, String requestTitle, String requestedAmount) {
		Integer partnershipId = mdfRequest.getPartnership().getId();
		Integer createdUserId = mdfRequest.getCreatedBy();
		Integer vendorCompanyId = mdfRequest.getVendorCompanyId();
		Integer partnerCompanyId = mdfRequest.getPartnerCompanyId();

		String partnerCompanyName = mdfDao.getPartnerCompanyNameByVendorCompanyId(vendorCompanyId, createdUserId);
		String requestCreatedByDisplayName = mdfDao.getPartnerDisplayNameForMdfEmailNotification(partnershipId,
				createdUserId);
		List<UserDTO> vendors = userDAO.listAdminsByCompanyId(vendorCompanyId);
		List<UserDTO> partners = userDAO.listAdminsByCompanyId(partnerCompanyId);
		CompanyProfile vendorCompany = genericDAO.get(CompanyProfile.class, vendorCompanyId);
		UserDTO requestCreatedUserProfileDetails = userDAO.getEmailIdAndDisplayName(createdUserId);
		String partnerModuleCustomName = utilService.findPartnerModuleCustomNameByCompanyId(vendorCompanyId);

		/************
		 * Getting admin emailIds to send emails by vendor companyId
		 ************/
		sendNewMdfRequestNotificationsToVendorCompany(requestTitle, requestedAmount, partnerCompanyName,
				requestCreatedByDisplayName, vendors, partnerModuleCustomName);

		/************
		 * Sending Email To loggedInUser who created the request
		 ************/
		sendNewMdfRequestNotificationToRequestCreator(requestTitle, requestedAmount, vendorCompany,
				requestCreatedUserProfileDetails);

		/************
		 * Getting admin emailIds to send emails by partner companyId
		 ************/
		sendNewMdfRequestNotificationsToPartnerCompany(requestTitle, requestedAmount, partners, vendorCompany,
				requestCreatedUserProfileDetails);

	}

	private void sendNewMdfRequestNotificationToRequestCreator(String requestTitle, String requestedAmount,
			CompanyProfile vendorCompany, UserDTO requestCreatedUserProfileDetails) {
		sendMdfNotificationToPartner(requestTitle, requestedAmount, vendorCompany,
				requestCreatedUserProfileDetails.getFullName(), requestCreatedUserProfileDetails.getEmailId(),
				requestCreatedUserProfileDetails.getFullNameOrEmailId());
	}

	private void sendMdfNotificationToPartner(String requestTitle, String requestedAmount, CompanyProfile vendorCompany,
			String welcomeDisplayName, String receiverEmailId, String requestCreatedByUserProfileName) {
		Context context = new Context();
		Map<String, Object> model = new HashMap<>();
		model.put(WELCOME_DISPLAY_NAME, StringUtils.hasText(welcomeDisplayName) ? welcomeDisplayName : replaceThere);
		model.put("vendorCompanyName",
				vendorCompany != null && StringUtils.hasText(vendorCompany.getCompanyName())
						? vendorCompany.getCompanyName()
						: "");
		setModelProperties(requestTitle, requestedAmount, requestCreatedByUserProfileName, context, model);
		String htmlName = "new-mdf-request-partner-notification";
		String subject = utilService.addPerfixToSubject(mdfRequestPartnerNotification);
		sendEmailNotification(context, htmlName, receiverEmailId, subject);
	}

	private void sendNewMdfRequestNotificationsToPartnerCompany(String requestTitle, String requestedAmount,
			List<UserDTO> partners, CompanyProfile vendorCompany, UserDTO requestCreatedUserProfileDetails) {
		String requestCreatedByUserEmailId = requestCreatedUserProfileDetails != null
				&& StringUtils.hasText(requestCreatedUserProfileDetails.getEmailId())
						? requestCreatedUserProfileDetails.getEmailId()
						: "";
		String requestCreatedByUserFullName = requestCreatedUserProfileDetails != null
				? requestCreatedUserProfileDetails.getFullNameOrEmailId()
				: "";
		for (UserDTO partner : partners) {
			String receiverDisplayName = partner.getFullName();
			String receiverEmailId = partner.getEmailId();
			if (!receiverEmailId.equals(requestCreatedByUserEmailId)) {
				sendMdfNotificationToPartner(requestTitle, requestedAmount, vendorCompany, receiverDisplayName,
						receiverEmailId, requestCreatedByUserFullName);
			} else {
				logger.info("Request created by admin it self.No duplicate emails will be sent to {}",
						requestCreatedByUserEmailId);
			}

		}
	}

	private void sendNewMdfRequestNotificationsToVendorCompany(String requestTitle, String requestedAmount,
			String partnerCompanyName, String requestCreatedByDisplayName, List<UserDTO> vendors,
			String partnerModuleCustomName) {
		for (UserDTO vendor : vendors) {
			Context context = new Context();
			Map<String, Object> model = new HashMap<>();
			model.put(WELCOME_DISPLAY_NAME,
					StringUtils.hasText(vendor.getFullName()) ? vendor.getFullName() : replaceThere);
			model.put(PARTNER_COMPANY_NAME, StringUtils.hasText(partnerCompanyName) ? partnerCompanyName : "");
			model.put(PARTNER_MODULE_CUSTOM_NAME, partnerModuleCustomName);
			setModelProperties(requestTitle, requestedAmount, requestCreatedByDisplayName, context, model);
			String htmlName = "new-mdf-request-vendor-notification";
			String receiverEmailId = vendor.getEmailId();
			String subject = utilService.addPerfixToSubject(mdfRequestVendorNotification);
			sendEmailNotification(context, htmlName, receiverEmailId, subject);
		}
	}

	private void sendEmailNotification(Context context, String htmlName, String receiverEmailId, String subject) {
		String html = templateEngine.process(htmlName, context);
		mailService.sendMail(new EmailBuilder().from(fromEmail).senderName(fromName).to(receiverEmailId)
				.subject(subject).body(html).build());
	}

	private void setModelProperties(String requestTitle, String requestedAmount, String requestCreatedBy,
			Context context, Map<String, Object> model) {
		model.put(REQUEST_TITLE, StringUtils.hasText(requestTitle) ? requestTitle : "");
		model.put("requestedAmount", StringUtils.hasText(requestedAmount) ? requestedAmount : "$");
		model.put("requestCreatedBy", StringUtils.hasText(requestCreatedBy) ? requestCreatedBy : "");
		setLoginUrl(model);
		context.setVariables(model);
	}

	public void sendMdfAmountNotification(MdfDetails mdfDetails) {
		String mdfAmountType = mdfDetails.getMdfAmountType().name();
		Double mdfAmount = mdfDetails.getMdfAmount();
		Integer loggedInUserId = mdfDetails.getCreatedBy();
		Integer partnershipId = mdfDetails.getPartnership().getId();
		Partnership partnership = partnershipDao.getPartnershipById(partnershipId);
		Integer vendorCompanyId = partnership.getVendorCompany().getId();
		Integer partnerCompanyId = partnership.getPartnerCompany().getId();
		UserDTO loggedInUserDetails = userDAO.getEmailIdAndDisplayName(loggedInUserId);
		Integer partnerId = partnership.getRepresentingPartner().getUserId();
		String partnerEmailId = partnership.getRepresentingPartner().getEmailId();
		UserDTO partnerDetails = userDAO.getEmailIdAndDisplayName(partnerId);
		String partnerWelcomeDisplayName = partnerDetails != null && StringUtils.hasText(partnerDetails.getFullName())
				? partnerDetails.getFullName()
				: replaceThere;

		CompanyProfile vendorCompanyProfile = genericDAO.get(CompanyProfile.class, vendorCompanyId);
		String vendorCompanyName = vendorCompanyProfile != null ? vendorCompanyProfile.getCompanyName() : "";

		sendMdfAmountNotificationsToVendorCompany(mdfDetails, loggedInUserDetails, partnershipId, partnerId,
				vendorCompanyId);

		sendMdfAmountEmailNotificationToPartner(mdfAmountType, mdfAmount, partnerEmailId, vendorCompanyName,
				partnerWelcomeDisplayName);

		sendMdfAmountEmailNotificationToPartnerCompany(mdfAmountType, mdfAmount, partnerCompanyId, partnerEmailId,
				vendorCompanyName);

	}

	private void sendMdfAmountEmailNotificationToPartnerCompany(String mdfAmountType, Double mdfAmount,
			Integer partnerCompanyId, String partnerEmailId, String vendorCompanyName) {
		List<UserDTO> partners = userDAO.listAdminsByCompanyId(partnerCompanyId);

		for (UserDTO partner : partners) {
			String emailId = partner.getEmailId();
			if (!emailId.equals(partnerEmailId)) {
				sendMdfAmountEmailNotificationToPartner(mdfAmountType, mdfAmount, emailId, vendorCompanyName,
						partner.getFullName());
			} else {
				logger.debug("Partnership established with admin it self.No duplicate emails will be sent to {}",
						partnerEmailId);
			}
		}
	}

	private void sendMdfAmountEmailNotificationToPartner(String mdfAmountType, Double mdfAmount, String partnerEmailId,
			String vendorCompanyName, String welcomeDisplayName) {
		Context context = new Context();
		Map<String, Object> model = new HashMap<>();
		model.put("vendorCompanyName", vendorCompanyName);
		setMdfUtilProperties(mdfAmountType, mdfAmount, model, "", welcomeDisplayName);
		context.setVariables(model);
		String htmlName = "mdf-fund-partner-notification";
		String subject = utilService.addPerfixToSubject(mdfAmountUpdatedByYourVendor);
		sendEmailNotification(context, htmlName, partnerEmailId, subject);
	}

	private void sendMdfAmountNotificationsToVendorCompany(MdfDetails mdfDetails, UserDTO loggedInUserDetails,
			Integer partnershipId, Integer partnerId, Integer vendorCompanyId) {
		Integer loggedInUserCompanyId = mdfDetails.getCompanyProfile().getId();
		String mdfAmountType = mdfDetails.getMdfAmountType().name();
		Double mdfAmount = mdfDetails.getMdfAmount();
		String loggedInUserEmailId = loggedInUserDetails != null
				&& StringUtils.hasText(loggedInUserDetails.getEmailId()) ? loggedInUserDetails.getEmailId() : "";
		String mdfAmountAddedBy = loggedInUserDetails != null
				&& StringUtils.hasText(loggedInUserDetails.getFullNameOrEmailId())
						? loggedInUserDetails.getFullNameOrEmailId()
						: "";
		String loggedInUserWelcomeDisplayName = loggedInUserDetails != null
				&& StringUtils.hasText(loggedInUserDetails.getFullName()) ? loggedInUserDetails.getFullName()
						: replaceThere;
		String partnerCompanyName = mdfDao.getPartnerCompanyNameByVendorCompanyId(vendorCompanyId, partnerId);
		String partnerDisplayName = mdfDao.getPartnerDisplayNameForMdfEmailNotification(partnershipId, partnerId);
		String partnerModuleCustomName = utilService.findPartnerModuleCustomNameByCompanyId(loggedInUserCompanyId);
		/*********
		 * Send Email Notification To LoggedIn User Who Added/Removed Funds
		 ***********/
		sendMdfAmountNotificationToVendorCompanyUsers(partnerCompanyName, mdfAmountType, mdfAmount, loggedInUserEmailId,
				mdfAmountAddedBy, partnerDisplayName, loggedInUserWelcomeDisplayName, partnerModuleCustomName);

		List<UserDTO> vendors = userDAO.listAdminsByCompanyId(loggedInUserCompanyId);
		for (UserDTO vendor : vendors) {
			String emailId = vendor.getEmailId();
			if (!emailId.equals(loggedInUserEmailId)) {
				String welcomeDisplayName = StringUtils.hasText(vendor.getFullName()) ? vendor.getFullName()
						: replaceThere;
				sendMdfAmountNotificationToVendorCompanyUsers(partnerCompanyName, mdfAmountType, mdfAmount, emailId,
						mdfAmountAddedBy, partnerDisplayName, welcomeDisplayName, partnerModuleCustomName);
			} else {
				logger.debug("Fund details updated by admin it self.No duplicate emails will be sent to {}",
						loggedInUserEmailId);
			}
		}
	}

	private void sendMdfAmountNotificationToVendorCompanyUsers(String partnerCompanyName, String mdfAmountType,
			Double mdfAmount, String emailId, String mdfAmountAddedBy, String partnerDisplayName,
			String welcomeDisplayName, String partnerModuleCustomName) {
		Context context = new Context();
		Map<String, Object> model = new HashMap<>();
		setMdfUtilProperties(mdfAmountType, mdfAmount, model, mdfAmountAddedBy, welcomeDisplayName);
		model.put("partnerDisplayName", partnerDisplayName);
		model.put(PARTNER_COMPANY_NAME, partnerCompanyName);
		model.put(PARTNER_MODULE_CUSTOM_NAME, partnerModuleCustomName);
		context.setVariables(model);
		String htmlName = "mdf-fund-vendor-notification";
		String subject = utilService
				.addPerfixToSubject("MDF Amount Updated For One Of Your " + partnerModuleCustomName);
		sendEmailNotification(context, htmlName, emailId, subject);
	}

	private void setMdfUtilProperties(String mdfAmountType, Double mdfAmount, Map<String, Object> model,
			String mdfAmountAddedBy, String welcomeDisplayName) {
		model.put(WELCOME_DISPLAY_NAME, welcomeDisplayName);
		if (MdfAmountType.FUND_ADDED.name().equals(mdfAmountType)) {
			model.put("mdfAmountType", "Added");
		} else if (MdfAmountType.FUND_REMOVED.name().equals(mdfAmountType)) {
			model.put("mdfAmountType", "Removed");
		}
		model.put("mdfAmount", "$ " + mdfAmount);
		model.put("mdfAmountAddedBy", mdfAmountAddedBy);
		setLoginUrl(model);
	}

	private void setLoginUrl(Map<String, Object> model) {
		model.put("targetURL", webUrl + LOGIN);
	}

	public void sendMdfRequestDocumentUploadedNotification(String fileName, Integer loggedInUserId, Integer requestId) {
		MdfRequest mdfRequest = mdfDao.getMdfRequestById(requestId);
		String requestTitle = mdfDao.listTitleAndEventDateAndRequestAmountByRequestId(requestId).get(0);
		Partnership partnership = mdfRequest.getPartnership();
		Integer vendorCompanyId = partnership.getVendorCompany().getId();
		Integer partnerCompanyId = partnership.getPartnerCompany().getId();
		String partnerCompanyNameForVendorEmailNotification = mdfDao
				.getPartnerCompanyNameByVendorCompanyId(vendorCompanyId, loggedInUserId);
		List<UserDTO> vendors = userDAO.listAdminsByCompanyId(vendorCompanyId);
		List<UserDTO> partners = userDAO.listAdminsByCompanyId(partnerCompanyId);
		UserDTO documentUploaderUserProfileDetails = userDAO.getEmailIdAndDisplayName(loggedInUserId);

		String welcomeDisplayName = documentUploaderUserProfileDetails != null
				&& StringUtils.hasText(documentUploaderUserProfileDetails.getFullName())
						? XamplifyUtils.escapeDollarSequece(documentUploaderUserProfileDetails.getFullName())
						: replaceThere;
		String loggedInUserEmailId = documentUploaderUserProfileDetails != null
				&& StringUtils.hasText(documentUploaderUserProfileDetails.getEmailId())
						? documentUploaderUserProfileDetails.getEmailId()
						: "xxxxx";
		String uploadedBy = documentUploaderUserProfileDetails != null
				&& StringUtils.hasText(documentUploaderUserProfileDetails.getFullNameOrEmailId())
						? documentUploaderUserProfileDetails.getFullNameOrEmailId()
						: "";

		sendMdfRequestDocumentUploadedPartnerNotification(fileName, welcomeDisplayName, loggedInUserEmailId, uploadedBy,
				requestTitle);

		for (UserDTO partner : partners) {
			String partnerEmailId = partner.getEmailId();
			if (!partnerEmailId.equals(loggedInUserEmailId)) {
				sendMdfRequestDocumentUploadedPartnerNotification(fileName,
						XamplifyUtils.escapeDollarSequece(partner.getFullName()), partnerEmailId, uploadedBy,
						requestTitle);
			} else {
				logger.debug("Document has been uploaded by the admin it self.So No duplicate email will be sent {}",
						loggedInUserEmailId);
			}
		}
		String partnerModuleCustomName = utilService.findPartnerModuleCustomNameByCompanyId(vendorCompanyId);
		for (UserDTO vendor : vendors) {
			Context context = new Context();
			Map<String, Object> model = new HashMap<>();
			model.put(WELCOME_DISPLAY_NAME,
					StringUtils.hasText(vendor.getFullName()) ? XamplifyUtils.escapeDollarSequece(vendor.getFullName())
							: replaceThere);
			model.put(REQUEST_TITLE, requestTitle);
			model.put("uploadedFileName", fileName);
			model.put(PARTNER_COMPANY_NAME, partnerCompanyNameForVendorEmailNotification);
			model.put(PARTNER_MODULE_CUSTOM_NAME, partnerModuleCustomName);
			setLoginUrl(model);
			context.setVariables(model);
			String htmlName = "mdf-request-document-uploaded-vendor-notification";
			String subject = utilService
					.addPerfixToSubject("MDF Request Document Submitted By One Of Your " + partnerModuleCustomName);
			sendEmailNotification(context, htmlName, vendor.getEmailId(), subject);
		}

	}

	private void sendMdfRequestDocumentUploadedPartnerNotification(String fileName, String welcomeDisplayName,
			String receiverEmailId, String uploadedBy, String requestTitle) {
		Context context = new Context();
		Map<String, Object> model = new HashMap<>();
		model.put(WELCOME_DISPLAY_NAME, welcomeDisplayName);
		model.put(REQUEST_TITLE, requestTitle);
		model.put("uploadedBy", uploadedBy);
		model.put("uploadedFileName", fileName);
		setLoginUrl(model);
		context.setVariables(model);
		String htmlName = "mdf-request-document-uploaded-partner-notification";
		String subject = utilService.addPerfixToSubject(mdfRequestDocumentUploadedPartnerNotification);
		sendEmailNotification(context, htmlName, receiverEmailId, subject);
	}

	public void sendMdfRequestStatusChangedNotification(MdfRequestViewDTO mdfRequestViewDTO, MdfRequest mdfRequest) {
		Integer requestId = mdfRequestViewDTO.getId();
		String requestTitle = mdfDao.listTitleAndEventDateAndRequestAmountByRequestId(requestId).get(0);
		Integer loggedInUserId = mdfRequestViewDTO.getLoggedInUserId();
		Integer partnershipId = mdfRequest.getPartnership().getId();
		Partnership partnership = partnershipDao.getPartnershipById(partnershipId);
		Integer vendorCompanyId = partnership.getVendorCompany().getId();
		Integer partnerCompanyId = partnership.getPartnerCompany().getId();
		UserDTO loggedInUserDetails = userDAO.getEmailIdAndDisplayName(loggedInUserId);
		Integer partnerId = partnership.getRepresentingPartner().getUserId();
		String partnerEmailId = partnership.getRepresentingPartner().getEmailId();
		UserDTO partnerDetails = userDAO.getEmailIdAndDisplayName(partnerId);
		String partnerWelcomeDisplayName = partnerDetails != null && StringUtils.hasText(partnerDetails.getFullName())
				? partnerDetails.getFullName()
				: replaceThere;

		CompanyProfile vendorCompanyProfile = genericDAO.get(CompanyProfile.class, vendorCompanyId);
		String vendorCompanyName = vendorCompanyProfile != null ? vendorCompanyProfile.getCompanyName() : "";

		String loggedInUserEmailId = loggedInUserDetails != null
				&& StringUtils.hasText(loggedInUserDetails.getEmailId()) ? loggedInUserDetails.getEmailId() : "";
		String statusChangedBy = loggedInUserDetails != null
				&& StringUtils.hasText(loggedInUserDetails.getFullNameOrEmailId())
						? loggedInUserDetails.getFullNameOrEmailId()
						: "";
		String loggedInUserWelcomeDisplayName = loggedInUserDetails != null
				&& StringUtils.hasText(loggedInUserDetails.getFullName()) ? loggedInUserDetails.getFullName()
						: replaceThere;
		String partnerCompanyName = mdfDao.getPartnerCompanyNameByVendorCompanyId(vendorCompanyId, partnerId);
		/********* Send Emails To Vendor Company **************/
		sendStautsChangedNotificationToVendorCompany(mdfRequest, requestTitle, vendorCompanyId, loggedInUserEmailId,
				statusChangedBy, loggedInUserWelcomeDisplayName, partnerCompanyName);
		sendStatusChangedNotificationToPartnerCompany(mdfRequest, requestTitle, partnerCompanyId, partnerEmailId,
				partnerWelcomeDisplayName, vendorCompanyName);

	}

	private void sendStatusChangedNotificationToPartnerCompany(MdfRequest mdfRequest, String requestTitle,
			Integer partnerCompanyId, String partnerEmailId, String partnerWelcomeDisplayName,
			String vendorCompanyName) {
		sendStatusChangedNotificationToPartner(mdfRequest, requestTitle, partnerEmailId, partnerWelcomeDisplayName,
				vendorCompanyName);

		List<UserDTO> partners = userDAO.listAdminsByCompanyId(partnerCompanyId);
		for (UserDTO partner : partners) {
			String emailId = partner.getEmailId();
			if (!emailId.equals(partnerEmailId)) {
				sendStatusChangedNotificationToPartner(mdfRequest, requestTitle, emailId, partner.getFullName(),
						vendorCompanyName);
			} else {
				logger.debug("Partnership established with admin it self.No duplicate emails will be sent to {}",
						partnerEmailId);
			}
		}
	}

	private void sendStatusChangedNotificationToPartner(MdfRequest mdfRequest, String requestTitle,
			String partnerEmailId, String partnerWelcomeDisplayName, String vendorCompanyName) {
		Context context = new Context();
		Map<String, Object> model = new HashMap<>();
		requestStatusChangedUtil(mdfRequest, requestTitle, partnerWelcomeDisplayName, model);
		model.put("vendorCompanyName", vendorCompanyName);
		context.setVariables(model);
		String htmlName = "mdf-request-status-partner-notification";
		String subject = utilService.addPerfixToSubject(mdfRequestStatusChangedPartnerNotification);
		sendEmailNotification(context, htmlName, partnerEmailId, subject);
	}

	private void sendStautsChangedNotificationToVendorCompany(MdfRequest mdfRequest, String requestTitle,
			Integer vendorCompanyId, String loggedInUserEmailId, String statusChangedBy,
			String loggedInUserWelcomeDisplayName, String partnerCompanyName) {
		String partnerModuleCustomName = utilService.findPartnerModuleCustomNameByCompanyId(vendorCompanyId);

		sendStatusChangedNotification(mdfRequest, requestTitle, loggedInUserEmailId, statusChangedBy,
				loggedInUserWelcomeDisplayName, partnerCompanyName, partnerModuleCustomName);

		List<UserDTO> vendors = userDAO.listAdminsByCompanyId(vendorCompanyId);
		for (UserDTO vendor : vendors) {
			String emailId = vendor.getEmailId();
			if (!emailId.equals(loggedInUserEmailId)) {
				String welcomeDisplayName = StringUtils.hasText(vendor.getFullName()) ? vendor.getFullName()
						: replaceThere;
				sendStatusChangedNotification(mdfRequest, requestTitle, emailId, statusChangedBy, welcomeDisplayName,
						partnerCompanyName, partnerModuleCustomName);
			} else {
				logger.debug("Status changed by admin it self.No duplicate emails will be sent to {}",
						loggedInUserEmailId);
			}
		}
	}

	private void sendStatusChangedNotification(MdfRequest mdfRequest, String requestTitle, String loggedInUserEmailId,
			String statusChangedBy, String loggedInUserWelcomeDisplayName, String partnerCompanyName,
			String partnerModuleCustomName) {
		Context context = new Context();
		Map<String, Object> model = new HashMap<>();
		requestStatusChangedUtil(mdfRequest, requestTitle, loggedInUserWelcomeDisplayName, model);
		model.put(PARTNER_COMPANY_NAME, partnerCompanyName);
		model.put("requestStatusChangedBy", statusChangedBy);
		model.put(PARTNER_MODULE_CUSTOM_NAME, partnerModuleCustomName);
		context.setVariables(model);
		String htmlName = "mdf-request-status-vendor-notification";
		String subject = utilService
				.addPerfixToSubject("MDF Request Status Changed For One Of Your " + partnerModuleCustomName);
		sendEmailNotification(context, htmlName, loggedInUserEmailId, subject);

	}

	private void requestStatusChangedUtil(MdfRequest mdfRequest, String requestTitle,
			String loggedInUserWelcomeDisplayName, Map<String, Object> model) {
		model.put(WELCOME_DISPLAY_NAME, loggedInUserWelcomeDisplayName);
		model.put(REQUEST_TITLE, requestTitle);
		model.put("requestStatus",
				xamplifyUtil.getMdfRequestStatusInString(mdfRequest.getMdfWorkFlowStepType().name()));
		setLoginUrl(model);
	}

	public void uploadAsset(DamAwsDTO damAwsDTO, DamUploadPostDTO damUploadPostDTO) {
		if (damAwsDTO != null && damUploadPostDTO != null) {
			Integer damId = damAwsDTO.getDamId();
			damAwsDTO.setPartnerIds(damUploadPostDTO.getPartnerIds());
			damAwsDTO.setPartnerGroupIds(damUploadPostDTO.getPartnerGroupIds());
			FilePathAndThumbnailPath filePathAndThumbnailPath = amazonWebService.uploadAssetAndThumbnail(damAwsDTO);
			String awsFilePath = filePathAndThumbnailPath.getFilePath();
			String thumbnailPath = filePathAndThumbnailPath.getThumbnailPath();
			String updatedThumbnailPath = "";
			updatedThumbnailPath = getUpdatedThumbnailPath(damAwsDTO, damUploadPostDTO, thumbnailPath,
					updatedThumbnailPath);
			damDao.updateAssetPathAndThumbnailPath(damId, awsFilePath, updatedThumbnailPath);
			if (XamplifyUtils.isValidInteger(damUploadPostDTO.getId())) {
				damDao.updateAssetPathAndThumbnailPath(damUploadPostDTO.getId(), awsFilePath, updatedThumbnailPath);
			}
			if (!damUploadPostDTO.isReplaceAsset()) {
				publishAssetAndSendEmailNotification(damAwsDTO, damUploadPostDTO, damId);
			}
		}
	}

	private void publishAssetAndSendEmailNotification(DamAwsDTO damAwsDTO, DamUploadPostDTO damUploadPostDTO,
			Integer damId) {
		UserDTO loggedInUserDetails = userDAO.getEmailIdAndDisplayName(damAwsDTO.getUserId());
		Context context = new Context();
		Map<String, Object> model = new HashMap<>();
		String welcomeDisplayName = StringUtils.hasText(loggedInUserDetails.getFullName())
				? loggedInUserDetails.getFullName()
				: replaceThere;
		model.put(WELCOME_DISPLAY_NAME, welcomeDisplayName);
		model.put("assetName", damAwsDTO.getAssetName());
		model.put("uploadedOn", new Date());
		context.setVariables(model);
		String htmlName = "upload-asset-notification";
		String subject = utilService.addPerfixToSubject("Asset Uploaded Successfully");
		if (!damUploadPostDTO.isBeeTemplate()) {
			sendEmailNotification(context, htmlName, loggedInUserDetails.getEmailId(), subject);
		}
		logger.debug("Asset Uploaded Successfully for {}", damId);
		damUploadPostDTO.setDamId(damId);
		Set<Integer> partnerIds = damUploadPostDTO.getPartnerIds();
		Set<Integer> partnerGroupIds = damUploadPostDTO.getPartnerGroupIds();
		boolean isAssetCanBePublished = XamplifyUtils.isPartnerGroupOrPartnerCompanySelected(partnerGroupIds,
				partnerIds);
		if (isAssetCanBePublished) {
			damDao.publishAsset(damUploadPostDTO);
			checkPartnerIdsAndSendAssetSharedEmailNotification(damUploadPostDTO.getUpdatedPartnerIds(),
					damUploadPostDTO.getCompanyName(), damAwsDTO.getAssetName(), true,
					damUploadPostDTO.getLoggedInUserId(), damUploadPostDTO.getDamPartnerIds());

		}
	}

	private String getUpdatedThumbnailPath(DamAwsDTO damAwsDTO, DamUploadPostDTO damUploadPostDTO, String thumbnailPath,
			String updatedThumbnailPath) {
		if (thumbnailPath != null && StringUtils.hasText(thumbnailPath)) {
			updatedThumbnailPath = thumbnailPath;
		} else {
			boolean isThumbnailPreservedForPdfReapproval = damUploadPostDTO.isSendForReApproval()
					&& damAwsDTO.getFileType().equalsIgnoreCase("pdf")
					&& XamplifyUtils.isValidString(damUploadPostDTO.getThumbnailPath());

			if (!damUploadPostDTO.isReplaceAsset() && !isThumbnailPreservedForPdfReapproval) {
				updatedThumbnailPath = xamplifyUtil.getThumbnailPathByFileType(damAwsDTO.getFileType());
			}
		}
		return updatedThumbnailPath;
	}

	public void checkPartnerIdsAndSendAssetSharedEmailNotification(List<Integer> partnerIds, String companyName,
			String assetName, boolean sendEmailNotification, Integer userId,
			Map<Integer, LinkedList<Integer>> damPartnerIds) {

		if (sendEmailNotification) {
			/**** XNFR-326 ****/
			boolean isAssetPublishedEmailNotification = companyProfileDao
					.isAssetPublishedEmailNotificationByUserId(userId);
			if (isAssetPublishedEmailNotification) {
				if (XamplifyUtils.isNotEmptyList(partnerIds)) {
					Set<Integer> uniquePartnerIds = XamplifyUtils.convertListToSetElements(partnerIds);
					List<UserDTO> partners = partnershipDao.findEmailIdAndFullNameByUserIds(uniquePartnerIds);
					iteratePartners(companyName, assetName, userId, partners, damPartnerIds);

				} else {
					logNoPartnersFoundToPublishAssets();
				}
			} else {
				logEmailNotificationTurnedOff(companyName);
			}

		}
	}

	private void iteratePartners(String companyName, String assetName, Integer userId, List<UserDTO> partners,
			Map<Integer, LinkedList<Integer>> damPartnerIds) {
		boolean isPartnerSignatureEnabled = utilDao.isPartnerSignatureEnabled(assetName);
		for (UserDTO partner : partners) {
			if (StringUtils.hasText(companyName)) {
				User user = userService.loadUser(Arrays.asList(new Criteria(USER_ID, OPERATION_NAME.eq, userId)),
						new FindLevel[] { FindLevel.COMPANY_PROFILE });
				CompanyProfile companyProfile = genericDAO.get(CompanyProfile.class,
						userDAO.getCompanyIdByUserId(userId));
				Integer partnerCompanyId = userDAO.getCompanyIdByUserId(partner.getUserId());
				if (damPartnerIds != null) {
					LinkedList<Integer> damIds = damPartnerIds.get(partnerCompanyId);
					partner.setDamPartnerIds(damIds);
					DefaultEmailTemplateType templateType = isPartnerSignatureEnabled
							? DefaultEmailTemplateType.PARTNER_SIGNATURE_ENABLED
							: DefaultEmailTemplateType.ASSET_PUBLISH;
					getEmailTemplateAndSendEmail(companyName, assetName, partner, user, companyProfile, templateType);

				}
			}
		}
	}

	private void logNoPartnersFoundToPublishAssets() {
		String debugMessage = " No Partners Found To Publish Assets " + new Date();
		logger.debug(debugMessage);
	}

	private void logEmailNotificationTurnedOff(String companyName) {
		String debugMessage = "Published Asset Email Notification Option Turned Off For Partners Of " + companyName
				+ ":::" + new Date();
		logger.debug(debugMessage);
	}

	private void getEmailTemplateAndSendEmail(String companyName, String assetName, UserDTO partner, User user,
			CompanyProfile companyProfile, DefaultEmailTemplateType templateType) {
		String body;
		String subject;
		DefaultEmailTemplate vanityDefaultEmailTemplate = vanityURLDao
				.getVanityDefaultEmailTemplateByType(templateType);
		CustomDefaultEmailTemplate vanityEmailTemplate = null;
		boolean hasVanityAccess = utilDao.hasVanityAccessByUserId(user.getUserId());

		if (vanityDefaultEmailTemplate != null) {
			vanityEmailTemplate = vanityURLDao
					.getVanityETByDefVanityETIdAndCompanyId(vanityDefaultEmailTemplate.getId(), companyProfile);
			if (hasVanityAccess && vanityEmailTemplate != null) {
				subject = vanityEmailTemplate.getSubject();
				body = genericDAO.get(CustomDefaultEmailTemplate.class, vanityEmailTemplate.getId()).getHtmlBody();
			} else {
				subject = vanityDefaultEmailTemplate.getSubject();
				body = genericDAO.get(DefaultEmailTemplate.class, vanityDefaultEmailTemplate.getId()).getHtmlBody();
			}
			body = XamplifyUtils.replaceReceiverMergeTagsInfo(user, body);
			if (!StringUtils.hasText(companyProfile.getCompanyProfileName()) || !hasVanityAccess) {
				body = replaceCompanyLogo(body);
			}
			body = replaceAssetLink(assetName, partner, user, companyProfile, body);
			body = replaceMergeTags(companyName, assetName, partner, body, companyProfile);

			mailService.sendMail(new EmailBuilder().from(user.getEmailId())
					.senderName(mailService.getFullName(user.getFirstName(), user.getLastName()))
					.to(partner.getEmailId()).subject(subject).body(body).build());
		}
	}

	private String replaceAssetLink(String assetName, UserDTO partner, User user, CompanyProfile companyProfile,
			String body) {
		String assetUrl = null;
		String finalLinks = assetName;
		if (XamplifyUtils.isNotEmptyList(partner.getDamPartnerIds())) {
			String vanityURLDomain;
			if (utilDao.hasVanityAccessByUserId(user.getUserId())) {
				vanityURLDomain = xamplifyUtil.frameVanityURL(webUrl, companyProfile.getCompanyProfileName());
			} else {
				vanityURLDomain = webUrl;
			}
			List<String> assetNames = Arrays.asList(assetName.split(","));
			Set<String> uniqueAssetNames = new LinkedHashSet<>();
			for (String name : assetNames) {
				uniqueAssetNames.add(name.trim());
			}
			List<Integer> damPartnerIds = partner.getDamPartnerIds();
			StringBuilder assetLinks = new StringBuilder();
			if (XamplifyUtils.isNotEmptyList(damPartnerIds)) {
				int size = assetNames.size();
				int index = 0;
				for (String dam : uniqueAssetNames) {
					if (index < damPartnerIds.size()) {
						Integer damPartnerId = damPartnerIds.get(index);
						assetUrl = vanityURLDomain + "home/dam/sharedp/view/" + damPartnerId;
						assetLinks.append("<a href=\"").append(assetUrl).append("\" target=\"_blank\">").append(dam)
								.append("</a>");
						if (index < size - 1) {
							assetLinks.append(", ");
						}
					}
					index++;
				}
			}
			finalLinks = assetLinks.toString();
		}
		body = body.replaceAll(XamplifyUtils.replaceMergeTagPrefixer("{{assetName}}"), finalLinks);
		return body;
	}

	private String replaceMergeTags(String companyName, String assetName, UserDTO partner, String body,
			CompanyProfile companyProfile) {
		String updatedCompanyLogoPath = serverPath
				+ XamplifyUtils.escapeDollarSequece(companyProfile.getCompanyLogoPath());
		body = body.replaceAll("<Vanity_Company_Logo>", updatedCompanyLogoPath);
		body = body.replaceAll(replaceCompanyLogo, updatedCompanyLogoPath);
		body = body.replaceAll(XamplifyUtils.replaceMergeTagPrefixer("{{customerFullName}}"),
				StringUtils.hasText(partner.getFullName()) && !partner.getFullName().trim().isEmpty()
						? partner.getFullName()
						: "There");
		Date date = Calendar.getInstance().getTime();
		DateFormat dateFormat = new SimpleDateFormat("E MMM dd hh:mm:ss z yyyy");
		String strDate = dateFormat.format(date);
		body = body.replaceAll(XamplifyUtils.replaceMergeTagPrefixer("{{sharedDate}}"), strDate);
		body = body.replaceAll(XamplifyUtils.replaceMergeTagPrefixer("{{senderCompanyName}}"), companyName);
		body = body.replaceAll("<Vanity_Company_Logo_Href>", companyProfile.getWebsite());
		return body;
	}

	private String replaceCompanyLogo(String body) {
		body = body.replaceAll("<Vanity_Company_Logo>", xAmplifyLogo);
		body = body.replaceAll(replaceCompanyLogo, xAmplifyLogo);
		body = body.replaceAll("<Vanity_Company_Logo_Href>", webUrl + "/login");
		return body;
	}

	public void generatePartnerTemplateThumbnails() {
	}

	public void sendLMSPublishedNotification(Integer learningTrackId, Integer loggedInUserId) {
		try {
			Thread.sleep(2000);
			LearningTrack learningTrack = genericDAO.get(LearningTrack.class, learningTrackId);
			if (learningTrack != null) {
				User publishedUser = genericDAO.get(User.class, loggedInUserId);
				Date publishedDate = new Date();
				sendSuccessMailToVendor(learningTrack, publishedUser, publishedDate);
				sendNotificationToPartners(learningTrack, publishedUser, publishedDate);
			}
		} catch (Exception e) {
			throw new XamplifyDataAccessException(e);
		}

	}

	private void sendSuccessMailToVendor(LearningTrack learningTrack, User publishedUser, Date publishedDate) {
		/*** XNFR-688 *****/
		boolean trackOrPlayBookPublishVendorEmailNotification = checkVendorEmailNotification(learningTrack);
		if (trackOrPlayBookPublishVendorEmailNotification) {
			List<User> superiors = partnershipDao.getSuperiors(learningTrack.getCompany().getId());
			if (superiors != null && !superiors.isEmpty()) {
				for (User superior : superiors) {
					if (superior != null) {
						Context context = new Context();
						Map<String, Object> model = new HashMap<>();
						model.put("publishedBy",
								mailService.getFullName(publishedUser.getFirstName(), publishedUser.getLastName()));
						model.put("publishedByEmail", publishedUser.getEmailId());
						model.put("publishedOn", publishedDate);
						model.put("receiverName",
								mailService.getFullName(superior.getFirstName(), superior.getLastName()));
						model.put("learningTrackTitle", learningTrack.getTitle());
						model.put("loginUrl", partnerLoginUrl);

						String subject = getSubject(learningTrack, model);
						context.setVariables(model);
						String html = templateEngine.process("publish-learning-track-vendor", context);
						subject = utilService.addPerfixToSubject(subject);
						mailService.sendMail(new EmailBuilder().from(fromEmail).senderName(fromName)
								.to(superior.getEmailId()).subject(subject).body(html).build());
					}
				}
			}
		} else {
			String debugMessage = learningTrack.getType()
					+ " Published Email Notifications Will Be Skipped For Vendor Company : " + new Date();
			logger.debug(debugMessage);
		}

	}

	/*** XNFR-688 *****/
	private boolean checkVendorEmailNotification(LearningTrack learningTrack) {
		boolean trackOrPlayBookPublishVendorEmailNotification = false;
		boolean isLearningTrack = LearningTrackType.TRACK.equals(learningTrack.getType());
		boolean isPlayBook = LearningTrackType.PLAYBOOK.equals(learningTrack.getType());
		if (isLearningTrack) {
			trackOrPlayBookPublishVendorEmailNotification = learningTrack.getCompany()
					.isTrackPublishVendorEmailNotification();
		} else if (isPlayBook) {
			trackOrPlayBookPublishVendorEmailNotification = learningTrack.getCompany()
					.isPlaybookPublishVendorEmailNotification();
		}
		return trackOrPlayBookPublishVendorEmailNotification;
	}

	private String getSubject(LearningTrack learningTrack, Map<String, Object> model) {
		String subject = "";
		if (LearningTrackType.TRACK == learningTrack.getType()) {
			model.put("learningTrackType", "learning track");
			subject = lmsPublishVendorSubject;
		}
		if (LearningTrackType.PLAYBOOK == learningTrack.getType()) {
			model.put("learningTrackType", "playbook");
			subject = playbookPublishVendorSubject;
		}
		return subject;
	}

	private void sendNotificationToPartners(LearningTrack learningTrack, User user, Date publishedDate) {
		if (learningTrack.isPublished()) {
			CompanyProfile companyProfile = learningTrack.getCompany();
			if (LearningTrackType.TRACK == learningTrack.getType() && companyProfile != null
					&& companyProfile.isTrackPublishedEmailNotification()) {
				/*** XNFR-326 *****/
				sendTrackNotificationToPartners(learningTrack, user, publishedDate);
				/*** XNFR-326 *****/
			}
			if (LearningTrackType.PLAYBOOK == learningTrack.getType() && companyProfile != null
					&& companyProfile.isPlaybookPublishedEmailNotification()) {
				/*** XNFR-326 *****/
				sendPlaybookNotificationToPartners(learningTrack, user, publishedDate);
				/*** XNFR-326 *****/

			}
		} else {
			String trackNotPublishedDebugMessage = learningTrack.getTitle() + " : " + learningTrack.getId()
					+ " Track Is Not Published" + "-" + new Date();
			logger.debug(trackNotPublishedDebugMessage);
		}

	}

	public void sendTrackNotificationToPartners(LearningTrack learningTrack, User sender, Date publishedDate) {
		if (learningTrack.isLoadVisibilityUsersFromDb()) {
			List<LearningTrackVisibility> users = lmsDao.findVisibilityUsersById(learningTrack.getId());
			if (XamplifyUtils.isNotEmptyList(users)) {
				Set<LearningTrackVisibility> distinctUsers = new HashSet<>(users);
				iterateAndSendTrackEmails(learningTrack, sender, publishedDate, distinctUsers);
			}
		} else {
			Set<LearningTrackVisibility> users = learningTrack.getVisibilityUsers();
			iterateAndSendTrackEmails(learningTrack, sender, publishedDate, users);
		}
	}

	private void iterateAndSendTrackEmails(LearningTrack learningTrack, User sender, Date publishedDate,
			Set<LearningTrackVisibility> distinctUsers) {
		int totalVisibilityUsersSize = distinctUsers.size();
		int counter = 1;
		List<Integer> deactivatedPartners = utilDao
				.findDeactivedPartnersByCompanyId(sender.getCompanyProfile().getId());
		for (LearningTrackVisibility visibilityUser : distinctUsers) {
			String totalUsersDebugMessage = "Total Visibility Users Completed : " + counter + "/"
					+ totalVisibilityUsersSize;
			if (visibilityUser != null) {
				User user = visibilityUser.getUser();
				if (user != null) {
					Integer userId = user.getUserId();
					if (!deactivatedPartners.contains(userId)) {
						logger.debug(totalUsersDebugMessage);
						String trackTitle = learningTrack.getTitle();
						/**** XNFR-523 ****/
						sendTrackUpdatedEmailNotification(learningTrack, sender, publishedDate, user, trackTitle);
					}
				}
			}
			counter++;
		}
	}

	/**** XNFR-523 ****/
	private void sendTrackUpdatedEmailNotification(LearningTrack learningTrack, User sender, Date publishedDate,
			User user, String trackTitle) {
		Integer userId = user.getUserId();
		String trackSlugs = learningTrack.getSlug();
		List<Integer> previouslySelectedVisibilityUserIds = learningTrack.getExistingVisibilityUserIds();
		boolean isExistingVisibilityUserExcluded = XamplifyUtils.isNotEmptyList(previouslySelectedVisibilityUserIds)
				&& previouslySelectedVisibilityUserIds.indexOf(userId) > -1;
		List<Integer> progressedVisibilityUserIds = learningTrack.getProgressedVisibilityUserIds();
		boolean isProgressedVisibilityUser = XamplifyUtils.isNotEmptyList(progressedVisibilityUserIds)
				&& progressedVisibilityUserIds.indexOf(userId) > -1;
		if (!isExistingVisibilityUserExcluded && !isProgressedVisibilityUser) {
			sendTrackEmailNotificationToPartners(sender, user, publishedDate, trackTitle, trackSlugs);
		} else if (isProgressedVisibilityUser && learningTrack.isPublishedTrackUpated()) {
			String progressedTrackEmailDebugMessage = "Updated Track Email Notification Will Be Sent To "
					+ user.getEmailId() + " For Track :" + learningTrack.getTitle() + "(" + learningTrack.getId() + ")";
			logger.debug(progressedTrackEmailDebugMessage);
			thymeleafService.sendUpdatedTrackOrPlayBookEmailNotificationToProgressedPartners(sender, user,
					learningTrack);
		}
	}

	/**** XNFR-523 ****/
	private void sendPlayBookUpdatedEmailNotification(LearningTrack learningTrack, User sender, Date publishedDate,
			User user, String playBookTitle, Integer userId) {
		List<Integer> previouslySelectedVisibilityUserIds = learningTrack.getExistingVisibilityUserIds();
		String playbookSlugs = learningTrack.getSlug();
		boolean isExistingVisibilityUserExcluded = XamplifyUtils.isNotEmptyList(previouslySelectedVisibilityUserIds)
				&& previouslySelectedVisibilityUserIds.indexOf(userId) > -1;
		List<Integer> progressedVisibilityUserIds = learningTrack.getProgressedVisibilityUserIds();
		boolean isProgressedVisibilityUser = XamplifyUtils.isNotEmptyList(progressedVisibilityUserIds)
				&& progressedVisibilityUserIds.indexOf(userId) > -1;
		if (!isExistingVisibilityUserExcluded && !isProgressedVisibilityUser) {
			sendPlaybookEmailNotificationToPartners(sender, user, publishedDate, playBookTitle, playbookSlugs);
		} else if (isProgressedVisibilityUser && learningTrack.isPublishedTrackUpated()) {
			String progressedTrackEmailDebugMessage = "Updated Play Book Email Notification Will Be Sent To "
					+ user.getEmailId() + " For Play Book :" + learningTrack.getTitle() + "(" + learningTrack.getId()
					+ ")";
			logger.debug(progressedTrackEmailDebugMessage);
			thymeleafService.sendUpdatedTrackOrPlayBookEmailNotificationToProgressedPartners(sender, user,
					learningTrack);
		}
	}

	private void sendTrackEmailNotificationToPartners(User sender, User user, Date publishedDate, String trackTitle,
			String tracksSlugs) {
		CompanyProfile companyProfile = sender.getCompanyProfile();
		if (companyProfile != null && StringUtils.hasText(companyProfile.getCompanyProfileName())) {
			String body = null;
			String subject = null;
			String trackUrl = null;
			String finalLinks = tracksSlugs;
			DefaultEmailTemplate vanityDefaultEmailTemplate = vanityURLDao
					.getVanityDefaultEmailTemplateByType(DefaultEmailTemplateType.TRACK_PUBLISH);
			if (vanityDefaultEmailTemplate != null) {
				CustomDefaultEmailTemplate vanityEmailTemplate = vanityURLDao
						.getVanityETByDefVanityETIdAndCompanyId(vanityDefaultEmailTemplate.getId(), companyProfile);
				if (vanityEmailTemplate != null) {
					subject = vanityEmailTemplate.getSubject();
					body = genericDAO.get(CustomDefaultEmailTemplate.class, vanityEmailTemplate.getId()).getHtmlBody();
				} else {
					subject = vanityDefaultEmailTemplate.getSubject();
					body = genericDAO.get(DefaultEmailTemplate.class, vanityDefaultEmailTemplate.getId()).getHtmlBody();
				}
				body = XamplifyUtils.replaceReceiverMergeTagsInfo(sender, body);
				if (StringUtils.hasText(companyProfile.getCompanyProfileName())) {
					body = body.replaceAll("<Vanity_Company_Logo>",
							serverPath + XamplifyUtils.escapeDollarSequece(companyProfile.getCompanyLogoPath()));
					body = body.replaceAll(replaceCompanyLogo,
							serverPath + XamplifyUtils.escapeDollarSequece(companyProfile.getCompanyLogoPath()));
					body = body.replaceAll(XamplifyUtils.replaceMergeTagPrefixer("{{customerFullName}}"),
							mailService.getFullName(user.getFirstName(), user.getLastName()));
					tracksSlugs = XamplifyUtils.escapeDollarSequece(tracksSlugs);
					String vanityURLDomain;
					if (utilDao.hasVanityAccessByUserId(sender.getUserId())) {
						vanityURLDomain = xamplifyUtil.frameVanityURL(webUrl, companyProfile.getCompanyProfileName());
					} else {
						vanityURLDomain = webUrl;
					}
					List<String> trackNames = Arrays.asList(tracksSlugs.split(","));
					List<String> trackTitles = Arrays.asList(trackTitle.split(","));
					Integer companyId = companyProfile.getId();
					StringBuilder trackLink = new StringBuilder();
					if (XamplifyUtils.isValidInteger(companyId)) {
						int size = trackNames.size();
						int titleSize = trackTitles.size();
						int minSize = Math.min(size, titleSize);
						for (int index = 0; index < minSize; index++) {
							String trackSlug = trackNames.get(index).trim();
							String trackTitleNames = trackTitles.get(index).trim();
							if (!trackSlug.isEmpty() && !trackTitleNames.isEmpty()) {
								trackUrl = vanityURLDomain + "home/tracks/tb/" + companyId + "/" + trackSlug;
								trackLink.append("<a href=\"").append(trackUrl).append("\" target=\"_blank\">")
										.append(trackTitleNames).append("</a>");
								if (index < minSize - 1) {
									trackLink.append(", ");
								}
							}

						}
					}
					finalLinks = trackLink.toString();
					body = body.replaceAll(XamplifyUtils.replaceMergeTagPrefixer("{{trackTitle}}"), finalLinks);

					DateFormat dateFormat = new SimpleDateFormat("E MMM dd hh:mm:ss z yyyy");
					String strDate = dateFormat.format(publishedDate);
					body = body.replaceAll(XamplifyUtils.replaceMergeTagPrefixer("{{publishedDate}}"), strDate);
					body = body.replaceAll(XamplifyUtils.replaceMergeTagPrefixer("{{senderCompanyName}}"),
							sender.getCompanyProfile().getCompanyName());
					body = body.replaceAll("<Vanity_Company_Logo_Href>", sender.getCompanyProfile().getWebsite());

				} else {
					body = replaceCompanyLogo(body);
				}
				mailService.sendMail(new EmailBuilder().from(sender.getEmailId())
						.senderName(mailService.getFullName(sender.getFirstName(), sender.getLastName()))
						.to(user.getEmailId()).subject(subject).body(body).build());
			}

		}
	}

	public void sendPlaybookNotificationToPartners(LearningTrack learningTrack, User sender, Date publishedDate) {
		if (learningTrack.isLoadVisibilityUsersFromDb()) {
			List<LearningTrackVisibility> users = lmsDao.findVisibilityUsersById(learningTrack.getId());
			if (XamplifyUtils.isNotEmptyList(users)) {
				Set<LearningTrackVisibility> distinctUsers = new HashSet<>(users);
				iterateAndSendPlayBookEmails(learningTrack, sender, publishedDate, distinctUsers);
			}
		} else {
			Set<LearningTrackVisibility> users = learningTrack.getVisibilityUsers();
			iterateAndSendPlayBookEmails(learningTrack, sender, publishedDate, users);
		}
	}

	private void iterateAndSendPlayBookEmails(LearningTrack learningTrack, User sender, Date publishedDate,
			Set<LearningTrackVisibility> distinctUsers) {
		int totalVisibilityUsersSize = distinctUsers.size();
		int counter = 1;
		List<Integer> deactivatedPartners = utilDao
				.findDeactivedPartnersByCompanyId(sender.getCompanyProfile().getId());
		for (LearningTrackVisibility visibilityUser : distinctUsers) {
			String totalUsersDebugMessage = "Total Visibility Users Completed : " + counter + "/"
					+ totalVisibilityUsersSize;
			if (visibilityUser != null) {
				User user = visibilityUser.getUser();
				if (user != null) {
					Integer userId = user.getUserId();
					if (!deactivatedPartners.contains(userId)) {
						logger.debug(totalUsersDebugMessage);
						String playBookTitle = learningTrack.getTitle();
						/**** XNFR-523 ****/
						sendPlayBookUpdatedEmailNotification(learningTrack, sender, publishedDate, user, playBookTitle,
								userId);
					}
				}
			}
			counter++;
		}
	}

	private void sendPlaybookEmailNotificationToPartners(User sender, User user, Date publishedDate,
			String playbookTitle, String playbookSlugs) {
		CompanyProfile companyProfile = sender.getCompanyProfile();
		String playbookUrl = null;
		String finalLinks = playbookSlugs;
		if (companyProfile != null && StringUtils.hasText(companyProfile.getCompanyProfileName())) {
			DefaultEmailTemplate vanityDefaultEmailTemplate = vanityURLDao
					.getVanityDefaultEmailTemplateByType(DefaultEmailTemplateType.PLAYBOOK_PUBLISH);
			if (vanityDefaultEmailTemplate != null) {
				String body = null;
				String subject = null;
				CustomDefaultEmailTemplate vanityEmailTemplate = vanityURLDao
						.getVanityETByDefVanityETIdAndCompanyId(vanityDefaultEmailTemplate.getId(), companyProfile);
				if (vanityEmailTemplate != null) {
					subject = vanityEmailTemplate.getSubject();
					body = genericDAO.get(CustomDefaultEmailTemplate.class, vanityEmailTemplate.getId()).getHtmlBody();
				} else {

					subject = vanityDefaultEmailTemplate.getSubject();
					body = genericDAO.get(DefaultEmailTemplate.class, vanityDefaultEmailTemplate.getId()).getHtmlBody();
				}
				body = XamplifyUtils.replaceReceiverMergeTagsInfo(sender, body);
				if (StringUtils.hasText(companyProfile.getCompanyProfileName())) {
					body = body.replaceAll("<Vanity_Company_Logo>",
							serverPath + XamplifyUtils.escapeDollarSequece(companyProfile.getCompanyLogoPath()));
					body = body.replaceAll(replaceCompanyLogo,
							serverPath + XamplifyUtils.escapeDollarSequece(companyProfile.getCompanyLogoPath()));
					body = body.replaceAll(XamplifyUtils.replaceMergeTagPrefixer("{{customerFullName}}"),
							mailService.getFullName(user.getFirstName(), user.getLastName()));

					playbookSlugs = XamplifyUtils.escapeDollarSequece(playbookSlugs);
					String vanityURLDomain;
					if (utilDao.hasVanityAccessByUserId(sender.getUserId())) {
						vanityURLDomain = xamplifyUtil.frameVanityURL(webUrl, companyProfile.getCompanyProfileName());
					} else {
						vanityURLDomain = webUrl;
					}
					List<String> playbookNames = Arrays.asList(playbookSlugs.split(","));
					List<String> playbookTitles = Arrays.asList(playbookTitle.split(","));
					Integer companyId = companyProfile.getId();
					StringBuilder playbookLink = new StringBuilder();
					if (XamplifyUtils.isValidInteger(companyId)) {
						int size = playbookNames.size();
						int titleSize = playbookTitles.size();
						int minSize = Math.min(size, titleSize);
						for (int index = 0; index < minSize; index++) {
							String playbookSlug = playbookNames.get(index).trim();
							String playBookTitles = playbookTitles.get(index).trim();
							if (!playbookSlug.isEmpty() && !playBookTitles.isEmpty()) {
								playbookUrl = vanityURLDomain + "home/playbook/pb/" + companyId + "/" + playbookSlug;
								playbookLink.append("<a href=\"").append(playbookUrl).append("\" target=\"_blank\">")
										.append(playBookTitles).append("</a>");
								if (index < minSize - 1) {
									playbookLink.append(", ");
								}
							}

						}
					}
					finalLinks = playbookLink.toString();
					body = body.replaceAll(XamplifyUtils.replaceMergeTagPrefixer("{{playbookTitle}}"), finalLinks);
					DateFormat dateFormat = new SimpleDateFormat("E MMM dd hh:mm:ss z yyyy");
					String strDate = dateFormat.format(publishedDate);
					body = body.replaceAll(XamplifyUtils.replaceMergeTagPrefixer("{{publishedDate}}"), strDate);
					body = body.replaceAll(XamplifyUtils.replaceMergeTagPrefixer("{{senderCompanyName}}"),
							sender.getCompanyProfile().getCompanyName());
					body = body.replaceAll("<Vanity_Company_Logo_Href>", sender.getCompanyProfile().getWebsite());

				} else {
					body = replaceCompanyLogo(body);
				}
				mailService.sendMail(new EmailBuilder().from(sender.getEmailId())
						.senderName(mailService.getFullName(sender.getFirstName(), sender.getLastName()))
						.to(user.getEmailId()).subject(subject).body(body).build());
			}

		}
	}

	public void sendShareLeadEmailNotificationToPartner(ShareLeadsDTO shareLeadsDTO, String sharedLeadListName) {
		Set<Integer> uniquePartnerIds = shareLeadsDTO.getPartnerIds();
		User user = userService.loadUser(
				Arrays.asList(new Criteria(USER_ID, OPERATION_NAME.eq, shareLeadsDTO.getUserId())),
				new FindLevel[] { FindLevel.COMPANY_PROFILE });
		List<UserDTO> partners = partnershipDao.findEmailIdAndFullNameByUserIds(uniquePartnerIds);
		for (UserDTO partner : partners) {
			String body = null;
			String subject = null;
			CompanyProfile companyProfile = user.getCompanyProfile();
			if (StringUtils.hasText(companyProfile.getCompanyName())) {
				DefaultEmailTemplate vanityDefaultEmailTemplate = vanityURLDao
						.getVanityDefaultEmailTemplateByType(DefaultEmailTemplateType.SHARE_LEAD);
				if (vanityDefaultEmailTemplate != null) {
					CustomDefaultEmailTemplate vanityEmailTemplate = vanityURLDao
							.getVanityETByDefVanityETIdAndCompanyId(vanityDefaultEmailTemplate.getId(), companyProfile);
					if (vanityEmailTemplate != null) {
						subject = vanityEmailTemplate.getSubject();
						body = genericDAO.get(CustomDefaultEmailTemplate.class, vanityEmailTemplate.getId())
								.getHtmlBody();
					} else {
						subject = vanityDefaultEmailTemplate.getSubject();
						body = genericDAO.get(DefaultEmailTemplate.class, vanityDefaultEmailTemplate.getId())
								.getHtmlBody();
					}
					body = XamplifyUtils.replaceReceiverMergeTagsInfo(user, body);
					if (StringUtils.hasText(companyProfile.getCompanyProfileName())) {
						body = body.replaceAll("<Vanity_Company_Logo>",
								serverPath + XamplifyUtils.escapeDollarSequece(companyProfile.getCompanyLogoPath()));
						body = body.replaceAll(replaceCompanyLogo,
								serverPath + XamplifyUtils.escapeDollarSequece(companyProfile.getCompanyLogoPath()));
						body = body.replaceAll(XamplifyUtils.replaceMergeTagPrefixer("{{customerFullName}}"),
								partner.getFullName());
						body = body.replaceAll(XamplifyUtils.replaceMergeTagPrefixer("{{shareLeadListName}}"),
								sharedLeadListName);
						Date date = Calendar.getInstance().getTime();
						DateFormat dateFormat = new SimpleDateFormat("E MMM dd hh:mm:ss z yyyy");
						String strDate = dateFormat.format(date);
						body = body.replaceAll(XamplifyUtils.replaceMergeTagPrefixer("{{sharedDate}}"), strDate);
						body = body.replaceAll(XamplifyUtils.replaceMergeTagPrefixer("{{senderCompanyName}}"),
								companyProfile.getCompanyName());
						body = body.replaceAll("<Vanity_Company_Logo_Href>", companyProfile.getWebsite());

					} else {
						body = replaceCompanyLogo(body);
					}
					mailService.sendMail(new EmailBuilder().from(user.getEmailId())
							.senderName(mailService.getFullName(user.getFirstName(), user.getLastName()))
							.to(partner.getEmailId()).subject(subject).body(body).build());
				}

			}
		}
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void publishDamToTeamMemberPartnerListPartners(Set<Integer> userListIds, User loggedInUser,
			Set<UserDTO> partners) {
		try {
			boolean damAccess = utilDao.hasDamAccessByUserId(loggedInUser.getUserId());
			if (damAccess) {
				for (Integer userListId : userListIds) {
					List<DamBasicInfo> publishedDams = damDao.findAssociatedDamBasicInfoByUserListId(userListId);
					if (publishedDams != null && !publishedDams.isEmpty() && !partners.isEmpty()) {
						iterateAssetsAndPublishToPartners(userListIds, loggedInUser, partners, damAccess, userListId,
								publishedDams);
					}
				}
			} else {
				logger.debug(
						"******DAM Access Removed For This Company.So No Assets Will Be Shared With Partners******");
			}

		} catch (Exception e) {
			throw new DamDataAccessException(e);
		}

	}

	private boolean iterateAssetsAndPublishToPartners(Set<Integer> userListIds, User loggedInUser,
			Set<UserDTO> partners, boolean damAccess, Integer userListId, List<DamBasicInfo> publishedDams) {
		boolean isAtleastSharedWithOnePartner = false;
		List<UserDTO> userDtos = new ArrayList<>();
		List<Integer> partnerIds = new ArrayList<>();
		List<String> assetNames = new ArrayList<>();
		Map<Integer, LinkedList<Integer>> damPartnerIds = new LinkedHashMap<>();
		String companyName = "";
		String assetName = "";
		Integer userId = loggedInUser.getUserId();
		int damsCount = 0;
		int damsLeft = 0;
		List<Integer> deactivatedPartners = utilDao
				.findDeactivedPartnersByCompanyId(loggedInUser.getCompanyProfile().getId());
		for (DamBasicInfo damInfo : publishedDams) {
			if (damsCount == 0 && damsLeft == 0) {
				damsLeft = publishedDams.size();
			}
			String damsLeftMessage = "******Dams Left : " + damsLeft;
			Integer damId = damInfo.getDamId();
			if (damInfo.getPublishedTime() == null) {
				damDao.updatePublishedTimeById(damId);
			}
			int partnersCounter = 0;
			int totalPartnersCount = partners.size();
			for (UserDTO partner : partners) {
				Integer partnerId = partner.getId();
				if (partnerId != null && !deactivatedPartners.contains(partnerId)) {
					Integer partnershipId = partnershipDao.findPartnershipIdByPartnerIdAndVendorCompanyId(partnerId,
							loggedInUser.getCompanyProfile().getId());
					boolean isDamPublishedToPartner = false;
					if (partnershipId != null) {
						isDamPublishedToPartner = damDao.isPublishedToPartnerByDamIdAndPartnershipId(damId,
								partnershipId);
					}
					if (isDamPublishedToPartner) {
						findDamPartnerIdAndInsertIntoDamPartnerGroupMapping(userListId, damId, partnerId, partnershipId,
								damPartnerIds);
					} else {
						partnerIds.add(partnerId);
						assetNames.add(damInfo.getAssetName());
						companyName = damInfo.getCompanyName();
						assetName = String.join(", ", assetNames);
						insertIntoDamPartnerAndDamPartnerGroupMapping(userListId, loggedInUser, partnerId,
								partnershipId, damId, userDtos, damPartnerIds);
					}
					/******* XNFR-255 ***********/
					boolean isAnyVendorAdminCompany = utilDao.isAnyVendorAdminCompany(partnerId);
					if (isAnyVendorAdminCompany) {
						shareWhiteLabelAssetWithPartner(userListIds, loggedInUser, damAccess, damInfo, damId,
								partnerId);
					}
					/******* XNFR-255 ***********/
					partnersCounter++;
					int partnersLeft = totalPartnersCount - partnersCounter;
					String message = "";
					if (!isDamPublishedToPartner) {
						message = "Dam:-" + damInfo.getAssetName() + "-" + damInfo.getDamId()
								+ "****Shared With Partner Id:-" + partnerId + "****Partners Left:-" + partnersLeft
								+ " " + damsLeftMessage;
						logger.debug(message);

					}

				}

			}
			damsCount++;
			damsLeft = publishedDams.size() - damsCount;
		}
		if (partnerIds != null && !partnerIds.isEmpty()) {
			isAtleastSharedWithOnePartner = true;
			checkPartnerIdsAndSendAssetSharedEmailNotification(partnerIds, companyName, assetName, damAccess, userId,
					damPartnerIds);
		} else {
			logger.debug("******No Partners Found To Send Emails*******");
		}
		return isAtleastSharedWithOnePartner;

	}

	private void shareWhiteLabelAssetWithPartner(Set<Integer> userListIds, User loggedInUser, boolean damAccess,
			DamBasicInfo damInfo, Integer damId, Integer partnerId) {
		boolean shareWhiteLabeledContentAccess = utilDao
				.hasShareWhiteLabeledContentAccessByCompanyId(loggedInUser.getCompanyProfile().getId());
		if (shareWhiteLabeledContentAccess && damInfo.isWhiteLabeledAssetSharedWithPartners() && damAccess) {
			DamUploadPostDTO damUploadPostDTO = new DamUploadPostDTO();
			damUploadPostDTO.setDamId(damId);
			damUploadPostDTO.setPartnerGroupIds(userListIds);
			damUploadPostDTO.setPartnerGroupSelected(true);
			damUploadPostDTO.setShareAsWhiteLabeledAsset(true);
			damUploadPostDTO.setNewlyAddedPartnerFromPartnerList(true);
			damUploadPostDTO.setAssetName(damInfo.getAssetName());
			damUploadPostDTO.setLoggedInUserId(loggedInUser.getUserId());
			damUploadPostDTO.setVideoId(damInfo.getVideoId());
			Set<Integer> partnerIds = new HashSet<Integer>();
			partnerIds.add(partnerId);
			damUploadPostDTO.setPartnerIds(partnerIds);
			if (damInfo.getVideoId() != null && damInfo.getVideoId() > 0) {
				publishVideoAsset(damUploadPostDTO);
			} else {
				publishAsset(damUploadPostDTO);
			}

		}
	}

	private void findDamPartnerIdAndInsertIntoDamPartnerGroupMapping(Integer userListId, Integer damId,
			Integer partnerId, Integer partnershipId, Map<Integer, LinkedList<Integer>> damPartnerIds) {
		Integer damPartnerId = damDao.findDamPartnerIdByDamIdAndPartnershipId(damId, partnershipId);
		if (damPartnerId != null) {
			DamPartner damPartner = new DamPartner();
			damPartner.setId(damPartnerId);
			Integer partnerCompanyId = partnershipDao.getPartnerCompanyIdByPartnershipId(partnershipId);
			if (!damPartnerIds.containsKey(partnerCompanyId)) {
				LinkedList<Integer> damIds = new LinkedList<>();
				damIds.add(damPartner.getId());
				damPartnerIds.put(partnerCompanyId, damIds);
			} else {
				List<Integer> damIds = damPartnerIds.get(partnerCompanyId);
				damIds.add(damPartner.getId());
			}
			boolean isDamPartnerGroupExists = damDao.isDamPartnerGroupRowExists(userListId, damPartnerId, partnerId);
			if (!isDamPartnerGroupExists) {
				insertIntoDamPartnerGroupMapping(userListId, partnerId, damPartner, partnershipId);
			}
		}
	}

	private void insertIntoDamPartnerAndDamPartnerGroupMapping(Integer userListId, User loggedInUser, Integer partnerId,
			Integer partnershipId, Integer damId, List<UserDTO> userDtos,
			Map<Integer, LinkedList<Integer>> damPartnerIds) {
		DamPartner damPartner = damDao.findByPartnershipIdAndDamId(partnershipId, damId);
		if (damPartner != null) {
			boolean isDamPartnerGroupExists = damDao.isDamPartnerGroupRowExists(userListId, damPartner.getId(),
					partnerId);
			if (!isDamPartnerGroupExists) {
				insertIntoDamPartnerGroupMapping(userListId, partnerId, damPartner, partnershipId);
				addPartnersToSendEmailNotifications(partnerId, userDtos);
			}
		} else {
			damPartner = new DamPartner();
			DamUploadPostDTO damUploadPostDTO = new DamUploadPostDTO();
			Dam dam = damDao.getById(damId);
			String assestPath = null;
			damPartner.setDam(dam);
			Partnership partnership = new Partnership();
			partnership.setId(partnershipId);
			damPartner.setPartnership(partnership);
			damPartner.setDam(dam);
			GenerateRandomPassword password = new GenerateRandomPassword();
			damPartner.setAlias(password.getPassword());
			damPartner.setJsonBody(dam.getJsonBody());
			damPartner.setHtmlBody(dam.getHtmlBody());
			damPartner.setPartnerGroupSelected(true);
			damPartner.setPublishedTime(new Date());
			damPartner.setPublishedBy(loggedInUser.getUserId());
			if (dam.getAssetType().equals("pdf") && !dam.isBeeTemplate()) {
				assestPath = damDao.fetchAssestPath(damId);
				xamplifyUtil.shareAssestToPartner(damId, damPartner, damUploadPostDTO, assestPath);
			}
			genericDAO.save(damPartner);
			insertIntoDamPartnerGroupMapping(userListId, partnerId, damPartner, partnershipId);
			addPartnersToSendEmailNotifications(partnerId, userDtos);
		}
		Integer partnerCompanyId = partnershipDao.getPartnerCompanyIdByPartnershipId(partnershipId);
		if (XamplifyUtils.isValidInteger(damPartner.getId())) {
			if (!damPartnerIds.containsKey(partnerCompanyId)) {
				LinkedList<Integer> damIds = new LinkedList<>();
				damIds.add(damPartner.getId());
				damPartnerIds.put(partnerCompanyId, damIds);
			} else {
				List<Integer> damIds = damPartnerIds.get(partnerCompanyId);
				damIds.add(damPartner.getId());
			}
		}
	}

	private void addPartnersToSendEmailNotifications(Integer partnerId, List<UserDTO> userDtos) {
		UserDTO userDto = partnershipDao.findEmailIdAndFullNameByUserId(partnerId);
		if (userDtos.isEmpty()) {
			userDtos.add(userDto);
		} else if (!userDtos.isEmpty()) {
			List<String> emailIds = userDtos.stream().map(UserDTO::getEmailId).collect(Collectors.toList());
			if (emailIds != null && !emailIds.isEmpty()) {
				if (emailIds.indexOf(userDto.getEmailId()) < 0) {
					userDtos.add(userDto);
				}
			} else {
				userDtos.add(userDto);
			}
		}
	}

	private void insertIntoDamPartnerGroupMapping(Integer userListId, Integer partnerId, DamPartner damPartner,
			Integer partnershipId) {
		DamPartnerGroupMapping damPartnerGroupMapping = new DamPartnerGroupMapping();
		damPartnerGroupMapping.setDamPartner(damPartner);
		damPartnerGroupMapping.setUserId(partnerId);
		damPartnerGroupMapping.setUserListId(userListId);
		damPartnerGroupMapping.setCreatedTime(new Date());
		genericDAO.save(damPartnerGroupMapping);

		utilService.insertIntoDamPartnerGroupUserMapping(partnershipId, partnerId, damPartnerGroupMapping.getId());
	}

	public void publishLmsToNewUsersInUserList(Set<Integer> userListIds, User loggedInUser, Set<UserDTO> partners) {
		try {
			for (Integer userListId : userListIds) {
				boolean isValidUserListId = XamplifyUtils.isValidInteger(userListId);
				boolean isValidPartners = partners != null && !partners.isEmpty();
				if (isValidUserListId && loggedInUser != null && isValidPartners) {
					List<LearningTrack> learningTrackList = lmsDao.getLearningTracksByGroupId(userListId);
					iteratePartnersAndPublishLms(loggedInUser, partners, userListId, learningTrackList, false, null,
							false);
				}
			}

		} catch (Exception e) {
			throw new XamplifyDataAccessException(e);
		}
	}

	/** XNFR-342 **/
	private boolean iteratePartnersAndPublishLms(User loggedInUser, Set<UserDTO> partners, Integer userListId,
			List<LearningTrack> learningTrackList, boolean isPublishingToPartnerList, Integer selectedPartnershipId,
			boolean isPublishingToPartnerInsideList) {
		boolean isAtleastSharedWithOnePartner = false;
		CompanyProfile loggedInCompany = loggedInUser.getCompanyProfile();
		UserList userList = genericDAO.get(UserList.class, userListId);
		Map<Integer, Map<String, Object>> notificationsMap = new HashMap<>();
		if (loggedInCompany != null && learningTrackList != null && !learningTrackList.isEmpty() && userList != null) {
			List<String> learningTrackTitles = learningTrackList.stream().map(LearningTrack::getTitle)
					.collect(Collectors.toList());
			int totalPartners = partners.size();
			int counter = 1;
			List<Integer> deactivatedPartners = utilDao.findDeactivedPartnersByCompanyId(loggedInCompany.getId());
			for (UserDTO partner : partners) {
				String partnerEmailId = partner.getEmailId();
				String publishingDebugMessage = counter + "/" + totalPartners + ". Publishing Tracks "
						+ learningTrackTitles + " To " + partnerEmailId + "(" + partner.getId() + ")";
				User user = userDAO.getUserByEmail(partnerEmailId);
				if (user != null && !deactivatedPartners.contains(user.getUserId())) {
					logger.debug(publishingDebugMessage);
					CompanyProfile company = user.getCompanyProfile();
					Partnership partnership = null;
					List<User> companyUsers = new ArrayList<>();
					if (company != null) {
						partnership = partnershipDao.checkPartnership(loggedInCompany.getId(), company.getId());
						companyUsers.addAll(userDAO.getAllUsersByCompanyId(company.getId()));
					} else {
						partnership = partnershipDao.getPartnershipByRepresentingPartner(user, loggedInCompany);
						if (partnership != null) {
							companyUsers.add(partnership.getRepresentingPartner());
						}
					}
					if (partnership != null && !companyUsers.isEmpty()) {
						isAtleastSharedWithOnePartner = true;
						saveVisibilityForAllUsers(companyUsers, learningTrackList, partnership, userList, loggedInUser,
								notificationsMap, isPublishingToPartnerList, selectedPartnershipId,
								isPublishingToPartnerInsideList);
					}
				}
				counter++;
			}
			String debugMessage = learningTrackTitles.size() + " Tracks Publishing Completed  at " + new Date();
			logger.debug(debugMessage);
			sendLmsNotificationsToPartners(notificationsMap, loggedInUser, loggedInCompany,
					isPublishingToPartnerInsideList, selectedPartnershipId, learningTrackList);
		}
		return isAtleastSharedWithOnePartner;
	}

	public void deleteLMSForDeletedUsersInUserList(Integer userListId, List<Integer> removePartnerIds) {
		if (userListId != null && userListId > 0 && removePartnerIds != null && !removePartnerIds.isEmpty()) {
			lmsDao.deleteVisibilityForDeletedUsersFromUserList(userListId, removePartnerIds);
		}
	}

	private void sendLmsNotificationsToPartners(Map<Integer, Map<String, Object>> notificationsMap, User sender,
			CompanyProfile loggedInCompany, boolean isPublishingToPartnerInsideList, Integer partnershipId,
			List<LearningTrack> learningTrackList) {
		Date publishedDate = new Date();
		/******** XNFR-326 *******/
		boolean isTrackPublishedEmailNotifications = loggedInCompany.isTrackPublishedEmailNotification();
		boolean isPlaybookPublishedEmailNotifications = loggedInCompany.isPlaybookPublishedEmailNotification();
		/******** XNFR-326 *******/
		/*** XBI-2048 ****/
		Integer patnerCompanyId = partnershipDao.getPartnerCompanyIdByPartnershipId(partnershipId);
		boolean isValidPartnerCompany = XamplifyUtils.isValidInteger(patnerCompanyId);
		Integer partnerId = 0;
		if (!isValidPartnerCompany) {
			partnerId = partnershipDao.getPartnerIdByPartnershipId(partnershipId);
		}

		for (Integer key : notificationsMap.keySet()) {
			Map<String, Object> detailsMap = notificationsMap.get(key);
			User user = (User) detailsMap.get("user");
			String trackTitles = (String) detailsMap.get("tracks");
			String trackSlugs = (String) detailsMap.get("track_slugs");
			String playbookTitles = (String) detailsMap.get("playbooks");
			String playbookSlugs = (String) detailsMap.get("playbook_slugs");
			if (!StringUtils.isEmpty(trackTitles)) {
				/******** XNFR-326 *******/
				if (isTrackPublishedEmailNotifications) {
					/**** XBI-2048 ****/
					if (isPublishingToPartnerInsideList) {
						if (isValidPartnerCompany) {
							boolean hasCompany = user.getCompanyProfile() != null;
							if (hasCompany) {
								Integer userCompanyId = user.getCompanyProfile().getId();
								boolean isCompanyIdMatched = patnerCompanyId.equals(userCompanyId);
								if (isCompanyIdMatched) {
									sendTrackEmailNotificationToPartners(sender, user, publishedDate, trackTitles,
											trackSlugs);
								}
							} else {
								sendTrackSharedEmailNotificationsByPartnerId(sender, publishedDate, partnerId, user,
										trackTitles, trackSlugs);
							}

						} else {
							sendTrackSharedEmailNotificationsByPartnerId(sender, publishedDate, partnerId, user,
									trackTitles, trackSlugs);
						}
						/**** XBI-2048 ****/

					} else {
						sendTrackEmailNotificationToPartners(sender, user, publishedDate, trackTitles, trackSlugs);
					}
				}
				/******** XNFR-326 *******/
			}

			if (!StringUtils.isEmpty(playbookTitles)) {
				/******** XNFR-326 *******/
				if (isPlaybookPublishedEmailNotifications) {
					if (isPublishingToPartnerInsideList) {
						if (isValidPartnerCompany) {
							boolean hasCompany = user.getCompanyProfile() != null;
							if (hasCompany) {
								Integer userCompanyId = user.getCompanyProfile().getId();
								boolean isCompanyIdMatched = patnerCompanyId.equals(userCompanyId);
								if (isCompanyIdMatched) {
									sendPlaybookEmailNotificationToPartners(sender, user, publishedDate, playbookTitles,
											playbookSlugs);
								}
							} else {
								sendPlayBookSharedEmailNotificationsByPartnerId(sender, publishedDate, partnerId, user,
										trackTitles, playbookSlugs);
							}

						} else {
							sendPlayBookSharedEmailNotificationsByPartnerId(sender, publishedDate, partnerId, user,
									trackTitles, playbookSlugs);
						}
						/**** XBI-2048 ****/

					} else {
						sendPlaybookEmailNotificationToPartners(sender, user, publishedDate, playbookTitles,
								playbookSlugs);
					}

				}
				/******** XNFR-326 *******/
			}
		}
	}

	/**** XBI-2048 ****/
	private void sendTrackSharedEmailNotificationsByPartnerId(User sender, Date publishedDate, Integer partnerId,
			User user, String trackTitles, String trackSlugs) {
		Integer userId = user.getUserId();
		if (partnerId.equals(userId)) {
			sendTrackEmailNotificationToPartners(sender, user, publishedDate, trackTitles, trackSlugs);
		}
	}

	/**** XBI-2048 ****/
	private void sendPlayBookSharedEmailNotificationsByPartnerId(User sender, Date publishedDate, Integer partnerId,
			User user, String playbookTitles, String playbookSlugs) {
		Integer userId = user.getUserId();
		if (partnerId.equals(userId)) {
			sendPlaybookEmailNotificationToPartners(sender, user, publishedDate, playbookTitles, playbookSlugs);
		}
	}

	private void saveVisibilityForAllUsers(List<User> companyUsers, List<LearningTrack> learningTrackList,
			Partnership partnership, UserList userList, User loggedInUser,
			Map<Integer, Map<String, Object>> notificationsMap, boolean isPublishingToPartnerList,
			Integer selectedPartnershipId, boolean isPublishingToPartnerInsideList) {
		for (User companyUser : companyUsers) {
			for (LearningTrack learningTrack : learningTrackList) {
				Integer partnershipId = partnership.getId();
				LearningTrackVisibility learningTrackVisibility = lmsDao.getVisibilityUser(companyUser.getUserId(),
						partnershipId, learningTrack.getId());
				if (learningTrackVisibility == null) {
					saveVisibilityUser(loggedInUser.getUserId(), partnership, learningTrack, companyUser, userList,
							false, selectedPartnershipId, isPublishingToPartnerList);
					if (learningTrack.isPublished() || isPublishingToPartnerInsideList || isPublishingToPartnerList) {
						updateNotoficationMap(notificationsMap, companyUser, learningTrack);
					}
				} else {
					/******* XNFR-342 *****/
					if (isPublishingToPartnerList) {
						learningTrackVisibility.setPublished(true);
						learningTrackVisibility.setPublishedOn(new Date());
						updateNotoficationMap(notificationsMap, companyUser, learningTrack);
						boolean isDataExists = lmsDao.isDataExistsInLearningVisibilityGroupByUserListAndVisibilityId(
								userList.getId(), learningTrackVisibility.getId());
						if (!isDataExists) {
							saveVisibilityGroup(userList, learningTrackVisibility, loggedInUser.getUserId());
						}
					} else if (isPublishingToPartnerInsideList) {
						if (selectedPartnershipId != null && selectedPartnershipId.equals(partnership.getId())) {
							learningTrackVisibility.setPublished(true);
							learningTrackVisibility.setPublishedOn(new Date());
							updateNotoficationMap(notificationsMap, companyUser, learningTrack);
						}
					} /******* XNFR-342 *****/
					else {
						saveVisibilityGroup(userList, learningTrackVisibility, loggedInUser.getUserId());
					}

				}
			}
		}
	}

	private void updateNotoficationMap(Map<Integer, Map<String, Object>> notificationsMap, User companyUser,
			LearningTrack learningTrack) {
		if (notificationsMap.containsKey(companyUser.getUserId())) {
			Map<String, Object> detailsMap = notificationsMap.get(companyUser.getUserId());
			if (learningTrack.getType() == LearningTrackType.TRACK) {
				String titleString = (String) detailsMap.get("tracks");
				titleString += (!StringUtils.isEmpty((titleString).trim())) ? ", " + learningTrack.getTitle()
						: learningTrack.getTitle();
				detailsMap.put("tracks", titleString);

				String trackTitleString = (String) detailsMap.get("track_slugs");
				trackTitleString += (!StringUtils.isEmpty((trackTitleString).trim())) ? ", " + learningTrack.getSlug()
						: learningTrack.getSlug();
				detailsMap.put("track_slugs", trackTitleString);
			} else {
				String titleString = (String) detailsMap.get("playbooks");
				titleString += (!StringUtils.isEmpty((titleString).trim())) ? ", " + learningTrack.getTitle()
						: learningTrack.getTitle();
				detailsMap.put("playbooks", titleString);

				String playbookslugString = (String) detailsMap.get("playbook_slugs");
				playbookslugString += (!playbookslugString.trim().isEmpty()) ? ", " + learningTrack.getSlug()
						: learningTrack.getSlug();
				detailsMap.put("playbook_slugs", playbookslugString);
			}
		} else {
			Map<String, Object> detailsMap = new HashMap<>();
			detailsMap.put("user", companyUser);
			detailsMap.put("tracks", "");
			detailsMap.put("track_slugs", "");
			detailsMap.put("playbooks", "");
			detailsMap.put("playbook_slugs", "");
			if (learningTrack.getType() == LearningTrackType.TRACK) {
				detailsMap.put("tracks", learningTrack.getTitle());
				detailsMap.put("track_slugs", learningTrack.getSlug());
			} else {
				detailsMap.put("playbooks", learningTrack.getTitle());
				detailsMap.put("playbook_slugs", learningTrack.getSlug());
			}
			notificationsMap.put(companyUser.getUserId(), detailsMap);
		}
	}

	private LearningTrackVisibility saveVisibilityUser(Integer loggedInUserId, Partnership partnerShip,
			LearningTrack learningTrack, User user, UserList userList, boolean isAssociatedThroughCompany,
			Integer selectedPartnershipId, boolean isPublishingToPartnerList) {
		LearningTrackVisibility visibilityUser = null;
		if (user != null && partnerShip != null && learningTrack != null) {
			visibilityUser = new LearningTrackVisibility();
			visibilityUser.setPartnership(partnerShip);
			visibilityUser.setLearningTrack(learningTrack);
			visibilityUser.setUser(user);
			if (user.getTeamMembers() != null && !user.getTeamMembers().isEmpty()) {
				visibilityUser.setTeamMember(user.getTeamMembers().get(0));
			}
			visibilityUser.setAssociatedThroughCompany(isAssociatedThroughCompany);
			visibilityUser.setCreatedBy(loggedInUserId);
			visibilityUser.initialiseCommonFields(true, loggedInUserId);
			/** XNFR-342 ******/
			boolean isPublishedToOnePartner = selectedPartnershipId != null
					&& selectedPartnershipId.equals(partnerShip.getId());
			boolean isPublished = isPublishedToOnePartner || isPublishingToPartnerList;
			if (isPublished || learningTrack.isPublished()) {
				visibilityUser.setPublished(true);
				visibilityUser.setPublishedOn(new Date());
			}
			/** XNFR-342 ******/
			genericDAO.save(visibilityUser);
			if (userList != null) {
				saveVisibilityGroup(userList, visibilityUser, loggedInUserId);
			}
		}
		return visibilityUser;
	}

	private void saveVisibilityGroup(UserList userList, LearningTrackVisibility learningVisibility, Integer userId) {
		LearningTrackVisibilityGroup visibilityGroup = new LearningTrackVisibilityGroup();
		visibilityGroup.setUserList(userList);
		visibilityGroup.setLearningTrackVisibility(learningVisibility);
		visibilityGroup.setCreatedBy(userId);
		visibilityGroup.initialiseCommonFields(true, userId);
		genericDAO.save(visibilityGroup);
	}

	public void publishLMSToNewTeamMembers(List<TeamMemberDTO> teamMemberDTOs, Integer loggedInUserId) {
		try {
			Thread.sleep(2000);
			if (teamMemberDTOs != null && !teamMemberDTOs.isEmpty()) {
				Map<Integer, Map<String, Object>> notificationsMap = new HashMap<>();
				User loggedInUser = genericDAO.get(User.class, loggedInUserId);
				List<Object[]> learningTrackAndPartnerListIds = lmsDao
						.getLearningTracksForTeamMember(loggedInUser.getCompanyProfile().getId());
				if (learningTrackAndPartnerListIds != null && !learningTrackAndPartnerListIds.isEmpty()) {
					iterateDtosAndPublish(teamMemberDTOs, notificationsMap, loggedInUser,
							learningTrackAndPartnerListIds);
				}

				List<DamPartnerDetailsDTO> damGroupDtos = damDao.findPublishedGroupAssetsByPartnerId(loggedInUserId);
				if (XamplifyUtils.isNotEmptyList(damGroupDtos)) {
					iterateGroupDtosAndPublishDam(teamMemberDTOs, damGroupDtos);
				}
			}
		} catch (Exception e) {
			throw new XamplifyDataAccessException(e);
		}
	}

	private void iterateGroupDtosAndPublishDam(List<TeamMemberDTO> teamMemberDTOs,
			List<DamPartnerDetailsDTO> damGroupDtos) {
		for (TeamMemberDTO teamMemberDTO : teamMemberDTOs) {
			String emailId = teamMemberDTO.getEmailId().trim().toLowerCase();
			Integer teamMemberId = userDAO.getUserIdByEmail(emailId);
			if (XamplifyUtils.isValidInteger(teamMemberId)) {
				TeamMember teamMember = teamDao.getTeamMemberByUserId(teamMemberId);
				for (DamPartnerDetailsDTO damGroupDto : damGroupDtos) {
					DamPartnerGroupUserMapping teamGroupMapping = new DamPartnerGroupUserMapping();
					DamPartnerGroupMapping damPartnerGroup = new DamPartnerGroupMapping();
					damPartnerGroup.setId(damGroupDto.getDamPartnerGroupId());
					teamGroupMapping.setDamPartnerGroupMapping(damPartnerGroup);
					Partnership partnership = new Partnership();
					partnership.setId(damGroupDto.getPartnershipId());
					teamGroupMapping.setPartnership(partnership);
					User teamUser = new User();
					teamUser.setUserId(teamMemberId);
					teamGroupMapping.setUser(teamUser);
					TeamMember team = new TeamMember();
					team.setId(teamMember.getId());
					teamGroupMapping.setTeamMember(team);
					teamGroupMapping.setCreatedTime(new Date());
					genericDAO.save(teamGroupMapping);

					String publishingDebugMessage = "Dam: " + damGroupDto.getAssetName() + " Published To " + emailId;
					logger.debug(publishingDebugMessage);
				}
			}
		}
	}

	private void iterateDtosAndPublish(List<TeamMemberDTO> teamMembers,
			Map<Integer, Map<String, Object>> notificationsMap, User loggedInUser,
			List<Object[]> learningTrackAndPartnerListIds) {
		for (TeamMemberDTO teamMemberDTO : teamMembers) {
			String emailId = teamMemberDTO.getEmailId().trim().toLowerCase();
			String publishingDebugMessage = "Publishing To " + emailId;
			logger.debug(publishingDebugMessage);
			User user = userDAO.getUserByEmail(emailId);
			if (user != null) {
				TeamMember teamMember = teamDao.getTeamMemberByUserId(user.getUserId());
				TeamMemberGroup teamMemberGroup = teamMember.getTeamMemberGroupUserMapping().getTeamMemberGroup();
				boolean hasTrackRole = teamMemberGroupDao.isTeamMemberGroupRoleMappingRowExists(teamMemberGroup.getId(),
						23);
				boolean hasPlayBookRole = teamMemberGroupDao
						.isTeamMemberGroupRoleMappingRowExists(teamMemberGroup.getId(), 24);
				if (hasTrackRole || hasPlayBookRole) {
					publishLMSToNewTeamMember(user, learningTrackAndPartnerListIds, loggedInUser.getCompanyProfile(),
							notificationsMap, hasTrackRole, hasPlayBookRole);
				}
			}
		}
	}

	private void publishLMSToNewTeamMember(User teamMember, List<Object[]> learningTrackAndPartnerListIds,
			CompanyProfile loggedInCompany, Map<Integer, Map<String, Object>> notificationsMap, boolean hasTrackRole,
			boolean hasPlayBookRole) {
		for (Object[] learningTrackAndPartnerListId : learningTrackAndPartnerListIds) {
			if (learningTrackAndPartnerListId != null && learningTrackAndPartnerListId.length > 0) {
				Integer learningTrackId = (Integer) learningTrackAndPartnerListId[0];
				LearningTrack learningTrack = genericDAO.get(LearningTrack.class, learningTrackId);
				if (learningTrack != null && ((LearningTrackType.TRACK == learningTrack.getType() && hasTrackRole)
						|| (LearningTrackType.PLAYBOOK == learningTrack.getType() && hasPlayBookRole))) {
					LearningTrackVisibility visibilityUser = lmsDao.getVisibilityUser(teamMember.getUserId(),
							learningTrackId);
					if (visibilityUser == null) {
						visibilityUser = findPartnershipAndInsertIntoVisibilityUserGroup(teamMember, loggedInCompany,
								notificationsMap, learningTrack, visibilityUser);
					}
					saveIntoVisibilityGroup(learningTrackAndPartnerListId, learningTrack, visibilityUser);
				}
			}
		}
	}

	private LearningTrackVisibility findPartnershipAndInsertIntoVisibilityUserGroup(User teamMember,
			CompanyProfile loggedInCompany, Map<Integer, Map<String, Object>> notificationsMap,
			LearningTrack learningTrack, LearningTrackVisibility visibilityUser) {
		Partnership partnership = partnershipDao.checkPartnership(learningTrack.getCompany().getId(),
				loggedInCompany.getId());
		if (partnership != null) {
			visibilityUser = saveVisibilityUser(learningTrack.getCreatedBy(), partnership, learningTrack, teamMember,
					null, false, null, false);
			if (learningTrack.isPublished()) {
				updateNotoficationMap(notificationsMap, teamMember, learningTrack);
			}
		}
		return visibilityUser;
	}

	private void saveIntoVisibilityGroup(Object[] learningTrackAndPartnerListId, LearningTrack learningTrack,
			LearningTrackVisibility visibilityUser) {
		if (visibilityUser != null) {
			UserList userList = genericDAO.get(UserList.class, (Integer) learningTrackAndPartnerListId[1]);
			saveVisibilityGroup(userList, visibilityUser, learningTrack.getCreatedBy());
		}
	}

	public void sendActiveAndInactivePartnerNotification(
			ActiveAndInActivePartnerEmailNotificationDTO activeAndInActivePartnerEmailNotificationDTO) {
		thymeleafService.sendActiveAndInActivePartnerEmailNotification(activeAndInActivePartnerEmailNotificationDTO);
	}

	public void sendActivePartnerNotification(
			ActiveAndInActivePartnerEmailNotificationDTO activeAndInActivePartnerEmailNotificationDTO) {
		thymeleafService.sendActivePartnerEmailNotification(activeAndInActivePartnerEmailNotificationDTO);
	}

	public void sendInactivePartnerNotification(
			ActiveAndInActivePartnerEmailNotificationDTO activeAndInActivePartnerEmailNotificationDTO) {
		thymeleafService.sendInActivePartnerEmailNotification(activeAndInActivePartnerEmailNotificationDTO);
	}

	public void uploadThumbnail(DamAwsDTO damAwsDTO, String awsFilePath) {
		Integer damId = damAwsDTO.getDamId();
		FilePathAndThumbnailPath filePathAndThumbnailPath = amazonWebService.uploadThumbnail(damAwsDTO);
		// String awsFilePath = filePathAndThumbnailPath.getFilePath();
		String thumbnailPath = filePathAndThumbnailPath.getThumbnailPath();
		String updatedThumbnailPath = thumbnailPath != null && StringUtils.hasText(thumbnailPath) ? thumbnailPath
				: xamplifyUtil.getThumbnailPathByFileType(damAwsDTO.getFileType());
		damDao.updateAssetPathAndThumbnailPath(damId, awsFilePath, updatedThumbnailPath);
	}

	public void generateHighLevelAnalyticsExcelAndSendEmailNotification(Integer id,
			VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		try {
			DownloadRequest downloadRequest = highlevelAnalyticsDao.findById(id);
			downloadRequest.setDownloadStatus(DownloadStatus.PROCESSING);
			downloadRequest.setUpdatedOn(new Date());
			XtremandResponse response = highlevalAnalyticsService.downloadAnalytics(vanityUrlDetailsDTO, id);
			Integer userId = vanityUrlDetailsDTO.getUserId();
			XSSFWorkbook workbook = (XSSFWorkbook) response.getData();
			if (workbook != null) {
				String filePath = fileUtil.createHighLevelAnalyticsDirectory(id, userId, workbook);
				AmazonWebModel amazonWebModel = new AmazonWebModel();
				amazonWebModel.setFilePath(filePath);
				String folderSuffixPath = highLevelAnalyticsFolder + "/" + System.currentTimeMillis()
						+ "/High-Level-Anlaytics.xlsx";
				amazonWebModel.setFolderSuffixPath(folderSuffixPath);
				String awsPath = amazonWebService.uploadFileToAWS(amazonWebModel);
				String cloudFrontUrl = xamplifyUtil.replaceS3WithCloudfrontViceVersa(awsPath);
				DownloadRequestUserDetailsDTO dto = new DownloadRequestUserDetailsDTO();
				User user = userDAO.getUser(vanityUrlDetailsDTO.getUserId());
				if (user != null) {
					dto.setEmailId(user.getEmailId());
					if ((user.getFirstName() != null && !user.getFirstName().isEmpty())
							&& (user.getLastName() != null && !user.getLastName().isEmpty())) {
						dto.setFullName(user.getFirstName() + "  " + user.getLastName());
					} else if (user.getFirstName() != null && !user.getFirstName().isEmpty()) {
						dto.setFullName(user.getFirstName());
					} else {
						dto.setFullName(user.getLastName());
					}
					thymeleafService.sendHighLevelAnalyticsLinkEmailNotification(dto, awsPath);
					downloadRequest.setDownloadStatus(DownloadStatus.COMPLETED);
					downloadRequest.setUpdatedOn(new Date());
					downloadRequest.setDownloadPath(awsPath);
					downloadRequest.setHighLevelCdnPath(cloudFrontUrl);
				}
			} else {
				logger.error(
						"Unable to generate high level analytics for user id : " + userId + " and request id : " + id);
			}

		} catch (Exception e) {
			highlevelAnalyticsDao.updateDownloadRequestStatus(id, DownloadStatus.FAILED);
		}
	}

	/**********
	 * XNFR-211
	 * 
	 * @param isCreate
	 ***********/
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void createUsersInUserList(Set<UserDTO> userDTOs, Integer userListId, Integer loggedInUserId,
			boolean isCreate, UserListDTO userListDTO) {
		try {
			Thread.sleep(5000);
			if (userListDTO.isCopyList()) {
				userDTOs = getCopyListUsers(userListDTO);
			}
			if (userDTOs != null && !userDTOs.isEmpty() && userListId != null && userListId > 0
					&& loggedInUserId != null && loggedInUserId > 0) {

				UserList userList = userListDAO.findByPrimaryKey(userListId,
						new FindLevel[] { FindLevel.USERS, FindLevel.USER_USER_LIST, FindLevel.COMPANY_PROFILE });
				User loggedInUser = userService.loadUser(
						Arrays.asList(new Criteria(USER_ID, OPERATION_NAME.eq, loggedInUserId)),
						new FindLevel[] { FindLevel.COMPANY_PROFILE });
				if (userList != null && loggedInUser != null) {
					logger.debug("INITIATED: Uploading users in to userlist...");
					userList.setUploadInProgress(true);
					boolean isGdprOn = false;
					if (userList.getModuleName().equalsIgnoreCase("SHARE LEADS")) {
						isGdprOn = gdprSettingService.isGdprEnabled(loggedInUser.getCompanyProfile().getId());
					} else {
						isGdprOn = gdprSettingService.isGdprEnabled(userList.getCompany().getId());
					}
					createUsersInUserList(userList, userDTOs, loggedInUser, isGdprOn, isCreate);
					logger.debug("COMPLETED: Uploaded users in to userlist...");
				}
			}
		} catch (InterruptedException e) {
			logger.debug(e.getMessage());
		}
	}

	private void createUsersInUserList(UserList userList, Set<UserDTO> userDTOs, User loggedInUser, boolean isGdprOn,
			boolean isCreate) {
		int counter = 0;
		Map<String, User> existingUsersMap = getExistingUsersMap(userDTOs);
		List<User> validationRequiredUsers = new ArrayList<>();
		iterateUsersAndInsertIntoContactList(userList, userDTOs, loggedInUser, isGdprOn, counter, existingUsersMap,
				validationRequiredUsers);
		changeProgessStatusAndEmailValidationIndStatus(userList, validationRequiredUsers);
		userListDAO.updateUserListProcessingStatus(userList);
		if (XamplifyUtils.isNotEmptyList(validationRequiredUsers)) {
		} else {
			sendEmailNotifications(userList, loggedInUser, isCreate);
		}
	}

	private void changeProgessStatusAndEmailValidationIndStatus(UserList userList, List<User> validationRequiredUsers) {
		userList.setUploadInProgress(false);
		if (XamplifyUtils.isNotEmptyList(validationRequiredUsers)) {
			userList.setValidationInProgress(true);
			userList.setEmailValidationInd(false);
		} else {
			userList.setValidationInProgress(false);
			userList.setEmailValidationInd(true);
		}
	}

	private void iterateUsersAndInsertIntoContactList(UserList userList, Set<UserDTO> userDTOs, User loggedInUser,
			boolean isGdprOn, int counter, Map<String, User> existingUsersMap, List<User> validationRequiredUsers) {
		Set<String> insertedUsers = new HashSet<>();
		int c = 1;
		int totalUsers = XamplifyUtils.isNotEmptySet(userDTOs) ? userDTOs.size() : 0;
		for (UserDTO userDTO : userDTOs) {
			if (userDTO != null) {
				String emailId = userDTO.getEmailId().toLowerCase().trim();
				String uploadingUsersDebugMessage = c + "/" + totalUsers + ". Uploading " + emailId + " into "
						+ userList.getName() + "(" + userList.getId() + ")";
				logger.debug(uploadingUsersDebugMessage);
				c++;
				if (!StringUtils.isEmpty(emailId) && !insertedUsers.contains(emailId)) {

					User user = saveOrUpdateUser(userDTO, loggedInUser, existingUsersMap);
					if (user != null) {
						insertedUsers.add(emailId);
						if (!user.isEmailValidationInd()) {
							validationRequiredUsers.add(user);
						}
						boolean hasUserUserList = false;
						if (userList.getId() != null && user.getUserId() != null) {
							hasUserUserList = userListDAO.hasUserUserList(user.getUserId(), userList.getId());
						}
						if (!hasUserUserList) {
							userListService.createUserUserList(user, userList, userDTO, loggedInUser, isGdprOn);
						}

						if (counter % 30 == 0) {
							utilDao.flushCurrentSession();
						}
						counter++;
					}
				}
			}
		}
	}

	private void sendEmailNotifications(UserList userList, User loggedInUser, boolean isCreate) {
		if (userList.getModuleName() != null) {
			if (userList.getModuleName().equalsIgnoreCase("SHARE LEADS")) {
				List<Integer> totalUnsubscribedUserIds = userListDAO
						.getUnsubscribedUsers(loggedInUser.getCompanyProfile().getId());
				utilService.sendLeadsListMail(loggedInUser, userList, isCreate, totalUnsubscribedUserIds);
			}
		}
	}

	private User saveOrUpdateUser(UserDTO userDTO, User loggedInUser, Map<String, User> existingUsersMap) {
		User user = null;
		String emailId = userDTO.getEmailId().toLowerCase().trim();
		if (existingUsersMap.containsKey(emailId)) {
			user = existingUsersMap.get(emailId);
			if (user != null && user.getAlias() == null) {
				GenerateRandomPassword randomPassword = new GenerateRandomPassword();
				user.setAlias(randomPassword.getPassword());
				user.initialiseCommonFields(true, loggedInUser.getUserId());
				genericDAO.update(user);
			}
		} else {
			user = new User();
			user.setEmailId(emailId);
			user.setUserName(emailId);
			user.setUserDefaultPage(UserDefaultPage.WELCOME);
			user.setUserStatus(UserStatus.UNAPPROVED);
			user.setModulesDisplayType(ModulesDisplayType.LIST);
			user.setRoles(new HashSet<>(Arrays.asList(Role.USER_ROLE)));
			GenerateRandomPassword randomPassword = new GenerateRandomPassword();
			user.setAlias(randomPassword.getPassword());
			user.initialiseCommonFields(true, loggedInUser.getUserId());
			genericDAO.save(user);
		}
		return user;
	}

	private Map<String, User> getExistingUsersMap(Set<UserDTO> userDTOs) {
		Map<String, User> existingUsersMap = new HashMap<String, User>();
		List<String> emailIds = userDTOs.parallelStream().map(u -> u.getEmailId().toLowerCase().trim())
				.collect(Collectors.toList());
		if (emailIds != null && !emailIds.isEmpty()) {
			List<User> existingUsers = userDAO.getUsersByEmailIds(emailIds);
			if (existingUsers != null) {
				existingUsersMap = existingUsers.parallelStream()
						.collect(Collectors.toMap(User::getEmailId, Function.identity()));
			}
		}
		return existingUsersMap;
	}

	/****** XNFR-224 ******/
	public void sendLoginAsPartnerEmailNotification(LoginAsEmailNotificationDTO loginAsEmailNotificationDTO) {
		if (loginAsEmailNotificationDTO.isSuperAdminLoggedIn()) {
			logger.debug("Login As Partner Email Notification Will Not Be Sent As The SuperAdmin Logged In...");
		} else {
			Integer partnerCompanyUserId = loginAsEmailNotificationDTO.getPartnerCompanyUserId();
			Integer vendorCompanyUserId = loginAsEmailNotificationDTO.getVendorCompanyUserId();
			String vendorCompanyProfileName = loginAsEmailNotificationDTO.getDomainName();
			LoginAsPartnerDTO loginAsPartnerDto = null;
			if (StringUtils.hasText(vendorCompanyProfileName)) {
				loginAsPartnerDto = utilDao.findLoginAsPartnerSettingsOptions(vendorCompanyProfileName,
						partnerCompanyUserId);
			} else {
				Integer vendorCompanyId = userDAO.getCompanyIdByUserId(vendorCompanyUserId);
				Integer partnerCompanyId = userDAO.getCompanyIdByUserId(partnerCompanyUserId);
				loginAsPartnerDto = utilDao.findLoginAsPartnerSettingsOptionsByVendorCompanyIdAndPartnerCompanyId(
						vendorCompanyId, partnerCompanyId);
			}

			if (loginAsPartnerDto != null && loginAsPartnerDto.isLoginAsPartnerEmailNotificationEnabled()) {
				UserDTO partnerCompanyUserDTO = userDAO.getEmailIdAndDisplayName(partnerCompanyUserId);
				UserDTO vendorCompanyUserDTO = userDAO.getEmailIdAndDisplayName(vendorCompanyUserId);
				String vendorCompanyName = userDAO.getCompanyNameByUserId(vendorCompanyUserId);
				thymeleafService.sendLoginAsPartnerEmailNotification(partnerCompanyUserDTO, vendorCompanyUserDTO,
						loginAsEmailNotificationDTO.getDomainName(), vendorCompanyName);
			}
		}

	}

	public void sendCRMIntegrationInvalidNotification(Integer companyId, String crmName) {
		if (companyId != null && companyId > 0) {
			List<User> admins = partnershipDao.getOrgAdminsOrVendorsOrVendorTiersOrMarketing(companyId);
			String partnerModuleName = utilService.findPartnerModuleCustomNameByCompanyId(companyId);
			for (User admin : admins) {
				if (admin != null) {
					Context context = new Context();
					Map<String, Object> model = new HashMap<>();
					model.put("receiverName", mailService.getFullName(admin.getFirstName(), admin.getLastName()));
					model.put("loginUrl", partnerLoginUrl);
					model.put("partnerModuleName", partnerModuleName);
					model.put("crmName", crmName);
					context.setVariables(model);
					String html = templateEngine.process("integration-expired", context);
					String subject = utilService.addPerfixToSubject(
							crmIntegrationInvalidSubjectPre + crmName + " " + crmIntegrationInvalidSubjectPost);
					mailService.sendMail(new EmailBuilder().from(fromEmail).senderName(fromName).to(admin.getEmailId())
							.subject(subject).body(html).build());
				}
			}
		}
	}

	public void publishAsset(DamUploadPostDTO damUploadPostDTO) {
		damDao.publishAsset(damUploadPostDTO);
		/***** XBI-1829 ****/
		List<Integer> updatedPartnerIds = damUploadPostDTO.getUpdatedPartnerIds();
		List<Integer> publishedPartnerUserIds = damUploadPostDTO.getPublishedPartnerUserIds();
		if (publishedPartnerUserIds != null && !publishedPartnerUserIds.isEmpty()) {
			if (damUploadPostDTO.isPublishingToPartnersInsidePartnerList()) {
				updatedPartnerIds = publishedPartnerUserIds;
			} else {
				updatedPartnerIds.removeAll(publishedPartnerUserIds);
			}
		}
		String debugMessage = "Sending Asset Shared Email Notifications To " + updatedPartnerIds;
		logger.debug(debugMessage);
		/***** XBI-1829 ****/
		checkPartnerIdsAndSendAssetSharedEmailNotification(updatedPartnerIds, damUploadPostDTO.getCompanyName(),
				damUploadPostDTO.getAssetName(), damUploadPostDTO.isAssetSharedEmailNotification(),
				damUploadPostDTO.getLoggedInUserId(), damUploadPostDTO.getDamPartnerIds());
	}

	public void generateThumbnailAndPublishToPartners(DamPostDTO damPostDTO) {
		Set<Integer> partnerGroupIds = damPostDTO.getPartnerGroupIds();
		Set<Integer> partnerIds = damPostDTO.getPartnerIds();
		boolean isAssetCanBePublished = XamplifyUtils.isPartnerGroupOrPartnerCompanySelected(partnerGroupIds,
				partnerIds);
		if (isAssetCanBePublished) {
			DamUploadPostDTO damUploadPostDTO = new DamUploadPostDTO();
			damUploadPostDTO.setPartnerGroupIds(damPostDTO.getPartnerGroupIds());
			damUploadPostDTO.setPartnerIds(damPostDTO.getPartnerIds());
			damUploadPostDTO.setPartnerGroupSelected(damPostDTO.isPartnerGroupSelected());
			damUploadPostDTO.setLoggedInUserId(damPostDTO.getCreatedBy());
			damUploadPostDTO.setDamId(damPostDTO.getDamId());
			damUploadPostDTO.setShareAsWhiteLabeledAsset(damPostDTO.isShareAsWhiteLabeledAsset());
			damUploadPostDTO.setAssetSharedEmailNotification(true);
			damUploadPostDTO.setAssetName(damPostDTO.getName());
			publishAsset(damUploadPostDTO);
		}

	}

	public void publishVideoAsset(DamUploadPostDTO damUploadPostDTO) {
		Set<Integer> partnerIds = damUploadPostDTO.getPartnerIds();
		Set<Integer> partnerGroupIds = damUploadPostDTO.getPartnerGroupIds();
		boolean isAssetCanBePublished = XamplifyUtils.isPartnerGroupOrPartnerCompanySelected(partnerGroupIds,
				partnerIds);
		if (isAssetCanBePublished) {
			Dam dam = damDao.getById(damUploadPostDTO.getDamId());
			DamVideoDTO damVideoDTO = damDao.findDamAndVideoDetailsByVideoId(damUploadPostDTO.getVideoId());
			damUploadPostDTO.setDamVideoDTO(damVideoDTO);
			damDao.publishVideoAsset(dam, damUploadPostDTO);
			damUploadPostDTO.setAssetName(dam.getAssetName());
			/***** XBI-1829 ****/
			List<Integer> updatedPartnerIds = damUploadPostDTO.getUpdatedPartnerIds();
			List<Integer> publishedPartnerUserIds = damUploadPostDTO.getPublishedPartnerUserIds();
			if (publishedPartnerUserIds != null && !publishedPartnerUserIds.isEmpty()) {
				updatedPartnerIds.removeAll(publishedPartnerUserIds);
			}
			String debugMessage = "Sending Asset Shared Email Notifications To " + updatedPartnerIds;
			logger.debug(debugMessage);
			/***** XBI-1829 ****/
			checkPartnerIdsAndSendAssetSharedEmailNotification(updatedPartnerIds,
					dam.getCompanyProfile().getCompanyName(), damUploadPostDTO.getAssetName(),
					damUploadPostDTO.isAssetSharedEmailNotification(), damUploadPostDTO.getLoggedInUserId(),
					damUploadPostDTO.getDamPartnerIds());
		}

	}

	/********* XNFR-327 ********/
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void publishTrackOrPlayBook(LearningTrackDto learningTrackDto) {
		Integer loggedInUserId = learningTrackDto.getUserId();
		Integer learningTrackId = learningTrackDto.getId();
		CompanyProfile loggedInCompany = userService.getCompanyProfileByUser(loggedInUserId);
		LearningTrack learningTrack = genericDAO.get(LearningTrack.class, learningTrackId);
		if (learningTrack != null) {
			insertVisibility(learningTrackDto, learningTrack, loggedInCompany);
			User publishedUser = genericDAO.get(User.class, loggedInUserId);
			Date publishedDate = new Date();
			if (learningTrack.isPublished()) {
				learningTrack.setLoadVisibilityUsersFromDb(true);
				sendNotificationToPartners(learningTrack, publishedUser, publishedDate);
				sendSuccessMailToVendor(learningTrack, publishedUser, publishedDate);
			}
			learningTrack.setPublishingOrWhiteLabelingInProgress(false);
		} else {
			String errorMessage = "Learning Track Not Found For " + learningTrackId;
			logger.error(errorMessage);
		}

	}

	/********* XNFR-327 ********/
	private void insertVisibility(LearningTrackDto learningTrackDto, LearningTrack learningTrack,
			CompanyProfile loggedInCompany) {
		Set<Integer> allUserIds = new HashSet<>();
		List<Integer> deactivatedPartners = utilDao.findDeactivedPartnersByCompanyId(loggedInCompany.getId());
		allUserIds.addAll(insertVisibilityUsers(learningTrackDto.getUserIds(), learningTrackDto, learningTrack,
				loggedInCompany, deactivatedPartners));

		if (learningTrackDto.getGroupIds() != null && !learningTrackDto.getGroupIds().isEmpty()) {
			for (Integer groupId : learningTrackDto.getGroupIds()) {
				if (groupId != null && groupId > 0) {
					UserList userList = genericDAO.get(UserList.class, groupId);
					if (userList != null) {
						Set<Integer> userIds = insertVisibilityGroup(userList, learningTrackDto, learningTrack,
								loggedInCompany, deactivatedPartners);
						allUserIds.addAll(userIds);
					}
				}
			}
		}

		if (!XamplifyUtils.isNotEmptySet(allUserIds)) {
			learningTrack.setPublished(false);
		}
	}

	/********* XNFR-327 ********/
	private Set<Integer> insertVisibilityUsers(Set<Integer> userIds, LearningTrackDto learningTrackDto,
			LearningTrack learningTrack, CompanyProfile loggedInCompany, List<Integer> deactivatedPartners) {
		Map<Integer, Partnership> companyPartnershipMap = new HashMap<>();
		Set<Integer> addedUserIds = new HashSet<>();
		if (XamplifyUtils.isNotEmptySet(userIds)) {
			for (Integer userId : userIds) {
				if (userId == null || deactivatedPartners.contains(userId)) {
					continue;
				}

				User user = userService.loadUser(Arrays.asList(new Criteria(USER_ID, OPERATION_NAME.eq, userId)),
						new FindLevel[] { FindLevel.COMPANY_PROFILE });

				CompanyProfile company = user.getCompanyProfile();
				if (company != null) {
					Partnership partnership = companyPartnershipMap.computeIfAbsent(company.getId(),
							k -> partnershipDao.checkPartnership(loggedInCompany.getId(), company.getId()));

					if (partnership != null) {
						LearningTrackVisibility visibilityUser = lmsDao.getVisibilityUser(user.getUserId(),
								partnership.getId(), learningTrack.getId());

						if (visibilityUser == null) {
							insertVisibilityUser(learningTrackDto, partnership, learningTrack, user, null);
						} else {
							visibilityUser.setAssociatedThroughCompany(true);
							if (learningTrack.isPublished()) {
								visibilityUser.setPublished(true);
								visibilityUser.setPublishedOn(new Date());
							}
						}
						addedUserIds.add(user.getUserId());
					}
				}
			}
		}
		return addedUserIds;
	}

	/********* XNFR-327 ********/
	private Set<Integer> insertVisibilityGroup(UserList userList, LearningTrackDto learningTrackDto,
			LearningTrack learningTrack, CompanyProfile loggedInCompany, List<Integer> deactivatedPartners) {
		Set<Integer> userIds = new HashSet<>();
		Set<UserUserList> userUserLists = userList.getUserUserLists();
		if (userUserLists != null && !userUserLists.isEmpty()) {
			for (UserUserList userUserList : userUserLists) {
				if (userUserList == null || userUserList.getUser() == null)
					continue;

				User user = userUserList.getUser();
				if (deactivatedPartners.contains(user.getUserId()))
					continue;

				CompanyProfile company = user.getCompanyProfile();
				Partnership partnership = null;
				List<User> companyUsers = new ArrayList<>();

				if (company != null) {
					partnership = partnershipDao.checkPartnership(loggedInCompany.getId(), company.getId());
					companyUsers = userDAO.getAllUsersByCompanyId(company.getId());
				} else {
					partnership = partnershipDao.getPartnershipByRepresentingPartner(user, loggedInCompany);
					if (partnership != null) {
						companyUsers.add(partnership.getRepresentingPartner());
					}
				}

				if (partnership != null && !companyUsers.isEmpty()) {
					for (User companyUser : companyUsers) {
						if (deactivatedPartners.contains(companyUser.getUserId()))
							continue;

						LearningTrackVisibility visibilityUser = lmsDao.getVisibilityUser(companyUser.getUserId(),
								partnership.getId(), learningTrack.getId());

						if (visibilityUser == null) {
							insertVisibilityUser(learningTrackDto, partnership, learningTrack, companyUser, userList);
						} else {
							saveVisibilityGroup(userList, visibilityUser, learningTrackDto.getUserId());
						}
						userIds.add(companyUser.getUserId());
					}
				}
			}
		}
		return userIds;
	}

	/********* XNFR-327 ********/
	private void insertVisibilityUser(LearningTrackDto learningTrackDto, Partnership partnerShip,
			LearningTrack learningTrack, User visibilityUser, UserList userList) {
		insertVisibilityUser(learningTrackDto.getUserId(), partnerShip, learningTrack, visibilityUser, userList,
				userList == null);
	}

	/********* XNFR-327 ********/
	private LearningTrackVisibility insertVisibilityUser(Integer loggedInUserId, Partnership partnerShip,
			LearningTrack learningTrack, User user, UserList userList, boolean isAssociatedThroughCompany) {
		LearningTrackVisibility visibilityUser = null;
		if (user != null && partnerShip != null && learningTrack != null) {
			visibilityUser = new LearningTrackVisibility();
			visibilityUser.setPartnership(partnerShip);
			visibilityUser.setLearningTrack(learningTrack);
			visibilityUser.setUser(user);
			if (user.getTeamMembers() != null && !user.getTeamMembers().isEmpty()) {
				visibilityUser.setTeamMember(user.getTeamMembers().get(0));
			}
			visibilityUser.setAssociatedThroughCompany(isAssociatedThroughCompany);
			visibilityUser.setCreatedBy(loggedInUserId);
			visibilityUser.initialiseCommonFields(true, loggedInUserId);
			/**** XNFR-324 ****/
			if (learningTrack.isPublished()) {
				visibilityUser.setPublished(true);
				visibilityUser.setPublishedOn(new Date());
			}
			/**** XNFR-324 ****/
			genericDAO.save(visibilityUser);
			if (userList != null) {
				saveVisibilityGroup(userList, visibilityUser, loggedInUserId);
			}
		}
		return visibilityUser;
	}

	/**** XNFR-327 *******/
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void updatePublishTrackOrPlayBook(LearningTrackDto learningTrackDto) {
		Integer loggedInUserId = learningTrackDto.getUserId();
		Integer learningTrackId = learningTrackDto.getId();
		CompanyProfile loggedInCompany = userService.getCompanyProfileByUser(loggedInUserId);
		LearningTrack learningTrack = genericDAO.get(LearningTrack.class, learningTrackId);
		/*** XNFR-523 ***/
		learningTrack.setExistingVisibilityUserIds(learningTrackDto.getPreviouslySelectedVisibilityUserIds());
		learningTrack.setProgressedVisibilityUserIds(learningTrackDto.getProgressedVisibilityUserIds());
		learningTrack.setPublishedTrackUpated(learningTrackDto.isPublishedTrackUpdated());
		/*** XNFR-523 ***/
		updateVisibility(learningTrackDto, learningTrack, loggedInCompany);
		updateAllPartnerProgress(learningTrack);// Remove this code.
		User publishedUser = genericDAO.get(User.class, loggedInUserId);
		Date publishedDate = new Date();
		learningTrack.setLoadVisibilityUsersFromDb(true);
		sendNotificationToPartners(learningTrack, publishedUser, publishedDate);
		learningTrack.setPublishingOrWhiteLabelingInProgress(false);
		if (learningTrack.isPublished()) {
			sendSuccessMailToVendor(learningTrack, publishedUser, publishedDate);
		}
	}

	/**** XNFR-327 *******/
	private void updateAllPartnerProgress(LearningTrack learningTrack) {
		genericDAO.flushCurrentSession();
		Integer totalContentCount = 0;
		List<LearningTrackContent> totalContents = lmsDao.getLearningTrackContent(learningTrack.getId());
		if (totalContents != null && !totalContents.isEmpty()) {
			totalContentCount = totalContents.size();
		}
		Set<LearningTrackVisibility> visibilityUsers = learningTrack.getVisibilityUsers();
		for (LearningTrackVisibility visibilityUser : visibilityUsers) {
			if (visibilityUser != null && visibilityUser.getProgress() != null && visibilityUser.getProgress() > 0) {
				calculateAndSavePartnerProgress(learningTrack, visibilityUser, totalContentCount);
			}
		}
	}

	/**** XNFR-327 *******/
	private void calculateAndSavePartnerProgress(LearningTrack learningTrack, LearningTrackVisibility visibilityUser,
			Integer totalContentCount) {
		List<Integer> finishedContentIds = lmsDao.getPartnerFinishedContent(learningTrack.getId(),
				visibilityUser.getId());
		Integer finishedCount = 0;
		if (finishedContentIds != null && !finishedContentIds.isEmpty()) {
			finishedCount = finishedContentIds.size();
		}
		Integer progress = null;
		if (totalContentCount != null && totalContentCount > 0 && finishedCount != null && finishedCount > 0
				&& finishedCount <= totalContentCount) {
			if (finishedCount == totalContentCount) {
				progress = 100;
			} else {
				progress = (100 / totalContentCount) * finishedCount;
			}
		}
		visibilityUser.setProgress(progress);
	}

	/**** XNFR-327 *******/
	private void updateVisibility(LearningTrackDto learningTrackDto, LearningTrack learningTrack,
			CompanyProfile loggedInCompany) {
		Set<Integer> newIds = new HashSet<>(learningTrackDto.getUserIds());
		Set<Integer> existingIds = new HashSet<>();
		Set<Integer> newGroupIds = new HashSet<>(learningTrackDto.getGroupIds());
		Set<Integer> existingGroupIds = new HashSet<>();
		Set<Integer> allVisibleUserIds = new HashSet<>();

		lmsDao.retainGroups(newGroupIds, learningTrack.getId());
		List<Integer> deactivatedPartners = utilDao.findDeactivedPartnersByCompanyId(loggedInCompany.getId());
		for (LearningTrackVisibility visibilityUser : learningTrack.getVisibilityUsers()) {
			visibilityUser.setPublished(learningTrackDto.isPublished());
			if (learningTrackDto.isPublished()) {
				visibilityUser.setPublishedOn(new Date());
			}

			Integer userId = visibilityUser.getUser().getUserId();
			if (visibilityUser.isAssociatedThroughCompany()) {
				existingIds.add(userId);

				if (newIds != null && !newIds.contains(userId)) {
					if (visibilityUser.getGroups() != null && !visibilityUser.getGroups().isEmpty()) {
						visibilityUser.setAssociatedThroughCompany(false);
						for (LearningTrackVisibilityGroup group : visibilityUser.getGroups()) {
							existingGroupIds.add(group.getUserList().getId());
						}
						allVisibleUserIds.add(userId);
					} else {
						formDao.deleteLearningTrackQuizSubmissionsByUserId(learningTrack.getId(), userId);
						genericDAO.remove(visibilityUser);
					}
				} else {
					if (visibilityUser.getGroups() != null) {
						for (LearningTrackVisibilityGroup group : visibilityUser.getGroups()) {
							existingGroupIds.add(group.getUserList().getId());
						}
					}
					allVisibleUserIds.add(userId);
				}
			} else {
				if (visibilityUser.getGroups() != null && !visibilityUser.getGroups().isEmpty()) {
					for (LearningTrackVisibilityGroup group : visibilityUser.getGroups()) {
						existingGroupIds.add(group.getUserList().getId());
					}
					allVisibleUserIds.add(userId);
				} else {
					formDao.deleteLearningTrackQuizSubmissionsByUserId(learningTrack.getId(), userId);
					genericDAO.remove(visibilityUser);
				}
			}
		}

		if (newIds != null && !newIds.isEmpty()) {
			newIds.removeAll(existingIds);
			Set<Integer> addedUserIds = insertVisibilityUsers(newIds, learningTrackDto, learningTrack, loggedInCompany,
					deactivatedPartners);
			allVisibleUserIds.addAll(addedUserIds);
		}

		if (XamplifyUtils.isNotEmptySet(newGroupIds)) {
			for (Integer groupId : newGroupIds) {
				if (groupId != null && groupId > 0) {
					UserList userList = genericDAO.get(UserList.class, groupId);
					if (userList != null) {
						Set<Integer> addedGroupUserIds = insertVisibilityGroup(userList, learningTrackDto,
								learningTrack, loggedInCompany, deactivatedPartners);
						allVisibleUserIds.addAll(addedGroupUserIds);
					}
				}
			}
		}

		if (!XamplifyUtils.isNotEmptySet(allVisibleUserIds)) {
			learningTrack.setPublished(false);
		}
	}

	// *******XNFR-316**************
	public void sendWorkflowEmail(User sender, User receiver, EmailTemplateDTO emailTemplateDTO) {
		if (sender != null && receiver != null && emailTemplateDTO != null) {
			mailService.sendWorkflowEmail(sender, receiver, emailTemplateDTO);
		}
	}

	/******
	 * XNFR-342
	 * 
	 * 
	 ******/
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void publishDamToNewlyAddedPartners(ShareContentRequestDTO shareContentRequestDTO, boolean damAccess) {
		try {
			if (damAccess) {
				Set<Integer> damIds = shareContentRequestDTO.getDamIds();
				User loggedInUser = userService.loadUser(
						Arrays.asList(
								new Criteria(USER_ID, OPERATION_NAME.eq, shareContentRequestDTO.getLoggedInUserId())),
						new FindLevel[] { FindLevel.COMPANY_PROFILE });
				Integer userListId = shareContentRequestDTO.getUserListId();
				Set<UserDTO> partners = new LinkedHashSet<>();
				addPartners(shareContentRequestDTO, userListId, partners);
				List<DamBasicInfo> publishedDams = damDao.findAssociatedDamBasicInfoByIds(damIds);
				boolean assetPublishVendorEmailNotification = false;
				CompanyProfile companyProfile = loggedInUser.getCompanyProfile();
				if (companyProfile != null) {
					assetPublishVendorEmailNotification = companyProfile.isAssetPublishVendorEmailNotification();
				}

				if (publishedDams != null && !publishedDams.isEmpty() && !partners.isEmpty()) {
					Set<Integer> userListIds = new LinkedHashSet<>();
					userListIds.add(userListId);
					ContentSharedEmailNotificationDTO contentSharedEmailNotificationDTO = new ContentSharedEmailNotificationDTO();
					List<String> names = publishedDams.stream().map(DamBasicInfo::getAssetName)
							.collect(Collectors.toList());
					contentSharedEmailNotificationDTO.setNames(names);
					contentSharedEmailNotificationDTO
							.setPublishedToPartnerList(shareContentRequestDTO.isPublishingToPartnerList());
					String receiverName = XamplifyUtils.getFullName(loggedInUser);
					contentSharedEmailNotificationDTO.setReceiverEmailId(loggedInUser.getEmailId());
					contentSharedEmailNotificationDTO.setReceiverName(receiverName);
					if (!shareContentRequestDTO.isPublishingToPartnerList()) {
						contentSharedEmailNotificationDTO.setPartnerCompanyName(
								partners.stream().map(UserDTO::getCompanyName).collect(Collectors.toList()).get(0));
						contentSharedEmailNotificationDTO.setPartnerEmailId(
								partners.stream().map(UserDTO::getEmailId).collect(Collectors.toList()).get(0));
					} else {
						contentSharedEmailNotificationDTO
								.setPartnerListName(userListDAO.getUserListNameByUserListId(userListId));
					}
					boolean atleastSharedWithOnePartner = iterateAssetsAndPublishToPartners(userListIds, loggedInUser,
							partners, damAccess, userListId, publishedDams);
					/********* Send Email Notification To Vendor Company **********/
					if (atleastSharedWithOnePartner && assetPublishVendorEmailNotification) {
						thymeleafService.sendAssetSharedWithNewPartnersEmailNotificationToVendorCompany(
								contentSharedEmailNotificationDTO);
					} else {
						String debugMessage = "******No Email Notification Will Be Sent To Vendor Company******"
								+ new Date();
						logger.debug(debugMessage);
					}
				}
				damDao.updateIsPublishedToPartnerListByIds(damIds, false);

			} else {
				String debugMessage = "******DAM Access Removed For This Company.So No Assets Will Be Shared With Partners******"
						+ new Date();
				logger.debug(debugMessage);
			}

		} catch (Exception e) {
			throw new DamDataAccessException(e);
		}

	}

	private void addPartners(ShareContentRequestDTO shareContentRequestDTO, Integer userListId, Set<UserDTO> partners) {
		if (!shareContentRequestDTO.isPublishingToPartnerList()) {
			for (PartnerOrContactInputDTO partnerOrContactInputDTO : shareContentRequestDTO
					.getPartnersOrContactDtos()) {
				UserDTO userDto = new UserDTO();
				userDto.setEmailId(partnerOrContactInputDTO.getEmailId());
				userDto.setFirstName(partnerOrContactInputDTO.getFirstName());
				userDto.setLastName(partnerOrContactInputDTO.getLastName());
				userDto.setCompanyName(partnerOrContactInputDTO.getCompanyName());
				userDto.setId(userDAO.getUserIdByEmail(partnerOrContactInputDTO.getEmailId()));
				partners.add(userDto);
			}
		} else {
			partners.addAll(userListDAO.listPartnersByUserListId(userListId));
		}
	}

	/****** XNFR-342 ******/
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void publishTracksOrPlayBooksToNewlyAddedPartners(ShareContentRequestDTO shareContentRequestDTO,
			boolean hasAccess) {
		try {
			if (hasAccess) {
				Set<Integer> trackOrPlayBookIds = shareContentRequestDTO.getTrackOrPlaybookIds();
				User loggedInUser = userService.loadUser(
						Arrays.asList(
								new Criteria(USER_ID, OPERATION_NAME.eq, shareContentRequestDTO.getLoggedInUserId())),
						new FindLevel[] { FindLevel.COMPANY_PROFILE });

				Integer userListId = shareContentRequestDTO.getUserListId();
				Set<UserDTO> partners = new HashSet<>();
				boolean isPublishingToPartnerList = shareContentRequestDTO.isPublishingToPartnerList();
				if (isPublishingToPartnerList) {
					logger.debug("Publishing To Partner List");
					partners.addAll(userListDAO.listPartnersByUserListId(userListId));
				} else {
					logger.debug("Publishing To Partner");
					String emailId = shareContentRequestDTO.getPartnersOrContactDtos().iterator().next().getEmailId();
					UserDTO userDto = userListDAO.findUserUserListDetailsByUserListIdAndEmailId(userListId, emailId);
					partners.add(userDto);
				}
				List<LearningTrack> learningTrackList = lmsDao.findByIds(trackOrPlayBookIds);
				if (learningTrackList != null && !learningTrackList.isEmpty() && !partners.isEmpty()) {
					Set<Integer> userListIds = new HashSet<>();
					userListIds.add(userListId);

					/**** Delete UnSelected User ListIds/Company Ids *******/
					lmsDao.deleteUnPublishedPartnerCompaniesOrPartnerLists(userListIds, trackOrPlayBookIds);
					Integer partnershipId = shareContentRequestDTO.getPartnershipId();
					boolean isPublishingToPartnerInsideList = partnershipId != null && partnershipId > 0;

					boolean isAtleastSharedWithOnePartner = iteratePartnersAndPublishLms(loggedInUser, partners,
							userListId, learningTrackList, shareContentRequestDTO.isPublishingToPartnerList(),
							shareContentRequestDTO.getPartnershipId(), isPublishingToPartnerInsideList);

					ContentSharedEmailNotificationDTO contentSharedEmailNotificationDTO = prepareContentSharedEmailNotificationDTO(
							loggedInUser, userListId, learningTrackList);

					if (isAtleastSharedWithOnePartner) {
						lmsDao.updatePublishedStatusByIds(trackOrPlayBookIds);
						thymeleafService
								.sendVendorNotificationForSharedTrackOrPlayBook(contentSharedEmailNotificationDTO);
					} else {
						String debugMessage = "******No Email Notification Will Be Sent To Vendor Company******"
								+ new Date();
						logger.debug(debugMessage);
					}
				} else {
					logger.debug("No Tracks Found To Share");
				}
				lmsDao.updateIsPublishedToPartnerListByIds(trackOrPlayBookIds, false);

			} else {
				logger.debug(
						"******LMS Access Removed For This Company.So No Tracks Will Be Shared With Partners******");
			}

		} catch (Exception e) {
			throw new DamDataAccessException(e);
		}

	}

	private ContentSharedEmailNotificationDTO prepareContentSharedEmailNotificationDTO(User loggedInUser,
			Integer userListId, List<LearningTrack> learningTrackList) {
		ContentSharedEmailNotificationDTO contentSharedEmailNotificationDTO = new ContentSharedEmailNotificationDTO();
		List<String> trackOrPlayBookNames = learningTrackList.stream().map(LearningTrack::getTitle)
				.collect(Collectors.toList());
		contentSharedEmailNotificationDTO.setNames(trackOrPlayBookNames);
		String receiverName = XamplifyUtils.getFullName(loggedInUser);
		contentSharedEmailNotificationDTO.setReceiverEmailId(loggedInUser.getEmailId());
		contentSharedEmailNotificationDTO.setReceiverName(receiverName);
		contentSharedEmailNotificationDTO.setPartnerListName(userListDAO.getUserListNameByUserListId(userListId));
		List<LearningTrackType> types = learningTrackList.stream().map(LearningTrack::getType)
				.collect(Collectors.toList());
		String learningTrackType = types.get(0).name();
		boolean isLearningTrack = LearningTrackType.TRACK.name().equals(learningTrackType);
		String moduleName = isLearningTrack ? "Track" : "Play Book";
		contentSharedEmailNotificationDTO.setModuleName(moduleName);

		/*** XNFR-688 ***/
		determineAndSetVendorEmailNotificationPreference(loggedInUser, contentSharedEmailNotificationDTO,
				isLearningTrack);
		return contentSharedEmailNotificationDTO;
	}

	private void determineAndSetVendorEmailNotificationPreference(User loggedInUser,
			ContentSharedEmailNotificationDTO contentSharedEmailNotificationDTO, boolean isLearningTrack) {
		boolean isVendorEmailNotificationRequired = false;

		CompanyProfile companyProfile = loggedInUser.getCompanyProfile();

		if (isLearningTrack) {
			isVendorEmailNotificationRequired = companyProfile.isTrackPublishVendorEmailNotification();
		} else {
			isVendorEmailNotificationRequired = companyProfile.isPlaybookPublishVendorEmailNotification();
		}

		contentSharedEmailNotificationDTO.setVendorEmailNotificationRequired(isVendorEmailNotificationRequired);
	}

	/***** XNFR-445 *****/
	public void uploadCsvToAws(Integer userId, Pagination pagination, DownloadItem dataType,
			Integer downloadDataInfoId) {
		CopiedFileDetails copiedFileDetails = new CopiedFileDetails();
		String folderName = "";
		if (DownloadItem.LEADS_DATA.equals(dataType)) {
			folderName = leadsDataFolder;
			copiedFileDetails = downloadDataUtilService.downloadLeadsData(userId, pagination, dataType, folderName);
		} else if (DownloadItem.DEALS_DATA.equals(dataType)) {
			folderName = dealsDataFolder;
			copiedFileDetails = downloadDataUtilService.downloadDealsData(userId, pagination, dataType, folderName);
		}
		uploadToAws(userId, folderName, dataType, copiedFileDetails, downloadDataInfoId, pagination);
	}

	private void uploadToAws(Integer userId, String folderName, DownloadItem dataType,
			CopiedFileDetails copiedFileDetails, Integer downloadDataInfoId, Pagination pagination) {
		String date = new SimpleDateFormat("ddMMyyyy").format(new Date());
		String amazonFilePath = amazonEnvFolder + folderName + userId + "/" + date + "/"
				+ copiedFileDetails.getUpdatedFileName();
		String localFilePath = copiedFileDetails.getCompleteName();
		amazonWebService.uploadDataToAws(amazonFilePath, localFilePath);
		String completeAmazonFilePath = amazonBaseUrl + amazonBucketName + "/" + amazonFilePath;
		xamplifyUtil.replaceS3WithCloudfrontViceVersa(completeAmazonFilePath);
		utilDao.updateDownloadDataInfo(userId, completeAmazonFilePath, dataType, downloadDataInfoId);
		sendDownloadDataEmail(userId, completeAmazonFilePath, dataType, pagination);
	}

	private void sendDownloadDataEmail(Integer userId, String completeAmazonFilePath, DownloadItem dataType,
			Pagination pagination) {
		User loggedInUser = userService.loadUser(Arrays.asList(new Criteria(USER_ID, OPERATION_NAME.eq, userId)),
				new FindLevel[] { FindLevel.SHALLOW });
		String fromEmailId = fromEmail;
		String senderFullName = fromName;
		try {
			/**** XNFR-930 ***/
			String cloudFrontUrl = xamplifyUtil.replaceS3WithCloudfrontViceVersa(completeAmazonFilePath);
			/***** XNFR-930 ***/
			Context context = new Context();
			Map<String, Object> model = new HashMap<>();
			String firstName = loggedInUser.getFirstName() == null ? "there" : loggedInUser.getFirstName();
			String subject = "Hi " + firstName + " Your Module Report is Ready! 🚀";
			model.put("firstName", firstName);
			model.put("completeAmazonFilePath", cloudFrontUrl);
			if (dataType.equals(DownloadItem.CAMPAIGNS_DATA)) {
				subject = subject.replaceAll("Module", "Campaigns");
				model.put("moduleName", "campaign");
			} else if (dataType.equals(DownloadItem.LEADS_DATA)) {
				subject = subject.replaceAll("Module", "Leads");
				model.put("moduleName", "leads");
			} else if (dataType.equals(DownloadItem.DEALS_DATA)) {
				subject = subject.replaceAll("Module", "Deals");
				model.put("moduleName", "deals");
			} else if (dataType.equals(DownloadItem.MASTER_PARTNER_LIST)) {
				subject = subject.replaceAll("Module", "MasterPartner");
				model.put("moduleName", "master partner");
			} else if (dataType.equals(DownloadItem.PARTNER_LIST)) {
				subject = subject.replaceAll("Module", "Partners");
				model.put("moduleName", "partners");
			} else if (dataType.equals(DownloadItem.CONTACT_LIST)) {
				subject = subject.replaceAll("Module", "Contacts");
				model.put("moduleName", "contacts");
			} else if (dataType.equals(DownloadItem.SHARE_LEADS)) {
				subject = subject.replaceAll("Module", "Share Leads");
				model.put("moduleName", "share leads");
			} else if (dataType.equals(DownloadItem.SHARED_LEADS)) {
				subject = subject.replaceAll("Module", "Shared Leads");
				model.put("moduleName", "shared leads");
			}
			context.setVariables(model);
			String htmlBody = templateEngine.process("download-csv", context);
			subject = utilService.addPerfixToSubject(subject);
			if (pagination != null && pagination.isVanityUrlFilter()) {
				VanityUrlDetailsDTO vanityUrlDetailsDTO = utilService.getVanityUrlFilteredData(pagination.getUserId(),
						pagination.isVanityUrlFilter(), utilDao.getPrmCompanyProfileName());
				Integer adminId = utilDao.findAdminIdByCompanyId(vanityUrlDetailsDTO.getVendorCompanyId());
				User sender = userService.loadUser(Arrays.asList(new Criteria(USER_ID, OPERATION_NAME.eq, adminId)),
						new FindLevel[] { FindLevel.SHALLOW });
				if (vanityUrlDetailsDTO.isPartnerLoggedInThroughVanityUrl()) {
					fromEmailId = sender.getEmailId();
					senderFullName = getFullName(sender.getFirstName(), sender.getLastName());
				}
			}
			mailService.sendMail(new EmailBuilder().from(fromEmailId).senderName(senderFullName)
					.to(loggedInUser.getEmailId()).subject(subject).body(htmlBody).build());
		} catch (Exception e) {
			logger.error("error occured in sendDownloadDataEmail()");
		}
	}

	public void uploadUserList(Integer userId, UserListPaginationWrapper userListPaginationWrapper,
			Integer downloadDataInfoId) {
		User loggedInUser = userDAO.findByPrimaryKey(userId,
				new FindLevel[] { FindLevel.COMPANY_PROFILE, FindLevel.ROLES });
		Pagination pagination = userListPaginationWrapper.getPagination();
		UserListDTO userListDTO = userListPaginationWrapper.getUserList();
		pagination.setMaxResults(pagination.getTotalRecords());
		Map<String, Object> resultMap = null;
		if (userListDTO.isEditList()) {
			List<Integer> userListIds = new ArrayList<Integer>();
			userListIds.add(userListDTO.getId());
			resultMap = userListService.listUserListscontacts(pagination, userListIds, loggedInUser, userListDTO);
		} else {
			resultMap = userListService.listAllUserListContacts(userListPaginationWrapper, userId);
		}
		@SuppressWarnings("unchecked")
		List<UserDTO> userDtos = (List<UserDTO>) resultMap.get("listOfUsers");
		String listName = userListDTO.getName();
		String moduleName = userListDTO.getModuleName().toLowerCase().replace(" ", "");
		String folderName = "";
		String fileName = "";
		DownloadItem dataType = null;
		if (moduleName.equalsIgnoreCase("partners")) {
			folderName = partnerList;
			fileName = listName + "_Partner-List";
			dataType = DownloadItem.PARTNER_LIST;
		} else if (moduleName.equalsIgnoreCase("contacts")) {
			folderName = contactList;
			fileName = listName + "_Contact-List";
			dataType = DownloadItem.CONTACT_LIST;
		} else if (moduleName.equalsIgnoreCase("leads")) {
			folderName = shareLeadsList;
			fileName = listName + "_Share-Leads-List";
			dataType = DownloadItem.SHARE_LEADS;
		} else if (moduleName.equalsIgnoreCase("sharedleads")) {
			folderName = sharedLeadsList;
			fileName = listName + "_Shared-Leads-List";
			dataType = DownloadItem.SHARED_LEADS;
		} else {
			folderName = masterPartnerList;
			fileName = "master_partner_list";
			dataType = DownloadItem.MASTER_PARTNER_LIST;
		}
		CopiedFileDetails copiedFileDetails = null;
		if (userListDTO.isEditList()) {
			copiedFileDetails = downloadDataUtilService.editListCsv(userId, userDtos, folderName, fileName,
					userListDTO);
		} else {
			copiedFileDetails = downloadDataUtilService.userListCsv(userId, userDtos, folderName, fileName,
					userListDTO);
		}
		uploadToAws(userId, folderName, dataType, copiedFileDetails, downloadDataInfoId, null);
	}

	/*** XNFR-434 ***/
	public void replaceAndPublishAsset(DamAwsDTO damAwsDTO, DamUploadPostDTO damUploadPostDTO) {
		if (damAwsDTO != null && damUploadPostDTO != null) {
			Integer damId = damAwsDTO.getDamId();
			damUploadPostDTO.setDamId(damId);
			FilePathAndThumbnailPath filePathAndThumbnailPath = amazonWebService.uploadAssetAndThumbnail(damAwsDTO);
			String awsFilePath = filePathAndThumbnailPath.getFilePath();
			String thumbnailPath = filePathAndThumbnailPath.getThumbnailPath();
			String updatedThumbnailPath = "";
			updatedThumbnailPath = getUpdatedThumbnailPath(damAwsDTO, damUploadPostDTO, thumbnailPath,
					updatedThumbnailPath);
			damDao.updateAssetPathAndThumbnailPath(damId, awsFilePath, updatedThumbnailPath);
			/***** Publish This Asset ****/
			publishAsset(damUploadPostDTO);
		}

	}

	public void sendTeamMemberSignedUpEmailNotificationsToAdminsAndPublishTracksOrPlayBooks(
			SignUpRequestDTO signUpRequestDTO) {
		asyncComponent.sendTeamMemberSignedUpEmailNotificationsToAdmins(signUpRequestDTO);
		String emailId = signUpRequestDTO.getEmailId().toLowerCase().trim();
		String firstName = signUpRequestDTO.getFirstName();
		String lastName = signUpRequestDTO.getLastName();
		Integer loggedInUserCompanyId = signUpRequestDTO.getCompanyId();
		List<TeamMemberDTO> teamMemberDtos = new ArrayList<>();
		TeamMemberDTO teamMemberDTO = new TeamMemberDTO();
		teamMemberDTO.setEmailId(emailId);
		teamMemberDTO.setFirstName(firstName);
		teamMemberDTO.setLastName(lastName);
		teamMemberDtos.add(teamMemberDTO);
		Integer loggedInUserId = teamDao.findPrimaryAdminIdByCompanyId(loggedInUserCompanyId);
		asyncComponent.publishLMSToNewTeamMembers(teamMemberDtos, loggedInUserId);
	}

	/**** XNFR-571 ****/
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class, noRollbackFor = {
			DataIntegrityViolationException.class })
	public void publishDashboardButtons(DashboardButtonsDTO dashboardButtonsDTO) {
		Set<Integer> partnerGroupIds = dashboardButtonsDTO.getPartnerGroupIds();
		Set<Integer> partnerIds = dashboardButtonsDTO.getPartnerIds();
		Integer dashboardButtonId = dashboardButtonsDTO.getId();
		Integer loggedInUserId = dashboardButtonsDTO.getVendorId();
		Integer companyId = userDAO.getCompanyIdByUserId(loggedInUserId);
		String title = dashboardButtonsDTO.getButtonTitle();
		boolean isDashboardButtonCanBePublished = XamplifyUtils.isPartnerGroupOrPartnerCompanySelected(partnerGroupIds,
				partnerIds);
		List<Integer> insertedPartnerGroupIds = new ArrayList<>();
		List<Integer> insertedPartnerIds = new ArrayList<>();

		if (isDashboardButtonCanBePublished) {
			/***** Publish To Partner Groups *****/
			publishToPartnerGroups(dashboardButtonsDTO, partnerGroupIds, dashboardButtonId, loggedInUserId,
					insertedPartnerGroupIds);
			/*** Publish To Partners ******/
			publishToPartners(dashboardButtonsDTO, partnerIds, dashboardButtonId, loggedInUserId, companyId,
					insertedPartnerIds);
		} else if (dashboardButtonsDTO.isPublishingFromEditSection()) {
			/****
			 * User unselected all partner companies & partner groups.We are deleting all
			 * the data from mapping tables
			 *****/
			deleteUnPublishedPartnerGroupIds(dashboardButtonsDTO);

			deletePartnerCompanies(dashboardButtonsDTO);
		}
		setEmailProperitesAndSendEmail(dashboardButtonId, loggedInUserId, companyId, title, insertedPartnerGroupIds,
				insertedPartnerIds, dashboardButtonsDTO.getPublishedPartnerUserIds());

		vanityURLDao.updatePubishedStatusByDashboardButtonId(dashboardButtonId);

	}

	private void deletePartnerCompanies(DashboardButtonsDTO dashboardButtonsDTO) {
		Set<Integer> publishedPartnerIds = dashboardButtonsDTO.getPublishedPartnerIds();
		vanityURLDao.deletePartnerIds(publishedPartnerIds, dashboardButtonsDTO.getId());
	}

	private void deleteUnPublishedPartnerGroupIds(DashboardButtonsDTO dashboardButtonsDTO) {
		Set<Integer> publishedPartnerGroupIds = dashboardButtonsDTO.getPublishedPartnerGroupIds();
		vanityURLDao.deletePartnerGroupIds(publishedPartnerGroupIds, dashboardButtonsDTO.getId());
	}

	private void setEmailProperitesAndSendEmail(Integer dashboardButtonId, Integer loggedInUserId, Integer companyId,
			String title, List<Integer> insertedPartnerGroupIds, List<Integer> insertedPartnerIds,
			List<Integer> publishedPartnerUserIds) {
		try {
			DashboardButtonsEmailNotificationRequestDTO dashboardButtonsEmailNotificationRequestDTO = new DashboardButtonsEmailNotificationRequestDTO();
			dashboardButtonsEmailNotificationRequestDTO.setLoggedInUserCompanyId(companyId);
			dashboardButtonsEmailNotificationRequestDTO.setLoggedInUserId(loggedInUserId);
			dashboardButtonsEmailNotificationRequestDTO.setDashboardTitle(title);
			dashboardButtonsEmailNotificationRequestDTO.setPartnerGroupIds(insertedPartnerGroupIds);
			dashboardButtonsEmailNotificationRequestDTO.setPartnerIds(insertedPartnerIds);
			dashboardButtonsEmailNotificationRequestDTO.setDashboardButtonId(dashboardButtonId);
			dashboardButtonsEmailNotificationRequestDTO.setPublishedPartnerUserIds(publishedPartnerUserIds);
			boolean isPartnerGroupSelected = XamplifyUtils.isNotEmptyList(insertedPartnerGroupIds);
			boolean isPartnerCompanySelected = XamplifyUtils.isNotEmptyList(insertedPartnerIds);
			dashboardButtonsEmailNotificationRequestDTO
					.setDashboardButtonPublished(isPartnerGroupSelected || isPartnerCompanySelected);

			thymeleafService.sendDashboardButtonPublishedEmailNotificationsToPartnersAndVendors(
					dashboardButtonsEmailNotificationRequestDTO);
		} catch (EmailNotificationException e) {
			String message = "Unable To Send Dashboard Button Published Email:" + title + "(" + dashboardButtonId + ")";
			logger.error(message, e);
		}
	}

	private void publishToPartners(DashboardButtonsDTO dashboardButtonsDTO, Set<Integer> partnerIds,
			Integer dashboardButtonId, Integer loggedInUserId, Integer companyId, List<Integer> insertedPartnerIds) {
		boolean isPartnerIdsOptionSelected = XamplifyUtils.isNotEmptySet(partnerIds);
		if (isPartnerIdsOptionSelected) {
			List<Integer> deactivatedPartners = utilDao.findDeactivedPartnersByCompanyId(companyId);
			partnerIds.removeIf(deactivatedPartners::contains);
			boolean isPublishingFromEditSection = dashboardButtonsDTO.isPublishingFromEditSection();
			if (isPublishingFromEditSection) {
				boolean isPartnerIdsUpdated = !dashboardButtonsDTO.isPartnerIdsMatched();
				if (isPartnerIdsUpdated) {
					/******* Remove Unselected Partners **********/
					List<Integer> previouslyPublishedPartnerIdsCopiedList = deleteUnSelectedPartners(partnerIds,
							dashboardButtonId);

					/***** Insert Newly Selected Partners *********/
					insertNewlyAddedPartners(dashboardButtonsDTO, partnerIds, dashboardButtonId, loggedInUserId,
							companyId, previouslyPublishedPartnerIdsCopiedList, insertedPartnerIds);

				} else {
					String debugMessage = "Nothing To Publish To Partner Companies For  Dashboard Button Id : "
							+ dashboardButtonsDTO.getId();
					logger.debug(debugMessage);
				}

			} else {
				insertIntoDashboardButtonsPartnerCompanyMapping(dashboardButtonsDTO, partnerIds, dashboardButtonId,
						loggedInUserId, companyId, insertedPartnerIds);
			}
		} else {
			if (dashboardButtonsDTO.isPublishingFromEditSection()) {
				deletePartnerCompanies(dashboardButtonsDTO);
			}
		}
	}

	private List<Integer> deleteUnSelectedPartners(Set<Integer> partnerIds, Integer dashboardButtonId) {
		List<Integer> previouslyPublishedPartnerIdsCopiedList = new ArrayList<>();
		List<Integer> previouslyPublishedPartnerIds = vanityURLDao
				.findPublishedPartnerIdsByDashboardButtonId(dashboardButtonId);
		Collections.sort(previouslyPublishedPartnerIds);
		previouslyPublishedPartnerIdsCopiedList.addAll(previouslyPublishedPartnerIds);
		previouslyPublishedPartnerIds.removeAll(partnerIds);
		Set<Integer> previouslyPublishedPartnerIdsSet = XamplifyUtils
				.convertListToSetElements(previouslyPublishedPartnerIds);
		vanityURLDao.deletePartnerIds(previouslyPublishedPartnerIdsSet, dashboardButtonId);
		return previouslyPublishedPartnerIdsCopiedList;
	}

	private void insertNewlyAddedPartners(DashboardButtonsDTO dashboardButtonsDTO, Set<Integer> partnerIds,
			Integer dashboardButtonId, Integer loggedInUserId, Integer companyId,
			List<Integer> previouslyPublishedPartnerIdsCopiedList, List<Integer> insertedPartnerIds) {
		partnerIds.removeAll(previouslyPublishedPartnerIdsCopiedList);
		insertIntoDashboardButtonsPartnerCompanyMapping(dashboardButtonsDTO, partnerIds, dashboardButtonId,
				loggedInUserId, companyId, insertedPartnerIds);
	}

	private void insertIntoDashboardButtonsPartnerCompanyMapping(DashboardButtonsDTO dashboardButtonsDTO,
			Set<Integer> partnerIds, Integer dashboardButtonId, Integer loggedInUserId, Integer companyId,
			List<Integer> insertedPartnerIds) {
		insertedPartnerIds.addAll(partnerIds);
		String publishingDebugMessage = "Publishing Dashboard Buttons To Partner Ids (" + partnerIds.size()
				+ ") Dashboard Button Id : " + dashboardButtonsDTO.getId();
		logger.debug(publishingDebugMessage);
		List<DashboardButtonsPartnersDTO> dashboardButtonsPartnerDTOs = partnershipDao
				.findPartnerIdsAndPartnershipIdsByPartnerIdsAndCompanyId(partnerIds, companyId);
		List<DashboardButtonsPartnerCompanyMapping> dashboardButtonsPartnerCompanyMappings = new ArrayList<>();
		int total = dashboardButtonsPartnerDTOs.size();
		int counter = 1;
		String totalRecordsDebugMessage = "Total Records To Be Iterated : " + total;
		logger.debug(totalRecordsDebugMessage);
		for (DashboardButtonsPartnersDTO dashboardButtonsPartnerDTO : dashboardButtonsPartnerDTOs) {
			String debugMessage = counter + "/" + total + " records iterated ";
			logger.debug(debugMessage);
			DashboardButtonsPartnerCompanyMapping dashboardButtonsPartnerCompanyMapping = new DashboardButtonsPartnerCompanyMapping();
			DashboardButton dashboardButton = new DashboardButton();
			dashboardButton.setId(dashboardButtonId);
			dashboardButtonsPartnerCompanyMapping.setDashboardButton(dashboardButton);
			Partnership partnership = new Partnership();
			partnership.setId(dashboardButtonsPartnerDTO.getPartnershipId());
			dashboardButtonsPartnerCompanyMapping.setPartnership(partnership);
			dashboardButtonsPartnerCompanyMapping.setPublishedOn(new Date());
			User publishedBy = new User();
			publishedBy.setUserId(loggedInUserId);
			dashboardButtonsPartnerCompanyMapping.setPublishedBy(publishedBy);
			User publishedTo = new User();
			publishedTo.setUserId(dashboardButtonsPartnerDTO.getPartnerId());
			dashboardButtonsPartnerCompanyMapping.setPublishedTo(publishedTo);
			dashboardButtonsPartnerCompanyMappings.add(dashboardButtonsPartnerCompanyMapping);
			counter++;
		}
		genericDAO.saveAll(dashboardButtonsPartnerCompanyMappings, "Partners");
	}

	private void publishToPartnerGroups(DashboardButtonsDTO dashboardButtonsDTO, Set<Integer> partnerGroupIds,
			Integer dashboardButtonId, Integer loggedInUserId, List<Integer> insertedPartnerGroupIds) {
		boolean isPartnerGroupIdsSelected = XamplifyUtils.isNotEmptySet(partnerGroupIds);
		if (isPartnerGroupIdsSelected) {
			boolean isPublishingFromEditSection = dashboardButtonsDTO.isPublishingFromEditSection();
			if (isPublishingFromEditSection) {
				boolean isPartnerGroupIdsUpdated = !dashboardButtonsDTO.isPartnerGroupIdsMatched();
				if (isPartnerGroupIdsUpdated) {
					/******* Delete Unselected Partner Groups **********/
					List<Integer> previouslyPublishedPartnerGroupIdsCopiedList = deleteUnSelectedPartnerGroupIds(
							partnerGroupIds, dashboardButtonId);
					/***** Insert Newly Selected Partner Groups *********/
					insertNewlyAddedPartnerGroups(partnerGroupIds, dashboardButtonId, loggedInUserId,
							previouslyPublishedPartnerGroupIdsCopiedList, insertedPartnerGroupIds);
				} else {
					String debugMessage = "Nothing To Publish To Partner Groups :  Dashboard Button Id : "
							+ dashboardButtonsDTO.getId();
					logger.debug(debugMessage);
				}
			} else {
				insertIntoDashboardButtonsPartnerGroupMappingTable(partnerGroupIds, dashboardButtonId, loggedInUserId,
						insertedPartnerGroupIds);
			}

		} else {
			if (dashboardButtonsDTO.isPublishingFromEditSection()) {
				deleteUnPublishedPartnerGroupIds(dashboardButtonsDTO);
			}

		}
	}

	private void insertNewlyAddedPartnerGroups(Set<Integer> partnerGroupIds, Integer dashboardButtonId,
			Integer loggedInUserId, List<Integer> previouslyPublishedPartnerGroupIdsCopiedList,
			List<Integer> insertedPartnerGroupIds) {
		boolean isPublishedAllUserswithInList = userListDAO.isPublishedAllUserswithInList(dashboardButtonId,
				previouslyPublishedPartnerGroupIdsCopiedList);
		if (isPublishedAllUserswithInList) {
			partnerGroupIds.removeAll(previouslyPublishedPartnerGroupIdsCopiedList);
		}

		insertIntoDashboardButtonsPartnerGroupMappingTable(partnerGroupIds, dashboardButtonId, loggedInUserId,
				insertedPartnerGroupIds);
	}

	private List<Integer> deleteUnSelectedPartnerGroupIds(Set<Integer> partnerGroupIds, Integer dashboardButtonId) {
		List<Integer> previouslyPublishedPartnerGroupIdsCopiedList = new ArrayList<>();
		List<Integer> previouslyPublishedPartnerGroupIds = vanityURLDao
				.findPublishedPartnerGroupIdsByDashboardButtonId(dashboardButtonId);
		Collections.sort(previouslyPublishedPartnerGroupIds);
		previouslyPublishedPartnerGroupIdsCopiedList.addAll(previouslyPublishedPartnerGroupIds);
		previouslyPublishedPartnerGroupIds.removeAll(partnerGroupIds);
		Set<Integer> previouslyPublishedPartnerGroupIdsSet = XamplifyUtils
				.convertListToSetElements(previouslyPublishedPartnerGroupIds);
		vanityURLDao.deletePartnerGroupIds(previouslyPublishedPartnerGroupIdsSet, dashboardButtonId);
		return previouslyPublishedPartnerGroupIdsCopiedList;
	}

	private void insertIntoDashboardButtonsPartnerGroupMappingTable(Set<Integer> partnerGroupIds,
			Integer dashboardButtonId, Integer loggedInUserId, List<Integer> insertedPartnerGroupIds) {
		insertedPartnerGroupIds.addAll(partnerGroupIds);
		String publishingDebugMessage = "Publishing Dashboard Buttons To Partner Groups For Dashboard Button Id : "
				+ dashboardButtonId;
		logger.debug(publishingDebugMessage);
		List<DashboardButtonsPartnersDTO> dashboardButtonsPartnerGroupsDTOs = userListDAO
				.findUserListIdsAndPartnerIdsAndPartnershipIdsByUserListIds(partnerGroupIds);
		List<DashboardButtonsPartnerGroupMapping> dashboardButtonsPartnerGroupMappings = new ArrayList<>();
		int total = dashboardButtonsPartnerGroupsDTOs.size();
		int counter = 1;
		String totalRecordsDebugMessage = "Total Partner Groups  : " + total;
		logger.debug(totalRecordsDebugMessage);
		for (DashboardButtonsPartnersDTO dashboardButtonsPartnerGroupsDTO : dashboardButtonsPartnerGroupsDTOs) {
			String debugMessage = counter + "/" + total + " Partner Groups iterated ";
			logger.debug(debugMessage);
			boolean isDashboardButtonPublished = dashboardButtonDao.isDashboardButtonPublished(dashboardButtonId,
					dashboardButtonsPartnerGroupsDTO.getPartnershipId(),
					dashboardButtonsPartnerGroupsDTO.getUserListId(), dashboardButtonsPartnerGroupsDTO.getPartnerId());
			if (isDashboardButtonPublished) {
				logger.debug("AlreadyPublished");
				continue;
			}
			DashboardButtonsPartnerGroupMapping dashboardButtonsPartnerGroupMapping = new DashboardButtonsPartnerGroupMapping();
			DashboardButton dashboardButton = new DashboardButton();
			dashboardButton.setId(dashboardButtonId);
			dashboardButtonsPartnerGroupMapping.setDashboardButton(dashboardButton);
			UserUserList userUserList = new UserUserList();
			userUserList.setId(dashboardButtonsPartnerGroupsDTO.getUserListId());
			dashboardButtonsPartnerGroupMapping.setUserUserList(userUserList);
			Partnership partnership = new Partnership();
			partnership.setId(dashboardButtonsPartnerGroupsDTO.getPartnershipId());
			dashboardButtonsPartnerGroupMapping.setPartnership(partnership);
			dashboardButtonsPartnerGroupMapping.setPublishedOn(new Date());
			User publishedBy = new User();
			publishedBy.setUserId(loggedInUserId);
			dashboardButtonsPartnerGroupMapping.setPublishedBy(publishedBy);
			dashboardButtonsPartnerGroupMappings.add(dashboardButtonsPartnerGroupMapping);
			counter++;
		}
		xamplifyUtilDao.saveAll(dashboardButtonsPartnerGroupMappings, "Partner Groups");
	}

	public String getFullName(String firstName, String lastName) {
		String name = "";
		if (firstName != null) {
			name = firstName + " ";
		}
		if (lastName != null) {
			name = name + lastName;
		}
		return name;
	}

	/** XNFR-735 **/
	public void uploadAttachmentFiles(List<ActivityAWSDTO> activityAWSDTOs) {
		if (XamplifyUtils.isNotEmptyList(activityAWSDTOs)) {
			for (ActivityAWSDTO activityAWSDTO : activityAWSDTOs) {
				String awsFilePath = amazonWebService.uploadAttachmentAndGetFilePath(activityAWSDTO);
				updateEmailAttachmentFilePath(activityAWSDTO, awsFilePath);
				String finalPathToRemove = attachmentPath + activityAWSDTO.getUserId() + sep
						+ activityAWSDTO.getUpdatedFileName();
				deleteTemporaryFolder(finalPathToRemove);
				String finalPathToRemoveFolder = attachmentPath + activityAWSDTO.getUserId();
				deleteTemporaryFolder(finalPathToRemoveFolder);
				logger.info("Activity attachment uploaded successfully.");
			}
		}
	}

	private void updateEmailAttachmentFilePath(ActivityAWSDTO activityAWSDTO, String awsFilePath) {
		for (Integer attachmentId : activityAWSDTO.getAttachmentIds()) {
			emailActivityService.updateEmailAttachmentFilePath(attachmentId, awsFilePath);
		}
	}

	private void deleteTemporaryFolder(String finalPathToRemove) {
		Path filePath = Paths.get(finalPathToRemove);
		try {
			Files.delete(filePath);
			String debugMessage = filePath + " deleted successfully";
			logger.debug(debugMessage);
		} catch (NoSuchFileException e) {
			logger.debug("Error Caught In Deleting File. NoSuchFileException occured for file path :{}, Timestamp:{}",
					filePath, new Date());
		} catch (IOException e) {
			logger.debug("Error Caught In Deleting File. IOException occured for file path :{}, Timestamp:{}", filePath,
					new Date());
		} catch (Exception e) {
			logger.debug("Error Caught In Deleting File. path :{}, Timestamp:{}", filePath, new Date());
		}
	}

	public void deleteAttachmentFilesFromAWS(List<String> awsFilePaths) {
		if (XamplifyUtils.isNotEmptyList(awsFilePaths)) {
			String debugMessage = "Deleting files from aws : " + awsFilePaths.size() + " at " + new Date();
			logger.debug(debugMessage);
			amazonWebService.deleteFilesByUsingFilePaths(awsFilePaths);
			String debugSuccessMessage = awsFilePaths.size() + " file(s) deleted successfully";
			logger.debug(debugSuccessMessage);
		}
	}

	/** XNFR-780 **/
	public void uploadDomainMediaResourcesToAWS(List<DomainMediaResourceDTO> domainMediaResourceDTOs) {
		if (XamplifyUtils.isNotEmptyList(domainMediaResourceDTOs)) {
			for (DomainMediaResourceDTO domainMediaResourceDTO : domainMediaResourceDTOs) {
				File newImageFile = new File(domainMediaResourceDTO.getFilePath());
				if (newImageFile.exists()) {
					String awsFilePath = amazonWebService
							.uploadDomainMediaResourcesAndGetFilePath(domainMediaResourceDTO);
					domainDao.updateDomainMediaResourceFilePath(domainMediaResourceDTO.getId(), awsFilePath);
					String finalPathToRemoveContentInsideFolder = uploadCompanyDomainImagesPath + sep
							+ domainMediaResourceDTO.getDomainName() + sep
							+ domainMediaResourceDTO.getUpdatedFileName();
					deleteTemporaryFolder(finalPathToRemoveContentInsideFolder);
					String finalPathToRemoveFolder = uploadCompanyDomainImagesPath + sep
							+ domainMediaResourceDTO.getDomainName();
					deleteTemporaryFolder(finalPathToRemoveFolder);
				}
			}
			logger.info("Domain Media files uploaded to AWS successfully. Timestamp: {}", new Date());
		}
	}

	public void distributeDashboardButtons(DashboardButtonsToPartnersDTO dashboardButtonsToPartnersDTO) {
		if (XamplifyUtils.isValidInteger(dashboardButtonsToPartnersDTO.getPartnerId())) {
			shareDashboardButtonsWithPartners(dashboardButtonsToPartnersDTO);
		} else {
			sendDashboardButtonsToPartnersFromUserList(dashboardButtonsToPartnersDTO);
		}
	}

	public void shareDashboardButtonsWithPartners(DashboardButtonsToPartnersDTO dashboardButtonsToPartnersDTO) {
		Integer userUserListId = userListDAO.getUserUserListIdByUserListIdAndUserId(
				dashboardButtonsToPartnersDTO.getUserListId(), dashboardButtonsToPartnersDTO.getPartnerId());
		Set<Integer> insertedPartnerGroupIds = new HashSet<>();
		List<Integer> publishedPartnerUserIds = new ArrayList<>();
		List<Integer> insertedPartnerIds = new ArrayList<>();
		boolean isDashboardButtonPublished;
		insertedPartnerIds.add(dashboardButtonsToPartnersDTO.getPartnerId());
		DashboardButtonsPartnersDTO dashboardButtonsPartnerGroupsDTO = new DashboardButtonsPartnersDTO();
		dashboardButtonsPartnerGroupsDTO.setUserListId(userUserListId);
		dashboardButtonsPartnerGroupsDTO.setPartnershipId(dashboardButtonsToPartnersDTO.getPartnershipId());
		Set<String> savedTitles = new HashSet<>();
		if (XamplifyUtils.isNotEmptySet(dashboardButtonsToPartnersDTO.getIds())) {
			// Iterate through the dashboard IDs
			for (Integer dashboardId : dashboardButtonsToPartnersDTO.getIds()) {
				isDashboardButtonPublished = dashboardButtonDao.isDashboardButtonPublished(dashboardId,
						dashboardButtonsToPartnersDTO.getPartnershipId(), userUserListId,
						dashboardButtonsToPartnersDTO.getPartnerId());
				if (isDashboardButtonPublished) {
					logger.debug("Dashboard button has already been published.");
				} else {
					savedTitles.addAll(dashboardButtonDao.savePartnerCompanyDashboardbuttons(dashboardId,
							dashboardButtonsToPartnersDTO.getVendorId(), dashboardButtonsPartnerGroupsDTO));
				}
			}
		}

		sendEmailToVendorAndPartners(dashboardButtonsToPartnersDTO, insertedPartnerGroupIds, publishedPartnerUserIds,
				insertedPartnerIds, savedTitles);
	}

	private void sendDashboardButtonsToPartnersFromUserList(
			DashboardButtonsToPartnersDTO dashboardButtonsToPartnersDTO) {
		Set<Integer> insertedPartnerGroupIds = new HashSet<>();
		insertedPartnerGroupIds.add(dashboardButtonsToPartnersDTO.getUserListId());
		List<Integer> publishedPartnerUserIds = new ArrayList<>();
		List<Integer> insertedPartnerIds = new ArrayList<>();
		Set<String> savedTitles = new HashSet<>();
		List<DashboardButtonsPartnersDTO> dashboardButtonsPartnerGroupsDTOs = userListDAO
				.findUserListIdsAndPartnerIdsAndPartnershipIdsByUserListIds(insertedPartnerGroupIds);
		int total = dashboardButtonsPartnerGroupsDTOs.size();
		String totalRecordsDebugMessage = "Total Partner Groups  : " + total;
		logger.debug(totalRecordsDebugMessage);
		for (Integer dashboardId : dashboardButtonsToPartnersDTO.getIds()) {
			publishedPartnerUserIds = userListDAO.findPublishedPartnerIdsById(dashboardId);
			savedTitles.addAll(dashboardButtonDao.saveDashboardButtonsMapping(dashboardButtonsPartnerGroupsDTOs,
					dashboardId, dashboardButtonsToPartnersDTO.getVendorId(), publishedPartnerUserIds));
		}

		sendEmailToVendorAndPartners(dashboardButtonsToPartnersDTO, insertedPartnerGroupIds, publishedPartnerUserIds,
				insertedPartnerIds, savedTitles);
	}

	private void sendEmailToVendorAndPartners(DashboardButtonsToPartnersDTO dashboardButtonsToPartnersDTO,
			Set<Integer> insertedPartnerGroupIds, List<Integer> publishedPartnerUserIds,
			List<Integer> insertedPartnerIds, Set<String> savedTitles) {
		String titleName = XamplifyUtils.convertSetToCommaSeperatedString(savedTitles);
		Integer compnayId = userDAO.getCompanyIdByUserId(dashboardButtonsToPartnersDTO.getVendorId());
		setEmailProperitesAndSendEmail(0, dashboardButtonsToPartnersDTO.getVendorId(), compnayId, titleName,
				XamplifyUtils.convertSetToList(insertedPartnerGroupIds), insertedPartnerIds, publishedPartnerUserIds);
	}

	/** XNFR-781 **/
	public void sendContentApprovalStatusEmailNotification(ApprovalStatusHistoryDTO approvalStatusHistoryDTO) {
		if (approvalStatusHistoryDTO != null && XamplifyUtils.isValidInteger(approvalStatusHistoryDTO.getCreatedBy())
				&& XamplifyUtils.isValidInteger(approvalStatusHistoryDTO.getLoggedInUserId())
				&& XamplifyUtils.isValidString(approvalStatusHistoryDTO.getModuleType())) {
			String toMailId = userDAO.getEmailIdByUserId(approvalStatusHistoryDTO.getCreatedBy());
			User user = userDAO.getUser(approvalStatusHistoryDTO.getLoggedInUserId());
			if (user != null) {
				String updatedByName = XamplifyUtils.getFullName(user);
				if (XamplifyUtils.isValidString(toMailId) && XamplifyUtils.isValidString(updatedByName)) {
					approvalStatusHistoryDTO.setEmailId(toMailId);
					approvalStatusHistoryDTO.setStatusUpdatedByName(updatedByName);
					String formattedModuleTypeName = formatModuleTypeNameForEmailTemplate(
							approvalStatusHistoryDTO.getModuleType());
					approvalStatusHistoryDTO.setModuleType(formattedModuleTypeName);
					thymeleafService.sendContentApprovalStatusEmailNotification(approvalStatusHistoryDTO);
				}
			}
		}
	}

	private String formatModuleTypeNameForEmailTemplate(String moduleType) {
		if (!XamplifyUtils.isValidString(moduleType)) {
			return "";
		}

		switch (moduleType.toUpperCase()) {
		case "DAM":
			return "Asset";
		case "TRACK":
			return "Track";
		case "PLAYBOOK":
			return "Playbook";
		default:
			return moduleType;
		}
	}

	/** XNFR-821 **/
	public void sendTeamMemberPrivilegesUpdatedForApprovalProccessEmailNotification(
			List<ApprovalPrivilegesEmailNotificationDTO> approvalPrivilegesEmailNotificationDTOs,
			Integer loggedInUserId) {
		try {
			if (XamplifyUtils.isNotEmptyList(approvalPrivilegesEmailNotificationDTOs)
					&& XamplifyUtils.isValidInteger(loggedInUserId)) {
				UserDTO senderDetailsUserDTO = userDAO.getEmailIdAndDisplayName(loggedInUserId);
				Integer companyId = userDAO.getCompanyIdByUserId(loggedInUserId);
				thymeleafService.iterateAndSendTeamMemberPrivilegesUpdatedForApprovalProccessEmailNotification(
						approvalPrivilegesEmailNotificationDTOs, senderDetailsUserDTO, companyId);
			}
		} catch (EmailNotificationException e) {
			logger.debug("Unable To Send Team Member Privileges Updated Email. Error Message: {}, Timestamp: {}",
					e.getMessage(), new Date());
		} catch (Exception e) {
			logger.debug(
					"Exception Occured while Sending Team Member Privileges Updated Email. Error Message: {}, Timestamp: {}",
					e.getMessage(), new Date());
		}
	}

	/** XNFR-822 **/
	public void sendApprovalReminderNotificationForPendingContent(List<Integer> allApproversIds,
			PendingApprovalDamAndLmsDTO pendingApprovalDamAndLmsDTO) {
		try {
			if (XamplifyUtils.isNotEmptyList(allApproversIds)
					&& XamplifyUtils.isValidInteger(pendingApprovalDamAndLmsDTO.getCreatedById())) {
				String debugMessage = "Approval Reminders : " + allApproversIds.size() + " at " + new Date();
				logger.debug(debugMessage);
				UserDTO senderDetailsUserDTO = userDAO
						.getEmailIdAndDisplayName(pendingApprovalDamAndLmsDTO.getCreatedById());
				thymeleafService.iterateAndSendApprovalReminderNotificationForPendingContent(allApproversIds,
						pendingApprovalDamAndLmsDTO, senderDetailsUserDTO);
			}
		} catch (EmailNotificationException e) {
			logger.debug("Unable To Send Reminder Email to All Approvers. Error Message: {}, Timestamp: {}",
					e.getMessage(), new Date());
		} catch (Exception e) {
			logger.debug(
					"Exception Occured while Sending Reminder Email to Approvers. Error Message: {}, Timestamp: {}",
					e.getMessage(), new Date());
		}
	}

	public Set<UserDTO> getCopyListUsers(UserListDTO userListDTO) {
		Integer sourceUserListId = userListDTO.getSourceUserListId();
		String moduleName = userListDTO.getModuleName();
		UserList existingUserList = userListDAO.findByPrimaryKey(sourceUserListId, new FindLevel[] { FindLevel.USERS });
		Set<UserDTO> users = existingUserList.getUserDTOs();
		userService.removeNullEmailUserDTOs(users);
		for (UserDTO exuser : users) {
			if (sourceUserListId != null) {
				exuser.setLegalBasis(
						userListDAO.listLegalBasisByContactListIdAndUserId(sourceUserListId, exuser.getId()));
				exuser.setUserListId(userListDTO.getId());
				if (XamplifyUtils.CONTACTS.equalsIgnoreCase(moduleName)) {
					Integer contactStatusId = flexiFieldDao.findContactStatusIdByUserIdAndUserListId(exuser.getId(),
							sourceUserListId);
					exuser.setContactStatusId(contactStatusId);
					List<FlexiFieldRequestDTO> flexiFields = flexiFieldDao
							.findFlexiFieldsBySelectedUserIdAndUserListId(sourceUserListId, exuser.getId());
					exuser.setFlexiFields(flexiFields);
				}
			}
		}
		return users;
	}

	public void addPartnerToList(Set<UserDTO> partners, UserList createdList, Integer companyId,
			UserListDTO userListDTO) {
		if (userListDTO.isCopyList()) {
			partners = getCopyListUsers(userListDTO);
		}
		partnershipServiceHelper.addPartnerToList(partners, createdList, companyId);
	}

	public void sendLeadAddedOrUpdatedEmailToPartner(LeadDto leadDto, boolean isUpdateLead) {
		Integer userId = leadDto.getUserId();
		Integer companyId = userDAO.getCompanyIdByUserId(userId);
		if (XamplifyUtils.isValidInteger(companyId) && !companyId.equals(leadDto.getCreatedForCompanyId())) {
			UserDetailsUtilDTO userDetails = leadDAO.fetchFullNameAndEmailIdByUserId(userId);
			LeadDto mergeTagsDto = leadDAO.fetchMergeTagsDataForPartnerMailNotification(leadDto.getId());
			mergeTagsDto.setLeadComment(leadDto.getLeadComment());
			mergeTagsDto.setUserId(leadDto.getUserId());
			mergeTagsDto.setCreatedForCompanyId(leadDto.getCreatedForCompanyId());
			mergeTagsDto.setSfCustomFieldsDataDto(leadDto.getSfCustomFieldsDataDto());
			thymeleafService.sendLeadAddedOrUpdatedEmailToPartner(userDetails, mergeTagsDto, isUpdateLead);
		}
	}

	public void sendDealAddedOrUpdatedEmailToPartner(DealDto dealDto, boolean isUpdateDeal) {
		Integer userId = dealDto.getUserId();
		Integer companyId = userDAO.getCompanyIdByUserId(userId);
		if (XamplifyUtils.isValidInteger(companyId) && !companyId.equals(dealDto.getCreatedForCompanyId())) {
			UserDetailsUtilDTO userDetails = leadDAO.fetchFullNameAndEmailIdByUserId(userId);
			DealDto mergeTagsDto = dealDAO.fetchMergeTagsDataForPartnerMailNotification(dealDto.getId());
			mergeTagsDto.setDealComment(dealDto.getDealComment());
			mergeTagsDto.setUserId(dealDto.getUserId());
			mergeTagsDto.setCreatedForCompanyId(dealDto.getCreatedForCompanyId());
			thymeleafService.sendDealAddedOrUpdatedEmailToPartner(userDetails, mergeTagsDto, isUpdateDeal);
		}
	}

	/*** XNFR-564 ****/
	public void shareLMSAndPlaybooksWithNewTeamMembers(Integer sourceCompanyId, Integer destinationCompanyId) {
		List<UserDTO> sourceCompanyUsers = userDAO.findAllUserNamesByCompanyId(sourceCompanyId);
		Integer destinationCompanyPrimaryAdminId = teamDao.findPrimaryAdminIdByCompanyId(destinationCompanyId);
		List<TeamMemberDTO> teamMemberDTOs = new ArrayList<>();
		for (UserDTO sourceCompanyUser : sourceCompanyUsers) {
			TeamMemberDTO teamMemberDTO = new TeamMemberDTO();
			teamMemberDTO.setEmailId(sourceCompanyUser.getEmailId());
			teamMemberDTO.setFirstName(sourceCompanyUser.getFirstName());
			teamMemberDTO.setLastName(sourceCompanyUser.getLastName());
			List<TeamMemberGroupDTO> defaultTeamMemerGroups = teamMemberGroupDao
					.findDefaultGroupsByCompanyId(destinationCompanyId);
			Integer teamMemberGroupId = defaultTeamMemerGroups.get(0).getId();
			teamMemberDTO.setTeamMemberGroupId(teamMemberGroupId);
			teamMemberDTO.setUserId(destinationCompanyPrimaryAdminId);
			teamMemberDTOs.add(teamMemberDTO);
		}
		asyncComponent.publishLMSToNewTeamMembers(teamMemberDTOs, destinationCompanyPrimaryAdminId);
	}

	/** XNFR-891 **/
	public void updatePartnerModulesAccess(List<AddPartnerResponseDTO> responseDTOList, Set<UserDTO> users) {
		if (XamplifyUtils.isNotEmptyList(responseDTOList)) {
			for (AddPartnerResponseDTO partnerResponseDTO : responseDTOList) {
				if (XamplifyUtils.isNotEmptySet(partnerResponseDTO.getModuleDTOs())) {
					List<Integer> restrictedModuleIds = partnerResponseDTO.getModuleDTOs().stream()
							.filter(module -> !module.isPartnerAccessModule()).map(ModuleCustomDTO::getModuleId)
							.collect(Collectors.toList());
					if (XamplifyUtils.isNotEmptyList(restrictedModuleIds)) {
						moduleDao.updateModulesAccess(restrictedModuleIds, partnerResponseDTO.getPartnershipId(),
								false);
					}
				}
			}

		}
	}

	public void updatePartnershipCompanyIdByPartnerId(Integer partnerId, Integer partnerCompanyId) {
		if (XamplifyUtils.isValidInteger(partnerId) && XamplifyUtils.isValidInteger(partnerCompanyId)) {
			partnershipDao.updatePartnershipCompanyIdByPartnerId(partnerId, partnerCompanyId);
		}
	}

	/** XNFR-885 **/
	public void handleWhiteLabeledAssetsAfterReApproval(List<Integer> whiteLabeledParentDamIds, Integer companyId,
			Integer loggedInUserId) {
		if (XamplifyUtils.isNotEmptyList(whiteLabeledParentDamIds) && XamplifyUtils.isValidInteger(loggedInUserId)
				&& XamplifyUtils.isValidInteger(companyId)) {
			damDao.handleWhiteLabeledAssetsAfterReApproval(whiteLabeledParentDamIds, companyId, loggedInUserId);
		}
	}

	/** XNFR-911 **/
	public void sendLeadFieldUpdatedNotification(Map<User, List<LeadDTO>> map) {
		thymeleafService.sendLeadFieldUpdatedNotification(map);
	}

	public void sendPartnerSignatureRemainderEmailNotification(Pagination pagination) {
		List<UserDTO> partners = pagination.getPartners();
		User user = userService.loadUser(
				Arrays.asList(new Criteria(USER_ID, OPERATION_NAME.eq, pagination.getUserId())),
				new FindLevel[] { FindLevel.COMPANY_PROFILE });
		CompanyProfile companyProfile = genericDAO.get(CompanyProfile.class,
				userDAO.getCompanyIdByUserId(pagination.getUserId()));

		String assetName = damDao.getAssetDetailsById(pagination.getDamId()).getAssetName();

		for (UserDTO partner : partners) {
			getEmailTemplateAndSendEmail(companyProfile.getCompanyName(), assetName, partner, user, companyProfile,
					DefaultEmailTemplateType.PARTNER_SIGNATURE_PENDING);
		}

	}

	public void uploadDesignedPdfToAws(Integer damId) {
		DamPreviewDTO damPreviewDTO = damDao.previewAssetById(damId);
		if (damPreviewDTO.isBeeTemplate() && !XamplifyUtils.isValidString(damPreviewDTO.getAssetPath())) {
			String htmlBody = damPreviewDTO.getHtmlBody();
			String assetName = XamplifyUtils.convertToLowerCaseAndExcludeSpace(damPreviewDTO.getName());
			assetName = assetName.replace("/", "_").replaceAll(regex, "-");
			String updatedAssetName = assetName + ".pdf";
			String localFilePath = contentPdfPath + sep + damPreviewDTO.getAlias() + sep
					+ new SimpleDateFormat("ddMMyyyy").format(new Date()) + sep + System.currentTimeMillis();

			fileUtil.createDirectory(localFilePath);
			String updatedLocalFilePath = localFilePath + sep + updatedAssetName;
			try (OutputStream outputStream = new FileOutputStream(updatedLocalFilePath)) {
				HtmlConverter.convertToPdf(htmlBody, outputStream);
			} catch (IOException e) {
				e.printStackTrace();
				String debugMessage = "Failed to convert HTML to PDF. Asset ID: " + damId;
				logger.debug(debugMessage);
				return;
			}

			Integer companyId = userDAO.getCompanyIdByUserId(damPreviewDTO.getCreatedBy());
			String awsFilePath = amazonEnvFolder + "images" + sep + "bee-" + companyId + sep + updatedAssetName;
			amazonWebService.uploadDataToAws(awsFilePath, updatedLocalFilePath);
			String updatedAwsFilePath = amazonBaseUrl + amazonBucketName + sep + awsFilePath;
			damDao.updateAssetPath(damId, updatedAwsFilePath);
			String debugMessage = "Asset successfully saved to AWS. Asset ID: " + damId;
			logger.debug(debugMessage);
		}
	}

	// XNFR-921
	public void savePlaybookWorkFlows(List<WorkflowRequestDTO> workflowRequestDTOs, Integer playbookId,
			List<Integer> savePlaybookWorkFlows, String playbookName) {
		workflowService.savePlaybookWorkFlows(workflowRequestDTOs, playbookId, savePlaybookWorkFlows, playbookName);
	}

	// XNFR-993
	public void savePlaybookWorkflowEmailHistory(EmailBuilder builder) {
		if (builder.getWorkflowId() != null) {
			WorkflowEmailSentLog history = new WorkflowEmailSentLog();
			history.setWorkflowId(builder.getWorkflowId());
			history.setUserId(builder.getUserId());
			history.setSentTime(new Date());
			history.setStatusCode(builder.getStatusCode());
			genericDAO.save(history);
		}

	}

	public void saveCompanyDomainColors(CompanyProfile companyProfile) {
		utilService.saveDomainColors(companyProfile); /*** XNFR-1013 ***/
	}

}
