package com.xtremand.integration.dao.hibernate;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.activity.bom.MeetingSchedulingURL;
import com.xtremand.integration.bom.CalendarIntegration;
import com.xtremand.integration.bom.CalendarIntegrationTypeEnum;
import com.xtremand.integration.dao.CalendarIntegrationDAO;
import com.xtremand.integration.dto.CalendarIntegrationDTO;
import com.xtremand.util.dao.HibernateSQLQueryResultUtilDao;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.QueryParameterDTO;
import com.xtremand.util.dto.XamplifyConstants;

@Repository("hibernateCalendarIntegrationDao")
@Transactional
public class HibernateCalendarIntegrationDAO implements CalendarIntegrationDAO{
	
	private static final String COMPANY_ID = "companyId";

	private static final String USER_ID = "userId";

	@Autowired
	SessionFactory sessionFactory;

	@Autowired
	private HibernateSQLQueryResultUtilDao hibernateSQLQueryResultUtilDao;

	@Override
	public CalendarIntegration getUserIntegrationDetails(Integer companyId, CalendarIntegrationTypeEnum type) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(CalendarIntegration.class);
		criteria.add(Restrictions.eq("company.id", companyId));
		criteria.add(Restrictions.eq("type", type));
		return (CalendarIntegration) criteria.uniqueResult();
	}

	@Override
	public CalendarIntegrationDTO getActiveCalendarIntegration(Integer userId) {
		return null;
	}

	@Override
	public CalendarIntegrationDTO getAuthenticationResources(Integer companyId, CalendarIntegrationTypeEnum type) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String sqlQuery = "select access_token as \"accessToken\", refresh_token as \"refreshToken\", owner_url as \"ownerUrl\" from xt_calendar_integration "
				+ "where company_id = :companyId and type = '"+ type.getType() +"'";
		hibernateSQLQueryResultRequestDTO.setQueryString(sqlQuery);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(COMPANY_ID, companyId));
		return (CalendarIntegrationDTO) hibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO, CalendarIntegrationDTO.class);
	}

	@Override
	public Integer getTotalIntegrationsCount(Integer companyId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String sqlQuery = "select cast(count(id) as int) from xt_calendar_integration where company_id = :companyId";
		hibernateSQLQueryResultRequestDTO.setQueryString(sqlQuery);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(COMPANY_ID, companyId));
		return (Integer) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public CalendarIntegrationDTO getUserOrganizationDetails(Integer userId, Integer companyId, CalendarIntegrationTypeEnum type) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String sqlQuery = "select cn.id, cn.external_name as \"externalName\", cn.external_email_id as \"externalEmailId\", cn.active as \"active\", msu.scheduling_url as \"userUri\" "
				+ "from xt_calendar_integration cn left join xt_meeting_scheduling_url msu on cn.id = msu.calendar_integration_id and msu.user_id = :userId "
				+ "where cn.company_id = :companyId and cn.type = '"+ type.getType() +"'";
		hibernateSQLQueryResultRequestDTO.setQueryString(sqlQuery);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(COMPANY_ID, companyId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(USER_ID, userId));
		return (CalendarIntegrationDTO) hibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO, CalendarIntegrationDTO.class);
	}

	@Override
	public Integer removeCalendar(Integer id) {
		String sqlQuery = "delete from xt_calendar_integration where id = :id";
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(sqlQuery);
		query.setParameter(XamplifyConstants.ID, id);
		return query.executeUpdate();
	}

	@Override
	public CalendarIntegrationDTO getActiveCalendarDetails(Integer userId, Integer companyId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String sqlQuery = "select cn.id, msu.scheduling_url as \"userUri\", cast(cn.type as text) as \"type\", cn.active as \"active\" "
				+ "from xt_calendar_integration cn left join xt_meeting_scheduling_url msu on cn.id = msu.calendar_integration_id and msu.user_id = :userId "
				+ "where cn.company_id = :companyId and cn.active";
		hibernateSQLQueryResultRequestDTO.setQueryString(sqlQuery);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(USER_ID, userId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(COMPANY_ID, companyId));
		return (CalendarIntegrationDTO) hibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO, CalendarIntegrationDTO.class);
	}

	@Override
	public MeetingSchedulingURL getMeetingSchedulingURLDetails(Integer userId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(MeetingSchedulingURL.class);
		criteria.add(Restrictions.eq("user.userId", userId));
		return (MeetingSchedulingURL) criteria.uniqueResult();
	}

	@Override
	public Integer removeAllTeamMembersMeetingLinks(Integer id) {
		String sqlQuery = "delete from xt_meeting_scheduling_url where calendar_integration_id = :id";
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(sqlQuery);
		query.setParameter(XamplifyConstants.ID, id);
		return query.executeUpdate();
	}

	@Override
	public Integer fetchCalendarIntegrationIdByCompanyId(Integer companyId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String sqlQuery = "select cast(id as int) from xt_calendar_integration where company_id = :companyId";
		hibernateSQLQueryResultRequestDTO.setQueryString(sqlQuery);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(COMPANY_ID, companyId));
		return (Integer) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
	}
	
	
}
