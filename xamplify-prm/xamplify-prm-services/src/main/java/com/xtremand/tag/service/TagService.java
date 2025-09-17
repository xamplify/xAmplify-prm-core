package com.xtremand.tag.service;

import com.xtremand.common.bom.Pagination;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.tag.dto.TagDTO;

public interface TagService {

	public XtremandResponse save(TagDTO tagDTO);
	
	public XtremandResponse update(TagDTO tagDTO);
	
	public XtremandResponse delete(TagDTO tagDTO);
	
	public XtremandResponse getAllByCompanyId(Pagination pagination, String searchBy);
	
}
