package com.xtremand.dashboard.analytics.views.dao;

import java.util.List;
import java.util.Map;

import com.xtremand.common.bom.Pagination;
import com.xtremand.dashboard.analytics.views.bom.DashboardModuleAnalyticsView;
import com.xtremand.dashboard.analytics.views.bom.EmailStatsVanityUrlView;
import com.xtremand.dashboard.analytics.views.bom.EmailStatsView;
import com.xtremand.dashboard.analytics.views.bom.OpportunitiesPartnerAnalyticsView;
import com.xtremand.dashboard.analytics.views.bom.OpportunitiesVanityUrlPartnerAnalyticsView;
import com.xtremand.dashboard.analytics.views.bom.OpportunitiesVendorAnalyticsView;
import com.xtremand.dashboard.analytics.views.bom.RegionalStatisticsVanityUrlView;
import com.xtremand.dashboard.analytics.views.bom.RegionalStatisticsView;
import com.xtremand.dashboard.analytics.views.bom.VendorActivityVanityUrlView;
import com.xtremand.dashboard.analytics.views.bom.VendorActivityView;
import com.xtremand.dashboard.analytics.views.bom.VendorEmailStatsView;
import com.xtremand.dashboard.analytics.views.bom.VendorRegionalStatisticsView;
import com.xtremand.dashboard.analytics.views.dto.DealStatisticsDTO;
import com.xtremand.dashboard.analytics.views.dto.PartnerAnalyticsCountDTO;
import com.xtremand.dashboard.analytics.views.dto.StatisticsDetailsOfPieChart;
import com.xtremand.dashboard.analytics.views.dto.WordCloudMapDTO;
import com.xtremand.team.member.dto.RoleDisplayDTO;
import com.xtremand.util.dto.LeftSideNavigationBarItem;
import com.xtremand.util.dto.PaginatedDTO;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;

public interface DashboardAnalyticsViewsDao {

	DashboardModuleAnalyticsView getDashboardModuleViewByCompanyId(VanityUrlDetailsDTO dto);

	List<VendorActivityView> getVendorActivityViewByCompanyId(VanityUrlDetailsDTO dto);

	List<VendorActivityVanityUrlView> getVendorActivityVanityUrlViewByCompanyId(VanityUrlDetailsDTO dto);

	public List<Object[]> listVendorCompanyDetailsByPartnerCompanyId(Integer partnerCompanyId);

	EmailStatsView getEmailStats(Integer userId);

	VendorEmailStatsView getVendorEmailStats(Integer loggedInUserId);

	EmailStatsVanityUrlView getEmailStatsForVanityUrl(VanityUrlDetailsDTO dto);

	List<RegionalStatisticsView> listRegionalStatisticsViewsByCompanyId(VanityUrlDetailsDTO dto);

	List<VendorRegionalStatisticsView> listVendorRegionalStatisticsViewsBy(VanityUrlDetailsDTO dto);

	List<RegionalStatisticsVanityUrlView> listRegionalStatisticsVanityUrlViewsByCompanyId(VanityUrlDetailsDTO dto);


	public OpportunitiesVendorAnalyticsView getOpportunitiesVendorAnalyticsByCompanyId(Integer userId);

	public OpportunitiesPartnerAnalyticsView getOpportunitiePartnerAnalyticsByCompanyId(VanityUrlDetailsDTO dto);

	public OpportunitiesVanityUrlPartnerAnalyticsView getOpportunitiesVanityUrlPartnerAnalytics(
			VanityUrlDetailsDTO dto);



	public Map<String, Object> getDashboardWorldMapDetailReport(List<Integer> userIdList, Integer pageSize,
			Integer pageNumber, VanityUrlDetailsDTO dto, String countryCode);

	public List<Object[]> getDashboardViewsData(List<Integer> userIdList, Integer daysInterval,
			VanityUrlDetailsDTO dto);

	public List<Object[]> getDashboardMinutesWatchedData(List<Integer> userIdList, Integer daysInterval,
			VanityUrlDetailsDTO dto);

	public List<Object[]> getDashboardAverageDurationData(List<Integer> userIdList, Integer daysInterval,
			VanityUrlDetailsDTO dto);


	public PartnerAnalyticsCountDTO getActiveInActiveTotalPartnerCounts(Integer userId, boolean applyFilter);


	List<WordCloudMapDTO> findDataForDealBubbleChart(Integer userId, boolean applyFilter);

	List<WordCloudMapDTO> findDataForLeadBubbleChart(Integer userId, boolean applyFilter);

	/*********** funnel chart ***************/
	public List<List<Object>> getFunnelChartAnalyticsData(VanityUrlDetailsDTO vanityUrlDetailsDto,
			RoleDisplayDTO roleDisplayDto);

	/********** pie chart **************/
	public List<List<Object>> getPieChartLeadAnalyticsData(VanityUrlDetailsDTO vanityUrlDetailsDto,
			RoleDisplayDTO roleDisplayDto);

	public List<List<Object>> getPieChartDealsAnalyticsData(VanityUrlDetailsDTO vanityUrlDetailsDto,
			RoleDisplayDTO roleDisplayDto);

	public List<StatisticsDetailsOfPieChart> getPieChartLeadStatisticsData(VanityUrlDetailsDTO vanityUrlDetailsDto,
			RoleDisplayDTO roleDisplayDto);

	public List<StatisticsDetailsOfPieChart> getPieChartDealStatisticsData(VanityUrlDetailsDTO vanityUrlDetailsDto,
			RoleDisplayDTO roleDisplayDto);

	List<DealStatisticsDTO> findDealsWithStageNames(VanityUrlDetailsDTO vanityUrlDetailsDto,
			RoleDisplayDTO roleDisplayDto);

	List<DealStatisticsDTO> findLeadsWithStageNames(VanityUrlDetailsDTO vanityUrlDetailsDto,
			RoleDisplayDTO roleDisplayDto);

	PaginatedDTO findAllQuickLinksForVendor(Pagination pagination, String search,
			LeftSideNavigationBarItem leftSideNavigationBarItem);

	PaginatedDTO findAllQuickLinksForPartner(Pagination pagination, String search,
			LeftSideNavigationBarItem leftSideNavigationBarItem, VanityUrlDetailsDTO vanityUrlDetailsDTO);

	PaginatedDTO universalSearchForVendorVanity(Pagination pagination, String search,
			LeftSideNavigationBarItem leftSideNavigationBarItem, VanityUrlDetailsDTO vanityUrlDetailsDTO);

	PaginatedDTO universalSearchForVendor(Pagination pagination, String search,
			LeftSideNavigationBarItem leftSideNavigationBarItem);

	PaginatedDTO universalSearchForXamplifyLogin(Pagination pagination, String search,
			LeftSideNavigationBarItem leftSideNavigationBarItem, RoleDisplayDTO roleDisplayDto, Integer userId);

}
