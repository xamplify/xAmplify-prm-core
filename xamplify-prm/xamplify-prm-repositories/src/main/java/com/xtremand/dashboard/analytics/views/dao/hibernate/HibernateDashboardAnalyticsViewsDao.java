package com.xtremand.dashboard.analytics.views.dao.hibernate;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.xtremand.analytics.dao.PartnerAnalyticsDAO;
import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.common.bom.Pagination;
import com.xtremand.dashboard.analytics.views.bom.DashboardModuleAnalyticsView;
import com.xtremand.dashboard.analytics.views.bom.EmailStatsVanityUrlView;
import com.xtremand.dashboard.analytics.views.bom.EmailStatsView;
import com.xtremand.dashboard.analytics.views.bom.OpportunitiesPartnerAnalyticsView;
import com.xtremand.dashboard.analytics.views.bom.OpportunitiesVanityUrlPartnerAnalyticsView;
import com.xtremand.dashboard.analytics.views.bom.OpportunitiesVendorAnalyticsView;
import com.xtremand.dashboard.analytics.views.bom.RegionalStatisticsVanityUrlView;
import com.xtremand.dashboard.analytics.views.bom.RegionalStatisticsView;
import com.xtremand.dashboard.analytics.views.bom.VendorActivityVanityUrlView;
import com.xtremand.dashboard.analytics.views.bom.VendorActivityView;
import com.xtremand.dashboard.analytics.views.bom.VendorEmailStatsView;
import com.xtremand.dashboard.analytics.views.bom.VendorRegionalStatisticsView;
import com.xtremand.dashboard.analytics.views.dao.DashboardAnalyticsViewsDao;
import com.xtremand.dashboard.analytics.views.dto.DealStatisticsDTO;
import com.xtremand.dashboard.analytics.views.dto.InstantNavigationLinksDTO;
import com.xtremand.dashboard.analytics.views.dto.PartnerAnalyticsCountDTO;
import com.xtremand.dashboard.analytics.views.dto.StatisticsDetailsOfPieChart;
import com.xtremand.dashboard.analytics.views.dto.UniversalSearchDTO;
import com.xtremand.dashboard.analytics.views.dto.WordCloudMapDTO;
import com.xtremand.formbeans.EmailLogReport;
import com.xtremand.integration.dao.IntegrationDao;
import com.xtremand.partnership.dao.PartnershipDAO;
import com.xtremand.team.member.dto.RoleDisplayDTO;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.util.DateUtils;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.HibernateSQLQueryResultUtilDao;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.LeftSideNavigationBarItem;
import com.xtremand.util.dto.NumberFormatterString;
import com.xtremand.util.dto.PaginatedDTO;
import com.xtremand.util.dto.QueryParameterDTO;
import com.xtremand.util.dto.TeamMemberFilterDTO;
import com.xtremand.util.dto.XamplifyConstants;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;

@Repository
public class HibernateDashboardAnalyticsViewsDao implements DashboardAnalyticsViewsDao {

	static final Logger logger = LoggerFactory.getLogger(HibernateDashboardAnalyticsViewsDao.class);

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private UserDAO userDao;

	@Autowired
	private UtilDao utilDao;

	@Autowired
	private IntegrationDao integrationDao; // XNFR-758

	private static final String PARTNER_COMPANY_ID = "partnerCompanyId";

	private static final String COMPANY_ID = "companyId";

	private static final String USER_ID = "userId";

	private static final String LEADS = "Leads";

	private static final String LEADS_WON = "Leads Won";

	private static final String LOST_LEADS = "Lost Leads";

	private static final String CONVERTED_LEADS = "Converted Leads";

	private static final String DEALS = "Deals";

	private static final String RECIPIENTS = "Recipients";

	private static final String DEALS_WON = "Won Deals";

	private static final String DEALS_LOST = "Lost Deals";

	private static final String CREATED_LEAD_TIME = "Updated Lead Time";

	private static final String CREATED_DEAL_TIME = "Updated Deal Time";

	private static final String CONTACT_TO_LEAD_RATIO = "Contact to Lead Ratio";

	private static final String LEAD_TO_DEAL_RATIO = "Lead to Deal Ratio";

	private static final String OPPORTUNITY_AMOUNT = "Opportunity Amount";

	private static final String SEARCH_QUERY_STRING = "${searchQueryString}";

	private static final String PARTNER_QUERY_STRING = "${partnerCompanyIdQueryString}"; // XNFR-758

	private static final String AND_XLTV_IS_PUBLISHED = " and xltv.is_published= true "; // XNFR-758

	private static final String NAVIGATE_QUERY_STRING = "${navigateQueryString}"; // XNFR-758

	private static final String VENDOR_NAVIGATE_QUERY_STRING = ",cast('Vendor' as text) as \"navigate\" ";

	private static final String WHERE_CONDITION_FOR_ASSERT_PARTNER_QUERY = "${whereCondionPartnerUserIdQueryString}"; // XNFR-578

	private static final String NAVIGATE_SHARED_STRING = ", cast('Shared' as text) as \"navigate\" "; // XNFR-578

	@Value("${dashboard.OrgAdminandPartnerLeadsStatuswiseDealsxamplifylogin}")
	private String OrgAdminandPartnerLeadsStatuswiseDealsxamplifylogin;

	@Value("${dashboard.contactsAnalyticsTreeMapQuery}")
	private String contactsAnalyticsTreeMapQuery;

	@Value("${dashboard.contactsAnalyticsTreeMapFilterQuery}")
	private String contactsAnalyticsTreeMapFilterQuery;

	@Value("${dashboard.partnerAnalyticsCountQuery}")
	private String partnerAnalyticsCountQuery;

	@Value("${dashboard.totalPartnerContactsQuery}")
	private String totalPartnerContactsQuery;

	@Value("${dashboard.totalPartnerContactsFilterQuery}")
	private String totalPartnerContactsFilterQuery;

	@Value("${dashboard.partnerCompanyNamesAndRedistributedCampaignsCountQuery}")
	private String partnerCompanyNamesAndRedistributedCampaignsCountQuery;

	@Value("${dashboard.partnerCompanyNamesAndRedistributedCampaignsCountFilterQuery}")
	private String partnerCompanyNamesAndRedistributedCampaignsCountFilterQuery;

	@Value("${dashboard.dealBubbleChartQuery}")
	private String dealBubbleChartQuery;

	@Value("${dashboard.leadBubbleChartQuery}")
	private String leadBubbleChartQuery;

	@Value("${dashboard.dealBubbleChartFilterQuery}")
	private String dealBubbleChartFilterQuery;

	@Value("${dashboard.leadBubbleChartFilterQuery}")
	private String leadlBubbleChartFilterQuery;

	/***** Funel chart data ****/
	@Value("${dashboard.vendorContactsQuery}")
	private String funnelvendorContactsQuery;

	@Value("${dashboard.orgAdminContactsQuery}")
	private String funnelOrgAdminQuery;

	@Value("${dashboard.partnerContactsQuery}")
	private String funnelpartnerContactQuery;
	/**** Leads ****/

	@Value("${dashboard.vendorLeadsQueryForOwnVanityLogin}")
	private String vendorLeadsQueryForOwnVanityLogin;

	@Value("${dashboard.vendorDealsForOwnVanityLogin}")
	private String vendorDealsForOwnVanityLogin;

	@Value("${dashboard.vendorDealsWonForOwnVanityLogin}")
	private String vendorDealsWonForOwnVanityLogin;

	@Value("${dashboard.leadsQueryForVendorVanityLogin}")
	private String leadsQueryForVendorVanityLogin;

	@Value("${dashboard.dealsWonQueryForVendorVanityLogin}")
	private String dealsWonQueryForVendorVanityLogin;

	@Value("${dashboard.funnelPartnerLeadsQuery}")
	private String funnelPartnerLeadsQuery;
	/**** Deals *****/

	@Value("${dashboard.partnerDealsQuery}")
	private String partnerDealsQuery;

	/** Deals Won *****/

	@Value("${dashboard.partnerDealsWon}")
	private String partnerDealsWon;

	@Value("${dashboard.leadQueryForPartnerVanityLogin}")
	private String leadQueryForPartnerVanityLogin;

	@Value("${dashboard.dealsQueryForPartnerVanityLogin}")
	private String dealsQueryForPartnerVanityLogin;

	@Value("${dashboard.dealWonsQueryForPartnerVanityLogin}")
	private String dealWonsQueryForPartnerVanityLogin;

	/****************** Pie chart reports **********************/

	/******** Vendor Leads Pie **************/

	@Value("${dashboard.vendorLeadsWonPieChart}")
	private String vendorLeadsWonPieChart;

	@Value("${dashboard.vendorLeadsLostPieChart}")
	private String vendorLeadsLostPieChart;

	@Value("${dashboard.vendorLeadsConvertedPieChart}")
	private String vendorLeadsConvertedPieChart;

	@Value("${dashboard.vendorLeadsWonForOwnVanityLoginPieChart}")
	private String vendorLeadsWonForOwnVanityLoginPieChart;

	@Value("${dashboard.leadsWonQueryForVendorVanityLoginOfPieChart}")
	private String leadsWonQueryForVendorVanityLoginOfPieChart;

	@Value("${dashboard.leadsLostQueryForVendorVanityLoginOfPieChart}")
	private String leadsLostQueryForVendorVanityLoginOfPieChart;

	@Value("${dashboard.leadsConvertedQueryForVendorVanityLoginOfPieChart}")
	private String leadsConvertedQueryForVendorVanityLoginOfPieChart;

	/*********** Vendor Deals Pie Chart ******************/

	@Value("${dashboard.vendorDealsWonPieChart}")
	private String vendorDealsWonPieChart;

	@Value("${dashboard.vendorDealsLostPieChart}")
	private String vendorDealsLostPieChart;

	@Value("${dashboard.dealsWonQueryForVendorVanityLoginOfPieChart}")
	private String dealsWonQueryForVendorVanityLoginOfPieChart;

	@Value("${dashboard.dealsLostQueryForVendorVanityLoginOfPieChart}")
	private String dealsLostQueryForVendorVanityLoginOfPieChart;

	/*********** Partner Deals Pie Chart ***********/

	@Value("${dashboard.partnerPieDealsWonQuery}")
	private String partnerPieDealsWonQuery;

	@Value("${dashboard.partnerPieDealsLostQuery}")
	private String partnerPieDealsLostQuery;

	/********** Convertions for Pie chart **************/

	@Value("${dashboard.vendorLeadToDealConversionPieChart}")
	private String vendorLeadToDealConversionPieChart;

	@Value("${dashboard.vendorLeadToDealQueryForVendorVanityLoginOfPieChart}")
	private String vendorLeadToDealQueryForVendorVanityLoginOfPieChart;

	@Value("${dashboard.vendorLeadToDealConvertion}")
	private String vendorLeadToDealConvertion;

	@Value("${dashboard.vendorLatestLeadCreatedTimePieChart}")
	private String vendorLatestLeadCreatedTimePieChart;

	@Value("${dashboard.VendorandPartnerxamplifyloginLeadstoDealsConversion}")
	private String VendorandPartnerxamplifyloginLeadstoDealsConversion;

	@Value("${dashboard.orgadminandPartnerLatestLeadCreatedTimexamplifylogin}")
	private String orgadminandPartnerLatestLeadCreatedTimexamplifylogin;

	@Value("${dashboard.vendorLatestLeadCreatedQueryForVendorVanityLoginOfPieChart}")
	private String vendorLatestLeadCreatedQueryForVendorVanityLoginOfPieChart;

	@Value("${dashboard.vendorStatusWiseLeadsOfPieChart}")
	private String vendorStatusWiseLeadsOfPieChart;

	@Value("${dashboard.vendorStatuswiseLeadsQueryForVendorVanityLoginOfPieChart}")
	private String vendorStatuswiseLeadsQueryForVendorVanityLoginOfPieChart;

	@Value("${dashboard.vendorDealOpportunityAmountOfPieChart}")
	private String vendorDealOpportunityAmountOfPieChart;

	@Value("${dashboard.vendorDealLatestCreatedTimeOfPieChart}")
	private String vendorDealLatestCreatedTimeOfPieChart;

	/***** Conversion for partner ************/
	@Value("${dashboard.partnerContactToLeadConversionForPieChart}")
	private String partnerContactToLeadConversionForPieChart;

	@Value("${dashboard.partnerLeadToDealConversionForPieChart}")
	private String partnerLeadToDealConversionForPieChart;

	@Value("${dashboard.partnerLatestLeadCreatedTimeOfPieChart}")
	private String partnerLatestLeadCreatedTimeOfPieChart;

	@Value("${dashboard.partnerDealOpportunityAmountOfPieChart}")
	private String partnerDealOpportunityAmountOfPieChart;

	@Value("${dashboard.patnerDealOpportunityAmountQueryForPartnerVanityLoginOfPieChart}")
	private String patnerDealOpportunityAmountQueryForPartnerVanityLoginOfPieChart;

	@Value("${dashboard.partnerLatestDealCreatedTimeOfPieChart}")
	private String partnerLatestDealCreatedTimeOfPieChart;

	@Value("${dashboard.partnerLatestDealCreatedTimeQueryForOwnVanityLoginOfPieChart}")
	private String partnerLatestDealCreatedTimeQueryForOwnVanityLoginOfPieChart;

	@Value("${dashboard.partnerLatestDealCreatedTimeQueryForPartnerVanityLoginOfPieChart}")
	private String partnerLatestDealCreatedTimeQueryForPartnerVanityLoginOfPieChart;

	@Value("${dashboard.vendorStatusWiseDealsOfPiehart}")
	private String vendorStatusWiseDealsOfPiehart;

	@Value("${dashboard.vendorStatuswiseDealQueryForVendorVanityLoginOfPieChart}")
	private String vendorStatuswiseDealQueryForVendorVanityLoginOfPieChart;

	@Value("${dashboard.partnerStatusWiseDealsOfPiehart}")
	private String partnerStatusWiseDealsOfPiehart;

	@Value("${dashboard.partnerStatusWiseLeadsOfPieChart}")
	private String partnerStatusWiseLeadsOfPieChart;

	/*********** TeamMember *********/

	@Value("${dashboard.teamMemberLeadsQuery}")
	private String teamMemberLeadsQuery;

	@Value("${dashboard.teamMemberDealQuery}")
	private String teamMemberDealQuery;

	@Value("${dashboard.teamMemberDealWonQuery}")
	private String teamMemberDealWonQuery;

	/***** Pie chart teamMember ********/
	/******* Leads ************/
	@Value("${dashboard.leadQueryForVedorTeamMemberOfPieChart}")
	private String leadQueryForVedorTeamMemberOfPieChart;

	@Value("${dashboard.leadWonQueryForVedorTeamMemberOfPieChart}")
	private String leadWonQueryForVedorTeamMemberOfPieChart;

	@Value("${dashboard.leadLostQueryForVedorTeamMemberOfPieChart}")
	private String leadLostQueryForVedorTeamMemberOfPieChart;

	@Value("${dashboard.leadCovertedQueryForVedorTeamMemberOfPieChart}")
	private String leadCovertedQueryForVedorTeamMemberOfPieChart;

	/********* Deals ***********/

	@Value("${dashboard.dealWonQueryForVedorTeamMemberOfPieChart}")
	private String dealWonQueryForVedorTeamMemberOfPieChart;

	@Value("${dashboard.dealLostQueryForVedorTeamMemberOfPieChart}")
	private String dealLostQueryForVedorTeamMemberOfPieChart;

	@Value("${dashboard.vendorStatusWiseDealForTeamMember}")
	private String vendorStatusWiseDealForTeamMember;

	/*********** OrgAdmin Leads for xAmplify Login / Own Vanity Login ***********/
	@Value("${dashboard.orgAdminLeadsforxAmplifyLoginOrOwnVanityLogin}")
	private String orgAdminLeadsforxAmplifyLoginOrOwnVanityLogin;

	@Value("${dashboard.orgAdminDealsforxAmplifyLoginOrOwnVanityLogin}")
	private String orgAdminDealsforxAmplifyLoginOrOwnVanityLogin;

	@Value("${dashboard.orgAdminDealsWonforxAmplifyLoginOrOwnVanityLogin}")
	private String orgAdminDealsWonforxAmplifyLoginOrOwnVanityLogin;

	/****** vendorVanity ******/
	@Value("${dashboard.vendorAndPartnerWithVendorVantityLogInRecipients}")
	private String vendorAndPartnerWithVendorVantityLogInRecipients;

	@Value("${dashboard.vendorAndPartnerWithVendorVantityLogInLeads}")
	private String vendorAndPartnerWithVendorVantityLogInLeads;

	@Value("${dashboard.vendorAndPartnerWithVendorVantityLogInDeals}")
	private String vendorAndPartnerWithVendorVantityLogInDeals;

	@Value("${dashboard.vendorAndPartnerWithVendorVantityLogInDealsWon}")
	private String vendorAndPartnerWithVendorVantityLogInDealsWon;

	/*********** Prm ***************/
	@Value("${dashboard.prmLeadsOfFunelChart}")
	private String prmLeadsOfFunelChart;

	@Value("${dashboard.prmDealsOfFunelChart}")
	private String prmDealsOfFunelChart;

	@Value("${dashboard.prmDealsWonOfFunelChart}")
	private String prmDealsWonOfFunelChart;

	/******
	 * Prm And Partner with If parntership established With vendor/orgadmin
	 * xamplifylogin
	 *********/
	@Value("${dashboard.prmAndPartnerwithIfparntershipestablishedWithvendorOrorgadminRecipientsofXamplifyLogin}")
	private String prmAndPartnerwithIfparntershipestablishedWithvendorOrorgadminRecipientsofXamplifyLogin;

	@Value("${dashboard.prmAndPartnerwithIfparntershipestablishedWithvendorOrorgadminLeadsofXamplifyLogin}")
	private String prmAndPartnerwithIfparntershipestablishedWithvendorOrorgadminLeadsofXamplifyLogin;

	@Value("${dashboard.prmAndPartnerwithIfparntershipestablishedWithvendorOrorgadminDealsofXamplifyLogin}")
	private String prmAndPartnerwithIfparntershipestablishedWithvendorOrorgadminDealsofXamplifyLogin;

	@Value("${dashboard.prmAndPartnerwithIfparntershipestablishedWithvendorOrorgadminDealsWonofXamplifyLogin}")
	private String prmAndPartnerwithIfparntershipestablishedWithvendorOrorgadminDealsWonofXamplifyLogin;

	/******
	 * Prm And Partner with If parntership established With vendor/orgadmin own
	 * vanitylogin
	 *********/

	@Value("${dashboard.prmAndPartnerwithIfparntershipestablishedWithvendorOrorgadminLeadsofOwnvanityLogin}")
	private String prmAndPartnerwithIfparntershipestablishedWithvendorOrorgadminLeadsofOwnvanityLogin;

	@Value("${dashboard.prmAndPartnerwithIfparntershipestablishedWithvendorOrorgadminDealsofOwnvanityLogin}")
	private String prmAndPartnerwithIfparntershipestablishedWithvendorOrorgadminDealsofOwnvanityLogin;

	@Value("${dashboard.prmAndPartnerwithIfparntershipestablishedWithvendorOrorgadminDealsWonofOwnvanityLogin}")
	private String prmAndPartnerwithIfparntershipestablishedWithvendorOrorgadminDealsWonofOwnvanityLogin;
	/********* prm established orgadmin / vendor vendor vanity login *****/
	@Value("${dashboard.prmAndPartnerPartnershipEstablishedWithVendorOrOrgAdminRecipientVendorVanity}")
	private String prmAndPartnerPartnershipEstablishedWithVendorOrOrgAdminRecipientVendorVanity;

	@Value("${dashboard.prmAndPartnerPartnershipEstablishedWithVendorOrOrgAdminLeadsVendorVanity}")
	private String prmAndPartnerPartnershipEstablishedWithVendorOrOrgAdminLeadsVendorVanity;

	@Value("${dashboard.prmAndPartnerPartnershipEstablishedWithVendorOrOrgAdminDealsVendorVanity}")
	private String prmAndPartnerPartnershipEstablishedWithVendorOrOrgAdminDealsVendorVanity;

	@Value("${dashboard.prmAndPartnerPartnershipEstablishedWithVendorOrOrgAdminDealswonVendorVanity}")
	private String prmAndPartnerPartnershipEstablishedWithVendorOrOrgAdminDealswonVendorVanity;
	/*********** prm established *********/
	@Value("${dashboard.PartnerPartnershipWithPrmXamplifyLoginllead}")
	private String PartnerPartnershipWithPrmXamplifyLoginllead;

	@Value("${dashboard.PartnerPartnershipWithPrmXamplifyLoginDeal}")
	private String PartnerPartnershipWithPrmXamplifyLoginDeal;

	@Value("${dashboard.PartnerPartnershipWithPrmXamplifyLoginlDealWon}")
	private String PartnerPartnershipWithPrmXamplifyLoginlDealWon;

	/************ PrmPartner ownvanity **********/
	@Value("${dashboard.prmAndPartnerEstablishedWithPrmOwnVanityLoginleads}")
	private String prmAndPartnerEstablishedWithPrmOwnVanityLoginleads;

	@Value("${dashboard.prmAndPartnerEstablishedWithPrmOwnVanityLoginDeals}")
	private String prmAndPartnerEstablishedWithPrmOwnVanityLoginDeals;

	@Value("${dashboard.prmAndPartnerEstablishedWithPrmOwnVanityLoginDealsWon}")
	private String prmAndPartnerEstablishedWithPrmOwnVanityLoginDealsWon;

	/******* vendor vanity **********/
	@Value("${dashboard.prmAndPartnerEstablishedWithPrmVendorVanityLoginLeads}")
	private String prmAndPartnerEstablishedWithPrmVendorVanityLoginLeads;

	@Value("${dashboard.prmAndPartnerEstablishedWithPrmVendorVanityLoginDeals}")
	private String prmAndPartnerEstablishedWithPrmVendorVanityLoginDeals;

	@Value("${dashboard.prmAndPartnerEstablishedWithPrmVendorVanityLoginDealsWon}")
	private String prmAndPartnerEstablishedWithPrmVendorVanityLoginDealsWon;

	/********** partner vanity ***********/
	@Value("${dashboard.partnerRecipientVendorVanityLogin}")
	private String partnerRecipientVendorVanityLogin;

	/******** PIe OrgadminAnd partner Leads *************/
	@Value("${dashboard.orgAdminAndPartnerLeadWonPiechartxamplifylogin}")
	private String orgAdminAndPartnerLeadWonPiechartxamplifylogin;

	@Value("${dashboard.orgAdminAndPartnerLeadLostPiechartxamplifylogin}")
	private String orgAdminAndPartnerLeadLostPiechartxamplifylogin;

	@Value("${dashboard.orgAdminAndPartnerLeadConvertedPiechartxamplifylogin}")
	private String orgAdminAndPartnerLeadConvertedPiechartxamplifylogin;

	/************ Partnber Lead *************/
	@Value("${dashboard.partnerXamplifyLogInLeadWon}")
	private String partnerXamplifyLogInLeadWon;

	@Value("${dashboard.partnerXamplifyLogInLeadsLost}")
	private String partnerXamplifyLogInLeadsLost;

	@Value("${dashboard.partnerXamplifyLogInLeadsConvertd}")
	private String partnerXamplifyLogInLeadsConvertd;

	/********* 8withpartner deal *********/

	@Value("${dashboard.dealsLostForAdminAndPartner}")
	private String dealsLostForAdminAndPartner;

	@Value("${dashboard.dealsWonForAdminAndPartner}")
	private String dealsWonForAdminAndPartner;

	/************** partner ***********/

	@Value("${dashboard.partnerDealsForXamplifyLogIn}")
	private String partnerDealsForXamplifyLogIn;

	@Value("${dashboard.partnerDealsWonForXamplifyLogIn}")
	private String partnerDealsWonForXamplifyLogIn;

	@Value("${dashboard.patnerLeadToDealQueryForPartnerVanityLoginOfPieChart}")
	private String patnerLeadToDealQueryForPartnerVanityLoginOfPieChart;

	@Value("${dashboard.partnerVendorVanityForContacttoLeadcoversion}")
	private String partnerVendorVanityForContacttoLeadcoversion;

	@Value("${dashboard.partnetTmXamplifyLogIn}")
	private String partnetTmXamplifyLogIn;

	/**************** Teammembers query ******************/
	@Value("${dashboard.teamMemberRecipientsQuery}")
	private String teamMemberRecipientsQuery;

	@Value("${dashboard.teamMemberDealOpportunityAmount}")
	private String teamMemberDealOpportunityAmount;

	@Value("${dashboard.teamMemberleadtodealConverstion}")
	private String teamMemberleadtodealConverstion;

	@Value("${dashboard.teammembercreatedDealtime}")
	private String teammembercreatedDealtime;

	@Value("${dashboard.teammemberlatestCreatedLeadTime}")
	private String teammemberlatestCreatedLeadTime;

	@Value("${dashboard.teammembercontacttoleadconvertion}")
	private String teammembercontacttoleadconvertion;

	@Value("${dashboard.teammemberLeadsStatusWiseLeads}")
	private String teammemberLeadsStatusWiseLeads;

	@Value("${dashboard.teammemberstagesWiseDeals}")
	private String teammemberstagesWiseDeals;

	/******* LogIn As Partner Queries *********/
	// orgAdminandPartner
	@Value("${dashboard.logInAsPartnerOrgAdminandPartnerRecipient}")
	private String logInAsPartnerOrgAdminandPartnerRecipient;
	@Value("${dashboard.logInAsPartnerOrgAdminandPartnerLeads}")
	private String logInAsPartnerOrgAdminandPartnerLeads;
	@Value("${dashboard.logInAsPartnerOrgAdminandPartnerDeals}")
	private String logInAsPartnerOrgAdminandPartnerDeals;
	@Value("${dashboard.logInAsPartnerOrgAdminandPartnerDealsWon}")
	private String logInAsPartnerOrgAdminandPartnerDealsWon;
	// vendorAndPartner
	@Value("${dashboard.logInAsPartnerasVendorandPartnerRecipients}")
	private String logInAsPartnerasVendorandPartnerRecipients;

	@Value("${dashboard.logInAsPartnerasVendorandPartnerLeads}")
	private String logInAsPartnerasVendorandPartnerLeads;

	@Value("${dashboard.logInAsPartnerasVendorandPartnerDeals}")
	private String logInAsPartnerasVendorandPartnerDeals;

	@Value("${dashboard.logInAsPartnerasVendorandPartnerDealsWon}")
	private String logInAsPartnerasVendorandPartnerDealsWon;

	// MArketing AndPartner
	@Value("${dashboard.logInAsPartnerasMarketingandPartnerRecipients}")
	private String logInAsPartnerasMarketingandPartnerRecipients;

	@Value("${dashboard.logInAsPartnerasMarketingandPartnerLeads}")
	private String logInAsPartnerasMarketingandPartnerLeads;

	@Value("${dashboard.logInAsPartnerasMarketingandPartnerDeals}")
	private String logInAsPartnerasMarketingandPartnerDeals;

	@Value("${dashboard.logInAsPartnerasMarketingandPartnerDealsWon}")
	private String logInAsPartnerasMarketingandPartnerDealsWon;

	// Prm AndPartner
	@Value("${dashboard.logInAsPartnerasPRMandPartnerRecipients}")
	private String logInAsPartnerasPRMandPartnerRecipients;

	@Value("${dashboard.logInAsPartnerasPRMandPartnerLeads}")
	private String logInAsPartnerasPRMandPartnerLeads;

	@Value("${dashboard.logInAsPartnerasPRMandPartnerDeals}")
	private String logInAsPartnerasPRMandPartnerDeals;

	@Value("${dashboard.logInAsPartnerasPRMandPartnerDealsWon}")
	private String logInAsPartnerasPRMandPartnerDealsWon;

	@Value("${dashboard.logInAsPartnerasWonLeadspie}")
	private String logInAsPartnerasWonLeadspie;

	@Value("${dashboard.logInAsPartnerasLostLeadspie}")
	private String logInAsPartnerasLostLeadspie;

	@Value("${dashboard.logInAsPartnerasConvertedLeadspie}")
	private String logInAsPartnerasConvertedLeadspie;

	// Pie Chart Deals
	@Value("${dashboard.logInAsPartnerasDealsLostpie}")
	private String logInAsPartnerasDealsLostpie;

	@Value("${dashboard.logInAsPartnerasDealsWonpie}")
	private String logInAsPartnerasDealsWonpie;

	// Leads StageNames
	@Value("${dashboard.logInAsPartnerStageswiseLeads}")
	private String logInAsPartnerStageswiseLeads;
	// Deals StageNames
	@Value("${dashboard.logInAsPartnerStageswiseDeals}")
	private String logInAsPartnerStageswiseDeals;

	// LatestLeadCreated
	@Value("${dashboard.logInAsPartnerLeadCreatedTime}")
	private String logInAsPartnerLeadCreatedTime;
	// LatestDealCreated
	@Value("${dashboard.logInAsPartnerDealCreatedTime}")
	private String logInAsPartnerDealCreatedTime;

	// Contact To LEad
	@Value("${dashboard.logInAsPartnerLeadstodealConversion}")
	private String logInAsPartnerLeadstodealConversion;

	// Contact To Lead
	@Value("${dashboard.logInAsPartnerOrgAdminandPartnerContactstoleadsConversion}")
	private String logInAsPartnerOrgAdminandPartnerContactstoleadsConversion;
	@Value("${dashboard.logInAsPartnerVendorandPartnerContactstoleadsConversion}")
	private String logInAsPartnerVendorandPartnerContactstoleadsConversion;
	@Value("${dashboard.logInAsPartnerMarketingandPartnerContactstoleadsConversion}")
	private String logInAsPartnerMarketingandPartnerContactstoleadsConversion;
	@Value("${dashboard.logInAsPartnerPRMandPartnerContactstoleadsConversion}")
	private String logInAsPartnerPRMandPartnerContactstoleadsConversion;

	// Opportinity Amount
	@Value("${dashboard.logInAsPartnerOpportunityAmount}")
	private String logInAsPartnerOpportunityAmount;

	// PRM AS ADMIN CONTACT TO LEAD RATIO
	@Value("${dashboard.logInAsPartnerOrgAdminandPartnerPRMasAdminContacttoLead}")
	private String logInAsPartnerOrgAdminandPartnerPRMasAdminContacttoLead;

	@Value("${dashboard.logInAsPartnerVendorandPartnerPRMasAdminContacttoLead}")
	private String logInAsPartnerVendorandPartnerPRMasAdminContacttoLead;

	@Value("${dashboard.logInAsPartnerMarketingandPartnerPRMasAdminContacttoLead}")
	private String logInAsPartnerMarketingandPartnerPRMasAdminContacttoLead;

	@Autowired
	private HibernateSQLQueryResultUtilDao hibernateSQLQueryResultUtilDao;

	@Value("${instantNavigationLinks.vendor.dam.query}")
	private String instantNavigationLinksVendorDamQuery;

	@Value("${instantNavigationLinks.vendor.track.query}")
	private String instantNavigationLinksVendorTrackQuery;

	@Value("${instantNavigationLinks.vendor.playbook.query}")
	private String instantNavigationLinksVendorPlaybookQuery;

	@Value("${instantNavigationLinks.partner.dam.query}")
	private String instantNavigationLinksPartnerDamQuery;

	@Value("${instantNavigationLinks.partner.track.query}")
	private String instantNavigationLinksPartnerTrackQuery;

	@Value("${instantNavigationLinks.partner.playbook.query}")
	private String instantNavigationLinksPartnerPlaybookQuery;

	@Value("${instantNavigationLinks.vendor.order.query}")
	private String instantNavigationLinksVendorOrderQuery;

	@Value("${instantNavigationLinks.partner.order.query}")
	private String instantNavigationLinksPartnerOrderQuery;

	@Value("${universalsearch.track.query}")
	private String universalSearchTrackQuery;

	@Value("${universalsearch.dam.query}")
	private String universalSearchDamQuery;

	@Value("${universalsearch.playbook.query}")
	private String universalSearchPlaybookQuery;

	@Value("${universalsearch.leads.query}")
	private String universalSearchLeadsQuery;

	@Value("${universalsearch.deals.query}")
	private String universalSearchDealsQuery;

	@Value("${universalsearch.partner.vanity.query}")
	private String universalSearchPartnerVanityQuery;
	/**** XNFR-792 ***/
	@Value("${universalsearch.onboardpartners.query}")
	private String universalSearchOnboardPartnersQuery;

	@Value("${dashboard.OrgAdminandPartnerwithVendorVanityLoginContacttodealconverstion}")
	private String OrgAdminandPartnerwithVendorVanityLoginContacttodealconverstion;

	@Value("${dashboard.OrgAdminandPartnerDealStatuswiseDealsxamplifylogin}")
	private String OrgAdminandPartnerDealStatuswiseDealsxamplifylogin;

	@Value("${dashboard.OrgadminandPartnerownvanityLeadtoDealConversion}")
	private String OrgadminandPartnerownvanityLeadtoDealConversion;

	@Value("${dashboard.VendorandPartnerxamplifyloginDealOpportunityAmount}")
	private String VendorandPartnerxamplifyloginDealOpportunityAmount;

	/**** XNFR-792 ***/
	@Autowired
	private PartnershipDAO partnershipDao;
	/**** XNFR-792 ***/
	@Autowired
	private PartnerAnalyticsDAO partnerAnalyticsDAO;

	/**** XNFR-792 ***/

	@Override
	public DashboardModuleAnalyticsView getDashboardModuleViewByCompanyId(VanityUrlDetailsDTO dto) {
		Integer loggedInUserId = dto.getUserId();
		Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(DashboardModuleAnalyticsView.class);
		criteria.add(Restrictions.eq(COMPANY_ID, companyId));
		return (DashboardModuleAnalyticsView) criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)
				.uniqueResult();

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<VendorActivityView> getVendorActivityViewByCompanyId(VanityUrlDetailsDTO dto) {
		Integer loggedInUserId = dto.getUserId();
		Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
		if (loggedInUserId != null && loggedInUserId > 0 && companyId != null && companyId > 0) {
			Session session = sessionFactory.getCurrentSession();
			Criteria criteria = session.createCriteria(VendorActivityView.class);
			criteria.add(Restrictions.eq(COMPANY_ID, companyId));
			return criteria.list();
		} else {
			return Collections.emptyList();
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<VendorActivityVanityUrlView> getVendorActivityVanityUrlViewByCompanyId(VanityUrlDetailsDTO dto) {
		boolean loginAsPartner = XamplifyUtils.isLoginAsPartner(dto.getLoginAsUserId());
		Integer vendorCompanyId = 0;
		if (loginAsPartner) {
			vendorCompanyId = userDao.getCompanyIdByUserId(dto.getLoginAsUserId());
		} else {
			vendorCompanyId = userDao.getCompanyIdByProfileName(dto.getVendorCompanyProfileName());
		}
		Integer loggedInUserId = dto.getUserId();
		Integer partnerCompanyId = userDao.getCompanyIdByUserId(loggedInUserId);
		if (loggedInUserId != null && loggedInUserId > 0 && partnerCompanyId != null && partnerCompanyId > 0
				&& vendorCompanyId != null && vendorCompanyId > 0) {
			Session session = sessionFactory.getCurrentSession();
			Criteria criteria = session.createCriteria(VendorActivityVanityUrlView.class);
			criteria.add(Restrictions.eq(PARTNER_COMPANY_ID, partnerCompanyId));
			criteria.add(Restrictions.eq("vendorCompanyId", vendorCompanyId));
			return criteria.list();
		} else {
			return Collections.emptyList();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> listVendorCompanyDetailsByPartnerCompanyId(Integer partnerCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		String sqlQueryString = " select cp.company_name,cp.company_profile_name, p.vendor_company_id from xt_partnership p,xt_company_profile cp "
				+ " where p.partner_company_id = :partnerCompanyId and p.status  ='approved' and cp.company_id = p.vendor_company_id ";
		SQLQuery query = session.createSQLQuery(sqlQueryString);
		query.setParameter(PARTNER_COMPANY_ID, partnerCompanyId);
		return query.list();
	}

	@Override
	public EmailStatsView getEmailStats(Integer userId) {
		Session session = sessionFactory.getCurrentSession();
		Integer companyId = userDao.getCompanyIdByUserId(userId);
		Criteria criteria = session.createCriteria(EmailStatsView.class);
		criteria.add(Restrictions.eq(COMPANY_ID, companyId));
		return (EmailStatsView) criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)
				.uniqueResult();

	}

	@Override
	public VendorEmailStatsView getVendorEmailStats(Integer loggedInUserId) {
		Session session = sessionFactory.getCurrentSession();
		Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
		Criteria criteria = session.createCriteria(VendorEmailStatsView.class);
		criteria.add(Restrictions.eq(COMPANY_ID, companyId));
		VendorEmailStatsView vendorEmailStatsView = (VendorEmailStatsView) criteria
				.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).uniqueResult();
		if (vendorEmailStatsView != null) {
			return vendorEmailStatsView;
		} else {
			BigInteger bigInteger = BigInteger.valueOf(0);
			VendorEmailStatsView emptyVendorEmailStatsView = new VendorEmailStatsView();
			emptyVendorEmailStatsView.setOpened(bigInteger);
			emptyVendorEmailStatsView.setClicked(bigInteger);
			emptyVendorEmailStatsView.setViews(bigInteger);
			return emptyVendorEmailStatsView;
		}

	}

	@Override
	public EmailStatsVanityUrlView getEmailStatsForVanityUrl(VanityUrlDetailsDTO dto) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(EmailStatsVanityUrlView.class);
		criteria.add(Restrictions.eq(PARTNER_COMPANY_ID, dto.getLoggedInUserCompanyId()));
		criteria.add(Restrictions.eq("vendorCompanyId", dto.getVendorCompanyId()));
		return (EmailStatsVanityUrlView) criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)
				.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<RegionalStatisticsView> listRegionalStatisticsViewsByCompanyId(VanityUrlDetailsDTO dto) {
		Session session = sessionFactory.getCurrentSession();
		Integer companyId = userDao.getCompanyIdByUserId(dto.getUserId());
		Criteria criteria = session.createCriteria(RegionalStatisticsView.class);
		criteria.add(Restrictions.eq(COMPANY_ID, companyId));
		return criteria.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<VendorRegionalStatisticsView> listVendorRegionalStatisticsViewsBy(VanityUrlDetailsDTO dto) {
		Session session = sessionFactory.getCurrentSession();
		Integer companyId = userDao.getCompanyIdByUserId(dto.getUserId());
		Criteria criteria = session.createCriteria(VendorRegionalStatisticsView.class);
		criteria.add(Restrictions.eq(COMPANY_ID, companyId));
		return criteria.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<RegionalStatisticsVanityUrlView> listRegionalStatisticsVanityUrlViewsByCompanyId(
			VanityUrlDetailsDTO dto) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(RegionalStatisticsVanityUrlView.class);
		criteria.add(Restrictions.eq(PARTNER_COMPANY_ID, dto.getLoggedInUserCompanyId()));
		criteria.add(Restrictions.eq(COMPANY_ID, dto.getVendorCompanyId()));
		return criteria.list();
	}

	@Override
	public OpportunitiesVendorAnalyticsView getOpportunitiesVendorAnalyticsByCompanyId(Integer userId) {
		Session session = sessionFactory.getCurrentSession();
		Integer companyId = userDao.getCompanyIdByUserId(userId);
		Criteria criteria = session.createCriteria(OpportunitiesVendorAnalyticsView.class);
		criteria.add(Restrictions.eq(COMPANY_ID, companyId));
		return (OpportunitiesVendorAnalyticsView) criteria
				.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).uniqueResult();
	}

	@Override
	public OpportunitiesPartnerAnalyticsView getOpportunitiePartnerAnalyticsByCompanyId(VanityUrlDetailsDTO dto) {
		Session session = sessionFactory.getCurrentSession();
		Integer companyId = userDao.getCompanyIdByUserId(dto.getUserId());
		Criteria criteria = session.createCriteria(OpportunitiesPartnerAnalyticsView.class);
		criteria.add(Restrictions.eq(COMPANY_ID, companyId));
		return (OpportunitiesPartnerAnalyticsView) criteria
				.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).uniqueResult();
	}

	@Override
	public OpportunitiesVanityUrlPartnerAnalyticsView getOpportunitiesVanityUrlPartnerAnalytics(
			VanityUrlDetailsDTO dto) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(OpportunitiesVanityUrlPartnerAnalyticsView.class);
		criteria.add(Restrictions.eq(COMPANY_ID, dto.getLoggedInUserCompanyId()));
		criteria.add(Restrictions.eq("vendorOrgainzationId", dto.getVendorCompanyId()));
		return (OpportunitiesVanityUrlPartnerAnalyticsView) criteria
				.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getDashboardWorldMapDetailReport(List<Integer> userIdList, Integer pageSize,
			Integer pageNumber, VanityUrlDetailsDTO dto, String countryCode) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			Session session = sessionFactory.getCurrentSession();
			String sql = " select x.firstname as \"firstName\", x.lastname as \"lastName\", x.email_id as \"emailId\", min(x.start_time) as \"time\", "
					+ " x.device_type as \"deviceType\", x.city as \"city\", x.state as \"state\", x.country as \"country\" , x.os as  \"os\"       "
					+ "  from v_xt_xtremand_log x, xt_user_profile up, xt_campaign c "
					+ " where up.user_id=x.user_id and x.campaign_id= c.campaign_id and c.customer_id in (:userIds) and x.country_code= '"
					+ countryCode + "' " + " and x.country_code IS NOT NULL  ";
			if ((dto.isVanityUrlFilter()
					&& (dto.getLoggedInUserCompanyId().intValue() != dto.getVendorCompanyId().intValue()))
					|| (dto.getLoginAsUserId() != null && dto.getLoginAsUserId() > 0)) {
				sql = sql + " and c.vendor_organization_id=" + dto.getVendorCompanyId()
						+ " and c.is_nurture_campaign=true and c.parent_campaign_id is not null";
			} else if ((!dto.isVanityUrlFilter())) {
				//
			} else if ((dto.isVanityUrlFilter()
					&& dto.getLoggedInUserCompanyId().intValue() == dto.getVendorCompanyId().intValue())) {
				sql = sql + " and c.is_launched = true and c.is_nurture_campaign=false ";
			}
			sql = sql
					+ " group by x.firstname,x.lastname,x.email_id,x.city,x.state,x.country,x.user_id,x.device_type,x.os ";
			SQLQuery query = session.createSQLQuery(sql);
			query.setParameterList("userIds", userIdList);
			ScrollableResults scrollableResults = query.scroll();
			scrollableResults.last();
			Integer totalRecords = scrollableResults.getRowNumber() + 1;
			query.setFirstResult((pageNumber - 1) * pageSize);
			query.setMaxResults(pageSize);
			query.setResultTransformer(Transformers.aliasToBean(EmailLogReport.class));
			List<EmailLogReport> data = query.list();
			resultMap.put("totalRecords", totalRecords);
			resultMap.put("data", data);
		} catch (HibernateException e) {
			e.printStackTrace();
		}
		return resultMap;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getDashboardViewsData(List<Integer> userIdList, Integer daysInterval,
			VanityUrlDetailsDTO dto) {
		if (userIdList.isEmpty()) {
			return Collections.emptyList();
		} else {
			Session session = sessionFactory.getCurrentSession();
			String sql = "select (SELECT COUNT(DISTINCT  x.session_id )) +  (SUM(CASE WHEN x.action_id = 10 THEN 1 ELSE 0 END)) AS views, "
					+ " CAST(TRUNC(EXTRACT(DAY FROM   x.start_time )) AS INTEGER) AS  dy_start_time, "
					+ " to_date(to_char(x.start_time, 'YYYY/MM/DD'), 'YYYY/MM/DD') AS  tdy_start_time FROM  xt_xtremand_log x inner join xt_campaign c "
					+ " ON (x.campaign_id = c.campaign_id) inner join xt_user_profile p on (c.customer_id=p.user_id) WHERE "
					+ " ((  to_date(to_char(x.start_time,'YYYY-MM-DD'),'YYYY-MM-DD')  <= CURRENT_DATE) "
					+ " AND ( to_date(to_char(x.start_time,'YYYY-MM-DD'),'YYYY-MM-DD')  >= (CURRENT_DATE - INTERVAL '"
					+ daysInterval + " DAY'))) and  c.customer_id in (:userIds) ";
			if ((dto.isVanityUrlFilter()
					&& (dto.getLoggedInUserCompanyId().intValue() != dto.getVendorCompanyId().intValue()))
					|| (dto.getLoginAsUserId() != null && dto.getLoginAsUserId() > 0)) {
				sql = sql + " and c.vendor_organization_id=" + dto.getVendorCompanyId()
						+ " and c.is_nurture_campaign=true and c.parent_campaign_id is not null";
			} else if ((!dto.isVanityUrlFilter())) {
				//
			} else if ((dto.isVanityUrlFilter()
					&& dto.getLoggedInUserCompanyId().intValue() == dto.getVendorCompanyId().intValue())
					|| (!dto.isVanityUrlFilter()
							&& dto.getLoggedInUserCompanyId().intValue() == dto.getVendorCompanyId().intValue())) {
				sql = sql + " and c.is_launched = true and is_nurture_campaign=false ";
			}

			sql = sql + " GROUP BY 2, 3 order by 3 desc ";
			SQLQuery query = session.createSQLQuery(sql);
			query.setParameterList("userIds", userIdList);
			return query.list();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getDashboardMinutesWatchedData(List<Integer> userIdList, Integer daysInterval,
			VanityUrlDetailsDTO dto) {
		if (userIdList.isEmpty()) {
			return Collections.emptyList();
		} else {
			Session session = sessionFactory.getCurrentSession();
			String sql = "SELECT round(cast((sum(extract(epoch from x.end_time)-extract(epoch from x.start_time))/60) as numeric),2) AS minutes_watched, "
					+ " CAST(TRUNC(EXTRACT(DAY FROM x.start_time)) AS INTEGER) AS dystart_time, to_date(to_char(x.start_time, 'YYYY/MM/DD'), 'YYYY/MM/DD') AS  tdy_start_time "
					+ " FROM xt_xtremand_log x  inner join xt_campaign c ON (x.campaign_id = c.campaign_id) inner join xt_user_profile p on (c.customer_id=p.user_id) "
					+ " WHERE (((to_date(to_char(x.start_time,'YYYY-MM-DD'),'YYYY-MM-DD') <= CURRENT_DATE) AND (to_date(to_char(x.start_time,'YYYY-MM-DD'),'YYYY-MM-DD') >= (CURRENT_DATE - INTERVAL '"
					+ daysInterval + " DAY'))) AND (x.action_id = 1)) and  c.customer_id in (:userIds) ";
			if ((dto.isVanityUrlFilter()
					&& (dto.getLoggedInUserCompanyId().intValue() != dto.getVendorCompanyId().intValue()))
					|| (dto.getLoginAsUserId() != null && dto.getLoginAsUserId() > 0)) {
				sql = sql + " and c.vendor_organization_id=" + dto.getVendorCompanyId()
						+ " and c.is_nurture_campaign=true and c.parent_campaign_id is not null";
			} else if ((!dto.isVanityUrlFilter())) {
				//
			} else if ((dto.isVanityUrlFilter()
					&& dto.getLoggedInUserCompanyId().intValue() == dto.getVendorCompanyId().intValue())
					|| (!dto.isVanityUrlFilter()
							&& dto.getLoggedInUserCompanyId().intValue() == dto.getVendorCompanyId().intValue())) {
				sql = sql + " and c.is_launched = true and is_nurture_campaign=false ";
			}
			sql = sql + " GROUP BY 2, 3 order by 3 desc ";
			SQLQuery query = session.createSQLQuery(sql);
			query.setParameterList("userIds", userIdList);
			return query.list();
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getDashboardAverageDurationData(List<Integer> userIdList, Integer daysInterval,
			VanityUrlDetailsDTO dto) {
		if (userIdList.isEmpty()) {
			return Collections.emptyList();
		} else {
			Session session = sessionFactory.getCurrentSession();
			String sql = "select round(cast((sum(extract(epoch from x.end_time)-extract(epoch from x.start_time))/60)/count(distinct x.video_id) as numeric),2) AS average, "
					+ " CAST(TRUNC(EXTRACT(DAY FROM   x.start_time )) AS INTEGER) AS  dy_start_time, to_date(to_char(x.start_time, 'YYYY/MM/DD'), 'YYYY/MM/DD') AS  tdy_start_time "
					+ " FROM  xt_xtremand_log x inner join xt_campaign c ON (x.campaign_id = c.campaign_id) inner join xt_user_profile p on (c.customer_id=p.user_id) "
					+ " WHERE (((to_date(to_char(x.start_time,'YYYY-MM-DD'),'YYYY-MM-DD') <= CURRENT_DATE) AND (to_date(to_char(x.start_time,'YYYY-MM-DD'),'YYYY-MM-DD') >= (CURRENT_DATE - INTERVAL '"
					+ daysInterval + " DAY'))) AND (x.action_id = 1)) and c.customer_id in (:userIds)";
			if ((dto.isVanityUrlFilter()
					&& (dto.getLoggedInUserCompanyId().intValue() != dto.getVendorCompanyId().intValue()))
					|| (dto.getLoginAsUserId() != null && dto.getLoginAsUserId() > 0)) {
				sql = sql + " and c.vendor_organization_id=" + dto.getVendorCompanyId()
						+ " and c.is_nurture_campaign=true and c.parent_campaign_id is not null";
			} else if ((!dto.isVanityUrlFilter())) {
				//
			} else if ((dto.isVanityUrlFilter()
					&& dto.getLoggedInUserCompanyId().intValue() == dto.getVendorCompanyId().intValue())
					|| (!dto.isVanityUrlFilter()
							&& dto.getLoggedInUserCompanyId().intValue() == dto.getVendorCompanyId().intValue())) {
				sql = sql + " and c.is_launched = true and is_nurture_campaign=false ";
			}
			sql = sql + " GROUP BY 2, 3 order by 3 desc ";
			SQLQuery query = session.createSQLQuery(sql);
			query.setParameterList("userIds", userIdList);
			return query.list();
		}

	}

	@Override
	public PartnerAnalyticsCountDTO getActiveInActiveTotalPartnerCounts(Integer userId, boolean applyFilter) {
		try {
			Session session = sessionFactory.getCurrentSession();
			TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(userId, applyFilter, true);
			boolean applyTeamMemberFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
			if (teamMemberFilterDTO.isEmptyFilter()) {
				PartnerAnalyticsCountDTO partnerAnalyticsCountDTO = new PartnerAnalyticsCountDTO();
				partnerAnalyticsCountDTO.setActivePartners(BigInteger.valueOf(0));
				partnerAnalyticsCountDTO.setInActivePartners(BigInteger.valueOf(0));
				partnerAnalyticsCountDTO.setTotalPartners(BigInteger.valueOf(0));
				return partnerAnalyticsCountDTO;
			} else {
				if (applyTeamMemberFilter) {
					PartnerAnalyticsCountDTO partnerAnalyticsCountDTO = new PartnerAnalyticsCountDTO();
					filterTotalPartners(session, teamMemberFilterDTO.getPartnershipIdsOrPartnerCompanyIds(),
							partnerAnalyticsCountDTO);
					filterActivePartners(userId, session, partnerAnalyticsCountDTO);
					filterInActivePartners(partnerAnalyticsCountDTO);
					return partnerAnalyticsCountDTO;
				} else {
					SQLQuery query = session.createSQLQuery(partnerAnalyticsCountQuery);
					query.setParameter(USER_ID, userId);
					return (PartnerAnalyticsCountDTO) query
							.setResultTransformer(Transformers.aliasToBean(PartnerAnalyticsCountDTO.class))
							.uniqueResult();
				}
			}

		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex);
		}

	}

	private void filterInActivePartners(PartnerAnalyticsCountDTO partnerAnalyticsCountDTO) {
		Integer InActivePartnersCount = partnerAnalyticsCountDTO.getTotalPartners().intValue()
				- partnerAnalyticsCountDTO.getActivePartners().intValue();
		partnerAnalyticsCountDTO.setInActivePartners(BigInteger.valueOf(InActivePartnersCount));
	}

	@SuppressWarnings("unchecked")
	private void filterActivePartners(Integer userId, Session session,
			PartnerAnalyticsCountDTO partnerAnalyticsCountDTO) {
		List<Integer> partnerCompanyIds = utilDao.findPartnerCompanyIdsByLoggedInUserId(userId);
		String activePartnersQueryString = " select distinct u.company_id from xt_campaign c,xt_user_profile u where c.vendor_organization_id = (select company_id from xt_user_profile where user_id = :userId) and c.is_nurture_campaign and c.is_launched and "
				+ " c.customer_id = u.user_id and u.company_id is not null ";
		List<Integer> activePartnerCompanyIds = session.createSQLQuery(activePartnersQueryString)
				.setParameter("userId", userId).list();
		if (activePartnerCompanyIds != null && !activePartnerCompanyIds.isEmpty()) {
			Integer activePartnersCount = XamplifyUtils.findCommonIntegers(partnerCompanyIds, activePartnerCompanyIds)
					.size();
			partnerAnalyticsCountDTO.setActivePartners(BigInteger.valueOf(activePartnersCount));
		} else {
			partnerAnalyticsCountDTO.setActivePartners(BigInteger.valueOf(0));
		}
	}

	private void filterTotalPartners(Session session, List<Integer> partnershipIds,
			PartnerAnalyticsCountDTO partnerAnalyticsCountDTO) {
		String totalPartnersQueryString = "select distinct count(id) from xt_partnership where status = 'approved' and partner_company_id is not null and id in (:partnershipIds)";
		BigInteger totalPartners = (BigInteger) session.createSQLQuery(totalPartnersQueryString)
				.setParameterList("partnershipIds", partnershipIds).uniqueResult();
		partnerAnalyticsCountDTO.setTotalPartners(totalPartners);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<WordCloudMapDTO> findDataForDealBubbleChart(Integer userId, boolean applyFilter) {
		Session session = sessionFactory.getCurrentSession();
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(userId, applyFilter, false);
		boolean applyTeamMemberFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
		if (teamMemberFilterDTO.isEmptyFilter()) {
			return new ArrayList<WordCloudMapDTO>();
		} else {
			if (applyTeamMemberFilter) {
				SQLQuery query = session.createSQLQuery(dealBubbleChartFilterQuery);
				query.setParameter(USER_ID, userId);
				query.setParameterList("partnerCompanyIds", teamMemberFilterDTO.getPartnershipIdsOrPartnerCompanyIds());
				return query.setResultTransformer(Transformers.aliasToBean(WordCloudMapDTO.class)).list();
			} else {
				SQLQuery query = session.createSQLQuery(dealBubbleChartQuery);
				query.setParameter(USER_ID, userId);
				return query.setResultTransformer(Transformers.aliasToBean(WordCloudMapDTO.class)).list();
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<WordCloudMapDTO> findDataForLeadBubbleChart(Integer userId, boolean applyFilter) {
		Session session = sessionFactory.getCurrentSession();
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(userId, applyFilter, false);
		boolean applyTeamMemberFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
		if (teamMemberFilterDTO.isEmptyFilter()) {
			return new ArrayList<WordCloudMapDTO>();
		} else {
			if (applyTeamMemberFilter) {
				SQLQuery query = session.createSQLQuery(leadlBubbleChartFilterQuery);
				query.setParameter(USER_ID, userId);
				query.setParameterList("partnerCompanyIds", teamMemberFilterDTO.getPartnershipIdsOrPartnerCompanyIds());
				return query.setResultTransformer(Transformers.aliasToBean(WordCloudMapDTO.class)).list();
			} else {
				SQLQuery query = session.createSQLQuery(leadBubbleChartQuery);
				query.setParameter(USER_ID, userId);
				return query.setResultTransformer(Transformers.aliasToBean(WordCloudMapDTO.class)).list();
			}
		}

	}

	@Override
	public List<List<Object>> getFunnelChartAnalyticsData(VanityUrlDetailsDTO vanityUrlDetailsDto,
			RoleDisplayDTO roleDisplayDto) {
		try {
			Session session = sessionFactory.getCurrentSession();
			List<List<Object>> funnelChartReports = new ArrayList<List<Object>>();
			Integer userId = vanityUrlDetailsDto.getUserId();
			boolean applyFilter = vanityUrlDetailsDto.isApplyFilter();
			TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(userId, applyFilter, false);
			boolean applyTeamMemberFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
			boolean partnerTeammember = roleDisplayDto.partnerOrPartnerTeamMember();
			boolean isAnyTeamMember = roleDisplayDto.isAdminTeammemberOrAdminAndPartnerTeamMember();
			if (teamMemberFilterDTO.isEmptyFilter()) {
				if (partnerTeammember || vanityUrlDetailsDto.isVanityUrlFilter()) {
					if (vanityUrlDetailsDto.isPartnerLoggedInThroughVanityUrl()) {
						findCountForVendorVanityTeamMember(vanityUrlDetailsDto, session, funnelChartReports,
								roleDisplayDto);
					} else if (vanityUrlDetailsDto.getLoginAsUserId() != null
							&& vanityUrlDetailsDto.getLoginAsUserId() > 0) {
						Integer partnerCompanyId = userDao.getCompanyIdByUserId(vanityUrlDetailsDto.getUserId());
						Integer vendorCompanyId = userDao.getCompanyIdByUserId(vanityUrlDetailsDto.getLoginAsUserId());
						vanityUrlDetailsDto.setLoggedInUserCompanyId(partnerCompanyId);
						vanityUrlDetailsDto.setVendorCompanyId(vendorCompanyId);
						if (roleDisplayDto.partnerOrPartnerTeamMember()) {
							findCountsForVendorVanityLogin(vanityUrlDetailsDto, session, funnelChartReports,
									roleDisplayDto);
						}
					} else {
						findCountForTeamMember(session, funnelChartReports, userId, roleDisplayDto,
								teamMemberFilterDTO);
					}
				} else {
					return new ArrayList<List<Object>>();
				}
			} else {
				if (applyTeamMemberFilter) {
					if (vanityUrlDetailsDto.isPartnerLoggedInThroughVanityUrl()) {
						findCountForVendorVanityTeamMember(vanityUrlDetailsDto, session, funnelChartReports,
								roleDisplayDto);
					} else {
						findCountForTeamMember(session, funnelChartReports, userId, roleDisplayDto,
								teamMemberFilterDTO);
					}
				} else {
					if (vanityUrlDetailsDto.isVendorLoggedInThroughOwnVanityUrl()) {
						findCountsForOwnVanityLogin(session, funnelChartReports, userId, roleDisplayDto);
					}
					/**** LOgin in as Partner ******/

					else if ((vanityUrlDetailsDto.getLoginAsUserId() != null
							&& vanityUrlDetailsDto.getLoginAsUserId() > 0)
							|| (vanityUrlDetailsDto.getLoginAsUserId() != null
									&& vanityUrlDetailsDto.getLoginAsUserId() > 0 && isAnyTeamMember)) {
						Integer partnerCompanyId = userDao.getCompanyIdByUserId(vanityUrlDetailsDto.getUserId());
						Integer vendorCompanyId = userDao.getCompanyIdByUserId(vanityUrlDetailsDto.getLoginAsUserId());
						vanityUrlDetailsDto.setLoggedInUserCompanyId(partnerCompanyId);
						vanityUrlDetailsDto.setVendorCompanyId(vendorCompanyId);
						if (roleDisplayDto.partnerOrPartnerTeamMember()) {
							findCountsForVendorVanityLogin(vanityUrlDetailsDto, session, funnelChartReports,
									roleDisplayDto);
						} else {
							loginAsPartnerRole(vanityUrlDetailsDto, session, funnelChartReports, roleDisplayDto);
						}
					}

					/**** LOgin in as Partner ******/
					else if (vanityUrlDetailsDto.isPartnerLoggedInThroughVanityUrl()) {
						findCountsForVendorVanityLogin(vanityUrlDetailsDto, session, funnelChartReports,
								roleDisplayDto);
					} else {
						findCountForXamplifyLogin(session, funnelChartReports, userId, roleDisplayDto);
					}
				}
			}
			return funnelChartReports;
		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex);
		}
	}

	private void findCountForVendorVanityTeamMember(VanityUrlDetailsDTO vanityUrlDetailsDto, Session session,
			List<List<Object>> funnelChartReports, RoleDisplayDTO roleDisplayDto) {
		boolean isPartnershipEstablishedWithprm = utilDao
				.isPartnershipEstablishedOnlyWithPrm(vanityUrlDetailsDto.getUserId());
		if (isPartnershipEstablishedWithprm) {
			getLeadsCountForVendorVanityLoginAndAddToArray(session, funnelChartReports, vanityUrlDetailsDto,
					leadQueryForPartnerVanityLogin);
			getDealsCountForVendorVanityLoginAndAddToArray(session, funnelChartReports, vanityUrlDetailsDto,
					dealsQueryForPartnerVanityLogin);
			getDealsWonCountForVendorVanityLoginAndAddToArray(session, funnelChartReports, vanityUrlDetailsDto,
					dealWonsQueryForPartnerVanityLogin);
		} else {
			getRecipientCountForVendorVanityLogInAndAddToArray(session, funnelChartReports, vanityUrlDetailsDto,
					partnerRecipientVendorVanityLogin);
			getLeadsCountForVendorVanityLoginAndAddToArray(session, funnelChartReports, vanityUrlDetailsDto,
					leadQueryForPartnerVanityLogin);
			getDealsCountForVendorVanityLoginAndAddToArray(session, funnelChartReports, vanityUrlDetailsDto,
					dealsQueryForPartnerVanityLogin);
			getDealsWonCountForVendorVanityLoginAndAddToArray(session, funnelChartReports, vanityUrlDetailsDto,
					dealWonsQueryForPartnerVanityLogin);
		}

	}

	/********** LOgin AS Partner *********/
	private void loginAsPartnerRole(VanityUrlDetailsDTO vanityUrlDetailsDto, Session session,
			List<List<Object>> funnelChartReports, RoleDisplayDTO roleDisplayDto) {

		if (roleDisplayDto.isPrmAndPartner() || roleDisplayDto.isPrmOrPrmAndPartnerCompany()) {
			boolean isPrmAdmin = utilDao.isPrmByVendorCompanyId(vanityUrlDetailsDto.getVendorCompanyId());
			boolean isPartnerPrm = utilDao.isPrmByVendorCompanyId(vanityUrlDetailsDto.getLoggedInUserCompanyId());
			if (isPrmAdmin && isPartnerPrm) {
				getLeadsCountForVendorVanityLoginAndAddToArray(session, funnelChartReports, vanityUrlDetailsDto,
						logInAsPartnerasPRMandPartnerLeads);
				getDealsCountForVendorVanityLoginAndAddToArray(session, funnelChartReports, vanityUrlDetailsDto,
						logInAsPartnerasPRMandPartnerDeals);
				getDealsWonCountForVendorVanityLoginAndAddToArray(session, funnelChartReports, vanityUrlDetailsDto,
						logInAsPartnerasPRMandPartnerDealsWon);
			} else {
				getRecipientCountForVendorVanityLogInAndAddToArray(session, funnelChartReports, vanityUrlDetailsDto,
						logInAsPartnerasPRMandPartnerRecipients);
				getLeadsCountForVendorVanityLoginAndAddToArray(session, funnelChartReports, vanityUrlDetailsDto,
						logInAsPartnerasPRMandPartnerLeads);
				getDealsCountForVendorVanityLoginAndAddToArray(session, funnelChartReports, vanityUrlDetailsDto,
						logInAsPartnerasPRMandPartnerDeals);
				getDealsWonCountForVendorVanityLoginAndAddToArray(session, funnelChartReports, vanityUrlDetailsDto,
						logInAsPartnerasPRMandPartnerDealsWon);
			}
		}
	}

	/********** LOgin AS Partner *********/
	private void findCountsForVendorVanityLogin(VanityUrlDetailsDTO vanityUrlDetailsDto, Session session,
			List<List<Object>> funnelChartReports, RoleDisplayDTO roleDisplayDto) {
		Integer userId = vanityUrlDetailsDto.getUserId();
		boolean isPrmAndPartner = roleDisplayDto.isPrmOrPrmAndPartnerCompany();
		boolean isPartnershipEstablishedWithonlyPrm = utilDao.isPartnershipEstablishedOnlyWithPrm(userId);
		boolean isPartnershipEstablishedWithPrm = utilDao
				.isPrmByVendorCompanyId(vanityUrlDetailsDto.getVendorCompanyId());
		if (isPrmAndPartner || isPartnershipEstablishedWithonlyPrm || isPartnershipEstablishedWithPrm) {
			findCountsOfVaityLogInForPrmAndPartner(session, funnelChartReports, vanityUrlDetailsDto, isPrmAndPartner,
					userId, roleDisplayDto);
		} else {
			getRecipientCountForVendorVanityLogInAndAddToArray(session, funnelChartReports, vanityUrlDetailsDto,
					partnerRecipientVendorVanityLogin);
			getLeadsCountForVendorVanityLoginAndAddToArray(session, funnelChartReports, vanityUrlDetailsDto,
					leadQueryForPartnerVanityLogin);
			getDealsCountForVendorVanityLoginAndAddToArray(session, funnelChartReports, vanityUrlDetailsDto,
					dealsQueryForPartnerVanityLogin);
			getDealsWonCountForVendorVanityLoginAndAddToArray(session, funnelChartReports, vanityUrlDetailsDto,
					dealWonsQueryForPartnerVanityLogin);
		}
	}

	private void findCountsOfVaityLogInForPrmAndPartner(Session session, List<List<Object>> funnelChartReports,
			VanityUrlDetailsDTO vanityUrlDetailsDto, boolean isPrmAndPartner, Integer userId,
			RoleDisplayDTO roleDisplayDTO) {
		boolean isPartnerShipEstablishedWithPrm = utilDao
				.isPrmByVendorCompanyId(vanityUrlDetailsDto.getVendorCompanyId());
		if (isPartnerShipEstablishedWithPrm) {
			getLeadsCountForVendorVanityLoginAndAddToArray(session, funnelChartReports, vanityUrlDetailsDto,
					prmAndPartnerEstablishedWithPrmVendorVanityLoginLeads);
			getDealsCountForVendorVanityLoginAndAddToArray(session, funnelChartReports, vanityUrlDetailsDto,
					prmAndPartnerEstablishedWithPrmVendorVanityLoginDeals);
			getDealsWonCountForVendorVanityLoginAndAddToArray(session, funnelChartReports, vanityUrlDetailsDto,
					prmAndPartnerEstablishedWithPrmVendorVanityLoginDealsWon);
		} else {
			getRecipientCountForVendorVanityLogInAndAddToArray(session, funnelChartReports, vanityUrlDetailsDto,
					prmAndPartnerPartnershipEstablishedWithVendorOrOrgAdminRecipientVendorVanity);
			getLeadsCountForVendorVanityLoginAndAddToArray(session, funnelChartReports, vanityUrlDetailsDto,
					prmAndPartnerPartnershipEstablishedWithVendorOrOrgAdminLeadsVendorVanity);
			getDealsCountForVendorVanityLoginAndAddToArray(session, funnelChartReports, vanityUrlDetailsDto,
					prmAndPartnerPartnershipEstablishedWithVendorOrOrgAdminDealsVendorVanity);
			getDealsWonCountForVendorVanityLoginAndAddToArray(session, funnelChartReports, vanityUrlDetailsDto,
					prmAndPartnerPartnershipEstablishedWithVendorOrOrgAdminDealswonVendorVanity);
		}
	}

	private void findCountsForOwnVanityLogin(Session session, List<List<Object>> funnelChartReports, Integer userId,
			RoleDisplayDTO roleDisplayDto) {
		boolean isPrmAdminCompany = roleDisplayDto.isPrmOrPrmAndPartnerCompany();
		if (isPrmAdminCompany) {
			findPrmOrPrmAndPartnerForOwnvanityLogIn(session, funnelChartReports, userId, roleDisplayDto);
		}
	}

	private void findPrmOrPrmAndPartnerForOwnvanityLogIn(Session session, List<List<Object>> funnelChartReports,
			Integer userId, RoleDisplayDTO displayDTO) {
		boolean isPrm = displayDTO.isAdminTeamMember();
		boolean isAdmin = displayDTO.isAdminRole();
		if (isPrm || isAdmin) {
			getLeadsCountAndAddToArray(session, funnelChartReports, userId, prmLeadsOfFunelChart);
			getDealsCountAndAddToArray(session, funnelChartReports, userId, prmDealsOfFunelChart);
			getDealsWonCountAndAddToArray(session, funnelChartReports, userId, prmDealsWonOfFunelChart);
		} else {
			boolean isPartnerEstablishedWithPrm = utilDao.isPartnershipEstablishedWithPrm(userId);
			if (isPartnerEstablishedWithPrm) {
				getLeadsCountAndAddToArray(session, funnelChartReports, userId,
						prmAndPartnerEstablishedWithPrmOwnVanityLoginleads);
				getDealsCountAndAddToArray(session, funnelChartReports, userId,
						prmAndPartnerEstablishedWithPrmOwnVanityLoginDeals);
				getDealsWonCountAndAddToArray(session, funnelChartReports, userId,
						prmAndPartnerEstablishedWithPrmOwnVanityLoginDealsWon);
			}
		}

	}

	private void findCountForXamplifyLogin(Session session, List<List<Object>> funnelChartReports, Integer userId,
			RoleDisplayDTO roleDisplayDto) {
		boolean isPrmRole = roleDisplayDto.isPrmOrPrmAndPartnerCompany();
		if (isPrmRole) {
			findAllCountForXamplifyLogInForPrmOrPrmAdPartner(session, funnelChartReports, userId, roleDisplayDto);
		} else {
			findCountsForPartnerAdmin(session, funnelChartReports, userId);
		}
	}

	private void findAllCountForXamplifyLogInForPrmOrPrmAdPartner(Session session,
			List<List<Object>> funnelChartReports, Integer userId, RoleDisplayDTO displayDTO) {
		boolean isPrmTm = displayDTO.isAdminTeamMember();
		boolean isAdminRole = displayDTO.isAdminRole();
		boolean isPartnerTm = displayDTO.prmAndPartnerCompany();

		if ((isPrmTm || isAdminRole) && !isPartnerTm) {
			getLeadsCountAndAddToArray(session, funnelChartReports, userId, prmLeadsOfFunelChart);
			getDealsCountAndAddToArray(session, funnelChartReports, userId, prmDealsOfFunelChart);
			getDealsWonCountAndAddToArray(session, funnelChartReports, userId, prmDealsWonOfFunelChart);
		} else if (isPartnerTm) {

			getLeadsCountAndAddToArray(session, funnelChartReports, userId,
					PartnerPartnershipWithPrmXamplifyLoginllead);
			getDealsCountAndAddToArray(session, funnelChartReports, userId, PartnerPartnershipWithPrmXamplifyLoginDeal);
			getDealsWonCountAndAddToArray(session, funnelChartReports, userId,
					PartnerPartnershipWithPrmXamplifyLoginlDealWon);

		}

	}

	private void findCountForTeamMember(Session session, List<List<Object>> funnelChartReports, Integer userId,
			RoleDisplayDTO roleDisplayDTO, TeamMemberFilterDTO teamMemberFilterDTO) {
		boolean isPartnerRole = roleDisplayDTO.partnerOrPartnerTeamMember();
		boolean isPartnershipEstablishedWithOnlyPrm = utilDao.isPartnershipEstablishedOnlyWithPrm(userId);
		boolean isPrmRole = roleDisplayDTO.isPrmTeamMember();
		if (isPartnerRole) {
			findCountsForPartnerAdmin(session, funnelChartReports, userId);
		} else if (isPartnershipEstablishedWithOnlyPrm || isPrmRole) {
			getLeadsCountAndAddToArray(session, funnelChartReports, userId, teamMemberLeadsQuery);
			getDealsCountAndAddToArray(session, funnelChartReports, userId, teamMemberDealQuery);
			getDealsWonCountAndAddToArray(session, funnelChartReports, userId, teamMemberDealWonQuery);

		} else {
			/*** Added On 25/05/2022 For Loading Issue For Team Member Login *********/
			addItemsToArray(funnelChartReports, RECIPIENTS, 0, new ArrayList<Object>());
			getLeadsCountAndAddToArray(session, funnelChartReports, userId, teamMemberLeadsQuery);
			getDealsCountAndAddToArray(session, funnelChartReports, userId, teamMemberDealQuery);
			getDealsWonCountAndAddToArray(session, funnelChartReports, userId, teamMemberDealWonQuery);
		}

	}

	private void addRecipientsCountToArray(List<List<Object>> funnelChartReports, Integer recipientsCount) {
		addItemsToArray(funnelChartReports, RECIPIENTS, recipientsCount, new ArrayList<Object>());
	}

	private void findCountsForPartnerAdmin(Session session, List<List<Object>> funnelChartReports, Integer userId) {
		boolean isPartnershipestablshedWithOnlyPrm = utilDao.isPartnershipEstablishedOnlyWithPrm(userId);
		if (isPartnershipestablshedWithOnlyPrm) {
			/*********** Leads Count ***********/
			getPartnerLeadsCountAndAddToArray(session, funnelChartReports, userId);
			/*********** Deals Count ***********/
			getPartnerDealsCount(session, funnelChartReports, userId);
			/*********** Deals Won Count ***********/
			getPartnerDealsWonCountAndAddToArray(session, funnelChartReports, userId);
		} else {
			/*********** Leads Count ***********/
			getPartnerLeadsCountAndAddToArray(session, funnelChartReports, userId);
			/*********** Deals Count ***********/
			getPartnerDealsCount(session, funnelChartReports, userId);
			/*********** Deals Won Count ***********/
			getPartnerDealsWonCountAndAddToArray(session, funnelChartReports, userId);
		}
	}

	private void getLeadsCountAndAddToArray(Session session, List<List<Object>> funnelChartReports, Integer userId,
			String queryString) {
		Integer leadsCount = (Integer) getCountByQuery(session, userId, queryString);
		addLeadsCountToArray(funnelChartReports, leadsCount);
	}

	private void getRecipientCountForVendorVanityLogInAndAddToArray(Session session,
			List<List<Object>> funnelChartReports, VanityUrlDetailsDTO vanityUrlDetailsDto, String queryString) {
		Integer leadsCount = (Integer) getCountByQueryForVendorVanityLogin(session, vanityUrlDetailsDto, queryString);
		addRecipientsCountToArray(funnelChartReports, leadsCount);
	}

	private void getLeadsCountForVendorVanityLoginAndAddToArray(Session session, List<List<Object>> funnelChartReports,
			VanityUrlDetailsDTO vanityUrlDetailsDto, String queryString) {
		Integer leadsCount = (Integer) getCountByQueryForVendorVanityLogin(session, vanityUrlDetailsDto, queryString);
		addLeadsCountToArray(funnelChartReports, leadsCount);
	}

	private void getDealsCountForVendorVanityLoginAndAddToArray(Session session, List<List<Object>> funnelChartReports,
			VanityUrlDetailsDTO vanityUrlDetailsDto, String queryString) {
		Integer leadsCount = (Integer) getCountByQueryForVendorVanityLogin(session, vanityUrlDetailsDto, queryString);
		addDealsCountToArray(funnelChartReports, leadsCount);
	}

	private void getDealsWonCountForVendorVanityLoginAndAddToArray(Session session,
			List<List<Object>> funnelChartReports, VanityUrlDetailsDTO vanityUrlDetailsDto, String queryString) {
		Integer leadsCount = (Integer) getCountByQueryForVendorVanityLogin(session, vanityUrlDetailsDto, queryString);
		addDealsWonCountToArray(funnelChartReports, leadsCount);
	}

	private void addLeadsCountToArray(List<List<Object>> funnelChartReports, Integer leadsCount) {
		addItemsToArray(funnelChartReports, LEADS, leadsCount, new ArrayList<Object>());
	}

	private void getDealsCountAndAddToArray(Session session, List<List<Object>> funnelChartReports, Integer userId,
			String query) {
		Integer dealsCount = (Integer) getCountByQuery(session, userId, query);
		addDealsCountToArray(funnelChartReports, dealsCount);
	}

	private void getDealsWonCountAndAddToArray(Session session, List<List<Object>> funnelChartReports, Integer userId,
			String query) {
		Integer dealsWonCount = (Integer) getCountByQuery(session, userId, query);
		addDealsWonCountToArray(funnelChartReports, dealsWonCount);
	}

	private void getPartnerLeadsCountAndAddToArray(Session session, List<List<Object>> funnelChartReports,
			Integer userId) {
		Integer partnerLeadsCount = (Integer) getCountByQuery(session, userId, funnelPartnerLeadsQuery);
		addLeadsCountToArray(funnelChartReports, partnerLeadsCount);
	}

	private void getPartnerDealsWonCountAndAddToArray(Session session, List<List<Object>> funnelChartReports,
			Integer userId) {
		Integer partnerDealsWonCount = (Integer) getCountByQuery(session, userId, partnerDealsWon);
		addDealsWonCountToArray(funnelChartReports, partnerDealsWonCount);

	}

	private void addDealsWonCountToArray(List<List<Object>> funnelChartReports, Integer count) {
		addItemsToArray(funnelChartReports, DEALS_WON, count, new ArrayList<Object>());
	}

	private void getPartnerDealsCount(Session session, List<List<Object>> funnelChartReports, Integer userId) {
		Integer partnerDealsCount = (Integer) getCountByQuery(session, userId, partnerDealsQuery);
		addDealsCountToArray(funnelChartReports, partnerDealsCount);

	}

	private void addDealsCountToArray(List<List<Object>> funnelChartReports, Integer count) {
		addItemsToArray(funnelChartReports, DEALS, count, new ArrayList<Object>());
	}

	private void addItemsToArray(List<List<Object>> funnelChartReports, String moduleName, Object count,
			List<Object> array) {
		array.add(moduleName);
		array.add(count);
		funnelChartReports.add(array);
	}

	private Object getCountByQuery(Session session, Integer userId, String queryString) {
		SQLQuery query = session.createSQLQuery(queryString);
		query.setParameter(USER_ID, userId);
		return (Object) query.uniqueResult();
	}

	private Object getCountByQueryForVendorVanityLogin(Session session, VanityUrlDetailsDTO vanityUrlDetailsDto,
			String queryString) {
		SQLQuery query = session.createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", vanityUrlDetailsDto.getVendorCompanyId());
		query.setParameter("partnerCompanyId", vanityUrlDetailsDto.getLoggedInUserCompanyId());
		return (Object) query.uniqueResult();
	}

	@Override
	public List<List<Object>> getPieChartLeadAnalyticsData(VanityUrlDetailsDTO vanityUrlDetailsDto,
			RoleDisplayDTO roleDisplayDto) {
		try {
			Session session = sessionFactory.getCurrentSession();
			List<List<Object>> pieChartReports = new ArrayList<List<Object>>();
			Integer userId = vanityUrlDetailsDto.getUserId();
			boolean applyFilter = vanityUrlDetailsDto.isApplyFilter();
			TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(userId, applyFilter, false);
			boolean applyTeamMemberFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
			boolean ispartnerTm = roleDisplayDto.partnerOrPartnerTeamMember();
			if (teamMemberFilterDTO.isEmptyFilter()) {
				if (ispartnerTm || vanityUrlDetailsDto.isVanityUrlFilter()) {
					if (vanityUrlDetailsDto.isPartnerLoggedInThroughVanityUrl()) {
						findCountsForTeamMemberOfVendorVanityLogIn(vanityUrlDetailsDto, session, pieChartReports);
					} else if (vanityUrlDetailsDto.getLoginAsUserId() != null
							&& vanityUrlDetailsDto.getLoginAsUserId() > 0) {
						if (ispartnerTm) {
							Integer partnerCompanyId = userDao.getCompanyIdByUserId(vanityUrlDetailsDto.getUserId());
							Integer vendorCompanyId = userDao
									.getCompanyIdByUserId(vanityUrlDetailsDto.getLoginAsUserId());
							vanityUrlDetailsDto.setLoggedInUserCompanyId(partnerCompanyId);
							vanityUrlDetailsDto.setVendorCompanyId(vendorCompanyId);
							findCountsForVendorVanityLoginOfPieChart(vanityUrlDetailsDto, session, pieChartReports,
									roleDisplayDto);
						}
					} else {
						findLeadsCountForTeamMemberForPartnerOfPieChart(session, pieChartReports, userId);
					}
				} else {
					return new ArrayList<List<Object>>();
				}
			} else {
				if (applyTeamMemberFilter) {
					if (vanityUrlDetailsDto.isPartnerLoggedInThroughVanityUrl()) {
						findCountsForTeamMemberOfVendorVanityLogIn(vanityUrlDetailsDto, session, pieChartReports);
					} else {
						findLeadsCountForTeamMemberOfPieChart(session, pieChartReports, userId, roleDisplayDto);
					}
				} else {
					if (vanityUrlDetailsDto.isVendorLoggedInThroughOwnVanityUrl()) {
						findCountsForOwnVanityLoginOfPieChart(session, pieChartReports, userId, roleDisplayDto);
					}
					/**** LOgin in as Partner ******/
					else if (vanityUrlDetailsDto.getLoginAsUserId() != null
							&& vanityUrlDetailsDto.getLoginAsUserId() > 0) {
						Integer partnerCompanyId = userDao.getCompanyIdByUserId(vanityUrlDetailsDto.getUserId());
						Integer vendorCompanyId = userDao.getCompanyIdByUserId(vanityUrlDetailsDto.getLoginAsUserId());
						vanityUrlDetailsDto.setLoggedInUserCompanyId(partnerCompanyId);
						vanityUrlDetailsDto.setVendorCompanyId(vendorCompanyId);
						if (roleDisplayDto.partnerOrPartnerTeamMember()) {
							findCountsForVendorVanityLoginOfPieChart(vanityUrlDetailsDto, session, pieChartReports,
									roleDisplayDto);
						} else {
							logInAsPartnerPieChart(vanityUrlDetailsDto, session, pieChartReports, roleDisplayDto);
						}

					}
					/**** LOgin in as Partner ******/
					else if (vanityUrlDetailsDto.isPartnerLoggedInThroughVanityUrl()) {
						findCountsForVendorVanityLoginOfPieChart(vanityUrlDetailsDto, session, pieChartReports,
								roleDisplayDto);
					} else {
						findCountForXamplifyLoginOfPieChart(session, pieChartReports, userId, roleDisplayDto);
					}
				}
			}
			return pieChartReports;
		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex);
		}
	}

	private void findLeadsCountForTeamMemberForPartnerOfPieChart(Session session, List<List<Object>> pieChartReports,
			Integer userId) {
		getPieWonLeadsCountAndAddToArray(session, pieChartReports, userId, partnerXamplifyLogInLeadWon);
		getPieLeadsLostCountAndAddToArray(session, pieChartReports, userId, partnerXamplifyLogInLeadsLost);
		getPieLeadsConvertedCountAndAddToArray(session, pieChartReports, userId, partnerXamplifyLogInLeadsConvertd);

	}

	private void findCountsForTeamMemberOfVendorVanityLogIn(VanityUrlDetailsDTO vanityUrlDetailsDto, Session session,
			List<List<Object>> funnelChartReports) {
		getLeadsLostCountForVendorVanityLoginAndAddToArray(session, funnelChartReports, vanityUrlDetailsDto,
				leadsLostQueryForVendorVanityLoginOfPieChart);
		getLeadsWonCountForVendorVanityLoginAndAddToArray(session, funnelChartReports, vanityUrlDetailsDto,
				leadsWonQueryForVendorVanityLoginOfPieChart);
		/****** leadsConvertedQueryForVendorVanityLogin *******/
		getLeadsConvertedCountForVendorVanityLoginAndAddToArray(session, funnelChartReports, vanityUrlDetailsDto,
				leadsConvertedQueryForVendorVanityLoginOfPieChart);

	}

	/******* logInAsPartnerPieChart ********/
	private void logInAsPartnerPieChart(VanityUrlDetailsDTO vanityUrlDetailsDto, Session session,
			List<List<Object>> funnelChartReports, RoleDisplayDTO roleDisplayDto) {
		logInAsPartnerPieChart(session, funnelChartReports, vanityUrlDetailsDto);
	}

	private void logInAsPartnerPieChart(Session session, List<List<Object>> funnelChartReports,
			VanityUrlDetailsDTO vanityUrlDetailsDto) {
		getLeadsLostCountForVendorVanityLoginAndAddToArray(session, funnelChartReports, vanityUrlDetailsDto,
				logInAsPartnerasLostLeadspie);
		getLeadsWonCountForVendorVanityLoginAndAddToArray(session, funnelChartReports, vanityUrlDetailsDto,
				logInAsPartnerasWonLeadspie);
		/****** leadsConvertedQueryForVendorVanityLogin *******/
		getLeadsConvertedCountForVendorVanityLoginAndAddToArray(session, funnelChartReports, vanityUrlDetailsDto,
				logInAsPartnerasConvertedLeadspie);

	}

	/************ logInAsPartnerPieChart ******/
	private void findCountsForVendorVanityLoginOfPieChart(VanityUrlDetailsDTO vanityUrlDetailsDto, Session session,
			List<List<Object>> funnelChartReports, RoleDisplayDTO roleDisplayDto) {

		findLeadsCountForVendorVanityLogInForOrgAdmin(session, funnelChartReports, vanityUrlDetailsDto);
	}

	private void findLeadsCountForVendorVanityLogInForOrgAdmin(Session session, List<List<Object>> funnelChartReports,
			VanityUrlDetailsDTO vanityUrlDetailsDto) {
		getLeadsLostCountForVendorVanityLoginAndAddToArray(session, funnelChartReports, vanityUrlDetailsDto,
				leadsLostQueryForVendorVanityLoginOfPieChart);
		getLeadsWonCountForVendorVanityLoginAndAddToArray(session, funnelChartReports, vanityUrlDetailsDto,
				leadsWonQueryForVendorVanityLoginOfPieChart);
		/****** leadsConvertedQueryForVendorVanityLogin *******/
		getLeadsConvertedCountForVendorVanityLoginAndAddToArray(session, funnelChartReports, vanityUrlDetailsDto,
				leadsConvertedQueryForVendorVanityLoginOfPieChart);

	}

	private void findCountsForOwnVanityLoginOfPieChart(Session session, List<List<Object>> pieChartReports,
			Integer userId, RoleDisplayDTO roleDisplayDto) {
		getPieWonLeadsCountAndAddToArray(session, pieChartReports, userId, vendorLeadsWonPieChart);
		getPieLeadsLostCountAndAddToArray(session, pieChartReports, userId, vendorLeadsLostPieChart);
		getPieLeadsConvertedCountAndAddToArray(session, pieChartReports, userId, vendorLeadsConvertedPieChart);
	}

	private void findCountForXamplifyLoginOfPieChart(Session session, List<List<Object>> pieChartReports,
			Integer userId, RoleDisplayDTO roleDisplayDto) {

		boolean isPartner = roleDisplayDto.partnerOrPartnerTeamMember();
		boolean isAnyAdminOrTeamMemberOfAdmin = roleDisplayDto.isAnyAdminOrTeamMemberOfAdmin();
		if (isAnyAdminOrTeamMemberOfAdmin) {
			findLeadsCountForOrgAdminAndOrgAdminPartnerXamplifyLogIn(session, pieChartReports, userId, roleDisplayDto);
		} else if (isPartner) {
			getPieWonLeadsCountAndAddToArray(session, pieChartReports, userId, partnerXamplifyLogInLeadWon);
			getPieLeadsLostCountAndAddToArray(session, pieChartReports, userId, partnerXamplifyLogInLeadsLost);
			getPieLeadsConvertedCountAndAddToArray(session, pieChartReports, userId, partnerXamplifyLogInLeadsConvertd);
		} else {
			findLeadsCountForOrgAdminAndOrgAdminPartnerXamplifyLogIn(session, pieChartReports, userId, roleDisplayDto);
		}
	}

	private void findLeadsCountForOrgAdminAndOrgAdminPartnerXamplifyLogIn(Session session,
			List<List<Object>> pieChartReports, Integer userId, RoleDisplayDTO roleDisplayDTO) {

		boolean isAnyAdmin = roleDisplayDTO.isAdminRole();
		boolean isAdminTM = roleDisplayDTO.isAdminTeamMember();
		boolean isAdminAndPartnerTm = roleDisplayDTO.isAdminAndPartnerTeamMember();
		if ((isAnyAdmin || isAdminTM) && (!isAdminAndPartnerTm)) {
			getPieWonLeadsCountAndAddToArray(session, pieChartReports, userId, vendorLeadsWonPieChart);
			getPieLeadsLostCountAndAddToArray(session, pieChartReports, userId, vendorLeadsLostPieChart);
			getPieLeadsConvertedCountAndAddToArray(session, pieChartReports, userId, vendorLeadsConvertedPieChart);
		} else {
			getPieWonLeadsCountAndAddToArray(session, pieChartReports, userId,
					orgAdminAndPartnerLeadWonPiechartxamplifylogin);
			getPieLeadsLostCountAndAddToArray(session, pieChartReports, userId,
					orgAdminAndPartnerLeadLostPiechartxamplifylogin);
			getPieLeadsConvertedCountAndAddToArray(session, pieChartReports, userId,
					orgAdminAndPartnerLeadConvertedPiechartxamplifylogin);

		}

	}

	private void findLeadsCountForTeamMemberOfPieChart(Session session, List<List<Object>> funnelChartReports,
			Integer userId, RoleDisplayDTO roleDisplayDTO) {
		boolean isPartnerRole = roleDisplayDTO.partnerOrPartnerTeamMember();

		if (isPartnerRole) {
			getPieWonLeadsCountAndAddToArray(session, funnelChartReports, userId, partnerXamplifyLogInLeadWon);
			getPieLeadsLostCountAndAddToArray(session, funnelChartReports, userId, partnerXamplifyLogInLeadsLost);
			getPieLeadsConvertedCountAndAddToArray(session, funnelChartReports, userId,
					partnerXamplifyLogInLeadsConvertd);
		} else {
			getLeadsLostCountAndAddToArray(session, funnelChartReports, userId,
					leadLostQueryForVedorTeamMemberOfPieChart);
			getLeadsWonCountAndAddToArray(session, funnelChartReports, userId,
					leadWonQueryForVedorTeamMemberOfPieChart);
			getLeadsConvertedCountAndAddToArray(session, funnelChartReports, userId,
					leadCovertedQueryForVedorTeamMemberOfPieChart);
		}

	}

	private void addLeadsWonCountToArray(List<List<Object>> funnelChartReports, Integer leadsCount) {
		addItemsToArray(funnelChartReports, LEADS_WON, leadsCount, new ArrayList<Object>());
	}

	private void addLeadsConvertedCountToArray(List<List<Object>> funnelChartReports, Integer leadsCount) {
		addItemsToArray(funnelChartReports, CONVERTED_LEADS, leadsCount, new ArrayList<Object>());
	}

	private void addLeadsLostCountToArray(List<List<Object>> funnelChartReports, Integer leadsCount) {
		addItemsToArray(funnelChartReports, LOST_LEADS, leadsCount, new ArrayList<Object>());
	}

	private void getLeadsLostCountAndAddToArray(Session session, List<List<Object>> funnelChartReports, Integer userId,
			String query) {
		Integer leadsLostCount = (Integer) getCountByQuery(session, userId, query);
		addLeadsLostCountToArray(funnelChartReports, leadsLostCount);
	}

	private void getLeadsWonCountAndAddToArray(Session session, List<List<Object>> funnelChartReports, Integer userId,
			String query) {
		Integer leadsWonCount = (Integer) getCountByQuery(session, userId, query);
		addLeadsWonCountToArray(funnelChartReports, leadsWonCount);
	}

	private void getLeadsConvertedCountAndAddToArray(Session session, List<List<Object>> funnelChartReports,
			Integer userId, String query) {
		Integer leadsConvertedCount = (Integer) getCountByQuery(session, userId, query);
		addLeadsConvertedCountToArray(funnelChartReports, leadsConvertedCount);
	}

	private void getLeadsWonCountForVendorVanityLoginAndAddToArray(Session session,
			List<List<Object>> funnelChartReports, VanityUrlDetailsDTO vanityUrlDetailsDto, String queryString) {
		Integer leadsCount = (Integer) getCountByQueryForVendorVanityLogin(session, vanityUrlDetailsDto, queryString);
		addLeadsWonCountToArray(funnelChartReports, leadsCount);
	}

	private void getLeadsLostCountForVendorVanityLoginAndAddToArray(Session session,
			List<List<Object>> funnelChartReports, VanityUrlDetailsDTO vanityUrlDetailsDto, String queryString) {
		Integer leadsCount = (Integer) getCountByQueryForVendorVanityLogin(session, vanityUrlDetailsDto, queryString);
		addLeadsLostCountToArray(funnelChartReports, leadsCount);
	}

	private void getLeadsConvertedCountForVendorVanityLoginAndAddToArray(Session session,
			List<List<Object>> funnelChartReports, VanityUrlDetailsDTO vanityUrlDetailsDto, String queryString) {
		Integer leadsCount = (Integer) getCountByQueryForVendorVanityLogin(session, vanityUrlDetailsDto, queryString);
		addLeadsConvertedCountToArray(funnelChartReports, leadsCount);
	}

	private void getPieWonLeadsCountAndAddToArray(Session session, List<List<Object>> pieChartReports, Integer userId,
			String query) {
		Integer wonLeadsCount = (Integer) getCountByQuery(session, userId, query);
		addItemsToArray(pieChartReports, LEADS_WON, wonLeadsCount, new ArrayList<Object>());

	}

	private void getPieLeadsLostCountAndAddToArray(Session session, List<List<Object>> pieChartReports, Integer userId,
			String query) {
		Integer lostLeadsCount = (Integer) getCountByQuery(session, userId, query);
		addItemsToArray(pieChartReports, LOST_LEADS, lostLeadsCount, new ArrayList<Object>());
	}

	private void getPieLeadsConvertedCountAndAddToArray(Session session, List<List<Object>> pieChartReports,
			Integer userId, String query) {
		Integer convertedLeadsCount = (Integer) getCountByQuery(session, userId, query);
		addItemsToArray(pieChartReports, CONVERTED_LEADS, convertedLeadsCount, new ArrayList<Object>());
	}

	@Override
	public List<List<Object>> getPieChartDealsAnalyticsData(VanityUrlDetailsDTO vanityUrlDetailsDto,
			RoleDisplayDTO roleDisplayDto) {
		try {
			Session session = sessionFactory.getCurrentSession();
			List<List<Object>> pieChartReports = new ArrayList<List<Object>>();
			Integer userId = vanityUrlDetailsDto.getUserId();
			boolean applyFilter = vanityUrlDetailsDto.isApplyFilter();
			TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(userId, applyFilter, false);
			boolean applyTeamMemberFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
			boolean isPartnerTm = roleDisplayDto.partnerOrPartnerTeamMember();
			if (teamMemberFilterDTO.isEmptyFilter()) {
				if (isPartnerTm || vanityUrlDetailsDto.isVanityUrlFilter()) {
					if (vanityUrlDetailsDto.isPartnerLoggedInThroughVanityUrl()) {
						findCountsForTeamMemberOfVendorVanityLogIn(vanityUrlDetailsDto, session, pieChartReports);
					} else if (vanityUrlDetailsDto.getLoginAsUserId() != null
							&& vanityUrlDetailsDto.getLoginAsUserId() > 0) {
						if (isPartnerTm) {
							Integer partnerCompanyId = userDao.getCompanyIdByUserId(vanityUrlDetailsDto.getUserId());
							Integer vendorCompanyId = userDao
									.getCompanyIdByUserId(vanityUrlDetailsDto.getLoginAsUserId());
							vanityUrlDetailsDto.setLoggedInUserCompanyId(partnerCompanyId);
							vanityUrlDetailsDto.setVendorCompanyId(vendorCompanyId);
							findDealCountsForVendorVanityLoginOfPieChart(vanityUrlDetailsDto, session, pieChartReports);
						}
					} else {
						findDealsCountForPartnerTeamMemberOfPieChart(session, pieChartReports, userId);
					}
				} else {
					return new ArrayList<List<Object>>();
				}
			} else {
				if (applyTeamMemberFilter) {
					if (vanityUrlDetailsDto.isPartnerLoggedInThroughVanityUrl()) {
						findDealsCountsForTeamMemberOfVendorVanityLogIn(vanityUrlDetailsDto, session, pieChartReports);
					} else {
						findDealsCountForTeamMemberOfPieChart(session, pieChartReports, userId, roleDisplayDto);
					}
				} else {
					if (vanityUrlDetailsDto.isVendorLoggedInThroughOwnVanityUrl()) {
						findDealCountsForOwnVanityLoginOfPieChart(session, pieChartReports, userId);
					}
					/**** LOgin in as Partner ******/
					else if (vanityUrlDetailsDto.getLoginAsUserId() != null
							&& vanityUrlDetailsDto.getLoginAsUserId() > 0) {
						Integer partnerCompanyId = userDao.getCompanyIdByUserId(vanityUrlDetailsDto.getUserId());
						Integer vendorCompanyId = userDao.getCompanyIdByUserId(vanityUrlDetailsDto.getLoginAsUserId());
						vanityUrlDetailsDto.setLoggedInUserCompanyId(partnerCompanyId);
						vanityUrlDetailsDto.setVendorCompanyId(vendorCompanyId);
						if (roleDisplayDto.partnerOrPartnerTeamMember()) {
							findDealCountsForVendorVanityLoginOfPieChart(vanityUrlDetailsDto, session, pieChartReports);
						} else {
							logInAsPartnerDealsOfPieChart(vanityUrlDetailsDto, session, pieChartReports);
						}
					}
					/**** LOgin in as Partner ******/
					else if (vanityUrlDetailsDto.isPartnerLoggedInThroughVanityUrl()) {
						findDealCountsForVendorVanityLoginOfPieChart(vanityUrlDetailsDto, session, pieChartReports);
					} else {
						findDealCountForXamplifyLoginOfPieChart(session, pieChartReports, userId, roleDisplayDto);
					}
				}
			}
			return pieChartReports;

		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex);
		}
	}

	private void findDealsCountForPartnerTeamMemberOfPieChart(Session session, List<List<Object>> pieChartReports,
			Integer userId) {
		getPieDealsWonCountAndAddToArra(session, pieChartReports, userId, partnerPieDealsWonQuery);
		getPieDealsLostCountAndAddToArray(session, pieChartReports, userId, partnerPieDealsLostQuery);

	}

	private void findDealsCountsForTeamMemberOfVendorVanityLogIn(VanityUrlDetailsDTO vanityUrlDetailsDto,
			Session session, List<List<Object>> pieChartReports) {
		getDealsWonCountForVendorVanityLoginAndAddToArray(session, pieChartReports, vanityUrlDetailsDto,
				dealsWonQueryForVendorVanityLoginOfPieChart);
		/***** dealsLostQueryForVendorVanityLogin *********/
		getDealsLostCountForVendorVanityLoginAndAddToArray(session, pieChartReports, vanityUrlDetailsDto,
				dealsLostQueryForVendorVanityLoginOfPieChart);
	}

	private void findDealsCountForTeamMemberOfPieChart(Session session, List<List<Object>> funnelChartReports,
			Integer userId, RoleDisplayDTO roleDisplayDTO) {
		boolean isPartnerRole = roleDisplayDTO.partnerOrPartnerTeamMember();
		if (isPartnerRole) {
			findPieChartDealsForPartner(session, funnelChartReports, userId, isPartnerRole);
		} else {
			getDealsWonCountAndAddToArray(session, funnelChartReports, userId,
					dealWonQueryForVedorTeamMemberOfPieChart);
			getDealsLostCountAndAddToArray(session, funnelChartReports, userId,
					dealLostQueryForVedorTeamMemberOfPieChart);
		}

	}

	private void findDealCountsForOwnVanityLoginOfPieChart(Session session, List<List<Object>> funnelChartReports,
			Integer userId) {

		/*********** Deals Won Count ***********/
		getDealsWonCountAndAddToArray(session, funnelChartReports, userId, vendorDealsWonPieChart);
		/********* Deals Lost Count **********/
		getDealsLostCountAndAddToArray(session, funnelChartReports, userId, vendorDealsLostPieChart);
	}

	/***** Login As Partner Delas ******/
	private void logInAsPartnerDealsOfPieChart(VanityUrlDetailsDTO vanityUrlDetailsDto, Session session,
			List<List<Object>> funnelChartReports) {

		getDealsWonCountForVendorVanityLoginAndAddToArray(session, funnelChartReports, vanityUrlDetailsDto,
				logInAsPartnerasDealsWonpie);
		getDealsLostCountForVendorVanityLoginAndAddToArray(session, funnelChartReports, vanityUrlDetailsDto,
				logInAsPartnerasDealsLostpie);
	}

	/**** Login As Partner Deals *****/
	private void findDealCountsForVendorVanityLoginOfPieChart(VanityUrlDetailsDTO vanityUrlDetailsDto, Session session,
			List<List<Object>> funnelChartReports) {

		getDealsWonCountForVendorVanityLoginAndAddToArray(session, funnelChartReports, vanityUrlDetailsDto,
				dealsWonQueryForVendorVanityLoginOfPieChart);
		/***** dealsLostQueryForVendorVanityLogin *********/
		getDealsLostCountForVendorVanityLoginAndAddToArray(session, funnelChartReports, vanityUrlDetailsDto,
				dealsLostQueryForVendorVanityLoginOfPieChart);
	}

	private void findDealCountForXamplifyLoginOfPieChart(Session session, List<List<Object>> pieChartReports,
			Integer userId, RoleDisplayDTO roleDisplayDto) {
		boolean isPartner = roleDisplayDto.partnerOrPartnerTeamMember();
		boolean isPrm = roleDisplayDto.isPrm();
		if (isPrm) {
			/************ Deals ******************/
			findPieChartDealsForVendor(session, pieChartReports, userId, roleDisplayDto);
		} else if (isPartner) {
			findPieChartDealsForPartner(session, pieChartReports, userId, isPartner);
		} else {
			getPieDealsWonCountAndAddToArra(session, pieChartReports, userId, dealsWonForAdminAndPartner);
			getPieDealsLostCountAndAddToArray(session, pieChartReports, userId, dealsLostForAdminAndPartner);
		}
	}

	private void getDealsLostCountAndAddToArray(Session session, List<List<Object>> funnelChartReports, Integer userId,
			String query) {
		Integer dealsWonCount = (Integer) getCountByQuery(session, userId, query);
		addDealsLostCountToArray(funnelChartReports, dealsWonCount);
	}

	private void getDealsLostCountForVendorVanityLoginAndAddToArray(Session session,
			List<List<Object>> funnelChartReports, VanityUrlDetailsDTO vanityUrlDetailsDto, String queryString) {
		Integer leadsCount = (Integer) getCountByQueryForVendorVanityLogin(session, vanityUrlDetailsDto, queryString);
		addDealsLostCountToArray(funnelChartReports, leadsCount);
	}

	private void addDealsLostCountToArray(List<List<Object>> funnelChartReports, Integer count) {
		addItemsToArray(funnelChartReports, DEALS_LOST, count, new ArrayList<Object>());
	}

	private void findPieChartDealsForVendor(Session session, List<List<Object>> pieChartReports, Integer userId,
			RoleDisplayDTO roleDisplayDto) {
		boolean isAdminTeamMember = roleDisplayDto.isAdminTeamMember();
		boolean isPrm = roleDisplayDto.isPrm();
		if (isPrm || isAdminTeamMember) {
			getPieDealsWonCountAndAddToArra(session, pieChartReports, userId, vendorDealsWonPieChart);
			getPieDealsLostCountAndAddToArray(session, pieChartReports, userId, vendorDealsLostPieChart);
		}

	}

	private void findPieChartDealsForPartner(Session session, List<List<Object>> pieChartReports, Integer userId,
			boolean isPartner) {
		if (isPartner) {
			// getPieDealsCountAndAddToArray(session, pieChartReports, userId,
			// partnerPieDealQuery);
			getPieDealsWonCountAndAddToArra(session, pieChartReports, userId, partnerPieDealsWonQuery);
			getPieDealsLostCountAndAddToArray(session, pieChartReports, userId, partnerPieDealsLostQuery);
		}

	}

	private void getPieDealsWonCountAndAddToArra(Session session, List<List<Object>> pieChartReports, Integer userId,
			String queryString) {
		Integer dealsWonCount = (Integer) getCountByQuery(session, userId, queryString);
		addItemsToArray(pieChartReports, DEALS_WON, dealsWonCount, new ArrayList<Object>());
	}

	private void getPieDealsLostCountAndAddToArray(Session session, List<List<Object>> pieChartReports, Integer userId,
			String queryString) {
		Integer dealsWonCount = (Integer) getCountByQuery(session, userId, queryString);
		addItemsToArray(pieChartReports, DEALS_LOST, dealsWonCount, new ArrayList<Object>());
	}

	@Override
	public List<StatisticsDetailsOfPieChart> getPieChartLeadStatisticsData(VanityUrlDetailsDTO vanityUrlDetailsDto,
			RoleDisplayDTO roleDisplayDto) {
		Session session = sessionFactory.getCurrentSession();
		List<StatisticsDetailsOfPieChart> pieChartReports = new ArrayList<StatisticsDetailsOfPieChart>();

		Integer userId = vanityUrlDetailsDto.getUserId();
		boolean applyFilter = vanityUrlDetailsDto.isApplyFilter();
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(userId, applyFilter, false);
		boolean applyTeamMemberFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
		boolean isPartnerTm = roleDisplayDto.partnerOrPartnerTeamMember();
		if (teamMemberFilterDTO.isEmptyFilter()) {
			if (isPartnerTm || vanityUrlDetailsDto.isVanityUrlFilter()) {
				if (vanityUrlDetailsDto.isPartnerLoggedInThroughVanityUrl()) {
					findLeadsStatisticsForVendorvanityLogIn(session, pieChartReports, vanityUrlDetailsDto);
				} else if (vanityUrlDetailsDto.getLoginAsUserId() != null
						&& vanityUrlDetailsDto.getLoginAsUserId() > 0) {
					Integer partnerCompanyId = userDao.getCompanyIdByUserId(vanityUrlDetailsDto.getUserId());
					Integer vendorCompanyId = userDao.getCompanyIdByUserId(vanityUrlDetailsDto.getLoginAsUserId());
					vanityUrlDetailsDto.setLoggedInUserCompanyId(partnerCompanyId);
					vanityUrlDetailsDto.setVendorCompanyId(vendorCompanyId);
					if (roleDisplayDto.partnerOrPartnerTeamMember()) {
						findLeadsStatisticsForVendorvanityLogIn(session, pieChartReports, vanityUrlDetailsDto);
					}
				} else {
					findPieChartStatisticsForpartner(session, userId, pieChartReports);
				}
			} else {
				return new ArrayList<StatisticsDetailsOfPieChart>();
			}
		} else {
			if (applyTeamMemberFilter) {
				if (vanityUrlDetailsDto.isPartnerLoggedInThroughVanityUrl()) {
					findLeadsStatisticsForVendorvanityLogIn(session, pieChartReports, vanityUrlDetailsDto);
				} else {
					findLeadStatisticsDataForTeamMember(session, pieChartReports, userId, roleDisplayDto);
				}
			} else {
				if (vanityUrlDetailsDto.isVendorLoggedInThroughOwnVanityUrl()) {
					findLeadStatisticsForOwnVanityLoginOfPieChart(session, userId, pieChartReports, roleDisplayDto);
				}
				/**** LOgin in as Partner ******/
				else if (vanityUrlDetailsDto.getLoginAsUserId() != null && vanityUrlDetailsDto.getLoginAsUserId() > 0) {
					Integer partnerCompanyId = userDao.getCompanyIdByUserId(vanityUrlDetailsDto.getUserId());
					Integer vendorCompanyId = userDao.getCompanyIdByUserId(vanityUrlDetailsDto.getLoginAsUserId());
					vanityUrlDetailsDto.setLoggedInUserCompanyId(partnerCompanyId);
					vanityUrlDetailsDto.setVendorCompanyId(vendorCompanyId);
					if (roleDisplayDto.partnerOrPartnerTeamMember()) {
						findLeadsStatisticsForVendorvanityLogIn(session, pieChartReports, vanityUrlDetailsDto);
					} else {
						logInAsPartnerLeadStatistics(vanityUrlDetailsDto, session, pieChartReports, roleDisplayDto);
					}
				}
				/**** LOgin in as Partner ******/
				else if (vanityUrlDetailsDto.isPartnerLoggedInThroughVanityUrl()) {
					findLeadStatisticsForVendorLoginOfPieChart(vanityUrlDetailsDto, session, pieChartReports,
							roleDisplayDto);
				} else {
					findLeadStatisticsForXamplifyLoginOfPieChart(session, pieChartReports, userId, roleDisplayDto);
				}
			}
		}
		return pieChartReports;
	}

	private void findLeadStatisticsForVendorLoginOfPieChart(VanityUrlDetailsDTO vanityUrlDetailsDto, Session session,
			List<StatisticsDetailsOfPieChart> pieChartReports, RoleDisplayDTO roleDisplayDTO) {
		boolean isPartenrShipEstablishedOnlyPrm = utilDao
				.isPrmByVendorCompanyId(vanityUrlDetailsDto.getVendorCompanyId());
		if (isPartenrShipEstablishedOnlyPrm) {
			getLeadToDealCountForVendorVanityLoginAndAddToArray(session, pieChartReports, vanityUrlDetailsDto,
					vendorLeadToDealQueryForVendorVanityLoginOfPieChart);
			getLeadCreatedTimeForVendorVanityLoginAndAddToArray(session, pieChartReports, vanityUrlDetailsDto,
					vendorLatestLeadCreatedQueryForVendorVanityLoginOfPieChart);
		} else {
			getContactToLeadCountForVendorVanityLoginAndAddToArray(session, pieChartReports, vanityUrlDetailsDto,
					OrgAdminandPartnerwithVendorVanityLoginContacttodealconverstion);
			getLeadToDealCountForVendorVanityLoginAndAddToArray(session, pieChartReports, vanityUrlDetailsDto,
					vendorLeadToDealQueryForVendorVanityLoginOfPieChart);
			getLeadCreatedTimeForVendorVanityLoginAndAddToArray(session, pieChartReports, vanityUrlDetailsDto,
					vendorLatestLeadCreatedQueryForVendorVanityLoginOfPieChart);
		}

	}

	private void findLeadsStatisticsForVendorvanityLogIn(Session session,
			List<StatisticsDetailsOfPieChart> pieChartReports, VanityUrlDetailsDTO vanityUrlDetailsDto) {
		boolean isPartnerShipEstablishedWithPrm = utilDao
				.isPartnershipEstablishedOnlyWithPrm(vanityUrlDetailsDto.getUserId());
		if (isPartnerShipEstablishedWithPrm) {
			getLeadToDealCountForVendorVanityLoginAndAddToArray(session, pieChartReports, vanityUrlDetailsDto,
					patnerLeadToDealQueryForPartnerVanityLoginOfPieChart);
			getLeadCreatedTimeForVendorVanityLoginAndAddToArray(session, pieChartReports, vanityUrlDetailsDto,
					vendorLatestLeadCreatedQueryForVendorVanityLoginOfPieChart);
		} else {
			getContactToLeadCountForVendorVanityLoginAndAddToArray(session, pieChartReports, vanityUrlDetailsDto,
					partnerVendorVanityForContacttoLeadcoversion);
			getLeadToDealCountForVendorVanityLoginAndAddToArray(session, pieChartReports, vanityUrlDetailsDto,
					patnerLeadToDealQueryForPartnerVanityLoginOfPieChart);
			getLeadCreatedTimeForVendorVanityLoginAndAddToArray(session, pieChartReports, vanityUrlDetailsDto,
					vendorLatestLeadCreatedQueryForVendorVanityLoginOfPieChart);
		}
	}

	private void findLeadStatisticsForOwnVanityLoginOfPieChart(Session session, Integer userId,
			List<StatisticsDetailsOfPieChart> data, RoleDisplayDTO roleDisplayDTO) {
		boolean isPartnershipEstablishedWithonlyPrm = roleDisplayDTO
				.isPartnershipEstablishedWithPrmAndLoggedInAsPartner();
		boolean isPartnershipEstablishedWithonlyPrmWithUserId = utilDao.isPartnershipEstablishedOnlyWithPrm(userId);
		boolean isPrm = roleDisplayDTO.isPrm();
		boolean isPrmByUserId = utilDao.isPrmCompany(userId);
		if (isPartnershipEstablishedWithonlyPrm || isPartnershipEstablishedWithonlyPrmWithUserId
				|| (isPrm || isPrmByUserId)) {
			getPieChartLeadToDealConvertion(session, userId, data, vendorLeadToDealConvertion);
			getPieChartLatestLeadCreatedTime(session, userId, data, vendorLatestLeadCreatedTimePieChart,
					CREATED_LEAD_TIME);
		} else {
			getPieChartLeadToDealConvertion(session, userId, data, vendorLeadToDealConvertion);
			getPieChartLatestLeadCreatedTime(session, userId, data, vendorLatestLeadCreatedTimePieChart,
					CREATED_LEAD_TIME);
		}
	}

	private void logInAsPartnerLeadStatistics(VanityUrlDetailsDTO vanityUrlDetailsDto, Session session,
			List<StatisticsDetailsOfPieChart> pieChartReports, RoleDisplayDTO roleDisplayDTO) {
		boolean isPrmAsAdmin = utilDao.isPrmByVendorCompanyId(vanityUrlDetailsDto.getVendorCompanyId());
		boolean isPartnerPrm = utilDao.isPrmByVendorCompanyId(vanityUrlDetailsDto.getLoggedInUserCompanyId());
		if (isPrmAsAdmin) {
			if (isPartnerPrm) {
				// not show contact to lead ration
			}
			getLeadToDealCountForVendorVanityLoginAndAddToArray(session, pieChartReports, vanityUrlDetailsDto,
					logInAsPartnerLeadstodealConversion);
			getLeadCreatedTimeForVendorVanityLoginAndAddToArray(session, pieChartReports, vanityUrlDetailsDto,
					logInAsPartnerLeadCreatedTime);
		} else {
			if (roleDisplayDTO.isPrmOrPrmAndPartnerCompany()) {
				getContactToLeadCountForVendorVanityLoginAndAddToArray(session, pieChartReports, vanityUrlDetailsDto,
						logInAsPartnerPRMandPartnerContactstoleadsConversion);
			}
			getLeadToDealCountForVendorVanityLoginAndAddToArray(session, pieChartReports, vanityUrlDetailsDto,
					logInAsPartnerLeadstodealConversion);
			getLeadCreatedTimeForVendorVanityLoginAndAddToArray(session, pieChartReports, vanityUrlDetailsDto,
					logInAsPartnerLeadCreatedTime);
		}

	}

	private List<StatisticsDetailsOfPieChart> getContactToLeadCountForVendorVanityLoginAndAddToArray(Session session,
			List<StatisticsDetailsOfPieChart> pieChartReports, VanityUrlDetailsDTO vanityUrlDetailsDto,
			String queryString) {
		StatisticsDetailsOfPieChart pieChart = new StatisticsDetailsOfPieChart();
		String contactToLead = (String) getCountByQueryForVendorVanityLogin(session, vanityUrlDetailsDto, queryString);
		pieChart.setNameOfPie(CONTACT_TO_LEAD_RATIO);
		if (contactToLead.equals("0.00%") || contactToLead.equals("%")) {
			pieChart.setWeightOfPie("-");
		} else {
			pieChart.setWeightOfPie(contactToLead);
		}

		pieChartReports.add(pieChart);
		return pieChartReports;
	}

	private List<StatisticsDetailsOfPieChart> getLeadToDealCountForVendorVanityLoginAndAddToArray(Session session,
			List<StatisticsDetailsOfPieChart> pieChartReports, VanityUrlDetailsDTO vanityUrlDetailsDto,
			String queryString) {
		StatisticsDetailsOfPieChart pieChart = new StatisticsDetailsOfPieChart();
		String leadToDeal = (String) getCountByQueryForVendorVanityLogin(session, vanityUrlDetailsDto, queryString);
		pieChart.setNameOfPie(LEAD_TO_DEAL_RATIO);
		if (leadToDeal.equals("0.00%")) {
			pieChart.setWeightOfPie("-");

		} else {
			pieChart.setWeightOfPie(leadToDeal);
		}
		pieChartReports.add(pieChart);
		return pieChartReports;
	}

	private List<StatisticsDetailsOfPieChart> getLeadCreatedTimeForVendorVanityLoginAndAddToArray(Session session,
			List<StatisticsDetailsOfPieChart> pieChartReports, VanityUrlDetailsDTO vanityUrlDetailsDto,
			String queryString) {
		StatisticsDetailsOfPieChart pieChart = new StatisticsDetailsOfPieChart();
		Date createdLeadTime = (Date) getCountByQueryForVendorVanityLogin(session, vanityUrlDetailsDto, queryString);
		String convertDate = convertDateToString(createdLeadTime);
		pieChart.setNameOfPie(CREATED_LEAD_TIME);
		if (StringUtils.isEmpty(convertDate)) {
			pieChart.setWeightOfPie("-");
		} else {
			pieChart.setWeightOfPie(convertDate);
		}
		pieChartReports.add(pieChart);
		return pieChartReports;
	}

	private void findLeadStatisticsForXamplifyLoginOfPieChart(Session session,
			List<StatisticsDetailsOfPieChart> pieChartReports, Integer userId, RoleDisplayDTO roleDisplayDTO) {
		boolean isPartner = roleDisplayDTO.partnerOrPartnerTeamMember();
		boolean isPrmCompany = roleDisplayDTO.isPrmOrPrmAndPartnerCompany();
		if (isPrmCompany) {
			findLeadsStatisticsOfXamplifyPrmRoleLogged(session, pieChartReports, userId, roleDisplayDTO);
		} else if (isPartner) {
			findPieChartStatisticsForpartner(session, userId, pieChartReports);
		}
	}

	private void findLeadsStatisticsOfXamplifyPrmRoleLogged(Session session,
			List<StatisticsDetailsOfPieChart> pieChartReports, Integer userId, RoleDisplayDTO roleDisplayDTO) {
		boolean isPrm = roleDisplayDTO.isPrm();
		boolean isPrmTm = roleDisplayDTO.isAdminTeamMember();
		boolean isPrmAndPartnerTm = roleDisplayDTO.isAdminAndPartnerTeamMember();
		if ((isPrm || isPrmTm) && !isPrmAndPartnerTm) {
			getPieChartLeadToDealConvertion(session, userId, pieChartReports, vendorLeadToDealConvertion);
			getPieChartLatestLeadCreatedTime(session, userId, pieChartReports, vendorLatestLeadCreatedTimePieChart,
					CREATED_LEAD_TIME);
		} else {
			boolean isParntershipWithOnlyPrm = utilDao.isPartnershipEstablishedOnlyWithPrm(userId);
			if (isParntershipWithOnlyPrm) {
				getPieChartLeadToDealConvertion(session, userId, pieChartReports,
						VendorandPartnerxamplifyloginLeadstoDealsConversion);
				getPieChartLatestLeadCreatedTime(session, userId, pieChartReports,
						orgadminandPartnerLatestLeadCreatedTimexamplifylogin, CREATED_LEAD_TIME);
			}

		}
	}

	private void findLeadStatisticsDataForTeamMember(Session session, List<StatisticsDetailsOfPieChart> pieChartReports,
			Integer userId, RoleDisplayDTO roleDisplayDTO) {

		boolean isPartnerRole = roleDisplayDTO.partnerOrPartnerTeamMember();
		boolean isPartnershipEstablishedWithOnlyPrm = utilDao.isPartnershipEstablishedOnlyWithPrm(userId);
		boolean isPrmRole = roleDisplayDTO.isPrmOrPrmAndPartnerCompany();
		if (isPartnerRole) {
			findPieChartStatisticsForpartner(session, userId, pieChartReports);
		} else if (isPartnershipEstablishedWithOnlyPrm || isPrmRole) {
			getPieChartLeadToDealConvertion(session, userId, pieChartReports, teamMemberleadtodealConverstion);
			getPieChartLatestLeadCreatedTime(session, userId, pieChartReports, teammemberlatestCreatedLeadTime,
					CREATED_LEAD_TIME);
		} else {
			getPieChartContactToLeadConversionAndAddToArray(session, userId, pieChartReports,
					teammembercontacttoleadconvertion);
			getPieChartLeadToDealConvertion(session, userId, pieChartReports, teamMemberleadtodealConverstion);
			getPieChartLatestLeadCreatedTime(session, userId, pieChartReports, teammemberlatestCreatedLeadTime,
					CREATED_LEAD_TIME);
		}

	}

	private void findPieChartStatisticsForpartner(Session session, Integer userId,
			List<StatisticsDetailsOfPieChart> data) {
		boolean isPartnershipEstablishedWithonlyprm = utilDao.isPartnershipEstablishedOnlyWithPrm(userId);
		if (isPartnershipEstablishedWithonlyprm) {
			getPieChartLeadToDealConvertion(session, userId, data, partnerLeadToDealConversionForPieChart);
			getPieChartLatestLeadCreatedTime(session, userId, data, partnerLatestLeadCreatedTimeOfPieChart,
					CREATED_LEAD_TIME);
		} else {
			getPieChartContactToLeadConversionAndAddToArray(session, userId, data,
					partnerContactToLeadConversionForPieChart);
			getPieChartLeadToDealConvertion(session, userId, data, partnerLeadToDealConversionForPieChart);
			getPieChartLatestLeadCreatedTime(session, userId, data, partnerLatestLeadCreatedTimeOfPieChart,
					CREATED_LEAD_TIME);
		}

	}

	private List<StatisticsDetailsOfPieChart> getPieChartContactToLeadConversionAndAddToArray(Session session,
			Integer userId, List<StatisticsDetailsOfPieChart> data, String queryString) {
		StatisticsDetailsOfPieChart sDP = new StatisticsDetailsOfPieChart();
		String contactToLead = (String) getCountByQuery(session, userId, queryString);
		sDP.setNameOfPie(CONTACT_TO_LEAD_RATIO);
		if (contactToLead.equals("0.00%") || contactToLead.equalsIgnoreCase("%")) {
			sDP.setWeightOfPie("-");

		} else {
			sDP.setWeightOfPie(contactToLead);
		}

		addItemsToArrayForStatistics(sDP, new ArrayList<StatisticsDetailsOfPieChart>());
		data.add(sDP);
		return data;
	}

	private List<StatisticsDetailsOfPieChart> getPieChartLeadToDealConvertion(Session session, Integer userId,
			List<StatisticsDetailsOfPieChart> data, String queryString) {
		StatisticsDetailsOfPieChart sDP = new StatisticsDetailsOfPieChart();
		String leadToDeal = (String) getCountByQuery(session, userId, queryString);
		sDP.setNameOfPie(LEAD_TO_DEAL_RATIO);
		if (leadToDeal.equals("0.00%") || leadToDeal == "%") {
			sDP.setWeightOfPie("-");

		} else {
			sDP.setWeightOfPie(leadToDeal);
		}
		data.add(sDP);
		addItemsToArrayForStatistics(sDP, new ArrayList<StatisticsDetailsOfPieChart>());
		return data;
	}

	private void addItemsToArrayForStatistics(StatisticsDetailsOfPieChart count,
			List<StatisticsDetailsOfPieChart> array) {
		array.add(count);
	}

	private List<StatisticsDetailsOfPieChart> getPieChartLatestLeadCreatedTime(Session session, Integer userId,
			List<StatisticsDetailsOfPieChart> data, String queryString, String moduleName) {
		StatisticsDetailsOfPieChart sDP = new StatisticsDetailsOfPieChart();
		Date latestLeadTime = (Date) getCountByQuery(session, userId, queryString);
		String convertDate = convertDateToString(latestLeadTime);
		sDP.setNameOfPie(moduleName);
		if (StringUtils.isEmpty(convertDate)) {
			sDP.setWeightOfPie("-");
		} else {
			sDP.setWeightOfPie(convertDate);
		}
		data.add(sDP);
		return data;
	}

	private String convertDateToString(Date date) {
		String time = DateUtils.getUtcString(date);
		return time;

	}

	@Override
	public List<StatisticsDetailsOfPieChart> getPieChartDealStatisticsData(VanityUrlDetailsDTO vanityUrlDetailsDto,
			RoleDisplayDTO roleDisplayDto) {
		try {
			Session session = sessionFactory.getCurrentSession();
			List<StatisticsDetailsOfPieChart> pieChartReports = new ArrayList<StatisticsDetailsOfPieChart>();

			Integer userId = vanityUrlDetailsDto.getUserId();
			boolean applyFilter = vanityUrlDetailsDto.isApplyFilter();
			TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(userId, applyFilter, false);
			boolean applyTeamMemberFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
			boolean isPartnerTm = roleDisplayDto.partnerOrPartnerTeamMember();
			if (teamMemberFilterDTO.isEmptyFilter()) {
				if (isPartnerTm || vanityUrlDetailsDto.isVanityUrlFilter()) {
					if (vanityUrlDetailsDto.isPartnerLoggedInThroughVanityUrl()) {
						findDealsStatisticsForPartnerVanityLoginOfPieChart(vanityUrlDetailsDto, session,
								pieChartReports);
					} else if (vanityUrlDetailsDto.getLoginAsUserId() != null
							&& vanityUrlDetailsDto.getLoginAsUserId() > 0) {
						Integer partnerCompanyId = userDao.getCompanyIdByUserId(vanityUrlDetailsDto.getUserId());
						Integer vendorCompanyId = userDao.getCompanyIdByUserId(vanityUrlDetailsDto.getLoginAsUserId());
						vanityUrlDetailsDto.setLoggedInUserCompanyId(partnerCompanyId);
						vanityUrlDetailsDto.setVendorCompanyId(vendorCompanyId);
						if (roleDisplayDto.partnerOrPartnerTeamMember()) {
							findDealsStatisticsForPartnerVanityLoginOfPieChart(vanityUrlDetailsDto, session,
									pieChartReports);
						}
					} else {
						findDealStatisticsForPartnerTeamMemberOfPieChart(session, pieChartReports, userId,
								roleDisplayDto);
					}
				} else {
					return new ArrayList<StatisticsDetailsOfPieChart>();
				}
			} else {
				if (applyTeamMemberFilter) {
					if (vanityUrlDetailsDto.isPartnerLoggedInThroughVanityUrl()) {
						findDealsStatisticsForPartnerVanityLoginOfPieChart(vanityUrlDetailsDto, session,
								pieChartReports);
					} else {
						findDealStatisticsForTeamMemberOfPieChart(session, pieChartReports, userId, roleDisplayDto);
					}
				} else {
					if (vanityUrlDetailsDto.isVendorLoggedInThroughOwnVanityUrl()) {
						findDealStatisticsForOwnVanityLoginOfPieChart(session, pieChartReports, userId);
					}
					/**** LOgin in as Partner ******/
					else if (vanityUrlDetailsDto.getLoginAsUserId() != null
							&& vanityUrlDetailsDto.getLoginAsUserId() > 0) {
						Integer partnerCompanyId = userDao.getCompanyIdByUserId(vanityUrlDetailsDto.getUserId());
						Integer vendorCompanyId = userDao.getCompanyIdByUserId(vanityUrlDetailsDto.getLoginAsUserId());
						vanityUrlDetailsDto.setLoggedInUserCompanyId(partnerCompanyId);
						vanityUrlDetailsDto.setVendorCompanyId(vendorCompanyId);
						if (roleDisplayDto.partnerOrPartnerTeamMember()) {
							findDealsStatisticsForPartnerVanityLoginOfPieChart(vanityUrlDetailsDto, session,
									pieChartReports);
						} else {
							logInAsPartnerDealStatistics(vanityUrlDetailsDto, session, pieChartReports);
						}
					}
					/**** LOgin in as Partner ******/
					else if (vanityUrlDetailsDto.isPartnerLoggedInThroughVanityUrl()) {
						findDealsStatisticsForPartnerVanityLoginOfPieChart(vanityUrlDetailsDto, session,
								pieChartReports);
					} else {
						findDealStatisticsForXamplifyLoginOfPieChart(session, pieChartReports, userId, roleDisplayDto);
					}
				}
			}
			return pieChartReports;
		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex);
		}

	}

	private void findDealStatisticsForPartnerTeamMemberOfPieChart(Session session,
			List<StatisticsDetailsOfPieChart> pieChartReports, Integer userId, RoleDisplayDTO roleDisplayDto) {
		getPieChartOpportunityDealAmount(session, pieChartReports, userId, partnerDealOpportunityAmountOfPieChart);
		getPieChartLeadToDealConvertion(session, userId, pieChartReports, partnerLeadToDealConversionForPieChart);

	}

	private void findDealStatisticsForOwnVanityLoginOfPieChart(Session session,
			List<StatisticsDetailsOfPieChart> pieChartReports, Integer userId) {
		getPieChartOpportunityDealAmount(session, pieChartReports, userId, vendorDealOpportunityAmountOfPieChart);
		getPieChartLeadToDealConvertion(session, userId, pieChartReports,
				OrgadminandPartnerownvanityLeadtoDealConversion);
		getPieChartLatestLeadCreatedTime(session, userId, pieChartReports, vendorDealLatestCreatedTimeOfPieChart,
				CREATED_DEAL_TIME);

	}

	private void findDealsStatisticsForPartnerVanityLoginOfPieChart(VanityUrlDetailsDTO vanityUrlDetailsDto,
			Session session, List<StatisticsDetailsOfPieChart> pieChartReports) {
		getDealOpportunityAmountForVendorVanityLoginAndAddToArray(session, pieChartReports, vanityUrlDetailsDto,
				patnerDealOpportunityAmountQueryForPartnerVanityLoginOfPieChart);
		getLeadToDealCountForVendorVanityLoginAndAddToArray(session, pieChartReports, vanityUrlDetailsDto,
				patnerLeadToDealQueryForPartnerVanityLoginOfPieChart);
		getLatestDealCreatedTimeForVendorVanityLoginAndAddToArray(session, pieChartReports, vanityUrlDetailsDto,
				partnerLatestDealCreatedTimeQueryForPartnerVanityLoginOfPieChart);
	}

	private void logInAsPartnerDealStatistics(VanityUrlDetailsDTO vanityUrlDetailsDto, Session session,
			List<StatisticsDetailsOfPieChart> pieChartReports) {
		getDealOpportunityAmountForVendorVanityLoginAndAddToArray(session, pieChartReports, vanityUrlDetailsDto,
				logInAsPartnerOpportunityAmount);
		getLeadToDealCountForVendorVanityLoginAndAddToArray(session, pieChartReports, vanityUrlDetailsDto,
				logInAsPartnerLeadstodealConversion);
		getLatestDealCreatedTimeForVendorVanityLoginAndAddToArray(session, pieChartReports, vanityUrlDetailsDto,
				logInAsPartnerDealCreatedTime);
	}

	private List<StatisticsDetailsOfPieChart> getDealOpportunityAmountForVendorVanityLoginAndAddToArray(Session session,
			List<StatisticsDetailsOfPieChart> pieChartReports, VanityUrlDetailsDTO vanityUrlDetailsDto,
			String queryString) {
		StatisticsDetailsOfPieChart piechart = new StatisticsDetailsOfPieChart();
		Double leadToDeal = (Double) getCountByQueryForVendorVanityLogin(session, vanityUrlDetailsDto, queryString);
		String opportunityAmount = "$ " + NumberFormatterString.formatValueInTrillionsOrBillions(leadToDeal);
		piechart.setNameOfPie(OPPORTUNITY_AMOUNT);
		if (opportunityAmount.equals("$ 0")) {
			piechart.setWeightOfPie("-");
		} else {
			piechart.setWeightOfPie(opportunityAmount);
		}
		pieChartReports.add(piechart);
		return pieChartReports;
	}

	private List<StatisticsDetailsOfPieChart> getLatestDealCreatedTimeForVendorVanityLoginAndAddToArray(Session session,
			List<StatisticsDetailsOfPieChart> pieChartReports, VanityUrlDetailsDTO vanityUrlDetailsDto,
			String queryString) {
		StatisticsDetailsOfPieChart pieChart = new StatisticsDetailsOfPieChart();
		Date leadToDeal = (Date) getCountByQueryForVendorVanityLogin(session, vanityUrlDetailsDto, queryString);
		String convertString = convertDateToString(leadToDeal);
		pieChart.setNameOfPie(CREATED_DEAL_TIME);
		if (StringUtils.isEmpty(convertString)) {
			pieChart.setWeightOfPie("-");
		} else {
			pieChart.setWeightOfPie(convertString);
		}
		pieChartReports.add(pieChart);
		return pieChartReports;
	}

	private void findDealStatisticsForTeamMemberOfPieChart(Session session,
			List<StatisticsDetailsOfPieChart> pieChartReports, Integer userId, RoleDisplayDTO roleDisplayDTO) {

		boolean isPartnerRole = roleDisplayDTO.partnerOrPartnerTeamMember();
		if (isPartnerRole) {
			findPieChartDealStatisticsForPartner(session, pieChartReports, userId, isPartnerRole);
		} else {
			getPieChartOpportunityDealAmount(session, pieChartReports, userId, teamMemberDealOpportunityAmount);
			getPieChartLeadToDealConvertion(session, userId, pieChartReports, teamMemberleadtodealConverstion);
			getPieChartLatestLeadCreatedTime(session, userId, pieChartReports, teammembercreatedDealtime,
					CREATED_DEAL_TIME);
		}

	}

	private void findPieChartDealStatisticsForVenor(Session session, List<StatisticsDetailsOfPieChart> pieChartReports,
			Integer userId) {
		getPieChartOpportunityDealAmount(session, pieChartReports, userId, vendorDealOpportunityAmountOfPieChart);
		getPieChartLeadToDealConvertion(session, userId, pieChartReports, vendorLeadToDealConversionPieChart);
		getPieChartLatestLeadCreatedTime(session, userId, pieChartReports, vendorDealLatestCreatedTimeOfPieChart,
				CREATED_DEAL_TIME);

	}

	private void findPieChartDealStatisticsForPartner(Session session,
			List<StatisticsDetailsOfPieChart> pieChartReports, Integer userId, boolean isPartner) {
		getPieChartOpportunityDealAmount(session, pieChartReports, userId, partnerDealOpportunityAmountOfPieChart);
		getPieChartLeadToDealConvertion(session, userId, pieChartReports, partnerLeadToDealConversionForPieChart);
		getPieChartLatestLeadCreatedTime(session, userId, pieChartReports, partnerLatestDealCreatedTimeOfPieChart,
				CREATED_DEAL_TIME);

	}

	private List<StatisticsDetailsOfPieChart> getPieChartOpportunityDealAmount(Session session,
			List<StatisticsDetailsOfPieChart> pieChartReports, Integer userId, String queryString) {
		StatisticsDetailsOfPieChart pieChart = new StatisticsDetailsOfPieChart();
		Double opportunityAmountDouble = (Double) getCountByQuery(session, userId, queryString);
		String opportunityAmount = "$ "
				+ NumberFormatterString.formatValueInTrillionsOrBillions(opportunityAmountDouble);
		pieChart.setNameOfPie(OPPORTUNITY_AMOUNT);
		if (opportunityAmount.equals("$ 0")) {
			pieChart.setWeightOfPie("-");
		} else {

			pieChart.setWeightOfPie(opportunityAmount);
		}
		pieChartReports.add(pieChart);
		return pieChartReports;
	}

	@Override
	public List<DealStatisticsDTO> findDealsWithStageNames(VanityUrlDetailsDTO vanityUrlDetailsDto,
			RoleDisplayDTO roleDisplayDto) {
		try {
			Session session = sessionFactory.getCurrentSession();
			List<DealStatisticsDTO> stagenamesWithCountList = new ArrayList<>();
			Integer userId = vanityUrlDetailsDto.getUserId();
			boolean applyFilter = vanityUrlDetailsDto.isApplyFilter();
			TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(userId, applyFilter, false);
			boolean applyTeamMemberFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
			boolean isPartnerTm = roleDisplayDto.partnerOrPartnerTeamMember();
			if (teamMemberFilterDTO.isEmptyFilter()) {
				if (isPartnerTm || vanityUrlDetailsDto.isVanityUrlFilter()) {
					if (vanityUrlDetailsDto.isPartnerLoggedInThroughVanityUrl()) {
						findDealsNamesWithCountForVendorVanityLogin(session, stagenamesWithCountList, userId,
								vanityUrlDetailsDto);
					} else if (vanityUrlDetailsDto.getLoginAsUserId() != null
							&& vanityUrlDetailsDto.getLoginAsUserId() > 0) {
						Integer partnerCompanyId = userDao.getCompanyIdByUserId(vanityUrlDetailsDto.getUserId());
						Integer vendorCompanyId = userDao.getCompanyIdByUserId(vanityUrlDetailsDto.getLoginAsUserId());
						vanityUrlDetailsDto.setLoggedInUserCompanyId(partnerCompanyId);
						vanityUrlDetailsDto.setVendorCompanyId(vendorCompanyId);

						if (roleDisplayDto.partnerOrPartnerTeamMember()) {
							findDealsNamesWithCountForVendorVanityLogin(session, stagenamesWithCountList, userId,
									vanityUrlDetailsDto);
						}
					} else {
						findStageNamesOfDealsForPartnerOfTeamMember(session, stagenamesWithCountList, userId);
					}
				} else {
					return new ArrayList<>();
				}
			} else {
				if (applyTeamMemberFilter) {
					if (vanityUrlDetailsDto.isPartnerLoggedInThroughVanityUrl()) {
						findDealsNamesWithCountForVendorVanityLogin(session, stagenamesWithCountList, userId,
								vanityUrlDetailsDto);
					} else {
						findStageNamesOfDealsForVendorOfTeamMember(session, stagenamesWithCountList, userId,
								roleDisplayDto);
					}
				} else {
					if (vanityUrlDetailsDto.isVendorLoggedInThroughOwnVanityUrl()) {
						findDealsNamesWithCountForOwnVanityLogin(session, stagenamesWithCountList, userId);
					}
					/**** LOgin in as Partner ******/
					else if (vanityUrlDetailsDto.getLoginAsUserId() != null
							&& vanityUrlDetailsDto.getLoginAsUserId() > 0) {
						Integer partnerCompanyId = userDao.getCompanyIdByUserId(vanityUrlDetailsDto.getUserId());
						Integer vendorCompanyId = userDao.getCompanyIdByUserId(vanityUrlDetailsDto.getLoginAsUserId());
						vanityUrlDetailsDto.setLoggedInUserCompanyId(partnerCompanyId);
						vanityUrlDetailsDto.setVendorCompanyId(vendorCompanyId);
						if (roleDisplayDto.partnerOrPartnerTeamMember()) {
							findDealsNamesWithCountForVendorVanityLogin(session, stagenamesWithCountList, userId,
									vanityUrlDetailsDto);
						} else {
							logInAsPartnerStageNamesWithDeals(session, stagenamesWithCountList, userId,
									vanityUrlDetailsDto);
						}
					}
					/**** LOgin in as Partner ******/
					else if (vanityUrlDetailsDto.isPartnerLoggedInThroughVanityUrl()) {
						findDealsNamesWithCountForVendorVanityLogin(session, stagenamesWithCountList, userId,
								vanityUrlDetailsDto);
					} else {
						findDealsNamesWithCountForForXamplifyLogin(session, stagenamesWithCountList, userId,
								roleDisplayDto);
					}
				}
			}
			return stagenamesWithCountList;
		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	private List<DealStatisticsDTO> findStageNamesOfDealsForPartnerOfTeamMember(Session session,
			List<DealStatisticsDTO> stagenamesWithCountList, Integer userId) {
		SQLQuery query = session.createSQLQuery(OrgAdminandPartnerDealStatuswiseDealsxamplifylogin);
		query.setParameter(USER_ID, userId);
		stagenamesWithCountList
				.addAll(query.setResultTransformer(Transformers.aliasToBean(DealStatisticsDTO.class)).list());
		return stagenamesWithCountList;
	}

	@SuppressWarnings("unchecked")
	private List<DealStatisticsDTO> findStageNamesOfDealsForVendorOfTeamMember(Session session,
			List<DealStatisticsDTO> stagenamesWithCountList, Integer userId, RoleDisplayDTO roleDisplayDTO) {

		boolean ispartner = roleDisplayDTO.partnerOrPartnerTeamMember();
		if (ispartner) {
			SQLQuery query = session.createSQLQuery(partnerStatusWiseDealsOfPiehart);
			query.setParameter(USER_ID, userId);
			stagenamesWithCountList
					.addAll(query.setResultTransformer(Transformers.aliasToBean(DealStatisticsDTO.class)).list());
		} else {
			SQLQuery query = session.createSQLQuery(teammemberstagesWiseDeals);
			query.setParameter(USER_ID, userId);
			stagenamesWithCountList
					.addAll(query.setResultTransformer(Transformers.aliasToBean(DealStatisticsDTO.class)).list());
		}
		return stagenamesWithCountList;
	}

	@SuppressWarnings("unchecked")
	private List<DealStatisticsDTO> findDealsNamesWithCountForForXamplifyLogin(Session session,
			List<DealStatisticsDTO> stagenamesWithCountList, Integer userId, RoleDisplayDTO roleDisplayDto) {
		boolean isPartner = roleDisplayDto.partnerOrPartnerTeamMember();
		boolean isPrm = roleDisplayDto.isPrm();
		if (isPrm) {
			SQLQuery query = session.createSQLQuery(vendorStatusWiseDealsOfPiehart);
			query.setParameter(USER_ID, userId);
			stagenamesWithCountList
					.addAll(query.setResultTransformer(Transformers.aliasToBean(DealStatisticsDTO.class)).list());
			return stagenamesWithCountList;
		} else if (isPartner) {
			SQLQuery query = session.createSQLQuery(partnerStatusWiseDealsOfPiehart);
			query.setParameter(USER_ID, userId);
			stagenamesWithCountList
					.addAll(query.setResultTransformer(Transformers.aliasToBean(DealStatisticsDTO.class)).list());
			return stagenamesWithCountList;
		} else {

			SQLQuery query = session.createSQLQuery(OrgAdminandPartnerDealStatuswiseDealsxamplifylogin);
			query.setParameter(USER_ID, userId);
			stagenamesWithCountList
					.addAll(query.setResultTransformer(Transformers.aliasToBean(DealStatisticsDTO.class)).list());
			return stagenamesWithCountList;
		}
	}

	@SuppressWarnings("unchecked")
	private List<DealStatisticsDTO> findDealsNamesWithCountForOwnVanityLogin(Session session,
			List<DealStatisticsDTO> stagenamesWithCountList, Integer userId) {
		SQLQuery query = session.createSQLQuery(vendorStatusWiseDealsOfPiehart);
		query.setParameter(USER_ID, userId);
		stagenamesWithCountList
				.addAll(query.setResultTransformer(Transformers.aliasToBean(DealStatisticsDTO.class)).list());
		return stagenamesWithCountList;
	}

	@SuppressWarnings("unchecked")
	private List<DealStatisticsDTO> findDealsNamesWithCountForVendorVanityLogin(Session session,
			List<DealStatisticsDTO> stagenamesWithCountList, Integer userId, VanityUrlDetailsDTO vanityUrlDetailsDto) {
		SQLQuery query = session.createSQLQuery(vendorStatuswiseDealQueryForVendorVanityLoginOfPieChart);
		query.setParameter("vendorCompanyId", vanityUrlDetailsDto.getVendorCompanyId());
		query.setParameter("partnerCompanyId", vanityUrlDetailsDto.getLoggedInUserCompanyId());
		stagenamesWithCountList
				.addAll(query.setResultTransformer(Transformers.aliasToBean(DealStatisticsDTO.class)).list());
		return stagenamesWithCountList;
	}

	@SuppressWarnings("unchecked")
	private List<DealStatisticsDTO> logInAsPartnerStageNamesWithDeals(Session session,
			List<DealStatisticsDTO> stagenamesWithCountList, Integer userId, VanityUrlDetailsDTO vanityUrlDetailsDto) {
		SQLQuery query = session.createSQLQuery(logInAsPartnerStageswiseDeals);
		query.setParameter("vendorCompanyId", vanityUrlDetailsDto.getVendorCompanyId());
		query.setParameter("partnerCompanyId", vanityUrlDetailsDto.getLoggedInUserCompanyId());
		stagenamesWithCountList
				.addAll(query.setResultTransformer(Transformers.aliasToBean(DealStatisticsDTO.class)).list());
		return stagenamesWithCountList;
	}

	private void findDealStatisticsForXamplifyLoginOfPieChart(Session session,
			List<StatisticsDetailsOfPieChart> pieChartReports, Integer userId, RoleDisplayDTO roleDisplayDTO) {
		boolean isPartner = roleDisplayDTO.partnerOrPartnerTeamMember();
		boolean isAdminRole = roleDisplayDTO.isAdminRole();
		boolean isAdminTm = roleDisplayDTO.isAdminTeamMember();
		boolean isAdminAndPartnerTM = roleDisplayDTO.isAdminAndPartnerTeamMember();

		if ((isAdminRole || isAdminTm) && (!isAdminAndPartnerTM)) {
			findPieChartDealStatisticsForVenor(session, pieChartReports, userId);
		} else if (isPartner) {
			findPieChartDealStatisticsForPartner(session, pieChartReports, userId, isPartner);
		} else {
			findDealStatisticsForXamplifyLogInForWithPartner(userId, session, pieChartReports);
		}

	}

	private void findDealStatisticsForXamplifyLogInForWithPartner(Integer userId, Session session,
			List<StatisticsDetailsOfPieChart> pieChartReports) {
		getPieChartOpportunityDealAmount(session, pieChartReports, userId,
				VendorandPartnerxamplifyloginDealOpportunityAmount);
		getPieChartLeadToDealConvertion(session, userId, pieChartReports,
				VendorandPartnerxamplifyloginLeadstoDealsConversion);
	}

	@Override
	public List<DealStatisticsDTO> findLeadsWithStageNames(VanityUrlDetailsDTO vanityUrlDetailsDto,
			RoleDisplayDTO roleDisplayDTO) {
		try {
			Session session = sessionFactory.getCurrentSession();
			List<DealStatisticsDTO> stagenamesWithCountList = new ArrayList<>();
			Integer userId = vanityUrlDetailsDto.getUserId();
			boolean applyFilter = vanityUrlDetailsDto.isApplyFilter();
			TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(userId, applyFilter, false);
			boolean applyTeamMemberFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
			boolean isPartnerTm = roleDisplayDTO.partnerOrPartnerTeamMember();
			if (teamMemberFilterDTO.isEmptyFilter()) {
				if (isPartnerTm || vanityUrlDetailsDto.isVanityUrlFilter()) {
					if (vanityUrlDetailsDto.isPartnerLoggedInThroughVanityUrl()) {
						findLeadsNamesWithCountForVendorVanityLogin(session, stagenamesWithCountList,
								vanityUrlDetailsDto);
					} else if (vanityUrlDetailsDto.getLoginAsUserId() != null
							&& vanityUrlDetailsDto.getLoginAsUserId() > 0) {
						Integer partnerCompanyId = userDao.getCompanyIdByUserId(vanityUrlDetailsDto.getUserId());
						Integer vendorCompanyId = userDao.getCompanyIdByUserId(vanityUrlDetailsDto.getLoginAsUserId());
						vanityUrlDetailsDto.setLoggedInUserCompanyId(partnerCompanyId);
						vanityUrlDetailsDto.setVendorCompanyId(vendorCompanyId);

						if (roleDisplayDTO.partnerOrPartnerTeamMember()) {
							findLeadsNamesWithCountForVendorVanityLogin(session, stagenamesWithCountList,
									vanityUrlDetailsDto);
						}
					} else {
						findStageNamesForLeadForPartnerTeamMember(session, stagenamesWithCountList, userId);
					}
				} else {
					return new ArrayList<>();
				}
			} else {
				if (applyTeamMemberFilter) {
					if (vanityUrlDetailsDto.isPartnerLoggedInThroughVanityUrl()) {
						findLeadsNamesWithCountForVendorVanityLogin(session, stagenamesWithCountList,
								vanityUrlDetailsDto);
					} else {
						findStageNamesForLeadVendorOfTeamMember(session, stagenamesWithCountList, userId,
								roleDisplayDTO);
					}
				} else {
					if (vanityUrlDetailsDto.isVendorLoggedInThroughOwnVanityUrl()) {
						findLeadsNamesWithCountForOwnVanityLogin(session, stagenamesWithCountList, userId);
					}
					/**** LOgin in as Partner ******/
					else if (vanityUrlDetailsDto.getLoginAsUserId() != null
							&& vanityUrlDetailsDto.getLoginAsUserId() > 0) {
						Integer partnerCompanyId = userDao.getCompanyIdByUserId(vanityUrlDetailsDto.getUserId());
						Integer vendorCompanyId = userDao.getCompanyIdByUserId(vanityUrlDetailsDto.getLoginAsUserId());
						vanityUrlDetailsDto.setLoggedInUserCompanyId(partnerCompanyId);
						vanityUrlDetailsDto.setVendorCompanyId(vendorCompanyId);
						if (roleDisplayDTO.partnerOrPartnerTeamMember()) {
							findLeadsNamesWithCountForVendorVanityLogin(session, stagenamesWithCountList,
									vanityUrlDetailsDto);
						} else {
							logInAsPartnerLeadsWithStageNames(session, stagenamesWithCountList, vanityUrlDetailsDto);
						}
					}
					/**** LOgin in as Partner ******/
					else if (vanityUrlDetailsDto.isPartnerLoggedInThroughVanityUrl()) {
						findLeadsNamesWithCountForVendorVanityLogin(session, stagenamesWithCountList,
								vanityUrlDetailsDto);
					} else {
						findLeadsNamesWithCountForForXamplifyLogin(session, stagenamesWithCountList, userId,
								roleDisplayDTO);
					}
				}
			}
			return stagenamesWithCountList;
		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	private List<DealStatisticsDTO> findLeadsNamesWithCountForOwnVanityLogin(Session session,
			List<DealStatisticsDTO> stagenamesWithCountList, Integer userId) {
		SQLQuery query = session.createSQLQuery(vendorStatusWiseLeadsOfPieChart);
		query.setParameter(USER_ID, userId);
		stagenamesWithCountList
				.addAll(query.setResultTransformer(Transformers.aliasToBean(DealStatisticsDTO.class)).list());
		return stagenamesWithCountList;
	}

	@SuppressWarnings("unchecked")
	private List<DealStatisticsDTO> findStageNamesForLeadForPartnerTeamMember(Session session,
			List<DealStatisticsDTO> stagenamesWithCountList, Integer userId) {
		SQLQuery query = session.createSQLQuery(OrgAdminandPartnerLeadsStatuswiseDealsxamplifylogin);
		query.setParameter(USER_ID, userId);
		stagenamesWithCountList
				.addAll(query.setResultTransformer(Transformers.aliasToBean(DealStatisticsDTO.class)).list());
		return stagenamesWithCountList;

	}

	@SuppressWarnings("unchecked")
	private List<DealStatisticsDTO> findStageNamesForLeadVendorOfTeamMember(Session session,
			List<DealStatisticsDTO> stagenamesWithCountList, Integer userId, RoleDisplayDTO roleDisplayDTO) {
		boolean ispartner = roleDisplayDTO.partnerOrPartnerTeamMember();
		if (ispartner) {
			SQLQuery query = session.createSQLQuery(partnerStatusWiseLeadsOfPieChart);
			query.setParameter(USER_ID, userId);
			stagenamesWithCountList
					.addAll(query.setResultTransformer(Transformers.aliasToBean(DealStatisticsDTO.class)).list());
		} else {
			SQLQuery query = session.createSQLQuery(teammemberLeadsStatusWiseLeads);
			query.setParameter(USER_ID, userId);
			stagenamesWithCountList
					.addAll(query.setResultTransformer(Transformers.aliasToBean(DealStatisticsDTO.class)).list());
		}

		return stagenamesWithCountList;
	}

	@SuppressWarnings("unchecked")
	private List<DealStatisticsDTO> findLeadsNamesWithCountForForXamplifyLogin(Session session,
			List<DealStatisticsDTO> stagenamesWithCountList, Integer userId, RoleDisplayDTO roleDisplayDto) {
		boolean isPartner = roleDisplayDto.partnerOrPartnerTeamMember();
		boolean isPrm = roleDisplayDto.isPrm();
		if (isPrm) {
			SQLQuery query = session.createSQLQuery(vendorStatusWiseLeadsOfPieChart);
			query.setParameter(USER_ID, userId);
			stagenamesWithCountList
					.addAll(query.setResultTransformer(Transformers.aliasToBean(DealStatisticsDTO.class)).list());
			return stagenamesWithCountList;
		} else if (isPartner) {
			SQLQuery query = session.createSQLQuery(partnerStatusWiseLeadsOfPieChart);
			query.setParameter(USER_ID, userId);
			stagenamesWithCountList
					.addAll(query.setResultTransformer(Transformers.aliasToBean(DealStatisticsDTO.class)).list());
			return stagenamesWithCountList;
		} else {
			SQLQuery query = session.createSQLQuery(OrgAdminandPartnerLeadsStatuswiseDealsxamplifylogin);
			query.setParameter(USER_ID, userId);
			stagenamesWithCountList
					.addAll(query.setResultTransformer(Transformers.aliasToBean(DealStatisticsDTO.class)).list());
			return stagenamesWithCountList;
		}
	}

	@SuppressWarnings("unchecked")
	private List<DealStatisticsDTO> logInAsPartnerLeadsWithStageNames(Session session,
			List<DealStatisticsDTO> stagenamesWithCountList, VanityUrlDetailsDTO vanityUrlDetailsDto) {
		SQLQuery query = session.createSQLQuery(logInAsPartnerStageswiseLeads);
		query.setParameter("vendorCompanyId", vanityUrlDetailsDto.getVendorCompanyId());
		query.setParameter("partnerCompanyId", vanityUrlDetailsDto.getLoggedInUserCompanyId());
		stagenamesWithCountList
				.addAll(query.setResultTransformer(Transformers.aliasToBean(DealStatisticsDTO.class)).list());
		return stagenamesWithCountList;
	}

	@SuppressWarnings("unchecked")
	private List<DealStatisticsDTO> findLeadsNamesWithCountForVendorVanityLogin(Session session,
			List<DealStatisticsDTO> stagenamesWithCountList, VanityUrlDetailsDTO vanityUrlDetailsDto) {
		SQLQuery query = session.createSQLQuery(vendorStatuswiseLeadsQueryForVendorVanityLoginOfPieChart);
		query.setParameter("vendorCompanyId", vanityUrlDetailsDto.getVendorCompanyId());
		query.setParameter("partnerCompanyId", vanityUrlDetailsDto.getLoggedInUserCompanyId());
		stagenamesWithCountList
				.addAll(query.setResultTransformer(Transformers.aliasToBean(DealStatisticsDTO.class)).list());
		return stagenamesWithCountList;
	}

	@Override
	public PaginatedDTO findAllQuickLinksForVendor(Pagination pagination, String search,
			LeftSideNavigationBarItem leftsideNavigationBarItem) {
		boolean isDamAccess = leftsideNavigationBarItem.isDam();
		boolean isLmsAccess = leftsideNavigationBarItem.isLms();
		boolean isPlayBookAccess = leftsideNavigationBarItem.isPlaybook();
		if (isDamAccess || isLmsAccess || isPlayBookAccess) {
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = setQueryByAccess(isDamAccess,
					isLmsAccess, isPlayBookAccess, instantNavigationLinksVendorDamQuery,
					instantNavigationLinksVendorTrackQuery, instantNavigationLinksVendorPlaybookQuery, search,
					pagination);
			if (StringUtils.hasText(hibernateSQLQueryResultRequestDTO.getQueryString())) {
				List<QueryParameterDTO> queryParameterDTOs = new ArrayList<>();
				queryParameterDTOs.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, pagination.getCompanyId()));
				hibernateSQLQueryResultRequestDTO.setQueryParameterDTOs(queryParameterDTOs);
				hibernateSQLQueryResultRequestDTO.setClassInstance(InstantNavigationLinksDTO.class);
				hibernateSQLQueryResultRequestDTO.setSortQueryString("order by 3 desc nulls last");
				return hibernateSQLQueryResultUtilDao.returnPaginatedDTO(hibernateSQLQueryResultRequestDTO, pagination,
						search);
			} else {
				return new PaginatedDTO();
			}

		} else {
			return new PaginatedDTO();
		}

	}

	private HibernateSQLQueryResultRequestDTO setQueryByAccess(boolean isDamAccess, boolean isLmsAccess,
			boolean isPlayBookAccess, String damQuery, String trackQuery, String playbookQuery, String searchKey,
			Pagination pagination) {
		String queryString = "";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		boolean hasSearchKey = StringUtils.hasText(searchKey);
		searchKey = XamplifyUtils.escapeSingleQuotesForSearchQuery(searchKey);
		searchKey = XamplifyUtils.addBackSlashToSpecialCharacters(searchKey);
		String filterBy = pagination.getFilterBy();
		filterBy = StringUtils.hasText(filterBy) ? filterBy : "All";
		boolean isAssetFilterApplied = "Assets".equalsIgnoreCase(pagination.getFilterBy())
				|| "All".equalsIgnoreCase(filterBy);
		boolean isTrackFilterApplied = "Tracks".equalsIgnoreCase(pagination.getFilterBy())
				|| "All".equalsIgnoreCase(filterBy);
		boolean isPlayBookFilterApplied = "PlayBooks".equalsIgnoreCase(pagination.getFilterBy())
				|| "All".equalsIgnoreCase(filterBy);

		queryString = searchAssetsQueryString(isDamAccess, damQuery, searchKey, queryString, hasSearchKey,
				isAssetFilterApplied);
		queryString = searchTracksOrPlayBooksQueryString(isLmsAccess, trackQuery, searchKey, queryString, hasSearchKey,
				isTrackFilterApplied);
		queryString = searchTracksOrPlayBooksQueryString(isPlayBookAccess, playbookQuery, searchKey, queryString,
				hasSearchKey, isPlayBookFilterApplied);

		StringBuilder queryStringBuilder = new StringBuilder(queryString);
		if (queryString.endsWith(XamplifyConstants.UNION)) {
			queryStringBuilder.replace(queryString.lastIndexOf(XamplifyConstants.UNION),
					queryString.lastIndexOf(XamplifyConstants.UNION) + XamplifyConstants.UNION.length(), "");
		}
		String updatedQueryString = String.valueOf(queryStringBuilder);
		hibernateSQLQueryResultRequestDTO.setQueryString(updatedQueryString);
		return hibernateSQLQueryResultRequestDTO;
	}

	private String searchTracksOrPlayBooksQueryString(boolean isLmsAccess, String trackQuery, String searchKey,
			String queryString, boolean hasSearchKey, boolean isTrackFilterApplied) {
		if (isLmsAccess && isTrackFilterApplied) {
			String searchQuery = "";
			if (hasSearchKey) {
				searchQuery = "and (LOWER(title) like LOWER('%" + searchKey + "%'))";
			}
			queryString += trackQuery.replace(SEARCH_QUERY_STRING, searchQuery) + " " + XamplifyConstants.UNION;
		}
		return queryString;
	}

	private String searchAssetsQueryString(boolean isDamAccess, String damQuery, String searchKey, String queryString,
			boolean hasSearchKey, boolean isAssetFilterApplied) {
		if (isDamAccess && isAssetFilterApplied) {
			String searchQuery = "";
			if (hasSearchKey) {
				searchQuery = "and (LOWER(d.asset_name) like LOWER('%" + searchKey + "%'))";
			}
			queryString = damQuery.replace(SEARCH_QUERY_STRING, searchQuery) + " " + XamplifyConstants.UNION;
		}
		return queryString;
	}

	@Override
	public PaginatedDTO findAllQuickLinksForPartner(Pagination pagination, String search,
			LeftSideNavigationBarItem leftSideNavigationBarItem, VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		boolean isDamAccessAsPartner = leftSideNavigationBarItem.isDamAccessAsPartner();
		boolean isLmsAccessAsPartner = leftSideNavigationBarItem.isLmsAccessAsPartner();
		boolean isPlayBookAccessAsPartner = leftSideNavigationBarItem.isPlaybookAccessAsPartner();
		if (isDamAccessAsPartner || isLmsAccessAsPartner || isPlayBookAccessAsPartner) {
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = setQueryByAccess(isDamAccessAsPartner,
					isLmsAccessAsPartner, isPlayBookAccessAsPartner, instantNavigationLinksPartnerDamQuery,
					instantNavigationLinksPartnerTrackQuery, instantNavigationLinksPartnerPlaybookQuery, search,
					pagination);
			if (StringUtils.hasText(hibernateSQLQueryResultRequestDTO.getQueryString())) {
				Integer vendorCompanyId = vanityUrlDetailsDTO.getVendorCompanyId();
				Integer partnerCompanyId = vanityUrlDetailsDTO.getLoggedInUserCompanyId();
				Integer partnershipId = partnershipDao
						.getPartnershipIdByVendorCompanyIdAndPartnerCompanyId(vendorCompanyId, partnerCompanyId);
				List<QueryParameterDTO> queryParameterDTOs = new ArrayList<>();
				queryParameterDTOs
						.add(new QueryParameterDTO("vendorCompanyId", vanityUrlDetailsDTO.getVendorCompanyId()));
				queryParameterDTOs.add(new QueryParameterDTO("loggedInUserId", vanityUrlDetailsDTO.getUserId()));
				boolean hasPartnershipIdParameter = hibernateSQLQueryResultRequestDTO.getQueryString()
						.indexOf(":partnershipId") > -1;
				if (hasPartnershipIdParameter) {
					if (partnershipId == null) {
						partnershipId = 0;
					}
					queryParameterDTOs.add(new QueryParameterDTO("partnershipId", partnershipId));
				}
				hibernateSQLQueryResultRequestDTO.setQueryParameterDTOs(queryParameterDTOs);
				hibernateSQLQueryResultRequestDTO.setClassInstance(InstantNavigationLinksDTO.class);
				hibernateSQLQueryResultRequestDTO.setSortQueryString("order by 4 desc nulls last");
				return hibernateSQLQueryResultUtilDao.returnPaginatedDTO(hibernateSQLQueryResultRequestDTO, pagination,
						search);
			} else {
				return new PaginatedDTO();
			}

		} else {
			return new PaginatedDTO();
		}

	}

	@Override
	public PaginatedDTO universalSearchForVendorVanity(Pagination pagination, String search,
			LeftSideNavigationBarItem leftSideNavigationBarItem, VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		if (!hasAccess(leftSideNavigationBarItem)) {
			return new PaginatedDTO();
		}
		leftSideNavigationBarItem
				.setLoggedInThroughVendorVanityUrl(vanityUrlDetailsDTO.isPartnerLoggedInThroughVanityUrl());
		Integer vendorCompanyId = vanityUrlDetailsDTO.getVendorCompanyId();
		Integer partnerCompanyId = vanityUrlDetailsDTO.getLoggedInUserCompanyId();
		String navigatePartnerQueryString = ",cast('Partner' as text) as \"navigate\" ";
		String xup1CompanyIdString = " and xup1.company_id = ";
		String trackAndPlayBooksQueryString = " and xltv.is_published= true and xup1.user_id=" + pagination.getUserId()
				+ " and xlt.company_id=" + vendorCompanyId + xup1CompanyIdString + partnerCompanyId + " ";
		String assertQueryString = " d.company_id=" + vendorCompanyId + xup1CompanyIdString + partnerCompanyId + " ";
		String leadOrDealWhereCondition = " created_for_company_id=" + vendorCompanyId + " and created_by_company_id="
				+ partnerCompanyId + " ";
		String damQueryString = universalSearchPartnerVanityQuery
				.replace(WHERE_CONDITION_FOR_ASSERT_PARTNER_QUERY, assertQueryString)
				.replace(PARTNER_QUERY_STRING, assertQueryString);
		String tracksQuery = replacePartnerQuery(universalSearchTrackQuery, trackAndPlayBooksQueryString)
				.replace(NAVIGATE_QUERY_STRING, NAVIGATE_SHARED_STRING);
		String playBooksQuery = replacePartnerQuery(universalSearchPlaybookQuery, trackAndPlayBooksQueryString)
				.replace(NAVIGATE_QUERY_STRING, NAVIGATE_SHARED_STRING);
		String leadsQuery = replacePartnerQuery(universalSearchLeadsQuery, leadOrDealWhereCondition)
				.replace(NAVIGATE_QUERY_STRING, navigatePartnerQueryString);
		String dealsQuery = replacePartnerQuery(universalSearchDealsQuery, leadOrDealWhereCondition)
				.replace(NAVIGATE_QUERY_STRING, navigatePartnerQueryString);
		String onboardPartnersQuery = "";// XNFR-792
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = setUniversalSearchQueryByAccess(
				leftSideNavigationBarItem, damQueryString, tracksQuery, playBooksQuery, leadsQuery, dealsQuery, search,
				pagination, onboardPartnersQuery);
		if (StringUtils.hasText(hibernateSQLQueryResultRequestDTO.getQueryString())) {
			hibernateSQLQueryResultRequestDTO.setClassInstance(UniversalSearchDTO.class);
			return hibernateSQLQueryResultUtilDao.returnPaginatedDTO(hibernateSQLQueryResultRequestDTO, pagination,
					search);
		} else {
			return new PaginatedDTO();
		}
	}

	private HibernateSQLQueryResultRequestDTO setUniversalSearchQueryByAccess(
			LeftSideNavigationBarItem leftSideNavigationBarItem, String damQuery, String trackQuery,
			String playbookQuery, String leadQuery, String dealQuery, String searchKey, Pagination pagination,
			String onboardPartnersQuery) {
		boolean isDamAccess = isHavingAccess(leftSideNavigationBarItem, leftSideNavigationBarItem.isDam(),
				leftSideNavigationBarItem.isDamAccessAsPartner());
		boolean isLmsAccess = isHavingAccess(leftSideNavigationBarItem, leftSideNavigationBarItem.isLms(),
				leftSideNavigationBarItem.isLmsAccessAsPartner());
		boolean isPlayBookAccess = isHavingAccess(leftSideNavigationBarItem, leftSideNavigationBarItem.isPlaybook(),
				leftSideNavigationBarItem.isPlaybookAccessAsPartner());
		boolean isOpportunitiesAcces = isHavingAccess(leftSideNavigationBarItem,
				leftSideNavigationBarItem.isOpportunities(),
				leftSideNavigationBarItem.isOpportunitiesAccessAsPartner());
		String queryString = "";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		boolean hasSearchKey = StringUtils.hasText(searchKey);
		searchKey = XamplifyUtils.escapeSingleQuotesForSearchQuery(searchKey);
		searchKey = XamplifyUtils.addBackSlashToSpecialCharacters(searchKey);
		String filterBy = pagination.getFilterBy();
		filterBy = StringUtils.hasText(filterBy) ? filterBy : "All";
		boolean isAssetFilterApplied = "Assets".equalsIgnoreCase(pagination.getFilterBy())
				|| "All".equalsIgnoreCase(filterBy);
		boolean isTrackFilterApplied = "Tracks".equalsIgnoreCase(pagination.getFilterBy())
				|| "All".equalsIgnoreCase(filterBy);
		boolean isPlayBookFilterApplied = "PlayBooks".equalsIgnoreCase(pagination.getFilterBy())
				|| "All".equalsIgnoreCase(filterBy);
		boolean isLeadFilterApplied = LEADS.equalsIgnoreCase(pagination.getFilterBy())
				|| "All".equalsIgnoreCase(filterBy);
		boolean isDealFilterApplied = DEALS.equalsIgnoreCase(pagination.getFilterBy())
				|| "All".equalsIgnoreCase(filterBy);
		boolean isPartnerFilterApplied = "Partners".equalsIgnoreCase(pagination.getFilterBy())
				|| "All".equalsIgnoreCase(filterBy);
		queryString = universalSearchAssetsQueryString(isDamAccess, damQuery, searchKey, queryString, hasSearchKey,
				isAssetFilterApplied);
		queryString = universalSearchTracksOrPlayBooksQueryString(isLmsAccess, trackQuery, searchKey, queryString,
				hasSearchKey, isTrackFilterApplied);
		queryString = universalSearchTracksOrPlayBooksQueryString(isPlayBookAccess, playbookQuery, searchKey,
				queryString, hasSearchKey, isPlayBookFilterApplied);
		queryString = universalSearchLeadsQueryString(isOpportunitiesAcces, leadQuery, searchKey, queryString,
				hasSearchKey, isLeadFilterApplied, leftSideNavigationBarItem.isOpportunities());
		queryString = universalSearchDealsQueryString(isOpportunitiesAcces, dealQuery, searchKey, queryString,
				hasSearchKey, isDealFilterApplied, leftSideNavigationBarItem.isOpportunities());

		queryString = universalSearchPartnersQueryString(leftSideNavigationBarItem.isPartners(), onboardPartnersQuery,
				searchKey, queryString, hasSearchKey, isPartnerFilterApplied);
		StringBuilder queryStringBuilder = new StringBuilder(queryString);
		if (queryString.endsWith(XamplifyConstants.UNION)) {
			queryStringBuilder.replace(queryString.lastIndexOf(XamplifyConstants.UNION),
					queryString.lastIndexOf(XamplifyConstants.UNION) + XamplifyConstants.UNION.length(), "");
		}
		String updatedQueryString = String.valueOf(queryStringBuilder);
		hibernateSQLQueryResultRequestDTO.setQueryString(updatedQueryString);
		return hibernateSQLQueryResultRequestDTO;

	}

	private String universalSearchPartnersQueryString(boolean isPartnerAccess, String onBoardPArtnerQueryString,
			String searchKey, String queryString, boolean hasSearchKey, boolean isPartnerFilterApplied) {
		if (isPartnerAccess && isPartnerFilterApplied) {
			String searchQuery = "";
			if (hasSearchKey) {
				List<String> fields = new ArrayList<>(Arrays.asList("up.email_id", "uul.firstname", "uul.lastname",
						"REPLACE(uul.contact_company,' ', '')", "REPLACE(uul.firstname || uul.lastname,' ', '')",
						"REPLACE(uul.lastname || uul.firstname,' ', '')", "REPLACE(xcp.company_name,' ', '')"));
				searchQuery = buildSearchConditions(searchKey.replace(" ", ""), fields);
			}

			queryString += onBoardPArtnerQueryString.replace(SEARCH_QUERY_STRING, searchQuery) + " "
					+ XamplifyConstants.UNION;
		}
		return queryString;
	}

	private String buildSearchConditions(String searchKey, List<String> fields) {
		String searchConditions = fields.stream().map(field -> "LOWER(" + field + ") LIKE LOWER('%" + searchKey + "%')")
				.collect(Collectors.joining(" OR "));

		return "AND (" + searchConditions + ")";
	}

	private String universalSearchDealsQueryString(boolean isOpportunitiesAccessAsPartner, String dealQuery,
			String searchKey, String queryString, boolean hasSearchKey, boolean isDealFilterApplied,
			boolean isVendorView) {
		if (isOpportunitiesAccessAsPartner && isDealFilterApplied) {
			String searchQuery = "";
			if (hasSearchKey) {
				List<String> fields = new ArrayList<>(Arrays.asList("title", "xps.stage_name", "xc.campaign_name",
						"xcp.company_name", "xcp1.company_name"));
				if (isVendorView) {
					String[] nonPartnerFields = { "cast(xd.sf_deal_id AS text)",
							"cast(xd.microsoft_dynamics_deal_id as text)", "cast(xd.pipedrive_deal_id as text)",
							"cast(xd.zoho_deal_id as text)", "crm_reference_id"

					};
					fields.addAll(Arrays.asList(nonPartnerFields));
				}
				searchQuery = buildSearchConditions(searchKey, fields);
			}
			queryString += dealQuery.replace(SEARCH_QUERY_STRING, searchQuery) + " " + XamplifyConstants.UNION;
		}
		return queryString;
	}

	private String universalSearchLeadsQueryString(boolean isOpportunitiesAccessAsPartner, String leadQuery,
			String searchKey, String queryString, boolean hasSearchKey, boolean isLeadFilterApplied,
			boolean isVendorView) {
		if (isOpportunitiesAccessAsPartner && isLeadFilterApplied) {
			String searchQuery = "";
			if (hasSearchKey) {
				List<String> fields = new ArrayList<>(Arrays.asList(" first_name ", " last_name ", " xps.stage_name ",
						" xl.email ", " xc.campaign_name ", " xl.company ", " xcp.company_name", "xcp1.company_name"));
				if (isVendorView) {
					fields.addAll(Arrays.asList(" crm_reference_id ", " cast(xl.sf_lead_id AS text) ",
							" cast(xl.microsoft_dynamics_lead_id as text) ", " cast(xl.pipedrive_lead_id as text) ",
							" cast(xl.zoho_lead_id as text) "

					));
				}
				searchQuery = buildSearchConditions(searchKey, fields);
			}
			queryString += leadQuery.replace(SEARCH_QUERY_STRING, searchQuery) + " " + XamplifyConstants.UNION;
		}
		return queryString;
	}

	private String universalSearchTracksOrPlayBooksQueryString(boolean isLmsAccess, String trackQuery, String searchKey,
			String queryString, boolean hasSearchKey, boolean isTrackFilterApplied) {
		if (isLmsAccess && isTrackFilterApplied) {
			String searchQuery = "";
			if (hasSearchKey) {
				List<String> fields = new ArrayList<>();
				fields.add(" title");
				fields.add(" cast(cat.name as text) "); // for feature purpose
				fields.add(" t.tag_name ");
				searchQuery = buildSearchConditions(searchKey, fields);
			}

			// Replace placeholder and add GROUP BY and UNION
			queryString += trackQuery.replace(SEARCH_QUERY_STRING, searchQuery + "GROUP BY xlt.id,6,7,12,19,20 ") + " "
					+ XamplifyConstants.UNION;
		}
		return queryString;
	}

	private String universalSearchAssetsQueryString(boolean isDamAccess, String damQuery, String searchKey,
			String queryString, boolean hasSearchKey, boolean isAssetFilterApplied) {
		if (isDamAccess && isAssetFilterApplied) {
			String searchQuery = "";
			if (hasSearchKey) {
				List<String> fields = new ArrayList<>(Arrays.asList("d.asset_name", "d.asset_type", "t.tag_name",
						"vt.video_tags", "xup.firstname", "xup.lastname", "cat.name"));
				searchQuery = buildSearchConditions(searchKey, fields);
			}

			queryString += damQuery.replace(SEARCH_QUERY_STRING, searchQuery + " group by d.id,6,7,9,12 ") + " "
					+ XamplifyConstants.UNION;
		}
		return queryString;
	}

	private boolean isHavingAccess(LeftSideNavigationBarItem leftSideNavigationBarItem, boolean isAdminAccess,
			boolean isAccessAsPartner) {
		boolean isPartnerView = leftSideNavigationBarItem.getRoleDisplayDto().partnerOrPartnerTeamMember()
				|| leftSideNavigationBarItem.getRoleDisplayDto().isAdminAndPartnerTeamMember();
		if (leftSideNavigationBarItem.isLoggedInThroughOwnVanityUrl()) {
			return isAdminAccess;
		} else if (isPartnerView) {
			return isAccessAsPartner;
		} else {
			return (isAccessAsPartner || isAdminAccess);
		}
	}

	private String replacePartnerQuery(String originalQuery, String condition) {
		return originalQuery.replace(PARTNER_QUERY_STRING, condition);
	}

	private boolean hasAccess(LeftSideNavigationBarItem leftSideNavigationBarItem) {
		return leftSideNavigationBarItem.isDam() || leftSideNavigationBarItem.isDamAccessAsPartner()
				|| leftSideNavigationBarItem.isLms() || leftSideNavigationBarItem.isLmsAccessAsPartner()
				|| leftSideNavigationBarItem.isPlaybook() || leftSideNavigationBarItem.isPlaybookAccessAsPartner()
				|| leftSideNavigationBarItem.isOpportunities()
				|| leftSideNavigationBarItem.isOpportunitiesAccessAsPartner() || leftSideNavigationBarItem.isPartners();
	}

	@Override
	public PaginatedDTO universalSearchForVendor(Pagination pagination, String search,
			LeftSideNavigationBarItem leftSideNavigationBarItem) {
		if (!hasAccess(leftSideNavigationBarItem)) {
			return new PaginatedDTO();
		}
		Integer logInAsUserId = pagination.getLoginAsUserId();
		TeamMemberFilterDTO teamMemberFilter = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), true);
		Integer vendorCompanyId = pagination.getCompanyId();
		String baseCompanyCondition = " = " + vendorCompanyId;
		leftSideNavigationBarItem.setLoggedInThroughOwnVanityUrl(true);
		String damSqlQuery = replacePartnerQuery(universalSearchDamQuery, "d.company_id" + baseCompanyCondition);
		String tracksSqlQuery = buildTrackQuery(universalSearchTrackQuery, vendorCompanyId, pagination.getUserId(),
				leftSideNavigationBarItem, logInAsUserId);
		String playBooksQuery = buildTrackQuery(universalSearchPlaybookQuery, vendorCompanyId, pagination.getUserId(),
				leftSideNavigationBarItem, logInAsUserId);
		String leadsQueryString = buildLeadQuery(universalSearchLeadsQuery, vendorCompanyId, pagination.getUserId(),
				leftSideNavigationBarItem, " and xl.created_by_company_id ", teamMemberFilter, logInAsUserId);
		String dealsQueryString = buildLeadQuery(universalSearchDealsQuery, vendorCompanyId, pagination.getUserId(),
				leftSideNavigationBarItem, " and xd.created_by_company_id ", teamMemberFilter, logInAsUserId);
		String onboardPartnersQuery = buildOnboardPartnerQueryString(pagination.getUserId(), vendorCompanyId,
				teamMemberFilter, " and up.user_id ");
		HibernateSQLQueryResultRequestDTO queryRequest = setUniversalSearchQueryByAccess(leftSideNavigationBarItem,
				damSqlQuery, tracksSqlQuery, playBooksQuery, leadsQueryString, dealsQueryString, search, pagination,
				onboardPartnersQuery);
		if (StringUtils.hasText(queryRequest.getQueryString())) {
			queryRequest.setClassInstance(UniversalSearchDTO.class);
			return hibernateSQLQueryResultUtilDao.returnPaginatedDTO(queryRequest, pagination, search);
		}
		return new PaginatedDTO();
	}

	/** XNFR-792 **/
	private String buildOnboardPartnerQueryString(Integer userId, Integer vendorCompanyId,
			TeamMemberFilterDTO teamMemberFilterDTO, String teamMemberInQueryString) {
		String onBoardPartnersQueryString = universalSearchOnboardPartnersQuery
				.replace("${loggedUserCompanyID}", " " + vendorCompanyId)
				.replace("${userListId}", "" + partnerAnalyticsDAO.getDefaultPartnerListidByUserid(userId));
		String teamMemberFilterQueryString = setTeamMemberFilter(teamMemberInQueryString, "", userId,
				teamMemberFilterDTO, true);
		return onBoardPartnersQueryString.replace("${teamMemberQueryString}", teamMemberFilterQueryString);
	}

	private String buildTrackQuery(String queryString, Integer vendorCompanyId, Integer userId,
			LeftSideNavigationBarItem leftSideNavigationBarItem, Integer logInAsUserId) {
		String logInAsReplaceQueryString = "";
		if (XamplifyUtils.isValidInteger(logInAsUserId)) {
			Integer logInAsUserCompanyId = userDao.getCompanyIdByUserId(logInAsUserId);
			logInAsReplaceQueryString = " and xlt.company_id in ( " + logInAsUserCompanyId + "," + vendorCompanyId
					+ " )";
		}

		if ((leftSideNavigationBarItem.isLms() || leftSideNavigationBarItem.isPlaybook())
				&& !(leftSideNavigationBarItem.isLmsAccessAsPartner()
						|| leftSideNavigationBarItem.isPlaybookAccessAsPartner())
				|| leftSideNavigationBarItem.isLoggedInThroughOwnVanityUrl()) {
			return replacePartnerQuery(queryString, " and xlt.company_id=" + vendorCompanyId)
					.replace(NAVIGATE_QUERY_STRING, ", cast('Manage' as text) as \"navigate\" ");
		} else if ((leftSideNavigationBarItem.isLmsAccessAsPartner()
				|| leftSideNavigationBarItem.isPlaybookAccessAsPartner())
				&& !(leftSideNavigationBarItem.isLms() || leftSideNavigationBarItem.isPlaybook())) {
			return replacePartnerQuery(queryString,
					logInAsReplaceQueryString + "and xup1.user_id=" + userId + AND_XLTV_IS_PUBLISHED)
							.replace(NAVIGATE_QUERY_STRING, NAVIGATE_SHARED_STRING);
		} else {
			return replacePartnerQuery(queryString, logInAsReplaceQueryString + " and ( xlt.company_id="
					+ vendorCompanyId + " or ( xup1.user_id=" + userId + AND_XLTV_IS_PUBLISHED + " ) )").replace(
							NAVIGATE_QUERY_STRING,
							",cast(case when xlt.company_id= " + vendorCompanyId + " then 'Manage' "
									+ "when xup1.user_id = " + userId
									+ "   AND xltv.is_published = true then 'Shared' end as text) as \"navigate\"");
		}
	}

	private String buildLeadQuery(String queryString, Integer vendorCompanyId, Integer userId,
			LeftSideNavigationBarItem leftSideNavigationBarItem, String teamMemberQueryString,
			TeamMemberFilterDTO teamMemberFilterDTO, Integer logInAsUserId) {
		String dealsOrDealsQuery;
		boolean isCRMIntegrationActive = isCRMIntegrationActive(vendorCompanyId);
		String addIntegrationQueryString = isCRMIntegrationActive ? "  and xi.active= true " : "";
		String logInAsReplaceQuery = "";
		if (XamplifyUtils.isValidInteger(logInAsUserId)) {
			Integer logInAsUserCompanyId = userDao.getCompanyIdByUserId(logInAsUserId);
			logInAsReplaceQuery = " and created_for_company_id= " + logInAsUserCompanyId;
		}
		String whereConditionForLeadQueryString = "( (created_for_company_id=" + vendorCompanyId
				+ addIntegrationQueryString + ") or  created_by_company_id=" + vendorCompanyId + logInAsReplaceQuery
				+ ")";
		if (leftSideNavigationBarItem.isOpportunities() && !leftSideNavigationBarItem.isOpportunitiesAccessAsPartner()
				|| leftSideNavigationBarItem.isLoggedInThroughOwnVanityUrl()) {
			dealsOrDealsQuery = replacePartnerQuery(queryString,
					"created_for_company_id=" + vendorCompanyId + addIntegrationQueryString)
							.replace(NAVIGATE_QUERY_STRING, VENDOR_NAVIGATE_QUERY_STRING);
		} else if (!leftSideNavigationBarItem.isOpportunities()
				&& leftSideNavigationBarItem.isOpportunitiesAccessAsPartner()) {
			dealsOrDealsQuery = replacePartnerQuery(queryString,
					"created_by_company_id=" + vendorCompanyId + "and created_for_company_id !=" + vendorCompanyId
							+ logInAsReplaceQuery + addIntegrationQueryString).replace(NAVIGATE_QUERY_STRING,
									" ,cast('Partner' as text) as \"navigate\" ");
		} else {
			dealsOrDealsQuery = replacePartnerQuery(queryString, whereConditionForLeadQueryString)
					.replace(NAVIGATE_QUERY_STRING, ",cast(case when created_for_company_id =" + vendorCompanyId
							+ " then 'Vendor'else 'Partner' end as text) as \"navigate\" ");
		}
		return setTeamMemberFilter(teamMemberQueryString, dealsOrDealsQuery, userId, teamMemberFilterDTO, false);
	}

	private boolean isCRMIntegrationActive(Integer companyId) {
		String activeCRMIntegration = integrationDao.getActiveIntegrationTypeByCompanyId(companyId);
		return activeCRMIntegration != null && !activeCRMIntegration.isEmpty();
	}

	private String setTeamMemberFilter(String prefix, String query, Integer userId, TeamMemberFilterDTO filter,
			boolean isPartner) {
		return setTeamMemberFilterQueryString(prefix + " ", query, userId, filter, isPartner);
	}

	private String setTeamMemberFilterQueryString(String alias, String universalSearchQuery, Integer userId,
			TeamMemberFilterDTO teamMemberFilterDTO, boolean isPartner) {
		String partnerTeamMemberGroupFilterSQL = "";
		String projectColumnQueryString = isPartner ? "p.partner_id" : "p.partner_company_id";
		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			partnerTeamMemberGroupFilterSQL = alias + " IN (" + "SELECT DISTINCT " + projectColumnQueryString
					+ " FROM xt_team_member t "
					+ "LEFT OUTER JOIN xt_team_member_group_user_mapping tgum ON t.id = tgum.team_member_id "
					+ "LEFT OUTER JOIN xt_partner_team_group_mapping ptgm ON tgum.id = ptgm.team_member_group_user_mapping_id "
					+ "LEFT OUTER JOIN xt_partnership p ON ptgm.partnership_id = p.id "
					+ "LEFT JOIN xt_campaign xc ON xc.customer_id = tgum.team_member_id " + "WHERE t.team_member_id = "
					+ userId + " AND p.partner_id IS NOT NULL AND p.status = 'approved') ";
		}
		return universalSearchQuery + partnerTeamMemberGroupFilterSQL;
	}

	@Override
	public PaginatedDTO universalSearchForXamplifyLogin(Pagination pagination, String search,
			LeftSideNavigationBarItem leftSideNavigationBarItem, RoleDisplayDTO roleDisplayDto, Integer userId) {
		if (!hasAccess(leftSideNavigationBarItem)) {
			return new PaginatedDTO();
		}
		leftSideNavigationBarItem.setRoleDisplayDto(roleDisplayDto);
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), false);
		if ((roleDisplayDto.isAdminRole() || roleDisplayDto.isAdminTeamMember())
				&& !roleDisplayDto.isAdminAndPartnerTeamMember()) {
			return universalSearchForVendor(pagination, search, leftSideNavigationBarItem);
		} else if (roleDisplayDto.anyPartnerRole() && !roleDisplayDto.partnerOrPartnerTeamMember()) {
			return universalSearchForAdminAndPartner(leftSideNavigationBarItem, pagination, search,
					teamMemberFilterDTO);
		} else if (roleDisplayDto.partnerOrPartnerTeamMember()) {
			return universalSearchForPartner(pagination, search, leftSideNavigationBarItem, userId);
		}
		return new PaginatedDTO();
	}

	/** XNFR-792 **/
	private PaginatedDTO universalSearchForPartner(Pagination pagination, String search,
			LeftSideNavigationBarItem leftSideNavigationBarItem, Integer userId) {
		Integer companyId = pagination.getCompanyId();
		Integer logInAsUserId = pagination.getLoginAsUserId();
		String navigatePartnerString = ",cast('Partner' as text) as \"navigate\" ";
		String logInAsReplaceQuery = "";
		if (XamplifyUtils.isValidInteger(logInAsUserId)) {
			Integer logInAsUserCompanyId = userDao.getCompanyIdByUserId(logInAsUserId);
			logInAsReplaceQuery = " and created_for_company_id= " + logInAsUserCompanyId;
		}
		if (!hasAccess(leftSideNavigationBarItem)) {
			return new PaginatedDTO();
		}
		String damVendorVanityORPartnerQueryString = buildPartnerDamQuery(userId, companyId, logInAsUserId);
		String trackQueryString = buildTrackQuery(universalSearchTrackQuery, companyId, userId,
				leftSideNavigationBarItem, logInAsUserId);
		String universalSearchPlaybookQueryForPartner = buildTrackQuery(universalSearchPlaybookQuery, companyId, userId,
				leftSideNavigationBarItem, logInAsUserId);
		String universalSearchLeadsQueryStringForPartner = replacePartnerQuery(universalSearchLeadsQuery,
				" created_by_company_id=" + companyId + logInAsReplaceQuery + " ").replace(NAVIGATE_QUERY_STRING,
						navigatePartnerString);
		String universalSearchDealsQueryStringForPartner = replacePartnerQuery(universalSearchDealsQuery,
				" created_by_company_id=" + companyId + logInAsReplaceQuery + " ").replace(NAVIGATE_QUERY_STRING,
						navigatePartnerString);
		String onboardPartnersQuery = "";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = setUniversalSearchQueryByAccess(
				leftSideNavigationBarItem, damVendorVanityORPartnerQueryString, trackQueryString,
				universalSearchPlaybookQueryForPartner, universalSearchLeadsQueryStringForPartner,
				universalSearchDealsQueryStringForPartner, search, pagination, onboardPartnersQuery);
		if (StringUtils.hasText(hibernateSQLQueryResultRequestDTO.getQueryString())) {
			List<QueryParameterDTO> queryParameterDTOs = new ArrayList<>();
			hibernateSQLQueryResultRequestDTO.setQueryParameterDTOs(queryParameterDTOs);
			hibernateSQLQueryResultRequestDTO.setClassInstance(UniversalSearchDTO.class);
			return hibernateSQLQueryResultUtilDao.returnPaginatedDTO(hibernateSQLQueryResultRequestDTO, pagination,
					search);
		} else {
			return new PaginatedDTO();
		}

	}

	private String buildPartnerDamQuery(Integer userId, Integer companyId, Integer logInAsUserId) {
		Integer logInAsUserCompanyId = 0;
		String loginAsReplaceQueryString = "";
		if (XamplifyUtils.isValidInteger(logInAsUserId)) {
			logInAsUserCompanyId = userDao.getCompanyIdByUserId(logInAsUserId);
			loginAsReplaceQueryString = " d.company_id = " + logInAsUserCompanyId + " and ";
		}
		return universalSearchPartnerVanityQuery
				.replace(WHERE_CONDITION_FOR_ASSERT_PARTNER_QUERY,
						loginAsReplaceQueryString + " xup1.user_id=" + userId + " ")
				.replace(PARTNER_QUERY_STRING, loginAsReplaceQueryString + "p.partner_company_id =" + companyId);
	}

	private PaginatedDTO universalSearchForAdminAndPartner(LeftSideNavigationBarItem leftSideNavigationBarItem,
			Pagination pagination, String search, TeamMemberFilterDTO teamMemberFilterDTO) {

		Integer userId = pagination.getUserId();
		Integer logInAsUserId = pagination.getLoginAsUserId();
		Integer vendorCompanyId = userDao.getCompanyIdByUserId(userId);
		// Build Queries
		String vendorDamQuery = leftSideNavigationBarItem.isDam()
				? replacePartnerQuery(universalSearchDamQuery, " d.company_id=" + vendorCompanyId + " ")
				: "";
		String partnerDamQuery = leftSideNavigationBarItem.isDamAccessAsPartner()
				? buildPartnerDamQuery(userId, vendorCompanyId, logInAsUserId)
				: "";
		String combinedDamQuery = combineDamQueries(vendorDamQuery, partnerDamQuery, leftSideNavigationBarItem);
		String trackQuery = buildTrackQuery(universalSearchTrackQuery, vendorCompanyId, userId,
				leftSideNavigationBarItem, logInAsUserId);
		String playbookQuery = buildTrackQuery(universalSearchPlaybookQuery, vendorCompanyId, userId,
				leftSideNavigationBarItem, logInAsUserId);
		String leadQuery = buildLeadQuery(universalSearchLeadsQuery, vendorCompanyId, userId, leftSideNavigationBarItem,
				" and xl.created_by_company_id ", teamMemberFilterDTO, logInAsUserId);

		String dealQuery = buildLeadQuery(universalSearchDealsQuery, vendorCompanyId, userId, leftSideNavigationBarItem,
				" and xd.created_by_company_id ", teamMemberFilterDTO, logInAsUserId);

		String onboardPartnersQuery = buildOnboardPartnerQueryString(userId, vendorCompanyId, teamMemberFilterDTO,
				" and up.user_id ");

		// Create Request DTO
		HibernateSQLQueryResultRequestDTO queryRequestDTO = setUniversalSearchQueryByAccess(leftSideNavigationBarItem,
				combinedDamQuery, trackQuery, playbookQuery, leadQuery, dealQuery, search, pagination,
				onboardPartnersQuery);

		// Return Results
		if (StringUtils.hasText(queryRequestDTO.getQueryString())) {
			queryRequestDTO.setClassInstance(UniversalSearchDTO.class);
			return hibernateSQLQueryResultUtilDao.returnPaginatedDTO(queryRequestDTO, pagination, search);
		} else {
			return new PaginatedDTO();
		}
	}

	private String combineDamQueries(String vendorDamQuery, String partnerDamQuery,
			LeftSideNavigationBarItem leftSideNavigationBarItem) {
		if (leftSideNavigationBarItem.isDam() && !leftSideNavigationBarItem.isDamAccessAsPartner()) {
			return vendorDamQuery;
		} else if (!leftSideNavigationBarItem.isDam() && leftSideNavigationBarItem.isDamAccessAsPartner()) {
			return "(" + partnerDamQuery + ")";
		} else {
			return vendorDamQuery + XamplifyConstants.UNION + "(" + partnerDamQuery + ")";
		}
	}

}
