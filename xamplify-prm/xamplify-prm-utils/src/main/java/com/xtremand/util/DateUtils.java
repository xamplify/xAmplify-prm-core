package com.xtremand.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.util.dto.DateDTO;
import com.xtremand.util.dto.DateInString;

/**
 * 
 * @author Prabhakar
 * @since 21st June 2015 Utility class for date modifications and conversions
 *
 */
public class DateUtils {

	private static final String DATE_TIME_24_FORMAT = "yyyy-MM-dd HH:mm";

	private static final String DATE_TIME_24_FORMAT_SECONDS = "yyyy-MM-dd HH:mm:ss";

	private static final String DATE_TIME_24_FORMAT_ZONE = "EEEE, MMM dd, yyyy 'at' HH:mm a z";

	private static final String DATE_TIME_12_FORMAT_ZONE = "EEEE, MMM dd, yyyy 'at' hh:mm a z";

	private static final String DATE_TIME_24_FORMAT_DAY = "EEE MMM dd yyyy HH:mm a";

	private static final String DATE_TIME_12_FORMAT_AMPM = "EEEE, MMMM dd, yyyy h:mm a";

	private static final String TIME_IN_AM_PM = "hh:mm a";

	private static final String CAMPAIGN_DISPLAY_DATE_FORMAT = "MMM dd, yyyy, ";

	private static final String MDF_CREATED_DATE_FORMAT = "MMM dd, yyyy";

	private static final String CAMPAIGN_DISPLAY_TIME_FORMAT = "h:mm aa";

	private static final String ONLY_DATE_FORMAT = "MM/dd/yyyy";

	private static final String CAMPAIGN_DISPLAY_DATE_AND_TIME_FORMAT = "MM/dd/yyyy h:mm a";

	private static final Logger logger = LoggerFactory.getLogger(DateUtils.class);

	private DateUtils() {

	}

	/**
	 * 
	 * @param date
	 * @return converted string format date
	 */
	public static String dateToString(Date date) {

		SimpleDateFormat formater = new SimpleDateFormat(DATE_TIME_24_FORMAT_SECONDS);
		return formater.format(date);

	}

	public static String dateToStringDate(Date date) {

		SimpleDateFormat formater = new SimpleDateFormat(DATE_TIME_24_FORMAT_DAY);
		return formater.format(date);

	}

	public static String dateToStringDate12HRAMPM(Date date) {

		SimpleDateFormat formater = new SimpleDateFormat(DATE_TIME_12_FORMAT_AMPM);
		return formater.format(date);

	}

	public static String convertDateToStringForCampaign(Date date) {
		SimpleDateFormat formater = new SimpleDateFormat("MMM d yyyy h:mm aa");
		return formater.format(date);
	}

	public static String convertToShortDateString(Date date) {
		SimpleDateFormat formater = new SimpleDateFormat("MMM dd,yyyy");
		return formater.format(date);
	}

	public static String convertToOnlyDate(Date date) {
		return DateInString.convertToOnlyDate(date);

	}

	public static String getNextDayDateString(String dateString) {
		String nextDay = null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Calendar c = Calendar.getInstance();
			c.setTime(sdf.parse(dateString));
			c.add(Calendar.DATE, 1); // number of days to add
			nextDay = sdf.format(c.getTime());
			nextDay = nextDay + " 00:00";
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return nextDay;
	}

	public static String convertToOnlyDateStringYMDFormat(Date date) {
		if (date != null) {
			SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
			return formater.format(date);
		} else {
			return "";
		}

	}

	public static String convertDateToString(Date date) {
		SimpleDateFormat formater = new SimpleDateFormat("MMM dd,yyyy h:mm:ss aa");
		return formater.format(date);
	}

	public static String[] getStartAndEndDates(Integer daysToBack) {
		SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
		String[] dates = new String[2];
		Calendar cal = Calendar.getInstance();
		dates[0] = formater.format(cal.getTime());
		cal.add(Calendar.DATE, -daysToBack);
		dates[1] = formater.format(cal.getTime());
		return dates;
	}

	public static String convertDateToStringWithOutSec(Date date) {
		if (date != null) {
			SimpleDateFormat formater = new SimpleDateFormat(DATE_TIME_24_FORMAT);
			return formater.format(date);
		} else {
			return "";
		}
	}

	public static String convertDateToStringWithOutSecond(Date date) {
		if (date != null) {
			SimpleDateFormat formater = new SimpleDateFormat(DATE_TIME_12_FORMAT_AMPM);
			return formater.format(date);
		} else {
			return "";
		}
	}

	public static String convertDateToStringWithZone(Date date) {
		if (date != null) {
			SimpleDateFormat formater = new SimpleDateFormat(DATE_TIME_24_FORMAT_ZONE);
			return formater.format(date);
		} else {
			return "";
		}
	}

	public static String convertDateToStringWithZone12Hr(Date date) {
		if (date != null) {
			SimpleDateFormat formater = new SimpleDateFormat(DATE_TIME_12_FORMAT_ZONE);
			return formater.format(date);
		} else {
			return "";
		}
	}

	public static String convertUTCTimeInString(ZonedDateTime date) {
		if (date != null) {
			DateTimeFormatter format = DateTimeFormatter.ofPattern(DATE_TIME_24_FORMAT);
			return format.format(date);
		} else {
			return "";
		}
	}

	public static Date addReplyDaysToLauchedTime(Date launchDate, int replyDays, Date replyTime) {
		try {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(launchDate);
			calendar.add(Calendar.DATE, replyDays);
			return addTimeToReplyDay(calendar, replyTime);
		} catch (Exception e) {
			logger.error("addReplyDaysToLauchedTime(LaunchDate:" + launchDate + ",replyDays:" + replyDays
					+ ",replyTime:" + replyTime, e);
			throw new XamplifyDataAccessException(e.getMessage());
		}

	}

	public static Date addTimeToReplyDay(Calendar calendar, Date replyTime) {
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
		String time = timeFormat.format(replyTime);
		int hours = Integer.parseInt(time.split(":")[0]);
		int minutes = Integer.parseInt(time.split(":")[1]);
		calendar.set(Calendar.HOUR_OF_DAY, hours);
		calendar.set(Calendar.MINUTE, minutes);
		return calendar.getTime();
	}

	public static ZonedDateTime convertToUTC(String timeZoneId, String convertedTimeInString) {
		ZoneId zoneId = ZoneId.of(timeZoneId);
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_24_FORMAT);
		LocalDateTime localDateTime = LocalDateTime.parse(convertedTimeInString, dateTimeFormatter);
		ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, zoneId);
		return zonedDateTime.withZoneSameInstant(ZoneOffset.UTC);
	}

	public static ZonedDateTime getServerTimeInUTC() {
		Instant instant = Instant.now();
		String currentTimeZone = getServerTimeZone();
		ZoneOffset off = ZoneOffset.of(currentTimeZone);
		OffsetDateTime sc = OffsetDateTime.ofInstant(instant, off);
		DateTimeFormatter format = DateTimeFormatter.ofPattern(DATE_TIME_24_FORMAT);
		String currentServerDateInString = format.format(sc);
		return convertToUTC(currentTimeZone, currentServerDateInString);
	}

	public static String getServerTimeZone() {
		return String.valueOf(ZoneOffset.systemDefault().getRules().getOffset(Instant.now()));
	}

	public static Date getDateWithOutTime(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		return calendar.getTime();
	}

	public static Date getCalculatedReplyTime(int days, Date replyTime, Date launchTime) {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
		String time = timeFormat.format(replyTime);
		int hours = Integer.parseInt(time.split(":")[0]);
		int minutes = Integer.parseInt(time.split(":")[1]);
		return setCalendarData(days, launchTime, calendar, hours, minutes);
	}

	public static Date getCalculatedAutoResposeReplyTime(int days, String timeAndHoursInMintues, Date launchTime) {
		Calendar calendar = Calendar.getInstance();
		int hours = Integer.parseInt(timeAndHoursInMintues.split(":")[0]);
		int minutes = Integer.parseInt(timeAndHoursInMintues.split(":")[1]);
		return setCalendarData(days, launchTime, calendar, hours, minutes);
	}

	public static Date getCalculatedAutoResponseReplyTime(int days, String timeAndHoursInMintues, Date startTime) {
		Calendar calendar = Calendar.getInstance();
		int hours = Integer.parseInt(timeAndHoursInMintues.split(":")[0]);
		int minutes = Integer.parseInt(timeAndHoursInMintues.split(":")[1]);
		calendar.setTime(getDateWithOutTime(startTime));
		calendar.add(Calendar.DATE, -days);
		calendar.add(Calendar.HOUR_OF_DAY, hours);
		calendar.add(Calendar.MINUTE, minutes);
		return calendar.getTime();
	}

	/**
	 * @param days
	 * @param launchTime
	 * @param calendar
	 * @param hours
	 * @param minutes
	 * @return
	 */
	private static Date setCalendarData(int days, Date launchTime, Calendar calendar, int hours, int minutes) {
		calendar.setTime(getDateWithOutTime(launchTime));
		calendar.add(Calendar.DATE, days);
		calendar.add(Calendar.HOUR_OF_DAY, hours);
		calendar.add(Calendar.MINUTE, minutes);
		return calendar.getTime();
	}

	public static Date convertTimeToDate(String time, Date launchTime) {
		Calendar calendar = Calendar.getInstance();
		int hours = Integer.parseInt(time.split(":")[0]);
		int minutes = Integer.parseInt(time.split(":")[1]);
		return setCalendarData(0, launchTime, calendar, hours, minutes);
	}

	public static ZonedDateTime getClientDateTimeInString(String clientTimeZone, Date launchTime) {
		String dateInString = getDateInClientTimeZone(clientTimeZone, launchTime);
		return clientTimeInUTC(dateInString, clientTimeZone);
	}

	/**
	 * @param clientTimeZone
	 * @return
	 */
	public static String getDateInClientTimeZone(String clientTimeZone, Date time) {
		SimpleDateFormat sdfAmerica = new SimpleDateFormat(DATE_TIME_24_FORMAT);
		sdfAmerica.setTimeZone(TimeZone.getTimeZone(clientTimeZone));
		return sdfAmerica.format(time);
	}

	public static ZonedDateTime clientTimeInUTC(String dateInString, String clientTimeZone) {
		ZoneId zoneId = ZoneId.of(clientTimeZone);
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_24_FORMAT);
		LocalDateTime localDateTime = LocalDateTime.parse(dateInString, dateTimeFormatter);
		ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, zoneId);
		return zonedDateTime.withZoneSameInstant(ZoneOffset.UTC);
	}

	public static ZonedDateTime getCampaignRulesTimeInUTC(String timeInString, String clientTimeZone, Date launchTime) {
		String launchTimeInString = convertDateToStringWithOutSec(launchTime);
		StringBuilder date = new StringBuilder(launchTimeInString.split(" ")[0]);
		String hours = timeInString.split(":")[0];
		String minutes = timeInString.split(":")[1];
		if (hours.length() == 1) {
			hours = "0" + hours;
		}
		if (minutes.length() == 1) {
			minutes = "0" + minutes;
		}
		date.append(" " + hours + ":" + minutes);
		return clientTimeInUTC(String.valueOf(date), clientTimeZone);

	}

	public static String splitHoursAndMinutes(Date date) {
		if (date != null) {
			SimpleDateFormat sdf = new SimpleDateFormat(TIME_IN_AM_PM);
			return sdf.format(date);
		} else {
			return "-";
		}

	}

	public static Date addMinutesToDate(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.MINUTE, 60);
		return calendar.getTime();
	}

	public static Date addDayToDate(Date date, Integer days) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		if (days != null) {
			calendar.add(Calendar.DATE, days);
		} else {
			calendar.add(Calendar.DATE, 0);
		}

		return calendar.getTime();
	}

	public static Map<String, Object> extractHoursAndMinutes(Date date) {
		try {
			HashMap<String, Object> map = new HashMap<>();
			if (date != null) {
				SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
				String time = timeFormat.format(date);
				String hours = time.split(":")[0];
				String minutes = time.split(":")[1];
				if (hours.length() == 1) {
					hours = "0" + hours;
				}
				if (minutes.length() == 1) {
					minutes = "0" + minutes;
				}
				map.put("hours", hours);
				map.put("minutes", minutes);
			} else {
				map.put("hours", "00");
				map.put("minutes", "00");
			}
			return map;
		} catch (NumberFormatException e) {
			logger.error("getTimeStringFromDate(NumberFormatException for Date:" + date + ")", e);
			throw new XamplifyDataAccessException(e.getMessage());
		} catch (Exception e) {
			logger.error("getTimeStringFromDate(Exception for Date:" + date + ")", e);
			throw new XamplifyDataAccessException(e.getMessage());
		}
	}

	public static boolean compareTwoTimes(String autoResponseTimeInString, String launchTimeInString) {
		try {
			LocalDate today = LocalDate.now();
			String launchTimeStringT = today + " " + launchTimeInString;
			String autoResponseTimeStringT = today + " " + autoResponseTimeInString;
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_24_FORMAT_SECONDS);
			LocalDateTime launchTime = LocalDateTime.parse(launchTimeStringT, formatter);
			LocalDateTime autoResponseTime = LocalDateTime.parse(autoResponseTimeStringT, formatter);
			Duration duration = Duration.between(autoResponseTime, launchTime);
			return duration.getSeconds() > 0;
		} catch (Exception e) {
			logger.error("compareTwoTimes(launchTimeString:" + launchTimeInString + ",autoResponseTimeString:"
					+ autoResponseTimeInString + ")", e);
			throw new XamplifyDataAccessException(e.getMessage());
		}
	}

	public static String convertSecondsToHHMMSS(int totalSecs) {
		int hours = totalSecs / 3600;
		int minutes = (totalSecs % 3600) / 60;
		int seconds = totalSecs % 60;
		return String.format("%02d:%02d:%02d", hours, minutes, seconds);

	}

	public static Date convertStringToDate(String dateString) {
		Date date = null;
		try {
			SimpleDateFormat launchTimeFormat = new SimpleDateFormat("yyyy-MM-dd");
			date = launchTimeFormat.parse(dateString);
		} catch (ParseException e) {
			logger.error("convertStringToDate(" + dateString + ")", e);
		}
		return date;

	}

	public static DateDTO convertStringToDateMDYFormat(String dateString) {
		DateDTO dateDTO = new DateDTO();
		try {
			if (StringUtils.hasText(dateString)) {
				SimpleDateFormat launchTimeFormat = new SimpleDateFormat(ONLY_DATE_FORMAT);
				dateDTO.setDate(launchTimeFormat.parse(dateString));
				dateDTO.setValidDate(true);
			} else {
				dateDTO.setValidDate(true);
			}

		} catch (ParseException e) {
			dateDTO.setValidDate(false);
		}
		return dateDTO;

	}

	public static String getUtcTimeInString(Date date, String timeZoneId) {
		String dateInString = convertDateToStringWithOutSec(date);
		return String.valueOf(convertToUTC(timeZoneId, dateInString));
	}

	public static boolean validTimeZone(String timezone) {
		if (StringUtils.hasText(timezone)) {
			return Arrays.asList(TimeZone.getAvailableIDs()).contains(timezone);
		} else {
			return false;
		}

	}

	public static String getUtcString(Date date) {
		if (date != null) {
			String timeZone = getServerTimeZone();
			return getUtcTimeInString(date, timeZone);
		} else {
			return "";
		}

	}

	public static Date convertStringToDateCampaignFilter(String dateInString) throws ParseException {
		SimpleDateFormat format = new SimpleDateFormat(DATE_TIME_24_FORMAT_SECONDS);
		return format.parse(dateInString);
	}

	public static Date convertStringToDate24Format(String string) {
		SimpleDateFormat format = new SimpleDateFormat(DATE_TIME_24_FORMAT_SECONDS);
		try {
			return format.parse(string);
		} catch (ParseException e) {
			throw new XamplifyDataAccessException(e);
		}
	}

	public static String getConvertedScheduleDateInString(Date date) {
		if (date != null) {
			SimpleDateFormat formater = new SimpleDateFormat(CAMPAIGN_DISPLAY_DATE_FORMAT);
			return formater.format(date);
		} else {
			return "";
		}

	}

	public static String getConvertedScheduleTimeInString(Date date) {
		if (date != null) {
			SimpleDateFormat formater = new SimpleDateFormat(CAMPAIGN_DISPLAY_TIME_FORMAT);
			return formater.format(date);
		} else {
			return "";
		}

	}

	public static String getMdfCreatedDateInString(Date date) {
		if (date != null) {
			SimpleDateFormat formater = new SimpleDateFormat(MDF_CREATED_DATE_FORMAT);
			return formater.format(date);
		} else {
			return "";
		}

	}

	public static final String getDateFormatForUploadedFiles() {
		return new SimpleDateFormat("ddMMyyyy").format(new Date()) + '/' + System.currentTimeMillis();
	}

	public static Date convertClientToServerTimeZone(String dateString, String clientTimeZone) {
		Date serverDate = null;
		if (StringUtils.hasText(dateString) && StringUtils.hasText(clientTimeZone)) {
			try {
				SimpleDateFormat clientFormat = new SimpleDateFormat(DATE_TIME_24_FORMAT);
				clientFormat.setTimeZone(TimeZone.getTimeZone(clientTimeZone));
				serverDate = clientFormat.parse(dateString);
			} catch (ParseException e) {
				logger.error("convertClientToServerTimeZone(" + dateString + "," + clientTimeZone + ")", e);
			}
		}
		return serverDate;
	}

	public static String changeTimeZone(Date dateObj, ZoneId fromTimeZone, ZoneId toTimeZone) {
		String convertedDateInString = null;
		if (dateObj != null && fromTimeZone != null && toTimeZone != null) {
			String dateInString = DateUtils.convertDateToStringWithOutSec(dateObj);
			DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_24_FORMAT);
			LocalDateTime localDateTime = LocalDateTime.parse(dateInString, dateTimeFormatter);
			ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, fromTimeZone);
			convertedDateInString = String.valueOf(zonedDateTime.withZoneSameInstant(toTimeZone));
		}
		return convertedDateInString;
	}

	public static ZonedDateTime convertCampaignDateAndTimeInUTC(String clientTimeZone, String dateInString) {
		ZonedDateTime zonedDateTime = null;
		try {
			ZoneId zoneId = ZoneId.of(clientTimeZone);
			DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(CAMPAIGN_DISPLAY_DATE_AND_TIME_FORMAT);
			LocalDateTime localDateTime = LocalDateTime.parse(dateInString, dateTimeFormatter);
			zonedDateTime = ZonedDateTime.of(localDateTime, zoneId);
			zonedDateTime = zonedDateTime.withZoneSameInstant(ZoneOffset.UTC);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return zonedDateTime;
	}

	public static Date addOneDayToDateAndTime(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DATE, 1);
		date = c.getTime();
		return date;
	}

	public static Date atEndOfDay(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 999);
		return calendar.getTime();
	}

	public static String getIndianStandardTime(String timeZone, Date launchTime) {
		String dateInString = DateUtils.convertDateToStringWithOutSec(launchTime);
		// Format for input date string
		DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(DATE_TIME_24_FORMAT);
		// Parse the input date and time using the formatter
		LocalDateTime localDateTime = LocalDateTime.parse(dateInString, inputFormatter);
		// Convert the local date and time to the input timezone
		ZonedDateTime inputZonedDateTime = localDateTime.atZone(ZoneId.of(timeZone));

		// Convert the input date and time to IST
		ZonedDateTime istZonedDateTime = inputZonedDateTime.withZoneSameInstant(ZoneId.of("Asia/Kolkata"));

		// Format the output date and time in a readable format
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_12_FORMAT_AMPM);
		return istZonedDateTime.format(formatter);
	}
	
	public static String getUtcDateString(Date date) {
		if (date != null) {
			ZonedDateTime serverDateTime = date.toInstant().atZone(ZoneId.systemDefault());
			ZonedDateTime utcDateTime = serverDateTime.withZoneSameInstant(ZoneId.of("UTC"));
			return utcDateTime.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
		} else {
			return "";
		}
	}
	public static String formatUtcIsoStringToReadable(String utcIsoString) {
	    if (utcIsoString == null || utcIsoString.isEmpty()) return "";
	    ZonedDateTime utcDateTime = ZonedDateTime.parse(utcIsoString);
	    ZonedDateTime istDateTime = utcDateTime.withZoneSameInstant(ZoneId.of("Asia/Kolkata"));
	    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy, h:mm a");
	    return istDateTime.format(formatter);
	}
}
