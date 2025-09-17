package com.xtremand.activity.dao.hibernate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.activity.bom.NoteVisibilityType;
import com.xtremand.activity.dao.NoteDao;
import com.xtremand.activity.dto.NoteDTO;
import com.xtremand.activity.dto.NoteResponseDTO;
import com.xtremand.common.bom.Pagination;
import com.xtremand.user.bom.User;
import com.xtremand.util.PaginationUtil;
import com.xtremand.util.dao.HibernateSQLQueryResultUtilDao;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.QueryParameterDTO;
import com.xtremand.util.dto.SortColumnDTO;
import com.xtremand.util.dto.XamplifyConstants;

@Repository
@Transactional
public class HibernateNoteDao implements NoteDao{

	private static final String CAST_XN_CREATED_TIME_AS_TEXT_AS_CREATED_TIME_XN_UPDATED_BY_AS_UPDATED_BY_CAST_XN_UPDATED_TIME_AS_TEXT_AS_UPDATED_TIME_XUP_FIRSTNAME_AS_CREATED_BY_FIRST_NAME_XUP_LASTNAME_AS_CREATED_BY_LAST_NAME = "cast(xn.created_time as text) AS \"createdTime\", xn.updated_by AS \"updatedBy\", cast(xn.updated_time as text) AS \"updatedTime\", xup.firstname AS \"createdByFirstName\", xup.lastname AS \"createdByLastName\", ";

	private static final String TITLE = "title";

	private static final String USER_ID = "userId";

	private static final String SELECT_XN_ID_AS_ID_XN_TITLE_AS_TITLE_XN_CONTENT_AS_CONTENT_CAST_XN_VISIBILITY_AS_TEXT_AS_VISIBILITY = "SELECT xn.id AS \"id\", xn.title AS \"title\", xn.content AS \"content\", CAST(xn.visibility AS TEXT) AS \"visibility\", ";

	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired
	private HibernateSQLQueryResultUtilDao hibernateSQLQueryResultUtilDao;
	
	private static final String CREATED_TIME = XamplifyConstants.CREATED_TIME;
	
	@Autowired
	private PaginationUtil paginationUtil;
	
	@Value("${company.notes.from.and.where.clause.data.query}")
	private String companyNotesFromAndWhereClauseDataQuery;
	
	@Value("${company.notes.from.and.where.clause.count.query}")
	private String companyNotesFromAndWhereClauseCountQuery;
	
	@Value("${contact.notes.from.and.where.clause.data.query}")
	private String contactNotesFromAndWhereClauseDataQuery;
	
	@Value("${contact.notes.from.and.where.clause.count.query}")
	private String contactNotesFromAndWhereClauseCountQuery;
	
	@Value("${all.note.activity.data.query}")
	private String allNoteActivityDataQuery;
		
	@Override
	public NoteResponseDTO getNoteById(Integer id) {
		String sqlQuery = SELECT_XN_ID_AS_ID_XN_TITLE_AS_TITLE_XN_CONTENT_AS_CONTENT_CAST_XN_VISIBILITY_AS_TEXT_AS_VISIBILITY
				+ "CAST(xn.association_type AS TEXT) AS \"associationType\", "
				+ "xn.is_pinned AS \"pinned\", xn.created_by AS \"createdBy\", "
				+ CAST_XN_CREATED_TIME_AS_TEXT_AS_CREATED_TIME_XN_UPDATED_BY_AS_UPDATED_BY_CAST_XN_UPDATED_TIME_AS_TEXT_AS_UPDATED_TIME_XUP_FIRSTNAME_AS_CREATED_BY_FIRST_NAME_XUP_LASTNAME_AS_CREATED_BY_LAST_NAME
				+ "xup.email_id AS \"createdByEmailId\", xcp.company_name AS \"createdByCompanyName\" "
				+ "FROM xt_note AS xn INNER JOIN xt_user_profile xup ON xn.created_by = xup.user_id "
				+ "LEFT JOIN xt_company_profile xcp ON xup.company_id = xcp.company_id " + "where xn.id = :id";

		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(sqlQuery);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(XamplifyConstants.ID, id));
		return (NoteResponseDTO) hibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO,
				NoteResponseDTO.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<NoteResponseDTO> getNotesDataByUserId(Integer userId) {
		String sqlQuery = SELECT_XN_ID_AS_ID_XN_TITLE_AS_TITLE_XN_CONTENT_AS_CONTENT_CAST_XN_VISIBILITY_AS_TEXT_AS_VISIBILITY
				+ "CAST(xn.association_type AS TEXT) AS \"associationType\", "
				+ "xn.is_pinned AS \"pinned\", xn.created_by AS \"createdBy\", cast(xn.created_time as text) AS \"createdTime\", "
				+ "xn.updated_by AS \"updatedBy\", cast(xn.updated_time as text) AS \"updatedTime\", xup.firstname AS \"createdByFirstName\", "
				+ "xup.email_id AS \"createdByEmailId\", xcp.company_name AS \"createdByCompanyName\", xup.lastname AS \"createdByLastName\", "
				+ "FROM xt_note AS xn INNER JOIN xt_user_profile xup ON xn.created_by = xup.user_id "
				+ "LEFT JOIN xt_company_profile xcp ON xup.company_id = xcp.company_id "
				+ "where xn.created_by = :userId";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(sqlQuery);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(XamplifyConstants.USER_ID, userId));
		return (List<NoteResponseDTO>) hibernateSQLQueryResultUtilDao.getListDto(hibernateSQLQueryResultRequestDTO,
				NoteResponseDTO.class);
	}

	@Override
	public void update(Integer id, NoteDTO noteDTO) {
		Integer loggedInUserId = noteDTO.getLoggedInUserId();
		User user = new User();
		user.setUserId(loggedInUserId);
		String hqlQuery = "Update Note set title = :title, content = :content, visibility = :visibility, "
				+ " updatedBy = :updatedBy, updatedTime = :updatedTime where id = :id";
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createQuery(hqlQuery);
		query.setParameter(TITLE, noteDTO.getTitle());
		query.setParameter("content", noteDTO.getContent());
		query.setParameter("updatedTime", new Date());
		query.setParameter("updatedBy", user);
		if (NoteVisibilityType.PRIVATE.getNoteVisibilityType().equals(noteDTO.getVisibility())) {
			query.setParameter("visibility", NoteVisibilityType.PRIVATE);
		} else if (NoteVisibilityType.PUBLIC.getNoteVisibilityType().equals(noteDTO.getVisibility())) {
			query.setParameter("visibility", NoteVisibilityType.PUBLIC);
		}
		query.setParameter(XamplifyConstants.ID, id);
		query.executeUpdate();
	}

	@Override
	public void delete(Integer loggedInUserId, Integer id) {
		String sqlQuery = "delete from xt_note where id = :id";
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(sqlQuery);
		query.setParameter(XamplifyConstants.ID, id);
		query.executeUpdate();
	}
	
	@Override
	public Map<String, Object> getPaginatedNotes(Pagination pagination, String searchKey) {
		String fromAndWhereClauseDataQueryString = "";
		String fromAndWhereClauseRowCountQueryString = "";
		String addedForEmailIdString = "";
		if (Boolean.TRUE.equals(pagination.getIsCompanyJourney())) {
			fromAndWhereClauseDataQueryString = companyNotesFromAndWhereClauseDataQuery;
			fromAndWhereClauseRowCountQueryString = companyNotesFromAndWhereClauseCountQuery;
			addedForEmailIdString = " adup.email_id as \"addedForEmailId\", ";
		} else {
			fromAndWhereClauseDataQueryString = contactNotesFromAndWhereClauseDataQuery;
			fromAndWhereClauseRowCountQueryString = contactNotesFromAndWhereClauseCountQuery;
		}
		String sqlQuery = allNoteActivityDataQuery.replace("{addedForEmailIdString}", addedForEmailIdString);
		sqlQuery = sqlQuery.replace("{fromAndWhereClauseDataQueryString}", fromAndWhereClauseDataQueryString);
		String sortQueryString = addSortColumns(pagination);
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(sqlQuery);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(USER_ID, pagination.getUserId()));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("contactId", pagination.getContactId()));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, pagination.getCompanyId()));
		hibernateSQLQueryResultRequestDTO.setSortQueryString(sortQueryString);
		hibernateSQLQueryResultRequestDTO.setClassInstance(NoteResponseDTO.class);
		List<String> searchColumns = new ArrayList<>();
		searchColumns.add("xn.title");
		hibernateSQLQueryResultRequestDTO.setSearchColumns(searchColumns);
		hibernateSQLQueryResultRequestDTO.setRowCountQueryString("select cast(count(xn.id) as int) " + fromAndWhereClauseRowCountQueryString);
		return hibernateSQLQueryResultUtilDao.returnPaginatedDTOList(hibernateSQLQueryResultRequestDTO, pagination,
				searchKey);
	}

	private String addSortColumns(Pagination pagination) {
		List<SortColumnDTO> sortColumnDTOs = new ArrayList<>();
		SortColumnDTO idSortOption = new SortColumnDTO("id", "xn.id", true, false, false);
		SortColumnDTO createdTimeSortOption = new SortColumnDTO(CREATED_TIME, "xn.created_time", false, true, false);
		SortColumnDTO subjectSortOption = new SortColumnDTO(TITLE, "xn.title", false, true, false);
		sortColumnDTOs.add(createdTimeSortOption);
		sortColumnDTOs.add(subjectSortOption);
		sortColumnDTOs.add(idSortOption);
		return paginationUtil.generateSortQuery(pagination, sortColumnDTOs, "desc");
	}
	
	@SuppressWarnings("unchecked")
    @Override
	public List<NoteDTO> fetchNotesForConatctAgent(String dynamicQueryCondition, Integer companyId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String queryString = "select xn.id as \"id\", xn.title as \"title\", xn.content as \"content\", initcap(CAST(xn.visibility AS TEXT)) as \"visibility\", "
				+ "cast(xn.created_time as text) as \"createdTime\", cast(xn.updated_time as text) as \"updatedTime\", adup.email_id as \"addedForEmailId\" "
				+ "from xt_note xn left join xt_user_profile adup on xn.contact_id = adup.user_id join ( select distinct(uul.user_id) as \"id\" from xt_user_list ul join xt_user_userlist uul on ul.user_list_id = uul.user_list_id join xt_user_profile up on uul.user_id = up.user_id "
				+ " where ul.company_id = :companyId and ul.module_name = 'CONTACTS' " + dynamicQueryCondition
				+ " ) a on xn.contact_id = a.id "
				+ " where xn.company_id = :companyId and xn.association_type = 'CONTACT' order by xn.created_time desc";
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("companyId", companyId));
		return (List<NoteDTO>) hibernateSQLQueryResultUtilDao.getListDto(hibernateSQLQueryResultRequestDTO,
				NoteDTO.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<NoteDTO> fetchNotesForConatctAgentOnContact(Integer companyId, Integer contactId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String queryString = "select xn.id as \"id\", xn.title as \"title\", xn.content as \"content\", initcap(CAST(xn.visibility AS TEXT)) as \"visibility\", "
				+ "cast(xn.created_time as text) as \"createdTime\", cast(xn.updated_time as text) as \"updatedTime\", adup.email_id as \"addedForEmailId\" "
				+ "from xt_note xn left join xt_user_profile adup on xn.contact_id = adup.user_id "
				+ "where xn.contact_id = :contactId and xn.company_id = :companyId and xn.association_type = 'CONTACT' order by xn.created_time desc";
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("companyId", companyId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("contactId", contactId));
		return (List<NoteDTO>) hibernateSQLQueryResultUtilDao.getListDto(hibernateSQLQueryResultRequestDTO,
				NoteDTO.class);
	}

}
