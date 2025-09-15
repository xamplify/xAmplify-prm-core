package com.xtremand.mdf.dto;

import java.io.Serializable;
import java.util.Date;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Data
@EqualsAndHashCode(callSuper=false)

public class TimeLineUserMappedDTO extends MdfUserMappedDTO implements Serializable {/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@JsonIgnore
	private Date createdTime;
	
	@JsonIgnore
	private String createdTimeInString;
	
	private String createdTimeInUTCString;
	
	@JsonIgnore
	private String creatorFullName;
	
	@JsonIgnore
	private String creatorEmailId;
	
	@JsonIgnore
	@Getter(value = AccessLevel.NONE)
	private String creatorDisplayName;
	
	@JsonIgnore
	private String creatorProfilePicturePath;
	
	@JsonIgnore
	private String updaterFullName;
	
	@JsonIgnore
	private String updaterEmailId;
	
	@JsonIgnore
	@Getter(value = AccessLevel.NONE)
	private String updaterDisplayName;
	
	@JsonIgnore
	private String updaterProfilePicturePath;
	
	@JsonIgnore
	private Date updatedTime;
	
	
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	
	public String getCreatorDisplayName() {
		if(StringUtils.hasText(creatorFullName)) {
			return creatorFullName.trim();
		}else {
			return creatorEmailId;
		}
	}


	

	public String getUpdaterDisplayName() {
		if(StringUtils.hasText(updaterFullName)) {
			return updaterFullName.trim();
		}else {
			return updaterEmailId;
		}
	}
	
	

}
