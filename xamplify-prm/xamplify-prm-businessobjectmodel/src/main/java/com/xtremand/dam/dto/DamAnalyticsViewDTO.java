package com.xtremand.dam.dto;

import java.io.Serializable;
import java.util.Date;

import com.xtremand.dam.bom.DamAnalyticsActionEnum;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class DamAnalyticsViewDTO implements Serializable {/**
	 * 
	 */
	private static final long serialVersionUID = -463171579095805418L;
	
	
	@Getter(value = AccessLevel.NONE)
	private String actionType;
	
	private Date actionTime;
	
	private String actionTimeInUTCString;
	
	private String deviceType;
	
	private String os;
	
	private String city;
	
	private String state;
	
	private String country;
	
	private String countryCode;

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getActionType() {
		if(actionType.equals(DamAnalyticsActionEnum.VIEW.name())) {
			return "View";
		}else if(actionType.equals(DamAnalyticsActionEnum.DOWNLOAD.name())) {
			return "Download";
		}else {
			return actionType;
		}
	}

	

}
