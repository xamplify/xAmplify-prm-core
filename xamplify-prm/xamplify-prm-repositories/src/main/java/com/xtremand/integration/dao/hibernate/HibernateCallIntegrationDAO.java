package com.xtremand.integration.dao.hibernate;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.integration.bom.CallIntegration;
import com.xtremand.integration.bom.CallIntegrationTypeEnum;
import com.xtremand.integration.dao.CallIntegrationDAO;
import com.xtremand.integration.dto.CallIntegrationDTO;
import com.xtremand.util.dao.HibernateSQLQueryResultUtilDao;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.QueryParameterDTO;
import com.xtremand.util.dto.XamplifyConstants;

@Repository("hibernateCallIntegrationDao")
@Transactional
public class HibernateCallIntegrationDAO implements CallIntegrationDAO{
	
	private static final String COMPANY_ID = "companyId";
	
	@Autowired
	SessionFactory sessionFactory;

	@Autowired
	private HibernateSQLQueryResultUtilDao hibernateSQLQueryResultUtilDao;

	@Override
	public CallIntegration getUserIntegrationDetails(Integer companyId, CallIntegrationTypeEnum type) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(CallIntegration.class);
		criteria.add(Restrictions.eq("company.id", companyId));
		criteria.add(Restrictions.eq("type", type));
		return (CallIntegration) criteria.uniqueResult();
	}

	@Override
	public CallIntegrationDTO getAuthenticationResources(Integer companyId, CallIntegrationTypeEnum type) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String sqlQuery = "select access_token as \"accessToken\", refresh_token as \"refreshToken\" from xt_call_integration "
				+ "where company_id = :companyId and type = '"+ type.getType() +"'";
		hibernateSQLQueryResultRequestDTO.setQueryString(sqlQuery);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(COMPANY_ID, companyId));
		return (CallIntegrationDTO) hibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO, CallIntegrationDTO.class);
	}
	
	@Override
	public Integer getTotalIntegrationsCount(Integer companyId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String sqlQuery = "select cast(count(id) as int) from xt_call_integration where company_id = :companyId";
		hibernateSQLQueryResultRequestDTO.setQueryString(sqlQuery);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(COMPANY_ID, companyId));
		return (Integer) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public CallIntegrationDTO getUserOrganizationDetails(Integer userId, Integer companyId,
			CallIntegrationTypeEnum type) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String sqlQuery = "select cn.id, cn.external_name as \"externalName\", cn.external_email_id as \"externalEmailId\", cn.active as \"active\" "
				+ "from xt_call_integration cn "
				+ "where cn.company_id = :companyId and cn.type = '"+ type.getType() +"'";
		hibernateSQLQueryResultRequestDTO.setQueryString(sqlQuery);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(COMPANY_ID, companyId));
		return (CallIntegrationDTO) hibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO, CallIntegrationDTO.class);
	}

	@Override
	public Integer removeCall(Integer id) {
		String sqlQuery = "delete from xt_call_integration where id = :id";
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(sqlQuery);
		query.setParameter(XamplifyConstants.ID, id);
		return query.executeUpdate();
	}

	@Override
	public CallIntegrationDTO getActiveCallDetails(Integer companyId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String sqlQuery = "select cn.id, cast(cn.type as text) as \"type\", cn.active as \"active\" "
				+ "from xt_call_integration cn "
				+ "where cn.company_id = :companyId and cn.active";
		hibernateSQLQueryResultRequestDTO.setQueryString(sqlQuery);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(COMPANY_ID, companyId));
		return (CallIntegrationDTO) hibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO, CallIntegrationDTO.class);
	}

}
