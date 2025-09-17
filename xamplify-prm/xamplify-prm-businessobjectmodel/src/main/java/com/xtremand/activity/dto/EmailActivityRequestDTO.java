package com.xtremand.activity.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class EmailActivityRequestDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2806673219865950256L;
	
	private Integer loggedInUserId;
	
	private Integer companyId;
	
	private Integer userId;
	
	private String subject;
	
	private String body;
	
	private Integer recipient;
	
	private Integer sender;
	
	private Date createdTime;
	
	private String toEmailId;
	
	private String fromEmailId;
	
	private String firstName;
	
	private String lastName;
	
	private Integer emailActivityId;
	
	private List<String> ccEmailIds;
	
	private List<String> bccEmailIds;
	
	private List<String> toEmailIds;
	
	private String receiverName;
	
	private String middleName;
	
	private Boolean isCompanyJourney;
	
	private List<Integer> ids;
	
	private List<Integer> userIds;
	
	private Integer templateId;
	
}
