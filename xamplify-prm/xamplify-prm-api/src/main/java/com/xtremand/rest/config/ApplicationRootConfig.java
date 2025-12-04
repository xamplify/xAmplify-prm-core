package com.xtremand.rest.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheFactoryBean;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.xtremand.aop.LoggingAspectJ;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@ComponentScan(basePackages = {
		"com.xtremand" }, excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = { "com.test" }))
@EnableScheduling
@EnableAspectJAutoProxy
@EnableCaching
@EnableWebMvc
@EnableAsync
@PropertySource("classpath:config/config-${spring.profiles.active:dev}.properties")
@PropertySource("classpath:application.properties")
@PropertySource("classpath:social.properties")
@PropertySource("classpath:sql.properties")
@PropertySource("classpath:mdf-sql.properties")
@PropertySource("classpath:dam-sql.properties")
@PropertySource("classpath:contacts-sql.properties")
@PropertySource("classpath:tag-sql.properties")
@PropertySource("classpath:angular-urls.properties")
@PropertySource("classpath:dashboard-sql.properties")
@PropertySource("classpath:partner-reports-sql.properties")
@PropertySource("classpath:workflow-sql.properties")
@PropertySource("classpath:team-member-sql.properties")
@PropertySource("classpath:high-level-analytics-sql.properties")
@PropertySource("classpath:validation-messages/agency-validation-messages.properties")
@PropertySource("classpath:validation-messages/pageable-validation-messages.properties")
@PropertySource("classpath:validation-messages/validation-messages.properties")
@PropertySource("classpath:validation-messages/partner-journey-validation-messages.properties")
@PropertySource("classpath:validation-messages/customize-login-screen-validation-messages.properties")
@PropertySource("classpath:validation-messages/custom-link-validation-messages.properties")
@PropertySource("classpath:validation-messages/flexi-field-validation-messages.properties")
@PropertySource("classpath:messages.properties")
@PropertySource("classpath:campaigns-sql.properties")
@PropertySource("classpath:event-campaign-sql.properties")
@PropertySource("classpath:validation-messages/activity-validation-messages.properties")
@PropertySource("classpath:activity.properties")
public class ApplicationRootConfig extends WebMvcConfigurerAdapter implements AsyncConfigurer {

	/*** Scheduled Email Campaign Notification ****/
	private static final String SCHEDULED_EMAIL_CAMPAIGN_NOTIFICATION = "scheduledEmailCampaignNotification";

	private static final String SCHEDULED_EMAIL_CAMPAIGN_NOTIFICATION_BEAN_ID = SCHEDULED_EMAIL_CAMPAIGN_NOTIFICATION
			+ "-";

	/*** Scheduled Channel Email Campaign Partner Template ****/
	private static final String SCHEDULED_EMAIL_CHANNEL_CAMPAIGN_PARTNER_TEMPLATE = "scheduledEmailChannelCampaignPartnerTemplate";

	private static final String SCHEDULED_EMAIL_CHANNEL_CAMPAIGN_PARTNER_TEMPLATE_BEAN_ID = SCHEDULED_EMAIL_CHANNEL_CAMPAIGN_PARTNER_TEMPLATE
			+ "-";

	/*** Scheduled Video Campaign Notification ****/
	private static final String SCHEDULED_VIDEO_CAMPAIGN_NOTIFICATION = "scheduledVideoCampaignNotification";

	private static final String SCHEDULED_VIDEO_CAMPAIGN_NOTIFICATION_BEAN_ID = SCHEDULED_VIDEO_CAMPAIGN_NOTIFICATION
			+ "-";

	/*** Scheduled Channel Video Campaign Partner Template ****/
	private static final String SCHEDULED_VIDEO_CHANNEL_CAMPAIGN_PARTNER_TEMPLATE = "scheduledVideoChannelCampaignPartnerTemplate";

	private static final String SCHEDULED_VIDEO_CHANNEL_CAMPAIGN_PARTNER_TEMPLATE_BEAN_ID = SCHEDULED_VIDEO_CHANNEL_CAMPAIGN_PARTNER_TEMPLATE
			+ "-";

	/*** Scheduled Survey Campaign Notification ****/
	private static final String SCHEDULED_SURVEY_CAMPAIGN_NOTIFICATION = "scheduledSurveyCampaignNotification";

	private static final String SCHEDULED_SURVEY_CAMPAIGN_NOTIFICATION_BEAN_ID = SCHEDULED_SURVEY_CAMPAIGN_NOTIFICATION
			+ "-";

	/*** Scheduled Channel Survey Campaign Partner Template ****/
	private static final String SCHEDULED_SURVEY_CHANNEL_CAMPAIGN_PARTNER_TEMPLATE = "scheduledSurveyChannelCampaignPartnerTemplate";

	private static final String SCHEDULED_SURVEY_CHANNEL_CAMPAIGN_PARTNER_TEMPLATE_BEAN_ID = SCHEDULED_SURVEY_CHANNEL_CAMPAIGN_PARTNER_TEMPLATE
			+ "-";

	/*** Scheduled Event Campaign Notification ****/
	private static final String SCHEDULED_EVENT_CAMPAIGN_NOTIFICATION = "scheduledEventCampaignNotification";

	private static final String SCHEDULED_EVENT_CAMPAIGN_NOTIFICATION_BEAN_ID = SCHEDULED_EVENT_CAMPAIGN_NOTIFICATION
			+ "-";

	/*** Scheduled Channel Event Campaign Partner Template ****/
	private static final String SCHEDULED_EVENT_CHANNEL_CAMPAIGN_PARTNER_TEMPLATE = "scheduledEventChannelCampaignPartnerTemplate";

	private static final String SCHEDULED_EVENT_CHANNEL_CAMPAIGN_PARTNER_TEMPLATE_BEAN_ID = SCHEDULED_EVENT_CHANNEL_CAMPAIGN_PARTNER_TEMPLATE
			+ "-";

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private Environment environment;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");

		registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
	}

        @Bean
        public CacheManager cacheManager() {
                logger.info("Cache Manager");
                SimpleCacheManager cacheManager = new SimpleCacheManager();
                List<Cache> caches = new ArrayList<>();
		// to customize
		caches.add(defaultCache());
		cacheManager.setCaches(caches);
		return cacheManager;
	}

	@Bean
	public Cache defaultCache() {
		ConcurrentMapCacheFactoryBean cacheFactoryBean = new ConcurrentMapCacheFactoryBean();
		cacheFactoryBean.setName("default");
		cacheFactoryBean.afterPropertiesSet();
		return cacheFactoryBean.getObject();

	}

	@Bean
	@Profile({ "dev", "qa", "production", "release" })
	public DataSource dataSource() throws Exception {
		boolean useHikari = Boolean.parseBoolean(environment.getProperty("hikaricp.configuration"));
		if (useHikari) {
			HikariConfig config = new HikariConfig();

			config.setDriverClassName(environment.getProperty("jdbc.driver.className"));
			config.setJdbcUrl(environment.getProperty("jdbc.url"));
			config.setUsername(environment.getProperty("jdbc.username"));
			config.setPassword(environment.getProperty("jdbc.password"));

			config.setMaximumPoolSize(30);
			config.setMinimumIdle(5);
			config.setConnectionTimeout(60000);
			config.setIdleTimeout(600000);
			config.setMaxLifetime(1800000);
			config.setLeakDetectionThreshold(20000);

			config.addDataSourceProperty("autosave", "CONSERVATIVE");

			return new HikariDataSource(config);
		} else {
			ComboPooledDataSource bean = new ComboPooledDataSource();
			bean.setDriverClass(environment.getProperty("jdbc.driver.className"));
			bean.setJdbcUrl(environment.getProperty("jdbc.url"));
			bean.setUser(environment.getProperty("jdbc.username"));
			bean.setPassword(environment.getProperty("jdbc.password"));
			bean.setMinPoolSize(5);
			bean.setAcquireIncrement(5);
			bean.setMaxPoolSize(20);
			bean.setMaxAdministrativeTaskTime(60);

			Properties properties = bean.getProperties();
			// properties.setProperty("reWriteBatchedInserts", "true");
			properties.setProperty("autosave", "CONSERVATIVE");
			bean.setProperties(properties);
			return bean;
		}
	}

	@Bean
	public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
		MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
		return mappingJackson2HttpMessageConverter;
	}

	@Bean
	public HttpMessageConverter<Object> mappingJackson2XmlHttpMessageConverter() {
		MappingJackson2XmlHttpMessageConverter xmlConverter = new MappingJackson2XmlHttpMessageConverter();
		return xmlConverter;
	}

	@Bean(name = "multipartResolver")
	public CommonsMultipartResolver getResolver() throws IOException {
		CommonsMultipartResolver resolver = new CommonsMultipartResolver();
		resolver.setDefaultEncoding("utf-8");

		// Set the maximum allowed size (in bytes) for each individual file.
		// resolver.setMaxUploadSizePerFile(900000000);//900MB

		// You may also set other available properties.

		return resolver;
	}

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.add(mappingJackson2HttpMessageConverter());
		converters.add(mappingJackson2XmlHttpMessageConverter());
		super.configureMessageConverters(converters);

	}

	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		configurer.enable();
	}

	@Override
	@Bean
	public Executor getAsyncExecutor() {
		return getThreadPoolTaskExecutorInstance("xAmplify-Async-");
	}

	@Bean(name = "autoResponse")
	public Executor autoResponseExecutor() {
		return getThreadPoolTaskExecutorInstance("autoResponse-");
	}

	@Bean(name = "partnerEmails")
	public Executor partnerEmails() {
		return getThreadPoolTaskExecutorInstance("partnerEmails-");
	}

	@Bean(name = "campaignEmails")
	public Executor campaignEmails() {
		return getThreadPoolTaskExecutorInstance("campaignEmails-");
	}

	@Bean(name = "campaignEmailsHistory")
	public Executor campaignEmailsHistory() {
		return getThreadPoolTaskExecutorInstance("campaignEmailsHistory-");
	}

	@Bean(name = "campaignAutoResponseHistory")
	public Executor campaignAutoResponseHistory() {
		return getThreadPoolTaskExecutorInstance("campaignAutoResponseHistory-");
	}

	@Bean(name = "campaignEmailAdmin")
	public Executor campaignEmailAdmin() {
		return getThreadPoolTaskExecutorInstance("campaignEmailAdmin-");
	}

	@Bean(name = "teamMember")
	public Executor teamMember() {
		return getThreadPoolTaskExecutorInstance("teamMember-");
	}

	@Bean(name = "campaignEmailOnBehalfOfUser")
	public Executor campaignEmailOnBehalfOfUser() {
		return getThreadPoolTaskExecutorInstance("campaignEmailOnBehalfOfUser-");
	}

	@Bean(name = "processVideo")
	public Executor processVideo() {
		return getThreadPoolTaskExecutorInstance("processVideo-");
	}

	@Bean(name = "eventPreviewEmails")
	public Executor eventPreviewEmails() {
		return getThreadPoolTaskExecutorInstance("eventPreviewEmails-");
	}

	@Bean(name = "htmlImageGenerator")
	public Executor htmlToImageGenerator() {
		return getThreadPoolTaskExecutorInstance("htmlImageGenerator-");
	}

	@Bean(name = "partnerHtmlImageGenerator")
	public Executor partnerHtmlToImageGenerator() {
		return getThreadPoolTaskExecutorInstance("partnerHtmlImageGenerator-");
	}

	@Bean(name = "videoProcessor")
	public Executor videoProcessor() {
		return getThreadPoolTaskExecutorInstance("videoProcessor-");
	}

	@Bean(name = "formLeadsNotification")
	public Executor formLeads() {
		return getThreadPoolTaskExecutorInstance("formLeadsNotification-");
	}

	@Bean(name = "eventLeadsEmailsHistory")
	public Executor eventLeadsEmailsHistory() {
		return getThreadPoolTaskExecutorInstance("eventLeadsEmailsHistory-");
	}

	@Bean(name = "socialCampaignScheduler")
	public Executor socialCampaignScheduler() {
		return getThreadPoolTaskExecutorInstance("socialCampaignScheduler-");
	}

	@Bean(name = "campaignShortenUrl")
	public Executor campaignShortenUrl() {
		return getThreadPoolTaskExecutorInstance("campaignShortenUrl-");
	}

	@Bean(name = "marketoContacts")
	public Executor marketoContacts() {
		return getThreadPoolTaskExecutorInstance("marketoContacts-");
	}

	@Bean(name = "synchronizeMarketoContacts")
	public Executor synchronizeMarketoContacts() {
		return getThreadPoolTaskExecutorInstance("synchronizeMarketoContacts-");
	}

	@Bean(name = "enableMarketoSynchronization")
	public Executor enableMarketoSynchronization() {
		return getThreadPoolTaskExecutorInstance("enableMarketoSynchronization-");
	}

	@Bean(name = "teamMemberEmailsHistory")
	public Executor teamMemberEmailsHistory() {
		return getThreadPoolTaskExecutorInstance("teamMemberEmailsHistory-");
	}

	@Bean(name = "newMdfRequest")
	public Executor newMdfRequest() {
		return getThreadPoolTaskExecutorInstance("newMdfRequest-");
	}

	@Bean(name = "fundAmountUpdated")
	public Executor fundAmountUpdated() {
		return getThreadPoolTaskExecutorInstance("fundAmountUpdated-");
	}

	@Bean(name = "mdfRequestStatusChanged")
	public Executor mdfRequestStatusChanged() {
		return getThreadPoolTaskExecutorInstance("mdfRequestStatusChanged-");
	}

	@Bean(name = "mdfRequestDocumentsUploaded")
	public Executor mdfRequestDocumentsUploaded() {
		return getThreadPoolTaskExecutorInstance("mdfRequestDocumentsUploaded-");
	}

	@Bean(name = "uploadAsset")
	public Executor uploadAsset() {
		return getThreadPoolTaskExecutorInstance("uploadAsset-");
	}

	@Bean(name = "assetSharedNotification")
	public Executor assetSharedNotification() {
		return getThreadPoolTaskExecutorInstance("assetSharedNotification-");
	}

	@Bean(name = "generatePartnerTemplateThumbnails")
	public Executor generatePartnerTemplateThumbnails() {
		return getThreadPoolTaskExecutorInstance("generatePartnerTemplateThumbnails-");
	}

	@Bean(name = "lMSPublishedNotification")
	public Executor lMSPublishedNotification() {
		return getThreadPoolTaskExecutorInstance("lMSPublishedNotification-");
	}

	@Bean(name = "sfExpiredNotification")
	public Executor sfExpiredNotification() {
		return getThreadPoolTaskExecutorInstance("sfExpiredNotification-");
	}

	@Bean(name = "crmIntegrationInvalidNotification")
	public Executor crmIntegrationInvalidNotification() {
		return getThreadPoolTaskExecutorInstance("crmIntegrationInvalidNotification-");
	}

	@Bean(name = "microsoftConfigurationIssueNotification")
	public Executor microsoftConfigurationIssueNotification() {
		return getThreadPoolTaskExecutorInstance("microsoftConfigurationIssueNotification-");
	}

	@Bean(name = "damBeeTemplateImage")
	public Executor damBeeTemplateImage() {
		return getThreadPoolTaskExecutorInstance("damBeeTemplateImage-");
	}

	/*** Start XNFR-233 ****/
	@Bean(name = "loginBeeTemplateImage")
	public Executor loginBeeTemplateImage() {
		return getThreadPoolTaskExecutorInstance("loginBeeTemplateImage-");
	}

	/*** End XNFR-233 ****/

	@Bean(name = "sendCampaignTypeChangedNotification")
	public Executor sendCampaignTypeChangedNotification() {
		return getThreadPoolTaskExecutorInstance("sendCampaignTypeChangedNotification-");
	}

	@Bean(name = "publishDamToTeamMemberPartnerListPartners")
	public Executor publishDamToTeamMemberPartnerListPartners() {
		return getThreadPoolTaskExecutorInstance("publishDamToTeamMemberPartnerListPartners-");
	}

	@Bean(name = "publishLmsToNewUsersInUserList")
	public Executor publishLmsToNewUsersInUserList() {
		return getThreadPoolTaskExecutorInstance("publishLmsToNewUsersInUserList-");
	}

	@Bean(name = "deleteLMSForDeletedUsersInUserList")
	public Executor deleteLMSForDeletedUsersInUserList() {
		return getThreadPoolTaskExecutorInstance("deleteLMSForDeletedUsersInUserList-");
	}

	@Bean(name = "publishLMSToNewTeamMembers")
	public Executor publishLMSToNewTeamMembers() {
		return getThreadPoolTaskExecutorInstance("publishLMSToNewTeamMembers-");
	}

	@Bean(name = "savingCampaignsAsDraftNotification")
	public Executor savingCampaignsAsDraftNotification() {
		return getThreadPoolTaskExecutorInstance("savingCampaignsAsDraftNotification-");
	}

	@Bean(name = "userListScheduler")
	public Executor userListScheduler() {
		return getThreadPoolTaskExecutorInstance("userListScheduler-");
	}

	@Bean(name = "partnerOrContactNotification")
	public Executor partnerOrContactNotification() {
		return getThreadPoolTaskExecutorInstance("partnerOrContactNotification-");
	}

	@Bean(name = "publiEventEmailNotification")
	public Executor publiEventEmailNotification() {
		return getThreadPoolTaskExecutorInstance("publiEventEmailNotification-");
	}

	@Bean(name = "demoRequestNotificationToSalesTeam")
	public Executor demoRequestNotificationToSalesTeam() {
		return getThreadPoolTaskExecutorInstance("demoRequestNotificationToSalesTeam-");
	}

	@Bean(name = "vendorInvitation")
	public Executor vendorInvitation() {
		return getThreadPoolTaskExecutorInstance("vendorInvitation-");
	}

	@Bean(name = "partnerEmailNotification")
	public Executor partnerEmailNotification() {
		return getThreadPoolTaskExecutorInstance("partnerEmailNotification-");
	}

	@Bean(name = "activeAndInActivePartnerNotification")
	public Executor activeAndInActivePartnerNotification() {
		return getThreadPoolTaskExecutorInstance("activeAndInActivePartnerNotification-");
	}

	@Bean(name = "uploadThumbnail")
	public Executor uploadThumbnail() {
		return getThreadPoolTaskExecutorInstance("uploadThumbnail-");
	}

	@Bean(name = "oneClickLaunchCampaignNotification")
	public Executor oneClickLaunchCampaignNotification() {
		return getThreadPoolTaskExecutorInstance("oneClickLaunchCampaignNotification-");
	}

	@Bean(name = "downloadHighLevelAnalytics")
	public Executor downloadHighLevelAnalytics() {
		return getThreadPoolTaskExecutorInstance("downloadHighLevelAnalytics-");
	}

	@Bean(name = "agencyEmailNotification")
	public Executor agencyEmailNotification() {
		return getThreadPoolTaskExecutorInstance("agencyEmailNotification-");
	}

	@Bean(name = "setActiveCRMPipelinesForExistingCampaigns")
	public Executor setActiveCRMPipelinesForExistingCampaigns() {
		return getThreadPoolTaskExecutorInstance("setActiveCRMPipelinesForExistingCampaigns-");
	}

	@Bean(name = "setNewPipelinesForExistingCampaigns")
	public Executor setNewPipelinesForExistingCampaigns() {
		return getThreadPoolTaskExecutorInstance("setNewPipelinesForExistingCampaigns-");
	}

	@Bean(name = "campaignUserUserListAsync")
	public Executor campaignUserUserListAsync() {
		return getThreadPoolTaskExecutorInstance("campaignUserUserListAsync-");
	}

	@Bean(name = "ThreadCountExecutor")
	public Executor threadCountExecutor() {
		return getThreadPoolTaskExecutorInstance("ThreadCountExecutor-");
	}

	@Bean(name = "campaignEmailsToNewlyAddedPartners")
	public Executor campaignEmailsToNewlyAddedPartners() {
		return getThreadPoolTaskExecutorInstance("campaignEmailsToNewlyAddedPartners-");
	}

	@Bean(name = "campaignPartnerEmailTemplates")
	public Executor campaignPartnerEmailTemplates() {
		return getThreadPoolTaskExecutorInstance("campaignPartnerEmailTemplates-");
	}

	@Bean(name = "campaignPartnerLandingPages")
	public Executor campaignPartnerLandingPages() {
		return getThreadPoolTaskExecutorInstance("campaignPartnerLandingPages-");
	}

	@Bean(name = "createUsersInUserList")
	public Executor createUsersInUserList() {
		return getThreadPoolTaskExecutorInstance("createUsersInUserList-");
	}

	@Bean(name = "updateContactList")
	public Executor updateContactList() {
		return getThreadPoolTaskExecutorInstance("updateContactList-");
	}

	@Bean(name = "validateEmailIdsInBatch")
	public Executor validateEmailIdsInBatch() {
		return getThreadPoolTaskExecutorInstance("validateEmailIdsInBatch-");
	}

	/****** XNFR-222 ******/
	@Bean(name = "socialCampaignPartner")
	public Executor socialCampaignPartner() {
		return getThreadPoolTaskExecutorInstance("socialCampaignPartner-");
	}

	/****** XNFR-222 ******/
	@Bean(name = "socialCampaignEmailNotifications")
	public Executor socialCampaignEmailNotifications() {
		return getThreadPoolTaskExecutorInstance("socialCampaignEmailNotifications-");
	}

	@Bean(name = "scheduledEmailCampaign")
	public Executor scheduledEmailCampaign() {
		return getThreadPoolTaskExecutorInstance("scheduledEmailCampaign-");
	}

	@Bean(name = "scheduledVideoCampaign")
	public Executor scheduledVideoCampaign() {
		return getThreadPoolTaskExecutorInstance("scheduledVideoCampaign-");
	}

	@Bean(name = "scheduledEventCampaign")
	public Executor scheduledEventCampaign() {
		return getThreadPoolTaskExecutorInstance("scheduledEventCampaign-");
	}

	@Bean(name = "scheduledSurveyCampaign")
	public Executor scheduledSurveyCampaign() {
		return getThreadPoolTaskExecutorInstance("scheduledSurveyCampaign-");
	}

	@Bean(name = "scheduledPageCampaign")
	public Executor scheduledPageCampaign() {
		return getThreadPoolTaskExecutorInstance("scheduledPageCampaign-");
	}

	@Bean(name = "publishAsset")
	public Executor publishAsset() {
		return getThreadPoolTaskExecutorInstance("publishAsset-");
	}

	/****** XNFR-327 ****/
	@Bean(name = "publishTrackOrPlayBook")
	public Executor publishTrackOrPlayBook() {
		return getThreadPoolTaskExecutorInstance("publishTrackOrPlayBook-");
	}

	/****** XNFR-327 ****/
	@Bean(name = "updatePublishTrackOrPlayBook")
	public Executor updatePublishTrackOrPlayBook() {
		return getThreadPoolTaskExecutorInstance("updatePublishTrackOrPlayBook-");
	}

	/****** XNFR-327 ****/

	/****** XNFR-331 ****/
	@Bean(name = "emailIsNotOpened")
	public Executor emailIsNotOpened() {
		return getThreadPoolTaskExecutorInstance("emailIsNotOpened-");
	}

	@Bean(name = "emailIsOpened")
	public Executor emailIsOpened() {
		return getThreadPoolTaskExecutorInstance("emailIsOpened-");
	}

	@Bean(name = "followUpEmail")
	public Executor sendFollowUpEmail() {
		return getThreadPoolTaskExecutorInstance("followUpEmail-");
	}

	@Bean(name = "emailCampaignIsNotRedistributedByPartner")
	public Executor emailCampaignIsNotRedistributedByPartner() {
		return getThreadPoolTaskExecutorInstance("emailCampaignIsNotRedistributedByPartner-");
	}

	@Bean(name = "surveyCampaignIsNotRedistributedByPartner")
	public Executor surveyCampaignIsNotRedistributedByPartner() {
		return getThreadPoolTaskExecutorInstance("surveyCampaignIsNotRedistributedByPartner-");
	}

	@Bean(name = "videoCampaignIsNotRedistributedByPartner")
	public Executor videoCampaignIsNotRedistributedByPartner() {
		return getThreadPoolTaskExecutorInstance("videoCampaignIsNotRedistributedByPartner-");
	}

	@Bean(name = "eventCampaignIsNotRedistributedByPartner")
	public Executor eventCampaignIsNotRedistributedByPartner() {
		return getThreadPoolTaskExecutorInstance("eventCampaignIsNotRedistributedByPartner-");
	}

	@Bean(name = "videoIsPlayed")
	public Executor videoIsPlayed() {
		return getThreadPoolTaskExecutorInstance("videoIsPlayed-");
	}

	@Bean(name = "videoIsNotPlayed")
	public Executor videoIsNotPlayed() {
		return getThreadPoolTaskExecutorInstance("videoIsNotPlayed-");
	}

	@Bean(name = "linkNotClicked")
	public Executor linkNotClicked() {
		return getThreadPoolTaskExecutorInstance("linkNotClicked-");
	}

	@Bean(name = "linkClicked")
	public Executor linkClicked() {
		return getThreadPoolTaskExecutorInstance("linkClicked-");
	}

	@Bean(name = "respondedNo")
	public Executor respondedNo() {
		return getThreadPoolTaskExecutorInstance("respondedNo-");
	}

	@Bean(name = "respondedYes")
	public Executor respondedYes() {
		return getThreadPoolTaskExecutorInstance("respondedYes-");
	}

	@Bean(name = "afterEvent")
	public Executor sendAfterEvent() {
		return getThreadPoolTaskExecutorInstance("afterEvent-");
	}

	@Bean(name = "leadAutoResponseEmailNotification")
	public Executor sendLeadAutoResponseEmailNotification() {
		return getThreadPoolTaskExecutorInstance("leadAutoResponseEmailNotification-");
	}

	@Bean(name = "campaignEmailsForScheduler")
	public Executor sendCampaignEmailsForScheduler() {
		return getThreadPoolTaskExecutorInstance("campaignEmailsForScheduler-");
	}

	/**** XNFR-344 *******/
	@Bean(name = "hubspotLeadsAndDealsSync")
	public Executor hubspotLeadsAndDealsSync() {
		return getThreadPoolTaskExecutorInstance("hubspotLeadsAndDealsSync-");
	}

	@Bean(name = "salesforceLeadsAndDealsSync")
	public Executor salesforceLeadsAndDealsSync() {
		return getThreadPoolTaskExecutorInstance("salesforceLeadsAndDealsSync-");
	}

	@Bean(name = "pipedriveLeadsAndDealsSync")
	public Executor pipedriveLeadsAndDealsSync() {
		return getThreadPoolTaskExecutorInstance("pipedriveLeadsAndDealsSync-");
	}

	@Bean(name = "microsoftLeadsAndDealsSync")
	public Executor microsoftLeadsAndDealsSync() {
		return getThreadPoolTaskExecutorInstance("microsoftLeadsAndDealsSync-");
	}

	@Bean(name = "connectwiseLeadsAndDealsSync")
	public Executor connectwiseLeadsAndDealsSync() {
		return getThreadPoolTaskExecutorInstance("connectwiseLeadsAndDealsSync-");
	}

	/**** XNFR-316 *******/
	@Bean(name = "workflowEmails")
	public Executor workflowEmails() {
		return getThreadPoolTaskExecutorInstance("workflowEmails-");
	}

	/**** XNFR-342 *******/
	@Bean(name = "publishDamToNewlyAddedPartners")
	public Executor publishDamToNewlyAddedPartners() {
		return getThreadPoolTaskExecutorInstance("publishDamToNewlyAddedPartners-");
	}

	/**** XNFR-342 *******/
	@Bean(name = "publishTracksToNewlyAddedPartners")
	public Executor publishTracksToNewlyAddedPartners() {
		return getThreadPoolTaskExecutorInstance("publishTracksToNewlyAddedPartners-");
	}

	@Bean(name = "publishPlayBooksToNewlyAddedPartners")
	public Executor publishPlayBooksToNewlyAddedPartners() {
		return getThreadPoolTaskExecutorInstance("publishPlayBooksToNewlyAddedPartners-");
	}

	/****** XNFR-331 ****/

	@Bean(name = "publishVendorJourneyLandingPage")
	public Executor publishVendorJourneyLandingPage() {
		return getThreadPoolTaskExecutorInstance("publishVendorJourneyLandingPage-");
	}

	/****** XNFR-434 ****/
	@Bean(name = "replaceAsset")
	public Executor replaceAsset() {
		return getThreadPoolTaskExecutorInstance("replaceAsset-");
	}

	/****** XNFR-450 ****/
	@Bean(name = "syncMasterContactList")
	public Executor syncMasterContactList() {
		return getThreadPoolTaskExecutorInstance("syncMasterContactList-");
	}

	@Bean(name = "migration_createCompanyContacts")
	public Executor migrateCompanyContacts() {
		return getThreadPoolTaskExecutorInstance("migration_createCompanyContacts-");
	}

	@Bean(name = "mergeDuplicateCompanies")
	public Executor mergeDuplicateCompanies() {
		return getThreadPoolTaskExecutorInstance("mergeDuplicateCompanies-");
	}

	@Bean(name = "handleCompanyContacts")
	public Executor handleCompanyContacts() {
		return getThreadPoolTaskExecutorInstance("handleCompanyContacts-");
	}

	@Bean(name = "syncContactsInCompanyLists")
	public Executor syncContactsInCompanyLists() {
		return getThreadPoolTaskExecutorInstance("syncContactsInCompanyLists-");
	}

	@Bean(name = "checkSyncCompaniesInProgress")
	public Executor checkSyncCompaniesInProgress() {
		return getThreadPoolTaskExecutorInstance("checkSyncCompaniesInProgress-");
	}

	@Bean(name = "publishContentToCopiedList")
	public Executor publishContentToCopiedList() {
		return getThreadPoolTaskExecutorInstance("publishContentToCopiedList-");
	}

	@Bean(name = "processingUserLists")
	public Executor processingUserLists() {
		return getThreadPoolTaskExecutorInstance("processingUserLists-");
	}

	@Bean(name = "processEmailIdsByUserListId")
	public Executor processEmailIdsByUserListId() {
		return getThreadPoolTaskExecutorInstance("processEmailIdsByUserListId-");
	}

	@Bean(name = "processUserList")
	public Executor processUserList() {
		return getThreadPoolTaskExecutorInstance("processUserList-");
	}

	@Bean(name = "startProcessUserLists")
	public Executor startProcessUserLists() {
		return getThreadPoolTaskExecutorInstance("startProcessUserLists-");
	}

	@Bean(name = "noSalesforceForOauthSSO")
	public Executor noSalesforceForOauthSSO() {
		return getThreadPoolTaskExecutorInstance("noSalesforceForOauthSSO-");
	}

	@Bean(name = "noSalesforceAccountIdForLead")
	public Executor noSalesforceAccountIdForLead() {
		return getThreadPoolTaskExecutorInstance("noSalesforceAccountIdForLead-");
	}

	@Bean(name = "exceptionAlertEmailSender")
	public Executor exceptionAlertEmailSender() {
		return getThreadPoolTaskExecutorInstance("exceptionAlertEmailSender-");
	}

	@Bean(name = "externalApiTokenExpiredNotifier")
	public Executor externalApiTokenExpiredNotifier() {
		return getThreadPoolTaskExecutorInstance("externalApiTokenExpiredNotifier-");
	}

	/**
	 * Executor For Sending Email Notifications For Scheduled Email Campaigns
	 *******/
	@Bean(name = SCHEDULED_EMAIL_CAMPAIGN_NOTIFICATION)
	public Executor scheduledEmailCampaignNotification() {
		return getThreadPoolTaskExecutorInstance(SCHEDULED_EMAIL_CAMPAIGN_NOTIFICATION_BEAN_ID);
	}

	/**
	 * Executor For Sharing Email Template To Partners For Scheduled Email Campaigns
	 *******/
	@Bean(name = SCHEDULED_EMAIL_CHANNEL_CAMPAIGN_PARTNER_TEMPLATE)
	public Executor scheduledEmailChannelCampaignPartnerTemplateExecutor() {
		return getThreadPoolTaskExecutorInstance(SCHEDULED_EMAIL_CHANNEL_CAMPAIGN_PARTNER_TEMPLATE_BEAN_ID);
	}

	/**
	 * Executor For Sending Email Notifications For Scheduled Video Campaigns
	 *******/
	@Bean(name = SCHEDULED_VIDEO_CAMPAIGN_NOTIFICATION)
	public Executor scheduledVideoCampaignNotification() {
		return getThreadPoolTaskExecutorInstance(SCHEDULED_VIDEO_CAMPAIGN_NOTIFICATION_BEAN_ID);
	}

	/**
	 * Executor For Sharing Email Template To Partners For Scheduled Video Campaigns
	 *******/
	@Bean(name = SCHEDULED_VIDEO_CHANNEL_CAMPAIGN_PARTNER_TEMPLATE)
	public Executor scheduledVideoChannelCampaignPartnerTemplateExecutor() {
		return getThreadPoolTaskExecutorInstance(SCHEDULED_VIDEO_CHANNEL_CAMPAIGN_PARTNER_TEMPLATE_BEAN_ID);
	}

	/**
	 * Executor For Sending Email Notifications For Scheduled Survey Campaigns
	 *******/
	@Bean(name = SCHEDULED_SURVEY_CAMPAIGN_NOTIFICATION)
	public Executor scheduledSurveyCampaignNotification() {
		return getThreadPoolTaskExecutorInstance(SCHEDULED_SURVEY_CAMPAIGN_NOTIFICATION_BEAN_ID);
	}

	/**
	 * Executor For Sharing Email Template To Partners For Scheduled Video Campaigns
	 *******/
	@Bean(name = SCHEDULED_SURVEY_CHANNEL_CAMPAIGN_PARTNER_TEMPLATE)
	public Executor scheduledSurveyChannelCampaignPartnerTemplateExecutor() {
		return getThreadPoolTaskExecutorInstance(SCHEDULED_SURVEY_CHANNEL_CAMPAIGN_PARTNER_TEMPLATE_BEAN_ID);
	}

	/**
	 * Executor For Sending Email Notifications For Scheduled Event Campaigns
	 *******/
	@Bean(name = SCHEDULED_EVENT_CAMPAIGN_NOTIFICATION)
	public Executor scheduledEventCampaignNotification() {
		return getThreadPoolTaskExecutorInstance(SCHEDULED_EVENT_CAMPAIGN_NOTIFICATION_BEAN_ID);
	}

	/**
	 * Executor For Sharing Email Template To Partners For Scheduled Event Campaigns
	 *******/
	@Bean(name = SCHEDULED_EVENT_CHANNEL_CAMPAIGN_PARTNER_TEMPLATE)
	public Executor scheduledEventChannelCampaignPartnerTemplateExecutor() {
		return getThreadPoolTaskExecutorInstance(SCHEDULED_EVENT_CHANNEL_CAMPAIGN_PARTNER_TEMPLATE_BEAN_ID);
	}

	@Bean(name = "addPartnersToList")
	public Executor addPartnersToList() {
		return getThreadPoolTaskExecutorInstance("addPartnersToList-");
	}

	public ThreadPoolTaskScheduler getThreadPoolTaskExecutorInstance(String beanId) {
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		if (beanId.equals("teamMemberEmailsHistory-") || beanId.equals("campaignEmailsHistory-")
				|| beanId.equals("campaignShortenUrl-") || beanId.equals("marketoContacts-")
				|| beanId.equals("synchronizeMarketoContacts-") || beanId.equals("enableMarketoSynchronization-")
				|| beanId.equals("uploadAsset-") || beanId.equals("lMSPublishedNotification-")
				|| beanId.equals("sfExpiredNotification-") || beanId.equals("microsoftConfigurationIssueNotification-")
				|| beanId.equals("publishLmsToNewUsersInUserList-")
				|| beanId.equals("deleteLMSForDeletedUsersInUserList-")
				|| beanId.equals("publishDamToTeamMemberPartnerListPartners-")
				|| beanId.equals("publishLMSToNewTeamMembers-") || beanId.equals("uploadThumbnail-")
				|| beanId.equals("savingCampaignsAsDraftNotification-")
				|| beanId.equals("setActiveCRMPipelinesForExistingCampaigns-")
				|| beanId.equals("setNewPipelinesForExistingCampaigns-")
				|| beanId.equals("crmIntegrationInvalidNotification-") || beanId.equals("publishAsset-")
				|| beanId.equals("publishTrackOrPlayBook-") || beanId.equals("updatePublishTrackOrPlayBook-")
				|| beanId.equals("campaignUserUserListAsync-") || beanId.equals("campaignEmailsToNewlyAddedPartners-")
				|| beanId.equals("campaignEmails-") || beanId.equals("campaignPartnerEmailTemplates-")
				|| beanId.equals("campaignPartnerLandingPages-") || beanId.equals("validateEmailIdsInBatch-")
				|| beanId.equals("socialCampaignPartner-") || beanId.equals("publishDamToNewlyAddedPartners-")
				|| beanId.equals("uploadCsvToAws-") || beanId.equals("uploadUserList-")
				|| beanId.equals("syncMasterContactList-") || beanId.equals("migration_createCompanyContacts-")
				|| beanId.equals("mergeDuplicateCompanies-") || beanId.equals("handleCompanyContacts-")
				|| beanId.equals("publishContentToCopiedList-") || beanId.equals("publishVendorJourneyLandingPage-")
				|| beanId.equals("checkSyncCompaniesInProgress-") || beanId.equals("syncContactsInCompanyLists-")
				|| beanId.equals("publishPartnerJourneyPage-") || beanId.equals("uploadAttachmentFiles-")
				|| beanId.equals("sendTaskAddedEmailToAssignedUser-") || beanId.equals("sendOverDueEmailNotification-")
				|| beanId.equals("sendTaskCompletedEmailToAssignedByUser-")
				|| beanId.equals("sendRemainderEmailNotification-") || beanId.equals("deleteAttachmentFilesFromAWS-")
				|| beanId.equals("domainMediaUploader-") || beanId.equals("approvalStatusUpdatedNotifier-")
				|| beanId.equals("approvalPrivilegesUpdatedNotifier-")
				|| beanId.equals("sendLeadAddedOrUpdatedEmailToPartner-")
				|| beanId.equals("sendDealAddedOrUpdatedEmailToPartner-")
				|| beanId.equals("updatePartnerModuleAccesses-")
				|| beanId.equals("updatePartnershipCompanyIdByPartnerId-")
				|| beanId.equals("sendLeadFieldUpdatedNotification-")
				|| beanId.equals("sendPartnerSignatureRemainderEmailNotification-")
				|| beanId.equals("savePlaybookWorkFlows-") || beanId.equals("savePlaybookWorkflowEmailHistory-")
				|| beanId.equals("downloadListOfContacts-") || beanId.equals("updateActivityAttachmentOpenAIFileId-")
				|| beanId.equals("updateGlobalChatSearchColumn-") || beanId.equals("saveAndPushLeadToxAmplify-")
				|| beanId.equals("saveAndPushDealToxAmplify-")) {
			scheduler.setPoolSize(Integer.valueOf(environment.getProperty("three.pool.size")));
		} else if (beanId.equals("campaignEmailsForScheduler-") || beanId.equals("createUsersInUserList-")
				|| beanId.equals("addPartnersToList-") || "updateContactList-".equals(beanId)
				|| beanId.equals(SCHEDULED_EMAIL_CAMPAIGN_NOTIFICATION_BEAN_ID)
				|| beanId.equals(SCHEDULED_EMAIL_CHANNEL_CAMPAIGN_PARTNER_TEMPLATE_BEAN_ID)
				|| beanId.equals(SCHEDULED_VIDEO_CAMPAIGN_NOTIFICATION_BEAN_ID)
				|| beanId.equals(SCHEDULED_VIDEO_CHANNEL_CAMPAIGN_PARTNER_TEMPLATE_BEAN_ID)
				|| beanId.equals(SCHEDULED_SURVEY_CAMPAIGN_NOTIFICATION_BEAN_ID)
				|| beanId.equals(SCHEDULED_SURVEY_CHANNEL_CAMPAIGN_PARTNER_TEMPLATE_BEAN_ID)
				|| beanId.equals(SCHEDULED_EVENT_CAMPAIGN_NOTIFICATION_BEAN_ID)
				|| beanId.equals(SCHEDULED_EVENT_CHANNEL_CAMPAIGN_PARTNER_TEMPLATE_BEAN_ID)
				|| beanId.equals("uploadPdfToAws-")) {
			scheduler.setPoolSize(Integer.valueOf(environment.getProperty("ten.pool.size")));
		} else if (beanId.equals("userListScheduler-") || beanId.equals("processEmailIdsByUserListId-")
				|| beanId.equals("startProcessUserLists-") || beanId.equals("processUserList-")
				|| beanId.equals("publishCampaignsToNewlyAddedUsers-")) {
			scheduler.setPoolSize(Integer.valueOf(environment.getProperty("five.pool.size")));
		} else {
			scheduler.setPoolSize(Integer.valueOf(environment.getProperty("scheduler.pool.size")));
		}
		scheduler.setThreadNamePrefix(beanId);
		scheduler.initialize();
		return scheduler;
	}

	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return new CustomAsyncExceptionHandler();
	}

	@Bean
	public LoggingAspectJ logingAspect() {
		return new LoggingAspectJ();
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean(name = "uploadCsvToAws")
	public Executor uploadCsvToAws() {
		return getThreadPoolTaskExecutorInstance("uploadCsvToAws-");
	}

	@Bean(name = "uploadUserList")
	public Executor uploadUserList() {
		return getThreadPoolTaskExecutorInstance("uploadUserList-");
	}

	@Bean(name = "publishPartnerJourneyPage")
	public Executor publishPartnerJourneyPage() {
		return getThreadPoolTaskExecutorInstance("publishPartnerJourneyPage-");
	}

	@Bean(name = "uploadAttachmentFiles")
	public Executor uploadAttachmentFiles() {
		return getThreadPoolTaskExecutorInstance("uploadAttachmentFiles-");
	}

	@Bean(name = "publishCampaignsToNewlyAddedUsers")
	public Executor publishCampaignsToNewlyAddedUsers() {
		return getThreadPoolTaskExecutorInstance("publishCampaignsToNewlyAddedUsers-");
	}

	/** XNFR-757 **/
	@Bean(name = "sendTaskAddedEmailToAssignedUser")
	public Executor sendTaskAddedEmailToAssignedUser() {
		return getThreadPoolTaskExecutorInstance("sendTaskAddedEmailToAssignedUser-");
	}

	@Bean(name = "sendOverDueEmailNotification")
	public Executor sendOverDueEmailNotification() {
		return getThreadPoolTaskExecutorInstance("sendOverDueEmailNotification-");
	}

	@Bean(name = "sendTaskCompletedEmailToAssignedByUser")
	public Executor sendTaskCompletedEmailToAssignedByUser() {
		return getThreadPoolTaskExecutorInstance("sendTaskCompletedEmailToAssignedByUser-");
	}

	@Bean(name = "sendRemainderEmailNotification")
	public Executor sendRemainderEmailNotification() {
		return getThreadPoolTaskExecutorInstance("sendRemainderEmailNotification-");
	}

	@Bean(name = "deleteAttachmentFilesFromAWS")
	public Executor deleteAttachmentFilesFromAWS() {
		return getThreadPoolTaskExecutorInstance("deleteAttachmentFilesFromAWS-");
	}

	@Bean(name = "domainMediaUploader")
	public Executor domainMediaUploader() {
		return getThreadPoolTaskExecutorInstance("domainMediaUploader-");
	}

	@Bean(name = "approvalStatusUpdatedNotifier")
	public Executor approvalStatusUpdatedNotifier() {
		return getThreadPoolTaskExecutorInstance("approvalStatusUpdatedNotifier-");
	}

	@Bean(name = "approvalPrivilegesUpdatedNotifier")
	public Executor approvalPrivilegesUpdatedNotifier() {
		return getThreadPoolTaskExecutorInstance("approvalPrivilegesUpdatedNotifier-");
	}

	@Bean(name = "sendLeadAddedOrUpdatedEmailToPartner")
	public Executor sendLeadAddedOrUpdatedEmailToPartner() {
		return getThreadPoolTaskExecutorInstance("sendLeadAddedOrUpdatedEmailToPartner-");
	}

	@Bean(name = "sendDealAddedOrUpdatedEmailToPartner")
	public Executor sendDealAddedOrUpdatedEmailToPartner() {
		return getThreadPoolTaskExecutorInstance("sendDealAddedOrUpdatedEmailToPartner-");
	}

	@Bean(name = "updatePartnerModuleAccesses")
	public Executor updatePartnerModuleAccesses() {
		return getThreadPoolTaskExecutorInstance("updatePartnerModuleAccesses-");
	}

	@Bean(name = "updatePartnershipCompanyIdByPartnerId")
	public Executor updatePartnershipCompanyIdByPartnerId() {
		return getThreadPoolTaskExecutorInstance("updatePartnershipCompanyIdByPartnerId-");
	}

	@Bean(name = "sendLeadFieldUpdatedNotification")
	public Executor sendLeadFieldUpdatedNotification() {
		return getThreadPoolTaskExecutorInstance("sendLeadFieldUpdatedNotification-");
	}

	@Bean(name = "sendPartnerSignatureRemainderEmailNotification")
	public Executor sendPartnerSignatureRemainderEmailNotification() {
		return getThreadPoolTaskExecutorInstance("sendPartnerSignatureRemainderEmailNotification-");
	}

	@Bean(name = "uploadPdfToAws")
	public Executor uploadPdfToAws() {
		return getThreadPoolTaskExecutorInstance("uploadPdfToAws-");
	}

	@Bean(name = "deleteFileIdFromOpenAIFilesAndDam")
	public Executor deleteFileIdFromOpenAIFilesAndDam() {
		return getThreadPoolTaskExecutorInstance("deleteFileIdFromOpenAIFilesAndDam-");
	}

	@Bean(name = "deleteThreadAndVecotrStore")
	public Executor deleteThreadAndVecotrStore() {
		return getThreadPoolTaskExecutorInstance("deleteThreadAndVecotrStore-");
	}

	@Bean(name = "savePlaybookWorkFlows")
	public Executor savePlaybookWorkFlows() {
		return getThreadPoolTaskExecutorInstance("savePlaybookWorkFlows-");
	}

	// XNFR-993
	@Bean(name = "savePlaybookWorkflowEmailHistory")
	public Executor savePlaybookWorkflowEmailHistory() {
		return getThreadPoolTaskExecutorInstance("savePlaybookWorkflowEmailHistory-");
	}

	@Bean(name = "playbookWorkflowEmails")
	public Executor playbookWorkflowEmails() {
		return getThreadPoolTaskExecutorInstance("playbookWorkflowEmails-");
	}

	@Bean(name = "downloadListOfContacts")
	public Executor downloadListOfContacts() {
		return getThreadPoolTaskExecutorInstance("downloadListOfContacts-");
	}

	@Bean(name = "updateActivityAttachmentOpenAIFileId")
	public Executor updateActivityAttachmentOpenAIFileId() {
		return getThreadPoolTaskExecutorInstance("updateActivityAttachmentOpenAIFileId-");
	}

	@Bean(name = "updateGlobalChatSearchColumn")
	public Executor updateGlobalChatSearchColumn() {
		return getThreadPoolTaskExecutorInstance("updateGlobalChatSearchColumn-");
	}
	
	@Bean(name = "saveAndPushLeadToxAmplify")
	public Executor saveAndPushLeadToxAmplify() {
		return getThreadPoolTaskExecutorInstance("saveAndPushLeadToxAmplify-");
	}
	
	@Bean(name = "saveAndPushDealToxAmplify")
    public Executor saveAndPushDealToxAmplify() {
            return getThreadPoolTaskExecutorInstance("saveAndPushDealToxAmplify-");
    }

}