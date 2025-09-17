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
public class LmsUrlAuthorizationStrategy implements UrlAuthorizationStrategy {

	@Autowired
	private UtilDao utilDao;

	@Autowired
	private LMSDAO lmsDao;

	@Override
	public boolean isModuleIdMatched(Integer moduleId) {
		return ModuleIdList.LEARNING_TRACK.equals(moduleId);
	}

	@Override
	public void authorize(UrlAuthorizationDTO urlAuthorizationDTO) {
		VanityUrlDetailsDTO vanityUrlDetailsDTO = urlAuthorizationDTO.getVanityUrlDetailsDTO();
		String routerUrl = urlAuthorizationDTO.getRouterUrl();
		AuthorizationParametersDTO authorizationParametersDTO = new AuthorizationParametersDTO(vanityUrlDetailsDTO,
				routerUrl);
		boolean isLmsUrlAccessibleByPartner = isLmsUrlForOnlyPartnerLogin(urlAuthorizationDTO);
		if (authorizationParametersDTO.isLoggedInThroughVanity()) {
			authorizeVanityLogin(authorizationParametersDTO, isLmsUrlAccessibleByPartner, urlAuthorizationDTO);
		} else {
			authorizeForDefaultLogin(urlAuthorizationDTO, isLmsUrlAccessibleByPartner);
		}
	}

	private boolean isLmsUrlForOnlyPartnerLogin(UrlAuthorizationDTO urlAuthorizationDTO) {
		String routerUrl = urlAuthorizationDTO.getRouterUrl();
		return validateIfPartnerLoginAccessUrl(routerUrl) || "tb".equalsIgnoreCase(routerUrl);
	}

	private boolean validateIfPartnerLoginAccessUrl(String routerUrl) {
		return "shared".equalsIgnoreCase(routerUrl);
	}

	private void authorizeVanityLogin(AuthorizationParametersDTO authorizationParametersDTO,
			boolean isLmsUrlAccessibleByPartner, UrlAuthorizationDTO urlAuthorizationDTO) {
		if (authorizationParametersDTO.isLoggedInThroughOwnVanityUrl()) {
			authorizeForOwnVanityLogin(urlAuthorizationDTO);
		} else if (authorizationParametersDTO.isPartnerLoggedInThroughVanityUrl()) {
			authorizeForPartnerVanityLogin(urlAuthorizationDTO, isLmsUrlAccessibleByPartner);
		}
	}

	private void authorizeForOwnVanityLogin(UrlAuthorizationDTO urlAuthorizationDTO) {
		boolean isLmsModuleEnabledBySuperAdmin = utilDao.hasLmsAccessByUserId(urlAuthorizationDTO.getLoggedInUserId());
		boolean isLmsRole = urlAuthorizationDTO.getRoleIds().contains(Role.LEARNING_TRACK.getRoleId());
		boolean lmsAccess = urlAuthorizationDTO.isVendorAdmin() || isLmsRole;
		boolean isLmsPartnerUrl = validateIfPartnerLoginAccessUrl(urlAuthorizationDTO.getRouterUrl());
		boolean isUrlAuthorized = isLmsModuleEnabledBySuperAdmin && lmsAccess && !isLmsPartnerUrl;
		if (!isUrlAuthorized) {
			denyAccess();
		}
	}

	private void authorizeForPartnerVanityLogin(UrlAuthorizationDTO urlAuthorizationDTO,
			boolean isLmsUrlAccessibleByPartner) {
		Integer vendorCompanyId = urlAuthorizationDTO.getVanityUrlDetailsDTO().getVendorCompanyId();
		if (!isLmsUrlAccessibleByPartner || !isUrlAuthorizedForPartner(vendorCompanyId, urlAuthorizationDTO)) {
			denyAccess();
		}
	}

	private boolean isUrlAuthorizedForPartner(Integer vendorCompanyId, UrlAuthorizationDTO urlAuthorizationDTO) {
		boolean isLmsModuleEnabledBySuperAdmin = utilDao.hasLmsAccessByCompanyId(vendorCompanyId);
		boolean isLmsRole = urlAuthorizationDTO.getRoleIds().contains(Role.LEARNING_TRACK.getRoleId());
		boolean isLmsSharedToPartner = lmsDao.isLMSSharedToPartnerCompanyByPartnerId(
				urlAuthorizationDTO.getLoggedInUserId(), LearningTrackType.TRACK, vendorCompanyId);
		boolean lmsAccessAsPartner = urlAuthorizationDTO.isPartnerAdmin() || isLmsRole || isLmsSharedToPartner;

		boolean isVendorGivenAccessToPartner = utilDao.fetchModuleAccessForPartnerByModuleIdAndVendorCompanyIdAndUserId(vendorCompanyId, 18, urlAuthorizationDTO.getLoggedInUserId());
		
		return isLmsModuleEnabledBySuperAdmin && lmsAccessAsPartner && isVendorGivenAccessToPartner;
	}

	private void authorizeForDefaultLogin(UrlAuthorizationDTO urlAuthorizationDTO,
			boolean isLmsUrlAccessibleByPartner) {
		Integer loggedInUserId = urlAuthorizationDTO.getLoggedInUserId();
		Integer vendorCompanyId = urlAuthorizationDTO.getVanityUrlDetailsDTO().getVendorCompanyId();
		boolean isLmsRole = urlAuthorizationDTO.getRoleIds().contains(Role.LEARNING_TRACK.getRoleId());
		boolean isLmsSharedToPartnerCompany = lmsDao.isLMSSharedToPartnerCompanyByPartnerId(loggedInUserId, 
				LearningTrackType.TRACK, vendorCompanyId);
		boolean lmsAccessAsPartner = utilDao.lmsAccessForPartner(loggedInUserId);
		boolean isLmsSharedToPartnerOrLmsAccessAsPartner = isLmsUrlAccessibleByPartner
				&& (isLmsSharedToPartnerCompany || lmsAccessAsPartner);

		boolean isLmsModuleEnabledBySuperAdmin = !isLmsUrlAccessibleByPartner
				&& utilDao.hasLmsAccessByUserId(loggedInUserId);

		boolean lmsAccess = urlAuthorizationDTO.isVendorAdmin() || isLmsSharedToPartnerOrLmsAccessAsPartner
				|| isLmsRole;
		boolean hasAccess = isLmsUrlAccessibleByPartner ? lmsAccess : lmsAccess && isLmsModuleEnabledBySuperAdmin;
		if (!hasAccess) {
			denyAccess();
		}
	}

	private void denyAccess() {
		throw new AccessDeniedException(XamplifyConstants.USER_NOT_AUTHORIZED_TO_ACCESS_PAGE);
	}

}
