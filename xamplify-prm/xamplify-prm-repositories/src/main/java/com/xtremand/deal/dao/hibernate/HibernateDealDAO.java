package com.xtremand.deal.dao.hibernate;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.activity.dto.ContactOpportunityRequestDTO;
import com.xtremand.common.bom.Pagination;
import com.xtremand.deal.bom.Deal;
import com.xtremand.deal.dao.DealDAO;
import com.xtremand.deal.dto.ContactDealResponseDTO;
import com.xtremand.deal.dto.DealCountsResponseDTO;
import com.xtremand.deal.dto.DealDto;
import com.xtremand.deal.dto.DealResponseDTO;
import com.xtremand.integration.bom.Integration.IntegrationType;
import com.xtremand.lead.bom.Pipeline;
import com.xtremand.lead.bom.PipelineType;
import com.xtremand.util.PaginationUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.HibernateSQLQueryResultUtilDao;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.QueryParameterDTO;
import com.xtremand.util.dto.TeamMemberFilterDTO;

@Repository("DealDAO")
@Transactional
public class HibernateDealDAO implements DealDAO {

	private static final String VANITY_FILTER_QUERY = "{vanityFilterQuery}";

	private static final String AND_D_CREATED_FOR_COMPANY_ID_VENDOR_COMPANY_ID = " and d.created_for_company_id = :vendorCompanyId";

	private static final String DEAL_IDS = "dealIds";

	@Autowired
	SessionFactory sessionFactory;

	@Autowired
	private UtilDao utilDao;

	@Autowired
	private HibernateSQLQueryResultUtilDao hibernateSQLQueryResultUtilDao;

	@Autowired
	private PaginationUtil paginationUtil;

	/** XNFR-650 **/
	@Value("${dealResponseDtoQuery}")
	private String dealResponseDtoQuery;

	@Value("${showDealActionsQuery}")
	private String showDealActionsQuery;

	@Value("${showEditDealStageQuery}")
	private String showEditDealStageQuery;

	@Value("${canUpdateDealQuery}")
	private String canUpdateDealQuery;

	@Value("${dealUnreadChatCountQuery}")
	private String dealUnreadChatCountQuery;

	@Value("${dealUnreadPropertyChatCountQuery}")
	private String dealUnreadPropertyChatCountQuery;

	@Value("${dealFROMClause}")
	private String dealFROMClause;

	@Value("${showDealActionsAndEditDealStageJoinCondition}")
	private String showDealActionsAndEditDealStageJoinCondition;

	@Value("${canUpdateDealJoinCondition}")
	private String canUpdateDealJoinCondition;

	@Value("${dealUnreadChatCountJoinCondition}")
	private String dealUnreadChatCountJoinCondition;

	@Value("${dealUnreadPropertyChatCountJoinQuery}")
	private String dealUnreadPropertyChatCountJoinQuery;

	@Value("${orderDealsBy}")
	private String orderDealsBy;

	@Value("${dealTileCountsQuery}")
	private String dealTileCountsQuery;

	/** XNFR-882 **/
	@Value("${company.deals.from.and.where.clause.data.query}")
	private String companyDealsFromAndWhereClauseDataQuery;

	@Value("${company.deals.from.and.where.clause.count.query}")
	private String companyDealsFromAndWhereClauseCountQuery;

	@Value("${contact.deals.from.and.where.clause.data.query}")
	private String contactDealsFromAndWhereClauseDataQuery;

	@Value("${contact.deals.from.and.where.clause.count.query}")
	private String contactDealsFromAndWhereClauseCountQuery;

	@Value("${deals.limited.data.query}")
	private String dealsLimitedDataQuery;

	private static final String CREATED_FOR_COMPANY_ID = "createdForCompany.id";

	private static final String CREATED_BY_COMPANY_ID = "createdByCompany.id";

	private static final String COMPANY_ID = "companyId";

	private static final String TOTAL_RECORDS = "totalRecords";

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getDealsForPartner(Pagination pagination) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(Deal.class, "D");
		criteria.add(Restrictions.eq(CREATED_BY_COMPANY_ID, pagination.getCompanyId()));

		if (!XamplifyUtils.isValidInteger(pagination.getContactId())) {
			criteria.add(Restrictions.ne(CREATED_FOR_COMPANY_ID, pagination.getCompanyId()));
		}

		if (pagination.getVendorCompanyId() != null && pagination.getVendorCompanyId() > 0) {
			criteria.add(Restrictions.eq(CREATED_FOR_COMPANY_ID, pagination.getVendorCompanyId()));
		}

		criteria.createAlias("D.currentStage", "CS", JoinType.LEFT_OUTER_JOIN);
		if (StringUtils.isNotBlank(pagination.getFilterKey())) {
			if ("won".equals(pagination.getFilterKey())) {
				criteria.add(Restrictions.eq("CS.won", true));
			} else if ("lost".equals(pagination.getFilterKey())) {
				criteria.add(Restrictions.eq("CS.lost", true));
			}
		}

		if (pagination.getSearchKey() != null && !pagination.getSearchKey().isEmpty()) {
			criteria.createAlias("D.campaign", "C", JoinType.LEFT_OUTER_JOIN);
			criteria.createAlias("D.createdForCompany", "CFC", JoinType.LEFT_OUTER_JOIN);
			criteria.createAlias("D.createdByCompany", "CBC", JoinType.LEFT_OUTER_JOIN);
			Disjunction disjunction = Restrictions.disjunction();
			disjunction.add(Restrictions.ilike("D.title", pagination.getSearchKey(), MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("C.campaign", pagination.getSearchKey(), MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("CFC.companyName", pagination.getSearchKey(), MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("CBC.companyName", pagination.getSearchKey(), MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("CS.stageName", pagination.getSearchKey(), MatchMode.ANYWHERE));

			criteria.add(disjunction);
		}

		if (pagination.getFromDateFilter() != null) {
			criteria.add(Restrictions.ge("D.createdTime", pagination.getFromDateFilter()));
		}
		if (pagination.getToDateFilter() != null) {
			criteria.add(Restrictions.le("D.createdTime", pagination.getToDateFilter()));
		}

		if (!StringUtils.isBlank(pagination.getStageFilter())) {
			criteria.add(Restrictions.eq("CS.stageName", pagination.getStageFilter()));
		}

		Integer registeredByUserId = pagination.getRegisteredByUserId();
		if (XamplifyUtils.isValidInteger(registeredByUserId)) {
			criteria.add(Restrictions.eq("createdBy", registeredByUserId));
		}

		if (XamplifyUtils.isValidInteger(pagination.getContactId())) {
			criteria.add(Restrictions.eq("associatedUser.userId", pagination.getContactId()));
		}

		criteria.addOrder(Order.desc("id"));

		ScrollableResults scrollableResults = criteria.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;

		if (!pagination.isExcludeLimit()) {
			criteria.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
			criteria.setMaxResults(pagination.getMaxResults());
		}

		List<Deal> deals = criteria.list();
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put(TOTAL_RECORDS, totalRecords);
		resultMap.put("data", deals);
		return resultMap;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getDealsForVendor(Pagination pagination) {
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), false);
		Map<String, Object> resultMap = new HashMap<>();
		Integer totalRecords = 0;
		List<Deal> deals = new ArrayList<>();
		if (!teamMemberFilterDTO.isEmptyFilter()) {
			Session session = sessionFactory.getCurrentSession();
			Criteria criteria = session.createCriteria(Deal.class, "D");
			criteria.add(Restrictions.eq("createdForCompany.id", pagination.getCompanyId()));

			criteria.createAlias("D.currentStage", "CS", JoinType.LEFT_OUTER_JOIN);
			if (StringUtils.isNotBlank(pagination.getFilterKey())) {
				if ("won".equals(pagination.getFilterKey())) {
					criteria.add(Restrictions.eq("CS.won", true));
				} else if ("lost".equals(pagination.getFilterKey())) {
					criteria.add(Restrictions.eq("CS.lost", true));
				}
			}

			if (teamMemberFilterDTO.isApplyTeamMemberFilter()) {
				criteria.add(Restrictions.in("createdByCompany.id",
						teamMemberFilterDTO.getPartnershipIdsOrPartnerCompanyIds()));
			}

			if (pagination.getSearchKey() != null && !pagination.getSearchKey().isEmpty()) {
				criteria.createAlias("D.campaign", "C", JoinType.LEFT_OUTER_JOIN);
				criteria.createAlias("D.createdForCompany", "CFC", JoinType.LEFT_OUTER_JOIN);
				criteria.createAlias("D.createdByCompany", "CBC", JoinType.LEFT_OUTER_JOIN);
				Disjunction disjunction = Restrictions.disjunction();
				disjunction.add(Restrictions.ilike("D.title", pagination.getSearchKey(), MatchMode.ANYWHERE));
				disjunction.add(Restrictions.ilike("C.campaign", pagination.getSearchKey(), MatchMode.ANYWHERE));
				disjunction.add(Restrictions.ilike("CFC.companyName", pagination.getSearchKey(), MatchMode.ANYWHERE));
				disjunction.add(Restrictions.ilike("CBC.companyName", pagination.getSearchKey(), MatchMode.ANYWHERE));
				disjunction.add(Restrictions.ilike("CS.stageName", pagination.getSearchKey(), MatchMode.ANYWHERE));
				disjunction.add(Restrictions.ilike("D.referenceId", pagination.getSearchKey(), MatchMode.ANYWHERE));
				disjunction.add(Restrictions.ilike("D.sfDealId", pagination.getSearchKey(), MatchMode.ANYWHERE));
				disjunction.add(Restrictions.ilike("D.pipedriveDealId", pagination.getSearchKey(), MatchMode.ANYWHERE));
				disjunction.add(
						Restrictions.ilike("D.microsoftDynamicsDealId", pagination.getSearchKey(), MatchMode.ANYWHERE));
				disjunction.add(Restrictions.ilike("D.zohoDealId", pagination.getSearchKey(), MatchMode.ANYWHERE));
				criteria.add(disjunction);
			}

			if (pagination.getFromDateFilter() != null) {
				criteria.add(Restrictions.ge("D.createdTime", pagination.getFromDateFilter()));
			}
			if (pagination.getToDateFilter() != null) {
				criteria.add(Restrictions.le("D.createdTime", pagination.getToDateFilter()));
			}

			if (!StringUtils.isBlank(pagination.getStageFilter())) {
				criteria.add(Restrictions.eq("CS.stageName", pagination.getStageFilter()));
			}

			Integer registeredByUserId = pagination.getRegisteredByUserId();
			if (XamplifyUtils.isValidInteger(registeredByUserId)) {
				criteria.add(Restrictions.eq("createdBy", registeredByUserId));
			}

			Integer registeredByCompanyId = pagination.getRegisteredByCompanyId();
			if (XamplifyUtils.isValidInteger(registeredByCompanyId)) {
				criteria.add(Restrictions.eq(CREATED_BY_COMPANY_ID, registeredByCompanyId));
			}

			criteria.addOrder(Order.desc("id"));

			ScrollableResults scrollableResults = criteria.scroll();
			scrollableResults.last();
			totalRecords = scrollableResults.getRowNumber() + 1;

			if (!pagination.isExcludeLimit()) {
				criteria.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
				criteria.setMaxResults(pagination.getMaxResults());
			}

			deals = criteria.list();
		}

		resultMap.put("totalRecords", totalRecords);
		resultMap.put("data", deals);
		return resultMap;
	}

	@Override
	public DealCountsResponseDTO getCountsForVendor(Integer companyId, boolean applyFilter) {

		String sqlQuery = dealTileCountsQuery + " where xd.created_for_company_id = :companyId";
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(sqlQuery).setParameter(COMPANY_ID, companyId);
		return (DealCountsResponseDTO) query.setResultTransformer(Transformers.aliasToBean(DealCountsResponseDTO.class))
				.uniqueResult();
	}

	@Override
	public DealCountsResponseDTO getCountsForPartner(Integer companyId) {
		String sqlQuery = dealTileCountsQuery + " where xd.created_by_company_id = :companyId";
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(sqlQuery).setParameter(COMPANY_ID, companyId);
		return (DealCountsResponseDTO) query.setResultTransformer(Transformers.aliasToBean(DealCountsResponseDTO.class))
				.uniqueResult();
	}

	@Override
	public DealCountsResponseDTO getCountsForPartnerInVanity(Integer companyId, Integer vendorCompanyId) {

		String sqlQuery = dealTileCountsQuery
				+ " where xd.created_by_company_id = :companyId and xd.created_for_company_id = :createdForCompanyId";
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(sqlQuery).setParameter(COMPANY_ID, companyId)
				.setParameter("createdForCompanyId", vendorCompanyId);
		return (DealCountsResponseDTO) query.setResultTransformer(Transformers.aliasToBean(DealCountsResponseDTO.class))
				.uniqueResult();
	}

	@Override
	public BigInteger getUnReadChatCount(Integer dealId, Integer loggedInUserId) {
		String sql = "select count(*) from xt_campaign_deal_comments dc where " + "(dc.deal_id = " + dealId
				+ " or dc.lead_id = (select associated_lead_id from xt_deal d where d.id = " + dealId + ")) "
				+ " and dc.created_by != " + loggedInUserId
				+ " and dc.id > (select coalesce((select comment_id from xt_campaign_deal_comments_statistics where deal_id = "
				+ dealId + " and created_by =" + loggedInUserId + "), 0) as cid)";
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(sql);
		return (BigInteger) query.uniqueResult();
	}

	@Override
	public BigInteger getUnReadPropertyChatCount(Integer propertyId, Integer loggedInUserId) {
		String sql = "select count(*) from xt_campaign_deal_comments where property_id = " + propertyId
				+ " and id > (select coalesce((select comment_id from xt_campaign_deal_comments_statistics where property_id = "
				+ propertyId + " and created_by =" + loggedInUserId + "), 0) as cid)";
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(sql);
		return (BigInteger) query.uniqueResult();
	}

	@Override
	public DealCountsResponseDTO findDealsCountByFilter(String sqlQuery, Integer companyId,
			List<Integer> partnerCompanyIds) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(sqlQuery).setParameter(COMPANY_ID, companyId)
				.setParameterList("partnerCompanyIds", partnerCompanyIds);
		return (DealCountsResponseDTO) query.setResultTransformer(Transformers.aliasToBean(DealCountsResponseDTO.class))
				.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getStageNamesForPartner(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "  select distinct xps.stage_name as \"Stage Name\" from xt_company_profile xcp\r\n"
				+ "  left join xt_deal xd on xcp.company_id=xd.created_by_company_id left join \r\n"
				+ "  xt_pipeline_stage xps on xps.id =xd.pipeline_stage_id where xd.created_by_company_id !="
				+ companyId + " \r\n" + "  and xd.created_for_company_id =" + companyId + " order by 1 desc";
		Query query = session.createSQLQuery(sql);
		return (List<String>) query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getStageNamesForVendorInCampaign(Integer companyId) {
		// TODO Auto-generated method stub
		Session session = sessionFactory.getCurrentSession();
		String sql = "select distinct(ps.stage_name) from xt_deal l, xt_pipeline_stage ps \r\n"
				+ " where l.pipeline_stage_id = ps.id and created_for_company_id =  " + companyId + " ";
		Query query = session.createSQLQuery(sql);
		return (List<String>) query.list();

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getStageNamesForPartnerCompanyId(Integer companyId) {
		// TODO Auto-generated method stub
		Session session = sessionFactory.getCurrentSession();
		String sql = "select distinct xps.stage_name  from\r\n" + "xt_company_profile xcp\r\n"
				+ " left join xt_deal xd on xcp.company_id=xd.created_for_company_id\r\n"
				+ " left join xt_pipeline_stage xps on xps.id =xd.pipeline_stage_id\r\n"
				+ " where xd.created_by_company_id =" + companyId + "";
		Query query = session.createSQLQuery(sql);
		return (List<String>) query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getStageNamesForVendor(Integer companyId, String activeCRM) {
		Session session = sessionFactory.getCurrentSession();
		/*
		 * String sql =
		 * "select distinct(ps.stage_name) from xt_deal l, xt_pipeline_stage ps \r\n" +
		 * "where l.pipeline_stage_id = ps.id and created_for_company_id =  "
		 * +companyId+" ";
		 */
		String activeCRMSQL = "";
		if (!StringUtils.isBlank(activeCRM)) {
			activeCRMSQL = ", '" + activeCRM + "'";
		}
		String sql = "select distinct(ps.stage_name) from xt_pipeline p, xt_pipeline_stage ps \r\n"
				+ " where p.id = ps.pipeline_id and p.company_id =  " + companyId + " and p.type ='"
				+ PipelineType.DEAL.name() + "' " + " and p.integration_type in ('"
				+ IntegrationType.XAMPLIFY.name().toLowerCase() + "'" + activeCRMSQL + ")";

		Query query = session.createSQLQuery(sql);
		return (List<String>) query.list();
	}

	@Override
	public List<Deal> getDealsForVendorByType(Integer companyId, IntegrationType type) {
		List<Deal> deals = new ArrayList<Deal>();
		String colName = "";
		if (colName != "") {
			Session session = sessionFactory.getCurrentSession();
			Criteria criteria = session.createCriteria(Deal.class, "D");
			criteria.add(Restrictions.eq(CREATED_FOR_COMPANY_ID, companyId));
			criteria.add(Restrictions.isNotNull(colName));
			deals = criteria.list();
		}
		return deals;
	}

	@Override
	public List<Deal> getDealsWithoutLeadAndWithSfDealIdForVendor(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(Deal.class, "D");
		criteria.add(Restrictions.eq(CREATED_FOR_COMPANY_ID, companyId));
		criteria.add(Restrictions.isNotNull("sfDealId"));
		criteria.add(Restrictions.isNull("associatedLead"));
		return criteria.list();
	}

	@Override
	public Integer getDefaultDealOrLeadPipeLineIdByNameAndTypeAndCompanyId(Pipeline pipeLine) {
		String sqlString = "select id from xt_pipeline where name = :name"
				+ " and cast(type as text) = :type and company_id = :companyId and is_default";
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(sqlString);
		query.setParameter(COMPANY_ID, pipeLine.getCompany().getId());
		query.setParameter("type", pipeLine.getType().name());
		query.setParameter("name", pipeLine.getName());
		return (Integer) query.uniqueResult();
	}

	@Override
	public boolean isDefaultStageNameExistsByStageNameAndPipeLineId(String stageName, Integer pipeLineId) {
		String sqlString = "select case when count(*)>0 then true else false end from xt_pipeline_stage where stage_name = :stageName"
				+ " and pipeline_id = :pipeLineId  and is_default=:isDefault";
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(sqlString);
		query.setParameter("stageName", stageName);
		query.setParameter("pipeLineId", pipeLineId);
		query.setParameter("isDefault", "Opened".equals(stageName));
		return (boolean) query.uniqueResult();
	}

	@Override
	public void updateDealTitle(Integer formId, String labelId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "update xt_deal set title = b.value from (select scd.deal_id, scd.value from xt_sf_cf_data scd, xt_form_label fl, xt_form f where scd.sf_cf_label_id = fl.id and fl.form_id = f.id and fl.label_id = :labelId and f.id = :formId and scd.value is not null) b where id=b.deal_id ";
		Query query = session.createSQLQuery(sql);
		query.setParameter("formId", formId);
		query.setParameter("labelId", labelId);
		query.executeUpdate();
	}

	@Override
	public void updateDealAmount(Integer formId, String labelId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "update xt_deal set amount = cast(b.value as double precision) from (select scd.deal_id, scd.value from xt_sf_cf_data scd, xt_form_label fl, xt_form f where scd.sf_cf_label_id = fl.id and fl.form_id = f.id and fl.label_id = :labelId and f.id = :formId and scd.value is not null) b where id=b.deal_id ";
		Query query = session.createSQLQuery(sql);
		query.setParameter("formId", formId);
		query.setParameter("labelId", labelId);
		query.executeUpdate();
	}

	@Override
	public void updateDealCloseDate(Integer formId, String labelId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "update xt_deal set close_date = cast(b.value as timestamp without time zone) from (select scd.deal_id, scd.value from xt_sf_cf_data scd, xt_form_label fl, xt_form f where scd.sf_cf_label_id = fl.id and fl.form_id = f.id and fl.label_id = :labelId and f.id = :formId and scd.value is not null) b where id=b.deal_id ";
		Query query = session.createSQLQuery(sql);
		query.setParameter("formId", formId);
		query.setParameter("labelId", labelId);
		query.executeUpdate();
	}

	@Override
	public List<Deal> getDealsForSyncByType(Integer companyId, IntegrationType type) {
		List<Deal> deals = new ArrayList<Deal>();
		String colName = "";
		if (colName != "") {
			Session session = sessionFactory.getCurrentSession();
			Criteria criteria = session.createCriteria(Deal.class, "D");
			Disjunction disjunction = Restrictions.disjunction();
			disjunction.add(Restrictions.eq(CREATED_FOR_COMPANY_ID, companyId));
			disjunction.add(Restrictions.eq(CREATED_BY_COMPANY_ID, companyId));
			criteria.add(disjunction);
			criteria.add(Restrictions.isNotNull(colName));
			deals = criteria.list();
		}
		return deals;
	}

	@Override
	public List<Deal> getDealsWithoutLeadAndWithSfDealId(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(Deal.class, "D");
		Disjunction disjunction = Restrictions.disjunction();
		disjunction.add(Restrictions.eq(CREATED_FOR_COMPANY_ID, companyId));
		disjunction.add(Restrictions.eq(CREATED_BY_COMPANY_ID, companyId));
		criteria.add(disjunction);
		criteria.add(Restrictions.isNotNull("sfDealId"));
		criteria.add(Restrictions.isNull("associatedLead"));
		return criteria.list();
	}

	@Override
	public Integer deleteDealById(Integer dealId) {
		Session session = sessionFactory.getCurrentSession();
		String sqlQuery = "delete from xt_deal where id = :dealId";
		Query query = session.createSQLQuery(sqlQuery);
		query.setParameter("dealId", dealId);
		return query.executeUpdate();
	}

	/** XNFR-650 **/
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> queryDealsForPartner(Pagination pagination) {
		Map<String, Object> resultMap = new HashMap<>();
		String sql = "";

		sql += dealResponseDtoQuery + showDealActionsQuery + showEditDealStageQuery + canUpdateDealQuery
				+ dealUnreadChatCountQuery + dealUnreadPropertyChatCountQuery + dealFROMClause
				+ showDealActionsAndEditDealStageJoinCondition + canUpdateDealJoinCondition
				+ dealUnreadChatCountJoinCondition + dealUnreadPropertyChatCountJoinQuery;

		sql += "WHERE xd.created_by_company_id = :companyId AND xd.created_for_company_id != :companyId ";

		if (pagination.getVendorCompanyId() != null && pagination.getVendorCompanyId() > 0) {
			sql += "AND xd.created_for_company_id = :vendorCompanyId ";
		}

		if (StringUtils.isNotBlank(pagination.getFilterKey())) {
			if ("won".equals(pagination.getFilterKey())) {
				sql += "AND xps.is_won is true ";
			} else if ("lost".equals(pagination.getFilterKey())) {
				sql += "AND xps.is_lost is true ";
			}
		}

		if (pagination.getSearchKey() != null && !pagination.getSearchKey().isEmpty()) {
			String searchKey = "'%" + pagination.getSearchKey() + "%'";
			sql += "AND (lower(xd.title) like " + searchKey + " or lower(xcp.company_name) like " + searchKey
					+ " or lower(xcp1.company_name) like " + searchKey + " or lower(xc.campaign_name) like " + searchKey
					+ " or lower(xpc.campaign_name) like " + searchKey + " or lower(xps.stage_name) like " + searchKey
					+ ") ";
		}

		sql = setDealFilterQuery(sql, pagination);

		sql += orderDealsBy;

		Session session = sessionFactory.getCurrentSession();
		SQLQuery query = session.createSQLQuery(sql);

		query.setParameter(COMPANY_ID, pagination.getCompanyId());
		query.setParameter("loggedInUserId", pagination.getUserId());
		if (pagination.getVendorCompanyId() != null && pagination.getVendorCompanyId() > 0) {
			query.setParameter("vendorCompanyId", pagination.getVendorCompanyId());
		}

		setParametersForDealFilterQuery(query, pagination);

		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		query.setMaxResults(pagination.getMaxResults());
		List<DealResponseDTO> list = query.setResultTransformer(Transformers.aliasToBean(DealResponseDTO.class)).list();
		resultMap.put("totalRecords", totalRecords);
		resultMap.put("data", list);

		return resultMap;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> queryDealsForVendor(Pagination pagination) {
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), false);
		Map<String, Object> resultMap = new HashMap<>();
		List<DealResponseDTO> list = new ArrayList<>();
		Integer totalRecords = 0;
		if (!teamMemberFilterDTO.isEmptyFilter()) {
			String sql = "";

			sql += dealResponseDtoQuery + showDealActionsQuery + showEditDealStageQuery + canUpdateDealQuery
					+ dealUnreadChatCountQuery + dealUnreadPropertyChatCountQuery + dealFROMClause
					+ showDealActionsAndEditDealStageJoinCondition + canUpdateDealJoinCondition
					+ dealUnreadChatCountJoinCondition + dealUnreadPropertyChatCountJoinQuery;
			sql += "WHERE xd.created_for_company_id = :companyId ";

			if (teamMemberFilterDTO.isApplyTeamMemberFilter()) {
				sql += "AND xd.created_by_company_id in (:partnershipIdsOrPartnerCompanyIds) ";
			}

			if (StringUtils.isNotBlank(pagination.getFilterKey())) {
				if ("won".equals(pagination.getFilterKey())) {
					sql += "AND xps.is_won is true ";
				} else if ("lost".equals(pagination.getFilterKey())) {
					sql += "AND xps.is_lost is true ";
				}
			}

			if (pagination.getSearchKey() != null && !pagination.getSearchKey().isEmpty()) {
				String searchKey = "'%" + pagination.getSearchKey() + "%'";
				sql += "AND (lower(xd.title) like " + searchKey + " or lower(xcp.company_name) like " + searchKey
						+ " or lower(xcp1.company_name) like " + searchKey + " or lower(xc.campaign_name) like "
						+ searchKey + " or lower(xpc.campaign_name) like " + searchKey
						+ " or lower(xps.stage_name) like " + searchKey + " or xd.crm_reference_id like " + searchKey
						+ ") ";
			}

			sql = setDealFilterQuery(sql, pagination);

			sql += orderDealsBy;

			Session session = sessionFactory.getCurrentSession();
			SQLQuery query = session.createSQLQuery(sql);

			query.setParameter("loggedInUserId", pagination.getUserId());
			query.setParameter(COMPANY_ID, pagination.getCompanyId());

			if (pagination.getVendorCompanyId() != null && pagination.getVendorCompanyId() > 0) {
				query.setParameter(COMPANY_ID, pagination.getVendorCompanyId());
			}

			if (teamMemberFilterDTO.isApplyTeamMemberFilter()) {
				query.setParameterList("partnershipIdsOrPartnerCompanyIds",
						teamMemberFilterDTO.getPartnershipIdsOrPartnerCompanyIds());
			}

			setParametersForDealFilterQuery(query, pagination);

			ScrollableResults scrollableResults = query.scroll();
			scrollableResults.last();
			totalRecords = scrollableResults.getRowNumber() + 1;
			query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
			query.setMaxResults(pagination.getMaxResults());
			list = query.setResultTransformer(Transformers.aliasToBean(DealResponseDTO.class)).list();
		}

		resultMap.put("totalRecords", totalRecords);
		resultMap.put("data", list);
		return resultMap;
	}

	private String setDealFilterQuery(String sql, Pagination pagination) {
		if (pagination.getFromDateFilter() != null) {
			sql += "AND xd.created_time >= :fromDate ";
		}
		if (pagination.getToDateFilter() != null) {
			sql += "AND xd.created_time <= :toDate ";
		}

		if (!StringUtils.isBlank(pagination.getStageFilter())) {
			sql += "AND xps.stage_name = :currentStageName ";
		}

		if (XamplifyUtils.isValidInteger(pagination.getRegisteredByUserId())) {
			sql += "AND xd.created_by = :registeredByUserId ";
		}

		if (XamplifyUtils.isValidInteger(pagination.getRegisteredByCompanyId())) {
			sql += "ANd xd.created_by_company_id = :registeredByCompanyId ";
		}
		return sql;
	}

	private void setParametersForDealFilterQuery(SQLQuery query, Pagination pagination) {
		if (pagination.getFromDateFilter() != null) {
			query.setParameter("fromDate", pagination.getFromDateFilter());
		}
		if (pagination.getToDateFilter() != null) {
			query.setParameter("toDate", pagination.getToDateFilter());
		}

		if (!StringUtils.isBlank(pagination.getStageFilter())) {
			query.setParameter("currentStageName", pagination.getStageFilter());
		}

		if (XamplifyUtils.isValidInteger(pagination.getRegisteredByUserId())) {
			query.setParameter("registeredByUserId", pagination.getRegisteredByUserId());
		}

		if (XamplifyUtils.isValidInteger(pagination.getRegisteredByCompanyId())) {
			query.setParameter("registeredByCompanyId", pagination.getRegisteredByCompanyId());
		}
	}

	/** XNFR-650 END **/

	/** XNFR-553 **/
	@Override
	public Map<String, Object> findDealsAndCountByContactId(ContactOpportunityRequestDTO contactOpportunityRequestDTO) {
		String vanityFilterQuery = "";
		String fromAndWhereClauseDataQueryString = "";
		String fromAndWhereClauseRowCountQueryString = "";
		if (contactOpportunityRequestDTO.isVanityUrlFilter()) {
			vanityFilterQuery = AND_D_CREATED_FOR_COMPANY_ID_VENDOR_COMPANY_ID;
		}
		if (Boolean.TRUE.equals(contactOpportunityRequestDTO.getIsCompanyJourney())) {
			fromAndWhereClauseDataQueryString = companyDealsFromAndWhereClauseDataQuery.replace(VANITY_FILTER_QUERY,
					vanityFilterQuery);
			fromAndWhereClauseRowCountQueryString = companyDealsFromAndWhereClauseCountQuery
					.replace(VANITY_FILTER_QUERY, vanityFilterQuery);
		} else {
			fromAndWhereClauseDataQueryString = contactDealsFromAndWhereClauseDataQuery.replace(VANITY_FILTER_QUERY,
					vanityFilterQuery);
			fromAndWhereClauseRowCountQueryString = contactDealsFromAndWhereClauseCountQuery
					.replace(VANITY_FILTER_QUERY, vanityFilterQuery);
		}
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String queryString = dealsLimitedDataQuery + fromAndWhereClauseDataQueryString;
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(COMPANY_ID, contactOpportunityRequestDTO.getLoggedInUserCompanyId()));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("contactId", contactOpportunityRequestDTO.getContactId()));
		if (contactOpportunityRequestDTO.isVanityUrlFilter()) {
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("vendorCompanyId", contactOpportunityRequestDTO.getVendorCompanyId()));
		}
		hibernateSQLQueryResultRequestDTO.setClassInstance(ContactDealResponseDTO.class);
		hibernateSQLQueryResultRequestDTO
				.setRowCountQueryString("select cast(count(*) as int) " + fromAndWhereClauseRowCountQueryString);
		Pagination pagination = new Pagination();
		pagination.setMaxResults(4);
		return hibernateSQLQueryResultUtilDao.returnPaginatedDTOList(hibernateSQLQueryResultRequestDTO, pagination, "");
	}

	/** XNFR-848 **/
	@Override
	public Map<String, Object> fetchDealsForCompanyJourney(Pagination pagination) {
		String dataQuery = dealResponseDtoQuery + showDealActionsQuery + showEditDealStageQuery + canUpdateDealQuery
				+ dealUnreadChatCountQuery + dealUnreadPropertyChatCountQuery;
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = frameHibernateSQLResultRequestDTO(
				pagination, dataQuery);
		return hibernateSQLQueryResultUtilDao.returnPaginatedDTOList(hibernateSQLQueryResultRequestDTO, pagination,
				pagination.getSearchKey());
	}

	private String frameDealsDataQueryString(Pagination pagination) {
		String queryString = dealFROMClause + showDealActionsAndEditDealStageJoinCondition + canUpdateDealJoinCondition
				+ dealUnreadChatCountJoinCondition + dealUnreadPropertyChatCountJoinQuery
				+ ", xt_user_list ul,xt_user_userlist uul "
				+ " where ul.associated_company_id = :companyJourneyId and ul.user_list_id = uul.user_list_id and uul.user_id = xd.associated_user_id and xd.created_by_company_id = :companyId "
				+ "and ul.company_id = :companyId ";
		if (pagination.isIgnoreSelfLeadsOrDeals() && !XamplifyUtils.isValidInteger(pagination.getContactId())) {
			queryString += "and xd.created_for_company_id != :companyId ";
		}
		if (pagination.getVendorCompanyId() != null && pagination.getVendorCompanyId() > 0) {
			queryString += "and xd.created_for_company_id = :createdForCompanyId ";
		}
		if (StringUtils.isNotBlank(pagination.getFilterKey())) {
			if ("won".equals(pagination.getFilterKey())) {
				queryString += "and xps.is_won is true ";
			} else if ("lost".equals(pagination.getFilterKey())) {
				queryString += "and xps.is_lost is true ";
			}
		}
		if (pagination.getSearchKey() != null && !pagination.getSearchKey().isEmpty()) {
			String searchKey = "'%" + pagination.getSearchKey() + "%'";
			queryString += "and (lower(xd.title) like " + searchKey + " or lower(xcp.company_name) like " + searchKey
					+ " or lower(xcp1.company_name) like " + searchKey + " or lower(xc.campaign_name) like " + searchKey
					+ " or lower(xpc.campaign_name) like " + searchKey + " or lower(xps.stage_name) like " + searchKey
					+ ") ";
		}

		queryString = setDealFilterQuery(queryString, pagination);
		return queryString;
	}

	private HibernateSQLQueryResultRequestDTO frameHibernateSQLResultRequestDTO(Pagination pagination,
			String dataQuery) {
		String countQuery = "select cast(count(xd.id) as int) ";
		String queryString = frameDealsDataQueryString(pagination);
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(dataQuery + queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(COMPANY_ID, pagination.getCompanyId()));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("companyJourneyId", pagination.getContactId()));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("loggedInUserId", pagination.getUserId()));
		if (pagination.getVendorCompanyId() != null && pagination.getVendorCompanyId() > 0) {
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("createdForCompanyId", pagination.getVendorCompanyId()));
		}
		setParametersForDealFilterQuery(hibernateSQLQueryResultRequestDTO, pagination);
		hibernateSQLQueryResultRequestDTO.setClassInstance(DealResponseDTO.class);
		hibernateSQLQueryResultRequestDTO.setSortQueryString(orderDealsBy);
		hibernateSQLQueryResultRequestDTO.setRowCountQueryString(countQuery + queryString);
		return hibernateSQLQueryResultRequestDTO;
	}

	private void setParametersForDealFilterQuery(HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO,
			Pagination pagination) {
		if (pagination.getFromDateFilter() != null) {
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("fromDate", pagination.getFromDateFilter()));
		}
		if (pagination.getToDateFilter() != null) {
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("toDate", pagination.getToDateFilter()));
		}

		if (!StringUtils.isBlank(pagination.getStageFilter())) {
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("currentStageName", pagination.getStageFilter()));
		}

		if (XamplifyUtils.isValidInteger(pagination.getRegisteredByUserId())) {
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("registeredByUserId", pagination.getRegisteredByUserId()));
		}

		if (XamplifyUtils.isValidInteger(pagination.getRegisteredByCompanyId())) {
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("registeredByCompanyId", pagination.getRegisteredByCompanyId()));
		}
	}

	@Override
	public Double fetchTotalDealAmount(ContactOpportunityRequestDTO contactOpportunityRequestDTO) {
		String vanityFilterQuery = "";
		if (contactOpportunityRequestDTO.isVanityUrlFilter()) {
			vanityFilterQuery = AND_D_CREATED_FOR_COMPANY_ID_VENDOR_COMPANY_ID;
		}
		String sqlQueryString = "select sum(d.amount) from xt_user_list ul,xt_user_userlist uul, xt_deal d where ul.associated_company_id = :contactId and ul.user_list_id = uul.user_list_id "
				+ "and uul.user_id = d.associated_user_id and d.created_by_company_id = :companyId and ul.company_id = :companyId "
				+ vanityFilterQuery;
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(sqlQueryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("contactId", contactOpportunityRequestDTO.getContactId()));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(COMPANY_ID, contactOpportunityRequestDTO.getLoggedInUserCompanyId()));
		if (contactOpportunityRequestDTO.isVanityUrlFilter()) {
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("vendorCompanyId", contactOpportunityRequestDTO.getVendorCompanyId()));
		}
		return (Double) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public DealDto fetchMergeTagsDataForPartnerMailNotification(Integer dealId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String queryString = "select ps.stage_name as \"currentStageName\", ccp.company_name as \"createdForCompanyName\", ccp.company_profile_name as \"createdForcompanyProfileName\", "
				+ "concat(up.firstname, case when up.firstname is not null and up.middle_name is not null then concat(' ',up.middle_name) else up.middle_name end, "
				+ "case when up.firstname is not null or up.middle_name is not null then concat(' ',up.lastname) else up.lastname end) as \"createdByName\", pcp.website as \"website\", "
				+ "pcp.company_name as \"createdByCompanyName\", pcp.company_logo as \"partnerCompanyLogoPath\", concat(l.first_name, ' ', l.last_name) as \"leadName\", l.company as \"leadCompany\", "
				+ "d.title as \"title\", d.amount as \"amount\" "
				+ "from xt_deal d left join xt_lead l on l.id = d.associated_lead_id "
				+ "join xt_company_profile ccp on d.created_for_company_id = ccp.company_id "
				+ "join xt_user_profile up on d.updated_by = up.user_id join xt_pipeline_stage ps on d.pipeline_stage_id = ps.id "
				+ "join xt_company_profile pcp on d.created_by_company_id = pcp.company_id where d.id = :dealId";
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("dealId", dealId));
		return (DealDto) hibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO, DealDto.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<DealDto> fetchDealsForContactReport(String dynamicQueryCondition, Integer createdByCompanyId,
			Integer createdForCompanyId, Integer limit) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String vendorCompanyIdQueryString = "";
		if (XamplifyUtils.isValidInteger(createdForCompanyId)) {
			vendorCompanyIdQueryString = "  and d.created_for_company_id = :createdForCompanyId";
		}
		String partnerCompanyIdQueryString = " and d.created_by_company_id = :createdByCompanyId ";
		if (Objects.equals(createdByCompanyId, createdForCompanyId)) {
			partnerCompanyIdQueryString = "";
		}
		String sql = "WITH integration_form_type AS ( SELECT  d.id AS deal_id, " + " COALESCE( ( SELECT CASE (xi.type) "
				+ " WHEN 'salesforce' THEN 'SALES_FORCE_CUSTOM_FORM' " + " WHEN 'hubspot' THEN 'HUBSPOT_CUSTOM_FORM' "
				+ " WHEN 'pipedrive' THEN 'PIPEDRIVE_CUSTOM_FORM' "
				+ " WHEN 'connectwise' THEN 'CONNECTWISE_CUSTOM_FORM' " + " WHEN 'halopsa' THEN 'HALOPSA_CUSTOM_FORM' "
				+ " WHEN 'zoho' THEN 'ZOHO_CUSTOM_FORM' " + " ELSE 'XAMPLIFY_DEAL_CUSTOM_FORM'  END "
				+ " FROM xt_integration xi " + " WHERE xi.company_id = d.created_for_company_id "
				+ " AND xi.active  ORDER BY xi.id DESC " + " ),  'XAMPLIFY_DEAL_CUSTOM_FORM'  ) AS form_type "
				+ " FROM xt_deal d "
				+ ") select d.id as \"id\", d.title as \"title\", d.amount as \"amount\", cast(d.close_date as text) as \"closeDateString\", l.crm_reference_id as \"referenceId\", "
				+ "cbcp.company_name as \"createdByCompanyName\", cfcp.company_name as \"createdForCompanyName\", c.campaign_name as \"campaignName\", "
				+ "cast(d.created_time as text) as \"createdTime\", concat( up.firstname, case "
				+ "when up.firstname is not null and up.middle_name is not null then concat(' ', up.middle_name) "
				+ "else up.middle_name end, case "
				+ "when up.firstname is not null or up.middle_name is not null then concat(' ', up.lastname) "
				+ "else up.lastname end) as \"createdByName\", cast(cf.custom_fields as text) as \"customFields\", "
				+ "cfp.name as \"createdForPipeline\", cbp.name as \"createdByPipeline\", "
				+ "cfps.stage_name as \"createdForPipelineStage\", "
				+ "cbps.stage_name as \"createdByPipelineStage\", a.\"contactName\" as \"contactName\", a.\"contactEmailId\" as \"contactEmailId\" from xt_deal d "
				+ "left join xt_lead l on d.associated_lead_id = l.id "
				+ "left join xt_campaign c on d.campaign_id = c.campaign_id "
				+ "join xt_company_profile cbcp on d.created_by_company_id = cbcp.company_id "
				+ "join xt_company_profile cfcp on d.created_for_company_id = cfcp.company_id "
				+ "join xt_user_profile up on d.created_by = up.user_id "
				+ "join xt_pipeline cfp on d.created_for_pipeline_id = cfp.id "
				+ "left join xt_pipeline cbp on d.created_by_pipeline_id = cbp.id "
				+ "join xt_pipeline_stage cfps on d.created_for_pipeline_stage_id = cfps.id "
				+ "left join xt_pipeline_stage cbps on d.created_by_pipeline_stage_id = cbps.id join ( "
				+ "select distinct(uul.user_id) as \"id\", concat(up.firstname, case when up.firstname is not null and up.middle_name is not null then concat(' ', up.middle_name) "
				+ " else up.middle_name end, case when up.firstname is not null or up.middle_name is not null then concat(' ', up.lastname) "
				+ " else up.lastname end ) as \"contactName\", up.email_id as \"contactEmailId\" from xt_user_list ul "
				+ "join xt_user_userlist uul on ul.user_list_id = uul.user_list_id "
				+ "join xt_user_profile up on uul.user_id = up.user_id "
				+ "where ul.company_id = :companyId and ul.module_name ='CONTACTS'  " + dynamicQueryCondition
				+ ") a on d.associated_user_id = a.id LEFT JOIN integration_form_type ai ON ai.deal_id = d.id "
				+ "LEFT JOIN LATERAL ( "
				+ "  SELECT jsonb_object_agg(fl.label_name, COALESCE(scd.value, '')) AS custom_fields "
				+ "  FROM xt_form f JOIN xt_form_label fl ON f.id = fl.form_id " + "  LEFT JOIN xt_sf_cf_data scd "
				+ "         ON fl.id = scd.sf_cf_label_id AND scd.deal_id = d.id "
				+ "  WHERE f.company_id = d.created_for_company_id\r\n"
				+ "    AND cast(f.form_type as text) = ai.form_type AND f.form_sub_type IS NULL "
				+ "    AND (fl.form_field_type = 'CUSTOM' OR fl.form_field_type IS NULL) ) cf ON TRUE " + "where 1 = 1 "
				+ partnerCompanyIdQueryString + vendorCompanyIdQueryString + " order by d.id desc";
		if (XamplifyUtils.isValidInteger(limit)) {
			sql += " limit " + limit;
		}
		hibernateSQLQueryResultRequestDTO.setQueryString(sql);
		if (XamplifyUtils.isValidInteger(createdForCompanyId)) {
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("createdForCompanyId", createdForCompanyId));
		}
		if (!Objects.equals(createdByCompanyId, createdForCompanyId)) {
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("createdByCompanyId", createdByCompanyId));
		}
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(COMPANY_ID, createdByCompanyId));
		return (List<DealDto>) hibernateSQLQueryResultUtilDao.getListDto(hibernateSQLQueryResultRequestDTO,
				DealDto.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<DealDto> fetchDealsForContact(Integer contactId, Integer userListId, Integer createdByCompanyId,
			Integer createdForCompanyId) {
		HibernateSQLQueryResultRequestDTO requestDTO = new HibernateSQLQueryResultRequestDTO();
		String vendorCompanyIdQueryString = "";
		if (XamplifyUtils.isValidInteger(createdForCompanyId)) {
			vendorCompanyIdQueryString = "  and d.created_for_company_id = :createdForCompanyId";
		}
		String partnerCompanyIdQueryString = " and d.created_by_company_id = :createdByCompanyId ";
		if (Objects.equals(createdByCompanyId, createdForCompanyId)) {
			partnerCompanyIdQueryString = "";
		}
		String condition = " and uul.user_id = :contactId and ul.user_list_id = :userListId";
		String sql = "WITH integration_form_type AS (SELECT d.id AS deal_id, " + " COALESCE( ( SELECT CASE (xi.type) "
				+ " WHEN 'salesforce' THEN 'SALES_FORCE_CUSTOM_FORM' " + " WHEN 'hubspot' THEN 'HUBSPOT_CUSTOM_FORM' "
				+ " WHEN 'pipedrive' THEN 'PIPEDRIVE_CUSTOM_FORM' "
				+ " WHEN 'connectwise' THEN 'CONNECTWISE_CUSTOM_FORM' " + " WHEN 'halopsa' THEN 'HALOPSA_CUSTOM_FORM' "
				+ " WHEN 'zoho' THEN 'ZOHO_CUSTOM_FORM' " + " ELSE 'XAMPLIFY_DEAL_CUSTOM_FORM'  END "
				+ " FROM xt_integration xi " + " WHERE xi.company_id = d.created_for_company_id AND xi.active "
				+ " ORDER BY xi.id DESC ), " + " 'XAMPLIFY_DEAL_CUSTOM_FORM'  ) AS form_type FROM xt_deal d "
				+ ") select d.id as \"id\", d.title as \"title\", d.amount as \"amount\", cast(d.close_date as text) as \"closeDateString\", l.crm_reference_id as \"referenceId\", "
				+ "cbcp.company_name as \"createdByCompanyName\", cfcp.company_name as \"createdForCompanyName\", c.campaign_name as \"campaignName\", "
				+ "cast(d.created_time as text) as \"createdTime\", concat( up.firstname, case "
				+ "when up.firstname is not null and up.middle_name is not null then concat(' ', up.middle_name) "
				+ "else up.middle_name end, case "
				+ "when up.firstname is not null or up.middle_name is not null then concat(' ', up.lastname) "
				+ "else up.lastname end) as \"createdByName\", cast(cf.custom_fields as text) as \"customFields\", "
				+ "cfp.name as \"createdForPipeline\", cbp.name as \"createdByPipeline\", "
				+ "cfps.stage_name as \"createdForPipelineStage\", "
				+ "cbps.stage_name as \"createdByPipelineStage\", a.\"contactName\" as \"contactName\", a.\"contactEmailId\" as \"contactEmailId\" from xt_deal d "
				+ "left join xt_lead l on d.associated_lead_id = l.id "
				+ "left join xt_campaign c on d.campaign_id = c.campaign_id "
				+ "join xt_company_profile cbcp on d.created_by_company_id = cbcp.company_id "
				+ "join xt_company_profile cfcp on d.created_for_company_id = cfcp.company_id "
				+ "join xt_user_profile up on d.created_by = up.user_id "
				+ "join xt_pipeline cfp on d.created_for_pipeline_id = cfp.id "
				+ "left join xt_pipeline cbp on d.created_by_pipeline_id = cbp.id "
				+ "join xt_pipeline_stage cfps on d.created_for_pipeline_stage_id = cfps.id "
				+ "left join xt_pipeline_stage cbps on d.created_by_pipeline_stage_id = cbps.id join ( "
				+ "select distinct(uul.user_id) as \"id\", concat(up.firstname, case when up.firstname is not null and up.middle_name is not null then concat(' ', up.middle_name) "
				+ " else up.middle_name end, case when up.firstname is not null or up.middle_name is not null then concat(' ', up.lastname) "
				+ " else up.lastname end ) as \"contactName\", up.email_id as \"contactEmailId\" from xt_user_list ul "
				+ "join xt_user_userlist uul on ul.user_list_id = uul.user_list_id "
				+ "join xt_user_profile up on uul.user_id = up.user_id "
				+ "where ul.company_id = :companyId and ul.module_name ='CONTACTS'  " + condition
				+ " ) a on d.associated_user_id = a.id " + " LEFT JOIN integration_form_type ai ON ai.deal_id = d.id "
				+ " LEFT JOIN LATERAL ( "
				+ "  SELECT jsonb_object_agg(fl.label_name, COALESCE(scd.value, '')) AS custom_fields "
				+ "  FROM xt_form f JOIN xt_form_label fl ON f.id = fl.form_id " + "  LEFT JOIN xt_sf_cf_data scd "
				+ "         ON fl.id = scd.sf_cf_label_id AND scd.deal_id = d.id "
				+ "  WHERE f.company_id = d.created_for_company_id "
				+ "    AND cast(f.form_type as text) = ai.form_type AND f.form_sub_type IS NULL "
				+ "    AND (fl.form_field_type = 'CUSTOM' OR fl.form_field_type IS NULL)  ) cf ON TRUE "
				+ "where 1 = 1 " + partnerCompanyIdQueryString + vendorCompanyIdQueryString + " order by d.id desc";

		requestDTO.setQueryString(sql);
		if (!Objects.equals(createdByCompanyId, createdForCompanyId)) {
			requestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("createdByCompanyId", createdByCompanyId));
		}
		requestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(COMPANY_ID, createdByCompanyId));
		requestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("contactId", contactId));
		requestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("userListId", userListId));
		if (XamplifyUtils.isValidInteger(createdForCompanyId)) {
			requestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("createdForCompanyId", createdForCompanyId));
		}
		return (List<DealDto>) hibernateSQLQueryResultUtilDao.getListDto(requestDTO, DealDto.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<DealDto> fetchDealsForPartnerReport(Integer createdByCompanyId, Integer createdForCompanyId,
			Integer userListId, Integer queryLimit) {
		HibernateSQLQueryResultRequestDTO requestDTO = new HibernateSQLQueryResultRequestDTO();
		String createdByCompanyQuery = "";
		String partnersCompanyJoinQuery = "";
		if (XamplifyUtils.isValidInteger(createdByCompanyId)) {
			createdByCompanyQuery = " d.created_by_company_id = :createdByCompanyId AND ";
		} else {
			partnersCompanyJoinQuery = " JOIN  (SELECT DISTINCT up.company_id " + " FROM xt_user_list ul "
					+ " JOIN xt_user_userlist uul ON uul.user_list_id = ul.user_list_id "
					+ " JOIN xt_user_profile up ON up.user_id = uul.user_id " + " WHERE ul.module_name = 'PARTNERS' "
					+ " AND ul.user_list_id = :userListId " + " AND up.company_id IS NOT NULL) sk "
					+ " ON sk.company_id = d.created_by_company_id ";
		}
		String sql = "WITH integration_form_type AS ( SELECT d.id AS deal_id, COALESCE( ( "
				+ "SELECT CASE (xi.type) WHEN 'salesforce' THEN 'SALESFORCE_CUSTOM_FORM' WHEN 'hubspot' THEN 'HUBSPOT_CUSTOM_FORM' "
				+ "WHEN 'pipedrive' THEN 'PIPEDRIVE_CUSTOM_FORM' WHEN 'connectwise' THEN 'CONNECTWISE_CUSTOM_FORM' "
				+ "WHEN 'halopsa' THEN 'HALOPSA_CUSTOM_FORM' WHEN 'zoho' THEN 'ZOHO_CUSTOM_FORM' "
				+ "ELSE 'XAMPLIFY_DEAL_CUSTOM_FORM' END FROM xt_integration xi WHERE xi.company_id = d.created_for_company_id "
				+ "AND xi.active ORDER BY xi.id DESC ), 'XAMPLIFY_DEAL_CUSTOM_FORM') AS form_type FROM xt_deal d ) "
				+ "SELECT d.id as \"id\", d.title as \"title\", d.amount as \"amount\", cast(d.close_date as text) as \"closeDateString\", "
				+ "l.crm_reference_id as \"referenceId\", cbcp.company_name as \"createdByCompanyName\", cfcp.company_name as \"createdForCompanyName\", "
				+ "c.campaign_name as \"campaignName\", cast(d.created_time as text) as \"createdTime\", "
				+ "concat(up.firstname, case when up.firstname is not null and up.middle_name is not null then concat(' ', up.middle_name) else up.middle_name end, "
				+ "case when up.firstname is not null or up.middle_name is not null then concat(' ', up.lastname) else up.lastname end) as \"createdByName\", "
				+ "cast(cf.custom_fields as text) as \"customFields\", cfp.name as \"createdForPipeline\", cbp.name as \"createdByPipeline\", "
				+ "cfps.stage_name as \"createdForPipelineStage\", cbps.stage_name as \"createdByPipelineStage\", up.email_id as \"createdByEmail\" "
				+ "FROM xt_deal d LEFT JOIN xt_lead l ON d.associated_lead_id = l.id "
				+ "LEFT JOIN xt_campaign c ON d.campaign_id = c.campaign_id "
				+ "JOIN xt_company_profile cbcp ON d.created_by_company_id = cbcp.company_id "
				+ "JOIN xt_company_profile cfcp ON d.created_for_company_id = cfcp.company_id "
				+ "JOIN xt_user_profile up ON d.created_by = up.user_id "
				+ "JOIN xt_pipeline cfp ON d.created_for_pipeline_id = cfp.id "
				+ "LEFT JOIN xt_pipeline cbp ON d.created_by_pipeline_id = cbp.id "
				+ "JOIN xt_pipeline_stage cfps ON d.created_for_pipeline_stage_id = cfps.id "
				+ "LEFT JOIN xt_pipeline_stage cbps ON d.created_by_pipeline_stage_id = cbps.id "
				+ "LEFT JOIN integration_form_type ai ON ai.deal_id = d.id "
				+ "LEFT JOIN LATERAL ( SELECT jsonb_object_agg(fl.label_name, COALESCE(scd.value, '')) AS custom_fields "
				+ "FROM xt_form f JOIN xt_form_label fl ON f.id = fl.form_id "
				+ "LEFT JOIN xt_sf_cf_data scd ON fl.id = scd.sf_cf_label_id AND scd.deal_id = d.id "
				+ "WHERE f.company_id = d.created_for_company_id AND cast(f.form_type as text) = ai.form_type "
				+ "AND f.form_sub_type IS NULL AND (fl.form_field_type = 'CUSTOM' OR fl.form_field_type IS NULL) ) cf ON TRUE "
				+ partnersCompanyJoinQuery + " WHERE " + createdByCompanyQuery
				+ " d.created_for_company_id = :createdForCompanyId " + "ORDER BY d.id DESC";

		if (XamplifyUtils.isValidInteger(queryLimit)) {
			sql += " limit " + queryLimit;
		}

		requestDTO.setQueryString(sql);
		if (XamplifyUtils.isValidInteger(createdByCompanyId)
				&& !Objects.equals(createdByCompanyId, createdForCompanyId)) {
			requestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("createdByCompanyId", createdByCompanyId));
		} else {
			requestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("userListId", userListId));
		}
		if (XamplifyUtils.isValidInteger(createdForCompanyId)) {
			requestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("createdForCompanyId", createdForCompanyId));
		}
		return (List<DealDto>) hibernateSQLQueryResultUtilDao.getListDto(requestDTO, DealDto.class);
	}

	@Override
	public void getUserUserDetailsForDeal(DealDto dealDto, Integer partnerId, Integer vendorId) {
		Session session = sessionFactory.getCurrentSession();

		String sql = "select uul.account_sub_type, uul.partner_type " + "from xt_user_list ul "
				+ "left join xt_user_userlist  uul on ul.user_list_id= uul.user_list_id "
				+ "left join xt_user_profile xup on xup.user_id= uul.user_id "
				+ "where ul.module_name = 'PARTNERS' and  " + "ul.is_default_partnerlist = true "
				+ " and xup.company_id = :partnerId and ul.company_id = :vendorId ";

		Query query = session.createSQLQuery(sql);
		query.setParameter("partnerId", partnerId);
		query.setParameter("vendorId", vendorId);

		@SuppressWarnings("unchecked")
		List<Object[]> resultList = query.list();

		if (resultList != null && !resultList.isEmpty()) {
			Object[] row = resultList.get(0);

			if (row[0] != null) {
				dealDto.setAccountSubType(row[0].toString());
			}
			if (row[1] != null) {
				dealDto.setPartnerType(row[1].toString());
			}
		}

	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Deal> findDealsByCreatedForCompanyId(Integer createdForCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(Deal.class);
		criteria.add(Restrictions.eq("createdForCompany.id", createdForCompanyId));
		criteria.addOrder(Order.desc("id"));
		return criteria.list();
	}

}
