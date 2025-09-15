package com.xtremand.util.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class LoginAsEmailNotificationDTO implements Serializable {/**
	 * 
	 */
	private static final long serialVersionUID = -7339063904888907894L;
	
	private Integer partnerCompanyUserId;
	
	private Integer vendorCompanyUserId;
	
	private String domainName;
	
	private boolean superAdminLoggedIn;

}
