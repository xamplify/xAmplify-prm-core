package com.xtremand.url.authorization.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.xtremand.module.service.ModuleService;
import com.xtremand.url.authorization.dto.UrlAuthorizationDTO;
import com.xtremand.user.bom.Role;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.util.dto.LeftSideNavigationBarItem;
import com.xtremand.util.dto.XamplifyConstants;
import com.xtremand.util.service.UtilService;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;

@Service
public class UrlAuthorizationService {

	private final List<UrlAuthorizationStrategy> strategies;

	@Autowired
	private UserDAO userDao;

	@Autowired
	private UtilService utilService;

	@Autowired
	private ModuleService moduleService;

	@Autowired
	public UrlAuthorizationService(List<UrlAuthorizationStrategy> strategies) {
		this.strategies = strategies;
	}

	public void authorizeByModuleId(Integer moduleId, Integer loggedInUserId, String routerUrl, String subDomain) {

		VanityUrlDetailsDTO vanityUrlDetailsDTO = new VanityUrlDetailsDTO();
		vanityUrlDetailsDTO.setVanityUrlFilter(isUserLoggedInThroughVanityUrl(subDomain));
		vanityUrlDetailsDTO.setVendorCompanyProfileName(subDomain);
		vanityUrlDetailsDTO.setUserId(loggedInUserId);
		utilService.isVanityUrlFilterApplicable(vanityUrlDetailsDTO);
		Integer loggedInUserCompanyId = userDao.getCompanyIdByUserId(loggedInUserId);
		List<Integer> roleIds = userDao.getRoleIdsByUserId(loggedInUserId);
		boolean isAnyVendorAdmin = Role.isAnyVendorCompanyAdmin(roleIds);
		boolean isAnyPartnerAdmin = Role.isPartnerAdmin(roleIds);

		UrlAuthorizationDTO urlAuthorizationDTO = new UrlAuthorizationDTO();
		urlAuthorizationDTO.setRouterUrl(routerUrl);
		urlAuthorizationDTO.setLoggedInUserId(loggedInUserId);
		urlAuthorizationDTO.setLoggedInUserCompanyId(loggedInUserCompanyId);
		urlAuthorizationDTO.setVendorAdmin(isAnyVendorAdmin);
		urlAuthorizationDTO.setPartnerAdmin(isAnyPartnerAdmin);
		urlAuthorizationDTO.setVanityUrlDetailsDTO(vanityUrlDetailsDTO);
		urlAuthorizationDTO.setRoleIds(roleIds);

		if ("modules".equalsIgnoreCase(routerUrl)) {
			LeftSideNavigationBarItem leftSideItems = moduleService.findLeftMenuItems(vanityUrlDetailsDTO);
			boolean hasAccess = leftSideItems.isDam() || leftSideItems.isDamAccessAsPartner()
					|| leftSideItems.isLms() || leftSideItems.isLmsAccessAsPartner()
					|| leftSideItems.isPlaybook() || leftSideItems.isPlaybookAccessAsPartner();
			if (!hasAccess) {
				throw new AccessDeniedException(XamplifyConstants.USER_NOT_AUTHORIZED_TO_ACCESS_PAGE);
			}
		} else {
			strategies.stream().filter(strategy -> strategy.isModuleIdMatched(moduleId)).findFirst()
			.orElseThrow(() -> new IllegalArgumentException("Unsupported module Id : " + moduleId 
					+ " for loggedInUserId : " + loggedInUserId)).authorize(urlAuthorizationDTO);
		}
	}

	private boolean isUserLoggedInThroughVanityUrl(String subDomain) {
		return StringUtils.hasText(subDomain);
	}

}
