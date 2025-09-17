package com.xtremand.lead.dao.hibernate;

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
import com.xtremand.campaign.bom.ModuleAccess;
import com.xtremand.common.bom.Pagination;
import com.xtremand.integration.bom.Integration.IntegrationType;
import com.xtremand.lead.bom.Lead;
import com.xtremand.lead.bom.LeadCustomField;
import com.xtremand.lead.bom.LeadField;
import com.xtremand.lead.dao.LeadDAO;
import com.xtremand.lead.dto.ContactLeadResponseDTO;
import com.xtremand.lead.dto.LeadCountsResponseDTO;
import com.xtremand.lead.dto.LeadCustomFieldDto;
import com.xtremand.lead.dto.LeadDto;
import com.xtremand.lead.dto.LeadResponseDTO;
import com.xtremand.partnership.bom.Partnership.PartnershipStatus;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.HibernateSQLQueryResultUtilDao;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.QueryParameterDTO;
import com.xtremand.util.dto.TeamMemberFilterDTO;
import com.xtremand.util.dto.UserDetailsUtilDTO;
import com.xtremand.util.dto.XamplifyConstants;

@Repository("LeadDAO")
@Transactional
public class HibernateLeadDAO implements LeadDAO {

	private static final String VANITY_FILTER_QUERY = "{vanityFilterQuery}";

	/*** XNFR-505 ***/
	@Value("${getLeadsForLeadAttachment}")
	private String getLeadsForLeadAttachment;

	@Value("${sortLeadsForLeadAttachment}")
	private String sortLeadsForLeadAttachment;

	@Value("${getSelectedVendorsLeadsForLeadAttachment}")
	private String getSelectedVendorsLeadsForLeadAttachment;

	/*** XNFR-649 ***/
	@Value("${leadResponseDtoSQLQuery}")
	private String leadResponseDtoSQLQuery;

	@Value("${enableRegisterDealButtonQuery}")
	private String enableRegisterDealButtonQuery;

	@Value("${showRegisterDealQuery}")
	private String showRegisterDealQuery;

	@Value("${checkAssociatedCampaignDeletedQuery}")
	private String checkAssociatedCampaignDeletedQuery;

	@Value("${canUpdateQuery}")
	private String canUpdateQuery;

	@Value("${isSelfLead}")
	private String isSelfLead;

	@Value("${canUpdate}")
	private String canUpdate;

	@Value("${unReadChatCountQuery}")
	private String unReadChatCountQuery;

	@Value("${leadFROMClause}")
	private String leadFROMClause;

	@Value("${enableRegisterDealButtonJoinCondition}")
	private String enableRegisterDealButtonJoinCondition;

	@Value("${showRegisterDealJoinCondition}")
	private String showRegisterDealJoinCondition;

	@Value("${canUpdateJoinCondition}")
	private String canUpdateJoinCondition;

	@Value("${unReadChatCountJoinCondition}")
	private String unReadChatCountJoinCondition;

	@Value("${orderLeadsBy}")
	private String orderLeadsBy;

	@Value("${leadTileCountsQuery}")
	private String leadTileCountsQuery;

	@Value("${versa.custom.field.deal.reg.status}")
	String versaDealRegStatusCustomField;

	/** XNFR-882 **/
	@Value("${company.leads.from.and.where.clause.data.query}")
	private String companyLeadsFromAndWhereClauseDataQuery;

	@Value("${company.leads.from.and.where.clause.count.query}")
	private String companyLeadsFromAndWhereClauseCountQuery;

	@Value("${contact.leads.from.and.where.clause.data.query}")
	private String contactLeadsFromAndWhereClauseDataQuery;

	@Value("${contact.leads.from.and.where.clause.count.query}")
	private String contactLeadsFromAndWhereClauseCountQuery;

	@Value("${leads.limited.data.query}")
	private String leadsLimitedDataQuery;

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private UtilDao utilDao;

	@Autowired
	private HibernateSQLQueryResultUtilDao hibernateSQLQueryResultUtilDao;

	private static final String CREATED_FOR_COMPANY_ID = "createdForCompany.id";
	private static final String CREATED_BY_COMPANY_ID = "createdByCompany.id";

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getLeadsForPartner(Pagination pagination) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(Lead.class, "L");
		criteria.add(Restrictions.eq(CREATED_BY_COMPANY_ID, pagination.getCompanyId()));
		if (pagination.isIgnoreSelfLeadsOrDeals() && !XamplifyUtils.isValidInteger(pagination.getContactId())) {
			criteria.add(Restrictions.ne(CREATED_FOR_COMPANY_ID, pagination.getCompanyId()));
		}

		if (pagination.getVendorCompanyId() != null && pagination.getVendorCompanyId() > 0) {
			criteria.add(Restrictions.eq(CREATED_FOR_COMPANY_ID, pagination.getVendorCompanyId()));
		}

		criteria.createAlias("L.currentStage", "CS", JoinType.LEFT_OUTER_JOIN);
		criteria.createAlias("L.associatedDeal", "AD", JoinType.LEFT_OUTER_JOIN);
		if (StringUtils.isNotBlank(pagination.getFilterKey())) {
			if ("won".equals(pagination.getFilterKey())) {
				criteria.add(Restrictions.eq("CS.won", true));
			} else if ("lost".equals(pagination.getFilterKey())) {
				criteria.add(Restrictions.eq("CS.lost", true));
			} else if ("converted".equals(pagination.getFilterKey())) {
				criteria.add(Restrictions.isNotNull("AD.id"));
			} else if ("not-converted".equals(pagination.getFilterKey())) {
				criteria.add(Restrictions.isNull("AD.id"));
			}
		}

		if (pagination.getSearchKey() != null && !pagination.getSearchKey().isEmpty()) {
			// criteria.createAlias("L.campaign", "C", JoinType.LEFT_OUTER_JOIN);\
			criteria.createAlias("L.createdByCompany", "CBC", JoinType.LEFT_OUTER_JOIN);
			criteria.createAlias("L.createdForCompany", "CFC", JoinType.LEFT_OUTER_JOIN);

			Disjunction disjunction = Restrictions.disjunction();
			disjunction.add(Restrictions.ilike("email", pagination.getSearchKey(), MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("firstName", pagination.getSearchKey(), MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("lastName", pagination.getSearchKey(), MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("company", pagination.getSearchKey(), MatchMode.ANYWHERE));
			// disjunction.add(Restrictions.ilike("C.campaign", pagination.getSearchKey(),
			// MatchMode.ANYWHERE));

			disjunction.add(Restrictions.ilike("CFC.companyName", pagination.getSearchKey(), MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("CBC.companyName", pagination.getSearchKey(), MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("CS.stageName", pagination.getSearchKey(), MatchMode.ANYWHERE));
			criteria.add(disjunction);
		}

		if (XamplifyUtils.isValidInteger(pagination.getContactId())) {
			criteria.add(Restrictions.eq("associatedUser.userId", pagination.getContactId()));
		}

		addFilters(pagination, criteria);
		criteria.addOrder(Order.desc("id"));

		ScrollableResults scrollableResults = criteria.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;

		if (!pagination.isExcludeLimit()) {
			criteria.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
			criteria.setMaxResults(pagination.getMaxResults());
		}

		List<Lead> leadList = criteria.list();
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("totalRecords", totalRecords);
		resultMap.put("data", leadList);
		return resultMap;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getLeadsForVendor(Pagination pagination) {
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), false);
		Map<String, Object> resultMap = new HashMap<>();
		Integer totalRecords = 0;
		List<Lead> leadList = new ArrayList<>();
		if (!teamMemberFilterDTO.isEmptyFilter()) {
			Session session = sessionFactory.getCurrentSession();
			Criteria criteria = session.createCriteria(Lead.class, "L");
			criteria.add(Restrictions.eq(CREATED_FOR_COMPANY_ID, pagination.getCompanyId()));
			criteria.createAlias("L.currentStage", "CS", JoinType.LEFT_OUTER_JOIN);
			criteria.createAlias("L.associatedDeal", "AD", JoinType.LEFT_OUTER_JOIN);
			if (StringUtils.isNotBlank(pagination.getFilterKey())) {
				if ("won".equals(pagination.getFilterKey())) {
					criteria.add(Restrictions.eq("CS.won", true));
				} else if ("lost".equals(pagination.getFilterKey())) {
					criteria.add(Restrictions.eq("CS.lost", true));
				} else if ("converted".equals(pagination.getFilterKey())) {
					criteria.add(Restrictions.isNotNull("AD.id"));
				}
			}

			if (teamMemberFilterDTO.isApplyTeamMemberFilter()) {
				criteria.add(Restrictions.in(CREATED_BY_COMPANY_ID,
						teamMemberFilterDTO.getPartnershipIdsOrPartnerCompanyIds()));
			}

			addSearchFilter(pagination, criteria);

			addFilters(pagination, criteria);

			criteria.addOrder(Order.desc("id"));

			ScrollableResults scrollableResults = criteria.scroll();
			scrollableResults.last();
			totalRecords = scrollableResults.getRowNumber() + 1;

			if (!pagination.isExcludeLimit()) {
				criteria.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
				criteria.setMaxResults(pagination.getMaxResults());
			}

			leadList = criteria.list();
		}

		resultMap.put("totalRecords", totalRecords);
		resultMap.put("data", leadList);
		return resultMap;
	}

	public void getUserUserDetailsForLead(LeadDto leadDto, Integer partnerId, Integer vendorId) {
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
				leadDto.setAccountSubType(row[0].toString());
			}
			if (row[1] != null) {
				leadDto.setPartnerType(row[1].toString());
			}
		}
	}

	/*
	 * public void getUserUserDetailsByLeadForComments(CampaignDealCommentDTO
	 * commentDTO, Integer partnerId, Integer vendorId) { Session session =
	 * sessionFactory.getCurrentSession();
	 * 
	 * String sql = "select uul.account_sub_type, uul.partner_type " +
	 * "from xt_user_list ul " +
	 * "left join xt_user_userlist  uul on ul.user_list_id= uul.user_list_id  " +
	 * "where ul.module_name = 'PARTNERS' and  " +
	 * "ul.is_default_partnerlist = true " + "and uul.user_id = :partnerId " +
	 * "and ul.company_id = :vendorId ";
	 * 
	 * Query query = session.createSQLQuery(sql); query.setParameter("partnerId",
	 * partnerId); query.setParameter("vendorId", vendorId);
	 * 
	 * @SuppressWarnings("unchecked") List<Object[]> resultList = query.list();
	 * 
	 * if (resultList != null && !resultList.isEmpty()) { Object[] row =
	 * resultList.get(0);
	 * 
	 * if (row[0] != null) { commentDTO.setAccountSubType(row[0].toString()); } if
	 * (row[1] != null) { commentDTO.setPartnerType(row[1].toString()); } } }
	 */
	private void addFilters(Pagination pagination, Criteria criteria) {
		addFromDateFilter(pagination, criteria);
		addToDateFilter(pagination, criteria);
		addStageFilter(pagination, criteria);
		Integer registeredByCompanyId = pagination.getRegisteredByCompanyId();
		if (XamplifyUtils.isValidInteger(registeredByCompanyId)) {
			criteria.add(Restrictions.eq(CREATED_BY_COMPANY_ID, registeredByCompanyId));
		}
		Integer registeredByUserId = pagination.getRegisteredByUserId();
		if (XamplifyUtils.isValidInteger(registeredByUserId)) {
			criteria.add(Restrictions.eq("createdBy", registeredByUserId));
		}
	}

	private void addStageFilter(Pagination pagination, Criteria criteria) {
		if (!StringUtils.isBlank(pagination.getStageFilter())) {
			criteria.add(Restrictions.eq("CS.stageName", pagination.getStageFilter()));
		}
	}

	private void addToDateFilter(Pagination pagination, Criteria criteria) {
		if (pagination.getToDateFilter() != null) {
			criteria.add(Restrictions.le("L.createdTime", pagination.getToDateFilter()));
		}
	}

	private void addFromDateFilter(Pagination pagination, Criteria criteria) {
		if (pagination.getFromDateFilter() != null) {
			criteria.add(Restrictions.ge("L.createdTime", pagination.getFromDateFilter()));
		}
	}

	private void addSearchFilter(Pagination pagination, Criteria criteria) {
		if (pagination.getSearchKey() != null && !pagination.getSearchKey().isEmpty()) {
			criteria.createAlias("L.createdForCompany", "CFC", JoinType.LEFT_OUTER_JOIN);
			criteria.createAlias("L.createdByCompany", "CBC", JoinType.LEFT_OUTER_JOIN);
			Disjunction disjunction = Restrictions.disjunction();
			disjunction.add(Restrictions.ilike("email", pagination.getSearchKey(), MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("firstName", pagination.getSearchKey(), MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("lastName", pagination.getSearchKey(), MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("company", pagination.getSearchKey(), MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("CFC.companyName", pagination.getSearchKey(), MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("CBC.companyName", pagination.getSearchKey(), MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("CS.stageName", pagination.getSearchKey(), MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("L.referenceId", pagination.getSearchKey(), MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("L.sfLeadId", pagination.getSearchKey(), MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("L.pipedriveLeadId", pagination.getSearchKey(), MatchMode.ANYWHERE));
			disjunction.add(
					Restrictions.ilike("L.microsoftDynamicsLeadId", pagination.getSearchKey(), MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("L.zohoLeadId", pagination.getSearchKey(), MatchMode.ANYWHERE));

			criteria.add(disjunction);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getStageNamesForVendor(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();

		String sql = "";
		String activeCRM = getActiveCRMTypeByCompanyId(companyId);
		if (XamplifyUtils.isValidString(activeCRM) && activeCRM.equals("salesforce")) {
			boolean isPipelineStageMappedToCustomField = checkPipelineStageMappedToCustomFiled(companyId);
			if (isPipelineStageMappedToCustomField) {
				sql = "select xflc.label_choice_name from xt_form_label_choice xflc, xt_form_label xfl where xflc.form_label_id = xfl.id and xfl.form_default_field_type = 'PIPELINE_STAGE' and xfl.form_id in (select id from xt_form where form_type = 'SALES_FORCE_LEAD_CUSTOM_FORM' and company_id = "
						+ companyId + ")";
			} else {
				sql = "select distinct(ps.stage_name) from xt_lead l, xt_pipeline_stage ps "
						+ " where l.pipeline_stage_id = ps.id and created_for_company_id = " + companyId;
			}
		} else {
			sql = "select distinct(ps.stage_name) from xt_lead l, xt_pipeline_stage ps "
					+ " where l.pipeline_stage_id = ps.id and created_for_company_id = " + companyId;
		}

		Query query = session.createSQLQuery(sql);
		return (List<String>) query.list();

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Lead> getLeadsWithSfLeadIdForVendor(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(Lead.class, "D");
		criteria.add(Restrictions.eq(CREATED_FOR_COMPANY_ID, companyId));
		criteria.add(Restrictions.isNotNull("sfLeadId"));
		return criteria.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getVendorList(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String hql = "select cp.id, cp.companyName" + " from ModuleAccess ca JOIN ca.companyProfile cp "
				+ " where cp.id in (select vendorCompany.id from Partnership p where p.partnerCompany.id = :companyId and status = :status) "
				+ " and ca.companyProfile.id=cp.id and ca.enableLeads = true order by cp.companyName asc";
		Query query = session.createQuery(hql);
		query.setInteger("companyId", companyId);
		query.setParameter("status", PartnershipStatus.APPROVED);
		return query.list();
	}

	@Override
	public Lead getCampaignLead(Integer campaignId, Integer userId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(Lead.class);
		criteria.add(Restrictions.eq("campaign.id", campaignId));
		criteria.add(Restrictions.eq("associatedUser.userId", userId));
		return (Lead) criteria.uniqueResult();

	}

	@Override
	public LeadCountsResponseDTO getCountsForVendor(Integer companyId) {
		String sqlQuery = leadTileCountsQuery + " WHERE xl.created_for_company_id = :companyId";
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(sqlQuery).setParameter("companyId", companyId);
		return (LeadCountsResponseDTO) query.setResultTransformer(Transformers.aliasToBean(LeadCountsResponseDTO.class))
				.uniqueResult();
	}

	@Override
	public LeadCountsResponseDTO getCountsForPartner(Integer companyId) {
		String sqlQuery = leadTileCountsQuery + " WHERE xl.created_by_company_id = :companyId";
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(sqlQuery).setParameter("companyId", companyId);
		return (LeadCountsResponseDTO) query.setResultTransformer(Transformers.aliasToBean(LeadCountsResponseDTO.class))
				.uniqueResult();
	}

	@Override
	public LeadCountsResponseDTO getCountsForPartnerInVanity(Integer companyId, Integer vendorCompanyId) {

		String sqlQuery = leadTileCountsQuery
				+ " WHERE xl.created_by_company_id = :companyId and xl.created_for_company_id = :createdForCompanyId";
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(sqlQuery).setParameter("companyId", companyId)
				.setParameter("createdForCompanyId", vendorCompanyId);
		return (LeadCountsResponseDTO) query.setResultTransformer(Transformers.aliasToBean(LeadCountsResponseDTO.class))
				.uniqueResult();
	}

	@Override
	public BigInteger getUnReadChatCount(Integer leadId, Integer loggedInUserId) {
		String sql = "select count(*) from xt_campaign_deal_comments dc LEFT JOIN xt_deal d on dc.deal_id = d.id where "
				+ " (dc.lead_id = " + leadId + " or d.associated_lead_id =" + leadId + ") " + "and dc.created_by !="
				+ loggedInUserId
				+ " and dc.id > (select coalesce((select comment_id from xt_campaign_deal_comments_statistics where lead_id = "
				+ leadId + " and created_by =" + loggedInUserId + "), 0) as cid)";
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(sql);
		return (BigInteger) query.uniqueResult();
	}

	@Override
	public LeadCountsResponseDTO findLeadsCountByFilter(String sqlQuery, Integer companyId,
			List<Integer> partnerCompanyIds) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(sqlQuery).setParameter("companyId", companyId);

		if (partnerCompanyIds != null && !partnerCompanyIds.isEmpty()) {
			query.setParameterList("partnerCompanyIds", partnerCompanyIds);
		}

		return (LeadCountsResponseDTO) query.setResultTransformer(Transformers.aliasToBean(LeadCountsResponseDTO.class))
				.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getStageNamesForPartner(Integer partnerCompanyId, Integer vendorCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "";
		if (vendorCompanyId != null && vendorCompanyId > 0) {
			String activeCRM = getActiveCRMTypeByCompanyId(vendorCompanyId);
			if (XamplifyUtils.isValidString(activeCRM) && activeCRM.equals("salesforce")) {
				boolean isPipelineStageMappedToCustomField = checkPipelineStageMappedToCustomFiled(vendorCompanyId);
				if (isPipelineStageMappedToCustomField) {
					sql = "select xflc.label_choice_name from xt_form_label_choice xflc, xt_form_label xfl where xflc.form_label_id = xfl.id and xfl.form_default_field_type = 'PIPELINE_STAGE' and xfl.form_id in (select id from xt_form where form_type = 'SALES_FORCE_LEAD_CUSTOM_FORM' and company_id = "
							+ vendorCompanyId + ")";
				} else {
					sql = "select distinct(ps.stage_name) from xt_lead l, xt_pipeline_stage ps "
							+ " where l.pipeline_stage_id = ps.id and created_by_company_id = " + partnerCompanyId;

					if (vendorCompanyId != null && vendorCompanyId > 0) {
						sql += " and created_for_company_id = " + vendorCompanyId;
					}
				}
			} else {
				sql = "select distinct(ps.stage_name) from xt_lead l, xt_pipeline_stage ps "
						+ " where l.pipeline_stage_id = ps.id and created_by_company_id = " + partnerCompanyId;

				if (vendorCompanyId != null && vendorCompanyId > 0) {
					sql += " and created_for_company_id = " + vendorCompanyId;
				}
			}
		} else {
			sql = "select distinct(ps.stage_name) from xt_lead l, xt_pipeline_stage ps "
					+ " where l.pipeline_stage_id = ps.id and created_by_company_id = " + partnerCompanyId;

			if (vendorCompanyId != null && vendorCompanyId > 0) {
				sql += " and created_for_company_id = " + vendorCompanyId;
			}
		}

		Query query = session.createSQLQuery(sql);
		return (List<String>) query.list();

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getStageNamesForPartner(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select distinct(ps.stage_name) from xt_lead l, xt_pipeline_stage ps "
				+ " where l.pipeline_stage_id = ps.id and created_by_company_id = " + companyId;
		Query query = session.createSQLQuery(sql);
		return (List<String>) query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getStgaeNamesFOrPartnerInCampaign(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select distinct(ps.stage_name) from xt_lead l, xt_pipeline_stage ps "
				+ " where l.pipeline_stage_id = ps.id and created_by_company_id = " + companyId;
		Query query = session.createSQLQuery(sql);
		return (List<String>) query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getStageNamesForCampaign(Integer campaignId, Integer companyId, boolean vanityUrlFilter) {
		Session session = sessionFactory.getCurrentSession();
		/*
		 * String sql =
		 * "select distinct(ps.stage_name) from xt_pipeline_stage ps, xt_pipeline p, xt_campaign c where "
		 * + "ps.pipeline_id = p.id and p.id = c.lead_pipeline_id and c.campaign_id = "
		 * + campaignId;
		 */

		String sql = "";
		if (vanityUrlFilter) {
			String activeCRM = getActiveCRMTypeByCompanyId(companyId);
			if (XamplifyUtils.isValidString(activeCRM) && activeCRM.equals("salesforce")) {
				boolean isPipelineStageMappedToCustomField = checkPipelineStageMappedToCustomFiled(companyId);
				if (isPipelineStageMappedToCustomField) {
					sql = "select xflc.label_choice_name from xt_form_label_choice xflc, xt_form_label xfl where xflc.form_label_id = xfl.id and xfl.form_default_field_type = 'PIPELINE_STAGE' and xfl.form_id in (select id from xt_form where form_type = 'SALES_FORCE_LEAD_CUSTOM_FORM' and company_id = "
							+ companyId + ")";
				} else {
					sql = "select distinct(ps.stage_name) from xt_lead l, xt_pipeline_stage ps "
							+ " where l.pipeline_stage_id = ps.id and l.campaign_id = " + campaignId;
				}
			} else {
				sql = "select distinct(ps.stage_name) from xt_lead l, xt_pipeline_stage ps "
						+ " where l.pipeline_stage_id = ps.id and l.campaign_id = " + campaignId;
			}

		} else {
			sql = "select distinct(ps.stage_name) from xt_lead l, xt_pipeline_stage ps "
					+ " where l.pipeline_stage_id = ps.id and l.campaign_id = " + campaignId;
		}

//		String sql = "select distinct(ps.stage_name) from xt_lead l, xt_pipeline_stage ps "
//				+ " where l.pipeline_stage_id = ps.id and l.campaign_id = " + campaignId;
		Query query = session.createSQLQuery(sql);
		return (List<String>) query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getStageNamesForParentCampaign(Integer campaignId, Integer companyId, boolean vanityUrlFilter) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "";
		if (vanityUrlFilter) {
			String activeCRM = getActiveCRMTypeByCompanyId(companyId);
			if (XamplifyUtils.isValidString(activeCRM) && activeCRM.equals("salesforce")) {
				boolean isPipelineStageMappedToCustomField = checkPipelineStageMappedToCustomFiled(companyId);
				if (isPipelineStageMappedToCustomField) {
					sql = "select xflc.label_choice_name from xt_form_label_choice xflc, xt_form_label xfl where xflc.form_label_id = xfl.id and xfl.form_default_field_type = 'PIPELINE_STAGE' and xfl.form_id in (select id from xt_form where form_type = 'SALES_FORCE_LEAD_CUSTOM_FORM' and company_id = "
							+ companyId + ")";
				} else {
					sql = "select distinct(ps.stage_name) from xt_lead l, xt_pipeline_stage ps "
							+ " where l.pipeline_stage_id = ps.id and l.campaign_id in "
							+ " (select campaign_id from xt_campaign where parent_campaign_id =" + campaignId + ") ";
				}
			} else {
				sql = "select distinct(ps.stage_name) from xt_lead l, xt_pipeline_stage ps "
						+ " where l.pipeline_stage_id = ps.id and l.campaign_id in "
						+ " (select campaign_id from xt_campaign where parent_campaign_id =" + campaignId + ") ";
			}
		} else {
			sql = "select distinct(ps.stage_name) from xt_lead l, xt_pipeline_stage ps "
					+ " where l.pipeline_stage_id = ps.id and l.campaign_id in "
					+ " (select campaign_id from xt_campaign where parent_campaign_id =" + campaignId + ") ";
		}

//		String sql = "select distinct(ps.stage_name) from xt_lead l, xt_pipeline_stage ps "
//				+ " where l.pipeline_stage_id = ps.id and l.campaign_id in "
//				+ " (select campaign_id from xt_campaign where parent_campaign_id =" + campaignId + ") ";

		Query query = session.createSQLQuery(sql);
		return (List<String>) query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Lead> getLeadsWithMicrosoftDynamicsLeadIdForVendor(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(Lead.class, "D");
		criteria.add(Restrictions.eq(CREATED_FOR_COMPANY_ID, companyId));
		criteria.add(Restrictions.isNotNull("microsoftDynamicsLeadId"));
		return criteria.list();
	}

	@Override
	public List<Lead> getLeadsForVendorByType(Integer companyId, IntegrationType type) {
		List<Lead> leads = new ArrayList<Lead>();
		String colName = "";
		if (colName != "") {
			Session session = sessionFactory.getCurrentSession();
			Criteria criteria = session.createCriteria(Lead.class, "D");
			Disjunction disjunction = Restrictions.disjunction();
			disjunction.add(Restrictions.eq(CREATED_FOR_COMPANY_ID, companyId));
			disjunction.add(Restrictions.eq(CREATED_BY_COMPANY_ID, companyId));
			criteria.add(disjunction);
			criteria.add(Restrictions.isNotNull(colName));
			leads = criteria.list();
		}
		return leads;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getVendorListForLoginAsPartner(Integer companyId, Integer vendorCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		String hql = "select cp.id, cp.companyName" + " from ModuleAccess ca JOIN ca.companyProfile cp "
				+ " where cp.id in (select vendorCompany.id from Partnership p where p.partnerCompany.id = :companyId and p.vendorCompany.id = :vendorCompanyId and status = :status) "
				+ " and ca.companyProfile.id=cp.id and ca.enableLeads = true order by cp.companyName asc";
		Query query = session.createQuery(hql);
		query.setInteger("companyId", companyId);
		query.setInteger("vendorCompanyId", vendorCompanyId);
		query.setParameter("status", PartnershipStatus.APPROVED);
		return (List<Object[]>) query.list();
	}

	@Override
	public List<Lead> getLeadsByType(Integer companyId, IntegrationType type) {
		List<Lead> leads = new ArrayList<Lead>();
		String colName = "";
		if (colName != "") {
			Session session = sessionFactory.getCurrentSession();
			Criteria criteria = session.createCriteria(Lead.class, "D");
			Disjunction disjunction = Restrictions.disjunction();
			disjunction.add(Restrictions.eq(CREATED_FOR_COMPANY_ID, companyId));
			disjunction.add(Restrictions.eq(CREATED_BY_COMPANY_ID, companyId));
			criteria.add(disjunction);
			criteria.add(Restrictions.isNotNull(colName));
			leads = criteria.list();
		}
		return leads;
	}

	/*** XNFR-505 ***/
	@Override
	public Map<String, Object> getLeadsForLeadAttachment(Pagination pagination) {
		Map<String, Object> resultMap = new HashMap<>();

		String sql = getLeadsForLeadAttachment;

		sql = sql.replace("{checkPartnershipForListingLeads}",
				"left outer join xt_partnership AS xp ON xp.vendor_company_id = xl.created_for_company_id AND xp.partner_company_id = xl.created_by_company_id");
		sql = sql.replace("{checkPartnershipStatusForListingLeads}", "AND xp.status = 'approved'");

		if (pagination.getVendorCompanyId() != null && pagination.getVendorCompanyId() > 0) {
			String activeCRM = getActiveCRMTypeByCompanyId(pagination.getVendorCompanyId());
			if (activeCRM.equals("salesforce")) {
				sql = sql.replace("{stageName}",
						"COALESCE((SELECT sfcf.value FROM xt_sf_cf_data sfcf JOIN xt_form_label xfl ON sfcf.sf_cf_label_id = xfl.id WHERE sfcf.lead_id = xl.id AND xfl.form_default_field_type = 'PIPELINE_STAGE' LIMIT 1), xps.stage_name)");
			} else {
				sql = sql.replace("{stageName}", "xps.stage_name");
			}
		} else {
			sql = sql.replace("{stageName}", "xps.stage_name");
		}

		if (pagination.getVendorCompanyId() != null && pagination.getVendorCompanyId() > 0) {
			sql += getSelectedVendorsLeadsForLeadAttachment;
		}

		if (pagination.getSearchKey() != null && !pagination.getSearchKey().isEmpty()) {
			String searchKey = "'%" + pagination.getSearchKey() + "%'";

			sql += "AND (lower(xl.company) like " + searchKey + " or lower(xl.email) like " + searchKey
					+ " or lower(xl.first_name) like " + searchKey + " or lower(xl.last_name) like " + searchKey
					+ " or lower(xcp.company_name) like " + searchKey + " or lower(xps.stage_name) like " + searchKey
					+ " OR lower(COALESCE((" + "SELECT sfcf.value FROM xt_sf_cf_data sfcf "
					+ "JOIN xt_form_label xfl ON sfcf.sf_cf_label_id = xfl.id "
					+ "WHERE sfcf.lead_id = xl.id AND xfl.form_default_field_type = 'PIPELINE_STAGE' LIMIT 1"
					+ "), xps.stage_name)) LIKE " + searchKey + ") ";
		}

		if (pagination.getInvalidVendorIds() != null && !pagination.getInvalidVendorIds().isEmpty()) {
			sql += "AND xl.created_for_company_id not in (:created_for_company_ids) ";
		}

		if (XamplifyUtils.isValidInteger(pagination.getContactId())) {
			sql += "And xl.associated_user_id = :contactId ";
		}

		sql += sortLeadsForLeadAttachment;

		Session session = sessionFactory.getCurrentSession();
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("created_by_company_id", pagination.getCompanyId());
		if (pagination.getVendorCompanyId() != null && pagination.getVendorCompanyId() > 0) {
			query.setParameter("created_for_company_id", pagination.getVendorCompanyId());
		}
		if (pagination.getInvalidVendorIds() != null && !pagination.getInvalidVendorIds().isEmpty()) {
			query.setParameterList("created_for_company_ids", pagination.getInvalidVendorIds());
		}
		if (XamplifyUtils.isValidInteger(pagination.getContactId())) {
			query.setParameter("contactId", pagination.getContactId());
		}

		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		query.setMaxResults(pagination.getMaxResults());
		@SuppressWarnings("unchecked")
		List<LeadDto> list = query.setResultTransformer(Transformers.aliasToBean(LeadDto.class)).list();
		resultMap.put("totalRecords", totalRecords);
		resultMap.put("data", list);
		resultMap.put("isVendorEnabledLeadApprovalRejectionFeature",
				pagination.isLeadApprovalFeatureEnabledForVendorCompany());
		return resultMap;
	}

	@Override
	public List<LeadField> getDefaultLeadFilds() {
		List<LeadCustomFieldDto> fields = new ArrayList<LeadCustomFieldDto>();
		Session session = sessionFactory.getCurrentSession();
		String sql = " SELECT id as\"id\",label_name as \"labelName\",label_id as \"labelId\",label_type as \"labelType\"  FROM  xt_lead_fields order by id";
		Query query = session.createSQLQuery(sql);
		query.setResultTransformer(Transformers.aliasToBean(LeadField.class));
		return query.list();
	}

	@Override
	public List<LeadCustomField> getLeadCustomFields(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(LeadCustomField.class);
		criteria.add(Restrictions.eq("companyId", companyId));
		criteria.addOrder(Order.asc("displayIndex"));
		return (List<LeadCustomField>) criteria.list();
	}

	/*** XNFR-649 ***/
	public Map<String, Object> queryLeadsForPartner(Pagination pagination) {
		Map<String, Object> resultMap = new HashMap<>();
		String sql = "";

		sql = leadResponseDtoSQLQuery + enableRegisterDealButtonQuery + showRegisterDealQuery
				+ checkAssociatedCampaignDeletedQuery + canUpdateQuery + unReadChatCountQuery + leadFROMClause
				+ enableRegisterDealButtonJoinCondition + showRegisterDealJoinCondition + canUpdateJoinCondition
				+ unReadChatCountJoinCondition;

		sql += "WHERE xl.created_by_company_id = :companyId ";

		if (pagination.isIgnoreSelfLeadsOrDeals()) {
			sql += "AND xl.created_for_company_id != :companyId ";
		}

		if (pagination.getVendorCompanyId() != null && pagination.getVendorCompanyId() > 0) {
			sql += "AND xl.created_for_company_id = :createdForCompanyId ";
		}

		if (StringUtils.isNotBlank(pagination.getFilterKey())) {
			if ("won".equals(pagination.getFilterKey())) {
				sql += "AND xps.is_won is true ";
			} else if ("lost".equals(pagination.getFilterKey())) {
				sql += "AND xps.is_lost is true ";
			} else if ("converted".equals(pagination.getFilterKey())) {
				sql += "AND xd.id is not null ";
			} else if ("not-converted".equals(pagination.getFilterKey())) {
				sql += "AND xd.id is null ";
			}
		}

		if (pagination.getSearchKey() != null && !pagination.getSearchKey().isEmpty()) {
			String searchKey = "'%" + pagination.getSearchKey() + "%'";
			sql += "AND (lower(xl.company) like " + searchKey + " or lower(xl.email) like " + searchKey
					+ " or lower(xl.first_name) like " + searchKey + " or lower(xl.last_name) like " + searchKey
					+ " or lower(xcp.company_name) like " + searchKey + " or lower(xcp1.company_name) like " + searchKey
					+ " or lower(xc.campaign_name) like " + searchKey + " or lower(xpc.campaign_name) like " + searchKey
					+ " or lower(xps.stage_name) like " + searchKey + ") ";
		}

		sql = setLeadFilterQuery(sql, pagination);

		sql += orderLeadsBy;

		Session session = sessionFactory.getCurrentSession();
		SQLQuery query = session.createSQLQuery(sql);

		query.setParameter("loggedInUserId", pagination.getUserId());
		query.setParameter("companyId", pagination.getCompanyId());
		if (pagination.getVendorCompanyId() != null && pagination.getVendorCompanyId() > 0) {
			query.setParameter("createdForCompanyId", pagination.getVendorCompanyId());
		}

		setParametersForLeadFilterQuery(pagination, query);

		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		query.setMaxResults(pagination.getMaxResults());
		@SuppressWarnings("unchecked")
		List<LeadResponseDTO> list = query.setResultTransformer(Transformers.aliasToBean(LeadResponseDTO.class)).list();
		resultMap.put("totalRecords", totalRecords);
		resultMap.put("data", list);

		return resultMap;
	}

	/*** XNFR-649 ***/
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> queryLeadsForVendor(Pagination pagination) {
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), false);
		List<LeadResponseDTO> list = new ArrayList<>();
		Map<String, Object> resultMap = new HashMap<>();
		Integer totalRecords = 0;

		if (!teamMemberFilterDTO.isEmptyFilter()) {
			String sql = "";

			sql = leadResponseDtoSQLQuery + enableRegisterDealButtonQuery + showRegisterDealQuery
					+ checkAssociatedCampaignDeletedQuery + canUpdateQuery + unReadChatCountQuery + leadFROMClause
					+ enableRegisterDealButtonJoinCondition + showRegisterDealJoinCondition + canUpdateJoinCondition
					+ unReadChatCountJoinCondition;

			sql += " WHERE xl.created_for_company_id = :companyId ";

			if (StringUtils.isNotBlank(pagination.getFilterKey())) {
				if ("won".equals(pagination.getFilterKey())) {
					sql += "AND xps.is_won is true ";
				} else if ("lost".equals(pagination.getFilterKey())) {
					sql += "AND xps.is_lost is true ";
				} else if ("converted".equals(pagination.getFilterKey())) {
					sql += "AND xd.id is not null ";
				} else if ("not-converted".equals(pagination.getFilterKey())) {
					sql += "AND xd.id is null ";
				}
			}

			if (teamMemberFilterDTO.isApplyTeamMemberFilter()) {
				sql += "AND xl.created_by_company_id in (:partnershipIdsOrPartnerCompanyIds) ";
			}

			if (pagination.getSearchKey() != null && !pagination.getSearchKey().isEmpty()) {
				String searchKey = "'%" + pagination.getSearchKey() + "%'";
				sql += "AND (lower(xl.company) like " + searchKey + " or lower(xl.email) like " + searchKey
						+ " or lower(xl.first_name) like " + searchKey + " or lower(xl.last_name) like " + searchKey
						+ " or lower(xcp.company_name) like " + searchKey + " or lower(xcp1.company_name) like "
						+ searchKey + " or lower(xc.campaign_name) like " + searchKey
						+ " or lower(xpc.campaign_name) like " + searchKey + " or lower(xps.stage_name) like "
						+ searchKey + " or xl.crm_reference_id like " + searchKey + ") ";
			}

			sql = setLeadFilterQuery(sql, pagination);

			sql += orderLeadsBy;

			Session session = sessionFactory.getCurrentSession();
			SQLQuery query = session.createSQLQuery(sql);

			query.setParameter("loggedInUserId", pagination.getUserId());
			query.setParameter("companyId", pagination.getCompanyId());

			if (teamMemberFilterDTO.isApplyTeamMemberFilter()) {
				query.setParameterList("partnershipIdsOrPartnerCompanyIds",
						teamMemberFilterDTO.getPartnershipIdsOrPartnerCompanyIds());
			}

			setParametersForLeadFilterQuery(pagination, query);

			ScrollableResults scrollableResults = query.scroll();
			scrollableResults.last();
			totalRecords = scrollableResults.getRowNumber() + 1;
			query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
			query.setMaxResults(pagination.getMaxResults());
			list = query.setResultTransformer(Transformers.aliasToBean(LeadResponseDTO.class)).list();
		}

		resultMap.put("totalRecords", totalRecords);
		resultMap.put("data", list);
		return resultMap;
	}

	private String setLeadFilterQuery(String sql, Pagination pagination) {
		if (pagination.getFromDateFilter() != null) {
			sql += "AND xl.created_time >= :fromDate ";
		}

		if (pagination.getToDateFilter() != null) {
			sql += "AND xl.created_time <= :toDate ";
		}

		if (!StringUtils.isBlank(pagination.getStageFilter())) {
			sql += "AND xps.stage_name = :currentStageName ";
		}

		if (XamplifyUtils.isValidInteger(pagination.getRegisteredByCompanyId())) {
			sql += "ANd xl.created_by_company_id = :registeredByCompanyId ";
		}

		if (XamplifyUtils.isValidInteger(pagination.getRegisteredByUserId())) {
			sql += "AND xl.created_by = :registeredByUserId ";
		}
		return sql;
	}

	private void setParametersForLeadFilterQuery(Pagination pagination, SQLQuery query) {
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

	/** XNFR-553 **/
	@Override
	public Map<String, Object> findLeadsAndCountByContactId(ContactOpportunityRequestDTO contactOpportunityRequestDTO) {
		String vanityFilterQuery = "";
		String fromAndWhereClauseDataQueryString = "";
		String fromAndWhereClauseRowCountQueryString = "";
		if (contactOpportunityRequestDTO.isVanityUrlFilter()) {
			vanityFilterQuery = " and l.created_for_company_id = :vendorCompanyId";
		}
		if (Boolean.TRUE.equals(contactOpportunityRequestDTO.getIsCompanyJourney())) {
			fromAndWhereClauseDataQueryString = companyLeadsFromAndWhereClauseDataQuery.replace(VANITY_FILTER_QUERY,
					vanityFilterQuery);
			fromAndWhereClauseRowCountQueryString = companyLeadsFromAndWhereClauseCountQuery
					.replace(VANITY_FILTER_QUERY, vanityFilterQuery);
		} else {
			fromAndWhereClauseDataQueryString = contactLeadsFromAndWhereClauseDataQuery.replace(VANITY_FILTER_QUERY,
					vanityFilterQuery);
			fromAndWhereClauseRowCountQueryString = contactLeadsFromAndWhereClauseCountQuery
					.replace(VANITY_FILTER_QUERY, vanityFilterQuery);
		}
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String queryString = leadsLimitedDataQuery + fromAndWhereClauseDataQueryString;
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("companyId", contactOpportunityRequestDTO.getLoggedInUserCompanyId()));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("contactId", contactOpportunityRequestDTO.getContactId()));
		if (contactOpportunityRequestDTO.isVanityUrlFilter()) {
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("vendorCompanyId", contactOpportunityRequestDTO.getVendorCompanyId()));
		}
		hibernateSQLQueryResultRequestDTO.setClassInstance(ContactLeadResponseDTO.class);
		hibernateSQLQueryResultRequestDTO
				.setRowCountQueryString("select cast(count(*) as int) " + fromAndWhereClauseRowCountQueryString);
		Pagination pagination = new Pagination();
		pagination.setMaxResults(4);
		return hibernateSQLQueryResultUtilDao.returnPaginatedDTOList(hibernateSQLQueryResultRequestDTO, pagination, "");
	}

	/** XNFR-848 **/
	@Override
	public Map<String, Object> fetchLeadsForCompanyJourney(Pagination pagination) {
		String dataQuery = leadResponseDtoSQLQuery + enableRegisterDealButtonQuery + showRegisterDealQuery
				+ checkAssociatedCampaignDeletedQuery + canUpdateQuery + unReadChatCountQuery;
		String countQuery = "select cast(count(xl.id) as int) ";
		String queryString = getFinalQueryString(pagination);

		queryString = setLeadFilterQuery(queryString, pagination);

		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(dataQuery + queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("companyId", pagination.getCompanyId()));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("companyJourneyId", pagination.getContactId()));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("loggedInUserId", pagination.getUserId()));
		if (pagination.getVendorCompanyId() != null && pagination.getVendorCompanyId() > 0) {
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("createdForCompanyId", pagination.getVendorCompanyId()));
		}
		setParametersForLeadFilterQuery(hibernateSQLQueryResultRequestDTO, pagination);
		hibernateSQLQueryResultRequestDTO.setClassInstance(LeadResponseDTO.class);
		hibernateSQLQueryResultRequestDTO.setSortQueryString(orderLeadsBy);
		hibernateSQLQueryResultRequestDTO.setRowCountQueryString(countQuery + queryString);
		return hibernateSQLQueryResultUtilDao.returnPaginatedDTOList(hibernateSQLQueryResultRequestDTO, pagination,
				pagination.getSearchKey());
	}

	private String getFinalQueryString(Pagination pagination) {
		String queryString = leadFROMClause + enableRegisterDealButtonJoinCondition + showRegisterDealJoinCondition
				+ canUpdateJoinCondition + unReadChatCountJoinCondition + ", xt_user_list ul,xt_user_userlist uul "
				+ " where ul.associated_company_id = :companyJourneyId and ul.user_list_id = uul.user_list_id and uul.user_id = xl.associated_user_id and xl.created_by_company_id = :companyId "
				+ "and ul.company_id = :companyId ";
		if (pagination.isIgnoreSelfLeadsOrDeals() && !XamplifyUtils.isValidInteger(pagination.getContactId())) {
			queryString += "and xl.created_for_company_id != :companyId ";
		}
		if (pagination.getVendorCompanyId() != null && pagination.getVendorCompanyId() > 0) {
			queryString += "and xl.created_for_company_id = :createdForCompanyId ";
		}
		if (StringUtils.isNotBlank(pagination.getFilterKey())) {
			if ("won".equals(pagination.getFilterKey())) {
				queryString += "and xps.is_won is true ";
			} else if ("lost".equals(pagination.getFilterKey())) {
				queryString += "and xps.is_lost is true ";
			} else if ("converted".equals(pagination.getFilterKey())) {
				queryString += "and xd.id is not null ";
			} else if ("not-converted".equals(pagination.getFilterKey())) {
				queryString += "and xd.id is null ";
			}
		}
		if (pagination.getSearchKey() != null && !pagination.getSearchKey().isEmpty()) {
			String searchKey = "'%" + pagination.getSearchKey() + "%'";
			queryString += "and (lower(xl.company) like " + searchKey + " or lower(xl.email) like " + searchKey
					+ " or lower(xl.first_name) like " + searchKey + " or lower(xl.last_name) like " + searchKey
					+ " or lower(xcp.company_name) like " + searchKey + " or lower(xcp1.company_name) like " + searchKey
					+ " or lower(xc.campaign_name) like " + searchKey + " or lower(xpc.campaign_name) like " + searchKey
					+ " or lower(xps.stage_name) like " + searchKey + ") ";
		}
		return queryString;
	}

	private void setParametersForLeadFilterQuery(HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO,
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

	/** XNFR-892 **/
	@Override
	public UserDetailsUtilDTO fetchFullNameAndEmailIdByUserId(Integer userId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String queryString = "select u.email_id as \"emailId\", concat(u.firstname, case when u.firstname is not null and u.middle_name is not null then concat(' ',u.middle_name) else u.middle_name end, "
				+ "case when u.firstname is not null or u.middle_name is not null then concat(' ',u.lastname) else u.lastname end) as \"fullName\" from xt_user_profile u where u.user_id = :userId ";
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("userId", userId));
		return (UserDetailsUtilDTO) hibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO,
				UserDetailsUtilDTO.class);
	}

	@Override
	public LeadDto fetchMergeTagsDataForPartnerMailNotification(Integer leadId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String queryString = "select concat(l.first_name, ' ', l.last_name) as \"fullName\", c.campaign_name as \"campaignName\", ps.stage_name as \"currentStageName\", ccp.company_name as \"createdForCompanyName\", "
				+ "concat(up.firstname, case when up.firstname is not null and up.middle_name is not null then concat(' ',up.middle_name) else up.middle_name end, "
				+ "case when up.firstname is not null or up.middle_name is not null then concat(' ',up.lastname) else up.lastname end) as \"createdByName\", pcp.company_name as \"createdByCompanyName\", "
				+ "pcp.company_logo as \"partnerCompanyLogoPath\", pcp.website as \"website\", l.company as \"company\" "
				+ "from xt_lead l left join xt_campaign c on l.campaign_id = c.campaign_id "
				+ "join xt_company_profile ccp on l.created_for_company_id = ccp.company_id "
				+ "join xt_user_profile up on l.updated_by = up.user_id join xt_pipeline_stage ps on l.pipeline_stage_id = ps.id "
				+ "join xt_company_profile pcp on l.created_by_company_id = pcp.company_id where l.id = :leadId";
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("leadId", leadId));
		return (LeadDto) hibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO, LeadDto.class);
	}

	@Override
	public String getActiveCRMTypeByCompanyId(Integer companyId) {
		String defaultActiveCRM = "xamplify";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String sqlQuery = "select cast(type as text) from xt_integration where company_id = :companyId and active";
		hibernateSQLQueryResultRequestDTO.setQueryString(sqlQuery);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("companyId", companyId));
		String activeCRM = (String) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
		activeCRM = XamplifyUtils.isValidString(activeCRM) ? activeCRM : defaultActiveCRM;
		return activeCRM;
	}

	@Override
	public boolean checkPipelineStageMappedToCustomFiled(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "SELECT CASE WHEN  count(*) > 0 THEN true ELSE false END from xt_form_label where form_default_field_type = 'PIPELINE_STAGE' and form_id in (select id from xt_form where form_type = 'SALES_FORCE_LEAD_CUSTOM_FORM' and company_id = :companyId)";
		Query query = session.createSQLQuery(sql);
		query.setParameter("companyId", companyId);
		return (boolean) query.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> findPartnerAssosiatedTeamMembers(Integer partnerCompanyId, Integer companyId) {
		String queryString = "SELECT DISTINCT xup_team.email_id AS team_member_email FROM xt_user_profile xup_partner\r\n"
				+ "JOIN xt_partnership xps ON xps.partner_id = xup_partner.user_id\r\n"
				+ "JOIN xt_partner_team_group_mapping xptgm ON xptgm.partnership_id = xps.id\r\n"
				+ "JOIN xt_team_member_group_user_mapping xtmgum ON xtmgum.id = xptgm.team_member_group_user_mapping_id\r\n"
				+ "JOIN xt_team_member xtm ON xtm.id = xtmgum.team_member_id\r\n"
				+ "JOIN xt_user_profile xup_team ON xup_team.user_id = xtm.team_member_id\r\n"
				+ "WHERE xps.partner_company_id = :partnerCompanyId and xps.vendor_company_id = :companyId";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("partnerCompanyId", partnerCompanyId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId));
		return (List<String>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public String findLatestLeadCommentByLead(Lead lead) {
		String queryString = "SELECT comment FROM xt_campaign_deal_comments WHERE lead_id = :leadId "
				+ "and created_by = :createdBy\n ORDER BY created_time DESC LIMIT 1";
		HibernateSQLQueryResultRequestDTO requestDTO = new HibernateSQLQueryResultRequestDTO();
		requestDTO.setQueryString(queryString);
		requestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("leadId", lead.getId()));
		requestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("createdBy", lead.getCreatedBy()));
		return (String) hibernateSQLQueryResultUtilDao.getUniqueResult(requestDTO);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<LeadDto> fetchLeadsForContactReport(String dynamicQueryCondition, Integer createdByCompanyId,
			Integer createdForCompanyId, Integer limit) {
		String vendorCompanyIdQueryString = "";
		String partnerCompanyIdQueryString = " and l.created_by_company_id = :createdByCompanyId ";
		if (XamplifyUtils.isValidInteger(createdForCompanyId)) {
			vendorCompanyIdQueryString = "  and l.created_for_company_id = :createdForCompanyId";
		}
		if (Objects.equals(createdByCompanyId, createdForCompanyId)) {
			partnerCompanyIdQueryString = "";
		}
		String sql = "WITH integration_form_type AS ( SELECT l.id AS lead_id, COALESCE( ( SELECT CASE (xi.type) "
				+ "  WHEN 'salesforce' THEN 'SALES_FORCE_LEAD_CUSTOM_FORM'  "
				+ " WHEN 'hubspot' THEN 'HUBSPOT_CUSTOM_FORM' WHEN 'pipedrive' THEN 'PIPEDRIVE_CUSTOM_FORM' "
				+ " WHEN 'connectwise' THEN 'CONNECTWISE_CUSTOM_FORM' WHEN 'halopsa' THEN 'HALOPSA_CUSTOM_FORM' "
				+ "  WHEN 'zoho' THEN 'ZOHO_CUSTOM_FORM' ELSE 'XAMPLIFY_LEAD_CUSTOM_FORM' END "
				+ "  FROM xt_integration xi WHERE xi.company_id = l.created_for_company_id "
				+ "  AND xi.active ORDER BY xi.id DESC ),  'XAMPLIFY_LEAD_CUSTOM_FORM' ) AS form_type "
				+ "  FROM xt_lead l "
				+ ")  select l.id as \"id\", l.first_name as \"firstName\", l.last_name as \"lastName\", l.email as \"email\", l.company as \"company\", l.phone as \"phone\", "
				+ "l.website as \"website\", l.street as \"street\", l.city as \"city\", "
				+ "l.state as \"state\", l.postal_code as \"postalCode\", " + "l.country as \"country\", "
				+ "l.industry as \"industry\", l.region as \"region\", "
				+ "l.crm_reference_id as \"referenceId\", cfc.company_name as \"createdForCompanyName\", "
				+ "cbc.company_name as \"createdByCompanyName\", cfp.name as \"createdForPipeline\", "
				+ "cbp.name as \"createdByPipeline\", cfps.stage_name as \"createdForPipelineStage\", "
				+ "cbps.stage_name as \"createdByPipelineStage\", cast(l.created_time as text) as \"createdTime\", "
				+ "concat(up.firstname, case when up.firstname is not null and up.middle_name is not null then concat(' ', up.middle_name) "
				+ "else up.middle_name end, case when up.firstname is not null or up.middle_name is not null then concat(' ', up.lastname) "
				+ "else up.lastname end ) as \"createdByName\", c.campaign_name as \"campaignName\", a.\"contactName\" as \"contactName\", "
				+ "a.\"contactEmailId\" as \"contactEmailId\", cast(cf.custom_fields as text) as \"customFields\" from xt_lead l "
				+ "join xt_company_profile cfc on l.created_for_company_id = cfc.company_id "
				+ "join xt_company_profile cbc on l.created_by_company_id = cbc.company_id "
				+ "join xt_pipeline cfp on l.created_for_pipeline_id = cfp.id "
				+ "left join xt_pipeline cbp on l.created_by_pipeline_id = cbp.id "
				+ "join xt_pipeline_stage cfps on l.created_for_pipeline_stage_id = cfps.id "
				+ "left join xt_pipeline_stage cbps on l.created_by_pipeline_stage_id = cbps.id "
				+ "left join xt_campaign c on l.campaign_id = c.campaign_id "
				+ "join xt_user_profile up on l.created_by = up.user_id join ( "
				+ "select distinct(uul.user_id) as \"id\", concat(up.firstname, case when up.firstname is not null and up.middle_name is not null then concat(' ', up.middle_name) "
				+ " else up.middle_name end, case when up.firstname is not null or up.middle_name is not null then concat(' ', up.lastname)  "
				+ " else up.lastname end ) as \"contactName\", up.email_id as \"contactEmailId\" from xt_user_list ul "
				+ "join xt_user_userlist uul on ul.user_list_id = uul.user_list_id "
				+ "join xt_user_profile up on uul.user_id = up.user_id "
				+ "where ul.company_id = :companyId and ul.module_name ='CONTACTS'  " + dynamicQueryCondition
				+ ") a on l.associated_user_id = a.id LEFT JOIN integration_form_type ai ON ai.lead_id = l.id "
				+ " LEFT JOIN LATERAL ( "
				+ " SELECT jsonb_object_agg(fl.label_name, COALESCE(scd.value, '')) AS custom_fields "
				+ " FROM xt_form f JOIN xt_form_label fl ON f.id = fl.form_id LEFT JOIN xt_sf_cf_data scd "
				+ " ON fl.id = scd.sf_cf_label_id AND scd.lead_id = l.id  "
				+ " WHERE f.company_id = l.created_for_company_id "
				+ " AND cast(f.form_type as text) = ai.form_type AND f.form_sub_type IS NULL "
				+ " AND (fl.form_field_type = 'CUSTOM' OR fl.form_field_type IS NULL) ) cf ON TRUE where 1 = 1 "
				+ partnerCompanyIdQueryString + vendorCompanyIdQueryString + " order by l.id desc";

		if (XamplifyUtils.isValidInteger(limit)) {
			sql += " limit " + limit;
		}

		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(sql);
		if (!Objects.equals(createdByCompanyId, createdForCompanyId)) {
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("createdByCompanyId", createdByCompanyId));
		}
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("companyId", createdByCompanyId));
		if (XamplifyUtils.isValidInteger(createdForCompanyId)) {
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("createdForCompanyId", createdForCompanyId));
		}
		return (List<LeadDto>) hibernateSQLQueryResultUtilDao.getListDto(hibernateSQLQueryResultRequestDTO,
				LeadDto.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<LeadDto> fetchLeadsForContact(Integer contactId, Integer userListId, Integer createdByCompanyId,
			Integer createdForCompanyId) {
		String vendorCompanyIdQueryString = "";
		if (XamplifyUtils.isValidInteger(createdForCompanyId)) {
			vendorCompanyIdQueryString = "  and l.created_for_company_id = :createdForCompanyId";
		}
		String partnerCompanyIdQueryString = " and l.created_by_company_id = :createdByCompanyId ";
		if (Objects.equals(createdByCompanyId, createdForCompanyId)) {
			partnerCompanyIdQueryString = "";
		}
		String condition = " and uul.user_id = :contactId and ul.user_list_id = :userListId";
		String sql = "WITH integration_form_type AS ( SELECT l.id AS lead_id, " + " COALESCE( ( SELECT CASE (xi.type) "
				+ " WHEN 'salesforce' THEN 'SALES_FORCE_LEAD_CUSTOM_FORM' "
				+ " WHEN 'hubspot' THEN 'HUBSPOT_CUSTOM_FORM' " + " WHEN 'pipedrive' THEN 'PIPEDRIVE_CUSTOM_FORM' "
				+ " WHEN 'connectwise' THEN 'CONNECTWISE_CUSTOM_FORM' " + " WHEN 'halopsa' THEN 'HALOPSA_CUSTOM_FORM' "
				+ " WHEN 'zoho' THEN 'ZOHO_CUSTOM_FORM' " + " ELSE 'XAMPLIFY_LEAD_CUSTOM_FORM' END "
				+ " FROM xt_integration xi " + " WHERE xi.company_id = l.created_for_company_id "
				+ " AND xi.active ORDER BY xi.id DESC " + " ),  'XAMPLIFY_LEAD_CUSTOM_FORM' ) AS form_type "
				+ " FROM xt_lead l "
				+ ")  select l.id as \"id\", l.first_name as \"firstName\", l.last_name as \"lastName\", l.email as \"email\", l.company as \"company\", l.phone as \"phone\", "
				+ "l.website as \"website\", l.street as \"street\", l.city as \"city\", "
				+ "l.state as \"state\", l.postal_code as \"postalCode\", " + "l.country as \"country\", "
				+ "l.industry as \"industry\", l.region as \"region\", "
				+ "l.crm_reference_id as \"referenceId\", cfc.company_name as \"createdForCompanyName\", "
				+ "cbc.company_name as \"createdByCompanyName\", cfp.name as \"createdForPipeline\", "
				+ "cbp.name as \"createdByPipeline\", cfps.stage_name as \"createdForPipelineStage\", "
				+ "cbps.stage_name as \"createdByPipelineStage\", cast(l.created_time as text) as \"createdTime\", "
				+ "concat(up.firstname, case when up.firstname is not null and up.middle_name is not null then concat(' ', up.middle_name) "
				+ "else up.middle_name end, case when up.firstname is not null or up.middle_name is not null then concat(' ', up.lastname) "
				+ "else up.lastname end ) as \"createdByName\", c.campaign_name as \"campaignName\", a.\"contactName\" as \"contactName\", "
				+ "a.\"contactEmailId\" as \"contactEmailId\", cast(cf.custom_fields as text) as \"customFields\" from xt_lead l "
				+ "join xt_company_profile cfc on l.created_for_company_id = cfc.company_id "
				+ "join xt_company_profile cbc on l.created_by_company_id = cbc.company_id "
				+ "join xt_pipeline cfp on l.created_for_pipeline_id = cfp.id "
				+ "left join xt_pipeline cbp on l.created_by_pipeline_id = cbp.id "
				+ "join xt_pipeline_stage cfps on l.created_for_pipeline_stage_id = cfps.id "
				+ "left join xt_pipeline_stage cbps on l.created_by_pipeline_stage_id = cbps.id "
				+ "left join xt_campaign c on l.campaign_id = c.campaign_id "
				+ "join xt_user_profile up on l.created_by = up.user_id join ( "
				+ "select distinct(uul.user_id) as \"id\", concat(up.firstname, case when up.firstname is not null and up.middle_name is not null then concat(' ', up.middle_name) "
				+ " else up.middle_name end, case when up.firstname is not null or up.middle_name is not null then concat(' ', up.lastname) "
				+ " else up.lastname end ) as \"contactName\", up.email_id as \"contactEmailId\" from xt_user_list ul "
				+ "join xt_user_userlist uul on ul.user_list_id = uul.user_list_id "
				+ "join xt_user_profile up on uul.user_id = up.user_id "
				+ "where ul.company_id = :companyId and ul.module_name ='CONTACTS'  " + condition
				+ ") a on l.associated_user_id = a.id  LEFT JOIN integration_form_type ai ON ai.lead_id = l.id "
				+ "LEFT JOIN LATERAL ( "
				+ "  SELECT jsonb_object_agg(fl.label_name, COALESCE(scd.value, '')) AS custom_fields "
				+ "  FROM xt_form f JOIN xt_form_label fl ON f.id = fl.form_id " + "  LEFT JOIN xt_sf_cf_data scd\r\n"
				+ "         ON fl.id = scd.sf_cf_label_id AND scd.lead_id = l.id "
				+ "  WHERE f.company_id = l.created_for_company_id "
				+ "    AND cast(f.form_type as text) = ai.form_type AND f.form_sub_type IS NULL "
				+ "    AND (fl.form_field_type = 'CUSTOM' OR fl.form_field_type IS NULL) ) cf ON TRUE" + " where 1 = 1 "
				+ partnerCompanyIdQueryString + vendorCompanyIdQueryString + " order by l.id desc";

		HibernateSQLQueryResultRequestDTO requestDTO = new HibernateSQLQueryResultRequestDTO();
		requestDTO.setQueryString(sql);
		if (!Objects.equals(createdByCompanyId, createdForCompanyId)) {
			requestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("createdByCompanyId", createdByCompanyId));
		}
		requestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("companyId", createdByCompanyId));
		requestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("contactId", contactId));
		requestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("userListId", userListId));
		if (XamplifyUtils.isValidInteger(createdForCompanyId)) {
			requestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("createdForCompanyId", createdForCompanyId));
		}
		return (List<LeadDto>) hibernateSQLQueryResultUtilDao.getListDto(requestDTO, LeadDto.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<LeadDto> fetchLeadsForPartnerReport(Integer createdByCompanyId, Integer createdForCompanyId,
			Integer userListId, Integer queryLimit) {
		String createdByCompanyQuery = "";
		String partnersJoinQuery = "";
		if (XamplifyUtils.isValidInteger(createdByCompanyId)) {
			createdByCompanyQuery = " AND l.created_by_company_id = :createdByCompanyId ";
		} else {
			partnersJoinQuery = "join (select up.company_id as companyId from xt_user_list ul "
					+ " join xt_user_userlist uul on ul.user_list_id = uul.user_list_id "
					+ "JOIN xt_user_profile up ON uul.user_id = up.user_id "
					+ "where ul.module_name ='PARTNERS' and ul.user_list_id = :userListId and up.company_id is not null) as sk "
					+ " on l.created_by_company_id = sk.companyId";
		}
		String sql = "WITH integration_form_type AS (SELECT l.id AS lead_id, COALESCE((SELECT CASE (xi.type) WHEN 'salesforce' THEN 'SALESFORCE_LEAD_CUSTOM_FORM' WHEN 'hubspot' THEN 'HUBSPOT_CUSTOM_FORM' WHEN 'pipedrive' THEN 'PIPEDRIVE_CUSTOM_FORM' WHEN 'connectwise' THEN 'CONNECTWISE_CUSTOM_FORM' WHEN 'halopsa'"
				+ " THEN 'HALOPSA_CUSTOM_FORM' WHEN 'zoho' THEN 'ZOHO_CUSTOM_FORM' ELSE 'XAMPLIFY_LEAD_CUSTOM_FORM' END FROM xt_integration xi WHERE xi.company_id = l.created_for_company_id AND xi.active ORDER BY xi.id DESC), 'XAMPLIFY_LEAD_CUSTOM_FORM') AS form_type FROM xt_lead l) "
				+ "SELECT l.id as \"id\", l.first_name as \"firstName\", l.last_name as \"lastName\", l.email as \"email\", l.company as \"company\", l.phone as \"phone\", l.website as \"website\", l.street as \"street\", l.city as \"city\", l.state as \"state\", l.postal_code as \"postalCode\","
				+ " l.country as \"country\", l.industry as \"industry\", l.region as \"region\", l.crm_reference_id as \"referenceId\", cfc.company_name as \"createdForCompanyName\", cbc.company_name as \"createdByCompanyName\", cfp.name as \"createdForPipeline\", cbp.name as \"createdByPipeline\","
				+ " cfps.stage_name as \"createdForPipelineStage\", cbps.stage_name as \"createdByPipelineStage\", "
				+ "cast(l.created_time as text) as \"createdTime\", concat(up.firstname, case when up.firstname is not null and up.middle_name is not null then"
				+ " concat(' ', up.middle_name) else up.middle_name end, case when up.firstname is not null or up.middle_name is not null then concat(' ', up.lastname) else up.lastname end) as \"createdByName\", "
				+ "c.campaign_name as \"campaignName\", up.email_id as \"createdByEmail\", cast(cf.custom_fields as text) as \"customFields\" "
				+ "FROM xt_lead l JOIN xt_company_profile cfc ON l.created_for_company_id = cfc.company_id JOIN xt_company_profile cbc ON l.created_by_company_id = cbc.company_id JOIN xt_pipeline cfp ON l.created_for_pipeline_id = cfp.id LEFT JOIN xt_pipeline cbp ON l.created_by_pipeline_id = cbp.id"
				+ " JOIN xt_pipeline_stage cfps ON l.created_for_pipeline_stage_id = cfps.id LEFT JOIN xt_pipeline_stage cbps ON l.created_by_pipeline_stage_id = cbps.id LEFT JOIN xt_campaign c ON l.campaign_id = c.campaign_id JOIN xt_user_profile up ON l.created_by = up.user_id"
				+ " LEFT JOIN integration_form_type ai ON ai.lead_id = l.id "
				+ "LEFT JOIN LATERAL (SELECT jsonb_object_agg(fl.label_name, COALESCE(scd.value, '')) AS custom_fields FROM xt_form f JOIN xt_form_label fl ON f.id = fl.form_id LEFT JOIN xt_sf_cf_data scd ON fl.id = scd.sf_cf_label_id AND scd.lead_id = l.id "
				+ " WHERE f.company_id = l.created_for_company_id AND cast(f.form_type as text) = ai.form_type AND f.form_sub_type IS NULL AND (fl.form_field_type = 'CUSTOM' OR fl.form_field_type IS NULL)) cf ON TRUE "
				+ partnersJoinQuery + " WHERE 1 = 1 " + createdByCompanyQuery
				+ " AND l.created_for_company_id = :createdForCompanyId " + "ORDER BY l.id DESC";

		if (XamplifyUtils.isValidInteger(queryLimit)) {
			sql += " limit " + queryLimit;
		}

		HibernateSQLQueryResultRequestDTO requestDTO = new HibernateSQLQueryResultRequestDTO();
		requestDTO.setQueryString(sql);
//		if (!Objects.equals(createdByCompanyId, createdForCompanyId)) {
//			requestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("createdByCompanyId", createdByCompanyId));
//		}
		if (XamplifyUtils.isValidInteger(createdByCompanyId)
				&& !Objects.equals(createdByCompanyId, createdForCompanyId)) {
			requestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("createdByCompanyId", createdByCompanyId));
		} else {
			requestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("userListId", userListId));
		}
		if (XamplifyUtils.isValidInteger(createdForCompanyId)) {
			requestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("createdForCompanyId", createdForCompanyId));
		}
		return (List<LeadDto>) hibernateSQLQueryResultUtilDao.getListDto(requestDTO, LeadDto.class);
	}

	@Override
	public ModuleAccess getCompanyAccess(Integer companyId) {
			Session session = sessionFactory.getCurrentSession();
			org.hibernate.Criteria criteria = session.createCriteria(ModuleAccess.class);
			return (ModuleAccess) criteria.add(Restrictions.eq("companyProfile.id", companyId)).uniqueResult();
	}

}
