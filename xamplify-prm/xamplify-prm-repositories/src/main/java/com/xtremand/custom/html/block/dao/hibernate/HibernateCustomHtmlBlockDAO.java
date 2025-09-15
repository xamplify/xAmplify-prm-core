package com.xtremand.custom.html.block.dao.hibernate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.common.bom.Pagination;
import com.xtremand.custom.html.block.bom.CustomHtmlBlock;
import com.xtremand.custom.html.block.dao.CustomHtmlblockDAO;
import com.xtremand.custom.html.block.dto.CustomHtmlBlockDTO;
import com.xtremand.util.PaginationUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.HibernateSQLQueryResultUtilDao;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.QueryParameterDTO;
import com.xtremand.util.dto.SortColumnDTO;
import com.xtremand.util.dto.XamplifyConstants;

@Repository
@Transactional
public class HibernateCustomHtmlBlockDAO implements CustomHtmlblockDAO {

	@Autowired
	private HibernateSQLQueryResultUtilDao hibernateSQLQueryResultUtilDao;

	@Autowired
	private PaginationUtil paginationUtil;

	@Autowired
	private SessionFactory sessionFactory;

	private static final String ID = "id";

	private static final String TITLE = "title";

	private static final String HTML_BODY = "htmlBody";

	private static final String LEFT_HTML_BODY = "leftHtmlBody";
	
	private static final String RIGHT_HTML_BODY = "rightHtmlBody";

	private static final String SELECTED = "selected";

	private static final String LAYOUT_SIZE = "layoutSize";

	private static final String TITLEVISIBLE = "titleVisible";

	@Override
	public Map<String, Object> findPaginatedCustomHtmls(Pagination pagination) {
		String queryString = "select xchb.id as \"id\", xchb.title as \"title\", \n"
				+ "case when length(TRIM(concat(xup.firstname,'',xup.lastname)))>0 \n"
				+ "then TRIM(concat(xup.firstname,' ',xup.lastname)) else xup.email_id end as \"createdBy\" , \n"
				+ "xchb.created_time as \"createdTime\", xchb.is_selected as \"isSelected\" \n "
				+ "from xt_custom_html_block xchb, xt_user_profile xup \n"
				+ "where xup.user_id = xchb.created_user_id and xchb.company_id = :companyId";
		String sortQueryString = addSortColumns(pagination);
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		QueryParameterDTO queryParameterDTO = new QueryParameterDTO(XamplifyConstants.COMPANY_ID,
				pagination.getCompanyId());
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(queryParameterDTO);
		hibernateSQLQueryResultRequestDTO.setSortQueryString(sortQueryString);
		hibernateSQLQueryResultRequestDTO.setClassInstance(CustomHtmlBlockDTO.class);
		List<String> searchColumns = new ArrayList<>();
		searchColumns.add("xchb.title");
		hibernateSQLQueryResultRequestDTO.setSearchColumns(searchColumns);
		return hibernateSQLQueryResultUtilDao.returnPaginatedDTOList(hibernateSQLQueryResultRequestDTO, pagination,
				pagination.getSearchKey());
	}

	private String addSortColumns(Pagination pagination) {
		List<SortColumnDTO> sortColumnDTOs = new ArrayList<>();
		SortColumnDTO idSortOption = new SortColumnDTO(ID, "xchb.id", true, false, false);
		SortColumnDTO createdTimeSortOption = new SortColumnDTO(XamplifyConstants.CREATED_TIME, "xchb.created_time",
				false, true, false);
		SortColumnDTO titleSortOption = new SortColumnDTO("name", "xchb.title", false, true, false);
		sortColumnDTOs.add(createdTimeSortOption);
		sortColumnDTOs.add(titleSortOption);
		sortColumnDTOs.add(idSortOption);
		return paginationUtil.generateSortQuery(pagination, sortColumnDTOs, "desc");
	}

	@Override
	public boolean isTitleExist(String title, Integer companyId, Integer customHtmlBlockId) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "SELECT COUNT(*) FROM xt_custom_html_block WHERE TRIM(LOWER(title)) = TRIM(LOWER(:title))"
				+ " AND company_id = :companyId";
		if (XamplifyUtils.isValidInteger(customHtmlBlockId)) {
			queryString += " AND id != :id";
		}
		Query query = session.createSQLQuery(queryString);
		query.setParameter(TITLE, title);
		query.setParameter(XamplifyConstants.COMPANY_ID, companyId);
		if (XamplifyUtils.isValidInteger(customHtmlBlockId)) {
			query.setParameter(ID, customHtmlBlockId);
		}
		Long count = ((Number) query.uniqueResult()).longValue();
		return count > 0;
	}

	@Override
	public CustomHtmlBlock findById(Integer id, Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(CustomHtmlBlock.class);
		criteria.add(Restrictions.eq(ID, id));
		criteria.setProjection(Projections.projectionList().add(Projections.property(ID), ID)
				.add(Projections.property(TITLE), TITLE)
				.add(Projections.property(HTML_BODY), HTML_BODY)
				.add(Projections.property(SELECTED), SELECTED)
				.add(Projections.property(LAYOUT_SIZE), LAYOUT_SIZE)
				.add(Projections.property(LEFT_HTML_BODY), LEFT_HTML_BODY)
				.add(Projections.property(RIGHT_HTML_BODY), RIGHT_HTML_BODY)
				.add(Projections.property(TITLEVISIBLE), TITLEVISIBLE));
		criteria.setResultTransformer(Transformers.aliasToBean(CustomHtmlBlock.class));
		return (CustomHtmlBlock) criteria.uniqueResult();
	}

	@Override
	public void delete(Integer id, Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String hqlString = "delete from CustomHtmlBlock where id = :id and company_id = :companyId";
		Query query = session.createQuery(hqlString);
		query.setParameter(ID, id);
		query.setParameter(XamplifyConstants.COMPANY_ID, companyId);
		query.executeUpdate();
	}

}
