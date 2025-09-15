package com.xtremand.url.authorization.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.xtremand.url.authorization.dto.UrlAuthorizationDTO;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.dto.ModuleIdList;
import com.xtremand.util.dto.XamplifyConstants;

@Service
public class ApprovalHubUrlAuthorizationStrategy implements UrlAuthorizationStrategy {
	
	@Autowired
	private UtilDao utilDao;
	
	@Autowired
	private UserDAO userDAO;

	@Override
	public void authorize(UrlAuthorizationDTO urlAuthorizationDTO) {
		authorizeForDefaultLogin(urlAuthorizationDTO);
	}

	@Override
	public boolean isModuleIdMatched(Integer moduleId) {
		return ModuleIdList.APPROVAL_HUB.equals(moduleId);
	}
	
	private void authorizeForDefaultLogin(UrlAuthorizationDTO urlAuthorizationDTO) {
		Integer loggedInUserId = urlAuthorizationDTO.getLoggedInUserId();
		Integer vendorCompanyId = urlAuthorizationDTO.getLoggedInUserCompanyId();
		boolean isApprovalHubModuleEnabledBySuperAdmin = utilDao.hasApprovalHubAccessByUserId(loggedInUserId);
		boolean hasAssetApprovalRequired = userDAO.checkIfAssetApprovalRequiredByCompanyId(vendorCompanyId);
		boolean hasTracksApprovalRequired = userDAO.checkIfTracksApprovalRequiredByCompanyId(vendorCompanyId);
		boolean hasPlayBookApprovalRequired = userDAO.checkIfPlaybooksApprovalRequiredByCompanyId(vendorCompanyId);
		boolean hasContentApprovalAccess = hasAssetApprovalRequired || hasTracksApprovalRequired
				|| hasPlayBookApprovalRequired;
		boolean hasAccess = isApprovalHubModuleEnabledBySuperAdmin && hasContentApprovalAccess;
		if (!hasAccess) {
			denyAccess();
		}
	}

	private void denyAccess() {
		throw new AccessDeniedException(XamplifyConstants.USER_NOT_AUTHORIZED_TO_ACCESS_PAGE);
	}

}
