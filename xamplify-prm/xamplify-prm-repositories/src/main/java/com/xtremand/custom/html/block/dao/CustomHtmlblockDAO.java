package com.xtremand.custom.html.block.dao;

import java.util.Map;

import com.xtremand.common.bom.Pagination;
import com.xtremand.custom.html.block.bom.CustomHtmlBlock;

public interface CustomHtmlblockDAO {

	Map<String, Object> findPaginatedCustomHtmls(Pagination pagination);

	boolean isTitleExist(String title, Integer companyId, Integer customHtmlBlockId);

	CustomHtmlBlock findById(Integer id, Integer companyId);

	void delete(Integer id, Integer companyId);

}
