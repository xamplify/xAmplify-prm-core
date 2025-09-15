package com.xtremand.url.authorization.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.xtremand.lms.bom.LearningTrackType;
import com.xtremand.lms.dao.LMSDAO;
import com.xtremand.url.authorization.dto.AuthorizationParametersDTO;
import com.xtremand.url.authorization.dto.UrlAuthorizationDTO;
import com.xtremand.user.bom.Role;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.dto.ModuleIdList;
import com.xtremand.util.dto.XamplifyConstants;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;

@Service
public class PlaybookUrlAuthorizationStrategy implements UrlAuthorizationStrategy {

	@Autowired
	private UtilDao utilDao;

	@Autowired
	private LMSDAO lmsDao;

	@Override
	public boolean isModuleIdMatched(Integer moduleId) {
		return ModuleIdList.PLAY_BOOK.equals(moduleId);
	}

	@Override
	public void authorize(UrlAuthorizationDTO urlAuthorizationDTO) {
		VanityUrlDetailsDTO vanityUrlDetailsDTO = urlAuthorizationDTO.getVanityUrlDetailsDTO();
		String routerUrl = urlAuthorizationDTO.getRouterUrl();
		AuthorizationParametersDTO authorizationParametersDTO = new AuthorizationParametersDTO(vanityUrlDetailsDTO,
				routerUrl);
		boolean isPlaybookUrlAccessibleByPartner = isPlaybookUrlForOnlyPartnerLogin(urlAuthorizationDTO);
		if (authorizationParametersDTO.isLoggedInThroughVanity()) {
			authorizeVanityLogin(authorizationParametersDTO, isPlaybookUrlAccessibleByPartner, urlAuthorizationDTO);
		} else {
			authorizeForDefaultLogin(urlAuthorizationDTO, isPlaybookUrlAccessibleByPartner);
		}
	}

	private boolean isPlaybookUrlForOnlyPartnerLogin(UrlAuthorizationDTO urlAuthorizationDTO) {
		String routerUrl = urlAuthorizationDTO.getRouterUrl();
		return validateIfPartnerLoginAccessUrl(routerUrl) || "pb".equalsIgnoreCase(routerUrl);
	}

	private boolean validateIfPartnerLoginAccessUrl(String routerUrl) {
		return "shared".equalsIgnoreCase(routerUrl);
	}

	private void authorizeVanityLogin(AuthorizationParametersDTO authorizationParametersDTO,
			boolean isPlaybookUrlAccessibleByPartner, UrlAuthorizationDTO urlAuthorizationDTO) {
		if (authorizationParametersDTO.isLoggedInThroughOwnVanityUrl()) {
			authorizeForOwnVanityLogin(urlAuthorizationDTO);
		} else if (authorizationParametersDTO.isPartnerLoggedInThroughVanityUrl()) {
			authorizeForPartnerVanityLogin(urlAuthorizationDTO, isPlaybookUrlAccessibleByPartner);
		}
	}

	private void authorizeForOwnVanityLogin(UrlAuthorizationDTO urlAuthorizationDTO) {
		boolean isPlaybookModuleEnabledBySuperAdmin = utilDao
				.hasPlaybookAccessByUserId(urlAuthorizationDTO.getLoggedInUserId());
		boolean isPlaybookRole = urlAuthorizationDTO.getRoleIds().contains(Role.PLAY_BOOK.getRoleId());
		boolean playBookAccess = urlAuthorizationDTO.isVendorAdmin() || isPlaybookRole;
		boolean isPlaybookPartnerUrl = validateIfPartnerLoginAccessUrl(urlAuthorizationDTO.getRouterUrl());
		boolean isUrlAuthorized = isPlaybookModuleEnabledBySuperAdmin && playBookAccess && !isPlaybookPartnerUrl;
		if (!isUrlAuthorized) {
			denyAccess();
		}
	}

	private void authorizeForPartnerVanityLogin(UrlAuthorizationDTO urlAuthorizationDTO,
			boolean isPlaybookUrlAccessibleByPartner) {
		Integer vendorCompanyId = urlAuthorizationDTO.getVanityUrlDetailsDTO().getVendorCompanyId();
		if (!isPlaybookUrlAccessibleByPartner || !isUrlAuthorizedForPartner(vendorCompanyId, urlAuthorizationDTO)) {
			denyAccess();
		}
	}

	private boolean isUrlAuthorizedForPartner(Integer vendorCompanyId, UrlAuthorizationDTO urlAuthorizationDTO) {
		boolean isPlaybookModuleEnabledBySuperAdmin = utilDao.hasPlaybookAccessByCompanyId(vendorCompanyId);
		boolean isPlaybookRole = urlAuthorizationDTO.getRoleIds().contains(Role.PLAY_BOOK.getRoleId());
		boolean isPlaybookSharedToPartner = lmsDao.isLMSSharedToPartnerCompanyByPartnerId(
				urlAuthorizationDTO.getLoggedInUserId(), LearningTrackType.PLAYBOOK, vendorCompanyId);
		boolean playbookAccessAsPartner = urlAuthorizationDTO.isPartnerAdmin() || isPlaybookRole
				|| isPlaybookSharedToPartner;
		
		boolean isVendorGivenAccessToPartner = utilDao.fetchModuleAccessForPartnerByModuleIdAndVendorCompanyIdAndUserId(vendorCompanyId, 12, urlAuthorizationDTO.getLoggedInUserId());

		return isPlaybookModuleEnabledBySuperAdmin && playbookAccessAsPartner && isVendorGivenAccessToPartner;
	}

	private void authorizeForDefaultLogin(UrlAuthorizationDTO urlAuthorizationDTO,
			boolean isPlaybookUrlAccessibleByPartner) {
		Integer loggedInUserId = urlAuthorizationDTO.getLoggedInUserId();
		Integer vendorCompanyId = urlAuthorizationDTO.getVanityUrlDetailsDTO().getVendorCompanyId();
		boolean hasPlaybookRole = urlAuthorizationDTO.getRoleIds().contains(Role.PLAY_BOOK.getRoleId());
		boolean isPlaybookSharedToPartnerCompany = lmsDao.isLMSSharedToPartnerCompanyByPartnerId(loggedInUserId,
				LearningTrackType.PLAYBOOK, vendorCompanyId);
		boolean playBookAccessAsPartner = utilDao.playbookAccessForPartner(loggedInUserId);
		boolean isPlaybookSharedToPartnerOrPlaybookAccessAsPartner = isPlaybookUrlAccessibleByPartner
				&& (isPlaybookSharedToPartnerCompany || playBookAccessAsPartner);

		boolean isPlaybookModuleEnabledBySuperAdmin = !isPlaybookUrlAccessibleByPartner
				&& utilDao.hasPlaybookAccessByUserId(loggedInUserId);

		boolean playBookAccess = urlAuthorizationDTO.isVendorAdmin()
				|| isPlaybookSharedToPartnerOrPlaybookAccessAsPartner || hasPlaybookRole;
		boolean hasAccess = isPlaybookUrlAccessibleByPartner ? playBookAccess
				: playBookAccess && isPlaybookModuleEnabledBySuperAdmin;
		if (!hasAccess) {
			denyAccess();
		}
	}

	private void denyAccess() {
		throw new AccessDeniedException(XamplifyConstants.USER_NOT_AUTHORIZED_TO_ACCESS_PAGE);
	}

}
