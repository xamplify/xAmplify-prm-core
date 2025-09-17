package com.xtremand.flexi.fields.dao.hibernate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
import com.xtremand.flexi.fields.bom.FlexiField;
import com.xtremand.flexi.fields.dao.FlexiFieldDao;
import com.xtremand.flexi.fields.dto.FlexiFieldRequestDTO;
import com.xtremand.flexi.fields.dto.FlexiFieldResponseDTO;
import com.xtremand.lead.dto.PipelineStageDto;
import com.xtremand.util.PaginationUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.HibernateSQLQueryResultUtilDao;
import com.xtremand.util.dao.HibernateUtilDao;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.QueryParameterDTO;
import com.xtremand.util.dto.SortColumnDTO;
import com.xtremand.util.dto.XamplifyConstants;

@Repository
@Transactional
public class HibernateFlexiFieldDao implements FlexiFieldDao {

	private static final String XT_FLEXI_FIELDS = "xt_flexi_fields";
	
	private static final String XT_USER_LIST_FLEXI_FIELD = "xt_user_list_flexi_field_entries";
	
	private static final String XT_USER_USERLIST = "xt_user_userlist";

	private static final String FIELD_NAME = "fieldName";

	private static final String CREATED_TIME = XamplifyConstants.CREATED_TIME;

	private static final String XT_CONTACT_STATUS = "xt_contact_status";

	@Autowired
	private HibernateSQLQueryResultUtilDao hibernateSQLQueryResultUtilDao;

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private HibernateUtilDao hibernateUtilDao;

	@Autowired
	private PaginationUtil paginationUtil;

	@SuppressWarnings("unchecked")
	@Override
	public List<FlexiFieldResponseDTO> findAll(Integer loggedInUserId) {
		if (XamplifyUtils.isValidInteger(loggedInUserId)) {
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			String sqlString = "select  xcf.id as \"id\",xcf.field_name as \"fieldName\" from " + XT_FLEXI_FIELDS
					+ " xcf,xt_user_profile xup where xcf.company_id  = xup.company_id  and xup.user_id  = :userId order by xcf.created_time asc\n";
			hibernateSQLQueryResultRequestDTO.setQueryString(sqlString);
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("userId", loggedInUserId));
			return (List<FlexiFieldResponseDTO>) hibernateSQLQueryResultUtilDao
					.getListDto(hibernateSQLQueryResultRequestDTO, FlexiFieldResponseDTO.class);
		} else {
			return Collections.emptyList();
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> findAllFlexiFieldNamesByCompanyIdAndExcludeFieldNameById(Integer companyId, Integer id) {
		if (XamplifyUtils.isValidInteger(companyId)) {
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO.setQueryString("select lower(trim(field_name)) from " + XT_FLEXI_FIELDS
					+ " where company_id = :companyId and field_name is not null and id!=:id");
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO(XamplifyConstants.ID, id));
			return (List<String>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
		} else {
			return Collections.emptyList();
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> findAllFlexiFieldNamesByCompanyId(Integer companyId) {
		if (XamplifyUtils.isValidInteger(companyId)) {
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO.setQueryString("select lower(trim(field_name)) from " + XT_FLEXI_FIELDS
					+ " where company_id = :companyId and field_name is not null");
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId));
			return (List<String>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
		} else {
			return Collections.emptyList();
		}

	}

	@Override
	public FlexiField getById(Integer id) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(FlexiField.class);
		criteria.add(Restrictions.eq("id", id));
		criteria.setProjection(Projections.projectionList().add(Projections.property("id"), "id")
				.add(Projections.property(FIELD_NAME), FIELD_NAME)
				.add(Projections.property(CREATED_TIME), CREATED_TIME));
		criteria.setResultTransformer(Transformers.aliasToBean(FlexiField.class));
		return (FlexiField) criteria.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findIdsByCompanyId(Integer companyId) {
		if (XamplifyUtils.isValidInteger(companyId)) {
			Session session = sessionFactory.getCurrentSession();
			Criteria criteria = session.createCriteria(FlexiField.class);
			criteria.add(Restrictions.eq("company.id", companyId));
			List<String> propertyNames = new ArrayList<>();
			propertyNames.add("id");
			return (List<Integer>) hibernateUtilDao.getProjectionList(criteria, propertyNames);
		} else {
			return Collections.emptyList();
		}

	}

	@Override
	public void delete(Integer id) {
		Session session = sessionFactory.getCurrentSession();
		String hqlString = "delete from FlexiField where id=:id";
		Query query = session.createQuery(hqlString);
		query.setParameter("id", id);
		query.executeUpdate();
	}

	@Override
	public void update(FlexiFieldRequestDTO flexiFieldRequestDTO) {
		Session session = sessionFactory.getCurrentSession();
		String hql = "UPDATE FlexiField c SET c.fieldName = :fieldName,c.updatedTime = :updatedTime, c.updatedBy.userId = :updatedBy WHERE id = :id";
		Query query = session.createQuery(hql);
		query.setParameter(FIELD_NAME, flexiFieldRequestDTO.getFieldName());
		query.setParameter("updatedTime", new Date());
		query.setParameter("updatedBy", flexiFieldRequestDTO.getLoggedInUserId());
		query.setParameter("id", flexiFieldRequestDTO.getId());
		query.executeUpdate();
	}

	@Override
	public Map<String, Object> findPaginatedFlexiFields(Pagination pagination, String search) {
		String queryString = "select cf.id as \"id\",cf.field_name as \"fieldName\",  "
				+ " case when length(TRIM(concat(up.firstname,'',up.lastname)))>0 then TRIM(concat(up.firstname,' ',up.lastname)) else up.email_id end "
				+ " as \"createdBy\",cf.created_time as \"createdTime\"   from " + XT_FLEXI_FIELDS
				+ " cf,xt_user_profile up \r\n" + " where up.user_id = cf.created_by and cf.company_id = :companyId ";
		String sortQueryString = addSortColumns(pagination);
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		QueryParameterDTO queryParameterDTO = new QueryParameterDTO(XamplifyConstants.COMPANY_ID,
				pagination.getCompanyId());
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(queryParameterDTO);
		hibernateSQLQueryResultRequestDTO.setSortQueryString(sortQueryString);
		hibernateSQLQueryResultRequestDTO.setClassInstance(FlexiFieldResponseDTO.class);
		List<String> searchColumns = new ArrayList<>();
		searchColumns.add("up.email_id");
		searchColumns.add("up.firstname");
		searchColumns.add("up.lastname");
		searchColumns.add("cf.field_name");
		hibernateSQLQueryResultRequestDTO.setSearchColumns(searchColumns);
		return hibernateSQLQueryResultUtilDao.returnPaginatedDTOList(hibernateSQLQueryResultRequestDTO, pagination,
				search);

	}

	private String addSortColumns(Pagination pagination) {
		List<SortColumnDTO> sortColumnDTOs = new ArrayList<>();
		SortColumnDTO idSortOption = new SortColumnDTO("id", "cf.id", true, false, false);
		SortColumnDTO createdTimeSortOption = new SortColumnDTO(CREATED_TIME, "cf.created_time", false, true, false);
		SortColumnDTO titleSortOption = new SortColumnDTO(FIELD_NAME, "cf.field_name", false, true, false);
		sortColumnDTOs.add(createdTimeSortOption);
		sortColumnDTOs.add(titleSortOption);
		sortColumnDTOs.add(idSortOption);
		return paginationUtil.generateSortQuery(pagination, sortColumnDTOs, "desc");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<FlexiFieldRequestDTO> findFlexiFieldsBySelectedUserIdAndUserListId(Integer userListId, Integer selectedUserId) {
		String queryString = "select xff.id as \"id\", xff.field_name as \"fieldName\", xulff.flexi_field_value as \"fieldValue\"\n" + 
				"from  " + XT_USER_USERLIST + " xuul, " + XT_USER_LIST_FLEXI_FIELD + " xulff, " + XT_FLEXI_FIELDS + " xff\n" + 
				"where xulff.user_user_list_id =  xuul.id and xulff.flexi_field_id = xff.id\n" + 
				"and xuul.user_list_id = :userListId and xuul.user_id = :userId order by xff.created_time desc\n";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		QueryParameterDTO queryParameterDTO1 = new QueryParameterDTO(XamplifyConstants.USER_LIST_ID, userListId);
		QueryParameterDTO queryParameterDTO2 = new QueryParameterDTO(XamplifyConstants.USER_ID, selectedUserId);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(queryParameterDTO1);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(queryParameterDTO2);
		return (List<FlexiFieldRequestDTO>) hibernateSQLQueryResultUtilDao.getListDto(hibernateSQLQueryResultRequestDTO, 
				FlexiFieldRequestDTO.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PipelineStageDto> findContactStatusStages(Integer companyId) {
		String queryString = "select xcs.id as \"id\", xcs.stage_name as \"stageName\", "
				+ "xcs.is_default as \"defaultStage\", xcs.company_id as \"companyId\" \n"
				+ "from " + XT_CONTACT_STATUS + " xcs \n"
				+ "where xcs.company_id = :companyId order by xcs.id asc";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		QueryParameterDTO queryParameterDTO1 = new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(queryParameterDTO1);
		return (List<PipelineStageDto>) hibernateSQLQueryResultUtilDao.getListDto(hibernateSQLQueryResultRequestDTO,
				PipelineStageDto.class);
	}

	@Override
	public void deleteContactStatusStage(Integer id) {
		Session session = sessionFactory.getCurrentSession();
		String hqlString = "delete from ContactStatus where id=:id";
		Query query = session.createQuery(hqlString);
		query.setParameter("id", id);
		query.executeUpdate();
	}

	@Override
	public boolean isValidContactStatusStageId(Integer id) {
		String queryString = "SELECT EXISTS (SELECT 1 FROM xt_user_userlist WHERE contact_status_id = :id)";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("id", id));
		return (boolean) hibernateSQLQueryResultUtilDao.returnBoolean(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public Integer findContactStatusIdByUserIdAndUserListId(Integer userId, Integer userListId) {
		String queryString = "select contact_status_id from " + XT_USER_USERLIST
				+ " where user_id = :userId and user_list_id = :userListId";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(XamplifyConstants.USER_ID, userId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(XamplifyConstants.USER_LIST_ID, userListId));
		return (Integer) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
	}

}
