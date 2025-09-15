package com.xtremand.util.bom;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.springframework.stereotype.Component;

@Component
public class DateUtils {
	
	public String getUTCString(Date date) {
		String dateInUTC = null;
		String timeZone = String.valueOf(ZoneOffset.systemDefault().getRules().getOffset(Instant.now()));
		SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String dateInString = formater.format(date);
		ZoneId zoneId = ZoneId.of(timeZone);
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		LocalDateTime localDateTime = LocalDateTime.parse(dateInString, dateTimeFormatter);
		ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, zoneId);
		dateInUTC = String.valueOf(zonedDateTime.withZoneSameInstant(ZoneOffset.UTC));
		return dateInUTC;
	}

}
