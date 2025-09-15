package com.xtremand.tag.dao.hibernate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.xtremand.common.bom.Pagination;
import com.xtremand.tag.bom.Tag;
import com.xtremand.tag.dao.TagDao;
import com.xtremand.tag.dto.TagDTO;
import com.xtremand.tag.exception.TagDataAccessException;
import com.xtremand.util.DateUtils;
import com.xtremand.util.XamplifyUtils;

@Repository
@Transactional
public class HibernateTagDao implements TagDao {

	private static final String TAG_NAME = "tagName";

	private static final String SEARCH_KEY = "searchKey";

	private static final String DESC = " desc";

	private static final String CREATED_TIME = "createdTime";

	private static final String UPDATED_TIME = "updatedTime";

	@Autowired
	private SessionFactory sessionFactory;

	@Value("${tagCreatedTimeOrderByQuery}")
	private String createdTimeOrderByQuery;

	@Value("${tagNameOrderByQuery}")
	private String tagNameOrderByQuery;

	@Value("${tagUpdatedTimeOrderByQuery}")
	private String updatedTimeOrderByQuery;

	@Value("${listTagsViewQuery}")
	private String listTagsViewQuery;

	@Value("${listTagsSearchQuery}")
	private String listTagsSearchQuery;

	@Value("${listTagsSearchByTagNameQuery}")
	private String listTagsSearchByTagNameQuery;

	@Value("${totalRecords}")
	private String totalRecordsStringKey;

	@Override
	public void save(Object clazz) {
		try {
			sessionFactory.getCurrentSession().save(clazz);
		} catch (HibernateException | TagDataAccessException e) {
			throw new TagDataAccessException(e);
		} catch (Exception ex) {
			throw new TagDataAccessException(ex);
		}
	}

	@Override
	public void update(Tag tag) {
		sessionFactory.getCurrentSession().update(tag);
	}

	@Override
	public Tag getById(Integer id) {
		try {
			Session session = sessionFactory.getCurrentSession();
			org.hibernate.Criteria criteria = session.createCriteria(Tag.class);
			criteria.add(Restrictions.eq("id", id));
			return (Tag) criteria.uniqueResult();
		} catch (HibernateException | TagDataAccessException e) {
			throw new TagDataAccessException(e);
		} catch (Exception ex) {
			throw new TagDataAccessException(ex);
		}
	}

	@Override
	public void delete(List<Integer> ids) {
		try {
			if (XamplifyUtils.isNotEmptyList(ids)) {
				Session session = sessionFactory.getCurrentSession();
				String hql = "delete from Tag  where id in (:ids)";
				Query query = session.createQuery(hql);
				query.setParameterList("ids", ids);
				query.executeUpdate();
			}

		} catch (HibernateException | TagDataAccessException e) {
			throw new TagDataAccessException(e);
		} catch (Exception ex) {
			throw new TagDataAccessException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getAllByCompanyId(Integer id, Pagination pagination, String searchBy) {

		HashMap<String, Object> map = new HashMap<>();
		String finalQueryString = "";
		String sortQueryString = getSelectedSortOptionFortags(pagination);
		String searchKey = pagination.getSearchKey();
		boolean hasSearchKey = StringUtils.hasText(searchKey);
		if (hasSearchKey) {
			String searchQuery = listTagsSearchQuery;
			if (StringUtils.hasText(searchBy) && "TagName".equalsIgnoreCase(searchBy)) {
				searchQuery = listTagsSearchByTagNameQuery;
			}
			finalQueryString = listTagsViewQuery + " " + searchQuery.replace(SEARCH_KEY, searchKey) + " "
					+ sortQueryString;
		} else {
			finalQueryString = listTagsViewQuery + " " + sortQueryString;
		}
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(finalQueryString);
		query.setParameter("companyId", id);
		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		query.setMaxResults(pagination.getMaxResults());
		List<TagDTO> tags = query.setResultTransformer(Transformers.aliasToBean(TagDTO.class)).list();
		List<TagDTO> updatedTags = new ArrayList<>();
		for (TagDTO tag : tags) {
			TagDTO updatedTag = new TagDTO();
			BeanUtils.copyProperties(tag, updatedTag);
			String createdTimeInUTCString = DateUtils.getUtcString(tag.getCreatedTime());
			updatedTag.setCreatedDateInUTCString(createdTimeInUTCString);
			updatedTags.add(updatedTag);
		}
		map.put(totalRecordsStringKey, totalRecords);
		map.put("tags", updatedTags);
		return map;
	}

	private String getSelectedSortOptionFortags(Pagination pagination) {
		String sortOptionQueryString = "";
		if (StringUtils.hasText(pagination.getSortcolumn())) {
			if (TAG_NAME.equals(pagination.getSortcolumn())) {
				sortOptionQueryString += tagNameOrderByQuery + " " + pagination.getSortingOrder();
			} else if (CREATED_TIME.equals(pagination.getSortcolumn())) {
				sortOptionQueryString += createdTimeOrderByQuery + " " + pagination.getSortingOrder();
			} else if (UPDATED_TIME.equals(pagination.getSortcolumn())) {
				sortOptionQueryString += updatedTimeOrderByQuery + " " + pagination.getSortingOrder();
			}
		} else {
			sortOptionQueryString += "order by t.created_time" + DESC;
		}
		return sortOptionQueryString;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getTagIds(Integer companyId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String hql = "select id from TagView where companyId=:companyId";
			Query query = session.createQuery(hql);
			query.setParameter("companyId", companyId);
			return query.list();
		} catch (HibernateException | TagDataAccessException e) {
			throw new TagDataAccessException(e);
		} catch (Exception ex) {
			throw new TagDataAccessException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getTagNames(Integer userId, Integer companyId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String hql = "select lower(trim(tagName)) from TagView  where createdBy=:userId and companyId=:companyId";
			Query query = session.createQuery(hql);
			query.setParameter("userId", userId);
			query.setParameter("companyId", companyId);
			return query.list();
		} catch (HibernateException | TagDataAccessException e) {
			throw new TagDataAccessException(e);
		} catch (Exception ex) {
			throw new TagDataAccessException(ex);
		}
	}

	@Override
	public Tag getByIdAndCompanyId(Integer tagId, Integer companyId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			org.hibernate.Criteria criteria = session.createCriteria(Tag.class);
			criteria.add(Restrictions.eq("id", tagId));
			criteria.add(Restrictions.eq("companyProfile.id", companyId));
			return (Tag) criteria.uniqueResult();
		} catch (HibernateException | TagDataAccessException e) {
			throw new TagDataAccessException(e);
		} catch (Exception ex) {
			throw new TagDataAccessException(ex);
		}
	}

}
