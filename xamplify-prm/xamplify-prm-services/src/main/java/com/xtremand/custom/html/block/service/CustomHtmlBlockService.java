package com.xtremand.custom.html.block.service;

import com.xtremand.custom.html.block.dto.CustomHtmlBlockDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.util.dto.Pageable;

public interface CustomHtmlBlockService {

	XtremandResponse findPaginatedCustomHtmls(Pageable pageable, Integer loggedInUserId);

	XtremandResponse save(CustomHtmlBlockDTO customHtmlBlockDto);

	XtremandResponse findById(Integer id, Integer loggedInUserId);

	XtremandResponse update(CustomHtmlBlockDTO customHtmlBlockDto);

	XtremandResponse delete(Integer id, Integer loggedInUserId);

	XtremandResponse updateSelectedHtmlBlock(CustomHtmlBlockDTO customHtmlBlockDto);

}
