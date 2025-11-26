package com.xtremand.module.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jcodec.common.logging.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.campaign.bom.ModuleAccess;
import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.Criteria;
import com.xtremand.common.bom.Criteria.OPERATION_NAME;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.common.bom.ModuleCustom;
import com.xtremand.company.service.CompanyProfileService;
import com.xtremand.dam.dao.DamDao;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.formbeans.RoleDTO;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.lms.bom.LearningTrackType;
import com.xtremand.lms.dao.LMSDAO;
import com.xtremand.module.dao.ModuleDao;
import com.xtremand.module.service.ModuleService;
import com.xtremand.partnership.bom.Partnership;
import com.xtremand.partnership.dao.PartnershipDAO;
import com.xtremand.partnership.dto.LoginAsPartnerDTO;
import com.xtremand.team.dao.TeamDao;
import com.xtremand.team.member.dto.RoleDisplayDTO;
import com.xtremand.user.bom.Role;
import com.xtremand.user.bom.User;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.user.service.UserService;
import com.xtremand.userlist.dao.UserListDAO;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.bom.Module;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.dao.XamplifyUtilDao;
import com.xtremand.util.dto.DeletedPartnerDTO;
import com.xtremand.util.dto.LeftSideNavigationBarItem;
import com.xtremand.util.dto.ModuleCustomDTO;
import com.xtremand.util.dto.ModuleCustomRequestDTO;
import com.xtremand.util.dto.WelcomePageItem;
import com.xtremand.util.service.UtilService;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;

@Service
@Transactional
public class ModuleServiceImpl implements ModuleService {

	@Autowired
	private ModuleDao moduleDao;

	@Autowired
	private UtilService utilService;

	@Autowired
	private UserService userService;

	@Autowired
	private UserDAO userDAO;

	@Autowired
	private UtilDao utilDao;

	@Autowired
	private PartnershipDAO partnershipDAO;

	@Value("${role.user}")
	private String userRole;

	@Autowired
	private DamDao damDao;

	@Autowired
	private LMSDAO lmsDao;

	@Autowired
	private UserListDAO userListDAO;

	@Autowired
	private GenericDAO genericDao;

	@Autowired
	private TeamDao teamDao;

	@Autowired
	private XamplifyUtilDao xamplifyUtilDao;

	@Autowired
	private CompanyProfileService companyProfileService;

	@Value("${xAmplify.pat}")
	private String pat;

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ModuleServiceImpl.class);

	@Override
	public XtremandResponse getAvailableModules(Integer userId, String companyProfileName) {
		XtremandResponse response = new XtremandResponse();
		Module module = new Module();
		List<RoleDTO> roleDtos = moduleDao.getRoleDetailsByUserId(userId);
		if (!roleDtos.isEmpty()) {
			Integer companyId = roleDtos.stream().map(RoleDTO::getTotalRoles).collect(Collectors.toList()).get(0);
			boolean prmRole = utilDao.isPrmCompany(userId);
			ModuleAccess moduleAccess = utilDao.getModuleAccess(companyId);
			setFormModule(module, prmRole, moduleAccess);
			response.setStatusCode(200);
		} else {
			response.setStatusCode(0);
		}
		response.setData(module);
		return response;
	}

	private void setFormModule(Module module, boolean prmRole, ModuleAccess moduleAccess) {
		if (moduleAccess != null) {
			if (prmRole) {
				module.setForm(true);
			} else {
				module.setForm(moduleAccess.isForm());
			}
		}
	}

	@Override
	public LeftSideNavigationBarItem findLeftMenuItems(VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		LeftSideNavigationBarItem leftSideNavigationBarItem = new LeftSideNavigationBarItem();
		Integer userId = vanityUrlDetailsDTO.getUserId();
		leftSideNavigationBarItem.setUserId(userId);
		RoleDisplayDTO roleDisplayDto = utilService.getRoleDetailsByUserId(userId);
		utilService.isVanityUrlFilterApplicable(vanityUrlDetailsDTO);
		List<Integer> roleIds = roleDisplayDto.getRoleIds();
		boolean isVanityUrlFilter = vanityUrlDetailsDTO.isVanityUrlFilter();
		boolean isPartnershipEstablishedOnlyWithPrm = utilDao.isPartnershipEstablishedOnlyWithPrm(userId);
		leftSideNavigationBarItem.setPartnershipEstablishedOnlyWithPrm(isPartnershipEstablishedOnlyWithPrm);
		boolean isPartnershipEstablishedOnlyWithPrmAndLoggedInAsPartnerOrPartnerTeamMember = isPartnershipEstablishedOnlyWithPrm
				&& (roleDisplayDto.isPartner() || roleDisplayDto.isPartnerTeamMember());
		utilDao.isOnlyPartnerOrPartnerCompany(userId, leftSideNavigationBarItem);
		boolean isTeamMember = teamDao.isTeamMember(userId);
		leftSideNavigationBarItem.setTeamMember(isTeamMember);
		leftSideNavigationBarItem.setLoginAsUserId(vanityUrlDetailsDTO.getLoginAsUserId());
		/******** XNFR-219 **********/
		Integer companyId = userDAO.getCompanyIdByUserId(userId);
		if (companyId != null) {
			leftSideNavigationBarItem.setCompanyId(companyId);
			vanityUrlDetailsDTO.setLoggedInUserCompanyId(companyId);
		} else {
			leftSideNavigationBarItem.setCompanyId(0);
			vanityUrlDetailsDTO.setLoggedInUserCompanyId(0);
		}

		/** XNFR-781 **/
		boolean isApprovalRequiredForAssets = userDAO.checkIfAssetApprovalRequiredByCompanyId(companyId);
		leftSideNavigationBarItem.setApprovalRequiredForAssets(isApprovalRequiredForAssets);

		boolean isApprovalRequiredForTracks = userDAO.checkIfTracksApprovalRequiredByCompanyId(companyId);
		leftSideNavigationBarItem.setApprovalRequiredForTracks(isApprovalRequiredForTracks);

		boolean isApprovalRequiredForPlaybooks = userDAO.checkIfPlaybooksApprovalRequiredByCompanyId(companyId);
		leftSideNavigationBarItem.setApprovalRequiredForPlaybooks(isApprovalRequiredForPlaybooks);

		/**** Account Dashboard ******/
		setAccountDashboard(leftSideNavigationBarItem, roleDisplayDto);

		/******** Team **********/
		leftSideNavigationBarItem.setTeam(roleDisplayDto.hasTeamAccess());

		if (isVanityUrlFilter) {
			setMenuItemsForVanityLogin(vanityUrlDetailsDTO, leftSideNavigationBarItem, userId, roleDisplayDto, roleIds);
		} else {
			setModuleAccessForXamplifyLogin(leftSideNavigationBarItem, roleDisplayDto,
					isPartnershipEstablishedOnlyWithPrm,
					isPartnershipEstablishedOnlyWithPrmAndLoggedInAsPartnerOrPartnerTeamMember);
			leftSideNavigationBarItem.setFolders(true);
			/*** XNFR-698 ***/
			leftSideNavigationBarItem.setMyVendorsOptionDisplayed(leftSideNavigationBarItem.isPartnerCompany());
		}

		/*** XNFR-820 *****/
		setApprovalHubAccessForXamplifyLogin(leftSideNavigationBarItem, userId, roleDisplayDto);

		leftSideNavigationBarItem.setRoleDisplayDto(roleDisplayDto);

		/**** XNFR-84 *******/
		setLoginAsPartnerOption(vanityUrlDetailsDTO, leftSideNavigationBarItem, isVanityUrlFilter, companyId);
		isAdminOrSuperVisor(leftSideNavigationBarItem, roleIds);
		/**** If Only User Role Then Contacts,Campaign,Deals Should be Added **********/
		setDefaultModulesAccessForDeletedPartners(leftSideNavigationBarItem, roleIds, userId);
		boolean admin = Role.isAnyAdmin(roleIds);
		leftSideNavigationBarItem.setAdmin(admin);
		boolean partnerAdmin = Role.isPartnerAdmin(roleIds);
		leftSideNavigationBarItem.setPartnerAdmin(partnerAdmin);

		List<ModuleCustomDTO> menuItems = getCustomizedLeftMenuItems(vanityUrlDetailsDTO, leftSideNavigationBarItem);
		leftSideNavigationBarItem.setMenuItems(menuItems);

		boolean updateModulesFromMyProfile = utilDao.isUpdateModulesFromMyProfileOptionEnabled(companyId);
		if (!isVanityUrlFilter || vanityUrlDetailsDTO.isVendorLoggedInThroughOwnVanityUrl()) {
			leftSideNavigationBarItem.setUpdateModulesFromMyProfile(updateModulesFromMyProfile);
		}

		/** XNFR-878 **/
		boolean allowVendorToChangePartnerPrimaryAdmin = utilDao
				.isAllowVendorToChangeThePartnerAdminOptionEnabledByCompanyId(companyId);
		leftSideNavigationBarItem.setAllowVendorToChangePartnerPrimaryAdmin(allowVendorToChangePartnerPrimaryAdmin);

		leftSideNavigationBarItem.setPat(pat);
		
		return leftSideNavigationBarItem;
	}

	private void setLoginAsPartnerOption(VanityUrlDetailsDTO vanityUrlDetailsDTO,
			LeftSideNavigationBarItem leftSideNavigationBarItem, boolean isVanityUrlFilter, Integer companyId) {
		if (XamplifyUtils.isValidInteger(companyId)) {
			ModuleCustomDTO moduleCustomNameDTO = moduleDao.findPartnerModuleByCompanyId(companyId);
			if (moduleCustomNameDTO != null) {
				leftSideNavigationBarItem.getModuleNames().add(moduleCustomNameDTO);
			}
			ModuleAccess moduleAccess = utilDao.getModuleAccess(companyId);
			leftSideNavigationBarItem.setLoginAs(moduleAccess != null && moduleAccess.isLoginAsTeamMember());
			/***** XNFR-224 *****/
			if (isVanityUrlFilter && vanityUrlDetailsDTO.isVendorLoggedInThroughOwnVanityUrl()) {
				leftSideNavigationBarItem.setLoginAsPartner(moduleAccess != null && moduleAccess.isLoginAsPartner());
			} else if (isVanityUrlFilter && vanityUrlDetailsDTO.isPartnerLoggedInThroughVanityUrl()) {
				Integer vendorCompanyId = vanityUrlDetailsDTO.getVendorCompanyId();
				Integer partnerCompanyId = vanityUrlDetailsDTO.getLoggedInUserCompanyId();
				setLoginAsPartnerOption(leftSideNavigationBarItem, vendorCompanyId, partnerCompanyId);
			}
			/***** XNFR-224 *****/

		}
	}

	private void setLoginAsPartnerOption(LeftSideNavigationBarItem leftSideNavigationBarItem, Integer vendorCompanyId,
			Integer partnerCompanyId) {
		boolean hasLoginAsPartnerAccessByVendorCompanyId = utilDao.hasLoginAsPartnerAccessByCompanyId(vendorCompanyId);
		boolean isLoginAsPartnerOptionEnabledForVendor = utilDao
				.isLoginAsPartnerOptionEnabledForVendorByVendorCompanyIdAndPartnerCompanyId(vendorCompanyId,
						partnerCompanyId);
		boolean loginAsPartnerOptionEnabledForVendor = hasLoginAsPartnerAccessByVendorCompanyId
				&& isLoginAsPartnerOptionEnabledForVendor;
		leftSideNavigationBarItem.setLoginAsPartnerOptionEnabledForVendor(loginAsPartnerOptionEnabledForVendor);
	}

	private void setDefaultModulesAccessForDeletedPartners(LeftSideNavigationBarItem leftSideNavigationBarItem,
			List<Integer> roleIds, Integer userId) {
		DeletedPartnerDTO deletedPartnerDTO = utilDao.getDeletedPartnerDTOByRoleIds(userId, roleIds);
		if (deletedPartnerDTO.isDeletedPartnerCompanyUser()) {
			leftSideNavigationBarItem.setOpportunitiesAccessAsPartner(true);
			leftSideNavigationBarItem.setTeam(true && !leftSideNavigationBarItem.isTeamMember());
			leftSideNavigationBarItem.setAccountDashboard(true);
			leftSideNavigationBarItem.setDeletedPartner(true);
		}
	}

	private void isAdminOrSuperVisor(LeftSideNavigationBarItem leftSideNavigationBarItem, List<Integer> roleIds) {
		boolean isPrm = roleIds.indexOf(Role.PRM_ROLE.getRoleId()) > -1;
		boolean isSuperVisor = roleIds.indexOf(Role.ALL_ROLES.getRoleId()) > -1;
		boolean adminOrSuperVisor = isSuperVisor || isPrm || isSuperVisor;
		leftSideNavigationBarItem.setAdminOrSuperVisor(adminOrSuperVisor);
	}

	private void setMenuItemsForVanityLogin(VanityUrlDetailsDTO vanityUrlDetailsDTO,
			LeftSideNavigationBarItem leftSideNavigationBarItem, Integer userId, RoleDisplayDTO roleDisplayDto,
			List<Integer> roleIds) {
		boolean isLoggedInThroughOwnVanityUrl = vanityUrlDetailsDTO.isVendorLoggedInThroughOwnVanityUrl();
		boolean isLoggedInThroughVendorVanityUrl = vanityUrlDetailsDTO.isPartnerLoggedInThroughVanityUrl();
		leftSideNavigationBarItem.setLoggedInThroughVendorVanityUrl(isLoggedInThroughVendorVanityUrl);
		leftSideNavigationBarItem.setLoggedInThroughOwnVanityUrl(isLoggedInThroughOwnVanityUrl);
		if (isLoggedInThroughOwnVanityUrl) {
			setMenuItemsForOwnVanityLogin(leftSideNavigationBarItem, userId, roleDisplayDto, roleIds);
			/******* XNFR-130 *********/
			boolean isPrmOrPrmAndPartnerCompany = roleDisplayDto.isPrmOrPrmAndPartnerCompany();
			leftSideNavigationBarItem.setPrmDashboard(isPrmOrPrmAndPartnerCompany);
			/*** XNFR-698 ***/
			leftSideNavigationBarItem.setMyVendorsOptionDisplayed(leftSideNavigationBarItem.isPartnerCompany());
		} else if (isLoggedInThroughVendorVanityUrl) {
			setMenuItemsForVendorVanityLogin(vanityUrlDetailsDTO, leftSideNavigationBarItem, roleDisplayDto, userId);
			/******* XNFR-130 *********/
			leftSideNavigationBarItem.setPrmDashboard(
					leftSideNavigationBarItem.isPartnershipEstablishedOnlyWithPrmAndLoggedInAsPartner());
			leftSideNavigationBarItem.setMyVendorsOptionDisplayed(true);
		}

	}

	private void setMenuItemsForVendorVanityLogin(VanityUrlDetailsDTO vanityUrlDetailsDTO,
			LeftSideNavigationBarItem leftSideNavigationBarItem, RoleDisplayDTO roleDisplayDto, Integer userId) {
		Integer vendorCompanyId = vanityUrlDetailsDTO.getVendorCompanyId();

		boolean partnershipEstablishedOnlyWithPrm = utilDao.isPrmByVendorCompanyId(vendorCompanyId);
		leftSideNavigationBarItem
				.setPartnershipEstablishedOnlyWithPrmAndLoggedInAsPartner(partnershipEstablishedOnlyWithPrm);

		leftSideNavigationBarItem.setFolders(true);

		/**** Content (DAM / Learning Tracks / PlayBook) **********/
		setContentMenuForVendorVanityLogin(leftSideNavigationBarItem, vendorCompanyId, roleDisplayDto);

		/**** XNFR-252 ****/
		setSharedLeadsAndStatsAndMdfAndOpportunitesAndChatSupportOptionsForVendorVanityLogin(leftSideNavigationBarItem,
				roleDisplayDto, userId, vendorCompanyId);

		/****** Show Add Leads/Deals Option In The Dashboard ***********/
		boolean showAddLeadsAndDealsOptionInTheDashboard = partnershipEstablishedOnlyWithPrm
				&& leftSideNavigationBarItem.isOpportunitiesAccessAsPartner();
		leftSideNavigationBarItem.setShowAddLeadsAndDealsOptionsInDashboard(showAddLeadsAndDealsOptionInTheDashboard);

		/*** XNFR-979 *****/
		setInsightsAccessForVendorVanityLogin(leftSideNavigationBarItem, vendorCompanyId);

		/** XNFR-1062 ***/
		setMailEnabledAccessForVendorVanityLogin(leftSideNavigationBarItem, vendorCompanyId);

	}

	/************ XNFR-252 ********/
	private void setSharedLeadsAndStatsAndMdfAndOpportunitesAndChatSupportOptionsForVendorVanityLogin(
			LeftSideNavigationBarItem leftSideNavigationBarItem, RoleDisplayDTO roleDisplayDto, Integer userId,
			Integer vendorCompanyId) {
		/**** Shared Leads ***********/
		setSharedLeadsModuleForVendorVanityLogin(leftSideNavigationBarItem, roleDisplayDto, vendorCompanyId, userId);

		/**** Stats *********/
		setStatsForXamplifyLogin(leftSideNavigationBarItem, roleDisplayDto.getRoleIds(), roleDisplayDto.isAnyAdmin());

		/********** MDF ********/
		setMdfMenuForVendorVanityLogin(leftSideNavigationBarItem, vendorCompanyId, roleDisplayDto);

		/******* Opporutnities **********/
		setOpportunitiesModuleForVendorVanityLogin(leftSideNavigationBarItem, vendorCompanyId, roleDisplayDto);

	}

	private void setOpportunitiesModuleForVendorVanityLogin(LeftSideNavigationBarItem leftSideNavigationBarItem,
			Integer vendorCompanyId, RoleDisplayDTO roleDisplayDto) {
		boolean opportuntiesRole = roleDisplayDto.getRoleIds().indexOf(Role.OPPORTUNITY.getRoleId()) > -1;
		boolean opportunitiesAccessAsPartner = false;
		boolean isVendorGivenAccessToPartner = utilDao.fetchModuleAccessForPartnerByModuleIdAndVendorCompanyIdAndUserId(
				vendorCompanyId, 9, leftSideNavigationBarItem.getUserId());
		if (roleDisplayDto.isPartner() || roleDisplayDto.isPartnerTeamMember()) {
			opportunitiesAccessAsPartner = opportuntiesRole || roleDisplayDto.isPartner();
		} else {
			opportunitiesAccessAsPartner = roleDisplayDto.anyAdminAndPartnerRole() || opportuntiesRole;
		}
		boolean opportunitiesAccessForVendor = utilDao.hasEnableLeadsAccessByCompanyId(vendorCompanyId);
		leftSideNavigationBarItem.setOpportunitiesAccessAsPartner(
				opportunitiesAccessForVendor && opportunitiesAccessAsPartner && isVendorGivenAccessToPartner);
		/*** 04/05/2023 **/
		leftSideNavigationBarItem.setShowAddLeadOrDealButtonInMyProfileSection(
				leftSideNavigationBarItem.isOpportunitiesAccessAsPartner() && isVendorGivenAccessToPartner);

	}

	/*** XNFR-266 ****/
	private void setSharedLeadsModuleForVendorVanityLogin(LeftSideNavigationBarItem leftSideNavigationBarItem,
			RoleDisplayDTO roleDisplayDto, Integer vendorCompanyId, Integer userId) {
		boolean shareLeadsAccessForVendor = utilDao.hasShareLeadsAccessByCompanyId(vendorCompanyId);
		Integer partnerCompanyId = leftSideNavigationBarItem.getCompanyId();
		if (partnerCompanyId != null && partnerCompanyId > 0) {
			boolean isAtLeastOneShareLeadsListSharedWithPartnerCompany = userListDAO
					.isAtLeastOneShareLeadsListSharedByVendorCompanyWithPartnerCompany(vendorCompanyId,
							partnerCompanyId, userId);
			boolean isVendorGivenAccessToPartner = utilDao
					.fetchModuleAccessForPartnerByModuleIdAndVendorCompanyIdAndUserId(vendorCompanyId, 14,
							leftSideNavigationBarItem.getUserId());
			leftSideNavigationBarItem.setSharedLeads(shareLeadsAccessForVendor
					&& isAtLeastOneShareLeadsListSharedWithPartnerCompany && isVendorGivenAccessToPartner);
		}

	}

	private void setContentMenuForVendorVanityLogin(LeftSideNavigationBarItem leftSideNavigationBarItem,
			Integer vendorCompanyId, RoleDisplayDTO roleDisplayDto) {

		/**** DAM ***********/
		setDAMMenuForVendorVanityLogin(leftSideNavigationBarItem, vendorCompanyId, roleDisplayDto);
		/********** LMS ********/
		setLearningTracksMenuForVendorVanityLogin(leftSideNavigationBarItem, vendorCompanyId, roleDisplayDto);
		/**** PLAYBOOKS *******/
		setPlayBookAccessForVendorVanityLogin(leftSideNavigationBarItem, vendorCompanyId, roleDisplayDto);

		boolean content = leftSideNavigationBarItem.isDamAccessAsPartner()
				|| leftSideNavigationBarItem.isLmsAccessAsPartner()
				|| leftSideNavigationBarItem.isPlaybookAccessAsPartner();
		leftSideNavigationBarItem.setContent(content);
	}

	private void setPlayBookAccessForVendorVanityLogin(LeftSideNavigationBarItem leftSideNavigationBarItem,
			Integer vendorCompanyId, RoleDisplayDTO roleDisplayDto) {
		utilService.setPlayBookAccessForVendorVanityLogin(leftSideNavigationBarItem, vendorCompanyId, roleDisplayDto);
	}

	private void setLearningTracksMenuForVendorVanityLogin(LeftSideNavigationBarItem leftSideNavigationBarItem,
			Integer vendorCompanyId, RoleDisplayDTO roleDisplayDto) {
		utilService.setLearningTracksAccessForVendorVanityLogin(leftSideNavigationBarItem, vendorCompanyId,
				roleDisplayDto);
	}

	private void setDAMMenuForVendorVanityLogin(LeftSideNavigationBarItem leftSideNavigationBarItem,
			Integer vendorCompanyId, RoleDisplayDTO roleDisplayDto) {
		utilService.setDAMAccessForVendorVanityLogin(leftSideNavigationBarItem, vendorCompanyId, roleDisplayDto);
	}

	private void setMdfMenuForVendorVanityLogin(LeftSideNavigationBarItem leftSideNavigationBarItem,
			Integer vendorCompanyId, RoleDisplayDTO roleDisplayDto) {
		boolean mdfRole = roleDisplayDto.getRoleIds().indexOf(Role.MDF.getRoleId()) > -1;
		boolean mdfAccessAsPartner = false;
		if (roleDisplayDto.isPartner() || roleDisplayDto.isPartnerTeamMember()) {
			mdfAccessAsPartner = mdfRole || roleDisplayDto.isPartner();
		} else {
			mdfAccessAsPartner = roleDisplayDto.anyAdminAndPartnerRole() || mdfRole;
		}
		boolean isVendorGivenAccessToPartner = utilDao.fetchModuleAccessForPartnerByModuleIdAndVendorCompanyIdAndUserId(
				vendorCompanyId, 8, leftSideNavigationBarItem.getUserId());
		leftSideNavigationBarItem.setMdfAccessAsPartner(
				utilDao.hasMdfAccessByCompanyId(vendorCompanyId) && mdfAccessAsPartner && isVendorGivenAccessToPartner);
	}

	private void setMenuItemsForOwnVanityLogin(LeftSideNavigationBarItem leftSideNavigationBarItem, Integer userId,
			RoleDisplayDTO roleDisplayDto, List<Integer> roleIds) {
		boolean isAnyAdmin = roleDisplayDto.isAnyAdmin();

		/**** Partners **********/
		setPartnersMenuForXamplifyLogin(leftSideNavigationBarItem, userId, roleIds, isAnyAdmin);

		/**** Content (Videos/ DAM / Learning Tracks / Playbook) **********/
		setContentMenuForOwnVanityLogin(leftSideNavigationBarItem, userId, roleIds, isAnyAdmin);

		/******* Design (Email Templates / Forms / Pages) **************/
		setDesignMenuForXamplifyLogin(leftSideNavigationBarItem, userId, roleIds, isAnyAdmin);

		/**** Share Leads/Shared Leads *****/
		setShareLeadsAccess(leftSideNavigationBarItem, userId, roleIds, isAnyAdmin);

		/******** Opportunities ***********/
		setOpportunitiesAccess(leftSideNavigationBarItem, userId, roleIds, isAnyAdmin);

		/******** MDF **********/
		setMdfAccess(leftSideNavigationBarItem, userId, roleIds, isAnyAdmin);

		leftSideNavigationBarItem.setFolders(true);

		/**** Stats *********/
		setStatsForXamplifyLogin(leftSideNavigationBarItem, roleIds, isAnyAdmin);
		/*** XNFR-979 *****/
		setInsightsAccessForXamplifyLogin(leftSideNavigationBarItem, userId);

		/*** XNFR-1062 *****/
		setMailEnabledAccessForXamplifyLogin(leftSideNavigationBarItem, userId);

		/*** Chat Support ************/
		leftSideNavigationBarItem
				.setNotifyPartners(roleDisplayDto.isAnyAdminOrSuperVisorExcludingOnlyPartnerAndMarketing());
		/***** XNFR-326 ****/
		leftSideNavigationBarItem
				.setEmailNotificationSettings(roleDisplayDto.isAnyAdminOrSuperVisorExcludingOnlyPartnerAndMarketing());

	}

	private void setContentMenuForOwnVanityLogin(LeftSideNavigationBarItem leftSideNavigationBarItem, Integer userId,
			List<Integer> roleIds, boolean isAnyAdmin) {

		setDAMAccess(leftSideNavigationBarItem, userId, roleIds, isAnyAdmin);

		setLearningTrackAccess(leftSideNavigationBarItem, userId, roleIds, isAnyAdmin);

		setPlayBookAccess(leftSideNavigationBarItem, userId, roleIds, isAnyAdmin);

		setContentMenu(leftSideNavigationBarItem);
	}

	private void setContentMenu(LeftSideNavigationBarItem leftSideNavigationBarItem) {
		boolean damAccess = leftSideNavigationBarItem.isDam();
		boolean lmsAccess = leftSideNavigationBarItem.isLms();
		boolean playBookAccess = leftSideNavigationBarItem.isPlaybook();
		leftSideNavigationBarItem
				.setContent(leftSideNavigationBarItem.isVideos() || damAccess || lmsAccess || playBookAccess);
	}

	private void setAccountDashboard(LeftSideNavigationBarItem leftSideNavigationBarItem,
			RoleDisplayDTO roleDisplayDto) {
		boolean isOnlyUser = roleDisplayDto.getRole().equals(userRole);
		boolean companyProfileCreated = roleDisplayDto.isCompanyExists();
		boolean accountDashboard = companyProfileCreated && !isOnlyUser;
		boolean superAdmin = false;
		leftSideNavigationBarItem.setAccountDashboard(accountDashboard && !superAdmin);
		leftSideNavigationBarItem.setCompanyProfileCreated(companyProfileCreated && !superAdmin);
	}

	private void setModuleAccessForXamplifyLogin(LeftSideNavigationBarItem leftSideNavigationBarItem,
			RoleDisplayDTO roleDisplayDto, boolean isPartnershipEstablishedOnlyWithPrm,
			boolean isPartnershipEstablishedOnlyWithPrmAndLoggedInAsPartnerOrPartnerTeamMember) {
		leftSideNavigationBarItem.setLoggedInThroughXamplifyUrl(true);
		Integer userId = leftSideNavigationBarItem.getUserId();
		List<Integer> roleIds = roleDisplayDto.getRoleIds();
		boolean isAnyAdmin = roleDisplayDto.isAnyAdmin();
		/**** Partners **********/
		setPartnersMenuForXamplifyLogin(leftSideNavigationBarItem, userId, roleIds, isAnyAdmin);

		/**** Content (Videos/ DAM / Learning Tracks / Playbook) **********/
		setContentMenuForXamplifyLogin(leftSideNavigationBarItem, roleDisplayDto, userId, roleIds, isAnyAdmin);

		/**** Share Leads/Shared Leads *****/
		setShareLeadsOrSharedLeadsMenuForXamplifyLogin(leftSideNavigationBarItem, roleDisplayDto, userId, roleIds,
				isAnyAdmin);

		/******* Design (Email Templates / Forms / Pages) **************/
		setDesignMenuForXamplifyLogin(leftSideNavigationBarItem, userId, roleIds, isAnyAdmin);

		/******** Opportunities ***********/
		setOpportunitesMenuIcon(leftSideNavigationBarItem, roleDisplayDto, userId, roleIds, isAnyAdmin);

		/******** MDF **********/
		setMDFMenuForXamplifyLogin(leftSideNavigationBarItem, roleDisplayDto, userId, roleIds, isAnyAdmin);

		/**** Stats *********/
		setStatsForXamplifyLogin(leftSideNavigationBarItem, roleIds, isAnyAdmin);

		/*** XNFR-979 *****/
		setInsightsAccessForXamplifyLogin(leftSideNavigationBarItem, userId);

		/****** Show Add Leads/Deals Option In The Dashboard ***********/
		boolean showAddLeadsAndDealsOptionInTheDashboard = isPartnershipEstablishedOnlyWithPrm
				&& leftSideNavigationBarItem.isOpportunitiesAccessAsPartner()
				&& leftSideNavigationBarItem.isOnlyPartnerCompany();
		leftSideNavigationBarItem.setShowAddLeadsAndDealsOptionsInDashboard(showAddLeadsAndDealsOptionInTheDashboard);

		leftSideNavigationBarItem.setPartnershipEstablishedOnlyWithPrmAndLoggedInAsPartner(
				isPartnershipEstablishedOnlyWithPrmAndLoggedInAsPartnerOrPartnerTeamMember);

		leftSideNavigationBarItem
				.setNotifyPartners(roleDisplayDto.isAnyAdminOrSuperVisorExcludingOnlyPartnerAndMarketing());

		/***** XNFR-326 ***/
		leftSideNavigationBarItem
				.setEmailNotificationSettings(roleDisplayDto.isAnyAdminOrSuperVisorExcludingOnlyPartnerAndMarketing());

		/**** XNFR-252 ****/
		Integer loginAsUserId = leftSideNavigationBarItem.getLoginAsUserId();
		boolean isVendorLoggedInAsPartnerFromxAmplifyLogin = loginAsUserId != null && loginAsUserId > 0;
		setPartnerLoginAccessForXAmplifyNavigation(leftSideNavigationBarItem, roleDisplayDto, userId, loginAsUserId,
				isVendorLoggedInAsPartnerFromxAmplifyLogin);

	}

	// XNFR-820
	private void setApprovalHubAccessForXamplifyLogin(LeftSideNavigationBarItem leftSideNavigationBarItem,
			Integer userId, RoleDisplayDTO roleDisplayDto) {
		boolean damAccess = leftSideNavigationBarItem.isDam();
		boolean lmsAccess = leftSideNavigationBarItem.isLms();
		boolean playBookAccess = leftSideNavigationBarItem.isPlaybook();
		boolean contetAcess = leftSideNavigationBarItem.isContent();
		if (damAccess || lmsAccess || playBookAccess || contetAcess) {
			leftSideNavigationBarItem.setApprovalHub(utilDao.hasApprovalHubAccessByUserId(userId));
		}
	}

	private void setPartnerLoginAccessForXAmplifyNavigation(LeftSideNavigationBarItem leftSideNavigationBarItem,
			RoleDisplayDTO roleDisplayDto, Integer userId, Integer loginAsUserId,
			boolean isVendorLoggedInAsPartnerFromxAmplifyLogin) {
		if (isVendorLoggedInAsPartnerFromxAmplifyLogin) {

			setLoginAsPartnerOptionForXamplifyLogin(leftSideNavigationBarItem, loginAsUserId);
			Integer vendorCompanyId = leftSideNavigationBarItem.getLoginAsUserCompanyId();
			/************ Content ***********/
			setContentMenuForLoginAsPartnerFromXamplifyLogin(leftSideNavigationBarItem, roleDisplayDto);

			boolean partnershipEstablishedOnlyWithPrm = utilDao.isPrmByVendorCompanyId(vendorCompanyId);
			leftSideNavigationBarItem
					.setPartnershipEstablishedOnlyWithPrmAndLoggedInAsPartner(partnershipEstablishedOnlyWithPrm);
			/**** XNFR-252 ****/
			setSharedLeadsAndStatsAndMdfAndOpportunitesAndChatSupportOptionsForVendorVanityLogin(
					leftSideNavigationBarItem, roleDisplayDto, userId, vendorCompanyId);

			/** 05/05/2023 ***/
			showAddLeadsOrDealsButtonsInMyProfileSectionForXamplifyLogin(leftSideNavigationBarItem, roleDisplayDto);

		}
	}

	/**** XNFR-252 ****/
	private void setContentMenuForLoginAsPartnerFromXamplifyLogin(LeftSideNavigationBarItem leftSideNavigationBarItem,
			RoleDisplayDTO roleDisplayDto) {

		boolean contentAccessForXamplifyLogin = leftSideNavigationBarItem.isDam() || leftSideNavigationBarItem.isLms()
				|| leftSideNavigationBarItem.isPlaybook();

		setContentMenuForVendorVanityLogin(leftSideNavigationBarItem,
				leftSideNavigationBarItem.getLoginAsUserCompanyId(), roleDisplayDto);

		boolean contentAccessForVendorVanityLogin = leftSideNavigationBarItem.isContent();

		leftSideNavigationBarItem.setContent(contentAccessForXamplifyLogin || contentAccessForVendorVanityLogin);

	}

	/***** XNFR-252 ****/
	private void setLoginAsPartnerOptionForXamplifyLogin(LeftSideNavigationBarItem leftSideNavigationBarItem,
			Integer loginAsUserId) {

		Integer vendorCompanyId = userDAO.getCompanyIdByUserId(loginAsUserId);
		leftSideNavigationBarItem.setLoginAsUserCompanyId(vendorCompanyId);
		Integer partnerCompanyId = leftSideNavigationBarItem.getCompanyId();

		boolean hasLoginAsPartnerAccessByVendorCompanyId = false;
		if (vendorCompanyId != null && vendorCompanyId > 0) {
			hasLoginAsPartnerAccessByVendorCompanyId = utilDao.hasLoginAsPartnerAccessByCompanyId(vendorCompanyId);
		}

		boolean isLoginAsPartnerOptionEnabledForVendor = false;
		if (vendorCompanyId != null && vendorCompanyId > 0 && partnerCompanyId != null && partnerCompanyId > 0) {
			isLoginAsPartnerOptionEnabledForVendor = utilDao
					.isLoginAsPartnerOptionEnabledForVendorByVendorCompanyIdAndPartnerCompanyId(vendorCompanyId,
							partnerCompanyId);
		}

		boolean loginAsPartnerOptionEnabledForVendor = hasLoginAsPartnerAccessByVendorCompanyId
				&& isLoginAsPartnerOptionEnabledForVendor;
		leftSideNavigationBarItem.setLoginAsPartnerOptionEnabledForVendor(loginAsPartnerOptionEnabledForVendor);

	}

	private void setStatsForXamplifyLogin(LeftSideNavigationBarItem leftSideNavigationBarItem, List<Integer> roleIds,
			boolean isAnyAdmin) {
		boolean statsRole = roleIds.indexOf(Role.STATS_ROLE.getRoleId()) > -1;
		leftSideNavigationBarItem.setStats(isAnyAdmin || statsRole);
	}

	private void setMDFMenuForXamplifyLogin(LeftSideNavigationBarItem leftSideNavigationBarItem,
			RoleDisplayDTO roleDisplayDto, Integer userId, List<Integer> roleIds, boolean isAnyAdmin) {
		boolean mdfRole = setMdfAccess(leftSideNavigationBarItem, userId, roleIds, isAnyAdmin);
		setMDFAccessAsPartner(leftSideNavigationBarItem, roleDisplayDto, userId, mdfRole);

	}

	private boolean setMdfAccess(LeftSideNavigationBarItem leftSideNavigationBarItem, Integer userId,
			List<Integer> roleIds, boolean isAnyAdmin) {
		boolean mdfRole = roleIds.indexOf(Role.MDF.getRoleId()) > -1;
		boolean mdfAccess = utilDao.hasMdfAccessByUserId(userId);
		leftSideNavigationBarItem.setMdf(mdfAccess && (isAnyAdmin || mdfRole));
		return mdfRole;
	}

	private void setMDFAccessAsPartner(LeftSideNavigationBarItem leftSideNavigationBarItem,
			RoleDisplayDTO roleDisplayDto, Integer userId, boolean mdfRole) {
		if (roleDisplayDto.anyPartnerRole()) {
			boolean mdfAccessAsPartner = false;
			if (roleDisplayDto.isPartner() || roleDisplayDto.isPartnerTeamMember()) {
				mdfAccessAsPartner = mdfRole || roleDisplayDto.isPartner();
			} else {
				mdfAccessAsPartner = roleDisplayDto.anyAdminAndPartnerRole() || mdfRole;
			}
			leftSideNavigationBarItem.setMdfAccessAsPartner(utilDao.mdfAccessForPartner(userId) && mdfAccessAsPartner);
		}
	}

	private void setOpportunitesMenuIcon(LeftSideNavigationBarItem leftSideNavigationBarItem,
			RoleDisplayDTO roleDisplayDto, Integer userId, List<Integer> roleIds, boolean isAnyAdmin) {
		boolean opportunityRole = setOpportunitiesAccess(leftSideNavigationBarItem, userId, roleIds, isAnyAdmin);
		setOpportunitiesAccessAsPartner(leftSideNavigationBarItem, roleDisplayDto, userId, opportunityRole);
		/*** Show/Hide Add Leads/Deals Button ****/
		showAddLeadsOrDealsButtonsInMyProfileSectionForXamplifyLogin(leftSideNavigationBarItem, roleDisplayDto);

	}

	/** 04/05/2023 ***/
	private void showAddLeadsOrDealsButtonsInMyProfileSectionForXamplifyLogin(
			LeftSideNavigationBarItem leftSideNavigationBarItem, RoleDisplayDTO roleDisplayDto) {
		boolean opportunitiesAccessAsPartner = leftSideNavigationBarItem.isOpportunitiesAccessAsPartner();
		boolean showAddLeadsOrDealsButton = false;
		boolean navigateToPartnerSection = false;
		boolean isPrmAndPartnerCompany = roleDisplayDto.isPrmAndPartner() || roleDisplayDto.isPrmAndPartnerTeamMember();
		boolean isPartnerCompany = roleDisplayDto.isPartner() || roleDisplayDto.isPartnerTeamMember();
		if (isPrmAndPartnerCompany || isPartnerCompany) {
			showAddLeadsOrDealsButton = opportunitiesAccessAsPartner;
			navigateToPartnerSection = isPrmAndPartnerCompany && showAddLeadsOrDealsButton;
		}
		leftSideNavigationBarItem.setShowAddLeadOrDealButtonInMyProfileSection(showAddLeadsOrDealsButton);
		leftSideNavigationBarItem.setNavigateToPartnerViewSection(navigateToPartnerSection);
	}

	private boolean setOpportunitiesAccess(LeftSideNavigationBarItem leftSideNavigationBarItem, Integer userId,
			List<Integer> roleIds, boolean isAnyAdmin) {
		boolean opportunityRole = roleIds.indexOf(Role.OPPORTUNITY.getRoleId()) > -1;
		boolean opportunitiesAccess = utilDao.hasEnableLeadsAccessByUserId(userId);
		leftSideNavigationBarItem.setOpportunities(opportunitiesAccess && (isAnyAdmin || opportunityRole));
		return opportunityRole;
	}

	private void setOpportunitiesAccessAsPartner(LeftSideNavigationBarItem leftSideNavigationBarItem,
			RoleDisplayDTO roleDisplayDto, Integer userId, boolean opportunityRole) {
		if (roleDisplayDto.anyPartnerRole()) {
			boolean opportunitiesAccessAsPartner = false;
			if (roleDisplayDto.isPartner() || roleDisplayDto.isPartnerTeamMember()) {
				opportunitiesAccessAsPartner = opportunityRole || roleDisplayDto.isPartner();
			} else {
				opportunitiesAccessAsPartner = roleDisplayDto.anyAdminAndPartnerRole() || opportunityRole;
			}
			leftSideNavigationBarItem.setOpportunitiesAccessAsPartner(
					utilDao.enableLeadsForPartner(userId) && opportunitiesAccessAsPartner);
		}
	}

	private void setDesignMenuForXamplifyLogin(LeftSideNavigationBarItem leftSideNavigationBarItem, Integer userId,
			List<Integer> roleIds, boolean isAnyAdmin) {

		boolean formAccess = utilDao.hasFormAccessByUserId(userId);
		leftSideNavigationBarItem.setForms(formAccess);

		leftSideNavigationBarItem.setDesign(formAccess);
	}

	private void setShareLeadsOrSharedLeadsMenuForXamplifyLogin(LeftSideNavigationBarItem leftSideNavigationBarItem,
			RoleDisplayDTO roleDisplayDto, Integer userId, List<Integer> roleIds, boolean isAnyAdmin) {
		boolean shareLeadsModule = setShareLeadsAccess(leftSideNavigationBarItem, userId, roleIds, isAnyAdmin);
		setSharedLeadsOptionForXamplifyLogin(leftSideNavigationBarItem, roleDisplayDto, userId, shareLeadsModule);
	}

	private boolean setShareLeadsAccess(LeftSideNavigationBarItem leftSideNavigationBarItem, Integer userId,
			List<Integer> roleIds, boolean isAnyAdmin) {
		boolean shareLeadsModule = roleIds.indexOf(Role.SHARE_LEADS.getRoleId()) > -1;
		boolean shareLeadsAccess = utilDao.hasShareLeadsAccessByUserId(userId);
		leftSideNavigationBarItem.setShareLeads(shareLeadsAccess && (isAnyAdmin || shareLeadsModule));
		return shareLeadsModule;
	}

	/*** XNFR-266 ****/
	private void setSharedLeadsOptionForXamplifyLogin(LeftSideNavigationBarItem leftSideNavigationBarItem,
			RoleDisplayDTO roleDisplayDto, Integer userId, boolean shareLeadsModule) {
		if (roleDisplayDto.anyPartnerRole()) {
			Integer partnerCompanyId = leftSideNavigationBarItem.getCompanyId();
			if (partnerCompanyId != null && partnerCompanyId > 0) {
				boolean isShareLeadsShared = userListDAO
						.isAtLeastOneShareLeadsListSharedByVendorCompanyWithPartnerCompany(partnerCompanyId, userId);
				leftSideNavigationBarItem
						.setSharedLeads(utilDao.sharedLeadsAccessForPartner(userId) && isShareLeadsShared);
			}

		}

	}

	private void setContentMenuForXamplifyLogin(LeftSideNavigationBarItem leftSideNavigationBarItem,
			RoleDisplayDTO roleDisplayDto, Integer userId, List<Integer> roleIds, boolean isAnyAdmin) {
		/***** DAM ******/
		setDAMMenuForXamplifyLogin(leftSideNavigationBarItem, roleDisplayDto, userId, roleIds, isAnyAdmin);
		/**** LMS *******/
		setLearningTracksMenuForXamplifyLogin(leftSideNavigationBarItem, roleDisplayDto, userId, roleIds, isAnyAdmin);
		/***** PlayBook ************/
		setPlayBookMenuForXamplifyLogin(leftSideNavigationBarItem, roleDisplayDto, userId, roleIds, isAnyAdmin);

		setContentMenuForXamplifyLogin(leftSideNavigationBarItem);
	}

	private void setContentMenuForXamplifyLogin(LeftSideNavigationBarItem leftSideNavigationBarItem) {
		boolean damAccess = leftSideNavigationBarItem.isDam() || leftSideNavigationBarItem.isDamAccessAsPartner();
		boolean lmsAccess = leftSideNavigationBarItem.isLms() || leftSideNavigationBarItem.isLmsAccessAsPartner();
		boolean playBookAccess = leftSideNavigationBarItem.isPlaybook()
				|| leftSideNavigationBarItem.isPlaybookAccessAsPartner();
		leftSideNavigationBarItem
				.setContent(leftSideNavigationBarItem.isVideos() || damAccess || lmsAccess || playBookAccess);
	}

	private void setPlayBookMenuForXamplifyLogin(LeftSideNavigationBarItem leftSideNavigationBarItem,
			RoleDisplayDTO roleDisplayDto, Integer userId, List<Integer> roleIds, boolean isAnyAdmin) {
		boolean playBookRole = setPlayBookAccess(leftSideNavigationBarItem, userId, roleIds, isAnyAdmin);
		setPlayBookAccessAsPartner(leftSideNavigationBarItem, roleDisplayDto, userId, playBookRole);
	}

	private boolean setPlayBookAccess(LeftSideNavigationBarItem leftSideNavigationBarItem, Integer userId,
			List<Integer> roleIds, boolean isAnyAdmin) {
		return utilService.setPlayBookAccess(leftSideNavigationBarItem, userId, roleIds, isAnyAdmin);
	}

	private void setPlayBookAccessAsPartner(LeftSideNavigationBarItem leftSideNavigationBarItem,
			RoleDisplayDTO roleDisplayDto, Integer userId, boolean playBookRole) {
		if (roleDisplayDto.anyPartnerRole()) {
			boolean playBookAccessAsPartner = false;
			if (roleDisplayDto.isPartner() || roleDisplayDto.isPartnerTeamMember()) {
				playBookAccessAsPartner = playBookRole || roleDisplayDto.isPartner();
			} else {
				playBookAccessAsPartner = roleDisplayDto.anyAdminAndPartnerRole() || playBookRole;
			}
			boolean isLMSSharedToPartnerCompany = lmsDao.isLMSSharedToPartnerCompanyByPartnerId(userId,
					LearningTrackType.PLAYBOOK, null);
			leftSideNavigationBarItem.setPlaybookAccessAsPartner(utilDao.playbookAccessForPartner(userId)
					&& (playBookAccessAsPartner || isLMSSharedToPartnerCompany));
		}
	}

	private void setLearningTracksMenuForXamplifyLogin(LeftSideNavigationBarItem leftSideNavigationBarItem,
			RoleDisplayDTO roleDisplayDto, Integer userId, List<Integer> roleIds, boolean isAnyAdmin) {
		boolean lmsRole = setLearningTrackAccess(leftSideNavigationBarItem, userId, roleIds, isAnyAdmin);
		setLearningTracksAccessAsPartner(leftSideNavigationBarItem, roleDisplayDto, userId, lmsRole);
	}

	private boolean setLearningTrackAccess(LeftSideNavigationBarItem leftSideNavigationBarItem, Integer userId,
			List<Integer> roleIds, boolean isAnyAdmin) {
		return utilService.setLearningTrackAccess(leftSideNavigationBarItem, userId, roleIds, isAnyAdmin);
	}

	private void setLearningTracksAccessAsPartner(LeftSideNavigationBarItem leftSideNavigationBarItem,
			RoleDisplayDTO roleDisplayDto, Integer userId, boolean lmsRole) {
		if (roleDisplayDto.anyPartnerRole()) {
			boolean lmsAccessAsPartner = false;
			if (roleDisplayDto.isPartner() || roleDisplayDto.isPartnerTeamMember()) {
				lmsAccessAsPartner = lmsRole || roleDisplayDto.isPartner();
			} else {
				lmsAccessAsPartner = roleDisplayDto.anyAdminAndPartnerRole() || lmsRole;
			}
			boolean isLMSSharedToPartnerCompany = lmsDao.isLMSSharedToPartnerCompanyByPartnerId(userId,
					LearningTrackType.TRACK, null);
			leftSideNavigationBarItem.setLmsAccessAsPartner(
					utilDao.lmsAccessForPartner(userId) && (lmsAccessAsPartner || isLMSSharedToPartnerCompany));
		}
	}

	private void setDAMMenuForXamplifyLogin(LeftSideNavigationBarItem leftSideNavigationBarItem,
			RoleDisplayDTO roleDisplayDto, Integer userId, List<Integer> roleIds, boolean isAnyAdmin) {
		boolean damRole = setDAMAccess(leftSideNavigationBarItem, userId, roleIds, isAnyAdmin);
		setDAMAccessAsPartner(leftSideNavigationBarItem, roleDisplayDto, userId, damRole);
	}

	private boolean setDAMAccess(LeftSideNavigationBarItem leftSideNavigationBarItem, Integer userId,
			List<Integer> roleIds, boolean isAnyAdmin) {
		return utilService.setDAMAccess(leftSideNavigationBarItem, userId, roleIds, isAnyAdmin);
	}

	private void setDAMAccessAsPartner(LeftSideNavigationBarItem leftSideNavigationBarItem,
			RoleDisplayDTO roleDisplayDto, Integer userId, boolean damRole) {
		if (roleDisplayDto.anyPartnerRole()) {
			boolean damAccessAsPartner = false;
			if (roleDisplayDto.isPartner() || roleDisplayDto.isPartnerTeamMember()) {
				damAccessAsPartner = damRole || roleDisplayDto.isPartner();
			} else {
				damAccessAsPartner = roleDisplayDto.anyAdminAndPartnerRole() || damRole;
			}
			boolean isAssetSharedToPartnerCompany = damDao.isAssetSharedToPartnerCompanyByPartnerId(userId);
			leftSideNavigationBarItem.setDamAccessAsPartner(
					utilDao.damAccessForPartner(userId) && (damAccessAsPartner || isAssetSharedToPartnerCompany));
		}
	}

	private void setPartnersMenuForXamplifyLogin(LeftSideNavigationBarItem leftSideNavigationBarItem, Integer userId,
			List<Integer> roleIds, boolean isAnyAdmin) {
		boolean partnersRole = roleIds.indexOf(Role.PARTNERS.getRoleId()) > -1;
		boolean partners = isAnyAdmin || partnersRole;
		leftSideNavigationBarItem.setPartners(partners);
		boolean hasCreateWorkflowAccess = utilDao.hasCreateWorkflowAccessByUserId(userId);
		leftSideNavigationBarItem.setCreateWorkflow(hasCreateWorkflowAccess);
	}

	@Override
	public WelcomePageItem getWelcomePageItems(VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		WelcomePageItem welcomePageItem = new WelcomePageItem();
		LeftSideNavigationBarItem leftSideNavigationBarItem = findLeftMenuItems(vanityUrlDetailsDTO);
		RoleDisplayDTO roleDisplayDTO = leftSideNavigationBarItem.getRoleDisplayDto();
		List<Integer> roleIds = roleDisplayDTO.getRoleIds();
		boolean isPartner = roleIds.indexOf(Role.COMPANY_PARTNER.getRoleId()) > -1;
		boolean isPrm = roleIds.indexOf(Role.PRM_ROLE.getRoleId()) > -1;
		boolean isPartnerTeamMember = roleDisplayDTO.isPartnerTeamMember();
		int i = 0;
		/*** Partners ********/
		boolean partners = leftSideNavigationBarItem.isPartners();
		if (partners) {
			i = i + 1;
			welcomePageItem.setPartnersImgePosition(WelcomePageItem.getPosition(i));
			welcomePageItem.setShowPartners(partners);
		}
		/*** Content ********/
		boolean content = leftSideNavigationBarItem.isContent();
		if (content) {
			i = i + 1;
			welcomePageItem.setContentImagePosition(WelcomePageItem.getPosition(i));
			welcomePageItem.setShowContent(content);
		}
		/*** Team Members ********/
		boolean teamMembers = leftSideNavigationBarItem.isDeletedPartner();
		if (teamMembers) {
			i = i + 1;
			welcomePageItem.setTeamMembersImagePosition(WelcomePageItem.getPosition(i));
			welcomePageItem.setShowTeamMembers(teamMembers);
		}

		/*** Analytics *******/
		welcomePageItem.setShowAnalytics(
				isPrm || (isPartner || isPartnerTeamMember) || roleIds.indexOf(Role.STATS_ROLE.getRoleId()) > -1);
		if (welcomePageItem.isShowAnalytics() || leftSideNavigationBarItem.isDeletedPartner()) {
			i = i + 1;
			welcomePageItem.setAnalyticsImagePosition(WelcomePageItem.getPosition(i));
		}

		return welcomePageItem;
	}

	public boolean isShareLeadsListAssignedToPartner(Integer userId) {
		return userListDAO.isShareLeadsListAssignedToPartner(userId);
	}

	public boolean isShareLeadsListAssignedToPartner(Integer vendorCompanyId, Integer userId) {
		Integer partnerCompanyId = userDAO.getCompanyIdByUserId(userId);
		Integer partnershipId = partnershipDAO.getPartnershipIdByVendorCompanyIdAndPartnerCompanyId(vendorCompanyId,
				partnerCompanyId);
		return userListDAO.isShareLeadsListAssignedToPartner(partnershipId, userId);
	}

	@Override
	public XtremandResponse findModuleCustomNamesByCompanyId(Integer companyId) {
		XtremandResponse xtremandResponse = new XtremandResponse();
		xtremandResponse.setStatusCode(200);
		xtremandResponse.setData(moduleDao.findModuleCustomNamesByCompanyId(companyId));
		return xtremandResponse;
	}

	@Override
	public XtremandResponse findPartnerModuleByCompanyId(Integer companyId) {
		XtremandResponse xtremandResponse = new XtremandResponse();
		xtremandResponse.setStatusCode(200);
		xtremandResponse.setData(moduleDao.findPartnerModuleByCompanyId(companyId));
		return xtremandResponse;
	}

	@Override
	public XtremandResponse updateModuleName(ModuleCustomDTO moduleCustomNameDTO) {
		XtremandResponse xtremandResponse = new XtremandResponse();
		xtremandResponse.setStatusCode(200);
		ModuleCustom moduleCustomName = genericDao.get(ModuleCustom.class, moduleCustomNameDTO.getId());
		moduleCustomName.setCustomName(moduleCustomNameDTO.getCustomName());
		moduleCustomName.setUpdatedTime(new Date());
		xtremandResponse.setMessage("Name Updated Successfully");
		return xtremandResponse;
	}

	/********* XNFR-224 ********/
	@Override
	public XtremandResponse findLoginAsPartnerSettingsOptions(String vendorCompanyProfileName, Integer loggedInUserId) {
		XtremandResponse xtremandResponse = new XtremandResponse();
		xtremandResponse.setStatusCode(200);
		xtremandResponse.setData(utilDao.findLoginAsPartnerSettingsOptions(vendorCompanyProfileName, loggedInUserId));
		return xtremandResponse;
	}

	/********* XNFR-224 ********/
	@Override
	public XtremandResponse updateLoginAsPartnerSettingsOptions(LoginAsPartnerDTO loginAsPartnerDTO) {
		XtremandResponse xtremandResponse = new XtremandResponse();
		Integer vendorCompanyId = userDAO.getCompanyIdByProfileName(loginAsPartnerDTO.getVendorCompanyProfileName());
		Integer partnerCompanyId = userDAO.getCompanyIdByUserId(loginAsPartnerDTO.getLoggedInUserId());
		loginAsPartnerDTO.setVendorCompanyId(vendorCompanyId);
		loginAsPartnerDTO.setPartnerCompanyId(partnerCompanyId);
		utilDao.updateLoginAsPartnerOptionEnabledForVendor(loginAsPartnerDTO);
		xtremandResponse.setStatusCode(200);
		xtremandResponse.setMessage("Settings Updated Successfully");
		return xtremandResponse;
	}

	/********* XNFR-276 ********/
	@Override
	public XtremandResponse getCustomizedLeftMenuItems(VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		XtremandResponse xtremandResponse = new XtremandResponse();
		xtremandResponse.setStatusCode(400);
		if (vanityUrlDetailsDTO != null && vanityUrlDetailsDTO.getUserId() != null
				&& vanityUrlDetailsDTO.getUserId() > 0) {
			Integer loggedInUserId = vanityUrlDetailsDTO.getUserId();
			vanityUrlDetailsDTO.setLoggedInUserCompanyId(userDAO.getCompanyIdByUserId(loggedInUserId));
			LeftSideNavigationBarItem leftSideNavigationBarItem = findLeftMenuItems(vanityUrlDetailsDTO);
			List<ModuleCustomDTO> moduleCustomDtos = getCustomizedLeftMenuItems(vanityUrlDetailsDTO,
					leftSideNavigationBarItem);
			xtremandResponse.setData(moduleCustomDtos);
			xtremandResponse.setStatusCode(200);
		}
		return xtremandResponse;
	}

	private List<ModuleCustomDTO> getCustomizedLeftMenuItems(VanityUrlDetailsDTO vanityUrlDetailsDTO,
			LeftSideNavigationBarItem leftSideNavigationBarItem) {
		List<ModuleCustomDTO> moduleCustomDtos = null;
		List<ModuleCustom> moduleCustoms = getModuleCustomsInOrder(vanityUrlDetailsDTO, leftSideNavigationBarItem);
		if (moduleCustoms != null) {
			moduleCustomDtos = getModuleCustomDtos(moduleCustoms, vanityUrlDetailsDTO, leftSideNavigationBarItem);
		}
		return moduleCustomDtos;
	}

	private List<ModuleCustomDTO> getModuleCustomDtos(List<ModuleCustom> moduleCustoms,
			VanityUrlDetailsDTO vanityUrlDetailsDTO, LeftSideNavigationBarItem leftSideNavigationBarItem) {
		List<ModuleCustomDTO> moduleCustomDtos = new ArrayList<>();
		for (ModuleCustom moduleCustom : moduleCustoms) {
			ModuleCustomDTO moduleCustomDto = new ModuleCustomDTO();
			BeanUtils.copyProperties(moduleCustom, moduleCustomDto);
			com.xtremand.common.bom.Module module = moduleCustom.getModule();
			moduleCustomDto.setModuleId(module.getId());
			moduleCustomDto.setAngularPath(module.getAngularPath());
			moduleCustomDto.setModuleName(module.getModuleName());
			moduleCustomDto.setAngularIcon(module.getAngularIcon());
			moduleCustomDto.setMergeTag(module.getMergeTag());
			if (!moduleCustom.isHideMenu()) {
				setSubModuleDtos(module, leftSideNavigationBarItem, moduleCustomDto);
			}
			moduleCustomDtos.add(moduleCustomDto);
		}
		return moduleCustomDtos;
	}

	private void setSubModuleDtos(com.xtremand.common.bom.Module module,
			LeftSideNavigationBarItem leftSideNavigationBarItem, ModuleCustomDTO moduleCustomDto) {
		List<ModuleCustomDTO> subModuleDtos = new ArrayList<>();
		List<com.xtremand.common.bom.Module> subModules = module.getSubModules();
		for (com.xtremand.common.bom.Module subModule : subModules) {
			boolean create = true;
			if ((subModule.getId().equals(26)) || (subModule.getId().equals(27))) {
				create = false;
			}
			if (create) {
				ModuleCustomDTO subModuleDto = new ModuleCustomDTO();
				subModuleDto.setAngularPath(subModule.getAngularPath());
				String moduleName = subModule.getModuleName();
				boolean isPartnerExperienceAutomationModule = "Partner Experience Automation".equals(moduleName);
				if (module.getId().equals(11) || isPartnerExperienceAutomationModule) {
					moduleName = moduleName.replace("Partner", moduleCustomDto.getCustomName());
				}
				if (isPartnerExperienceAutomationModule) {
					if (leftSideNavigationBarItem.isCreateWorkflow()) {
						addSubModules(subModuleDtos, subModule, subModuleDto, moduleName);
					}
				} else {
					addSubModules(subModuleDtos, subModule, subModuleDto, moduleName);
				}

			}
		}
		if (subModuleDtos != null && !subModuleDtos.isEmpty()) {
			moduleCustomDto.setShowSubMenu(true);
			moduleCustomDto.setSubModules(subModuleDtos);
		}
	}

	private void addSubModules(List<ModuleCustomDTO> subModuleDtos, com.xtremand.common.bom.Module subModule,
			ModuleCustomDTO subModuleDto, String moduleName) {
		subModuleDto.setModuleName(moduleName);
		subModuleDto.setCustomName(moduleName);
		subModuleDto.setModuleId(subModule.getId());
		subModuleDto.setAngularIcon(subModule.getAngularIcon());
		subModuleDto.setMergeTag(subModule.getMergeTag());
		subModuleDtos.add(subModuleDto);
	}

	private List<ModuleCustom> getModuleCustomsInOrder(VanityUrlDetailsDTO vanityUrlDetailsDTO,
			LeftSideNavigationBarItem leftSideNavigationBarItem) {
		List<ModuleCustom> moduleCustoms = new ArrayList<ModuleCustom>();
		List<ModuleCustom> allModuleCustoms = getAllModuleCustomsInOrder(vanityUrlDetailsDTO,
				leftSideNavigationBarItem);
		if (allModuleCustoms != null) {
			moduleCustoms = getModuleCustomToDisplay(allModuleCustoms, vanityUrlDetailsDTO, leftSideNavigationBarItem);
		}
		return moduleCustoms;
	}

	private List<ModuleCustom> getModuleCustomToDisplay(List<ModuleCustom> allModuleCustoms,
			VanityUrlDetailsDTO vanityUrlDetailsDTO, LeftSideNavigationBarItem leftSideNavigationBarItem) {
		List<ModuleCustom> moduleCustoms = new ArrayList<ModuleCustom>();
		User user = userService.loadUser(
				Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, vanityUrlDetailsDTO.getUserId())),
				new FindLevel[] { FindLevel.COMPANY_PROFILE });
		for (ModuleCustom module : allModuleCustoms) {
			if (module.getModule().getModuleId().equals(1) && leftSideNavigationBarItem.isAccountDashboard()) {
				moduleCustoms.add(module);
			} else if (module.getModule().getModuleId().equals(4) && leftSideNavigationBarItem.isContent()) {
				moduleCustoms.add(module);
			} else if (module.getModule().getModuleId().equals(6) && leftSideNavigationBarItem.isDesign()) {
				moduleCustoms.add(module);
			} else if (module.getModule().getModuleId().equals(8)
					&& (leftSideNavigationBarItem.isMdf() || (leftSideNavigationBarItem.isMdfAccessAsPartner()))) {
				moduleCustoms.add(module);
			} else if (module.getModule().getModuleId().equals(9) && (leftSideNavigationBarItem.isOpportunities()
					|| (leftSideNavigationBarItem.isOpportunitiesAccessAsPartner()))) {
				moduleCustoms.add(module);
			} else if (module.getModule().getModuleId().equals(11) && leftSideNavigationBarItem.isPartners()) {
				moduleCustoms.add(module);
			} else if (module.getModule().getModuleId().equals(13) && leftSideNavigationBarItem.isShareLeads()) {
				moduleCustoms.add(module);
			} else if (module.getModule().getModuleId().equals(14) && leftSideNavigationBarItem.isSharedLeads()) {
				moduleCustoms.add(module);
			} else if (module.getModule().getModuleId().equals(16) && leftSideNavigationBarItem.isTeam()) {
				moduleCustoms.add(module);
			} else if (module.getModule().getModuleId().equals(19)
					&& ((user.getCompanyProfile() != null && !userService.isOnlyUser(user))
							|| leftSideNavigationBarItem.isDeletedPartner())) {
				moduleCustoms.add(module);
			} else if (module.getModule().getModuleId().equals(33) && (leftSideNavigationBarItem.isApprovalHub()
					&& (checkIfHasContentAccess(leftSideNavigationBarItem)))) {
				moduleCustoms.add(module);
			} else if (module.getModule().getModuleId().equals(34) && (leftSideNavigationBarItem.isInsights())) {
				moduleCustoms.add(module);
			} else if (module.getModule().getModuleId().equals(35) && (leftSideNavigationBarItem.isMailEnable())) {
				moduleCustoms.add(module);
			}
		}
		return moduleCustoms;
	}

	private boolean checkIfHasContentAccess(LeftSideNavigationBarItem leftSideNavigationBarItem) {
		return (leftSideNavigationBarItem.isApprovalRequiredForAssets() && leftSideNavigationBarItem.isDam())
				|| (leftSideNavigationBarItem.isApprovalRequiredForPlaybooks()
						&& leftSideNavigationBarItem.isPlaybook())
				|| (leftSideNavigationBarItem.isApprovalRequiredForTracks() && leftSideNavigationBarItem.isLms());
	}

	private List<ModuleCustom> getAllModuleCustomsInOrder(VanityUrlDetailsDTO vanityUrlDetailsDTO,
			LeftSideNavigationBarItem leftSideNavigationBarItem) {
		Integer partnershipId = null;
		if (vanityUrlDetailsDTO.isPartnerLoggedInThroughVanityUrl()) {
			Partnership partnership = partnershipDAO.checkPartnership(vanityUrlDetailsDTO.getVendorCompanyId(),
					vanityUrlDetailsDTO.getLoggedInUserCompanyId());
			if (partnership != null && partnership.getId() != null && partnership.getId() > 0) {
				partnershipId = partnership.getId();
			}
			return moduleDao.findMarketingModulesCustomInOrder(vanityUrlDetailsDTO.getLoggedInUserCompanyId(),
					partnershipId);
		} else {
			return moduleDao.findModuleCustomInOrder(vanityUrlDetailsDTO.getLoggedInUserCompanyId(), partnershipId);
		}
	}

	@Override
	public XtremandResponse updateLeftMenuItems(ModuleCustomRequestDTO moduleCustomRequestDTO) {
		XtremandResponse xtremandResponse = new XtremandResponse();
		xtremandResponse.setStatusCode(400);
		List<ModuleCustomDTO> moduleCustomDTOs = moduleCustomRequestDTO.getMenuItems();
		if (moduleCustomRequestDTO.getUserId() != null) {
			int index = 1;
			for (ModuleCustomDTO moduleCustomDTO : moduleCustomDTOs) {
				ModuleCustom moduleCustom = genericDao.get(ModuleCustom.class, moduleCustomDTO.getId());
				if (moduleCustom != null) {
					moduleCustom.setDisplayIndex(index++);
					xtremandResponse.setStatusCode(200);
					xtremandResponse.setMessage("Submitted successfully");
					xtremandResponse.setData(moduleCustomDTO);
				}
			}
		}
		return xtremandResponse;
	}

	@Override
	public XtremandResponse handleCustomNamesForExistingCompanies(Integer moduleId) {
		XtremandResponse response = new XtremandResponse();
		List<Integer> companyIds = userDAO.findAllCompanyIds();
		List<com.xtremand.common.bom.Module> modules = moduleDao.findModuleNames();
		for (Integer companyId : companyIds) {

			if (modules != null) {
				List<ModuleCustom> moduleCustoms = utilService.frameModuleCustomsForNewModules(companyId, null, modules,
						moduleId);
				if (moduleCustoms != null) {
					xamplifyUtilDao.saveAll(moduleCustoms, "Modules");
				}
			}

		}
		response.setStatusCode(200);
		response.setMessage("module customs are added succesfully");
		return response;
	}

	@Override
	public XtremandResponse handleCustomNamesForExistingPartnerships(Integer moduleId) {
		XtremandResponse response = new XtremandResponse();
		int count = 0;
		List<Partnership> partnerships = partnershipDAO.findAllApprovedPartnerships();
		Integer size = partnerships.size();
		for (Partnership partnership : partnerships) {
			count++;
			String noOfUsers = "No of Users to be Added :" + count + " / " + size;
			Logger.info(noOfUsers);
			if (partnership != null) {
				if (partnership.getPartnerCompany() != null) {
					Integer companyId = partnership.getPartnerCompany().getId();
					boolean isVanity = utilDao.hasVanityAccessByCompanyId(partnership.getVendorCompany().getId());
					if (isVanity) {
						List<com.xtremand.common.bom.Module> modules = moduleDao.findModuleNames();
						if (modules != null) {
							List<ModuleCustom> moduleCustoms = utilService.frameModuleCustomsForNewModules(companyId,
									partnership, modules, moduleId);
							if (moduleCustoms != null) {
								xamplifyUtilDao.saveAll(moduleCustoms, "Modules");
							}
						}
					}
				}
			}
		}
		response.setStatusCode(200);
		response.setMessage("module customs are added succesfully");
		return response;
	}

	/** XNFR-891 **/
	@Override
	public XtremandResponse fetchModulesToAddPartnerModulesAccess(Integer loggedInUserId, String companyProfileName) {
		XtremandResponse response = new XtremandResponse();
		if (XamplifyUtils.isValidInteger(loggedInUserId)) {
			Map<String, Object> resultMap = new HashMap<>();
			fetchCustomModules(loggedInUserId, null, resultMap, companyProfileName);
			resultMap.put("hasMarketingModulesAccessToPartner", false);
			response.setData(resultMap);
			XamplifyUtils.addSuccessStatus(response);
		}
		return response;
	}

	private void fetchCustomModules(Integer loggedInUserId, Integer partnershipId, Map<String, Object> resultMap,
			String companyProfileName) {
		List<ModuleCustomDTO> modules = new ArrayList<>();
		List<Integer> assignedModuleIds = new ArrayList<>();
		listAccessedModuleIds(loggedInUserId, assignedModuleIds);
		if (XamplifyUtils.isNotEmptyList(assignedModuleIds)) {
			modules = moduleDao.fetchModuleCustomDTOs(assignedModuleIds, partnershipId);
		}
		resultMap.put("defaultModules", modules);
		List<Integer> moduleIds = new ArrayList<>();
		if (XamplifyUtils.isValidString(companyProfileName)) {
			Integer vendorCompanyId = userDAO.getCompanyIdByProfileName(companyProfileName);
			if (utilDao.hasDamAccessByCompanyId(vendorCompanyId)) {
				moduleIds.add(5);
			}
			moduleIds.add(17);
			if (utilDao.hasFormAccessByCompanyId(vendorCompanyId)) {
				moduleIds.add(7);
			}
			List<ModuleCustomDTO> marketingModules = moduleDao.fetchModuleCustomDTOs(moduleIds, null);
			marketingModules.forEach(module -> module.setMarketingModule(true));
			marketingModules.sort(Comparator.comparingInt(m -> moduleIds.indexOf(m.getModuleId())));
			resultMap.put("marketingModules", marketingModules);
		}
	}

	private void listAccessedModuleIds(Integer loggedInUserId, List<Integer> assignedModuleIds) {
		addCampaignModuleByAccess(loggedInUserId, assignedModuleIds);
		addShareLeadsModuleByAccess(loggedInUserId, assignedModuleIds);
		addOpportunityModuleByAccess(loggedInUserId, assignedModuleIds);
		addPrmModules(loggedInUserId, assignedModuleIds);
	}

	private void addOpportunityModuleByAccess(Integer loggedInUserId, List<Integer> assignedModuleIds) {
		if (utilDao.hasEnableLeadsAccessByUserId(loggedInUserId)) {
			assignedModuleIds.add(9);
		}
	}

	private void addShareLeadsModuleByAccess(Integer loggedInUserId, List<Integer> assignedModuleIds) {
		if (utilDao.hasShareLeadsAccessByUserId(loggedInUserId)) {
			assignedModuleIds.add(14);
		}
	}

	private void addCampaignModuleByAccess(Integer userId, List<Integer> assignedModuleIds) {
		if (utilDao.hasModuleAccessByUserId(userId)) {
			assignedModuleIds.add(2);
			assignedModuleIds.add(3);
		}
	}

	private void addPrmModules(Integer userId, List<Integer> assignedModuleIds) {
		addMdfModuleByAccess(userId, assignedModuleIds);
		addDamModuleByAccess(userId, assignedModuleIds);
		addLmsModuleByAccess(userId, assignedModuleIds);
		addPlaybookModuleByAccess(userId, assignedModuleIds);
	}

	private void addPlaybookModuleByAccess(Integer userId, List<Integer> assignedModuleIds) {
		if (utilDao.hasPlaybookAccessByUserId(userId)) {
			assignedModuleIds.add(12);
		}
	}

	private void addLmsModuleByAccess(Integer userId, List<Integer> assignedModuleIds) {
		if (utilDao.hasLmsAccessByUserId(userId)) {
			assignedModuleIds.add(18);
		}
	}

	private void addDamModuleByAccess(Integer userId, List<Integer> assignedModuleIds) {
		if (utilDao.hasDamAccessByUserId(userId)) {
			assignedModuleIds.add(5);
		}
	}

	private void addMdfModuleByAccess(Integer userId, List<Integer> assignedModuleIds) {
		if (utilDao.hasMdfAccessByUserId(userId)) {
			assignedModuleIds.add(8);
		}
	}

	@Override
	public XtremandResponse fetchModulesToEditPartnerModulesAccess(Integer loggedInUserId, Integer partnershipId,
			String companyProfileName) {
		XtremandResponse response = new XtremandResponse();
		if (XamplifyUtils.isValidInteger(partnershipId)) {
			Map<String, Object> resultMap = new HashMap<>();
			fetchCustomModules(loggedInUserId, partnershipId, resultMap, companyProfileName);
			response.setData(resultMap);
			XamplifyUtils.addSuccessStatus(response);
		}
		return response;
	}

	@Override
	public XtremandResponse updatePartnerModulesAccess(UserDTO userDTO) {
		XtremandResponse response = new XtremandResponse();

		if (!isValidUserDTO(userDTO)) {
			XamplifyUtils.addErorMessageWithStatusCode(response, "Failed to update the partner modules", 404);
			return response;
		}

		Integer companyId = userDAO.getCompanyIdByUserId(userDTO.getId());
		if (XamplifyUtils.isValidInteger(companyId)) {
			userDTO.setPartnerCompanyId(companyId);
			updateExistingCustomModules(userDTO, companyId);
		} else {
			CompanyProfile companyProfile = createCompanyProfileAndCustomModules(userDTO);
			response.setData(companyProfile.getId());
		}

		XamplifyUtils.addSuccessStatusWithMessage(response, "Partner modules updated successfully.");
		return response;
	}

	private void updateExistingCustomModules(UserDTO userDTO, Integer companyId) {
		if (moduleDao.checkModuleCustom(userDTO.getPartnershipId(), companyId, 1)) {
			List<Integer> restrictedModuleIds = userDTO.getDefaultModules().stream()
					.filter(module -> !module.isPartnerAccessModule()).map(ModuleCustomDTO::getModuleId)
					.collect(Collectors.toList());
			List<Integer> accessedModuleIds = userDTO.getDefaultModules().stream()
					.filter(ModuleCustomDTO::isPartnerAccessModule).map(ModuleCustomDTO::getModuleId)
					.collect(Collectors.toList());
			if (XamplifyUtils.isNotEmptyList(restrictedModuleIds)) {
				moduleDao.updateModulesAccess(restrictedModuleIds, userDTO.getPartnershipId(), false);
			}
			if (XamplifyUtils.isNotEmptyList(accessedModuleIds)) {
				moduleDao.updateModulesAccess(accessedModuleIds, userDTO.getPartnershipId(), true);
			}
			logger.info("updatePartnerModulesAccess(): Partner custom modules updated successfully.");
		} else {
			Integer vendorCompanyId = userDAO.getCompanyIdByUserId(userDTO.getUserId());
			addCustomModulesForPartner(userDTO, frameCustomModulesMap(userDTO.getDefaultModules()), vendorCompanyId);
			logger.info("Partner custom modules created successfully.");
		}
	}

	private CompanyProfile createCompanyProfileAndCustomModules(UserDTO userDTO) {
		Integer vendorCompanyId = userDAO.getCompanyIdByUserId(userDTO.getUserId());
		CompanyProfile companyProfile = createCompanyProfile(userDTO, vendorCompanyId);
		userDTO.setPartnerCompanyId(companyProfile.getId());
		updateCompanyIdInUserAndPartnership(userDTO, companyProfile);
		addCustomModulesForPartner(userDTO, frameCustomModulesMap(userDTO.getDefaultModules()), vendorCompanyId);
		logger.info("Partner company profile created and custom modules updated successfully.");
		return companyProfile;
	}

	private void updateCompanyIdInUserAndPartnership(UserDTO userDTO, CompanyProfile companyProfile) {
		User user = genericDao.get(User.class, userDTO.getId());
		user.setCompanyProfile(companyProfile);
		user.setUpdatedTime(new Date());
		Partnership partnership = genericDao.get(Partnership.class, userDTO.getPartnershipId());
		partnership.setPartnerCompany(companyProfile);
		partnership.setUpdatedTime(new Date());
	}

	private CompanyProfile createCompanyProfile(UserDTO userDTO, Integer vendorCompanyId) {
		String domain = XamplifyUtils.getEmailBaseDomain(userDTO.getEmailId());
		String companyName = "";
		userDTO.setContactCompany(companyName);
		return companyProfileService.createPartnerCompany(userDTO, vendorCompanyId);
	}

	private Map<Integer, Boolean> frameCustomModulesMap(Set<ModuleCustomDTO> modules) {
		return modules.stream()
				.collect(Collectors.toMap(ModuleCustomDTO::getModuleId, ModuleCustomDTO::isPartnerAccessModule));
	}

	private void addCustomModulesForPartner(UserDTO userDTO, Map<Integer, Boolean> modulesMap,
			Integer vendorCompanyId) {
		List<com.xtremand.common.bom.Module> modules = moduleDao.findModuleNames();
		Partnership partnership = new Partnership();
		partnership.setId(userDTO.getPartnershipId());

		if (utilDao.hasVanityAccessByCompanyId(vendorCompanyId)) {
			utilService.frameAndSaveModuleCustomsForPartner(userDTO.getPartnerCompanyId(), partnership, modules,
					modulesMap);
		}
	}

	private boolean isValidUserDTO(UserDTO userDTO) {
		return userDTO != null && XamplifyUtils.isNotEmptySet(userDTO.getDefaultModules())
				&& XamplifyUtils.isValidInteger(userDTO.getPartnershipId());
	}

	@Override
	public XtremandResponse getModulesAccessGivenByVendorForPartners(String vendorCompanyProfileName,
			Integer partnerCompanyId, Integer loggedInUserId) {
		XtremandResponse xtremandResponse = new XtremandResponse();
		xtremandResponse.setStatusCode(400);
		if (!XamplifyUtils.isValidInteger(partnerCompanyId)) {
			Integer loggedInUserIdCompanyId = userDAO.getCompanyIdByUserId(loggedInUserId);
			if (XamplifyUtils.isValidInteger(loggedInUserIdCompanyId)) {
				partnerCompanyId = loggedInUserIdCompanyId;

			}
		}
		Integer vendorCompanyId = userDAO.getCompanyIdByProfileName(vendorCompanyProfileName);
		if (XamplifyUtils.isValidInteger(vendorCompanyId) && XamplifyUtils.isValidInteger(partnerCompanyId)) {
			List<ModuleCustomDTO> moduleDtos = utilDao.getModulesAccessGivenByVendorForPartners(vendorCompanyId,
					partnerCompanyId);
			xtremandResponse.setData(moduleDtos);
			xtremandResponse.setStatusCode(200);
			xtremandResponse.setMessage("Successfully Fetched Modules");
			return xtremandResponse;
		}
		return xtremandResponse;
	}

	/*** XNFR-979 *****/
	private void setInsightsAccessForXamplifyLogin(LeftSideNavigationBarItem leftSideNavigationBarItem,
			Integer userId) {
		leftSideNavigationBarItem.setInsights(utilDao.hasInsightsAccessByUserId(userId));
	}

	/*** XNFR-979 *****/
	private void setInsightsAccessForVendorVanityLogin(LeftSideNavigationBarItem leftSideNavigationBarItem,
			Integer vendorCompanyId) {
		leftSideNavigationBarItem.setInsights(utilDao.hasInsightsAccessByCompanyId(vendorCompanyId));
	}

	@Override
	public XtremandResponse loadTotalContactSubscriptionUsedByCompanyAndPartners(Integer companyId) {
		XtremandResponse response = new XtremandResponse();
		if (XamplifyUtils.isValidInteger(companyId)) {
			Integer totalContactQuotaUsed = utilDao.getTotalContactSubscriptionUsedByCompanyAndPartners(companyId);
			response.setData(totalContactQuotaUsed);
			XamplifyUtils.addSuccessStatus(response);
		}
		return response;
	}

	/*** XNFR-1062 *****/
	private void setMailEnabledAccessForXamplifyLogin(LeftSideNavigationBarItem leftSideNavigationBarItem,
			Integer userId) {
		leftSideNavigationBarItem.setMailEnable(utilDao.hasMailsEnabledAccessByUserId(userId));
	}

	/*** XNFR-1062 *****/
	private void setMailEnabledAccessForVendorVanityLogin(LeftSideNavigationBarItem leftSideNavigationBarItem,
			Integer vendorCompanyId) {
		leftSideNavigationBarItem.setMailEnable(utilDao.hasMailsEnabledAccessAccessByCompanyId(vendorCompanyId));
	}

}
