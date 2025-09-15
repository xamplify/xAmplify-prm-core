package com.xtremand.dashboard.analytics.views.dto;

import lombok.Data;

@Data
public class DashboardModuleAnalyticsViewDTO {
	
	private Integer moduleId;
	
	private String moduleName;
	
	private Integer count;
	
	private boolean hasAccess;
	
	private String faIcon;
	
	private String color;
	
	private String description;
	
	

}
