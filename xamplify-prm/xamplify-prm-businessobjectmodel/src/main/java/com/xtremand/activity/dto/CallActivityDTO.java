package com.xtremand.activity.dto;

import lombok.Data;

@Data
public class CallActivityDTO {
	
	private Long id;
	
	private String name;
	
	private String createdTime;
	
	private String status;
	
	private String direction;
	
	private String missedCallReason;
	
	private String duration;
	
	private String recordingURL;
	
	private String voiceMailURL;
	
	private String startTimeString;
	
	private String endTimeString;
	
	private String answeredAt;
	
	private boolean recordExpanded;
	
	private String callInfoString;
	
	private boolean voiceMailExpanded;
	
	private boolean deletedRecording;
	
	private String fromNumber;

    private String toNumber;

    private String conversation;

}
