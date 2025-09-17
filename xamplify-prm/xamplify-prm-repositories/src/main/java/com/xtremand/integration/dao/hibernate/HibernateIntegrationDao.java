package com.xtremand.integration.dao.hibernate;

import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.integration.bom.Integration;
import com.xtremand.integration.bom.Integration.IntegrationType;
import com.xtremand.integration.dao.IntegrationDao;
import com.xtremand.integration.dto.IntegrationSettingsDTO;
import com.xtremand.lead.bom.Pipeline;
import com.xtremand.lead.bom.PipelineStage;
import com.xtremand.lead.bom.PipelineType;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.HibernateSQLQueryResultUtilDao;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.QueryParameterDTO;

@Repository("hibernateIntegrationDao")
@Transactional
public class HibernateIntegrationDao implements IntegrationDao {

	@Autowired
	SessionFactory sessionFactory;

	@Autowired
	private HibernateSQLQueryResultUtilDao hibernateSQLQueryResultUtilDao;

	@Override
	public Integration getUserIntegrationDetails(Integer companyId, IntegrationType type) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(Integration.class);
		criteria.add(Restrictions.eq("company.id", companyId));
		criteria.add(Restrictions.eq("type", type));
		return (Integration) criteria.uniqueResult();
	}

	@Override
	public Integration getActiveCRMIntegration(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(Integration.class);
		criteria.add(Restrictions.eq("company.id", companyId));
		criteria.add(Restrictions.eq("active", true));
		return (Integration) criteria.uniqueResult();
	}

	@Override
	public Integer getTotalIntegrationsCount(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(Integration.class);
		criteria.add(Restrictions.eq("company.id", companyId));
		criteria.setProjection(Projections.rowCount());
		return ((Long) criteria.uniqueResult()).intValue();
	}

	@Override
	public void updateIntegrationTypeOnPipelines(Integer companyId, IntegrationType type) {
		Session session = sessionFactory.getCurrentSession();
		String sqlQueryString = "update xt_pipeline set integration_type = 'xamplify' where integration_type ='"
				+ type.name().toLowerCase() + "' and company_id=:companyId";
		Query query = session.createSQLQuery(sqlQueryString).setParameter("companyId", companyId);
		// .setParameter("type", type);
		query.executeUpdate();

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integration> getAllIntegrationsByType(IntegrationType type) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(Integration.class);
		criteria.add(Restrictions.eq("type", type));
		return (List<Integration>) criteria.list();
	}

	// **********************Deprecated***********************
	@Override
	public void setCRMPipelinesForExistingCampaigns(IntegrationType activeIntegrationType, Pipeline leadPipeline,
			Pipeline dealPipeline, Integer companyId, PipelineStage leadStage, PipelineStage dealStage) {
		activeIntegrationType = IntegrationType.XAMPLIFY;
		Session session = sessionFactory.getCurrentSession();
		String sqlQueryString = "update xt_campaign set lead_pipeline_id = :leadPipelineId where lead_pipeline_id in "
				+ "(select id from xt_pipeline where company_id = :companyId and " + "integration_type = '"
				+ activeIntegrationType.name().toLowerCase() + "' and type = '" + PipelineType.LEAD + "')";
		Query query = session.createSQLQuery(sqlQueryString).setParameter("companyId", companyId)
				.setParameter("leadPipelineId", leadPipeline.getId());
		query.executeUpdate();

		sqlQueryString = "update xt_campaign set deal_pipeline_id = :dealPipelineId where deal_pipeline_id in "
				+ "(select id from xt_pipeline where company_id = :companyId and " + "integration_type = '"
				+ activeIntegrationType.name().toLowerCase() + "' and type = '" + PipelineType.DEAL + "')";
		Query query2 = session.createSQLQuery(sqlQueryString).setParameter("companyId", companyId)
				.setParameter("dealPipelineId", dealPipeline.getId());
		query2.executeUpdate();

		sqlQueryString = "update xt_lead set pipeline_id = :leadPipelineId, pipeline_stage_id = :leadStageId where pipeline_id in "
				+ "(select id from xt_pipeline where company_id = :companyId and " + "integration_type = '"
				+ activeIntegrationType.name().toLowerCase() + "' and type = '" + PipelineType.LEAD + "')";
		Query query3 = session.createSQLQuery(sqlQueryString).setParameter("companyId", companyId)
				.setParameter("leadPipelineId", leadPipeline.getId()).setParameter("leadStageId", leadStage.getId());
		query3.executeUpdate();

		sqlQueryString = "update xt_deal set pipeline_id = :dealPipelineId, pipeline_stage_id = :dealStageId where pipeline_id in "
				+ "(select id from xt_pipeline where company_id = :companyId and " + "integration_type = '"
				+ activeIntegrationType.name().toLowerCase() + "' and type = '" + PipelineType.DEAL + "')";
		Query query4 = session.createSQLQuery(sqlQueryString).setParameter("companyId", companyId)
				.setParameter("dealPipelineId", dealPipeline.getId()).setParameter("dealStageId", dealStage.getId());
		query4.executeUpdate();
	}

	@Override
	public void setDealPipelinesForExistingCampaigns(Pipeline pipeline, Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String sqlQueryString = "update xt_campaign set deal_pipeline_id = :dealPipelineId where deal_pipeline_id in "
				+ "(select id from xt_pipeline where company_id = :companyId and " + "integration_type = '"
				+ IntegrationType.XAMPLIFY.name().toLowerCase() + "' and type = '" + PipelineType.DEAL + "')";
		Query query2 = session.createSQLQuery(sqlQueryString).setParameter("companyId", companyId)
				.setParameter("dealPipelineId", pipeline.getId());
		query2.executeUpdate();

	}

	@Override
	public void setLeadPipelinesForExistingCampaigns(Pipeline pipeline, Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String sqlQueryString = "update xt_campaign set lead_pipeline_id = :leadPipelineId where lead_pipeline_id in "
				+ "(select id from xt_pipeline where company_id = :companyId and " + "integration_type = '"
				+ IntegrationType.XAMPLIFY.name().toLowerCase() + "' and type = '" + PipelineType.LEAD + "')";
		Query query = session.createSQLQuery(sqlQueryString).setParameter("companyId", companyId)
				.setParameter("leadPipelineId", pipeline.getId());
		query.executeUpdate();

	}

	@Override
	public void setOtherDealPipelineForExistingCampaigns(List<Integer> removedIds, Pipeline pipeline, Integer companyId,
			PipelineStage dealStage) {
		Session session = sessionFactory.getCurrentSession();
		String removedIdString = removedIds.stream().map(String::valueOf).collect(Collectors.joining(","));
		String sqlQueryString = "update xt_campaign set deal_pipeline_id = :dealPipelineId where deal_pipeline_id in "
				+ "(" + removedIdString + ")";
		Query query2 = session.createSQLQuery(sqlQueryString).setParameter("dealPipelineId", pipeline.getId());
		query2.executeUpdate();

		sqlQueryString = "update xt_deal set pipeline_id = :dealPipelineId, pipeline_stage_id = :dealStageId, "
				+ "created_for_pipeline_id = :dealPipelineId, created_for_pipeline_stage_id = :dealStageId"
				+ "where created_for_pipeline_id in (" + removedIdString + ")";
		Query query3 = session.createSQLQuery(sqlQueryString).setParameter("dealPipelineId", pipeline.getId())
				.setParameter("dealStageId", dealStage.getId());
		query3.executeUpdate();

		sqlQueryString = "update xt_deal set created_by_pipeline_id = :dealPipelineId, created_by_pipeline_stage_id = :dealStageId "
				+ "where created_by_pipeline_id in (" + removedIdString + ")";
		Query query4 = session.createSQLQuery(sqlQueryString).setParameter("dealPipelineId", pipeline.getId())
				.setParameter("dealStageId", dealStage.getId());
		query4.executeUpdate();
	}

	// **********************Deprecated***********************
	@Override
	public void setNewPipelinesForExistingCampaigns(List<Integer> existingLeadPipeineIds,
			List<Integer> existingDealPipeineIds, Pipeline leadPipeline, Pipeline dealPipeline, Integer companyId,
			PipelineStage leadStage, PipelineStage dealStage) {
		Session session = sessionFactory.getCurrentSession();
		String sqlQueryString = "";
		if (existingLeadPipeineIds != null && !existingLeadPipeineIds.isEmpty()) {
			String existingLeadPipeineIdsString = existingLeadPipeineIds.stream().map(String::valueOf)
					.collect(Collectors.joining(","));
			sqlQueryString = "update xt_campaign set lead_pipeline_id = :leadPipelineId where lead_pipeline_id in "
					+ "(" + existingLeadPipeineIdsString + ")";
			Query query = session.createSQLQuery(sqlQueryString).setParameter("leadPipelineId", leadPipeline.getId());
			query.executeUpdate();

			sqlQueryString = "update xt_lead set pipeline_id = :leadPipelineId, pipeline_stage_id = :leadStageId where pipeline_id in "
					+ "(" + existingLeadPipeineIdsString + ")";
			Query query3 = session.createSQLQuery(sqlQueryString).setParameter("leadPipelineId", leadPipeline.getId())
					.setParameter("leadStageId", leadStage.getId());
			query3.executeUpdate();
		}

		if (existingDealPipeineIds != null && !existingDealPipeineIds.isEmpty()) {
			String existingDealPipeineIdsString = existingDealPipeineIds.stream().map(String::valueOf)
					.collect(Collectors.joining(","));
			sqlQueryString = "update xt_campaign set deal_pipeline_id = :dealPipelineId where deal_pipeline_id in "
					+ "(" + existingDealPipeineIdsString + ")";
			Query query2 = session.createSQLQuery(sqlQueryString).setParameter("dealPipelineId", dealPipeline.getId());
			query2.executeUpdate();

			sqlQueryString = "update xt_deal set pipeline_id = :dealPipelineId, pipeline_stage_id = :dealStageId where pipeline_id in "
					+ "(" + existingDealPipeineIdsString + ")";
			Query query4 = session.createSQLQuery(sqlQueryString).setParameter("dealPipelineId", dealPipeline.getId())
					.setParameter("dealStageId", dealStage.getId());
			query4.executeUpdate();

		}
	}

	@Override
	public void setCRMLeadPipelineForExistingCampaignsAndLeads(IntegrationType activeIntegrationType,
			Pipeline leadPipeline, Integer companyId, PipelineStage leadStage, Long ticketTypeId) {
		activeIntegrationType = IntegrationType.XAMPLIFY;
		PipelineType pipelineType = PipelineType.LEAD;
		String ticketType = "";
		Session session = sessionFactory.getCurrentSession();
		String sqlQueryString = "update xt_lead set pipeline_id = :leadPipelineId, pipeline_stage_id = :leadStageId, "
				+ "created_for_pipeline_id = :leadPipelineId, created_for_pipeline_stage_id = :leadStageId" + ticketType
				+ " where created_for_pipeline_id in "
				+ "(select id from xt_pipeline where company_id = :companyId and " + "integration_type = '"
				+ activeIntegrationType.name().toLowerCase() + "' and type = '" + pipelineType + "')";
		Query query3 = session.createSQLQuery(sqlQueryString).setParameter("companyId", companyId)
				.setParameter("leadPipelineId", leadPipeline.getId()).setParameter("leadStageId", leadStage.getId());
		if (ticketTypeId != null && ticketTypeId > 0L) {
			query3.setParameter("ticketTypeId", ticketTypeId);
		}
		query3.executeUpdate();

		sqlQueryString = "update xt_lead set created_by_pipeline_id = :leadPipelineId, created_by_pipeline_stage_id = :leadStageId"
				+ " where created_by_pipeline_id in " + "(select id from xt_pipeline where company_id = :companyId and "
				+ "integration_type = '" + activeIntegrationType.name().toLowerCase() + "' and type = '" + pipelineType
				+ "')";
		Query query4 = session.createSQLQuery(sqlQueryString).setParameter("companyId", companyId)
				.setParameter("leadPipelineId", leadPipeline.getId()).setParameter("leadStageId", leadStage.getId());

	}

	@Override
	public void setCRMDealPipelineForExistingCampaignsAndDeals(IntegrationType activeIntegrationType,
			Pipeline dealPipeline, Integer companyId, PipelineStage dealStage, Long ticketTypeId) {
		activeIntegrationType = IntegrationType.XAMPLIFY;
		String ticketType = "";
		String campaignTicketTypeId = "";
		if (ticketTypeId != null && ticketTypeId > 0L) {
			ticketType = ",halopsa_tickettype_id = :ticketTypeId";
			campaignTicketTypeId = ",halopsa_deal_ticket_type_id = :ticketTypeId";
		}

		Session session = sessionFactory.getCurrentSession();
		String sqlQueryString = "update xt_campaign set deal_pipeline_id = :dealPipelineId" + campaignTicketTypeId
				+ " where deal_pipeline_id in " + "(select id from xt_pipeline where company_id = :companyId and "
				+ "integration_type = '" + activeIntegrationType.name().toLowerCase() + "' and type = '"
				+ PipelineType.DEAL + "')";
		Query query2 = session.createSQLQuery(sqlQueryString).setParameter("companyId", companyId)
				.setParameter("dealPipelineId", dealPipeline.getId());
		if (ticketTypeId != null && ticketTypeId > 0L) {
			query2.setParameter("ticketTypeId", ticketTypeId);
		}
		query2.executeUpdate();

		sqlQueryString = "update xt_deal set pipeline_id = :dealPipelineId, pipeline_stage_id = :dealStageId,"
				+ "created_for_pipeline_id = :dealPipelineId, created_for_pipeline_stage_id = :dealStageId" + ticketType
				+ " where created_for_pipeline_id in "
				+ "(select id from xt_pipeline where company_id = :companyId and " + "integration_type = '"
				+ activeIntegrationType.name().toLowerCase() + "' and type = '" + PipelineType.DEAL + "')";
		Query query3 = session.createSQLQuery(sqlQueryString).setParameter("companyId", companyId)
				.setParameter("dealPipelineId", dealPipeline.getId()).setParameter("dealStageId", dealStage.getId());
		if (ticketTypeId != null && ticketTypeId > 0L) {
			query3.setParameter("ticketTypeId", ticketTypeId);
		}
		query3.executeUpdate();

		sqlQueryString = "update xt_deal set created_by_pipeline_id = :dealPipelineId, created_by_pipeline_stage_id = :dealStageId"
				+ " where created_by_pipeline_id in " + "(select id from xt_pipeline where company_id = :companyId and "
				+ "integration_type = '" + activeIntegrationType.name().toLowerCase() + "' and type = '"
				+ PipelineType.DEAL + "')";
		Query query4 = session.createSQLQuery(sqlQueryString).setParameter("companyId", companyId)
				.setParameter("dealPipelineId", dealPipeline.getId()).setParameter("dealStageId", dealStage.getId());
		query4.executeUpdate();

	}

	@Override
	public void setNewLeadPipelineForExistingCampaignsAndLeads(List<Integer> existingLeadPipeineIds,
			Pipeline leadPipeline, Integer companyId, PipelineStage leadStage, Long ticketTypeId) {
		if (existingLeadPipeineIds != null && !existingLeadPipeineIds.isEmpty()) {
			Session session = sessionFactory.getCurrentSession();
			String sqlQueryString = "";
			String ticketType = "";
			String campaignTicketTypeId = "";
			if (ticketTypeId != null && ticketTypeId > 0L) {
				ticketType = ",halopsa_tickettype_id = :ticketTypeId";
				campaignTicketTypeId = ",halopsa_lead_ticket_type_id = :ticketTypeId";
			}
			String existingLeadPipeineIdsString = existingLeadPipeineIds.stream().map(String::valueOf)
					.collect(Collectors.joining(","));
			sqlQueryString = "update xt_campaign set lead_pipeline_id = :leadPipelineId" + campaignTicketTypeId
					+ " where lead_pipeline_id in " + "(" + existingLeadPipeineIdsString + ")";
			Query query = session.createSQLQuery(sqlQueryString).setParameter("leadPipelineId", leadPipeline.getId());
			if (ticketTypeId != null && ticketTypeId > 0L) {
				query.setParameter("ticketTypeId", ticketTypeId);
			}
			query.executeUpdate();

			sqlQueryString = "update xt_lead set pipeline_id = :leadPipelineId, pipeline_stage_id = :leadStageId, "
					+ "created_for_pipeline_id = :leadPipelineId, created_for_pipeline_stage_id = :leadStageId"
					+ ticketType + " where created_for_pipeline_id in  (" + existingLeadPipeineIdsString + ")";
			Query query3 = session.createSQLQuery(sqlQueryString).setParameter("leadPipelineId", leadPipeline.getId())
					.setParameter("leadStageId", leadStage.getId());
			if (ticketTypeId != null && ticketTypeId > 0L) {
				query3.setParameter("ticketTypeId", ticketTypeId);
			}
			query3.executeUpdate();

			sqlQueryString = "update xt_lead set created_by_pipeline_id = :leadPipelineId, created_by_pipeline_stage_id = :leadStageId "
					+ "where created_by_pipeline_id in (" + existingLeadPipeineIdsString + ")";
			Query query4 = session.createSQLQuery(sqlQueryString).setParameter("leadPipelineId", leadPipeline.getId())
					.setParameter("leadStageId", leadStage.getId());
			query4.executeUpdate();
		}

	}

	@Override
	public void setNewDealPipelineForExistingCampaignsAndDeals(List<Integer> existingDealPipeineIds,
			Pipeline dealPipeline, Integer companyId, PipelineStage dealStage, Long ticketTypeId) {
		if (existingDealPipeineIds != null && !existingDealPipeineIds.isEmpty()) {
			Session session = sessionFactory.getCurrentSession();
			String sqlQueryString = "";
			String ticketType = "";
			String campaignTicketTypeId = "";
			if (ticketTypeId != null && ticketTypeId > 0L) {
				ticketType = ",halopsa_tickettype_id = :ticketTypeId";
				campaignTicketTypeId = ",halopsa_deal_ticket_type_id = :ticketTypeId";
			}
			String existingDealPipeineIdsString = existingDealPipeineIds.stream().map(String::valueOf)
					.collect(Collectors.joining(","));
			sqlQueryString = "update xt_campaign set deal_pipeline_id = :dealPipelineId" + campaignTicketTypeId
					+ " where deal_pipeline_id in " + "(" + existingDealPipeineIdsString + ")";
			Query query2 = session.createSQLQuery(sqlQueryString).setParameter("dealPipelineId", dealPipeline.getId());
			if (ticketTypeId != null && ticketTypeId > 0L) {
				query2.setParameter("ticketTypeId", ticketTypeId);
			}
			query2.executeUpdate();

			sqlQueryString = "update xt_deal set pipeline_id = :dealPipelineId, pipeline_stage_id = :dealStageId, "
					+ "created_for_pipeline_id = :dealPipelineId, created_for_pipeline_stage_id = :dealStageId"
					+ ticketType + " where created_for_pipeline_id in (" + existingDealPipeineIdsString + ")";
			Query query3 = session.createSQLQuery(sqlQueryString).setParameter("dealPipelineId", dealPipeline.getId())
					.setParameter("dealStageId", dealStage.getId());
			if (ticketTypeId != null && ticketTypeId > 0L) {
				query3.setParameter("ticketTypeId", ticketTypeId);
			}
			query3.executeUpdate();

			sqlQueryString = "update xt_deal set created_by_pipeline_id = :dealPipelineId, created_by_pipeline_stage_id = :dealStageId "
					+ "where created_by_pipeline_id in (" + existingDealPipeineIdsString + ")";
			Query query4 = session.createSQLQuery(sqlQueryString).setParameter("dealPipelineId", dealPipeline.getId())
					.setParameter("dealStageId", dealStage.getId());
			query4.executeUpdate();

		}

	}

	// *******XNFR-344********
	@SuppressWarnings("unchecked")
	@Override
	public List<Integration> getAllActiveIntegrationsByType(IntegrationType type) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(Integration.class);
		criteria.add(Restrictions.eq("type", type));
		criteria.add(Restrictions.eq("active", true));
		return criteria.list();
	}

	@Override
	public boolean isMultipleCRMsActivatedByCompanyId(Integer companyId) {
		String queryString = "select case when count(*)>1 then true else false end from xt_integration where company_id = :companyId and active";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("companyId", companyId));
		return hibernateSQLQueryResultUtilDao.returnBoolean(hibernateSQLQueryResultRequestDTO);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<IntegrationSettingsDTO> getVendorRegisterDealList(Integer partnerCompanyId, Integer vendorCompanyId) {
		String vendorCompany = "";
		boolean isValidVendorCompanyId = XamplifyUtils.isValidInteger(vendorCompanyId);
		if (isValidVendorCompanyId) {
			vendorCompany = " and p.vendor_company_id = :vendorCompanyId";
		}
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String sql = "select cast(p.vendor_company_id as text) as \"vendorCompanyId\",COALESCE(i.deal_by_partner, true) as \"dealByPartner\" from xt_partnership p left join xt_integration i on i.company_id = p.vendor_company_id"
				+ " and (i.active = 'true' or i.active is null) where p.partner_company_id = :partnerCompanyId and p.status = 'approved'"
				+ vendorCompany;
		hibernateSQLQueryResultRequestDTO.setQueryString(sql);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("partnerCompanyId", partnerCompanyId));
		if (isValidVendorCompanyId) {
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("vendorCompanyId", vendorCompanyId));
		}
		hibernateSQLQueryResultRequestDTO.setClassInstance(IntegrationSettingsDTO.class);
		return (List<IntegrationSettingsDTO>) hibernateSQLQueryResultUtilDao
				.returnDTOList(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public String getActiveIntegrationTypeByCompanyId(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String activeCRMQueryString = "select cast(type as text) from xt_integration where company_id = :companyId and active";
		return (String) session.createSQLQuery(activeCRMQueryString).setParameter("companyId", companyId)
				.uniqueResult();
	}

	@Override
	public IntegrationSettingsDTO isSelfDealByVendor(Integer companyId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String sql = "select distinct deal_by_vendor as \"dealByVendor\", deal_by_self_lead as \"dealBySelfLead\" from xt_integration where company_id = :companyId and active";
		hibernateSQLQueryResultRequestDTO.setQueryString(sql);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("companyId", companyId));
		return (IntegrationSettingsDTO) hibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO,
				IntegrationSettingsDTO.class);
	}

	@Override
	public boolean hasActiveCRMIntegration(Integer companyId) {
		String queryString = "select case when count(*)>0 then true else false end from xt_integration where company_id = :companyId and active";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("companyId", companyId));
		return hibernateSQLQueryResultUtilDao.returnBoolean(hibernateSQLQueryResultRequestDTO);
	}

}
