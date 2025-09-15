package com.xtremand.util.dto;

import java.util.Date;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatedTimeConverter  {
	
	private Date createdTime;

	@Getter(value = AccessLevel.NONE)
	private String createdTimeInString;
	
	public String getCreatedTimeInString() {
		if(createdTime!=null) {
			return DateInString.getUtcString(createdTime);
		}else {
			return "";
		}
	}

	
}
