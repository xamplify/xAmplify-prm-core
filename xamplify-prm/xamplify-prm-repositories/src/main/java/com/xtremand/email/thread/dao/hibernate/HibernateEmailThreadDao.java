package com.xtremand.email.thread.dao.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.xtremand.email.thread.dao.EmailThreadDao;
import com.xtremand.integration.bom.MailIntegration;
import com.xtremand.integration.bom.MailIntegrationTypeEnum;
import com.xtremand.integration.dto.CallIntegrationDTO;
import com.xtremand.util.dao.HibernateSQLQueryResultUtilDao;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.QueryParameterDTO;

@Repository
public class HibernateEmailThreadDao implements EmailThreadDao {
	private static final String USER_ID = "userId";
	@Autowired
	SessionFactory sessionFactory;

	@Autowired
	private HibernateSQLQueryResultUtilDao hibernateSQLQueryResultUtilDao;

	@SuppressWarnings("unchecked")
	@Override
	public List<CallIntegrationDTO> getAccessToken(Integer userId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String sqlQuery = "select access_token as \"accessToken\", refresh_token as \"refreshToken\", type as \"type\",active as \"active\", external_email_id as \"externalEmailId\"  from xt_mail_integration "
				+ "where created_by = :userId";
		hibernateSQLQueryResultRequestDTO.setQueryString(sqlQuery);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(USER_ID, userId));
		return  (List<CallIntegrationDTO>) hibernateSQLQueryResultUtilDao.getListDto(hibernateSQLQueryResultRequestDTO, CallIntegrationDTO.class);
	}

	@Override
	public MailIntegration getUserIntegrationDetails(Integer userId, MailIntegrationTypeEnum type) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(MailIntegration.class);
		criteria.add(Restrictions.eq("createdBy.userId", userId));
		criteria.add(Restrictions.eq("type", type));
		return (MailIntegration) criteria.uniqueResult();
	}

	@Override
	public Integer getTotalIntegrationsCount(Integer loggedInUser) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String sqlQuery = "select cast(count(id) as int) from xt_mail_integration where  created_by= :userId";
		hibernateSQLQueryResultRequestDTO.setQueryString(sqlQuery);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(USER_ID, loggedInUser));
		return (Integer) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
	}
	
	@Override
	public void deactivateAllMailIntegrations(Integer userId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
	    String sqlQuery = "UPDATE xt_mail_integration SET active = false where created_by= :userId";
	    hibernateSQLQueryResultRequestDTO.setQueryString(sqlQuery);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(USER_ID, userId));
		hibernateSQLQueryResultUtilDao.updateAndReturnCount(hibernateSQLQueryResultRequestDTO);
	}
	
	
	@Override
	public CallIntegrationDTO getAuthenticationResources(Integer userId, MailIntegrationTypeEnum type) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String sqlQuery = "select access_token as \"accessToken\", refresh_token as \"refreshToken\", external_email_id as \"externalEmailId\" from xt_mail_integration "
				+ "where created_by= :userId and type = '"+ type.getType() +"'";
		hibernateSQLQueryResultRequestDTO.setQueryString(sqlQuery);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(USER_ID, userId));
		return (CallIntegrationDTO) hibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO, CallIntegrationDTO.class);
	}

}
