package com.xtremand.dashboard.analytics.views.service;

import java.util.Map;

import org.springframework.validation.BindingResult;

import com.xtremand.dashboard.analytics.views.dto.PartnerAnalyticsCountDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.log.bom.VideoStats;
import com.xtremand.util.dto.Pageable;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;

public interface DashboardAnalyticsViewsService {

	XtremandResponse getDashboardModuleViewByCompanyId(VanityUrlDetailsDTO dto);


	XtremandResponse listVendorCompanyDetailsByUserId(Integer userId);

	XtremandResponse getEmailStats(VanityUrlDetailsDTO postDto);

	XtremandResponse getRegionalStatistics(VanityUrlDetailsDTO postDto);


	public XtremandResponse getOpportunitiesVendorAnalytics(Integer userId);

	XtremandResponse getOpportunitiesPartnerAnalytics(VanityUrlDetailsDTO dto);

	public Map<String, Object> listEmailOpenLogs(VanityUrlDetailsDTO dto, Integer actionId, Integer pageSize,
			Integer pageNumber);

	public Map<String, Object> listEmailGifClickedUrlClickedLogs(VanityUrlDetailsDTO dto, Integer pageSize,
			Integer pageNumber);

	public Map<String, Object> listWatchedUsersByUser(VanityUrlDetailsDTO dto, Integer pageSize, Integer pageNumber);

	public Map<String, Object> getDashboardWorldMapDetailReport(VanityUrlDetailsDTO dto, Integer pageSize,
			Integer pageNumber, String countryCode);

	public VideoStats getDashboardVideoStatsData(VanityUrlDetailsDTO dto, Integer daysInterval);


	public PartnerAnalyticsCountDTO getActiveInActiveTotalPartnerCounts(Integer userId, boolean applyFilter);


	Map<String, Object> findDataForDealOrLeadBubbleChart(Integer userId, String moduleType, boolean applyFilter);

	/**** Funnel Charts Analytics ***/

	public XtremandResponse getFunnelChartsAnalytics(VanityUrlDetailsDTO vanityUrlDetailsDTO);

	XtremandResponse getPieChartsLeadsAnalytics(VanityUrlDetailsDTO vanityUrlDetailsDTO);

	XtremandResponse getPieChartsDealsAnalytics(VanityUrlDetailsDTO vanityUrlDetailsDTO);

	XtremandResponse getPieChartsStatisticsLeadAnalytics(VanityUrlDetailsDTO vanityUrlDetailsDto);

	XtremandResponse getPieChartsDealStatisticsAnalytics(VanityUrlDetailsDTO vanityUrlDetailsDto);

	public XtremandResponse getPieChartsDealStatisticsWithStageNames(VanityUrlDetailsDTO vanityUrlDetailsDto);

	public XtremandResponse getPieChartsLeadsStatisticsWithStageNames(VanityUrlDetailsDTO vanityUrlDetailsDto);

	XtremandResponse findAllQuickLinks(Pageable pageable, String domainName, Integer userId, BindingResult result);
	
    /**** XNFR-574 ****/
	XtremandResponse universalSearch(Pageable pageable, String domainName, Integer userId, BindingResult result);

}
