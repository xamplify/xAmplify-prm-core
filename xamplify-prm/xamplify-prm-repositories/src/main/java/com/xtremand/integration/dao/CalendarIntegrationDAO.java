package com.xtremand.integration.dao;

import com.xtremand.activity.bom.MeetingSchedulingURL;
import com.xtremand.integration.bom.CalendarIntegration;
import com.xtremand.integration.bom.CalendarIntegrationTypeEnum;
import com.xtremand.integration.dto.CalendarIntegrationDTO;

public interface CalendarIntegrationDAO {
	
	CalendarIntegration getUserIntegrationDetails(Integer companyId, CalendarIntegrationTypeEnum type);
	
	CalendarIntegrationDTO getActiveCalendarIntegration(Integer companyId);
	
	CalendarIntegrationDTO getAuthenticationResources(Integer companyId, CalendarIntegrationTypeEnum type);
	
	Integer getTotalIntegrationsCount(Integer companyId);
	
	CalendarIntegrationDTO getUserOrganizationDetails(Integer userId, Integer companyId, CalendarIntegrationTypeEnum type);
	
	Integer removeCalendar(Integer id);
	
	CalendarIntegrationDTO getActiveCalendarDetails(Integer userId, Integer companyId);
	
	MeetingSchedulingURL getMeetingSchedulingURLDetails(Integer userId);
	
	Integer removeAllTeamMembersMeetingLinks(Integer id);
	
	Integer fetchCalendarIntegrationIdByCompanyId(Integer companyId);

}
