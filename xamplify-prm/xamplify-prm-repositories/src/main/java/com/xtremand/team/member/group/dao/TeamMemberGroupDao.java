package com.xtremand.team.member.group.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.xtremand.common.bom.Pagination;
import com.xtremand.partnership.dto.PartnerTeamMemberGroupDTO;
import com.xtremand.team.member.dto.AddTeamMemberGroup;
import com.xtremand.team.member.dto.TeamMemberGroupDTO;
import com.xtremand.team.member.group.bom.TeamMemberGroup;

public interface TeamMemberGroupDao {

	Map<String, Object> findGroups(Pagination pagination);

	TeamMemberGroup findTeamMemberGroupById(Integer id);

	void deleteUnMappedRoleIds(Integer teamMemberGroupId, Set<Integer> unmappedRoleIds);

	void delete(Integer id);

	List<TeamMemberGroupDTO> findTeamMemberGroupRoleMappingIdsAndNamesByRoleIdAndCompanyId(Integer companyId,
			Integer roleId);

	List<TeamMemberGroupDTO> findTeamMemberGroupIdsAndNamesByRoleIdAndCompanyId(Integer companyId, Integer roleId);

	List<Integer> findRoleIdsByTeamMemberGroupId(Integer teamMemberGroupId);

	void deleteFromTeamMemberGroupRoleMapping(List<Integer> ids);

	boolean isTeamMemberGroupRoleMappingRowExists(Integer teamMemberGroupId, Integer roleId);

	List<TeamMemberGroup> findGroupIdsAndNamesByCompanyId(Integer companyId);

	List<AddTeamMemberGroup> addTeamMemberGroupsToCompanies();

	List<Integer> findTeamMemberUserIdsByRoleIdAndCompanyId(Integer companyId, Integer roleId);

	List<Integer> findTeamMemberUserIdsByGroupId(Integer teamMemberGroupId);

	boolean hasSuperVisorRole(Integer teamMemberGroupId);

	boolean isGroupAssignedToSecondAdmin(Integer teamMemberGroupId);

	String findTeamMemberGroupNameById(Integer id);

	TeamMemberGroupDTO previewGroupById(Integer id);

	List<Integer> findTeamMemberGroupRoleIdsByTeamMemberUserId(Integer teamMemberUserId);

	List<TeamMemberGroupDTO> findDefaultGroupsByCompanyId(Integer companyId);

	List<Integer> findDefaultGroupCompanyIds(String groupName);

	/******* XNFR-85 *********/

	List<PartnerTeamMemberGroupDTO> findTeamMemberGroupIdByPartnershipId(Integer partnershipId);

	Set<Integer> findSelectedTeamMemberGroupUserMappingIdsByPartnershipId(Integer partnershipId);

	boolean isTeamMemberGroupAssignedToPartnerByTeamMemberGroupId(Integer teamMemberGroupId, Integer teamMemberId);

	/******* XNFR-98 *********/
	public List<Integer> findTeamMemberIdsByTeamMemberGroupUserMappingIds(List<Integer> teamMemberGroupUserMappingIds);

	public List<Integer> findAssociatedPartnershipIdsByTeamMemberGroupId(Integer teamMemberGroupId);

	/******* XNFR-108 *********/

	public Set<Integer> findSelectedTeamMemberGroupUserMappingIdsByFormId(Integer formId);

	/**** XNFR-117 ****/
	public void deleteByCompanyId(Integer companyId);

	public List<Integer> findIdsByCompanyId(Integer companyId);

	public void deleteFromTeamMemberGroupUserMapping(List<Integer> teamMemberGroupIds);

	public void changeDefaultGroup(Integer companyId, Integer userId);

	public void deleteTeamMemberGroupRoleMappings(List<Integer> teamMemberGroupIds);

	public TeamMemberGroup findGroupIdsByPartnerCompanyId(Integer companyId);

	/*** XNFR-883 ***/
	void setDefaultSSOGroupToFalseByCompanyId(Integer compnayId);


	void updateDefaultSSOGroupById(Integer id);

	List<TeamMemberGroup> findGroupIdsAndNamesByCompanyIdAndDefaultSSOGroupId(Integer companyId,
			Integer defaultSSOGroupId);
	
	/*** XNFR-1046 ***/
	public List<Integer> getPartnerTeamGroupMapping(Integer teamMemberGroupId,Integer teamMemberId);

	public void deletePartnerTeamGroupMapping(List<Integer> partnerTeamGroupMappingIds);

	public boolean isLastTeamMemberGroupById(Integer id);

	public Map<String, Object> findPaginatedTeamMemberGroupSignUpUrls(Pagination pagination);

}
