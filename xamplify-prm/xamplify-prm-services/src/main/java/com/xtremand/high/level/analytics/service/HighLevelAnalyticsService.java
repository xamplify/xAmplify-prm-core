package com.xtremand.high.level.analytics.service;

import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.util.dto.DownloadRequestPostDTO;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;

public interface HighLevelAnalyticsService {
	/*********** HighLevel Analytics ***********/
	XtremandResponse getActiveAndInActivePartnersForDonutChart(VanityUrlDetailsDTO vanityUrlDetailsDto);

	XtremandResponse getHighLevelAnalyticsDetailReportsForTiles(VanityUrlDetailsDTO vanityUrlDetailsDTO);

	public XtremandResponse downloadAnalytics(VanityUrlDetailsDTO vanityUrlDetailsDto, Integer id);

	XtremandResponse saveDownloadRequest(DownloadRequestPostDTO downloadRequestPostDTO);

	void processHighLevelAnalyticsFailedRequests();

}
