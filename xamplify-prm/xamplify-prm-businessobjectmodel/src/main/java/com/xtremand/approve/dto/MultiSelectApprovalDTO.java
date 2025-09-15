package com.xtremand.approve.dto;

import java.util.List;

import lombok.Data;

@Data
public class MultiSelectApprovalDTO {

	private String status;

	private String comment;
	
	private List<Integer> damIds;
	
	private List<Integer> trackIds;
	
	private List<Integer> playBooksIds;
	
	private Integer createdById;
	
	private Integer entityId;

	private List<Integer> whiteLabeledReApprovalDamIds;
	
	private Integer companyId;
	
	private Integer loggedInUserId;
}
