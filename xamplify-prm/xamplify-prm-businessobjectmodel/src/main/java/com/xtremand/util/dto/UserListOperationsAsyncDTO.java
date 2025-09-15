package com.xtremand.util.dto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.xtremand.formbeans.UserDTO;
import com.xtremand.team.member.dto.TeamMemberDTO;
import com.xtremand.user.bom.User;

import lombok.Data;

@Data
public class UserListOperationsAsyncDTO {
	private Set<UserDTO> partners = new HashSet<>();
	private Set<Integer> partnerListIds = new HashSet<>();
	private boolean partnerList;
	private Integer statusCode;
	private Integer vendorAdminId;
	private User loggedInUser;
	private boolean defaultPartnerList;
	private Integer userListId;
	private Integer defaultPartnerListId;
	private boolean ssoPartnerCreatedSuccessfully;
	private List<TeamMemberDTO> teamMembers;
	private boolean ssoTeamMemberCreatedSuccessfully;
	private boolean constraint;
	private boolean partnerSignedUpUsingSSO;
 	private String exceptionMessage;
	private String emailId;
	private Set<UserDTO> allPartners = new HashSet<>();
	private Set<Integer> allPartnerListIds = new HashSet<>();
	private boolean editPartner;
	private boolean copyPartnersToGroup;
	private UserDTO partner;
	private Integer vendorCompanyId;
	private List<String> companyNames = new ArrayList<>();
}
