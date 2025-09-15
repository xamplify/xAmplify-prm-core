package com.xtremand.dam.dto;

import java.io.Serializable;
import java.util.List;

import com.xtremand.util.dto.CreatedTimeConverter;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class ApprovalStatusHistoryDTO extends CreatedTimeConverter implements Serializable{
	
	private static final long serialVersionUID = 9218316822680009088L;

	private Integer id;
		
	private String status;
	
	private String comment;
	
	private Integer createdBy;
	
	private String companyLogoPath;
	
	private String createdByName;
	
	private String emailId;
	
	private String userLogoPathOrFirstLetter;
	
	private Integer loggedInUserId;
	
	private Integer commentedBy;
	
	private boolean invalidComment;
	
	private String moduleType;
	
	private String statusInString;
	
	private Integer companyId;
	
	private boolean statusUpdated;
	
	private String name;
	
	private String statusUpdatedByName;
		
	private Integer entityId;
	
	private Integer videoId;
	
	/** XNFR-885 **/
	private boolean sendForReApproval;
	
	private Integer approvalReferenceId;
	
	private List<Integer> whiteLabeledParentDamIds;
		
}
