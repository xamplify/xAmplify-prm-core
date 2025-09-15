package com.xtremand.campaign.dto;

import com.opencsv.bean.CsvBindByPosition;

import lombok.Data;

@Data
public class ClickedUrlAnalyticsDTO  {
	
	@CsvBindByPosition(position = 0)
	private String url;
	@CsvBindByPosition(position = 1)
	private Integer clickedCount;

}
