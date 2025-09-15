package com.xtremand.util.dto;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
public class PartnerCompanyDTO extends CreatedTimeConverter implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Integer partnershipId;
	
	private Integer partnerCompanyId;
	
	private Integer vendorCompanyId;
	
	private String companyName;
	
	private String companyLogo = "assets/images/company-profile-logo.png";
	
	@JsonIgnore
	private Integer partnerId;
	
	private String companyNameAddedByVendor;
	
	@JsonIgnore
	private Integer companyId;
	
	private String partnerName;
	
	private String partnerEmail;
	
	private String partnerJobTitle;
	
	private String partnerMobileNumber;
	
	private Date partnerAddedDate;
	
	private Date lastLogInDate;
	
	private String partnerAddedDateStr;

	private String partnerStatus;
	
	public void setPartnerAddedDate(Date partnerAddedDate) {
		this.partnerAddedDate = partnerAddedDate;
		if (partnerAddedDate != null) {
			String timeZone = String.valueOf(ZoneOffset.systemDefault().getRules().getOffset(Instant.now()));
			SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			String dateInString = formater.format(partnerAddedDate);
			ZoneId zoneId = ZoneId.of(timeZone);
			DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
			LocalDateTime localDateTime = LocalDateTime.parse(dateInString, dateTimeFormatter);
			ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, zoneId);
			setPartnerAddedDateStr(String.valueOf(zonedDateTime.withZoneSameInstant(ZoneOffset.UTC)));
		}
	}

}
