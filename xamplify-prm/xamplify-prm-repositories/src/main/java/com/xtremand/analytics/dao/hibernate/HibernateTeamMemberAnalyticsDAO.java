package com.xtremand.analytics.dao.hibernate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.xtremand.analytics.dao.TeamMemberAnalyticsDAO;
import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.Pagination;
import com.xtremand.deal.dto.DealDto;
import com.xtremand.lead.dto.LeadDto;
import com.xtremand.mdf.dto.MdfAmountTilesDTO;
import com.xtremand.mdf.dto.MdfRequestDeatailsDTO;
import com.xtremand.team.member.dto.TeamMemberDTO;
import com.xtremand.user.bom.ShareLeadsDTO;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.user.list.dto.ContactsDetailsDTO;
import com.xtremand.util.PaginationUtil;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.dto.AllPartnersDetailsDTO;
import com.xtremand.util.dto.PartnerJourneyTrackDetailsDTO;
import com.xtremand.util.dto.TeamMemberAnalyticsDTO;
import com.xtremand.util.dto.TeamMemberAnalyticsRequestDTO;
import com.xtremand.vanity.url.dao.VanityURLDao;
import com.xtremand.vendor.bom.VendorDTO;

@Repository("teamMemberAnalyticsDAO")
@Transactional
public class HibernateTeamMemberAnalyticsDAO implements TeamMemberAnalyticsDAO {

	@Autowired
	SessionFactory sessionFactory;

	@Autowired
	private PaginationUtil paginationUtil;

        @Autowired
        private UserDAO userDao;

        @Autowired
        private VanityURLDao vanityURLDao;

        @Autowired
        private UtilDao utilDao;

	@Value("${allRedistributedCampaignCountQueryForTeamMember}")
	private String allRedistributedCampaignCountQueryForTeamMember;

	@Value("${allShareLeadCountQueryForTeamMember}")
	private String allShareLeadCountQueryForTeamMember;

	@Value("${allLeadCountQueryForTeamMember}")
	private String allLeadCountQueryForTeamMember;

	@Value("${allDealCountQueryForTeamMember}")
	private String allDealCountQueryForTeamMember;

	@Value("${allAssetCountQueryForTeamMember}")
	private String allAssetCountQueryForTeamMember;

	@Value("${allTrackAndPlaybookCountQueryForTeamMember}")
	private String allTrackAndPlaybookCountQueryForTeamMember;

	@Value("${allContactsQuery}")
	private String allContactsQuery;

	@Value("${allViewedAndNotViewedTrackCountsQueryForTeamMember}")
	private String allViewedAndNotViewedTrackCountsQueryForTeamMember;

	@Value("${allViewedAndNotViewedTrackCountsQueryForTeamMemberGroupBy}")
	private String allViewedAndNotViewedTrackCountsQueryForTeamMemberGroupBy;

	@Value("${partnerJourneyInteracted}")
	private String partnerJourneyInteracted;

	@Value("${partnerJourneyNotInteracted}")
	private String partnerJourneyNotInteracted;

	@Value("${partnerJourneyTrackTypeInteractedFilter}")
	private String partnerJourneyTrackTypeInteractedFilter;

	@Value("${partnerJourneyTrackTypeNotInteractedFilter}")
	private String partnerJourneyTrackTypeNotInteractedFilter;

	@Value("${allViewedAndNotViewedTrackDetailsQueryForTeamMember}")
	private String allViewedAndNotViewedTrackDetailsQueryForTeamMember;

	@Value("${allViewedAndNotViewedTrackDetailsSearchConditionsForTeamMember}")
	private String allViewedAndNotViewedTrackDetailsSearchConditionsForTeamMember;

	@Value("${allViewedAndNotViewedTrackDetailsQueryGroupByForTeamMember}")
	private String allViewedAndNotViewedTrackDetailsQueryGroupByForTeamMember;

	@Value("${allTypeWiseTrackCountsQueryForTeamMember}")
	private String allTypeWiseTrackCountsQueryForTeamMember;

	@Value("${partnerJourneyTypeWiseTrackInteractedFilter}")
	private String partnerJourneyTypeWiseTrackInteractedFilter;

	@Value("${partnerJourneyTypeWiseTrackNotInteractedFilter}")
	private String partnerJourneyTypeWiseTrackNotInteractedFilter;

	@Value("${allTypeWiseTrackDetailsQueryForTeamMember}")
	private String allTypeWiseTrackDetailsQueryForTeamMember;

	@Value("${partnerJourneyNotOpened}")
	private String partnerJourneyNotOpened;

	@Value("${allTypeWiseTrackDetailsNotOpenedSearchConditionsForTeamMember}")
	private String allTypeWiseTrackDetailsNotOpenedSearchConditionsForTeamMember;

	@Value("${allTypeWiseTrackDetailsSearchConditionsForTeamMember}")
	private String allTypeWiseTrackDetailsSearchConditionsForTeamMember;

	@Value("${partnerJourneyTypeWiseTrackDetailsAssetFilter}")
	private String partnerJourneyTypeWiseTrackDetailsAssetFilter;

	@Value("${allTracksTeamMemberWiseCountsQuery}")
	private String allTracksTeamMemberWiseCountsQuery;

	@Value("${emailIdSearchCondition}")
	private String emailIdSearchCondition;

	@Value("${allTracksTeamMemberWiseCountsGroupBy}")
	private String allTracksTeamMemberWiseCountsGroupBy;

	@Value("${allPlaybooksTeamMemberWiseCountsQuery}")
	private String allPlaybooksTeamMemberWiseCountsQuery;

	@Value("${allPlayBooksTeamMemberWiseCountsGroupBy}")
	private String allPlayBooksTeamMemberWiseCountsGroupBy;

	@Value("${allTracksUserWiseContentDetailsSearchConditionForTeamMember}")
	private String allTracksUserWiseContentDetailsSearchConditionForTeamMember;

	@Value("${partnerJourneyTrackAssetsSearchCondition}")
	private String partnerJourneyTrackAssetsSearchCondition;

	@Value("${allTrackAssetsQueryForTeamMember}")
	private String allTrackAssetsQueryForTeamMember;

	@Value("${allTrackAssetsGroupByForTeamMember}")
	private String allTrackAssetsGroupByForTeamMember;

	@Value("${allPlaybookAssetsQueryForTeamMember}")
	private String allPlaybookAssetsQueryForTeamMember;

	@Value("${allPlaybookAssetsGroupByForTeamMember}")
	private String allPlaybookAssetsGroupByForTeamMember;

	@Value("${partnerJourneyPlaybookAssetsSearchCondition}")
	private String partnerJourneyPlaybookAssetsSearchCondition;

	@Value("${allShareLeadDetailsQueryForTeamMember}")
	private String allShareLeadDetailsQueryForTeamMember;

	@Value("${allShareLeadDetailsGroupByForTeamMember}")
	private String allShareLeadDetailsGroupByForTeamMember;

	@Value("${allShareLeadDetailsSearchConditionsForTeamMember}")
	private String allShareLeadDetailsSearchConditionsForTeamMember;

	@Value("${allRedistributedCampignDetailsForPieChartForTeamMember}")
	private String allRedistributedCampignDetailsForPieChartForTeamMember;

	@Value("${allRedistributedCampignDetailsForPieChartGroupBy}")
	private String allRedistributedCampignDetailsForPieChartGroupBy;

	@Value("${allRedistributedCampaignDetailsQueryForTeamMember}")
	private String allRedistributedCampaignDetailsQueryForTeamMember;

	@Value("${allRedistributedCampaignDetailsSearchConditionsForTeamMember}")
	private String allRedistributedCampaignDetailsSearchConditionsForTeamMember;

	@Value("${allLeadDetailsQueryForTeamMember}")
	private String allLeadDetailsQueryForTeamMember;

	@Value("${allLeadDetailsSearchConditionsForTeamMember}")
	private String allLeadDetailsSearchConditionsForTeamMember;

	@Value("${allDealDetailsQueryForTeamMember}")
	private String allDealDetailsQueryForTeamMember;

	@Value("${allDealDetailsSearchConditionsForTeamMember}")
	private String allDealDetailsSearchConditionsForTeamMember;

	@Value("${TeamMemberAnalyticsMDFRequetsQuery}")
	private String TeamMemberAnalyticsMDFRequetsQuery;

	@Value("${TeamMemberAnalyticsMDFRequetsQueryGroupBy}")
	private String TeamMemberAnalyticsMDFRequetsQueryGroupBy;

	@Value("${TeamMemberAnalyticsMDFRequetsSearchConditions}")
	private String TeamMemberAnalyticsMDFRequetsSearchConditions;

	@Value("${allPartnersCountForTeamAnalytics}")
	private String allPartnersCountForTeamAnalytics;

	@Value("${launchedCampaignCountForTeamAnalytics}")
	private String launchedCampaignCountForTeamAnalytics;

	@Value("${shareLeadCountForVendorView}")
	private String shareLeadCountForVendorView;

	@Value("${allLeadCountQueryForVendorView}")
	private String allLeadCountQueryForVendorView;

	@Value("${allDealCountQueryForVendorView}")
	private String allDealCountQueryForVendorView;

	@Value("${allAssertsCountQueryForVendorView}")
	private String allAssertsCountQueryForVendorView;

	@Value("${allTrackAndPlaybookCountQueryForVendorView}")
	private String allTrackAndPlaybookCountQueryForVendorView;

	@Value("${allContactsQueryForVendorView}")
	private String allContactsQueryForVendorView;

	@Value("${allViewedAndNotViewedTrackCountsQueryForVendorView}")
	private String allViewedAndNotViewedTrackCountsQueryForVendorView;

	@Value("${allViewedAndNotViewedTrackCountsQueryForVendorViewGroupBy}")
	private String allViewedAndNotViewedTrackCountsQueryForVendorViewGroupBy;

	@Value("${allViewedAndNotViewedTrackDetailsQueryForVendor}")
	private String allViewedAndNotViewedTrackDetailsQueryForVendor;

	@Value("${allTypeWiseTrackCountsQueryGroupByForVendor}")
	private String allTypeWiseTrackCountsQueryGroupByForVendor;

	@Value("${allTypeWiseTrackCountsQueryForVendor}")
	private String allTypeWiseTrackCountsQueryForVendor;

	@Value("${allTypeWiseTrackDetailsQueryForVendor}")
	private String allTypeWiseTrackDetailsQueryForVendor;

	@Value("${allTracksTeamMemberWiseCountsQueryForVendor}")
	private String allTracksTeamMemberWiseCountsQueryForVendor;

	@Value("${allTracksTeamMemberWiseCountsQueryForVendorGroupBy}")
	private String allTracksTeamMemberWiseCountsQueryForVendorGroupBy;

	@Value("${allPlaybooksTeamMemberWiseCountsQueryForVendor}")
	private String allPlaybooksTeamMemberWiseCountsQueryForVendor;

	@Value("${allTrackAssetsQueryForVendor}")
	private String allTrackAssetsQueryForVendor;

	@Value("${allPlaybookAssetsQueryForVendorTeamMember}")
	private String allPlaybookAssetsQueryForVendorTeamMember;

	@Value("${allShareLeadDetailsQueryForVendorTeamMember}")
	private String allShareLeadDetailsQueryForVendorTeamMember;

	@Value("${allShareLeadDetailsGroupByForVendorTeamMember}")
	private String allShareLeadDetailsGroupByForVendorTeamMember;

	@Value("${allLaunchedCampignDetailsForPieChartForVendorTeamMember}")
	private String allLaunchedCampignDetailsForPieChartForVendorTeamMember;

	@Value("${allLaunchedCampaignDetailsQueryForTeamMember}")
	private String allLaunchedCampaignDetailsQueryForTeamMember;

	@Value("${allLaunchedCampaignDetailsQuerySearchConditionsForVendorTeamMember}")
	private String allLaunchedCampaignDetailsQuerySearchConditionsForVendorTeamMember;

	@Value("${allTeamMemberAnalyticsContactDetailsQuery}")
	private String allTeamMemberAnalyticsContactDetailsQuery;

	@Value("${allTeamMemberAnalyticsContactDetailsQueryGroupBy}")
	private String allTeamMemberAnalyticsContactDetailsQueryGroupBy;

	@Value("${allTeamMemberAnalyticsContactDetailsSearchConditions}")
	private String allTeamMemberAnalyticsContactDetailsSearchConditions;

	@Value("${allTeamMemberAnalyticsallPartnersDetailsQuery}")
	private String allTeamMemberAnalyticsallPartnersDetailsQuery;

	@Value("${allTeamMemberAnalyticsallPartnersDetailsSearchConditions}")
	private String allTeamMemberAnalyticsallPartnersDetailsSearchConditions;

	@Value("${redistributedCampaignsCountOrderByQuery}")
	private String redistributedCampaignsCountOrderByQuery;

	@Value("${leadsCountOrderByQuery}")
	private String leadsCountOrderByQuery;

	@Value("${leadsOrderByQuery}")
	private String leadsOrderByQuery;

	@Value("${dealsOrderByQuery}")
	private String dealsOrderByQuery;

	@Value("${allLaunchedCampaignDetailsQueryForTeamMemberOrderBy}")
	private String allLaunchedCampaignDetailsQueryForTeamMemberOrderBy;

	@Value("${allTeamMemberAnalyticsContactDetailsQueryForPartnerTeamMember}")
	private String allTeamMemberAnalyticsContactDetailsQueryForPartnerTeamMember;

	@Value("${allLeadDetailsQueryForVendorTeamMember}")
	private String allLeadDetailsQueryForVendorTeamMember;

	@Value("${allDealDetailsQueryForVendorTeamMember}")
	private String allDealDetailsQueryForVendorTeamMember;

	@Value("${allVendorteamMemberAnalyticsMDFQuery}")
	private String allVendorteamMemberAnalyticsMDFQuery;

	@Value("${allLeadDetailsSearchConditionsForVendorTeamMember}")
	private String allLeadDetailsSearchConditionsForVendorTeamMember;

	@Value("${allDealDetailsSearchConditionsForVendorTeamMember}")
	private String allDealDetailsSearchConditionsForVendorTeamMember;

	@Value("${allVendorteamMemberAssetsCountQuery}")
	private String allVendorteamMemberAssetsCountQuery;

	@Value("${allAssetsCountSearchConditionsForTeamMember}")
	private String allAssetsCountSearchConditionsForTeamMember;

	@Value("${allVendorteamMemberAssetsCountGroupBy}")
	private String allVendorteamMemberAssetsCountGroupBy;

	@Value("${allVendorteamMemberAssetsDetailsQuery}")
	private String allVendorteamMemberAssetsDetailsQuery;

	@Value("${allAssetsDetailsSearchConditionsForTeamMember}")
	private String allAssetsDetailsSearchConditionsForTeamMember;

	@Value("${allCompanyQuery}")
	private String allCompanyQuery;

	@Value("${allTeamMemberAnalyticsCompanyDetailsQuery}")
	private String allTeamMemberAnalyticsCompanyDetailsQuery;

	@Value("${allTeamMemberAnalyticsCompanyDetailsSearchConditions}")
	private String allTeamMemberAnalyticsCompanyDetailsSearchConditions;

	@Value("${allTeamMemberAnalyticsCompanyDetailsQueryGroupBy}")
	private String allTeamMemberAnalyticsCompanyDetailsQueryGroupBy;

	@Value("${shareLeadCountForVendorViewGroupBy}")
	private String shareLeadCountForVendorViewGroupBy;

	@Value("${allTrackAssetsGroupByForVendorTeamMember}")
	private String allTrackAssetsGroupByForVendorTeamMember;

	@Value("${allPlaybookCountsGroupByForVendorTeamMember}")
	private String allPlaybookCountsGroupByForVendorTeamMember;

	@Value("${allViewedAndNotViewedTrackDetailsSearchConditionsForVendorTeamMember}")
	private String allViewedAndNotViewedTrackDetailsSearchConditionsForVendorTeamMember;

	@Value("${allTypeWiseTrackDetailsSearchConditionsForVendorTeamMember}")
	private String allTypeWiseTrackDetailsSearchConditionsForVendorTeamMember;

	@Value("${allTypeWiseTrackDetailsNotOpenedSearchConditionsForVendorTeamMember}")
	private String allTypeWiseTrackDetailsNotOpenedSearchConditionsForVendorTeamMember;

	@Value("${allShareLeadDetailsSearchConditionsForVendorTeamMember}")
	private String allShareLeadDetailsSearchConditionsForVendorTeamMember;

	@Value("${allTracksandPlayBooksCountsQuerySearchCondition}")
	private String allTracksandPlayBooksCountsQuerySearchCondition;

	@Value("${allTeamMemberAnalyticsContactDetailsForPartnerSearchConditions}")
	private String allTeamMemberAnalyticsContactDetailsForPartnerSearchConditions;

	@Value("${allLeadDetailsOrderByVendorTeamMember}")
	private String allLeadDetailsOrderByVendorTeamMember;

	@Value("${allDealDetailsOrderByTeamMember}")
	private String allDealDetailsOrderByTeamMember;

	@Value("${allTrackAssetQueryForTeamMember}")
	private String allTrackAssetQueryForTeamMember;

	@Value("${allTrackAssetQueryForVendorTeamMember}")
	private String allTrackAssetQueryForVendorTeamMember;

	@Value("${allAssetDetailsQueryForTeamMember}")
	private String allAssetDetailsQueryForTeamMember;

	@Value("${allAssetDetailsSearchQuery}")
	private String allAssetDetailsSearchQuery;

	private static final String CAMPAIGN_DATE_COLUMN = "p.launch_time";

	private static final String SHARELEADS_DATE_COLUMN = "sp.created_time";

	private static final String LEAD_DATE_COLUMN = "xl.created_time";

	private static final String DEAL_DATE_COLUMN = "xd.created_time";

	private static final String ASSET_DATE_COLUMN = "xdp.published_time";

	private static final String TRACK_DATE_COLUMN = "xlt.published_time";

	private static final String CONTACT_DATE_COLUMN = "xup.created_time";

	private static final String COMPANY_DATE_COLUMN = "xc.created_time";

	private static final String TRACK_DATE_COLUMN_FOR_VENDOR = "xlt.created_time";

	private static final String CAMPAIGN_DATE_COLUMN_FOR_VENDOR = "c.launch_time";

	private static final String ASSSET_DATE_COLUMN_FOR_VENDOR = "xd.created_time";

	@Override
	public String getTeamMembersRedistributedCampaignCount(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO) {
		String finalQueryString = allRedistributedCampaignCountQueryForTeamMember;

		String dateFilterQueryString = frameDateFilterQuery(teamMemberJourneyRequestDTO, CAMPAIGN_DATE_COLUMN);
		finalQueryString += dateFilterQueryString;

		if (teamMemberJourneyRequestDTO.getSelectedVendorCompanyIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedVendorCompanyIds().isEmpty()) {
			finalQueryString += "and xup1.company_id in (:vendorCompanyIds) ";
		}
		if (teamMemberJourneyRequestDTO.getSelectedTeamMemberIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString += "and xup.user_id in (:teamMemberIds) ";
		}
		if (teamMemberJourneyRequestDTO.isVanityUrlFilter()) {
			finalQueryString += "and xup1.company_id =:vendorCompanyId ";
		}
		return (String) executeUniqueResultQuery(finalQueryString, teamMemberJourneyRequestDTO);
	}

	private Object executeUniqueResultQuery(String finalQueryString,
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(finalQueryString);
		query.setParameter("partnerCompanyId", teamMemberJourneyRequestDTO.getPartnerCompanyId());
		if (teamMemberJourneyRequestDTO.getSelectedVendorCompanyIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedVendorCompanyIds().isEmpty()) {
			query.setParameterList("vendorCompanyIds", teamMemberJourneyRequestDTO.getSelectedVendorCompanyIds());
		}
		if (teamMemberJourneyRequestDTO.getSelectedTeamMemberIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedTeamMemberIds().isEmpty()) {
			query.setParameterList("teamMemberIds", teamMemberJourneyRequestDTO.getSelectedTeamMemberIds());
		}

		if (teamMemberJourneyRequestDTO.isVanityUrlFilter()) {
			CompanyProfile cp = vanityURLDao
					.getCompanyProfileByCompanyProfileName(teamMemberJourneyRequestDTO.getVendorCompanyProfileName());
			Integer companyId = cp.getId();
			if (companyId != null) {
				query.setParameter("vendorCompanyId", companyId);
			}
		}
		return query.uniqueResult();
	}

	@Override
	public String getTeamMembersShareLeadCount(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO) {
		String finalQueryString = allShareLeadCountQueryForTeamMember;

		String dateFilterQueryString = frameDateFilterQuery(teamMemberJourneyRequestDTO, SHARELEADS_DATE_COLUMN);
		finalQueryString += dateFilterQueryString;

		if (teamMemberJourneyRequestDTO.getSelectedVendorCompanyIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedVendorCompanyIds().isEmpty()) {
			finalQueryString += "and xul.assigned_company_id in (:vendorCompanyIds) ";
		}
		if (teamMemberJourneyRequestDTO.getSelectedTeamMemberIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString += "and  xup1.user_id in (:teamMemberIds) ";
		}
		if (teamMemberJourneyRequestDTO.isVanityUrlFilter()) {
			finalQueryString += "and xul.assigned_company_id =:vendorCompanyId ";
		}
		return (String) executeUniqueResultQuery(finalQueryString, teamMemberJourneyRequestDTO);
	}

	@Override
	public String getTeamMembersLeadCount(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO) {
		String finalQueryString = allLeadCountQueryForTeamMember;

		String dateFilterQueryString = frameDateFilterQuery(teamMemberJourneyRequestDTO, LEAD_DATE_COLUMN);
		finalQueryString += dateFilterQueryString;

		if (teamMemberJourneyRequestDTO.getSelectedVendorCompanyIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedVendorCompanyIds().isEmpty()) {
			finalQueryString += "and xl.created_for_company_id in (:vendorCompanyIds) ";
		}
		if (teamMemberJourneyRequestDTO.getSelectedTeamMemberIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString += "and xup.user_id in (:teamMemberIds) ";
		}
		if (teamMemberJourneyRequestDTO.isVanityUrlFilter()) {
			finalQueryString += "and xl.created_for_company_id =:vendorCompanyId ";
		}
		return (String) executeUniqueResultQuery(finalQueryString, teamMemberJourneyRequestDTO);
	}

	@Override
	public String getTeamMembersDealCount(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO) {
		String finalQueryString = allDealCountQueryForTeamMember;

		String dateFilterQueryString = frameDateFilterQuery(teamMemberJourneyRequestDTO, DEAL_DATE_COLUMN);
		finalQueryString += dateFilterQueryString;

		if (teamMemberJourneyRequestDTO.getSelectedVendorCompanyIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedVendorCompanyIds().isEmpty()) {
			finalQueryString += "and xd.created_for_company_id in (:vendorCompanyIds) ";
		}
		if (teamMemberJourneyRequestDTO.getSelectedTeamMemberIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString += "and xup.user_id in (:teamMemberIds) ";
		}
		if (teamMemberJourneyRequestDTO.isVanityUrlFilter()) {
			finalQueryString += "and xd.created_for_company_id =:vendorCompanyId ";
		}
		return (String) executeUniqueResultQuery(finalQueryString, teamMemberJourneyRequestDTO);
	}

	@Override
	public String getTeamMembersAssetCount(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO) {
		String finalQueryString = allAssetCountQueryForTeamMember;
		String teamMemberFilterQuery = "";
		String vendorFilterQuery = "";
		String vanityFilterQuery = "";

		String dateFilterQueryString = frameDateFilterQuery(teamMemberJourneyRequestDTO, ASSET_DATE_COLUMN);

		if (teamMemberJourneyRequestDTO.getSelectedVendorCompanyIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedVendorCompanyIds().isEmpty()) {
			vendorFilterQuery = "and xp.vendor_company_id in (:vendorCompanyIds) ";
		}
		if (teamMemberJourneyRequestDTO.getSelectedTeamMemberIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedTeamMemberIds().isEmpty()) {
			teamMemberFilterQuery = "and xup1.user_id in (:teamMemberIds) ";
		}
		if (teamMemberJourneyRequestDTO.isVanityUrlFilter()) {
			vanityFilterQuery = "and xp.vendor_company_id =:vendorCompanyId ";
		}

		finalQueryString = finalQueryString.replace("{teamMemberFilter}", teamMemberFilterQuery);
		finalQueryString = finalQueryString.replace("{vendorFilter}", vendorFilterQuery);
		finalQueryString = finalQueryString.replace("{vanityFilter}", vanityFilterQuery);
		finalQueryString = finalQueryString.replace("{dateFilter}", dateFilterQueryString);

		return (String) executeUniqueResultQuery(finalQueryString, teamMemberJourneyRequestDTO);
	}

	@Override
	public TeamMemberAnalyticsDTO getTeamMembersTrackAndPlaybookCount(
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO) {
		String finalQueryString = allTrackAndPlaybookCountQueryForTeamMember;

		String dateFilterQueryString = frameDateFilterQuery(teamMemberJourneyRequestDTO, TRACK_DATE_COLUMN);
		finalQueryString += dateFilterQueryString;

		if (teamMemberJourneyRequestDTO.getSelectedVendorCompanyIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedVendorCompanyIds().isEmpty()) {
			finalQueryString += "and xlt.company_id  in (:vendorCompanyIds) ";
		}
		if (teamMemberJourneyRequestDTO.getSelectedTeamMemberIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString += "and xup1.user_id  in (:teamMemberIds) ";
		}
		if (teamMemberJourneyRequestDTO.isVanityUrlFilter()) {
			finalQueryString += "and xlt.company_id =:vendorCompanyId ";
		}
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(finalQueryString);
		query.setParameter("partnerCompanyId", teamMemberJourneyRequestDTO.getPartnerCompanyId());
		if (teamMemberJourneyRequestDTO.getSelectedVendorCompanyIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedVendorCompanyIds().isEmpty()) {
			query.setParameterList("vendorCompanyIds", teamMemberJourneyRequestDTO.getSelectedVendorCompanyIds());
		}
		if (teamMemberJourneyRequestDTO.getSelectedTeamMemberIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedTeamMemberIds().isEmpty()) {
			query.setParameterList("teamMemberIds", teamMemberJourneyRequestDTO.getSelectedTeamMemberIds());
		}
		if (teamMemberJourneyRequestDTO.isVanityUrlFilter()) {
			CompanyProfile cp = vanityURLDao
					.getCompanyProfileByCompanyProfileName(teamMemberJourneyRequestDTO.getVendorCompanyProfileName());
			Integer companyId = cp.getId();
			if (companyId != null) {
				query.setParameter("vendorCompanyId", companyId);
			}
		}
		return (TeamMemberAnalyticsDTO) paginationUtil.getDto(TeamMemberAnalyticsDTO.class, query);
	}

	@Override
	public String getTeamMembersContactCount(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO) {
		String finalQueryString = allContactsQuery;

		String dateFilterQueryString = frameDateFilterQuery(teamMemberJourneyRequestDTO, CONTACT_DATE_COLUMN);
		finalQueryString += dateFilterQueryString;

		if (teamMemberJourneyRequestDTO.getSelectedTeamMemberIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString += "and u.user_id  in (:teamMemberIds) ";
		}

		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(finalQueryString);
		query.setParameter("partnerCompanyId", teamMemberJourneyRequestDTO.getPartnerCompanyId());
		if (teamMemberJourneyRequestDTO.getSelectedTeamMemberIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedTeamMemberIds().isEmpty()) {
			query.setParameterList("teamMemberIds", teamMemberJourneyRequestDTO.getSelectedTeamMemberIds());
		}
		return (String) query.uniqueResult();
	}

	@Override
	public String getTeamMembersCompanyCount(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO) {
		String finalQueryString = allCompanyQuery;

		String dateFilterQueryString = frameDateFilterQuery(teamMemberJourneyRequestDTO, COMPANY_DATE_COLUMN);
		finalQueryString += dateFilterQueryString;

		if (teamMemberJourneyRequestDTO.getSelectedTeamMemberIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString += "and created_by  in (:teamMemberIds) ";
		}
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(finalQueryString);
		query.setParameter("partnerCompanyId", teamMemberJourneyRequestDTO.getPartnerCompanyId());
		if (teamMemberJourneyRequestDTO.getSelectedTeamMemberIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedTeamMemberIds().isEmpty()) {
			query.setParameterList("teamMemberIds", teamMemberJourneyRequestDTO.getSelectedTeamMemberIds());
		}
		return (String) query.uniqueResult();
	}

	@Override
	public String getVendorTeamMembersCompanyCount(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO) {
		String finalQueryString = allCompanyQuery;

		String dateFilterQueryString = frameDateFilterQuery(teamMemberJourneyRequestDTO, COMPANY_DATE_COLUMN);
		finalQueryString += dateFilterQueryString;

		if (teamMemberJourneyRequestDTO.getSelectedTeamMemberIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString += "and created_by  in (:teamMemberIds) ";
		}
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(finalQueryString);
		query.setParameter("partnerCompanyId", teamMemberJourneyRequestDTO.getVendorCompanyId());
		if (teamMemberJourneyRequestDTO.getSelectedTeamMemberIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedTeamMemberIds().isEmpty()) {
			query.setParameterList("teamMemberIds", teamMemberJourneyRequestDTO.getSelectedTeamMemberIds());
		}

		return (String) query.uniqueResult();
	}

	@Override
	public PartnerJourneyTrackDetailsDTO getTeamMemberTrackCountsByInteraction(
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO) {
		Session session = sessionFactory.getCurrentSession();
		String finalQueryString = allViewedAndNotViewedTrackCountsQueryForTeamMember;
		String dateFilterQueryString = frameDateFilterQuery(teamMemberJourneyRequestDTO, TRACK_DATE_COLUMN);
		finalQueryString += dateFilterQueryString;

		if (teamMemberJourneyRequestDTO.getSelectedVendorCompanyIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedVendorCompanyIds().isEmpty()) {
			finalQueryString += "and xlt.company_id in (:vendorCompanyIds) ";
		}
		if (teamMemberJourneyRequestDTO.getSelectedTeamMemberIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString += "and xup1.user_id in (:teamMemberIds) ";
		}
		if (teamMemberJourneyRequestDTO.isVanityUrlFilter()) {
			finalQueryString += "and xlt.company_id =:vendorCompanyId ";
		}
		finalQueryString += allViewedAndNotViewedTrackCountsQueryForTeamMemberGroupBy;
		Query query = session.createSQLQuery(finalQueryString);
		query.setParameter("partnerCompanyId", teamMemberJourneyRequestDTO.getPartnerCompanyId());
		if (teamMemberJourneyRequestDTO.getSelectedVendorCompanyIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedVendorCompanyIds().isEmpty()) {
			query.setParameterList("vendorCompanyIds", teamMemberJourneyRequestDTO.getSelectedVendorCompanyIds());
		}
		if (teamMemberJourneyRequestDTO.getSelectedTeamMemberIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedTeamMemberIds().isEmpty()) {
			query.setParameterList("teamMemberIds", teamMemberJourneyRequestDTO.getSelectedTeamMemberIds());
		}
		if (teamMemberJourneyRequestDTO.isVanityUrlFilter()) {
			CompanyProfile cp = vanityURLDao
					.getCompanyProfileByCompanyProfileName(teamMemberJourneyRequestDTO.getVendorCompanyProfileName());
			Integer companyId = cp.getId();
			if (companyId != null) {
				query.setParameter("vendorCompanyId", companyId);
			}
		}
		return (PartnerJourneyTrackDetailsDTO) paginationUtil.getDto(PartnerJourneyTrackDetailsDTO.class, query);
	}

	@Override
	public PartnerJourneyTrackDetailsDTO getTeamMemberTrackCountsByInteractionForVendorTeamMember(
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO) {
		Session session = sessionFactory.getCurrentSession();
		String finalQueryString = allViewedAndNotViewedTrackCountsQueryForVendorView;
		String dateFilterQueryString = frameDateFilterQuery(teamMemberJourneyRequestDTO, TRACK_DATE_COLUMN_FOR_VENDOR);
		finalQueryString += dateFilterQueryString;

		if (teamMemberJourneyRequestDTO.getSelectedTeamMemberIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString += "and xup1.user_id in (:teamMemberIds) ";
		}
		finalQueryString += allViewedAndNotViewedTrackCountsQueryForVendorViewGroupBy;
		Query query = session.createSQLQuery(finalQueryString);
		query.setParameter("vendorCompanyId", teamMemberJourneyRequestDTO.getVendorCompanyId());
		if (teamMemberJourneyRequestDTO.getSelectedTeamMemberIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedTeamMemberIds().isEmpty()) {
			query.setParameterList("teamMemberIds", teamMemberJourneyRequestDTO.getSelectedTeamMemberIds());
		}
		return (PartnerJourneyTrackDetailsDTO) paginationUtil.getDto(PartnerJourneyTrackDetailsDTO.class, query);
	}

	@Override
	public Map<String, Object> getTeamMemberTrackDetailsByInteraction(Pagination pagination) {
		HashMap<String, Object> map = new HashMap<>();
		String queryString = frameTeamMemberTrackDetailsByInteractionQuery(pagination);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("partnerCompanyId", pagination.getPartnerCompanyId());
		if (pagination.getSelectedVendorCompanyIds() != null && !pagination.getSelectedVendorCompanyIds().isEmpty()) {
			query.setParameterList("vendorCompanyIds", pagination.getSelectedVendorCompanyIds());
		}
		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			query.setParameterList("teamMemberIds", pagination.getSelectedTeamMemberIds());
		}
		if (pagination.isVanityUrlFilter()) {
			CompanyProfile cp = vanityURLDao
					.getCompanyProfileByCompanyProfileName(utilDao.getPrmCompanyProfileName());
			Integer companyId = cp.getId();
			if (companyId != null) {
				query.setParameter("vendorCompanyId", companyId);
			}
		}
		return paginationUtil.setScrollableAndGetList(pagination, map, query, PartnerJourneyTrackDetailsDTO.class);
	}

	private String frameTeamMemberTrackDetailsByInteractionQuery(Pagination pagination) {
		String finalQueryString = allViewedAndNotViewedTrackDetailsQueryForTeamMember;
		String searchQuery = allViewedAndNotViewedTrackDetailsSearchConditionsForTeamMember;
		String groupByQuery = allViewedAndNotViewedTrackDetailsQueryGroupByForTeamMember;

		String dateFilterQueryString = frameDateFilterQuery(pagination, TRACK_DATE_COLUMN);
		finalQueryString += dateFilterQueryString;

		if (pagination.getSelectedVendorCompanyIds() != null && !pagination.getSelectedVendorCompanyIds().isEmpty()) {
			finalQueryString += "and xlt.company_id in (:vendorCompanyIds) ";
		}
		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString += "and xup.user_id in (:teamMemberIds) ";
		}
		if (pagination.isVanityUrlFilter()) {
			finalQueryString += "and xlt.company_id =:vendorCompanyId ";
		}
		String searchKey = pagination.getSearchKey();
		if (StringUtils.hasText(searchKey)) {
			finalQueryString += searchQuery.replace("searchKey", searchKey);
		}
		String trackTypeFilter = pagination.getTrackTypeFilter();
		if (!StringUtils.isEmpty(trackTypeFilter)) {
			if (trackTypeFilter.equals(partnerJourneyInteracted)) {
				finalQueryString += partnerJourneyTrackTypeInteractedFilter;
			} else if (trackTypeFilter.equals(partnerJourneyNotInteracted)) {
				finalQueryString += partnerJourneyTrackTypeNotInteractedFilter;
			}
		}
		return finalQueryString += groupByQuery;
	}

	@Override
	public Map<String, Object> getTeamMemberTrackDetailsByInteractionForVendor(Pagination pagination) {
		HashMap<String, Object> map = new HashMap<>();
		String queryString = frameTeamMemberTrackDetailsByInteractionQueryForVendor(pagination);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", pagination.getVendorCompanyId());
		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			query.setParameterList("teamMemberIds", pagination.getSelectedTeamMemberIds());
		}
		return paginationUtil.setScrollableAndGetList(pagination, map, query, PartnerJourneyTrackDetailsDTO.class);
	}

	private String frameTeamMemberTrackDetailsByInteractionQueryForVendor(Pagination pagination) {
		String finalQueryString = allViewedAndNotViewedTrackDetailsQueryForVendor;
		String searchQuery = allViewedAndNotViewedTrackDetailsSearchConditionsForVendorTeamMember;

		String dateFilterQueryString = frameDateFilterQuery(pagination, TRACK_DATE_COLUMN_FOR_VENDOR);
		finalQueryString += dateFilterQueryString;

		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString += "and xup1.user_id in (:teamMemberIds) ";
		}
		String searchKey = pagination.getSearchKey();
		if (StringUtils.hasText(searchKey)) {
			finalQueryString += searchQuery.replace("searchKey", searchKey);
		}
		String trackTypeFilter = pagination.getTrackTypeFilter();
		if (!StringUtils.isEmpty(trackTypeFilter)) {
			if (trackTypeFilter.equals(partnerJourneyInteracted)) {
				finalQueryString += partnerJourneyTrackTypeInteractedFilter;
			} else if (trackTypeFilter.equals(partnerJourneyNotInteracted)) {
				finalQueryString += partnerJourneyTrackTypeNotInteractedFilter;
			}
		}
		return finalQueryString;
	}

	@Override
	public PartnerJourneyTrackDetailsDTO getTeamMemberTrackCountsByType(
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO) {
		String finalQueryString = allTypeWiseTrackCountsQueryForTeamMember;
		String dateFilterQueryString = frameDateFilterQuery(teamMemberJourneyRequestDTO, TRACK_DATE_COLUMN);
		finalQueryString += dateFilterQueryString;

		if (teamMemberJourneyRequestDTO.getSelectedVendorCompanyIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedVendorCompanyIds().isEmpty()) {
			finalQueryString += " and xlt.company_id in (:vendorCompanyIds) ";
		}
		if (teamMemberJourneyRequestDTO.getSelectedTeamMemberIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString += "and xup1.user_id in (:teamMemberIds) ";
		}
		if (teamMemberJourneyRequestDTO.isVanityUrlFilter()) {
			finalQueryString += "and xlt.company_id =:vendorCompanyId ";
		}
		String trackTypeFilter = teamMemberJourneyRequestDTO.getTrackTypeFilter();
		if (!StringUtils.isEmpty(trackTypeFilter)) {
			if (trackTypeFilter.equals(partnerJourneyInteracted)) {
				finalQueryString += partnerJourneyTypeWiseTrackInteractedFilter;
			} else if (trackTypeFilter.equals(partnerJourneyNotInteracted)) {
				finalQueryString += partnerJourneyTypeWiseTrackNotInteractedFilter;
			}
		}
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(finalQueryString);
		query.setParameter("partnerCompanyId", teamMemberJourneyRequestDTO.getPartnerCompanyId());
		if (teamMemberJourneyRequestDTO.getSelectedVendorCompanyIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedVendorCompanyIds().isEmpty()) {
			query.setParameterList("vendorCompanyIds", teamMemberJourneyRequestDTO.getSelectedVendorCompanyIds());
		}
		if (teamMemberJourneyRequestDTO.getSelectedTeamMemberIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedTeamMemberIds().isEmpty()) {
			query.setParameterList("teamMemberIds", teamMemberJourneyRequestDTO.getSelectedTeamMemberIds());
		}
		if (teamMemberJourneyRequestDTO.isVanityUrlFilter()) {
			CompanyProfile cp = vanityURLDao
					.getCompanyProfileByCompanyProfileName(teamMemberJourneyRequestDTO.getVendorCompanyProfileName());
			Integer companyId = cp.getId();
			if (companyId != null) {
				query.setParameter("vendorCompanyId", companyId);
			}
		}
		return (PartnerJourneyTrackDetailsDTO) paginationUtil.getDto(PartnerJourneyTrackDetailsDTO.class, query);
	}

	@Override
	public PartnerJourneyTrackDetailsDTO getTeamMemberTrackCountsByTypeForVendor(
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO) {
		String finalQueryString = allTypeWiseTrackCountsQueryForVendor;
		String dateFilterQueryString = frameDateFilterQuery(teamMemberJourneyRequestDTO, TRACK_DATE_COLUMN_FOR_VENDOR);
		finalQueryString += dateFilterQueryString;
		if (teamMemberJourneyRequestDTO.getSelectedTeamMemberIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString += "and xup1.user_id in (:teamMemberIds) ";
		}
		String trackTypeFilter = teamMemberJourneyRequestDTO.getTrackTypeFilter();
		if (!StringUtils.isEmpty(trackTypeFilter)) {
			if (trackTypeFilter.equals(partnerJourneyInteracted)) {
				finalQueryString += partnerJourneyTypeWiseTrackInteractedFilter;
			} else if (trackTypeFilter.equals(partnerJourneyNotInteracted)) {
				finalQueryString += partnerJourneyTypeWiseTrackNotInteractedFilter;
			}
		}
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(finalQueryString);
		query.setParameter("vendorCompanyId", teamMemberJourneyRequestDTO.getVendorCompanyId());
		if (teamMemberJourneyRequestDTO.getSelectedTeamMemberIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedTeamMemberIds().isEmpty()) {
			query.setParameterList("teamMemberIds", teamMemberJourneyRequestDTO.getSelectedTeamMemberIds());
		}
		return (PartnerJourneyTrackDetailsDTO) paginationUtil.getDto(PartnerJourneyTrackDetailsDTO.class, query);
	}

	@Override
	public Map<String, Object> getTeamMemberTrackAssetDetailsByType(Pagination pagination) {
		HashMap<String, Object> map = new HashMap<>();
		String queryString = frameTeamMemberTrackAssetDetailsByTypeQuery(pagination);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("partnerCompanyId", pagination.getPartnerCompanyId());
		if (pagination.getSelectedVendorCompanyIds() != null && !pagination.getSelectedVendorCompanyIds().isEmpty()) {
			query.setParameterList("vendorCompanyIds", pagination.getSelectedVendorCompanyIds());
		}
		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			query.setParameterList("teamMemberIds", pagination.getSelectedTeamMemberIds());
		}
		if (pagination.isVanityUrlFilter()) {
			CompanyProfile cp = vanityURLDao
					.getCompanyProfileByCompanyProfileName(utilDao.getPrmCompanyProfileName());
			Integer companyId = cp.getId();
			if (companyId != null) {
				query.setParameter("vendorCompanyId", companyId);
			}
		}
		return paginationUtil.setScrollableAndGetList(pagination, map, query, PartnerJourneyTrackDetailsDTO.class);
	}

	private String frameTeamMemberTrackAssetDetailsByTypeQuery(Pagination pagination) {
		String finalQueryString = "";
		String searchKey = pagination.getSearchKey();
		finalQueryString = allTypeWiseTrackDetailsQueryForTeamMember;
		if (StringUtils.hasText(searchKey)) {
			if (searchKey.toLowerCase().equals(partnerJourneyNotOpened.toLowerCase())) {
				finalQueryString += allTypeWiseTrackDetailsNotOpenedSearchConditionsForTeamMember.replace("searchKey",
						searchKey);
			} else {
				finalQueryString += allTypeWiseTrackDetailsSearchConditionsForTeamMember.replace("searchKey",
						searchKey);
			}
		}

		String dateFilterQueryString = frameDateFilterQuery(pagination, TRACK_DATE_COLUMN);
		finalQueryString += dateFilterQueryString;

		if (pagination.getSelectedVendorCompanyIds() != null && !pagination.getSelectedVendorCompanyIds().isEmpty()) {
			finalQueryString += "and xlt.company_id in (:vendorCompanyIds) ";
		}
		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString += "and xup1.user_id  in (:teamMemberIds) ";
		}

		if (pagination.isVanityUrlFilter()) {
			finalQueryString += "and xlt.company_id =:vendorCompanyId ";
		}
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
	public Map<String, Object> getTeamMemberTrackAssetDetailsByTypeForVendorTeamMember(Pagination pagination) {
		HashMap<String, Object> map = new HashMap<>();
		String queryString = frameTeamMemberTrackAssetDetailsByTypeQueryForVendor(pagination);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", pagination.getVendorCompanyId());
		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			query.setParameterList("teamMemberIds", pagination.getSelectedTeamMemberIds());
		}
		return paginationUtil.setScrollableAndGetList(pagination, map, query, PartnerJourneyTrackDetailsDTO.class);
	}

	private String frameTeamMemberTrackAssetDetailsByTypeQueryForVendor(Pagination pagination) {
		String finalQueryString = "";
		String searchKey = pagination.getSearchKey();
		finalQueryString = allTypeWiseTrackDetailsQueryForVendor;
		if (StringUtils.hasText(searchKey)) {
			if (searchKey.toLowerCase().equals(partnerJourneyNotOpened.toLowerCase())) {
				finalQueryString += allTypeWiseTrackDetailsNotOpenedSearchConditionsForVendorTeamMember
						.replace("searchKey", searchKey);
			} else {
				finalQueryString += allTypeWiseTrackDetailsSearchConditionsForVendorTeamMember.replace("searchKey",
						searchKey);
			}
		}

		String dateFilterQueryString = frameDateFilterQuery(pagination, TRACK_DATE_COLUMN_FOR_VENDOR);
		finalQueryString += dateFilterQueryString;

		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString += "and xup1.user_id  in (:teamMemberIds) ";
		}
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
	public Map<String, Object> getTeamMemberTracksCount(Pagination pagination) {
		HashMap<String, Object> map = new HashMap<>();
		String queryString = frameTeamMemberWiseTracksCountQuery(pagination);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("partnerCompanyId", pagination.getPartnerCompanyId());
		if (pagination.getSelectedVendorCompanyIds() != null && !pagination.getSelectedVendorCompanyIds().isEmpty()) {
			query.setParameterList("vendorCompanyIds", pagination.getSelectedVendorCompanyIds());
		}
		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			query.setParameterList("teamMemberIds", pagination.getSelectedTeamMemberIds());
		}
		if (pagination.isVanityUrlFilter()) {
			CompanyProfile cp = vanityURLDao
					.getCompanyProfileByCompanyProfileName(utilDao.getPrmCompanyProfileName());
			Integer companyId = cp.getId();
			if (companyId != null) {
				query.setParameter("vendorCompanyId", companyId);
			}
		}
		return paginationUtil.setScrollableAndGetList(pagination, map, query, TeamMemberDTO.class);
	}

	private String frameTeamMemberWiseTracksCountQuery(Pagination pagination) {
		String finalQueryString = allTracksTeamMemberWiseCountsQuery;
		String searchQuery = allTracksandPlayBooksCountsQuerySearchCondition;
		String groupByQuery = allTracksTeamMemberWiseCountsGroupBy;
		String searchKey = pagination.getSearchKey();
		if (pagination.getSelectedVendorCompanyIds() != null && !pagination.getSelectedVendorCompanyIds().isEmpty()) {
			finalQueryString += "and xlt.company_id in (:vendorCompanyIds) ";
		}

		String dateFilterQueryString = frameDateFilterQuery(pagination, TRACK_DATE_COLUMN);
		finalQueryString += dateFilterQueryString;

		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString += "and xup.user_id  in (:teamMemberIds) ";
		}
		if (pagination.isVanityUrlFilter()) {
			finalQueryString += "and xlt.company_id =:vendorCompanyId ";
		}
		if (StringUtils.hasText(searchKey)) {
			finalQueryString += searchQuery.replace("searchKey", searchKey);
		}
		finalQueryString += groupByQuery;
		return finalQueryString;
	}

	@Override
	public Map<String, Object> getTeamMemberTracksCountForVendorTeamMember(Pagination pagination) {
		HashMap<String, Object> map = new HashMap<>();
		String queryString = frameTeamMemberWiseTracksCountQueryForVendor(pagination);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", pagination.getVendorCompanyId());
		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			query.setParameterList("teamMemberIds", pagination.getSelectedTeamMemberIds());
		}
		return paginationUtil.setScrollableAndGetList(pagination, map, query, TeamMemberDTO.class);
	}

	private String frameTeamMemberWiseTracksCountQueryForVendor(Pagination pagination) {
		String finalQueryString = allTracksTeamMemberWiseCountsQueryForVendor;
		String searchQuery = emailIdSearchCondition;
		String groupByQuery = allTracksTeamMemberWiseCountsQueryForVendorGroupBy;
		String searchKey = pagination.getSearchKey();
		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString += "and xup.user_id  in (:teamMemberIds) ";
		}

		String dateFilterQueryString = frameDateFilterQuery(pagination, TRACK_DATE_COLUMN_FOR_VENDOR);
		finalQueryString += dateFilterQueryString;

		if (StringUtils.hasText(searchKey)) {
			finalQueryString += searchQuery.replace("searchKey", searchKey);
		}
		finalQueryString += groupByQuery;
		return finalQueryString;
	}

	@Override
	public Map<String, Object> getTeamMemberWisePlaybooksCount(Pagination pagination) {
		HashMap<String, Object> map = new HashMap<>();
		String queryString = frameTeamMemberWisePlaybooksCountQuery(pagination);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("partnerCompanyId", pagination.getPartnerCompanyId());
		if (pagination.getSelectedVendorCompanyIds() != null && !pagination.getSelectedVendorCompanyIds().isEmpty()) {
			query.setParameterList("vendorCompanyIds", pagination.getSelectedVendorCompanyIds());
		}
		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			query.setParameterList("teamMemberIds", pagination.getSelectedTeamMemberIds());
		}
		if (pagination.isVanityUrlFilter()) {
			CompanyProfile cp = vanityURLDao
					.getCompanyProfileByCompanyProfileName(utilDao.getPrmCompanyProfileName());
			Integer companyId = cp.getId();
			if (companyId != null) {
				query.setParameter("vendorCompanyId", companyId);
			}
		}
		return paginationUtil.setScrollableAndGetList(pagination, map, query, TeamMemberDTO.class);
	}

	private String frameTeamMemberWisePlaybooksCountQuery(Pagination pagination) {
		String finalQueryString = allPlaybooksTeamMemberWiseCountsQuery;
		String searchQuery = allTracksandPlayBooksCountsQuerySearchCondition;
		String groupByQuery = allPlayBooksTeamMemberWiseCountsGroupBy;
		String searchKey = pagination.getSearchKey();
		if (pagination.getSelectedVendorCompanyIds() != null && !pagination.getSelectedVendorCompanyIds().isEmpty()) {
			finalQueryString += "and xlt.company_id in (:vendorCompanyIds) ";
		}

		String dateFilterQueryString = frameDateFilterQuery(pagination, TRACK_DATE_COLUMN);
		finalQueryString += dateFilterQueryString;

		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString += "and xup.user_id  in (:teamMemberIds) ";
		}
		if (pagination.isVanityUrlFilter()) {
			finalQueryString += "and xlt.company_id =:vendorCompanyId ";
		}
		if (StringUtils.hasText(searchKey)) {
			finalQueryString += searchQuery.replace("searchKey", searchKey);
		}
		finalQueryString += groupByQuery;
		return finalQueryString;
	}

	@Override
	public Map<String, Object> getTeamMemberWisePlaybooksCountForVendorTeamMember(Pagination pagination) {
		HashMap<String, Object> map = new HashMap<>();
		String queryString = frameTeamMemberWisePlaybooksCountQueryForVendor(pagination);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", pagination.getVendorCompanyId());
		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			query.setParameterList("teamMemberIds", pagination.getSelectedTeamMemberIds());
		}
		return paginationUtil.setScrollableAndGetList(pagination, map, query, TeamMemberDTO.class);
	}

	private String frameTeamMemberWisePlaybooksCountQueryForVendor(Pagination pagination) {
		String finalQueryString = allPlaybooksTeamMemberWiseCountsQueryForVendor;
		String searchQuery = emailIdSearchCondition;
		String groupByQuery = allPlaybookCountsGroupByForVendorTeamMember;
		String searchKey = pagination.getSearchKey();

		String dateFilterQueryString = frameDateFilterQuery(pagination, TRACK_DATE_COLUMN_FOR_VENDOR);
		finalQueryString += dateFilterQueryString;

		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString += "and xup.user_id  in (:teamMemberIds) ";
		}
		if (StringUtils.hasText(searchKey)) {
			finalQueryString += searchQuery.replace("searchKey", searchKey);
		}
		finalQueryString += groupByQuery;
		return finalQueryString;
	}

	@Override
	public Map<String, Object> getTeamMemberTrackDetails(Pagination pagination) {
		HashMap<String, Object> map = new HashMap<>();
		String queryString = frameTeamMemberTrackDetailsQuery(pagination);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		if (pagination.getSelectedVendorCompanyIds() != null && !pagination.getSelectedVendorCompanyIds().isEmpty()) {
			query.setParameterList("vendorCompanyIds", pagination.getSelectedVendorCompanyIds());
		}
		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			query.setParameterList("teamMemberIds", pagination.getSelectedTeamMemberIds());
		}
		if (pagination.isVanityUrlFilter()) {
			CompanyProfile cp = vanityURLDao
					.getCompanyProfileByCompanyProfileName(utilDao.getPrmCompanyProfileName());
			Integer companyId = cp.getId();
			if (companyId != null) {
				query.setParameter("vendorCompanyId", companyId);
			}
		}
		return paginationUtil.setScrollableAndGetList(pagination, map, query, PartnerJourneyTrackDetailsDTO.class);
	}

	private String frameTeamMemberTrackDetailsQuery(Pagination pagination) {
		Integer companyId = userDao.getCompanyIdByUserId(pagination.getUserId());
		String vendorCompanyIds = "vendorCompanyIds";
		String teamMemberIds = "teamMemberIds";
		String dateFilter = "dateFilter";
		String searchQuery = " ";
		String searchKey = pagination.getSearchKey();
		if (StringUtils.hasText(searchKey)) {
			searchQuery = allTracksUserWiseContentDetailsSearchConditionForTeamMember;
		}
		String finalQueryString = "with a as "
				+ "(select distinct xlt.id as \"id1\",xup1.user_id as \"uid\", xup1.email_id as \"emailId\",concat(xup1.firstname , ' ' , xup1.lastname) as \"fullName\", "
				+ "xlt.title as \"title\", xlt.published_time as \"publishedOn\", "
				+ "sum(coalesce(xltv.progress, 0))/count(distinct xltv.user_id) as \"progress\"  "
				+ "from xt_learning_track xlt  "
				+ "left join xt_learning_track_visibility xltv on xltv.learning_track_id=xlt.id "
				+ "left join xt_user_profile xup1 on xup1.user_id=xltv.user_id " + "where xup1.company_id = "
				+ companyId + vendorCompanyIds + teamMemberIds + dateFilter + " "
				+ "and xlt.type='TRACK' and xlt.is_published= true  " + searchQuery + " " + " group by 1,2,3,4,5 ), "
				+ "b as "
				+ "(select distinct xlt.id,xltv.user_id as \"ouid\",count( case when xltcp.type='OPENED' then xd.id end ) as \"openedCount\", "
				+ "count( case when xltcp.type='VIEWED' then xd.id end ) as \"viewedCount\", "
				+ "count( case when xltcp.type='DOWNLOADED' then xd.id end ) as \"downloadedCount\", "
				+ "count(distinct xd.id) as \"assetCount\", "
				+ "coalesce(count(distinct xltc.quiz_id), 0) as \"quizCount\" "
				+ "from xt_learning_track_visibility xltv  "
				+ "left join xt_learning_track xlt on xltv.learning_track_id=xlt.id "
				+ "left join xt_learning_track_content xltc on xltc.learning_track_id=xlt.id "
				+ "left join xt_learning_track_content_partner_activity xltcp on xltcp.learning_track_visibility_id=xltv.id and xltcp.learning_track_content_id =xltc.id  "
				+ "left join xt_dam xd on xd.id = xltc.dam_id  "
				+ "left join xt_user_profile xup1 on xup1.user_id=xltv.user_id where xup1.company_id=" + companyId
				+ vendorCompanyIds + teamMemberIds + " " + " and xlt.type='TRACK' and xlt.is_published= true "
				+ " group by 1,2), " + " c as " + "(select distinct xup1.user_id as \"scid\",xlt.id  as \"qid\",\r\n"
				+ "xfs.score as score,xf.max_score  as max_score \r\n" + "from xt_learning_track xlt \r\n"
				+ "left join xt_learning_track_visibility xltv on xltv.learning_track_id=xlt.id \r\n"
				+ "left join xt_partnership p on xltv.partnership_id = p.id \r\n"
				+ "left join xt_learning_track_content xltc on xltc.learning_track_id=xlt.id \r\n"
				+ "left join xt_user_profile xup1 on xup1.user_id=xltv.user_id \r\n"
				+ "left join xt_form xf on xltc.quiz_id= xf.id \r\n"
				+ "left join xt_form_submit xfs on xf.id=xfs.form_id and xfs.user_id =xup1.user_id and xfs.learning_track_id = xlt.id \r\n"
				+ "where xup1.company_id =" + companyId + vendorCompanyIds + teamMemberIds
				+ "and xlt.type='TRACK' and xlt.is_published= true and xltv.progress>0 \r\n"
				+ "and xfs.form_submit_type='LMS_FORM' \r\n" + " )"
				+ "select distinct  a.\"emailId\",a.\"fullName\",a.\"title\",a.\"publishedOn\", "
				+ "b.\"openedCount\",b.\"viewedCount\",b.\"downloadedCount\",cast(a.\"progress\" as Integer),b.\"assetCount\",b.\"quizCount\", "
				+ "coalesce (c.score, 0) || ' out of ' ||coalesce(c.max_score, 0) as  \"score\" "
				+ "from a left join b on  a.\"id1\"=b.id and a.\"uid\"= b.\"ouid\" "
				+ "left join c on c.\"qid\"=a.\"id1\" and c.\"scid\"=a.\"uid\" ";

		if (pagination.getSelectedVendorCompanyIds() != null && !pagination.getSelectedVendorCompanyIds().isEmpty()) {
			finalQueryString = finalQueryString.replace(vendorCompanyIds,
					" and xlt.company_id in (:vendorCompanyIds) ");
		} else if (pagination.isVanityUrlFilter()) {
			finalQueryString = finalQueryString.replace(vendorCompanyIds, " and xlt.company_id =:vendorCompanyId ");
		} else {
			finalQueryString = finalQueryString.replace(vendorCompanyIds, " ");
		}

		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString = finalQueryString.replace(teamMemberIds, " and xup1.user_id in (:teamMemberIds) ");
		} else {
			finalQueryString = finalQueryString.replace(teamMemberIds, " ");
		}

		if (StringUtils.hasText(searchKey)) {
			finalQueryString = finalQueryString.replace("searchKey", searchKey);
		}

		String dateFilterQueryString = frameDateFilterQuery(pagination, TRACK_DATE_COLUMN);
		finalQueryString = finalQueryString.replace(dateFilter, dateFilterQueryString);

		return finalQueryString;
	}

	@Override
	public Map<String, Object> getTeamMemberTrackAssetDetails(Pagination pagination) {
		HashMap<String, Object> map = new HashMap<>();
		String queryString = frameTeamMemberTrackAssetDetailsQuery(pagination);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("partnerCompanyId", pagination.getPartnerCompanyId());
		if (pagination.getSelectedVendorCompanyIds() != null && !pagination.getSelectedVendorCompanyIds().isEmpty()) {
			query.setParameterList("vendorCompanyIds", pagination.getSelectedVendorCompanyIds());
		}
		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			query.setParameterList("teamMemberIds", pagination.getSelectedTeamMemberIds());
		}
		if (pagination.isVanityUrlFilter()) {
			CompanyProfile cp = vanityURLDao
					.getCompanyProfileByCompanyProfileName(utilDao.getPrmCompanyProfileName());
			Integer companyId = cp.getId();
			if (companyId != null) {
				query.setParameter("vendorCompanyId", companyId);
			}
		}
		return paginationUtil.setScrollableAndGetList(pagination, map, query, PartnerJourneyTrackDetailsDTO.class);
	}

	private String frameTeamMemberTrackAssetDetailsQuery(Pagination pagination) {
		String finalQueryString = allTrackAssetsQueryForTeamMember;
		String groupByQuery = allTrackAssetsGroupByForTeamMember;

		String dateFilterQueryString = frameDateFilterQuery(pagination, TRACK_DATE_COLUMN);
		finalQueryString += dateFilterQueryString;

		String searchKey = pagination.getSearchKey();
		if (pagination.getSelectedVendorCompanyIds() != null && !pagination.getSelectedVendorCompanyIds().isEmpty()) {
			finalQueryString += "and xlt.company_id in (:vendorCompanyIds) ";
		}
		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString += "and xup1.user_id   in (:teamMemberIds) ";
		}
		if (StringUtils.hasText(searchKey)) {
			finalQueryString += partnerJourneyTrackAssetsSearchCondition.replace("searchKey", searchKey);
		}
		if (pagination.isVanityUrlFilter()) {
			finalQueryString += "and xlt.company_id =:vendorCompanyId ";
		}
		finalQueryString += groupByQuery;
		return finalQueryString;
	}

	@Override
	public Map<String, Object> getTeamMemberTrackAssetDetailsForVendorTeamMember(Pagination pagination) {
		HashMap<String, Object> map = new HashMap<>();
		String queryString = frameTeamMemberTrackAssetDetailsQueryForVendor(pagination);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", pagination.getVendorCompanyId());
		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			query.setParameterList("teamMemberIds", pagination.getSelectedTeamMemberIds());
		}
		return paginationUtil.setScrollableAndGetList(pagination, map, query, PartnerJourneyTrackDetailsDTO.class);
	}

	private String frameTeamMemberTrackAssetDetailsQueryForVendor(Pagination pagination) {
		String finalQueryString = allTrackAssetsQueryForVendor;
		String groupByQuery = allTrackAssetsGroupByForTeamMember;
		String searchKey = pagination.getSearchKey();

		String dateFilterQueryString = frameDateFilterQuery(pagination, TRACK_DATE_COLUMN_FOR_VENDOR);
		finalQueryString += dateFilterQueryString;

		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString += "and xup1.user_id  in (:teamMemberIds) ";
		}
		if (StringUtils.hasText(searchKey)) {
			finalQueryString += partnerJourneyTrackAssetsSearchCondition.replace("searchKey", searchKey);
		}
		finalQueryString += groupByQuery;
		return finalQueryString;
	}

	@Override
	public Map<String, Object> getTeamMemberPlaybookAssetDetails(Pagination pagination) {
		HashMap<String, Object> map = new HashMap<>();
		String queryString = frameTeamMemberPlaybookAssetDetailsQuery(pagination);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("partnerCompanyId", pagination.getPartnerCompanyId());
		if (pagination.getSelectedVendorCompanyIds() != null && !pagination.getSelectedVendorCompanyIds().isEmpty()) {
			query.setParameterList("vendorCompanyIds", pagination.getSelectedVendorCompanyIds());
		}
		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			query.setParameterList("teamMemberIds", pagination.getSelectedTeamMemberIds());
		}
		if (pagination.isVanityUrlFilter()) {
			CompanyProfile cp = vanityURLDao
					.getCompanyProfileByCompanyProfileName(utilDao.getPrmCompanyProfileName());
			Integer companyId = cp.getId();
			if (companyId != null) {
				query.setParameter("vendorCompanyId", companyId);
			}
		}
		return paginationUtil.setScrollableAndGetList(pagination, map, query, PartnerJourneyTrackDetailsDTO.class);
	}

	private String frameTeamMemberPlaybookAssetDetailsQuery(Pagination pagination) {
		String finalQueryString = allPlaybookAssetsQueryForTeamMember;
		String groupByQuery = allPlaybookAssetsGroupByForTeamMember;

		String dateFilterQueryString = frameDateFilterQuery(pagination, TRACK_DATE_COLUMN);
		finalQueryString += dateFilterQueryString;

		if (pagination.getSelectedVendorCompanyIds() != null && !pagination.getSelectedVendorCompanyIds().isEmpty()) {
			finalQueryString += "and  xlt.company_id in (:vendorCompanyIds) ";
		}
		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString += "and xup1.user_id  in (:teamMemberIds) ";
		}
		if (pagination.isVanityUrlFilter()) {
			finalQueryString += "and xlt.company_id =:vendorCompanyId ";
		}
		String searchKey = pagination.getSearchKey();
		if (StringUtils.hasText(searchKey)) {
			finalQueryString += partnerJourneyPlaybookAssetsSearchCondition.replace("searchKey", searchKey);
		}
		finalQueryString += groupByQuery;
		return finalQueryString;
	}

	@Override
	public Map<String, Object> getTeamMemberPlaybookAssetDetailsForVendorTeamMember(Pagination pagination) {
		HashMap<String, Object> map = new HashMap<>();
		String queryString = frameTeamMemberPlaybookAssetDetailsQueryForVendor(pagination);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", pagination.getVendorCompanyId());
		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			query.setParameterList("teamMemberIds", pagination.getSelectedTeamMemberIds());
		}
		return paginationUtil.setScrollableAndGetList(pagination, map, query, PartnerJourneyTrackDetailsDTO.class);
	}

	private String frameTeamMemberPlaybookAssetDetailsQueryForVendor(Pagination pagination) {
		String finalQueryString = allPlaybookAssetsQueryForVendorTeamMember;
		String groupByQuery = allPlaybookAssetsGroupByForTeamMember;

		String dateFilterQueryString = frameDateFilterQuery(pagination, TRACK_DATE_COLUMN_FOR_VENDOR);
		finalQueryString += dateFilterQueryString;

		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString += "and xup1.user_id  in (:teamMemberIds) ";
		}
		String searchKey = pagination.getSearchKey();
		if (StringUtils.hasText(searchKey)) {
			finalQueryString += partnerJourneyPlaybookAssetsSearchCondition.replace("searchKey", searchKey);
		}
		finalQueryString += groupByQuery;
		return finalQueryString;
	}

	@Override
	public Map<String, Object> getTeamMemberShareLeadDetails(Pagination pagination) {
		HashMap<String, Object> map = new HashMap<>();
		String queryString = frameTeamMemberShareLeadDetailsQuery(pagination);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("partnerCompanyId", pagination.getPartnerCompanyId());
		if (pagination.getSelectedVendorCompanyIds() != null && !pagination.getSelectedVendorCompanyIds().isEmpty()) {
			query.setParameterList("vendorCompanyIds", pagination.getSelectedVendorCompanyIds());
		}
		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			query.setParameterList("teamMemberIds", pagination.getSelectedTeamMemberIds());
		}
		if (pagination.isVanityUrlFilter()) {
			CompanyProfile cp = vanityURLDao
					.getCompanyProfileByCompanyProfileName(utilDao.getPrmCompanyProfileName());
			Integer companyId = cp.getId();
			if (companyId != null) {
				query.setParameter("vendorCompanyId", companyId);
			}
		}
		return paginationUtil.setScrollableAndGetList(pagination, map, query, ShareLeadsDTO.class);
	}

	private String frameTeamMemberShareLeadDetailsQuery(Pagination pagination) {
		String finalQueryString = allShareLeadDetailsQueryForTeamMember;
		String groupByQuery = allShareLeadDetailsGroupByForTeamMember;
		String searchQuery = allShareLeadDetailsSearchConditionsForTeamMember;
		String searchKey = pagination.getSearchKey();

		if (pagination.getSelectedVendorCompanyIds() != null && !pagination.getSelectedVendorCompanyIds().isEmpty()) {
			finalQueryString += "and  xul.assigned_company_id in (:vendorCompanyIds) ";
		}
		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString += "and xup1.user_id  in (:teamMemberIds) ";
		}

		String dateFilterQueryString = frameDateFilterQuery(pagination, SHARELEADS_DATE_COLUMN);
		finalQueryString += dateFilterQueryString;

		if (pagination.isVanityUrlFilter()) {
			finalQueryString += "and xul.assigned_company_id  =:vendorCompanyId ";
		}

		if (StringUtils.hasText(searchKey)) {
			finalQueryString += searchQuery.replace("searchKey", searchKey);
		}
		finalQueryString += groupByQuery;
		return finalQueryString;
	}

	@Override
	public Map<String, Object> getTeamMemberShareLeadDetailsForVendorTeamMember(Pagination pagination) {
		HashMap<String, Object> map = new HashMap<>();
		String queryString = frameTeamMemberShareLeadDetailsQueryForVendorTeamMember(pagination);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", pagination.getVendorCompanyId());
		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			query.setParameterList("teamMemberIds", pagination.getSelectedTeamMemberIds());
		}
		return paginationUtil.setScrollableAndGetList(pagination, map, query, ShareLeadsDTO.class);
	}

	private String frameTeamMemberShareLeadDetailsQueryForVendorTeamMember(Pagination pagination) {
		String finalQueryString = allShareLeadDetailsQueryForVendorTeamMember;
		String groupByQuery = allShareLeadDetailsGroupByForVendorTeamMember;
		String searchQuery = allShareLeadDetailsSearchConditionsForVendorTeamMember;
		String searchKey = pagination.getSearchKey();

		String dateFilterQueryString = frameDateFilterQuery(pagination, SHARELEADS_DATE_COLUMN);
		finalQueryString += dateFilterQueryString;

		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString += "and u.user_id  in (:teamMemberIds) ";
		}

		if (StringUtils.hasText(searchKey)) {
			finalQueryString += searchQuery.replace("searchKey", searchKey);
		}
		finalQueryString += groupByQuery;
		return finalQueryString;
	}

	@Override
	public Map<String, Object> getTeamMemberRedistributedCampaignDetails(Pagination pagination) {
		return null;
	}

	@Override
	public Map<String, Object> getTeamMemberLeadDetails(Pagination pagination) {
		HashMap<String, Object> map = new HashMap<>();
		String queryString = frameTeamMemberLeadDetailsQuery(pagination);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("partnerCompanyId", pagination.getPartnerCompanyId());
		if (pagination.getSelectedVendorCompanyIds() != null && !pagination.getSelectedVendorCompanyIds().isEmpty()) {
			query.setParameterList("vendorCompanyIds", pagination.getSelectedVendorCompanyIds());
		}
		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			query.setParameterList("teamMemberIds", pagination.getSelectedTeamMemberIds());
		}
		if (pagination.isVanityUrlFilter()) {
			CompanyProfile cp = vanityURLDao
					.getCompanyProfileByCompanyProfileName(utilDao.getPrmCompanyProfileName());
			Integer companyId = cp.getId();
			if (companyId != null) {
				query.setParameter("vendorCompanyId", companyId);
			}
		}
		return paginationUtil.setScrollableAndGetList(pagination, map, query, LeadDto.class);
	}

	private String frameTeamMemberLeadDetailsQuery(Pagination pagination) {
		String finalQueryString = allLeadDetailsQueryForTeamMember;
		String searchQuery = allLeadDetailsSearchConditionsForTeamMember;
		String orderBy = allLeadDetailsOrderByVendorTeamMember;
		String searchKey = pagination.getSearchKey();

		String dateFilterQueryString = frameDateFilterQuery(pagination, LEAD_DATE_COLUMN);
		finalQueryString += dateFilterQueryString;

		if (pagination.getSelectedVendorCompanyIds() != null && !pagination.getSelectedVendorCompanyIds().isEmpty()) {
			finalQueryString += "and  xl.created_for_company_id  in (:vendorCompanyIds) ";
		}
		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString += "and xup.user_id  in (:teamMemberIds) ";
		}

		if (pagination.isVanityUrlFilter()) {
			finalQueryString += "and xl.created_for_company_id =:vendorCompanyId ";
		}

		if (StringUtils.hasText(searchKey)) {
			finalQueryString += searchQuery.replace("searchKey", searchKey);
		}

		finalQueryString += orderBy;

		return finalQueryString;
	}

	@Override
	public Map<String, Object> getTeamMemberDealDetails(Pagination pagination) {
		HashMap<String, Object> map = new HashMap<>();
		String queryString = frameTeamMemberDealDetailsQuery(pagination);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("partnerCompanyId", pagination.getPartnerCompanyId());
		if (pagination.getSelectedVendorCompanyIds() != null && !pagination.getSelectedVendorCompanyIds().isEmpty()) {
			query.setParameterList("vendorCompanyIds", pagination.getSelectedVendorCompanyIds());
		}
		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			query.setParameterList("teamMemberIds", pagination.getSelectedTeamMemberIds());
		}

		if (pagination.isVanityUrlFilter()) {
			CompanyProfile cp = vanityURLDao
					.getCompanyProfileByCompanyProfileName(utilDao.getPrmCompanyProfileName());
			Integer companyId = cp.getId();
			if (companyId != null) {
				query.setParameter("vendorCompanyId", companyId);
			}
		}

		return paginationUtil.setScrollableAndGetList(pagination, map, query, DealDto.class);
	}

	private String frameTeamMemberDealDetailsQuery(Pagination pagination) {
		String finalQueryString = allDealDetailsQueryForTeamMember;
		String searchQuery = allDealDetailsSearchConditionsForTeamMember;
		String orderBy = allDealDetailsOrderByTeamMember;

		String dateFilterQueryString = frameDateFilterQuery(pagination, DEAL_DATE_COLUMN);
		finalQueryString += dateFilterQueryString;

		if (pagination.getSelectedVendorCompanyIds() != null && !pagination.getSelectedVendorCompanyIds().isEmpty()) {
			finalQueryString += "and xd.created_for_company_id   in (:vendorCompanyIds) ";
		}
		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString += "and xup.user_id  in (:teamMemberIds) ";
		}

		if (pagination.isVanityUrlFilter()) {
			finalQueryString += "and xd.created_for_company_id =:vendorCompanyId ";
		}

		String searchKey = pagination.getSearchKey();
		if (StringUtils.hasText(searchKey)) {
			finalQueryString += searchQuery.replace("searchKey", searchKey);
		}

		finalQueryString += orderBy;

		return finalQueryString;
	}

	@Override
	public List<TeamMemberDTO> getTeamMemberInfoForFilter(Pagination pagination) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select distinct email_id as \"emailId\"," + "user_id as \"id\", " + "status as \"userStatus\""
				+ "from xt_user_profile where company_id =:partnerCompanyId" + " and status ='APPROVE' ";
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("partnerCompanyId", pagination.getPartnerCompanyId());
		query.setResultTransformer(Transformers.aliasToBean(TeamMemberDTO.class));
		List<TeamMemberDTO> teamMemberDtOs = query.list();
		return teamMemberDtOs;
	}

	@Override
	public List<VendorDTO> getVendorInfoForFilter(Pagination pagination) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select distinct c.company_id as \"companyId\"," + "c.company_name as \"companyName\"  "
				+ " from xt_partnership p " + " join xt_company_profile c on p.vendor_company_id = c.company_id "
				+ " left join xt_user_profile xup1 on c.company_id=xup1.company_id " + " where p.status = 'approved' "
				+ " and p.partner_company_id =:partnerCompanyId ";
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("partnerCompanyId", pagination.getPartnerCompanyId());
		query.setResultTransformer(Transformers.aliasToBean(VendorDTO.class));
		List<VendorDTO> vendorDtOs = query.list();
		return vendorDtOs;
	}

	@Override
	public Map<String, Object> getTeamMemberMdfDetails(Pagination pagination) {
		HashMap<String, Object> map = new HashMap<>();
		String queryString = frameTeamMemberMdfDetailsQuery(pagination);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("partnerCompanyId", pagination.getPartnerCompanyId());
		if (pagination.getSelectedVendorCompanyIds() != null && !pagination.getSelectedVendorCompanyIds().isEmpty()) {
			query.setParameterList("vendorCompanyIds", pagination.getSelectedVendorCompanyIds());
		}
		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			query.setParameterList("teamMemberIds", pagination.getSelectedTeamMemberIds());
		}
		if (pagination.isVanityUrlFilter()) {
			CompanyProfile cp = vanityURLDao
					.getCompanyProfileByCompanyProfileName(utilDao.getPrmCompanyProfileName());
			Integer companyId = cp.getId();
			if (companyId != null) {
				query.setParameter("vendorCompanyId", companyId);
			}
		}
		return paginationUtil.setScrollableAndGetList(pagination, map, query, MdfRequestDeatailsDTO.class);
	}

	private String frameTeamMemberMdfDetailsQuery(Pagination pagination) {
		String finalQueryString = TeamMemberAnalyticsMDFRequetsQuery;
		String groupByQuery = TeamMemberAnalyticsMDFRequetsQueryGroupBy;

		String dateFilterQueryString = frameDateFilterQuery(pagination, "xmr.created_time");
		finalQueryString += dateFilterQueryString;

		String searchKey = pagination.getSearchKey();
		if (pagination.getSelectedVendorCompanyIds() != null && !pagination.getSelectedVendorCompanyIds().isEmpty()) {
			finalQueryString += "and xp.vendor_company_id  in (:vendorCompanyIds) ";
		}
		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString += "and xup.user_id  in (:teamMemberIds) ";
		}

		if (pagination.isVanityUrlFilter()) {
			finalQueryString += "and xp.vendor_company_id =:vendorCompanyId ";
		}
		if (StringUtils.hasText(searchKey)) {
			finalQueryString += TeamMemberAnalyticsMDFRequetsSearchConditions.replace("searchKey", searchKey);
		}
		finalQueryString += groupByQuery;
		return finalQueryString;
	}

	@Override
	public String getAllPartnersCountForVendorTeamMember(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO) {
		String finalQueryString = allPartnersCountForTeamAnalytics;

		String dateFilterQueryString = frameDateFilterQuery(teamMemberJourneyRequestDTO, CONTACT_DATE_COLUMN);
		finalQueryString += dateFilterQueryString;

		if (teamMemberJourneyRequestDTO.getSelectedTeamMemberIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString += " and p.vendor_id in (:teamMemberIds) ";
		}
		return (String) executeUniqueResultQueryForVendor(finalQueryString, teamMemberJourneyRequestDTO);
	}

	private Object executeUniqueResultQueryForVendor(String finalQueryString,
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(finalQueryString);
		query.setParameter("vendorCompanyId", teamMemberJourneyRequestDTO.getVendorCompanyId());
		if (teamMemberJourneyRequestDTO.getSelectedTeamMemberIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedTeamMemberIds().isEmpty()) {
			query.setParameterList("teamMemberIds", teamMemberJourneyRequestDTO.getSelectedTeamMemberIds());
		}
		return query.uniqueResult();
	}

	@Override
	public String getlaunchedCampaignCountForVendorTeamMember(
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO) {
		String finalQueryString = launchedCampaignCountForTeamAnalytics;

		String dateFilterQueryString = frameDateFilterQuery(teamMemberJourneyRequestDTO, "c.launch_time");
		finalQueryString += dateFilterQueryString;

		if (teamMemberJourneyRequestDTO.getSelectedTeamMemberIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString += " and xup.user_id in (:teamMemberIds) ";
		}
		return (String) executeUniqueResultQueryForVendor(finalQueryString, teamMemberJourneyRequestDTO);
	}

	@Override
	public String getShareLeadCountForVendorTeamMember(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO) {
		String finalQueryString = shareLeadCountForVendorView;
		String groupBy = shareLeadCountForVendorViewGroupBy;
		String dateFilterQueryString = frameDateFilterQuery(teamMemberJourneyRequestDTO, SHARELEADS_DATE_COLUMN);
		finalQueryString = finalQueryString.replace("{dateFilter}", dateFilterQueryString);
		if (teamMemberJourneyRequestDTO.getSelectedTeamMemberIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString += " and u.user_id in (:teamMemberIds) ";
		}

		finalQueryString += groupBy;
		return (String) executeUniqueResultQueryForVendor(finalQueryString, teamMemberJourneyRequestDTO);
	}

	@Override
	public String getTeamMembersLeadCountForVendorTeamMember(
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO) {
		String finalQueryString = allLeadCountQueryForVendorView;

		String dateFilterQueryString = frameDateFilterQuery(teamMemberJourneyRequestDTO, LEAD_DATE_COLUMN);
		finalQueryString += dateFilterQueryString;

		if (teamMemberJourneyRequestDTO.getSelectedTeamMemberIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString += "and xup.user_id in (:teamMemberIds) ";
		}
		return (String) executeUniqueResultQueryForVendor(finalQueryString, teamMemberJourneyRequestDTO);
	}

	@Override
	public String getTeamMembersDealCountForVendorTeamMember(
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO) {
		String finalQueryString = allDealCountQueryForVendorView;
		String dateFilterQueryString = frameDateFilterQuery(teamMemberJourneyRequestDTO, DEAL_DATE_COLUMN);
		finalQueryString += dateFilterQueryString;

		if (teamMemberJourneyRequestDTO.getSelectedTeamMemberIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString += " and xup.user_id in (:teamMemberIds) ";
		}
		return (String) executeUniqueResultQueryForVendor(finalQueryString, teamMemberJourneyRequestDTO);
	}

	@Override
	public String getTeamMembersAssetCountForVendorTeamMember(
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO) {
		String finalQueryString = allAssertsCountQueryForVendorView;
		String dateFilterQueryString = frameDateFilterQuery(teamMemberJourneyRequestDTO, "xd.created_time");
		finalQueryString += dateFilterQueryString;

		if (teamMemberJourneyRequestDTO.getSelectedTeamMemberIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString += " and xup.user_id in (:teamMemberIds) ";
		}
		return (String) executeUniqueResultQueryForVendor(finalQueryString, teamMemberJourneyRequestDTO);
	}

	@Override
	public TeamMemberAnalyticsDTO getTeamMembersTrackAndPlaybookCountForVendorTeamMember(
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO) {
		String finalQueryString = allTrackAndPlaybookCountQueryForVendorView;

		String dateFilterQueryString = frameDateFilterQuery(teamMemberJourneyRequestDTO, TRACK_DATE_COLUMN_FOR_VENDOR);
		finalQueryString += dateFilterQueryString;

		if (teamMemberJourneyRequestDTO.getSelectedTeamMemberIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString += " and xup.user_id   in (:teamMemberIds) ";
		}
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(finalQueryString);
		query.setParameter("vendorCompanyId", teamMemberJourneyRequestDTO.getVendorCompanyId());
		if (teamMemberJourneyRequestDTO.getSelectedTeamMemberIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedTeamMemberIds().isEmpty()) {
			query.setParameterList("teamMemberIds", teamMemberJourneyRequestDTO.getSelectedTeamMemberIds());
		}
		return (TeamMemberAnalyticsDTO) paginationUtil.getDto(TeamMemberAnalyticsDTO.class, query);
	}

	@Override
	public String getTeamMembersContactCountForVendorTeamMember(
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO) {

		String finalQueryString = allContactsQueryForVendorView;

		String dateFilterQueryString = frameDateFilterQuery(teamMemberJourneyRequestDTO, CONTACT_DATE_COLUMN);
		finalQueryString += dateFilterQueryString;

		if (teamMemberJourneyRequestDTO.getSelectedTeamMemberIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString += " and xup1.user_id  in (:teamMemberIds) ";
		}
		return (String) executeUniqueResultQueryForVendor(finalQueryString, teamMemberJourneyRequestDTO);
	}

	@Override
	public Map<String, Object> getVendorTeamMemberLaunchedCampaignDetails(Pagination pagination) {
		return null;
	}

	private String frameTeamMemberLaunchedCampaignDetailsQueryForVendorTeamMember(Pagination pagination) {
		return TeamMemberAnalyticsMDFRequetsQuery;
	}

	@Override
	public Map<String, Object> getVendorTeamMemberContactDetails(Pagination pagination) {
		HashMap<String, Object> map = new HashMap<>();
		String queryString = frameTeamMemberContactDetailsQueryForVendorTeamMember(pagination);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", pagination.getVendorCompanyId());
		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			query.setParameterList("teamMemberIds", pagination.getSelectedTeamMemberIds());
		}
		return paginationUtil.setScrollableAndGetList(pagination, map, query, ContactsDetailsDTO.class);
	}

	private String frameTeamMemberContactDetailsQueryForVendorTeamMember(Pagination pagination) {
		String finalQueryString = allTeamMemberAnalyticsContactDetailsQuery;
		String groupByQuery = allTeamMemberAnalyticsContactDetailsQueryGroupBy;
		String searchKey = pagination.getSearchKey();

		String dateFilterQueryString = frameDateFilterQuery(pagination, CONTACT_DATE_COLUMN);
		finalQueryString += dateFilterQueryString;

		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString += "and xup1.user_id  in (:teamMemberIds) ";
		}
		if (StringUtils.hasText(searchKey)) {
			finalQueryString += allTeamMemberAnalyticsContactDetailsSearchConditions.replace("searchKey", searchKey);
		}
		finalQueryString += groupByQuery;
		return finalQueryString;
	}

	@Override
	public Map<String, Object> getallPartnersDetailsForVendor(Pagination pagination) {
		HashMap<String, Object> map = new HashMap<>();
		String queryString = frameallPartnersDetailsQueryForVendor(pagination);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", pagination.getVendorCompanyId());
		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			query.setParameterList("teamMemberIds", pagination.getSelectedTeamMemberIds());
		}
		return paginationUtil.setScrollableAndGetList(pagination, map, query, AllPartnersDetailsDTO.class);
	}

	private String frameallPartnersDetailsQueryForVendor(Pagination pagination) {
		String finalQueryString = allTeamMemberAnalyticsallPartnersDetailsQuery;
		String searchKey = pagination.getSearchKey();

		String dateFilterQueryString = frameDateFilterQuery(pagination, CONTACT_DATE_COLUMN);
		finalQueryString += dateFilterQueryString;

		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString += "and xup1.user_id  in (:teamMemberIds) ";
		}
		if (StringUtils.hasText(searchKey)) {
			finalQueryString += allTeamMemberAnalyticsallPartnersDetailsSearchConditions.replace("searchKey",
					searchKey);
		}
		return finalQueryString;
	}

	private String frameRedistributedCampaignsAndLeadsCountQuery(
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO) {
		Integer companyId = userDao.getCompanyIdByUserId(teamMemberJourneyRequestDTO.getLoggedInUserId());
		String vendorCompanyIds = "vendorCompanyIds";
		String teamMemberIds = "teamMemberIds";
		String vendorCompanyId = "vendorCompanyId";
		String teamMemberId = "teamMemberId";
		String leadFilter = frameDateFilterQuery(teamMemberJourneyRequestDTO, LEAD_DATE_COLUMN);
		String campaignFilter = frameDateFilterQuery(teamMemberJourneyRequestDTO, "xc1.launch_time");
		String finalQueryString = "with a as (select  xup.email_id,xup.company_id , xup.user_id ,cast(count(distinct xl.id) as int) as leads "
				+ "from xt_lead xl  "
				+ "left join xt_company_profile xuc on xl.created_by_company_id = xuc.company_id  "
				+ "left join xt_user_profile xup on xl.created_by = xup.user_id  "
				+ "left join xt_user_role xur on xur.user_id = xup.user_id " + "and xur.user_id = xl.created_by "
				+ "where xl.created_by_company_id =" + companyId + vendorCompanyIds + teamMemberIds + leadFilter
				+ " and xur.role_id !=2  group by 1,2,3),"
				+ "b as (select distinct  xup1.email_id, xup1.company_id as company,xup1.user_id as user, "
				+ "cast(count (distinct xc1.campaign_id) as int) as \"redistributedCampaignsCount\"  "
				+ "from public.xt_campaign xc left join xt_user_profile xup on (xc.customer_id=xup.user_id)  "
				+ "left join xt_user_role xur on (xur.user_id=xup.user_id)   "
				+ "left join xt_company_profile xcp on (xup.company_id=xcp.company_id)  "
				+ "left join xt_campaign xc1 on (xc.campaign_id=xc1.parent_campaign_id)  "
				+ "left join xt_user_profile xup1 ON (xc1.customer_id = xup1.user_id)   "
				+ "left join xt_company_profile xcp1 ON (xup1.company_id = xcp1.company_id) "
				+ " where xup1.company_id=" + companyId + vendorCompanyId + teamMemberId + campaignFilter
				+ "  and xc1.vendor_organization_id is not null and xc1.is_launched= true "
				+ " and  xc1.is_nurture_campaign =  true  and xc1.parent_campaign_id is not null "
				+ " and xur.role_id !=2  and xcp.company_id != xcp1.company_id group by 1,2,3 order by 4 desc) "
				+ "select distinct case when a.email_id is null then b.email_id  when b.email_id is null then a.email_id else a.email_id end as \"emailId\","
				+ " coalesce (b.\"redistributedCampaignsCount\",0) as \"redistributedCampaignsCount\", "
				+ "case when a.leads is null then 0 else a.leads end as \"leadsCount\"  "
				+ "from a full join b on a.company_id = b.company and a.user_id = b.user";

		if (teamMemberJourneyRequestDTO.getSelectedVendorCompanyIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedVendorCompanyIds().isEmpty()) {
			finalQueryString = finalQueryString.replace(vendorCompanyIds,
					" and xl.created_for_company_id in (:vendorCompany) ");
			finalQueryString = finalQueryString.replace(vendorCompanyId, " and  xup.company_id in (:vendorCompany) ");
		} else if (teamMemberJourneyRequestDTO.isVanityUrlFilter()) {
			finalQueryString = finalQueryString.replace(vendorCompanyIds,
					" and xl.created_for_company_id =:vendorCompany ");
			finalQueryString = finalQueryString.replace(vendorCompanyId, " and  xup.company_id =:vendorCompany ");
		} else {
			finalQueryString = finalQueryString.replace(vendorCompanyIds, " ");
			finalQueryString = finalQueryString.replace(vendorCompanyId, " ");
		}

		if (teamMemberJourneyRequestDTO.getSelectedTeamMemberIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString = finalQueryString.replace(teamMemberIds, "  and xup.user_id in (:teamMember) ");
			finalQueryString = finalQueryString.replace(teamMemberId, " and xup1.user_id in (:teamMember) ");
		} else {
			finalQueryString = finalQueryString.replace(teamMemberIds, " ");
			finalQueryString = finalQueryString.replace(teamMemberId, " ");
		}

		return finalQueryString;
	}

	private String appendOrderByQuery(String filterType, String queryString) {
		String updatedQuery = "";
		if ("r".equals(filterType)) {
			updatedQuery = queryString + " " + redistributedCampaignsCountOrderByQuery;
		} else {
			updatedQuery = queryString + " " + leadsCountOrderByQuery;
		}
		return updatedQuery;
	}

	@Override
	public List<Object[]> findLeadsAndDealsCountForTeamMember(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO,
			String filterType) {
		String queryString = frameLeadsAndDealsCountQuery(teamMemberJourneyRequestDTO);
		Session session = sessionFactory.getCurrentSession();
		String updatedQuery = "";
		updatedQuery = "l".equals(filterType) ? queryString + " " + leadsOrderByQuery
				: queryString + " " + dealsOrderByQuery;
		SQLQuery query = session.createSQLQuery(updatedQuery);
		if (teamMemberJourneyRequestDTO.getSelectedVendorCompanyIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedVendorCompanyIds().isEmpty()) {
			query.setParameterList("vendorCompany", teamMemberJourneyRequestDTO.getSelectedVendorCompanyIds());
		}
		if (teamMemberJourneyRequestDTO.getSelectedTeamMemberIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedTeamMemberIds().isEmpty()) {
			query.setParameterList("teamMember", teamMemberJourneyRequestDTO.getSelectedTeamMemberIds());
		}
		if (teamMemberJourneyRequestDTO.isVanityUrlFilter()) {
			CompanyProfile cp = vanityURLDao
					.getCompanyProfileByCompanyProfileName(teamMemberJourneyRequestDTO.getVendorCompanyProfileName());
			Integer companyId = cp.getId();
			if (companyId != null) {
				query.setParameter("vendorCompany", companyId);
			}
		}
		return query.list();
	}

	private String frameLeadsAndDealsCountQuery(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO) {
		Integer companyId = userDao.getCompanyIdByUserId(teamMemberJourneyRequestDTO.getLoggedInUserId());
		String vendorCompanyIds = "vendorCompanyIds";
		String teamMemberIds = "teamMemberIds";
		String vendorCompanyId = "vendorCompanyId";
		String leadFilter = frameDateFilterQuery(teamMemberJourneyRequestDTO, LEAD_DATE_COLUMN);
		String dealFilter = frameDateFilterQuery(teamMemberJourneyRequestDTO, DEAL_DATE_COLUMN);
		String finalQueryString = "select distinct foo.email_id as emailId,case when sum(leads) is null then 0 else sum(leads) end as leads, \r\n"
				+ "case when sum(deals) is null then 0 else sum(deals) end as deals " + " from "
				+ "(select distinct xup.email_id,xl.created_by_company_id,count(distinct xl.id) as leads, "
				+ "cast(null as int) as deals,'leads' as data " + "from xt_company_profile xuc "
				+ "left join public.xt_lead xl on xuc.company_id = xl.created_by_company_id "
				+ "left join public.xt_user_profile xup on xup.user_id = xl.created_by "
				+ "left join xt_user_role xur on xur.user_id = xup.user_id " + "where xl.created_by_company_id = "
				+ companyId + vendorCompanyIds + teamMemberIds + leadFilter
				+ " and xur.role_id !=2 and xl.created_by_company_id != xl.created_for_company_id " + "group by 1,2 "
				+ "union all " + " select xup.email_id,xd.created_by_company_id, "
				+ " cast(null as int ) as leads,count(distinct xd.id) as deals, "
				+ "'deals' as data  from xt_company_profile xuc "
				+ " left join public.xt_deal xd on (xuc.company_id = xd.created_by_company_id) "
				+ "left join public.xt_user_profile xup on xup.user_id = xd.created_by "
				+ "left join xt_user_role xur on xur.user_id = xup.user_id  " + " where xd.created_by_company_id = "
				+ companyId + vendorCompanyId + teamMemberIds + dealFilter
				+ " and xur.role_id !=2  and  xd.created_by_company_id != xd.created_for_company_id group by 1,2)foo "
				+ " group by 1 ";

		if (teamMemberJourneyRequestDTO.getSelectedVendorCompanyIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedVendorCompanyIds().isEmpty()) {
			finalQueryString = finalQueryString.replace(vendorCompanyIds,
					" and xl.created_for_company_id  in (:vendorCompany) ");
			finalQueryString = finalQueryString.replace(vendorCompanyId,
					" and  xd.created_for_company_id in (:vendorCompany) ");
		} else if (teamMemberJourneyRequestDTO.isVanityUrlFilter()) {
			finalQueryString = finalQueryString.replace(vendorCompanyIds,
					" and xl.created_for_company_id  =:vendorCompany ");
			finalQueryString = finalQueryString.replace(vendorCompanyId,
					" and  xd.created_for_company_id =:vendorCompany ");
		} else {
			finalQueryString = finalQueryString.replace(vendorCompanyIds, " ");
			finalQueryString = finalQueryString.replace(vendorCompanyId, " ");
		}

		if (teamMemberJourneyRequestDTO.getSelectedTeamMemberIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString = finalQueryString.replace(teamMemberIds, "  and xup.user_id in (:teamMember) ");
		} else {
			finalQueryString = finalQueryString.replace(teamMemberIds, " ");
		}

		return finalQueryString;
	}

	@Override
	public List<Object[]> findAllLeadsAndDealsCountForTeamMember(
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO, String filterType) {
		String queryString = frameLeadsAndDealsCountQuery(teamMemberJourneyRequestDTO);
		Session session = sessionFactory.getCurrentSession();
		SQLQuery query = session.createSQLQuery(queryString);
		if (teamMemberJourneyRequestDTO.getSelectedVendorCompanyIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedVendorCompanyIds().isEmpty()) {
			query.setParameterList("vendorCompany", teamMemberJourneyRequestDTO.getSelectedVendorCompanyIds());
		}
		if (teamMemberJourneyRequestDTO.getSelectedTeamMemberIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedTeamMemberIds().isEmpty()) {
			query.setParameterList("teamMember", teamMemberJourneyRequestDTO.getSelectedTeamMemberIds());
		}
		if (teamMemberJourneyRequestDTO.isVanityUrlFilter()) {
			CompanyProfile cp = vanityURLDao
					.getCompanyProfileByCompanyProfileName(teamMemberJourneyRequestDTO.getVendorCompanyProfileName());
			Integer companyId = cp.getId();
			if (companyId != null) {
				query.setParameter("vendorCompany", companyId);
			}
		}
		return query.list();
	}

	@Override
	public Map<String, Object> getContactsDetailsForTeamMember(Pagination pagination) {
		HashMap<String, Object> map = new HashMap<>();
		String queryString = frameTeamMemberContactDetailsQueryForTeamMember(pagination);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("partnerCompanyId", pagination.getPartnerCompanyId());
		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			query.setParameterList("teamMemberIds", pagination.getSelectedTeamMemberIds());
		}
		return paginationUtil.setScrollableAndGetList(pagination, map, query, ContactsDetailsDTO.class);
	}

	private String frameTeamMemberContactDetailsQueryForTeamMember(Pagination pagination) {
		String finalQueryString = allTeamMemberAnalyticsContactDetailsQueryForPartnerTeamMember;
		String groupByQuery = allTeamMemberAnalyticsContactDetailsQueryGroupBy;
		String searchKey = pagination.getSearchKey();

		String dateFilterQueryString = frameDateFilterQuery(pagination, CONTACT_DATE_COLUMN);
		finalQueryString += dateFilterQueryString;

		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString += "and u.user_id  in (:teamMemberIds) ";
		}
		if (StringUtils.hasText(searchKey)) {
			finalQueryString += allTeamMemberAnalyticsContactDetailsForPartnerSearchConditions.replace("searchKey",
					searchKey);
		}
		finalQueryString += groupByQuery;
		return finalQueryString;
	}

	@Override
	public Map<String, Object> getCompanyDetailsForTeamMember(Pagination pagination) {
		HashMap<String, Object> map = new HashMap<>();
		String queryString = frameTeamMemberCompanyDetailsQuery(pagination);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("partnerCompanyId", pagination.getPartnerCompanyId());
		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			query.setParameterList("teamMemberIds", pagination.getSelectedTeamMemberIds());
		}
		return paginationUtil.setScrollableAndGetList(pagination, map, query, ContactsDetailsDTO.class);
	}

	private String frameTeamMemberCompanyDetailsQuery(Pagination pagination) {
		String finalQueryString = allTeamMemberAnalyticsCompanyDetailsQuery;
		String groupByQuery = allTeamMemberAnalyticsCompanyDetailsQueryGroupBy;
		String searchKey = pagination.getSearchKey();

		String dateFilterQueryString = frameDateFilterQuery(pagination, "xul.created_time");
		finalQueryString += dateFilterQueryString;

		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString += "and xup.user_id  in (:teamMemberIds) ";
		}
		if (StringUtils.hasText(searchKey)) {
			finalQueryString += allTeamMemberAnalyticsCompanyDetailsSearchConditions.replace("searchKey", searchKey);
		}
		finalQueryString += groupByQuery;
		return finalQueryString;
	}

	@Override
	public Map<String, Object> getVendorTeamMemberTrackDetails(Pagination pagination) {
		HashMap<String, Object> map = new HashMap<>();
		String queryString = frameVendorTeamMemberTrackDetailsQuery(pagination);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			query.setParameterList("teamMemberIds", pagination.getSelectedTeamMemberIds());
		}
		return paginationUtil.setScrollableAndGetList(pagination, map, query, PartnerJourneyTrackDetailsDTO.class);
	}

	private String frameVendorTeamMemberTrackDetailsQuery(Pagination pagination) {
		Integer companyId = userDao.getCompanyIdByUserId(pagination.getUserId());
		String teamMemberIds = "teamMemberIds";
		String searchQuery = " ";
		String dateFilter = frameDateFilterQuery(pagination, TRACK_DATE_COLUMN_FOR_VENDOR);
		String searchKey = pagination.getSearchKey();
		if (StringUtils.hasText(searchKey)) {
			searchQuery = allTracksUserWiseContentDetailsSearchConditionForTeamMember;
		}
		String finalQueryString = "with a as "
				+ "(select distinct xup1.email_id, xup1.user_id,xlt.id as \"id1\", concat(xup1.firstname , ' ' , xup1.lastname) as \"fullName\","
				+ "xcp.company_id as \"id\" ,xup1.email_id as \"emailId\", "
				+ "xcp.company_id ,xlt.title as \"title\", xlt.published_time as \"publishedOn\", "
				+ "sum(coalesce(xltv.progress, 0))/count(distinct xltv.user_id)  as \"progress\" "
				+ "from xt_learning_track xlt "
				+ "left join xt_learning_track_visibility xltv on xltv.learning_track_id=xlt.id "
				+ "left join xt_user_profile xup1 on xup1.user_id=xltv.created_by "
				+ "left join xt_user_profile xup on xup.user_id=xltv.user_id "
				+ "left join xt_company_profile xcp on xcp.company_id=xup1.company_id " + "where xlt.company_id= "
				+ companyId + teamMemberIds + dateFilter + " "
				+ "and xlt.type='TRACK' and xlt.is_published= true and xcp.company_name_status ='active' " + " "
				+ " and xup.status='APPROVE' " + searchQuery + " " + " group by 1,2,3,4,5,6,7,8), " + "b as "
				+ "(select distinct xup1.email_id, xup1.user_id,xlt.id,xup1.company_id , "
				+ "count(distinct xd.id)  as \"assetCount\", "
				+ "coalesce(count(distinct xltc.quiz_id), 0) as \"quizCount\" "
				+ "from xt_learning_track_visibility xltv "
				+ "left join xt_learning_track xlt on xltv.learning_track_id=xlt.id "
				+ "left join xt_learning_track_content xltc on xltc.learning_track_id=xlt.id "
				+ "left join xt_learning_track_content_partner_activity xltcp on xltcp.learning_track_visibility_id=xltv.id and xltcp.learning_track_content_id =xltc.id  "
				+ "left join xt_dam xd on xd.id = xltc.dam_id "
				+ "left join xt_user_profile xup1 on xup1.user_id=xltv.created_by " + "where xlt.company_id="
				+ companyId + teamMemberIds + " " + "and xlt.type='TRACK' and xlt.is_published= true "
				+ " group by 1,2,3,4) "
				+ "select distinct a.\"id\",a.\"emailId\",a.\"fullName\",a.\"title\",a.\"publishedOn\",cast(a.\"progress\" as int),b.\"assetCount\",b.\"quizCount\" "
				+ "from a left join b on a.\"id\"=b.company_id and a.\"id1\"=b.id ";

		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString = finalQueryString.replace(teamMemberIds, " and xup1.user_id in (:teamMemberIds) ");
		} else {
			finalQueryString = finalQueryString.replace(teamMemberIds, " ");
		}

		if (StringUtils.hasText(searchKey)) {
			finalQueryString = finalQueryString.replace("searchKey", searchKey);
		}

		return finalQueryString;
	}

	@Override
	public Map<String, Object> getVendorTeamMemberLeadDetails(Pagination pagination) {
		HashMap<String, Object> map = new HashMap<>();
		String queryString = frameVendorTeamMemberLeadDetailsQuery(pagination);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", pagination.getVendorCompanyId());
		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			query.setParameterList("teamMemberIds", pagination.getSelectedTeamMemberIds());
		}
		return paginationUtil.setScrollableAndGetList(pagination, map, query, LeadDto.class);
	}

	private String frameVendorTeamMemberLeadDetailsQuery(Pagination pagination) {
		String finalQueryString = allLeadDetailsQueryForVendorTeamMember;
		String searchQuery = allLeadDetailsSearchConditionsForVendorTeamMember;
		String orderByQuery = allLeadDetailsOrderByVendorTeamMember;
		String searchKey = pagination.getSearchKey();

		String dateFilterQueryString = frameDateFilterQuery(pagination, LEAD_DATE_COLUMN);
		finalQueryString += dateFilterQueryString;

		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString += "and xup.user_id   in (:teamMemberIds) ";
		}

		if (StringUtils.hasText(searchKey)) {
			finalQueryString += searchQuery.replace("searchKey", searchKey);
		}

		finalQueryString += orderByQuery;

		return finalQueryString;
	}

	@Override
	public Map<String, Object> getVendorTeamMemberDealDetails(Pagination pagination) {
		HashMap<String, Object> map = new HashMap<>();
		String queryString = frameVendorTeamMemberDealDetailsQuery(pagination);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", pagination.getVendorCompanyId());
		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			query.setParameterList("teamMemberIds", pagination.getSelectedTeamMemberIds());
		}
		return paginationUtil.setScrollableAndGetList(pagination, map, query, DealDto.class);
	}

	private String frameVendorTeamMemberDealDetailsQuery(Pagination pagination) {
		String finalQueryString = allDealDetailsQueryForVendorTeamMember;
		String searchQuery = allDealDetailsSearchConditionsForTeamMember;
		String orderBy = allDealDetailsOrderByTeamMember;

		String dateFilterQueryString = frameDateFilterQuery(pagination, DEAL_DATE_COLUMN);
		finalQueryString += dateFilterQueryString;

		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString += "and xup.user_id  in (:teamMemberIds) ";
		}

		String searchKey = pagination.getSearchKey();
		if (StringUtils.hasText(searchKey)) {
			finalQueryString += searchQuery.replace("searchKey", searchKey);
		}

		finalQueryString += orderBy;

		return finalQueryString;
	}

	@Override
	public Map<String, Object> getVendorTeamMemberMdfDetails(Pagination pagination) {
		HashMap<String, Object> map = new HashMap<>();
		String queryString = frameVendorTeamMemberMdfDetailsQuery(pagination);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", pagination.getVendorCompanyId());
		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			query.setParameterList("teamMemberIds", pagination.getSelectedTeamMemberIds());
		}
		return paginationUtil.setScrollableAndGetList(pagination, map, query, MdfAmountTilesDTO.class);
	}

	private String frameVendorTeamMemberMdfDetailsQuery(Pagination pagination) {
		String finalQueryString = allVendorteamMemberAnalyticsMDFQuery;
		String dateFilterQueryString = frameDateFilterQuery(pagination, TRACK_DATE_COLUMN_FOR_VENDOR);
		finalQueryString += dateFilterQueryString;
		String searchKey = pagination.getSearchKey();
		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString += "and a.user_id  in (:teamMemberIds) ";
		}
		if (StringUtils.hasText(searchKey)) {
			finalQueryString += TeamMemberAnalyticsMDFRequetsSearchConditions.replace("searchKey", searchKey);
		}
		return finalQueryString;
	}

	@Override
	public Map<String, Object> getVendorTeamMemberAssetsCount(Pagination pagination) {
		HashMap<String, Object> map = new HashMap<>();
		String queryString = frameVendorTeamMemberAssetsCountQuery(pagination);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", pagination.getVendorCompanyId());
		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			query.setParameterList("teamMemberIds", pagination.getSelectedTeamMemberIds());
		}
		return paginationUtil.setScrollableAndGetList(pagination, map, query, PartnerJourneyTrackDetailsDTO.class);
	}

	private String frameVendorTeamMemberAssetsCountQuery(Pagination pagination) {
		String finalQueryString = allVendorteamMemberAssetsCountQuery;
		String groupBy = allVendorteamMemberAssetsCountGroupBy;

		String dateFilterQueryString = frameDateFilterQuery(pagination, ASSSET_DATE_COLUMN_FOR_VENDOR);
		finalQueryString += dateFilterQueryString;

		String searchKey = pagination.getSearchKey();
		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString += "and xup.user_id  in (:teamMemberIds) ";
		}
		if (StringUtils.hasText(searchKey)) {
			finalQueryString += allAssetsCountSearchConditionsForTeamMember.replace("searchKey", searchKey);
		}
		finalQueryString += groupBy;
		return finalQueryString;
	}

	@Override
	public Map<String, Object> getVendorTeamMemberAssetsDetails(Pagination pagination) {
		HashMap<String, Object> map = new HashMap<>();
		String queryString = frameVendorTeamMemberAssetsDetailsQuery(pagination);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", pagination.getVendorCompanyId());
		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			query.setParameterList("teamMemberIds", pagination.getSelectedTeamMemberIds());
		}
		return paginationUtil.setScrollableAndGetList(pagination, map, query, PartnerJourneyTrackDetailsDTO.class);
	}

	private String frameVendorTeamMemberAssetsDetailsQuery(Pagination pagination) {
		String finalQueryString = allVendorteamMemberAssetsDetailsQuery;

		String dateFilterQueryString = frameDateFilterQuery(pagination, ASSSET_DATE_COLUMN_FOR_VENDOR);
		finalQueryString += dateFilterQueryString;

		String searchKey = pagination.getSearchKey();
		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString += "and xup.user_id  in (:teamMemberIds) ";
		}
		if (StringUtils.hasText(searchKey)) {
			finalQueryString += allAssetsDetailsSearchConditionsForTeamMember.replace("searchKey", searchKey);
		}
		return finalQueryString;
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

	private String frameDateFilterQuery(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO, String dateColumn) {
		String dateFilterQueryString = "";
		if (teamMemberJourneyRequestDTO.getFromDateFilter() != null
				&& teamMemberJourneyRequestDTO.getToDateFilter() != null) {
			dateFilterQueryString = " and " + dateColumn + " between  TO_TIMESTAMP('"
					+ teamMemberJourneyRequestDTO.getFromDateFilter()
					+ "', 'Dy Mon DD HH24:MI:SS ZZZ YYYY') and TO_TIMESTAMP('"
					+ teamMemberJourneyRequestDTO.getToDateFilter() + "', 'Dy Mon DD HH24:MI:SS ZZZ YYYY') ";
		}
		return dateFilterQueryString;
	}

	@Override
	public String getTeamMembersTrackAssetCount(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO) {
		String finalQueryString = allTrackAssetQueryForTeamMember;

		String dateFilterQueryString = frameDateFilterQuery(teamMemberJourneyRequestDTO, TRACK_DATE_COLUMN);
		finalQueryString += dateFilterQueryString;

		if (teamMemberJourneyRequestDTO.getSelectedVendorCompanyIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedVendorCompanyIds().isEmpty()) {
			finalQueryString += "and xlt.company_id in (:vendorCompanyIds) ";
		}
		if (teamMemberJourneyRequestDTO.getSelectedTeamMemberIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString += "and xup1.user_id in (:teamMemberIds) ";
		}
		if (teamMemberJourneyRequestDTO.isVanityUrlFilter()) {
			finalQueryString += "and xlt.company_id =:vendorCompanyId ";
		}
		return (String) executeUniqueResultQuery(finalQueryString, teamMemberJourneyRequestDTO);
	}

	@Override
	public String getVendorTeamMembersTrackAssetCount(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO) {
		String finalQueryString = allTrackAssetQueryForVendorTeamMember;
		String dateFilterQueryString = frameDateFilterQuery(teamMemberJourneyRequestDTO, TRACK_DATE_COLUMN_FOR_VENDOR);
		finalQueryString += dateFilterQueryString;

		if (teamMemberJourneyRequestDTO.getSelectedTeamMemberIds() != null
				&& !teamMemberJourneyRequestDTO.getSelectedTeamMemberIds().isEmpty()) {
			finalQueryString += " and xup.user_id in (:teamMemberIds) ";
		}
		return (String) executeUniqueResultQueryForVendor(finalQueryString, teamMemberJourneyRequestDTO);
	}

	@Override
	public Map<String, Object> getTeamMemberAssetsDetails(Pagination pagination) {
		HashMap<String, Object> map = new HashMap<>();
		String queryString = frameTeamMemberAssetDetailsQuery(pagination);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("partnerCompanyId", pagination.getPartnerCompanyId());
		if (pagination.getSelectedVendorCompanyIds() != null && !pagination.getSelectedVendorCompanyIds().isEmpty()) {
			query.setParameterList("vendorCompanyIds", pagination.getSelectedVendorCompanyIds());
		}

		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			query.setParameterList("teamMemberIds", pagination.getSelectedTeamMemberIds());
		}

		if (pagination.isVanityUrlFilter()) {
			Integer companyId = userDao.getCompanyIdByProfileName(utilDao.getPrmCompanyProfileName());
			if (companyId != null) {
				query.setParameter("vendorCompanyId", companyId);
			}
		}
		return paginationUtil.setScrollableAndGetList(pagination, map, query, PartnerJourneyTrackDetailsDTO.class);
	}

	private String frameTeamMemberAssetDetailsQuery(Pagination pagination) {
		String finalQueryString = allAssetDetailsQueryForTeamMember;
		String searchQuery = "";
		String teamMemberFilterQueryString = "";
		String vendorFilterQueryString = "";
		String vanityFilterQueryString = "";
		String searchKey = pagination.getSearchKey();
		String dateFilterQueryString = frameDateFilterQuery(pagination, ASSET_DATE_COLUMN);

		if (pagination.getSelectedVendorCompanyIds() != null && !pagination.getSelectedVendorCompanyIds().isEmpty()) {
			vendorFilterQueryString = "and xd.company_id in (:vendorCompanyIds) ";
		}

		if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
			teamMemberFilterQueryString = "and xup1.user_id  in (:teamMemberIds) ";
		}
		if (pagination.isVanityUrlFilter()) {
			vanityFilterQueryString = "and xd.company_id =:vendorCompanyId ";
		}

		if (StringUtils.hasText(searchKey)) {
			searchQuery = allAssetDetailsSearchQuery.replace("searchKey", searchKey);
		}

		finalQueryString = finalQueryString.replace("{teamMemberFilter}", teamMemberFilterQueryString);
		finalQueryString = finalQueryString.replace("{vendorFilter}", vendorFilterQueryString);
		finalQueryString = finalQueryString.replace("{vanityFilter}", vanityFilterQueryString);
		finalQueryString = finalQueryString.replace("{dateFilter}", dateFilterQueryString);
		finalQueryString = finalQueryString.replace("{searchKey}", searchQuery);

		return finalQueryString;
	}

}
