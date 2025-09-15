package com.xtremand.util.bom;

import java.io.Serializable;

import org.springframework.util.StringUtils;

import com.xtremand.util.dto.DateInString;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class ActiveQueryInfo implements Serializable {

	private static final String MANAGE_CAMPAIGNS_QUERY_STRING_PREFIX = "with a as( select distinct campaign_id";

	private static final String TOTAL_AND_ACTIVE_QUERY_STRING_PREFIX = "select count(distinct xc.campaign_id) as";

	/**
	 * 
	 */
	private static final long serialVersionUID = 3159484857922010561L;

	private String dataBaseName;

	private Integer processId;

	private String ipAddress;

	private String queryStartedOn;

	@Getter(value = AccessLevel.NONE)
	private String queryStartedOnInUTCString;

	private String query;

	private String queryAccessedFrom;

	@Getter(value = AccessLevel.NONE)
	private String queryUsedIn;

	private String status;

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getQueryUsedIn() {
		boolean validQuery = query != null && StringUtils.hasText(query);
		if (validQuery && query.contains(MANAGE_CAMPAIGNS_QUERY_STRING_PREFIX)) {
			queryUsedIn = "Manage Campaigns";
		} else if (validQuery && query.contains(TOTAL_AND_ACTIVE_QUERY_STRING_PREFIX)) {
			queryUsedIn = "All Contacts / Export To Excel In All Contacts";
		} else {
			queryUsedIn = "Other";
		}
		return queryUsedIn;
	}

	public String getQueryStartedOnInUTCString() {
		return DateInString.getUTCStringByDateAndTimeWithTimeZone(queryStartedOn);
	}

}
