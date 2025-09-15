package com.xtremand.util.dto;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.springframework.util.StringUtils;

public class DateInString {

	private DateInString() {
	}

	private static final String DATE_TIME_24_FORMAT = "yyyy-MM-dd HH:mm";

	private static final String ONLY_DATE_FORMAT = "MM/dd/yyyy";

	private static final String CAMPAIGN_DISPLAY_TIME_FORMAT = "h:mm aa";

	private static final String CAMPAIGN_DISPLAY_DATE_FORMAT = "MMM dd, yyyy, ";

	private static final String DATE_AND_TIME_WITH_TIME_ZONE = "yyyy-MM-dd HH:mm:ssXXXXX";

	private static final String DATE_TIME_12_FORMAT_AMPM = "EEEE, MMMM dd, yyyy h:mm a";

	public static String getUtcString(Date date) {
		if (date != null) {
			String timeZone = getServerTimeZone();
			return getUtcTimeInString(date, timeZone);
		} else {
			return "";
		}

	}

	private static String getServerTimeZone() {
		return String.valueOf(ZoneId.systemDefault());
	}

	private static String getUtcTimeInString(Date date, String timeZoneId) {
		String dateInString = convertDateToStringWithOutSec(date);
		return String.valueOf(convertToUTC(timeZoneId, dateInString));
	}

	private static String convertDateToStringWithOutSec(Date date) {
		if (date != null) {
			SimpleDateFormat formater = new SimpleDateFormat(DATE_TIME_24_FORMAT);
			return formater.format(date);
		} else {
			return "";
		}
	}

	private static ZonedDateTime convertToUTC(String timeZoneId, String convertedTimeInString) {
		ZoneId zoneId = ZoneId.of(timeZoneId);
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_24_FORMAT);
		LocalDateTime localDateTime = LocalDateTime.parse(convertedTimeInString, dateTimeFormatter);
		ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, zoneId);
		return zonedDateTime.withZoneSameInstant(ZoneOffset.UTC);
	}

	public static String convertToOnlyDate(Date date) {
		if (date != null) {
			SimpleDateFormat formater = new SimpleDateFormat(ONLY_DATE_FORMAT);
			return formater.format(date);
		} else {
			return "";
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

	public static String getUTCStringByDateAndTimeWithTimeZone(String dateAndTimeWithTimeZoneInString) {
		String convertedTime = "-";
		try {
			if (dateAndTimeWithTimeZoneInString != null && StringUtils.hasText(dateAndTimeWithTimeZoneInString)) {
				String updatedDateTimeWithTimeZoneInString = dateAndTimeWithTimeZoneInString;

				if (dateAndTimeWithTimeZoneInString.endsWith("-08")) {
					updatedDateTimeWithTimeZoneInString += ":00";
				}

				DateTimeFormatter sdf = DateTimeFormatter.ofPattern(DATE_AND_TIME_WITH_TIME_ZONE);
				OffsetDateTime odtInstanceAtOffset = OffsetDateTime.parse(updatedDateTimeWithTimeZoneInString, sdf);

				OffsetDateTime odtInstanceAtUTC = odtInstanceAtOffset.withOffsetSameInstant(ZoneOffset.UTC);
				convertedTime = String.valueOf(odtInstanceAtUTC);
			}
		} catch (Exception e) {
			convertedTime = "-";
		}
		return convertedTime;

	}

	public static String getIndianStandardTime(String timeZone, Date launchTime) {
		String dateInString = convertDateToStringWithOutSec(launchTime);
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

}
