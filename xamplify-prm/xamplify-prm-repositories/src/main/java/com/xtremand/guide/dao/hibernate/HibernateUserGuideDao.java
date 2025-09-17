package com.xtremand.guide.dao.hibernate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.xtremand.common.bom.Pagination;
import com.xtremand.guide.bom.UserGuide;
import com.xtremand.guide.dao.UserGuideDao;
import com.xtremand.guide.dto.UserGuideDto;
import com.xtremand.module.dao.ModuleDao;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.util.PaginationUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.UtilDao;

@Repository
@Transactional
public class HibernateUserGuideDao implements UserGuideDao {

	private static final String DESCRIPTION = "description";
	private static final String TITLE = "title";
	private static final String MERGETAG_ID = "mergeTag.id";
	private static final String SUBMODULE_ID = "subModuleId";
	private static final String MODULEID = "module.id";
	private static final String MODULE_ID = "moduleId";

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private UserDAO userDao;

        @Autowired
        private PaginationUtil paginationUtil;

        @Autowired
        private ModuleDao moduleDao;

        @Autowired
        private UtilDao utilDao;

	public Integer getTagIdByName(String tagName) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select id as \"id\" from xt_merge_tag where name = :name";
		return (Integer) session.createSQLQuery(queryString).setParameter("name", tagName).uniqueResult(); // Return
																											// null if
																											// no result
																											// found
	}

	@Override
	public UserGuideDto getUserGuideByTagId(Integer id) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(UserGuide.class);
		return (UserGuideDto) criteria
				.setProjection(Projections.distinct(Projections.projectionList().add(Projections.property("id"), "id")
						.add(Projections.property(TITLE), TITLE).add(Projections.property("link"), "link")
						.add(Projections.property(DESCRIPTION), DESCRIPTION).add(Projections.property("slug"), "slug")
						.add(Projections.property(MODULEID), MODULE_ID)
						.add(Projections.property("subModule.id"), SUBMODULE_ID)
						.add(Projections.property(MERGETAG_ID), "mergeTagId")
						.add(Projections.property("createdUserId"), "createdBy")
						.add(Projections.property("updatedUserId"), "updatedBy")
						.add(Projections.property("createdTime"), "createdDate")
						.add(Projections.property("updatedTime"), "updatedDate")))
				.setResultTransformer(Transformers.aliasToBean(UserGuideDto.class))
				.add(Restrictions.eq(MERGETAG_ID, id)).uniqueResult();
	}

	@Override
	public List<UserGuideDto> getUserGudesByModuleId(Integer moduleId, List<Object> subModuleIds,
			List<Object> guideTitles) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "";

		if (moduleId != 1 && moduleId != 8 && moduleId != 15) {
			queryString = "select DISTINCT u.title as \"title\" ,s.id as \"subModuleId\",u.id as \"id\", u.created_time as \"createdDate\" , u.module_id as \"moduleId\" , u.slug as \"slug\",u.description as \"description\",s.name as \"subModuleName\" from  xt_user_guide u "
					+ " join xt_sub_module s on  u.sub_module_id = s.id  "
					+ " where u.module_id =:moduleId and u.sub_module_id in (:subModuleIds) and u.title in (:guideTitles) order by s.id,u.created_time,u.id ";
		} else {
			queryString = "select DISTINCT u.title as \"title\" ,u.sub_module_id as \"subModuleId\",u.id as \"id\", u.created_time as \"createdDate\" , u.module_id as \"moduleId\" , u.slug as \"slug\",u.description as \"description\" from  xt_user_guide u "
					+ " where u.module_id =:moduleId and u.title in (:guideTitles) order by u.sub_module_id,u.created_time,u.id ";
		}
		SQLQuery query = session.createSQLQuery(queryString);
		query.setParameter(MODULE_ID, moduleId);
		if (moduleId != 1 && moduleId != 8 && moduleId != 15) {
			query.setParameterList("subModuleIds", subModuleIds);
		}
		query.setParameterList("guideTitles", guideTitles);
		@SuppressWarnings("unchecked")
		List<UserGuideDto> userGuideDtos = query.setResultTransformer(Transformers.aliasToBean(UserGuideDto.class))
				.list();
		return userGuideDtos;
	}

	@Override
	public Map<String, Object> getUserGuideLnkByTitle(String title) {
		Session session = sessionFactory.getCurrentSession();
		Map<String, Object> map = new HashMap<>();
		Criteria criteria = session.createCriteria(UserGuide.class);

		UserGuideDto dto = (UserGuideDto) criteria
				.setProjection(Projections.distinct(Projections.projectionList()
						.add(Projections.property("slug"), "slug").add(Projections.property(MODULEID), "moduleId")
						.add(Projections.property("link"), "link")))
				.setResultTransformer(Transformers.aliasToBean(UserGuideDto.class)).add(Restrictions.eq(TITLE, title))
				.uniqueResult();

		map.put("slug", dto.getSlug());
		map.put("link", dto.getLink());
		map.put("moduleId", dto.getModuleId());

		return map;
	}

	@Override
	public UserGuideDto getUserGuideBySlug(String slug) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(UserGuide.class);
		return (UserGuideDto) criteria
				.setProjection(Projections.distinct(Projections.projectionList().add(Projections.property("id"), "id")
						.add(Projections.property(TITLE), TITLE).add(Projections.property("link"), "link")
						.add(Projections.property(DESCRIPTION), DESCRIPTION).add(Projections.property("slug"), "slug")
						.add(Projections.property(MODULEID), "moduleId")
						.add(Projections.property("subModule.id"), SUBMODULE_ID)
						.add(Projections.property(MERGETAG_ID), "mergeTagId")
						.add(Projections.property("createdUserId"), "createdBy")
						.add(Projections.property("updatedUserId"), "updatedBy")
						.add(Projections.property("createdTime"), "createdDate")
						.add(Projections.property("updatedTime"), "updatedDate")))
				.setResultTransformer(Transformers.aliasToBean(UserGuideDto.class)).add(Restrictions.eq("slug", slug))
				.uniqueResult();
	}

	@Override
	public Map<String, Object> getUserGuidesByModuleAndSubMOdules(Pagination pagination, List<Object> moduleIds,
			List<Object> subModuleIds, List<Object> guideTitles) {
		Session session = sessionFactory.getCurrentSession();
		Map<String, Object> resultMap = new HashMap<>();
		String searchKey = pagination.getSearchKey().toLowerCase();
		Integer companyId = getCompanyIdFromPagination(pagination);
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append(" SELECT DISTINCT u.title AS \"title\", u.id AS \"id\", ")
				.append(" c.custom_name AS \"customName\", u.description AS \"description\", ")
				.append(" u.sub_module_id AS \"subModuleId\", u.created_time AS \"createdDate\" ")
				.append(" FROM xt_module_custom c ").append(" JOIN xt_module m ON c.module_id = m.id ")
				.append(" LEFT JOIN xt_user_guide u ON u.module_id = m.id ").append(" WHERE c.company_id = :companyId ")
				.append(" AND u.module_id IN (:moduleIds) ");
		if (!guideTitles.isEmpty()) {
			queryBuilder.append(" AND u.title IN (:titles) ");
		}
		if (subModuleIds != null && !subModuleIds.isEmpty()) {
			if (subModuleIds.contains(0)) {
				queryBuilder.append(" AND (u.sub_module_id IN (:subModuleIds) OR u.sub_module_id IS NULL) ");
			} else {
				queryBuilder.append(" AND u.sub_module_id IN (:subModuleIds) ");
			}
		}
		boolean hasSearchKey = StringUtils.hasText(pagination.getSearchKey());
		if (hasSearchKey) {
			if (pagination.isSearchWithModuleName()) {
				queryBuilder.append(" AND LOWER(c.custom_name) = LOWER(:moduleName) ");
			} else {
				queryBuilder.append(" AND (").append(" LOWER(u.slug) LIKE :searchPattern ")
						.append(" OR LOWER(u.title) LIKE :searchPattern ")
						.append(" OR LOWER(u.description) LIKE :searchPattern ")
						.append(" OR LOWER(c.custom_name) LIKE :searchPattern ").append(") ");
			}
		}
		queryBuilder.append(" ORDER BY u.sub_module_id, u.created_time ");
		SQLQuery query = session.createSQLQuery(queryBuilder.toString());
		query.setParameter("companyId", companyId);
		query.setParameterList("moduleIds", moduleIds);
		if (!guideTitles.isEmpty()) {
			query.setParameterList("titles", guideTitles);
		}
		if (subModuleIds != null && !subModuleIds.isEmpty()) {
			query.setParameterList("subModuleIds", subModuleIds);
		}
		if (hasSearchKey) {
			if (pagination.isSearchWithModuleName() && XamplifyUtils.isValidString(searchKey)) {
				query.setParameter("moduleName", searchKey);
			} else {
				query.setParameter("searchPattern", "%" + searchKey + "%");
			}
		}
		return paginationUtil.setScrollableAndGetList(pagination, resultMap, query, UserGuideDto.class);
	}

	@Override
	public Integer getSubModuleIdsWithName(String subModuleName) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select id from xt_sub_module where  LOWER(name) like LOWER('%" + subModuleName + "%')";
		return (Integer) session.createSQLQuery(queryString).uniqueResult();
	}

	private Integer getCompanyIdFromPagination(Pagination pagination) {
		return pagination.isVanityUrlFilter()
				? userDao.getCompanyIdByProfileName(utilDao.getPrmCompanyProfileName())
				: userDao.getCompanyIdByUserId(pagination.getUserId());
	}

	@Override
	public String getGuideTitleByMergeTagName(String tagName) {
		Session session = sessionFactory.getCurrentSession();
		Integer mergeTagId = getTagIdByName(tagName);
		String queryString = "select title from xt_user_guide where merge_tag_id = :tagId";
		SQLQuery query = session.createSQLQuery(queryString);
		return (String) query.setParameter("tagId", mergeTagId).uniqueResult();

	}

	@Override
	public String getModuleNameByModuleId(Integer moduleId) {
		return moduleDao.getModuleNameById(moduleId);
	}
}
