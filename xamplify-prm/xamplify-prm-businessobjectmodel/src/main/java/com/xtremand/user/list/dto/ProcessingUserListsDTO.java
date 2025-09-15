package com.xtremand.user.list.dto;

import java.io.Serializable;
import java.util.Date;

import com.xtremand.util.dto.DateInString;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class ProcessingUserListsDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Integer userListId;

	private String userListName;

	private String companyName;

	private Integer usersCount;

	private Integer companyId;

	private Integer createdUserId;

	private boolean uploadInProgress;

	private boolean zeroBounceEmailValidationInProgress;

	private boolean userListProcessed;

	private String csvPath;

	private Date createdTime;

	private boolean processingTimeLimitReached;

	private Integer totalCount;
	
	private Integer previouslyUploadedCount;

	@Getter(value = AccessLevel.NONE)
	private String createdTimeInUTCString;

	private Date updatedTime;
	@Getter(value = AccessLevel.NONE)
	private String updatedTimeInUTCString;

	private String uploadedPercentage;

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getCreatedTimeInUTCString() {
		if (createdTime != null) {
			return DateInString.getUtcString(createdTime);
		} else {
			return "";
		}
	}

	public String getUpdatedTimeInUTCString() {
		if (updatedTime != null) {
			return DateInString.getUtcString(updatedTime);
		} else {
			return "";
		}
	}

}
