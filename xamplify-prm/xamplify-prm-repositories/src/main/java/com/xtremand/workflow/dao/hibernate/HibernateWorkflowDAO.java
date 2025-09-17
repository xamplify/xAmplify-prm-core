package com.xtremand.workflow.dao.hibernate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.hibernate.transform.Transformers;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.xtremand.campaign.bom.WorkflowsStatusEnum;
import com.xtremand.common.bom.Pagination;
import com.xtremand.dam.exception.DamDataAccessException;
import com.xtremand.lms.dao.LMSDAO;
import com.xtremand.partner.journey.bom.TriggerComponent;
import com.xtremand.partner.journey.bom.TriggerComponentType;
import com.xtremand.partner.journey.bom.TriggerTimePhraseEnum;
import com.xtremand.partner.journey.bom.Workflow;
import com.xtremand.partner.journey.bom.WorkflowPartner;
import com.xtremand.partner.journey.bom.WorkflowUserlist;
import com.xtremand.partner.journey.dto.TriggerComponentDTO;
import com.xtremand.partner.journey.dto.WorkflowListResponseDTO;
import com.xtremand.partner.journey.dto.WorkflowResponseDTO;
import com.xtremand.user.bom.User;
import com.xtremand.user.bom.UserList;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.util.PaginationUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.HibernateSQLQueryResultUtilDao;
import com.xtremand.util.dao.HibernateUtilDao;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.QueryParameterDTO;
import com.xtremand.util.dto.QueryParameterListDTO;
import com.xtremand.util.dto.SortColumnDTO;
import com.xtremand.util.dto.UserUserListDTO;
import com.xtremand.workflow.dao.WorkflowDAO;

@Repository("WorkflowDAO")
@Transactional
public class HibernateWorkflowDAO implements WorkflowDAO {
	private static final String SPACE = " ";

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private PaginationUtil paginationUtil;

	@Autowired
	private UserDAO userDao;

	@Autowired
	private UtilDao utilDao;

	@Autowired
	private HibernateUtilDao hibernateUtilDao;

	@Value("${redistributedCampaignUsersQuery}")
	private String redistributedCampaignUsersQuery;

	@Value("${redistributedCampaignUsersGroupBy}")
	private String redistributedCampaignUsersGroupBy;

	@Value("${notRedistributedCampaignUsersQuery}")
	private String notRedistributedCampaignUsersQuery;

	@Value("${notRedistributedCampaignUsersGroupBy}")
	private String notRedistributedCampaignUsersGroupBy;

	@Value("${notSignedUpUsersQuery}")
	private String notSignedUpUsersQuery;

	@Value("${signedUpUsersQuery}")
	private String signedUpUsersQuery;

	@Value("${notActivatedUsersQuery}")
	private String notActivatedUsersQuery;

	@Value("${activatedUsersQuery}")
	private String activtedUsersQuery;

	@Value("${notCreatedCompanyProfileUsersQuery}")
	private String notCreatedCompanyProfileUsersQuery;

	@Value("${createdCompanyProfileUsersQuery}")
	private String createdCompanyProfileUsersQuery;

	@Value("${createdLeadUsersQuery}")
	private String createdLeadUsersQuery;

	@Value("${notCreatedLeadUsersQuery}")
	private String notCreatedLeadUsersQuery;

	@Value("${convertedLeadUsersQuery}")
	private String convertedLeadUsersQuery;

	@Value("${notConvertedLeadUsersQuery}")
	private String notConvertedLeadUsersQuery;

	@Value("${createdDealUsersQuery}")
	private String createdDealUsersQuery;

	@Value("${notCreatedDealUsersQuery}")
	private String notCreatedDealUsersQuery;

	@Value("${closedDealUsersQuery}")
	private String closedDealUsersQuery;

	@Value("${notClosedDealQuery}")
	private String notClosedDealQuery;

	@Value("${addedTeamMemberUsersQuery}")
	private String addedTeamMemberUsersQuery;

	@Value("${notAddedTeamMemberUsersQuery}")
	private String notAddedTeamMemberUsersQuery;

	@Value("${addedContactUsersQuery}")
	private String addedContactUsersQuery;

	@Value("${notAddedContactUsersQuery}")
	private String notAddedContactUsersQuery;

	@Value("${notCompletedTrackUsersQuery}")
	private String notCompletedTrackUsersQuery;

	@Value("${completedTrackUsersQuery}")
	private String completedTrackUsersQuery;

	@Value("${completedPlaybookUsersQuery}")
	private String completedPlaybookUsersQuery;

	@Value("${notCompletedPlaybookUsersQuery}")
	private String notCompletedPlaybookUsersQuery;

	@Value("${requestedMdfUsersQuery}")
	private String requestedMdfUsersQuery;

	@Value("${notRequestedMdfUsersQuery}")
	private String notRequestedMdfUsersQuery;

	@Value("${viewedPagesUsersQuery}")
	private String viewedPagesUsersQuery;

	@Value("${notViewedPagesUsersQuery}")
	private String notViewedPagesUsersQuery;

	@Value("${viewedTrackUsersQuery}")
	private String viewedTrackUsersQuery;

	@Value("${notViewedTrackUsersQuery}")
	private String notViewedTrackUsersQuery;

	@Value("${viewedPlaybookUsersQuery}")
	private String viewedPlaybookUsersQuery;

	@Value("${notViewedPlaybookUsersQuery}")
	private String notViewedPlaybookUsersQuery;

	@Value("${createdLeadUsersQueryGroupBy}")
	private String createdLeadUsersQueryGroupBy;

	@Value("${notCreatedLeadUsersQueryGroupBy}")
	private String notCreatedLeadUsersQueryGroupBy;

	@Value("${notConvertedLeadUsersQueryGroupBy}")
	private String notConvertedLeadUsersQueryGroupBy;

	@Value("${convertedLeadUsersQueryGroupBy}")
	private String convertedLeadUsersQueryGroupBy;

	@Value("${notCreatedDealUsersQueryGroupBy}")
	private String notCreatedDealUsersQueryGroupBy;

	@Value("${createdDealUsersQueryGroupBy}")
	private String createdDealUsersQueryGroupBy;

	@Value("${closedDealUsersQueryGroupBy}")
	private String closedDealUsersQueryGroupBy;

	@Value("${notClosedDealUsersQueryGroupBy}")
	private String notClosedDealUsersQueryGroupBy;

	@Value("${addedTeamMemberUsersQueryGroupBy}")
	private String addedTeamMemberUsersQueryGroupBy;

	@Value("${notAddedTeamMemberUsersQueryGroupBy}")
	private String notAddedTeamMemberUsersQueryGroupBy;

	@Value("${addedContactUsersQueryGroupBy}")
	private String addedContactUsersQueryGroupBy;

	@Value("${notAddedContactUsersQueryUsersQuery}")
	private String notAddedContactUsersQueryUsersQuery;

	@Value("${redistributedShareLeadUsersQuery}")
	private String redistributedShareLeadUsersQuery;

	@Value("${notRedistributedShareLeadUsersQuery}")
	private String notRedistributedShareLeadUsersQuery;

	@Value("${redistributedShareLeadUsersQueryGroupBy}")
	private String redistributedShareLeadUsersQueryGroupBy;

	@Value("${notRedistributedShareLeadUsersQueryGroupBy}")
	private String notRedistributedShareLeadUsersQueryGroupBy;

	@Value("${requestedMdfUsersQueryGroupBy}")
	private String requestedMdfUsersQueryGroupBy;

	@Value("${notRequestedMdfUsersQueryGroupBy}")
	private String notRequestedMdfUsersQueryGroupBy;

	@Value("${viewedPagesUsersQueryGroupBy}")
	private String viewedPagesUsersQueryGroupBy;

	@Value("${notViewedPagesUsersQueryGroupBy}")
	private String notViewedPagesUsersQueryGroupBy;

	@Value("${notViewedTrackUsersQueryGroupBy}")
	private String notViewedTrackUsersQueryGroupBy;

	@Value("${viewedTrackUsersQueryGroupBy}")
	private String viewedTrackUsersQueryGroupBy;

	@Value("${viewedPlaybookUsersQueryGroupBy}")
	private String viewedPlaybookUsersQueryGroupBy;

	@Value("${notViewedPlaybookUsersQueryGroupBy}")
	private String notViewedPlaybookUsersQueryGroupBy;

	@Value("#{'${in.operators}'.split(',')}")
	private List<String> inOperators;

	@Value("${createdTime.property.name}")
	private String createdTimePropertyName;

	@Value("${partnerJourney.workflow.title}")
	private String partnerJourneyWorkflowTitlePropertyName;

	@Value("${workflow.filter.user.table.alias}")
	private String userTableAlias;

	@Value("${viewedPlaybookUsersQueryForCompanies}")
	private String viewedPlaybookUsersQueryForCompanies;

	@Value("${notViewedPlaybookUsersQueryForCompanies}")
	private String notViewedPlaybookUsersQueryForCompanies;

	@Value("${completedPlaybookUsersQueryForCompanies}")
	private String completedPlaybookUsersQueryForCompanies;

	@Value("${notCompletedPlaybookUsersQueryForCompanies}")
	private String notCompletedPlaybookUsersQueryForCompanies;
	
	@Value("${playbookIdCondition}")
	private String playbookIdCondition;

	@Value("${playbookExpiryDateConditionQuery}")
	private String playbookExpiryDateConditionQuery;
	
	@Value("${viewedPlaybookUsersQueryv2}")
	private String viewedPlaybookUsersQueryv2;

	@Value("${notViewedPlaybookUsersQueryv2}")
	private String notViewedPlaybookUsersQueryv2;

	@Value("${completedPlaybookUsersQueryv2}")
	private String completedPlaybookUsersQueryv2;

	@Value("${notCompletedPlaybookUsersQueryv2}")
	private String notCompletedPlaybookUsersQueryv2;


	@Autowired
	private HibernateSQLQueryResultUtilDao hibernateSQLQueryResultUtilDao;
	
	@Autowired
	private LMSDAO lmsDAO;

	@SuppressWarnings("unchecked")
	@Override
	public List<TriggerComponentDTO> getTriggerComponentData(TriggerComponentType type) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(TriggerComponent.class, "TC");
		criteria.setProjection(
				Projections.distinct(Projections.projectionList().add(Projections.property("TC.id"), "id")
						.add(Projections.property("TC.key"), "key").add(Projections.property("TC.value"), "value")));
		criteria.add(Restrictions.eq("TC.type", type));
		criteria.addOrder(Order.asc("TC.id"));
		criteria.setResultTransformer(Transformers.aliasToBean(TriggerComponentDTO.class));
		return criteria.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<UserUserListDTO> findPartnerDetailsForWorkFlow(Integer loggedInUserId) {
		Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
		if (companyId != null && companyId > 0) {
			String sqlString = "select job_title as \"jobTitle\",vertical as \"vertical\",region as \"region\",partner_type as \"partnerType\",category as \"category\""
					+ ",address as \"address\",city as \"city\",state as \"state\",zip as \"zip\",country as \"country\" from xt_user_list ul,xt_user_userlist uul\r\n"
					+ " where ul.user_list_id = uul.user_list_id\r\n"
					+ " and ul.company_id = :companyId and ul.is_default_partnerlist";
			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(sqlString);
			query.setParameter("companyId", companyId);
			return (List<UserUserListDTO>) paginationUtil.getListDTO(UserUserListDTO.class, query);
		} else {
			return Collections.emptyList();
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> findValuesByColumnName(String columnName, Integer companyId) {
		String sqlString = "select distinct LOWER( " + columnName + ")"
				+ " from xt_user_list ul,xt_user_userlist uul where ul.user_list_id = uul.user_list_id and ul.company_id = :companyId and ul.is_default_partnerlist and length(TRIM("
				+ columnName + "))>0\r\n" + " order by LOWER(" + columnName + ") asc ";
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(sqlString);
		query.setParameter("companyId", companyId);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TriggerComponentDTO> findDefaultTriggerComponentsByTimePhraseAndActionAndSubjectAndFilterProperty() {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(TriggerComponent.class, "TC");
		List<TriggerComponentType> triggerComponentTypes = new ArrayList<TriggerComponentType>();
		triggerComponentTypes.add(TriggerComponentType.ACTION);
		triggerComponentTypes.add(TriggerComponentType.TIME_PHRASE);
		triggerComponentTypes.add(TriggerComponentType.SUBJECT);
		triggerComponentTypes.add(TriggerComponentType.FILTER_PROPERTY);
		criteria.setProjection(Projections.distinct(Projections.projectionList()
				.add(Projections.property("TC.id"), "id").add(Projections.property("TC.key"), "key")
				.add(Projections.property("TC.type"), "type").add(Projections.property("TC.value"), "value")));
		criteria.add(Restrictions.in("TC.type", triggerComponentTypes));
		criteria.addOrder(Order.asc("TC.id"));
		criteria.setResultTransformer(Transformers.aliasToBean(TriggerComponentDTO.class));
		return criteria.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Workflow> getAllActiveWorkflows() {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(Workflow.class, "W");
		// criteria.add(Restrictions.eq("WT.workflow.id", workflowId));
		return (List<Workflow>) criteria.list();
	}

	private String replaceTimePhrases(Workflow workflow, String queryString) {
		String minTimePhrase = "";
		String maxTimePhrase = "";
		TriggerComponent timePhraseTriggerComponent = workflow.getTriggerTimePhrase();
		if (timePhraseTriggerComponent != null) {
			TriggerTimePhraseEnum triggerTimePhrase = TriggerTimePhraseEnum
					.valueOf(timePhraseTriggerComponent.getKey());
			if (triggerTimePhrase.equals(TriggerTimePhraseEnum.custom)) {
				minTimePhrase = workflow.getCustomTriggerDays() + " days";
				maxTimePhrase = workflow.getCustomTriggerDays() - 1 + " days";
			} else {
				minTimePhrase = triggerTimePhrase.getValue() + " days";
				maxTimePhrase = triggerTimePhrase.getValue() - 1 + " days";
			}
		}
		queryString = queryString.replace(":minTimePhrase", "current_date -interval '" + minTimePhrase + "'");
		queryString = queryString.replace(":maxTimePhrase", "current_date -interval '" + maxTimePhrase + "'");
		return queryString;
	}

	private String getFilterQuery(String filterQueryJson) {
		String query = "";
		if (StringUtils.hasText(filterQueryJson)) {
			JSONObject json = new JSONObject(filterQueryJson);
			String filterQuery = buildQuery(json);
			if (StringUtils.hasText(filterQuery)) {
				query = SPACE + " and " + SPACE + filterQuery + SPACE;
			}
		}
		return query;
	}

	private String buildQuery(JSONObject json) {
		String query = "";
		if (json != null) {
			String condition = json.getString("condition");
			JSONArray rules = json.getJSONArray("rules");
			if (rules != null && rules.length() > 0) {
				query = "(";
				Iterator<Object> iterator = rules.iterator();
				while (iterator.hasNext()) {
					JSONObject rule = (JSONObject) iterator.next();
					if (rule != null) {
						if (rule.has("condition") && rule.has("rules")) {
							query += buildQuery(rule);
						} else {
							query += buildFilter(rule);
						}

						if (iterator.hasNext()) {
							query += SPACE + condition + SPACE;
						}
					}
				}
				query += ")";
			}
		}
		return query;
	}

	private String buildFilter(JSONObject rule) {
		String filter = "";
		String field = userTableAlias + rule.getString("field");
		String operator = rule.getString("operator");
		Object valueObj = rule.has("value") ? rule.get("value") : null;

		if (StringUtils.hasText(operator) && valueObj != null) {
			filter = field + SPACE + operator + SPACE;
			if (inOperators.contains(operator)) {
				filter += buildValueArrayString(valueObj);
			} else {
				String value = (String) valueObj;
				filter += "'" + value + "'";
			}
		}
		return filter;
	}

	private String buildValueArrayString(Object valueArrayObj) {
		String valueArrayString = "";
		JSONArray valueArray = (JSONArray) valueArrayObj;
		if (valueArray != null) {
			valueArrayString = "(";
			Iterator<Object> iterator = valueArray.iterator();
			while (iterator.hasNext()) {
				String value = (String) iterator.next();
				if (value != null) {
					valueArrayString += "'" + value + "'";
					if (iterator.hasNext()) {
						valueArrayString += "," + SPACE;
					}
				}
			}
			valueArrayString += ")";
		}
		return valueArrayString;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findIdsByType(TriggerComponentType triggerComponentType) {
		Session session = sessionFactory.getCurrentSession();
		String hqlString = "select distinct id from TriggerComponent where type=:type";
		Query query = session.createQuery(hqlString);
		query.setParameter("type", triggerComponentType);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> findAllTriggerTitlesByUserId(Integer userId) {
		List<String> titles = new ArrayList<>();
		if (userId != null && userId > 0) {
			Session session = sessionFactory.getCurrentSession();
			Integer companyId = utilDao.getCompanyIdByUserId(userId, session);
			if (companyId != null && companyId > 0) {
				String hqlString = "select LOWER(TRIM(title)) from Workflow where company.id=:companyId";
				Query query = session.createQuery(hqlString);
				query.setParameter("companyId", companyId);
				List<String> data = query.list();
				if (data != null && !data.isEmpty()) {
					titles.addAll(data);
				}
			}
		}
		return titles;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> findAllTriggerTitlesByUserIdAndExcludeTitleById(Integer userId, Integer workflowId) {
		List<String> titles = new ArrayList<>();
		if (userId != null && userId > 0 && workflowId != null && workflowId > 0) {
			Session session = sessionFactory.getCurrentSession();
			Integer companyId = utilDao.getCompanyIdByUserId(userId, session);
			if (companyId != null && companyId > 0) {
				String hqlString = "select LOWER(TRIM(title)) from Workflow where company.id=:companyId and id!=:workflowId";
				Query query = session.createQuery(hqlString);
				query.setParameter("companyId", companyId);
				query.setParameter("workflowId", workflowId);
				List<String> data = query.list();
				if (data != null && !data.isEmpty()) {
					titles.addAll(data);
				}
			}
		}
		return titles;
	}

	@Override
	public Integer findCustomDaysActionId() {
		Session session = sessionFactory.getCurrentSession();
		String keyAndValue = "custom";
		String hqlString = "select id from TriggerComponent where type=:type and key=:key";
		Query query = session.createQuery(hqlString);
		query.setParameter("type", TriggerComponentType.TIME_PHRASE);
		query.setParameter("key", keyAndValue);
		return (Integer) query.uniqueResult();
	}

	@Override
	public Map<String, Object> findAll(Pagination pagination, String searchKey) {
		Map<String, Object> map = new HashMap<>();
		boolean hasSearchKey = searchKey != null && StringUtils.hasText(searchKey);
		StringBuilder findAllWorkflowsQueryString = new StringBuilder();
		String sortQueryString = addSortColumns(pagination);
		String queryString = "select wf.id as  \"id\",wf.title as \"title\", ts.value as \"subjectValue\",ta.value as \"ActionValue\",tt.value as \"timePhraseValue\",\r\n"
				+ " wf.custom_trigger_days as \"customTriggerDays\",case when length(TRIM(concat(up.firstname,'',up.lastname)))>0 then TRIM(concat(up.firstname,' ',up.lastname)) else up.email_id end "
				+ " as \"createdBy\",wf.created_time as \"createdTime\", UPPER(cast(wf.status as text)) as \"status\",tt.key as \"timePhraseKey\" from xt_workflow wf,xt_trigger_component ts,xt_trigger_component ta,xt_trigger_component tt,xt_user_profile up \r\n"
				+ " where wf.trigger_subject_id = ts.id and ta.id = wf.trigger_action_id and tt.id = wf.trigger_time_phrase_id\r\n"
				+ " and up.user_id = wf.created_by and wf.company_id = :companyId \r\n";
		String searchWorkflowsQueryString = " and (LOWER(up.email_id) like LOWER('%searchKey%') OR LOWER(up.firstname) like LOWER('%searchKey%') OR LOWER(up.lastname) like LOWER('%searchKey%')"
				+ " OR LOWER(wf.title) like LOWER('%searchKey%') OR LOWER(ts.value) like LOWER('%searchKey%') OR LOWER(ta.value) like LOWER('%searchKey%') OR "
				+ " LOWER(tt.value) like LOWER('%searchKey%') OR  UPPER(cast(wf.status as text)) like UPPER('%searchKey%') OR CAST(wf.custom_trigger_days AS TEXT) LIKE  ('%searchKey%'))";
		findAllWorkflowsQueryString.append(queryString);
		if (hasSearchKey) {
			searchKey = XamplifyUtils.escapeSingleQuotesForSearchQuery(searchKey);
			searchKey = XamplifyUtils.addBackSlashToSpecialCharacters(searchKey);
			findAllWorkflowsQueryString.append(searchWorkflowsQueryString.replace("searchKey", searchKey))
					.append(sortQueryString);
		} else {
			findAllWorkflowsQueryString.append(sortQueryString);
		}
		Session session = sessionFactory.getCurrentSession();
		SQLQuery query = session.createSQLQuery(String.valueOf(findAllWorkflowsQueryString));
		query.setParameter("companyId", pagination.getCompanyId());
		return paginationUtil.setScrollableAndGetList(pagination, map, query, WorkflowListResponseDTO.class);

	}

	private String addSortColumns(Pagination pagination) {
		List<SortColumnDTO> sortColumnDTOs = new ArrayList<>();
		SortColumnDTO createdTimeSortOption = new SortColumnDTO(createdTimePropertyName, "wf.created_time", true, true,
				false);
		SortColumnDTO titleSortOption = new SortColumnDTO(partnerJourneyWorkflowTitlePropertyName, "wf.title", false,
				true, false);
		sortColumnDTOs.add(createdTimeSortOption);
		sortColumnDTOs.add(titleSortOption);
		return paginationUtil.generateSortQuery(pagination, sortColumnDTOs, "desc");
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findIdsByCompanyId(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(Workflow.class);
		criteria.add(Restrictions.eq("company.id", companyId));
		List<String> propertyNames = new ArrayList<>();
		propertyNames.add("id");
		return (List<Integer>) hibernateUtilDao.getProjectionList(criteria, propertyNames);
	}

	@Override
	public void delete(Integer id) {
		Session session = sessionFactory.getCurrentSession();
		String hqlString = "delete  from Workflow where id=:id";
		Query query = session.createQuery(hqlString);
		query.setParameter("id", id);
		query.executeUpdate();
	}

	@Override
	public WorkflowResponseDTO findById(Integer id) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(Workflow.class, "workflow")
				.createAlias("workflow.triggerSubject", "triggerSubject")
				.createAlias("workflow.triggerAction", "triggerAction")
				.createAlias("workflow.triggerTimePhrase", "triggerTimePhrase")
				.createAlias("workflow.fromEmailUser", "fromEmailUser")
				.createAlias("workflow.learningTrack", "learningTrack", JoinType.LEFT_OUTER_JOIN)
				.createAlias("workflow.template", "template", JoinType.LEFT_OUTER_JOIN);
		criteria.add(Restrictions.eq("workflow.id", id)).setProjection(Projections.distinct(Projections.projectionList()
				.add(Projections.property("workflow.title"), "title").add(Projections.property("workflow.id"), "id")
				.add(Projections.property("triggerSubject.id"), "subjectId")
				.add(Projections.property("triggerAction.id"), "actionId")
				.add(Projections.property("triggerTimePhrase.id"), "timePhraseId")
				.add(Projections.property("workflow.filterQueryJson"), "queryBuilderInputString")
				.add(Projections.property("workflow.notificationSubject"), "notificationSubject")
				.add(Projections.property("workflow.preHeader"), "preHeader")
				.add(Projections.property("fromEmailUser.userId"), "fromEmailUserId")
				.add(Projections.property("template.id"), "templateId")
				.add(Projections.property("workflow.customTriggerDays"), "customDays")
				.add(Projections.property("workflow.notificationMessage"), "notificationMessage")
				.add(Projections.property("learningTrack.id"), "learningTrackId")
				));
		WorkflowResponseDTO workflowResponseDTO = (WorkflowResponseDTO) criteria
				.setResultTransformer(Transformers.aliasToBean(WorkflowResponseDTO.class)).uniqueResult();
		setDefaultValuesAndPartnerListIds(id, session, workflowResponseDTO);
		return workflowResponseDTO;
	}

	private void setDefaultValuesAndPartnerListIds(Integer id, Session session,
			WorkflowResponseDTO workflowResponseDTO) {
		if (workflowResponseDTO != null) {
			Integer templateId = workflowResponseDTO.getTemplateId();
			if (templateId == null) {
				workflowResponseDTO.setTemplateId(0);
			}
			workflowResponseDTO.setCustomTemplateSelected(
					workflowResponseDTO.getTemplateId() != null && workflowResponseDTO.getTemplateId() > 0);
			if (workflowResponseDTO.getSelectedPartnerListIds() == null) {
				workflowResponseDTO.setSelectedPartnerListIds(Collections.emptySet());
			}
			if (workflowResponseDTO.getCustomDays() == null) {
				workflowResponseDTO.setCustomDays(0);
			}
			getSelectedPartnerListIds(id, session, workflowResponseDTO);
			getSelectedPartnerIds(id, session, workflowResponseDTO);
			if(workflowResponseDTO.getLearningTrackId() != null) {
				getPartnerShipIdByPlaybookId(workflowResponseDTO);							
			}
		}
	}



	@SuppressWarnings("unchecked")
	private void getSelectedPartnerListIds(Integer id, Session session, WorkflowResponseDTO workflowResponseDTO) {
		String partnerListIdsHQLString = "select userList.id from WorkflowUserlist where workflow.id=:id";
		Query query = session.createQuery(partnerListIdsHQLString);
		query.setParameter("id", id);
		List<Integer> partnerListIds = query.list();
		if (partnerListIds != null && !partnerListIds.isEmpty()) {
			Set<Integer> selectedPartnerListIds = XamplifyUtils.convertListToSetElements(partnerListIds);
			workflowResponseDTO.setSelectedPartnerListIds(selectedPartnerListIds);

		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findSelectedPartnerGroupIdsByWorkflowId(Integer workflowId, boolean isPartnerGroupSelected) {
		try {
			if (workflowId != null && workflowId > 0) {
				Session session = sessionFactory.getCurrentSession();
				String sql = "";
				if(isPartnerGroupSelected) {
					sql = "select distinct wful.user_list_id from xt_workflow_userlist wful, xt_workflow wf where wf.id = wful.workflow_id and wf.id=:workflowId";					
				}else {
					sql = "select distinct wfp.partner_id from xt_workflow_partner wfp, xt_workflow wf where wf.id = wfp.workflow_id and wf.id=:workflowId";							
				}
				Query query = session.createSQLQuery(sql);
				query.setParameter("workflowId", workflowId);
				if (query.list() != null && !query.list().isEmpty()) {
					return query.list();
				} else {
					return Collections.emptyList();
				}
			} else {
				return Collections.emptyList();
			}

		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	@Override
	public void deleteWorkflowUserLists(List<Integer> userListIds) {
		if (userListIds != null && !userListIds.isEmpty()) {
			Session session = sessionFactory.getCurrentSession();
			String hqlString = "delete  from WorkflowUserlist where userList.id in (:userListIds)";
			Query query = session.createQuery(hqlString);
			query.setParameterList("userListIds", userListIds);
			query.executeUpdate();
		}

	}

	@Override
	public void saveWorkflowUserLists(List<Integer> userListIds, Workflow workflow) {
		Session session = sessionFactory.getCurrentSession();
		for (int i = 0; i < userListIds.size(); i++) {
			Integer partnerListId = userListIds.get(i);
			WorkflowUserlist workflowUserlist = new WorkflowUserlist();
			workflowUserlist.setWorkflow(workflow);
			UserList userList = new UserList();
			userList.setId(partnerListId);
			workflowUserlist.setUserList(userList);
			workflowUserlist.setCreatedBy(workflow.getCreatedBy());
			workflowUserlist.setCreatedTime(new Date());
			workflowUserlist.setUpdatedBy(workflow.getCreatedBy());
			workflowUserlist.setUpdatedTime(new Date());
			workflowUserlist.setStatus(WorkflowsStatusEnum.ACTIVE);
			session.save(workflowUserlist);
			if (i % 30 == 0) {
				session.flush();
				session.clear();
			}
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> listNotRedistributedCampaignUsers(Workflow workflow) {
		List<Integer> userlistIds = new ArrayList<>();
		for (WorkflowUserlist workflowUserList : workflow.getUserlists()) {
			userlistIds.add(workflowUserList.getUserList().getId());
		}
		String queryString = notRedistributedCampaignUsersQuery + getFilterQuery(workflow.getFilterQueryJson())
				+ notRedistributedCampaignUsersGroupBy;
		queryString = replaceTimePhrases(workflow, queryString);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", workflow.getCompany().getId());
		query.setParameterList("userlistIds", userlistIds);
		return (List<User>) paginationUtil.getListDTO(User.class, query);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> listRedistributedCampaignUsers(Workflow workflow) {
		List<Integer> userlistIds = new ArrayList<>();
		for (WorkflowUserlist workflowUserList : workflow.getUserlists()) {
			userlistIds.add(workflowUserList.getUserList().getId());
		}
		String queryString = redistributedCampaignUsersQuery + getFilterQuery(workflow.getFilterQueryJson())
				+ redistributedCampaignUsersGroupBy;
		queryString = replaceTimePhrases(workflow, queryString);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", workflow.getCompany().getId());
		query.setParameterList("userlistIds", userlistIds);
		return (List<User>) paginationUtil.getListDTO(User.class, query);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> listNotSignedUpUsers(Workflow workflow) {
		List<Integer> userlistIds = new ArrayList<>();
		for (WorkflowUserlist workflowUserList : workflow.getUserlists()) {
			userlistIds.add(workflowUserList.getUserList().getId());
		}
		String queryString = notSignedUpUsersQuery + getFilterQuery(workflow.getFilterQueryJson());
		queryString = replaceTimePhrases(workflow, queryString);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", workflow.getCompany().getId());
		query.setParameterList("userlistIds", userlistIds);
		return (List<User>) paginationUtil.getListDTO(User.class, query);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> listSignedUpUsers(Workflow workflow) {
		List<Integer> userlistIds = new ArrayList<>();
		for (WorkflowUserlist workflowUserList : workflow.getUserlists()) {
			userlistIds.add(workflowUserList.getUserList().getId());
		}
		String queryString = signedUpUsersQuery + getFilterQuery(workflow.getFilterQueryJson());
		queryString = replaceTimePhrases(workflow, queryString);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", workflow.getCompany().getId());
		query.setParameterList("userlistIds", userlistIds);
		return (List<User>) paginationUtil.getListDTO(User.class, query);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> listNotActivatedUsers(Workflow workflow) {
		List<Integer> userlistIds = new ArrayList<>();
		for (WorkflowUserlist workflowUserList : workflow.getUserlists()) {
			userlistIds.add(workflowUserList.getUserList().getId());
		}
		String queryString = notActivatedUsersQuery + getFilterQuery(workflow.getFilterQueryJson());
		queryString = replaceTimePhrases(workflow, queryString);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", workflow.getCompany().getId());
		query.setParameterList("userlistIds", userlistIds);
		return (List<User>) paginationUtil.getListDTO(User.class, query);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> listActivatedUsers(Workflow workflow) {
		List<Integer> userlistIds = new ArrayList<>();
		for (WorkflowUserlist workflowUserList : workflow.getUserlists()) {
			userlistIds.add(workflowUserList.getUserList().getId());
		}
		String queryString = activtedUsersQuery + getFilterQuery(workflow.getFilterQueryJson());
		queryString = replaceTimePhrases(workflow, queryString);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", workflow.getCompany().getId());
		query.setParameterList("userlistIds", userlistIds);
		return (List<User>) paginationUtil.getListDTO(User.class, query);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> listNotCreatedCompanyProfileUsers(Workflow workflow) {
		List<Integer> userlistIds = new ArrayList<>();
		for (WorkflowUserlist workflowUserList : workflow.getUserlists()) {
			userlistIds.add(workflowUserList.getUserList().getId());
		}
		String queryString = createdCompanyProfileUsersQuery + getFilterQuery(workflow.getFilterQueryJson());
		queryString = replaceTimePhrases(workflow, queryString);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", workflow.getCompany().getId());
		query.setParameterList("userlistIds", userlistIds);
		return (List<User>) paginationUtil.getListDTO(User.class, query);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> listCreatedCompanyProfileUsers(Workflow workflow) {
		List<Integer> userlistIds = new ArrayList<>();
		for (WorkflowUserlist workflowUserList : workflow.getUserlists()) {
			userlistIds.add(workflowUserList.getUserList().getId());
		}
		String queryString = notCreatedCompanyProfileUsersQuery + getFilterQuery(workflow.getFilterQueryJson());
		queryString = replaceTimePhrases(workflow, queryString);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", workflow.getCompany().getId());
		query.setParameterList("userlistIds", userlistIds);
		return (List<User>) paginationUtil.getListDTO(User.class, query);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> listAddedTeamMemberUsers(Workflow workflow) {
		List<Integer> userlistIds = new ArrayList<>();
		for (WorkflowUserlist workflowUserList : workflow.getUserlists()) {
			userlistIds.add(workflowUserList.getUserList().getId());
		}
		String queryString = addedTeamMemberUsersQuery + getFilterQuery(workflow.getFilterQueryJson())
				+ addedTeamMemberUsersQueryGroupBy;
		queryString = replaceTimePhrases(workflow, queryString);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", workflow.getCompany().getId());
		query.setParameterList("userlistIds", userlistIds);
		return (List<User>) paginationUtil.getListDTO(User.class, query);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> listNotAddedTeamMemberUsers(Workflow workflow) {
		List<Integer> userlistIds = new ArrayList<>();
		for (WorkflowUserlist workflowUserList : workflow.getUserlists()) {
			userlistIds.add(workflowUserList.getUserList().getId());
		}
		String queryString = notAddedTeamMemberUsersQuery + getFilterQuery(workflow.getFilterQueryJson())
				+ notAddedTeamMemberUsersQueryGroupBy;
		queryString = replaceTimePhrases(workflow, queryString);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", workflow.getCompany().getId());
		query.setParameterList("userlistIds", userlistIds);
		return (List<User>) paginationUtil.getListDTO(User.class, query);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> listCreatedDealUsers(Workflow workflow) {
		List<Integer> userlistIds = new ArrayList<>();
		for (WorkflowUserlist workflowUserList : workflow.getUserlists()) {
			userlistIds.add(workflowUserList.getUserList().getId());
		}
		String queryString = createdDealUsersQuery + getFilterQuery(workflow.getFilterQueryJson())
				+ createdDealUsersQueryGroupBy;
		queryString = replaceTimePhrases(workflow, queryString);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", workflow.getCompany().getId());
		query.setParameterList("userlistIds", userlistIds);
		return (List<User>) paginationUtil.getListDTO(User.class, query);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> listNotCreatedDealUsers(Workflow workflow) {
		List<Integer> userlistIds = new ArrayList<>();
		for (WorkflowUserlist workflowUserList : workflow.getUserlists()) {
			userlistIds.add(workflowUserList.getUserList().getId());
		}
		String queryString = notCreatedDealUsersQuery + getFilterQuery(workflow.getFilterQueryJson())
				+ notCreatedDealUsersQueryGroupBy;
		queryString = replaceTimePhrases(workflow, queryString);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", workflow.getCompany().getId());
		query.setParameterList("userlistIds", userlistIds);
		return (List<User>) paginationUtil.getListDTO(User.class, query);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> listCreatedLeadUsers(Workflow workflow) {
		List<Integer> userlistIds = new ArrayList<>();
		for (WorkflowUserlist workflowUserList : workflow.getUserlists()) {
			userlistIds.add(workflowUserList.getUserList().getId());
		}
		String queryString = createdLeadUsersQuery + getFilterQuery(workflow.getFilterQueryJson())
				+ createdLeadUsersQueryGroupBy;
		queryString = replaceTimePhrases(workflow, queryString);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", workflow.getCompany().getId());
		query.setParameterList("userlistIds", userlistIds);
		return (List<User>) paginationUtil.getListDTO(User.class, query);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> listNotCreatedLeadUsers(Workflow workflow) {
		List<Integer> userlistIds = new ArrayList<>();
		for (WorkflowUserlist workflowUserList : workflow.getUserlists()) {
			userlistIds.add(workflowUserList.getUserList().getId());
		}
		String queryString = notCreatedLeadUsersQuery + getFilterQuery(workflow.getFilterQueryJson())
				+ notCreatedLeadUsersQueryGroupBy;
		queryString = replaceTimePhrases(workflow, queryString);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", workflow.getCompany().getId());
		query.setParameterList("userlistIds", userlistIds);
		return (List<User>) paginationUtil.getListDTO(User.class, query);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> listConvertedLeadUsers(Workflow workflow) {
		List<Integer> userlistIds = new ArrayList<>();
		for (WorkflowUserlist workflowUserList : workflow.getUserlists()) {
			userlistIds.add(workflowUserList.getUserList().getId());
		}
		String queryString = convertedLeadUsersQuery + getFilterQuery(workflow.getFilterQueryJson())
				+ convertedLeadUsersQueryGroupBy;
		queryString = replaceTimePhrases(workflow, queryString);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", workflow.getCompany().getId());
		query.setParameterList("userlistIds", userlistIds);
		return (List<User>) paginationUtil.getListDTO(User.class, query);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> listNotConvertedLeadUsers(Workflow workflow) {
		List<Integer> userlistIds = new ArrayList<>();
		for (WorkflowUserlist workflowUserList : workflow.getUserlists()) {
			userlistIds.add(workflowUserList.getUserList().getId());
		}
		String queryString = notConvertedLeadUsersQuery + getFilterQuery(workflow.getFilterQueryJson())
				+ notConvertedLeadUsersQueryGroupBy;
		queryString = replaceTimePhrases(workflow, queryString);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", workflow.getCompany().getId());
		query.setParameterList("userlistIds", userlistIds);
		return (List<User>) paginationUtil.getListDTO(User.class, query);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> listClosedDealUsers(Workflow workflow) {
		List<Integer> userlistIds = new ArrayList<>();
		for (WorkflowUserlist workflowUserList : workflow.getUserlists()) {
			userlistIds.add(workflowUserList.getUserList().getId());
		}
		String queryString = closedDealUsersQuery + getFilterQuery(workflow.getFilterQueryJson())
				+ closedDealUsersQueryGroupBy;
		queryString = replaceTimePhrases(workflow, queryString);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", workflow.getCompany().getId());
		query.setParameterList("userlistIds", userlistIds);
		return (List<User>) paginationUtil.getListDTO(User.class, query);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> listNotClosedDealUsers(Workflow workflow) {
		List<Integer> userlistIds = new ArrayList<>();
		for (WorkflowUserlist workflowUserList : workflow.getUserlists()) {
			userlistIds.add(workflowUserList.getUserList().getId());
		}
		String queryString = notClosedDealQuery + getFilterQuery(workflow.getFilterQueryJson())
				+ notClosedDealUsersQueryGroupBy;
		queryString = replaceTimePhrases(workflow, queryString);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", workflow.getCompany().getId());
		query.setParameterList("userlistIds", userlistIds);
		return (List<User>) paginationUtil.getListDTO(User.class, query);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> listCompletedPlaybookUsers(Workflow workflow) {
		List<Integer> userlistIds = new ArrayList<>();
		List<Integer> userIds = new ArrayList<>();
		boolean isPlaybooExists = workflow.getLearningTrack() != null && workflow.getLearningTrack().getId() != null;
		String selectQuery = workflow.isPartnerGroupSelected()? completedPlaybookUsersQuery :completedPlaybookUsersQueryForCompanies;
		if(isPlaybooExists) {
			selectQuery = selectQuery + SPACE+ playbookIdCondition+ SPACE;
		}
		if(workflow.isPartnerGroupSelected()) {
			for (WorkflowUserlist workflowUserList : workflow.getUserlists()) {
				userlistIds.add(workflowUserList.getUserList().getId());				
			}
		}else {
			for (WorkflowPartner workflowUser: workflow.getPartners()) {
				userIds.add(workflowUser.getPartner().getUserId());				
			}
		}
		String queryString = selectQuery + getFilterQuery(workflow.getFilterQueryJson());
		queryString = replaceTimePhrases(workflow, queryString);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		if(workflow.isPartnerGroupSelected()) {
			query.setParameter("vendorCompanyId", workflow.getCompany().getId());
			query.setParameterList("userlistIds", userlistIds);
			
		}else {
			query.setParameterList("userIds", userIds);			
		}
		if(isPlaybooExists) {
			query.setParameter("playbookId", workflow.getLearningTrack().getId());		
		}
		return (List<User>) paginationUtil.getListDTO(User.class, query);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> listNotCompletedPlaybookUsers(Workflow workflow) {
		List<Integer> userlistIds = new ArrayList<>();
		List<Integer> userIds = new ArrayList<>();
		boolean isPlaybooExists = workflow.getLearningTrack() != null && workflow.getLearningTrack().getId() != null;
		String selectQuery = workflow.isPartnerGroupSelected()? notCompletedPlaybookUsersQuery :notCompletedPlaybookUsersQueryForCompanies;
		selectQuery = selectQuery + SPACE+playbookExpiryDateConditionQuery;
		if(isPlaybooExists) {
			selectQuery = selectQuery + SPACE+ playbookIdCondition+ SPACE;
		}
		if(workflow.isPartnerGroupSelected()) {
			for (WorkflowUserlist workflowUserList : workflow.getUserlists()) {
				userlistIds.add(workflowUserList.getUserList().getId());				
			}
		}else {
			for (WorkflowPartner workflowUser: workflow.getPartners()) {
				userIds.add(workflowUser.getPartner().getUserId());				
			}
		}
		String queryString = selectQuery + getFilterQuery(workflow.getFilterQueryJson());
		queryString = replaceTimePhrases(workflow, queryString);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		if(workflow.isPartnerGroupSelected()) {
			query.setParameter("vendorCompanyId", workflow.getCompany().getId());
			query.setParameterList("userlistIds", userlistIds);
			
		}else {
			query.setParameterList("userIds", userIds);			
		}
		if(isPlaybooExists) {
			query.setParameter("playbookId", workflow.getLearningTrack().getId());		
		}
		return (List<User>) paginationUtil.getListDTO(User.class, query);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> listCompletedTrackUsers(Workflow workflow) {
		List<Integer> userlistIds = new ArrayList<>();
		for (WorkflowUserlist workflowUserList : workflow.getUserlists()) {
			userlistIds.add(workflowUserList.getUserList().getId());
		}
		String queryString = completedTrackUsersQuery + getFilterQuery(workflow.getFilterQueryJson());
		queryString = replaceTimePhrases(workflow, queryString);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", workflow.getCompany().getId());
		query.setParameterList("userlistIds", userlistIds);
		return (List<User>) paginationUtil.getListDTO(User.class, query);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> listNotCompletedTrackUsers(Workflow workflow) {
		List<Integer> userlistIds = new ArrayList<>();
		for (WorkflowUserlist workflowUserList : workflow.getUserlists()) {
			userlistIds.add(workflowUserList.getUserList().getId());
		}
		String queryString = notCompletedTrackUsersQuery + getFilterQuery(workflow.getFilterQueryJson());
		queryString = replaceTimePhrases(workflow, queryString);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", workflow.getCompany().getId());
		query.setParameterList("userlistIds", userlistIds);
		return (List<User>) paginationUtil.getListDTO(User.class, query);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> listAddedContactUsers(Workflow workflow) {
		List<Integer> userlistIds = new ArrayList<>();
		for (WorkflowUserlist workflowUserList : workflow.getUserlists()) {
			userlistIds.add(workflowUserList.getUserList().getId());
		}
		String queryString = addedContactUsersQuery + getFilterQuery(workflow.getFilterQueryJson())
				+ addedContactUsersQueryGroupBy;
		queryString = replaceTimePhrases(workflow, queryString);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", workflow.getCompany().getId());
		query.setParameterList("userlistIds", userlistIds);
		return (List<User>) paginationUtil.getListDTO(User.class, query);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> listNotAddedContactUsers(Workflow workflow) {
		List<Integer> userlistIds = new ArrayList<>();
		for (WorkflowUserlist workflowUserList : workflow.getUserlists()) {
			userlistIds.add(workflowUserList.getUserList().getId());
		}
		String queryString = notAddedContactUsersQuery + getFilterQuery(workflow.getFilterQueryJson())
				+ notAddedContactUsersQueryUsersQuery;
		queryString = replaceTimePhrases(workflow, queryString);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", workflow.getCompany().getId());
		query.setParameterList("userlistIds", userlistIds);
		return (List<User>) paginationUtil.getListDTO(User.class, query);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> listRequestedMdfUsers(Workflow workflow) {
		List<Integer> userlistIds = new ArrayList<>();
		for (WorkflowUserlist workflowUserList : workflow.getUserlists()) {
			userlistIds.add(workflowUserList.getUserList().getId());
		}
		String queryString = requestedMdfUsersQuery + getFilterQuery(workflow.getFilterQueryJson())
				+ requestedMdfUsersQueryGroupBy;
		queryString = replaceTimePhrases(workflow, queryString);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", workflow.getCompany().getId());
		query.setParameterList("userlistIds", userlistIds);
		return (List<User>) paginationUtil.getListDTO(User.class, query);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> listNotRequestedMdfUsers(Workflow workflow) {
		List<Integer> userlistIds = new ArrayList<>();
		for (WorkflowUserlist workflowUserList : workflow.getUserlists()) {
			userlistIds.add(workflowUserList.getUserList().getId());
		}
		String queryString = notRequestedMdfUsersQuery + getFilterQuery(workflow.getFilterQueryJson())
				+ notRequestedMdfUsersQueryGroupBy;
		queryString = replaceTimePhrases(workflow, queryString);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", workflow.getCompany().getId());
		query.setParameterList("userlistIds", userlistIds);
		return (List<User>) paginationUtil.getListDTO(User.class, query);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> listViewedPagesUsers(Workflow workflow) {
		List<Integer> userlistIds = new ArrayList<>();
		for (WorkflowUserlist workflowUserList : workflow.getUserlists()) {
			userlistIds.add(workflowUserList.getUserList().getId());
		}
		String queryString = viewedPagesUsersQuery + getFilterQuery(workflow.getFilterQueryJson())
				+ viewedPagesUsersQueryGroupBy;
		queryString = replaceTimePhrases(workflow, queryString);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", workflow.getCompany().getId());
		query.setParameterList("userlistIds", userlistIds);
		return (List<User>) paginationUtil.getListDTO(User.class, query);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> listNotViewedPagesUsers(Workflow workflow) {
		List<Integer> userlistIds = new ArrayList<>();
		for (WorkflowUserlist workflowUserList : workflow.getUserlists()) {
			userlistIds.add(workflowUserList.getUserList().getId());
		}
		String queryString = notViewedPagesUsersQuery + getFilterQuery(workflow.getFilterQueryJson())
				+ notViewedPagesUsersQueryGroupBy;
		queryString = replaceTimePhrases(workflow, queryString);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", workflow.getCompany().getId());
		query.setParameterList("userlistIds", userlistIds);
		return (List<User>) paginationUtil.getListDTO(User.class, query);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> listViewedPlaybookUsers(Workflow workflow) {
		List<Integer> userlistIds = new ArrayList<>();
		List<Integer> userIds = new ArrayList<>();
		boolean isPlaybooExists = workflow.getLearningTrack() != null && workflow.getLearningTrack().getId() != null;
		String selectQuery = workflow.isPartnerGroupSelected()? viewedPlaybookUsersQuery :viewedPlaybookUsersQueryForCompanies;
		selectQuery = selectQuery + SPACE+playbookExpiryDateConditionQuery;

		if(isPlaybooExists) {
			selectQuery = selectQuery + SPACE+ playbookIdCondition + SPACE;
		}
		if(workflow.isPartnerGroupSelected()) {
			for (WorkflowUserlist workflowUserList : workflow.getUserlists()) {
				userlistIds.add(workflowUserList.getUserList().getId());				
			}
		}else {
			for (WorkflowPartner workflowUser: workflow.getPartners()) {
				userIds.add(workflowUser.getPartner().getUserId());				
			}
			
		}
		String queryString = selectQuery + getFilterQuery(workflow.getFilterQueryJson())
				+ viewedPlaybookUsersQueryGroupBy;
		queryString = replaceTimePhrases(workflow, queryString);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		if(workflow.isPartnerGroupSelected()) {
			query.setParameter("vendorCompanyId", workflow.getCompany().getId());
			query.setParameterList("userlistIds", userlistIds);
			
		}else {
			query.setParameterList("userIds", userIds);			
		}
		if(isPlaybooExists) {
			query.setParameter("playbookId", workflow.getLearningTrack().getId());		
		}
		return (List<User>) paginationUtil.getListDTO(User.class, query);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> listNotViewedPlaybookUsers(Workflow workflow) {
		List<Integer> userlistIds = new ArrayList<>();
		List<Integer> userIds = new ArrayList<>();
		boolean isPlaybooExists = workflow.getLearningTrack() != null && workflow.getLearningTrack().getId() != null;
		String selectQuery = workflow.isPartnerGroupSelected()? notViewedPlaybookUsersQuery :notViewedPlaybookUsersQueryForCompanies;
		selectQuery = selectQuery + SPACE+playbookExpiryDateConditionQuery;
		if(isPlaybooExists) {
			selectQuery = selectQuery + SPACE+ playbookIdCondition+ SPACE;
		}
		if(workflow.isPartnerGroupSelected()) {
			for (WorkflowUserlist workflowUserList : workflow.getUserlists()) {
				userlistIds.add(workflowUserList.getUserList().getId());				
			}
		}else {
			for (WorkflowPartner workflowUser: workflow.getPartners()) {
				userIds.add(workflowUser.getPartner().getUserId());				
			}
		}
		String queryString = selectQuery + getFilterQuery(workflow.getFilterQueryJson())
				+ notViewedPlaybookUsersQueryGroupBy;
		queryString = replaceTimePhrases(workflow, queryString);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		if(workflow.isPartnerGroupSelected()) {
			query.setParameter("vendorCompanyId", workflow.getCompany().getId());
			query.setParameterList("userlistIds", userlistIds);
			
		}else {
			query.setParameterList("userIds", userIds);			
		}
		if(isPlaybooExists) {
			query.setParameter("playbookId", workflow.getLearningTrack().getId());		
		}
		return (List<User>) paginationUtil.getListDTO(User.class, query);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> listViewedTrackUsers(Workflow workflow) {
		List<Integer> userlistIds = new ArrayList<>();
		for (WorkflowUserlist workflowUserList : workflow.getUserlists()) {
			userlistIds.add(workflowUserList.getUserList().getId());
		}
		String queryString = viewedTrackUsersQuery + getFilterQuery(workflow.getFilterQueryJson())
				+ viewedTrackUsersQueryGroupBy;
		queryString = replaceTimePhrases(workflow, queryString);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", workflow.getCompany().getId());
		query.setParameterList("userlistIds", userlistIds);
		return (List<User>) paginationUtil.getListDTO(User.class, query);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> listNotViewedTrackUsers(Workflow workflow) {
		List<Integer> userlistIds = new ArrayList<>();
		for (WorkflowUserlist workflowUserList : workflow.getUserlists()) {
			userlistIds.add(workflowUserList.getUserList().getId());
		}
		String queryString = notViewedTrackUsersQuery + getFilterQuery(workflow.getFilterQueryJson())
				+ notViewedTrackUsersQueryGroupBy;
		queryString = replaceTimePhrases(workflow, queryString);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", workflow.getCompany().getId());
		query.setParameterList("userlistIds", userlistIds);
		return (List<User>) paginationUtil.getListDTO(User.class, query);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> listRedistributedShareLeadUsers(Workflow workflow) {
		List<Integer> userlistIds = new ArrayList<>();
		for (WorkflowUserlist workflowUserList : workflow.getUserlists()) {
			userlistIds.add(workflowUserList.getUserList().getId());
		}
		String queryString = redistributedShareLeadUsersQuery + getFilterQuery(workflow.getFilterQueryJson())
				+ redistributedShareLeadUsersQueryGroupBy;
		queryString = replaceTimePhrases(workflow, queryString);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", workflow.getCompany().getId());
		query.setParameterList("userlistIds", userlistIds);
		return (List<User>) paginationUtil.getListDTO(User.class, query);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> listNotRedistributedShareLeadUsers(Workflow workflow) {
		List<Integer> userlistIds = new ArrayList<>();
		for (WorkflowUserlist workflowUserList : workflow.getUserlists()) {
			userlistIds.add(workflowUserList.getUserList().getId());
		}
		String queryString = notRedistributedShareLeadUsersQuery + getFilterQuery(workflow.getFilterQueryJson())
				+ notRedistributedShareLeadUsersQueryGroupBy;
		queryString = replaceTimePhrases(workflow, queryString);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("vendorCompanyId", workflow.getCompany().getId());
		query.setParameterList("userlistIds", userlistIds);
		return (List<User>) paginationUtil.getListDTO(User.class, query);
	}

	@Override
	public void deleteWorkflowPartners(List<Integer> partnerIds) {
		if (partnerIds != null && !partnerIds.isEmpty()) {
			Session session = sessionFactory.getCurrentSession();
			String hqlString = "delete  from WorkflowPartner where partner.id in (:partnerIds)";
			Query query = session.createQuery(hqlString);
			query.setParameterList("partnerIds", partnerIds);
			query.executeUpdate();
		}

	}

	@Override
	public void saveWorkflowPartners(List<Integer> partnerIds, Workflow workflow) {
		Session session = sessionFactory.getCurrentSession();
		for (int i = 0; i < partnerIds.size(); i++) {
			Integer partnerId = partnerIds.get(i);
			WorkflowPartner workflowUserlist = new WorkflowPartner();
			workflowUserlist.setWorkflow(workflow);
			User partner = new User();
			partner.setUserId(partnerId);
			workflowUserlist.setPartner(partner);
			workflowUserlist.setCreatedBy(workflow.getCreatedBy());
			workflowUserlist.setCreatedTime(new Date());
			workflowUserlist.setUpdatedBy(workflow.getCreatedBy());
			workflowUserlist.setUpdatedTime(new Date());
			workflowUserlist.setStatus(WorkflowsStatusEnum.ACTIVE);
			session.save(workflowUserlist);
			if (i % 30 == 0) {
				session.flush();
				session.clear();
			}
		}

	}

	@Override
	public void deleteWorkflowUserListsByWorkFlowId(Integer workFlowId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			String sql = " delete from xt_workflow_userlist where workflow_id =:workFlowId";
			hibernateSQLQueryResultRequestDTO.setQueryString(sql);
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
			.add(new QueryParameterDTO("workFlowId", workFlowId));  
			hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);

	}
	
	@Override
	public void deleteWorkflowByWorkFlowIds(List<Integer> workFlowIds) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String sql = "delete from xt_workflow  where id in(:ids)";
		hibernateSQLQueryResultRequestDTO.setQueryString(sql);
		hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs()
		.add(new QueryParameterListDTO("ids", workFlowIds));
		hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);
	}
	
	@Override
	public void deleteWorkflowPartnersByWorkFlowId(Integer workFlowId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String sql = "delete from xt_workflow_partner  where workflow_id =:workFlowId";
		hibernateSQLQueryResultRequestDTO.setQueryString(sql);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
		.add(new QueryParameterDTO("workFlowId", workFlowId));  
		hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);
	}
	
	//XNFR-921
	@SuppressWarnings("unchecked")
	@Override
	public List<WorkflowResponseDTO> getWorkflowsByPlaybookId(Integer playbookId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();

		String sql = " 	SELECT \r\n" + 
				"    w.title AS \"title\",\r\n" + 
				"    w.id AS \"id\",\r\n" + 
				"    w.trigger_subject_id  AS \"subjectId\",\r\n" + 
				"    w.trigger_action_id  AS \"actionId\",\r\n" + 
				"    w.trigger_time_phrase_id  AS \"timePhraseId\",\r\n" + 
				"    w.filter_query_json AS \"queryBuilderInputString\",\r\n" + 
				"    w.notification_subject AS \"notificationSubject\",\r\n" + 
				"    w.pre_header AS \"preHeader\",\r\n" + 
				"    w.from_email_user_id  AS \"fromEmailUserId\",\r\n" + 
				"    w.notification_template_id AS \"templateId\",\r\n" + 
				"    w.custom_trigger_days AS \"customDays\",\r\n" + 
				"    w.notification_message AS \"notificationMessage\"\r\n" + 
				" FROM xt_workflow  w\r\n" + 
				" WHERE w.learning_track_id  = :playbookId ";
		hibernateSQLQueryResultRequestDTO.setQueryString(sql);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
		.add(new QueryParameterDTO("playbookId", playbookId));  
		hibernateSQLQueryResultRequestDTO.setClassInstance(WorkflowResponseDTO.class);
		return (List<WorkflowResponseDTO>) hibernateSQLQueryResultUtilDao.returnDTOList(hibernateSQLQueryResultRequestDTO);
	}
	//XNFR-993
	public List<Workflow> getAllActivePlabookWorkflows() {
	    Session session = sessionFactory.getCurrentSession();
	    Criteria criteria = session.createCriteria(Workflow.class, "W");
	    criteria.add(Restrictions.isNotNull("learningTrack")); 
	    return (List<Workflow>) criteria.list();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getUserIdsByWorkflowId(Integer workflowId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();

		String sql = " select user_id from xt_workflow_email_sent_log where workflow_id = :workflowId ";
		hibernateSQLQueryResultRequestDTO.setQueryString(sql);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("workflowId", workflowId));
		return (List<Integer>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
	}
	
	@SuppressWarnings("unchecked")
	private void getSelectedPartnerIds(Integer id, Session session, WorkflowResponseDTO workflowResponseDTO) {
		String partnerIdsHQLString = "select partner.userId from WorkflowPartner where workflow.id=:id";
		Query query = session.createQuery(partnerIdsHQLString);
		query.setParameter("id", id);
		List<Integer> partnerIds = query.list();
		if (partnerIds != null && !partnerIds.isEmpty()) {
			Set<Integer> selectedPartnerIds = XamplifyUtils.convertListToSetElements(partnerIds);
			workflowResponseDTO.setSelectedPartnerIds(selectedPartnerIds);

		}
	}

	private void getPartnerShipIdByPlaybookId(WorkflowResponseDTO workflowResponseDTO) {
		Set<Integer> partnerShipIds = new HashSet<>();
		List<Object[]> rows = lmsDAO.getVisibilityByLearningTrackId(workflowResponseDTO.getLearningTrackId());
		if(XamplifyUtils.isNotEmptyList(rows)) {
			for (Object[] row : rows) {
				if (row[1] != null) {
					partnerShipIds.add(((Number) row[1]).intValue());
				}
			}
			workflowResponseDTO.setPartnerShipIds(partnerShipIds);			
		}
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public List<User> listNotViewedPlaybookUsersV2(Workflow workflow) {
		String selectQuery = notViewedPlaybookUsersQueryv2 ;
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(selectQuery);
			query.setParameter("noOfDays", workflow.getCustomTriggerDays());
			query.setParameter("playbookId", workflow.getLearningTrack().getId());		
		return (List<User>) paginationUtil.getListDTO(User.class, query);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<User> listViewedPlaybookUsersV2(Workflow workflow) {
		String selectQuery = viewedPlaybookUsersQueryv2 ;
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(selectQuery);
			query.setParameter("noOfDays", workflow.getCustomTriggerDays());
			query.setParameter("playbookId", workflow.getLearningTrack().getId());		
		return (List<User>) paginationUtil.getListDTO(User.class, query);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> listNotCompletedPlaybookUsersV2(Workflow workflow) {
		String selectQuery = notCompletedPlaybookUsersQueryv2 ;
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(selectQuery);
			query.setParameter("noOfDays", workflow.getCustomTriggerDays());
			query.setParameter("playbookId", workflow.getLearningTrack().getId());		
		return (List<User>) paginationUtil.getListDTO(User.class, query);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<User> listCompletedPlaybookUsersV2(Workflow workflow) {
		String selectQuery = completedPlaybookUsersQueryv2 ;
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(selectQuery);
			query.setParameter("noOfDays", workflow.getCustomTriggerDays());
			query.setParameter("playbookId", workflow.getLearningTrack().getId());		
		return (List<User>) paginationUtil.getListDTO(User.class, query);
	}

}



