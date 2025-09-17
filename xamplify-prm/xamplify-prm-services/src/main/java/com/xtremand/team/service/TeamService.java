package com.xtremand.team.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.xtremand.common.bom.Pagination;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.formbeans.VendorInvitationDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.signup.dto.SignUpRequestDTO;
import com.xtremand.team.member.dto.DeleteTeamMemberPartnersRequestDTO;
import com.xtremand.team.member.dto.PartnerPrimaryAdminUpdateDto;
import com.xtremand.team.member.dto.TeamMemberDTO;
import com.xtremand.user.bom.TeamMember;
import com.xtremand.user.bom.User;
import com.xtremand.user.bom.UserList;
import com.xtremand.util.dto.Pageable;

public interface TeamService {

	XtremandResponse saveAll(TeamMemberDTO teamMemberDTO);

	XtremandResponse findAll(Pagination pagination);

	XtremandResponse update(TeamMemberDTO teamMemberDTO, XtremandResponse response);

	List<UserDTO> findUsersToTransferData(Integer userId);

	XtremandResponse delete(TeamMemberDTO teamMemberDTO);

	void sendEmailsToTeamMembers(Map<String, Object> map);

	XtremandResponse resendTeamMemberInvitation(TeamMemberDTO teamMemberDTO);

	XtremandResponse getVanityUrlRoles(TeamMemberDTO teamMemberInputDTO);

	void changeTeamMemberStatus(Integer teamMemberId);

	XtremandResponse findById(Integer id);

	/******** XNFR-85 ******/
	XtremandResponse findTeamMemberDetailsByTeamMemberGroupId(Pagination pagination);

	/******** XNFR-97 ******/
	XtremandResponse findPartners(Pagination pagination);

	XtremandResponse deleteTeamMemberPartners(DeleteTeamMemberPartnersRequestDTO deleteTeamMemberPartnersRequestDTO);

	/******** XNFR-139 ******/
	XtremandResponse findMaximumAdminsLimitDetails(Integer loggedInUserId);

	XtremandResponse updatePrimaryAdmin(Integer loggedInUserId, Integer teamMemberUserId, XtremandResponse response,
			TeamMemberDTO teamMemberDTO);

	XtremandResponse findMaximumAdminsLimitDetailsByCompanyId(Integer companyId);

	XtremandResponse findPrimaryAdminAndExtraAdmins(Integer loggedInUserId);

	/**** XNFR-454 *****/
	XtremandResponse addTeamMemberUsingSignUpLink(SignUpRequestDTO signUpRequestDTO);

	XtremandResponse findPrimaryAdmin(Integer loggedInUserId);

	void setNewUsersAndTeamMembersData(User representingPartner, List<User> newUsers, TeamMemberDTO teamMemberDTO,
			List<Integer> roleIds, Integer teamMemberGroupId, UserList teamMemberUserList);

	void iterateDtosAndAddTeamMembers(List<TeamMemberDTO> teamMemberDTOs, User primaryAdminUser,
			List<TeamMember> teamMembers, List<User> newUsers);

	XtremandResponse saveInviteTeamMembersData(Integer userId, VendorInvitationDTO vendorInvitationDTO);

	Map<String, Object> inviteTeamMembersCount(Integer userId);

	Map<String, Object> inviteTeamMemberAnalytics(Integer userId, String type, Pageable pageable);

	XtremandResponse getInviteTeamMemberTemplate(Integer userId, String companyProfileName, Integer templateId);

	void inviteTeamMemberDownloadCsv(Integer userId, String type, Pageable pageable, HttpServletResponse response);

	XtremandResponse sendTeamMemberReminder(Pagination pagination, Integer loggedInUserId);

	XtremandResponse updatePartnerCompanyPrimaryAdmin(PartnerPrimaryAdminUpdateDto partnerPrimaryAdminUpdateDto);

	/** XNFR-564 ***/
	void addSourceCompanyMembersToDestination(Integer sourceCompanyId, Integer destinationCompanyId);

	/** XNFR-1046 ***/
	void deletePartnerTeamGroupMapping(List<Integer> partnerTeamGroupMappingIds);
}
