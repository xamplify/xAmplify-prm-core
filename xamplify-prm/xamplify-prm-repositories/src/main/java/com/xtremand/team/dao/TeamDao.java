package com.xtremand.team.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.xtremand.common.bom.Pagination;
import com.xtremand.formbeans.EmailTemplateDTO;
import com.xtremand.formbeans.RoleDTO;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.team.member.dto.TeamMemberAndPartnerIdsAndUserListIdDTO;
import com.xtremand.team.member.dto.TeamMemberDTO;
import com.xtremand.team.member.dto.TeamMemberListDTO;
import com.xtremand.user.bom.TeamMember;
import com.xtremand.user.bom.User;
import com.xtremand.user.bom.UserList;
import com.xtremand.user.bom.UserUserList;
import com.xtremand.util.dto.AdminAndTeamMemberDetailsDTO;
import com.xtremand.util.dto.CompanyAndRolesDTO;
import com.xtremand.util.dto.UserDetailsUtilDTO;
import com.xtremand.util.dto.ViewTypePatchRequestDTO;
import com.xtremand.vanity.url.dto.AnalyticsCountDTO;

public interface TeamDao {

	void saveAll(List<TeamMember> teamMembers, Integer companyId);

	List<String> listTeamMemberEmailIds();

	void saveUsersAndTeamMembers(List<User> newUsers, Integer companyId);

	Map<String, Object> findAll(Pagination pagination);

	void deleteUnMappedRoleIds(List<Integer> userIds, Set<Integer> roleIds);

	void addNewRoles(Integer teamMemberId, Set<Integer> roleIds);

	void addNewRole(Integer teamMemberId, Integer roleId);

	void delete(TeamMemberDTO teamMemberDTO);

	void deleteUnMappedRoleIdsByTeamMemberIds(List<Integer> teamMemberIds, Set<Integer> roleIds);

	boolean roleExists(Integer roleId, Integer userId);

	public boolean isTeamMember(Integer teamMemberId);

	public List<TeamMember> findAllByCompanyId(Integer companyId);

	Integer getOrgAdminIdByTeamMemberId(Integer teamMemberId);

	public Integer getPrimaryKeyId(Integer teamMemberId);

	Integer getTeamMemberSuperiorId(Integer loggedInUserId);

	TeamMember getByTeamMemberId(Integer teamMemberId);

	public RoleDTO getSuperiorIdAndRolesByTeamMemberId(Integer teamMemberId);

	public void apporveTeamMember(Integer teamMemberId);

	List<Integer> getAllTeamMemberIdsByOrgAdmin(Integer orgAdminId);

	boolean getModuleAccess(Integer userId);

	Integer getSuperiorId(Integer userId);

	public List<Integer> findSuperVisorsAndTeamMembers(Integer teamMemberId);

	List<Integer> listSecondOrgAdminAndSuperVisorsByCompanyId(Integer companyId);

	List<Integer> listAllTeamMembersByCompanyId(Integer companyId);

	List<Integer> listAllSuperVisorsByCompanyId(Integer companyId);

	TeamMemberListDTO findById(Integer id);

	public Integer getApprovedTeamMembersCount(Integer companyId);

	TeamMember getTeamMemberByUserId(Integer userId);

	List<CompanyAndRolesDTO> findCompanyDetailsAndRoles();

	CompanyAndRolesDTO findCompanyDetailsAndUserId(Integer companyId);

	/**** XNFR-85 *********/
	Map<String, Object> findTeamMemberDetailsByTeamMemberGroupId(Pagination pagination);

	Integer getAssignedPartnersCount(Integer userId);

	public AdminAndTeamMemberDetailsDTO getTeamMemberPartnerMasterListName(Integer teamMemberGroupMappingId);

	/**** XNFR-85 *********/
	Map<String, Object> findPartners(Pagination pagination);

	public TeamMemberAndPartnerIdsAndUserListIdDTO findTeamMemberIdAndPartnerIds(
			List<Integer> partnerTeamGroupMappingIds);

	public void deleteTeamMemberPartners(Integer userListId, List<Integer> partnerIds);

	public void deletePartnerTeamGroupMappingIds(List<Integer> partnerTeamGroupMappingIds);

	/**** XNFR-107 ***********/
	public void saveTeamMemberPartnerList(TeamMember teamMember, User teamMemberUser, boolean newPartnerGroupRequired);

	public void saveTeamMemberUserUserListsAndUpdateTeamMemberPartnerList(List<UserUserList> teamMemberPartners,
			UserList userList, User teamMemberUser, TeamMember teamMember);

	/******* XNFR-139 ******/
	Integer getSecondAdminsCountByCompanyId(Integer companyId);

	List<Integer> findSecondAdminTeamMemberIds(Integer companyId);

	AnalyticsCountDTO findMaxAdminAnalyticsByCompanyId(Integer companyId);

	Integer findPrimaryAdminIdByCompanyId(Integer companyId);

	Integer findPrimaryAdminIdByCompanyProfileName(String companyProfileName);

	void updatePrimaryAdminId(Integer existingPrimaryAdminId, Integer newPrimaryAdminId);

	List<TeamMemberListDTO> findPrimaryAdminAndExtraAdmins(Integer companyId);

	public Integer getTeamMemberGroupIdById(Integer teamMemberGroupUserMappingId);

	List<Integer> findPartnershipIdsFromPartnerTeamGroupMapping();

	List<ViewTypePatchRequestDTO> findTeamMemberIdsAndUserIdsByCompanyId(Integer companyId);

	public TeamMemberDTO getTeamMemberStatus(Integer teamMemberId);

	public Integer findOrgAdminIdByTeamMemberId(Integer teamMemberId);
	
	public boolean getTeamMemberOption(Integer teamMemberId);

	EmailTemplateDTO fetchCustomTemplate(Integer id, Integer userId);

	EmailTemplateDTO fetchDefaultTemplate(Integer id, Integer userId);
	
	UserDetailsUtilDTO findPrimaryAdminDetailsByCompanyId(Integer companyId);

	List<UserDTO> findTeamMemberIdsByCompanyId(Integer partnerCompanyId);

	/*** XNFR-1022 ****/
	String findTeamMemberFullNameOrEmaiIdByPartnerCompanyId(Integer partnerCompanyId, Integer vendorCompanyId);

	Integer findTeamMemberGroupIdByAlias(String groupAlias);

	Integer findChannelAccountManagerIdByCompanyId(Integer companyId);
	
	public List<Integer> findPartnerTeamGroupMappingIdsByPartnershipIds(List<Integer> partnershipIds, Integer teamMemberId); 
	
	List<TeamMemberListDTO> fetchTeamMemberDetailsForGorupOfPartners(Integer userListId);

	boolean hasMarketingModulesAccessToTeamMember(Integer loggedInUserId);

	Integer findTeamMemberGroupUserMappingIdByEmailId(String emailId, Integer companyId);

}
