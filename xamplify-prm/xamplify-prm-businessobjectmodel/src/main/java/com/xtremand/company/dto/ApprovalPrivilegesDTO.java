package com.xtremand.company.dto;

import lombok.Data;

@Data
public class ApprovalPrivilegesDTO {
	
	private Integer id;
	
	private Integer loggedInUserId;
	
	private Integer companyId;
	
	private boolean approvalRequiredForAssets;
	
	private boolean approvalRequiredForTracks;
	
	private boolean approvalRequiredForPlaybooks;
	
	private boolean assetApprovalEnabledForCompany;
	
	private boolean tracksApprovalEnabledForCompany;
	
	private boolean playbooksApprovalEnabledForCompany;	

}
