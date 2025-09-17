package com.xtremand.log.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.IntStream;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.neovisionaries.i18n.CountryCode;
import com.xtremand.analytics.service.PartnerAnalyticsService;
import com.xtremand.analytics.service.VendorAnalyticsService;
import com.xtremand.common.bom.Criteria;
import com.xtremand.common.bom.Criteria.OPERATION_NAME;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.common.bom.Pagination;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.formbeans.EmailLogReport;
import com.xtremand.formbeans.HeatMapData;
import com.xtremand.formbeans.RoleDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.log.bom.VideoStats;
import com.xtremand.log.bom.VideoViewsMinutesWatched;
import com.xtremand.log.bom.XtremandLog;
import com.xtremand.log.dao.XtremandLogDAO;
import com.xtremand.log.service.XamplifyLogService;
import com.xtremand.mail.service.MailService;
import com.xtremand.notification.service.NotificationService;
import com.xtremand.team.dao.TeamDao;
import com.xtremand.user.bom.Role;
import com.xtremand.user.bom.User;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.user.service.UserService;
import com.xtremand.userlist.dao.UserListDAO;
import com.xtremand.userlist.service.UserListService;
import com.xtremand.util.DateUtils;
import com.xtremand.util.service.UtilService;
import com.xtremand.video.bom.VideoFile;
import com.xtremand.video.dao.VideoDao;
import com.xtremand.video.service.VideoService;

@Service("xamplifyLogService")
@Transactional
public class XamplifyLogServiceImpl implements XamplifyLogService {
	private static final Logger logger = LoggerFactory.getLogger(XamplifyLogServiceImpl.class);

	@Autowired
	GenericDAO genericDao;

	@Autowired
	XtremandLogDAO xtremandLogDAO;

	@Autowired
	UserListDAO userListDAO;

	@Autowired
	private UserService userService;

	@Autowired
	MailService mailService;

	@Autowired
	UserListService userListService;

	@Autowired
	VideoService videoService;

	@Autowired
	PartnerAnalyticsService partnerAnalyticsService;

	@Autowired
	VendorAnalyticsService vendorAnalyticsService;

	@Autowired
	VideoDao videoDAO;

	@Autowired
	NotificationService notificationService;

	@Autowired
	UserDAO userDao;

	@Autowired
	private UtilService utilService;

	@Autowired
	private TeamDao teamDao;

	@Value("${server_path}")
	String server_path;

	@Value("${web_url}")
	String webUrl;

	@Value("${unsubscirbeDiv}")
	String unsubscirbeDiv;

	@Value("${unsubscribeUrl}")
	String unsubscribeUrl;

	@Value("${dev.cobranding.image}")
	String devCoBrandingImage;

	@Value("${prod.cobranding.image}")
	String prodCoBrandingImage;

	@Value("${release.cobranding.image}")
	String releaseCoBrandingImage;

	@Value("${company.logo.url}")
	String companyLogoUrl;

	@Value("${view.in.browser.link.clicked}")
	String viewInBrowserLinkClicked;

	@Value("${replace.there}")
	private String replaceThere;

	@Value("${google.captcha.secret.key}")
	String googleCaptchaSecretKey;

	@Value("${google.captcha.verification.url}")
	String googleCaptchaVerificationUrl;

	@Value("${unsubscribeLink.tag}")
	private String defaultUnsubscibeLinkMergeTag;

	@Value("${mergetag.unsubscribeLink}")
	private String unsubscribeLinkMergeTag;

	@Value("${default.unsubscribeLink.mergeTag}")
	private String unsubscribeLinkMergeTagForViewInBrowser;

	@Value("${senderEventUrl.tag}")
	private String defaultSenderEventUrlMergeTag;

	/******* XNFR-281 ******/
	@Value("${senderCompanyUrl.tag}")
	private String senderCompanyUrlMergeTag;

	@Value("${senderCompanyInstagramUrl.tag}")
	private String senderCompanyInstagramUrlMergeTag;

	@Value("${senderCompanyTwitterUrl.tag}")
	private String senderCompanyTwitterUrlMergeTag;

	@Value("${senderCompanyFacebookUrl.tag}")
	private String senderCompanyFacebookUrlMergeTag;

	@Value("${senderCompanyGoogleUrl.tag}")
	private String senderCompanyGoogleUrlMergeTag;

	@Value("${senderCompanyLinkedinUrl.tag}")
	private String senderCompanyLinkedinUrlMergeTag;

	@Override
	public Integer logUserVideoActions(XtremandLog log) {

		Integer previousId = log.getPreviousId();

		if (log.getActionId() != 8) {
			if (previousId != null) {
				XtremandLog previousXtremandLogObj = genericDao.get(XtremandLog.class, previousId);
				if (previousXtremandLogObj.getActionId() != 2 && previousXtremandLogObj.getActionId() != 8) {
					previousXtremandLogObj.setEndTime(log.getStartTime());
					previousXtremandLogObj.setStopDuration(log.getStopDuration());
					genericDao.saveOrUpdate(previousXtremandLogObj);
				}

				if (log.getActionId() == 2 && previousXtremandLogObj.getActionId() == 8) {
					XtremandLog previousPreviousXtremandLogObj = genericDao.get(XtremandLog.class, (previousId) - 1);
					previousPreviousXtremandLogObj.setEndTime(log.getStartTime());
					previousPreviousXtremandLogObj.setStopDuration(log.getStartDuration());
					genericDao.saveOrUpdate(previousPreviousXtremandLogObj);
				}
			}
		}

		logger.debug("from XamplifyLogServiceImpl logUserVideoActions() method");
		log.setOpenCount(0);
		Integer id = genericDao.save(log);
		logger.info("newly inserted log record id : " + log.getId());
		return id;
	}

	@Override
	public Integer logEmbedVideoActions(XtremandLog log) {
		logger.debug("from XamplifyLogServiceImpl logEmbedVideoActions() method");
		Integer previousId = log.getPreviousId();
		if (log.getActionId() != 8) {
			if (previousId != null) {
				XtremandLog previousXtremandLogObj = genericDao.get(XtremandLog.class, previousId);
				if (previousXtremandLogObj.getActionId() != 2 && previousXtremandLogObj.getActionId() != 8) {
					previousXtremandLogObj.setEndTime(log.getStartTime());
					previousXtremandLogObj.setStopDuration(log.getStopDuration());
					genericDao.saveOrUpdate(previousXtremandLogObj);
				}

				if (log.getActionId() == 2 && previousXtremandLogObj.getActionId() == 8) {
					XtremandLog previousPreviousXtremandLogObj = genericDao.get(XtremandLog.class, (previousId) - 1);
					previousPreviousXtremandLogObj.setEndTime(log.getStartTime());
					previousPreviousXtremandLogObj.setStopDuration(log.getStartDuration());
					genericDao.saveOrUpdate(previousPreviousXtremandLogObj);
				}
			}
		}

		Integer id = genericDao.save(log);

		logger.info("newly inserted log record id : " + log.getId());
		return id;
	}

	@Override
	public Map<String, Integer> getDashboardPageAnalyticsCount(Integer userId) {
		Map<String, Integer> resultMap = new HashMap<String, Integer>();
		User user = userService.loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, userId)),
				new FindLevel[] { FindLevel.ROLES, FindLevel.COMPANY_PROFILE });
		Integer companyId = user.getCompanyProfile().getId();
		List<Integer> userIdList = userService.getCompanyUserIds(userId);
		Integer[] userIdArray = ArrayUtils.toObject(userIdList.stream().mapToInt(i -> i).toArray());
		RoleDTO roleDTO = userService.getSuperiorRole(userId);
		boolean isPartnerTeamMember = false;
		if (roleDTO.getTotalRoles() > 0) {
			isPartnerTeamMember = true;
		}
		if ((user.getRoles().stream()
				.anyMatch((role) -> ((role.getRoleId()) == Role.ALL_ROLES.getRoleId()
						|| Role.PRM_ROLE.getRoleId().equals(role.getRoleId())))
				&& (user.getRoles().stream()
						.noneMatch((role) -> (role.getRoleId()) == Role.COMPANY_PARTNER.getRoleId())))) {
			resultMap.put("totalVideosCount", videoDAO.getVideosCount(userIdArray));
			resultMap.put("totalVideoViewsCount", videoDAO.getVideosViewsCount(userIdArray));
			Integer totalCompanyPartnersCount = partnerAnalyticsService
					.getCompanyPartnersCount(user.getCompanyProfile().getId());
			resultMap.put("totalCompanyPartnersCount", totalCompanyPartnersCount);
			Integer totalTeamMembersCount = teamDao.getAllTeamMemberIdsByOrgAdmin(userId).size();
			resultMap.put("totalTeamMembersCount", totalTeamMembersCount);
			getVendorsCount(companyId, resultMap);

		} else if ((user.getRoles().stream()
				.noneMatch((role) -> ((role.getRoleId()) == Role.VIDEO_UPLOAD_ROLE.getRoleId()
						|| (role.getRoleId()) == Role.ALL_ROLES.getRoleId()
						|| Role.PRM_ROLE.getRoleId().equals(role.getRoleId())

				))
				&& (user.getRoles().stream().anyMatch((role) -> (role.getRoleId()) == Role.COMPANY_PARTNER.getRoleId()))
				|| isPartnerTeamMember)) {
			Pagination pagination = new Pagination();
			Integer channelVideosCount = (Integer) videoService.getChannelVideos(pagination, 0, userId)
					.get("totalRecords");
			resultMap.put("totalVideosCount", channelVideosCount);
			Integer channelVideosViewsCount = videoService.getChannelVideosViewsCount(userId);
			resultMap.put("totalVideoViewsCount", channelVideosViewsCount);
			getVendorsCount(companyId, resultMap);
		} else if ((user.getRoles().stream()
				.anyMatch((role) -> (role.getRoleId()) == Role.ALL_ROLES.getRoleId()
						|| Role.PRM_ROLE.getRoleId().equals(role.getRoleId())))
				&& (user.getRoles().stream().anyMatch((role) -> (role.getRoleId()) == Role.COMPANY_PARTNER.getRoleId())
						|| isPartnerTeamMember)) {
			Integer regularVideosCount = videoDAO.getVideosCount(userIdArray);
			Pagination pagination = new Pagination();
			Integer channelVideosCount = (Integer) videoService.getChannelVideos(pagination, 0, userId)
					.get("totalRecords");
			resultMap.put("totalVideosCount", regularVideosCount + channelVideosCount);
			getVendorsCount(companyId, resultMap);
			Integer totalTeamMembersCount = teamDao.getAllTeamMemberIdsByOrgAdmin(userId).size();
			resultMap.put("totalTeamMembersCount", totalTeamMembersCount);

			Integer regularVideosViewsCount = videoDAO.getVideosViewsCount(userIdArray);
			Integer channelVideosViewsCount = videoService.getChannelVideosViewsCount(userId);
			Integer totalCompanyPartnersCount = partnerAnalyticsService
					.getCompanyPartnersCount(user.getCompanyProfile().getId());
			resultMap.put("totalCompanyPartnersCount", totalCompanyPartnersCount);
			resultMap.put("totalVideoViewsCount", regularVideosViewsCount + channelVideosViewsCount);
		} else if ((user.getRoles().stream().anyMatch((role) -> (role.getRoleId()) == Role.VIDEO_UPLOAD_ROLE.getRoleId()
				|| (role.getRoleId()) == Role.STATS_ROLE.getRoleId()))) {
			resultMap.put("totalVideosCount", videoDAO.getVideosCount(userIdArray));
			resultMap.put("totalVideoViewsCount", videoDAO.getVideosViewsCount(userIdArray));

			Integer totalCompanyPartnersCount = partnerAnalyticsService
					.getCompanyPartnersCount(user.getCompanyProfile().getId());
			resultMap.put("totalCompanyPartnersCount", totalCompanyPartnersCount);
		}
		return resultMap;
	}

	private void getVendorsCount(Integer companyId, Map<String, Integer> resultMap) {
		resultMap.put("vendorsCount", vendorAnalyticsService.getVendorsCountByPartnerCompanyId(companyId));
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> countrywiseUsersCount(Integer userId) {
		Map<String, Object> resultMap = new HashMap<String, Object>();

		Map<String, Integer> countrywiseusers = new HashMap<String, Integer>();
		List<Object[]> list = xtremandLogDAO.countrywiseUsersCount(userId);

		MultiMap multiMap = new MultiValueMap();
		for (Object[] row : list) {
			multiMap.put((String) row[1], ((BigInteger) row[0]).intValue());
		}
		Set<String> keys = multiMap.keySet();
		for (String key : keys) {
			ArrayList<Integer> countlist = (ArrayList<Integer>) multiMap.get(key);
			int[] arr = countlist.stream().filter(i -> i != null).mapToInt(i -> i).toArray();
			countrywiseusers.put(key, IntStream.of(arr).sum());
		}

		JSONArray countrywiseViewsJsonArray = new JSONArray();
		for (Map.Entry<String, Integer> entry : countrywiseusers.entrySet()) {
			JSONArray json = new JSONArray();
			json.put(entry.getKey().toLowerCase());
			json.put(entry.getValue());
			countrywiseViewsJsonArray.put(json);
		}

		resultMap.put("countrywiseusers", countrywiseViewsJsonArray);
		return resultMap;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List listWatchedUsersByUser(Integer userId, Integer pageSize, Integer pageNumber) {
		List<Integer> userIdList = new ArrayList<>();
		User user = userService.loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, userId)),
				new FindLevel[] { FindLevel.ROLES });
		userIdList.add(userId);
		List<EmailLogReport> emailLogReportList = new ArrayList<EmailLogReport>();
		List<Object[]> listObjects = xtremandLogDAO.listWatchedUsersByUser(userIdList, pageSize, pageNumber);
		for (Object[] object : listObjects) {
			EmailLogReport emailLogReport = new EmailLogReport();
			emailLogReport.setFirstName(object[1] != null ? (String) object[1] : null);
			emailLogReport.setLastName(object[2] != null ? (String) object[2] : null);
			emailLogReport.setEmailId(object[3] != null ? (String) object[3] : null);
			emailLogReport.setTime(object[4] != null ? (Date) object[4] : null);
			emailLogReport.setCity(object[5] != null ? (String) object[5] : null);
			emailLogReport.setState(object[6] != null ? (String) object[6] : null);
			emailLogReport.setCountry(object[7] != null ? (String) object[7] : null);
			emailLogReport.setOs(object[8] != null ? (String) object[8] : null);
			emailLogReport.setCampaignName(object[9] != null ? (String) object[9] : null);
			emailLogReport.setCountryCode(object[10] != null ? (String) object[10] : null);
			if (emailLogReport.getTime() != null) {
				String utcTimeString = DateUtils.getUtcTimeInString(emailLogReport.getTime(),
						DateUtils.getServerTimeZone());
				emailLogReport.setUtcTimeString(utcTimeString);
			}

			emailLogReportList.add(emailLogReport);
		}
		return emailLogReportList;
	}

	@Override
	public Integer getWatchedUsersCountByUser(Integer userId) {
		List<Integer> userIdList = new ArrayList<>();
		User user = userService.loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, userId)),
				new FindLevel[] { FindLevel.ROLES });
		userIdList.add(userId);
		return xtremandLogDAO.getWatchedUsersCountByUser(userIdList);
	}

	@Override
	@Transactional(readOnly = true)
	public Map<String, Object> getHeatMapByUniqueSession(String sessionId) {
		List<XtremandLog> xtremandLogs = xtremandLogDAO.listXtremandLogsBySessionId(sessionId);
		Map<String, Object> map = new HashMap<>();

		if (!xtremandLogs.isEmpty()) {
			VideoFile videoFile = genericDao.get(VideoFile.class, xtremandLogs.get(0).getVideoId());
			try {
				long videoLength = convertHHMMSSToSeconds(videoFile.getVideoLength());
				map.put("videoLength", videoLength);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		for (XtremandLog xtremandLog : xtremandLogs) {
			xtremandLog.setStartDurationHHMMSS(formatSecondsToHHMMSS(xtremandLog.getStartDuration()));
			xtremandLog.setStopDurationHHMMSS(formatSecondsToHHMMSS(xtremandLog.getStopDuration()));
		}
		map.put("logs", xtremandLogs);
		return map;
	}

	public String formatSecondsToHHMMSS(int timeInSeconds) {
		int hours = timeInSeconds / 3600;
		int secondsLeft = timeInSeconds - hours * 3600;
		int minutes = secondsLeft / 60;
		int seconds = secondsLeft - minutes * 60;

		String formattedTime = "";
		if (hours < 10)
			formattedTime += "0";
		formattedTime += hours + ":";

		if (hours == 0)
			formattedTime = "";

		if (minutes < 10)
			formattedTime += "0";
		formattedTime += minutes + ":";

		if (seconds < 10)
			formattedTime += "0";
		formattedTime += seconds;

		return formattedTime;
	}

	public long convertHHMMSSToSeconds(String timestampStr) throws ParseException {
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		Date reference = dateFormat.parse("00:00:00");
		Date date = dateFormat.parse(timestampStr);
		return (date.getTime() - reference.getTime()) / 1000L;
	}

	@Override
	public List<HeatMapData> getDashboardHeatMapData(Integer userId, String limit) {
		List<HeatMapData> heatMapList = new ArrayList<HeatMapData>();
		List<Object[]> list = xtremandLogDAO.getDashboardHeatMapData(userId, limit);

		for (int i = 0; i < list.size(); i++) {
			Object[] row = list.get(i);
			HeatMapData heatMap = new HeatMapData();
			heatMap.setCampaignId(row[0] != null ? Integer.parseInt(row[0].toString()) : null);
			heatMap.setName(row[1] != null ? row[1].toString() : null);
			heatMap.setValue(row[2] != null ? ((java.math.BigInteger) row[2]).intValue() : null);
			heatMap.setColorValue(row[2] != null ? ((java.math.BigInteger) row[2]).intValue() : null);
			heatMap.setTotalUsers(row[3] != null ? ((java.math.BigInteger) row[3]).intValue() : null);
			Date date = row[4] != null ? (Date) row[4] : null;
			if (date != null) {
				String utcTimeString = DateUtils.getUtcTimeInString(date, DateUtils.getServerTimeZone());
				heatMap.setLaunchTime(utcTimeString);
			}
			// heatMap.setLaunchTime(row[4] != null ? new SimpleDateFormat("MM/dd/yyyy
			// hh:mm:ss a").format(date) : null);
			heatMap.setInteractionPercentage(((java.math.BigDecimal) row[5]).doubleValue());
			heatMap.setCampaignType(String.valueOf(row[6]));
		}
		return heatMapList;
	}

	@Override
	public Map<String, Object> getDashboardBarChartData(Integer userId, List<Integer> campaignIdsList) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		List<String> campaignNames = new ArrayList<String>();
		List<Integer> emailOpenedCount = new ArrayList<Integer>();
		List<Integer> emailClickedCount = new ArrayList<Integer>();
		List<Integer> watchedCount = new ArrayList<Integer>();

		campaignIdsList.sort(Comparator.naturalOrder());
		logger.debug("campaignIdsList: " + campaignIdsList);
		List<Object[]> list = xtremandLogDAO.getCampaignEmailOpenedClickedCount(userId, campaignIdsList);
		for (int i = 0; i < list.size(); i++) {
			Object[] row = list.get(i);
			campaignNames.add(row[0].toString());
			emailOpenedCount.add(((java.math.BigInteger) row[1]).intValue());
			emailClickedCount.add(((java.math.BigInteger) row[2]).intValue());
		}

		logger.debug("campaignIdsList: " + campaignIdsList);
		List<Object[]> watchedist = xtremandLogDAO.getCampaignWatchedCount(userId, campaignIdsList);
		for (int i = 0; i < watchedist.size(); i++) {
			Object[] row = watchedist.get(i);
			watchedCount.add(((java.math.BigInteger) row[1]).intValue());
		}

		resultMap.put("campaignNames", campaignNames);
		resultMap.put("emailOpenedCount", emailOpenedCount);
		resultMap.put("emailClickedCount", emailClickedCount);
		resultMap.put("watchedCount", watchedCount);

		return resultMap;
	}

	@Override
	public VideoStats getDashboardVideoStatsData(Integer userId, Integer daysInterval) {
		Map<String, Integer> viewsMap = new HashMap<>();
		Map<String, Double> minutesWatchedMap = new HashMap<>();
		Map<String, Double> averageDurationMap = new HashMap<>();

		List<String> totalDatesList = new ArrayList<String>();
		LocalDate now = LocalDate.now();
		for (int i = 0; i < daysInterval; i++) {
			Period p1 = Period.ofDays(i);
			totalDatesList.add(now.minus(p1).toString());
		}

		List<String> dbDatesList = new ArrayList<String>();
		List<Integer> views = new ArrayList<Integer>();
		List<Double> minutesWatched = new ArrayList<Double>();
		List<Double> averageDuration = new ArrayList<Double>();
		HashMap<Integer, String> dates = new HashMap<Integer, String>();

		List<Object[]> viewsList = xtremandLogDAO.getDashboardViewsData(userId, daysInterval);
		for (int i = 0; i < viewsList.size(); i++) {
			Object[] row = viewsList.get(i);
			dates.put(i, (row[2].toString()));
			dbDatesList.add(row[2].toString());
			viewsMap.put(row[2].toString(), ((java.math.BigInteger) row[0]).intValue());
		}

		List<Object[]> minutesWatchedList = xtremandLogDAO.getDashboardMinutesWatchedData(userId, daysInterval);
		for (int i = 0; i < minutesWatchedList.size(); i++) {
			Object[] row = minutesWatchedList.get(i);
			minutesWatchedMap.put(row[2].toString(), ((java.math.BigDecimal) row[0]).doubleValue());
		}

		List<Object[]> averageDurationList = xtremandLogDAO.getDashboardAverageDurationData(userId, daysInterval);
		for (int i = 0; i < averageDurationList.size(); i++) {
			Object[] row = averageDurationList.get(i);
			averageDurationMap.put(row[2].toString(), ((java.math.BigDecimal) row[0]).doubleValue());
		}

		totalDatesList.removeIf(dbDatesList::contains);

		int index = 0;
		int limit = dates.size() + totalDatesList.size();
		for (int i = dates.size(); i < limit; i++) {
			dates.put(i, totalDatesList.get(index));
			viewsMap.put(totalDatesList.get(index), 0);
			minutesWatchedMap.put(totalDatesList.get(index), 0.0);
			averageDurationMap.put(totalDatesList.get(index), 0.0);
			index++;
		}

		TreeMap<String, Integer> viewsTreeMap = new TreeMap<>();
		viewsTreeMap.putAll(viewsMap);
		TreeMap<String, Double> minutesWatchedTreeMap = new TreeMap<>();
		minutesWatchedTreeMap.putAll(minutesWatchedMap);
		TreeMap<String, Double> averageDurationTreeMap = new TreeMap<>();
		averageDurationTreeMap.putAll(averageDurationMap);

		Set<String> values = viewsTreeMap.keySet();
		int position = 0;
		for (String key : values) {
			dates.put((position++), key);
		}
		for (Map.Entry<String, Integer> entry : viewsTreeMap.entrySet()) {
			views.add(entry.getValue());

		}
		for (Map.Entry<String, Double> entry : minutesWatchedTreeMap.entrySet()) {
			minutesWatched.add(entry.getValue());

		}
		for (Map.Entry<String, Double> entry : averageDurationTreeMap.entrySet()) {
			averageDuration.add(entry.getValue());

		}
		VideoStats videoStats = new VideoStats();
		videoStats.setViews(views);
		videoStats.setMinutesWatched(minutesWatched);
		videoStats.setAverageDuration(averageDuration);
		videoStats.setDates(dates);
		return videoStats;
	}

	@Override
	public List<Object[]> listTotalMunutesWatchedByTop10Users(Integer videoId) {
		return xtremandLogDAO.listTotalMunutesWatchedByTop10Users(videoId);
	}

	@Override
	public Integer totalVideoViewsCount(Integer videoId) {
		return xtremandLogDAO.totalVideoViewsCount(videoId);
	}

	@Override
	public Map<String, Object> listVideoViewsByTimePeriod(String timePeriod, Integer videoId) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		List<Integer> views = new ArrayList<Integer>();
		List<String> dates = new ArrayList<String>();

		List<Object[]> list = new ArrayList<>();

		switch (timePeriod) {
		case "current-month":
			list = xtremandLogDAO.listCurrentMonthVideoViews(videoId);
			break;
		case "month":
			list = xtremandLogDAO.listMonthWiseVideoViews(videoId);
			break;
		case "quarter":
			list = xtremandLogDAO.listQuarterlyVideoViews(videoId);
			break;
		case "year":
			list = xtremandLogDAO.listYearlyVideoViews(videoId);
			break;
		}

		for (int i = 0; i < list.size(); i++) {
			Object[] row = list.get(i);
			views.add(((java.math.BigInteger) row[0]).intValue());
			dates.add(row[1].toString());
		}
		resultMap.put("views", views);
		resultMap.put("dates", dates);
		return resultMap;
	}

	@Override
	public List<VideoViewsMinutesWatched> listVideoViewsMinutesWatchedByTimePeriod(String timePeriod, Integer videoId,
			String timePeriodValue) {
		List<VideoViewsMinutesWatched> resultList = new ArrayList<VideoViewsMinutesWatched>();

		List<Integer> views = new ArrayList<Integer>();
		List<Double> minutesWatched = new ArrayList<Double>();

		List<Object[]> list = new ArrayList<>();
		switch (timePeriod) {
		case "today":
			list = xtremandLogDAO.listTodayVideoViewsMinutesWatched(videoId);
			break;
		case "month":
			list = xtremandLogDAO.listVideoViewsMinutesWatchedByMonth(videoId, timePeriodValue);
			break;
		case "quarter":
			list = xtremandLogDAO.listVideoViewsMinutesWatchedByQuarter(videoId, timePeriodValue);
			break;
		case "year":
			list = xtremandLogDAO.listVideoViewsMinutesWatchedByYear(videoId, timePeriodValue);
			break;
		}
		for (int i = 0; i < list.size(); i++) {
			Object[] row = list.get(i);
			views.add(((java.math.BigInteger) row[4]).intValue());
			minutesWatched.add(((Number) row[5]).doubleValue());

		}

		Integer maxViews = null;
		Double maxMinutesWatched = null;

		if (list.size() > 0) {
			maxViews = Collections.max(views);
			maxMinutesWatched = Collections.max(minutesWatched);
		}

		for (int i = 0; i < list.size(); i++) {
			Object[] row = list.get(i);
			VideoViewsMinutesWatched videoViewsMinutesWatched = new VideoViewsMinutesWatched();
			videoViewsMinutesWatched.setFirstName((String) row[0]);
			videoViewsMinutesWatched.setLastName((String) row[1]);
			videoViewsMinutesWatched.setEmailId((String) row[2]);
			videoViewsMinutesWatched.setUserId(row[3] != null ? Integer.parseInt(row[3].toString()) : null);
			videoViewsMinutesWatched.setViews(row[4] != null ? ((java.math.BigInteger) row[4]).intValue() : null);
			videoViewsMinutesWatched.setMinutesWatched(row[5] != null ? ((Number) row[5]).doubleValue() : null);

			double viewsColor = Math.round((double) videoViewsMinutesWatched.getViews() / (double) maxViews * 100.0)
					/ 100.0;
			videoViewsMinutesWatched.setViewsColor("rgb(0,0,255," + viewsColor + ")");

			double minutesWatchedColor = Math
					.round(videoViewsMinutesWatched.getMinutesWatched() / maxMinutesWatched * 100.0) / 100.0;
			videoViewsMinutesWatched.setMinutesWatchedColor("rgb(255, 255, 0," + minutesWatchedColor + ")");
			if (videoViewsMinutesWatched.getViews() != null && videoViewsMinutesWatched.getViews() == 1
					&& videoViewsMinutesWatched.getMinutesWatched() == 0) {
				videoViewsMinutesWatched.setMinutesWatched(0.01);
			}

			resultList.add(videoViewsMinutesWatched);
		}
		return resultList;
	}

	@Override
	public List<String> getTimePeriodValues(String timePeriod) {
		List<String> dates = new ArrayList<String>();

		if (timePeriod.equalsIgnoreCase("year")) {
			Calendar cal = Calendar.getInstance();
			dates.add(String.valueOf(cal.get(Calendar.YEAR)));
			dates.add(String.valueOf(cal.get(Calendar.YEAR) - 1));
		} else if (timePeriod.equalsIgnoreCase("month")) {
			Calendar cal = Calendar.getInstance();
			int year = cal.get(Calendar.YEAR);
			int prevYear = cal.get(Calendar.YEAR) - 1;
			List<Integer> yearsList = Arrays.asList(year, prevYear);

			List<String> monthsList = Arrays.asList(new DateFormatSymbols().getShortMonths());
			String currentMOnth = monthsList.get(YearMonth.now().getMonthValue() - 1);

			for (Integer yearValue : yearsList) {
				for (int i = 0; i < monthsList.size() - 1; i++) {
					String month = monthsList.get(i);
					if (yearValue == prevYear) {
						dates.add(month + "-" + prevYear);
					} else if (yearValue == year) {
						dates.add(month + "-" + year);
						if (currentMOnth.equalsIgnoreCase(month)) {
							break;
						}
					}
				}
			}

		} else if (timePeriod.equalsIgnoreCase("quarter")) {
			final Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MONTH, 0);
			int currentQuarter = (cal.get(Calendar.MONTH) / 3) + 1;
			int allQuarters = currentQuarter + 4;
			for (int i = 0; i < allQuarters; i++) {
				if (i == 0) {
					cal.add(Calendar.MONTH, 0);
				} else {
					cal.add(Calendar.MONTH, -3);
				}
				int quarter = (cal.get(Calendar.MONTH) / 3) + 1;
				dates.add("Q" + quarter + "-" + cal.get(Calendar.YEAR));

			}
		}
		return dates;
	}

	@Override
	public Map<String, Object> listVideoViewsMinutesWatchedDetailReport(Integer loggedInUser, String timePeriod,
			Integer userId, Integer videoId, String timePeriodValue, Pagination pagination) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		if (utilService.hasVideoAccess(loggedInUser)) {
			Map<String, Object> map = new HashMap<String, Object>();
			switch (timePeriod) {
			case "today":
				map = xtremandLogDAO.listTodayVideoViewsMinutesWatchedDetailReport(userId, videoId, pagination);
				break;
			case "month":
				map = xtremandLogDAO.listVideoViewsMinutesWatchedDetailReportByMonth(userId, videoId, timePeriodValue,
						pagination);
				break;
			case "quarter":
				map = xtremandLogDAO.listVideoViewsMinutesWatchedDetailReportByQuarter(userId, videoId, timePeriodValue,
						pagination);
				break;
			case "year":
				map = xtremandLogDAO.listVideoViewsMinutesWatchedDetailReportByYear(userId, videoId, timePeriodValue,
						pagination);
				break;
			}

			Integer totalRecords = Integer.parseInt(map.get("totalRecords").toString());
			@SuppressWarnings("unchecked")
			List<Object[]> list = (List<Object[]>) map.get("data");
			List<VideoViewsMinutesWatched> resultList = new ArrayList<VideoViewsMinutesWatched>();
			for (int i = 0; i < list.size(); i++) {
				Object[] row = list.get(i);
				VideoViewsMinutesWatched videoViewsMinutesWatched = new VideoViewsMinutesWatched();
				videoViewsMinutesWatched.setVideoTitle((String) row[1]);
				videoViewsMinutesWatched.setFirstName((String) row[2]);
				videoViewsMinutesWatched.setLastName((String) row[3]);
				videoViewsMinutesWatched.setEmailId((String) row[4]);
				videoViewsMinutesWatched.setDate(row[5] != null ? row[5].toString() : null);
				videoViewsMinutesWatched.setMinutesWatched(row[6] != null ? ((Number) row[6]).doubleValue() : null);
				videoViewsMinutesWatched.setDevice((String) row[7]);
				videoViewsMinutesWatched.setCity((String) row[8]);
				videoViewsMinutesWatched.setState((String) row[9]);
				videoViewsMinutesWatched.setCountry((String) row[10]);
				String countryName = WordUtils.capitalizeFully((String) row[10]);
				if (StringUtils.hasText(countryName) && !countryName.equalsIgnoreCase("Select Country")) {
					String code = CountryCode.findByName(countryName).get(0).name();
					videoViewsMinutesWatched.setCountryCode(code);
				}
				videoViewsMinutesWatched.setCampaignName((String) row[11]);

				if (videoViewsMinutesWatched.getMinutesWatched() != null
						&& videoViewsMinutesWatched.getMinutesWatched() == 0) {
					videoViewsMinutesWatched.setMinutesWatched(0.01);
				}
				if (videoViewsMinutesWatched.getDate() != null) {
					String utcTimeString = DateUtils.getUtcTimeInString((Date) row[5], DateUtils.getServerTimeZone());
					videoViewsMinutesWatched.setUtcTimeString(utcTimeString);
				}
				resultList.add(videoViewsMinutesWatched);
			}
			resultMap.put("data", resultList);
			resultMap.put("totalRecords", totalRecords);
			resultMap.put("access", true);
		} else {
			resultMap.put("access", false);
		}

		return resultMap;
	}

	@Override
	public VideoViewsMinutesWatched listVideoViewsDetailReport1(String timePeriod, Integer videoId,
			String timePeriodValue) {
		Object[] result = null;
		switch (timePeriod) {
		case "current-month":
			result = xtremandLogDAO.listCurrentMonthVideoViewsDetailReport1(videoId, timePeriodValue);
			break;
		case "month":
			result = xtremandLogDAO.listVideoViewsByMonthDetailReport1(videoId, timePeriodValue);
			break;
		case "quarter":
			result = xtremandLogDAO.listVideoViewsByQuarterDetailReport1(videoId, timePeriodValue);
			break;
		case "year":
			result = xtremandLogDAO.listVideoViewsByYearDetailReport1(videoId, timePeriodValue);
			break;
		}
		VideoViewsMinutesWatched videoViewsMinutesWatched = new VideoViewsMinutesWatched();
		if (result != null) {
			videoViewsMinutesWatched.setDate(result[0].toString());
			videoViewsMinutesWatched.setVideoTitle(result[2].toString());
			videoViewsMinutesWatched.setViews(((java.math.BigInteger) result[3]).intValue());
		}
		return videoViewsMinutesWatched;
	}

	@Override
	public Map<String, Object> listVideoViewsDetailReport2(Integer userId, String timePeriod, Integer videoId,
			String timePeriodValue, Pagination pagination) {
		Map<String, Object> resultMap = new HashMap<String, Object>();

		if (utilService.hasVideoAccess(userId)) {
			Map<String, Object> map = new HashMap<String, Object>();
			switch (timePeriod) {
			case "current-month":
				map = xtremandLogDAO.listCurrentMonthVideoViewsDetailReport2(videoId, timePeriodValue, pagination);
				break;
			case "month":
				map = xtremandLogDAO.listVideoViewsByMonthDetailReport2(videoId, timePeriodValue, pagination);
				break;
			case "quarter":
				map = xtremandLogDAO.listVideoViewsByQuarterDetailReport2(videoId, timePeriodValue, pagination);
				break;
			case "year":
				map = xtremandLogDAO.listVideoViewsByYearDetailReport2(videoId, timePeriodValue, pagination);
				break;
			}
			Integer totalRecords = Integer.parseInt(map.get("totalRecords").toString());
			@SuppressWarnings("unchecked")
			List<Object[]> list = (List<Object[]>) map.get("data");
			List<VideoViewsMinutesWatched> resultList = new ArrayList<VideoViewsMinutesWatched>();
			for (int i = 0; i < list.size(); i++) {
				Object[] row = list.get(i);
				Integer views = row[6] != null ? ((java.math.BigInteger) row[6]).intValue() : null;
				if (views != null && views != 0) {
					VideoViewsMinutesWatched videoViewsMinutesWatched = new VideoViewsMinutesWatched();
					videoViewsMinutesWatched.setDate(row[0] != null ? row[0].toString() : null);
					videoViewsMinutesWatched.setVideoTitle((String) row[2]);
					videoViewsMinutesWatched.setEmailId((String) row[5]);
					videoViewsMinutesWatched.setFirstName((String) row[3]);
					videoViewsMinutesWatched.setLastName((String) row[4]);
					videoViewsMinutesWatched
							.setViews(row[6] != null ? ((java.math.BigInteger) row[6]).intValue() : null);
					videoViewsMinutesWatched.setDevice((String) row[7]);
					videoViewsMinutesWatched.setCity((String) row[8]);
					videoViewsMinutesWatched.setState((String) row[9]);
					videoViewsMinutesWatched.setCountry((String) row[10]);
					String countryName = WordUtils.capitalizeFully((String) row[10]);
					if (StringUtils.hasText(countryName) && !countryName.equalsIgnoreCase("Select Country")) {
						String code = CountryCode.findByName(countryName).get(0).name();
						videoViewsMinutesWatched.setCountryCode(code);
					}
					resultList.add(videoViewsMinutesWatched);
				}
			}
			resultMap.put("data", resultList);
			resultMap.put("totalRecords", totalRecords);
			resultMap.put("access", true);
		} else {
			resultMap.put("access", false);
		}
		return resultMap;
	}

	@Override
	public Map<String, Object> getVideoSkippedDurationData(Integer videoId) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		List<Integer> durationList = new ArrayList<Integer>();
		List<Integer> viewsList = new ArrayList<Integer>();
		List<Integer> skippedList = new ArrayList<Integer>();
		List<Object[]> list = xtremandLogDAO.getVideoSkippedDurationData(videoId);
		for (int i = 0; i < list.size(); i++) {
			Object[] row = list.get(i);
			durationList.add(Integer.parseInt(row[0].toString()));
			viewsList.add(((java.math.BigInteger) row[1]).intValue());
			skippedList.add(((java.math.BigInteger) row[2]).intValue());
		}
		resultMap.put("duration", durationList);
		resultMap.put("views", viewsList);
		resultMap.put("skipped", skippedList);
		return resultMap;
	}

	@Override
	public Map<String, Object> getVideoWatchedFullyDetailReport(Integer videoId, Pagination pagination,
			Integer userId) {
		Map<String, Object> resultMap = new HashMap<String, Object>();

		if (utilService.hasVideoAccess(userId)) {
			Map<String, Object> map = xtremandLogDAO.getVideoWatchedFullyDetailReport(videoId, pagination);
			Integer totalRecords = Integer.parseInt(map.get("totalRecords").toString());
			@SuppressWarnings("unchecked")
			List<Object[]> list = (List<Object[]>) map.get("data");
			List<EmailLogReport> resultList = new ArrayList<EmailLogReport>();
			for (int i = 0; i < list.size(); i++) {
				Object[] row = list.get(i);
				EmailLogReport emailLogReport = new EmailLogReport();
				emailLogReport.setCampaignName(row[0] != null ? row[0].toString() : null);
				emailLogReport.setEmailId(row[1] != null ? row[1].toString() : null);
				emailLogReport.setFirstName(row[2] != null ? row[2].toString() : null);
				emailLogReport.setLastName(row[3] != null ? row[3].toString() : null);
				emailLogReport.setTime(row[4] != null ? (Date) row[4] : null);
				emailLogReport.setDeviceType(row[5] != null ? row[5].toString() : null);
				emailLogReport.setCity(row[6] != null ? row[6].toString() : null);
				emailLogReport.setState(row[7] != null ? row[7].toString() : null);
				emailLogReport.setCountry(row[8] != null ? row[8].toString() : null);
				String countryName = WordUtils.capitalizeFully((String) row[8]);
				if (StringUtils.hasText(countryName) && !countryName.equalsIgnoreCase("Select Country")) {
					String code = CountryCode.findByName(countryName).get(0).name();
					emailLogReport.setCountryCode(code);
				}
				if (emailLogReport.getTime() != null) {
					String utcTimeString = DateUtils.getUtcTimeInString(emailLogReport.getTime(),
							DateUtils.getServerTimeZone());
					emailLogReport.setUtcTimeString(utcTimeString);
				}

				resultList.add(emailLogReport);
			}
			resultMap.put("data", resultList);
			resultMap.put("totalRecords", totalRecords);
			resultMap.put("access", true);
		} else {
			resultMap.put("access", false);
		}
		return resultMap;
	}

	@Override
	public XtremandResponse listTotalMinutesWatchedByTop10UsersDetailReport(Integer videoId, Integer userId) {
		XtremandResponse response = new XtremandResponse();
		List<VideoViewsMinutesWatched> resultList = new ArrayList<VideoViewsMinutesWatched>();

		if (utilService.hasVideoAccess(userId)) {
			List<Object[]> list = xtremandLogDAO.listTotalMinutesWatchedByTop10UsersDetailReport(videoId);
			for (int i = 0; i < list.size(); i++) {
				Object[] row = list.get(i);
				VideoViewsMinutesWatched videoViewsMinutesWatched = new VideoViewsMinutesWatched();
				videoViewsMinutesWatched.setFirstName(row[0] != null ? row[0].toString() : null);
				videoViewsMinutesWatched.setLastName(row[1] != null ? row[1].toString() : null);
				videoViewsMinutesWatched.setEmailId(row[2] != null ? row[2].toString() : null);
				videoViewsMinutesWatched.setDate(row[3] != null ? row[3].toString() : null);
				videoViewsMinutesWatched.setMinutesWatched(row[4] != null ? ((Number) row[4]).doubleValue() : null);
				videoViewsMinutesWatched.setDevice(row[5] != null ? row[5].toString() : null);
				videoViewsMinutesWatched.setCity((String) row[6]);
				videoViewsMinutesWatched.setState((String) row[7]);
				videoViewsMinutesWatched.setCountry((String) row[8]);
				String countryName = WordUtils.capitalizeFully((String) row[8]);
				if (StringUtils.hasText(countryName) && !countryName.equalsIgnoreCase("Select Country")) {
					String code = CountryCode.findByName(countryName).get(0).name();
					videoViewsMinutesWatched.setCountryCode(code);
				}
				if (row[3] != null) {
					String utcTimeString = DateUtils.getUtcTimeInString((Date) row[3], DateUtils.getServerTimeZone());
					videoViewsMinutesWatched.setUtcTimeString(utcTimeString);
				}
				resultList.add(videoViewsMinutesWatched);
			}
			response.setAccess(true);
			response.setData(resultList);
		} else {
			response.setAccess(false);
		}
		return response;
	}

	@Override
	public Map<String, Object> listVideoDurationPlayedUsers(Integer userId, Integer videoId, Pagination pagination) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		if (utilService.hasVideoAccess(userId)) {
			Map<String, Object> map = xtremandLogDAO.listVideoDurationPlayedUsers(videoId, pagination);
			Integer totalRecords = Integer.parseInt(map.get("totalRecords").toString());
			@SuppressWarnings("unchecked")
			List<Object[]> list = (List<Object[]>) map.get("data");
			List<VideoViewsMinutesWatched> resultList = new ArrayList<VideoViewsMinutesWatched>();
			for (int i = 0; i < list.size(); i++) {
				Object[] row = list.get(i);
				VideoViewsMinutesWatched videoViewsMinutesWatched = new VideoViewsMinutesWatched();
				videoViewsMinutesWatched.setDuration(Integer.parseInt(row[0] != null ? row[0].toString() : null));
				videoViewsMinutesWatched.setFirstName(row[1] != null ? row[1].toString() : null);
				videoViewsMinutesWatched.setLastName(row[2] != null ? row[2].toString() : null);
				videoViewsMinutesWatched.setEmailId(row[3] != null ? row[3].toString() : null);
				if (row[4] != null) {
					String utcTimeString = DateUtils.getUtcTimeInString((Date) row[4], DateUtils.getServerTimeZone());
					videoViewsMinutesWatched.setDate(utcTimeString);
				}
				if (row[5] != null) {
					String utcTimeString = DateUtils.getUtcTimeInString((Date) row[5], DateUtils.getServerTimeZone());
					videoViewsMinutesWatched.setEndTime(utcTimeString);
				}
				videoViewsMinutesWatched.setDevice(row[6] != null ? row[6].toString() : null);
				videoViewsMinutesWatched.setLocation(row[7] != null ? row[7].toString() : null);
				resultList.add(videoViewsMinutesWatched);
			}
			resultMap.put("data", resultList);
			resultMap.put("totalRecords", totalRecords);
			resultMap.put("access", true);
		} else {
			resultMap.put("access", false);
		}

		return resultMap;
	}

	@Override
	public Map<String, Object> listVideoDurationSkippedUsers(Integer userId, Integer videoId, Pagination pagination) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		if (utilService.hasVideoAccess(userId)) {
			Map<String, Object> map = xtremandLogDAO.listVideoDurationSkippedUsers(videoId, pagination);
			Integer totalRecords = Integer.parseInt(map.get("totalRecords").toString());
			@SuppressWarnings("unchecked")
			List<Object[]> list = (List<Object[]>) map.get("data");
			List<VideoViewsMinutesWatched> resultList = new ArrayList<VideoViewsMinutesWatched>();
			for (int i = 0; i < list.size(); i++) {
				Object[] row = list.get(i);
				VideoViewsMinutesWatched videoViewsMinutesWatched = new VideoViewsMinutesWatched();
				videoViewsMinutesWatched.setDuration(Integer.parseInt(row[0] != null ? row[0].toString() : null));
				videoViewsMinutesWatched.setFirstName(row[1] != null ? row[1].toString() : null);
				videoViewsMinutesWatched.setLastName(row[2] != null ? row[2].toString() : null);
				videoViewsMinutesWatched.setEmailId(row[3] != null ? row[3].toString() : null);
				if (row[4] != null) {
					String utcTimeString = DateUtils.getUtcTimeInString((Date) row[4], DateUtils.getServerTimeZone());
					videoViewsMinutesWatched.setDate(utcTimeString);
				}
				if (row[5] != null) {
					String utcTimeString = DateUtils.getUtcTimeInString((Date) row[5], DateUtils.getServerTimeZone());
					videoViewsMinutesWatched.setEndTime(utcTimeString);
				}
				videoViewsMinutesWatched.setDevice(row[6] != null ? row[6].toString() : null);
				videoViewsMinutesWatched.setLocation(row[7] != null ? row[7].toString() : null);
				resultList.add(videoViewsMinutesWatched);
			}
			resultMap.put("data", resultList);
			resultMap.put("totalRecords", totalRecords);
			resultMap.put("access", true);
		} else {
			resultMap.put("access", false);
		}
		return resultMap;
	}

	@Override
	public VideoViewsMinutesWatched getNAUsersVideoViewsMinutesWatched(Integer videoId) {
		List<Number> list = xtremandLogDAO.getNAUsersVideoViewsMinutesWatched(videoId);
		VideoViewsMinutesWatched videoViewsMinutesWatched = new VideoViewsMinutesWatched();

		for (int i = 0; i < list.size(); i++) {
			Number row = list.get(i);
			if (i == 0) {
				videoViewsMinutesWatched.setViews(((Double) ((Number) row).doubleValue()).intValue());
			} else if (i == 1) {
				videoViewsMinutesWatched.setMinutesWatched(((Number) row).doubleValue());
			}

		}
		return videoViewsMinutesWatched;
	}

	@Override
	public Map<String, Object> getDashboardWorldMapDetailReport(Integer userId, String countryCode,
			Pagination pagination) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		List<EmailLogReport> resultList = new ArrayList<EmailLogReport>();
		Map<String, Object> map = xtremandLogDAO.getDashboardWorldMapDetailReport(pagination, userId, countryCode);
		Integer totalRecords = Integer.parseInt(map.get("totalRecords").toString());
		@SuppressWarnings("unchecked")
		List<Object[]> list = (List<Object[]>) map.get("data");
		for (Object[] object : list) {
			EmailLogReport emailLogReport = new EmailLogReport();

			emailLogReport.setFirstName(object[0] != null ? (String) object[0] : null);
			emailLogReport.setLastName(object[1] != null ? (String) object[1] : null);
			emailLogReport.setEmailId(object[2] != null ? (String) object[2] : null);
			emailLogReport.setTime(object[3] != null ? (Date) object[3] : null);
			emailLogReport.setDeviceType(object[4] != null ? (String) object[4] : null);
			emailLogReport.setCity(object[5] != null ? (String) object[5] : null);
			emailLogReport.setState(object[6] != null ? (String) object[6] : null);
			emailLogReport.setCountry(object[7] != null ? (String) object[7] : null);
			emailLogReport.setOs(object[8] != null ? (String) object[8] : null);
			if (emailLogReport.getTime() != null) {
				String utcTimeString = DateUtils.getUtcTimeInString(emailLogReport.getTime(),
						DateUtils.getServerTimeZone());
				emailLogReport.setUtcTimeString(utcTimeString);
			}
			resultList.add(emailLogReport);
		}
		resultMap.put("data", resultList);
		resultMap.put("totalRecords", totalRecords);
		return resultMap;
	}

	@Override
	public Map<String, Object> getVideoViewsCountByCountry(Integer videoId) {
		Map<String, Object> resultMap = new HashMap<String, Object>();

		Map<String, Integer> countrywiseusers = new HashMap<String, Integer>();
		List<Object[]> list = xtremandLogDAO.getVideoViewsCountByCountry(videoId);

		MultiMap multiMap = new MultiValueMap();
		for (Object[] row : list) {
			multiMap.put((String) row[1], ((BigInteger) row[0]).intValue());
		}
		Set<String> keys = multiMap.keySet();
		for (String key : keys) {
			ArrayList<Integer> countlist = (ArrayList) multiMap.get(key);
			int[] arr = countlist.stream().filter(i -> i != null).mapToInt(i -> i).toArray();
			countrywiseusers.put(key, IntStream.of(arr).sum());
		}

		JSONArray countrywiseViewsJsonArray = new JSONArray();
		for (Map.Entry<String, Integer> entry : countrywiseusers.entrySet()) {
			JSONArray json = new JSONArray();
			json.put(entry.getKey().toLowerCase());
			json.put(entry.getValue());
			countrywiseViewsJsonArray.put(json);
		}

		resultMap.put("countrywiseusers", countrywiseViewsJsonArray);
		return resultMap;

	}

	@Override
	public Map<String, Object> listCountryWiseVideoViewsDetailReport(Integer userId, Integer videoId,
			String countryCode, Pagination pagination) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		if (utilService.hasVideoAccess(userId)) {
			Map<String, Object> map = xtremandLogDAO.listCountryWiseVideoViewsDetailReport(videoId, countryCode,
					pagination);
			Integer totalRecords = Integer.parseInt(map.get("totalRecords").toString());
			@SuppressWarnings("unchecked")
			List<EmailLogReport> resultList = (List<EmailLogReport>) map.get("data");
			for (EmailLogReport emailLogReport : resultList) {
				if (emailLogReport.getTime() != null) {
					String utcTimeString = DateUtils.getUtcTimeInString(emailLogReport.getTime(),
							DateUtils.getServerTimeZone());
					emailLogReport.setUtcTimeString(utcTimeString);
				}
			}
			resultMap.put("data", resultList);
			resultMap.put("totalRecords", totalRecords);
			resultMap.put("access", true);
		} else {
			resultMap.put("access", false);
		}
		return resultMap;
	}

	@Override
	public Map<String, Object> listLeadsDetails(Integer userId, Integer videoId, Pagination pagination) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		if (utilService.hasVideoAccess(userId)) {
			Map<String, Object> map = xtremandLogDAO.listLeadsDetails(videoId, pagination);
			Integer totalRecords = Integer.parseInt(map.get("totalRecords").toString());
			@SuppressWarnings("unchecked")
			List<Object[]> list = (List<Object[]>) map.get("data");
			List<EmailLogReport> resultList = new ArrayList<EmailLogReport>();
			for (Object[] object : list) {
				EmailLogReport emailLogReport = new EmailLogReport();
				emailLogReport.setEmailId(object[12] != null ? (String) object[12] : null);
				emailLogReport.setFirstName(object[13] != null ? (String) object[13] : null);
				emailLogReport.setLastName(object[14] != null ? (String) object[14] : null);
				// XtremandLog xtremandLog = ((XtremandLog) object[0]);
				emailLogReport.setDeviceType(object[0] != null ? (String) object[0] : null);
				emailLogReport.setOs(object[1] != null ? (String) object[1] : null);
				emailLogReport.setCity(object[2] != null ? (String) object[2] : null);
				emailLogReport.setCountry(object[3] != null ? (String) object[3] : null);
				emailLogReport.setIsp(object[4] != null ? (String) object[4] : null);
				emailLogReport.setIpAddress(object[5] != null ? (String) object[5] : null);
				emailLogReport.setState(object[6] != null ? (String) object[6] : null);
				emailLogReport.setZip(object[7] != null ? (String) object[7] : null);
				emailLogReport.setLatitude(object[8] != null ? (String) object[8] : null);
				emailLogReport.setLongitude(object[9] != null ? (String) object[9] : null);
				emailLogReport.setCountryCode(object[10] != null ? (String) object[10] : null);
				emailLogReport.setTime(object[11] != null ? (Date) object[11] : null);
				if (emailLogReport.getTime() != null) {
					String utcTimeString = DateUtils.getUtcTimeInString(emailLogReport.getTime(),
							DateUtils.getServerTimeZone());
					emailLogReport.setUtcTimeString(utcTimeString);
				}
				resultList.add(emailLogReport);
			}
			resultMap.put("data", resultList);
			resultMap.put("totalRecords", totalRecords);
			resultMap.put("access", true);
		} else {
			resultMap.put("access", false);
		}
		return resultMap;
	}

	@Override
	public boolean executePost(String input) {
		boolean success = false;
		try {
			System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
			CloseableHttpClient httpclient = HttpClientBuilder.create().build();
			try {
				HttpPost httpost = new HttpPost(googleCaptchaVerificationUrl);
				List<NameValuePair> nvps = new ArrayList<NameValuePair>();
				nvps.add(new BasicNameValuePair("secret", googleCaptchaSecretKey));
				nvps.add(new BasicNameValuePair("response", input));

				httpost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
				CloseableHttpResponse closeableresponse = httpclient.execute(httpost);
				logger.debug("Response Status line :" + closeableresponse.getStatusLine());

				try {
					HttpEntity entity = closeableresponse.getEntity();
					InputStream rstream = entity.getContent();
					JSONObject authResponse = new JSONObject(new JSONTokener(rstream));
					success = authResponse.getBoolean("success");
				} catch (JSONException e) {

				}

				finally {
					closeableresponse.close();
				}
			} catch (IOException ex) {
				logger.error(" Exception occured :  " + ex.getMessage());

			} finally {
				try {
					httpclient.close();
				} catch (IOException e) {
					logger.error(" Exception occured :  " + e.getMessage());

				}
			}
		} catch (UnsupportedOperationException exception) {
			logger.error(exception.getMessage(), exception);

		}
		return success;

	}

}