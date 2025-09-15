package com.xtremand.team.member.group.service;

import java.util.List;
import java.util.Map;

import com.xtremand.campaign.bom.ModuleAccessDTO;
import com.xtremand.common.bom.Pagination;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.team.member.dto.TeamMemberGroupDTO;
import com.xtremand.team.member.group.bom.TeamMemberGroup;
import com.xtremand.util.dto.Pageable;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;

public interface TeamMemberGroupService {

	XtremandResponse findAll(Pagination pagination);

	XtremandResponse findDefaultModules(VanityUrlDetailsDTO vanityUrlDetailsDTO);

	XtremandResponse save(TeamMemberGroupDTO teamMemberGroupDto);

	XtremandResponse findById(VanityUrlDetailsDTO vanityUrlDetailsDTO);

	XtremandResponse update(TeamMemberGroupDTO teamMemberGroupDto, XtremandResponse response);

	XtremandResponse delete(Integer id);

	XtremandResponse findAllGroupIdsAndNamesByLoggedInUserId(Integer userId, boolean addDefaultOption);

	String addTeamMemberGroups();

	XtremandResponse hasSuperVisorRole(Integer teamMemberGroupId);

	XtremandResponse previewGroupDetailsById(Integer id);

	XtremandResponse addDefaultGroups(String defaultGroupName);

	void addDefaultGroups(Integer roleId, Integer companyId, ModuleAccessDTO moduleAccessDTO);

	/******* XNFR-85 *********/
	List<TeamMemberGroup> findAllGroupIdsAndNamesByCompanyId(Integer companyId, boolean addDefaultOption);

	XtremandResponse findSelectedTeamMemberIdsByPartnershipId(Integer partnershipId);

	XtremandResponse findPartnersCountByTeamMemberGroupId(Integer teamMemberGroupId);

	void findAndDeleteTeamMemberGroupUserMappingsByCompanyId(Integer companyId);

	void findAndDeleteTeamMemberGroupRoleMappingsByCompanyId(Integer companyId);

	void findAndDeleteAllTeamMemberGroupsByCompanyId(Integer companyId);

	void upgradeTeamMemberGroups(Integer roleId, Integer companyId, ModuleAccessDTO moduleAccessDTO);


	Map<String, Object> getRoleIdsAndRoleNamesByUserId(Integer userId);

	/*** XNFR-883 ***/
	XtremandResponse updateDefaultSSOGroup(Integer id);

	XtremandResponse findAllGroupIdsAndNamesWithDefaultSSOFirst(String companyProfileName, Integer loggedInUserId);

	XtremandResponse findPaginatedTeamMemberGroupSignUpUrls(Integer loggedInUserId, String domainName,
			boolean isVanityLogin, Pageable pageable);

}
