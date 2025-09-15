package com.xtremand.url.authorization.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.xtremand.dam.dao.DamDao;
import com.xtremand.url.authorization.dto.AuthorizationParametersDTO;
import com.xtremand.url.authorization.dto.UrlAuthorizationDTO;
import com.xtremand.user.bom.Role;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.dto.ModuleIdList;
import com.xtremand.util.dto.XamplifyConstants;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;

@Service
public class DamUrlAuthorizationStrategy implements UrlAuthorizationStrategy {

	@Autowired
	private UtilDao utilDao;

	@Autowired
	private DamDao damDao;

	@Override
	public void authorize(UrlAuthorizationDTO urlAuthorizationDTO) {
		VanityUrlDetailsDTO vanityUrlDetailsDTO = urlAuthorizationDTO.getVanityUrlDetailsDTO();
		AuthorizationParametersDTO authorizationParametersDTO = new AuthorizationParametersDTO(vanityUrlDetailsDTO,
				urlAuthorizationDTO.getRouterUrl());
		boolean isDamUrlAccessibleByPartner = isDamUrlForOnlyPartnerLogin(urlAuthorizationDTO.getRouterUrl());
		if (authorizationParametersDTO.isLoggedInThroughVanity()) {
			authorizeVanityLogin(authorizationParametersDTO, isDamUrlAccessibleByPartner, urlAuthorizationDTO);
		} else {
			authorizeForDefaultLogin(urlAuthorizationDTO, isDamUrlAccessibleByPartner);
		}
	}

	private void authorizeVanityLogin(AuthorizationParametersDTO authorizationParametersDTO,
			boolean isDamUrlAccessibleByPartner, UrlAuthorizationDTO urlAuthorizationDTO) {
		if (authorizationParametersDTO.isLoggedInThroughOwnVanityUrl()) {
			authorizeForOwnVanityLogin(urlAuthorizationDTO);
		} else if (authorizationParametersDTO.isPartnerLoggedInThroughVanityUrl()) {
			authorizeForPartnerVanityLogin(urlAuthorizationDTO, isDamUrlAccessibleByPartner);
		}
	}

	private void authorizeForPartnerVanityLogin(UrlAuthorizationDTO urlAuthorizationDTO,
			boolean isDamUrlAccessibleByPartner) {
		Integer vendorCompanyId = urlAuthorizationDTO.getVanityUrlDetailsDTO().getVendorCompanyId();
		if (!isDamUrlAccessibleByPartner || !isUrlAuthorizedForPartner(vendorCompanyId, urlAuthorizationDTO)) {
			denyAccess();
		}
	}

	private boolean isUrlAuthorizedForPartner(Integer vendorCompanyId, UrlAuthorizationDTO urlAuthorizationDTO) {
		boolean isDamModuleEnabledBySuperAdmin = utilDao.hasDamAccessByCompanyId(vendorCompanyId);
		boolean isDamRole = urlAuthorizationDTO.getRoleIds().contains(Role.DAM.getRoleId());
		boolean isAssetSharedToPartner = damDao.isAssetSharedToPartnerCompanyByPartnerIdAndVendorCompany(
				urlAuthorizationDTO.getLoggedInUserId(), vendorCompanyId);
		boolean damAccessAsPartner = urlAuthorizationDTO.isPartnerAdmin() || isDamRole || isAssetSharedToPartner;
		
		boolean isVendorGivenAccessToPartner = utilDao.fetchModuleAccessForPartnerByModuleIdAndVendorCompanyIdAndUserId(vendorCompanyId, 5, urlAuthorizationDTO.getLoggedInUserId());

		return isDamModuleEnabledBySuperAdmin && damAccessAsPartner && isVendorGivenAccessToPartner;
	}

	private void authorizeForOwnVanityLogin(UrlAuthorizationDTO urlAuthorizationDTO) {
		boolean isDamModuleEnabledBySuperAdmin = utilDao.hasDamAccessByUserId(urlAuthorizationDTO.getLoggedInUserId());
		boolean isDamRole = urlAuthorizationDTO.getRoleIds().contains(Role.DAM.getRoleId());
		boolean damAccess = urlAuthorizationDTO.isVendorAdmin() || isDamRole;
		boolean isDamPartnerUrl = isDamUrlForOnlyPartnerLogin(urlAuthorizationDTO.getRouterUrl());
		boolean isUrlAuthorized = isDamModuleEnabledBySuperAdmin && damAccess && (!isDamPartnerUrl || "vapv".equalsIgnoreCase(urlAuthorizationDTO.getRouterUrl()));
		if (!isUrlAuthorized) {
			denyAccess();
		}
	}

	private void authorizeForDefaultLogin(UrlAuthorizationDTO urlAuthorizationDTO,
			boolean isDamUrlAccessibleByPartner) {
		boolean isDamRole = urlAuthorizationDTO.getRoleIds().contains(Role.DAM.getRoleId());
		Integer loggedInUserId = urlAuthorizationDTO.getLoggedInUserId();
		boolean isAssetSharedToPartnerCompany = damDao.isAssetSharedToPartnerCompanyByPartnerId(loggedInUserId);
		boolean damAccessAsPartner = utilDao.damAccessForPartner(loggedInUserId);
		boolean isAssetSharedToPartnerOrDamAccessAsPartner = isDamUrlAccessibleByPartner
				&& (isAssetSharedToPartnerCompany || damAccessAsPartner);

		boolean isDamModuleEnabledBySuperAdmin = !isDamUrlAccessibleByPartner
				&& utilDao.hasDamAccessByUserId(loggedInUserId);

		boolean damAccess = urlAuthorizationDTO.isVendorAdmin() || isDamRole
				|| isAssetSharedToPartnerOrDamAccessAsPartner;
		boolean hasAccess = isDamUrlAccessibleByPartner ? damAccess : damAccess && isDamModuleEnabledBySuperAdmin;
		if (!hasAccess) {
			denyAccess();
		}
	}

	private void denyAccess() {
		throw new AccessDeniedException(XamplifyConstants.USER_NOT_AUTHORIZED_TO_ACCESS_PAGE);
	}

	private boolean isDamUrlForOnlyPartnerLogin(String routerUrl) {
		return "shared".equalsIgnoreCase(routerUrl) || "pda".equalsIgnoreCase(routerUrl)
				|| "editp".equalsIgnoreCase(routerUrl) || "vapv".equalsIgnoreCase(routerUrl);
	}

	@Override
	public boolean isModuleIdMatched(Integer moduleId) {
		return ModuleIdList.DAM_MODULE.equals(moduleId);
	}

}
