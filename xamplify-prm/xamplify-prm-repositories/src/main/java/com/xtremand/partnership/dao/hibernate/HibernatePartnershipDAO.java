package com.xtremand.partnership.dao.hibernate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.Criteria;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.common.bom.Pagination;
import com.xtremand.common.bom.Pagination.SORTINGORDER;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.dashboard.buttons.dto.DashboardButtonsPartnersDTO;
import com.xtremand.domain.bom.DomainModuleNameType;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.partner.bom.PartnerDTO;
import com.xtremand.partner.bom.PartnerDataAccessException;
import com.xtremand.partnership.bom.PartnerTeamGroupMapping;
import com.xtremand.partnership.bom.Partnership;
import com.xtremand.partnership.bom.Partnership.PartnershipSource;
import com.xtremand.partnership.bom.Partnership.PartnershipStatus;
import com.xtremand.partnership.bom.PartnershipDTO;
import com.xtremand.partnership.dao.PartnershipDAO;
import com.xtremand.partnership.dto.PartnerJourneyResponseDTO;
import com.xtremand.partnership.dto.VendorInvitationReport;
import com.xtremand.user.bom.Role;
import com.xtremand.user.bom.TeamMember;
import com.xtremand.user.bom.User;
import com.xtremand.user.bom.UserList;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.user.exception.TeamMemberDataAccessException;
import com.xtremand.util.PaginationUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.HibernateSQLQueryResultUtilDao;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.dto.CompanyDTO;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.PartnerCompanyDTO;
import com.xtremand.util.dto.PartnerGroupDTO;
import com.xtremand.util.dto.QueryParameterDTO;
import com.xtremand.util.dto.QueryParameterListDTO;
import com.xtremand.util.dto.TeamMemberFilterDTO;
import com.xtremand.util.dto.XamplifyConstants;
import com.xtremand.vendor.journey.dto.VendorLogoDTO;

@Repository("partnershipDAO")
@Transactional
public class HibernatePartnershipDAO implements PartnershipDAO {

	@Autowired
	SessionFactory sessionFactory;

	@Autowired
	GenericDAO genericDAO;

	@Autowired
	private UserDAO userDao;

	@Autowired
	private UtilDao utilDao;

	@Autowired
	private PaginationUtil paginationUtil;

	@Value("${partnerCompaniesQuery}")
	private String partnerCompaniesQuery;

	@Value("${partnerCompaniesSearchQuery}")
	private String partnerCompaniesSearchQuery;

	@Value("${partnerGroupsQuerySelect}")
	private String partnerGroupsQuerySelect;

	@Value("${partnerGroupsCountQuerySelect}")
	private String partnerGroupsCountQuerySelect;

	@Value("${partnerGroupsQueryFrom}")
	private String partnerGroupsQueryFrom;

	@Value("${partnerGroupsSearchQuery}")
	private String partnerGroupsSearchQuery;

	@Value("${partnerGroupsGroupByQuery}")
	private String partnerGroupsGroupByQuery;

	@Value("${partnerCompaniesQueryGroupBy}")
	private String partnerCompaniesQueryGroupBy;

	@Value("${active.partner.list.name}")
	private String activePartnerListName;

	@Value("${inactive.partner.list.name}")
	private String inActivePartnerListName;

	/*** XNFR-125 ****/
	@Value("${oneClickLaunchCampaignPartnerQuery}")
	private String oneClickLaunchCampaignPartnerQuery;

	/*** XNFR-220 ****/
	@Value("${allPartnerCompaniesQuery}")
	private String allPartnerCompaniesQuery;

	@Value("${allPartnerCompaniesSearchQuery}")
	private String allPartnerCompaniesSearchQuery;

	@Value("${journey.onboarded}")
	private String onboardedOnText;

	@Value("${journey.companyProfileCreatedOn}")
	private String companyProfileCreatedOnText;

	@Value("${journey.teamMemberCreatedOn}")
	private String teamMemberCreatedOnText;

	@Value("${journey.contactCreatedOn}")
	private String contactsUploadedOnText;

	@Value("${journey.campaignRedistributedOn}")
	private String campaignRedistributedOnText;

	@Value("${journey.leadCreatedOn}")
	private String leadCreatedOnText;

	@Value("${journey.dealCreatedOn}")
	private String dealCreatedOnText;

	// XNFR-316
	@Value("${activePartnerCompaniesQuery}")
	private String activePartnerCompaniesQuery;

	@Autowired
	private HibernateSQLQueryResultUtilDao hibernateSQLQueryResultUtilDao;

	private static final String PARTNER_COMPANY = "partnerCompany";

	private static final String VENDOR_COMPANY = "vendorCompany";

	private static final String CONTACTS_LIMIT = "contactsLimit";

	private static final String ALL_ADMIN_ROLES = Role.getAllAdminRolesInString();

	private static final String ALL_ADMIN_AND_PARTNER_ROLE = ALL_ADMIN_ROLES + "," + Role.COMPANY_PARTNER.getRoleId();

	private static final String ALL_ADMIN_AND_PARTNER_AND_SUPERVISOR_ROLE = ALL_ADMIN_AND_PARTNER_ROLE + ","
			+ Role.ALL_ROLES.getRoleId();

	@Override
	public Partnership getPartnershipByPartnerCompany(CompanyProfile partnerCompany, CompanyProfile vendorCompany) {
		Session session = sessionFactory.getCurrentSession();
		Partnership partnership = null;
		if (partnerCompany != null && vendorCompany != null) {
			org.hibernate.Criteria criteria = session.createCriteria(Partnership.class);
			criteria.add(Restrictions.eq(PARTNER_COMPANY, partnerCompany));
			criteria.add(Restrictions.eq(VENDOR_COMPANY, vendorCompany));
			partnership = (Partnership) criteria.uniqueResult();
		}
		return partnership;
	}

	@Override
	public Partnership getPartnershipByRepresentingPartner(User representingPartner, CompanyProfile vendorCompany) {
		Session session = sessionFactory.getCurrentSession();
		Partnership partnership = null;
		if (representingPartner != null && vendorCompany != null) {
			org.hibernate.Criteria criteria = session.createCriteria(Partnership.class);
			criteria.add(Restrictions.eq("representingPartner", representingPartner));
			criteria.add(Restrictions.eq(VENDOR_COMPANY, vendorCompany));
			partnership = (Partnership) criteria.uniqueResult();
		}
		return partnership;
	}

	@Override
	public Partnership getPartnershipByRepresentingVendor(User representingVendor, CompanyProfile partnerCompany) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(Partnership.class);
		criteria.add(Restrictions.eq("representingVendor", representingVendor));
		criteria.add(Restrictions.eq(PARTNER_COMPANY, partnerCompany));
		return (Partnership) criteria.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Partnership> getInvitedPartnershipsAsVendor(User representingVendor) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(Partnership.class);
		criteria.add(Restrictions.eq("representingVendor", representingVendor));
		criteria.add(Restrictions.eq("status", PartnershipStatus.INVITED));
		return criteria.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Partnership> getInvitedPartnerships(CompanyProfile companyProfile) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(Partnership.class);
		criteria.add(Restrictions.eq(VENDOR_COMPANY, companyProfile));
		criteria.add(Restrictions.eq("status", PartnershipStatus.INVITED));
		return criteria.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Partnership> getApprovedPartnershipsByVendorCompany(CompanyProfile companyProfile) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(Partnership.class);
		criteria.add(Restrictions.eq(VENDOR_COMPANY, companyProfile));
		criteria.add(Restrictions.eq("status", PartnershipStatus.APPROVED));
		return criteria.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Partnership> getApprovedPartnershipsByPartnerCompany(CompanyProfile companyProfile) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(Partnership.class);
		criteria.add(Restrictions.eq(PARTNER_COMPANY, companyProfile));
		criteria.add(Restrictions.eq("status", PartnershipStatus.APPROVED));
		return criteria.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getVendorCompanyIdsByPartnerCompany(CompanyProfile companyProfile) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(Partnership.class);
		criteria.add(Restrictions.eq(PARTNER_COMPANY, companyProfile));
		criteria.add(Restrictions.eq("status", PartnershipStatus.APPROVED));
		Projection projection = Projections.property("vendorCompany.id");
		criteria.setProjection(projection);
		return criteria.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> getOrgAdminsOrVendorsOrVendorTiersOrMarketing(Integer companyId) {
		List<User> orgAdminsOrVendors = null;
		if (companyId != null && companyId > 0) {
			Session session = sessionFactory.getCurrentSession();
			String hql1 = "select distinct u from User u Join u.roles r where r.roleId in (" + ALL_ADMIN_ROLES
					+ ") and u.companyProfile.id = :companyId";
			Query query1 = session.createQuery(hql1);
			query1.setInteger("companyId", companyId);
			orgAdminsOrVendors = query1.list();
		}
		return orgAdminsOrVendors;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> getOwners(Integer companyId) {
		List<User> orgAdminsOrVendors = null;
		if (companyId != null && companyId > 0) {
			Session session = sessionFactory.getCurrentSession();
			String hql1 = "select distinct u from User u Join u.roles r where r.roleId in ("
					+ ALL_ADMIN_AND_PARTNER_ROLE + ") and u.companyProfile.id = :companyId";
			Query query1 = session.createQuery(hql1);
			query1.setInteger("companyId", companyId);
			orgAdminsOrVendors = query1.list();
		}
		return orgAdminsOrVendors;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> getSuperiors(Integer companyId) {
		List<User> orgAdminsOrVendors = null;
		if (companyId != null && companyId > 0) {
			Session session = sessionFactory.getCurrentSession();
			String hql1 = "select distinct u from User u Join u.roles r where r.roleId in ("
					+ ALL_ADMIN_AND_PARTNER_AND_SUPERVISOR_ROLE + ") and u.companyProfile.id = :companyId";
			Query query1 = session.createQuery(hql1);
			query1.setInteger("companyId", companyId);
			orgAdminsOrVendors = query1.list();
		}
		return orgAdminsOrVendors;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getSuperiorIds(Integer companyId) {
		List<Integer> superiorIds = null;
		if (companyId != null && companyId > 0) {
			Session session = sessionFactory.getCurrentSession();
			String hql1 = "select u.userId from User u Join u.roles r where r.roleId in ("
					+ ALL_ADMIN_AND_PARTNER_AND_SUPERVISOR_ROLE + ") and u.companyProfile.id = :companyId";
			Query query1 = session.createQuery(hql1);
			query1.setInteger("companyId", companyId);
			superiorIds = query1.list();
		}
		return superiorIds;
	}

	@Override
	public void deletePartnerShipByPartnerCompany(CompanyProfile partnerCompany, CompanyProfile vendorCompany) {
		Session session = sessionFactory.getCurrentSession();
		if (partnerCompany != null && vendorCompany != null) {
			Partnership partnership = getPartnershipByPartnerCompany(partnerCompany, vendorCompany);
			if (partnership != null) {
				session.createSQLQuery(
						"delete from  xt_partnership_status_history where partnership_id=" + partnership.getId())
						.executeUpdate();
				session.createSQLQuery("delete from  xt_partnership where id=" + partnership.getId()).executeUpdate();
				partnerCompany.getPartnershipsAsPartner().remove(partnership);
			}
		}
	}

	@Override
	public void deletePartnerShipByRepresentingPartner(User representingPartner, CompanyProfile vendorCompany) {
		Session session = sessionFactory.getCurrentSession();
		Partnership partnership = getPartnershipByRepresentingPartner(representingPartner, vendorCompany);
		if (partnership != null) {
			session.createSQLQuery(
					"delete from  xt_partnership_status_history where partnership_id=" + partnership.getId())
					.executeUpdate();
			session.createSQLQuery("delete from  xt_partnership where id=" + partnership.getId()).executeUpdate();
		}
	}

	@Override
	public Integer getApprovePartnersCount(CompanyProfile vendorCompany, Integer userId, boolean applyFilter) {
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(userId, applyFilter, true);
		Session session = sessionFactory.getCurrentSession();
		String sql = " select count(*) from xt_partnership xp where xp.vendor_company_id= :vendorCompany and xp.source= 'invitation' "
				+ getPartnerTeamMemberGroupFilterSQL(" xp.partner_company_id", userId, teamMemberFilterDTO, "approved");
		Query query = session.createSQLQuery(sql);
		query.setParameter(VENDOR_COMPANY, vendorCompany.getId());
		Integer count = ((java.math.BigInteger) query.uniqueResult()).intValue();
		return (count != null && count > 0) ? count : 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getApprovalSectionPartnersDetails(Pagination pagination) {
		Map<String, Object> resultMap = new HashMap<>();
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), true);
		Session session = sessionFactory.getCurrentSession();
		String sql = " select distinct up.user_id as partnerId, up.firstname as firstName, up.lastname as lastName,concat(up.firstname,' ',  up.lastname) as fullName, up.email_id as emailId, "
				+ " cp.company_name as partnerCompanyName, pv.status as invitationstatus from  xt_partnership pv, xt_user_profile up, "
				+ " xt_company_profile cp  where  pv.vendor_company_id = :vendorCompanyId  and  up.company_id = cp.company_id "
				+ " and up.company_id= pv.partner_company_id and pv.source='invitation' and pv.partner_id=up.user_id"
				+ getPartnerTeamMemberGroupFilterSQL(" pv.partner_company_id", pagination.getUserId(),
						teamMemberFilterDTO, "approved");
		if (StringUtils.hasText(pagination.getSearchKey())) {
			String searchKey = pagination.getSearchKey().trim().replaceAll("\\s+", " ");
			searchKey = "'%" + searchKey + "%'";
			sql += " and (LOWER(up.email_id) ilike LOWER(" + searchKey + ") " + " or LOWER(up.firstname) ilike LOWER("
					+ searchKey + ")" + " or LOWER(up.lastname) ilike LOWER(" + searchKey + ") "
					+ " or LOWER(concat(up.firstname,' ',up.lastname)) ilike LOWER(" + searchKey + ") "
					+ " or LOWER(CAST(pv.status AS TEXT)) ilike  LOWER(" + searchKey + ") ) ";
		}

		String sortColumn = pagination.getSortcolumn();
		String sortOrder = pagination.getSortingOrder();
		String sort = "";
		if ("emailId".equalsIgnoreCase(sortColumn) && XamplifyUtils.isValidString(sortOrder)) {
			String safeOrder = "asc".equalsIgnoreCase(sortOrder) ? "ASC" : "DESC";
			sort = " order by up.email_id " + safeOrder + " nulls last";
		} else {
			sort = " order by up.email_id desc nulls last";
		}
		sql += sort;

		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("vendorCompanyId", pagination.getCompanyId());
		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		query.setMaxResults(pagination.getMaxResults());
		List<Object[]> partnerDTOList = query.list();
		resultMap.put("approvePartnerList", partnerDTOList);
		resultMap.put("totalRecords", totalRecords);
		return resultMap;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void updateCompanyIdOnUserList() {
		Session session = sessionFactory.getCurrentSession();
		String hql1 = "from UserList";
		Query query1 = session.createQuery(hql1);
		List<UserList> userLists = (List<UserList>) query1.list();
		if (userLists != null && !userLists.isEmpty()) {
			for (UserList userList : userLists) {
				User owner = userList.getOwner();
				if (owner != null) {
					CompanyProfile company = owner.getCompanyProfile();
					if (company != null && company.getId() != null && company.getId() > 0) {
						userList.setCompany(company);
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void removePartnerRoleOnTeamMember() {
		Session session = sessionFactory.getCurrentSession();
		String hql1 = "from TeamMember";
		Query query1 = session.createQuery(hql1);
		List<TeamMember> teamMembers = (List<TeamMember>) query1.list();
		if (teamMembers != null && !teamMembers.isEmpty()) {
			for (TeamMember teamMember : teamMembers) {
				User teamMemberUser = teamMember.getTeamMember();
				teamMemberUser.getRoles().remove(Role.COMPANY_PARTNER);
			}
		}

	}

	// *********************************PATCH*******************************************************

	@SuppressWarnings("unchecked")
	@Override
	public List<UserList> getAllDefaultPartnerLists() {
		Session session = sessionFactory.getCurrentSession();
		String hql1 = "from UserList where isDefaultPartnerList = true and isPartnerUserList = true";
		Query query1 = session.createQuery(hql1);
		List<UserList> defaultUserLists = (List<UserList>) query1.list();
		return defaultUserLists;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void processExistingPartnerLists() {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select user_list_id, customer_id, company_id from xt_user_list "
				+ "where is_default_partnerlist = true and is_partner_userlist = true";
		Query query1 = session.createSQLQuery(sql);
		List<Object[]> defaultUserLists = query1.list();
		if (defaultUserLists != null && !defaultUserLists.isEmpty()) {
			for (Object[] defaultUserList : defaultUserLists) {
				Integer listId = (Integer) defaultUserList[0];
				Integer userId = (Integer) defaultUserList[1];
				Integer companyId = (Integer) defaultUserList[2];
				String user_userlist_sql = "select user_id from xt_user_userlist" + "where user_list_id =" + listId;
				List<Integer> parterIds = session.createSQLQuery(user_userlist_sql).list();
				if (parterIds != null && !parterIds.isEmpty()) {
					for (Integer parterId : parterIds) {
						String user_sql = "select company_id from xt_user_profile" + "where user_id =" + parterId;
						Integer partnerCompanyId = (Integer) session.createSQLQuery(user_sql).uniqueResult();
						Integer partnershipId = getPartnerShipId(parterId, partnerCompanyId, userId, companyId);
						if (partnershipId != null) {
							// skip
						} else {
							// create partnership
							partnershipId = createPartnership(parterId, partnerCompanyId, userId, companyId);
							// create partnership status history
							createPartnershipStatusHistory(partnershipId, userId);
						}
					}
				}
			}
		}
	}

	@Override
	public void processExistingPartnerList(UserList list) {
		Session session = sessionFactory.getCurrentSession();
		Integer userId = list.getOwner().getUserId();
		Integer companyId = list.getCompany().getId();
		/*
		 * String user_userlist_sql = "select user_id from xt_user_userlist" +
		 * "where user_list_id ="+ listId; List<Integer> parterIds =
		 * session.createSQLQuery(user_userlist_sql).list();
		 */

		Set<User> partners = list.getUsers();
		List<Integer> parterIds = partners.stream().map(User::getUserId).collect(Collectors.toList());
		if (parterIds != null && !parterIds.isEmpty()) {
			for (Integer parterId : parterIds) {
				String user_sql = "select company_id from xt_user_profile where user_id=" + parterId;
				Integer partnerCompanyId = (Integer) session.createSQLQuery(user_sql).uniqueResult();
				Integer partnershipId = getPartnerShipId(parterId, partnerCompanyId, userId, companyId);
				if (partnershipId != null) {
					// skip
				} else {
					// create partnership
					partnershipId = createPartnership(parterId, partnerCompanyId, userId, companyId);
					// create partnership status history
					createPartnershipStatusHistory(partnershipId, userId);
				}
			}
		}

	}

	private void createPartnershipStatusHistory(Integer partnershipId, Integer userId) {
		Session session = sessionFactory.getCurrentSession();
		String insert_sql = "insert into xt_partnership_status_history"
				+ "(partnership_id, status, created_by, created_time) "
				+ "values (:partnershipId, CAST(:status AS partnership_status), :userId, :ct) ";
		Query query = session.createSQLQuery(insert_sql);
		query.setParameter("partnershipId", partnershipId);
		query.setParameter("status", "approved");
		query.setParameter("userId", userId);
		query.setParameter("ct", new Date());
		query.executeUpdate();
	}

	private Integer createPartnership(Integer parterId, Integer partnerCompanyId, Integer userId, Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String insert_sql = null;
		if (partnerCompanyId != null) {
			insert_sql = "insert into xt_partnership"
					+ "(partner_id, partner_company_id, vendor_id, vendor_company_id, source, status, created_by, updated_by, created_time, updated_time) "
					+ "values (:parterId, :partnerCompanyId, :userId, :companyId, CAST(:source AS partnership_source), CAST(:status AS partnership_status), :userId, :userId, :ct, :ct ) ";

		} else {
			insert_sql = "insert into xt_partnership"
					+ "(partner_id, vendor_id, vendor_company_id, source, status, created_by, updated_by, created_time, updated_time) "
					+ "values (:parterId, :userId, :companyId, CAST(:source AS partnership_source), CAST(:status AS partnership_status), :userId, :userId, :ct, :ct ) ";

		}

		Query query = session.createSQLQuery(insert_sql);
		query.setParameter("parterId", parterId);
		if (partnerCompanyId != null) {
			query.setParameter("partnerCompanyId", partnerCompanyId);
		}

		query.setParameter("userId", userId);
		query.setParameter("companyId", companyId);
		// PartnershipSource.valueOf(Partnership., name)
		// query.setParameter("source", "'"+PartnershipSource.ONBOARD+"'");
		query.setParameter("source", "onboard");
		query.setParameter("status", "approved");
		query.setParameter("ct", new Date());
		Integer partershipId = query.executeUpdate();
		return partershipId;
	}

	private Integer getPartnerShipId(Integer parterId, Integer partnerCompanyId, Integer userId, Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		Integer partnershipId = null;
		String partnership_sql = null;
		if (partnerCompanyId != null) {
			partnership_sql = "select id from xt_partnership where partner_company_id =" + partnerCompanyId
					+ " and vendor_company_id =" + companyId;
		} else {
			partnership_sql = "select id from xt_partnership where partner_id =" + parterId + " and vendor_company_id ="
					+ companyId;
		}
		partnershipId = (Integer) session.createSQLQuery(partnership_sql).uniqueResult();
		return partnershipId;
	}

	// *********************************PATCH
	// ENDS*******************************************************

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getAllPartnershipsByPartnerCompany(CompanyProfile partnerCompany,
			PartnershipSource source, Pagination pagination, PartnershipStatus status) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(Partnership.class, "p");
		criteria.createAlias("p.representingVendor", "v", JoinType.LEFT_OUTER_JOIN);
		criteria.createAlias("p.vendorCompany", "vc", JoinType.LEFT_OUTER_JOIN);
		criteria.createAlias("p.partnershipStatusHistory", "psh", JoinType.LEFT_OUTER_JOIN);
		criteria.add(Restrictions.eq("partnerCompany", partnerCompany));
		criteria.add(Restrictions.eq("source", source));

		if (status != null) {
			criteria.add(Restrictions.eq("status", status));
		}

		if (XamplifyUtils.isValidString(pagination.getSearchKey())) {
			String searchPattern = "%" + pagination.getSearchKey().trim() + "%";
			criteria.add(Restrictions.or(Restrictions.ilike("v.emailId", searchPattern),
					Restrictions.ilike("vc.companyName", searchPattern),
					Restrictions.sqlRestriction("CAST({alias}.status AS TEXT) ILIKE ?", searchPattern,
							org.hibernate.type.StringType.INSTANCE)));
		}

		String sortColumn = pagination.getSortcolumn();
		String sortOrder = pagination.getSortingOrder();

		if (XamplifyUtils.isValidString(sortColumn) && XamplifyUtils.isValidString(sortOrder)) {
			Order order = "asc".equalsIgnoreCase(sortOrder) ? Order.asc(sortColumn) : Order.desc(sortColumn);
			criteria.addOrder(order);
		} else {
			criteria.addOrder(Order.desc("createdTime"));
		}
		List<Partnership> allRecords = criteria.list();
		int totalRecords = allRecords.size();

		int firstResult = (pagination.getPageIndex() - 1) * pagination.getMaxResults();
		criteria.setFirstResult(firstResult);
		criteria.setMaxResults(pagination.getMaxResults());

		List<Partnership> partnerships = criteria.list();

		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("totalRecords", totalRecords); // ✅ correct dynamic count
		resultMap.put("data", partnerships);
		return resultMap;

	}

	@Override
	public Long getAllPartnershipsCountByPartnerCompany(CompanyProfile partnerCompany, PartnershipSource source,
			PartnershipStatus status) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteriaCount = session.createCriteria(Partnership.class);
		criteriaCount.add(Restrictions.eq(PARTNER_COMPANY, partnerCompany));
		criteriaCount.add(Restrictions.eq("source", source));
		if (status != null) {
			criteriaCount.add(Restrictions.eq("status", status));
		}
		criteriaCount.setProjection(Projections.rowCount());
		return (Long) criteriaCount.uniqueResult();
	}

	@Override
	public PartnershipDTO getPartnerShipByParnterIdAndVendorCompanyId(Integer partnerId, Integer vendorCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		String sqlQueryString = "select id as \"id\",partner_company_id as \"partnerCompanyId\", CAST(status AS text) as \"status\" from xt_partnership where vendor_company_id = :vendorCompanyId and partner_id = :partnerId";
		SQLQuery query = session.createSQLQuery(sqlQueryString);
		query.setParameter("vendorCompanyId", vendorCompanyId);
		query.setParameter("partnerId", partnerId);
		return (PartnershipDTO) paginationUtil.getDto(PartnershipDTO.class, query);
	}

	@Override
	public Integer getSharedPagesCount(Integer partnerId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = " select count(*) from (select  c.campaign_type,cp.campaign_id,company_id,partner_company_id from xt_campaign c,xt_campaign_partner cp where cp.campaign_id = c.campaign_id and cp.user_id = :partnerId)as count";
		Query query = session.createSQLQuery(sql);
		query.setParameter("partnerId", partnerId);
		Integer count = ((java.math.BigInteger) query.uniqueResult()).intValue();
		return (count != null && count > 0) ? count : 0;
	}

	@Override
	public Partnership getMdfAmountAndContactsLimitAndNotifyPartnersByVendorCompanyIdAndPartnerId(
			Integer vendorCompanyId, Integer partnerId) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(Partnership.class);
		CompanyProfile vendorCompany = new CompanyProfile();
		vendorCompany.setId(vendorCompanyId);

		User partner = new User();
		partner.setUserId(partnerId);

		if (vendorCompanyId != null && partnerId != null) {
			criteria.add(Restrictions.eq("representingPartner", partner));
			criteria.add(Restrictions.eq(VENDOR_COMPANY, vendorCompany));
			criteria.setProjection(
					Projections.projectionList().add(Projections.property(CONTACTS_LIMIT), CONTACTS_LIMIT)
							.add(Projections.property("notifyPartners"), "notifyPartners")
							.add(Projections.property("partnerCompany"), "partnerCompany")
							.add(Projections.property("loginAsPartnerOptionEnabledForVendor"),
									"loginAsPartnerOptionEnabledForVendor")
							.add(Projections.property("id"), "id").add(Projections.property("status"), "status"))
					.setResultTransformer(Transformers.aliasToBean(Partnership.class));
			return (Partnership) criteria.uniqueResult();
		} else {
			return null;
		}
	}

	@Override
	public void updateContactsLimitAndMDFAmountAndTeamMembers(Integer vendorCompanyId, Integer partnerId,
			Integer contactsLimit, Double mdfAmount) {
		if (vendorCompanyId != null && partnerId != null) {
			Session session = sessionFactory.getCurrentSession();
			String queryString = "update xt_partnership set contacts_limit=:contactsLimit where partner_id=:partnerId and vendor_company_id=:vendorCompanyId";
			Query query = session.createSQLQuery(queryString);
			if (contactsLimit != null) {
				query.setParameter(CONTACTS_LIMIT, contactsLimit);
			} else {
				query.setParameter(CONTACTS_LIMIT, 1);
			}
			query.setParameter("partnerId", partnerId);
			query.setParameter("vendorCompanyId", vendorCompanyId);
			query.executeUpdate();
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PartnershipDTO> listPartnerDetailsByVendorCompanyId(Integer vendorCompanyId) {
		if (vendorCompanyId != null) {
			List<PartnershipDTO> partnershipDTOs = new ArrayList<>();
			Session session = sessionFactory.getCurrentSession();
			String queryString = "select u.email_id,p.partner_company_id from xt_partnership p,xt_user_profile u where p.vendor_company_id=:vendorCompanyId and p.partner_company_id is not null \n"
					+ " and p.partner_id = u.user_id ";
			Query query = session.createSQLQuery(queryString).setParameter("vendorCompanyId", vendorCompanyId);
			List<Object[]> list = query.list();
			for (Object[] object : list) {
				PartnershipDTO partnershipDTO = new PartnershipDTO();
				partnershipDTO.setPartnerEmailId((String) object[0]);
				partnershipDTO.setPartnerCompanyId((Integer) object[1]);
				partnershipDTOs.add(partnershipDTO);
			}
			return partnershipDTOs;
		} else {
			return new ArrayList<>();
		}

	}

	@Override
	public PartnershipDTO findContactsLimitAndNotifyPartnersByEmailIdAndVendorCompanyId(String emailId,
			Integer vendorCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = " select contacts_limit as \"contactsLimit\", notify_partners as \"notifyPartners\",id as \"id\" from xt_partnership where partner_id =	(select user_id from xt_user_profile where email_id =:emailId) and vendor_company_id = :vendorCompanyId";
		Query query = session.createSQLQuery(queryString).setParameter("vendorCompanyId", vendorCompanyId)
				.setParameter("emailId", emailId);
		PartnershipDTO partnershipDTO = (PartnershipDTO) query
				.setResultTransformer(Transformers.aliasToBean(PartnershipDTO.class)).uniqueResult();
		if (partnershipDTO != null) {
			partnershipDTO.setDisableNotifyPartnersOption(true);
			return partnershipDTO;
		} else {
			PartnershipDTO defaultPartnershipDTO = new PartnershipDTO();
			defaultPartnershipDTO.setContactsLimit(1);
			defaultPartnershipDTO.setId(0);
			defaultPartnershipDTO.setNotifyPartners(userDao.findNotifyPartnersOption(vendorCompanyId));
			return defaultPartnershipDTO;
		}
	}

	@Override
	public Partnership checkPartnership(Integer vendorCompanyId, Integer partnerCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		Partnership partnership = null;
		if (partnerCompanyId != null && vendorCompanyId != null) {
			org.hibernate.Criteria criteria = session.createCriteria(Partnership.class);
			criteria.add(Restrictions.eq("partnerCompany.id", partnerCompanyId));
			criteria.add(Restrictions.eq("vendorCompany.id", vendorCompanyId));
			criteria.add(Restrictions.eq("status", PartnershipStatus.APPROVED));
			partnership = (Partnership) criteria.uniqueResult();
		}
		return partnership;
	}

	@Override

	public Partnership getPartnershipById(Integer partnershipId) {
		Session session = sessionFactory.getCurrentSession();
		Partnership partnership = null;
		if (partnershipId != null) {
			org.hibernate.Criteria criteria = session.createCriteria(Partnership.class);
			criteria.add(Restrictions.eq("id", partnershipId));
			partnership = (Partnership) criteria.uniqueResult();
		}
		return partnership;
	}

	public boolean isPartnershipEstablished(Integer partnerCompanyId) {
		if (partnerCompanyId != null) {
			String sqlString = "select case when count(*)>0  then true else false end as partnershipEstablished from xt_partnership where partner_company_id = :partnerCompanyId and status = 'approved'";
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createSQLQuery(sqlString).setParameter("partnerCompanyId", partnerCompanyId);
			return (boolean) query.uniqueResult();
		} else {
			return false;
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> listAllPartnershipIdsByVendorCompanyId(Integer vendorCompanyId) {
		if (vendorCompanyId != null) {
			Session session = sessionFactory.getCurrentSession();
			Query query = session
					.createSQLQuery("select id from xt_partnership where vendor_company_id=:vendorCompanyId")
					.setParameter("vendorCompanyId", vendorCompanyId);
			return query.list();
		} else {
			return new ArrayList<>();
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Partnership> getPartnershipsByPartnerId(Integer userId) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(Partnership.class);
		criteria.add(Restrictions.eq("representingPartner.userId", userId));
		criteria.add(Restrictions.eq("status", PartnershipStatus.APPROVED));
		return criteria.list();
	}

	@Override
	public Integer getPartnershipIdByVendorCompanyIdAndPartnerCompanyId(Integer vendorCompanyId,
			Integer partnerCompanyId) {
		if (vendorCompanyId != null && partnerCompanyId != null) {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createSQLQuery(
					"select id from xt_partnership where vendor_company_id=:vendorCompanyId and partner_company_id=:partnerCompanyId")
					.setParameter("vendorCompanyId", vendorCompanyId)
					.setParameter("partnerCompanyId", partnerCompanyId);
			return (Integer) query.uniqueResult();
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> listPartnersByVendorCompanyId(Integer vendorCompanyId, Pagination pagination) {
		Session session = sessionFactory.getCurrentSession();

		String searchSql = "";
		if (pagination.getSearchKey() != null) {
			searchSql = " and (up.email_id like '%" + pagination.getSearchKey() + "%' or uul.firstname like '%"
					+ pagination.getSearchKey() + "%' or " + " uul.lastname like '%" + pagination.getSearchKey()
					+ "%' or uul.vertical like '%" + pagination.getSearchKey() + "%'  " + " or uul.region like '%"
					+ pagination.getSearchKey() + "%' or uul.partner_type like '%" + pagination.getSearchKey()
					+ "%' or " + "uul.category like '%" + pagination.getSearchKey() + "%' or cp.company_name like '%"
					+ pagination.getSearchKey() + "%' )";
		}
		String sortSql = "";
		if (pagination.getSortcolumn() != null) {
			if (pagination.getSortcolumn().equalsIgnoreCase("emailId")) {
				sortSql = " order by up.email_id " + pagination.getSortingOrder() + " NULLS LAST";
			} else if (pagination.getSortcolumn().equalsIgnoreCase("firstName")) {
				sortSql = " order by max(uul.firstname)  " + pagination.getSortingOrder() + " NULLS LAST";
			} else if (pagination.getSortcolumn().equalsIgnoreCase("lastName")) {
				sortSql = " order by max(uul.lastname)  " + pagination.getSortingOrder() + " NULLS LAST";
			} else if (pagination.getSortcolumn().equalsIgnoreCase("contactCompany")) {
				sortSql = " order by cp.company_name " + pagination.getSortingOrder() + " NULLS LAST";
			}
		}
		String sql = " select up.user_id as \"id\", up.email_id as \"emailId\", max(uul.firstname) as \"firstName\", max(uul.lastname) as \"lastName\", "
				+ " cp.company_name as \"companyName\", max(uul.vertical) as \"vertical\", max(uul.region) as \"region\", max(uul.partner_type) as \"partnerType\", "
				+ " max(uul.category) as \"category\", max(uul.job_title) as \"jobTitle\", max(uul.address) as \"address\", max(uul.country) as \"country\", max(uul.city) as \"city\", max(uul.state) as \"state\", "
				+ " max(uul.zip) as \"zipCode\", max(uul.contact_company) as \"contactCompany\" from xt_partnership p "
				+ " left outer join xt_user_list ul on ul.company_id = p.vendor_company_id "
				+ " left outer join xt_user_userlist uul on (  ul.user_list_id=uul.user_list_id and uul.user_id = p.partner_id ) "
				+ " left outer join xt_user_profile up on (up.user_id = uul.user_id  ) "
				+ " left outer join xt_company_profile cp on cp.company_id = up.company_id "
				+ " where p.vendor_company_id = :vendorCompanyId and p.status = 'approved' and up.user_id is not null "
				+ searchSql + " group by up.email_id,  cp.company_name, up.user_id " + sortSql;
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("vendorCompanyId", vendorCompanyId);
		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		query.setMaxResults(pagination.getMaxResults());
		query.setResultTransformer(Transformers.aliasToBean(UserDTO.class));
		List<UserDTO> partners = query.list();
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("totalRecords", totalRecords);
		resultMap.put("data", partners);
		return resultMap;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<UserDTO> findEmailIdAndFullNameByUserIds(Set<Integer> userIds) {
		if (userIds != null && !userIds.isEmpty()) {
			Session session = sessionFactory.getCurrentSession();
			String sqlString = " select distinct u.user_id as \"userId\",  u.email_id as \"emailId\",concat(u.firstname,' ',u.lastname,' ',u.middle_name) as \"fullName\" from xt_user_profile u where u.user_id in (:userIds)";
			SQLQuery query = session.createSQLQuery(sqlString);
			query.setParameterList("userIds", userIds);
			query.setResultTransformer(Transformers.aliasToBean(UserDTO.class));
			return query.list();
		} else {
			return Collections.emptyList();
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> getPartnerCompanySuperior(Integer companyId) {
		List<User> orgAdminsOrVendors = null;
		if (companyId != null && companyId > 0) {
			Session session = sessionFactory.getCurrentSession();
			String hql1 = "select distinct u from User u Join u.roles r where r.roleId in ("
					+ Role.COMPANY_PARTNER.getRoleId() + ") and u.companyProfile.id = :companyId";
			Query query1 = session.createQuery(hql1);
			query1.setInteger("companyId", companyId);
			orgAdminsOrVendors = (List<User>) query1.list();
		}
		return orgAdminsOrVendors;
	}

	@Override
	public Map<String, Object> findPartnerCompanies(Pagination pagination) {
		try {
			HashMap<String, Object> map = new HashMap<>();
			String finalQueryString = "";
			/*********** XNFR-85 **********/
			String findPartnerCompaniesQuery = partnerCompaniesQuery;
			TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
					pagination.isPartnerTeamMemberGroupFilter(), true);
			boolean applyPartnershipIdsFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
			if (teamMemberFilterDTO.isEmptyFilter()) {
				return paginationUtil.returnEmptyList(map, new ArrayList<>());
			} else {
				if (applyPartnershipIdsFilter) {
					findPartnerCompaniesQuery = findPartnerCompaniesQuery + " and p.id in (:partnershipIds)";
				}
			}
			String sortQueryString = getSelectedSortOptionForPartners(pagination);
			String searchKey = pagination.getSearchKey();
			boolean hasSearchKey = StringUtils.hasText(searchKey);
			if (hasSearchKey) {
				finalQueryString = findPartnerCompaniesQuery
						+ partnerCompaniesSearchQuery.replace("searchKey", searchKey) + partnerCompaniesQueryGroupBy
						+ " " + sortQueryString;
			} else {
				finalQueryString = findPartnerCompaniesQuery + partnerCompaniesQueryGroupBy + " " + sortQueryString;
			}
			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(finalQueryString);
			Integer companyId = pagination.getVendorCompanyId();
			if (companyId == null || companyId.equals(0)) {
				companyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			}
			query.setParameter("vendorCompanyId", companyId);
			utilDao.applyPartnershipIdsParameterList(applyPartnershipIdsFilter,
					teamMemberFilterDTO.getPartnershipIdsOrPartnerCompanyIds(), query);
			return paginationUtil.setScrollableAndGetList(pagination, map, query, PartnerCompanyDTO.class);
		} catch (HibernateException | PartnerDataAccessException e) {
			throw new PartnerDataAccessException(e);
		} catch (Exception ex) {
			throw new PartnerDataAccessException(ex);
		}

	}

	private String getSelectedSortOptionForPartners(Pagination pagination) {
		String sortOptionQueryString = "";
		if (StringUtils.hasText(pagination.getSortcolumn())) {
			if ("companyName".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += " order by uul.contact_company " + pagination.getSortingOrder();
				sortOptionQueryString = paginationUtil.setNullConditionsForAscOrDesc(pagination, sortOptionQueryString);
			} else if ("createdTime".equals(pagination.getSortcolumn())) {
				sortOptionQueryString = orderByArrayOrCreatedTime(pagination, sortOptionQueryString);
			}
		} else {
			sortOptionQueryString = orderByArrayOrCreatedTime(pagination, sortOptionQueryString);
		}
		return sortOptionQueryString;
	}

	private String orderByArrayOrCreatedTime(Pagination pagination, String sortOptionQueryString) {
		if (pagination.getFiltertedEmailTempalteIds() != null && !pagination.getFiltertedEmailTempalteIds().isEmpty()) {
			sortOptionQueryString += "   order by array_position(array" + pagination.getFiltertedEmailTempalteIds()
					+ ", p.id),uul.contact_company asc nulls first";
		} else {
			sortOptionQueryString += " order by p.created_time " + " desc";
		}
		return sortOptionQueryString;
	}

	@Override
	public Partnership findByPrimaryKey(Serializable pk, FindLevel[] levels) {
		return null;
	}

	@Override
	public Collection<Partnership> find(List<Criteria> criterias, FindLevel[] levels) {
		return Collections.emptyList();
	}

	@Override
	public Map<String, Object> find(List<Criteria> criterias, FindLevel[] levels, Pagination pagination) {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getPartnershipIdsByPartnerCompanyUserIds(List<Integer> partnerIds, Integer vendorCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		String sqlQueryString = " select p.id from xt_partnership p,xt_user_profile u,xt_company_profile c "
				+ " where c.company_id = u.company_id and p.partner_company_id = c.company_id "
				+ " and u.user_id in (:partnerIds) and p.vendor_company_id = :vendorCompanyId "
				+ "and p.status = 'approved' ";
		Query query = session.createSQLQuery(sqlQueryString).setParameter("vendorCompanyId", vendorCompanyId)
				.setParameterList("partnerIds", partnerIds);
		return query.list();
	}

	@Override
	public Integer getPartnershipIdByPartnerCompanyUserId(Integer partnerId, Integer vendorCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		String sqlQueryString = " select p.id from xt_partnership p,xt_user_profile u,xt_company_profile c "
				+ " where c.company_id = u.company_id and p.partner_company_id = c.company_id "
				+ " and u.user_id = :partnerId and p.vendor_company_id = :vendorCompanyId ";
		Query query = session.createSQLQuery(sqlQueryString).setParameter("vendorCompanyId", vendorCompanyId)
				.setParameter("partnerId", partnerId);
		return (Integer) query.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getApprovedPartnershipsByVendorCompany(Pagination pagination) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(Partnership.class, "P");
		criteria.add(Restrictions.eq("vendorCompany.id", pagination.getCompanyId()));
		criteria.add(Restrictions.eq("status", PartnershipStatus.APPROVED));
		criteria.add(Restrictions.isNotNull("partnerCompany.id"));
		criteria.createAlias("P.partnerCompany", "PC", JoinType.LEFT_OUTER_JOIN);

		if (pagination.getSearchKey() != null && !pagination.getSearchKey().isEmpty()) {
			// criteria.createAlias("P.partnerCompany", "PC", JoinType.LEFT_OUTER_JOIN);
			criteria.add(Restrictions.ilike("PC.companyName", pagination.getSearchKey(), MatchMode.ANYWHERE));
		}

		ScrollableResults scrollableResults = criteria.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		scrollableResults.close();

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
				criteria.addOrder(Order.asc("PC." + sortcolumnObj.get()));
			} else if (SORTINGORDER.DESC == SORTINGORDER.valueOf(paginationObj.get().getSortingOrder())) {
				criteria.addOrder(Order.desc("PC." + sortcolumnObj.get()));
			}

		} else {
			criteria.addOrder(Order.desc("id"));
		}

		/*
		 * criteria.setFirstResult((pagination.getPageIndex() - 1) *
		 * pagination.getMaxResults());
		 * criteria.setMaxResults(pagination.getMaxResults());
		 */

		List<Partnership> ltList = criteria.list();
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("totalRecords", totalRecords);
		resultMap.put("data", ltList);
		return resultMap;
	}

	@Override
	public Map<String, Object> findPartnerGroups(Pagination pagination) {
		try {
			HashMap<String, Object> map = new HashMap<>();
			String finalQueryString = "";
			String sortQueryString = sortByForPartnerGroups(pagination);
			String searchKey = pagination.getSearchKey();
			boolean hasSearchKey = StringUtils.hasText(searchKey);
			if (hasSearchKey) {
				String query1 = partnerGroupsQuerySelect + partnerGroupsQueryFrom
						+ partnerGroupsSearchQuery.replace("searchKey", searchKey) + partnerGroupsGroupByQuery
						+ sortQueryString;

				String query2 = partnerGroupsCountQuerySelect + partnerGroupsQueryFrom + partnerGroupsGroupByQuery
						+ sortQueryString;

				finalQueryString = "select q1.*, q2.count as \"numberOfPartners\" from (" + query1 + ") as q1, ("
						+ query2 + ") as q2 where q1.id = q2.id";
			} else {
				finalQueryString = partnerGroupsQuerySelect + ", count(DISTINCT xuu.user_id) AS \"numberOfPartners\" "
						+ partnerGroupsQueryFrom + partnerGroupsGroupByQuery + sortQueryString;
			}
			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(finalQueryString);
			Integer companyId = pagination.getCompanyId();
			if (companyId == null || companyId.equals(0)) {
				companyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			}
			query.setParameter("companyId", companyId);
			List<String> activeAndInactivePartnerLists = new ArrayList<>();
			activeAndInactivePartnerLists.add(activePartnerListName);
			activeAndInactivePartnerLists.add(inActivePartnerListName);
			query.setParameterList("activeAndInactivePartnerList", activeAndInactivePartnerLists);
			return paginationUtil.setScrollableAndGetList(pagination, map, query, PartnerGroupDTO.class);
		} catch (HibernateException | PartnerDataAccessException e) {
			throw new PartnerDataAccessException(e);
		} catch (Exception ex) {
			throw new PartnerDataAccessException(ex);
		}

	}

	private String sortByForPartnerGroups(Pagination pagination) {
		String sortOptionQueryString = " ";
		if (pagination.getFiltertedEmailTempalteIds() != null && !pagination.getFiltertedEmailTempalteIds().isEmpty()) {
			sortOptionQueryString += "   order by array_position(array" + pagination.getFiltertedEmailTempalteIds()
					+ ", xul.user_list_id),xul.user_list_name asc nulls first";
		} else {
			sortOptionQueryString += " order by xul.user_list_id " + " asc";
		}
		return sortOptionQueryString;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findPartnershipIdsByPartnerIdsAndVendorCompanyId(List<Integer> partnerIds,
			Integer vendorCompanyId) {
		List<Integer> allPartnershipIds = new ArrayList<>();
		if (XamplifyUtils.isNotEmptyList(partnerIds)) {
			List<List<Integer>> chunkedPartnerIdsList = XamplifyUtils.getChunkedList(partnerIds);
			Session session = sessionFactory.getCurrentSession();
			String sqlQueryString = "select distinct id from xt_partnership where vendor_company_id = :vendorCompanyId "
					+ "and partner_id in (:partnerIds)";
			for (List<Integer> chunkedPartnerIds : chunkedPartnerIdsList) {
				Query query = session.createSQLQuery(sqlQueryString).setParameter("vendorCompanyId", vendorCompanyId)
						.setParameterList("partnerIds", chunkedPartnerIds);
				allPartnershipIds.addAll(query.list());
			}
		}
		return allPartnershipIds;

	}

	@Override
	public Integer findPartnershipIdByPartnerIdAndVendorCompanyId(Integer partnerId, Integer vendorCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		String sqlQueryString = "select distinct id from xt_partnership where vendor_company_id = :vendorCompanyId and partner_id = :partnerId";
		Query query = session.createSQLQuery(sqlQueryString).setParameter("vendorCompanyId", vendorCompanyId)
				.setParameter("partnerId", partnerId);
		return (Integer) query.uniqueResult();
	}

	@Override
	public UserDTO findEmailIdAndFullNameByUserId(Integer partnerId) {
		if (partnerId != null) {
			Session session = sessionFactory.getCurrentSession();
			String sqlString = " select distinct u.email_id as \"emailId\",concat(u.firstname,' ',u.lastname,' ',u.middle_name) as \"fullName\" from xt_user_profile u where u.user_id = :userId";
			SQLQuery query = session.createSQLQuery(sqlString);
			query.setParameter("userId", partnerId);
			query.setResultTransformer(Transformers.aliasToBean(UserDTO.class));
			return (UserDTO) query.uniqueResult();
		} else {
			return null;
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findPartnerCompanyIdsByVendorCompanyId(Integer vendorCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		String sqlQueryString = "select distinct partner_company_id from xt_partnership where vendor_company_id = :vendorCompanyId and partner_company_id is not null";
		Query query = session.createSQLQuery(sqlQueryString).setParameter("vendorCompanyId", vendorCompanyId);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findVendorCompanyIdsByPartnerCompanyId(Integer partnerCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		String sqlQueryString = "select distinct vendor_company_id from xt_partnership where partner_company_id = :partnerCompanyId";
		Query query = session.createSQLQuery(sqlQueryString).setParameter("partnerCompanyId", partnerCompanyId);
		return query.list();
	}

	@Override
	public Map<String, Object> findVendorInvitationReports(Pagination pagination) {
		try {
			HashMap<String, Object> map = new HashMap<>();
			String finalQueryString = "";
			String searchKey = pagination.getSearchKey();
			boolean hasSearchKey = StringUtils.hasText(searchKey);
			String findVendorInvitationQuery = " select  p.partner_id as \"inviterId\",partner.email_id as \"inviterEmailId\", "
					+ " vendorCompany.company_name as \"inviterCompanyName\",vendorCompany.company_logo as \"inviterCompanyLogo\", "
					+ " p.vendor_id as \"inviteeId\",vendor.email_id as \"inviteeEmailId\", partnerCompany.company_name as \"inviteeCompanyName\", "
					+ " partnerCompany.company_logo as \"inviteeCompanyLogo\",p.vendor_company_id as \"inviteeCompanyId\",p.created_time as \"createdTime\",cast(p.status as text) as \"inviteStatus\", "
					+ " p.updated_time as \"updatedTime\" from  xt_user_profile vendor,xt_company_profile vendorCompany, "
					+ " xt_user_profile partner,xt_partnership p " + " left join xt_company_profile partnerCompany "
					+ " on partnerCompany.company_id = p.vendor_company_id  "
					+ " where p.partner_id = partner.user_id and " + " vendorCompany.company_id = p.partner_company_id "
					+ " and p.vendor_id = vendor.user_id and p.status = '" + pagination.getFilterKey() + "' ";
			String findVendorInvitationSearchQuery = " and ( LOWER(partner.email_id) like LOWER('%searchKey%') OR LOWER(vendorCompany.company_name) like "
					+ "LOWER('%searchKey%') OR LOWER(vendor.email_id) like LOWER('%searchKey%')  OR LOWER(partnerCompany.company_name) like LOWER('%searchKey%') )";
			String orderByQuery = " order by p.created_time desc";
			if (hasSearchKey) {
				finalQueryString = findVendorInvitationQuery + " "
						+ findVendorInvitationSearchQuery.replace("%searchKey%", "%" + searchKey + "%") + " "
						+ orderByQuery;
			} else {
				finalQueryString = findVendorInvitationQuery + " " + orderByQuery;
			}
			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(finalQueryString);
			return paginationUtil.setScrollableAndGetList(pagination, map, query, VendorInvitationReport.class);
		} catch (HibernateException | PartnerDataAccessException e) {
			throw new PartnerDataAccessException(e);
		} catch (Exception ex) {
			throw new PartnerDataAccessException(ex);
		}

	}

	@Override
	public void updateNotifyPartners(Integer partnerId, Integer vendorCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		String sqlQueryString = "update xt_partnership set notify_partners = true where partner_id=:partnerId and vendor_company_id=:vendorCompanyId";
		Query query = session.createSQLQuery(sqlQueryString).setParameter("vendorCompanyId", vendorCompanyId)
				.setParameter("partnerId", partnerId);
		query.executeUpdate();

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findPartnerIdsByVendorCompanyId(Integer vendorCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		String sqlQueryString = "select distinct partner_id from xt_partnership where vendor_company_id = :vendorCompanyId and status = 'approved'";
		Query query = session.createSQLQuery(sqlQueryString).setParameter("vendorCompanyId", vendorCompanyId);
		return query.list();
	}

	@Override
	public Integer findPartnerIdByVendorCompanyIdAndPartnerCompanyId(Integer vendorCompanyId,
			Integer partnerCompanyId) {
		if (partnerCompanyId != null) {
			Session session = sessionFactory.getCurrentSession();
			String sqlQueryString = "select distinct partner_id from xt_partnership where vendor_company_id = :vendorCompanyId and partner_company_id=:partnerCompanyId";
			Query query = session.createSQLQuery(sqlQueryString).setParameter("vendorCompanyId", vendorCompanyId)
					.setParameter("partnerCompanyId", partnerCompanyId);
			return (Integer) query.uniqueResult();
		} else {
			return 0;
		}

	}

	@Override
	public void findAndDeletePartnerTeamMemberGroupMappingByPartnershipIdAndTeamMemberGroupMappingIds(
			Integer partnership, List<Integer> teamMemberGroupMappingIds) {
		if (partnership != null && teamMemberGroupMappingIds != null && !teamMemberGroupMappingIds.isEmpty()) {
			String sqlString = "delete from xt_partner_team_group_mapping where partnership_id = :partnershipId and team_member_group_user_mapping_id in (:teamMemberGroupMappingIds)";
			Session session = sessionFactory.getCurrentSession();
			session.createSQLQuery(sqlString).setParameter("partnershipId", partnership)
					.setParameterList("teamMemberGroupMappingIds", teamMemberGroupMappingIds).executeUpdate();

		}

	}

	@Override
	public void savePartnerTeamMemberGroupMapping(Set<PartnerTeamGroupMapping> partnerTeamGroupMappings) {
		try {
			if (partnerTeamGroupMappings != null && !partnerTeamGroupMappings.isEmpty()) {
				Session session = sessionFactory.getCurrentSession();
				List<PartnerTeamGroupMapping> partnerTeamGroupMappingsList = new ArrayList<PartnerTeamGroupMapping>(
						partnerTeamGroupMappings);
				for (int i = 0; i < partnerTeamGroupMappingsList.size(); i++) {
					session.save(partnerTeamGroupMappingsList.get(i));
					if (i % 30 == 0) {
						session.flush();
						session.clear();
					}
				}
			}

		} catch (HibernateException | TeamMemberDataAccessException e) {
			throw new TeamMemberDataAccessException(e.getMessage());
		} catch (Exception ex) {
			throw new TeamMemberDataAccessException(ex.getMessage());
		}
	}

	@Override
	public boolean isPartnerTeamMemberGroupMappingExists(Integer partnershipId, Integer teamMemberGroupUserMappingId) {
		String sqlString = "select case when count(*)>0 then true else false end as row_exists from xt_partner_team_group_mapping where partnership_id = :partnershipId and team_member_group_user_mapping_id = :mappingId";
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(sqlString).setParameter("partnershipId", partnershipId)
				.setParameter("mappingId", teamMemberGroupUserMappingId);
		return (boolean) query.uniqueResult();
	}

	@Override
	public PartnershipDTO findPartnerIdAndPartnerCompanyIdByPartnershipId(Integer partnershipId) {
		Session session = sessionFactory.getCurrentSession();
		String sqlString = " select partner_id as \"id\", partner_company_id as \"partnerCompanyId\" from xt_partnership where id=:id";
		SQLQuery query = session.createSQLQuery(sqlString);
		query.setParameter("id", partnershipId);
		query.setResultTransformer(Transformers.aliasToBean(PartnershipDTO.class));
		return (PartnershipDTO) query.uniqueResult();
	}

	@Override
	public PartnerCompanyDTO findOneClickLaunchCampaignPartnerCompany(Integer partnershipId) {
		String queryString = oneClickLaunchCampaignPartnerQuery + " " + partnerCompaniesQueryGroupBy;
		queryString = queryString.replace("and p.vendor_company_id = :vendorCompanyId", "");
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(queryString);
		query.setParameter("partnershipId", partnershipId);
		return (PartnerCompanyDTO) paginationUtil.getDto(PartnerCompanyDTO.class, query);
	}

	/*********** XNFR-220 **********/
	@Override
	public Map<String, Object> findAllPartnerCompanies(Pagination pagination) {
		try {
			HashMap<String, Object> map = new HashMap<>();
			String finalQueryString = "";
			String findPartnerCompaniesQuery = allPartnerCompaniesQuery;
			TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
					pagination.isPartnerTeamMemberGroupFilter(), true);
			boolean applyPartnershipIdsFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
			if (teamMemberFilterDTO.isEmptyFilter()) {
				return paginationUtil.returnEmptyList(map, new ArrayList<PartnerCompanyDTO>());
			} else {
				if (applyPartnershipIdsFilter) {
					findPartnerCompaniesQuery = findPartnerCompaniesQuery + " and p.id in (:partnershipIds)";
				}
			}
			String sortQueryString = getSelectedSortOptionForPartners(pagination);
			String searchKey = pagination.getSearchKey();
			boolean hasSearchKey = StringUtils.hasText(searchKey);
			if (hasSearchKey) {
				finalQueryString = findPartnerCompaniesQuery
						+ allPartnerCompaniesSearchQuery.replace("searchKey", searchKey) + " " + sortQueryString;
			} else {
				finalQueryString = findPartnerCompaniesQuery + " " + sortQueryString;
			}
			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(finalQueryString);
			query.setParameter("vendorCompanyId", pagination.getVendorCompanyId());
			utilDao.applyPartnershipIdsParameterList(applyPartnershipIdsFilter,
					teamMemberFilterDTO.getPartnershipIdsOrPartnerCompanyIds(), query);
			return paginationUtil.setScrollableAndGetList(pagination, map, query, PartnerCompanyDTO.class);
		} catch (HibernateException | PartnerDataAccessException e) {
			throw new PartnerDataAccessException(e);
		} catch (Exception ex) {
			throw new PartnerDataAccessException(ex);
		}

	}

	@Override
	public List<PartnerJourneyResponseDTO> findJourney(Integer partnershipId) {
		List<PartnerJourneyResponseDTO> partnerJourneyResponseDTOs = new ArrayList<PartnerJourneyResponseDTO>();
		Session session = sessionFactory.getCurrentSession();
		/******* OnBoarded On ******/
		PartnerJourneyResponseDTO partnerJourneyResponseDTO = new PartnerJourneyResponseDTO();
		String onBoardedOnQueryString = "select created_time as \"createdTime\",vendor_company_id as \"vendorCompanyId\", partner_company_id as \"partnerCompanyId\" from xt_partnership where id=:id";
		SQLQuery query = session.createSQLQuery(onBoardedOnQueryString);
		query.setParameter("id", partnershipId);
		PartnerCompanyDTO partnerCompanyDTO = (PartnerCompanyDTO) paginationUtil.getDto(PartnerCompanyDTO.class, query);
		if (partnerCompanyDTO != null) {
			Date createdTime = partnerCompanyDTO.getCreatedTime();
			Integer vendorCompanyId = partnerCompanyDTO.getVendorCompanyId();
			Integer partnerCompanyId = partnerCompanyDTO.getPartnerCompanyId();
			partnerJourneyResponseDTO.setOrder(1);
			partnerJourneyResponseDTO.setJourneyType(onboardedOnText);
			partnerJourneyResponseDTO.setCreatedTime(createdTime);
			partnerJourneyResponseDTOs.add(partnerJourneyResponseDTO);

			if (partnerCompanyId != null) {

				/******** Company Profile Created **********/
				PartnerJourneyResponseDTO companyProfileCreated = new PartnerJourneyResponseDTO();
				companyProfileCreated.setOrder(2);
				companyProfileCreated.setJourneyType(companyProfileCreatedOnText);
				companyProfileCreated.setCreatedTime(createdTime);
				partnerJourneyResponseDTOs.add(companyProfileCreated);

				/****** Team Member Created ******/
				String teamMemberCreatedOnQueryString = "select min(created_time) from xt_team_member where company_id = :partnerCompanyId";
				Date teamMemberCreateOn = getMinTime(session, partnerCompanyId, teamMemberCreatedOnQueryString);
				if (teamMemberCreateOn != null) {
					PartnerJourneyResponseDTO teamMemberCreatedOnJourneyDTO = new PartnerJourneyResponseDTO();
					teamMemberCreatedOnJourneyDTO.setOrder(3);
					teamMemberCreatedOnJourneyDTO.setJourneyType(teamMemberCreatedOnText);
					teamMemberCreatedOnJourneyDTO.setCreatedTime(teamMemberCreateOn);
					partnerJourneyResponseDTOs.add(teamMemberCreatedOnJourneyDTO);
				}

				/******* Contacts Created *********/
				String contactCreatedOnQueryString = "select min(created_time) from xt_user_list where company_id = :partnerCompanyId and module_name = 'CONTACTS'";
				Date contactsCreatedOn = getMinTime(session, partnerCompanyId, contactCreatedOnQueryString);
				if (contactsCreatedOn != null) {
					PartnerJourneyResponseDTO contactCreatedOnJourneyDTO = new PartnerJourneyResponseDTO();
					contactCreatedOnJourneyDTO.setOrder(4);
					contactCreatedOnJourneyDTO.setJourneyType(contactsUploadedOnText);
					contactCreatedOnJourneyDTO.setCreatedTime(contactsCreatedOn);
					partnerJourneyResponseDTOs.add(contactCreatedOnJourneyDTO);
				}

				/******* Campaign Redistributed *********/
				String campaignRedistributedOnQueryString = "select min(launch_time) from xt_campaign where customer_id in (select user_id from xt_user_profile where company_id = :partnerCompanyId)\r\n"
						+ " and vendor_organization_id = :vendorCompanyId and is_launched and is_nurture_campaign";
				SQLQuery campaignRedistributedQuery = session.createSQLQuery(campaignRedistributedOnQueryString);
				campaignRedistributedQuery.setParameter("partnerCompanyId", partnerCompanyId);
				campaignRedistributedQuery.setParameter("vendorCompanyId", vendorCompanyId);
				Date campaignRedistributedOn = (Date) campaignRedistributedQuery.uniqueResult();
				if (campaignRedistributedOn != null) {
					PartnerJourneyResponseDTO campaignRedistributedJourney = new PartnerJourneyResponseDTO();
					campaignRedistributedJourney.setOrder(5);
					campaignRedistributedJourney.setJourneyType(campaignRedistributedOnText);
					campaignRedistributedJourney.setCreatedTime(campaignRedistributedOn);
					partnerJourneyResponseDTOs.add(campaignRedistributedJourney);
				}

				/******* Lead Created *****/
				String leadCreatedQueryString = "select min(created_time) from xt_lead where created_for_company_id = :vendorCompanyId and created_by_company_id = :partnerCompanyId";
				Date leadCreatedOn = findLeadOrDealCreatedOn(session, vendorCompanyId, partnerCompanyId,
						leadCreatedQueryString);
				if (leadCreatedOn != null) {
					PartnerJourneyResponseDTO leadCreatedJourney = new PartnerJourneyResponseDTO();
					leadCreatedJourney.setCreatedTime(leadCreatedOn);
					leadCreatedJourney.setOrder(6);
					leadCreatedJourney.setJourneyType(leadCreatedOnText);
					partnerJourneyResponseDTOs.add(leadCreatedJourney);
				}

				/***** Deal Created ****/

				String dealCreatedQueryString = "select min(created_time) from xt_deal where created_for_company_id = :vendorCompanyId and created_by_company_id = :partnerCompanyId";
				Date dealCreatedOn = findLeadOrDealCreatedOn(session, vendorCompanyId, partnerCompanyId,
						dealCreatedQueryString);
				if (dealCreatedOn != null) {
					PartnerJourneyResponseDTO dealCreatedJourney = new PartnerJourneyResponseDTO();
					dealCreatedJourney.setOrder(7);
					dealCreatedJourney.setJourneyType(dealCreatedOnText);
					dealCreatedJourney.setCreatedTime(dealCreatedOn);
					partnerJourneyResponseDTOs.add(dealCreatedJourney);
				}

			}

		}

		return partnerJourneyResponseDTOs;
		/********* Adding A Team Member *********/
	}

	private Date findLeadOrDealCreatedOn(Session session, Integer vendorCompanyId, Integer partnerCompanyId,
			String leadCreatedQueryString) {
		SQLQuery leadOrDealCreatedQuery = session.createSQLQuery(leadCreatedQueryString);
		leadOrDealCreatedQuery.setParameter("vendorCompanyId", vendorCompanyId);
		leadOrDealCreatedQuery.setParameter("partnerCompanyId", partnerCompanyId);
		Date leadOrDealCreatedOn = (Date) leadOrDealCreatedQuery.uniqueResult();
		return leadOrDealCreatedOn;
	}

	private Date getMinTime(Session session, Integer partnerCompanyId, String teamMemberCreatedOnQueryString) {
		SQLQuery query = session.createSQLQuery(teamMemberCreatedOnQueryString);
		query.setParameter("partnerCompanyId", partnerCompanyId);
		Date date = (Date) query.uniqueResult();
		return date;
	}

	@Override
	public boolean isPartnershipEstablishedByVendorCompanyIdAndPartnerCompanyId(Integer vendorCompanyId,
			Integer partnerCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		String sqlString = "select case when count(*)>0 then true else false end from xt_partnership where vendor_company_id = :vendorCompanyId and partner_company_id = :partnerCompanyId";
		SQLQuery query = session.createSQLQuery(sqlString);
		query.setParameter("vendorCompanyId", vendorCompanyId);
		query.setParameter("partnerCompanyId", partnerCompanyId);
		return (boolean) query.uniqueResult();
	}

	@Override
	public Map<String, Object> findPartnerCompaniesForSharingWhiteLabeledContent(Pagination pagination) {
		try {
			HashMap<String, Object> map = new HashMap<>();
			String finalQueryString = "";
			/*********** XNFR-85 **********/
			String findPartnerCompaniesQuery = partnerCompaniesQuery;
			TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
					pagination.isPartnerTeamMemberGroupFilter(), true);
			boolean applyPartnershipIdsFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
			if (teamMemberFilterDTO.isEmptyFilter()) {
				return paginationUtil.returnEmptyList(map, new ArrayList<>());
			} else {
				if (applyPartnershipIdsFilter) {
					findPartnerCompaniesQuery = findPartnerCompaniesQuery + " and p.id in (:partnershipIds)";
				}
			}
			String sortQueryString = getSelectedSortOptionForPartners(pagination);
			String searchKey = pagination.getSearchKey();
			boolean hasSearchKey = StringUtils.hasText(searchKey);
			if (hasSearchKey) {
				finalQueryString = findPartnerCompaniesQuery
						+ partnerCompaniesSearchQuery.replace("searchKey", searchKey) + partnerCompaniesQueryGroupBy
						+ " " + sortQueryString;
			} else {
				finalQueryString = findPartnerCompaniesQuery + partnerCompaniesQueryGroupBy + " " + sortQueryString;
			}
			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(finalQueryString);
			query.setParameter("vendorCompanyId", pagination.getVendorCompanyId());
			utilDao.applyPartnershipIdsParameterList(applyPartnershipIdsFilter,
					teamMemberFilterDTO.getPartnershipIdsOrPartnerCompanyIds(), query);
			return paginationUtil.setScrollableAndGetList(pagination, map, query, PartnerCompanyDTO.class);
		} catch (HibernateException | PartnerDataAccessException e) {
			throw new PartnerDataAccessException(e);
		} catch (Exception ex) {
			throw new PartnerDataAccessException(ex);
		}

	}

	// XNFR-316
	@Override
	public Map<String, Object> getActivePartnerCompanies(Pagination pagination) {
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), true);
		Session session = sessionFactory.getCurrentSession();
		Integer userId = pagination.getUserId();
		Integer companyId = userDao.getCompanyIdByUserId(userId);
		String status = pagination.getPartnershipStatus();

		String searchQuery = "";
		if (StringUtils.hasText(pagination.getSearchKey())) {
			String searchKey = "'%" + pagination.getSearchKey() + "%'";
			searchQuery = " and (xup1.email_id ilike " + searchKey + " OR " + "xcp.company_name ilike " + searchKey
					+ " OR " + "xuul.firstname ilike " + searchKey + " OR " + "xuul.lastname ilike " + searchKey
					+ " OR " + "xuul.contact_company ilike " + searchKey + " OR " + "xuul.job_title ilike " + searchKey
					+ " OR " + "xuul.mobile_number ilike " + searchKey + ") ";
		}
		String sortSql = "";
		if (pagination.getSortcolumn() != null) {
			if (pagination.getSortcolumn().equalsIgnoreCase("campaign")) {
				sortSql = " order by xcp.company_name " + pagination.getSortingOrder() + " NULLS LAST";
			} else if (pagination.getSortcolumn().equalsIgnoreCase("campaign")) {
				sortSql = " order by xcp.company_name  " + pagination.getSortingOrder() + " NULLS LAST";
			} else if (pagination.getSortcolumn().equalsIgnoreCase("createdTime")) {
				sortSql = " order by xp.created_time  " + pagination.getSortingOrder() + " NULLS LAST";
			} else if (pagination.getSortcolumn().equalsIgnoreCase("createdTime")) {
				sortSql = " order by xp.created_time " + pagination.getSortingOrder() + " NULLS LAST";
			}
		}
		String sql = "SELECT DISTINCT xp.partner_id as \"partnerId\", xup1.company_id AS \"companyId\", "
				+ "xcp.company_logo AS \"companyLogo\", " + "xcp.company_name AS \"companyName\", "
				+ "xuul.contact_company AS \"partnerCompanyName\", " + "xup1.email_id AS \"emailId\", "
				+ "xuul.firstname AS \"firstName\", xuul.lastname AS \"lastName\", "
				+ "xuul.job_title AS \"jobTitle\", " + "xuul.mobile_number AS \"mobileNumber\", "
				+ "xp.created_time AS \"createdDate\" " + "FROM xt_partnership xp "
				+ "LEFT JOIN xt_user_list xul ON xul.company_id = xp.vendor_company_id "
				+ "LEFT JOIN xt_user_userlist xuul ON xul.user_list_id = xuul.user_list_id AND xp.partner_id = xuul.user_id "
				+ "LEFT JOIN xt_user_profile xup1 ON xuul.user_id = xup1.user_id "
				+ "LEFT JOIN xt_company_profile xcp ON xcp.company_id = xup1.company_id "
				+ "WHERE xp.vendor_company_id = " + companyId + " AND xul.is_default_partnerlist = TRUE "
				+ " AND xup1.company_id IS NOT NULL " + " AND xp.status ={status} and xcp.company_name_status='active' "
				+ getPartnerTeamMemberGroupFilterSQL("xup1.company_id", userId, teamMemberFilterDTO, status)
				+ " AND xp.partner_company_id IN " + "(SELECT DISTINCT up.company_id " + "FROM xt_lead l "
				+ "LEFT JOIN xt_user_profile up ON l.created_by = up.user_id " + "WHERE l.created_for_company_id = "
				+ companyId + " AND l.created_for_company_id != l.created_by_company_id "
				+ " AND up.company_id IS NOT NULL "
				+ getPartnerTeamMemberGroupFilterSQL("l.created_by_company_id", userId, teamMemberFilterDTO, status)
				+ "  " + " UNION " + "SELECT DISTINCT up.company_id " + "FROM xt_deal d "
				+ "LEFT JOIN xt_user_profile up ON d.created_by = up.user_id " + "WHERE d.created_for_company_id = "
				+ companyId + " AND d.created_for_company_id != d.created_by_company_id "
				+ " AND up.company_id IS NOT NULL "
				+ getPartnerTeamMemberGroupFilterSQL("d.created_by_company_id", userId, teamMemberFilterDTO, status)
				+ " " + " UNION " + "SELECT DISTINCT p.partner_company_id " + "FROM xt_learning_track_visibility xltv "
				+ "LEFT JOIN xt_learning_track xlt ON xltv.learning_track_id = xlt.id "
				+ "LEFT JOIN xt_partnership p ON p.vendor_company_id = xlt.company_id "
				+ " AND xltv.partnership_id = p.id "
				+ "LEFT JOIN xt_learning_track_content_partner_activity xltcp ON xltcp.learning_track_visibility_id = xltv.id "
				+ "WHERE p.vendor_company_id = " + companyId
				+ " AND p.partner_company_id IS NOT NULL and xlt.type = 'TRACK' and xlt.is_published = true and xltv.progress>0 "
				+ getPartnerTeamMemberGroupFilterSQL("p.partner_company_id", userId, teamMemberFilterDTO, status) + " "
				+ " UNION " + "SELECT DISTINCT p.partner_company_id " + "FROM xt_learning_track_visibility xltv "
				+ "LEFT JOIN xt_learning_track xlt ON xltv.learning_track_id = xlt.id "
				+ "LEFT JOIN xt_partnership p ON p.vendor_company_id = xlt.company_id "
				+ " AND xltv.partnership_id = p.id "
				+ "LEFT JOIN xt_learning_track_content_partner_activity xltcp ON xltcp.learning_track_visibility_id = xltv.id "
				+ "WHERE p.vendor_company_id = " + companyId
				+ " AND p.partner_company_id IS NOT NULL and xlt.type = 'PLAYBOOK' and xlt.is_published = true and xltv.progress>0 "
				+ getPartnerTeamMemberGroupFilterSQL("p.partner_company_id", userId, teamMemberFilterDTO, status) + " "
				+ "UNION " + "SELECT DISTINCT xp.partner_company_id " + "FROM xt_dam_partner xdp "
				+ "LEFT JOIN xt_dam xd ON xd.id = xdp.dam_id "
				+ "LEFT JOIN xt_dam_partner_mapping xdpm ON xdpm.dam_partner_id = xdp.id "
				+ "left join xt_dam_analytics xda on xda.dam_partner_mapping_fk_id=xdpm.id "
				+ "LEFT JOIN xt_partnership xp ON xp.id = xdp.partnership_id "
				+ " AND xd.company_id = xp.vendor_company_id " + "WHERE xp.vendor_company_id = " + companyId
				+ " AND xp.partner_company_id IS NOT NULL and xda.action_type ='VIEW' "
				+ getPartnerTeamMemberGroupFilterSQL("xp.partner_company_id", userId, teamMemberFilterDTO, status) + " "
				+ " UNION " + "SELECT distinct p.partner_company_id " + "FROM xt_dam_partner dp "
				+ "JOIN xt_dam d on dp.dam_id = d.id "
				+ "JOIN xt_partnership p on p.id=dp.partnership_id and d.company_id =p.vendor_company_id "
				+ "JOIN xt_company_profile c on p.partner_company_id = c.company_id "
				+ "JOIN xt_dam_partner_group_mapping dpgm on dp.id = dpgm.dam_partner_id "
				+ "left join xt_dam_analytics xda on xda.dam_partner_fk_id=dp.id " + "WHERE p.vendor_company_id = "
				+ companyId + " and xda.action_type ='VIEW' "
				+ getPartnerTeamMemberGroupFilterSQL("p.partner_company_id", userId, teamMemberFilterDTO, status) + " "
				+ "UNION " + "SELECT DISTINCT up1.company_id " + "FROM xt_campaign c "
				+ "LEFT JOIN xt_campaign p ON (c.campaign_id = p.parent_campaign_id) "
				+ "LEFT JOIN xt_user_profile up ON (up.user_id = c.customer_id) "
				+ "LEFT JOIN xt_user_profile up1 ON (up1.user_id = p.customer_id) " + "WHERE up.company_id = "
				+ companyId + " AND p.vendor_organization_id = " + companyId + " AND p.is_launched = TRUE "
				+ " AND p.is_nurture_campaign = TRUE " + " AND p.campaign_id IS NOT NULL "
				+ " AND up1.company_id IS NOT NULL "
				+ getPartnerTeamMemberGroupFilterSQL("up1.company_id", userId, teamMemberFilterDTO, status) + ")";
		sql = replacePartnershipStatus(sql, status);
		if (pagination.getSelectedPartnerCompanyIds() != null && !pagination.getSelectedPartnerCompanyIds().isEmpty()) {
			sql += " and xup1.company_id in (:partnerCompanyIds) ";
		}
		sql = sql + searchQuery + sortSql;
		SQLQuery query = session.createSQLQuery(sql);
		if (pagination.getSelectedPartnerCompanyIds() != null && !pagination.getSelectedPartnerCompanyIds().isEmpty()) {
			query.setParameterList("partnerCompanyIds", pagination.getSelectedPartnerCompanyIds());
		}
		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		query.setMaxResults(pagination.getMaxResults());
		@SuppressWarnings("unchecked")
		List<PartnerDTO> list = query.setResultTransformer(Transformers.aliasToBean(PartnerDTO.class)).list();
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("totalRecords", totalRecords);
		resultMap.put("list", list);
		return resultMap;
	}

	private String getPartnerTeamMemberGroupFilterSQL(String alias, Integer userId,
			TeamMemberFilterDTO teamMemberFilterDTO, String status) {
		String partnerTeamMemberGroupFilterSQL = "";
		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			partnerTeamMemberGroupFilterSQL = " AND  " + alias + " IN (" + "SELECT DISTINCT p.partner_company_id "
					+ "FROM xt_team_member t "
					+ "LEFT OUTER JOIN xt_team_member_group_user_mapping tgum ON t.id = tgum.team_member_id "
					+ "LEFT OUTER JOIN xt_partner_team_group_mapping ptgm ON tgum.id = ptgm.team_member_group_user_mapping_id "
					+ "LEFT OUTER JOIN xt_partnership p ON ptgm.partnership_id = p.id "
					+ "LEFT JOIN xt_campaign xc ON xc.customer_id = tgum.team_member_id " + "WHERE t.team_member_id = "
					+ userId + " AND p.partner_id IS NOT NULL AND p.status ={status} ) ";
			partnerTeamMemberGroupFilterSQL = replacePartnershipStatus(partnerTeamMemberGroupFilterSQL, status);
		}
		return partnerTeamMemberGroupFilterSQL;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Partnership> findAllApprovedPartnerships() {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(Partnership.class);
		criteria.add(Restrictions.eq("status", PartnershipStatus.APPROVED));
		return criteria.list();
	}

	@Override
	public Integer getPartnerCompanyIdByPartnershipId(Integer selectedPartnershipId) {
		if (XamplifyUtils.isValidInteger(selectedPartnershipId)) {
			String queryString = "select partner_company_id from xt_partnership where id = :id";
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("id", selectedPartnershipId));
			return (Integer) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
		} else {
			return 0;
		}

	}

	@Override
	public Integer getPartnerIdByPartnershipId(Integer selectedPartnershipId) {
		if (XamplifyUtils.isValidInteger(selectedPartnershipId)) {
			String queryString = "select partner_id from xt_partnership where id = :id";
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("id", selectedPartnershipId));
			return (Integer) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
		} else {
			return 0;
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> listAllPartnershipIdsByPartnerCompanyId(Integer partnerCompanyId) {
		if (partnerCompanyId != null) {
			Session session = sessionFactory.getCurrentSession();
			Query query = session
					.createSQLQuery("select id from xt_partnership where partner_company_id=:partnerCompanyId")
					.setParameter("partnerCompanyId", partnerCompanyId);
			return query.list();
		} else {
			return new ArrayList<>();
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getVendorsByPartnerId(Integer partnerId) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select vendor_id from xt_partnership where partner_id = :id";
		SQLQuery query = session.createSQLQuery(queryString);
		query.setParameter("id", partnerId);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getExistingPartnerCompanyNamesByVendorCompanyId(Integer vendorCompanyId) {
		if (XamplifyUtils.isValidInteger(vendorCompanyId)) {
			Session session = sessionFactory.getCurrentSession();
			String queryString = "select distinct trim(lower(cp.company_name)) from xt_partnership pa, xt_company_profile cp "
					+ "where pa.vendor_company_id = :vendorCompanyId and pa.partner_company_id = cp.company_id ";
			SQLQuery query = session.createSQLQuery(queryString);
			query.setParameter("vendorCompanyId", vendorCompanyId);
			return query.list();
		} else {
			return Collections.emptyList();
		}

	}

	@Override
	public Integer addedAdminId(Integer partnerId) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select a.vendor_id from "
				+ " (select distinct row_number() over( order by created_time) rn , vendor_id from xt_partnership where partner_id = :partnerId)a "
				+ " where rn=1";
		SQLQuery query = session.createSQLQuery(queryString);
		query.setParameter("partnerId", partnerId);
		return (Integer) query.uniqueResult();

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<DashboardButtonsPartnersDTO> findPartnerIdsAndPartnershipIdsByPartnerIdsAndCompanyId(
			Set<Integer> partnerIds, Integer companyId) {
		if (XamplifyUtils.isNotEmptySet(partnerIds)) {
			List<Integer> partnerIdsList = XamplifyUtils.convertSetToList(partnerIds);
			String queryString = "select distinct  p.id as \"partnershipId\",up.user_id as \"partnerId\" \n"
					+ "from xt_partnership p,xt_user_profile up where up.company_id = p.partner_company_id \n"
					+ "and p.vendor_company_id = :companyId and up.user_id in (:partnerIds) and p.status = 'approved'";
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("companyId", companyId));
			hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs()
					.add(new QueryParameterListDTO("partnerIds", partnerIdsList));
			hibernateSQLQueryResultRequestDTO.setClassInstance(DashboardButtonsPartnersDTO.class);
			return (List<DashboardButtonsPartnersDTO>) hibernateSQLQueryResultUtilDao
					.returnDTOList(hibernateSQLQueryResultRequestDTO);
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public void updateSalesforceAccountIdAndAccountNameByParterCompanyIdAndVendorCompanyId(Integer vendorCompanyId,
			Integer partnerCompanyId, String partnerSalesforceAccountId, String partnerSalesforceAccountName) {
		Session session = sessionFactory.getCurrentSession();
		String sqlQueryString = "update xt_partnership set partner_salesforce_account_id = :partnerSalesforceAccountId, partner_salesforce_account_name =:partnerSalesforceAccountName where vendor_company_id =:vendorCompanyId and partner_company_id =:partnerCompanyId";
		Query query = session.createSQLQuery(sqlQueryString).setParameter("vendorCompanyId", vendorCompanyId)
				.setParameter("partnerCompanyId", partnerCompanyId)
				.setParameter("partnerSalesforceAccountId", partnerSalesforceAccountId)
				.setParameter("partnerSalesforceAccountName", partnerSalesforceAccountName);
		query.executeUpdate();
	}

	@Override
	public PartnershipDTO getAccountIdByPartnerCompanyIdAndVendorCompanyId(Integer vendorCompanyId,
			Integer partnerCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		String sqlQueryString = "select partner_salesforce_account_id as \"partnerSalesforceAccountId\", partner_salesforce_account_name as \"partnerSalesforceAccountName\" from xt_partnership where vendor_company_id = :vendorCompanyId and partner_company_id = :partnerCompanyId";
		SQLQuery query = session.createSQLQuery(sqlQueryString);
		query.setParameter("vendorCompanyId", vendorCompanyId);
		query.setParameter("partnerCompanyId", partnerCompanyId);
		return (PartnershipDTO) paginationUtil.getDto(PartnershipDTO.class, query);
	}

	@Override
	public Partnership getPartnershipByPartnerSalesforceAccountId(String partnerSalesforceAccountId,
			Integer vendorCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		Partnership partnership = null;
		if (partnerSalesforceAccountId != null && vendorCompanyId != null) {
			org.hibernate.Criteria criteria = session.createCriteria(Partnership.class);
			criteria.add(Restrictions.eq("partnerSalesforceAccountId", partnerSalesforceAccountId));
			criteria.add(Restrictions.eq(VENDOR_COMPANY + ".id", vendorCompanyId));
			partnership = (Partnership) criteria.uniqueResult();
		}
		return partnership;
	}

	@Override
	public Map<String, Object> findVendorCompaniesPagination(Pagination pagination, String search) {
		String queryString = "select xcp.company_id as \"companyId\", xcp.company_logo as \"companyLogo\", xcp.company_name as \"companyName\", xp.id as \"partnershipId\" from xt_partnership xp join xt_company_profile xcp on xcp.company_id = xp.vendor_company_id \r\n"
				+ "  join xt_module_access xma on xma.company_id =xp.vendor_company_id "
				+ " where xp.\"status\" ='approved' and xp.partner_company_id = :companyId and xma.vendor_marketplace ";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		QueryParameterDTO queryParameterDTO = new QueryParameterDTO(XamplifyConstants.COMPANY_ID,
				pagination.getCompanyId());
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(queryParameterDTO);
		hibernateSQLQueryResultRequestDTO.setClassInstance(VendorLogoDTO.class);
		List<String> searchColumns = new ArrayList<>();
		searchColumns.add("xcp.company_name");
		hibernateSQLQueryResultRequestDTO.setSearchColumns(searchColumns);
		return hibernateSQLQueryResultUtilDao.returnPaginatedDTOList(hibernateSQLQueryResultRequestDTO, pagination,
				search);

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PartnershipDTO> getPartnershipByPartnerCompanyDomain(String partnerCompanyDomain,
			Integer vendorCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		String sqlQueryString = "select p.id, p.partner_company_id as \"partnerCompanyId\", p.partner_salesforce_account_id as \"partnerSalesforceAccountId\", p.partner_salesforce_account_name as \"partnerSalesforceAccountName\", p.partner_id as \"representingPartnerId\" from xt_partnership p, xt_partner_company_domain pcd "
				+ "where p.id= pcd.partnership_id  and p.vendor_company_id = :vendorCompanyId and pcd.partner_company_domain = :partnerCompanyDomain order by p.id";
		SQLQuery query = session.createSQLQuery(sqlQueryString);
		query.setParameter("vendorCompanyId", vendorCompanyId);
		query.setParameter("partnerCompanyDomain", partnerCompanyDomain);
		return (List<PartnershipDTO>) paginationUtil.getListDTO(PartnershipDTO.class, query);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PartnershipDTO> getPartnershipsByPartnerSalesforceAccountIds(List<String> partnerSalesforceAccountIds,
			Integer vendorCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		String sqlQueryString = "select p.id, p.partner_company_id as \"partnerCompanyId\", p.partner_salesforce_account_id as \"partnerSalesforceAccountId\", p.partner_salesforce_account_name as \"partnerSalesforceAccountName\" from xt_partnership p "
				+ "where p.vendor_company_id = :vendorCompanyId and p.partner_salesforce_account_id in (:partnerSalesforceAccountIds) order by p.id";
		SQLQuery query = session.createSQLQuery(sqlQueryString);
		query.setParameter("vendorCompanyId", vendorCompanyId);
		query.setParameterList("partnerSalesforceAccountIds", partnerSalesforceAccountIds);
		return (List<PartnershipDTO>) paginationUtil.getListDTO(PartnershipDTO.class, query);
	}

	@Override
	public void updateAccountDetailsInUserUserListForPartner(UserDTO userDTO, PartnershipDTO partnershipDTO,
			Integer vendorCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		String sqlQueryString = "UPDATE xt_user_userlist uul SET region = :region, territory = :territory, "
				+ "company_domain = :companyDomain, country = :country, account_sub_type = :accountSubType "
				+ "FROM xt_user_list ul WHERE uul.user_list_id = ul.user_list_id AND ul.company_id = :vendorCompanyId "
				+ "AND uul.user_id = :userId";

		Query query = session.createSQLQuery(sqlQueryString).setParameter("region", userDTO.getRegion())
				.setParameter("territory", userDTO.getTerritory())
				.setParameter("companyDomain", userDTO.getCompanyDomain()).setParameter("country", userDTO.getCountry())
				.setParameter("accountSubType", userDTO.getAccountSubType())
				.setParameter("vendorCompanyId", vendorCompanyId)
				.setParameter("userId", partnershipDTO.getRepresentingPartnerId());
		query.executeUpdate();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getDomainsByPartnershipId(Integer partnershipId) {
		if (XamplifyUtils.isValidInteger(partnershipId)) {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createSQLQuery(
					"select partner_company_domain from xt_partner_company_domain where partnership_id=:partnershipId")
					.setParameter("partnershipId", partnershipId);
			return query.list();
		} else {
			return Collections.emptyList();
		}
	}

	public boolean isPartnershipEstablishedAndApporved(Integer vendorCompanyId, Integer partnerCompanyId) {
		if (partnerCompanyId != null) {
			String sqlString = "select case when count(*)>0  then true else false end as partnershipEstablished from xt_partnership where partner_company_id = :partnerCompanyId and vendor_company_id = :vendorCompanyId and status = 'approved'";
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createSQLQuery(sqlString).setParameter("partnerCompanyId", partnerCompanyId)
					.setParameter("vendorCompanyId", vendorCompanyId);
			return (boolean) query.uniqueResult();
		} else {
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CompanyDTO> findVendorCompanyDetailsByPartnerUserId(Integer partnerUserId) {
		Session session = sessionFactory.getCurrentSession();
		String sqlQueryString = "select distinct cp.company_name as \"name\",cp.company_id as \"id\" from xt_partnership p,xt_company_profile cp\r\n"
				+ "where p.partner_id = :partnerUserId and p.vendor_company_id = cp.company_id and p.status = 'approved' order by 1 asc";
		SQLQuery query = session.createSQLQuery(sqlQueryString);
		query.setParameter("partnerUserId", partnerUserId);
		return (List<CompanyDTO>) paginationUtil.getListDTO(CompanyDTO.class, query);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PartnershipDTO> getPartnershipDtoByPartnerCompanyDomain(String domainName, Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String sqlQueryString = "select p.id, p.partner_company_id as \"partnerCompanyId\",p.partner_id as \"representingPartnerId\" from xt_partnership p, xt_partner_company_domain pcd "
				+ "where p.id= pcd.partnership_id  and p.vendor_company_id = :vendorCompanyId and pcd.partner_company_domain = :partnerCompanyDomain "
				+ " and p.partner_company_id is not null order by p.id";
		SQLQuery query = session.createSQLQuery(sqlQueryString);
		query.setParameter("vendorCompanyId", companyId);
		query.setParameter("partnerCompanyDomain", domainName);
		return (List<PartnershipDTO>) paginationUtil.getListDTO(PartnershipDTO.class, query);
	}

	/** XNFR-891 **/
	@Override
	public void updatePartnershipCompanyIdByPartnerId(Integer partnerId, Integer partnerCompanyId) {
		String queryString = "update xt_partnership set partner_company_id = :partnerCompanyId where partner_id = :partnerId";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("partnerId", partnerId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("partnerCompanyId", partnerCompanyId));
		hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<UserDTO> getOwnersAndChannelAccountManagers(Integer companyId, Integer partnerShipId) {
		List<Integer> adminAndPartnerRoleIds = Arrays.stream(ALL_ADMIN_AND_PARTNER_ROLE.split(","))
				.map(Integer::parseInt).collect(Collectors.toList());

		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();

		String channelManagerQuery = partnerShipId == null ? ""
				: " SELECT DISTINCT xup.user_id as \"userId\", xup.email_id as \"emailId\", xup.firstName \"firstName\", xup.lastName as \"lastName\"  "
						+ "FROM xt_user_profile xup " + "JOIN xt_team_member tm ON xup.user_id = tm.team_member_id\r\n"
						+ "JOIN xt_team_member_group_user_mapping tmgu ON tm.id = tmgu.team_member_id\r\n"
						+ "JOIN xt_partner_team_group_mapping ptgm ON tmgu.id = ptgm.team_member_group_user_mapping_id\r\n"
						+ "WHERE ptgm.partnership_id = :partnerShipId\r\n" + "UNION \r\n";
		String adminQuery = " SELECT DISTINCT xup.user_id as \"userId\", xup.email_id as \"emailId\", xup.firstName \"firstName\", xup.lastName as \"lastName\"  "
				+ "FROM xt_user_profile xup\r\n" + "JOIN xt_user_role xur ON xur.user_id = xup.user_id\r\n"
				+ "WHERE xur.role_id IN (:adminAndPartnerRoleIds) \r\n" + "AND xup.company_id = :companyId ";
		String sql = channelManagerQuery + adminQuery;

		hibernateSQLQueryResultRequestDTO.setQueryString(sql);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("companyId", companyId));
		if (partnerShipId != null) {
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("partnerShipId", partnerShipId));
		}
		hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs()
				.add(new QueryParameterListDTO("adminAndPartnerRoleIds", adminAndPartnerRoleIds));

		hibernateSQLQueryResultRequestDTO.setClassInstance(UserDTO.class);
		return (List<UserDTO>) hibernateSQLQueryResultUtilDao.returnDTOList(hibernateSQLQueryResultRequestDTO);
	}

	/***** XNFR-988 *****/
	@Override
	public void updatePartnerShipStatusForPartner(String partnerStatus, List<Integer> partnershipIds) {
		boolean isDeactivated = partnerStatus.equals("approved") ? false : true;
		Date deactivatedOn = new Date();
		String deactivatedOnQuery = isDeactivated ? " , deactivated_on= :deactivatedOn " : "";

		String queryString = "update xt_partnership set status = '" + partnerStatus + "'" + deactivatedOnQuery
				+ " where id in (:partnershipIds)";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs()
				.add(new QueryParameterListDTO("partnershipIds", partnershipIds));
		if (isDeactivated) {
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("deactivatedOn", deactivatedOn));
		}
		hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> findDeactivatedDomainsByCompanyId(Integer companyId) {
		String queryString = "select domain_name from xt_allowed_domain \n"
				+ "where company_id = :companyId and is_domain_deactivated is true";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId));
		return (List<String>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> findDeactivatedPartnersByCompanyId(Integer companyId) {
		String queryString = "SELECT xup.email_id AS \"emailId\" FROM xt_partnership xps \n"
				+ "JOIN xt_user_profile xup ON xps.partner_id = xup.user_id \n"
				+ "WHERE xps.vendor_company_id = :companyId AND xps.status = 'deactivated' \n"
				+ "UNION\n SELECT xup.email_id AS \"emailId\" FROM xt_partnership p \n"
				+ "JOIN xt_team_member tm ON tm.org_admin_id = p.partner_id \n"
				+ "JOIN xt_user_profile xup ON xup.user_id = tm.team_member_id \n"
				+ "WHERE p.vendor_company_id = 1584 AND p.status = 'deactivated' \n"
				+ "AND tm.team_member_id != p.partner_id";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId));
		return (List<String>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public String findPartnerShipStatusByPartnerCompanyIdAndVendorCompanyId(Integer partnerCompanyId,
			Integer vendorCompanyId) {
		String queryString = "select status from xt_partnership \n where vendor_company_id = :companyId "
				+ "and partner_company_id = :partnerCompanyId";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("partnerCompanyId", partnerCompanyId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, vendorCompanyId));
		return (String) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> findPartnerCompaniesByDomain(PartnershipDTO partnershipDTO) {
		Map<String, Object> response = new HashMap<>();
		Session session = sessionFactory.getCurrentSession();
		String sqlQueryString = "SELECT p.id AS \"id\", p.partner_company_id AS \"partnerCompanyId\", p.partner_id AS \"representingPartnerId\", p.status as \"status\", u.email_id AS \"partnerEmailId\", c.company_name AS \"partnerCompanyName\" FROM xt_partnership p LEFT JOIN xt_user_profile u ON u.user_id = p.partner_id LEFT JOIN xt_company_profile c ON c.company_id = p.partner_company_id WHERE p.partner_id IN (SELECT user_id FROM xt_user_profile WHERE email_id ILIKE :domainName AND user_id IN (SELECT partner_id FROM xt_partnership WHERE vendor_company_id = :vendorCompanyId)) AND p.vendor_company_id = :vendorCompanyId and p.status = '"
				+ partnershipDTO.getStatus() + "' ";
		SQLQuery query = session.createSQLQuery(sqlQueryString);
		query.setParameter("vendorCompanyId", partnershipDTO.getVendorCompanyId());
		query.setParameter("domainName", "%" + partnershipDTO.getDomainName() + "%");
		List<PartnershipDTO> partners = (List<PartnershipDTO>) paginationUtil.getListDTO(PartnershipDTO.class, query);

		String countQueryString = "SELECT COUNT(*) FROM xt_partnership p WHERE p.partner_id IN (SELECT user_id FROM xt_user_profile WHERE email_id ILIKE :domainName AND user_id IN (SELECT partner_id FROM xt_partnership WHERE vendor_company_id = :vendorCompanyId)) AND p.vendor_company_id = :vendorCompanyId AND p.status = '"
				+ partnershipDTO.getStatus() + "' ";
		SQLQuery countQuery = session.createSQLQuery(countQueryString);
		countQuery.setParameter("vendorCompanyId", partnershipDTO.getVendorCompanyId());
		countQuery.setParameter("domainName", "%" + partnershipDTO.getDomainName() + "%");
		Integer count = ((Number) countQuery.uniqueResult()).intValue();
		response.put("data", partners);
		response.put("totalCount", count);
		return response;
	}

	@Override
	public void updatePartnerCompaniesByDomain(PartnershipDTO partnershipDTO, Date updatedOn) {
		String status = partnershipDTO.getStatus();
		boolean isDeactivated = !"approved".equalsIgnoreCase(status);
		String deactivatedOnQuery = isDeactivated ? " , deactivated_on= :deactivatedOn " : "";
		String queryString = "UPDATE xt_partnership SET status = '" + status + "'" + deactivatedOnQuery
				+ " WHERE partner_id IN (SELECT user_id FROM xt_user_profile \n"
				+ "WHERE email_id ILIKE :domainName AND user_id IN (SELECT partner_id FROM xt_partnership \n"
				+ "WHERE vendor_company_id = :vendorCompanyId)) AND vendor_company_id = :vendorCompanyId "
				+ "AND (source <> 'invitation' OR (source = 'invitation' AND status <> 'invited'))";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("domainName", "%" + partnershipDTO.getDomainName() + "%"));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("vendorCompanyId", partnershipDTO.getVendorCompanyId()));
		if (isDeactivated) {
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("deactivatedOn", updatedOn));
		}
		hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);

		String queryString2 = "UPDATE xt_allowed_domain SET is_domain_deactivated = " + isDeactivated
				+ deactivatedOnQuery + " WHERE id = :domainId";
		HibernateSQLQueryResultRequestDTO request2 = new HibernateSQLQueryResultRequestDTO();
		request2.setQueryString(queryString2);
		request2.getQueryParameterDTOs().add(new QueryParameterDTO("domainId", partnershipDTO.getDomainId()));
		if (isDeactivated) {
			request2.getQueryParameterDTOs().add(new QueryParameterDTO("deactivatedOn", updatedOn));
		}
		hibernateSQLQueryResultUtilDao.update(request2);
	}

	@Override
	public void deactivatePartnerCompanies(List<Integer> deactivateUserIds, Integer vendorCompanyId, Date updatedOn) {
		String queryString = "UPDATE xt_partnership SET status = 'deactivated' , deactivated_on= :deactivatedOn WHERE vendor_company_id = :vendorCompanyId AND partner_id IN (:partnerIds) ";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("vendorCompanyId", vendorCompanyId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs()
				.add(new QueryParameterListDTO("partnerIds", deactivateUserIds));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("deactivatedOn", updatedOn));
		hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public boolean isPartnerCompanyDeactivatedAndDisabledNonVanityLogIn(Integer partnerCompanyId) {
		if (partnerCompanyId != null) {
			String sqlString = "select case when count(*)>0  then true else false end as hasLoginAcess from xt_partnership where partner_company_id = :partnerCompanyId and status = 'deactivated'";
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createSQLQuery(sqlString).setParameter("partnerCompanyId", partnerCompanyId);
			return (boolean) query.uniqueResult();
		} else {
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> findDeactivatedPartnerDomainsByCompanyIdAndModuleName(Integer companyId,
			DomainModuleNameType domainModuleNameType) {
		String queryString = "select domain_name from xt_allowed_domain \n"
				+ "where company_id = :companyId and is_domain_deactivated is true and module_name = '"
				+ domainModuleNameType.name() + "'";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId));
		return (List<String>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getPartnerCompanyIdsByDomain(PartnershipDTO partnershipDTO) {
		String queryString = "SELECT p.partner_company_id FROM xt_partnership p "
				+ "WHERE p.partner_id IN (SELECT user_id FROM xt_user_profile " + "WHERE email_id ILIKE :domainName "
				+ "AND user_id IN (SELECT partner_id FROM xt_partnership WHERE vendor_company_id = :vendorCompanyId)) "
				+ "AND p.vendor_company_id = :vendorCompanyId ";
		Session session = sessionFactory.getCurrentSession();
		SQLQuery query = session.createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", partnershipDTO.getVendorCompanyId());
		query.setParameter("domainName", "%" + partnershipDTO.getDomainName() + "%");
		List<Integer> partnerCompanyIds = query.list();
		return partnerCompanyIds;
	}

	public boolean isNonVanityAccessEnabled(Integer companyId) {
		if (companyId != null) {
			String sql = "SELECT non_vanity_access_enabled FROM xt_module_access WHERE company_id = :companyId";
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createSQLQuery(sql).setParameter("companyId", companyId);
			Object result = query.uniqueResult();
			return result != null && (boolean) result;
		} else {
			return false;
		}
	}

	// XNFR-1006
	@Override
	public Map<String, Object> getdeactivePartnerCompanies(Pagination pagination) {
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), true);
		Session session = sessionFactory.getCurrentSession();
		Integer userId = pagination.getUserId();
		Integer companyId = userDao.getCompanyIdByUserId(userId);
		String searchQuery = "";
		if (StringUtils.hasText(pagination.getSearchKey())) {
			String searchKey = "'%" + pagination.getSearchKey() + "%'";
			searchQuery = " and (xup.email_id ilike " + searchKey + " OR " + "xcp.company_name ilike " + searchKey
					+ ") ";
		}
		String sortSql = "";
		if (pagination.getSortcolumn() != null) {
			if (pagination.getSortcolumn().equalsIgnoreCase("campaign")) {
				sortSql = " order by xcp.company_name " + pagination.getSortingOrder() + " NULLS LAST";
			} else if (pagination.getSortcolumn().equalsIgnoreCase("campaign")) {
				sortSql = " order by xcp.company_name  " + pagination.getSortingOrder() + " NULLS LAST";
			}
		}
		String sql = "select distinct xcp.company_id as \"companyId\", xup.email_id as \"emailId\", xcp.company_name as \"companyName\", xup.firstname as \"firstName\", xup.lastname as \"lastName\",  xup.job_title as \"jobTitle\",  xup.mobile_number as \"mobileNumber\", cast(p.status as text) as \"partnershipStatus\", cast(xcp.company_name_status as text) as \"companyStatus\",  \r\n"
				+ " CAST(CASE WHEN p.deactivated_on IS NOT NULL THEN p.deactivated_on END AS TEXT) as \"deactivatedOn\", p.id as \"partnershipId\", "
				+ " case when xup.status='UnApproved' then 'Pendingsignup' "
				+ " when  (xcp.company_name_status = 'inactive' OR xcp.company_id IS NULL) AND xup.status = 'APPROVE' then 'incomplete' end as \"loginStatus\" "
				+ " from xt_partnership p  left join xt_company_profile xcp on xcp.company_id= p.partner_company_id\r\n"
				+ "left join xt_user_profile xup on xup.user_id= p.partner_id\r\n" + "where p.vendor_company_id = "
				+ companyId + " and p.status='deactivated' " + getPartnerTeamMemberGroupFilterSQLForDeactivatedPartners(
						" p.partner_id", userId, teamMemberFilterDTO, "deactivated")
				+ " ";
		if (pagination.getSelectedPartnerCompanyIds() != null && !pagination.getSelectedPartnerCompanyIds().isEmpty()) {
			sql += " and xup.company_id in (:partnerCompanyIds) ";
		}
		sql = sql + searchQuery + sortSql;
		SQLQuery query = session.createSQLQuery(sql);
		if (pagination.getSelectedPartnerCompanyIds() != null && !pagination.getSelectedPartnerCompanyIds().isEmpty()) {
			query.setParameterList("partnerCompanyIds", pagination.getSelectedPartnerCompanyIds());
		}
		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		query.setMaxResults(pagination.getMaxResults());
		@SuppressWarnings("unchecked")
		List<PartnerDTO> list = query.setResultTransformer(Transformers.aliasToBean(PartnerDTO.class)).list();
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("totalRecords", totalRecords);
		resultMap.put("list", list);
		return resultMap;
	}

	private String getPartnerTeamMemberGroupFilterSQLForDeactivatedPartners(String alias, Integer userId,
			TeamMemberFilterDTO teamMemberFilterDTO, String status) {
		String partnerTeamMemberGroupFilterSQL = "";
		if (teamMemberFilterDTO.isApplyTeamMemberFilter() || teamMemberFilterDTO.isEmptyFilter()) {
			partnerTeamMemberGroupFilterSQL = " AND  " + alias + " IN (" + "SELECT DISTINCT p.partner_id "
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
	public Map<String, Object> findTeamMemberPartnerCompanies(Pagination pagination, Integer teamMemberGroupId) {

		String sortQuery = XamplifyUtils.isNotEmptyList(pagination.getFiltertedEmailTempalteIds())
				? " order by array_position(array" + pagination.getFiltertedEmailTempalteIds() + ", p.id) "
				: "";

		String finalQueryString = "select p.id as \"partnershipId\", "
				+ "p.partner_company_id as \"partnerCompanyId\", " + "p.vendor_company_id as \"vendorCompanyId\", "
				+ "coalesce(uul.contact_company,'') as \"companyNameAddedByVendor\", "
				+ "p.created_time as \"createdTime\", " + "coalesce(c.company_logo,'NA') as \"companyLogo\", "
				+ "coalesce(c.company_name,'NA') as \"companyName\", " + "c.company_id as \"companyId\", "
				+ "partner.email_id as \"partnerEmail\", "
				+ "case when LENGTH(TRIM(concat(TRIM(partner.firstname), ' ', TRIM(partner.lastname))))>0 "
				+ "then TRIM(concat(TRIM(partner.firstname), ' ', TRIM(partner.lastname))) else partner.email_id end as \"partnerName\" "
				+ "from xt_partnership p "
				+ "join (select distinct ptgm.partnership_id from xt_partner_team_group_mapping ptgm "
				+ "join xt_team_member_group_user_mapping tgum on ptgm.team_member_group_user_mapping_id = tgum.id "
				+ "join xt_team_member_group tg on tgum.team_member_group_id = tg.id where tg.id = :teamMemberGroupId) as filtered_partnerships "
				+ "on p.id = filtered_partnerships.partnership_id "
				+ "left join xt_company_profile c on c.company_id = p.partner_company_id "
				+ "join xt_user_profile partner on p.partner_id = partner.user_id "
				+ "join xt_user_userlist uul on p.partner_id = uul.user_id "
				+ "join xt_user_list ul on uul.user_list_id = ul.user_list_id and p.vendor_company_id = ul.company_id "
				+ "where ul.is_default_partnerlist";

		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();

		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("teamMemberGroupId", teamMemberGroupId));
		hibernateSQLQueryResultRequestDTO.setQueryString(finalQueryString);
		hibernateSQLQueryResultRequestDTO.setSortQueryString(sortQuery);
		hibernateSQLQueryResultRequestDTO.setClassInstance(PartnerCompanyDTO.class);
		List<String> searchColumns = new ArrayList<>();
		searchColumns.add("uul.contact_company");
		searchColumns.add("c.company_name");
		searchColumns.add("partner.firstname");
		searchColumns.add("partner.lastname");
		searchColumns.add("partner.email_id");

		hibernateSQLQueryResultRequestDTO.setSearchColumns(searchColumns);
		return hibernateSQLQueryResultUtilDao.returnPaginatedDTOList(hibernateSQLQueryResultRequestDTO, pagination,
				pagination.getSearchKey());
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<PartnerCompanyDTO> findTeamMemberPartnerCompanyByTeamMemberGroupIdAndTeamMemberId(Integer teamMemberId,
			Integer teamMemberGroupId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String sql = " SELECT " + " p.id AS \"partnershipId\", " + " p.partner_company_id AS \"partnerCompanyId\", "
				+ " p.vendor_company_id AS \"vendorCompanyId\", " + " COALESCE(uul.contact_company, "
				+ " '') AS \"companyNameAddedByVendor\", " + " p.created_time AS \"createdTime\", "
				+ " c.company_logo AS \"companyLogo\", " + " COALESCE(c.company_name, " + " '-') AS \"companyName\", "
				+ " c.company_id AS \"companyId\" " + " FROM " + " xt_partnership p " + " JOIN xt_company_profile c ON "
				+ " c.company_id = p.partner_company_id " + " JOIN xt_user_list ul ON "
				+ " p.vendor_company_id = ul.company_id " + " JOIN xt_user_userlist uul ON "
				+ " ul.user_list_id = uul.user_list_id AND p.partner_id = uul.user_id "
				+ " JOIN xt_user_profile partner ON " + " p.partner_id = partner.user_id " + " JOIN ( " + " SELECT "
				+ "    ptgm.partnership_id " + " FROM " + "    xt_partner_team_group_mapping AS ptgm " + " JOIN "
				+ "    xt_team_member_group_user_mapping AS tmgum  "
				+ "    ON ptgm.team_member_group_user_mapping_id = tmgum.id " + " GROUP BY "
				+ "    ptgm.partnership_id " + "HAVING "
				+ "    COUNT(DISTINCT tmgum.team_member_id) = 1 AND MIN(tmgum.team_member_id) = :teamMemberId "
				+ "    AND COUNT(tmgum.team_member_group_id) FILTER (WHERE tmgum.team_member_group_id = :teamMemberGroupId) > 0 "
				+ " ) AS filtered_partnerships ON " + " p.id = filtered_partnerships.partnership_id " + " WHERE "
				+ " ul.is_default_partnerlist ";
		hibernateSQLQueryResultRequestDTO.setQueryString(sql);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("teamMemberId", teamMemberId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("teamMemberGroupId", teamMemberGroupId));
		hibernateSQLQueryResultRequestDTO.setClassInstance(PartnerCompanyDTO.class);
		return (List<PartnerCompanyDTO>) hibernateSQLQueryResultUtilDao
				.returnDTOList(hibernateSQLQueryResultRequestDTO);
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
	public Map<String, Object> getAllPartnerCompaniesDetails(Pagination pagination) {
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), true);
		Session session = sessionFactory.getCurrentSession();
		Integer userId = pagination.getUserId();
		Integer companyId = userDao.getCompanyIdByUserId(userId);
		String status = "approved,deactivated";

		String searchQuery = "";
		if (StringUtils.hasText(pagination.getSearchKey())) {
			String searchKey = "'%" + pagination.getSearchKey() + "%'";
			searchQuery = " and (a.\"emailId\" ilike " + searchKey + " OR " + "a.\"companyName\" ilike " + searchKey
					+ " OR " + "a.\"firstName\" ilike " + searchKey + " OR " + "a.\"lastName\" ilike " + searchKey
					+ ") ";
		}

		String orderByQuery = "ORDER BY CASE " + "WHEN status = 'Active' THEN 1 " + "WHEN status = 'Dormant' THEN 2 "
				+ "WHEN status = 'IncompleteCompanyProfile' THEN 3 " + "WHEN status = 'Pending Signup' THEN 4 "
				+ "WHEN status = 'Deactivated' THEN 5 " + "END";

		String sortSql = "";
		if (pagination.getSortcolumn() != null) {
			if (pagination.getSortcolumn().equalsIgnoreCase("campaign")) {
				sortSql = " order by a.\"companyName\" " + pagination.getSortingOrder() + " NULLS LAST";
			} else if (pagination.getSortcolumn().equalsIgnoreCase("campaign")) {
				sortSql = " order by a.\"companyName\"  " + pagination.getSortingOrder() + " NULLS LAST";
			} else if (pagination.getSortcolumn().equalsIgnoreCase("createdTime")) {
				sortSql = " order by xp.created_time  " + pagination.getSortingOrder() + " NULLS LAST";
			} else if (pagination.getSortcolumn().equalsIgnoreCase("createdTime")) {
				sortSql = " order by xp.created_time " + pagination.getSortingOrder() + " NULLS LAST";
			}
		}

		String sql = "SELECT a.* FROM ( " + "WITH engaged_company AS ( " +

		// LEAD
				"SELECT DISTINCT xup.company_id AS partner_company_id " + "FROM xt_lead l "
				+ "LEFT JOIN xt_user_profile xup ON l.created_by = xup.user_id " + "WHERE l.created_for_company_id = "
				+ companyId + " " + "AND l.created_for_company_id != l.created_by_company_id "
				+ "AND xup.company_id IS NOT NULL "
				+ getPartnerTeamMemberGroupFilterSQL("xup.company_id", userId, teamMemberFilterDTO, status) + " " +

				"UNION " +

				// DEAL
				"SELECT DISTINCT xup.company_id " + "FROM xt_deal d "
				+ "LEFT JOIN xt_user_profile xup ON d.created_by = xup.user_id " + "WHERE d.created_for_company_id = "
				+ companyId + " " + "AND d.created_for_company_id != d.created_by_company_id "
				+ "AND xup.company_id IS NOT NULL "
				+ getPartnerTeamMemberGroupFilterSQL("xup.company_id", userId, teamMemberFilterDTO, status) + " " +

				"UNION " +

				// LEARNING TRACK
				"SELECT DISTINCT p.partner_company_id " + "FROM xt_learning_track_visibility xltv "
				+ "LEFT JOIN xt_learning_track xlt ON xltv.learning_track_id = xlt.id "
				+ "LEFT JOIN xt_partnership p ON p.vendor_company_id = xlt.company_id AND xltv.partnership_id = p.id "
				+ "LEFT JOIN xt_user_profile xup ON xup.company_id = p.partner_company_id "
				+ "WHERE p.vendor_company_id = " + companyId + " " + "AND p.partner_company_id IS NOT NULL "
				+ "AND xlt.type IN ('TRACK', 'PLAYBOOK') " + "AND xlt.is_published = TRUE " + "AND xltv.progress > 0 "
				+ getPartnerTeamMemberGroupFilterSQL("xup.company_id", userId, teamMemberFilterDTO, status) + " " +

				"UNION " +

				// DAM PARTNER MAPPING
				"SELECT DISTINCT xp.partner_company_id " + "FROM xt_dam_partner xdp "
				+ "LEFT JOIN xt_dam xd ON xd.id = xdp.dam_id "
				+ "LEFT JOIN xt_dam_partner_mapping xdpm ON xdpm.dam_partner_id = xdp.id "
				+ "LEFT JOIN xt_dam_analytics xda ON xda.dam_partner_mapping_fk_id = xdpm.id "
				+ "LEFT JOIN xt_partnership xp ON xp.id = xdp.partnership_id AND xd.company_id = xp.vendor_company_id "
				+ "LEFT JOIN xt_user_profile xup ON xup.company_id = xp.partner_company_id "
				+ "WHERE xp.vendor_company_id = " + companyId + " " + "AND xp.partner_company_id IS NOT NULL "
				+ "AND xda.action_type = 'VIEW' "
				+ getPartnerTeamMemberGroupFilterSQL("xup.company_id", userId, teamMemberFilterDTO, status) + " " +

				"UNION " +

				// DAM GROUP
				"SELECT DISTINCT p.partner_company_id " + "FROM xt_dam_partner dp "
				+ "JOIN xt_dam d ON dp.dam_id = d.id "
				+ "JOIN xt_partnership p ON p.id = dp.partnership_id AND d.company_id = p.vendor_company_id "
				+ "JOIN xt_company_profile c ON p.partner_company_id = c.company_id "
				+ "JOIN xt_dam_partner_group_mapping dpgm ON dp.id = dpgm.dam_partner_id "
				+ "LEFT JOIN xt_dam_analytics xda ON xda.dam_partner_fk_id = dp.id "
				+ "LEFT JOIN xt_user_profile xup ON xup.company_id = p.partner_company_id "
				+ "WHERE p.vendor_company_id = " + companyId + " " + "AND xda.action_type = 'VIEW' "
				+ getPartnerTeamMemberGroupFilterSQL("xup.company_id", userId, teamMemberFilterDTO, status) + " " +

				"UNION " +

				// CAMPAIGN
				"SELECT DISTINCT xup.company_id " + "FROM xt_campaign c "
				+ "LEFT JOIN xt_campaign p ON c.campaign_id = p.parent_campaign_id "
				+ "LEFT JOIN xt_user_profile up ON up.user_id = c.customer_id "
				+ "LEFT JOIN xt_user_profile xup ON xup.user_id = p.customer_id " + "WHERE up.company_id = " + companyId
				+ " " + "AND p.vendor_organization_id = " + companyId + " " + "AND p.is_launched = TRUE "
				+ "AND p.is_nurture_campaign = TRUE " + "AND p.campaign_id IS NOT NULL "
				+ "AND xup.company_id IS NOT NULL "
				+ getPartnerTeamMemberGroupFilterSQL("xup.company_id", userId, teamMemberFilterDTO, status) + " "
				+ "), " +

				"base AS ( " + "SELECT DISTINCT xcp.company_logo AS \"companyLogo\", "
				+ "xcp.company_name AS \"companyName\", " + "xup.email_id AS \"emailId\", "
				+ "xup.company_id AS \"companyId\", " + "xup.user_id AS \"partnerId\", "
				+ "xup.firstname AS \"firstName\", " + "xup.lastname AS \"lastName\", " + "xp.partner_company_id, "
				+ "xcp.company_name_status, " + "xp.status AS \"partnershipStatus\", "
				+ "xup.status AS \"userStatus\", " + "xup.company_id AS \"userCompanyId\" " + "FROM xt_partnership xp "
				+ "LEFT JOIN xt_user_list xul ON xul.company_id = xp.vendor_company_id "
				+ "LEFT JOIN xt_user_userlist xuul ON xul.user_list_id = xuul.user_list_id AND xp.partner_id = xuul.user_id "
				+ "LEFT JOIN xt_user_profile xup ON xuul.user_id = xup.user_id "
				+ "LEFT JOIN xt_company_profile xcp ON xcp.company_id = xup.company_id " + "WHERE xul.company_id = "
				+ companyId + " " + "AND xp.status IN ('approved', 'deactivated') "
				+ "AND xul.is_default_partnerlist = TRUE " + getPartnerTeamMemberGroupFilterSQLForDeactivatedPartners(
						"xup.user_id", userId, teamMemberFilterDTO, status)
				+ " " + ") " +

				"SELECT DISTINCT \"companyLogo\", \"companyName\", \"emailId\", \"partnerId\", \"firstName\", \"lastName\", \"userCompanyId\", "
				+ "CASE "
				+ "WHEN \"partnershipStatus\" = 'approved' AND company_name_status = 'active' AND a.partner_company_id IS NOT NULL THEN 'Active' "
				+ "WHEN \"partnershipStatus\" = 'approved' AND company_name_status = 'active' AND a.partner_company_id IS NULL THEN 'Dormant' "
				+ "WHEN \"userStatus\" = 'UnApproved' AND \"partnershipStatus\" = 'approved' THEN 'Pending Signup' "
				+ "WHEN \"partnershipStatus\" = 'deactivated' THEN 'Deactivated' "
				+ "WHEN (company_name_status = 'inactive' OR \"userCompanyId\" IS NULL) AND \"userStatus\" = 'APPROVE' AND \"partnershipStatus\" = 'approved' THEN 'IncompleteCompanyProfile' "
				+ "END AS \"status\" "
				+ "FROM engaged_company a RIGHT JOIN base b ON b.partner_company_id = a.partner_company_id "
				+ ") a where 1=1 ";
		if (pagination.getSelectedPartnerCompanyIds() != null && !pagination.getSelectedPartnerCompanyIds().isEmpty()) {
			sql += " and a.\"userCompanyId\" in (:partnerCompanyIds) ";
		}
		if (StringUtils.hasText(sortSql)) {
			sql += searchQuery + sortSql;
		} else {
			sql += searchQuery + orderByQuery;
		}
		SQLQuery query = session.createSQLQuery(sql);
		if (pagination.getSelectedPartnerCompanyIds() != null && !pagination.getSelectedPartnerCompanyIds().isEmpty()) {
			query.setParameterList("partnerCompanyIds", pagination.getSelectedPartnerCompanyIds());
		}
		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		query.setMaxResults(pagination.getMaxResults());
		@SuppressWarnings("unchecked")
		List<PartnerDTO> list = query.setResultTransformer(Transformers.aliasToBean(PartnerDTO.class)).list();
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("totalRecords", totalRecords);
		resultMap.put("list", list);
		return resultMap;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Integer> findPartnershipsByTeamMemberGroupIdAndTeamMemberId(Integer teamMemberId,
			Integer teamMemberGroupId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String sql = "SELECT DISTINCT ptgm.partnership_id " + " FROM xt_partner_team_group_mapping AS ptgm "
				+ " JOIN xt_team_member_group_user_mapping AS tmgum "
				+ " ON ptgm.team_member_group_user_mapping_id = tmgum.id "
				+ " WHERE tmgum.team_member_id = :teamMemberId "
				+ " AND tmgum.team_member_group_id = :teamMemberGroupId ";

		hibernateSQLQueryResultRequestDTO.setQueryString(sql);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("teamMemberId", teamMemberId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("teamMemberGroupId", teamMemberGroupId));
		return (List<Integer>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
	}

}
