package com.xtremand.lead.dto;

import java.io.Serializable;

import com.xtremand.integration.bom.Integration.IntegrationType;
import com.xtremand.lead.bom.PipelineType;

import lombok.Data;

@Data
public class PipelineRequestDTO implements Serializable {/**
	 * 
	 */
	private static final long serialVersionUID = -7462209753107408543L;
	
	private Integer vendorCompanyId = 0;
	
	private Integer loggedInUserId = 0;
	
	private Integer campaignId = 0;
	
	private Long ticketTypeId = 0L;
	
	private IntegrationType integrationType;
	
	private PipelineType pipelineType;

}
