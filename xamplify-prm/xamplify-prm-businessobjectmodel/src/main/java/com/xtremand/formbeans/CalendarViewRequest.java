package com.xtremand.formbeans;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CalendarViewRequest {
	private Integer userId;
	private Date startTime;
	private Date endTime;
	private boolean teamMemberAnalytics;
	private Integer teamMemberId;
	private Integer categoryId;
	private boolean vanityUrlFilter;
	private String vendorCompanyProfileName;
	private boolean vanityUrlFilterApplicable;
	private Integer vendorCompanyId;
	private boolean archived = false;
}
