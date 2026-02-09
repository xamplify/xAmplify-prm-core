package com.xtremand.vanity.url.dao.hibernate;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.NullPrecedence;
import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.Pagination;
import com.xtremand.common.bom.Pagination.SORTINGORDER;
import com.xtremand.dashboard.buttons.bom.DashboardButton;
import com.xtremand.dashboard.buttons.dto.DashboardButtonsDTO;
import com.xtremand.formbeans.EmailTemplateDTO;
import com.xtremand.integration.dao.IntegrationDao;
import com.xtremand.team.dao.TeamDao;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.HibernateSQLQueryResultUtilDao;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.QueryParameterDTO;
import com.xtremand.util.dto.XamplifyConstants;
import com.xtremand.vanity.email.templates.bom.CustomDefaultEmailTemplate;
import com.xtremand.vanity.email.templates.bom.DefaultEmailTemplate;
import com.xtremand.vanity.email.templates.bom.DefaultEmailTemplateType;
import com.xtremand.vanity.url.dao.VanityURLDao;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;

@Repository("vanityURLDao")
@Transactional
public class HibernateVanityURLDao implements VanityURLDao {

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private UtilDao utilDao;

	@Autowired
	private TeamDao teamDao;

	@Autowired
	private UserDAO userDao;

	@Autowired
	private IntegrationDao integrationDao;

	@Autowired
	private HibernateSQLQueryResultUtilDao hibernateSQLQueryResultUtilDao;

	private static final String DASHBOARD_BUTTON_ID = "dashboardButtonId";

	private static final String COMPANY_PROFILE_ID = "companyProfile.id";

	private static final String TOTAL_RECORDS = XamplifyConstants.TOTAL_RECORDS;

	private static final String VENDOR_COMPANY_ID = "vendorCompanyId";

	private static final String DEFAULT_EMAIL_TEMPLATE_ID = "defaultEmailTemplate.id";

	@Override
	public CompanyProfile getCompanyProfileByCompanyProfileName(String companyProfileName) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(CompanyProfile.class);
		criteria.add(Restrictions.eq("companyProfileName", utilDao.getPrmCompanyProfileName()));
		return (CompanyProfile) criteria.uniqueResult();
	}

	@Override
	public boolean isUserBelongsToCompany(String emailId, Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(
				"select count(*) from xt_user_profile where email_id=:emailId and company_id=:companyId");
		query.setParameter("emailId", emailId);
		query.setParameter(XamplifyConstants.COMPANY_ID, companyId);
		int count = query.uniqueResult() != null ? ((BigInteger) query.uniqueResult()).intValue() : 0;
		return count > 0;
	}

	@Override
	public void saveDashboardButton(DashboardButton dashboardButton) {
		sessionFactory.getCurrentSession().save(dashboardButton);
	}

	@Override
	public void updateDashboardButton(DashboardButton dashboardButton) {
		sessionFactory.getCurrentSession().update(dashboardButton);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<DashboardButton> getVendorDashboardButtons(CompanyProfile companyProfile) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(DashboardButton.class);
		criteria.add(Restrictions.eq("companyProfile", companyProfile));
		return criteria.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getVendorDashboardButtons(Pagination pagination, String searchKey) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(DashboardButton.class);
		criteria.add(Restrictions.eq(COMPANY_PROFILE_ID, pagination.getVendorCompanyId()));

		/********************* Search Functionality **********************************/
		if (StringUtils.hasText(searchKey)) {
			searchKey = XamplifyUtils.escapeSingleQuotesForSearchQuery(searchKey);
			searchKey = XamplifyUtils.addBackSlashToSpecialCharacters(searchKey);
			Disjunction disjunction = Restrictions.disjunction();
			disjunction.add(Restrictions.ilike("buttonTitle", searchKey, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("buttonLink", searchKey, MatchMode.ANYWHERE));
			criteria.add(disjunction);
		}

		ScrollableResults scrollableResults = criteria.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		scrollableResults.close();

		/*********** Sort ************/
		Optional<Pagination> paginationObj = Optional.ofNullable(pagination);
		Optional<Integer> maxResultsObj = Optional.ofNullable(paginationObj.get().getMaxResults());
		Optional<Integer> pageIndexObj = Optional.ofNullable(paginationObj.get().getPageIndex());
		Optional<String> sortcolumnObj = Optional.ofNullable(paginationObj.get().getSortcolumn());
		if (maxResultsObj.isPresent() && pageIndexObj.isPresent()) {
			criteria.setFirstResult((pageIndexObj.get() * maxResultsObj.get()) - maxResultsObj.get());
			criteria.setMaxResults(maxResultsObj.get());
		}
		if (sortcolumnObj.isPresent()) {
			if (SORTINGORDER.ASC == SORTINGORDER.valueOf(paginationObj.get().getSortingOrder())) {
				criteria.addOrder(Order.asc(sortcolumnObj.get()).nulls(NullPrecedence.LAST));
			} else if (SORTINGORDER.DESC == SORTINGORDER.valueOf(paginationObj.get().getSortingOrder())) {
				criteria.addOrder(Order.desc(sortcolumnObj.get()).nulls(NullPrecedence.LAST));
			}
		} else {
			criteria.addOrder(Order.desc("timestamp").nulls(NullPrecedence.LAST));
		}

		Map<String, Object> dashboardButtonMap = new HashMap<>();
		dashboardButtonMap.put(TOTAL_RECORDS, totalRecords);

		criteria.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		criteria.setMaxResults(pagination.getMaxResults());

		List<DashboardButton> dbButtonsList = criteria.list();
		dashboardButtonMap.put("dbButtons", dbButtonsList);
		return dashboardButtonMap;
	}

	@Override
	public DashboardButton getDashboardButtonById(Integer id) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(DashboardButton.class);
		criteria.add(Restrictions.eq("id", id));
		return (DashboardButton) criteria.uniqueResult();
	}

	@Override
	public void deleteDashboardButtonById(Integer id) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "delete from xt_dashboard_buttons where id = " + id + "";
		session.createSQLQuery(sql).executeUpdate();
	}

	@Override
	public DefaultEmailTemplate getVanityDefaultEmailTemplateById(Integer id) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(DefaultEmailTemplate.class);
		criteria.add(Restrictions.eq("id", id));
		return (DefaultEmailTemplate) criteria.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getVanityDefaultEmailTemplates(Pagination pagination,
			VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(DefaultEmailTemplate.class);
		String vendorCompanyProfileName = utilDao.getPrmCompanyProfileName();
		Integer userId = pagination.getUserId();
		boolean isOnlyPartnerCompany = utilDao.isOnlyPartnerCompany(userId);
		boolean isPrm = utilDao.isPrmCompany(userId);
		boolean isTemplateExist = false;
		boolean isTeamMember = teamDao.isTeamMember(userId);

		searchCriteria(criteria, pagination.getSearchKey(), "name");
		setSort(pagination, criteria);
		if (isPrm) {
			Integer companyId = utilDao.getCompanyIdByUserId(userId, session);
			isTemplateExist = getPrmCustomDefaultCount(companyId);
		}
		if (vanityUrlDetailsDTO.isVendorLoggedInThroughOwnVanityUrl()) {
			addApproveAndRejectLeadTemplate(criteria, isPrm);
			if (isOnlyPartnerCompany) {
				customizeEmailTemplateRestrictions(criteria, "id");
				criteria.add(Restrictions.ne("id", 28));
			} else if (isPrm && isTemplateExist) {
				criteria.add(Restrictions.ne("type", DefaultEmailTemplateType.JOIN_PRM_COMPANY));
				criteria.add(Restrictions.ne("type", DefaultEmailTemplateType.ADD_SELF_LEAD));
				criteria.add(Restrictions.ne("type", DefaultEmailTemplateType.ADD_SELF_DEAL));
				criteria.add(Restrictions.ne("type", DefaultEmailTemplateType.UPDATE_SELF_LEAD));
				criteria.add(Restrictions.ne("type", DefaultEmailTemplateType.UPDATE_SELF_DEAL));
				criteria.add(Restrictions.ne("type", DefaultEmailTemplateType.ADD_LEAD));
				criteria.add(Restrictions.ne("type", DefaultEmailTemplateType.LEAD_UPDATE));
				criteria.add(Restrictions.ne("type", DefaultEmailTemplateType.LEAD_DETAILS_UPDATE));
				criteria.add(Restrictions.ne("type", DefaultEmailTemplateType.LEAD_STATUS_REMINDER_NOTIFICATION));
				hideJoinMyTeamTemplateForVersa(vendorCompanyProfileName, criteria);

			} else if (isPrm) {
				criteria.add(Restrictions.ne("type", DefaultEmailTemplateType.JOIN_VENDOR_COMPANY));
				criteria.add(Restrictions.ne("type", DefaultEmailTemplateType.ADD_LEAD));
				criteria.add(Restrictions.ne("type", DefaultEmailTemplateType.LEAD_UPDATE));
				criteria.add(Restrictions.ne("type", DefaultEmailTemplateType.ADD_SELF_LEAD));
				criteria.add(Restrictions.ne("type", DefaultEmailTemplateType.ADD_SELF_DEAL));
				criteria.add(Restrictions.ne("type", DefaultEmailTemplateType.UPDATE_SELF_LEAD));
				criteria.add(Restrictions.ne("type", DefaultEmailTemplateType.UPDATE_SELF_DEAL));
				criteria.add(Restrictions.ne("type", DefaultEmailTemplateType.LEAD_DETAILS_UPDATE));
				criteria.add(Restrictions.ne("type", DefaultEmailTemplateType.LEAD_STATUS_REMINDER_NOTIFICATION));
				hideJoinMyTeamTemplateForVersa(vendorCompanyProfileName, criteria);

			}
			if (isTeamMember) {
				criteria.add(Restrictions.ne("type", DefaultEmailTemplateType.JOIN_MY_TEAM));
				criteria.add(Restrictions.ne("type", DefaultEmailTemplateType.JOIN_VERSA_TEAM));
			}
		} else {
			customizeEmailTemplateRestrictions(criteria, "id");
			criteria.add(Restrictions.ne("id", 28));
		}
		List<Integer> usedDefaultTempalteIds = getDefaultTemplateIdsFromCustomTemplateByCompanyId(
				pagination.getCompanyId());
		if (usedDefaultTempalteIds != null && !usedDefaultTempalteIds.isEmpty()) {
			criteria.add(Restrictions.not(Restrictions.in("id", usedDefaultTempalteIds)));
		}

		String selectedTab = pagination.getCategory();
		myNotificationDefaultTab(criteria, selectedTab, isPrm);

		Map<String, Object> resultMap = new HashMap<>();
		List<DefaultEmailTemplate> list = criteria.list();
		criteria.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		criteria.setMaxResults(pagination.getMaxResults());
		resultMap.put(TOTAL_RECORDS, list.size());
		resultMap.put("totalTemplates", list);
		resultMap.put("defaultTemplates", criteria.list());
		return resultMap;
	}

	public Map<String, Object> getVanityDefaultEmailTemplatesForPartner(Pagination pagination,
			VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(DefaultEmailTemplate.class);
		Integer userId = pagination.getUserId();
		boolean isPrm = utilDao.isPrmCompany(userId);
		searchCriteria(criteria, pagination.getSearchKey(), "name");
		setSort(pagination, criteria);

		List<Integer> usedDefaultTempalteIds = getDefaultTemplateIdsFromCustomTemplateByCompanyId(
				pagination.getCompanyId());
		if (usedDefaultTempalteIds != null && !usedDefaultTempalteIds.isEmpty()) {
			criteria.add(Restrictions.not(Restrictions.in("id", usedDefaultTempalteIds)));
		}

		Integer companyId = userDao.getCompanyIdByUserId(userId);
		String activeIntegrationType = integrationDao.getActiveIntegrationTypeByCompanyId(companyId);
		String selectedTab = pagination.getCategory();

		partnerNotificationDefaultTab(criteria, selectedTab, isPrm, activeIntegrationType);

		Map<String, Object> resultMap = new HashMap<>();
		@SuppressWarnings("unchecked")
		List<DefaultEmailTemplate> list = criteria.list();
		criteria.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		criteria.setMaxResults(pagination.getMaxResults());
		resultMap.put(TOTAL_RECORDS, list.size());
		resultMap.put("totalTemplates", list);
		resultMap.put("defaultTemplates", criteria.list());
		return resultMap;
	}

	private void addApproveAndRejectLeadTemplate(Criteria criteria, boolean isPrm) {
		if (isPrm) {
			criteria.add(Restrictions.ne("type", DefaultEmailTemplateType.LEAD_APPROVE));
			criteria.add(Restrictions.ne("type", DefaultEmailTemplateType.LEAD_REJECT));
		} else {
			criteria.add(Restrictions.ne("type", DefaultEmailTemplateType.PRM_LEAD_APPROVE));
			criteria.add(Restrictions.ne("type", DefaultEmailTemplateType.PRM_LEAD_REJECT));
		}
	}

	/***** XNFR-805 *****/
	private void hideJoinMyTeamTemplateForVersa(String companyProfileName, Criteria criteria) {
		criteria.add(Restrictions.ne("type", DefaultEmailTemplateType.JOIN_MY_TEAM));
	}

	private void setSort(Pagination pagination, org.hibernate.Criteria criteria) {

		Optional<Pagination> paginationObj = Optional.ofNullable(pagination);

		if (paginationObj.isPresent()) {

			Optional<String> sortcolumnObj = Optional.ofNullable(paginationObj.get().getSortcolumn());

			if (sortcolumnObj.isPresent()) {
				if (SORTINGORDER.ASC == SORTINGORDER.valueOf(paginationObj.get().getSortingOrder())) {
					criteria.addOrder(Order.asc(sortcolumnObj.get()));
				} else if (SORTINGORDER.DESC == SORTINGORDER.valueOf(paginationObj.get().getSortingOrder())) {
					criteria.addOrder(Order.desc(sortcolumnObj.get()));
				}
			}
		} else {
			criteria.addOrder(Order.desc("id"));
		}
	}

	private void applySortingToCriteria(Pagination pagination, Criteria criteria) {
		if (pagination != null && pagination.getSortcolumn() != null && !pagination.getSortcolumn().isEmpty()) {
			String sortingOrder = pagination.getSortingOrder();

			if (!criteria.toString().contains("defaultEmailTemplate")) {
				criteria.createAlias("defaultEmailTemplate", "de");
			}

			String sortColumn = "de.name";

			criteria.addOrder(SORTINGORDER.DESC.name().equalsIgnoreCase(sortingOrder) ? Order.desc(sortColumn)
					: Order.asc(sortColumn));
		} else {
			criteria.addOrder(Order.desc("id"));
		}
	}

	private void customizeEmailTemplateRestrictions(Criteria criteria, String type) {
		criteria.add(Restrictions.ne(type, 2));
		criteria.add(Restrictions.ne(type, 4));
		criteria.add(Restrictions.ne(type, 5));
		criteria.add(Restrictions.ne(type, 6));
		criteria.add(Restrictions.ne(type, 7));
		criteria.add(Restrictions.ne(type, 8));
		criteria.add(Restrictions.ne(type, 9));
		criteria.add(Restrictions.ne(type, 10));
		criteria.add(Restrictions.ne(type, 11));
		criteria.add(Restrictions.ne(type, 12));
		criteria.add(Restrictions.ne(type, 13));
		criteria.add(Restrictions.ne(type, 14));
		criteria.add(Restrictions.ne(type, 15));
		criteria.add(Restrictions.ne(type, 16));
		criteria.add(Restrictions.ne(type, 17));
		criteria.add(Restrictions.ne(type, 18));
		criteria.add(Restrictions.ne(type, 19));
		criteria.add(Restrictions.ne(type, 20));
		criteria.add(Restrictions.ne(type, 21));
		criteria.add(Restrictions.ne(type, 22));
		criteria.add(Restrictions.ne(type, 23));
		criteria.add(Restrictions.ne(type, 24));
		criteria.add(Restrictions.ne(type, 25));
		criteria.add(Restrictions.ne(type, 26));
		criteria.add(Restrictions.ne(type, 27));
		criteria.add(Restrictions.ne(type, 30));
		criteria.add(Restrictions.ne(type, 31));
		criteria.add(Restrictions.ne(type, 32));
		criteria.add(Restrictions.ne(type, 33));
		criteria.add(Restrictions.ne(type, 34));
		criteria.add(Restrictions.ne(type, 44));
		criteria.add(Restrictions.ne(type, 43));
		criteria.add(Restrictions.ne(type, 45));
		criteria.add(Restrictions.ne(type, 47));
		criteria.add(Restrictions.ne(type, 29));
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getVanityEmailTemplates(CompanyProfile companyProfile, Pagination pagination,
			VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(CustomDefaultEmailTemplate.class);
		Integer userId = pagination.getUserId();
		Integer partnerCompanyId = userDao.getCompanyIdByUserId(userId);
		boolean isTeamMember = teamDao.isTeamMember(userId);
		boolean isSecondAdmin = pagination.isAdmin();

		searchCriteriaForCustomTemplates(criteria, pagination.getSearchKey(), "name", pagination.getCompanyId());
		applySortingToCriteria(pagination, criteria);
		if (vanityUrlDetailsDTO.isPartnerLoggedInThroughVanityUrl()) {
			customizeEmailTemplateRestrictions(criteria, DEFAULT_EMAIL_TEMPLATE_ID);
			criteria.add(Restrictions.ne(DEFAULT_EMAIL_TEMPLATE_ID, 28));
		} else if (isTeamMember) {
			criteria.add(Restrictions.ne(DEFAULT_EMAIL_TEMPLATE_ID, 28));
			criteria.add(Restrictions.ne(DEFAULT_EMAIL_TEMPLATE_ID, 1));
		} else if (isSecondAdmin) {
			criteria.add(Restrictions.eq(DEFAULT_EMAIL_TEMPLATE_ID, 1));
			criteria.add(Restrictions.ne(DEFAULT_EMAIL_TEMPLATE_ID, 28));
		}
		if (vanityUrlDetailsDTO.isPartnerLoggedInThroughVanityUrl()) {
			criteria.add(Restrictions.eq(COMPANY_PROFILE_ID, partnerCompanyId));
		} else {
			criteria.add(Restrictions.eq(COMPANY_PROFILE_ID, companyProfile.getId()));
		}

		Map<String, Object> resultMap = new HashMap<>();
		List<CustomDefaultEmailTemplate> list = criteria.list();
		resultMap.put(TOTAL_RECORDS, list.size());
		resultMap.put("customTemplates", criteria.list());
		criteria.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		criteria.setMaxResults(pagination.getMaxResults());
		return resultMap;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getVanityEmailTemplatesForPartner(CompanyProfile companyProfile, Pagination pagination,
			VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(CustomDefaultEmailTemplate.class);

		Integer userId = pagination.getUserId();
		Integer partnerCompanyId = userDao.getCompanyIdByUserId(userId);
		boolean isTeamMember = teamDao.isTeamMember(userId);
		boolean isSecondAdmin = pagination.isAdmin();
		boolean isPrm = utilDao.isPrmCompany(userId);

		searchCriteriaForCustomTemplates(criteria, pagination.getSearchKey(), "name", pagination.getCompanyId());
		applySortingToCriteria(pagination, criteria);
		String activeIntegrationType = integrationDao.getActiveIntegrationTypeByCompanyId(partnerCompanyId);
		if (vanityUrlDetailsDTO.isPartnerLoggedInThroughVanityUrl()) {
			customizeEmailTemplateRestrictions(criteria, DEFAULT_EMAIL_TEMPLATE_ID);
			criteria.add(Restrictions.ne(DEFAULT_EMAIL_TEMPLATE_ID, 28));
		} else if (isTeamMember) {
			criteria.add(Restrictions.ne(DEFAULT_EMAIL_TEMPLATE_ID, 28));
			criteria.add(Restrictions.ne(DEFAULT_EMAIL_TEMPLATE_ID, 1));
		} else if (isSecondAdmin) {
			criteria.add(Restrictions.eq(DEFAULT_EMAIL_TEMPLATE_ID, 1));
			criteria.add(Restrictions.ne(DEFAULT_EMAIL_TEMPLATE_ID, 28));
		}
		if (vanityUrlDetailsDTO.isPartnerLoggedInThroughVanityUrl()) {
			criteria.add(Restrictions.eq(COMPANY_PROFILE_ID, partnerCompanyId));
		} else {
			criteria.add(Restrictions.eq(COMPANY_PROFILE_ID, companyProfile.getId()));
		}

		String selectedTab = pagination.getCategory();
		partnerNotificationCustomTab(criteria, selectedTab, isPrm, activeIntegrationType);

		Map<String, Object> resultMap = new HashMap<>();
		List<CustomDefaultEmailTemplate> list = criteria.list();

		resultMap.put(TOTAL_RECORDS, list.size());
		resultMap.put("customTemplates", list);

		criteria.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		criteria.setMaxResults(pagination.getMaxResults());

		return resultMap;
	}

	@Override
	public void deleteVanityEmailTemplateById(Integer defaultEmailTemplateId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "delete from xt_custom_default_templates where default_email_template_id = "
				+ defaultEmailTemplateId + "";
		session.createSQLQuery(sql).executeUpdate();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<List<String>> listAllTemplateDuplicates(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();

		String sql1 = "SELECT xcdt.id, xcdt.subject  FROM xt_custom_default_templates AS xcdt WHERE xcdt.company_id = :companyId";
		Query query1 = session.createSQLQuery(sql1);
		query1.setParameter(XamplifyConstants.COMPANY_ID, companyId);
		List<String> subjects = query1.list();

		String sql2 = "SELECT name FROM xt_default_email_templates";
		Query query2 = session.createSQLQuery(sql2);
		List<String> names = query2.list();

		List<List<String>> combinedResults = new ArrayList<>();
		combinedResults.add(subjects);
		combinedResults.add(names);

		return combinedResults;

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getCustomTemplates(String name, Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "SELECT de.name, de.id " + "FROM xt_custom_default_templates cu "
				+ "JOIN xt_default_email_templates de ON cu.default_email_template_id = de.id "
				+ "WHERE de.name LIKE :name AND cu.companyId = :companyId";

		Query query = session.createSQLQuery(sql);
		query.setParameter("name", "%" + name + "%");
		query.setParameter("companyId", companyId);

		return query.list();
	}

	private org.hibernate.Criteria searchCriteriaForCustomTemplates(org.hibernate.Criteria criteria, String searchKey,
			String columnName, Integer companyId) {
		criteria.createAlias("defaultEmailTemplate", "de");

		if (StringUtils.hasText(searchKey) && !"null".equals(searchKey)) {
			searchKey = XamplifyUtils.escapeSingleQuotesForSearchQuery(searchKey);
			searchKey = XamplifyUtils.addBackSlashToSpecialCharacters(searchKey);

			criteria.add(Restrictions.like("de.name", "%" + searchKey + "%").ignoreCase());

		}
		return criteria;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getCustomTemplatesAsc(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "SELECT de.name, de.id " + "FROM xt_custom_default_templates cu "
				+ "JOIN xt_default_email_templates de ON cu.default_email_template_id = de.id "
				+ "WHERE cu.company_id = :companyId " + "ORDER BY de.name ASC";

		Query query = session.createSQLQuery(sql);
		query.setParameter("companyId", companyId);

		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getCustomTemplatesDesc(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "SELECT de.name, de.id " + "FROM xt_custom_default_templates cu "
				+ "JOIN xt_default_email_templates de ON cu.default_email_template_id = de.id "
				+ "WHERE cu.company_id = :companyId " + "ORDER BY de.name DESC";

		Query query = session.createSQLQuery(sql);
		query.setParameter("companyId", companyId);

		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getDefaultTemplateIdsFromCustomTemplateByCompanyId(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select distinct default_email_template_id from xt_custom_default_templates where company_id = :companyId";
		Query query = session.createSQLQuery(sql);
		query.setParameter("companyId", companyId);
		return query.list();
	}

	@Override
	public CustomDefaultEmailTemplate getVanityETByDefVanityETIdAndCompanyId(Integer defaultVanityETId,
			CompanyProfile companyProfile) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(CustomDefaultEmailTemplate.class);
		criteria.add(Restrictions.eq(DEFAULT_EMAIL_TEMPLATE_ID, defaultVanityETId));
		criteria.add(Restrictions.eq(COMPANY_PROFILE_ID, companyProfile.getId()));
		return (CustomDefaultEmailTemplate) criteria.uniqueResult();
	}

	@Override
	public DefaultEmailTemplate getVanityDefaultEmailTemplateByType(DefaultEmailTemplateType vanityEmailTemplateType) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(DefaultEmailTemplate.class);
		criteria.add(Restrictions.eq("type", vanityEmailTemplateType));
		return (DefaultEmailTemplate) criteria.uniqueResult();
	}

	@Override
	public boolean getPrmCustomDefaultCount(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select count(*) from xt_custom_default_templates where company_id = :companyId and  default_email_template_id = 4";
		Query query = session.createSQLQuery(sql);
		query.setParameter("companyId", companyId);
		Integer count = query.uniqueResult() != null ? ((BigInteger) query.uniqueResult()).intValue() : 0;
		return count > 0;

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> findExistingDashboardButtonsByCompanyId(Integer vendorCompanyId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(
				"select distinct LOWER(TRIM(title)) from xt_dashboard_buttons where vendor_company_id = :vendorCompanyId");
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(VENDOR_COMPANY_ID, vendorCompanyId));
		return (List<String>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public boolean isDashboardButtonPublished(Integer dashboardButtonId) {
		Session session = sessionFactory.getCurrentSession();
		String partnerGroupsQueryString = "select case when count(*)>0 then true else false end from xt_dashboard_buttons_partner_group_mapping where dashboard_button_id = :id";
		boolean isPublishedToPartnerGroup = (boolean) session.createSQLQuery(partnerGroupsQueryString)
				.setParameter("id", dashboardButtonId).uniqueResult();

		String partnerCompaniesQueryString = "select case when count(*)>0 then true else false end from xt_dashboard_buttons_partner_company_mapping where dashboard_button_id = :id";
		boolean isPublishedToPartnerCompany = (boolean) session.createSQLQuery(partnerCompaniesQueryString)
				.setParameter("id", dashboardButtonId).uniqueResult();

		return isPublishedToPartnerGroup || isPublishedToPartnerCompany;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findPublishedPartnerGroupIdsByDashboardButtonId(Integer dashboardButtonId) {
		if (XamplifyUtils.isValidInteger(dashboardButtonId)) {
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO.setQueryString(
					"select distinct xu.user_list_id from xt_dashboard_buttons_partner_group_mapping xdbp,xt_user_userlist xuul,xt_user_list xu\r\n"
							+ "where xdbp.dashboard_button_id = :dashboardButtonId and xu.user_list_id = xuul.user_list_id and xdbp.user_user_list_id = xuul.id");
			setDashboardButtonIdParameter(dashboardButtonId, hibernateSQLQueryResultRequestDTO);
			return (List<Integer>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
		} else {
			return Collections.emptyList();
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findPublishedPartnershipIdsByDashboardButtonId(Integer dashboardButtonId) {
		if (XamplifyUtils.isValidInteger(dashboardButtonId)) {
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO.setQueryString(
					"select distinct partnership_id from xt_dashboard_buttons_partner_company_mapping where dashboard_button_id = :dashboardButtonId");
			setDashboardButtonIdParameter(dashboardButtonId, hibernateSQLQueryResultRequestDTO);
			return (List<Integer>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public void updatePubishedStatusByDashboardButtonId(Integer dashboardButtonId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO
				.setQueryString("update xt_dashboard_buttons set is_publishing_in_progress = false where id = :id");
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("id", dashboardButtonId));
		hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findPublishedPartnerIdsByDashboardButtonId(Integer dashboardButtonId) {
		if (XamplifyUtils.isValidInteger(dashboardButtonId)) {
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO.setQueryString(
					"select distinct published_to from xt_dashboard_buttons_partner_company_mapping where dashboard_button_id = :dashboardButtonId");
			setDashboardButtonIdParameter(dashboardButtonId, hibernateSQLQueryResultRequestDTO);
			return (List<Integer>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
		} else {
			return Collections.emptyList();
		}
	}

	private void setDashboardButtonIdParameter(Integer dashboardButtonId,
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO) {
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(DASHBOARD_BUTTON_ID, dashboardButtonId));
	}

	@Override
	public void deletePartnerGroupIds(Set<Integer> publishedPartnerGroupIds, Integer dashboardButtonId) {
		if (XamplifyUtils.isNotEmptySet(publishedPartnerGroupIds) && XamplifyUtils.isValidInteger(dashboardButtonId)) {
			List<Integer> publishedPartnerGroupIdsList = XamplifyUtils.convertSetToList(publishedPartnerGroupIds);
			Session session = sessionFactory.getCurrentSession();
			List<Integer> allUserUserListIds = findAllUserUserListIds(publishedPartnerGroupIdsList, session);
			deleteUserUserListIdsFromDashboardPartnerGroupMappingTable(session, allUserUserListIds, dashboardButtonId);

		}

	}

	private void deleteUserUserListIdsFromDashboardPartnerGroupMappingTable(Session session,
			List<Integer> allUserUserListIds, Integer dashboardButtonId) {
		List<List<Integer>> chunkedUserUserListIds = XamplifyUtils.getChunkedList(allUserUserListIds);
		for (List<Integer> userUserListIds : chunkedUserUserListIds) {
			session.createSQLQuery(
					"delete from xt_dashboard_buttons_partner_group_mapping where user_user_list_id in (:userUserListIds) and dashboard_button_id = :dashboardButtonId")
					.setParameterList("userUserListIds", userUserListIds)
					.setParameter(DASHBOARD_BUTTON_ID, dashboardButtonId).executeUpdate();
		}
	}

	@SuppressWarnings("unchecked")
	private List<Integer> findAllUserUserListIds(List<Integer> publishedPartnerGroupIdsList, Session session) {
		List<Integer> allUserUserListIds = new ArrayList<>();
		List<List<Integer>> chunkedPartnerGroupIdsList = XamplifyUtils.getChunkedList(publishedPartnerGroupIdsList);
		for (List<Integer> chunkedPartnerGroupIds : chunkedPartnerGroupIdsList) {
			List<Integer> userUserListChunkedIds = session.createSQLQuery(
					"select distinct id from xt_user_userlist where user_list_id in  (:publishedPartnerGroupIds)")
					.setParameterList("publishedPartnerGroupIds", chunkedPartnerGroupIds).list();
			allUserUserListIds.addAll(userUserListChunkedIds);
		}
		return allUserUserListIds;
	}

	@Override
	public void deletePartnerIds(Set<Integer> publishedPartnerIds, Integer dashboardButtonId) {
		if (XamplifyUtils.isNotEmptySet(publishedPartnerIds) && XamplifyUtils.isValidInteger(dashboardButtonId)) {
			Session session = sessionFactory.getCurrentSession();
			List<Integer> publishedPartnerIdsList = XamplifyUtils.convertSetToList(publishedPartnerIds);
			List<List<Integer>> chunkedPublishedPartnerIdsList = XamplifyUtils.getChunkedList(publishedPartnerIdsList);
			for (List<Integer> chunkedPublishedPartnerIds : chunkedPublishedPartnerIdsList) {
				session.createSQLQuery(
						"delete from xt_dashboard_buttons_partner_company_mapping where published_to in (:publishedPartnerIds) and dashboard_button_id = :dashboardButtonId ")
						.setParameterList("publishedPartnerIds", chunkedPublishedPartnerIds)
						.setParameter(DASHBOARD_BUTTON_ID, dashboardButtonId).executeUpdate();
			}
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<DashboardButton> getVendorDashboardButtons() {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(DashboardButton.class);
		criteria.addOrder(Order.asc("timestamp"));
		return criteria.list();
	}

	@Override
	public Integer getMaxOrderIdByCompanyId(Integer vendorCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		return (Integer) session
				.createSQLQuery(
						"select max(order_id) from xt_dashboard_buttons where vendor_company_id = :vendorCompanyId")
				.setParameter(VENDOR_COMPANY_ID, vendorCompanyId).uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<DashboardButtonsDTO> findDashboardButtonsForPartnerView(Integer vendorCompanyId,
			Integer loggedInUserId) {
		String dashboardButtonsQueryStringPrefix = "select distinct xdb.id as \"id\", xdb.order_id as \"orderId\",xdb.title as \"buttonTitle\",xdb.sub_title as \"buttonSubTitle\",\r\n"
				+ "xdb.icon as \"buttonIcon\", xdb.link as \"buttonLink\", xdb.description as \"buttonDescription\", xdb.new_tab as \"openInNewTab\",\r\n"
				+ "case when xdb.new_tab then '_blank' else '_self' end as \"openInNewTabTarget\"";

		String partnerCompanyMappingQuery = dashboardButtonsQueryStringPrefix
				+ " from xt_dashboard_buttons xdb,xt_dashboard_buttons_partner_company_mapping xdbpcm,xt_partnership xp \r\n"
				+ "where xdb.vendor_company_id  = :vendorCompanyId and xdbpcm.dashboard_button_id  = xdb.id and xp.id = xdbpcm.partnership_id \r\n"
				+ "and xdbpcm.published_to  = :loggedInUserId and xdb.is_publishing_in_progress = false";

		String partnerGroupMappingQuery = dashboardButtonsQueryStringPrefix
				+ " from xt_dashboard_buttons xdb,xt_dashboard_buttons_partner_group_mapping xdbpgm,xt_partnership xp,xt_user_userlist xuu,xt_user_list xul where xdb.vendor_company_id  = :vendorCompanyId and \r\n"
				+ "xdbpgm.dashboard_button_id  = xdb.id and xp.id = xdbpgm.partnership_id \r\n"
				+ "and xuu.id  = xdbpgm.user_user_list_id and xp.partner_company_id  =(select company_id from xt_user_profile where user_id =  :loggedInUserId) and xul.company_id  = xp.partner_company_id and xdb.is_publishing_in_progress = false";

		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO
				.setQueryString(partnerCompanyMappingQuery + " UNION " + partnerGroupMappingQuery + " order by 2 asc");
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("loggedInUserId", loggedInUserId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(VENDOR_COMPANY_ID, vendorCompanyId));
		hibernateSQLQueryResultRequestDTO.setClassInstance(DashboardButtonsDTO.class);
		return (List<DashboardButtonsDTO>) hibernateSQLQueryResultUtilDao
				.returnDTOList(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public CompanyProfile getCompanyProfileByCustomDomain(String customDomain) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(CompanyProfile.class);
		criteria.add(Restrictions.eq("customDomain", customDomain));
		return (CompanyProfile) criteria.uniqueResult();
	}

	@Override
	public String getCompanyProfileNameByCustomDomain(String customDomain) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(
				"select lower(company_profile_name) from xt_company_profile where custom_domain = :customDomain");
		query.setParameter("customDomain", customDomain);
		return (String) query.uniqueResult();
	}

	private org.hibernate.Criteria searchCriteria(org.hibernate.Criteria criteria, String searchKey,
			String columnName) {
		if (StringUtils.hasText(searchKey) && !"null".equals(searchKey)) {
			searchKey = XamplifyUtils.escapeSingleQuotesForSearchQuery(searchKey);
			searchKey = XamplifyUtils.addBackSlashToSpecialCharacters(searchKey);
			Criterion templateNameCriteria = Restrictions.like(columnName, "%" + searchKey + "%").ignoreCase();
			criteria.add(templateNameCriteria);
		}
		return criteria;
	}

	/*** XNFR-832 ***/
	@Override
	public Integer getDefaultTemplateIdByType(String templateTypeInString) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session
				.createSQLQuery("select id from xt_default_email_templates where cast(type as text) = :type");
		query.setParameter("type", templateTypeInString);
		return (Integer) query.uniqueResult();
	}

	@Override
	public void updateCustomEmailTemplateImagePath(List<Integer> ids, String awsImagePath) {
		// TODO Auto-generated method stub
		Session session = sessionFactory.getCurrentSession();
		String querystr = "update CustomDefaultEmailTemplate set imagePath = :thumbnailPath  where id in (:ids)";
		Query query = session.createQuery(querystr);
		query.setParameter("thumbnailPath", awsImagePath);
		query.setParameterList("ids", ids);
		query.executeUpdate();
	}

	private void myNotificationDefaultTab(Criteria criteria, String selectedTab, boolean isPrm) {
		Set<DefaultEmailTemplateType> types = new HashSet<>();

		switch (selectedTab.toLowerCase()) {
		case "all":
			if (isPrm) {
				types.addAll(Arrays.asList(DefaultEmailTemplateType.DEAL_UPDATE, DefaultEmailTemplateType.ADD_DEAL,
						DefaultEmailTemplateType.PRM_ADD_LEAD, DefaultEmailTemplateType.PRM_UPDATED,
						DefaultEmailTemplateType.JOIN_MY_TEAM, DefaultEmailTemplateType.PARTNER_PDF_SIGNATURE_COMPLETED,
						DefaultEmailTemplateType.FORM_COMPLETED, DefaultEmailTemplateType.FORGOT_PASSWORD,
						DefaultEmailTemplateType.PRM_LEAD_STATUS_REMINDER_NOTIFICATION));
			}
			break;

		case "assets":
			types.add(DefaultEmailTemplateType.PARTNER_PDF_SIGNATURE_COMPLETED);
			break;

		case "opportunities":
			if (isPrm) {
				types.addAll(Arrays.asList(DefaultEmailTemplateType.DEAL_UPDATE, DefaultEmailTemplateType.ADD_DEAL,
						DefaultEmailTemplateType.PRM_ADD_LEAD, DefaultEmailTemplateType.PRM_UPDATED,
						DefaultEmailTemplateType.PRM_LEAD_STATUS_REMINDER_NOTIFICATION));
			} else {
				types.addAll(Arrays.asList(DefaultEmailTemplateType.ADD_LEAD, DefaultEmailTemplateType.LEAD_UPDATE,
						DefaultEmailTemplateType.ADD_DEAL, DefaultEmailTemplateType.DEAL_UPDATE,
						DefaultEmailTemplateType.ADD_SELF_LEAD, DefaultEmailTemplateType.ADD_SELF_DEAL,
						DefaultEmailTemplateType.UPDATE_SELF_LEAD, DefaultEmailTemplateType.UPDATE_SELF_DEAL,
						DefaultEmailTemplateType.LEAD_STATUS_REMINDER_NOTIFICATION));
			}
			break;

		case "team":
			types.add(DefaultEmailTemplateType.JOIN_MY_TEAM);
			break;

		case "forms":
			types.add(DefaultEmailTemplateType.FORM_COMPLETED);
			break;

		case "others":
			types.add(DefaultEmailTemplateType.FORGOT_PASSWORD);
			break;

		default:
			break;
		}

		if (XamplifyUtils.isNotEmptySet(types)) {
			criteria.add(Restrictions.in("type", types));
		}
	}

	private void partnerNotificationDefaultTab(Criteria criteria, String selectedTab, boolean isPrm,
			String activeIntegrationType) {
		Set<DefaultEmailTemplateType> types = new HashSet<>();

		switch (selectedTab.toLowerCase()) {
		case "all":
			if (isPrm) {
				types.addAll(Arrays.asList(DefaultEmailTemplateType.PRM_PARTNER_ADD_LEAD,
						DefaultEmailTemplateType.PRM_PARTNER_UPDATE_LEAD, DefaultEmailTemplateType.PARTNER_ADD_DEAL,
						DefaultEmailTemplateType.PARTNER_UPDATE_DEAL,
						DefaultEmailTemplateType.PARTNER_SIGNATURE_ENABLED, DefaultEmailTemplateType.PRM_LEAD_APPROVE,
						DefaultEmailTemplateType.PRM_LEAD_REJECT, DefaultEmailTemplateType.ACCOUNT_ACTIVATION,
						DefaultEmailTemplateType.PAGE_CAMPAIGN_CONTACT, DefaultEmailTemplateType.TEAM_MEMBER_PORTAL,
						DefaultEmailTemplateType.COMPANY_PROFILE_INCOMPLETE, DefaultEmailTemplateType.ASSET_PUBLISH,
						DefaultEmailTemplateType.PLAYBOOK_PUBLISH, DefaultEmailTemplateType.TRACK_PUBLISH,
						DefaultEmailTemplateType.SHARE_LEAD, DefaultEmailTemplateType.PARTNER_REMAINDER,
						DefaultEmailTemplateType.PARTNER_SIGNATURE_PENDING,
						DefaultEmailTemplateType.WELCOME_EMAIL_REMAINDER, DefaultEmailTemplateType.JOIN_PRM_COMPANY));
			} else {
				types.addAll(Arrays.asList(DefaultEmailTemplateType.PARTNER_ADD_DEAL,
						DefaultEmailTemplateType.PARTNER_UPDATE_DEAL, DefaultEmailTemplateType.PARTNER_ADD_LEAD,
						DefaultEmailTemplateType.PARTNER_UPDATE_LEAD,
						DefaultEmailTemplateType.PARTNER_SIGNATURE_ENABLED,
						DefaultEmailTemplateType.JOIN_VENDOR_COMPANY, DefaultEmailTemplateType.ACCOUNT_ACTIVATION,
						DefaultEmailTemplateType.LEAD_APPROVE, DefaultEmailTemplateType.LEAD_REJECT,
						DefaultEmailTemplateType.PAGE_CAMPAIGN_PARTNER, DefaultEmailTemplateType.PAGE_CAMPAIGN_CONTACT,
						DefaultEmailTemplateType.ONE_CLICK_LAUNCH, DefaultEmailTemplateType.SOCIAL_CAMPAIGN,
						DefaultEmailTemplateType.TO_SOCIAL_CAMPAIGN, DefaultEmailTemplateType.TEAM_MEMBER_PORTAL,
						DefaultEmailTemplateType.COMPANY_PROFILE_INCOMPLETE, DefaultEmailTemplateType.ASSET_PUBLISH,
						DefaultEmailTemplateType.PLAYBOOK_PUBLISH, DefaultEmailTemplateType.SHARE_LEAD,
						DefaultEmailTemplateType.TRACK_PUBLISH, DefaultEmailTemplateType.PARTNER_REMAINDER,
						DefaultEmailTemplateType.PARTNER_SIGNATURE_PENDING,
						DefaultEmailTemplateType.WELCOME_EMAIL_REMAINDER));
			}
			break;

		case "partner":
			if (isPrm) {
				types.addAll(Arrays.asList(DefaultEmailTemplateType.ACCOUNT_ACTIVATION,
						DefaultEmailTemplateType.COMPANY_PROFILE_INCOMPLETE, DefaultEmailTemplateType.PARTNER_REMAINDER,
						DefaultEmailTemplateType.TEAM_MEMBER_PORTAL, DefaultEmailTemplateType.WELCOME_EMAIL_REMAINDER,
						DefaultEmailTemplateType.JOIN_PRM_COMPANY));
			} else {
				types.addAll(Arrays.asList(DefaultEmailTemplateType.ACCOUNT_ACTIVATION,
						DefaultEmailTemplateType.COMPANY_PROFILE_INCOMPLETE, DefaultEmailTemplateType.PARTNER_REMAINDER,
						DefaultEmailTemplateType.TEAM_MEMBER_PORTAL, DefaultEmailTemplateType.WELCOME_EMAIL_REMAINDER,
						DefaultEmailTemplateType.JOIN_VENDOR_COMPANY));
			}

			break;

		case "assets":
			types.addAll(Arrays.asList(DefaultEmailTemplateType.ASSET_PUBLISH,
					DefaultEmailTemplateType.PARTNER_SIGNATURE_ENABLED,
					DefaultEmailTemplateType.PARTNER_SIGNATURE_PENDING));
			break;

		case "tracks":
			types.add(DefaultEmailTemplateType.TRACK_PUBLISH);
			break;

		case "playbooks":
			types.add(DefaultEmailTemplateType.PLAYBOOK_PUBLISH);
			break;

		case "opportunities":
			if (isPrm) {
				types.addAll(Arrays.asList(DefaultEmailTemplateType.PRM_PARTNER_ADD_LEAD,
						DefaultEmailTemplateType.PRM_PARTNER_UPDATE_LEAD, DefaultEmailTemplateType.PARTNER_ADD_DEAL,
						DefaultEmailTemplateType.PARTNER_UPDATE_DEAL, DefaultEmailTemplateType.PRM_LEAD_APPROVE,
						DefaultEmailTemplateType.PRM_LEAD_REJECT));
			} else {
				types.addAll(Arrays.asList(DefaultEmailTemplateType.LEAD_APPROVE, DefaultEmailTemplateType.LEAD_REJECT,
						DefaultEmailTemplateType.PARTNER_ADD_DEAL, DefaultEmailTemplateType.PARTNER_UPDATE_DEAL,
						DefaultEmailTemplateType.PARTNER_ADD_LEAD, DefaultEmailTemplateType.PARTNER_UPDATE_LEAD));
			}
			break;

		case "campaigns":
			types.addAll(Arrays.asList(DefaultEmailTemplateType.PAGE_CAMPAIGN_PARTNER,
					DefaultEmailTemplateType.PAGE_CAMPAIGN_CONTACT, DefaultEmailTemplateType.ONE_CLICK_LAUNCH,
					DefaultEmailTemplateType.SOCIAL_CAMPAIGN, DefaultEmailTemplateType.TO_SOCIAL_CAMPAIGN));
			break;

		case "sharedleads":
			types.add(DefaultEmailTemplateType.SHARE_LEAD);
			break;

		default:
			break;
		}

		if (XamplifyUtils.isNotEmptySet(types)) {
			criteria.add(Restrictions.in("type", types));
		}
	}

	private void partnerNotificationCustomTab(Criteria criteria, String selectedTab, boolean isPrm,
			String activeIntegrationType) {
		Set<Integer> templateIds = new HashSet<>();

		switch (selectedTab.toLowerCase()) {
		case "all":
			if (isPrm) {
				templateIds.addAll(
						Arrays.asList(36, 38, 40, 41, 42, 32, 33, 2, 11, 34, 27, 5, 6, 7, 8, 26, 46, 35, 37, 39, 14));
			} else {
				templateIds.addAll(Arrays.asList(39, 40, 37, 35, 41, 4, 2, 30, 31, 10, 11, 9, 12, 13, 27, 34, 5, 6, 7,
						8, 42, 26, 46));
			}
			break;
		case "partner":
			if (isPrm) {
				templateIds.addAll(Arrays.asList(2, 27, 26, 34, 46, 14));
			} else {
				templateIds.addAll(Arrays.asList(2, 27, 26, 34, 46, 4));
			}
			break;
		case "assets":
			templateIds.addAll(Arrays.asList(41, 7, 42));
			break;
		case "tracks":
			templateIds.add(5);
			break;
		case "playbooks":
			templateIds.add(6);
			break;
		case "opportunities":
			if (isPrm) {
				templateIds.addAll(Arrays.asList(32, 33, 35, 37, 36, 38, 39, 40));
			} else {
				templateIds.addAll(Arrays.asList(31, 30, 35, 39, 37, 40));
			}
			break;
		case "campaigns":
			templateIds.addAll(Arrays.asList(10, 11, 12, 9, 13));
			break;
		case "sharedleads":
			templateIds.add(8);
			break;
		default:
			break;
		}

		if (XamplifyUtils.isNotEmptySet(templateIds)) {
			criteria.add(Restrictions.in(DEFAULT_EMAIL_TEMPLATE_ID, templateIds));
		}
	}

	public EmailTemplateDTO getCustomEmailTemplate(Integer templateId, Integer userId) {
		String customQuery = "SELECT xcdt.html_body AS \"body\", " + "xcdt.subject AS \"subject\", "
				+ "xcp.company_logo AS \"companyLogoPath\", " + "xcp.website AS \"vendorOrganizationName\" "
				+ "FROM xt_custom_default_templates xcdt "
				+ "JOIN xt_user_profile xup ON xcdt.company_id = xup.company_id "
				+ "JOIN xt_company_profile xcp ON xup.company_id = xcp.company_id "
				+ "WHERE xcdt.default_email_template_id = :id " + "AND xup.user_id = :loggedInUserId";

		HibernateSQLQueryResultRequestDTO requestDTO = new HibernateSQLQueryResultRequestDTO();
		requestDTO.setQueryString(customQuery);
		requestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("id", templateId));
		requestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("loggedInUserId", userId));

		return (EmailTemplateDTO) hibernateSQLQueryResultUtilDao.getDto(requestDTO, EmailTemplateDTO.class);
	}

	public EmailTemplateDTO getDefaultEmailTemplateById(Integer templateId, Integer userId) {
		String defaultQuery = "SELECT det.html_body AS \"body\", " + "det.subject AS \"subject\", "
				+ "cp.company_logo AS \"companyLogoPath\", " + "cp.website AS \"vendorOrganizationName\" "
				+ "FROM xt_default_email_templates det " + "JOIN xt_company_profile cp ON cp.company_id = ( "
				+ "    SELECT up.company_id FROM xt_user_profile up WHERE up.user_id = :loggedInUserId " + ") "
				+ "WHERE det.id = :id";

		HibernateSQLQueryResultRequestDTO requestDTO = new HibernateSQLQueryResultRequestDTO();
		requestDTO.setQueryString(defaultQuery);
		requestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("id", templateId));
		requestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("loggedInUserId", userId));

		return (EmailTemplateDTO) hibernateSQLQueryResultUtilDao.getDto(requestDTO, EmailTemplateDTO.class);
	}

}
