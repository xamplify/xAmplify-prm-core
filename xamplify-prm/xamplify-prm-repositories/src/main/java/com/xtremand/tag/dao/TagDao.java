package com.xtremand.tag.dao;

import java.util.List;
import java.util.Map;

import com.xtremand.common.bom.Pagination;
import com.xtremand.tag.bom.Tag;

public interface TagDao {

	public void save(Object clazz);
	
	public void update(Tag tag);

	public Tag getById(Integer id);

	public void delete(List<Integer> ids);

	public Map<String, Object> getAllByCompanyId(Integer id, Pagination pagination, String searchBy);

	public List<Integer> getTagIds(Integer userId);
	
	public List<String> getTagNames(Integer userId, Integer companyId);
	
	public Tag getByIdAndCompanyId(Integer tagId, Integer companyId);
	
}
