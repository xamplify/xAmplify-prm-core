package com.xtremand.pipeline.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.common.bom.Pagination;
import com.xtremand.integration.bom.Integration.IntegrationType;
import com.xtremand.lead.bom.Pipeline;
import com.xtremand.lead.bom.PipelineStage;
import com.xtremand.lead.bom.PipelineType;
import com.xtremand.lead.dto.PipelineRequestDTO;
import com.xtremand.lead.dto.PipelineResponseDTO;
import com.xtremand.lead.dto.PipelineStageResponseDTO;
import com.xtremand.util.PaginationUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.HibernateSQLQueryResultUtilDao;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.QueryParameterDTO;

@Repository("PipelineDAO")
@Transactional
public class HibernatePipelineDAO implements PipelineDAO {

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private PaginationUtil paginationUtil;

	@Autowired
	private HibernateSQLQueryResultUtilDao hibernateSQLQueryResultUtilDao;

	@SuppressWarnings("unchecked")
	@Override
	public List<Pipeline> getPipeLines(Integer companyId, PipelineType type, Boolean isPrivate) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(Pipeline.class);
		criteria.add(Restrictions.eq("type", type));
		if (isPrivate != null) {
			criteria.add(Restrictions.eq("isPrivate", isPrivate));
		}
		criteria.add(Restrictions.eq("company.id", companyId));
		criteria.addOrder(Order.asc("name"));
		return criteria.list();
	}

	@Override
	public Pipeline getPipeLineByName(Integer companyId, String name, PipelineType type) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(Pipeline.class);
		criteria.add(Restrictions.eq("name", name).ignoreCase());
		criteria.add(Restrictions.eq("company.id", companyId));
		return (Pipeline) criteria.uniqueResult();
	}

	@Override
	public void clearDisplayIndex(Integer pipelineId) {
		Session session = sessionFactory.getCurrentSession();
		String hql = "update PipelineStage p set displayIndex = displayIndex*(-1) where p.pipeline.id = " + pipelineId
				+ " and displayIndex > 0";
		Query query = session.createQuery(hql);
		query.executeUpdate();
		// session.getTransaction().commit();
		// session.flush();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getPipeLinesForVendor(Pagination pagination) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(Pipeline.class);
		criteria.add(Restrictions.eq("type", PipelineType.valueOf(pagination.getPipelineType())));
		criteria.add(Restrictions.eq("company.id", pagination.getCompanyId()));

		if (pagination.getSearchKey() != null && !pagination.getSearchKey().isEmpty()) {
			criteria.add(Restrictions.ilike("name", "%" + pagination.getSearchKey() + "%"));
		}

		ScrollableResults scrollableResults = criteria.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;

		criteria.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		criteria.setMaxResults(pagination.getMaxResults());
		criteria.addOrder(Order.asc("name"));

		// List<SocialStatus> socialStatusList = criteria.list();
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("totalRecords", totalRecords);
		resultMap.put("data", criteria.list());
		return resultMap;
	}

	@Override
	public Pipeline getSalesforcePipeline(Integer companyId, PipelineType type) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(Pipeline.class);
		criteria.add(Restrictions.eq("isSalesforcePipeline", true));
		criteria.add(Restrictions.eq("company.id", companyId));
		criteria.add(Restrictions.eq("type", type));
		return (Pipeline) criteria.uniqueResult();
	}

	@Override
	public Pipeline getPipeLineByCompanyIdAndName(Integer companyId, String name) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(Pipeline.class);
		criteria.add(Restrictions.eq("company.id", companyId));
		criteria.add(Restrictions.eq("name", name));
		return (Pipeline) criteria.uniqueResult();
	}

	@Override
	public Pipeline getDefaultPipeLine(Integer companyId, PipelineType type) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(Pipeline.class);
		criteria.add(Restrictions.eq("isDefault", true));
		criteria.add(Restrictions.eq("company.id", companyId));
		criteria.add(Restrictions.eq("type", type));
		return (Pipeline) criteria.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Pipeline> getPipelinesByIntegrationType(Integer companyId, PipelineType type,
			IntegrationType integrationType, Boolean isPrivate) {
		integrationType = IntegrationType.XAMPLIFY;
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(Pipeline.class);
		criteria.add(Restrictions.eq("integrationType", integrationType));
		criteria.add(Restrictions.eq("company.id", companyId));
		criteria.add(Restrictions.eq("type", type));
		if (isPrivate != null) {
			criteria.add(Restrictions.eq("isPrivate", isPrivate));
		}
		return criteria.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Pipeline> getPipelinesByIntegrationType(Integer companyId, IntegrationType integrationType) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(Pipeline.class);
		criteria.add(Restrictions.eq("integrationType", integrationType));
		criteria.add(Restrictions.eq("company.id", companyId));
		return criteria.list();
	}

	@Override
	public Pipeline getDealPipelineByExternalPipelineId(Integer companyId, String externalPipelineId,
			IntegrationType integrationType) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(Pipeline.class);
		criteria.add(Restrictions.eq("externalPipelineId", externalPipelineId));
		criteria.add(Restrictions.eq("company.id", companyId));
		criteria.add(Restrictions.eq("type", PipelineType.DEAL));
		criteria.add(Restrictions.eq("integrationType", integrationType));
		return (Pipeline) criteria.uniqueResult();
	}

	@Override
	public PipelineStage getPipelineStageByExternalPipelineStageId(Integer companyId, Integer pipelineId,
			String externalPipelineStageId) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(PipelineStage.class, "PS");
		criteria.add(Restrictions.eq("externalPipelineStageId", externalPipelineStageId));
		criteria.add(Restrictions.eq("pipeline.id", pipelineId));
		return (PipelineStage) criteria.uniqueResult();
	}

	@Override
	public PipelineStage getDefaultStage(Integer pipelineId) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(PipelineStage.class, "PS");
		criteria.add(Restrictions.eq("defaultStage", true));
		criteria.add(Restrictions.eq("pipeline.id", pipelineId));
		return (PipelineStage) criteria.uniqueResult();
	}

	@Override
	public Integer getPublicPipelinesCount(Integer companyId, IntegrationType integrationType, PipelineType type) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(Pipeline.class);
		criteria.add(Restrictions.eq("company.id", companyId));
		criteria.add(Restrictions.eq("type", type));
		criteria.add(Restrictions.eq("integrationType", integrationType));
		criteria.add(Restrictions.eq("isPrivate", false));
		criteria.setProjection(Projections.rowCount());
		return ((Long) criteria.uniqueResult()).intValue();
	}

	@Override
	public boolean hasPartnerCreatedDealsOnPipeline(Integer pipelineId, Integer vendorCompanyId) {
		String sqlString = "select CASE WHEN count(*) > 0 THEN true ELSE false END from xt_deal "
				+ "where created_for_company_id=:vendorCompanyId and pipeline_id =:pipelineId "
				+ "and created_by_company_id !=created_for_company_id";
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(sqlString);
		query.setParameter("vendorCompanyId", vendorCompanyId);
		query.setParameter("pipelineId", pipelineId);
		return (boolean) query.uniqueResult();
	}

	@Override
	public boolean hasPartnerCreatedDealsOnPipelineStage(Integer pipelineStageId, Integer vendorCompanyId) {
		String sqlString = "select CASE WHEN count(*) > 0 THEN true ELSE false END from xt_deal "
				+ "where created_for_company_id=:vendorCompanyId and pipeline_stage_id =:pipelineStageId "
				+ "and created_by_company_id !=created_for_company_id";
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(sqlString);
		query.setParameter("vendorCompanyId", vendorCompanyId);
		query.setParameter("pipelineStageId", pipelineStageId);
		return (boolean) query.uniqueResult();
	}

	@Override
	public boolean hasPartnerCreatedLeadsOnPipeline(Integer pipelineId, Integer vendorCompanyId) {
		String sqlString = "select CASE WHEN count(*) > 0 THEN true ELSE false END from xt_lead "
				+ "where created_for_company_id=:vendorCompanyId and pipeline_id =:pipelineId "
				+ "and created_by_company_id !=created_for_company_id";
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(sqlString);
		query.setParameter("vendorCompanyId", vendorCompanyId);
		query.setParameter("pipelineId", pipelineId);
		return (boolean) query.uniqueResult();
	}

	@Override
	public boolean hasPartnerCreatedLeadsOnPipelineStage(Integer pipelineStageId, Integer vendorCompanyId) {
		String sqlString = "select CASE WHEN count(*) > 0 THEN true ELSE false END from xt_lead "
				+ "where created_for_company_id=:vendorCompanyId and pipeline_stage_id =:pipelineStageId "
				+ "and created_by_company_id !=created_for_company_id";
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(sqlString);
		query.setParameter("vendorCompanyId", vendorCompanyId);
		query.setParameter("pipelineStageId", pipelineStageId);
		return (boolean) query.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	public List<Pipeline> getPipelineByExternalPipelineIdForHaloPSA(Integer companyId, PipelineType type,
			IntegrationType integrationType, Boolean isPrivate, List<String> externalPipelineIds) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(Pipeline.class);
		criteria.add(Restrictions.eq("integrationType", integrationType));
		criteria.add(Restrictions.eq("company.id", companyId));
		criteria.add(Restrictions.eq("type", type));
		criteria.add(Restrictions.in("externalPipelineId", externalPipelineIds));
		if (isPrivate != null) {
			criteria.add(Restrictions.eq("isPrivate", isPrivate));
		}
		return criteria.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PipelineResponseDTO> findLeadPipeLinesByActiveCRM(PipelineRequestDTO pipelineRequestDTO,
			String activeCRM, String externalPipelineId) {
		Integer vendorCompanyId = pipelineRequestDTO.getVendorCompanyId();
		String externalPipelineQueryString = "";
		Session session = sessionFactory.getCurrentSession();
		String leadPipeLinesQueryString = "select id as \"id\", name as \"name\" from xt_pipeline where company_id = :vendorCompanyId"
				+ " and cast(type as text) = :type and cast(integration_type as text) = :activeCRM"
				+ externalPipelineQueryString;
		Query query = session.createSQLQuery(leadPipeLinesQueryString);
		query.setParameter("vendorCompanyId", vendorCompanyId);
		query.setParameter("type", PipelineType.LEAD.name());
		query.setParameter("activeCRM", activeCRM);
		return (List<PipelineResponseDTO>) paginationUtil.getListDTO(PipelineResponseDTO.class, query);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PipelineStageResponseDTO> findPipelineStagesByPipelineId(Integer pipelineId, boolean isPrivateStage) {
		String getPrivateStages = "";
		if (isPrivateStage) {
			getPrivateStages += "and is_private = false ";
		}
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(
				"select id as \"id\",stage_name as \"stageName\" from xt_pipeline_stage where pipeline_id = :pipelineId "
						+ getPrivateStages + "order by display_index asc");
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("pipelineId", pipelineId));
		hibernateSQLQueryResultRequestDTO.setClassInstance(PipelineStageResponseDTO.class);
		return (List<PipelineStageResponseDTO>) hibernateSQLQueryResultUtilDao
				.returnDTOList(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public String getActiveCRM(PipelineRequestDTO pipelineRequestDTO) {
		String defaultActiveCRM = "xamplify";
		Session session = sessionFactory.getCurrentSession();
		String activeCRMQueryString = "select cast(type as text) from xt_integration where company_id = :companyId and active";
		String activeCRMByCompanyId = (String) session.createSQLQuery(activeCRMQueryString)
				.setParameter("companyId", pipelineRequestDTO.getVendorCompanyId()).uniqueResult();
		String activeCRM = XamplifyUtils.isValidString(activeCRMByCompanyId) ? activeCRMByCompanyId : defaultActiveCRM;
		return activeCRM;
	}

	@Override
	public PipelineResponseDTO findLeadPipelinesByCampaignId(Integer campaignId) {
		if (XamplifyUtils.isValidInteger(campaignId)) {
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			String queryString = "select distinct p.id as \"id\", p.name as \"name\" from xt_pipeline p, xt_campaign c where c.lead_pipeline_id = p.id"
					+ " and c.campaign_id = :campaignId order by p.name asc";
			hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("campaignId", campaignId));
			return (PipelineResponseDTO) hibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO,
					PipelineResponseDTO.class);
		} else {
			return null;
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PipelineResponseDTO> findPipeLinesByCompanyIdAndPipeLineType(Integer companyId, PipelineType type,
			boolean isPrivate) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String sqlQueryString = "select id as \"id\", name as \"name\",is_default as \"isDefault\" from xt_pipeline where company_id = :companyId and cast(type as text) = :type and is_private = :private  order by name asc ";
		hibernateSQLQueryResultRequestDTO.setQueryString(sqlQueryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("companyId", companyId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("type", type.name()));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("private", isPrivate));
		hibernateSQLQueryResultRequestDTO.setClassInstance(PipelineResponseDTO.class);
		return (List<PipelineResponseDTO>) hibernateSQLQueryResultUtilDao
				.returnDTOList(hibernateSQLQueryResultRequestDTO);

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PipelineResponseDTO> findPipelinesByCompanyIdAndPipeLineTypeAndIntegrationType(Integer companyId,
			PipelineType type, String integrationType) {

		integrationType = IntegrationType.XAMPLIFY.name();

		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String sqlQueryString = "select id as \"id\", name as \"name\", is_default as \"isDefault\" from xt_pipeline where company_id = :companyId and cast(type as text) = :type"
				+ "  and cast(integration_type as text) =:integrationType  order by name asc ";
		hibernateSQLQueryResultRequestDTO.setQueryString(sqlQueryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("companyId", companyId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("integrationType", integrationType.toLowerCase()));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("type", type.name()));
		hibernateSQLQueryResultRequestDTO.setClassInstance(PipelineResponseDTO.class);
		return (List<PipelineResponseDTO>) hibernateSQLQueryResultUtilDao
				.returnDTOList(hibernateSQLQueryResultRequestDTO);

	}

	@Override
	public String getTicketTypeIdFromCampaignByCampaignId(Integer campaignId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String sqlQueryString = "select cast(halopsa_lead_ticket_type_id as text) from xt_campaign where campaign_id = :campaignId";
		hibernateSQLQueryResultRequestDTO.setQueryString(sqlQueryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("campaignId", campaignId));
		return (String) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PipelineResponseDTO> findPipelineForCRMSettings(PipelineRequestDTO pipelineRequestDTO,
			Integer companyId) {
		IntegrationType integrationType = pipelineRequestDTO.getIntegrationType();
		PipelineType pipelineType = pipelineRequestDTO.getPipelineType();
		integrationType = IntegrationType.XAMPLIFY;
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String sqlQuery = "select id as \"id\", name as \"name\" from xt_pipeline where company_id = :companyId and cast(integration_type as text) = :integrationType and cast(type as text) = :pipelineType";
		hibernateSQLQueryResultRequestDTO.setQueryString(sqlQuery);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("companyId", companyId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("integrationType", integrationType.name().toLowerCase()));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("pipelineType", pipelineType.name()));
		hibernateSQLQueryResultRequestDTO.setClassInstance(PipelineResponseDTO.class);
		return (List<PipelineResponseDTO>) hibernateSQLQueryResultUtilDao
				.returnDTOList(hibernateSQLQueryResultRequestDTO);
	}
	
	@Override
	public Pipeline getLeadPipelineByExternalPipelineId(Integer companyId, String externalPipelineId,
			IntegrationType integrationType) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(Pipeline.class);
		criteria.add(Restrictions.eq("externalPipelineId", externalPipelineId));
		criteria.add(Restrictions.eq("company.id", companyId));
		criteria.add(Restrictions.eq("type", PipelineType.LEAD));
		criteria.add(Restrictions.eq("integrationType", integrationType));
		return (Pipeline) criteria.uniqueResult();
	}

}
