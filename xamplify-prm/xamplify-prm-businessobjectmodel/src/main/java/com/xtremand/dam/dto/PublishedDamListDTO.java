package com.xtremand.dam.dto;

import java.util.Date;

import com.xtremand.util.dto.DateInString;
import com.xtremand.util.dto.DisplayUserDetailsDTO;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Data
@EqualsAndHashCode(callSuper = false)
public class PublishedDamListDTO extends DisplayUserDetailsDTO {/**
	 * 
	 */
	private static final long serialVersionUID = -7242733230571258142L;
	
	
	private Integer id;
	
	private boolean publishedToPartnerGroup;
	
	private String assetName;
	
	private String assetType;
	
	private boolean beeTemplate;
	
	private String alias;
	
	private String thumbnailPath;
	
	@Getter(value = AccessLevel.NONE)
	private String publishedTimeInUTCString;
	
	private Date publishedTime;
	
	private String vendorCompanyName;
	
	private String status;

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getPublishedTimeInUTCString() {
		if(publishedTime!=null) {
			return DateInString.getUtcString(publishedTime);
		}else {
			return "";
		}
	}
	
	
	

}
