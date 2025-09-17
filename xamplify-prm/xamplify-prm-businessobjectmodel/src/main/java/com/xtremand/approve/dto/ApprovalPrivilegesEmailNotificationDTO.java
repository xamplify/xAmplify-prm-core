package com.xtremand.approve.dto;

import lombok.Data;

@Data
public class ApprovalPrivilegesEmailNotificationDTO {
	
	private Integer id;
	
	private String firstName;
	
	private String emailId;
	
	private boolean assetApprover;
	
	private boolean trackApprover;
	
	private boolean playbookApprover;
	
	private boolean assetApproverFieldUpdated;
	
	private boolean trackApproverFieldUpdated;
	
	private boolean playbookApproverFieldUpdated;
	
	private Integer privilegesUpdatedBy;
}
