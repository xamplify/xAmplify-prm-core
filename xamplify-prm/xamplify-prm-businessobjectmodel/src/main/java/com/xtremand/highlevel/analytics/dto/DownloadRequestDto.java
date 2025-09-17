package com.xtremand.highlevel.analytics.dto;

import java.io.Serializable;
import java.util.Date;

import com.xtremand.util.dto.UserDetailsUtilDTO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class DownloadRequestDto extends UserDetailsUtilDTO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Integer id;
	
	private Integer requestedBy;
	
	private String downloadStatus;
	
	private String moduleStatus;
	
	private String downloadPath;
	
	private Date requestedOn;
	
	private Date updatedOn;

	
}
