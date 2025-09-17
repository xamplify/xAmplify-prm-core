package com.xtremand.upgrade.dao.hibernate;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.common.bom.Pagination;
import com.xtremand.team.member.group.exception.TeamMemberGroupDataAccessException;
import com.xtremand.unsubscribe.exception.UnsubscribeDataAccessException;
import com.xtremand.upgrade.bom.UpgradeRoleRequestStatus;
import com.xtremand.upgrade.dao.UpgradeRoleDao;
import com.xtremand.upgrade.dto.UpgradeRoleDTO;
import com.xtremand.upgrade.dto.UpgradeRoleGetDTO;
import com.xtremand.util.PaginationUtil;

@Repository
@Transactional
public class HibernateUpgradeRoleDao implements UpgradeRoleDao {

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private PaginationUtil paginationUtil;

	@Override
	public boolean isRequestExists(Integer companyId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String queryString = "select case when count(*)>0 then true else false end  from xt_upgrade_role where requested_company_id=:companyId";
			Query query = session.createSQLQuery(queryString);
			query.setParameter("companyId", companyId);
			return query.uniqueResult() != null ? (boolean) query.uniqueResult() : false;
		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception e) {
			throw new XamplifyDataAccessException(e);
		}
	}

	@Override
	public Map<String, Object> findAll(Pagination pagination) {
		try {
			HashMap<String, Object> map = new HashMap<>();
			String finalQueryString = "";
			String requestType = setRequestType(pagination);
			String queryString = " select ur.id as \"id\",cp.company_id as \"companyId\",cp.company_name as \"companyName\",cp.company_logo as \"companyLogo\",up.email_id as \"emailId\", "
					+ " coalesce(up.firstname,'') as \"firstName\",coalesce(up.lastname,'') as \"lastName\",TRIM(concat(TRIM(up.firstname), ' ', TRIM(up.lastname), ' ',TRIM(up.middle_name))) as \"fullName\","
					+ " ur.created_time as \"createdTime\", ur.updated_time as \"updatedTime\",cast(ur.request_status as text) as \"status\" from xt_upgrade_role ur,xt_company_profile cp,xt_user_profile up "
					+ " where cp.company_id = ur.requested_company_id	and up.user_id = ur.created_by and cast(ur.request_status as text) = :requestType";
			String searchQueryString = " and ( LOWER(cp.company_name) like LOWER('%searchKey%')   OR  LOWER(up.email_id) like LOWER('%searchKey%')"
					+ " OR LOWER(up.firstname) like LOWER('%searchKey%') OR LOWER(up.lastname) like LOWER('%searchKey%')   )";
			String searchKey = pagination.getSearchKey();
			boolean hasSearchKey = StringUtils.hasText(searchKey);
			if (hasSearchKey) {
				finalQueryString = queryString + " " + searchQueryString.replace("searchKey", searchKey);
			} else {
				finalQueryString = queryString;
			}
			String querySuffix = addOrderByQuery(requestType);
			finalQueryString+=querySuffix;
			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(finalQueryString);
			query.setParameter("requestType", requestType);
			return paginationUtil.setScrollableAndGetList(pagination, map, query, UpgradeRoleDTO.class);
		} catch (HibernateException | XamplifyDataAccessException u) {
			throw new UnsubscribeDataAccessException(u);
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex);
		}

	}

	private String addOrderByQuery(String requestType) {
		String querySuffix = "";
		if (requestType.equals(UpgradeRoleRequestStatus.APPROVED.name())) {
			querySuffix = " order by ur.updated_time desc";
		} else if (requestType.equals(UpgradeRoleRequestStatus.APPROVED.name())) {
			querySuffix = " order by ur.created_time desc";
		}
		return querySuffix;
	}

	private String setRequestType(Pagination pagination) {
		String requestType = UpgradeRoleRequestStatus.REQUESTED.name();
		if (UpgradeRoleRequestStatus.REQUESTED.name().toLowerCase().equals(pagination.getFilterKey())) {
			requestType = UpgradeRoleRequestStatus.REQUESTED.name();
		} else if (UpgradeRoleRequestStatus.REJECTED.name().toLowerCase().equals(pagination.getFilterKey())) {
			requestType = UpgradeRoleRequestStatus.REJECTED.name();
		} else if (UpgradeRoleRequestStatus.APPROVED.name().toLowerCase().equals(pagination.getFilterKey())) {
			requestType = UpgradeRoleRequestStatus.APPROVED.name();
		}
		return requestType;
	}

	@Override
	public boolean isRequestApproved(Integer requestId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String queryString = "select case when cast(request_status as text) = :requestStatus then true else false end  from xt_upgrade_role where id=:id";
			Query query = session.createSQLQuery(queryString);
			query.setParameter("id", requestId);
			query.setParameter("requestStatus", UpgradeRoleRequestStatus.APPROVED.name());
			return query.uniqueResult() != null ? (boolean) query.uniqueResult() : false;
		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception e) {
			throw new XamplifyDataAccessException(e);
		}
	}

	@Override
	public UpgradeRoleGetDTO findCompanyIdAndCreatedBy(Integer requestId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String queryString = "select requested_company_id as  \"companyId\",created_by as \"userId\" from xt_upgrade_role where id=:id";
			Query query = session.createSQLQuery(queryString);
			query.setParameter("id", requestId);
			return (UpgradeRoleGetDTO) query.setResultTransformer(Transformers.aliasToBean(UpgradeRoleGetDTO.class))
					.uniqueResult();
		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception e) {
			throw new XamplifyDataAccessException(e);
		}
	}

	@Override
	public void approveRequest(Integer requestId) {
		try {
			UpgradeRoleRequestStatus approvedStatus = UpgradeRoleRequestStatus.APPROVED;
			changeRequestByRequestId(requestId, approvedStatus);
		} catch (HibernateException | TeamMemberGroupDataAccessException e) {
			throw new TeamMemberGroupDataAccessException(e);
		} catch (Exception ex) {
			throw new TeamMemberGroupDataAccessException(ex);
		}

	}

	@Override
	public void rejectRequest(Integer requestId) {
		try {
			UpgradeRoleRequestStatus rejectedStatus = UpgradeRoleRequestStatus.REJECTED;
			changeRequestByRequestId(requestId, rejectedStatus);
		} catch (HibernateException | TeamMemberGroupDataAccessException e) {
			throw new TeamMemberGroupDataAccessException(e);
		} catch (Exception ex) {
			throw new TeamMemberGroupDataAccessException(ex);
		}

	}

	private void changeRequestByRequestId(Integer requestId, UpgradeRoleRequestStatus approvedStatus) {
		Session session = sessionFactory.getCurrentSession();
		String changeRequestQueryString = "UPDATE UpgradeRole set upgradeRoleRequestStatus=:requestStatus,updatedBy = :updatedBy,updatedTime = :updatedTime where id=:id";
		Query changeRequestHQLQuery = session.createQuery(changeRequestQueryString);
		changeRequestHQLQuery.setParameter("id", requestId);
		changeRequestHQLQuery.setParameter("requestStatus", approvedStatus);
		changeRequestHQLQuery.setParameter("updatedBy", 1);
		changeRequestHQLQuery.setParameter("updatedTime", new Date());
		changeRequestHQLQuery.executeUpdate();
	}


}
