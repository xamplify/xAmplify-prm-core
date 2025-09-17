package com.xtremand.formbeans;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.xtremand.salesforce.dto.SfCustomFieldsDataDTO;

import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class LeadDTO  {

	private Integer campaignId;
	
	private Integer dealId;
	
	private String dealTitle;
	
	private String campaignName;
	
	private Date createdTime;
	private Date dealCreatedTime;
	
	private Integer leadId;
	
	private String emailId;
	
	private String firstName;
	
	private String lastName;
	
	private String dealStatus;
	private boolean isDeal;
	private Integer createdBy;
	private UserDTO partner;
	
	private Integer commentCount;
	private Integer newCommentCount;
	
	private Integer propCommentCount;
	
	private String leadCreatorFirstName;
	
	private String leadCreatorLastName;
	
	private String leadCreatorEmailId;

	private Integer updatedBy;
	
	private List<SfCustomFieldsDataDTO> sfCfDataDtos;
	
	
	
}
