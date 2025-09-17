package com.xtremand.analytics.dao.hibernate;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.xtremand.analytics.dao.PartnerAnalyticsDAO;
import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.common.bom.Pagination;
import com.xtremand.deal.dto.DealDto;
import com.xtremand.exception.CategoryDataAccessException;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.lead.dto.LeadDto;
import com.xtremand.mdf.dto.MdfAmountTilesDTO;
import com.xtremand.partner.bom.PartnerContactUsageDTO;
import com.xtremand.partner.bom.PartnerDTO;
import com.xtremand.partner.bom.PartnerDataAccessException;
import com.xtremand.partner.bom.ReminderEmailLog;
import com.xtremand.team.dao.TeamDao;
import com.xtremand.team.member.dto.TeamMemberDTO;
import com.xtremand.team.member.dto.TeamMemberListDTO;
import com.xtremand.user.bom.ShareLeadsDTO;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.userlist.dao.UserListDAO;
import com.xtremand.util.PaginationUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.HibernateSQLQueryResultUtilDao;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.PaginatedDTO;
import com.xtremand.util.dto.PartnerCompanyDTO;
import com.xtremand.util.dto.PartnerJourneyAnalyticsDTO;
import com.xtremand.util.dto.PartnerJourneyRequestDTO;
import com.xtremand.util.dto.PartnerJourneyTrackDetailsDTO;
import com.xtremand.util.dto.QueryParameterDTO;
import com.xtremand.util.dto.QueryParameterListDTO;
import com.xtremand.util.dto.SortColumnDTO;
import com.xtremand.util.dto.TeamMemberFilterDTO;
import com.xtremand.util.dto.XamplifyConstants;

@Repository("partnerAnalyticsDAO")
@Transactional
public class HibernatePartnerAnalyticsDAO implements PartnerAnalyticsDAO {

	private static final String USER_LIST_ID = "userListId";

	private static final Logger logger = LoggerFactory.getLogger(HibernatePartnerAnalyticsDAO.class);

	@Autowired
	SessionFactory sessionFactory;

	@Autowired
	private UserDAO userDao;

	@Autowired
	private UtilDao utilDao;

	@Autowired
	private PaginationUtil paginationUtil;

	@Autowired
	private UserListDAO userListDAO;

	@Autowired
	private TeamDao teamDao;

	@Value("${partner.redistributedCampaignsAndLeadsBarChartQuery}")
	private String redistributedCampaignsAndLeadsBarChartQuery;

	@Value("${partner.redistributedCampaignsAndLeadsBarChartTeamMemberFilterQuery}")
	private String redistributedCampaignsAndLeadsBarChartTeamMemberFilterQuery;

	@Value("${partner.redistributedCampaignsAndLeadsForPreviousQuarterBarChartQuery}")
	private String redistributedCampaignsAndLeadsForPreviousQuarterBarChartQuery;

	@Value("${partner.redistributedCampaignsAndLeadsForPreviousQuarterBarChartTeamMemberFilterQuery}")
	private String redistributedCampaignsAndLeadsForPreviousQuarterBarChartTeamMemberFilterQuery;

	@Value("${partner.leadsAndDealsCountQuery}")
	private String leadsAndDealsCountQuery;

	@Value("${partner.leadsAndDealsCountTeamMemberFilterQuery}")
	private String leadsAndDealsCountTeamMemberFilterQuery;

	@Value("${partner.leadsToDealsConversionQuery}")
	private String leadsToDealsConversionQuery;

	@Value("${partner.opportunityAmountQuery}")
	private String opportunityAmountQuery;

	@Value("${partner.redistributedCampaignsCountOrderByQuery}")
	private String redistributedCampaignsCountOrderByQuery;

	@Value("${partner.leadsCountOrderByQuery}")
	private String leadsCountOrderByQuery;

	@Value("${partner.leadsOrderByQuery}")
	private String leadsOrderByQuery;

	@Value("${partner.dealsOrderByQuery}")
	private String dealsOrderByQuery;

	@Value("${dashboard.vendorLeadToDealConversionPieChart}")
	private String vendorLeadToDealConversionPieChart;

	// Start : XNFR-316
	@Value("${activePartnerCompaniesQuery}")
	private String activePartnerCompaniesQuery;

	@Value("${partnerJourneyCompanyInfoQuery}")
	private String partnerJourneyCompanyInfoQuery;

	@Value("${partnerJourneyCampaignCountQuery}")
	private String partnerJourneyCampaignCountQuery;

	@Value("${partnerJourneyTeamInfoQuery}")
	private String partnerJourneyTeamInfoQuery;

	@Value("${partnerJourneyTeamQuery}")
	private String partnerJourneyTeamQuery;

	@Value("${partnerJourneyTeamInfoSearchConditions}")
	private String partnerJourneyTeamInfoSearchConditions;

	@Value("${partnerJourneyTeamInfoTMFilterCondition}")
	private String partnerJourneyTeamInfoTMFilterCondition;

	@Value("${partnerJourneyRedistributedCampaignCountQuery}")
	private String partnerJourneyRedistributedCampaignCountQuery;

	@Value("${partnerJourneyTeamMemberCountQuery}")
	private String partnerJourneyTeamMemberCountQuery;

	@Value("${partnerJourneyShareLeadCountQuery}")
	private String partnerJourneyShareLeadCountQuery;

	@Value("${partnerJourneyLeadCountQuery}")
	private String partnerJourneyLeadCountQuery;

	@Value("${partnerJourneyDealCountQuery}")
	private String partnerJourneyDealCountQuery;

	@Value("${partnerJourneyContactCountQuery}")
	private String partnerJourneyContactCountQuery;

	@Value("${partnerJourneyMDFAmountQuery}")
	private String partnerJourneyMDFAmountQuery;

	@Value("${partnerJourneyTrackAndPlaybookCountQuery}")
	private String partnerJourneyTrackAndPlaybookCountQuery;

	@Value("${partnerJourneyLeadToDealConversionQuery}")
	private String partnerJourneyLeadToDealConversionQuery;

	@Value("${partnerJourneyLeadToDealConversionGroupBy}")
	private String partnerJourneyLeadToDealConversionGroupBy;

	@Value("${partnerJourneyAssetCountQuery}")
	private String partnerJourneyAssetCountQuery;

	@Value("${xup1UserIdTMFilter}")
	private String xup1UserIdTMFilter;

	@Value("${xup2UserIdTMFilter}")
	private String xup2UserIdTMFilter;

	@Value("${xupUserIdTMFilter}")
	private String xupUserIdTMFilter;

	@Value("${partnerJourneyViewedAndNotViewedTrackCountsQuery}")
	private String partnerJourneyViewedAndNotViewedTrackCountsQuery;

	@Value("${partnerJourneyViewedAndNotViewedTrackCountsGroupBy}")
	private String partnerJourneyViewedAndNotViewedTrackCountsGroupBy;

	@Value("${allViewedAndNotViewedTrackDetailsQueryGroupBy}")
	private String allViewedAndNotViewedTrackDetailsQueryGroupBy;

	@Value("${partnerJourneyViewedAndNotViewedTrackDetailsQuery}")
	private String partnerJourneyViewedAndNotViewedTrackDetailsQuery;

	@Value("${partnerJourneyTrackTypeInteractedFilter}")
	private String partnerJourneyTrackTypeInteractedFilter;

	@Value("${partnerJourneyTrackTypeNotInteractedFilter}")
	private String partnerJourneyTrackTypeNotInteractedFilter;

	@Value("${partnerJourneyViewedAndNotViewedTrackDetailsSearchConditions}")
	private String partnerJourneyViewedAndNotViewedTrackDetailsSearchConditions;

	@Value("${partnerJourneyTypeWiseTrackCountsQuery}")
	private String partnerJourneyTypeWiseTrackCountsQuery;

	@Value("${partnerJourneyTypeWiseTrackInteractedFilter}")
	private String partnerJourneyTypeWiseTrackInteractedFilter;

	@Value("${partnerJourneyTypeWiseTrackNotInteractedFilter}")
	private String partnerJourneyTypeWiseTrackNotInteractedFilter;

	@Value("${partnerJourneyTypeWiseTrackDetailsAssetFilter}")
	private String partnerJourneyTypeWiseTrackDetailsAssetFilter;

	@Value("${partnerJourneyTypeWiseTrackDetailsQuery}")
	private String partnerJourneyTypeWiseTrackDetailsQuery;

	@Value("${partnerJourneyTypeWiseTrackDetailsSearchConditions}")
	private String partnerJourneyTypeWiseTrackDetailsSearchConditions;

	@Value("${partnerJourneyTypeWiseTrackDetailsNotOpenedSearchConditions}")
	private String partnerJourneyTypeWiseTrackDetailsNotOpenedSearchConditions;

	@Value("${partnerJourneyTracksUserWiseCountsQuery}")
	private String partnerJourneyTracksUserWiseCountsQuery;

	@Value("${partnerJourneyTracksUserWiseCountsSearchConditions}")
	private String partnerJourneyTracksUserWiseCountsSearchConditions;

	@Value("${partnerJourneyTracksUserWiseGroupBy}")
	private String partnerJourneyTracksUserWiseGroupBy;

	@Value("${partnerJourneyTracksUserWiseContentDetailsQuery}")
	private String partnerJourneyTracksUserWiseContentDetailsQuery;

	@Value("${partnerJourneyTracksUserWiseContentDetailsGroupBy}")
	private String partnerJourneyTracksUserWiseContentDetailsGroupBy;

	@Value("${partnerJourneyTracksUserWiseContentDetailsSearchCondition}")
	private String partnerJourneyTracksUserWiseContentDetailsSearchCondition;

	@Value("${partnerJourneyTrackAssetsQuery}")
	private String partnerJourneyTrackAssetsQuery;

	@Value("${partnerJourneyTrackAssetsGroupBy}")
	private String partnerJourneyTrackAssetsGroupBy;

	@Value("${partnerJourneyTrackAssetsSearchCondition}")
	private String partnerJourneyTrackAssetsSearchCondition;

	@Value("${partnerJourneyPlaybookAssetsQuery}")
	private String partnerJourneyPlaybookAssetsQuery;

	@Value("${partnerJourneyPlaybookAssetsGroupBy}")
	private String partnerJourneyPlaybookAssetsGroupBy;

	@Value("${partnerJourneyPlaybookAssetsSearchCondition}")
	private String partnerJourneyPlaybookAssetsSearchCondition;

	@Value("${partnerJourneyShareLeadDetailsQuery}")
	private String partnerJourneyShareLeadDetailsQuery;

	@Value("${partnerJourneyShareLeadDetailsGroupBy}")
	private String partnerJourneyShareLeadDetailsGroupBy;

	@Value("${partnerJourneyShareLeadDetailsSearchConditions}")
	private String partnerJourneyShareLeadDetailsSearchConditions;

	@Value("${partnerJourneyUserWiseRedistributedCampaignDetailsQuery}")
	private String partnerJourneyUserWiseRedistributedCampaignDetailsQuery;

	@Value("${redistributedCampaignDetailsTeamMemberFilterQuery}")
	private String redistributedCampaignDetailsTeamMemberFilterQuery;

	@Value("${partnerJourneyRedistributedCampaignDetailsSearchConditions}")
	private String partnerJourneyRedistributedCampaignDetailsSearchConditions;

	@Value("${allRedistributedCampignDetailsForPieChartTeamMemberFilterQuery}")
	private String allRedistributedCampignDetailsForPieChartTeamMemberFilterQuery;

	@Value("${allRedistributedCampignDetailsForPieChartGroupBy}")
	private String allRedistributedCampignDetailsForPieChartGroupBy;

	@Value("${allRedistributedCampignDetailsForPieChart}")
	private String allRedistributedCampignDetailsForPieChart;

	@Value("${partnerJourneyLeadDetailsQuery}")
	private String partnerJourneyLeadDetailsQuery;

	@Value("${partnerJourneyLeadDetailsSearchConditions}")
	private String partnerJourneyLeadDetailsSearchConditions;

	@Value("${partnerJourneyDealDetailsQuery}")
	private String partnerJourneyDealDetailsQuery;

	@Value("${partnerJourneyDealDetailsSearchConditions}")
	private String partnerJourneyDealDetailsSearchConditions;

	@Value("${partnerJourneycontactDetailsQuery}")
	private String partnerJourneycontactDetailsQuery;

	@Value("${partnerJourneyContactDetailsSearchConditions}")
	private String partnerJourneyContactDetailsSearchConditions;

	@Value("${partnerJourneycontactDetailsGroupBy}")
	private String partnerJourneycontactDetailsGroupBy;

	@Value("${partnerJourneyMdfDetailsQuery}")
	private String partnerJourneyMdfDetailsQuery;

	@Value("${partnerJourneyMdfDetailsSearchConditions}")
	private String partnerJourneyMdfDetailsSearchConditions;

	@Value("${partnerJourneyMdfDetailsGroupBy}")
	private String partnerJourneyMdfDetailsGroupBy;

	@Value("${partnerJourneyInteracted}")
	private String partnerJourneyInteracted;

	@Value("${partnerJourneyNotInteracted}")
	private String partnerJourneyNotInteracted;

	@Value("${partnerJourneyNotOpened}")
	private String partnerJourneyNotOpened;

	@Value("${allViewedAndNotViewedTrackCountsQuery}")
	private String allViewedAndNotViewedTrackCountsQuery;

	@Value("${allViewedAndNotViewedTrackCountsQueryGroupBy}")
	private String allViewedAndNotViewedTrackCountsQueryGroupBy;

	@Value("${allViewedAndNotViewedTrackDetailsQuery}")
	private String allViewedAndNotViewedTrackDetailsQuery;

	@Value("${allRegionWiseDonutQuery}")
	private String allRegionWiseDonutQuery;

	@Value("${allRegionWiseDonutQueryForTeamMemberLogin}")
	private String allRegionWiseDonutQueryForTeamMemberLogin;

	@Value("${allPartnersDetailsListQuery}")
	private String allPartnersDetailsListQuery;

	@Value("${allPartnersDetailsListQueryAndTeamMemberLoginQuery}")
	private String allPartnersDetailsListQueryAndTeamMemberLoginQuery;

	@Value("${prefixQuery}")
	private String prefixQuery;

	@Value("${orderBySuffix}")
	private String orderBySuffix;

	@Value("${orderByPrefix}")
	private String orderByPrefix;

	@Value("${allRegionNames}")
	private String allRegionNames;

	@Value("${allRegionNamesByTeamMemberLogin}")
	private String allRegionNamesByTeamMemberLogin;

	@Value("${allPartnersDetailsListQuerySuffixForBothRegionAndSearch}")
	private String allPartnersDetailsListQuerySuffixForBothRegionAndSearch;

	@Value("${allPartnersDetailsListQuerySuffix}")
	private String allPartnersDetailsListQuerySuffix;

	@Value("${allPartnersDetailsListByFilter}")
	private String allPartnersDetailsListByFilter;

	@Value("${allViewedAndNotViewedTrackDetailsSearchConditions}")
	private String allViewedAndNotViewedTrackDetailsSearchConditions;

	@Value("${allTypeWiseTrackCountsQuery}")
	private String allTypeWiseTrackCountsQuery;

	@Value("${allTypeWiseTrackDetailsQuery}")
	private String allTypeWiseTrackDetailsQuery;

	@Value("${allTypeWiseTrackDetailsSearchConditions}")
	private String allTypeWiseTrackDetailsSearchConditions;

	@Value("${allTypeWiseTrackDetailsNotOpenedSearchConditions}")
	private String allTypeWiseTrackDetailsNotOpenedSearchConditions;

	@Value("${allTracksUserWiseCountsQuery}")
	private String allTracksUserWiseCountsQuery;

	@Value("${companyNameSearchCondition}")
	private String companyNameSearchCondition;

	@Value("${allTracksUserWiseGroupBy}")
	private String allTracksUserWiseGroupBy;

	@Value("${allPlaybooksUserWiseCountsQuery}")
	private String allPlaybooksUserWiseCountsQuery;

	@Value("${allPlaybooksUserWiseGroupBy}")
	private String allPlaybooksUserWiseGroupBy;

	@Value("${allTracksUserWiseContentDetailsQuery}")
	private String allTracksUserWiseContentDetailsQuery;

	@Value("${allTracksUserWiseContentDetailsGroupBy}")
	private String allTracksUserWiseContentDetailsGroupBy;

	@Value("${allTracksUserWiseContentDetailsSearchCondition}")
	private String allTracksUserWiseContentDetailsSearchCondition;

	@Value("${allTrackAssetsQuery}")
	private String allTrackAssetsQuery;

	@Value("${allTrackAssetsGroupBy}")
	private String allTrackAssetsGroupBy;

	@Value("${allPlaybookAssetsQuery}")
	private String allPlaybookAssetsQuery;

	@Value("${allPlaybookAssetsGroupBy}")
	private String allPlaybookAssetsGroupBy;

	@Value("${allShareLeadDetailsQuery}")
	private String allShareLeadDetailsQuery;

	@Value("${allShareLeadDetailsGroupBy}")
	private String allShareLeadDetailsGroupBy;

	@Value("${allShareLeadDetailsSearchConditions}")
	private String allShareLeadDetailsSearchConditions;

	@Value("${allRedistributedCampaignDetailsQuery}")
	private String allRedistributedCampaignDetailsQuery;

	@Value("${allRedistributedCampaignDetailsSearchConditions}")
	private String allRedistributedCampaignDetailsSearchConditions;

	@Value("${allLeadDetailsQuery}")
	private String allLeadDetailsQuery;

	@Value("${allLeadDetailsSearchConditions}")
	private String allLeadDetailsSearchConditions;

	@Value("${allDealDetailsQuery}")
	private String allDealDetailsQuery;

	@Value("${findAllTeamMembers}")
	private String findAllTeamMembers;

	@Value("${teamMemberPrimaryAdmin}")
	private String teamMemberPrimaryAdmin;

	@Value("${teamMemberPrimaryAdminSearch}")
	private String teamMemberPrimaryAdminSearch;

	@Value("${findAllTeamMembersSearch}")
	private String findAllTeamMembersSearch;

	@Value("${findTeamMemberPrimaryAdminGroupBy}")
	private String findTeamMemberPrimaryAdminGroupBy;

	@Value("${findAllTeamMembersGroupBy}")
	private String findAllTeamMembersGroupBy;

	@Value("${orderByfindAllTeamMembers}")
	private String orderByfindAllTeamMembers;

	@Value("${orderByTeamMemberPrimaryAdmin}")
	private String orderByTeamMemberPrimaryAdmin;

	@Value("${allDealDetailsSearchConditions}")
	private String allDealDetailsSearchConditions;

	@Value("${allRedistributedCampaignCountQuery}")
	private String allRedistributedCampaignCountQuery;

	@Value("${allTeamMemberCountQuery}")
	private String allTeamMemberCountQuery;

	@Value("${allShareLeadCountQuery}")
	private String allShareLeadCountQuery;

	@Value("${allLeadCountQuery}")
	private String allLeadCountQuery;

	@Value("${allDealCountQuery}")
	private String allDealCountQuery;

	@Value("${allMDFAmountQuery}")
	private String allMDFAmountQuery;

	@Value("${allTrackAndPlaybookCountQuery}")
	private String allTrackAndPlaybookCountQuery;

	@Value("${allAssetCountQuery}")
	private String allAssetCountQuery;

	@Value("${allContactCountQuery}")
	private String allContactCountQuery;

	@Value("${partnerJourneyTeamMemberFilterQuery}")
	private String partnerJourneyTeamMemberFilterQuery;

	@Value("${partnerJourneyPlayBookUserWiseCountsQuery}")
	private String partnerJourneyPlayBookUserWiseCountsQuery;

	@Value("${partnerJourneyPlayBookUserWiseGroupBy}")
	private String partnerJourneyPlayBookUserWiseGroupBy;

	@Value("${partnerJourneyCampaignToLeadConversionQuery}")
	private String partnerJourneyCampaignToLeadConversionQuery;

	@Value("${partnerJourneyCampaignToLeadConversionGroupBy}")
	private String partnerJourneyCampaignToLeadConversionGroupBy;

	@Value("${partnerJourneyAssetDetailsQuery}")
	private String partnerJourneyAssetDetailsQuery;

	@Value("${partnerJourneyAssetDetailsViewAndDetailedCountQuery}")
	private String partnerJourneyAssetDetailsViewAndDetailedCountQuery;

	@Value("${partnerJourneyAssetDetailsSearchQuery}")
	private String partnerJourneyAssetDetailsSearchQuery;

	@Value("${allTrackAssetsCountQuery}")
	private String allTrackAssetsCountQuery;

	@Value("${assetDetailsDetailedQuery}")
	private String assetDetailsDetailedQuery;

	@Value("${assetDetailsViewAndDownloadedDetailedQuery}")
	private String assetDetailsViewAndDownloadedDetailedQuery;

	@Value("${assetDetailsSearchQuery}")
	private String assetDetailsSearchQuery;

	@Value("${all.partners.contacts.upload.management.query}")
	private String allPartnersContactsUploadManagementQuery;

	@Value("${all.partners.contacts.upload.management.search.query}")
	private String allPartnersContactsUploadManagementSearchQuery;

	@Value("${contact.count.uploaded.by.all.partners.query}")
	private String contactCountUploadedByAllPartnersQuery;

	@Value("${partnerJourneyAssetNames}")
	private String partnerJourneyAssetNames;

	@Value("${partnerJourneyEmailIds}")
	private String partnerJourneyEmailIds;

	@Value("${assetJourneyAssetDetails}")
	private String assetJourneyAssetDetails;

	@Value("${assetJourneyAssetDetailsSearchQuery}")
	private String assetJourneyAssetDetailsSearchQuery;

	@Value("${partnerDetailedAnalyticsJourneyAssetNames}")
	private String partnerDetailedAnalyticsJourneyAssetNames;

	@Value("${partnerJourneyAssetNamesForTeamMemberLogin}")
	private String partnerJourneyAssetNamesForTeamMemberLogin;

	@Value("${partnerJourneyAssetDetailsForTeamMemberLogin}")
	private String partnerJourneyAssetDetailsForTeamMemberLogin;

	@Value("${assetJourneyAssetDetailsForTeamMemberLogin}")
	private String assetJourneyAssetDetailsForTeamMemberLogin;

	@Value("${playbookJourneyInteractionQuery}")
	private String playbookJourneyInteractionQuery;

	@Value("${totalPartnerInteractionQuery}")
	private String totalPartnerInteractionQuery;

	@Value("${playbookJourneyInteractionQueryForTeamMember}")
	private String playbookJourneyInteractionQueryForTeamMember;

	@Value("${totalPartnerTeamMemberInteractionQuery}")
	private String totalPartnerTeamMemberInteractionQuery;

	@Value("${playbookJourneyDetailsAnalytics}")
	private String playbookJourneyDetailsAnalytics;

	@Value("${partnerJourneyAssetInteractionDetailsForTeamMemberLogin}")
	private String partnerJourneyAssetInteractionDetailsForTeamMemberLogin;

	@Value("${partnerJourneyAssetInteractionDetailsViewAndDetailedCountQuery}")
	private String partnerJourneyAssetInteractionDetailsViewAndDetailedCountQuery;

	@Value("${partnerAssetInteractionDetailsQuery}")
	private String partnerAssetInteractionDetailsQuery;

	@Value("${assetInteractionDetailsForTeamMember}")
	private String assetInteractionDetailsForTeamMember;

	@Autowired
	private HibernateSQLQueryResultUtilDao hibernateSQLQueryResultUtilDao;

	// END : XNFR-316

	private static final String TOTAL_RECORDS = "totalRecords";

	private static final String PARENT_CAMPAIGN_ID = "parentCampaignId";

	private static final String USER_ID = "userId";

	private static final String COMPANY_ID = "companyId";

	private static final String TRACK_DATE_COLUMN = "xlt.created_time";

	private static final String CAMPAIGN_DATE_COLUMN = "xc1.launch_time";

	private static final String LEAD_DATE_COLUMN = "xl.created_time";

	private static final String DEAL_DATE_COLUMN = "xd.created_time";

	private static final String CONTACT_DATE_COLUMN = "xup.created_time";

	private static final String TRACK_PUBLISHED_DATE_COLUMN = "xltv.published_on";

	private static final String ALL_PARTNERS_CONTACTS_SEARCH_REPLACE_KEY = "${allPartnersContactsSearchReplaceKey}";

	@Override
	public Integer getCompanyPartnersCount(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select count(*) from xt_partnership where vendor_company_id= :companyId and status = 'approved' ";
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter(COMPANY_ID, companyId);
		return query.uniqueResult() != null ? ((BigInteger) query.uniqueResult()).intValue() : 0;
	}

	@Override
	public Integer getPartnersLaunchedCampaignsCount(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select count(campaign_id) from xt_campaign where is_nurture_campaign = true and is_launched=true and vendor_organization_id="
				+ companyId;
		SQLQuery query = session.createSQLQuery(sql);
		return query.uniqueResult() != null ? ((BigInteger) query.uniqueResult()).intValue() : 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> listPartnersLaunchedCampaignsByCampaignType(Integer companyId, boolean applyFilter,
			Integer userId) {
		Session session = sessionFactory.getCurrentSession();
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(userId, applyFilter, false);
		boolean applyTeamMemberFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
		if (teamMemberFilterDTO.isEmptyFilter()) {
			return new ArrayList<Object[]>();
		} else {
			if (applyTeamMemberFilter) {
				String sql = " select campaign_type,count(distinct campaign_id) from xt_campaign where is_nurture_campaign = true and parent_campaign_id is not null and is_launched = true and vendor_organization_id = "
						+ companyId
						+ " and campaign_type!='LANDINGPAGE' and customer_id in (select user_id from xt_user_profile where company_id in(:partnerCompanyIds))  group by campaign_type";
				SQLQuery query = session.createSQLQuery(sql);
				query.setParameterList("partnerCompanyIds", teamMemberFilterDTO.getPartnershipIdsOrPartnerCompanyIds());
				return query.list();

			} else {
				String sql = " select campaign_type,count(distinct campaign_id) from xt_campaign where is_nurture_campaign = true and parent_campaign_id is not null and is_launched = true and vendor_organization_id = "
						+ companyId + " and campaign_type!='LANDINGPAGE' group by campaign_type";
				SQLQuery query = session.createSQLQuery(sql);
				return query.list();
			}
		}
	}

	@Override
	public Map<String, Object> listNoOfCampaignsLaunchedByPartner(Pagination pagination) {
		String partnershipStatus = pagination.getPartnershipStatus() != null ? pagination.getPartnershipStatus()
				: "approved";
		try {
			Map<String, Object> map = new HashMap<>();
			String sql = " select distinct xcp1.company_name, xcp1.company_id,count (distinct xc1.campaign_id) from  xt_campaign xc "
					+ " left join xt_user_profile xup on (xc.customer_id=xup.user_id) left join xt_company_profile xcp on (xup.company_id=xcp.company_id) "
					+ " left join xt_campaign xc1 on (xc.campaign_id=xc1.parent_campaign_id ) left join xt_user_profile xup1 ON (xc1.customer_id = xup1.user_id) "
					+ " left join xt_company_profile xcp1 ON (xup1.company_id = xcp1.company_id)  left join xt_partnership p on p.partner_company_id= xup1.company_id and p.vendor_company_id="
					+ pagination.getCompanyId() + "  where xup.company_id=  " + pagination.getCompanyId()
					+ " and  xcp1.company_name is not null and xc1.vendor_organization_id is not null and xc1.is_launched= true  and  xc1.is_nurture_campaign =  true "
					+ " and xc1.parent_campaign_id is not null and xcp.company_id != xcp1.company_id and p.status={status} ";
			String groupByQuery = " group by 1,2 order by 3 desc";
			sql = replacePartnershipStatus(sql, partnershipStatus);
			String dateFilterQueryString = frameDateFilterQuery(pagination, CAMPAIGN_DATE_COLUMN);
			String listQuery;
			if (pagination.getSelectedPartnerCompanyIds() != null
					&& !pagination.getSelectedPartnerCompanyIds().isEmpty()) {
				sql += "and xcp1.company_id in (:partnerCompanyIds) ";
			}
			TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
					pagination.isPartnerTeamMemberGroupFilter(), false);
			boolean applyTeamMemberFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
			boolean isFilterOption = false;
			if (applyTeamMemberFilter) {
				isFilterOption = teamDao.getTeamMemberOption(pagination.getUserId());
			}
			if (teamMemberFilterDTO.isEmptyFilter()) {
				map.put(TOTAL_RECORDS, 0);
				map.put("activePartnesList", new ArrayList<Object[]>());
				return map;
			} else {
				if (applyTeamMemberFilter && isFilterOption) {
					sql += "  and xcp1.company_id in (:partnerCompanyIds) ";
				}
			}

			if (StringUtils.hasText(pagination.getSearchKey())) {
				String searchKey = " LOWER('%" + pagination.getSearchKey() + "%')";
				String searchQuery = " and(LOWER(xcp1.company_name) like " + searchKey + ")";
				sql += searchQuery;
			}

			listQuery = sql + dateFilterQueryString + groupByQuery;
			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(listQuery);
			if (pagination.getSelectedPartnerCompanyIds() != null
					&& !pagination.getSelectedPartnerCompanyIds().isEmpty()) {
				query.setParameterList("partnerCompanyIds", pagination.getSelectedPartnerCompanyIds());
			}
			if (isFilterOption) {
				utilDao.applyPartnerCompanyIdsParameterList(applyTeamMemberFilter,
						teamMemberFilterDTO.getPartnershipIdsOrPartnerCompanyIds(), query);
			}
			ScrollableResults scrollableResults = query.scroll();
			scrollableResults.last();
			Integer totalRecords = scrollableResults.getRowNumber() + 1;
			if (!pagination.isExcludeLimit()) {
				query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
				query.setMaxResults(pagination.getMaxResults());
			}
			map.put(TOTAL_RECORDS, totalRecords);
			map.put("activePartnesList", query.list());
			return map;
		} catch (HibernateException | PartnerDataAccessException e) {
			logger.error("listNoOfCampaignsLaunchedByPartner(" + pagination.getCompanyId() + ")", e);
			throw new PartnerDataAccessException(e);
		} catch (Exception ex) {
			logger.error("listNoOfCampaignsLaunchedByPartner(" + pagination.getCompanyId() + ")", ex);
			throw new PartnerDataAccessException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> listCountrywisePartnersCount(Integer userId, Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = " select distinct on (uul.country) uul.country, count(distinct uul.user_id)  from xt_user_userlist uul where uul.user_list_id in "
				+ " (select user_list_id from xt_user_list ul where ul.customer_id=" + userId
				+ " and ul.is_partner_userlist=true) " + " and uul.user_id " + " in "
				+ " (select u.user_id from xt_user_profile u,xt_campaign c where c.customer_id = u.user_id and is_nurture_campaign  = true "
				+ " and is_launched = true and c.vendor_organization_id = " + companyId + " group by u.user_id) "
				+ " and uul.country is not null group by uul.country ";
		SQLQuery query = session.createSQLQuery(sql);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> listPartnerCampaigns(Integer companyId, Pagination pagination) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String queryString;
			String userCompanyQueryString = "(select c1.company_name from  xt_company_profile c1,xt_user_profile u1 where u1.company_id = c1.company_id  and u1.company_id = u.company_id and u1.user_id= pc.partner_id)";
			String sql = "SELECT cp.company_id,pc.partner_id,u.email_id as partner_name,c.campaign_id,c.campaign_name,u.firstname,u.lastname, "
					+ userCompanyQueryString
					+ " ,c.is_detailed_analytics_shared,c.launch_time,c.is_data_share FROM xt_company_profile cp,xt_partnership pc,xt_campaign c, "
					+ " xt_user_profile u,xt_campaign_partner xcp WHERE cp.company_id=pc.vendor_company_id and pc.partner_id=c.customer_id and pc.partner_id=u.user_id and is_launched=true "
					+ " and cp.company_id=" + companyId
					+ " and xcp.email_template_id=c.email_template_id and xcp.company_id=" + companyId
					+ " and c.vendor_organization_id=" + companyId;
			if (StringUtils.hasText(pagination.getSearchKey())) {
				String searchKey = "'%" + pagination.getSearchKey() + "%'";
				String searchQueryString = " and  (u.email_id like LOWER(" + searchKey + ")"
						+ " or LOWER(c.campaign_name) like LOWER(" + searchKey + ")"
						+ " or LOWER(u.firstname) like LOWER(" + searchKey + ")" + " or LOWER(u.lastname) like LOWER("
						+ searchKey + ")" + " or LOWER(" + userCompanyQueryString + ") like LOWER(" + searchKey
						+ ") ) ";
				queryString = sql + searchQueryString;
			} else {
				queryString = sql;
			}
			SQLQuery query = session.createSQLQuery(queryString);
			ScrollableResults scrollableResults = query.scroll();
			scrollableResults.last();
			Integer totalRecords = scrollableResults.getRowNumber() + 1;
			query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
			query.setMaxResults(pagination.getMaxResults());
			List<Object[]> data = query.list();
			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put(TOTAL_RECORDS, totalRecords);
			resultMap.put("data", data);
			return resultMap;
		} catch (HibernateException e) {
			throw new PartnerDataAccessException(e);
		} catch (Exception ex) {
			throw new PartnerDataAccessException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> listPartnerCampaignInteraction(Integer campaignId, Pagination pagination) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select c.campaign_name, u.firstname,u.lastname, u.email_id,xt.city, xt.state, xt.country, min(xt.start_time) as played_time, xt.device_type as Device,  "
				+ " COUNT(distinct(case when  Action_Id <=9 THEN Session_Id end)) + COUNT(distinct(case when  Action_Id =10 THEN session_id end)) as views_count "
				+ " from xt_xtremand_log xt ,xt_user_profile u, xt_campaign c where u.user_id=xt.user_id and c.campaign_id=xt.campaign_id and  c.campaign_id="
				+ campaignId
				+ " group by c.campaign_name,u.firstname,u.lastname,u.email_id,xt.device_type,u.email_id,xt.city, xt.state, xt.country ";
		SQLQuery query = session.createSQLQuery(sql);
		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		query.setMaxResults(pagination.getMaxResults());
		List<Object[]> data = query.list();
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put(TOTAL_RECORDS, totalRecords);
		resultMap.put("data", data);
		return resultMap;
	}

	@Override
	public Integer getThroughPartnerCampaignsCount(Integer companyId, boolean applyFilter, Integer userId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			List<Integer> userIds = userDao.listAllUserIdsByCompanyId(companyId);
			TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(userId, applyFilter, false);
			boolean applyTeamMemberFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
			if (teamMemberFilterDTO.isEmptyFilter()) {
				return 0;
			} else {
				if (applyTeamMemberFilter) {
					String sql = "select count(distinct c.campaign_id) from xt_campaign c \r\n"
							+ "left join xt_user_profile xup on c.customer_id =xup.user_id \r\n"
							+ "left join xt_partnership xp on xp.partner_company_id =xup.company_id\r\n"
							+ "where is_channel_campaign= true and is_launched = true \r\n"
							+ "and xup.company_id = :company_id \r\n" + "and xp.partner_company_id in \r\n"
							+ "(select distinct p.partner_company_id\r\n" + "from xt_team_member t\r\n"
							+ "left outer join xt_team_member_group_user_mapping tgum on t.id = tgum.team_member_id\r\n"
							+ "left outer join xt_partner_team_group_mapping ptgm on tgum.id = ptgm.team_member_group_user_mapping_id\r\n"
							+ "left outer join xt_partnership p on ptgm.partnership_id=p.id\r\n"
							+ "left join xt_campaign xc on xc.customer_id=tgum.team_member_id\r\n"
							+ "where t.team_member_id = :team_member_id \r\n"
							+ "and p.partner_id is not null and p.status='approved')";
					SQLQuery query = session.createSQLQuery(sql);
					query.setParameter("company_id", companyId);
					query.setParameter("team_member_id", userId);
					return query.uniqueResult() != null ? ((BigInteger) query.uniqueResult()).intValue() : 0;
				} else {
					String sql = "select count(*) from xt_campaign where is_channel_campaign= true and is_launched = true and customer_id in(:customerIds)";
					SQLQuery query = session.createSQLQuery(sql);
					query.setParameterList("customerIds", userIds);
					return query.uniqueResult() != null ? ((BigInteger) query.uniqueResult()).intValue() : 0;
				}
			}
		} catch (HibernateException | PartnerDataAccessException e) {
			logger.error("Error In getThroughPartnerCampaignsCount(" + companyId + ")", e);
			throw new PartnerDataAccessException(e.getMessage());
		} catch (Exception ex) {
			logger.error("Error In getThroughPartnerCampaignsCount(" + companyId + ")", ex);
			throw new PartnerDataAccessException(ex.getMessage());
		}
	}

	@Override
	public Map<String, Object> listInActiveCampaignPartners(Pagination pagination) {
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), false);
		Session session = sessionFactory.getCurrentSession();
		Integer userId = pagination.getUserId();
		Integer companyId = userDao.getCompanyIdByUserId(userId);
		String searchQuery = "";
		String partnershipStatus = "approved";
		if (StringUtils.hasText(pagination.getSearchKey())) {
			String searchKey = "'%" + pagination.getSearchKey() + "%'";
			searchQuery = " and (xup1.email_id ilike " + searchKey + " OR " + "xcp.company_name ilike " + searchKey
					+ " OR " + "xuul.firstname ilike " + searchKey + " OR " + "xuul.lastname ilike " + searchKey
					+ " OR " + "xuul.contact_company ilike " + searchKey + " OR " + "xuul.job_title ilike " + searchKey
					+ " OR " + "xuul.mobile_number ilike " + searchKey + ") ";
		}

		String sql = "select a.* from(SELECT DISTINCT xp.partner_id as \"partnerId\", xup1.company_id AS \"companyId\", "
				+ "xcp.company_logo AS \"companyLogo\", " + "xcp.company_name AS \"companyName\", "
				+ "xuul.contact_company AS \"partnerCompanyName\", " + "xup1.email_id AS \"emailId\", "
				+ "xuul.firstname AS \"firstName\", xuul.lastname AS \"lastName\", "
				+ "xuul.job_title AS \"jobTitle\", " + "xuul.mobile_number AS \"mobileNumber\", "
				+ "xp.created_time AS \"createdDate\", cast(xrel.time as text)as \"time\",ROW_NUMBER() OVER (PARTITION BY xp.partner_id ORDER BY xrel.time DESC) AS rn "
				+ "FROM xt_partnership xp " + "LEFT JOIN xt_user_list xul ON xul.company_id = xp.vendor_company_id "
				+ "LEFT JOIN xt_user_userlist xuul ON xul.user_list_id = xuul.user_list_id AND xp.partner_id = xuul.user_id "
				+ "LEFT JOIN xt_user_profile xup1 ON xuul.user_id = xup1.user_id "
				+ "LEFT JOIN xt_company_profile xcp ON xcp.company_id = xup1.company_id "
				+ "LEFT JOIN xt_reminder_email_log xrel ON xrel.partner_id = xp.partner_id "
				+ "WHERE xp.vendor_company_id = " + companyId + " AND xul.is_default_partnerlist = TRUE "
				+ " AND xup1.company_id IS NOT NULL "
				+ " AND xp.status = 'approved' and xcp.company_name_status='active' "
				+ getPartnerTeamMemberGroupFilterSQL("xup1.company_id", userId, teamMemberFilterDTO, partnershipStatus)
				+ searchQuery + " AND xp.partner_company_id NOT IN " + "(SELECT DISTINCT up.company_id "
				+ "FROM xt_lead l " + "LEFT JOIN xt_user_profile up ON l.created_by = up.user_id "
				+ "WHERE l.created_for_company_id = " + companyId
				+ " AND l.created_for_company_id != l.created_by_company_id " + " AND up.company_id IS NOT NULL "
				+ getPartnerTeamMemberGroupFilterSQL("l.created_by_company_id", userId, teamMemberFilterDTO,
						partnershipStatus)
				+ "  " + "UNION " + "SELECT DISTINCT up.company_id " + "FROM xt_deal d "
				+ "LEFT JOIN xt_user_profile up ON d.created_by = up.user_id " + "WHERE d.created_for_company_id = "
				+ companyId + " AND d.created_for_company_id != d.created_by_company_id "
				+ " AND up.company_id IS NOT NULL "
				+ getPartnerTeamMemberGroupFilterSQL("d.created_by_company_id", userId, teamMemberFilterDTO,
						partnershipStatus)
				+ " " + "UNION " + "SELECT DISTINCT p.partner_company_id " + "FROM xt_learning_track_visibility xltv "
				+ "LEFT JOIN xt_learning_track xlt ON xltv.learning_track_id = xlt.id "
				+ "LEFT JOIN xt_partnership p ON p.vendor_company_id = xlt.company_id "
				+ " AND xltv.partnership_id = p.id "
				+ "LEFT JOIN xt_learning_track_content_partner_activity xltcp ON xltcp.learning_track_visibility_id = xltv.id "
				+ "WHERE p.vendor_company_id = " + companyId
				+ " AND p.partner_company_id IS NOT NULL and xlt.type = 'TRACK' and xlt.is_published = true and xltv.progress>0 "
				+ getPartnerTeamMemberGroupFilterSQL("p.partner_company_id", userId, teamMemberFilterDTO,
						partnershipStatus)
				+ " " + "UNION " + "SELECT DISTINCT p.partner_company_id " + "FROM xt_learning_track_visibility xltv "
				+ "LEFT JOIN xt_learning_track xlt ON xltv.learning_track_id = xlt.id "
				+ "LEFT JOIN xt_partnership p ON p.vendor_company_id = xlt.company_id "
				+ " AND xltv.partnership_id = p.id "
				+ "LEFT JOIN xt_learning_track_content_partner_activity xltcp ON xltcp.learning_track_visibility_id = xltv.id "
				+ "WHERE p.vendor_company_id = " + companyId
				+ " AND p.partner_company_id IS NOT NULL and xlt.type = 'PLAYBOOK' and xlt.is_published = true and xltv.progress>0 "
				+ getPartnerTeamMemberGroupFilterSQL("p.partner_company_id", userId, teamMemberFilterDTO,
						partnershipStatus)
				+ " " + "UNION " + "SELECT DISTINCT xp.partner_company_id " + "FROM xt_dam_partner xdp "
				+ "LEFT JOIN xt_dam xd ON xd.id = xdp.dam_id "
				+ "LEFT JOIN xt_dam_partner_mapping xdpm ON xdpm.dam_partner_id = xdp.id "
				+ "left join xt_dam_analytics xda on xda.dam_partner_mapping_fk_id=xdpm.id "
				+ "LEFT JOIN xt_partnership xp ON xp.id = xdp.partnership_id "
				+ " AND xd.company_id = xp.vendor_company_id " + "WHERE xp.vendor_company_id = " + companyId
				+ " AND xp.partner_company_id IS NOT NULL and xda.action_type ='VIEW' "
				+ getPartnerTeamMemberGroupFilterSQL("xp.partner_company_id", userId, teamMemberFilterDTO,
						partnershipStatus)
				+ " " + "UNION " + "SELECT distinct p.partner_company_id " + "FROM xt_dam_partner dp "
				+ "JOIN xt_dam d on dp.dam_id = d.id "
				+ "JOIN xt_partnership p on p.id=dp.partnership_id and d.company_id =p.vendor_company_id "
				+ "JOIN xt_company_profile c on p.partner_company_id = c.company_id "
				+ "JOIN xt_dam_partner_group_mapping dpgm on dp.id = dpgm.dam_partner_id "
				+ "left join xt_dam_analytics xda on xda.dam_partner_fk_id=dp.id " + "WHERE p.vendor_company_id = "
				+ companyId + " and xda.action_type ='VIEW' "
				+ getPartnerTeamMemberGroupFilterSQL("p.partner_company_id", userId, teamMemberFilterDTO,
						partnershipStatus)
				+ " " + "UNION " + "SELECT DISTINCT up1.company_id " + "FROM xt_campaign c "
				+ "LEFT JOIN xt_campaign p ON (c.campaign_id = p.parent_campaign_id) "
				+ "LEFT JOIN xt_user_profile up ON (up.user_id = c.customer_id) "
				+ "LEFT JOIN xt_user_profile up1 ON (up1.user_id = p.customer_id) " + "WHERE up.company_id = "
				+ companyId + " AND p.vendor_organization_id = " + companyId + " AND p.is_launched = TRUE "
				+ " AND p.is_nurture_campaign = TRUE " + " AND p.campaign_id IS NOT NULL "
				+ " AND up1.company_id IS NOT NULL "
				+ getPartnerTeamMemberGroupFilterSQL("up1.company_id", userId, teamMemberFilterDTO, partnershipStatus)
				+ ")" + ")a";

		String sortColumn = pagination.getSortcolumn();
		String sortOrder = pagination.getSortingOrder();
		String sort = "";

		if ("time".equalsIgnoreCase(sortColumn) && XamplifyUtils.isValidString(sortOrder)) {
			String safeOrder = "asc".equalsIgnoreCase(sortOrder) ? "ASC" : "DESC";
			sort = "  WHERE rn=1 order by \"time\" " + safeOrder + " nulls last";
		} else {
			sort = "  WHERE rn=1 order by \"time\" desc nulls last";
		}

		sql = sql + sort;
		SQLQuery query = session.createSQLQuery(sql);
		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		query.setMaxResults(pagination.getMaxResults());
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> list = query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("totalRecords", totalRecords);
		resultMap.put("inactivePartnerList", list);
		return resultMap;
	}

	@Override
	public Integer getInactivePartnersCount(Integer companyId, boolean applyFilter, Integer userId) {
		Session session = sessionFactory.getCurrentSession();
		String partnerTeamMemberGroupFilterSQL = "";
		String partnershipStatus = "approved";
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(userId, applyFilter, false);

		String sql = "SELECT CAST(COUNT(DISTINCT xup1.company_id) as Integer) AS \"Active Partners\" "
				+ "FROM xt_partnership xp " + "LEFT JOIN xt_user_list xul ON xul.company_id = xp.vendor_company_id "
				+ "LEFT JOIN xt_user_userlist xuul ON xul.user_list_id = xuul.user_list_id AND xp.partner_id = xuul.user_id "
				+ "LEFT JOIN xt_user_profile xup1 ON xuul.user_id = xup1.user_id "
				+ "LEFT JOIN xt_company_profile xcp ON xcp.company_id = xup1.company_id "
				+ "WHERE xp.vendor_company_id = " + companyId + " AND xul.is_default_partnerlist = TRUE "
				+ " AND xup1.company_id IS NOT NULL "
				+ " AND xp.status = 'approved' and xcp.company_name_status='active' "
				+ getPartnerTeamMemberGroupFilterSQL("xup1.company_id", userId, teamMemberFilterDTO, partnershipStatus)
				+ " AND xp.partner_company_id NOT IN " + "(SELECT DISTINCT up.company_id " + "FROM xt_lead l "
				+ "LEFT JOIN xt_user_profile up ON l.created_by = up.user_id " + "WHERE l.created_for_company_id = "
				+ companyId + " AND l.created_for_company_id != l.created_by_company_id "
				+ " AND up.company_id IS NOT NULL "
				+ getPartnerTeamMemberGroupFilterSQL("l.created_by_company_id", userId, teamMemberFilterDTO,
						partnershipStatus)
				+ "  " + "UNION " + "SELECT DISTINCT up.company_id " + "FROM xt_deal d "
				+ "LEFT JOIN xt_user_profile up ON d.created_by = up.user_id " + "WHERE d.created_for_company_id = "
				+ companyId + " AND d.created_for_company_id != d.created_by_company_id "
				+ " AND up.company_id IS NOT NULL "
				+ getPartnerTeamMemberGroupFilterSQL("d.created_by_company_id", userId, teamMemberFilterDTO,
						partnershipStatus)
				+ " " + "UNION " + "SELECT DISTINCT p.partner_company_id " + "FROM xt_learning_track_visibility xltv "
				+ "LEFT JOIN xt_learning_track xlt ON xltv.learning_track_id = xlt.id "
				+ "LEFT JOIN xt_partnership p ON p.vendor_company_id = xlt.company_id "
				+ " AND xltv.partnership_id = p.id "
				+ "LEFT JOIN xt_learning_track_content_partner_activity xltcp ON xltcp.learning_track_visibility_id = xltv.id "
				+ "WHERE p.vendor_company_id = " + companyId
				+ " AND p.partner_company_id IS NOT NULL and xlt.type = 'TRACK' and xlt.is_published = true and xltv.progress>0 "
				+ getPartnerTeamMemberGroupFilterSQL("p.partner_company_id", userId, teamMemberFilterDTO,
						partnershipStatus)
				+ " " + "UNION " + "SELECT DISTINCT p.partner_company_id " + "FROM xt_learning_track_visibility xltv "
				+ "LEFT JOIN xt_learning_track xlt ON xltv.learning_track_id = xlt.id "
				+ "LEFT JOIN xt_partnership p ON p.vendor_company_id = xlt.company_id "
				+ " AND xltv.partnership_id = p.id "
				+ "LEFT JOIN xt_learning_track_content_partner_activity xltcp ON xltcp.learning_track_visibility_id = xltv.id "
				+ "WHERE p.vendor_company_id = " + companyId
				+ " AND p.partner_company_id IS NOT NULL and xlt.type = 'PLAYBOOK' and xlt.is_published = true and xltv.progress>0 "
				+ getPartnerTeamMemberGroupFilterSQL("p.partner_company_id", userId, teamMemberFilterDTO,
						partnershipStatus)
				+ " " + "UNION " + "SELECT DISTINCT xp.partner_company_id " + "FROM xt_dam_partner xdp "
				+ "LEFT JOIN xt_dam xd ON xd.id = xdp.dam_id "
				+ "LEFT JOIN xt_dam_partner_mapping xdpm ON xdpm.dam_partner_id = xdp.id "
				+ "left join xt_dam_analytics xda on xda.dam_partner_mapping_fk_id=xdpm.id "
				+ "LEFT JOIN xt_partnership xp ON xp.id = xdp.partnership_id "
				+ " AND xd.company_id = xp.vendor_company_id " + "WHERE xp.vendor_company_id = " + companyId
				+ " AND xp.partner_company_id IS NOT NULL and xda.action_type ='VIEW' "
				+ getPartnerTeamMemberGroupFilterSQL("xp.partner_company_id", userId, teamMemberFilterDTO,
						partnershipStatus)
				+ " " + "UNION " + "SELECT distinct p.partner_company_id " + "FROM xt_dam_partner dp "
				+ "JOIN xt_dam d on dp.dam_id = d.id "
				+ "JOIN xt_partnership p on p.id=dp.partnership_id and d.company_id =p.vendor_company_id "
				+ "JOIN xt_company_profile c on p.partner_company_id = c.company_id "
				+ "JOIN xt_dam_partner_group_mapping dpgm on dp.id = dpgm.dam_partner_id "
				+ "left join xt_dam_analytics xda on xda.dam_partner_fk_id=dp.id " + "WHERE p.vendor_company_id = "
				+ companyId + " and xda.action_type ='VIEW' "
				+ getPartnerTeamMemberGroupFilterSQL("p.partner_company_id", userId, teamMemberFilterDTO,
						partnershipStatus)
				+ " " + "UNION " + "SELECT DISTINCT up1.company_id " + "FROM xt_campaign c "
				+ "LEFT JOIN xt_campaign p ON (c.campaign_id = p.parent_campaign_id) "
				+ "LEFT JOIN xt_user_profile up ON (up.user_id = c.customer_id) "
				+ "LEFT JOIN xt_user_profile up1 ON (up1.user_id = p.customer_id) " + "WHERE up.company_id = "
				+ companyId + " AND p.vendor_organization_id = " + companyId + " AND p.is_launched = TRUE "
				+ " AND p.is_nurture_campaign = TRUE " + " AND p.campaign_id IS NOT NULL "
				+ " AND up1.company_id IS NOT NULL "
				+ getPartnerTeamMemberGroupFilterSQL("up1.company_id", userId, teamMemberFilterDTO, partnershipStatus)
				+ ")";
		SQLQuery query = session.createSQLQuery(sql);
		return query.uniqueResult() != null ? (Integer) query.uniqueResult() : 0;

	}

	private String getPartnerTeamMemberGroupFilterSQL(String alias, Integer userId,
			TeamMemberFilterDTO teamMemberFilterDTO, String status) {
		String partnerTeamMemberGroupFilterSQL = "";
		if ((teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter())) {
			partnerTeamMemberGroupFilterSQL = " AND  " + alias + " IN (" + "SELECT DISTINCT p.partner_company_id "
					+ "FROM xt_team_member t "
					+ "LEFT OUTER JOIN xt_team_member_group_user_mapping tgum ON t.id = tgum.team_member_id "
					+ "LEFT OUTER JOIN xt_partner_team_group_mapping ptgm ON tgum.id = ptgm.team_member_group_user_mapping_id "
					+ "LEFT OUTER JOIN xt_partnership p ON ptgm.partnership_id = p.id "
					+ "LEFT JOIN xt_campaign xc ON xc.customer_id = tgum.team_member_id " + "WHERE t.team_member_id = "
					+ userId + " AND p.partner_id IS NOT NULL AND p.status = {status} ) ";
			partnerTeamMemberGroupFilterSQL = replacePartnershipStatus(partnerTeamMemberGroupFilterSQL, status);
		}
		return partnerTeamMemberGroupFilterSQL;
	}

	@Override
	public Integer getActivePartnersCount(Integer companyId, boolean applyFilter, Integer userId) {
		Session session = sessionFactory.getCurrentSession();
		String partnershipStatus = "approved";
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(userId, applyFilter, false);

		String sql = "SELECT CAST(COUNT(DISTINCT xup1.company_id) as Integer) AS \"Active Partners\" "
				+ "FROM xt_partnership xp " + "LEFT JOIN xt_user_list xul ON xul.company_id = xp.vendor_company_id "
				+ "LEFT JOIN xt_user_userlist xuul ON xul.user_list_id = xuul.user_list_id AND xp.partner_id = xuul.user_id "
				+ "LEFT JOIN xt_user_profile xup1 ON xuul.user_id = xup1.user_id "
				+ "LEFT JOIN xt_company_profile xcp ON xcp.company_id = xup1.company_id "
				+ "WHERE xp.vendor_company_id = " + companyId + " AND xul.is_default_partnerlist = TRUE "
				+ " AND xup1.company_id IS NOT NULL "
				+ " AND xp.status = 'approved' and xcp.company_name_status='active' "
				+ getPartnerTeamMemberGroupFilterSQL("xup1.company_id", userId, teamMemberFilterDTO, partnershipStatus)
				+ " " + " AND xp.partner_company_id IN " + "(SELECT DISTINCT up.company_id " + "FROM xt_lead l "
				+ "LEFT JOIN xt_user_profile up ON l.created_by = up.user_id " + "WHERE l.created_for_company_id = "
				+ companyId + " AND l.created_for_company_id != l.created_by_company_id "
				+ " AND up.company_id IS NOT NULL "
				+ getPartnerTeamMemberGroupFilterSQL("l.created_by_company_id", userId, teamMemberFilterDTO,
						partnershipStatus)
				+ "  " + "UNION " + "SELECT DISTINCT up.company_id " + "FROM xt_deal d "
				+ "LEFT JOIN xt_user_profile up ON d.created_by = up.user_id " + "WHERE d.created_for_company_id = "
				+ companyId + " AND d.created_for_company_id != d.created_by_company_id "
				+ " AND up.company_id IS NOT NULL "
				+ getPartnerTeamMemberGroupFilterSQL("d.created_by_company_id", userId, teamMemberFilterDTO,
						partnershipStatus)
				+ " " + "UNION " + "SELECT DISTINCT p.partner_company_id " + "FROM xt_learning_track_visibility xltv "
				+ "LEFT JOIN xt_learning_track xlt ON xltv.learning_track_id = xlt.id "
				+ "LEFT JOIN xt_partnership p ON p.vendor_company_id = xlt.company_id "
				+ " AND xltv.partnership_id = p.id "
				+ "LEFT JOIN xt_learning_track_content_partner_activity xltcp ON xltcp.learning_track_visibility_id = xltv.id "
				+ "WHERE p.vendor_company_id = " + companyId
				+ " AND p.partner_company_id IS NOT NULL and xlt.type = 'TRACK' and xlt.is_published = true and xltv.progress>0 "
				+ getPartnerTeamMemberGroupFilterSQL("p.partner_company_id", userId, teamMemberFilterDTO,
						partnershipStatus)
				+ " " + "UNION " + "SELECT DISTINCT p.partner_company_id " + "FROM xt_learning_track_visibility xltv "
				+ "LEFT JOIN xt_learning_track xlt ON xltv.learning_track_id = xlt.id "
				+ "LEFT JOIN xt_partnership p ON p.vendor_company_id = xlt.company_id "
				+ " AND xltv.partnership_id = p.id "
				+ "LEFT JOIN xt_learning_track_content_partner_activity xltcp ON xltcp.learning_track_visibility_id = xltv.id "
				+ "WHERE p.vendor_company_id = " + companyId
				+ " AND p.partner_company_id IS NOT NULL and xlt.type = 'PLAYBOOK' and xlt.is_published = true and xltv.progress>0 "
				+ getPartnerTeamMemberGroupFilterSQL("p.partner_company_id", userId, teamMemberFilterDTO,
						partnershipStatus)
				+ " " + "UNION " + "SELECT DISTINCT xp.partner_company_id " + "FROM xt_dam_partner xdp "
				+ "LEFT JOIN xt_dam xd ON xd.id = xdp.dam_id "
				+ "LEFT JOIN xt_dam_partner_mapping xdpm ON xdpm.dam_partner_id = xdp.id "
				+ "left join xt_dam_analytics xda on xda.dam_partner_mapping_fk_id=xdpm.id "
				+ "LEFT JOIN xt_partnership xp ON xp.id = xdp.partnership_id "
				+ " AND xd.company_id = xp.vendor_company_id " + "WHERE xp.vendor_company_id = " + companyId
				+ " AND xp.partner_company_id IS NOT NULL and xda.action_type ='VIEW' "
				+ getPartnerTeamMemberGroupFilterSQL("xp.partner_company_id", userId, teamMemberFilterDTO,
						partnershipStatus)
				+ " " + "UNION " + "SELECT distinct p.partner_company_id " + "FROM xt_dam_partner dp "
				+ "JOIN xt_dam d on dp.dam_id = d.id "
				+ "JOIN xt_partnership p on p.id=dp.partnership_id and d.company_id =p.vendor_company_id "
				+ "JOIN xt_company_profile c on p.partner_company_id = c.company_id "
				+ "JOIN xt_dam_partner_group_mapping dpgm on dp.id = dpgm.dam_partner_id "
				+ "left join xt_dam_analytics xda on xda.dam_partner_fk_id=dp.id " + "WHERE p.vendor_company_id = "
				+ companyId + " and xda.action_type ='VIEW' "
				+ getPartnerTeamMemberGroupFilterSQL("p.partner_company_id", userId, teamMemberFilterDTO,
						partnershipStatus)
				+ " " + "UNION " + "SELECT DISTINCT up1.company_id " + "FROM xt_campaign c "
				+ "LEFT JOIN xt_campaign p ON (c.campaign_id = p.parent_campaign_id) "
				+ "LEFT JOIN xt_user_profile up ON (up.user_id = c.customer_id) "
				+ "LEFT JOIN xt_user_profile up1 ON (up1.user_id = p.customer_id) " + "WHERE up.company_id = "
				+ companyId + " AND p.vendor_organization_id = " + companyId + " AND p.is_launched = TRUE "
				+ " AND p.is_nurture_campaign = TRUE " + " AND p.campaign_id IS NOT NULL "
				+ " AND up1.company_id IS NOT NULL "
				+ getPartnerTeamMemberGroupFilterSQL("up1.company_id", userId, teamMemberFilterDTO, partnershipStatus)
				+ ")";
		SQLQuery query = session.createSQLQuery(sql);
		return query.uniqueResult() != null ? (Integer) query.uniqueResult() : 0;
	}

	@Override
	public Integer partnersRedistributedCampaignsCount(Integer companyId, Integer userId, boolean applyFilter) {
		Session session = sessionFactory.getCurrentSession();
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(userId, applyFilter, false);
		boolean applyTeamMemberFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
		if (teamMemberFilterDTO.isEmptyFilter()) {
			return 0;
		} else {
			if (applyTeamMemberFilter) {
				String sqlString = "SELECT  cast(COUNT(DISTINCT c.campaign_id) as int) FROM xt_campaign rc LEFT JOIN xt_campaign_user_userlist xcuu ON rc.campaign_id = xcuu.campaign_id,"
						+ "  xt_user_profile u,xt_company_profile cp,xt_campaign c  WHERE u.user_id = c.customer_id AND c.is_channel_campaign AND c.is_launched "
						+ " AND cp.company_id = u.company_id AND rc.parent_campaign_id IS NOT NULL   AND rc.vendor_organization_id IS NOT NULL AND rc.parent_campaign_id = "
						+ " c.campaign_id AND c.campaign_type !='LANDINGPAGE' AND rc.is_launched  and rc.customer_id in (select user_id from xt_user_profile where company_id in (:partnerCompanyIds)) "
						+ " and cp.company_id = :companyId";
				SQLQuery query = session.createSQLQuery(sqlString);
				query.setParameter("companyId", companyId);
				query.setParameterList("partnerCompanyIds", teamMemberFilterDTO.getPartnershipIdsOrPartnerCompanyIds());
				return (Integer) query.uniqueResult();
			} else {
				String sql = "select count(*) from v_channel_campaigns where company_id = " + companyId;
				SQLQuery query = session.createSQLQuery(sql);
				return query.uniqueResult() != null ? ((BigInteger) query.uniqueResult()).intValue() : 0;
			}
		}

	}

	@Override
	public Integer partnersRedistributedCampaignsCount(List<Integer> userIds) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select count(*) from xt_campaign where parent_campaign_id is not null and is_channel_campaign=false and is_nurture_campaign=true "
				+ " and vendor_organization_id is not null and is_launched = true and customer_id in :userIds";
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameterList("userIds", userIds);
		return query.uniqueResult() != null ? ((BigInteger) query.uniqueResult()).intValue() : 0;
	}

	/**
	 * @param pagination
	 */
	private String getSortedOptionValues(Pagination pagination) {
		String sortOptionQueryString = "";
		if (pagination != null) {
			sortOptionQueryString = " order by ";
			if ("createdTime".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += "c.launch_time " + pagination.getSortingOrder();
			} else if ("campaign".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += "c.campaign_name " + pagination.getSortingOrder();
			} else if ("launchedBy".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += pagination.getSortcolumn() + " " + pagination.getSortingOrder();
			} else {
				sortOptionQueryString += "c.launch_time DESC";
			}
		}
		return sortOptionQueryString;
	}

	@SuppressWarnings("unchecked")
	private List<Integer> getChildCampaignIds(Integer campaignId) {
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(
				"select campaign_id from xt_campaign where parent_campaign_id=:parentCampaignId and vendor_organization_id is not null");
		query.setInteger(PARENT_CAMPAIGN_ID, campaignId);
		return query.list();
	}

	@Override
	public Integer getThroughPartnersCampaignRedistributedCount(Integer campaignId) {
		return 0;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map<String, Object> listRedistributedCampaigns(Integer campaignId, Pagination pagination) {
		return null;
	}

	@Override
	public ReminderEmailLog getReminderEmailLog(Integer vendorId, Integer partnerId) {
		logger.debug("entered in HibernatePartnerAnalyticsDAO getReminderEmailLog() mehtod");
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(ReminderEmailLog.class);
		criteria.add(Restrictions.eq("vendorId", vendorId));
		criteria.add(Restrictions.eq("partnerId", partnerId));
		ReminderEmailLog reminderEmailLog = (ReminderEmailLog) criteria.uniqueResult();
		return reminderEmailLog;
	}


	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> findPartnerCompanyNamesAndLeadsAndDealsCount(
			PartnerJourneyRequestDTO partnerJourneyRequestDTO, Integer companyId) {
		Integer userId = partnerJourneyRequestDTO.getLoggedInUserId();
		String filterType = partnerJourneyRequestDTO.getFilterType();
		boolean applyFilter = partnerJourneyRequestDTO.isPartnerTeamMemberGroupFilter();
		String partnershipStatus = partnerJourneyRequestDTO.getPartnershipStatus() != null
				? partnerJourneyRequestDTO.getPartnershipStatus()
				: "approved";
		Session session = sessionFactory.getCurrentSession();
		String queryString = frameAllPartnerCompanyNamesAndLeadsAndDealsCountQuery(partnerJourneyRequestDTO);
		queryString = replacePartnershipStatus(queryString, partnershipStatus);
		String updatedQuery = "";
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(userId, applyFilter, false);
		boolean applyTeamMemberFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
		if (teamMemberFilterDTO.isEmptyFilter()) {
			return new ArrayList<>();
		} else {
			if (applyTeamMemberFilter) {
				updatedQuery = "l".equals(filterType)
						? leadsAndDealsCountTeamMemberFilterQuery + " " + leadsOrderByQuery
						: leadsAndDealsCountTeamMemberFilterQuery + " " + dealsOrderByQuery;
			} else {
				updatedQuery = "l".equals(filterType) ? queryString + " " + leadsOrderByQuery
						: queryString + " " + dealsOrderByQuery;
			}
		}
		SQLQuery query = session.createSQLQuery(updatedQuery);
		query.setParameter(COMPANY_ID, companyId);
		utilDao.applyPartnerCompanyIdsParameterList(applyTeamMemberFilter,
				teamMemberFilterDTO.getPartnershipIdsOrPartnerCompanyIds(), query);
		return query.list();

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> findAllPartnerCompanyNamesAndLeadsAndDealsCount(
			PartnerJourneyRequestDTO partnerJourneyRequestDTO, Integer companyId) {
		Integer userId = partnerJourneyRequestDTO.getLoggedInUserId();
		String filterType = partnerJourneyRequestDTO.getFilterType();
		boolean applyFilter = partnerJourneyRequestDTO.isPartnerTeamMemberGroupFilter();
		String partnershipStatus = partnerJourneyRequestDTO.getPartnershipStatus() != null
				? partnerJourneyRequestDTO.getPartnershipStatus()
				: "approved";
		Session session = sessionFactory.getCurrentSession();
		String updatedQuery = "";
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(userId, applyFilter, false);
		boolean applyTeamMemberFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
		if (teamMemberFilterDTO.isEmptyFilter()) {
			return new ArrayList<>();
		} else {
			if (applyTeamMemberFilter) {
				updatedQuery = "l".equals(filterType) ? leadsAndDealsCountTeamMemberFilterQuery
						: leadsAndDealsCountTeamMemberFilterQuery;
			} else {
				updatedQuery = frameAllPartnerCompanyNamesAndLeadsAndDealsCountQuery(partnerJourneyRequestDTO);
				updatedQuery = replacePartnershipStatus(updatedQuery, partnershipStatus);
			}
		}
		SQLQuery query = session.createSQLQuery(updatedQuery);
		query.setParameter(COMPANY_ID, companyId);
		utilDao.applyPartnerCompanyIdsParameterList(applyTeamMemberFilter,
				teamMemberFilterDTO.getPartnershipIdsOrPartnerCompanyIds(), query);
		return query.list();
	}

	@Override
	public String findLeadsToDealsConversionPercentageAsText(Integer companyId, Integer userId, boolean applyFilter) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = vendorLeadToDealConversionPieChart;
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(userId, applyFilter, false);
		boolean applyTeamMemberFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
		if (teamMemberFilterDTO.isEmptyFilter()) {
			return "";
		} else {
			if (applyTeamMemberFilter) {
				queryString = vendorLeadToDealConversionPieChart
						+ " and xl.created_by_company_id in (:partnerCompanyIds)";
			}
		}
		SQLQuery query = session.createSQLQuery(queryString);
		query.setParameter(USER_ID, userId);
		utilDao.applyPartnerCompanyIdsParameterList(applyTeamMemberFilter,
				teamMemberFilterDTO.getPartnershipIdsOrPartnerCompanyIds(), query);
		return (String) query.uniqueResult();
	}

	@Override
	public Double findOpportunityAmount(Integer companyId, Integer userId, boolean applyFilter) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = opportunityAmountQuery;
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(userId, applyFilter, false);
		boolean applyTeamMemberFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
		if (teamMemberFilterDTO.isEmptyFilter()) {
			return new Double(0);
		} else {
			if (applyTeamMemberFilter) {
				queryString = opportunityAmountQuery + " and xd.created_by_company_id in (:partnerCompanyIds)";
			}
		}
		SQLQuery query = session.createSQLQuery(queryString);
		query.setParameter(COMPANY_ID, companyId);
		utilDao.applyPartnerCompanyIdsParameterList(applyTeamMemberFilter,
				teamMemberFilterDTO.getPartnershipIdsOrPartnerCompanyIds(), query);
		return (Double) query.uniqueResult();

	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> findChannelCampaigns(Pagination pagination) {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> findRedistributedCampaigns(Pagination pagination) {
		return null;
	}

	@Override
	public PartnerCompanyDTO getPartnerJourneyCompanyInfo(Integer vendorCompanyId, Integer partnerCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(partnerJourneyCompanyInfoQuery);
		query.setParameter("vendorCompanyId", vendorCompanyId);
		query.setParameter("partnerCompanyId", partnerCompanyId);
		return (PartnerCompanyDTO) paginationUtil.getDto(PartnerCompanyDTO.class, query);
	}

	@Override
	public Map<String, Object> getPartnerJourneyTeamInfo(Pagination pagination) {
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), true);
		HashMap<String, Object> map = new HashMap<>();
		String finalQueryString = "";
		String searchKey = pagination.getSearchKey();
		String partnershipStatus = pagination.getPartnershipStatus() != null ? pagination.getPartnershipStatus()
				: "approved";
		boolean hasSearchKey = StringUtils.hasText(searchKey);
		String teamMembersQueryString = "";
		teamMembersQueryString = findAllTeamMembers;
		String dateFilterQueryString = frameDateFilterQuery(pagination, "u.datelastlogin");
		teamMembersQueryString += dateFilterQueryString;

		if (hasSearchKey) {
			finalQueryString += teamMembersQueryString + " " + findAllTeamMembersSearch.replace("searchKey", searchKey)
					+ " " + findAllTeamMembersGroupBy + " ";
		} else {
			finalQueryString += teamMembersQueryString + " " + findAllTeamMembersGroupBy + " ";
		}
		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			finalQueryString = finalQueryString.replace("{teamMemberFilterQuery}",
					" and xcp.company_id " + partnerJourneyTeamMemberFilterQuery + " ");
		} else {
			finalQueryString = finalQueryString.replace("{teamMemberFilterQuery}", "");
		}
		finalQueryString = replacePartnershipStatus(finalQueryString, partnershipStatus);
		if (pagination.getSelectedPartnerCompanyIds() != null && !pagination.getSelectedPartnerCompanyIds().isEmpty()) {
			finalQueryString = finalQueryString.replace("{partnerCompanyIds}",
					" and xcp.company_id in (:partnerCompanyIds)");
		} else {
			finalQueryString = finalQueryString.replace("{partnerCompanyIds}", "");
		}
		if ("nameAsc".equalsIgnoreCase(pagination.getSortcolumn())) {
			finalQueryString += " ORDER BY u.firstname ASC nulls first";
		} else if ("nameDesc".equalsIgnoreCase(pagination.getSortcolumn())) {
			finalQueryString += " ORDER BY u.firstname DESC nulls last";
		} else if ("datelastloginAsc".equalsIgnoreCase(pagination.getSortcolumn())) {
			finalQueryString += " ORDER BY u.datelastlogin ASC";
		} else if ("datelastloginDesc".equalsIgnoreCase(pagination.getSortcolumn())) {
			finalQueryString += " ORDER BY u.datelastlogin DESC";
		}

		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(finalQueryString);
		Integer companyId = userDao.getCompanyIdByUserId(pagination.getUserId());
		query.setParameter(COMPANY_ID, companyId);
		query.setParameter(USER_ID, pagination.getUserId());
		if (pagination.getSelectedPartnerCompanyIds() != null && !pagination.getSelectedPartnerCompanyIds().isEmpty()) {
			query.setParameterList("partnerCompanyIds", pagination.getSelectedPartnerCompanyIds());
		}
		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			query.setParameter("teamMemberUserIdFilter", pagination.getUserId());
		}
		return paginationUtil.setScrollableAndGetList(pagination, map, query, TeamMemberListDTO.class);
	}

	@Override
	public String getPartnerJourneyRedistributedCampaignCount(PartnerJourneyRequestDTO partnerJourneyRequestDTO) {
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(
				partnerJourneyRequestDTO.getLoggedInUserId(), partnerJourneyRequestDTO.isPartnerTeamMemberGroupFilter(),
				true);
		String finalQueryString = allRedistributedCampaignCountQuery;
		String partnershipStatus = partnerJourneyRequestDTO.getPartnershipStatus() != null
				? partnerJourneyRequestDTO.getPartnershipStatus()
				: "approved";
		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			finalQueryString += " and  xup1.company_id " + partnerJourneyTeamMemberFilterQuery;
		}
		finalQueryString = replacePartnershipStatus(finalQueryString, partnershipStatus);
		if (partnerJourneyRequestDTO.isDetailedAnalytics()) {
			finalQueryString = partnerJourneyRedistributedCampaignCountQuery;
		} else {
			if (partnerJourneyRequestDTO.getSelectedPartnerCompanyIds() != null
					&& !partnerJourneyRequestDTO.getSelectedPartnerCompanyIds().isEmpty()) {
				finalQueryString += "and xup1.company_id in (:partnerCompanyIds) ";
			}
		}
		Integer teamMemberUserId = partnerJourneyRequestDTO.getTeamMemberUserId();
		if (teamMemberUserId != null && teamMemberUserId > 0) {
			finalQueryString += xup1UserIdTMFilter;
		}

		String dateFilterQueryString = frameDateFilterQuery(partnerJourneyRequestDTO, CAMPAIGN_DATE_COLUMN);
		finalQueryString += dateFilterQueryString;

		return (String) executePartnerJourneyUniqueResultQuery(finalQueryString, partnerJourneyRequestDTO, true);
	}

	@Override
	public String getPartnerJourneyTeamMemberCount(PartnerJourneyRequestDTO partnerJourneyRequestDTO) {
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(
				partnerJourneyRequestDTO.getLoggedInUserId(), partnerJourneyRequestDTO.isPartnerTeamMemberGroupFilter(),
				true);
		String finalQueryString = allTeamMemberCountQuery;
		String partnershipStatus = partnerJourneyRequestDTO.getPartnershipStatus() != null
				? partnerJourneyRequestDTO.getPartnershipStatus()
				: "approved";
		String dateFilterQuery = frameDateFilterQuery(partnerJourneyRequestDTO, "xup1.datelastlogin");

		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			finalQueryString += " and xup1.company_id " + partnerJourneyTeamMemberFilterQuery;
		}
		finalQueryString = replacePartnershipStatus(finalQueryString, partnershipStatus);
		if (partnerJourneyRequestDTO.isDetailedAnalytics()) {
			finalQueryString = partnerJourneyTeamMemberCountQuery;
			dateFilterQuery = frameDateFilterQuery(partnerJourneyRequestDTO, "xup.datelastlogin");

		} else {
			if (partnerJourneyRequestDTO.getSelectedPartnerCompanyIds() != null
					&& !partnerJourneyRequestDTO.getSelectedPartnerCompanyIds().isEmpty()) {
				finalQueryString += "and  xup1.company_id in (:partnerCompanyIds) ";
			}
		}

		finalQueryString += dateFilterQuery;

		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(finalQueryString);
		if (partnerJourneyRequestDTO.isDetailedAnalytics()) {
			query.setParameter("partnerCompanyId", partnerJourneyRequestDTO.getPartnerCompanyId());
		} else {
			query.setParameter("vendorCompanyId", partnerJourneyRequestDTO.getVendorCompanyId());
			if (partnerJourneyRequestDTO.getSelectedPartnerCompanyIds() != null
					&& !partnerJourneyRequestDTO.getSelectedPartnerCompanyIds().isEmpty()) {
				query.setParameterList("partnerCompanyIds", partnerJourneyRequestDTO.getSelectedPartnerCompanyIds());
			}
		}

		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			query.setParameter("teamMemberUserIdFilter", partnerJourneyRequestDTO.getLoggedInUserId());
		}

		return (String) query.uniqueResult();
	}

	@Override
	public String getPartnerJourneyShareLeadCount(PartnerJourneyRequestDTO partnerJourneyRequestDTO) {
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(
				partnerJourneyRequestDTO.getLoggedInUserId(), partnerJourneyRequestDTO.isPartnerTeamMemberGroupFilter(),
				true);
		String finalQueryString = allShareLeadCountQuery;
		String partnershipStatus = partnerJourneyRequestDTO.getPartnershipStatus() != null
				? partnerJourneyRequestDTO.getPartnershipStatus()
				: "approved";
		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			finalQueryString += " and xup.company_id " + partnerJourneyTeamMemberFilterQuery;
		}
		finalQueryString = replacePartnershipStatus(finalQueryString, partnershipStatus);
		if (partnerJourneyRequestDTO.isDetailedAnalytics()) {
			finalQueryString = partnerJourneyShareLeadCountQuery;
		} else {
			if (partnerJourneyRequestDTO.getSelectedPartnerCompanyIds() != null
					&& !partnerJourneyRequestDTO.getSelectedPartnerCompanyIds().isEmpty()) {
				finalQueryString += "and xul.company_id in (:partnerCompanyIds) ";
			}
		}
		Integer teamMemberUserId = partnerJourneyRequestDTO.getTeamMemberUserId();
		if (teamMemberUserId != null && teamMemberUserId > 0) {
			finalQueryString += xupUserIdTMFilter;
		}

		String dateFilterQueryString = frameDateFilterQuery(partnerJourneyRequestDTO, "xul.created_time");
		finalQueryString += dateFilterQueryString;

		return (String) executePartnerJourneyUniqueResultQuery(finalQueryString, partnerJourneyRequestDTO, true);
	}

	@Override
	public String getPartnerJourneyLeadCount(PartnerJourneyRequestDTO partnerJourneyRequestDTO) {
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(
				partnerJourneyRequestDTO.getLoggedInUserId(), partnerJourneyRequestDTO.isPartnerTeamMemberGroupFilter(),
				true);
		String finalQueryString = allLeadCountQuery;
		String partnershipStatus = partnerJourneyRequestDTO.getPartnershipStatus() != null
				? partnerJourneyRequestDTO.getPartnershipStatus()
				: "approved";
		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			finalQueryString += " and xup1.company_id " + partnerJourneyTeamMemberFilterQuery;
		}
		finalQueryString = replacePartnershipStatus(finalQueryString, partnershipStatus);
		if (partnerJourneyRequestDTO.isDetailedAnalytics()) {
			finalQueryString = partnerJourneyLeadCountQuery;
		} else {
			if (partnerJourneyRequestDTO.getSelectedPartnerCompanyIds() != null
					&& !partnerJourneyRequestDTO.getSelectedPartnerCompanyIds().isEmpty()) {
				finalQueryString += " and xl.created_by_company_id in (:partnerCompanyIds) ";
			}
		}
		Integer teamMemberUserId = partnerJourneyRequestDTO.getTeamMemberUserId();
		if (teamMemberUserId != null && teamMemberUserId > 0) {
			finalQueryString += xup1UserIdTMFilter;
		}

		String dateFilterQueryString = frameDateFilterQuery(partnerJourneyRequestDTO, LEAD_DATE_COLUMN);
		finalQueryString += dateFilterQueryString;

		return (String) executePartnerJourneyUniqueResultQuery(finalQueryString, partnerJourneyRequestDTO, true);
	}

	@Override
	public String getPartnerJourneyDealCount(PartnerJourneyRequestDTO partnerJourneyRequestDTO) {
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(
				partnerJourneyRequestDTO.getLoggedInUserId(), partnerJourneyRequestDTO.isPartnerTeamMemberGroupFilter(),
				true);
		String finalQueryString = allDealCountQuery;
		String partnershipStatus = partnerJourneyRequestDTO.getPartnershipStatus() != null
				? partnerJourneyRequestDTO.getPartnershipStatus()
				: "approved";
		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			finalQueryString += " and  xup1.company_id " + partnerJourneyTeamMemberFilterQuery;
		}
		finalQueryString = replacePartnershipStatus(finalQueryString, partnershipStatus);
		if (partnerJourneyRequestDTO.isDetailedAnalytics()) {
			finalQueryString = partnerJourneyDealCountQuery;
		} else {
			if (partnerJourneyRequestDTO.getSelectedPartnerCompanyIds() != null
					&& !partnerJourneyRequestDTO.getSelectedPartnerCompanyIds().isEmpty()) {
				finalQueryString += "and xd.created_by_company_id in (:partnerCompanyIds) ";
			}
		}
		Integer teamMemberUserId = partnerJourneyRequestDTO.getTeamMemberUserId();
		if (teamMemberUserId != null && teamMemberUserId > 0) {
			finalQueryString += xup1UserIdTMFilter;
		}

		String dateFilterQueryString = frameDateFilterQuery(partnerJourneyRequestDTO, DEAL_DATE_COLUMN);
		finalQueryString += dateFilterQueryString;

		return (String) executePartnerJourneyUniqueResultQuery(finalQueryString, partnerJourneyRequestDTO, true);
	}

	@Override
	public String getPartnerJourneyContactCount(PartnerJourneyRequestDTO partnerJourneyRequestDTO) {
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(
				partnerJourneyRequestDTO.getLoggedInUserId(), partnerJourneyRequestDTO.isPartnerTeamMemberGroupFilter(),
				true);
		String finalQueryString = allContactCountQuery;
		String partnershipStatus = partnerJourneyRequestDTO.getPartnershipStatus() != null
				? partnerJourneyRequestDTO.getPartnershipStatus()
				: "approved";
		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			finalQueryString += " and  xup.company_id " + partnerJourneyTeamMemberFilterQuery;
		}
		finalQueryString = replacePartnershipStatus(finalQueryString, partnershipStatus);
		if (partnerJourneyRequestDTO.isDetailedAnalytics()) {
			finalQueryString = partnerJourneyContactCountQuery;
		} else {
			if (partnerJourneyRequestDTO.getSelectedPartnerCompanyIds() != null
					&& !partnerJourneyRequestDTO.getSelectedPartnerCompanyIds().isEmpty()) {
				finalQueryString += "and ul.company_id in (:partnerCompanyIds) ";
			}
		}
		Integer teamMemberUserId = partnerJourneyRequestDTO.getTeamMemberUserId();
		if (teamMemberUserId != null && teamMemberUserId > 0) {
			finalQueryString += "and tm.team_member_id =:teamMemberUserId ";
		}

		String dateFilterQueryString = frameDateFilterQuery(partnerJourneyRequestDTO, CONTACT_DATE_COLUMN);
		finalQueryString += dateFilterQueryString;

		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(finalQueryString);
		if (partnerJourneyRequestDTO.isDetailedAnalytics()) {
			query.setParameter("partnerCompanyId", partnerJourneyRequestDTO.getPartnerCompanyId());
		} else {
			query.setParameter("vendorCompanyId", partnerJourneyRequestDTO.getVendorCompanyId());
			if (partnerJourneyRequestDTO.getSelectedPartnerCompanyIds() != null
					&& !partnerJourneyRequestDTO.getSelectedPartnerCompanyIds().isEmpty()) {
				query.setParameterList("partnerCompanyIds", partnerJourneyRequestDTO.getSelectedPartnerCompanyIds());
			}
		}
		if (teamMemberUserId != null && teamMemberUserId > 0) {
			query.setParameter("teamMemberUserId", partnerJourneyRequestDTO.getTeamMemberUserId());
		}
		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			query.setParameter("teamMemberUserIdFilter", partnerJourneyRequestDTO.getLoggedInUserId());
		}
		return (String) query.uniqueResult();
	}

	@Override
	public String getPartnerJourneyMdfAmount(PartnerJourneyRequestDTO partnerJourneyRequestDTO) {
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(
				partnerJourneyRequestDTO.getLoggedInUserId(), partnerJourneyRequestDTO.isPartnerTeamMemberGroupFilter(),
				true);
		String finalQueryString = allMDFAmountQuery;
		String partnershipStatus = partnerJourneyRequestDTO.getPartnershipStatus() != null
				? partnerJourneyRequestDTO.getPartnershipStatus()
				: "approved";
		String dateFilterQueryString = frameDateFilterQuery(partnerJourneyRequestDTO, "xmdh.created_time");
		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			finalQueryString += " and xp.partner_company_id " + partnerJourneyTeamMemberFilterQuery;
		}
		finalQueryString = replacePartnershipStatus(finalQueryString, partnershipStatus);
		if (partnerJourneyRequestDTO.isDetailedAnalytics()) {
			finalQueryString = partnerJourneyMDFAmountQuery;
			dateFilterQueryString = frameDateFilterQuery(partnerJourneyRequestDTO, "xmr.created_time");
		} else {
			if (partnerJourneyRequestDTO.getSelectedPartnerCompanyIds() != null
					&& !partnerJourneyRequestDTO.getSelectedPartnerCompanyIds().isEmpty()) {
				finalQueryString += "and xp.partner_company_id in (:partnerCompanyIds) ";
			}
		}
		Integer teamMemberUserId = partnerJourneyRequestDTO.getTeamMemberUserId();
		if (teamMemberUserId != null && teamMemberUserId > 0) {
			finalQueryString += xup1UserIdTMFilter;
		}

		finalQueryString += dateFilterQueryString;

		return (String) executePartnerJourneyUniqueResultQuery(finalQueryString, partnerJourneyRequestDTO, true);
	}

	@Override
	public String getPartnerJourneyAssetCount(PartnerJourneyRequestDTO partnerJourneyRequestDTO) {
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(
				partnerJourneyRequestDTO.getLoggedInUserId(), partnerJourneyRequestDTO.isPartnerTeamMemberGroupFilter(),
				true);
		String finalQueryString = allAssetCountQuery;
		String myPartnerFilter = "";
		String partnerCompanyFilter = "";
		String partnershipStatus = partnerJourneyRequestDTO.getPartnershipStatus() != null
				? partnerJourneyRequestDTO.getPartnershipStatus()
				: "approved";
		finalQueryString = replacePartnershipStatus(finalQueryString, partnershipStatus);
		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			myPartnerFilter = " and xup1.company_id " + partnerJourneyTeamMemberFilterQuery;
		}
		String dateFilterQueryString = frameDateFilterQuery(partnerJourneyRequestDTO, "xdp.published_time");
		if (partnerJourneyRequestDTO.isDetailedAnalytics()) {
			finalQueryString = partnerJourneyAssetCountQuery;

			Integer teamMemberUserId = partnerJourneyRequestDTO.getTeamMemberUserId();
			if (teamMemberUserId != null && teamMemberUserId > 0) {
				finalQueryString += xup1UserIdTMFilter;
			}
			finalQueryString += dateFilterQueryString;

		} else {
			if (partnerJourneyRequestDTO.getSelectedPartnerCompanyIds() != null
					&& !partnerJourneyRequestDTO.getSelectedPartnerCompanyIds().isEmpty()) {
				partnerCompanyFilter = "and xup1.company_id in (:partnerCompanyIds) ";
			}
			finalQueryString = finalQueryString.replace("{partnerCompanyFilter}", partnerCompanyFilter);
			finalQueryString = finalQueryString.replace("{myPartnerFilter}", myPartnerFilter);
			if (XamplifyUtils.isValidString(myPartnerFilter)) {
				finalQueryString = replacePartnershipStatus(finalQueryString, partnershipStatus);
			}
			finalQueryString = finalQueryString.replace("{dateFilter}", dateFilterQueryString);
		}

		return (String) executePartnerJourneyUniqueResultQuery(finalQueryString, partnerJourneyRequestDTO, true);
	}

	@Override
	public PartnerJourneyAnalyticsDTO getPartnerJourneyTrackAndPlaybookCount(
			PartnerJourneyRequestDTO partnerJourneyRequestDTO) {
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(
				partnerJourneyRequestDTO.getLoggedInUserId(), partnerJourneyRequestDTO.isPartnerTeamMemberGroupFilter(),
				true);
		String finalQueryString = allTrackAndPlaybookCountQuery;
		String partnershipStatus = partnerJourneyRequestDTO.getPartnershipStatus() != null
				? partnerJourneyRequestDTO.getPartnershipStatus()
				: "approved";
		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			finalQueryString += " and xup1.company_id " + partnerJourneyTeamMemberFilterQuery;
		}
		finalQueryString = replacePartnershipStatus(finalQueryString, partnershipStatus);
		if (partnerJourneyRequestDTO.isDetailedAnalytics()) {
			finalQueryString = partnerJourneyTrackAndPlaybookCountQuery;
		} else {
			if (partnerJourneyRequestDTO.getSelectedPartnerCompanyIds() != null
					&& !partnerJourneyRequestDTO.getSelectedPartnerCompanyIds().isEmpty()) {
				finalQueryString += "and  xup1.company_id in (:partnerCompanyIds) ";
			}
		}
		Integer teamMemberUserId = partnerJourneyRequestDTO.getTeamMemberUserId();
		if (teamMemberUserId != null && teamMemberUserId > 0) {
			finalQueryString += xup1UserIdTMFilter;
		}

		String dateFilterQueryString = frameDateFilterQuery(partnerJourneyRequestDTO, TRACK_DATE_COLUMN);
		finalQueryString += dateFilterQueryString;

		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(finalQueryString);
		query.setParameter("vendorCompanyId", partnerJourneyRequestDTO.getVendorCompanyId());
		if (partnerJourneyRequestDTO.isDetailedAnalytics()) {
			query.setParameter("partnerCompanyId", partnerJourneyRequestDTO.getPartnerCompanyId());
		} else {
			if (partnerJourneyRequestDTO.getSelectedPartnerCompanyIds() != null
					&& !partnerJourneyRequestDTO.getSelectedPartnerCompanyIds().isEmpty()) {
				query.setParameterList("partnerCompanyIds", partnerJourneyRequestDTO.getSelectedPartnerCompanyIds());
			}
		}
		if (teamMemberUserId != null && teamMemberUserId > 0) {
			query.setParameter("teamMemberUserId", partnerJourneyRequestDTO.getTeamMemberUserId());
		}
		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			query.setParameter("teamMemberUserIdFilter", partnerJourneyRequestDTO.getLoggedInUserId());
		}

		return (PartnerJourneyAnalyticsDTO) paginationUtil.getDto(PartnerJourneyAnalyticsDTO.class, query);
	}

	private Object executePartnerJourneyUniqueResultQuery(String finalQueryString,
			PartnerJourneyRequestDTO partnerJourneyRequestDTO, boolean teamMemberFilter) {
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(
				partnerJourneyRequestDTO.getLoggedInUserId(), partnerJourneyRequestDTO.isPartnerTeamMemberGroupFilter(),
				true);
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(finalQueryString);
		query.setParameter("vendorCompanyId", partnerJourneyRequestDTO.getVendorCompanyId());
		if (partnerJourneyRequestDTO.isDetailedAnalytics()) {
			query.setParameter("partnerCompanyId", partnerJourneyRequestDTO.getPartnerCompanyId());
		} else {
			if (partnerJourneyRequestDTO.getSelectedPartnerCompanyIds() != null
					&& !partnerJourneyRequestDTO.getSelectedPartnerCompanyIds().isEmpty()) {
				query.setParameterList("partnerCompanyIds", partnerJourneyRequestDTO.getSelectedPartnerCompanyIds());
			}
		}
		if (teamMemberFilter && partnerJourneyRequestDTO.getTeamMemberUserId() != null
				&& partnerJourneyRequestDTO.getTeamMemberUserId() > 0) {
			query.setParameter("teamMemberUserId", partnerJourneyRequestDTO.getTeamMemberUserId());
		}
		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			query.setParameter("teamMemberUserIdFilter", partnerJourneyRequestDTO.getLoggedInUserId());
		}
		return query.uniqueResult();
	}

	@Override
	public Map<String, Object> getPartnerJourneyTrackDetailsByInteraction(Pagination pagination) {
		HashMap<String, Object> map = new HashMap<>();
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), true);
		String queryString = framePartnerJourneyTrackDetailsByInteractionQuery(pagination);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", pagination.getVendorCompanyId());
		if (pagination.isDetailedAnalytics()) {
			query.setParameter("partnerCompanyId", pagination.getPartnerCompanyId());
		} else {
			if (pagination.getSelectedPartnerCompanyIds() != null
					&& !pagination.getSelectedPartnerCompanyIds().isEmpty()) {
				query.setParameterList("partnerCompanyIds", pagination.getSelectedPartnerCompanyIds());
			}
		}
		if (pagination.getTeamMemberId() != null && pagination.getTeamMemberId() > 0) {
			query.setParameter("teamMemberUserId", pagination.getTeamMemberId());
		}
		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			query.setParameter("teamMemberUserIdFilter", pagination.getUserId());
		}

		return paginationUtil.setScrollableAndGetList(pagination, map, query, PartnerJourneyTrackDetailsDTO.class);
	}

	private String framePartnerJourneyTrackDetailsByInteractionQuery(Pagination pagination) {
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), true);
		String finalQueryString = allViewedAndNotViewedTrackDetailsQuery;
		String searchQuery = allViewedAndNotViewedTrackDetailsSearchConditions;
		String groupByQuery = allViewedAndNotViewedTrackDetailsQueryGroupBy;
		String partnershipStatus = pagination.getPartnershipStatus() != null ? pagination.getPartnershipStatus()
				: "approved";
		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			finalQueryString += " and xcp.company_id " + partnerJourneyTeamMemberFilterQuery;
		}
		finalQueryString = replacePartnershipStatus(finalQueryString, partnershipStatus);
		if (pagination.isDetailedAnalytics()) {
			finalQueryString = partnerJourneyViewedAndNotViewedTrackDetailsQuery;
			searchQuery = partnerJourneyViewedAndNotViewedTrackDetailsSearchConditions;
		} else {
			if (pagination.getSelectedPartnerCompanyIds() != null
					&& !pagination.getSelectedPartnerCompanyIds().isEmpty()) {
				finalQueryString += "and xcp.company_id in (:partnerCompanyIds) ";
			}
		}
		String searchKey = pagination.getSearchKey();
		if (StringUtils.hasText(searchKey)) {
			finalQueryString += searchQuery.replace("searchKey", searchKey);
		}
		if (pagination.getTeamMemberId() != null && pagination.getTeamMemberId() > 0) {
			finalQueryString += xup1UserIdTMFilter;
		}
		String trackTypeFilter = pagination.getTrackTypeFilter();
		if (!StringUtils.isEmpty(trackTypeFilter)) {
			if (trackTypeFilter.equals(partnerJourneyInteracted)) {
				finalQueryString += partnerJourneyTrackTypeInteractedFilter;
			} else if (trackTypeFilter.equals(partnerJourneyNotInteracted)) {
				finalQueryString += partnerJourneyTrackTypeNotInteractedFilter;
			}
		}

		String dateFilterQuery = frameDateFilterQuery(pagination, TRACK_DATE_COLUMN);
		finalQueryString += dateFilterQuery;

		if (pagination.isDetailedAnalytics()) {
			return finalQueryString;
		} else {
			return finalQueryString += groupByQuery;
		}
	}

	@Override
	public Map<String, Object> getPartnerJourneyTrackAssetDetailsByType(Pagination pagination) {
		HashMap<String, Object> map = new HashMap<>();
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), true);
		String queryString = framePartnerJourneyTrackAssetDetailsByTypeQuery(pagination);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", pagination.getVendorCompanyId());
		if (pagination.isDetailedAnalytics()) {
			query.setParameter("partnerCompanyId", pagination.getPartnerCompanyId());
		} else {
			if (pagination.getSelectedPartnerCompanyIds() != null
					&& !pagination.getSelectedPartnerCompanyIds().isEmpty()) {
				query.setParameterList("partnerCompanyIds", pagination.getSelectedPartnerCompanyIds());
			}
		}
		if (pagination.getTeamMemberId() != null && pagination.getTeamMemberId() > 0) {
			query.setParameter("teamMemberUserId", pagination.getTeamMemberId());
		}
		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			query.setParameter("teamMemberUserIdFilter", pagination.getUserId());
		}
		return paginationUtil.setScrollableAndGetList(pagination, map, query, PartnerJourneyTrackDetailsDTO.class);
	}

	private String framePartnerJourneyTrackAssetDetailsByTypeQuery(Pagination pagination) {
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), true);
		String finalQueryString = "";
		String searchKey = pagination.getSearchKey();
		finalQueryString = allTypeWiseTrackDetailsQuery;
		String partnershipStatus = pagination.getPartnershipStatus() != null ? pagination.getPartnershipStatus()
				: "approved";
		if (StringUtils.hasText(searchKey)) {
			if (searchKey.toLowerCase().equals(partnerJourneyNotOpened.toLowerCase())) {
				finalQueryString += allTypeWiseTrackDetailsNotOpenedSearchConditions.replace("searchKey", searchKey);
			} else {
				finalQueryString += allTypeWiseTrackDetailsSearchConditions.replace("searchKey", searchKey);
			}
		}
		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			finalQueryString += " and xcp.company_id " + partnerJourneyTeamMemberFilterQuery;
		}
		finalQueryString = replacePartnershipStatus(finalQueryString, partnershipStatus);
		if (pagination.isDetailedAnalytics()) {
			finalQueryString = partnerJourneyTypeWiseTrackDetailsQuery;
			if (StringUtils.hasText(searchKey)) {
				if (searchKey.toLowerCase().equals(partnerJourneyNotOpened.toLowerCase())) {
					finalQueryString += partnerJourneyTypeWiseTrackDetailsNotOpenedSearchConditions.replace("searchKey",
							searchKey);
				} else {
					finalQueryString += partnerJourneyTypeWiseTrackDetailsSearchConditions.replace("searchKey",
							searchKey);
				}
			}
		} else {
			if (pagination.getSelectedPartnerCompanyIds() != null
					&& !pagination.getSelectedPartnerCompanyIds().isEmpty()) {
				finalQueryString += "and xcp.company_id in (:partnerCompanyIds) ";
			}
		}

		if (pagination.getTeamMemberId() != null && pagination.getTeamMemberId() > 0) {
			finalQueryString += xup1UserIdTMFilter;
		}

		String dateFilterQueryString = frameDateFilterQuery(pagination, TRACK_DATE_COLUMN);
		finalQueryString += dateFilterQueryString;

		String trackTypeFilter = pagination.getTrackTypeFilter();
		if (!StringUtils.isEmpty(trackTypeFilter)) {
			if (trackTypeFilter.equals(partnerJourneyInteracted)) {
				finalQueryString += partnerJourneyTrackTypeInteractedFilter;
			} else if (trackTypeFilter.equals(partnerJourneyNotInteracted)) {
				finalQueryString += partnerJourneyTrackTypeNotInteractedFilter;
			}
		}

		String assetTypeFilter = pagination.getAssetTypeFilter();
		if (!StringUtils.isEmpty(assetTypeFilter)) {
			if (assetTypeFilter.toLowerCase().equals(partnerJourneyNotOpened.toLowerCase())) {
				finalQueryString += partnerJourneyTrackTypeNotInteractedFilter;
			} else {
				finalQueryString += partnerJourneyTypeWiseTrackDetailsAssetFilter.replace("assetTypeFilter",
						assetTypeFilter.toUpperCase());
			}
		}

		return finalQueryString;
	}

	@Override
	public Map<String, Object> getPartnerJourneyTracksByUser(Pagination pagination) {
		HashMap<String, Object> map = new HashMap<>();
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), true);
		String queryString = framePartnerJourneyTracksByUserQuery(pagination);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", pagination.getVendorCompanyId());
		if (pagination.isDetailedAnalytics()) {
			query.setParameter("partnerCompanyId", pagination.getPartnerCompanyId());
		} else {
			if (pagination.getSelectedPartnerCompanyIds() != null
					&& !pagination.getSelectedPartnerCompanyIds().isEmpty()) {
				query.setParameterList("partnerCompanyIds", pagination.getSelectedPartnerCompanyIds());
			}
		}
		if (pagination.getTeamMemberId() != null && pagination.getTeamMemberId() > 0) {
			query.setParameter("teamMemberUserId", pagination.getTeamMemberId());
		}
		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			query.setParameter("teamMemberUserIdFilter", pagination.getUserId());
		}
		return paginationUtil.setScrollableAndGetList(pagination, map, query, TeamMemberDTO.class);
	}

	private String framePartnerJourneyTracksByUserQuery(Pagination pagination) {
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), true);
		String finalQueryString = allTracksUserWiseCountsQuery;
		String searchQuery = companyNameSearchCondition;
		String groupByQuery = allTracksUserWiseGroupBy;
		String searchKey = pagination.getSearchKey();
		String partnershipStatus = pagination.getPartnershipStatus() != null ? pagination.getPartnershipStatus()
				: "approved";
		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			finalQueryString += " and xcp.company_id " + partnerJourneyTeamMemberFilterQuery;
		}
		finalQueryString = replacePartnershipStatus(finalQueryString, partnershipStatus);
		if (pagination.isDetailedAnalytics()) {
			finalQueryString = partnerJourneyTracksUserWiseCountsQuery;
			searchQuery = partnerJourneyTracksUserWiseCountsSearchConditions;
			groupByQuery = partnerJourneyTracksUserWiseGroupBy;
		} else {
			if (pagination.getSelectedPartnerCompanyIds() != null
					&& !pagination.getSelectedPartnerCompanyIds().isEmpty()) {
				finalQueryString += "and xcp.company_id in (:partnerCompanyIds) ";
			}
		}
		if (StringUtils.hasText(searchKey)) {
			finalQueryString += searchQuery.replace("searchKey", searchKey);
		}
		if (pagination.getTeamMemberId() != null && pagination.getTeamMemberId() > 0) {
			finalQueryString += xup1UserIdTMFilter;
		}

		String dateFilterQueryString = frameDateFilterQuery(pagination, TRACK_DATE_COLUMN);
		finalQueryString += dateFilterQueryString;

		finalQueryString += groupByQuery;
		return finalQueryString;
	}

	@Override
	public Map<String, Object> getPartnerJourneyTrackDetailsByUser(Pagination pagination) {
		HashMap<String, Object> map = new HashMap<>();
		String queryString = framePartnerJourneyTrackDetailsByUser(pagination);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		if (pagination.getSelectedPartnerCompanyIds() != null && !pagination.getSelectedPartnerCompanyIds().isEmpty()) {
			query.setParameterList("partnerCompanyIds", pagination.getSelectedPartnerCompanyIds());
		}
		if (pagination.getTeamMemberId() != null && pagination.getTeamMemberId() > 0) {
			query.setParameter("teamMemberUserId", pagination.getTeamMemberId());
		}
		return paginationUtil.setScrollableAndGetList(pagination, map, query, PartnerJourneyTrackDetailsDTO.class);
	}

	private String framePartnerJourneyTrackDetailsByUser(Pagination pagination) {
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), true);
		Integer companyId = userDao.getCompanyIdByUserId(pagination.getUserId());
		String xupTMFilter = "xupTMFilter";
		String xup1TMFilter = "xup1TMFilter";
		String partnerCompanyIds = "partnerCompanyIds";
		String companyIds = "companyIds";
		String searchQuery = " ";
		String dateFilter = "dateFilter";
		String searchKey = pagination.getSearchKey();
		String partnershipStatus = pagination.getPartnershipStatus() != null ? pagination.getPartnershipStatus()
				: "approved";
		if (StringUtils.hasText(searchKey)) {
			searchQuery = allTracksUserWiseContentDetailsSearchCondition;
		}
		String finalQueryString = "with a as "
				+ "(select distinct  xlt.id as \"id1\", xcp.company_id as \"companyId\" ,xcp.company_name as \"companyName\", "
				+ "xcp.company_id ,xlt.title as \"title\", xlt.published_time as \"publishedOn\", "
				+ "sum(coalesce(xltv.progress, 0))/count(distinct xltv.user_id) as \"progress\" "
				+ "from xt_learning_track xlt "
				+ "left join xt_learning_track_visibility xltv on xltv.learning_track_id=xlt.id "
				+ "left join xt_user_profile xup on xup.user_id=xltv.user_id "
				+ "left join xt_company_profile xcp on xcp.company_id=xup.company_id left join xt_partnership xp on xp.id= xltv.partnership_id "
				+ "where xlt.company_id= " + companyId + partnerCompanyIds + " "
				+ "and xlt.type='TRACK' and xlt.is_published= true and xcp.company_name_status ='active' and xp.status={status} "
				+ dateFilter
				+ getPartnerTeamMemberGroupFilterSQL("xcp.company_id", pagination.getUserId(), teamMemberFilterDTO,
						partnershipStatus)
				+ " " + searchQuery + " " + " group by 1,2,3,4,5,6), " + "b as "
				+ "(select distinct xlt.id,xup1.company_id ,count( case when xltcp.type='OPENED' then xd.id end ) as \"openedCount\", "
				+ "count( case when xltcp.type='VIEWED' then xd.id end ) as \"viewedCount\", "
				+ "count( case when xltcp.type='DOWNLOADED' then xd.id end ) as \"downloadedCount\", "
				+ "count(distinct xd.id) as \"assetCount\", "
				+ "coalesce(count(distinct xltc.quiz_id), 0) as \"quizCount\" "
				+ "from xt_learning_track_visibility xltv "
				+ "left join xt_learning_track xlt on xltv.learning_track_id=xlt.id "
				+ "left join xt_learning_track_content xltc on xltc.learning_track_id=xlt.id "
				+ "left join xt_learning_track_content_partner_activity xltcp on xltcp.learning_track_visibility_id=xltv.id and xltcp.learning_track_content_id =xltc.id "
				+ "left join xt_dam xd on xd.id = xltc.dam_id "
				+ "left join xt_user_profile xup1 on xup1.user_id=xltv.user_id left join xt_partnership xp on xp.id= xltv.partnership_id "
				+ "where xlt.company_id=" + companyId + companyIds + " "
				+ "and xlt.type='TRACK' and xlt.is_published= true and xp.status={status}  "
				+ getPartnerTeamMemberGroupFilterSQL("xup1.company_id", pagination.getUserId(), teamMemberFilterDTO,
						partnershipStatus)
				+ " group by 1,2), " + "c as "
				+ "(select distinct   a.\"scid\",a.\"qid\",a.title,a.score,a.max_score , a.\"Submitted\" from \r\n"
				+ "(select distinct xup1.company_id as \"scid\", xlt.title, xltc.quiz_id as \"quiz\", xup1.user_id,\r\n"
				+ "xlt.id  as \"qid\", xup1.email_id,row_number ()over(partition by  xup1.company_id, xlt.id order by xfs.submitted_on desc) as rn,\r\n"
				+ "xfs.score as score, xf.max_score  as max_score, (xfs.submitted_on) as \"Submitted\"\r\n"
				+ "from xt_learning_track xlt\r\n"
				+ "left join xt_learning_track_visibility xltv on xltv.learning_track_id=xlt.id\r\n"
				+ "left join xt_partnership p on xltv.partnership_id = p.id\r\n"
				+ "left join xt_learning_track_content xltc on xltc.learning_track_id=xlt.id\r\n"
				+ "left join xt_user_profile xup1 on xup1.user_id=xltv.user_id\r\n"
				+ "left join xt_form xf on  xltc.quiz_id= xf.id\r\n"
				+ "left join xt_form_submit xfs on xf.id=xfs.form_id and xfs.user_id =xup1.user_id and xfs.learning_track_id = xlt.id \r\n"
				+ "where xlt.company_id=" + companyId
				+ " and xlt.type='TRACK' and xlt.is_published= true and xltv.progress>0\r\n"
				+ "and xfs.form_submit_type='LMS_FORM' and p.status={status} " + companyIds + " \r\n"
				+ getPartnerTeamMemberGroupFilterSQL("xup1.company_id", pagination.getUserId(), teamMemberFilterDTO,
						partnershipStatus)
				+ " )a where a.rn=1) "
				+ "select distinct  a.\"companyId\",a.\"companyName\",a.\"title\",a.\"publishedOn\", "
				+ "b.\"openedCount\",b.\"viewedCount\",b.\"downloadedCount\",cast(a.\"progress\" as Integer),b.\"assetCount\",b.\"quizCount\", "
				+ "max(case when \"Submitted\" is not null then coalesce (c.score, 0) || '  out of  ' ||coalesce(c.max_score, 0) "
				+ "else coalesce (c.score, 0) || '  out of  ' ||coalesce(c.max_score, 0) end ) as \"score\" "
				+ "from a left join b on  a.\"companyId\"=b.company_id and a.\"id1\"=b.id "
				+ "left join c on  c.\"scid\"=a.\"companyId\"  and c.\"qid\"=a.\"id1\" "
				+ "group by 1,2,3,4,5,6,7,8,9,10";
		finalQueryString = replacePartnershipStatus(finalQueryString, partnershipStatus);
		if (pagination.isDetailedAnalytics()) {
			Integer partnerCompanyId = pagination.getPartnerCompanyId();
			finalQueryString = "with a as "
					+ "(select distinct xlt.id as \"id1\", xcp.company_id as \"companyId\", xcp.company_name as \"companyName\", "
					+ "xup.email_id as \"emailId\", xup.user_id as \"tuid\", xcp.company_id, "
					+ "xlt.title as \"title\", xlt.published_time as \"publishedOn\", coalesce(xltv.progress, 0) as \"progress\" "
					+ "from xt_learning_track xlt "
					+ "left join xt_learning_track_visibility xltv on xltv.learning_track_id=xlt.id "
					+ "left join xt_user_profile xup on xup.user_id=xltv.user_id "
					+ "left join xt_company_profile xcp on xcp.company_id=xup.company_id " + "where xlt.company_id="
					+ companyId + " and xcp.company_id=" + partnerCompanyId + " " + xupTMFilter + " "
					+ "and xlt.type='TRACK' and xlt.is_published= true and xcp.company_name_status ='active' "
					+ dateFilter + searchQuery + "), " + " b as "
					+ "(select distinct xlt.id, xup1.user_id as \"auid\", "
					+ "count(case when xltcp.type='OPENED' then xd.id end ) as \"openedCount\", "
					+ "count(case when xltcp.type='VIEWED' then xd.id end ) as \"viewedCount\", "
					+ "count(case when xltcp.type='DOWNLOADED' then xd.id end ) as \"downloadedCount\", "
					+ "count(distinct xd.id) as \"assetCount\", "
					+ "coalesce(count(distinct xltc.quiz_id), 0) as \"quizCount\" "
					+ "from xt_learning_track_visibility xltv "
					+ "left join xt_learning_track xlt on xltv.learning_track_id=xlt.id "
					+ "left join xt_learning_track_content xltc on xltc.learning_track_id=xlt.id "
					+ "left join xt_learning_track_content_partner_activity xltcp on xltcp.learning_track_visibility_id=xltv.id and xltcp.learning_track_content_id =xltc.id "
					+ "left join xt_dam xd on xd.id = xltc.dam_id "
					+ "left join xt_user_profile xup1 on xup1.user_id=xltv.user_id " + "where xlt.company_id="
					+ companyId + " and xup1.company_id= " + partnerCompanyId + " " + xup1TMFilter + " "
					+ "and xlt.type='TRACK' and xlt.is_published= true " + "group by 1,2), " + "c as "
					+ "(select distinct xlt.id as \"qid\", xup1.user_id, "
					+ "xfs.score as score, xf.max_score as max_score, xfs.submitted_on as \"Submitted\" "
					+ "from xt_learning_track_visibility xltv "
					+ "left join xt_learning_track xlt on xltv.learning_track_id=xlt.id "
					+ "left join xt_learning_track_content xltc on xltc.learning_track_id=xlt.id "
					+ "left join xt_user_profile xup1 on xup1.user_id=xltv.user_id "
					+ "left join xt_form xf on xltc.quiz_id= xf.id "
					+ "left join xt_form_submit xfs on xf.id=xfs.form_id and xfs.user_id =xup1.user_id "
					+ "where xlt.company_id=" + companyId + " and xlt.type='TRACK' and xlt.is_published= true "
					+ "and xfs.form_submit_type='LMS_FORM' and xup1.company_id=" + partnerCompanyId + " " + xup1TMFilter
					+ " " + "group by 1,2,3,4,5) "
					+ "select distinct a.\"companyId\", a.\"companyName\", a.\"emailId\", a.\"title\", a.\"publishedOn\", "
					+ "b.\"openedCount\", b.\"viewedCount\", b.\"downloadedCount\", a.\"progress\", b.\"assetCount\", b.\"quizCount\", "
					+ "max(case when \"Submitted\" is not null then coalesce(c.score, 0) || '  out of  ' || coalesce(c.max_score, 0) "
					+ "else coalesce(c.score, 0) || '  out of  ' || coalesce(c.max_score, 0) end) as \"score\" "
					+ "from a left join b on a.\"id1\"=b.id and a.\"tuid\"=b.\"auid\" "
					+ "left join c on c.\"qid\"=a.\"id1\" and a.\"tuid\"=c.user_id "
					+ "group by 1,2,3,4,5,6,7,8,9,10,11";
		} else {
			if (!pagination.isDetailedAnalytics() && pagination.getSelectedPartnerCompanyIds() != null
					&& !pagination.getSelectedPartnerCompanyIds().isEmpty()) {
				finalQueryString = finalQueryString.replace(partnerCompanyIds,
						" and xcp.company_id in (:partnerCompanyIds) ");
				finalQueryString = finalQueryString.replace(companyIds,
						" and xup1.company_id in (:partnerCompanyIds) ");
			} else {
				finalQueryString = finalQueryString.replace(partnerCompanyIds, " ");
				finalQueryString = finalQueryString.replace(companyIds, " ");
			}
		}

		if (StringUtils.hasText(searchKey)) {
			finalQueryString = finalQueryString.replace("searchKey", searchKey);
		}
		if (pagination.getTeamMemberId() != null && pagination.getTeamMemberId() > 0) {
			finalQueryString = finalQueryString.replace(xupTMFilter, " and xup.user_id= :teamMemberUserId ");
			finalQueryString = finalQueryString.replace(xup1TMFilter, " and xup1.user_id= :teamMemberUserId ");
		} else {
			finalQueryString = finalQueryString.replace(xupTMFilter, " ");
			finalQueryString = finalQueryString.replace(xup1TMFilter, " ");
		}

		String dateFilterQueryString = frameDateFilterQuery(pagination, TRACK_DATE_COLUMN);
		finalQueryString = finalQueryString.replace(dateFilter, dateFilterQueryString);

		return finalQueryString;
	}

	@Override
	public Map<String, Object> getPartnerJourneyTrackAssetDetails(Pagination pagination) {
		HashMap<String, Object> map = new HashMap<>();
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), true);
		String queryString = framePartnerJourneyTrackAssetDetails(pagination);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", pagination.getVendorCompanyId());
		if (pagination.isDetailedAnalytics()) {
			query.setParameter("partnerCompanyId", pagination.getPartnerCompanyId());
		} else {
			if (pagination.getSelectedPartnerCompanyIds() != null
					&& !pagination.getSelectedPartnerCompanyIds().isEmpty()) {
				query.setParameterList("partnerCompanyIds", pagination.getSelectedPartnerCompanyIds());
			}
		}
		if (pagination.getTeamMemberId() != null && pagination.getTeamMemberId() > 0) {
			query.setParameter("teamMemberUserId", pagination.getTeamMemberId());
		}
		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			query.setParameter("teamMemberUserIdFilter", pagination.getUserId());
		}
		return paginationUtil.setScrollableAndGetList(pagination, map, query, PartnerJourneyTrackDetailsDTO.class);
	}

	private String framePartnerJourneyTrackAssetDetails(Pagination pagination) {
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), true);
		String finalQueryString = allTrackAssetsQuery;
		String groupByQuery = allTrackAssetsGroupBy;
		String partnershipStatus = pagination.getPartnershipStatus() != null ? pagination.getPartnershipStatus()
				: "approved";

		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			finalQueryString += " and xup.company_id " + partnerJourneyTeamMemberFilterQuery;
		}
		finalQueryString = replacePartnershipStatus(finalQueryString, partnershipStatus);
		if (pagination.isDetailedAnalytics()) {
			finalQueryString = partnerJourneyTrackAssetsQuery;
			groupByQuery = partnerJourneyTrackAssetsGroupBy;
		} else {
			if (pagination.getSelectedPartnerCompanyIds() != null
					&& !pagination.getSelectedPartnerCompanyIds().isEmpty()) {
				finalQueryString += "and xup.company_id in (:partnerCompanyIds) ";
			}
		}
		String searchKey = pagination.getSearchKey();
		if (StringUtils.hasText(searchKey)) {
			finalQueryString += partnerJourneyTrackAssetsSearchCondition.replace("searchKey", searchKey);
		}
		if (pagination.getTeamMemberId() != null && pagination.getTeamMemberId() > 0) {
			finalQueryString += xupUserIdTMFilter;
		}

		String dateFilterQueryString = frameDateFilterQuery(pagination, TRACK_DATE_COLUMN);
		finalQueryString += dateFilterQueryString;

		finalQueryString += groupByQuery;
		return finalQueryString;
	}

	@Override
	public Map<String, Object> getPartnerJourneyPlaybookAssetDetails(Pagination pagination) {
		HashMap<String, Object> map = new HashMap<>();
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), true);
		String queryString = framePartnerJourneyPlaybookAssetDetails(pagination);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", pagination.getVendorCompanyId());
		if (pagination.isDetailedAnalytics()) {
			query.setParameter("partnerCompanyId", pagination.getPartnerCompanyId());
		} else {
			if (pagination.getSelectedPartnerCompanyIds() != null
					&& !pagination.getSelectedPartnerCompanyIds().isEmpty()) {
				query.setParameterList("partnerCompanyIds", pagination.getSelectedPartnerCompanyIds());
			}
		}
		if (pagination.getTeamMemberId() != null && pagination.getTeamMemberId() > 0) {
			query.setParameter("teamMemberUserId", pagination.getTeamMemberId());
		}
		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			query.setParameter("teamMemberUserIdFilter", pagination.getUserId());
		}
		return paginationUtil.setScrollableAndGetList(pagination, map, query, PartnerJourneyTrackDetailsDTO.class);
	}

	private String framePartnerJourneyPlaybookAssetDetails(Pagination pagination) {
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), true);
		String finalQueryString = allPlaybookAssetsQuery;
		String groupByQuery = allPlaybookAssetsGroupBy;
		String partnershipStatus = pagination.getPartnershipStatus() != null ? pagination.getPartnershipStatus()
				: "approved";
		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			finalQueryString += " and xup1.company_id " + partnerJourneyTeamMemberFilterQuery;
		}
		finalQueryString = replacePartnershipStatus(finalQueryString, partnershipStatus);
		if (pagination.isDetailedAnalytics()) {
			finalQueryString = partnerJourneyPlaybookAssetsQuery;
			groupByQuery = partnerJourneyPlaybookAssetsGroupBy;
		} else {
			if (pagination.getSelectedPartnerCompanyIds() != null
					&& !pagination.getSelectedPartnerCompanyIds().isEmpty()) {
				finalQueryString += "and xup1.company_id in (:partnerCompanyIds) ";
			}
		}
		String searchKey = pagination.getSearchKey();
		if (StringUtils.hasText(searchKey)) {
			finalQueryString += partnerJourneyPlaybookAssetsSearchCondition.replace("searchKey", searchKey);
		}
		if (pagination.getTeamMemberId() != null && pagination.getTeamMemberId() > 0) {
			finalQueryString += xup1UserIdTMFilter;
		}

		String dateFilerQueryString = frameDateFilterQuery(pagination, TRACK_DATE_COLUMN);
		finalQueryString += dateFilerQueryString;

		finalQueryString += groupByQuery;
		return finalQueryString;
	}

	@Override
	public Map<String, Object> getPartnerJourneyShareLeadDetails(Pagination pagination) {
		HashMap<String, Object> map = new HashMap<>();
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), true);
		String queryString = framePartnerJourneyShareLeadDetails(pagination);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", pagination.getVendorCompanyId());
		if (pagination.isDetailedAnalytics()) {
			query.setParameter("partnerCompanyId", pagination.getPartnerCompanyId());
		} else {
			if (pagination.getSelectedPartnerCompanyIds() != null
					&& !pagination.getSelectedPartnerCompanyIds().isEmpty()) {
				query.setParameterList("partnerCompanyIds", pagination.getSelectedPartnerCompanyIds());
			}
		}
		if (pagination.getTeamMemberId() != null && pagination.getTeamMemberId() > 0) {
			query.setParameter("teamMemberUserId", pagination.getTeamMemberId());
		}
		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			query.setParameter("teamMemberUserIdFilter", pagination.getUserId());
		}
		return paginationUtil.setScrollableAndGetList(pagination, map, query, ShareLeadsDTO.class);
	}

	private String framePartnerJourneyShareLeadDetails(Pagination pagination) {
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), true);
		String finalQueryString = allShareLeadDetailsQuery;
		String groupByQuery = allShareLeadDetailsGroupBy;
		String searchQuery = allShareLeadDetailsSearchConditions;
		String searchKey = pagination.getSearchKey();
		String partnershipStatus = pagination.getPartnershipStatus() != null ? pagination.getPartnershipStatus()
				: "approved";
		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			finalQueryString += " and xcp.company_id " + partnerJourneyTeamMemberFilterQuery;
		}
		finalQueryString = replacePartnershipStatus(finalQueryString, partnershipStatus);
		if (pagination.isDetailedAnalytics()) {
			finalQueryString = partnerJourneyShareLeadDetailsQuery;
			groupByQuery = partnerJourneyShareLeadDetailsGroupBy;
			searchQuery = partnerJourneyShareLeadDetailsSearchConditions;
		} else {
			if (pagination.getSelectedPartnerCompanyIds() != null
					&& !pagination.getSelectedPartnerCompanyIds().isEmpty()) {
				finalQueryString += "and xcp.company_id in (:partnerCompanyIds) ";
			}
		}

		String dateFilterQueryString = frameDateFilterQuery(pagination, "sp.created_time");
		finalQueryString += dateFilterQueryString;

		if (StringUtils.hasText(searchKey)) {
			finalQueryString += searchQuery.replace("searchKey", searchKey);
		}
		if (pagination.getTeamMemberId() != null && pagination.getTeamMemberId() > 0) {
			finalQueryString += xupUserIdTMFilter;
		}
		finalQueryString += groupByQuery;
		return finalQueryString;
	}

	@Override
	public Map<String, Object> getPartnerJourneyRedistributedCampaignDetails(Pagination pagination) {
		return null;
	}

	@Override
	public Map<String, Object> getPartnerJourneyLeadDetails(Pagination pagination) {
		HashMap<String, Object> map = new HashMap<>();
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), true);
		String queryString = framePartnerJourneyLeadDetails(pagination);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", pagination.getVendorCompanyId());
		if (pagination.isDetailedAnalytics()) {
			query.setParameter("partnerCompanyId", pagination.getPartnerCompanyId());
		} else {
			if (pagination.getSelectedPartnerCompanyIds() != null
					&& !pagination.getSelectedPartnerCompanyIds().isEmpty()) {
				query.setParameterList("partnerCompanyIds", pagination.getSelectedPartnerCompanyIds());
			}
		}
		if (pagination.getTeamMemberId() != null && pagination.getTeamMemberId() > 0) {
			query.setParameter("teamMemberUserId", pagination.getTeamMemberId());
		}
		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			query.setParameter("teamMemberUserIdFilter", pagination.getUserId());
		}
		return paginationUtil.setScrollableAndGetList(pagination, map, query, LeadDto.class);
	}

	private String framePartnerJourneyLeadDetails(Pagination pagination) {
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), true);
		String finalQueryString = allLeadDetailsQuery;
		String searchQuery = allLeadDetailsSearchConditions;
		String partnershipStatus = pagination.getPartnershipStatus() != null ? pagination.getPartnershipStatus()
				: "approved";
		String searchKey = pagination.getSearchKey();
		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			finalQueryString += " and xl.created_by_company_id " + partnerJourneyTeamMemberFilterQuery;
		}
		finalQueryString = replacePartnershipStatus(finalQueryString, partnershipStatus);
		if (pagination.isDetailedAnalytics()) {
			finalQueryString = partnerJourneyLeadDetailsQuery;
			searchQuery = partnerJourneyLeadDetailsSearchConditions;
		} else {
			if (pagination.getSelectedPartnerCompanyIds() != null
					&& !pagination.getSelectedPartnerCompanyIds().isEmpty()) {
				finalQueryString += "and xl.created_by_company_id in (:partnerCompanyIds) ";
			}
		}

		String dateFilterQueryString = frameDateFilterQuery(pagination, LEAD_DATE_COLUMN);
		finalQueryString += dateFilterQueryString;

		if (StringUtils.hasText(searchKey)) {
			finalQueryString += searchQuery.replace("searchKey", searchKey);
		}
		if (pagination.getTeamMemberId() != null && pagination.getTeamMemberId() > 0) {
			finalQueryString += xup1UserIdTMFilter;
		}
		return finalQueryString;
	}

	@Override
	public Map<String, Object> getPartnerJourneyDealDetails(Pagination pagination) {
		HashMap<String, Object> map = new HashMap<>();
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), true);
		String queryString = framePartnerJourneyDealDetails(pagination);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", pagination.getVendorCompanyId());
		if (pagination.isDetailedAnalytics()) {
			query.setParameter("partnerCompanyId", pagination.getPartnerCompanyId());
		} else {
			if (pagination.getSelectedPartnerCompanyIds() != null
					&& !pagination.getSelectedPartnerCompanyIds().isEmpty()) {
				query.setParameterList("partnerCompanyIds", pagination.getSelectedPartnerCompanyIds());
			}
		}
		if (pagination.getTeamMemberId() != null && pagination.getTeamMemberId() > 0) {
			query.setParameter("teamMemberUserId", pagination.getTeamMemberId());
		}
		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			query.setParameter("teamMemberUserIdFilter", pagination.getUserId());
		}
		return paginationUtil.setScrollableAndGetList(pagination, map, query, DealDto.class);
	}

	private String framePartnerJourneyDealDetails(Pagination pagination) {
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), true);
		String finalQueryString = allDealDetailsQuery;
		String searchQuery = allDealDetailsSearchConditions;
		String partnershipStatus = pagination.getPartnershipStatus() != null ? pagination.getPartnershipStatus()
				: "approved";
		String searchKey = pagination.getSearchKey();
		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			finalQueryString += " and xd.created_by_company_id " + partnerJourneyTeamMemberFilterQuery;
		}
		finalQueryString = replacePartnershipStatus(finalQueryString, partnershipStatus);
		if (pagination.isDetailedAnalytics()) {
			finalQueryString = partnerJourneyDealDetailsQuery;
			searchQuery = partnerJourneyDealDetailsSearchConditions;
		} else {
			if (pagination.getSelectedPartnerCompanyIds() != null
					&& !pagination.getSelectedPartnerCompanyIds().isEmpty()) {
				finalQueryString += "and xd.created_by_company_id in (:partnerCompanyIds) ";
			}
		}

		String dateFilterQueryString = frameDateFilterQuery(pagination, DEAL_DATE_COLUMN);
		finalQueryString += dateFilterQueryString;

		if (StringUtils.hasText(searchKey)) {
			finalQueryString += searchQuery.replace("searchKey", searchKey);
		}
		if (pagination.getTeamMemberId() != null && pagination.getTeamMemberId() > 0) {
			finalQueryString += xup1UserIdTMFilter;
		}
		return finalQueryString;
	}

	@Override
	public Map<String, Object> getPartnerJourneyContactDetails(Pagination pagination) {
		HashMap<String, Object> map = new HashMap<>();
		String queryString = framePartnerJourneyContactDetails(pagination);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("partnerCompanyId", pagination.getPartnerCompanyId());
		if (pagination.getTeamMemberId() != null && pagination.getTeamMemberId() > 0) {
			query.setParameter("teamMemberUserId", pagination.getTeamMemberId());
		}
		return paginationUtil.setScrollableAndGetList(pagination, map, query, UserDTO.class);
	}

	private String framePartnerJourneyContactDetails(Pagination pagination) {
		String finalQueryString = partnerJourneycontactDetailsQuery;
		String searchKey = pagination.getSearchKey();
		if (StringUtils.hasText(searchKey)) {
			finalQueryString += partnerJourneyContactDetailsSearchConditions.replace("searchKey", searchKey);
		}
		if (pagination.getTeamMemberId() != null && pagination.getTeamMemberId() > 0) {
			finalQueryString += "and tm.team_member_id = :teamMemberUserId";
		}
		finalQueryString += partnerJourneycontactDetailsGroupBy;
		return finalQueryString;
	}

	@Override
	public Map<String, Object> getPartnerJourneyMdfDetails(Pagination pagination) {
		HashMap<String, Object> map = new HashMap<>();
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), true);
		String queryString = framePartnerJourneyMdfDetails(pagination);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", pagination.getVendorCompanyId());
		if (pagination.getSelectedPartnerCompanyIds() != null && !pagination.getSelectedPartnerCompanyIds().isEmpty()) {
			query.setParameterList("partnerCompanyIds", pagination.getSelectedPartnerCompanyIds());
		}
//		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
//			query.setParameter("teamMemberUserIdFilter", pagination.getUserId());
//		}
		return paginationUtil.setScrollableAndGetList(pagination, map, query, MdfAmountTilesDTO.class);
	}

	private String framePartnerJourneyMdfDetails(Pagination pagination) {
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), true);
		String partnershipStatus = pagination.getPartnershipStatus() != null ? pagination.getPartnershipStatus()
				: "approved";
//		String finalQueryString = partnerJourneyMdfDetailsQuery;
		String finalQueryString = "with a as(select distinct xp.vendor_company_id, xp.partner_company_id , xp.partner_id , "
				+ "sum(case when xmdh.mdf_amount_type='FUND_ADDED' then xmdh.mdf_amount else 0 end) - "
				+ "sum(case when xmdh.mdf_amount_type='FUND_REMOVED' then xmdh.mdf_amount else 0 end) as mdf_amount,xmd.created_time as \"createdtime\""
				+ "from xt_partnership xp " + "left join xt_mdf_details xmd on xmd.partnership_id = xp.id "
				+ "left join xt_mdf_details_history xmdh on xmdh.mdf_details_id = xmd.id "
				+ "where xp.status = {status} "
				+ getPartnerTeamMemberGroupFilterSQL("xp.partner_company_id", pagination.getUserId(),
						teamMemberFilterDTO, partnershipStatus)
				+ " " + " group by 1,2,3,5), "
				+ "b as (select distinct xp.vendor_company_id,xuu.contact_company as \"companyName\", xp.partner_id, "
				+ "coalesce(sum(xmrh.reimburse_amount), 0) reimburse_amount, "
				+ "coalesce(sum(xmrh.allocation_amount),0) allocation_amount " + "from xt_partnership xp "
				+ "left join xt_mdf_request xmr on xp.id = xmr.partnership_id "
				+ "left join xt_mdf_request_history xmrh on xmrh.request_id = xmr.id "
				+ "left join xt_user_profile xup on xup.user_id = xp.partner_id "
				+ "left join xt_user_userlist xuu on xup.user_id = xuu.user_id "
				+ "left join xt_user_list xul on xul.user_list_id = xuu.user_list_id and xul.company_id = xp.vendor_company_id "
				+ "where xul.is_partner_userlist = true and xul.is_default_partnerlist = true "
				+ getPartnerTeamMemberGroupFilterSQL("xp.partner_company_id", pagination.getUserId(),
						teamMemberFilterDTO, partnershipStatus)
				+ " group by 1,2,3) "
				+ "select distinct b.\"companyName\", a.partner_company_id as \"companyId\",  coalesce(a.mdf_amount,0) as \"totalBalance\", "
				+ "coalesce(b.reimburse_amount,0) as \"usedBalance\", "
				+ "(coalesce(a.mdf_amount,0)-coalesce(b.allocation_amount,0)) + "
				+ "(coalesce(allocation_amount,0)-coalesce(b.reimburse_amount,0)) as \"availableBalance\" "
				+ "from a left join b on a.vendor_company_id= b.vendor_company_id and a.partner_id = b.partner_id "
				+ "where a.vendor_company_id=:vendorCompanyId  and a.partner_company_id is not null and b.\"companyName\" is not null ";

		String dateFilterQueryString = frameDateFilterQuery(pagination, "a.createdtime");
		finalQueryString = replacePartnershipStatus(finalQueryString, partnershipStatus);
		finalQueryString += dateFilterQueryString;

		String searchKey = pagination.getSearchKey();
		if (pagination.getSelectedPartnerCompanyIds() != null && !pagination.getSelectedPartnerCompanyIds().isEmpty()) {
			finalQueryString += " and a.partner_company_id in (:partnerCompanyIds) ";
		}
		if (StringUtils.hasText(searchKey)) {
			finalQueryString += partnerJourneyMdfDetailsSearchConditions.replace("searchKey", searchKey);
		}
		return finalQueryString;
	}

	@Override
	public List<Object[]> getPartnerLeadToDealCounts(PartnerJourneyRequestDTO partnerJourneyRequestDTO) {
		String finalQueryString = partnerJourneyLeadToDealConversionQuery;
		if (partnerJourneyRequestDTO.getTeamMemberUserId() != null
				&& partnerJourneyRequestDTO.getTeamMemberUserId() > 0) {
			finalQueryString += xup1UserIdTMFilter;
		}

		String dateFilterQueryString = frameDateFilterQuery(partnerJourneyRequestDTO, LEAD_DATE_COLUMN);
		finalQueryString += dateFilterQueryString;

		finalQueryString += partnerJourneyLeadToDealConversionGroupBy;
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(finalQueryString);
		query.setParameter("vendorCompanyId", partnerJourneyRequestDTO.getVendorCompanyId());
		query.setParameter("partnerCompanyId", partnerJourneyRequestDTO.getPartnerCompanyId());
		if (partnerJourneyRequestDTO.getTeamMemberUserId() != null
				&& partnerJourneyRequestDTO.getTeamMemberUserId() > 0) {
			query.setParameter("teamMemberUserId", partnerJourneyRequestDTO.getTeamMemberUserId());
		}
		return query.list();
	}

	@Override
	public List<Object[]> getPartnerCampaignToLeadCounts(PartnerJourneyRequestDTO partnerJourneyRequestDTO) {
		String finalQueryString = partnerJourneyCampaignToLeadConversionQuery;
		if (partnerJourneyRequestDTO.getTeamMemberUserId() != null
				&& partnerJourneyRequestDTO.getTeamMemberUserId() > 0) {
			finalQueryString += xup1UserIdTMFilter;
		}

		String dateFilterQueryString = frameDateFilterQuery(partnerJourneyRequestDTO, "p.launch_time");
		finalQueryString += dateFilterQueryString;

		finalQueryString += partnerJourneyCampaignToLeadConversionGroupBy;
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(finalQueryString);
		query.setParameter("vendorCompanyId", partnerJourneyRequestDTO.getVendorCompanyId());
		query.setParameter("partnerCompanyId", partnerJourneyRequestDTO.getPartnerCompanyId());
		if (partnerJourneyRequestDTO.getTeamMemberUserId() != null
				&& partnerJourneyRequestDTO.getTeamMemberUserId() > 0) {
			query.setParameter("teamMemberUserId", partnerJourneyRequestDTO.getTeamMemberUserId());
		}
		return query.list();
	}

	@Override
	public PartnerJourneyTrackDetailsDTO getPartnerJourneyTrackCountsByInteraction(
			PartnerJourneyRequestDTO partnerJourneyRequestDTO) {
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(
				partnerJourneyRequestDTO.getLoggedInUserId(), partnerJourneyRequestDTO.isPartnerTeamMemberGroupFilter(),
				true);
		Session session = sessionFactory.getCurrentSession();
		String finalQueryString = allViewedAndNotViewedTrackCountsQuery;
		String partnershipStatus = partnerJourneyRequestDTO.getPartnershipStatus() != null
				? partnerJourneyRequestDTO.getPartnershipStatus()
				: "approved";
		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			finalQueryString += " and  xup1.company_id " + partnerJourneyTeamMemberFilterQuery;
		}
		finalQueryString = replacePartnershipStatus(finalQueryString, partnershipStatus);

		String dateFilterQuery = frameDateFilterQuery(partnerJourneyRequestDTO, TRACK_DATE_COLUMN);

		if (partnerJourneyRequestDTO.isDetailedAnalytics()) {
			finalQueryString = partnerJourneyViewedAndNotViewedTrackCountsQuery;
			Integer teamMemberUserId = partnerJourneyRequestDTO.getTeamMemberUserId();
			if (teamMemberUserId != null && teamMemberUserId > 0) {
				finalQueryString += xup1UserIdTMFilter;
			}
			finalQueryString += dateFilterQuery;
			finalQueryString += partnerJourneyViewedAndNotViewedTrackCountsGroupBy;
		} else {
			if (partnerJourneyRequestDTO.getSelectedPartnerCompanyIds() != null
					&& !partnerJourneyRequestDTO.getSelectedPartnerCompanyIds().isEmpty()) {
				finalQueryString += "and xup1.company_id in (:partnerCompanyIds) ";
			}
			finalQueryString += dateFilterQuery;
			finalQueryString += allViewedAndNotViewedTrackCountsQueryGroupBy;
		}

		Query query = session.createSQLQuery(finalQueryString);
		query.setParameter("vendorCompanyId", partnerJourneyRequestDTO.getVendorCompanyId());
		if (partnerJourneyRequestDTO.isDetailedAnalytics()) {
			query.setParameter("partnerCompanyId", partnerJourneyRequestDTO.getPartnerCompanyId());
		} else {
			if (partnerJourneyRequestDTO.getSelectedPartnerCompanyIds() != null
					&& !partnerJourneyRequestDTO.getSelectedPartnerCompanyIds().isEmpty()) {
				query.setParameterList("partnerCompanyIds", partnerJourneyRequestDTO.getSelectedPartnerCompanyIds());
			}
		}
		if (partnerJourneyRequestDTO.getTeamMemberUserId() != null
				&& partnerJourneyRequestDTO.getTeamMemberUserId() > 0) {
			query.setParameter("teamMemberUserId", partnerJourneyRequestDTO.getTeamMemberUserId());
		}
		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			query.setParameter("teamMemberUserIdFilter", partnerJourneyRequestDTO.getLoggedInUserId());
		}
		return (PartnerJourneyTrackDetailsDTO) paginationUtil.getDto(PartnerJourneyTrackDetailsDTO.class, query);
	}

	@Override
	public PartnerJourneyTrackDetailsDTO getPartnerJourneyTrackCountsByType(
			PartnerJourneyRequestDTO partnerJourneyRequestDTO) {
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(
				partnerJourneyRequestDTO.getLoggedInUserId(), partnerJourneyRequestDTO.isPartnerTeamMemberGroupFilter(),
				true);
		String finalQueryString = allTypeWiseTrackCountsQuery;
		String partnershipStatus = partnerJourneyRequestDTO.getPartnershipStatus() != null
				? partnerJourneyRequestDTO.getPartnershipStatus()
				: "approved";
		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			finalQueryString += " and  xup1.company_id " + partnerJourneyTeamMemberFilterQuery;
		}
		finalQueryString = replacePartnershipStatus(finalQueryString, partnershipStatus);
		if (partnerJourneyRequestDTO.isDetailedAnalytics()) {
			finalQueryString = partnerJourneyTypeWiseTrackCountsQuery;
		} else {
			if (partnerJourneyRequestDTO.getSelectedPartnerCompanyIds() != null
					&& !partnerJourneyRequestDTO.getSelectedPartnerCompanyIds().isEmpty()) {
				finalQueryString += "and xup1.company_id in (:partnerCompanyIds) ";
			}
		}
		Integer teamMemberUserId = partnerJourneyRequestDTO.getTeamMemberUserId();
		if (teamMemberUserId != null && teamMemberUserId > 0) {
			finalQueryString += xup1UserIdTMFilter;
		}

		String dateFilterQueryString = frameDateFilterQuery(partnerJourneyRequestDTO, TRACK_DATE_COLUMN);
		finalQueryString += dateFilterQueryString;

		String trackTypeFilter = partnerJourneyRequestDTO.getTrackTypeFilter();
		if (!StringUtils.isEmpty(trackTypeFilter)) {
			if (trackTypeFilter.equals(partnerJourneyInteracted)) {
				finalQueryString += partnerJourneyTypeWiseTrackInteractedFilter;
			} else if (trackTypeFilter.equals(partnerJourneyNotInteracted)) {
				finalQueryString += partnerJourneyTypeWiseTrackNotInteractedFilter;
			}
		}
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(finalQueryString);
		query.setParameter("vendorCompanyId", partnerJourneyRequestDTO.getVendorCompanyId());
		if (partnerJourneyRequestDTO.isDetailedAnalytics()) {
			query.setParameter("partnerCompanyId", partnerJourneyRequestDTO.getPartnerCompanyId());
		} else {
			if (partnerJourneyRequestDTO.getSelectedPartnerCompanyIds() != null
					&& !partnerJourneyRequestDTO.getSelectedPartnerCompanyIds().isEmpty()) {
				query.setParameterList("partnerCompanyIds", partnerJourneyRequestDTO.getSelectedPartnerCompanyIds());
			}
		}
		if (partnerJourneyRequestDTO.getTeamMemberUserId() != null
				&& partnerJourneyRequestDTO.getTeamMemberUserId() > 0) {
			query.setParameter("teamMemberUserId", partnerJourneyRequestDTO.getTeamMemberUserId());
		}
		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			query.setParameter("teamMemberUserIdFilter", partnerJourneyRequestDTO.getLoggedInUserId());
		}
		return (PartnerJourneyTrackDetailsDTO) paginationUtil.getDto(PartnerJourneyTrackDetailsDTO.class, query);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TeamMemberDTO> getPartnerJourneyTeamEmails(PartnerJourneyRequestDTO partnerJourneyRequestDTO) {
		Session session = sessionFactory.getCurrentSession();
		String finalQueryString = partnerJourneyTeamQuery;
		String dateFilterQueryString = frameDateFilterQuery(partnerJourneyRequestDTO, "xup.datelastlogin");
		finalQueryString += dateFilterQueryString;
		Query query = session.createSQLQuery(finalQueryString);
		query.setParameter("partnerCompanyId", partnerJourneyRequestDTO.getPartnerCompanyId());
		return (List<TeamMemberDTO>) paginationUtil.getListDTO(TeamMemberDTO.class, query);
	}

	@Override
	public Map<String, Object> getPartnerJourneyPlaybooksByUser(Pagination pagination) {
		HashMap<String, Object> map = new HashMap<>();
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), true);
		String queryString = framePartnerJourneyPlaybooksByUserQuery(pagination);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", pagination.getVendorCompanyId());
//		if (pagination.getPartnerCompanyId() != null && pagination.getPartnerCompanyId() > 0) {
//			query.setParameter("partnerCompanyId", pagination.getPartnerCompanyId());
//		}
		if (pagination.isDetailedAnalytics()) {
			query.setParameter("partnerCompanyId", pagination.getPartnerCompanyId());
		} else {
			if (pagination.getSelectedPartnerCompanyIds() != null
					&& !pagination.getSelectedPartnerCompanyIds().isEmpty()) {
				query.setParameterList("partnerCompanyIds", pagination.getSelectedPartnerCompanyIds());
			}
		}

		if (pagination.getTeamMemberId() != null && pagination.getTeamMemberId() > 0) {
			query.setParameter("teamMemberUserId", pagination.getTeamMemberId());
		}

		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			query.setParameter("teamMemberUserIdFilter", pagination.getUserId());
		}
		return paginationUtil.setScrollableAndGetList(pagination, map, query, TeamMemberDTO.class);
	}

	private String framePartnerJourneyPlaybooksByUserQuery(Pagination pagination) {
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), true);
		String finalQueryString = allPlaybooksUserWiseCountsQuery;
		String searchQuery = companyNameSearchCondition;
		String groupByQuery = allPlaybooksUserWiseGroupBy;
		String searchKey = pagination.getSearchKey();
		String partnershipStatus = pagination.getPartnershipStatus() != null ? pagination.getPartnershipStatus()
				: "approved";
		/*
		 * if (pagination.getPartnerCompanyId() != null &&
		 * pagination.getPartnerCompanyId() > 0) { finalQueryString =
		 * partnerJourneyTracksUserWiseCountsQuery; searchQuery =
		 * partnerJourneyTracksUserWiseCountsSearchConditions; groupByQuery =
		 * partnerJourneyTracksUserWiseGroupBy; }
		 */
		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			finalQueryString += " and xcp.company_id " + partnerJourneyTeamMemberFilterQuery;
		}
		finalQueryString = replacePartnershipStatus(finalQueryString, partnershipStatus);
		if (pagination.isDetailedAnalytics()) {
			finalQueryString = partnerJourneyPlayBookUserWiseCountsQuery;
			searchQuery = partnerJourneyTracksUserWiseCountsSearchConditions;
			groupByQuery = partnerJourneyPlayBookUserWiseGroupBy;
		} else {
			if (pagination.getSelectedPartnerCompanyIds() != null
					&& !pagination.getSelectedPartnerCompanyIds().isEmpty()) {
				finalQueryString += "and xcp.company_id in (:partnerCompanyIds) ";
			}
		}
		if (StringUtils.hasText(searchKey)) {
			finalQueryString += searchQuery.replace("searchKey", searchKey);
		}

		if (pagination.getTeamMemberId() != null && pagination.getTeamMemberId() > 0) {
			finalQueryString += xup1UserIdTMFilter;
		}

		String dateFilterQueryString = frameDateFilterQuery(pagination, TRACK_DATE_COLUMN);
		finalQueryString += dateFilterQueryString;

		finalQueryString += groupByQuery;
		return finalQueryString;
	}

	public List<PartnerDTO> getPartnerJourneyCompanyInfoForFilter(Pagination pagination) {
		List<Object[]> folders = getPartnerJourneyCompanyInfoForFilterById(pagination);
		List<PartnerDTO> partnerDtos = new ArrayList<>();
		for (Object[] folder : folders) {
			Integer companyId = (Integer) folder[0];
			String companyName = (String) folder[1];
			PartnerDTO partnerDTO = new PartnerDTO();
			partnerDTO.setCompanyId(companyId);
			partnerDTO.setCompanyName(companyName);
			partnerDtos.add(partnerDTO);
		}
		return partnerDtos;
	}

	@Override
	public List<PartnerDTO> getAllPartnersRegionNamesForFilter(Pagination pagination) {

		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), true);

		String queryString = "";

		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			queryString = allRegionNamesByTeamMemberLogin;
		} else {
			queryString = allRegionNames;
		}

		Integer companyId = userDao.getCompanyIdByUserId(pagination.getUserId());

		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("companyId", companyId);

		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			query.setParameter("userId", pagination.getUserId());
		}

		List<String> regionList = query.list();
		List<PartnerDTO> partnerDtos = new ArrayList<>();

		for (String regionName : regionList) {
			PartnerDTO partnerDTO = new PartnerDTO();
			partnerDTO.setRegion(regionName);
			partnerDtos.add(partnerDTO);
		}
		return partnerDtos;
	}

	@SuppressWarnings("unchecked")
	private List<Object[]> getPartnerJourneyCompanyInfoForFilterById(Pagination pagination) {
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), true);
		try {
			Session session = sessionFactory.getCurrentSession();
			Integer userId = pagination.getUserId();
			Integer companyId = userDao.getCompanyIdByUserId(userId);
			String partnershipStatus = pagination.getPartnershipStatus() != null ? pagination.getPartnershipStatus()
					: "approved";
			String sql = "SELECT DISTINCT xup1.company_id AS \"companyId\", " + "xcp.company_name AS \"companyName\" "
					+ "FROM xt_partnership xp " + "LEFT JOIN xt_user_list xul ON xul.company_id = xp.vendor_company_id "
					+ "LEFT JOIN xt_user_userlist xuul ON xul.user_list_id = xuul.user_list_id AND xp.partner_id = xuul.user_id "
					+ "LEFT JOIN xt_user_profile xup1 ON xuul.user_id = xup1.user_id "
					+ "LEFT JOIN xt_company_profile xcp ON xcp.company_id = xup1.company_id "
					+ "WHERE xp.vendor_company_id = " + companyId + " AND xul.is_default_partnerlist = TRUE "
					+ " AND xup1.company_id IS NOT NULL "
					+ " AND xp.status ={status} and xcp.company_name_status='active' "
					+ getPartnerTeamMemberGroupFilterSQL("xup1.company_id", userId, teamMemberFilterDTO,
							partnershipStatus)
					+ " AND xp.partner_company_id IN " + "(SELECT DISTINCT up.company_id " + "FROM xt_lead l "
					+ "LEFT JOIN xt_user_profile up ON l.created_by = up.user_id " + "WHERE l.created_for_company_id = "
					+ companyId + " AND l.created_for_company_id != l.created_by_company_id "
					+ " AND up.company_id IS NOT NULL "
					+ getPartnerTeamMemberGroupFilterSQL("l.created_by_company_id", userId, teamMemberFilterDTO,
							partnershipStatus)
					+ "  " + "UNION " + "SELECT DISTINCT up.company_id " + "FROM xt_deal d "
					+ "LEFT JOIN xt_user_profile up ON d.created_by = up.user_id " + "WHERE d.created_for_company_id = "
					+ companyId + " AND d.created_for_company_id != d.created_by_company_id "
					+ " AND up.company_id IS NOT NULL "
					+ getPartnerTeamMemberGroupFilterSQL("d.created_by_company_id", userId, teamMemberFilterDTO,
							partnershipStatus)
					+ " " + "UNION " + "SELECT DISTINCT p.partner_company_id "
					+ "FROM xt_learning_track_visibility xltv "
					+ "LEFT JOIN xt_learning_track xlt ON xltv.learning_track_id = xlt.id "
					+ "LEFT JOIN xt_partnership p ON p.vendor_company_id = xlt.company_id "
					+ " AND xltv.partnership_id = p.id "
					+ "LEFT JOIN xt_learning_track_content_partner_activity xltcp ON xltcp.learning_track_visibility_id = xltv.id "
					+ "WHERE p.vendor_company_id = " + companyId
					+ " AND p.partner_company_id IS NOT NULL and xlt.type = 'TRACK' and xlt.is_published = true and xltv.progress>0 "
					+ getPartnerTeamMemberGroupFilterSQL("p.partner_id", userId, teamMemberFilterDTO, partnershipStatus)
					+ " " + "UNION " + "SELECT DISTINCT p.partner_company_id "
					+ "FROM xt_learning_track_visibility xltv "
					+ "LEFT JOIN xt_learning_track xlt ON xltv.learning_track_id = xlt.id "
					+ "LEFT JOIN xt_partnership p ON p.vendor_company_id = xlt.company_id "
					+ " AND xltv.partnership_id = p.id "
					+ "LEFT JOIN xt_learning_track_content_partner_activity xltcp ON xltcp.learning_track_visibility_id = xltv.id "
					+ "WHERE p.vendor_company_id = " + companyId
					+ " AND p.partner_company_id IS NOT NULL and xlt.type = 'PLAYBOOK' and xlt.is_published = true and xltv.progress>0 "
					+ getPartnerTeamMemberGroupFilterSQL("p.partner_id", userId, teamMemberFilterDTO, partnershipStatus)
					+ " " + "UNION " + "SELECT DISTINCT xp.partner_company_id " + "FROM xt_dam_partner xdp "
					+ "LEFT JOIN xt_dam xd ON xd.id = xdp.dam_id "
					+ "LEFT JOIN xt_dam_partner_mapping xdpm ON xdpm.dam_partner_id = xdp.id "
					+ "left join xt_dam_analytics xda on xda.dam_partner_mapping_fk_id=xdpm.id "
					+ "LEFT JOIN xt_partnership xp ON xp.id = xdp.partnership_id "
					+ " AND xd.company_id = xp.vendor_company_id " + "WHERE xp.vendor_company_id = " + companyId
					+ " AND xp.partner_company_id IS NOT NULL and xda.action_type ='VIEW' "
					+ getPartnerTeamMemberGroupFilterSQL("xp.partner_id", userId, teamMemberFilterDTO,
							partnershipStatus)
					+ " " + "UNION " + "SELECT distinct p.partner_company_id " + "FROM xt_dam_partner dp "
					+ "JOIN xt_dam d on dp.dam_id = d.id "
					+ "JOIN xt_partnership p on p.id=dp.partnership_id and d.company_id =p.vendor_company_id "
					+ "JOIN xt_company_profile c on p.partner_company_id = c.company_id "
					+ "JOIN xt_dam_partner_group_mapping dpgm on dp.id = dpgm.dam_partner_id "
					+ "left join xt_dam_analytics xda on xda.dam_partner_fk_id=dp.id " + "WHERE p.vendor_company_id = "
					+ companyId + " and xda.action_type ='VIEW' "
					+ getPartnerTeamMemberGroupFilterSQL("p.partner_id", userId, teamMemberFilterDTO, partnershipStatus)
					+ " " + "UNION " + "SELECT DISTINCT up1.company_id " + "FROM xt_campaign c "
					+ "LEFT JOIN xt_campaign p ON (c.campaign_id = p.parent_campaign_id) "
					+ "LEFT JOIN xt_user_profile up ON (up.user_id = c.customer_id) "
					+ "LEFT JOIN xt_user_profile up1 ON (up1.user_id = p.customer_id) " + "WHERE up.company_id = "
					+ companyId + " AND p.vendor_organization_id = " + companyId + " AND p.is_launched = TRUE "
					+ " AND p.is_nurture_campaign = TRUE " + " AND p.campaign_id IS NOT NULL "
					+ " AND up1.company_id IS NOT NULL "
					+ getPartnerTeamMemberGroupFilterSQL("up1.user_id", userId, teamMemberFilterDTO, partnershipStatus)
					+ ")";
			sql = replacePartnershipStatus(sql, partnershipStatus);
			Query query = session.createSQLQuery(sql);
			return query.list();
		} catch (HibernateException e) {
			throw new CategoryDataAccessException(e.getMessage());
		} catch (Exception ex) {
			throw new CategoryDataAccessException(ex);
		}
	}

	@Override
	public List<Object[]> listPartnersLaunchedCampaignsByCampaignTypeBarGraph(
			PartnerJourneyRequestDTO partnerJourneyRequestDTO) {
		Session session = sessionFactory.getCurrentSession();
		String partnershipStatus = partnerJourneyRequestDTO.getPartnershipStatus() != null
				? partnerJourneyRequestDTO.getPartnershipStatus()
				: "approved";
		CompanyProfile companyProfile = userDao.findByPrimaryKey(partnerJourneyRequestDTO.getLoggedInUserId(),
				new FindLevel[] { FindLevel.COMPANY_PROFILE }).getCompanyProfile();
		Integer companyId = companyProfile.getId();
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(
				partnerJourneyRequestDTO.getLoggedInUserId(), partnerJourneyRequestDTO.isPartnerTeamMemberGroupFilter(),
				false);
		boolean applyTeamMemberFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
		if (teamMemberFilterDTO.isEmptyFilter()) {
			return new ArrayList<Object[]>();
		} else {
			if (applyTeamMemberFilter) {
				String sql = " select campaign_type,count(distinct campaign_id) from xt_campaign c  left join xt_partnership p on p.partner_id = c.customer_id and p.vendor_company_id="
						+ companyId
						+ "  where is_nurture_campaign = true and parent_campaign_id is not null and is_launched = true and vendor_organization_id = "
						+ companyId
						+ " and campaign_type!='LANDINGPAGE' and p.status={status} and customer_id in (select user_id from xt_user_profile where company_id in(:partnerCompanyIds))  group by campaign_type";
				sql = replacePartnershipStatus(sql, partnershipStatus);
				SQLQuery query = session.createSQLQuery(sql);
				query.setParameterList("partnerCompanyIds", teamMemberFilterDTO.getPartnershipIdsOrPartnerCompanyIds());
				return query.list();

			} else {
				String sql = " select campaign_type,count(distinct campaign_id) from xt_campaign c left join xt_partnership p on p.partner_id = c.customer_id and p.vendor_company_id="
						+ companyId
						+ " where is_nurture_campaign = true and parent_campaign_id is not null and is_launched = true and vendor_organization_id = "
						+ companyId + " and campaign_type!='LANDINGPAGE' and p.status={status} group by campaign_type";
				if (partnerJourneyRequestDTO.getSelectedPartnerCompanyIds() != null
						&& !partnerJourneyRequestDTO.getSelectedPartnerCompanyIds().isEmpty()) {
					sql = " select campaign_type,count(distinct campaign_id) from xt_campaign c left join xt_partnership p on p.partner_id = c.customer_id and p.vendor_company_id="
							+ companyId
							+ " where is_nurture_campaign = true and parent_campaign_id is not null and is_launched = true and vendor_organization_id = "
							+ companyId
							+ " and campaign_type!='LANDINGPAGE' and p.status={status} and customer_id in (select user_id from xt_user_profile where company_id in(:partnerCompanyIds))  group by campaign_type";
				}
				sql = replacePartnershipStatus(sql, partnershipStatus);

				SQLQuery query = session.createSQLQuery(sql);
				if (partnerJourneyRequestDTO.getSelectedPartnerCompanyIds() != null
						&& !partnerJourneyRequestDTO.getSelectedPartnerCompanyIds().isEmpty()) {
					query.setParameterList("partnerCompanyIds",
							partnerJourneyRequestDTO.getSelectedPartnerCompanyIds());
				}
				return query.list();
			}
		}
	}
	// ************************ End:
	// XNFR-316********************************************

	@Override
	public BigInteger getPendingSignupAndCompanyProfileIncompletePartnersCount(Integer companyId, boolean applyFilter,
			Integer userId, String countType) {
		String queryString = "";
		if (countType.equals(XamplifyConstants.PARTNER_SIGNUP_URL_PREFIX)) {
			queryString = " select count(distinct xp.partner_id)\n" + "  from xt_partnership xp\n"
					+ "  left join xt_user_profile xup on xp.vendor_id=xup.user_id\n"
					+ "  left join xt_user_profile xup1 on xp.partner_id=xup1.user_id\n"
					+ "  where xup.company_id=:companyId \n"
					+ "  and xup1.status='UnApproved' and xp.status = 'approved' ";

		} else if (countType.equals(XamplifyConstants.INCOMPLETE_COMPANY_PROFILE)) {
			queryString = "SELECT COUNT(DISTINCT CASE \n"
					+ " WHEN(xcp.company_name_status = 'inactive' and xup1.status='APPROVE') \n"
					+ " or (xup1.company_id IS null and  xup1.status='APPROVE') THEN xp.partner_id \n"
					+ " END) AS inactive_partners_count FROM \n" + " xt_partnership xp LEFT JOIN \n"
					+ " xt_user_profile xup ON xp.vendor_id = xup.user_id \n"
					+ "LEFT JOIN xt_user_profile xup1 ON xp.partner_id = xup1.user_id \n"
					+ "LEFT JOIN xt_company_profile xcp ON xcp.company_id = xup1.company_id \n"
					+ "WHERE xup.company_id =:companyId and xp.status = 'approved'";
		}
		List<Integer> filteredPartnersIdsList = teamMemberIds(applyFilter, userId);
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId));
		if (applyFilter) {
			queryString += " and xp.partner_id in(:userListId) ";
			hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs()
					.add(new QueryParameterListDTO(XamplifyConstants.USER_LIST_ID, filteredPartnersIdsList));
		}
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);

		return (BigInteger) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
	}

	private List<Integer> teamMemberIds(boolean applyFilter, Integer userId) {
		List<Integer> filteredPartnersIdsList = new ArrayList<>();
		if (applyFilter) {
			filteredPartnersIdsList = userListDAO.getTeamMemberGroupedPartnerIds(userId);
			filteredPartnersIdsList = XamplifyUtils.isNotEmptyList(filteredPartnersIdsList) ? filteredPartnersIdsList
					: Arrays.asList(0);
		}
		return filteredPartnersIdsList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getPendingSignupAndCompanyProfileIncompletePartners(Integer companyId,
			Pagination pagination) {
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), true);
		List<String> defaultEmailTypes = new ArrayList<String>();
		String queryString = "SELECT DISTINCT xp.partner_id AS \"partnerId\", "
				+ "xcp.company_name AS \"CompanyName\", " + "xup1.email_id AS \"emailId\", "
				+ "xup1.firstname AS \"firstName\", " + "xup1.lastname AS \"lastName\", "
				+ "xup1.job_title AS \"jobTitle\", " + "xup1.mobile_number AS \"mobileNumber\", "
				+ "CAST(xup1.password AS TEXT) AS \"password\", " + "xma.vanity_url_domain AS \"vanityUrlDomain\", "
				+ "case when latest_email.email_type = 'ACCOUNT_ACTIVATION' then true else false end as \"activationMail\","
				+ "latest_email.sent_on AS \"sentOn\"" + "FROM xt_partnership xp "
				+ "LEFT JOIN xt_user_profile xup ON xp.vendor_id = xup.user_id "
				+ "LEFT JOIN xt_user_profile xup1 ON xp.partner_id = xup1.user_id "
				+ "LEFT JOIN xt_company_profile xcp ON xcp.company_id = xup1.company_id "
				+ "LEFT JOIN xt_module_access xma ON xma.company_id = :companyId " + "LEFT JOIN (\r\n"
				+ "    SELECT\r\n" + "        partner_id,\r\n" + "        email_type,\r\n" + "        sent_on,\r\n"
				+ "        vendor_company_id,\r\n"
				+ "        ROW_NUMBER() OVER (PARTITION BY partner_id ORDER BY sent_on DESC) AS rn\r\n"
				+ "    FROM xt_vendor_email_sent_log\r\n" + "    WHERE vendor_company_id = :companyId\r\n"
				+ ") latest_email\r\n" + " ON latest_email.partner_id = xp.partner_id\r\n"
				+ "   AND latest_email.vendor_company_id = :companyId\r\n" + "   AND latest_email.rn = 1\r\n"
				+ "   and latest_email.email_type in (DEFAULT_EMAIL_TEMPLATES) "
				+ "WHERE xup.company_id = :companyId  and xp.status = 'approved' ";
		if (XamplifyUtils.isValidString(pagination.getModuleName())) {
			if (pagination.getModuleName().equalsIgnoreCase(XamplifyConstants.PARTNER_SIGNUP_URL_PREFIX)) {
				queryString += " AND xup1.status = 'UnApproved'";
				defaultEmailTypes.add("'JOIN_VENDOR_COMPANY'");
				defaultEmailTypes.add("'JOIN_PRM_COMPANY'");
				defaultEmailTypes.add("'ACCOUNT_ACTIVATION'");
			} else if (pagination.getModuleName().equalsIgnoreCase(XamplifyConstants.INCOMPLETE_COMPANY_PROFILE)) {
				queryString += "AND ((xcp.company_name_status = 'inactive' AND xup1.status = 'APPROVE') "
						+ "OR (xup1.company_id IS NULL AND xup1.status = 'APPROVE'))";
				defaultEmailTypes.add("'COMPANY_PROFILE_INCOMPLETE'");
			}
		}

		List<Integer> filteredPartnersIdsList = teamMemberIds(pagination.isPartnerTeamMemberGroupFilter(),
				pagination.getUserId());
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId));
		QueryParameterDTO queryParameterDTO = new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId);
		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			queryString += " and xp.partner_id in(:userListId) ";
			hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs()
					.add(new QueryParameterListDTO(XamplifyConstants.USER_LIST_ID, filteredPartnersIdsList));
		}
		if (XamplifyUtils.isNotEmptyList(defaultEmailTypes)) {
			queryString = queryString.replaceAll("DEFAULT_EMAIL_TEMPLATES", String.join(",", defaultEmailTypes));
		}
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(queryParameterDTO);

		hibernateSQLQueryResultRequestDTO.setClassInstance(PartnerDTO.class);
		List<String> searchColumns = new ArrayList<>();
		searchColumns.add("xup1.email_id");
		searchColumns.add("xup1.firstname");
		searchColumns.add("xup1.lastname");
		searchColumns.add("xcp.company_name");
		hibernateSQLQueryResultRequestDTO.setSearchColumns(searchColumns);
		hibernateSQLQueryResultRequestDTO.setGroupByQueryString(" GROUP BY xp.partner_id," + "xcp.company_name,"
				+ "xup1.email_id," + "xup1.firstname," + "xup1.lastname," + "xup1.job_title," + "xup1.mobile_number,"
				+ "xup1.password," + "xma.vanity_url_domain," + "latest_email.email_type," + "latest_email.sent_on");
		return hibernateSQLQueryResultUtilDao.returnPaginatedDTOList(hibernateSQLQueryResultRequestDTO, pagination,
				pagination.getSearchKey());
	}

	@Override
	public Integer getDefaultPartnerListidByUserid(Integer logginedUserId) {
		String queryString = "SELECT xul.user_list_id FROM xt_user_list xul\n"
				+ "JOIN xt_user_profile xup ON xul.company_id = xup.company_id\n"
				+ "WHERE xup.user_id =:userId AND xul.is_default_partnerlist = true; ";

		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(XamplifyConstants.USER_ID, logginedUserId));

		return (Integer) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
	}

	private String frameDateFilterQuery(PartnerJourneyRequestDTO partnerJourneyRequestDTO, String dateColumn) {
		String dateFilterQueryString = "";
		if (partnerJourneyRequestDTO.getFromDateFilter() != null
				&& partnerJourneyRequestDTO.getToDateFilter() != null) {
			dateFilterQueryString = " and " + dateColumn + " between  TO_TIMESTAMP('"
					+ partnerJourneyRequestDTO.getFromDateFilter()
					+ "', 'Dy Mon DD HH24:MI:SS ZZZ YYYY') and TO_TIMESTAMP('"
					+ partnerJourneyRequestDTO.getToDateFilter() + "', 'Dy Mon DD HH24:MI:SS ZZZ YYYY') ";
		}
		return dateFilterQueryString;
	}

	private String frameDateFilterQuery(Pagination pagination, String dateColumn) {
		String dateFilterQueryString = "";
		if (pagination.getFromDateFilter() != null && pagination.getToDateFilter() != null) {
			dateFilterQueryString = " and " + dateColumn + " between  TO_TIMESTAMP('" + pagination.getFromDateFilter()
					+ "', 'Dy Mon DD HH24:MI:SS ZZZ YYYY') and TO_TIMESTAMP('" + pagination.getToDateFilter()
					+ "', 'Dy Mon DD HH24:MI:SS ZZZ YYYY') ";
		}
		return dateFilterQueryString;
	}

	private String frameAllRedistributedCampaignsAndLeadsCountQuery(PartnerJourneyRequestDTO partnerJourneyRequestDTO) {
		String leadDateFilter = frameDateFilterQuery(partnerJourneyRequestDTO, LEAD_DATE_COLUMN);
		String campiagnDateFilter = frameDateFilterQuery(partnerJourneyRequestDTO, CAMPAIGN_DATE_COLUMN);
		return "with a as (select  xl.created_by_company_id,cast(count(distinct xl.id) as int) as leads from public.xt_lead xl left join \n"
				+ "xt_company_profile xuc on xl.created_by_company_id = xuc.company_id left join xt_user_profile xup on xl.created_by = xup.user_id left join xt_user_role xur on \n"
				+ "xur.user_id = xup.user_id and xur.user_id = xl.created_by left join xt_partnership p on p.partner_company_id= xl.created_by_company_id and p.vendor_company_id = :companyId where created_for_company_id = :companyId  and p.status={status} "
				+ leadDateFilter + "and xur.role_id !=2 group by 1), b as (select distinct xcp1.company_name \n"
				+ "as \"partnerCompanyName\",xcp1.company_id as \"partner_company\",cast(count (distinct xc1.campaign_id) as int) as \"redistributedCampaignsCount\" from public.xt_campaign xc left join \n"
				+ "xt_user_profile xup on (xc.customer_id=xup.user_id)left join xt_user_role xur on (xur.user_id=xup.user_id) left join public.xt_company_profile xcp on \n"
				+ "(xup.company_id=xcp.company_id) left join public.xt_campaign xc1 on (xc.campaign_id=xc1.parent_campaign_id)left join public.xt_user_profile xup1 ON \n"
				+ "(xc1.customer_id = xup1.user_id) left join public.xt_company_profile xcp1 ON (xup1.company_id = xcp1.company_id) left join xt_partnership p on p.partner_company_id= xup1.company_id and p.vendor_company_id = :companyId where xup.company_id= :companyId and \n"
				+ "xc1.vendor_organization_id is not null and xc1.is_launched= true and  xc1.is_nurture_campaign =  true and xc1.parent_campaign_id is not null \n"
				+ "and xur.role_id !=2  and p.status={status} " + campiagnDateFilter
				+ " and xcp.company_id != xcp1.company_id group by 1,2 order by 2 desc) select b.\"partnerCompanyName\",b.\"redistributedCampaignsCount\",case when a.leads is null then 0 else a.leads end as \"leadsCount\" \n"
				+ "from b left join a on a.created_by_company_id = b.partner_company";
	}

	private String frameAllPartnerCompanyNamesAndLeadsAndDealsCountQuery(
			PartnerJourneyRequestDTO partnerJourneyRequestDTO) {
		String leadDateFilter = frameDateFilterQuery(partnerJourneyRequestDTO, LEAD_DATE_COLUMN);
		String dealDateFilter = frameDateFilterQuery(partnerJourneyRequestDTO, DEAL_DATE_COLUMN);
		return "select distinct company_name,case when sum(leads) is null then 0 else sum(leads) end as leads, \n"
				+ " case when sum(deals) is null then 0 else sum(deals) end as deals from (select distinct xuc.company_name,xl.created_by_company_id,count(distinct xl.id) as leads,\n"
				+ " cast(null as int) as deals,'leads' as data from xt_company_profile xuc left join public.xt_lead xl on xuc.company_id = xl.created_by_company_id \n"
				+ " left join public.xt_user_profile xup on xup.user_id = xl.created_by left join xt_user_role xur on xur.user_id = xup.user_id  left join xt_partnership p on p.partner_company_id= xl.created_by_company_id and p.vendor_company_id = :companyId  \n"
				+ " where xl.created_for_company_id = :companyId and xur.role_id !=2  and p.status={status} "
				+ leadDateFilter + " and xl.created_by_company_id != xl.created_for_company_id \n"
				+ " group by 1,2 union all select xuc.company_name,xd.created_by_company_id,cast(null as int ) as leads,count(distinct xd.id) as deals, \n"
				+ " 'deals' as data from xt_company_profile xuc left join public.xt_deal xd on (xuc.company_id = xd.created_by_company_id) \n"
				+ " left join public.xt_user_profile xup on xup.user_id = xd.created_by left join xt_user_role xur on xur.user_id = xup.user_id  left join xt_partnership p on p.partner_company_id= xd.created_by_company_id and p.vendor_company_id = :companyId  \n"
				+ " where xd.created_for_company_id = :companyId and p.status={status} " + dealDateFilter
				+ " and xur.role_id !=2 and  xd.created_by_company_id != xd.created_for_company_id group by 1,2)foo \n"
				+ " group by 1";
	}

	@Override
	public Map<String, Object> getTotalPartnersCount(Integer companyId, boolean applyFilter, Integer userId) {
		Map<String, Object> map = new HashMap<>();
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(userId, applyFilter, false);
		String queryString = " select count(distinct xp.partner_id) from xt_partnership xp\n"
				+ "  left join xt_user_profile xup on xp.vendor_id=xup.user_id\n"
				+ "  left join xt_user_profile xup1 on xp.partner_id=xup1.user_id\n"
				+ "  where xup.company_id=:companyId and (xp.status='approved' or xp.status='deactivated') ";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId));
		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			List<Integer> filteredPartnersIdsList = teamMemberIds(applyFilter, userId);
			queryString += " and xp.partner_id in(:userListId) ";
			hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs()
					.add(new QueryParameterListDTO(XamplifyConstants.USER_LIST_ID, filteredPartnersIdsList));
		}
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		BigInteger totalPartnersCount = (BigInteger) hibernateSQLQueryResultUtilDao
				.getUniqueResult(hibernateSQLQueryResultRequestDTO);
		map.put("totalPartnersCount", totalPartnersCount);
		return map;
	}

	@Override
	public String getPartnerJourneyTrackAssetCount(PartnerJourneyRequestDTO partnerJourneyRequestDTO) {
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(
				partnerJourneyRequestDTO.getLoggedInUserId(), partnerJourneyRequestDTO.isPartnerTeamMemberGroupFilter(),
				true);
		String finalQueryString = allTrackAssetsCountQuery;
		String partnershipStatus = partnerJourneyRequestDTO.getPartnershipStatus() != null
				? partnerJourneyRequestDTO.getPartnershipStatus()
				: "approved";
		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			finalQueryString += " and xup1.company_id " + partnerJourneyTeamMemberFilterQuery;
		}
		finalQueryString = replacePartnershipStatus(finalQueryString, partnershipStatus);
		if (partnerJourneyRequestDTO.isDetailedAnalytics()) {
			finalQueryString += " and xup1.company_id = :partnerCompanyId ";
		} else {
			if (partnerJourneyRequestDTO.getSelectedPartnerCompanyIds() != null
					&& !partnerJourneyRequestDTO.getSelectedPartnerCompanyIds().isEmpty()) {
				finalQueryString += " and xup1.company_id in (:partnerCompanyIds) ";
			}
		}
		Integer teamMemberUserId = partnerJourneyRequestDTO.getTeamMemberUserId();
		if (teamMemberUserId != null && teamMemberUserId > 0) {
			finalQueryString += xup1UserIdTMFilter;
		}

		String dateFilterQueryString = frameDateFilterQuery(partnerJourneyRequestDTO, TRACK_PUBLISHED_DATE_COLUMN);
		finalQueryString += dateFilterQueryString;

		return (String) executePartnerJourneyUniqueResultQuery(finalQueryString, partnerJourneyRequestDTO, true);
	}

	@Override
	public Map<String, Object> getPartnerJourneyAssetDetails(Pagination pagination) {
		HashMap<String, Object> map = new HashMap<>();
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), true);
		String queryString = framePartnerJourneyAssetsDetailsQuery(pagination, teamMemberFilterDTO);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", pagination.getVendorCompanyId());

		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			query.setParameter("teamMemberUserId", pagination.getTeamMemberId());
		}
		if (pagination.isDetailedAnalytics()) {
			query.setParameter("partnerCompanyId", pagination.getPartnerCompanyId());
		} else if (pagination.getSelectedPartnerCompanyIds() != null
				&& !pagination.getSelectedPartnerCompanyIds().isEmpty()) {
			query.setParameterList("partnerCompanyIds", pagination.getSelectedPartnerCompanyIds());
		}

		/*
		 * if (teamMemberFilterDTO.isApplyTeamMemberFilter() ||
		 * teamMemberFilterDTO.isEmptyFilter()) {
		 * query.setParameter("teamMemberUserIdFilter", pagination.getUserId()); }
		 */
		bindQueryParametersForAssets(query, pagination, teamMemberFilterDTO);
		return paginationUtil.setScrollableAndGetList(pagination, map, query, PartnerJourneyTrackDetailsDTO.class);
	}

	private void bindQueryParametersForAssets(SQLQuery query, Pagination pagination,
			TeamMemberFilterDTO teamMemberFilterDTO) {
		if (StringUtils.hasText(pagination.getSearchKey())) {
			query.setParameter("searchKey", "%" + pagination.getSearchKey() + "%");
		}
		if (XamplifyUtils.isNotEmptyList(pagination.getSelectedCompanyIds())) {
			query.setParameterList("companyIdList", pagination.getSelectedCompanyIds());
		}
		if (XamplifyUtils.isNotEmptyList(pagination.getAssetIds())) {
			query.setParameterList("assetList", pagination.getAssetIds());
		}
		if (XamplifyUtils.isNotEmptyList(pagination.getSelectedEmailIds())) {
			query.setParameterList("emailIdList", pagination.getSelectedEmailIds());
		}
	}

	private String framePartnerJourneyAssetsDetailsQuery(Pagination pagination,
			TeamMemberFilterDTO teamMemberFilterDTO) {

		String partnershipStatus = pagination.getPartnershipStatus() != null ? pagination.getPartnershipStatus()
				: "approved";

		boolean isDetailed = pagination.isDetailedAnalytics();
		boolean isTeamMemberContext = teamMemberFilterDTO.isApplyTeamMemberFilter()
				|| teamMemberFilterDTO.isEmptyFilter();

		String finalQueryString = "";

		if (isTeamMemberContext) {
			finalQueryString = partnerJourneyAssetDetailsForTeamMemberLogin;
		} else if (isDetailed) {
			finalQueryString = assetDetailsViewAndDownloadedDetailedQuery;
		} else {
			finalQueryString = partnerJourneyAssetDetailsViewAndDetailedCountQuery;
		}

		if (!pagination.isDetailedAnalytics()) {
			finalQueryString = replacePartnershipStatus(finalQueryString, partnershipStatus);
		}

		String searchQueryString = isDetailed ? partnerJourneyAssetDetailsSearchQuery : assetDetailsSearchQuery;

		String teamMemberFilterQuery = "";
		String myPartnerFilter = "";
		String partnerFilter = "";

		boolean hasCompanyIds = XamplifyUtils.isNotEmptyList(pagination.getSelectedCompanyIds());
		boolean hasAssetIds = XamplifyUtils.isNotEmptyList(pagination.getAssetIds());
		boolean hasEmailIds = XamplifyUtils.isNotEmptyList(pagination.getSelectedEmailIds());
		boolean hasSortFilter = StringUtils.hasText(pagination.getSortcolumn());
		boolean hasSearchKey = StringUtils.hasText(pagination.getSearchKey());

		if (isDetailed) {
			finalQueryString = finalQueryString.replace("{teamMemberFilter}", teamMemberFilterQuery);

			if (hasEmailIds || hasAssetIds) {
				finalQueryString = applyEmailIdAndAssetConditions(finalQueryString, pagination);
			}
		} else {
			if (hasCompanyIds || hasAssetIds) {
				finalQueryString = applyCompanyIdAndAssetConditions(finalQueryString, pagination);
			}
			if (XamplifyUtils.isNotEmptyList(pagination.getSelectedPartnerCompanyIds())) {
				partnerFilter = "and xup1.company_id in (:partnerCompanyIds) ";
			}
			finalQueryString = finalQueryString.replace("{partnerFilter}", partnerFilter).replace("{myPartnerFilter}",
					myPartnerFilter);
		}

		if (hasSearchKey) {
			finalQueryString += searchQueryString;
		}

		if (XamplifyUtils.isValidString(pagination.getFilterFromDateString())
				&& XamplifyUtils.isValidString(pagination.getFilterToDateString())) {
			finalQueryString = addDateFilterQueryForPartnerAssets(finalQueryString, pagination);
		}

		finalQueryString = hasSortFilter ? applySortOptionCondition(finalQueryString, pagination)
				: finalQueryString + "  ORDER BY a.\"viewedCount\" DESC";

		String dateFilterQuery = frameDateFilterQuery(pagination, "xdp.published_time");
		finalQueryString = finalQueryString.replace("{dateFilter}", dateFilterQuery);

		return finalQueryString;
	}

	private String addDateFilterQueryForPartnerAssets(String query, Pagination pagination) {
		StringBuilder filterQuery = new StringBuilder(" ");

		String fromDate = pagination.getFilterFromDateString();
		String toDate = pagination.getFilterToDateString();
		if (XamplifyUtils.isValidString(fromDate) && XamplifyUtils.isValidString(toDate)) {
			if (fromDate != null && toDate != null) {
				filterQuery.append(" AND ((a.\"viewedTime\" >= '").append(fromDate).append(" 00:00:00' ")
						.append(" AND a.\"viewedTime\" <= '").append(toDate).append(" 23:59:59') ")
						.append(" OR (a.\"downloadedTime\" >= '").append(fromDate).append(" 00:00:00' ")
						.append(" AND a.\"downloadedTime\" <= '").append(toDate).append(" 23:59:59')) ");
			}
		}
		return query + String.valueOf(filterQuery);
	}

	private String applyCompanyIdAndAssetConditions(String query, Pagination pagination) {
		StringBuilder conditionBuilder = new StringBuilder();

		if (XamplifyUtils.isNotEmptyList(pagination.getSelectedCompanyIds())) {
			conditionBuilder.append(" AND a.\"companyId\" IN (:companyIdList) ");
		}

		if (XamplifyUtils.isNotEmptyList(pagination.getAssetIds())) {
			conditionBuilder.append(" AND a.\"assetId\" IN (:assetList) ");
		}
		return query + String.valueOf(conditionBuilder);
	}

	private String applyEmailIdAndAssetConditions(String query, Pagination pagination) {
		StringBuilder conditionBuilder = new StringBuilder();

		if (XamplifyUtils.isNotEmptyList(pagination.getSelectedEmailIds())) {
			conditionBuilder.append(" AND a.\"emailId\" IN (:emailIdList) ");
		}

		if (XamplifyUtils.isNotEmptyList(pagination.getAssetIds())) {
			conditionBuilder.append(" AND a.\"assetId\" IN (:assetList) ");
		}
		return query + String.valueOf(conditionBuilder);
	}

	private String addDateFilterQueryForAssets(String query, Pagination pagination) {
		StringBuilder filterQuery = new StringBuilder(" ");

		String fromDate = pagination.getFromDateFilterString();
		String toDate = pagination.getToDateFilterString();
		if (XamplifyUtils.isValidString(fromDate) && XamplifyUtils.isValidString(toDate)) {
			if (fromDate != null && toDate != null) {
				filterQuery.append(" AND ((a.\"viewedTime\" >= '").append(fromDate).append(" 00:00:00' ")
						.append(" AND a.\"viewedTime\" <= '").append(toDate).append(" 23:59:59') ")
						.append(" OR (a.\"downloadedTime\" >= '").append(fromDate).append(" 00:00:00' ")
						.append(" AND a.\"downloadedTime\" <= '").append(toDate).append(" 23:59:59')) ");
			}
		}
		return query + String.valueOf(filterQuery);
	}

	private String applySortOptionCondition(String query, Pagination pagination) {
		StringBuilder updatedQuery = new StringBuilder(query);
		String sortColumn = pagination.getSortcolumn();
		String sortOrder = pagination.getSortingOrder();

		if (XamplifyUtils.isValidString(sortColumn) && XamplifyUtils.isValidString(sortOrder)
				&& !"null".equalsIgnoreCase(sortColumn)) {
			if ("viewCount".equalsIgnoreCase(sortColumn) && "Asc".equalsIgnoreCase(sortOrder)) {
				updatedQuery.append(" ORDER BY a.\"viewedCount\" ASC");
			} else if ("viewCount".equalsIgnoreCase(sortColumn) && "Desc".equalsIgnoreCase(sortOrder)) {
				updatedQuery.append(" ORDER BY a.\"viewedCount\" DESC");
			} else if ("downloadCount".equalsIgnoreCase(sortColumn) && "Asc".equalsIgnoreCase(sortOrder)) {
				updatedQuery.append(" ORDER BY a.\"downloadedCount\" ASC");
			} else if ("downloadCount".equalsIgnoreCase(sortColumn) && "Desc".equalsIgnoreCase(sortOrder)) {
				updatedQuery.append(" ORDER BY a.\"downloadedCount\" DESC");
			}
		} else {
			updatedQuery.append(" ORDER BY a.\"viewedCount\" DESC ");
		}
		return String.valueOf(updatedQuery);
	}

	private String addDateFilterQuery(Pagination pagination) {
		StringBuilder filterQuery = new StringBuilder(" ");

		String fromDate = pagination.getFromDateFilterString();
		String toDate = pagination.getToDateFilterString();
		if (XamplifyUtils.isValidString(fromDate) && XamplifyUtils.isValidString(toDate)) {
			if (fromDate != null && toDate != null) {
				filterQuery.append(" and (a.\"publishedOn\" >= '").append(fromDate).append(" 00:00:00' ")
						.append("and a.\"publishedOn\" < '").append(toDate).append(" 23:59:59') ");
			}
		}
		return String.valueOf(filterQuery);
	}

//	XNFR-944
	public List<PartnerJourneyTrackDetailsDTO> getAllPartnerRegionSDetailsCount(
			PartnerJourneyRequestDTO partnerJourneyRequestDTO) {
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(
				partnerJourneyRequestDTO.getLoggedInUserId(), partnerJourneyRequestDTO.isPartnerTeamMemberGroupFilter(),
				true);
		Session session = sessionFactory.getCurrentSession();
		String finalQueryString = allRegionWiseDonutQuery;
		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			finalQueryString = allRegionWiseDonutQueryForTeamMemberLogin;
		}

		Query query = session.createSQLQuery(finalQueryString);
		query.setParameter("companyId", partnerJourneyRequestDTO.getVendorCompanyId());
		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			query.setParameter("userId", partnerJourneyRequestDTO.getLoggedInUserId());
		}

		return (List<PartnerJourneyTrackDetailsDTO>) paginationUtil.getListDTO(PartnerJourneyTrackDetailsDTO.class,
				query);
	}

	private String addFilterQuery(Pagination pagination) {
		StringBuilder filterQuery = new StringBuilder(" ");

		String fromDate = pagination.getFromDateFilterString();
		String toDate = pagination.getToDateFilterString();
		if (XamplifyUtils.isValidString(fromDate) && XamplifyUtils.isValidString(toDate)) {
			if (fromDate != null && toDate != null) {
				filterQuery.append(" and (a.\"dateLastLogin\" >= '").append(fromDate).append(" 00:00:00' ")
						.append("and a.\"dateLastLogin\" < '").append(toDate).append(" 23:59:59') ");
			}
		}
		return String.valueOf(filterQuery);
	}

	/** XNFR-952 **/
	@Override
	public PaginatedDTO listAllPartnersForContactUploadManagementSettings(Pagination pagination, String searchKey) {
		Integer companyId = pagination.getCompanyId();
		if (XamplifyUtils.isValidInteger(companyId)) {
			String sqlQueryString = allPartnersContactsUploadManagementQuery;
			if (XamplifyUtils.isValidString(searchKey)) {
				sqlQueryString = sqlQueryString.replace(ALL_PARTNERS_CONTACTS_SEARCH_REPLACE_KEY,
						allPartnersContactsUploadManagementSearchQuery);
				sqlQueryString = sqlQueryString.replace("${searchKey}", searchKey);
			} else {
				sqlQueryString = sqlQueryString.replace(ALL_PARTNERS_CONTACTS_SEARCH_REPLACE_KEY, "");
			}
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO.setQueryString(sqlQueryString);
			List<QueryParameterDTO> queryParameterDTOs = new ArrayList<>();
			queryParameterDTOs.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId));
			hibernateSQLQueryResultRequestDTO.setQueryParameterDTOs(queryParameterDTOs);
			String sortQuery = getSortQueryForPartnerContactLimitReport(pagination);
			hibernateSQLQueryResultRequestDTO.setSortQueryString(sortQuery);
			hibernateSQLQueryResultRequestDTO.setClassInstance(PartnerContactUsageDTO.class);
			return hibernateSQLQueryResultUtilDao.returnPaginatedDTO(hibernateSQLQueryResultRequestDTO, pagination,
					searchKey);
		} else {
			return new PaginatedDTO();
		}
	}

	private String getSortQueryForPartnerContactLimitReport(Pagination pagination) {
		String sortingOrder = pagination.getSortingOrder();
		if (XamplifyUtils.isValidString(pagination.getSortcolumn())
				&& XamplifyUtils.isValidString(pagination.getSortingOrder())) {
			List<SortColumnDTO> sortColumnDTOs = new ArrayList<>();
			if (pagination.getSortcolumn().equalsIgnoreCase("emailId")) {
				SortColumnDTO sortByEmail = new SortColumnDTO("emailId", "xup.email_id", false, true, false);
				sortColumnDTOs.add(sortByEmail);
			} else if (pagination.getSortcolumn().equalsIgnoreCase("assigned")) {
				SortColumnDTO sortByEmailAsc = new SortColumnDTO("assigned", "p.contacts_limit", false, true, false);
				sortColumnDTOs.add(sortByEmailAsc);
			} else if (pagination.getSortcolumn().equalsIgnoreCase("contactCompany")) {
				SortColumnDTO sortByEmailAsc = new SortColumnDTO("contactCompany", "xcp.company_name", false, true,
						false);
				sortColumnDTOs.add(sortByEmailAsc);
			} else if (pagination.getSortcolumn().equalsIgnoreCase("exceeded")) {
				return "order by \"exceededContactUploadLimit\" " + sortingOrder;
			}
			return paginationUtil.generateSortQuery(pagination, sortColumnDTOs, sortingOrder);
		}
		return "order by \"exceededContactUploadLimit\" desc";
	}

	/** XNFR-952 **/
	@Override
	public void updatePartnerContactUploadLimit(Integer partnerCompanyId, Integer vendorCompanyId,
			Integer contactLimit) {
		if (XamplifyUtils.isValidInteger(partnerCompanyId) && XamplifyUtils.isValidInteger(vendorCompanyId)
				&& XamplifyUtils.isValidInteger(contactLimit)) {
			String queryString = "update xt_partnership set contacts_limit = :contactLimit where "
					+ "partner_company_id = :partnerCompanyId and vendor_company_id = :vendorCompanyId";
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("partnerCompanyId", partnerCompanyId));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("vendorCompanyId", vendorCompanyId));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("contactLimit", contactLimit));
			hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);
		}
	}

	/** XNFR-952 **/
	@Override
	public Integer fetchNumberOfContactsAddedByCompanyId(Integer companyId) {
		Integer contactsCount = null;
		if (XamplifyUtils.isValidInteger(companyId)) {
			String queryString = "SELECT DISTINCT CAST(COUNT(DISTINCT xuul.user_id) as int) AS user_count "
					+ "from xt_user_list xul LEFT JOIN xt_user_userlist xuul ON xuul.user_list_id = xul.user_list_id "
					+ "where xul.module_name = 'CONTACTS' and xul.company_id = :companyId";
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("companyId", companyId));
			contactsCount = (Integer) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
		}
		return contactsCount != null ? contactsCount : 0;
	}

	/** XNFR-952 **/
	@Override
	public Integer getContactsUploadedCountByAllPartnersForCompanyById(Integer companyId) {
		Integer contactsCount = null;
		if (XamplifyUtils.isValidInteger(companyId)) {
			String queryString = contactCountUploadedByAllPartnersQuery;
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("companyId", companyId));
			contactsCount = (Integer) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
		}
		return contactsCount != null ? contactsCount : 0;
	}

	/** XNFR-952 **/
	@Override
	public Integer getContactUploadSubscriptionLimitByCompanyId(Integer companyId) {
		Integer contactLimit = null;
		if (XamplifyUtils.isValidInteger(companyId)) {
			String queryString = "select cast(contact_subscription_limit as int) from xt_module_access where company_id = :companyId";
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("companyId", companyId));
			contactLimit = (Integer) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
		}
		return contactLimit != null ? contactLimit : 0;
	}

	/* XNFR 944 */
	@Override
	public Map<String, Object> getAllPartnersDetailsList(Pagination pagination) {
		Integer companyId = userDao.getCompanyIdByUserId(pagination.getUserId());
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), true);

		String finalQuery = (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter())
				? allPartnersDetailsListQueryAndTeamMemberLoginQuery
				: allPartnersDetailsListQuery;

		StringBuilder baseQuery = new StringBuilder(finalQuery);

		boolean hasSearchKey = StringUtils.hasText(pagination.getSearchKey());
		boolean hasRegionIds = pagination.getSelectedRegionIds() != null
				&& !pagination.getSelectedRegionIds().isEmpty();
		boolean hasStatusIds = pagination.getSelectedStatusIds() != null
				&& !pagination.getSelectedStatusIds().isEmpty();
		boolean hasDateFilter = XamplifyUtils.isValidString(pagination.getFromDateFilterString())
				&& XamplifyUtils.isValidString(pagination.getToDateFilterString());
		boolean hasSortFilter = StringUtils.hasText(pagination.getSortcolumn());

		if (hasSearchKey) {
			applySearchCondition(baseQuery, pagination);
		}
		if (hasRegionIds || hasStatusIds) {
			applyFilterConditions(baseQuery, pagination);
		}
		if (hasDateFilter) {
			applyDateFilterConditions(baseQuery, pagination);
		}
		if (hasSortFilter) {
			applySortCondition(baseQuery, pagination);
		}

		Session session = sessionFactory.getCurrentSession();
		SQLQuery query = session.createSQLQuery(String.valueOf(baseQuery));

		query.setParameter("companyId", companyId);
		bindQueryParameters(query, pagination, teamMemberFilterDTO);
		Map<String, Object> resultMap = new HashMap<>();
		return paginationUtil.setScrollableAndGetList(pagination, resultMap, query,
				PartnerJourneyTrackDetailsDTO.class);
	}

	private void applyDateFilterConditions(StringBuilder query, Pagination pagination) {
		String fromDate = pagination.getFromDateFilterString();
		String toDate = pagination.getToDateFilterString();

		if (XamplifyUtils.isValidString(fromDate) && XamplifyUtils.isValidString(toDate)) {
			query.append(" AND (a.\"dateLastLogin\" >= '").append(fromDate).append(" 00:00:00' ")
					.append("AND a.\"dateLastLogin\" <= '").append(toDate).append(" 23:59:59') ");
		}
	}

	private void applySearchCondition(StringBuilder query, Pagination pagination) {
		if (XamplifyUtils.isValidString(pagination.getSearchKey())) {
			query.append("AND (").append("a.\"companyName\" ILIKE :searchKey OR ")
					.append("a.\"emailId\" ILIKE :searchKey OR ").append("a.\"firstName\" ILIKE :searchKey OR ")
					.append("a.\"lastName\" ILIKE :searchKey OR ").append("a.\"region\" ILIKE :searchKey OR ")
					.append("a.\"status\" ILIKE :searchKey OR ")
					.append("REPLACE(LOWER(a.\"firstName\" || a.\"lastName\"), ' ', '') ILIKE TRIM(LOWER(:searchKey)) OR ")
					.append("REPLACE(LOWER(a.\"lastName\" || a.\"firstName\"), ' ', '') ILIKE TRIM(LOWER(:searchKey)) ")
					.append(") ");
		}
	}

	private void applyFilterConditions(StringBuilder query, Pagination pagination) {
		if (XamplifyUtils.isNotEmptyList(pagination.getSelectedRegionIds())) {
			query.append("AND a.\"region\" IN (:regionList) ");
		}

		if (XamplifyUtils.isNotEmptyList(pagination.getSelectedStatusIds())) {
			query.append("AND a.\"status\" IN (:statusList) ");
		}
	}

	private void applySortCondition(StringBuilder query, Pagination pagination) {
		String sortColumn = pagination.getSortcolumn();
		if (XamplifyUtils.isValidString(sortColumn)) {
			switch (sortColumn.toLowerCase()) {
			case "nameasc":
				query.append("ORDER BY a.\"firstName\" ASC NULLS FIRST ");
				break;
			case "namedesc":
				query.append("ORDER BY a.\"firstName\" DESC NULLS LAST ");
				break;
			case "datelastloginasc":
				query.append("ORDER BY a.\"dateLastLogin\" ASC NULLS FIRST");
				break;
			case "datelastlogindesc":
				query.append("ORDER BY a.\"dateLastLogin\" DESC NULLS LAST");
				break;
			default:
				query.append("ORDER BY a.\"onboardedOn\" DESC NULLS LAST ");
				break;
			}
		} else {
			query.append("ORDER BY a.\"onboardedOn\" DESC NULLS LAST ");
		}
	}

	private void bindQueryParameters(SQLQuery query, Pagination pagination, TeamMemberFilterDTO teamMemberFilterDTO) {
		if (XamplifyUtils.isValidString(pagination.getSearchKey())) {
			query.setParameter("searchKey", "%" + pagination.getSearchKey() + "%");
		}
		if (XamplifyUtils.isNotEmptyList(pagination.getSelectedRegionIds())) {
			query.setParameterList("regionList", pagination.getSelectedRegionIds());
		}

		if (XamplifyUtils.isNotEmptyList(pagination.getSelectedStatusIds())) {
			query.setParameterList("statusList", pagination.getSelectedStatusIds());
		}

		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			query.setParameter("userId", pagination.getUserId());
		}
	}

	@Override
	public List<PartnerDTO> getAllAssetNamesForFilter(Pagination pagination) {

		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), true);
		String partnershipStatus = pagination.getPartnershipStatus() != null ? pagination.getPartnershipStatus()
				: "approved";
		String finalQuery = "";

		if (pagination.isDetailedAnalytics()) {
			finalQuery = partnerDetailedAnalyticsJourneyAssetNames;
		} else if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			finalQuery = partnerJourneyAssetNamesForTeamMemberLogin;
			finalQuery = replacePartnershipStatus(finalQuery, partnershipStatus);
		} else {
			finalQuery = partnerJourneyAssetNames;
			finalQuery = replacePartnershipStatus(finalQuery, partnershipStatus);
		}

		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(finalQuery);
		query.setParameter("vendorCompanyId", pagination.getVendorCompanyId());

		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			query.setParameter("teamMemberId", pagination.getUserId());
		}
		if (pagination.isDetailedAnalytics()) {
			query.setParameter("partnerCompanyId", pagination.getPartnerCompanyId());
		}
		List<Object[]> folders = query.list();
		List<PartnerDTO> partnerDtos = new ArrayList<>();
		for (Object[] folder : folders) {
			Integer assetId = (Integer) folder[0];
			String assetName = (String) folder[1];
			PartnerDTO partnerDTO = new PartnerDTO();
			partnerDTO.setAssetId(assetId);
			partnerDTO.setAssetName(assetName);
			partnerDtos.add(partnerDTO);
		}
		return partnerDtos;
	}

	@Override
	public List<PartnerDTO> getAllEmailIdsForFilter(Pagination pagination) {
		String queryString = partnerJourneyEmailIds;
		// Integer companyId = userDao.getCompanyIdByUserId(pagination.getUserId());

		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", pagination.getVendorCompanyId());
		query.setParameter("partnerCompanyId", pagination.getPartnerCompanyId());

		List<String> EmailIdList = query.list();
		List<PartnerDTO> partnerDtos = new ArrayList<>();

		for (String emailId : EmailIdList) {
			PartnerDTO partnerDTO = new PartnerDTO();
			partnerDTO.setEmailId(emailId);
			partnerDtos.add(partnerDTO);
		}
		return partnerDtos;
	}

	// XNFR - 989
	@Override
	public Map<String, Object> getAssetJourneyAssetsDetails(Pagination pagination) {
		HashMap<String, Object> map = new HashMap<>();

		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), true);

		String queryString = frameAssetJourneyAssetsDetailsQuery(pagination);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", pagination.getVendorCompanyId());

		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			query.setParameter("teamMemberId", pagination.getUserId());
		}

		if (XamplifyUtils.isNotEmptyList(pagination.getAssetIds())) {
			query.setParameterList("assetList", pagination.getAssetIds());
		}
		return paginationUtil.setScrollableAndGetList(pagination, map, query, PartnerJourneyTrackDetailsDTO.class);
	}

	private String frameAssetJourneyAssetsDetailsQuery(Pagination pagination) {

		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), true);

		String finalQueryString = "";
		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			finalQueryString = assetJourneyAssetDetailsForTeamMemberLogin;
		} else {
			finalQueryString = assetJourneyAssetDetails;
		}

		String searchQueryString = assetJourneyAssetDetailsSearchQuery;
		String searchFilter = "";
		String partnershipStatus = pagination.getPartnershipStatus() != null ? pagination.getPartnershipStatus()
				: "approved";
		finalQueryString = replacePartnershipStatus(finalQueryString, partnershipStatus);
		String searchKey = pagination.getSearchKey();
		boolean hasSortFilter = StringUtils.hasText(pagination.getSortcolumn());
		boolean hasAssetIds = pagination.getAssetIds() != null && !pagination.getAssetIds().isEmpty();
		boolean hasDateFilter = XamplifyUtils.isValidString(pagination.getFromDateFilterString())
				&& XamplifyUtils.isValidString(pagination.getToDateFilterString());

		boolean hasAnyFilter = hasAssetIds || hasDateFilter;
		if (hasAnyFilter) {
			finalQueryString = applyAssetConditions(finalQueryString, pagination);
		}
		if (StringUtils.hasText(searchKey)) {
			searchFilter = searchQueryString.replace("searchKey", searchKey);
			finalQueryString += " " + searchFilter;
		}
		if (XamplifyUtils.isValidString(pagination.getFromDateFilterString())
				&& XamplifyUtils.isValidString(pagination.getToDateFilterString())) {
			finalQueryString = addDateFilterQueryForAssets(finalQueryString, pagination);
		}
		if (hasSortFilter) {
			finalQueryString = applySortOptionCondition(finalQueryString, pagination);
		} else {
			finalQueryString += " ORDER BY a.\"viewedCount\" DESC ";
		}
		return finalQueryString;
	}

	private String applyAssetConditions(String query, Pagination pagination) {
		StringBuilder conditionBuilder = new StringBuilder();

		if (XamplifyUtils.isNotEmptyList(pagination.getAssetIds())) {
			conditionBuilder.append(" AND a.\"assetId\" IN (:assetList) ");
		}
		return query + String.valueOf(conditionBuilder);
	}

	@Override
	public Map<String, Object> getPlaybookJourneyInteractionDetails(Pagination pagination) {
		HashMap<String, Object> map = new HashMap<>();
		String status = pagination.getPartnershipStatus() != null ? pagination.getPartnershipStatus() : "approved";

		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), true);

		String queryString = framePlaybookJourneyInteractionDetailsQuery(pagination);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", pagination.getVendorCompanyId());
		if (pagination.isDetailedAnalytics()) {
			query.setParameter("partnerCompanyId", pagination.getPartnerCompanyId());
		}
		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			query.setParameter("teamMemberId", pagination.getUserId());
		}
		if (pagination.getTeamMemberId() != null && pagination.getTeamMemberId() > 0) {
			query.setParameter("teamMemberUserId", pagination.getTeamMemberId());
		}
		if (XamplifyUtils.isNotEmptyList(pagination.getSelectedPlaybookNames())) {
			query.setParameterList("playbookList", pagination.getSelectedPlaybookNames());
		}
		return paginationUtil.setScrollableAndGetList(pagination, map, query, PartnerJourneyTrackDetailsDTO.class);
	}

	private String framePlaybookJourneyInteractionDetailsQuery(Pagination pagination) {

		String partnershipStatus = pagination.getPartnershipStatus() != null ? pagination.getPartnershipStatus()
				: "approved";

		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), true);

		String finalQueryString = "";
		String searchQueryString = "";
		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			finalQueryString = playbookJourneyInteractionQueryForTeamMember;
			finalQueryString = replacePartnershipStatus(finalQueryString, partnershipStatus);
			searchQueryString = " and (a.\"playbookName\" ILIKE CONCAT('%searchKey%') OR a.\"createdByName\" ILIKE CONCAT('%searchKey%'))";
		} else if (pagination.isDetailedAnalytics()) {
			finalQueryString = playbookJourneyDetailsAnalytics;
			searchQueryString = " and (a.\"firstName\" ILIKE CONCAT('%searchKey%') OR a.\"emailId\" ILIKE CONCAT('%searchKey%') OR a.\"playbookName\" ILIKE CONCAT('%searchKey%') OR a.\"createdByName\" ILIKE CONCAT('%searchKey%'))";
			if (pagination.getTeamMemberId() != null && pagination.getTeamMemberId() > 0) {
				finalQueryString = finalQueryString.replace("{teamMemberFilterQuery}",
						" and xup1.user_id = :teamMemberUserId ");
			} else {
				finalQueryString = finalQueryString.replace("{teamMemberFilterQuery}", "");
			}
		} else {
			finalQueryString = playbookJourneyInteractionQuery;
			finalQueryString = replacePartnershipStatus(finalQueryString, partnershipStatus);
			searchQueryString = " and (a.\"playbookName\" ILIKE CONCAT('%searchKey%') OR a.\"createdByName\" ILIKE CONCAT('%searchKey%'))";
		}

		String searchFilter = "";

		String searchKey = pagination.getSearchKey();
		boolean hasSortFilter = StringUtils.hasText(pagination.getSortcolumn());
		boolean hasPlaybookNames = pagination.getSelectedPlaybookNames() != null
				&& !pagination.getSelectedPlaybookNames().isEmpty();

		if (XamplifyUtils.isValidString(pagination.getFromDateFilterString())
				&& XamplifyUtils.isValidString(pagination.getToDateFilterString())) {
			finalQueryString = addDateFilterQueryForAssets(finalQueryString, pagination);
		}

		boolean hasAnyFilter = hasPlaybookNames;
		if (hasAnyFilter) {
			finalQueryString = applyPlaybookConditions(finalQueryString, pagination);
		}
		if (StringUtils.hasText(searchKey)) {
			searchFilter = searchQueryString.replace("searchKey", searchKey);
			finalQueryString += " " + searchFilter;
		}
		if (hasSortFilter) {
			if (pagination.isDetailedAnalytics()) {
				finalQueryString = sortOptionConditionForPlaybookDetails(finalQueryString, pagination);
			} else {
				finalQueryString = sortOptionConditionForPlaybooks(finalQueryString, pagination);
			}
		} else {
			if (pagination.isDetailedAnalytics()) {
				finalQueryString += " ORDER BY a.\"progress\" DESC nulls last";
			} else {
				finalQueryString += " ORDER BY a.\"viewedCount\" DESC ";
			}
		}
		return finalQueryString;
	}

	private String applyPlaybookConditions(String query, Pagination pagination) {
		StringBuilder conditionBuilder = new StringBuilder();

		if (XamplifyUtils.isNotEmptyList(pagination.getSelectedPlaybookNames())) {
			conditionBuilder.append(" AND a.\"playbookName\" IN (:playbookList) ");
		}

		// String dateFilterQuery = addDateFilterQuery(pagination);
		// conditionBuilder.append(dateFilterQuery);

		return query + String.valueOf(conditionBuilder);
	}

	private String sortOptionConditionForPlaybookDetails(String query, Pagination pagination) {
		StringBuilder updatedQuery = new StringBuilder(query);
		String sortColumn = pagination.getSortcolumn();
		String sortOrder = pagination.getSortingOrder();

		if (XamplifyUtils.isValidString(sortColumn) && XamplifyUtils.isValidString(sortOrder)
				&& !"null".equalsIgnoreCase(sortColumn)) {
			if ("progress".equalsIgnoreCase(sortColumn) && "Asc".equalsIgnoreCase(sortOrder)) {
				updatedQuery.append(" ORDER BY a.\"progress\" ASC nulls first");
			} else if ("progress".equalsIgnoreCase(sortColumn) && "Desc".equalsIgnoreCase(sortOrder)) {
				updatedQuery.append(" ORDER BY a.\"progress\" DESC nulls last");
			}
		} else {
			updatedQuery.append(" ORDER BY a.\"progress\" DESC nulls last");
		}
		return String.valueOf(updatedQuery);
	}

	private String sortOptionConditionForPlaybooks(String query, Pagination pagination) {
		StringBuilder updatedQuery = new StringBuilder(query);
		String sortColumn = pagination.getSortcolumn();
		String sortOrder = pagination.getSortingOrder();

		if (XamplifyUtils.isValidString(sortColumn) && XamplifyUtils.isValidString(sortOrder)
				&& !"null".equalsIgnoreCase(sortColumn)) {
			if ("viewCount".equalsIgnoreCase(sortColumn) && "Asc".equalsIgnoreCase(sortOrder)) {
				updatedQuery.append(" ORDER BY a.\"viewedCount\" ASC");
			} else if ("viewCount".equalsIgnoreCase(sortColumn) && "Desc".equalsIgnoreCase(sortOrder)) {
				updatedQuery.append(" ORDER BY a.\"viewedCount\" DESC");
			} else if ("completedCount".equalsIgnoreCase(sortColumn) && "Asc".equalsIgnoreCase(sortOrder)) {
				updatedQuery.append(" ORDER BY a.\"completedCount\" ASC");
			} else if ("completedCount".equalsIgnoreCase(sortColumn) && "Desc".equalsIgnoreCase(sortOrder)) {
				updatedQuery.append(" ORDER BY a.\"completedCount\" DESC");
			}
		} else {
			updatedQuery.append(" ORDER BY a.\"viewedCount\" DESC ");
		}
		return String.valueOf(updatedQuery);
	}

	@Override
	public List<PartnerDTO> getAllPlaybookNamesForFilter(Pagination pagination) {
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), true);

		String partnershipStatus = pagination.getPartnershipStatus() != null ? pagination.getPartnershipStatus()
				: "approved";

		boolean isTeamMemberContext = teamMemberFilterDTO.isApplyTeamMemberFilter()
				|| teamMemberFilterDTO.isEmptyFilter();
		String finalQuery = "";

		if (isTeamMemberContext) {
			finalQuery = "SELECT DISTINCT xlt.title AS \"playbookName\" FROM xt_learning_track xlt "
					+ "LEFT JOIN xt_learning_track_visibility xltv ON xltv.learning_track_id = xlt.id "
					+ "LEFT JOIN xt_user_profile xup ON xup.user_id = xltv.user_id WHERE xlt.company_id = :vendorCompanyId "
					+ "AND xlt.type = 'PLAYBOOK' AND xlt.is_published = true AND xup.company_id IN ( "
					+ "SELECT DISTINCT p.partner_company_id FROM xt_team_member t "
					+ "LEFT JOIN xt_team_member_group_user_mapping tgum ON t.id = tgum.team_member_id "
					+ "LEFT JOIN xt_partner_team_group_mapping ptgm ON tgum.id = ptgm.team_member_group_user_mapping_id "
					+ "LEFT JOIN xt_partnership p ON ptgm.partnership_id = p.id "
					+ "WHERE t.team_member_id = :teamMemberId "
					+ "AND p.partner_id IS NOT NULL AND p.status = {status} )";
			finalQuery = replacePartnershipStatus(finalQuery, partnershipStatus);

		} else if (pagination.isDetailedAnalytics()) {
			finalQuery = "SELECT DISTINCT xlt.title AS \"playbookName\" " + "FROM xt_learning_track xlt "
					+ "left join xt_learning_track_visibility xltv on xltv.learning_track_id= xlt.id "
					+ "left join xt_user_profile xup on xup.user_id= xltv.user_id "
					+ "WHERE xlt.company_id = :vendorCompanyId " + "and xup.company_id= :partnerCompanyId "
					+ "AND xlt.type = 'PLAYBOOK' " + "AND xlt.is_published = true ";

		} else {
			finalQuery = "SELECT DISTINCT xlt.title AS \"playbookName\" FROM xt_learning_track xlt "
					+ " left join xt_learning_track_visibility xltv on xltv.learning_track_id = xlt.id "
					+ " left join xt_partnership xp on xp.id= xltv.partnership_id  and xp.vendor_company_id= :vendorCompanyId "
					+ "WHERE xlt.company_id = :vendorCompanyId AND xlt.type = 'PLAYBOOK' AND xlt.is_published = true and xp.status={status} ";
			finalQuery = replacePartnershipStatus(finalQuery, partnershipStatus);
		}
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(finalQuery);
		query.setParameter("vendorCompanyId", pagination.getVendorCompanyId());

		if (isTeamMemberContext) {
			query.setParameter("teamMemberId", pagination.getUserId());
		}
		if (pagination.isDetailedAnalytics()) {
			query.setParameter("partnerCompanyId", pagination.getPartnerCompanyId());
		}
		@SuppressWarnings("unchecked")
		List<String> playbookNamesList = query.list();
		List<PartnerDTO> partnerDtos = new ArrayList<>();

		for (String playbook : playbookNamesList) {
			PartnerDTO partnerDTO = new PartnerDTO();
			partnerDTO.setPlaybook(playbook);
			partnerDtos.add(partnerDTO);
		}
		return partnerDtos;
	}

	@Override
	public Map<String, Object> findTotalDeactivatePartnersCount(Integer companyId, boolean applyFilter,
			Integer userId) {
		Map<String, Object> map = new HashMap<>();
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(userId, applyFilter, false);
		String queryString = " select count(distinct partner_id) from xt_partnership where vendor_company_id= :companyId and status='deactivated'";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId));
		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			List<Integer> filteredPartnersIdsList = teamMemberIds(applyFilter, userId);
			queryString += " and partner_id in(:userListId) ";
			hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs()
					.add(new QueryParameterListDTO(XamplifyConstants.USER_LIST_ID, filteredPartnersIdsList));
		}
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		BigInteger totalPartnersCount = (BigInteger) hibernateSQLQueryResultUtilDao
				.getUniqueResult(hibernateSQLQueryResultRequestDTO);
		map.put("totalDeactivatePartnersCount", totalPartnersCount);
		return map;
	}

	@Override
	public Map<String, Object> getPartnerJourneyAssetInteractionDetails(Pagination pagination) {
		HashMap<String, Object> map = new HashMap<>();
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), true);
		String queryString = framePartnerJourneyAssetsInteractionDetailsQuery(pagination, teamMemberFilterDTO);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", pagination.getVendorCompanyId());

		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			query.setParameter("teamMemberId", pagination.getTeamMemberId());
		}

		bindQueryParametersForAssetsDetails(query, pagination, teamMemberFilterDTO);
		return paginationUtil.setScrollableAndGetList(pagination, map, query, PartnerJourneyTrackDetailsDTO.class);
	}

	private void bindQueryParametersForAssetsDetails(SQLQuery query, Pagination pagination,
			TeamMemberFilterDTO teamMemberFilterDTO) {
		if (StringUtils.hasText(pagination.getSearchKey())) {
			query.setParameter("searchKey", "%" + pagination.getSearchKey() + "%");
		}
		if (XamplifyUtils.isNotEmptyList(pagination.getAssetIds())) {
			query.setParameterList("assetList", pagination.getAssetIds());
		}
		if (XamplifyUtils.isNotEmptyList(pagination.getSelectedCompanyIds())) {
			query.setParameterList("companyList", pagination.getSelectedCompanyIds());
		}
	}

	private String applyAssetIdCondition(String query, Pagination pagination) {
		StringBuilder conditionBuilder = new StringBuilder();
		if (XamplifyUtils.isNotEmptyList(pagination.getAssetIds())) {
			conditionBuilder.append(" AND a.\"assetId\" IN (:assetList) ");
		}
		if (XamplifyUtils.isNotEmptyList(pagination.getSelectedCompanyIds())) {
			conditionBuilder.append(" AND a.\"companyId\" IN (:companyList) ");
		}
		return query + String.valueOf(conditionBuilder);
	}

	private String framePartnerJourneyAssetsInteractionDetailsQuery(Pagination pagination,
			TeamMemberFilterDTO teamMemberFilterDTO) {
		String partnershipStatus = pagination.getPartnershipStatus() != null ? pagination.getPartnershipStatus()
				: "approved";
		String finalQueryString = "";
		String searchQueryString = "";
		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			finalQueryString = partnerJourneyAssetInteractionDetailsForTeamMemberLogin;
			finalQueryString = replacePartnershipStatus(finalQueryString, partnershipStatus);
		} else {
			finalQueryString = partnerJourneyAssetInteractionDetailsViewAndDetailedCountQuery;
			finalQueryString = replacePartnershipStatus(finalQueryString, partnershipStatus);
		}

		boolean hasSearchKey = StringUtils.hasText(pagination.getSearchKey());
		boolean hasSortFilter = StringUtils.hasText(pagination.getSortcolumn());

		searchQueryString = assetDetailsSearchQuery;

		finalQueryString = applyAssetIdCondition(finalQueryString, pagination);

		if (XamplifyUtils.isValidString(pagination.getFilterFromDateString())
				&& XamplifyUtils.isValidString(pagination.getFilterToDateString())) {
			finalQueryString = addDateFilterQueryForPartnerAssets(finalQueryString, pagination);
		}

		if (hasSearchKey) {
			finalQueryString += searchQueryString;
		}

		finalQueryString = hasSortFilter ? applySortOptionCondition(finalQueryString, pagination)
				: finalQueryString + "  ORDER BY a.\"viewedCount\" DESC";

		String dateFilterQuery = frameDateFilterQuery(pagination, "xd.published_time");
		finalQueryString = finalQueryString.replace("{dateFilter}", dateFilterQuery);

		return finalQueryString;
	}

	@Override
	public Map<String, Object> getTotalPartnerInteractionDetails(Pagination pagination) {
		HashMap<String, Object> map = new HashMap<>();

		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), true);
		String queryString = frameTotalPartnerInteractionDetailsQuery(pagination);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", pagination.getVendorCompanyId());
		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			query.setParameter("teamMemberId", pagination.getUserId());
		}
		if (XamplifyUtils.isNotEmptyList(pagination.getSelectedPlaybookNames())) {
			query.setParameterList("playbookList", pagination.getSelectedPlaybookNames());
		}
		return paginationUtil.setScrollableAndGetList(pagination, map, query, PartnerJourneyTrackDetailsDTO.class);
	}

	private String frameTotalPartnerInteractionDetailsQuery(Pagination pagination) {
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), true);
		String partnershipStatus = pagination.getPartnershipStatus() != null ? pagination.getPartnershipStatus()
				: "approved";
		String finalQueryString = "";
		String searchQueryString = "";
		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			finalQueryString = totalPartnerTeamMemberInteractionQuery;
			searchQueryString = " and (a.\"playbookName\" ILIKE CONCAT('%searchKey%') OR a.\"createdByName\" ILIKE CONCAT('%searchKey%'))";
			finalQueryString = replacePartnershipStatus(finalQueryString, partnershipStatus);
		} else {
			finalQueryString = totalPartnerInteractionQuery;
			searchQueryString = " and (a.\"playbookName\" ILIKE CONCAT('%searchKey%') OR a.\"createdByName\" ILIKE CONCAT('%searchKey%'))";
			finalQueryString = replacePartnershipStatus(finalQueryString, partnershipStatus);

		}
		String searchFilter = "";

		String searchKey = pagination.getSearchKey();
		boolean hasSortFilter = StringUtils.hasText(pagination.getSortcolumn());
		boolean hasPlaybookNames = pagination.getSelectedPlaybookNames() != null
				&& !pagination.getSelectedPlaybookNames().isEmpty();

		if (XamplifyUtils.isValidString(pagination.getFromDateFilterString())
				&& XamplifyUtils.isValidString(pagination.getToDateFilterString())) {
			finalQueryString = addDateFilterQueryForAssets(finalQueryString, pagination);
		}

		boolean hasAnyFilter = hasPlaybookNames;
		if (hasAnyFilter) {
			finalQueryString = applyPlaybookConditions(finalQueryString, pagination);
		}
		if (StringUtils.hasText(searchKey)) {
			searchFilter = searchQueryString.replace("searchKey", searchKey);
			finalQueryString += " " + searchFilter;
		}
		if (hasSortFilter) {
			finalQueryString = sortOptionConditionForPlaybooks(finalQueryString, pagination);
		} else {
			finalQueryString += " ORDER BY a.\"viewedCount\" DESC ";
		}
		return finalQueryString;
	}

	private String replacePartnershipStatus(String query, String partnershipStatus) {
		String status = StringUtils.hasText(partnershipStatus) ? partnershipStatus : "approved";
		if (status.contains(",")) {
			String inClause = Arrays.stream(status.split(",")).map(String::trim).filter(StringUtils::hasText)
					.map(s -> "'" + s + "'").collect(Collectors.joining(","));
			query = query.replace("= {status}", " in (" + inClause + ")").replace("={status}", " in (" + inClause + ")")
					.replace("{status}", inClause);
		} else {
			query = query.replace("{status}", "'" + status + "'");
		}
		return query;
	}

	@Override
	public Map<String, Object> getPartnerAssetDetailsInteraction(Pagination pagination) {
		HashMap<String, Object> map = new HashMap<>();

		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), true);

		String queryString = frameAssetsInteractionsDetailsQuery(pagination);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", pagination.getVendorCompanyId());

		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			query.setParameter("teamMemberId", pagination.getUserId());
		}

		if (XamplifyUtils.isNotEmptyList(pagination.getAssetIds())) {
			query.setParameterList("assetList", pagination.getAssetIds());
		}
		return paginationUtil.setScrollableAndGetList(pagination, map, query, PartnerJourneyTrackDetailsDTO.class);
	}

	private String frameAssetsInteractionsDetailsQuery(Pagination pagination) {

		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), true);

		String finalQueryString = "";
		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			finalQueryString = assetInteractionDetailsForTeamMember;
		} else {
			finalQueryString = partnerAssetInteractionDetailsQuery;
		}

		String searchQueryString = assetJourneyAssetDetailsSearchQuery;
		String searchFilter = "";
		String partnershipStatus = pagination.getPartnershipStatus() != null ? pagination.getPartnershipStatus()
				: "approved";
		finalQueryString = replacePartnershipStatus(finalQueryString, partnershipStatus);
		String searchKey = pagination.getSearchKey();
		boolean hasSortFilter = StringUtils.hasText(pagination.getSortcolumn());
		boolean hasAssetIds = pagination.getAssetIds() != null && !pagination.getAssetIds().isEmpty();
		boolean hasDateFilter = XamplifyUtils.isValidString(pagination.getFromDateFilterString())
				&& XamplifyUtils.isValidString(pagination.getToDateFilterString());

		boolean hasAnyFilter = hasAssetIds || hasDateFilter;
		if (hasAnyFilter) {
			finalQueryString = applyAssetConditions(finalQueryString, pagination);
		}
		if (StringUtils.hasText(searchKey)) {
			searchFilter = searchQueryString.replace("searchKey", searchKey);
			finalQueryString += " " + searchFilter;
		}
		if (XamplifyUtils.isValidString(pagination.getFromDateFilterString())
				&& XamplifyUtils.isValidString(pagination.getToDateFilterString())) {
			finalQueryString = addDateFilterQueryForAssets(finalQueryString, pagination);
		}
		if (hasSortFilter) {
			finalQueryString = applySortOptionCondition(finalQueryString, pagination);
		} else {
			finalQueryString += " ORDER BY a.\"viewedCount\" DESC ";
		}
		return finalQueryString;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PartnerJourneyTrackDetailsDTO> getPlaybookInteractionDetailsForGroupOfPartners(Integer userListId,
			Integer vendorCompanyId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String queryString = "select a.* from (select distinct xlt.id as \"id\", xup1.company_id as \"partnerCompanyId\",  "
				+ " case when xup1.firstname is null and xup1.lastname is null then xup1.email_id "
				+ " else concat(xup1.firstname, ' ', xup1.lastname) end as \"firstName\", xup1.email_id "
				+ " as \"emailId\", xlt.title as \"playbookName\", xltv.published_on as \"publishedOn\", "
				+ " concat(xup.firstname, ' ', xup.lastname) as \"createdByName\", xltv.progress as \"progress\" , "
				+ " max( case when xltcp.type='VIEWED' then xltcp.created_time end)  as \"viewedTime\", "
				+ " max( case when xltcp.type='DOWNLOADED' then xltcp.created_time end) as \"downloadedTime\" "
				+ " from xt_learning_track_visibility xltv left join xt_learning_track xlt "
				+ " on xltv.learning_track_id = xlt.id left join xt_learning_track_content xltc "
				+ " on xltc.learning_track_id = xlt.id left join xt_learning_track_content_partner_activity "
				+ " xltcp on xltcp.learning_track_visibility_id = xltv.id "
				+ " and xltcp.learning_track_content_id = xltc.id left join xt_user_profile xup "
				+ " on xup.user_id = xlt.created_by left join xt_user_profile xup1 "
				+ " on xup1.user_id = xltv.user_id left join xt_company_profile xcp "
				+ " on xcp.company_id = xup1.company_id join (SELECT DISTINCT up.company_id "
				+ " FROM xt_user_list ul  JOIN xt_user_userlist uul ON uul.user_list_id = ul.user_list_id "
				+ " JOIN xt_user_profile up ON up.user_id = uul.user_id  WHERE ul.module_name = 'PARTNERS' "
				+ " AND ul.user_list_id = :userListId  AND up.company_id IS NOT NULL) sk ON sk.company_id = xup1.company_id "
				+ " where xlt.company_id = :vendorCompanyId and xlt.type = 'PLAYBOOK' "
				+ " and xlt.is_published = true and xcp.company_id is not null group by 1,2,3,4,5,6,7,8) a "
				+ "where 1 = 1  ORDER BY a.\"progress\" DESC nulls last";
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(USER_LIST_ID, userListId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("vendorCompanyId", vendorCompanyId));
		return (List<PartnerJourneyTrackDetailsDTO>) hibernateSQLQueryResultUtilDao
				.getListDto(hibernateSQLQueryResultRequestDTO, PartnerJourneyTrackDetailsDTO.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PartnerJourneyTrackDetailsDTO> getTrackDetailsForGroupOfPartners(Integer userListId,
			Integer vendorCompanyId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String queryString = "with a as (select distinct xlt.id as \"id1\", xcp.company_id as \"companyId\", xcp.company_name "
				+ " as \"companyName\", xup.email_id as \"emailId\", xup.user_id as \"tuid\", xcp.company_id, "
				+ " xlt.title as \"title\", xlt.published_time as \"publishedOn\", coalesce(xltv.progress, 0)"
				+ " as \"progress\" from xt_learning_track xlt left join xt_learning_track_visibility xltv "
				+ " on xltv.learning_track_id=xlt.id left join xt_user_profile xup on xup.user_id=xltv.user_id  "
				+ " left join xt_company_profile xcp on xcp.company_id=xup.company_id "
				+ " join (SELECT DISTINCT up.company_id FROM xt_user_list ul JOIN xt_user_userlist uul ON uul.user_list_id = ul.user_list_id "
				+ " JOIN xt_user_profile up ON up.user_id = uul.user_id WHERE ul.module_name = 'PARTNERS' "
				+ " AND ul.user_list_id = :userListId AND up.company_id IS NOT NULL) sk "
				+ " ON sk.company_id = xcp.company_id where xlt.company_id= :vendorCompanyId "
				+ " and xlt.type='TRACK' and xlt.is_published= true " + " and xcp.company_name_status ='active'  ), "
				+ " b as (select distinct xlt.id, xup1.user_id as \"auid\", "
				+ " count(case when xltcp.type='OPENED' then xd.id end ) as \"openedCount\", "
				+ " count(case when xltcp.type='VIEWED' then xd.id end ) as \"viewedCount\", "
				+ " count(case when xltcp.type='DOWNLOADED' then xd.id end ) as \"downloadedCount\", "
				+ " count(distinct xd.id) as \"assetCount\", coalesce(count(distinct xltc.quiz_id), 0) "
				+ " as \"quizCount\" from xt_learning_track_visibility xltv left join xt_learning_track xlt "
				+ " on xltv.learning_track_id=xlt.id left join xt_learning_track_content xltc "
				+ " on xltc.learning_track_id=xlt.id left join xt_learning_track_content_partner_activity xltcp "
				+ " on xltcp.learning_track_visibility_id=xltv.id and xltcp.learning_track_content_id =xltc.id "
				+ " left join xt_dam xd on xd.id = xltc.dam_id left join xt_user_profile xup1 "
				+ " on xup1.user_id=xltv.user_id join (SELECT DISTINCT up.company_id " + " FROM   xt_user_list ul "
				+ " JOIN   xt_user_userlist uul ON uul.user_list_id = ul.user_list_id "
				+ " JOIN   xt_user_profile up ON up.user_id = uul.user_id " + " WHERE  ul.module_name = 'PARTNERS' "
				+ " AND ul.user_list_id = :userListId AND up.company_id IS NOT NULL) sk "
				+ " ON sk.company_id = xup1.company_id where xlt.company_id= :vendorCompanyId "
				+ " and xlt.type='TRACK' and xlt.is_published= true group by 1,2), "
				+ " c as (select distinct xlt.id as \"qid\", xup1.user_id, xfs.score as score, xf.max_score as "
				+ " max_score, xfs.submitted_on as \"Submitted\" from xt_learning_track_visibility xltv "
				+ " left join xt_learning_track xlt on xltv.learning_track_id=xlt.id "
				+ " left join xt_learning_track_content xltc on xltc.learning_track_id=xlt.id "
				+ " left join xt_user_profile xup1 on xup1.user_id=xltv.user_id left join xt_form xf "
				+ " on xltc.quiz_id= xf.id left join xt_form_submit xfs on xf.id=xfs.form_id "
				+ " and xfs.user_id =xup1.user_id " + " join (SELECT DISTINCT up.company_id FROM xt_user_list ul "
				+ " JOIN xt_user_userlist uul ON uul.user_list_id = ul.user_list_id JOIN xt_user_profile up ON up.user_id = uul.user_id "
				+ " WHERE ul.module_name = 'PARTNERS' AND ul.user_list_id = :userListId AND up.company_id IS NOT NULL) sk "
				+ " ON sk.company_id = xup1.company_id where xlt.company_id= :vendorCompanyId and xlt.type='TRACK' "
				+ " and xlt.is_published= true and xfs.form_submit_type='LMS_FORM' "
				+ " group by 1,2,3,4,5) select distinct a.\"companyId\", "
				+ " a.\"companyName\", a.\"emailId\", a.\"title\", a.\"publishedOn\", b.\"openedCount\", "
				+ " b.\"viewedCount\", b.\"downloadedCount\", a.\"progress\", b.\"assetCount\", b.\"quizCount\", "
				+ " max(case when \"Submitted\" is not null then coalesce(c.score, 0) || '  out of  ' || "
				+ " coalesce(c.max_score, 0) else coalesce(c.score, 0) || '  out of  ' || "
				+ " coalesce(c.max_score, 0) end) as \"score\" from a left join b on a.\"id1\"=b.id "
				+ " and a.\"tuid\"=b.\"auid\" "
				+ "left join c on c.\"qid\"=a.\"id1\" and a.\"tuid\"=c.user_id group by 1,2,3,4,5,6,7,8,9,10,11";
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(USER_LIST_ID, userListId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("vendorCompanyId", vendorCompanyId));
		return (List<PartnerJourneyTrackDetailsDTO>) hibernateSQLQueryResultUtilDao
				.getListDto(hibernateSQLQueryResultRequestDTO, PartnerJourneyTrackDetailsDTO.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PartnerJourneyTrackDetailsDTO> getAssetDetailsForGroupOfPartners(Integer userListId,
			Integer vendorCompanyId) {
		HibernateSQLQueryResultRequestDTO queryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String queryString = "select a.* from "
				+ "(select distinct xup1.firstname as \"firstName\", xup1.lastname as \"lastName\", "
				+ " xup1.email_id as \"emailId\" , xd.asset_name as \"assetName\", xd.id as \"assetId\", xd.asset_type as "
				+ " \"assetType\",xdp.dam_id as \"id\",xdpm.dam_partner_id as \"partnerId\",case when xup.firstname is null and "
				+ " xup.lastname  is null then xup.email_id else concat(xup.firstname, ' ' , xup.lastname) end as "
				+ " \"createdByName\",max(xdp.published_time) as \"publishedOn\", "
				+ " max( case when xda.action_type= 'VIEW' then xda.action_time end ) as \"viewedTime\", "
				+ " max( case when xda.action_type= 'DOWNLOAD' then xda.action_time end ) as \"downloadedTime\", "
				+ " count(distinct case when xda.action_type= 'VIEW' then xda.id end ) as \"viewedCount\", "
				+ " count(distinct case when xda.action_type= 'DOWNLOAD' then xda.id end ) as \"downloadedCount\" "
				+ " from xt_dam_partner xdp left join xt_dam xd on xd.id = xdp.dam_id left join xt_dam_partner_mapping xdpm "
				+ " on xdpm.dam_partner_id = xdp.id left join xt_dam_analytics xda on xda.dam_partner_mapping_fk_id = xdpm.id "
				+ " left join xt_user_profile xup1 on xup1.user_id = xdpm.partner_id left join xt_user_profile xup "
				+ " on xup.user_id = xd.created_by " + " join (SELECT DISTINCT up.company_id "
				+ " FROM xt_user_list ul " + " JOIN xt_user_userlist uul ON uul.user_list_id = ul.user_list_id "
				+ " JOIN xt_user_profile up   ON up.user_id = uul.user_id " + " WHERE ul.module_name = 'PARTNERS' "
				+ " AND ul.user_list_id = :userListId " + " AND up.company_id IS NOT NULL) sk "
				+ " ON sk.company_id = xup1.company_id " + " where xd.company_id = :vendorCompanyId "
				+ " group by 1,2,3,4,5,6,7,8,9 union select distinct xup1.firstname "
				+ " as \"firstName\",xup1.lastname as \"lastName\",xup1.email_id as \"emailId\", xd.asset_name as \"assetName\", "
				+ " xd.id as \"assetId\", xd.asset_type as \"assetType\", xdp.dam_id as \"id\",dpgm.dam_partner_id as \"partnerId\", "
				+ " case when xup.firstname is null and  xup.lastname  is null then xup.email_id else \r\n"
				+ " concat(xup.firstname, ' ' , xup.lastname) end as \"createdByName\",max(xdp.published_time) as \"publishedOn\", "
				+ " max( case when xda.action_type= 'VIEW' then xda.action_time end ) as \"viewedTime\", "
				+ " max( case when xda.action_type= 'DOWNLOAD' then xda.action_time end ) as \"downloadedTime\", "
				+ " count(distinct case when xda.action_type= 'VIEW' then xda.id end ) as \"viewedCount\", "
				+ " count(distinct case when xda.action_type= 'DOWNLOAD' then xda.id end ) as \"downloadedCount\" "
				+ " from xt_dam_partner xdp left join xt_dam xd on xdp.dam_id = xd.id "
				+ " left join xt_dam_partner_group_mapping dpgm on xdp.id = dpgm.dam_partner_id "
				+ " left join xt_dam_analytics xda on xda.dam_partner_fk_id = xdp.id left join xt_user_profile xup1 "
				+ " on xup1.user_id = dpgm.user_id left join xt_user_profile xup on xup.user_id = xd.created_by "
				+ " join (SELECT DISTINCT up.company_id " + " FROM xt_user_list ul "
				+ " JOIN xt_user_userlist uul ON uul.user_list_id = ul.user_list_id "
				+ " JOIN xt_user_profile up ON up.user_id = uul.user_id " + " WHERE ul.module_name = 'PARTNERS' "
				+ " AND ul.user_list_id = :userListId " + " AND up.company_id IS NOT NULL) sk "
				+ " ON sk.company_id = xup1.company_id " + " where xd.company_id = :vendorCompanyId "
				+ " group by 1,2,3,4,5,6,7,8,9) a " + "where 1 = 1   ORDER BY a.\"viewedCount\" DESC";
		queryResultRequestDTO.setQueryString(queryString);
		queryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(USER_LIST_ID, userListId));
		queryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("vendorCompanyId", vendorCompanyId));
		return (List<PartnerJourneyTrackDetailsDTO>) hibernateSQLQueryResultUtilDao.getListDto(queryResultRequestDTO,
				PartnerJourneyTrackDetailsDTO.class);
	}

}