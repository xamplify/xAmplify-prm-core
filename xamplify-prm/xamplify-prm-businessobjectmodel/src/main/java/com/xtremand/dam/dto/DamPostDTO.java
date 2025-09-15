package com.xtremand.dam.dto;

import java.io.Serializable;
import java.util.Set;

import lombok.Data;

@Data
public class DamPostDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4870371444102227606L;

	private Integer id;

	private String name;

	private String description;

	private Integer categoryId;

	private boolean published;

	private String htmlBody;

	private String jsonBody;

	private Integer createdBy;

	private Set<Integer> tagIds;

	private boolean saveAs;

	/***** XNFR-255 ****/
	private boolean shareAsWhiteLabeledAsset;

	private Integer damId;

	private Integer companyId;

	private Set<Integer> partnerGroupIds;

	private boolean partnerGroupSelected;

	private Set<Integer> partnerIds;

	private String pageSize;

	private String pageOrientation;

	private Integer loggedInUserId;

	private boolean addedToQuickLinks;
	
	private boolean draft;
	
	private boolean sendForReApproval;
	
	private Integer approvalReferenceId;
	
}
