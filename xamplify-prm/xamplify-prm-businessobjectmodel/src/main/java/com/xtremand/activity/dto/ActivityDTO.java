package com.xtremand.activity.dto;

import lombok.Data;

@Data
public class ActivityDTO {

	private Integer id;
	
	private String type;
	
	private String name;
	
	private String stage;
	
	private String schedule;
	
	private String createdTime;
	
	private String updatedTime;
	
	private String closeDate;
	
	private String activity;
	
	private String dueDateString;
	
	private boolean editEnabled;
	
	private boolean deleteEnabled;
	
	private String addedForEmailId;
}
