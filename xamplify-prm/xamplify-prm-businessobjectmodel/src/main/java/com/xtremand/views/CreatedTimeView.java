package com.xtremand.views;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import com.xtremand.util.dto.DateInString;

import lombok.AccessLevel;
import lombok.Getter;

@MappedSuperclass
public class CreatedTimeView {
	
	@Column(name="created_time")
	private Date createdTime;

	@Getter(value = AccessLevel.NONE)
	@Transient
	private String createdTimeInString;
	
	public String getCreatedTimeInString() {
		if(createdTime!=null) {
			return DateInString.getUtcString(createdTime);
		}else {
			return "";
		}
	}

}
