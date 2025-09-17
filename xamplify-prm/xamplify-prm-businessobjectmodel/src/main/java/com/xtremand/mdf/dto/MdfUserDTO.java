package com.xtremand.mdf.dto;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class MdfUserDTO extends MdfUserMappedDTO implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3394832524489565690L;
	
	private Integer userId;
	
	private String companyName;
	
	private String phoneNumber;
	
	private String website;
	
	private String title;

	private String partnerStatus;

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
