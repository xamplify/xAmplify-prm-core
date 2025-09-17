package com.xtremand.util.dao;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.common.bom.Pagination;
import com.xtremand.util.BadRequestException;
import com.xtremand.util.PaginationUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.PaginatedDTO;
import com.xtremand.util.dto.QueryParameterDTO;
import com.xtremand.util.dto.QueryParameterListDTO;
import com.xtremand.util.dto.XamplifyConstants;

@Repository
@Transactional
public class HibernateSQLQueryResultUtilDao {

	private static final Logger logger = LoggerFactory.getLogger(HibernateSQLQueryResultUtilDao.class);

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private PaginationUtil paginationUtil;

	public boolean returnBoolean(HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO) {

		SQLQuery sqlQuery = generateSQLQuery(hibernateSQLQueryResultRequestDTO);

		Object uniqueResult = sqlQuery.uniqueResult();

		return uniqueResult != null && (boolean) uniqueResult;

	}

	private SQLQuery generateSQLQuery(HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO) {
		Session session = sessionFactory.getCurrentSession();

		SQLQuery sqlQuery = session.createSQLQuery(hibernateSQLQueryResultRequestDTO.getQueryString());

		addQueryParameters(hibernateSQLQueryResultRequestDTO, sqlQuery);

		addQueryParameterLists(hibernateSQLQueryResultRequestDTO, sqlQuery);

		setRowCount(hibernateSQLQueryResultRequestDTO, session);

		return sqlQuery;
	}

	private void setRowCount(HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO, Session session) {
		String rowCountQueryString = hibernateSQLQueryResultRequestDTO.getRowCountQueryString();

		if (XamplifyUtils.isValidString(rowCountQueryString)) {
			SQLQuery rowQuery = session.createSQLQuery(rowCountQueryString);

			addQueryParameters(hibernateSQLQueryResultRequestDTO, rowQuery);

			addQueryParameterLists(hibernateSQLQueryResultRequestDTO, rowQuery);

			Integer rowCount = (Integer) rowQuery.uniqueResult();
			hibernateSQLQueryResultRequestDTO.setRowCount(rowCount);

		}
	}

	private void addQueryParameterLists(HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO,
			SQLQuery sqlQuery) {
		List<QueryParameterListDTO> queryParameterListDTOs = hibernateSQLQueryResultRequestDTO
				.getQueryParameterListDTOs();
		if (queryParameterListDTOs != null && !queryParameterListDTOs.isEmpty()) {
			for (QueryParameterListDTO queryParameterListDTO : queryParameterListDTOs) {
				List<?> values = queryParameterListDTO.getValues();
				String key = queryParameterListDTO.getKey();
				if (XamplifyUtils.isNotEmptyList(values)) {
					sqlQuery.setParameterList(key, values);
				} else {
					throwInvalidKeyError(key);
				}

			}
		}
	}

	private void throwInvalidKeyError(String key) {
		throw new BadRequestException(key + " cannot be null.");
	}

	private void addQueryParameters(HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO,
			SQLQuery sqlQuery) {
		List<QueryParameterDTO> queryParameterDTOs = hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs();
		if (queryParameterDTOs != null && !queryParameterDTOs.isEmpty()) {
			for (QueryParameterDTO queryParameterDTO : queryParameterDTOs) {
				String key = queryParameterDTO.getKey();
				Object value = queryParameterDTO.getValue();
				if (value != null) {
					sqlQuery.setParameter(key, value);
				} else {
					throwInvalidKeyError(key);
				}

			}
		}
	}

	public Map<String, Object> returnPaginatedDTOList(
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO, Pagination pagination,
			String searchKey) {
		return returnPaginatedDataMap(hibernateSQLQueryResultRequestDTO, pagination, searchKey);
	}

	private Map<String, Object> returnPaginatedDataMap(
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO, Pagination pagination,
			String searchKey) {
		Map<String, Object> map = new HashMap<>();
		String searchQueryString = getSearchQueryString(hibernateSQLQueryResultRequestDTO);
		String sortQueryString = hibernateSQLQueryResultRequestDTO.getSortQueryString();
		if (!StringUtils.hasText(sortQueryString)) {
			String sortColumn = pagination.getSortcolumn();
			boolean isSortColumnParameterExists = StringUtils.hasText(sortColumn);
			if (isSortColumnParameterExists) {
				sortColumn = "\"" + pagination.getSortcolumn() + "\"";
				sortQueryString = " order by " + sortColumn + " " + pagination.getSortingOrder() + " nulls last";
			}
		}
		StringBuilder queryStringBuilder = new StringBuilder();
		boolean hasSearchKey = searchKey != null && StringUtils.hasText(searchKey);
		queryStringBuilder.append(hibernateSQLQueryResultRequestDTO.getQueryString());
		String groupByQueryString = hibernateSQLQueryResultRequestDTO.getGroupByQueryString();
		String groupByAndSortQueryString = groupByQueryString + " " + sortQueryString;
		if (hasSearchKey) {
			searchKey = XamplifyUtils.escapeSingleQuotesForSearchQuery(searchKey);
			searchKey = XamplifyUtils.addBackSlashToSpecialCharacters(searchKey);
			queryStringBuilder.append(searchQueryString.replace("searchKey", searchKey))
					.append(groupByAndSortQueryString);
		} else {
			queryStringBuilder.append(groupByAndSortQueryString);
		}

		hibernateSQLQueryResultRequestDTO.setQueryString(String.valueOf(queryStringBuilder));

		SQLQuery sqlQuery = generateSQLQuery(hibernateSQLQueryResultRequestDTO);

		if (XamplifyUtils.isValidString(hibernateSQLQueryResultRequestDTO.getRowCountQueryString())) {
			return paginationUtil.addTotalRecordsAndListToMap(pagination, map, sqlQuery,
					hibernateSQLQueryResultRequestDTO.getClassInstance(),
					hibernateSQLQueryResultRequestDTO.getRowCount());
		} else {
			return paginationUtil.setScrollableAndGetList(pagination, map, sqlQuery,
					hibernateSQLQueryResultRequestDTO.getClassInstance());
		}

	}

	public PaginatedDTO returnPaginatedDTO(HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO,
			Pagination pagination, String searchKey) {
		Map<String, Object> map = returnPaginatedDataMap(hibernateSQLQueryResultRequestDTO, pagination, searchKey);
		PaginatedDTO paginatedDTO = new PaginatedDTO();
		List<?> list = (List<?>) map.get(XamplifyConstants.LIST);
		Integer totalRecords = (Integer) map.get(XamplifyConstants.TOTAL_RECORDS);
		paginatedDTO.setTotalRecords(totalRecords);
		paginatedDTO.setList(list);
		return paginatedDTO;
	}

	public PaginatedDTO returnEmptyPaginatedDTO() {
		PaginatedDTO paginatedDTO = new PaginatedDTO();
		paginatedDTO.setTotalRecords(0);
		paginatedDTO.setList(Collections.emptyList());
		return paginatedDTO;
	}

	private String getSearchQueryString(HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO) {
		List<String> searchColumns = hibernateSQLQueryResultRequestDTO.getSearchColumns();
		String searchQueryString = "";
		if (searchColumns != null && !searchColumns.isEmpty()) {
			StringBuilder searchQueryStringBuilder = new StringBuilder(" and (");
			int index = 0;
			int size = searchColumns.size();
			for (String searchColumn : searchColumns) {
				index++;
				String searchText = " LOWER(" + searchColumn + ") like LOWER('%searchKey%')";
				if (index != size) {
					searchQueryStringBuilder.append(searchText + " OR ");
				} else {
					searchQueryStringBuilder.append(searchText + ")");
				}
			}
			searchQueryString = String.valueOf(searchQueryStringBuilder + "\t");
		}
		return searchQueryString;
	}

	public List<?> returnDTOList(HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO) {
		SQLQuery sqlQuery = generateSQLQuery(hibernateSQLQueryResultRequestDTO);
		return paginationUtil.getListDTO(hibernateSQLQueryResultRequestDTO.getClassInstance(), sqlQuery);
	}

	public List<?> returnList(HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO) {
		SQLQuery sqlQuery = generateSQLQuery(hibernateSQLQueryResultRequestDTO);
		List<?> list = sqlQuery.list();
		if (list != null) {
			return list;
		} else {
			return Collections.emptyList();
		}
	}

	public void update(HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO) {
		SQLQuery sqlQuery = generateSQLQuery(hibernateSQLQueryResultRequestDTO);
		sqlQuery.executeUpdate();
	}

	public int updateAndReturnCount(HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO) {
		SQLQuery sqlQuery = generateSQLQuery(hibernateSQLQueryResultRequestDTO);
		return sqlQuery.executeUpdate();
	}

	public Object getUniqueResult(HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO) {
		SQLQuery sqlQuery = generateSQLQuery(hibernateSQLQueryResultRequestDTO);
		return sqlQuery.uniqueResult();
	}

	public Object getDto(HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO, Class<?> clazz) {
		SQLQuery sqlQuery = generateSQLQuery(hibernateSQLQueryResultRequestDTO);
		return sqlQuery.setResultTransformer(Transformers.aliasToBean(clazz)).uniqueResult();
	}

	public Object getListDto(HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO, Class<?> clazz) {
		SQLQuery sqlQuery = generateSQLQuery(hibernateSQLQueryResultRequestDTO);
		return sqlQuery.setResultTransformer(Transformers.aliasToBean(clazz)).list();
	}

	public void saveAll(List<?> objects) {
		Session session = sessionFactory.getCurrentSession();
		int total = objects.size();
		int counter = 0;
		for (int i = 0; i < total; i++) {
			session.save(objects.get(i));
			if (i % 30 == 0) {
				session.flush();
				session.clear();
			}
			counter++;
			int itemsLeft = total - counter;
			String debugMessage = "Companies Left :-" + itemsLeft;
			logger.debug(debugMessage);
		}
	}

	public void save(Object clazz) {
		try {
			Session session = sessionFactory.getCurrentSession();
			session.save(clazz);
		} catch (HibernateException e) {
			throw new XamplifyDataAccessException(e);
		}
	}

	public void updateChunkedIds(String sqlQueryString, List<Integer> ids) {
		Session session = sessionFactory.getCurrentSession();
		List<List<Integer>> chunkedIds = XamplifyUtils.getChunkedList(ids);
		for (List<Integer> splittedIds : chunkedIds) {
			session.createSQLQuery(sqlQueryString).setParameterList("ids", splittedIds).executeUpdate();
		}
	}

}
