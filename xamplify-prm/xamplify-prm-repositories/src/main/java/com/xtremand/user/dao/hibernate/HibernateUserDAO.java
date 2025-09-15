package com.xtremand.user.dao.hibernate;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.AliasToBeanResultTransformer;
import org.hibernate.transform.Transformers;
import org.hibernate.type.StandardBasicTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.xtremand.activity.dto.EmailActivityDTO;
import com.xtremand.campaign.bom.ModuleAccess;
import com.xtremand.campaign.bom.ModuleAccessDTO;
import com.xtremand.campaign.bom.UnsubscribedUser;
import com.xtremand.campaign.dto.ReceiverMergeTagsDTO;
import com.xtremand.category.dto.CategoryDTO;
import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.Criteria;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.common.bom.Pagination;
import com.xtremand.common.bom.PartnerTeamMemberViewType;
import com.xtremand.company.dto.ApprovalSettingsDTO;
import com.xtremand.dam.dto.SharedAssetDetailsViewDTO;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.exclude.bom.ExcludedDomain;
import com.xtremand.exclude.bom.ExcludedUser;
import com.xtremand.exclude.bom.ExcludedUserDTO;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.partner.bom.PartnerDataAccessException;
import com.xtremand.partnership.bom.PartnershipDTO;
import com.xtremand.social.formbeans.TeamMemberDTO;
import com.xtremand.user.bom.AllAccountsView;
import com.xtremand.user.bom.AllUsersView;
import com.xtremand.user.bom.ModulesDisplayType;
import com.xtremand.user.bom.PartnerCompanyAndModuleAccessView;
import com.xtremand.user.bom.Role;
import com.xtremand.user.bom.TeamMember;
import com.xtremand.user.bom.TeamMemberStatus;
import com.xtremand.user.bom.User;
import com.xtremand.user.bom.User.UserStatus;
import com.xtremand.user.bom.UserCustomer;
import com.xtremand.user.bom.UserCustomerId;
import com.xtremand.user.bom.UserSource;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.user.exception.UserDataAccessException;
import com.xtremand.util.GenerateRandomPassword;
import com.xtremand.util.PaginationUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.HibernateSQLQueryResultUtilDao;
import com.xtremand.util.dto.AdminAndTeamMemberDetailsDTO;
import com.xtremand.util.dto.CompanyDTO;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.PartnerOrContactInputDTO;
import com.xtremand.util.dto.QueryParameterDTO;
import com.xtremand.util.dto.QueryParameterListDTO;
import com.xtremand.util.dto.ShareContentRequestDTO;
import com.xtremand.util.dto.UserDetailsUtilDTO;
import com.xtremand.util.dto.ViewTypePatchRequestDTO;
import com.xtremand.util.dto.XamplifyConstants;
import com.xtremand.views.RegisteredAndRecentLoggedInUsersView;

@Repository("userDAO")
@Transactional
public class HibernateUserDAO implements UserDAO {

	private static final String COMPANY_PROFILE_NAME = "companyProfileName";

	private static final String USER_ID = "userId";

	private static final Logger logger = LoggerFactory.getLogger(HibernateUserDAO.class);

	@Autowired
	SessionFactory sessionFactory;

	@Autowired
	GenericDAO genericDao;

	@Autowired
	private PaginationUtil paginationUtil;

	@Value("${adminRolesByCompanyIdQuery}")
	private String adminRolesByCompanyIdQuery;

	@Value("${teamMembersAndAdminQuery}")
	private String teamMembersAndAdminQuery;

	@Value("${teamMembersAndAdminSerachQuery}")
	private String teamMembersAndAdminSerachQuery;

	@Value("${company.logo.url}")
	private String companyLogoDefaultUrl;

	@Value("${dev.host}")
	String devHost;

	@Value("${prod.host}")
	String productionHost;

	@Value("${images.folder}")
	String vod;

	@Value("${spring.profiles.active}")
	private String profiles;

	@Value("${domain}")
	private String domain;

	@Value("${adminUserIdsByCompanyIdQuery}")
	private String adminUserIdsByCompanyIdQuery;

	@Value("${sql.shared.asset.details.query}")
	String sharedAssetData;

	@Value("${sql.folder.asset.details.query}")
	String folderAssetDataForPartner;

	@Value("${sql.folder.details.query}")
	String folderAssetData;

	@Value("${sql.folder.assets.query}")
	String assetDataForFolders;

	@Value("${sql.folder.assets.partner.query}")
	String assetDataForFoldersForPartner;

	public static final String TOTAL_RECORDS = "totalRecords";

	@Autowired
	private HibernateSQLQueryResultUtilDao hibernateSQLQueryResultUtilDao;

	@Override
	public User findByPrimaryKey(Serializable pk, com.xtremand.common.bom.FindLevel[] levels) {
		logger.debug("UserDAO findByPrimaryKey " + pk);
		User user = (User) sessionFactory.getCurrentSession().get(User.class, pk);

		if (user != null) {
			loadAssociations(user, levels);
		}
		return user;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<User> find(List<Criteria> criterias, FindLevel[] levels) {
		logger.debug("UserDAO find ");

		Session session = sessionFactory.getCurrentSession();

		org.hibernate.Criteria criteria = session.createCriteria(User.class);
		List<Criterion> criterions = generateCriteria(criterias);

		for (Criterion criterion : criterions) {
			criteria.add(criterion);
		}
		List<User> users = criteria.list();

		// Initializing Associations for one Object. Since fetch is set as
		// SUBSELECT,
		// it will ensure same is initialized for all Objects.
		if (!users.isEmpty()) {
			loadAssociations(users.get(0), levels);
		}
		return users;
	}

	private void loadAssociations(User user, FindLevel[] levels) {
		for (FindLevel level : levels) {
			switch (level) {
			case ROLES:
				Hibernate.initialize(user.getRoles());
				break;
			case CUSTOMERS:
				Hibernate.initialize(user.getUserCustomers());
				logger.debug("user customers loaded : " + user.getUserCustomers().size());
				break;
			case USER_LISTS:
				Hibernate.initialize(user.getUserLists());
				break;
			case COMPANY_PROFILE:
				Hibernate.initialize(user.getCompanyProfile());
				break;
			default:
				break;
			}
		}
	}

	@Override
	public UserCustomer getUserCustomer(User user, User customer) {
		UserCustomer userCustomer = (UserCustomer) sessionFactory.getCurrentSession().get(UserCustomer.class,
				new UserCustomerId(user, customer));
		return userCustomer;
	}

	// ************This method is DEPRECATED
	@Override
	public void deletePartnerCompany(List<Integer> partnerIds, Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		// session.createSQLQuery("delete from xt_partner_company where partner_id=
		// "+partnerId+" and company_id="+companyId).executeUpdate();
		// **************This method is DEPRECATED
		String sql = "delete from xt_partner_company where partner_id in (:partnerIds) and company_id=" + companyId;
		Query query = session.createSQLQuery(sql);
		query.setParameterList("partnerIds", partnerIds);
		query.executeUpdate();
	}

	@Override
	public User getUser(Integer userId) {
		logger.debug("Get User For # " + userId);
		Session session = null;
		try {
			session = sessionFactory.getCurrentSession();
			return session.get(User.class, userId);
		} catch (UserDataAccessException e) {
			logger.error("Error In Getting User for" + userId, e);
			throw new UserDataAccessException("No User Found For Given User Id" + userId);

		}

	}

	@Override
	public User getUserByEmail(String email) {
		logger.debug("getUserByEmail called");
		Session session = sessionFactory.getCurrentSession();
		return (User) session.createCriteria(User.class).add(Restrictions.eq("emailId", email.toLowerCase().trim()))
				.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getSubAdminUserIds(Integer userId) {
		logger.debug("Getting SubAdmin User Ids For  " + userId);
		try {
			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(
					"select user_id from xt_user_subscription where is_org_admin=:orgAdmin and customer_id =:customerId");
			query.setInteger("customerId", userId);
			query.setBoolean("orgAdmin", true);
			return query.list();
		} catch (UserDataAccessException e) {
			logger.error("Unable to fetch data for getting SubAdmins For" + userId, e);
			throw new UserDataAccessException(e.getMessage());
		}
	}

	@Override
	public Integer getOrgAdminUserId(Integer userId) {

		logger.debug("Getting OrgAdmin Id for " + userId);
		try {
			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(
					"select customer_id from xt_user_subscription where is_org_admin=:orgAdmin and user_id =:userId");
			query.setInteger(USER_ID, userId);
			query.setBoolean("orgAdmin", true);
			return (Integer) query.uniqueResult();
		} catch (UserDataAccessException e) {
			logger.error("Unable to fetch data for getting Org Admin For" + userId, e);
			throw new UserDataAccessException(e.getMessage());
		}
	}

	@Override
	public Map<String, Object> find(List<Criteria> criterias, FindLevel[] levels, Pagination pagination) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void loadUnsubscribedUsers(List<Criteria> criterias) {

	}

	@Override
	public String getUserDefaultPage(Integer userId) {
		logger.debug("Getting User Default page for: " + userId);
		try {
			Session session = sessionFactory.getCurrentSession();
			SQLQuery query = session.createSQLQuery("select default_page from xt_user_profile where user_id =:userId");
			query.setInteger(USER_ID, userId);
			return (String) query.uniqueResult();
		} catch (UserDataAccessException e) {
			logger.error("An error occured while getting Default page for: " + userId, e);
			throw new UserDataAccessException(e.getMessage());
		}
	}

	@Override
	public boolean isGridView(Integer userId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			SQLQuery query = session.createSQLQuery("select is_grid_view from xt_user_profile where user_id =:userId");
			query.setInteger(USER_ID, userId);
			return (boolean) query.uniqueResult();
		} catch (UserDataAccessException e) {
			logger.error("An error occured while getting isGridView for: " + userId, e);
			throw new UserDataAccessException(e.getMessage());
		}
	}

	@Override
	public void saveUsers(List<User> users) {
		try {
			logger.debug("In saveUsers(" + users.toString() + ")");
			Session session = sessionFactory.getCurrentSession();
			for (int i = 0; i < users.size(); i++) {
				User user = users.get(i);
				session.save(user);
				if (user.getAlias() == null) {
					GenerateRandomPassword randomPassword = new GenerateRandomPassword();
					user.setAlias(randomPassword.getPassword());
				}
				TeamMember teamMember = user.getTeamMembers().get(0);
				teamMember.setTeamMember(user);
				session.saveOrUpdate(teamMember);
				if (i % 30 == 0) {
					session.flush();
					session.clear();
				}
			}
		} catch (HibernateException | UserDataAccessException e) {
			logger.error("Error In saveUsers(" + users.toString() + ")", e);
			throw new UserDataAccessException(e.getMessage());
		} catch (Exception e) {
			logger.error("Error In saveUsers(" + users.toString() + ")", e);
			throw new UserDataAccessException(e.getMessage());
		}
	}

	@Override
	public void deleteAllTeamMemberRoles(List<Integer> userIds) {
		try {
			if (userIds != null && !userIds.isEmpty()) {
				logger.debug("In deleteRolesByUserIds(" + userIds + ")");
				Session session = sessionFactory.getCurrentSession();
				Query query = session.createSQLQuery(
						"delete from xt_user_role where user_id in(:teamMemberIds) and role_id not in(1,3,12)");
				query.setParameterList("teamMemberIds", userIds).executeUpdate();
				logger.debug("************Deleted All Existing Roles**********************");
			}
		} catch (HibernateException | UserDataAccessException e) {
			logger.error("Error In deleteRolesByUserIds(" + userIds + ")", e);
			throw new UserDataAccessException(e.getMessage());
		} catch (Exception ex) {
			logger.error("Error In deleteRolesByUserIds(" + userIds + ")", ex);
			throw new UserDataAccessException(ex.getMessage());
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> listAllAdminEmailIds() {
		try {
			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(
					"select LOWER(TRIM(u.email_id)) from xt_user_profile u,xt_user_role r where r.role_id = "
							+ Role.PRM_ROLE.getRoleId() + " and u.user_id = r.user_id");
			return query.list();
		} catch (HibernateException | UserDataAccessException e) {
			logger.error("Error In listAllAdminEmailIds()", e);
			throw new UserDataAccessException(e.getMessage());
		} catch (Exception ex) {
			logger.error("Error In listAllAdminEmailIds()", ex);
			throw new UserDataAccessException(ex.getMessage());
		}

	}

	@Override
	public void updateTeamMembersCompanyProfile(Integer userId, Integer companyId) {
		try {
			logger.debug("In updateTeamMembersCompanyProfile(" + userId + ")");
			Session session = sessionFactory.getCurrentSession();
			Query query = session
					.createSQLQuery("update xt_user_profile set company_id=:companyId where user_id = :userId");
			query.setParameter("companyId", companyId);
			query.setParameter(USER_ID, userId);
			query.executeUpdate();
		} catch (HibernateException | UserDataAccessException e) {
			logger.error("Error In updateTeamMembersCompanyProfile(" + userId + ")", e);
			throw new UserDataAccessException(e.getMessage());
		} catch (Exception ex) {
			logger.error("Error In updateTeamMembersCompanyProfile(" + userId + ")", ex);
			throw new UserDataAccessException(ex.getMessage());
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> listAllCompanyNames() {
		try {
			logger.debug("In listAllCompanyNames()");
			return sessionFactory.getCurrentSession()
					.createSQLQuery("select LOWER(replace(company_name , ' ','')) from xt_company_profile").list();
		} catch (HibernateException | UserDataAccessException e) {
			logger.error("Error In listAllCompanyNames()", e);
			throw new UserDataAccessException(e.getMessage());
		} catch (Exception ex) {
			logger.error("Error In listAllCompanyNames()", ex);
			throw new UserDataAccessException(ex.getMessage());
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> listAllCompanyProfileNames() {
		try {
			logger.debug("In listAllCompanyProfileNames()");
			return sessionFactory.getCurrentSession()
					.createSQLQuery("select LOWER(replace(company_profile_name , ' ','')) from xt_company_profile")
					.list();
		} catch (HibernateException | UserDataAccessException e) {
			logger.error("Error In listAllCompanyProfileNames()", e);
			throw new UserDataAccessException(e.getMessage());
		} catch (Exception ex) {
			logger.error("Error In listAllCompanyProfileNames()", ex);
			throw new UserDataAccessException(ex.getMessage());
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> listAllUserIdsByLoggedInUserId(Integer userId) {
		try {
			logger.debug("In listAllUsersByCompanyId()" + userId);
			CompanyProfile companyProfile = findByPrimaryKey(userId, new FindLevel[] { FindLevel.COMPANY_PROFILE })
					.getCompanyProfile();
			if (companyProfile != null) {
				Integer companyId = companyProfile.getId();
				SQLQuery query = sessionFactory.getCurrentSession()
						.createSQLQuery("select user_id from xt_user_profile where company_id=:companyId");
				query.setInteger("companyId", companyId);
				return query.list();
			} else {
				return new ArrayList<>();
			}

		} catch (HibernateException | UserDataAccessException e) {
			logger.error("Error In listAllUsersByCompanyId()", e);
			throw new UserDataAccessException(e.getMessage());
		} catch (Exception ex) {
			logger.error("Error In listAllUsersByCompanyId()", ex);
			throw new UserDataAccessException(ex.getMessage());
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getCompanyUserIds(Integer userId) {
		try {
			logger.debug("In getCompanyUserIds(" + userId + ")");
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery(
					"select userId from User user where user.companyProfile.id=(select user.companyProfile.id from User user where user.userId=:userId)");
			query.setParameter(USER_ID, userId);
			List<Integer> userIds = new ArrayList<Integer>();
			List<Integer> data = query.list();
			if (data != null && !data.isEmpty()) {
				userIds.addAll(data);
			}
			return userIds;
		} catch (HibernateException | UserDataAccessException ex) {
			logger.error("Error In getCompanyUserIds(" + userId + ")", ex);
			throw new UserDataAccessException(ex.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getCompanyUsers(Integer companyId) {
		try {
			logger.debug("In getCompanyUserIds(" + companyId + ")");
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery("select userId from User user where user.companyProfile.id=:companyId");
			query.setParameter("companyId", companyId);
			return query.list();
		} catch (HibernateException | UserDataAccessException ex) {
			logger.error("Error In getCompanyUserIds(" + companyId + ")", ex);
			throw new UserDataAccessException(ex.getMessage());
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xtremand.user.dao.UserDAO#getFirstOrgAdminId(java.lang.Integer) Get
	 * the First Org Admin Id based on the created date.
	 */
	@Override
	public Integer getFirstAdminId(Integer userId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String sqlQuery = "SELECT u.user_id FROM xt_user_profile u "
					+ " INNER JOIN xt_user_role ur ON u.user_id = ur.user_id "
					+ " WHERE company_id = (SELECT company_id FROM xt_user_profile WHERE user_id =:userId) "
					+ " AND ur.role_id  = " + Role.PRM_ROLE.getRoleId() + " ORDER BY u.created_time ASC LIMIT 1";
			SQLQuery query = session.createSQLQuery(sqlQuery);
			query.setParameter(USER_ID, userId);
			// query.setParameter("roleId", Role.ORG_ADMIN_ROLE.getRoleId());
			return (Integer) query.uniqueResult();
		} catch (UserDataAccessException e) {
			logger.error("Unable to fetch data for getting Org Admin For" + userId, e);
			throw new UserDataAccessException(e.getMessage());
		}
	}

	@Override
	public Integer getOrgAdminsCountByUserId(Integer userId) {
		try {
			logger.debug("In getOrgAdminsCountByUserId(" + userId + ")");
			Session session = sessionFactory.getCurrentSession();
			Integer companyId = findByPrimaryKey(userId, new FindLevel[] { FindLevel.COMPANY_PROFILE })
					.getCompanyProfile().getId();
			SQLQuery query = session.createSQLQuery(
					"select  count(u.email_id) from xt_user_profile u,xt_company_profile c,xt_user_role r where c.company_id = u.company_id and c.company_id = :companyId and r.user_id = u.user_id and r.role_id  = :roleId");
			query.setParameter("companyId", companyId);
			query.setParameter("roleId", 2);
			return ((BigInteger) query.uniqueResult()).intValue();
		} catch (HibernateException | UserDataAccessException ex) {
			logger.error("Error In getOrgAdminsCountByUserId(" + userId + ")", ex);
			throw new UserDataAccessException(ex.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> listAllTeamMembers(Integer userId) {
		try {
			Integer companyId = findByPrimaryKey(userId, new FindLevel[] { FindLevel.COMPANY_PROFILE })
					.getCompanyProfile().getId();
			String status = "'APPROVE'";
			String sqlQuery = "(select u.email_id,u.firstname,u.lastname,u.user_id from xt_user_profile u,xt_team_member t where t.team_member_id = u.user_id and t.company_id = u.company_id and u.company_id =  "
					+ companyId + " and u.status=" + status
					+ ") UNION (select email_id,firstname,lastname,user_id from xt_user_profile where company_id = "
					+ companyId + " and status=" + status + ") order by email_id asc";
			Query query = sessionFactory.getCurrentSession().createSQLQuery(sqlQuery);
			return query.list();
		} catch (HibernateException | UserDataAccessException e) {
			logger.error("Error In listAllTeamMembers()", e);
			throw new UserDataAccessException(e.getMessage());
		} catch (Exception ex) {
			logger.error("Error In listAllTeamMembers()", ex);
			throw new UserDataAccessException(ex.getMessage());
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> listAllPartnerEmailIds() {
		try {
			logger.debug("In listAllPartnerEmailIds()");
			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(
					"select LOWER(TRIM(u.email_id)) from xt_user_profile u,xt_user_role r where r.role_id = 12 and u.user_id = r.user_id");
			return query.list();
		} catch (HibernateException | UserDataAccessException e) {
			logger.error("Error In listAllPartnerEmailIds()", e);
			throw new UserDataAccessException(e.getMessage());
		} catch (Exception ex) {
			logger.error("Error In listAllPartnerEmailIds()", ex);
			throw new UserDataAccessException(ex.getMessage());
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> findAllAdminsAndSuperVisors(Integer userId) {
		try {
			logger.debug("In findAllAdminsAndSuperVisors()");
			Integer companyId = findByPrimaryKey(userId, new FindLevel[] { FindLevel.COMPANY_PROFILE })
					.getCompanyProfile().getId();
			String status = "'APPROVE'";
			String rolesString = Role.ALL_ROLES.getRoleId() + "," + Role.PRM_ROLE.getRoleId();
			String queryString = "(select u.email_id,u.firstname,u.lastname,u.user_id from xt_user_profile u,xt_team_member t,xt_user_role r where t.team_member_id = u.user_id and t.company_id = u.company_id and u.company_id =  "
					+ companyId + " and u.status=" + status + " and u.user_id = r.user_id and r.role_id in ("
					+ rolesString
					+ ")) UNION (select u.email_id,u.firstname,u.lastname,u.user_id from xt_user_profile u,xt_user_role r where u.company_id = "
					+ companyId + " and u.status=" + status + " and u.user_id = r.user_id and r.role_id in ("
					+ rolesString + "))";
			return sessionFactory.getCurrentSession().createSQLQuery(queryString).list();
		} catch (HibernateException | UserDataAccessException e) {
			logger.error("Error In findAllAdminsAndSuperVisors()", e);
			throw new UserDataAccessException(e.getMessage());
		} catch (Exception ex) {
			logger.error("Error In findAllAdminsAndSuperVisors()", ex);
			throw new UserDataAccessException(ex.getMessage());
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> listPartnerAndHisTeamMembers(Integer userId) {
		try {
			logger.debug("In listPartnerAndHisTeamMembers(" + userId + ")");
			Integer companyId = findByPrimaryKey(userId, new FindLevel[] { FindLevel.COMPANY_PROFILE })
					.getCompanyProfile().getId();
			String status = "'APPROVE'";
			String rolesString = Role.COMPANY_PARTNER.getRoleId() + "," + Role.ALL_ROLES.getRoleId();
			return sessionFactory.getCurrentSession().createSQLQuery(
					"(select u.email_id,u.firstname,u.lastname,u.user_id from xt_user_profile u,xt_team_member t,xt_user_role r where t.team_member_id = u.user_id and t.company_id = u.company_id and u.company_id =  "
							+ companyId + " and u.status=" + status + " and u.user_id = r.user_id and r.role_id in ("
							+ rolesString
							+ ")) UNION (select u.email_id,u.firstname,u.lastname,u.user_id from xt_user_profile u,xt_user_role r where u.company_id = "
							+ companyId + " and u.status=" + status + " and u.user_id = r.user_id and r.role_id in ("
							+ rolesString + "))")
					.list();
		} catch (HibernateException | UserDataAccessException e) {
			logger.error("Error In listPartnerAndHisTeamMembers(" + userId + ")", e);
			throw new UserDataAccessException(e.getMessage());
		} catch (Exception ex) {
			logger.error("Error In listPartnerAndHisTeamMembers(" + userId + ")", ex);
			throw new UserDataAccessException(ex.getMessage());
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> listAllRolesEmailIds() {
		try {
			logger.debug("In listAllRolesEmailIds");
			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(
					"select LOWER(TRIM(u.email_id)) from xt_user_profile u,xt_user_role r where u.user_id = r.user_id and r.role_id in(2,4,5,6,7,8,9,10,11,12) group by u.user_id order by u.user_id desc");
			return query.list();
		} catch (HibernateException | UserDataAccessException e) {
			logger.error("Error In listAllRolesEmailIds()", e);
			throw new UserDataAccessException(e.getMessage());
		} catch (Exception ex) {
			logger.error("Error In listAllRolesEmailIds()", ex);
			throw new UserDataAccessException(ex.getMessage());
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> listAllUserIdsByCompanyId(Integer companyId) {
		try {
			logger.debug("In listAllUsersByCompanyId()" + companyId);
			SQLQuery query = sessionFactory.getCurrentSession()
					.createSQLQuery("select user_id from xt_user_profile where company_id=:companyId");
			query.setInteger("companyId", companyId);
			return query.list();
		} catch (HibernateException | UserDataAccessException | PartnerDataAccessException e) {
			logger.error("Error In listAllUsersByCompanyId()", e);
			throw new UserDataAccessException(e.getMessage());
		} catch (Exception ex) {
			logger.error("Error In listAllUsersByCompanyId()", ex);
			throw new UserDataAccessException(ex.getMessage());
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> listAllPartnerIdsByCompanyId(Integer companyId) {
		try {
			logger.debug("In listAllPartnerIdsByCompanyId()" + companyId);
			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(
					"select partner_id from xt_partnership where vendor_company_id=:companyId and status='approved'");
			query.setInteger("companyId", companyId);
			return query.list();
		} catch (HibernateException | UserDataAccessException | PartnerDataAccessException e) {
			logger.error("Error In listAllPartnerIdsByCompanyId()", e);
			throw new UserDataAccessException(e.getMessage());
		} catch (Exception ex) {
			logger.error("Error In listAllPartnerIdsByCompanyId()", ex);
			throw new UserDataAccessException(ex.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void deletePartnerData(List<Integer> partnerIds, Integer companyId) {
		try {
			String partnerIdsString = "partnerIds";
			List<Integer> updatedPartnerIds = XamplifyUtils.removeDuplicatesAndNulls(partnerIds);
			Session session = sessionFactory.getCurrentSession();
			SQLQuery query = session.createSQLQuery(
					"select email_template_id from xt_campaign_partner where user_id in(:partnerIds) and company_id=:companyId and  email_template_id is not null");
			query.setParameterList(partnerIdsString, updatedPartnerIds);
			query.setParameter("companyId", companyId);
			List<Integer> emailTemplateIds = query.list();

			Query partnerQuery = session.createSQLQuery(
					"delete from xt_campaign_partner where user_id in(:partnerIds) and company_id=:companyId");
			partnerQuery.setParameterList(partnerIdsString, updatedPartnerIds);
			partnerQuery.setParameter("companyId", companyId);
			partnerQuery.executeUpdate();
			if (!emailTemplateIds.isEmpty()) {
				/******** Get all redistributed campaigns for that vendor ****************/
				List<Integer> videoCampaignIds = getVideoCampaignIdsByVendorOrgId(companyId, partnerIds, session);
				/*******
				 * Remove the row from xt_campaign_videos for all re-distributed by campaign by
				 * campaign id
				 *******/
				deleteCampaignVideo(session, videoCampaignIds);
				/**********
				 * Delete scheduled/saved campaigns for this partners for that vendor
				 ***********/
				deleteSavedScheduledNurtureCampaigns(partnerIds, companyId, session);
				/**********
				 * Update vendor_organization_id as NULL(This code is for partner campaigns
				 * which are created from videos tab,which is actually wrong.So updating them
				 * manually)
				 *************/
				Query updateToNurtureCampaignQuery = session.createSQLQuery(
						"update  xt_campaign set email_template_id = NULL,is_nurture_campaign = true  where email_template_id in(:ids)");
				updateToNurtureCampaignQuery.setParameterList("ids", emailTemplateIds);
				updateToNurtureCampaignQuery.executeUpdate();
				/***********
				 * Update vendor_organization_id as NULL in xt_campaign table
				 ****************/
				Query updateCampaignQuery = session.createSQLQuery(
						"update  xt_campaign set vendor_organization_id = NULL,email_template_id = NULL  where vendor_organization_id =:vendorOrgId and customer_id in (:partnerIds)");
				updateCampaignQuery.setParameter("vendorOrgId", companyId);
				updateCampaignQuery.setParameterList("partnerIds", partnerIds);
				updateCampaignQuery.executeUpdate();

				/********** Now delete the shared email templates spam score ******************/
				Query emailTemplateSpamScoreDeleteQuery = session
						.createSQLQuery("delete from xt_email_spam_score where email_template_id in(:ids)");
				emailTemplateSpamScoreDeleteQuery.setParameterList("ids", emailTemplateIds);
				emailTemplateSpamScoreDeleteQuery.executeUpdate();
				/********** Now delete the shared email templates ******************/
				Query emailTemplatesDeleteQuery = session
						.createSQLQuery("delete from xt_email_templates where id in(:ids)");
				emailTemplatesDeleteQuery.setParameterList("ids", emailTemplateIds);
				emailTemplatesDeleteQuery.executeUpdate();
			}

		} catch (HibernateException | UserDataAccessException | PartnerDataAccessException e) {
			logger.error("Error In listAllPartnerIdsByCompanyId()", e);
			throw new UserDataAccessException(e.getMessage());
		} catch (Exception ex) {
			logger.error("Error In listAllPartnerIdsByCompanyId()", ex);
			throw new UserDataAccessException(ex.getMessage());
		}
	}

	/**
	 * @param partnerIds
	 * @param companyId
	 * @param session
	 */
	private void deleteSavedScheduledNurtureCampaigns(List<Integer> partnerIds, Integer companyId, Session session) {
	}

	/**
	 * @param session
	 * @param campaignIds
	 */
	private void deleteCampaignVideo(Session session, List<Integer> campaignIds) {
		if (!campaignIds.isEmpty()) {
			Query videoDeleteQuery = session
					.createSQLQuery("delete from xt_campaign_videos where campaign_id in(:campaignIds)");
			videoDeleteQuery.setParameterList("campaignIds", campaignIds);
			videoDeleteQuery.executeUpdate();
		}

	}

	/**
	 * @param companyId
	 * @param session
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<Integer> getVideoCampaignIdsByVendorOrgId(Integer companyId, List<Integer> partnerIds,
			Session session) {
		SQLQuery campaignsQuery = session.createSQLQuery(
				"select campaign_id from xt_campaign where vendor_organization_id=:vendorOrganizationId and customer_id in(:partnerIds) and campaign_type='VIDEO'");
		campaignsQuery.setParameter("vendorOrganizationId", companyId);
		campaignsQuery.setParameterList("partnerIds", partnerIds);
		return (List<Integer>) campaignsQuery.list();
	}

	/**
	 * DEPRECATED
	 * 
	 * @param partnerIds
	 */
	// Deprecated Method
	/*
	 * private void deleteAllData(List<Integer> partnerIds,Session session) { try {
	 * for(Integer partnerId:partnerIds){ if(findByPrimaryKey(partnerId, new
	 * FindLevel[]{FindLevel.PARTNER_COMPANY}).getPartnerCompanies().isEmpty()){
	 * Query deleteCampaignQuery = session.
	 * createSQLQuery("delete from xt_campaign where customer_id=:customerId");
	 * deleteCampaignQuery.setParameter("customerId",partnerId);
	 * deleteCampaignQuery.executeUpdate();
	 *//************ Delete all email templates ****************//*
																	 * Query emailTemplateDeleteQuery = session
																	 * .createSQLQuery("delete from xt_email_templates where user_id=:userId"
																	 * );
																	 * emailTemplateDeleteQuery.setParameter("userId",
																	 * partnerId);
																	 * emailTemplateDeleteQuery.executeUpdate(); } }
																	 * }catch (HibernateException |
																	 * UserDataAccessException |
																	 * PartnerDataAccessException e) {
																	 * logger.error("Error In deleteAllData()", e);
																	 * throw new
																	 * UserDataAccessException(e.getMessage()); } catch
																	 * (Exception ex) {
																	 * logger.error("Error In deleteAllData()", ex);
																	 * throw new
																	 * UserDataAccessException(ex.getMessage()); } }
																	 */

	@Override
	public Object[] getSignUpDetails(String alias, Integer userListId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select up.email_id, ul.firstname,ul.lastname from xt_user_profile up inner join xt_user_userlist ul ON up.alias= :alias and up.user_id = ul.user_id and ul.user_list_id = :userListId";
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("alias", alias);
		query.setParameter("userListId", userListId);
		return (Object[]) query.uniqueResult();
	}

	@Override
	public Integer getCompanyIdByUserId(Integer userId) {
		logger.debug("getCompanyIdByUserId with {}", userId);
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery("select company_id from xt_user_profile where user_id = :userId");
		query.setParameter(USER_ID, userId);
		return (Integer) query.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Boolean getUserOppertunityModule(Integer userId) {
		logger.debug("getUserOppertunityModule with {}", userId);
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(
				"select distinct up.company_id as companyId, cp.company_name as companyName,cp.company_logo as companyLogo, up.user_id,ma.enable_leads as id from xt_user_profile up , \r\n"
						+ "	xt_user_role ur, xt_company_profile cp, xt_partner_company pc, xt_module_access ma \r\n"
						+ " where up.company_id in   (select company_id from xt_partner_company pc where \r\n"
						+ " pc.partner_id = :userId) and cp.company_id=up.company_id  and up.user_id=ur.user_id and ur.role_id=13 and ma.company_id = cp.company_id and ma.enable_leads = true");
		query.setParameter(USER_ID, userId);
		try {
			List<Object> list = query.list();
			if (list != null && list.size() > 0)
				return true;
			else
				return false;
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;
	}

	@Override
	public ModuleAccess getAccessByCompanyId(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		return (ModuleAccess) session.createCriteria(ModuleAccess.class)
				.add(Restrictions.eq("companyProfile.id", companyId)).uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getCompanyIdsByEmailIds(List<String> emailIds) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select company_id,email_id from xt_user_profile where email_id in (:emailIds) and company_id IS NOT NULL";
		Query query = session.createSQLQuery(queryString).setParameterList("emailIds", emailIds);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> getAllUsersByCompanyId(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(User.class)
				.add(Restrictions.eq("companyProfile.id", companyId));
		List<User> users = criteria.list();
		for (User user : users) {
			Hibernate.initialize(user.getRoles());
		}
		return users;
	}

	@Override
	public boolean isUserExistsinList(Integer teamMemberId) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(
				"select count(uul.*) from xt_user_userlist uul,xt_user_list ul where ul.user_list_id = uul.user_list_id and uul.user_id=:userId and ul.is_partner_userlist = true");
		query.setParameter(USER_ID, teamMemberId);
		int count = query.uniqueResult() != null ? ((BigInteger) query.uniqueResult()).intValue() : 0;
		return count > 0;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Integer> getPartnerIds() {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select user_id from xt_user_role where role_id =  12 and role_id not in(5,6) order by user_id asc";
		Query query = session.createSQLQuery(queryString);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getRoleIdsByUserId(Integer userId) {
		if (XamplifyUtils.isValidInteger(userId)) {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createSQLQuery(
					"select distinct ur.role_id from xt_user_role ur,xt_role r where r.role_id = ur.role_id and ur.user_id = :userId");
			query.setParameter(USER_ID, userId);
			return query.list();
		} else {
			return Collections.emptyList();
		}

	}

	@Override
	public boolean isEmailDnsConfigured(String emailId) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(
				"select c.is_email_dns_configured from xt_user_profile u,xt_company_profile c where u.company_id = c.company_id and u.email_id =:emailId");
		query.setParameter("emailId", emailId);
		if (query.uniqueResult() != null) {
			return (boolean) query.uniqueResult();
		} else {
			return false;
		}
	}

	@Override
	public void updateAccess(ModuleAccess moduleAccess) {
		sessionFactory.getCurrentSession().update(moduleAccess);
	}

	@Override
	public Integer getUserIdByEmail(String emailId) {
		logger.debug("getUserIdByEmail(" + emailId + ")");
		try {
			Session session = sessionFactory.getCurrentSession();
			SQLQuery query = session
					.createSQLQuery("select min(user_id) from xt_user_profile where LOWER(email_id)=LOWER(:emailId)");
			query.setParameter("emailId", emailId);
			return (Integer) query.uniqueResult();
		} catch (UserDataAccessException e) {
			logger.error("getUserIdByEmail(" + emailId + ")", e);
			throw new UserDataAccessException(e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Boolean getSMSServiceModule(Integer userId) {
		logger.debug("getSMSServiceModule with {}", userId);
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(
				"select distinct up.company_id as companyId, cp.company_name as companyName,cp.company_logo as companyLogo, up.user_id,ma.enable_leads as id from xt_user_profile up , \r\n"
						+ " xt_user_role ur, xt_company_profile cp, xt_partner_company pc, xt_module_access ma \r\n"
						+ "	where up.company_id in   (select company_id from xt_partner_company pc where \r\n"
						+ "	pc.partner_id = :userId) and cp.company_id=up.company_id  and up.user_id=ur.user_id and ur.role_id=13 and ma.company_id = cp.company_id and ma.sms_service=true");
		query.setParameter(USER_ID, userId);
		try {
			List<Object> list = query.list();
			if (list != null && list.size() > 0)
				return true;
			else
				return false;
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;
	}

	@Override
	public User findByAlias(String alias) {
		return (User) sessionFactory.getCurrentSession().createCriteria(User.class)
				.setProjection(Projections.projectionList().add(Projections.property(USER_ID), USER_ID)
						.add(Projections.property("emailId"), "emailId"))
				.add(Restrictions.eq("alias", alias)).setResultTransformer(Transformers.aliasToBean(User.class))
				.uniqueResult();
	}

	@Override
	public String getCompanyLogoPath(Integer companyId) {
		try {
			if (companyId != null) {
				Session session = sessionFactory.getCurrentSession();
				org.hibernate.Criteria criteria = session.createCriteria(CompanyProfile.class);
				criteria.add(Restrictions.eq("id", companyId));
				criteria.setProjection(Projections.property("companyLogoPath"));
				return (String) criteria.setResultTransformer(org.hibernate.Criteria.DISTINCT_ROOT_ENTITY)
						.uniqueResult();
			} else {
				return "";
			}

		} catch (HibernateException | UserDataAccessException e) {
			throw new UserDataAccessException(e);
		} catch (Exception ex) {
			throw new UserDataAccessException(ex);
		}
	}

	@Override

	public void updateUser(User user) {

		Session session = sessionFactory.getCurrentSession();
		String sql = "update xt_user_profile set email_validation_ind=:emailValidationInd, is_email_valid=:isEmailValid, email_category=:emailCategory where  user_id=:userId";
		Query query = session.createSQLQuery(sql);
		query.setParameter("emailValidationInd", user.isEmailValidationInd());
		query.setParameter("isEmailValid", user.isEmailValid());
		query.setParameter("emailCategory", user.getEmailCategory());
		query.setParameter(USER_ID, user.getUserId());
		query.executeUpdate();

	}

	@Override
	public void updateUserEmailValidationInd(User user) {

		Session session = sessionFactory.getCurrentSession();
		String sql = "update xt_user_profile set email_validation_ind=:emailValidationInd, is_email_valid=:isEmailValid, email_category=:emailCategory where  user_id=:userId";
		Query query = session.createSQLQuery(sql);
		query.setParameter("emailValidationInd", user.isEmailValidationInd());
		query.setParameter("isEmailValid", user.isEmailValid());
		query.setParameter("emailCategory", user.getEmailCategory());
		query.setParameter(USER_ID, user.getUserId());
		query.executeUpdate();
	}

	@Override
	public String getAboutUsByUserId(Integer userId) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select b.about_us from xt_user_profile a,xt_company_profile b where a.company_id = b.company_id and a.user_id=:userId";
		Query query = session.createSQLQuery(queryString).setParameter(USER_ID, userId);
		String aboutUs = (String) query.uniqueResult();
		if (aboutUs != null) {
			return aboutUs;
		} else {
			return "";
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> listUnsubscribedCompanyIdsByUserId(Integer userId) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session
				.createSQLQuery("select customer_company_id from xt_unsubscribed_user where user_id=:userId");
		query.setParameter(USER_ID, userId);
		return query.list();
	}

	@Override
	public boolean isUserUnsubscribedForCompany(Integer userId, Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(
				"select count(*) from xt_unsubscribed_user where user_id=:userId and customer_company_id=:customerCompanyId");
		query.setParameter(USER_ID, userId);
		query.setParameter("customerCompanyId", companyId);
		int count = query.uniqueResult() != null ? ((BigInteger) query.uniqueResult()).intValue() : 0;
		return count > 0;
	}

	@Override
	public String getEmailIdByUserId(Integer userId) {
		if (userId != null && userId > 0) {
			Session session = sessionFactory.getCurrentSession();
			String queryString = "select email_id from xt_user_profile where user_id=:userId";
			Query query = session.createSQLQuery(queryString).setParameter(USER_ID, userId);
			return query.uniqueResult() != null ? (String) query.uniqueResult() : "";
		} else {
			return "";
		}

	}

	@Override
	public boolean isTeamMemberBelongsToLoggedInUserCompanyId(Integer companyId, Integer teamMemberId) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(
				"select count(*) from xt_user_profile where user_id=:teamMemberId and company_id=:companyId");
		query.setParameter("teamMemberId", teamMemberId);
		query.setParameter("companyId", companyId);
		int count = query.uniqueResult() != null ? ((BigInteger) query.uniqueResult()).intValue() : 0;
		return count > 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> listRolesByUserId(Integer userId) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(
				"select r.role from xt_user_role ur,xt_role r  where ur.user_id =:userId and r.role_id = ur.role_id ");
		query.setParameter(USER_ID, userId);
		return query.list();
	}

	@Override
	public boolean hasLoginAsTeamMemberAccess(Integer userId) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(
				"select login_as_team_member from xt_module_access where company_id= (select company_id from xt_user_profile where user_id=:userId)");
		query.setParameter(USER_ID, userId);
		return (boolean) query.uniqueResult();
	}

	@Override
	public boolean isTeamMemberBelongsToLoggedInUserIdCompany(Integer loggedInUserId, Integer teamMemberId) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(
				"select count(*) from xt_user_profile where user_id=:teamMemberId and company_id=(select company_id from xt_user_profile where user_id=:loggedInUserId)");
		query.setParameter("teamMemberId", teamMemberId);
		query.setParameter("loggedInUserId", loggedInUserId);
		int count = query.uniqueResult() != null ? ((BigInteger) query.uniqueResult()).intValue() : 0;
		return count > 0;
	}

	@Override
	public String getSuperiorSourceType(Integer teamMemberId) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select source from xt_user_profile where user_id = (select org_admin_id from xt_team_member where team_member_id=:teamMemberId)";
		Query query = session.createSQLQuery(queryString).setParameter("teamMemberId", teamMemberId);
		String sourceType = (String) query.uniqueResult();
		if (sourceType != null) {
			return sourceType;
		} else {
			return "-";
		}
	}

	@Override
	public CompanyProfile getCompanyProfileByCompanyName(String companyName) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(CompanyProfile.class);
		criteria.add(Restrictions.eq("companyName", companyName.trim()));
		return (CompanyProfile) criteria.uniqueResult();
	}

	@Override
	public ModulesDisplayType getModulesDisplayDefaultView(Integer userId) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(User.class);
		criteria.add(Restrictions.eq(USER_ID, userId));
		criteria.setProjection(
				Projections.projectionList().add(Projections.property("modulesDisplayType"), "modulesDisplayType"))
				.setResultTransformer(Transformers.aliasToBean(User.class));
		User user = (User) criteria.uniqueResult();
		if (user != null) {
			if (user.getModulesDisplayType() != null) {
				return user.getModulesDisplayType();
			} else {
				return ModulesDisplayType.LIST;
			}

		} else {
			return null;
		}
	}

	@Override
	public void updateDefaultDisplayView(Integer userId, String type) {
		Session session = sessionFactory.getCurrentSession();
		String viewType = "'" + type + "'";
		Query query = session.createSQLQuery(
				"update xt_user_profile set modules_display_type=" + viewType + " where user_id=" + userId);
		query.executeUpdate();

	}

	/* -- XNFR-415 -- */
	@Override
	public void updateDefaultDashboardForPartner(Integer companyId, String type) {
		Session session = sessionFactory.getCurrentSession();
		String dashboardType = "'" + type + "'";
		Query query = session.createSQLQuery("update xt_company_profile set default_dashboard_for_partner="
				+ dashboardType + " where company_id=" + companyId);
		query.executeUpdate();

	}

	/* -- XNFR-415 -- */
	@Override
	public Object getDefaultDashboardForPartner(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(
				"select default_dashboard_for_partner from xt_company_profile where company_id=" + companyId);
		return query.uniqueResult();
	}

	@Override
	public User getFirstNameAndEmailIdByUserId(Integer id) {
		if (id != null) {
			Session session = sessionFactory.getCurrentSession();
			org.hibernate.Criteria criteria = session.createCriteria(User.class);
			criteria.add(Restrictions.eq(USER_ID, id));
			criteria.setProjection(Projections.projectionList().add(Projections.property("emailId"), "emailId")
					.add(Projections.property("firstName"), "firstName"))
					.setResultTransformer(Transformers.aliasToBean(User.class));
			return (User) criteria.uniqueResult();
		} else {
			return null;
		}
	}

	public Integer getCompanyIdByProfileName(String vendorCompanyProfileName) {
		if (StringUtils.hasText(vendorCompanyProfileName)) {
			vendorCompanyProfileName = getPrmCompanyProfileName();
			Session session = sessionFactory.getCurrentSession();
			String queryString = "select company_id  from xt_company_profile where company_profile_name=:companyProfileName";
			Query query = session.createSQLQuery(queryString).setParameter(COMPANY_PROFILE_NAME,
					vendorCompanyProfileName);
			return query.uniqueResult() != null ? (Integer) query.uniqueResult() : 0;
		} else {
			return 0;
		}
	}

	public String getPrmCompanyProfileName() {
		Session session = sessionFactory.getCurrentSession();
		String sqlString = "select LOWER(TRIM(cp.company_profile_name)) from xt_company_profile cp,xt_user_profile up,xt_user_role ur\r\n"
				+ "where up.company_id = cp.company_id and up.user_id = ur.user_id and ur.role_id = "
				+ Role.PRM_ROLE.getRoleId();
		Query query = session.createSQLQuery(sqlString);
		String companyProfileName = (String) query.uniqueResult();
		if (StringUtils.hasText(companyProfileName)) {
			return companyProfileName;
		} else {
			return "xamplify-prm";
		}
	}

	@Override
	public CompanyProfile getCompanyNameByCompanyId(Integer companyId) {
		if (companyId != null) {
			Session session = sessionFactory.getCurrentSession();
			org.hibernate.Criteria criteria = session.createCriteria(CompanyProfile.class);
			criteria.add(Restrictions.eq("id", companyId));
			criteria.setProjection(Projections.projectionList().add(Projections.property("companyName"), "companyName"))
					.setResultTransformer(Transformers.aliasToBean(CompanyProfile.class));
			return (CompanyProfile) criteria.uniqueResult();
		} else {
			return null;
		}

	}

	@Override
	public User getFirstNameAndUserIdByEmailId(String emailId) {
		if (StringUtils.hasText(emailId)) {
			Session session = sessionFactory.getCurrentSession();
			org.hibernate.Criteria criteria = session.createCriteria(User.class);
			criteria.add(Restrictions.eq("emailId", emailId));
			criteria.setProjection(Projections.projectionList().add(Projections.property(USER_ID), USER_ID)
					.add(Projections.property("firstName"), "firstName"))
					.setResultTransformer(Transformers.aliasToBean(User.class));
			return (User) criteria.uniqueResult();
		} else {
			return null;
		}

	}

	@Override
	public String getStatusByUserId(Integer userId) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select status from xt_user_profile where user_id=" + userId;
		Query query = session.createSQLQuery(queryString);
		return (String) query.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean isPartnerTeamMember(Integer teamMemberId) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select ur.role_id from xt_team_member t,xt_user_role ur where t.team_member_id = "
				+ teamMemberId + " and ur.user_id = t.org_admin_id";
		Query query = session.createSQLQuery(queryString);
		List<Integer> list = query.list();
		return !list.isEmpty() && list.indexOf(Role.COMPANY_PARTNER.getRoleId()) > -1;
	}

	@Override
	public boolean isSecondAdmin(AllAccountsView account) {
		String sqlString = "select is_second_admin from xt_team_member where team_member_id=:teamMemberId";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(sqlString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("teamMemberId", account.getUserId()));
		return hibernateSQLQueryResultUtilDao.returnBoolean(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public String getCompanyProfileNameByUserId(Integer userId) {
		if (userId != null) {
			Session session = sessionFactory.getCurrentSession();
			String queryString = "select c.company_profile_name  from xt_company_profile c,xt_user_profile u where u.company_id = c.company_id  and u.user_id=:userId";
			SQLQuery query = session.createSQLQuery(queryString);
			query.setParameter(USER_ID, userId);
			String result = (String) query.uniqueResult();
			return result != null ? result : "";
		} else {
			return "";
		}

	}

	@Override
	public Set<User> setSubscribeOrUnsubscribeUsers(ShareContentRequestDTO shareContentRequestDTO, Integer companyId) {
		Set<User> users = new HashSet<>();
		Session session = sessionFactory.getCurrentSession();
		Set<PartnerOrContactInputDTO> partnerOrContactDtos = shareContentRequestDTO.getPartnersOrContactDtos();
		for (PartnerOrContactInputDTO partnerOrContactDto : partnerOrContactDtos) {
			Integer userId = getUserIdByEmail(partnerOrContactDto.getEmailId());
			users.add(findReceiverMergeTagsAndUnsubscribeStatus(userId, companyId,
					shareContentRequestDTO.getUserListId(), session));
		}
		return users;
	}

	public boolean isUnsubscribed(Session session, Integer companyId, Integer userId) {
		String queryString = "select case when count(*)>0 then true else false end as unsubscried from xt_unsubscribed_user where user_id=:userId and customer_company_id=:companyId";
		return (boolean) session.createSQLQuery(queryString).setParameter(USER_ID, userId)
				.setParameter("companyId", companyId).uniqueResult();
	}

	@Override
	public User getFirstNameLastNameAndEmailIdByUserId(Integer userId) {
		if (userId != null) {
			Session session = sessionFactory.getCurrentSession();
			org.hibernate.Criteria criteria = session.createCriteria(User.class);
			criteria.add(Restrictions.eq(USER_ID, userId));
			criteria.setProjection(Projections.projectionList().add(Projections.property("emailId"), "emailId")
					.add(Projections.property("firstName"), "firstName")
					.add(Projections.property("lastName"), "lastName"))
					.setResultTransformer(Transformers.aliasToBean(User.class));
			return (User) criteria.uniqueResult();
		} else {
			return null;
		}
	}

	@Override
	public User getFirstNameMiddleNameLastNameAndEmailIdByUserId(Integer userId) {
		if (userId != null) {
			Session session = sessionFactory.getCurrentSession();
			org.hibernate.Criteria criteria = session.createCriteria(User.class);
			criteria.add(Restrictions.eq(USER_ID, userId));
			criteria.setProjection(Projections.projectionList().add(Projections.property("emailId"), "emailId")
					.add(Projections.property("firstName"), "firstName")
					.add(Projections.property("middleName"), "middleName")
					.add(Projections.property("lastName"), "lastName"))
					.setResultTransformer(Transformers.aliasToBean(User.class));
			return (User) criteria.uniqueResult();
		} else {
			return null;
		}
	}

	@Override
	public void deleteByRoleId(Integer roleId, Integer userId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "delete from xt_user_role where role_id = " + roleId + " and user_id=" + userId;
		Query query = session.createSQLQuery(sql);
		query.executeUpdate();
	}

	@Override
	public void insertRole(Integer roleId, Integer userId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "insert into  xt_user_role(user_id,role_id) values (:userId,:roleId)";
		Query query = session.createSQLQuery(sql);
		query.setParameter(USER_ID, userId);
		query.setParameter("roleId", roleId);
		query.executeUpdate();
	}

	@Override
	public void updateSource(ModuleAccessDTO moduleAccessDTO) {
		Session session = sessionFactory.getCurrentSession();
		String source = "";
		source = "'" + UserSource.ADMIN + "'";
		String sql = "update xt_user_profile set source=" + source + " where user_id=:userId";
		Query query = session.createSQLQuery(sql);
		query.setParameter(USER_ID, moduleAccessDTO.getUserId());
		query.executeUpdate();
	}

	@Override
	public String getDisplayName(Integer userId) {
		User userDto = getFirstNameLastNameAndEmailIdByUserId(userId);
		if (userDto != null) {
			String displayName = XamplifyUtils.setDisplayName(userDto.getFirstName(), userDto.getLastName(), "");
			if (displayName != null && StringUtils.hasText(displayName.trim())) {
				return displayName;
			} else {
				return "";
			}
		} else {
			return "";
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<UserDTO> listAdminsByCompanyId(Integer companyId) {
		if (companyId != null && companyId > 0) {
			String uddatedQuery = adminRolesByCompanyIdQuery.replace("adminRoleIdsString",
					Role.getAllAdminRolesAndPartnerRoleInString());
			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(uddatedQuery);
			query.setParameter("companyId", companyId);
			List<UserDTO> users = query.setResultTransformer(Transformers.aliasToBean(UserDTO.class)).list();
			if (users != null && !users.isEmpty()) {
				return users;
			} else {
				return new ArrayList<>();
			}
		} else {
			return new ArrayList<>();
		}
	}

	@Override
	public UserDTO getEmailIdAndDisplayName(Integer userId) {
		User user = getFirstNameLastNameAndEmailIdByUserId(userId);
		if (user != null) {
			UserDTO userDTO = new UserDTO();
			String displayName = XamplifyUtils.setDisplayName(user.getFirstName(), user.getLastName(), "");
			String updatedDisplayName = displayName != null && StringUtils.hasText(displayName) ? displayName.trim()
					: "";
			userDTO.setEmailId(user.getEmailId());
			userDTO.setFullName(XamplifyUtils.escapeDollarSequece(updatedDisplayName));
			if (StringUtils.hasText(updatedDisplayName)) {
				userDTO.setFullNameOrEmailId(updatedDisplayName);
			} else {
				userDTO.setFullNameOrEmailId(user.getEmailId());
			}
			return userDTO;
		} else {
			return null;
		}
	}

	@Override
	public boolean hasCompany(Integer userId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select case when company_id is not null then true else false end as hasCompany from xt_user_profile where user_id = :userId";
		return (boolean) session.createSQLQuery(sql).setParameter(USER_ID, userId).uniqueResult();
	}

	@Override
	public boolean isExcludedUserExists(Integer customerCompanyId, Integer excludedUserId) {
		boolean isExists = false;
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(ExcludedUser.class);
		criteria.add(Restrictions.and(Restrictions.eq("companyId", customerCompanyId),
				Restrictions.eq(USER_ID, excludedUserId)));
		ExcludedUser excludedUser = (ExcludedUser) criteria.uniqueResult();
		if (excludedUser != null) {
			isExists = true;
		}
		return isExists;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> listExcludedUsers(Integer companyId, Pagination pagination) {
		Session session = sessionFactory.getCurrentSession();
		String searchSQl = "";
		if (pagination.getSearchKey() != null && pagination.getSearchKey().length() > 0) {
			searchSQl = " and up.email_id like '%" + pagination.getSearchKey().toLowerCase() + "%' ";
		}
		String sql = "select up.user_id as \"userId\", up.email_id as \"emailId\", eu.created_time as \"time\" "
				+ " from xt_excluded_user eu, xt_user_profile up where eu.company_id = :companyId and eu.user_id = up.user_id"
				+ searchSQl;

		Query query = session.createSQLQuery(sql);
		query.setParameter("companyId", companyId);
		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		query.setMaxResults(pagination.getMaxResults());
		query.setResultTransformer(Transformers.aliasToBean(ExcludedUserDTO.class));
		List<ExcludedUserDTO> data = query.list();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("totalRecords", totalRecords);
		resultMap.put("data", data);
		return resultMap;

	}

	@Override
	public void deleteExcludedUser(Integer excludedUserId, Integer customerCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		String hql = " delete ExcludedUser where  userId=:userId and companyId=:companyId";
		Query query = session.createQuery(hql);
		query.setParameter(USER_ID, excludedUserId);
		query.setParameter("companyId", customerCompanyId);
		query.executeUpdate();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> findAdminsAndTeamMembersByCompanyId(Pagination pagination) {
		try {
			HashMap<String, Object> map = new HashMap<>();
			String finalQueryString = "";
			String searchKey = pagination.getSearchKey();
			boolean hasSearchKey = StringUtils.hasText(searchKey);
			if (!pagination.getFiltertedEmailTempalteIds().isEmpty()) {
				if (hasSearchKey) {
					finalQueryString = teamMembersAndAdminQuery + " "
							+ teamMembersAndAdminSerachQuery.replace("searchKey", searchKey)
							+ "  group by u.user_id order by array_position(array"
							+ pagination.getFiltertedEmailTempalteIds() + ", u.user_id),u.email_id asc nulls first";
				} else {
					finalQueryString = teamMembersAndAdminQuery + "  group by u.user_id order by array_position(array"
							+ pagination.getFiltertedEmailTempalteIds() + ", u.user_id),u.email_id asc nulls first";
				}
			} else {
				if (hasSearchKey) {
					finalQueryString = teamMembersAndAdminQuery + " "
							+ teamMembersAndAdminSerachQuery.replace("searchKey", searchKey)
							+ " group by u.user_id order by u.user_id desc";
				} else {
					finalQueryString = teamMembersAndAdminQuery + "  group by u.user_id order by u.user_id desc";
				}
			}
			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(finalQueryString);
			query.setParameter("companyId", pagination.getCompanyId());
			ScrollableResults scrollableResults = query.scroll();
			scrollableResults.last();
			Integer totalRecords = scrollableResults.getRowNumber() + 1;
			List<AdminAndTeamMemberDetailsDTO> list = query
					.setResultTransformer(Transformers.aliasToBean(AdminAndTeamMemberDetailsDTO.class)).list();
			map.put("totalRecords", totalRecords);
			map.put("list", list);
			return map;
		} catch (HibernateException | UserDataAccessException e) {
			throw new UserDataAccessException(e);
		} catch (Exception ex) {
			throw new UserDataAccessException(ex);
		}
	}

	@Override
	public void updateSpfConfiguration(Integer companyId) {
		updateSpfConfiguration(companyId, true);
	}

	@Override
	public boolean isSpfConfigured(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select is_spf_configured from xt_company_profile where company_id=:companyId";
		return (boolean) session.createSQLQuery(sql).setParameter("companyId", companyId).uniqueResult();

	}

	@Override
	public boolean isDnsConfigured(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select is_email_dns_configured from xt_company_profile where company_id=:companyId";
		return (boolean) session.createSQLQuery(sql).setParameter("companyId", companyId).uniqueResult();

	}

	@Override
	public void updateDnsConfiguration(Integer companyId, boolean isDnsConfigured) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "update xt_company_profile set is_email_dns_configured = :dnsConfigured where company_id = :companyId";
		session.createSQLQuery(sql).setParameter("dnsConfigured", isDnsConfigured).setParameter("companyId", companyId)
				.executeUpdate();
	}

	@Override
	public boolean isExcludedDomainExists(Integer customerCompanyId, String domain) {
		boolean isExists = false;
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(ExcludedDomain.class);
		criteria.add(Restrictions.and(Restrictions.eq("companyId", customerCompanyId),
				Restrictions.eq("domainName", domain)));
		ExcludedDomain excludedDomain = (ExcludedDomain) criteria.uniqueResult();
		if (excludedDomain != null) {
			isExists = true;
		}
		return isExists;
	}

	@Override
	public void deleteExcludedDomain(String domain, Integer customerCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		String hql = " delete ExcludedDomain where  domainName=:domainName and companyId=:companyId";
		Query query = session.createQuery(hql);
		query.setParameter("domainName", domain);
		query.setParameter("companyId", customerCompanyId);
		query.executeUpdate();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> listExcludedDomains(Integer companyId, Pagination pagination) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(ExcludedDomain.class);
		criteria.add(Restrictions.eq("companyId", companyId));
		if (pagination.getSearchKey() != null && pagination.getSearchKey().length() > 0) {
			criteria.add(Restrictions.ilike("domainName", pagination.getSearchKey(), MatchMode.ANYWHERE));
		}
		criteria.setProjection(Projections.projectionList().add(Projections.property("domainName"), "domainName")
				.add(Projections.property("createdTime"), "time"));
		ScrollableResults scrollableResults = criteria.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		criteria.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		criteria.setMaxResults(pagination.getMaxResults());
		criteria.setResultTransformer(new AliasToBeanResultTransformer(ExcludedUserDTO.class));
		List<ExcludedUserDTO> data = criteria.list();
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("totalRecords", totalRecords);
		resultMap.put("data", data);
		return resultMap;
	}

	@Override
	public Map<String, Object> findPartnerCompaniesAndModulesAccess(Pagination pagination) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(PartnerCompanyAndModuleAccessView.class);
		List<com.xtremand.common.bom.Criteria> criterias = new ArrayList<>();
		List<Criterion> criterions = generateCriteria(criterias);
		List<String> columnNames = new ArrayList<>();
		columnNames.add("companyName");
		columnNames.add(COMPANY_PROFILE_NAME);
		return paginationUtil.addSearchAndPaginationAndSort(pagination, criteria, criterions, columnNames,
				"companyName", "asc");

	}

	@Override
	public void updatePartnerModules(ModuleAccessDTO moduleAccessDTO) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "update xt_module_access set login_as_team_member = :loginAsTeamMember, exclude_users_or_domains = :excludeUsersOrDomains,"
				+ " max_admins = :maxAdmins, non_vanity_access_enabled = :isNonVanityAceessEnabled where company_id = :companyId";
		session.createSQLQuery(sql).setParameter("companyId", moduleAccessDTO.getCompanyId())
				.setParameter("loginAsTeamMember", moduleAccessDTO.isLoginAsTeamMember())
				.setParameter("maxAdmins", moduleAccessDTO.getMaxAdmins())
				.setParameter("isNonVanityAceessEnabled", moduleAccessDTO.isNonVanityAccessEnabled())
				.setParameter("excludeUsersOrDomains", moduleAccessDTO.isExcludeUsersOrDomains()).executeUpdate();
	}

	@Override
	public Map<String, Object> findAllUsers(Pagination pagination) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(AllUsersView.class);
		List<com.xtremand.common.bom.Criteria> criterias = new ArrayList<>();
		List<Criterion> criterions = generateCriteria(criterias);
		List<String> columnNames = new ArrayList<>();
		columnNames.add("firstName");
		columnNames.add("lastName");
		columnNames.add("emailId");
		return paginationUtil.addSearchAndPaginationAndSort(pagination, criteria, criterions, columnNames, "emailId",
				"asc");

	}

	@Override
	public Map<String, Object> findRegisteredOrRecentLoggedInUsers(Pagination pagination) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(RegisteredAndRecentLoggedInUsersView.class);
		List<com.xtremand.common.bom.Criteria> criterias = new ArrayList<>();
		List<Criterion> criterions = generateCriteria(criterias);
		List<String> columnNames = new ArrayList<>();
		columnNames.add("firstName");
		columnNames.add("lastName");
		columnNames.add("emailId");
		columnNames.add("companyName");
		String defaultColumnName = "";
		if (pagination.isAddingMoreLists()) {
			defaultColumnName = "lastLoginOn";
		} else {
			defaultColumnName = "registeredOn";
		}
		return paginationUtil.addSearchAndPaginationAndSort(pagination, criteria, criterions, columnNames,
				defaultColumnName, "desc");

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findAllCompanyIds() {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery("select distinct company_id from xt_company_profile ");
		return query.list();
	}

	@Override
	public String findCompanyLogoPath(Integer companyId) {
		String companyLogoPath = findCompanyLogoPathPrefix(companyId);
		if (StringUtils.hasText(companyLogoPath)) {
			companyLogoPath = XamplifyUtils.escapeDollarSequece(companyLogoPath);
			if ("dev".equals(profiles) || "qa".equals(profiles)) {
				companyLogoPath = devHost + vod + companyLogoPath;
			} else if ("production".equals(profiles)) {
				companyLogoPath = productionHost + vod + companyLogoPath;
			}
		}
		return companyLogoPath;

	}

	@Override
	public User findReceiverMergeTagsAndUnsubscribeStatus(Integer userId, Integer companyId, Integer userListId,
			Session session) {
		ReceiverMergeTagsDTO campaignReceiverMergeTagsDTO = findReceiverMergeTagsInfoByUserListIdAndUserId(userListId,
				userId);
		User receiver = new User();
		receiver.setUserId(userId);
		receiver.setAlias(campaignReceiverMergeTagsDTO.getAlias());
		receiver.setEmailValid(campaignReceiverMergeTagsDTO.isValidEmail());
		receiver.setFirstName(campaignReceiverMergeTagsDTO.getFirstName());
		receiver.setLastName(campaignReceiverMergeTagsDTO.getLastName());
		receiver.setEmailId(campaignReceiverMergeTagsDTO.getEmailId());
		receiver.setCompanyName(campaignReceiverMergeTagsDTO.getCompanyName());
		receiver.setAddress(campaignReceiverMergeTagsDTO.getAddress());
		receiver.setZipCode(campaignReceiverMergeTagsDTO.getZip());
		receiver.setCity(campaignReceiverMergeTagsDTO.getCity());
		receiver.setState(campaignReceiverMergeTagsDTO.getState());
		receiver.setCountry(campaignReceiverMergeTagsDTO.getCountry());
		receiver.setMobileNumber(campaignReceiverMergeTagsDTO.getMobileNumber());
		receiver.setContactCompany(campaignReceiverMergeTagsDTO.getCompanyName());
		boolean unsubscribed = isUnsubscribed(session, companyId, userId);
		receiver.setUnsubscribed(unsubscribed);
		User user = findByPrimaryKey(userId, new FindLevel[] { FindLevel.COMPANY_PROFILE, FindLevel.ROLES });
		CompanyProfile companyProfile = user.getCompanyProfile();
		if (companyProfile != null) {
			receiver.setCompanyProfile(companyProfile);
		}
		return receiver;
	}

	@Override
	public ReceiverMergeTagsDTO findReceiverMergeTagsInfoByUserListIdAndUserId(Integer userListId, Integer userId) {

		String queryString = " select u.alias as \"alias\", u.is_email_valid as \"validEmail\", u.email_id as \"emailId\", coalesce(xuul.firstname,'') as \"firstName\",coalesce(xuul.lastname,'') as \"lastName\", concat (coalesce(xuul.firstname,''), ' ', coalesce(xuul.lastname,'')) as \"fullName\", "
				+ " xuul.contact_company as \"companyName\",xuul.mobile_number as \"mobileNumber\",xuul.address as \"address\",xuul.zip as \"zip\","
				+ " xuul.city as \"city\",xuul.state as \"state\",xuul.country as \"country\" from xt_user_userlist xuul,xt_user_profile u "
				+ " where xuul.user_id = u.user_id and u.user_id = :userId and xuul.user_list_id = :userListId ";
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter(USER_ID, userId);
		query.setParameter("userListId", userListId);
		return (ReceiverMergeTagsDTO) query.setResultTransformer(Transformers.aliasToBean(ReceiverMergeTagsDTO.class))
				.uniqueResult();

	}

	@Override
	public CompanyProfile findPageMergeTagsInfoByCompanyId(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(CompanyProfile.class);
		criteria.add(Restrictions.eq("id", companyId));
		criteria.setProjection(Projections.projectionList().add(Projections.property("companyName"), "companyName")
				.add(Projections.property("aboutUs"), "aboutUs")
				.add(Projections.property("privacyPolicy"), "privacyPolicy").add(Projections.property("phone"), "phone")
				.add(Projections.property("city"), "city").add(Projections.property("state"), "state")
				.add(Projections.property("country"), "country").add(Projections.property("zip"), "zip")
				.add(Projections.property("facebookLink"), "facebookLink")
				.add(Projections.property("googlePlusLink"), "googlePlusLink")
				.add(Projections.property("linkedInLink"), "linkedInLink")
				.add(Projections.property("twitterLink"), "twitterLink")
				.add(Projections.property("instagramLink"), "instagramLink")
				.add(Projections.property("website"), "website").add(Projections.property("eventUrl"), "eventUrl"))
				.setResultTransformer(Transformers.aliasToBean(CompanyProfile.class));
		return (CompanyProfile) criteria.uniqueResult();

	}

	@Override
	public String getCompanyProfileNameById(Integer companyId) {
		if (companyId != null) {
			Session session = sessionFactory.getCurrentSession();
			String queryString = "select c.company_profile_name  from xt_company_profile c where company_id=:companyId";
			SQLQuery query = session.createSQLQuery(queryString);
			query.setParameter("companyId", companyId);
			String result = (String) query.uniqueResult();
			return result != null ? result : "";
		} else {
			return "";
		}

	}

	@Override
	public boolean findNotifyPartnersOption(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select notify_partners from xt_company_profile where company_id=:companyId";
		return (boolean) session.createSQLQuery(sql).setParameter("companyId", companyId).uniqueResult();

	}

	@Override
	public void updateNotifyPartnersOption(Integer companyId, boolean status) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "update xt_company_profile set notify_partners = :status where company_id = :companyId";
		session.createSQLQuery(sql).setParameter("companyId", companyId).setParameter("status", status).executeUpdate();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getUserIdsByEmailIds(Set<String> emailIds) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select user_id from xt_user_profile where email_id in (:emailIds)";
		Query query = session.createSQLQuery(queryString).setParameterList("emailIds", emailIds);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> findEmailIdsByCompanyId(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select distinct email_id from xt_user_profile where company_id = :companyId";
		Query query = session.createSQLQuery(queryString).setParameter("companyId", companyId);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findAdminAndApprovedOrDeclinedOrSupspendedTeamMemberIdsByCompanyId(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = " select distinct  u.user_id from xt_user_profile u where  u.email_id is not null "
				+ " and u.status in ('APPROVE','DECLINE','SUSPEND') and u.company_id is not null  and u.company_id = :companyId  group by u.user_id order by u.user_id desc";
		Query query = session.createSQLQuery(queryString).setParameter("companyId", companyId);
		return query.list();
	}

	@Override
	public String getCompanyNameByUserId(Integer userId) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select TRIM(cp.company_name) from xt_company_profile cp,xt_user_profile up where up.user_id = :userId and up.company_id = cp.company_id";
		Query query = session.createSQLQuery(queryString).setParameter(USER_ID, userId);
		return (String) query.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getComapnyAdminUserIds(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select distinct up.user_id from xt_user_profile up, xt_user_role r where r.role_id in (2,13,19,18,20) and up.company_id = "
				+ companyId + " and up.user_id=r.user_id ";
		Query query = session.createSQLQuery(queryString);
		return query.list();
	}

	@Override
	public Integer getCompanyIdByEmailId(String emailId) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select company_id from xt_user_profile where email_id =:emailId";
		Query query = session.createSQLQuery(queryString);
		query.setParameter("emailId", emailId.toLowerCase().trim());
		return (Integer) query.uniqueResult();

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> findAllEmailIds() {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select distinct LOWER(TRIM(email_id)) as e from xt_user_profile order by e asc";
		Query query = session.createSQLQuery(queryString);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> getUsersByEmailIds(List<String> emailIds) {
		Session session = sessionFactory.getCurrentSession();
		List<User> users = new ArrayList<User>();
		int listSize = emailIds.size(), chunkSize = 20000;
		logger.debug("Converting into chunks...");
		List<List<String>> chunkedEmailIdsArrayList = IntStream.range(0, (listSize - 1) / chunkSize + 1)
				.mapToObj(i -> emailIds.subList(i *= chunkSize, listSize - chunkSize >= i ? i + chunkSize : listSize))
				.collect(Collectors.toList());
		logger.debug("Chunk Size:-" + chunkedEmailIdsArrayList.size());
		for (List<String> chunkedEmailIds : chunkedEmailIdsArrayList) {
			logger.debug("Writing query to fetch data...");
			org.hibernate.Criteria criteria = session.createCriteria(User.class);
			criteria.add(Restrictions.in("emailId", chunkedEmailIds));
			List<User> chunckedList = (List<User>) criteria.list();
			if (chunckedList != null) {
				users.addAll(chunckedList);
			}
		}
		return users;
	}

	@Override
	public String findCompanyLogoPathPrefix(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select company_logo from xt_company_profile where company_id = :companyId";
		String companyLogoPath = (String) session.createSQLQuery(sql).setParameter("companyId", companyId)
				.uniqueResult();
		return companyLogoPath;
	}

	@Override
	public CompanyProfile getCompanyProfileNameAndCompanyNameByCompanyId(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(CompanyProfile.class);
		criteria.add(Restrictions.eq("id", companyId));
		criteria.setProjection(Projections.projectionList().add(Projections.property("companyName"), "companyName")
				.add(Projections.property(COMPANY_PROFILE_NAME), COMPANY_PROFILE_NAME))
				.setResultTransformer(Transformers.aliasToBean(CompanyProfile.class));
		return (CompanyProfile) criteria.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findCompanyIdsByUserIds(Set<Integer> userIds) {
		if (userIds != null && !userIds.isEmpty()) {
			Session session = sessionFactory.getCurrentSession();
			String sqlString = "select distinct company_id from xt_user_profile where user_id in (:userIds) and company_id is not null";
			Query query = session.createSQLQuery(sqlString);
			query.setParameterList("userIds", userIds);
			return query.list();
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public String getCompanyLogoPathByUserId(Integer userId) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select c.company_logo from xt_company_profile c, xt_user_profile u where u.company_id = c.company_id and u.user_id = :userId";
		Query query = session.createSQLQuery(queryString).setParameter(USER_ID, userId);
		return (String) query.uniqueResult();
	}

	@Override
	public UserDTO getDisplayNameByEmailId(String emailId) {
		Integer userId = getUserIdByEmail(emailId);
		User user = getFirstNameLastNameAndEmailIdByUserId(userId);
		UserDTO userDTO = new UserDTO();
		if (user != null) {
			String displayName = XamplifyUtils.setDisplayName(user.getFirstName(), user.getLastName(), "");
			String updatedDisplayName = displayName != null && StringUtils.hasText(displayName) ? displayName.trim()
					: "";
			userDTO.setEmailId(user.getEmailId());
			userDTO.setFullName(XamplifyUtils.escapeDollarSequece(updatedDisplayName));
			if (StringUtils.hasText(updatedDisplayName)) {
				userDTO.setFullNameOrEmailId(updatedDisplayName);
			} else {
				userDTO.setFullNameOrEmailId(user.getEmailId());
			}
		} else {
			userDTO.setFullNameOrEmailId("xAmplify");
		}
		return userDTO;
	}

	@Override
	public User findByEmailId(String emailId) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(User.class);
		criteria.add(Restrictions.eq("emailId", emailId));
		return (User) criteria.uniqueResult();
	}

	@Override
	public void declineAllUsersByCompanyId(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		Query declineUsersQuery = session
				.createQuery("Update User set userStatus=:status where companyProfile.id = :companyId");
		declineUsersQuery.setParameter("companyId", companyId);
		declineUsersQuery.setParameter("status", UserStatus.DECLINE);
		declineUsersQuery.executeUpdate();

		Query declineTeamMembersQuery = session
				.createQuery("Update TeamMember set teamMemberStatus=:status where companyId = :companyId");
		declineTeamMembersQuery.setParameter("companyId", companyId);
		declineTeamMembersQuery.setParameter("status", TeamMemberStatus.DECLINE);
		declineTeamMembersQuery.executeUpdate();
	}

	@Override
	public void approveOrUnApproveAllUsersByCompanyId(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		approveUsers(companyId, session);

		unApproveUsers(companyId, session);

		unApproveTeamMembers(companyId, session);

		approveTeamMembers(companyId, session);

	}

	@SuppressWarnings("unchecked")
	private void approveTeamMembers(Integer companyId, Session session) {
		String apporvedTeamMembersQueryString = "select user_id from xt_user_profile where company_id = :companyId and "
				+ " password is not null";
		SQLQuery approvedTeamMemberIdsQuery = session.createSQLQuery(apporvedTeamMembersQueryString);
		approvedTeamMemberIdsQuery.setParameter("companyId", companyId);
		List<Integer> approvedTeamMemberIds = approvedTeamMemberIdsQuery.list();
		if (approvedTeamMemberIds != null && !approvedTeamMemberIds.isEmpty()) {
			String unApprovedStatusInString = "'" + TeamMemberStatus.APPROVE.name() + "'";
			SQLQuery unApproveTeamMembersQuery = session.createSQLQuery("update xt_team_member set status = "
					+ unApprovedStatusInString + " where team_member_id in (:approvedTeamMemberIds)  ");
			unApproveTeamMembersQuery.setParameterList("approvedTeamMemberIds", approvedTeamMemberIds);
			unApproveTeamMembersQuery.executeUpdate();
		}
	}

	private void unApproveUsers(Integer companyId, Session session) {
		Query unApproveUsersQuery = session.createQuery(
				"Update User set userStatus=:status where companyProfile.id = :companyId and (length(TRIM(password)) = 0 OR password is null)");
		unApproveUsersQuery.setParameter("companyId", companyId);
		unApproveUsersQuery.setParameter("status", UserStatus.UNAPPROVED);
		unApproveUsersQuery.executeUpdate();
	}

	private void approveUsers(Integer companyId, Session session) {
		Query approveUsersQuery = session.createQuery(
				"Update User set userStatus=:status where companyProfile.id = :companyId and password is not null");
		approveUsersQuery.setParameter("companyId", companyId);
		approveUsersQuery.setParameter("status", UserStatus.APPROVED);
		approveUsersQuery.executeUpdate();
	}

	@SuppressWarnings("unchecked")
	private void unApproveTeamMembers(Integer companyId, Session session) {
		String unApprovedTeamMemberIdsQueryString = "select user_id from xt_user_profile where company_id = :companyId and "
				+ "(length(TRIM(password)) = 0 OR password is null)";
		SQLQuery unApprovedTeamMemberIdsQuery = session.createSQLQuery(unApprovedTeamMemberIdsQueryString);
		unApprovedTeamMemberIdsQuery.setParameter("companyId", companyId);
		List<Integer> unApprovedTeamMemberIds = unApprovedTeamMemberIdsQuery.list();
		if (unApprovedTeamMemberIds != null && !unApprovedTeamMemberIds.isEmpty()) {
			String unApprovedStatusInString = "'" + TeamMemberStatus.UNAPPROVED.name() + "'";
			SQLQuery unApproveTeamMembersQuery = session.createSQLQuery("update xt_team_member set status = "
					+ unApprovedStatusInString + " where team_member_id in (:unApprovedTeamMemberIds)  ");
			unApproveTeamMembersQuery.setParameterList("unApprovedTeamMemberIds", unApprovedTeamMemberIds);
			unApproveTeamMembersQuery.executeUpdate();
		}
	}

	@Override
	public void updateSpfConfiguration(Integer companyId, boolean isSpfConfigured) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "update xt_company_profile set is_spf_configured = :spfConfigured where company_id = :companyId";
		session.createSQLQuery(sql).setParameter("companyId", companyId).setParameter("spfConfigured", isSpfConfigured)
				.executeUpdate();
	}

	@Override
	public UnsubscribedUser getUnsubscribedUser(Integer userId, Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createQuery(
				"from UnsubscribedUser u  where u.userId=:userId and u.customerCompanyId=:customerCompanyId");
		query.setParameter(USER_ID, userId);
		query.setParameter("customerCompanyId", companyId);
		UnsubscribedUser unsubscribedUser = (UnsubscribedUser) query.uniqueResult();
		return unsubscribedUser;
	}

	@Override
	public CompanyDTO isSpfConfiguredOrDomainConnected(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select is_spf_configured as \"spfConfigured\", is_domain_connected as \"domainConnected\" from xt_company_profile where company_id = :companyId";
		Query query = session.createSQLQuery(sql);
		query.setParameter("companyId", companyId);
		return (CompanyDTO) paginationUtil.getDto(CompanyDTO.class, query);
	}

	@SuppressWarnings("unchecked")
	@Override

	public List<UserDTO> findAllApprovedUsersByUserId(Integer userId) {
		List<UserDTO> userDtos = new ArrayList<>();
		Session session = sessionFactory.getCurrentSession();
		String sqlString = "select user_id as \"id\",firstname as \"firstName\",lastname as \"lastName\",email_id as \"emailId\" from xt_user_profile\r\n"
				+ " where status = 'APPROVE' and company_id = (\r\n"
				+ "	select company_id from xt_user_profile where user_id = :userId)";
		SQLQuery query = session.createSQLQuery(sqlString);
		query.setParameter(USER_ID, userId);
		userDtos = (List<UserDTO>) paginationUtil.getListDTO(UserDTO.class, query);
		return userDtos;

	}

	@SuppressWarnings("unchecked")
	public List<Integer> findUnsubscribedUserIdsByCompanyId(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(
				"select  distinct user_id from xt_unsubscribed_user where customer_company_id = :companyId");
		query.setParameter("companyId", companyId);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findExcludedUserIdsByCompanyId(Integer companyId) {
		String sqlString = "select distinct user_id from xt_excluded_user where company_id = :companyId";
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(sqlString);
		query.setParameter("companyId", companyId);
		return query.list();

	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<User> findCampaignUsersByCampaignId(Integer campaignId) {
		List<User> campaignUsers = new ArrayList<>();
		String sqlString = "select distinct u.user_id as \"userId\",u.alias as \"alias\",u.is_email_valid as \"emailValid\",u.email_id as \"emailId\" from xt_user_profile u,\r\n"
				+ " xt_campaign_user_userlist cuu where cuu.user_id = u.user_id and cuu.campaign_id = :campaignId and u.is_email_valid";
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(sqlString);
		query.setParameter("campaignId", campaignId);
		campaignUsers = (List<User>) paginationUtil.getListDTO(User.class, query);
		if (campaignUsers != null && !campaignUsers.isEmpty()) {
			return new HashSet<>(campaignUsers);
		} else {
			return Collections.emptySet();
		}
	}

	@Override
	public boolean isUnsubscribedUserByCompanyIdAndUserId(Integer companyId, Integer userId) {
		Session session = sessionFactory.getCurrentSession();
		return isUnsubscribed(session, companyId, userId);
	}

	/****** XNFR-426 ******/
	@Override
	public void updateLeadApprovalOrRejectionStatus(Integer companyId, Boolean leadApprovalStatus) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery("update xt_company_profile set lead_approval_status=" + leadApprovalStatus
				+ " where company_id=" + companyId);

		/*** XNFR-483 ***/
		if (Boolean.FALSE.equals(leadApprovalStatus)) {
			Query queryForLeadApproval = session
					.createSQLQuery("update xt_lead\r\n" + "set approval_status = 'APPROVED',\r\n"
							+ "	approval_status_updated_time = CURRENT_TIMESTAMP\r\n"
							+ "where created_for_company_id = " + companyId + " AND approval_status is null");
			queryForLeadApproval.executeUpdate();
		}

		query.executeUpdate();
	}

	@Override
	public Object getLeadApprovalStatus(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session
				.createSQLQuery("select lead_approval_status from xt_company_profile where company_id=" + companyId);
		return query.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CompanyDTO> findAllCompanyNames() {
		String queryString = "select distinct company_id as \"id\", TRIM(company_name) as \"name\", TRIM(concat (company_name,' [',company_profile_name,']')) as \"itemName\" from list_all_accounts_view order by 2 asc ";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.setClassInstance(CompanyDTO.class);
		return (List<CompanyDTO>) hibernateSQLQueryResultUtilDao.returnDTOList(hibernateSQLQueryResultRequestDTO);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CompanyDTO> findAllPartnerCompanyNamesByVendorCompanyId(Integer vendorCompanyId) {
		String queryString = "select distinct laav.company_id  as \"id\", TRIM(laav.company_name) as \"name\", TRIM(concat (company_name,' [',company_profile_name,']'))  as \"itemName\" from list_all_accounts_view laav,xt_partnership xp \r\n"
				+ "where xp.partner_company_id  = laav.company_id \r\n"
				+ "and xp.vendor_company_id  = :vendorCompanyId and xp.partner_company_id  is not null\r\n"
				+ "order by 2  asc";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("vendorCompanyId", vendorCompanyId));
		hibernateSQLQueryResultRequestDTO.setClassInstance(CompanyDTO.class);
		return (List<CompanyDTO>) hibernateSQLQueryResultUtilDao.returnDTOList(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public boolean isPasswordExists(String emailId) {
		String queryString = "select case when length(TRIM(password))>0 then true else false end  from xt_user_profile\r\n"
				+ "where LOWER(email_id) = LOWER(:emailId)";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("emailId", emailId.toLowerCase()));
		return hibernateSQLQueryResultUtilDao.returnBoolean(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public String getCompanyWebSiteUrlByCompanyId(Integer companyId) {
		String queryString = "select website from xt_company_profile where company_id = :companyId";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId));
		return (String) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);

	}

	@Override
	public CompanyProfile getCompanyProfile(String companyName, Integer addedAdminCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(CompanyProfile.class);
		criteria.add(Restrictions.eq("companyName", companyName.trim()));
		criteria.add(Restrictions.eq("addedAdminCompanyId", addedAdminCompanyId));
		return (CompanyProfile) criteria.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> findUserIdAndEmailIdAndFullNameByUserIds(List<Integer> userIds) {
		List<User> allUsers = new ArrayList<>();
		if (XamplifyUtils.isNotEmptyList(userIds)) {
			Session session = sessionFactory.getCurrentSession();
			List<List<Integer>> chunkedUserIdsList = XamplifyUtils.getChunkedList(userIds);
			for (List<Integer> chunkedUserIds : chunkedUserIdsList) {
				String sqlString = "select distinct u.user_id as \"userId\", TRIM(LOWER(u.email_id)) as \"emailId\",\r\n"
						+ "concat(u.firstname, ' ', u.lastname) as \"fullName\" from xt_user_profile u where u.user_id in (:userIds) order by \"emailId\" asc";
				Query query = session.createSQLQuery(sqlString).setParameterList("userIds", chunkedUserIds);
				List<User> users = (List<User>) paginationUtil.getListDTO(User.class, query);
				allUsers.addAll(users);
			}
		}
		return allUsers;
	}

	@Override
	public void updateAdminOrPartnerOrTeamMemberViewType(Integer userId, Integer partnershipId, Integer teamMemberId,
			String type, boolean isViewUpdated) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String queryString = null;
		String viewType = "'" + type + "'";
		if (userId != null && teamMemberId == null && partnershipId == null) {
			queryString = "UPDATE xt_partner_team_member_view_type SET vendor_view_type=" + viewType
					+ ", updated_time = :updatedTime \n"
					+ "WHERE admin_id = :userId and is_view_updated = :isViewUpdated";
		} else if (userId != null && partnershipId != null && teamMemberId == null) {
			queryString = "UPDATE xt_partner_team_member_view_type SET vendor_view_type=" + viewType
					+ ", updated_time = :updatedTime, "
					+ "is_view_updated = :isViewUpdated \n WHERE admin_id = :userId and partnership_id = :partnershipId and team_member_id IS NULL";
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("partnershipId", partnershipId));
		} else if (userId != null && teamMemberId != null && partnershipId == null) {
			queryString = "UPDATE xt_partner_team_member_view_type SET vendor_view_type=" + viewType
					+ ", updated_time = :updatedTime, "
					+ "is_view_updated = :isViewUpdated \n WHERE admin_id = :userId and team_member_id = :teamMemberId";
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("teamMemberId", teamMemberId));
		}
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("updatedTime", new Date()));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(USER_ID, userId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("isViewUpdated", isViewUpdated));
		hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public String getAdminOrPartnerOrTeamMemberViewType(Integer userId, Integer partnershipId, Integer teamMemberId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String queryString = null;
		if (userId != null && teamMemberId == null && partnershipId == null) {
			queryString = "select vendor_view_type from xt_partner_team_member_view_type where admin_id = :userId"
					+ " and partnership_id IS NULL and team_member_id IS NULL";
		} else if (userId != null && partnershipId != null && teamMemberId == null) {
			queryString = "select vendor_view_type from xt_partner_team_member_view_type where admin_id = :userId"
					+ " and partnership_id = :partnershipId and team_member_id IS NULL";
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("partnershipId", partnershipId));
		} else if (userId != null && teamMemberId != null && partnershipId == null) {
			queryString = "select vendor_view_type from xt_partner_team_member_view_type where admin_id = :userId"
					+ " and team_member_id = :teamMemberId";
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("teamMemberId", teamMemberId));
		}
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(USER_ID, userId));
		return (String) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<UserDTO> findAllPrimaryAdminsAndViewTypes() {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery("select distinct u.user_id as \"id\", c.company_id as \"companyId\", "
				+ "u.modules_display_type as \"viewType\" \n from xt_company_profile c,xt_user_role ur,"
				+ "xt_user_profile u left join xt_team_member t on t.team_member_id = u.user_id \n"
				+ "  where c.company_id = u.company_id and u.user_id = ur.user_id and role_id in (2,13,18,20) \n"
				+ "  and c.company_name_status = 'active' group by c.company_id,u.user_id \n"
				+ "  having  CAST(count(t.id)AS integer) = 0 and c.company_id = u.company_id order by c.company_id;");
		query.setResultTransformer(Transformers.aliasToBean(UserDTO.class));
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PartnershipDTO> getAllPartnershipsByCompanyId(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(
				"select p.id as \"id\", p.partner_id as \"representingPartnerId\", u.modules_display_type as \"viewType\" \n "
						+ "from xt_partnership p, xt_user_profile u where p.partner_id = u.user_id and vendor_company_id = :companyId");
		query.setParameter("companyId", companyId);
		query.setResultTransformer(Transformers.aliasToBean(PartnershipDTO.class));
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TeamMemberDTO> getAllTeamMembersByUserId(Integer userId) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery("select t.id as \"id\", u.modules_display_type as \"viewType\" "
				+ "\n from xt_team_member t, xt_user_profile u where t.team_member_id = u.user_id and t.org_admin_id = :userId");
		query.setParameter(USER_ID, userId);
		query.setResultTransformer(Transformers.aliasToBean(TeamMemberDTO.class));
		return query.list();
	}

	@Override
	public void saveAll(List<PartnerTeamMemberViewType> partnerTeamMemberViewTypes) {
		try {
			Session session = sessionFactory.getCurrentSession();
			for (int i = 0; i < partnerTeamMemberViewTypes.size(); i++) {
				session.save(partnerTeamMemberViewTypes.get(i));
				if (i % 30 == 0) {
					session.flush();
					session.clear();
				}
			}
		} catch (HibernateException | UserDataAccessException e) {
			throw new UserDataAccessException(e.getMessage());
		} catch (Exception ex) {
			throw new UserDataAccessException(ex.getMessage());
		}
	}

	@Override
	public boolean isEmailAddressExists(String existingEmailAddress) {
		String queryString = "select case when count(*)>0 then true else false end  from xt_user_profile\r\n"
				+ "where LOWER(email_id) = LOWER(:emailId)";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("emailId", existingEmailAddress));
		return hibernateSQLQueryResultUtilDao.returnBoolean(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public boolean isCampaignAnalyticsSettingsOptionEnabled(Integer loggedInUserId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(
				"select is_campaign_analytics_settings_enabled from xt_user_profile where user_id = :userId");
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(USER_ID, loggedInUserId));
		return hibernateSQLQueryResultUtilDao.returnBoolean(hibernateSQLQueryResultRequestDTO);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ViewTypePatchRequestDTO> findUpdatedAdminsViewTypeData(Integer adminId, boolean partnership,
			boolean teamMember) {
		String queryString = "";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		if (adminId == null && !partnership && !teamMember) {
			queryString = "select admin_id as \"userId\", partnership_id as \"partnershipId\", "
					+ "team_member_id as \"teamMemberId\", vendor_view_type as \"viewType\", created_time as \"createdTime\", "
					+ "updated_time as \"updatedTime\", is_view_updated as \"viewUpdated\" \n "
					+ "from xt_partner_team_member_view_type_old \n where partnership_id is null and team_member_id is null "
					+ "and updated_time > '2024-07-02 00:00:00'";
		} else if (adminId != null && partnership && !teamMember) {
			queryString = "select admin_id as \"userId\", partnership_id as \"partnershipId\", "
					+ "team_member_id as \"teamMemberId\", vendor_view_type as \"viewType\", created_time as \"createdTime\", "
					+ "updated_time as \"updatedTime\", is_view_updated as \"viewUpdated\" \n "
					+ "from xt_partner_team_member_view_type_old \n where admin_id = :adminId and partnership_id is not null "
					+ "and team_member_id is null";
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("adminId", adminId));
		} else if (adminId != null && !partnership && teamMember) {
			queryString = "select admin_id as \"userId\", partnership_id as \"partnershipId\", "
					+ "team_member_id as \"teamMemberId\", vendor_view_type as \"viewType\", created_time as \"createdTime\", "
					+ "updated_time as \"updatedTime\", is_view_updated as \"viewUpdated\" \n "
					+ "from xt_partner_team_member_view_type_old \n where admin_id = :adminId and partnership_id is null "
					+ "and team_member_id is not null";
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("adminId", adminId));
		} else if (adminId != null && partnership && teamMember) {
			queryString = "select admin_id as \"userId\", partnership_id as \"partnershipId\", "
					+ "team_member_id as \"teamMemberId\", vendor_view_type as \"viewType\", created_time as \"createdTime\", "
					+ "updated_time as \"updatedTime\", is_view_updated as \"viewUpdated\" \n "
					+ "from xt_partner_team_member_view_type_old \n where admin_id = :adminId and partnership_id is not null "
					+ "and team_member_id is not null";
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("adminId", adminId));
		}
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.setClassInstance(ViewTypePatchRequestDTO.class);
		return (List<ViewTypePatchRequestDTO>) hibernateSQLQueryResultUtilDao
				.returnDTOList(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public String getSupportEmailIdByCompanyId(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session
				.createSQLQuery("select support_email_id from xt_company_profile where company_id=" + companyId);
		return (String) query.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<UserDTO> findAllUserNamesByCompanyId(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select firstname as \"firstName\", middle_name as \"middleName\", lastname as \"lastName\","
				+ " user_id as \"id\", LOWER(TRIM(email_id)) as \"emailId\" from xt_user_profile where company_id = :companyId";
		Query query = session.createSQLQuery(queryString).setParameter("companyId", companyId);
		query.setResultTransformer(Transformers.aliasToBean(UserDTO.class));
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> findAllPartnerAndPartnerTeamMemberEmailIdsByVendorCompanyId(Integer vendorCompanyId) {
		// check it with dummy records
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select distinct  xup.email_id from xt_partnership xp "
				+ " join  xt_user_profile xup on xup.company_id = xp.partner_company_id "
				+ " where  xp.vendor_company_id  = :vendorCompanyId and xup.email_validation_ind "
				+ " and xp.\"status\" ='approved'  and xp.partner_company_id  is not null ";
		Query query = session.createSQLQuery(queryString).setParameter("vendorCompanyId", vendorCompanyId);
		return query.list();

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CompanyDTO> findAllVendorCompanyIdAndNames() {
		String queryString = "select distinct company_id as \"id\", TRIM(company_name) as \"name\", TRIM(concat (company_name,' [',company_profile_name,']')) as \"itemName\" from list_all_accounts_view "
				+ "where role_name  role_name like '%prmRole%'  order by 2 asc ";
		queryString = queryString.replace("prmRole", Role.PRM_ROLE.getRoleName());
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.setClassInstance(CompanyDTO.class);
		return (List<CompanyDTO>) hibernateSQLQueryResultUtilDao.returnDTOList(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public User getUserNameByEmail(String emailId) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(User.class);
		criteria.add(Restrictions.eq("emailId", emailId.trim().toLowerCase()));
		Projection projection1 = Projections.property(USER_ID);
		Projection projection2 = Projections.property("firstName");
		Projection projection3 = Projections.property("lastName");
		Projection projection4 = Projections.property("middleName");

		ProjectionList projectionList = Projections.projectionList();
		projectionList.add(projection1);
		projectionList.add(projection2);
		projectionList.add(projection3);
		projectionList.add(projection4);
		criteria.setProjection(projectionList);
		return (User) criteria.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<UserDTO> findAdminAndSuperVisorsFirstNameLastNameEmailIdByCompanyId(Integer vendorCompanyId) {
		String status = "'APPROVE'";
		String queryString = "select u.email_id as \"emailId\",u.firstname as \"firstName\",u.lastname as \"lastName\",u.user_id as \"id\" \n"
				+ "from xt_user_profile u,xt_team_member t, xt_user_role r where t.team_member_id = u.user_id \n"
				+ "and t.company_id = u.company_id and u.company_id = :companyId and u.status=" + status
				+ " and u.user_id = r.user_id \n"
				+ "and r.role_id in (9,20) UNION select u.email_id as \"emailId\", u.firstname as \"firstName\", \n"
				+ "u.lastname as \"lastName\", u.user_id as \"id\" from xt_user_profile u,xt_user_role r where u.company_id = :companyId \n"
				+ "and u.status=" + status + " and u.user_id = r.user_id and r.role_id in (9,20)";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, vendorCompanyId));
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		return (List<UserDTO>) hibernateSQLQueryResultUtilDao.getListDto(hibernateSQLQueryResultRequestDTO,
				UserDTO.class);
	}

	/** XNFR-824 start **/
	@Override
	public boolean checkIfAssetApprovalRequiredByCompanyId(Integer companyId) {
		boolean result = false;
		if (XamplifyUtils.isValidInteger(companyId)) {
			try {
				String queryString = "select is_approval_required_for_assets from xt_company_profile where company_id = :companyId";
				HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
				hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
				hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
						.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId));
				result = (boolean) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
			} catch (Exception e) {
				result = false;
				logger.debug(
						"Exception occured in method checkIfAssetApprovalRequiredByCompanyId() for companyId: {} Timestamp: {}",
						companyId, new Date());
			}
		}
		return result;
	}

	@Override
	public boolean checkIfTracksApprovalRequiredByCompanyId(Integer companyId) {
		boolean result = false;
		if (XamplifyUtils.isValidInteger(companyId)) {
			try {
				String queryString = "select is_approval_required_for_tracks from xt_company_profile where company_id = :companyId";
				HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
				hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
				hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
						.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId));
				result = (boolean) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
			} catch (Exception e) {
				result = false;
				logger.debug(
						"Exception occured in method checkIfTracksApprovalRequiredByCompanyId() for companyId: {} Timestamp: {}",
						companyId, new Date());
			}
		}
		return result;
	}

	@Override
	public boolean checkIfPlaybooksApprovalRequiredByCompanyId(Integer companyId) {
		boolean result = false;
		if (XamplifyUtils.isValidInteger(companyId)) {
			try {
				String queryString = "select is_approval_required_for_playbooks from xt_company_profile where company_id = :companyId";
				HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
				hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
				hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
						.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId));
				result = (boolean) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
			} catch (Exception e) {
				result = false;
				logger.debug(
						"Exception occured in method checkIfPlaybooksApprovalRequiredByCompanyId() for companyId: {} Timestamp: {}",
						companyId, new Date());
			}
		}
		return result;
	}

	@Override
	public ApprovalSettingsDTO getApprovalConfigurationSettingsByCompanyId(Integer companyId) {
		ApprovalSettingsDTO result = new ApprovalSettingsDTO();
		if (XamplifyUtils.isValidInteger(companyId)) {
			try {
				String queryString = "select is_approval_required_for_assets as \"approvalRequiredForAssets\", "
						+ "is_approval_required_for_tracks as \"approvalRequiredForTracks\", "
						+ "is_approval_required_for_playbooks as \"approvalRequiredForPlaybooks\" from xt_company_profile where company_id = :companyId";
				HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
				hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
				hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
						.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId));
				result = (ApprovalSettingsDTO) hibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO,
						ApprovalSettingsDTO.class);
			} catch (Exception e) {
				result = new ApprovalSettingsDTO();
				logger.debug(
						"Exception occured in method getApprovalConfigurationSettingsByCompanyId() for companyId: {} Timestamp: {}",
						companyId, new Date());
			}
		}
		return result;
	}

	@Override
	public Integer updateApprovalConfigurationSettings(ApprovalSettingsDTO approvalSettingsDTO) {
		Integer updatedCount = null;
		if (XamplifyUtils.isValidInteger(approvalSettingsDTO.getCompanyId())) {
			String queryString = "update xt_company_profile set is_approval_required_for_assets = :assetApprovalEnabledForCompany,"
					+ "is_approval_required_for_tracks = :tracksApprovalEnabledForCompany, is_approval_required_for_playbooks = :playbooksApprovalEnabledForCompany"
					+ "  where company_id = :companyId";
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, approvalSettingsDTO.getCompanyId()));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(
					"assetApprovalEnabledForCompany", approvalSettingsDTO.isAssetApprovalEnabledForCompany()));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(
					"tracksApprovalEnabledForCompany", approvalSettingsDTO.isTracksApprovalEnabledForCompany()));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(
					"playbooksApprovalEnabledForCompany", approvalSettingsDTO.isPlaybooksApprovalEnabledForCompany()));
			updatedCount = hibernateSQLQueryResultUtilDao.updateAndReturnCount(hibernateSQLQueryResultRequestDTO);
		}
		return updatedCount;
	}

	/** XNFR-824 **/

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> listAdminsUserIdsByCompanyId(Integer companyId) {
		if (!XamplifyUtils.isValidInteger(companyId)) {
			return Collections.emptyList();
		}
		String queryString = adminUserIdsByCompanyIdQuery.replace("adminRoleIdsString",
				Role.getAllAdminRolesAndPartnerRoleInString());
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add((new QueryParameterDTO("companyId", companyId)));
		return (List<Integer>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public String getAliasByUserId(Integer userId) {
		String queryString = "select alias from xt_user_profile where user_id = :userId";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add((new QueryParameterDTO(USER_ID, userId)));
		return (String) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
	}

	/** XNFR-781 end **/

	@Override
	public void approvePendingTracksOrPlaybooksByModuleType(Integer companyId, Integer loggedInUserId,
			String moduleType) {
		if (XamplifyUtils.isValidInteger(companyId) && XamplifyUtils.isValidInteger(loggedInUserId)
				&& XamplifyUtils.isValidString(moduleType)) {
			String queryString = "UPDATE xt_learning_track SET approval_status = 'APPROVED', "
					+ "approval_status_updated_by = :userId, " + "approval_status_updated_time = :updatedTime "
					+ "WHERE company_id = :companyId AND approval_status = 'CREATED' and type = cast(:moduleType as learning_track_type)";
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO(XamplifyConstants.USER_ID, loggedInUserId));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO(XamplifyConstants.UPDATED_TIME, new Date()));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("moduleType", moduleType.toUpperCase()));
			hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);
		}
	}

	@Override
	public void approveInTimeLineStatusHistoryByCompanyIdAndModuleType(Integer companyId, String moduleType) {
		if (XamplifyUtils.isValidInteger(companyId) && XamplifyUtils.isValidString(moduleType)) {
			String sqlQueryString = "UPDATE xt_approval_status_history AS xash SET status = CAST('APPROVED' AS approval_status_type) "
					+ "FROM xt_user_profile AS xup WHERE xash.created_by = xup.user_id AND xup.company_id = :companyId "
					+ "AND xup.status = CAST('APPROVE' AS status) AND xash.status = CAST('CREATED' AS approval_status_type) AND "
					+ "xash.module_type = CAST(:moduleType AS approval_module_type)";
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO.setQueryString(sqlQueryString);
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("moduleType", moduleType.toUpperCase()));
			hibernateSQLQueryResultUtilDao.updateAndReturnCount(hibernateSQLQueryResultRequestDTO);
		}
	}

	/** XNFR-824 end **/

	public void updateTeammemberFilterOption(Integer userId, boolean filterOption) {
		if (XamplifyUtils.isValidInteger(userId)) {
			String queryString = "update  xt_team_member set is_partner_filter = :filterOption where team_member_id =:userId\n ";
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO(XamplifyConstants.USER_ID, userId));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("filterOption", filterOption));
			hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);
		}
	}

	/** XNFR-821 **/
	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findAllAdminAndSuperVisorUserIdsByCompanyId(Integer companyId) {
		List<Integer> rolesList = Arrays.asList(Role.ALL_ROLES.getRoleId(), Role.PRM_ROLE.getRoleId());

		String queryString = "(select u.user_id from xt_user_profile u,xt_team_member t,xt_user_role r where t.team_member_id = u.user_id and t.company_id = u.company_id and u.company_id = :companyId "
				+ " and u.status = cast('APPROVE' as status) and u.user_id = r.user_id and r.role_id in (:rolesList)) UNION (select u.user_id from xt_user_profile u,xt_user_role r where u.company_id = :companyId "
				+ "and u.status = cast('APPROVE' as status) and u.user_id = r.user_id and r.role_id in (:rolesList))";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add((new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId)));
		hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs()
				.add((new QueryParameterListDTO("rolesList", rolesList)));
		return (List<Integer>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public User getFirstNameLastNameAndJobTitleByUserId(Integer userId) {
		if (userId != null) {
			Session session = sessionFactory.getCurrentSession();
			org.hibernate.Criteria criteria = session.createCriteria(User.class);
			criteria.add(Restrictions.eq(USER_ID, userId));
			criteria.setProjection(Projections.projectionList().add(Projections.property("occupation"), "occupation")
					.add(Projections.property("firstName"), "firstName")
					.add(Projections.property("lastName"), "lastName"))
					.setResultTransformer(Transformers.aliasToBean(User.class));
			return (User) criteria.uniqueResult();
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getPartnerCompanyByEmailDomain(String domain, Integer vendorCompanyId) {
		String queryString = "SELECT c.company_name AS company_name FROM xt_partnership p JOIN xt_partner_company_domain pcd ON p.id = pcd.partnership_id"
				+ " JOIN xt_company_profile c ON p.partner_company_id = c.company_id WHERE p.vendor_company_id = :vendorCompanyId "
				+ " AND pcd.partner_company_domain = :domain " + "order by p.id LIMIT 1";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add((new QueryParameterDTO("vendorCompanyId", vendorCompanyId)));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add((new QueryParameterDTO("domain", domain)));
		return (List<String>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public boolean checkValidUserOrNotByUserId(Integer userId) {
		String queryString = "SELECT EXISTS (SELECT 1 FROM xt_user_profile WHERE user_id = :userId)";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add((new QueryParameterDTO(USER_ID, userId)));
		return hibernateSQLQueryResultUtilDao.returnBoolean(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public String getMobileNumberByUserIdAndUserListId(Integer userId, Integer userListId, boolean isCompanyJourney) {
		String queryString = "select REPLACE(mobile_number, ' ', '') from xt_user_userlist where user_list_id = :userListId and user_id = :userId";
		if (isCompanyJourney) {
			queryString = "select REPLACE(uul.mobile_number, ' ', '') from xt_user_userlist uul join xt_user_list ul on uul.user_list_id = ul.user_list_id "
					+ "where ul.associated_company_id = :userListId and uul.user_id = :userId";
		}
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add((new QueryParameterDTO(USER_ID, userId)));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add((new QueryParameterDTO("userListId", userListId)));
		return (String) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public SharedAssetDetailsViewDTO getSharedAssetDetailsByPartnerDamId(Integer damId, Integer loggedInUserId) {
		if (!XamplifyUtils.isValidInteger(damId) && !XamplifyUtils.isValidInteger(loggedInUserId)) {
			return null;
		}
		String queryString = sharedAssetData;
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("damId", damId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("loggedInUserId", loggedInUserId));
		return (SharedAssetDetailsViewDTO) hibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO,
				SharedAssetDetailsViewDTO.class);
	}

	@SuppressWarnings("unchecked")
	public List<UserDTO> getUsersByDamIdAndPartnerCompanyIds(Integer damId, List<Integer> companyIds,
			boolean isPartnerGroupSelected) {
		String queryString = "";
		if (isPartnerGroupSelected) {
			queryString = "SELECT distinct on(up.user_id) \r\n"
					+ "up.firstname as \"firstName\", up.middle_name as \"middleName\", up.lastname as \"lastName\",\r\n"
					+ "up.user_id as \"id\", LOWER(TRIM(up.email_id)) as \"emailId\", dp.id as \"damPartnerId\", p.partner_company_id as \"companyId\"\r\n"
					+ "FROM  xt_dam d\r\n" + "JOIN xt_dam_partner dp ON d.id = dp.dam_id\r\n"
					+ "JOIN xt_partnership p ON dp.partnership_id = p.id\r\n"
					+ "JOIN xt_dam_partner_group_mapping dpgm ON dp.id = dpgm.dam_partner_id\r\n"
					+ "JOIN xt_user_profile up ON dpgm.user_id = up.user_id\r\n"
					+ "WHERE d.id = :damId AND p.partner_company_id IN (:companyIds);\r\n";
		} else {
			queryString = "SELECT  distinct on(up.user_id) \r\n"
					+ "up.firstname as \"firstName\", up.middle_name as \"middleName\", up.lastname as \"lastName\",\r\n"
					+ "up.user_id as \"id\", LOWER(TRIM(up.email_id)) as \"emailId\", dp.id as \"damPartnerId\", p.partner_company_id as \"companyId\"\r\n"
					+ "FROM xt_dam d\r\n" + "JOIN xt_dam_partner dp ON d.id = dp.dam_id\r\n"
					+ "JOIN xt_partnership p ON dp.partnership_id = p.id\r\n"
					+ "JOIN xt_dam_partner_mapping dpm ON dp.id = dpm.dam_partner_id\r\n"
					+ "JOIN xt_user_profile up ON dpm.partner_id= up.user_id\r\n"
					+ "WHERE d.id = :damId AND p.partner_company_id IN (:companyIds);";
		}

		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("damId", damId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs()
				.add(new QueryParameterListDTO("companyIds", companyIds));
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		return (List<UserDTO>) hibernateSQLQueryResultUtilDao.getListDto(hibernateSQLQueryResultRequestDTO,
				UserDTO.class);
	}

	@Override
	public UserDTO getFullNameAndEmailIdAndCompanyNameByUserId(Integer userId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String queryString = "select concat(u.firstname, case when u.firstname is not null and u.middle_name is not null then concat(' ',u.middle_name) else u.middle_name end, \r\n"
				+ "case when u.firstname is not null or u.middle_name is not null then concat(' ',u.lastname) else u.lastname end) as \"fullName\", u.email_id as \"emailId\", cp.company_name as \"companyName\", cp.company_profile_name as \"companyProfileName\" , cp.company_logo as \"companyLogo\", cp.website as \"website\"  \r\n"
				+ "from xt_user_profile u join xt_company_profile cp on u.company_id = cp.company_id \r\n"
				+ "where u.user_id = :userId ";
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("userId", userId));

		return (UserDTO) hibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO, UserDTO.class);
	}

	@Override
	public UserDTO getSendorCompanyDetailsByUserId(Integer userId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String queryString = "SELECT u.firstname AS \"firstName\", u.middle_name AS \"middleName\", u.lastname AS \"lastName\", "
				+ "CONCAT(u.firstname, CASE WHEN u.firstname IS NOT NULL AND u.middle_name IS NOT NULL THEN CONCAT(' ', u.middle_name) ELSE u.middle_name END, "
				+ "CASE WHEN u.firstname IS NOT NULL OR u.middle_name IS NOT NULL THEN CONCAT(' ', u.lastname) ELSE u.lastname END) AS \"fullName\", "
				+ "u.email_id AS \"emailId\", cp.company_name AS \"companyName\", cp.company_profile_name AS \"companyProfileName\", cp.company_logo AS \"companyLogo\", "
				+ "cp.website AS \"website\", cp.instagram_link AS \"instagramUrl\", cp.twitter_link AS \"twitterUrl\", cp.facebook_link AS \"facebookLink\", "
				+ "cp.linked_in_link AS \"linkedInLink\", cp.google_plus_link AS \"googlePlusLink\", cp.event_url AS \"eventUrl\", cp.about_us AS \"aboutUs\", "
				+ "CONCAT(cp.street, ', ', cp.city, ', ', cp.state, ', ', cp.zip, ', ', cp.country) AS \"senderCompanyAddress\", cp.privacy_policy AS \"privacyPolicy\", "
				+ "u.job_title AS \"jobTitle\", u.mobile_number AS \"mobileNumber\",cp.phone as \"senderCompanyContactNumber\" FROM xt_user_profile u "
				+ "JOIN xt_company_profile cp ON u.company_id = cp.company_id WHERE u.user_id = :userId";
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("userId", userId));

		return (UserDTO) hibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO, UserDTO.class);
	}

	@SuppressWarnings("unchecked")
	public List<UserDetailsUtilDTO> fetchAdminsAndSupervisorsByPartnerIdAndVendorCompanyId(Integer companyId,
			Integer userId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String queryString = "(select u.email_id as \"emailId\", "
				+ "concat(u.firstname, case when u.firstname is not null and u.middle_name is not null then concat(' ',u.middle_name) else u.middle_name end, "
				+ "case when u.firstname is not null or u.middle_name is not null then concat(' ',u.lastname) else u.lastname end) as \"fullName\", "
				+ "c.company_profile_name as \"companyName\" "
				+ "from xt_user_profile u, xt_team_member t, xt_user_role r, xt_company_profile c "
				+ "where t.team_member_id = u.user_id and t.company_id = u.company_id "
				+ "and u.company_id = :companyId and u.status = cast('APPROVE' as status) "
				+ "and u.user_id = r.user_id and u.company_id = c.company_id "
				+ "and (r.role_id in (2, 9, 12, 13, 19, 18, 20) or u.user_id = :userId)) " + "UNION "
				+ "(select u.email_id, "
				+ "concat(u.firstname, case when u.firstname is not null and u.middle_name is not null then concat(' ',u.middle_name) else u.middle_name end, "
				+ "case when u.firstname is not null or u.middle_name is not null then concat(' ',u.lastname) else u.lastname end) as \"fullName\", "
				+ "c.company_profile_name as \"companyName\" "
				+ "from xt_user_profile u, xt_user_role r, xt_company_profile c "
				+ "where u.company_id = :companyId and u.status = cast('APPROVE' as status) "
				+ "and u.user_id = r.user_id and u.company_id = c.company_id "
				+ "and (r.role_id in (2, 9, 12, 13, 19, 18, 20) or u.user_id = :userId))";

		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("companyId", companyId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("userId", userId));
		return (List<UserDetailsUtilDTO>) hibernateSQLQueryResultUtilDao.getListDto(hibernateSQLQueryResultRequestDTO,
				UserDetailsUtilDTO.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CategoryDTO> getAssetDetailsByCategoryId(Integer id, Integer companyId) {
		if (!XamplifyUtils.isValidInteger(id) && !XamplifyUtils.isValidInteger(companyId)) {
			return null;
		}
		String queryString = folderAssetData;
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("categoryId", id));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("companyId", companyId));
		return (List<CategoryDTO>) hibernateSQLQueryResultUtilDao.getListDto(hibernateSQLQueryResultRequestDTO,
				CategoryDTO.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CategoryDTO> getAssetDetailsByCategoryIdForPartner(Integer id, Integer loggedInUserId) {
		if (!XamplifyUtils.isValidInteger(id) && !XamplifyUtils.isValidInteger(loggedInUserId)) {
			return null;
		}
		String queryString = folderAssetDataForPartner;
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("categoryId", id));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("userId", loggedInUserId));
		return (List<CategoryDTO>) hibernateSQLQueryResultUtilDao.getListDto(hibernateSQLQueryResultRequestDTO,
				CategoryDTO.class);
	}

	@Override
	public User getFirstNameLastNameMidlleNameByEmailId(String emailId) {
		if (XamplifyUtils.isValidString(emailId)) {
			Session session = sessionFactory.getCurrentSession();
			org.hibernate.Criteria criteria = session.createCriteria(User.class);
			criteria.add(Restrictions.eq("emailId", emailId));
			criteria.setProjection(Projections.projectionList().add(Projections.property("firstName"), "firstName")
					.add(Projections.property("middleName"), "middleName")
					.add(Projections.property("lastName"), "lastName"))
					.setResultTransformer(Transformers.aliasToBean(User.class));
			return (User) criteria.uniqueResult();
		} else {
			return null;
		}
	}

	@Override
	public Map<String, Object> getWelcomeEmailsList(Integer userId, Pagination pagination) {
		Map<String, Object> result = new HashMap<>();

		Session session = sessionFactory.getCurrentSession();
		String sql = "SELECT distinct e.id as userId, e.subject as \"subject\", e.created_time as \"sentOn\", "
				+ "e.status as \"status\" " + "FROM xt_email_activity e "
				+ "JOIN xt_email_recipient r ON r.email_activity_id = e.id "
				+ "WHERE e.company_id = :userId AND r.email_recipient_type = 'TO' ";

		if (pagination.getSearchKey() != null && !pagination.getSearchKey().isEmpty()) {
			sql += "AND e.subject ILIKE LOWER(:searchKey) ";
		}

		if ("sentOn".equalsIgnoreCase(pagination.getSortcolumn())) {
			sql += "ORDER BY e.\"created_time\" " + pagination.getSortingOrder();
		} else {
			sql += "ORDER BY e.\"created_time\" DESC";
		}

		SQLQuery query = (SQLQuery) session.createSQLQuery(sql).addScalar("userId", StandardBasicTypes.INTEGER)
				.addScalar("subject", StandardBasicTypes.STRING).addScalar("sentOn", StandardBasicTypes.TIMESTAMP)
				.addScalar("status", StandardBasicTypes.STRING).setParameter("userId", userId);

		if (pagination.getSearchKey() != null && !pagination.getSearchKey().isEmpty()) {
			query.setParameter("searchKey", "%" + pagination.getSearchKey() + "%");
		}

		return paginationUtil.setScrollableAndGetList(pagination, result, query, EmailActivityDTO.class);
	}

	@Override
	public Map<String, Object> downloadWelcomeEmailsList(Pagination pagination) {
		Map<String, Object> map = new HashMap<>();
		Session session = sessionFactory.getCurrentSession();
		String sql = "";

		sql = "SELECT DISTINCT " + "e.subject as \"subject\", " + "e.created_time as \"sentOn\", "
				+ "e.status as \"status\", " + "e.body as \"body\", " + "e.id AS \"emailActivityId\", "
				+ "(SELECT string_agg(a1.file_name, ' ,') FROM xt_activity_attachment a1 WHERE a1.email_activity_id = e.id) AS \"fileNames\", "
				+ "(SELECT string_agg(a2.file_path, ' ,') FROM xt_activity_attachment a2 WHERE a2.email_activity_id = e.id) AS \"attachmentPaths\", "
				+ "(SELECT string_agg(r1.email_id, ' ,') FROM xt_email_recipient r1 WHERE r1.email_activity_id = e.id AND r1.email_recipient_type = 'TO') AS \"toEmailIdsString\", "
				+ "(SELECT string_agg(r2.email_id, ' ,') FROM xt_email_recipient r2 WHERE r2.email_activity_id = e.id AND r2.email_recipient_type = 'CC') AS \"ccEmailIdsString\", "
				+ "(SELECT string_agg(r3.email_id, ' ,') FROM xt_email_recipient r3 WHERE r3.email_activity_id = e.id AND r3.email_recipient_type = 'BCC') AS \"bccEmailIdsString\" "
				+ "FROM xt_email_activity e "
				+ "JOIN xt_company_profile c ON e.company_id = c.company_id JOIN xt_email_recipient r ON r.email_activity_id = e.id AND r.email_recipient_type = 'TO' "
				+ "WHERE c.company_id = " + pagination.getUserId() + " ";

		if (pagination.getSearchKey() != null && !pagination.getSearchKey().isEmpty()) {
			sql += "AND e.subject ILIKE LOWER(:searchKey) ";
		}
		if ("sentOn".equalsIgnoreCase(pagination.getSortcolumn())) {
			sql += " ORDER BY e.\"created_time\" " + pagination.getSortingOrder();
		} else {
			sql += " ORDER BY e.\"created_time\" DESC ";
		}

		Query query = session.createSQLQuery(sql);
		if (pagination.getSearchKey() != null && !pagination.getSearchKey().isEmpty()) {
			query.setParameter("searchKey", "%" + pagination.getSearchKey() + "%");
		}
		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		@SuppressWarnings("unchecked")
		List<EmailActivityDTO> emailActivityDTOList = (List<EmailActivityDTO>) paginationUtil
				.getListDTO(EmailActivityDTO.class, query);
		map.put(TOTAL_RECORDS, totalRecords);
		map.put("data", emailActivityDTOList);
		return map;
	}

	@Override
	public boolean prmAccountExists() {
		String sql = "SELECT 1 FROM xt_user_role WHERE role_id = :roleId LIMIT 1";
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(sql);
		query.setParameter("roleId", Role.PRM_ROLE.getRoleId());
		return query.uniqueResult() != null;
	}
}
