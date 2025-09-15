package com.xtremand.activity.dao;

import java.util.List;
import java.util.Map;

import com.xtremand.activity.dto.ActivityAttachmentDTO;
import com.xtremand.activity.dto.EmailActivityDTO;
import com.xtremand.activity.dto.EmailMergeTagDTO;
import com.xtremand.common.bom.Pagination;

public interface EmailActivityDAO {
	
	Map<String, Object> fetchAllEmailActivities (Pagination pagination, String searchKey);
	
	EmailActivityDTO fetchEmailActivityById (Integer emailActivityId);
	
	List<ActivityAttachmentDTO> fetchEmailAttachments (Integer emailActivityId);

	EmailMergeTagDTO fetchEmailMergeTagsData(Integer loggedInUserId, Integer contactId);
	
	/**XNFR-867**/
	Map<String, Object> fetchAllEmailActivitiesForCompanyJourney(Pagination pagination, List<Integer> userIds, String searchKey);
	
	String getTemplateById(Integer templateId);
	
	/**XNFR-1055**/
	List<EmailActivityDTO> fetchEmailActivitiesForConatctAgent(String dynamicQueryCondition, Integer companyId);
	
	List<EmailActivityDTO> fetchEmailActivitiesForConatctAgentOnContact(Integer companyId, Integer contactId);

	EmailActivityDTO fetchWelcomeEmailActivityById(Integer emailActivityId);

}
