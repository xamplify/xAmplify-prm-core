package com.xtremand.company.dao.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.company.dao.CompanyProfileDao;
import com.xtremand.company.dto.EmailNotificationSettingsDTO;
import com.xtremand.util.PaginationUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.HibernateSQLQueryResultUtilDao;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.dto.CompanyDetailsDTO;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.QueryParameterDTO;

@Repository
/***** XNFR-326 ***/
public class HibernateCompanyProfileDao implements CompanyProfileDao {

	private static final String COMPANY_NAME = "companyName";

	private static final String COMPANY_ID = "companyId";

	private static final String WHERE_ID_COMPANY_ID = " where id = :companyId";

	private static final String PLAYBOOK_PUBLISH_VENDOR_EMAIL_NOTIFICATION = "playbookPublishVendorEmailNotification";

	private static final String PLAYBOOK_PUBLISHED_EMAIL_NOTIFICATION = "playbookPublishedEmailNotification";

	private static final String TRACK_PUBLISH_VENDOR_EMAIL_NOTIFICATION = "trackPublishVendorEmailNotification";

	private static final String TRACK_PUBLISHED_EMAIL_NOTIFICATION = "trackPublishedEmailNotification";

	private static final String ASSET_PUBLISH_VENDOR_EMAIL_NOTIFICATION = "assetPublishVendorEmailNotification";

	private static final String ASSET_PUBLISHED_EMAIL_NOTIFICATION = "assetPublishedEmailNotification";

	private static final String IS_ASSET_PUBLISHED_EMAIL_NOTIFICATION = "is_asset_published_email_notification";

	private static final String IS_TRACK_PUBLISHED_EMAIL_NOTIFICATION = "is_track_published_email_notification";

	private static final String IS_PLAYBOOK_PUBLISHED_EMAIL_NOTIFICATION = "is_playbook_published_email_notification";

	private static final String IS_DASHBOARD_BUTTON_PUBLISHED_EMAIL_NOTIFICATION = "is_dashboard_button_published_email_notification";

	private static final String IS_DASHBOARD_BANNER_PUBLISHED_EMAIL_NOTIFICATION = "is_dashboard_banner_published_email_notification";

	private static final String IS_NEWS_AND_ANNOUNCEMENTS_PUBLISHED_EMAIL_NOTIFICATION = "is_news_and_announcements_published_email_notification";

	private static final String PARTNER_ONBOARD_VENDOR_EMAIL_NOTIFICATION = "partner_onboard_vendor_email_notification";

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private PaginationUtil paginationUtil;

	@Autowired
	private HibernateSQLQueryResultUtilDao HibernateSQLQueryResultUtilDao;
	
	@Autowired
	private UtilDao utilDao;

	@Override
	public EmailNotificationSettingsDTO getEmailNotificationSettings(Integer companyId) {
		if (companyId != null && companyId > 0) {
			Session session = sessionFactory.getCurrentSession();
			Criteria criteria = session.createCriteria(CompanyProfile.class, "company");
			criteria.add(Restrictions.eq("company.id", companyId))
					.setProjection(Projections.distinct(Projections.projectionList()
							.add(Projections.property("company.assetPublishedEmailNotification"),
									ASSET_PUBLISHED_EMAIL_NOTIFICATION)
							.add(Projections.property("company.assetPublishVendorEmailNotification"),
									ASSET_PUBLISH_VENDOR_EMAIL_NOTIFICATION)
							.add(Projections.property("company.trackPublishedEmailNotification"),
									TRACK_PUBLISHED_EMAIL_NOTIFICATION)
							.add(Projections.property("company.trackPublishVendorEmailNotification"),
									TRACK_PUBLISH_VENDOR_EMAIL_NOTIFICATION)
							.add(Projections.property("company.playbookPublishedEmailNotification"),
									PLAYBOOK_PUBLISHED_EMAIL_NOTIFICATION)
							.add(Projections.property("company.playbookPublishVendorEmailNotification"),
									PLAYBOOK_PUBLISH_VENDOR_EMAIL_NOTIFICATION)
							.add(Projections.property("company.notifyPartners"), "notifyPartners")
							.add(Projections.property("company.dashboardButtonsEmailNotification"),
									"dashboardButtonsEmailNotification")
							.add(Projections.property("company.dashboardButtonPublishVendorEmailNotification"),
									"dashboardButtonPublishVendorEmailNotification")
							.add(Projections.property("company.dashboardBannersEmailNotification"),
									"dashboardBannersEmailNotification")
							.add(Projections.property("company.newsAndAnnouncementsEmailNotification"),
									"newsAndAnnouncementsEmailNotification")
							.add(Projections.property("company.partnerOnBoardVendorEmailNotification"),
									"partnerOnBoardVendorEmailNotification")

					));
			EmailNotificationSettingsDTO emailNotificationSettingsDTO = (EmailNotificationSettingsDTO) paginationUtil
					.getDtoByCriteria(criteria, EmailNotificationSettingsDTO.class);
			if (emailNotificationSettingsDTO != null) {
				return emailNotificationSettingsDTO;
			} else {
				return new EmailNotificationSettingsDTO();
			}
		} else {
			return new EmailNotificationSettingsDTO();
		}

	}

	@Override
	public void updateEmailNotificationSettings(Integer companyId,
			EmailNotificationSettingsDTO emailNotificationSettingsDTO) {
		if (companyId != null && companyId > 0 && emailNotificationSettingsDTO != null) {
			Session session = sessionFactory.getCurrentSession();
			String hQLString = "Update CompanyProfile set notifyPartners = :notifyPartners, assetPublishedEmailNotification=:assetPublishedEmailNotification,"
					+ " trackPublishedEmailNotification=:trackPublishedEmailNotification, playbookPublishedEmailNotification=:playbookPublishedEmailNotification,"
					+ " dashboardButtonsEmailNotification=:dashboardButtonsEmailNotification, dashboardBannersEmailNotification=:dashboardBannersEmailNotification,"
					+ " newsAndAnnouncementsEmailNotification=:newsAndAnnouncementsEmailNotification,"
					+ " assetPublishVendorEmailNotification=:assetPublishVendorEmailNotification,"
					+ " trackPublishVendorEmailNotification=:trackPublishVendorEmailNotification,"
					+ " playbookPublishVendorEmailNotification=:playbookPublishVendorEmailNotification,"
					+ " dashboardButtonPublishVendorEmailNotification=:dashboardButtonPublishVendorEmailNotification, "
					+ " partnerOnBoardVendorEmailNotification = :partnerOnBoardVendorEmailNotification "
					+ WHERE_ID_COMPANY_ID;
			Query query = session.createQuery(hQLString);
			query.setParameter(COMPANY_ID, companyId);
			query.setParameter("notifyPartners", emailNotificationSettingsDTO.isNotifyPartners());
			query.setParameter(ASSET_PUBLISHED_EMAIL_NOTIFICATION,
					emailNotificationSettingsDTO.isAssetPublishedEmailNotification());
			query.setParameter(ASSET_PUBLISH_VENDOR_EMAIL_NOTIFICATION,
					emailNotificationSettingsDTO.isAssetPublishVendorEmailNotification());
			query.setParameter(TRACK_PUBLISHED_EMAIL_NOTIFICATION,
					emailNotificationSettingsDTO.isTrackPublishedEmailNotification());
			query.setParameter(TRACK_PUBLISH_VENDOR_EMAIL_NOTIFICATION,
					emailNotificationSettingsDTO.isTrackPublishVendorEmailNotification());
			query.setParameter(PLAYBOOK_PUBLISHED_EMAIL_NOTIFICATION,
					emailNotificationSettingsDTO.isPlaybookPublishedEmailNotification());
			query.setParameter(PLAYBOOK_PUBLISH_VENDOR_EMAIL_NOTIFICATION,
					emailNotificationSettingsDTO.isPlaybookPublishVendorEmailNotification());
			query.setParameter("dashboardButtonsEmailNotification",
					emailNotificationSettingsDTO.isDashboardButtonsEmailNotification());
			query.setParameter("dashboardButtonPublishVendorEmailNotification",
					emailNotificationSettingsDTO.isDashboardButtonPublishVendorEmailNotification());
			query.setParameter("dashboardBannersEmailNotification",
					emailNotificationSettingsDTO.isDashboardBannersEmailNotification());
			query.setParameter("newsAndAnnouncementsEmailNotification",
					emailNotificationSettingsDTO.isNewsAndAnnouncementsEmailNotification());
			query.setParameter("partnerOnBoardVendorEmailNotification",
					emailNotificationSettingsDTO.isPartnerOnBoardVendorEmailNotification());
			query.executeUpdate();
		}

	}

	@Override
	public boolean isAssetPublishedEmailNotificationByUserId(Integer userId) {
		return isSendEmailNotification(userId, IS_ASSET_PUBLISHED_EMAIL_NOTIFICATION);
	}

	@Override
	public boolean isTrackPublishedEmailNotificationByUserId(Integer userId) {
		return isSendEmailNotification(userId, IS_TRACK_PUBLISHED_EMAIL_NOTIFICATION);
	}

	@Override
	public boolean isPlaybookPublishedEmailNotificationByUserId(Integer userId) {
		return isSendEmailNotification(userId, IS_PLAYBOOK_PUBLISHED_EMAIL_NOTIFICATION);
	}

	private boolean isSendEmailNotification(Integer userId, String columnName) {
		Session session = sessionFactory.getCurrentSession();
		String sqlString = "select c." + columnName + " from xt_user_profile u,xt_company_profile c\r\n"
				+ " where u.user_id = :userId and u.company_id = c.company_id";
		SQLQuery sqlQuery = session.createSQLQuery(sqlString);
		sqlQuery.setParameter("userId", userId);
		return sqlQuery.uniqueResult() != null && (boolean) sqlQuery.uniqueResult();
	}

	@Override
	public boolean isAssetPublishedEmailNotificationByCompanyProfileName(String companyProfileName) {
		return isSendEmailNotificationByCompanyProfileName(companyProfileName, IS_ASSET_PUBLISHED_EMAIL_NOTIFICATION);
	}

	@Override
	public boolean isTrackPublishedEmailNotificationByCompanyProfileName(String companyProfileName) {
		return isSendEmailNotificationByCompanyProfileName(companyProfileName, IS_TRACK_PUBLISHED_EMAIL_NOTIFICATION);
	}

	@Override
	public boolean isPlaybookPublishedEmailNotificationByCompanyProfileName(String companyProfileName) {
		return isSendEmailNotificationByCompanyProfileName(companyProfileName,
				IS_PLAYBOOK_PUBLISHED_EMAIL_NOTIFICATION);
	}

	private boolean isSendEmailNotificationByCompanyProfileName(String companyProfileName, String columnName) {
		Session session = sessionFactory.getCurrentSession();
		String sqlString = "select " + columnName
				+ " from xt_company_profile where company_profile_name = :companyProfileName";
		SQLQuery sqlQuery = session.createSQLQuery(sqlString);
		sqlQuery.setParameter("companyProfileName", utilDao.getPrmCompanyProfileName());
		return sqlQuery.uniqueResult() != null && (boolean) sqlQuery.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean companyNameExists(String companyName) {
		Session session = sessionFactory.getCurrentSession();
		Criteria crit = session.createCriteria(CompanyProfile.class);
		crit.add(Restrictions.ilike(COMPANY_NAME, companyName.trim(), MatchMode.EXACT));
		List<CompanyProfile> results = crit.list();
		return !results.isEmpty();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean companyNameExists(String companyName, Integer partnerCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria crit = session.createCriteria(CompanyProfile.class);
		crit.add(Restrictions.ilike(COMPANY_NAME, companyName.trim(), MatchMode.EXACT));
		if (partnerCompanyId != null && partnerCompanyId > 0) {
			crit.add(Restrictions.ne("id", partnerCompanyId));
		}
		List<CompanyProfile> results = crit.list();
		return !results.isEmpty();
	}

	@Override
	public boolean isDashboardButtonPublishedEmailNotificationByUserId(Integer userId) {
		return isSendEmailNotification(userId, IS_DASHBOARD_BUTTON_PUBLISHED_EMAIL_NOTIFICATION);
	}

	@Override
	public boolean isDashboardButtonPublishedEmailNotificationByCompanyProfileName(String companyProfileName) {
		return isSendEmailNotificationByCompanyProfileName(companyProfileName,
				IS_DASHBOARD_BUTTON_PUBLISHED_EMAIL_NOTIFICATION);
	}

	@Override
	public boolean isDashboardBannerPublishedEmailNotificationByUserId(Integer userId) {
		return isSendEmailNotification(userId, IS_DASHBOARD_BANNER_PUBLISHED_EMAIL_NOTIFICATION);
	}

	@Override
	public boolean isDashboardBannerPublishedEmailNotificationByCompanyProfileName(String companyProfileName) {
		return isSendEmailNotificationByCompanyProfileName(companyProfileName,
				IS_DASHBOARD_BANNER_PUBLISHED_EMAIL_NOTIFICATION);
	}

	@Override
	public boolean isNewsAndAnnouncementsPublishedEmailNotificationByUserId(Integer userId) {
		return isSendEmailNotification(userId, IS_NEWS_AND_ANNOUNCEMENTS_PUBLISHED_EMAIL_NOTIFICATION);
	}

	@Override
	public boolean isNewsAndAnnouncementsPublishedEmailNotificationByCompanyProfileName(String companyProfileName) {
		return isSendEmailNotificationByCompanyProfileName(companyProfileName,
				IS_NEWS_AND_ANNOUNCEMENTS_PUBLISHED_EMAIL_NOTIFICATION);
	}

	@Override
	public boolean isPartnerOnBoardVendorEmailNotificationEnabledByCompanyId(Integer companyId) {
		String columnName = PARTNER_ONBOARD_VENDOR_EMAIL_NOTIFICATION;
		Session session = sessionFactory.getCurrentSession();
		String sqlString = "select " + columnName + " from xt_company_profile  where company_id =:companyId";
		SQLQuery sqlQuery = session.createSQLQuery(sqlString);
		sqlQuery.setParameter(COMPANY_ID, companyId);
		return sqlQuery.uniqueResult() != null && (boolean) sqlQuery.uniqueResult();
	}

	@Override
	public void turnOffEmailNotificationSettingsOptionForAssetsModuleByCompanyId(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String hQLString = "Update CompanyProfile set  assetPublishedEmailNotification=:assetPublishedEmailNotification,assetPublishVendorEmailNotification=:assetPublishVendorEmailNotification"
				+ WHERE_ID_COMPANY_ID;
		Query query = session.createQuery(hQLString);
		query.setParameter(COMPANY_ID, companyId);
		query.setParameter(ASSET_PUBLISHED_EMAIL_NOTIFICATION, false);
		query.setParameter(ASSET_PUBLISH_VENDOR_EMAIL_NOTIFICATION, false);
		query.executeUpdate();
	}

	@Override
	public void turnOffEmailNotificationSettingsOptionForTracksModuleByCompanyId(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String hQLString = "Update CompanyProfile set  trackPublishedEmailNotification=:trackPublishedEmailNotification,"
				+ "trackPublishVendorEmailNotification=:trackPublishVendorEmailNotification" + WHERE_ID_COMPANY_ID;
		Query query = session.createQuery(hQLString);
		query.setParameter(COMPANY_ID, companyId);
		query.setParameter(TRACK_PUBLISHED_EMAIL_NOTIFICATION, false);
		query.setParameter(TRACK_PUBLISH_VENDOR_EMAIL_NOTIFICATION, false);
		query.executeUpdate();

	}

	@Override
	public void turnOffEmailNotificationSettingsOptionForPlayBooksModuleByCompanyId(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String hQLString = "Update CompanyProfile set  playbookPublishedEmailNotification=:playbookPublishedEmailNotification,"
				+ "playbookPublishVendorEmailNotification=:playbookPublishVendorEmailNotification"
				+ WHERE_ID_COMPANY_ID;
		Query query = session.createQuery(hQLString);
		query.setParameter(COMPANY_ID, companyId);
		query.setParameter(PLAYBOOK_PUBLISHED_EMAIL_NOTIFICATION, false);
		query.setParameter(PLAYBOOK_PUBLISH_VENDOR_EMAIL_NOTIFICATION, false);
		query.executeUpdate();

	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean companyNameExists(Integer companyId, String companyName) {
		Session session = sessionFactory.getCurrentSession();
		String hql = "FROM CompanyProfile cp WHERE REPLACE(LOWER(cp.companyName), ' ', '') = REPLACE(LOWER(:companyName), ' ', '') ";
		if (XamplifyUtils.isValidInteger(companyId)) {
			hql = hql + " and id != :companyId ";
		}
		Query query = session.createQuery(hql);
		if (XamplifyUtils.isValidInteger(companyId)) {
			query.setParameter(COMPANY_ID, companyId);
		}
		query.setParameter(COMPANY_NAME, companyName);
		List<CompanyProfile> results = query.list();
		return !results.isEmpty();
	}

	@Override
	public CompanyDetailsDTO findCompanyDetailsByLoggedInUserId(Integer loggedInUserId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(
				"select c.company_name as \"companyName\",c.company_profile_name as \"companyProfileName\",c.website as \"companyUrl\" from xt_company_profile c, xt_user_profile u\r\n"
						+ "where u.company_id = c.company_id and u.user_id = :userId");
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("userId", loggedInUserId));
		return (CompanyDetailsDTO) HibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO,
				CompanyDetailsDTO.class);
	}

	@Override
	public boolean isDuplicateCompanyNameExistWithAddedAdminCompanyId(Integer addedAdminCompanyId, String companyName) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(
				"select case when count(*)>0 then true else false end from xt_company_profile WHERE added_admin_company_id = "+ addedAdminCompanyId +" AND company_name = '"+ companyName +"' ");
		return HibernateSQLQueryResultUtilDao.returnBoolean(hibernateSQLQueryResultRequestDTO);
	}

}
