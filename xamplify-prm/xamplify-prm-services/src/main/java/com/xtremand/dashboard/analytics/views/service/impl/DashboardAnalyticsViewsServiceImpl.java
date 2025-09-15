package com.xtremand.dashboard.analytics.views.service.impl;

import static java.util.Arrays.asList;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;

import com.xtremand.common.bom.Pagination;
import com.xtremand.dashboard.analytics.views.bom.DashboardModuleAnalyticsView;
import com.xtremand.dashboard.analytics.views.bom.EmailStatsVanityUrlView;
import com.xtremand.dashboard.analytics.views.bom.EmailStatsView;
import com.xtremand.dashboard.analytics.views.bom.OpportunitiesPartnerAnalyticsView;
import com.xtremand.dashboard.analytics.views.bom.OpportunitiesVanityUrlPartnerAnalyticsView;
import com.xtremand.dashboard.analytics.views.bom.OpportunitiesVendorAnalyticsView;
import com.xtremand.dashboard.analytics.views.bom.RegionalStatisticsVanityUrlView;
import com.xtremand.dashboard.analytics.views.bom.RegionalStatisticsView;
import com.xtremand.dashboard.analytics.views.bom.VendorEmailStatsView;
import com.xtremand.dashboard.analytics.views.bom.VendorRegionalStatisticsView;
import com.xtremand.dashboard.analytics.views.dao.DashboardAnalyticsViewsDao;
import com.xtremand.dashboard.analytics.views.dto.DashboardModuleAnalyticsViewDTO;
import com.xtremand.dashboard.analytics.views.dto.PartnerAnalyticsCountDTO;
import com.xtremand.dashboard.analytics.views.dto.WordCloudMapDTO;
import com.xtremand.dashboard.analytics.views.service.DashboardAnalyticsViewsService;
import com.xtremand.form.dao.FormDao;
import com.xtremand.formbeans.EmailLogReport;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.log.bom.VideoStats;
import com.xtremand.log.service.XamplifyLogService;
import com.xtremand.module.dao.ModuleDao;
import com.xtremand.team.dao.TeamDao;
import com.xtremand.team.member.dto.RoleDisplayDTO;
import com.xtremand.user.bom.Role;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.userlist.service.UserListService;
import com.xtremand.util.DateUtils;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.dto.AccessDetailsDTO;
import com.xtremand.util.dto.CompanyDTO;
import com.xtremand.util.dto.LeftSideNavigationBarItem;
import com.xtremand.util.dto.ModuleCustomDTO;
import com.xtremand.util.dto.Pageable;
import com.xtremand.util.dto.PaginatedDTO;
import com.xtremand.util.dto.TeamMemberFilterDTO;
import com.xtremand.util.dto.XamplifyConstants;
import com.xtremand.util.dto.XamplifyUtilValidator;
import com.xtremand.util.service.UtilService;
import com.xtremand.validator.PageableValidator;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;

@Service
@Transactional
public class DashboardAnalyticsViewsServiceImpl implements DashboardAnalyticsViewsService {

	@Autowired
	private DashboardAnalyticsViewsDao dashboardAnalyticsViewsDao;

	@Autowired
	private UtilService utilService;

	@Autowired
	private UtilDao utilDao;

	@Autowired
	private UserDAO userDao;

	@Autowired
	XamplifyLogService xamplifyLogService;

	@Autowired
	private FormDao formDao;

	@Autowired
	UserListService userListService;

	@Autowired
	private ModuleDao moduleDao;

	@Autowired
	private TeamDao teamDao;

	@Autowired
	private PageableValidator pageableValidator;

	@Autowired
	private XamplifyUtilValidator xamplifyUtilValidator;

	@Value("${campaigns.module}")
	private String campaigns;

	@Value("${campaigns.icon}")
	private String campaignIcon;

	@Value("${campaigns.color}")
	private String campaignColor;

	@Value("${partner.analytics.module}")
	private String partnerAnalytics;

	@Value("${partner.analytics.icon}")
	private String partnerAnalyticsIcon;

	@Value("${partner.analytics.color}")
	private String partnerAnalyticsColor;

	@Value("${uploaded.videos.module}")
	private String uploadedVideos;

	@Value("${uploaded.videos.icon}")
	private String uploadedVideosIcon;

	@Value("${uploaded.videos.color}")
	private String uploadedVideosColor;

	@Value("${vendors.module}")
	private String vendors;

	@Value("${vendors.icon}")
	private String vendorsIcon;

	@Value("${vendors.color}")
	private String vendorsColor;

	@Value("${contacts.module}")
	private String contacts;

	@Value("${contacts.icon}")
	private String contactsIcon;

	@Value("${contacts.color}")
	private String contactsColor;

	@Value("${partners.module}")
	private String partners;

	@Value("${partners.icon}")
	private String partnersIcon;

	@Value("${partners.color}")
	private String partnersColor;

	@Value("${forms.module}")
	private String forms;

	@Value("${forms.icon}")
	private String formsIcon;

	@Value("${forms.color}")
	private String formsColor;

	@Value("${social.accounts.module}")
	private String socialAccounts;

	@Value("${social.accounts.icon}")
	private String socialAccountsIcon;

	@Value("${social.accounts.color}")
	private String socialAccountsColor;

	@Value("${email.templates.module}")
	private String emailTemplates;

	@Value("${email.templates.icon}")
	private String emailTemplatesIcon;

	@Value("${email.templates.color}")
	private String emailTemplatesColor;

	@Value("${team.members.module}")
	private String teamMembers;

	@Value("${team.members.icon}")
	private String teamMembersIcon;

	@Value("${team.members.color}")
	private String teamMembersColor;

	@Value("${dashboard.access.denied}")
	private String accessDeniedClassName;

	@Value("${email.campaign.name}")
	private String emailCampaign;

	@Value("${email.campaign.image}")
	private String emailCampaignImagePath;

	@Value("${video.campaign.name}")
	private String videoCampaign;

	@Value("${video.campaign.image}")
	private String videoCampaignImagePath;

	@Value("${social.campaign.name}")
	private String socialCampaign;

	@Value("${social.campaign.image}")
	private String socialCampaignImagePath;

	@Value("${event.campaign.name}")
	private String eventCampaign;

	@Value("${event.campaign.image}")
	private String eventCampaignImagePath;

	@Value("${page.campaign.name}")
	private String pageCampaign;

	@Value("${page.campaign.image}")
	private String pageCampaignImagePath;

	@Value("${survey.campaign.name}")
	private String surveyCampaign;

	@Value("${survey.campaign.image}")
	private String surveyCampaignImagePath;

//	#XNFR-736
	@Value("${campiagn.tile.info}")
	private String campiagnTileInfo;

	@Value("${partneranalytics.tile.info}")
	private String partneranalyticsTileInfo;

	@Value("${templates.Tile.Info}")
	private String templatesTileInfo;

	@Value("${teammember.Tile.Info}")
	private String teammemberTileInfo;

	@Value("${partners.Campaign.Tile.Info}")
	private String partnersCampaignTileInfo;

	@Value("${contacts.Tile.Info}")
	private String contactsTileInfo;

	@Value("${partners.Tile.Info}")
	private String partnersTileInfo;

	@Value("${forms.Tile.Info}")
	private String formsTileInfo;

	@Value("${vendors.Tile.Info}")
	private String vendorsTileInfo;

	@Value("${teammember.campiagns.tile.info}")
	private String teammemberCampiagnsTileInfo;

	@Override
	public XtremandResponse getDashboardModuleViewByCompanyId(VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		List<DashboardModuleAnalyticsViewDTO> dtos = new ArrayList<>();
		/**** XNFR-252 ****/
		Integer loginAsUserId = vanityUrlDetailsDTO.getLoginAsUserId();
		boolean loginAsPartner = loginAsUserId != null && loginAsUserId > 0;
		Integer loginAsPartnerCompanyId = 0;
		if (loginAsPartner) {
			loginAsPartnerCompanyId = userDao.getCompanyIdByUserId(loginAsUserId);
		}

		DashboardModuleAnalyticsView dashboardAnalyticsView = dashboardAnalyticsViewsDao
				.getDashboardModuleViewByCompanyId(vanityUrlDetailsDTO);
		if (dashboardAnalyticsView == null) {
			dashboardAnalyticsView = new DashboardModuleAnalyticsView();
		}
		/*** This method will set the values for click/disable the userclick *********/
		AccessDetailsDTO accessDetailsDTO = utilService.getAccessDetails(vanityUrlDetailsDTO.getUserId());
		utilService.isVanityUrlFilterApplicable(vanityUrlDetailsDTO);
		boolean isPartnerLoggedInThroughVanityUrl = vanityUrlDetailsDTO.isPartnerLoggedInThroughVanityUrl();
		boolean isPartnershipEstablishedOnlyWithPrm = false;
		isPartnershipEstablishedOnlyWithPrm = checkPartnership(vanityUrlDetailsDTO, loginAsPartner,
				loginAsPartnerCompanyId, accessDetailsDTO);

		/************ XNFR-219 *********/
		getPartnerAnalyticsCount(dtos, dashboardAnalyticsView, accessDetailsDTO, isPartnerLoggedInThroughVanityUrl,
				vanityUrlDetailsDTO.getUserId(), vanityUrlDetailsDTO.isApplyFilter());
		/******* Vendors ******/
		getVendorsCount(vanityUrlDetailsDTO, dtos, loginAsPartner, dashboardAnalyticsView, accessDetailsDTO,
				isPartnershipEstablishedOnlyWithPrm);

		/************ XNFR-219 *********/
		/******* Email Templates & Team Members ************/
		teamMembersCount(dashboardAnalyticsView, accessDetailsDTO, dtos, vanityUrlDetailsDTO);

		/******* Partners & Forms For PRM ****************/
		getPartnersAndFormsCount(vanityUrlDetailsDTO, dtos, dashboardAnalyticsView, accessDetailsDTO);

		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(200);
		response.setData(dtos);
		return response;
	}

	private boolean checkPartnership(VanityUrlDetailsDTO vanityUrlDetailsDTO, boolean loginAsPartner,
			Integer loginAsPartnerCompanyId, AccessDetailsDTO accessDetailsDTO) {
		boolean isPartnershipEstablishedOnlyWithPrm;
		if (loginAsPartner) {
			isPartnershipEstablishedOnlyWithPrm = utilDao.isPrmByVendorCompanyId(loginAsPartnerCompanyId);
		} else {
			isPartnershipEstablishedOnlyWithPrm = utilDao
					.isPartnershipEstablishedOnlyWithPrm(vanityUrlDetailsDTO.getUserId())
					&& (accessDetailsDTO.isPartner() || accessDetailsDTO.isPartnerTeamMember());
		}
		return isPartnershipEstablishedOnlyWithPrm;
	}

	private void getPartnersAndFormsCount(VanityUrlDetailsDTO vanityUrlDetailsDTO,
			List<DashboardModuleAnalyticsViewDTO> dtos, DashboardModuleAnalyticsView dashboardAnalyticsView,
			AccessDetailsDTO accessDetailsDTO) {
		boolean isAnyPrm = accessDetailsDTO.isPrm() || accessDetailsDTO.isPrmTeamMember()
				|| accessDetailsDTO.isPrmAndPartner() || accessDetailsDTO.isPrmAndPartnerTeamMember();
		if (isAnyPrm && !vanityUrlDetailsDTO.isPartnerLoggedInThroughVanityUrl()) {
			Integer userId = vanityUrlDetailsDTO.getUserId();
			boolean formAccess = accessDetailsDTO.isPrm() || accessDetailsDTO.isPrmAndPartner();
			boolean partnersAccess = accessDetailsDTO.isPrm() || accessDetailsDTO.isPrmAndPartner()
					|| accessDetailsDTO.isPartnerAccess();
			Integer partnersCount = dashboardAnalyticsView.getPartnerAnalytics() != null
					? dashboardAnalyticsView.getPartnerAnalytics().intValue()
					: 0;
			partnersCount = applyPartnersCountFilterForTeamMember(userId, partnersCount,
					vanityUrlDetailsDTO.isApplyFilter());
			dtos.add(setModuleAnalyticsDetails(partnersCount, partnersAccess, partners, partnersIcon, partnersColor, 9,
					partnersTileInfo));
			dtos.add(setModuleAnalyticsDetails(formDao.findFormsCountByUserId(userId), formAccess, forms, formsIcon,
					formsColor, 10, formsTileInfo));
		}
	}

	private void getVendorsCount(VanityUrlDetailsDTO vanityUrlDetailsDTO, List<DashboardModuleAnalyticsViewDTO> dtos,
			boolean loginAsPartner, DashboardModuleAnalyticsView dashboardAnalyticsView,
			AccessDetailsDTO accessDetailsDTO, boolean isPartnershipEstablishedOnlyWithPrm) {
		if (loginAsPartner) {
			dtos.add(setModuleAnalyticsDetails(1, true, vendors, vendorsIcon, vendorsColor, 4, vendorsTileInfo));
		} else {
			setVendorModuleCount(dashboardAnalyticsView, accessDetailsDTO, dtos, vanityUrlDetailsDTO,
					isPartnershipEstablishedOnlyWithPrm);
		}
	}

	private void getPartnerAnalyticsCount(List<DashboardModuleAnalyticsViewDTO> dtos,
			DashboardModuleAnalyticsView dashboardAnalyticsView, AccessDetailsDTO accessDetailsDTO,
			boolean isPartnerLoggedInThroughVanityUrl, Integer userId, boolean applyFilter) {
		if (accessDetailsDTO.isPartnerAnalyticsAccess() && !isPartnerLoggedInThroughVanityUrl) {
			/******* Partner Analytics ******/
			/*******
			 * If logged in user do not have access for campaigns then he can't click on
			 * partner analytics
			 ******/
			/**** XNFR-84 *****/
			ModuleCustomDTO moduleCustomNameDTO = moduleDao
					.findPartnerModuleByCompanyId(dashboardAnalyticsView.getCompanyId());
			String moduleName = partnerAnalytics;
			String description = partneranalyticsTileInfo;
			if (moduleCustomNameDTO != null) {
				moduleName = moduleCustomNameDTO.getCustomName() + " Analytics";
				description = moduleCustomNameDTO.getCustomName().equalsIgnoreCase("partner") ? partneranalyticsTileInfo
						: description.replace("Partners", moduleCustomNameDTO.getCustomName());
			}
			Integer partnersCount = dashboardAnalyticsView.getPartnerAnalytics() != null
					? dashboardAnalyticsView.getPartnerAnalytics().intValue()
					: 0;

			partnersCount = applyPartnersCountFilterForTeamMember(userId, partnersCount, applyFilter);
			dtos.add(setModuleAnalyticsDetails(partnersCount, accessDetailsDTO.isPartnerAnalyticsAccess(), moduleName,
					partnerAnalyticsIcon, partnerAnalyticsColor, 2, description));
		}
	}

	private Integer applyPartnersCountFilterForTeamMember(Integer userId, Integer partnersCount, boolean applyFilter) {
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(userId, applyFilter, true);
		boolean applyTeamMemberFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
		if (teamMemberFilterDTO.isEmptyFilter()) {
			partnersCount = 0;
		} else {
			if (applyTeamMemberFilter) {
				partnersCount = teamDao.getAssignedPartnersCount(userId);
			}
		}
		return partnersCount;
	}

	private void setVendorModuleCount(DashboardModuleAnalyticsView dashboardAnalyticsView,
			AccessDetailsDTO accessDetailsDTO, List<DashboardModuleAnalyticsViewDTO> dtos, VanityUrlDetailsDTO postDto,
			boolean isPartnershipEstablishedOnlyWithPrm) {
		if (!postDto.isPartnerLoggedInThroughVanityUrl() && !postDto.isVanityUrlFilter()) {
			boolean isPartnerOrPartnerTeamMember = accessDetailsDTO.isPartner()
					|| accessDetailsDTO.isPartnerTeamMember();
			boolean isAdminAndPartner = accessDetailsDTO.isPrmAndPartner();
			boolean isAnyAdminAndPartnerTeamMember = accessDetailsDTO.isPrmAndPartnerTeamMember();
			if ((isPartnerOrPartnerTeamMember || isAnyAdminAndPartnerTeamMember)
					&& !isPartnershipEstablishedOnlyWithPrm) {
				setForTeamMember(dashboardAnalyticsView, accessDetailsDTO, dtos, isPartnerOrPartnerTeamMember,
						isAnyAdminAndPartnerTeamMember);
			} else if (isAdminAndPartner || isPartnerOrPartnerTeamMember) {
				dtos.add(setModuleAnalyticsDetails(
						dashboardAnalyticsView.getVendors() != null ? dashboardAnalyticsView.getVendors().intValue()
								: 0,
						true, vendors, vendorsIcon, vendorsColor, 4, vendorsTileInfo));
			}

		}
	}

	private void setForTeamMember(DashboardModuleAnalyticsView dashboardAnalyticsView,
			AccessDetailsDTO accessDetailsDTO, List<DashboardModuleAnalyticsViewDTO> dtos,
			boolean isPartnerOrPartnerTeamMember, boolean isAnyAdminAndPartnerTeamMember) {
		boolean access = isPartnerOrPartnerTeamMember;
		if (accessDetailsDTO.isPartnerTeamMember() || isAnyAdminAndPartnerTeamMember) {
			/****** Enabling for team members as per XNFR-143 ******/
			access = true;
		}
		dtos.add(setModuleAnalyticsDetails(
				dashboardAnalyticsView.getVendors() != null ? dashboardAnalyticsView.getVendors().intValue() : 0,
				access, vendors, vendorsIcon, vendorsColor, 4, vendorsTileInfo));
	}

	private void teamMembersCount(DashboardModuleAnalyticsView dashboardAnalyticsView,
			AccessDetailsDTO accessDetailsDTO, List<DashboardModuleAnalyticsViewDTO> dtos,
			VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		dtos.add(setModuleAnalyticsDetails(
				dashboardAnalyticsView.getTeamMembers() != null ? dashboardAnalyticsView.getTeamMembers().intValue()
						: 0,
				accessDetailsDTO.isTeamMemberAccess(), teamMembers, teamMembersIcon, teamMembersColor, 8,
				teammemberTileInfo));
	}

	private DashboardModuleAnalyticsViewDTO setModuleAnalyticsDetails(Integer count, boolean access, String module,
			String faIcon, String color, Integer moduleId, String description) {
		DashboardModuleAnalyticsViewDTO dto = new DashboardModuleAnalyticsViewDTO();
		dto.setModuleId(moduleId);
		dto.setModuleName(module);
		dto.setCount(count);
		dto.setHasAccess(access);
		if (access) {
			dto.setColor(color);
		} else {
			dto.setColor(color + " " + accessDeniedClassName);

		}
		dto.setFaIcon(faIcon);
		dto.setDescription(description);
		return dto;
	}

	@Override
	public XtremandResponse listVendorCompanyDetailsByUserId(Integer userId) {
		XtremandResponse response = new XtremandResponse();
		Integer partnerCompanyId = userDao.getCompanyIdByUserId(userId);
		List<Object[]> companies = dashboardAnalyticsViewsDao
				.listVendorCompanyDetailsByPartnerCompanyId(partnerCompanyId);
		List<CompanyDTO> dtos = new ArrayList<>();
		for (Object[] company : companies) {
			CompanyDTO companyDTO = new CompanyDTO();
			companyDTO.setCompanyName((String) company[0]);
			companyDTO.setCompanyProfileName((String) company[1]);
			companyDTO.setId((Integer) company[2]);
			dtos.add(companyDTO);
		}
		response.setData(dtos);
		return response;
	}

	@Override
	public XtremandResponse getEmailStats(VanityUrlDetailsDTO postDto) {
		XtremandResponse response = new XtremandResponse();
		utilService.isVanityUrlFilterApplicable(postDto);
		if (postDto.isPartnerLoggedInThroughVanityUrl()
				|| (postDto.getLoginAsUserId() != null && postDto.getLoginAsUserId() > 0)) {
			if (postDto.getLoginAsUserId() != null && postDto.getLoginAsUserId() > 0) {
				postDto.setVendorCompanyId(userDao.getCompanyIdByUserId(postDto.getLoginAsUserId()));
				postDto.setLoggedInUserCompanyId(userDao.getCompanyIdByUserId(postDto.getUserId()));
			}
			EmailStatsVanityUrlView emailStatsVanityUrlView = dashboardAnalyticsViewsDao
					.getEmailStatsForVanityUrl(postDto);
			if (emailStatsVanityUrlView != null) {
				response.setData(emailStatsVanityUrlView);
			} else {
				BigInteger bigInteger = BigInteger.valueOf(0);
				EmailStatsVanityUrlView emptyEmailStatsVanityUrlView = new EmailStatsVanityUrlView();
				emptyEmailStatsVanityUrlView.setClicked(bigInteger);
				emptyEmailStatsVanityUrlView.setOpened(bigInteger);
				emptyEmailStatsVanityUrlView.setViews(bigInteger);
				response.setData(emptyEmailStatsVanityUrlView);
			}
		} else if (postDto.isVendorLoggedInThroughOwnVanityUrl()) {
			VendorEmailStatsView vendorEmailStatsView = dashboardAnalyticsViewsDao
					.getVendorEmailStats(postDto.getUserId());
			response.setData(vendorEmailStatsView);
		} else {
			EmailStatsView emailStatsView = dashboardAnalyticsViewsDao.getEmailStats(postDto.getUserId());
			if (emailStatsView != null) {
				response.setData(emailStatsView);
			} else {
				BigInteger bigInteger = BigInteger.valueOf(0);
				EmailStatsView emailStatsEmptyView = new EmailStatsView();
				emailStatsEmptyView.setClicked(bigInteger);
				emailStatsEmptyView.setOpened(bigInteger);
				emailStatsEmptyView.setViews(bigInteger);
				response.setData(emailStatsEmptyView);
			}
		}
		response.setStatusCode(200);
		return response;
	}

	@Override
	public XtremandResponse getRegionalStatistics(VanityUrlDetailsDTO postDto) {
		XtremandResponse response = new XtremandResponse();
		utilService.isVanityUrlFilterApplicable(postDto);
		JSONArray countrywiseViewsJsonArray = new JSONArray();
		Integer loginAsUserId = postDto.getLoginAsUserId();

		if (loginAsUserId != null && loginAsUserId > 0) {
			postDto.setVendorCompanyId(userDao.getCompanyIdByUserId(postDto.getLoginAsUserId()));
			postDto.setLoggedInUserCompanyId(userDao.getCompanyIdByUserId(postDto.getUserId()));
		}

		if (postDto.isPartnerLoggedInThroughVanityUrl() || (loginAsUserId != null && loginAsUserId > 0)) {
			List<RegionalStatisticsVanityUrlView> regionalStatisticsVanityUrlViews = dashboardAnalyticsViewsDao
					.listRegionalStatisticsVanityUrlViewsByCompanyId(postDto);
			for (RegionalStatisticsVanityUrlView regionalStatisticsUrlView : regionalStatisticsVanityUrlViews) {
				String countryCode = regionalStatisticsUrlView.getCountryCode();
				BigDecimal count = regionalStatisticsUrlView.getWathedCount();
				addDataToJsonArray(countrywiseViewsJsonArray, countryCode, count);
			}

		} else if (postDto.isVendorLoggedInThroughOwnVanityUrl()) {
			List<VendorRegionalStatisticsView> vendorRegionalStatisticsViews = dashboardAnalyticsViewsDao
					.listVendorRegionalStatisticsViewsBy(postDto);
			for (VendorRegionalStatisticsView vendorRegionalStatisticsView : vendorRegionalStatisticsViews) {
				String countryCode = vendorRegionalStatisticsView.getCountryCode();
				BigDecimal count = vendorRegionalStatisticsView.getWathedCount();
				addDataToJsonArray(countrywiseViewsJsonArray, countryCode, count);
			}
		} else {
			List<RegionalStatisticsView> regionalStatisticsViewsByCompanyId = dashboardAnalyticsViewsDao
					.listRegionalStatisticsViewsByCompanyId(postDto);
			for (RegionalStatisticsView regionalStatisticsView : regionalStatisticsViewsByCompanyId) {
				String countryCode = regionalStatisticsView.getCountryCode();
				BigDecimal count = regionalStatisticsView.getWathedCount();
				addDataToJsonArray(countrywiseViewsJsonArray, countryCode, count);
			}
		}
		response.setData(countrywiseViewsJsonArray);
		return response;
	}

	private void addDataToJsonArray(JSONArray countrywiseViewsJsonArray, String countryCode, BigDecimal count) {
		JSONArray json = new JSONArray();
		if (StringUtils.hasText(countryCode)) {
			json.put(countryCode.toLowerCase());
		} else {
			json.put("");
		}
		json.put(count);
		countrywiseViewsJsonArray.put(json);
	}

	public List<Integer> getCustomerIds(VanityUrlDetailsDTO dto) {
		List<Integer> customerIds = new ArrayList<>();
		Integer loginAsUserId = dto.getLoginAsUserId();
		if ((dto.isVanityUrlFilter()
				&& dto.getLoggedInUserCompanyId().intValue() != dto.getVendorCompanyId().intValue())
				|| (!dto.isVanityUrlFilter()) || (loginAsUserId != null && loginAsUserId > 0)) {
			customerIds = userDao.getCompanyUserIds(dto.getUserId());
			if (loginAsUserId != null && loginAsUserId > 0) {
				dto.setVendorCompanyId(userDao.getCompanyIdByUserId(loginAsUserId));
			}
		} else if ((dto.isVanityUrlFilter()
				&& dto.getLoggedInUserCompanyId().intValue() == dto.getVendorCompanyId().intValue())) {
			customerIds = userDao.getCompanyUsers(dto.getVendorCompanyId());
		}
		return customerIds;
	}

	public VanityUrlDetailsDTO getUpdatedDto(VanityUrlDetailsDTO dto) {
		Integer loggedInUserCompanyId = userDao.getCompanyIdByUserId(dto.getUserId());
		Integer vendorCompanyId = userDao.getCompanyIdByProfileName(dto.getVendorCompanyProfileName());
		dto.setLoggedInUserCompanyId(loggedInUserCompanyId);
		dto.setVendorCompanyId(vendorCompanyId);
		return dto;
	}

	@Override
	public XtremandResponse getOpportunitiesVendorAnalytics(Integer userId) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(200);
		OpportunitiesVendorAnalyticsView opportunitiesVendorAnalyticsView = new OpportunitiesVendorAnalyticsView();
		response.setData(opportunitiesVendorAnalyticsView);
		return response;
	}

	@Override
	public XtremandResponse getOpportunitiesPartnerAnalytics(VanityUrlDetailsDTO dto) {
		XtremandResponse response = new XtremandResponse();
		utilService.isVanityUrlFilterApplicable(dto);
		if (dto.isPartnerLoggedInThroughVanityUrl()) {
			OpportunitiesVanityUrlPartnerAnalyticsView opportunitiesVanityUrlPartnerAnalyticsView = dashboardAnalyticsViewsDao
					.getOpportunitiesVanityUrlPartnerAnalytics(dto);
			if (opportunitiesVanityUrlPartnerAnalyticsView != null) {
				response.setData(opportunitiesVanityUrlPartnerAnalyticsView);
			} else {
				response.setData(new OpportunitiesPartnerAnalyticsView());
			}
		} else {
			OpportunitiesPartnerAnalyticsView opportunitiesPartnerAnalyticsView = dashboardAnalyticsViewsDao
					.getOpportunitiePartnerAnalyticsByCompanyId(dto);
			if (opportunitiesPartnerAnalyticsView != null) {
				response.setData(opportunitiesPartnerAnalyticsView);
			} else {
				response.setData(new OpportunitiesPartnerAnalyticsView());
			}
			response.setStatusCode(200);
		}
		return response;
	}

	@Override
	public Map<String, Object> listEmailOpenLogs(VanityUrlDetailsDTO dto, Integer actionId, Integer pageSize,
			Integer pageNumber) {
		Map<String, Object> resultMap = new HashMap<>();
		return resultMap;
	}

	private void addTotalRecordsToMap(Map<String, Object> resultMap, Map<String, Object> map) {
		resultMap.put(XamplifyConstants.TOTAL_RECORDS, map.get(XamplifyConstants.TOTAL_RECORDS));
	}

	@Override
	public Map<String, Object> listEmailGifClickedUrlClickedLogs(VanityUrlDetailsDTO dto, Integer pageSize,
			Integer pageNumber) {
		Map<String, Object> resultMap = new HashMap<>();
		return resultMap;
	}

	@Override
	public Map<String, Object> listWatchedUsersByUser(VanityUrlDetailsDTO dto, Integer pageSize, Integer pageNumber) {
		Map<String, Object> resultMap = new HashMap<>();
		return resultMap;
	}

	private void setEmailLogReportTime(EmailLogReport emailLogReport) {
		if (emailLogReport.getTime() != null) {
			String utcTimeString = DateUtils.getUtcTimeInString(emailLogReport.getTime(),
					DateUtils.getServerTimeZone());
			emailLogReport.setUtcTimeString(utcTimeString);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getDashboardWorldMapDetailReport(VanityUrlDetailsDTO dto, Integer pageSize,
			Integer pageNumber, String countryCode) {
		Map<String, Object> resultMap = new HashMap<>();
		dto = getUpdatedDto(dto);
		List<Integer> customerIds = getCustomerIds(dto);
		Map<String, Object> map = dashboardAnalyticsViewsDao.getDashboardWorldMapDetailReport(customerIds, pageSize,
				pageNumber, dto, countryCode);

		List<EmailLogReport> emailLogReportList = (List<EmailLogReport>) map.get("data");
		for (EmailLogReport emailLogReport : emailLogReportList) {
			setEmailLogReportTime(emailLogReport);
		}
		resultMap.put("data", emailLogReportList);
		addTotalRecordsToMap(resultMap, map);
		return resultMap;
	}

	@Override
	public VideoStats getDashboardVideoStatsData(VanityUrlDetailsDTO dto, Integer daysInterval) {
		dto = getUpdatedDto(dto);
		List<Integer> customerIds = getCustomerIds(dto);

		Map<String, Integer> viewsMap = new HashMap<>();
		Map<String, Double> minutesWatchedMap = new HashMap<>();
		Map<String, Double> averageDurationMap = new HashMap<>();

		List<String> totalDatesList = new ArrayList<>();
		LocalDate now = LocalDate.now();
		for (int i = 0; i < daysInterval; i++) {
			Period p1 = Period.ofDays(i);
			totalDatesList.add(now.minus(p1).toString());
		}

		List<String> dbDatesList = new ArrayList<>();
		List<Integer> views = new ArrayList<>();
		List<Double> minutesWatched = new ArrayList<>();
		List<Double> averageDuration = new ArrayList<>();
		HashMap<Integer, String> dates = new HashMap<>();

		List<Object[]> viewsList = dashboardAnalyticsViewsDao.getDashboardViewsData(customerIds, daysInterval, dto);
		for (int i = 0; i < viewsList.size(); i++) {
			Object[] row = viewsList.get(i);
			dates.put(i, (row[2].toString()));
			dbDatesList.add(row[2].toString());
			viewsMap.put(row[2].toString(), ((java.math.BigInteger) row[0]).intValue());
		}

		List<Object[]> minutesWatchedList = dashboardAnalyticsViewsDao.getDashboardMinutesWatchedData(customerIds,
				daysInterval, dto);
		for (int i = 0; i < minutesWatchedList.size(); i++) {
			Object[] row = minutesWatchedList.get(i);
			minutesWatchedMap.put(row[2].toString(), ((java.math.BigDecimal) row[0]).doubleValue());
		}

		List<Object[]> averageDurationList = dashboardAnalyticsViewsDao.getDashboardAverageDurationData(customerIds,
				daysInterval, dto);
		for (int i = 0; i < averageDurationList.size(); i++) {
			Object[] row = averageDurationList.get(i);
			averageDurationMap.put(row[2].toString(), ((java.math.BigDecimal) row[0]).doubleValue());
		}

		totalDatesList.removeIf(dbDatesList::contains);

		int index = 0;
		int limit = dates.size() + totalDatesList.size();
		for (int i = dates.size(); i < limit; i++) {
			dates.put(i, totalDatesList.get(index));
			viewsMap.put(totalDatesList.get(index), 0);
			minutesWatchedMap.put(totalDatesList.get(index), 0.0);
			averageDurationMap.put(totalDatesList.get(index), 0.0);
			index++;
		}

		TreeMap<String, Integer> viewsTreeMap = new TreeMap<>();
		viewsTreeMap.putAll(viewsMap);
		TreeMap<String, Double> minutesWatchedTreeMap = new TreeMap<>();
		minutesWatchedTreeMap.putAll(minutesWatchedMap);
		TreeMap<String, Double> averageDurationTreeMap = new TreeMap<>();
		averageDurationTreeMap.putAll(averageDurationMap);

		Set<String> values = viewsTreeMap.keySet();
		int position = 0;
		for (String key : values) {
			dates.put((position++), key);
		}
		for (Map.Entry<String, Integer> entry : viewsTreeMap.entrySet()) {
			views.add(entry.getValue());

		}
		for (Map.Entry<String, Double> entry : minutesWatchedTreeMap.entrySet()) {
			minutesWatched.add(entry.getValue());

		}
		for (Map.Entry<String, Double> entry : averageDurationTreeMap.entrySet()) {
			averageDuration.add(entry.getValue());

		}
		VideoStats videoStats = new VideoStats();
		videoStats.setViews(views);
		videoStats.setMinutesWatched(minutesWatched);
		videoStats.setAverageDuration(averageDuration);
		videoStats.setDates(dates);
		return videoStats;
	}

	@Override
	public PartnerAnalyticsCountDTO getActiveInActiveTotalPartnerCounts(Integer userId, boolean applyFilter) {
		return dashboardAnalyticsViewsDao.getActiveInActiveTotalPartnerCounts(userId, applyFilter);
	}

	@Override
	public Map<String, Object> findDataForDealOrLeadBubbleChart(Integer userId, String moduleType,
			boolean applyFilter) {
		Map<String, Object> map = new HashMap<>();
		Object[][] postions = { { 16, 45, 15 }, { 11, 38 }, { 0.0, 47.0 }, { 5, 38 }, { 2, 36.7 }, { 5, 42 }, { 1, 45 },
				{ -5, 42 }, { 0, 39.2 }, { 0, 38 } };
		List<List<?>> listOfLists = new ArrayList<>();
		List<WordCloudMapDTO> names = new ArrayList<>();
		if ("d".equals(moduleType)) {
			names.addAll(dashboardAnalyticsViewsDao.findDataForDealBubbleChart(userId, applyFilter));
		} else {
			names.addAll(dashboardAnalyticsViewsDao.findDataForLeadBubbleChart(userId, applyFilter));
		}
		for (int i = 0; i < names.size(); i++) {
			WordCloudMapDTO wordCloudMapDTO = names.get(i);
			listOfLists.add(new ArrayList<>(asList(postions[i][0], postions[i][1], wordCloudMapDTO.getWeight())));
		}
		map.put("names", names);
		map.put("values", listOfLists);
		return map;
	}

	/**** Funnel Charts Analytics ***/
	@Override
	public XtremandResponse getFunnelChartsAnalytics(VanityUrlDetailsDTO vanityUrlDetailsDto) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(200);
		utilService.isVanityUrlFilterApplicable(vanityUrlDetailsDto);
		Integer userId = vanityUrlDetailsDto.getUserId();
		RoleDisplayDTO roleDisplayDto = utilService.getRoleDetailsByUserId(userId);
		response.setData(dashboardAnalyticsViewsDao.getFunnelChartAnalyticsData(vanityUrlDetailsDto, roleDisplayDto));
		return response;
	}

	@Override
	public XtremandResponse getPieChartsLeadsAnalytics(VanityUrlDetailsDTO vanityUrlDetailsDto) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(200);
		utilService.isVanityUrlFilterApplicable(vanityUrlDetailsDto);
		Integer userId = vanityUrlDetailsDto.getUserId();
		RoleDisplayDTO roleDisplayDto = utilService.getRoleDetailsByUserId(userId);
		response.setData(dashboardAnalyticsViewsDao.getPieChartLeadAnalyticsData(vanityUrlDetailsDto, roleDisplayDto));
		return response;
	}

	@Override
	public XtremandResponse getPieChartsDealsAnalytics(VanityUrlDetailsDTO vanityUrlDetailsDto) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(200);
		utilService.isVanityUrlFilterApplicable(vanityUrlDetailsDto);
		Integer userId = vanityUrlDetailsDto.getUserId();
		RoleDisplayDTO roleDisplayDto = utilService.getRoleDetailsByUserId(userId);
		response.setData(dashboardAnalyticsViewsDao.getPieChartDealsAnalyticsData(vanityUrlDetailsDto, roleDisplayDto));
		return response;
	}

	@Override
	public XtremandResponse getPieChartsStatisticsLeadAnalytics(VanityUrlDetailsDTO vanityUrlDetailsDto) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(200);
		utilService.isVanityUrlFilterApplicable(vanityUrlDetailsDto);
		Integer userId = vanityUrlDetailsDto.getUserId();
		RoleDisplayDTO roleDisplayDto = utilService.getRoleDetailsByUserId(userId);
		response.setData(dashboardAnalyticsViewsDao.getPieChartLeadStatisticsData(vanityUrlDetailsDto, roleDisplayDto));
		return response;
	}

	@Override
	public XtremandResponse getPieChartsDealStatisticsAnalytics(VanityUrlDetailsDTO vanityUrlDetailsDto) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(200);
		utilService.isVanityUrlFilterApplicable(vanityUrlDetailsDto);
		Integer userId = vanityUrlDetailsDto.getUserId();
		RoleDisplayDTO roleDisplayDto = utilService.getRoleDetailsByUserId(userId);
		response.setData(dashboardAnalyticsViewsDao.getPieChartDealStatisticsData(vanityUrlDetailsDto, roleDisplayDto));
		return response;
	}

	@Override
	public XtremandResponse getPieChartsDealStatisticsWithStageNames(VanityUrlDetailsDTO vanityUrlDetailsDto) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(200);
		utilService.isVanityUrlFilterApplicable(vanityUrlDetailsDto);
		Integer userId = vanityUrlDetailsDto.getUserId();
		RoleDisplayDTO roleDisplayDto = utilService.getRoleDetailsByUserId(userId);
		response.setData(dashboardAnalyticsViewsDao.findDealsWithStageNames(vanityUrlDetailsDto, roleDisplayDto));
		return response;
	}

	@Override
	public XtremandResponse getPieChartsLeadsStatisticsWithStageNames(VanityUrlDetailsDTO vanityUrlDetailsDto) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(200);
		utilService.isVanityUrlFilterApplicable(vanityUrlDetailsDto);
		Integer userId = vanityUrlDetailsDto.getUserId();
		RoleDisplayDTO roleDisplayDto = utilService.getRoleDetailsByUserId(userId);
		response.setData(dashboardAnalyticsViewsDao.findLeadsWithStageNames(vanityUrlDetailsDto, roleDisplayDto));
		return response;
	}

	@Override
	public XtremandResponse findAllQuickLinks(Pageable pageable, String domainName, Integer userId,
			BindingResult result) {
		XtremandResponse response = new XtremandResponse();
		pageableValidator.validatePagableParameters(pageable, result, "");
		if (result.hasErrors()) {
			xamplifyUtilValidator.addErrorResponse(result, response);
		} else {
			Map<String, Object> map = new HashMap<>();
			PaginatedDTO paginatedDTO = new PaginatedDTO();
			Pagination pagination = utilService.setPageableParameters(pageable, userId);
			VanityUrlDetailsDTO vanityUrlDetailsDTO = new VanityUrlDetailsDTO();
			vanityUrlDetailsDTO.setVanityUrlFilter(true);
			vanityUrlDetailsDTO.setUserId(userId);
			vanityUrlDetailsDTO.setVendorCompanyProfileName(domainName);
			utilService.isVanityUrlFilterApplicable(vanityUrlDetailsDTO);
			boolean isPartnerLoggedInThroughVanityUrl = vanityUrlDetailsDTO.isPartnerLoggedInThroughVanityUrl();
			RoleDisplayDTO roleDisplayDto = utilService.getRoleDetailsByUserId(userId);
			Integer vendorCompanyId = vanityUrlDetailsDTO.getVendorCompanyId();
			LeftSideNavigationBarItem leftSideNavigationBarItem = new LeftSideNavigationBarItem();
			leftSideNavigationBarItem.setUserId(userId);
			if (isPartnerLoggedInThroughVanityUrl) {
				paginatedDTO = findAllQuickLinksForPartner(pageable, pagination, vanityUrlDetailsDTO, roleDisplayDto,
						vendorCompanyId, leftSideNavigationBarItem);
			} else if (vanityUrlDetailsDTO.isVendorLoggedInThroughOwnVanityUrl()) {
				paginatedDTO = findAllQuickLinksForVendor(pageable, userId, pagination, roleDisplayDto,
						leftSideNavigationBarItem);
			}
			map.put("companyId", vanityUrlDetailsDTO.getVendorCompanyId());
			map.put("isPartnerLoggedInThroughVanityUrl", isPartnerLoggedInThroughVanityUrl);
			response.setMap(map);
			XamplifyUtils.addPaginatedDTO(response, paginatedDTO);
		}
		return response;
	}

	private PaginatedDTO findAllQuickLinksForPartner(Pageable pageable, Pagination pagination,
			VanityUrlDetailsDTO vanityUrlDetailsDTO, RoleDisplayDTO roleDisplayDto, Integer vendorCompanyId,
			LeftSideNavigationBarItem leftSideNavigationBarItem) {
		PaginatedDTO paginatedDTO;
		utilService.setDAMAccessForVendorVanityLogin(leftSideNavigationBarItem, vendorCompanyId, roleDisplayDto);
		utilService.setLearningTracksAccessForVendorVanityLogin(leftSideNavigationBarItem, vendorCompanyId,
				roleDisplayDto);
		utilService.setPlayBookAccessForVendorVanityLogin(leftSideNavigationBarItem, vendorCompanyId, roleDisplayDto);
		paginatedDTO = dashboardAnalyticsViewsDao.findAllQuickLinksForPartner(pagination, pageable.getSearch(),
				leftSideNavigationBarItem, vanityUrlDetailsDTO);
		return paginatedDTO;
	}

	private PaginatedDTO findAllQuickLinksForVendor(Pageable pageable, Integer userId, Pagination pagination,
			RoleDisplayDTO roleDisplayDto, LeftSideNavigationBarItem leftSideNavigationBarItem) {
		List<Integer> roleIds = roleDisplayDto.getRoleIds();
		boolean isAnyAdmin = roleDisplayDto.isAnyAdmin();
		utilService.setDAMAccess(leftSideNavigationBarItem, userId, roleIds, isAnyAdmin);
		utilService.setLearningTrackAccess(leftSideNavigationBarItem, userId, roleIds, isAnyAdmin);
		utilService.setPlayBookAccess(leftSideNavigationBarItem, userId, roleIds, isAnyAdmin);
		return dashboardAnalyticsViewsDao.findAllQuickLinksForVendor(pagination, pageable.getSearch(),
				leftSideNavigationBarItem);
	}

	// XNFR-574
	@Override
	public XtremandResponse universalSearch(Pageable pageable, String domainName, Integer userId,
			BindingResult result) {
		XtremandResponse response = new XtremandResponse();
		pageableValidator.validatePagableParameters(pageable, result, "");
		if (result.hasErrors()) {
			xamplifyUtilValidator.addErrorResponse(result, response);
		} else {
			Map<String, Object> map = new HashMap<>();
			PaginatedDTO paginatedDTO = new PaginatedDTO();
			Pagination pagination = utilService.setPageableParameters(pageable, userId);
			pagination.setPartnerTeamMemberGroupFilter(pageable.isFilterPartners());
			VanityUrlDetailsDTO vanityUrlDetailsDTO = new VanityUrlDetailsDTO();
			if (StringUtils.hasText(domainName)) {
				vanityUrlDetailsDTO.setVanityUrlFilter(true);
				vanityUrlDetailsDTO.setVendorCompanyProfileName(domainName);
			}
			vanityUrlDetailsDTO.setUserId(userId);
			utilService.isVanityUrlFilterApplicable(vanityUrlDetailsDTO);
			boolean isPartnerLoggedInThroughVanityUrl = vanityUrlDetailsDTO.isPartnerLoggedInThroughVanityUrl();
			RoleDisplayDTO roleDisplayDto = utilService.getRoleDetailsByUserId(userId);
			Integer vendorCompanyId = 0;
			if (XamplifyUtils.isValidInteger(vanityUrlDetailsDTO.getVendorCompanyId())) {
				vendorCompanyId = vanityUrlDetailsDTO.getVendorCompanyId();
			} else {
				Integer logInAsUserId = pagination.getLoginAsUserId();
				if (XamplifyUtils.isValidInteger(logInAsUserId)) {
					vendorCompanyId = userDao.getCompanyIdByUserId(logInAsUserId);
				} else {
					vendorCompanyId = userDao.getCompanyIdByUserId(userId);
				}
			}
			LeftSideNavigationBarItem leftSideNavigationBarItem = new LeftSideNavigationBarItem();
			leftSideNavigationBarItem.setUserId(userId);
			if (isPartnerLoggedInThroughVanityUrl) {
				paginatedDTO = universalSearchForVendorVanity(pageable, pagination, vanityUrlDetailsDTO, roleDisplayDto,
						vendorCompanyId, leftSideNavigationBarItem);
			} else if (vanityUrlDetailsDTO.isVendorLoggedInThroughOwnVanityUrl()) {
				leftSideNavigationBarItem.setLoggedInThroughOwnVanityUrl(true);
				paginatedDTO = universalSearchForVendorLoggedInThroughOwnVanityUrl(pageable, userId, pagination,
						roleDisplayDto, leftSideNavigationBarItem);
			} else {
				leftSideNavigationBarItem.setLoggedInThroughXamplifyUrl(true);
				paginatedDTO = findUniversalSearchForXamplifyLogin(pageable, userId, pagination, roleDisplayDto,
						leftSideNavigationBarItem);
			}
			map.put("companyId", vendorCompanyId);
			map.put("isPartnerLoggedInThroughVanityUrl", isPartnerLoggedInThroughVanityUrl);
			response.setMap(map);
			XamplifyUtils.addPaginatedDTO(response, paginatedDTO);
		}
		return response;
	}

	private PaginatedDTO findUniversalSearchForXamplifyLogin(Pageable pageable, Integer userId, Pagination pagination,
			RoleDisplayDTO roleDisplayDto, LeftSideNavigationBarItem leftSideNavigationBarItem) {
		PaginatedDTO paginatedDTO;
		universalSearchAccess(userId, leftSideNavigationBarItem, roleDisplayDto);
		paginatedDTO = dashboardAnalyticsViewsDao.universalSearchForXamplifyLogin(pagination, pageable.getSearch(),
				leftSideNavigationBarItem, roleDisplayDto, userId);
		return paginatedDTO;
	}

	private void universalSearchAccess(Integer userId, LeftSideNavigationBarItem leftSideNavigationBarItem,
			RoleDisplayDTO roleDisplayDto) {
		List<Integer> roleIds = roleDisplayDto.getRoleIds();
		boolean isAnyAdmin = roleDisplayDto.isAnyAdmin();
		utilService.setDAMAccess(leftSideNavigationBarItem, userId, roleIds, isAnyAdmin);
		boolean isDamAccessAsPartner = utilDao.damAccessForPartner(userId);
		utilService.setLearningTrackAccess(leftSideNavigationBarItem, userId, roleIds, isAnyAdmin);
		boolean isLmsAccessAsPartner = utilDao.lmsAccessForPartner(userId);
		utilService.setPlayBookAccess(leftSideNavigationBarItem, userId, roleIds, isAnyAdmin);
		boolean isPlaybooksAsPartnerAccess = utilDao.playbookAccessForPartner(userId);
		boolean opportunityRole = utilService.setOpportunitiesAccess(leftSideNavigationBarItem, userId, roleIds,
				isAnyAdmin);
		setOpportunitiesAccessAsPartner(leftSideNavigationBarItem, roleDisplayDto, userId, opportunityRole);
		leftSideNavigationBarItem.setDamAccessAsPartner(isDamAccessAsPartner);
		leftSideNavigationBarItem.setLmsAccessAsPartner(isLmsAccessAsPartner);
		leftSideNavigationBarItem.setPlaybookAccessAsPartner(isPlaybooksAsPartnerAccess);
		setPartnerModuleAccess(userId, leftSideNavigationBarItem, roleIds, isAnyAdmin);
	}

	/** XNFR-792 **/
	private void setPartnerModuleAccess(Integer userId, LeftSideNavigationBarItem leftSideNavigationBarItem,
			List<Integer> roleIds, boolean isAnyAdmin) {
		boolean partnersRole = roleIds.indexOf(Role.PARTNERS.getRoleId()) > -1;
		leftSideNavigationBarItem.setPartners(isAnyAdmin || partnersRole);
	}

	/** XNFR-792 **/
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

	private PaginatedDTO universalSearchForVendorLoggedInThroughOwnVanityUrl(Pageable pageable, Integer userId,
			Pagination pagination, RoleDisplayDTO roleDisplayDto, LeftSideNavigationBarItem leftSideNavigationBarItem) {
		leftSideNavigationBarItem.setRoleDisplayDto(roleDisplayDto);
		universalSearchAccess(userId, leftSideNavigationBarItem, roleDisplayDto);
		return dashboardAnalyticsViewsDao.universalSearchForVendor(pagination, pageable.getSearch(),
				leftSideNavigationBarItem);
	}

	private PaginatedDTO universalSearchForVendorVanity(Pageable pageable, Pagination pagination,
			VanityUrlDetailsDTO vanityUrlDetailsDTO, RoleDisplayDTO roleDisplayDto, Integer vendorCompanyId,
			LeftSideNavigationBarItem leftSideNavigationBarItem) {
		PaginatedDTO paginatedDTO;
		leftSideNavigationBarItem.setRoleDisplayDto(roleDisplayDto);
		utilService.setDAMAccessForVendorVanityLogin(leftSideNavigationBarItem, vendorCompanyId, roleDisplayDto);
		utilService.setLearningTracksAccessForVendorVanityLogin(leftSideNavigationBarItem, vendorCompanyId,
				roleDisplayDto);
		utilService.setPlayBookAccessForVendorVanityLogin(leftSideNavigationBarItem, vendorCompanyId, roleDisplayDto);
		utilService.setOpportunitiesAccessForVendorVanityLogin(leftSideNavigationBarItem, vendorCompanyId,
				roleDisplayDto);
		paginatedDTO = dashboardAnalyticsViewsDao.universalSearchForVendorVanity(pagination, pageable.getSearch(),
				leftSideNavigationBarItem, vanityUrlDetailsDTO);
		return paginatedDTO;
	}

}
