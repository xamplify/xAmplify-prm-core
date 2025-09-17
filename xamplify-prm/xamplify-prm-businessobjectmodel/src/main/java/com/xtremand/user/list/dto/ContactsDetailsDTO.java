package com.xtremand.user.list.dto;

import java.util.Date;

import com.xtremand.util.dto.DateInString;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class ContactsDetailsDTO {
	
	private Integer userListId;

	private String userListName;
	
	private String contactName;

	private Date createdTime;
	
	private String contacts;
	
	private String emailId;
	
	private String company;
	
	private String fullName;


	@Getter(value = AccessLevel.NONE)
	private String createdTimeInUTCString;


	public String getCreatedTimeInUTCString() {
		if (createdTime != null) {
			return DateInString.getUtcString(createdTime);
		} else {
			return "";
		}
	}

}
