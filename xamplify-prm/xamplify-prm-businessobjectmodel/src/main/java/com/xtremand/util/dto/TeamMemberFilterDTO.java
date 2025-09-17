package com.xtremand.util.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class TeamMemberFilterDTO implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2212342152803895966L;

	private boolean applyTeamMemberFilter;
	
	private boolean emptyFilter;
	
	private List<Integer> partnershipIdsOrPartnerCompanyIds;
	
	private List<Integer> vendorIdsOrVendorCompanyIds;

}
