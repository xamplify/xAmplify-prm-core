package com.xtremand.guide.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.campaign.bom.ModuleAccess;
import com.xtremand.common.bom.Pagination;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.guide.dao.UserGuideDao;
import com.xtremand.guide.dto.UserGuideDashboardDto;
import com.xtremand.guide.dto.UserGuideDto;
import com.xtremand.guide.service.UserGuideService;
import com.xtremand.module.dao.ModuleDao;
import com.xtremand.module.service.ModuleService;
import com.xtremand.team.member.dto.RoleDisplayDTO;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.dto.LeftSideNavigationBarItem;
import com.xtremand.util.service.UtilService;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;

@Service
@Transactional
public class UserGuideServiceImpl implements UserGuideService {

	@Autowired
	private UserGuideDao userGuideDao;

	@Autowired
	private ModuleDao moduleDao;

	@Autowired
	private ModuleService moduleService;
	@Autowired
	private UserDAO userDao;

	@Autowired
	private UtilService utilService;

	@Autowired
	private UtilDao utilDao;

	/** For MergeTags **/
	private static final String UPLOADING_ASSETS = "upload_and_publishing_assets";
	private static final String IMPORT_SHARE_LEADS_LIST_FROM_CSV = "import_share_leads_lists_from_csv";
	private static final String MANAGE_SHARE_LEADS_LISTS = "manage_share_lead_lists";
	private static final String MANAGE_LEADS_PARTNER = "manage_leads_partner";
	private static final String MANAGE_DEALS_PARTNER = "manage_deals_partner";
	private static final String CONFIGURATION_OF_VIEW_TYPE_PARTNER = "configuration_of_view_type_partner";
	private static final String MANAGE_LEADS = "manage_leads";
	private static final String MANAGE_DEALS = "manage_deals";
	private static final String CREATING_PUBLISHING_TRACKS = "creating_and_publishing_learning_tracks";
	private static final String MANAGE_PLAYBOOKS = "manage_playbooks";
	private static final String CREATING_PUBLISHING_PLAY_BOOKS = "creating_and_publishing_play_books";
	private static final String MANAGE_TRACKS = "manage_tracks";
	private static final String ADMIN = "Admin";
	private static final String SUCCESS = "Success";
	private static final String FAILED = "Failed";
	private static final String INVALID_INPUTS = "Invalid Input";
	private static final String ACCESS_DENIED = "Access Denied";
	private static final String GUIDES_ARE_IN_PROGRESS = "Guides Are In Progress..";
	// sub module names
	private static final String SETTINGS = "Settings";
	private static final String ACCOUNT_DETAILS = "Account Details";
	private static final String DEVELOPERS = "Developers";

	@Override
	public XtremandResponse getTagIdByName(String tagName) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (StringUtils.isNotBlank(tagName)) {
			Integer tagId = userGuideDao.getTagIdByName(tagName);
			if (tagId != null) {
				UserGuideDto userGuideDto = userGuideDao.getUserGuideByTagId(tagId);
				response.setData(userGuideDto);
				responseMessage = SUCCESS;
				responseStatusCode = 200;
			} else {
				responseMessage = FAILED;
				responseStatusCode = 400;
			}
		} else {
			responseMessage = INVALID_INPUTS;
			responseStatusCode = 401;
		}
		response.setMessage(responseMessage);
		response.setStatusCode(responseStatusCode);
		return response;
	}

	@Override
	public XtremandResponse getUserGudesByModuleName(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		List<Object> moduleIds = new ArrayList<>();
		List<Object> subModuleIds = new ArrayList<>();
		List<Object> guideTitles = new ArrayList<>();
		VanityUrlDetailsDTO vanityUrlDetailsDTO = getVanityUrlDtoByPagination(pagination);
		Map<String, List<Object>> resultMap = getModuleIdsAndSubModuleIds(vanityUrlDetailsDTO);
		moduleIds = resultMap.get("moduleIds");
		subModuleIds = resultMap.get("subModuleIds");
		guideTitles = resultMap.get("guideTitles");
		String moduleName = pagination.getModuleName();
		if (StringUtils.isNotBlank(moduleName)) {
			Integer moduleId = moduleDao.getModuleIdByModuleName(moduleName);
			if (moduleIds != null && subModuleIds != null) {
				List<UserGuideDto> userGuideDtos = userGuideDao.getUserGudesByModuleId(moduleId, subModuleIds,
						guideTitles);
				response.setData(userGuideDtos);
				responseMessage = SUCCESS;
				responseStatusCode = 200;
			}
		} else {
			responseMessage = INVALID_INPUTS;
			responseStatusCode = 401;
		}
		response.setMessage(responseMessage);
		response.setStatusCode(responseStatusCode);
		return response;
	}

	private List<Object> processingGuideTitlesForSpecificRoles(List<Object> moduleIds,
			VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		List<Object> guideTitles = new ArrayList<Object>();
		Integer userId = vanityUrlDetailsDTO.getUserId();
		RoleDisplayDTO roleDisplayDto = utilService.getRoleDetailsByUserId(userId);
		if (vanityUrlDetailsDTO.isVanityUrlFilter() && roleDisplayDto.isPrmOrPrmAndPartnerCompany()
				&& moduleIds.contains(4)) {
			guideTitles.add(userGuideDao.getGuideTitleByMergeTagName(UPLOADING_ASSETS));
		}
		if (roleDisplayDto.anyPartnerRole() && (!vanityUrlDetailsDTO.isVendorLoggedInThroughOwnVanityUrl())
				|| vanityUrlDetailsDTO.isPartnerLoggedInThroughVanityUrl()) {
			guideTitles.add(userGuideDao.getGuideTitleByMergeTagName(MANAGE_LEADS_PARTNER));
			guideTitles.add(userGuideDao.getGuideTitleByMergeTagName(MANAGE_DEALS_PARTNER));
		}
		return guideTitles;
	}

	private VanityUrlDetailsDTO getVanityUrlDtoByPagination(Pagination pagination) {
		VanityUrlDetailsDTO vanityUrlDetailsDTO = new VanityUrlDetailsDTO();
		vanityUrlDetailsDTO.setUserId(pagination.getUserId());
		vanityUrlDetailsDTO.setVanityUrlFilter(pagination.isVanityUrlFilter());
		vanityUrlDetailsDTO.setVendorCompanyProfileName(utilDao.getPrmCompanyProfileName());
		return vanityUrlDetailsDTO;
	}

	@Override
	public XtremandResponse getGuideLinkByTitle(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		VanityUrlDetailsDTO vanityUrlDetailsDTO = getVanityUrlDtoByPagination(pagination);
		Map<String, List<Object>> resultMap = getModuleIdsAndSubModuleIds(vanityUrlDetailsDTO);
		List<Object> guideTitles = new ArrayList<>();
		guideTitles = resultMap.get("guideTitles");
		String title = pagination.getGuideTitle();
		if (StringUtils.isNotBlank(title)) {
			if (guideTitles.contains(title)) {
				response.setMap(userGuideDao.getUserGuideLnkByTitle(title));
				responseMessage = SUCCESS;
				responseStatusCode = 200;
			} else {
				responseMessage = GUIDES_ARE_IN_PROGRESS;
				responseStatusCode = 404;
			}

		} else {
			responseMessage = FAILED;
			responseStatusCode = 400;
		}
		response.setStatusCode(responseStatusCode);
		response.setMessage(responseMessage);
		return response;
	}

	@Override
	public XtremandResponse getUserGuideBySlug(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		String slug = pagination.getSlug();
		VanityUrlDetailsDTO vanityUrlDetailsDTO = getVanityUrlDtoByPagination(pagination);
		Map<String, List<Object>> map = getModuleIdsAndSubModuleIds(vanityUrlDetailsDTO);
		List<Object> moduleIds = map.get("moduleIds");
		List<Object> subModuleIds = map.get("subModuleIds");
		List<Object> guideTitles = map.get("guideTitles");
		// for Marketing Company
		List<Object> processingGuides = processingGuideTitlesForSpecificRoles(subModuleIds, vanityUrlDetailsDTO);
		if (StringUtils.isNotBlank(slug)) {
			if (XamplifyUtils.isValidString(slug)) {
				UserGuideDto userGuideDto = userGuideDao.getUserGuideBySlug(slug);
				if (userGuideDto != null) {
					if (userGuideDto.getSubModuleId() == null) {
						userGuideDto.setSubModuleId(0);
					}
					if (moduleIds.contains(userGuideDto.getModuleId())) {
						if (subModuleIds.contains(userGuideDto.getSubModuleId())) {
							if (guideTitles.contains(userGuideDto.getTitle())) {
								response.setData(userGuideDto);
								responseMessage = SUCCESS;
								responseStatusCode = 200;
							} else if ((processingGuides.contains(userGuideDto.getTitle()))) {
								responseMessage = GUIDES_ARE_IN_PROGRESS;
								responseStatusCode = 404;
							} else {
								responseMessage = ACCESS_DENIED;
								responseStatusCode = 403;
							}

						} else {
							responseMessage = GUIDES_ARE_IN_PROGRESS;
							responseStatusCode = 404;
						}
					}
				} else {
					responseMessage = ACCESS_DENIED;
					responseStatusCode = 403;
				}
			} else {
				responseMessage = FAILED;
				responseStatusCode = 400;
			}
		} else {
			responseMessage = INVALID_INPUTS;
			responseStatusCode = 401;
		}
		response.setMessage(responseMessage);
		response.setStatusCode(responseStatusCode);
		return response;
	}

	@Override
	public XtremandResponse getSearchResults(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (pagination != null) {
			VanityUrlDetailsDTO vanityUrlDetailsDTO = getVanityUrlDtoByPagination(pagination);
			Map<String, List<Object>> map = getModuleIdsAndSubModuleIds(vanityUrlDetailsDTO);
			List<Object> moduleIds = map.get("moduleIds");
			List<Object> subModuleIds = map.get("subModuleIds");
			List<Object> guideTitles = map.get("guideTitles");
			if (moduleIds != null) {
				Map<String, Object> guides = userGuideDao.getUserGuidesByModuleAndSubMOdules(pagination, moduleIds,
						subModuleIds, guideTitles);
				if (guides != null && guides.get("list") != null) {
					response.setData(guides);
					responseMessage = SUCCESS;
					responseStatusCode = 200;

				} else {
					responseMessage = ACCESS_DENIED;
					responseStatusCode = 403;
				}

			} else {
				responseMessage = FAILED;
				responseStatusCode = 400;
			}
		} else {
			responseMessage = INVALID_INPUTS;
			responseStatusCode = 401;
		}

		response.setMessage(responseMessage);
		response.setStatusCode(responseStatusCode);
		return response;
	}

	private Map<String, List<Object>> getModuleIdsAndSubModuleIds(VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		List<Object> moduleIds = new ArrayList<>();
		List<Object> subModuleIds = new ArrayList<>();
		List<Object> titles = new ArrayList<>();
		LeftSideNavigationBarItem leftSideNavigationBarItem = moduleService.findLeftMenuItems(vanityUrlDetailsDTO);
		Integer companyId = userDao.getCompanyIdByUserId(vanityUrlDetailsDTO.getUserId());
		ModuleAccess moduleAccess = userDao.getAccessByCompanyId(companyId);
		/********** Account Dashboard ***********/
		if (leftSideNavigationBarItem.isAccountDashboard()) {
			moduleIds.add(1);
			subModuleIds.add(0);
			accountDashboardAccess(titles, leftSideNavigationBarItem);
		}
		/********** Content ***********/
		if (leftSideNavigationBarItem.isContent()) {
			moduleIds.add(4);
			/********** DAM ************/
			damModuleAccess(vanityUrlDetailsDTO, subModuleIds, titles, leftSideNavigationBarItem);
			/*********** Learning Track ***************/
			learningTracksAccess(subModuleIds, titles, leftSideNavigationBarItem);
			/*********** Play Books ***************/
			playBooksAccess(subModuleIds, titles, leftSideNavigationBarItem);
		}
		/********** Design ***********/
		if (leftSideNavigationBarItem.isDesign()) {
			moduleIds.add(6);
			/*********** Fomrs **************/
			formAccess(subModuleIds, titles, leftSideNavigationBarItem, moduleAccess);
		}
		/********** MDF ***********/
		if (leftSideNavigationBarItem.isMdf() || leftSideNavigationBarItem.isMdfAccessAsPartner()) {
			moduleIds.add(8);
			subModuleIds.add(0);
			if (leftSideNavigationBarItem.isMdf()
					&& !(leftSideNavigationBarItem.getRoleDisplayDto().partnerOrPartnerTeamMember())) {
				titles.add(userGuideDao.getGuideTitleByMergeTagName("mdf_allocation"));
				titles.add(userGuideDao.getGuideTitleByMergeTagName("manage_mdf_requests"));
			}
		}
		/********** Opportunities ***********/
		if ((leftSideNavigationBarItem.isOpportunities()
				|| (leftSideNavigationBarItem.isOpportunitiesAccessAsPartner()))) {
			moduleIds.add(9);
			subModuleIds.add(userGuideDao.getSubModuleIdsWithName("Manage Leads"));
			subModuleIds.add(userGuideDao.getSubModuleIdsWithName("Manage Deals"));
			opportunitiesModuleTitles(titles, leftSideNavigationBarItem);
		}
		/********** Share Leads ***********/
		if (leftSideNavigationBarItem.isShareLeads()) {
			moduleIds.add(13);
			subModuleIds.add(userGuideDao.getSubModuleIdsWithName("Add Share Leads"));
			subModuleIds.add(userGuideDao.getSubModuleIdsWithName("Manage Share Lead Lists"));
			titles.add(userGuideDao.getGuideTitleByMergeTagName(IMPORT_SHARE_LEADS_LIST_FROM_CSV));
			titles.add(userGuideDao.getGuideTitleByMergeTagName(MANAGE_SHARE_LEADS_LISTS));
			titles.add(userGuideDao.getGuideTitleByMergeTagName("import_share_leads_connectwise"));

		}
		/********** Shared Leads ***********/
		if (leftSideNavigationBarItem.isSharedLeads()) {
			moduleIds.add(14);
			subModuleIds.add(userGuideDao.getSubModuleIdsWithName("Shared Leads"));
			titles.add(userGuideDao.getGuideTitleByMergeTagName("using_shared_leads"));
		}
		/********** Team ***********/
		if (leftSideNavigationBarItem.isTeam()) {
			moduleIds.add(16);
			subModuleIds.add(userGuideDao.getSubModuleIdsWithName("Add And Manage Team Members"));
			RoleDisplayDTO roleDto = leftSideNavigationBarItem.getRoleDisplayDto();
			boolean isPartnerVanity = vanityUrlDetailsDTO.isPartnerLoggedInThroughVanityUrl();
			boolean isLoggedInVendorVanity = leftSideNavigationBarItem.isLoggedInThroughVendorVanityUrl();
			boolean isVendorOwnVanity = vanityUrlDetailsDTO.isVendorLoggedInThroughOwnVanityUrl();
			boolean isAnyPartnerRole = roleDto.anyPartnerRole();
			if (roleDto.partnerOrPartnerTeamMember() || isPartnerVanity || isLoggedInVendorVanity) {
				titles.add(userGuideDao.getGuideTitleByMergeTagName("adding_team_members_partner"));
				titles.add(userGuideDao.getGuideTitleByMergeTagName("team_member_analytics_partner"));
			} else {
				titles.add(userGuideDao.getGuideTitleByMergeTagName("add_and_manage_team_members"));
				// XNFR-991
				if (roleDto.isPrmOrPrmAndPartnerCompany()) {
					titles.add(userGuideDao.getGuideTitleByMergeTagName("team_member_analytics_prm"));
				}

				if (isAnyPartnerRole && !isVendorOwnVanity) {
					titles.add(userGuideDao.getGuideTitleByMergeTagName("team_member_analytics_partner"));
				}
			}
		}

		/********** Forms ***********/
		if (leftSideNavigationBarItem.isForms()) {
			moduleIds.add(7);
		}
		/******** Configuration ************/
		leftSideNavigationBarItem.setConfiguration(true);
		moduleIds.add(19);
		if (leftSideNavigationBarItem.isConfiguration()) {
			userGuidesForAccountDetailsTab(subModuleIds, titles);
			/***** Settings Tab **********/
			userGuidesForSettingsTab(subModuleIds, titles, leftSideNavigationBarItem, moduleAccess);
			/**** Lead/Deal Configuration *****/
			userGuidesForLeadOrDealConfiguration(subModuleIds, titles, leftSideNavigationBarItem);
			/**** DEVELOPERS ****/
			userGuidesForDeveloperTab(subModuleIds, titles, leftSideNavigationBarItem);
			/***** Admin *****/
			userGuidesForAdminTab(vanityUrlDetailsDTO, subModuleIds, titles, leftSideNavigationBarItem, moduleAccess);
		}
		Map<String, List<Object>> map = new HashMap<>();
		map.put("moduleIds", moduleIds);
		map.put("subModuleIds", subModuleIds);
		map.put("guideTitles", titles);
		return map;
	}

	/******** 20-02-2024 ********/
	private void userGuidesForAdminTab(VanityUrlDetailsDTO vanityUrlDetailsDTO, List<Object> subModuleIds,
			List<Object> titles, LeftSideNavigationBarItem leftSideNavigationBarItem, ModuleAccess moduleAccess) {
		subModuleIds.add(userGuideDao.getSubModuleIdsWithName(ADMIN));
		if (vanityUrlDetailsDTO.isVanityUrlFilter()
				&& leftSideNavigationBarItem.getRoleDisplayDto().isAnyAdminOrSuperVisor()) {
			if (leftSideNavigationBarItem.isLoggedInThroughOwnVanityUrl()) {
				titles.add(userGuideDao.getGuideTitleByMergeTagName("add_dashboard_buttons"));
				titles.add(userGuideDao.getGuideTitleByMergeTagName("dashboard_banners"));
				titles.add(userGuideDao.getGuideTitleByMergeTagName("news_and_announcements"));
			}
			titles.add(userGuideDao.getGuideTitleByMergeTagName("your_templates"));
		}
		if (leftSideNavigationBarItem.getRoleDisplayDto().isAnyAdminOrSuperVisor()) {
			titles.add(userGuideDao.getGuideTitleByMergeTagName("unsubscribe_reasons"));
		}
		if (moduleAccess.isExcludeUsersOrDomains() && (leftSideNavigationBarItem.isAdminOrSuperVisor()
				|| leftSideNavigationBarItem.getRoleDisplayDto().isPartner())) {
			titles.add(userGuideDao.getGuideTitleByMergeTagName("exclusion_lists_configuration"));
		}
		if (leftSideNavigationBarItem.isFolders()) {
			titles.add(userGuideDao.getGuideTitleByMergeTagName("creating_folders"));
		}
		if (leftSideNavigationBarItem.getRoleDisplayDto().isAnyAdminOrSuperVisor()) {
			if (leftSideNavigationBarItem.getRoleDisplayDto().partnerOrPartnerTeamMember()) {
				titles.add(userGuideDao.getGuideTitleByMergeTagName("creating_team_member_groups_partner"));
			} else if (leftSideNavigationBarItem.getRoleDisplayDto().isPrmOrPrmAndPartnerCompany()) {
				titles.add(userGuideDao.getGuideTitleByMergeTagName("create_team_member_groups"));
			}
		}
		if (leftSideNavigationBarItem.isEmailNotificationSettings()) {
			titles.add(userGuideDao.getGuideTitleByMergeTagName("email_notification_settings"));
		}
		if (leftSideNavigationBarItem.getRoleDisplayDto().isAnyAdminOrSuperVisor()) {
			if (leftSideNavigationBarItem.getRoleDisplayDto().partnerOrPartnerTeamMember()) {
				titles.add(userGuideDao.getGuideTitleByMergeTagName("domain_whitelisting_partner"));
			} else {
				titles.add(userGuideDao.getGuideTitleByMergeTagName("domain_whitelisting"));
			}
		}
	}

	private void userGuidesForDeveloperTab(List<Object> subModuleIds, List<Object> titles,
			LeftSideNavigationBarItem leftSideNavigationBarItem) {
		subModuleIds.add(userGuideDao.getSubModuleIdsWithName(DEVELOPERS));
		titles.add(userGuideDao.getGuideTitleByMergeTagName("spf_configuration"));
		titles.add(userGuideDao.getGuideTitleByMergeTagName("godaddy_spf_configuration"));
		/**** Pending with gdpr setting option *******/
		RoleDisplayDTO roleDisplayDto = leftSideNavigationBarItem.getRoleDisplayDto();
		if ((roleDisplayDto.isPrmOrPrmAndPartnerCompany() || leftSideNavigationBarItem.isOnlyPartnerRole()
				|| roleDisplayDto.getRole().equalsIgnoreCase("Partner"))) {
			titles.add(userGuideDao.getGuideTitleByMergeTagName("gdpr_setting_partner"));
		}
		if (roleDisplayDto.isAnyAdminOrSuperVisor())
			userGuidesForIntegrations(titles);
	}

	private void userGuidesForIntegrations(List<Object> titles) {
		titles.add(userGuideDao.getGuideTitleByMergeTagName("marketo_integration"));
		titles.add(userGuideDao.getGuideTitleByMergeTagName("hubspot_integration"));
		titles.add(userGuideDao.getGuideTitleByMergeTagName("microsoft_integration"));
		titles.add(userGuideDao.getGuideTitleByMergeTagName("pipedrive_integration"));
		titles.add(userGuideDao.getGuideTitleByMergeTagName("connectwise_integration"));
		titles.add(userGuideDao.getGuideTitleByMergeTagName("salesforce_integration"));
		titles.add(userGuideDao.getGuideTitleByMergeTagName("halopsa_integration"));
		titles.add(userGuideDao.getGuideTitleByMergeTagName("zoho_integration"));
	}

	private void userGuidesForLeadOrDealConfiguration(List<Object> subModuleIds, List<Object> titles,
			LeftSideNavigationBarItem leftSideNavigationBarItem) {
		subModuleIds.add(userGuideDao.getSubModuleIdsWithName("Lead And Deal Configuration"));
		boolean isOpportunities = leftSideNavigationBarItem.isOpportunities();
		boolean isAnyAdminOrSuperVisor = leftSideNavigationBarItem.getRoleDisplayDto().isAnyAdminOrSuperVisor();
		if ((isOpportunities || leftSideNavigationBarItem.isOpportunitiesAccessAsPartner()) && isAnyAdminOrSuperVisor) {
			if (isOpportunities) {
				titles.add(userGuideDao.getGuideTitleByMergeTagName("deal_registration"));
			}
			titles.add(userGuideDao.getGuideTitleByMergeTagName("lead_pipelines"));
			titles.add(userGuideDao.getGuideTitleByMergeTagName("deal_pipelines"));
			if (leftSideNavigationBarItem.isAdmin()
					&& !leftSideNavigationBarItem.getRoleDisplayDto().partnerOrPartnerTeamMember()) {
				titles.add(userGuideDao.getGuideTitleByMergeTagName("lead_approval_or_rejection"));
			}

			if (!leftSideNavigationBarItem.getRoleDisplayDto().partnerOrPartnerTeamMember()) {
				titles.add(userGuideDao.getGuideTitleByMergeTagName("custom_fields_settings"));
			}
		}
	}

	private void userGuidesForSettingsTab(List<Object> subModuleIds, List<Object> titles,
			LeftSideNavigationBarItem leftSideNavigationBarItem, ModuleAccess moduleAccess) {
		subModuleIds.add(userGuideDao.getSubModuleIdsWithName(SETTINGS));
		/***** custom_blocks *******/
		if (leftSideNavigationBarItem.getRoleDisplayDto().isAnyAdminOrSuperVisor()
				&& !leftSideNavigationBarItem.getRoleDisplayDto().partnerOrPartnerTeamMember()) {
			titles.add(userGuideDao.getGuideTitleByMergeTagName("custom_blocks"));
		}
		/************* View Type *********/
		if (!leftSideNavigationBarItem.getRoleDisplayDto().isUser()) {
			if (leftSideNavigationBarItem.isLoggedInThroughXamplifyUrl()) {
				titles.add(userGuideDao.getGuideTitleByMergeTagName(CONFIGURATION_OF_VIEW_TYPE_PARTNER));
			} else {
				titles.add(userGuideDao.getGuideTitleByMergeTagName("view_type_vanity_account"));
			}
		}

		/******** Create Tag **********/
		if (leftSideNavigationBarItem.isLms() || leftSideNavigationBarItem.isPlaybook()) {
			titles.add(userGuideDao.getGuideTitleByMergeTagName("create_tags"));
		}

		/****** customize_left_menu ******/
		if (leftSideNavigationBarItem.getRoleDisplayDto().isAnyAdminOrSuperVisor()) {
			if (leftSideNavigationBarItem.getRoleDisplayDto().partnerOrPartnerTeamMember()) {
				titles.add(userGuideDao.getGuideTitleByMergeTagName("customize_left_menu_partner"));
			} else if (leftSideNavigationBarItem.getRoleDisplayDto().formAccessForPrm()) {
				titles.add(userGuideDao.getGuideTitleByMergeTagName("customize_left_menu_prm"));
			}
		}

		if (moduleAccess.isCustomSkinSettings() && !leftSideNavigationBarItem.isLoggedInThroughVendorVanityUrl()
				&& leftSideNavigationBarItem.getRoleDisplayDto().isAnyAdminOrSuperVisor()) {
			titles.add(userGuideDao.getGuideTitleByMergeTagName("create_and_activate_theme"));
		}
		if (leftSideNavigationBarItem.isLoggedInThroughOwnVanityUrl()
				&& leftSideNavigationBarItem.isAdminOrSuperVisor()) {
			titles.add(userGuideDao.getGuideTitleByMergeTagName("custom_login_screen_settings"));
		}
	}

	private void userGuidesForAccountDetailsTab(List<Object> subModuleIds, List<Object> titles) {
		subModuleIds.add(userGuideDao.getSubModuleIdsWithName(ACCOUNT_DETAILS));
		titles.add(userGuideDao.getGuideTitleByMergeTagName("configuration_of_account_details"));
		titles.add(userGuideDao.getGuideTitleByMergeTagName("digital_signature"));
	}

	private void formAccess(List<Object> subModuleIds, List<Object> titles,
			LeftSideNavigationBarItem leftSideNavigationBarItem, ModuleAccess moduleAccess) {
		if (leftSideNavigationBarItem.isForms() || leftSideNavigationBarItem.getRoleDisplayDto().isPrmTeamMember()) {
			subModuleIds.add(userGuideDao.getSubModuleIdsWithName("Design Forms"));
			subModuleIds.add(userGuideDao.getSubModuleIdsWithName("Manage Forms"));
			titles.add(userGuideDao.getGuideTitleByMergeTagName("design_forms"));
			titles.add(userGuideDao.getGuideTitleByMergeTagName("designing_quiz_form"));
			titles.add(userGuideDao.getGuideTitleByMergeTagName("designing_survey_form"));
			titles.add(userGuideDao.getGuideTitleByMergeTagName("manage_form"));

		}
	}

	private void accountDashboardAccess(List<Object> titles, LeftSideNavigationBarItem leftSideNavigationBarItem) {
		if (leftSideNavigationBarItem.isLoggedInThroughVendorVanityUrl()) {
			if (leftSideNavigationBarItem.isPartnershipEstablishedOnlyWithPrmAndLoggedInAsPartner()) {
				titles.add(userGuideDao.getGuideTitleByMergeTagName("vanity_prm_partner_account_dashboard"));
			} else {
				titles.add(userGuideDao.getGuideTitleByMergeTagName("vanity_partner_account_dashboard"));
			}
		} else {
			if (leftSideNavigationBarItem.getRoleDisplayDto().isPrmOrPrmAndPartnerCompany()) {
				if (leftSideNavigationBarItem.isLoggedInThroughOwnVanityUrl()) {
					titles.add(userGuideDao.getGuideTitleByMergeTagName("vanity_prm_account_dashboard"));
				} else {
					titles.add(userGuideDao.getGuideTitleByMergeTagName("prm_account_dashboard"));
				}
			} else if (leftSideNavigationBarItem.getRoleDisplayDto().partnerOrPartnerTeamMember()) {
				titles.add(userGuideDao.getGuideTitleByMergeTagName("partner_account_dashboard"));
			}
		}
	}

	private void playBooksAccess(List<Object> subModuleIds, List<Object> titles,
			LeftSideNavigationBarItem leftSideNavigationBarItem) {
		if (leftSideNavigationBarItem.isPlaybook() || leftSideNavigationBarItem.isPlaybookAccessAsPartner()) {
			if (leftSideNavigationBarItem.isPlaybook()) {
				subModuleIds.add(userGuideDao.getSubModuleIdsWithName("Play Book"));
				titles.add(userGuideDao.getGuideTitleByMergeTagName(CREATING_PUBLISHING_PLAY_BOOKS));
				titles.add(userGuideDao.getGuideTitleByMergeTagName(MANAGE_PLAYBOOKS));
			}
			if (leftSideNavigationBarItem.isPlaybookAccessAsPartner()) {
				subModuleIds.add(userGuideDao.getSubModuleIdsWithName("Access Shared PlayBooks"));
				titles.add(userGuideDao.getGuideTitleByMergeTagName("access_shared_playbooks"));

			}
		}
	}

	private void learningTracksAccess(List<Object> subModuleIds, List<Object> titles,
			LeftSideNavigationBarItem leftSideNavigationBarItem) {
		if (leftSideNavigationBarItem.isLms() || leftSideNavigationBarItem.isLmsAccessAsPartner()) {
			if (leftSideNavigationBarItem.isLms()) {
				subModuleIds.add(userGuideDao.getSubModuleIdsWithName("Track Builder"));
				if (leftSideNavigationBarItem.getRoleDisplayDto().isPrmOrPrmAndPartnerCompany()) {
					titles.add(userGuideDao.getGuideTitleByMergeTagName(CREATING_PUBLISHING_TRACKS));
					titles.add(userGuideDao.getGuideTitleByMergeTagName(MANAGE_TRACKS));
				}
			}
			if (leftSideNavigationBarItem.isLmsAccessAsPartner()) {
				subModuleIds.add(userGuideDao.getSubModuleIdsWithName("Access Shared Tracks"));
				titles.add(userGuideDao.getGuideTitleByMergeTagName("access_shared_tracks"));

			}
		}
	}

	private void damModuleAccess(VanityUrlDetailsDTO vanityUrlDetailsDTO, List<Object> subModuleIds,
			List<Object> titles, LeftSideNavigationBarItem leftSideNavigationBarItem) {
		if (leftSideNavigationBarItem.isDam() || leftSideNavigationBarItem.isDamAccessAsPartner()) {
			if (leftSideNavigationBarItem.isDam()) {
				subModuleIds.add(userGuideDao.getSubModuleIdsWithName("Asset Library"));
				if (vanityUrlDetailsDTO.isVanityUrlFilter()
						&& leftSideNavigationBarItem.getRoleDisplayDto().isPrmOrPrmAndPartnerCompany()) {

				} else {
					titles.add(userGuideDao.getGuideTitleByMergeTagName(UPLOADING_ASSETS));
					titles.add(userGuideDao.getGuideTitleByMergeTagName("manage_assets"));
				}
			}
			if (leftSideNavigationBarItem.isDamAccessAsPartner()) {
				subModuleIds.add(userGuideDao.getSubModuleIdsWithName("Access Shared Assets"));
				titles.add(userGuideDao.getGuideTitleByMergeTagName("accessing_shared_assets"));
			}
		}
	}

	private void opportunitiesModuleTitles(List<Object> titles, LeftSideNavigationBarItem leftSideNavigationBarItem) {
		if (leftSideNavigationBarItem.isLoggedInThroughVendorVanityUrl()) {
			titles.add(userGuideDao.getGuideTitleByMergeTagName(MANAGE_LEADS_PARTNER));
			titles.add(userGuideDao.getGuideTitleByMergeTagName(MANAGE_DEALS_PARTNER));
		} else if (leftSideNavigationBarItem.isLoggedInThroughOwnVanityUrl()) {
			titles.addAll(getTitles(leftSideNavigationBarItem, titles));
		} else {
			if (leftSideNavigationBarItem.getRoleDisplayDto().isPrmOrPrmAndPartnerCompany()) {
				titles.add(userGuideDao.getGuideTitleByMergeTagName(MANAGE_LEADS));
				titles.add(userGuideDao.getGuideTitleByMergeTagName(MANAGE_DEALS));
			}
			if (leftSideNavigationBarItem.isLoggedInThroughXamplifyUrl()
					|| leftSideNavigationBarItem.isLoggedInThroughVendorVanityUrl()) {
				titles.addAll(getTitles(leftSideNavigationBarItem, titles));
			}
		}
	}

	private List<Object> getTitles(LeftSideNavigationBarItem leftSideNavigationBarItem, List<Object> titles) {
		if (leftSideNavigationBarItem.getRoleDisplayDto().partnerOrPartnerTeamMember()
				|| (leftSideNavigationBarItem.getRoleDisplayDto().isAdminWithPartnerCompany()
						|| leftSideNavigationBarItem.getRoleDisplayDto().isAdminAndPartnerTeamMember())) {
			if (leftSideNavigationBarItem.isLoggedInThroughOwnVanityUrl()
					&& (leftSideNavigationBarItem.getRoleDisplayDto().isPrmOrPrmAndPartnerCompany())) {
				titles.add(userGuideDao.getGuideTitleByMergeTagName(MANAGE_LEADS));
				titles.add(userGuideDao.getGuideTitleByMergeTagName(MANAGE_DEALS));
			} else {
				titles.add(userGuideDao.getGuideTitleByMergeTagName(MANAGE_LEADS_PARTNER));
				titles.add(userGuideDao.getGuideTitleByMergeTagName(MANAGE_DEALS_PARTNER));
			}
		} else {
			titles.add(userGuideDao.getGuideTitleByMergeTagName(MANAGE_LEADS));
			titles.add(userGuideDao.getGuideTitleByMergeTagName(MANAGE_DEALS));
		}
		return titles;
	}

	@Override
	public XtremandResponse getUserRolesForDashBoard(VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (vanityUrlDetailsDTO.getUserId() != null) {
			Integer userId = vanityUrlDetailsDTO.getUserId();
			UserGuideDashboardDto dashboardDto = new UserGuideDashboardDto();
			utilService.isVanityUrlFilterApplicable(vanityUrlDetailsDTO);
			boolean isLoggedInThroughVendorVanityUrl = vanityUrlDetailsDTO.isPartnerLoggedInThroughVanityUrl();
			dashboardDto.setPartnerLoggedInThroughVanityUrl(isLoggedInThroughVendorVanityUrl);
			boolean isPrmByVendorCompanyId = utilDao.isPrmByVendorCompanyId(
					userDao.getCompanyIdByProfileName(vanityUrlDetailsDTO.getVendorCompanyProfileName()));
			boolean isPrmCompany = utilDao.isPrmCompany(userId) || isPrmByVendorCompanyId;
			boolean isLoggedInThroughOwnVanityUrl = vanityUrlDetailsDTO.isVendorLoggedInThroughOwnVanityUrl();
			dashboardDto.setPrmCompany(isPrmCompany);
			dashboardDto.setVendorLoggedInThroughOwnVanityUrl(isLoggedInThroughOwnVanityUrl);
			response.setData(dashboardDto);
			responseMessage = SUCCESS;
			responseStatusCode = 200;
		}
		response.setMessage(responseMessage);
		response.setStatusCode(responseStatusCode);
		return response;
	}

	@Override
	public XtremandResponse getModuleNameByModuleId(Integer moduleId) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (moduleId != null) {
			String moduleName = userGuideDao.getModuleNameByModuleId(moduleId);
			if (moduleName != null) {
				response.setData(moduleName);
				responseMessage = SUCCESS;
				responseStatusCode = 200;
			} else {
				responseMessage = FAILED;
				responseStatusCode = 401;
			}
		} else {
			responseMessage = INVALID_INPUTS;
			responseStatusCode = 400;
		}
		response.setMessage(responseMessage);
		response.setStatusCode(responseStatusCode);
		return response;
	}

}
