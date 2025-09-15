package com.xtremand.integration.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class IntegrationSettingsDTO implements Serializable{
		
	/**
	 * 
	 */
	private static final long serialVersionUID = -390909808506685425L;

	private String vendorCompanyId;
	
	private boolean dealByPartner = true;
	
	private boolean dealByVendor;
	
	private boolean dealBySelfLead;
	
	private boolean showDealPipelineStage;
	
	private boolean partnerEditLead;
	
	private boolean partnerDeleteLead;

}
