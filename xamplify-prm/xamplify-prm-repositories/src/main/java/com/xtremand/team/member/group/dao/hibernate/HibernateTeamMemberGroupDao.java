package com.xtremand.team.member.group.dao.hibernate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.xtremand.common.bom.Pagination;
import com.xtremand.exception.DuplicateEntryException;
import com.xtremand.partnership.dto.PartnerTeamMemberGroupDTO;
import com.xtremand.team.member.dto.AddTeamMemberGroup;
import com.xtremand.team.member.dto.TeamMemberGroupDTO;
import com.xtremand.team.member.group.bom.TeamMemberGroup;
import com.xtremand.team.member.group.dao.TeamMemberGroupDao;
import com.xtremand.team.member.group.exception.TeamMemberGroupDataAccessException;
import com.xtremand.user.bom.Role;
import com.xtremand.util.PaginationUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.HibernateSQLQueryResultUtilDao;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.QueryParameterDTO;
import com.xtremand.util.dto.QueryParameterListDTO;
import com.xtremand.util.dto.SortColumnDTO;
import com.xtremand.util.dto.XamplifyConstants;

@Repository
public class HibernateTeamMemberGroupDao implements TeamMemberGroupDao {

	private static final String COMPANY_ID = "companyId";

	private static final String ROLE_ID = "roleId";

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private PaginationUtil paginationUtil;

	@Autowired
	private HibernateSQLQueryResultUtilDao hibernateSqlQueryResultUtilDao;

	@Value("${query.findTeamMemberGroups}")
	private String findTeamMemberGroupsQuery;

	@Value("${query.groupByTeamMemberGroups}")
	private String groupByTeamMemberGroupsQuery;

	@Value("${query.searchTeamMemberGroups}")
	private String searchTeamMemberGroupsQuery;

	@Value("${query.isGroupAssingedToSecondAdmin}")
	private String isGroupAssingedToSecondAdminQuery;

	@Value("${query.teamMemberGroup.companyId}")
	private String companyIdFilter;

	@Value("${query.teamMemberGroupId}")
	private String teamMemberGroupIdFilter;

	private static final String TEAM_MEMBER_GROUP_ID = "teamMemberGroupId";

	@Override
	public Map<String, Object> findGroups(Pagination pagination) {
		try {
			HashMap<String, Object> map = new HashMap<>();
			String finalQueryString = "";
			String sortQueryString = "";
			/******* XNFR-108 *********/
			if (!StringUtils.hasText(pagination.getSortcolumn()) && pagination.getFiltertedEmailTempalteIds() != null
					&& !pagination.getFiltertedEmailTempalteIds().isEmpty()) {
				sortQueryString = orderByArrayOrCreatedTime(pagination, sortQueryString);
			} else {
				sortQueryString = getSortQuery(pagination);
			}
			/******* XNFR-108 ENDS *********/

			String searchKey = pagination.getSearchKey();
			boolean hasSearchKey = StringUtils.hasText(searchKey);
			if (hasSearchKey) {
				finalQueryString = findTeamMemberGroupsQuery + " " + companyIdFilter
						+ searchTeamMemberGroupsQuery.replace("searchKey", searchKey) + " "
						+ groupByTeamMemberGroupsQuery + " " + sortQueryString;
			} else {
				finalQueryString = findTeamMemberGroupsQuery + " " + companyIdFilter + " "
						+ groupByTeamMemberGroupsQuery + " " + sortQueryString;
			}
			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(finalQueryString);
			query.setParameter("userId", pagination.getUserId());
			return paginationUtil.setScrollableAndGetList(pagination, map, query, TeamMemberGroupDTO.class);
		} catch (HibernateException | TeamMemberGroupDataAccessException e) {
			throw new TeamMemberGroupDataAccessException(e);
		} catch (Exception ex) {
			throw new TeamMemberGroupDataAccessException(ex);
		}

	}

	private String getSortQuery(Pagination pagination) {
		List<SortColumnDTO> sortColumnDTOs = new ArrayList<>();
		SortColumnDTO sortByName = new SortColumnDTO("name", "tmg.name", false, true, false);
		SortColumnDTO sortByCreatedTime = new SortColumnDTO("createdTime", "tmg.created_time", true, false, false);
		SortColumnDTO sortByUpdatedTime = new SortColumnDTO("updatedTime", "tmg.updated_time", false, false, false);
		sortColumnDTOs.add(sortByName);
		sortColumnDTOs.add(sortByCreatedTime);
		sortColumnDTOs.add(sortByUpdatedTime);
		return paginationUtil.generateSortQuery(pagination, sortColumnDTOs, "desc");
	}

	/******* XNFR-108 *********/

	private String orderByArrayOrCreatedTime(Pagination pagination, String sortOptionQueryString) {
		sortOptionQueryString += "   order by array_position(array" + pagination.getFiltertedEmailTempalteIds()
				+ ", tmg.id),tmg.created_time DESC";
		return sortOptionQueryString;
	}

	/******* XNFR-108 ENDS *********/

	@Override
	public TeamMemberGroup findTeamMemberGroupById(Integer id) {
		try {
			Session session = sessionFactory.getCurrentSession();
			return (TeamMemberGroup) session.createCriteria(TeamMemberGroup.class).add(Restrictions.eq("id", id))
					.uniqueResult();
		} catch (HibernateException | TeamMemberGroupDataAccessException e) {
			throw new TeamMemberGroupDataAccessException(e);
		} catch (Exception ex) {
			throw new TeamMemberGroupDataAccessException(ex);
		}
	}

	@Override
	public void deleteUnMappedRoleIds(Integer teamMemberGroupId, Set<Integer> unmappedRoleIds) {
		try {
			if (unmappedRoleIds != null && !unmappedRoleIds.isEmpty()) {
				Session session = sessionFactory.getCurrentSession();
				Query query = session.createQuery(
						"delete from TeamMemberGroupRoleMapping where teamMemberGroup.id=:teamMemberGroupId and roleId in (:unmappedRoleIds)");
				query.setParameter(TEAM_MEMBER_GROUP_ID, teamMemberGroupId);
				query.setParameterList("unmappedRoleIds", unmappedRoleIds);
				query.executeUpdate();
			}
		} catch (HibernateException | TeamMemberGroupDataAccessException e) {
			throw new TeamMemberGroupDataAccessException(e);
		} catch (Exception ex) {
			throw new TeamMemberGroupDataAccessException(ex);
		}

	}

	@Override
	public void delete(Integer id) {
		try {
			if (id != null && id > 0) {
				Session session = sessionFactory.getCurrentSession();
				Query query = session.createQuery("delete from TeamMemberGroup where id=:id");
				query.setParameter("id", id);
				query.executeUpdate();
			}
		} catch (ConstraintViolationException con) {
			throw new DuplicateEntryException(
					"This group cannot be deleted.Because one or more team members are part of this group.");
		} catch (HibernateException | TeamMemberGroupDataAccessException e) {
			throw new TeamMemberGroupDataAccessException(e);
		} catch (Exception ex) {
			throw new TeamMemberGroupDataAccessException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TeamMemberGroupDTO> findTeamMemberGroupRoleMappingIdsAndNamesByRoleIdAndCompanyId(Integer companyId,
			Integer roleId) {
		try {
			String sqlQueryString = "select distinct b.id as \"mappingId\",a.name as \"name\",a.id as \"id\" from xt_team_member_group a,xt_team_member_group_role_mapping b where a.company_id = :companyId "
					+ " and a.id = b.team_member_group_id and b.role_id = :roleId";
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createSQLQuery(sqlQueryString).setParameter(COMPANY_ID, companyId)
					.setParameter(ROLE_ID, roleId);
			return query.setResultTransformer(Transformers.aliasToBean(TeamMemberGroupDTO.class)).list();
		} catch (HibernateException | TeamMemberGroupDataAccessException e) {
			throw new TeamMemberGroupDataAccessException(e);
		} catch (Exception ex) {
			throw new TeamMemberGroupDataAccessException(ex);
		}

	}

	@Override
	public void deleteFromTeamMemberGroupRoleMapping(List<Integer> ids) {
		try {
			if (ids != null && !ids.isEmpty()) {
				Session session = sessionFactory.getCurrentSession();
				Query query = session.createQuery("delete from TeamMemberGroupRoleMapping where id in (:ids)");
				query.setParameterList("ids", ids);
				query.executeUpdate();
			}
		} catch (HibernateException | TeamMemberGroupDataAccessException e) {
			throw new TeamMemberGroupDataAccessException(e);
		} catch (Exception ex) {
			throw new TeamMemberGroupDataAccessException(ex);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TeamMemberGroupDTO> findTeamMemberGroupIdsAndNamesByRoleIdAndCompanyId(Integer companyId,
			Integer roleId) {
		try {
			String sqlQueryString = "select distinct a.id as \"id\",a.name as \"name\" from xt_team_member_group a,xt_team_member_group_role_mapping b where a.company_id = :companyId "
					+ " and a.id = b.team_member_group_id and b.role_id = :roleId";
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createSQLQuery(sqlQueryString).setParameter(COMPANY_ID, companyId)
					.setParameter(ROLE_ID, roleId);
			return query.setResultTransformer(Transformers.aliasToBean(TeamMemberGroupDTO.class)).list();
		} catch (HibernateException | TeamMemberGroupDataAccessException e) {
			throw new TeamMemberGroupDataAccessException(e);
		} catch (Exception ex) {
			throw new TeamMemberGroupDataAccessException(ex);
		}

	}

	@Override
	public boolean isTeamMemberGroupRoleMappingRowExists(Integer teamMemberGroupId, Integer roleId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String sqlQueryString = " select case when count(*)>0 then true else false end from xt_team_member_group_role_mapping where team_member_group_id = :teamMemberGroupId and role_id = :roleId";
			return (boolean) session.createSQLQuery(sqlQueryString)
					.setParameter(TEAM_MEMBER_GROUP_ID, teamMemberGroupId).setParameter(ROLE_ID, roleId).uniqueResult();
		} catch (HibernateException | TeamMemberGroupDataAccessException e) {
			throw new TeamMemberGroupDataAccessException(e);
		} catch (Exception ex) {
			throw new TeamMemberGroupDataAccessException(ex);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findRoleIdsByTeamMemberGroupId(Integer teamMemberGroupId) {
		try {
			String sqlQueryString = "select distinct role_id from xt_team_member_group_role_mapping where team_member_group_id=:teamMemberGroupId";
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createSQLQuery(sqlQueryString).setParameter(TEAM_MEMBER_GROUP_ID, teamMemberGroupId);
			return query.list();
		} catch (HibernateException | TeamMemberGroupDataAccessException e) {
			throw new TeamMemberGroupDataAccessException(e);
		} catch (Exception ex) {
			throw new TeamMemberGroupDataAccessException(ex);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TeamMemberGroup> findGroupIdsAndNamesByCompanyId(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = findAllGroupsWithTeamMemberCountOrderedByDefaultSSOIdAndName(null);
		SQLQuery query = session.createSQLQuery(queryString);
		query.setParameter(XamplifyConstants.COMPANY_ID, companyId);
		return query.setResultTransformer(Transformers.aliasToBean(TeamMemberGroup.class)).list();
	}

	private String findAllGroupsWithTeamMemberCountOrderedByDefaultSSOIdAndName(Integer defaultSSOGroupId) {
		StringBuilder queryString = new StringBuilder(
				"SELECT tg.id AS \"id\", tg.name, CAST(COUNT(tgum.*) AS int) AS \"teamMembersCount\", "
				+ "tg.is_default_sso_group as \"defaultSsoGroup\", tg.alias as \"alias\" \n"
				+ "FROM xt_team_member_group tg LEFT JOIN xt_team_member_group_user_mapping tgum "
				+ "ON tgum.team_member_group_id = tg.id \n WHERE tg.company_id = :companyId " + "GROUP BY tg.id");
		if (XamplifyUtils.isValidInteger(defaultSSOGroupId)) {
			queryString.append(" ORDER BY (CASE WHEN tg.id = :defaultSSOGroupId THEN 0 ELSE 1 END), tg.name ASC");
		} else {
			queryString.append(" ORDER BY tg.name ASC");
		}

		return queryString.toString();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<AddTeamMemberGroup> addTeamMemberGroupsToCompanies() {
		try {
			String finalQueryString = "select ROW_NUMBER () OVER () as \"rowId\", t.id as \"teamMemberId\",string_agg(distinct cast(ur.role_id as text), ',') as \"roleIdsInString\","
					+ "cp.company_id as \"companyId\",cp.company_profile_name as \"companyProfileName\",t.org_admin_id as \"orgAdminId\""
					+ ", t.team_member_id as \"teamMemberUserId\" from xt_team_member t,xt_user_role ur,xt_company_profile cp "
					+ " where ur.user_id = t.team_member_id and cp.company_id = t.company_id group by t.id,cp.company_id order by cp.company_id asc";
			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(finalQueryString);
			ScrollableResults scrollableResults = query.scroll();
			scrollableResults.last();
			return query.setResultTransformer(Transformers.aliasToBean(AddTeamMemberGroup.class)).list();
		} catch (HibernateException | TeamMemberGroupDataAccessException e) {
			throw new TeamMemberGroupDataAccessException(e);
		} catch (Exception ex) {
			throw new TeamMemberGroupDataAccessException(ex);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findTeamMemberUserIdsByRoleIdAndCompanyId(Integer companyId, Integer roleId) {
		try {
			String sqlQueryString = " select distinct a.team_member_id from xt_team_member a,xt_team_member_group b,xt_team_member_group_user_mapping c, "
					+ " xt_team_member_group_role_mapping d where a.company_id = :companyId and b.company_id = a.company_id and c.team_member_group_id = b.id and a.id = c.team_member_id "
					+ " and d.team_member_group_id = b.id and d.role_id = :roleId";
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createSQLQuery(sqlQueryString).setParameter(COMPANY_ID, companyId)
					.setParameter(ROLE_ID, roleId);
			return query.list();
		} catch (HibernateException | TeamMemberGroupDataAccessException e) {
			throw new TeamMemberGroupDataAccessException(e);
		} catch (Exception ex) {
			throw new TeamMemberGroupDataAccessException(ex);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findTeamMemberUserIdsByGroupId(Integer teamMemberGroupId) {
		try {
			String sqlQueryString = " select distinct a.team_member_id from xt_team_member a,xt_team_member_group_user_mapping c, xt_team_member_group b left join "
					+ " xt_team_member_group_role_mapping d on b.id = d.team_member_group_id  where  b.company_id = a.company_id and c.team_member_group_id = b.id and a.id = c.team_member_id "
					+ " and b.id = :teamMemberGroupId";
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createSQLQuery(sqlQueryString).setParameter(TEAM_MEMBER_GROUP_ID, teamMemberGroupId);
			return query.list();
		} catch (HibernateException | TeamMemberGroupDataAccessException e) {
			throw new TeamMemberGroupDataAccessException(e);
		} catch (Exception ex) {
			throw new TeamMemberGroupDataAccessException(ex);
		}

	}

	@Override
	public boolean hasSuperVisorRole(Integer teamMemberGroupId) {
		try {
			String sqlQueryString = " select case when cast(count(b.id) as int)>0 then true else false end "
					+ " from xt_team_member_group a,xt_team_member_group_role_mapping b where a.id = b.team_member_group_id and a.id = :teamMemberGroupId"
					+ " and b.role_id = :roleId";
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createSQLQuery(sqlQueryString).setParameter(TEAM_MEMBER_GROUP_ID, teamMemberGroupId)
					.setParameter(ROLE_ID, Role.ALL_ROLES.getRoleId());
			return (boolean) query.uniqueResult();
		} catch (HibernateException | TeamMemberGroupDataAccessException e) {
			throw new TeamMemberGroupDataAccessException(e);
		} catch (Exception ex) {
			throw new TeamMemberGroupDataAccessException(ex);
		}

	}

	@Override
	public boolean isGroupAssignedToSecondAdmin(Integer teamMemberGroupId) {
		Session session = sessionFactory.getCurrentSession();
		return (boolean) session.createSQLQuery(isGroupAssingedToSecondAdminQuery)
				.setParameter(TEAM_MEMBER_GROUP_ID, teamMemberGroupId).uniqueResult();
	}

	@Override
	public String findTeamMemberGroupNameById(Integer id) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createQuery("select name from TeamMemberGroup where id=:id").setParameter("id", id);
		return (String) query.uniqueResult();
	}

	@Override
	public TeamMemberGroupDTO previewGroupById(Integer id) {
		String queryString = findTeamMemberGroupsQuery + " " + teamMemberGroupIdFilter + " "
				+ groupByTeamMemberGroupsQuery;
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("groupId", id);
		return (TeamMemberGroupDTO) query.setResultTransformer(Transformers.aliasToBean(TeamMemberGroupDTO.class))
				.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findTeamMemberGroupRoleIdsByTeamMemberUserId(Integer teamMemberUserId) {
		String sqlQueryString = " select distinct  r.role_id from xt_team_member tm,xt_team_member_group_user_mapping u,xt_team_member_group_role_mapping r"
				+ " where tm.id = u.team_member_id and tm.team_member_id = :teamMemberUserId and u.team_member_group_id = r.team_member_group_id group by u.id,r.role_id";
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(sqlQueryString).setParameter("teamMemberUserId", teamMemberUserId);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TeamMemberGroupDTO> findDefaultGroupsByCompanyId(Integer companyId) {
		try {
			String sqlQueryString = " select id as \"id\",name as \"name\" from xt_team_member_group where company_id=:companyId and is_default";
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createSQLQuery(sqlQueryString).setParameter(COMPANY_ID, companyId);
			return query.setResultTransformer(Transformers.aliasToBean(TeamMemberGroupDTO.class)).list();
		} catch (HibernateException | TeamMemberGroupDataAccessException e) {
			throw new TeamMemberGroupDataAccessException(e);
		} catch (Exception ex) {
			throw new TeamMemberGroupDataAccessException(ex);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findDefaultGroupCompanyIds(String groupName) {
		String sqlQueryString = "select distinct company_id from xt_team_member_group where is_default and name=:name";
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(sqlQueryString);
		query.setParameter("name", groupName);
		return query.list();
	}

	/******* XNFR-85 *********/
	@SuppressWarnings("unchecked")
	@Override
	public List<PartnerTeamMemberGroupDTO> findTeamMemberGroupIdByPartnershipId(Integer partnershipId) {
		Session session = sessionFactory.getCurrentSession();
		String sqlString = "select distinct tg.id as \"id\",cast(count(ptgm.id) as int) as \"count\",tg.name as \"teamMemberGroupName\" from xt_team_member_group tg,xt_team_member_group_user_mapping tgum, "
				+ " xt_partner_team_group_mapping ptgm where tgum.team_member_group_id = tg.id and tgum.id = ptgm.team_member_group_user_mapping_id "
				+ " and ptgm.partnership_id = :partnershipId group by tg.id";
		Query query = session.createSQLQuery(sqlString).setParameter("partnershipId", partnershipId);
		return query.setResultTransformer(Transformers.aliasToBean(PartnerTeamMemberGroupDTO.class)).list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<Integer> findSelectedTeamMemberGroupUserMappingIdsByPartnershipId(Integer partnershipId) {
		if (partnershipId != null && partnershipId > 0) {
			String sqlString = "select  team_member_group_user_mapping_id from xt_partner_team_group_mapping where partnership_id = :partnershipId order by updated_time desc";
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createSQLQuery(sqlString).setParameter("partnershipId", partnershipId);
			return new HashSet<>(query.list());
		} else {
			return new HashSet<>();
		}

	}

	@Override
	public boolean isTeamMemberGroupAssignedToPartnerByTeamMemberGroupId(Integer teamMemberGroupId,
			Integer teamMemberId) {
		Session session = sessionFactory.getCurrentSession();
		String sqlString = " select case when count(*)>0 then true else false end as count from xt_partner_team_group_mapping ptgm,xt_team_member_group_user_mapping tgum "
				+ " where tgum.id = ptgm.team_member_group_user_mapping_id and tgum.team_member_group_id  = :teamMemberGroupId and tgum.team_member_id = :teamMemberId";
		Query query = session.createSQLQuery(sqlString).setParameter(TEAM_MEMBER_GROUP_ID, teamMemberGroupId)
				.setParameter("teamMemberId", teamMemberId);
		return (boolean) query.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findTeamMemberIdsByTeamMemberGroupUserMappingIds(List<Integer> teamMemberGroupUserMappingIds) {
		if (teamMemberGroupUserMappingIds != null && !teamMemberGroupUserMappingIds.isEmpty()) {
			String sqlQueryString = "select distinct team_member_id from xt_team_member_group_user_mapping where id in (:teamMemberGroupUserMappingIds)";
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createSQLQuery(sqlQueryString);
			query.setParameterList("teamMemberGroupUserMappingIds", teamMemberGroupUserMappingIds);
			return query.list();
		} else {
			return new ArrayList<>();
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findAssociatedPartnershipIdsByTeamMemberGroupId(Integer teamMemberGroupId) {
		if (teamMemberGroupId != null && teamMemberGroupId > 0) {
			String sqlQueryString = "select distinct ptgm.partnership_id from xt_partner_team_group_mapping ptgm,xt_team_member_group_user_mapping tgum,"
					+ " xt_team_member_group tg where ptgm.team_member_group_user_mapping_id = tgum.id and tgum.team_member_group_id  = tg.id and tg.id = :teamMemberGroupId "
					+ " order by 1 desc";
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createSQLQuery(sqlQueryString);
			query.setParameter(TEAM_MEMBER_GROUP_ID, teamMemberGroupId);
			return query.list();
		} else {
			return new ArrayList<>();
		}

	}

	/******* XNFR-108 *********/

	@SuppressWarnings("unchecked")
	@Override
	public Set<Integer> findSelectedTeamMemberGroupUserMappingIdsByFormId(Integer formId) {
		if (formId != null && formId > 0) {
			String sqlString = "select  team_member_group_user_mapping_id from xt_form_team_group_mapping where form_id = :formId order by updated_time desc";
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createSQLQuery(sqlString).setParameter("formId", formId);
			return new HashSet<>(query.list());
		} else {
			return new HashSet<>();
		}

	}

	@Override
	public void deleteByCompanyId(Integer companyId) {
		try {
			if (companyId != null && companyId > 0) {
				Session session = sessionFactory.getCurrentSession();
				Query query = session.createQuery("delete from TeamMemberGroup where companyId=:companyId");
				query.setParameter(XamplifyConstants.COMPANY_ID, companyId);
				query.executeUpdate();
			}
		} catch (ConstraintViolationException con) {
			throw new DuplicateEntryException(
					"This group cannot be deleted.Because one or more team members are part of this group.");
		} catch (HibernateException | TeamMemberGroupDataAccessException e) {
			throw new TeamMemberGroupDataAccessException(e);
		} catch (Exception ex) {
			throw new TeamMemberGroupDataAccessException(ex);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findIdsByCompanyId(Integer companyId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String hql = "SELECT t.id FROM TeamMemberGroup t where t.companyId = :companyId";
			Query query = session.createQuery(hql);
			query.setParameter(XamplifyConstants.COMPANY_ID, companyId);
			return query.list();
		} catch (HibernateException | TeamMemberGroupDataAccessException e) {
			throw new TeamMemberGroupDataAccessException(e);
		} catch (Exception ex) {
			throw new TeamMemberGroupDataAccessException(ex);
		}

	}

	@Override
	public void deleteFromTeamMemberGroupUserMapping(List<Integer> teamMemberGroupIds) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String deleteFromTeamMemberGroupUserMappingHQLQueryString = "DELETE FROM TeamMemberGroupUserMapping "
					+ " WHERE teamMemberGroup.id in (:teamMemberGroupIds)";
			Query teamMemberGroupUserMapping = session.createQuery(deleteFromTeamMemberGroupUserMappingHQLQueryString);
			teamMemberGroupUserMapping.setParameterList("teamMemberGroupIds", teamMemberGroupIds);
			teamMemberGroupUserMapping.executeUpdate();
		} catch (HibernateException | TeamMemberGroupDataAccessException e) {
			throw new TeamMemberGroupDataAccessException(e);
		} catch (Exception ex) {
			throw new TeamMemberGroupDataAccessException(ex);
		}

	}

	@Override
	public void changeDefaultGroup(Integer companyId, Integer userId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String updateDefaultTeamMemberGroupHQLQueryString = "Update TeamMemberGroup set defaultGroup=false,createdUserId=:userId,updatedUserId=:userId where defaultGroup = true and companyId = :companyId";
			Query teamMemberGroupQuery = session.createQuery(updateDefaultTeamMemberGroupHQLQueryString);
			teamMemberGroupQuery.setParameter(XamplifyConstants.COMPANY_ID, companyId);
			teamMemberGroupQuery.setParameter("userId", userId);
			teamMemberGroupQuery.executeUpdate();
		} catch (HibernateException | TeamMemberGroupDataAccessException e) {
			throw new TeamMemberGroupDataAccessException(e);
		} catch (Exception ex) {
			throw new TeamMemberGroupDataAccessException(ex);
		}

	}

	@Override
	public void deleteTeamMemberGroupRoleMappings(List<Integer> teamMemberGroupIds) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String deleteFromTeamMemberGroupRoleMappingHQLQueryString = "DELETE FROM TeamMemberGroupRoleMapping "
					+ " WHERE teamMemberGroup.id in (:teamMemberGroupIds)";
			Query teamMemberGroupRoleMapping = session.createQuery(deleteFromTeamMemberGroupRoleMappingHQLQueryString);
			teamMemberGroupRoleMapping.setParameterList("teamMemberGroupIds", teamMemberGroupIds);
			teamMemberGroupRoleMapping.executeUpdate();
		} catch (HibernateException | TeamMemberGroupDataAccessException e) {
			throw new TeamMemberGroupDataAccessException(e);
		} catch (Exception ex) {
			throw new TeamMemberGroupDataAccessException(ex);
		}

	}

	@Override
	public TeamMemberGroup findGroupIdsByPartnerCompanyId(Integer companyId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			return (TeamMemberGroup) session.createCriteria(TeamMemberGroup.class)
					.add(Restrictions.eq(XamplifyConstants.COMPANY_ID, companyId)).uniqueResult();
		} catch (HibernateException | TeamMemberGroupDataAccessException e) {
			throw new TeamMemberGroupDataAccessException(e);
		} catch (Exception ex) {
			throw new TeamMemberGroupDataAccessException(ex);
		}
	}

	/** XNFR-883 ***/
	@Override
	public void setDefaultSSOGroupToFalseByCompanyId(Integer companyId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(
				"update xt_team_member_group set is_default_sso_group = false where company_id = :companyId");
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(COMPANY_ID, companyId));
		hibernateSqlQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);
	}

	/** XNFR-883 ***/

	/** XNFR-883 ***/
	@Override
	public void updateDefaultSSOGroupById(Integer id) {
		try {
			Session session = sessionFactory.getCurrentSession();
			Integer companyId = (Integer) session
					.createSQLQuery("select company_id from xt_team_member_group where id = :id").setParameter("id", id)
					.uniqueResult();

			session.createSQLQuery(
					"update xt_team_member_group set is_default_sso_group = false where company_id = :companyId")
					.setParameter(COMPANY_ID, companyId).executeUpdate();
			session.createSQLQuery("update xt_team_member_group set is_default_sso_group = true where id = :id")
					.setParameter("id", id).executeUpdate();

		} catch (HibernateException | TeamMemberGroupDataAccessException e) {
			throw new TeamMemberGroupDataAccessException(e);
		} catch (Exception ex) {
			throw new TeamMemberGroupDataAccessException(ex);
		}
	}

	/*** XNFR-883 ****/
	@SuppressWarnings("unchecked")
	@Override
	public List<TeamMemberGroup> findGroupIdsAndNamesByCompanyIdAndDefaultSSOGroupId(Integer companyId,
			Integer defaultSSOGroupId) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = findAllGroupsWithTeamMemberCountOrderedByDefaultSSOIdAndName(defaultSSOGroupId);
		SQLQuery query = session.createSQLQuery(queryString);
		query.setParameter(XamplifyConstants.COMPANY_ID, companyId);
		query.setParameter("defaultSSOGroupId", defaultSSOGroupId);
		return query.setResultTransformer(Transformers.aliasToBean(TeamMemberGroup.class)).list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getPartnerTeamGroupMapping(Integer teamMemberGroupId,Integer teamMemberId) {
		String queryString = "select ptgm.id from xt_partner_team_group_mapping ptgm, xt_team_member_group_user_mapping tgum "
				 + "where tgum.id = ptgm.team_member_group_user_mapping_id and tgum.team_member_group_id  = :teamMemberGroupId "
				 + "and tgum.team_member_id = :teamMemberId ";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
		.add(new QueryParameterDTO(TEAM_MEMBER_GROUP_ID, teamMemberGroupId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
		.add(new QueryParameterDTO("teamMemberId", teamMemberId));
		return (List<Integer>) hibernateSqlQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public void deletePartnerTeamGroupMapping(List<Integer> partnerTeamGroupMappingIds) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String sql = "delete from xt_partner_team_group_mapping where id in (:partnerTeamGroupMappingIds)";
		hibernateSQLQueryResultRequestDTO.setQueryString(sql);
		hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs()
		.add(new QueryParameterListDTO("partnerTeamGroupMappingIds", partnerTeamGroupMappingIds));
		hibernateSqlQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public boolean isLastTeamMemberGroupById(Integer id) {
		String queryString = "SELECT CASE WHEN COUNT(*) = 1 THEN true ELSE false END AS \"is_last_group\" \n"
				+ "FROM xt_team_member_group WHERE company_id = (SELECT company_id FROM xt_team_member_group \n"
				+ "WHERE id = :id)";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("id", id));
		return hibernateSqlQueryResultUtilDao.returnBoolean(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public Map<String, Object> findPaginatedTeamMemberGroupSignUpUrls(Pagination pagination) {
		String searchKey = pagination.getSearchKey();
		Integer companyId = pagination.getCompanyId();
		String queryString = "SELECT tg.id AS \"id\", tg.name, CAST(COUNT(tgum.*) AS int) AS \"teamMembersCount\", "
				+ "tg.is_default_sso_group as \"defaultSsoGroup\", tg.alias as \"alias\" \n"
				+ "FROM xt_team_member_group tg LEFT JOIN xt_team_member_group_user_mapping tgum "
				+ "ON tgum.team_member_group_id = tg.id \n WHERE tg.company_id = :companyId ";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		List<String> searchColumns = new ArrayList<>();
		searchColumns.add("tg.name");
		hibernateSQLQueryResultRequestDTO.setGroupByQueryString(" GROUP BY tg.id ");
		hibernateSQLQueryResultRequestDTO.setSortQueryString(" ORDER BY tg.created_time DESC ");
		hibernateSQLQueryResultRequestDTO.setClassInstance(TeamMemberGroup.class);
		hibernateSQLQueryResultRequestDTO.setSearchColumns(searchColumns);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId));
		return hibernateSqlQueryResultUtilDao.returnPaginatedDTOList(hibernateSQLQueryResultRequestDTO, pagination,
				searchKey);
	}

}
