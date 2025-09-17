package com.xtremand.mail.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;

import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.Criteria;
import com.xtremand.common.bom.Criteria.OPERATION_NAME;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.common.bom.Pagination;
import com.xtremand.common.bom.Template;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.deal.bom.Deal;
import com.xtremand.drip.email.bom.DripEmailHistory;
import com.xtremand.form.bom.FormDefaultFieldTypeEnum;
import com.xtremand.formbeans.EmailTemplateDTO;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.lead.bom.Lead;
import com.xtremand.lead.dao.LeadDAO;
import com.xtremand.mail.bom.EmailTemplate;
import com.xtremand.mail.exception.MailException;
import com.xtremand.partner.bom.PartnerDataAccessException;
import com.xtremand.partnership.dao.PartnershipDAO;
import com.xtremand.salesforce.bom.SfCustomFieldsData;
import com.xtremand.team.dao.TeamDao;
import com.xtremand.user.bom.User;
import com.xtremand.user.bom.User.UserStatus;
import com.xtremand.user.bom.UserList;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.user.service.UserService;
import com.xtremand.userlist.service.UserListService;
import com.xtremand.util.DripEmailConstants;
import com.xtremand.util.XamplifyUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.dto.XamplifyConstants;
import com.xtremand.util.service.UtilService;
import com.xtremand.vanity.email.templates.bom.CustomDefaultEmailTemplate;
import com.xtremand.vanity.email.templates.bom.DefaultEmailTemplate;
import com.xtremand.vanity.email.templates.bom.DefaultEmailTemplateType;
import com.xtremand.vanity.url.dao.VanityURLDao;
import com.xtremand.vanity.url.dto.VanityURLDTO;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;
import com.xtremand.vanity.url.service.VanityURLService;
import com.xtremand.video.bom.VideoFile;

public abstract class MailService {
	private static final Logger logger = LoggerFactory.getLogger(MailService.class);

	@Autowired
	private GenericDAO genericDao;

	@Autowired
	private UserDAO userDao;

	@Autowired
	private AsyncComponent asyncComponent;

	@Autowired
	private UserService userService;

	@Autowired
	UserListService userListService;

	@Autowired
	private PartnershipDAO partnerShipDao;

	@Autowired
	private UtilDao utilDao;

	@Autowired
	private TeamDao teamDao;

	@Autowired
	private SpringTemplateEngine templateEngine;

	@Autowired
	MailService mailService;

	@Autowired
	private UtilService utilService;

	@Autowired
	private XamplifyUtil xamplifyUtil;

	@Autowired
	private LeadDAO leadDAO;

	@Value("${email}")
	String email;

	@Value("${appUrl}")
	String appUrl;

	@Value("${sendMail}")
	protected String sendMail;

	@Value("${email}")
	String fromEmail;

	@Value("${mail.sender}")
	String fromName;

	@Value("${server_path}")
	String server_path;

	@Value("${mobileAppUrl}")
	String mobileAppUrl;

	@Value("${web_url}")
	String webUrl;

	@Value("${mail.sender}")
	private String mailSender;

	@Value("${rsvp.yes}")
	String rsvpYes;

	@Value("${rsvp.no}")
	String rsvpNo;

	@Value("${rsvp.maybe}")
	String rsvpMaybe;

	@Value("${dev.campaign.url}")
	String devCampaignUrl;

	@Value("${prod.campaign.url}")
	String prodCampaignUrl;

	@Value("${release.campaign.url}")
	String releaseCampaignUrl;

	@Value("${dev.video.gif.tag}")
	String devVideoGifTag;

	@Value("${prod.video.gif.tag}")
	String prodVideoGifTag;

	@Value("${release.video.gif.tag}")
	String releaseVideoGifTag;

	@Value("${campaign.tag}")
	String campaignTag;

	@Value("${dev.cobranding.image}")
	String devCoBrandingImage;

	@Value("${prod.cobranding.image}")
	String prodCoBrandingImage;

	@Value("${release.cobranding.image}")
	String releaseCoBrandingImage;

	@Value("${company.logo.url}")
	String companyLogoUrl;
	
	@Value("${co.branding.logo}")
	String coBrandingLogo;

	@Value("${xamplify.logo}")
	String xAmplifyLogo;

	@Value("${support.email.id}")
	String supportEmailId;

	@Value("${event.utm.public}")
	String eventUtmCode;

	@Value("${socialCampaignMessageToPartner}")
	String socialCampaignMessageToPartner;

	@Value("${socialCampaignMessageToContact}")
	String socialCampaignMessageToContact;

	@Value("${replace.company.logo}")
	private String replaceCompanyLogo;

	@Value("${replace.there}")
	private String replaceThere;

	@Value("${logMessage.seperator}")
	private String logMessageSeperator;

	@Value("${show_shorten_url_log}")
	private boolean showShortenUrlLog;

	@Autowired
	private VanityURLService vanityURLService;

	@Autowired
	private VanityURLDao vanityURLDao;

	private static final String CUSTOMER_FULL_NAME = "_CUSTOMER_FULL_NAME";

	private static final String LOGIN = "/login";

	public abstract void sendMail(EmailBuilder builder) throws MailException;

	public static final class EmailBuilder implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -2876045673940647021L;

		private String from;
		private String senderName;
		private String to;
		private String subject;
		private String body;
		private String sendGridApiUser;
		private String sendGridApiKey;
		private Integer campaignId;
		private Integer replyId;
		private Integer urlId;
		private Integer userId;
		private boolean campaignCreation;
		private Map<String, ?> model;
		private boolean sendingEmailToLead;
		private String publicEventAlias;
		private boolean sendEmail;
		private boolean campaignReplyAutoResponseExists;
		private Integer autoResponseEmailsSentCount;
		private boolean campaignUrlsAutoResponseExits;
		private Integer clickedUrlsEmailsSentCount;
		private Integer statusCode;
		private boolean teamMemberEmail;
		private boolean invitationSentFromTeamMemberSection;
		private Integer teamMemberId;
		/** XNFR-735 **/
		private List<String> ccEmailIds;
		private List<String> bccEmailIds;
		private List<MultipartFile> attachments;
		// XNFR-993
		private boolean playbookWorkflowExists;
		private Integer learningTrackId;
		private Integer workflowId;

		public EmailBuilder() {

		}

		public EmailBuilder teamMemberEmail(boolean teamMemberEmail) {
			this.teamMemberEmail = teamMemberEmail;
			return this;
		}

		public EmailBuilder invitationSentFromTeamMemberSection(boolean invitationSentFromTeamMemberSection) {
			this.invitationSentFromTeamMemberSection = invitationSentFromTeamMemberSection;
			return this;
		}

		public EmailBuilder teamMemberId(Integer teamMemberId) {
			this.teamMemberId = teamMemberId;
			return this;
		}

		public EmailBuilder campaignId(Integer campaignId) {
			this.campaignId = campaignId;
			return this;
		}

		public EmailBuilder replyId(Integer replyId) {
			this.replyId = replyId;
			return this;
		}

		public EmailBuilder urlId(Integer urlId) {
			this.urlId = urlId;
			return this;
		}

		public EmailBuilder userId(Integer userId) {
			this.userId = userId;
			return this;
		}

		public EmailBuilder campaignCreation(boolean campaignCreation) {
			this.campaignCreation = campaignCreation;
			return this;
		}

		public EmailBuilder sendingEmailToLead(boolean sendingEmailToLead) {
			this.sendingEmailToLead = sendingEmailToLead;
			return this;
		}

		public EmailBuilder sendEmail(boolean sendEmail) {
			this.sendEmail = sendEmail;
			return this;
		}

		public EmailBuilder publicEventAlias(String publicEventAlias) {
			this.publicEventAlias = publicEventAlias;
			return this;
		}

		public EmailBuilder from(String from) {
			this.from = from;
			return this;
		}

		public EmailBuilder senderName(String senderName) {
			this.senderName = senderName;
			return this;
		}

		public EmailBuilder to(String to) {
			this.to = to;
			return this;
		}

		public EmailBuilder subject(String subject) {
			this.subject = subject;
			return this;
		}

		public EmailBuilder body(String body) {
			this.body = body;
			return this;
		}

		public EmailBuilder model(Map<String, ?> model) {
			this.model = model;
			return this;
		}

		public EmailBuilder playbookWorkflowExists(boolean playbookWorkflowExists) {
			this.playbookWorkflowExists = playbookWorkflowExists;
			return this;
		}

		public EmailBuilder learningTrackId(Integer learningTrackId) {
			this.learningTrackId = learningTrackId;
			return this;
		}

		public EmailBuilder workflowId(Integer workflowkId) {
			this.workflowId = workflowkId;
			return this;
		}

		public EmailBuilder build() {
			if (this.from == null)
				throw new MailException("From can not be null");
			if (this.to == null)
				throw new MailException("To can not be null");
			if (this.subject == null)
				throw new MailException("Subject can not be null");
			if (this.body == null)
				throw new MailException("body can not be null");
			return this;
		}

		public String getFrom() {
			return from;
		}

		public String getSenderName() {
			return senderName;
		}

		public String getTo() {
			return to;
		}

		public String getSubject() {
			return subject;
		}

		public String getBody() {
			return body;
		}

		public String getSendGridApiUser() {
			return sendGridApiUser;
		}

		public String getSendGridApiKey() {
			return sendGridApiKey;
		}

		public Integer getCampaignId() {
			return campaignId;
		}

		public Integer getReplyId() {
			return replyId;
		}

		public Integer getUrlId() {
			return urlId;
		}

		public Integer getUserId() {
			return userId;
		}

		public boolean isCampaignCreation() {
			return campaignCreation;
		}

		public boolean isSendingEmailToLead() {
			return sendingEmailToLead;
		}

		public String getPublicEventAlias() {
			return publicEventAlias;
		}

		public Map<String, ?> getModel() {
			return model;
		}

		public void setCampaignCreation(boolean campaignCreation) {
			this.campaignCreation = campaignCreation;
		}

		public boolean isSendEmail() {
			return sendEmail;
		}

		public boolean isCampaignReplyAutoResponseExists() {
			return campaignReplyAutoResponseExists;
		}

		public void setCampaignReplyAutoResponseExists(boolean campaignReplyAutoResponseExists) {
			this.campaignReplyAutoResponseExists = campaignReplyAutoResponseExists;
		}

		public Integer getAutoResponseEmailsSentCount() {
			return autoResponseEmailsSentCount;
		}

		public void setAutoResponseEmailsSentCount(Integer autoResponseEmailsSentCount) {
			this.autoResponseEmailsSentCount = autoResponseEmailsSentCount;
		}

		public boolean isCampaignUrlsAutoResponseExits() {
			return campaignUrlsAutoResponseExits;
		}

		public void setCampaignUrlsAutoResponseExits(boolean campaignUrlsAutoResponseExits) {
			this.campaignUrlsAutoResponseExits = campaignUrlsAutoResponseExits;
		}

		public Integer getClickedUrlsEmailsSentCount() {
			return clickedUrlsEmailsSentCount;
		}

		public void setClickedUrlsEmailsSentCount(Integer clickedUrlsEmailsSentCount) {
			this.clickedUrlsEmailsSentCount = clickedUrlsEmailsSentCount;
		}

		public Integer getStatusCode() {
			return statusCode;
		}

		public void setStatusCode(Integer statusCode) {
			this.statusCode = statusCode;
		}

		public boolean isTeamMemberEmail() {
			return teamMemberEmail;
		}

		public boolean isPlaybookWorkflowExists() {
			return playbookWorkflowExists;
		}

		public void setPlaybookWorkflowExists(boolean playbookWorkflowExists) {
			this.playbookWorkflowExists = playbookWorkflowExists;
		}

		public Integer getLearningTrackId() {
			return learningTrackId;
		}

		public void setLearningTrackId(Integer learningTrackId) {
			this.learningTrackId = learningTrackId;
		}

		public Integer getWorkflowId() {
			return workflowId;
		}

		public void setWorkflowId(Integer workflowId) {
			this.workflowId = workflowId;
		}

		@Override
		public String toString() {
			return "EmailBuilder [from=" + from + ", senderName=" + senderName + ", to=" + to + ", subject=" + subject
					+ ", sendGridApiUser=" + sendGridApiUser + ", sendGridApiKey=" + sendGridApiKey + ", campaignId="
					+ campaignId + ", replyId=" + replyId + ", urlId=" + urlId + ", userId=" + userId
					+ ", campaignCreation=" + campaignCreation + ", model=" + model + ", sendingEmailToLead="
					+ sendingEmailToLead + ", publicEventAlias=" + publicEventAlias + ", sendEmail=" + sendEmail
					+ ", campaignReplyAutoResponseExists=" + campaignReplyAutoResponseExists
					+ ", autoResponseEmailsSentCount=" + autoResponseEmailsSentCount
					+ ", campaignUrlsAutoResponseExits=" + campaignUrlsAutoResponseExits
					+ ", clickedUrlsEmailsSentCount=" + clickedUrlsEmailsSentCount + ", statusCode=" + statusCode
					+ ", teamMemberEmail=" + teamMemberEmail + ",invitationSentFromTeamMemberSection="
					+ invitationSentFromTeamMemberSection + "]";
		}

		public boolean isInvitationSentFromTeamMemberSection() {
			return invitationSentFromTeamMemberSection;
		}

		public void setInvitationSentFromTeamMemberSection(boolean invitationSentFromTeamMemberSection) {
			this.invitationSentFromTeamMemberSection = invitationSentFromTeamMemberSection;
		}

		public Integer getTeamMemberId() {
			return teamMemberId;
		}

		public void setTeamMemberId(Integer teamMemberId) {
			this.teamMemberId = teamMemberId;
		}

		/** XNFR-735 **/
		public EmailBuilder ccEmailIds(List<String> ccEmailIds) {
			this.ccEmailIds = ccEmailIds;
			return this;
		}

		public EmailBuilder bccEmailIds(List<String> bccEmailIds) {
			this.bccEmailIds = bccEmailIds;
			return this;
		}

		public List<String> getCCEmailIds() {
			return ccEmailIds;
		}

		public List<String> getBCCEmailIds() {
			return bccEmailIds;
		}

		public EmailBuilder attachments(List<MultipartFile> attachments) {
			this.attachments = attachments;
			return this;
		}

		public List<MultipartFile> getAttachments() {
			return attachments;
		}

	}

	public void sendMail(User user, int templateId) throws MailException {
		EmailTemplate template = genericDao.get(EmailTemplate.class, templateId);
		mailService.sendMail(new EmailBuilder().from(email).senderName(mailSender).to(user.getEmailId())
				.subject(template.getSubject()).body(getUpdateBody(user, template)).build());
	}

	public void sendActivationMail(User user, int templateId) throws MailException {
		String body = null;
		String subject = null;
		String emailId = null;
		String senderFullName = null;
		boolean isEmailDnsConfigured = false;
		if (StringUtils.hasText(user.getCompanyProfileName())) {
			CompanyProfile cp = vanityURLDao.getCompanyProfileByCompanyProfileName(user.getCompanyProfileName());
			DefaultEmailTemplate vanityDefaultEmailTemplate = vanityURLDao
					.getVanityDefaultEmailTemplateByType(DefaultEmailTemplateType.ACCOUNT_ACTIVATION);
			CustomDefaultEmailTemplate vanityEmailTemplate = vanityURLDao
					.getVanityETByDefVanityETIdAndCompanyId(vanityDefaultEmailTemplate.getId(), cp);
			if (vanityEmailTemplate != null) {
				subject = vanityEmailTemplate.getSubject();
				body = genericDao.get(CustomDefaultEmailTemplate.class, vanityEmailTemplate.getId()).getHtmlBody();
			} else {
				subject = vanityDefaultEmailTemplate.getSubject();
				body = genericDao.get(DefaultEmailTemplate.class, vanityDefaultEmailTemplate.getId()).getHtmlBody();
			}
			body = XamplifyUtils.replaceReceiverMergeTagsInfo(user, body);
		} else {
			EmailTemplate template = genericDao.get(EmailTemplate.class, templateId);
			body = template.getBody();
			subject = template.getSubject();
		}

		body = body.replaceAll("<tmpl_var company>", "xAmplify");
		body = body.replaceAll("<appUrl>", appUrl);

		body = XamplifyUtils.replaceCustomerFullName(user, body);
		body = body.replaceAll("<tmpl_var username>",
				user.getUserName() != null && user.getUserName().length() > 0
						? XamplifyUtils.escapeDollarSequece(user.getUserName())
						: replaceThere);

		if (StringUtils.hasText(user.getCompanyProfileName())) {
			String vanityURLDomain = xamplifyUtil.frameVanityURL(webUrl, user.getCompanyProfileName());
			VanityURLDTO vanityURLDto = vanityURLService.getCompanyDetails(user.getCompanyProfileName());
			Integer adminId = utilDao.findAdminIdByCompanyId(vanityURLDto.getCompanyId());
			User sender = userService.loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, adminId)),
					new FindLevel[] { FindLevel.COMPANY_PROFILE });
			emailId = sender.getEmailId();
			senderFullName = getFullName(sender.getFirstName(), sender.getLastName());
			isEmailDnsConfigured = userDao.isEmailDnsConfigured(emailId);
			body = body.replaceAll("<VerifyEmailLink>",
					vanityURLDomain + "register/verifyemail/user?alias=" + user.getAlias());
			body = body.replaceAll("<Vanity_Company_Logo>",
					server_path + XamplifyUtils.escapeDollarSequece(vanityURLDto.getCompanyLogoImagePath()));
			body = body.replaceAll("<Vanity_Company_Logo_Href>", vanityURLDto.getCompanyUrl());
			body = body.replaceAll(replaceCompanyLogo,
					server_path + XamplifyUtils.escapeDollarSequece(vanityURLDto.getCompanyLogoImagePath()));
			body = body.replaceAll("<<PARTNER_NAME>>", user.getFirstName() + " "
					+ (XamplifyUtils.isValidString(user.getLastName()) ? user.getLastName() : ""));
			body = body.replaceAll("<<senderFullName>>", sender.getFirstName() + " "
					+ (XamplifyUtils.isValidString(sender.getLastName()) ? sender.getLastName() : ""));
			body = body.replaceAll("<<VENDOR_COMPANY_NAME>>",
					XamplifyUtils.escapeDollarSequece(sender.getCompanyProfile().getCompanyName()));

		} else {
			body = body.replaceAll("<Vanity_Company_Logo>", xAmplifyLogo);
			body = body.replaceAll("<Vanity_Company_Logo_Href>", xamplifyUtil.getLoginUrl());
			body = body.replaceAll("<VerifyEmailLink>", webUrl + "register/verifyemail/user?alias=" + user.getAlias());
		}
		if (isEmailDnsConfigured) {
			mailService.sendMail(new EmailBuilder().from(emailId).senderName(senderFullName).to(user.getEmailId())
					.subject(subject).body(body).build());
		} else {
			subject = utilService.addPerfixToSubject(subject);
			mailService.sendMail(new EmailBuilder().from(email).senderName(mailSender).to(user.getEmailId())
					.subject(subject).body(body).build());
		}
	}

	public void sendMails(User user, List<String> emails, int templateId) throws MailException {
		EmailTemplate template = genericDao.get(EmailTemplate.class, templateId);
		for (String userEmail : emails)
			mailService.sendMail(new EmailBuilder().from(email).senderName(null).to(userEmail)
					.subject(template.getSubject()).body(getUpdateBody(user, template)).build());
	}

	public void sendForgotPasswordMail(User user, int templateId, String password) throws MailException {
		String body = null;
		String subject = null;
		String email = null;
		String senderFullName = null;
		boolean isEmailDnsConfigured = false;
		String companyProfileName = vanityCheckingForgotPassword(user);
		if (StringUtils.hasText(user.getCompanyProfileName())
				&& !"versa-networks".equalsIgnoreCase(user.getCompanyProfileName())) {
			CompanyProfile cp = vanityURLDao.getCompanyProfileByCompanyProfileName(companyProfileName);
			DefaultEmailTemplate vanityDefaultEmailTemplate = vanityURLDao
					.getVanityDefaultEmailTemplateByType(DefaultEmailTemplateType.FORGOT_PASSWORD);
			if (cp != null && XamplifyUtils.isValidInteger(cp.getId())) {
				CustomDefaultEmailTemplate vanityEmailTemplate = vanityURLDao
						.getVanityETByDefVanityETIdAndCompanyId(vanityDefaultEmailTemplate.getId(), cp);
				if (vanityEmailTemplate != null) {
					subject = vanityEmailTemplate.getSubject();
					body = genericDao.get(CustomDefaultEmailTemplate.class, vanityEmailTemplate.getId()).getHtmlBody();
				} else {
					subject = vanityDefaultEmailTemplate.getSubject();
					body = genericDao.get(DefaultEmailTemplate.class, vanityDefaultEmailTemplate.getId()).getHtmlBody();
				}
			} else {
				subject = vanityDefaultEmailTemplate.getSubject();
				body = genericDao.get(DefaultEmailTemplate.class, vanityDefaultEmailTemplate.getId()).getHtmlBody();
			}
			body = XamplifyUtils.replaceReceiverMergeTagsInfo(user, body);
		} else {
			EmailTemplate template = genericDao.get(EmailTemplate.class, templateId);
			subject = template.getSubject();
			body = genericDao.get(EmailTemplate.class, templateId).getBody();
		}

		body = body.replaceAll("<tmpl_var company>", "xAmplify");
		body = body.replaceAll("<appUrl>", appUrl);

		body = body.replaceAll(CUSTOMER_FULL_NAME,
				user.getFirstName() != null && user.getFirstName().length() > 0
						? XamplifyUtils.escapeDollarSequece(user.getFirstName())
						: replaceThere);
		body = body.replaceAll("_TEMPORARY_PASSWORD", XamplifyUtils.escapeDollarSequece(password));
		if (StringUtils.hasText(user.getCompanyProfileName())) {
			String vanityURLDomain = xamplifyUtil.frameVanityURL(webUrl, user.getCompanyProfileName());
			VanityURLDTO vanityURLDto = vanityURLService.getCompanyDetails(user.getCompanyProfileName());
			Integer adminId = utilDao.findAdminIdByCompanyId(vanityURLDto.getCompanyId());
			User sender = userService.loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, adminId)),
					new FindLevel[] { FindLevel.COMPANY_PROFILE });
			email = sender.getEmailId();
			senderFullName = getFullName(sender.getFirstName(), sender.getLastName());
			isEmailDnsConfigured = userDao.isEmailDnsConfigured(email);
			body = body.replaceAll("<Vanity_Company_Logo>",
					server_path + XamplifyUtils.escapeDollarSequece(vanityURLDto.getCompanyLogoImagePath()));
			body = body.replaceAll("<Vanity_Company_Logo_Href>", vanityURLDto.getCompanyUrl());
			body = body.replaceAll("<login_url>", vanityURLDomain);
			body = body.replaceAll(replaceCompanyLogo,
					server_path + XamplifyUtils.escapeDollarSequece(vanityURLDto.getCompanyLogoImagePath()));
		} else {
			body = body.replaceAll("<Vanity_Company_Logo>", xAmplifyLogo);
			body = body.replaceAll("<Vanity_Company_Logo_Href>", xamplifyUtil.getLoginUrl());
		}
		CompanyProfile companyProfile = user.getCompanyProfile();
		if (companyProfile != null && StringUtils.hasText(companyProfile.getWebsite())) {
			body = body.replaceAll("<Vanity_Company_Logo_Href>", companyProfile.getWebsite());
		}
		if (isEmailDnsConfigured) {
			mailService.sendMail(new EmailBuilder().from(email).senderName(senderFullName).to(user.getEmailId())
					.subject(subject).body(body).build());
		} else {
			subject = utilService.addPerfixToSubject(subject);
			mailService.sendMail(new EmailBuilder().from(this.email).senderName(mailSender).to(user.getEmailId())
					.subject(subject).body(body).build());
		}
	}

	private String vanityCheckingForgotPassword(User user) {
		VanityUrlDetailsDTO vanityUrlDetailsDTO = new VanityUrlDetailsDTO();
		String companyProfileName;
		vanityUrlDetailsDTO.setUserId(user.getUserId());
		vanityUrlDetailsDTO.setVendorCompanyProfileName(user.getCompanyProfileName());
		vanityUrlDetailsDTO.setVanityUrlFilter(true);
		utilService.isVanityUrlFilterApplicable(vanityUrlDetailsDTO);
		if (vanityUrlDetailsDTO.isVendorLoggedInThroughOwnVanityUrl()) {
			companyProfileName = user.getCompanyProfileName();
		} else {
			companyProfileName = userDao.getCompanyProfileNameByUserId(user.getUserId());

		}
		return companyProfileName;
	}

	private String getUpdateBody(User user, Template template) {
		String body = template.getBody();
		body = body.replaceAll("<tmpl_var company>", "xAmplify");
		body = body.replaceAll("<appUrl>", appUrl);

		switch (template.getId()) {
		case EmailConstants.NEW_USER_SIGNUP:
			body = XamplifyUtils.replaceCustomerFullName(user, body);
			body = body.replaceAll("<tmpl_var username>",
					user.getUserName() != null && user.getUserName().length() > 0
							? XamplifyUtils.escapeDollarSequece(user.getUserName())
							: replaceThere);

			if (StringUtils.hasText(user.getCompanyProfileName())) {
				String vanityURLDomain = xamplifyUtil.frameVanityURL(webUrl, user.getCompanyProfileName());
				body = body.replaceAll("<VerifyEmailLink>",
						vanityURLDomain + "register/verifyemail/user?alias=" + user.getAlias());
				VanityURLDTO vanityURLDto = vanityURLService.getCompanyDetails(user.getCompanyProfileName());
				body = body.replaceAll("<Vanity_Company_Logo>",
						server_path + XamplifyUtils.escapeDollarSequece(vanityURLDto.getCompanyLogoImagePath()));
				body = body.replaceAll("<Vanity_Company_Logo_Href>", vanityURLDomain);
			} else {
				body = body.replaceAll("<Vanity_Company_Logo>", xAmplifyLogo);
				body = body.replaceAll("<Vanity_Company_Logo_Href>", xamplifyUtil.getLoginUrl());
				body = body.replaceAll("<VerifyEmailLink>",
						webUrl + "register/verifyemail/user?alias=" + user.getAlias());
			}
			break;
		default:
			break;
		}
		return body;

	}

	public void sendCustomerJoinedMail(User user, String superAdminEmail) throws MailException {
		EmailTemplate template = genericDao.get(EmailTemplate.class, EmailConstants.USERJOINED);
		String body = getUpdateBody(user, template);

		mailService.sendMail(new EmailBuilder().from(this.email).senderName(null).to(superAdminEmail)
				.subject(template.getSubject()).body(body).build());
	}

	public void sendPartnerReminderEmail(UserDTO userDto, String subject, String body, User vendor) {
		try {
			CompanyProfile companyProfile = vendor.getCompanyProfile();
			boolean hasVanityAccess = utilDao.hasVanityAccessByUserId(userDto.getUserId());
			body = body.replaceAll("VENDOR_COMPANY_NAME",
					XamplifyUtils.escapeDollarSequece(companyProfile.getCompanyName()));
			body = body.replaceAll("VENDOR_FULL_NAME",
					(vendor.getFirstName() != null ? XamplifyUtils.escapeDollarSequece(vendor.getFirstName()) : "")
							+ " "
							+ (vendor.getLastName() != null ? XamplifyUtils.escapeDollarSequece(vendor.getLastName())
									: ""));
			body = body.replaceAll("VENDOR_COMPANY_LOGO", xAmplifyLogo);
			body = replaceCustomerFullName(userDto, body);
			body = body.replaceAll("VENDOR_COMPANY_LOGO", xAmplifyLogo);

			if (hasVanityAccess) {
				body = body.replaceAll("VENDOR_COMPANY_NAME",
						XamplifyUtils.escapeDollarSequece(companyProfile.getCompanyName()));
				body = body.replaceAll("VENDOR_FULL_NAME",
						(vendor.getFirstName() != null ? XamplifyUtils.escapeDollarSequece(vendor.getFirstName()) : "")
								+ " "
								+ (vendor.getLastName() != null
										? XamplifyUtils.escapeDollarSequece(vendor.getLastName())
										: ""));
				body = body.replaceAll("VENDOR_COMPANY_LOGO",
						server_path + XamplifyUtils.escapeDollarSequece(companyProfile.getCompanyLogoPath()));
				body = body.replaceAll("<Vanity_Company_Logo>", server_path
						+ XamplifyUtils.escapeDollarSequece(vendor.getCompanyProfile().getCompanyLogoPath()));
				body = body.replaceAll("<Vanity_Company_Logo_Href>",
						XamplifyUtils.escapeDollarSequece(vendor.getCompanyProfile().getWebsite()));
				body = body.replaceAll(replaceCompanyLogo,
						server_path + XamplifyUtils.escapeDollarSequece(companyProfile.getCompanyLogoPath()));
				body = replaceCustomerFullName(userDto, body);
			} else {
				body = body.replaceAll("VENDOR_COMPANY_LOGO", xAmplifyLogo);
				body = body.replaceAll("<Vanity_Company_Logo_Href>", xamplifyUtil.getLoginUrl());
			}
			sendMail(new EmailBuilder().from(vendor.getEmailId())
					.senderName((vendor.getFirstName() != null)
							? XamplifyUtils.escapeDollarSequece(vendor.getFirstName())
							: (vendor.getLastName() != null ? XamplifyUtils.escapeDollarSequece(vendor.getLastName())
									: XamplifyUtils.escapeDollarSequece(companyProfile.getCompanyName())))
					.to(userDto.getEmailId()).subject(subject).body(body).build());

		} catch (MailException | HibernateException | PartnerDataAccessException e) {
			logger.error("sendPartnerReminderEmail()", e);
			throw new PartnerDataAccessException(e.getMessage());
		}
	}

	public void sendTeamMemberEmails(List<User> teamMembers, User orgAdmin, boolean resendingInvitation) {
		try {
			String body = null;
			boolean hasVanityAccess = utilDao.hasVanityAccessByUserId(orgAdmin.getUserId());
			if (StringUtils.hasText(orgAdmin.getCompanyProfileName()) || hasVanityAccess) {
				CompanyProfile cp = orgAdmin.getCompanyProfile();
				DefaultEmailTemplateType templateType = DefaultEmailTemplateType.JOIN_MY_TEAM;
				DefaultEmailTemplate vanityDefaultEmailTemplate = vanityURLDao
						.getVanityDefaultEmailTemplateByType(templateType);
				CustomDefaultEmailTemplate vanityEmailTemplate = vanityURLDao
						.getVanityETByDefVanityETIdAndCompanyId(vanityDefaultEmailTemplate.getId(), cp);
				if (vanityEmailTemplate != null) {
					orgAdmin.setSubjectLine(vanityEmailTemplate.getSubject());
					body = genericDao.get(CustomDefaultEmailTemplate.class, vanityEmailTemplate.getId()).getHtmlBody();
				} else {
					orgAdmin.setSubjectLine(vanityDefaultEmailTemplate.getSubject());
					body = genericDao.get(DefaultEmailTemplate.class, vanityDefaultEmailTemplate.getId()).getHtmlBody();
				}
			} else {
				DefaultEmailTemplate vanityDefaultEmailTemplate = vanityURLDao
						.getVanityDefaultEmailTemplateByType(DefaultEmailTemplateType.JOIN_MY_TEAM);
				orgAdmin.setSubjectLine(vanityDefaultEmailTemplate.getSubject());
				body = genericDao.get(DefaultEmailTemplate.class, vanityDefaultEmailTemplate.getId()).getHtmlBody();
			}
			body = body.replace("<<orgAdminEmailId>>", XamplifyUtils.escapeDollarSequece(orgAdmin.getEmailId()));
			if (StringUtils.hasText(orgAdmin.getFirstName())) {
				body = body.replace("<Org_Admin_First_Name>",
						XamplifyUtils.replaceNullWithEmptyString(orgAdmin.getFirstName()));
			} else {
				body = body.replace("<Org_Admin_First_Name>", "");
			}
			if (StringUtils.hasText(orgAdmin.getLastName())) {
				body = body.replace("<Org_Admin_Last_Name>",
						XamplifyUtils.replaceNullWithEmptyString(orgAdmin.getLastName()));
			} else {
				body = body.replace("<Org_Admin_Last_Name>", "");
			}
			body = body.replace("<Org_Admin_Title>", XamplifyUtils.replaceNullWithEmptyString(orgAdmin.getJobTitle()));
			body = body.replace("<Org_Admin_Company>",
					XamplifyUtils.replaceNullWithEmptyString(orgAdmin.getCompanyProfile().getCompanyName()));
			asyncComponent.sendTeamMemberEmailsAsync(teamMembers, orgAdmin, body, resendingInvitation);
		} catch (Exception e) {
			logger.error("sendTeamMemberEmails()", e);
		}
	}

	public void sendVideoUploadedMail(User user, int templateId, String videoTitle) throws MailException {
		EmailTemplate template = genericDao.get(EmailTemplate.class, templateId);
		String body = template.getBody();
		String subject = utilService.addPerfixToSubject(template.getSubject());
		body = XamplifyUtils.replaceCustomerFullName(user, body);
		body = body.replaceAll("<video_title>",
				videoTitle != null && videoTitle.length() > 0 ? XamplifyUtils.escapeDollarSequece(videoTitle) : "");
		mailService.sendMail(new EmailBuilder().from(email).senderName(mailSender).to(user.getEmailId())
				.subject(subject).body(body).build());
	}

	public void sendProcessCompletedMail(User user, int templateId, VideoFile video) throws MailException {
		EmailTemplate template = genericDao.get(EmailTemplate.class, templateId);
		String body = template.getBody();
		String subject = utilService.addPerfixToSubject(template.getSubject());
		body = XamplifyUtils.replaceCustomerFullName(user, body);
		body = body.replaceAll("<video_title>",
				video.getTitle() != null && video.getTitle().length() > 0
						? XamplifyUtils.escapeDollarSequece(video.getTitle())
						: "");
		mailService.sendMail(new EmailBuilder().from(email).senderName(mailSender).to(user.getEmailId())
				.subject(subject).body(body).build());
	}

	public void sendDraftVideoMail(User user, int templateId, VideoFile video) throws MailException {
		EmailTemplate template = genericDao.get(EmailTemplate.class, templateId);
		String body = template.getBody();
		body = XamplifyUtils.replaceCustomerFullName(user, body);
		body = body.replaceAll("<video_title>",
				video.getTitle() != null && video.getTitle().length() > 0
						? XamplifyUtils.escapeDollarSequece(video.getTitle())
						: "");

		mailService.sendMail(new EmailBuilder().from(email).senderName(mailSender).to(user.getEmailId())
				.subject(template.getSubject()).body(body).build());
	}

	public void sendPartnerlistMail(User user, int templateId, UserList userList) throws MailException {

		UserList defaultPartnerList = null;
		if (!userList.isDefaultPartnerList()) {
			defaultPartnerList = userListService.getDefaultPartnerList(user);
		}

		EmailTemplate template = genericDao.get(EmailTemplate.class, templateId);
		String body = template.getBody();
		body = XamplifyUtils.replaceCustomerFullName(user, body);
		body = body.replaceAll("<partner_list_name>", XamplifyUtils.escapeDollarSequece(userList.getName()));
		List<User> exUsers = new ArrayList<User>(userList.getUsers());
		Integer companyId = userList.getCompany().getId();
		List<User> subscribedUsers = ((Map<String, List<User>>) userListService
				.getSubscribedAndUnsubscribedUsers(exUsers, companyId)).get("subscribedUsers");
		long validUsers = subscribedUsers.stream().filter(u -> u.isEmailValidationInd() == true
				&& (u.getUserStatus().equals(UserStatus.APPROVED) || u.getUserStatus().equals(UserStatus.UNAPPROVED))
				&& u.isEmailValid() == true).count();
		long inValidUsers = subscribedUsers.stream()
				.filter(u -> u.isEmailValidationInd() == true && u.isEmailValid() == false).count();
		body = body.replaceAll("<valid_contacts_count>", String.valueOf(validUsers));
		body = body.replaceAll("<Invalid_contacts_count>", String.valueOf(inValidUsers));

		body = body.replaceAll("<contactlist_title>",
				!userList.isDefaultPartnerList() ? XamplifyUtils.escapeDollarSequece(userList.getName()) : "");
		body = body.replaceAll("<default_partnerlist_title>",
				userList.isDefaultPartnerList() ? XamplifyUtils.escapeDollarSequece(userList.getName())
						: XamplifyUtils.escapeDollarSequece(defaultPartnerList.getName()));

		if (StringUtils.hasText(user.getCompanyProfileName())) {
			String vanityURLDomain = xamplifyUtil.frameVanityURL(webUrl,
					user.getCompanyProfile().getCompanyProfileName());
			VanityURLDTO vanityURLDto = vanityURLService.getCompanyDetails(user.getCompanyProfileName());
			body = body.replaceAll("<Vanity_Company_Logo>",
					server_path + XamplifyUtils.escapeDollarSequece(vanityURLDto.getCompanyLogoImagePath()));
			body = body.replaceAll("<Vanity_Company_Logo_Href>", vanityURLDomain);
		} else {
			body = body.replaceAll("<Vanity_Company_Logo>", xAmplifyLogo);
			body = body.replaceAll("<Vanity_Company_Logo_Href>", xamplifyUtil.getLoginUrl());
		}
		String partnerModuleCustomName = utilService.findPartnerModuleCustomName(user.getUserId());
		// String subject = systemNotificationPrefixMessage + "New " +
		// partnerModuleCustomName + " added to the list";
		String subject = utilService.addPerfixToSubject("New " + partnerModuleCustomName + " added to the group");
		body = body.replaceAll("<<PARTNER_MODULE_CUSTOM_NAME>>", partnerModuleCustomName);
		mailService.sendMail(new EmailBuilder().from(email).senderName(mailSender).to(user.getEmailId())
				.subject(subject).body(body).build());
	}

	public void sendContactListMail(User user, Integer templateId, UserList userList,
			List<Integer> totalUnsubscribedUserIds) throws MailException {
		EmailTemplate template = genericDao.get(EmailTemplate.class, templateId);
		String body = template.getBody();
		String subject = utilService.addPerfixToSubject(template.getSubject());
		body = XamplifyUtils.replaceCustomerFullName(user, body);
		body = body.replaceAll("<contactlist_title>", XamplifyUtils.escapeDollarSequece(userList.getName()));
		long validUsers = 0L;
		long inValidUsers = 0L;

		if (userList.isEmailValidationInd()) {
			List<User> exUsers = new ArrayList<>(userList.getUsers());
			if (totalUnsubscribedUserIds != null && exUsers != null) {
				List<User> unsubscribedUsers = exUsers.stream()
						.filter(exuser -> totalUnsubscribedUserIds.contains(exuser.getUserId()))
						.collect(Collectors.toList());
				List<Integer> unsubscribedUserIds = unsubscribedUsers.stream().map(User::getUserId)
						.collect(Collectors.toList());
				exUsers.removeIf(exUser -> unsubscribedUserIds.contains(exUser.getUserId()));
			}
			if (exUsers != null) {
				List<User> subscribedUsers = exUsers.stream().distinct().collect(Collectors.toList());
				if (subscribedUsers != null) {
					for (User userObj : subscribedUsers) {
						if (userObj != null && userObj.isEmailValidationInd() && userObj.isEmailValid()
								&& userObj.getUserStatus() != null
								&& (userObj.getUserStatus().equals(UserStatus.APPROVED)
										|| userObj.getUserStatus().equals(UserStatus.UNAPPROVED))) {
							validUsers = validUsers + 1;
						}
						if (userObj != null && userObj.isEmailValidationInd() && !userObj.isEmailValid()) {
							inValidUsers = inValidUsers + 1;
						}
					}
				}
			}
			body = body.replaceAll("<valid_contacts_count>", String.valueOf(validUsers));
			body = body.replaceAll("<Invalid_contacts_count>", String.valueOf(inValidUsers));
		}

		mailService.sendMail(new EmailBuilder().from(email).senderName(mailSender).to(user.getEmailId())
				.subject(subject).body(body).build());

	}

	public void sendLeadsListMail(User user, Integer templateId, UserList userList,
			List<Integer> totalUnsubscribedUserIds) throws MailException {
		Context context = new Context();
		Map<String, Object> model = new HashMap<>();
		String htmlName = "";
		String subject = "";
		String partnerModuleCustomName = "";
		if (user.getCompanyProfile() != null) {
			partnerModuleCustomName = utilService
					.findPartnerModuleCustomNameByCompanyId(user.getCompanyProfile().getId());
		} else {
			partnerModuleCustomName = "";
		}
		if (EmailConstants.LEADS_LIST_PROCESSED_NOT_SHARED == templateId) {
			htmlName = "lead-list-processed-not-shared-notification";
			subject = "Your Leads list has been processed successfully";
		} else if (EmailConstants.LEADS_LIST_IN_PROCESS == templateId) {
			htmlName = "lead-list-in-process-notification";
			subject = "We're processing your leads  list";
		} else if (EmailConstants.LEADS_LIST_PROCESSED_SHARED == templateId) {
			htmlName = "lead-list-processed-shared-notification";
			subject = "Your Leads list has been processed and shared successfully";
			model.put("companyName",
					userList.getCompany() != null ? userList.getCompany().getCompanyProfileName() : "");
		} else if (EmailConstants.LEADS_LIST_SHARED_TO_PARTNER == templateId) {
			htmlName = "lead-list-shared-to-partner";
			subject = "Your Leads list has been shared successfully";
			model.put("companyName",
					userList.getCompany() != null ? userList.getCompany().getCompanyProfileName() : "");
		} else if (EmailConstants.LEADS_LIST_UPDATED == templateId) {
			htmlName = "lead-list-updated-notification";
			subject = "New Lead(s) added to the lead list";
		}
		subject = utilService.addPerfixToSubject(subject);
		model.put("targetURL", webUrl + LOGIN);
		model.put("welcomeDisplayName", XamplifyUtils.getCustomerFullName(user));
		model.put("leadListName", XamplifyUtils.escapeDollarSequece(userList.getName()));
		model.put("partnerModuleCustomName", partnerModuleCustomName);
		// Integer companyId = user.getCompanyProfile().getId();
		if (userList.isEmailValidationInd() && templateId != EmailConstants.LEADS_LIST_SHARED_TO_PARTNER) {
			// Integer companyId = userList.getCompany().getId();
			List<User> exUsers = new ArrayList<User>(userList.getUsers());
			List<User> unsubscribedUsers = exUsers.stream()
					.filter(exuser -> totalUnsubscribedUserIds.contains(exuser.getUserId()))
					.collect(Collectors.toList());
			List<Integer> unsubscribedUserIds = unsubscribedUsers.stream().map(User::getUserId)
					.collect(Collectors.toList());
			exUsers.removeIf(exUser -> unsubscribedUserIds.contains(exUser.getUserId()));
			List<User> subscribedUsers = exUsers.stream().distinct().collect(Collectors.toList());
			long validUsers = subscribedUsers
					.stream().filter(
							u -> u.isEmailValidationInd() == true
									&& (u.getUserStatus().equals(UserStatus.APPROVED)
											|| u.getUserStatus().equals(UserStatus.UNAPPROVED))
									&& u.isEmailValid() == true)
					.count();
			long inValidUsers = subscribedUsers.stream()
					.filter(u -> u.isEmailValidationInd() == true && u.isEmailValid() == false).count();
			model.put("validLeadsCount", String.valueOf(validUsers));
			model.put("invalidLeadsCount", String.valueOf(inValidUsers));
		}
		context.setVariables(model);
		String receiverEmailId = user.getEmailId();
		sendEmailByThymleafTemplate(context, htmlName, receiverEmailId, subject);

	}

	public void sendLeadsListDeleteMail(User user, UserList userList, String htmlName, String campaigns)
			throws MailException {
		Context context = new Context();
		Map<String, Object> model = new HashMap<>();
		String subject = utilService.addPerfixToSubject("Shared Leads List have been deleted");
		model.put("targetURL", webUrl + LOGIN);
		model.put("welcomeDisplayName", XamplifyUtils.getCustomerFullName(user));
		model.put("leadListName", XamplifyUtils.escapeDollarSequece(userList.getName()));
		if (campaigns != null) {
			model.put("campaigns", XamplifyUtils.escapeDollarSequece(campaigns));
		}
		context.setVariables(model);
		String receiverEmailId = user.getEmailId();
		sendEmailByThymleafTemplate(context, htmlName, receiverEmailId, subject);
	}

	private void sendEmailByThymleafTemplate(Context context, String htmlName, String receiverEmailId, String subject) {
		String html = templateEngine.process(htmlName, context);
		sendMail(new EmailBuilder().from(email).senderName(mailSender).to(receiverEmailId).subject(subject).body(html)
				.build());
	}

	public void sendDealStatusChangeMail(Deal deal, Integer loggedInUserId) throws MailException {
		CompanyProfile loggedInCompany = userService.getCompanyProfileByUser(loggedInUserId);
		if (loggedInCompany != null) {
			if (deal.getCreatedForCompany().getId().equals(loggedInCompany.getId())) { // If Vendor is changing the
																						// stage
				if (!deal.getCreatedForCompany().getId().equals(deal.getCreatedByCompany().getId())) { // Not Orgadmin
					sendDealStatusChangeMailToPartner(deal);
				} else { // This is Orgadmin's self deal and Orgadmin is changing the stage
					sendDealUpdatedMailToOrgAdmin(deal);
				}
			} else if (deal.getCreatedByCompany().getId().equals(loggedInCompany.getId())) {// If Partner is changing
																							// the stage
				sendDealMailToVendor(deal, true);
				if (!deal.getCreatedBy().equals(loggedInUserId)) {// Notification to the person who created the deal
					sendDealStatusChangeMailToPartner(deal);
				}
			}
		}
	}

	private void sendDealStatusChangeMailToPartner(Deal deal) {
		User createdUser = userService.loadUser(
				Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, deal.getCreatedBy())),
				new FindLevel[] { FindLevel.COMPANY_PROFILE });

		EmailTemplate template = genericDao.get(EmailTemplate.class, 42);
		String body = template.getBody();
		body = body.replaceAll("PARTNER_FIRST_NAME", getUserName(createdUser));
		body = body.replaceAll("VENDOR_COMPANY_NAME",
				XamplifyUtils.escapeDollarSequece(deal.getCreatedForCompany().getCompanyName()));
		body = body.replaceAll("DEAL_TITLE", XamplifyUtils.escapeDollarSequece(deal.getTitle()));
		body = body.replaceAll("DEAL_STATUS", deal.getCurrentStage().getStageName());
		String dealComment = ((deal.getDealComment() != null || deal.getDealComment() != "") ? deal.getDealComment()
				: "---");
		body = body.replaceAll("DEAL_COMMENT", dealComment);// XNFR-426
		body = body.replaceAll("LOGIN_LINK", webUrl + "/login");

		if (deal.getAssociatedLead() != null) {
			body = body.replaceAll("LEAD_COMPANY_NAME",
					XamplifyUtils.escapeDollarSequece(deal.getAssociatedLead().getCompany()));
		} else {
			body = body.replaceAll("LEAD_COMPANY_NAME", "---");
		}
		body = body.replaceAll("CAMPAIGN_NAME", "---");
		String subject = utilService.addPerfixToSubject(template.getSubject());
		mailService.sendMail(new EmailBuilder().from(email).senderName(mailSender).to(createdUser.getEmailId())
				.subject(subject).body(body).build());

	}

	// XNFR-221
	public void sendDealOpenedMail(Deal deal) {
		if (!deal.getCreatedForCompany().getId().equals(deal.getCreatedByCompany().getId())) {
			sendDealMailToVendor(deal, false);
		} else {
			sendDealOpenedMailToOrgAdmin(deal);
		}
	}

	public String getUserName(User user) {
		String userName = "";
		if (user != null) {
			if (user.getFirstName() != null)
				userName = XamplifyUtils.escapeDollarSequece(userName)
						+ XamplifyUtils.escapeDollarSequece(user.getFirstName());
			if (user.getLastName() != null)
				userName = userName + " " + XamplifyUtils.escapeDollarSequece(user.getLastName());
		}
		return userName;
	}

	public String replaceCustomerFullName(UserDTO userDTO, String body) {
		User user = new User();
		user.setEmailId(userDTO.getEmailId());
		user.setFirstName(userDTO.getFirstName());
		user.setLastName(userDTO.getLastName());
		return XamplifyUtils.replaceCustomerFullName(user, body);
	}

	// XNFR-221
	public void sendLeadAddedEmail(Lead lead) {
		if (!lead.getCreatedForCompany().getId().equals(lead.getCreatedByCompany().getId())) {
			sendLeadEmailToVendor(lead, false);
		} else {
			sendLeadAddedEmailToOrgAdmin(lead);
		}
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

	public String getFirstNameMiddleNameLastName(String firstName, String middleName, String lastName) {
		StringBuilder nameBuilder = new StringBuilder();

		if (firstName != null && !firstName.trim().isEmpty()) {
			nameBuilder.append(firstName.trim());
		}

		if (middleName != null && !middleName.trim().isEmpty()) {
			if (nameBuilder.length() > 0)
				nameBuilder.append(" ");
			nameBuilder.append(middleName.trim());
		}

		if (lastName != null && !lastName.trim().isEmpty()) {
			if (nameBuilder.length() > 0)
				nameBuilder.append(" ");
			nameBuilder.append(lastName.trim());
		}

		return nameBuilder.toString();
	}

	public void sendPartnerDeclinedEmail(User receiver, User sender) {
		String fromEmail = receiver.getEmailId();
		String sendorName = (receiver.getFirstName() != null) ? receiver.getFirstName()
				: (receiver.getLastName() != null ? receiver.getLastName()
						: receiver.getCompanyProfile().getCompanyName());
		String subject = "Decline Notification";

		Context context = new Context();
		Map<String, Object> model = new HashMap<>();
		model.put("vendor", receiver);
		model.put("vendorCompanyLogo", server_path + receiver.getCompanyProfile().getCompanyLogoPath());
		model.put("partnerFirstName", sender.getFirstName());
		context.setVariables(model);
		String html = templateEngine.process("decline-partner", context);

		sendMail(new EmailBuilder().from(fromEmail).senderName(sendorName).to(sender.getEmailId()).subject(subject)
				.body(html).build());

	}

	public DripEmailHistory sendDripEmail(User user, EmailTemplate emailTemplate, String name) {
		DripEmailHistory dripObj = new DripEmailHistory();
		try {
			String body = emailTemplate.getBody();
			if (user != null) {
				if (StringUtils.hasText(user.getFirstName())) {
					body = body.replaceAll(name, XamplifyUtils.escapeDollarSequece(user.getFirstName()));
				} else {
					body = body.replaceAll(name, replaceThere);
				}
				if (body.contains(DripEmailConstants.COMPANY_NAME)) {
					body = body.replaceAll(DripEmailConstants.COMPANY_NAME,
							XamplifyUtils.escapeDollarSequece(user.getCompanyProfile().getCompanyName()));
				}
				String subject = emailTemplate.getSubject();
				if (subject.contains(DripEmailConstants.COMPANY_NAME)) {
					subject = subject.replaceAll(DripEmailConstants.COMPANY_NAME,
							XamplifyUtils.escapeDollarSequece(user.getCompanyProfile().getCompanyName()));
				}

				dripObj.setIsEmailSent(true);
			}
		} catch (Exception e) {
			dripObj.setIsEmailSent(false);
			dripObj.setEmailStatusMessage(e.getMessage());
		}
		return dripObj;
	}

	public DripEmailHistory sendPartnerDripEmail(User user, EmailTemplate emailTemplate, String name,
			String companyName) {
		DripEmailHistory dripObj = new DripEmailHistory();
		try {
			String body = emailTemplate.getBody();
			if (user != null) {
				if (StringUtils.hasText(user.getFirstName())) {
					body = body.replaceAll(name, XamplifyUtils.escapeDollarSequece(user.getFirstName()));
				} else {
					body = body.replaceAll(name, replaceThere);
				}
				if (body.contains(DripEmailConstants.COMPANY_NAME)) {
					body = body.replaceAll(DripEmailConstants.COMPANY_NAME,
							XamplifyUtils.escapeDollarSequece(companyName));
				}
				String subject = emailTemplate.getSubject();
				if (subject.contains(DripEmailConstants.COMPANY_NAME)) {
					subject = subject.replaceAll(DripEmailConstants.COMPANY_NAME,
							XamplifyUtils.escapeDollarSequece(user.getCompanyProfile().getCompanyName()));
				}
				dripObj.setIsEmailSent(true);
			}
		} catch (Exception e) {
			dripObj.setIsEmailSent(false);
			dripObj.setEmailStatusMessage(e.getMessage());
		}
		return dripObj;
	}

	public void sendVendorUpgradationEmail(User user, int templateId) throws MailException {
		EmailTemplate template = genericDao.get(EmailTemplate.class, templateId);
		if (user != null && template != null) {
			String body = template.getBody();
			body = body.replaceAll("CUSTOMER_FULL_NAME",
					user.getFirstName() != null ? XamplifyUtils.escapeDollarSequece(user.getFirstName())
							: replaceThere);
			String subject = utilService.addPerfixToSubject(template.getSubject());
			sendMail(new EmailBuilder().from(email).senderName(mailSender).to(user.getEmailId()).subject(subject)
					.body(body).build());
		}
	}

	public void sendDealUpdatedMailToOrgAdmin(Deal deal) {
		Integer partnerShipId = partnerShipDao.getPartnershipIdByVendorCompanyIdAndPartnerCompanyId(
				deal.getCreatedForCompany().getId(), deal.getCreatedByCompany().getId());
		// List<User> toUsers =
		// partnerShipDao.getOwners(deal.getCreatedForCompany().getId());
		List<UserDTO> toUsers = partnerShipDao.getOwnersAndChannelAccountManagers(deal.getCreatedForCompany().getId(),
				partnerShipId);

		if (toUsers != null && !toUsers.isEmpty()) {
			for (UserDTO toUser : toUsers) {
				boolean hasVanityAccess = utilDao.hasVanityAccessByUserId(toUser.getUserId());
				if (hasVanityAccess) {
					String partnerModuleCustomName = "";
					sendNotification(null, deal, partnerModuleCustomName, DefaultEmailTemplateType.UPDATE_SELF_DEAL,
							toUser);
				} else {
					Context context = new Context();
					Map<String, Object> model = new HashMap<>();
					model.put("deal", deal);
					model.put("toName", getFullName(toUser.getFirstName(), toUser.getLastName()));
					model.put("targetURL", webUrl + "/login");
					context.setVariables(model);
					String html = templateEngine.process("updateSelfDeal", context);
					String subject = utilService.addPerfixToSubject("An update on a deal");

					sendMail(new EmailBuilder().from(fromEmail).senderName(fromName).to(toUser.getEmailId())
							.subject(subject).body(html).build());
				}
			}
		}
	}

	public void sendLeadUpdatedEmail(Lead lead) {
		if (!lead.getCreatedForCompany().getId().equals(lead.getCreatedByCompany().getId())) {
			sendLeadEmailToVendor(lead, true);
		} else {
			sendLeadUpdatedEmailToOrgAdmin(lead);
		}
	}

	public void sendLeadUpdatedEmailToOrgAdmin(Lead lead) {
		Integer partnerShipId = partnerShipDao.getPartnershipIdByVendorCompanyIdAndPartnerCompanyId(
				lead.getCreatedForCompany().getId(), lead.getCreatedByCompany().getId());
		// List<User> toUsers =
		// partnerShipDao.getOwners(deal.getCreatedForCompany().getId());
		List<UserDTO> toUsers = partnerShipDao.getOwnersAndChannelAccountManagers(lead.getCreatedForCompany().getId(),
				partnerShipId);
		if (toUsers != null && !toUsers.isEmpty()) {
			for (UserDTO toUser : toUsers) {
				boolean hasVanityAccess = utilDao.hasVanityAccessByUserId(toUser.getUserId());
				if (hasVanityAccess) {
					String partnerModuleCustomName = "";
					sendNotification(lead, null, partnerModuleCustomName, DefaultEmailTemplateType.UPDATE_SELF_LEAD,
							toUser);
				} else {
					Context context = new Context();
					Map<String, Object> model = new HashMap<>();
					model.put("lead", lead);
					model.put("toName", getFullName(toUser.getFirstName(), toUser.getLastName()));
					context.setVariables(model);
					String html = templateEngine.process("updateSelfLead", context);
					String subject = utilService.addPerfixToSubject("An update on a lead");

					sendMail(new EmailBuilder().from(fromEmail).senderName(fromName).to(toUser.getEmailId())
							.subject(subject).body(html).build());
				}
			}
		}
	}

	public void sendDealUpdatedMail(Deal deal) {
		if (!deal.getCreatedForCompany().getId().equals(deal.getCreatedByCompany().getId())) {
			sendDealMailToVendor(deal, true);
		} else {
			sendDealUpdatedMailToOrgAdmin(deal);
		}
	}

	public void sendDealMailToVendor(Deal deal, boolean isDealUpdated) {

		Integer partnerShipId = partnerShipDao.getPartnershipIdByVendorCompanyIdAndPartnerCompanyId(
				deal.getCreatedForCompany().getId(), deal.getCreatedByCompany().getId());
		// List<User> toUsers =
		// partnerShipDao.getOwners(deal.getCreatedForCompany().getId());
		List<UserDTO> toUsers = partnerShipDao.getOwnersAndChannelAccountManagers(deal.getCreatedForCompany().getId(),
				partnerShipId);
		if (toUsers != null && !toUsers.isEmpty()) {
			/*** XNFR-84 ****/
			String partnerModuleCustomName = utilService
					.findPartnerModuleCustomNameByCompanyId(deal.getCreatedForCompany().getId());
			/*** XNFR-84 ****/
			for (UserDTO toUser : toUsers) {
				boolean hasVanityAccess = utilDao.hasVanityAccessByUserId(toUser.getUserId());
				if (hasVanityAccess) {
					vanityDealEmailNotification(deal, isDealUpdated, partnerModuleCustomName, toUser);
				} else {
					Context context = new Context();
					Map<String, Object> model = new HashMap<>();
					model.put("deal", deal);
					model.put("partnerModuleCustomName", partnerModuleCustomName);
					model.put("toName", getFullName(toUser.getFirstName(), toUser.getLastName()));
					model.put("targetURL", webUrl + "/login");
					context.setVariables(model);
					String subject = "";
					String html = "";
					if (isDealUpdated) {
						html = templateEngine.process("updateDeal", context);
						subject = " has updated a deal";
					} else {
						html = templateEngine.process("addDeal", context);
						subject = " has registered a deal";
					}
					sendMail(new EmailBuilder().from(fromEmail).senderName(fromName).to(toUser.getEmailId())
							.subject(utilService.addPerfixToSubject(deal.getCreatedByCompanyName() + subject))
							.body(html).build());
				}

			}

		}
	}

	private void vanityDealEmailNotification(Deal deal, boolean isDealUpdated, String partnerModuleCustomName,
			UserDTO toUser) {
		boolean isPrm = false;
		boolean isPartner = false;
		boolean isTeamMember = false;
		isPrm = utilDao.isPrmCompany(toUser.getUserId());
		isPartner = utilDao.isPartnerCompany(toUser.getUserId());
		isTeamMember = teamDao.isTeamMember(toUser.getUserId());

		DefaultEmailTemplateType emailTemplateType = null;

		if (isDealUpdated && (isPrm || isPartner || isTeamMember)) {
			emailTemplateType = DefaultEmailTemplateType.DEAL_UPDATE;
		} else {
			emailTemplateType = DefaultEmailTemplateType.ADD_DEAL;
		}
		sendNotification(null, deal, partnerModuleCustomName, emailTemplateType, toUser);
	}

	public void sendDealOpenedMailToOrgAdmin(Deal deal) {
		Integer partnerShipId = partnerShipDao.getPartnershipIdByVendorCompanyIdAndPartnerCompanyId(
				deal.getCreatedForCompany().getId(), deal.getCreatedByCompany().getId());
		// List<User> toUsers =
		// partnerShipDao.getOwners(deal.getCreatedForCompany().getId());
		List<UserDTO> toUsers = partnerShipDao.getOwnersAndChannelAccountManagers(deal.getCreatedForCompany().getId(),
				partnerShipId);
		if (toUsers != null && !toUsers.isEmpty()) {
			for (UserDTO toUser : toUsers) {
				boolean hasVanityAccess = utilDao.hasVanityAccessByUserId(toUser.getUserId());
				if (hasVanityAccess) {
					String partnerModuleCustomName = "";
					sendNotification(null, deal, partnerModuleCustomName, DefaultEmailTemplateType.ADD_SELF_DEAL,
							toUser);
				} else {
					Context context = new Context();
					Map<String, Object> model = new HashMap<>();
					model.put("deal", deal);
					model.put("toName", getFullName(toUser.getFirstName(), toUser.getLastName()));
					model.put("targetURL", webUrl + "/login");
					context.setVariables(model);
					String html = templateEngine.process("addSelfDeal", context);
					String subject = utilService.addPerfixToSubject("A new deal has registered");

					sendMail(new EmailBuilder().from(fromEmail).senderName(fromName).to(toUser.getEmailId())
							.subject(subject).body(html).build());
				}
			}
		}
	}

	public void sendLeadEmailToVendor(Lead lead, boolean isLeadUpdated) {
		Integer partnerShipId = partnerShipDao.getPartnershipIdByVendorCompanyIdAndPartnerCompanyId(
				lead.getCreatedForCompany().getId(), lead.getCreatedByCompany().getId());
		// List<User> toUsers =
		// partnerShipDao.getOwners(deal.getCreatedForCompany().getId());
		List<UserDTO> toUsers = partnerShipDao.getOwnersAndChannelAccountManagers(lead.getCreatedForCompany().getId(),
				partnerShipId);
		/*** XNFR-84 ****/
		String partnerModuleCustomName = utilService
				.findPartnerModuleCustomNameByCompanyId(lead.getCreatedForCompany().getId());
		/*** XNFR-84 ****/
		if (toUsers != null && !toUsers.isEmpty()) {
			for (UserDTO toUser : toUsers) {
				boolean hasVanityAccess = utilDao.hasVanityAccessByUserId(toUser.getUserId());
				if (hasVanityAccess) {
					vanityLeadEmailNotification(lead, isLeadUpdated, partnerModuleCustomName, toUser);
				} else {
					Context context = new Context();
					Map<String, Object> model = new HashMap<>();
					model.put("lead", lead);
					model.put("partnerModuleCustomName", partnerModuleCustomName);
					model.put("toName", getFullName(toUser.getFirstName(), toUser.getLastName()));
					context.setVariables(model);
					String subject = "";
					String html = "";
					if (isLeadUpdated) {
						if (utilDao.isPrmCompany(toUser.getUserId())) {
							html = templateEngine.process("prmUpdateLead", context);
						} else {
							html = templateEngine.process("updateLead", context);
						}
						subject = " has updated a lead";
					} else {
						if (utilDao.isPrmCompany(toUser.getUserId())) {
							html = templateEngine.process("prmAddLead", context);
						} else {
							html = templateEngine.process("addLead", context);
						}
						subject = " has added a new lead";
					}
					sendMail(new EmailBuilder().from(fromEmail).senderName(fromName).to(toUser.getEmailId())
							.subject(utilService.addPerfixToSubject(lead.getCreatedByCompanyName() + subject))
							.body(html).build());
				}
			}
		}
	}

	private void vanityLeadEmailNotification(Lead lead, boolean isLeadUpdated, String partnerModuleCustomName,
			UserDTO toUser) {
		boolean isPrm = false;
		boolean isPartner = false;
		boolean isTeamMember = false;
		isPrm = utilDao.isPrmCompany(toUser.getUserId());
		isPartner = utilDao.isPartnerCompany(toUser.getUserId());
		isTeamMember = teamDao.isTeamMember(toUser.getUserId());
		DefaultEmailTemplateType emailTemplateType = null;

		if (isLeadUpdated) {
			if (isPrm) {
				emailTemplateType = DefaultEmailTemplateType.PRM_UPDATED;
			} else if (isPartner || isTeamMember) {
				emailTemplateType = DefaultEmailTemplateType.LEAD_UPDATE;
			}
		} else {
			if (isPrm) {
				emailTemplateType = DefaultEmailTemplateType.PRM_ADD_LEAD;
			} else if (isPartner || isTeamMember) {
				emailTemplateType = DefaultEmailTemplateType.ADD_LEAD;
			}
		}

		sendNotification(lead, null, partnerModuleCustomName, emailTemplateType, toUser);
	}

	public void sendLeadAddedEmailToOrgAdmin(Lead lead) {
		Integer partnerShipId = partnerShipDao.getPartnershipIdByVendorCompanyIdAndPartnerCompanyId(
				lead.getCreatedForCompany().getId(), lead.getCreatedByCompany().getId());
		// List<User> toUsers =
		// partnerShipDao.getOwners(deal.getCreatedForCompany().getId());
		List<UserDTO> toUsers = partnerShipDao.getOwnersAndChannelAccountManagers(lead.getCreatedForCompany().getId(),
				partnerShipId);
		if (toUsers != null && !toUsers.isEmpty()) {
			for (UserDTO toUser : toUsers) {
				boolean hasVanityAccess = utilDao.hasVanityAccessByUserId(toUser.getUserId());
				if (hasVanityAccess) {
					String partnerModuleCustomName = "";
					sendNotification(lead, null, partnerModuleCustomName, DefaultEmailTemplateType.ADD_SELF_LEAD,
							toUser);
				} else {
					Context context = new Context();
					Map<String, Object> model = new HashMap<>();
					model.put("lead", lead);
					model.put("toName", getFullName(toUser.getFirstName(), toUser.getLastName()));
					context.setVariables(model);
					String html = templateEngine.process("addSelfLead", context);
					String subject = utilService.addPerfixToSubject("A new lead has created.");
					sendMail(new EmailBuilder().from(fromEmail).senderName(fromName).to(toUser.getEmailId())
							.subject(subject).body(html).build());
				}
			}
		}
	}

	// *******XNFR-316**************
	public void sendWorkflowEmail(User sender, User receiver, EmailTemplateDTO emailTemplateDTO) {
		String body = emailTemplateDTO.getBody();
		body = XamplifyUtils.replaceReceiverMergeTagsInfo(receiver, body);
		String subject = emailTemplateDTO.getSubject();
		subject = XamplifyUtils.replaceReceiverMergeTagsInfo(receiver, subject);
		if (emailTemplateDTO.isLearningTrackExist()) {
			emailTemplateDTO.setUserId(receiver.getUserId());
		}
		sendEmail(sender.getEmailId(), utilService.getFullName(sender), receiver.getEmailId(), subject, body,
				emailTemplateDTO);
	}

	public void sendEmail(String fromEmail, String fromName, String toEmail, String subject, String body,
			EmailTemplateDTO emailTemplateDTO) {
		sendMail(new EmailBuilder().from(fromEmail).senderName(fromName).to(toEmail).subject(subject).body(body)
				.userId(emailTemplateDTO.getUserId()).playbookWorkflowExists(emailTemplateDTO.isLearningTrackExist())
				.learningTrackId(emailTemplateDTO.getLearningTrackId()).workflowId(emailTemplateDTO.getWorkflowId())
				.build());
	}

	/****** XNFR-426 ********/
	public void sendLeadApprovedOrRejectedEmail(Lead lead, Integer userId) {
		User partner = userService.loadUser(
				Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, lead.getCreatedBy())),
				new FindLevel[] { FindLevel.SHALLOW });

		User customer = userService.loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, userId)),
				new FindLevel[] { FindLevel.COMPANY_PROFILE });

		String partnerModuleCustomName = utilService
				.findPartnerModuleCustomNameByCompanyId(lead.getCreatedForCompany().getId());
		String subject = null;
		String htmlBody = null;
		String senderName = XamplifyUtils.isValidString(getFullName(customer.getFirstName(), customer.getLastName()))
				? getFullName(customer.getFirstName(), customer.getLastName())
				: customer.getEmailId();
		String partnerName = XamplifyUtils.isValidString(getFullName(partner.getFirstName(), partner.getLastName()))
				? getFullName(partner.getFirstName(), partner.getLastName())
				: "There";
		if (utilDao.hasVanityAccessByUserId(userId)) {
			Map<String, String> result = frameVanityLeadApproveOrRejectMailData(customer, lead, partnerName,
					senderName);
			subject = result.get("subject");
			htmlBody = result.get("htmlBody");
		} else {
			Context context = new Context();
			Map<String, Object> model = new HashMap<>();
			model.put("lead", lead);
			model.put("partnerModuleCustomName", partnerModuleCustomName);
			model.put("toName", partnerName);
			model.put("approveOrReject", lead.getLeadApprovalStatusType().name().toString().toLowerCase() + "");
			// model.put("vendorName",firstName);
			model.put("vendorCompanyName", lead.getCreatedForCompany().getCompanyName());
			context.setVariables(model);
			htmlBody = templateEngine.process("approve-or-reject-lead", context);
			subject = lead.getCreatedForCompany().getCompanyName() + " has "
					+ lead.getLeadApprovalStatusType().name().toString().toLowerCase() + " a lead";
		}

		sendMail(new EmailBuilder().from(customer.getEmailId()).senderName(senderName).to(partner.getEmailId())
				.subject(subject).body(htmlBody).build());
	}

	public Map<String, String> frameVanityLeadApproveOrRejectMailData(User customer, Lead lead, String partnerName,
			String senderName) {
		Map<String, String> result = new HashMap<>();
		String subject = null;
		String htmlBody = null;
		boolean isPrm = utilDao.isPrmCompany(customer.getUserId());
		DefaultEmailTemplateType leadType;
		if ("APPROVED".equalsIgnoreCase(lead.getLeadApprovalStatusType().name())) {
			leadType = isPrm ? DefaultEmailTemplateType.PRM_LEAD_APPROVE : DefaultEmailTemplateType.LEAD_APPROVE;
		} else {
			leadType = isPrm ? DefaultEmailTemplateType.PRM_LEAD_REJECT : DefaultEmailTemplateType.LEAD_REJECT;
		}
		DefaultEmailTemplate vanityDefaultEmailTemplate = vanityURLDao.getVanityDefaultEmailTemplateByType(leadType);
		if (vanityDefaultEmailTemplate != null) {
			CustomDefaultEmailTemplate vanityEmailTemplate = vanityURLDao.getVanityETByDefVanityETIdAndCompanyId(
					vanityDefaultEmailTemplate.getId(), customer.getCompanyProfile());

			if (vanityEmailTemplate != null) {
				subject = vanityEmailTemplate.getSubject();
				htmlBody = genericDao.get(CustomDefaultEmailTemplate.class, vanityEmailTemplate.getId()).getHtmlBody();
			} else {
				subject = vanityDefaultEmailTemplate.getSubject();
				htmlBody = genericDao.get(DefaultEmailTemplate.class, vanityDefaultEmailTemplate.getId()).getHtmlBody();
			}

			htmlBody = replaceLeadMergeTags(customer, lead, htmlBody, partnerName, senderName);
		}
		result.put("htmlBody", htmlBody);
		result.put("subject", subject);
		return result;
	}

	private String replaceLeadMergeTags(User customer, Lead lead, String htmlBody, String partnerName,
			String senderName) {
		htmlBody = htmlBody.replace("{{leadStage}}", lead.getCurrentStage().getStageName());
		htmlBody = htmlBody.replace("{{leadComment}}", lead.getApprovalStatusComment());
		htmlBody = htmlBody.replace("{{leadName}}", getFullName(lead.getFirstName(), lead.getLastName()));
		htmlBody = htmlBody.replace("{{leadCompany}}",
				XamplifyUtils.isValidString(lead.getCompany()) ? lead.getCompany() : "---");
		htmlBody = htmlBody.replace("{{customerFullName}}", partnerName);
		htmlBody = htmlBody.replace("{{VENDOR_COMPANY_NAME}}", customer.getCompanyProfile().getCompanyName());
		htmlBody = htmlBody.replace("{{senderFullName}}", senderName);
		htmlBody = htmlBody.replace("<Vanity_Company_Logo_Href>",
				utilService.getWebsite(customer.getCompanyProfile().getWebsite()));
		htmlBody = htmlBody.replace("<Vanity_Company_Logo>",
				server_path + XamplifyUtils.escapeDollarSequece(customer.getCompanyProfile().getCompanyLogoPath()));
		htmlBody = htmlBody.replace(replaceCompanyLogo,
				server_path + XamplifyUtils.escapeDollarSequece(customer.getCompanyProfile().getCompanyLogoPath()));
		return htmlBody;
	}

	public void sendNotification(Lead lead, Deal deal, String partnerModuleCustomName, DefaultEmailTemplateType type,
			UserDTO toUser) {
		CompanyProfile companyProfile = null;
		String subject = null;
		String body = null;
		boolean isPrm = false;
		boolean isMarketing = false;
		boolean isOrgAdmin = false;
		boolean isLead = false;
		boolean isPartner = false;
		boolean isVendor = false;
		boolean isTeamMember = false;

		if (lead != null && deal == null) {
			isPrm = utilDao.isPrmCompany(toUser.getUserId());
			isPartner = utilDao.isPartnerCompany(toUser.getUserId());
			isTeamMember = teamDao.isTeamMember(toUser.getUserId());
			isLead = true;
			companyProfile = lead.getCreatedForCompany();
			if (companyProfile == null)
				return;
			subject = "Lead Subject";
			body = "Lead Body";
		} else if (deal != null && lead == null) {
			isPrm = utilDao.isPrmCompany(toUser.getUserId());
			isPartner = utilDao.isPartnerCompany(toUser.getUserId());
			isTeamMember = teamDao.isTeamMember(toUser.getUserId());
			isLead = false;
			companyProfile = deal.getCreatedForCompany();
			if (companyProfile == null)
				return;
			subject = "Deal Subject";
			body = "Deal Body";
		} else {
			return;
		}

		if (StringUtils.hasText(companyProfile.getCompanyProfileName()) && isLead ? lead != null : deal != null) {
			DefaultEmailTemplateType demo = type;
			DefaultEmailTemplate vanityDefaultEmailTemplate = vanityURLDao.getVanityDefaultEmailTemplateByType(demo);

			if (vanityDefaultEmailTemplate != null) {
				CustomDefaultEmailTemplate vanityEmailTemplate = vanityURLDao
						.getVanityETByDefVanityETIdAndCompanyId(vanityDefaultEmailTemplate.getId(), companyProfile);
				if (vanityEmailTemplate != null) {
					subject = vanityEmailTemplate.getSubject();
					body = genericDao.get(CustomDefaultEmailTemplate.class, vanityEmailTemplate.getId()).getHtmlBody();
				} else {
					subject = vanityDefaultEmailTemplate.getSubject();
					body = genericDao.get(DefaultEmailTemplate.class, vanityDefaultEmailTemplate.getId()).getHtmlBody();
				}
			}

			body = body.replaceAll("<Vanity_Company_Logo>",
					server_path + XamplifyUtils.escapeDollarSequece(companyProfile.getCompanyLogoPath()));
			body = body.replaceAll(replaceCompanyLogo,
					server_path + XamplifyUtils.escapeDollarSequece(companyProfile.getCompanyLogoPath()));
			body = body.replace("{{customerFullName}}",
					mailService.getFullName(toUser.getFirstName(), toUser.getLastName()));
			body = body.replace("{{partnerModuleCustomName}}",
					StringUtils.hasText(partnerModuleCustomName) ? partnerModuleCustomName : "Partner");
			body = body.replace("{{partnerName}}", isLead ? lead.getCreatedByName() : deal.getCreatedByName());
			body = body.replace("{{partnerCompany}}",
					isLead ? lead.getCreatedByCompanyName() : deal.getCreatedByCompanyName());
			if (isOrgAdmin || isMarketing || isTeamMember || isVendor || isPartner) {
				String createdByName = isLead ? lead.getCreatedByName() : deal.getCreatedByName();
				String createdByCompanyName = isLead ? lead.getCreatedByCompanyName() : deal.getCreatedByCompanyName();
				String companyName = isLead ? lead.getCreatedByCompanyName() : deal.getCreatedByCompanyName();

				createdByName = (createdByName != null && !createdByName.isEmpty()) ? createdByName : "---";
				createdByCompanyName = (createdByCompanyName != null && !createdByCompanyName.isEmpty())
						? createdByCompanyName
						: "---";
				companyName = (companyName != null && !companyName.isEmpty()) ? companyName : "---";
				body = body.replace("{{createdByName}}", createdByName);
				body = body.replace("{{createdByCompanyName}}", createdByCompanyName);
				body = body.replace("{{companyName}}", companyName);
			}
			String nameToUse;

			if (isLead) {
				nameToUse = mailService.getFullName(lead.getFirstName(), lead.getLastName());
			} else {
				if (deal.getAssociatedLead() != null) {
					nameToUse = deal.getAssociatedLead().getLastName();
				} else {
					nameToUse = "---";
				}
			}

			body = body.replace("{{leadName}}", nameToUse);

			String companyName;
			if (isLead) {
				companyName = lead.getCompany();
			} else {
				if (deal.getAssociatedLead() != null) {
					companyName = deal.getAssociatedLead().getCompany();
				} else {
					companyName = "---";
				}
			}
			body = body.replace("{{leadCompany}}", companyName != null ? companyName : "---");

			if (lead != null) {
				String activeCRM = leadDAO.getActiveCRMTypeByCompanyId(lead.getCreatedForCompany().getId());
				String stageName = (lead.getCurrentStage() != null && lead.getCurrentStage().getStageName() != null)
						? lead.getCurrentStage().getStageName()
						: "---";
				if (lead.getSfCustomFieldsData() != null && activeCRM.equals("salesforce")) {
					boolean replaced = false;
					for (SfCustomFieldsData sfcfData : lead.getSfCustomFieldsData()) {
						if (sfcfData.getFormLabel() != null && sfcfData.getFormLabel()
								.getFormDefaultFieldType() == FormDefaultFieldTypeEnum.PIPELINE_STAGE) {
							String value = sfcfData.getValue();
							body = body.replace("{{leadStage}}",
									(value != null && !value.isEmpty()) ? value : stageName);
							replaced = true;
							break;
						}
					}
					if (!replaced) {
						body = body.replace("{{leadStage}}", stageName);
					}
				} else {
					body = body.replace("{{leadStage}}", stageName);
				}
			} else {
				body = body.replace("{{leadStage}}", "---");
			}

			body = body.replace("{{leadComment}}",
					(lead != null && lead.getLeadComment() != null) ? lead.getLeadComment() : "---");

			if (isPrm) {
				body = body.replace("{{leadAssociatedCampaign}}", "");
				body = body.replace("Associated Campaign:", "");
			}
			body = body.replace("{{dealName}}", isLead ? "---" : deal.getTitle());
			body = body.replace("{{dealAmount}}", isLead ? "---" : String.valueOf(deal.getAmount()));
			body = body.replace("{{dealStage}}",
					isLead ? lead.getCurrentStage().getStageName() : deal.getCurrentStage().getStageName());
			String comment;
			if (isLead) {
				comment = (lead.getLeadComment() != null) ? lead.getLeadComment() : "---";
			} else {
				comment = (deal.getDealComment() != null) ? deal.getDealComment() : "---";
			}

			body = body.replace("{{dealComment}}", comment);
			body = body.replaceAll("<pageLink>", isLead ? "---" : webUrl + "/login");
			body = body.replaceAll("<Vanity_Company_Logo_Href>", companyProfile.getWebsite());
		} else {
			body = body.replaceAll("<Vanity_Company_Logo>", xAmplifyLogo);
			body = body.replaceAll("<Vanity_Company_Logo_Href>", xamplifyUtil.getLoginUrl());
		}

		mailService.sendMail(new EmailBuilder().from(fromEmail)
				.senderName(isLead ? lead.getCreatedByCompanyName() : deal.getCreatedByCompanyName())
				.to(toUser.getEmailId()).subject(subject).body(body).build());
	}

	public void sendPartnerTeamMemberEmail(Pagination pagination, String subject, String body, User vendor) {
		try {
			CompanyProfile companyProfile = vendor.getCompanyProfile();
			String senderName = XamplifyUtils.isValidString(getFullName(vendor.getFirstName(), vendor.getLastName()))
					? getFullName(vendor.getFirstName(), vendor.getLastName())
					: companyProfile.getCompanyName();
			for (int selectedEmailIds : pagination.getSelectedPartnerIds()) {
				String updatedBody = body;
				pagination.setTeamMemberId(selectedEmailIds);
				User partner = userDao.getFirstNameLastNameAndEmailIdByUserId(selectedEmailIds);
				body = replaceTeamMemberMergeTags(vendor, updatedBody, partner);
				sendMail(new EmailBuilder().from(vendor.getEmailId()).senderName(senderName).to(partner.getEmailId())
						.subject(subject).body(body).build());
				body = updatedBody;

			}
		} catch (MailException | HibernateException | PartnerDataAccessException e) {
			logger.error("sendPartnerTeamMemberEmail()", e);
			throw new PartnerDataAccessException(e.getMessage());
		}
	}

	private String replaceTeamMemberMergeTags(User vendor, String body, User partner) {
		String senderName = senderFullNameMergeTag(vendor);
		String fullName = "";
		boolean hasVanityAccess = utilDao.hasVanityAccessByUserId(vendor.getUserId());
		if (partner != null) {
			String firstName = (XamplifyUtils.isValidString(partner.getFirstName()))
					? XamplifyUtils.escapeDollarSequece(partner.getFirstName())
					: "";

			String lastName = (XamplifyUtils.isValidString(partner.getLastName()))
					? XamplifyUtils.escapeDollarSequece(partner.getLastName())
					: "";

			fullName = (firstName + " " + " " + lastName).trim();

		}
		if (!XamplifyUtils.isValidString(fullName)) {
			fullName = "There";
		}

		body = body.replace("{{customerFullName}}", fullName);
		fullName = "";

		body = body.replace("{{VENDOR_COMPANY_NAME}}",
				XamplifyUtils.escapeDollarSequece(vendor.getCompanyProfile().getCompanyName()));
		body = body.replace("{{VENDOR_FULL_NAME}}", senderName);
		if (hasVanityAccess) {
			String vanityURLDomain = xamplifyUtil.frameVanityURL(webUrl,
					vendor.getCompanyProfile().getCompanyProfileName());
			body = body.replace("login_url", vanityURLDomain);

			body = body.replace("<Vanity_Company_Logo_Href>",
					utilService.getWebsite(vendor.getCompanyProfile().getWebsite()));
			body = body.replace("<Vanity_Company_Logo>",
					server_path + XamplifyUtils.escapeDollarSequece(vendor.getCompanyProfile().getCompanyLogoPath()));
			body = body.replace(replaceCompanyLogo,
					server_path + XamplifyUtils.escapeDollarSequece(vendor.getCompanyProfile().getCompanyLogoPath()));
		} else {
			body = body.replace("<Vanity_Company_Logo>", xAmplifyLogo);
			body = body.replace("<Vanity_Company_Logo_Href>", xamplifyUtil.getLoginUrl());
			body = body.replace("login_url", webUrl);

		}
		return body;

	}

	private String senderFullNameMergeTag(User vendor) {
		String senderNameEmpty = "";
		if (vendor.getFirstName() != null && !vendor.getFirstName().trim().isEmpty()) {
			senderNameEmpty += vendor.getFirstName().trim();
		}
		if (vendor.getMiddleName() != null && !vendor.getMiddleName().trim().isEmpty()) {
			if (!senderNameEmpty.isEmpty()) {
				senderNameEmpty += " ";
			}
			senderNameEmpty += vendor.getMiddleName().trim();
		}
		if (vendor.getLastName() != null && !vendor.getLastName().trim().isEmpty()) {
			if (!senderNameEmpty.isEmpty()) {
				senderNameEmpty += " ";
			}
			senderNameEmpty += vendor.getLastName().trim();
		}
		String senderName = senderNameEmpty.isEmpty() ? "There" : senderNameEmpty;
		return senderName;
	}
}
