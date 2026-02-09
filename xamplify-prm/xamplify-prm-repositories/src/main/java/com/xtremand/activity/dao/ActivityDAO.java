package com.xtremand.activity.dao;

import java.util.List;
import java.util.Map;

import com.xtremand.common.bom.Pagination;

public interface ActivityDAO {

	Map<String, Object> fetchRecentActivities(Pagination pagination);
	
	/**XNFR-867**/
	Map<String, Object> fetchRecentActivitiesForCompanyJourney(Pagination pagination, List<Integer> userIds); 
}
