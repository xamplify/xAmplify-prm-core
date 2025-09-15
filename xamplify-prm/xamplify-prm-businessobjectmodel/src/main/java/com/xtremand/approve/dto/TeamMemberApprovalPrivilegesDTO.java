package com.xtremand.approve.dto;

import lombok.Data;

@Data
public class TeamMemberApprovalPrivilegesDTO {

	private Integer id;
	
	private String role;
	
	private String firstName;
	
	private String emailId;
	
	private boolean assetApprover;
	
	private boolean trackApprover;
	
	private boolean playbookApprover;
	
	private boolean hasDamRole;
	
	private boolean hasTrackRole;
	
	private boolean hasPlaybookRole;
	
	private boolean anyApprover;
	
	private boolean anyAdminOrSupervisor;
	
	
	private boolean assetApproverFieldUpdated;
	
	private boolean trackApproverFieldUpdated;
	
	private boolean playbookApproverFieldUpdated;
}
