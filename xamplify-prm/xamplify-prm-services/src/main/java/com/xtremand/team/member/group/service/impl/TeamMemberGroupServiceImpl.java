package com.xtremand.team.member.group.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.approve.dao.ApproveDAO;
import com.xtremand.approve.dto.ContentReApprovalDTO;
import com.xtremand.approve.dto.TeamMemberApprovalPrivilegesDTO;
import com.xtremand.approve.service.ApproveService;
import com.xtremand.campaign.bom.ModuleAccessDTO;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.common.bom.Pagination;
import com.xtremand.dam.bom.ApprovalStatusHistory;
import com.xtremand.dam.bom.DamTag;
import com.xtremand.dam.dao.DamDao;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.exception.DuplicateEntryException;
import com.xtremand.form.dao.FormDao;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.team.dao.TeamDao;
import com.xtremand.team.member.dto.AddTeamMemberGroup;
import com.xtremand.team.member.dto.TeamMemberGroupDTO;
import com.xtremand.team.member.dto.TeamMemberModuleDTO;
import com.xtremand.team.member.group.bom.TeamMemberGroup;
import com.xtremand.team.member.group.bom.TeamMemberGroupRoleMapping;
import com.xtremand.team.member.group.bom.TeamMemberGroupUserMapping;
import com.xtremand.team.member.group.dao.TeamMemberGroupDao;
import com.xtremand.team.member.group.exception.TeamMemberGroupDataAccessException;
import com.xtremand.team.member.group.service.TeamMemberGroupService;
import com.xtremand.user.bom.Role;
import com.xtremand.user.bom.TeamMember;
import com.xtremand.user.bom.User;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.util.BadRequestException;
import com.xtremand.util.GenerateRandomPassword;
import com.xtremand.util.XamplifyUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.dao.XamplifyUtilDao;
import com.xtremand.util.dto.CompanyAndRolesDTO;
import com.xtremand.util.dto.LeftSideNavigationBarItem;
import com.xtremand.util.dto.Pageable;
import com.xtremand.util.dto.XamplifyConstants;
import com.xtremand.util.service.UtilService;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;
import com.xtremand.video.bom.VideoTag;
import com.xtremand.video.dao.VideoDao;

@Service
@Transactional
public class TeamMemberGroupServiceImpl implements TeamMemberGroupService {

	private static final String SHARE_LEADS = "Share Leads";

	private static final String PARTNER_ACCOUNT_MANAGER = "Partner Account Manager";

	private static final String PRM_ACCOUNT_MANAGER = "PRM Account Manager";

	private static final String VIDEO_TAGS_TO_SAVE_MAP_KEY = "videoTagsToSave";

	private static final String VIDEO_IDS_TO_DELETE_MAP_KEY = "videoIdsToDelete";

	private static final String WHITE_LABELED_RE_APPROVAL_DAM_IDS = "whiteLabeledReApprovalDamIds";

	private static final String APPROVAL_HISTORY_LIST_MAP_KEY = "approvalHistoryList";

	@Autowired
	private TeamMemberGroupDao teamMemberGroupDao;

	@Autowired
	private GenericDAO genericDao;

	@Autowired
	private UserDAO userDao;

	@Autowired
	private UtilService utilService;

	@Autowired
	private UtilDao utilDao;

	@Autowired
	private TeamDao teamDao;

	@Autowired
	private FormDao formDao;

	@Autowired
	private XamplifyUtil xamplifyUtil;

	@Autowired
	private ApproveDAO approveDao;

	@Autowired
	private ApproveService approveService;

	@Autowired
	private DamDao damDao;

	@Autowired
	private XamplifyUtilDao xamplifyUtilDao;

	@Autowired
	private VideoDao videoDAO;

	@Value("${web_url}")
	private String webUrl;

	@SuppressWarnings("unchecked")
	@Override
	public XtremandResponse findAll(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		Integer loggedInUserId = pagination.getUserId();
		String companyProfileName = utilDao.getPrmCompanyProfileName();
		String partnerModuleCustomName = utilService.findPartnerModuleCustomName(loggedInUserId);
		/******* XNFR-108 *********/
		findExistingTeamMemberIds(pagination);
		/******* XNFR-108 ENDS *********/
		VanityUrlDetailsDTO vanityUrlDetailsDTO = new VanityUrlDetailsDTO();
		vanityUrlDetailsDTO.setUserId(loggedInUserId);
		vanityUrlDetailsDTO.setVanityUrlFilter(pagination.isVanityUrlFilter());
		vanityUrlDetailsDTO.setVendorCompanyProfileName(companyProfileName);
		String signUpUrl = findTeamMemberGroupSignupUrl(vanityUrlDetailsDTO);
		Map<String, Object> map = teamMemberGroupDao.findGroups(pagination);
		List<TeamMemberGroupDTO> teamMemberGroupDTOs = (List<TeamMemberGroupDTO>) map.get("list");
		boolean hasPartnerAccess = false;
		for (TeamMemberGroupDTO teamMemberGroupDTO : teamMemberGroupDTOs) {
			Integer companyId = teamMemberGroupDTO.getCompanyId();
			teamMemberGroupDTO.setSignUpUrl(signUpUrl + teamMemberGroupDTO.getAlias());
			utilService.addModules(teamMemberGroupDTO, partnerModuleCustomName);
			if (teamMemberGroupDTO.isMarketingModulesAccessToTeamMemberGroup() && hasPartnerAccess) {
				List<TeamMemberModuleDTO> teamMemberModuleDTOs = teamMemberGroupDTO.getTeamMemberModuleDTOs();
				teamMemberModuleDTOs
						.add(new TeamMemberModuleDTO("Marketing Modules", "fa fa-cogs", "Marketing Modules", 0, true));
			}
		}
		response.setStatusCode(200);
		response.setData(map);
		return response;
	}

	/******* XNFR-108 *********/

	private void findExistingTeamMemberIds(Pagination pagination) {
		List<Integer> findSelecetedTeamMemberIds = new ArrayList<>();
		if ("form".equals(pagination.getType()) && pagination.getFormId() > 0) {
			findSelecetedTeamMemberIds = formDao.getAllSelecetedGroupIds(pagination.getFormId());
		}
		if (findSelecetedTeamMemberIds != null && !findSelecetedTeamMemberIds.isEmpty()) {
			pagination.setFiltertedEmailTempalteIds(findSelecetedTeamMemberIds);
		}
	}

	@Override
	public XtremandResponse findDefaultModules(VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		XtremandResponse response = new XtremandResponse();
		Integer loggedInUserId = vanityUrlDetailsDTO.getUserId();
		if (XamplifyUtils.isValidInteger(loggedInUserId)) {
			response.setStatusCode(200);
			List<TeamMemberModuleDTO> modules = new ArrayList<>();
			Map<String, Object> map = new HashMap<>();
			List<Integer> assignedModuleIds = new ArrayList<>();
			addModules(loggedInUserId, modules, map, assignedModuleIds);
			boolean hasPartnerAccess = false;
			if (hasPartnerAccess) {
				List<TeamMemberModuleDTO> marketingModules = new ArrayList<>();
				map.put("hasMarketingModulesAccessToTeamMemberGroup", false);
				map.put("marketingModules", marketingModules);
			}
			response.setData(map);
		} else {
			response.setStatusCode(400);
			response.setMessage("userId is missing");
		}
		return response;
	}

	private void addModules(Integer userId, List<TeamMemberModuleDTO> modules, Map<String, Object> map,
			List<Integer> assignedModuleIds) {
		LeftSideNavigationBarItem leftSideNavigationBarItem = utilService.findModulesForTeamMemberGroup(userId);
		String partnerModuleCustomName = utilService.findPartnerModuleCustomName(userId);
		addTeamMemberGroupModules(modules, leftSideNavigationBarItem, assignedModuleIds, partnerModuleCustomName);
		map.put("modules", modules);
	}

	private void addTeamMemberGroupModules(List<TeamMemberModuleDTO> modules,
			LeftSideNavigationBarItem leftSideNavigationBarItem, List<Integer> assignedModuleIds,
			String partnerModuleCustomName) {
		xamplifyUtil.addAllModule(modules, assignedModuleIds);
		addPartnersModuleByAccess(modules, leftSideNavigationBarItem, assignedModuleIds, partnerModuleCustomName);
		addStatsModuleByAccess(modules, leftSideNavigationBarItem, assignedModuleIds);
		addShareLeadsModuleByAccess(modules, leftSideNavigationBarItem, assignedModuleIds);
		addOpportunityModuleByAccess(modules, leftSideNavigationBarItem, assignedModuleIds);
		addPrmModules(modules, leftSideNavigationBarItem, assignedModuleIds);
	}

	private void addOpportunityModuleByAccess(List<TeamMemberModuleDTO> modules,
			LeftSideNavigationBarItem leftSideNavigationBarItem, List<Integer> assignedModuleIds) {
		if (leftSideNavigationBarItem.isEnableLeads()) {
			xamplifyUtil.addOpportunityModule(modules, assignedModuleIds);
		}
	}

	private void addShareLeadsModuleByAccess(List<TeamMemberModuleDTO> modules,
			LeftSideNavigationBarItem leftSideNavigationBarItem, List<Integer> assignedModuleIds) {
		if (leftSideNavigationBarItem.isShareLeads() || leftSideNavigationBarItem.isSharedLeads()) {
			boolean shareLeadsAndSharedLeads = leftSideNavigationBarItem.isShareLeads()
					&& leftSideNavigationBarItem.isSharedLeads();
			boolean onlyShareLeads = leftSideNavigationBarItem.isShareLeads()
					&& !leftSideNavigationBarItem.isSharedLeads();
			boolean onlySharedLeads = leftSideNavigationBarItem.isSharedLeads()
					&& !leftSideNavigationBarItem.isShareLeads();
			String moduleName = "";
			if (shareLeadsAndSharedLeads) {
				moduleName = "Share / Shared Leads";
			} else if (onlyShareLeads) {
				moduleName = SHARE_LEADS;
			} else if (onlySharedLeads) {
				moduleName = "Shared Leads";
			}
			xamplifyUtil.addShareLeadsModule(modules, assignedModuleIds, moduleName, "");
		}
	}

	private void addStatsModuleByAccess(List<TeamMemberModuleDTO> modules,
			LeftSideNavigationBarItem leftSideNavigationBarItem, List<Integer> assignedModuleIds) {
		if (leftSideNavigationBarItem.isStats()) {
			xamplifyUtil.addStatsModule(modules, assignedModuleIds);
		}
	}

	private void addPartnersModuleByAccess(List<TeamMemberModuleDTO> modules,
			LeftSideNavigationBarItem leftSideNavigationBarItem, List<Integer> assignedModuleIds,
			String partnerModuleCustomName) {
		if (leftSideNavigationBarItem.isPartners()) {
			xamplifyUtil.addPartnersModule(modules, assignedModuleIds, partnerModuleCustomName);
		}
	}

	private void addPrmModules(List<TeamMemberModuleDTO> modules, LeftSideNavigationBarItem leftSideNavigationBarItem,
			List<Integer> assignedModuleIds) {
		addMdfModuleByAccess(modules, leftSideNavigationBarItem, assignedModuleIds);
		addDamModuleByAccess(modules, leftSideNavigationBarItem, assignedModuleIds);
		addLmsModuleByAccess(modules, leftSideNavigationBarItem, assignedModuleIds);
		addPlaybookModuleByAccess(modules, leftSideNavigationBarItem, assignedModuleIds);
	}

	private void addPlaybookModuleByAccess(List<TeamMemberModuleDTO> modules,
			LeftSideNavigationBarItem leftSideNavigationBarItem, List<Integer> assignedModuleIds) {
		if (leftSideNavigationBarItem.isPlaybook() || leftSideNavigationBarItem.isPlaybookAccessAsPartner()) {
			xamplifyUtil.addPlayBookModule(modules, assignedModuleIds);
		}
	}

	private void addLmsModuleByAccess(List<TeamMemberModuleDTO> modules,
			LeftSideNavigationBarItem leftSideNavigationBarItem, List<Integer> assignedModuleIds) {
		if (leftSideNavigationBarItem.isLms() || leftSideNavigationBarItem.isLmsAccessAsPartner()) {
			xamplifyUtil.addLmsModule(modules, assignedModuleIds);
		}
	}

	private void addDamModuleByAccess(List<TeamMemberModuleDTO> modules,
			LeftSideNavigationBarItem leftSideNavigationBarItem, List<Integer> assignedModuleIds) {
		if (leftSideNavigationBarItem.isDam() || leftSideNavigationBarItem.isDamAccessAsPartner()) {
			xamplifyUtil.addDamModule(modules, assignedModuleIds);
		}
	}

	private void addMdfModuleByAccess(List<TeamMemberModuleDTO> modules,
			LeftSideNavigationBarItem leftSideNavigationBarItem, List<Integer> assignedModuleIds) {
		if (leftSideNavigationBarItem.isMdf() || leftSideNavigationBarItem.isMdfAccessAsPartner()) {
			xamplifyUtil.addMdfModule(modules, assignedModuleIds, "");
		}
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse save(TeamMemberGroupDTO teamMemberGroupDto) {
		XtremandResponse response = new XtremandResponse();
		Set<Integer> roleIds = teamMemberGroupDto.getRoleIds();
		if (XamplifyUtils.isNotEmptySet(roleIds)) {
			response.setStatusCode(200);
			TeamMemberGroup teamMemberGroup = new TeamMemberGroup();
			BeanUtils.copyProperties(teamMemberGroupDto, teamMemberGroup);
			teamMemberGroup.setCompanyId(userDao.getCompanyIdByUserId(teamMemberGroupDto.getUserId()));
			teamMemberGroup.setCreatedUserId(teamMemberGroupDto.getUserId());
			teamMemberGroup.setUpdatedUserId(teamMemberGroupDto.getUserId());
			teamMemberGroup.setCreatedTime(new Date());
			teamMemberGroup.setUpdatedTime(new Date());
			GenerateRandomPassword password = new GenerateRandomPassword();
			teamMemberGroup.setAlias(password.getPassword());
			addTeamMemberGroupRoleMapping(roleIds, teamMemberGroup);
			/*** XNFR-883 ***/
			updateDefaultSsoGroupIfSelected(teamMemberGroupDto, teamMemberGroup.getCompanyId());
			genericDao.save(teamMemberGroup);
		} else {
			response.setStatusCode(404);
		}
		return response;
	}

	private void addTeamMemberGroupRoleMapping(Set<Integer> roleIds, TeamMemberGroup teamMemberGroup) {
		Set<TeamMemberGroupRoleMapping> teamMemberGroupRoleMappings = new HashSet<>();
		for (Integer roleId : roleIds) {
			TeamMemberGroupRoleMapping teamMemberGroupRoleMapping = new TeamMemberGroupRoleMapping();
			teamMemberGroupRoleMapping.setTeamMemberGroup(teamMemberGroup);
			teamMemberGroupRoleMapping.setRoleId(roleId);
			teamMemberGroupRoleMapping.setCreatedTime(new Date());
			teamMemberGroupRoleMappings.add(teamMemberGroupRoleMapping);
		}
		teamMemberGroup.setTeamMemberGroupRoleMappings(teamMemberGroupRoleMappings);
	}

	@Override
	public XtremandResponse findById(VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		XtremandResponse response = new XtremandResponse();
		Integer teamMemberGroupId = (Integer) vanityUrlDetailsDTO.getMap().get("teamMemberGroupId");
		TeamMemberGroup teamMemberGroup = teamMemberGroupDao.findTeamMemberGroupById(teamMemberGroupId);
		TeamMemberGroupDTO teamMemberGroupDTO = new TeamMemberGroupDTO();
		teamMemberGroupDTO.setId(teamMemberGroup.getId());
		teamMemberGroupDTO.setName(teamMemberGroup.getName());
		teamMemberGroupDTO.setDefaultGroup(teamMemberGroup.isDefaultGroup());
		String teamGroupSignUpUrl = findTeamMemberGroupSignupUrl(vanityUrlDetailsDTO);
		teamMemberGroupDTO.setSignUpUrl(teamGroupSignUpUrl + teamMemberGroup.getAlias());
		setTeamMemberGroupRoleIds(teamMemberGroup, teamMemberGroupDTO);
		response.setStatusCode(200);
		Map<String, Object> map = new HashMap<>();
		map.put("teamMemberGroupDTO", teamMemberGroupDTO);
		map.put("validForm", !teamMemberGroupDTO.getRoleIds().isEmpty());
		addSelectedAndOtherModules(vanityUrlDetailsDTO.getUserId(), teamMemberGroupDTO, map);
		boolean hasPartnerAccess = false;
		if (hasPartnerAccess) {
			List<TeamMemberModuleDTO> marketingModules = new ArrayList<>();
			map.put("marketingModules", marketingModules);
			teamMemberGroupDTO.setMarketingModulesAccessToTeamMemberGroup(
					teamMemberGroup.isMarketingModulesAccessToTeamMemberGroup());
		}
		response.setData(map);
		return response;
	}

	private String findTeamMemberGroupSignupUrl(VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		Integer loggedInUserId = vanityUrlDetailsDTO.getUserId();
		String domainName = vanityUrlDetailsDTO.getVendorCompanyProfileName();
		utilService.isVanityUrlFilterApplicable(vanityUrlDetailsDTO);
		String domainUrl = webUrl;
		if (vanityUrlDetailsDTO.isVendorLoggedInThroughOwnVanityUrl()) {
			domainUrl = xamplifyUtil.frameVanityURL(webUrl, domainName);
		} else {
			domainName = userDao.getCompanyProfileNameByUserId(loggedInUserId);
		}
		return (domainUrl + XamplifyConstants.TEAM_MEMBER_SIGN_UP_URL_PREFIX + "/" + domainName + "/");
	}

	private void addSelectedAndOtherModules(Integer userId, TeamMemberGroupDTO teamMemberGroupDTO,
			Map<String, Object> map) {
		List<TeamMemberModuleDTO> modules = new ArrayList<>();
		List<Integer> roleIdsArrayList = XamplifyUtils.convertSetToList(teamMemberGroupDTO.getRoleIds());
		addModules(userId, modules, map, roleIdsArrayList);
	}

	private void setTeamMemberGroupRoleIds(TeamMemberGroup teamMemberGroup, TeamMemberGroupDTO teamMemberGroupDTO) {
		if (teamMemberGroup.getTeamMemberGroupRoleMappings() != null
				&& !teamMemberGroup.getTeamMemberGroupRoleMappings().isEmpty()) {
			Set<Integer> roleIds = teamMemberGroup.getTeamMemberGroupRoleMappings().stream()
					.map(TeamMemberGroupRoleMapping::getRoleId).collect(Collectors.toSet());
			teamMemberGroupDTO.setRoleIds(roleIds);
		} else {
			teamMemberGroupDTO.setRoleIds(new HashSet<>());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse update(TeamMemberGroupDTO teamMemberGroupDto, XtremandResponse response) {
		try {
			Integer teamMemberGroupId = teamMemberGroupDto.getId();
			Integer loggedInUserId = teamMemberGroupDto.getUserId();
			TeamMemberGroup teamMemberGroup = teamMemberGroupDao.findTeamMemberGroupById(teamMemberGroupId);
			teamMemberGroup.setName(teamMemberGroupDto.getName());
			teamMemberGroup.setUpdatedTime(new Date());
			teamMemberGroup.setUpdatedUserId(loggedInUserId);

			Set<Integer> roleIds = teamMemberGroupDto.getRoleIds();
			List<Integer> roleIdsInArray = XamplifyUtils.convertSetToList(roleIds);
			List<Integer> teamMemberIds = teamMemberGroupDao.findTeamMemberUserIdsByGroupId(teamMemberGroup.getId());
			Set<Integer> existingRoleIds = getExistingRoleIds(teamMemberGroup);
			deleteUnMappedRoles(teamMemberGroupId, teamMemberGroup, roleIds, teamMemberIds);
			addNewRoles(teamMemberGroup, roleIds, loggedInUserId);
			/** XNFR-821 **/
			Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
			updateApprovalAuthorities(roleIdsInArray, existingRoleIds, teamMemberGroupDto, teamMemberIds, companyId);

			Map<String, Object> approvalsMap = autoApprovePendingContent(loggedInUserId, teamMemberIds,
					teamMemberGroupDto.isApprovalManager(), companyId);

			if (approvalsMap.containsKey(WHITE_LABELED_RE_APPROVAL_DAM_IDS)) {
				teamMemberGroupDto.setWhiteLabeledReApprovalDamIds(
						(List<Integer>) approvalsMap.get(WHITE_LABELED_RE_APPROVAL_DAM_IDS));
				teamMemberGroupDto.setCompanyId(companyId);
			}

			/*** XNFR-883 ***/
			boolean isDefaultSsoGroupSelected = updateDefaultSsoGroupIfSelected(teamMemberGroupDto, companyId);
			teamMemberGroup.setDefaultSsoGroup(isDefaultSsoGroupSelected);
			teamMemberGroup.setMarketingModulesAccessToTeamMemberGroup(
					teamMemberGroupDto.isMarketingModulesAccessToTeamMemberGroup());
			/*** XNFR-883 ***/

			boolean isGroupAssignedToSecondAdmin = teamMemberGroupDao.isGroupAssignedToSecondAdmin(teamMemberGroupId);
			if (isGroupAssignedToSecondAdmin && roleIdsInArray.indexOf(Role.ALL_ROLES.getRoleId()) < 0) {
				throw new BadRequestException(
						"Modules cannot be updated as this group is assigned to second admin.Please unassign to update modules.");
			} else {
				performApprovalInsertionOperations(approvalsMap);
				List<ContentReApprovalDTO> pdfTypeAssetContentDetails = (List<ContentReApprovalDTO>) approvalsMap
						.get("pdfTypeAssetContentDetails");
				approveService.handleSharedAssetPathForPdfTypeAssets(pdfTypeAssetContentDetails);
				response.setStatusCode(200);
			}
			return response;
		} catch (BadRequestException e) {
			throw new BadRequestException(e.getMessage());
		} catch (TeamMemberGroupDataAccessException mex) {
			throw new TeamMemberGroupDataAccessException(mex);
		} catch (Exception e) {
			throw new TeamMemberGroupDataAccessException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private void performApprovalInsertionOperations(Map<String, Object> approvalsMap) {

		List<ApprovalStatusHistory> approvalHistoryList = (List<ApprovalStatusHistory>) approvalsMap
				.get(APPROVAL_HISTORY_LIST_MAP_KEY);
		List<Integer> videoIdsToDelete = (List<Integer>) approvalsMap.get(VIDEO_IDS_TO_DELETE_MAP_KEY);
		List<Integer> damIdsToDelete = (List<Integer>) approvalsMap.get("damIdsToDelete");
		List<DamTag> allDamTagsToSave = (List<DamTag>) approvalsMap.get("allDamTagsToSave");
		List<VideoTag> videoTagsToSave = (List<VideoTag>) approvalsMap.get(VIDEO_TAGS_TO_SAVE_MAP_KEY);

		if (XamplifyUtils.isNotEmptyList(approvalHistoryList)) {
			xamplifyUtilDao.saveAll(approvalHistoryList, "Approval Status History");
		}

		if (XamplifyUtils.isNotEmptyList(videoIdsToDelete)) {
			videoDAO.deleteVideoRecordsByIds(videoIdsToDelete);
		}

		if (XamplifyUtils.isNotEmptyList(damIdsToDelete)) {
			damDao.deleteByDamIds(damIdsToDelete);
		}

		if (XamplifyUtils.isNotEmptyList(allDamTagsToSave)) {
			xamplifyUtilDao.saveAll(allDamTagsToSave, "Dam Tag for Re-Approval");
		}

		if (XamplifyUtils.isNotEmptyList(videoTagsToSave)) {
			xamplifyUtilDao.saveAll(videoTagsToSave, "VideoTag for Re-Approval");
		}
	}

	private boolean updateDefaultSsoGroupIfSelected(TeamMemberGroupDTO teamMemberGroupDto, Integer companyId) {
		boolean isDefaultSsoGroupSelected = teamMemberGroupDto.isDefaultSsoGroup();
		if (isDefaultSsoGroupSelected) {
			teamMemberGroupDao.setDefaultSSOGroupToFalseByCompanyId(companyId);
		}
		return isDefaultSsoGroupSelected;
	}

	private void addNewRoles(TeamMemberGroup teamMemberGroup, Set<Integer> roleIds, Integer loggedInUserId) {
		Set<Integer> existingRoleIds = getExistingRoleIds(teamMemberGroup);
		roleIds.removeAll(existingRoleIds);
		if (!roleIds.isEmpty()) {
			Set<TeamMemberGroupRoleMapping> teamMemberGroupRoleMappings = new HashSet<>();
			for (Integer roleId : roleIds) {
				if (Role.MDF.getRoleId().equals(roleId)) {
					addMDFRoleIfAvailable(teamMemberGroup, loggedInUserId, teamMemberGroupRoleMappings, roleId);
				} else if (Role.DAM.getRoleId().equals(roleId)) {
					addDAMRoleIfAvailable(teamMemberGroup, loggedInUserId, teamMemberGroupRoleMappings, roleId);
				} else if (Role.LEARNING_TRACK.getRoleId().equals(roleId)) {
					addLearningTrackRoleIfAvailable(teamMemberGroup, loggedInUserId, teamMemberGroupRoleMappings,
							roleId);
				} else if (Role.PLAY_BOOK.getRoleId().equals(roleId)) {
					addPlaybookRoleIfAvailable(teamMemberGroup, loggedInUserId, teamMemberGroupRoleMappings, roleId);
				} else if (Role.OPPORTUNITY.getRoleId().equals(roleId)) {
					addOpportunitiesRoleIfAvailable(teamMemberGroup, loggedInUserId, teamMemberGroupRoleMappings,
							roleId);
				} else if (Role.SHARE_LEADS.getRoleId().equals(roleId)) {
					addShareLeadsRoleIfAvailable(teamMemberGroup, loggedInUserId, teamMemberGroupRoleMappings, roleId);
				} else {
					addRolesToTeamMemberGroupRoleMappingAndTeamMemberUsers(teamMemberGroup, teamMemberGroupRoleMappings,
							roleId);
				}
			}
			teamMemberGroup.setTeamMemberGroupRoleMappings(teamMemberGroupRoleMappings);
		}
	}

	private void addShareLeadsRoleIfAvailable(TeamMemberGroup teamMemberGroup, Integer loggedInUserId,
			Set<TeamMemberGroupRoleMapping> teamMemberGroupRoleMappings, Integer roleId) {
		boolean shareLeadsAccess = utilDao.hasShareLeadsAccessByUserId(loggedInUserId);
		boolean sharedLeadsAccess = utilDao.sharedLeadsAccessForPartner(loggedInUserId);
		if (shareLeadsAccess || sharedLeadsAccess) {
			addRolesToTeamMemberGroupRoleMappingAndTeamMemberUsers(teamMemberGroup, teamMemberGroupRoleMappings,
					roleId);
		}
	}

	private void addOpportunitiesRoleIfAvailable(TeamMemberGroup teamMemberGroup, Integer loggedInUserId,
			Set<TeamMemberGroupRoleMapping> teamMemberGroupRoleMappings, Integer roleId) {
		boolean opportunitiesAccess = utilDao.hasEnableLeadsAccessByUserId(loggedInUserId);
		boolean opportunitiesAccessAsPartner = utilDao.enableLeadsForPartner(loggedInUserId);
		if (opportunitiesAccess || opportunitiesAccessAsPartner) {
			addRolesToTeamMemberGroupRoleMappingAndTeamMemberUsers(teamMemberGroup, teamMemberGroupRoleMappings,
					roleId);
		}
	}

	private void addPlaybookRoleIfAvailable(TeamMemberGroup teamMemberGroup, Integer loggedInUserId,
			Set<TeamMemberGroupRoleMapping> teamMemberGroupRoleMappings, Integer roleId) {
		boolean playbookAccess = utilDao.hasPlaybookAccessByUserId(loggedInUserId);
		boolean playbookAccessAsPartner = utilDao.playbookAccessForPartner(loggedInUserId);
		if (playbookAccess || playbookAccessAsPartner) {
			addRolesToTeamMemberGroupRoleMappingAndTeamMemberUsers(teamMemberGroup, teamMemberGroupRoleMappings,
					roleId);
		}
	}

	private void addLearningTrackRoleIfAvailable(TeamMemberGroup teamMemberGroup, Integer loggedInUserId,
			Set<TeamMemberGroupRoleMapping> teamMemberGroupRoleMappings, Integer roleId) {
		boolean hasLearningTrackAccess = utilDao.hasLmsAccessByUserId(loggedInUserId);
		boolean hasLearningTrackAccessAsPartner = utilDao.lmsAccessForPartner(loggedInUserId);
		if (hasLearningTrackAccess || hasLearningTrackAccessAsPartner) {
			addRolesToTeamMemberGroupRoleMappingAndTeamMemberUsers(teamMemberGroup, teamMemberGroupRoleMappings,
					roleId);
		}
	}

	private void addDAMRoleIfAvailable(TeamMemberGroup teamMemberGroup, Integer loggedInUserId,
			Set<TeamMemberGroupRoleMapping> teamMemberGroupRoleMappings, Integer roleId) {
		boolean hasDamAccess = utilDao.hasDamAccessByUserId(loggedInUserId);
		boolean hasDamAccessAsPartner = utilDao.damAccessForPartner(loggedInUserId);
		if (hasDamAccess || hasDamAccessAsPartner) {
			addRolesToTeamMemberGroupRoleMappingAndTeamMemberUsers(teamMemberGroup, teamMemberGroupRoleMappings,
					roleId);
		}
	}

	private void addMDFRoleIfAvailable(TeamMemberGroup teamMemberGroup, Integer loggedInUserId,
			Set<TeamMemberGroupRoleMapping> teamMemberGroupRoleMappings, Integer roleId) {
		boolean hasMdfAccess = utilDao.hasMdfAccessByUserId(loggedInUserId);
		boolean hasMdfAccessAsPartner = utilDao.mdfAccessForPartner(loggedInUserId);
		if (hasMdfAccess || hasMdfAccessAsPartner) {
			addRolesToTeamMemberGroupRoleMappingAndTeamMemberUsers(teamMemberGroup, teamMemberGroupRoleMappings,
					roleId);
		}
	}

	private void addRolesToTeamMemberGroupRoleMappingAndTeamMemberUsers(TeamMemberGroup teamMemberGroup,
			Set<TeamMemberGroupRoleMapping> teamMemberGroupRoleMappings, Integer roleId) {
		addRoleToTeamMemberGroupRoleMapping(teamMemberGroup, teamMemberGroupRoleMappings, roleId);
		List<Integer> teamMemberUserIds = teamMemberGroupDao.findTeamMemberUserIdsByGroupId(teamMemberGroup.getId());
		Set<Integer> roleIds = new HashSet<>();
		roleIds.add(roleId);
		for (Integer teamMemberUserId : teamMemberUserIds) {
			teamDao.addNewRoles(teamMemberUserId, roleIds);
		}

	}

	private void deleteUnMappedRoles(Integer teamMemberGroupId, TeamMemberGroup teamMemberGroup, Set<Integer> roleIds,
			List<Integer> teamMemberIds) {
		Set<Integer> existingRoleIds = getExistingRoleIds(teamMemberGroup);
		existingRoleIds.removeAll(roleIds);
		teamDao.deleteUnMappedRoleIdsByTeamMemberIds(teamMemberIds, existingRoleIds);
		teamMemberGroupDao.deleteUnMappedRoleIds(teamMemberGroupId, existingRoleIds);
	}

	private Set<Integer> getExistingRoleIds(TeamMemberGroup teamMemberGroup) {
		return teamMemberGroup.getTeamMemberGroupRoleMappings().stream().map(TeamMemberGroupRoleMapping::getRoleId)
				.collect(Collectors.toSet());
	}

	@Override
	public XtremandResponse delete(Integer id) {
		try {
			XtremandResponse response = new XtremandResponse();
			List<Integer> teamMemberUserIdsList = teamMemberGroupDao.findTeamMemberUserIdsByGroupId(id);
			boolean isLastGroup = teamMemberGroupDao.isLastTeamMemberGroupById(id);
			if (XamplifyUtils.isNotEmptyList(teamMemberUserIdsList)) {
				response.setStatusCode(400);
				response.setMessage(
						"This group cannot be deleted.Because one or more team members are part of this group.");
			} else if (isLastGroup) {
				response.setStatusCode(400);
				response.setMessage("You can't delete this group—at least one team member group must remain.");
			} else {
				teamMemberGroupDao.delete(id);
				response.setStatusCode(200);
			}
			return response;
		} catch (DuplicateEntryException e) {
			throw new DuplicateEntryException(e.getMessage());
		} catch (TeamMemberGroupDataAccessException mex) {
			throw new TeamMemberGroupDataAccessException(mex);
		} catch (Exception e) {
			throw new TeamMemberGroupDataAccessException(e);

		}
	}

	@Override
	public XtremandResponse findAllGroupIdsAndNamesByLoggedInUserId(Integer loggedInUserId, boolean addDefaultOption) {
		XtremandResponse response = new XtremandResponse();
		Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
		List<TeamMemberGroup> teamMemberGroups = addAllGroupIdsAndNames(addDefaultOption, companyId);
		response.setStatusCode(200);
		response.setData(teamMemberGroups);
		return response;
	}

	private List<TeamMemberGroup> addAllGroupIdsAndNames(boolean addDefaultOption, Integer companyId) {
		List<TeamMemberGroup> teamMemberGroups = new ArrayList<>();
		if (addDefaultOption) {
			TeamMemberGroup teamMemberGroup = new TeamMemberGroup();
			teamMemberGroup.setId(0);
			teamMemberGroup.setTeamMembersCount(0);
			teamMemberGroup.setName("--Please Select--");
			teamMemberGroups.add(teamMemberGroup);
		}
		teamMemberGroups.addAll(teamMemberGroupDao.findGroupIdsAndNamesByCompanyId(companyId));
		return teamMemberGroups;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public String addTeamMemberGroups() {
		List<AddTeamMemberGroup> list = teamMemberGroupDao.addTeamMemberGroupsToCompanies();
		for (AddTeamMemberGroup addTeamMemberGroup : list) {
			TeamMemberGroup teamMemberGroup = insertIntoTeamMemberGroup(addTeamMemberGroup);
			insertIntoTeamMemberGroupUserMapping(addTeamMemberGroup, teamMemberGroup);
		}
		return "Added Successfully";
	}

	private TeamMemberGroup insertIntoTeamMemberGroup(AddTeamMemberGroup addTeamMemberGroup) {
		TeamMemberGroup teamMemberGroup = addTeamMemberGroupData(addTeamMemberGroup);
		addTeamMemberGroupRoleMappingData(addTeamMemberGroup, teamMemberGroup);
		genericDao.save(teamMemberGroup);
		return teamMemberGroup;
	}

	private void insertIntoTeamMemberGroupUserMapping(AddTeamMemberGroup addTeamMemberGroup,
			TeamMemberGroup teamMemberGroup) {
		TeamMember teamMember = new TeamMember();
		teamMember.setId(addTeamMemberGroup.getTeamMemberId());
		TeamMemberGroupUserMapping teamMemberGroupUserMapping = new TeamMemberGroupUserMapping();
		teamMemberGroupUserMapping.setTeamMember(teamMember);
		teamMemberGroupUserMapping.setTeamMemberGroup(teamMemberGroup);
		teamMemberGroupUserMapping.setCreatedTime(new Date());
		teamMemberGroupUserMapping.setUpdatedTime(new Date());
		teamMemberGroupUserMapping.setCreatedUserId(addTeamMemberGroup.getOrgAdminId());
		teamMemberGroupUserMapping.setUpdatedUserId(addTeamMemberGroup.getOrgAdminId());
		genericDao.save(teamMemberGroupUserMapping);
	}

	private TeamMemberGroup addTeamMemberGroupData(AddTeamMemberGroup addTeamMemberGroup) {
		TeamMemberGroup teamMemberGroup = new TeamMemberGroup();
		teamMemberGroup.setName(addTeamMemberGroup.getCompanyProfileName() + "-" + addTeamMemberGroup.getRowId());
		teamMemberGroup.setCompanyId(addTeamMemberGroup.getCompanyId());
		teamMemberGroup.setCreatedUserId(addTeamMemberGroup.getOrgAdminId());
		teamMemberGroup.setCreatedTime(new Date());
		teamMemberGroup.setUpdatedUserId(addTeamMemberGroup.getOrgAdminId());
		teamMemberGroup.setUpdatedTime(new Date());
		GenerateRandomPassword password = new GenerateRandomPassword();
		teamMemberGroup.setAlias(password.getPassword());
		return teamMemberGroup;
	}

	private void addTeamMemberGroupRoleMappingData(AddTeamMemberGroup addTeamMemberGroup,
			TeamMemberGroup teamMemberGroup) {
		Set<TeamMemberGroupRoleMapping> teamMemberGroupRoleMappings = new HashSet<>();
		for (Integer roleId : addTeamMemberGroup.getRoleIds()) {
			addRoleToTeamMemberGroupRoleMapping(teamMemberGroup, teamMemberGroupRoleMappings, roleId);
		}

		User teamMemberUser = userDao.findByPrimaryKey(addTeamMemberGroup.getTeamMemberUserId(),
				new FindLevel[] { FindLevel.COMPANY_PROFILE, FindLevel.ROLES });

		addDamRole(addTeamMemberGroup, teamMemberGroup, teamMemberGroupRoleMappings, teamMemberUser);

		addMdfRole(addTeamMemberGroup, teamMemberGroup, teamMemberGroupRoleMappings, teamMemberUser);

		addPlaybookRole(addTeamMemberGroup, teamMemberGroup, teamMemberGroupRoleMappings, teamMemberUser);

		addLearningTrackRole(addTeamMemberGroup, teamMemberGroup, teamMemberGroupRoleMappings, teamMemberUser);

		addOpportunitiesRole(addTeamMemberGroup, teamMemberGroup, teamMemberGroupRoleMappings, teamMemberUser);

		addShareLeadsRole(addTeamMemberGroup, teamMemberGroup, teamMemberGroupRoleMappings, teamMemberUser);

		teamMemberGroup.setTeamMemberGroupRoleMappings(teamMemberGroupRoleMappings);
	}

	private void addShareLeadsRole(AddTeamMemberGroup addTeamMemberGroup, TeamMemberGroup teamMemberGroup,
			Set<TeamMemberGroupRoleMapping> teamMemberGroupRoleMappings, User teamMemberUser) {
		boolean hasShareLeads = utilDao.hasShareLeadsAccessByCompanyId(addTeamMemberGroup.getCompanyId());
		if (hasShareLeads && addTeamMemberGroup.getRoleIds().indexOf(Role.ALL_ROLES.getRoleId()) > -1) {
			addRoleToTeamMemberGroupRoleMapping(teamMemberGroup, teamMemberGroupRoleMappings,
					Role.SHARE_LEADS.getRoleId());
			teamMemberUser.getRoles().add(Role.SHARE_LEADS);
		}
	}

	private void addOpportunitiesRole(AddTeamMemberGroup addTeamMemberGroup, TeamMemberGroup teamMemberGroup,
			Set<TeamMemberGroupRoleMapping> teamMemberGroupRoleMappings, User teamMemberUser) {
		boolean hasEnableLeads = utilDao.hasEnableLeadsAccessByCompanyId(addTeamMemberGroup.getCompanyId());
		boolean hasEnableLeadsAccessAsPartner = utilDao.enableLeadsForPartner(addTeamMemberGroup.getTeamMemberUserId());
		if ((hasEnableLeads || hasEnableLeadsAccessAsPartner)
				&& addTeamMemberGroup.getRoleIds().indexOf(Role.ALL_ROLES.getRoleId()) > -1) {
			addRoleToTeamMemberGroupRoleMapping(teamMemberGroup, teamMemberGroupRoleMappings,
					Role.OPPORTUNITY.getRoleId());
			teamMemberUser.getRoles().add(Role.OPPORTUNITY);
		}
	}

	private void addLearningTrackRole(AddTeamMemberGroup addTeamMemberGroup, TeamMemberGroup teamMemberGroup,
			Set<TeamMemberGroupRoleMapping> teamMemberGroupRoleMappings, User teamMemberUser) {
		boolean hasLearningTrackAccess = utilDao.hasLmsAccessByCompanyId(addTeamMemberGroup.getCompanyId());

		boolean hasLearningTrackAccessAsPartner = utilDao.lmsAccessForPartner(addTeamMemberGroup.getTeamMemberUserId());

		if ((hasLearningTrackAccess || hasLearningTrackAccessAsPartner)
				&& addTeamMemberGroup.getRoleIds().indexOf(Role.ALL_ROLES.getRoleId()) > -1) {
			addRoleToTeamMemberGroupRoleMapping(teamMemberGroup, teamMemberGroupRoleMappings,
					Role.LEARNING_TRACK.getRoleId());
			teamMemberUser.getRoles().add(Role.LEARNING_TRACK);
		}
	}

	private void addPlaybookRole(AddTeamMemberGroup addTeamMemberGroup, TeamMemberGroup teamMemberGroup,
			Set<TeamMemberGroupRoleMapping> teamMemberGroupRoleMappings, User teamMemberUser) {
		boolean playbookAccess = utilDao.hasPlaybookAccessByCompanyId(addTeamMemberGroup.getCompanyId());

		boolean playbookAccessAsPartner = utilDao.playbookAccessForPartner(addTeamMemberGroup.getTeamMemberUserId());

		if ((playbookAccess || playbookAccessAsPartner)
				&& addTeamMemberGroup.getRoleIds().indexOf(Role.ALL_ROLES.getRoleId()) > -1) {
			addRoleToTeamMemberGroupRoleMapping(teamMemberGroup, teamMemberGroupRoleMappings,
					Role.PLAY_BOOK.getRoleId());
			teamMemberUser.getRoles().add(Role.PLAY_BOOK);
		}
	}

	private void addMdfRole(AddTeamMemberGroup addTeamMemberGroup, TeamMemberGroup teamMemberGroup,
			Set<TeamMemberGroupRoleMapping> teamMemberGroupRoleMappings, User teamMemberUser) {
		boolean hasMdfAccess = utilDao.hasMdfAccessByCompanyId(addTeamMemberGroup.getCompanyId());
		boolean hasMdfAccessAsPartner = utilDao.mdfAccessForPartner(addTeamMemberGroup.getTeamMemberUserId());
		if ((hasMdfAccess || hasMdfAccessAsPartner)
				&& addTeamMemberGroup.getRoleIds().indexOf(Role.ALL_ROLES.getRoleId()) > -1) {
			addRoleToTeamMemberGroupRoleMapping(teamMemberGroup, teamMemberGroupRoleMappings, Role.MDF.getRoleId());
			teamMemberUser.getRoles().add(Role.MDF);
		}
	}

	private void addDamRole(AddTeamMemberGroup addTeamMemberGroup, TeamMemberGroup teamMemberGroup,
			Set<TeamMemberGroupRoleMapping> teamMemberGroupRoleMappings, User teamMemberUser) {
		boolean damAccess = utilDao.hasDamAccessByCompanyId(addTeamMemberGroup.getCompanyId());
		boolean damAccessAsPartner = utilDao.damAccessForPartner(addTeamMemberGroup.getTeamMemberUserId());
		if ((damAccess || damAccessAsPartner)
				&& addTeamMemberGroup.getRoleIds().indexOf(Role.ALL_ROLES.getRoleId()) > -1) {
			addRoleToTeamMemberGroupRoleMapping(teamMemberGroup, teamMemberGroupRoleMappings, Role.DAM.getRoleId());
			teamMemberUser.getRoles().add(Role.DAM);
		}
	}

	private void addRoleToTeamMemberGroupRoleMapping(TeamMemberGroup teamMemberGroup,
			Set<TeamMemberGroupRoleMapping> teamMemberGroupRoleMappings, Integer roleId) {
		TeamMemberGroupRoleMapping teamMemberGroupRoleMapping = new TeamMemberGroupRoleMapping();
		teamMemberGroupRoleMapping.setRoleId(roleId);
		teamMemberGroupRoleMapping.setTeamMemberGroup(teamMemberGroup);
		teamMemberGroupRoleMapping.setCreatedTime(new Date());
		teamMemberGroupRoleMappings.add(teamMemberGroupRoleMapping);
	}

	@Override
	public XtremandResponse hasSuperVisorRole(Integer teamMemberGroupId) {
		XtremandResponse response = new XtremandResponse();
		response.setData(teamMemberGroupDao.hasSuperVisorRole(teamMemberGroupId));
		return response;
	}

	@Override
	public XtremandResponse previewGroupDetailsById(Integer id) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(200);
		TeamMemberGroupDTO teamMemberGroupDTO = teamMemberGroupDao.previewGroupById(id);
		Integer companyId = teamMemberGroupDTO.getCompanyId();
		String partnerModuleCustomName = utilService.findPartnerModuleCustomNameByCompanyId(companyId);
		utilService.addModules(teamMemberGroupDTO, partnerModuleCustomName);
		if (teamMemberGroupDTO.isMarketingModulesAccessToTeamMemberGroup()) {
			List<TeamMemberModuleDTO> marketingModuleDTOs = new ArrayList<>();
			teamMemberGroupDTO.setMarketingModuleDTOs(marketingModuleDTOs);
		}
		response.setData(teamMemberGroupDTO);
		return response;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse addDefaultGroups(String defaultGroupName) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(200);
		List<CompanyAndRolesDTO> dtos = teamDao.findCompanyDetailsAndRoles();
		for (CompanyAndRolesDTO companyAndRolesDTO : dtos) {
			Integer companyId = companyAndRolesDTO.getCompanyId();
			String name = mapDefaultGroupName(defaultGroupName);
			List<Integer> companyIds = teamMemberGroupDao.findDefaultGroupCompanyIds(name);
			if (companyIds.indexOf(companyId) < 0) {
				List<Integer> userRoleIds = companyAndRolesDTO.getRoleIds();
				boolean partner = userRoleIds.indexOf(Role.COMPANY_PARTNER.getRoleId()) > -1;
				boolean onlyPartner = userRoleIds.size() == 1 && partner;
				boolean prm = userRoleIds.indexOf(Role.PRM_ROLE.getRoleId()) > -1;
				VanityUrlDetailsDTO vanityUrlDetailsDTO = new VanityUrlDetailsDTO();
				vanityUrlDetailsDTO.setUserId(companyAndRolesDTO.getUserId());
				XtremandResponse teamMemberGroupResponse = findDefaultModules(vanityUrlDetailsDTO);
				extracted(defaultGroupName, companyId, onlyPartner, prm, teamMemberGroupResponse);
			}

		}
		response.setMessage(defaultGroupName + "Added Successfully");
		return response;
	}

	@SuppressWarnings("unchecked")
	private void extracted(String defaultGroupName, Integer companyId, boolean onlyPartner, boolean prm,
			XtremandResponse teamMemberGroupResponse) {
		Map<String, Object> map = (Map<String, Object>) teamMemberGroupResponse.getData();
		List<TeamMemberModuleDTO> modules = (List<TeamMemberModuleDTO>) map.get("modules");
		boolean partnerManager = onlyPartner;
		if ("PAM".equals(defaultGroupName) && partnerManager) {
			setPAM(companyId, modules);
		} else if ("PRMAM".equals(defaultGroupName) && prm) {
			setPRMAM(companyId, modules);
		}
	}

	private String mapDefaultGroupName(String defaultGroupName) {
		String name = "";
		if ("PAM".equals(defaultGroupName)) {
			name = PARTNER_ACCOUNT_MANAGER;
		} else if ("PRMAM".equals(defaultGroupName)) {
			name = PRM_ACCOUNT_MANAGER;
		}
		return name;
	}

	private void setPRMAM(Integer companyId, List<TeamMemberModuleDTO> modules) {
		List<Integer> moduleIds = modules.stream().map(TeamMemberModuleDTO::getRoleId).collect(Collectors.toList());
		Set<Integer> prmManagerRoleIds = new HashSet<>();
		filterPRMManagerRoles(moduleIds, prmManagerRoleIds);
		setChannelManagerDTO(companyId, prmManagerRoleIds, PRM_ACCOUNT_MANAGER);
	}

	private void setPAM(Integer companyId, List<TeamMemberModuleDTO> modules) {
		Set<Integer> moduleIds = modules.stream().map(TeamMemberModuleDTO::getRoleId).collect(Collectors.toSet());
		setChannelManagerDTO(companyId, moduleIds, PARTNER_ACCOUNT_MANAGER);
	}

	private void filterPRMManagerRoles(List<Integer> moduleIds, Set<Integer> prmManagerRoleIds) {
		if (moduleIds.indexOf(Role.PARTNERS.getRoleId()) > -1) {
			prmManagerRoleIds.add(Role.PARTNERS.getRoleId());
		}

		if (moduleIds.indexOf(Role.STATS_ROLE.getRoleId()) > -1) {
			prmManagerRoleIds.add(Role.STATS_ROLE.getRoleId());
		}

		if (moduleIds.indexOf(Role.OPPORTUNITY.getRoleId()) > -1) {
			prmManagerRoleIds.add(Role.OPPORTUNITY.getRoleId());
		}

		if (moduleIds.indexOf(Role.MDF.getRoleId()) > -1) {
			prmManagerRoleIds.add(Role.MDF.getRoleId());
		}

		if (moduleIds.indexOf(Role.DAM.getRoleId()) > -1) {
			prmManagerRoleIds.add(Role.DAM.getRoleId());
		}

		if (moduleIds.indexOf(Role.LEARNING_TRACK.getRoleId()) > -1) {
			prmManagerRoleIds.add(Role.LEARNING_TRACK.getRoleId());
		}

		if (moduleIds.indexOf(Role.PLAY_BOOK.getRoleId()) > -1) {
			prmManagerRoleIds.add(Role.PLAY_BOOK.getRoleId());
		}

	}

	private void setChannelManagerDTO(Integer companyId, Set<Integer> moduleIds, String groupName) {
		TeamMemberGroupDTO teamMemberGroupDTO = new TeamMemberGroupDTO();
		teamMemberGroupDTO.setUserId(1);
		teamMemberGroupDTO.setName(groupName);
		teamMemberGroupDTO.setRoleIds(moduleIds);
		teamMemberGroupDTO.setDefaultGroup(true);
		teamMemberGroupDTO.setCompanyIdFromApi(companyId);
		save(teamMemberGroupDTO);
	}

	@Override
	public void addDefaultGroups(Integer roleId, Integer companyId, ModuleAccessDTO moduleAccessDTO) {
		if (Role.PRM_ROLE.getRoleId().equals(roleId)) {
			addPRMAccountManagerDefaultGroup(moduleAccessDTO, companyId);
		}

	}

	private void addPRMAccountManagerDefaultGroup(ModuleAccessDTO moduleAccessDTO, Integer companyId) {
		String name = PRM_ACCOUNT_MANAGER;
		List<Integer> prmAccountManagerRoleIds = new ArrayList<>();
		prmAccountManagerRoleIds.add(Role.PARTNERS.getRoleId());
		prmAccountManagerRoleIds.add(Role.STATS_ROLE.getRoleId());

		if (moduleAccessDTO.isLeads()) {
			prmAccountManagerRoleIds.add(Role.OPPORTUNITY.getRoleId());
		}

		if (moduleAccessDTO.isMdf()) {
			prmAccountManagerRoleIds.add(Role.MDF.getRoleId());
		}

		if (moduleAccessDTO.isDam()) {
			prmAccountManagerRoleIds.add(Role.DAM.getRoleId());
		}

		if (moduleAccessDTO.isLms()) {
			prmAccountManagerRoleIds.add(Role.LEARNING_TRACK.getRoleId());
		}

		if (moduleAccessDTO.isPlaybooks()) {
			prmAccountManagerRoleIds.add(Role.PLAY_BOOK.getRoleId());
		}

		moduleAccessDTO.setTeamMemberGroupRoleIds(prmAccountManagerRoleIds);
		saveTeamMemberGroup(name, prmAccountManagerRoleIds, companyId, moduleAccessDTO);
	}

	private void saveTeamMemberGroup(String name, List<Integer> channelAccountManagerRoleIds, Integer companyId,
			ModuleAccessDTO moduleAccessDTO) {
		TeamMemberGroup teamMemberGroup = new TeamMemberGroup();
		teamMemberGroup.setName(name);
		teamMemberGroup.setCreatedUserId(1);
		teamMemberGroup.setUpdatedUserId(1);
		teamMemberGroup.setCompanyId(companyId);
		teamMemberGroup.setCreatedTime(new Date());
		teamMemberGroup.setUpdatedTime(new Date());
		teamMemberGroup.setDefaultGroup(true);
		GenerateRandomPassword password = new GenerateRandomPassword();
		teamMemberGroup.setAlias(password.getPassword());
		Set<TeamMemberGroupRoleMapping> teamMemberGroupRoleMappings = new HashSet<>();
		for (Integer moduleId : channelAccountManagerRoleIds) {
			TeamMemberGroupRoleMapping teamMemberGroupRoleMapping = new TeamMemberGroupRoleMapping();
			teamMemberGroupRoleMapping.setTeamMemberGroup(teamMemberGroup);
			teamMemberGroupRoleMapping.setRoleId(moduleId);
			teamMemberGroupRoleMapping.setCreatedTime(new Date());
			teamMemberGroupRoleMappings.add(teamMemberGroupRoleMapping);
		}
		teamMemberGroup.setTeamMemberGroupRoleMappings(teamMemberGroupRoleMappings);
		genericDao.save(teamMemberGroup);
		moduleAccessDTO.setTeamMemberGroupId(teamMemberGroup.getId());
	}

	@Override
	public List<TeamMemberGroup> findAllGroupIdsAndNamesByCompanyId(Integer companyId, boolean addDefaultOption) {
		return addAllGroupIdsAndNames(addDefaultOption, companyId);
	}

	@Override
	public XtremandResponse findSelectedTeamMemberIdsByPartnershipId(Integer partnershipId) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(200);
		response.setData(teamMemberGroupDao.findSelectedTeamMemberGroupUserMappingIdsByPartnershipId(partnershipId));
		return response;
	}

	@Override
	public XtremandResponse findPartnersCountByTeamMemberGroupId(Integer teamMemberGroupId) {
		XtremandResponse response = new XtremandResponse();
		List<Integer> partnershipIds = teamMemberGroupDao
				.findAssociatedPartnershipIdsByTeamMemberGroupId(teamMemberGroupId);
		if (partnershipIds != null) {
			response.setData(partnershipIds.size());
		} else {
			response.setData(0);
		}
		response.setStatusCode(200);
		return response;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void findAndDeleteTeamMemberGroupUserMappingsByCompanyId(Integer companyId) {
		List<Integer> ids = teamMemberGroupDao.findIdsByCompanyId(companyId);
		if (XamplifyUtils.isNotEmptyList(ids)) {
			teamMemberGroupDao.deleteFromTeamMemberGroupUserMapping(ids);
		}
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void findAndDeleteTeamMemberGroupRoleMappingsByCompanyId(Integer companyId) {
		List<Integer> ids = teamMemberGroupDao.findIdsByCompanyId(companyId);
		if (XamplifyUtils.isNotEmptyList(ids)) {
			teamMemberGroupDao.deleteTeamMemberGroupRoleMappings(ids);
		}

	}

	@Override
	public void findAndDeleteAllTeamMemberGroupsByCompanyId(Integer companyId) {
		teamMemberGroupDao.deleteByCompanyId(companyId);

	}

	@Override
	public void upgradeTeamMemberGroups(Integer roleId, Integer companyId, ModuleAccessDTO moduleAccessDTO) {
		findAndDeleteTeamMemberGroupUserMappingsByCompanyId(companyId);
		findAndDeleteTeamMemberGroupRoleMappingsByCompanyId(companyId);
		findAndDeleteAllTeamMemberGroupsByCompanyId(companyId);
		Integer defaultTeamMemberGroupId = 0;
		Set<Integer> teamMemberGroupRoleIds = new HashSet<>();
		if (Role.PRM_ROLE.getRoleId().equals(roleId)) {
			addPRMAccountManagerDefaultGroup(moduleAccessDTO, companyId);
			defaultTeamMemberGroupId = moduleAccessDTO.getTeamMemberGroupId();
			teamMemberGroupRoleIds.addAll(moduleAccessDTO.getTeamMemberGroupRoleIds());
		}
		List<TeamMember> teamMembers = teamDao.findAllByCompanyId(companyId);
		if (teamMembers != null && !teamMembers.isEmpty()) {
			List<Integer> teamMemberUserIds = teamMembers.stream().map(TeamMember::getTeamMember).map(User::getUserId)
					.collect(Collectors.toList());
			if (teamMemberUserIds != null && !teamMemberUserIds.isEmpty()) {
				userDao.deleteAllTeamMemberRoles(teamMemberUserIds);
			}

			for (TeamMember teamMember : teamMembers) {
				Integer teamMemberUserId = teamMember.getTeamMember().getUserId();
				teamMember.setSecondAdmin(false);
				teamMember.setUpdatedTime(new Date());
				TeamMemberGroupUserMapping teamMemberGroupUserMapping = new TeamMemberGroupUserMapping();
				TeamMemberGroup teamMemberGroup = new TeamMemberGroup();
				teamMemberGroup.setId(defaultTeamMemberGroupId);
				teamMemberGroupUserMapping.setTeamMemberGroup(teamMemberGroup);
				teamMemberGroupUserMapping.setTeamMember(teamMember);
				teamMemberGroupUserMapping.setCreatedTime(new Date());
				teamMemberGroupUserMapping.setCreatedUserId(teamMemberUserId);
				teamMemberGroupUserMapping.setUpdatedTime(new Date());
				teamMemberGroupUserMapping.setUpdatedUserId(teamMemberUserId);
				genericDao.save(teamMemberGroupUserMapping);
				teamDao.addNewRoles(teamMemberUserId, teamMemberGroupRoleIds);

			}

		}

	}

	@Override
	public Map<String, Object> getRoleIdsAndRoleNamesByUserId(Integer userId) {
		TeamMemberGroupDTO teamMemberGroupDTO = new TeamMemberGroupDTO();
		Set<Integer> distinctRoleIds = XamplifyUtils.convertListToSetElements(userDao.getRoleIdsByUserId(userId));
		teamMemberGroupDTO.setRoleIds(distinctRoleIds);
		Map<String, Object> map = new HashMap<>();
		List<TeamMemberModuleDTO> modules = new ArrayList<>();
		List<Integer> roleIdsArrayList = XamplifyUtils.convertSetToList(teamMemberGroupDTO.getRoleIds());
		addModules(userId, modules, map, roleIdsArrayList);
		return map;

	}

	/** XNFR-821 **/
	private void updateApprovalAuthorities(List<Integer> roleIdsToUpdate, Set<Integer> existingRoleIds,
			TeamMemberGroupDTO teamMemberGroupDto, List<Integer> teamMemberIds, Integer companyId) {
		if (XamplifyUtils.isValidInteger(companyId) && XamplifyUtils.isNotEmptyList(roleIdsToUpdate)
				&& XamplifyUtils.isNotEmptyList(teamMemberIds) && XamplifyUtils.isNotEmptySet(existingRoleIds)) {

			TeamMemberApprovalPrivilegesDTO teamMemberApprovalPrivilegesDTO = new TeamMemberApprovalPrivilegesDTO();

			boolean previouslyHadAllRole = existingRoleIds.contains(Role.ALL_ROLES.getRoleId());
			boolean currentlyHasAllRole = roleIdsToUpdate.contains(Role.ALL_ROLES.getRoleId());

			if (!previouslyHadAllRole && currentlyHasAllRole) {
				setAllApprovalPrivileges(teamMemberApprovalPrivilegesDTO, true);
				teamMemberGroupDto.setApprovalManager(true);
			} else if (previouslyHadAllRole && !currentlyHasAllRole) {
				setAllApprovalPrivileges(teamMemberApprovalPrivilegesDTO, false);
			} else {
				revokeRoleSpecificApprovals(roleIdsToUpdate, existingRoleIds, teamMemberApprovalPrivilegesDTO);
			}

			if (teamMemberApprovalPrivilegesDTO.isAssetApproverFieldUpdated()
					|| teamMemberApprovalPrivilegesDTO.isTrackApproverFieldUpdated()
					|| teamMemberApprovalPrivilegesDTO.isPlaybookApproverFieldUpdated()) {
				approveDao.updateApprovalConfigurationSettingsForTeamMembers(teamMemberApprovalPrivilegesDTO, companyId,
						teamMemberIds);
			}
		}
	}

	private void revokeRoleSpecificApprovals(List<Integer> roleIdsToUpdate, Set<Integer> existingRoleIds,
			TeamMemberApprovalPrivilegesDTO teamMemberApprovalPrivilegesDTO) {
		if (existingRoleIds.contains(Role.DAM.getRoleId()) && !roleIdsToUpdate.contains(Role.DAM.getRoleId())) {
			teamMemberApprovalPrivilegesDTO.setAssetApprover(false);
			teamMemberApprovalPrivilegesDTO.setAssetApproverFieldUpdated(true);
		}
		if (existingRoleIds.contains(Role.LEARNING_TRACK.getRoleId())
				&& !roleIdsToUpdate.contains(Role.LEARNING_TRACK.getRoleId())) {
			teamMemberApprovalPrivilegesDTO.setTrackApprover(false);
			teamMemberApprovalPrivilegesDTO.setTrackApproverFieldUpdated(true);
		}
		if (existingRoleIds.contains(Role.PLAY_BOOK.getRoleId())
				&& !roleIdsToUpdate.contains(Role.PLAY_BOOK.getRoleId())) {
			teamMemberApprovalPrivilegesDTO.setPlaybookApprover(false);
			teamMemberApprovalPrivilegesDTO.setPlaybookApproverFieldUpdated(true);
		}
	}

	private void setAllApprovalPrivileges(TeamMemberApprovalPrivilegesDTO teamMemberApprovalPrivilegesDTO,
			boolean access) {
		teamMemberApprovalPrivilegesDTO.setAssetApprover(access);
		teamMemberApprovalPrivilegesDTO.setTrackApprover(access);
		teamMemberApprovalPrivilegesDTO.setPlaybookApprover(access);
		teamMemberApprovalPrivilegesDTO.setAssetApproverFieldUpdated(true);
		teamMemberApprovalPrivilegesDTO.setTrackApproverFieldUpdated(true);
		teamMemberApprovalPrivilegesDTO.setPlaybookApproverFieldUpdated(true);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> autoApprovePendingContent(Integer loggedInUserId, List<Integer> createdByIds,
			boolean isApprovalManager, Integer companyId) {
		Map<String, Object> resultMap = new HashMap<>();

		if (isApprovalManager && XamplifyUtils.isValidInteger(loggedInUserId)
				&& XamplifyUtils.isNotEmptyList(createdByIds)) {

			List<ApprovalStatusHistory> approvalHistoryList = approveService
					.processAndSaveApprovalTimelineHistory(createdByIds, loggedInUserId);
			approveDao.autoApprovePendingAssets(loggedInUserId, companyId, createdByIds);
			approveDao.autoApprovePendingLMS(createdByIds, companyId, loggedInUserId, null);

			List<DamTag> allDamTagsToSave = new ArrayList<>();
			List<Integer> reApprovalVersionDamIds = approveDao.getReApprovalVersionDamIdsByUserIds(createdByIds,
					companyId);

			if (XamplifyUtils.isNotEmptyList(reApprovalVersionDamIds)) {
				List<ContentReApprovalDTO> contentReApprovalDTOs = damDao
						.getAssetDetailsForReApproval(reApprovalVersionDamIds);
				List<Integer> whiteLabeledReApprovalDamIds = contentReApprovalDTOs.stream()
						.filter(ContentReApprovalDTO::isWhiteLabeledAssetSharedWithPartners)
						.map(ContentReApprovalDTO::getApprovalReferenceId).collect(Collectors.toList());
				resultMap.put(WHITE_LABELED_RE_APPROVAL_DAM_IDS, whiteLabeledReApprovalDamIds);

				contentReApprovalDTOs.forEach(dto -> dto.setLoggedInUserId(loggedInUserId));
				List<ContentReApprovalDTO> videoTypeAssetContentDetails = contentReApprovalDTOs.stream()
						.filter(dto -> dto.getVideoId() != null).collect(Collectors.toList());

				List<ContentReApprovalDTO> nonVideoTypeAssetContentDetails = contentReApprovalDTOs.stream()
						.filter(dto -> dto.getVideoId() == null).collect(Collectors.toList());

				List<ContentReApprovalDTO> pdfTypeAssetContentDetails = contentReApprovalDTOs.stream()
						.filter(dto -> "pdf".equalsIgnoreCase(dto.getAssetType())).collect(Collectors.toList());

				List<Integer> damIdsToDelete = approveService.processNonVideoAssetReApprovalAndGetIds(loggedInUserId,
						allDamTagsToSave, nonVideoTypeAssetContentDetails, approvalHistoryList, "");
				resultMap.put("damIdsToDelete", damIdsToDelete);
				resultMap.put("allDamTagsToSave", allDamTagsToSave);

				Map<String, Object> videoMap = approveService.handleReApprovalVersionForVideoTypeAsset(loggedInUserId,
						videoTypeAssetContentDetails, approvalHistoryList, "");
				List<VideoTag> videoTagsToSave = (List<VideoTag>) videoMap.get(VIDEO_TAGS_TO_SAVE_MAP_KEY);
				List<Integer> videoIdsToDelete = (List<Integer>) videoMap.get(VIDEO_IDS_TO_DELETE_MAP_KEY);
				resultMap.put(VIDEO_TAGS_TO_SAVE_MAP_KEY, videoTagsToSave);
				resultMap.put(VIDEO_IDS_TO_DELETE_MAP_KEY, videoIdsToDelete);
				resultMap.put("pdfTypeAssetContentDetails", pdfTypeAssetContentDetails);
			}
			resultMap.put(APPROVAL_HISTORY_LIST_MAP_KEY, approvalHistoryList);
		}
		return resultMap;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse updateDefaultSSOGroup(Integer id) {
		XtremandResponse response = new XtremandResponse();
		teamMemberGroupDao.updateDefaultSSOGroupById(id);
		XamplifyUtils.addSuccessStatusWithMessage(response, "The default SSO group has been updated successfully");
		return response;
	}

	@Override
	public XtremandResponse findAllGroupIdsAndNamesWithDefaultSSOFirst(String companyProfileName,
			Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		List<TeamMemberGroup> teamMemberGroups = new ArrayList<>();
		VanityUrlDetailsDTO vanityUrlDetailsDTO = new VanityUrlDetailsDTO();
		vanityUrlDetailsDTO.setUserId(loggedInUserId);
		vanityUrlDetailsDTO.setVanityUrlFilter(true);
		vanityUrlDetailsDTO.setVendorCompanyProfileName(companyProfileName);
		utilService.isVanityUrlFilterApplicable(vanityUrlDetailsDTO);
		Integer loggedInUserCompanyId = vanityUrlDetailsDTO.getLoggedInUserCompanyId();

		TeamMemberGroup teamMemberGroup = new TeamMemberGroup();
		teamMemberGroup.setId(0);
		teamMemberGroup.setTeamMembersCount(0);
		teamMemberGroup.setName("--Please Select--");
		teamMemberGroups.add(teamMemberGroup);
		teamMemberGroups.addAll(teamMemberGroupDao.findGroupIdsAndNamesByCompanyId(loggedInUserCompanyId));

		response.setStatusCode(200);
		response.setData(teamMemberGroups);
		return response;
	}

	@Override
	public XtremandResponse findPaginatedTeamMemberGroupSignUpUrls(Integer loggedInUserId, String domainName,
			boolean isVanityLogin, Pageable pageable) {
		XtremandResponse response = new XtremandResponse();
		Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
		Pagination pagination = utilService.setPageableParameters(pageable, loggedInUserId);
		pagination.setCompanyId(companyId);

		Map<String, Object> map = teamMemberGroupDao.findPaginatedTeamMemberGroupSignUpUrls(pagination);

		VanityUrlDetailsDTO vanityUrlDetailsDTO = new VanityUrlDetailsDTO();
		vanityUrlDetailsDTO.setUserId(loggedInUserId);
		vanityUrlDetailsDTO.setVanityUrlFilter(isVanityLogin);
		vanityUrlDetailsDTO.setVendorCompanyProfileName(domainName);
		String signUpUrlPrefix = findTeamMemberGroupSignupUrl(vanityUrlDetailsDTO);

		@SuppressWarnings("unchecked")
		List<TeamMemberGroup> teamMemberGroups = (List<TeamMemberGroup>) map.get("list");
		teamMemberGroups.forEach(group -> group.setSignUpUrl(signUpUrlPrefix + group.getAlias()));

		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("list", teamMemberGroups);
		resultMap.put("totalRecords", map.get("totalRecords"));

		response.setData(resultMap);
		XamplifyUtils.addSuccessStatus(response);
		return response;
	}

}
