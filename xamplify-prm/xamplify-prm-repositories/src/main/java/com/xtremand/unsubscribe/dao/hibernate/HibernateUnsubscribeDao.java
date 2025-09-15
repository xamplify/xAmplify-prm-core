package com.xtremand.unsubscribe.dao.hibernate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.xtremand.common.bom.Pagination;
import com.xtremand.unsubscribe.bom.UnsubscribePageDetails;
import com.xtremand.unsubscribe.bom.UnsubscribeReason;
import com.xtremand.unsubscribe.dao.UnsubscribeDao;
import com.xtremand.unsubscribe.dto.UnsubscribeReasonDTO;
import com.xtremand.unsubscribe.exception.UnsubscribeDataAccessException;
import com.xtremand.util.PaginationUtil;
import com.xtremand.util.dto.SortColumnDTO;

@Repository
@Transactional
public class HibernateUnsubscribeDao implements UnsubscribeDao {

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private PaginationUtil paginationUtil;

	@Override
	public Map<String, Object> findAll(Pagination pagination) {
		try {
			HashMap<String, Object> map = new HashMap<>();
			String finalQueryString = "";
			String findUnsubscribeReasonsQuery = "select ur.id as \"id\", ur.reason as \"reason\", ur.is_custom_reason as \"customReason\", ur.created_time as \"createdTime\",  "
					+ " case when LENGTH(TRIM(concat(TRIM(u.firstname), ' ', TRIM(u.lastname))))>0  then TRIM(concat(TRIM(u.firstname), ' ', TRIM(u.lastname))) else u.email_id end as \"createdBy\" "
					+ " from  xt_unsubscribe_reasons ur,xt_user_profile u where u.user_id = ur.created_user_id and ur.company_id = (select company_id from xt_user_profile where user_id = :userId)";
			String searchUnsubscribeReasonsQuery = " and ( LOWER(ur.reason) like LOWER('%searchKey%'))";
			String sortQueryString = getSortQuery(pagination);
			String searchKey = pagination.getSearchKey();
			boolean hasSearchKey = StringUtils.hasText(searchKey);
			if (hasSearchKey) {
				finalQueryString = findUnsubscribeReasonsQuery + " "
						+ searchUnsubscribeReasonsQuery.replace("searchKey", searchKey) + " " + sortQueryString;
			} else {
				finalQueryString = findUnsubscribeReasonsQuery + " " + sortQueryString;
			}
			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(finalQueryString);
			query.setParameter("userId", pagination.getUserId());
			return paginationUtil.setScrollableAndGetList(pagination, map, query, UnsubscribeReasonDTO.class);
		} catch (HibernateException | UnsubscribeDataAccessException u) {
			throw new UnsubscribeDataAccessException(u);
		} catch (Exception ex) {
			throw new UnsubscribeDataAccessException(ex);
		}

	}

	private String getSortQuery(Pagination pagination) {
		List<SortColumnDTO> sortColumnDTOs = new ArrayList<>();
		SortColumnDTO sortByName = new SortColumnDTO("reason", "ur.reason", false, true, false);
		SortColumnDTO sortByCreatedTime = new SortColumnDTO("createdTime", "ur.created_time", true, false, false);
		SortColumnDTO sortByUpdatedTime = new SortColumnDTO("updatedTime", "ur.updated_time", false, false, false);
		SortColumnDTO customReason = new SortColumnDTO("customReason", "ur.is_custom_reason", false, false, false);
		sortColumnDTOs.add(sortByName);
		sortColumnDTOs.add(sortByCreatedTime);
		sortColumnDTOs.add(sortByUpdatedTime);
		sortColumnDTOs.add(customReason);
		return paginationUtil.generateSortQuery(pagination, sortColumnDTOs, "desc");
	}

	@Override
	public UnsubscribeReason findById(Integer id) {
		try {
			Session session = sessionFactory.getCurrentSession();
			return (UnsubscribeReason) session.createCriteria(UnsubscribeReason.class).add(Restrictions.eq("id", id))
					.uniqueResult();
		} catch (HibernateException | UnsubscribeDataAccessException e) {
			throw new UnsubscribeDataAccessException(e);
		} catch (Exception ex) {
			throw new UnsubscribeDataAccessException(ex);
		}

	}

	@Override
	public void delete(Integer id) {
		try {
			Session session = sessionFactory.getCurrentSession();
			session.delete(findById(id));
		} catch (HibernateException | UnsubscribeDataAccessException e) {
			throw new UnsubscribeDataAccessException(e);
		} catch (Exception ex) {
			throw new UnsubscribeDataAccessException(ex);
		}

	}

	@Override
	public void saveAll(List<?> list) {
		try {
			Session session = sessionFactory.getCurrentSession();
			for (int i = 0; i < list.size(); i++) {
				session.save(list.get(i));
				if (i % 30 == 0) {
					session.flush();
					session.clear();
				}
			}
		} catch (HibernateException | UnsubscribeDataAccessException e) {
			throw new UnsubscribeDataAccessException(e);
		} catch (Exception ex) {
			throw new UnsubscribeDataAccessException(ex);
		}

	}

	@Override
	public void save(UnsubscribeReason unsubscribeReason) {
		try {
			Session session = sessionFactory.getCurrentSession();
			session.save(unsubscribeReason);
		} catch (HibernateException | UnsubscribeDataAccessException e) {
			throw new UnsubscribeDataAccessException(e);
		} catch (Exception ex) {
			throw new UnsubscribeDataAccessException(ex);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<UnsubscribeReason> findAll(Integer companyId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			Criteria criteria = session.createCriteria(UnsubscribeReason.class)
					.add(Restrictions.eq("companyId", companyId)).addOrder(Order.asc("customReason"));
			return criteria.list();
		} catch (HibernateException | UnsubscribeDataAccessException e) {
			throw new UnsubscribeDataAccessException(e);
		} catch (Exception ex) {
			throw new UnsubscribeDataAccessException(ex);
		}

	}

	@Override
	public UnsubscribePageDetails findUnsubscribePageDetailsByCompanyId(Integer companyId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			return (UnsubscribePageDetails) session.createCriteria(UnsubscribePageDetails.class)
					.add(Restrictions.eq("companyId", companyId)).uniqueResult();
		} catch (HibernateException | UnsubscribeDataAccessException e) {
			throw new UnsubscribeDataAccessException(e);
		} catch (Exception ex) {
			throw new UnsubscribeDataAccessException(ex);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findUnsubscribeUserIdsByCompanyId(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String sqlString = "select distinct user_id from xt_unsubscribed_user where customer_company_id= :companyId and is_customer_enabled=false";
		SQLQuery query = session.createSQLQuery(sqlString);
		query.setParameter("companyId", companyId);
		if (query.list() != null) {
			return query.list();
		} else {
			return Collections.EMPTY_LIST;
		}

	}

}
