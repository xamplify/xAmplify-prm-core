package com.xtremand.util.dto;

import lombok.Data;

@Data
public class AccessDetailsDTO {
	
	
	private boolean allAccess;
	
	private boolean videoAccess;
	
	private boolean statsAccess;
	
	private boolean formAccess;
	
	private boolean partnerAccess;
	
	private boolean partnerAnalyticsAccess;
	
	private boolean teamMemberAccess;
	
	private boolean enableLeads;
	
	private boolean mdf;
	
	
 /*********Roles*******************/	
	private boolean partner;
	
	private boolean onlyPartner;
	
	private boolean partnerTeamMember;
	
	private boolean prm;
	
	private boolean prmTeamMember;
	
	private boolean prmAndPartner;
	
	private boolean prmAndPartnerTeamMember;
	
	private boolean onlyUser;
	

	
	
	
	

}
