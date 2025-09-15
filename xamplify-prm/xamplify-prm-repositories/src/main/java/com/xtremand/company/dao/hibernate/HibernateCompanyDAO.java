package com.xtremand.company.dao.hibernate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.xtremand.common.bom.Pagination;
import com.xtremand.company.bom.Company;
import com.xtremand.company.dao.CompanyDAO;
import com.xtremand.company.dto.CompanyDTO;
import com.xtremand.util.PaginationUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.HibernateSQLQueryResultUtilDao;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.QueryParameterDTO;
import com.xtremand.util.dto.SortColumnDTO;
import com.xtremand.util.dto.UserUserListDTO;
import com.xtremand.util.dto.XamplifyConstants;

@Repository
public class HibernateCompanyDAO implements CompanyDAO {

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private PaginationUtil paginationUtil;

	@Value("${createdTime.property.name}")
	private String createdTimePropertyName;

	@Value("${findCompaniesQuery}")
	private String findCompaniesQuery;

	@Value("${findCompaniesSearch}")
	private String findCompaniesSearch;

	@Value("${findCompaniesGroupBy}")
	private String findCompaniesGroupBy;

	@Value("${findCompanyCounts}")
	private String findCompanyCounts;
	
	@Autowired
	private HibernateSQLQueryResultUtilDao hibernateSqlQueryResultUtilDao;

	@SuppressWarnings("unchecked")
	@Override
	public List<CompanyDTO> getCompaniesForDropdown(Integer companyId) {
		String sqlString = "select id, name from xt_company where company_id = :companyId";
		Query query = sessionFactory.getCurrentSession().createSQLQuery(sqlString);
		query.setParameter(XamplifyConstants.COMPANY_ID, companyId);
		query.setResultTransformer(Transformers.aliasToBean(CompanyDTO.class));
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Company getCompanyByName(String name, Integer companyProfileId) {
		Company company = null;
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(Company.class);
		criteria.add(Restrictions.eq("name", name).ignoreCase());
		criteria.add(Restrictions.eq("companyProfile.id", companyProfileId));
		criteria.addOrder(Order.desc("id"));
		List<Company> companies = criteria.list();
		if (companies != null && !companies.isEmpty()) {
			company = companies.get(0);
		}
		return company;
	}

	@Override
	public Map<String, Object> getCompanies(Pagination pagination) {
		Map<String, Object> map = new HashMap<>();
		String searchKey = pagination.getSearchKey();
		searchKey = XamplifyUtils.escapeSingleQuotesForSearchQuery(searchKey);
		searchKey = XamplifyUtils.addBackSlashToSpecialCharacters(searchKey);
		String searchQueryString = "";
		if (StringUtils.isNotBlank(searchKey)) {
			searchQueryString = findCompaniesSearch.replace("searchKey", searchKey);
		}
		String queryString = findCompaniesQuery + searchQueryString + findCompaniesGroupBy + addSortColumns(pagination);
		Session session = sessionFactory.getCurrentSession();
		SQLQuery query = session.createSQLQuery(queryString);
		query.setParameter(XamplifyConstants.COMPANY_ID, pagination.getCompanyId());
		return paginationUtil.setScrollableAndGetList(pagination, map, query, CompanyDTO.class);
	}

	private String addSortColumns(Pagination pagination) {
		List<SortColumnDTO> sortColumnDTOs = new ArrayList<>();
		SortColumnDTO createdTimeSortOption = new SortColumnDTO(createdTimePropertyName, "c.created_time", true, true,
				false);
		SortColumnDTO nameSortOption = new SortColumnDTO("name", "c.name", false, true, false);
		sortColumnDTOs.add(createdTimeSortOption);
		sortColumnDTOs.add(nameSortOption);
		return paginationUtil.generateSortQuery(pagination, sortColumnDTOs, "desc");
	}

	@Override
	public Object getCounts(Integer companyId) {
		String queryString = findCompanyCounts;
		Session session = sessionFactory.getCurrentSession();
		SQLQuery query = session.createSQLQuery(queryString);
		query.setParameter(XamplifyConstants.COMPANY_ID, companyId);
		query.setResultTransformer(Transformers.aliasToBean(CompanyDTO.class));
		return query.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<UserUserListDTO> getAllCompanyContactsForMigration(Integer companyId) {
		String sqlString = "with recent_contact as (select uul.user_id, ul.company_id, max(uul.id) as maxid "
				+ "from xt_user_userlist uul, xt_user_list ul where uul.user_list_id = ul.user_list_id and ul.module_name = 'CONTACTS' "
				+ "and uul.contact_company is not null and uul.contact_company != '' and uul.contact_company_id is null and ul.company_id = "
				+ companyId + " group by ul.company_id, uul.user_id)"
				+ "select uul.user_id as \"userId\", uul.user_list_id as \"userListId\", uul.city, uul.country, uul.address, "
				+ "uul.contact_company_id as \"contactCompanyId\", uul.contact_company as \"contactCompany\", uul.job_title as \"jobTitle\", "
				+ "uul.firstname as \"firstName\", uul.lastname as \"lastName\", uul.mobile_number as \"mobileNumber\", uul.state, uul.zip, uul.vertical, uul.region, "
				+ "uul.partner_type as \"partnerType\", uul.category, uul.description, rc.company_id as \"companyId\" "
				+ "from xt_user_userlist uul, recent_contact rc where uul.id = rc.maxid ";
		Query query = sessionFactory.getCurrentSession().createSQLQuery(sqlString);
		query.setResultTransformer(Transformers.aliasToBean(UserUserListDTO.class));
		return query.list();
	}

	@Override
	public void updateCompanyNamesForExistingContacts(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "WITH recent_contact AS (" + "SELECT uul.user_id, ul.company_id, MAX(uul.id) AS maxid "
				+ "FROM xt_user_userlist uul, xt_user_list ul " + "WHERE uul.user_list_id = ul.user_list_id "
				+ "AND ul.module_name = 'CONTACTS' " + "AND ul.company_id = " + companyId + " "
				+ "GROUP BY ul.company_id, uul.user_id " + "HAVING COUNT(*) > 1), " + "recent_contact_company AS ("
				+ "SELECT uul.id, rc.user_id, rc.company_id, uul.contact_company, uul.contact_company_id "
				+ "FROM xt_user_userlist uul, recent_contact rc " + "WHERE uul.id = rc.maxid) "
				+ "UPDATE xt_user_userlist AS uul "
				+ "SET contact_company = rcc.contact_company, contact_company_id = rcc.contact_company_id "
				+ "FROM xt_user_list ul, recent_contact_company rcc " + "WHERE uul.user_id = rcc.user_id "
				+ "AND uul.user_list_id = ul.user_list_id " + "AND ul.company_id = rcc.company_id "
				+ "AND ul.module_name = 'CONTACTS' " + "AND uul.contact_company_id IS NULL ";
		Query query = session.createSQLQuery(sql);
		query.executeUpdate();
	}

	@Override
	public void updateSyncStatus(Integer companyId, Boolean isSync) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "update xt_company_profile set sync_contacts_company_list = " + isSync + "  where company_id = "
				+ companyId;
		Query query = session.createSQLQuery(sql);
		query.executeUpdate();
	}

	@Override
	public boolean isMergeCompaniesInProgress(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select is_merge_companies_in_progress from xt_company_profile where company_id = " + companyId
				+ " ";
		Query query = session.createSQLQuery(sql);
		return (boolean) query.uniqueResult();
	}

	@Override
	public void updateMergeCompaniesInProgress(Integer companyId, Boolean mergeCompaniesInProgress) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "update xt_company_profile set is_merge_companies_in_progress = " + mergeCompaniesInProgress
				+ "  where company_id = " + companyId;
		Query query = session.createSQLQuery(sql);
		query.executeUpdate();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getDuplicateCompanies(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select distinct lower(name) from xt_company where company_id = :companyId group by lower(name), company_id having count(*) > 1";
		Query query = session.createSQLQuery(sql);
		query.setParameter("companyId", companyId);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Company> getAllCompaniesByName(String name, Integer companyProfileId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(Company.class);
		criteria.add(Restrictions.eq("name", name).ignoreCase());
		criteria.add(Restrictions.eq("companyProfile.id", companyProfileId));
		criteria.addOrder(Order.desc("id"));
		return criteria.list();
	}

	@Override
	public void mergeCompanyContacts(Integer parentCompanyId, Integer parentCompanyContactListId,
			String parentCompanyName, List<Integer> mergingCompanyIds, Integer loggedInCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "UPDATE xt_user_userlist uul set contact_company = :parentCompanyName, contact_company_id = "
				+ parentCompanyId + " ,  user_list_id = " + parentCompanyContactListId
				+ " FROM xt_user_list ul WHERE uul.user_list_id = ul.user_list_id "
				+ "AND ul.module_name = 'CONTACTS' AND ul.company_id = :companyId "
				+ "AND ul.associated_company_id in (:mergingCompanyIds)";
		Query query = session.createSQLQuery(sql);
		query.setParameter("companyId", loggedInCompanyId);
		query.setParameter("parentCompanyName", parentCompanyName);
		query.setParameterList("mergingCompanyIds", mergingCompanyIds);
		query.executeUpdate();
	}

	@Override
	public void updateCompanyOnAllContacts(Integer parentCompanyId, String parentCompanyName,
			List<Integer> mergingCompanyIds) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "UPDATE xt_user_userlist set contact_company = :parentCompanyName, contact_company_id = :parentCompanyId WHERE contact_company_id in (:mergingCompanyIds)";
		Query query = session.createSQLQuery(sql);
		query.setParameter("parentCompanyName", parentCompanyName);
		query.setParameter("parentCompanyId", parentCompanyId);
		query.setParameterList("mergingCompanyIds", mergingCompanyIds);
		query.executeUpdate();
	}

	@Override
	public void deleteCompanyContactLists(List<Integer> companyIds) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "delete from xt_user_list where associated_company_id in (:companyIds)";
		Query query = session.createSQLQuery(sql);
		query.setParameterList("companyIds", companyIds);
		query.executeUpdate();
	}

	@Override
	public void deleteCompanies(List<Integer> companyIds) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "delete from xt_company where id in (:companyIds)";
		Query query = session.createSQLQuery(sql);
		query.setParameterList("companyIds", companyIds);
		query.executeUpdate();
	}

	@Override
	public void updateSyncCompaniesInProgress(Integer companyId, boolean isSyncCompaniesInProgress) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "update xt_company_profile set is_sync_companies_in_progress = " + isSyncCompaniesInProgress
				+ "  where company_id = " + companyId;
		Query query = session.createSQLQuery(sql);
		query.executeUpdate();
	}

	@Override
	public boolean isSyncCompaniesInProgress(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select is_sync_companies_in_progress from xt_company_profile where company_id = " + companyId
				+ " ";
		Query query = session.createSQLQuery(sql);
		return (boolean) query.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<UserUserListDTO> getUserUserListsToHandleCompany(Integer userListId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select uul.user_id as \"userId\", uul.user_list_id as \"userListId\", uul.city, uul.country, uul.address, "
				+ " uul.contact_company_id as \"contactCompanyId\", uul.contact_company as \"contactCompany\", uul.job_title as \"jobTitle\", "
				+ " uul.firstname as \"firstName\", uul.lastname as \"lastName\", uul.mobile_number as \"mobileNumber\", uul.state, uul.zip, uul.vertical, uul.region, "
				+ " uul.partner_type as \"partnerType\", uul.category, uul.description, uul.contact_status_id as \"contactStatusId\" "
				+ " from xt_user_userlist uul where user_list_id = :userListId and "
				+ " uul.contact_company is not null and uul.contact_company != '' and uul.contact_company_id is null";
		Query query = session.createSQLQuery(sql);
		query.setParameter("userListId", userListId);
		query.setResultTransformer(Transformers.aliasToBean(UserUserListDTO.class));
		return (List<UserUserListDTO>) query.list();
	}

	@Override
	public void updateCompanyIdOnAllContacts(Integer userId, String companyName, Integer companyId,
			Integer loggedInCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "UPDATE xt_user_userlist uul SET contact_company = :contactCompany, "
				+ "contact_company_id = :contactCompanyId "
				+ " FROM xt_user_list ul  WHERE uul.user_list_id = ul.user_list_id AND ul.company_id = :loggedInCompanyId "
				+ " AND ul.module_name = 'CONTACTS' and uul.user_id=:userId ";
		Query query = session.createSQLQuery(sql);
		query.setParameter("contactCompany", companyName);
		query.setParameter("contactCompanyId", companyId);
		query.setParameter("loggedInCompanyId", loggedInCompanyId);
		query.setParameter("userId", userId);
		query.executeUpdate();
	}

	@Override
	public void deleteOtherCompanyContacts(Integer loggedInCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "DELETE FROM xt_user_userlist uul USING xt_user_list ul WHERE uul.user_list_id = ul.user_list_id "
				+ "AND ul.company_id = :loggedInCompanyId AND ul.module_name = 'CONTACTS' AND ul.associated_company_id is not null "
				+ "AND (uul.contact_company_id != ul.associated_company_id OR uul.contact_company_id is null)";
		Query query = session.createSQLQuery(sql);
		query.setParameter("loggedInCompanyId", loggedInCompanyId);
		query.executeUpdate();
	}

	@Override
	public String fetchWebsiteUsingCompanyId(Integer companyId) {
		String queryString = "select website from xt_company where id = :companyId";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("companyId", companyId));
		return (String) hibernateSqlQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
	}

}
