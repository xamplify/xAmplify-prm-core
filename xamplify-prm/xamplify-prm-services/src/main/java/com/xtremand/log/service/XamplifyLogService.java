package com.xtremand.log.service;

import java.util.List;
import java.util.Map;

import com.xtremand.common.bom.Pagination;
import com.xtremand.formbeans.HeatMapData;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.log.bom.VideoStats;
import com.xtremand.log.bom.VideoViewsMinutesWatched;
import com.xtremand.log.bom.XtremandLog;

public interface XamplifyLogService {

	public Map<String, Integer> getDashboardPageAnalyticsCount(Integer userId);

	public Map<String, Object> countrywiseUsersCount(Integer userId);

	public Integer logUserVideoActions(XtremandLog log);

	public Integer logEmbedVideoActions(XtremandLog log);

	public List listWatchedUsersByUser(Integer userId, Integer pageSize, Integer pageNumber);

	public Integer getWatchedUsersCountByUser(Integer userId);

	public Map<String, Object> getHeatMapByUniqueSession(String sessionId);

	public List<HeatMapData> getDashboardHeatMapData(Integer userId, String limit);

	public Map<String, Object> getDashboardBarChartData(Integer userId, List<Integer> campaignIdsList);

	public VideoStats getDashboardVideoStatsData(Integer userId, Integer daysInterval);

	public List<Object[]> listTotalMunutesWatchedByTop10Users(Integer videoId);

	public Integer totalVideoViewsCount(Integer videoId);

	public Map<String, Object> listVideoViewsByTimePeriod(String timePeriod, Integer videoId);

	public List<VideoViewsMinutesWatched> listVideoViewsMinutesWatchedByTimePeriod(String timePeriod, Integer videoId,
			String timePeriodValue);

	public List<String> getTimePeriodValues(String timePeriod);


	public Map<String, Object> listVideoViewsMinutesWatchedDetailReport(Integer loggedInUser, String timePeriod,
			Integer userId, Integer videoId, String timePeriodValue, Pagination pagination);

	public VideoViewsMinutesWatched listVideoViewsDetailReport1(String timePeriod, Integer videoId,
			String timePeriodValue);

	public Map<String, Object> listVideoViewsDetailReport2(Integer userId, String timePeriod, Integer videoId,
			String timePeriodValue, Pagination pagination);

	public Map<String, Object> getVideoSkippedDurationData(Integer videoId);

	public Map<String, Object> getVideoWatchedFullyDetailReport(Integer videoId, Pagination pagination, Integer userId);

	public XtremandResponse listTotalMinutesWatchedByTop10UsersDetailReport(Integer videoId, Integer userId);

	public Map<String, Object> listVideoDurationPlayedUsers(Integer userId, Integer videoId, Pagination pagination);

	public Map<String, Object> listVideoDurationSkippedUsers(Integer userId, Integer videoId, Pagination pagination);

	public VideoViewsMinutesWatched getNAUsersVideoViewsMinutesWatched(Integer videoId);

	public Map<String, Object> getDashboardWorldMapDetailReport(Integer userId, String countryCode,
			Pagination pagination);

	public Map<String, Object> getVideoViewsCountByCountry(Integer videoId);

	public Map<String, Object> listCountryWiseVideoViewsDetailReport(Integer userId, Integer videoId,
			String countryCode, Pagination pagination);

	public Map<String, Object> listLeadsDetails(Integer userId, Integer videoId, Pagination pagination);

	public boolean executePost(String response);

}