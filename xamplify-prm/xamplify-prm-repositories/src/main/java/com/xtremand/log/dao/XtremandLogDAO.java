package com.xtremand.log.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.xtremand.common.bom.Pagination;
import com.xtremand.log.bom.EmailLogView;
import com.xtremand.log.bom.SMSLog;
import com.xtremand.log.bom.SMS_URLShortener;
import com.xtremand.log.bom.URLShortener;
import com.xtremand.log.bom.XtremandLog;

public interface XtremandLogDAO {

	public String getURLShortenerAliasByURL(String aliasUrl);

	public URLShortener getURLShortenerByAlias(String alias);

	public URLShortener getURLShortenerByURL(String url);

	public List<Object[]> countrywiseUsersCount(Integer userId);

	public List listEmailLogsByUserAndAction(List<Integer> userIdList, Integer actionId, Integer pageSize,
			Integer pageNumber);

	public List listEmailGifClickedUrlClickedLogsByUser(List<Integer> userIdList, Integer pageSize, Integer pageNumber);

	public Integer getEmailLogCountByUser(List<Integer> userIdList, Integer actionId);

	public List listWatchedUsersByUser(List<Integer> userIdList, Integer pageSize, Integer pageNumber);

	public Integer getWatchedUsersCountByUser(List<Integer> userIdList);

	public Integer getEmailLogCountByCampaign(Integer campaignId, String actionId);

	public Integer getDataShareClickedUrlsCountForVendor(Integer campaignId, String actionId);

	public Integer getCampaignTotalViews(Integer campaignId, String date);

	public Integer getCampaignTotalViewsCount(Integer campaignId);

	public Integer campaignWatchedUsersCount(Integer campaignId);

	public Integer getUsersWatchedCountByUser(Integer userId, Integer actionId);

	public List<XtremandLog> listXtremandLogsBySessionId(String sessionId);

	public List listUniqueXtremandLogsByCampaignAndUser(Integer userId, Integer campaignId);

	public List listReplayLogsByCampaignAndUser(Integer userId, Integer campaignId);

	public Object getCountryWiseCampaignViews(Integer campaignId);

	public Integer getEmailLogClickedUrlsCountByUserId(Integer userId, String url, Integer campaignId);

	public Integer getOpenedEmailsCount(Integer userId, Integer videoId, Integer campaignId);

	public Date getOpenedEmailUserByMinTime(Integer userId, Integer campaignId);

	public Date getMinTimeOfClickedUrl(Integer campaignId, Integer userId, String clickedUrl);

	public Integer getWatchedVideosCount(Integer userId, Integer videoId, Integer campaignId);

	public Integer getVideoNotOpenedEmailNotificationCount(Integer userId, Integer videoId, Integer campaignId,
			Integer replyId);

	public Integer getEmailsSentCount(Integer campaignId);

	public List<Object[]> getDashboardHeatMapData(Integer userId, String limit);

	public List<Object[]> getCampaignEmailOpenedClickedCount(Integer userId, List<Integer> campaignIds);

	public List<Object[]> getCampaignWatchedCount(Integer userId, List<Integer> campaignIds);

	public List<Object[]> getDashboardViewsData(Integer userId, Integer daysInterval);

	public List<Object[]> getDashboardMinutesWatchedData(Integer userId, Integer daysInterval);

	public List<Object[]> getDashboardAverageDurationData(Integer userId, Integer daysInterval);

	public List<Object[]> listCampaignViewsDetialReport1(Integer userId, Integer daysInterval, Integer selectedDate);

	public Map<String, Object> listCampaignViewsDetialReport2(Integer videoId, Integer daysInterval,
			Integer selectedDate, Pagination pagination);

	public List<Object[]> listCampaignMinutesWatchedDetialReport1(Integer userId, Integer daysInterval,
			Integer selectedDate);

	public Map<String, Object> listCampaignMinutesWatchedDetialReport2(Pagination pagination, Integer videoId,
			Integer daysInterval, Integer selectedDate);

	public List<Object[]> listTotalMunutesWatchedByTop10Users(Integer videoId);

	public Integer totalVideoViewsCount(Integer videoId);

	public List<Object[]> listCurrentMonthVideoViews(Integer videoId);

	public List<Object[]> listMonthWiseVideoViews(Integer videoId);

	public List<Object[]> listQuarterlyVideoViews(Integer videoId);

	public List<Object[]> listYearlyVideoViews(Integer videoId);

	public List<Object[]> listVideoViewsMinutesWatchedByMonth(Integer videoId, String month);

	public List<Object[]> listVideoViewsMinutesWatchedByQuarter(Integer videoId, String quarter);

	public List<Object[]> listTodayVideoViewsMinutesWatched(Integer videoId);

	public List<Object[]> listVideoViewsMinutesWatchedByYear(Integer videoId, String year);

	public List<Object[]> campaignBubbleChartData(Integer campaignId, String type);

	public List<Object[]> listCampaignLifeTimeViewsDetailReport(Integer campaignId, Pagination pagination);

	public List<Object[]> listCampaignCurrentMonthViewsDetailReport(Integer campaignId, Pagination pagination);

	public List<Object[]> listCampaignTodaysViewsDetailReport(Integer campaignId, Pagination pagination);

	public Map<String, Object> listVideoViewsMinutesWatchedDetailReportByYear(Integer userId, Integer videoId,
			String year, Pagination pagination);

	public Map<String, Object> listTodayVideoViewsMinutesWatchedDetailReport(Integer userId, Integer videoId,
			Pagination pagination);

	public Map<String, Object> listVideoViewsMinutesWatchedDetailReportByMonth(Integer userId, Integer videoId,
			String year, Pagination pagination);

	public Map<String, Object> listVideoViewsMinutesWatchedDetailReportByQuarter(Integer userId, Integer videoId,
			String month, Pagination pagination);

	public Object[] listVideoViewsByYearDetailReport1(Integer videoId, String year);

	public Map<String, Object> listVideoViewsByYearDetailReport2(Integer videoId, String year, Pagination pagination);

	public Object[] listVideoViewsByQuarterDetailReport1(Integer videoId, String quarter);

	public Map<String, Object> listVideoViewsByQuarterDetailReport2(Integer videoId, String quarter,
			Pagination pagination);

	public Object[] listVideoViewsByMonthDetailReport1(Integer videoId, String month);

	public Map<String, Object> listVideoViewsByMonthDetailReport2(Integer videoId, String month, Pagination pagination);

	public Object[] listCurrentMonthVideoViewsDetailReport1(Integer videoId, String month);

	public Map<String, Object> listCurrentMonthVideoViewsDetailReport2(Integer videoId, String month,
			Pagination pagination);

	public List<Object[]> getVideoSkippedDurationData(Integer videoId);

	public Map<String, Object> getVideoWatchedFullyDetailReport(Integer videoId, Pagination pagination);

	public List<Object[]> listTotalMinutesWatchedByTop10UsersDetailReport(Integer videoId);

	public Map<String, Object> listVideoDurationPlayedUsers(Integer videoId, Pagination pagination);

	public Map<String, Object> listVideoDurationSkippedUsers(Integer videoId, Pagination pagination);

	public List<Number> getNAUsersVideoViewsMinutesWatched(Integer videoId);

	public Map<String, Object> getDashboardWorldMapDetailReport(Pagination pagination, Integer userId,
			String countryCode);

	public Integer getTotalTimeSpentByCampaignUser(Integer userId, Integer campaignId);

	public List<Object[]> getCampaignViewsCountByCountry(Integer campaignId);

	public Map<String, Object> listCountryWiseCampaignViewsDetailReport(Integer campaignId, String countryCode,
			Pagination pagination);

	public List<Object[]> getVideoViewsCountByCountry(Integer videoId);

	public Map<String, Object> listCountryWiseVideoViewsDetailReport(Integer videoId, String countryCode,
			Pagination pagination);

	public Integer getCampaignEmailOpenCountByUser(Integer campaignId, Integer userId);

	public List<Object[]> getVideoCoBrandingLogoEnableStatus(Integer videoId, Integer userId);

	public Integer getEmailActionCountByCampaignIdAndUserIdAndActionType(Integer campaignId, Integer userId,
			String actionType);

	public List<EmailLogView> listEmailLogsByCampaignIdUserIdActionType(Integer campaignId, Integer userId,
			String actionType);

	public Map<String, Object> campaignInteractiveViews(Pagination pagination);

	SMS_URLShortener getSMS_URLShortenerByAlias(String alias);

	public Integer getSMSSentCount(Integer campaignId);

	Integer getSMSSentSuccessCount(Integer campaignId);

	Integer getSMSSentFailureCount(Integer campaignId);

	List<Object> listSMSLogsByCampaignIdUserIdActionType(Integer campaignId, Integer userId, String actionType);

	public List<SMSLog> listSMSLogsByCampaignAndUser(Integer userId, Integer campaignId);

	public Integer getSMS_LogCountByCampaign(Integer campaignId, String string);

	public List<EmailLogView> listSMS_LogsByAction(Integer campaignId, String actionType, Pagination pagination);

	public Map<String, Object> listLeadsDetails(Integer videoId, Pagination pagination);

	public List<String> listClickedUrlsByCampaignIdUserId(Integer campaignId, Integer userId);

	public List<Integer> updateAliasesUserIds();

	public void updateAlias(String alias, Integer userId);

	public List<Integer> updateAliasesUserIds3();

	List<Integer> findVideoIsNotPlayedEmailNotificationOpenedUserIdsByCampaignIdAndVideoIdAndReplyId(Integer campaignId,
			Integer videoId, Integer replyId);

}
