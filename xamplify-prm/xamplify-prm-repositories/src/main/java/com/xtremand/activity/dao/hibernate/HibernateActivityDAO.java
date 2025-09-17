package com.xtremand.activity.dao.hibernate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.activity.dao.ActivityDAO;
import com.xtremand.activity.dto.ActivityDTO;
import com.xtremand.common.bom.Pagination;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.HibernateSQLQueryResultUtilDao;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.QueryParameterDTO;

@Repository
@Transactional
public class HibernateActivityDAO implements ActivityDAO {

	private static final String UNION = " union ";

	private static final String LOGGED_IN_USER_ID = "loggedInUserId";

	@Autowired
	HibernateSQLQueryResultUtilDao hibernateSQLQueryResultUtilDao;

	@Autowired
	private SessionFactory sessionFactory;

	@Value("${all.recent.activity.query}")
	private String allRecentActivityQuery;

	@Value("${all.recent.activity.count.query}")
	private String allRecentActivityCountQuery;

	@Value("${all.recent.activity.company.query}")
	private String allRecentActivityCompanyQuery;

	@Value("${all.recent.activity.count.company.query}")
	private String allRecentActivityCountCompanyQuery;

	@Value("${all.activity.campaign.query}")
	private String allActivityCampaignQuery;

	@Value("${all.activity.lead.query}")
	private String allActivityLeadQuery;

	@Value("${all.activity.deal.query}")
	private String allActivityDealQuery;

	@Value("${all.activity.note.query}")
	private String allActivityNoteQuery;

	@Value("${all.activity.email.query}")
	private String allActivityEmailQuery;

	@Value("${all.activity.task.query}")
	private String allActivityTaskQuery;

	@Override
	public Map<String, Object> fetchRecentActivities(Pagination pagination) {
		boolean isLoggedInThroughVanity = XamplifyUtils.isValidInteger(pagination.getVendorCompanyId());
		boolean isCompanyJourney = Boolean.TRUE.equals(pagination.getIsCompanyJourney());
		String allActivitiesQueryString = isCompanyJourney ? allRecentActivityCompanyQuery : allRecentActivityQuery;
		String allActivitiesCountQueryString = isCompanyJourney ? allRecentActivityCountCompanyQuery
				: allRecentActivityCountQuery;
		String vanityUrlFilterForCampaigns = "";
		String vanityUrlFilterForLeads = "";
		String vanityUrlFilterForDeals = "";
		String filterBy = "";
		String searchQuery = "";
		if (isLoggedInThroughVanity) {
			boolean isLoggedInThroughOwnVanity = pagination.getCompanyId().equals(pagination.getVendorCompanyId());
			String vanityUrlFilterForCampaignsVendorComapany = " and xc.vendor_organization_id = :vendorCompanyId ";
			vanityUrlFilterForCampaigns = isLoggedInThroughOwnVanity ? " and xc.is_nurture_campaign = 'false' "
					: vanityUrlFilterForCampaignsVendorComapany;
			vanityUrlFilterForLeads = " and xl.created_for_company_id = :vendorCompanyId ";
			vanityUrlFilterForDeals = " and xd.created_for_company_id = :vendorCompanyId ";
		}
		if (XamplifyUtils.isValidString(pagination.getSortcolumn()) && !pagination.getSortcolumn().equals("All")) {
			filterBy = " where a.\"type\" = '" + pagination.getSortcolumn() + "'";
		}
		if (XamplifyUtils.isValidString(pagination.getSearchKey())) {
			searchQuery = XamplifyUtils.isValidString(filterBy) ? " and" : " where";
			searchQuery += " a.\"name\" ilike '%" + pagination.getSearchKey() + "%'";
		}
		allActivitiesQueryString = addFilterConditions(allActivitiesQueryString, vanityUrlFilterForCampaigns,
				vanityUrlFilterForLeads, vanityUrlFilterForDeals, filterBy, searchQuery);
		allActivitiesCountQueryString = addFilterConditions(allActivitiesCountQueryString, vanityUrlFilterForCampaigns,
				vanityUrlFilterForLeads, vanityUrlFilterForDeals, filterBy, searchQuery);
		pagination.setSortcolumn(null);
		pagination.setSortingOrder(null);
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = createQueryRequestDTO(
				allActivitiesQueryString, allActivitiesCountQueryString, pagination, isLoggedInThroughVanity);
		return hibernateSQLQueryResultUtilDao.returnPaginatedDTOList(hibernateSQLQueryResultRequestDTO, pagination,
				null);
	}

	private HibernateSQLQueryResultRequestDTO createQueryRequestDTO(String allActivitiesQueryString,
			String allActivitiesCountQueryString, Pagination pagination, boolean isLoggedInThroughVanity) {
		HibernateSQLQueryResultRequestDTO queryRequest = new HibernateSQLQueryResultRequestDTO();
		queryRequest.setQueryString(allActivitiesQueryString);
		queryRequest.getQueryParameterDTOs().add(new QueryParameterDTO("userId", pagination.getContactId()));
		queryRequest.getQueryParameterDTOs().add(new QueryParameterDTO("companyId", pagination.getCompanyId()));
		if (isLoggedInThroughVanity) {
			queryRequest.getQueryParameterDTOs()
					.add(new QueryParameterDTO("vendorCompanyId", pagination.getVendorCompanyId()));
		}
		queryRequest.getQueryParameterDTOs().add(new QueryParameterDTO(LOGGED_IN_USER_ID, pagination.getUserId()));
		queryRequest.setClassInstance(ActivityDTO.class);
		queryRequest.setRowCountQueryString(allActivitiesCountQueryString);
		queryRequest.setSearchQueryString("");
		return queryRequest;
	}

	private String addFilterConditions(String queryString, String vanityUrlFilterForCampaigns,
			String vanityUrlFilterForLeads, String vanityUrlFilterForDeals, String filterBy, String searchQuery) {
		queryString = queryString.replace("{vanityUrlFilterForCampaigns}", vanityUrlFilterForCampaigns);
		queryString = queryString.replace("{vanityUrlFilterForLeads}", vanityUrlFilterForLeads);
		queryString = queryString.replace("{vanityUrlFilterForDeals}", vanityUrlFilterForDeals);
		queryString = queryString.replace("{filterBy}", filterBy);
		queryString = queryString.replace("{searchQuery}", searchQuery);
		return queryString;
	}

	@Override
	public Map<String, Object> fetchRecentActivitiesForCompanyJourney(Pagination pagination, List<Integer> userIds) {
		Map<String, Object> map = new HashMap<>();
		Session session = sessionFactory.getCurrentSession();
		String filterBy = "";
		String unionString = "";
		if (XamplifyUtils.isValidString(pagination.getSortcolumn()) && !pagination.getSortcolumn().equals("All")) {
			filterBy = " where a.\"type\" = '" + pagination.getSortcolumn() + "' ";
		} else {
			unionString = UNION;
		}
		List<ActivityDTO> activities = getAllContactsActivities(pagination, userIds, session, filterBy);
		List<ActivityDTO> list = getAllActivitiesForCompanyJourney(pagination, session, unionString, activities);
		map.put("totalRecords", activities.size());
		map.put("list", list);
		return map;
	}

	private List<ActivityDTO> getAllActivitiesForCompanyJourney(Pagination pagination, Session session,
			String unionString, List<ActivityDTO> activities) {
		List<ActivityDTO> list = null;

		List<ActivityDTO> activitiesSubList = getActivitiesSubList(pagination, activities);

		if (XamplifyUtils.isNotEmptyList(activitiesSubList)) {
			list = fetchAllPaginatedActivities(pagination, session, unionString, activitiesSubList);
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	private List<ActivityDTO> fetchAllPaginatedActivities(Pagination pagination, Session session, String unionString,
			List<ActivityDTO> activitiesSubList) {
		List<ActivityDTO> list;
		List<Integer> campaignIds = null;
		List<Integer> leadIds = null;
		List<Integer> dealIds = null;
		List<Integer> noteIds = null;
		List<Integer> emailIds = null;
		List<Integer> taskIds = null;
		campaignIds = activitiesSubList.stream().filter(activity -> "Campaign".equals(activity.getType()))
				.map(ActivityDTO::getId).collect(Collectors.toList());
		leadIds = activitiesSubList.stream().filter(activity -> "Lead".equals(activity.getType()))
				.map(ActivityDTO::getId).collect(Collectors.toList());
		dealIds = activitiesSubList.stream().filter(activity -> "Deal".equals(activity.getType()))
				.map(ActivityDTO::getId).collect(Collectors.toList());
		noteIds = activitiesSubList.stream().filter(activity -> "Note".equals(activity.getType()))
				.map(ActivityDTO::getId).collect(Collectors.toList());
		emailIds = activitiesSubList.stream().filter(activity -> "Email".equals(activity.getType()))
				.map(ActivityDTO::getId).collect(Collectors.toList());
		taskIds = activitiesSubList.stream().filter(activity -> "Task".equals(activity.getType()))
				.map(ActivityDTO::getId).collect(Collectors.toList());

		String allSQLQuery = getFinalSQLQuery(unionString, campaignIds, leadIds, dealIds, noteIds, emailIds, taskIds);

		SQLQuery query = session.createSQLQuery(allSQLQuery);
		if (XamplifyUtils.isNotEmptyList(campaignIds)) {
			query.setParameterList("campaignIds", campaignIds);
		}
		if (XamplifyUtils.isNotEmptyList(leadIds)) {
			query.setParameterList("leadIds", leadIds);
		}
		if (XamplifyUtils.isNotEmptyList(dealIds)) {
			query.setParameterList("dealIds", dealIds);
		}
		if (XamplifyUtils.isNotEmptyList(noteIds)) {
			query.setParameterList("noteIds", noteIds);
			query.setParameter(LOGGED_IN_USER_ID, pagination.getUserId());
		}
		if (XamplifyUtils.isNotEmptyList(emailIds)) {
			query.setParameterList("emailIds", emailIds);
		}
		if (XamplifyUtils.isNotEmptyList(taskIds)) {
			query.setParameterList("taskIds", taskIds);
			if (!XamplifyUtils.isNotEmptyList(noteIds)) {
				query.setParameter(LOGGED_IN_USER_ID, pagination.getUserId());
			}
		}
		list = query.setResultTransformer(Transformers.aliasToBean(ActivityDTO.class)).list();
		return list;
	}

	private String getFinalSQLQuery(String unionString, List<Integer> campaignIds, List<Integer> leadIds,
			List<Integer> dealIds, List<Integer> noteIds, List<Integer> emailIds, List<Integer> taskIds) {
		String campaignQuery = getCampaignQuery(unionString, campaignIds, leadIds, dealIds, noteIds, emailIds, taskIds);

		String leadQuery = getLeadQuery(unionString, leadIds, dealIds, noteIds, emailIds, taskIds);

		String dealQuery = getDealQuery(unionString, dealIds, noteIds, emailIds, taskIds);

		String noteQuery = getNoteQuery(unionString, noteIds, emailIds, taskIds);

		String emailQuery = getEmailQuery(unionString, emailIds, taskIds);

		String taskQuery = getTaskQuery(taskIds);

		return "select a.* from ( " + campaignQuery + leadQuery + dealQuery + noteQuery + emailQuery + taskQuery
				+ ") as a order by a.\"createdTime\" desc";
	}

	private String getTaskQuery(List<Integer> taskIds) {
		String taskQuery = "";
		if (XamplifyUtils.isNotEmptyList(taskIds)) {
			taskQuery = allActivityTaskQuery;
		}
		return taskQuery;
	}

	private String getEmailQuery(String unionString, List<Integer> emailIds, List<Integer> taskIds) {
		String emailQuery = "";
		if (XamplifyUtils.isNotEmptyList(emailIds)) {
			emailQuery = allActivityEmailQuery;
			emailQuery += (XamplifyUtils.isNotEmptyList(taskIds)) ? unionString : "";
		}
		return emailQuery;
	}

	private String getNoteQuery(String unionString, List<Integer> noteIds, List<Integer> emailIds,
			List<Integer> taskIds) {
		String noteQuery = "";
		if (XamplifyUtils.isNotEmptyList(noteIds)) {
			noteQuery = allActivityNoteQuery;
			noteQuery += (XamplifyUtils.isNotEmptyList(emailIds) || XamplifyUtils.isNotEmptyList(taskIds)) ? unionString
					: "";
		}
		return noteQuery;
	}

	private String getDealQuery(String unionString, List<Integer> dealIds, List<Integer> noteIds,
			List<Integer> emailIds, List<Integer> taskIds) {
		String dealQuery = "";
		if (XamplifyUtils.isNotEmptyList(dealIds)) {
			dealQuery = allActivityDealQuery;
			dealQuery += (XamplifyUtils.isNotEmptyList(noteIds) || XamplifyUtils.isNotEmptyList(emailIds)
					|| XamplifyUtils.isNotEmptyList(taskIds)) ? unionString : "";
		}
		return dealQuery;
	}

	private String getLeadQuery(String unionString, List<Integer> leadIds, List<Integer> dealIds, List<Integer> noteIds,
			List<Integer> emailIds, List<Integer> taskIds) {
		String leadQuery = "";
		if (XamplifyUtils.isNotEmptyList(leadIds)) {
			leadQuery = allActivityLeadQuery;
			leadQuery += (XamplifyUtils.isNotEmptyList(dealIds) || XamplifyUtils.isNotEmptyList(noteIds)
					|| XamplifyUtils.isNotEmptyList(emailIds) || XamplifyUtils.isNotEmptyList(taskIds)) ? unionString
							: "";
		}
		return leadQuery;
	}

	private String getCampaignQuery(String unionString, List<Integer> campaignIds, List<Integer> leadIds,
			List<Integer> dealIds, List<Integer> noteIds, List<Integer> emailIds, List<Integer> taskIds) {
		String campaignQuery = "";
		if (XamplifyUtils.isNotEmptyList(campaignIds)) {
			campaignQuery = allActivityCampaignQuery;
			campaignQuery += (XamplifyUtils.isNotEmptyList(leadIds) || XamplifyUtils.isNotEmptyList(dealIds)
					|| XamplifyUtils.isNotEmptyList(noteIds) || XamplifyUtils.isNotEmptyList(emailIds)
					|| XamplifyUtils.isNotEmptyList(taskIds)) ? unionString : "";
		}
		return campaignQuery;
	}

	private List<ActivityDTO> getActivitiesSubList(Pagination pagination, List<ActivityDTO> activities) {
		Integer limitMaxResults = pagination.getMaxResults();
		Integer startIndex = ((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		Integer endIndex = ((pagination.getPageIndex() - 1) * pagination.getMaxResults()) + limitMaxResults;
		if (activities.size() > limitMaxResults) {
			if (endIndex <= activities.size()) {
				activities = activities.subList(startIndex, endIndex);
			} else {
				activities = activities.subList(startIndex, activities.size());
			}
		}
		return activities;
	}

	@SuppressWarnings("unchecked")
	private List<ActivityDTO> getAllContactsActivities(Pagination pagination, List<Integer> userIds, Session session,
			String filterBy) {
		String campaignSearchQuery = "";
		String leadSearchQuery = "";
		String dealSearchQuery = "";
		String noteSearchQuery = "";
		String emailSearchQuery = "";
		String taskSearchQuery = "";
		String vanityUrlFilterForCampaigns = "";
		String vanityUrlFilterForLeads = "";
		String vanityUrlFilterForDeals = "";
		boolean isLoggedInThroughVanity = XamplifyUtils.isValidInteger(pagination.getVendorCompanyId());
		if (isLoggedInThroughVanity) {
			boolean isLoggedInThroughOwnVanity = pagination.getCompanyId().equals(pagination.getVendorCompanyId());
			vanityUrlFilterForCampaigns = isLoggedInThroughOwnVanity ? " and xc.is_nurture_campaign = 'false' "
					: " and xc.vendor_organization_id = :vendorCompanyId ";
			vanityUrlFilterForLeads = " and xl.created_for_company_id = :vendorCompanyId ";
			vanityUrlFilterForDeals = " and xd.created_for_company_id = :vendorCompanyId ";
		}
		if (XamplifyUtils.isValidString(pagination.getSearchKey())) {
			campaignSearchQuery = " and xc.campaign_name ilike '%" + pagination.getSearchKey() + "%' ";
			leadSearchQuery = " and xl.last_name ilike '%" + pagination.getSearchKey() + "%' ";
			dealSearchQuery = " and xd.title ilike '%" + pagination.getSearchKey() + "%' ";
			noteSearchQuery = " and xn.title ilike '%" + pagination.getSearchKey() + "%' ";
			emailSearchQuery = " and xea.subject ilike '%" + pagination.getSearchKey() + "%' ";
			taskSearchQuery = " and xta.name ilike '%" + pagination.getSearchKey() + "%' ";
		}
		String sqlQuery = "select a.* from (select distinct 'Campaign' as \"type\", xc.campaign_id as \"id\", cast(xc.launch_time as text) as \"createdTime\" from xt_campaign xc "
				+ "left join xt_campaign_user_userlist xcuul on xcuul.campaign_id= xc.campaign_id "
				+ "left join xt_user_profile xup on xc.customer_id = xup.user_id "
				+ "where xc.campaign_type in ('VIDEO', 'REGULAR') and xcuul.user_id in (:userIds) and xc.is_launched = 'true' and xup.company_id = :companyId "
				+ vanityUrlFilterForCampaigns + campaignSearchQuery + UNION
				+ "select distinct 'Lead', xl.id as \"id\", case when cd.activity_type = 'LEAD_STAGE_UPDATED' then cast(cd.created_time as text) else cast(xl.created_time as text) end as \"createdTime\" from xt_lead xl left join xt_user_profile xup on xup.user_id= xl.associated_user_id "
				+ "left join xt_pipeline_stage ps on xl.pipeline_stage_id=ps.id left join xt_campaign_deal_comments cd on cd.lead_id= xl.id "
				+ "where xl.associated_user_id in (:userIds) and xl.created_by_company_id = :companyId "
				+ vanityUrlFilterForLeads + leadSearchQuery + UNION
				+ "select distinct 'Deal', xd.id as \"id\", case when cd.activity_type = 'DEAL_STAGE_UPDATED' then cast(cd.created_time as text) else cast(xd.created_time as text) end as \"createdTime\" from xt_deal xd left join xt_user_profile xup on xup.user_id= xd.associated_user_id left join xt_pipeline_stage ps on xd.pipeline_stage_id=ps.id "
				+ "left join xt_campaign_deal_comments cd on cd.deal_id=xd.id where xd.associated_user_id in (:userIds) and xd.created_by_company_id = :companyId "
				+ vanityUrlFilterForDeals + dealSearchQuery + UNION
				+ "select distinct 'Note', xn.id as \"id\", cast(xn.updated_time as text) as \"createdTime\" from xt_note xn left join xt_user_profile xup on xn.contact_id= xup.user_id left join xt_user_profile xcup on xn.created_by = xcup.user_id "
				+ "where ((xn.visibility = 'PUBLIC') or (xn.visibility = 'PRIVATE' AND xn.created_by = :loggedInUserId)) and xn.contact_id in (:userIds) and xn.company_id= :companyId "
				+ noteSearchQuery + UNION
				+ "select distinct 'Email', xea.id as \"id\", cast(xea.created_time as text) as \"createdTime\" from xt_email_activity xea left join xt_user_profile xup on xup.user_id= xea.recipient_user_id left join xt_user_profile xupp on xupp.user_id = xea.sender_user_id "
				+ "where xup.user_id in (:userIds) and xea.company_id= :companyId" + emailSearchQuery + UNION
				+ "select distinct 'Task', xta.id, cast(xta.updated_time as text) as \"createdTime\" from xt_task_activity xta left join xt_task_activity_status xtas on xtas.id = xta.status where xta.contact_id in (:userIds) and xta.company_id = :companyId"
				+ taskSearchQuery + ") as a " + filterBy + " order by a.\"createdTime\" desc";
		List<List<Integer>> chunkedUserIdsList = XamplifyUtils.getChunkedList(userIds);
		List<ActivityDTO> activities = new ArrayList<>();
		if (XamplifyUtils.isNotEmptyList(chunkedUserIdsList.get(0))) {
			for (List<Integer> chunckedUserIds : chunkedUserIdsList) {
				SQLQuery query = session.createSQLQuery(sqlQuery);
				query.setParameter(LOGGED_IN_USER_ID, pagination.getUserId());
				query.setParameter("companyId", pagination.getCompanyId());
				query.setParameterList("userIds", chunckedUserIds);
				if (isLoggedInThroughVanity) {
					query.setParameter("vendorCompanyId", pagination.getVendorCompanyId());
				}
				activities.addAll(query.setResultTransformer(Transformers.aliasToBean(ActivityDTO.class)).list());
			}
		}
		return activities;
	}

}
