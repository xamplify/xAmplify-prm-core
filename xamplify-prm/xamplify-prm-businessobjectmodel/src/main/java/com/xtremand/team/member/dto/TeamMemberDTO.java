package com.xtremand.team.member.dto;

import java.util.List;

import org.springframework.util.StringUtils;

import com.xtremand.user.bom.TeamMemberStatus;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class TeamMemberDTO {

	private Integer id;

	private String firstName;

	private String lastName;

	private String companyName;

	private Integer companyId;

	@Getter(value = AccessLevel.NONE)
	private String emailId;

	public String getEmailId() {
		if (StringUtils.hasText(emailId)) {
			return emailId.toLowerCase().trim();
		} else {
			return emailId;
		}

	}

	private Integer teamMemberGroupId;

	private Integer userId;

	private boolean ssoEnabled;

	private TeamMemberStatus status;

	private Integer orgAdminId;

	private boolean secondAdmin;

	private boolean vanityUrlFilter;

	private String vendorCompanyProfileName;

	private List<TeamMemberDTO> teamMemberDTOs;

	// XNFR-316
	private String fullName;

	private String mobileNumber;

	private String trackCount;

	// XNFR-429
	private String userStatus;

	private boolean addedThroughSignUpLink;

	private String password;
	
	private String vendorTeamMemberEmailId;
	
	private boolean addedThroughOAuthSSO;

	private boolean addedThroughInvitation;

	private String subjectLine;
	
	/** XNFR-821 **/
	private boolean approvalManager;
	
	private List<Integer> whiteLabeledReApprovalDamIds;
	
	private boolean newPartnerGroupRequired;
	
	private List<Integer> selectedPartnershipIds;
	
	private List<Integer> deletedPartnershipIds;
	
	private boolean newPartnersAdded;
	
	private boolean newTeamMember;
	
	private boolean newAndSinglePartner;
	
}
