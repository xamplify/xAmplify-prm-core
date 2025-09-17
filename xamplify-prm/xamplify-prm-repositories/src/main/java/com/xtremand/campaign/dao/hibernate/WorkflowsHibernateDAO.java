package com.xtremand.campaign.dao.hibernate;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.xtremand.campaign.dao.WorkflowsDao;
import com.xtremand.campaign.dto.WorkFlowDTO;
import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.common.bom.Pagination;
import com.xtremand.util.PaginationUtil;
import com.xtremand.util.XamplifyUtil;

@Repository
public class WorkflowsHibernateDAO implements WorkflowsDao {

	@Value("${workflowQuery}")
	private String workflowQuery;

	@Value("${workflowSearchQuery}")
	private String workflowSearchQuery;

	@Value("${workflowGroupByQuery}")
	private String workflowGroupByQuery;

	@Value("${replyTimeSortQuery}")
	private String replyTimeSortQuery;

	@Value("${replyInDaysSortQuery}")
	private String replyInDaysSortQuery;

	@Value("${campaignNameSortQuery}")
	private String campaignNameSortQuery;

	@Value("${emailsSentSortQuery}")
	private String emailsSentSortQuery;

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private PaginationUtil paginationUtil;

	@Autowired
	private XamplifyUtil xamplifyUtil;

	@Override
	public Map<String, Object> findEmailNotOpenedWorkflowDetails(Pagination pagination) {
		try {
			HashMap<String, Object> map = new HashMap<>();
			String finalQueryString = "";
			String sortQueryString = getSelectedSortOptionForWorkflowDetails(pagination);
			String searchKey = pagination.getSearchKey();
			boolean hasSearchKey = StringUtils.hasText(searchKey);
			if (hasSearchKey) {
				finalQueryString = workflowQuery + " " + workflowSearchQuery.replace("searchKey", searchKey) + " "
						+ workflowGroupByQuery + " " + sortQueryString;
			} else {
				finalQueryString = workflowQuery + " " + workflowGroupByQuery + " " + sortQueryString;
			}
			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(finalQueryString);
			query.setParameter("actionId", pagination.getCategoryId());
			String[] filterArray = pagination.getFilterKey().split("-");
			Integer monthFilter = Integer.valueOf(filterArray[0].trim());
			Integer yearFilter = Integer.valueOf(filterArray[1].trim());
			query.setParameter("month", monthFilter);
			query.setParameter("year", yearFilter);
			query.setParameter("replyTime", new Date());
			return paginationUtil.setScrollableAndGetList(pagination, map, query, WorkFlowDTO.class);
		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex);
		}

	}

	private String getSelectedSortOptionForWorkflowDetails(Pagination pagination) {
		String sortOptionQueryString = "";
		if (StringUtils.hasText(pagination.getSortcolumn())) {
			if ("replyTime".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += replyTimeSortQuery + pagination.getSortingOrder();
			} else if ("replyInDays".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += replyInDaysSortQuery + pagination.getSortingOrder();
			} else if ("campaignName".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += campaignNameSortQuery + pagination.getSortingOrder();
				sortOptionQueryString = xamplifyUtil.setNullConditionsForAscOrDesc(pagination, sortOptionQueryString);
			} else if ("emailsSent".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += emailsSentSortQuery + pagination.getSortingOrder();
			}
		} else {
			sortOptionQueryString += replyTimeSortQuery + " desc";
		}
		return sortOptionQueryString;
	}

}
