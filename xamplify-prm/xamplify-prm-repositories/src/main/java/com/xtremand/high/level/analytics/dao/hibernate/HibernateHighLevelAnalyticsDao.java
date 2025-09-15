package com.xtremand.high.level.analytics.dao.hibernate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.high.level.analytics.dao.HighLevelAnalyticsDao;
import com.xtremand.highlevel.analytics.bom.DownloadRequest;
import com.xtremand.highlevel.analytics.bom.DownloadStatus;
import com.xtremand.highlevel.analytics.dto.HighLevelAnalyticsActivePartnersDto;
import com.xtremand.highlevel.analytics.dto.HighLevelAnalyticsDto;
import com.xtremand.highlevel.analytics.dto.HighLevelAnalyticsInactivePartnersDto;
import com.xtremand.highlevel.analytics.dto.HighLevelAnalyticsOnboardPartnersDto;
import com.xtremand.highlevel.analytics.dto.HighLevelAnalyticsPartnerAndTeamMemberDto;
import com.xtremand.highlevel.analytics.dto.HighLevelAnalyticsShareLeadsDto;
import com.xtremand.highlevel.analytics.dto.HighLevelAnalyticsTotalContactDto;
import com.xtremand.module.dao.ModuleDao;
import com.xtremand.util.XamplifyUtil;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.dto.TeamMemberFilterDTO;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;

@Repository
public class HibernateHighLevelAnalyticsDao implements HighLevelAnalyticsDao {

	static final Logger logger = LoggerFactory.getLogger(HibernateHighLevelAnalyticsDao.class);

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private UtilDao utilDao;

	@Autowired
	private ModuleDao moduleDao;

	@Autowired
	private XamplifyUtil xamplifyUtil;

	/****** High Level Analytics ***********/
	private static final String ACTIVE_PARTNERS = "Active ";

	private static final String INACTIVE_PARTNERS = "InActive ";

	private static final String USER_ID = "userId";

	@Value("${highlevelanalytics.activePartnersCount}")
	private String activePartnersCountQuery;

	@Value("${highlevelanalytics.activePartnersTeamMemberCountQuery}")
	private String activePartnersTeamMemberCountQuery;

	@Value("${highlevelanalytics.inActivePartnersCount}")
	private String inActivePartnersCountQuery;

	@Value("${highlevelanalytics.inActivePartnersTeamMemberCountQuery}")
	private String inActivePartnersTeamMemberCountQuery;

	@Value("${highlevelanalytics.totalCampaignLaunched}")
	private String totalCampaignLaunchedCountQuery;

	@Value("${highlevelanalytics.totalCampaignLaunchedForMarketingRole}")
	private String totalCampaignLaunchedForMarketingRole;

	@Value("${highlevelanalytics.totalThroughCampaignLaunchedQuery}")
	private String totalThroughCampaignLaunchedCountQuery;

	@Value("${highlevelanalytics.totalToCampaignLaunchedQuery}")
	private String totalToCampaignLaunchedCountQuery;

	@Value("${highlevelanalytics.totalRedistributedCampaigns}")
	private String totalRedistributedCampaignsCountQuery;

	@Value("${highlevelanalytics.totalRedistributedCampaignsForTeamMemberQuery}")
	private String totalRedistributedCampaignsForTeamMemberCountQuery;

	@Value("${highlevelanalytics.onBoardPartnersQuery}")
	private String onBoardPartnersCountQuery;

	@Value("${highlevelanalytics.onBoardPartnersTeamMemberQuery}")
	private String onBoardPartnersTeamMemberCountQuery;

	@Value("${highlevelanalytics.totalPartnersQuery}")
	private String totalPartnersCountQuery;

	@Value("${highlevelanalytics.totalPartnersTeamMemberQuery}")
	private String totalPartnersTeamMemberCountQuery;

	@Value("${highlevelanalytics.totalcontactsForOrgAdminOrOrgAdminAndPartnerQuery}")
	private String totalcontactsForOrgAdminOrOrgAdminAndPartnerCountQuery;

	@Value("${highlevelanalytics.totalcontactsForMarketingOrMarketingAndPartnerQuery}")
	private String totalcontactsForMarketingOrMarketingAndPartnerCountQuery;

	@Value("${highlevelanalytics.totalcontactsForVendorOrVendorAndPartnerQuery}")
	private String totalcontactsForVendorOrVendorAndPartnerCountQuery;

	@Value("${highlevelanalytics.totalcontactsForTeamMemberQuery}")
	private String totalcontactsForTeamMemberCountQuery;

	@Value("${highlevelanalytics.shareLeadsQuery}")
	private String shareLeadsCountQuery;

	@Value("${highlevelanalytics.shareLeadsForTeamMemberQuery}")
	private String shareLeadsForTeamMemberCountQuery;

	@Value("${highlevelanalytics.totalUsersQuery}")
	private String totalUsersCountQuery;

	@Value("${highlevelanalytics.totalToCampaignLaunchedQuery}")
	private String toCampaignCountQuery;

	@Value("${highlevelanalytics.totalThroughCampaignLaunchedQuery}")
	private String throughCampaignCountQuery;

	@Value("${highlevelanalytics.totalUsersDetailReport}")
	private String totalUsersDetailListQuery;

	@Value("${highlevelanalytics.partnerWhichIncludesPartnerAndTeamMember}")
	private String partnerAndTeamMemberQuery;

	@Value("${highlevelanalytics.partnerWhichIncludesPartnerAndTeamMemberForTeamMemberRole}")
	private String partnerWhichIncludesPartnerAndTeamMemberForTeamMemberRoleQuery;

	@Value("${highlevelanalytics.throughCampaignDetailreport}")
	private String throughCampaignDetailreportQuery;

	@Value("${highlevelanalytics.inactivePartners}")
	private String inactivePartnersQuery;

	@Value("${highlevelanalytics.inActicePartnerTeamMemberReportsQuery}")
	private String inActicePartnerTeamMemberReportsQuery;

	@Value("${highlevelanalytics.onboardPartners}")
	private String onboardPartnersQuery;

	@Value("${highlevelanalytics.activePartners}")
	private String activePartnersQuery;

	@Value("${highlevelanalytics.acticePartnerTeamMemberReportsQuery}")
	private String activePartnerTeamMemberReportsQuery;

	@Value("${highlevelanalytics.toCampaign}")
	private String toCampaignQuery;

	@Value("${highlevelanalytics.redistributedCampaign}")
	private String redistributedCampaignQuery;

	@Value("${highlevelanalytics.totalRedistributedCampaignsTeamMemberFilter}")
	private String totalRedistributedCampaignsTeamMemberFilterReportQuery;

	@Value("${highlevelanalytics.onBoardPartnerTeamMemberFilter}")
	private String onBoardPartnerTeamMemberFilterReportQuery;

	@Value("${highlevelanalytics.partnerQuery}")
	private String getPartnerCountInExcel;

	@Value("${highlevelanalytics.teamMemberQuery}")
	private String getTeamMemberCountInExcel;

	@Value("${highlevelanalytics.partnerLoggedAsTeamMember}")
	private String getPartnerLoggedAsTeamMemberQuery;

	@Value("${highlevelanalytics.teamMemberLoggedAsTeamMember}")
	private String getTeamMemberLoggedAsTeamMemberQuery;

	@Value("${highlevelanalytics.totalContactsForMarketing}")
	private String getTotalContactsListQuery;

	@Value("${highlevelanalytics.shareLeadsOfFirstDrill}")
	private String shareLeadsDetailReportForFirstDrillQuery;

	@Value("${highlevelanalytics.shareLeadsOfFirstDrillAsTeamMember}")
	private String shareLeadsDetailReportForFirstDrillAsTeamMemberQuery;

	@Value("${highlevelanalytics.shareLeadsOfSecondDrill}")
	private String shareLeadsDetailReportForSecondDrillQuery;

	@Value("${highlevelanalytics.shareLeadsOfSecondDrillAsTeamMember}")
	private String shareLeadsDetailReportForSecondDrillAsTeamMemberQuery;

	@Value("${highlevelanalytics.shareLeadsOfThirdDrill}")
	private String shareLeadsDetailReportForThirdDrillQuery;

	@Value("${highlevelanalytics.shareLeadsOfThirdDrillAsTeamMember}")
	private String shareLeadsDetailReportForThirdDrillAsTeamMemberQuery;

	/****** FIle ************/
	@Value("${separator}")
	private String sep;

	/*************** High Level Analytics **********************/
	@Override
	public XtremandResponse getActiveAndInActivePartnersForDonutChart(VanityUrlDetailsDTO vanityUrlDetailsDto) {
		try {
			XtremandResponse response = new XtremandResponse();
			Session session = sessionFactory.getCurrentSession();
			List<List<Object>> activeAndInActivePartnersData = new ArrayList<List<Object>>();
			Integer userId = vanityUrlDetailsDto.getUserId();
			boolean applyFilter = vanityUrlDetailsDto.isApplyFilter();
			boolean partnerShipIds = getPartnerShipIdsByUserId(userId);
			TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(userId, applyFilter,
					partnerShipIds);
			boolean applyTeamMemberFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
			String customName = moduleDao.findPartnersModuleCustomNameByUserId(userId);
			if (teamMemberFilterDTO.isEmptyFilter()) {
				response.setStatusCode(404);
			} else {
				getActiveAndInActivePartnersCountByFilter(response, session, activeAndInActivePartnersData, userId,
						customName, applyTeamMemberFilter);
			}
			response.setData(activeAndInActivePartnersData);
			return response;
		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex);
		}

	}

	private void getActiveAndInActivePartnersCountByFilter(XtremandResponse response, Session session,
			List<List<Object>> activeAndInActivePartnersData, Integer userId, String customName,
			boolean applyTeamMemberFilter) {
		String activePartnerQuery = applyTeamMemberFilter ? activePartnersTeamMemberCountQuery
				: activePartnersCountQuery;
		String inActivePartnerQuery = applyTeamMemberFilter ? inActivePartnersTeamMemberCountQuery
				: inActivePartnersCountQuery;
		Integer activePartnersCount = utilDao.getCountByUserId(session, userId, activePartnerQuery);
		Integer inActivePartnersCount = utilDao.getCountByUserId(session, userId, inActivePartnerQuery);
		xamplifyUtil.addItemsToArrayList(activeAndInActivePartnersData, ACTIVE_PARTNERS + customName,
				activePartnersCount);
		xamplifyUtil.addItemsToArrayList(activeAndInActivePartnersData, INACTIVE_PARTNERS + customName,
				inActivePartnersCount);
		Integer totalCount = activePartnersCount + inActivePartnersCount;
		response.setStatusCode(totalCount > 0 ? 200 : 404);
	}

	@Override
	public Integer getHighLevelAnalyticsDetailReportsForTotalPartners(VanityUrlDetailsDTO vanityUrlDetailsDto) {
		try {
			Session session = sessionFactory.getCurrentSession();
			Integer userId = vanityUrlDetailsDto.getUserId();
			boolean applyFilter = vanityUrlDetailsDto.isApplyFilter();
			boolean partnerShipIds = getPartnerShipIdsByUserId(userId);
			TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(userId, applyFilter,
					partnerShipIds);
			boolean applyTeamMemberFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
			Integer totalPartnersCount;
			if (teamMemberFilterDTO.isEmptyFilter()) {
				totalPartnersCount = 0;
			} else {
				String totalPartnerQuery = getQueryAfterApplyFilterCondition(applyTeamMemberFilter,
						totalPartnersTeamMemberCountQuery, totalPartnersCountQuery);
				totalPartnersCount = utilDao.getCountByUserId(session, userId, totalPartnerQuery);
			}

			return totalPartnersCount;
		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex);
		}
	}

	private String getQueryAfterApplyFilterCondition(boolean applyTeamMemberFilter, String teamMemberFilterQuery,
			String vanityLogInQuery) {
		return applyTeamMemberFilter ? teamMemberFilterQuery : vanityLogInQuery;
	}

	/********** find partnershipIds **************/
	private boolean getPartnerShipIdsByUserId(Integer loggedUserId) {
		List<Integer> parterCompanyId = utilDao.findPartnerCompanyIdsByLoggedInUserId(loggedUserId);
		if (parterCompanyId != null) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Integer getHighLevelAnalyticsDetailReportsForOnBoardPartners(VanityUrlDetailsDTO vanityUrlDetailsDto) {
		try {
			Session session = sessionFactory.getCurrentSession();
			Integer userId = vanityUrlDetailsDto.getUserId();
			boolean applyFilter = vanityUrlDetailsDto.isApplyFilter();
			boolean partnerShipIds = getPartnerShipIdsByUserId(userId);
			TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(userId, applyFilter,
					partnerShipIds);
			boolean applyTeamMemberFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
			Integer onBoardPartnerCount;
			if (teamMemberFilterDTO.isEmptyFilter()) {
				onBoardPartnerCount = 0;
			} else {
				String onBoardPartnerQuery = getQueryAfterApplyFilterCondition(applyTeamMemberFilter,
						onBoardPartnersTeamMemberCountQuery, onBoardPartnersCountQuery);
				onBoardPartnerCount = utilDao.getCountByUserId(session, userId, onBoardPartnerQuery);
			}

			return onBoardPartnerCount;
		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex);
		}

	}

	@Override
	public Integer getHighLevelAnalyticsDetailReportsForActivePartners(VanityUrlDetailsDTO vanityUrlDetailsDto) {
		try {
			Session session = sessionFactory.getCurrentSession();
			Integer userId = vanityUrlDetailsDto.getUserId();
			boolean applyFilter = vanityUrlDetailsDto.isApplyFilter();
			boolean partnerShipIds = getPartnerShipIdsByUserId(userId);
			TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(userId, applyFilter,
					partnerShipIds);
			boolean applyTeamMemberFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
			Integer activeParterCount;
			if (teamMemberFilterDTO.isEmptyFilter()) {
				return activeParterCount = 0;
			} else {
				String activePartnerQuery = getQueryAfterApplyFilterCondition(applyTeamMemberFilter,
						activePartnersTeamMemberCountQuery, activePartnersCountQuery);
				activeParterCount = utilDao.getCountByUserId(session, userId, activePartnerQuery);
			}

			return activeParterCount;
		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex);
		}

	}

	@Override
	public Integer getHighLevelAnalyticsDetailReportsForInActivePartners(VanityUrlDetailsDTO vanityUrlDetailsDto) {
		try {
			Session session = sessionFactory.getCurrentSession();
			Integer userId = vanityUrlDetailsDto.getUserId();
			boolean applyFilter = vanityUrlDetailsDto.isApplyFilter();
			boolean partnerShipIds = getPartnerShipIdsByUserId(userId);
			TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(userId, applyFilter,
					partnerShipIds);
			boolean applyTeamMemberFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
			Integer inActivePartnerCount;
			if (teamMemberFilterDTO.isEmptyFilter()) {
				return inActivePartnerCount = 0;
			} else {
				String inActivePartnerQuery = getQueryAfterApplyFilterCondition(applyTeamMemberFilter,
						inActivePartnersTeamMemberCountQuery, inActivePartnersCountQuery);
				inActivePartnerCount = utilDao.getCountByUserId(session, userId, inActivePartnerQuery);
			}

			return inActivePartnerCount;
		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex);
		}
	}

	@Override
	public Integer getHighLevelAnalyticsDetailReportsForShareLeadsTile(VanityUrlDetailsDTO vanityUrlDetailsDto) {
		try {
			Session session = sessionFactory.getCurrentSession();
			Integer userId = vanityUrlDetailsDto.getUserId();
			boolean applyFilter = vanityUrlDetailsDto.isApplyFilter();
			TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(userId, applyFilter, false);
			boolean applyTeamMemberFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
			Integer shereLeasCount;
			String shareLeadsQuery = getQueryAfterApplyFilterCondition(applyTeamMemberFilter, shareLeadsCountQuery,
					shareLeadsCountQuery);
			shereLeasCount = utilDao.getCountByUserId(session, userId, shareLeadsQuery);

			return shereLeasCount;
		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex);
		}
	}

	@Override
	public Integer getHighLevelAnalyticsDetailReportsForTotalUsersTile(VanityUrlDetailsDTO vanityUrlDetailsDto) {
		try {
			Session session = sessionFactory.getCurrentSession();
			Integer userId = vanityUrlDetailsDto.getUserId();
			boolean applyFilter = vanityUrlDetailsDto.isApplyFilter();
			TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(userId, applyFilter, true);
			boolean applyTeamMemberFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
			Integer totalUsersCount;
			if (teamMemberFilterDTO.isEmptyFilter()) {
				totalUsersCount = 0;
			} else {
				String totalUsersQuery = getQueryAfterApplyFilterCondition(applyTeamMemberFilter, totalUsersCountQuery,
						totalUsersCountQuery);
				totalUsersCount = utilDao.getCountByUserId(session, userId, totalUsersQuery);
			}

			return totalUsersCount;
		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	public List<HighLevelAnalyticsDto> getHighLevelAnalyticsTotalUsersDetailReport(Integer companyId,
			VanityUrlDetailsDTO vanityUrlDetailsDto) {
		try {
			Session session = sessionFactory.getCurrentSession();
			Integer userId = vanityUrlDetailsDto.getUserId();
			boolean applyFilter = vanityUrlDetailsDto.isApplyFilter();
			TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(userId, applyFilter, true);
			// boolean applyTeamMemberFilter =
			// teamMemberFilterDTO.isApplyTeamMemberFilter();
			List<HighLevelAnalyticsDto> totalUsersList = new ArrayList<>();
			if (teamMemberFilterDTO.isEmptyFilter()) {
				totalUsersList = new ArrayList<>();
			} else {
				SQLQuery query = session.createSQLQuery(totalUsersDetailListQuery);
				query.setParameter(USER_ID, vanityUrlDetailsDto.getUserId());
				totalUsersList.addAll(
						query.setResultTransformer(Transformers.aliasToBean(HighLevelAnalyticsDto.class)).list());
			}
			return totalUsersList;
		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	public List<HighLevelAnalyticsPartnerAndTeamMemberDto> getHighLevelAnalyticsPartnerWhichIncludesPartnerAndTeamMember(
			Integer userId, VanityUrlDetailsDTO vanityUrlDetailsDto) {
		try {
			Session session = sessionFactory.getCurrentSession();
			boolean applyFilter = vanityUrlDetailsDto.isApplyFilter();
			TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(userId, applyFilter, true);
			boolean applyTeamMemberFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
			List<HighLevelAnalyticsPartnerAndTeamMemberDto> partnerAndteamMemberList = new ArrayList<>();
			if (teamMemberFilterDTO.isEmptyFilter()) {
				partnerAndteamMemberList = new ArrayList<>();
			} else {
				String queryExcute = applyTeamMemberFilter
						? partnerWhichIncludesPartnerAndTeamMemberForTeamMemberRoleQuery
						: partnerAndTeamMemberQuery;
				SQLQuery query = session.createSQLQuery(queryExcute);
				query.setParameter(USER_ID, vanityUrlDetailsDto.getUserId());
				partnerAndteamMemberList.addAll(query
						.setResultTransformer(Transformers.aliasToBean(HighLevelAnalyticsPartnerAndTeamMemberDto.class))
						.list());
			}
			return partnerAndteamMemberList;
		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	public List<HighLevelAnalyticsInactivePartnersDto> getInactivePartners(Integer userId,
			VanityUrlDetailsDTO vanityUrlDetailsDto) {
		try {
			Session session = sessionFactory.getCurrentSession();
			boolean applyFilter = vanityUrlDetailsDto.isApplyFilter();
			TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(userId, applyFilter, true);
			boolean applyTeamMemberFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
			List<HighLevelAnalyticsInactivePartnersDto> inactivePartnerList = new ArrayList<>();
			if (teamMemberFilterDTO.isEmptyFilter()) {
				inactivePartnerList = new ArrayList<>();
			} else {
				String queryExcute = applyTeamMemberFilter ? inActicePartnerTeamMemberReportsQuery
						: inactivePartnersQuery;
				SQLQuery query = session.createSQLQuery(queryExcute);
				query.setParameter(USER_ID, vanityUrlDetailsDto.getUserId());
				inactivePartnerList.addAll(query
						.setResultTransformer(Transformers.aliasToBean(HighLevelAnalyticsInactivePartnersDto.class))
						.list());
			}
			return inactivePartnerList;
		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<HighLevelAnalyticsOnboardPartnersDto> getOnboardPartners(Integer userId,
			VanityUrlDetailsDTO vanityUrlDetailsDto) {
		try {
			Session session = sessionFactory.getCurrentSession();
			boolean applyFilter = vanityUrlDetailsDto.isApplyFilter();
			TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(userId, applyFilter, true);
			boolean applyTeamMemberFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
			List<HighLevelAnalyticsOnboardPartnersDto> onboardPartnerList = new ArrayList<>();
			if (teamMemberFilterDTO.isEmptyFilter()) {
				onboardPartnerList = new ArrayList<>();
			} else {
				String onboardPartnerQueryString = applyTeamMemberFilter ? onBoardPartnerTeamMemberFilterReportQuery
						: onboardPartnersQuery;
				SQLQuery query = session.createSQLQuery(onboardPartnerQueryString);
				query.setParameter(USER_ID, vanityUrlDetailsDto.getUserId());
				onboardPartnerList.addAll(
						query.setResultTransformer(Transformers.aliasToBean(HighLevelAnalyticsOnboardPartnersDto.class))
								.list());
			}
			return onboardPartnerList;
		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	public List<HighLevelAnalyticsActivePartnersDto> getActivePartners(Integer userId,
			VanityUrlDetailsDTO vanityUrlDetailsDto) {
		try {
			Session session = sessionFactory.getCurrentSession();
			Integer vUserId = vanityUrlDetailsDto.getUserId();
			boolean applyFilter = vanityUrlDetailsDto.isApplyFilter();
			TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(userId, applyFilter, true);
			boolean applyTeamMemberFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
			List<HighLevelAnalyticsActivePartnersDto> activePartnerList = new ArrayList<>();
			if (teamMemberFilterDTO.isEmptyFilter()) {
				activePartnerList = new ArrayList<>();
			} else {
				String activePArtnerQuery = applyTeamMemberFilter ? activePartnerTeamMemberReportsQuery
						: activePartnersQuery;
				SQLQuery query = session.createSQLQuery(activePArtnerQuery);
				query.setParameter(USER_ID, vUserId);
				activePartnerList.addAll(
						query.setResultTransformer(Transformers.aliasToBean(HighLevelAnalyticsActivePartnersDto.class))
								.list());
			}
			return activePartnerList;
		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex);
		}
	}

	@Override
	public boolean isDownloadRequestExists(Integer userId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String queryString = "select case when count(*)>0 then true else false end  from xt_download_request where requested_by=:userId and cast(status as text)!=:status";
			Query query = session.createSQLQuery(queryString);
			query.setParameter("userId", userId);
			query.setParameter("status", DownloadStatus.COMPLETED.name());
			return query.uniqueResult() != null ? (boolean) query.uniqueResult() : false;
		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception e) {
			throw new XamplifyDataAccessException(e);
		}
	}

	@Override
	public void updateDownloadRequestStatus(Integer id, DownloadStatus downloadStatus) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String queryString = "Update DownloadRequest set downloadStatus=:downloadStatus,updatedOn=:updatedOn where id = :id";
			Query query = session.createQuery(queryString);
			query.setParameter("id", id);
			query.setParameter("downloadStatus", downloadStatus);
			query.setParameter("updatedOn", new Date());
			query.executeUpdate();
		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception e) {
			throw new XamplifyDataAccessException(e);
		}
	}

	@Override
	public void updateDownloadFilePath(Integer id, String awsPath) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String queryString = "Update DownloadRequest set downloadStatus=:downloadStatus,updatedOn=:updatedOn,downloadPath=:downloadPath where id = :id";
			Query query = session.createQuery(queryString);
			query.setParameter("id", id);
			query.setParameter("downloadStatus", DownloadStatus.COMPLETED);
			query.setParameter("updatedOn", new Date());
			query.setParameter("downloadPath", awsPath);
			query.executeUpdate();
		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception e) {
			throw new XamplifyDataAccessException(e);
		}
	}

	@Override
	public DownloadRequest findById(Integer id) {
		try {
			Session session = sessionFactory.getCurrentSession();
			org.hibernate.Criteria criteria = session.createCriteria(DownloadRequest.class);
			criteria.add(Restrictions.eq("id", id));
			return (DownloadRequest) criteria.uniqueResult();
		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<DownloadRequest> findFailedRequests() {
		try {
			Session session = sessionFactory.getCurrentSession();
			org.hibernate.Criteria criteria = session.createCriteria(DownloadRequest.class);
			criteria.add(Restrictions.eq("downloadStatus", DownloadStatus.FAILED));
			List<DownloadRequest> request = (List<DownloadRequest>) criteria.list();
			return request;
		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex);
		}

	}

	@Override
	public Integer getPartnerCountInExcel(VanityUrlDetailsDTO vanityUrlDetailsDto) {
		try {
			Session session = sessionFactory.getCurrentSession();
			Integer userId = vanityUrlDetailsDto.getUserId();
			boolean applyFilter = vanityUrlDetailsDto.isApplyFilter();
			TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(userId, applyFilter, true);
			boolean applyTeamMemberFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
			Integer totalPartnersCount;
			if (teamMemberFilterDTO.isEmptyFilter()) {
				totalPartnersCount = 0;
			} else {
				String totalUsersQuery = getQueryAfterApplyFilterCondition(applyTeamMemberFilter,
						getPartnerLoggedAsTeamMemberQuery, getPartnerCountInExcel);
				totalPartnersCount = utilDao.getCountByUserId(session, userId, totalUsersQuery);
			}
			return totalPartnersCount;
		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex);
		}
	}

	@Override
	public Integer getTeamMemberCountInExcel(VanityUrlDetailsDTO vanityUrlDetailsDto) {
		try {
			Session session = sessionFactory.getCurrentSession();
			Integer userId = vanityUrlDetailsDto.getUserId();
			boolean applyFilter = vanityUrlDetailsDto.isApplyFilter();
			TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(userId, applyFilter, true);
			boolean applyTeamMemberFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
			Integer totalPartnersCount;
			if (teamMemberFilterDTO.isEmptyFilter()) {
				totalPartnersCount = 0;
			} else {
				String totalUsersQuery = getQueryAfterApplyFilterCondition(applyTeamMemberFilter,
						getTeamMemberLoggedAsTeamMemberQuery, getTeamMemberCountInExcel);
				totalPartnersCount = utilDao.getCountByUserId(session, userId, totalUsersQuery);
			}
			return totalPartnersCount;
		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<HighLevelAnalyticsTotalContactDto> getTotalContacts(VanityUrlDetailsDTO vanityUrlDetailsDto) {
		try {
			Session session = sessionFactory.getCurrentSession();
			Integer userId = vanityUrlDetailsDto.getUserId();
			boolean applyFilter = vanityUrlDetailsDto.isApplyFilter();
			TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(userId, applyFilter, true);
			boolean applyTeamMemberFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
			List<HighLevelAnalyticsTotalContactDto> totalContactsList = new ArrayList<>();
			String totalContactsQuery = applyTeamMemberFilter ? getTotalContactsListQuery : getTotalContactsListQuery;
			SQLQuery query = session.createSQLQuery(totalContactsQuery);
			query.setParameter(USER_ID, userId);
			totalContactsList.addAll(query
					.setResultTransformer(Transformers.aliasToBean(HighLevelAnalyticsTotalContactDto.class)).list());
			return totalContactsList;
		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<HighLevelAnalyticsShareLeadsDto> getShareLeads(VanityUrlDetailsDTO vanityUrlDetailsDto) {
		try {
			Session session = sessionFactory.getCurrentSession();
			Integer userId = vanityUrlDetailsDto.getUserId();
			boolean applyFilter = vanityUrlDetailsDto.isApplyFilter();
			TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(userId, applyFilter, true);
			boolean applyTeamMemberFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
			List<HighLevelAnalyticsShareLeadsDto> shareLeadsList = new ArrayList<>();
			String shareLeadsQuery = applyTeamMemberFilter ? shareLeadsDetailReportForFirstDrillQuery
					: shareLeadsDetailReportForFirstDrillQuery;
			SQLQuery query = session.createSQLQuery(shareLeadsQuery);
			query.setParameter(USER_ID, userId);
			shareLeadsList.addAll(
					query.setResultTransformer(Transformers.aliasToBean(HighLevelAnalyticsShareLeadsDto.class)).list());
			return shareLeadsList;
		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<HighLevelAnalyticsShareLeadsDto> getShareLeadsForSecondDrill(VanityUrlDetailsDTO vanityUrlDetailsDto) {
		try {
			Session session = sessionFactory.getCurrentSession();
			Integer userId = vanityUrlDetailsDto.getUserId();
			boolean applyFilter = vanityUrlDetailsDto.isApplyFilter();
			TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(userId, applyFilter, true);
			boolean applyTeamMemberFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
			List<HighLevelAnalyticsShareLeadsDto> shareLeadsList = new ArrayList<>();
			String shareLeadsQuery = applyTeamMemberFilter ? shareLeadsDetailReportForSecondDrillQuery
					: shareLeadsDetailReportForSecondDrillQuery;
			SQLQuery query = session.createSQLQuery(shareLeadsQuery);
			query.setParameter(USER_ID, userId);
			shareLeadsList.addAll(
					query.setResultTransformer(Transformers.aliasToBean(HighLevelAnalyticsShareLeadsDto.class)).list());

			return shareLeadsList;
		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<HighLevelAnalyticsShareLeadsDto> getShareLeadsForThirdDrill(VanityUrlDetailsDTO vanityUrlDetailsDto) {
		// TODO Auto-generated method stub
		try {
			Session session = sessionFactory.getCurrentSession();
			Integer userId = vanityUrlDetailsDto.getUserId();
			boolean applyFilter = vanityUrlDetailsDto.isApplyFilter();
			TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(userId, applyFilter, true);
			boolean applyTeamMemberFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
			List<HighLevelAnalyticsShareLeadsDto> shareLeadsList = new ArrayList<>();
			String shareLeadsQuery = applyTeamMemberFilter ? shareLeadsDetailReportForThirdDrillQuery
					: shareLeadsDetailReportForThirdDrillQuery;
			SQLQuery query = session.createSQLQuery(shareLeadsQuery);
			query.setParameter(USER_ID, userId);
			shareLeadsList.addAll(
					query.setResultTransformer(Transformers.aliasToBean(HighLevelAnalyticsShareLeadsDto.class)).list());
			return shareLeadsList;
		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex);
		}
	}

}