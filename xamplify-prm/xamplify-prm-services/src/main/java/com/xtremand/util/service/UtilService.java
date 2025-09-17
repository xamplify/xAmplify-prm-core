package com.xtremand.util.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.hibernate.HibernateException;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.xtremand.aws.AmazonWebModel;
import com.xtremand.aws.AmazonWebService;
import com.xtremand.campaign.bom.DashboardTypeEnum;
import com.xtremand.campaign.bom.ModuleAccess;
import com.xtremand.category.dao.CategoryDao;
import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.Criteria;
import com.xtremand.common.bom.Criteria.OPERATION_NAME;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.common.bom.Module;
import com.xtremand.common.bom.ModuleCustom;
import com.xtremand.common.bom.Pagination;
import com.xtremand.company.bom.Company;
import com.xtremand.company.bom.DomainColor;
import com.xtremand.company.dao.CompanyDAO;
import com.xtremand.company.dao.CompanyProfileDao;
import com.xtremand.company.dto.DomainColorDTO;
import com.xtremand.custom.css.bom.CustomModule;
import com.xtremand.custom.css.bom.CustomSkin;
import com.xtremand.custom.css.bom.Theme;
import com.xtremand.custom.link.bom.CustomLink;
import com.xtremand.custom.link.bom.CustomLinkType;
import com.xtremand.custom.link.dao.CustomLinkDao;
import com.xtremand.dam.bom.DamPartnerGroupMapping;
import com.xtremand.dam.bom.DamPartnerGroupUserMapping;
import com.xtremand.dam.dao.DamDao;
import com.xtremand.dam.dto.DamDownloadDTO;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.dashboard.buttons.bom.DashboardButton;
import com.xtremand.deal.bom.Deal;
import com.xtremand.deal.bom.DealFail;
import com.xtremand.domain.bom.Domain;
import com.xtremand.domain.bom.DomainModuleNameType;
import com.xtremand.domain.dao.DomainDao;
import com.xtremand.domain.service.DomainService;
import com.xtremand.exclude.bom.ExcludedDomain;
import com.xtremand.form.bom.Form;
import com.xtremand.form.bom.FormDefaultFieldTypeEnum;
import com.xtremand.form.bom.FormLabel;
import com.xtremand.form.bom.FormLabelChoice;
import com.xtremand.form.bom.FormLookUpDefaultFieldTypeEnum;
import com.xtremand.form.bom.FormSubTypeEnum;
import com.xtremand.form.bom.FormTypeEnum;
import com.xtremand.form.dao.FormDao;
import com.xtremand.form.dto.FormChoiceDTO;
import com.xtremand.form.dto.FormLabelDTO;
import com.xtremand.form.dto.FormLabelDTORow;
import com.xtremand.formbeans.EmailTemplateDTO;
import com.xtremand.formbeans.RoleDTO;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.gdpr.setting.bom.GdprSettingView;
import com.xtremand.gdpr.setting.dao.GdprSettingDao;
import com.xtremand.integration.bom.Integration;
import com.xtremand.integration.bom.Integration.IntegrationType;
import com.xtremand.integration.dao.IntegrationDao;
import com.xtremand.integration.dto.IntegrationDTO;
import com.xtremand.lead.bom.Lead;
import com.xtremand.lead.bom.LeadFail;
import com.xtremand.lead.bom.Pipeline;
import com.xtremand.lead.bom.PipelineStage;
import com.xtremand.lead.bom.PipelineType;
import com.xtremand.lms.bom.LearningTrackType;
import com.xtremand.lms.dao.LMSDAO;
import com.xtremand.mail.bom.EmailTemplate;
import com.xtremand.mail.bom.EmailTemplateType;
import com.xtremand.mail.service.EmailConstants;
import com.xtremand.mail.service.MailService;
import com.xtremand.module.dao.ModuleDao;
import com.xtremand.partnership.bom.PartnerCompanyDomain;
import com.xtremand.partnership.bom.Partnership;
import com.xtremand.partnership.bom.PartnershipDTO;
import com.xtremand.partnership.dao.PartnershipDAO;
import com.xtremand.partnership.service.PartnershipService;
import com.xtremand.pipeline.dao.PipelineDAO;
import com.xtremand.salesforce.bom.SfCustomFieldsData;
import com.xtremand.salesforce.dto.OpportunityFormFieldsDTO;
import com.xtremand.sf.cf.data.dao.SfCustomFormDataDAO;
import com.xtremand.signup.dto.SignUpRequestDTO;
import com.xtremand.social.formbeans.MyMergeTagsInfo;
import com.xtremand.team.dao.TeamDao;
import com.xtremand.team.member.dto.RoleDisplayDTO;
import com.xtremand.team.member.dto.TeamMemberDTO;
import com.xtremand.team.member.dto.TeamMemberGroupDTO;
import com.xtremand.team.member.dto.TeamMemberModuleDTO;
import com.xtremand.team.member.group.bom.TeamMemberGroup;
import com.xtremand.team.member.group.bom.TeamMemberGroupRoleMapping;
import com.xtremand.team.member.group.dao.TeamMemberGroupDao;
import com.xtremand.team.member.group.service.TeamMemberGroupService;
import com.xtremand.team.service.TeamService;
import com.xtremand.user.bom.LegalBasis;
import com.xtremand.user.bom.ModulesDisplayType;
import com.xtremand.user.bom.Role;
import com.xtremand.user.bom.ShareListPartner;
import com.xtremand.user.bom.ShareListPartnerMapping;
import com.xtremand.user.bom.TeamMember;
import com.xtremand.user.bom.User;
import com.xtremand.user.bom.User.UserStatus;
import com.xtremand.user.bom.UserList;
import com.xtremand.user.bom.UserList.SocialNetwork;
import com.xtremand.user.bom.UserList.TYPE;
import com.xtremand.user.bom.UserUserList;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.user.exception.TeamMemberDataAccessException;
import com.xtremand.user.service.UserService;
import com.xtremand.userlist.dao.UserListDAO;
import com.xtremand.userlist.service.UserListService;
import com.xtremand.util.BadRequestException;
import com.xtremand.util.DateUtils;
import com.xtremand.util.ExportUtil;
import com.xtremand.util.FileUtil;
import com.xtremand.util.GenerateRandomPassword;
import com.xtremand.util.XamplifyUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.HibernateSQLQueryResultUtilDao;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.dto.AccessDetailsDTO;
import com.xtremand.util.dto.AccountDetailsRequestDTO;
import com.xtremand.util.dto.ActiveThreadsDTO;
import com.xtremand.util.dto.AngularUrl;
import com.xtremand.util.dto.CompanyAndRolesDTO;
import com.xtremand.util.dto.CompanyDTO;
import com.xtremand.util.dto.CompanyDetailsDTO;
import com.xtremand.util.dto.DeletedPartnerDTO;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.LeftSideNavigationBarItem;
import com.xtremand.util.dto.ModuleCustomDTO;
import com.xtremand.util.dto.ModulesEmailNotification;
import com.xtremand.util.dto.Pageable;
import com.xtremand.util.dto.PartnerJourneyRequestDTO;
import com.xtremand.util.dto.QueryParameterDTO;
import com.xtremand.util.dto.TeamMemberAnalyticsRequestDTO;
import com.xtremand.util.dto.UserListOperationsAsyncDTO;
import com.xtremand.util.dto.UserUserListDTO;
import com.xtremand.util.dto.XamplifyConstants;
import com.xtremand.util.dto.XamplifyUtility;
import com.xtremand.vanity.url.dao.VanityURLDao;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;

@Service
@Transactional
public class UtilService {

	private static final String COMPANY_ID = "companyId";

	private static final Logger logger = LoggerFactory.getLogger(UtilService.class);

	private static Header prettyPrintHeader = new BasicHeader("X-PrettyPrint", "1");

	private static final String FIRST_NAME = "<<firstName>>";

	private static final String DEFAULT_FIRST_NAME_MERGE_TAG = "{{firstName}}";

	private static final String LAST_NAME = "<<lastName>>";

	private static final String DEFAULT_LAST_NAME_MERGE_TAG = "{{lastName}}";

	private static final String EMAIL_ID = "<<emailId>>";

	private static final String DEFAULT_EMAIL_ID_MERGE_TAG = "{{emailId}}";

	private static final String FULL_NAME = "<<fullName>>";

	private static final String DEFAULT_FULL_NAME_MERGE_TAG = "{{fullName}}";

	private static final String COMPANY_NAME = "<<companyName>>";

	private static final String DEFAULT_COMPANY_NAME_MERGE_TAG = "{{companyName}}";

	private static final String ADDRESS = "<<address>>";

	private static final String DEFAULT_ADDRESS_MERGE_TAG = "{{address}}";

	private static final String ZIPCODE = "<<zipcode>>";

	private static final String DEFAULT_ZIPCODE_MERGE_TAG = "{{zipcode}}";

	private static final String CITY = "<<city>>";

	private static final String DEFAULT_CITY_MERGE_TAG = "{{city}}";

	private static final String STATE = "<<state>>";

	private static final String DEFAULT_STATE_MERGE_TAG = "{{state}}";

	private static final String COUNTRY = "<<country>>";

	private static final String DEFAULT_COUNTRY_MERGE_TAG = "{{country}}";

	private static final String MOBILE_NUMBER = "<<mobileNumber>>";

	private static final String DEFAULT_MOBILE_NUMBER_MERGE_TAG = "{{mobileNumber}}";

	private static final String SENDER_FIRST_NAME = "<<senderFirstName>>";

	private static final String DEFAULT_SENDER_FIRST_NAME_MERGE_TAG = "{{senderFirstName}}";

	private static final String SENDER_MIDDLE_NAME_TEXT = "senderMiddleName";

	private static final String SENDER_MIDDLE_NAME = "<<" + SENDER_MIDDLE_NAME_TEXT + ">>";

	private static final String DEFAULT_SENDER_MIDDLE_NAME_MERGE_TAG = "{{" + SENDER_MIDDLE_NAME_TEXT + "}}";

	private static final String SENDER_LAST_NAME = "<<senderLastName>>";

	private static final String DEFAULT_SENDER_LAST_NAME_MERGE_TAG = "{{senderLastName}}";

	private static final String SENDER_FULL_NAME = "<<senderFullName>>";

	private static final String DEFAULT_SENDER_FULL_NAME_MERGE_TAG = "{{senderFullName}}";

	private static final String SENDER_JOB_TITLE_TEXT = "senderJobTitle";

	private static final String SENDER_JOB_TITLE = "<<" + SENDER_JOB_TITLE_TEXT + ">>";

	private static final String DEFAULT_SENDER_JOB_TITLE_MERGE_TAG = "{{" + SENDER_JOB_TITLE_TEXT + "}}";

	/**** Deleted Merge Tag ****/
	private static final String SENDER_TITLE_TEXT = "senderTitle";

	private static final String SENDER_TITLE = "<<" + SENDER_TITLE_TEXT + ">>";

	private static final String DEFAULT_SENDER_TITLE_MERGE_TAG = "{{" + SENDER_TITLE_TEXT + "}}";

	private static final String SENDER_EMAIL_ID = "<<senderEmailId>>";

	private static final String DEFAULT_SENDER_EMAIL_ID_MERGE_TAG = "{{senderEmailId}}";

	private static final String SENDER_CONTACT_NUMBER = "<<senderContactNumber>>";

	private static final String DEFAULT_SENDER_CONTACT_NUMBER_MERGE_TAG = "{{senderContactNumber}}";

	private static final String SENDER_COMPANY_URL = "<<senderCompanyUrl>>";

	private static final String DEFAULT_SENDER_COMPANY_URL_MERGE_TAG = "{{senderCompanyUrl}}";

	/******* XNFR-281 ********/
	private static final String SENDER_COMPANY_INSTAGRAM_URL_TEXT = "senderCompanyInstagramUrl";

	private static final String SENDER_COMPANY_INSTAGRAM_URL = "<<" + SENDER_COMPANY_INSTAGRAM_URL_TEXT + ">>";

	private static final String DEFAULT_SENDER_COMPANY_INSTAGRAM_URL_MERGE_TAG = "{{"
			+ SENDER_COMPANY_INSTAGRAM_URL_TEXT + "}}";

	private static final String SENDER_COMPANY_TWITTER_URL_TEXT = "senderCompanyTwitterUrl";

	private static final String SENDER_COMPANY_TWITTER_URL = "<<" + SENDER_COMPANY_TWITTER_URL_TEXT + ">>";

	private static final String DEFAULT_SENDER_COMPANY_TWITTER_URL_MERGE_TAG = "{{" + SENDER_COMPANY_TWITTER_URL_TEXT
			+ "}}";

	/******* XNFR-281 ********/

	private static final String SENDER_COMPANY_ADDRESS_TEXT = "senderCompanyAddress";

	private static final String SENDER_COMPANY_ADDRESS = "<<" + SENDER_COMPANY_ADDRESS_TEXT + ">>";

	private static final String DEFAULT_SENDER_COMPANY_ADDRESS_MERGE_TAG = "{{" + SENDER_COMPANY_ADDRESS_TEXT + "}}";

	private static final String SENDER_COMPANY_CONTACT_NUMBER = "<<senderCompanyContactNumber>>";

	private static final String DEFAULT_SENDER_COMPANY_CONTACT_NUMBER_MERGE_TAG = "{{senderCompanyContactNumber}}";

	private static final String SENDER_COMPANY = "<<senderCompany>>";

	private static final String DEFAULT_SENDER_COMPANY_MERGE_TAG = "{{senderCompany}}";

	private static final String SENDER_PRIVACY_POLICY = "<<senderPrivacyPolicy>>";

	private static final String DEFAULT_SENDER_PRIVACY_POLICY_MERGE_TAG = "{{senderPrivacyPolicy}}";

	private static final String SENDER_EVENT_URL = "<<senderEventUrl>>";

	private static final String DEFAULT_SENDER_EVENT_URL_MERGE_TAG = "{{senderEventUrl}}";

	private static final String PARTNER_ABOUT_US = "<<partnerAboutUs>>";

	private static final String DEFAULT_PARTNER_ABOUT_US_MERGE_TAG = "{{partnerAboutUs}}";

	private static final String SENDER_ABOUT_US = "<<senderAboutUs>>";

	private static final String DEFAULT_SENDER_ABOUT_US_MERGE_TAG = "{{senderAboutUs}}";

	private static final String PARTNERS = "Partners";

	private static final String MODULE_ADDED_PREFIX = " module has been added to ";

	private static final String HOST = "http://localhost:8080/";

	@Value("${prod.host}")
	private String productionHost;

	@Value("${dev.host}")
	private String devHost;

	@Value("${your.logo}")
	private String yourLogo;

	@Value("${xamplify-rest-client-id}")
	private String xamplifyRestClientId;

	@Value("${show.shortenUrlAlias}")
	private String showShortenUrlAlias;

	@Value("${save.workflow.analytics}")
	private String saveWorkflowAnalytics;

	@Autowired
	private UserService userService;

	@Autowired
	private UserListService userListService;

	@Autowired
	private UserListDAO userListDao;

	@Autowired
	private GenericDAO genericDAO;

	@Autowired
	private GdprSettingDao gdprSettingDao;

	@Autowired
	private UserDAO userDao;

	@Autowired
	private MailService mailService;

	@Autowired
	private XamplifyUtil xamplifyUtil;

	@Autowired
	private UtilDao utilDao;

	@Autowired
	private PartnershipDAO partnershipDao;

	@Autowired
	private AmazonWebService amazonWebService;

	@Autowired
	private HttpServletResponse httpServletResponse;

	@Autowired
	private FormDao formDao;

	@Autowired
	private ModuleDao moduleDao;

	@Autowired
	private IntegrationDao integrationDao;

	/*** XNFR-427 ***/
	@Autowired
	private CompanyDAO companyDAO;

	@Autowired
	private DomainDao domainDao;

	@Autowired
	private CustomLinkDao customLinkDao;

	@Autowired
	private VanityURLDao vanityUrlDao;

	@Resource(name = "tokenStore")
	TokenStore tokenStore;

	@Resource(name = "tokenServices")
	ConsumerTokenServices tokenServices;

	@Value("${dev.cobranding.image}")
	String devCoBrandingImage;

	@Value("${prod.cobranding.image}")
	String prodCoBrandingImage;

	@Value("${release.cobranding.image}")
	String releaseCoBrandingImage;

	@Value("${company.logo.url}")
	String companyLogoUrl;

	@Value("${server_path}")
	String serverPath;

	@Value("${rsvp.yes}")
	String rsvpYes;

	@Value("${rsvp.no}")
	String rsvpNo;

	@Value("${rsvp.maybe}")
	String rsvpMaybe;

	@Value("${spring.profiles.active}")
	private String profiles;

	@Value("${web_url}")
	private String webUrl;

	@Value("${images.folder}")
	String imagesFolderPath;

	@Value("${role.orgadmin}")
	private String orgAdminRole;

	@Value("${role.vendor}")
	private String vendorRole;

	@Value("${role.partner}")
	private String partnerRole;

	@Value("${role.teamMember}")
	private String teamMemberRole;

	@Value("${role.orgadmin.partner}")
	private String orgAdminAndPartnerRole;

	@Value("${role.vendor.partner}")
	private String vendorAndPartnerRole;

	@Value("${role.user}")
	private String userRole;

	@Value("${role.distributor}")
	private String distributorRole;

	@Value("${role.vendor.tier}")
	private String vendorTierRole;

	@Value("${role.vendor.tier.partner}")
	private String vendorTierAndPartnerRole;

	@Value("${role.marketing}")
	private String marketingRole;

	@Value("${role.marketing.partner}")
	private String marketingAndPartnerRole;

	@Value("${role.prm}")
	private String prmRole;

	@Value("${role.prm.partner}")
	private String prmAndPartnerRole;

	@Value("${replace.there}")
	private String replaceThere;

	@Value("${separator}")
	String sep;

	@Value("${specialCharacters}")
	String regex;

	@Value("${addCompanyProfileUrl}")
	private String addCompanyProfileUrl;

	@Value("${editCompanyProfileUrl}")
	private String editCompanyProfileUrl;

	@Value("${myProfileUrl}")
	private String myProfileUrl;

	@Value("${defaultDashboardUrl}")
	private String defaultDashboardUrl;

	@Value("${homeAndDashboardUrl}")
	private String homeAndDashboardUrl;

	@Value("${welcomePageUrl}")
	private String welcomePageUrl;

	@Value("${adminReportUrl}")
	private String adminReportUrl;

	@Value("${adminCompanyCreationUrl}")
	private String adminCompanyCreationUrl;

	@Value("${moduleAccessUrl}")
	private String moduleAccessUrl;

	@Value("${designBaseUrl}")
	private String designBaseUrl;

	@Value("${emailTemplatesBaseUrl}")
	private String emailTemplatesBaseUrl;

	@Value("${formsBaseUrl}")
	private String formsBaseUrl;

	@Value("${pagesBaseUrl}")
	private String pagesBaseUrl;

	@Value("${partnerPagesUrl}")
	private String partnerPagesUrl;

	@Value("${partnerFormsUrl}")
	private String partnerFormsUrl;

	@Value("${campaignBaseUrl}")
	private String campaignBaseUrl;

	@Value("${partnerCampaignsUrl}")
	private String partnerCampaignsUrl;

	@Value("${redistributeCampaignUrl}")
	private String redistributeCampaignUrl;

	@Value("${redistributeEventBaseUrl}")
	private String redistributeEventBaseUrl;

	@Value("${redistributeManageBaseUrl}")
	private String redistributeManageBaseUrl;

	@Value("${teamBaseUrl}")
	private String teamBaseUrl;

	@Value("${mdfBaseUrl}")
	private String mdfBaseUrl;

	@Value("${mdfRequestPartnerUrl}")
	private String mdfRequestPartnerUrl;

	@Value("${mdfCreateRequestPartnerUrl}")
	private String mdfCreateRequestPartnerUrl;

	@Value("${mdfTimeLinePartnerUrl}")
	private String mdfTimeLinePartnerUrl;

	@Value("${damBaseUrl}")
	private String damBaseUrl;

	@Value("${damPartnerSharedUrl}")
	private String damPartnerSharedUrl;

	@Value("${damPartnerAnalyticsUrl}")
	private String damPartnerAnalyticsUrl;

	@Value("${content.pdf.path}")
	private String contentPdfPath;

	@Value("${content.html.path}")
	private String contentHtmlPath;

	@Value("${mergetag.unsubscribeLink}")
	private String unsubscribeLinkMergeTag;

	@Value("${default.unsubscribeLink.mergeTag}")
	private String defaultUnsubscribeLinkMergeTag;

	@Value("${active.partner.list.name}")
	private String activePartnerListName;

	@Value("${inactive.partner.list.name}")
	private String inActivePartnerListName;

	@Value("${logMessage.seperator}")
	private String logMessageSeperator;

	/* XNFR-215 */
	@Value("#{'${crms.without.lead.pipelines}'.split(',')}")
	private List<String> crmsWithoutLeadPipelines;

	@Value("#{'${crms.without.deal.pipelines}'.split(',')}")
	private List<String> crmsWithoutDealPipelines;
	/* XNFR-215 */

	/* XNFR-276 */
	@Value("#{${moduleDisplayIndexMap}}")
	private Map<Integer, Integer> moduleDisplayIndexMap;

	@Value("${system.notification.message}")
	private String systemNotificationPrefixMessage;

	@Value("${team.member.errorMessage}")
	private String teamMemberErrorMessage;

	@Value("${team.members.errorMessage}")
	private String teamMembersErrorMessage;

	@Value("${company.domain.regex}")
	private String companyDoaminRegex;

	@Value("${partnership.established.successMessage}")
	private String partnershipEstablishedSuccesMessage;

	@Value("${added.as.teamMember.successMessage}")
	private String addedAsTeamMemberSucesssMessage;

	@Autowired
	private FileUtil fileUtil;

	@Autowired
	private ExportUtil exportUtil;

	@Autowired
	private DamDao damDao;

	@Autowired
	private TeamDao teamDao;

	@Autowired
	private TeamMemberGroupDao teamMemberGroupDao;

	@Autowired
	private EmailValidatorService emailValidatorService;

	@Autowired
	private CategoryDao categoryDao;

	@Autowired
	private SfCustomFormDataDAO sfCustomFormDataDAO;

	/***** XNFR-326 *****/
	@Autowired
	private CompanyProfileDao companyProfileDao;

	@Autowired
	private HibernateSQLQueryResultUtilDao hibernateSQLQueryResultUtilDao;

	@Autowired
	private LMSDAO lmsDao;

	@Autowired
	private PipelineDAO pipelineDAO;

	@Autowired
	private PartnershipService partnershipService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private DomainService domainService;

	@Autowired
	private TeamService teamService;

	@Autowired
	private TeamMemberGroupService teamMemberGroupService;

	private static final String DUMMY_CO_BRANDING_URL = "https://dummycobrandingurl.com";

	private static final String DUMMY_URL = "https://dummyurl.com";

	private static final String REGULAR_EMAIL_TEMPLATE = "Email";
	private static final String REGULAR_CO_BRANDING_EMAIL_TEMPLATE = "Email Co-Branding";
	private static final String VIDEO_EMAIL_TEMPLATE = "Video";
	private static final String VIDEO_CO_BRANDING_EMAIL_TEMPLATE = "Video Co-Branding";
	private static final String EVENT_EMAIL_TEMPLATE = "Event";
	private static final String EVENT_CO_BRANDING_EMAIL_TEMPLATE = "Event Co-Branding";

	private static final String SURVEY = "Survey";
	private static final String SURVEY_CO_BRANDING = "Survey Co-Branding";

	private static final String PARTNER = "Partner";
	private static final String PRM = "Prm";
	private static final String IMAGE_PATH_CURRENT_DATE = new SimpleDateFormat("ddMMyyyy").format(new Date());

	public Integer getSuperiorRoleId(Integer loggedInUserId) {
		RoleDTO roleDto = userService.getSuperiorRole(loggedInUserId);
		if (roleDto.getTotalRoles() > 0) {
			return roleDto.getSuperiorId();
		} else {
			return loggedInUserId;
		}
	}

	public List<Integer> findCommonElements(List<Integer> list1, List<Integer> list2) {
		list1.retainAll(list2);
		return list1;
	}

	public Map<String, Object> validatePartnerOrganizationByEmailIds(List<String> emailIds, Integer loggedInUserId,
			Integer userListId) {
		String key = "statusCode";
		HashMap<String, Object> map = new HashMap<>();
		List<Integer> existingPartnerCompanyIds = userListService.partnerCompanyIds(loggedInUserId, userListId);
		List<UserDTO> userDtos = userService.getCompanyIdsByEmailIds(emailIds);
		List<Integer> companyIds = userDtos.stream().map(UserDTO::getCompanyId).collect(Collectors.toList());
		if (!existingPartnerCompanyIds.isEmpty()) {
			List<Integer> duplicatePartnerCompanyIds = findCommonElements(existingPartnerCompanyIds, companyIds);
			if (duplicatePartnerCompanyIds.isEmpty()) {
				map.put(key, 200);
			} else {
				addValidationResponse(key, map, userDtos, duplicatePartnerCompanyIds,
						"Following Organization(s) already added as partner(s)");
			}
		} else {
			/**************
			 * Validate If Duplicate Emailds Belong To Same Organization
			 *****************/
			Set<Integer> duplicateCompanyIds = companyIds.stream().filter(i -> Collections.frequency(companyIds, i) > 1)
					.collect(Collectors.toSet());
			List<Integer> duplicateCompanyIdsList = duplicateCompanyIds.stream().collect(Collectors.toList());
			if (duplicateCompanyIds.isEmpty()) {
				map.put(key, 200);
			} else {
				addValidationResponse(key, map, userDtos, duplicateCompanyIdsList,
						"Following email address(es)'s belongs to same organization");
			}
		}
		return map;
	}

	private void addValidationResponse(String key, HashMap<String, Object> map, List<UserDTO> userDtos,
			List<Integer> duplicatePartnerCompanyIds, String message) {
		List<UserDTO> filtertedUserDtos = userDtos.stream()
				.filter(userDto -> duplicatePartnerCompanyIds.indexOf(userDto.getCompanyId()) > -1)
				.collect(Collectors.toList());
		map.put("errorMessage", message);
		map.put("emailAddresses", filtertedUserDtos.stream().map(UserDTO::getEmailId).collect(Collectors.toList()));
		map.put(key, 409);
	}

	public boolean unsubscribeStatus(Integer companyId) {
		return findUnsubscribeStatusByCompanyId(companyId);
	}

	public String addParametersToUnsubscribeLink(String body, String unsubscribeUrl, String webUrl, String userAlias,
			Integer companyId) {
		return body.replaceAll(unsubscribeUrl, getUnsubscribeLink(webUrl, userAlias, companyId));
	}

	public String getUnsubscribeLink(String webUrl, String userAlias, Integer companyId) {
		return webUrl + "log/unsubscribe-user?userAlias=" + userAlias + "&companyId=" + companyId;
	}

	public boolean hasAnyAdminRole(List<String> roles) {
		return roles.indexOf(Role.PRM_ROLE.getRoleName()) > -1;

	}

	public String replaceSenderMergeTagsAndPartnerAboutUsMergeTag(String updatedTemplateBody, User loggedInUser,
			UserDTO sendor, boolean skipPartnerAboutUs) {
		if (StringUtils.hasText(updatedTemplateBody)) {
			MyMergeTagsInfo myMergeTagInfo = XamplifyUtils.getMyMergeTagsData(loggedInUser);
			if (sendor != null) {
				updatedTemplateBody = XamplifyUtils.replaceSenderAndPartnerMergeTags(updatedTemplateBody,
						myMergeTagInfo, sendor);
			} else {
				updatedTemplateBody = XamplifyUtils.replaceSenderMergeTags(updatedTemplateBody, myMergeTagInfo);
			}
			updatedTemplateBody = replacePartnerAboutUsMergeTag(updatedTemplateBody, skipPartnerAboutUs,
					myMergeTagInfo);
			return updatedTemplateBody;
		} else {
			return "";
		}

	}

	private String replacePartnerAboutUsMergeTag(String updatedTemplateBody, boolean skipPartnerAboutUs,
			MyMergeTagsInfo myMergeTagInfo) {
		if (skipPartnerAboutUs) {
			updatedTemplateBody = updatedTemplateBody.replace(PARTNER_ABOUT_US, DEFAULT_PARTNER_ABOUT_US_MERGE_TAG);
		} else {
			updatedTemplateBody = updatedTemplateBody.replace(PARTNER_ABOUT_US,
					XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getAboutUs()));
		}
		return updatedTemplateBody;
	}

	public String replaceMergeTagPrefixAndSuffixWithDefaultBraces(String templateHtmlBody) {
		/********** Receiver Merge Tags ******************/
		templateHtmlBody = replaceReceiverMergeTagsWithDefaultMergeTags(templateHtmlBody);
		/********** Sender Merge Tags ******************/
		templateHtmlBody = templateHtmlBody.replace(SENDER_FIRST_NAME, DEFAULT_SENDER_FIRST_NAME_MERGE_TAG);
		templateHtmlBody = templateHtmlBody.replace(SENDER_MIDDLE_NAME, DEFAULT_SENDER_MIDDLE_NAME_MERGE_TAG);
		templateHtmlBody = templateHtmlBody.replace(SENDER_LAST_NAME, DEFAULT_SENDER_LAST_NAME_MERGE_TAG);
		templateHtmlBody = templateHtmlBody.replace(SENDER_FULL_NAME, DEFAULT_SENDER_FULL_NAME_MERGE_TAG);
		templateHtmlBody = templateHtmlBody.replace(SENDER_JOB_TITLE, DEFAULT_SENDER_JOB_TITLE_MERGE_TAG);
		templateHtmlBody = templateHtmlBody.replace(SENDER_TITLE, DEFAULT_SENDER_TITLE_MERGE_TAG);
		templateHtmlBody = templateHtmlBody.replace(SENDER_EMAIL_ID, DEFAULT_SENDER_EMAIL_ID_MERGE_TAG);
		templateHtmlBody = templateHtmlBody.replace(SENDER_CONTACT_NUMBER, DEFAULT_SENDER_CONTACT_NUMBER_MERGE_TAG);
		templateHtmlBody = templateHtmlBody.replace(SENDER_COMPANY_URL, DEFAULT_SENDER_COMPANY_URL_MERGE_TAG);
		templateHtmlBody = templateHtmlBody.replace(SENDER_COMPANY_ADDRESS, DEFAULT_SENDER_COMPANY_ADDRESS_MERGE_TAG);
		templateHtmlBody = templateHtmlBody.replace(SENDER_COMPANY_CONTACT_NUMBER,
				DEFAULT_SENDER_COMPANY_CONTACT_NUMBER_MERGE_TAG);
		templateHtmlBody = templateHtmlBody.replace(SENDER_COMPANY, DEFAULT_SENDER_COMPANY_MERGE_TAG);
		templateHtmlBody = templateHtmlBody.replace(SENDER_PRIVACY_POLICY, DEFAULT_SENDER_PRIVACY_POLICY_MERGE_TAG);
		templateHtmlBody = templateHtmlBody.replace(SENDER_EVENT_URL, DEFAULT_SENDER_EVENT_URL_MERGE_TAG);
		templateHtmlBody = templateHtmlBody.replace(PARTNER_ABOUT_US, DEFAULT_PARTNER_ABOUT_US_MERGE_TAG);
		templateHtmlBody = templateHtmlBody.replace(SENDER_ABOUT_US, DEFAULT_SENDER_ABOUT_US_MERGE_TAG);
		/****** XNFR-281 *******/
		templateHtmlBody = templateHtmlBody.replace(SENDER_COMPANY_INSTAGRAM_URL,
				DEFAULT_SENDER_COMPANY_INSTAGRAM_URL_MERGE_TAG);
		templateHtmlBody = templateHtmlBody.replace(SENDER_COMPANY_TWITTER_URL,
				DEFAULT_SENDER_COMPANY_TWITTER_URL_MERGE_TAG);/****** XNFR-281 *******/
		return templateHtmlBody;
	}

	public String replaceReceiverMergeTagsWithDefaultMergeTags(String templateHtmlBody) {
		templateHtmlBody = templateHtmlBody.replace(FIRST_NAME, DEFAULT_FIRST_NAME_MERGE_TAG);
		templateHtmlBody = templateHtmlBody.replace(LAST_NAME, DEFAULT_LAST_NAME_MERGE_TAG);
		templateHtmlBody = templateHtmlBody.replace(EMAIL_ID, DEFAULT_EMAIL_ID_MERGE_TAG);
		templateHtmlBody = templateHtmlBody.replace(FULL_NAME, DEFAULT_FULL_NAME_MERGE_TAG);
		templateHtmlBody = templateHtmlBody.replace(COMPANY_NAME, DEFAULT_COMPANY_NAME_MERGE_TAG);
		templateHtmlBody = templateHtmlBody.replace(ADDRESS, DEFAULT_ADDRESS_MERGE_TAG);
		templateHtmlBody = templateHtmlBody.replace(ZIPCODE, DEFAULT_ZIPCODE_MERGE_TAG);
		templateHtmlBody = templateHtmlBody.replace(CITY, DEFAULT_CITY_MERGE_TAG);
		templateHtmlBody = templateHtmlBody.replace(STATE, DEFAULT_STATE_MERGE_TAG);
		templateHtmlBody = templateHtmlBody.replace(COUNTRY, DEFAULT_COUNTRY_MERGE_TAG);
		templateHtmlBody = templateHtmlBody.replace(MOBILE_NUMBER, DEFAULT_MOBILE_NUMBER_MERGE_TAG);
		return templateHtmlBody;
	}

	public String replaceReceiverMergeTags(User user, String updatedBody) {
		return XamplifyUtils.replaceReceiverMergeTagsInfo(user, updatedBody);
	}

	public String replaceReceiverMergeTagsWithEmptyString(String updatedTemplateBody) {
		updatedTemplateBody = updatedTemplateBody.replace(DEFAULT_FIRST_NAME_MERGE_TAG, "");
		updatedTemplateBody = updatedTemplateBody.replace(DEFAULT_LAST_NAME_MERGE_TAG, "");
		updatedTemplateBody = updatedTemplateBody.replace(DEFAULT_FULL_NAME_MERGE_TAG, "");
		updatedTemplateBody = updatedTemplateBody.replace(DEFAULT_EMAIL_ID_MERGE_TAG, "");
		updatedTemplateBody = updatedTemplateBody.replace(DEFAULT_COMPANY_NAME_MERGE_TAG, "");
		updatedTemplateBody = updatedTemplateBody.replace(DEFAULT_ADDRESS_MERGE_TAG, "");
		updatedTemplateBody = updatedTemplateBody.replace(DEFAULT_ZIPCODE_MERGE_TAG, "");
		updatedTemplateBody = updatedTemplateBody.replace(DEFAULT_CITY_MERGE_TAG, "");
		updatedTemplateBody = updatedTemplateBody.replace(DEFAULT_STATE_MERGE_TAG, "");
		updatedTemplateBody = updatedTemplateBody.replace(DEFAULT_COUNTRY_MERGE_TAG, "");
		updatedTemplateBody = updatedTemplateBody.replace(DEFAULT_MOBILE_NUMBER_MERGE_TAG, "");
		updatedTemplateBody = updatedTemplateBody.replace(FIRST_NAME, "");
		updatedTemplateBody = updatedTemplateBody.replace(LAST_NAME, "");
		updatedTemplateBody = updatedTemplateBody.replace(FULL_NAME, "");
		updatedTemplateBody = updatedTemplateBody.replace(EMAIL_ID, "");
		updatedTemplateBody = updatedTemplateBody.replace(COMPANY_NAME, "");
		updatedTemplateBody = updatedTemplateBody.replace(ADDRESS, "");
		updatedTemplateBody = updatedTemplateBody.replace(ZIPCODE, "");
		updatedTemplateBody = updatedTemplateBody.replace(CITY, "");
		updatedTemplateBody = updatedTemplateBody.replace(STATE, "");
		updatedTemplateBody = updatedTemplateBody.replace(COUNTRY, "");
		updatedTemplateBody = updatedTemplateBody.replace(MOBILE_NUMBER, "");
		return updatedTemplateBody;
	}

	public Set<User> getAllValidPartners(Set<User> users) {
		Set<User> uniqueUsers = new HashSet<>();
		uniqueUsers.addAll(users.parallelStream().filter(User::isEmailValid).collect(Collectors.toSet()));
		return uniqueUsers;
	}

	public Set<String> listUrlsFromHtmlBody(String body) {
		Set<String> urls = new HashSet<>();
		if (StringUtils.hasText(body)) {
			Document doc = Jsoup.parse(body);
			Elements links = doc.select("a[href]");
			for (Element link : links) {
				String ahref = link.attr("href");
				boolean unsubscribeUrl = ("<unsubscribeURL>").equalsIgnoreCase(ahref);
				boolean isDummyUrlExists = (DUMMY_URL).equalsIgnoreCase(ahref)
						|| (DUMMY_CO_BRANDING_URL).equalsIgnoreCase(ahref);
				boolean isRsvpUrlsExist = rsvpYes.equalsIgnoreCase(ahref) || rsvpNo.equalsIgnoreCase(ahref)
						|| rsvpMaybe.equalsIgnoreCase(ahref);
				if (!(unsubscribeUrl || isDummyUrlExists || isRsvpUrlsExist)) {
					urls.add(ahref);
				}

			}
		}
		return urls;
	}

	public EmailTemplateDTO setEmailTemplateData(EmailTemplate emailTemplate) {
		EmailTemplateDTO emailTemplateDTO = new EmailTemplateDTO();
		CompanyProfile userCompanyProfile = emailTemplate.getUser().getCompanyProfile();
		emailTemplateDTO.setId(emailTemplate.getId());
		emailTemplateDTO.setName(emailTemplate.getName());
		emailTemplateDTO.setBody(emailTemplate.getBody());
		emailTemplateDTO.setJsonBody(emailTemplate.getJsonBody());
		emailTemplateDTO.setSubject(emailTemplate.getSubject());
		emailTemplateDTO.setDescription(emailTemplate.getDesc());
		emailTemplateDTO.setUserDefined(emailTemplate.isUserDefined());
		emailTemplateDTO.setRegularTemplate(emailTemplate.isRegularTemplate());
		emailTemplateDTO.setVideoTemplate(emailTemplate.isVideoTemplate());
		emailTemplateDTO.setType(emailTemplate.getType());
		emailTemplateDTO.setUserId(emailTemplate.getUser().getUserId());
		emailTemplateDTO.setCreatedBy(XamplifyUtils.setDisplayName(emailTemplate.getUser()));
		emailTemplateDTO.setCompanyName(XamplifyUtils.setCompanyName(userCompanyProfile));
		emailTemplateDTO.setDraft(emailTemplate.isDraft());
		emailTemplateDTO.setSpamScore(emailTemplate.getSpamScore());
		emailTemplateDTO.setEditPartnerTemplate(
				emailTemplate.getJsonBody() != null && StringUtils.hasText(emailTemplate.getJsonBody()));
		if (emailTemplate.getCreatedTime() != null) {
			emailTemplateDTO.setCreatedDate(DateUtils.getUtcString(emailTemplate.getCreatedTime()));
		}
		setVendorData(emailTemplate, emailTemplateDTO);
		setEmailTemplateType(emailTemplate, emailTemplateDTO);
		setPartnerLogoFullPath(emailTemplate, emailTemplateDTO, userCompanyProfile);
		return emailTemplateDTO;
	}

	public EmailTemplateDTO setEventCampaignTemplate(EmailTemplate emailTemplate) {
		EmailTemplateDTO emailTemplateDTO = new EmailTemplateDTO();
		CompanyProfile userCompanyProfile = emailTemplate.getUser().getCompanyProfile();
		emailTemplateDTO.setCompanyName(XamplifyUtils.setCompanyName(userCompanyProfile));
		emailTemplateDTO.setId(emailTemplate.getId());
		emailTemplateDTO.setName(emailTemplate.getName());
		emailTemplateDTO.setBody(emailTemplate.getBody());
		emailTemplateDTO.setJsonBody(emailTemplate.getJsonBody());
		emailTemplateDTO.setSubject(emailTemplate.getSubject());
		emailTemplateDTO.setDescription(emailTemplate.getDesc());
		emailTemplateDTO.setUserDefined(emailTemplate.isUserDefined());
		emailTemplateDTO.setRegularTemplate(emailTemplate.isRegularTemplate());
		emailTemplateDTO.setVideoTemplate(emailTemplate.isVideoTemplate());
		emailTemplateDTO.setType(emailTemplate.getType());
		emailTemplateDTO.setUserId(emailTemplate.getUser().getUserId());
		emailTemplateDTO.setCreatedBy(XamplifyUtils.setDisplayName(emailTemplate.getUser()));
		emailTemplateDTO.setDraft(emailTemplate.isDraft());
		if (emailTemplate.getCreatedTime() != null) {
			emailTemplateDTO.setCreatedDate(DateUtils.getUtcString(emailTemplate.getCreatedTime()));
		}
		setVendorData(emailTemplate, emailTemplateDTO);
		setEmailTemplateType(emailTemplate, emailTemplateDTO);
		setPartnerLogoFullPath(emailTemplate, emailTemplateDTO, userCompanyProfile);
		emailTemplateDTO.setEditPartnerTemplate(
				emailTemplate.getJsonBody() != null && StringUtils.hasText(emailTemplate.getJsonBody()));
		return emailTemplateDTO;
	}

	private void setPartnerLogoFullPath(EmailTemplate emailTemplate, EmailTemplateDTO emailTemplateDTO,
			CompanyProfile userCompanyProfile) {
		if (emailTemplate.getType() != null && EmailTemplateType.PARTNER.equals(emailTemplate.getType())
				&& userCompanyProfile != null && StringUtils.hasText(userCompanyProfile.getCompanyLogoPath())) {
			emailTemplateDTO.setPartnerCompanyLogoPath(serverPath + userCompanyProfile.getCompanyLogoPath());
		}
	}

	private void setVendorData(EmailTemplate emailTemplate, EmailTemplateDTO emailTemplateDTO) {
		if (emailTemplate.getType() != null && EmailTemplateType.PARTNER.name().equals(emailTemplate.getType().name())
				&& emailTemplate.getVendor() != null) {
			emailTemplateDTO.setVendorName(XamplifyUtils.setDisplayName(emailTemplate.getVendor()));
			CompanyProfile vendorCompanyProfile = emailTemplate.getVendor().getCompanyProfile();
			if (vendorCompanyProfile != null) {
				emailTemplateDTO.setVendorOrganizationName(XamplifyUtils.setCompanyName(vendorCompanyProfile));
				emailTemplateDTO.setVendorCompanyId(vendorCompanyProfile.getId());
				String companyLogoPath = vendorCompanyProfile.getCompanyLogoPath();
				if (StringUtils.hasText(companyLogoPath)) {
					emailTemplateDTO.setVendorCompanyLogoPath(serverPath + companyLogoPath);
				} else {
					emailTemplateDTO.setVendorCompanyLogoPath("");
				}
			} else {
				emailTemplateDTO.setVendorOrganizationName("");
				emailTemplateDTO.setVendorCompanyLogoPath("");
				emailTemplateDTO.setVendorCompanyId(0);
			}
		}
	}

	public void setEmailTemplateType(EmailTemplate emailTemplate, EmailTemplateDTO emailTemplateDTO) {
		boolean isRegularTemplate = emailTemplate.isRegularTemplate();
		boolean isVideoTemplate = emailTemplate.isVideoTemplate();
		if (isRegularTemplate) {
			emailTemplateDTO.setEmailTemplateType(REGULAR_EMAIL_TEMPLATE);
		} else if (isVideoTemplate) {
			emailTemplateDTO.setEmailTemplateType(VIDEO_EMAIL_TEMPLATE);
		}
	}

	public void setEmailTemplateType(EmailTemplateDTO emailTemplateDTO) {
		boolean isRegularTemplate = emailTemplateDTO.isRegularTemplate() || emailTemplateDTO.isBeeRegularTemplate();
		boolean isRegularCoBrandingTemplate = emailTemplateDTO.isRegularCoBrandingTemplate();
		boolean isVideoCoBrandingTemplate = emailTemplateDTO.isVideoCoBrandingTemplate();
		boolean isVideoTemplate = emailTemplateDTO.isVideoTemplate() || emailTemplateDTO.isBeeVideoTemplate();
		boolean isBeeEventTemplate = emailTemplateDTO.isBeeEventTemplate();
		boolean isBeeEventCoBrandingTemplate = emailTemplateDTO.isBeeEventCoBrandingTemplate();
		boolean isSurveyTemplate = emailTemplateDTO.isSurveyTemplate();
		boolean isSurveyCoBrandingTemplate = emailTemplateDTO.isSurveyCoBrandingTemplate();
		if (isRegularCoBrandingTemplate) {
			emailTemplateDTO.setEmailTemplateType(REGULAR_CO_BRANDING_EMAIL_TEMPLATE);
		} else if (isRegularTemplate) {
			emailTemplateDTO.setEmailTemplateType(REGULAR_EMAIL_TEMPLATE);
		} else if (isVideoCoBrandingTemplate) {
			emailTemplateDTO.setEmailTemplateType(VIDEO_CO_BRANDING_EMAIL_TEMPLATE);
		} else if (isVideoTemplate) {
			emailTemplateDTO.setEmailTemplateType(VIDEO_EMAIL_TEMPLATE);
		} else if (isBeeEventTemplate) {
			emailTemplateDTO.setEmailTemplateType(EVENT_EMAIL_TEMPLATE);
		} else if (isBeeEventCoBrandingTemplate) {
			emailTemplateDTO.setEmailTemplateType(EVENT_CO_BRANDING_EMAIL_TEMPLATE);
		} else if (isSurveyTemplate) {
			emailTemplateDTO.setEmailTemplateType(SURVEY);
		} else if (isSurveyCoBrandingTemplate) {
			emailTemplateDTO.setEmailTemplateType(SURVEY_CO_BRANDING);
		}
	}

	public XtremandResponse hasPartnerAccess(Integer userId) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(200);
		if (userId != null) {
			List<String> roles = userDao.listRolesByUserId(userId);
			String status = userDao.getStatusByUserId(userId);
			boolean access = roles.indexOf(Role.COMPANY_PARTNER.getRoleName()) > -1
					&& UserStatus.APPROVED.getStatus().equals(status);
			if (!access) {
				boolean isPartnerTemMember = userDao.isPartnerTeamMember(userId);
				response.setAccess(isPartnerTemMember);
			} else {
				response.setAccess(true);
			}
		}
		return response;
	}

	public boolean hasTeamMemberAccess(Integer userId) {
		if (userId != null) {
			List<String> roles = userDao.listRolesByUserId(userId);
			String status = userDao.getStatusByUserId(userId);
			return (hasAnyAdminRole(roles) || roles.indexOf(Role.COMPANY_PARTNER.getRoleName()) > -1)
					&& UserStatus.APPROVED.getStatus().equals(status);
		} else {
			return false;
		}
	}

	public boolean hasFolderAccess(Integer userId) {
		if (userId != null) {
			List<String> roles = userDao.listRolesByUserId(userId);
			return !(!roles.isEmpty() && roles.indexOf(Role.USER_ROLE.getRoleName()) > -1 && roles.size() == 1);
		} else {
			return false;
		}

	}

	public boolean hasVideoAccess(Integer userId) {
		if (userId != null) {
			List<String> roles = userDao.listRolesByUserId(userId);
			String status = userDao.getStatusByUserId(userId);
			return (roles.indexOf(Role.DAM.getRoleName()) > -1 || hasAnyAdminRole(roles))
					&& UserStatus.APPROVED.getStatus().equals(status);
		} else {
			return false;
		}
	}

	public boolean hasPartnerModuleAccess(Integer userId) {
		if (userId != null) {
			List<String> roles = userDao.listRolesByUserId(userId);
			String status = userDao.getStatusByUserId(userId);
			return (roles.indexOf(Role.PARTNERS.getRoleName()) > -1 || roles.indexOf(Role.PRM_ROLE.getRoleName()) > -1
					&& UserStatus.APPROVED.getStatus().equals(status));
		} else {
			return false;
		}
	}

	public boolean hasShareLeadsModuleAccess(Integer userId) {
		return true;
	}

	/********************** Vanity Url Related Code **************************/
	public AccessDetailsDTO getAccessDetails(Integer userId) {
		AccessDetailsDTO accessDetailsDTO = new AccessDetailsDTO();
		if (userId != null && userId > 0) {
			List<String> rolesByUserId = userDao.listRolesByUserId(userId);
			boolean isPartner = rolesByUserId.indexOf(Role.COMPANY_PARTNER.getRoleName()) > -1;
			boolean isPrm = rolesByUserId.indexOf(Role.PRM_ROLE.getRoleName()) > -1;
			RoleDTO roleDTO = teamDao.getSuperiorIdAndRolesByTeamMemberId(userId);
			List<String> superiorRoles = roleDTO.getRoles();
			boolean isSuperiorOrgAdmin = false;
			boolean isSuperiorPartner = false;
			boolean orgAdminAndPartnerTeamMember = false;
			boolean partnerAnalyticsAccess = false;
			if (superiorRoles != null && !superiorRoles.isEmpty()) {
				/********* OrgAdmin **********/
				isSuperiorPartner = superiorRoles.indexOf(Role.COMPANY_PARTNER.getRoleName()) > -1;
				orgAdminAndPartnerTeamMember = isSuperiorOrgAdmin && isSuperiorPartner;
				/********* PRM **********/
				boolean isPrmAndPartnerCompany = findIsPrmAndPartnerCompany(isPrm, superiorRoles, isSuperiorPartner);

				/****** Condition is for extra Admin ************/
				if (isPrmAndPartnerCompany) {
					isPartner = true;
				} else if (superiorRoles.indexOf(Role.PRM_ROLE.getRoleName()) > -1) {
					accessDetailsDTO.setPrmTeamMember(true);
				}
			}
			if (superiorRoles != null && !superiorRoles.isEmpty() && !isPrm) {
				partnerAnalyticsAccess = setTeamMemberData(accessDetailsDTO, superiorRoles, isSuperiorOrgAdmin,
						isSuperiorPartner, orgAdminAndPartnerTeamMember);
			}

			setModuleAccess(accessDetailsDTO, rolesByUserId, isPartner, partnerAnalyticsAccess, userId);
		}
		return accessDetailsDTO;
	}

	private boolean findIsPrmAndPartnerCompany(boolean isMarketing, List<String> superiorRoles,
			boolean isSuperiorPartner) {
		boolean isSuperiorPrm = superiorRoles.indexOf(Role.PRM_ROLE.getRoleName()) > -1;
		boolean prmAndPartnerTeamMember = isSuperiorPrm && isSuperiorPartner;
		boolean isPrmAndPartnerCompany = prmAndPartnerTeamMember && isMarketing;
		return isPrmAndPartnerCompany;
	}

	private boolean setTeamMemberData(AccessDetailsDTO accessDetailsDTO, List<String> superiorRoles,
			boolean isSuperiorOrgAdmin, boolean isSuperiorPartner, boolean orgAdminAndPartnerTeamMember) {
		boolean partnerAnalyticsAccess;
		boolean isSuperiorPrm = superiorRoles.indexOf(Role.PRM_ROLE.getRoleName()) > -1;
		partnerAnalyticsAccess = isSuperiorOrgAdmin || isSuperiorPrm;

		boolean isPartnerTeamMember = superiorRoles.size() == 2
				&& superiorRoles.indexOf(Role.COMPANY_PARTNER.getRoleName()) > -1;
		accessDetailsDTO.setPartnerTeamMember(isPartnerTeamMember);

		accessDetailsDTO.setPrmAndPartnerTeamMember(
				superiorRoles.indexOf(Role.PRM_ROLE.getRoleName()) > -1 && isSuperiorPartner);
		return partnerAnalyticsAccess;
	}

	private void setModuleAccess(AccessDetailsDTO accessDetailsDTO, List<String> rolesByUserId, boolean isPartner,
			boolean partnerAnalyticsAccess, Integer userId) {
		boolean isAllRole = rolesByUserId.indexOf(Role.ALL_ROLES.getRoleName()) > -1;
		boolean isOnlyPartner = Role.isOnlyPartnerCompanyByRoleNames(rolesByUserId);
		boolean isPrm = rolesByUserId.indexOf(Role.PRM_ROLE.getRoleName()) > -1;
		boolean isPrmAndPartner = isPrm && isPartner;
		accessDetailsDTO.setPartner(isPartner);
		accessDetailsDTO.setOnlyPartner(isOnlyPartner);
		accessDetailsDTO.setPrm(isPrm);
		accessDetailsDTO.setPrmAndPartner(isPrmAndPartner);
		boolean isAnyAdmin = isPrm;
		accessDetailsDTO.setVideoAccess(rolesByUserId.indexOf(Role.VIDEO_UPLOAD_ROLE.getRoleName()) > -1 || isAnyAdmin);
		accessDetailsDTO.setStatsAccess(rolesByUserId.indexOf(Role.STATS_ROLE.getRoleName()) > -1 || isAnyAdmin);
		accessDetailsDTO.setPartnerAccess(rolesByUserId.indexOf(Role.PARTNERS.getRoleName()) > -1 || isPrm);
		accessDetailsDTO.setAllAccess(isAllRole);
		accessDetailsDTO.setPartnerAnalyticsAccess(isAnyAdmin || partnerAnalyticsAccess);
		accessDetailsDTO.setTeamMemberAccess(isAnyAdmin || isPartner);
		/********* XNFR-117 ***********/
		DeletedPartnerDTO deletedPartnerDTO = utilDao.getDeletedPartnerDTOByRoleNames(userId, rolesByUserId);
		accessDetailsDTO.setOnlyUser(deletedPartnerDTO.isOnlyUser());
		if (deletedPartnerDTO.isDeletedPartnerCompanyUser()) {
			accessDetailsDTO.setTeamMemberAccess(!teamDao.isTeamMember(userId));
		}
	}

	public void isVanityUrlFilterApplicable(VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		boolean isVanityUrlFilter = vanityUrlDetailsDTO.isVanityUrlFilter();
		if (isVanityUrlFilter) {
			Integer loggedInUserId = vanityUrlDetailsDTO.getUserId();
			String vendorCompanyProfileName = utilDao.getPrmCompanyProfileName();
			vanityUrlDetailsDTO.setVendorCompanyProfileName(vendorCompanyProfileName);
			if (StringUtils.hasText(vendorCompanyProfileName)) {
				Integer loggedInUserCompanyId = userDao.getCompanyIdByUserId(loggedInUserId);
				Integer vendorCompanyId = userDao.getCompanyIdByProfileName(vendorCompanyProfileName);
				boolean isLoggedInThroughPartnerUrl = !vendorCompanyId.equals(loggedInUserCompanyId);
				vanityUrlDetailsDTO.setPartnerLoggedInThroughVanityUrl(isLoggedInThroughPartnerUrl);
				vanityUrlDetailsDTO.setVendorLoggedInThroughOwnVanityUrl(vendorCompanyId.equals(loggedInUserCompanyId));
				vanityUrlDetailsDTO.setLoggedInUserCompanyId(loggedInUserCompanyId);
				vanityUrlDetailsDTO.setVendorCompanyId(vendorCompanyId);
			} else {
				boolean isPrm = utilDao.isPrmCompany(loggedInUserId);
				boolean isPartner = utilDao.isOnlyPartnerCompany(loggedInUserId);
				if (isPrm) {
					vanityUrlDetailsDTO.setVendorLoggedInThroughOwnVanityUrl(true);
				} else if (isPartner) {
					vanityUrlDetailsDTO.setPartnerLoggedInThroughVanityUrl(true);
					Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
					vanityUrlDetailsDTO.setLoggedInUserCompanyId(companyId);
					vanityUrlDetailsDTO.setVendorCompanyId(utilDao.getPrmCompanyId());
				}

			}
		}
	}

	public void setVanityUrlFilter(Pagination pagination) {
		VanityUrlDetailsDTO postDto = new VanityUrlDetailsDTO();
		postDto.setUserId(pagination.getUserId());
		postDto.setVendorCompanyProfileName(utilDao.getPrmCompanyProfileName());
		postDto.setVanityUrlFilter(pagination.isVanityUrlFilter());
		isVanityUrlFilterApplicable(postDto);
		/***** XNFR-252 *****/
		boolean loginAsPartner = XamplifyUtils.isLoginAsPartner(pagination.getLoginAsUserId());
		if (postDto.isPartnerLoggedInThroughVanityUrl() || loginAsPartner) {
			pagination.setVanityUrlFilterApplicable(true);
			if (loginAsPartner) {
				Integer loggedInUserCompanyId = userDao.getCompanyIdByUserId(pagination.getLoginAsUserId());
				pagination.setVendorCompanyId(loggedInUserCompanyId);
			} else {
				pagination.setVendorCompanyId(postDto.getVendorCompanyId());
			}
		}
	}

	public RoleDisplayDTO getRoleDetailsByUserId(Integer userId) {
		List<Integer> roleIds = userDao.getRoleIdsByUserId(userId);
		utilDao.addPartnerRoleToSecondAdmins(userId, roleIds);
		RoleDisplayDTO roleDisplayDTO = setRoleDTO(roleIds);
		if (teamMemberRole.equals(roleDisplayDTO.getRole())) {
			RoleDTO roleDTO = teamDao.getSuperiorIdAndRolesByTeamMemberId(userId);
			List<String> superiorRoles = roleDTO.getRoles();
			if (superiorRoles != null && !superiorRoles.isEmpty()) {
				boolean isSuperiorAlsoPartner = superiorRoles.indexOf(Role.COMPANY_PARTNER.getRoleName()) > -1;
				boolean prmTeamMember = superiorRoles.indexOf(Role.PRM_ROLE.getRoleName()) > -1;
				boolean prmAndPartnerTeamMember = prmTeamMember && isSuperiorAlsoPartner;
				boolean isOnlyPartnerTeamMember = Role.isOnlyPartnerCompanyByRoleNames(superiorRoles);

				roleDisplayDTO.setPartnerTeamMember(isOnlyPartnerTeamMember);

				roleDisplayDTO.setPrmTeamMember(prmTeamMember);

				roleDisplayDTO.setPrmAndPartnerTeamMember(prmAndPartnerTeamMember);

			}

		}

		boolean hasCompany = userDao.hasCompany(userId);
		roleDisplayDTO.setCompanyExists(hasCompany);
		return roleDisplayDTO;
	}

	public RoleDisplayDTO getRoleDetailsByRoleIds(List<Integer> roleIds) {
		return setRoleDTO(roleIds);

	}

	public RoleDisplayDTO setRoleDTO(List<Integer> roleIds) {
		RoleDisplayDTO roleDisplayDTO = new RoleDisplayDTO();
		roleDisplayDTO.setRoleIds(roleIds);
		boolean hasNotCompanyPartnerRole = roleIds.indexOf(Role.COMPANY_PARTNER.getRoleId()) < 0;
		boolean hasCompanyPartnerRole = roleIds.indexOf(Role.COMPANY_PARTNER.getRoleId()) > -1;
		boolean hasUserRole = roleIds.indexOf(Role.USER_ROLE.getRoleId()) > -1;
		boolean hasUserRoleAndNotCompanyPartnerRole = hasUserRole && hasNotCompanyPartnerRole;
		boolean hasUserRoleAndCompanyPartnerRole = hasUserRole && hasCompanyPartnerRole;
		boolean isPartner = Role.isOnlyPartnerCompanyByRoleIds(roleIds);

		boolean isPrm = roleIds.indexOf(Role.PRM_ROLE.getRoleId()) > -1 && hasUserRoleAndNotCompanyPartnerRole;

		boolean isPrmAndPartner = roleIds.indexOf(Role.PRM_ROLE.getRoleId()) > -1 && hasUserRoleAndCompanyPartnerRole;

		boolean isOnlyUser = roleIds.size() == 1 && roleIds.indexOf(Role.USER_ROLE.getRoleId()) > -1;
		roleDisplayDTO.setPrm(isPrm);
		roleDisplayDTO.setPrmAndPartner(isPrmAndPartner);
		roleDisplayDTO.setPartner(isPartner);
		if (isPartner) {
			roleDisplayDTO.setRole(partnerRole);
		} else if (isPrm) {
			roleDisplayDTO.setRole(prmRole);
		} else if (isPrmAndPartner) {
			roleDisplayDTO.setRole(prmAndPartnerRole);
		} else if (isOnlyUser) {
			roleDisplayDTO.setRole(userRole);
		} else {
			roleDisplayDTO.setRole(teamMemberRole);
		}

		return roleDisplayDTO;
	}

	public VanityUrlDetailsDTO getVanityUrlFilteredData(Integer loggedInUserId, boolean isUserLoggedInThroughVanityUrl,
			String vanityUrlDomainName) {
		VanityUrlDetailsDTO vanityUrlDetailsDTO = new VanityUrlDetailsDTO();
		if (isUserLoggedInThroughVanityUrl) {
			vanityUrlDetailsDTO.setUserId(loggedInUserId);
			vanityUrlDetailsDTO.setVanityUrlFilter(isUserLoggedInThroughVanityUrl);
			vanityUrlDetailsDTO.setVendorCompanyProfileName(vanityUrlDomainName);
			isVanityUrlFilterApplicable(vanityUrlDetailsDTO);
		}
		return vanityUrlDetailsDTO;
	}

	/**********************
	 * End Of Vanity Url Related Code
	 **************************/

	public XtremandResponse getSenderMergeTagsData(Integer userId) {
		XtremandResponse response = new XtremandResponse();
		User user = userDao.findByPrimaryKey(userId, new FindLevel[] { FindLevel.COMPANY_PROFILE });
		if (user != null) {
			MyMergeTagsInfo myMergeTagsInfo = XamplifyUtils.getMyMergeTagsData(user);
			response.setData(myMergeTagsInfo);
			response.setStatusCode(200);
		} else {
			response.setStatusCode(404);
			response.setMessage("This user does not exists.");
		}

		return response;
	}

	public String getCompanyLogoPath(Integer userId) {
		Integer companyId = userDao.getCompanyIdByUserId(userId);
		return userDao.getCompanyLogoPath(companyId);
	}

	public boolean isValidAccessToken(String tokenValidateUrl, String accessToken) {
		boolean result = false;
		try {
			HttpClient httpClient = HttpClientBuilder.create().build();
			HttpGet httpGet = new HttpGet(tokenValidateUrl);

			Header oauthHeader = new BasicHeader("Authorization", "OAuth " + accessToken);
			httpGet.addHeader(oauthHeader);
			httpGet.addHeader(prettyPrintHeader);

			HttpResponse response = httpClient.execute(httpGet);

			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == 200) {
				String responseString = EntityUtils.toString(response.getEntity());
				try {
					@SuppressWarnings("unused")
					JSONObject jsonObject = new JSONObject(responseString);
					result = true;
				} catch (JSONException je) {
					je.printStackTrace();
				}
			} else {
				logger.error("Query was unsuccessful. Status code returned is " + statusCode);
				logger.error("An error has occured. Http status: " + response.getStatusLine().getStatusCode());
				String errorData = getBody(response.getEntity().getContent());
				logger.debug(errorData);
				result = false;
			}
		} catch (Exception e) {
			result = false;
		}
		return result;
	}

	private String getBody(InputStream inputStream) {
		String result = "";
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				result += inputLine;
				result += "\n";
			}
			in.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return result;
	}

	public List<String> findCommonStrings(List<String> list1, List<String> list2) {
		list1.retainAll(list2);
		return list1;
	}

	public String getCompanyType(Integer companyId) {
		String companyType = null;
		List<User> owners = partnershipDao.getOwners(companyId);
		if (owners != null && !owners.isEmpty()) {
			List<Integer> roles = new ArrayList<>();
			for (User owner : owners) {
				if (!teamDao.isTeamMember(owner.getUserId())) {
					roles = userDao.getRoleIdsByUserId(owner.getUserId());
					break;
				}
			}

			if (roles != null && !roles.isEmpty()) {
				Integer companyPartner = Role.COMPANY_PARTNER.getRoleId();
				Integer prm = Role.PRM_ROLE.getRoleId();
				if (roles.indexOf(prm) > -1) {
					companyType = PRM;
				}
				companyType = setCompanyPartner(companyType, roles, companyPartner);
			}
		} else {
			companyType = PARTNER;
		}
		return companyType;
	}

	private String setCompanyPartner(String companyType, List<Integer> roles, Integer companyPartner) {
		if (roles.indexOf(companyPartner) > -1) {
			if (companyType != null) {
				companyType = companyType + " & " + PARTNER;
			} else {
				companyType = PARTNER;
			}

		}
		return companyType;
	}

	public void downloadPdf(String htmlFilePath, String pdfPath, String htmlBody, String pageSize, String fileName,
			String alias, String pageOrientation) throws IOException {
		String pdfFileDestinationPath = pdfPath + sep + alias + sep + IMAGE_PATH_CURRENT_DATE + sep
				+ System.currentTimeMillis();
		fileUtil.createDirectory(pdfFileDestinationPath);
		String updatedFileName = fileName.replaceAll(regex, "-");
		String pdfFileNameToDownload = updatedFileName + ".pdf";
		String pdfFileCompleteName = pdfFileDestinationPath + sep + pdfFileNameToDownload;
		String htmlFileDestinationPath = "";
		htmlFileDestinationPath = htmlFilePath + alias + sep + IMAGE_PATH_CURRENT_DATE + sep
				+ System.currentTimeMillis();
		fileUtil.createDirectory(htmlFileDestinationPath);
		String htmlFileCompleteName = htmlFileDestinationPath + sep + updatedFileName + ".html";
		exportUtil.generatePdfBySelectedCustomSize(htmlFileCompleteName, pdfFileCompleteName, pdfFileNameToDownload,
				httpServletResponse, htmlBody, pageSize, pageOrientation);
	}

	public void sendLeadsListMail(User user, UserList userList, boolean isCreate,
			List<Integer> totalUnsubscribedUserIds) {
		if (isCreate && userList.isEmailValidationInd() && userList.getCompany() != null) {
			mailService.sendLeadsListMail(user, EmailConstants.LEADS_LIST_PROCESSED_SHARED, userList,
					totalUnsubscribedUserIds);
		} else if (isCreate && userList.isEmailValidationInd() && userList.getCompany() == null) {
			mailService.sendLeadsListMail(user, EmailConstants.LEADS_LIST_PROCESSED_NOT_SHARED, userList,
					totalUnsubscribedUserIds);
		} else if (!isCreate && userList.isEmailValidationInd() && userList.getCompany() == null) {
			mailService.sendLeadsListMail(user, EmailConstants.LEADS_LIST_UPDATED, userList, totalUnsubscribedUserIds);
		} else if (!userList.isEmailValidationInd()) {
			mailService.sendLeadsListMail(user, EmailConstants.LEADS_LIST_IN_PROCESS, userList,
					totalUnsubscribedUserIds);
		}
	}

	public String getLoggedInUserName() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication.getName().contains("@")) {
			UserDetails userDetail = (UserDetails) authentication.getPrincipal();
			return userDetail != null && userDetail.getUsername() != null
					? userDetail.getUsername().toLowerCase().trim()
					: "";
		} else {
			return "test@test.com";
		}
	}

	public XtremandResponse revokeAccessTokenByUserId(Integer userId) {
		XtremandResponse response = new XtremandResponse();
		String emailId = userService.getEmailIdByUserId(userId);
		Collection<OAuth2AccessToken> tokens = tokenStore.findTokensByClientIdAndUserName(xamplifyRestClientId,
				emailId);
		if (tokens != null && !tokens.isEmpty()) {
			for (OAuth2AccessToken token : tokens) {
				String tokenValue = token.getValue();
				if (StringUtils.hasText(tokenValue)) {
					tokenServices.revokeToken(tokenValue);
				}
			}
			response.setStatusCode(200);
			response.setMessage("Access Token Removed Successfully For " + emailId);
		} else {
			setAccessTokenNotFoundMessage(response, emailId);
		}
		return response;
	}

	private void setAccessTokenNotFoundMessage(XtremandResponse response, String emailId) {
		response.setStatusCode(400);
		response.setMessage("Access Token Not Found  For " + emailId);
	}

	public XtremandResponse authorizeUrl(AngularUrl angularUrl) {
		XtremandResponse response = new XtremandResponse();
		boolean isAuthorizedUrl = false;
		isAuthorizedUrl = checkCompanyExistsAndAuthorizeUrls(angularUrl);
		response.setAccess(isAuthorizedUrl);
		return response;
	}

	private boolean checkCompanyExistsAndAuthorizeUrls(AngularUrl angularUrl) {
		boolean isAuthorizedUrl;
		String url = angularUrl.getUrl();
		Integer userId = angularUrl.getUserId();
		boolean hasCompany = userDao.hasCompany(userId);
		if (hasCompany) {
			isAuthorizedUrl = authorizeUrlsForSuperAdminAndAdmins(angularUrl, url, userId);
		} else {
			isAuthorizedUrl = authorizeUrlsForUser(url);
		}
		return isAuthorizedUrl;
	}

	private boolean authorizeUrlsForSuperAdminAndAdmins(AngularUrl angularUrl, String url, Integer userId) {
		boolean isAuthorizedUrl;
		isAuthorizedUrl = authorizeUrlsForAnyAdmin(angularUrl, url, userId);
		return isAuthorizedUrl;
	}

	private boolean authorizeUrlsForAnyAdmin(AngularUrl angularUrl, String url, Integer userId) {
		boolean isAuthorizedUrl;
		RoleDisplayDTO roleDisplayDTO = getRoleDetailsByUserId(userId);
		isVanityUrlFilterApplicable(angularUrl);
		ModuleAccess moduleAccess = utilDao.getModuleAccess(userDao.getCompanyIdByUserId(userId));
		isAuthorizedUrl = authorizeAllModuleUrls(url, angularUrl, userId, roleDisplayDTO, moduleAccess);
		return isAuthorizedUrl;
	}

	private boolean authorizeAllModuleUrls(String url, AngularUrl angularUrl, Integer userId,
			RoleDisplayDTO roleDisplayDTO, ModuleAccess moduleAccess) {
		boolean isAuthorizedUrl = false;
		boolean isLoggedInThroughOwnVanityUrl = angularUrl.isVendorLoggedInThroughOwnVanityUrl();
		boolean isPartnerLoggedInThroughVanityUrl = angularUrl.isPartnerLoggedInThroughVanityUrl();
		boolean isVanityUrlFilter = angularUrl.isVanityUrlFilter();
		if (url.contains(designBaseUrl) || url.contains(emailTemplatesBaseUrl)) {
			isAuthorizedUrl = authorizeDesignModuleUrls(roleDisplayDTO, isAuthorizedUrl, isLoggedInThroughOwnVanityUrl,
					isPartnerLoggedInThroughVanityUrl, isVanityUrlFilter);
		} else if (url.contains(formsBaseUrl)) {
			isAuthorizedUrl = authorizeFormModuleUrls(url, userId, roleDisplayDTO, moduleAccess, angularUrl);
		} else if (url.contains(teamBaseUrl)) {
			isAuthorizedUrl = roleDisplayDTO.hasTeamAccess();
		} else if (url.contains(mdfBaseUrl)) {
			isAuthorizedUrl = authorizeMdfModuleUrls(url, userId, roleDisplayDTO, moduleAccess, angularUrl);
		} else if (url.contains(damBaseUrl)) {
			isAuthorizedUrl = authorizeDamModuleUrls(url, userId, roleDisplayDTO, moduleAccess, angularUrl);
		} else {
			isAuthorizedUrl = true;
		}
		return isAuthorizedUrl;
	}

	private boolean authorizeDesignModuleUrls(RoleDisplayDTO roleDisplayDTO, boolean isAuthorizedUrl,
			boolean isLoggedInThroughOwnVanityUrl, boolean isPartnerLoggedInThroughVanityUrl,
			boolean isVanityUrlFilter) {
		if (isVanityUrlFilter) {
			if (isLoggedInThroughOwnVanityUrl) {
				isAuthorizedUrl = roleDisplayDTO.hasDesignAccess();
			} else if (isPartnerLoggedInThroughVanityUrl) {
				isAuthorizedUrl = false;
			}
		} else {
			isAuthorizedUrl = roleDisplayDTO.hasDesignAccess();
		}
		return isAuthorizedUrl;
	}

	private boolean authorizeFormModuleUrls(String url, Integer userId, RoleDisplayDTO roleDisplayDTO,
			ModuleAccess moduleAccess, AngularUrl angularUrl) {
		boolean isValidUrl = false;
		if (angularUrl.isVanityUrlFilter()) {
			isValidUrl = authorizeFormModuleUrlsForVanityLogin(url, roleDisplayDTO, moduleAccess, angularUrl,
					isValidUrl);
		} else {
			isValidUrl = authorizeFormModuleUrlsForXamplifyLogin(url, userId, roleDisplayDTO, moduleAccess);
		}
		return isValidUrl;
	}

	private boolean authorizeFormModuleUrlsForVanityLogin(String url, RoleDisplayDTO roleDisplayDTO,
			ModuleAccess moduleAccess, AngularUrl angularUrl, boolean isValidUrl) {
		if (angularUrl.isVendorLoggedInThroughOwnVanityUrl()) {
			isValidUrl = authroizeFormModuleUrlsForOwnVanityLogin(url, roleDisplayDTO, moduleAccess);
		} else if (angularUrl.isPartnerLoggedInThroughVanityUrl()) {
			isValidUrl = authorizeFormModuleUrlsForVendorVanityLogin(url, angularUrl);
		}
		return isValidUrl;
	}

	private boolean authorizeFormModuleUrlsForVendorVanityLogin(String url, AngularUrl angularUrl) {
		boolean isValidUrl;
		if (url.contains(partnerFormsUrl)) {
			isValidUrl = utilDao.hasFormAccessByCompanyId(angularUrl.getVendorCompanyId());
		} else {
			isValidUrl = false;
		}
		return isValidUrl;
	}

	private boolean authroizeFormModuleUrlsForOwnVanityLogin(String url, RoleDisplayDTO roleDisplayDTO,
			ModuleAccess moduleAccess) {
		boolean isValidUrl;
		if (url.contains(partnerFormsUrl)) {
			isValidUrl = false;
		} else {
			isValidUrl = moduleAccess.isForm() && roleDisplayDTO.hasDesignAccess();
		}
		return isValidUrl;
	}

	private boolean authorizeFormModuleUrlsForXamplifyLogin(String url, Integer userId, RoleDisplayDTO roleDisplayDTO,
			ModuleAccess moduleAccess) {
		boolean isValidUrl;
		if (url.contains(partnerFormsUrl)) {
			boolean anyPartnerRole = roleDisplayDTO.anyPartnerRole();
			isValidUrl = anyPartnerRole && utilDao.formAccessForPartner(userId);
		} else {
			isValidUrl = moduleAccess.isForm() && roleDisplayDTO.hasDesignAccess();
		}
		return isValidUrl;
	}

	private boolean authorizeDamModuleUrls(String url, Integer userId, RoleDisplayDTO roleDisplayDTO,
			ModuleAccess moduleAccess, AngularUrl angularUrl) {
		boolean isAuthorizedUrl = false;
		if (angularUrl.isVanityUrlFilter()) {
			isAuthorizedUrl = authorizeDamModuleUrlsForVanityLogin(url, roleDisplayDTO, moduleAccess, angularUrl,
					isAuthorizedUrl);
		} else {
			isAuthorizedUrl = authorizeDamModuleUrlsForXamplifyLogin(url, userId, roleDisplayDTO, moduleAccess);
		}
		return isAuthorizedUrl;

	}

	private boolean authorizeDamModuleUrlsForVanityLogin(String url, RoleDisplayDTO roleDisplayDTO,
			ModuleAccess moduleAccess, AngularUrl angularUrl, boolean isAuthorizedUrl) {
		if (angularUrl.isVendorLoggedInThroughOwnVanityUrl()) {
			isAuthorizedUrl = authroizeDamModuleUrlsForOwnVanityLogin(url, roleDisplayDTO, moduleAccess);
		} else if (angularUrl.isPartnerLoggedInThroughVanityUrl()) {
			isAuthorizedUrl = authorizeDamModuleUrlsForVendorVanityLogin(url, angularUrl);
		}
		return isAuthorizedUrl;
	}

	private boolean authorizeDamModuleUrlsForVendorVanityLogin(String url, AngularUrl angularUrl) {
		boolean isAuthorizedUrl;
		if (isDamPartnerUrl(url)) {
			isAuthorizedUrl = utilDao.hasDamAccessByCompanyId(angularUrl.getVendorCompanyId());
		} else {
			isAuthorizedUrl = false;
		}
		return isAuthorizedUrl;
	}

	private boolean authroizeDamModuleUrlsForOwnVanityLogin(String url, RoleDisplayDTO roleDisplayDTO,
			ModuleAccess moduleAccess) {
		boolean isAuthorizedUrl;
		if (isDamPartnerUrl(url)) {
			isAuthorizedUrl = false;
		} else {
			isAuthorizedUrl = roleDisplayDTO.isAnyAdminOrTeamMemberOfAdmin() && moduleAccess.isDam();
		}
		return isAuthorizedUrl;
	}

	private boolean authorizeDamModuleUrlsForXamplifyLogin(String url, Integer userId, RoleDisplayDTO roleDisplayDTO,
			ModuleAccess moduleAccess) {
		boolean isAuthorizedUrl;
		if (isDamPartnerUrl(url)) {
			isAuthorizedUrl = roleDisplayDTO.anyPartnerRole() && utilDao.damAccessForPartner(userId);
		} else {
			isAuthorizedUrl = roleDisplayDTO.isAnyAdminOrTeamMemberOfAdmin() && moduleAccess.isDam();
		}
		return isAuthorizedUrl;
	}

	private boolean isDamPartnerUrl(String url) {
		return url.contains(damPartnerAnalyticsUrl) || url.contains(damPartnerSharedUrl);
	}

	private boolean authorizeMdfModuleUrls(String url, Integer userId, RoleDisplayDTO roleDisplayDTO,
			ModuleAccess moduleAccess, AngularUrl angularUrl) {
		boolean isAuthorizedUrl = false;
		if (angularUrl.isVanityUrlFilter()) {
			if (angularUrl.isVendorLoggedInThroughOwnVanityUrl()) {
				isAuthorizedUrl = authorizeMdfModuleUrlsForOwnVanityLogin(url, roleDisplayDTO, moduleAccess);
			} else if (angularUrl.isPartnerLoggedInThroughVanityUrl()) {
				isAuthorizedUrl = authroizeMdfModuleUrlsForVendorVanityLogin(url, angularUrl);
			}
		} else {
			isAuthorizedUrl = authorizeMdfModuleUrlsForXamplifyLogin(url, userId, roleDisplayDTO, moduleAccess);
		}
		return isAuthorizedUrl;
	}

	private boolean authroizeMdfModuleUrlsForVendorVanityLogin(String url, AngularUrl angularUrl) {
		boolean isAuthorizedUrl;
		if (isMdfPartnerUrl(url)) {
			isAuthorizedUrl = utilDao.hasMdfAccessByCompanyId(angularUrl.getVendorCompanyId());
		} else {
			isAuthorizedUrl = false;
		}
		return isAuthorizedUrl;
	}

	private boolean isMdfPartnerUrl(String url) {
		return url.contains(mdfRequestPartnerUrl) || url.contains(mdfCreateRequestPartnerUrl)
				|| url.contains(mdfTimeLinePartnerUrl);
	}

	private boolean authorizeMdfModuleUrlsForOwnVanityLogin(String url, RoleDisplayDTO roleDisplayDTO,
			ModuleAccess moduleAccess) {
		boolean isAuthorizedUrl;
		if (isMdfPartnerUrl(url)) {
			isAuthorizedUrl = false;
		} else {
			isAuthorizedUrl = roleDisplayDTO.isAnyAdminOrTeamMemberOfAdmin() && moduleAccess.isMdf();
		}
		return isAuthorizedUrl;
	}

	private boolean authorizeMdfModuleUrlsForXamplifyLogin(String url, Integer userId, RoleDisplayDTO roleDisplayDTO,
			ModuleAccess moduleAccess) {
		boolean isAuthorizedUrl;
		boolean anyMdfPartnerUrl = isMdfPartnerUrl(url);
		if (anyMdfPartnerUrl) {
			isAuthorizedUrl = roleDisplayDTO.anyPartnerRole() && utilDao.mdfAccessForPartner(userId);
		} else {
			isAuthorizedUrl = roleDisplayDTO.isAnyAdminOrTeamMemberOfAdmin() && moduleAccess.isMdf();
		}
		return isAuthorizedUrl;
	}

	private boolean authorizeUrlsForUser(String url) {
		return authorizeCompanyProfileUrls(url) && !url.contains(myProfileUrl);
	}

	private boolean authorizeCompanyProfileUrls(String url) {
		return url.contains(addCompanyProfileUrl) || url.contains(editCompanyProfileUrl) || authorizeDefaultUrls(url);
	}

	private boolean authorizeDefaultUrls(String url) {
		return url.contains(defaultDashboardUrl) || url.contains(welcomePageUrl) || url.contains(homeAndDashboardUrl);
	}

	public Set<String> getDashboardType(VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		HashSet<String> dashboardTypes = new HashSet<>();

		String dashboard = "Dashboard";
		isVanityUrlFilterApplicable(vanityUrlDetailsDTO);
		if (vanityUrlDetailsDTO.isPartnerLoggedInThroughVanityUrl()) {
			dashboardTypes.add(dashboard);
		} else {
			setDashboardTypesXamplifyLogin(vanityUrlDetailsDTO, dashboardTypes, dashboard);
		}
		dashboardTypes.add("Welcome");

		return dashboardTypes;
	}

	private void setDashboardTypesXamplifyLogin(VanityUrlDetailsDTO vanityUrlDetailsDTO, HashSet<String> dashboardTypes,
			String dashboard) {
		Integer companyId = userDao.getCompanyIdByUserId(vanityUrlDetailsDTO.getUserId());
		ModuleAccess moduleAccess = utilDao.getModuleAccess(companyId);
		if (moduleAccess != null) {
			String dashboardType = moduleAccess.getDashboardTypeInString();
			if (DashboardTypeEnum.DASHBOARD.name().equals(dashboardType)) {
				dashboardTypes.add(dashboard);
			} else if (DashboardTypeEnum.ADVANCED_DASHBOARD.name().equals(dashboardType)) {
				dashboardTypes.add("Advanced " + dashboard);
			} else if (DashboardTypeEnum.DETAILED_DASHBOARD.name().equals(dashboardType)) {
				dashboardTypes.add(dashboard);
				dashboardTypes.add("Detailed " + dashboard);
			}
		} else {
			dashboardTypes.add(dashboard);
		}
	}

	public ModuleAccess getModuleDetailsByUserId(Integer userId) {
		Integer companyId = userDao.getCompanyIdByUserId(userId);
		return utilDao.getModuleAccess(companyId);
	}

	public void replaceHtmlBodyWithDynamicTags(String alias, String pageSize, boolean isPartnerDownloading,
			String pageOrientation, Integer loggedInPartnerId) throws IOException {
		DamDownloadDTO damDownloadDTO = damDao.getDownloadContent(alias, isPartnerDownloading);
		setTagsAndDownloadPDF(damDownloadDTO, alias, pageSize, isPartnerDownloading, pageOrientation,
				loggedInPartnerId);
	}

	public void setTagsAndDownloadPDF(DamDownloadDTO damDownloadDTO, String alias, String pageSize,
			boolean isPartnerDownloading, String pageOrientation, Integer loggedInPartnerId) throws IOException {
		String updatedHtmlBody = "";
		String htmlBody = damDownloadDTO.getHtmlBody();
		User createdUser = userDao.findByPrimaryKey(damDownloadDTO.getCreatedBy(),
				new FindLevel[] { FindLevel.COMPANY_PROFILE });
		if (isPartnerDownloading) {
			UserDTO sendor = userDao.getSendorCompanyDetailsByUserId(damDownloadDTO.getCreatedBy());
			User receiver = userDao.findByPrimaryKey(loggedInPartnerId, new FindLevel[] { FindLevel.COMPANY_PROFILE });
			String partnerCompanyLogo = damDownloadDTO.getPartnerCompanyLogo();
			String companyLogo = damDownloadDTO.getCompanyLogo();
			updatedHtmlBody = replaceSenderMergeTagsAndPartnerAboutUsMergeTag(htmlBody, receiver, sendor, true);
			CompanyProfile partnerCompany = receiver.getCompanyProfile();
			String partnerCompanyWebSiteUrl = partnerCompany != null ? partnerCompany.getWebsite() : "";
			updatedHtmlBody = xamplifyUtil.replaceCompanyLogoAndCoBrandedLogo(updatedHtmlBody, partnerCompanyLogo,
					companyLogo, partnerCompanyWebSiteUrl);
			updatedHtmlBody = updatedHtmlBody.replace(DEFAULT_PARTNER_ABOUT_US_MERGE_TAG,
					receiver.getCompanyProfile().getAboutUs());
			updatedHtmlBody = replaceCompanyWebSiteUrl(updatedHtmlBody, createdUser);
			receiver.setDamTemplateDownload(true);
			updatedHtmlBody = replaceReceiverMergeTags(receiver, updatedHtmlBody);
			pageSize = damDownloadDTO.getPageSize();
			pageOrientation = damDownloadDTO.getPageOrientation();
		} else {
			updatedHtmlBody = replaceSenderMergeTagsAndPartnerAboutUsMergeTag(htmlBody, createdUser, null, true);
			String companyLogo = damDownloadDTO.getCompanyLogo();
			if (!StringUtils.hasText(companyLogo)) {
				CompanyProfile userCompanyProfile = createdUser.getCompanyProfile();
				if (userCompanyProfile != null) {
					damDownloadDTO.setCompanyLogo(userCompanyProfile.getCompanyLogoPath());
				}
			}
			updatedHtmlBody = replaceCompanyWebSiteUrl(updatedHtmlBody, createdUser);
			updatedHtmlBody = updatedHtmlBody.replace(unsubscribeLinkMergeTag, "");
		}

		String fileName = XamplifyUtils.convertToLowerCaseAndExcludeSpace(damDownloadDTO.getFileName());
		fileName = fileName.replace("/", "_");
		downloadPdf(contentHtmlPath, contentPdfPath, updatedHtmlBody, pageSize, fileName, alias, pageOrientation);
	}

	public String replaceCompanyWebSiteUrl(String updatedHtmlBody, User createdUser) {
		updatedHtmlBody = xamplifyUtil.replaceCompanyWebsiteUrl(updatedHtmlBody,
				createdUser.getCompanyProfile().getWebsite());
		return updatedHtmlBody;
	}

	public String getFullName(User user) {
		String displayName = "";
		if (user != null) {
			String firstName = user.getFirstName();
			String updatedFirstName = firstName != null && StringUtils.hasText(firstName) ? firstName : "";
			String lastName = user.getLastName();
			String updatedLastName = lastName != null && StringUtils.hasText(lastName) ? lastName : "";
			String middleName = user.getMiddleName();
			String updatedMiddlName = middleName != null && StringUtils.hasText(middleName) ? middleName : "";
			String fullName = (updatedFirstName + " " + updatedLastName + " " + updatedMiddlName).trim();
			if (StringUtils.hasText(fullName)) {
				displayName = fullName;
			} else {
				displayName = user.getEmailId();
			}
		}
		return displayName;

	}

	public List<String> listExtractUrls(String text) {
		List<String> containedUrls = new ArrayList<>();
		String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
		Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
		Matcher urlMatcher = pattern.matcher(text);
		while (urlMatcher.find()) {
			containedUrls.add(text.substring(urlMatcher.start(0), urlMatcher.end(0)));
		}
		return containedUrls;
	}

	public Set<User> getNonExcludedUsers(Set<User> uniqueUsers, Integer customerCompanyId) {
		String debugM1 = "Entered Into getNonExcludedUsers() and Users Size " + uniqueUsers.size();
		logger.debug(debugM1);
		Set<User> nonExcludedUsers = new HashSet<>();
		int totalUsers = uniqueUsers.size();
		int count = 0;
		for (User user : uniqueUsers) {
			String debugM2 = "Validating " + user.getEmailId();
			logger.debug(debugM2);
			if (XamplifyUtils.isValidString(user.getEmailId())) {
				boolean isUserExcluded = isUserExcluded(user, customerCompanyId);
				if (!isUserExcluded) {
					nonExcludedUsers.add(user);
				}
			}
			count++;
			int itemsLeft = totalUsers - count;
			String debugM3 = "Users Left : " + itemsLeft;
			logger.debug(debugM3);
		}
		String debugM4 = "getNonExcludedUsers() completed and non excluded users count is " + nonExcludedUsers.size();
		logger.debug(debugM4);
		return nonExcludedUsers;
	}

	public boolean isUserExcluded(User user, Integer customerCompanyId) {
		boolean isUserExcluded = false;
		if (StringUtils.hasText(user.getEmailId()) && user.getEmailId() != null) {
			boolean result = userDao.isExcludedUserExists(customerCompanyId, user.getUserId());
			if (result) {
				isUserExcluded = true;
			}
			if (!isUserExcluded) {
				String emailId = user.getEmailId();
				String domain = emailId.substring(emailId.lastIndexOf('@') + 1);
				boolean isExcludedDomainExists = userDao.isExcludedDomainExists(customerCompanyId, domain);
				if (isExcludedDomainExists) {
					isUserExcluded = true;
				}
			}
		}
		return isUserExcluded;
	}

	public XtremandResponse addAllDomains(Integer companyId) {
		XtremandResponse xtremandResponse = new XtremandResponse();
		List<String> domains = utilDao.findAllDomains();
		for (String domain : domains) {
			String updatedDomain = domain.substring(1);
			ExcludedDomain excludedDomain = new ExcludedDomain();
			excludedDomain.setCompanyId(companyId);
			excludedDomain.setCreatedTime(new Date());
			User createdUser = new User();
			createdUser.setUserId(1);
			excludedDomain.setCreatedUser(createdUser);
			excludedDomain.setDomainName(updatedDomain);
			genericDAO.save(excludedDomain);
		}
		xtremandResponse.setStatusCode(200);
		return xtremandResponse;
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public String addDummyContacts(Integer userId, String publicOrPrivate, Integer limit) {
		long startTime = getStartTime();
		int contactsListSize = 1;
		for (int i = 0; i < contactsListSize; i++) {
			String userListName = publicOrPrivate + "-Contact-" + limit + "(" + System.currentTimeMillis() + ")";
			boolean isPublic = "Public".equalsIgnoreCase(publicOrPrivate);
			UserList userList = new UserList();
			userList.setName(userListName);
			User createdUser = new User();
			createdUser.setUserId(userId);
			userList.setOwner(createdUser);
			userList.setUploadedDate(new Date());
			userList.setCreatedTime(new Date());
			userList.setSocialNetwork(UserList.getSocialNetworkEnum("MANUAL"));
			userList.setContactType(UserList.getContactTypeEnum("CONTACT"));
			userList.setSynchronisedList(false);
			userList.setPartnerUserList(false);
			userList.setEmailValidationInd(true);
			CompanyProfile companyProfile = new CompanyProfile();
			companyProfile.setId(userDao.getCompanyIdByUserId(userId));
			userList.setCompany(companyProfile);
			userList.setPublicList(isPublic);
			userList.setUpdatedTime(new Date());
			userList.setUpdatedBy(userId);
			userList.setModuleName("CONTACTS");
			genericDAO.save(userList);
			genericDAO.saveContacts(userList, limit, userId, "contact");
			getExecutionTime(startTime, "addDummyContacts()");
		}
		return "Dummy Contacts Are Added Succssfully";

	}

	private long getStartTime() {
		return System.currentTimeMillis();
	}

	private void getExecutionTime(long startTime, String methodName) {
		long stopTime = getStartTime();
		long elapsedTime = stopTime - startTime;
		long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime);
		String debugMessage = methodName + " Completed In " + minutes + " minutes at " + new Date();
		logger.debug(debugMessage);
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public String addDummyPartners(Integer userId, Integer limit) {
		long startTime = getStartTime();
		Integer defaultPartnerListId = userListDao.getDefaultPartnerListId(userId);
		if (defaultPartnerListId != null && defaultPartnerListId > 0) {
			UserList userList = new UserList();
			userList.setId(defaultPartnerListId);
			CompanyProfile companyProfile = new CompanyProfile();
			companyProfile.setId(userDao.getCompanyIdByUserId(userId));
			userList.setCompany(companyProfile);
			userList.setModuleName("PARTNERS");
			genericDAO.saveContacts(userList, limit, userId, "partner");
			getExecutionTime(startTime, "addDummyPartners()");
			return "Dummy Partners Are Added Succssfully";
		} else {
			return " Default Partner List Not Found ";
		}
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public String addDummyLeads(Integer userId, Integer limit, String publicOrPrivate) {
		long startTime = getStartTime();
		String userListName = publicOrPrivate + "-Lead-" + limit + "(" + System.currentTimeMillis() + ")";
		UserList userList = new UserList();
		userList.setName(userListName);
		User createdUser = new User();
		createdUser.setUserId(userId);
		userList.setUploadedDate(new Date());
		userList.setCreatedTime(new Date());
		userList.setSocialNetwork(UserList.getSocialNetworkEnum("MANUAL"));
		userList.setContactType(UserList.getContactTypeEnum("CONTACT"));
		userList.setSynchronisedList(false);
		userList.setPartnerUserList(false);
		userList.setEmailValidationInd(true);
		CompanyProfile companyProfile = new CompanyProfile();
		companyProfile.setId(userDao.getCompanyIdByUserId(userId));
		userList.setCompany(companyProfile);
		userList.setPublicList("Public".equalsIgnoreCase(publicOrPrivate));
		userList.setUpdatedTime(new Date());
		userList.setUpdatedBy(userId);
		userList.setAssignedBy(createdUser);
		userList.setAssignedCompany(companyProfile);
		genericDAO.save(userList);
		genericDAO.saveContacts(userList, limit, userId, "lead");
		getExecutionTime(startTime, "addDummyLeads()");
		return "Dummy Leads Are Added Succssfully";

	}

	public boolean findUnsubscribeStatusByUserId(Integer userId) {
		Integer companyId = userDao.getCompanyIdByUserId(userId);
		return findUnsubscribeStatusByCompanyId(companyId);
	}

	public boolean findUnsubscribeStatusByCompanyId(Integer companyId) {
		GdprSettingView gdprSettingView = gdprSettingDao.getByCompanyId(companyId);
		boolean value = true;
		if (gdprSettingView != null) {
			value = gdprSettingView.isGdprStatus();
		}
		return value;
	}

	public String replaceUnsubscribeMergeTagForSendTestEmail(Integer userId, String updatedBody) {
		boolean isUnsubscribeLink = findUnsubscribeStatusByUserId(userId);
		if (isUnsubscribeLink) {
			updatedBody = updatedBody.replace(unsubscribeLinkMergeTag, defaultUnsubscribeLinkMergeTag);
		} else {
			updatedBody = updatedBody.replace(unsubscribeLinkMergeTag, "");
		}
		return updatedBody;
	}

	public void deleteThumbnailFromAWS(Integer id, Integer companyId, String type) {
		String fileName = id + ".png";
		AmazonWebModel amazonWebModel = new AmazonWebModel();
		amazonWebModel.setCompanyId(companyId);
		amazonWebModel.setCategory(type);
		List<String> awsFileKeys = new ArrayList<>();
		awsFileKeys.add(fileName);
		amazonWebModel.setAwsFileKeys(awsFileKeys);
		amazonWebService.deleteItem(amazonWebModel);
	}

	public User createNewUserWithOnlyEmailId(String emailId, Integer customerId) {
		User user = null;
		if (!StringUtils.isEmpty(emailId) && customerId != null) {
			user = new User();
			user.setEmailId(emailId.toLowerCase());
			user.setUserStatus(UserStatus.UNAPPROVED);
			user.setModulesDisplayType(ModulesDisplayType.LIST);
			user.getRoles().add(Role.USER_ROLE);
			user.initialiseCommonFields(true, customerId);
			genericDAO.save(user);
			if (user.getAlias() == null) {
				GenerateRandomPassword randomPassword = new GenerateRandomPassword();
				user.setAlias(randomPassword.getPassword());
			}
		}
		return user;
	}

	public LeftSideNavigationBarItem findModulesForTeamMemberGroup(Integer userId) {
		LeftSideNavigationBarItem leftSideNavigationBarItem = new LeftSideNavigationBarItem();
		RoleDisplayDTO roleDisplayDto = getRoleDetailsByUserId(userId);

		addPartnersOnly(leftSideNavigationBarItem, roleDisplayDto);

		addVideosAndDesignModule(leftSideNavigationBarItem, roleDisplayDto);

		addStatsModule(leftSideNavigationBarItem, roleDisplayDto);

		addShareLeadsModule(userId, leftSideNavigationBarItem);

		addSharedLeadsModule(userId, leftSideNavigationBarItem);

		addEnableLeadsModule(userId, leftSideNavigationBarItem);

		addDamModule(userId, leftSideNavigationBarItem);

		addLmsModule(userId, leftSideNavigationBarItem);

		addPlaybookModule(userId, leftSideNavigationBarItem);

		return leftSideNavigationBarItem;
	}

	private void addPartnersOnly(LeftSideNavigationBarItem leftSideNavigationBarItem, RoleDisplayDTO roleDisplayDto) {
		boolean notPartner = !roleDisplayDto.isPartner();
		leftSideNavigationBarItem.setPartners(notPartner);
	}

	private void addVideosAndDesignModule(LeftSideNavigationBarItem leftSideNavigationBarItem,
			RoleDisplayDTO roleDisplayDto) {
		boolean notPartnerAndPrm = !roleDisplayDto.isPartner() && !roleDisplayDto.isPrm()
				&& !roleDisplayDto.isPrmAndPartner();
		leftSideNavigationBarItem.setVideos(notPartnerAndPrm);
		leftSideNavigationBarItem.setDesign(!roleDisplayDto.isPartner());
	}

	private void addStatsModule(LeftSideNavigationBarItem leftSideNavigationBarItem, RoleDisplayDTO roleDisplayDto) {
		boolean statsModule = !roleDisplayDto.isPartner();
		leftSideNavigationBarItem.setStats(statsModule);
	}

	private void addShareLeadsModule(Integer userId, LeftSideNavigationBarItem leftSideNavigationBarItem) {
		boolean shareLeadsModule = utilDao.hasShareLeadsAccessByUserId(userId);
		leftSideNavigationBarItem.setShareLeads(shareLeadsModule);

	}

	private void addSharedLeadsModule(Integer userId, LeftSideNavigationBarItem leftSideNavigationBarItem) {
		boolean sharedLeadsModule = utilDao.sharedLeadsAccessForPartner(userId);
		leftSideNavigationBarItem.setSharedLeads(sharedLeadsModule);
	}

	private void addEnableLeadsModule(Integer userId, LeftSideNavigationBarItem leftSideNavigationBarItem) {
		boolean enableLeadsModule = utilDao.hasEnableLeadsAccessByUserId(userId)
				|| utilDao.enableLeadsForPartner(userId);
		leftSideNavigationBarItem.setEnableLeads(enableLeadsModule);
	}

	private void addPlaybookModule(Integer userId, LeftSideNavigationBarItem leftSideNavigationBarItem) {
		boolean playbook = utilDao.hasPlaybookAccessByUserId(userId);
		boolean playbookAccessAsPartner = utilDao.playbookAccessForPartner(userId);

		leftSideNavigationBarItem.setPlaybook(playbook);
		leftSideNavigationBarItem.setPlaybookAccessAsPartner(playbookAccessAsPartner);
	}

	private void addLmsModule(Integer userId, LeftSideNavigationBarItem leftSideNavigationBarItem) {
		boolean lms = utilDao.hasLmsAccessByUserId(userId);
		boolean lmsAccessAsPartner = utilDao.lmsAccessForPartner(userId);
		leftSideNavigationBarItem.setLms(lms);
		leftSideNavigationBarItem.setLmsAccessAsPartner(lmsAccessAsPartner);
	}

	private void addDamModule(Integer userId, LeftSideNavigationBarItem leftSideNavigationBarItem) {
		boolean dam = utilDao.hasDamAccessByUserId(userId);
		boolean damAccessAsPartner = utilDao.damAccessForPartner(userId);
		leftSideNavigationBarItem.setDam(dam);
		leftSideNavigationBarItem.setDamAccessAsPartner(damAccessAsPartner);
	}

	public boolean isPartner(User user) {
		boolean isPartner = false;
		if (user != null) {
			List<Integer> roleIdsList = user.getRoles().stream().map(Role::getRoleId).collect(Collectors.toList());
			isPartner = roleIdsList != null && !roleIdsList.isEmpty()
					&& roleIdsList.indexOf(Role.COMPANY_PARTNER.getRoleId()) > -1;
		}
		return isPartner;
	}

	public void addPartnerRole(User user) {
		if (user != null && !isPartner(user)) {
			user.getRoles().add(Role.COMPANY_PARTNER);
		}
	}

	/****************************
	 * XNFR-2
	 ****************************/
	public void addRoleToSuperVisorGroupsAndTeamMembers(Integer companyId, Integer roleId,
			ModulesEmailNotification modulesEmailNotification) {
		List<TeamMemberGroupDTO> teamMemberGroupDtos = teamMemberGroupDao
				.findTeamMemberGroupIdsAndNamesByRoleIdAndCompanyId(companyId, Role.ALL_ROLES.getRoleId());
		addRoleToTeamMemberGroupsAndTeamMembers(companyId, roleId, modulesEmailNotification, teamMemberGroupDtos);
	}

	public void addRoleToTeamMemberGroupsAndTeamMembers(Integer companyId, Integer roleId,
			ModulesEmailNotification modulesEmailNotification, List<TeamMemberGroupDTO> teamMemberGroupDtos) {
		addRoleToTeamMemberGroups(roleId, teamMemberGroupDtos, modulesEmailNotification);
		addRolesToTeamMembers(companyId, roleId, modulesEmailNotification);
	}

	public void addRolesToTeamMembers(Integer companyId, Integer roleId,
			ModulesEmailNotification modulesEmailNotification) {
		List<Integer> teamMemberUserIds = teamMemberGroupDao.findTeamMemberUserIdsByRoleIdAndCompanyId(companyId,
				Role.ALL_ROLES.getRoleId());
		for (Integer teamMemberId : teamMemberUserIds) {
			boolean roleExists = teamDao.roleExists(roleId, teamMemberId);
			String roleName = utilDao.findModuleByRoleId(roleId);
			String teamMemberEmailId = userDao.getEmailIdByUserId(teamMemberId);
			String message = "";
			if (!roleExists) {
				teamDao.addNewRole(teamMemberId, roleId);
				message = roleName + MODULE_ADDED_PREFIX + teamMemberEmailId;
				addDebugInfoAndAppendNotification(modulesEmailNotification, message);
			}
		}
	}

	public void addDebugInfoAndAppendNotification(ModulesEmailNotification modulesEmailNotification, String message) {
		logger.debug(message);
		if (modulesEmailNotification != null) {
			modulesEmailNotification.getAddedTeamMemberEmailIdsNotifications().add(message);
		}
	}

	public void addRoleToTeamMemberGroups(Integer roleId, List<TeamMemberGroupDTO> teamMemberGroupDtos,
			ModulesEmailNotification moduleEmailNotification) {
		if (teamMemberGroupDtos != null && !teamMemberGroupDtos.isEmpty()) {
			for (TeamMemberGroupDTO teamMemberGroupDto : teamMemberGroupDtos) {
				Integer teamMemberGroupId = teamMemberGroupDto.getId();
				boolean isRowExists = teamMemberGroupDao.isTeamMemberGroupRoleMappingRowExists(teamMemberGroupId,
						roleId);
				String moduleName = utilDao.findModuleByRoleId(roleId);
				String teamMemberGroupName = teamMemberGroupDto.getName();
				String messageSuffix = teamMemberGroupName + "(" + teamMemberGroupId + ")";
				String message = "";
				if (!isRowExists) {
					TeamMemberGroupRoleMapping teamMemberGroupRoleMapping = new TeamMemberGroupRoleMapping();
					TeamMemberGroup teamMemberGroup = new TeamMemberGroup();
					teamMemberGroup.setId(teamMemberGroupId);
					teamMemberGroupRoleMapping.setTeamMemberGroup(teamMemberGroup);
					teamMemberGroupRoleMapping.setCreatedTime(new Date());
					teamMemberGroupRoleMapping.setRoleId(roleId);
					genericDAO.save(teamMemberGroupRoleMapping);
					message = moduleName + MODULE_ADDED_PREFIX + messageSuffix;
					addDebugInfoAndAppendNotification(moduleEmailNotification, message);
				}
			}

		}
	}

	public void removeRoleFromTeamMemberGroupRoleMappingAndTeamMember(Integer companyId, Integer roleId,
			ModulesEmailNotification modulesEmailNotification) {
		removeRoleForTeamMember(companyId, roleId, modulesEmailNotification);
		removeRoleFromTeamMemberGroupRoleMapping(companyId, roleId, modulesEmailNotification);

	}

	private void removeRoleFromTeamMemberGroupRoleMapping(Integer companyId, Integer roleId,
			ModulesEmailNotification modulesEmailNotification) {
		List<TeamMemberGroupDTO> teamMemberGroupRoleMappingDtos = teamMemberGroupDao
				.findTeamMemberGroupRoleMappingIdsAndNamesByRoleIdAndCompanyId(companyId, roleId);
		if (teamMemberGroupRoleMappingDtos != null && !teamMemberGroupRoleMappingDtos.isEmpty()) {
			List<Integer> teamMemberGroupRoleMappingIds = teamMemberGroupRoleMappingDtos.stream()
					.map(TeamMemberGroupDTO::getMappingId).collect(Collectors.toList());
			teamMemberGroupDao.deleteFromTeamMemberGroupRoleMapping(teamMemberGroupRoleMappingIds);
			for (TeamMemberGroupDTO teamMemberGroupRoleMappingDto : teamMemberGroupRoleMappingDtos) {
				String teamMemberGroupName = teamMemberGroupRoleMappingDto.getName();
				Integer id = teamMemberGroupRoleMappingDto.getId();
				String moduleName = utilDao.findModuleByRoleId(roleId);
				String message = moduleName + " module is removed from " + teamMemberGroupName + "(" + id + ")";
				logger.debug(message);
				if (modulesEmailNotification != null) {
					modulesEmailNotification.getRemovedTeamMemberGroupsNotifications().add(message);
				}
			}
		}

	}

	private void removeRoleForTeamMember(Integer companyId, Integer roleId,
			ModulesEmailNotification modulesEmailNotification) {
		List<Integer> teamMemberUserIds = teamMemberGroupDao.findTeamMemberUserIdsByRoleIdAndCompanyId(companyId,
				roleId);
		Set<Integer> roleIds = new HashSet<>();
		roleIds.add(roleId);
		teamDao.deleteUnMappedRoleIds(teamMemberUserIds, roleIds);
		for (Integer teamMemberId : teamMemberUserIds) {
			String message = utilDao.findModuleByRoleId(roleId) + " module is removed for "
					+ userDao.getEmailIdByUserId(teamMemberId);
			logger.debug(message);
			if (modulesEmailNotification != null) {
				modulesEmailNotification.getRemovedTeamMemberEmailIdsNotifications().add(message);
			}
		}

	}

	public void addRolesToDefaultGroups(List<Integer> moduleIds, Integer companyId,
			ModulesEmailNotification modulesEmailNotification) {
		if (!moduleIds.isEmpty()) {
			List<TeamMemberGroupDTO> defaultGroups = teamMemberGroupDao.findDefaultGroupsByCompanyId(companyId);
			for (TeamMemberGroupDTO teamMemberGroupDto : defaultGroups) {
				String groupName = teamMemberGroupDto.getName();
				List<Integer> existingRoleIds = teamMemberGroupDao
						.findRoleIdsByTeamMemberGroupId(teamMemberGroupDto.getId());

				List<Integer> teamMemberUserIds = teamMemberGroupDao
						.findTeamMemberUserIdsByGroupId(teamMemberGroupDto.getId());

				/****** PRM Account Manager *******/
				addRolesToPRMAccountManager(teamMemberGroupDto, groupName, existingRoleIds, moduleIds,
						teamMemberUserIds, modulesEmailNotification);

			}

		}
	}

	private void addRolesToPRMAccountManager(TeamMemberGroupDTO teamMemberGroupDTO, String groupName,
			List<Integer> existingRoleIds, List<Integer> moduleIds, List<Integer> teamMemberUserIds,
			ModulesEmailNotification moduleEmailNotification) {
		if ("PRM Account Manager".equals(groupName)) {

			/******* Add Opportunity *********/
			addModuleToDefaultGroup(moduleIds.indexOf(Role.OPPORTUNITY.getRoleId()) > -1, teamMemberGroupDTO,
					existingRoleIds, Role.OPPORTUNITY.getRoleId(), teamMemberUserIds, moduleEmailNotification);

			/******* Add MDF *********/
			addModuleToDefaultGroup(moduleIds.indexOf(Role.MDF.getRoleId()) > -1, teamMemberGroupDTO, existingRoleIds,
					Role.MDF.getRoleId(), teamMemberUserIds, moduleEmailNotification);

			/******* Add DAM *********/
			addModuleToDefaultGroup(moduleIds.indexOf(Role.DAM.getRoleId()) > -1, teamMemberGroupDTO, existingRoleIds,
					Role.DAM.getRoleId(), teamMemberUserIds, moduleEmailNotification);

			/******* Add Learning Tracks *********/
			addModuleToDefaultGroup(moduleIds.indexOf(Role.LEARNING_TRACK.getRoleId()) > -1, teamMemberGroupDTO,
					existingRoleIds, Role.LEARNING_TRACK.getRoleId(), teamMemberUserIds, moduleEmailNotification);

			/******* Add PlayBook *********/
			addModuleToDefaultGroup(moduleIds.indexOf(Role.PLAY_BOOK.getRoleId()) > -1, teamMemberGroupDTO,
					existingRoleIds, Role.PLAY_BOOK.getRoleId(), teamMemberUserIds, moduleEmailNotification);

		}
	}

	private void addModuleToDefaultGroup(boolean module, TeamMemberGroupDTO teamMemberGroupDto,
			List<Integer> existingRoleIds, Integer roleId, List<Integer> teamMemberUserIds,
			ModulesEmailNotification moduleEmailNotification) {
		boolean moduleDoesNotExists = true;
		if (existingRoleIds != null && !existingRoleIds.isEmpty()) {
			moduleDoesNotExists = existingRoleIds.indexOf(roleId) < 0;
		}
		if (module && moduleDoesNotExists) {
			addRolesToDefaultGroup(teamMemberGroupDto, roleId, moduleEmailNotification);
			addRolesToDefaultGroupTeamMembers(roleId, teamMemberUserIds, moduleEmailNotification);

		}
	}

	private void addRolesToDefaultGroup(TeamMemberGroupDTO teamMemberGroupDTO, Integer roleId,
			ModulesEmailNotification moduleEmailNotification) {
		TeamMemberGroupRoleMapping teamMemberGroupRoleMapping = new TeamMemberGroupRoleMapping();
		teamMemberGroupRoleMapping.setRoleId(roleId);
		teamMemberGroupRoleMapping.setCreatedTime(new Date());
		TeamMemberGroup teamMemberGroup = new TeamMemberGroup();
		teamMemberGroup.setId(teamMemberGroupDTO.getId());
		teamMemberGroupRoleMapping.setTeamMemberGroup(teamMemberGroup);
		genericDAO.save(teamMemberGroupRoleMapping);
		String moduleName = utilDao.findModuleByRoleId(roleId);
		String teamMemberGroupName = teamMemberGroupDTO.getName();
		String messageSuffix = teamMemberGroupName + "(" + teamMemberGroupDTO.getId() + ")";
		String message = moduleName + MODULE_ADDED_PREFIX + messageSuffix;
		addDebugInfoAndAppendNotification(moduleEmailNotification, message);
	}

	private void addRolesToDefaultGroupTeamMembers(Integer roleId, List<Integer> teamMemberUserIds,
			ModulesEmailNotification modulesEmailNotification) {
		for (Integer teamMemberId : teamMemberUserIds) {
			boolean roleExists = teamDao.roleExists(roleId, teamMemberId);
			String roleName = utilDao.findModuleByRoleId(roleId);
			String teamMemberEmailId = userDao.getEmailIdByUserId(teamMemberId);
			String message = "";
			if (!roleExists) {
				teamDao.addNewRole(teamMemberId, roleId);
				message = roleName + MODULE_ADDED_PREFIX + teamMemberEmailId;
				addDebugInfoAndAppendNotification(modulesEmailNotification, message);
			}
		}
	}

	public Form getFormByUrl(String url) {
		Form form = null;
		String formAlias = url.substring(url.lastIndexOf('/') + 1);
		if (!StringUtils.isEmpty(formAlias)) {
			form = formDao.getByAlias(formAlias);
		}
		return form;
	}

	public boolean isFormUrl(String hrefLink) {
		boolean isFormUrl = false;
		Form form = getFormByUrl(hrefLink);
		// && FormSubTypeEnum.SURVEY == form.getFormSubTypeEnum()
		if (form != null) {
			isFormUrl = true;
		}
		return isFormUrl;
	}

	public boolean isSurveyFormUrl(String hrefLink) {
		boolean isSurveyFormUrl = false;
		if (!StringUtils.isEmpty(hrefLink) && hrefLink.contains("/f/")) {
			Form form = getFormByUrl(hrefLink);
			if (form != null && FormSubTypeEnum.SURVEY == form.getFormSubTypeEnum()) {
				isSurveyFormUrl = true;
			}
		}
		return isSurveyFormUrl;
	}

	/**** XNFR-81 ********/
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse addActiveAndInActiveMasterPartnerLists() {
		XtremandResponse response = new XtremandResponse();
		logger.debug("****************D o n e****************");
		return response;
	}

	/**** XNFR-81 ********/
	private void addActiveAndInactiveMasterList(CompanyAndRolesDTO companyAndRolesDTO, XtremandResponse response) {
		Integer companyId = companyAndRolesDTO.getCompanyId();
		Integer userId = companyAndRolesDTO.getUserId();
		CompanyProfile companyProfile = new CompanyProfile();
		companyProfile.setId(companyId);
		User partner = new User();
		partner.setUserId(companyAndRolesDTO.getUserId());

		UserList activeMasterPartnerList = new UserList();
		activeMasterPartnerList.setName(activePartnerListName);

		UserList inactiveMasterPartnerList = new UserList();
		inactiveMasterPartnerList.setName(inActivePartnerListName);

		createActiveOrInactiveMasterPartnerList(userId, companyProfile, partner, activeMasterPartnerList);
		createActiveOrInactiveMasterPartnerList(userId, companyProfile, partner, inactiveMasterPartnerList);

		List<Integer> partnerIds = partnershipDao.findPartnerIdsByVendorCompanyId(companyId);
		Set<UserUserList> activeUserUserLists = new HashSet<>();
		Set<UserUserList> inactiveUserUserLists = new HashSet<>();
		iteratePartners(companyId, activeMasterPartnerList, inactiveMasterPartnerList, partnerIds, activeUserUserLists,
				inactiveUserUserLists);

		activeMasterPartnerList.setUserUserLists(activeUserUserLists);
		inactiveMasterPartnerList.setUserUserLists(inactiveUserUserLists);

		boolean activeMasterPartnerListExists = userListDao.isActiveMasterPartnerListExists(companyId);

		boolean inActiveMasterPartnerListExits = userListDao.isInActiveMasterPartnerListExists(companyId);

		if (!activeMasterPartnerListExists) {
			genericDAO.save(activeMasterPartnerList);
		}

		if (!inActiveMasterPartnerListExits) {
			genericDAO.save(inactiveMasterPartnerList);

		}

		logger.debug("********************************");

		String message = "";
		if (!activeMasterPartnerListExists && !inActiveMasterPartnerListExits) {
			message = activeMasterPartnerList.getName() + " [ " + activeMasterPartnerList.getId() + " ] & "
					+ inactiveMasterPartnerList.getName() + " [ " + inactiveMasterPartnerList.getId()
					+ " ] are created for " + companyAndRolesDTO.getCompanyName() + " (" + companyId + ")";

		}

		if (!activeMasterPartnerListExists) {
			message = activeMasterPartnerList.getName() + " [ " + activeMasterPartnerList.getId() + " ] "
					+ " is created for " + companyAndRolesDTO.getCompanyName() + " (" + companyId + ")";
		}

		if (!inActiveMasterPartnerListExits) {
			message = inactiveMasterPartnerList.getName() + " [ " + inactiveMasterPartnerList.getId()
					+ " ] is created for " + companyAndRolesDTO.getCompanyName() + " (" + companyId + ")";

		}

		if (activeMasterPartnerListExists && inActiveMasterPartnerListExits) {
			message = "No list created";
		}

		logger.debug(message);

		logger.debug("********************************");

		response.setStatusCode(200);
		response.setMessage(message);
	}

	/**** XNFR-81 ********/
	private void iteratePartners(Integer companyId, UserList activeMasterPartnerList,
			UserList inactiveMasterPartnerList, List<Integer> partnerIds, Set<UserUserList> activeUserUserLists,
			Set<UserUserList> inactiveUserUserLists) {
		for (Integer partnerId : partnerIds) {
			boolean isActivePartner = utilDao.isActivePartner(partnerId, companyId);
			UserUserListDTO userListDTO = utilDao.findPartnerInfoFromDefaultPartnerList(partnerId, companyId);
			if (userListDTO != null) {
				User user = new User();
				user.setUserId(partnerId);
				if (isActivePartner) {
					addActiveOrInActiveUserUserListData(activeMasterPartnerList, activeUserUserLists, userListDTO,
							user);
				} else {
					addActiveOrInActiveUserUserListData(inactiveMasterPartnerList, inactiveUserUserLists, userListDTO,
							user);
				}
			}

		}
	}

	/**** XNFR-81 ********/
	private void addActiveOrInActiveUserUserListData(UserList activeMasterPartnerList, Set<UserUserList> userUserLists,
			UserUserListDTO userListDTO, User user) {
		UserUserList userUserList = new UserUserList();
		userUserList.setUser(user);
		userUserList.setUserList(activeMasterPartnerList);
		userUserList.setCountry(userListDTO.getCountry());
		userUserList.setCity(userListDTO.getCity());
		userUserList.setAddress(userListDTO.getAddress());
		userUserList.setContactCompany(userListDTO.getContactCompany());
		userUserList.setJobTitle(userListDTO.getJobTitle());
		userUserList.setFirstName(userListDTO.getFirstName());
		userUserList.setLastName(userListDTO.getLastName());
		userUserList.setMobileNumber(userListDTO.getMobileNumber());
		userUserList.setState(userListDTO.getState());
		userUserList.setZipCode(userListDTO.getZip());
		userUserList.setVertical(userListDTO.getVertical());
		userUserList.setRegion(userListDTO.getRegion());
		userUserList.setPartnerType(userListDTO.getPartnerType());
		userUserList.setCategory(userListDTO.getCategory());
		List<LegalBasis> legalBasisList = new ArrayList<>();
		for (Integer legalBasisId : userListDTO.getLegalBasis()) {
			LegalBasis legalBasis = new LegalBasis();
			legalBasis.setId(legalBasisId);
			legalBasisList.add(legalBasis);
		}
		userUserList.setLegalBasis(legalBasisList);
		userUserLists.add(userUserList);
	}

	/**** XNFR-81 ********/
	private void createActiveOrInactiveMasterPartnerList(Integer userId, CompanyProfile companyProfile, User user,
			UserList userList) {

		setUserList(userId, user, companyProfile, userList);

	}

	/**** XNFR-81 ********/
	public void addActiveAndInActiveMasterPartnerListsByCompanyId(Integer companyId, Integer userId) {
		User user = new User();
		user.setUserId(userId);
		CompanyProfile companyProfile = new CompanyProfile();
		companyProfile.setId(companyId);
		addActiveOrInActiveMasterPartnerList(companyId, userId, user, companyProfile, activePartnerListName);
		addActiveOrInActiveMasterPartnerList(companyId, userId, user, companyProfile, inActivePartnerListName);

	}

	/**** XNFR-81 ********/
	private void addActiveOrInActiveMasterPartnerList(Integer companyId, Integer userId, User user,
			CompanyProfile companyProfile, String userListName) {
		UserList userList = new UserList();
		userList.setName(userListName);

		setUserList(userId, user, companyProfile, userList);

		genericDAO.save(userList);
		String messag = userList.getName() + " is created for " + companyId;
		logger.debug(messag);
	}

	/**** XNFR-81 ********/
	private void setUserList(Integer userId, User user, CompanyProfile companyProfile, UserList userList) {
		userList.setOwner(user);
		userList.setCompany(companyProfile);
		userList.setUpdatedBy(userId);
		userList.setCreatedTime(new Date());
		userList.setUpdatedTime(new Date());
		userList.setSocialNetwork(SocialNetwork.MANUAL);
		userList.setContactType(TYPE.CONTACT);
		userList.setPartnerUserList(true);
		userList.setDefaultPartnerList(false);
		userList.setEmailValidationInd(true);
		userList.setModuleName("PARTNERS");
		userList.setPublicList(true);
	}

	/**** XNFR-81 ********/
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse addActiveAndInActiveMasterPartnerListsByCompanyId(Integer companyId) {
		XtremandResponse response = new XtremandResponse();
		CompanyAndRolesDTO companyAndRolesDTO = teamDao.findCompanyDetailsAndUserId(companyId);
		addActiveAndInactiveMasterList(companyAndRolesDTO, response);
		logger.debug("****************D o n e****************");

		return response;
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse addActiveAndInactivePartners(Integer companyId) {
		XtremandResponse response = new XtremandResponse();
		UserList activeMasterPartnerList = userListDao.findActivePartnerListByCompanyId(companyId);
		UserList inactiveMasterPartnerList = userListDao.findInActivePartnerListByCompanyId(companyId);
		List<Integer> partnerIds = partnershipDao.findPartnerIdsByVendorCompanyId(companyId);
		for (Integer partnerId : partnerIds) {
			insertIntoUserUserList(companyId, activeMasterPartnerList, inactiveMasterPartnerList, partnerId);
		}
		String message = "success";
		response.setStatusCode(200);
		;
		response.setMessage(message);
		return response;
	}

	private void insertIntoUserUserList(Integer companyId, UserList activeMasterPartnerList,
			UserList inactiveMasterPartnerList, Integer partnerId) {
		boolean isActivePartner = utilDao.isActivePartner(partnerId, companyId);
		UserUserListDTO userListDTO = utilDao.findPartnerInfoFromDefaultPartnerList(partnerId, companyId);
		if (userListDTO != null) {
			User user = new User();
			user.setUserId(partnerId);
			if (isActivePartner) {
				boolean isPartnerAlreadyExistsInActivePartnerList = utilDao.isPartnerAddedToActivePartnerList(partnerId,
						companyId);
				if (!isPartnerAlreadyExistsInActivePartnerList) {
					setUserUserListDataForActiveAndInActivePartners(activeMasterPartnerList, userListDTO, user);
				}

			} else {
				boolean isPartnerAlreadyExistsInInActivePartnerList = utilDao
						.isPartnerAddedToInActivePartnerList(partnerId, companyId);
				if (!isPartnerAlreadyExistsInInActivePartnerList) {
					setUserUserListDataForActiveAndInActivePartners(inactiveMasterPartnerList, userListDTO, user);
				}
			}
		}
	}

	private void setUserUserListDataForActiveAndInActivePartners(UserList userList, UserUserListDTO userListDTO,
			User user) {
		userListDao.saveUserUserList(userList, userListDTO, user);
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse addCustomModuleNames() {
		XtremandResponse response = new XtremandResponse();
		List<Integer> companyIds = userDao.findAllCompanyIds();
		List<Module> moduleNames = moduleDao.findModuleNames();
		List<ModuleCustom> moduleCustomNames = new ArrayList<ModuleCustom>();
		for (Integer companyId : companyIds) {

			for (Module moduleName : moduleNames) {
				ModuleCustom moduleCustomName = new ModuleCustom();
				moduleCustomName.setCustomName(moduleName.getModuleName());
				moduleCustomName.setModule(moduleName);
				moduleCustomName.setCompanyId(companyId);
				moduleCustomName.setCreatedTime(new Date());
				moduleCustomName.setCreatedUserId(1);
				moduleCustomName.setUpdatedTime(new Date());
				moduleCustomName.setUpdatedUserId(1);
				moduleCustomNames.add(moduleCustomName);
			}

		}
		utilDao.saveAll(moduleCustomNames);
		response.setStatusCode(200);
		response.setMessage("Module Names are added succesfully");
		return response;
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void addCustomModuleNamesByCompanyId(Integer companyId, Integer userId) {
		List<Module> modules = moduleDao.findModuleNames();
		if (modules != null) {
			List<ModuleCustom> moduleCustoms = frameModuleCustoms(companyId, null, modules);
			List<ModuleCustom> moduleCustomsForVendors = getModuleCustomsForVendors(userId, companyId, modules);
			if (moduleCustomsForVendors != null) {
				moduleCustoms.addAll(moduleCustomsForVendors);
			}
			utilDao.saveAll(moduleCustoms);
		}
	}

	public List<ModuleCustom> getModuleCustomsForVendors(Integer userId, Integer companyId, List<Module> modules) {
		List<Partnership> partnerships = partnershipDao.getPartnershipsByPartnerId(userId);
		List<ModuleCustom> moduleCustoms = new ArrayList<>();
		if (partnerships != null && !partnerships.isEmpty()) {
			for (Partnership partnership : partnerships) {
				List<ModuleCustom> moduleCustomsForVendor = getModuleCustomsForVendor(partnership, companyId, modules);
				if (moduleCustomsForVendor != null) {
					moduleCustoms.addAll(moduleCustomsForVendor);
				}
			}
		}
		return moduleCustoms;
	}

	public List<ModuleCustom> getModuleCustomsForVendor(Partnership partnership, Integer companyId,
			List<Module> modules) {
		List<ModuleCustom> moduleCustoms = null;
		if (partnership != null) {
			boolean isVanity = utilDao.hasVanityAccessByCompanyId(partnership.getVendorCompany().getId());
			if (isVanity) {
				moduleCustoms = frameModuleCustoms(companyId, partnership, modules);
			}
		}
		return moduleCustoms;
	}

	public List<ModuleCustom> frameModuleCustoms(Integer companyId, Partnership partnership, List<Module> modules) {
		List<ModuleCustom> moduleCustoms = new ArrayList<>();
		for (Module module : modules) {
			Integer partnershipId = (partnership != null) ? partnership.getId() : null;
			if (!moduleDao.checkModuleCustom(partnershipId, companyId, module.getId())) {
				ModuleCustom moduleCustom = new ModuleCustom();
				if (module.getParentModule() == null) {
					moduleCustom.setCustomName(module.getModuleName());
					moduleCustom.setModule(module);
					moduleCustom.setCompanyId(companyId);
					moduleCustom.setCreatedTime(new Date());
					moduleCustom.setCreatedUserId(1);
					moduleCustom.setUpdatedTime(new Date());
					moduleCustom.setDisplayIndex(moduleDisplayIndexMap.get(module.getId()));
					moduleCustom.setUpdatedUserId(1);
					moduleCustom.setPartnership(partnership);
					moduleCustom.setCanPartnerAccessModule(true);
					moduleCustom.setMarketingModule(false);
					moduleCustoms.add(moduleCustom);
				}
			}
		}
		return moduleCustoms;
	}

	public List<ModuleCustom> frameModuleCustomsForNewModules(Integer companyId, Partnership partnership,
			List<Module> modules, Integer moduleId) {
		List<ModuleCustom> moduleCustoms = new ArrayList<>();
		for (Module module : modules) {
			Integer partnershipId = (partnership != null) ? partnership.getId() : null;
			if (!moduleDao.checkModuleCustom(partnershipId, companyId, module.getId())) {
				ModuleCustom moduleCustom = new ModuleCustom();
				if (module.getParentModule() == null && Objects.equals(module.getModuleId(), moduleId)) {
					moduleCustom.setCustomName(module.getModuleName());
					moduleCustom.setModule(module);
					moduleCustom.setCompanyId(companyId);
					moduleCustom.setCreatedTime(new Date());
					moduleCustom.setCreatedUserId(1);
					moduleCustom.setUpdatedTime(new Date());
					moduleCustom.setDisplayIndex(moduleDisplayIndexMap.get(module.getId()));
					moduleCustom.setUpdatedUserId(1);
					moduleCustom.setPartnership(partnership);
					moduleCustom.setCanPartnerAccessModule(true);
					moduleCustom.setMarketingModule(false);
					moduleCustoms.add(moduleCustom);
				}
			}
		}
		return moduleCustoms;
	}

	public String findPartnerModuleCustomName(Integer userId) {
		Integer companyId = userDao.getCompanyIdByUserId(userId);
		if (companyId != null && companyId > 0) {
			ModuleCustomDTO moduleCustomNameDTO = moduleDao.findPartnerModuleByCompanyId(companyId);
			if (moduleCustomNameDTO != null) {
				return moduleCustomNameDTO.getCustomName();
			} else {
				return PARTNERS;
			}
		} else {
			return PARTNERS;
		}
	}

	public String findPartnerModuleCustomNameByCompanyId(Integer companyId) {
		ModuleCustomDTO moduleCustomNameDTO = moduleDao.findPartnerModuleByCompanyId(companyId);
		if (moduleCustomNameDTO != null) {
			return moduleCustomNameDTO.getCustomName();
		} else {
			return PARTNERS;
		}
	}

	public XtremandResponse showPartnersFilter(Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		boolean isTeamMember = teamDao.isTeamMember(loggedInUserId);
		response.setData(isTeamMember);
		response.setStatusCode(200);
		return response;
	}

	public void setEmailValidationByZeroBounce(User user) {
		JSONObject jsonObject;
		try {
			jsonObject = emailValidatorService.validate(user.getEmailId(), null, "", 1, 1);
			if (jsonObject.has("error")) {
				user.setEmailValid(false);
				user.setEmailValidationInd(false);
			} else {
				String status = jsonObject.getString("status");
				user.setEmailValid(
						(status.equalsIgnoreCase("valid") || status.equalsIgnoreCase("catch-all")) ? true : false);
				user.setEmailValidationInd(true);
				user.setEmailCategory(status);
			}
		} catch (ParseException | IOException e) {
			user.setEmailValid(false);
			user.setEmailValidationInd(false);
		}

	}

	public void setDateFilters(Pagination pagination) {
		if (!StringUtils.isEmpty(pagination.getTimeZone())) {
			if (!StringUtils.isEmpty(pagination.getFromDateFilterString())) {
				pagination.setFromDateFilter(DateUtils.convertClientToServerTimeZone(
						pagination.getFromDateFilterString() + " 00:00:00", pagination.getTimeZone()));
			}

			if (!StringUtils.isEmpty(pagination.getToDateFilterString())) {
				Date toDate = new Date();
				toDate = DateUtils.convertClientToServerTimeZone(pagination.getToDateFilterString() + " 23:59:59",
						pagination.getTimeZone());
				Calendar c = Calendar.getInstance();
				c.setTime(toDate);
				c.add(Calendar.MINUTE, 1);
				toDate = c.getTime();
				pagination.setToDateFilter(toDate);
			}
		}
	}

	public String getFullName(String firstName, String lastName) {
		return XamplifyUtility.getFullName(firstName, lastName);
	}

	public void revokeAccessTokensByCompanyId(Integer companyId) {
		List<String> emailIds = userDao.findEmailIdsByCompanyId(companyId);
		iterateAndRevokeAccessTokensByEmailAddresses(emailIds);
	}

	private void iterateAndRevokeAccessTokensByEmailAddresses(List<String> emailIds) {
		if (XamplifyUtils.isNotEmptyList(emailIds)) {
			for (String emailId : emailIds) {
				if (StringUtils.hasText(emailId)) {
					Collection<OAuth2AccessToken> tokens = tokenStore
							.findTokensByClientIdAndUserName(xamplifyRestClientId, emailId);
					iterateAndRevokeTokens(tokens);
				}
			}
		}
	}

	private void iterateAndRevokeTokens(Collection<OAuth2AccessToken> tokens) {
		if (tokens != null && !tokens.isEmpty()) {
			for (OAuth2AccessToken token : tokens) {
				String tokenValue = token.getValue();
				if (StringUtils.hasText(tokenValue)) {
					tokenServices.revokeToken(tokenValue);
				}
			}
		}
	}

	public XtremandResponse partnershipEstablishedOnlyWithPrm(Integer userId) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(200);
		boolean isPrmCompany = utilDao.isPrmCompany(userId);
		boolean partnershipWithPrm = utilDao.isPartnershipEstablishedOnlyWithPrm(userId);
		response.setData(isPrmCompany && partnershipWithPrm);
		return response;
	}

	/************** XNFR-125 ************/
	public ShareListPartner saveShareListPartner(UserList userList, Integer partnershipId, boolean sharedToCompany) {
		ShareListPartner shareListPartner = new ShareListPartner();
		shareListPartner.setUserList(userList);
		Partnership partnership = new Partnership();
		partnership.setId(partnershipId);
		shareListPartner.setPartnership(partnership);
		shareListPartner.setCreatedTime(new Date());
		shareListPartner.setSharedToCompany(sharedToCompany);
		genericDAO.save(shareListPartner);
		return shareListPartner;
	}

	/************** XNFR-125 ************/
	public void saveShareListPartnerMapping(Set<Integer> partnerIds, ShareListPartner shareListPartner,
			boolean doCheck) {
		for (Integer partnerId : partnerIds) {
			if (doCheck) {
				checkListAndInsert(shareListPartner, partnerId);
			} else {
				insertIntoShareListPartnerMapping(shareListPartner, partnerId);
			}

		}
	}

	/************** XNFR-125 ************/
	private void checkListAndInsert(ShareListPartner shareListPartner, Integer partnerId) {
		boolean isListSharedToPartner = utilDao.isShareListSharedByListIdAndPartnerId(shareListPartner.getId(),
				partnerId);
		if (!isListSharedToPartner) {
			insertIntoShareListPartnerMapping(shareListPartner, partnerId);
		}
	}

	/************** XNFR-125 ************/
	private void insertIntoShareListPartnerMapping(ShareListPartner shareListPartner, Integer partnerId) {
		ShareListPartnerMapping shareListPartnerMapping = new ShareListPartnerMapping();
		shareListPartnerMapping.setPartnerId(partnerId);
		shareListPartnerMapping.setShareListPartner(shareListPartner);
		genericDAO.save(shareListPartnerMapping);
	}

	/************** XNFR-125 ************/
	public void shareListsToPartnerCompany(Integer partnershipId, List<Integer> userListIds, User partner) {
		List<Integer> partnerIds = userDao.findAdminAndApprovedOrDeclinedOrSupspendedTeamMemberIdsByCompanyId(
				partner.getCompanyProfile().getId());
		Set<Integer> distinctPartnerIds = XamplifyUtils.convertListToSetElements(partnerIds);
		for (Integer shareListId : userListIds) {
			UserList shareList = userListDao.findByPrimaryKey(shareListId, new FindLevel[] { FindLevel.SHALLOW });
			Integer shareListPartnerId = userListDao.findShareListPartnerIdByUserListId(shareListId);
			if (shareListPartnerId != null) {
				ShareListPartner assignedShareListPartner = new ShareListPartner();
				assignedShareListPartner.setId(shareListPartnerId);
				saveShareListPartnerMapping(distinctPartnerIds, assignedShareListPartner, true);
				userListDao.updateSharedToCompanyById(shareListPartnerId);
			} else {
				ShareListPartner shareListPartner = saveShareListPartner(shareList, partnershipId, true);
				saveShareListPartnerMapping(distinctPartnerIds, shareListPartner, false);
				shareList.setCompany(partner.getCompanyProfile());
			}
		}

	}

	public Integer getLoggedInUserIdByUserName() {
		String userName = getLoggedInUserName();
		return userDao.getUserIdByEmail(userName);
	}

	public Integer getLoggedInUserCompanyIdByUserName() {
		String userName = getLoggedInUserName();
		Integer userId = userDao.getUserIdByEmail(userName);
		return userDao.getCompanyIdByUserId(userId);
	}

	public void addModules(TeamMemberGroupDTO teamMemberGroupDTO, String partnerModuleCustomName) {
		List<Integer> assignedModuleIds = teamMemberGroupDTO.getAssignedRoleIds();
		addModules(teamMemberGroupDTO, partnerModuleCustomName, assignedModuleIds);
	}

	public void addModules(TeamMemberGroupDTO teamMemberGroupDTO, String partnerModuleCustomName,
			List<Integer> assignedModuleIds) {
		List<TeamMemberModuleDTO> teamMemberModuleDTOs = new ArrayList<>();
		xamplifyUtil.addAllModule(teamMemberModuleDTOs, assignedModuleIds);
		xamplifyUtil.addPartnersModule(teamMemberModuleDTOs, assignedModuleIds, partnerModuleCustomName);
		xamplifyUtil.addVideosModule(teamMemberModuleDTOs, assignedModuleIds);
		xamplifyUtil.addStatsModule(teamMemberModuleDTOs, assignedModuleIds);
		xamplifyUtil.addShareLeadsModule(teamMemberModuleDTOs, assignedModuleIds, "Share / Shared Leads", "");
		xamplifyUtil.addOpportunityModule(teamMemberModuleDTOs, assignedModuleIds);
		xamplifyUtil.addMdfModule(teamMemberModuleDTOs, assignedModuleIds, "");
		xamplifyUtil.addDamModule(teamMemberModuleDTOs, assignedModuleIds);
		xamplifyUtil.addLmsModule(teamMemberModuleDTOs, assignedModuleIds);
		xamplifyUtil.addPlayBookModule(teamMemberModuleDTOs, assignedModuleIds);
		if (XamplifyUtils.isNotEmptyList(teamMemberModuleDTOs)) {
			List<TeamMemberModuleDTO> enabledModules = teamMemberModuleDTOs.stream()
					.filter(TeamMemberModuleDTO::isEnabled).collect(Collectors.toList());
			teamMemberGroupDTO.setTeamMemberModuleDTOs(enabledModules);
		}
	}

	public boolean hasPartnerShip(Integer vendorCompanyId, Integer partnerCompanyId) {
		Partnership partnership = partnershipDao.checkPartnership(vendorCompanyId, partnerCompanyId);
		if (partnership != null) {
			return true;
		}
		return false;
	}

	public List<FormLabelDTO> frameFormLabelDTO(List<FormLabel> formLables, List<FormLabelDTO> formLabelDtos,
			boolean isCustom) {
		List<FormLabelDTO> notRequiredLabelDtos = new ArrayList<FormLabelDTO>();
		for (FormLabel formLabel : formLables) {
			if (formLabel.getLabelName().equals("Stage")) {
				continue;
			}
			FormLabelDTO formLabelDTO = new FormLabelDTO();
			formLabelDTO.setId(formLabel.getId());
			formLabelDTO.setLabelId(formLabel.getLabelId());
			formLabelDTO.setLabelName(formLabel.getLabelName());
			formLabelDTO.setHiddenLabelId(formLabel.getHiddenLabelId());
			formLabelDTO.setOrder(formLabel.getOrder());
			formLabelDTO.setPlaceHolder(formLabel.getPlaceHolder());
			formLabelDTO.setRequired(formLabel.isRequired());
			formLabelDTO.setDisplayName(formLabel.getDisplayName());
			formLabelDTO.setFormDefaultFieldType(formLabel.getFormDefaultFieldType());
			String labelType = formLabel.getLabelType().getLabelType();
			formLabelDTO.setLabelType(labelType);
			formLabelDTO.setSfCustomField(isCustom);
			formLabelDTO.setLabelLength(formLabel.getLabelLength());

			List<FormLabelChoice> formLabelChoices = formLabel.getFormLabelChoices();
			List<FormChoiceDTO> radioButtonChoiceDtos = new ArrayList<>();
			List<FormChoiceDTO> checkBoxChoiceDtos = new ArrayList<>();
			List<FormChoiceDTO> dropDownChoiceDtos = new ArrayList<>();
			for (FormLabelChoice formLabelChoice : formLabelChoices) {
				FormChoiceDTO formChoiceDto = addChoiceData(formLabelChoice);
				if (labelType.equals("radio")) {
					radioButtonChoiceDtos.add(formChoiceDto);
				} else if (labelType.equals("checkbox")) {
					checkBoxChoiceDtos.add(formChoiceDto);
				} else if (labelType.equals("select")) {
					dropDownChoiceDtos.add(formChoiceDto);
				} else if (labelType.equals("multiselect")) {
					dropDownChoiceDtos.add(formChoiceDto);
				}
			}
			formLabelDTO.setRadioButtonChoices(radioButtonChoiceDtos);
			formLabelDTO.setCheckBoxChoices(checkBoxChoiceDtos);
			formLabelDTO.setDropDownChoices(dropDownChoiceDtos);
			if (formLabelDTO.isRequired()) {
				formLabelDtos.add(formLabelDTO);
			} else {
				notRequiredLabelDtos.add(formLabelDTO);
			}

		}
		formLabelDtos.addAll(notRequiredLabelDtos);
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

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse updatePlayBooksType() {
		long startTime = System.currentTimeMillis();
		XtremandResponse response = new XtremandResponse();
		List<Integer> categoryModuleIds = categoryDao.findCategoryModuleIdsForPlayBooksPatch();
		utilDao.updateCategoryModuleTypeToPlayBooks(categoryModuleIds);
		response.setStatusCode(200);
		response.setMessage("PlayBook Type Changed Successfully");
		long endTime = System.currentTimeMillis();
		long timeTaken = endTime - startTime;
		long minutes = (timeTaken / 1000) / 60;
		long seconds = (timeTaken / 1000) % 60;
		String timeTakenInMinutesAndSeconds = minutes + " minutes and " + seconds + " seconds";
		logger.debug("Time Taken To Complete The API:-" + timeTakenInMinutesAndSeconds);
		return response;
	}

	public String deletePartners(Integer companyId) {
		UserList userList = userListDao.getDefaultPartnerList(companyId);
		Integer userListId = userList.getId();
		Integer userId = userList.getOwner().getUserId();
		List<Integer> partnerIds = userListDao.findUserIdsByUserListId(userListId);
		userListService.removeUsersFromUserList(userListId, userId, partnerIds);
		return "Partners Removed Successfully";
	}

	public XtremandResponse findActiveQueries() {
		XtremandResponse response = new XtremandResponse();
		response.setData(utilDao.findActiveQueries());
		response.setStatusCode(200);
		return response;
	}

	// XNFR-215
	public boolean isUpdateRequiredForLeadPipelines(IntegrationType toIntegrationType,
			IntegrationType fromIntegrationType, PipelineType pipelineType) {
		String fromIntegrationTypeString = fromIntegrationType.name().toLowerCase();
		String toIntegrationTypeString = toIntegrationType.name().toLowerCase();
		boolean isUpdateRequiredForPipelines = true;
		List<String> crmsToCompare = crmsWithoutLeadPipelines;
		if (pipelineType == PipelineType.DEAL) {
			crmsToCompare = crmsWithoutDealPipelines;
		}
		if (crmsToCompare.contains(fromIntegrationTypeString) && crmsToCompare.contains(toIntegrationTypeString)) {
			isUpdateRequiredForPipelines = false;
		}
		return isUpdateRequiredForPipelines;
	}

	public void sendUserListProcessedEmail(UserList userList, boolean isCreate) {
		if (userList.getModuleName().equalsIgnoreCase("SHARE LEADS")) {
			User customer = userList.getAssignedBy();
			List<Integer> totalUnsubscribedUserIds = userListDao
					.getUnsubscribedUsers(userList.getAssignedCompany().getId());
			sendLeadsListMail(customer, userList, isCreate, totalUnsubscribedUserIds);
		} else {
			User customer = userList.getOwner();
			List<Integer> totalUnsubscribedUserIds = userListDao.getUnsubscribedUsers(userList.getCompany().getId());
			sendContactListMail(customer, userList, isCreate, totalUnsubscribedUserIds);
		}
	}

	@Transactional(readOnly = true, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void sendContactListMail(User user, UserList userList, boolean isCreate,
			List<Integer> totalUnsubscribedUserIds) {
		if (isCreate && userList.isEmailValidationInd() && userList.getCompany() != null) {
			mailService.sendContactListMail(user, EmailConstants.CONTACT_LIST_CREATED, userList,
					totalUnsubscribedUserIds);
		} else if (!isCreate && userList.isEmailValidationInd() && userList.getCompany() != null) {
			mailService.sendContactListMail(user, EmailConstants.CONTACT_LIST_UPDATED, userList,
					totalUnsubscribedUserIds);
		} else if (!userList.isEmailValidationInd() && userList.getCompany() != null) {
			mailService.sendContactListMail(user, EmailConstants.CONTACT_LIST_IN_PROCESS, userList,
					totalUnsubscribedUserIds);
		}
	}

	// XNFR-230
	public List<FormLabel> getCustomFormLabelsByFormType(Integer companyId, FormTypeEnum formType) {
		List<FormLabel> cfLabels = null;
		Integer formId = formDao.getSfCustomFormIdByCompanyIdAndFormType(companyId, formType);
		if (formId != null) {
			Form form = formDao.getById(formId);
			cfLabels = form.getFormLabels();
		}
		return cfLabels;
	}

	public void updatePipelineDetailsForLostStatus(Deal deal, Pipeline pipeline) {

		Optional<PipelineStage> stage = pipeline.getStages().stream().filter(e -> e.isLost()).findFirst();
		if (stage != null && stage.isPresent()) {
			pipeline.setPrivate(false);
			setPipelines(deal, pipeline.getCompany().getId(), pipeline, stage.get());
		}

	}

	public void updatePipelineDetailsForWonStatus(Deal deal, Pipeline pipeline) {
		Optional<PipelineStage> stage = pipeline.getStages().stream().filter(e -> e.isWon()).findFirst();
		if (stage != null && stage.isPresent()) {
			pipeline.setPrivate(false);
			setPipelines(deal, pipeline.getCompany().getId(), pipeline, stage.get());
		}

	}

	public void updateCustomFieldsData(Deal deal, org.json.simple.JSONObject dealJson, List<FormLabel> formLabels,
			IntegrationType integrationType) {
		if (formLabels != null && !formLabels.isEmpty()) {
			for (FormLabel formLabel : formLabels) {
				if (formLabel != null) {
					SfCustomFieldsData sfCustomFieldsData = sfCustomFormDataDAO
							.getSfCustomFieldDataByDealIdAndLabelId(deal, formLabel);
					String value = null;
					if (dealJson.containsKey(formLabel.getLabelId()) && dealJson.get(formLabel.getLabelId()) != null) {
						value = String.valueOf(dealJson.get(formLabel.getLabelId()));
					}
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
		}
	}

	public void markLeadAsFailed(Integration integration, Lead lead, String errorMessage) {
		if (integration != null && lead != null && lead.getLeadFail() == null) {
			LeadFail failedLead = new LeadFail();
			failedLead.setLead(lead);
			failedLead.setIntegration(integration);
			failedLead.setExternalOrganizationId(integration.getExternalOrganizationId());
			failedLead.setErrorMessage(errorMessage);
			failedLead.setCreatedBy(lead.getUpdatedBy());
			failedLead.initialiseCommonFields(true, lead.getUpdatedBy());
			genericDAO.save(failedLead);
		}
	}

	public void markDealAsFailed(Integration integration, Deal deal, String errorMessage) {
		if (integration != null && deal != null && deal.getDealFail() == null) {
			DealFail failedDeal = new DealFail();
			failedDeal.setDeal(deal);
			failedDeal.setIntegration(integration);
			failedDeal.setExternalOrganizationId(integration.getExternalOrganizationId());
			failedDeal.setErrorMessage(errorMessage);
			failedDeal.setCreatedBy(deal.getUpdatedBy());
			failedDeal.initialiseCommonFields(true, deal.getUpdatedBy());
			genericDAO.save(failedDeal);
		}
	}

	public void deleteLeadFail(Lead lead) {
		LeadFail leadFail = lead.getLeadFail();
		if (leadFail != null) {
			genericDAO.remove(leadFail);
		}
	}

	public void deleteDealFail(Deal deal) {
		DealFail dealFail = deal.getDealFail();
		if (dealFail != null) {
			genericDAO.remove(dealFail);
		}
	}

	/***** XNFR-255 ***/
	public XtremandResponse findShareWhiteLabelContentAccessByLoggedInUserId(Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		response.setData(utilDao.hasShareWhiteLabeledContentAccessByUserId(loggedInUserId));
		return response;
	}

	/***** XNFR-255 ***/
	public XtremandResponse findShareWhiteLabelContentAccessByCompanyProfileName(String companyProfileName) {
		XtremandResponse response = new XtremandResponse();
		Integer companyId = userDao.getCompanyIdByProfileName(companyProfileName);
		response.setData(utilDao.hasShareWhiteLabeledContentAccessByCompanyId(companyId));
		return response;
	}

	public XtremandResponse findCompanyDetailsByEmailIdOrUserId(String emailIdOrUserId) {
		return utilDao.findCompanyDetailsByEmailIdOrUserId(emailIdOrUserId);

	}

	public XtremandResponse assetPublishedEmailNotification(Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		XamplifyUtils.addSuccessStatus(response);
		response.setData(companyProfileDao.isAssetPublishedEmailNotificationByUserId(loggedInUserId));
		return response;
	}

	public XtremandResponse assetPublishedEmailNotification(String companyProfileName) {
		XtremandResponse response = new XtremandResponse();
		XamplifyUtils.addSuccessStatus(response);
		response.setData(companyProfileDao.isAssetPublishedEmailNotificationByCompanyProfileName(companyProfileName));
		return response;
	}

	public XtremandResponse trackPublishedEmailNotification(Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		XamplifyUtils.addSuccessStatus(response);
		response.setData(companyProfileDao.isTrackPublishedEmailNotificationByUserId(loggedInUserId));
		return response;
	}

	public XtremandResponse trackPublishedEmailNotification(String companyProfileName) {
		XtremandResponse response = new XtremandResponse();
		XamplifyUtils.addSuccessStatus(response);
		response.setData(companyProfileDao.isTrackPublishedEmailNotificationByCompanyProfileName(companyProfileName));
		return response;
	}

	public XtremandResponse playbookPublishedEmailNotification(Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		XamplifyUtils.addSuccessStatus(response);
		response.setData(companyProfileDao.isPlaybookPublishedEmailNotificationByUserId(loggedInUserId));
		return response;
	}

	public XtremandResponse playbookPublishedEmailNotification(String companyProfileName) {
		XtremandResponse response = new XtremandResponse();
		XamplifyUtils.addSuccessStatus(response);
		response.setData(
				companyProfileDao.isPlaybookPublishedEmailNotificationByCompanyProfileName(companyProfileName));
		return response;
	}

	@SuppressWarnings("static-access")
	public XtremandResponse findActiveThreads() {
		XtremandResponse response = new XtremandResponse();
		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		Set<ActiveThreadsDTO> activeThreadsDTO = new HashSet<ActiveThreadsDTO>();
		for (Thread thread : threadSet) {
			ActiveThreadsDTO activeThreadDTO = new ActiveThreadsDTO();
			activeThreadDTO.setThreadName(thread.getName());
			activeThreadDTO.setActiveCount(thread.activeCount());
			ThreadGroup threadGroup = thread.getThreadGroup();
			activeThreadDTO.setThreadGroupName(threadGroup.getName());
			activeThreadDTO.setThreadId(thread.getId());
			activeThreadDTO.setPriority(thread.getPriority());
			activeThreadsDTO.add(activeThreadDTO);
		}
		response.setStatusCode(200);
		response.setData(activeThreadsDTO);
		String message = "Total Running Threads : " + threadSet.size();
		response.setMessage(message);
		return response;
	}

	public Map<String, FormLabel> getExistingFieldsMap(User user, FormTypeEnum formType) {
		Map<String, FormLabel> exisitngFieldsMap = null;
		Integer formId = formDao.getSfCustomFormIdByCompanyIdAndFormType(user.getCompanyProfile().getId(), formType);
		if (formId != null) {
			Form form = formDao.getById(formId);
			exisitngFieldsMap = form.getFormLabels().stream()
					.collect(Collectors.toMap(FormLabel::getLabelId, Function.identity()));
		}
		return exisitngFieldsMap;
	}

	public List<OpportunityFormFieldsDTO> getSelectedCustomFields(List<OpportunityFormFieldsDTO> allCustomFields,
			List<OpportunityFormFieldsDTO> selectedFields) {
		List<OpportunityFormFieldsDTO> selectedCustomFields = new ArrayList<OpportunityFormFieldsDTO>();
		if (allCustomFields != null && !allCustomFields.isEmpty()) {
			Map<String, OpportunityFormFieldsDTO> selectedFieldsMap = selectedFields.stream()
					.collect(Collectors.toMap(x -> x.getName(), x -> x));
			for (OpportunityFormFieldsDTO customField : allCustomFields) {
				if (customField != null) {
					if (selectedFieldsMap.containsKey(customField.getName())) {
						OpportunityFormFieldsDTO selectedField = selectedFieldsMap.get(customField.getName());
						customField.setRequired(selectedField.isRequired());
						customField.setPlaceHolder(selectedField.getPlaceHolder());
						customField.setDisplayName(selectedField.getDisplayName());
						customField.setFormDefaultFieldType(selectedField.getFormDefaultFieldType());
						customField.setOptions(selectedField.getOptions());
						customField.setOriginalCRMType(selectedField.getOriginalCRMType());
						selectedCustomFields.add(customField);
					}

				}
			}
		}
		return selectedCustomFields;
	}

	public Pagination setPageableParameters(Pageable pageable, Integer loggedInUserId) {
		String sort = pageable.getSort();
		String search = pageable.getSearch();
		String filterBy = pageable.getFilterBy();
		Integer loginAsUserId = pageable.getLoginAsUserId();
		Pagination pagination = new Pagination();
		pagination.setPageIndex(pageable.getPageNumber());
		pagination.setMaxResults(pageable.getLimit());
		if (sort != null && StringUtils.hasText(sort)) {
			String sortColumn = sort.split(",")[0];
			String sortOrder = sort.split(",")[1];
			pagination.setSortcolumn(sortColumn);
			pagination.setSortingOrder(sortOrder);
		}
		pagination.setSearchKey(search);
		if (loggedInUserId != null) {
			pagination.setUserId(loggedInUserId);
			Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
			pagination.setCompanyId(companyId);
		}
		pagination.setLoginAsUserId(loginAsUserId);
		pagination.setFilterBy(filterBy);
		return pagination;
	}

	// ****XNFR-316*********
	public String replaceCompanyLogos(String body, String companyLogoPath) {
		String logoPath = serverPath + XamplifyUtils.escapeDollarSequece(companyLogoPath);
		body = body.replaceAll("<<COMPANY_LOGO>>", logoPath);
		body = body.replaceAll(yourLogo, logoPath);
		return body;
	}

	/*********** XNFR-403 ***********/
	public void saveIntegrationDetails(Integration integration, User loggedInUser, IntegrationDTO integrationDTO) {
		integration.setApiKey(integrationDTO.getApiKey());
		integration.setCompany(loggedInUser.getCompanyProfile());
		integration.setType(integrationDTO.getType());
		integration.setClientId(integrationDTO.getClientId());
		integration.setPublicKey(integrationDTO.getPublicKey());
		integration.setPrivateKey(integrationDTO.getPrivateKey());
		integration.setInstanceUrl(integrationDTO.getInstanceUrl());
		integration.setWebApiInstanceUrl(integrationDTO.getWebApiInstanceUrl());
		integration.setCreatedBy(loggedInUser.getUserId());
		integration.setCreatedTime(new Date());
		integration.setUpdatedBy(loggedInUser.getUserId());
		integration.setUpdatedTime(new Date());
		integration.setPushLeads(true);
		integration.setExternalDisplayName(integrationDTO.getExternalDisplayName());
		integration.setExternalEmail(integrationDTO.getExternalEmail());
		integration.setExternalOrganizationId(integrationDTO.getExternalOrganizationId());
		integration.setExternalUserId(integrationDTO.getExternalUserId());
		integration.setExternalUserName(integrationDTO.getExternalEmail());
		integration.setExternalOrganizationName(integrationDTO.getExternalOrganizationName());
		Integer integrationsCount = integrationDao.getTotalIntegrationsCount(loggedInUser.getCompanyProfile().getId());
		if (integrationsCount == 0) {
			integration.setActive(true);
			integration.setFirstIntegration(true);
		}
		genericDAO.saveOrUpdate(integration);

	}

	/********* XNFR-423 ********/
	public XtremandResponse getCountryNames() {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(200);
		response.setData(XamplifyUtils.getCountryNames());
		return response;
	}

	public List<User> getOwners(Integer companyId) {
		return partnershipDao.getOwners(companyId);
	}

	public XtremandResponse findCompanyDetailsByCompanyProfileName(String companyProfileName) {
		XtremandResponse response = new XtremandResponse();
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String queryString = "select company_id as \"companyId\", cast(company_name_status as text) as \"companyStatus\" from xt_company_profile where company_profile_name = :companyProfileName";
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("companyProfileName", utilDao.getPrmCompanyProfileName()));
		CompanyDetailsDTO companyDetailsDTO = (CompanyDetailsDTO) hibernateSQLQueryResultUtilDao
				.getDto(hibernateSQLQueryResultRequestDTO, CompanyDetailsDTO.class);
		if (companyDetailsDTO != null) {
			response.setStatusCode(200);
			response.setData(companyDetailsDTO);
		} else {
			response.setStatusCode(404);
		}
		return response;
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void saveDomain(Integer createdUserId, CompanyProfile company, List<String> updatedDomainNames,
			DomainModuleNameType domainModuleNameType, boolean isDomainAllowedToAddToSamePartnerAccount) {
		List<Domain> domains = new ArrayList<>();
		for (String domainName : updatedDomainNames) {
			Domain domain = new Domain();
			domain.setDomainName(domainName);
			domain.setCompany(company);
			domain.setCreatedUserId(createdUserId);
			domain.setCreatedTime(new Date());
			domain.setDomainModuleNameType(domainModuleNameType);
			domain.setUpdatedTime(domain.getCreatedTime());
			domain.setUpdatedUserId(createdUserId);
			domain.setDomainAllowedToAddToSamePartnerAccount(isDomainAllowedToAddToSamePartnerAccount);
			domains.add(domain);
		}
		domainDao.saveAll(domains);

	}

	// XNFR-461
	public FormLabelDTO getMatchedObject(FormLabelDTO formLabelDTO, List<FormLabelDTO> formLabelDTOsToCompare) {
		FormLabelDTO matchedFormLabelDTO = null;
		Optional<FormLabelDTO> matchedFormLabelObj = formLabelDTOsToCompare.stream().filter(
				x -> x.getDisplayName().toLowerCase().contains(formLabelDTO.getDisplayName().trim().toLowerCase()))
				.findFirst();
		if (matchedFormLabelObj.isPresent() && matchedFormLabelObj.get() != null) {
			matchedFormLabelDTO = matchedFormLabelObj.get();
		} else {
			matchedFormLabelObj = formLabelDTOsToCompare.stream().filter(
					x -> formLabelDTO.getDisplayName().trim().toLowerCase().contains(x.getDisplayName().toLowerCase()))
					.findFirst();
			if (matchedFormLabelObj.isPresent() && matchedFormLabelObj.get() != null) {
				matchedFormLabelDTO = matchedFormLabelObj.get();
			}
		}
		return matchedFormLabelDTO;
	}

	public FormLabel getMatchedObject(FormLabel formLabel, List<FormLabel> formLabelsToCompare) {
		FormLabel matchedFormLabel = null;
		Optional<FormLabel> matchedFormLabelObj = formLabelsToCompare.stream()
				.filter(x -> x.getDisplayName().toLowerCase().contains(formLabel.getDisplayName().trim().toLowerCase()))
				.findFirst();
		if (matchedFormLabelObj.isPresent() && matchedFormLabelObj.get() != null) {
			matchedFormLabel = matchedFormLabelObj.get();
		} else {
			matchedFormLabelObj = formLabelsToCompare.stream().filter(
					x -> formLabel.getDisplayName().trim().toLowerCase().contains(x.getDisplayName().toLowerCase()))
					.findFirst();
			if (matchedFormLabelObj.isPresent() && matchedFormLabelObj.get() != null) {
				matchedFormLabel = matchedFormLabelObj.get();
			}
		}
		return matchedFormLabel;
	}

	public Map<String, Object> getCustomFormMap(Integer companyId, FormTypeEnum type) {
		Map<String, Object> customFormLabelsMap = new HashMap<String, Object>();
		List<Integer> customFormLabelIds = new ArrayList<Integer>();
		List<String> customFormLabelNames = new ArrayList<String>();
		Integer formId = formDao.getSfCustomFormIdByCompanyIdAndFormType(companyId, type);
		if (formId != null) {
			Form form = formDao.getById(formId);
			List<FormLabel> cfLabels = form.getFormLabels();
			if (cfLabels != null && !cfLabels.isEmpty()) {
				customFormLabelIds = cfLabels.stream().map(FormLabel::getId).collect(Collectors.toList());
				customFormLabelNames = cfLabels.stream().map(FormLabel::getLabelName).collect(Collectors.toList());
				Optional<FormLabel> formLabelOptional = cfLabels.stream()
						.filter(x -> FormDefaultFieldTypeEnum.DEAL_ID.equals(x.getFormDefaultFieldType())).findFirst();

				if (formLabelOptional.isPresent()) {
					customFormLabelsMap.put("deaIdLabelId", formLabelOptional.get().getLabelId());
				}
				Optional<FormLabel> leadIdOptional = cfLabels.stream()
						.filter(x -> FormDefaultFieldTypeEnum.LEAD_ID.equals(x.getFormDefaultFieldType())).findFirst();

				if (leadIdOptional.isPresent()) {
					customFormLabelsMap.put("leadIdLabelId", leadIdOptional.get().getLabelId());
				}
				Optional<FormLabel> leadCreatedDateOptional = cfLabels.stream().filter(
						x -> FormDefaultFieldTypeEnum.XAMPLIFY_LEAD_CREATED_DATE.equals(x.getFormDefaultFieldType()))
						.findFirst();

				if (leadCreatedDateOptional.isPresent()) {
					customFormLabelsMap.put("leadCreatedDateId", leadCreatedDateOptional.get().getLabelId());
				}
				Optional<FormLabel> dealRegsiteredDateOptional = cfLabels.stream().filter(
						x -> FormDefaultFieldTypeEnum.XAMPLIFY_DEAL_REGISTERED_DATE.equals(x.getFormDefaultFieldType()))
						.findFirst();

				if (dealRegsiteredDateOptional.isPresent()) {
					customFormLabelsMap.put("dealRegisteredDateId", dealRegsiteredDateOptional.get().getLabelId());
				}
				Optional<FormLabel> leadRegsiteredDateOptional = cfLabels.stream().filter(
						x -> FormDefaultFieldTypeEnum.XAMPLIFY_LEAD_REGISTERED_DATE.equals(x.getFormDefaultFieldType()))
						.findFirst();

				if (leadRegsiteredDateOptional.isPresent()) {
					customFormLabelsMap.put("leadRegisteredDateId", leadRegsiteredDateOptional.get().getLabelId());
				}
				Optional<FormLabel> accountIdOptional = cfLabels.stream()
						.filter(x -> FormLookUpDefaultFieldTypeEnum.AUTO_SELECT_ACCOUNT_ID
								.equals(x.getFormLookUpDefaultFieldType()) && x.isNonInteractive())
						.findFirst();

				if (accountIdOptional.isPresent()) {
					customFormLabelsMap.put("sfAccountId", accountIdOptional.get().getLabelId());
				}
			}
		}
		customFormLabelsMap.put("customFormLabelIds",
				(customFormLabelIds == null) ? new ArrayList<Integer>() : customFormLabelIds);
		customFormLabelsMap.put("customFormLabelNames",
				(customFormLabelNames == null) ? new ArrayList<String>() : customFormLabelNames);
		customFormLabelsMap.put("formId", formId);
		return customFormLabelsMap;
	}

	public Integration getOtherActiveCRMIntegration(Deal deal, Integer activeCRMCompanyId) {
		Integration otherActiveCRMIntegration = null;
		if (!deal.getCreatedByCompany().getId().equals(deal.getCreatedForCompany().getId())) {
			Integer otherActiveCRMCompanyId = null;
			if (activeCRMCompanyId.equals(deal.getCreatedByCompany().getId())) {
				otherActiveCRMCompanyId = deal.getCreatedForCompany().getId();
			} else if (activeCRMCompanyId.equals(deal.getCreatedForCompany().getId())) {
				otherActiveCRMCompanyId = deal.getCreatedByCompany().getId();
			}

			otherActiveCRMIntegration = integrationDao.getActiveCRMIntegration(otherActiveCRMCompanyId);
		}
		return otherActiveCRMIntegration;
	}

	public Integration getOtherActiveCRMIntegration(Integer createdForCompanyId, Integer createdByCompanyId,
			Integer activeCRMCompanyId) {
		Integration otherActiveCRMIntegration = null;
		if (!createdByCompanyId.equals(createdForCompanyId)) {
			Integer otherActiveCRMCompanyId = null;
			if (activeCRMCompanyId.equals(createdByCompanyId)) {
				otherActiveCRMCompanyId = createdForCompanyId;
			} else if (activeCRMCompanyId.equals(createdForCompanyId)) {
				otherActiveCRMCompanyId = createdByCompanyId;
			}

			otherActiveCRMIntegration = integrationDao.getActiveCRMIntegration(otherActiveCRMCompanyId);
		}
		return otherActiveCRMIntegration;
	}

	public List<FormLabel> getCustomFormLabelsByIntegrationType(Integer companyId, IntegrationType integrationType) {
		switch (integrationType) {
		default:
			break;
		}

		List<FormLabel> cfLabels = new ArrayList<>();
		return cfLabels;
	}

	public List<FormLabel> getLeadCustomFormLabelsByIntegrationType(Integer companyId,
			IntegrationType integrationType) {
		switch (integrationType) {
		default:
			break;
		}

		List<FormLabel> cfLabels = new ArrayList<>();
		return cfLabels;
	}

	public Map<String, Object> getSfCustomFieldsDataValue(final String value, FormLabel formLabel,
			FormLabel formLabelInOtherForm) {
		String sfCustomFieldsDataValue = null;
		boolean saveOrUpdate = true;
		if (formLabelInOtherForm.getLabelType().getLabelType().equals("select")
				|| formLabelInOtherForm.getLabelType().getLabelType().equals("radio")) {
			Optional<FormLabelChoice> selectedChoiceObj = formLabelInOtherForm.getFormLabelChoices().stream()
					.filter(x -> x.getLabelChoiceId().equals(value)).findFirst();
			if (selectedChoiceObj.isPresent()) {
				FormLabelChoice selectedChoice = selectedChoiceObj.get();
				if (selectedChoice != null) {
					if (formLabel.getLabelType().getLabelType().equals("select")
							|| formLabel.getLabelType().getLabelType().equals("radio")) {
						Optional<FormLabelChoice> matchedChoiceObj = formLabel.getFormLabelChoices().stream().filter(
								x -> x.getLabelChoiceName().equalsIgnoreCase(selectedChoice.getLabelChoiceName()))
								.findFirst();
						if (matchedChoiceObj.isPresent()) {
							FormLabelChoice matchedChoice = matchedChoiceObj.get();
							sfCustomFieldsDataValue = matchedChoice.getLabelChoiceId();
						} else {
							saveOrUpdate = false;
						}
					} else if (formLabel.getLabelType().getLabelType().equals("percent")
							|| formLabel.getLabelType().getLabelType().equals("number")) {
						if (isNumberDropdown(formLabelInOtherForm)) {
							sfCustomFieldsDataValue = selectedChoice.getLabelChoiceName();
						} else {
							saveOrUpdate = false;
						}
					} else if (formLabel.getLabelType().getLabelType().equals("text")
							|| formLabel.getLabelType().getLabelType().equals("textarea")) {
						sfCustomFieldsDataValue = selectedChoice.getLabelChoiceName();
					} else {
						saveOrUpdate = false;
					}
				}
			}
		} else if (formLabelInOtherForm.getLabelType().getLabelType().equals("multiselect")) {
			Optional<FormLabelChoice> selectedChoiceObj = formLabelInOtherForm.getFormLabelChoices().stream()
					.filter(x -> x.getLabelChoiceId().equals(value)).findFirst();
			if (selectedChoiceObj.isPresent()) {
				FormLabelChoice selectedChoice = selectedChoiceObj.get();
				if (selectedChoice != null) {
					if (formLabel.getLabelType().getLabelType().equals("select")
							|| formLabel.getLabelType().getLabelType().equals("radio")) {
						Optional<FormLabelChoice> matchedChoiceObj = formLabel.getFormLabelChoices().stream().filter(
								x -> x.getLabelChoiceName().equalsIgnoreCase(selectedChoice.getLabelChoiceName()))
								.findFirst();
						if (matchedChoiceObj.isPresent()) {
							FormLabelChoice matchedChoice = matchedChoiceObj.get();
							sfCustomFieldsDataValue = matchedChoice.getLabelChoiceId();
						} else {
							saveOrUpdate = false;
						}
					} else if (formLabel.getLabelType().getLabelType().equals("text")
							|| formLabel.getLabelType().getLabelType().equals("textarea")) {
						sfCustomFieldsDataValue = selectedChoice.getLabelChoiceName();
					} else {
						saveOrUpdate = false;
					}
				}
			}
		} else if (formLabelInOtherForm.getLabelType().getLabelType().equals("checkbox")) {
			if (formLabel.getLabelType().getLabelType().equals("checkbox")) {
				sfCustomFieldsDataValue = value;
			} else {
				saveOrUpdate = false;
			}
		} else if (formLabelInOtherForm.getLabelType().getLabelType().equals("text")
				|| formLabelInOtherForm.getLabelType().getLabelType().equals("textarea")) {
			if (formLabel.getLabelType().getLabelType().equals("select")) {
				Optional<FormLabelChoice> matchedChoiceObj = formLabel.getFormLabelChoices().stream()
						.filter(x -> x.getLabelChoiceName().equalsIgnoreCase(value)).findFirst();
				if (matchedChoiceObj.isPresent()) {
					FormLabelChoice matchedChoice = matchedChoiceObj.get();
					sfCustomFieldsDataValue = matchedChoice.getLabelChoiceId();
				}
			} else if (formLabel.getLabelType().getLabelType().equals("text")
					|| formLabel.getLabelType().getLabelType().equals("textarea")) {
				sfCustomFieldsDataValue = value;
			} else if (formLabel.getLabelType().getLabelType().equals("percent")
					|| formLabel.getLabelType().getLabelType().equals("number")
					|| formLabel.getLabelType().getLabelType().equals("currency")) {
				if (isNumeric(value)) {
					sfCustomFieldsDataValue = value;
				} else {
					saveOrUpdate = false;
				}
			} else if (formLabel.getLabelType().getLabelType().equals("date")) {
				if (isDate(value)) {
					sfCustomFieldsDataValue = value;
				} else {
					saveOrUpdate = false;
				}
			} else if (formLabel.getLabelType().getLabelType().equals("email")) {
				if (isEmail(value)) {
					sfCustomFieldsDataValue = value;
				} else {
					saveOrUpdate = false;
				}
			} else if (formLabel.getLabelType().getLabelType().equals("url")) {
				if (isURL(value)) {
					sfCustomFieldsDataValue = value;
				} else {
					saveOrUpdate = false;
				}
			} else if (formLabel.getLabelType().getLabelType().equals("phone")) {
				if (isPhone(value)) {
					sfCustomFieldsDataValue = value;
				} else {
					saveOrUpdate = false;
				}
			} else {
				saveOrUpdate = false;
			}
		} else if (formLabelInOtherForm.getLabelType().getLabelType().equals("percent")) {
			if (formLabel.getLabelType().getLabelType().equals("select")
					|| formLabel.getLabelType().getLabelType().equals("radio")) {
				if (isNumberDropdown(formLabel)) {
					Optional<FormLabelChoice> matchedChoiceObj = formLabel.getFormLabelChoices().stream()
							.filter(x -> x.getLabelChoiceName().equalsIgnoreCase(value)).findFirst();
					if (matchedChoiceObj.isPresent()) {
						FormLabelChoice matchedChoice = matchedChoiceObj.get();
						sfCustomFieldsDataValue = matchedChoice.getLabelChoiceId();
					}
				} else {
					saveOrUpdate = false;
				}
			} else if (formLabel.getLabelType().getLabelType().equals("percent")
					|| formLabel.getLabelType().getLabelType().equals("text")
					|| formLabel.getLabelType().getLabelType().equals("textarea")) {
				sfCustomFieldsDataValue = value;
			} else {
				saveOrUpdate = false;
			}
		} else if (formLabelInOtherForm.getLabelType().getLabelType().equals("date")) {
			if (formLabel.getLabelType().getLabelType().equals("date")
					|| formLabel.getLabelType().getLabelType().equals("text")
					|| formLabel.getLabelType().getLabelType().equals("textarea")) {
				sfCustomFieldsDataValue = value;
			} else {
				saveOrUpdate = false;
			}
		} else if (formLabelInOtherForm.getLabelType().getLabelType().equals("datetime")) {
			if (formLabel.getLabelType().getLabelType().equals("datetime")
					|| formLabel.getLabelType().getLabelType().equals("text")
					|| formLabel.getLabelType().getLabelType().equals("textarea")) {
				sfCustomFieldsDataValue = value;
			} else {
				saveOrUpdate = false;
			}
		} else if (formLabelInOtherForm.getLabelType().getLabelType().equals("time")) {
			if (formLabel.getLabelType().getLabelType().equals("time")
					|| formLabel.getLabelType().getLabelType().equals("text")
					|| formLabel.getLabelType().getLabelType().equals("textarea")) {
				sfCustomFieldsDataValue = value;
			} else {
				saveOrUpdate = false;
			}
		} else if (formLabelInOtherForm.getLabelType().getLabelType().equals("email")) {
			if (formLabel.getLabelType().getLabelType().equals("email")
					|| formLabel.getLabelType().getLabelType().equals("text")
					|| formLabel.getLabelType().getLabelType().equals("textarea")) {
				sfCustomFieldsDataValue = value;
			} else {
				saveOrUpdate = false;
			}
		} else if (formLabelInOtherForm.getLabelType().getLabelType().equals("currency")) {
			if (formLabel.getLabelType().getLabelType().equals("currency")
					|| formLabel.getLabelType().getLabelType().equals("text")
					|| formLabel.getLabelType().getLabelType().equals("textarea")) {
				sfCustomFieldsDataValue = value;
			} else {
				saveOrUpdate = false;
			}
		} else if (formLabelInOtherForm.getLabelType().getLabelType().equals("url")) {
			if (formLabel.getLabelType().getLabelType().equals("url")
					|| formLabel.getLabelType().getLabelType().equals("text")
					|| formLabel.getLabelType().getLabelType().equals("textarea")) {
				sfCustomFieldsDataValue = value;
			} else {
				saveOrUpdate = false;
			}
		} else if (formLabelInOtherForm.getLabelType().getLabelType().equals("phone")) {
			if (formLabel.getLabelType().getLabelType().equals("phone")
					|| formLabel.getLabelType().getLabelType().equals("text")
					|| formLabel.getLabelType().getLabelType().equals("textarea")) {
				sfCustomFieldsDataValue = value;
			} else {
				saveOrUpdate = false;
			}
		} else if (formLabelInOtherForm.getLabelType().getLabelType().equals("number")) {
			if (formLabel.getLabelType().getLabelType().equals("number")
					|| formLabel.getLabelType().getLabelType().equals("text")
					|| formLabel.getLabelType().getLabelType().equals("textarea")) {
				sfCustomFieldsDataValue = value;
			} else {
				saveOrUpdate = false;
			}
		} else if (formLabelInOtherForm.getLabelType().getLabelType().equals("geolocation")) {
			if (formLabel.getLabelType().getLabelType().equals("geolocation")
					|| formLabel.getLabelType().getLabelType().equals("text")
					|| formLabel.getLabelType().getLabelType().equals("textarea")) {
				sfCustomFieldsDataValue = value;
			} else {
				saveOrUpdate = false;
			}
		} else {
			sfCustomFieldsDataValue = value;
		}

		Map<String, Object> customFieldsDataValue = new HashMap<String, Object>();
		customFieldsDataValue.put("saveOrUpdate", saveOrUpdate);
		customFieldsDataValue.put("value", sfCustomFieldsDataValue);
		return customFieldsDataValue;
	}

	public void setPipelines(Deal deal, Integer companyId, Pipeline pipeline, PipelineStage pipelineStage) {
		if (companyId.equals(deal.getCreatedForCompany().getId())) {
			deal.setPipeline(pipeline);
			deal.setCurrentStage(pipelineStage);
			deal.setCreatedForPipeline(pipeline);
			deal.setCreatedForPipelineStage(pipelineStage);
			deal.setPushToCRMStage(deal.getCreatedByPipelineStage());
		} else if (companyId.equals(deal.getCreatedByCompany().getId())) {
			deal.setCreatedByPipeline(pipeline);
			deal.setCreatedByPipelineStage(pipelineStage);
			deal.setPushToCRMStage(deal.getCreatedForPipelineStage());
		}
	}

	public void setPipelines(Lead lead, Integer companyId, Pipeline pipeline, PipelineStage pipelineStage) {
		if (companyId.equals(lead.getCreatedForCompany().getId())) {
			lead.setPipeline(pipeline);
			lead.setCurrentStage(pipelineStage);
			lead.setCreatedForPipeline(pipeline);
			lead.setCreatedForPipelineStage(pipelineStage);
			lead.setPushToCRMStage(lead.getCreatedByPipelineStage());
		} else if (companyId.equals(lead.getCreatedByCompany().getId())) {
			lead.setCreatedByPipeline(pipeline);
			lead.setCreatedByPipelineStage(pipelineStage);
			lead.setPushToCRMStage(lead.getCreatedForPipelineStage());
		}
	}

	public void validateId(Integer id, boolean isDelete, List<Integer> ids) {
		if (id != null && id > 0) {
			if (ids.indexOf(id) < 0) {
				throwInvalidIdException(id, isDelete);
			}
		} else {
			throwInvalidIdException(id, isDelete);
		}
	}

	public void throwInvalidIdException(Integer id, boolean isDelete) {
		String message = isDelete ? "You do not have permission to delete the record for " + id
				: "You do not have permission to update/view details for " + id;
		throw new AccessDeniedException(message);
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse addDashboardBanners() {
		logger.debug("****Entered Into addDashboardBanners()********");
		XtremandResponse response = new XtremandResponse();
		List<CompanyAndRolesDTO> companyDetailsDTOs = teamDao.findCompanyDetailsAndRoles();
		int counter = 0;
		List<CustomLink> customLinks = new ArrayList<>();
		for (CompanyAndRolesDTO companyDetailsDTO : companyDetailsDTOs) {
			List<Integer> userRoleIds = companyDetailsDTO.getRoleIds();
			boolean prm = userRoleIds.indexOf(Role.PRM_ROLE.getRoleId()) > -1;
			if (prm) {
				addDashboardBannerImageAndHelpImages(customLinks, companyDetailsDTO);
				counter++;
				if (XamplifyUtils.isNotEmptyList(customLinks)) {
					String debugMessage = "Total Companies Added : " + counter;
					logger.debug(debugMessage);
				}
			}
		}
		hibernateSQLQueryResultUtilDao.saveAll(customLinks);
		logger.debug("DashBoard Banners Created Successfully.");

		return response;
	}

	private void addDashboardBannerImageAndHelpImages(List<CustomLink> customLinks,
			CompanyAndRolesDTO companyAndRolesDTO) {
		Integer companyId = companyAndRolesDTO.getCompanyId();
		CompanyProfile companyProfile = new CompanyProfile();
		companyProfile.setId(companyId);

		/**** Default DashBoard Help Image ****/
		addDefaultDashboardHelpImage(customLinks, companyAndRolesDTO, companyId, companyAndRolesDTO.getUserId(),
				companyProfile);

		/**** Default DashBoard Banner Image ****/
		addDefaultDashboardBannerImage(customLinks, companyAndRolesDTO, companyId, companyAndRolesDTO.getUserId(),
				companyProfile);
	}

	private void addDefaultDashboardHelpImage(List<CustomLink> customLinks, CompanyAndRolesDTO companyDto,
			Integer companyId, Integer primaryAdminId, CompanyProfile companyProfile) {
		boolean isDefaultDashboardHelpImageExists = customLinkDao.isDefaultHelpBannerExists(companyId);
		if (!isDefaultDashboardHelpImageExists) {
			CustomLink defaultDashboardHelpImage = new CustomLink();
			defaultDashboardHelpImage.setTitle("Help Guides");
			defaultDashboardHelpImage.setDescription(xamplifyUtil.getHelpDescription());
			defaultDashboardHelpImage.setButtonText("Learn More");
			String helpLink = webUrl + "/home/help/guides";
			defaultDashboardHelpImage.setLink(helpLink);
			defaultDashboardHelpImage.setBannerImagePath(xamplifyUtil.getDefaultHelpImagePath());
			setDefaultDashboardCommonProperties(primaryAdminId, companyProfile, defaultDashboardHelpImage, customLinks);
		}
	}

	private void addDefaultDashboardBannerImage(List<CustomLink> customLinks, CompanyAndRolesDTO companyDetailsDTO,
			Integer companyId, Integer primaryAdminId, CompanyProfile companyProfile) {
		boolean isDefaultDashboardBannerImageExits = customLinkDao.isDefaultDashboardBannerExists(companyId);
		if (!isDefaultDashboardBannerImageExits) {
			CustomLink dashboardBannerImage = new CustomLink();
			dashboardBannerImage.setTitle(companyDetailsDTO.getCompanyName());
			dashboardBannerImage.setLink(companyDetailsDTO.getWebsite());
			dashboardBannerImage.setBannerImagePath(xamplifyUtil.getDefaultDashboardBannerImagePath());
			dashboardBannerImage.setCdnBannerImagePath(xamplifyUtil.getDefaultDashboardBannerImagePath());
			dashboardBannerImage.setDescription(companyDetailsDTO.getAboutAs());
			dashboardBannerImage.setButtonText("Learn More");
			setDefaultDashboardCommonProperties(primaryAdminId, companyProfile, dashboardBannerImage, customLinks);
		}
	}

	private void setDefaultDashboardCommonProperties(Integer primaryAdminId, CompanyProfile companyProfile,
			CustomLink defaultDashboardCustomLink1, List<CustomLink> customLinks) {
		defaultDashboardCustomLink1.setCustomLinkType(CustomLinkType.DASHBOARD_BANNERS);
		defaultDashboardCustomLink1.setOpenLinkInNewTab(true);
		defaultDashboardCustomLink1.setCompany(companyProfile);
		defaultDashboardCustomLink1.setCreatedTime(new Date());
		defaultDashboardCustomLink1.setUpdatedTime(new Date());
		defaultDashboardCustomLink1.setCreatedUserId(primaryAdminId);
		defaultDashboardCustomLink1.setUpdatedUserId(primaryAdminId);
		customLinks.add(defaultDashboardCustomLink1);
	}

	public void addDefaultDashboardBanners(Integer userId, CompanyProfile companyProfile, Integer roleId,
			boolean isUpgrading) {
		boolean isPrm = Role.PRM_ROLE.getRoleId().equals(roleId);
		if (isPrm) {
			List<CustomLink> customLinks = new ArrayList<>();
			CompanyAndRolesDTO companyAndRolesDTO = new CompanyAndRolesDTO();
			Integer companyId = companyProfile.getId();
			companyAndRolesDTO.setCompanyId(companyId);
			companyAndRolesDTO.setUserId(userId);
			companyAndRolesDTO.setAboutAs(companyProfile.getAboutUs());
			companyAndRolesDTO.setWebsite(companyProfile.getWebsite());
			companyAndRolesDTO.setCompanyProfileName(companyProfile.getCompanyProfileName());
			companyAndRolesDTO.setCompanyName(companyProfile.getCompanyName());
			addDashboardBannerImageAndHelpImages(customLinks, companyAndRolesDTO);
			hibernateSQLQueryResultUtilDao.saveAll(customLinks);
			String debugMessage = "DashBoard Banners Created Successfully For " + companyId;
			logger.debug(debugMessage);
		}

	}

	public void insertDefaultCustomSkins() {

		List<CustomSkin> customSkins = new ArrayList<CustomSkin>();
		CompanyProfile company = new CompanyProfile();
		company.setId(1);

		createSkin(company, 1, CustomModule.FOOTER, "#000", null, null, "Open Sans, sans-serif", webUrl, "#fff", null,
				null, true, false, null, null, null, null, customSkins);

		createSkin(company, 1, CustomModule.TOP_NAVIGATION_BAR, "#fff", "#3575b5", "#fff", "Open Sans, sans-serif",
				webUrl, "#fff", "#008fd5", "#fff", true, false, null, null, null, null, customSkins);

		createSkin(company, 1, CustomModule.LEFT_SIDE_MENU, "#fff", "#3d4f65", "#15c1df", "Open Sans, sans-serif",
				webUrl, "#34495a", null, null, true, false, null, null, null, null, customSkins);

		createSkin(company, 1, CustomModule.MAIN_CONTENT, "#fff", null, null, "Open Sans, sans-serif", webUrl,
				"#F1F3FA", null, null, true, false, "#fff", "#00a6e8", "#fff", "#fff", customSkins);
		hibernateSQLQueryResultUtilDao.saveAll(customSkins);
		String debugMessage = "Custom Skin Added Successfully";
		logger.debug(debugMessage);
	}

	private void createSkin(CompanyProfile company, Integer userId, CustomModule module, String textColor,
			String buttonBorderColor, String iconColor, String fontFamily, String textContent, String backgroundColor,
			String buttonColor, String buttonValueColor, boolean defaultSkin, boolean showFooter, String divColor,
			String headerTextColor, String iconBorderColor, String iconHoverColor, List<CustomSkin> customSkins) {

		CustomSkin skin = new CustomSkin();
		skin.setCompanyProfile(company);
		skin.setModuleType(module);
		skin.setTextColor(textColor);
		skin.setButtonBorderColor(buttonBorderColor);
		skin.setIconColor(iconColor);
		skin.setFontFamily(fontFamily);
		skin.setTextContent(textContent);
		skin.setBackgroundColor(backgroundColor);
		skin.setButtonColor(buttonColor);
		skin.setButtonValueColor(buttonValueColor);
		skin.setDefaultSkin(defaultSkin);
		skin.setShowFooter(showFooter);
		skin.setDivBgColor(divColor);
		skin.setIconBorderColor(iconBorderColor);
		skin.setIconHoverColor(iconHoverColor);
		skin.setCreatedUserId(userId);
		skin.setCreatedTime(new Date());
		skin.setUpdatedTime(new Date());
		skin.setUpdatedUserId(userId);
		customSkins.add(skin);
	}

	public void insertDefaultThemes() {

		List<Theme> themes = new ArrayList<Theme>();

		CompanyProfile company = new CompanyProfile();
		company.setId(1);

		createTheme(company, 1, "Light", "Default or Light Theme", 1,
				"assets/images/theme/Final/light-theme-custom.webp", Theme.ThemeStatus.LIGHT, null, themes);

		createTheme(company, 1, "Dark", "Dark Theme", 2, "assets/images/theme/Final/dark-theme-custom.webp",
				Theme.ThemeStatus.DARK, null, themes);

		createTheme(company, 1, "Neumorphism Light", "Neumorphism Light", 3,
				"assets/images/theme/Final/light-theme-custom.webp", Theme.ThemeStatus.NEUMORPHISMLIGHT, null, themes);

		createTheme(company, 1, "Neumorphism Dark", "Neumorphism Dark", 4,
				"assets/images/theme/Final/dark-theme-custom.webp", Theme.ThemeStatus.NEUMORPHISMDARK, null, themes);

		createTheme(company, 1, "Glassomorphism Light", "Glassomorphism Light", 5,
				"assets/images/theme/Final/glassomorphism-light.webp", Theme.ThemeStatus.GLASSMORPHISMLIGHT,
				"/assets/images/glassmorphism-images/glassomorphism.png", themes);

		createTheme(company, 1, "Glassomorphism Dark", "Glassomorphism Dark", 6,
				"assets/images/theme/Final/glassomorphism-dark.webp", Theme.ThemeStatus.GLASSMORPHISMDARK,
				"/assets/images/glassmorphism-images/glassomorphism-dark.jpg", themes);

		hibernateSQLQueryResultUtilDao.saveAll(themes);
		String debugMessage = "Themes Added Successfully";
		logger.debug(debugMessage);

	}

	private void createTheme(CompanyProfile company, Integer userId, String name, String description, Integer parentId,
			String themeImagePath, Theme.ThemeStatus parentThemeName, String backgroundImage, List<Theme> themes) {

		Theme theme = new Theme();
		theme.setId(parentId);
		theme.setCompanyProfile(company);
		theme.setName(name);
		theme.setDescription(description);
		theme.setDefaultTheme(true);
		theme.setParentId(parentId);
		theme.setThemeImagePath(themeImagePath);
		theme.setParentThemeName(parentThemeName);
		theme.setBackgroundImage(backgroundImage);
		theme.setCreatedTime(new Date());
		theme.setCreatedUserId(userId);
		theme.setUpdatedTime(new Date());
		theme.setUpdatedUserId(userId);
		themes.add(theme);
	}

	public XtremandResponse addDomains() {
		XtremandResponse response = new XtremandResponse();
		List<CompanyAndRolesDTO> companyDetailsDTOs = teamDao.findCompanyDetailsAndRoles();
		int total = companyDetailsDTOs.size();
		int counter = 0;
		List<Integer> companyIds = new ArrayList<>();
		for (CompanyAndRolesDTO companyDetailsDTO : companyDetailsDTOs) {
			Integer userId = companyDetailsDTO.getUserId();
			Integer companyId = companyDetailsDTO.getCompanyId();
			if (XamplifyUtils.isNotEmptyList(companyIds) && companyIds.indexOf(companyId) < 0) {
				String adminDomain = XamplifyUtils.getEmailDomain(companyDetailsDTO.getEmailId()).trim().toLowerCase();
				Domain domain = new Domain();
				domain.setDomainName(adminDomain);
				CompanyProfile company = new CompanyProfile();
				company.setId(companyId);
				domain.setCompany(company);
				domain.setCreatedUserId(userId);
				domain.setCreatedTime(new Date());
				domain.setUpdatedTime(domain.getCreatedTime());
				domain.setUpdatedUserId(userId);
				genericDAO.save(domain);
				String domainAddedSuccessfully = adminDomain + " Added Successfully For " + companyId;
				logger.debug(domainAddedSuccessfully);
			} else {
				String skippedMessage = "************************S k i p p e d *******************" + companyId;
				logger.debug(skippedMessage);
			}

			counter++;
			companyIds.add(companyId);
			int itemsLeft = total - counter;
			String debugMessage = "Total Companies Left : " + itemsLeft;
			logger.debug(debugMessage);

		}
		response.setStatusCode(200);
		response.setMessage("Domains Added Successfully");
		return response;
	}

	public boolean setDAMAccess(LeftSideNavigationBarItem leftSideNavigationBarItem, Integer userId,
			List<Integer> roleIds, boolean isAnyAdmin) {
		boolean damRole = roleIds.indexOf(Role.DAM.getRoleId()) > -1;
		boolean damAccess = utilDao.hasDamAccessByUserId(userId);
		leftSideNavigationBarItem.setDam(damAccess && (isAnyAdmin || damRole));
		return damRole;
	}

	public boolean setLearningTrackAccess(LeftSideNavigationBarItem leftSideNavigationBarItem, Integer userId,
			List<Integer> roleIds, boolean isAnyAdmin) {
		boolean lmsRole = roleIds.indexOf(Role.LEARNING_TRACK.getRoleId()) > -1;
		boolean lmsAccess = utilDao.hasLmsAccessByUserId(userId);
		leftSideNavigationBarItem.setLms(lmsAccess && (isAnyAdmin || lmsRole));
		return lmsRole;
	}

	public boolean setPlayBookAccess(LeftSideNavigationBarItem leftSideNavigationBarItem, Integer userId,
			List<Integer> roleIds, boolean isAnyAdmin) {
		boolean playBookRole = roleIds.indexOf(Role.PLAY_BOOK.getRoleId()) > -1;
		boolean playBookAccess = utilDao.hasPlaybookAccessByUserId(userId);
		leftSideNavigationBarItem.setPlaybook(playBookAccess && (isAnyAdmin || playBookRole));
		return playBookRole;
	}

	/**** XNFR-574 ***/
	public boolean setOpportunitiesAccess(LeftSideNavigationBarItem leftSideNavigationBarItem, Integer userId,
			List<Integer> roleIds, boolean isAnyAdmin) {
		boolean opportunitiesRole = roleIds.indexOf(Role.OPPORTUNITY.getRoleId()) > -1;
		boolean opportunitiesAccess = utilDao.hasEnableLeadsAccessByUserId(userId);
		leftSideNavigationBarItem.setOpportunities(opportunitiesAccess && (isAnyAdmin || opportunitiesRole));
		return opportunitiesRole;
	}

	public void setOpportunitiesAccessForVendorVanityLogin(LeftSideNavigationBarItem leftSideNavigationBarItem,
			Integer vendorCompanyId, RoleDisplayDTO roleDisplayDto) {
		boolean opportuntiesRole = roleDisplayDto.getRoleIds().indexOf(Role.OPPORTUNITY.getRoleId()) > -1;
		boolean opportunitiesAccessAsPartner = false;
		if (roleDisplayDto.isPartner() || roleDisplayDto.isPartnerTeamMember()) {
			opportunitiesAccessAsPartner = opportuntiesRole || roleDisplayDto.isPartner();
		} else {
			opportunitiesAccessAsPartner = roleDisplayDto.anyAdminAndPartnerRole() || opportuntiesRole;
		}
		boolean isVendorGivenAccessToPartner = utilDao.fetchModuleAccessForPartnerByModuleIdAndVendorCompanyIdAndUserId(
				vendorCompanyId, 9, leftSideNavigationBarItem.getUserId());
		boolean opportunitiesAccessForVendor = utilDao.hasEnableLeadsAccessByCompanyId(vendorCompanyId);
		leftSideNavigationBarItem.setOpportunitiesAccessAsPartner(
				opportunitiesAccessForVendor && opportunitiesAccessAsPartner && isVendorGivenAccessToPartner);
	}

	/*** XNFR-574 ***/
	public void setDAMAccessForVendorVanityLogin(LeftSideNavigationBarItem leftSideNavigationBarItem,
			Integer vendorCompanyId, RoleDisplayDTO roleDisplayDto) {
		boolean damRole = roleDisplayDto.getRoleIds().indexOf(Role.DAM.getRoleId()) > -1;
		boolean damAccessAsPartner = false;
		if (roleDisplayDto.isPartner() || roleDisplayDto.isPartnerTeamMember()) {
			damAccessAsPartner = damRole || roleDisplayDto.isPartner();
		} else {
			damAccessAsPartner = roleDisplayDto.anyAdminAndPartnerRole() || damRole;
		}
		boolean isAssetSharedToPartnerCompany = damDao.isAssetSharedToPartnerCompanyByPartnerIdAndVendorCompany(
				leftSideNavigationBarItem.getUserId(), vendorCompanyId);
		boolean isVendorGivenAccessToPartner = utilDao.fetchModuleAccessForPartnerByModuleIdAndVendorCompanyIdAndUserId(
				vendorCompanyId, 5, leftSideNavigationBarItem.getUserId());
		leftSideNavigationBarItem.setDamAccessAsPartner(utilDao.hasDamAccessByCompanyId(vendorCompanyId)
				&& (damAccessAsPartner || isAssetSharedToPartnerCompany) && isVendorGivenAccessToPartner);
	}

	public void setPlayBookAccessForVendorVanityLogin(LeftSideNavigationBarItem leftSideNavigationBarItem,
			Integer vendorCompanyId, RoleDisplayDTO roleDisplayDto) {
		boolean playBooksRole = roleDisplayDto.getRoleIds().indexOf(Role.PLAY_BOOK.getRoleId()) > -1;
		boolean playBookAccessAsPartner = false;
		if (roleDisplayDto.isPartner() || roleDisplayDto.isPartnerTeamMember()) {
			playBookAccessAsPartner = playBooksRole || roleDisplayDto.isPartner();
		} else {
			playBookAccessAsPartner = roleDisplayDto.anyAdminAndPartnerRole() || playBooksRole;
		}

		boolean isPlaybookSharedToPartnerCompany = lmsDao.isLMSSharedToPartnerCompanyByPartnerId(
				leftSideNavigationBarItem.getUserId(), LearningTrackType.PLAYBOOK, vendorCompanyId);
		boolean isVendorGivenAccessToPartner = utilDao.fetchModuleAccessForPartnerByModuleIdAndVendorCompanyIdAndUserId(
				vendorCompanyId, 12, leftSideNavigationBarItem.getUserId());
		leftSideNavigationBarItem.setPlaybookAccessAsPartner(utilDao.hasPlaybookAccessByCompanyId(vendorCompanyId)
				&& (playBookAccessAsPartner || isPlaybookSharedToPartnerCompany) && isVendorGivenAccessToPartner);
	}

	public void setLearningTracksAccessForVendorVanityLogin(LeftSideNavigationBarItem leftSideNavigationBarItem,
			Integer vendorCompanyId, RoleDisplayDTO roleDisplayDto) {
		boolean lmsRole = roleDisplayDto.getRoleIds().indexOf(Role.LEARNING_TRACK.getRoleId()) > -1;
		boolean lmsAccessAsPartner = false;
		if (roleDisplayDto.isPartner() || roleDisplayDto.isPartnerTeamMember()) {
			lmsAccessAsPartner = lmsRole || roleDisplayDto.isPartner();
		} else {
			lmsAccessAsPartner = roleDisplayDto.anyAdminAndPartnerRole() || lmsRole;
		}

		boolean isTrackSharedToPartnerCompany = lmsDao.isLMSSharedToPartnerCompanyByPartnerId(
				leftSideNavigationBarItem.getUserId(), LearningTrackType.TRACK, vendorCompanyId);
		boolean isVendorGivenAccessToPartner = utilDao.fetchModuleAccessForPartnerByModuleIdAndVendorCompanyIdAndUserId(
				vendorCompanyId, 18, leftSideNavigationBarItem.getUserId());
		leftSideNavigationBarItem.setLmsAccessAsPartner(utilDao.hasLmsAccessByCompanyId(vendorCompanyId)
				&& (lmsAccessAsPartner || isTrackSharedToPartnerCompany) && isVendorGivenAccessToPartner);
	}

	public void mergeCompanies(Company parentCompany, List<Company> mergingCompanies, Integer loggedInCompanyId) {
		if (parentCompany != null && mergingCompanies != null && !mergingCompanies.isEmpty()) {
			List<Integer> mergingCompanyIds = mergingCompanies.stream().map(Company::getId)
					.collect(Collectors.toList());
			Integer parentCompanyId = parentCompany.getId();
			String parentCompanyName = parentCompany.getName();
			UserList parentCompanyContactListId = parentCompany.getContactList();
			mergingCompanyIds.remove(parentCompanyId);
			if (parentCompanyId != null && parentCompanyContactListId != null
					&& parentCompanyContactListId.getId() != null && !StringUtils.isEmpty(parentCompanyName)
					&& !mergingCompanyIds.isEmpty()) {
				companyDAO.mergeCompanyContacts(parentCompanyId, parentCompanyContactListId.getId(), parentCompanyName,
						mergingCompanyIds, loggedInCompanyId);
				companyDAO.updateCompanyOnAllContacts(parentCompanyId, parentCompanyName, mergingCompanyIds);
				companyDAO.deleteCompanyContactLists(mergingCompanyIds);
				companyDAO.deleteCompanies(mergingCompanyIds);
			}

		}
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse addAsPartner(SignUpRequestDTO signUpRequestDto,
			UserListOperationsAsyncDTO userListOperationsAsyncDTO) {
		try {
			XtremandResponse response = new XtremandResponse();
			Integer companyId = userDao.getCompanyIdByProfileName(signUpRequestDto.getCompanyProfileName());
			signUpRequestDto.setCompanyId(companyId);
			String emailId = signUpRequestDto.getEmailId();
			validateDomainAndAddAsPartner(signUpRequestDto, userListOperationsAsyncDTO, response, companyId, emailId);
			return response;
		} catch (BadRequestException e) {
			throw new BadRequestException(e.getMessage());
		} catch (HibernateException | TeamMemberDataAccessException u) {
			throw new TeamMemberDataAccessException(u);
		} catch (Exception ex) {
			throw new TeamMemberDataAccessException(ex);
		}
	}

	private void validateDomainAndAddAsPartner(SignUpRequestDTO signUpRequestDto,
			UserListOperationsAsyncDTO userListOperationsAsyncDTO, XtremandResponse response, Integer companyId,
			String emailId) {
		List<String> domains = domainDao.findAllDomainNames(companyId, DomainModuleNameType.PARTNER);
		String domainName = XamplifyUtils.getEmailDomain(emailId);
		boolean isValidDomain = XamplifyUtils.isNotEmptyList(domains) && domains.indexOf(domainName) > -1;
		List<String> deactivatedDomains = partnershipDao.findDeactivatedDomainsByCompanyId(companyId);
		boolean isDeactivatedDomain = XamplifyUtils.isNotEmptyList(deactivatedDomains)
				&& deactivatedDomains.indexOf(domainName) > -1;
		if (isValidDomain && !isDeactivatedDomain) {
			Integer companyIdByEmailId = userDao.getCompanyIdByEmailId(emailId);
			Integer partnerId = userDao.getUserIdByEmail(emailId);
			boolean isCompanyProfileExists = XamplifyUtils.isValidInteger(companyIdByEmailId);
			boolean isAccountAlredayExists = XamplifyUtils.isValidInteger(partnerId);
			signUpRequestDto.setAccountAlreadyExists(isAccountAlredayExists);
			boolean isDuplicatePartnerCompanyName = false;
			boolean isPartnershipCanBeEstablished = false;
			boolean isPartnershipAlreadyEstablished = false;
			boolean isDomainAllowedToAddToSameAccount = domainDao
					.checkIfDomainIsAllowedToAddToSamePartnerAccount(domainName, companyId);
			if (isCompanyProfileExists) {
				isPartnershipAlreadyEstablished = validatePartnershipByPartnerCompany(companyId, companyIdByEmailId);
			} else if (isAccountAlredayExists) {
				isPartnershipAlreadyEstablished = validatePartnershipByPartnerId(companyId, partnerId);
			}
			isDuplicatePartnerCompanyName = validateDuplicatePartnerCompany(companyId, emailId,
					signUpRequestDto.getCompanyName(), isDomainAllowedToAddToSameAccount);
			isPartnershipCanBeEstablished = !isDuplicatePartnerCompanyName && !isPartnershipAlreadyEstablished;
			validatAccountCreationAndCreatePartnerAccount(signUpRequestDto, userListOperationsAsyncDTO, response,
					companyId, partnerId, isPartnershipCanBeEstablished, isDomainAllowedToAddToSameAccount);
		} else if (isDeactivatedDomain) {
			String deactivatedDomainEmailErrorMessage = "Following email domain has been deactivated";
			throw new BadRequestException(deactivatedDomainEmailErrorMessage);
		} else {
			String companySupportEmailId = userDao.getSupportEmailIdByCompanyId(companyId);
			String invalidDomainEmailErrorMessage = XamplifyConstants.INVALID_DOMAIN_EMAIL;
			if (StringUtils.hasText(companySupportEmailId)) {
				invalidDomainEmailErrorMessage += " or contact " + companySupportEmailId;
			}
			throw new BadRequestException(invalidDomainEmailErrorMessage);
		}
	}

	private void validatAccountCreationAndCreatePartnerAccount(SignUpRequestDTO signUpRequestDto,
			UserListOperationsAsyncDTO userListOperationsAsyncDTO, XtremandResponse response, Integer companyId,
			Integer partnerId, boolean isPartnershipCanBeEstablished, boolean isDomainAllowedToAddToSameAccount) {
		if (isPartnershipCanBeEstablished || isDomainAllowedToAddToSameAccount) {
			Integer partnerCompanyId = userDao.getCompanyIdByEmailId(signUpRequestDto.getEmailId());
			if (companyId.equals(partnerCompanyId)) {
				throw new BadRequestException("You can't add a member of your organization as partner");
			} else {
				isAccountAlreadyExistsWithPassword(signUpRequestDto, partnerId);
				if (isDomainAllowedToAddToSameAccount) {
					createPartnerOrAddPartnerTeamMember(signUpRequestDto, companyId, userListOperationsAsyncDTO,
							response);
				} else {
					createPartnerAccount(signUpRequestDto, userListOperationsAsyncDTO, response, companyId);
				}
			}
		}
	}

	private void isAccountAlreadyExistsWithPassword(SignUpRequestDTO signUpRequestDto, Integer partnerId) {
		boolean isPasswordExists = userDao.isPasswordExists(signUpRequestDto.getEmailId().toLowerCase().trim());
		signUpRequestDto.setPasswordExists(isPasswordExists);
		if (XamplifyUtils.isValidInteger(partnerId) && isPasswordExists && !signUpRequestDto.isSkipPassword()) {
			throw new BadRequestException(XamplifyConstants.ACCOUNT_ALREADY_EXISTS);
		}
	}

	private void createPartnerAccount(SignUpRequestDTO signUpRequestDto,
			UserListOperationsAsyncDTO userListOperationsAsyncDTO, XtremandResponse response, Integer companyId) {
		XamplifyUtils.addSuccessStatus(response);
		Set<UserDTO> partners = new HashSet<>();
		UserDTO partnerDto = new UserDTO();
		partnerDto.setEmailId(signUpRequestDto.getEmailId());
		partnerDto.setContactCompany(signUpRequestDto.getCompanyName());
		partnerDto.setContactsLimit(1);
		partnerDto.setFirstName(signUpRequestDto.getFirstName());
		partnerDto.setLastName(signUpRequestDto.getLastName());
		partnerDto.setNotifyPartners(false);
		boolean isUpdatePassword = !signUpRequestDto.isSkipPassword();
		if (isUpdatePassword) {
			partnerDto.setPassword(passwordEncoder.encode(signUpRequestDto.getPassword()));
			signUpRequestDto.setPasswordUpdated(true);
		}
		partnerDto.setSignUpUsingVendorLink(true);
		partners.add(partnerDto);
		Integer vendorAdminId = teamDao.findPrimaryAdminIdByCompanyId(companyId);
		userListOperationsAsyncDTO.setVendorAdminId(vendorAdminId);
		Integer defaultPartnerListId = userListDao.getDefaultPartnerListIdByCompanyId(companyId);
		String companyProfileName = signUpRequestDto.getCompanyProfileName();
		userListOperationsAsyncDTO.setPartnerList(true);
		UserList partnerList = userListDao.findByPrimaryKey(defaultPartnerListId,
				new FindLevel[] { FindLevel.COMPANY_PROFILE });
		partnerList.setSignUpUsingVendorLink(true);
		Map<String, Object> map = partnershipService.updatePartnerList(partnerList, partners, vendorAdminId,
				companyProfileName, userListOperationsAsyncDTO);
		Integer statusCode = (Integer) map.get("statusCode");
		updateExistingAccountPasswordAndActivateTheAccount(signUpRequestDto, partnerDto, isUpdatePassword);
		userListOperationsAsyncDTO.setStatusCode(statusCode);
		response.setMessage(partnershipEstablishedSuccesMessage);
	}

	private void updateExistingAccountPasswordAndActivateTheAccount(SignUpRequestDTO signUpRequestDto,
			UserDTO partnerDto, boolean isUpdatePassword) {
		boolean isPasswordExists = signUpRequestDto.isPasswordExists();
		boolean isAccountAlreadyExits = signUpRequestDto.isAccountAlreadyExists();
		if (!isPasswordExists && isUpdatePassword && isAccountAlreadyExits) {
			User partnerUser = userDao.getUserByEmail(signUpRequestDto.getEmailId());
			if (partnerUser != null) {
				Integer partnerId = partnerUser.getUserId();
				boolean isUnApproved = UserStatus.UNAPPROVED.equals(partnerUser.getUserStatus());
				if (isUnApproved) {
					partnerUser.setUserStatus(UserStatus.APPROVED);
					partnerUser.setActivatedTime(new Date());
				}
				partnerUser.setPassword(partnerDto.getPassword());
				teamService.changeTeamMemberStatus(partnerId);
			}
		}
	}

	private boolean validatePartnershipByPartnerCompany(Integer companyId, Integer companyIdByEmailId) {
		boolean isPartnershipAlreadyEstablished;
		isPartnershipAlreadyEstablished = partnershipDao
				.isPartnershipEstablishedByVendorCompanyIdAndPartnerCompanyId(companyId, companyIdByEmailId);
		if (isPartnershipAlreadyEstablished) {
			throw new BadRequestException(XamplifyConstants.DUPLICATE_PARTNERSHIP);
		}
		return isPartnershipAlreadyEstablished;
	}

	private boolean validatePartnershipByPartnerId(Integer companyId, Integer partnerId) {
		boolean isPartnershipAlreadyEstablished;
		PartnershipDTO partnershipDto = partnershipDao.getPartnerShipByParnterIdAndVendorCompanyId(partnerId,
				companyId);
		isPartnershipAlreadyEstablished = partnershipDto != null;
		if (isPartnershipAlreadyEstablished) {
			throw new BadRequestException(XamplifyConstants.DUPLICATE_PARTNERSHIP);
		}
		return isPartnershipAlreadyEstablished;
	}

	private boolean validateDuplicatePartnerCompany(Integer companyId, String emailId, String contactCompany,
			boolean isDomainAllowedToAddToSameAccount) {
		boolean isDuplicatePartnerCompanyName;
		List<User> inputPartners = new ArrayList<>();
		User partner = new User();
		partner.setEmailId(emailId);
		partner.setContactCompany(contactCompany.trim().toLowerCase());
		inputPartners.add(partner);
		XtremandResponse partnerCompanyResponse = new XtremandResponse();
		partnerCompanyResponse = validatePartnerCompany(inputPartners, partnerCompanyResponse, false, true, companyId);
		isDuplicatePartnerCompanyName = partnerCompanyResponse.getStatusCode() == 400;
		if (isDuplicatePartnerCompanyName && !isDomainAllowedToAddToSameAccount) {
			throw new BadRequestException("Company name has already been added.");
		}
		return isDuplicatePartnerCompanyName;
	}

	public void setOtherActiveCRMPipeline(Deal deal, Integration otherActiveCRMIntegration) {
		if (otherActiveCRMIntegration != null) {
			List<Pipeline> pipelines = pipelineDAO.getPipelinesByIntegrationType(
					otherActiveCRMIntegration.getCompany().getId(), PipelineType.DEAL,
					otherActiveCRMIntegration.getType(), false);
			if (pipelines != null) {
				Pipeline otherActiveCRMPipeline = pipelines.get(0);
				if (otherActiveCRMPipeline != null) {
					PipelineStage defaultStage = pipelineDAO.getDefaultStage(otherActiveCRMPipeline.getId());
					setPipelines(deal, otherActiveCRMIntegration.getCompany().getId(), otherActiveCRMPipeline,
							defaultStage);
				}

			}
		}
	}

	public XtremandResponse validatePartnerCompany(List<User> inputPartners, XtremandResponse response,
			boolean isCompanyProfileSubmit, boolean isAdd, Integer vendorCompanyId) {
		List<User> partners = addPartnersToArrayList(inputPartners, isCompanyProfileSubmit, isAdd, vendorCompanyId);

		if (!partners.isEmpty()) {
			List<String> existingCompanyNames = partnershipDao
					.getExistingPartnerCompanyNamesByVendorCompanyId(vendorCompanyId);

			List<String> inputCompanyNames = addInputCompanyNames(partners);

			List<String> duplicateCompanyNames = existingCompanyNames.stream().distinct()
					.filter(inputCompanyNames::contains).collect(Collectors.toList());

			response = validateDuplicateCompanyNames(response, duplicateCompanyNames);

			response = addDuplicateInputCompanyNamesAndValidateCompanyNames(response, inputCompanyNames,
					duplicateCompanyNames);
		}
		return response;

	}

	private List<String> addInputCompanyNames(List<User> partners) {
		List<String> inputCompanyNames = new ArrayList<>();
		for (User user : partners) {
			if (user.getContactCompany() != null && user.getContactCompany().trim().length() > 0) {
				inputCompanyNames.add(user.getContactCompany().trim().toLowerCase());
			}
		}
		return inputCompanyNames;
	}

	private XtremandResponse addDuplicateInputCompanyNamesAndValidateCompanyNames(XtremandResponse response,
			List<String> inputCompanyNames, List<String> duplicateCompanyNames) {
		if ((duplicateCompanyNames == null || duplicateCompanyNames.isEmpty())
				&& (inputCompanyNames != null && !inputCompanyNames.isEmpty())) {
			List<String> duplicateInputCompanyNames = new ArrayList<>();
			Set<String> set = new HashSet<>();
			for (String name : inputCompanyNames) {
				if (!set.add(name.trim()))
					duplicateInputCompanyNames.add(name.trim());
			}
			response = addDuplicateCompanyNamesErrorMessage(response, duplicateInputCompanyNames);
		}
		return response;
	}

	private XtremandResponse addDuplicateCompanyNamesErrorMessage(XtremandResponse response,
			List<String> duplicateInputCompanyNames) {
		if (!duplicateInputCompanyNames.isEmpty()) {
			Set<String> duplicateInputCompanyNamesSet = new HashSet<>(duplicateInputCompanyNames);
			if (!duplicateInputCompanyNamesSet.isEmpty()) {
				response = new XtremandResponse();
				response.setStatusCode(400);
				if (duplicateInputCompanyNamesSet.size() == 1) {
					response.setMessage("Following company name is duplicated");
				} else {
					response.setMessage("Following company names are duplicated");
				}
				response.setData(duplicateInputCompanyNamesSet);
			}
		}
		return response;
	}

	private XtremandResponse validateDuplicateCompanyNames(XtremandResponse response,
			List<String> duplicateCompanyNames) {
		if (duplicateCompanyNames != null && !duplicateCompanyNames.isEmpty()) {
			response = new XtremandResponse();
			response.setStatusCode(400);
			if (duplicateCompanyNames.size() == 1) {
				response.setMessage("Following company name has already been added");
			} else {
				response.setMessage("Following company names has already been added");
			}
			response.setData(duplicateCompanyNames);
		}
		return response;
	}

	private List<User> addPartnersToArrayList(List<User> inputPartners, boolean isCompanyProfileSubmit, boolean isAdd,
			Integer vendorCompanyId) {
		if (inputPartners != null && !inputPartners.isEmpty()) {
			inputPartners.removeAll(Collections.singleton(null));
		}
		List<User> partners = new ArrayList<>();
		if (!isCompanyProfileSubmit) {
			for (User partner : inputPartners) {
				Integer userId = userDao.getUserIdByEmail(partner.getEmailId());
				if (userId != null && isAdd) {
					if (!userListDao.isUserExistsInDefaultPartnerList(userId, vendorCompanyId)) {
						partners.add(partner);
					}
				} else {
					partners.add(partner);
				}
			}
		} else {
			partners.addAll(inputPartners);
		}
		return partners;
	}

	public XtremandResponse findTeamMemberSignUpUrl(SignUpRequestDTO signUpRequestDTO) {
		XtremandResponse response = new XtremandResponse();
		String companyName = signUpRequestDTO.getCompanyName();
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String queryString = "select xcp.company_profile_name \r\n"
				+ "from xt_company_profile xcp,xt_company_profile xcp2\r\n"
				+ "where LOWER(xcp.company_name)  = LOWER(:companyName)\r\n"
				+ "and xcp.added_admin_company_id = xcp2.company_id  and xcp2.company_profile_name=:companyProfileName";
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("companyName", companyName));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("companyProfileName", utilDao.getPrmCompanyProfileName()));
		String companyProfileName = (String) hibernateSQLQueryResultUtilDao
				.getUniqueResult(hibernateSQLQueryResultRequestDTO);
		boolean isCompanyProfileNameExists = StringUtils.hasText(companyProfileName);
		if (isCompanyProfileNameExists) {
			Integer adminIdByCompanyId = teamDao.findPrimaryAdminIdByCompanyProfileName(companyProfileName);
			response = domainService.getTeamMemberSignUpUrl(adminIdByCompanyId,
					signUpRequestDTO.isAccessedFromVanityDomain(), companyProfileName);
		} else {
			response.setStatusCode(404);
			response.setMessage("Team member signup url is not available for " + companyName);
		}
		return response;
	}

	public boolean isNumeric(String str) {
		try {
			Double.parseDouble(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public boolean isDate(String str) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss:ms");
		dateFormat.setLenient(false);
		try {
			dateFormat.parse(str.trim());
			return true;
		} catch (ParseException | java.text.ParseException pe) {
			return false;
		}
	}

	public boolean isEmail(String str) {
		Pattern pattern = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}");
		Matcher mat = pattern.matcher(str);
		if (mat.matches()) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isURL(String str) {
		try {
			URL url = new URL(str);
			url.toURI();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean isPhone(String str) {
		Pattern pattern = Pattern.compile("^[0-9\\-]*$");
		Matcher mat = pattern.matcher(str);
		if (mat.matches()) {
			return true;
		} else {
			return false;
		}
	}

	private boolean isNumberDropdown(FormLabel formLabel) {
		boolean isPercentDropdown = true;
		List<FormLabelChoice> choices = formLabel.getFormLabelChoices();
		if (choices != null && !choices.isEmpty()) {
			for (FormLabelChoice choice : choices) {
				if (choice != null && !StringUtils.isEmpty(choice.getLabelChoiceName())) {
					if (!isNumeric(choice.getLabelChoiceName())) {
						isPercentDropdown = false;
						break;
					}
				}
			}
		} else {
			isPercentDropdown = false;
		}
		return isPercentDropdown;
	}

	public String replaceCompanyWebsite(String websiteLinkTag, String website, String htmlBody) {
		website = getWebsite(website);
		htmlBody = htmlBody.replace(websiteLinkTag, website);
		return htmlBody;
	}

	public String getWebsite(String website) {
		if (XamplifyUtils.isValidString(website)) {
			boolean result = website.startsWith("http://") || website.startsWith("https://");
			if (!result) {
				website = "https://" + website;
			}
		}

		return website;

	}

	/* XNFR-424 */
	public List<FormLabelDTORow> constructFormRows(List<FormLabelDTO> formLabelDTOs) {
		List<FormLabelDTORow> formLabelDTORows = new ArrayList<>();
		for (FormLabelDTO formLabelDTO : formLabelDTOs) {
			FormLabelDTORow formLabelDTORow = new FormLabelDTORow();
			List<FormLabelDTO> formLabelDTOPerRow = Arrays.asList(formLabelDTO);
			formLabelDTORow.setFormLabelDTOs(formLabelDTOPerRow);
			formLabelDTORows.add(formLabelDTORow);
		}
		return formLabelDTORows;
	}

	/* XNFR-548 */
	public String addPerfixToSubject(String subject) {
		return systemNotificationPrefixMessage + " " + subject;
	}

	public String replacedDescription(String description) {
		return description.replace("&nbsp;", " ");

	}

	public XtremandResponse dashboardButtonPublishedEmailNotification(Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		XamplifyUtils.addSuccessStatus(response);
		response.setData(companyProfileDao.isDashboardButtonPublishedEmailNotificationByUserId(loggedInUserId));
		return response;
	}

	public XtremandResponse dashboardButtonPublishedEmailNotification(String companyProfileName) {
		XtremandResponse response = new XtremandResponse();
		XamplifyUtils.addSuccessStatus(response);
		response.setData(
				companyProfileDao.isDashboardButtonPublishedEmailNotificationByCompanyProfileName(companyProfileName));
		return response;
	}

	public XtremandResponse addDashboardButtonOrders() {
		XtremandResponse response = new XtremandResponse();
		List<DashboardButton> dashboardButtonOrders = vanityUrlDao.getVendorDashboardButtons();
		Set<Integer> companyIds = dashboardButtonOrders.stream().map(x -> x.getCompanyProfile().getId())
				.collect(Collectors.toSet());
		for (Integer companyId : companyIds) {
			int order = 1;
			for (DashboardButton dashboardButton : dashboardButtonOrders) {
				Integer vendorCompanyId = dashboardButton.getCompanyProfile().getId();
				if (companyId.equals(vendorCompanyId)) {
					dashboardButton.setOrder(order);
					dashboardButton.setUpdatedTime(dashboardButton.getTimestamp());
					order++;
				}

			}
		}
		XamplifyUtils.addSuccessStatus(response);
		return response;

	}

	public String getFullName(String firstName, String middleName, String lastName) {
		String displayName = "";
		String updatedFirstName = firstName != null && StringUtils.hasText(firstName) ? firstName : "";
		String updatedLastName = lastName != null && StringUtils.hasText(lastName) ? lastName : "";
		String updatedMiddlName = middleName != null && StringUtils.hasText(middleName) ? middleName : "";
		String fullName = (updatedFirstName + " "
				+ (StringUtils.hasText(updatedMiddlName) ? updatedMiddlName + " " : "") + updatedLastName).trim();
		if (StringUtils.hasText(fullName)) {
			displayName = fullName;
		}
		return displayName;

	}

	public void revokePartnerCompanyUsersAccessTokensByVendorCompanyId(Integer companyId) {
		List<String> emailIds = userDao.findAllPartnerAndPartnerTeamMemberEmailIdsByVendorCompanyId(companyId);
		iterateAndRevokeAccessTokensByEmailAddresses(emailIds);
	}

	public void validateTeamMemberEmailIds(XtremandResponse response, List<String> teamMemberEmailIds) {
		List<String> allEmailIds = new ArrayList<>();
		allEmailIds.addAll(userService.listAllOrgAdminEmailIds());
		allEmailIds.addAll(userService.listAllPartnerEmailIds());
		allEmailIds.addAll(teamDao.listTeamMemberEmailIds());
		List<String> duplicateEmailIds = new ArrayList<>(allEmailIds);
		duplicateEmailIds.retainAll(teamMemberEmailIds);
		if (!duplicateEmailIds.isEmpty()) {
			response.setStatusCode(413);
			Set<String> distinctDuplicateEmailIds = XamplifyUtils.convertListToSet(duplicateEmailIds);
			response.setData(distinctDuplicateEmailIds);
			if (distinctDuplicateEmailIds.size() == 1) {
				response.setMessage(teamMemberErrorMessage);
			} else {
				response.setMessage(teamMembersErrorMessage);
			}
		}
	}

	public boolean isVersaCompany(Integer companyId) {
		if (profiles.equals("production")) {
			return companyId.equals(2181);
		}
		return false;
	}

	public Set<String> getCompanyDomains(String domainString) {
		Set<String> uniqueDomains = new HashSet<>();
		if (!StringUtils.isEmpty(domainString)) {
			String[] domains = domainString.split(companyDoaminRegex);
			for (String domain : domains) {
				// Remove the "@" symbol before the domain if it exists
				String extractedDomain = domain.startsWith("@") ? domain.trim().substring(1) : domain.trim();
				if (extractedDomain != null) {
					uniqueDomains.add(extractedDomain.toLowerCase());
				}
			}
		}
		return uniqueDomains;
	}

	public void createPartnerCompanyDomains(UserDTO userDTO, Integer partnershipId) {
		Set<String> domains = getCompanyDomains(userDTO.getCompanyDomain());
		domains.add(XamplifyUtils.getEmailDomain(userDTO.getEmailId()).toLowerCase().trim());
		Partnership partnership = new Partnership();
		partnership.setId(partnershipId);
		List<String> existingDomains = partnershipDao.getDomainsByPartnershipId(partnershipId);
		if (existingDomains != null && !existingDomains.isEmpty()) {
			domains.removeAll(existingDomains);
		}
		for (String domain : domains) {
			if (XamplifyUtils.isValidString(domain)) {
				PartnerCompanyDomain partnerCompanyDomain = new PartnerCompanyDomain();
				partnerCompanyDomain.setPartnerCompanyDomainName(domain.toLowerCase().trim());
				partnerCompanyDomain.setPartnership(partnership);
				partnerCompanyDomain.setCreatedBy(userDTO.getId());
				partnerCompanyDomain.initialiseCommonFields(true, userDTO.getId());
				genericDAO.save(partnerCompanyDomain);
			}
		}
	}

	public XtremandResponse findRoleIdsAndNames(Integer userId) {
		XtremandResponse response = new XtremandResponse();
		XamplifyUtils.addSuccessStatus(response);
		Map<String, Object> map = teamMemberGroupService.getRoleIdsAndRoleNamesByUserId(userId);
		response.setData(map);
		return response;
	}

	public void setDateFiltersForPartnerJourney(PartnerJourneyRequestDTO partnerJourneyRequestDTO) {
		if (XamplifyUtils.isValidString(partnerJourneyRequestDTO.getTimeZone())) {
			if (XamplifyUtils.isValidString(partnerJourneyRequestDTO.getFromDateFilterInString())) {
				partnerJourneyRequestDTO.setFromDateFilter(DateUtils.convertClientToServerTimeZone(
						partnerJourneyRequestDTO.getFromDateFilterInString() + " 00:00:00",
						partnerJourneyRequestDTO.getTimeZone()));
			}

			if (XamplifyUtils.isValidString(partnerJourneyRequestDTO.getToDateFilterInString())) {
				Date toDate = DateUtils.convertClientToServerTimeZone(
						partnerJourneyRequestDTO.getToDateFilterInString() + " 23:59:59",
						partnerJourneyRequestDTO.getTimeZone());
				Calendar c = Calendar.getInstance();
				c.setTime(toDate);
				c.add(Calendar.MINUTE, 1);
				toDate = c.getTime();
				partnerJourneyRequestDTO.setToDateFilter(toDate);
			}
		}
	}

	public void setDateFiltersForTeamMemberAnalytics(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO) {
		if (XamplifyUtils.isValidString(teamMemberJourneyRequestDTO.getTimeZone())) {
			if (XamplifyUtils.isValidString(teamMemberJourneyRequestDTO.getFromDateFilterInString())) {
				teamMemberJourneyRequestDTO.setFromDateFilter(DateUtils.convertClientToServerTimeZone(
						teamMemberJourneyRequestDTO.getFromDateFilterInString() + " 00:00:00",
						teamMemberJourneyRequestDTO.getTimeZone()));
			}

			if (XamplifyUtils.isValidString(teamMemberJourneyRequestDTO.getToDateFilterInString())) {
				Date toDate = DateUtils.convertClientToServerTimeZone(
						teamMemberJourneyRequestDTO.getToDateFilterInString() + " 23:59:59",
						teamMemberJourneyRequestDTO.getTimeZone());
				Calendar c = Calendar.getInstance();
				c.setTime(toDate);
				c.add(Calendar.MINUTE, 1);
				toDate = c.getTime();
				teamMemberJourneyRequestDTO.setToDateFilter(toDate);
			}
		}
	}

	public XtremandResponse getTeamMemberFilter(Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		boolean filterOption = false;
		if (teamDao.isTeamMember(loggedInUserId)) {
			filterOption = teamDao.getTeamMemberOption(loggedInUserId);
			response.setData(filterOption);
		} else {
			response.setData(filterOption);
		}
		response.setStatusCode(200);
		return response;
	}

	public XtremandResponse findAccountDetails(AccountDetailsRequestDTO accountDetailsRequestDTO) {
		XtremandResponse response = new XtremandResponse();
		String filterType = accountDetailsRequestDTO.getFilterType();
		Integer id = accountDetailsRequestDTO.getCompanyIdOrUserId();
		String emailId = accountDetailsRequestDTO.getEmailId();
		if (COMPANY_ID.equals(filterType)) {
			Integer primaryAdminId = teamDao.findPrimaryAdminIdByCompanyId(id);
			emailId = userDao.getEmailIdByUserId(primaryAdminId);
		} else if ("userId".equals(filterType)) {
			emailId = userDao.getEmailIdByUserId(id);
		}
		if (emailId != null) {
			response = utilDao.findCompanyDetailsByEmailIdOrUserId(emailId);
		} else {
			XamplifyUtils.addErorMessageWithStatusCode(response, "No Details Found", 404);
		}
		return response;
	}

	private void createPartnerOrAddPartnerTeamMember(SignUpRequestDTO signUpRequestDto, Integer companyId,
			UserListOperationsAsyncDTO userListOperationsAsyncDTO, XtremandResponse response) {
		String emailId = signUpRequestDto.getEmailId();
		String domainName = XamplifyUtils.getEmailDomain(emailId);
		List<PartnershipDTO> partnerships = partnershipDao.getPartnershipDtoByPartnerCompanyDomain(domainName,
				companyId);
		if (partnerships != null && !partnerships.isEmpty()) {
			for (PartnershipDTO partnershipDto : partnerships) {
				if (partnershipDto != null) {
					createTeamMember(signUpRequestDto, partnershipDto.getPartnerCompanyId(), userListOperationsAsyncDTO,
							response);
					break;
				}
			}
		} else {
			createPartnerAccount(signUpRequestDto, userListOperationsAsyncDTO, response, companyId);
		}
	}

	private void createTeamMember(SignUpRequestDTO signUpRequestDto, Integer partnerCompanyId,
			UserListOperationsAsyncDTO userListOperationsAsyncDTO, XtremandResponse response) {
		List<String> teamMemberEmailIds = new ArrayList<>();
		teamMemberEmailIds.add(signUpRequestDto.getEmailId());
		validateTeamMemberEmailIds(response, teamMemberEmailIds);
		if (response.getStatusCode() == 413) {
			String debugMessage1 = "Validation failed for Team Member email id:" + signUpRequestDto.getEmailId()
					+ " at:" + new Date();
			logger.debug(debugMessage1);
			throw new BadRequestException(response.getMessage());
		} else {
			List<TeamMember> teamMembers = new ArrayList<>();
			List<User> newUsers = new ArrayList<>();
			Integer primaryAdminId = teamDao.findPrimaryAdminIdByCompanyId(partnerCompanyId);
			User primaryAdminUser = userService.loadUser(
					Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, primaryAdminId)),
					new FindLevel[] { FindLevel.COMPANY_PROFILE, FindLevel.ROLES });
			List<TeamMemberDTO> teamMemberDTOs = new ArrayList<>();
			TeamMemberDTO teamMemberDTO = new TeamMemberDTO();
			teamMemberDTO.setEmailId(signUpRequestDto.getEmailId());
			teamMemberDTO.setFirstName(signUpRequestDto.getFirstName());
			teamMemberDTO.setLastName(signUpRequestDto.getLastName());
			teamMemberDTO.setAddedThroughSignUpLink(true);
			teamMemberDTO.setPassword(passwordEncoder.encode(signUpRequestDto.getPassword()));
			List<TeamMemberGroupDTO> defaultTeamMemerGroups = teamMemberGroupDao
					.findDefaultGroupsByCompanyId(partnerCompanyId);
			Integer teamMemberGroupId = null;
			if (defaultTeamMemerGroups == null || defaultTeamMemerGroups.isEmpty()) {
				teamMemberGroupId = userService.addPartnerManagerDefaultGroup(primaryAdminId, partnerCompanyId);
				logger.debug("Default team member group created by team member for partner. at:" + new Date());
			} else {
				teamMemberGroupId = defaultTeamMemerGroups.get(0).getId();
			}
			teamMemberDTO.setTeamMemberGroupId(teamMemberGroupId);
			teamMemberDTO.setUserId(primaryAdminId);
			teamMemberDTOs.add(teamMemberDTO);
			userListOperationsAsyncDTO.setTeamMembers(teamMemberDTOs);
			userListOperationsAsyncDTO.setVendorAdminId(primaryAdminId);
			teamService.iterateDtosAndAddTeamMembers(teamMemberDTOs, primaryAdminUser, teamMembers, newUsers);
			response.setStatusCode(200);
			response.setMessage(addedAsTeamMemberSucesssMessage);
		}
	}

	public XtremandResponse getPartnerCompanyByEmailDomain(String emailId, String vendorCompanyProfileName) {
		String companyName = "";
		XtremandResponse response = new XtremandResponse();
		if (XamplifyUtils.isValidString(vendorCompanyProfileName) && XamplifyUtils.isValidString(emailId)) {
			Integer vendorCompanyId = userDao.getCompanyIdByProfileName(vendorCompanyProfileName);
			String domain = XamplifyUtils.getEmailDomain(emailId.trim().toLowerCase());
			List<String> domains = domainDao.findAllDomainNames(vendorCompanyId, DomainModuleNameType.PARTNER);
			boolean isValidDomain = XamplifyUtils.isNotEmptyList(domains) && domains.indexOf(domain) > -1;
			if (isValidDomain) {
				boolean isDomainAllowedToAddToSameAccount = domainDao
						.checkIfDomainIsAllowedToAddToSamePartnerAccount(domain, vendorCompanyId);
				if (isDomainAllowedToAddToSameAccount) {
					List<String> companyNames = userDao.getPartnerCompanyByEmailDomain(domain, vendorCompanyId);
					if (XamplifyUtils.isNotEmptyList(companyNames)) {
						companyName = companyNames.get(0);
					}
				}
			}
		}
		XamplifyUtils.addSuccessStatus(response);
		response.setData(companyName);
		return response;
	}

	public XtremandResponse findVendorCompanies(String emailAddress) {
		Integer userId = getUserId(emailAddress);
		if (!XamplifyUtils.isValidInteger(userId)) {
			return createErrorResponse("Account does not exist.");
		}
		Integer companyId = userDao.getCompanyIdByUserId(userId);
		String accountType = getAccountType(userId, companyId);
		if (PARTNER.equals(accountType)) {
			return createSuccessResponse(userId, companyId);
		} else {
			return createErrorResponse("The provided email address is associated with " + accountType
					+ " account type. Merging is restricted to the partner company only.");
		}
	}

	private Integer getUserId(String emailAddress) {
		return userDao.getUserIdByEmail(emailAddress);
	}

	private String getAccountType(Integer userId, Integer companyId) {
		String accountType = utilDao.findRoleByUserId(userId);

		if (XamplifyUtils.isValidInteger(companyId) && !PARTNER.equals(accountType) && !"User".equals(accountType)) {
			List<Integer> roleIds = utilDao.findRoleIdsByCompanyId(companyId);
			boolean hasPartnerRole = XamplifyUtils.isNotEmptyList(roleIds)
					&& roleIds.contains(Role.COMPANY_PARTNER.getRoleId());

			if (hasPartnerRole) {
				accountType += " & " + PARTNER;
			}
		}
		return accountType;
	}

	private XtremandResponse createSuccessResponse(Integer partnerUserId, Integer partnerCompanyId) {
		XtremandResponse response = new XtremandResponse();
		List<CompanyDTO> vendorCompanies = partnershipDao.findVendorCompanyDetailsByPartnerUserId(partnerUserId);
		boolean isPartnershipEstablishedWithMoreThanOneVendorCompany = vendorCompanies != null
				&& !vendorCompanies.isEmpty() && vendorCompanies.size() > 1;
		if (isPartnershipEstablishedWithMoreThanOneVendorCompany) {
			response.setStatusCode(400);
			response.setMessage(
					"Merging is not applicable for the entered partner email address as it has partnership with multiple vendors");
		} else {
			response.setStatusCode(200);
			response.setData(vendorCompanies);
			String partnerCompanyName = userDao.getCompanyNameByUserId(partnerUserId);
			Map<String, Object> map = new HashMap<>();
			map.put("partnerCompanyName", partnerCompanyName);
			map.put("partnerCompanyId", partnerCompanyId);
			response.setMap(map);
		}
		return response;
	}

	private XtremandResponse createErrorResponse(String message) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		response.setMessage(message);
		return response;
	}

	public void frameAndSaveModuleCustomsForPartner(Integer companyId, Partnership partnership, List<Module> modules,
			Map<Integer, Boolean> modulesMap) {
		if (XamplifyUtils.isNotEmptyList(modules) && XamplifyUtils.isValidInteger(companyId)) {
			for (Module module : modules) {
				Integer partnershipId = (partnership != null) ? partnership.getId() : null;
				if (!moduleDao.checkModuleCustom(partnershipId, companyId, module.getId())) {
					saveModuleCustom(companyId, partnership, modulesMap, module);
				}
			}
		}
	}

	private void saveModuleCustom(Integer companyId, Partnership partnership, Map<Integer, Boolean> modulesMap,
			Module module) {
		if (module.getParentModule() == null) {
			ModuleCustom moduleCustom = new ModuleCustom();
			moduleCustom.setCustomName(module.getModuleName());
			moduleCustom.setModule(module);
			moduleCustom.setCompanyId(companyId);
			moduleCustom.setCreatedTime(new Date());
			moduleCustom.setCreatedUserId(1);
			moduleCustom.setUpdatedTime(new Date());
			moduleCustom.setDisplayIndex(moduleDisplayIndexMap.get(module.getId()));
			moduleCustom.setUpdatedUserId(1);
			moduleCustom.setPartnership(partnership);
			boolean isPartnerAccessModule = true;
			if (modulesMap.containsKey(module.getId())) {
				isPartnerAccessModule = modulesMap.get(module.getId());
			}
			moduleCustom.setCanPartnerAccessModule(isPartnerAccessModule);
			moduleCustom.setMarketingModule(false);
			genericDAO.save(moduleCustom);
		}
	}

	public XtremandResponse checkVanityAccess(Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		if (XamplifyUtils.isValidInteger(loggedInUserId)) {
			boolean hasVanityAccess = utilDao.hasVanityAccessByUserId(loggedInUserId);
			response.setData(hasVanityAccess);
			XamplifyUtils.addSuccessStatus(response);
		}
		return response;
	}

	/** XNFR-976 **/
	public String setProxyAssetPath(String assetPath) throws UnsupportedEncodingException {
		String baseUrl;
		String encodedUrl;
		encodedUrl = URLEncoder.encode(assetPath, StandardCharsets.UTF_8.name());
		if (xamplifyUtil.isDev()) {
			baseUrl = HOST;
		} else if (xamplifyUtil.isQA()) {
			baseUrl = devHost;
		} else if (xamplifyUtil.isProduction()) {
			baseUrl = productionHost;
		} else {
			baseUrl = HOST;
		}
		String endpoint = "";
		if (assetPath.endsWith(".csv")) {
			endpoint = "/convertCsvToPdf";
		} else if (assetPath.endsWith(".doc") || assetPath.endsWith(".docx")) {
			endpoint = "/convertDocToPdf";
		} else if (assetPath.endsWith("xlsx")) {
			endpoint = "/convert-xlsx-to-pdf";
		} else {
			endpoint = "/proxy";
		}
		String proxyUrl = baseUrl + "xamplify-prm-api" + "/api/pdf" + endpoint + "?pdfUrl=" + encodedUrl;
		return proxyUrl;
	}

	public Set<User> filterDeactivatedUserIds(Integer companyId, Set<User> users) {
		List<Integer> deactivatedUserIds = utilDao.findDeactivedPartnersByCompanyId(companyId);
		if (XamplifyUtils.isNotEmptyList(deactivatedUserIds)) {
			users = users.stream().filter(user -> !deactivatedUserIds.contains(user.getUserId()))
					.collect(Collectors.toSet());
		}
		return users;
	}

	public void updateNonVanityAccessForPartnerCompany(Integer partnerCompanyId, boolean deactivated) {
		String queryString = "update xt_module_access set non_vanity_access_enabled =:deactivated where company_id = :companyId";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("companyId", partnerCompanyId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("deactivated", deactivated));
		hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);
	}

	/*** XNFR-1013 ****/
	public void saveDomainColors(CompanyProfile companyProfile) {
		List<DomainColor> domainColourList = new ArrayList<>();
		DomainColor companyDomainColor = new DomainColor();
		companyDomainColor.setCompany(companyProfile);
		if (XamplifyUtils.isValidString(companyProfile.getWebsite())) {
			setDomainColors(companyProfile, companyDomainColor);
		}

		domainColourList.add(companyDomainColor);
		genericDAO.save(companyDomainColor);
	}

	private void setDomainColors(CompanyProfile companyProfile, DomainColor companyDomainColor) {
		companyDomainColor.setWebsite(companyProfile.getWebsite());
		xamplifyUtil.getColorPalette(companyProfile.getWebsite()).ifPresent(colorMap -> {
			companyDomainColor.setBackgroundColor(extractColors(colorMap.get("background")));
			companyDomainColor.setButtonColor(extractColors(colorMap.get("button-bg")));
			companyDomainColor.setFooterColor(extractColors(colorMap.get("footer-bg")));
			companyDomainColor.setHeaderColor(extractColors(colorMap.get("header-bg")));
			companyDomainColor.setTextColor(extractColors(colorMap.get("text")));
			companyDomainColor.setLogoColor1(extractColors(colorMap.get("logo-color-1")));
			companyDomainColor.setLogoColor2(extractColors(colorMap.get("logo-color-2")));
			companyDomainColor.setLogoColor3(extractColors(colorMap.get("logo-color-3")));
			companyDomainColor.setHeadertextColor(extractColors(colorMap.get("header-text")));
			companyDomainColor.setFootertextColor(extractColors(colorMap.get("footer-text")));
		});
	}

	public void updateDomainColors(CompanyProfile companyProfile, Integer userId) {
		DomainColor companyDomainColor = new DomainColor();
		DomainColorDTO domainColorDTO = new DomainColorDTO();
		setDomainColors(companyProfile, companyDomainColor);
		domainColorDTO.setBackgroundColor(extractColors(companyDomainColor.getBackgroundColor()));
		domainColorDTO.setButtonColor(extractColors(companyDomainColor.getButtonColor()));
		domainColorDTO.setFooterColor(extractColors(companyDomainColor.getFooterColor()));
		domainColorDTO.setHeaderColor(extractColors(companyDomainColor.getHeaderColor()));
		domainColorDTO.setTextColor(extractColors(companyDomainColor.getTextColor()));
		domainColorDTO.setWebsite(extractColors(companyDomainColor.getWebsite()));
		domainColorDTO.setLogoColor1(extractColors(companyDomainColor.getLogoColor1()));
		domainColorDTO.setLogoColor2(extractColors(companyDomainColor.getLogoColor2()));
		domainColorDTO.setLogoColor3(extractColors(companyDomainColor.getLogoColor3()));
		domainColorDTO.setHeadertextColor(extractColors(companyDomainColor.getHeadertextColor()));
		domainColorDTO.setFootertextColor(extractColors(companyDomainColor.getFootertextColor()));
		utilDao.updateDomainColors(userId, domainColorDTO);
	}

	private String extractColors(String color) {
		if (!XamplifyUtils.isValidString(color) || "no data found".equalsIgnoreCase(color)) {
			return null;
		} else {
			return color;
		}
	}

	public void insertIntoDamPartnerGroupUserMapping(Integer partnershipId, Integer partnerId,
			Integer damPartnerGroupId) {
		Integer partnerCompanyId = partnershipDao.getPartnerCompanyIdByPartnershipId(partnershipId);
		List<User> partnerCompanyUsers = XamplifyUtils.isValidInteger(partnerCompanyId)
				? userDao.getAllUsersByCompanyId(partnerCompanyId)
				: Collections.singletonList(createPartnerUser(partnerId));
		for (User companyUser : partnerCompanyUsers) {
			DamPartnerGroupUserMapping partnerGroupMapping = new DamPartnerGroupUserMapping();
			DamPartnerGroupMapping damPartnerGroup = new DamPartnerGroupMapping();
			damPartnerGroup.setId(damPartnerGroupId);
			partnerGroupMapping.setDamPartnerGroupMapping(damPartnerGroup);
			Partnership partnership = new Partnership();
			partnership.setId(partnershipId);
			partnerGroupMapping.setPartnership(partnership);
			partnerGroupMapping.setUser(companyUser);
			if (XamplifyUtils.isNotEmptyList(companyUser.getTeamMembers())) {
				partnerGroupMapping.setTeamMember(companyUser.getTeamMembers().get(0));
			}
			partnerGroupMapping.setCreatedTime(new Date());
			genericDAO.save(partnerGroupMapping);
		}
	}

	private User createPartnerUser(Integer partnerId) {
		User partnerUser = new User();
		partnerUser.setUserId(partnerId);
		return partnerUser;
	}

	public boolean hasFormAccess(Integer userId) {
		if (userId != null) {
			List<String> roles = userDao.listRolesByUserId(userId);
			String status = userDao.getStatusByUserId(userId);
			return (roles.indexOf(Role.FORM_ROLE.getRoleName()) > -1 || hasAnyAdminRole(roles))
					&& UserStatus.APPROVED.getStatus().equals(status);
		} else {
			return false;
		}
	}

}
