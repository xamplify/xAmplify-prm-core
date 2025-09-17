package com.xtremand.dashboard.analytics.views.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class DealStatisticsDTO implements Serializable {
	
    private String name;
	
	private Integer value;
}
