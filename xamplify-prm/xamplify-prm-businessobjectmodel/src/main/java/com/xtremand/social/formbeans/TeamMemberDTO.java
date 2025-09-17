package com.xtremand.social.formbeans;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.xtremand.user.bom.TeamMemberStatus;

import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class TeamMemberDTO implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6216855893478500171L;

	private Integer id;
	
	private String emailId;
	
	private String firstName;
	
	private String lastName;
	
	private boolean campaign;
	
	private boolean contact;
	
	private boolean video;
	
	private boolean stats;
	
	private boolean socialShare;
	
	private boolean emailTemplate;
	
	private boolean form;
	
	private boolean landingPage;
	
	private boolean design;
	
	private boolean all;
	
	private boolean user;
	
	private boolean orgAdmin;
	
	private boolean superAdmin;
	
	private boolean partners;
	
	private TeamMemberStatus status;
	
	private boolean enabled;
	
	private Integer orgAdminId;
	
	private boolean opportunity;
	
	private boolean ssoEnabled;
	
	private Integer loggedInUserId;
  
	private Integer teamMemberId;
	
	private boolean allSelected;
	
	private boolean secondOrgAdmin;
	
	private boolean vanityUrlFilter;
	
	private String vanityUrlDomainName;
	
	private boolean partnershipEstablishedWithPrm;
	
	private String fullName;

	public String viewType;

}
