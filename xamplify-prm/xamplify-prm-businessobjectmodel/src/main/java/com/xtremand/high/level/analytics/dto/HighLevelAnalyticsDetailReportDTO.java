package com.xtremand.high.level.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;


@AllArgsConstructor
@RequiredArgsConstructor
@Data
public class HighLevelAnalyticsDetailReportDTO {
	
    private Integer moduleId;
	
	private String moduleName;
	
	private String count;
	
	private boolean hasAccess;
	
	private String faIcon;
	
	private String color;
	
    private String description;

}
