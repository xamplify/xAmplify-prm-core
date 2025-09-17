package com.xtremand.team.member.dto;

import java.io.Serializable;

import com.xtremand.user.bom.TeamMemberStatus;

import lombok.Data;

@Data
public class ManageTeamMemberDTO implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1698379611113394005L;

	private Integer id;
	
	private Integer teamMemberId;
	
	private String firstName;
	
	private String lastName;
	
	private String emailId;
	
	private boolean secondOrgAdmin;
	
	private boolean all;
	
	private boolean video;
	
	private boolean campaign;
	
	private boolean stats;
	
	private boolean design;
	
	private boolean emailTemplate;
	
	private boolean form;
	
	private boolean landingPage;
	
	private boolean socialShare;
	
	private boolean partners;
	
	private boolean contact;
	
	private boolean mdf;
	
	private boolean dam;
	
	private TeamMemberStatus status;
	
	private boolean enabled;
	
	private boolean loginAsButtonEnabled;
	
	private String loginAsButtonToolTipMessage;
	
	
	

}
