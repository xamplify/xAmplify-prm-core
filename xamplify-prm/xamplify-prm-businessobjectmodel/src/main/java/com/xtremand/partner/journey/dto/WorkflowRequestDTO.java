package com.xtremand.partner.journey.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class WorkflowRequestDTO extends WorkflowUtilDTO {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8366328141374143831L;
	private Integer loggedInUserId;
	
	private Integer playbookId;

}
