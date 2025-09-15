package com.xtremand.activity.dto;

import java.util.List;

import lombok.Data;

@Data
public class MeetingActivityDTO {
	
	private Integer id;
	
	private String name;
	
	private String startTime;
	
	private String endTime;
	
	private String createdTime;
	
	private String meetingUrl;
	
	private String status;
	
	private String description;
	
	private String updatedTime;
	
	private String type;
	
	private String uri;
	
	private List<MeetingGuest> guests;
	
	private String duration;
	
	private String startTimeString;
	
	private String endTimeString;
	
	private String organizerEmailId;
	
	private String recipientEmailId;
	
	private List<String> eventGuests;

}
