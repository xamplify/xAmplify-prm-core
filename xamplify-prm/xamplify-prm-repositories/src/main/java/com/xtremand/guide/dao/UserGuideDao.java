package com.xtremand.guide.dao;

import java.util.List;
import java.util.Map;

import com.xtremand.common.bom.Pagination;
import com.xtremand.guide.dto.UserGuideDto;

public interface UserGuideDao {

	Integer getTagIdByName(String tagName);

	UserGuideDto getUserGuideByTagId(Integer id);

	UserGuideDto getUserGuideBySlug(String slug);

	List<UserGuideDto> getUserGudesByModuleId(Integer moduleId, List<Object> subModuleIds, List<Object> guideTitles);

	Map<String, Object> getUserGuideLnkByTitle(String title);


	Map<String, Object> getUserGuidesByModuleAndSubMOdules(Pagination pagination, List<Object> moduleIds,
			List<Object> subModuleIds, List<Object> guideTitles);

	Integer getSubModuleIdsWithName(String subModuleName);
	
	String getGuideTitleByMergeTagName(String tagName);
	
	String getModuleNameByModuleId(Integer moduleId);
}
