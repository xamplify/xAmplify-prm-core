package com.xtremand.activity.dao.hibernate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.activity.dao.EmailActivityDAO;
import com.xtremand.activity.dto.ActivityAttachmentDTO;
import com.xtremand.activity.dto.EmailActivityDTO;
import com.xtremand.activity.dto.EmailMergeTagDTO;
import com.xtremand.common.bom.Pagination;
import com.xtremand.util.PaginationUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.HibernateSQLQueryResultUtilDao;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.QueryParameterDTO;
import com.xtremand.util.dto.SortColumnDTO;
import com.xtremand.util.dto.XamplifyConstants;

@Repository
@Transactional
public class HibernateEmailActivityDAO implements EmailActivityDAO {
	
	private static final String EMAIL_IDS = "emailIds";

	private static final String FROM_XT_EMAIL_ACTIVITY_EA_JOIN_XT_USER_PROFILE_UP_ON_EA_SENDER_USER_ID_UP_USER_ID = "from xt_email_activity ea join xt_user_profile up on ea.sender_user_id = up.user_id ";

	@Autowired
	HibernateSQLQueryResultUtilDao hibernateSQLQueryResultUtilDao;
	
	@Autowired
	private PaginationUtil paginationUtil;
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Value("${company.emails.from.and.where.clause.data.query}")
	private String companyEmailsFromAndWhereClauseDataQuery;
	
	@Value("${company.emails.from.and.where.clause.count.query}")
	private String companyEmailsFromAndWhereClauseCountQuery;
	
	@Value("${contact.emails.from.and.where.clause.data.query}")
	private String contactEmailsFromAndWhereClauseDataQuery;
	
	@Value("${contact.emails.from.and.where.clause.count.query}")
	private String contactEmailsFromAndWhereClauseCountQuery;
	
	@Value("${all.email.activity.data.query}")
	private String allEmailActivityDataQuery;
	
	private static final String CREATED_TIME = XamplifyConstants.CREATED_TIME;

	@Override
	public Map<String, Object> fetchAllEmailActivities(Pagination pagination, String search) {
		String fromAndWhereClauseDataQueryString = "";
		String fromAndWhereClauseRowCountQueryString = "";
		String addedForEmailIdString = "";
		if (Boolean.TRUE.equals(pagination.getIsCompanyJourney())) {
			fromAndWhereClauseDataQueryString = companyEmailsFromAndWhereClauseDataQuery;
			fromAndWhereClauseRowCountQueryString = companyEmailsFromAndWhereClauseCountQuery;
			addedForEmailIdString = " adup.email_id as \"addedForEmailId\", ";
		} else {
			fromAndWhereClauseDataQueryString = contactEmailsFromAndWhereClauseDataQuery;
			fromAndWhereClauseRowCountQueryString = contactEmailsFromAndWhereClauseCountQuery;
		}
		String queryString = allEmailActivityDataQuery.replace("{addedForEmailIdString}", addedForEmailIdString) + fromAndWhereClauseDataQueryString;
		String sortQueryString = addSortColumns(pagination);
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("companyId",pagination.getCompanyId()));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("userId", pagination.getContactId()));
		hibernateSQLQueryResultRequestDTO.setSortQueryString(sortQueryString);
		hibernateSQLQueryResultRequestDTO.setClassInstance(EmailActivityDTO.class);
		List<String> searchColumns = new ArrayList<>();
		searchColumns.add("ea.subject");
		hibernateSQLQueryResultRequestDTO.setSearchColumns(searchColumns);
		hibernateSQLQueryResultRequestDTO.setRowCountQueryString("select cast(count(ea.id) as int) " + fromAndWhereClauseRowCountQueryString);
		return hibernateSQLQueryResultUtilDao.returnPaginatedDTOList(hibernateSQLQueryResultRequestDTO, pagination,
				search);
	}
	
	private String addSortColumns(Pagination pagination) {
		List<SortColumnDTO> sortColumnDTOs = new ArrayList<>();
		SortColumnDTO idSortOption = new SortColumnDTO("id", "ea.id", true, false, false);
		SortColumnDTO createdTimeSortOption = new SortColumnDTO(CREATED_TIME, "ea.created_time", false, true, false);
		SortColumnDTO subjectSortOption = new SortColumnDTO("subject", "ea.subject", false, true, false);
		sortColumnDTOs.add(createdTimeSortOption);
		sortColumnDTOs.add(subjectSortOption);
		sortColumnDTOs.add(idSortOption);
		return paginationUtil.generateSortQuery(pagination, sortColumnDTOs, "desc");
	}

	@Override
	public EmailActivityDTO fetchEmailActivityById(Integer emailActivityId) {
		String queryString = "select ea.id as \"id\", ea.subject as \"subject\", ea.body as \"body\", "
				+ "up.email_id as \"senderEmailId\", cast(ea.created_time as text) as \"createdTime\", cast(ea.status as text) as \"status\", "
				+ "string_agg(case when er.email_recipient_type = 'CC' then er.email_id end, ',') as \"ccEmailIdsString\", "
				+ "string_agg(case when er.email_recipient_type = 'BCC' then er.email_id end, ',') as \"bccEmailIdsString\", adup.email_id as \"addedForEmailId\", "
				+ "concat(adup.firstname, case when adup.firstname is not null and adup.middle_name is not null then concat(' ',adup.middle_name) else adup.middle_name end, "
				+ "case when adup.firstname is not null or adup.middle_name is not null then concat(' ',adup.lastname) else adup.lastname end) as \"fullName\" "
				+ FROM_XT_EMAIL_ACTIVITY_EA_JOIN_XT_USER_PROFILE_UP_ON_EA_SENDER_USER_ID_UP_USER_ID
				+ "left join xt_email_recipient er on ea.id = er.email_activity_id left join xt_user_profile adup on ea.recipient_user_id = adup.user_id "
				+ "where ea.id = :emailActivityId group by 1, 4, 9, 10";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("emailActivityId", emailActivityId));
		return (EmailActivityDTO) hibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO,
				EmailActivityDTO.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ActivityAttachmentDTO> fetchEmailAttachments(Integer emailActivityId) {
		String queryString = "select file_name as \"fileName\", file_path as \"filePath\" from xt_activity_attachment where email_activity_id = :emailActivityId";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("emailActivityId", emailActivityId));
		return (List<ActivityAttachmentDTO>) hibernateSQLQueryResultUtilDao.getListDto(hibernateSQLQueryResultRequestDTO, ActivityAttachmentDTO.class);
	}
  
	@Override
	public EmailMergeTagDTO fetchEmailMergeTagsData(Integer loggedInUserId, Integer contactId) {
		String queryString = "select cup.firstname as \"contactFirstName\", cup.lastname as \"contactLastName\", cup.email_id as \"contactEmailId\", "
				+ "ccp.company_name as \"contactCompanyName\", cup.mobile_number as \"contactMobileNumber\", cup.address as \"contactAddress\", cup.zip as \"contactZipCode\", "
				+ "cup.city as \"contactCity\", cup.state as \"contactState\", cup.country as \"contactCountry\", lup.firstname as \"senderFirstName\", "
				+ "lup.middle_name as \"senderMiddleName\", lup.lastname as \"senderLastName\", lup.email_id as \"senderEmailId\", lup.mobile_number as \"senderMobileNumber\", "
				+ "lup.job_title as \"senderJobTitle\", lcp.company_name as \"senderCompanyName\", lcp.website as \"senderCompanyUrl\", lcp.google_plus_link as \"senderCompanyGoogleUrl\", "
				+ "lcp.facebook_link as \"senderCompanyFacebookUrl\", lcp.instagram_link as \"senderCompanyInstagramUrl\", lcp.linked_in_link as \"senderCompanyLinkedInUrl\", "
				+ "lcp.twitter_link as \"senderCompanyTwitterUrl\", lcp.phone as \"senderCompanyContactNumber\", lcp.about_us as \"senderAboutUs\", "
				+ "lcp.event_url as \"senderEventUrl\", lcp.privacy_policy as \"senderPrivacyPolicy\", ccp.about_us as \"contactAboutUs\", lcp.company_logo as \"senderCompanyLogoPath\" "
				+ "from xt_user_profile cup left join xt_company_profile ccp on cup.company_id = ccp.company_id "
				+ "join xt_user_profile lup on lup.user_id = :loggedInUserId left join xt_company_profile lcp on lup.company_id = lcp.company_id where cup.user_id = :contactId";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("loggedInUserId", loggedInUserId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("contactId", contactId));
		return (EmailMergeTagDTO) hibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO, EmailMergeTagDTO.class);
	}

	@Override
	public Map<String, Object> fetchAllEmailActivitiesForCompanyJourney(Pagination pagination, List<Integer> userIds,
			String searchKey) {
		Map<String, Object> map = new HashMap<>();
		if (XamplifyUtils.isNotEmptyList(userIds)) {
			Session session = sessionFactory.getCurrentSession();
			List<Integer> emailIds = fetchEmailIds(pagination, userIds, searchKey, session);
			List<EmailActivityDTO> emailActivities = new ArrayList<>();
			if (XamplifyUtils.isNotEmptyList(emailIds)) {
				emailActivities = fetchEmailActivitiesByEmailIds(pagination, emailIds, session);
			}
			map.put("list", emailActivities);
			map.put("totalRecords", emailIds.size());
		}
		return map;
	}
	
	@SuppressWarnings("unchecked")
	private List<Integer> fetchEmailIds(Pagination pagination, List<Integer> userIds,
			String searchKey, Session session) {
		List<Integer> taskIds = new ArrayList<>();
		List<List<Integer>> chunkedUserIdsList = XamplifyUtils.getChunkedList(userIds);
		if (XamplifyUtils.isNotEmptyList(chunkedUserIdsList.get(0))) {
			String sqlQuery = frameSqlQuery(pagination, searchKey);
			for (List<Integer> chunckedUserIds : chunkedUserIdsList) {
				SQLQuery query = session.createSQLQuery(sqlQuery);
				query.setParameter("companyId", pagination.getCompanyId());
				query.setParameterList("userIds", chunckedUserIds);
				taskIds.addAll(query.list());
			}
		}
		return taskIds;
	}
	
	private String frameSqlQuery(Pagination pagination, String searchKey) {
		String searchQuery = "";
		if (XamplifyUtils.isValidString(searchKey)) {
			searchQuery = " and ea.subject ilike '%" + searchKey + "%' ";
		}
		String orderQuery = getSortQuery(pagination);
		return "select ea.id " + FROM_XT_EMAIL_ACTIVITY_EA_JOIN_XT_USER_PROFILE_UP_ON_EA_SENDER_USER_ID_UP_USER_ID 
				+ " where ea.company_id = :companyId and ea.recipient_user_id in (:userIds) " + searchQuery + orderQuery;
	}
	
	@SuppressWarnings("unchecked")
	private List<EmailActivityDTO> fetchEmailActivitiesByEmailIds(Pagination pagination, List<Integer> emailIds, Session session) {
		List<EmailActivityDTO> emailActivities = new ArrayList<>();
		if (XamplifyUtils.isNotEmptyList(emailIds)) {
			String orderQuery = getSortQuery(pagination);
			String queryString = "select ea.id as \"id\", ea.subject as \"subject\", cast(ea.status as text) as \"status\", adup.email_id as \"addedForEmailId\", "
					+ "up.email_id as \"senderEmailId\", cast(ea.created_time as text) as \"createdTime\", cast(ea.opened_time as text) as \"openedTime\" "
					+ FROM_XT_EMAIL_ACTIVITY_EA_JOIN_XT_USER_PROFILE_UP_ON_EA_SENDER_USER_ID_UP_USER_ID
					+ " left join xt_user_profile adup on ea.recipient_user_id = adup.user_id "
					+ "where ea.id in (:emailIds) " + orderQuery;
			Integer limitMaxResults = pagination.getMaxResults();
			SQLQuery query = session.createSQLQuery(queryString);
			Integer startIndex = ((pagination.getPageIndex() - 1) * pagination.getMaxResults());
			Integer endIndex = ((pagination.getPageIndex() - 1) * pagination.getMaxResults()) + limitMaxResults;
			if (emailIds.size() > limitMaxResults) {
				if (endIndex <= emailIds.size()) {
					query.setParameterList(EMAIL_IDS, emailIds.subList(startIndex, endIndex));
				} else {
					query.setParameterList(EMAIL_IDS, emailIds.subList(startIndex, emailIds.size()));
				}
			} else {
				query.setParameterList(EMAIL_IDS, emailIds);
			}
			emailActivities = query.setResultTransformer(Transformers.aliasToBean(EmailActivityDTO.class)).list();
		}
		return emailActivities;
	}

	private String getSortQuery(Pagination pagination) {
		String orderQuery = " order by ea.created_time desc";
		if (XamplifyUtils.isValidString(pagination.getSortingOrder())) {
			if (pagination.getSortingOrder().equals("ASC")) {
				if ("createdTime".equals(pagination.getSortcolumn())) {
					orderQuery = " order by ea.created_time ";
				} else {
					orderQuery = " order by ea.subject ";
				}
			} else {
				if ("subject".equals(pagination.getSortcolumn())) {
					orderQuery = " order by ea.subject desc ";
				}
			}
		}
		return orderQuery;
	}

	@Override
	public String getTemplateById(Integer templateId) {
		String queryString = "select body from xt_email_templates where id = :templateId";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("templateId", templateId));
		return (String) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<EmailActivityDTO> fetchEmailActivitiesForConatctAgent(String dynamicQueryCondition, Integer companyId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String queryString = "select ea.id as \"id\",ea.subject as \"subject\", ea.body as \"body\", cast(ea.created_time as text) as \"createdTime\", initcap(cast(ea.status as text)) as status, cast(ea.opened_time as text) as \"openedTime\", rup.email_id as \"addedForEmailId\", "
				+ "concat(rup.firstname, case when rup.firstname is not null and rup.middle_name is not null then concat(' ', rup.middle_name) else rup.middle_name end, case when rup.firstname is not null or rup.middle_name is not null then concat(' ', rup.lastname) else rup.lastname end ) as \"recipientName\", "
				+ "sup.email_id as \"senderEmailId\", concat(sup.firstname, case when sup.firstname is not null and sup.middle_name is not null then concat(' ', sup.middle_name) else sup.middle_name end, case when sup.firstname is not null or sup.middle_name is not null then concat(' ', sup.lastname) else sup.lastname end ) as \"senderName\", "
				+ "string_agg(case when er.email_recipient_type = 'CC' then er.email_id end, ',') as \"ccEmailIdsString\", "
				+ "string_agg(case when er.email_recipient_type = 'BCC' then er.email_id end, ',') as \"bccEmailIdsString\", "
				+ "CAST( json_object_agg( aa.id, json_build_object('fileId', COALESCE(aa.open_ai_file_id, aa.file_path, ''), 'fileName', regexp_replace(aa.file_name, '\\.[^.]+$', '') )) "
				+ " FILTER ( WHERE aa.id IS NOT NULL AND COALESCE(aa.file_name, aa.file_path) ~* '\\.(pdf|docx?|pptx?|csv|xlsx)(\\?.*)?$' ) AS text ) AS \"attachmentPaths\" "
				+ " from xt_email_activity ea join xt_user_profile rup on rup.user_id = ea.recipient_user_id join xt_user_profile sup on sup.user_id = ea.sender_user_id left join xt_email_recipient er on er.email_activity_id = ea.id left join xt_activity_attachment aa on aa.email_activity_id = ea.id "
				+ " join ( select distinct(uul.user_id) as \"id\" from xt_user_list ul join xt_user_userlist uul on"
				+ " ul.user_list_id = uul.user_list_id join xt_user_profile up on uul.user_id = up.user_id "
				+ " where ul.company_id = :companyId and ul.module_name = 'CONTACTS' " + dynamicQueryCondition
				+ " ) a on ea.recipient_user_id = a.id "
				+ " where ea.company_id = :companyId group by 1,2,3,4,5,6,7,8,9,10 order by ea.created_time desc";
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("companyId", companyId));
		return (List<EmailActivityDTO>) hibernateSQLQueryResultUtilDao.getListDto(hibernateSQLQueryResultRequestDTO,
				EmailActivityDTO.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<EmailActivityDTO> fetchEmailActivitiesForConatctAgentOnContact(Integer companyId, Integer contactId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String queryString = "select ea.id as \"id\",ea.subject as \"subject\", ea.body as \"body\", cast(ea.created_time as text) as \"createdTime\", initcap(cast(ea.status as text)) as status, cast(ea.opened_time as text) as \"openedTime\", rup.email_id as \"addedForEmailId\", "
				+ " concat(rup.firstname, case when rup.firstname is not null and rup.middle_name is not null then concat(' ', rup.middle_name) else rup.middle_name end, case when rup.firstname is not null or rup.middle_name is not null then concat(' ', rup.lastname) else rup.lastname end ) as \"recipientName\", "
				+ " sup.email_id as \"senderEmailId\", concat(sup.firstname, case when sup.firstname is not null and sup.middle_name is not null then concat(' ', sup.middle_name) else sup.middle_name end, case when sup.firstname is not null or sup.middle_name is not null then concat(' ', sup.lastname) else sup.lastname end ) as \"senderName\", "
				+ " string_agg(case when er.email_recipient_type = 'CC' then er.email_id end, ',') as \"ccEmailIdsString\", string_agg(case when er.email_recipient_type = 'BCC' then er.email_id end, ',') as \"bccEmailIdsString\", "
				+ " CAST( json_object_agg( aa.id, json_build_object('fileId', COALESCE(aa.open_ai_file_id, aa.file_path, ''), 'fileName', regexp_replace(aa.file_name, '\\.[^.]+$', '') )) "
				+ " FILTER ( WHERE aa.id IS NOT NULL AND COALESCE(aa.file_name, aa.file_path) ~* '\\.(pdf|docx?|pptx?|csv|xlsx)(\\?.*)?$' ) AS text ) AS \"attachmentPaths\" "
				+ " from xt_email_activity ea join xt_user_profile rup on rup.user_id = ea.recipient_user_id join xt_user_profile sup on sup.user_id = ea.sender_user_id left join xt_email_recipient er on er.email_activity_id = ea.id left join xt_activity_attachment aa on aa.email_activity_id = ea.id "
				+ " where ea.recipient_user_id = :contactId and ea.company_id = :companyId group by 1,2,3,4,5,6,7,8,9,10 order by ea.created_time desc";
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("companyId", companyId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("contactId", contactId));
		return (List<EmailActivityDTO>) hibernateSQLQueryResultUtilDao.getListDto(hibernateSQLQueryResultRequestDTO,
				EmailActivityDTO.class);
	}

	@Override
	public EmailActivityDTO fetchWelcomeEmailActivityById(Integer emailActivityId) {	
		String queryString = "select distinct e.subject as \"subject\",e.created_time as \"sentOn\",e.status as \"status\", "+
		"e.body as \"body\",e.id as \"emailActivityId\", "+
		"(select string_agg(a1.file_name, ' ,') from xt_activity_attachment a1 where a1.email_activity_id = e.id) as \"fileNames\", "+
		"(select string_agg(a2.file_path, ' ,') from xt_activity_attachment a2 where a2.email_activity_id = e.id) as \"attachmentPaths\", "+
		"(select string_agg(r1.email_id, ' ,') from xt_email_recipient r1 where r1.email_activity_id = e.id and r1.email_recipient_type = 'TO') as \"toEmailIdsString\", "+
		"(select string_agg(r2.email_id, ' ,') from xt_email_recipient r2 where r2.email_activity_id = e.id and r2.email_recipient_type = 'CC') as \"ccEmailIdsString\", "+
		"(select string_agg(r3.email_id, ' ,') from xt_email_recipient r3 where r3.email_activity_id = e.id and r3.email_recipient_type = 'BCC') as \"bccEmailIdsString\" "+
		"from xt_email_activity e join xt_company_profile c on e.company_id = c.company_id where e.id = :emailActivityId ";

		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("emailActivityId", emailActivityId));
		return (EmailActivityDTO) hibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO,
				EmailActivityDTO.class);
	}

}
