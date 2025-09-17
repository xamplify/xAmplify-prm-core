package com.xtremand.formbeans;

import lombok.Data;

@Data
public class EventCheckInDTO {
	
	private Integer id;
	
	private boolean checkedIn;
	
	private String checkInTimeString;

}
