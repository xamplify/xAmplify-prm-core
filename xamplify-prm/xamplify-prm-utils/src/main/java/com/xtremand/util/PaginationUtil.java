package com.xtremand.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hibernate.Criteria;
import org.hibernate.NullPrecedence;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.xtremand.common.bom.Pagination;
import com.xtremand.common.bom.Pagination.SORTINGORDER;
import com.xtremand.util.dto.SortColumnDTO;
import com.xtremand.util.dto.XamplifyConstants;

@Component
public class PaginationUtil {

	private static final String TOTAL_RECORDS = XamplifyConstants.TOTAL_RECORDS;

	public Map<String, Object> addSearchAndPagination(Pagination pagination, org.hibernate.Criteria criteria,
			List<Criterion> criterions, String columnName) {
		for (Criterion criterion : criterions) {
			criteria.add(criterion);
		}

		/********************* Search Functionality **********************************/
		if (StringUtils.hasText(pagination.getSearchKey())) {
			Criterion name = Restrictions.like(columnName, pagination.getSearchKey(), MatchMode.ANYWHERE).ignoreCase();
			criteria.add(name);
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
				criteria.addOrder(Order.asc(sortcolumnObj.get()).nulls(NullPrecedence.FIRST));
			} else if (SORTINGORDER.DESC == SORTINGORDER.valueOf(paginationObj.get().getSortingOrder())) {
				criteria.addOrder(Order.desc(sortcolumnObj.get()).nulls(NullPrecedence.LAST));
			}
		} else {
			criteria.addOrder(Order.desc("id"));
		}
		List<?> items = criteria.list();
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put(XamplifyConstants.TOTAL_RECORDS, totalRecords);
		resultMap.put("list", items);
		return resultMap;
	}

	public Map<String, Object> addSearchAndSortPaginationCriteria(Pagination pagination,
			org.hibernate.Criteria criteria, List<Criterion> criterions, String columnName, String searchKey) {
		for (Criterion criterion : criterions) {
			criteria.add(criterion);
		}

		/********************* Search Functionality **********************************/
		if (StringUtils.hasText(searchKey)) {
			searchKey = XamplifyUtils.escapeSingleQuotesForSearchQuery(searchKey);
			searchKey = XamplifyUtils.addBackSlashToSpecialCharacters(searchKey);
			Criterion name = Restrictions.like(columnName, searchKey, MatchMode.ANYWHERE).ignoreCase();
			criteria.add(name);
		}

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
					criteria.addOrder(Order.asc(sortcolumnObj.get()).nulls(NullPrecedence.FIRST));
				} else if (SORTINGORDER.DESC == SORTINGORDER.valueOf(paginationObj.get().getSortingOrder())) {
					criteria.addOrder(Order.desc(sortcolumnObj.get()).nulls(NullPrecedence.LAST));
				}
			} else {
				criteria.addOrder(Order.asc("defaultLandingPage"));
				criteria.addOrder(Order.desc("id"));
			}
		}
		List<?> items = criteria.list();
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put(TOTAL_RECORDS, totalRecords);
		resultMap.put("list", items);
		return resultMap;
	}

	public Map<String, Object> addSearchAndPaginationAndSort(Pagination pagination, org.hibernate.Criteria criteria,
			List<Criterion> criterions, List<String> columnNames, String defaultSortColumn, String ascOrDesc) {
		if (criterions != null) {
			for (Criterion criterion : criterions) {
				criteria.add(criterion);
			}
		}
		/********************* Search Functionality **********************************/
		if (!pagination.isIgnoreSearch() && StringUtils.hasText(pagination.getSearchKey())) {
			Disjunction disjunction = Restrictions.disjunction();
			for (String column : columnNames) {
				Criterion columnCriteria = Restrictions
						.like(column, "%" + pagination.getSearchKey() + "%", MatchMode.ANYWHERE).ignoreCase();
				disjunction.add(columnCriteria);
			}
			criteria.add(disjunction);
		}
		ScrollableResults scrollableResults = criteria.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		scrollableResults.close();

		Optional<Pagination> paginationObj = Optional.ofNullable(pagination);
		Optional<Integer> maxResultsObj = Optional.ofNullable(paginationObj.get().getMaxResults());
		Optional<Integer> pageIndexObj = Optional.ofNullable(paginationObj.get().getPageIndex());
		Optional<String> sortcolumnObj = Optional.ofNullable(paginationObj.get().getSortcolumn());
		if (!pagination.isExcludeLimit() && (maxResultsObj.isPresent() && pageIndexObj.isPresent())) {
			criteria.setFirstResult((pageIndexObj.get() * maxResultsObj.get()) - maxResultsObj.get());
			criteria.setMaxResults(maxResultsObj.get());
		}
		addSortOrders(criteria, defaultSortColumn, ascOrDesc, paginationObj, sortcolumnObj);
		List<?> list = criteria.list();
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put(TOTAL_RECORDS, totalRecords);
		resultMap.put(XamplifyConstants.LIST, list);
		return resultMap;
	}

	private void addSortOrders(org.hibernate.Criteria criteria, String defaultSortColumn, String ascOrDesc,
			Optional<Pagination> paginationObj, Optional<String> sortcolumnObj) {
		if (sortcolumnObj.isPresent() && paginationObj.isPresent()) {
			if (SORTINGORDER.ASC == SORTINGORDER.valueOf(paginationObj.get().getSortingOrder())) {
				criteria.addOrder(Order.asc(sortcolumnObj.get()));
			} else if (SORTINGORDER.DESC == SORTINGORDER.valueOf(paginationObj.get().getSortingOrder())) {
				criteria.addOrder(Order.desc(sortcolumnObj.get()).nulls(NullPrecedence.LAST));
			}
		} else {
			if ("asc".equals(ascOrDesc)) {
				criteria.addOrder(Order.asc(defaultSortColumn));
			} else {
				criteria.addOrder(Order.desc(defaultSortColumn));
			}

		}
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> listItemsWithPaginationAndSearchByQuery(Pagination pagination, Session session,
			String sortQueryString) {
		String finalQueryString = "";
		Map<String, Object> map = new HashMap<>();
		String queryStringPrefix = " with campaign_list as "
				+ "( select c.user_id,c.campaign_id,max(c.firstname) as firstname,max(c.lastname) "
				+ " as lastname,up.email_id from xt_campaign_user_userlist c  join "
				+ " xt_user_profile up on c.user_id = up.user_id where c.campaign_id = :campaignId ";
		String queryStringSuffix = " group by c.user_id,c.campaign_id,up.email_id), "
				+ " campaign_status as (select * from public.xt_campaign_user_work_flow_status) "
				+ " select campaign_list.*,coalesce(campaign_status.work_flow_status,'ACTIVE') "
				+ " from campaign_list left join campaign_status on "
				+ " campaign_list.campaign_id = campaign_status.campaign_id"
				+ "  and campaign_list.user_id = campaign_status.user_id  ";

		if (StringUtils.hasText(pagination.getSearchKey())) {
			String searchKey = "'%" + pagination.getSearchKey() + "%'";
			String searchQueryString = " and  (LOWER(c.firstname) like LOWER(" + searchKey
					+ ")   or LOWER(c.lastname) like LOWER(" + searchKey + ")  or LOWER(up.email_id) like  LOWER("
					+ searchKey + "))";
			finalQueryString = queryStringPrefix + searchQueryString + queryStringSuffix;
		} else {
			finalQueryString = queryStringPrefix + queryStringSuffix + sortQueryString;
		}
		SQLQuery query = session.createSQLQuery(finalQueryString);
		query.setParameter("campaignId", pagination.getCampaignId());
		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		if (pagination.getPageIndex() == null) {
			pagination.setPageIndex(1);
		}
		if (pagination.getMaxResults() == null) {
			pagination.setMaxResults(12);
		}
		query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		query.setMaxResults(pagination.getMaxResults());
		List<Object[]> list = query.list();
		map.put(TOTAL_RECORDS, totalRecords);
		map.put("list", list);
		return map;
	}

	public String setNullConditionsForAscOrDesc(Pagination pagination, String sortOptionQueryString) {
		if (SORTINGORDER.ASC.name().equals(pagination.getSortingOrder())) {
			sortOptionQueryString += " nulls first";
		} else {
			sortOptionQueryString += " nulls last";
		}
		return sortOptionQueryString;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> setScrollableAndGetList(Pagination pagination, Map<String, Object> map, SQLQuery query,
			Class<?> clazz) {
		Integer totalRecords = setScrollableResults(pagination, query);
		List<Class<?>> list = query.setResultTransformer(Transformers.aliasToBean(clazz)).list();
		map.put(TOTAL_RECORDS, totalRecords);
		map.put("list", list);
		return map;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> addTotalRecordsAndListToMap(Pagination pagination, Map<String, Object> map,
			SQLQuery query, Class<?> clazz, Integer totalRecords) {
		setFirstAndMaxResults(pagination, query, totalRecords);
		List<Class<?>> list = query.setResultTransformer(Transformers.aliasToBean(clazz)).list();
		map.put(TOTAL_RECORDS, totalRecords);
		map.put("list", list);
		return map;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> setScrollableAndGetListByAddEntityOption(Pagination pagination, Map<String, Object> map,
			SQLQuery query) {
		Integer totalRecords = setScrollableResults(pagination, query);
		List<Class<?>> list = query.list();
		map.put(TOTAL_RECORDS, totalRecords);
		map.put("list", list);
		return map;
	}

	private Integer setScrollableResults(Pagination pagination, SQLQuery query) {
		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;

		setFirstAndMaxResults(pagination, query, totalRecords);

		return totalRecords;
	}

	private void setFirstAndMaxResults(Pagination pagination, SQLQuery query, Integer totalRecords) {
		if (!pagination.isExcludeLimit()) {
			Integer pageIndex = pagination.getPageIndex() != null ? pagination.getPageIndex() : 1;
			Integer limit = pagination.getMaxResults() != null ? pagination.getMaxResults() : 12;
			if (limit > 48 && limit > totalRecords) {
				limit = totalRecords - 1;
			}
			query.setFirstResult((pageIndex - 1) * limit);
			query.setMaxResults(limit);
		}
	}

	public String generateSortQuery(Pagination pagination, List<SortColumnDTO> sortColumnDTOs, String defaultSortType) {
		StringBuilder sortQuery = new StringBuilder("order by ");
		if (StringUtils.hasText(pagination.getSortcolumn())) {
			for (SortColumnDTO sortColumnDTO : sortColumnDTOs) {
				String sortColumn = sortColumnDTO.getColumnName();
				String dbColumn = sortColumnDTO.getDatabaseColumnName();
				if (sortColumn.equals(pagination.getSortcolumn())) {
					sortQuery.append(dbColumn + " " + pagination.getSortingOrder())
							.append(addNullParameter(pagination.getSortingOrder()));
				}
			}
		} else {
			sortQuery
					.append(sortColumnDTOs.stream().filter(SortColumnDTO::isDefaultSortColumn)
							.collect(Collectors.toList()).get(0).getDatabaseColumnName() + " " + defaultSortType)
					.append(addNullParameter(defaultSortType));
			;
		}
		return String.valueOf(sortQuery);
	}

	private String addNullParameter(String sortingOder) {
		if (SORTINGORDER.ASC.name().equals(sortingOder)) {
			return " nulls first";
		} else {
			return " nulls last";
		}
	}

	/****** XNFR-85 ***********/
	public Map<String, Object> returnEmptyList(Map<String, Object> map, List<?> arrayList) {
		map.put(TOTAL_RECORDS, 0);
		map.put("list", arrayList);
		return map;
	}

	/******** XNFR-125 ***********/
	public List<?> getListDTO(Class<?> clazz, Query query) {
		return query.setResultTransformer(Transformers.aliasToBean(clazz)).list();
	}

	public Object getDto(Class<?> clazz, Query query) {
		return query.setResultTransformer(Transformers.aliasToBean(clazz)).uniqueResult();
	}

	/**** XNFR-326 ****/
	public Object getDtoByCriteria(Criteria criteria, Class<?> clazz) {
		return criteria.setResultTransformer(Transformers.aliasToBean(clazz)).uniqueResult();
	}

}
