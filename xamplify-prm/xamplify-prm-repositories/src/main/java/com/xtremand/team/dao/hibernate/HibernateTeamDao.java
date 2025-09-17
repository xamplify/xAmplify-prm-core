package com.xtremand.team.dao.hibernate;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.sql.JoinType;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.common.bom.Pagination;
import com.xtremand.dam.bom.DamPartner;
import com.xtremand.dam.bom.DamPartnerMapping;
import com.xtremand.exception.DuplicateEntryException;
import com.xtremand.formbeans.EmailTemplateDTO;
import com.xtremand.formbeans.RoleDTO;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.team.dao.TeamDao;
import com.xtremand.team.member.dto.TeamMemberAndPartnerIdsAndUserListIdDTO;
import com.xtremand.team.member.dto.TeamMemberDTO;
import com.xtremand.team.member.dto.TeamMemberIdAndRolesDTO;
import com.xtremand.team.member.dto.TeamMemberListDTO;
import com.xtremand.team.member.dto.TeamMemberPartnersDTO;
import com.xtremand.user.bom.Role;
import com.xtremand.user.bom.TeamMember;
import com.xtremand.user.bom.User;
import com.xtremand.user.bom.UserList;
import com.xtremand.user.bom.UserUserList;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.user.exception.TeamMemberDataAccessException;
import com.xtremand.util.PaginationUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.HibernateSQLQueryResultUtilDao;
import com.xtremand.util.dto.AdminAndTeamMemberDetailsDTO;
import com.xtremand.util.dto.CompanyAndRolesDTO;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.QueryParameterDTO;
import com.xtremand.util.dto.QueryParameterListDTO;
import com.xtremand.util.dto.SortColumnDTO;
import com.xtremand.util.dto.UserDetailsUtilDTO;
import com.xtremand.util.dto.ViewTypePatchRequestDTO;
import com.xtremand.vanity.url.dto.AnalyticsCountDTO;

@Repository
@Transactional
public class HibernateTeamDao implements TeamDao {

	private static final String SENDER_COLUMN_NAME = " sender_user_id ";

	private static final String EMAIL_ACTIVITY_TABLE_NAME = " xt_email_activity ";

	private static final String NOTE_ACTIVITY_TABLE_NAME = " xt_note ";

	private static final Logger logger = LoggerFactory.getLogger(HibernateTeamDao.class);

	private static final String USER_ID = "userId";

	private static final String ROLE_ID = "roleId";

	private static final String WHERE_USER_ID = " where user_id=";

	private static final String WHERE_CUSTOMER_ID = " where customer_id=";

	private static final String TEAM_MEMBER_ID = "teamMemberId";

	private static final String COMPANY_ID = "companyId";

	private static final String ROLE_IDS = "roleIds";

	private static final String NEW_PRIMARY_ADMIN_ID = "newPrimaryAdminId";

	private static final String EXISTING_PRIMARY_ADMIN_ID = "existingPrimaryAdminId";

	private static final String SERACH_KEY = "searchKey";

	private static final String USER_IDS = "userIds";

	private static final String CREATED_USER_ID_IN_QUERY_STRING = " where created_user_id in(";

	private static final String CREATED_USER_ID_EQUALS_TO_QUERY_STRING = " where created_user_id = ";

	private static final String ALLOWED_DOMAINS_TABLE_NAME = "xt_allowed_domain";

	private static final String UPDATE_QUERY = "update ";

	private static final String TEAM_MEMBER_IDS = "teamMemberIds";

	public static final String DAM_TABLE_NAME = "xt_dam";

	public static final String CREATED_BY_COLUMN_NAME = " created_by ";

	private static final String WHERE = " where ";

	private static final String CREATED_BY_IN_QUERY_STRING = WHERE + CREATED_BY_COLUMN_NAME + " in(";

	private static final String CREATED_BY_EQUALS_TO_QUERY_STRING = WHERE + CREATED_BY_COLUMN_NAME + " = ";

	public static final String UPDATED_BY_COLUMN_NAME = " updated_by ";

	private static final String UPDATED_BY_IN_QUERY_STRING = WHERE + UPDATED_BY_COLUMN_NAME + " in(";

	private static final String UPDATED_BY_EQUALS_TO_QUERY_STRING = WHERE + UPDATED_BY_COLUMN_NAME + " = ";

	public static final String DAM_PARTNER_TABLE_NAME = "xt_dam_partner";

	public static final String PUBLISHED_BY_COLUMN_NAME = " published_by ";

	private static final String PUBLISHED_BY_IN_QUERY_STRING = WHERE + CREATED_BY_COLUMN_NAME + " in(";

	private static final String PUBLISHED_BY_EQUALS_TO_QUERY_STRING = WHERE + PUBLISHED_BY_COLUMN_NAME + " = ";

	private static final String SET = " set ";

	private static final String XT_FLEXI_FIELDS = "xt_flexi_fields";

	private static final String TASK_ACTIVITY_TABLE_NAME = " xt_task_activity ";

	private static final String ASSIGNED_BY = " assigned_by ";

	private static final String SENDER_COLUMN_NAME_IN_QUERY_STRING = SENDER_COLUMN_NAME + "in (";

	private static final String ASSIGNED_BY_IN_QUERY_STRING = ASSIGNED_BY + " in (";

	public static final String APPROVAL_STATUS_COLUMN_NAME = " approval_status ";

	public static final String APPROVAL_STATUS_TYPE_APPROVED = " cast('APPROVED' as approval_status_type) ";

	public static final String APPROVAL_STATUS_TYPE_CREATED = " cast('CREATED' as approval_status_type) ";

	public static final String APPROVAL_STATUS_TYPE_REJECTED = " cast('REJECTED' as approval_status_type) ";

	public static final String DISTINCT_FROM_APPROVAL_STATUS_TYPE_APPROVED = "(" + APPROVAL_STATUS_COLUMN_NAME + "!="
			+ APPROVAL_STATUS_TYPE_CREATED + "OR" + APPROVAL_STATUS_COLUMN_NAME + "!=" + APPROVAL_STATUS_TYPE_REJECTED
			+ ")";

	public static final String APPROVAL_STATUS_UPDATED_BY_COLUMN_NAME = " approval_status_updated_by ";

	public static final String LEARNING_TRACK_TABLE_NAME = "xt_learning_track";

	public static final String APPROVAL_STATUS_HISTORY_TABLE_NAME = "xt_approval_status_history";

	public static final String STATUS_COLUMN_NAME = " status ";

	public static final String AND = " and ";

	public static final String DISTINCT_FROM_APPROVAL_STATUS_TYPE_APPROVED_FOR_HISTORY = "(" + STATUS_COLUMN_NAME + "!="
			+ APPROVAL_STATUS_TYPE_CREATED + "OR" + STATUS_COLUMN_NAME + "!=" + APPROVAL_STATUS_TYPE_REJECTED + ")";

	public static final String APPROVAL_REFERENCE_ID_IS_NULL = "approval_reference_id is null";

	public static final String NO_MEMBERS_FOUND_INDICATOR = "--"; // XNFR-1022

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private PaginationUtil paginationUtil;

	@Autowired
	private UserDAO userDao;

	@Autowired
	private HibernateSQLQueryResultUtilDao hibernateSqlQueryResultDao;

	@Value("${query.findAllTeamMembers}")
	private String findAllTeamMembers;

	@Value("${query.searchfindAllTeamMembers}")
	private String searchfindAllTeamMembers;

	@Value("${query.orderByfindAllTeamMembers}")
	private String sortByQueryFindAllTeamMembers;

	@Value("${query.orderByPrimaryAdmin}")
	private String orderByPrimaryAdmin;

	@Value("${query.groupByfindAllTeamMembers}")
	private String groupByfindAllTeamMembers;

	@Value("${query.filterByTeamMemberGroup}")
	private String filterByTeamMemberGroupQuery;

	@Value("${query.filterByTeamMember}")
	private String filterByTeamMemberQuery;

	@Value("${query.findTeamMemberById}")
	private String findTeamMemberById;

	@Value("${query.findTeamMemberDetailsByGroupId}")
	private String findTeamMemberDetailsByGroupId;

	@Value("${query.searchfindTeamMemberDetailsByGroupId}")
	private String searchfindTeamMemberDetailsByGroupId;

	@Value("${query.orderByfindAllTeamMembers}")
	private String orderByfindAllTeamMembers;

	@Value("${query.findTeamMemberPartnerMasterListName}")
	private String findTeamMemberPartnerMasterListNameQuery;

	@Value("${query.teamMemberPartnersQuery}")
	private String teamMemberPartnersQuery;

	@Value("${query.teamMemberPartnersSearchQuery}")
	private String teamMemberPartnersSearchQuery;

	@Value("${query.teamMemberIdAndPartnerIdsQuery}")
	private String teamMemberIdAndPartnerIdsQuery;

	@Value("${query.deleteTeamMemberPartnersQuery}")
	private String deleteTeamMemberPartnersQuery;

	@Value("${query.filterByTeamMemberIds}")
	private String filterByTeamMemberIds;

	@Value("${query.primaryAdmin}")
	private String primaryAdmin;

	@Value("${query.searchPrimaryAdmin}")
	private String searchPrimaryAdminQuery;

	@Value("${query.findPrimaryAdminGroupBy}")
	private String primaryAdminGroupByQuery;

	@Override
	public void saveAll(List<TeamMember> teamMembers, Integer companyId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			for (int i = 0; i < teamMembers.size(); i++) {
				TeamMember teamMember = teamMembers.get(i);
				User teamMemberUser = teamMember.getTeamMember();
				session.save(teamMember);
				saveTeamMemberPartnerList(session, teamMember, teamMemberUser, false);
				if (i % 30 == 0) {
					session.flush();
					session.clear();
				}
			}
		} catch (ConstraintViolationException con) {
			throw new DuplicateEntryException(
					"Enable As Admin option cannot be selected as there are at most two admins for this company");
		} catch (HibernateException | TeamMemberDataAccessException e) {
			throw new TeamMemberDataAccessException(e.getMessage());
		} catch (Exception ex) {
			throw new TeamMemberDataAccessException(ex.getMessage());
		}
	}

	private void saveTeamMemberPartnerList(Session session, TeamMember teamMember, User teamMemberUser,
			boolean newPartnerGroupRequired) {
		UserList userList = teamMember.getTeamMemberUserList();
		List<UserUserList> teamMemberPartners = userList.getTeamMemberPartners();
		getUpdatedUserListNameAndInsertIntoUserUserList(session, teamMember, teamMemberUser, userList,
				teamMemberPartners, true, newPartnerGroupRequired);
	}

	private void getUpdatedUserListNameAndInsertIntoUserUserList(Session session, TeamMember teamMember,
			User teamMemberUser, UserList userList, List<UserUserList> teamMemberPartners, boolean createTeamMemberList,
			boolean newPartnerGroupRequired) {
		if ((teamMemberPartners != null && !teamMemberPartners.isEmpty()) || newPartnerGroupRequired) {
			AdminAndTeamMemberDetailsDTO adminAndTeamMemberDetailsDTO = new AdminAndTeamMemberDetailsDTO();
			adminAndTeamMemberDetailsDTO.setEmailId(teamMemberUser.getEmailId());
			String firstName = StringUtils.hasText(teamMemberUser.getFirstName()) ? teamMemberUser.getFirstName().trim()
					: "";
			String lastName = StringUtils.hasText(teamMemberUser.getLastName()) ? teamMemberUser.getLastName().trim()
					: "";
			adminAndTeamMemberDetailsDTO.setFirstName(firstName);
			adminAndTeamMemberDetailsDTO.setLastName(lastName);
			adminAndTeamMemberDetailsDTO.setId(teamMember.getId());
			AdminAndTeamMemberDetailsDTO teamMemberMasterPartnerListDetails = XamplifyUtils
					.getTeamMemberMasterPartnerListInfo(adminAndTeamMemberDetailsDTO);
			long milliSeconds = System.nanoTime();
			String partnerListName = teamMemberMasterPartnerListDetails.getFullName() + "_" + milliSeconds
					+ "-Master Partner Group";
			userList.setName(partnerListName);
			if (createTeamMemberList) {
				session.save(userList);
				logger.debug("********New Team Member Partner List Created**************");
			}
			if (XamplifyUtils.isNotEmptyList(teamMemberPartners)) {
				for (int i = 0; i < teamMemberPartners.size(); i++) {
					session.save(teamMemberPartners.get(i));
					logger.debug("********Partner Inserted Into Partner List**************");
					if (i % 30 == 0) {
						session.flush();
						session.clear();
					}
				}
			}

		} else {
			logger.debug("********No Team Member Master Partner Group Created**************");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> listTeamMemberEmailIds() {
		try {
			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(
					"select distinct LOWER(u.email_id) from xt_user_profile u,xt_team_member t where t.team_member_id = u.user_id and u.email_id is not null");
			return query.list();
		} catch (HibernateException | TeamMemberDataAccessException e) {
			throw new TeamMemberDataAccessException(e.getMessage());
		} catch (Exception ex) {
			throw new TeamMemberDataAccessException(ex.getMessage());
		}
	}

	@Override
	public void saveUsersAndTeamMembers(List<User> users, Integer companyId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			for (int i = 0; i < users.size(); i++) {
				User user = users.get(i);
				session.save(user);
				List<TeamMember> teamMembers = user.getTeamMembers();
				for (TeamMember teamMember : teamMembers) {
					saveTeamMemberPartnerList(session, teamMember, teamMember.getTeamMember(), false);
				}
				if (i % 30 == 0) {
					session.flush();
					session.clear();
				}
			}
		} catch (ConstraintViolationException con) {
			throw new DuplicateEntryException(
					"Enable As Admin cannot be enabled as there are at most two admins for this company");
		} catch (HibernateException | TeamMemberDataAccessException e) {
			throw new TeamMemberDataAccessException(e);
		} catch (Exception ex) {
			throw new TeamMemberDataAccessException(ex);
		}
	}

	@Override
	public Map<String, Object> findAll(Pagination pagination) {
		try {
			HashMap<String, Object> map = new HashMap<>();
			String finalQueryString = "";
			String searchKey = pagination.getSearchKey();
			boolean hasSearchKey = StringUtils.hasText(searchKey);
			boolean isTeamMembersFilter = false;
			boolean isTeamMemberGroupFilter = "teamMemberGroup".equals(pagination.getFilterKey());
			boolean isTeamMemberFilter = "teamMemberFilter".equals(pagination.getFilterKey())
					&& pagination.getTeamMemberId() != null && pagination.getTeamMemberId() > 0;
			String teamMembersQueryString = addTeamMembersQueryString(isTeamMemberGroupFilter, isTeamMemberFilter);

			if (hasSearchKey) {
				finalQueryString = appendSearchQuery(finalQueryString, searchKey, isTeamMemberGroupFilter,
						teamMembersQueryString);
			} else {
				finalQueryString = appendQueryWithOutSearch(finalQueryString, isTeamMemberGroupFilter,
						teamMembersQueryString);
			}

			if (XamplifyUtils.isValidString(pagination.getSortingOrder())
					&& XamplifyUtils.isValidString(pagination.getSortcolumn())) {
				finalQueryString = addSortQuery(pagination, finalQueryString);
			}

			if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
				isTeamMembersFilter = true;
			}

			finalQueryString = addTeamMemberFilterToQueryString(finalQueryString, isTeamMemberFilter,
					isTeamMembersFilter);

			finalQueryString = appendDateQuery(finalQueryString, pagination);

			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(finalQueryString);
			if (finalQueryString.indexOf(":userId") > -1) {
				query.setParameter(USER_ID, pagination.getUserId());
			}
			setCompanyIdQueryParameter(pagination, query);
			if (isTeamMemberGroupFilter) {
				query.setParameter("teamMemberGroupId", pagination.getCategoryId());
			} else {
				query.setParameterList(ROLE_IDS, Role.getAllAdminRoleIds());
			}
			if (isTeamMemberFilter) {
				query.setParameter("teamMemberUserId", pagination.getTeamMemberId());
			}

			if (pagination.getSelectedTeamMemberIds() != null && !pagination.getSelectedTeamMemberIds().isEmpty()) {
				query.setParameterList(TEAM_MEMBER_IDS, pagination.getSelectedTeamMemberIds());
			}
			return paginationUtil.setScrollableAndGetList(pagination, map, query, TeamMemberListDTO.class);
		} catch (HibernateException | TeamMemberDataAccessException e) {
			throw new TeamMemberDataAccessException(e);
		} catch (Exception ex) {
			throw new TeamMemberDataAccessException(ex);
		}

	}

	private String addSortQuery(Pagination pagination, String finalQueryString) {
		String valueToReplaceInSort = "u.user_id  = :userId desc";
		if ("Asc".equalsIgnoreCase(pagination.getSortingOrder())) {
			if ("name".equalsIgnoreCase(pagination.getSortcolumn())) {
				finalQueryString = finalQueryString.replace(valueToReplaceInSort, "u.firstName ASC nulls first");

			} else if ("logInTime".equalsIgnoreCase(pagination.getSortcolumn())) {
				finalQueryString = finalQueryString.replace(valueToReplaceInSort, "u.datelastlogin ASC");
			}
		} else if ("Desc".equalsIgnoreCase(pagination.getSortingOrder())) {
			if ("name".equalsIgnoreCase(pagination.getSortcolumn())) {
				finalQueryString = finalQueryString.replace(valueToReplaceInSort, "u.firstName DESC nulls last");
			} else if ("logInTime".equalsIgnoreCase(pagination.getSortcolumn())) {
				finalQueryString = finalQueryString.replace(valueToReplaceInSort, "u.datelastlogin DESC");
			}
		}
		return finalQueryString;
	}

	private String appendDateQuery(String finalQueryString, Pagination pagination) {
		if (pagination.isPartnerJourneyFilter()) {
			String dateFilterQueryString = frameDateQueryStringForTeamMembers(pagination, "u.datelastlogin");
			finalQueryString = finalQueryString.replace("{dateFilterQuery}", dateFilterQueryString);
		} else {
			finalQueryString = finalQueryString.replace("{dateFilterQuery}", "");
		}
		return finalQueryString;
	}

	private void setCompanyIdQueryParameter(Pagination pagination, SQLQuery query) {
		if (pagination.isPartnerJourneyFilter()) {
			Integer partnerCompanyId = pagination.getPartnerCompanyId();
			query.setParameter(COMPANY_ID, partnerCompanyId);
		} else {
			Integer companyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			query.setParameter(COMPANY_ID, companyId);
		}
	}

	private String addTeamMembersQueryString(boolean isTeamMemberGroupFilter, boolean isTeamMemberFilter) {
		String teamMembersQueryString = "";
		if (isTeamMemberGroupFilter) {
			teamMembersQueryString = findAllTeamMembers + " " + filterByTeamMemberGroupQuery;
		} else if (isTeamMemberFilter) {
			teamMembersQueryString = findAllTeamMembers + " " + filterByTeamMemberQuery;
		} else {
			teamMembersQueryString = findAllTeamMembers;
		}
		return teamMembersQueryString;
	}

	private String addTeamMemberFilterToQueryString(String finalQueryString, boolean isTeamMemberFilter,
			boolean isTeamMembersFilter) {
		if (isTeamMemberFilter) {
			finalQueryString = finalQueryString.replace("{teamMemberFilter}", filterByTeamMemberQuery);
		} else {
			finalQueryString = finalQueryString.replace("{teamMemberFilter}", "");
		}
		if (isTeamMembersFilter) {
			finalQueryString = finalQueryString.replace("{teamMembersFilter}", filterByTeamMemberIds);
		} else {
			finalQueryString = finalQueryString.replace("{teamMembersFilter}", "");
		}
		return finalQueryString;
	}

	private String appendQueryWithOutSearch(String finalQueryString, boolean isTeamMemberGroupFilter,
			String teamMembersQueryString) {
		if (!isTeamMemberGroupFilter) {
			finalQueryString += primaryAdmin + " " + primaryAdminGroupByQuery + teamMembersQueryString + " "
					+ groupByfindAllTeamMembers + " " + orderByPrimaryAdmin + " ";
		} else {
			finalQueryString = teamMembersQueryString + " " + groupByfindAllTeamMembers + " "
					+ sortByQueryFindAllTeamMembers + " ";
		}
		return finalQueryString;
	}

	private String appendSearchQuery(String finalQueryString, String searchKey, boolean isTeamMemberGroupFilter,
			String teamMembersQueryString) {
		if (!isTeamMemberGroupFilter) {
			finalQueryString += primaryAdmin + searchPrimaryAdminQuery.replace(SERACH_KEY, searchKey) + " "
					+ primaryAdminGroupByQuery + " " + teamMembersQueryString + " "
					+ searchfindAllTeamMembers.replace(SERACH_KEY, searchKey) + " " + groupByfindAllTeamMembers + " "
					+ orderByPrimaryAdmin + " ";
		} else {
			finalQueryString = teamMembersQueryString + " " + searchfindAllTeamMembers.replace(SERACH_KEY, searchKey)
					+ " " + groupByfindAllTeamMembers + " " + sortByQueryFindAllTeamMembers + " ";
		}
		return finalQueryString;
	}

	@Override
	public void deleteUnMappedRoleIds(List<Integer> userIds, Set<Integer> roleIds) {
		if (userIds != null && !userIds.isEmpty() && roleIds != null && !roleIds.isEmpty()) {
			Session session = sessionFactory.getCurrentSession();
			Query query = session
					.createSQLQuery("delete from xt_user_role where user_id in (:userIds) and role_id in (:roleIds)");
			query.setParameterList(USER_IDS, userIds);
			query.setParameterList(ROLE_IDS, roleIds);
			query.executeUpdate();
		}

	}

	@Override
	public void addNewRoles(Integer teamMemberId, Set<Integer> roleIds) {
		if (teamMemberId != null && teamMemberId > 0 && roleIds != null && !roleIds.isEmpty()) {
			Session session = sessionFactory.getCurrentSession();
			for (Integer roleId : roleIds) {
				boolean roleExists = roleExists(roleId, teamMemberId);
				if (!roleExists) {
					Query query = session
							.createSQLQuery("insert into  xt_user_role(user_id,role_id) values(:userId,:roleId)");
					setUserIdAndRoleIdQueryParameters(roleId, teamMemberId, query);
					query.executeUpdate();
				}

			}

		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public void delete(TeamMemberDTO teamMemberDTO) {
		try {
			Integer teamMemberId = teamMemberDTO.getId();
			Integer superiorId = teamMemberDTO.getOrgAdminId();
			Session session = sessionFactory.getCurrentSession();
			if (teamMemberId > 0) {
				if (superiorId == 0) {
					superiorId = getOrgAdminIdByTeamMemberId(teamMemberId);
				}
				removeAllData(teamMemberId, superiorId, session);
			} else {
				Integer companyId = userDao.findByPrimaryKey(superiorId, new FindLevel[] { FindLevel.COMPANY_PROFILE })
						.getCompanyProfile().getId();
				List<Integer> teamMemberIds = session
						.createSQLQuery("select team_member_id from  xt_team_member where company_id=" + companyId
								+ " and team_member_id!=" + superiorId)
						.list();
				removeAllTeamMembersData(superiorId, session, teamMemberIds);
			}

		} catch (HibernateException | TeamMemberDataAccessException e) {
			throw new TeamMemberDataAccessException(e);
		} catch (Exception ex) {
			throw new TeamMemberDataAccessException(ex);
		}

	}

	/**
	 * @param teamMemberId
	 * @param userId
	 * @param session
	 * @param teamMemberIds
	 */
	private void removeAllTeamMembersData(Integer userId, Session session, List<Integer> teamMemberIds) {
		Query roleQuery = session
				.createSQLQuery("delete from xt_user_role where user_id in(:teamMemberIds) and role_id not in(1,3)");
		roleQuery.setParameterList(TEAM_MEMBER_IDS, teamMemberIds);
		Query teamMemberQuery = session
				.createSQLQuery("delete from xt_team_member where team_member_id in(:teamMemberIds)");
		teamMemberQuery.setParameterList(TEAM_MEMBER_IDS, teamMemberIds);
		Query userSubscriptionQuery = session
				.createSQLQuery("delete from xt_user_subscription where customer_id=:customerId");
		userSubscriptionQuery.setParameter("customerId", userId);
		userSubscriptionQuery.executeUpdate();
		roleQuery.executeUpdate();
		teamMemberQuery.executeUpdate();
		String teamMemberIdsString = String.valueOf(teamMemberIds);
		teamMemberIdsString = teamMemberIdsString.substring(1, teamMemberIdsString.length() - 1);
		session.createSQLQuery(
				"update xt_user_profile set company_id=NULL where user_id in(" + teamMemberIdsString + ")")
				.executeUpdate();

		/******** Email Templates *******/
		session.createSQLQuery(
				"update xt_email_templates set user_id=" + userId + " where user_id in(" + teamMemberIdsString + ")")
				.executeUpdate();

		/******** Email Templates *******/
		session.createSQLQuery(
				"update xt_email_templates set vendor_id=" + userId + " where vendor_id in" + teamMemberIdsString)
				.executeUpdate();

		/******** Forms *******/
		session.createSQLQuery("update xt_form set created_user_id=" + userId + CREATED_USER_ID_IN_QUERY_STRING
				+ teamMemberIdsString + ")").executeUpdate();
		/******** Pages *******/
		session.createSQLQuery("update xt_landing_page set created_user_id=" + userId + CREATED_USER_ID_IN_QUERY_STRING
				+ teamMemberIdsString + ")").executeUpdate();
		/******** Video Files *******/
		session.createSQLQuery("update xt_video_files set created_user_id=" + userId + CREATED_USER_ID_IN_QUERY_STRING
				+ teamMemberIdsString + ")").executeUpdate();
		/******** UserList *******/
		session.createSQLQuery(
				"update xt_user_list set customer_id=" + userId + " where customer_id in(" + teamMemberIdsString + ")")
				.executeUpdate();

		/******** Campaign *******/
		session.createSQLQuery(
				"update xt_campaign set customer_id=" + userId + " where customer_id in(" + teamMemberIdsString + ")")
				.executeUpdate();

		/**** Domain *****/
		session.createSQLQuery(UPDATE_QUERY + ALLOWED_DOMAINS_TABLE_NAME + " set created_user_id=" + userId
				+ CREATED_USER_ID_IN_QUERY_STRING + teamMemberIdsString + ")").executeUpdate();

		session.createSQLQuery(UPDATE_QUERY + ALLOWED_DOMAINS_TABLE_NAME + " set updated_user_id=" + userId
				+ " where updated_user_id in(" + teamMemberIdsString + ")").executeUpdate();

		/*** Dam - update pending approval status XNFR-821 **/
		session.createSQLQuery(UPDATE_QUERY + DAM_TABLE_NAME + SET + APPROVAL_STATUS_COLUMN_NAME + "="
				+ APPROVAL_STATUS_TYPE_APPROVED + "," + APPROVAL_STATUS_UPDATED_BY_COLUMN_NAME + "=" + userId
				+ CREATED_BY_IN_QUERY_STRING + teamMemberIdsString + ")" + AND
				+ DISTINCT_FROM_APPROVAL_STATUS_TYPE_APPROVED + AND + APPROVAL_REFERENCE_ID_IS_NULL).executeUpdate();

		/**** Dam *****/
		session.createSQLQuery(UPDATE_QUERY + DAM_TABLE_NAME + SET + CREATED_BY_COLUMN_NAME + "=" + userId
				+ CREATED_BY_IN_QUERY_STRING + teamMemberIdsString + ")").executeUpdate();

		session.createSQLQuery(UPDATE_QUERY + DAM_TABLE_NAME + SET + UPDATED_BY_COLUMN_NAME + "=" + userId
				+ UPDATED_BY_IN_QUERY_STRING + teamMemberIdsString + ")").executeUpdate();

		/**** Dam Partner *****/
		session.createSQLQuery(UPDATE_QUERY + DAM_PARTNER_TABLE_NAME + SET + PUBLISHED_BY_COLUMN_NAME + "=" + userId
				+ PUBLISHED_BY_IN_QUERY_STRING + teamMemberIdsString + ")").executeUpdate();

		session.createSQLQuery(UPDATE_QUERY + DAM_PARTNER_TABLE_NAME + SET + UPDATED_BY_COLUMN_NAME + "=" + userId
				+ UPDATED_BY_IN_QUERY_STRING + teamMemberIdsString + ")").executeUpdate();

		/**** Update xt_category and xt_category_module tables *****/
		session.createSQLQuery("update xt_category set created_user_id=" + userId + CREATED_USER_ID_IN_QUERY_STRING
				+ teamMemberIdsString + ")").executeUpdate();

		session.createSQLQuery("update xt_category_module set created_user_id=" + userId
				+ CREATED_USER_ID_IN_QUERY_STRING + teamMemberIdsString + ")").executeUpdate();

		/**** DashboardButton PartnerGroup Mapping ******/
		session.createSQLQuery("update xt_dashboard_buttons_partner_group_mapping set published_by=" + userId
				+ " where published_by in(" + teamMemberIdsString + ")").executeUpdate();

		/**** DashboardButton Partner Company Mapping ******/
		session.createSQLQuery("update xt_dashboard_buttons_partner_company_mapping set published_by=" + userId
				+ " where published_by in(" + teamMemberIdsString + ")").executeUpdate();

		session.createSQLQuery("update xt_dashboard_buttons_partner_company_mapping set published_to=" + userId
				+ " where published_to in(" + teamMemberIdsString + ")").executeUpdate();

		/***** XNFR-671 FLEXI-FIELDS ******/
		session.createSQLQuery(UPDATE_QUERY + XT_FLEXI_FIELDS + SET + CREATED_BY_COLUMN_NAME + "= " + userId
				+ CREATED_BY_IN_QUERY_STRING + teamMemberIdsString + ")").executeUpdate();

		/***** Email Activity *****/
		session.createSQLQuery(UPDATE_QUERY + EMAIL_ACTIVITY_TABLE_NAME + SET + SENDER_COLUMN_NAME + "=" + userId
				+ WHERE + SENDER_COLUMN_NAME_IN_QUERY_STRING + teamMemberIdsString + ")").executeUpdate();

		/***** Note Activity *****/
		session.createSQLQuery(UPDATE_QUERY + NOTE_ACTIVITY_TABLE_NAME + SET + CREATED_BY_COLUMN_NAME + "=" + userId
				+ CREATED_BY_IN_QUERY_STRING + teamMemberIdsString + ")").executeUpdate();

		/****** XNFR-757/ Task Activity *******/
		session.createSQLQuery(UPDATE_QUERY + TASK_ACTIVITY_TABLE_NAME + SET + ASSIGNED_BY + "=" + userId + WHERE
				+ ASSIGNED_BY_IN_QUERY_STRING + teamMemberIdsString + ")").executeUpdate();

		/**** Learning Track XNFR-821 **/
		session.createSQLQuery(UPDATE_QUERY + LEARNING_TRACK_TABLE_NAME + SET + APPROVAL_STATUS_COLUMN_NAME + "="
				+ APPROVAL_STATUS_TYPE_APPROVED + "," + APPROVAL_STATUS_UPDATED_BY_COLUMN_NAME + "=" + userId
				+ CREATED_BY_IN_QUERY_STRING + teamMemberIdsString + ")" + AND
				+ DISTINCT_FROM_APPROVAL_STATUS_TYPE_APPROVED).executeUpdate();

		session.createSQLQuery(UPDATE_QUERY + LEARNING_TRACK_TABLE_NAME + SET + CREATED_BY_COLUMN_NAME + "=" + userId
				+ CREATED_BY_IN_QUERY_STRING + teamMemberIdsString + ")").executeUpdate();

		session.createSQLQuery(UPDATE_QUERY + LEARNING_TRACK_TABLE_NAME + SET + UPDATED_BY_COLUMN_NAME + "=" + userId
				+ UPDATED_BY_IN_QUERY_STRING + teamMemberIdsString + ")").executeUpdate();

		/**** Approval Status History XNFR-821 ****/
		session.createSQLQuery(UPDATE_QUERY + APPROVAL_STATUS_HISTORY_TABLE_NAME + SET + CREATED_BY_COLUMN_NAME + "="
				+ userId + "," + STATUS_COLUMN_NAME + "=" + APPROVAL_STATUS_TYPE_APPROVED + CREATED_BY_IN_QUERY_STRING
				+ teamMemberIdsString + ")" + AND + STATUS_COLUMN_NAME + "=" + APPROVAL_STATUS_TYPE_CREATED)
				.executeUpdate();

		/***** XNFR-860 *****/
		session.createSQLQuery(
				"delete from xt_dashboard_custom_layout where created_user_id in(" + teamMemberIdsString + ")")
				.executeUpdate();

		/***** XNFR-1029 *****/
		session.createSQLQuery("delete from xt_dam_analytics where action_performed_by in(" + teamMemberIdsString + ")")
				.executeUpdate();
	}

	/**
	 * @param teamMemberIdToRemove
	 * @param teamMemberIdToInsert
	 * @param session
	 */
	private void removeAllData(Integer teamMemberIdToRemove, Integer teamMemberIdToInsert, Session session) {
		session.createSQLQuery(
				"delete from xt_user_role where user_id  in(" + teamMemberIdToRemove + ") and role_id not in(1,3)")
				.executeUpdate();
		session.createSQLQuery("delete from xt_team_member where team_member_id in(" + teamMemberIdToRemove + ")")
				.executeUpdate();
		session.createSQLQuery("delete from xt_user_subscription where user_id in(" + teamMemberIdToRemove + ")")
				.executeUpdate();
		session.createSQLQuery("update xt_user_profile set company_id=NULL where user_id=" + teamMemberIdToRemove)
				.executeUpdate();

		/******** Email Templates *******/
		session.createSQLQuery(
				"update xt_email_templates set user_id=" + teamMemberIdToInsert + WHERE_USER_ID + teamMemberIdToRemove)
				.executeUpdate();

		/******** Email Templates *******/
		session.createSQLQuery("update xt_email_templates set vendor_id=" + teamMemberIdToInsert + " where vendor_id="
				+ teamMemberIdToRemove).executeUpdate();

		/******** Pages *******/
		session.createSQLQuery("update xt_landing_page set created_user_id=" + teamMemberIdToInsert
				+ CREATED_USER_ID_EQUALS_TO_QUERY_STRING + teamMemberIdToRemove).executeUpdate();

		/******** Forms *******/
		session.createSQLQuery("update xt_form set created_user_id=" + teamMemberIdToInsert
				+ CREATED_USER_ID_EQUALS_TO_QUERY_STRING + teamMemberIdToRemove).executeUpdate();

		/*********** Video Files **********/
		session.createSQLQuery("update xt_video_files set customer_id=" + teamMemberIdToInsert + WHERE_CUSTOMER_ID
				+ teamMemberIdToRemove).executeUpdate();
		/*********** Partners/Contacts/Share Leads *************/
		session.createSQLQuery("update xt_user_list set customer_id=" + teamMemberIdToInsert + WHERE_CUSTOMER_ID
				+ teamMemberIdToRemove).executeUpdate();
		/******* Campaign ******/
		session.createSQLQuery(
				"update xt_campaign set customer_id=" + teamMemberIdToInsert + WHERE_CUSTOMER_ID + teamMemberIdToRemove)
				.executeUpdate();

		/** XBI-3132 ***/
		updateFromAndFromEmailInCampaignTable(teamMemberIdToRemove, teamMemberIdToInsert, session);

		/******* Partnership ******/
		session.createSQLQuery("update xt_partnership set vendor_id=" + teamMemberIdToInsert + " where vendor_id ="
				+ teamMemberIdToRemove).executeUpdate();

		session.createSQLQuery("update xt_partnership set partner_id=" + teamMemberIdToInsert + " where partner_id="
				+ teamMemberIdToRemove).executeUpdate();

		/**** Domain *****/
		session.createSQLQuery(UPDATE_QUERY + ALLOWED_DOMAINS_TABLE_NAME + " set created_user_id="
				+ teamMemberIdToInsert + CREATED_USER_ID_EQUALS_TO_QUERY_STRING + teamMemberIdToRemove).executeUpdate();

		session.createSQLQuery(UPDATE_QUERY + ALLOWED_DOMAINS_TABLE_NAME + " set updated_user_id="
				+ teamMemberIdToInsert + " where updated_user_id = " + teamMemberIdToRemove).executeUpdate();

		/*** Dam - update pending approval status XNFR-821 **/
		session.createSQLQuery(UPDATE_QUERY + DAM_TABLE_NAME + SET + APPROVAL_STATUS_COLUMN_NAME + "="
				+ APPROVAL_STATUS_TYPE_APPROVED + "," + APPROVAL_STATUS_UPDATED_BY_COLUMN_NAME + "="
				+ teamMemberIdToInsert + CREATED_BY_EQUALS_TO_QUERY_STRING + teamMemberIdToRemove + AND
				+ DISTINCT_FROM_APPROVAL_STATUS_TYPE_APPROVED + AND + APPROVAL_REFERENCE_ID_IS_NULL).executeUpdate();

		/***** Dam *****/
		session.createSQLQuery(UPDATE_QUERY + DAM_TABLE_NAME + SET + CREATED_BY_COLUMN_NAME + "=" + teamMemberIdToInsert
				+ CREATED_BY_EQUALS_TO_QUERY_STRING + teamMemberIdToRemove).executeUpdate();

		session.createSQLQuery(UPDATE_QUERY + DAM_TABLE_NAME + SET + UPDATED_BY_COLUMN_NAME + "=" + teamMemberIdToInsert
				+ UPDATED_BY_EQUALS_TO_QUERY_STRING + teamMemberIdToRemove).executeUpdate();

		/***** Dam Partner *****/
		session.createSQLQuery(UPDATE_QUERY + DAM_PARTNER_TABLE_NAME + SET + PUBLISHED_BY_COLUMN_NAME + "="
				+ teamMemberIdToInsert + PUBLISHED_BY_EQUALS_TO_QUERY_STRING + teamMemberIdToRemove).executeUpdate();

		session.createSQLQuery(UPDATE_QUERY + DAM_PARTNER_TABLE_NAME + SET + UPDATED_BY_COLUMN_NAME + "="
				+ teamMemberIdToInsert + UPDATED_BY_EQUALS_TO_QUERY_STRING + teamMemberIdToRemove).executeUpdate();

		/***** Email Activity *****/
		session.createSQLQuery(UPDATE_QUERY + EMAIL_ACTIVITY_TABLE_NAME + SET + SENDER_COLUMN_NAME + "="
				+ teamMemberIdToInsert + WHERE + SENDER_COLUMN_NAME + "=" + teamMemberIdToRemove).executeUpdate();

		/***** Note Activity *****/
		session.createSQLQuery(UPDATE_QUERY + NOTE_ACTIVITY_TABLE_NAME + SET + CREATED_BY_COLUMN_NAME + "="
				+ teamMemberIdToInsert + WHERE + CREATED_BY_COLUMN_NAME + "=" + teamMemberIdToRemove).executeUpdate();

		/****
		 * If this email id is part of any partner list then update the email Id With
		 * Selected Email Id
		 ***********/
		boolean isTeamMemberExists = userDao.isUserExistsinList(teamMemberIdToRemove);
		if (isTeamMemberExists) {
			String subQuery = "select ul.user_list_id from xt_user_list ul,xt_user_userlist uul where ul.user_list_id = uul.user_list_id and "
					+ " ul.is_partner_userlist = true" + " and uul.user_id =" + teamMemberIdToRemove;
			String query = "update xt_user_userlist set user_id=" + teamMemberIdToInsert + WHERE_USER_ID
					+ teamMemberIdToRemove + " and user_list_id in (" + subQuery + ")";
			session.createSQLQuery(query).executeUpdate();
			session.createSQLQuery("update xt_campaign_partner set user_id=" + teamMemberIdToInsert + WHERE_USER_ID
					+ teamMemberIdToRemove).executeUpdate();

		}
		/********** Update/Remove from xt_dam_partner_mapping ******************/
		updateDamPartnerMappingTable(teamMemberIdToRemove, teamMemberIdToInsert, session);

		/**** Update xt_category and xt_category_module tables *****/
		session.createSQLQuery("update xt_category set created_user_id=" + teamMemberIdToInsert
				+ " where created_user_id=" + teamMemberIdToRemove).executeUpdate();

		session.createSQLQuery("update xt_category_module set created_user_id=" + teamMemberIdToInsert
				+ " where created_user_id=" + teamMemberIdToRemove).executeUpdate();

		/**** DashboardButton PartnerGroup Mapping ******/
		session.createSQLQuery("update xt_dashboard_buttons_partner_group_mapping set published_by="
				+ teamMemberIdToInsert + " where published_by=" + teamMemberIdToRemove + "").executeUpdate();

		/**** DashboardButton Partner Company Mapping ******/
		session.createSQLQuery("update xt_dashboard_buttons_partner_company_mapping set published_by="
				+ teamMemberIdToInsert + " where published_by=" + teamMemberIdToRemove + "").executeUpdate();

		session.createSQLQuery("update xt_dashboard_buttons_partner_company_mapping set published_to="
				+ teamMemberIdToInsert + " where published_to=" + teamMemberIdToRemove + "").executeUpdate();

		/***** XNFR-671 FLEXI-FIELDS ******/
		session.createSQLQuery(UPDATE_QUERY + XT_FLEXI_FIELDS + SET + CREATED_BY_COLUMN_NAME + "= "
				+ teamMemberIdToInsert + CREATED_BY_EQUALS_TO_QUERY_STRING + teamMemberIdToRemove).executeUpdate();

		/****** XNFR-757 *******/
		session.createSQLQuery(UPDATE_QUERY + TASK_ACTIVITY_TABLE_NAME + SET + ASSIGNED_BY + "=" + teamMemberIdToInsert
				+ WHERE + ASSIGNED_BY + "=" + teamMemberIdToRemove).executeUpdate();

		/**** Learning Track XNFR-821 **/
		session.createSQLQuery(UPDATE_QUERY + LEARNING_TRACK_TABLE_NAME + SET + APPROVAL_STATUS_COLUMN_NAME + "="
				+ APPROVAL_STATUS_TYPE_APPROVED + "," + APPROVAL_STATUS_UPDATED_BY_COLUMN_NAME + "="
				+ teamMemberIdToInsert + CREATED_BY_EQUALS_TO_QUERY_STRING + teamMemberIdToRemove + AND
				+ DISTINCT_FROM_APPROVAL_STATUS_TYPE_APPROVED).executeUpdate();

		session.createSQLQuery(UPDATE_QUERY + LEARNING_TRACK_TABLE_NAME + SET + CREATED_BY_COLUMN_NAME + "="
				+ teamMemberIdToInsert + CREATED_BY_EQUALS_TO_QUERY_STRING + teamMemberIdToRemove).executeUpdate();

		session.createSQLQuery(UPDATE_QUERY + LEARNING_TRACK_TABLE_NAME + SET + UPDATED_BY_COLUMN_NAME + "="
				+ teamMemberIdToInsert + UPDATED_BY_EQUALS_TO_QUERY_STRING + teamMemberIdToRemove).executeUpdate();

		/**** Approval Status History XNFR-821 ****/
		session.createSQLQuery(UPDATE_QUERY + APPROVAL_STATUS_HISTORY_TABLE_NAME + SET + CREATED_BY_COLUMN_NAME + "="
				+ teamMemberIdToInsert + "," + STATUS_COLUMN_NAME + "=" + APPROVAL_STATUS_TYPE_APPROVED
				+ CREATED_BY_EQUALS_TO_QUERY_STRING + teamMemberIdToRemove + AND + STATUS_COLUMN_NAME + "="
				+ APPROVAL_STATUS_TYPE_CREATED).executeUpdate();

		/***** XNFR-860 *****/
		session.createSQLQuery(
				"delete from xt_dashboard_custom_layout where created_user_id in(" + teamMemberIdToRemove + ")")
				.executeUpdate();

		/***** XNFR-1029 *****/
		session.createSQLQuery(
				"delete from xt_dam_analytics where action_performed_by in(" + teamMemberIdToRemove + ")")
				.executeUpdate();
	}

	private void updateFromAndFromEmailInCampaignTable(Integer teamMemberIdToRemove, Integer teamMemberIdToInsert,
			Session session) {
		String emailIdToUpdate = "";
		String fromNameToReplace = "";
		User replacedUser = userDao.getFirstNameLastNameAndEmailIdByUserId(teamMemberIdToInsert);
		if (replacedUser != null) {
			emailIdToUpdate = replacedUser.getEmailId();
			fromNameToReplace = XamplifyUtils.setDisplayName(replacedUser);
		}
		String emailIdToReplace = userDao.getEmailIdByUserId(teamMemberIdToRemove);
		if (XamplifyUtils.isValidString(emailIdToUpdate) && XamplifyUtils.isValidString(emailIdToReplace)) {
			session.createSQLQuery(
					"update xt_campaign set email=:emailIdToUpdate, from_name=:fromName where email=:emailIdToReplace")
					.setParameter("emailIdToUpdate", emailIdToUpdate).setParameter("emailIdToReplace", emailIdToReplace)
					.setParameter("fromName", fromNameToReplace).executeUpdate();
		}

	}

	@SuppressWarnings("unchecked")
	private void updateDamPartnerMappingTable(Integer teamMemberIdToRemove, Integer teamMemberIdToInsert,
			Session session) {
		List<Integer> findDamPartnerIds = session
				.createSQLQuery("select dam_partner_id from xt_dam_partner_mapping where partner_id = :partnerId")
				.setParameter("partnerId", teamMemberIdToRemove).list();
		if (findDamPartnerIds != null && !findDamPartnerIds.isEmpty()) {
			for (Integer damPartnerId : findDamPartnerIds) {
				session.createSQLQuery("delete from xt_dam_partner_mapping where dam_partner_id = " + damPartnerId
						+ " and partner_id = " + teamMemberIdToRemove).executeUpdate();
				boolean isRowExists = (boolean) session.createSQLQuery(
						"select case when count(*)>0 then true else false end from xt_dam_partner_mapping where dam_partner_id = "
								+ damPartnerId + " and partner_id = " + teamMemberIdToInsert)
						.uniqueResult();
				if (!isRowExists) {
					insertIntoDamPartnerMapping(teamMemberIdToInsert, session, damPartnerId);
				}
			}
		}
	}

	private void insertIntoDamPartnerMapping(Integer teamMemberIdToInsert, Session session, Integer damPartnerId) {
		DamPartnerMapping damPartnerMapping = new DamPartnerMapping();
		DamPartner damPartner = new DamPartner();
		damPartner.setId(damPartnerId);
		damPartnerMapping.setDamPartner(damPartner);
		damPartnerMapping.setPartnerId(teamMemberIdToInsert);
		damPartnerMapping.setCreatedTime(new Date());
		session.save(damPartnerMapping);
	}

	@Override
	public void deleteUnMappedRoleIdsByTeamMemberIds(List<Integer> teamMemberIds, Set<Integer> roleIds) {
		if (teamMemberIds != null && !teamMemberIds.isEmpty() && roleIds != null && !roleIds.isEmpty()) {
			Session session = sessionFactory.getCurrentSession();
			Query query = session
					.createSQLQuery("delete from xt_user_role where user_id in (:userIds) and role_id in (:roleIds)");
			query.setParameterList(USER_IDS, teamMemberIds);
			query.setParameterList(ROLE_IDS, roleIds);
			query.executeUpdate();
		}

	}

	@Override
	public boolean roleExists(Integer roleId, Integer userId) {
		String sql = "select case when count(*)>0 then true else false end as role from xt_user_role where user_id=:userId and role_id=:roleId";
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(sql);
		setUserIdAndRoleIdQueryParameters(roleId, userId, query);
		return (boolean) query.uniqueResult();
	}

	private void setUserIdAndRoleIdQueryParameters(Integer roleId, Integer userId, Query query) {
		query.setParameter(USER_ID, userId);
		query.setParameter(ROLE_ID, roleId);
	}

	@Override
	public void addNewRole(Integer teamMemberId, Integer roleId) {
		if (teamMemberId != null && teamMemberId > 0 && roleId != null && roleId > 0) {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createSQLQuery("insert into  xt_user_role(user_id,role_id) values(:userId,:roleId)");
			setUserIdAndRoleIdQueryParameters(roleId, teamMemberId, query);
			query.executeUpdate();

		}

	}

	@Override
	public boolean isTeamMember(Integer teamMemberId) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery("select count(*) from xt_team_member where team_member_id=:teamMemberId");
		query.setParameter(TEAM_MEMBER_ID, teamMemberId);
		int count = query.uniqueResult() != null ? ((BigInteger) query.uniqueResult()).intValue() : 0;
		return count > 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TeamMember> findAllByCompanyId(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createQuery("from TeamMember where companyId= :companyId");
		query.setParameter(COMPANY_ID, companyId);
		return query.list();
	}

	@Override
	public Integer getOrgAdminIdByTeamMemberId(Integer teamMemberId) {
		/*** Updated On 20/06/2024 By Sravan *****/
		Session session = sessionFactory.getCurrentSession();
		String sql = "select company_id from xt_team_member where team_member_id=" + teamMemberId;
		Query query = session.createSQLQuery(sql);
		Integer companyId = (Integer) query.uniqueResult();
		if (XamplifyUtils.isValidInteger(companyId)) {
			return findPrimaryAdminIdByCompanyId(companyId);
		} else {
			return null;
		}

	}

	@Override
	public Integer getPrimaryKeyId(Integer teamMemberId) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery("select id from xt_team_member where team_member_id = :teamMemberId");
		query.setParameter(TEAM_MEMBER_ID, teamMemberId);
		return query.uniqueResult() != null ? (int) (query.uniqueResult()) : 0;
	}

	@Override
	public Integer getTeamMemberSuperiorId(Integer teamMemberId) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session
				.createSQLQuery("select org_admin_id from xt_team_member where team_member_id = :teamMemberId");
		query.setParameter(TEAM_MEMBER_ID, teamMemberId);
		return query.uniqueResult() != null ? (int) (query.uniqueResult()) : 0;
	}

	@Override
	public TeamMember getByTeamMemberId(Integer teamMemberId) {
		Session session = sessionFactory.getCurrentSession();
		return (TeamMember) session.createCriteria(TeamMember.class).add(Restrictions.eq("teamMember.id", teamMemberId))
				.uniqueResult();
	}

	@Override
	public RoleDTO getSuperiorIdAndRolesByTeamMemberId(Integer teamMemberId) {
		Session session = sessionFactory.getCurrentSession();
		String superiorIdQueryString = "select company_id from xt_team_member where team_member_id=" + teamMemberId;
		Query sqlQuery = session.createSQLQuery(superiorIdQueryString);
		Integer companyId = (Integer) sqlQuery.uniqueResult();
		RoleDTO roleDTO = new RoleDTO();
		if (XamplifyUtils.isValidInteger(companyId)) {
			/*** Added On 20/06/2024 By Sravan *****/
			Integer primaryAdminId = findPrimaryAdminIdByCompanyId(companyId);
			if (XamplifyUtils.isValidInteger(primaryAdminId)) {
				List<String> listRolesByUserId = userDao.listRolesByUserId(primaryAdminId);
				roleDTO.setSuperiorId(primaryAdminId);
				roleDTO.setRoles(listRolesByUserId);
			}

		}
		return roleDTO;
	}

	@Override
	public void apporveTeamMember(Integer teamMemberId) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(
				"update xt_team_member set status = 'APPROVE' where team_member_id=" + teamMemberId + "");
		query.executeUpdate();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getAllTeamMemberIdsByOrgAdmin(Integer orgAdminId) {
		Session session = sessionFactory.getCurrentSession();
		return session.createSQLQuery("select team_member_id from  xt_team_member where org_admin_id=" + orgAdminId
				+ " and  team_member_id !=" + orgAdminId).list();
	}

	@Override
	public boolean getModuleAccess(Integer userId) {

		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery("\r\n"
				+ " select count(*) from xt_user_role r,xt_team_member t where r.user_id=:teamMemberId and r.role_id=5 and  t.team_member_id=:teamMemberId and t.status ='APPROVE'");
		query.setParameter(TEAM_MEMBER_ID, userId);
		int count = query.uniqueResult() != null ? ((BigInteger) query.uniqueResult()).intValue() : 0;
		return count > 0;

	}

	@Override
	public Integer getSuperiorId(Integer userId) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session
				.createSQLQuery(" select t.org_admin_id from xt_user_role r,xt_team_member t  where r.user_id=" + userId
						+ " and r.role_id=5 and  t.team_member_id=" + userId + " and t.status ='APPROVE' LIMIT 1");
		return query.uniqueResult() != null ? (int) (query.uniqueResult()) : 0;

	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Integer> findSuperVisorsAndTeamMembers(Integer teamMemberId) {
		List<Integer> teamMemberIds = new ArrayList<>();
		Session session = sessionFactory.getCurrentSession();
		String sqlString = " select distinct u.user_id as \"userId\",string_agg(distinct cast((ur.role_id) as text), ',') as \"rolesInString\" "
				+ " from xt_user_profile u,xt_user_role ur,xt_team_member t where u.user_id = ur.user_id and t.team_member_id = u.user_id and u.company_id = (select company_id from xt_user_profile where user_id = :userId) "
				+ " and u.company_id = t.company_id group by u.user_id order by u.user_id";
		List<TeamMemberIdAndRolesDTO> teamMembers = session.createSQLQuery(sqlString)
				.setParameter(USER_ID, teamMemberId)
				.setResultTransformer(Transformers.aliasToBean(TeamMemberIdAndRolesDTO.class)).list();

		return teamMemberIds;
	}

	@Override
	public List<Integer> listSecondOrgAdminAndSuperVisorsByCompanyId(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		List<Integer> roleIds = new ArrayList<>();
		roleIds.add(Role.ALL_ROLES.getRoleId());
		return returnIds(companyId, session, roleIds);

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> listAllTeamMembersByCompanyId(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		return session.createSQLQuery(
				"select distinct t.team_member_id from xt_team_member t,xt_user_role ur where t.company_id = :companyId"
						+ " and t.team_member_id = ur.user_id")
				.setParameter(COMPANY_ID, companyId).list();

	}

	@Override
	public List<Integer> listAllSuperVisorsByCompanyId(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		List<Integer> roleIds = new ArrayList<>();
		roleIds.add(Role.ALL_ROLES.getRoleId());
		return returnIds(companyId, session, roleIds);

	}

	@SuppressWarnings("unchecked")
	private List<Integer> returnIds(Integer companyId, Session session, List<Integer> roleIds) {
		return session.createSQLQuery(
				"select distinct t.team_member_id from xt_team_member t,xt_user_role ur where t.company_id = :companyId"
						+ " and ur.role_id  in (:roleIds) and t.team_member_id = ur.user_id")
				.setParameter(COMPANY_ID, companyId).setParameterList(ROLE_IDS, roleIds).list();
	}

	@Override
	public TeamMemberListDTO findById(Integer id) {
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(findTeamMemberById);
		query.setParameter(TEAM_MEMBER_ID, id);
		return (TeamMemberListDTO) query.setResultTransformer(Transformers.aliasToBean(TeamMemberListDTO.class))
				.uniqueResult();
	}

	@Override
	public Integer getApprovedTeamMembersCount(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = " select count(*) from xt_team_member where status = 'APPROVE' and company_id  = "
				+ companyId;
		Query query = session.createSQLQuery(queryString);
		return query.uniqueResult() != null ? ((BigInteger) query.uniqueResult()).intValue() : 0;
	}

	@Override
	public TeamMember getTeamMemberByUserId(Integer userId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(TeamMember.class, "TM");
		criteria.createAlias("TM.teamMember", "U", JoinType.LEFT_OUTER_JOIN);
		criteria.add(Restrictions.eq("U.userId", userId));
		return (TeamMember) criteria.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CompanyAndRolesDTO> findCompanyDetailsAndRoles() {
		Session session = sessionFactory.getCurrentSession();
		String queryString = " select distinct c.company_id as \"companyId\",trim(c.company_name) as \"companyName\",TRIM(c.company_profile_name) as \"companyProfileName\", "
				+ " string_agg(distinct cast(ur.role_id as text), ',') as \"rolesInString\",u.user_id as \"userId\",u.email_id as \"emailId\", c.about_us as \"aboutAs\", c.website as \"website\" from xt_company_profile c,xt_user_role ur,xt_user_profile u left join xt_team_member t on t.team_member_id = u.user_id "
				+ " where c.company_id = u.company_id and u.user_id = ur.user_id and c.company_profile_name is not null and role_id in (2,12,13,19,20,18) group by c.company_id,u.user_id having  CAST(count(t.id)AS integer) = 0 order by trim(c.company_name) asc ";
		Query query = session.createSQLQuery(queryString);
		return query.setResultTransformer(Transformers.aliasToBean(CompanyAndRolesDTO.class)).list();
	}

	@Override
	public CompanyAndRolesDTO findCompanyDetailsAndUserId(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = " select distinct c.company_id as \"companyId\",trim(c.company_name) as \"companyName\",c.company_profile_name as \"companyProfileName\", "
				+ " string_agg(distinct cast(ur.role_id as text), ',') as \"rolesInString\",u.user_id as \"userId\" from xt_company_profile c,xt_user_role ur,xt_user_profile u left join xt_team_member t on t.team_member_id = u.user_id "
				+ " where c.company_id = u.company_id and u.user_id = ur.user_id and role_id in (2,12,13,19,20) group by c.company_id,u.user_id having  CAST(count(t.id)AS integer) = 0 and c.company_id = :companyId";
		Query query = session.createSQLQuery(queryString);
		query.setParameter(COMPANY_ID, companyId);
		return (CompanyAndRolesDTO) query.setResultTransformer(Transformers.aliasToBean(CompanyAndRolesDTO.class))
				.uniqueResult();

	}

	@Override
	public Map<String, Object> findTeamMemberDetailsByTeamMemberGroupId(Pagination pagination) {
		Map<String, Object> map = new HashMap<>();
		String finalQueryString = "";
		String sortQueryString = "";
		/******* XNFR-108 *********/
		if (!StringUtils.hasText(pagination.getSortcolumn()) && "form".equals(pagination.getType())
				&& pagination.getFiltertedEmailTempalteIds() != null
				&& !pagination.getFiltertedEmailTempalteIds().isEmpty()) {
			sortQueryString = orderByArrayOrCreatedTime(pagination, sortQueryString);
		} else {
			sortQueryString = orderByfindAllTeamMembers.replace(")", "");
		}
		/******* XNFR-108 ENDS *********/
		String searchKey = pagination.getSearchKey();
		boolean hasSearchKey = StringUtils.hasText(searchKey);
		if (hasSearchKey) {
			finalQueryString = findTeamMemberDetailsByGroupId + " "
					+ searchfindTeamMemberDetailsByGroupId.replace(SERACH_KEY, searchKey) + " " + sortQueryString;
		} else {
			finalQueryString = findTeamMemberDetailsByGroupId + " " + sortQueryString;
		}
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(finalQueryString);
		query.setParameter("teamMemberGroupId", pagination.getCategoryId());
		return paginationUtil.setScrollableAndGetList(pagination, map, query, AdminAndTeamMemberDetailsDTO.class);
	}

	/******* XNFR-108 *********/

	private String orderByArrayOrCreatedTime(Pagination pagination, String sortOptionQueryString) {
		sortOptionQueryString += "   order by array_position(array" + pagination.getFiltertedEmailTempalteIds()
				+ ", tgum.id),u.email_id NULLS FIRST";
		return sortOptionQueryString;
	}

	/******* XNFR-108 ENDS *********/

	@Override
	public Integer getAssignedPartnersCount(Integer userId) {
		String sqlString = " select cast(count(distinct ptgm.id) as int) as \"partnersCount\"  from xt_user_profile u,xt_team_member t left join xt_team_member_group_user_mapping tgum on t.id = tgum.team_member_id "
				+ " left join xt_team_member_group tg on tg.id = tgum.team_member_group_id left join  xt_partner_team_group_mapping ptgm on ptgm.team_member_group_user_mapping_id =  tgum.id  where t.team_member_id = u.user_id  and t.team_member_id = :userId  "
				+ " group by u.user_id,t.id,tg.id";
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(sqlString);
		query.setParameter(USER_ID, userId);
		return query.uniqueResult() != null ? (int) (query.uniqueResult()) : 0;
	}

	@Override
	public AdminAndTeamMemberDetailsDTO getTeamMemberPartnerMasterListName(Integer teamMemberGroupMappingId) {
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(findTeamMemberPartnerMasterListNameQuery);
		query.setParameter("teamMemberGroupMappingId", teamMemberGroupMappingId);
		AdminAndTeamMemberDetailsDTO adminAndTeamMemberDetailsDTO = (AdminAndTeamMemberDetailsDTO) query
				.setResultTransformer(Transformers.aliasToBean(AdminAndTeamMemberDetailsDTO.class)).uniqueResult();
		return XamplifyUtils.getTeamMemberMasterPartnerListInfo(adminAndTeamMemberDetailsDTO);
	}

	@Override
	public Map<String, Object> findPartners(Pagination pagination) {
		try {
			HashMap<String, Object> map = new HashMap<>();
			String finalQueryString = "";
			String searchKey = pagination.getSearchKey();
			String sortQueryString = getSortQuery(pagination);
			boolean hasSearchKey = StringUtils.hasText(searchKey);
			if (hasSearchKey) {
				finalQueryString = teamMemberPartnersQuery + " "
						+ teamMemberPartnersSearchQuery.replace(SERACH_KEY, searchKey) + " " + sortQueryString;
			} else {
				finalQueryString = teamMemberPartnersQuery + " " + sortQueryString;
			}
			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(finalQueryString);
			query.setParameter(TEAM_MEMBER_ID, pagination.getUserId());
			return paginationUtil.setScrollableAndGetList(pagination, map, query, TeamMemberPartnersDTO.class);
		} catch (HibernateException | TeamMemberDataAccessException u) {
			throw new TeamMemberDataAccessException(u);
		} catch (Exception ex) {
			throw new TeamMemberDataAccessException(ex);
		}

	}

	private String getSortQuery(Pagination pagination) {
		List<SortColumnDTO> sortColumnDTOs = new ArrayList<>();
		SortColumnDTO sortByName = new SortColumnDTO("emailId", "pu.email_id", true, true, false);
		SortColumnDTO sortByCreatedTime = new SortColumnDTO("firstName", "uul.firstname", false, true, false);
		SortColumnDTO sortByUpdatedTime = new SortColumnDTO("lastName", "uul.lastname", false, true, false);
		SortColumnDTO companyName = new SortColumnDTO("companyName", "uul.contact_company", false, true, false);
		SortColumnDTO createdTime = new SortColumnDTO("createdTime", "ptgm.created_time", false, false, false);
		sortColumnDTOs.add(sortByName);
		sortColumnDTOs.add(sortByCreatedTime);
		sortColumnDTOs.add(sortByUpdatedTime);
		sortColumnDTOs.add(companyName);
		sortColumnDTOs.add(createdTime);
		return paginationUtil.generateSortQuery(pagination, sortColumnDTOs, "asc");
	}

	@Override
	public TeamMemberAndPartnerIdsAndUserListIdDTO findTeamMemberIdAndPartnerIds(
			List<Integer> partnerTeamGroupMappingIds) {
		try {
			if (partnerTeamGroupMappingIds != null && !partnerTeamGroupMappingIds.isEmpty()) {
				SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(teamMemberIdAndPartnerIdsQuery);
				query.setParameterList("partnerTeamGroupMappingIds", partnerTeamGroupMappingIds);
				return (TeamMemberAndPartnerIdsAndUserListIdDTO) query
						.setResultTransformer(Transformers.aliasToBean(TeamMemberAndPartnerIdsAndUserListIdDTO.class))
						.uniqueResult();
			} else {
				return null;
			}
		} catch (HibernateException | TeamMemberDataAccessException u) {
			throw new TeamMemberDataAccessException(u);
		} catch (Exception ex) {
			throw new TeamMemberDataAccessException(ex);
		}

	}

	@Override
	public void deleteTeamMemberPartners(Integer userListId, List<Integer> partnerIds) {
		try {
			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(deleteTeamMemberPartnersQuery);
			query.setParameterList("partnerIds", partnerIds);
			query.setParameter("userListId", userListId);
			query.executeUpdate();
		} catch (HibernateException | TeamMemberDataAccessException u) {
			throw new TeamMemberDataAccessException(u);
		} catch (Exception ex) {
			throw new TeamMemberDataAccessException(ex);
		}

	}

	@Override
	public void deletePartnerTeamGroupMappingIds(List<Integer> partnerTeamGroupMappingIds) {
		try {
			SQLQuery query = sessionFactory.getCurrentSession()
					.createSQLQuery("delete from xt_partner_team_group_mapping where id in (:ids)");
			query.setParameterList("ids", partnerTeamGroupMappingIds);
			query.executeUpdate();
		} catch (HibernateException | TeamMemberDataAccessException u) {
			throw new TeamMemberDataAccessException(u);
		} catch (Exception ex) {
			throw new TeamMemberDataAccessException(ex);
		}

	}

	@Override
	public void saveTeamMemberPartnerList(TeamMember teamMember, User teamMemberUser, boolean newPartnerGroupRequired) {
		try {
			Session session = sessionFactory.getCurrentSession();
			saveTeamMemberPartnerList(session, teamMember, teamMemberUser, newPartnerGroupRequired);
		} catch (HibernateException | TeamMemberDataAccessException e) {
			throw new TeamMemberDataAccessException(e.getMessage());
		} catch (Exception ex) {
			throw new TeamMemberDataAccessException(ex.getMessage());
		}
	}

	@Override
	public void saveTeamMemberUserUserListsAndUpdateTeamMemberPartnerList(List<UserUserList> teamMemberPartners,
			UserList userList, User teamMemberUser, TeamMember teamMember) {
		try {
			Session session = sessionFactory.getCurrentSession();
			getUpdatedUserListNameAndInsertIntoUserUserList(session, teamMember, teamMemberUser, userList,
					teamMemberPartners, false, false);
		} catch (HibernateException | TeamMemberDataAccessException e) {
			throw new TeamMemberDataAccessException(e.getMessage());
		} catch (Exception ex) {
			throw new TeamMemberDataAccessException(ex.getMessage());
		}
	}

	@Override
	public Integer getSecondAdminsCountByCompanyId(Integer companyId) {
		String sqlString = "select cast(count(id) as int) from xt_team_member where company_id = :companyId and is_second_admin";
		Session session = sessionFactory.getCurrentSession();
		SQLQuery query = session.createSQLQuery(sqlString);
		query.setParameter(COMPANY_ID, companyId);
		return (Integer) query.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findSecondAdminTeamMemberIds(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select id from xt_team_member where company_id = :companyId and is_second_admin";
		return session.createSQLQuery(queryString).setParameter(COMPANY_ID, companyId).list();

	}

	@Override
	public AnalyticsCountDTO findMaxAdminAnalyticsByCompanyId(Integer companyId) {
		AnalyticsCountDTO analyticsCountDTO = new AnalyticsCountDTO();
		Integer maxAdminsByCompanyId = 0;
		Integer availedAdminsCount = 0;
		Integer availableAdminsCount = 0;
		maxAdminsByCompanyId = findMaxAdminsCountByCompanyId(companyId);
		availedAdminsCount = getSecondAdminsCountByCompanyId(companyId) + 1;
		availableAdminsCount = maxAdminsByCompanyId - availedAdminsCount;
		analyticsCountDTO.setTotal(maxAdminsByCompanyId);
		analyticsCountDTO.setAvailed(availedAdminsCount);
		analyticsCountDTO.setAvailable(availableAdminsCount);
		return analyticsCountDTO;
	}

	private Integer findMaxAdminsCountByCompanyId(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String sqlString = "select max_admins from xt_module_access where company_id = :companyId";
		Query query = session.createSQLQuery(sqlString);
		query.setParameter("companyId", companyId);
		return (Integer) query.uniqueResult();
	}

	@Override
	public Integer findPrimaryAdminIdByCompanyId(Integer companyId) {
		String sqlString = "select distinct u.user_id from xt_company_profile c,xt_user_role ur,xt_user_profile u left join xt_team_member t on t.team_member_id = u.user_id \r\n"
				+ " where c.company_id = u.company_id and u.user_id = ur.user_id and role_id in (:roleIds) group by c.company_id,u.user_id having  CAST(count(t.id)AS integer) = 0 and c.company_id = :companyId";
		Session session = sessionFactory.getCurrentSession();
		SQLQuery query = session.createSQLQuery(sqlString);
		query.setParameterList(ROLE_IDS, Role.getAllAdminRoleIds());
		query.setParameter(COMPANY_ID, companyId);
		return (Integer) query.uniqueResult();
	}

	@Override
	public Integer findPrimaryAdminIdByCompanyProfileName(String companyProfileName) {
		String sqlString = "select distinct u.user_id from xt_company_profile c,xt_user_role ur,xt_user_profile u left join xt_team_member t on t.team_member_id = u.user_id \r\n"
				+ " where c.company_id = u.company_id and u.user_id = ur.user_id and role_id in (:roleIds) group by c.company_id,u.user_id having  CAST(count(t.id)AS integer) = 0 and LOWER(c.company_profile_name) = LOWER(:companyProfileName)";
		Session session = sessionFactory.getCurrentSession();
		SQLQuery query = session.createSQLQuery(sqlString);
		query.setParameterList(ROLE_IDS, Role.getAllAdminRoleIds());
		query.setParameter("companyProfileName", getPrmCompanyProfileName());
		return (Integer) query.uniqueResult();
	}

	public String getPrmCompanyProfileName() {
		Session session = sessionFactory.getCurrentSession();
		String sqlString = "select LOWER(TRIM(cp.company_profile_name)) from xt_company_profile cp,xt_user_profile up,xt_user_role ur\r\n"
				+ "where up.company_id = cp.company_id and up.user_id = ur.user_id and ur.role_id = "
				+ Role.PRM_ROLE.getRoleId();
		Query query = session.createSQLQuery(sqlString);
		return (String) query.uniqueResult();
	}

	@Override
	public void updatePrimaryAdminId(Integer existingPrimaryAdminId, Integer newPrimaryAdminId) {
		Session session = sessionFactory.getCurrentSession();

		updateTeamMemberPartnerListName(existingPrimaryAdminId, newPrimaryAdminId, session);

		updateOrgAdminIdColumn(existingPrimaryAdminId, newPrimaryAdminId, session);

		updateTeamMemberIdColumn(existingPrimaryAdminId, newPrimaryAdminId, session);

		updateTeamMemberGroupUserMappingCreatedUserIdColumn(existingPrimaryAdminId, newPrimaryAdminId, session);

		updateTeamMemberGroupUserMappingUpdatedUserIdColumn(existingPrimaryAdminId, newPrimaryAdminId, session);

		List<Integer> existingPrimaryAdminRoleIds = userDao.getRoleIdsByUserId(existingPrimaryAdminId);

		List<Integer> newPrimaryAdminRoleIds = userDao.getRoleIdsByUserId(newPrimaryAdminId);

		deleteExistingRoles(existingPrimaryAdminId, newPrimaryAdminId, session);

		insertNewRoles(existingPrimaryAdminId, newPrimaryAdminId, session, existingPrimaryAdminRoleIds,
				newPrimaryAdminRoleIds);

		updateAdminIdColumn(existingPrimaryAdminId, newPrimaryAdminId, session);

		/** XNFR-821 **/
		autoApprovePendingAssets(existingPrimaryAdminId, newPrimaryAdminId, session);

		autoApprovePendingTracksAndPlaybooks(existingPrimaryAdminId, newPrimaryAdminId, session);

		updateInApprovalStatusTimeLineHistory(existingPrimaryAdminId, newPrimaryAdminId, session);

		updateDashboardLayoutAdmin(existingPrimaryAdminId, newPrimaryAdminId, session);

	}

	private void updateTeamMemberPartnerListName(Integer existingPrimaryAdminId, Integer newPrimaryAdminId,
			Session session) {
		User existingAdminUser = userDao.getFirstNameLastNameAndEmailIdByUserId(existingPrimaryAdminId);

		String teamMemberIdQueryString = "select id from xt_team_member where team_member_id = :teamMemberId";
		SQLQuery idQuery = session.createSQLQuery(teamMemberIdQueryString);
		idQuery.setParameter(TEAM_MEMBER_ID, newPrimaryAdminId);
		Integer teamMemberPkId = (Integer) idQuery.uniqueResult();

		AdminAndTeamMemberDetailsDTO adminAndTeamMemberDetailsDTO = new AdminAndTeamMemberDetailsDTO();
		adminAndTeamMemberDetailsDTO.setEmailId(existingAdminUser.getEmailId());
		String firstName = StringUtils.hasText(existingAdminUser.getFirstName())
				? existingAdminUser.getFirstName().trim()
				: "";
		String lastName = StringUtils.hasText(existingAdminUser.getLastName()) ? existingAdminUser.getLastName().trim()
				: "";
		adminAndTeamMemberDetailsDTO.setFirstName(firstName);
		adminAndTeamMemberDetailsDTO.setLastName(lastName);
		adminAndTeamMemberDetailsDTO.setId(teamMemberPkId);
		AdminAndTeamMemberDetailsDTO teamMemberMasterPartnerListDetails = XamplifyUtils
				.getTeamMemberMasterPartnerListInfo(adminAndTeamMemberDetailsDTO);
		String partnerListName = teamMemberMasterPartnerListDetails.getFullName() + "-Master Partner Group";

		String updateTeamMemberPartnerListNameQueryString = "update xt_user_list set user_list_name = :name where team_member_id = :teamMemberPkId";
		session.createSQLQuery(updateTeamMemberPartnerListNameQueryString)
				.setParameter("teamMemberPkId", teamMemberPkId).setParameter("name", partnerListName).executeUpdate();
	}

	private void insertNewRoles(Integer existingPrimaryAdminId, Integer newPrimaryAdminId, Session session,
			List<Integer> existingPrimaryAdminRoleIds, List<Integer> newPrimaryAdminRoleIds) {
		for (Integer newPrimaryAdminRoleId : newPrimaryAdminRoleIds) {
			insertRoles(existingPrimaryAdminId, session, newPrimaryAdminRoleId);
		}

		for (Integer existingPrimaryAdminRoleId : existingPrimaryAdminRoleIds) {
			insertRoles(newPrimaryAdminId, session, existingPrimaryAdminRoleId);
		}
	}

	private void deleteExistingRoles(Integer existingPrimaryAdminId, Integer newPrimaryAdminId, Session session) {
		String deleteFromUserRoleQueryString = "delete from xt_user_role where user_id in (:userIds)";
		List<Integer> userIds = new ArrayList<>();
		userIds.add(existingPrimaryAdminId);
		userIds.add(newPrimaryAdminId);
		SQLQuery deleteQuery = session.createSQLQuery(deleteFromUserRoleQueryString);
		deleteQuery.setParameterList(USER_IDS, userIds);
		deleteQuery.executeUpdate();
	}

	private void insertRoles(Integer newPrimaryAdminId, Session session, Integer existingPrimaryAdminRoleId) {
		String sqlString = "insert into xt_user_role (user_id,role_id) values(:userId,:roleId)";
		SQLQuery roleQuery = session.createSQLQuery(sqlString);
		roleQuery.setParameter(USER_ID, newPrimaryAdminId);
		roleQuery.setParameter(ROLE_ID, existingPrimaryAdminRoleId);
		roleQuery.executeUpdate();
	}

	private void updateTeamMemberGroupUserMappingUpdatedUserIdColumn(Integer existingPrimaryAdminId,
			Integer newPrimaryAdminId, Session session) {
		SQLQuery updateTeamMemberGroupUserMappingUpdatedUserIdQuery = session
				.createSQLQuery("update xt_team_member_group_user_mapping set updated_user_id = :"
						+ NEW_PRIMARY_ADMIN_ID + " where updated_user_id = :" + EXISTING_PRIMARY_ADMIN_ID);
		replaceExistingPrimaryAdminIdWithNewPrimaryAdminId(existingPrimaryAdminId, newPrimaryAdminId,
				updateTeamMemberGroupUserMappingUpdatedUserIdQuery);
	}

	private void updateTeamMemberGroupUserMappingCreatedUserIdColumn(Integer existingPrimaryAdminId,
			Integer newPrimaryAdminId, Session session) {
		SQLQuery updateTeamMemberGroupUserMappingCreatedUserIdQuery = session
				.createSQLQuery("update xt_team_member_group_user_mapping set created_user_id = :"
						+ NEW_PRIMARY_ADMIN_ID + " where created_user_id = :" + EXISTING_PRIMARY_ADMIN_ID);
		replaceExistingPrimaryAdminIdWithNewPrimaryAdminId(existingPrimaryAdminId, newPrimaryAdminId,
				updateTeamMemberGroupUserMappingCreatedUserIdQuery);
	}

	private void updateTeamMemberIdColumn(Integer existingPrimaryAdminId, Integer newPrimaryAdminId, Session session) {
		SQLQuery updateTeamMemberIdQuery = session.createSQLQuery("update xt_team_member set team_member_id = :"
				+ EXISTING_PRIMARY_ADMIN_ID + " where team_member_id = :" + NEW_PRIMARY_ADMIN_ID);
		replaceExistingPrimaryAdminIdWithNewPrimaryAdminId(existingPrimaryAdminId, newPrimaryAdminId,
				updateTeamMemberIdQuery);
	}

	private void updateOrgAdminIdColumn(Integer existingPrimaryAdminId, Integer newPrimaryAdminId, Session session) {
		SQLQuery updateOrgAdminQuery = session.createSQLQuery("update xt_team_member set org_admin_id = :"
				+ NEW_PRIMARY_ADMIN_ID + " where org_admin_id =:" + EXISTING_PRIMARY_ADMIN_ID);
		replaceExistingPrimaryAdminIdWithNewPrimaryAdminId(existingPrimaryAdminId, newPrimaryAdminId,
				updateOrgAdminQuery);
	}

	private void updateAdminIdColumn(Integer existingPrimaryAdminId, Integer newPrimaryAdminId, Session session) {
		SQLQuery updateOrgAdminQuery = session.createSQLQuery("update xt_partner_team_member_view_type set admin_id = :"
				+ NEW_PRIMARY_ADMIN_ID + " where admin_id =:" + EXISTING_PRIMARY_ADMIN_ID);
		replaceExistingPrimaryAdminIdWithNewPrimaryAdminId(existingPrimaryAdminId, newPrimaryAdminId,
				updateOrgAdminQuery);
	}

	/** XNFR-821 **/
	private void autoApprovePendingAssets(Integer existingPrimaryAdminId, Integer newPrimaryAdminId, Session session) {
		SQLQuery approveDamQuery = session
				.createSQLQuery(UPDATE_QUERY + DAM_TABLE_NAME + SET + APPROVAL_STATUS_COLUMN_NAME + "="
						+ APPROVAL_STATUS_TYPE_APPROVED + "," + APPROVAL_STATUS_UPDATED_BY_COLUMN_NAME + "= :"
						+ EXISTING_PRIMARY_ADMIN_ID + CREATED_BY_EQUALS_TO_QUERY_STRING + ":" + NEW_PRIMARY_ADMIN_ID
						+ AND + DISTINCT_FROM_APPROVAL_STATUS_TYPE_APPROVED + AND + APPROVAL_REFERENCE_ID_IS_NULL);
		replaceExistingPrimaryAdminIdWithNewPrimaryAdminId(existingPrimaryAdminId, newPrimaryAdminId, approveDamQuery);
	}

	/** XNFR-821 **/
	private void autoApprovePendingTracksAndPlaybooks(Integer existingPrimaryAdminId, Integer newPrimaryAdminId,
			Session session) {
		SQLQuery approveDamQuery = session
				.createSQLQuery(UPDATE_QUERY + LEARNING_TRACK_TABLE_NAME + SET + APPROVAL_STATUS_COLUMN_NAME + "="
						+ APPROVAL_STATUS_TYPE_APPROVED + "," + APPROVAL_STATUS_UPDATED_BY_COLUMN_NAME + "= :"
						+ EXISTING_PRIMARY_ADMIN_ID + CREATED_BY_EQUALS_TO_QUERY_STRING + ":" + NEW_PRIMARY_ADMIN_ID
						+ AND + DISTINCT_FROM_APPROVAL_STATUS_TYPE_APPROVED);
		replaceExistingPrimaryAdminIdWithNewPrimaryAdminId(existingPrimaryAdminId, newPrimaryAdminId, approveDamQuery);
	}

	/** XNFR-821 **/
	private void updateInApprovalStatusTimeLineHistory(Integer existingPrimaryAdminId, Integer newPrimaryAdminId,
			Session session) {

		SQLQuery updateApprovalStatusHistory = session.createSQLQuery(UPDATE_QUERY + APPROVAL_STATUS_HISTORY_TABLE_NAME
				+ SET + CREATED_BY_COLUMN_NAME + "= :" + EXISTING_PRIMARY_ADMIN_ID + "," + STATUS_COLUMN_NAME + "="
				+ APPROVAL_STATUS_TYPE_APPROVED + CREATED_BY_EQUALS_TO_QUERY_STRING + ":" + NEW_PRIMARY_ADMIN_ID + AND
				+ DISTINCT_FROM_APPROVAL_STATUS_TYPE_APPROVED_FOR_HISTORY);
		replaceExistingPrimaryAdminIdWithNewPrimaryAdminId(existingPrimaryAdminId, newPrimaryAdminId,
				updateApprovalStatusHistory);

	}

	/***** XNFR-860 *****/
	private void updateDashboardLayoutAdmin(Integer existingPrimaryAdminId, Integer newPrimaryAdminId,
			Session session) {
		SQLQuery updateQuery = session.createSQLQuery(UPDATE_QUERY + "xt_dashboard_custom_layout" + SET
				+ "created_user_id = CASE " + " WHEN created_user_id = :" + NEW_PRIMARY_ADMIN_ID + " THEN :"
				+ EXISTING_PRIMARY_ADMIN_ID + " \n WHEN created_user_id = :" + EXISTING_PRIMARY_ADMIN_ID + " THEN :"
				+ NEW_PRIMARY_ADMIN_ID + " \n ELSE created_user_id \n" + " END WHERE created_user_id IN (:"
				+ NEW_PRIMARY_ADMIN_ID + ", :" + EXISTING_PRIMARY_ADMIN_ID + ")");
		replaceExistingPrimaryAdminIdWithNewPrimaryAdminId(existingPrimaryAdminId, newPrimaryAdminId, updateQuery);
	}

	private void replaceExistingPrimaryAdminIdWithNewPrimaryAdminId(Integer existingPrimaryAdminId,
			Integer newPrimaryAdminId, SQLQuery query) {
		query.setParameter(NEW_PRIMARY_ADMIN_ID, newPrimaryAdminId);
		query.setParameter(EXISTING_PRIMARY_ADMIN_ID, existingPrimaryAdminId);
		query.executeUpdate();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TeamMemberListDTO> findPrimaryAdminAndExtraAdmins(Integer companyId) {
		try {
			String sqlString = "select u.user_id as \"teamMemberUserId\", coalesce(u.firstname,'') as \"firstName\",\r\n"
					+ " coalesce(u.lastname,'') as \"lastName\", TRIM(concat(u.firstname, ' ', u.lastname)) as \"fullName\",u.email_id as \"emailId\",CAST(count(t.id)AS integer) = 0 as \"primaryAdmin\"\r\n"
					+ " from xt_user_profile u,xt_team_member t\r\n"
					+ " where t.company_id = :companyId and t.team_member_id = u.user_id  and u.company_id = t.company_id\r\n"
					+ " and t.is_second_admin \r\n" + " group by u.user_id,t.id  \r\n" + " UNION\r\n"
					+ " select u.user_id as \"teamMemberUserId\", coalesce(u.firstname,'') as \"firstName\",\r\n"
					+ " coalesce(u.lastname,'') as \"lastName\",TRIM(concat(u.firstname, ' ', u.lastname)) as \"fullName\", u.email_id as \"emailId\",\r\n"
					+ " CAST(count(t.id)AS integer) = 0 as \"primaryAdmin\" from xt_company_profile c,xt_user_role ur,xt_user_profile u \r\n"
					+ " left join xt_team_member t on t.team_member_id = u.user_id \r\n"
					+ " where c.company_id = u.company_id and u.user_id = ur.user_id and role_id in (:roleIds) group by c.company_id,u.user_id \r\n"
					+ " having  CAST(count(t.id)AS integer) = 0 and c.company_id = :companyId order by \"primaryAdmin\" desc ";
			Session session = sessionFactory.getCurrentSession();
			SQLQuery query = session.createSQLQuery(sqlString);
			query.setParameterList(ROLE_IDS, Role.getAllAdminRoleIds());
			query.setParameter(COMPANY_ID, companyId);
			return (List<TeamMemberListDTO>) paginationUtil.getListDTO(TeamMemberListDTO.class, query);
		} catch (HibernateException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex);
		}
	}

	@Override
	public Integer getTeamMemberGroupIdById(Integer teamMemberGroupUserMappingId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select team_member_group_id from xt_team_member_group_user_mapping where id=:teamMemberGroupUserMappingId";
		Query query = session.createSQLQuery(sql).setParameter("teamMemberGroupUserMappingId",
				teamMemberGroupUserMappingId);
		return (Integer) query.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findPartnershipIdsFromPartnerTeamGroupMapping() {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery("select distinct partnership_id from xt_partner_team_group_mapping");
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ViewTypePatchRequestDTO> findTeamMemberIdsAndUserIdsByCompanyId(Integer companyId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(
				"select distinct u.user_id as \"userId\",t.id as \"teamMemberId\",cast(modules_display_type as text) as \"viewType\" from xt_company_profile c,xt_user_profile u left join xt_team_member t on t.team_member_id = u.user_id where c.company_id = u.company_id and c.company_id  =:companyId and t.id is not null group by u.user_id,t.id\r\n"
						+ "");
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(COMPANY_ID, companyId));
		hibernateSQLQueryResultRequestDTO.setClassInstance(ViewTypePatchRequestDTO.class);
		return (List<ViewTypePatchRequestDTO>) hibernateSqlQueryResultDao
				.returnDTOList(hibernateSQLQueryResultRequestDTO);
	}

	/** XNFR-603, XNFR-605 **/
	@Override
	public TeamMemberDTO getTeamMemberStatus(Integer teamMemberId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(
				"select status as \"userStatus\" from xt_team_member where team_member_id = :teamMemberId");
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(TEAM_MEMBER_ID, teamMemberId));
		return (TeamMemberDTO) hibernateSqlQueryResultDao.getDto(hibernateSQLQueryResultRequestDTO,
				TeamMemberDTO.class);
	}

	@Override
	public Integer findOrgAdminIdByTeamMemberId(Integer teamMemberId) {
		/*** Written On 09/10/2024 By Lakshman *****/
		Session session = sessionFactory.getCurrentSession();
		String sql = "select org_admin_id from xt_team_member where team_member_id=" + teamMemberId;
		Query query = session.createSQLQuery(sql);
		return (Integer) query.uniqueResult();
	}

	private String frameDateQueryStringForTeamMembers(Pagination pagination, String dateColumn) {
		String dateFilterQueryString = "";
		if (pagination.getFromDateFilter() != null && pagination.getToDateFilter() != null) {
			dateFilterQueryString = " and " + dateColumn + " between  TO_TIMESTAMP('" + pagination.getFromDateFilter()
					+ "', 'Dy Mon DD HH24:MI:SS ZZZ YYYY') and TO_TIMESTAMP('" + pagination.getToDateFilter()
					+ "', 'Dy Mon DD HH24:MI:SS ZZZ YYYY') ";
		}
		return dateFilterQueryString;
	}

	@Override
	public boolean getTeamMemberOption(Integer teamMemberId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO
				.setQueryString("select is_partner_filter from xt_team_member where team_member_id =:teamMemberId\n");
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(TEAM_MEMBER_ID, teamMemberId));
		return (boolean) hibernateSqlQueryResultDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
	}

	public EmailTemplateDTO fetchCustomTemplate(Integer id, Integer loggedInUserId) {
		String customQuery = "SELECT xcdt.html_body AS \"body\", xcdt.subject AS \"subject\", xcp.company_logo  "
				+ "as " + " \"companyLogoPath\", xcp.website as \"vendorOrganizationName\" "
				+ "FROM xt_custom_default_templates xcdt JOIN xt_user_profile xup ON xcdt.company_id = xup.company_id "
				+ "JOIN xt_company_profile xcp ON xup.company_id = xcp.company_id "
				+ "WHERE xcdt.default_email_template_id = :id " + "AND xup.user_id = :loggedInUserId";
		HibernateSQLQueryResultRequestDTO requestDTO = new HibernateSQLQueryResultRequestDTO();
		requestDTO.setQueryString(customQuery);
		requestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("id", id));
		requestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("loggedInUserId", loggedInUserId));
		return (EmailTemplateDTO) hibernateSqlQueryResultDao.getDto(requestDTO, EmailTemplateDTO.class);
	}

	public EmailTemplateDTO fetchDefaultTemplate(Integer id, Integer loggedInUserId) {
		String defaultQuery = "SELECT det.html_body AS \"body\", det.subject AS \"subject\", "
				+ "cp.company_logo as \"companyLogoPath\", cp.website as \"vendorOrganizationName\" "
				+ "FROM xt_default_email_templates det JOIN xt_company_profile cp "
				+ "ON cp.company_id = (SELECT up.company_id FROM xt_user_profile up "
				+ "WHERE up.user_id = :loggedInUserId) WHERE det.id = :id";
		HibernateSQLQueryResultRequestDTO defaultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		defaultRequestDTO.setQueryString(defaultQuery);
		defaultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("id", id));
		defaultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("loggedInUserId", loggedInUserId));
		return (EmailTemplateDTO) hibernateSqlQueryResultDao.getDto(defaultRequestDTO, EmailTemplateDTO.class);
	}

	@Override
	public UserDetailsUtilDTO findPrimaryAdminDetailsByCompanyId(Integer companyId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String queryString = "select u.user_id as \"id\", u.email_id as \"emailId\", c.company_profile_name as \"companyName\" from xt_company_profile c,xt_user_role ur,xt_user_profile u left join xt_team_member t on t.team_member_id = u.user_id "
				+ " where c.company_id = u.company_id and u.user_id = ur.user_id and role_id in (:roleIds) group by c.company_id,u.user_id having  CAST(count(t.id)AS integer) = 0 and c.company_id = :companyId";
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(COMPANY_ID, companyId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs()
				.add(new QueryParameterListDTO(ROLE_IDS, Role.getAllAdminRoleIds()));
		return (UserDetailsUtilDTO) hibernateSqlQueryResultDao.getDto(hibernateSQLQueryResultRequestDTO,
				UserDetailsUtilDTO.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<UserDTO> findTeamMemberIdsByCompanyId(Integer companyId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String queryString = "SELECT id AS \"id\", team_member_id AS \"userId\" \n"
				+ "FROM xt_team_member WHERE company_id = :companyId ORDER BY team_member_id DESC";
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(COMPANY_ID, companyId));
		hibernateSQLQueryResultRequestDTO.setClassInstance(ViewTypePatchRequestDTO.class);
		return (List<UserDTO>) hibernateSqlQueryResultDao.getListDto(hibernateSQLQueryResultRequestDTO, UserDTO.class);
	}

	/*** XNFR-1022 ****/
	@SuppressWarnings("unchecked")
	@Override
	public String findTeamMemberFullNameOrEmaiIdByPartnerCompanyId(Integer partnerCompanyId, Integer vendorCompanyId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String queryString = "SELECT DISTINCT case when xup.firstname is null and xup.lastname is null then xup.email_id "
					+ " else concat(xup.firstname, ' ' , xup.lastname) end " + "      FROM xt_team_member t"
					+ "      LEFT OUTER JOIN xt_team_member_group_user_mapping tgum ON t.id = tgum.team_member_id  "
					+ "      LEFT OUTER JOIN xt_partner_team_group_mapping ptgm ON tgum.id = ptgm.team_member_group_user_mapping_id "
					+ "      LEFT OUTER JOIN xt_partnership p ON ptgm.partnership_id = p.id "
					+ "      left join xt_user_profile xup on xup.user_id = t.team_member_id "
//					+ "      left join xt_user_profile xup1 on xup1.user_id= p.partner_id "
					+ "      WHERE p.partner_company_id = :partnerCompanyId "
					+ "and p.vendor_company_id= :vendorCompanyId";
			Query query = session.createSQLQuery(queryString);
			query.setParameter("partnerCompanyId", partnerCompanyId);
			query.setParameter("vendorCompanyId", vendorCompanyId);
			List<String> teamMemberNames = query.list();
			if (XamplifyUtils.isNotEmptyList(teamMemberNames)) {
				return String.join(", ", teamMemberNames);
			} else {
				return NO_MEMBERS_FOUND_INDICATOR;
			}
		} catch (HibernateException | TeamMemberDataAccessException e) {
			throw new TeamMemberDataAccessException(e.getMessage());
		} catch (Exception ex) {
			throw new TeamMemberDataAccessException(ex.getMessage());
		}
	}

	@Override
	public Integer findTeamMemberGroupIdByAlias(String groupAlias) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String queryString = "SELECT id FROM xt_team_member_group WHERE alias = :alias";
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("alias", groupAlias));
		hibernateSQLQueryResultRequestDTO.setClassInstance(ViewTypePatchRequestDTO.class);
		return (Integer) hibernateSqlQueryResultDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public Integer findChannelAccountManagerIdByCompanyId(Integer companyId) {
		List<String> groupList = Arrays.asList("PRM Account Manager");
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String queryString = "SELECT id FROM xt_team_member_group WHERE company_id = :companyId AND name in (:name)";
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs().add(new QueryParameterListDTO("name", groupList));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(COMPANY_ID, companyId));
		hibernateSQLQueryResultRequestDTO.setClassInstance(ViewTypePatchRequestDTO.class);
		return (Integer) hibernateSqlQueryResultDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public boolean hasMarketingModulesAccessToTeamMember(Integer loggedInUserId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String queryString = "select xtg.is_marketing_modules_enabled from xt_team_member_group xtg \n"
				+ "left join xt_team_member_group_user_mapping xtgum on xtgum.team_member_group_id = xtg.id \n"
				+ "left join xt_team_member xtm on xtm.id = xtgum.team_member_id \n"
				+ "where xtm.team_member_id = :userId";
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(USER_ID, loggedInUserId));
		Object object = hibernateSqlQueryResultDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
		return Boolean.TRUE.equals(object);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findPartnerTeamGroupMappingIdsByPartnershipIds(List<Integer> partnershipIds,
			Integer teamMemberId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String sql = "SELECT xptgm.id FROM xt_partner_team_group_mapping AS xptgm "
				+ " JOIN xt_team_member_group_user_mapping AS xtmgum "
				+ " ON xptgm.team_member_group_user_mapping_id = xtmgum.id "
				+ " WHERE xptgm.partnership_id IN (:partnershipIds) " + " AND xtmgum.team_member_id = :teamMemberId ";
		hibernateSQLQueryResultRequestDTO.setQueryString(sql);
		hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs()
				.add(new QueryParameterListDTO("partnershipIds", partnershipIds));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("teamMemberId", teamMemberId));

		return (List<Integer>) hibernateSqlQueryResultDao.returnList(hibernateSQLQueryResultRequestDTO);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TeamMemberListDTO> fetchTeamMemberDetailsForGorupOfPartners(Integer userListId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String queryString = "SELECT distinct up1.email_id as \"emailId\", up1.firstname as \"firstName\", up1.lastname as \"lastName\", initcap(cast(up1.status as text)) as \"status\", "
				+ "up1.mobile_number as \"mobileNumber\", xcp.company_name as \"companyName\" FROM xt_user_list ul "
				+ "JOIN xt_user_userlist uul ON ul.user_list_id = uul.user_list_id "
				+ "left join xt_partnership p on p.partner_id= uul.user_id "
				+ " join xt_team_member xtm on p.partner_id= xtm.org_admin_id "
				+ "JOIN xt_user_profile up1 on xtm.team_member_id = up1.user_id "
				+ "join xt_company_profile xcp on xcp.company_id= up1.company_id "
				+ "WHERE ul.user_list_id = :userListId union "
				+ "SELECT distinct up1.email_id as \"emailId\", up1.firstname as \"firstName\", up1.lastname as \"lastName\", initcap(cast(up1.status as text)) as \"status\",  "
				+ " up1.mobile_number as \"mobileNumber\", xcp.company_name as \"companyName\" FROM xt_user_list ul "
				+ "JOIN xt_user_userlist uul ON ul.user_list_id = uul.user_list_id "
				+ "JOIN xt_user_profile up1 on uul.user_id = up1.user_id "
				+ "join xt_company_profile xcp on xcp.company_id= up1.company_id "
				+ "WHERE ul.user_list_id = :userListId";
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("userListId", userListId));
		return (List<TeamMemberListDTO>) hibernateSqlQueryResultDao.getListDto(hibernateSQLQueryResultRequestDTO,
				TeamMemberListDTO.class);
	}

	/** XNFR-1121 **/
	@Override
	public Integer findTeamMemberGroupUserMappingIdByEmailId(String emailId, Integer companyId) {
		Integer teamMemberGroupUserMappingId = null;
		if (XamplifyUtils.isValidString(emailId) && XamplifyUtils.isValidInteger(companyId)) {
			try {
				String sqlQueryString = "select distinct tgum.id " + "from xt_user_profile u "
						+ "join xt_team_member t on t.team_member_id = u.user_id "
						+ "join xt_team_member_group_user_mapping tgum on t.id = tgum.team_member_id "
						+ "where lower(u.email_id) = lower(:emailId) and u.company_id = :companyId";
				HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
				hibernateSQLQueryResultRequestDTO.setQueryString(sqlQueryString);
				hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
						.add(new QueryParameterDTO("emailId", emailId));
				hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
						.add(new QueryParameterDTO(COMPANY_ID, companyId));
				teamMemberGroupUserMappingId = (Integer) hibernateSqlQueryResultDao
						.getUniqueResult(hibernateSQLQueryResultRequestDTO);
			} catch (Exception e) {
				logger.debug(
						"Exception occured in the method findTeamMemberGroupUserMappingIdByEmailId for email id {} TimeStamp: {}",
						emailId, new Date());
			}
		}
		return teamMemberGroupUserMappingId;
	}

}
