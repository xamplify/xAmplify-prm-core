package com.xtremand.util.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class UpgradeRoleEmailNotification implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8601495044966022737L;
	
	private String firstName;
	
	private String upgradedFrom;
	
	private String upgradedTo;
	
	private String upgradedCompanyName;
	
	private String emailId;
	
	private Date upgradedOn;
	
	private List<String> upgradedInformations = new ArrayList<>();

}
