package com.xtremand.userlist.dao.hibernate;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.NullPrecedence;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.sql.JoinType;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.xtremand.campaign.bom.UnsubscribedUser;
import com.xtremand.campaign.dto.OneClickLaunchSharedLeadsDTO;
import com.xtremand.campaign.dto.ReceiverMergeTagsDTO;
import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.Criteria;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.common.bom.Pagination;
import com.xtremand.common.bom.Pagination.SORTINGORDER;
import com.xtremand.company.bom.Company;
import com.xtremand.company.dto.CompanyProfileDTO;
import com.xtremand.dam.exception.DamDataAccessException;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.dashboard.buttons.dto.DashboardButtonsPartnersDTO;
import com.xtremand.exception.DuplicateEntryException;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.formbeans.UserListDTO;
import com.xtremand.partner.bom.UpdatedContactsHistory;
import com.xtremand.partnership.dao.PartnershipDAO;
import com.xtremand.team.dao.TeamDao;
import com.xtremand.user.bom.LegalBasis;
import com.xtremand.user.bom.Role;
import com.xtremand.user.bom.ShareListPartner;
import com.xtremand.user.bom.SharedDetailsDTO;
import com.xtremand.user.bom.User;
import com.xtremand.user.bom.UserList;
import com.xtremand.user.bom.UserList.ContactListTypeValue;
import com.xtremand.user.bom.UserList.TYPE;
import com.xtremand.user.bom.UserListDetails;
import com.xtremand.user.bom.UserListUsersCount;
import com.xtremand.user.bom.UserListUsersView;
import com.xtremand.user.bom.UserUserList;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.user.exception.UserDataAccessException;
import com.xtremand.user.list.dto.ContactsCSVDTO;
import com.xtremand.user.list.dto.ContactsCountDTO;
import com.xtremand.user.list.dto.CopiedUserListUsersDTO;
import com.xtremand.user.list.dto.CopyGroupUsersDTO;
import com.xtremand.userlist.dao.UserListDAO;
import com.xtremand.userlist.exception.UserListException;
import com.xtremand.util.PaginationUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.HibernateSQLQueryResultUtilDao;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.dto.DeletedPartnerDTO;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.PartnerGroupDTO;
import com.xtremand.util.dto.QueryParameterDTO;
import com.xtremand.util.dto.QueryParameterListDTO;
import com.xtremand.util.dto.SortColumnDTO;
import com.xtremand.util.dto.TeamMemberFilterDTO;
import com.xtremand.util.dto.UserListAndUserId;
import com.xtremand.util.dto.UserUserListDTO;
import com.xtremand.util.dto.XamplifyConstants;
import com.xtremand.views.ContactAndPartnerListView;

@Repository("userListDAO")
@Transactional
public class HibernateUserListDAO implements UserListDAO {

	private static final String PARTNER_LIST_ID = "partnerListId";

	private static final Logger logger = LoggerFactory.getLogger(HibernateUserListDAO.class);

	@Autowired
	SessionFactory sessionFactory;

	@Autowired
	private UserDAO userDAO;

	@Autowired
	private PaginationUtil paginationUtil;

	@Autowired
	private GenericDAO genericDao;

	@Autowired
	private PartnershipDAO partnershipDAO;

	@Autowired
	private TeamDao teamDao;

	@Autowired
	private UtilDao utilDao;

	@Autowired
	private HibernateSQLQueryResultUtilDao hibernateSQLQueryResultUtilDao;

	@Value("${contactOrPartnerListsQuerySelect}")
	private String contactOrPartnerListsQuerySelect;

	@Value("${contactOrPartnerListsQueryFrom}")
	private String contactOrPartnerListsQueryFrom;

	@Value("${contactOrPartnerListsTotalCountQuerySelect}")
	private String contactOrPartnerListsTotalCountQuerySelect;

	@Value("${contactOrPartnerListsGroupByQuery}")
	private String contactOrPartnerListsGroupByQuery;

	@Value("${active.partner.list.name}")
	private String activePartnerListName;

	@Value("${inactive.partner.list.name}")
	private String inActivePartnerListName;

	@Value("${module.shareLeads}")
	private String shareLeadsModuleName;

	/********** XNFR-278 **********/
	@Value("${query.findUserListGroupsQuery}")
	private String userListGroupsQuery;

	/********** XNFR-966 **********/
	@Value("${query.findUserListContactsQuery}")
	private String userListContactsQuery;

	@Value("${query.groupByUserListContactQuery}")
	private String groupByUserListContactQuery;

	@Value("${query.groupByUserListGroupQuery}")
	private String userListGroupsGroupByQuery;

	@Value("${query.searchUserListGroups}")
	private String userListGroupsSearchKey;

	@Value("${userlist.partner.module}")
	private String partnerGroup;

	@Value("${contactsExcludedUsersCount}")
	private String contactsExcludedUsersCount;

	@Value("${assignedLeadsExcludedUsersCount}")
	private String assignedLeadsExcludedUsersCount;

	@Value("${SharedLeadsExcludedUsersCount}")
	private String SharedLeadsExcludedUsersCount;

	@Value("${contactsExcludedSql}")
	private String contactsExcludedSql;

	@Value("${assignedLeadsExcludedSql}")
	private String assignedLeadsExcludedSql;

	@Value("${SharedLeadsExcludedSql}")
	private String SharedLeadsExcludedSql;

	@Value("${contactsExcludedSqlWithSearch}")
	private String contactsExcludedSqlWithSearch;

	@Value("${assignedLeadsExcludedSqlWithSearch}")
	private String assignedLeadsExcludedSqlWithSearch;

	@Value("${SharedLeadsExcludedSqlWithSearch}")
	private String SharedLeadsExcludedSqlWithSearch;

	@Value("${AlldataSql}")
	private String allDataSql;

	@Value("${ActiveSql}")
	private String activeSql;

	@Value("${InActivesql}")
	private String inActivesql;

	@Value("${ValidSql}")
	private String validSql;

	@Value("${InValidSql}")
	private String inValidSql;

	@Value("${UnSubscribedSql}")
	private String unSubscribedSql;

	@Value("${partnersUnSubscribedSql}")
	private String partnerUnSubscribedSql;

	@Value("${SortSql}")
	private String sortSqlquery;

	@Value("${ActiveInActiveValidDataSql}")
	private String activeInActiveValidDataSql;

	@Value("${ExcludedDataSql}")
	private String excludedDataSql;

	@Value("${InValidDataSql}")
	private String inValidDataSql;

	@Value("${AllUnSubscribedSql}")
	private String allUnSubscribedSql;

	@Value("${PartnersAllCountSql}")
	private String partnersAllCountSql;

	@Value("${AllCountSql}")
	private String allCountSql;

	@Value("${PartnersActiveCountSql}")
	private String partnersActiveCountSql;

	@Value("${ActiveCountSql}")
	private String activeCountSql;

	@Value("${PartnersInActiveCountSql}")
	private String partnersInActiveCountSql;

	@Value("${InActivecountSql}")
	private String inActivecountSql;

	@Value("${PartnersUndeliverableCountSql}")
	private String partnersUndeliverableCountSql;

	@Value("${UndeliverableCountSql}")
	private String undeliverableCountSql;

	@Value("${PartnersUnsubscribedCountSql}")
	private String partnersUnsubscribedCountSql;

	@Value("${UnsubscribedCountSql}")
	private String unsubscribedCountSql;

	@Value("${AllDatadownaload}")
	private String allDatadownaload;

	@Value("${teamMemberSql}")
	private String teamMemberSql;

	/********** XNFR-278 **********/

	@Override
	public UserList findByPrimaryKey(Serializable pk, FindLevel[] levels) {
		logger.debug("HibernateUserListDAO findByPrimaryKey " + pk);
		UserList userList = (UserList) sessionFactory.getCurrentSession().get(UserList.class, pk);
		if (userList != null) {
			loadAssociations(userList, levels);
		}
		return userList;
	}

	@Override
	public Collection<UserList> find(List<Criteria> criterias, FindLevel[] levels) {
		logger.debug("HibernateUserListDAO find ");
		List<Criterion> criterions = generateCriteria(criterias);
		return findLists(criterions, levels);
	}

	@SuppressWarnings("unchecked")
	public Collection<UserList> findLists(List<Criterion> criterions, FindLevel[] levels) {
		logger.debug("HibernateUserListDAO find ");
		Session session = sessionFactory.getCurrentSession();

		org.hibernate.Criteria criteria = session.createCriteria(UserList.class);

		for (Criterion criterion : criterions) {
			criteria.add(criterion);
		}

		List<UserList> userLists = criteria.list();

		if (!userLists.isEmpty()) {
			loadAssociations(userLists.get(0), levels);
		}

		return userLists;
	}

	private void loadAssociations(UserList userList, FindLevel[] levels) {
		for (FindLevel level : levels) {
			switch (level) {
			case USERS:
				Hibernate.initialize(userList.getUsers());
				break;
			case USER_USER_LIST:
				Hibernate.initialize(userList.getUserUserLists());
				break;
			case COMPANY_PROFILE:
				Hibernate.initialize(userList.getCompany());
				break;
			default:
				break;
			}
		}
	}

	@Override
	public void resubscribeUser(Integer userId, Integer customerCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		/*
		 * session.
		 * createSQLQuery("update xt_unsubscribed_user set is_customer_enabled=true, customer_enabled_time='"
		 * + new Date() + "' where user_id = "+userId+" and customer_company_id="+
		 * customerCompanyId).executeUpdate();
		 */
		session.createSQLQuery("delete from xt_unsubscribed_user  where user_id = " + userId
				+ " and customer_company_id=" + customerCompanyId).executeUpdate();
	}

	@Override
	public void deleteByPrimaryKey(Integer id) {
		Session session = sessionFactory.getCurrentSession();
		session.createSQLQuery("delete from xt_user_userlist where user_list_id = " + id).executeUpdate();
		session.createSQLQuery(
				"delete from xt_user_list where user_list_id = " + id + " and is_default_partnerlist=false")
				.executeUpdate();
		session.createSQLQuery("delete from xt_user_legal_basis where user_list_id = " + id).executeUpdate();
	}

	@Override
	public void deletePartnersFromPartnerLists(List<Integer> userIdsList, List<Integer> userListIds) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "delete from xt_user_userlist where user_id in (:userIds) and user_list_id in (:userListIds)";
		Query query = session.createSQLQuery(sql);
		query.setParameterList("userIds", userIdsList);
		query.setParameterList("userListIds", userListIds);
		query.executeUpdate();
	}

	@Override
	public void deleteLegalBasis(List<Integer> userIds, Integer userListId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "delete from xt_user_legal_basis where user_id in (:userIds) and user_list_id = :userListId";
		Query query = session.createSQLQuery(sql);
		query.setParameterList("userIds", userIds);
		query.setParameter("userListId", userListId);
		query.executeUpdate();
	}

	@Override
	public void removeInvalidUser(Integer userId, Integer userListId) {
		// String userListIdsStr = userListIds.toString().replace("[", "(").replace("]",
		// ")");

		Session session = sessionFactory.getCurrentSession();
		session.createSQLQuery(
				"delete from xt_user_userlist where user_id =" + userId + " and user_list_id = " + userListId)
				.executeUpdate();
	}

	@Override
	public Integer saveUserList(UserList userList) {
		return (Integer) sessionFactory.getCurrentSession().save(userList);

	}

	@Override
	public UserList getUserListByCustomerAndName(Integer customerId, String name) {
		return (UserList) sessionFactory.getCurrentSession().createCriteria(UserList.class)
				.add(Restrictions.eq("name", name)).add(Restrictions.eq("owner.userId", customerId)).uniqueResult();

	}

	@Override
	public Map<String, Object> find(List<Criteria> criterias, FindLevel[] levels, Pagination pagination) {
		logger.debug("HibernateUserListDAO find ");
		List<Criterion> criterions = generateCriteria(criterias);
		return findUserLists(criterions, levels, pagination);
	}

	@Override
	public Map<String, Object> find(User user, Pagination pagination, FindLevel[] levels) {
		logger.debug("HibernateUserListDAO find ");
		List<Criterion> criterions = getContactsCriterias(user, pagination);
		return findUserLists(criterions, levels, pagination);
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> findUserLists(List<Criterion> criterions, FindLevel[] levels, Pagination pagination) {
		logger.debug("HibernateUserListDAO find ");
		Session session = sessionFactory.getCurrentSession();

		org.hibernate.Criteria criteria = session.createCriteria(UserList.class);
		// List<Criterion> criterions = generateCriteria(criterias);

		for (Criterion criterion : criterions) {
			criteria.add(criterion);
		}

		if (pagination != null && pagination.getSearchKey() != null) {
			criteria.add(Restrictions.ilike("name", "%" + pagination.getSearchKey() + "%"));
		}
		List<Integer> userListIds = new ArrayList<>();
		if ((pagination != null) && pagination.isAddingMoreLists()) {
			if (pagination.isAddingMoreLists()) {
				Criterion notInCriteria = Restrictions.not(Restrictions.in("id", userListIds));
				addContactCriteria(criteria, userListIds, notInCriteria);
			}
		}

		ScrollableResults scrollableResults = criteria.scroll();
		scrollableResults.last();

		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		logger.info("totalRecords : " + totalRecords);
		scrollableResults.close();

		Optional<Pagination> paginationObj = Optional.ofNullable(pagination);

		if (paginationObj.isPresent()) {
			Optional<Integer> maxResultsObj = Optional.ofNullable(paginationObj.get().getMaxResults());
			Optional<Integer> pageIndexObj = Optional.ofNullable(paginationObj.get().getPageIndex());
			Optional<String> sortcolumnObj = Optional.ofNullable(paginationObj.get().getSortcolumn());
			if (maxResultsObj.isPresent() && pageIndexObj.isPresent()) {
				if (!pagination.isEditCampaign()) {
					criteria.setFirstResult((pageIndexObj.get() * maxResultsObj.get()) - maxResultsObj.get());
					criteria.setMaxResults(maxResultsObj.get());
				}

			}

			if (sortcolumnObj.isPresent()) {
				if (SORTINGORDER.ASC == SORTINGORDER.valueOf(paginationObj.get().getSortingOrder())) {
					criteria.addOrder(Order.asc(sortcolumnObj.get()));
				} else if (SORTINGORDER.DESC == SORTINGORDER.valueOf(paginationObj.get().getSortingOrder())) {
					criteria.addOrder(Order.desc(sortcolumnObj.get()));
				}
			} else {
				criteria.addOrder(Order.desc("createdTime"));
			}
		}

		List<UserList> userLists = criteria.list();
		if (pagination.isEditCampaign()) {
			Integer records = 0;
			List<UserList> userLists2 = new ArrayList<UserList>();
			if ((!userListIds.isEmpty()) && userListIds.size() > 0) {
				Map<String, Object> map = getAllUserLists(criterions, levels, pagination, userListIds);
				userLists2 = (List<UserList>) map.get("userLists");
				records = (Integer) map.get("totalRecords");
			}

			List<UserList> sortedUserList = new ArrayList<>();
			Integer allRecords = totalRecords + records;
			sortedUserList.addAll(userLists);
			sortedUserList.addAll(userLists2);
			Map<String, Object> resultMap = new HashMap<String, Object>();
			Integer startIndex = 0;
			if (pagination.getPageIndex() > 1) {
				Integer pageIndex = pagination.getPageIndex();
				logger.debug("::::::::::Page Number::::::" + pageIndex);
				Integer pageNumber = pageIndex - 1;
				logger.debug("::::::::::::::Page Number::::::::::::" + pageNumber);
				startIndex = pagination.getMaxResults() * (pageNumber);
				logger.debug(":::::::::::Omde::::::::" + startIndex);
			}
			Integer endIndex = Math.min(startIndex + pagination.getMaxResults(), sortedUserList.size());
			logger.debug("Contact Start Index:::::::::" + startIndex + "::::::::::::::::End Index:::::::::" + endIndex);
			if (startIndex > sortedUserList.size()) {
				logger.debug("Start Index is greater than user list size");
			} else {
				resultMap.put("userLists", sortedUserList.subList(startIndex, endIndex));
			}
			resultMap.put("totalRecords", allRecords);
			return resultMap;
		} else {
			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put("userLists", userLists);
			resultMap.put("totalRecords", totalRecords);
			return resultMap;
		}

	}

	@Override
	public List<Criterion> getContactsCriterias(User user, Pagination pagination) {
		List<Criterion> criterions = new ArrayList<>();

		List<TYPE> list = new ArrayList<TYPE>();
		list.add(TYPE.CONTACT);
		list.add(TYPE.LEAD);
		list.add(TYPE.CONTACT_LISTVIEWS);
		list.add(TYPE.LEAD_LISTVIEWS);
		list.add(TYPE.LISTS);
		Integer userId = user.getUserId();
		List<Integer> roleIds = user.getRoles().stream().map(Role::getRoleId).collect(Collectors.toList());
		DeletedPartnerDTO deletedPartnerDTO = utilDao.getDeletedPartnerDTOByRoleIds(userId, roleIds);
		boolean deletedPartnerCompanyUser = deletedPartnerDTO.isDeletedPartnerCompanyUser();
		if (user.getRoles().stream().anyMatch((role) -> (role.getRoleId()) == Role.COMPANY_PARTNER.getRoleId())
				|| deletedPartnerCompanyUser) {
			Criterion creterion1 = Restrictions.and(Restrictions.in("contactType", list),
					Restrictions.eq("company.id", user.getCompanyProfile().getId()),
					Restrictions.eq("moduleName", "CONTACTS"));
			criterions.add(creterion1);
		} else if (user.getRoles().stream().anyMatch((role) -> (role.getRoleId()) == Role.ALL_ROLES.getRoleId())) {
			List<Integer> contactUploadTeamMemberIds = getContactUploadTeamMemberIds(user.getCompanyProfile().getId());
			contactUploadTeamMemberIds.add(user.getUserId());
			Criterion rest1 = Restrictions.and(Restrictions.in("contactType", list),
					Restrictions.eq("company.id", user.getCompanyProfile().getId()),
					Restrictions.isNull("contactListType"), Restrictions.eq("publicList", true),
					Restrictions.eq("moduleName", "CONTACTS"));
			Criterion rest2 = Restrictions.and(Restrictions.in("contactType", list),
					Restrictions.isNull("contactListType"),
					Restrictions.in("owner.userId", contactUploadTeamMemberIds.toArray()),
					Restrictions.eq("publicList", false), Restrictions.eq("moduleName", "CONTACTS"));
			Criterion criterion1 = Restrictions.or(rest1, rest2);
			criterions.add(criterion1);
		} else if (user.getRoles().stream().anyMatch((role) -> (role.getRoleId()) == Role.USER_ROLE.getRoleId())) {
			criterions.add(Restrictions.and(Restrictions.eq("owner.userId", user.getUserId()),
					Restrictions.eq("contactListType", null), Restrictions.eq("moduleName", "CONTACTS")));
		}

		if (pagination != null && pagination.getFilterBy() != null
				&& org.springframework.util.StringUtils.hasText(pagination.getFilterBy())) {
			if ("FORM-LEADS".equals(pagination.getFilterBy())) {
				criterions.add(Restrictions.isNotNull("form.id"));
				criterions.add(Restrictions.isNull("associatedCompany.id"));
			} else if ("COMPANY-CONTACTS".equals(pagination.getFilterBy())) {
				criterions.add(Restrictions.isNotNull("associatedCompany.id"));
				criterions.add(Restrictions.isNull("form.id"));
			} else if ("MY-CONTACTS".equals(pagination.getFilterBy())) {
				criterions.add(Restrictions.isNull("form.id"));
				criterions.add(Restrictions.isNull("associatedCompany.id"));
			}
		}

		criterions.add(Restrictions.eq("isPartnerUserList", false));
		if (pagination != null && pagination.getFilterBy() != null
				&& org.springframework.util.StringUtils.hasText(pagination.getFilterBy())) {
			if ("FORM-LEADS".equals(pagination.getFilterBy())) {
				criterions.add(Restrictions.isNotNull("form.id"));
				criterions.add(Restrictions.isNull("associatedCompany.id"));
			} else if ("COMPANY-CONTACTS".equals(pagination.getFilterBy())) {
				criterions.add(Restrictions.isNotNull("associatedCompany.id"));
				criterions.add(Restrictions.isNull("form.id"));
			} else if ("MY-CONTACTS".equals(pagination.getFilterBy())) {
				criterions.add(Restrictions.isNull("form.id"));
				criterions.add(Restrictions.isNull("associatedCompany.id"));
			}
		}
		return criterions;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getContactUploadTeamMemberIds(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select up.user_id from xt_user_profile up inner join xt_user_role ur on up.user_id=ur.user_id "
				+ " left outer join xt_team_member tm on up.user_id=tm.team_member_id where tm.company_id=:companyId and tm.status='APPROVE' "
				+ " and ur.user_id  not in (select distinct ur.user_id from xt_user_role ur,xt_user_profile up where up.user_id=ur.user_id "
				+ " and ur.role_id =9 and up.company_id=:companyId " + " ) " + " and ur.role_id=6";
		Query query = session.createSQLQuery(sql);
		query.setParameter("companyId", companyId);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getContactUploadTeamMemberAndSupervisorsIds(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select up.user_id from xt_user_profile up inner join xt_user_role ur on up.user_id=ur.user_id "
				+ " left outer join xt_team_member tm on up.user_id=tm.team_member_id where tm.company_id=:companyId and tm.status='APPROVE' "
				+ " and ur.user_id  not in (select distinct ur.user_id from xt_user_role ur,xt_user_profile up where up.user_id=ur.user_id "
				+ " and ur.role_id in (2) and up.company_id=:companyId " + " ) " + " and ur.role_id in (6, 9)";
		Query query = session.createSQLQuery(sql);
		query.setParameter("companyId", companyId);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getCampaignUploadTeamMemberIds(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select up.user_id from xt_user_profile up inner join xt_user_role ur on up.user_id=ur.user_id "
				+ " left outer join xt_team_member tm on up.user_id=tm.team_member_id where tm.company_id=:companyId and tm.status='APPROVE' "
				+ " and ur.user_id  not in (select distinct ur.user_id from xt_user_role ur,xt_user_profile up where up.user_id=ur.user_id "
				+ " and ur.role_id =9 and up.company_id=:companyId " + " ) " + " and ur.role_id=5";
		Query query = session.createSQLQuery(sql);
		query.setParameter("companyId", companyId);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getCampaignUploadTeamMemberIdsAndSupervisorIds(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select up.user_id from xt_user_profile up inner join xt_user_role ur on up.user_id=ur.user_id "
				+ " left outer join xt_team_member tm on up.user_id=tm.team_member_id where tm.company_id=:companyId and tm.status='APPROVE' "
				+ " and ur.user_id  not in (select distinct ur.user_id from xt_user_role ur,xt_user_profile up where up.user_id=ur.user_id "
				+ " and ur.role_id in (" + Role.getAllAdminRolesInString() + ") and up.company_id=:companyId " + " ) "
				+ " and ur.role_id in (5, 9)";
		Query query = session.createSQLQuery(sql);
		query.setParameter("companyId", companyId);
		return query.list();
	}

	private void addContactCriteria(org.hibernate.Criteria criteria, List<Integer> userListIds, Criterion inCriteria) {
		if (!userListIds.isEmpty()) {
			criteria.add(inCriteria);
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getAllUserLists(List<Criterion> criterions, FindLevel[] levels, Pagination pagination,
			List<Integer> userListIds) {
		logger.debug("HibernateUserListDAO find ");
		Session session = sessionFactory.getCurrentSession();

		org.hibernate.Criteria criteria = session.createCriteria(UserList.class);
		// List<Criterion> criterions = generateCriteria(criterias);

		for (Criterion criterion : criterions) {
			criteria.add(criterion);
		}

		if (pagination != null && pagination.getSearchKey() != null) {
			criteria.add(Restrictions.ilike("name", "%" + pagination.getSearchKey() + "%"));
		}
		logger.debug("Getting Campaign User List Ids::::::::" + userListIds);// Getting Userlists which are not in
		// campaign
		if ((!userListIds.isEmpty()) && userListIds.size() > 0) {
			criteria.add(Restrictions.not(Restrictions.in("id", userListIds)));
		}

		ScrollableResults scrollableResults = criteria.scroll();
		scrollableResults.last();

		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		logger.info("totalRecords : " + totalRecords);
		scrollableResults.close();

		Optional<Pagination> paginationObj = Optional.ofNullable(pagination);

		if (paginationObj.isPresent()) {
			Optional<Integer> maxResultsObj = Optional.ofNullable(paginationObj.get().getMaxResults());
			Optional<Integer> pageIndexObj = Optional.ofNullable(paginationObj.get().getPageIndex());
			Optional<String> sortcolumnObj = Optional.ofNullable(paginationObj.get().getSortcolumn());
			if (maxResultsObj.isPresent() && pageIndexObj.isPresent()) {
				/*
				 * criteria.setFirstResult((pageIndexObj.get()*maxResultsObj.get())-
				 * maxResultsObj.get()); criteria.setMaxResults(maxResultsObj.get());
				 */
			}

			if (sortcolumnObj.isPresent()) {
				if (SORTINGORDER.ASC == SORTINGORDER.valueOf(paginationObj.get().getSortingOrder())) {
					criteria.addOrder(Order.asc(sortcolumnObj.get()));
				} else if (SORTINGORDER.DESC == SORTINGORDER.valueOf(paginationObj.get().getSortingOrder())) {
					criteria.addOrder(Order.desc(sortcolumnObj.get()));
				}
			} else {
				criteria.addOrder(Order.asc("createdTime"));
			}
		}

		List<UserList> userLists = criteria.list();

		if (!userLists.isEmpty()) {
			loadAssociations(userLists.get(0), levels);
		}

		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("userLists", userLists);
		resultMap.put("totalRecords", totalRecords);
		return resultMap;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> listUserlistNames(Integer[] userIdArray) {
		logger.debug("entered into HibernateUserListDAO getUserlistNames() ");

		List<String> names = new ArrayList<String>();

		List<TYPE> contactListType = new ArrayList<TYPE>();
		contactListType.add(TYPE.CONTACT);
		contactListType.add(TYPE.LEAD);
		contactListType.add(TYPE.CONTACT_LISTVIEWS);
		contactListType.add(TYPE.LEAD_LISTVIEWS);

		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(UserList.class);
		criteria.add(Restrictions.in("owner.userId", userIdArray));
		criteria.add(Restrictions.in("contactType", contactListType));
		criteria.setProjection(Projections.property("name"));
		List<String> userListNames = criteria.list();

		for (String name : userListNames) {
			names.add(name.toLowerCase());
		}
		return names;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> findCampaignUserLists(List<Criteria> criterias, FindLevel[] levels,
			Pagination pagination, List<Integer> userListIds) {
		Session session = sessionFactory.getCurrentSession();

		org.hibernate.Criteria criteria = session.createCriteria(UserList.class);
		List<Criterion> criterions = generateCriteria(criterias);

		for (Criterion criterion : criterions) {
			criteria.add(criterion);
		}

		if (pagination != null && pagination.getSearchKey() != null) {
			criteria.add(Restrictions.ilike("name", "%" + pagination.getSearchKey() + "%"));
		}
		criteria.add(Restrictions.in("id", userListIds));

		ScrollableResults scrollableResults = criteria.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		scrollableResults.close();

		Optional<Pagination> paginationObj = Optional.ofNullable(pagination);

		if (paginationObj.isPresent()) {
			Optional<Integer> maxResultsObj = Optional.ofNullable(paginationObj.get().getMaxResults());
			Optional<Integer> pageIndexObj = Optional.ofNullable(paginationObj.get().getPageIndex());
			Optional<String> sortcolumnObj = Optional.ofNullable(paginationObj.get().getSortcolumn());

			if (maxResultsObj.isPresent() && pageIndexObj.isPresent()) {
				criteria.setFirstResult((pageIndexObj.get() * maxResultsObj.get()) - maxResultsObj.get());
				criteria.setMaxResults(maxResultsObj.get());
			}

			if (sortcolumnObj.isPresent()) {
				if (SORTINGORDER.ASC == SORTINGORDER.valueOf(paginationObj.get().getSortingOrder())) {
					criteria.addOrder(Order.asc(sortcolumnObj.get()));
				} else if (SORTINGORDER.DESC == SORTINGORDER.valueOf(paginationObj.get().getSortingOrder())) {
					criteria.addOrder(Order.desc(sortcolumnObj.get()));
				}
			} else {
				criteria.addOrder(Order.desc("createdTime"));
			}
		}

		List<UserList> userLists = criteria.list();
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("userLists", userLists);
		resultMap.put("totalRecords", totalRecords);
		return resultMap;

	}

	@Override
	public UserUserList getUserUserList(Integer userId, List<Integer> userListIds) {
		Query query;
		UserUserList userUserList = null;
		try {
			Session session = sessionFactory.getCurrentSession();
			String sql = "SELECT user_id, \r\n" + "max(firstname) as firstname, \r\n" + "max(lastname) as lastname,\r\n"
					+ " max(description) as description,\r\n" + "max(country) as country,\r\n"
					+ "max(city) as city,\r\n" + " max(address) as address,\r\n"
					+ "max(contact_company) as contact_company,\r\n" + " max(job_title) as job_title,\r\n"
					+ " max(mobile_number) as mobile_number,max(zip) as zip_code,max(state) as state "
					+ " FROM xt_user_userlist WHERE user_list_id IN (:userListIds) AND user_id = :userId GROUP BY user_id";
			query = session.createSQLQuery(sql);
			query.setParameter("userId", userId);
			query.setParameterList("userListIds", userListIds);
			Object result = query.uniqueResult();
			if (result == null) {
				// throw new ObjectNotFoundException(userId, "User");
			}
			userUserList = constructUserUserList((Object[]) result);
		} catch (HibernateException e) {
			e.printStackTrace();
		}
		return userUserList;
	}

	private UserUserList constructUserUserList(Object[] objects) {
		if (objects != null) {
			UserUserList userUserList = new UserUserList();
			userUserList.setFirstName((String) objects[1]);
			userUserList.setLastName((String) objects[2]);
			userUserList.setDescription((String) objects[3]);
			userUserList.setCountry((String) objects[4]);
			userUserList.setCity((String) objects[5]);
			userUserList.setAddress((String) objects[6]);
			userUserList.setContactCompany((String) objects[7]);
			userUserList.setJobTitle((String) objects[8]);
			userUserList.setMobileNumber((String) objects[9]);
			userUserList.setZipCode((String) objects[10]);
			userUserList.setState((String) objects[11]);
			return userUserList;
		} else
			return null;
	}

	@Override
	public UserUserList getByIdAndUserId(Integer id, Integer userId) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select * from xt_user_userlist where user_list_id=:userListId and user_id=:userId";
		Query query = session.createSQLQuery(queryString);
		query.setParameter("userListId", id);
		query.setParameter("userId", userId);
		Object[] object = (Object[]) query.uniqueResult();
		UserUserList userUserList = new UserUserList();
		if (object != null) {
			userUserList.setCountry((String) object[3]);
			userUserList.setCity((String) object[4]);
			userUserList.setAddress((String) object[5]);
			userUserList.setContactCompany((String) object[6]);
			userUserList.setFirstName((String) object[8]);
			userUserList.setLastName((String) object[9]);
			userUserList.setMobileNumber((String) object[10]);
			userUserList.setState((String) object[11]);
			userUserList.setZipCode((String) object[12]);
			return userUserList;
		} else {
			return userUserList;
		}
	}

	@Override
	public Integer getDefaultPartnerListId(Integer customerId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select user_list_id from xt_user_list where customer_id = :customerId and is_default_partnerlist= true";
		Query query = session.createSQLQuery(sql);
		query.setParameter("customerId", customerId);
		return (Integer) query.uniqueResult();
	}

	@Override
	public UserList getDefaultPartnerList(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(UserList.class);
		criteria.add(Restrictions.eq("company.id", companyId));
		criteria.add(Restrictions.eq("isPartnerUserList", true));
		criteria.add(Restrictions.eq("isDefaultPartnerList", true));
		return (UserList) criteria.uniqueResult();
	}

	@Override
	public UserUserList getCampaignUserUserList(Integer userId, List<Integer> userListIds, Integer campaignId) {
		UserUserList userList = new UserUserList();
		if (userListIds != null && !userListIds.isEmpty()) {
			userList = genericDao.get(UserUserList.class, userListIds.get(0));
		}
		return userList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getPartnerCompanyIds(Integer userId, Integer userListId) {
		Session session = sessionFactory.getCurrentSession();
		Query partnerCompanyIdsQuery = session.createSQLQuery(
				"select u.company_id from xt_user_list ul,xt_user_userlist uul,xt_user_profile u where uul.user_list_id = ul.user_list_id and ul.user_list_id = :userListId and u.user_id = uul.user_id and u.company_id IS NOT NULL group by u.company_id");
		partnerCompanyIdsQuery.setParameter("userListId", userListId);
		return partnerCompanyIdsQuery.list();
	}

	@Override
	public void saveUserUserList(UserUserList userUserList) {
		Session session = sessionFactory.getCurrentSession();
		session.save(userUserList);
	}

	public void makeContactsValid(List<Integer> userIds) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session
				.createSQLQuery("update xt_user_profile set is_email_valid=true where user_id in :userIds");
		query.setParameterList("userIds", userIds);
		query.executeUpdate();
	}

	@Override
	public void updatedContactsHistory(List<UpdatedContactsHistory> updatedContactsHistoryList) {
		try {
			logger.debug("In updatedContactsHistory()");
			Session session = sessionFactory.getCurrentSession();
			for (int i = 0; i < updatedContactsHistoryList.size(); i++) {
				session.save(updatedContactsHistoryList.get(i));
				if (i % 30 == 0) {
					session.flush();
					session.clear();
				}
			}
		} catch (HibernateException | XamplifyDataAccessException e) {
			logger.error("Error In updatedContactsHistoryList()", e);
			throw new UserDataAccessException(e.getMessage());
		} catch (Exception e) {
			logger.error("Error In updatedContactsHistoryList()", e);
			throw new UserDataAccessException(e.getMessage());
		}
	}

	@Override
	public void updateUserListProcessingStatus(UserList userList) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "update xt_user_list set email_validation_ind=:emailValidationInd, validation_in_progress =:validationInProgress, upload_in_progress =:uploadInProgress"
				+ " where  user_list_id=:userListId";
		Query query = session.createSQLQuery(sql);
		query.setParameter("emailValidationInd", userList.isEmailValidationInd());
		query.setParameter("userListId", userList.getId());
		query.setParameter("validationInProgress", userList.isValidationInProgress());
		query.setParameter("uploadInProgress", userList.isUploadInProgress());
		query.executeUpdate();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> listLegalBasisByContactListIdAndUserId(Integer userListId, Integer userId) {
		// String sql = "select legal_basis_id from xt_user_legal_basis where
		// user_id=:userId and user_list_id=:userListId";

		String sql = "select lb.legal_basis_id from xt_user_legal_basis lb, xt_user_userlist uul where lb.user_userlist_id = uul.id "
				+ " and uul.user_id=:userId and uul.user_list_id=:userListId";
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(sql);
		query.setParameter("userListId", userListId);
		query.setParameter("userId", userId);
		return query.list();
	}

	@Override
	public boolean isLegalBasisExists(Integer userId, Integer userListId, Integer legalBasisId) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(
				"SELECT CASE WHEN  count(*) > 0 THEN true ELSE false END  from xt_user_legal_basis lb, xt_user_userlist uul "
						+ " where lb.user_userlist_id = uul.id and uul.user_id = :userId and uul.user_list_id = :userListId and lb.legal_basis_id = :legalBasisId");
		query.setParameter("userId", userId);
		query.setParameter("userListId", userListId);
		query.setParameter("legalBasisId", legalBasisId);
		return (boolean) query.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<UserDTO> listPartnersByUserListId(Integer userListId) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(
				"select distinct u.email_id as \"emailId\",u.user_id as \"id\",u.company_id as \"companyId\" from xt_user_userlist uul,xt_user_profile u where uul.user_list_id = :userListId and u.user_id = uul.user_id");
		query.setParameter("userListId", userListId);
		List<UserDTO> userDtos = (List<UserDTO>) paginationUtil.getListDTO(UserDTO.class, query);
		if (XamplifyUtils.isNotEmptyList(userDtos)) {
			return userDtos;
		} else {
			return new ArrayList<>();
		}
	}

	@Override
	public Integer getCompanyIdByUserListId(Integer userListId) {
		if (userListId != null && userListId > 0) {
			Session session = sessionFactory.getCurrentSession();
			String sqlString = " select u.company_id from xt_user_list ul,xt_user_profile u where ul.user_list_id = :userListId and u.user_id = ul.customer_id";
			Query query = session.createSQLQuery(sqlString);
			query.setParameter("userListId", userListId);
			return (Integer) query.uniqueResult();
		} else {
			return null;
		}

	}

	@Override
	public boolean isDefaultPartnerList(Integer userListId) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session
				.createSQLQuery("select is_default_partnerlist from xt_user_list where user_list_id = :userListId ");
		query.setParameter("userListId", userListId);
		return (boolean) query.uniqueResult();
	}

	@Override
	public UserList getMarketoMasterContactList(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(UserList.class);
		criteria.add(Restrictions.eq("company.id", companyId));
		criteria.add(Restrictions.eq("marketoMasterList", true));
		return (UserList) criteria.uniqueResult();
	}

	@Override
	public UserList getDealContactList(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(UserList.class);
		criteria.add(Restrictions.eq("company.id", companyId));
		criteria.add(Restrictions.eq("dealContactList", true));
		return (UserList) criteria.uniqueResult();
	}

	@Override
	public UserUserList getContactFromDealContactList(Integer userId, Integer userListId) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(UserUserList.class);
		criteria.add(Restrictions.eq("user.id", userId));
		criteria.add(Restrictions.eq("userList.id", userListId));
		return (UserUserList) criteria.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public ArrayList<Integer> getUserListIdsBYUserId(boolean isPartnerUserList, Integer userId, Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select u.user_list_id from xt_user_list u, xt_user_userlist uul where u.user_list_id = uul.user_list_id and uul.user_id=:userId "
				+ " and u.is_partner_userlist=false and u.company_id = :companyId";
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("userId", userId);
		query.setParameter("companyId", companyId);
		return (ArrayList<Integer>) query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> listPartnerEmailsByOrganization(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select email_id from xt_partnership pa, xt_user_profile up where "
				+ " pa.vendor_company_id=:companyId and pa.status = 'approved' and pa.partner_id=up.user_id "
				+ " and partner_company_id is not null";
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("companyId", companyId);
		return (List<String>) query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> listUserlistNames(List<Criterion> criterions) {
		logger.debug("entered into HibernateUserListDAO getUserlistNames() ");
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(UserList.class);
		for (Criterion criterion : criterions) {
			criteria.add(criterion);
		}
		criteria.setProjection(Projections.property("name"));
		return criteria.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getExcludedUserIds(List<Integer> userListIds, Integer customerCompanyId) {
		Session excludedSession = sessionFactory.getCurrentSession();
		List<List<Integer>> chunkedUserListIds = XamplifyUtils.getChunkedList(userListIds);
		List<Object[]> object = new ArrayList<>();
		for (List<Integer> userListId : chunkedUserListIds) {
			String excludedHql = "select distinct xuul.user_id, \'user\'  from xt_user_profile xup "
					+ " left join xt_user_list xul on xul.customer_id = xup.user_id "
					+ " left join xt_user_userlist xuul on xuul.user_list_id = xul.user_list_id "
					+ " left join xt_user_profile xup1 on xup1.user_id = xuul.user_id "
					+ " left join xt_excluded_user xeu on xeu.user_id = xuul.user_id "
					+ " where xeu.company_id = :customerCompanyId and xul.user_list_id in :userListIds " + " union "
					+ " select distinct xuul.user_id, \'domain\' from xt_user_profile xup "
					+ " left join xt_user_list xul on xul.customer_id = xup.user_id "
					+ " left join xt_user_userlist xuul on xuul.user_list_id = xul.user_list_id "
					+ " left join xt_user_profile xup1 on xup1.user_id = xuul.user_id "
					+ " left join xt_excluded_domain xed on xed.company_id  =xul.company_id "
					+ " where xed.company_id = :customerCompanyId and xul.user_list_id in :userListIds "
					+ " and xup1.email_id like concat(\'%@\',xed.domain_name,\'%\') ";

			SQLQuery excludeQuery = excludedSession.createSQLQuery(excludedHql);
			excludeQuery.setParameterList("userListIds", userListId);
			excludeQuery.setParameter("customerCompanyId", customerCompanyId);
			List<Object[]> list = excludeQuery.list();
			object.addAll(list);
		}
		return object;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getExcludedAssignedLeasdUserIds(List<Integer> userListIds, Integer customerCompanyId) {
		Session excludedSession = sessionFactory.getCurrentSession();
		String excludedHql = "select distinct xuul.user_id, \'user\'  from xt_user_profile xup "
				+ " left join xt_user_list xul on xul.assigned_by = xup.user_id "
				+ " left join xt_user_userlist xuul on xuul.user_list_id = xul.user_list_id "
				+ " left join xt_user_profile xup1 on xup1.user_id = xuul.user_id "
				+ " left join xt_excluded_user xeu on xeu.user_id = xuul.user_id "
				+ " where xeu.company_id = :customerCompanyId and xul.user_list_id in :userListIds " + " union "
				+ " select distinct xuul.user_id, \'domain\' from xt_user_profile xup "
				+ " left join xt_user_list xul on xul.assigned_by = xup.user_id "
				+ " left join xt_user_userlist xuul on xuul.user_list_id = xul.user_list_id "
				+ " left join xt_user_profile xup1 on xup1.user_id = xuul.user_id "
				+ " left join xt_excluded_domain xed on xed.company_id  =xul.assigned_company_id "
				+ " where xed.company_id = :customerCompanyId and xul.user_list_id in :userListIds "
				+ " and xup1.email_id like concat(\'%@\',xed.domain_name,\'%\') ";

		SQLQuery excludeQuery = excludedSession.createSQLQuery(excludedHql);
		excludeQuery.setParameterList("userListIds", userListIds);
		excludeQuery.setParameter("customerCompanyId", customerCompanyId);

		return excludeQuery.list();

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getExcludedSharedUserIds(List<Integer> userListIds, Integer logedInUserId,
			Integer customerCompanyId) {
		Session excludedSession = sessionFactory.getCurrentSession();
		String excludedHql = "select distinct xuul.user_id, \'user\'  from xt_user_profile xup "
				+ " left join xt_user_list xul on xul.company_id = xup.company_id "

				+ " left join xt_sharelist_partner xslp on xslp.user_list_id = xul.user_list_id"
				+ " left join xt_sharelist_partner_mapping xslpm on xslpm.sharelist_partner_id = xslp.id"

				+ " left join xt_user_userlist xuul on xuul.user_list_id = xul.user_list_id "
				+ " left join xt_user_profile xup1 on xup1.user_id = xuul.user_id "
				+ " left join xt_excluded_user xeu on xeu.user_id = xuul.user_id "
				+ " where xeu.company_id = :customerCompanyId and xul.user_list_id in :userListIds "
				+ " and xslpm.partner_id = :logedInUserId" + " union "
				+ " select distinct xuul.user_id, \'domain\' from xt_user_profile xup "
				+ " left join xt_user_list xul on xul.company_id = xup.company_id "

				+ " left join xt_sharelist_partner xslp on xslp.user_list_id = xul.user_list_id"
				+ " left join xt_sharelist_partner_mapping xslpm on xslpm.sharelist_partner_id = xslp.id"

				+ " left join xt_user_userlist xuul on xuul.user_list_id = xul.user_list_id "
				+ " left join xt_user_profile xup1 on xup1.user_id = xuul.user_id "
				+ " left join xt_excluded_domain xed on xed.company_id  =xul.company_id "
				+ " where xed.company_id = :customerCompanyId and xul.user_list_id in :userListIds "
				+ " and xslpm.partner_id = :logedInUserId"
				+ " and xup1.email_id like concat(\'%@\',xed.domain_name,\'%\')";

		SQLQuery excludeQuery = excludedSession.createSQLQuery(excludedHql);
		excludeQuery.setParameterList("userListIds", userListIds);
		excludeQuery.setParameter("customerCompanyId", customerCompanyId);
		excludeQuery.setParameter("logedInUserId", logedInUserId);

		return excludeQuery.list();

	}

	@Override
	public Map<String, Object> fetchUserListUsers(User loggedInUser, List<Integer> userListIds, String searchHQL,
			String sortSQL, String dataSortSQL, String sortColumnSQL, String sortingOrder, Pagination pagination,
			UserListDTO userListDTO) {
		return getActiveUsers(loggedInUser, userListIds, searchHQL, sortSQL, dataSortSQL, sortColumnSQL, sortingOrder,
				pagination, userListDTO);

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getUserListIds(List<Criterion> criterions) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(UserList.class);
		for (Criterion criterion : criterions) {
			criteria.add(criterion);
		}
		Projection projection = Projections.property("id");
		criteria.setProjection(projection);
		return criteria.list();
	}

	@Override
	public ContactsCountDTO getContactsCount(List<Integer> userListIds, Integer customerCompanyId,
			UserListDTO userListDTO, Integer loggedInUserId) {
		List<Object[]> excludedData = new ArrayList<>();
		String moduleName = userListDTO.getModuleName();
		ContactsCountDTO contactsCountDTO = new ContactsCountDTO();
		Integer excludedUserCount = null;

		if (moduleName != null && !moduleName.equalsIgnoreCase("partners")) {
			if (moduleName.equalsIgnoreCase("sharedleads")) {
				excludedData = getExcludedSharedUserIds(userListIds, loggedInUserId, customerCompanyId);
			} else if (userListDTO.isAssignedLeadsList()) {
				excludedData = getExcludedAssignedLeasdUserIds(userListIds, customerCompanyId);
			} else {
				excludedData = getExcludedUserIds(userListIds, customerCompanyId);
			}

			List<Integer> excludedUserIds = excludedData.stream().map(data -> Integer.parseInt(data[0].toString()))
					.distinct().collect(Collectors.toList());
			excludedUserCount = excludedUserIds.size();
			contactsCountDTO.setExcludedCount(excludedUserCount);
			contactsCountDTO.setInValidCount(
					getUndeliverableCount(userListIds, customerCompanyId, moduleName, loggedInUserId, userListDTO));
		}

		contactsCountDTO.setAllCounts(getAllContactsCount(userListIds, customerCompanyId, moduleName, userListDTO));
		contactsCountDTO.setActiveCount(
				getActiveCount(userListIds, customerCompanyId, moduleName, loggedInUserId, userListDTO));
		contactsCountDTO.setInActiveCount(
				getInActiveCount(userListIds, customerCompanyId, moduleName, loggedInUserId, userListDTO));
		contactsCountDTO.setUnSubscribedCount(
				getUnSubscribedCount(userListIds, customerCompanyId, moduleName, loggedInUserId, userListDTO));
		return contactsCountDTO;
	}

	@Override
	public ContactsCountDTO getContactsCount(List<Integer> userListIds, Integer customerCompanyId) {
		List<Object[]> excludedData = getExcludedAssignedLeasdUserIds(userListIds, customerCompanyId);
		List<Integer> excludedUserIds = excludedData.stream().map(data -> Integer.parseInt(data[0].toString()))
				.distinct().collect(Collectors.toList());

		String excludedUsersSql = (!excludedUserIds.isEmpty()) ? " and uul.user_id not in (:excludedUserIds) " : " ";
		Integer excludedUserCount = excludedUserIds.size();

		Session session = sessionFactory.getCurrentSession();
		String sql = "select " + " cast( count(distinct up.user_id) as integer) as all, "
				+ " cast( count( distinct case when is_email_valid=true and up.status='APPROVE' "
				+ " and uul.user_id not in "
				+ " (select distinct un.user_id from xt_unsubscribed_user un where un.customer_company_id=u.company_id) "
				+ excludedUsersSql + " then up.user_id   end)  as integer) as active, "
				+ " cast( count ( distinct case when up.is_email_valid=true and up.status='UnApproved' "
				+ " and uul.user_id not in "
				+ " (select distinct un.user_id from xt_unsubscribed_user un where un.customer_company_id=u.company_id) "
				+ excludedUsersSql + " then up.user_id end)  as integer) as nonactive, "
				+ " cast( count ( distinct case when up.is_email_valid=false " + " and uul.user_id not in "
				+ " (select distinct un.user_id from xt_unsubscribed_user un where un.customer_company_id= u.company_id) "
				+ excludedUsersSql + " then up.user_id end) as integer) as invalid, "
				+ " cast( count ( distinct case when " + " uul.user_id in "
				+ " (select distinct un.user_id from xt_unsubscribed_user un where un.customer_company_id= u.company_id) "
				+ excludedUsersSql + " then up.user_id end) as integer) as unsubscribe, "

				+ "cast( :excludedUserCount as integer) as excludedCount "

				+ " from xt_user_list u, xt_user_userlist uul, xt_user_profile up  "
				+ " where u.user_list_id =uul.user_list_id and uul.user_id=up.user_id and up.email_validation_ind=true "
				+ " and u.user_list_id  in (:userListIds)";
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameterList("userListIds", userListIds);
		query.setParameter("excludedUserCount", excludedUserCount);
		if (!excludedUserIds.isEmpty()) {
			query.setParameterList("excludedUserIds", excludedUserIds);
		}

		return (ContactsCountDTO) query.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public ArrayList<Integer> getUserListIdsBYLeadId(boolean isPartnerUserList, Integer userId, Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select u.user_list_id from xt_user_list u, xt_user_userlist uul where u.user_list_id = uul.user_list_id and uul.user_id=:userId "
				+ " and u.is_partner_userlist=false and u.assigned_company_id = :companyId";
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("userId", userId);
		query.setParameter("companyId", companyId);
		return (ArrayList<Integer>) query.list();
	}

	@Override
	public Integer shareLeadsListsAvailable(Integer companyId, Integer vendorCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		Query query = null;
		String hql = null;
		if (vendorCompanyId != null) {
			hql = " select count(*) From UserList u where u.company.id = :companyId and module_name  = 'SHARE LEADS' and  assignedCompany.id = :vendorCompanyId";
			query = session.createQuery(hql);
			query.setParameter("companyId", companyId);
			query.setParameter("vendorCompanyId", vendorCompanyId);
		} else {
			hql = " select count(*) From UserList u where u.company.id = :companyId and module_name  = 'SHARE LEADS' ";
			query = session.createQuery(hql);
			query.setParameter("companyId", companyId);
		}
		return query.uniqueResult() != null ? ((Long) query.uniqueResult()).intValue() : 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<UserListAndUserId> findUserIdsAndUserListIds(List<Integer> userListIds) {
		try {
			if (userListIds != null && !userListIds.isEmpty()) {
				Session session = sessionFactory.getCurrentSession();
				String sql = "select distinct xuul.user_id as \"userId\", xuul.user_list_id as \"userListId\", "
						+ "ul.company_id as \"companyId\"  from xt_user_list ul, xt_user_userlist xuul "
						+ "where ul.user_list_id = xuul.user_list_id and ul.user_list_id in (:ids)";
				SQLQuery query = session.createSQLQuery(sql);
				query.setParameterList("ids", userListIds);
				return query.setResultTransformer(Transformers.aliasToBean(UserListAndUserId.class)).list();
			} else {
				return new ArrayList<>();
			}
		} catch (HibernateException e) {
			throw new UserListException(e);
		} catch (Exception ex) {
			throw new UserListException(ex);
		}

	}

	@Override
	public Map<String, Object> findUsersByUserListId(Pagination pagination) {
		try {
			HashMap<String, Object> map = new HashMap<>();
			String finalQueryString = "SELECT DISTINCT uul.user_id AS \"userId\", ul.user_list_id AS \"userListId\", \n"
					+ "ul.company_id AS \"companyId\", u.email_id AS \"emailId\", COALESCE(uul.firstname, '') AS \"firstName\", \n"
					+ "COALESCE(uul.lastname, '') AS \"lastName\", COALESCE(uul.contact_company, '') AS \"companyName\", \n"
					+ "COALESCE(uul.mobile_number, '') AS \"mobileNumber\", COALESCE(uul.job_title, '') AS \"jobTitle\", \n"
					+ "COALESCE(uul.address, '') AS \"address\", u.is_email_valid AS \"validEmail\", xp.id AS \"partnershipId\", \n"
					+ "ul.is_partner_userlist AS \"partnerGroup\",  xcs.stage_name as \"contactStatus\", xp.status \"partnerStatus\" \n"
					+ "FROM xt_user_list ul JOIN xt_user_userlist uul ON ul.user_list_id = uul.user_list_id \n"
					+ "JOIN xt_user_profile u ON u.user_id = uul.user_id \n"
					+ "LEFT JOIN xt_partnership xp ON xp.partner_id = u.user_id AND xp.vendor_company_id = ul.company_id \n"
					+ "LEFT JOIN xt_contact_status xcs on xcs.id = uul.contact_status_id \n"
					+ "WHERE ul.user_list_id = :userListId \n";

			if (XamplifyUtils.isValidString(pagination.getSearchKey())) {
				String searchKey = pagination.getSearchKey();
				StringBuilder searchbuilder = new StringBuilder();
				searchbuilder.append("and ( ");
				searchbuilder.append("uul.contact_company ilike '%" + searchKey + "%'");
				searchbuilder.append(" or uul.mobile_number ilike '%" + searchKey + "%'");
				searchbuilder.append(" or u.email_id ilike '%" + searchKey + "%'");
				searchbuilder.append(" or uul.job_title ilike '%" + searchKey + "%'");
				searchbuilder.append(" or uul.address ilike '%" + searchKey + "%'");
				searchbuilder.append(" or xcs.stage_name ilike '%" + searchKey + "%'");
				searchKey = searchKey.trim();
				String[] nameWords = searchKey.split("\\s+");
				if (nameWords.length == 1) {
					searchbuilder.append(" or uul.firstname ilike '%" + searchKey + "%'");
					searchbuilder.append(" or uul.lastname ilike '%" + searchKey + "%'");
					searchbuilder.append(
							" or REPLACE((uul.firstname || uul.lastname), ' ', '') " + "ilike '%" + searchKey + "%'");
					searchbuilder.append(
							" or REPLACE((uul.lastname || uul.firstname), ' ', '') " + "ilike '%" + searchKey + "%'");
				} else {
					for (String nameWord : nameWords) {
						searchbuilder.append(" or uul.firstname ilike '%" + nameWord + "%'");
						searchbuilder.append(" or uul.lastname ilike '%" + nameWord + "%'");
					}
				}
				searchbuilder.append(" ) ");
				finalQueryString += String.valueOf(searchbuilder);
			}

			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(finalQueryString);
			query.setParameter("userListId", pagination.getUserListId());
			return paginationUtil.setScrollableAndGetList(pagination, map, query, UserListUsersView.class);
		} catch (HibernateException | DamDataAccessException e) {
			throw new UserListException(e);
		} catch (Exception ex) {
			throw new UserListException(ex);
		}
	}

	@Override
	public Integer validContactsCount(List<Integer> userListIds, Integer customerCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = " select cast(count(distinct  up.user_id) as integer ) as count " + " from xt_user_list u, "
				+ " xt_user_userlist uul, " + " xt_user_profile up "
				+ " where u.user_list_id =uul.user_list_id and uul.user_id=up.user_id and up.email_validation_ind=true "
				+ " and u.user_list_id  in (:userListIds) "
				+ " and up.is_email_valid=true and ( up.status='APPROVE' or up.status='UnApproved' ) "
				+ " and uul.user_id not in ( " + " select distinct un.user_id from xt_unsubscribed_user un "
				+ " where un.customer_company_id=:customerCompanyId) " + " and uul.user_id not in ( "
				+ " select distinct ex.user_id from xt_excluded_user ex where ex.company_id=:customerCompanyId ) "
				+ " and split_part(up.email_id, '@', 2) not in ( "
				+ " select ex.domain_name  from xt_excluded_domain ex where ex.company_id=:customerCompanyId) ";
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameterList("userListIds", userListIds);
		query.setParameter("customerCompanyId", customerCompanyId);
		return query.uniqueResult() != null ? (Integer) query.uniqueResult() : 0;
	}

	@Override
	public Integer allUsersCount(List<Integer> userListIds) {
		Session session = sessionFactory.getCurrentSession();
		String sql = " select cast(count(distinct  up.user_id) as integer ) as count    "
				+ " from xt_user_list u, xt_user_userlist uul, xt_user_profile up, xt_unsubscribed_user un "
				+ " where u.user_list_id =uul.user_list_id and uul.user_id=up.user_id and up.email_validation_ind=true "
				+ " and u.user_list_id  in (:userListIds) "
				+ " and up.is_email_valid=true and ( up.status='APPROVE' or up.status='UnApproved' ) ";
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameterList("userListIds", userListIds);
		return query.uniqueResult() != null ? (Integer) query.uniqueResult() : 0;
	}

	@Override
	public Map<String, Object> findContactAndPartnerLists(Pagination pagination) {
		try {
			HashMap<String, Object> map = new HashMap<>();
			String finalQueryString = "";
			String searchKey = pagination.getSearchKey();
			Integer userId = pagination.getUserId();
			boolean orgAdminOrOrgAdminTeamMember = false;
			boolean isTeamMember = teamDao.isTeamMember(userId);
			List<Integer> teamMemberIds = new ArrayList<>();
			boolean showOnlyPartnerList = pagination.isChannelCampaign()
					|| !(orgAdminOrOrgAdminTeamMember || pagination.isPartnerMarketingCompany());
			boolean showPartnerAndContactLists = !pagination.isChannelCampaign()
					&& (orgAdminOrOrgAdminTeamMember || pagination.isPartnerMarketingCompany());

			finalQueryString = getFinalQueryForContactAndPartnerLists(searchKey, showOnlyPartnerList,
					showPartnerAndContactLists, teamMemberIds.isEmpty(), pagination);
			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(finalQueryString);
			query.setParameter("userId", pagination.getUserId());
			if (showPartnerAndContactLists && !teamMemberIds.isEmpty()) {
				query.setParameterList("createdBy", teamMemberIds);
			}
			return paginationUtil.setScrollableAndGetList(pagination, map, query, ContactAndPartnerListView.class);
		} catch (HibernateException | DamDataAccessException e) {
			throw new UserListException(e);
		} catch (Exception ex) {
			throw new UserListException(ex);
		}
	}

	private String getFinalQueryForContactAndPartnerLists(String searchKey, boolean showOnlyPartnerList,
			boolean showPartnerAndContactLists, boolean emptyTeamMembers, Pagination pagination) {
		String finalQueryString = "";
		String extraWhereClause = "";
		if (showOnlyPartnerList) {
			extraWhereClause = " and xul.is_partner_userlist";
		} else if (showPartnerAndContactLists && !emptyTeamMembers) {
			extraWhereClause = " and (xul.customer_id in (:createdBy) or is_public) ";
		}

		String filterWhereClause = getFilterWhereClauseByType(pagination);
		String sortQueryString = addSortOptionForContactAndParterLists(pagination);

		String query1 = "";
		if (StringUtils.hasText(searchKey)) {
			String searchQueryString = " and ( LOWER(xul.user_list_name) like LOWER('%searchKey%') "
					+ " or xuu.firstname ilike '%searchKey%'" + " or xuu.lastname ilike '%searchKey%'"
					+ " or xuu.contact_company ilike '%searchKey%'" + " or xuu.mobile_number ilike '%searchKey%'"
					+ " or u1.email_id ilike '%searchKey%'" + " or xuu.job_title ilike '%searchKey%'"
					+ " or xuu.address ilike '%searchKey%' "
					+ " or REPLACE(LOWER(xuu.firstname || xuu.lastname), ' ', '') like " + "LOWER('%"
					+ searchKey.replace(" ", "") + "%')"
					+ " or REPLACE(LOWER(xuu.lastname || xuu.firstname), ' ', '') like " + "LOWER('%"
					+ searchKey.replace(" ", "") + "%') ) ";
			query1 = contactOrPartnerListsQuerySelect + contactOrPartnerListsQueryFrom + extraWhereClause
					+ filterWhereClause + searchQueryString.replace("searchKey", searchKey) + " "
					+ contactOrPartnerListsGroupByQuery + " " + sortQueryString;
			String query2 = contactOrPartnerListsTotalCountQuerySelect + contactOrPartnerListsQueryFrom
					+ extraWhereClause + filterWhereClause + " " + contactOrPartnerListsGroupByQuery + " "
					+ sortQueryString;
			finalQueryString = "select q1.*, q2.count from (" + query1 + ") as q1, (" + query2
					+ ") as q2 where q1.id = q2.id";
		} else {
			finalQueryString = contactOrPartnerListsQuerySelect + ", count(DISTINCT xuu.user_id) AS count "
					+ contactOrPartnerListsQueryFrom + extraWhereClause + filterWhereClause + " "
					+ contactOrPartnerListsGroupByQuery + " " + sortQueryString;
		}
		return finalQueryString;
	}

	private String getFilterWhereClauseByType(Pagination pagination) {
		String filterWhereClause = "";
		if (pagination.getFilterBy() != null && !pagination.getFilterBy().isEmpty()) {
			if (pagination.getFilterBy().equalsIgnoreCase("MY-CONTACTS")) {
				filterWhereClause = " and xul.is_partner_userlist = 'false' and xul.form_id IS NULL and xul.associated_company_id IS NULL ";
			} else if (pagination.getFilterBy().equalsIgnoreCase("MY-PARTNERS")) {
				filterWhereClause = " and xul.is_partner_userlist = 'true' ";
			} else if (pagination.getFilterBy().equalsIgnoreCase("FORM-CONTACTS")) {
				filterWhereClause = " and xul.form_id IS NOT NULL ";
			} else if (pagination.getFilterBy().equalsIgnoreCase("COMPANY-CONTACTS")) {
				filterWhereClause = " and xul.associated_company_id IS NOT NULL ";
			} else if (pagination.getFilterBy().equalsIgnoreCase("ALL")) {
				filterWhereClause = "";
			}
		}
		return filterWhereClause;
	}

	private String addSortOptionForContactAndParterLists(Pagination pagination) {
		String sortOptionQueryString = "";
		if (StringUtils.hasText(pagination.getSortcolumn())) {
			if ("name".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += " order by xul.user_list_name " + pagination.getSortingOrder();
				sortOptionQueryString = paginationUtil.setNullConditionsForAscOrDesc(pagination, sortOptionQueryString);
			} else if ("createdTime".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += " order by xul.created_time " + pagination.getSortingOrder();
			} else if ("count".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += " order by  count(DISTINCT xuu.user_id) " + pagination.getSortingOrder();
			} else if ("selectedList".equals(pagination.getSortcolumn())) {
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
					+ ", xul.user_list_id),xul.user_list_name asc nulls first";
		} else {
			String sortOrder = StringUtils.hasText(pagination.getSortingOrder()) ? pagination.getSortingOrder()
					: " desc";
			sortOptionQueryString += " order by xul.created_time " + " " + sortOrder;
		}
		return sortOptionQueryString;
	}

	@Override
	public Integer findAllUsersCountByUserListIds(List<Integer> userListIds) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select cast(count(distinct user_id) as integer) as count from xt_user_userlist where user_list_id in (:userListIds)";
		Query query = session.createSQLQuery(queryString);
		query.setParameterList("userListIds", userListIds);
		return query.uniqueResult() != null ? (Integer) query.uniqueResult() : 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findSelectedUserListIdsByCampaignIds(Integer campaignId) {
		if (campaignId != null && campaignId > 0) {
			Session session = sessionFactory.getCurrentSession();
			String queryString = "select distinct user_list_id  from xt_campaign_user_userlist where campaign_id = :campaignId";
			Query query = session.createSQLQuery(queryString).setParameter("campaignId", campaignId);
			return query.list();
		} else {
			return new ArrayList<>();
		}

	}

	@Override
	public Map<String, Object> listUserLists(List<Criteria> criterias, Pagination pagination) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(UserListDetails.class);
		List<Criterion> criterions = generateCriteria(criterias);
		return addColumnNamesAndGetList(pagination, criteria, criterions);
	}

	private Map<String, Object> addColumnNamesAndGetList(Pagination pagination, org.hibernate.Criteria criteria,
			List<Criterion> criterions) {
		List<String> columnNames = new ArrayList<>();
		columnNames.add("name");
		return paginationUtil.addSearchAndPaginationAndSort(pagination, criteria, criterions, columnNames, "id",
				"desc");
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> listShareLists(Pagination pagination) {
		Session session = sessionFactory.getCurrentSession();
		String sql = " SELECT xul.user_list_id  as \"id\", " + " xul.is_partner_userlist as \"isPartnerUserList\",  "
				+ " xul.alias as \"alias\",  " + " xul.user_list_name as \"name\",  "
				+ " xul.module_name as \"moduleName\",  " + " cp.company_name as \"companyName\",  "
				+ " xul.created_time as \"createdTime\",  "

				+ " xul.assigned_by as \"uploadedUserId\",  "
				+ " cast((select count(DISTINCT user_id) from xt_user_userlist where user_list_id = xul.user_list_id) as integer) AS \"noOfContacts\",  "
				+ " xul.email_validation_ind as \"emailValidationInd\",  " + " CASE  "
				+ " WHEN length(btrim(concat(btrim(u.firstname), ' ', btrim(u.lastname)))) > 0 THEN  "

				+ " btrim(concat(btrim(u.firstname), ' ',btrim(u.middle_name), ' ', btrim(u.lastname))) "
				+ " ELSE u.email_id  " + " END AS \"uploadedBy\",  " + " xul.social_network as \"socialNetwork\", "
				+ " xul.contact_type as \"contactType\", " + " CASE " + " WHEN sp.user_list_id   IS NOT NULL THEN true "
				+ " ELSE false " + " END AS \"assignedToPartner\", "
				+ " xul.is_synchronized_list as \"synchronisedList\", "
				+ " xul.upload_in_progress as \"uploadInProgress\", "
				+ " xul.validation_in_progress as \"validationInProgress\", "
				+ " sp.created_time as \"sharedDate\",  cp1.company_name as \"partnerCompanyName\"   " + " From "
				+ " xt_user_profile u left join xt_user_list xul  on u.user_id = xul.assigned_by AND xul.module_name = 'SHARE LEADS' "
				+ " left join xt_user_userlist xuu on xul.user_list_id = xuu.user_list_id  "
				+ " left join  xt_user_profile u1 on u1.user_id = xuu.user_id "
				+ " left join xt_sharelist_partner sp on sp.user_list_id  = xul.user_list_id "
				+ " left join xt_sharelist_partner_mapping spm  on sp.id =spm.sharelist_partner_id "
				+ " left join xt_company_profile cp on cp.company_id = xul.assigned_company_id "
				+ " left join xt_company_profile cp1 on cp1.company_id = xul.company_id "
				+ " where xul.assigned_company_id= :companyId ";
		/******* XNFR-125 *****/
		List<Integer> excludedShareLeadsIds = new ArrayList<Integer>();
		sql = addQueryForOneClickLaunchVendorCampaign(pagination, sql, excludedShareLeadsIds);
		sql = addQueryForOneClickLaunchRedistributeCampaign(pagination, sql);
		sql = searchQueryString(pagination, sql);
		sql = sql
				+ " GROUP BY xul.user_list_id, xul.user_list_name, u.firstname, u.middle_name, u.lastname, u.email_id, cp.company_name, sp.user_list_id, sp.created_time, cp1.company_name ";
		sql = sortShareLists(pagination, sql);
		Query query = session.createSQLQuery(sql);
		query.setParameter("companyId", pagination.getCompanyId());
		setQueryParameterForOneClickLaunch(pagination, query, excludedShareLeadsIds);
		setQueryParameterForOneClickLaunchRedistribution(pagination, query);
		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		query.setMaxResults(pagination.getMaxResults());
		query.setResultTransformer(Transformers.aliasToBean(UserListDTO.class));
		List<UserListDTO> data = query.list();
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("totalRecords", totalRecords);
		if (pagination.isOneClickLaunch()) {
			resultMap.put("list", data);
		} else {
			resultMap.put("listOfUserLists", data);
		}
		return resultMap;
	}

	private String addQueryForOneClickLaunchVendorCampaign(Pagination pagination, String sql,
			List<Integer> excludedShareLeadsIds) {
		if (pagination.isChannelCampaign()) {
			sql += " and((xul.company_id is  null and xul.module_name = 'SHARE LEADS')\r\n"
					+ " or (xul.company_id = :partnerCompanyId  and  xul.module_name = 'SHARE LEADS'))  and xul.email_validation_ind";
			List<OneClickLaunchSharedLeadsDTO> list = findUnAssignedSharedLeadsListsInCampaign(
					pagination.getCompanyId());
			if (!list.isEmpty() && list != null) {
				Integer partnershipId = list.stream().map(OneClickLaunchSharedLeadsDTO::getPartnershipId)
						.collect(Collectors.toList()).get(0);
				if (partnershipId > 0 && !partnershipId.equals(pagination.getPartnershipId())) {
					List<Integer> shareLeadsIds = list.stream().map(OneClickLaunchSharedLeadsDTO::getUserListId)
							.collect(Collectors.toList());
					excludedShareLeadsIds.addAll(shareLeadsIds);
					sql += " and xul.user_list_id not in (:excludedUserListIds) ";
				}
			}
		}
		return sql;
	}

	private String addQueryForOneClickLaunchRedistributeCampaign(Pagination pagination, String sql) {
		if (pagination.isOneClickLaunch() || pagination.isPreviewSelectedSharedLeads()) {
			sql += " and xul.user_list_id in (:userListIds) ";
		}
		return sql;
	}

	private void setQueryParameterForOneClickLaunch(Pagination pagination, Query query,
			List<Integer> excludedShareLeadsIds) {
		if (pagination.isChannelCampaign()) {
			query.setParameter("partnerCompanyId", pagination.getPartnerCompanyId());
			if (!excludedShareLeadsIds.isEmpty()) {
				query.setParameterList("excludedUserListIds", excludedShareLeadsIds);
			}
		}
	}

	private void setQueryParameterForOneClickLaunchRedistribution(Pagination pagination, Query query) {
		if (pagination.isOneClickLaunch() || pagination.isPreviewSelectedSharedLeads()) {
			List<Integer> userListIdsByCampaignId = findSelectedUserListIdsByCampaignIds(
					pagination.getParentCampaignId());
			if (userListIdsByCampaignId != null && !userListIdsByCampaignId.isEmpty()) {
				query.setParameterList("userListIds", userListIdsByCampaignId);
			} else {
				query.setParameterList("userListIds", Arrays.asList(0));
			}

		}
	}

	private String sortShareLists(Pagination pagination, String sql) {
		if (pagination.getSortcolumn() != null) {
			if (pagination.getSortcolumn().equalsIgnoreCase("name")) {
				sql = sql + " order by xul.user_list_name " + pagination.getSortingOrder() + " NULLS LAST";
			} else if (pagination.getSortcolumn().equalsIgnoreCase("assignedTime")) {
				sql = sql + " order by sp.created_time " + pagination.getSortingOrder() + " NULLS LAST";
			} else {
				sql = sql + " order by xul.created_time " + pagination.getSortingOrder() + " NULLS LAST";
			}
		} else {
			boolean applySharedListSort = pagination.isChannelCampaign() && pagination.getCampaignId() != null
					&& pagination.getCampaignId() > 0;
			if (applySharedListSort) {
				sql = sortSelectedSharedList(pagination, sql);
			} else {
				sql = orderByCreatedTimeDescQueryString(sql);
			}
		}
		return sql;
	}

	/***** XNFR-125 ******/
	private String sortSelectedSharedList(Pagination pagination, String sql) {
		Integer partnershipId = pagination.getPartnershipId();
		Integer campaignId = pagination.getCampaignId();
		Integer selectedShareListPartnershipId = null;
		if (partnershipId.equals(selectedShareListPartnershipId)) {
			List<Integer> userListIdsByCampaignId = findSelectedUserListIdsByCampaignIds(campaignId);
			if (userListIdsByCampaignId != null && !userListIdsByCampaignId.isEmpty()) {
				sql += "   order by array_position(array" + userListIdsByCampaignId
						+ ", xul.user_list_id),xul.created_time DESC NULLS LAST";
			} else {
				sql = orderByCreatedTimeDescQueryString(sql);
			}
		} else {
			sql = orderByCreatedTimeDescQueryString(sql);
		}
		return sql;
	}

	/***** XNFR-125 ******/
	private String orderByCreatedTimeDescQueryString(String sql) {
		sql = sql + " order by xul.created_time DESC NULLS LAST";
		return sql;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> findUserListNamesByUserListIds(List<Integer> userListIds) {
		if (userListIds != null && !userListIds.isEmpty()) {
			if (userListIds.size() > 2) {
				userListIds = userListIds.subList(0, 2);
			}
			Session session = sessionFactory.getCurrentSession();
			String queryString = "select user_list_name from xt_user_list where user_list_id in (:userListIds)";
			Query query = session.createSQLQuery(queryString).setParameterList("userListIds", userListIds);
			return query.list();
		} else {
			return new ArrayList<>();
		}

	}

	@Override
	public UserListUsersCount getUserListUsersCount(Integer userListId) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(UserListUsersCount.class);
		criteria.add(Restrictions.eq("userListId", userListId));
		return (UserListUsersCount) criteria.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getUserListIds(List<Criterion> criterions, Pagination pagination) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(UserList.class, "UL");
		for (Criterion criterion : criterions) {
			criteria.add(criterion);
		}
		if (pagination != null && XamplifyUtils.isValidString(pagination.getSearchKey())) {
			criteria.createAlias("UL.userUserLists", "UUL", JoinType.LEFT_OUTER_JOIN);
			criteria.createAlias("UUL.user", "U", JoinType.LEFT_OUTER_JOIN);
			criteria.createAlias("UL.associatedCompany", "AC", JoinType.LEFT_OUTER_JOIN);
			criteria.createAlias("UUL.contactStatus", "CS", JoinType.LEFT_OUTER_JOIN);
			Disjunction disjunction = Restrictions.disjunction();
			String searchKey = pagination.getSearchKey();
			disjunction.add(Restrictions.ilike("UL.name", searchKey, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("UUL.contactCompany", searchKey, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("UUL.jobTitle", searchKey, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("UUL.address", searchKey, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("UUL.mobileNumber", searchKey, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("U.emailId", searchKey, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("AC.name", searchKey, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("CS.stageName", searchKey, MatchMode.ANYWHERE));
			searchKey = searchKey.trim();
			String[] nameWords = searchKey.split("\\s+");
			for (String nameWord : nameWords) {
				disjunction.add(Restrictions.ilike("UUL.firstName", nameWord, MatchMode.ANYWHERE));
				disjunction.add(Restrictions.ilike("UUL.lastName", nameWord, MatchMode.ANYWHERE));
			}
			criteria.add(disjunction);
			pagination.setIgnoreSearch(true);
		}
		getSortedUserListids(pagination, criteria);
		criteria.setProjection(Projections.property("id"));
		return criteria.list();
	}

	private void getSortedUserListids(Pagination pagination, org.hibernate.Criteria criteria) {
		if (pagination != null && pagination.getSortcolumn() != null) {
			if (SORTINGORDER.ASC == SORTINGORDER.valueOf(pagination.getSortingOrder())) {
				criteria.addOrder(Order.asc(pagination.getSortcolumn()).nulls(NullPrecedence.FIRST));
			} else if (SORTINGORDER.DESC == SORTINGORDER.valueOf(pagination.getSortingOrder())) {
				criteria.addOrder(Order.desc(pagination.getSortcolumn()).nulls(NullPrecedence.LAST));
			}
		} else {
			criteria.addOrder(Order.desc("id"));

		}
	}

	@Override
	public ReceiverMergeTagsDTO findReceiverMergeTagsInfo(Integer campaignId, Integer userId) {
		String queryString = " select coalesce(xuul.firstname,'') as \"firstName\",coalesce(xuul.lastname,'') as \"lastName\", concat (coalesce(xuul.firstname,''), ' ', coalesce(xuul.lastname,'')) as \"fullName\", "
				+ " xuul.contact_company as \"companyName\",xuul.mobile_number as \"mobileNumber\",xuul.address as \"address\",xuul.zip as \"zip\","
				+ " xuul.city as \"city\",xuul.state as \"state\",xuul.country as \"country\" from xt_campaign_user_userlist xcuul,xt_user_userlist xuul "
				+ " where xcuul.user_id = :userId	and xcuul.user_id = xuul.user_id and xuul.user_list_id = xcuul.user_list_id and "
				+ " xcuul.campaign_id = :campaignId order by xcuul.id asc limit 1";
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("campaignId", campaignId);
		query.setParameter("userId", userId);
		return (ReceiverMergeTagsDTO) query.setResultTransformer(Transformers.aliasToBean(ReceiverMergeTagsDTO.class))
				.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getNewlyAddedUserIdsForShareCampaign(Integer campaignId, Integer userListId) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select distinct user_id from xt_user_userlist uul where uul.user_list_id = :userListId "
				+ " EXCEPT  "
				+ " select distinct user_id from xt_campaign_user_userlist cuul where cuul.user_list_id = :userListId and cuul.campaign_id = :campaignId";
		Query query = session.createSQLQuery(queryString).setParameter("campaignId", campaignId)
				.setParameter("userListId", userListId);
		return query.list();
	}

	@Override
	public UserUserList getUserUserList(Integer userId, Integer userListId) {
		Query query;
		UserUserList userUserList = null;
		try {
			Session session = sessionFactory.getCurrentSession();
			String sql = "SELECT user_id, \r\n" + "firstname, \r\n" + "lastname,\r\n" + "description,\r\n"
					+ " country,\r\n" + "city,\r\n" + "address,\r\n" + "contact_company,\r\n" + "job_title,\r\n"
					+ " mobile_number, zip_code,state\n"
					+ " FROM xt_user_userlist WHERE user_list_id = :userListId AND user_id = :userId ";
			query = session.createSQLQuery(sql);
			query.setParameter("userId", userId);
			query.setParameter("userListId", userListId);
			Object result = query.uniqueResult();
			if (result == null) {
			}
			userUserList = constructUserUserList((Object[]) result);
		} catch (HibernateException e) {
			e.printStackTrace();
		}
		return userUserList;
	}

	@Override
	public Set<User> setSubscribeOrUnsubscribeUsers(Set<Integer> newlyAddedUserIds, Integer userListId,
			Integer companyId) {
		Set<User> users = new HashSet<>();
		Session session = sessionFactory.getCurrentSession();
		for (Integer userId : newlyAddedUserIds) {
			users.add(userDAO.findReceiverMergeTagsAndUnsubscribeStatus(userId, companyId, userListId, session));
		}
		return users;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> listSharedLists(Pagination pagination) {
		String condition = "";
		List<Integer> userListIds = sharedListUserIds(pagination.getUserId(), pagination.getPartnerCompanyId(),
				pagination.getVendorCompanyId(), pagination.isVanityUrlFilter(), pagination.isVanityUrlFilter());
		if (pagination.isVanityUrlFilter()) {
			condition = " where xul.company_id= :companyId and spm.partner_id = :partnerId and xul.assigned_company_id = :assignedCompanyId  and xul.user_list_id in (:userListIds) ";
		} else {
			condition = " where xul.company_id= :companyId and spm.partner_id = :partnerId and xul.user_list_id in (:userListIds) ";
		}
		Session session = sessionFactory.getCurrentSession();
		String sql = " SELECT xul.user_list_id  as \"id\", " + " xul.user_list_name as \"name\",  "
				+ " cp.company_name as \"companyName\",  " + " xul.created_time as \"createdTime\",  "
				+ " xul.assigned_by as \"uploadedUserId\",  "
				+ " cast(count(DISTINCT xuu.user_id) as integer) AS \"noOfContacts\",  "
				+ " xul.email_validation_ind as \"emailValidationInd\",  " + " CASE  "
				+ " WHEN length(btrim(concat(btrim(u.firstname), ' ',btrim(u.middle_name), ' ', btrim(u.lastname)))) > 0 THEN  "
				+ " btrim(concat(btrim(u.firstname), ' ',btrim(u.middle_name), ' ', btrim(u.lastname))) "
				+ " ELSE u.email_id  " + " END AS \"uploadedBy\",  " + " xul.social_network as \"socialNetwork\", "
				+ " xul.contact_type as \"contactType\", " + " CASE " + " WHEN sp.user_list_id   IS NOT NULL THEN true "
				+ " ELSE false " + " END AS \"assignedToPartner\", " + " sp.created_time as \"sharedDate\"  " + " From "
				+ " xt_user_profile u left join xt_user_list xul  on u.user_id = xul.assigned_by AND xul.module_name = 'SHARE LEADS' AND  xul.email_validation_ind = true "

				+ " left join xt_user_userlist xuu on xul.user_list_id = xuu.user_list_id  "
				+ " left join  xt_user_profile u1 on u1.user_id = xuu.user_id "
				+ " left join xt_sharelist_partner sp on sp.user_list_id  = xul.user_list_id "
				+ " left join xt_sharelist_partner_mapping spm  on sp.id =spm.sharelist_partner_id "
				+ " left join xt_company_profile cp on cp.company_id = xul.assigned_company_id " + condition;

		sql = searchQueryString(pagination, sql);
		sql = sql
				+ " GROUP BY xul.user_list_id, xul.user_list_name, u.firstname, u.middle_name, u.lastname, u.email_id, cp.company_name, sp.user_list_id, sp.created_time ";
		if (pagination.getSortcolumn() != null) {
			if (pagination.getSortcolumn().equalsIgnoreCase("name")) {
				sql = sql + " order by xul.user_list_name " + pagination.getSortingOrder() + " NULLS LAST";
			} else {
				sql = sql + " order by xul.created_time " + pagination.getSortingOrder() + " NULLS LAST";
			}
		} else {
			sql = orderByCreatedTimeDescQueryString(sql);
		}
		Query query = session.createSQLQuery(sql);
		if (pagination.isVanityUrlFilter()) {
			query.setParameter("assignedCompanyId", pagination.getVendorCompanyId());
		}
		query.setParameter("companyId", pagination.getPartnerCompanyId());
		query.setParameter("partnerId", pagination.getUserId());
		query.setParameterList("userListIds", userListIds);
		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		query.setMaxResults(pagination.getMaxResults());
		query.setResultTransformer(Transformers.aliasToBean(UserListDTO.class));
		List<UserListDTO> data = query.list();
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("totalRecords", totalRecords);
		resultMap.put("listOfUserLists", data);
		return resultMap;
	}

	private String searchQueryString(Pagination pagination, String sql) {
		if (XamplifyUtils.isValidString(pagination.getSearchKey())) {
			String searchKey = pagination.getSearchKey().toLowerCase();
			StringBuilder searchBuilder = new StringBuilder();
			searchBuilder.append(" and ( ");
			searchBuilder.append("LOWER(u1.email_id) like '%" + searchKey + "%' ");
			searchBuilder.append(" or LOWER(xul.user_list_name) like '%" + searchKey + "%' ");
			searchBuilder.append(" or LOWER(xuu.contact_company) like '%" + searchKey + "%' ");
			searchBuilder.append(" or LOWER(xuu.job_title) like '%" + searchKey + "%' ");
			searchBuilder.append(" or LOWER(xuu.address) like '%" + searchKey + "%' ");
			searchBuilder.append(" or LOWER(xuu.mobile_number) like '%" + searchKey + "%' ");
			searchKey = searchKey.trim();
			String[] nameWords = searchKey.split("\\s+");
			if (nameWords.length == 1) {
				searchBuilder.append(" or LOWER(xuu.firstname) like '%" + searchKey + "%' ");
				searchBuilder.append(" or LOWER(xuu.lastname) like '%" + searchKey + "%' ");
				searchBuilder.append(
						" or REPLACE(LOWER(xuu.firstname || xuu.lastname), ' ', '') " + "like '%" + searchKey + "%' ");
				searchBuilder.append(
						" or REPLACE(LOWER(xuu.lastname || xuu.firstname), ' ', '') " + "like '%" + searchKey + "%' ");
			} else {
				for (String nameWord : nameWords) {
					searchBuilder.append(" or LOWER(xuu.firstname) like '%" + nameWord + "%' ");
					searchBuilder.append(" or LOWER(xuu.lastname) like '%" + nameWord + "%' ");
				}
			}
			searchBuilder.append(" ) ");
			sql = sql + String.valueOf(searchBuilder);
		}
		return sql;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getSharedPartnerIds(Integer userListId, Integer partnershipId) {
		if (XamplifyUtils.isValidInteger(userListId) && XamplifyUtils.isValidInteger(partnershipId)) {
			Session session = sessionFactory.getCurrentSession();
			String sql = "select spm.partner_id from xt_user_list ul, xt_sharelist_partner sp, xt_sharelist_partner_mapping spm "
					+ " where ul.user_list_id = sp.user_list_id  and sp.id = spm.sharelist_partner_id "
					+ " and  ul.user_list_id=:userListId and sp.partnership_id  = :partnershipId ";
			Query query = session.createSQLQuery(sql);
			query.setParameter("partnershipId", partnershipId);
			query.setParameter("userListId", userListId);
			return query.list();
		} else {
			return Collections.emptyList();
		}

	}

	@Override
	public void deleteShareList(Integer vendorId, List<Integer> partnerIds) {
		Integer vendorCompanyId = userDAO.getCompanyIdByUserId(vendorId);
		for (Integer partnerId : partnerIds) {
			Integer partnerCompanyId = userDAO.getCompanyIdByUserId(partnerId);
			Integer partnershipId = partnershipDAO.getPartnershipIdByVendorCompanyIdAndPartnerCompanyId(vendorCompanyId,
					partnerCompanyId);
			List<Object[]> shareListPartnerObjs = getShareListPartnerIds(partnershipId);
			for (Object[] row : shareListPartnerObjs) {
				updateShareListCompanyIdAsNull((Integer) row[1]);
				deleteShareListPartnerMapping((Integer) row[0]);
				deleteShareListPartner((Integer) row[0]);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getShareListPartnerIds(Integer partnershipId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select id, user_list_id from xt_sharelist_partner where partnership_id=" + partnershipId;
		Query query = session.createSQLQuery(sql);
		return query.list();
	}

	@Override
	public Integer getShareListPartnerId(Integer partnershipId, Integer userListId) {
		Session session = sessionFactory.getCurrentSession();
		String hql = " select id from ShareListPartner where partnership.id=" + partnershipId + " and userList.id="
				+ userListId;
		Query query = session.createQuery(hql);
		return (Integer) query.uniqueResult();
	}

	public void deleteShareListPartner(Integer shareListPartnerId) {
		Session session = sessionFactory.getCurrentSession();
		session.createSQLQuery("delete from xt_sharelist_partner where id  = " + shareListPartnerId).executeUpdate();
	}

	@Override
	public void deleteShareListPartnerMapping(Integer shareListPartnerId) {
		Session session = sessionFactory.getCurrentSession();
		session.createSQLQuery(
				"delete from xt_sharelist_partner_mapping where sharelist_partner_id = " + shareListPartnerId)
				.executeUpdate();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> listSharedLeadsListIds(boolean isVanityUrlFilter, Integer partnerId,
			Integer assignedByCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = " select distinct ul.user_list_id from xt_user_list ul,xt_sharelist_partner sp, xt_sharelist_partner_mapping spm "
				+ " where ul.user_list_id = sp.user_list_id and sp.id = spm.sharelist_partner_id and ul.email_validation_ind=true "
				+ " and spm.partner_id = " + partnerId;
		if (isVanityUrlFilter) {
			sql = sql + " and ul.assigned_company_id=" + assignedByCompanyId;
		}
		Query query = session.createSQLQuery(sql);
		return query.list();
	}

	// This method needs to be removed
	@Override
	public void updateUserList(Integer userListId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "update xt_user_list set customer_id=null, assigned_date=null where  user_list_id=" + userListId;
		Query query = session.createSQLQuery(sql);
		query.executeUpdate();
	}

	@Override
	public UserList getUserListByNameAndCompany(String name, Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(UserList.class);
		criteria.add(Restrictions.eq("name", name).ignoreCase());
		criteria.add(Restrictions.eq("company.id", companyId));
		return (UserList) criteria.uniqueResult();
	}

	@Override
	public UserList getFormContactListForOrgAdmin(Integer formId, Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(UserList.class);
		criteria.add(Restrictions.eq("form.id", formId));
		criteria.add(Restrictions.eq("company.id", companyId));
		return (UserList) criteria.uniqueResult();
	}

	@Override
	public UserList getFormContactListByLandingPageId(Integer formId, Integer companyId, Integer landingPageId) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(UserList.class);
		criteria.add(Restrictions.eq("form.id", formId));
		criteria.add(Restrictions.eq("company.id", companyId));
		criteria.add(Restrictions.eq("landingPage.id", landingPageId));
		return (UserList) criteria.uniqueResult();
	}

	@Override
	public UserList getFormContactListByCampaignId(Integer formId, Integer campaignId) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(UserList.class);
		criteria.add(Restrictions.eq("form.id", formId));
		criteria.add(Restrictions.eq("campaign.id", campaignId));
		return (UserList) criteria.uniqueResult();
	}

	public Integer findAllPartnersCountByUserId(Integer userId) {
		Session session = sessionFactory.getCurrentSession();
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(userId, true, false);
		boolean applyTeamMemberFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
		if (teamMemberFilterDTO.isEmptyFilter()) {
			return 0;
		} else {
			if (applyTeamMemberFilter) {
				return teamDao.getAssignedPartnersCount(userId);
			} else {
				Integer companyId = userDAO.getCompanyIdByUserId(userId);
				String queryString = " select  count(distinct up.user_id) as all from xt_user_list u, xt_user_userlist uul, xt_user_profile up  "
						+ " where u.user_list_id =uul.user_list_id and 	 uul.user_id=up.user_id and up.email_validation_ind=true and u.user_list_id  in "
						+ " (select user_list_id from xt_user_list where company_id = :companyId and is_default_partnerlist )";
				Query query = session.createSQLQuery(queryString);
				query.setParameter("companyId", companyId);
				return query.uniqueResult() != null ? ((BigInteger) query.uniqueResult()).intValue() : 0;
			}

		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getListSharedCompanyDetails(Integer userListId, Pagination pagination) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "SELECT  distinct "
				+ " u.firstname as \"firstName\", u.lastname as \"lastName\", u.email_id as \"emailId\", "
				+ " xul.user_list_name as \"name\",cp.company_name as \"company\", "
				+ " xul.created_time as \"createdDate\", sp.created_time as \"sharedDate\" " + " from "
				+ " xt_user_list xul " + " left join xt_sharelist_partner sp on sp.user_list_id  = xul.user_list_id "
				+ " left join xt_sharelist_partner_mapping spm  on sp.id =spm.sharelist_partner_id "
				+ " left join xt_user_profile u on spm.partner_id = u.user_id "
				+ " left join xt_company_profile cp on cp.company_id = u.company_id " + " where xul.user_list_id  = "
				+ userListId + " GROUP BY xul.user_list_name,u.firstname, u.lastname, "
				+ " u.email_id, xul.created_time,sp.created_time, cp.company_name ";
		Query query = session.createSQLQuery(sql);
		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		query.setMaxResults(pagination.getMaxResults());
		query.setResultTransformer(Transformers.aliasToBean(SharedDetailsDTO.class));
		List<SharedDetailsDTO> data = query.list();
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("totalRecords", totalRecords);
		resultMap.put("list", data);
		return resultMap;
	}

	@Override
	public boolean isShareLeadsListAssignedToPartner(Integer partnerId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select count(*) from xt_sharelist_partner_mapping spm where spm.partner_id =" + partnerId;
		Query query = session.createSQLQuery(sql);
		return query.uniqueResult() != null && ((BigInteger) query.uniqueResult()).intValue() > 0 ? true : false;
	}

	@Override
	public boolean isShareLeadsListAssignedToPartner(Integer partnershipId, Integer partnerId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select count(sp.id) from xt_sharelist_partner sp, xt_sharelist_partner_mapping spm "
				+ " where sp.id = spm.sharelist_partner_id and sp.partnership_id = " + partnershipId
				+ " and spm.partner_id= " + partnerId;
		Query query = session.createSQLQuery(sql);
		return query.uniqueResult() != null && ((BigInteger) query.uniqueResult()).intValue() > 0 ? true : false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ShareListPartner> getShareListPartner(Integer partnershipId) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(ShareListPartner.class);
		criteria.add(Restrictions.eq("sharedToCompany", true));
		criteria.add(Restrictions.eq("partnership.id", partnershipId));
		return criteria.list();
	}

	@Override
	public boolean isUserListNameExists(UserListDTO userListDTO) {
		Session session = sessionFactory.getCurrentSession();
		String sql = " ";
		if (userListDTO.isPartnerUserList()) {
			sql = "select count(*) from xt_user_list where user_list_name = '" + userListDTO.getName()
					+ "' and is_partner_userlist = true "
					+ " and contact_type in ('CONTACT') and company_id=:companyId ";
		} else if (userListDTO.isAssignedLeadsList()) {
			sql = " select count(*) from xt_user_list where user_list_name = '" + userListDTO.getName()
					+ "' and is_partner_userlist = false  "
					+ " and module_name='SHARE LEADS' and assigned_company_id=:companyId ";
		} else {
			sql = " select count(*) from xt_user_list where user_list_name = '" + userListDTO.getName()
					+ "' and is_partner_userlist = false "
					+ " and contact_type in ('CONTACT', 'LEAD','CONTACT_LISTVIEWS', 'LEAD_LISTVIEWS') and company_id=:companyId ";
		}
		Query query = session.createSQLQuery(sql);
		query.setParameter("companyId", userListDTO.getCompanyId());
		return query.uniqueResult() != null && ((BigInteger) query.uniqueResult()).intValue() > 0 ? true : false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getUnsubscribedUsers(Integer customerCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(UnsubscribedUser.class);
		criteria.add(Restrictions.eq("customerCompanyId", customerCompanyId));
		criteria.setProjection(Projections.property("userId"));
		return criteria.list();
	}

	@Override
	public UserList findActivePartnerListByCompanyId(Integer companyId) {
		return findActiveOrInactiveMasterPartnerList(companyId, activePartnerListName);
	}

	@Override
	public UserList findInActivePartnerListByCompanyId(Integer companyId) {
		return findActiveOrInactiveMasterPartnerList(companyId, inActivePartnerListName);
	}

	private UserList findActiveOrInactiveMasterPartnerList(Integer companyId, String activeOrInActivePartnerListName) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(UserList.class);
		criteria.add(Restrictions.eq("company.id", companyId));
		criteria.add(Restrictions.eq("name", activeOrInActivePartnerListName));
		return (UserList) criteria.uniqueResult();
	}

	@Override
	public boolean isActiveMasterPartnerListExists(Integer companyId) {
		return isActiveOrInactiveMasterPartnerListExists(companyId, activePartnerListName);
	}

	private boolean isActiveOrInactiveMasterPartnerListExists(Integer companyId, String userListName) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select case when count(user_list_id)>0 then true else false end as result from xt_user_list where company_id = :companyId "
				+ " and user_list_name = :userListName";
		Query query = session.createSQLQuery(sql);
		query.setParameter("companyId", companyId);
		query.setParameter("userListName", userListName);
		return (boolean) query.uniqueResult();
	}

	@Override
	public boolean isInActiveMasterPartnerListExists(Integer companyId) {
		return isActiveOrInactiveMasterPartnerListExists(companyId, inActivePartnerListName);
	}

	@Override
	public void saveUserUserList(UserList userList, UserUserListDTO userListDTO, User user) {
		Session session = sessionFactory.getCurrentSession();
		UserUserList userUserList = new UserUserList();
		userUserList.setUser(user);
		userUserList.setUserList(userList);
		userUserList.setCountry(userListDTO.getCountry());
		userUserList.setCity(userListDTO.getCity());
		userUserList.setAddress(userListDTO.getAddress());
		userUserList.setContactCompany(userListDTO.getContactCompany());
		userUserList.setJobTitle(userListDTO.getJobTitle());
		userUserList.setFirstName(userListDTO.getFirstName());
		userUserList.setLastName(userListDTO.getLastName());
		userUserList.setMobileNumber(userListDTO.getMobileNumber());
		userUserList.setState(userListDTO.getState());
		userUserList.setZipCode(userListDTO.getZip());
		userUserList.setVertical(userListDTO.getVertical());
		userUserList.setRegion(userListDTO.getRegion());
		userUserList.setPartnerType(userListDTO.getPartnerType());
		userUserList.setCategory(userListDTO.getCategory());

		String sql = "insert into xt_user_userlist(user_list_id,user_id,country,city,address,contact_company,job_title,firstname,lastname,mobile_number,state,zip,vertical,region,"
				+ "partner_type,category) values(:userListId,:userId,:country,:city,:address,:contactCompany,:jobTitle,:firstName,:lastName,:mobileNumber,:state,:zip,:vertical,:region,:partnerType,:category)";

		Query userListQuery = session.createSQLQuery(sql);
		userListQuery.setParameter("userListId", userList.getId());
		userListQuery.setParameter("userId", user.getUserId());
		userListQuery.setParameter("country", userListDTO.getCountry());
		userListQuery.setParameter("city", userListDTO.getCity());
		userListQuery.setParameter("address", userListDTO.getAddress());
		userListQuery.setParameter("contactCompany", userListDTO.getContactCompany());
		userListQuery.setParameter("jobTitle", userListDTO.getJobTitle());
		userListQuery.setParameter("firstName", userListDTO.getFirstName());
		userListQuery.setParameter("lastName", userListDTO.getLastName());
		userListQuery.setParameter("mobileNumber", userListDTO.getMobileNumber());
		userListQuery.setParameter("state", userListDTO.getState());
		userListQuery.setParameter("zip", userListDTO.getZip());
		userListQuery.setParameter("vertical", userListDTO.getVertical());
		userListQuery.setParameter("region", userListDTO.getRegion());
		userListQuery.setParameter("partnerType", userListDTO.getPartnerType());
		userListQuery.setParameter("category", userListDTO.getCategory());
		userListQuery.executeUpdate();

		/*
		 * String userLegalBasisQuery =
		 * "insert into xt_user_legal_basis(legal_basis_id,user_userlist_id) values(:legalBasisId,:userUserListId)"
		 * ; for (Integer legalBasisId : userListDTO.getLegalBasis()) { boolean
		 * isLegalBasisExists = isLegalBasisExists(user.getUserId(), userList.getId(),
		 * legalBasisId); if (!isLegalBasisExists) { Query legalBasisQuery =
		 * session.createSQLQuery(userLegalBasisQuery);
		 * legalBasisQuery.setParameter("legalBasisId", legalBasisId);
		 * legalBasisQuery.setParameter("userUserListId", user.getUserId());
		 * legalBasisQuery.executeUpdate(); }
		 * 
		 * }
		 */

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getUserListIdsByRemovePartnerIds(Integer companyId, List<Integer> userIds) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select distinct u.user_list_id  from xt_user_list u,  xt_user_userlist uul "
				+ " where u.user_list_id=uul.user_list_id and uul.user_id in :userIds "
				+ " and u.email_validation_ind = true and u.is_partner_userlist=true and u.company_id=:companyId";
		Query query = session.createSQLQuery(sql);
		query.setParameterList("userIds", userIds);
		query.setParameter("companyId", companyId);
		return (List<Integer>) query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getTeamMemberGroupedPartnerIds(Integer teamMemberId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = " select distinct p.partner_id from " + " xt_team_member t "
				+ " left outer join xt_team_member_group_user_mapping tgum on t.id = tgum.team_member_id "
				+ " left outer join xt_partner_team_group_mapping ptgm on tgum.id = ptgm.team_member_group_user_mapping_id "
				+ " left outer join xt_partnership p on ptgm.partnership_id=p.id "
				+ " where t.team_member_id = :teamMemberId and p.partner_id is not null";

		Query query = session.createSQLQuery(sql);
		query.setParameter("teamMemberId", teamMemberId);
		List<Integer> list = query.list() != null ? (List<Integer>) query.list() : null;
		return list;
	}

	@Override
	public Integer findUserListIdByTeamMemberId(Integer teamMemberId, Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select user_list_id from xt_user_list where team_member_id = :teamMemberId and is_partner_userlist = true "
				+ " and contact_type in ('CONTACT') and company_id=:companyId ";
		Query query = session.createSQLQuery(queryString);
		query.setParameter("teamMemberId", teamMemberId);
		query.setParameter("companyId", companyId);
		return (Integer) query.uniqueResult();
	}

	@Override
	public void updateTeamMemberPartnerList(Integer userListId) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "update xt_user_list set is_team_member_partner_list = true where user_list_id=:userListId";
		Query query = session.createSQLQuery(queryString);
		query.setParameter("userListId", userListId);
		query.executeUpdate();
	}

	@Override
	public boolean isTeamMemberPartnerList(Integer userListId) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select is_team_member_partner_list from xt_user_list where user_list_id=:userListId";
		Query query = session.createSQLQuery(queryString);
		query.setParameter("userListId", userListId);
		return (boolean) query.uniqueResult();
	}

	@Override
	public void updateTeamMemberPartnerListName(Integer teamMemberId, String fullName) {
		if (StringUtils.hasText(fullName)) {
			Session session = sessionFactory.getCurrentSession();
			String fullNameSubString = "";
			if (fullName.length() > 150) {
				fullNameSubString = fullName.substring(0, 150);
			} else {
				fullNameSubString = fullName;
			}
			long milliSeconds = System.nanoTime();
			String partnerListName = fullNameSubString + "-" + teamMemberId + "_" + milliSeconds
					+ "-Master Partner Group";
			String queryString = "update xt_user_list set user_list_name = :userListName where team_member_id=:teamMemberId";
			Query query = session.createSQLQuery(queryString);
			query.setParameter("userListName", partnerListName);
			query.setParameter("teamMemberId", teamMemberId);
			query.executeUpdate();
		}

	}

	@Override
	public void deleteUserUserListByUserListId(Integer userListId) {
		Session session = sessionFactory.getCurrentSession();
		session.createSQLQuery("delete from xt_user_userlist where user_list_id = " + userListId).executeUpdate();
		session.createSQLQuery("delete from xt_user_legal_basis where user_list_id = " + userListId).executeUpdate();

	}

	@Override
	public void deletePartnerFromTeamMemberPartnerListByPartnerIdAndTeamMemberIds(Integer partnerId,
			List<Integer> teamMemberIds) {
		if (teamMemberIds != null && !teamMemberIds.isEmpty()) {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createSQLQuery(
					"delete from xt_user_userlist where user_id = :partnerId and user_list_id in (select user_list_id from xt_user_list where team_member_id in (:teamMemberIds))");
			query.setParameter("partnerId", partnerId);
			query.setParameterList("teamMemberIds", teamMemberIds);
			query.executeUpdate();
		}

	}

	@Override
	public UserDTO findPartnerDetailsByPartnerListId(Integer partnerListId, Integer partnerId) {
		Session session = sessionFactory.getCurrentSession();
		String sqlString = "select country as \"country\", city as \"city\", address as \"address\", contact_company as \"contactCompany\", job_title as \"jobTitle\",firstname as \"firstName\",lastname as \"lastName\","
				+ " mobile_number as \"mobileNumber\", state as \"state\", zip as \"zipCode\", vertical as \"vertical\", region as \"region\", partner_type as \"partnerType\", category as \"category\" from xt_user_userlist where user_list_id=:partnerListId and user_id=:partnerId ";
		Query query = session.createSQLQuery(sqlString);
		query.setParameter(PARTNER_LIST_ID, partnerListId);
		query.setParameter("partnerId", partnerId);
		UserDTO userDto = (UserDTO) query.setResultTransformer(Transformers.aliasToBean(UserDTO.class)).uniqueResult();
		setLegalBasisOptions(partnerListId, partnerId, session, userDto);
		return userDto;
	}

	@SuppressWarnings("unchecked")
	private void setLegalBasisOptions(Integer partnerListId, Integer partnerId, Session session, UserDTO userDto) {
		if (userDto != null) {
			String legalBasisStringQuery = " select distinct lb.legal_basis_id from xt_user_legal_basis lb, xt_user_userlist uul "
					+ " where lb.user_userlist_id = uul.id and uul.user_list_id = :partnerListId and uul.user_id = :partnerId";
			Query legalBasisQuery = session.createSQLQuery(legalBasisStringQuery);
			legalBasisQuery.setParameter(PARTNER_LIST_ID, partnerListId);
			legalBasisQuery.setParameter("partnerId", partnerId);
			List<Integer> legalBasisIds = legalBasisQuery.list();
			userDto.setLegalBasis(legalBasisIds);
		}
	}

	@Override
	public boolean isPartnerExistsInPartnerList(Integer partnerId, Integer partnerListId) {
		Session session = sessionFactory.getCurrentSession();
		String sqlString = "select case when count(*)>0 then true else false end as partnerExists from xt_user_userlist where user_list_id = :partnerListId and user_id = :partnerId";
		Query query = session.createSQLQuery(sqlString);
		query.setParameter(PARTNER_LIST_ID, partnerListId);
		query.setParameter("partnerId", partnerId);
		return (boolean) query.uniqueResult();
	}

	@Override
	public Integer findUserListIdByTeamMemberId(Integer teamMemberId) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select user_list_id from xt_user_list where team_member_id=:teamMemberId ";
		Query query = session.createSQLQuery(queryString);
		query.setParameter("teamMemberId", teamMemberId);
		return (Integer) query.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findUserListIdsByTeamMemberIds(List<Integer> teamMemberIds) {
		if (teamMemberIds != null && !teamMemberIds.isEmpty()) {
			Session session = sessionFactory.getCurrentSession();
			String queryString = "select user_list_id from xt_user_list where team_member_id in (:teamMemberIds)";
			Query query = session.createSQLQuery(queryString);
			query.setParameterList("teamMemberIds", teamMemberIds);
			return query.list();
		} else {
			return new ArrayList<>();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findTeamMemberPartnerListIdsByCompanyId(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select user_list_id from xt_user_list where company_id = :companyId and is_team_member_partner_list";
		Query query = session.createSQLQuery(queryString);
		query.setParameter("companyId", companyId);
		return query.list();
	}

	@Override
	public void deleteFromUserUserListByUserIdAndUserListIds(List<Integer> partnerIds, List<Integer> userListIds) {
		if (partnerIds != null && !partnerIds.isEmpty() && userListIds != null && !userListIds.isEmpty()) {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createSQLQuery(
					"delete from xt_user_userlist where user_id in (:userIds) and user_list_id in (:userListIds)");
			query.setParameterList("userIds", partnerIds);
			query.setParameterList("userListIds", userListIds);
			query.executeUpdate();
			/******** Legal Basis ************/
			/*
			 * Query legalBasisQuery = session.createSQLQuery(
			 * "delete from xt_user_legal_basis where user_id in (:userIds) and user_list_id in (:userListIds)"
			 * ); legalBasisQuery.setParameterList("userIds", partnerIds);
			 * legalBasisQuery.setParameterList("userListIds", userListIds);
			 * legalBasisQuery.executeUpdate();
			 */
		}
	}

	@Override
	public void deleteUsersFromUserListByUserListIdAndUserIds(Integer userListId, List<Integer> userIds) {
		if (userListId != null && userIds != null && !userIds.isEmpty()) {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createSQLQuery(
					"delete from xt_user_userlist where user_list_id = :userListId and user_id in (:userIds)");
			query.setParameter("userListId", userListId);
			query.setParameterList("userIds", userIds);
			query.executeUpdate();
			/******** Legal Basis ************/
			/*
			 * Query legalBasisQuery = session.createSQLQuery(
			 * "delete from xt_user_legal_basis where user_list_id = :userListId and user_id in (:userIds)"
			 * ); legalBasisQuery.setParameter("userListId", userListId);
			 * legalBasisQuery.setParameterList("userIds", userIds);
			 * legalBasisQuery.executeUpdate();
			 */
		}
	}

	@Override
	public UserDTO findPartnerDetailsByPartnershipId(Integer partnershipId) {
		Session session = sessionFactory.getCurrentSession();
		String sqlString = "select uul.user_id as \"id\",uul.user_list_id as \"userListId\", country as \"country\", city as \"city\", address as \"address\", contact_company as \"contactCompany\", job_title as \"jobTitle\",firstname as \"firstName\",lastname as \"lastName\","
				+ " mobile_number as \"mobileNumber\", state as \"state\", zip as \"zipCode\", vertical as \"vertical\", region as \"region\", partner_type as \"partnerType\", category as \"category\"  from xt_user_userlist uul,xt_user_list ul,xt_partnership p "
				+ " where p.vendor_company_id = ul.company_id and ul.user_list_id = uul.user_list_id "
				+ " and p.partner_id = uul.user_id and ul.is_default_partnerlist and p.id = :partnershipId ";
		Query query = session.createSQLQuery(sqlString);
		query.setParameter("partnershipId", partnershipId);
		UserDTO userDto = (UserDTO) query.setResultTransformer(Transformers.aliasToBean(UserDTO.class)).uniqueResult();
		setLegalBasisOptions(userDto.getUserListId(), userDto.getId(), session, userDto);
		return userDto;
	}

	public boolean isContactUsedInAnyLaunchedCampaigns(Integer userId, Integer companyId) {
		boolean result = false;
		Session session = sessionFactory.openSession();
		try {
			String sqlString = " select cast(count(c.campaign_id) as integer) from " + " xt_campaign c "
					+ " left outer join xt_campaign_user_userlist cuul on c.campaign_id=cuul.campaign_id and c.is_launched = true  and c.is_nurture_campaign = true "
					+ " left outer join xt_user_list u on cuul.user_list_id = u.user_list_id "
					+ " left outer join xt_user_userlist uul on u.user_list_id=uul.user_list_id and cuul.user_id=uul.user_id "
					+ " left outer join xt_user_profile up on c.customer_id=up.user_id " + " where uul.user_id="
					+ userId + " and up.company_id=" + companyId + " and u.company_id=" + companyId;
			SQLQuery query = session.createSQLQuery(sqlString);
			Integer count = (Integer) query.uniqueResult();
			if (count > 0) {
				result = true;
			}
			session.clear();
		} catch (HibernateException e) {
			logger.error("error occured in isContactUsedInAnyLaunchedCampaigns(): " + e.getMessage());
		} finally {
			session.close();
		}
		return result;
	}

	public boolean isUserExists(Integer userListId, String emailId) {
		boolean result = false;
		Session session = sessionFactory.openSession();
		try {
			String sqlString = "select count(uul.id)>0 from xt_user_userlist uul,xt_user_profile up\n"
					+ "where LOWER(up.email_id) = LOWER(':emailId') and uul.user_id = up.user_id and uul.user_list_id = :userListId\n";
			SQLQuery query = session.createSQLQuery(sqlString);
			Integer count = query.uniqueResult() != null ? (Integer) query.uniqueResult() : 0;
			if (count > 0) {
				result = true;
			}
			session.flush();
			session.clear();
		} catch (Exception e) {
			// Do Nothing
		} finally {
			session.close();
		}
		return result;
	}

	@Override
	public void deleteLegalBasis(Session session, Integer userId, List<Integer> userListIds) {
		String sql = "delete from xt_user_legal_basis where user_id = :userId and user_list_id in  (:userListIds) ";
		Query query = session.createSQLQuery(sql);
		query.setParameter("userId", userId);
		query.setParameterList("userListIds", userListIds);
		query.executeUpdate();
	}

	@Override
	public Integer findShareListPartnerIdByUserListId(Integer userListId) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select id from xt_sharelist_partner where user_list_id = :userListId";
		Query query = session.createSQLQuery(queryString);
		query.setParameter("userListId", userListId);
		return (Integer) query.uniqueResult();
	}

	@Override
	public void updateSharedToCompanyById(Integer id) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "update xt_sharelist_partner set is_shared_to_company = true where id = :id";
		Query query = session.createSQLQuery(queryString);
		query.setParameter("id", id);
		query.executeUpdate();
	}

	@Override
	public void updateShareListCompanyIdAsNull(Integer shareListId) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "update xt_user_list set company_id = null where user_list_id = :shareListId";
		Query query = session.createSQLQuery(queryString);
		query.setParameter("shareListId", shareListId);
		query.executeUpdate();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<OneClickLaunchSharedLeadsDTO> findUnAssignedSharedLeadsListsInCampaign(Integer companyId) {
		String queryString = " select distinct coalesce(c.partnership_id,0) as \"partnershipId\", ul.user_list_id as \"userListId\",ul.user_list_name as \"userListName\" "
				+ " from xt_campaign c,xt_campaign_user_userlist cuul,xt_user_list ul,xt_user_profile up "
				+ " where ul.user_list_id = cuul.user_list_id and c.campaign_id = cuul.campaign_id "
				+ " and c.is_channel_campaign  and is_launched = false and c.one_click_launch "
				+ " and ul.assigned_company_id = :companyId and ul.module_name = :moduleName and ul.company_id is null ";
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(queryString);
		query.setParameter("companyId", companyId);
		query.setParameter("moduleName", shareLeadsModuleName);
		return (List<OneClickLaunchSharedLeadsDTO>) paginationUtil.getListDTO(OneClickLaunchSharedLeadsDTO.class,
				query);
	}

	@Override
	public Integer findOneClickLaunchCampaignPartnershipIdByUserListId(Integer userListId) {
		String queryString = " select distinct c.partnership_id	from xt_campaign c,xt_campaign_user_userlist cuul,xt_user_list ul "
				+ " where ul.user_list_id = cuul.user_list_id and c.campaign_id = cuul.campaign_id 	and c.is_channel_campaign = true and is_launched = false"
				+ " and c.one_click_launch = true and ul.user_list_id = :userListId";
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(queryString);
		query.setParameter("userListId", userListId);
		return (Integer) query.uniqueResult();
	}

	@Override
	public Integer findOneClickLaunchCampaignPartnershipIdByUserListIds(List<Integer> userListIds) {
		if (userListIds != null && !userListIds.isEmpty()) {
			String queryString = " select distinct c.partnership_id	from xt_campaign c,xt_campaign_user_userlist cuul,xt_user_list ul "
					+ " where ul.user_list_id = cuul.user_list_id and c.campaign_id = cuul.campaign_id 	and c.is_channel_campaign = true and is_launched = false"
					+ " and c.one_click_launch = true and ul.user_list_id in (:userListIds)";
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createSQLQuery(queryString);
			query.setParameterList("userListIds", userListIds);
			return (Integer) query.uniqueResult();
		} else {
			return null;
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<UserUserListDTO> findSubscribedUserUserLists(List<Integer> unsubscribedUserIds,
			List<Integer> userListIds) {
		if (userListIds != null && !userListIds.isEmpty()) {
			Session session = sessionFactory.getCurrentSession();
			String queryString = "select user_list_id as \"userListId\", user_id as \"userId\", contact_company as \"contactCompany\", firstname as \"firstName\", lastname as \"lastName\", mobile_number as \"mobileNumber\" from xt_user_userlist where user_list_id in (:userListIds)";
			boolean unsubscribeUsersExists = unsubscribedUserIds != null && !unsubscribedUserIds.isEmpty();
			if (unsubscribeUsersExists) {
				queryString += " and user_id not in (:unsubscribeUserIds)";
			}
			Query query = session.createSQLQuery(queryString);
			query.setParameterList("userListIds", userListIds);
			if (unsubscribeUsersExists) {
				query.setParameterList("unsubscribeUserIds", unsubscribedUserIds);
			}
			query.setParameterList("userListIds", userListIds);
			List<UserUserListDTO> subscribedUserUserLists = (List<UserUserListDTO>) paginationUtil
					.getListDTO(UserUserListDTO.class, query);
			if (subscribedUserUserLists != null) {
				return subscribedUserUserLists;
			} else {
				return Collections.EMPTY_LIST;
			}
		} else {
			return Collections.EMPTY_LIST;
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findValidEmailUserIdsByUserListIds(List<Integer> userListIds) {
		if (userListIds != null && !userListIds.isEmpty()) {
			Session session = sessionFactory.getCurrentSession();
			String queryString = "select distinct u.user_id from xt_user_userlist uul,xt_user_profile u where uul.user_list_id in (:userListIds) and u.user_id = uul.user_id and u.is_email_valid";
			Query query = session.createSQLQuery(queryString);
			query.setParameterList("userListIds", userListIds);
			return query.list();
		} else {
			return Collections.EMPTY_LIST;
		}
	}

	@Override
	public boolean isDuplicateListName(String listName, Integer companyId, String module) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session
				.createSQLQuery("SELECT CASE WHEN  count(*) > 0 THEN true ELSE false END from xt_user_list ul "
						+ " where ul.user_list_name ilike :listName and ul.company_id = :companyId and ul.module_name = :module");
		query.setParameter("listName", listName);
		query.setParameter("companyId", companyId);
		query.setParameter("module", module);
		return (boolean) query.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findUserIdsByUserListId(Integer userListId) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select distinct uul.user_id from xt_user_userlist uul,xt_user_list ul where ul.user_list_id = uul.user_list_id and ul.user_list_id=:userListId";
		Query query = session.createSQLQuery(queryString);
		query.setParameter("userListId", userListId);
		return query.list();

	}

	@Override
	public boolean hasUserUserList(Integer userId, Integer userListId) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session
				.createSQLQuery("SELECT CASE WHEN  count(*) > 0 THEN true ELSE false END from xt_user_userlist ul "
						+ " where ul.user_id = :userId and ul.user_list_id = :userListId");
		query.setParameter("userId", userId);
		query.setParameter("userListId", userListId);
		return (boolean) query.uniqueResult();
	}

	@Override
	public boolean isAtLeastOneShareLeadsListSharedByVendorCompanyWithPartnerCompany(Integer vendorCompanyId,
			Integer partnerCompanyId, Integer partnerId) {
		return isShareListShared(partnerId, partnerCompanyId, vendorCompanyId, true);
	}

	@Override
	public boolean isAtLeastOneShareLeadsListSharedByVendorCompanyWithPartnerCompany(Integer partnerCompanyId,
			Integer partnerId) {
		return isShareListShared(partnerId, partnerCompanyId, 0, false);
	}

	private boolean isShareListShared(Integer partnerId, Integer partnerCompanyId, Integer vendorCompanyId,
			boolean vanityUrlFilter) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = " SELECT case when cast(count(distinct xul.user_list_id) as int) >0 then true else false end \r\n"
				+ " from  xt_user_profile u\r\n" + " left join xt_user_list xul  \r\n"
				+ " on u.user_id = xul.assigned_by AND xul.module_name = 'SHARE LEADS' AND\r\n"
				+ " xul.email_validation_ind = true  left join xt_user_userlist xuu on\r\n"
				+ " xul.user_list_id = xuu.user_list_id   left join  xt_user_profile u1 \r\n"
				+ " on u1.user_id = xuu.user_id  left join xt_sharelist_partner sp on\r\n"
				+ " sp.user_list_id  = xul.user_list_id  left join xt_sharelist_partner_mapping spm \r\n"
				+ " on sp.id =spm.sharelist_partner_id  left join xt_company_profile cp on cp.company_id =\r\n"
				+ " xul.assigned_company_id  where xul.company_id= :partnerCompanyId and spm.partner_id = :partnerId";
		if (vanityUrlFilter) {
			queryString += " and xul.assigned_company_id=:vendorCompanyId";
		}
		Query query = session.createSQLQuery(queryString);
		query.setParameter("partnerId", partnerId);
		query.setParameter("partnerCompanyId", partnerCompanyId);
		if (vanityUrlFilter) {
			query.setParameter("vendorCompanyId", vendorCompanyId);
		}
		return (boolean) query.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findPartnerCompanyIdsByUserListIds(Set<Integer> userListIds) {
		if (userListIds != null && !userListIds.isEmpty()) {
			Session session = sessionFactory.getCurrentSession();
			String sqlString = "select distinct  up.company_id from xt_user_list ul,xt_user_userlist xuul,xt_user_profile up \r\n"
					+ " where ul.user_list_id = xuul.user_list_id and ul.user_list_id in (:userListIds)\r\n"
					+ " and up.user_id = xuul.user_id and up.company_id is not null\r\n";
			Query query = session.createSQLQuery(sqlString);
			query.setParameterList("userListIds", userListIds);
			return query.list();
		} else {
			return Collections.emptyList();
		}
	}

	/************ XNFR-278 ********/
	@Override
	public Map<String, Object> findGroupsForMerging(Pagination pagination) {
		try {
			HashMap<String, Object> map = new HashMap<>();
			String finalQueryString = "";
			String sortQueryString = getSortOptionForUserGroup(pagination);
			String searchKey = pagination.getSearchKey();
			boolean hasSearchKey = StringUtils.hasText(searchKey);

			if (XamplifyUtils.CONTACTS.equalsIgnoreCase(pagination.getModuleName())) {
				finalQueryString = userListContactsQuery;
				if (pagination.isPartnerTeamMemberGroupFilter()) {
					finalQueryString += " and ul.customer_id = :userId";
				}
				if (hasSearchKey) {
					finalQueryString += " " + userListGroupsSearchKey.replace("searchKey", searchKey);
				}
				finalQueryString += " " + groupByUserListContactQuery;
			} else {
				finalQueryString = userListGroupsQuery;
				if (hasSearchKey) {
					finalQueryString += " " + userListGroupsSearchKey.replace("searchKey", searchKey);
				}
				finalQueryString += " " + userListGroupsGroupByQuery;
			}

			finalQueryString += " " + sortQueryString;
			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(finalQueryString);
			if ("CONTACTS".equals(pagination.getModuleName())) {
				Integer companyId = userDAO.getCompanyIdByUserId(pagination.getUserId());
				query.setParameter("companyId", companyId);
				if (pagination.isPartnerTeamMemberGroupFilter()) {
					query.setParameter("userId", pagination.getUserId());
				}
			} else {
				utilDao.addActiveAndInActiveMasterPartnerListsToParameterList(query);
				query.setParameter("moduleName", partnerGroup);
				query.setParameter("loggedInUserId", pagination.getUserId());
			}
			query.setParameter("userListId", pagination.getUserListId());
			return paginationUtil.setScrollableAndGetList(pagination, map, query, PartnerGroupDTO.class);
		} catch (HibernateException | UserListException e) {
			throw new UserListException(e);
		} catch (Exception ex) {
			throw new UserListException(ex);
		}

	}

	private String getSortOptionForUserGroup(Pagination pagination) {
		List<SortColumnDTO> sortColumnDTOs = new ArrayList<>();
		SortColumnDTO sortByName = new SortColumnDTO("name", "ul.user_list_name", true, true, false);
		SortColumnDTO sortByCreatedTime = new SortColumnDTO("createdTime", "ul.created_time", false, false, false);
		SortColumnDTO sortByCount = new SortColumnDTO("count", "count(uul.id)", false, false, false);
		sortColumnDTOs.add(sortByName);
		sortColumnDTOs.add(sortByCreatedTime);
		sortColumnDTOs.add(sortByCount);
		return paginationUtil.generateSortQuery(pagination, sortColumnDTOs, "asc");
	}

	@Override
	public Map<String, Object> copyUsersToUserGroups(CopyGroupUsersDTO copyGroupUserDto) {
		Integer statusCode = 200;
		Map<String, Object> map = new HashMap<>();
		try {
			Session session = sessionFactory.getCurrentSession();
			Integer loggedInUserId = copyGroupUserDto.getLoggedInUserId();
			Integer loggedInUserCompanyId = userDAO.getCompanyIdByUserId(loggedInUserId);
			Set<Integer> selectedUserGroupIds = copyGroupUserDto.getUserGroupIds();
			Set<Integer> selectedUserIds = copyGroupUserDto.getUserIds();
			List<UserUserList> existingUserUserLists = findUserUserListsByUserListIdAndUserIds(session,
					copyGroupUserDto.getUserGroupId(), selectedUserIds);
			List<UserUserList> newUserUserLists = new ArrayList<>();
			List<CopiedUserListUsersDTO> copiedUserListUsersDTOs = new ArrayList<>();

			Set<User> selectedPartners = new HashSet<>();
			for (Integer selectedPartnerGroupId : selectedUserGroupIds) {
				List<Integer> exisitingUserIds = findExistingUserIdsByUserListId(session, selectedPartnerGroupId);
				List<Integer> filteredUserIds = new ArrayList<>();
				for (UserUserList existingUserUserList : existingUserUserLists) {
					iterateUserUserListAndAddNewUsers(newUserUserLists, selectedPartnerGroupId, exisitingUserIds,
							filteredUserIds, existingUserUserList, copyGroupUserDto);
					User selectedPartner = getCopyListSelectedUser(existingUserUserList);
					selectedPartners.add(selectedPartner);
				}
				addDataToCopiedUserListUsersDTO(copiedUserListUsersDTOs, selectedPartnerGroupId, filteredUserIds,
						loggedInUserId, loggedInUserCompanyId);
				/**** Update Asset/Tracks/Play Books/DashBoard Buttons Status ******/

			}
			copyGroupUserDto.setUsers(selectedPartners);
			statusCode = saveUserListsWithBatchProcessing(statusCode, session, newUserUserLists);

			map.put("statusCode", statusCode);
			map.put(XamplifyConstants.COPIED_USERLIST_USERS_KEY, copiedUserListUsersDTOs);
		} catch (ConstraintViolationException con) {
			catchConstraintViolation(con);
		} catch (HibernateException | UserListException e) {
			throw new UserListException(e.getMessage());
		} catch (Exception ex) {
			throw new UserListException(ex.getMessage());
		}
		return map;
	}

	private void iterateUserUserListAndAddNewUsers(List<UserUserList> newUserUserLists, Integer selectedUserListId,
			List<Integer> exisitingUserIds, List<Integer> filteredUserIds, UserUserList existingUserUserList,
			CopyGroupUsersDTO copyGroupUserDto) {
		Integer userId = existingUserUserList.getUser().getUserId();
		if (exisitingUserIds != null && !exisitingUserIds.isEmpty() && exisitingUserIds.indexOf(userId) < 0) {
			saveUserUserList(newUserUserLists, selectedUserListId, filteredUserIds, existingUserUserList, userId);
		} else if ("CONTACTS".equalsIgnoreCase(copyGroupUserDto.getModuleName())
				&& exisitingUserIds.indexOf(userId) < 0) {
			saveUserUserList(newUserUserLists, selectedUserListId, filteredUserIds, existingUserUserList, userId);
		}
	}

	private void saveUserUserList(List<UserUserList> newUserUserLists, Integer selectedUserListId,
			List<Integer> filteredUserIds, UserUserList existingUserUserList, Integer userId) {
		UserUserList newUserUserList = new UserUserList();
		BeanUtils.copyProperties(existingUserUserList, newUserUserList, "user", "legalBasis", "userList");
		UserList selectedUserList = new UserList();
		selectedUserList.setId(selectedUserListId);
		newUserUserList.setUserList(selectedUserList);
		newUserUserList.setUser(existingUserUserList.getUser());
		filteredUserIds.add(userId);
		List<LegalBasis> newLegalBasisList = new ArrayList<>();
		for (LegalBasis existingLegalBasis : existingUserUserList.getLegalBasis()) {
			LegalBasis newLegalBasis = new LegalBasis();
			newLegalBasis.setId(existingLegalBasis.getId());
			newLegalBasisList.add(newLegalBasis);
		}
		newUserUserList.setLegalBasis(newLegalBasisList);
		newUserUserList.setUserListFlexiFields(new HashSet<>());
		newUserUserLists.add(newUserUserList);
	}

	private void addDataToCopiedUserListUsersDTO(List<CopiedUserListUsersDTO> copiedUserListUsersDTOs,
			Integer selectedUserListId, List<Integer> filteredUserIds, Integer loggedInUserId,
			Integer loggedInUserCompanyId) {
		if (XamplifyUtils.isNotEmptyList(filteredUserIds)) {
			CopiedUserListUsersDTO copiedUserListUsersDTO = new CopiedUserListUsersDTO();
			copiedUserListUsersDTO.setUserListId(selectedUserListId);
			copiedUserListUsersDTO.setLoggedInUserId(loggedInUserId);
			copiedUserListUsersDTO.setLoggedInUserCompanyId(loggedInUserCompanyId);
			copiedUserListUsersDTO.setPartnerIds(XamplifyUtils.convertListToSetElements(filteredUserIds));
			copiedUserListUsersDTOs.add(copiedUserListUsersDTO);
		}
	}

	private Integer saveUserListsWithBatchProcessing(Integer statusCode, Session session,
			List<UserUserList> newUserUserLists) {
		if (!newUserUserLists.isEmpty()) {
			for (int i = 0; i < newUserUserLists.size(); i++) {
				UserUserList newUserUserList = newUserUserLists.get(i);
				session.save(newUserUserList);
				if (i % 30 == 0) {
					session.flush();
					session.clear();
				}
			}
		} else {
			statusCode = 409;
		}
		return statusCode;
	}

	@SuppressWarnings("unchecked")
	private List<Integer> findExistingUserIdsByUserListId(Session session, Integer selectedUserListId) {
		String sqlString = "select user_id from xt_user_userlist where user_list_id = :userListId";
		return session.createSQLQuery(sqlString).setParameter("userListId", selectedUserListId).list();
	}

	private void catchConstraintViolation(ConstraintViolationException con) {
		String constaintName = con.getConstraintName();
		if ("user_userlist_unique".equalsIgnoreCase(constaintName)) {
			throw new DuplicateEntryException("User is already part of selected groups.");
		}
	}

	@SuppressWarnings("unchecked")
	private List<UserUserList> findUserUserListsByUserListIdAndUserIds(Session session, Integer userListId,
			Set<Integer> userIds) {
		org.hibernate.Criteria criteria = session.createCriteria(UserUserList.class);
		criteria.createAlias("userList", "ul");
		criteria.createAlias("user", "us");
		criteria.add(Restrictions.eq("ul.id", userListId));
		criteria.add(Restrictions.in("us.userId", userIds));
		criteria.setResultTransformer(org.hibernate.Criteria.DISTINCT_ROOT_ENTITY);
		return criteria.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findPartnerListIdsByCompanyId(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String hqlString = "select distinct id from UserList where company.id=:companyId";
		Query query = session.createQuery(hqlString);
		query.setParameter("companyId", companyId);
		List<Integer> partnerListIds = new ArrayList<>();
		if (query.list() != null && !query.list().isEmpty()) {
			partnerListIds.addAll(query.list());
		}
		return partnerListIds;
	}

	@Override
	public CompanyProfileDTO getPartnerDetails(Integer partnerId) {
		Session session = sessionFactory.getCurrentSession();
		String sqlString = "select distinct on (uul.user_id)  max(uul.contact_company) as \"companyName\", max(uul.address) as \"street\", "
				+ " max(uul.city) as \"city\", max(uul.state) as \"state\",  max(uul.zip) as \"zip\", max(uul.country) as \"country\", "
				+ " cp.company_id as \"id\" "
				+ " from xt_user_list ul, xt_user_userlist uul, xt_partnership p, xt_company_profile cp, xt_user_profile up "
				+ " where ul.user_list_id=uul.user_list_id and ul.is_default_partnerlist = true "
				+ " and p.partner_id = uul.user_id and uul.user_id = :partnerId and up.user_id = uul.user_id and cp.company_id = up.company_id "
				+ " group BY ul.updated_time, uul.user_id, cp.company_id ";
		Query query = session.createSQLQuery(sqlString);
		query.setParameter("partnerId", partnerId);
		query.setResultTransformer(Transformers.aliasToBean(CompanyProfileDTO.class));
		return (CompanyProfileDTO) query.uniqueResult();
	}

	@Override
	public String getUserListNameByUserListId(Integer userListId) {
		String queryString = "select user_list_name from xt_user_list where user_list_id = :userListId";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("userListId", userListId));
		return (String) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
	}

	/********* XBI-1816 *********/
	@Override
	public void updateHubspotOrPipedriveContactList(Integer companyId, String type) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "update xt_user_list set is_synchronized_list = 'false' where (company_id = :company_id or assigned_company_id = :company_id) and social_network = '"
				+ type.toUpperCase() + "'";
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("company_id", companyId);
		query.executeUpdate();
	}

	@Override
	public boolean isExistingUserListNameExists(UserListDTO userListDTO) {
		Session session = sessionFactory.getCurrentSession();
		String sql = " ";
		if (userListDTO.isPartnerUserList()) {
			sql = "select count(*) from xt_user_list where user_list_name = '" + userListDTO.getName()
					+ "' and is_partner_userlist = true "
					+ " and contact_type in ('CONTACT') and company_id=:companyId and user_list_id!= "
					+ userListDTO.getId();
		} else if (userListDTO.isAssignedLeadsList()) {
			sql = " select count(*) from xt_user_list where user_list_name = '" + userListDTO.getName()
					+ "' and is_partner_userlist = false  "
					+ " and module_name='SHARE LEADS' and assigned_company_id=:companyId and user_list_id!= "
					+ userListDTO.getId();
		} else {
			sql = " select count(*) from xt_user_list where user_list_name = '" + userListDTO.getName()
					+ "' and is_partner_userlist = false "
					+ " and contact_type in ('CONTACT', 'LEAD','CONTACT_LISTVIEWS', 'LEAD_LISTVIEWS') and company_id=:companyId and user_list_id!= "
					+ userListDTO.getId();
		}
		Query query = session.createSQLQuery(sql);
		query.setParameter("companyId", userListDTO.getCompanyId());
		return query.uniqueResult() != null && ((BigInteger) query.uniqueResult()).intValue() > 0 ? true : false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ContactsCSVDTO> findAllContactsOfAllPartnersByVendorCompanyId(Integer vendorCompanyId,
			List<Integer> partnerCompanyIds) {
		List<ContactsCSVDTO> contactsCSVDTOs = new ArrayList<ContactsCSVDTO>();
		String sqlString = "SELECT " + "DISTINCT " + "cp.company_name AS \"partnerCompanyName\", "
				+ "up.email_id AS \"emailId\", " + "max(coalesce(uul.firstname,'')) AS \"firstName\", "
				+ "max(coalesce(uul.lastname,'')) AS \"lastName\", "
				+ "max(coalesce(uul.contact_company,'')) AS \"company\", "
				+ "max(coalesce(uul.job_title,'')) AS \"jobTitle\", "
				+ "max(coalesce(uul.mobile_number,'')) AS \"mobileNumber\", "
				+ "max(coalesce(uul.address,'')) AS \"address\", " + "max(coalesce(uul.city,'')) AS \"city\", "
				+ "max(coalesce(uul.country,'')) AS \"country\", " + "max(coalesce(uul.state,'')) AS \"state\", "
				+ "max(coalesce(uul.zip,'')) AS \"zipCode\" " + "FROM " + "xt_user_list u, " + "xt_user_userlist uul, "
				+ "xt_user_profile up, " + "xt_company_profile cp " + "WHERE " + "u.user_list_id = uul.user_list_id "
				+ "AND uul.user_id = up.user_id " + "AND up.email_validation_ind = TRUE "
				+ "AND u.company_id = cp.company_id " + "AND u.user_list_id IN ( " + "SELECT " + "user_list_id "
				+ "FROM " + "xt_user_list " + "WHERE " + "company_id = :partnerCompanyId "
				+ "AND module_name = 'CONTACTS') " + "GROUP BY " + "1, " + "2 " + "ORDER BY " + "1 ";
		Session session = sessionFactory.getCurrentSession();
		for (Integer partnerCompanyId : partnerCompanyIds) {
			SQLQuery query = session.createSQLQuery(sqlString);
			query.setParameter("partnerCompanyId", partnerCompanyId);
			List<ContactsCSVDTO> contacts = (List<ContactsCSVDTO>) paginationUtil.getListDTO(ContactsCSVDTO.class,
					query);
			contactsCSVDTOs.addAll(contacts);
		}
		return contactsCSVDTOs;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ContactsCSVDTO> findAllContactsCountOfAllPartnersByVendorCompanyId(Integer companyId,
			List<Integer> partnerCompanyIds) {
		List<ContactsCSVDTO> contactsCSVDTOs = new ArrayList<ContactsCSVDTO>();
		String sqlString = "select\n" + "cast(\n" + "count(distinct up.user_id) as integer\n"
				+ ") as \"contactsCount\",cp.company_name as \"partnerCompanyName\"\n" + "\n" + "from\n"
				+ "xt_user_list u,\n" + "xt_user_userlist uul,\n" + "xt_user_profile up,\n" + "xt_company_profile cp\n"
				+ "where\n" + "u.user_list_id = uul.user_list_id\n" + "and uul.user_id = up.user_id\n"
				+ "and up.email_validation_ind = true\n" + "and u.user_list_id in (\n"
				+ "select user_list_id from xt_user_list where company_id =:partnerCompanyId and module_name = 'CONTACTS'\n"
				+ ") and u.company_id = cp.company_id\n" + "group by cp.company_id\n";
		Session session = sessionFactory.getCurrentSession();
		for (Integer partnerCompanyId : partnerCompanyIds) {
			SQLQuery query = session.createSQLQuery(sqlString);
			query.setParameter("partnerCompanyId", partnerCompanyId);
			List<ContactsCSVDTO> contacts = (List<ContactsCSVDTO>) paginationUtil.getListDTO(ContactsCSVDTO.class,
					query);
			contactsCSVDTOs.addAll(contacts);
		}
		return contactsCSVDTOs;
	}

	@Override
	public boolean checkDomineExcluded(Integer customerCompanyId, String emailId) {
		Session session = sessionFactory.getCurrentSession();
		String domain = emailId.substring(emailId.indexOf("@") + 1);

		String sql = "select count(*) from xt_excluded_domain ed where ed.company_id = :customerCompanyId and ed.domain_name = :domain ";
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("customerCompanyId", customerCompanyId);
		query.setParameter("domain", domain);
		return ((BigInteger) query.uniqueResult()).intValue() > 0;
	}

	@Override
	public void deleteUserFromExcludedUser(Integer customerCompanyId, Integer userId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "delete from xt_excluded_user eu where eu.company_id = :customerCompanyId and eu.user_id = :userId ";
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("customerCompanyId", customerCompanyId);
		query.setParameter("userId", userId);
		query.executeUpdate();

	}

	@Override
	public void updateFormContactListName(Integer formId, String userListName) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "update xt_user_list set user_list_name = :userListName where form_id = :formId";
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("userListName", userListName);
		query.setParameter("formId", formId);
		query.executeUpdate();
	}

	@Override
	public void setContactCompanyAsNull(Integer contactCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "update xt_user_userlist set contact_company_id = null, contact_company = '' "
				+ " where contact_company_id = :contactCompanyId";
		session.createSQLQuery(sql).setParameter("contactCompanyId", contactCompanyId).executeUpdate();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<UserUserList> getAllUserUserListsByUserId(Integer userId, Integer loggedinCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(UserUserList.class, "UUL");
		criteria.createAlias("UUL.userList", "UL", JoinType.INNER_JOIN);
		criteria.add(Restrictions.eq("UUL.user.userId", userId));
		criteria.add(Restrictions.eq("UL.company.id", loggedinCompanyId));
		criteria.add(Restrictions.eq("UL.moduleName", "CONTACTS"));
		return criteria.list();
	}

	public Boolean isUserExistsInCompanyContactList(Integer userId, Integer loggedinCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select count(uul.id) From xt_user_userlist uul inner join  xt_user_list ul on ul.user_list_id = uul.user_list_id where "
				+ " ul.company_id = :loggedinCompanyId and uul.user_id = :userId and  ul.associated_company_id is not null ";
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("loggedinCompanyId", loggedinCompanyId);
		query.setParameter("userId", userId);
		return query.uniqueResult() != null && ((BigInteger) query.uniqueResult()).intValue() > 0 ? true : false;
	}

	public Integer getCompanyContactListId(Integer contactCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = " select user_list_id From xt_user_list where associated_company_id = :contactCompanyId";
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("contactCompanyId", contactCompanyId);
		return (Integer) query.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void updateUserUserListByCompanyDetails(Integer contactCompanyId, String contactCompany,
			Integer loggedinCompanyId, Integer userId) {
		String hql = "SELECT ul FROM UserList ul inner join ul.userUserLists as uul WHERE ul.id = uul.userList.id "
				+ " and ul.company.id=:loggedinCompanyId and uul.user.userId=:userId and ul.moduleName ='CONTACTS'";

		Query query = sessionFactory.getCurrentSession().createQuery(hql);
		query.setParameter("loggedinCompanyId", loggedinCompanyId);
		query.setParameter("userId", userId);

		List<UserList> userLists = (List<UserList>) query.list();

		if (userLists != null && !userLists.isEmpty()) {
			Integer companyUserListId = getCompanyContactListId(contactCompanyId);
			Company company = genericDao.get(Company.class, contactCompanyId);

			for (UserList userlist : userLists) {
				for (UserUserList userUserList : userlist.getUserUserLists()) {
					if (Integer.compare(userId, userUserList.getUser().getUserId()) == 0) {
						userUserList.setContactCompany(contactCompany);
						userUserList.setCompany(company);
						if (userlist.getAssociatedCompany() != null && !(Integer.compare(companyUserListId,
								userlist.getAssociatedCompany().getId()) == 0)) {
							UserList compnayUserList = genericDao.get(UserList.class, companyUserListId);
							userUserList.setUserList(compnayUserList);
						}
					}
				}
			}
		}

	}

	@Override
	public Object getUserListByUserListId(Integer userListId) {
		return (UserListDTO) sessionFactory.getCurrentSession().createCriteria(UserList.class)
				.setProjection(Projections.projectionList().add(Projections.property("id"), "id")
						.add(Projections.property("name"), "name")
						.add(Projections.property("synchronisedList"), "synchronisedList")
						.add(Projections.property("isPartnerUserList"), "isPartnerUserList")
						.add(Projections.property("isDefaultPartnerList"), "isDefaultPartnerList")
						.add(Projections.property("publicList"), "publicList")
						.add(Projections.property("teamMemberPartnerList"), "teamMemberPartnerList")
						.add(Projections.property("associatedCompany.id"), "associatedCompanyId")
						.add(Projections.property("owner.userId"), "uploadedUserId"))
				.add(Restrictions.eq("id", userListId))
				.setResultTransformer(Transformers.aliasToBean(UserListDTO.class)).uniqueResult();
	}

	@Override
	public void updateCompanyOnAllUserUserlists(Integer contactCompanyId, String contactCompanyName,
			List<Integer> userId, Integer loggedInCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "UPDATE xt_user_userlist xuul SET contact_company_id = " + contactCompanyId
				+ ", contact_company = " + contactCompanyName + " "
				+ "FROM xt_user_list xul where xuul.user_list_id = xul.user_list_id "
				+ "and xul.company_id = :loggedInCompanyId and xuul.user_id in (:userId) AND xul.module_name ='CONTACTS' and xul.assigned_company_id is null";
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameterList("userId", userId);
		query.setParameter("loggedInCompanyId", loggedInCompanyId);
		query.executeUpdate();
	}

	@Override
	public UserUserList getContactInCompanyContactList(Integer userId, Integer loggedinCompanyId) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getExistingExcludedUsers(Integer companyId, List<String> existingMailIds) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select xup.email_id from xt_excluded_user xeu, xt_user_profile xup  "
				+ "where xeu.company_id = :companyId and "
				+ " xeu.user_id = xup.user_id and xup.email_id in (:existingMailIds) ";
		Query query = session.createSQLQuery(sql);
		query.setParameter("companyId", companyId);
		query.setParameterList("existingMailIds", existingMailIds);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<UserDTO> getAllUserContacts(Integer companyId, Integer userListId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select distinct up.user_id as \"id\", max(uul.user_list_id) as \"userListId\", max(uul.firstname) as \"firstName\", \n"
				+ "max(uul.lastname) as \"lastName\", max(uul.contact_company) as \"contactCompany\", max(uul.job_title) as \"jobTitle\", \n"
				+ "max(uul.mobile_number) as \"mobileNumber\" , max(uul.description) as \"description\", max(uul.address) as \"address\", \n"
				+ "max(uul.city) as \"city\", max(uul.country) as \"country\", max(uul.state) as \"state\", max(uul.zip) as \"zipCode\", \n"
				+ "max(uul.contact_company_id) as \"contactCompanyId\", max(uul.country_code) as \"countryCode\", "
				+ "max(uul.contact_status_id) as \"contactStatusId\" \nfrom xt_user_list ul, xt_user_userlist uul, xt_user_profile up \n "
				+ "where ul.user_list_id = uul.user_list_id and uul.user_id = up.user_id and ul.company_id = :companyId and ul.module_name = 'CONTACTS' "
				+ "and ul.contact_list_type is null and up.user_id not in (select distinct uul.user_id from xt_user_list ul, xt_user_userlist uul "
				+ "where ul.user_list_id=uul.user_list_id and  ul.company_id = :companyId and ul.module_name = 'CONTACTS' "
				+ "and ul.user_list_id = :userlistId)" + " group by  up.user_id ";
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("companyId", companyId);
		query.setParameter("userlistId", userListId);
		query.setResultTransformer(Transformers.aliasToBean(UserDTO.class));
		List<UserDTO> userDtOs = query.list();
		return userDtOs;
	}

	@SuppressWarnings("unchecked")
	public List<Integer> getAllExistingCompanyIds() {

		Session session = sessionFactory.getCurrentSession();
		String sql = "select distinct xup.company_id from xt_user_profile xup, xt_user_role xur "
				+ "where xup.user_id=xur.user_id and xup.status = 'APPROVE' "
				+ "and xup.company_id is not null and xur.role_id in (12,2,18)";
		SQLQuery query = session.createSQLQuery(sql);
		List<Integer> userIds = new ArrayList<Integer>();
		if (query.list() != null && !query.list().isEmpty()) {
			userIds.addAll(query.list());
		}
		return userIds;
	}

	public UserList getDefaultContactList(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(UserList.class);
		criteria.add(Restrictions.eq("company.id", companyId));
		criteria.add(Restrictions.eq("isPartnerUserList", false));
		criteria.add(Restrictions.eq("contactListType", ContactListTypeValue.DEFAULT_CONTACT_LIST));
		return (UserList) criteria.uniqueResult();
	}

	@Override
	public Boolean isUserExistsInDefaultPartnerList(Integer partnerId, Integer loggedInUserCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select count(uul.id) From xt_user_userlist uul inner join  xt_user_list ul on ul.user_list_id = uul.user_list_id where "
				+ " ul.company_id = :loggedinCompanyId and uul.user_id = :partnerId and  ul.is_default_partnerlist=true ";
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("loggedinCompanyId", loggedInUserCompanyId);
		query.setParameter("partnerId", partnerId);
		return query.uniqueResult() != null && ((BigInteger) query.uniqueResult()).intValue() > 0 ? true : false;
	}

	@Override
	public Integer getAllContactsCount(List<Integer> userListIds, Integer customerCompanyId, String moduleName,
			UserListDTO userListDTO) {
		Session session = sessionFactory.getCurrentSession();
		List<List<Integer>> chunkedUserListIds = XamplifyUtils.getChunkedList(userListIds);
		Integer allContactCountValue = 0;
		String sql = "";
		if (userListDTO.getId() == null && moduleName != null && moduleName.equalsIgnoreCase("PARTNERS")) {
			sql = partnersAllCountSql;
		} else {
			sql = allCountSql;
		}

		for (List<Integer> userListId : chunkedUserListIds) {
			SQLQuery query = session.createSQLQuery(sql);
			if (userListDTO.getId() == null && moduleName != null && moduleName.equalsIgnoreCase("PARTNERS")) {
				query.setParameter("customerCompanyId", customerCompanyId);
			} else {
				query.setParameterList("userListIds", userListId);
			}
			Integer count = (Integer) query.uniqueResult();
			if (count != null) {
				allContactCountValue += count;
			}
		}

		return allContactCountValue;
	}

	@Override
	public Integer getActiveCount(List<Integer> userListIds, Integer customerCompanyId, String moduleName,
			Integer logedInUserId, UserListDTO userListDTO) {
		String excludedUsersSql = getExcludedQuery(moduleName);
		Integer activeContactCountValue = 0;
		String sql = "";
		if (userListDTO.getId() == null && moduleName != null && (moduleName.equalsIgnoreCase("PARTNERS"))) {
			sql = partnersActiveCountSql;
		} else {
			sql = activeCountSql + excludedUsersSql;
		}
		activeContactCountValue = getTileCount(userListIds, customerCompanyId, moduleName, logedInUserId, userListDTO,
				sql);
		return activeContactCountValue;
	}

	@Override
	public Integer getInActiveCount(List<Integer> userListIds, Integer customerCompanyId, String moduleName,
			Integer logedInUserId, UserListDTO userListDTO) {
		String excludedUsersSql = getExcludedQuery(moduleName);
		Integer inActiveContactCountValue = 0;
		String sql = "";

		if (userListDTO.getId() == null && moduleName != null && moduleName.equalsIgnoreCase("PARTNERS")) {
			sql = partnersInActiveCountSql;
		} else {
			sql = inActivecountSql + excludedUsersSql;
		}
		inActiveContactCountValue = getTileCount(userListIds, customerCompanyId, moduleName, logedInUserId, userListDTO,
				sql);
		return inActiveContactCountValue;
	}

	@Override
	public Integer getUndeliverableCount(List<Integer> userListIds, Integer customerCompanyId, String moduleName,
			Integer logedInUserId, UserListDTO userListDTO) {
		String excludedUsersSql = getExcludedQuery(moduleName);
		Integer undeliverableCountValue = 0;
		String sql = "";
		if (userListDTO.getId() == null && moduleName != null && moduleName.equalsIgnoreCase("PARTNERS")) {
			sql = partnersUndeliverableCountSql;
		} else {
			sql = undeliverableCountSql + excludedUsersSql;
		}
		undeliverableCountValue = getTileCount(userListIds, customerCompanyId, moduleName, logedInUserId, userListDTO,
				sql);
		return undeliverableCountValue;
	}

	@Override
	public Integer getUnSubscribedCount(List<Integer> userListIds, Integer customerCompanyId, String moduleName,
			Integer logedInUserId, UserListDTO userListDTO) {
		String excludedUsersSql = getExcludedQuery(moduleName);
		Integer unSubscribedCountValue = 0;

		String sql = "";
		if (userListDTO.getId() == null && moduleName != null && moduleName.equalsIgnoreCase("PARTNERS")) {
			sql = partnersUnsubscribedCountSql;
		} else {
			sql = unsubscribedCountSql + excludedUsersSql;
		}
		unSubscribedCountValue = getTileCount(userListIds, customerCompanyId, moduleName, logedInUserId, userListDTO,
				sql);
		return unSubscribedCountValue;
	}

	@Override
	public UserList getUserList(Integer userListId) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(UserList.class);
		criteria.add(Restrictions.eq("id", userListId));
		return (UserList) criteria.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<UserList> getInProcessUserLists() {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(UserList.class);
		criteria.add(Restrictions.eq("emailValidationInd", false));
		return criteria.list();
	}

	@Override
	public Integer getDefaultPartnerListIdByCompanyId(Integer companyId) {
		String queryString = "select user_list_id from xt_user_list where company_id = :companyId and is_default_partnerlist = true";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("companyId", companyId));
		return (Integer) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public UserDTO findUserUserListDetailsByUserListIdAndEmailId(Integer userListId, String emailId) {
		String queryString = "select distinct u.email_id as \"emailId\",u.user_id as \"id\",u.company_id as \"companyId\" from xt_user_userlist uul,xt_user_profile u where uul.user_list_id = :userListId "
				+ "and u.user_id = uul.user_id and u.email_id = :emailId";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("userListId", userListId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("emailId", emailId));
		return (UserDTO) hibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO, UserDTO.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getActiveUsers(User logedInUser, List<Integer> userListIds, String searchHQL,
			String sortSQL, String dataSortSQL, String sortColumnSQL, String sortingOrder, Pagination pagination,
			UserListDTO userListDTO) {
		Session excludedSession = sessionFactory.getCurrentSession();
		Map<String, Object> resultMap = new HashMap<>();
		List<Integer> userIds = null;
		dataSortSQL = ((dataSortSQL != null) && !(dataSortSQL.isEmpty()) && dataSortSQL.length() >= 0) ? dataSortSQL
				: "";
		sortSQL = ((sortSQL != null) && !(sortSQL.isEmpty()) && sortSQL.length() >= 0) ? sortSQL : "";
		sortColumnSQL = XamplifyUtils.isValidString(sortColumnSQL) ? sortColumnSQL : "";
		searchHQL = (!(searchHQL.isEmpty()) && searchHQL.length() >= 1) ? searchHQL : "";
		String contactType = userListDTO.getContactType();
		Integer customerCompanyId = logedInUser.getCompanyProfile().getId();

		if (contactType != null && contactType.equalsIgnoreCase("excluded")) {
			userIds = getExcludedUserIds(userListIds, logedInUser, searchHQL, sortColumnSQL, sortingOrder, userListDTO);
		} else {
			userIds = getValidData(userListIds, logedInUser, searchHQL, sortSQL, sortColumnSQL, sortingOrder,
					userListDTO, pagination);
		}
		userIds = !userIds.isEmpty() ? userIds : Arrays.asList(0);
		Integer totalRecords = 0;
		SQLQuery excludeQuery = null;
		Integer limitMaxResults = pagination.getMaxResults();
		List<UserDTO> userDTOList = new ArrayList<>();
		String sql = "";
		if (Boolean.FALSE.equals(userListDTO.getIsDownload())) {
			sql = allDatadownaload;
			sql = sql.replace("dataSortSQL", dataSortSQL);
			sql = sql.replace("userListSql", " AND u.user_list_id IN (:userListIds)");
			Integer startIndex = ((pagination.getPageIndex() - 1) * pagination.getMaxResults());
			Integer endIndex = ((pagination.getPageIndex() - 1) * pagination.getMaxResults()) + limitMaxResults;

			if (endIndex > userIds.size()) {
				endIndex = userIds.size();
			}

			excludeQuery = excludedSession.createSQLQuery(sql);
			excludeQuery.setParameter("customerCompanyId", customerCompanyId);
			excludeQuery.setParameterList("userListIds", userListIds);
			excludeQuery.setParameterList("userIds", userIds.subList(startIndex, endIndex));
			excludeQuery.setResultTransformer(Transformers.aliasToBean(UserDTO.class));
			userDTOList = excludeQuery.list();
			resultMap.put("userDTOList", userDTOList);
			totalRecords = userIds.equals(Arrays.asList(0)) ? 0 : userIds.size();
			resultMap.put("totalRecords", totalRecords);
			if (userDTOList.size() < pagination.getMaxResults()) {
				limitMaxResults = pagination.getMaxResults() - userDTOList.size();
			}
		} else {
			List<List<Integer>> chunkedUserListIds = XamplifyUtils.getChunkedList(userIds);
			for (List<Integer> userListId : chunkedUserListIds) {
				sql = allDatadownaload;
				sql = sql.replace("dataSortSQL", dataSortSQL);
				sql = sql.replace("userListSql", " AND u.user_list_id IN (:userListIds)");
				excludeQuery = excludedSession.createSQLQuery(sql);
				excludeQuery.setParameter("customerCompanyId", customerCompanyId);
				excludeQuery.setParameterList("userListIds", userListIds);
				excludeQuery.setParameterList("userIds", userListId);
				ScrollableResults scrollableResults = excludeQuery.scroll();
				scrollableResults.last();
				Integer totalRecordsCount = scrollableResults.getRowNumber() + 1;
				totalRecords += totalRecordsCount;
				excludeQuery.setResultTransformer(Transformers.aliasToBean(UserDTO.class));
				excludeQuery.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
				excludeQuery.setMaxResults(pagination.getMaxResults());
				List<UserDTO> userDTOListObj = excludeQuery.list();
				userDTOList.addAll(userDTOListObj);
			}
			resultMap.put("userDTOList", userDTOList);
			resultMap.put("totalRecords", totalRecords);
		}
		return resultMap;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getValidData(List<Integer> userListIds, User loggedInUser, String searchHQL, String sortSQL,
			String sortColumnSQL, String sortingOrder, UserListDTO userListDTO, Pagination pagination) {
		String usersDataSql = "";
		List<Integer> validUserIds = new ArrayList<>();
		Integer logedInUserId = loggedInUser.getUserId();
		String contactType = userListDTO.getContactType();
		// Integer customerCompanyId = loggedInUser.getCompanyProfile().getId();
		boolean isSharedLeadsList = userListDTO.isSharedLeads();
		boolean isAssignedLeadsList = userListDTO.isAssignedLeadsList();
		String sortSqlQuery = sortSqlquery;
		List<Integer> results = null;
		if (XamplifyUtils.isValidString(contactType)) {
			if (contactType.equalsIgnoreCase("active")) {
				usersDataSql = activeSql;
				sortSqlQuery = sortSqlQuery.replace("excludedString", "");
			} else if (contactType.equalsIgnoreCase("non-active")) {
				usersDataSql = inActivesql;
				sortSqlQuery = sortSqlQuery.replace("excludedString", "");
			} else if (contactType.equalsIgnoreCase("valid")) {
				usersDataSql = validSql;
			} else if (contactType.equalsIgnoreCase("invalid")) {
				usersDataSql = inValidSql;
			} else if (contactType.equalsIgnoreCase("unsubscribed")) {
				if (XamplifyUtils.isValidString(userListDTO.getModuleName())
						&& userListDTO.getModuleName().equalsIgnoreCase("partners")
						&& !pagination.isPartnerTeamMemberGroupFilter()) {
					usersDataSql = partnerUnSubscribedSql;
				} else {
					usersDataSql = unSubscribedSql;
				}
			}
		}

		String excludedUsersSql = excludedUsersSql(isSharedLeadsList, isAssignedLeadsList);

		sortSqlQuery = sortSqlQuery.replace("excludedString", " and  eu.user_id IS NULL ");
		List<List<Integer>> chunkedUserIds = XamplifyUtils.getChunkedList(userListIds);
		for (List<Integer> chunk : chunkedUserIds) {
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = getUsersIdsSql(loggedInUser,
					searchHQL, sortSQL, sortColumnSQL, sortingOrder, userListDTO, pagination, usersDataSql,
					logedInUserId, contactType, isSharedLeadsList, chunk, excludedUsersSql, sortSqlQuery);

			results = (List<Integer>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
			results.remove(null);
			validUserIds.addAll(results);
			validUserIds.remove(null);
		}

		return validUserIds;
	}

	private HibernateSQLQueryResultRequestDTO getUsersIdsSql(User loggedInUser, String searchHQL, String sortSQL,
			String sortColumnSQL, String sortingOrder, UserListDTO userListDTO, Pagination pagination,
			String usersDataSql, Integer logedInUserId, String contactType, boolean isSharedLeadsList,
			List<Integer> chunk, String excludedUsersSql, String sortSqlQuery) {
		String sql;
		sql = usersDatasql(pagination, usersDataSql, contactType, excludedUsersSql, sortSqlQuery,
				userListDTO.getModuleName());
		sql = addingSortAndSearchSql(searchHQL, sortSQL, sortColumnSQL, sortingOrder, sql);
		sql = addingTeamMemberSql(pagination, sql);
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(sql);
		addingParametersToUsersDataSql(loggedInUser, userListDTO, pagination, logedInUserId, isSharedLeadsList, chunk,
				hibernateSQLQueryResultRequestDTO);
		return hibernateSQLQueryResultRequestDTO;
	}

	private void addingParametersToUsersDataSql(User loggedInUser, UserListDTO userListDTO, Pagination pagination,
			Integer logedInUserId, boolean isSharedLeadsList, List<Integer> chunk,
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO) {
		if ((XamplifyUtils.isValidString(userListDTO.getContactType())
				&& !userListDTO.getContactType().equalsIgnoreCase("All"))) {
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("customerCompanyId", loggedInUser.getCompanyProfile().getId()));
		}
		if (isSharedLeadsList && (XamplifyUtils.isValidString(userListDTO.getContactType())
				&& !userListDTO.getContactType().equalsIgnoreCase("All"))) {
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("logedInUserId", logedInUserId));
		}
		if (pagination.isPartnerTeamMemberGroupFilter() && Boolean.TRUE.equals(userListDTO.isPartnerUserList())) {
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("teamMemberId", loggedInUser.getUserId()));
		}
		hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs()
				.add(new QueryParameterListDTO("userListIds", chunk));
	}

	private String usersDatasql(Pagination pagination, String usersDataSql, String contactType, String excludedUsersSql,
			String sortSqlQuery, String moduleName) {
		String sql;
		if (!pagination.isPartnerTeamMemberGroupFilter() && contactType != null
				&& !contactType.equalsIgnoreCase("All")) {
			if (XamplifyUtils.isValidString(moduleName) && XamplifyUtils.isValidString(contactType)
					&& contactType.equalsIgnoreCase("unsubscribed") && moduleName.equalsIgnoreCase("partners")) {
				sql = usersDataSql;
			} else {
				sql = usersDataSql + "excluded_users as ( " + excludedUsersSql + ")" + sortSqlQuery;
			}
		} else if (pagination.isPartnerTeamMemberGroupFilter() && contactType != null
				&& !contactType.equalsIgnoreCase("All")) {
			sql = usersDataSql;
			sql = sql.replace(",", "");
			sql = sql + " select au.userId from users au \n" + " Where au.userId is not null ";
		} else {
			sql = allDataSql;
		}
		return sql;
	}

	private String addingTeamMemberSql(Pagination pagination, String sql) {
		if (pagination.isPartnerTeamMemberGroupFilter()) {
			sql = sql.replace("teamMemberSql", teamMemberSql);
		} else {
			sql = sql.replace("teamMemberSql", "");
		}
		return sql;
	}

	private String addingSortAndSearchSql(String searchHQL, String sortSQL, String sortColumnSQL, String sortingOrder,
			String sql) {
		sql = sql.replace("sortColumnSQL", sortColumnSQL);
		sql = sql.replace("sortSQL", sortSQL);
		sql = sql.replace("sortingOrder", sortingOrder);
		sql = sql.replace("searchHQL", searchHQL);
		return sql;
	}

	private String excludedUsersSql(boolean isSharedLeadsList, boolean isAssignedLeadsList) {
		String excludedUsersSql;
		if (isSharedLeadsList) {
			excludedUsersSql = SharedLeadsExcludedSql;
		} else if (isAssignedLeadsList) {
			excludedUsersSql = assignedLeadsExcludedSql;
		} else {
			excludedUsersSql = contactsExcludedSql;
		}
		return excludedUsersSql;
	}

	@Override
	public Integer getExcludedUsersCount(UserListDTO userListDTO, List<Integer> userListIds, Integer customerCompanyId,
			Integer loggedInUserId) {
		Session session = sessionFactory.getCurrentSession();
		String excludedUsersCountSQL = "";
		if (userListDTO.getModuleName() != null && userListDTO.getModuleName().equalsIgnoreCase("sharedleads")) {
			excludedUsersCountSQL = SharedLeadsExcludedUsersCount;
		} else if (userListDTO.isAssignedLeadsList()) {
			excludedUsersCountSQL = assignedLeadsExcludedUsersCount;
		} else {
			excludedUsersCountSQL = contactsExcludedUsersCount;
		}
		SQLQuery excludeQuery = session.createSQLQuery(excludedUsersCountSQL);
		excludeQuery.setParameterList("userListIds", userListIds);
		excludeQuery.setParameter("customerCompanyId", customerCompanyId);
		if (userListDTO.getModuleName() != null && userListDTO.getModuleName().equalsIgnoreCase("sharedleads")) {
			excludeQuery.setParameter("loggedInUserId", loggedInUserId);
		}
		return (Integer) excludeQuery.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getExcludedUserIds(List<Integer> userListIds, User loggedInUser, String searchHQL,
			String sortColumnSQL, String sortingOrder, UserListDTO userListDTO) {
		Session session = sessionFactory.getCurrentSession();
		Integer loggedInUserId = loggedInUser.getUserId();
		Integer customerCompanyId = loggedInUser.getCompanyProfile().getId();

		String excludedUsersIdsSQL = "";
		searchHQL = (!(searchHQL.isEmpty()) && searchHQL.length() >= 0) ? searchHQL : "";

		if (userListDTO.isSharedLeads()) {
			excludedUsersIdsSQL = SharedLeadsExcludedSqlWithSearch;
			excludedUsersIdsSQL = excludedUsersIdsSQL.replace("sortColumnSQL1", sortColumnSQL.replace("up", "up1"));
			excludedUsersIdsSQL = excludedUsersIdsSQL.replace(":excludeUserSearchSql", searchHQL.replace("up", "up1"));
		} else if (userListDTO.isAssignedLeadsList()) {
			excludedUsersIdsSQL = assignedLeadsExcludedSqlWithSearch;
			excludedUsersIdsSQL = excludedUsersIdsSQL.replace("sortColumnSQL1", sortColumnSQL.replace("up", "up1"));
			excludedUsersIdsSQL = excludedUsersIdsSQL.replace(":excludeUserSearchSql", searchHQL.replace("up", "up1"));
		} else {
			excludedUsersIdsSQL = contactsExcludedSqlWithSearch;
			excludedUsersIdsSQL = excludedUsersIdsSQL.replace(":excludeUserSearchSql", searchHQL);
		}

		if (XamplifyUtils.isValidString(sortColumnSQL)) {
			excludedUsersIdsSQL = excludedUsersIdsSQL.replace("sortColumnSQL", sortColumnSQL);
			sortingOrder = sortingOrder.replace("au.sortColumn", "2");
			excludedUsersIdsSQL = excludedUsersIdsSQL.replace("sortOrder", sortingOrder);
		} else {
			excludedUsersIdsSQL = excludedUsersIdsSQL.replace("sortColumnSQL", "");
			excludedUsersIdsSQL = excludedUsersIdsSQL.replace("sortOrder", "");
		}

		SQLQuery query = session.createSQLQuery(excludedUsersIdsSQL);
		query.setParameterList("userListIds", userListIds);
		query.setParameter("customerCompanyId", customerCompanyId);

		if (userListDTO.isSharedLeads()) {
			query.setParameter("logedInUserId", loggedInUserId);
		}
		return query.list();
	}

	public String getExcludedQuery(String moduleName) {
		String excludedUsersSql = "";
		String except = "except\r\n";
		if (moduleName != null && moduleName.equalsIgnoreCase("sharedleads")) {
			excludedUsersSql = except + " ( " + SharedLeadsExcludedSql + " ) )a";
		} else if (moduleName != null && moduleName.equalsIgnoreCase("leads")) {
			excludedUsersSql = except + " ( " + assignedLeadsExcludedSql + " ) )a";
		} else if (moduleName != null && moduleName.equalsIgnoreCase("contacts")) {
			excludedUsersSql = except + " ( " + contactsExcludedSql + " ) )a";
		} else {
			excludedUsersSql = ")a";
		}
		return excludedUsersSql;
	}

	public Integer getTileCount(List<Integer> userListIds, Integer customerCompanyId, String moduleName,
			Integer logedInUserId, UserListDTO userListDTO, String sql) {
		Integer tileCount = 0;
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		List<List<Integer>> chunkedUserListIds = XamplifyUtils.getChunkedList(userListIds);
		for (List<Integer> userListId : chunkedUserListIds) {
			hibernateSQLQueryResultRequestDTO.setQueryString(sql);
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("customerCompanyId", customerCompanyId));
			if ((userListDTO.getId() != null) || (moduleName != null && !moduleName.equalsIgnoreCase("partners"))) {
				hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs()
						.add(new QueryParameterListDTO("userListIds", userListId));
			}
			if (moduleName != null && moduleName.equalsIgnoreCase("sharedleads")) {
				hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
						.add(new QueryParameterDTO("logedInUserId", logedInUserId));

			}
			Integer count = (Integer) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
			if (count != null) {
				tileCount += count;
			}
		}
		return tileCount;
	}

	/****** XNFR-571 *********/
	@SuppressWarnings("unchecked")
	@Override
	public List<DashboardButtonsPartnersDTO> findUserListIdsAndPartnerIdsAndPartnershipIdsByUserListIds(
			Set<Integer> partnerGroupIds) {
		List<DashboardButtonsPartnersDTO> dashboardButtonsPublishedPartnerGroupsDTOs = new ArrayList<>();
		String queryString = "select distinct uul.id as \"userListId\",uul.user_id as \"partnerId\", \n"
				+ "p.id as \"partnershipId\" from xt_user_userlist uul,xt_partnership p,xt_user_list ul \n"
				+ "where p.partner_id = uul.user_id and uul.user_list_id in (:userListIds) \n"
				+ "and ul.user_list_id = uul.user_list_id and ul.company_id = p.vendor_company_id \n"
				+ "and p.status not in ('deactivated') order by uul.id asc";
		if (XamplifyUtils.isNotEmptySet(partnerGroupIds)) {
			List<List<Integer>> chunkedUserList = XamplifyUtils
					.getChunkedList(XamplifyUtils.convertSetToList(partnerGroupIds));
			for (List<Integer> chunkedIds : chunkedUserList) {
				HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
				hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
				hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs()
						.add(new QueryParameterListDTO("userListIds", chunkedIds));
				hibernateSQLQueryResultRequestDTO.setClassInstance(DashboardButtonsPartnersDTO.class);
				List<DashboardButtonsPartnersDTO> list = (List<DashboardButtonsPartnersDTO>) hibernateSQLQueryResultUtilDao
						.returnDTOList(hibernateSQLQueryResultRequestDTO);
				dashboardButtonsPublishedPartnerGroupsDTOs.addAll(list);
			}
		}
		return dashboardButtonsPublishedPartnerGroupsDTOs;

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findUserIdsByUserListIds(List<Integer> userListIds) {
		List<Integer> allUserIds = new ArrayList<>();
		if (XamplifyUtils.isNotEmptyList(userListIds)) {
			Session session = sessionFactory.getCurrentSession();
			List<List<Integer>> chunkedPartnerGroupIdsList = XamplifyUtils.getChunkedList(userListIds);
			for (List<Integer> chunkedPartnerGroupIds : chunkedPartnerGroupIdsList) {
				List<Integer> userUserListChunkedIds = session.createSQLQuery(
						"select distinct u.user_id  from xt_user_profile u,xt_user_userlist uul where uul.user_id = u.user_id and uul.user_list_id in (:partnerGroupIds) order by u.user_id asc")
						.setParameterList("partnerGroupIds", chunkedPartnerGroupIds).list();
				allUserIds.addAll(userUserListChunkedIds);
			}
		}
		return allUserIds;

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findPublishedPartnerIdsById(Integer dashboardButtonId) {
		if (XamplifyUtils.isValidInteger(dashboardButtonId)) {
			String queryString = "select distinct u.user_id  from xt_user_profile u,xt_user_userlist uul,xt_dashboard_buttons_partner_group_mapping\n"
					+ "dbpgm where uul.user_id = u.user_id and uul.id = dbpgm.user_user_list_id and  dbpgm.dashboard_button_id = :dashboardButtonId\n"
					+ "UNION select distinct published_to from xt_dashboard_buttons_partner_company_mapping where dashboard_button_id = :dashboardButtonId\n"
					+ "order by 1 asc\n";
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("dashboardButtonId", dashboardButtonId));
			return (List<Integer>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
		} else {
			return Collections.emptyList();
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> sharedListUserIds(Integer userId, Integer companyId, Integer vendorCompanyId, boolean isVanity,
			boolean isLoginAsPartner) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String query = "select distinct ul.user_list_id from xt_user_list ul,xt_sharelist_partner sp,\n"
				+ "xt_sharelist_partner_mapping spm , xt_partnership pa\n"
				+ "where ul.user_list_id = sp.user_list_id and sp.id = spm.sharelist_partner_id\n"
				+ " and pa.partner_company_id = :companyId and sp.partnership_id = pa.id\n"
				+ "and ul.email_validation_ind=true  and spm.partner_id = :partnerId";
		if (Boolean.TRUE.equals(isVanity) || Boolean.TRUE.equals(isLoginAsPartner)) {
			query = query + " and pa.vendor_company_id = :vendorCompanyId ";
		}

		hibernateSQLQueryResultRequestDTO.setQueryString(query);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("companyId", companyId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("partnerId", userId));
		if (Boolean.TRUE.equals(isVanity) || Boolean.TRUE.equals(isLoginAsPartner)) {
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("vendorCompanyId", vendorCompanyId));
		}
		return (List<Integer>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);

	}

	@Override
	public Integer getValidContactsCountByUserListId(Integer userListId) {
		return getValidOrInvalidContactsCount(userListId, true);
	}

	private Integer getValidOrInvalidContactsCount(Integer userListId, boolean isValid) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(
				"select cast(count(xup.*) as int) from xt_user_profile xup,xt_user_userlist xuul where xup.user_id = xuul.user_id and xup.is_email_valid = :isValid and xuul.user_list_id = :userListId");
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("userListId", userListId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("isValid", isValid));
		Integer count = (Integer) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
		if (count != null) {
			return count;
		} else {
			return 0;
		}
	}

	@Override
	public Integer getInvalidContactsCountByUserList(Integer userListId) {
		return getValidOrInvalidContactsCount(userListId, false);
	}

	@Override
	public Integer getValidContactsCountByUserListIdAndUserIds(Integer userListId, Set<Integer> userIds) {
		boolean isValid = true;
		return getValidOrInvalidContactsCountByUserListIdAndUserIds(userListId, userIds, isValid);

	}

	@Override
	public Integer getInvalidContactsCountByUserListIdAndUserIds(Integer userListId, Set<Integer> userIds) {
		boolean isValid = false;
		return getValidOrInvalidContactsCountByUserListIdAndUserIds(userListId, userIds, isValid);

	}

	private Integer getValidOrInvalidContactsCountByUserListIdAndUserIds(Integer userListId, Set<Integer> userIds,
			boolean isValid) {
		Integer totalCount = 0;
		if (XamplifyUtils.isNotEmptySet(userIds)) {
			Session session = sessionFactory.getCurrentSession();
			List<List<Integer>> userIdBatches = XamplifyUtils.getChunkedList(XamplifyUtils.convertSetToList(userIds));
			for (List<Integer> chunkedPartnerGroupIds : userIdBatches) {
				Integer chunkedCount = (Integer) session.createSQLQuery(
						"select cast(count(xup.*) as int) from xt_user_profile xup,xt_user_userlist xuul where xup.user_id = xuul.user_id and xup.is_email_valid = :isValid and xuul.user_list_id = :userListId and xup.user_id in (:userIds)")
						.setParameterList("userIds", chunkedPartnerGroupIds).setParameter("isValid", isValid)
						.setParameter("userListId", userListId).uniqueResult();
				totalCount += chunkedCount;

			}
		}
		return totalCount;
	}

	@Override
	public boolean isDefaultMasterContactListExists(Integer companyId) {
		String sqlString = "SELECT CASE WHEN count(*)>0 THEN TRUE ELSE FALSE END AS isDefaultContactListExists FROM xt_user_list xul\r\n"
				+ "WHERE xul.contact_list_type = 'DEFAULT_CONTACT_LIST' AND xul.company_id = :companyId";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(sqlString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("companyId", companyId));
		return (boolean) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public List<UserListDetails> onlylistUserLists(List<Criteria> criterias, Pagination pagination) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(UserListDetails.class);
		List<Criterion> criterions = generateCriteria(criterias);
		if (criterions != null) {
			for (Criterion criterion : criterions) {
				criteria.add(criterion);
			}
		}

		Optional<Pagination> paginationObj = Optional.ofNullable(pagination);
		Optional<String> sortColumnObj = Optional.ofNullable(paginationObj.get().getSortcolumn());
		if (sortColumnObj.isPresent() && paginationObj.isPresent()) {
			if (SORTINGORDER.ASC == SORTINGORDER.valueOf(paginationObj.get().getSortingOrder())) {
				criteria.addOrder(Order.asc(sortColumnObj.get()));
			} else if (SORTINGORDER.DESC == SORTINGORDER.valueOf(paginationObj.get().getSortingOrder())) {
				criteria.addOrder(Order.desc(sortColumnObj.get()).nulls(NullPrecedence.LAST));
			}
		}
		@SuppressWarnings("unchecked")
		List<UserListDetails> list = criteria.list();
		return list;
	}

	public User findUserUserListDetailsByUserListIdAndEmailId(String emailId) {
		String queryString = "select distinct u.userId as \"emailId\",u.user_id as \"id\",u.company_id as \"companyId\" from xt_user_profile u where uul.user_list_id = :userListId "
				+ "and u.user_id = uul.user_id and u.email_id = :emailId";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("emailId", emailId));
		return (User) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public Integer getUserUserListIdByUserListIdAndUserId(Integer userListId, Integer userId) {
		String sqlString = "select id from xt_user_userlist where user_list_id = :userListId and user_id = :userId";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(sqlString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("userListId", userListId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("userId", userId));
		return (Integer) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
	}

	/**** XNFR-791 *******/
	@Override
	public boolean isPublishedAllUserswithInList(Integer dashboardButtonId, List<Integer> publishedPartnerGroupIds) {
		List<Integer> alreadyPublishedPartnerUserIds = findPublishedPartnerIdsById(dashboardButtonId);
		Collections.sort(alreadyPublishedPartnerUserIds);
		List<Integer> groupPartnerIds = findUserIdsByUserListIds(publishedPartnerGroupIds);
		Collections.sort(groupPartnerIds);
		return alreadyPublishedPartnerUserIds.equals(groupPartnerIds);
	}

	@Override
	public boolean validateListName(String listName, Integer companyId, String module) {
		boolean isDuplicate = false;
		if (org.apache.commons.lang3.StringUtils.isNotBlank(listName)) {
			isDuplicate = isDuplicateListName(listName, companyId, module);
		}
		return !isDuplicate;
	}

	@Override
	public boolean validateAssignedListName(String listName, Integer assignedCompanyId, String module) {
		boolean isDuplicate = false;
		Session session = sessionFactory.getCurrentSession();
		Query query = session
				.createSQLQuery("SELECT CASE WHEN  count(*) > 0 THEN true ELSE false END from xt_user_list ul "
						+ " where ul.user_list_name ilike :listName and ul.assigned_company_id = :assignedCompanyId and ul.module_name = :module");
		query.setParameter("listName", listName);
		query.setParameter("assignedCompanyId", assignedCompanyId);
		query.setParameter("module", module);
		isDuplicate = (boolean) query.uniqueResult();
		return !isDuplicate;
	}

	@Override
	public Map<String, Object> fetchContactsFromUserList(Pagination pagination) {
		try {
			HashMap<String, Object> map = new HashMap<>();
			String finalQueryString = "SELECT DISTINCT\n" + "uul.user_id AS \"userId\",\n"
					+ "uul.id AS \"contactId\",\n" + "ul.user_list_id AS \"userListId\",\n"
					+ "ul.company_id AS \"companyId\",\n" + "u.email_id AS \"emailId\",\n"
					+ "COALESCE(uul.firstname, '') AS \"firstName\",\n"
					+ "COALESCE(uul.lastname, '') AS \"lastName\",\n"
					+ "COALESCE(uul.contact_company, '') AS \"companyName\",\n"
					+ "COALESCE(uul.zip, '') AS \"zipCode\",\n" + "COALESCE(uul.state, '') AS \"state\",\n"
					+ "COALESCE(uul.mobile_number, '') AS \"mobileNumber\",\n" + "COALESCE(uul.city, '') AS \"city\",\n"
					+ "COALESCE(uul.country, '') AS \"country\",\n" + "COALESCE(uul.job_title, '') AS \"jobTitle\",\n"
					+ "COALESCE(uul.address, '') AS \"address\",\n" + "u.is_email_valid AS \"validEmail\", \n"
					+ "xcs.stage_name as \"contactStatus\"  \n FROM " + "xt_user_list ul\n" + "JOIN\n"
					+ "xt_user_userlist uul ON ul.user_list_id = uul.user_list_id\n" + "JOIN\n"
					+ "xt_user_profile u ON u.user_id = uul.user_id\n" + "LEFT JOIN\n"
					+ "xt_partnership xp ON xp.partner_id = u.user_id\n" + "AND xp.vendor_company_id = ul.company_id\n"
					+ "LEFT JOIN xt_contact_status xcs ON xcs.id = uul.contact_status_id \n" + "WHERE "
					+ "ul.user_list_id = :userListId\n";

			if (XamplifyUtils.isValidString(pagination.getSearchKey())) {
				String searchKey = pagination.getSearchKey();
				StringBuilder searchbuilder = new StringBuilder();
				searchbuilder.append("and ( ");
				searchbuilder.append("uul.contact_company ilike '%" + searchKey + "%'");
				searchbuilder.append(" or uul.mobile_number ilike '%" + searchKey + "%'");
				searchbuilder.append(" or u.email_id ilike '%" + searchKey + "%'");
				searchbuilder.append(" or uul.job_title ilike '%" + searchKey + "%'");
				searchbuilder.append(" or uul.address ilike '%" + searchKey + "%'");
				searchbuilder.append(" or xcs.stage_name ilike '%" + searchKey + "%'");
				searchKey = searchKey.trim();
				String[] nameWords = searchKey.split("\\s+");
				if (nameWords.length == 1) {
					searchbuilder.append(" or uul.firstname ilike '%" + searchKey + "%'");
					searchbuilder.append(" or uul.lastname ilike '%" + searchKey + "%'");
					searchbuilder.append(
							" or REPLACE((uul.firstname || uul.lastname), ' ', '') " + "ilike '%" + searchKey + "%'");
					searchbuilder.append(
							" or REPLACE((uul.lastname || uul.firstname), ' ', '') " + "ilike '%" + searchKey + "%'");
				} else {
					for (String nameWord : nameWords) {
						searchbuilder.append(" or uul.firstname ilike '%" + nameWord + "%'");
						searchbuilder.append(" or uul.lastname ilike '%" + nameWord + "%'");
					}
				}
				searchbuilder.append(" ) ");
				finalQueryString += String.valueOf(searchbuilder);
			}

			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(finalQueryString);
			query.setParameter("userListId", pagination.getUserListId());
			return paginationUtil.setScrollableAndGetList(pagination, map, query, UserListUsersView.class);
		} catch (HibernateException | DamDataAccessException e) {
			throw new UserListException(e);
		} catch (Exception ex) {
			throw new UserListException(ex);
		}
	}

	public User getCopyListSelectedUser(UserUserList existingUserUserList) {
		User existingUser = existingUserUserList.getUser();

		User user = new User();
		user.setUserId(existingUser.getUserId());
		user.setAlias(existingUser.getAlias());
		user.setEmailValid(existingUser.isEmailValid());
		user.setFirstName(existingUserUserList.getFirstName());
		user.setLastName(existingUserUserList.getLastName());
		user.setEmailId(existingUser.getEmailId());
		user.setCompanyName(existingUserUserList.getContactCompany());
		user.setAddress(existingUserUserList.getAddress());
		user.setZipCode(existingUserUserList.getZipCode());
		user.setCity(existingUserUserList.getCity());
		user.setState(existingUserUserList.getState());
		user.setCountry(existingUserUserList.getCountry());
		user.setMobileNumber(existingUserUserList.getMobileNumber());
		user.setContactCompany(existingUserUserList.getContactCompany());
		user.setUnsubscribed(existingUser.isUnsubscribed());

		CompanyProfile companyProfile = existingUser.getCompanyProfile();
		if (companyProfile != null) {
			Integer companyId = companyProfile.getId();
			companyProfile.setId(companyId);
			user.setCompanyProfile(companyProfile);
		}
		return user;
	}

	@Override
	public Integer findDefaultContactListIdByCompanyId(Integer companyId, String moduleName) {
		String sqlString = "select user_list_id from xt_user_list where company_id = :companyId and ";
		if (XamplifyUtils.CONTACTS.equalsIgnoreCase(moduleName)) {
			sqlString += "contact_list_type is not null";
		} else {
			sqlString += "is_default_partnerlist is true";
		}
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(sqlString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId));
		return (Integer) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
	}

	public void updatePreviousUsersListByUserListId(Integer userListId) {
		if (XamplifyUtils.isValidInteger(userListId)) {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createSQLQuery(
					"update xt_user_list  set  is_team_member_partner_list = false, team_member_id = null where user_list_id=:userListId");
			query.setParameter("userListId", userListId);
			query.executeUpdate();
		}
	}

	@Override
	public Map<String, Object> deleteContactFromAllContactLists(Integer contactId, Integer loggedInUserId) {
		Map<String, Object> map = new HashMap<>();
		
		return map;

	}
	
	@SuppressWarnings("unchecked")
	private List<Integer> findContactListIds(Integer contactId, Integer loggedInUserCompanyId, Session session) {
		String contactTypesInString = "'" + UserList.TYPE.CONTACT.name() + "'," + "'" + UserList.TYPE.LEAD.name() + "',"
				+ "'" + UserList.TYPE.CONTACT_LISTVIEWS.name() + "'," + "'" + UserList.TYPE.LEAD_LISTVIEWS.name() + "'";
		String findContactListIdsQuery = " select ul.user_list_id from xt_user_list ul,xt_user_userlist uul where ul.user_list_id = uul.user_list_id "
				+ " and uul.user_id = " + contactId + " and ul.company_id = " + loggedInUserCompanyId
				+ " and is_partner_userlist  = false and ul.contact_type in (" + contactTypesInString + ")  ";
		return session.createSQLQuery(findContactListIdsQuery).list();
	}

}
