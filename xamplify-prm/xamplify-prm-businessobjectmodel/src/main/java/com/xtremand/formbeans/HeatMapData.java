package com.xtremand.formbeans;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HeatMapData {

	private Integer campaignId;
	private String name;
	private Integer value;
	private Integer colorValue;
	private Integer totalUsers;
	private String launchTime;
	private Double interactionPercentage;
	private String campaignType;
	private String campaignTitle;
}
